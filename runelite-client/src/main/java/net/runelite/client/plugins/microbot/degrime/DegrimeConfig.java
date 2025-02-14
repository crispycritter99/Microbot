package net.runelite.client.plugins.microbot.degrime;

import net.runelite.client.config.*;
import net.runelite.client.plugins.microbot.degrime.DegrimeConfig;
import net.runelite.client.plugins.microbot.degrime.enums.*;

@ConfigGroup(DegrimeConfig.configGroup)
@ConfigInformation(
        "• This plugin is an all-in-one jewelry crafting plugin. <br />" +
                "• Select the jewelry you would like to craft in the config <br />" +
                "• Prepare bank with all required items & runes <br />"
)
public interface DegrimeConfig extends Config {

    String configGroup = "micro-herb";
    String jewelry = "jewelry";
    String craftingLocation = "craftingLocation";
    String completionAction = "completionAction";
    String staff = "staff";
    String useRunePouch = "useRunePouch";
    String useCutGems = "useCutGems";

    @ConfigSection(
            name = "General",
            description = "Configure general settings for the plugin",
            position = 0
    )
    String generalSection = "general";

    @ConfigSection(
            name = "Magic",
            description = "Configure settings for magic",
            position = 1
    )
    String magicSection = "magic";

    @ConfigItem(
            keyName = jewelry,
            name = "Item",
            description = "Chose the jewelry item you would like to craft",
            position = 0,
            section = generalSection
    )
    default Herb Herb() { return Herb.GUAM; }


    @ConfigItem(
            keyName = useRunePouch,
            name = "Use RunePouch",
            description = "Should withdraw & check runes in the rune pouch (must be loaded)",
            position = 0,
            section = magicSection
    )
    default boolean useRunePouch() { return true; }
}
