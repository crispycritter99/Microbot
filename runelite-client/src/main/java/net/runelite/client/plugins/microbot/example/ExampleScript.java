package net.runelite.client.plugins.microbot.example;

import net.runelite.api.*;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldArea;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.widgets.ComponentID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.api.npc.Rs2NpcQueryable;
import net.runelite.client.plugins.microbot.api.tileobject.Rs2TileObjectCache;
import net.runelite.client.plugins.microbot.breakhandler.BreakHandlerScript;
import net.runelite.client.plugins.microbot.globval.enums.InterfaceTab;
import net.runelite.client.plugins.microbot.util.antiban.Rs2Antiban;
import net.runelite.client.plugins.microbot.util.bank.Rs2Bank;
import net.runelite.client.plugins.microbot.util.bank.enums.BankLocation;
import net.runelite.client.plugins.microbot.util.combat.Rs2Combat;
import net.runelite.client.plugins.microbot.util.coords.Rs2WorldPoint;
import net.runelite.client.plugins.microbot.util.dialogues.Rs2Dialogue;
import net.runelite.client.plugins.microbot.util.equipment.Rs2Equipment;
import net.runelite.client.plugins.microbot.util.gameobject.Rs2GameObject;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.inventory.Rs2ItemModel;
import net.runelite.client.plugins.microbot.util.item.Rs2EnsouledHead;
import net.runelite.client.plugins.microbot.util.item.Rs2ItemManager;
import net.runelite.client.plugins.microbot.util.keyboard.Rs2Keyboard;
import net.runelite.client.plugins.microbot.util.magic.Rs2Magic;
import net.runelite.client.plugins.microbot.util.magic.Rs2Spells;
import net.runelite.client.plugins.microbot.util.math.Rs2Random;
import net.runelite.client.plugins.microbot.util.menu.NewMenuEntry;
import net.runelite.client.plugins.microbot.util.npc.Rs2NpcModel;
import net.runelite.client.plugins.microbot.util.npc.Rs2Npc;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;
import net.runelite.client.plugins.microbot.util.player.Rs2PlayerModel;
import net.runelite.client.plugins.microbot.util.prayer.Rs2Prayer;
import net.runelite.client.plugins.microbot.util.prayer.Rs2PrayerEnum;
import net.runelite.client.plugins.microbot.util.tabs.Rs2Tab;
import net.runelite.client.plugins.microbot.util.tile.Rs2Tile;
import net.runelite.client.plugins.microbot.util.walker.Rs2Walker;
import net.runelite.client.plugins.microbot.util.widget.Rs2Widget;
import net.runelite.client.plugins.skillcalculator.skills.MagicAction;

import javax.inject.Inject;
import java.awt.*;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static net.runelite.client.plugins.microbot.util.Global.sleepUntil;
import static net.runelite.client.plugins.microbot.util.gameobject.Rs2GameObject.getAll;


public class ExampleScript extends Script {
    public static boolean tentacle = false;
    NPC vorkath;
    @Inject
    Rs2TileObjectCache rs2TileObjectCache;
    public static boolean lootnet = false;
     boolean test = false;
    public boolean run(ExampleConfig config) {
        Microbot.enableAutoRunOn = false;

        mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> {
            try {
                if (!Microbot.isLoggedIn()) return;
                if (!super.run()) return;
                long startTime = System.currentTimeMillis();
                Microbot.status="waiting to do something";

                if (Rs2Inventory.isFull()){
                    Microbot.status="grinding crabs";
                    Rs2Inventory.combine("pestle and mortar","blue crab");
                    sleep(23000);
                    sleepUntil(()->!Rs2Inventory.contains("Blue crab"),60000);

                }
                    if (!Rs2Inventory.contains("fish offcuts")) return;
//                GameObject trap = Rs2GameObject.getGameObject("Crab trap (empty)");
////                GameObject fulltrap = Rs2GameObject.getGameObject("Crab trap (full)");
                var fulltrap = rs2TileObjectCache.query()
//                        .fromWorldView()
                        .where(x -> x.getName() != null &&x.getName().equalsIgnoreCase("crab trap (full)"))

                        .nearestOnClientThread();
                var emptytrap = rs2TileObjectCache.query()
//                        .fromWorldView()
                        .where(x -> x.getName() != null &&x.getName().equalsIgnoreCase("crab trap (empty)"))
//                        .where(x -> x.getWorldView().getId() == new Rs2PlayerModel().getWorldView().getId())
                        .nearestOnClientThread();
                System.out.println(emptytrap+" "+fulltrap.getId());
                if (emptytrap!=null) {
                    emptytrap.click();
                    Microbot.status="baiting trap";
                    //sleep until -1 fish offcut
                    Rs2Inventory.waitForInventoryChanges(3000);
                    Microbot.status="idle";
//                    Rs2Inventory.waitForInventoryChanges(1800);
//                    return;
                }
                if (fulltrap!=null) {
                    fulltrap.click();
                    Microbot.status="resetting trap";
                    Rs2Player.waitForXpDrop(Skill.HUNTER);
                    Rs2Inventory.waitForInventoryChanges(3000);
                    Microbot.status="idle";
                    //sleep until -1 fish offcut
//                    Microbot.status="waiting for inventory change";
//                    Rs2Inventory.waitForInventoryChanges(1800);
//                    return;
                }
//                Rs2NpcModel npc = Rs2Npc.getAttackableNpcs("lizardman").findFirst().orElse(null);
//                if (npc == null) return;
//                Rs2Npc.interact(npc);
//                sleep(1200);


//                Rs2Inventory.slotInteract(27,"use");
//                Rs2GameObject.interact("Chaos Altar");
//
////                        return;
////
////                }
//                    if (Rs2Player.isInteracting()) return;
//                if (Rs2Inventory.contains("wing",false)){
//                    Rs2Walker.walkTo(new WorldPoint(1559,9452,0));
//                    shutdown();
////                        sleep(6000);
//                    return;
//                }
//                    Rs2Npc.interact("Sunlight Moth","catch");
//                    sleep(600);


//                Rs2Player.waitForAnimation(1000);
//                shutdown();
                long endTime = System.currentTimeMillis();
                long totalTime = endTime - startTime;
                System.out.println("Total time for loop " + totalTime);

            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
        }, 0, 600, TimeUnit.MILLISECONDS);
        return true;
    }
//                    if ((Rs2Player.isAnimating())||Rs2Player.isMoving()) {
//                if (Rs2Player.isAnimating() || Rs2Player.isMoving()) {
//                    return;
//                }
//                if (Rs2Player.isMoving()) {
//                    return;
//                }
//                if (!Rs2Inventory.isFull()) {
//                    if (Rs2GameObject.interact("tarnished chest")) {
//                        sleep(1800);
//                    }
//                }
//                Rs2Inventory.dropAll(false,"mithril","bar","cannonball");
//                if (!Rs2Inventory.contains(29354)) {
//                    if (!Rs2Inventory.contains(28899)) {
//                        Rs2Inventory.interact(28900);
//                        Rs2Inventory.useItemOnNpc(28900, 13346);
//                        sleep(2000);
//                        Rs2Player.waitForWalking(5000);
//                        Widget widget = Rs2Widget.findWidget("Exchanging:");
//                        if (widget != null) {
//                            Rs2Keyboard.keyPress(String.valueOf(3).charAt(0));
//                        }
//                        Rs2Inventory.waitForInventoryChanges(1800);
//                        return;
//                    }
//                    if (Rs2Inventory.contains(28899)) {
//                        Rs2GameObject.interact(52799, "Bless");
//                        Rs2Player.waitForWalking(5000);
//                        return;
//                    }
//                }
//                if (Rs2Inventory.contains(29354)){
//                    Rs2Inventory.interact(29354,"Break-down");
//                    sleep(2000);
//                    return;
//                }

//                sleep(2500,7800);
//                Rs2GameObject.interact(39095);
//                sleep(1200);
//                if (Rs2Player.isInteracting())return;
//                Microbot.status="ready to go";
    ////                Rs2Npc.interact("s   unlight moth","catch");

    @Override
    public void shutdown() {
        super.shutdown();
    }

}