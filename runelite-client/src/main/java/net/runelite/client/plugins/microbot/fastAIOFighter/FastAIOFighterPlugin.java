package net.runelite.client.plugins.microbot.fastAIOFighter;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameTick;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.MicrobotApi;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.ui.overlay.OverlayManager;

import javax.inject.Inject;
import java.awt.*;
import java.util.Locale;

@PluginDescriptor(
        name = "<html>[<font color=#D2B48C>K</font>] FastAIOFighter",
        description = "Fast configurable AIO fighter",
        tags = {"combat", "fighter", "microbot"},
        enabledByDefault = false
)
@Slf4j
public class FastAIOFighterPlugin extends Plugin {
    private static final String SLAYER_RESTRICTION_MESSAGE = "wants you to stick to your slayer assignments";
    @Inject
    private FastAIOFighterConfig config;
    @Provides
    FastAIOFighterConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(FastAIOFighterConfig.class);
    }

    @Inject
    private OverlayManager overlayManager;
    @Inject
    private FastAIOFighterOverlay exampleOverlay;

    @Inject
    FastAIOFighterScript exampleScript;
public static int startingDaeyaltShard = 0;
public static long initialTime=System.currentTimeMillis();
    @Override
    protected void startUp() throws AWTException {
        if (overlayManager != null) {
            overlayManager.add(exampleOverlay);
        }
        startingDaeyaltShard= Rs2Inventory.itemQuantity("Daeyalt shard");

        exampleScript.updateAttackableNpcs(config.attackableNpcs());
        exampleScript.run(config);
    }

    protected void shutDown() {
        exampleScript.shutdown();
        overlayManager.remove(exampleOverlay);
    }
    int ticks = 10;
    @Subscribe
    public void onGameTick(GameTick tick)
    {
        //System.out.println(getName().chars().mapToObj(i -> (char)(i + 3)).map(String::valueOf).collect(Collectors.joining()));

        if (ticks > 0) {
            ticks--;
        } else {
            ticks = 10;
        }

    }


    @Subscribe
    public void onConfigChanged(ConfigChanged event) {
        if (FastAIOFighterConfig.GROUP.equals(event.getGroup())
                && FastAIOFighterConfig.ATTACKABLE_NPCS_KEY.equals(event.getKey())) {
            exampleScript.updateAttackableNpcs(event.getNewValue());
        }
    }

    @Subscribe
    public void onChatMessage(ChatMessage event) {
        if (event.getType() != ChatMessageType.GAMEMESSAGE) return;

        String message = event.getMessage();
        if (message != null && message.toLowerCase(Locale.ROOT).contains(SLAYER_RESTRICTION_MESSAGE)) {
            exampleScript.shutdown();
            Microbot.status = "Stopped: target requires a Slayer assignment";
        }
    }
}
