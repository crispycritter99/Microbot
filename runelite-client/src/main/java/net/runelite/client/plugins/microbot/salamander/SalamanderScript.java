package net.runelite.client.plugins.microbot.salamander;

import net.runelite.api.GameObject;
import net.runelite.api.ItemID;
import net.runelite.api.ObjectID;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.breakhandler.BreakHandlerScript;
import net.runelite.client.plugins.microbot.util.gameobject.Rs2GameObject;
import net.runelite.client.plugins.microbot.util.grounditem.LootingParameters;
import net.runelite.client.plugins.microbot.util.grounditem.Rs2GroundItem;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.math.Rs2Random;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;
import net.runelite.client.plugins.microbot.util.walker.Rs2Walker;
import net.runelite.client.plugins.microbot.util.grounditem.LootingParameters;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

enum State {
    IDLE,
    CATCHING,
    DROPPING,
    LAYING
}


public class SalamanderScript extends Script {

    public static boolean test = false;
    public static String version = "1.1.0";
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
   public LootingParameters lootParams = new LootingParameters(
            10,
            1,
            1,
            0,
            false,
            true,
            "small fishing net","rope"
    );
    State currentState = State.IDLE;
    public boolean run(SalamanderConfig config) {
        Microbot.enableAutoRunOn = false;

        mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> {
            try {
                if (!Microbot.isLoggedIn()) return;
                if (!super.run()) return;
                long startTime = System.currentTimeMillis();

                switch(currentState) {
                    case IDLE:
//                        handleBreaks();
                        handleIdleState();
                        break;
                    case DROPPING:
//                        handleBreaks();
                        handleDroppingState(config);
                        break;
                    case CATCHING:
//                        handleBreaks();
                        handleCatchingState(config);
                        break;
                    case LAYING:
//                        handleBreaks();
                        handleLayingState(config);
                        break;
                }

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

    private void handleIdleState() {
        try {
            // If there are box traps on the floor, interact with them first

            if (Rs2Inventory.interact(10147,"release")){sleep(Rs2Random.randomGaussian(500,3));}
            if (Rs2GroundItem.lootItemsBasedOnNames(lootParams)) {
                Microbot.pauseAllScripts = false;
            }
            // If our inventory is full of ferrets
//            if(Rs2Inventory.getEmptySlots() <= 1 && Rs2Inventory.contains(ItemID.FERRET)){
//                // ferrets have the option release and not drop
//                while(Rs2Inventory.contains(ItemID.FERRET)){
//                    Rs2Inventory.interact(ItemID.FERRET, "Release");
//                    sleep(0,750);
//                    if(!Rs2Inventory.contains(ItemID.FERRET)){
//                        break;
//                    }
//                }
//                currentState = State.DROPPING;
//                return;
//            }

            // If there are shaking boxes, interact with them. ferrets
            if (Rs2GameObject.interact(ObjectID.NET_TRAP_8986, "reset", 10)) {
//                currentState = State.CATCHING;
                while (Rs2Player.getAnimation()!=5215){sleep(100);}
                while (Rs2Player.getAnimation()!=-1){sleep(100);}
                while (Rs2Player.isMoving()){sleep(100);}
                return;
            }
            if (Rs2Inventory.contains("small fishing net")&&Rs2Inventory.contains("rope")){
                if (Rs2GameObject.interact(8990)){
//                    currentState = State.LAYING;
                    while (Rs2Player.getAnimation()==-1){sleep(100);}
                    while (Rs2Player.getAnimation()!=-1){sleep(100);}
                }
            }
//            // If there are shaking boxes, interact with them
//            if (Rs2GameObject.interact(ObjectID.SHAKING_BOX_9383, "reset", 4)) {
//                currentState = State.CATCHING;
//                return;
//            }
//            // If there are shaking boxes, interact with them
//            if (Rs2GameObject.interact(ObjectID.SHAKING_BOX_9382, "reset", 4)) {
//                currentState = State.CATCHING;
//                return;
//            }

//            // Interact with traps that have not caught anything
//            if (Rs2GameObject.interact(ObjectID.BOX_TRAP_9385, "reset", 4)) {
//                currentState = State.CATCHING;
//            }
        } catch (Exception ex) {
            Microbot.log(ex.getMessage());
            ex.printStackTrace();
//            currentState = State.CATCHING;
        }
    }

    private void handleDroppingState(SalamanderConfig config) {
        sleep(config.minSleepAfterLay(), config.maxSleepAfterLay());
        currentState = State.IDLE;
    }

    private void handleCatchingState(SalamanderConfig config) {
        sleep(config.minSleepAfterCatch(), config.maxSleepAfterCatch());
        currentState = State.IDLE;
    }

    private void handleLayingState(SalamanderConfig config) {
        sleep(config.minSleepAfterLay(), config.maxSleepAfterLay());
        currentState = State.IDLE;
    }

    public void handleBreaks() {
         int secondsUntilBreak = BreakHandlerScript.breakIn; // Time until the break

        //Clear list incase user changed trap layout This should run about 2-4 minutes before break
        if(secondsUntilBreak > 61 && secondsUntilBreak <200){
            if(!boxtiles.isEmpty()){
                boxtiles.clear();
            }
        }

        if (secondsUntilBreak > 0 && secondsUntilBreak <= 60) {
            // We're going on break in 1 minute or less.
            // Save Trap locations
            for (int trapId : trapIds) {
                List<GameObject> gameObjects = Rs2GameObject.getGameObjects(trapId);
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
            oneRun=true;
        }

        //We're back from our break
        if (secondsUntilBreak > 60&&oneRun) {
            if(!boxtiles.isEmpty()) {
                //Setting traps down
                for (WorldPoint LayTrapTile : boxtiles) {
                    if(Rs2GameObject.getGameObject(LayTrapTile)!=null){
                        //There's already an object there do nothing

                    } else {
                        //we need to get to the tile
                        if(!Rs2Player.getWorldLocation().equals(LayTrapTile)) {
                            while(!Rs2Player.getWorldLocation().equals(LayTrapTile)){
                                Microbot.log("Walking to trap tile");
                                Rs2Walker.walkTo(LayTrapTile, 0);
                                sleep(1000,3000);
                            }
                        }
                        //we need to put a trap.
                        Microbot.log("Placing trap");
                        int maxTries = 0;
                        while(Rs2GameObject.getGameObject(LayTrapTile) == null) {
                            if(!Rs2GroundItem.exists("Box trap", 6)) {
                                if (Rs2Inventory.contains("Box trap")) {
                                    Rs2Inventory.interact("Box trap", "Lay");
                                    sleep(4000, 6000);
                                }
                            } else {
                                Rs2GroundItem.take("Box trap", 6);
                                sleep(4000, 6000);
                            }
                            if(maxTries>=3){
                                Microbot.log("Failed, placing the trap");
                                break;
                            }
                            maxTries++;
                        }
                    }
                }
            }
            oneRun=false;
        }
    }
}
