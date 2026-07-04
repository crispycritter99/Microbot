package net.runelite.client.plugins.microbot.frostDragonKiller;

import net.runelite.api.*;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldArea;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.widgets.ComponentID;
import net.runelite.api.widgets.InterfaceID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.api.npc.Rs2NpcQueryable;
import net.runelite.client.plugins.microbot.api.tileobject.Rs2TileObjectCache;
import net.runelite.client.plugins.microbot.api.tileobject.models.Rs2TileObjectModel;
import net.runelite.client.plugins.microbot.breakhandler.BreakHandlerScript;
import net.runelite.client.plugins.microbot.globval.enums.InterfaceTab;
import net.runelite.client.plugins.microbot.sailing.features.trials.BoatLocation;
import net.runelite.client.plugins.microbot.util.antiban.Rs2Antiban;
import net.runelite.client.plugins.microbot.util.bank.Rs2Bank;
import net.runelite.client.plugins.microbot.util.bank.enums.BankLocation;
import net.runelite.client.plugins.microbot.util.combat.Rs2Combat;
import net.runelite.client.plugins.microbot.util.coords.Rs2WorldPoint;
import net.runelite.client.plugins.microbot.util.depositbox.Rs2DepositBox;
import net.runelite.client.plugins.microbot.util.dialogues.Rs2Dialogue;
import net.runelite.client.plugins.microbot.util.equipment.Rs2Equipment;
import net.runelite.client.plugins.microbot.util.gameobject.Rs2GameObject;
import net.runelite.client.plugins.microbot.util.grounditem.InteractModel;
import net.runelite.client.plugins.microbot.util.grounditem.Rs2GroundItem;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.inventory.Rs2ItemModel;
import net.runelite.client.plugins.microbot.util.item.Rs2EnsouledHead;
import net.runelite.client.plugins.microbot.util.item.Rs2ItemManager;
import net.runelite.client.plugins.microbot.util.keyboard.Rs2Keyboard;
import net.runelite.client.plugins.microbot.util.magic.Rs2Magic;
import net.runelite.client.plugins.microbot.util.magic.Rs2Spells;
import net.runelite.client.plugins.microbot.util.math.Rs2Random;
import net.runelite.client.plugins.microbot.util.menu.NewMenuEntry;
import net.runelite.client.plugins.microbot.util.misc.Rs2UiHelper;
import net.runelite.client.plugins.microbot.util.models.RS2Item;
import net.runelite.client.plugins.microbot.util.npc.Rs2NpcModel;
import net.runelite.client.plugins.microbot.util.npc.Rs2Npc;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;
import net.runelite.client.plugins.microbot.util.player.Rs2PlayerModel;
import net.runelite.client.plugins.microbot.util.prayer.Rs2Prayer;
import net.runelite.client.plugins.microbot.util.prayer.Rs2PrayerEnum;
import net.runelite.client.plugins.microbot.util.reflection.Rs2Reflection;
import net.runelite.client.plugins.microbot.util.tabs.Rs2Tab;
import net.runelite.client.plugins.microbot.util.tile.Rs2Tile;
import net.runelite.client.plugins.microbot.util.walker.Rs2Walker;
import net.runelite.client.plugins.microbot.util.widget.Rs2Widget;
import net.runelite.client.plugins.skillcalculator.skills.MagicAction;

import javax.inject.Inject;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.*;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static net.runelite.client.plugins.microbot.util.Global.sleepUntil;
import static net.runelite.client.plugins.microbot.util.gameobject.Rs2GameObject.getAll;
import static net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory.items;


public class FrostDragonKillerScript extends Script {
    public static boolean tentacle = false;
    NPC vorkath;
    private WorldPoint workingTile = null;
    @Inject
    Rs2TileObjectCache rs2TileObjectCache;
    public static boolean lootnet = false;
     boolean test = false;
    private volatile Set<String> lootItems = Collections.emptySet();

    public void updateLootItems(String csvItems) {
        if (csvItems == null || csvItems.trim().isEmpty()) {
            lootItems = Collections.emptySet();
            return;
        }

        Set<String> parsedItems = Arrays.stream(csvItems.split(","))
                .map(String::trim)
                .map(item -> item.toLowerCase(Locale.ROOT))
                .filter(item -> !item.isEmpty())
                .collect(Collectors.toCollection(LinkedHashSet::new));
        lootItems = Collections.unmodifiableSet(parsedItems);
    }

    private boolean shouldLoot(String itemName) {
        if (itemName == null) {
            return false;
        }

        String normalizedName = itemName.toLowerCase(Locale.ROOT);
        return lootItems.stream().anyMatch(normalizedName::contains);
    }

    public boolean run(FrostDragonKillerConfig config) {
        Microbot.enableAutoRunOn = false;

        mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> {
            try {
                if (!Microbot.isLoggedIn()) return;
                if (!super.run()) return;
                long startTime = System.currentTimeMillis();
                if (Rs2Player.isInteracting()) return;
                Rs2NpcModel frostDragon = Rs2Npc.getNpcByIndex(21933);


                if (frostDragon!=null&&!frostDragon.isDead()&&frostDragon.getWorldLocation().distanceTo(Rs2Player.getWorldLocation())<8)
                {
                    double LOG_MEAN = 0.05; double LOG_STD = 0.34;Random r = new Random();double gaussian = r.nextGaussian();
                    double value = Math.exp(LOG_MEAN + LOG_STD * gaussian);
                    sleep((int) value*400);
                    Rs2Npc.attack(frostDragon);
                    sleep(600);
                    return;}

                if (Rs2Inventory.isFull()) return;
                var nearbyItems = Rs2GroundItem.getAll(10);
                    for (var item : nearbyItems) {
                        if (shouldLoot(item.getItem().getName())) {
                            double LOG_MEAN = 0.05; double LOG_STD = 0.34;Random r = new Random();double gaussian = r.nextGaussian();
                            double value = Math.exp(LOG_MEAN + LOG_STD * gaussian);
                            sleep((int) value*400);

                            Rs2Magic.cast(MagicAction.TELEKINETIC_GRAB);
                            r = new Random();gaussian = r.nextGaussian();
                            value = Math.exp(LOG_MEAN + LOG_STD * gaussian);
                            sleep((int) value*400);

                            Rs2GroundItem.interact(item,"Cast");
                            Rs2Inventory.waitForInventoryChanges(3000);
                        }
                    }



                ;









                





                long endTime = System.currentTimeMillis();
                long totalTime = endTime - startTime;

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