package net.runelite.client.plugins.microbot.mongooseamethystminer;

import com.google.inject.Provides;
import net.runelite.api.gameval.ObjectID;
import net.runelite.api.WallObject;
import net.runelite.api.events.WallObjectSpawned;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.microbot.PluginConstants;
import net.runelite.client.plugins.microbot.mongooseamethystminer.enums.Status;
import net.runelite.client.ui.overlay.OverlayManager;

import javax.inject.Inject;
import java.awt.*;

@PluginDescriptor(
        name = PluginDescriptor.See1Duck + " Mongoose Amethyst Miner",
        description = "Automates mining amethyst in the mining guild",
        tags = {"mining", "amethyst", "mining guild"},
        version = MongooseAmethystMiningPlugin.version,
        minClientVersion = "2.1.0",
        cardUrl = "",
        iconUrl = "",
        enabledByDefault = PluginConstants.DEFAULT_ENABLED,
        isExternal = PluginConstants.IS_EXTERNAL
)
public class MongooseAmethystMiningPlugin extends Plugin {
    public static final String version = "1.2.4";
    @Inject
    private OverlayManager overlayManager;
    @Inject
    private MongooseAmethystMiningOverlay amethystMiningOverlay;
    @Inject
    private MongooseAmethystMiningScript amethystMiningScript;
    @Inject
    private MongooseAmethystMiningConfig amethystMiningConfig;

    @Provides
    MongooseAmethystMiningConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(MongooseAmethystMiningConfig.class);
    }

    @Override
    protected void startUp() throws AWTException {
        overlayManager.add(amethystMiningOverlay);
        amethystMiningScript.run(amethystMiningConfig);
    }

    @Subscribe
    public void onConfigChanged(ConfigChanged event) {
        if (event.getGroup().equals("MongooseAmethystMining")) {
            if (event.getKey().equals("gemBag")) {
                if (amethystMiningConfig.gemBag()) {
                    MongooseAmethystMiningScript.itemsToKeep.add(MongooseAmethystMiningScript.gemBag);
                } else {
                    MongooseAmethystMiningScript.itemsToKeep.remove(MongooseAmethystMiningScript.gemBag);
                }
                if (amethystMiningConfig.gemBag()) {
                    MongooseAmethystMiningScript.itemsToKeep.add(MongooseAmethystMiningScript.openGemBag);
                } else {
                    MongooseAmethystMiningScript.itemsToKeep.remove(MongooseAmethystMiningScript.openGemBag);
                }
            }
            if (event.getKey().equals("chiselAmethysts")) {
                if (amethystMiningConfig.chiselAmethysts()) {
                    MongooseAmethystMiningScript.itemsToKeep.add(MongooseAmethystMiningScript.chisel);
                } else {
                    MongooseAmethystMiningScript.itemsToKeep.remove(MongooseAmethystMiningScript.chisel);
                }
            }
        }
    }

    @Subscribe
    public void onWallObjectSpawned(WallObjectSpawned event) {
        WallObject wallObject = event.getWallObject();
        if (wallObject == null)
            return;
        if (MongooseAmethystMiningScript.status == Status.MINING && wallObject.getId() == ObjectID.AMETHYSTROCK_EMPTY) {
            if (MongooseAmethystMiningScript.oreVein != null) {
                if (wallObject.getWorldLocation().equals(MongooseAmethystMiningScript.oreVein.getWorldLocation())) {
                    MongooseAmethystMiningScript.oreVein = null;
                }
            }
        }
    }

    protected void shutDown() {
        amethystMiningScript.shutdown();
        overlayManager.remove(amethystMiningOverlay);
    }
}
