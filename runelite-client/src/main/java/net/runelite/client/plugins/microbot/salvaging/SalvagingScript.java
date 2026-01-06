package net.runelite.client.plugins.microbot.salvaging;

import net.runelite.api.GameObject;
import net.runelite.api.GameState;
import net.runelite.api.NPC;
import net.runelite.api.Skill;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;

import net.runelite.client.plugins.microbot.api.boat.models.Rs2BoatModel;
import net.runelite.client.plugins.microbot.api.tileobject.Rs2TileObjectCache;
import net.runelite.client.plugins.microbot.util.antiban.Rs2AntibanSettings;
import net.runelite.client.plugins.microbot.util.dialogues.Rs2Dialogue;
import net.runelite.client.plugins.microbot.util.gameobject.Rs2GameObject;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.magic.Rs2Magic;
import net.runelite.client.plugins.microbot.util.math.Rs2Random;
import net.runelite.client.plugins.microbot.util.npc.Rs2Npc;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;
//import net.runelite.client.plugins.microbot.util.sailing.Rs2Sailing;
import net.runelite.client.plugins.microbot.util.player.Rs2PlayerModel;
import net.runelite.client.plugins.microbot.util.security.Login;
import net.runelite.client.plugins.microbot.util.tile.Rs2Tile;
import net.runelite.client.plugins.microbot.util.widget.Rs2Widget;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static net.runelite.client.plugins.microbot.salvaging.SalvagingPlugin.SALVAGE_LEVEL_REQ;
import static net.runelite.client.plugins.microbot.util.gameobject.Rs2GameObject.findObjectById;


public class SalvagingScript extends Script {
    public static boolean tentacle = false;
    NPC vorkath;
    public static int chestiterate=0;
    public static boolean lootnet = false;
     boolean test = false;
    @Inject
    Rs2TileObjectCache rs2TileObjectCache;
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
                    {
                        if (Rs2Inventory.contains("seed box")&&Rs2Inventory.contains(false, "hemp", "cotton", "camphor", "frag","ironwood")) {
                            Rs2Inventory.interact(24482, "Fill");
                        }
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
                        sleep(600,1800);
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
                }
                var shipwreck = rs2TileObjectCache.query()
                        .where(x -> x.getName() != null&&x.getId()==60474&& x.getName().toLowerCase().contains("shipwreck"))
                        .within(10)
                        .nearestOnClientThread();
                lootnet=(shipwreck!=null);
                var player = new Rs2PlayerModel();
                if (!Rs2Inventory.isFull()&&SalvagingPlugin.shouldloot) {
//                    Rs2GameObject.interact(60273);
                    rs2TileObjectCache.query( )
                            .fromWorldView()
                            .where(x -> x.getName() != null && x.getName().toLowerCase().contains("cargo hold"))
                            .where(x -> x.getWorldView().getId() == new Rs2PlayerModel().getWorldView().getId())
                            .nearestOnClientThread()
                            .click();
                Rs2Widget.waitForWidget("Dark Whip",5000);
Rs2Widget.clickWidget("Martial salvage");

                Rs2Inventory.waitForInventoryChanges(1800);
                    chestiterate--;
                    if (chestiterate<1) {
                        SalvagingPlugin.shouldloot = false;
                    }
//                sleep(300,500);
                    Rs2Widget.clickWidget("Close");
                    sleep(600,1200);
                }

                if (!Rs2Inventory.isFull()&&shipwreck!=null) {
                    //x.getName().toLowerCase().contains("salvaging hook")&&
                    rs2TileObjectCache.query().fromWorldView().where(x -> x.getName() != null && x.getId()==60506).nearestOnClientThread().click("Deploy");
                    sleepUntil(() -> player.getAnimation() != -1, 5000);
                    return;
                }
                if (Rs2Inventory.contains("salvage",false)){
                    rs2TileObjectCache.query()
                            .fromWorldView()
                            .where(x -> x.getName() != null && x.getName().equalsIgnoreCase("salvaging station"))
                            .where(x -> x.getWorldView().getId() == new Rs2PlayerModel().getWorldView().getId())
                            .nearestOnClientThread()
                            .click();
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