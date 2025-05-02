package net.runelite.client.plugins.microbot.runecrafting.mudrune;

import net.runelite.api.ItemID;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.util.bank.Rs2Bank;
import net.runelite.client.plugins.microbot.util.bank.enums.BankLocation;
import net.runelite.client.plugins.microbot.util.equipment.Rs2Equipment;
import net.runelite.client.plugins.microbot.util.gameobject.Rs2GameObject;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.magic.Rs2Magic;
import net.runelite.client.plugins.microbot.util.magic.Rs2Spells;
import net.runelite.client.plugins.microbot.util.math.Rs2Random;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;
import net.runelite.client.plugins.microbot.util.walker.Rs2Walker;
import net.runelite.client.plugins.skillcalculator.skills.MagicAction;

import java.util.concurrent.TimeUnit;
import static net.runelite.api.Varbits.*;
//import static net.runelite.api.Varbits.RESURRECT_THRALL;
import static net.runelite.client.plugins.microbot.Microbot.log;


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
                doBanking();
                goToAltar();
                makeRunes();
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
    private void doBanking() {
        if ((Rs2Inventory.hasItem("pure essence")||!Rs2Inventory.allPouchesEmpty())&&Rs2GameObject.exists(34763))
            return;
        Rs2Bank.walkToBankAndUseBank(BankLocation.CASTLE_WARS);
        if (!Rs2Bank.isOpen())
            return;
        if (!Rs2Equipment.isWearing(ItemID.BINDING_NECKLACE))
            Rs2Bank.withdrawAndEquip(ItemID.BINDING_NECKLACE);
        if (!Rs2Inventory.hasItem("Ring of dueling"))
            Rs2Bank.withdrawOne("Ring of dueling");
        Rs2Bank.depositAll("Mud rune");
//        Rs2Inventory.waitForInventoryChanges(1800);
        Rs2Bank.withdrawAll("Pure essence");
        Rs2Inventory.waitForInventoryChanges(1800);
        if (!Rs2Inventory.allPouchesFull()) {
            Rs2Inventory.interact("colossal", "Fill");
            sleep(300,500);
            Rs2Bank.withdrawAll("Pure essence");
            Rs2Inventory.waitForInventoryChanges(1800);
        }
        if (Rs2Inventory.hasDegradedPouch() && Rs2Magic.hasRequiredRunes(Rs2Spells.NPC_CONTACT)) {
            Rs2Bank.closeBank();
            Rs2Magic.repairPouchesWithLunar();
            Microbot.log("pouch repaired");
            return;
        }
        if (!Rs2Inventory.hasItem("Pure essence")) {
            Microbot.log("no essence");
//            Rs2Inventory.waitForInventoryChanges(1800);
            return;
        }
        Rs2Bank.closeBank();

    }
    private void goToAltar() {
        if (!Rs2Inventory.hasItem("pure essence")||Rs2Inventory.allPouchesEmpty())
            return;
        if (Rs2GameObject.exists(34763))
            return;
        if (!Rs2GameObject.exists(1282)) {
            Rs2Equipment.interact(26818, "Earth Altar");
            sleepUntil(() -> Rs2GameObject.exists(1282));
        }

        Rs2GameObject.interact(34816, "Enter");

        Rs2Player.waitForWalking(10000);

    }
    private void makeRunes() {
        if (!Rs2GameObject.exists(34763))
            return;
        if (!Rs2Inventory.hasItem("pure essence")&&Rs2Inventory.allPouchesEmpty())
            return;
//        if Rs2Inventory.ch
        Rs2Walker.walkTo(2658,4839,0,2);
        if (Microbot.getVarbitValue(MAGIC_IMBUE) == 0) {
            Rs2Magic.cast(MagicAction.MAGIC_IMBUE);
        }
//                while (Rs2Inventory.getRemainingCapacityInPouches() > 0){
        if (!Rs2Inventory.hasItem("pure essence")) {
            Rs2Inventory.emptyPouches();
            Rs2Random.wait(100, 300);
        }
        Rs2Inventory.useItemOnObject(555, 34763);
        sleepUntil(() -> !Rs2Inventory.contains(7936));

//        if (!Rs2Inventory.hasItem("pure essence")) {
//            Rs2Inventory.emptyPouches();
//            Rs2Random.wait(300, 600);
//        }
//        sleepUntil(() -> Rs2Inventory.contains(7936));
//        Rs2Inventory.useItemOnObject(555, 34763);
//        sleepUntil(() -> !Rs2Inventory.contains(7936));
    }
    @Override
    public void shutdown() {
        super.shutdown();
    }
}