package net.runelite.client.plugins.microbot.falconry;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.plugins.microbot.util.walker.enums.Salamanders;

@Getter
@RequiredArgsConstructor
public enum SalamanderLocalHunting {


    DARK("Dark kebbit", 9341, Salamanders.GREEN_SALAMANDER.getWorldPoint()),
    SPOTTED("Spotted kebbit", 8732, Salamanders.ORANGE_SALAMANDER.getWorldPoint()),
    DASHING("Dashing kebbit", 8990, Salamanders.RED_SALAMANDER.getWorldPoint());

    private final String name;
    private final int treeId;
    private final WorldPoint huntingPoint;

    @Override
    public String toString() {
        return name;
    }
}