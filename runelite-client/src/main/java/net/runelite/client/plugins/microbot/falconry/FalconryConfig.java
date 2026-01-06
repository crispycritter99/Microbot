package net.runelite.client.plugins.microbot.falconry;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.plugins.microbot.falconry.SalamanderLocalHunting;

@ConfigGroup("falconry")
public interface FalconryConfig extends Config {


    @ConfigItem(
            position = 0,
            keyName = "salamanderHunting",
            name = "Salamander to hunt",
            description = "Select which salamander to hunt"
    )
    default SalamanderLocalHunting salamanderHunting() {
        return SalamanderLocalHunting.DARK;
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
