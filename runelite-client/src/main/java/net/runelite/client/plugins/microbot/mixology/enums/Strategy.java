package net.runelite.client.plugins.microbot.mixology.enums;

public enum Strategy {
    FULL_ORDERS("Full Orders"),
    REDUCE_TRIPLES("Reduce Triples"),
    REDUCE_DOUBLE_AGA("Reduce Double Aga"),
    FULL_ORDER_IF_LYE_4_PLUS("Full Order if Lye 4+"),
    OPTIMISED_POINT_DISTRIBUTION("Optimised Point Distribution");

    private final String displayName;

    Strategy(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
