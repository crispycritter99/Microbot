package net.runelite.client.plugins.microbot.example;

import net.runelite.api.MenuAction;
import net.runelite.api.ObjectComposition;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.util.gameobject.Rs2GameObject;
import net.runelite.client.plugins.microbot.util.menu.NewMenuEntry;
import net.runelite.client.plugins.microbot.util.misc.Rs2UiHelper;

import java.awt.*;
import java.util.concurrent.TimeUnit;


public class ExampleScript extends Script {

    public static boolean test = false;
    public boolean run(ExampleConfig config) {
        Microbot.enableAutoRunOn = false;
        mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> {
            try {
                if (!Microbot.isLoggedIn()) return;
                if (!super.run()) return;
                long startTime = System.currentTimeMillis();

                //CODE HERE
//                Microbot.doInvoke(new NewMenuEntry("Take-from <col=00ffff>Fishing", "<col=ffff>Supply crates", 51371, MenuAction.GAME_OBJECT_SECOND_OPTION, 63, 48, false), new Rectangle(1, 1));
//                ObjectComposition objComp = Rs2GameObject.convertGameObjectToObjectComposition(51371);
//                String[] actions=objComp.getActions();
//                Microbot.log(""+Rs2UiHelper.stripColTags(actions[0]));

                Rs2GameObject.interact(51371,"Take-from Herblore");
//                Rs2GameObject.interact(51371,1);
                String text="Take-from <col=00ffff>Fishing";
                Microbot.log(text);
                Microbot.log(Rs2UiHelper.stripColTags(text));
                Microbot.log(text.replaceAll("<col=[^>]+>|</col>", ""));
                shutdown();
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