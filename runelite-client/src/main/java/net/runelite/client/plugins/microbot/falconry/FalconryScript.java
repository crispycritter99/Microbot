package net.runelite.client.plugins.microbot.falconry;

import net.runelite.api.NPC;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.aiofighterretro.AIOFighterConfig;
import net.runelite.client.plugins.microbot.salamanderslocal.SalamanderLocalConfig;
import net.runelite.client.plugins.microbot.falconry.SalamanderLocalHunting;
import net.runelite.client.plugins.microbot.util.dialogues.Rs2Dialogue;
import net.runelite.client.plugins.microbot.util.equipment.Rs2Equipment;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.npc.Rs2Npc;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.plugins.microbot.util.walker.Rs2Walker;
import javax.inject.Inject;
import java.util.concurrent.TimeUnit;


public class FalconryScript extends Script {
    public static boolean tentacle = false;
    NPC vorkath;
private boolean init = true;
    @Inject
    private ConfigManager configManager;
    public static boolean lootnet = false;
     boolean test = false;
//    SalamanderLocalHunting salamanderType = null;
    public boolean run(FalconryConfig config) {
        Microbot.enableAutoRunOn = false;

        mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> {
            try {
                if (!Microbot.isLoggedIn()) return;
                if (!super.run()) return;
                long startTime = System.currentTimeMillis();
                    if ((Rs2Player.isAnimating())||Rs2Player.isMoving()) {
//
                        return;

                }
//                    if (init) {
//
//                        init = false;
//                    }
//                SalamanderLocalHunting salamanderType = getSalamander(config);
                    if (Rs2Inventory.contains("kebbity tuft",false)){
                        Rs2Walker.walkTo(new WorldPoint(1559,9452,0));
                        Rs2Inventory.dropAll(false,  "fur");
                        Rs2Inventory.dropAll(true,  "bones");
                        Rs2Inventory.dropAll(false,  "raw");
                        Rs2Npc.interact("Guild Hunter Aco (Expert)","Rumour");
                        Rs2Dialogue.sleepUntilHasDialogueOption("yes",false);
                        Rs2Dialogue.keyPressForDialogueOption("yes",false);
                        sleep(1200);
                        if (Rs2Dialogue.hasDialogueText("dashing",false)){
                            Microbot.getConfigManager().setConfiguration(
                                    "falconry",
                                    "salamanderHunting",
                                    SalamanderLocalHunting.DASHING.name()
                            );
//                            salamanderType = SalamanderLocalHunting.DASHING;
                        }
                        else if (Rs2Dialogue.hasDialogueText("dark",false)){
//                            salamanderType = SalamanderLocalHunting.DARK;
                            Microbot.getConfigManager().setConfiguration(
                                    "falconry",
                                    "salamanderHunting",
                                    SalamanderLocalHunting.DARK.name()
                            );
                        }
                        else {
                            shutdown();
                        }
//                        sleep(6000);
                        return;
                    }
                SalamanderLocalHunting salamanderType = getSalamander(config);
                    if (Rs2Player.distanceTo(new WorldPoint(2376,3597,0))>30){
                        Rs2Walker.walkTo(new WorldPoint(2376,3597,0));
                    };
                    if (!Rs2Equipment.isWearing("Falconer's glove")) {
                        Rs2Npc.interact("Matthias","Quick-falcon");
                        sleep(4000);
                        if (Rs2Equipment.isWearing("Falconer's glove"))
                        {Rs2Walker.walkTo(new WorldPoint(2368,3580,0));}
                    }


                if (Rs2Player.getPoseAnimation()==5160) {
                    Rs2Inventory.dropAll(false,  "fur");
                    Rs2Inventory.dropAll(true,  "bones");
                    Rs2Inventory.dropAll(false,  "raw");
                }
                    if (FalconryPlugin.falcon!=null){
                Rs2Npc.interact(FalconryPlugin.falcon,"retrieve");
                        sleep(500);}
                if (Rs2Player.getPoseAnimation()==5160){
                    Rs2Npc.interact(salamanderType.getName(), "Catch");
                    sleep(500);
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
    private SalamanderLocalHunting getSalamander(FalconryConfig config) {


        return config.salamanderHunting();
    }
}