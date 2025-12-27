package net.runelite.client.plugins.microbot.sailing;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

@ConfigGroup(SailingConfig.configGroup)
public interface SailingConfig extends Config {
	String configGroup = "micro-sailing";

	@ConfigSection(
		name = "General",
		description = "General Plugin Settings",
		position = 0
	)
	String generalSection = "general";

	@ConfigItem(
		keyName = "Salvgaging",
		name = "Salvgaging",
		description = "Enable this option to use salvaging.",
		position = 0,
		section = generalSection
	)
	default boolean salvaging()
	{
		return false;
	}

	@ConfigItem(
		keyName = "dropItems",
		name = "Drop items",
		description = "Comma-separated list of items to drop when salvaging.",
		position = 1,
		section = generalSection
	)
	default String dropItems()
	{
		return "";
	}

    @ConfigItem(
            keyName = "alchItems",
            name = "Alch items",
            description = "Comma-separated list of items to alch when salvaging.",
            position = 2,
            section = generalSection
    )
    default String alchItems()
    {
        return "";
    }
}
