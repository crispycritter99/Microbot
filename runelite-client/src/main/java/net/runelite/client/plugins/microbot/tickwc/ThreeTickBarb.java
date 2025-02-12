package net.runelite.client.plugins.microbot.tickwc;


import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.GameTick;
import net.runelite.api.widgets.Widget;
import net.runelite.client.Notifier;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.FishingSpot;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.microbot.Microbot;

import net.runelite.client.plugins.microbot.util.antiban.Rs2Antiban;
import net.runelite.client.plugins.microbot.util.camera.Rs2Camera;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.mouse.VirtualMouse;
import net.runelite.client.plugins.microbot.util.npc.Rs2Npc;
import net.runelite.client.ui.overlay.OverlayManager;

import javax.inject.Inject;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static net.runelite.client.plugins.microbot.util.Global.sleep;
import static net.runelite.client.plugins.microbot.util.Global.sleepUntilOnClientThread;
import static net.runelite.client.plugins.microbot.util.npc.Rs2Npc.validateInteractable;


@PluginDescriptor(
        name = PluginDescriptor.Mocrosoft + "1.5T wc",
        description = "Performs 3T Barb Fishing flawlessly",
        enabledByDefault = false
)
@Slf4j
public class ThreeTickBarb extends Plugin {
    @Inject
    private Client client;
    @Inject
    private ClientThread clientThread;
    @Inject
    private OverlayManager overlayManager;
    @Inject
    private ThreeTickBarbOverlay threeTickBarbOverlay;
    @Inject
    private Notifier notifier;

    private boolean enabled;
    private boolean inProgress;

    private final ExecutorService executor = Executors.newFixedThreadPool(1);

    ThreeTickFishingState state = ThreeTickFishingState.Idle;

    @Provides
    ThreeTickBarbConfig getConfig(ConfigManager manager) {
        return manager.getConfig(ThreeTickBarbConfig.class);
    }

    @Override
    protected void startUp() {
        if (Microbot.getClient().getGameState() == GameState.LOGGED_IN) {
            Microbot.pauseAllScripts = false;
            Microbot.setClient(client);
            Microbot.setClientThread(clientThread);
            Microbot.setNotifier(notifier);
            Microbot.setMouse(new VirtualMouse());

            enabled = true;

            if (overlayManager != null) {
                overlayManager.add(threeTickBarbOverlay);
            }
        }
    }

    @Subscribe
    public void onGameTick(GameTick gameTick) {
        if (enabled && !inProgress) {

            switch (state) {
                case UseGuam:
                    executor.submit(this::useGuam);
                    break;
                case UseTarAndDrop:
                    executor.submit(this::useTarAndDropFish);
                    break;
                case ClickFishingSpot:
                case Idle:
                    executor.submit(this::locateFishingSpot);
                    break;
                case LocatingFishingSpot:
                    break;
                default:
                    notifier.notify("ThreeTickBarb stopped unexpectedly!");
                    break;
            }
        }
    }

    private void useGuam() {
        inProgress = true;
        sleep(13, 167);
//        Widget guamLeafWidget = Rs2Inventory.findItem("Guam leaf");
//        Microbot.getMouse().click(guamLeafWidget.getBounds());
        Rs2Inventory.interact("guam leaf","use");
        state = ThreeTickFishingState.UseTarAndDrop;
        inProgress = false;
    }

    private void useTarAndDropFish() {
        inProgress = true;
        sleep(18, 132);


        Rs2Inventory.interact(ItemID.SWAMP_TAR, "Use");

        Rs2Inventory.dropAll(ItemID.LEAPING_TROUT, ItemID.LEAPING_STURGEON, ItemID.LEAPING_SALMON);


        state = ThreeTickFishingState.ClickFishingSpot;
        inProgress = false;
    }

    private void clickFishingSpot() {
        inProgress = true;
        sleep(23, 213);


//        Rs2Npc.interact(fishingSpot, "Use-rod");

        state = ThreeTickFishingState.UseGuam;
        inProgress = false;
    }

    private void locateFishingSpot() {
        inProgress = true;
        sleep(11, 254);

        for (int fishingSpotId : FishingSpot.BARB_FISH.getIds()) {
            NPC fishingspot = Rs2Npc.getNpc(fishingSpotId);
            if (!Rs2Camera.isTileOnScreen(fishingspot.getLocalLocation())) {
                validateInteractable(fishingspot);
            }
            if (Rs2Npc.interact(fishingspot)) {
                break;
            }
        }




        state = ThreeTickFishingState.UseGuam;
        inProgress = false;
    }

    @Override
    protected void shutDown() {
        enabled = false;

        state = ThreeTickFishingState.Idle;
        overlayManager.remove(threeTickBarbOverlay);
    }

    private boolean isFish(Widget inventoryItem) {
        return inventoryItem.getItemId() == 11328 ||
                inventoryItem.getItemId() == 11330 ||
                inventoryItem.getItemId() == 11332;
    }

    private NPC getFishingSpot() {
        return Rs2Npc.getNpc("Fishing spot");
    }
}