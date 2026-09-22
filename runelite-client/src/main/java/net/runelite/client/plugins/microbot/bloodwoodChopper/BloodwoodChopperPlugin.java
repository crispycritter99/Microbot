package net.runelite.client.plugins.microbot.bloodwoodChopper;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

import javax.inject.Inject;
import java.awt.*;

@PluginDescriptor(
        name = PluginDescriptor.Default + "Bloodwood Chopper",
        description = "Empty Bloodwood Chopper plugin scaffold",
        tags = {"woodcutting", "bloodwood", "microbot"},
        enabledByDefault = false
)
@Slf4j
public class BloodwoodChopperPlugin extends Plugin {
    @Inject
    private BloodwoodChopperConfig config;
    @Provides
    BloodwoodChopperConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(BloodwoodChopperConfig.class);
    }

    @Inject
    private OverlayManager overlayManager;
    @Inject
    private BloodwoodChopperOverlay bloodwoodChopperOverlay;

    @Inject
    BloodwoodChopperScript bloodwoodChopperScript;
    @Override
    protected void startUp() throws AWTException {
        if (overlayManager != null) {
            overlayManager.add(bloodwoodChopperOverlay);
        }
        bloodwoodChopperScript.run(config);
    }

    protected void shutDown() {
        bloodwoodChopperScript.shutdown();
        overlayManager.remove(bloodwoodChopperOverlay);
    }

}
