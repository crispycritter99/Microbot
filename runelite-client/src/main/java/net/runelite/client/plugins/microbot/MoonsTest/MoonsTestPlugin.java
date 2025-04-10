package net.runelite.client.plugins.microbot.MoonsTest;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameTick;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.microbot.MoonsTest.enums.State;
import net.runelite.client.plugins.microbot.util.npc.Rs2Npc;
import net.runelite.client.plugins.microbot.util.walker.Rs2Walker;
import net.runelite.client.ui.overlay.OverlayManager;

import javax.inject.Inject;
import java.awt.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@PluginDescriptor(
        name = "<html>[<font color=red>Neon</font>] " + "Moons of Peril Test",
        description = "Moons of Peril bot for blood moon only",
        tags = {"microbot", "Moons of Peril", "boss"},
        enabledByDefault = false
)
@Slf4j
public class MoonsTestPlugin extends Plugin {
    private ScheduledExecutorService scheduler;
    private ExecutorService executor;
    private ExecutorService sleepUntilExecutor;
    private ExecutorService walkingExecutor;
    @Inject
    private MoonsTestConfig config;
    @Provides
    MoonsTestConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(MoonsTestConfig.class);
    }

    @Inject
    private OverlayManager overlayManager;
    @Inject
    private MoonsTestOverlay moonsTestOverlay;
    @Inject
    MoonsTestScript moonsTestScript;

    @Override
    protected void startUp() throws AWTException {
        if (overlayManager != null) {
            overlayManager.add(moonsTestOverlay);
        }
        MoonsTestScript.state = State.CHAMBER;
        moonsTestScript.startup();
        moonsTestScript.run(config);
        if (executor == null || executor.isShutdown() || executor.isTerminated()) {
            executor = Executors.newSingleThreadExecutor();
        }
        if (scheduler == null || scheduler.isShutdown() || scheduler.isTerminated()) {
            scheduler = Executors.newScheduledThreadPool(1);
        }
        if (sleepUntilExecutor == null || sleepUntilExecutor.isShutdown() || sleepUntilExecutor.isTerminated()) {
            sleepUntilExecutor = Executors.newSingleThreadExecutor();
        }
        if (walkingExecutor == null || walkingExecutor.isShutdown() || walkingExecutor.isTerminated()) {
            walkingExecutor = Executors.newSingleThreadExecutor();
        }
    }

    protected void shutDown() {
        moonsTestScript.shutdown();
        overlayManager.remove(moonsTestOverlay);
        scheduler.shutdown();
        executor.shutdown();
        sleepUntilExecutor.shutdown();
    }

    @Subscribe
    public void onChatMessage(ChatMessage event) {
        if (event.getType() != ChatMessageType.GAMEMESSAGE) return;

        if (event.getMessage().equalsIgnoreCase("oh dear, you are dead!")) {
            Rs2Walker.setTarget(null);
            shutDown();
        }
    }

    public static int ticks = 10;
    @Subscribe
    public void onGameTick(GameTick tick)
    {
        //System.out.println(getName().chars().mapToObj(i -> (char)(i + 3)).map(String::valueOf).collect(Collectors.joining()));
        if (Rs2Npc.getNpc(13011).getAnimation()!=11000){
            ticks = 0;
        }
        else {
            ticks++;
        }


    }

//    public static void danceJaguar(String npcName) {
//        Rs2Walker.walkFastCanvas(MoonsScript.closestTile);
//        if (Rs2Player.distanceTo(MoonsScript.closestTile)<3){
//            Global.sleep(300);
//            Rs2Npc.interact("blood jaguar", "attack");
//        }
//
//    }
}
