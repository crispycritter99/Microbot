package net.runelite.client.plugins.microbot.tithefarmfast;


import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.Item;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameObjectSpawned;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.client.Notifier;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.tithefarmfast.enums.TitheFarmMaterial;
import net.runelite.client.plugins.microbot.tithefarmfast.enums.TitheFarmStateFast;
import net.runelite.client.plugins.microbot.tithefarmfast.models.TitheFarmPlant;
import net.runelite.client.plugins.microbot.util.mouse.VirtualMouse;
import net.runelite.client.ui.overlay.OverlayManager;

import javax.inject.Inject;
import java.awt.*;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@PluginDescriptor(
        name = PluginDescriptor.Mocrosoft + "Tithe Farm Fast",
        description = "Plays the Tithe farm minigame for you!",
        tags = {"tithe farming", "microbot", "skills", "minigame"},
        enabledByDefault = false
)
@Slf4j
public class TitheFarmingFastPlugin extends Plugin {

    @Inject
    public TitheFarmingFastConfig config;

    @Provides
    TitheFarmingFastConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(TitheFarmingFastConfig.class);
    }

    @Inject
    private Client client;
    @Inject
    private ClientThread clientThread;
    @Inject
    private Notifier notifier;
    @Inject
    private OverlayManager overlayManager;
    @Inject
    private TitheFarmingFastOverlay titheFarmOverlay;

    private final TitheFarmingFastScript titheFarmScript = new TitheFarmingFastScript();

    @Override
    protected void startUp() throws AWTException {
        Microbot.pauseAllScripts = false;
        Microbot.setClient(client);
        Microbot.setClientThread(clientThread);
        Microbot.setNotifier(notifier);
        Microbot.setMouse(new VirtualMouse());
        if (overlayManager != null) {
            overlayManager.add(titheFarmOverlay);
        }
        titheFarmScript.run(config);
    }

    protected void shutDown() {
        titheFarmScript.shutdown();
        overlayManager.remove(titheFarmOverlay);
    }

    @Subscribe
    public void onGameObjectSpawned(GameObjectSpawned event) {
        for (TitheFarmPlant plant : TitheFarmingFastScript.plants) {
            if (event.getGameObject().getWorldLocation().equals(plant.getGameObject().getWorldLocation())) {
                plant.setGameObject(event.getGameObject());
            }
        }
    }

    @Subscribe
    public void onItemContainerChanged(ItemContainerChanged event) {
        if (TitheFarmMaterial.getSeedForLevel() != null) {
            Item fruit = Arrays.stream(event.getItemContainer().getItems()).filter(x -> x.getId() == TitheFarmMaterial.getSeedForLevel().getFruitId()).findFirst().orElse(null);
            if (fruit != null) {
                TitheFarmingFastScript.fruits = fruit.getQuantity() - TitheFarmingFastScript.initialFruit;
            }
        }
    }

    @Subscribe
    public void onChatMessage(ChatMessage chatMessage)
    {
        String message = chatMessage.getMessage();
        if (message.contains("%")) {
            Pattern pattern = Pattern.compile("(\\d+(\\.\\d+)?)%");
            Matcher matcher = pattern.matcher(message);

            if (matcher.find()) {
                String percentage = matcher.group(1);
                TitheFarmingFastScript.gricollerCanCharges = (int) (Float.parseFloat(percentage));
            }
        } else if (message.equalsIgnoreCase("Gricoller's can is already full.")) {
            TitheFarmingFastScript.gricollerCanCharges = 100;
        } else if (message.equalsIgnoreCase("You don't have a suitable vessel of water for watering the plant.")) {
            TitheFarmingFastScript.state = TitheFarmStateFast.REFILL_WATERCANS;
        }
    }
}
