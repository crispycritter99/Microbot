package net.runelite.client.plugins.microbot.salvaging;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("salvage")
public interface SalvageConfig extends Config {
    @ConfigItem(
            name = "List of Items to Drop",
            keyName = "listOfItemsToDrop",
            position = 1,
            description = "List of items to drop",
            section = "lootSection"
    )
    default String listOfItemsToLoot() {
        return "brimstone key";
    }
    @ConfigItem(
            name = "List of Items to Alch",
            keyName = "listOfItemsToalch",
            position = 2,
            description = "List of items to alch",
            section = "lootSection"
    )
    default String listOfItemsToAlch() {
        return "brimstone key";
    }
/*    @ConfigItem(
            keyName = "Ore",
            name = "Ore",
            description = "Choose the ore",
            position = 0
    )
    default List<String> ORE()
    {
        return Rocks.TIN;
    }*/
}
