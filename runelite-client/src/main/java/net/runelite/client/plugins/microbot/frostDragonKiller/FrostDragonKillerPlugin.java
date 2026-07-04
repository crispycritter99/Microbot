package net.runelite.client.plugins.microbot.frostDragonKiller;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.events.GameTick;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.microbot.MicrobotApi;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.ui.overlay.OverlayManager;

import javax.inject.Inject;
import java.awt.*;

@PluginDescriptor(
        name = "<html>[<font color=#D2B48C>K</font>] FrostDragonKiller",
        description = "Microbot example plugin",
        tags = {"example", "microbot"},
        enabledByDefault = false
)
@Slf4j
public class FrostDragonKillerPlugin extends Plugin {
    @Inject
    private FrostDragonKillerConfig config;
    @Provides
    FrostDragonKillerConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(FrostDragonKillerConfig.class);
    }

    @Inject
    private OverlayManager overlayManager;
    @Inject
    private FrostDragonKillerOverlay exampleOverlay;

    @Inject
    FrostDragonKillerScript exampleScript;
public static int startingDaeyaltShard = 0;
public static long initialTime=System.currentTimeMillis();
    @Override
    protected void startUp() throws AWTException {
        if (overlayManager != null) {
            overlayManager.add(exampleOverlay);
        }
        startingDaeyaltShard= Rs2Inventory.itemQuantity("Daeyalt shard");

        exampleScript.updateLootItems(config.lootItems());
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
        if (FrostDragonKillerConfig.GROUP.equals(event.getGroup())
                && FrostDragonKillerConfig.LOOT_ITEMS_KEY.equals(event.getKey())) {
            exampleScript.updateLootItems(event.getNewValue());
        }
    }

}
