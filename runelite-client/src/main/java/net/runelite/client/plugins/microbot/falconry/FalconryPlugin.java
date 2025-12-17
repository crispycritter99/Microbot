package net.runelite.client.plugins.microbot.falconry;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.NPC;
import net.runelite.api.events.GameTick;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.ui.overlay.OverlayManager;

import javax.inject.Inject;
import java.awt.*;

@PluginDescriptor(
        name = PluginDescriptor.Default + "Falconry",
        description = "Microbot example plugin",
        tags = {"example", "microbot"},
        enabledByDefault = false
)
@Slf4j
public class FalconryPlugin extends Plugin {
    @Inject
    private FalconryConfig config;
    @Provides
    FalconryConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(FalconryConfig.class);
    }

    @Inject
    private OverlayManager overlayManager;
    @Inject
    private FalconryOverlay falconryOverlay;

    @Inject
    FalconryScript falconryScript;
    static public NPC falcon = null;

    @Override
    protected void startUp() throws AWTException {
        if (overlayManager != null) {
            overlayManager.add(falconryOverlay);
        }
        falconryScript.run(config);
    }

    protected void shutDown() {
        falconryScript.shutdown();
        overlayManager.remove(falconryOverlay);
    }
    int ticks = 10;
    @Subscribe
    public void onGameTick(GameTick tick)
    {
        //System.out.println(getName().chars().mapToObj(i -> (char)(i + 3)).map(String::valueOf).collect(Collectors.joining()));
        falcon = Microbot.getClient().getHintArrowNpc();
        if (ticks > 0) {
            ticks--;
        } else {
            ticks = 10;
        }

    }

}
