package net.runelite.client.plugins.microbot.util.walker;

import net.runelite.client.plugins.microbot.Microbot;
import org.junit.Test;

public class Rs2WalkerRunToggleTest {

	@Test
	public void walkRunToggleOnIsSkippedWhenAutoRunIsDisabled() {
		boolean previous = Microbot.enableAutoRunOn;
		try {
			Microbot.enableAutoRunOn = false;
			Rs2Walker.toggleWalkRunEnergy(true);
		} finally {
			Microbot.enableAutoRunOn = previous;
		}
	}
}
