package net.runelite.client.plugins.microbot.animatedarmour;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;

@ConfigGroup("animatedarmour")
public interface AnimatedArmourConfig extends Config {
            @ConfigItem(
            name = "Guide",
            keyName = "guide",
            position = 0,
            description = ""

    )
    default String guide() {
        return "Reanimates and kills armour in warriors guild, grabs tokens.\n" +
                " make sure you have armour (and optionally food) in inventory.\n" +
                "turn on auto retaliate and ground items runelite plugin";
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
