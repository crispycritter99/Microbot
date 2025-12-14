package net.runelite.client.plugins.microbot.smeltglass;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.api.gameval.ItemID;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.microbot.PluginConstants;
import net.runelite.client.ui.overlay.OverlayManager;

import javax.inject.Inject;
import java.awt.*;

@PluginDescriptor(
        name = PluginDescriptor.Vince + "Glass Smelting",
        description = "Smelt ores/coal into bars",
        tags = {"smithing", "smelting", "microbot", "skilling"},
        version = GlassSmeltingPlugin.version,
        minClientVersion = "2.0.13",
        cardUrl = "",
        iconUrl = "",
        enabledByDefault = PluginConstants.DEFAULT_ENABLED,
        isExternal = PluginConstants.IS_EXTERNAL
)
@Slf4j
public class GlassSmeltingPlugin extends Plugin {
    public static final String version = "1.0.3";
    @Inject
    private GlassSmeltingConfig config;
    @Provides
    GlassSmeltingConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(GlassSmeltingConfig.class);
    }

    @Inject
    private OverlayManager overlayManager;
    @Inject
    private GlassSmeltingOverlay glassSmeltingOverlay;

    @Inject
    GlassSmeltingScript glassSmeltingScript;

    @Subscribe
    public void onChatMessage(ChatMessage chatMessage) {
        if (chatMessage.getType() == ChatMessageType.GAMEMESSAGE) {
            if(chatMessage.getMessage().contains("The coal bag is now empty.")){
                if(!GlassSmeltingScript.coalBagEmpty) GlassSmeltingScript.coalBagEmpty=true;
            }

            if(chatMessage.getMessage().contains("The coal bag contains")){
                if(GlassSmeltingScript.coalBagEmpty) GlassSmeltingScript.coalBagEmpty=false;
            }
        }
    }

    @Subscribe
    public void onItemContainerChanged(ItemContainerChanged inventory){
        if(inventory.getItemContainer().getId()==93) {
            if (!inventory.getItemContainer().contains(ItemID.COAL)) {
                if (!GlassSmeltingScript.coalBagEmpty) GlassSmeltingScript.coalBagEmpty = true;//TODO this sets the bag to empty when we're smithing and coal is added to our inventory.
            }
            if (inventory.getItemContainer().contains(ItemID.COAL)) {
                if (GlassSmeltingScript.coalBagEmpty) GlassSmeltingScript.coalBagEmpty = false;
            }
        }
    }

    @Override
    protected void startUp() throws AWTException {
        glassSmeltingScript.hasBeenFilled = false;
        if (overlayManager != null) {
            overlayManager.add(glassSmeltingOverlay);
        }
        glassSmeltingScript.run(config);
    }

    protected void shutDown() {
        glassSmeltingScript.hasBeenFilled = false;
        glassSmeltingScript.shutdown();
        overlayManager.remove(glassSmeltingOverlay);
    }
}
