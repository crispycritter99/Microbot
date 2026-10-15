package net.runelite.client.plugins.microbot.util.gameobject;

import net.runelite.api.GameObject;
import net.runelite.api.TileObject;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.plugins.cannon.CannonPlugin;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.Optional;
import java.util.concurrent.Callable;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;

public class Rs2CannonNullPositionTest {

    private MockedStatic<Microbot> microbot;
    private MockedStatic<Rs2Inventory> inventory;
    private MockedStatic<Rs2GameObject> gameObject;

    @Before
    public void setUp() {
        assertNull(CannonPlugin.getCannonPosition());

        ClientThread clientThread = mock(ClientThread.class);
        when(clientThread.runOnClientThreadOptional(any(Callable.class))).thenReturn(Optional.of(0));
        microbot = Mockito.mockStatic(Microbot.class);
        microbot.when(Microbot::getClientThread).thenReturn(clientThread);

        inventory = Mockito.mockStatic(Rs2Inventory.class);
        inventory.when(() -> Rs2Inventory.hasItemAmount(eq("cannonball"), anyInt(), anyBoolean())).thenReturn(true);

        GameObject cannon = mock(GameObject.class);
        when(cannon.getWorldLocation()).thenReturn(new WorldPoint(3200, 3200, 0));
        gameObject = Mockito.mockStatic(Rs2GameObject.class);
        gameObject.when(() -> Rs2GameObject.findObject(any(Integer[].class))).thenReturn(cannon);
    }

    @After
    public void tearDown() {
        gameObject.close();
        inventory.close();
        microbot.close();
        Microbot.pauseAllScripts.set(false);
    }

    @Test
    public void refillReturnsFalseWhenCannonPositionUnknown() {
        assertFalse(Rs2Cannon.refill(15));
        assertFalse(Microbot.pauseAllScripts.get());
        gameObject.verify(() -> Rs2GameObject.interact(any(TileObject.class), anyString()), never());
    }

    @Test
    public void repairReturnsFalseWhenCannonPositionUnknown() {
        assertFalse(Rs2Cannon.repair());
        gameObject.verify(() -> Rs2GameObject.interact(any(TileObject.class), anyString()), never());
    }
}
