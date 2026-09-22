package net.runelite.client.plugins.microbot.bloodwoodChopper;

import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.util.antiban.Rs2Antiban;
import net.runelite.client.plugins.microbot.util.antiban.Rs2AntibanSettings;
import net.runelite.client.plugins.microbot.util.gameobject.Rs2GameObject;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;

import java.util.Random;
import java.util.concurrent.TimeUnit;

public class BloodwoodChopperScript extends Script {
    public boolean run(BloodwoodChopperConfig config) {
        Microbot.enableAutoRunOn = false;
        Rs2Antiban.resetAntibanSettings();
        Rs2Antiban.antibanSetupTemplates.applyWoodcuttingSetup();
        Rs2AntibanSettings.dynamicActivity = true;
        Rs2AntibanSettings.dynamicIntensity = true;
        mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> {
            if (!Microbot.isLoggedIn() || !super.run()) {
                return;
            }
                if (Rs2Player.isAnimating(6500)||Rs2Player.isMoving()) return;
                double LOG_MEAN = 1; double LOG_STD = 0.8;
                Random r = new Random();double gaussian = r.nextGaussian();
                double value = Math.exp(LOG_MEAN + LOG_STD * gaussian);
                sleep((int) value * 100+2000);

                            if (!Rs2Inventory.contains(1925)){
                    shutdown();
                    return;
                }

                if (Rs2Inventory.contains(33833)){
                    Rs2Inventory.interact(33833,"Release");
                    Rs2Inventory.waitForInventoryChanges(1800);
                     r = new Random(); gaussian = r.nextGaussian();
                     value = Math.exp(LOG_MEAN + LOG_STD * gaussian);
                    sleep((int) value * 400);
                }

                if (!Rs2Inventory.contains(33833)&&Rs2Inventory.contains(1925)){
                    Rs2GameObject.interact(10048,"Chop");
                    sleep(1800);
                }

        }, 0, 600, TimeUnit.MILLISECONDS);

        return true;
    }

    @Override
    public void shutdown() {
        super.shutdown();
    }
}
