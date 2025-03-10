package net.runelite.client.plugins.microbot.example;

import net.runelite.api.GameObject;
import net.runelite.api.ObjectComposition;
import net.runelite.api.Skill;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.util.gameobject.Rs2GameObject;
import net.runelite.client.plugins.microbot.util.math.Rs2Random;
import net.runelite.client.plugins.microbot.util.npc.Rs2Npc;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;
import net.runelite.client.plugins.microbot.util.tile.Rs2Tile;
import net.runelite.client.plugins.microbot.util.walker.Rs2Walker;
import net.runelite.client.plugins.microbot.util.widget.Rs2Widget;

import java.util.concurrent.TimeUnit;


public class ExampleScript extends Script {
    public static boolean tentacle = false;
    public static boolean lootnet = false;
     boolean test = false;
    public boolean run(ExampleConfig config) {
        Microbot.enableAutoRunOn = false;

        mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> {
            try {
                if (!Microbot.isLoggedIn()) return;
                if (!super.run()) return;
                long startTime = System.currentTimeMillis();

                //CODE HER//
                //2012,
                if (Rs2GameObject.exists(2483)) {
                    if (lootnet) {
                        Rs2GameObject.interact(2483, "inspect");
//                Rs2Widget.isWidgetVisible()
                        Rs2Player.waitForWalking();
                        sleep(Rs2Random.randomGaussian(600, 300));
                        Rs2Widget.clickWidget("Bank-all");
                        sleep(Rs2Random.randomGaussian(600, 300));
                        lootnet=false;
                    }
                    if (Rs2GameObject.interact(new WorldPoint(2675, 3170, 0), "Cross"))
                        Rs2Player.waitForWalking(10000);

//
                }
                else if (Rs2GameObject.exists(1189)&&Rs2Player.getWorldLocation().getPlane() == 0) Rs2Walker.walkTo(new WorldPoint(2676, 3170, 0));
                String contribution = Rs2Widget.getWidget(23986189).getText();
                int ContributionValue = Integer.parseInt(contribution.split(":")[1].strip());
//                Microbot.log(ContributionValue + "");
                if (Rs2Player.getWorldLocation().getPlane() == 0 && Rs2GameObject.exists(4060)) {
//                    Rs2GameObject.interact(4060,"Climb-up");Rs2Player.waitForWalking();
                    lootnet = true;
                    Rs2GameObject.interact(new WorldPoint(1884, 4826, 0), "Climb-up");
                    Rs2Player.waitForWalking();
                    Rs2Player.waitForAnimation();
                    Rs2Walker.walkFastCanvas(new WorldPoint(1885, 4827, 1));
                }
                if (ContributionValue < 50)
                {
                if (!Rs2Player.isInteracting() && !Rs2Player.isMoving() && Rs2Npc.getNpc("Enormous Tentacle") != null) {
                    if(Rs2Npc.getNpc("Enormous Tentacle").getAnimation()==8910) {return;}
                    if (Rs2Npc.interact("Enormous Tentacle", "Chop")) {
                        tentacle = true;
//                    }
                    }
                }

                 if (!Rs2Player.isInteracting() && !Rs2Player.isMoving() && Rs2Npc.getNpc("Enormous Tentacle") == null && tentacle) {
//                    if(Rs2Npc.getNpc("Enormous Tentacle").getAnimation()==8953) {
                    WorldPoint currentLocation = Rs2Player.getWorldLocation();
                    WorldPoint ladder = Rs2GameObject.getGameObjects(4139).stream().findFirst().get().getWorldLocation();
//                  Microbot.log(""+ladder.dx(1).getX());
                    if (Rs2Player.getWorldLocation().getY() == 4823) {
                        Rs2Walker.walkFastCanvas(new WorldPoint(ladder.dx(1).getX(), currentLocation.dy(4).getY(), Rs2Player.getWorldLocation().getPlane()));
                        tentacle = false;
                    } else if (Rs2Player.getWorldLocation().getY() == 4827) {
                        Rs2Walker.walkFastCanvas(new WorldPoint(ladder.dx(1).getX(), currentLocation.dy(-4).getY(), Rs2Player.getWorldLocation().getPlane()));
                        tentacle = false;
                    }
                }
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
}