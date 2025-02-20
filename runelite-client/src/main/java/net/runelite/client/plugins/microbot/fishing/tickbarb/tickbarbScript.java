package net.runelite.client.plugins.microbot.fishing.tickbarb;

import net.runelite.api.NPC;
import net.runelite.api.Skill;
import net.runelite.client.game.FishingSpot;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.util.antiban.Rs2Antiban;
import net.runelite.client.plugins.microbot.util.antiban.Rs2AntibanSettings;
import net.runelite.client.plugins.microbot.util.camera.Rs2Camera;
import net.runelite.client.plugins.microbot.util.inventory.InteractOrder;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.inventory.Rs2ItemModel;
import net.runelite.client.plugins.microbot.util.npc.Rs2Npc;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;

import java.util.concurrent.TimeUnit;

import static net.runelite.client.plugins.microbot.util.npc.Rs2Npc.validateInteractable;

public class tickbarbScript extends Script {

    public static String version = "1.1.3";
    public static int timeout = 0;
    private tickbarbConfig config;

    public boolean run(tickbarbConfig config) {
        this.config = config;
        Rs2Antiban.resetAntibanSettings();
        Rs2Antiban.antibanSetupTemplates.applyFishingSetup();
        Rs2AntibanSettings.naturalMouse = false;
        mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> {
            Microbot.log("-1");
            if (!super.run() || !Microbot.isLoggedIn() || !Rs2Inventory.hasItem("feather") || !Rs2Inventory.hasItem("rod")) {
                return;
            }

            if (Rs2AntibanSettings.actionCooldownActive) return;


//            if (Rs2Player.isInteracting())
//                return;

            if (Rs2Inventory.isFull()) {
                dropInventoryItems(config);
                return;
            }
            Microbot.log("0");

                var fishingspot = findFishingSpot();
                if (fishingspot == null) {
                    return;
                }
                Microbot.log("1");
                if (!Rs2Camera.isTileOnScreen(fishingspot.getLocalLocation())) {
                    validateInteractable(fishingspot);
                }
            Microbot.log("2");

            if (Rs2Npc.interact(fishingspot)){
                Microbot.log("3");


                int skillExp = Microbot.getClient().getSkillExperience(Skill.FISHING);

                Rs2Inventory.use("teak logs");
                Rs2ItemModel knife = Rs2Inventory.get("knife");
                Rs2Inventory.hover(knife);
//                while(Microbot.getClient().getSkillExperience(Skill.FISHING)==skillExp){sleep(50);}

                sleepUntil(() -> Microbot.getClient().getSkillExperience(Skill.FISHING) != skillExp, 1800);
                Rs2Inventory.use(knife);
//                Rs2Inventory.dropAll("leaping trout", "leaping sturgeon", "leaping salmon");
//                Rs2Inventory.slotInteract(0,"drop");
                Rs2Inventory.drop("leaping",false);
                Microbot.log("4");
//                Rs2Antiban.actionCooldown();
//                Rs2Antiban.takeMicroBreakByChance();
            }

        }, 0, 100, TimeUnit.MILLISECONDS);
        return true;
    }

    public void onGameTick() {

    }

    private NPC findFishingSpot() {
        for (int fishingSpotId : FishingSpot.BARB_FISH.getIds()) {
            NPC fishingspot = Rs2Npc.getNpc(fishingSpotId);
            if (fishingspot != null) {
                return fishingspot;
            }
        }
        return null;
    }

    private void dropInventoryItems(tickbarbConfig config) {
        InteractOrder dropOrder = config.dropOrder() == InteractOrder.RANDOM ? InteractOrder.random() : config.dropOrder();
        Rs2Inventory.dropAll(x -> x.name.toLowerCase().contains("leaping"), dropOrder);
    }

    public void shutdown() {
        Rs2Antiban.resetAntibanSettings();
        super.shutdown();
    }
}