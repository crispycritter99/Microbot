package net.runelite.client.plugins.microbot.MoonsTest;

import net.runelite.client.config.*;
import net.runelite.client.plugins.microbot.MoonsTest.enums.State;

@ConfigGroup("MoonsTest")
public interface MoonsTestConfig extends Config {

    @ConfigSection(
            name = "General Settings",
            description = "General settings for the script",
            position = 0
    )
    String generalSettings = "generalSettings";

    @ConfigItem(
            keyName = "State",
            name = "State",
            description = "Choose state.",
            position = 0
    )
    default State getState()
    {
        return State.DEFAULT;
    }

    @ConfigItem(
            keyName = "foodAmount",
            name = "Food Amount",
            description = "Number of fish needed for a boss fight",
            position = 1,
            section = generalSettings
    )
    @Range(
            min = 1,
            max = 22
    )
    default int foodAmount() {
        return 8;
    }

    @ConfigItem(
            keyName = "prayerAmount",
            name = "Prayer Amount",
            description = "Number of prayer potions needed for a boss fight",
            position = 1,
            section = generalSettings
    )
    @Range(
            min = 1,
            max = 8
    )
    default int prayerAmount() {
        return 6;
    }
}
