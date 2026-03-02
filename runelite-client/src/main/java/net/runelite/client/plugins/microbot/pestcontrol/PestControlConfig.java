package net.runelite.client.plugins.microbot.pestcontrol;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigInformation;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.plugins.microbot.inventorysetups.InventorySetup;
import net.runelite.client.plugins.microbot.util.misc.SpecialAttackWeaponEnum;

@ConfigGroup("pestcontrol")
@ConfigInformation("Start near a boat of your combat level")
public interface PestControlConfig extends Config {
    @ConfigItem(
            keyName = "NPC Priority 1",
            name = "NPC Priority 1",
            description = "What npc to attack as first option",
            position = 2
    )
    default PestControlNpc Priority1() {
        return PestControlNpc.PORTAL;
    }
    @ConfigItem(
            keyName = "NPC Priority 2",
            name = "NPC Priority 2",
            description = "What npc to attack as second option",
            position = 3
    )
    default PestControlNpc Priority2() {
        return PestControlNpc.SPINNER;
    }
    @ConfigItem(
            keyName = "NPC Priority 3",
            name = "NPC Priority 3",
            description = "What npc to attack as third option",
            position = 4
    )
    default PestControlNpc Priority3() {
        return PestControlNpc.BRAWLER;
    }

    @ConfigItem(
            keyName = "Alch in boat",
            name = "Alch while waiting",
            description = "Alch while waiting",
            position = 5
    )
    default boolean alchInBoat() {
        return false;
    }

    @ConfigItem(
            keyName = "itemToAlch",
            name = "Item to alch",
            description = "Item to alch",
            position = 6
    )
    default String alchItem() {
        return "";
    }

    @ConfigItem(
            keyName = "QuickPrayer",
            name = "Enable QuickPrayer",
            description = "Enables quick prayer",
            position = 7
    )
    default boolean quickPrayer() {
        return false;
    }

    @ConfigItem(
            keyName = "Use special attack",
            name = "Use special attack",
            description = "Use special attack",
            position = 8

    )
    default boolean useSpecialAttack() {
        return false;
    }

    @ConfigItem(
            keyName = "Spec weapon",
            name = "Spec weapon",
            description = "Special attack weapon to use",
            position = 10

    )
    default SpecialAttackWeaponEnum specWeapon() {
        return null;
    }

    @ConfigItem(
            keyName = "inventorySetup",
            name = "Inventory Setup",
            description = "Inventory setup to use",
            position = 11
    )
    default InventorySetup inventorySetup() {
        return null;
    }
    @ConfigItem(
            keyName = "World",
            name = "World",
            description = "Pest Control world",
            position = 12
    )

    default int world() {
        return 344;
    }
}
