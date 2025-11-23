package net.runelite.client.plugins.microbot.agility;

import net.runelite.api.Skill;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.agility.ntp.NtpClient;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;

import javax.inject.Inject;
import java.awt.*;
import java.time.Instant;

public class MicroAgilityOverlay extends OverlayPanel
{
	private static final int TIMEOUT_MINUTES = 5;
	private static final long TIMEOUT_MILLIS = TIMEOUT_MINUTES * MarksOfGraceCDPlugin.MILLIS_PER_MINUTE;
	final MicroAgilityPlugin plugin;
	final MicroAgilityConfig config;

	@Inject
	MicroAgilityOverlay(MicroAgilityPlugin plugin, MicroAgilityConfig config)
	{
		super(plugin);
		this.plugin = plugin;
		this.config = config;
		setPosition(OverlayPosition.TOP_LEFT);
		setNaughty();
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		try
		{
			panelComponent.setPreferredSize(new Dimension(200, 300));
			panelComponent.getChildren().add(TitleComponent.builder()
				.text("Micro Agility V" + MicroAgilityPlugin.version)
				.color(Color.GREEN)
				.build());

			panelComponent.getChildren().add(LineComponent.builder().build());

			panelComponent.getChildren().add(LineComponent.builder()
				.left("Agility Exp")
				.right(Integer.toString(Microbot.getClient().getSkillExperience(Skill.AGILITY)))
				.build());

			panelComponent.getChildren().add(LineComponent.builder()
				.left("Current Obstacle")
				.right(Integer.toString(config.agilityCourse().getHandler().getCurrentObstacleIndex()))
				.build());
//			if (plugin.lastCompleteMarkTimeMillis == 0)
//			{
//				return null;
//			}
//
//			long currentMillis = Instant.now().toEpochMilli();
//			long millisSinceLastComplete = currentMillis - plugin.lastCompleteTimeMillis;
//
//			if (millisSinceLastComplete > TIMEOUT_MILLIS)
//			{
//				plugin.lastCompleteMarkTimeMillis = 0;
//				plugin.lastCompleteTimeMillis = 0;
//				plugin.currentCourse = null;
//				return null;
//			}
//
//			if (plugin.isOnCooldown)
//			{
//				panelComponent.getChildren().add(TitleComponent.builder()
//						.text("Wait")
//						.color(Color.RED)
//						.build());
//			}
//			else
//			{
//				panelComponent.getChildren().add(TitleComponent.builder()
//						.text("Run")
//						.color(Color.GREEN)
//						.build());
//			}
//
//			long millisLeft = Math.max(plugin.getCooldownTimestamp(false) - currentMillis, 0);
//			long secondsLeft = (long)Math.ceil((double)millisLeft / 1000);
//			panelComponent.getChildren().add(LineComponent.builder()
//					.left("Time until run:")
//					.right(String.format("%d:%02d", (secondsLeft % 3600) / 60, (secondsLeft % 60)))
//					.build());
//
//			if (plugin.hasReducedCooldown)
//			{
//				long shortTimeSecondsLeft = Math.max(secondsLeft - 60, 0);
//				panelComponent.getChildren().add(LineComponent.builder()
//						.left("Reduced time:")
//						.right(String.format("%d:%02d", (shortTimeSecondsLeft % 3600) / 60, (shortTimeSecondsLeft % 60)))
//						.build());
//			}
//
//			if (config.showDebugValues())
//			{
//				panelComponent.getChildren().add(LineComponent.builder()
//						.left("NTP State:")
//						.right(String.valueOf(NtpClient.SyncState))
//						.build());
//
//				panelComponent.getChildren().add(LineComponent.builder()
//						.left("Time offset:")
//						.right(getReadableOffset(NtpClient.SyncedOffsetMillis))
//						.build());
//			}
		}
		catch (Exception ex)
		{
			Microbot.logStackTrace(this.getClass().getSimpleName(), ex);
		}
		return super.render(graphics);
	}
	private String getReadableOffset(long offset)
	{
		if (Math.abs(offset) < 1000)
			return offset + "ms";

		offset /= 1000; // Seconds

		if (Math.abs(offset) < 1000)
			return offset + "s";

		offset /= 60; // Minutes

		if (Math.abs(offset) < 1000)
			return offset + "m";

		offset /= 60; // Hours

		if (Math.abs(offset) < 1000)
			return offset + "h";

		return "LOTS";
	}
}
