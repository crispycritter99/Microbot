package net.runelite.client.plugins.microbot.aiomagic.scripts;

import net.runelite.api.Skill;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.aiomagic.AIOMagicLocalPlugin;
import net.runelite.client.plugins.microbot.aiomagic.enums.MagicState;
import net.runelite.client.plugins.microbot.util.antiban.Rs2Antiban;
import net.runelite.client.plugins.microbot.util.antiban.Rs2AntibanSettings;
import net.runelite.client.plugins.microbot.util.antiban.enums.Activity;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.inventory.Rs2ItemModel;
import net.runelite.client.plugins.microbot.util.magic.Rs2Magic;
import net.runelite.client.plugins.microbot.util.magic.Rs2Spells;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;

import javax.inject.Inject;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class AlchScript extends Script {

    private MagicState state = MagicState.CASTING;
    private final AIOMagicLocalPlugin plugin;

    @Inject
    public AlchScript(AIOMagicLocalPlugin plugin) {
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
        Rs2Antiban.setActivity(Activity.ALCHING);
        mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> {
            try {
                if (!Microbot.isLoggedIn()) return;
                if (!super.run()) return;
                long startTime = System.currentTimeMillis();

                if (plugin.getAlchItemNames().isEmpty()) {
                    Microbot.showMessage("Alch Item list is empty");
                    shutdown();
                    return;
                }

                if (state == null) {
                    Microbot.showMessage("Unable to evaluate state");
                    shutdown();
                    return;
                }

                switch (state) {
                    case CASTING:
                        if (plugin.getAlchItemNames().isEmpty() || plugin.getAlchItemNames().stream().noneMatch(Rs2Inventory::hasItem)) {
                            Microbot.log("Missing alch items...");
                            return;
                        }

                        if (!Rs2Magic.hasRequiredRunes(plugin.getAlchSpell())) {
                            Microbot.showMessage("Out of runes for alchemy");
                            shutdown();
                            return;
                        }
                        
                        Rs2ItemModel alchItem = plugin.getAlchItemNames().stream()
                                .filter(Rs2Inventory::hasItem)
                                .map(Rs2Inventory::get)
                                .findFirst()
                                .orElse(null);
                        
                        if (alchItem == null) {
                            Microbot.log("Missing alch items...");
                            return;
                        }
                        
                        if (Rs2AntibanSettings.naturalMouse) {
                            int inventorySlot = Rs2Player.getSkillRequirement(Skill.MAGIC, 55) ? 12 : 4;
                            if (alchItem.getSlot() != inventorySlot) {
                                Rs2Inventory.moveItemToSlot(alchItem, inventorySlot);
                                return;
                            }
                        }
                        
                        Rs2Magic.alch(alchItem);
                        Rs2Player.waitForXpDrop(Skill.MAGIC, 10000, false);
                        double LOG_MEAN = 0.05; double LOG_STD = 0.34;
                        Random r = new Random();double gaussian = r.nextGaussian();
                        double value = Math.exp(LOG_MEAN + LOG_STD * gaussian);
                        sleep((int) value*200);
                        break;
                }

                long endTime = System.currentTimeMillis();
                long totalTime = endTime - startTime;
                System.out.println("Total time for loop " + totalTime);

            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
        }, 0, 600, TimeUnit.MILLISECONDS);
        return true;
    }

    @Override
    public void shutdown() {
        Rs2Antiban.resetAntibanSettings();
        super.shutdown();
    }
    
    private Rs2Spells getAlchSpell() {
        return Rs2Player.getBoostedSkillLevel(Skill.MAGIC) >= 55 ? Rs2Spells.HIGH_LEVEL_ALCHEMY : Rs2Spells.LOW_LEVEL_ALCHEMY;
    }

}
