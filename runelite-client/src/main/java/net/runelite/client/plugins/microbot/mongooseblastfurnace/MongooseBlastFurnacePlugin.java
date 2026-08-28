package net.runelite.client.plugins.microbot.mongooseblastfurnace;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.ItemContainer;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.api.gameval.ItemID;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.microbot.PluginConstants;
import net.runelite.client.plugins.microbot.mongooseblastfurnace.enums.State;
import net.runelite.client.ui.overlay.OverlayManager;

import javax.inject.Inject;
import java.awt.*;

import static net.runelite.client.plugins.microbot.mongooseblastfurnace.MongooseBlastFurnaceScript.*;


@PluginDescriptor(
        name = "MongooseBlastFurnace",
        description = "Storm's MongooseBlastFurnace plugin",
        tags = {"microbot", "smithing", "bar", "ore", "blast", "furnace"},
        authors = {"Storm"},
        version = MongooseBlastFurnacePlugin.version,
        minClientVersion = "2.1.0",
        enabledByDefault = PluginConstants.DEFAULT_ENABLED,
        isExternal = PluginConstants.IS_EXTERNAL,
        iconUrl = "https://chsami.github.io/Microbot-Hub/MongooseBlastFurnacePlugin/assets/icon.jpg",
        cardUrl = "https://chsami.github.io/Microbot-Hub/MongooseBlastFurnacePlugin/assets/card.jpg"
)
@Slf4j
public class MongooseBlastFurnacePlugin extends Plugin {
    final static String version = "1.2.2";
    @Inject
    private MongooseBlastFurnaceConfig config;

    @Provides
    MongooseBlastFurnaceConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(MongooseBlastFurnaceConfig.class);
    }

    @Inject
    private OverlayManager overlayManager;
    @Inject
    private MongooseBlastFurnaceOverlay mongooseBlastFurnaceOverlay;

    @Inject
    MongooseBlastFurnaceScript mongooseBlastFurnaceScript;

    @Override
    protected void startUp() throws AWTException {
        if (overlayManager != null) {
            overlayManager.add(mongooseBlastFurnaceOverlay);
        }
        mongooseBlastFurnaceScript.run();
    }

    @Subscribe
    public void onChatMessage(ChatMessage chatMessage) {
        if (chatMessage.getType() == ChatMessageType.GAMEMESSAGE) {
            if (chatMessage.getMessage().contains("The coal bag is now empty.")) {
                if (!coalBagEmpty) coalBagEmpty = true;
            }

            if (chatMessage.getMessage().contains("The coal bag contains")) {
                if (coalBagEmpty) coalBagEmpty = false;
            }
        }
    }

    @Subscribe
    public void onItemContainerChanged(ItemContainerChanged inventory) {
        if (inventory.getItemContainer().getId() != 93) return;

        final ItemContainer inv = inventory.getItemContainer();
        final boolean hasCoal = inv.contains(ItemID.COAL);
        final boolean hasPrimary = inv.contains(config.getBars().getPrimaryOre());
        final boolean hasSecondary = inv.contains(config.getBars().getSecondaryOre());

        // Coal bag tracking
        if (state != State.BANKING && !hasCoal && !coalBagEmpty) coalBagEmpty = true;
        if (state != State.SMITHING && hasCoal && coalBagEmpty) coalBagEmpty = false;

        // Primary ore tracking
        if (state != State.SMITHING && hasPrimary && primaryOreEmpty) primaryOreEmpty = false;
        if (state != State.BANKING && !hasPrimary && !primaryOreEmpty) primaryOreEmpty = true;

        // Secondary ore tracking
        if (state != State.SMITHING && hasSecondary && secondaryOreEmpty) {
            secondaryOreEmpty = false;
            System.out.println("secondary set to not empty"); // Optional debug
        }
        if (state != State.BANKING && !hasSecondary && !secondaryOreEmpty) secondaryOreEmpty = true;
    }

    protected void shutDown() {
        mongooseBlastFurnaceScript.shutdown();
        overlayManager.remove(mongooseBlastFurnaceOverlay);
    }
}
