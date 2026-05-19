package net.runelite.client.plugins.microbot.daeyaltmining;

import net.runelite.api.NPC;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.api.tileobject.Rs2TileObjectCache;
import net.runelite.client.plugins.microbot.api.tileobject.models.Rs2TileObjectModel;
import net.runelite.client.plugins.microbot.util.dialogues.Rs2Dialogue;
import net.runelite.client.plugins.microbot.util.gameobject.Rs2GameObject;
import net.runelite.client.plugins.microbot.util.keyboard.Rs2Keyboard;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;

import javax.inject.Inject;
import java.awt.event.KeyEvent;
import java.util.Random;
import java.util.concurrent.TimeUnit;


public class ExampleScript extends Script {
    public static boolean tentacle = false;
    NPC vorkath;
    private WorldPoint workingTile = null;
    @Inject
    Rs2TileObjectCache rs2TileObjectCache;
    public static boolean lootnet = false;
     boolean test = false;
    public boolean run(ExampleConfig config) {
        Microbot.enableAutoRunOn = false;

        mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> {
            try {
                if (!Microbot.isLoggedIn()) return;
                if (!super.run()) return;
                long startTime = System.currentTimeMillis();

                if (Rs2Player.isAnimating(6500)||Rs2Player.isMoving()) return;
                double LOG_MEAN = 1; double LOG_STD = 0.8;
                Random r = new Random();double gaussian = r.nextGaussian();
                double value = Math.exp(LOG_MEAN + LOG_STD * gaussian);
                sleep((int) value * 1000+2000);
                if (Rs2Player.isAnimating(6500)||Rs2Player.isMoving()) return;

                Rs2GameObject.interact(39095);
                sleep(600);


                long endTime = System.currentTimeMillis();
                long totalTime = endTime - startTime;
                System.out.println("Total time for loop " + totalTime);

            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
        }, 0, 100, TimeUnit.MILLISECONDS);
        return true;
    }
//                    if ((Rs2Player.isAnimating())||Rs2Player.isMoving()) {
//                if (Rs2Player.isAnimating() || Rs2Player.isMoving()) {
//                    return;
//                }
//                if (Rs2Player.isMoving()) {
//                    return;
//                }
//                if (!Rs2Inventory.isFull()) {
//                    if (Rs2GameObject.interact("tarnished chest")) {
//                        sleep(1800);
//                    }
//                }
//                Rs2Inventory.dropAll(false,"mithril","bar","cannonball");
//                if (!Rs2Inventory.contains(29354)) {
//                    if (!Rs2Inventory.contains(28899)) {
//                        Rs2Inventory.interact(28900);
//                        Rs2Inventory.useItemOnNpc(28900, 13346);
//                        sleep(2000);
//                        Rs2Player.waitForWalking(5000);
//                        Widget widget = Rs2Widget.findWidget("Exchanging:");
//                        if (widget != null) {
//                            Rs2Keyboard.keyPress(String.valueOf(3).charAt(0));
//                        }
//                        Rs2Inventory.waitForInventoryChanges(1800);
//                        return;
//                    }
//                    if (Rs2Inventory.contains(28899)) {
//                        Rs2GameObject.interact(52799, "Bless");
//                        Rs2Player.waitForWalking(5000);
//                        return;
//                    }
//                }
//                if (Rs2Inventory.contains(29354)){
//                    Rs2Inventory.interact(29354,"Break-down");
//                    sleep(2000);
//                    return;
//                }

//                sleep(2500,7800);
//                Rs2GameObject.interact(39095);
//                sleep(1200);
//                if (Rs2Player.isInteracting())return;
//                Microbot.status="ready to go";
    ////                Rs2Npc.interact("s   unlight moth","catch");

    @Override
    public void shutdown() {
        super.shutdown();
    }

}