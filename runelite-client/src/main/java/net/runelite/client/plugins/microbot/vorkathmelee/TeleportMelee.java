package net.runelite.client.plugins.microbot.vorkathmelee;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TeleportMelee {
    VARROCK_TAB("Varrock teleport", "break"),
    CRAFTING_CAPE("Crafting Cape", "teleport");


    ;private final String itemName;
    private final String action;

    @Override
    public String toString()
    {
        return itemName;
    }
}
