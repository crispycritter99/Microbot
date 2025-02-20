package net.runelite.client.plugins.microbot.salamander;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

import javax.inject.Inject;
import java.awt.*;

@PluginDescriptor(
        name = PluginDescriptor.Mocrosoft + "Salamander Hunter",
        description = "Microbot Salamander plugin",
        tags = {"hunter", "microbot"},
        enabledByDefault = false
)
@Slf4j
public class SalamanderPlugin extends Plugin {
    @Inject
    private SalamanderConfig config;
    @Provides
    SalamanderConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(SalamanderConfig.class);
    }

    @Inject
    private OverlayManager overlayManager;
    @Inject
    private SalamanderOverlay salamanderOverlay;

    @Inject
    SalamanderScript salamanderScript;


    @Override
    protected void startUp() throws AWTException {
        if (overlayManager != null) {
            overlayManager.add(salamanderOverlay);
        }
        salamanderScript.run(config);
    }

    protected void shutDown() {
        salamanderScript.shutdown();
        overlayManager.remove(salamanderOverlay);
    }

}