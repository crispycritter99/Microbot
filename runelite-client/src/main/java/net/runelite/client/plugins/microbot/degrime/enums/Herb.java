package net.runelite.client.plugins.microbot.degrime.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.runelite.api.ItemID;

@Getter
@RequiredArgsConstructor
public enum Herb {
    GUAM("Guam Leaf", "Grimy Guam Leaf", ItemID.GRIMY_GUAM_LEAF, ItemID.GUAM_LEAF, 3),
    MARRENTILL("Marrentill", "Grimy Marrentill",ItemID.GRIMY_MARRENTILL, ItemID.MARRENTILL, 5),
    TARROMIN("Tarromin", "Grimy Tarromin", ItemID.GRIMY_TARROMIN, ItemID.TARROMIN, 11),
    HARRALANDER("Harralander", "Grimy Harralander",ItemID.GRIMY_HARRALANDER, ItemID.HARRALANDER, 20),
    RANARR("Ranarr Weed", "Grimy Ranarr Weed", ItemID.GRIMY_RANARR_WEED, ItemID.RANARR_WEED, 25),
    TOADFLAX("Toadflax", "Grimy Toadflax", ItemID.GRIMY_TOADFLAX, ItemID.TOADFLAX, 30),
    IRIT("Irit", "Grimy Irit", ItemID.GRIMY_IRIT_LEAF, ItemID.IRIT_LEAF, 40),
    AVANTOE("Avantoe", "Grimy Avantoe", ItemID.GRIMY_AVANTOE, ItemID.AVANTOE, 48),
    KWUARM("Kwuarm", "Grimy Kwuarm", ItemID.GRIMY_KWUARM, ItemID.KWUARM, 54),
    HUASCA("Huasca", "Grimy Huasca", ItemID.GRIMY_HUASCA, ItemID.HUASCA, 58),
    SNAPDRAGON("Snapdragon", "Grimy Snapdragon", ItemID.GRIMY_SNAPDRAGON, ItemID.SNAPDRAGON, 59),
    CADANTINE("Cadantine", "Grimy Cadantine", ItemID.GRIMY_CADANTINE, ItemID.CADANTINE, 65),
    LANTADYME("Lantadyme", "Grimy Lantadyme", ItemID.GRIMY_LANTADYME, ItemID.LANTADYME, 67),
    DWARFWEED("Dwarf Weed", "Grimy Dwarf Weed", ItemID.GRIMY_DWARF_WEED, ItemID.DWARF_WEED, 70),
    TORSTOL("Torstol", "Grimy Torstol", ItemID.GRIMY_TORSTOL, ItemID.TORSTOL, 75);
    
    private final String grimyItemName;
    private final String cleanItemName;
    private final int grimyItemID;
    private final int cleanItemID;
    private final int levelRequired;
}