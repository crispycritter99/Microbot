package net.runelite.client.plugins.microbot.shamanspawntimers;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.NPC;
import net.runelite.api.Point;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayUtil;

public class ShamanSpawnTimersOverlay extends Overlay
{
	private static final Color SAFE_COLOR = new Color(255, 196, 64);
	private static final Color DANGER_COLOR = new Color(255, 64, 64);
	private static final Font TIMER_FONT = new Font(Font.SANS_SERIF, Font.BOLD, 18);

	private final Client client;
	private final ShamanSpawnTimersPlugin plugin;

	@Inject
	private ShamanSpawnTimersOverlay(Client client, ShamanSpawnTimersPlugin plugin)
	{
		this.client = client;
		this.plugin = plugin;
		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.ABOVE_SCENE);
		setPriority(Overlay.PRIORITY_HIGH);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		Font originalFont = graphics.getFont();
		graphics.setFont(TIMER_FONT);

		for (ShamanSpawnTimersPlugin.SpawnTimer timer : plugin.getSpawnTimers().values())
		{
			NPC npc = timer.getNpc();
			if (npc == null || npc.isDead())
			{
				continue;
			}

			int ticksRemaining = Math.max(0, timer.getExplosionTick() - client.getTickCount());
			String text = ticksRemaining + "t";
			Point textLocation = npc.getCanvasTextLocation(graphics, text, 0);
			if (textLocation != null)
			{
				OverlayUtil.renderTextLocation(
					graphics,
					textLocation,
					text,
					ticksRemaining <= 1 ? DANGER_COLOR : SAFE_COLOR);
			}
		}

		graphics.setFont(originalFont);
		return null;
	}
}
