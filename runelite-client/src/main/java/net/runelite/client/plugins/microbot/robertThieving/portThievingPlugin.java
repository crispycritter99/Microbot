package net.runelite.client.plugins.microbot.robertThieving;

import com.google.inject.Provides;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.events.GameTick;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.microbot.util.npc.Rs2NpcModel;
import net.runelite.client.ui.overlay.OverlayManager;

import javax.inject.Inject;
import java.awt.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@PluginDescriptor(
        name = PluginDescriptor.Mocrosoft + "Port Roberts Thieving",
        description = "Microbot example plugin",
        tags = {"example", "microbot"},
        enabledByDefault = false
)
@Slf4j
public class portThievingPlugin extends Plugin {
    @Inject
    private portThievingConfig config;
    @Provides
    portThievingConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(portThievingConfig.class);
    }
    @Getter
    private String npcName;
    @Getter
    private static List<String> itemNames;
    @Getter
    private int minStock;

    @Getter
    private boolean useBank;
    @Getter
    private boolean useNextWorld;
    @Getter
    private boolean useLogout;
    @Getter
    private boolean useExactNaming;
    @Inject
    private OverlayManager overlayManager;
    @Inject
    private portThievingOverlay portThievingOverlay;

    @Inject
    portThievingScript portThievingScript;
    public static Rs2NpcModel closestGuard=null;
    public static Rs2NpcModel[] portGuards=null;

    @Override
    protected void startUp() throws AWTException {
        if (overlayManager != null) {
            overlayManager.add(portThievingOverlay);
        }
        updateItemList("spice,silver ore,silver bar,tiara");
        portThievingScript.run(config);
    }

    protected void shutDown() {
        portThievingScript.shutdown();
        overlayManager.remove(portThievingOverlay);
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
    private void updateItemList(String items) {
        if (items.isBlank() || items.isEmpty()) return;

        if (items.contains(",") || items.contains(", ")) {
            itemNames = Arrays.stream(items.split(","))
                    .map(String::trim)
                    .map(String::toLowerCase)
                    .collect(Collectors.toList());
        } else {
            itemNames = Collections.singletonList(items.trim().toLowerCase());
        }
    }
}
