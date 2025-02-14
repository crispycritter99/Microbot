package net.runelite.client.plugins.microbot.SulphurNagua;

import net.runelite.client.config.*;
import net.runelite.client.plugins.microbot.SulphurNagua.enums.State;
import net.runelite.client.plugins.microbot.aiofighter.enums.PlayStyle;

@ConfigGroup("Moons")
public interface SulphurNaguaConfig extends Config {

    @ConfigSection(
            name = "General Settings",
            description = "General settings for the script",
            position = 0
    )
    String generalSettings = "generalSettings";
    @ConfigSection(
            name = "Combat",
            description = "Combat",
            position = 1,
            closedByDefault = false
    )
    String combatSection = "Combat";
    @ConfigSection(
            name = "Food & Potions",
            description = "Food & Potions",
            position = 2,
            closedByDefault = false
    )
    String foodAndPotionsSection = "Food & Potions";
    @ConfigSection(
            name = "Loot",
            description = "Loot",
            position = 3,
            closedByDefault = false
    )
    String lootSection = "Loot";

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
    @ConfigItem(
            keyName = "Combat",
            name = "Auto attack npc",
            description = "Attacks npc",
            position = 0,
            section = combatSection
    )
    default boolean toggleCombat() {
        return false;
    }

    @ConfigItem(
            keyName = "monster",
            name = "Attackable npcs",
            description = "List of attackable npcs",
            position = 1,
            section = combatSection
    )
    default String attackableNpcs() {
        return "";
    }

    @ConfigItem(
            keyName = "Attack Radius",
            name = "Attack Radius",
            description = "The max radius to attack npcs",
            position = 2,
            section = combatSection
    )
    default int attackRadius() {
        return 10;
    }

    @ConfigItem(
            keyName = "Use special attack",
            name = "Use special attack",
            description = "Use special attack",
            position = 3,
            section = combatSection
    )
    default boolean useSpecialAttack() {
        return false;
    }

    @ConfigItem(
            keyName = "Cannon",
            name = "Auto reload cannon",
            description = "Automatically reloads cannon",
            position = 4,
            section = combatSection
    )
    default boolean toggleCannon() {
        return false;
    }

    //safe spot
    @ConfigItem(
            keyName = "Safe Spot",
            name = "Safe Spot",
            description = "Shift Right-click the ground to select the safe spot tile",
            position = 5,
            section = combatSection
    )
    default boolean toggleSafeSpot() {
        return false;
    }

    //PlayStyle
    @ConfigItem(
            keyName = "PlayStyle",
            name = "Play Style",
            description = "Play Style",
            position = 6,
            section = combatSection
    )
    default PlayStyle playStyle() {
        return PlayStyle.AGGRESSIVE;
    }

    @ConfigItem(
            keyName = "ReachableNpcs",
            name = "Only attack reachable npcs",
            description = "Only attack npcs that we can reach with melee",
            position = 7,
            section = combatSection
    )
    default boolean attackReachableNpcs() {
        return true;
    }

    @ConfigItem(
            keyName = "Food",
            name = "Auto eat food",
            description = "Automatically eats food",
            position = 0,
            section = foodAndPotionsSection
    )
    default boolean toggleFood() {
        return false;
    }

    @ConfigItem(
            keyName = "Auto Prayer Potion",
            name = "Auto prayer potion",
            description = "Automatically drinks prayer potions",
            position = 1,
            section = foodAndPotionsSection
    )
    default boolean togglePrayerPotions() {
        return false;
    }
}
