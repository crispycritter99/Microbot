package net.runelite.client.plugins.microbot.woodcutting.fossilteaks;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.events.ChatMessage;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

import javax.inject.Inject;
import java.awt.*;

@PluginDescriptor(
        name = PluginDescriptor.Mocrosoft + "Auto Woodcutting",
        description = "Microbot woodcutting plugin",
        tags = {"Woodcutting", "microbot", "skilling"},
        enabledByDefault = false
)
@Slf4j
public class TeakWoodcuttingPlugin extends Plugin {
    @Inject
    private TeakWoodcuttingConfig config;

    @Provides
    TeakWoodcuttingConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(TeakWoodcuttingConfig.class);
    }

    @Inject
    private OverlayManager overlayManager;
    @Inject
    private TeaklWoodcuttingOverlay woodcuttingOverlay;

    @Inject
    TeakWoodcuttingScript teakWoodcuttingScript;


    @Override
    protected void startUp() throws AWTException {
        if (overlayManager != null) {
            overlayManager.add(woodcuttingOverlay);
        }
        teakWoodcuttingScript.run(config);
    }

    protected void shutDown() {
        teakWoodcuttingScript.shutdown();
        overlayManager.remove(woodcuttingOverlay);
    }

    @Subscribe
    public void onChatMessage(ChatMessage chatMessage) {
        if (chatMessage.getType() == ChatMessageType.GAMEMESSAGE && chatMessage.getMessage().toLowerCase().contains("you can't light a fire here.")) {
            teakWoodcuttingScript.cannotLightFire = true;
        }
    }
}
