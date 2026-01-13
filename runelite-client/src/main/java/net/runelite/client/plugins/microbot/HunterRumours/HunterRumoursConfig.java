package net.runelite.client.plugins.microbot.HunterRumours;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.Range;
import net.runelite.client.plugins.microbot.salamanderslocal.SalamanderLocalHunting;

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
    @ConfigItem(
            position = 0,
            keyName = "salamanderHunting",
            name = "Salamander to hunt",
            description = "Select which salamander to hunt"
    )
    default net.runelite.client.plugins.microbot.salamanderslocal.SalamanderLocalHunting salamanderHunting() {
        return SalamanderLocalHunting.GREEN;
    }

    @ConfigItem(
            position = 1,
            keyName = "progressiveHunting",
            name = "Automatically select best salamander to hunt.",
            description = "This will override the selected salamander. Furthermore, it will move you to the next location when you meet the requirements."
    )
    default boolean progressiveHunting() {
        return false;
    }

    @ConfigItem(
            position = 2,
            keyName = "showOverlay",
            name = "Show Overlay",
            description = "Displays overlay with traps and status"
    )
    default boolean showOverlay() {
        return true;
    }

    @ConfigItem(
            position = 3,
            keyName = "withdrawNumber",
            name = "Number of nets/ropes to withdraw",
            description = "Number of nets/ropes to withdraw from bank"
    )
    @Range(
            min = 3,
            max = 13
    )
    default int withdrawNumber() {
        return 8;
    }

    @ConfigItem(
            position = 4,
            keyName = "MinSleepAfterCatch",
            name = "Min. Sleep After Catch - Recommended minimum 7500ms",
            description = "Min sleep after catch"
    )
    default int minSleepAfterCatch() {
        return 7500;
    }

    @ConfigItem(
            position = 5,
            keyName = "MaxSleepAfterCatch",
            name = "Max. Sleep After Catch",
            description = "Max sleep after catch"
    )
    default int maxSleepAfterCatch() {
        return 8400;
    }

    @ConfigItem(
            position = 6,
            keyName = "MinSleepAfterLay",
            name = "Min. Sleep After Lay - Recommended minimum 4000ms",
            description = "Min sleep after lay"
    )
    default int minSleepAfterLay() {
        return 4000;
    }

    @ConfigItem(
            position = 7,
            keyName = "MaxSleepAfterLay",
            name = "Max. Sleep After Lay",
            description = "Max sleep after lay"
    )
    default int maxSleepAfterLay() {
        return 5400;
    }
}
