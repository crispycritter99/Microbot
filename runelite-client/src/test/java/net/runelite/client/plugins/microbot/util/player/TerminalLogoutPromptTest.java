package net.runelite.client.plugins.microbot.util.player;

import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;

public class TerminalLogoutPromptTest {
    @Test public void waitsThroughBankClosureAndBlockedLogoutBeforeOneNotification() {
        TerminalLogoutPrompt prompt = new TerminalLogoutPrompt();
        List<String> messages = new ArrayList<>();
        prompt.begin("Ran out of supplies: box traps");
        for (Rs2Player.TerminalLogoutState state : new Rs2Player.TerminalLogoutState[]{
                Rs2Player.TerminalLogoutState.BANK_CLOSING, Rs2Player.TerminalLogoutState.INPUT_BLOCKED,
                Rs2Player.TerminalLogoutState.LOGOUT_REQUESTED}) {
            assertFalse(prompt.tick(() -> state, messages::add));
            assertTrue(prompt.active());
            assertTrue(messages.isEmpty());
        }
        assertTrue(prompt.tick(() -> Rs2Player.TerminalLogoutState.LOGGED_OUT, messages::add));
        assertFalse(prompt.active());
        assertEquals(List.of("Ran out of supplies: box traps"), messages);
        assertFalse(prompt.tick(() -> { throw new AssertionError("completed stop retried"); }, messages::add));
    }

    @Test public void explicitRestartCancelsOldLogoutAndMessage() {
        TerminalLogoutPrompt prompt = new TerminalLogoutPrompt();
        prompt.begin("Old stop");
        prompt.reset();
        assertFalse(prompt.tick(() -> { throw new AssertionError("old stop continued"); },
                text -> { throw new AssertionError("old popup appeared"); }));
    }
}
