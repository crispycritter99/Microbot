package net.runelite.client.plugins.microbot.crafting.scripts;

import net.runelite.api.GameObject;
import net.runelite.api.ItemID;
import net.runelite.api.ObjectID;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.crafting.CraftingConfig;
import net.runelite.client.plugins.microbot.util.bank.Rs2Bank;
import net.runelite.client.plugins.microbot.util.bank.enums.BankLocation;
import net.runelite.client.plugins.microbot.util.dialogues.Rs2Dialogue;
import net.runelite.client.plugins.microbot.util.gameobject.Rs2GameObject;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.keyboard.Rs2Keyboard;
import net.runelite.client.plugins.microbot.util.math.Rs2Random;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;
import net.runelite.client.plugins.microbot.util.walker.Rs2Walker;
import net.runelite.client.plugins.microbot.util.widget.Rs2Widget;

import java.awt.event.KeyEvent;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static net.runelite.client.plugins.microbot.util.Global.sleepUntil;
import static net.runelite.client.plugins.microbot.util.Global.sleepUntilTrue;

enum State {
    SPINNING,
    BANKING,
    WALKING
}

public class FlaxSpinScript extends Script {
    public static double version = 1.0;

    State state;
    boolean init = true;

    public boolean run(CraftingConfig config) {
        Microbot.enableAutoRunOn = false;
        initialPlayerLocation = null;
        mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> {
            try {
                if (!super.run()) return;
                if (!Microbot.isLoggedIn()) return;
                long startTime = System.currentTimeMillis();

                if (init) {
                    getState(config);
                }

                if (initialPlayerLocation == null) {
                    initialPlayerLocation = Rs2Player.getWorldLocation();
                }

                switch (state) {
                    case SPINNING:
//                        Microbot.log("hi");
                        if (!Rs2Inventory.hasItem(ItemID.JUTE_FIBRE)) {
                            state = State.BANKING;
                            return;
                        }
                        GameObject loom = Rs2GameObject.findObject("Loom", false, 30, false, Rs2Player.getWorldLocation());

//                        Microbot.log("hi");
//                        Rs2Inventory.useItemOnObject(ItemID.JUTE_FIBRE, config.flaxSpinLocation().getObjectID());
//                        Rs2Inventory.use(ItemID.JUTE_FIBRE);
//                        Rs2GameObject.interact(loom, "use");
                        Rs2GameObject.interact(loom, "Weave");
                        Rs2Random.wait(2000, 3000);
                        //Rs2Inventory.useItemOnObject(ItemID.JUTE_FIBRE, config.flaxSpinLocation().getObjectID());
//                        Microbot.log("ha");
                        sleepUntil(() -> !Rs2Player.isMoving());
                        if (Rs2Widget.sleepUntilHasWidget("how many do you wish to make?")) {
//                        Rs2Keyboard.keyPress(KeyEvent.VK_SPACE);
//                        Rs2Dialogue.keyPressForDialogueOption("Drift net");
                        Rs2Widget.clickWidget("Drift Net");
                            sleepUntilTrue(() -> !Rs2Inventory.hasItem(ItemID.JUTE_FIBRE), 600, 150000);

                            state = State.BANKING;
                        }
                        break;
                    case BANKING:
                        GameObject chest = Rs2GameObject.findObject("Bank Chest", false, 30, false, Rs2Player.getWorldLocation());

//                        boolean isBankOpen = Rs2Bank.walkToBankAndUseBank(BankLocation.FOSSIL_ISLAND);
                        Rs2Bank.useBank();
                        sleepUntil(Rs2Bank::isOpen, 5000);
                        if (!Rs2Bank.useBank() || !Rs2Bank.isOpen()) return;

                        Rs2Bank.depositAll();
//                        sleep(Rs2Random.randomGaussian(800,1.5));
                        Rs2Bank.withdrawAll(ItemID.JUTE_FIBRE);
                        sleep(Rs2Random.randomGaussian(800,1.5));

//                        Rs2Bank.closeBank();
                        Rs2Keyboard.keyPress(KeyEvent.VK_ESCAPE);
                        if (!Rs2Inventory.hasItem(ItemID.JUTE_FIBRE)) {
                            shutdown();
                            return;
                        }
                        state = State.SPINNING;
                        break;
                    case WALKING:
//                        Microbot.log("hi32342");
//                        Rs2Walker.walkTo(config.flaxSpinLocation().getWorldPoint(), 4);
                        sleepUntilTrue(() -> isNearSpinningWheel(config, 30) && !Rs2Player.isMoving(), 600, 300000);
//                        if (!isNearSpinningWheel(config, 4)) return;
                        Optional<GameObject> spinningWheel = Rs2GameObject.getGameObjects().stream()
                                .filter(obj -> obj.getId() == config.flaxSpinLocation().getObjectID())
                                .sorted(Comparator.comparingInt(obj -> Rs2Player.getWorldLocation().distanceTo(obj.getWorldLocation())))
                                .findFirst();
                        if (spinningWheel.isEmpty()) {
                            Rs2Walker.walkFastCanvas(config.flaxSpinLocation().getWorldPoint());
                            return;
                        }
                        state = State.SPINNING;
                        break;
                }

                long endTime = System.currentTimeMillis();
                long totalTime = endTime - startTime;
                System.out.println("Total time for loop " + totalTime);

            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
        }, 0, 1000, TimeUnit.MILLISECONDS);
        return true;
    }

    @Override
    public void shutdown() {
        super.shutdown();
    }

    private void getState(CraftingConfig config) {
        if (!Rs2Inventory.hasItem(ItemID.JUTE_FIBRE)) {
            state = State.BANKING;
            init = false;
            return;
        }
       // if (!isNearSpinningWheel(config, 4)) {
      //      state = State.WALKING;
      //      init = false;
       //     return;
       // }

        state = State.SPINNING;
        init = false;
    }

    private boolean isNearSpinningWheel(CraftingConfig config, int distance) {
        return Rs2Player.getWorldLocation().distanceTo(config.flaxSpinLocation().getWorldPoint()) <= distance;
    }
}
