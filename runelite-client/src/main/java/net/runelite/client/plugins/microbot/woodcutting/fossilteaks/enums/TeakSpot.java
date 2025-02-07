package net.runelite.client.plugins.microbot.woodcutting.fossilteaks.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.runelite.api.coords.WorldPoint;

import java.util.Random;

@Getter
@RequiredArgsConstructor
public enum TeakSpot {
    RANDOM_POINT_1(new WorldPoint(3706, 3832, 0)),
    RANDOM_POINT_2(new WorldPoint(3706, 3834, 0)),
    RANDOM_POINT_3(new WorldPoint(3704, 3836, 0)),
    RANDOM_POINT_4(new WorldPoint(3704, 3838, 0)),
    NULL(null);
    private static final Random RANDOM = new Random();
    private final WorldPoint worldPoint;

    public static TeakSpot getRandomMiningSpot() {
        TeakSpot[] spots = values();
        return spots[RANDOM.nextInt(spots.length)];
    }
}
