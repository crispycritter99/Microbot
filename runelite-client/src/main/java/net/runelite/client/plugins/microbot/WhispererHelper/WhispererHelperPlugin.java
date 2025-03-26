package net.runelite.client.plugins.microbot.WhispererHelper;


import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.MenuAction;
import net.runelite.api.Prayer;
import net.runelite.api.Projectile;
import net.runelite.api.Skill;
import net.runelite.api.events.*;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.WhispererHelper.enums.State;
import net.runelite.client.plugins.microbot.WhispererHelper.enums.StateBank;
import net.runelite.client.plugins.microbot.WhispererHelper.enums.StatePOH;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.math.Rs2Random;
import net.runelite.client.plugins.microbot.util.menu.NewMenuEntry;
import net.runelite.client.plugins.microbot.util.prayer.Rs2Prayer;
import net.runelite.client.plugins.microbot.util.prayer.Rs2PrayerEnum;
import net.runelite.client.ui.overlay.OverlayManager;

import javax.inject.Inject;
import java.awt.*;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static net.runelite.client.plugins.microbot.LeviathanHelper.LeviathanHelperScript.*;

@PluginDescriptor(
        name = "<html>[<font color=red>Neon</font>] " + "Whisperer Helper",
        description = "Vardorvis killer",
        tags = {"vardorvis", "boss"},
        enabledByDefault = false
)
@Slf4j
public class WhispererHelperPlugin extends Plugin {
    private ScheduledExecutorService scheduler;
    private ExecutorService executor;
    private ExecutorService sleepUntilExecutor;
    private ExecutorService walkingExecutor;

    private static final int RANGE_PROJECTILE = 2444;
    private static final int MAGIC_PROJECTILE = 2445;


    private final Set<Integer> trackedWidgets = Set.of(54591499, 54591498, 54591497, 54591496, 54591495, 54591494);

    private int tickCounter = 0;

    public static boolean oppositeAxe = false;
    public static int oppositeAxeCounter = 0;

    public static int currentRunningTicks = 0;

    public static Rs2PrayerEnum currentPrayer = Rs2PrayerEnum.RAPID_HEAL;

    public static boolean axeOnSafeTile = false;
    private static int axeOnSafeTileTick = 0;

    @Inject
    private WhispererHelperConfig config;
    @Provides
    WhispererHelperConfig provideConfig(ConfigManager configManager) { return configManager.getConfig(WhispererHelperConfig.class); }

    @Inject
    private OverlayManager overlayManager;
    @Inject
    private WhispererHelperOverlay whispererHelperOverlay;
    @Inject
    WhispererHelperScript whispererHelperScript;

    @Override
    protected void startUp() throws AWTException {
        if (overlayManager != null) {
            overlayManager.add(whispererHelperOverlay);
        }
        whispererHelperScript.run(config);

        if (executor == null || executor.isShutdown() || executor.isTerminated()) {
            executor = Executors.newSingleThreadExecutor();
        }
        if (scheduler == null || scheduler.isShutdown() || scheduler.isTerminated()) {
            scheduler = Executors.newScheduledThreadPool(1);
        }
        if (sleepUntilExecutor == null || sleepUntilExecutor.isShutdown() || sleepUntilExecutor.isTerminated()) {
            sleepUntilExecutor = Executors.newSingleThreadExecutor();
        }
        if (walkingExecutor == null || walkingExecutor.isShutdown() || walkingExecutor.isTerminated()) {
            walkingExecutor = Executors.newSingleThreadExecutor();
        }
    }

    @Subscribe
    public void onGameTick(GameTick tick) {
        currentRunningTicks++;
         if (Microbot.getClient().isPrayerActive(Prayer.PROTECT_FROM_MISSILES) && currentPrayer != Rs2PrayerEnum.PROTECT_RANGE) {
            currentPrayer = Rs2PrayerEnum.PROTECT_RANGE;

            //Microbot.log("Prayer swapped to RANGE");
        }
        else if (Microbot.getClient().isPrayerActive(Prayer.PROTECT_FROM_MAGIC) && currentPrayer != Rs2PrayerEnum.PROTECT_MAGIC) {
            currentPrayer = Rs2PrayerEnum.PROTECT_MAGIC;

            //Microbot.log("Prayer swapped to RANGE");
        }




//        if (currentPrayer != Rs2PrayerEnum.PROTECT_MELEE && !LeviathanHelperScript.isProjectileActive &&(Rs2Player.isInCombat()||Rs2Player.isInteracting())&& LeviathanHelperScript.inInstance) {
//            Microbot.log("Toggling Protect Melee ON | Current tick = " + currentRunningTicks);
//            walkingExecutor.submit(() -> {
//                Rs2Prayer.toggle(Rs2PrayerEnum.PROTECT_MELEE, true);
//            });
//        }
//        if (Rs2Npc.getNpc("Vardorvis")==null&&currentPrayer != Rs2PrayerEnum.PROTECT_MELEE)
//                currentPrayer = Rs2PrayerEnum.RAPID_HEAL;
//        walkingExecutor.submit(() -> {
//            Rs2Prayer.disableAllPrayers();
//        });
    }

    @Subscribe
    public void onClientTick(ClientTick tick) {
    }

    @Subscribe
    public void onNpcSpawned(NpcSpawned event) {

    }

    @Subscribe
    public void onChatMessage(ChatMessage message) {

    }

    @Subscribe
    public void onProjectileMoved(ProjectileMoved event) {
        if (Microbot.getClient().getTopLevelWorldView().getScene().isInstance()) {
            handleProjectile(event);
        }
    }

    @Subscribe
    public void onNpcDespawned(NpcDespawned event) {
        if (Objects.equals(event.getNpc().getName(), "Vardorvis") && inFight && inInstance) {
            Microbot.log("Vardorvis DEAD!!!!");

            WhispererHelperScript.inFight = false;
            whispererHelperScript.inInstance = false;
            walkingExecutor.submit(() -> {
                Rs2Prayer.toggleQuickPrayer(false);
            });

        }
    }

    public void handleProjectile(ProjectileMoved event) {
        final Projectile projectile = event.getProjectile();
        final int remainingCycles = projectile.getRemainingCycles();
//        Rs2Random.nextInt(25,50,)
        if (remainingCycles >= 10 && remainingCycles < 35) { // > 0
            WhispererHelperScript.isProjectileActive = true;

            if (projectile.getId() == RANGE_PROJECTILE && !Rs2Prayer.isPrayerActive(Rs2PrayerEnum.PROTECT_RANGE) && currentPrayer != Rs2PrayerEnum.PROTECT_RANGE && (remainingCycles == 20||remainingCycles == 10)) {// && remainingCycles == 30 was 10

                Microbot.log("Toggling Protect Range ON   | cycles = " + remainingCycles +  " | Current tick = " + currentRunningTicks);

                //currentPrayer = Rs2PrayerEnum.PROTECT_RANGE;

                walkingExecutor.submit(() -> {
                    Rs2Prayer.toggle(Rs2PrayerEnum.PROTECT_RANGE, true);
                });
            }else if (projectile.getId() == MAGIC_PROJECTILE && !Rs2Prayer.isPrayerActive(Rs2PrayerEnum.PROTECT_MAGIC) && currentPrayer != Rs2PrayerEnum.PROTECT_MAGIC && (remainingCycles == 20||remainingCycles == 10)) {// && remainingCycles == 30 was 10

                Microbot.log("Toggling Protect Magic ON   | cycles = " + remainingCycles +  " | Current tick = " + currentRunningTicks);

                //currentPrayer = Rs2PrayerEnum.PROTECT_RANGE;

                walkingExecutor.submit(() -> {
                    Rs2Prayer.toggle(Rs2PrayerEnum.PROTECT_MAGIC, true);
                });
            }
        }
//        else if (remainingCycles == 0) {
//            Microbot.log("Vardorvis projectile stopped");
//            VardorvisScript.isProjectileActive = false;
//
//            walkingExecutor.submit(() -> {
//                Rs2Prayer.toggle(Rs2PrayerEnum.PROTECT_MELEE, true);
//            });
//        }
//        else if (remainingCycles >= 0 && remainingCycles <= 2 && isProjectileActive) {
//            Microbot.log("Vardorvis projectile stopped");
//            LeviathanHelperScript.isProjectileActive = false;
//
//            walkingExecutor.submit(() -> {
//                Rs2Prayer.toggle(Rs2PrayerEnum.PROTECT_MELEE, true);
//            }, 50);
//        }
    }

    public void handleHealingAndPrayer() {
        Microbot.log("Handling healing | Current tick = " + currentRunningTicks);

        int currentHealth = Microbot.getClient().getBoostedSkillLevel(Skill.HITPOINTS);
        int currentPrayer = Microbot.getClient().getBoostedSkillLevel(Skill.PRAYER);

        if (currentPrayer < maxPrayer - 30) {
            Rs2Inventory.interact("prayer potion", "drink");
        }

        if (currentHealth < maxHealth - 45) {
            Microbot.doInvoke(new NewMenuEntry("Eat", Rs2Inventory.slot(385), 9764864, MenuAction.CC_OP.getId(), 2, 385, "Shark"), new Rectangle(1, 1));

            scheduler.schedule(() -> {
                Microbot.doInvoke(new NewMenuEntry("Eat", Rs2Inventory.slot(3144), 9764864, MenuAction.CC_OP.getId(), 2, 3144, "Cooked karambwan"), new Rectangle(1, 1));
            }, 30, TimeUnit.MILLISECONDS);
        }
    }

    protected void shutDown() {
        whispererHelperScript.shutdown();
        overlayManager.remove(whispererHelperOverlay);

        scheduler.shutdown();
        executor.shutdown();
        sleepUntilExecutor.shutdown();

        tickCounter = 0;
        oppositeAxeCounter = 0;
        currentRunningTicks = 0;
        currentPrayer = Rs2PrayerEnum.RAPID_HEAL;

        WhispererHelperScript.inFight = false;
        WhispererHelperScript.inInstance = false;
        WhispererHelperScript.state = State.UNKNOWN;
        WhispererHelperScript.bankState = StateBank.UNKNOWN;
        WhispererHelperScript.POHState = StatePOH.UNKNOWN;
        WhispererHelperScript.isProjectileActive = false;
    }
}
