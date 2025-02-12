package net.runelite.client.plugins.microbot.degrime.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.runelite.api.ItemID;

@Getter
@RequiredArgsConstructor
public enum Herb {
    GUAM("Guam Leaf", "Grimy Guam Leaf", 0, 0, 0),
    MARRENTILL("Marrentill", "Grimy Marrentill",ItemID.UNCUT_OPAL, ItemID.OPAL, 1),
    TARROMIN("Tarromin", "Grimy Tarromin", ItemID.UNCUT_JADE, ItemID.JADE, 13),
    HARRALANDER("Harralander", "Grimy Harralander",ItemID.UNCUT_RED_TOPAZ, ItemID.RED_TOPAZ, 16),
    RANARR("Ranarr Weed", "Grimy Ranarr Weed", ItemID.UNCUT_SAPPHIRE, ItemID.SAPPHIRE, 20),
    TOADFLAX("Toadflax", "Grimy Toadflax", ItemID.UNCUT_EMERALD, ItemID.EMERALD, 27),
    IRIT("Irit", "Grimy Irit", ItemID.UNCUT_RUBY, ItemID.RUBY, 34),
    AVANTOE("Avantoe", "Grimy Avantoe", ItemID.UNCUT_DIAMOND, ItemID.DIAMOND, 43),
    KWUARM("Kwuarm", "Grimy Kwuarm", ItemID.UNCUT_DRAGONSTONE, ItemID.DRAGONSTONE, 55),
    HUASCA("Huasca", "Grimy Huasca", ItemID.UNCUT_ONYX, ItemID.ONYX, 67),
    SNAPDRAGON("Snapdragon", "Grimy Snapdragon", ItemID.UNCUT_ZENYTE, ItemID.ZENYTE, 89),
    CADANTINE("Cadantine", "Grimy Cadantine", ItemID.UNCUT_RUBY, ItemID.RUBY, 34),
    LANTADYME("Lantadyme", "Grimy Lantadyme", ItemID.UNCUT_DIAMOND, ItemID.DIAMOND, 43),
    DWARFWEED("Dwarf Weed", "Grimy Dwarf Weed", ItemID.UNCUT_DRAGONSTONE, ItemID.DRAGONSTONE, 55),
    TORSTOL("Torstol", "Grimy Torstol", ItemID.UNCUT_ONYX, ItemID.ONYX, 67);
    
    private final String grimyItemName;
    private final String cleanItemName;
    private final int grimyItemID;
    private final int cleanItemID;
    private final int levelRequired;
}