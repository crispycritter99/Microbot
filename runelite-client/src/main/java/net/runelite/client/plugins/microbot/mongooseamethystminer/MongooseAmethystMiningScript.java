package net.runelite.client.plugins.microbot.mongooseamethystminer;

import net.runelite.api.Skill;
import net.runelite.api.WallObject;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.gameval.ItemID;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.mongooseamethystminer.enums.AmethystCraftingOption;
import net.runelite.client.plugins.microbot.mongooseamethystminer.enums.MiningSpot;
import net.runelite.client.plugins.microbot.mongooseamethystminer.enums.Status;
import net.runelite.client.plugins.microbot.util.antiban.AntibanPlugin;
import net.runelite.client.plugins.microbot.util.antiban.Rs2Antiban;
import net.runelite.client.plugins.microbot.util.antiban.Rs2AntibanSettings;
import net.runelite.client.plugins.microbot.util.bank.Rs2Bank;
import net.runelite.client.plugins.microbot.util.bank.enums.BankLocation;
import net.runelite.client.plugins.microbot.util.combat.Rs2Combat;
import net.runelite.client.plugins.microbot.util.coords.Rs2WorldPoint;
import net.runelite.client.plugins.microbot.util.equipment.Rs2Equipment;
import net.runelite.client.plugins.microbot.util.gameobject.Rs2GameObject;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.inventory.Rs2ItemModel;
import net.runelite.client.plugins.microbot.util.keyboard.Rs2Keyboard;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;
import net.runelite.client.plugins.microbot.util.tile.Rs2Tile;
import net.runelite.client.plugins.microbot.util.walker.Rs2Walker;
import net.runelite.client.plugins.microbot.util.widget.Rs2Widget;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.concurrent.TimeUnit;

public class MongooseAmethystMiningScript extends Script {
    private static final String DEBUG_PREFIX = "[MongooseAmethystMining]";
    public static Status status = Status.IDLE;
    public static boolean lockedStatus = false;
    public static WallObject oreVein;
    private static MongooseAmethystMiningConfig config;
    private static MiningSpot miningSpot = MiningSpot.NULL;
    private String pickAxeInInventory = "";
    public static final String gemBag = "Gem bag";
    public static final String openGemBag = "Open gem bag";
    public static final String chisel = "Chisel";
    public static ArrayList<String> itemsToKeep = new ArrayList<>();
    public static int inventoryCountSinceLastGemBagCheck = 0;

    public boolean run(MongooseAmethystMiningConfig config) {
        debug("run() starting");
        MongooseAmethystMiningScript.config = config;
        initialize();

        mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(this::executeTask, 0, 600, TimeUnit.MILLISECONDS);
        return true;
    }



    private void executeTask() {
        long taskStartedAt = System.currentTimeMillis();
        debug("executeTask() start; status=" + status + ", miningSpot=" + miningSpot
                + ", oreVein=" + (oreVein == null ? "null" : oreVein.getWorldLocation()));
        try {
            if (!super.run() || !Microbot.isLoggedIn()) {
                miningSpot = MiningSpot.NULL;
                oreVein = null;
                return;
            }
            if (config.pickAxeInInventory() && pickAxeInInventory.isEmpty()) {
                pickAxeInInventory = Rs2Inventory.get("pickaxe").getName();
                if (!pickAxeInInventory.isEmpty()) {
                    itemsToKeep.add(pickAxeInInventory);
                }
            }
            if (pickAxeInInventory.isEmpty() && config.pickAxeInInventory()) {
                Microbot.showMessage("Pickaxe was not found in your inventory");
                sleep(5000);
                return;
            }


            if (Rs2AntibanSettings.actionCooldownActive) {
                debug("executeTask() returning: action cooldown active after "
                        + elapsed(taskStartedAt) + " ms");
                return;
            }
            debug("action cooldown expired; continuing after " + elapsed(taskStartedAt) + " ms");

            if (Rs2Player.isAnimating() || Microbot.getClient().getLocalPlayer().isInteracting()) {
                debug("executeTask() returning: player still animating/interacting after "
                        + elapsed(taskStartedAt) + " ms");
                return;
            }

            handleDragonPickaxeSpec();
            handleInventory();

            switch (status) {
                case IDLE:
                    return;
                case MINING:
                    debug("dispatching to handleMining()");
                    handleMining();
                    break;
                case BANKING:
                    bankItems();
                    break;
                case CHISELING:
                    chiselAmethysts();
                    break;
            }
        } catch (Exception e) {
            debug("executeTask() exception after " + elapsed(taskStartedAt) + " ms: " + e.getMessage());
            Microbot.log("Error in MongooseAmethystMiningScript: " + e.getMessage());
        }
    }

    private void handleDragonPickaxeSpec() {
        if (Rs2Equipment.isWearing("dragon pickaxe")&& ((Rs2Player.getBoostedSkillLevel(Skill.MINING))<92)) {
            Rs2Combat.setSpecState(true, 1000);
        }
    }

    private void handleInventory() {
        if(lockedStatus){
            oreVein = null;
            miningSpot = MiningSpot.NULL;
            return;
        }
        if (!Rs2Inventory.isFull()) {
            status = Status.MINING;
        } else {
            oreVein = null;
            miningSpot = MiningSpot.NULL;
            if (config.chiselAmethysts())
                status = Status.CHISELING;
            else
                status = Status.BANKING;
        }
    }

    private void bankItems() {
        if (Rs2Walker.walkTo(BankLocation.MINING_GUILD.getWorldPoint()))
            bank();
    }

    private void bank() {
        if (Rs2Bank.openBank()) {
            sleepUntil(Rs2Bank::isOpen);

            Rs2Bank.depositAllExcept(itemsToKeep);
            if (config.gemBag() && inventoryCountSinceLastGemBagCheck >= 5) {
                Rs2Bank.emptyGemBag();
                inventoryCountSinceLastGemBagCheck = 0;
            }

            sleep(100, 300);

            if (config.pickAxeInInventory() && !Rs2Inventory.hasItem(pickAxeInInventory)) {
                Rs2Bank.withdrawOne(pickAxeInInventory);
            }
            if (config.gemBag() && !(Rs2Inventory.hasItem(gemBag) || Rs2Inventory.hasItem(openGemBag))) {
                Rs2Bank.withdrawOne(gemBag);
                Rs2Bank.withdrawOne(openGemBag);
            }
            if (config.chiselAmethysts() && !Rs2Inventory.hasItem(chisel)) {
                Rs2Bank.withdrawOne(chisel);
            }
            sleep(600);
            lockedStatus = false;
        }
    }


    private void chiselAmethysts() {
        AmethystCraftingOption craftingOption = config.amethystCraftingOption();
        int requiredLevel = craftingOption.getRequiredLevel();
        Rs2ItemModel chisel = Rs2Inventory.get("chisel");
        Rs2Inventory.moveItemToSlot(chisel, 27);
        sleepUntil(() -> Rs2Inventory.slotContains(27, "chisel"), 5000);
        if (Microbot.getClient().getRealSkillLevel(Skill.CRAFTING) >= requiredLevel ) {
            Rs2Inventory.combineClosest(ItemID.CHISEL, ItemID.AMETHYST);
            sleepUntil(() -> Rs2Widget.getWidget(17694733) != null);
            Rs2Keyboard.keyPress(craftingOption.getDialogOption());
            sleepUntil(() -> !Rs2Inventory.hasItem(ItemID.AMETHYST), 40000);
            Rs2Antiban.actionCooldown();
            Rs2Antiban.takeMicroBreakByChance();
            inventoryCountSinceLastGemBagCheck++;
            if(inventoryCountSinceLastGemBagCheck >= 5) {
                status = Status.BANKING;
                lockedStatus = true;
            }


        } else {
            Microbot.showMessage("You do not have the required crafting level to make " + craftingOption.getDisplayName());
            status = Status.BANKING;
        }
    }

    private void handleMining() {

        debug("handleMining() entered; oreVein=" + (oreVein == null ? "null" : oreVein.getWorldLocation())
                + ", isMining=" + AntibanPlugin.isMining());
        if (oreVein != null && AntibanPlugin.isMining()) {
            debug("handleMining() returning: current vein still mining");
            return;
        }
        if ((Rs2Player.getBoostedSkillLevel(Skill.MINING))<92) {
            debug("handleMining() returning: boosted mining level below 92");
            return;
        }
        if (miningSpot == MiningSpot.NULL) {
            miningSpot = MiningSpot.getRandomMiningSpot();
            debug("handleMining() selected mining spot " + miningSpot);
        } else {
//            if (walkToMiningSpot()) {
                if (Rs2Player.isMoving()) {
                    debug("handleMining() returning: player is moving");
                    return;
                }
                debug("handleMining() calling mineVein()");
                mineVein();
                debug("handleMining() applying action cooldown");
                Rs2Antiban.actionCooldown();
                Rs2Antiban.takeMicroBreakByChance();
//            }
        }

    }

    private boolean mineVein() {
        long startedAt = System.currentTimeMillis();
        debug("mineVein() start");
        if (Rs2Player.isMoving()) {
            debug("mineVein() returning: player started moving");
            return false;
        }

        WallObject closestVein = findClosestVein();
        if (closestVein == null) {
            debug("mineVein() found no vein after " + elapsed(startedAt) + " ms; moving to spot");
            moveToMiningSpot();
            return false;
        }

        debug("mineVein() found vein id=" + closestVein.getId() + " at "
                + closestVein.getWorldLocation() + " after " + elapsed(startedAt) + " ms");
        interactWithVein(closestVein);
        return true;
    }

    private WallObject findClosestVein() {
        long startedAt = System.currentTimeMillis();
        var wallObjects = Rs2GameObject.getWallObjects(this::isVein);
        Rs2WorldPoint playerLocation = Rs2Player.getRs2WorldPoint();
        WallObject result = wallObjects.stream()
                .min(Comparator.comparingDouble(vein -> distanceToPlayer(playerLocation, vein))).orElse(null);
        debug("findClosestVein() evaluated " + wallObjects.size() + " veins; result="
                + (result == null ? "null" : result.getWorldLocation()) + ", elapsed=" + elapsed(startedAt) + " ms");
        return result;
    }

    private boolean isVein(WallObject wallObject) {
        int id = wallObject.getId();
        return id == 11388 || id == 11389;
    }

    private double distanceToPlayer(Rs2WorldPoint playerLocation, WallObject wallObject) {
        WorldPoint veinLocation = wallObject.getWorldLocation();
        WorldPoint player = playerLocation.getWorldPoint();
        int dx = Math.abs(player.getX() - veinLocation.getX());
        int dy = Math.abs(player.getY() - veinLocation.getY());

        int diagonalSteps = Math.min(dx, dy);
        int straightSteps = Math.max(dx, dy) - diagonalSteps;

        return straightSteps + diagonalSteps * Math.sqrt(2);
    }

    private void interactWithVein(WallObject vein) {
        long startedAt = System.currentTimeMillis();
        debug("interactWithVein() attempting id=" + vein.getId() + " at " + vein.getWorldLocation());
        if (Rs2GameObject.interact(vein))
            oreVein = vein;
        debug("interactWithVein() interaction returned; oreVein="
                + (oreVein == null ? "null" : oreVein.getWorldLocation())
                + ", elapsed=" + elapsed(startedAt) + " ms");
        sleepUntil(AntibanPlugin::isMining, 5000);
        debug("interactWithVein() mining wait finished; isMining=" + AntibanPlugin.isMining());
        if (!AntibanPlugin.isMining()) {
            oreVein = null;
        }
    }

    private boolean walkToMiningSpot() {
        WorldPoint miningWorldPoint = miningSpot.getWorldPoint();
//        if (miningWorldPoint.distanceTo(Rs2Player.getWorldLocation())<9) return true;
        return Rs2Walker.walkTo(miningWorldPoint, 8);
    }

    private void moveToMiningSpot() {
        debug("moveToMiningSpot() walking to " + miningSpot.getWorldPoint());
        Rs2Walker.walkFastCanvas(miningSpot.getWorldPoint());
    }

    private static void debug(String message) {
        System.out.println(DEBUG_PREFIX + " [" + System.currentTimeMillis() + "] " + message);
    }

    private static long elapsed(long startedAt) {
        return System.currentTimeMillis() - startedAt;
    }

    private void initialize() {
        Rs2Antiban.antibanSetupTemplates.applyMiningSetup();
        itemsToKeep.clear();
        pickAxeInInventory = "";
        inventoryCountSinceLastGemBagCheck = 0;
        status = Status.IDLE;
        miningSpot = MiningSpot.NULL;
        oreVein = null;
        if (config.gemBag()) {
            itemsToKeep.add(gemBag);
            itemsToKeep.add(openGemBag);
        }
        if (config.chiselAmethysts()) {
            itemsToKeep.add(chisel);
        }
    }

    @Override
    public void shutdown() {
        Rs2Antiban.resetAntibanSettings();
        super.shutdown();
        status = Status.IDLE;
        miningSpot = MiningSpot.NULL;
        oreVein = null;
    }
}
