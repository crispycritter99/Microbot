package net.runelite.client.plugins.microbot.brimhavenspikejumper;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.events.GameTick;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.microbot.MicrobotApi;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.ui.overlay.OverlayManager;

import javax.inject.Inject;
import java.awt.*;

@PluginDescriptor(
        name = "<html>[<font color=#93652a>K</font>] Brimhaven Spike Jumper",
        description = "Automatically jumps the Brimhaven spike obstacle",
        tags = {"brimhaven", "agility", "spike", "microbot"},
        enabledByDefault = false
)
@Slf4j
public class BrimhavenSpikeJumperPlugin extends Plugin {
    @Inject
    private BrimhavenSpikeJumperConfig config;
    @Provides
    BrimhavenSpikeJumperConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(BrimhavenSpikeJumperConfig.class);
    }

    @Inject
    private OverlayManager overlayManager;
    @Inject
    private BrimhavenSpikeJumperOverlay brimhavenSpikeJumperOverlay;

    @Inject
    BrimhavenSpikeJumperScript brimhavenSpikeJumperScript;
public static int startingDaeyaltShard = 0;
public static long initialTime=System.currentTimeMillis();
    @Override
    protected void startUp() throws AWTException {
        if (overlayManager != null) {
            overlayManager.add(brimhavenSpikeJumperOverlay);
        }
        startingDaeyaltShard= Rs2Inventory.itemQuantity("Daeyalt shard");

        brimhavenSpikeJumperScript.run(config);
    }

    protected void shutDown() {
        brimhavenSpikeJumperScript.shutdown();
        overlayManager.remove(brimhavenSpikeJumperOverlay);
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
