package net.runelite.client.plugins.microbot.HunterRumours.features;

import net.runelite.api.Skill;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.gameval.ItemID;
import net.runelite.client.plugins.hunter.HunterTrap;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.salamanderslocal.SalamanderLocalConfig;
import net.runelite.client.plugins.microbot.salamanderslocal.SalamanderLocalHunting;
import net.runelite.client.plugins.microbot.salamanderslocal.SalamanderLocalPlugin;
import net.runelite.client.plugins.microbot.util.antiban.Rs2Antiban;
import net.runelite.client.plugins.microbot.util.antiban.Rs2AntibanSettings;
import net.runelite.client.plugins.microbot.util.antiban.enums.Activity;
import net.runelite.client.plugins.microbot.util.bank.Rs2Bank;
import net.runelite.client.plugins.microbot.util.gameobject.Rs2GameObject;
import net.runelite.client.plugins.microbot.util.grounditem.LootingParameters;
import net.runelite.client.plugins.microbot.util.grounditem.Rs2GroundItem;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;
import net.runelite.client.plugins.microbot.util.walker.Rs2Walker;

import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class SalamanderLocalScript extends Script {
    public static final int SMALL_FISHING_NET = 303;
    public static final int ROPE = 954;
    public static int SalamandersCaught = 0;
    public boolean hasDied = false;
    private final String itemsToLoot = "rope,small fishing net";

    public boolean run(SalamanderLocalConfig config, SalamanderLocalPlugin plugin) {
        Rs2Antiban.resetAntibanSettings();
        applyAntiBanSettings();
        Rs2Antiban.setActivity(Activity.GENERAL_HUNTER);
        mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> {
            try {
                if (!super.run()) return;
                if (!Microbot.isLoggedIn()) return;
                if (!this.isRunning()) return;

                // Check if we have enough supplies
                if (config.salamanderHunting().getName().equals("Black salamander") && hasDied) {
                    Microbot.log("We died - Restocking supplies");
                    handleBanking(config);
                } else if ((Rs2Inventory.count(ROPE) < 1 || Rs2Inventory.count(SMALL_FISHING_NET) < 1) && plugin.getTraps().isEmpty()) {
                    Microbot.log("Not enough supplies, need ropes and fishing nets");
                    handleBanking(config);
                    return;
                }


                // Get selected salamander type from config
                SalamanderLocalHunting salamanderType = getSalamander(config);
                if (salamanderType == null) {
                    return;
                }

//                 Walk to area if not nearby
                if (!isNearSalamanderArea(salamanderType)) {
                    Microbot.log("Walking to " + salamanderType.getName() + " hunting area");
                    Rs2Walker.walkTo(salamanderType.getHuntingPoint());
                    return;
                }

                // Count existing traps from plugin's trap map
                int activeTrapCount = plugin.getTraps().size();
                if(getSalamander(config).getName().equals("Red salamander")){activeTrapCount=3;}
                int maxTraps = getMaxTrapsForHunterLevel(config);
                lootRobeAndNets();
                if (Rs2Inventory.count() > 20) {
                    cleanInventory();
                }
                // Tend to active traps
                boolean handledTrap = handleExistingTraps(plugin, config);
                if (handledTrap) return;

                // Set new traps if we have space and supplies
                if (activeTrapCount < maxTraps && !IsRopeOnTheGround()) {
                    setNewTrap(salamanderType, config);
                }

            } catch (Exception ex) {
                System.out.println("Salamander Script Error: " + ex.getMessage());
                ex.printStackTrace();
            }
        }, 0, 1000, TimeUnit.MILLISECONDS);
        return true;
    }

    private SalamanderLocalHunting getSalamander(SalamanderLocalConfig config) {
        if (config.progressiveHunting()) {
            return getBestSalamander();
        }

        return config.salamanderHunting();
    }

    private SalamanderLocalHunting getBestSalamander() {
        var skillLevel = Microbot.getClient().getRealSkillLevel(Skill.HUNTER);
        if (skillLevel > 78) {
            return SalamanderLocalHunting.TECU;
        } else if (skillLevel > 66) {
            return SalamanderLocalHunting.BLACK;
        } else if (skillLevel > 58) {
            return SalamanderLocalHunting.RED;
        } else if (skillLevel > 46) {
            return SalamanderLocalHunting.ORANGE;
        } else if (skillLevel > 28) {
            return SalamanderLocalHunting.GREEN;
        }
        Microbot.log("Not high enough hunter level for any salamander");
        shutdown();
        return null;
    }

    private void applyAntiBanSettings() {
        Rs2AntibanSettings.antibanEnabled = true;
        Rs2AntibanSettings.usePlayStyle = true;
        Rs2AntibanSettings.simulateFatigue = true;
        Rs2AntibanSettings.simulateAttentionSpan = true;
        Rs2AntibanSettings.behavioralVariability = true;
        Rs2AntibanSettings.nonLinearIntervals = true;
        Rs2AntibanSettings.naturalMouse = true;
        Rs2AntibanSettings.simulateMistakes = true;
        Rs2AntibanSettings.moveMouseOffScreen = true;
        Rs2AntibanSettings.contextualVariability = true;
        Rs2AntibanSettings.devDebug = false;
        Rs2AntibanSettings.playSchedule = true;
        Rs2AntibanSettings.actionCooldownChance = 0.1;
    }

    private void handleBanking(SalamanderLocalConfig config) {
        Rs2Bank.walkToBank();
        Rs2Bank.openBank();
        Rs2Bank.withdrawX(ROPE, config.withdrawNumber());
        Rs2Bank.withdrawX(SMALL_FISHING_NET, config.withdrawNumber());
        Rs2Bank.closeBank();
        hasDied = false;
    }

    private boolean isNearSalamanderArea(SalamanderLocalHunting salamanderType) {
        // Check if within ~20 tiles of the hunting point
        WorldPoint currentLocation = Rs2Player.getWorldLocation();
        return currentLocation.distanceTo(salamanderType.getHuntingPoint()) <= 20;
    }

    public int getMaxTrapsForHunterLevel(SalamanderLocalConfig config) {
        int hunterLevel = Microbot.getClient().getRealSkillLevel(Skill.HUNTER);
        int base = 0;
        // In the wilderness we get +1 trap
        if (config.salamanderHunting() != null && config.salamanderHunting().getName().equals("Black salamander")) {
            base = 1;
        }
        if (hunterLevel >= 80) return 5 + base;
        if (hunterLevel >= 60) return 4 + base;
        if (hunterLevel >= 40) return 3 + base;
        if (hunterLevel >= 20) return 2 + base;
        return 1;
    }

    private boolean handleExistingTraps(SalamanderLocalPlugin plugin, SalamanderLocalConfig config) {
        // Filter for FULL traps and sort by time (traps about to collapse first) and then pick the first one
        var trapToHandle = plugin.getTraps().entrySet().stream()
                .filter(entry -> entry.getValue().getState() == HunterTrap.State.FULL)
                .sorted((a, b) -> Double.compare(b.getValue().getTrapTimeRelative(), a.getValue().getTrapTimeRelative())).collect(Collectors.toList()).stream().findFirst().orElse(null);
        if (trapToHandle == null) return false;
        WorldPoint location = trapToHandle.getKey();
        if (!Rs2Player.isAnimating() && !Rs2Player.isMoving()) {
            var gameObject = Rs2GameObject.getGameObject(location);
            if (gameObject != null) {
                Rs2GameObject.interact(gameObject, "Reset");
                SalamandersCaught++;
                sleep(config.minSleepAfterCatch(), config.maxSleepAfterCatch());
                return true;
            }
        }
        return false;
    }

    private void setNewTrap(SalamanderLocalHunting salamanderType, SalamanderLocalConfig config) {
        if (Rs2GameObject.exists(salamanderType.getTreeId())) {
            Rs2GameObject.interact(salamanderType.getTreeId(), "Set-trap",7);
            sleep(config.minSleepAfterLay(), config.maxSleepAfterLay());
        }
    }

    public boolean IsRopeOnTheGround() {
        return Rs2GroundItem.exists(ROPE, 10) || Rs2GroundItem.exists(303, 10);
    }

    @Override
    public void shutdown() {
        super.shutdown();
        SalamandersCaught = 0;
    }
    private void cleanInventory() {
        Rs2Inventory.items().forEachOrdered(item -> {
            if (item.getId() == ItemID.BLACK_SALAMANDER || item.getId() == ItemID.GREEN_SALAMANDER || item.getId() == ItemID.ORANGE_SALAMANDER || item.getId() == ItemID.RED_SALAMANDER || item.getId() == ItemID.IMMATURE_MOUNTAIN_SALAMANDER) {
                Rs2Inventory.interact(item, "Release");
                sleep(150, 350);
            }
        });
    }

    private void lootRobeAndNets() {
        LootingParameters valueParams = new LootingParameters(
                15,
                1,
                1,
                1,
                false,
                true,
                itemsToLoot.trim().split(",")
        );
        if (Rs2GroundItem.lootItemsBasedOnNames(valueParams)) {
            Microbot.pauseAllScripts.compareAndSet(true, false);
        }
    }
}
