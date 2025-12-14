package net.runelite.client.plugins.microbot.thieving;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

import javax.inject.Inject;
import java.awt.*;

@PluginDescriptor(
        name = PluginDescriptor.Mocrosoft + "Thieving Local",
        description = "Microbot thieving plugin",
        tags = {"thieving", "microbot", "skilling"},
        enabledByDefault = false
)
@Slf4j
public class ThievingLocalPlugin extends Plugin {
    @Inject
    private ThievingConfig config;

    @Provides
    ThievingConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(ThievingConfig.class);
    }

    @Inject
    private OverlayManager overlayManager;
    @Inject
    private ThievingOverlay thievingOverlay;

    @Inject
    ThievingLocalScript thievingLocalScript;


    @Override
    protected void startUp() throws AWTException {
        if (overlayManager != null) {
            overlayManager.add(thievingOverlay);
        }
        thievingLocalScript.run(config);
    }

    protected void shutDown() {
        thievingLocalScript.shutdown();
        overlayManager.remove(thievingOverlay);
    }
}
