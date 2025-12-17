package net.runelite.client.plugins.microbot.robertThieving;

import net.runelite.api.NPC;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.util.bank.Rs2Bank;
import net.runelite.client.plugins.microbot.util.depositbox.Rs2DepositBox;
import net.runelite.client.plugins.microbot.util.gameobject.Rs2GameObject;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.misc.Rs2UiHelper;
import net.runelite.client.plugins.microbot.util.npc.Rs2Npc;
import net.runelite.client.plugins.microbot.util.npc.Rs2NpcModel;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;
import net.runelite.client.plugins.microbot.util.shop.Rs2Shop;
import net.runelite.client.plugins.microbot.util.shop.models.Rs2ShopSource;
import net.runelite.client.plugins.microbot.util.walker.Rs2Walker;

import java.awt.*;
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
                    if ((Rs2Player.isAnimating())||Rs2Player.isMoving()) {
                        return;

                }
                    if (Rs2Inventory.isFull()) {
                        WorldPoint placeHolder = Rs2Player.getWorldLocation();
                        Rs2DepositBox.openDepositBox();
                        sleepUntil(Rs2DepositBox::isOpen);
                        Rs2DepositBox.depositAll();
////                        Rs2DepositBox.closeDepositBox();
//                        Rs2Npc.interact("Samir","Trade");
//                        sleepUntil(() -> Rs2Shop.openShop("Samir", false), 5000);
                        Rs2Walker.walkFastCanvas(placeHolder);
                        Rs2Player.waitForWalking();
                        boolean successfullAction = false;
                        boolean outOfStock = false;
//                        if (Rs2Shop.isOpen()) {
//
//
//                                if (Rs2Shop.isFull()) continue;
//                                // Check if name is purely numeric or alphanumeric
//                                if (itemName.matches("\\d+")) {
//                                    while (isRunning() && processSellAction(Integer.parseInt(itemName), "50")) {
//                                        sleepGaussian(200, 40);
//
//                                    }
//                                } else {
//                                    while (isRunning() && processSellAction(itemName, "50")) {
//                                        sleepGaussian(200, 40);
//
//                                    }
//                                }
//
//                        }
//                        Rs2Npc.interact("Amos","Trade");
//                        sleepUntil(() -> Rs2Shop.openShop("Amos", false), 5000);
//                        Rs2Walker.walkFastCanvas(placeHolder);
//                        Rs2Player.waitForWalking();
//
//                        if (Rs2Shop.isOpen()) {
//                            for (String itemName : portThievingPlugin.getItemNames()) {
//                                if (!isRunning() || Microbot.pauseAllScripts.get()) break;
//                                if (itemName.length() <= 1) continue;
//
//                                if (Rs2Shop.isFull()) continue;
//                                // Check if name is purely numeric or alphanumeric
//                                if (itemName.matches("\\d+")) {
//                                    while (isRunning() && processSellAction(Integer.parseInt(itemName), "50")) {
//                                        sleepGaussian(200, 40);
//
//                                    }
//                                } else {
//                                    while (isRunning() && processSellAction(itemName, "50")) {
//                                        sleepGaussian(200, 40);
//
//                                    }
//                                }
//                            }
//                        }
                    }
                    WorldPoint silverPatrolSpot = new WorldPoint(1866, 3290, 0);
                    WorldPoint spicePatrolSpot = new WorldPoint(1864, 3290, 0);
                    Rs2NpcModel closestGuard=Rs2Npc.getNpc("Market Guard");
                    Rs2Player.getLocalPlayer().getCurrentOrientation();
                    if (closestGuard!=null) {
                        //&&Rs2Player.getLocalPlayer().getCurrentOrientation()!=1641
                     if (closestGuard.getWorldLocation().distanceTo(silverPatrolSpot) < 1) {
//                        sleep(0,200);
                         sleepUntilTick(8);
                        Rs2GameObject.interact(58104, "Steal-from");
                         Rs2GameObject.hoverOverObject(Rs2GameObject.get("Spice stall"));
                         sleepUntilTick(8);

                         Rs2GameObject.interact(58105, "Steal-from");
                         Rs2GameObject.hoverOverObject(Rs2GameObject.get("Silver stall"));
//                        sleep(600);

                    }
//                     else if (closestGuard.getWorldLocation().distanceTo(spicePatrolSpot) > 1&&Rs2Player.getLocalPlayer().getCurrentOrientation()!=407) {
////                        sleep(0,200);
//                            Rs2GameObject.interact(58105, "Steal-from");
//                            Rs2GameObject.hoverOverObject(Rs2GameObject.get("Silver stall"));
//                            sleep(600);
//                        }

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
    /**
     * Processes the sell action for the specified item.
     * @param itemName The name of the item to sell.
     * @param quantity The quantity of the item to sell.
     * @return true if sold successfully, false otherwise.
     */
    private boolean processSellAction(String itemName, String quantity) {
        if (Rs2Inventory.hasItem(itemName)) {
            boolean soldItem = Rs2Inventory.sellItem(itemName, quantity);
            System.out.println(soldItem ? "Successfully sold " + quantity + " " + itemName : "Failed to sell " + quantity + " " + itemName);
            return soldItem;
        }
        System.out.println("Item " + itemName + " not found in inventory.");
        return false;
    }

    /**
     * Processes the sell action for the specified item.
     * @param itemID The name of the item to sell.
     * @param quantity The quantity of the item to sell.
     * @return true if sold successfully, false otherwise.
     */
    private boolean processSellAction(int itemID, String quantity) {
        if (Rs2Inventory.hasItem(itemID)) {
            boolean soldItem = Rs2Inventory.sellItem(itemID, quantity);
            System.out.println(soldItem ? "Successfully sold " + quantity + ", item ID:" + itemID : "Failed to sell " + quantity + ", item ID: " + itemID);
            return soldItem;
        }
        System.out.println("Item ID" + itemID + " not found in inventory.");
        return false;
    }
}
