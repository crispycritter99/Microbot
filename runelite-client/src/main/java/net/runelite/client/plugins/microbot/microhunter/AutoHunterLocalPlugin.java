package net.runelite.client.plugins.microbot.microhunter;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.microbot.PluginConstants;
import net.runelite.client.plugins.microbot.microhunter.scripts.AutoChinScript;
import net.runelite.client.ui.overlay.OverlayManager;

import javax.inject.Inject;
import java.awt.*;

@PluginDescriptor(
        name = PluginDescriptor.Mocrosoft + "AutoHunter Local",
        description = "Microbot AutoHunter plugin",
        tags = {"hunter", "microbot"},
        version = AutoHunterLocalPlugin.version,
        minClientVersion = "2.0.13",
        cardUrl = "",
        iconUrl = "",
        enabledByDefault = PluginConstants.DEFAULT_ENABLED
)
@Slf4j
public class AutoHunterLocalPlugin extends Plugin {
    public static final String version = "1.1.1";
    @Inject
    private AutoHunterConfig config;

    @Provides
    AutoHunterConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(AutoHunterConfig.class);
    }

    @Inject
    private OverlayManager overlayManager;
    @Inject
    private AutoHunterOverlay autoHunterOverlay;

    @Inject
    AutoChinScript autoChinScript;


    @Override
    protected void startUp() throws AWTException {
        if (overlayManager != null) {
            overlayManager.add(autoHunterOverlay);
        }
        autoChinScript.run(config);
    }

    protected void shutDown() {
        autoChinScript.shutdown();
        overlayManager.remove(autoHunterOverlay);
    }

}