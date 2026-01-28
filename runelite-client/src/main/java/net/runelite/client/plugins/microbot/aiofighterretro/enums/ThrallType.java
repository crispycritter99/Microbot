package net.runelite.client.plugins.microbot.aiofighterretro.enums;

import net.runelite.client.plugins.skillcalculator.skills.MagicAction;

public enum ThrallType {
    MAGE(MagicAction.RESURRECT_GREATER_GHOST),
    RANGED(MagicAction.RESURRECT_GREATER_SKELETON),
    MELEE(MagicAction.RESURRECT_GREATER_ZOMBIE);
    private final MagicAction thrallSpell;

    ThrallType(MagicAction thrallSpell) {
        this.thrallSpell = thrallSpell;
    }

    public MagicAction toMagicAction() {
        return thrallSpell;
    }

}
