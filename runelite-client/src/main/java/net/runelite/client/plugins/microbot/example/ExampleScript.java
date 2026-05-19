package net.runelite.client.plugins.microbot.example;

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


public class ExampleScript extends Script {
    public static boolean tentacle = false;
    NPC vorkath;
    private WorldPoint workingTile = null;
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
//                if (Rs2Npc.getNpc(494)==null&&Rs2Npc.getNpc(496)!=null){
////                    Rs2Inventory.interact("Fishing Explosive");
////                    sleep(200,400);
//                    sleepGaussian(600,300);
//                    Rs2Inventory.useItemOnNpc(6664,496);
//                    sleep(10000);
//                    sleepUntil(Rs2Player::isInCombat);
//                }
//                System.out.println(""+Rs2Widget.isWidgetVisible(InterfaceID.DIALOG_OPTION, 1));
//                System.out.println(""+startTime);
//                Rs2Dialogue.keyPressForCombinationOption("Teak");
//                shutdown();
//                if (!Rs2Dialogue.hasCombinationDialogue()) return ;
//                System.out.println(""+"hi");
//
//                Rs2ItemModel gem = Rs2Inventory.get("uncut red topaz");
//                12582913
//                System.out.println(Rs2Widget.getWidget(12582913));
//               System.out.println(Rs2DepositBox.getItemWidget(gem.getSlot()).getOriginalX());
//                Rs2DepositBox.invokeMenu(6, gem);
//                    TileObject fung = Rs2GameObject.get("Fairy ring");
//                ObjectComposition composition = Rs2GameObject.convertToObjectComposition(fung);
                ;

//                var ops=composition.getOps();
//                int opIdx = 3;
//                int numSubOps = ops.getNumSubOps(3);
//        for (int subIdx = 0; subIdx < numSubOps; subIdx++)
//        {
//            String subOp = ops.getSubOp(opIdx, subIdx);
//            if (subOp == null) continue;
////            assert subOp != null;
//            if (subOp.contains("DJR"))
//            {
//                int subID = ops.getSubID(opIdx, subIdx);
//                System.out.println(95031+65536*(subID));
//
//            }
//        }
//                Rs2GameObject.clickObject(fung,"Favourites","DJR");
//                shutdown();
//                double LOG_MEAN = 0.05; double LOG_STD = 0.34;Random r = new Random();double gaussian = r.nextGaussian();
//                double value = Math.exp(LOG_MEAN + LOG_STD * gaussian);
//                sleep((int) value*200);
//                    Rs2GameObject.interact("iron rocks");
//                r = new Random();gaussian = r.nextGaussian();
//                value = Math.exp(LOG_MEAN + LOG_STD * gaussian);
//                sleep((int) value*200);
//                    Rs2Inventory.hover(0);
//                    Rs2Player.waitForXpDrop(Skill.MINING,5000);
//                Rs2Player.waitForXpDrop(Skill.MINING,5000);
//                r = new Random();gaussian = r.nextGaussian();
//                 value = Math.exp(LOG_MEAN + LOG_STD * gaussian);
//                sleep((int) value*200);
//                    Rs2Inventory.dropAll("iron ore");
//                Widget dialogueOption = Rs2Dialogue.getCombinationOption("Mahogany", false);
////                System.out.println(""+dialogueOption.getOnKeyListener()[7].toString().charAt(0));
//                Object[] keys = dialogueOption.getOnKeyListener();
//                if (keys != null) {
//                    for (int i = 0; i < keys.length; i++) {
//                        Microbot.log("index " + i + " = " + keys[i]);
//                    }
//                }
//                if (dialogueOption == null) return ;
//                NewMenuEntry menuEntry = new NewMenuEntry(
//                        -1,
//                        Rs2PrayerEnum.PROTECT_MELEE.getIndex(),
//                        MenuAction.CC_OP.getId(),
//                        1,
//                        -1,
//                        !Rs2Prayer.isPrayerActive(Rs2PrayerEnum.PROTECT_MELEE) ? "Activate": "Deactivate"
//                );
//
//                Rectangle prayerBounds = new Rectangle(1, 1);
//
//                Microbot.doInvoke(menuEntry, prayerBounds);
//                Microbot.doInvoke(new NewMenuEntry("Eat", Rs2Inventory.slot(385), 9764864, MenuAction.CC_OP.getId(), 2, 385, "Shark"), new Rectangle(1, 1));
//shutdown();
//                Microbot.status=""+dialogueOption.getText();

//                if (Rs2Player.isAnimating(6500)||Rs2Player.isMoving()) return;
//                double LOG_MEAN = 1; double LOG_STD = 0.8;
//                Random r = new Random();double gaussian = r.nextGaussian();
//                double value = Math.exp(LOG_MEAN + LOG_STD * gaussian);
//                sleep((int) value * 1000+2000);
//                if (Rs2Player.isAnimating(6500)||Rs2Player.isMoving()) return;
//
//                Rs2GameObject.interact(39095);
//                sleep(600);
                if (workingTile == null) {
                   workingTile = Microbot.getRs2TileObjectCache().query().withNames("Mythical cape","Guild trophy space").nearest().getWorldLocation();
                }
                Rs2TileObjectModel space = Microbot.getRs2TileObjectCache().query()
                        .where(o -> o.getWorldLocation().equals(workingTile))
                        .nearest();
//                System.out.println(""+space.getId());
//                if (space!=null&&space.getName().contains("Mythical")) {
//                    if (space.click()) {
//                        boolean done = false;
//                        long detectedTime = -1;
//                        long startTimeloop = System.currentTimeMillis();
//                        do {
//                            done = Rs2Widget.findWidget("Really remove it?", null) != null;
//
//
//                            sleep(100);
//                        } while (!done && System.currentTimeMillis() - startTime < 5000);
//

//                        if (Rs2Widget.findWidget("Really remove it?", null) != null) {
//                            double LOG_MEAN = 0.25;
//                            double LOG_STD = 0.34;
//                            Random r = new Random();
//                            double gaussian = r.nextGaussian();
//                            double value = Math.exp(LOG_MEAN + LOG_STD * gaussian);
//                            sleep((int) value * 200);
//                            Rs2Keyboard.keyPress('1');
//                            sleep(600);
//                        }
////                    }
////                }
//                if (Rs2Widget.findWidget("Repeat last task?", null) != null){
//                    double LOG_MEAN = 0.25; double LOG_STD = 0.34;Random r = new Random();double gaussian = r.nextGaussian();
//                    double value = Math.exp(LOG_MEAN + LOG_STD * gaussian);
//                    sleep((int) value*200);
//                    Rs2Keyboard.keyPress('1');
//                    sleep(600);
//
//                }
//                if (Rs2Inventory.count("Teak plank")<4) return;
////                if (Rs2GameObject.interact(new WorldPoint(13022,663,1))) {
////                    boolean done = false;
////                    long detectedTime = -1;
////                    long startTimeloop = System.currentTimeMillis();
////                    do {
////                        done = Rs2Widget.findWidget("Furniture Creation Menu", null) != null;
////
////
////                        sleep(100);
////                    } while (!done && System.currentTimeMillis() - startTime < 5000);
//
//                    if (Rs2Widget.findWidget("Furniture Creation Menu", null) != null) {
//                        double LOG_MEAN = 0.25;
//                        double LOG_STD = 0.34;
//                        Random r = new Random();
//                        double gaussian = r.nextGaussian();
//                        double value = Math.exp(LOG_MEAN + LOG_STD * gaussian);
//                        sleep((int) value * 200);
//                        Rs2Keyboard.keyPress('4');
//                        sleep(600);
//
//                    }
//                Rs2Dialogue.sleepUntilHasCombinationDialogue();

//                        Rs2Dialogue.clickCombinationOption(plugin.getPlank().getDialogueOption());
//                Rs2Dialogue.keyPressForCombinationOption(plugin.getPlank().getDialogueOption());
                if (Rs2Dialogue.hasCombinationDialogue()) {
                    double LOG_MEAN = 0.25;
                    double LOG_STD = 0.34;
                    Random r = new Random();
                    double gaussian = r.nextGaussian();
                    double value = Math.exp(LOG_MEAN + LOG_STD * gaussian);
                    sleep((int) value * 200);
                    Rs2Keyboard.keyPress(KeyEvent.VK_SPACE);
                    sleep(600);

                }
//                }
//                if (Rs2Inventory.contains(2353)){
//
//                    Rs2GameObject.interact("Ancient Furnace","Smelt");
//                        Rs2Player.waitForWalking();
//                        Rs2Keyboard.keyPress('1');
//                        sleep(1200);
//                    }
//
//                if (!Rs2Inventory.contains(2353)&&Rs2Inventory.contains(2354)) {
//                    if (Microbot.getClient().getWidget(14352385) == null) {
//                        if (!Rs2Inventory.isItemSelected()) {
//                            Rs2Inventory.use(2354);
//                        } else {
//                            Rs2Npc.interact("Isles", "use");
//                            Rs2Player.waitForWalking();
//                        }
//                    } else if (Microbot.getClient().getWidget(14352385) != null) {
//                        Rs2Keyboard.keyPress('3');
//                        Rs2Inventory.waitForInventoryChanges(2000);
//                    }
//                }
//                Rs2NpcModel npc = Rs2Npc.getAttackableNpcs("kalphite worker").findFirst().orElse(null);
//                if (npc == null) return;

//                if (Rs2Inventory.isFull())
//                {
//                    Rs2Inventory.dropAll(false,"scimitar","bar","dagger","necklace");
//                }
//                Rs2GameObject.interact(60514);
//                Rs2Player.waitForXpDrop(Skill.THIEVING,1800);
                
//                Rs2Walker.walkFastCanvas(new WorldPoint(2799,9568,3));
//                sleep(600);
//            Rs2Player.waitForXpDrop(Skill.AGILITY,1800);
//                    Rs2Inventory.interact("unfinished broad bolts","use");
//                    Rs2Inventory.interact("feather");

//                Rs2Inventory.slotInteract(27,"use");
//                Rs2GameObject.interact("Chaos Altar");
//
////                        return;
////
////                }
//                    if (Rs2Player.isInCombat()) return;
//                if (Rs2Inventory.contains("wing",false)){
//                    Rs2Walker.walkTo(new WorldPoint(1559,9452,0));
//                    shutdown();
////                        sleep(6000);
//                    return;
//                }
//                    Rs2Npc.interact("Demonic gorilla","attack");

//                    sleep(600);


//                Rs2Player.waitForAnimation(1000);

                long endTime = System.currentTimeMillis();
                long totalTime = endTime - startTime;
                System.out.println("Total time for loop " + totalTime);

            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
        }, 0, 100, TimeUnit.MILLISECONDS);
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