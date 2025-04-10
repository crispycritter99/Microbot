package net.runelite.client.plugins.microbot.vardorvis;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.plugins.microbot.inventorysetups.InventorySetup;

@ConfigGroup("Vardorvis")
public interface VardorvisConfig extends Config {

    @ConfigItem(
            keyName = "inventorySetup",
            name = "Inventory Setup",
            description = "Inventory setup to use",
            position = 1
    )
    default InventorySetup inventorySetup() {
        return null;
    }
}
