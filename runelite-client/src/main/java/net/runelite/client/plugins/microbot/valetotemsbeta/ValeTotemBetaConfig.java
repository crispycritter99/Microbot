package net.runelite.client.plugins.microbot.valetotemsbeta;

import net.runelite.client.config.*;

@ConfigGroup("valeTotemsBeta")
@ConfigInformation(
    "<html>" +
    "🤖 <b>VALE TOTEM BOT - SETUP GUIDE</b><br /><br />" +
    
    "🔴 <b>REQUIREMENTS:</b><br />" +
    "✅ Children of the Sun quest completed (Varlamore access)<br />" +
    "✅ Vale Totems miniquest completed<br />" +
    "✅ Fletching level 20+<br />" +
    "✅ Agility 45+ highly recommended (untested below 45)<br />" +
    "✅ Knife or Fletching knife in bank<br />" +
    "✅ logs in bank<br /><br />" +
    
    "📊 <b>FLETCHING REQUIREMENTS:</b><br />" +
    "• <b>Oak:</b> Shortbow (20) | Longbow (25)<br />" +
    "• <b>Willow:</b> Shortbow (35) | Longbow (40)<br />" +
    "• <b>Maple:</b> Shortbow (50) | Longbow (55)<br />" +
    "• <b>Yew:</b> Shortbow (65) | Longbow (70)<br />" +
    "• <b>Magic:</b> Shortbow (80) | Longbow (85)<br /><br />" +
    
    "🎯 <b>FEATURES:</b><br />" +
    "• Automatically plays Vale Totem minigame<br />" +
    "• <b>Strategy routes:</b> 8 with basket, 4-and-bank without, oak triangle<br />" +
    "• Automatically walks over ent trails for extra points and XP<br />" +
    "• Fletches while walking for efficiency<br />" +
    "• Auto banking and item management<br /><br />" +
    
    "⚠️ <b>CURRENT LIMITATIONS:</b><br />" +
    "• Beta keeps Withdraw-All for logs to avoid quantity-entry delays<br /><br />" +
    
    "🖥️ <b>CRITICAL REQUIREMENT:</b><br />" +
    "• <b>GPU Plugin</b> OR <b>117 HD Plugin</b> must be enabled<br />" +
    "• <b>Draw Distance:</b> Minimum 40<br />" +
    "• <i>Note: Bot clicks far distances to fletch while walking</i><br /><br />" +
    
    "🏁 <b>QUICK START:</b><br />" +
    "• Start anywhere with empty inventory (recommended)<br />" +
    "• Ensure desired logs and knife/fletching knife are in bank<br />" +
    "• <b>Equip desired gear before starting</b> (bot doesn't wield/unwield)<br />" +
    "• <b>Recommended:</b> Graceful gear for reduced run energy drain<br />" +
    "• Bot handles all navigation and banking automatically!<br /><br />" +

    "⚡ <b>STAMINA SUPPORT:</b><br />" +
    "• Enable 'Use Stamina Potions' to automatically drink stamina potions<br />" +
    "• Bot will withdraw potions from bank and drink when run energy is low<br />" +
    "• Stamina potions are prioritized over energy potions"
)
public interface ValeTotemBetaConfig extends Config {
    
    @ConfigItem(
            keyName = "logType",
            name = "Log Type",
            description = "Select which logs to use for fletching",
            position = 0
    )
    default LogType logType() {
        return LogType.YEW;
    }

    @ConfigItem(
            keyName = "bowType",
            name = "Bow Type",
            description = "Select whether to fletch shortbows or longbows",
            position = 1
    )
    default BowType bowType() {
        return BowType.LONGBOW;
    }

    @ConfigItem(
            keyName = "routeStrategy",
            name = "Route Strategy",
            description = "Auto follows the Wiki strategy: basket=8, oak=triangle, otherwise 4 and bank",
            position = 2
    )
    default RouteStrategy routeStrategy() {
        return RouteStrategy.AUTO;
    }

    @ConfigItem(
            keyName = "decorationSource",
            name = "Decoration Source",
            description = "Fletch while travelling or withdraw 16 premade decorations before filling remaining slots with logs",
            position = 5
    )
    default DecorationSource decorationSource() {
        return DecorationSource.FLETCH_ON_ROUTE;
    }

    @ConfigItem(
            keyName = "useBowStringSpool",
            name = "Use Bow String Spool",
            description = "String completed bows when a spool is available (not applicable to redwood)",
            position = 4
    )
    default boolean useBowStringSpool() {
        return false;
    }

    @ConfigItem(
            keyName = "collectOfferingsFrequency",
            name = "Offering Collection Frequency",
            description = "How often to collect offerings (every X rounds) - +- 1",
            position = 3
    )
    default int collectOfferingsFrequency() {
        return 5;
    }

    @ConfigSection(
            name = "Stamina",
            description = "Stamina potion settings",
            position = 10
    )
    String staminaSection = "stamina";

    @ConfigItem(
            keyName = "useStaminaPotions",
            name = "Use Stamina Potions",
            description = "Automatically drink stamina potions when run energy is low",
            position = 11,
            section = staminaSection
    )
    default boolean useStaminaPotions() {
        return false;
    }

    @ConfigItem(
            keyName = "staminaThreshold",
            name = "Drink At Energy %",
            description = "Drink stamina potion when run energy falls below this percentage",
            position = 12,
            section = staminaSection
    )
    @Range(min = 10, max = 90)
    default int staminaThreshold() {
        return 50;
    }

    enum LogType {
        OAK("Oak Logs", 20),
        WILLOW("Willow Logs", 35),
        MAPLE("Maple Logs", 50),
        YEW("Yew Logs", 65),
        MAGIC("Magic Logs", 80),
        REDWOOD("Redwood Logs", 90);

        private final String displayName;
        private final int requiredLevel;

        LogType(String displayName, int requiredLevel) {
            this.displayName = displayName;
            this.requiredLevel = requiredLevel;
        }

        public String getDisplayName() {
            return displayName;
        }

        public int getRequiredLevel() {
            return requiredLevel;
        }

        @Override
        public String toString() {
            return displayName + " (" + requiredLevel + " Fletching)";
        }
    }

    enum RouteStrategy {
        AUTO,
        FOUR_AND_BANK,
        FULL_EIGHT,
        OAK_TRIANGLE
    }

    enum DecorationSource {
        FLETCH_ON_ROUTE,
        PREMADE
    }

    enum BowType {
        SHORTBOW("Shortbow"),
        LONGBOW("Longbow");

        private final String displayName;

        BowType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }

        @Override
        public String toString() {
            return displayName;
        }
    }
}
