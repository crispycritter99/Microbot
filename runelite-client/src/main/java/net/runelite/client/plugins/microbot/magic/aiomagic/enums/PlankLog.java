package net.runelite.client.plugins.microbot.magic.aiomagic.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.runelite.api.ItemID;

@Getter
@RequiredArgsConstructor
public enum PlankLog {
    LOG("Logs", "Plank", ItemID.LOGS, ItemID.PLANK, 70),
    OAK("Oak Logs", "Oak Plank",ItemID.OAK_LOGS, ItemID.OAK_PLANK, 175),
    TEAK("Teak Logs", "Teak Plank", ItemID.TEAK_LOGS, ItemID.TEAK_PLANK, 350),
    MAHOGANY("Mahogany Logs", "Mahogany Plank",ItemID.MAHOGANY_LOGS, ItemID.MAHOGANY_PLANK, 1050);

    private final String LogName;
    private final String PlankName;
    private final int LogItemID;
    private final int PlankItemID;
    private final int coinsRequired;
}
