package net.runelite.client.plugins.microbot.util.world;

import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.util.bank.Rs2Bank;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;
import net.runelite.client.plugins.microbot.util.shop.Rs2Shop;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import static org.junit.Assert.assertFalse;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

public class Rs2WorldUtilHopWorldTest {

    @Test
    public void failedHopIsAttemptedOncePerAttempt() {
        Client client = mock(Client.class);
        when(client.getGameState()).thenReturn(GameState.LOGGED_IN);
        WorldHoppingConfig config = WorldHoppingConfig.builder()
                .maxAttemptsPerWorld(2)
                .baseHopDelay(10)
                .maxHopDelay(10)
                .build();

        try (MockedStatic<Microbot> microbot = Mockito.mockStatic(Microbot.class, Mockito.CALLS_REAL_METHODS);
             MockedStatic<Rs2Shop> shop = Mockito.mockStatic(Rs2Shop.class);
             MockedStatic<Rs2Bank> bank = Mockito.mockStatic(Rs2Bank.class);
             MockedStatic<Rs2Player> player = Mockito.mockStatic(Rs2Player.class)) {
            microbot.when(Microbot::getClient).thenReturn(client);
            microbot.when(() -> Microbot.hopToWorld(anyInt())).thenReturn(false);
            microbot.when(Microbot::isHopping).thenReturn(false);
            player.when(Rs2Player::getWorld).thenReturn(301);

            assertFalse(Rs2WorldUtil.hopWorld(null, 302, 0, config));

            microbot.verify(() -> Microbot.hopToWorld(302), times(2));
        }
    }
}
