package net.runelite.client.plugins.microbot.fishing.tickbarb;

import com.google.inject.Provides;
import net.runelite.api.Client;
import net.runelite.api.events.GameTick;
import net.runelite.client.Notifier;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.util.mouse.VirtualMouse;
import net.runelite.client.ui.overlay.OverlayManager;

import javax.inject.Inject;
import java.awt.*;

@PluginDescriptor(
        name = PluginDescriptor.See1Duck + " Tick Barbarian Fisher",
        description = "Barbarian Fishing plugin",
        tags = {"Fishing", "barbarian", "skilling"},
        enabledByDefault = false
)
public class tickbarbPlugin extends Plugin {
    @Inject
    Notifier notifier;
    @Inject
    tickbarbScript fishingScript;
    @Inject
    private tickbarbConfig config;
    @Inject
    private Client client;
    @Inject
    private ClientThread clientThread;
    @Inject
    private OverlayManager overlayManager;
    @Inject
    private tickbarbOverlay fishingOverlay;

    @Provides
    tickbarbConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(tickbarbConfig.class);
    }

    @Override
    protected void startUp() throws AWTException {
        Microbot.pauseAllScripts = false;
        Microbot.setClient(client);
        Microbot.setClientThread(clientThread);
        Microbot.setNotifier(notifier);
        Microbot.setMouse(new VirtualMouse());
        if (overlayManager != null) {
            overlayManager.add(fishingOverlay);
        }
        fishingScript.run(config);
    }

    @Subscribe
    public void onGameTick(GameTick tick) {
        fishingScript.onGameTick();
    }

    protected void shutDown() {
        fishingScript.shutdown();
        overlayManager.remove(fishingOverlay);
    }
}
