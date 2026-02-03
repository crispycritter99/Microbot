package net.runelite.client.plugins.microbot.scurriuslocal;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Prayer;
import net.runelite.api.Projectile;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.ProjectileMoved;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.microbot.Microbot;
//import net.runelite.client.plugins.microbot.VardorvisHelper.VardorvisHelperScript;
import net.runelite.client.plugins.microbot.scurriuslocal.enums.State;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;
import net.runelite.client.plugins.microbot.util.prayer.Rs2Prayer;
import net.runelite.client.plugins.microbot.util.prayer.Rs2PrayerEnum;
import net.runelite.client.plugins.microbot.util.walker.Rs2Walker;
//import net.runelite.client.plugins.microbot.vardorvis.VardorvisScript;
import net.runelite.client.ui.overlay.OverlayManager;

import javax.inject.Inject;
import java.awt.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

//import static net.runelite.client.plugins.microbot.vardorvis.VardorvisScript.isProjectileActive;

@PluginDescriptor(
        name = PluginDescriptor.Default + "Scurrius",
        description = "Scurrius example plugin",
        tags = {"microbot", "scurrius", "boss"},
        enabledByDefault = false
)
@Slf4j
public class ScurriusLocalPlugin extends Plugin {
    private ExecutorService walkingExecutor;
    public static int currentRunningTicks = 0;
//    public static int remainingCycles2=0;
    public static Rs2PrayerEnum currentPrayer = null;
    private static final int RANGE_PROJECTILE = 2642;
    private static final int MAGE_PROJECTILE = 2640;
    @Inject
    private ScurriusConfig config;
    @Provides
    ScurriusConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(ScurriusConfig.class);
    }

    @Inject
    private OverlayManager overlayManager;
    @Inject
    private ScurriusOverlay exampleOverlay;

    @Inject
    ScurriusScript scurriusScript;


    @Override
    protected void startUp() throws AWTException {
        if (overlayManager != null) {
            overlayManager.add(exampleOverlay);
        }
        ScurriusScript.state = State.BANKING;
        scurriusScript.run(config);
        if (walkingExecutor == null || walkingExecutor.isShutdown() || walkingExecutor.isTerminated()) {
            walkingExecutor = Executors.newSingleThreadExecutor();
        }
    }

    protected void shutDown() {
        scurriusScript.shutdown();
        overlayManager.remove(exampleOverlay);
    }

    @Subscribe
    public void onChatMessage(ChatMessage event) {
        if (event.getType() != ChatMessageType.GAMEMESSAGE) return;
        
        if (event.getMessage().equalsIgnoreCase("oh dear, you are dead!") && config.shutdownAfterDeath()) {
            Rs2Walker.setTarget(null);
            shutDown();
        }
    }
    @Subscribe
    public void onGameTick(GameTick tick) {
//        Microbot.log(Rs2Npc.getNpc("Scurrius").getWorldLocation()+"");
        currentRunningTicks++;
        if (Microbot.getClient().isPrayerActive(Prayer.PROTECT_FROM_MELEE) && currentPrayer != Rs2PrayerEnum.PROTECT_MELEE) {
            currentPrayer = Rs2PrayerEnum.PROTECT_MELEE;
            //Microbot.log("Prayer swapped to MELEE");

        } else if (Microbot.getClient().isPrayerActive(Prayer.PROTECT_FROM_MISSILES) && currentPrayer != Rs2PrayerEnum.PROTECT_RANGE) {
            currentPrayer = Rs2PrayerEnum.PROTECT_RANGE;

            //Microbot.log("Prayer swapped to RANGE");
        }
    else if (Microbot.getClient().isPrayerActive(Prayer.PROTECT_FROM_MAGIC) && currentPrayer != Rs2PrayerEnum.PROTECT_MAGIC) {
        currentPrayer = Rs2PrayerEnum.PROTECT_MAGIC;

        //Microbot.log("Prayer swapped to RANGE");
    }
        if (currentPrayer != Rs2PrayerEnum.PROTECT_MELEE && !ScurriusScript.isProjectileActive &&(Rs2Player.isInCombat()||Rs2Player.isInteracting())&& ScurriusScript.state == State.FIGHTING) {
            Microbot.log("Toggling Protect Melee ON | Current tick = " + currentRunningTicks);
            walkingExecutor.submit(() -> {
                Rs2Prayer.toggle(Rs2PrayerEnum.PROTECT_MELEE, true);
            });
        }
    }
    @Subscribe
    public void onProjectileMoved(ProjectileMoved event) {
        if (ScurriusScript.state == State.FIGHTING) {
            handleProjectile(event);
        }
    }
    public void handleProjectile(ProjectileMoved event) {
        final Projectile projectile = event.getProjectile();
        final int remainingCycles = projectile.getRemainingCycles();
//        Microbot.log(""+projectile.getStartCycle());

        if (remainingCycles >= 10 && remainingCycles < 25) { // > 0
            ScurriusScript.isProjectileActive = true;

            if (projectile.getId() == RANGE_PROJECTILE && !Rs2Prayer.isPrayerActive(Rs2PrayerEnum.PROTECT_RANGE) && currentPrayer != Rs2PrayerEnum.PROTECT_RANGE && remainingCycles == 20) {// && remainingCycles == 30 was 10

                Microbot.log("Toggling Protect Range ON   | cycles = " + remainingCycles +  " | Current tick = " + currentRunningTicks);

                //currentPrayer = Rs2PrayerEnum.PROTECT_RANGE;

                walkingExecutor.submit(() -> {
                    Rs2Prayer.toggle(Rs2PrayerEnum.PROTECT_RANGE, true);
                });
            } else if (projectile.getId() == RANGE_PROJECTILE && !Rs2Prayer.isPrayerActive(Rs2PrayerEnum.PROTECT_RANGE) && currentPrayer != Rs2PrayerEnum.PROTECT_RANGE && remainingCycles == 10) {

                Microbot.log("Toggling Protect Range ON   | cycles = " + remainingCycles +  " | Current tick = " + currentRunningTicks);

                walkingExecutor.submit(() -> {
                    Rs2Prayer.toggle(Rs2PrayerEnum.PROTECT_RANGE, true);
                });
            }
            else if (projectile.getId() == MAGE_PROJECTILE && !Rs2Prayer.isPrayerActive(Rs2PrayerEnum.PROTECT_MAGIC) && currentPrayer != Rs2PrayerEnum.PROTECT_MAGIC && remainingCycles == 20) {// && remainingCycles == 30 was 10

                Microbot.log("Toggling Protect MAGE ON   | cycles = " + remainingCycles +  " | Current tick = " + currentRunningTicks);

                //currentPrayer = Rs2PrayerEnum.PROTECT_MAGE;

                walkingExecutor.submit(() -> {
                    Rs2Prayer.toggle(Rs2PrayerEnum.PROTECT_MAGIC, true);
                });
            } else if (projectile.getId() == MAGE_PROJECTILE && !Rs2Prayer.isPrayerActive(Rs2PrayerEnum.PROTECT_MAGIC) && currentPrayer != Rs2PrayerEnum.PROTECT_MAGIC && remainingCycles == 10) {

                Microbot.log("Toggling Protect MAGE ON   | cycles = " + remainingCycles +  " | Current tick = " + currentRunningTicks);

                walkingExecutor.submit(() -> {
                    Rs2Prayer.toggle(Rs2PrayerEnum.PROTECT_MAGIC, true);
                });
            }
        }
//        else if (remainingCycles == 0) {
//            Microbot.log("Vardorvis projectile stopped");
//            VardorvisScript.ScurriusScript.isProjectileActive = false;
//
//            walkingExecutor.submit(() -> {
//                Rs2Prayer.toggle(Rs2PrayerEnum.PROTECT_MELEE, true);
//            });
//        }
        else if (remainingCycles >= 0 && remainingCycles <= 2 && ScurriusScript.isProjectileActive) {
            Microbot.log("Vardorvis projectile stopped");
            ScurriusScript.isProjectileActive = false;

            walkingExecutor.submit(() -> {
                Rs2Prayer.toggle(Rs2PrayerEnum.PROTECT_MELEE, true);
            }, 50);
        }
    }
}
