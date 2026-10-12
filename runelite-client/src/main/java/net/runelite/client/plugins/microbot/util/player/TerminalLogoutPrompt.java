package net.runelite.client.plugins.microbot.util.player;

import java.util.function.Consumer;
import java.util.function.Supplier;
import net.runelite.client.plugins.microbot.Microbot;

/** Keeps terminal logout separate from notification; the host pauses gameplay until completed. */
public final class TerminalLogoutPrompt {
    private String message;
    private long nextAttemptAt;

    public void begin(String text) {
        if (message == null) message = text;
    }

    public boolean active() { return message != null; }
    public String message() { return message; }
    public void reset() { message = null; nextAttemptAt = 0L; }

    public boolean tick() {
        long now = System.currentTimeMillis();
        if (now < nextAttemptAt) return false;
        nextAttemptAt = now + 600L;
        return tick(Rs2Player::logoutWithoutAutoLoginStep, Microbot::showMessage);
    }

    public boolean tick(Supplier<Rs2Player.TerminalLogoutState> logout, Consumer<String> notify) {
        if (message == null) return false;
        if (logout.get() != Rs2Player.TerminalLogoutState.LOGGED_OUT) return false;
        String completedMessage = message;
        message = null;
        notify.accept(completedMessage);
        return true;
    }
}
