package net.runelite.client.plugins.microbot.woodcutting.fossilteaks.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.runelite.api.ItemID;
import net.runelite.api.Skill;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;

@Getter
@RequiredArgsConstructor
public enum WoodcuttingTree {
    TEAK_TREE("teak tree", "Teak logs", ItemID.TEAK_LOGS, 35, "Chop down");


    private final String name;
    private final String log;
    private final int logID;
    private final int woodcuttingLevel;
    private final String action;

    @Override
    public String toString() {
        return name;
    }

    public boolean hasRequiredLevel() {
        return Rs2Player.getSkillRequirement(Skill.WOODCUTTING, this.woodcuttingLevel);
    }
}
