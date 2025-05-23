package net.runelite.client.plugins.microbot.vardorvis;

import net.runelite.api.MenuAction;
import net.runelite.api.Skill;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.widgets.ComponentID;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.herbrun.HerbrunConfig;
import net.runelite.client.plugins.microbot.herbrun.HerbrunPlugin;
import net.runelite.client.plugins.microbot.util.Rs2InventorySetup;
import net.runelite.client.plugins.microbot.util.antiban.Rs2AntibanSettings;
import net.runelite.client.plugins.microbot.util.bank.Rs2Bank;
import net.runelite.client.plugins.microbot.util.camera.Rs2Camera;
import net.runelite.client.plugins.microbot.util.combat.Rs2Combat;
import net.runelite.client.plugins.microbot.util.equipment.Rs2Equipment;
import net.runelite.client.plugins.microbot.util.gameobject.Rs2GameObject;
import net.runelite.client.plugins.microbot.util.grounditem.LootingParameters;
import net.runelite.client.plugins.microbot.util.grounditem.Rs2GroundItem;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.inventory.Rs2ItemModel;
import net.runelite.client.plugins.microbot.util.magic.Rs2Magic;
import net.runelite.client.plugins.microbot.util.menu.NewMenuEntry;
import net.runelite.client.plugins.microbot.util.npc.Rs2Npc;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;
import net.runelite.client.plugins.microbot.util.prayer.Rs2Prayer;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.util.prayer.Rs2PrayerEnum;
import net.runelite.client.plugins.microbot.util.walker.Rs2Walker;
import net.runelite.client.plugins.microbot.util.widget.Rs2Widget;
import net.runelite.client.plugins.microbot.vardorvis.enums.State;
import net.runelite.client.plugins.microbot.vardorvis.enums.StateBank;
import net.runelite.client.plugins.microbot.vardorvis.enums.StatePOH;
import net.runelite.client.plugins.skillcalculator.skills.MagicAction;

import javax.inject.Inject;
import java.awt.*;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class VardorvisScript extends Script {

    public static boolean isProjectileActive = false;

    public static State state = State.UNKNOWN;
    public static StateBank bankState = StateBank.UNKNOWN;
    public static StatePOH POHState = StatePOH.UNKNOWN;
    private final VardorvisPlugin plugin;
    private final VardorvisConfig config;
    public static boolean inFight = false;

    public static boolean inInstance = false;

    public static int maxHealth = 0;
    public static int maxPrayer = 0;
    @Inject
    public VardorvisScript(VardorvisPlugin plugin, VardorvisConfig config) {
        this.plugin = plugin;
        this.config = config;
    }
    public boolean run(VardorvisConfig config) {
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

                if (state == State.AFTER_FIGHT) {
                    Microbot.log("Fight done, cleaning up");
                } else if (regionID == 4405 && !inFight && inInstance) {
                    state = State.IN_BOSS;
                } else if (regionID == 4405 && !inInstance) {
                    Rs2GameObject.interact(49495, "Climb-over");

                    if (Microbot.getClient().isInInstancedRegion()) {
                        inInstance = true;
                    }
                } else if (regionID == 4405) {
                    state = State.FIGHTING;
                } else if (regionID == 4661) {
                    state = State.WALK_TO_BOSS;
                } else if (regionID == 7513) {
                    state = State.POH;
                } else if (regionID == 12598) {
                    state = State.BANK;
                } else {
                    state = State.UNKNOWN;
                }

                switch (state) {
                    case UNKNOWN:
                        //Microbot.log("Current state Unknown D:");

                        break;
                    case POH:
                        doingPOHThings();

                        break;
                    case BANK:
                        doingBankThings();

                        break;
                    case WALK_TO_BOSS:
                        Microbot.log("Current state Walking to boss");

                        walkingToBoss();

                        break;
                    case IN_BOSS:
                        Microbot.log("Current state In boss");

                        Microbot.doInvoke(new NewMenuEntry("Look South", "", 3, MenuAction.CC_OP, 1, 10551327, false), new Rectangle(1, 1));

                        sleepUntil(Rs2Player::isWalking);

                        Rs2Walker.walkFastCanvas(new WorldPoint(1129, 3419, 0));
//                        Rs2Walker.walkFastCanvas(new WorldPoint(1129, 3419, 0));

                        sleepUntil(()-> Objects.equals(Rs2Player.getWorldLocation(), new WorldPoint(1129, 3419, 0)));

                        Rs2Camera.setZoom(550);

                        Rs2Player.drinkCombatPotionAt(Skill.ATTACK);

                        sleepUntil(Rs2Inventory::isEmpty, 2_000);

                        inFight = true;

                        Rs2Prayer.toggleQuickPrayer(true);
                        if(Rs2Inventory.hasItem("Book")) {
//                            Microbot.log("diddy2");
                            Rs2Magic.cast(MagicAction.RESURRECT_GREATER_GHOST);
                        }
                        Rs2Npc.interact(12223, "Attack");

                        sleep(1000);

                        Rs2Walker.walkFastCanvas(new WorldPoint(1129,3423,0));

                        state = State.FIGHTING;

                        break;
                    case FIGHTING:

                        if (!Rs2Prayer.isQuickPrayerEnabled()) {

                            Microbot.log("Toggling Quick Prayer ON");
                            Rs2Prayer.toggleQuickPrayer(true);

                        }
//                        Microbot.log(""+Rs2Magic.isThrallActive());
                        if(!Rs2Magic.isThrallActive()&&Rs2Inventory.hasItem("Book")) {
//                            Microbot.log("diddy2");
                            Rs2Magic.cast(MagicAction.RESURRECT_GREATER_GHOST);
                        }
                        if(Rs2Combat.getSpecEnergy() < 500&&!Rs2Equipment.isWearing(29796) )
                        {
                            Rs2Inventory.wear(29796);
                        }
//                        else if (!Rs2Prayer.isPrayerActive(Rs2PrayerEnum.PROTECT_MELEE) && !isProjectileActive) {
//
//                            Microbot.log("Toggling Protect Melee ON");
//                            Rs2Prayer.toggle(Rs2PrayerEnum.PROTECT_MELEE);
//                        }

                        break;
                    case AFTER_FIGHT:
//                        shutdown();
                        
                        Microbot.log("Attempting to pick up items");
                        Rs2Prayer.toggleQuickPrayer(false);
                        LootingParameters itemLootParams = new LootingParameters(
                                10,
                                1,
                                1,
                                0,
                                false,
                                true,
                                ""
                        );
                        if (Rs2GroundItem.lootItemsBasedOnNames(itemLootParams)) {
//                            if Rs2Inventory.hasItemAmount("Shark",5);
                            Microbot.log("Picking up items");
                            sleep(5_000);

                            Microbot.log("Slept for 10 seconds now leaving");

                            Rs2Inventory.interact("Teleport to house", "break");

                            state = State.POH;

                            sleep(4000);

                            Microbot.log("Turning state to POH");

                            state = State.POH;
                        }

                        break;
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
                    Rs2Inventory.wear("Noxious");
                    Rs2GameObject.interact(29241, "Drink");
                    sleep(1800);
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
        var inventorySetup = new Rs2InventorySetup(config.inventorySetup(), mainScheduledFuture);
        if (!inventorySetup.doesInventoryMatch() || !inventorySetup.doesEquipmentMatch()) {
            Rs2Walker.walkTo(Rs2Bank.getNearestBank().getWorldPoint(), 20);
            if (!inventorySetup.loadEquipment() || !inventorySetup.loadInventory()) {
                Microbot.log("Failed to load inventory setup");
                Microbot.stopPlugin(plugin);
                return;
            }
            Rs2Bank.closeBank();
        }
//        if (!Rs2Inventory.isFull() && isBankOpen || !Rs2Inventory.hasItemAmount("Shark", 10) && isBankOpen) {
//            bankState = StateBank.GET_ITEMS;
//        } else if (Rs2Inventory.isFull() && !Objects.equals(Rs2Inventory.getItemInSlot(27).name, "Teleport to house") && Rs2Inventory.hasItemAmount("Shark", 10)) {
//            bankState = StateBank.MOVE_ITEMS;
//        } else if (!isBankOpen && !Rs2Inventory.isFull() || !isBankOpen && !Rs2Inventory.hasItemAmount("Shark", 10)) {
//            bankState = StateBank.OPEN_BANK;
//        }
//        else {
            bankState = StateBank.TELEPORT_TO_STRANGLEWOOD;
//        }

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

                Rs2Bank.withdrawX("Cooked Karambwan", 10);
                Rs2Bank.withdrawX("Shark", 10);

                Rs2Bank.withdrawItem("Ring of shadows");

                Rs2Bank.withdrawItem("Teleport to house");
                Rs2Bank.withdrawItem("Book of the dead");

                Rs2Bank.withdrawItem("Rune pouch");

                Rs2Bank.closeBank();

                break;
            case MOVE_ITEMS:
                Microbot.log("Current state BANK MOVE_ITEMS");

                if (Rs2Bank.isOpen()) {
                    Rs2Bank.closeBank();
                }

//                Rs2ItemModel ring = Rs2Inventory.get("Ring of shadows");
                Rs2ItemModel bankTab = Rs2Inventory.get("Teleport to house");

//                Rs2Inventory.moveItemToSlot(ring, 26);

                sleepUntil(() -> Objects.equals(Rs2Inventory.getItemInSlot(26).name, "Ring of shadows"));

                Rs2Inventory.moveItemToSlot(bankTab, 27);

                sleepUntil(() -> Objects.equals(Rs2Inventory.getItemInSlot(27).name, "Teleport to house"));

                break;
            case TELEPORT_TO_STRANGLEWOOD:
                Microbot.log("Current state BANK teleport to Stranglewood");

                if (Rs2Bank.isOpen()) {
                    Rs2Bank.closeBank();
                }

//                Rs2Inventory.interact("Ring of shadows", "Teleport");
//
//                sleepUntil(() -> Rs2Widget.hasWidget("The Stranglewood"));
//                Rs2Widget.clickWidget("The Stranglewood");
                Rs2ItemModel ring = Rs2Inventory.get("Ring of Shadows");
                Microbot.doInvoke(new NewMenuEntry("The Stranglewood", ring.getSlot(), ComponentID.INVENTORY_CONTAINER, 57, 327683, ring.id, ""), (Rs2Inventory.itemBounds(ring) == null) ? new Rectangle(1, 1) : Rs2Inventory.itemBounds(ring));
                Rs2Player.waitForAnimation();
                break;
        }
    }

    public void walkingToBoss() {
        Rs2Camera.setZoom(200);

//        Rs2GameObject.interact(48745, "Enter");
//
//        Microbot.log("Waiting until 1146");
//        sleepUntil(() -> Objects.equals(Rs2Player.getWorldLocation(), new WorldPoint(1146, 3433, 0)), 25_000);

        Microbot.log("Walking to boss");
        Rs2Walker.walkTo(new WorldPoint(1117, 3429, 0));

//        Microbot.log("Waiting until 1117");
//        sleepUntil(() -> Rs2Player.distanceTo(new WorldPoint(1117, 3429, 0)) <= 1, 12_000);

        Microbot.log("Climbing over");
        Rs2GameObject.interact(49495, "Climb-over");

        Microbot.log("waiting after climb");
        Rs2Player.waitForAnimation();

        Microbot.log("After waiting for climb | in instance = " + inInstance + " Region = " + Rs2Player.getWorldLocation().getRegionID());
    }

    @Override
    public void shutdown() {
        super.shutdown();
        Rs2Prayer.disableAllPrayers();
    }
}
