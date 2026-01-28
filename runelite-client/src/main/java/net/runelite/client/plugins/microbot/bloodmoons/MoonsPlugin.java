package net.runelite.client.plugins.microbot.bloodmoons;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameTick;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.microbot.bloodmoons.enums.State;
import net.runelite.client.plugins.microbot.util.Global;
import net.runelite.client.plugins.microbot.util.npc.Rs2Npc;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;
import net.runelite.client.plugins.microbot.util.walker.Rs2Walker;
import net.runelite.client.ui.overlay.OverlayManager;

import javax.inject.Inject;
import java.awt.*;

@PluginDescriptor(
        name = "<html>[<font color=red>Neon</font>] " + "Moons of Peril",
        description = "Moons of Peril bot for blood moon only",
        tags = {"microbot", "Moons of Peril", "boss"},
        enabledByDefault = false
)
@Slf4j
public class MoonsPlugin extends Plugin {
    @Inject
    private MoonsConfig config;
    @Provides
    MoonsConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(MoonsConfig.class);
    }

    @Inject
    private OverlayManager overlayManager;
    @Inject
    private MoonsOverlay moonsOverlay;
    @Inject
    MoonsScript moonsScript;

    @Override
    protected void startUp() throws AWTException {
        if (overlayManager != null) {
            overlayManager.add(moonsOverlay);
        }
        MoonsScript.state = State.CHAMBER;
        moonsScript.startup();
        moonsScript.run(config);
    }

    protected void shutDown() {
        moonsScript.shutdown();
        overlayManager.remove(moonsOverlay);
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
    public void onGameTick(GameTick tick) {
        //System.out.println(getName().chars().mapToObj(i -> (char)(i + 3)).map(String::valueOf).collect(Collectors.joining()));
        if (Rs2Npc.getNpcs("blood moon").count() != 0) {
            if (Rs2Npc.getNpc(13011).getAnimation() != 11000) {
                ticks = 0;
            } else {
                ticks++;
            }

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
