package net.runelite.client.plugins.microbot.caviarmixer;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.events.GameTick;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.ui.overlay.OverlayManager;

import javax.inject.Inject;
import java.awt.*;

@PluginDescriptor(
        name = PluginDescriptor.Default + "Caviar Mixer",
        description = "Microbot example plugin",
        tags = {"example", "microbot"},
        enabledByDefault = false
)
@Slf4j
public class caviarMixerPlugin extends Plugin {
    @Inject
    private CaviarMixerConfig config;
    @Provides
    CaviarMixerConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(CaviarMixerConfig.class);
    }

    @Inject
    private OverlayManager overlayManager;
    @Inject
    private CaviarMixerOverlay caviarMixerOverlay;

    @Inject
    CaviarMixerScript caviarMixerScript;
public static int startingDaeyaltShard = 0;
public static long initialTime=System.currentTimeMillis();
    @Override
    protected void startUp() throws AWTException {
        if (overlayManager != null) {
            overlayManager.add(caviarMixerOverlay);
        }
        startingDaeyaltShard= Rs2Inventory.itemQuantity("Daeyalt shard");

        caviarMixerScript.run(config);
    }

    protected void shutDown() {
        caviarMixerScript.shutdown();
        overlayManager.remove(caviarMixerOverlay);
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

}
