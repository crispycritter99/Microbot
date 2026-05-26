package net.runelite.client.plugins.microbot.caviarmixer;

import net.runelite.api.NPC;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.api.tileobject.Rs2TileObjectCache;
import net.runelite.client.plugins.microbot.util.antiban.Rs2Antiban;
import net.runelite.client.plugins.microbot.util.bank.Rs2Bank;
import net.runelite.client.plugins.microbot.util.gameobject.Rs2GameObject;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.inventory.Rs2ItemModel;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;

import javax.inject.Inject;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory.items;


public class CaviarMixerScript extends Script {
    public static boolean tentacle = false;
    NPC vorkath;
    private WorldPoint workingTile = null;
    @Inject
    Rs2TileObjectCache rs2TileObjectCache;
    public static boolean lootnet = false;
     boolean test = false;
    public boolean run(CaviarMixerConfig config) {
        Microbot.enableAutoRunOn = false;

        mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> {
            try {
                if (!Microbot.isLoggedIn()) return;
                if (!super.run()) return;
                long startTime = System.currentTimeMillis();
                String potion = "Super defence(2)";
                // Bank
                if (!Rs2Inventory.contains("caviar")||!Rs2Inventory.contains(potion)){
                    Rs2Antiban.takeMicroBreakByChance();
                    Rs2Bank.openBank();
                    sleepUntil(Rs2Bank::isOpen);
                    Rs2Bank.depositAll();
                    Rs2Bank.withdrawX("caviar",13);
                    Rs2Bank.withdrawX(potion,13);
                    Rs2Bank.withdrawOne("caviar");
                    Rs2Bank.withdrawOne(potion);
                    Rs2Inventory.waitForInventoryChanges(1800);
                    Rs2Bank.closeBank();
                    sleepUntil(Rs2Inventory::isFull);
                }
                // Mix Potions
                if (Rs2Inventory.contains("caviar")&&Rs2Inventory.contains(potion)) {
                    Rs2ItemModel lastCaviar = Rs2Inventory.getLast("caviar");
                    Rs2ItemModel lastPotion = Rs2Inventory.getLast(potion);
                    List<Rs2ItemModel> itemsToDrop = items(Rs2ItemModel.matches(true, "Caviar"))
                            .collect(Collectors.toList());
                    int numberOfCaviar = Math.min(
                            Rs2Inventory.count("Caviar"),
                            Rs2Inventory.count(potion)
                    );
                    for (int i = 0; i < numberOfCaviar; i++) {
                        if (!Rs2Inventory.contains("caviar")||!Rs2Inventory.contains(potion)) continue;
                        Rs2Inventory.interact(lastCaviar,"use");
                        Rs2Inventory.interact(lastPotion,"use");
                                        double LOG_MEAN = 0.05; double LOG_STD = 0.34;Random r = new Random();double gaussian = r.nextGaussian();
                double value = Math.exp(LOG_MEAN + LOG_STD * gaussian);
                sleep((int) value*10);
                    }
                    Rs2Inventory.waitForInventoryChanges(1800);
                }

                long endTime = System.currentTimeMillis();
                long totalTime = endTime - startTime;
                System.out.println("Total time for loop " + totalTime);

            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
        }, 0, 100, TimeUnit.MILLISECONDS);
        return true;
    }

    @Override
    public void shutdown() {
        super.shutdown();
    }

}