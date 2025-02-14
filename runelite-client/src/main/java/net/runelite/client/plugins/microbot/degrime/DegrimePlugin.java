package net.runelite.client.plugins.microbot.degrime;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.api.events.WidgetLoaded;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.microbot.util.bank.Rs2Bank;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.inventory.Rs2ItemModel;
import net.runelite.client.ui.overlay.OverlayManager;

import javax.inject.Inject;
import java.awt.*;
import java.util.Arrays;

@PluginDescriptor(
        name = PluginDescriptor.Bank + "Degrime",
        description = "Degrimes herbs at bank",
        tags = {"degrime", "bank", "eXioStorm", "storm"},
        enabledByDefault = false
)
@Slf4j
public class DegrimePlugin extends Plugin {
    @Inject
    private DegrimeConfig config;

    @Provides
    DegrimeConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(DegrimeConfig.class);
    }

    @Inject
    private OverlayManager overlayManager;
    @Inject
    private DegrimeOverlay degrimeOverlay;

    @Inject
    DegrimeScript degrimeScript;


    @Override
    protected void startUp() throws AWTException {
        if (overlayManager != null) {
            overlayManager.add(degrimeOverlay);
        }
        degrimeScript.run(config);
    }

    @Subscribe
    public void onWidgetLoaded(WidgetLoaded widget){
        if (widget.getGroupId()==270) {
            if(DegrimeScript.isWaitingForPrompt) {
                DegrimeScript.isWaitingForPrompt = false;
            }
        }
    }
    //*/ Added by Storm
    protected void shutDown() {
        degrimeScript.shutdown();
        overlayManager.remove(degrimeOverlay);
    }
}
