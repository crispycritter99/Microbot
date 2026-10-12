/* Copyright (c) 2026, DRO (droplugins). SPDX-License-Identifier: BSD-2-Clause */
package net.runelite.client.plugins.microbot.util;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import net.runelite.client.plugins.microbot.inventorysetups.InventorySetup;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class InventorySetupTerminalOwnerTest {
    @Test public void missingSupplyRetainsHostSchedulerForObservedLogout() throws Exception {
        ScheduledFuture<?> scheduler = mock(ScheduledFuture.class);
        Rs2InventorySetup loader = new Rs2InventorySetup(mock(InventorySetup.class), scheduler);
        List<String> missing = new ArrayList<>();
        loader.withMissingSupplyPolicy(Rs2InventorySetup.MissingSupplyPolicy.LOGOUT_AND_STOP)
                .withMissingSupplyHandler(missing::add);
        var method = Rs2InventorySetup.class.getDeclaredMethod("stopForMissingRequiredSupply", String.class);
        method.setAccessible(true);
        method.invoke(loader, "Super restore(4)");
        assertEquals(List.of("Super restore(4)"), missing);
        verifyNoInteractions(scheduler);
    }
}
