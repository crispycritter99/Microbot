package net.runelite.client.plugins.microbot.runecrafting.mudrune;

import net.runelite.api.ItemID;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.util.bank.Rs2Bank;
import net.runelite.client.plugins.microbot.util.bank.enums.BankLocation;
import net.runelite.client.plugins.microbot.util.equipment.Rs2Equipment;
import net.runelite.client.plugins.microbot.util.gameobject.Rs2GameObject;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.inventory.Rs2ItemModel;
import net.runelite.client.plugins.microbot.util.magic.Rs2Magic;
import net.runelite.client.plugins.microbot.util.magic.Rs2Spells;
import net.runelite.client.plugins.microbot.util.math.Rs2Random;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;
import net.runelite.client.plugins.skillcalculator.skills.MagicAction;

import java.util.concurrent.TimeUnit;


public class mudScript extends Script {
    public static boolean tentacle = false;
    public static boolean lootnet = false;
     boolean test = false;
    public boolean run(mudConfig config) {
        Microbot.enableAutoRunOn = false;

        mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> {
            try {
                if (!Microbot.isLoggedIn()) return;
                if (!super.run()) return;
                long startTime = System.currentTimeMillis();
                Rs2Bank.walkToBankAndUseBank(BankLocation.CASTLE_WARS);
                if (!Rs2Bank.isOpen())
                        return;
                if (!Rs2Equipment.isWearing(ItemID.BINDING_NECKLACE))
                    Rs2Bank.withdrawAndEquip(ItemID.BINDING_NECKLACE);
                if (!Rs2Inventory.hasItem("Ring of dueling"))
                    Rs2Bank.withdrawOne("Ring of dueling");
                Rs2Bank.depositAll("Mud rune");
                Rs2Bank.withdrawAll("Pure essence");
                Rs2Inventory.waitForInventoryChanges(1800);
//                Rs2Inventory.wear("Large Pouch");
//                Rs2Inventory.wear("Medium Pouch");
//                Rs2Inventory.wear("Small Pouch");
                if (Rs2Inventory.getRemainingCapacityInPouches()>0) {
                    Rs2Inventory.interact(5512, "Fill");
                    Rs2Inventory.interact(5510, "Fill");
                    Rs2Inventory.interact(5509, "Fill");
                    Rs2Bank.withdrawAll("Pure essence");
                    Rs2Inventory.waitForInventoryChanges(1800);
                }
                if (Rs2Inventory.hasDegradedPouch() && Rs2Magic.hasRequiredRunes(Rs2Spells.NPC_CONTACT)) {
                    Rs2Magic.repairPouchesWithLunar();
                    Microbot.log("pouch repaired");
                    return;
                }
                Rs2Bank.closeBank();
                if (!Rs2Inventory.hasItem("Pure essence")) {
                    Microbot.log("no essence");
                    return;
                }
//                Rs2Walker.walkTo(3304,3471,0,10);
                Rs2Equipment.interact(26818,"Earth Altar");
                sleepUntil(()->Rs2GameObject.exists(1282));
                Rs2GameObject.interact(34816,"Enter");
//                Rs2GameObject.interact("Mysterious Ruins","Enter");
//                sleepUntil(()->!Rs2GameObject.exists(1282),10000);
                Rs2Player.waitForWalking(10000);
                Rs2Magic.cast(MagicAction.MAGIC_IMBUE);
                Rs2Inventory.useItemOnObject(555,34763);
                sleepUntil(()->!Rs2Inventory.contains(7936));
                Rs2Inventory.interact(5512,"empty");
                Rs2Random.wait(100,300);
                Rs2Inventory.interact(5510,"empty");
                Rs2Random.wait(100,300);
                Rs2Inventory.interact(5509,"empty");
                sleepUntil(()->Rs2Inventory.contains(7936));
                Rs2Inventory.useItemOnObject(555,34763);
                sleepUntil(()->!Rs2Inventory.contains(7936));
//                Rs2Walker.walkTo(2440,3089,0,10);
//                Rs2Inventory.interact("ring of dueling","rub");
//                Rs2Dialogue.sleepUntilInDialogue();
//                Rs2Dialogue.clickOption("Castle Wars");
//                sleepUntil(()->Rs2GameObject.exists(4483));
//                Rs2ItemModel ring = Rs2Inventory.get("ring of dueling");
//                Microbot.doInvoke(new NewMenuEntry("Ferox Enclave", ring.getSlot(), ComponentID.INVENTORY_CONTAINER, 1007, 196614, ring.id, ""), (Rs2Inventory.itemBounds(ring) == null) ? new Rectangle(1, 1) : Rs2Inventory.itemBounds(ring));


//                    Microbot.log("diddy");
//                shutdown();
                long endTime = System.currentTimeMillis();
                long totalTime = endTime - startTime;
                System.out.println("Total time for loop " + totalTime);

            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
        }, 0, 100, TimeUnit.MILLISECONDS);
        return true;
    }

    @Override
    public void shutdown() {
        super.shutdown();
    }
}