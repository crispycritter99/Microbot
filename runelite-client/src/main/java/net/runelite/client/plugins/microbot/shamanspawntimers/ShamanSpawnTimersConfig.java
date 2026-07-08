package net.runelite.client.plugins.microbot.shamanspawntimers;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.Range;

@ConfigGroup("shamanSpawnTimers")
public interface ShamanSpawnTimersConfig extends Config
{
	@Range(min = 1, max = 20)
	@ConfigItem(
		keyName = "firstExplosionTicks",
		name = "First explosion",
		description = "Ticks from spawning until the lowest-index spawn explodes",
		position = 0
	)
	default int firstExplosionTicks()
	{
		return 7;
	}

	@Range(min = 0, max = 5)
	@ConfigItem(
		keyName = "explosionSpacingTicks",
		name = "Explosion spacing",
		description = "Ticks between each spawn in an index-ordered set of three",
		position = 1
	)
	default int explosionSpacingTicks()
	{
		return 2;
	}
}
