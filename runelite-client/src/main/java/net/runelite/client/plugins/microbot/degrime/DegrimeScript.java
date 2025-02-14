package net.runelite.client.plugins.microbot.degrime;

import net.runelite.api.Skill;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.util.bank.Rs2Bank;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.inventory.Rs2ItemModel;
import net.runelite.client.plugins.microbot.util.magic.Rs2Magic;
import net.runelite.client.plugins.microbot.util.math.Rs2Random;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;
import net.runelite.client.plugins.skillcalculator.skills.MagicAction;
import net.runelite.http.api.item.ItemPrice;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import static net.runelite.client.plugins.microbot.util.Global.sleepUntilTrue;
import static net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory.calculateInteractOrder;

// heaps of new features added by Storm
public class DegrimeScript extends Script {
    @Inject
    private DegrimeConfig config;
    public static double version = 2.1;

    public static long previousItemChange;

    public static DegrimeCurrentStatus degrimeCurrentStatus = DegrimeCurrentStatus.FETCH_SUPPLIES;

    public static int itemsProcessed;

    static Integer thirdItemId;
    static Integer fourthItemId;


    static Integer firstItemId;
    public static Integer secondItemId;
    private int sleepMin;
    private int sleepMax;
    private int sleepTarget;

    public static boolean isWaitingForPrompt = false;
    private long timeValue;
    private int randomNum;
    List<Rs2ItemModel> inventorySlots;

    public boolean run(DegrimeConfig config) {
        this.config = config; // Initialize the config object before accessing its parameters
        itemsProcessed = 0;
        inventorySlots = null;

        mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> {
            if (!Microbot.isLoggedIn()) return;
            if (!super.run()) return;
            try {
                //start
                degrimeHerbs();

            } catch (Exception ex) {
                ex.printStackTrace();
                Microbot.log(ex.getMessage());
            }
        }, 0, 100, TimeUnit.MILLISECONDS);
        return true;
    }


    private Boolean fetchItems() {

        if (degrimeCurrentStatus != DegrimeCurrentStatus.FETCH_SUPPLIES) {
            degrimeCurrentStatus = DegrimeCurrentStatus.FETCH_SUPPLIES;
        }
        if (!Rs2Inventory.hasItem(config.Herb().getGrimyItemID())) {
            if (!Rs2Bank.isOpen()) {
                Rs2Bank.openBank();
                sleepUntil(Rs2Bank::isOpen);
            }
            Rs2Bank.depositAll(config.Herb().getCleanItemID());
            // Checking that we have enough items in the bank
//            Rs2Inventory.waitForInventoryChanges(1800);
            Rs2Inventory.waitForInventoryChanges(1800);
            Rs2Bank.withdrawAll(config.Herb().getGrimyItemID());
            Rs2Inventory.waitForInventoryChanges(1800);

            // Checking that we have our items, and tallying a summary for the overlay.
            if (Rs2Inventory.hasItem(config.Herb().getGrimyItemID())) {
                Rs2Bank.closeBank();
                sleepUntil(() -> !Rs2Bank.isOpen());
                degrimeCurrentStatus = DegrimeCurrentStatus.DEGRIME_HERBS;
                return true;
            }
        }
        return false;
    }

    private boolean degrimeHerbs() {
        if (!Rs2Inventory.hasItem(config.Herb().getGrimyItemID())) {
            if (!fetchItems()) {
                Microbot.log("Insufficient " + config.Herb().getGrimyItemName());
                sleep(2500, 5000);
                shutdown();
                return false;
            }
            }
            // this is to prevent unintended behaviour when the script is started with the bank open.
            if (Rs2Bank.isOpen()) {
                Rs2Bank.closeBank();
                sleepUntil(() -> !Rs2Bank.isOpen());

                return false;
            }
            // We loop through executing this method "combineItems()", so we want to force return to do nothing while we wait for processing.


            if (degrimeCurrentStatus != DegrimeCurrentStatus.DEGRIME_HERBS) {
                degrimeCurrentStatus = DegrimeCurrentStatus.DEGRIME_HERBS;
            }

            // This just allows us to pause the script so that we don't lose our overlay.

            Rs2Magic.cast(MagicAction.DEGRIME);
            Rs2Player.waitForXpDrop(Skill.MAGIC, 50000);

            return true;
        }


    }
