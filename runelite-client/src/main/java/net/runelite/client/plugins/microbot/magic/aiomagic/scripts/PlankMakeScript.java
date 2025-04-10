package net.runelite.client.plugins.microbot.magic.aiomagic.scripts;

import net.runelite.api.ItemID;
import net.runelite.api.Skill;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.magic.aiomagic.AIOMagicPlugin;
import net.runelite.client.plugins.microbot.magic.aiomagic.enums.MagicState;
import net.runelite.client.plugins.microbot.util.antiban.Rs2Antiban;
import net.runelite.client.plugins.microbot.util.antiban.Rs2AntibanSettings;
import net.runelite.client.plugins.microbot.util.antiban.enums.Activity;
import net.runelite.client.plugins.microbot.util.bank.Rs2Bank;
import net.runelite.client.plugins.microbot.util.equipment.Rs2Equipment;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.inventory.Rs2ItemModel;
import net.runelite.client.plugins.microbot.util.keyboard.Rs2Keyboard;
import net.runelite.client.plugins.microbot.util.magic.Rs2Magic;
import net.runelite.client.plugins.microbot.util.magic.Rs2Staff;
import net.runelite.client.plugins.microbot.util.magic.Runes;
import net.runelite.client.plugins.microbot.util.math.Rs2Random;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;
import net.runelite.client.plugins.skillcalculator.skills.MagicAction;

import javax.inject.Inject;
import java.awt.event.KeyEvent;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class PlankMakeScript extends Script {

    private MagicState state;
    private final AIOMagicPlugin plugin;

    @Inject
    public PlankMakeScript(AIOMagicPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean run() {
        Microbot.enableAutoRunOn = false;
        Rs2Antiban.resetAntibanSettings();
        Rs2Antiban.antibanSetupTemplates.applyGeneralBasicSetup();
        Rs2AntibanSettings.simulateAttentionSpan = true;
        Rs2AntibanSettings.nonLinearIntervals = true;
        Rs2AntibanSettings.contextualVariability = true;
        Rs2AntibanSettings.usePlayStyle = true;
        Rs2Antiban.setActivity(Activity.SUPERHEATING);
        mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> {
            try {
                if (!Microbot.isLoggedIn()) return;
                if (!super.run()) return;
                long startTime = System.currentTimeMillis();

                if (hasStateChanged()) {
                    state = updateState();
                }

                if (state == null) {
                    Microbot.showMessage("Unable to evaluate state");
                    shutdown();
                    return;
                }

                switch (state) {
                    case BANKING:
                        Rs2Bank.openBank();

                        Rs2Bank.depositAllExcept(ItemID.RUNE_POUCH,ItemID.NATURE_RUNE,ItemID.ASTRAL_RUNE,ItemID.COINS,ItemID.COINS_995,ItemID.COINS_6964,ItemID.COINS_8890);
                        Rs2Inventory.waitForInventoryChanges(600);

                        List<Rs2Staff> staffList = Rs2Magic.findStavesByRunes(List.of(Runes.EARTH));

                        boolean hasFireStaffEquipped = staffList.stream()
                                .map(Rs2Staff::getItemID)
                                .anyMatch(Rs2Equipment::hasEquipped);

                        if (!hasFireStaffEquipped) {
                            Rs2ItemModel staffItem = Rs2Bank.bankItems().stream()
                                    .filter(rs2Item -> staffList.stream()
                                            .map(Rs2Staff::getItemID)
                                            .anyMatch(id -> id == rs2Item.getId()))
                                    .findFirst()
                                    .orElse(null);

                            if (staffItem == null) {
                                Microbot.showMessage("Unable to find staff");
                                shutdown();
                                return;
                            }

                            Rs2Bank.withdrawAndEquip(staffItem.getId());
                        }

                        if (!Rs2Inventory.hasItem(ItemID.NATURE_RUNE)) {
                            if (!Rs2Bank.hasItem(ItemID.NATURE_RUNE)) {
                                Microbot.showMessage("Nature Runes not found");
                                shutdown();
                                return;
                            }

                            Rs2Bank.withdrawAll(ItemID.NATURE_RUNE);
                            Rs2Inventory.waitForInventoryChanges(1200);
                        }
                        if (!Rs2Inventory.hasItem(ItemID.ASTRAL_RUNE)) {
                            if (!Rs2Bank.hasItem(ItemID.ASTRAL_RUNE)) {
                                Microbot.showMessage("Astral Runes not found");
                                shutdown();
                                return;
                            }

                            Rs2Bank.withdrawAll(ItemID.ASTRAL_RUNE);
                            Rs2Inventory.waitForInventoryChanges(1200);
                        }
                        if (!Rs2Inventory.hasItem(plugin.getPlankLog().getLogItemID())) {
                            if (!Rs2Bank.hasItem(plugin.getPlankLog().getLogItemID())) {
                                Microbot.showMessage(plugin.getPlankLog().getLogName()+" not found");
                                shutdown();
                                return;
                            }

                            Rs2Bank.withdrawAll(plugin.getPlankLog().getLogItemID());
                            Rs2Inventory.waitForInventoryChanges(1200);
                        }
//                        Rs2Bank.withdrawX(ItemID.FLAX,25);
//                        Rs2Bank.closeBank();
                        sleep(Rs2Random.randomGaussian(300,50));
                        Rs2Keyboard.keyPress(KeyEvent.VK_ESCAPE);
                        sleepUntil(() -> !Rs2Bank.isOpen());
                        break;
                    case CASTING:
                        if (!Rs2Inventory.hasItem(ItemID.ASTRAL_RUNE)&&!Rs2Inventory.hasItem(ItemID.NATURE_RUNE)) {
                            Microbot.showMessage("Runes not found");
                            shutdown();
                            return;
                        }
                        Rs2Magic.cast(MagicAction.PLANK_MAKE);
                        Rs2Inventory.interact(Rs2Inventory.getLast(plugin.getPlankLog().getLogItemID()));

                        Rs2Player.waitForXpDrop(Skill.MAGIC, 10000, false);
                        break;
                }

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
        Rs2Antiban.resetAntibanSettings();
        super.shutdown();
    }

    private boolean hasStateChanged() {
        if (state == null) return true;
        if (state == MagicState.BANKING && hasRequiredItems()) return true;
        if (state == MagicState.CASTING && !hasRequiredItems()) return true;
        return false;
    }

    private MagicState updateState() {
        if (state == null) return hasRequiredItems() ? MagicState.CASTING : MagicState.BANKING;
        if (state == MagicState.BANKING && hasRequiredItems()) return MagicState.CASTING;
        if (state == MagicState.CASTING && !hasRequiredItems()) return MagicState.BANKING;
        return null;
    }

    private boolean hasRequiredItems() {
        if (Rs2Inventory.hasItem(plugin.getPlankLog().getLogName())) {
            return Rs2Inventory.hasItem(plugin.getPlankLog().getLogName());
        }
        return false;
    }
}