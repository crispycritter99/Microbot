package net.runelite.client.plugins.microbot.example;

import net.runelite.api.*;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldArea;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.widgets.ComponentID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.util.bank.Rs2Bank;
import net.runelite.client.plugins.microbot.util.bank.enums.BankLocation;
import net.runelite.client.plugins.microbot.util.combat.Rs2Combat;
import net.runelite.client.plugins.microbot.util.dialogues.Rs2Dialogue;
import net.runelite.client.plugins.microbot.util.equipment.Rs2Equipment;
import net.runelite.client.plugins.microbot.util.gameobject.Rs2GameObject;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.inventory.Rs2ItemModel;
import net.runelite.client.plugins.microbot.util.magic.Rs2Magic;
import net.runelite.client.plugins.microbot.util.magic.Rs2Spells;
import net.runelite.client.plugins.microbot.util.math.Rs2Random;
import net.runelite.client.plugins.microbot.util.menu.NewMenuEntry;
import net.runelite.client.plugins.microbot.util.npc.Rs2NpcModel;
import net.runelite.client.plugins.microbot.util.npc.Rs2Npc;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;
import net.runelite.client.plugins.microbot.util.prayer.Rs2Prayer;
import net.runelite.client.plugins.microbot.util.prayer.Rs2PrayerEnum;
import net.runelite.client.plugins.microbot.util.tile.Rs2Tile;
import net.runelite.client.plugins.microbot.util.walker.Rs2Walker;
import net.runelite.client.plugins.microbot.util.widget.Rs2Widget;
import net.runelite.client.plugins.skillcalculator.skills.MagicAction;

import java.awt.*;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static net.runelite.client.plugins.microbot.util.Global.sleepUntil;


public class ExampleScript extends Script {
    public static boolean tentacle = false;
    NPC vorkath;
    public static boolean lootnet = false;
     boolean test = false;
    public boolean run(ExampleConfig config) {
        Microbot.enableAutoRunOn = false;

        mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> {
            try {
                if (!Microbot.isLoggedIn()) return;
                if (!super.run()) return;
                long startTime = System.currentTimeMillis();
                Rs2Bank.withdrawX("air rune",3);

//                Rs2Magic.cast(MagicAction.RESURRECT_GREATER_ZOMBIE);
//                Rs2Inventory.use("house");
//                sleep(600);
//                Rs2Npc.interact(Rs2Npc.getNpc("Phials"));
//                if (Rs2Player.isInteracting())
//                        return;
//                Rs2Magic.cast(MagicAction.EXPERT_REANIMATION);
//                Rs2Inventory.interact("ensouled","Reanimate");
//                Rs2Player.waitForXpDrop(Skill.MAGIC);
//                sleepUntilTick(15);
////                Rs2Walker.walkFastCanvas(new WorldPoint(2799,9568,3));
////                Rs2Player.waitForXpDrop(Skill.AGILITY);
//                    Rs2Widget.clickWidget(49938445);
//                    Rs2Dialogue.sleepUntilHasDialogueOption("Duels");
//
//                    Rs2Dialogue.clickOption("Duels group");
//                sleepUntilTick(3);
//                Rs2Widget.clickWidget(49938444);
////                sleepUntilTick(3);
//                Microbot.log(Arrays.stream(Rs2Widget.getWidget(49938444).getActions()).findFirst().get().contains("Sign")+"");
////                Rs2Widget.clickWidget(49938444);
//            if (!Rs2Prayer.isPrayerActive(Rs2PrayerEnum.PIETY)&&Rs2Npc.getNpc(8061)!=null)
//            {
//                Rs2Prayer.toggleQuickPrayer(true);
//            }
//            else if (!Rs2Prayer.isPrayerActive(Rs2PrayerEnum.PIETY)&&Rs2Npc.getNpc(8058)!=null)
//            {
//                Rs2Prayer.toggleQuickPrayer(true);
//            }
//            else if (Rs2Prayer.isPrayerActive(Rs2PrayerEnum.PIETY)&&Rs2Npc.getNpc(8059)!=null)
//            {
//                    Rs2Prayer.toggleQuickPrayer(false);
//            }
//Rs2Bank.walkToBankAndUseBank();



                shutdown();
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