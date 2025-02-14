package net.runelite.client.plugins.microbot.SulphurNagua;

import net.runelite.api.GameObject;
import net.runelite.api.NPC;
import net.runelite.api.Skill;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.widgets.Widget;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.SulphurNagua.enums.State;
import net.runelite.client.plugins.microbot.aiofighter.AIOFighterPlugin;
import net.runelite.client.plugins.microbot.util.camera.Rs2Camera;
import net.runelite.client.plugins.microbot.util.combat.Rs2Combat;
import net.runelite.client.plugins.microbot.util.gameobject.Rs2GameObject;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.math.Rs2Random;
import net.runelite.client.plugins.microbot.util.npc.Rs2Npc;
import net.runelite.client.plugins.microbot.util.npc.Rs2NpcModel;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;
import net.runelite.client.plugins.microbot.util.prayer.Rs2Prayer;
import net.runelite.client.plugins.microbot.util.walker.Rs2Walker;
import net.runelite.client.plugins.microbot.util.widget.Rs2Widget;

import java.util.List;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static net.runelite.client.plugins.microbot.util.npc.Rs2Npc.getNpc;
import static net.runelite.client.plugins.microbot.util.npc.Rs2Npc.interact;

public class SulphurNaguaScript extends Script {
    public static State state = State.DEFAULT;
    public static State previousState = State.DEFAULT;
    public static List<Rs2NpcModel> attackableNpcs = new ArrayList<>();
    private static final int bigFishingNet = 305;
    private static final int vialOfWater = 227;
    private static final int supplyBox = 51371;
    private static final int[] moonlightPotion = {29080, 29081, 29082, 29083};
    private static final int moonlightGrub = 29078;
    private static final int moonlightGrubPaste = 29079;
    private static final int pestleAndMortar = 233;
    private static final int grubbySapling = 51365;

    private long lastEatTime = -1;
    private long lastPrayerTime = -1;
    private static final int EAT_COOLDOWN_MS = 2000;
    private static final int PRAYER_COOLDOWN_MS = 2000;
          WorldPoint  centerLocation = new WorldPoint(1356, 9570, 0);
    private static final List<WorldPoint> points = Arrays.asList(
            new WorldPoint(1390, 9635, 0),// top
            new WorldPoint(1391, 9635, 0),
            new WorldPoint(1393, 9635, 0),
            new WorldPoint(1394, 9635, 0),

            new WorldPoint(1395, 9634, 0),// right
            new WorldPoint(1395, 9633, 0),
            new WorldPoint(1395, 9631, 0),
            new WorldPoint(1395, 9630, 0),

            new WorldPoint(1394, 9629, 0),// bottom
            new WorldPoint(1393, 9629, 0),
            new WorldPoint(1391, 9629, 0),
            new WorldPoint(1390, 9629, 0),

            new WorldPoint(1389, 9630, 0),// left
            new WorldPoint(1389, 9631, 0),
            new WorldPoint(1389, 9633, 0),
            new WorldPoint(1389, 9634, 0)
    );

    public boolean run(SulphurNaguaConfig config) {
        state = config.getState();
        Microbot.enableAutoRunOn = true;
        mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> {
            try {
                if (!Microbot.isLoggedIn()) return;
                if (!super.run()) return;
                long startTime = System.currentTimeMillis();
                long currentTime = System.currentTimeMillis();

                if (state != previousState) {
                    Microbot.log("State changed to: " + state);
                    previousState = state;
                }

                WorldPoint topLeft = new WorldPoint(1400, 9640, 0);
                WorldPoint bottomRight = new WorldPoint(1384, 9624, 0);

                WorldPoint topLeftChamber = new WorldPoint(1439, 9622, 0);
                WorldPoint bottomRightChamber = new WorldPoint(1441, 9615, 0);

                WorldPoint topLeftSupplies = new WorldPoint(1510, 9695, 0);
                WorldPoint bottomRightSupplies = new WorldPoint(1520, 9688, 0);

                Rs2Camera.setZoom(128);

                WorldPoint playerLocation = Rs2Player.getWorldLocation();
                if (playerLocation.getX() <= topLeft.getX() && playerLocation.getX() >= bottomRight.getX()
                        && playerLocation.getY() <= topLeft.getY() && playerLocation.getY() >= bottomRight.getY()) {

                    state = State.FIGHTING;
                }
                else if (playerLocation.getX() <= topLeftChamber.getX() && playerLocation.getX() >= bottomRightChamber.getX()
                        && playerLocation.getY() <= topLeftChamber.getY() && playerLocation.getY() >= bottomRightChamber.getY()) {

                    state = State.CHAMBER;
                }else if (!Rs2Inventory.contains("moonlight potion") && state==State.FIGHTING) {

                    state = State.GOING_TO_COOKER;
                } else if (Objects.equals(Rs2Player.getWorldLocation(), new WorldPoint(1390, 9676, 0))) {
                    state = State.GOING_TO_NAGUA;
                } else if (Objects.equals(Rs2Player.getWorldLocation(), new WorldPoint(1376, 9710, 0))) {
                    state = State.GOING_TO_LOOT;
                } else if (Objects.equals(Rs2Player.getWorldLocation(), new WorldPoint(1526, 9671, 0))) {
                    state = State.GETTING_SUPPLIES;
                }

                switch (state) {
                    case CHAMBER:
                        if (Rs2Player.isWalking() || Rs2Player.isInteracting()) break;

                        Rs2GameObject.interact(new WorldPoint(1458, 9650, 1), "Pass-through"); // Pass-though
                        break;
                    case GETTING_SUPPLIES:
                        if (Rs2Player.isWalking() || Rs2Player.isInteracting()) break;

                        if (Rs2Inventory.hasItem("Vial")) {
                            Rs2Inventory.dropAll("Vial");
                        }


                        if (Rs2Player.isAnimating()) break;

                        int moonlightOne = (int) Rs2Inventory.itemQuantity(moonlightPotion[0]);
                        int moonlightTwo = (int) Rs2Inventory.itemQuantity(moonlightPotion[1]);
                        int moonlightThree = (int) Rs2Inventory.itemQuantity(moonlightPotion[2]);
                        int moonlightFour = (int) Rs2Inventory.itemQuantity(moonlightPotion[3]);

                        int potionsTotal = (int) (moonlightOne + moonlightTwo + moonlightThree + moonlightFour + Rs2Inventory.itemQuantity(vialOfWater));


                        if (potionsTotal < 20) { // !Rs2Inventory.hasItem(moonlightPotion)
                            //Rs2GameObject.interact(supplyBox, "Take-from <col=00ffff>Herblore");
                            if (Rs2Player.getWorldLocation().getY() != 9695) {
                                Rs2Walker.walkFastCanvas(new WorldPoint(1512, 9695, 0));
                            }

                            Rs2GameObject.interact(51371, "Take-from Herblore");
                            sleep(600);
                        } else if (Rs2Inventory.hasItemAmount(vialOfWater, config.prayerAmount() + 2)) {
                            Rs2Inventory.drop(vialOfWater);
                            Rs2Inventory.drop(vialOfWater);
                        } else if (Rs2Inventory.itemQuantity(vialOfWater) >= (Rs2Inventory.itemQuantity(moonlightGrub) + Rs2Inventory.itemQuantity(moonlightGrubPaste)) && Rs2Inventory.hasItem(vialOfWater)) {
                            Rs2GameObject.interact(grubbySapling, "Collect-from");
                        } else if (Rs2Inventory.itemQuantity(vialOfWater) != Rs2Inventory.itemQuantity(moonlightGrub) && Rs2Inventory.hasItem(vialOfWater) && Rs2Inventory.hasItem(moonlightGrub)) {
                            Rs2Inventory.combine(pestleAndMortar, moonlightGrub);
                        } else if (Rs2Inventory.hasItem(moonlightGrubPaste)) {
                            if (!Rs2Inventory.hasItem(vialOfWater)) {
                                Rs2Inventory.dropAll(moonlightGrubPaste);
                            } else {
                                Rs2Inventory.combine(moonlightGrubPaste, vialOfWater);
                            }
                        } else if (Rs2Inventory.hasItemAmount("Moonlight potion", config.prayerAmount()) && Rs2Inventory.hasItem(pestleAndMortar)) {
                            Rs2Inventory.drop(pestleAndMortar);
                        } else if (Rs2Inventory.isFull() && Microbot.getClient().getEnergy() != 10_000 && !Objects.equals(Rs2Player.getWorldLocation(), new WorldPoint(1522, 9718, 0))) {
                            Rs2GameObject.interact(51362, "Make-cuppa");
                        } else if (Rs2Inventory.isFull() && Microbot.getClient().getEnergy() == 10_000) {
                            Rs2Walker.walkFastCanvas(new WorldPoint(1522, 9718, 0));

                            Microbot.log("Done getting supplies, should be going through!");
                        } else if (Objects.equals(Rs2Player.getWorldLocation(), new WorldPoint(1522, 9718, 0))) {
                            Rs2GameObject.interact(new WorldPoint(1522, 9720, 0), "Pass-through");
                        }
                        break;
                    case GOING_TO_NAGUA:
                        if (Objects.equals(Rs2Player.getWorldLocation(), new WorldPoint(1390, 9676, 0))) {
                            Rs2GameObject.interact(new WorldPoint(1374, 9665, 0), "Pass-through");
                        } else if (Objects.equals(Rs2Player.getWorldLocation(), new WorldPoint(1347, 9590, 0))) {
                            Rs2GameObject.interact(new WorldPoint(1388, 9589, 0), "Pass-through");
                        } else if (Objects.equals(Rs2Player.getWorldLocation(), new WorldPoint(1418, 9632, 0))) {
                            Rs2GameObject.interact(51372, "Use");
                        }
                        break;
                    case FIGHTING:
                        List<String> npcsToAttack = Arrays.stream(config.attackableNpcs().split(","))
                                .map(x -> x.trim().toLowerCase())
                                .collect(Collectors.toList());
                        Rs2Player.drinkPrayerPotionAt(Rs2Random.nextInt(10, 20, 1, true));

                        if (!Rs2Player.isWalking() && !Rs2Combat.inCombat()) {

                        //Microbot.log("Spec energy = " + Rs2Combat.getSpecEnergy());

                        attackableNpcs = Rs2Npc.getAttackableNpcs(config.attackReachableNpcs())
                                .filter(npc -> npc.getWorldLocation().distanceTo(centerLocation) <= config.attackRadius() && npcsToAttack.contains("suphur nagua"))
                                .sorted(Comparator.comparingInt((Rs2NpcModel npc) -> npc.getInteracting() == Microbot.getClient().getLocalPlayer() ? 0 : 1)
                                        .thenComparingInt(npc -> Rs2Player.getRs2WorldPoint().distanceToPath(npc.getWorldLocation())))
                                .collect(Collectors.toList());


                        if (!attackableNpcs.isEmpty()) {
                            Rs2NpcModel npc = attackableNpcs.stream().findFirst().orElse(null);

                            if (!Rs2Camera.isTileOnScreen(npc.getLocalLocation()))
                                Rs2Camera.turnTo(npc);

                            Rs2Npc.interact(npc, "attack");
                            Microbot.status = "Attacking " + npc.getName();
                            SulphurNaguaPlugin.setCooldown(config.playStyle().getRandomTickInterval());
                            sleepUntil(Rs2Player::isInteracting, 1000);
                        }
                }
                        break;
                }

                long endTime = System.currentTimeMillis();
                long totalTime = endTime - startTime;
                System.out.println("Total time for loop " + totalTime);
            } catch (Exception e) {
                Microbot.log("Error: " + e);
            }
        }, 0, 200, TimeUnit.MILLISECONDS);
        return true;
    }


    @Override
    public void shutdown() {
        super.shutdown();
        Rs2Prayer.disableAllPrayers();
    }
}
