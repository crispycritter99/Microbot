package net.runelite.client.plugins.microbot.valetotemsbeta.utils;

import net.runelite.api.ItemID;
import net.runelite.client.plugins.microbot.valetotemsbeta.ValeTotemBetaConfig;

/**
 * Utility class to map configuration enums to RuneLite item IDs
 */
public class FletchingItemMapper {

    /**
     * Get the item ID for the configured log type
     * @param logType the configured log type
     * @return the corresponding ItemID
     */
    public static int getLogItemId(ValeTotemBetaConfig.LogType logType) {
        switch (logType) {
            case OAK:
                return ItemID.OAK_LOGS;
            case WILLOW:
                return ItemID.WILLOW_LOGS;
            case MAPLE:
                return ItemID.MAPLE_LOGS;
            case YEW:
                return ItemID.YEW_LOGS;
            case MAGIC:
                return ItemID.MAGIC_LOGS;
            case REDWOOD:
                return ItemID.REDWOOD_LOGS;
            default:
                return ItemID.YEW_LOGS; // fallback
        }
    }

    /**
     * Get the item ID for the configured bow type
     * @param logType the configured log type
     * @param bowType the configured bow type
     * @return the corresponding unstrung bow ItemID
     */
    public static int getBowItemId(ValeTotemBetaConfig.LogType logType, ValeTotemBetaConfig.BowType bowType) {
        if (logType == ValeTotemBetaConfig.LogType.REDWOOD) {
            return ItemID.REDWOOD_HIKING_STAFF;
        }
        if (bowType == ValeTotemBetaConfig.BowType.SHORTBOW) {
            switch (logType) {
                case OAK:
                    return ItemID.OAK_SHORTBOW_U;
                case WILLOW:
                    return ItemID.WILLOW_SHORTBOW_U;
                case MAPLE:
                    return ItemID.MAPLE_SHORTBOW_U;
                case YEW:
                    return ItemID.YEW_SHORTBOW_U;
                case MAGIC:
                    return ItemID.MAGIC_SHORTBOW_U;
                default:
                    return ItemID.YEW_SHORTBOW_U; // fallback
            }
        } else { // LONGBOW
            switch (logType) {
                case OAK:
                    return ItemID.OAK_LONGBOW_U;
                case WILLOW:
                    return ItemID.WILLOW_LONGBOW_U;
                case MAPLE:
                    return ItemID.MAPLE_LONGBOW_U;
                case YEW:
                    return ItemID.YEW_LONGBOW_U;
                case MAGIC:
                    return ItemID.MAGIC_LONGBOW_U;
                default:
                    return ItemID.YEW_LONGBOW_U; // fallback
            }
        }
    }

    public static int getStrungBowItemId(ValeTotemBetaConfig.LogType logType, ValeTotemBetaConfig.BowType bowType) {
        if (logType == ValeTotemBetaConfig.LogType.REDWOOD) {
            return ItemID.REDWOOD_HIKING_STAFF;
        }
        if (bowType == ValeTotemBetaConfig.BowType.SHORTBOW) {
            switch (logType) {
                case OAK: return ItemID.OAK_SHORTBOW;
                case WILLOW: return ItemID.WILLOW_SHORTBOW;
                case MAPLE: return ItemID.MAPLE_SHORTBOW;
                case YEW: return ItemID.YEW_SHORTBOW;
                case MAGIC: return ItemID.MAGIC_SHORTBOW;
                default: return ItemID.YEW_SHORTBOW;
            }
        }
        switch (logType) {
            case OAK: return ItemID.OAK_LONGBOW;
            case WILLOW: return ItemID.WILLOW_LONGBOW;
            case MAPLE: return ItemID.MAPLE_LONGBOW;
            case YEW: return ItemID.YEW_LONGBOW;
            case MAGIC: return ItemID.MAGIC_LONGBOW;
            default: return ItemID.YEW_LONGBOW;
        }
    }

    public static String getCombinationOptionText(ValeTotemBetaConfig.LogType logType,
                                                   ValeTotemBetaConfig.BowType bowType) {
        if (logType == ValeTotemBetaConfig.LogType.REDWOOD) {
            return "Redwood hiking staff";
        }
        return logType.getDisplayName().replace(" Logs", "") + " "
                + bowType.getDisplayName().toLowerCase();
    }

    /**
     * Get a human-readable description of the configured fletching setup
     * @param logType the configured log type
     * @param bowType the configured bow type
     * @return formatted description string
     */
    public static String getFletchingDescription(ValeTotemBetaConfig.LogType logType, ValeTotemBetaConfig.BowType bowType) {
        if (logType == ValeTotemBetaConfig.LogType.REDWOOD) {
            return "Redwood hiking staff";
        }
        return String.format("%s %s (u)", logType.getDisplayName().replace(" Logs", ""), bowType.getDisplayName());
    }

} 
