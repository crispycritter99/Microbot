package net.runelite.client.plugins.microbot.mixology;

import net.runelite.client.plugins.microbot.mixology.enums.PotionComponent;
import net.runelite.client.plugins.microbot.mixology.enums.PotionType;
import net.runelite.client.plugins.microbot.mixology.enums.Strategy;

import java.util.List;

public class StrategyHelper {

    /**
     * Returns true if this order should be SKIPPED based on the selected strategy.
     *
     * @param order     The individual potion order to evaluate
     * @param allOrders All 3 orders in the current draw
     * @param strategy  The configured strategy
     */
    public static boolean shouldSkipOrder(PotionOrder order, List<PotionOrder> allOrders, Strategy strategy) {
        PotionComponent[] comps = order.potionType().components();

        boolean drawHasMAL = allOrders.stream()
                .anyMatch(o -> o.potionType() == PotionType.MIXALOT);

        boolean isTriple = comps[0] == comps[1] && comps[1] == comps[2]; // AAA, MMM, LLL
        boolean isDoubleAga = countComponent(comps, PotionComponent.AGA) >= 2
                && order.potionType() != PotionType.MIXALOT; // AAM, AAL, AAA

        int totalLye = countTotalLye(allOrders);

        switch (strategy) {
            case FULL_ORDERS:
                // Never skip anything
                return false;

            case REDUCE_TRIPLES:
                // Skip triple-component potions (AAA, MMM, LLL) unless MAL is in the draw
                return isTriple && !drawHasMAL;

            case REDUCE_DOUBLE_AGA:
                // Builds on Reduce Triples: also skip double-aga potions (AAM, AAL, AAA) unless MAL present
                return (isTriple || isDoubleAga) && !drawHasMAL;

            case FULL_ORDER_IF_LYE_4_PLUS:
                // Builds on Reduce Double Aga: if draw has 4+ Lye units, do everything
                if (totalLye >= 4) return false;
                return (isTriple || isDoubleAga) && !drawHasMAL;

            case OPTIMISED_POINT_DISTRIBUTION:
                // Builds on all previous: also deprioritise aga-heavy orders where Aga > Lye or Aga > Mox
                if (totalLye >= 4) return false;
                if ((isTriple || isDoubleAga) && !drawHasMAL) return true;
                int orderAga = countComponent(comps, PotionComponent.AGA);
                int orderLye = countComponent(comps, PotionComponent.LYE);
                int orderMox = countComponent(comps, PotionComponent.MOX);
                // Skip if aga-heavy and lye is not being favoured
                return orderAga > orderLye || orderAga > orderMox;

            default:
                return false;
        }
    }

    /**
     * Returns a filtered list of orders that should actually be brewed,
     * falling back to all unfulfilled orders if the strategy filters everything out.
     */
    public static List<PotionOrder> getActiveOrders(List<PotionOrder> potionOrders, Strategy strategy) {
        List<PotionOrder> unfulfilled = potionOrders.stream()
                .filter(o -> !o.fulfilled())
                .collect(java.util.stream.Collectors.toList());

        List<PotionOrder> filtered = unfulfilled.stream()
                .filter(o -> !shouldSkipOrder(o, potionOrders, strategy))
                .collect(java.util.stream.Collectors.toList());

        // Fallback: if strategy filtered everything out, just return all unfulfilled orders
        // (prefer the highest XP one so we don't idle)
        if (filtered.isEmpty()) {
            return unfulfilled;
        }

        return filtered;
    }

    // ---- Helpers ----

    private static int countComponent(PotionComponent[] comps, PotionComponent target) {
        int count = 0;
        for (PotionComponent c : comps) {
            if (c == target) count++;
        }
        return count;
    }

    /**
     * Counts total Lye units across all orders in a draw.
     * LLL counts as only 2 L's per the strategy spec (since it's 20 Lye, not 30).
     */
    private static int countTotalLye(List<PotionOrder> orders) {
        int total = 0;
        for (PotionOrder o : orders) {
            PotionComponent[] comps = o.potionType().components();
            boolean isLyeTriple = comps[0] == PotionComponent.LYE
                    && comps[1] == PotionComponent.LYE
                    && comps[2] == PotionComponent.LYE;
            if (isLyeTriple) {
                total += 2; // LLL counts as 2 per spec
            } else {
                total += countComponent(comps, PotionComponent.LYE);
            }
        }
        return total;
    }
}
