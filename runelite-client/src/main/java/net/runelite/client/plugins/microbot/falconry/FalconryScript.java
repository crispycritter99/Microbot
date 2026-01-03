package net.runelite.client.plugins.microbot.falconry;

import net.runelite.api.NPC;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.npc.Rs2Npc;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;

import java.util.concurrent.TimeUnit;


public class FalconryScript extends Script {
    public static boolean tentacle = false;
    NPC vorkath;
    public static boolean lootnet = false;
     boolean test = false;
    public boolean run(FalconryConfig config) {
        Microbot.enableAutoRunOn = false;

        mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> {
            try {
                if (!Microbot.isLoggedIn()) return;
                if (!super.run()) return;
                long startTime = System.currentTimeMillis();
                    if ((Rs2Player.isAnimating())||Rs2Player.isMoving()) {
//
                        return;

                }
                if (Rs2Player.getPoseAnimation()==5160) {
                    Rs2Inventory.dropAll(false,  "fur");
                    Rs2Inventory.dropAll(true,  "bones");
                }
                    if (FalconryPlugin.falcon!=null){
                Rs2Npc.interact(FalconryPlugin.falcon,"retrieve");
                        sleep(500);}
                if (Rs2Player.getPoseAnimation()==5160){
                    Rs2Npc.interact("Dark kebbit", "Catch");
                    sleep(500);
                }
                long endTime = System.currentTimeMillis();
                long totalTime = endTime - startTime;
                System.out.println("Total time for loop " + totalTime);

            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
        }, 0, 600, TimeUnit.MILLISECONDS);
        return true;
    }

    @Override
    public void shutdown() {
        super.shutdown();
    }

}