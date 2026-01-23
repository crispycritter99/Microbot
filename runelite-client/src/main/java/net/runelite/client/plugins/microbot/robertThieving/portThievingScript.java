package net.runelite.client.plugins.microbot.robertThieving;

import net.runelite.api.NPC;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.util.gameobject.Rs2GameObject;
import net.runelite.client.plugins.microbot.util.inventory.InteractOrder;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.npc.Rs2Npc;
import net.runelite.client.plugins.microbot.util.npc.Rs2NpcModel;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;

import java.util.concurrent.TimeUnit;


public class portThievingScript extends Script {
    public static boolean tentacle = false;
    NPC vorkath;
    public static boolean lootnet = false;
     boolean test = false;
    public boolean run(portThievingConfig config) {
        Microbot.enableAutoRunOn = false;

        mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> {
            try {
                if (!Microbot.isLoggedIn()) return;
                if (!super.run()) return;
                long startTime = System.currentTimeMillis();
                    if (Rs2Player.isMoving()) return;
                    if (Rs2Inventory.isFull()) {
                        Rs2Inventory.dropAll();
                        WorldPoint placeHolder = Rs2Player.getWorldLocation();
//                        Rs2DepositBox.openDepositBox();
//                        sleepUntil(Rs2DepositBox::isOpen);
//                        Rs2DepositBox.depositAll();
//                        Rs2Walker.walkFastCanvas(placeHolder);
//                        Rs2Player.waitForWalking();
                        boolean successfullAction = false;
                        boolean outOfStock = false;
                    }
                    WorldPoint gemPatrolSpot = new WorldPoint(1869, 3291, 0);
                    WorldPoint spicePatrolSpot = new WorldPoint(1864, 3290, 0);
                    Rs2NpcModel closestGuard=Rs2Npc.getNpc("Market Guard");
                    Rs2Player.getLocalPlayer().getCurrentOrientation();
                if (Rs2Player.getWorldLocation().distanceTo(new WorldPoint(1866,3293,0))>10) return;
                        //&&Rs2Player.getLocalPlayer().getCurrentOrientation()!=1641
                if (!portThievingPlugin.watching.get(portThievingPlugin.StallTypes.GEM)&&Rs2Player.getWorldLocation().distanceTo(gemPatrolSpot)>3){                            Rs2Inventory.dropAmount("Spice", 3, InteractOrder.ZIGZAG);
                    Rs2GameObject.interact(58106, "Steal-from"); //steal from gem stall
                    sleep(600);

//                         Rs2GameObject.hoverOverObject(Rs2GameObject.get("Silver stall"));

                }
                else  if (portThievingPlugin.watching.get(portThievingPlugin.StallTypes.GEM)&&Rs2Player.getWorldLocation().distanceTo(gemPatrolSpot)<3) {
                    Rs2Inventory.interact(24481, "Fill");
                    Rs2GameObject.interact(58105, "Steal-from");//steal from silver stall
                    sleep(600);
                }



                long endTime = System.currentTimeMillis();
                long totalTime = endTime - startTime;
                System.out.println("Total time for loop " + totalTime);

            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
        }, 0, 67, TimeUnit.MILLISECONDS);
        return true;
    }

    @Override
    public void shutdown() {
        super.shutdown();
    }

}
