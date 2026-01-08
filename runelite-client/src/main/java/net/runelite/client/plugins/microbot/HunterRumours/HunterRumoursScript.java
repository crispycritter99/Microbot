package net.runelite.client.plugins.microbot.HunterRumours;

import net.runelite.api.NpcID;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.falconry.FalconryConfig;
import net.runelite.client.plugins.microbot.falconry.FalconryScript;
import net.runelite.client.plugins.microbot.microhunter.AutoHunterConfig;
import net.runelite.client.plugins.microbot.microhunter.scripts.AutoChinScript;
import net.runelite.client.plugins.microbot.salamanderslocal.SalamanderLocalConfig;
import net.runelite.client.plugins.microbot.salamanderslocal.SalamanderLocalPlugin;
import net.runelite.client.plugins.microbot.salamanderslocal.SalamanderLocalScript;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.npc.Rs2Npc;
import net.runelite.client.plugins.microbot.util.npc.Rs2NpcModel;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;
import net.runelite.client.plugins.microbot.util.walker.Rs2Walker;
import net.runelite.client.plugins.microbot.util.dialogues.Rs2Dialogue;

import java.util.concurrent.TimeUnit;



/**
 * High-level controller for Hunter Rumours.
 * Delegates to FalconryScript / SalamanderLocalScript / AutoChinScript based on config.
 */
public class HunterRumoursScript extends Script
{
    // Wolf (Master) is in the Hunter Guild basement; taken from quest helper data.
    private static final WorldPoint HUNTER_GUILD_ACO_TILE = new WorldPoint(1561, 9461, 0);

    private final FalconryScript falconryScript;
    private final FalconryConfig falconryConfig;

    private final AutoChinScript autoChinScript;
    private final AutoHunterConfig autoHunterConfig;

    private final SalamanderLocalScript salamanderScript;
    private final SalamanderLocalConfig salamanderConfig;

    // We only use SalamanderLocalPlugin as a holder for the traps map;
    // this instance will have an empty trap map unless you also wire up its event handlers.
    private final SalamanderLocalPlugin salamanderPluginStub;

    private HunterRumoursConfig config;

    private HunterRumourTaskType activeTask = null;
    private boolean turningInRumour = false;

    public HunterRumoursScript(
            HunterRumoursConfig config,
            FalconryScript falconryScript, FalconryConfig falconryConfig,

            AutoChinScript autoChinScript, AutoHunterConfig autoHunterConfig,

            SalamanderLocalScript salamanderScript, SalamanderLocalConfig salamanderConfig

    )
    {

        this.config = config;
        this.falconryScript = falconryScript;
        this.falconryConfig = falconryConfig;

        this.autoChinScript = autoChinScript;
        this.autoHunterConfig = autoHunterConfig;

        this.salamanderScript = salamanderScript;
        this.salamanderConfig = salamanderConfig;


        // Stub plugin solely so salamanderScript.getTraps() doesn't NPE
        this.salamanderPluginStub = new SalamanderLocalPlugin();
    }

    public boolean run()
    {
        Microbot.enableAutoRunOn = false;

        mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() ->
        {
            try
            {
                if (!Microbot.isLoggedIn())
                {
                    return;
                }
                if (!super.run())
                {
                    return;
                }
                if (!this.isRunning())
                {
                    return;
                }

                loop();
            }
            catch (Exception ex)
            {
                Microbot.logStackTrace(this.getClass().getSimpleName(), ex);
            }
        }, 0, 600, TimeUnit.MILLISECONDS);

        return true;
    }

    private void loop()
    {
        // If we're in the middle of turning in a rumour, keep doing that first.
        if (turningInRumour)
        {
            stopActiveTask();
            if (handleTurnInRumour())
            {
                // Turn-in complete
                turningInRumour = false;
                activeTask = null;
                Microbot.status = "Rumour turned in - waiting for new rumour / config update";
            }
            return;
        }

        HunterRumourTaskType rumourType = resolveCurrentRumourType();
        if (rumourType == null || rumourType == HunterRumourTaskType.AUTO)
        {
            Microbot.status = "Hunter Rumours: No rumour type selected";
            stopActiveTask();
            return;
        }

        // If we have the completion item for this rumour type -> stop subscript and go turn it in.
        if (hasCompletionItemFor(rumourType))
        {
            Microbot.status = "Rumour completed - returning to Hunter Guild";
            stopActiveTask();
            turningInRumour = true;
            return;
        }

        // If no script is running yet, start the appropriate one.
        if (activeTask == null)
        {
            startTask(rumourType);
        }
        else
        {
            // Script is running; just keep letting it run.
            Microbot.status = "Hunter Rumours: running " + activeTask.name();
        }
    }

    private HunterRumourTaskType resolveCurrentRumourType()
    {
        HunterRumourTaskType type = config.currentRumour();
        if (type != null && type != HunterRumourTaskType.AUTO)
        {
            return type;
        }

        // If you later want true auto-detection, this is where you'd:
        // - read varbits, or
        // - parse recent chat messages from Wolf.
        // For now we just return null in AUTO mode.
        return null;
    }

    private boolean hasCompletionItemFor(HunterRumourTaskType type)
    {
        String itemName = null;
        switch (type)
        {
            case FALCONRY:
                itemName = "Kebbity tuft";
                break;
            case SALAMANDERS:
                itemName = "salamander claw";
                break;
            case CHINCHOMPAS:
                itemName = "Red chinchompa tuft";
                break;
            default:
                break;
        }

        if (itemName == null || itemName.isEmpty())
        {
            return false;
        }

        return Rs2Inventory.contains(false,itemName);
    }

    private void startTask(HunterRumourTaskType type)
    {
        stopActiveTask(); // just in case

        switch (type)
        {
            case FALCONRY:
                Microbot.status = "Starting Falconry for rumour";
                falconryScript.run(falconryConfig);
                activeTask = HunterRumourTaskType.FALCONRY;
                break;

            case SALAMANDERS:
                Microbot.status = "Starting Salamanders for rumour";
                salamanderScript.run(salamanderConfig, salamanderPluginStub);
                activeTask = HunterRumourTaskType.SALAMANDERS;
                break;

            case CHINCHOMPAS:
                Microbot.status = "Starting AutoChins for rumour";
                autoChinScript.run(autoHunterConfig);
                activeTask = HunterRumourTaskType.CHINCHOMPAS;
                break;
        }
    }

    private void stopActiveTask()
    {
        if (activeTask == null)
        {
            return;
        }

        try
        {
            switch (activeTask)
            {
                case FALCONRY:
                    falconryScript.shutdown();
                    break;
                case SALAMANDERS:
                    salamanderScript.shutdown();
                    break;
                case CHINCHOMPAS:
                    autoChinScript.shutdown();
                    break;
                default:
                    break;
            }

        }
        catch (Exception e)
        {
            Microbot.log("Error stopping " + activeTask + " script: " + e.getMessage());
        }
        finally
        {
            activeTask = null;
        }
    }

    /**
     * Handles walking back to the Hunter Guild and talking to Guild Hunter Wolf (Master)
     * until dialogue is finished (rumour turned in & new one assigned).
     *
     * @return true when we consider the turn-in complete.
     */
    private boolean handleTurnInRumour()
    {
        // 1) Walk to Wolf if we're not close.
        if (Rs2Player.getWorldLocation().distanceTo(HUNTER_GUILD_ACO_TILE) > 5)
        {
            Microbot.status = "Walking to Hunter Guild (Wolf)";
            Rs2Walker.walkTo(HUNTER_GUILD_ACO_TILE);
            return false;
        }

        // 2) If not in dialogue, initiate a talk.
        if (!Rs2Dialogue.isInDialogue() && !Rs2Player.isMoving() && !Rs2Player.isAnimating())
        {
            Rs2NpcModel wolf = Rs2Npc.getNpcs("Guild Hunter Aco", true)
                    .findFirst()
                    .orElse(null);

            if (wolf == null)
            {
                // Fallback: try by ID if name lookup fails
                wolf = Rs2Npc.getNpcs(npc -> npc.getId() == NpcID.GUILD_HUNTER_ACO_EXPERT)
                        .findFirst()
                        .orElse(null);
            }

            if (wolf != null)
            {
                Rs2Npc.interact(wolf, "Talk-to");
            }
            else
            {
                // We're near his tile but can't see him (instance/phase issue),
                // just keep trying in future ticks.
                Microbot.status = "Looking for Guild Hunter Aco...";
            }
            return false;
        }

        // 3) We are in dialogue; fast-forward it if configured.
        if (config.useQuickDialogue())
        {
            if (Rs2Dialogue.hasSelectAnOption())
            {
                // Default to option 1; adjust if rumour dialogue uses different options.
                Rs2Dialogue.keyPressForDialogueOption(1);
            }
            else if (Rs2Dialogue.hasContinue())
            {
                Rs2Dialogue.clickContinue();
            }
        }

        // 4) Once dialogue ends and we're still at Wolf, assume turn-in is done.
        if (!Rs2Dialogue.isInDialogue()
                && Rs2Player.getWorldLocation().distanceTo(HUNTER_GUILD_ACO_TILE) <= 5)
        {
            // At this point, a new rumour should be assigned.
            return true;
        }

        return false;
    }

    @Override
    public void shutdown()
    {
        super.shutdown();
        stopActiveTask();
    }
}
