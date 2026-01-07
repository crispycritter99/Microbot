package net.runelite.client.plugins.microbot.HunterRumours;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("hunterRumours")
public interface HunterRumoursConfig extends Config
{
    @ConfigItem(
            position = 0,
            keyName = "currentRumour",
            name = "Current rumour type",
            description = "Which Hunter Rumour you currently have. " +
                    "AUTO is a placeholder if you later implement automatic detection."
    )
    default HunterRumourTaskType currentRumour()
    {
        return HunterRumourTaskType.FALCONRY;
    }

    @ConfigItem(
            position = 1,
            keyName = "falconryCompletionItem",
            name = "Falconry completion item (name)",
            description = "Exact name of the item that proves the Falconry rumour is complete " +
                    "(e.g. \"Kebbit claw\" / whatever your rumour requires)."
    )
    default String falconryCompletionItem()
    {
        return "";
    }

    @ConfigItem(
            position = 2,
            keyName = "salamanderCompletionItem",
            name = "Salamander completion item (name)",
            description = "Exact name of the salamander-related rumour completion item."
    )
    default String salamanderCompletionItem()
    {
        return "";
    }

    @ConfigItem(
            position = 3,
            keyName = "chinCompletionItem",
            name = "Chinchompa completion item (name)",
            description = "Exact name of the chinchompa (or other box-trap target) rumour completion item."
    )
    default String chinCompletionItem()
    {
        return "";
    }

    @ConfigItem(
            position = 4,
            keyName = "useQuickDialogue",
            name = "Use quick dialogue (space / option 1)",
            description = "If enabled, the plugin will spam space/option 1 when talking to Wolf to turn in / get rumours."
    )
    default boolean useQuickDialogue()
    {
        return true;
    }
}
