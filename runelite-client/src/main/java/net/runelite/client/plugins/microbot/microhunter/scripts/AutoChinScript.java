package net.runelite.client.plugins.microbot.microhunter.scripts;

import net.runelite.api.GameObject;
import net.runelite.api.ItemID;
import net.runelite.api.NPC;
import net.runelite.api.ObjectID;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.game.npcoverlay.HighlightedNpc;
import net.runelite.client.plugins.hunter.HunterPlugin;
import net.runelite.client.plugins.hunter.HunterTrap;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.api.tileobject.Rs2TileObjectCache;
import net.runelite.client.plugins.microbot.api.tileobject.models.Rs2TileObjectModel;
import net.runelite.client.plugins.microbot.breakhandler.BreakHandlerScript;
import net.runelite.client.plugins.microbot.microhunter.AutoHunterConfig;
import net.runelite.client.plugins.microbot.microhunter.AutoHunterLocalPlugin;
import net.runelite.client.plugins.microbot.util.dialogues.Rs2Dialogue;
import net.runelite.client.plugins.microbot.util.gameobject.Rs2GameObject;
import net.runelite.client.plugins.microbot.util.grounditem.Rs2GroundItem;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.npc.Rs2Npc;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;
import net.runelite.client.plugins.microbot.util.walker.Rs2Walker;
import net.runelite.client.plugins.hunter.HunterPlugin.*;
import java.util.stream.Collectors;
import javax.inject.Inject;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.TimeUnit;

enum State {
    IDLE,
    CATCHING,
    DROPPING,
    LAYING,
}


public class AutoChinScript extends Script {
    Map<WorldPoint, HunterTrap> traplist;
    public static boolean test = false;
    private boolean oneRun = false;
    private List<WorldPoint> boxtiles = new ArrayList<>();
    private List<Integer> trapIds = Arrays.asList(
            ItemID.BOX_TRAP,
            ObjectID.BOX_TRAP,
            ObjectID.BOX_TRAP_9385,
            ObjectID.BOX_TRAP_9380,
            ObjectID.SHAKING_BOX_9384,
            ObjectID.SHAKING_BOX_9383,
            ObjectID.SHAKING_BOX_9382,
            ObjectID.SHAKING_BOX
    );

    private List<Integer> shouldResetIds = Arrays.asList(
            ObjectID.BOX_TRAP_9385,
            ObjectID.SHAKING_BOX_9384,
            ObjectID.SHAKING_BOX_9383,
            ObjectID.SHAKING_BOX_9382
    );

    State currentState = State.IDLE;
    @Inject
    Rs2TileObjectCache rs2TileObjectCache;
    public boolean run(AutoHunterConfig config) {
        Microbot.enableAutoRunOn = false;
        mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> {
            try {
                if (!Microbot.isLoggedIn()) return;
                if (!super.run()) return;
                long startTime = System.currentTimeMillis();
                if (Rs2Player.distanceTo(new WorldPoint(3259,2377,0))>30){
                    Rs2Walker.walkTo(new WorldPoint(3259,2377,0));
                     int x=3258;
                     int y=2377;
                    sleep(2400);
                    Rs2Walker.walkFastCanvas(new WorldPoint(x,y,0));
                    sleep(2400);
                    Rs2Inventory.interact("box trap","lay");
                    sleepUntilTick(9);
                    Rs2Walker.walkFastCanvas(new WorldPoint(x+1,y+1,0));
                    sleep(2400);
                    Rs2Inventory.interact("box trap","lay");
                    sleepUntilTick(9);
                    Rs2Walker.walkFastCanvas(new WorldPoint(x-1,y+1,0));
                    sleep(2400);
                    Rs2Inventory.interact("box trap","lay");
                    sleepUntilTick(9);
                    Rs2Walker.walkFastCanvas(new WorldPoint(x-1,y-1,0));
                    sleep(2400);
                    Rs2Inventory.interact("box trap","lay");
                    sleepUntilTick(9);
//                    Rs2Walker.walkFastCanvas(new WorldPoint(x+1,y-1,0));
//                    sleep(2400);
//                    Rs2Inventory.interact("box trap","lay");
//                    sleepUntilTick(9);
                };
                switch (currentState) {
                    case IDLE:
                        handleBreaks();
                        handleIdleState();
                        break;
                    case DROPPING:
                        handleBreaks();
                        handleDroppingState(config);
                        break;
                    case CATCHING:
                        handleBreaks();
                        handleCatchingState(config);
                        break;
                    case LAYING:
                        handleBreaks();
                        handleLayingState(config);
                        break;
                }

                long endTime = System.currentTimeMillis();
                long totalTime = endTime - startTime;
                System.out.println("Total time for loop " + totalTime);

            } catch (Exception ex) {
                Microbot.logStackTrace(this.getClass().getSimpleName(), ex);
            }
        }, 0, 200, TimeUnit.MILLISECONDS);
        return true;
    }

    @Override
    public void shutdown() {
        super.shutdown();
    }

    private void handleIdleState() {
        try {
            if (Rs2Player.isMoving()) return;
            if (!Rs2Player.isAnimating()){
                if (Rs2GroundItem.interact(ItemID.BOX_TRAP, "lay", 4)) {
//                    currentState = State.LAYING;
                    sleepUntilTick(8);
                    return;
                }
            }
            int activeTrapCount = HunterPlugin.getTraps().size();
            if (activeTrapCount==0&&Rs2Inventory.contains("tuft",false)){
                Rs2Walker.walkTo(new WorldPoint(1559,9452,0));
                Rs2Npc.interact("Guild Hunter Aco (Expert)","Rumour");
                Rs2Dialogue.sleepUntilHasDialogueOption("Yes.");
                Rs2Dialogue.keyPressForDialogueOption("Yes.");
                sleep(600);
                shutdown();
//                        sleep(6000);
                return;
            }

            // If there are box traps on the floor, interact with them first
//            for (Map.Entry<WorldPoint, HunterTrap> entry : HunterPlugin.traplist.entrySet()) {
//                HunterTrap.State state=entry.getValue().getState();
//                System.out.println(state+" "+entry.getKey());
//                if (Rs2Inventory.contains("tuft",false)){
//                    if (state.equals(HunterTrap.State.FULL)) {
////                    foundPoint = entry.getKey();
//                        HunterTrap foundTrap = entry.getValue();
//                        rs2TileObjectCache.query().fromWorldView().
//                                where(x -> x.getWorldLocation().equals(foundTrap.getWorldLocation())
//                                        &&x.getName() != null && x.getName().toLowerCase().contains("box")).nearestOnClientThread()
//                                .click("Check");
//                        sleep(1200);
//                        Rs2Player.waitForAnimation();
//                        Rs2GameObject.hoverOverObject(Rs2GameObject.getGameObject(new Integer[]{9383, 9385}));
//                        sleepUntil(() -> {
//                            HunterTrap trap = HunterPlugin.traplist.get(entry.getKey());
//                            if (trap!=null) {
//                                return trap.getObjectId()==9380;
//                            }
//                            return false;
//                        },2000);
////                    sleep(0,200);
//                        System.out.println("Found trap " + foundTrap.getWorldLocation());
////                    sleepUntilTick(8);
//                        break;
//                    }
//                    if (state.equals(HunterTrap.State.OPEN)||state.equals(HunterTrap.State.EMPTY)) {
////                    foundPoint = entry.getKey();
//                        HunterTrap foundTrap = entry.getValue();
//                        rs2TileObjectCache.query().fromWorldView().
//                                where(x -> x.getWorldLocation().equals(foundTrap.getWorldLocation())
//                                        &&x.getName() != null && x.getName().toLowerCase().contains("box")).nearestOnClientThread()
//                                .click("Dismantle");
//                        sleep(1200);
//                        Rs2Player.waitForAnimation();
//                        Rs2GameObject.hoverOverObject(Rs2GameObject.getGameObject(new Integer[]{9383, 9385}));
//                        sleepUntil(() -> {
//                            HunterTrap trap = HunterPlugin.traplist.get(entry.getKey());
//                            if (trap!=null) {
//                                return trap.getObjectId()==9380;
//                            }
//                            return false;
//                        },2000);
////                    sleep(0,200);
//                        System.out.println("Found trap " + foundTrap.getWorldLocation());
////                    sleepUntilTick(8);
//                        break;
//                    }
//
//                }
//                if (state.equals(HunterTrap.State.FULL)||state.equals(HunterTrap.State.EMPTY)) {
////                    foundPoint = entry.getKey();
//                    HunterTrap foundTrap = entry.getValue();
//                    rs2TileObjectCache.query().fromWorldView().
//                    where(x -> x.getWorldLocation().equals(foundTrap.getWorldLocation())
//                    &&x.getName() != null && x.getName().toLowerCase().contains("box")).nearestOnClientThread()
//                    .click("Reset");
//                    sleep(1200);
//                    Rs2Player.waitForAnimation();
//                    Rs2GameObject.hoverOverObject(Rs2GameObject.getGameObject(new Integer[]{9383, 9385}));
//                    sleepUntil(() -> {
//                        HunterTrap trap = HunterPlugin.traplist.get(entry.getKey());
//                        if (trap!=null) {
//                            return trap.getObjectId()==9380;
//                        }
//                        return false;
//                    },2000);
////                    sleep(0,200);
//                    System.out.println("Found trap " + foundTrap.getWorldLocation());
////                    sleepUntilTick(8);
//                    break;
//                }
//            }

            List<Map.Entry<WorldPoint, HunterTrap>> sortedTraps =
                    HunterPlugin.traplist.entrySet().stream()

                            .filter(entry -> {
                                HunterTrap.State state = entry.getValue().getState();

                                if (state == HunterTrap.State.FULL || state == HunterTrap.State.EMPTY)
                                    return true;

                                // Only allow OPEN if we have tuft
                                return state == HunterTrap.State.OPEN
                                        && Rs2Inventory.contains("tuft", false);
                            })

                            .sorted(Comparator.comparingDouble(
                                    (Map.Entry<WorldPoint, HunterTrap> e) ->
                                            e.getValue().getTrapTimeRelative()
                            ).reversed())

                            .collect(Collectors.toList());

            if (!sortedTraps.isEmpty()){
                Map.Entry<WorldPoint, HunterTrap> currentTrapEntry = sortedTraps.get(0);
                HunterTrap foundTrap = currentTrapEntry.getValue();
                HunterTrap.State state = foundTrap.getState();

                String action;

                if (Rs2Inventory.contains("tuft", false)) {
                    if (state == HunterTrap.State.FULL) {
                        action = "Check";
                    } else if (state == HunterTrap.State.OPEN || state == HunterTrap.State.EMPTY) {
                        action = "Dismantle";
                    } else {
                        return;
                    }
                } else {
                    if (state == HunterTrap.State.FULL || state == HunterTrap.State.EMPTY) {
                        action = "Reset";
                    } else {
                        return;
                    }
                }

                rs2TileObjectCache.query()
                        .fromWorldView()
                        .where(x -> x.getWorldLocation().equals(foundTrap.getWorldLocation())
                                && x.getName() != null
                                && x.getName().toLowerCase().contains("box"))
                        .nearestOnClientThread()
                        .click(action);

                sleep(1200);
                Rs2Player.waitForAnimation();
                // Your fitted parameters
                 double LOG_MEAN = 0.25; double LOG_STD = 0.34;Random r = new Random();double gaussian = r.nextGaussian();
                double value = Math.exp(LOG_MEAN + LOG_STD * gaussian);
                sleep((int) value*1000);
                // Hover next valid trap
                sortedTraps.stream()
                        .skip(1)
                        .map(Map.Entry::getValue)
                        .filter(trap ->
                                trap.getState() == HunterTrap.State.FULL
                                        || trap.getState() == HunterTrap.State.EMPTY
                                        || (trap.getState() == HunterTrap.State.OPEN
                                        && Rs2Inventory.contains("tuft", false)))
                        .findFirst()
                        .ifPresent(nextTrap ->
                                rs2TileObjectCache.query()
                                        .fromWorldView()
                                        .where(x -> x.getWorldLocation().equals(nextTrap.getWorldLocation())
                                                && x.getName() != null
                                                && x.getName().toLowerCase().contains("box"))
                                        .nearestOnClientThread()
                                        .hover()
                        );
                sleepUntil(() -> {
                    HunterTrap trap = HunterPlugin.traplist.get(currentTrapEntry.getKey());
                    return trap != null && trap.getObjectId() == 9380;
                }, 3000);
                LOG_MEAN = 0.06;LOG_STD = 0.6;r = new Random();gaussian = r.nextGaussian();
                value = Math.exp(LOG_MEAN + LOG_STD * gaussian);
                sleep((int) value*200);
                System.out.println("Handled trap " + foundTrap.getWorldLocation());
            }



        } catch (Exception ex) {
            Microbot.log(ex.getMessage());
            ex.printStackTrace();
//            currentState = State.CATCHING;
        }
    }

    private void handleDroppingState(AutoHunterConfig config) {
        sleep(config.minSleepAfterLay(), config.maxSleepAfterLay());
        currentState = State.IDLE;
    }

    private void handleCatchingState(AutoHunterConfig config) {
        sleep(config.minSleepAfterCatch(), config.maxSleepAfterCatch());
        currentState = State.IDLE;
    }

    private void handleLayingState(AutoHunterConfig config) {
        sleep(config.minSleepAfterLay(), config.maxSleepAfterLay());
        currentState = State.IDLE;
    }

    public void handleBreaks() {
        int secondsUntilBreak = BreakHandlerScript.breakIn; // Time until the break

        //Clear list incase user changed trap layout This should run about 2-4 minutes before break
        if (secondsUntilBreak > 61 && secondsUntilBreak < 200) {
            if (!boxtiles.isEmpty()) {
                boxtiles.clear();
            }
        }

        if (secondsUntilBreak > 0 && secondsUntilBreak <= 60) {
            // We're going on break in 1 minute or less.
            // Save Trap locations
            for (int trapId : trapIds) {
                List<GameObject> gameObjects = Rs2GameObject.getGameObjects(obj -> obj.getId() == trapId);
                if (gameObjects != null) {
                    for (GameObject gameObject : gameObjects) {
                        if (gameObject != null) {
                            WorldPoint location = gameObject.getWorldLocation();
                            if (Rs2Player.getWorldLocation().distanceTo(location) > 5) {
                                continue; // Skip traps beyond the range
                            }
                            if (!boxtiles.contains(location)) {
                                boxtiles.add(location);
                            }
                        }
                    }
                }
            }

            // At this point, boxtiles should be populated with the world points of the old traps.

            // Dismantling traps for our break.
            for (WorldPoint oldTile : boxtiles) {
                if (Rs2GameObject.getGameObject(oldTile) != null) {
                    //Dismantle or Reset
                    if (Rs2Player.getWorldLocation().distanceTo(oldTile) > 5) {
                        continue; // Skip traps beyond the range
                    }
                    while (Rs2GameObject.getGameObject(oldTile) != null) {
                        if (Rs2Player.getWorldLocation().distanceTo(oldTile) > 5) {
                            break; // Skip traps beyond the range
                        }
                        if (Rs2GameObject.interact(oldTile, "Dismantle")) {
                            sleep(1000, 3000);
                            break;
                        }
                        if (Rs2GameObject.interact(oldTile, "Reset")) {
                            sleep(1000, 3000);
                            break;
                        }
                    }
                }
            }
            oneRun = true;
        }

        //We're back from our break
        if (secondsUntilBreak > 60 && oneRun) {
            if (!boxtiles.isEmpty()) {
                //Setting traps down
                for (WorldPoint LayTrapTile : boxtiles) {
                    if (Rs2GameObject.getGameObject(LayTrapTile) != null) {
                        //There's already an object there do nothing

                    } else {
                        //we need to get to the tile
                        if (!Rs2Player.getWorldLocation().equals(LayTrapTile)) {
                            while (!Rs2Player.getWorldLocation().equals(LayTrapTile)) {
                                Microbot.log("Walking to trap tile");
                                Rs2Walker.walkTo(LayTrapTile, 0);
                                sleep(1000, 3000);
                            }
                        }
                        //we need to put a trap.
                        Microbot.log("Placing trap");
                        int maxTries = 0;
                        while (Rs2GameObject.getGameObject(LayTrapTile) == null) {
                            if (!Rs2GroundItem.exists("Box trap", 6)) {
                                if (Rs2Inventory.contains("Box trap")) {
                                    Rs2Inventory.interact("Box trap", "Lay");
                                    sleep(4000, 6000);
                                }
                            } else {
                                Rs2GroundItem.take("Box trap", 6);
                                sleep(4000, 6000);
                            }
                            if (maxTries >= 3) {
                                Microbot.log("Failed, placing the trap");
                                break;
                            }
                            maxTries++;
                        }
                    }
                }
            }
            oneRun = false;
        }
    }
}
