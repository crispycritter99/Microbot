package net.runelite.client.plugins.microbot.frostDragonKiller;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup(FrostDragonKillerConfig.GROUP)
public interface FrostDragonKillerConfig extends Config {
    String GROUP = "frostDragonKiller";
    String LOOT_ITEMS_KEY = "lootItems";

    @ConfigItem(
            keyName = LOOT_ITEMS_KEY,
            name = "Items to loot",
            description = "Comma-separated item names or partial names to loot",
            position = 0
    )
    default String lootItems() {
        return "bones, sheet, dra, rune";
    }
}
