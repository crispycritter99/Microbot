package net.runelite.client.plugins.microbot.bloodmoons;
import net.runelite.api.*;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.widgets.Widget;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.bloodmoons.enums.State;
import net.runelite.client.plugins.microbot.util.camera.Rs2Camera;
import net.runelite.client.plugins.microbot.util.combat.Rs2Combat;
import net.runelite.client.plugins.microbot.util.gameobject.Rs2GameObject;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.magic.Rs2Magic;
import net.runelite.client.plugins.microbot.util.npc.Rs2Npc;
import net.runelite.client.plugins.microbot.util.npc.Rs2NpcModel;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;
import net.runelite.client.plugins.microbot.util.prayer.Rs2Prayer;
import net.runelite.client.plugins.microbot.util.prayer.Rs2PrayerEnum;
import net.runelite.client.plugins.microbot.util.walker.Rs2Walker;
import net.runelite.client.plugins.microbot.util.widget.Rs2Widget;
import net.runelite.client.plugins.skillcalculator.skills.MagicAction;

import java.math.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import static net.runelite.client.plugins.microbot.util.npc.Rs2Npc.getNpc;
import static net.runelite.client.plugins.microbot.util.npc.Rs2Npc.interact;

public class MoonsScript extends Script {
    public static State state = State.DEFAULT;
    public static State previousState = State.DEFAULT;

    private static final int bigFishingNet = 305;
    private static final int vialOfWater = 227;
    private static final int supplyBox = 51371;
    private static final int[] moonlightPotion = {29080, 29081, 29082, 29083};
    private static final int moonlightGrub = 29078;
    private static final int moonlightGrubPaste = 29079;
    private static final int pestleAndMortar = 233;
    private static final int grubbySapling = 51365;
    boolean lootable;
    private long lastEatTime = -1;
    public static long start_time = 0;
    public static int loots=0;
    private long lastPrayerTime = -1;
    private static final int EAT_COOLDOWN_MS = 2000;
    private static final int PRAYER_COOLDOWN_MS = 2000;
    public static WorldPoint closestTile=null;

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
    private static final List<WorldPoint> pointsjaguar = Arrays.asList(
            new WorldPoint(1390, 9636, 0),// top
            new WorldPoint(1391, 9636, 0),
            new WorldPoint(1393, 9636, 0),
            new WorldPoint(1394, 9636, 0),

            new WorldPoint(1396, 9634, 0),// right
            new WorldPoint(1396, 9633, 0),
            new WorldPoint(1396, 9631, 0),
            new WorldPoint(1396, 9630, 0),

            new WorldPoint(1394, 9628, 0),// bottom
            new WorldPoint(1393, 9628, 0),
            new WorldPoint(1391, 9628, 0),
            new WorldPoint(1390, 9628, 0),

            new WorldPoint(1388, 9630, 0),// left
            new WorldPoint(1388, 9631, 0),
            new WorldPoint(1388, 9633, 0),
            new WorldPoint(1388, 9634, 0)
    );

    public boolean run(MoonsConfig config) {
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

//                Rs2Camera.setZoom(128);

                WorldPoint playerLocation = Rs2Player.getWorldLocation();
                if (playerLocation.getX() <= topLeft.getX() && playerLocation.getX() >= bottomRight.getX()
                        && playerLocation.getY() <= topLeft.getY() && playerLocation.getY() >= bottomRight.getY()) {

                    state = State.FIGHTING;
                }
                else if (playerLocation.getX() <= topLeftChamber.getX() && playerLocation.getX() >= bottomRightChamber.getX()
                        && playerLocation.getY() <= topLeftChamber.getY() && playerLocation.getY() >= bottomRightChamber.getY()) {

                    state = State.CHAMBER;
                }
                else if (Objects.equals(Rs2Player.getWorldLocation(), new WorldPoint(1481, 9669, 0))) {
                    state = State.GOING_TO_COOKER;
                } else if (!Rs2GameObject.getGameObjects(supplyBox).isEmpty() && Rs2Inventory.itemQuantity(29217) < config.foodAmount() &&
                          playerLocation.getX() <= topLeftSupplies.getX() && playerLocation.getX() >= bottomRightSupplies.getX() &&
                          playerLocation.getY() <= topLeftSupplies.getY() && playerLocation.getY() >= bottomRightSupplies.getY()) {

                    state = State.GETTING_SUPPLIES;
                } else if (Objects.equals(Rs2Player.getWorldLocation(), new WorldPoint(1390, 9676, 0))) {
                    state = State.GOING_TO_BLOOD;
//                } else if (Objects.equals(Rs2Player.getWorldLocation(), new WorldPoint(1376, 9710, 0))) {

                } else if (Rs2Widget.isWidgetVisible(56950788)) {
                    state = State.GOING_TO_LOOT;
                    lootable=true;
                } else if (Objects.equals(Rs2Player.getWorldLocation(), new WorldPoint(1526, 9671, 0))) {
                    state = State.GETTING_SUPPLIES;
                }

                switch (state) {
                    case CHAMBER:
                        if (Rs2Player.isMoving() || Rs2Player.isInteracting()) break;

                        Rs2GameObject.interact(new WorldPoint(1458,9650,1),"Pass-through"); // Pass-though
                        break;
                    case GOING_TO_COOKER:
                        if (Rs2Player.isMoving() || Rs2Player.isInteracting()) break;

                        Rs2Walker.walkTo(new WorldPoint(1511, 9693, 0));
                        break;
                    case GETTING_SUPPLIES:
                        if (Rs2Player.isMoving() || Rs2Player.isInteracting()) break;
//                        if (Rs2GameObject.exists(51346)){
//                            Rs2GameObject.interact(new WorldPoint(1513,9598,0),"Pass-through");
//                            sleepUntil(()->!Rs2GameObject.exists(51346),10000);
//                        }
                        if (!Rs2GameObject.exists(1177)) {
                            Rs2Walker.walkTo(1512, 9695, 0);
                        }
                        if (Rs2Inventory.hasItem("Vial")) {
                            Rs2Inventory.dropAll("Vial");
                        }

                        if (Rs2Player.getAnimation() == 11042 && Rs2Inventory.hasItem(bigFishingNet)) { // catching fish
                            Rs2Inventory.drop(bigFishingNet);
                        }

                        if (Rs2Player.isAnimating()) break;

                        int moonlightOne = (int) Rs2Inventory.itemQuantity(moonlightPotion[0]);
                        int moonlightTwo = (int) Rs2Inventory.itemQuantity(moonlightPotion[1]);
                        int moonlightThree = (int) Rs2Inventory.itemQuantity(moonlightPotion[2]);
                        int moonlightFour = (int) Rs2Inventory.itemQuantity(moonlightPotion[3]);

                        int potionsTotal = (int) (moonlightOne + moonlightTwo + moonlightThree + moonlightFour + Rs2Inventory.itemQuantity(vialOfWater));

                        if (!Rs2Inventory.hasItem(bigFishingNet) && (Rs2Inventory.itemQuantity(29217) + Rs2Inventory.itemQuantity(29216)) < config.foodAmount()) { // potionsTotal < config.prayerAmount()
                            //Rs2GameObject.interact(supplyBox, "Take-from <col=00ffff>Fishing");
                            if (Rs2Player.getWorldLocation().getY() != 9695) {
                                Rs2Walker.walkFastCanvas(new WorldPoint(1512,9695,0));
                            }
//                            Rs2GameObject.interact()
                            Rs2GameObject.interact(51371,"Take-from Fishing");
                            sleep(600);
                        }

                        else if (potionsTotal < config.prayerAmount()) { // !Rs2Inventory.hasItem(moonlightPotion)
                            //Rs2GameObject.interact(supplyBox, "Take-from <col=00ffff>Herblore");
                            if (Rs2Player.getWorldLocation().getY() != 9695) {
                                Rs2Walker.walkFastCanvas(new WorldPoint(1512,9695,0));
                            }

                            Rs2GameObject.interact(51371,"Take-from Herblore");
//                            sleep(600);
                            Rs2Player.waitForWalking();
                        }
                        else if (Rs2Inventory.hasItemAmount(vialOfWater, config.prayerAmount() + 2)) {
                            Rs2Inventory.drop(vialOfWater);
                            Rs2Inventory.drop(vialOfWater);
                        }
                        else if (Rs2Inventory.itemQuantity(vialOfWater) >= (Rs2Inventory.itemQuantity(moonlightGrub) + Rs2Inventory.itemQuantity(moonlightGrubPaste)) && Rs2Inventory.hasItem(vialOfWater)&&!Rs2Inventory.isFull()) {
                            Rs2GameObject.interact(grubbySapling, "Collect-from");
                        }
                        else if (Rs2Inventory.itemQuantity(vialOfWater) != Rs2Inventory.itemQuantity(moonlightGrub) && Rs2Inventory.hasItem(vialOfWater) && Rs2Inventory.hasItem(moonlightGrub)) {
                            Rs2Inventory.combine(pestleAndMortar, moonlightGrub);
                            sleep(1200);
                        }
                        else if (Rs2Inventory.hasItem(moonlightGrubPaste)) {
                            if (!Rs2Inventory.hasItem(vialOfWater)) { Rs2Inventory.dropAll(moonlightGrubPaste); } else {
                                Rs2Inventory.combine(moonlightGrubPaste, vialOfWater);
                                sleepUntil(()->!Rs2Inventory.contains(moonlightGrubPaste));
                            }
                        } else if (Rs2Inventory.hasItemAmount("Moonlight potion", config.prayerAmount()) && Rs2Inventory.hasItem(pestleAndMortar)) {
                            Rs2Inventory.drop(pestleAndMortar);
                        } else if (Rs2Inventory.hasItemAmount("Moonlight potion", config.prayerAmount()) && Rs2Inventory.hasItem(bigFishingNet)) {
                            Rs2GameObject.interact(51367, "Fish"); // fishing spot
                        } else if (Rs2Inventory.isFull() && Rs2Inventory.hasItem(29216)) { //  && !Rs2Inventory.hasItem(29217)
                            Rs2GameObject.interact(51362, "Cook");

                            sleep(600);
                        } else if (Rs2Inventory.isFull() && Rs2Inventory.hasItem(29217) && !Rs2Inventory.hasItem(29216) && Microbot.getClient().getEnergy() != 10_000 && !Objects.equals(Rs2Player.getWorldLocation(), new WorldPoint(1522, 9718, 0))) {
                            Rs2GameObject.interact(51362, "Make-cuppa");
                        } else if (Rs2Inventory.isFull() && Rs2Inventory.hasItem(29217) && !Rs2Inventory.hasItem(29216) && Microbot.getClient().getEnergy() == 10_000) {

//                            Rs2Walker.walkFastCanvas(new WorldPoint(1522,9718,0));

                            Microbot.log("Done getting supplies, should be going through!");
                            state = State.GOING_TO_BLOOD;
//                        } else if (Rs2GameObject.interact(new WorldPoint(1522,9720,0),"Pass-through")) {
//                            sleep(1200);
//                            sleepUntil(()->!Rs2Player.isMoving());
                        }
                        break;
                    case GOING_TO_BLOOD:
//                        if (Objects.equals(Rs2Player.getWorldLocation(), new WorldPoint(1390, 9676, 0))) {
//                            Rs2GameObject.interact(new WorldPoint(1374,9665,0),"Pass-through");
//                        } else if (Objects.equals(Rs2Player.getWorldLocation(), new WorldPoint(1347, 9590, 0))) {
//                            Rs2GameObject.interact(new WorldPoint(1388,9589,0),"Pass-through");
//                        } else if (Objects.equals(Rs2Player.getWorldLocation(), new WorldPoint(1418, 9632, 0))) {
//                            Rs2GameObject.interact(51372,"Use");
//                        }
                        Rs2Walker.walkTo(1418,9632,0,8);
                        sleep(1200);
                        Rs2GameObject.interact(51372,"Use");
//                        sleep(1800);
                        sleepUntil(()->Rs2Npc.getNpcs("blood moon").count()!=0);
//                        Rs2Player.waitForWalking();
//                        state = State.FIGHTING;
                        break;
                    case FIGHTING:
                        Rs2NpcModel floorTileNPC = Rs2Npc.getNpc(13015);
                        WorldPoint floorTileLocation = (floorTileNPC != null) ? floorTileNPC.getWorldLocation() : null;

                       //WorldPoint playerLocation = Rs2Player.getWorldLocation();

                        if (floorTileLocation != null) {
                            closestTile = points.stream()
                                    .min(Comparator.comparingDouble(tile -> tile.distanceTo2D(floorTileLocation)))
                                    .orElse(null);

                            if (closestTile != null && Rs2Npc.getNpcs("blood jaguar").count()!=0) {
//                                Microbot.log("The closest tile to the centre is: " + closestTile);
//                                Microbot.log("Player location: " + playerLocation);
//                                if (Rs2Npc.getNpcs(13021).count()>1&&closestTile.distanceTo(Microbot.getClient().getLocalPlayer().getWorldLocation())<2){sleep(1800);}
//                                if (Rs2Npc.getNpcs(13021).count()>1&&Rs2Player.isInteracting()){sleep(1800);}
                                if (Rs2Npc.getNpcs("blood jaguar").count()==0||MoonsPlugin.ticks==1&&!Rs2Player.isMoving()) {
                                    Rs2Walker.walkFastCanvas(closestTile);
                                    if (Rs2Npc.getNpcs("blood jaguar").count()!=0){
                                        sleep(300);
                                        Rs2NpcModel jaguar=Rs2Npc.getNpcs("blood jaguar").sorted(Comparator.comparingInt((Rs2NpcModel npc) -> floorTileLocation.distanceTo(npc.getWorldLocation()))).findFirst().orElse(null);
                                        int index=points.indexOf(closestTile);
                                        WorldPoint closestJaguarTile = pointsjaguar.get(index);
//                                        WorldPoint closestJaguarTile = pointsjaguar.stream()
//                                                .min(Comparator.comparingDouble(tile -> tile.distanceTo2D(jaguar.getWorldLocation())))
//                                                .orElse(null);
//                                        floorTileNPC.getWorldLocation().distanceTo()
//                                        sleep(200);
                                        Rs2Walker.walkFastCanvas(closestJaguarTile);
                                        sleep(300);
                                        if (playerLocation.equals(closestJaguarTile)&&!Rs2Player.isInteracting()){
                                            Rs2Npc.interact(jaguar, "attack");
                                        }
                                    }
                                }

                            }
                            else if (closestTile != null && !playerLocation.equals(closestTile)&&!Rs2Player.isMoving()) {
//                                Microbot.log("The closest tile to the centre is: " + closestTile);
//                                Microbot.log("Player location: " + playerLocation);
//                                if (Rs2Npc.getNpcs(13021).count()>1&&closestTile.distanceTo(Microbot.getClient().getLocalPlayer().getWorldLocation())<2){sleep(1800);}
//                                if (Rs2Npc.getNpcs(13021).count()>1&&Rs2Player.isInteracting()){sleep(1800);}
                                if (Rs2Npc.getNpcs("blood jaguar").count()==0||MoonsPlugin.ticks==1&&!Rs2Player.isMoving()) {
                                    Rs2Walker.walkFastCanvas(closestTile);
                                    if (Rs2Npc.getNpcs("blood jaguar").count()!=0){
                                        sleep(300);
                                        Rs2NpcModel jaguar=Rs2Npc.getNpcs("blood jaguar").sorted(Comparator.comparingInt((Rs2NpcModel npc) -> floorTileLocation.distanceTo(npc.getWorldLocation()))).findFirst().orElse(null);
                                        WorldPoint closestJaguarTile = pointsjaguar.stream()
                                                .min(Comparator.comparingDouble(tile -> tile.distanceTo2D(jaguar.getWorldLocation())))
                                                .orElse(null);
//                                        floorTileNPC.getWorldLocation().distanceTo()
//                                        sleep(200);
//                                        Rs2Walker.walkFastCanvas(closestJaguarTile);
//                                        sleep(300);
                                        Rs2Npc.interact(jaguar, "attack");
                                    }
                                }

                            }


                        }

                        List<WorldPoint> bloodSpotsWorldPoints = Rs2GameObject.getGameObjects(51046)
                                .stream()
                                .map(GameObject::getWorldLocation)
                                .collect(Collectors.toList());

                        if (!bloodSpotsWorldPoints.isEmpty() && Rs2Npc.getNpc(13021) == null) { //avoiding blood spot phase
                            if (bloodSpotsWorldPoints.contains(playerLocation)) {
//                                Microbot.log("Player In Blood Spot");

                                final WorldPoint safeTile = findSafeTile(new WorldPoint(1393, 9635, 0), bloodSpotsWorldPoints);
                                if (safeTile != null) {
                                    Rs2Walker.walkFastCanvas(safeTile);
//                                    Microbot.log("Dodging dangerous area, moving to safe tile at: " + safeTile);
                                }
                            }
                        }

                        int maxEat = 50;
                        int maxPrayer = 50;
//                        Microbot.log(""+ Rs2Magic.canCast(MagicAction.RESURRECT_GREATER_THRALL));
                        if(!Rs2Magic.isThrallActive()&&Rs2Inventory.hasItem("Book of the dead")&&Rs2Npc.getNpcs("blood moon").count()!=0) {
                            Rs2Magic.cast(MagicAction.RESURRECT_GREATER_GHOST);
                        }
                        int currentHitpoints = Microbot.getClient().getBoostedSkillLevel(Skill.HITPOINTS);
                        if (currentTime - lastEatTime > EAT_COOLDOWN_MS && currentHitpoints <= maxEat) {
                            Rs2Player.useFood();
                            if (Rs2Npc.getNpc(13011)!=null&&Rs2Npc.getNpcs("blood jaguar").count()==0) {
                                Rs2Inventory.wear(4151);
                                Rs2Inventory.wear(12954  );
                                //Rs2Npc.interact(npcToAttack, "attack");
                                attackBoss("Blood moon");
                            }
                            lastEatTime = currentTime;
                            Microbot.log("Eating food at " + maxEat + "% health.");
                        }

                        int currentPrayerPoints = Microbot.getClient().getBoostedSkillLevel(Skill.PRAYER);
//                        if (currentTime - lastPrayerTime > PRAYER_COOLDOWN_MS && currentPrayerPoints <= maxPrayer&&Rs2Npc.getNpcs("blood jaguar").count()==0) {
                            if (Rs2Npc.getNpcs("blood jaguar").count()==0) {
                                if (Rs2Player.drinkPrayerPotion()) {
                                    if (Rs2Npc.getNpc(13011) != null) {
                                        Rs2Inventory.wear(4151);
                                        Rs2Inventory.wear(12954);
                                        //Rs2Npc.interact(npcToAttack, "attack");
//                                        attackBoss("Blood moon");
                                    }
                                }
//                            lastPrayerTime = currentTime;
//                            Microbot.log("Drinking prayer potion at " + maxPrayer + "% prayer points.");
                        }

                        boolean dangerZone = !Rs2GameObject.getGameObjects(51054).isEmpty();
                        if (!Rs2Prayer.isQuickPrayerEnabled() && Rs2Player.hasPrayerPoints() && !dangerZone) {
                            Rs2Prayer.toggleQuickPrayer(true);
//                            Rs2Prayer.toggle(Rs2PrayerEnum.PIETY,true);
                        } else if (dangerZone) {
                            Rs2Prayer.toggleQuickPrayer(false);
//                            Rs2Prayer.toggle(Rs2PrayerEnum.PIETY,false);
                        }

                        if (!Rs2Player.isMoving() && !Rs2Player.isInteracting()) {
                            NPC npc13021 = Rs2Npc.getNpc(13021);
                            int npcToAttack = (npc13021 == null) ? 13011 : 13021;

                            //Microbot.log("Spec energy = " + Rs2Combat.getSpecEnergy());

                            if (npcToAttack == 13011 && Rs2Combat.getSpecEnergy() >= 500&&Rs2Inventory.hasItem("dragon claws")) {
                                Rs2Inventory.wear(13652); // d claws
                                Rs2Combat.setSpecState(true);
//                                Microbot.log(""+ Rs2Magic.canCast(MagicAction.RESURRECT_SUPERIOR_ZOMBIE));
                                //Rs2Npc.interact(npcToAttack, "attack");
                                attackBoss("Blood moon");
                            } else if (playerLocation.equals(closestTile)&&npcToAttack == 13011&&Rs2Npc.getNpcs("blood jaguar").count()==0&&bloodSpotsWorldPoints.isEmpty()) {
//                                Rs2Inventory.wear(4151);
//                                Rs2Inventory.wear(12954  );
                                Rs2Inventory.wield(29796);
                                //Rs2Npc.interact(npcToAttack, "attack");

                                attackBoss("Blood moon");
                            } else{
//                                Rs2Inventory.wear(4151  );
//                                Rs2Inventory.wear(12954  );
                                Rs2Inventory.wear(29796);
                                attackBoss("Blood jaguar");
                            }
                        }

                        break;
                    case GOING_TO_LOOT:
                        Rs2Prayer.toggleQuickPrayer(false);
//                        Rs2Prayer.toggle(Rs2PrayerEnum.PIETY,false);
//                        if (Rs2Widget.getWidget(56950788) != null)
                        if (Objects.equals(Rs2Player.getWorldLocation(), new WorldPoint(1376, 9710, 0))) {
                            Rs2GameObject.interact(51362, "Make-cuppa");
                            sleepUntil(() -> Microbot.getClient().getEnergy() == 10_000);
                        }
//                            sleep(1200);

//                            Rs2GameObject.interact(new WorldPoint(1374,9665,0),"Pass-through");
//                            sleep(1200);
//                        } else if (Objects.equals(Rs2Player.getWorldLocation(), new WorldPoint(1347, 9590, 0))) {
//                            Rs2GameObject.interact(51362, "Make-cuppa");
//
//                        } else if (Objects.equals(Rs2Player.getWorldLocation(), new WorldPoint(1350, 9582, 0)) && Microbot.getClient().getEnergy() == 10_000) {
//                            Rs2GameObject.interact(new WorldPoint(1356,9536,0),"Pass-through");
//                        }
//                        else if (Rs2Player.distanceTo(new WorldPoint(1513, 9563, 0))<10) {
                            Rs2Walker.walkTo(1513,9580,0,8);
                        if (Rs2GameObject.exists(51346)&&Rs2Widget.isWidgetVisible(56950788)) {

                            Rs2GameObject.interact(51346,"Claim");
                            sleepUntil(()->Rs2Widget.getWidget(56885268) != null,5000);
                            Widget widget = Rs2Widget.getWidget(56885268);
                            if (widget == null) return;

                            Microbot.getMouse().click(widget.getBounds());
                            loots++;
                            sleep(1200);
                        } else if (!Rs2Widget.isWidgetVisible(56950788)) {


                            moonlightOne = (int) Rs2Inventory.itemQuantity(moonlightPotion[0]);
                            moonlightTwo = (int) Rs2Inventory.itemQuantity(moonlightPotion[1]);
                            moonlightThree = (int) Rs2Inventory.itemQuantity(moonlightPotion[2]);
                            moonlightFour = (int) Rs2Inventory.itemQuantity(moonlightPotion[3]);

                            potionsTotal = moonlightOne + moonlightTwo + moonlightThree + moonlightFour;

                            if (Rs2Inventory.itemQuantity("Cooked bream") >= config.foodAmount() && potionsTotal >= 1) {
                                state = State.GOING_BACK_TO_BLOOD;
                                lootable =false;
                            } else {
                                state = State.GETTING_SUPPLIES;
                                lootable =false;
                            }
                        }

                        break;
                    case GOING_BACK_TO_BLOOD:

//                        if(!Rs2GameObject.exists(51372)) {
//                            Rs2Walker.walkTo(1350, 9582, 0, 2);
//                            Rs2GameObject.interact(51362, "Make-cuppa");
//                            sleepUntil(() -> Microbot.getClient().getEnergy() == 10_000);
//                        }
                        Rs2Walker.walkTo(1418,9632,0,8);
                        sleep(1200);
                        Rs2GameObject.interact(51372,"Use");
                        sleepUntil(()->Rs2Npc.getNpcs("blood moon").count()!=0);
                        break;
                }

                long endTime = System.currentTimeMillis();
                long totalTime = endTime - startTime;
                System.out.println("Total time for loop " + totalTime);
            } catch (Exception e) {
                Microbot.log("Error: " + e);
            }
        }, 0, 10, TimeUnit.MILLISECONDS);
        return true;
    }

    public static void attackBoss(String npcName) {
        attackBosser(Collections.singletonList(npcName));

    }
    public static void attackBosser(List<String> npcNames) {
        for (String npcName : npcNames) {
            Rs2NpcModel npc = getNpc(npcName);
            if (npc == null) continue;
            Microbot.log("Sending attack interaction.");
            Rs2Npc.interact(npc, "attack");
            return;
        }
    }

    private WorldPoint findSafeTile(WorldPoint playerLocation, List<WorldPoint> dangerousWorldPoints) {
        List<WorldPoint> nearbyTiles = List.of(
                new WorldPoint(playerLocation.getX() + 1, playerLocation.getY(), playerLocation.getPlane()),// normal
                new WorldPoint(playerLocation.getX() - 1, playerLocation.getY(), playerLocation.getPlane()),
                new WorldPoint(playerLocation.getX(), playerLocation.getY() + 1, playerLocation.getPlane()),
                new WorldPoint(playerLocation.getX(), playerLocation.getY() - 1, playerLocation.getPlane()),

                new WorldPoint(playerLocation.getX() - 1, playerLocation.getY() + 1, playerLocation.getPlane()),// diagonal
                new WorldPoint(playerLocation.getX() - 1, playerLocation.getY() - 1, playerLocation.getPlane()),
                new WorldPoint(playerLocation.getX() + 1, playerLocation.getY() - 1, playerLocation.getPlane()),
                new WorldPoint(playerLocation.getX() + 1, playerLocation.getY() + 1, playerLocation.getPlane())
        );

        for (WorldPoint tile : nearbyTiles) {
            if (!dangerousWorldPoints.contains(tile)) {
                Microbot.log("Found safe tile: " + tile);
                return tile;
            }
        }
        Microbot.log("No safe tile found!");
        return null;
    }

    private static void reset() {
        state = State.DEFAULT;
        previousState = State.DEFAULT;
    }
    public void startup() {
        loots=0;
        start_time=System.currentTimeMillis();
    }
    @Override
    public void shutdown() {
        super.shutdown();
        reset();
        Rs2Prayer.disableAllPrayers();
    }
}
