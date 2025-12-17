package net.runelite.client.plugins.microbot.SulphurNagua.combat;

import net.runelite.api.EquipmentInventorySlot;
import net.runelite.api.Skill;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.SulphurNagua.SulphurNaguaConfig;
import net.runelite.client.plugins.microbot.util.equipment.Rs2Equipment;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.inventory.Rs2ItemModel;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static net.runelite.client.plugins.microbot.util.equipment.Rs2Equipment.get;

public class FoodScript extends Script {

    String weaponname = "";
    String bodyName = "";
    String legsName = "";
    String helmName = "";

    String shieldName = "";

    public boolean run(SulphurNaguaConfig config) {
        weaponname = "";
        bodyName = "";
        legsName = "";
        helmName = "";
        shieldName = "";
        mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> {
            try {
                if (!Microbot.isLoggedIn()) return;
                if (!super.run()) return;
                if (!config.toggleFood()) return;
                if (Rs2Inventory.hasItem("empty vial"))
                    Rs2Inventory.drop("empty vial");
                double treshHold = (double) (Microbot.getClient().getBoostedSkillLevel(Skill.HITPOINTS) * 100) / Microbot.getClient().getRealSkillLevel(Skill.HITPOINTS);

                Rs2Player.eatAt(50);

            } catch(Exception ex) {
                System.out.println(ex.getMessage());
            }
        }, 0, 600, TimeUnit.MILLISECONDS);
        return true;
    }




}
