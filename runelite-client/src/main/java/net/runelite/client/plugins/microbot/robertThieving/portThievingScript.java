package net.runelite.client.plugins.microbot.robertThieving;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.util.depositbox.Rs2DepositBox;
import net.runelite.client.plugins.microbot.util.gameobject.Rs2GameObject;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;
import net.runelite.client.plugins.microbot.util.walker.Rs2Walker;

import javax.inject.Inject;
import java.util.concurrent.TimeUnit;

@Slf4j
public class portThievingScript extends Script
{
    @Inject
    private portThievingPlugin plugin;

    private static final int GEM_STALL_ID         = 58106;
    private static final int GEM_BAG_ID           = 24481;
    private static final int STEAL_SLEEP_MS       = 600;
    private static final int MAX_STEALS_PER_CYCLE = 4;

    // Inventory count snapshotted when a safe window opens
    private int cycleBaseline = -1;
    // Tracks whether we were in a safe window last tick
    private boolean inSafeWindow = false;
    // Prevents gem bag fill from firing more than once per idle phase
    private boolean filledThisCycle = false;

    public boolean run(portThievingConfig config)
    {
        Microbot.enableAutoRunOn = false;

        mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() ->
        {
            try
            {
                if (!Microbot.isLoggedIn()) return;
                if (!super.run())           return;
                if (Rs2Player.isMoving())   return;

                // Stay within the market area
                WorldPoint pos = Rs2Player.getWorldLocation();
                if (pos.distanceTo(portThievingPlugin.MARKET_CENTRE) > portThievingPlugin.MARKET_RADIUS)
                    return;

                portThievingPlugin.StallState gem = plugin.stalls.get(portThievingPlugin.StallTypes.GEM);
                boolean safeNow = gem.isSafe() && !gem.isWatched;

                // Detect the start of a new safe window and snapshot inventory size
                if (safeNow && !inSafeWindow)
                {
                    cycleBaseline = Rs2Inventory.count();
                    filledThisCycle = false;
                    log.debug("Safe window opened — baseline inventory: {}", cycleBaseline);
                }
                inSafeWindow = safeNow;

                // Predict whether there is enough safe time for another steal.
                // A full 4-steal cycle fits in the window; if we've already stolen 4
                // times since the window opened, the next steal would risk a guard catch.
                int stolenThisCycle = cycleBaseline >= 0
                        ? Rs2Inventory.count() - cycleBaseline
                        : 0;
                boolean cycleFull = stolenThisCycle >= MAX_STEALS_PER_CYCLE;

                if (!safeNow || cycleFull)
                {
                    // Guard here, window closed, or cycle exhausted — fill gem bag once then idle
                    if (!filledThisCycle && Rs2Inventory.contains(GEM_BAG_ID))
                    {
                        Rs2Inventory.interact(GEM_BAG_ID, "Fill");
                        filledThisCycle = true;
                        log.debug("Idling — guard incoming or cycle full ({} steals)", stolenThisCycle);
                    }
                    return;
                }

                // Don't queue another steal while the animation is still running
                if (Rs2Player.isAnimating()) return;

                // Bank when full
                if (Rs2Inventory.isFull())
                {
                    WorldPoint returnSpot = Rs2Player.getWorldLocation();
                    if (Rs2DepositBox.openDepositBox())
                    {
                        if (Rs2Inventory.contains("Open gem bag"))
                        {
                            Rs2Inventory.interact("Open gem bag", "Empty");
                            Rs2DepositBox.depositAllExcept("Open gem bag");
                        }
                        else
                        {
                            Rs2DepositBox.depositAll();
                        }
                        Rs2DepositBox.closeDepositBox();
                    }
                    Rs2Walker.walkFastCanvas(returnSpot);
                    Rs2Player.waitForWalking();
                    return;
                }

                // Safe, cycle not exhausted, not animating — steal
                boolean interacted = Rs2GameObject.interact(GEM_STALL_ID, "Steal-from");
                if (interacted)
                {
                    log.debug("Stealing from gem stall (steal {} of {})", stolenThisCycle + 1, MAX_STEALS_PER_CYCLE);
                    sleep(STEAL_SLEEP_MS);
                }

            }
            catch (Exception ex)
            {
                log.error("portThievingScript error: {}", ex.getMessage(), ex);
            }
        }, 0, 600, TimeUnit.MILLISECONDS);

        return true;
    }

    @Override
    public void shutdown()
    {
        super.shutdown();
    }
}