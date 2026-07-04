package net.runelite.client.plugins.microbot.fastAIOFighter;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup(FastAIOFighterConfig.GROUP)
public interface FastAIOFighterConfig extends Config {
    String GROUP = "fastAIOFighter";
    String ATTACKABLE_NPCS_KEY = "monster";

    @ConfigItem(
            keyName = ATTACKABLE_NPCS_KEY,
            name = "Attackable NPCs",
            description = "Comma-separated list of NPC names to attack",
            position = 0
    )
    default String attackableNpcs() {
        return "kalphite worker";
    }
}