package net.runelite.client.plugins.microbot.valetotemsbeta;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.events.GameTick;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.microbot.PluginConstants;
import net.runelite.client.ui.overlay.OverlayManager;

import javax.inject.Inject;
import java.awt.*;

@PluginDescriptor(
        name = PluginConstants.MKE + "Vale Totems Beta",
        description = "Strategy-driven beta of the automated Vale Totems minigame",
        tags = {"valetotems", "fletching", "minigame", "beta"},
        authors = { "Make" },
        version = ValeTotemBetaPlugin.version,
        minClientVersion = "1.9.7",
        iconUrl = "https://chsami.github.io/Microbot-Hub/ValeTotemPlugin/assets/card.png",
        cardUrl = "https://chsami.github.io/Microbot-Hub/ValeTotemPlugin/assets/card.png",
        enabledByDefault = PluginConstants.DEFAULT_ENABLED,
        isExternal = PluginConstants.IS_EXTERNAL
)
@Slf4j
public class ValeTotemBetaPlugin extends Plugin {
    static final String version = "1.1.0-beta.1";

    @Inject
    private ValeTotemBetaConfig config;
    
    @Provides
    ValeTotemBetaConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(ValeTotemBetaConfig.class);
    }

    @Inject
    private OverlayManager overlayManager;
    @Inject
    private ValeTotemBetaOverlay valeTotemOverlay;

    @Inject
    ValeTotemBetaScript valeTotemScript;

    @Override
    protected void startUp() throws AWTException {
        if (overlayManager != null) {
            overlayManager.add(valeTotemOverlay);
        }
        valeTotemScript.run(config);
        log.info("Vale Totems plugin started");
    }

    protected void shutDown() {
        if (valeTotemScript != null) {
            valeTotemScript.shutdown();
        }
        if (overlayManager != null && valeTotemOverlay != null) {
            overlayManager.remove(valeTotemOverlay);
        }
        log.info("Vale Totems plugin stopped");
    }

    @Subscribe
    public void onGameTick(GameTick tick) {
        // Plugin tick events can be used for additional monitoring or UI updates
        // Main bot logic is handled in the ValeTotemBetaScript
    }

    /**
     * Get the current script instance
     * @return the vale totem script
     */
    public ValeTotemBetaScript getScript() {
        return valeTotemScript;
    }

    /**
     * Get the plugin configuration
     * @return the vale totem config
     */
    public ValeTotemBetaConfig getPluginConfig() {
        return config;
    }
}
