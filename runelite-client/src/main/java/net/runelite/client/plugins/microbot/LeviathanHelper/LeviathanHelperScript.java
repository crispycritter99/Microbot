package net.runelite.client.plugins.microbot.LeviathanHelper;

import net.runelite.api.Skill;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.plugins.microbot.LeviathanHelper.enums.State;
import net.runelite.client.plugins.microbot.LeviathanHelper.enums.StateBank;
import net.runelite.client.plugins.microbot.LeviathanHelper.enums.StatePOH;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.util.bank.Rs2Bank;
import net.runelite.client.plugins.microbot.util.camera.Rs2Camera;
import net.runelite.client.plugins.microbot.util.gameobject.Rs2GameObject;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.inventory.Rs2ItemModel;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;
import net.runelite.client.plugins.microbot.util.prayer.Rs2Prayer;
import net.runelite.client.plugins.microbot.util.walker.Rs2Walker;
import net.runelite.client.plugins.microbot.util.widget.Rs2Widget;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class LeviathanHelperScript extends Script {

    public static boolean isProjectileActive = false;

    public static State state = State.UNKNOWN;
    public static StateBank bankState = StateBank.UNKNOWN;
    public static StatePOH POHState = StatePOH.UNKNOWN;

    public static boolean inFight = false;

    public static boolean inInstance = false;

    public static int maxHealth = 0;
    public static int maxPrayer = 0;

    public boolean run(LeviathanHelperConfig config) {
        Microbot.enableAutoRunOn = true;
        mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> {
            try {
                if (!Microbot.isLoggedIn()) return;
                //if (!super.run()) return;

                if (maxHealth == 0) {
                    maxHealth = Microbot.getClient().getRealSkillLevel(Skill.HITPOINTS);
                }
                if (maxPrayer == 0) {
                    maxPrayer = Microbot.getClient().getRealSkillLevel(Skill.PRAYER);
                }

                int regionID = Rs2Player.getWorldLocation().getRegionID();

                if (Microbot.getClient().getTopLevelWorldView().getScene().isInstance()) {
                    inInstance = true;
                } else {
                    inInstance = false;
                }

            } catch (Exception e) {
                Microbot.log("Vardorvis Error: " + e);
            }
        }, 0, 200, TimeUnit.MILLISECONDS);
        return true;
    }

    public void doingPOHThings() {
        int maxPrayer = Microbot.getClient().getBoostedSkillLevel(Skill.PRAYER);
        int maxHealth = Microbot.getClient().getBoostedSkillLevel(Skill.HITPOINTS);

        int prayer = Microbot.getClient().getRealSkillLevel(Skill.PRAYER);
        int health = Microbot.getClient().getRealSkillLevel(Skill.HITPOINTS);

        Microbot.log("Health = " + health + " Max health = " + maxHealth);
        Microbot.log("Prayer = " + prayer + " Max prayer = " + maxPrayer);

        if (health != maxHealth || prayer != maxPrayer) {
            POHState = StatePOH.REJUVENATION;
        } else {
            POHState = StatePOH.GRAND_EXCHANGE;
        }

        Rs2Inventory.count();

        switch (POHState) {
            case REJUVENATION:

                if (!Rs2Player.isWalking()) {
                    Microbot.log("Current state POH Rejuvenation");

                    Rs2GameObject.interact(29241, "Drink");
                }

                break;
            case GRAND_EXCHANGE:

                if (!Rs2Player.isWalking()) {
                    Microbot.log("Current state POH Grand Exchange");

                    Rs2GameObject.interact(29156, "Grand Exchange");
                }

                break;
        }
    }

    public void doingBankThings() {
        boolean isBankOpen = Rs2Bank.isOpen();

        if (!Rs2Inventory.isFull() && isBankOpen || !Rs2Inventory.hasItemAmount("Shark", 11) && isBankOpen) {
            bankState = StateBank.GET_ITEMS;
        } else if (Rs2Inventory.isFull() && !Objects.equals(Rs2Inventory.getItemInSlot(27).name, "Teleport to house") && Rs2Inventory.hasItemAmount("Shark", 11)) {
            bankState = StateBank.MOVE_ITEMS;
        } else if (!isBankOpen && !Rs2Inventory.isFull() || !isBankOpen && !Rs2Inventory.hasItemAmount("Shark", 11)) {
            bankState = StateBank.OPEN_BANK;
        }
        else {
            bankState = StateBank.TELEPORT_TO_STRANGLEWOOD;
        }

        switch (bankState) {
            case OPEN_BANK:
                Microbot.log("Current state BANK OPEN_BANK");
                Rs2Bank.openBank();
                break;
            case GET_ITEMS:
                Microbot.log("Current state BANK GET_ITEMS");

                Rs2Bank.depositAll();

                Rs2Bank.withdrawItem("Super combat potion(4)");

                Rs2Bank.withdrawItem("Prayer potion(4)");
                Rs2Bank.withdrawItem("Prayer potion(4)");

                Rs2Bank.withdrawItem("dragon claws");

                Rs2Bank.withdrawX("Cooked Karambwan", 11);
                Rs2Bank.withdrawX("Shark", 11);

                Rs2Bank.withdrawItem("Ring of shadows");

                Rs2Bank.withdrawItem("Teleport to house");

                Rs2Bank.closeBank();

                break;
            case MOVE_ITEMS:
                Microbot.log("Current state BANK MOVE_ITEMS");

                if (Rs2Bank.isOpen()) {
                    Rs2Bank.closeBank();
                }

                Rs2ItemModel ring = Rs2Inventory.get("Ring of shadows");
                Rs2ItemModel bankTab = Rs2Inventory.get("Teleport to house");

                Rs2Inventory.moveItemToSlot(ring, 26);

                sleepUntil(() -> Objects.equals(Rs2Inventory.getItemInSlot(26).name, "Ring of shadows"));

                Rs2Inventory.moveItemToSlot(bankTab, 27);

                sleepUntil(() -> Objects.equals(Rs2Inventory.getItemInSlot(27).name, "Teleport to house"));

                break;
            case TELEPORT_TO_STRANGLEWOOD:
                Microbot.log("Current state BANK teleport to Stranglewood");

                if (Rs2Bank.isOpen()) {
                    Rs2Bank.closeBank();
                }

                Rs2Inventory.interact("Ring of shadows", "Teleport");

                sleepUntil(() -> Rs2Widget.hasWidget("The Stranglewood"));
                Rs2Widget.clickWidget("The Stranglewood");

                break;
        }
    }

    public void walkingToBoss() {
        Rs2Camera.setZoom(200);

        Rs2GameObject.interact(48745, "Enter");

        Microbot.log("Waiting until 1146");
        sleepUntil(() -> Objects.equals(Rs2Player.getWorldLocation(), new WorldPoint(1146, 3433, 0)), 25_000);

        Microbot.log("Walking to boss");
        Rs2Walker.walkTo(new WorldPoint(1117, 3429, 0));

        Microbot.log("Waiting until 1117");
        sleepUntil(() -> Rs2Player.distanceTo(new WorldPoint(1117, 3429, 0)) <= 1, 12_000);

        Microbot.log("Climbing over");
        Rs2GameObject.interact(49495, "Climb-over");

        Microbot.log("waiting after climb");
        sleepUntil(Rs2Inventory::isEmpty, 7_000);

        Microbot.log("After waiting for climb | in instance = " + inInstance + " Region = " + Rs2Player.getWorldLocation().getRegionID());
    }

    @Override
    public void shutdown() {
        super.shutdown();
        Rs2Prayer.disableAllPrayers();
    }
}
