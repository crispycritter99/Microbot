package net.runelite.client.plugins.microbot.salvaging;

import net.runelite.api.GameObject;
import net.runelite.api.GameState;
import net.runelite.api.NPC;
import net.runelite.api.Skill;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;

import net.runelite.client.plugins.microbot.util.antiban.Rs2AntibanSettings;
import net.runelite.client.plugins.microbot.util.gameobject.Rs2GameObject;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.magic.Rs2Magic;
import net.runelite.client.plugins.microbot.util.math.Rs2Random;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;
import net.runelite.client.plugins.microbot.util.sailing.Rs2Sailing;
import net.runelite.client.plugins.microbot.util.security.Login;
import net.runelite.client.plugins.microbot.util.widget.Rs2Widget;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static net.runelite.client.plugins.microbot.util.gameobject.Rs2GameObject.findObjectById;


public class SalvagingScript extends Script {
    public static boolean tentacle = false;
    NPC vorkath;
    public static boolean lootnet = false;
     boolean test = false;
    public boolean run(SalvageConfig config) {
        Microbot.enableAutoRunOn = false;

        mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> {
            try {
                if (!Microbot.isLoggedIn()) return;
                if (!super.run()) return;


                long startTime = System.currentTimeMillis();
                    if ((Rs2Player.isAnimating())||Rs2Player.isMoving()) {
                        return;

                }
                {
                    String alchInput = Microbot.getConfigManager().getConfiguration(
                            "salvage",
                            "listOfItemsToalch",
                            String.class);
                    List<String> keywords = Arrays.stream(alchInput.split(","))
                            .map(String::trim)
                            .collect(Collectors.toList());

                    Rs2Inventory.all().stream()
                            .filter(item -> {
                                String name = item.getName().toLowerCase();
                                return keywords.stream().anyMatch(name::contains);
                            })
                            .forEach(item -> {
                                Rs2Magic.alch(item);
                                Rs2Player.waitForXpDrop(Skill.MAGIC);
                            });

                    String dropInput = Microbot.getConfigManager().getConfiguration(
                            "salvage",
                            "listOfItemsToDrop",
                            String.class);
                    List<String> dropkeywords = Arrays.stream(dropInput.split(","))
                            .map(String::trim)
                            .collect(Collectors.toList());

                    Rs2Inventory.all().stream()
                            .filter(item -> {
                                String name = item.getName().toLowerCase();
                                return dropkeywords.stream().anyMatch(name::contains);
                            })
                            .forEach(item -> {
                                Rs2Inventory.invokeMenu(item, "Drop");
                                if (!Rs2AntibanSettings.naturalMouse)
                                    sleep(150, 300);
                            });
                }
//                Rs2Inventory.dropAllExcept("coins","nature","salvage","seed","amulet","dull","scroll","bottle");
//                if (!Rs2Inventory.isFull()&&SalvagingPlugin.shouldloot) {
//                    Rs2GameObject.interact(60273);
//
//
//                }
                if (!Rs2Inventory.isFull()&&SalvagingPlugin.shouldloot) {
                    Rs2GameObject.interact(60273);
                Rs2Widget.waitForWidget("Dark Whip",5000);
Rs2Widget.clickWidget("Large salvage");
                SalvagingPlugin.shouldloot = false;
                sleep(300,500);
                }
                if (!Rs2Inventory.isFull()) {
                    for (GameObject wreck : SalvagingPlugin.wrecks) {
//                        System.out.println(wreck.getWorldLocation());
                        System.out.println(wreck.getWorldLocation().distanceTo(Rs2Sailing.getPlayerBoatLocation()));
                        if (wreck.getWorldLocation().distanceTo(Rs2Sailing.getPlayerBoatLocation()) < 10) {
                            SalvagingOverlay.isClose = true;

                            Rs2GameObject.interact(60504, "Deploy");
                            sleep(1200);
                            return;
                        }
                        else {
                            SalvagingOverlay.isClose = false;
                        }
                    }

                }
                if (Rs2Inventory.contains(32853)){
//                    Rs2GameObject.interact(59701, "Sort-salvage");
                    Rs2GameObject.interact(59701);
                    sleep(1200);
                    return;
                }
                if (2>1) return;
                System.out.println("Hopping worlds...");
                Rs2Random.waitEx(3200, 800); // this sleep is required to avoid the message: please finish what you're doing before using the world switcher.

                int world = Login.getNextWorld(Rs2Player.isMember());
                boolean hoppedWorlds = Microbot.hopToWorld(world);
                if (hoppedWorlds) {
                    sleepUntil(() -> Microbot.getClient().getGameState() == GameState.HOPPING);
                    sleepUntil(() -> Microbot.getClient().getGameState() == GameState.LOGGED_IN);
                    System.out.println("Successfully hopped to world: " + world);
                    Rs2Random.waitEx(3200, 800); // this sleep is required to avoid the message: please finish what you're doing before using the world switcher.

                }
                System.out.println(Rs2Sailing.getPlayerBoatLocation());
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
        super.shutdown();
    }

}