package net.runelite.client.plugins.microbot.shamanspawntimers;

import com.google.inject.Provides;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import lombok.Getter;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.NPC;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.NpcDespawned;
import net.runelite.api.events.NpcSpawned;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

@PluginDescriptor(
	name = "<html>[<font color=#93652a>K</font>] Shaman Spawn Timers",
	description = "Shows tick countdowns over Lizardman shaman explosive spawns",
	tags = {"lizardman", "shaman", "spawn", "timer", "overlay"},
	enabledByDefault = false
)
public class ShamanSpawnTimersPlugin extends Plugin
{
	static final int SHAMAN_SPAWN_ID = 6768;
	private static final int SPAWNS_PER_SET = 3;

	@Inject
	private Client client;

	@Inject
	private ShamanSpawnTimersConfig config;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private ShamanSpawnTimersOverlay overlay;

	@Getter
	private final Map<NPC, SpawnTimer> spawnTimers = new IdentityHashMap<>();
	private final List<PendingSpawn> pendingSpawns = new ArrayList<>();

	@Provides
	ShamanSpawnTimersConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(ShamanSpawnTimersConfig.class);
	}

	@Override
	protected void startUp()
	{
		spawnTimers.clear();
		pendingSpawns.clear();
		overlayManager.add(overlay);
	}

	@Override
	protected void shutDown()
	{
		overlayManager.remove(overlay);
		spawnTimers.clear();
		pendingSpawns.clear();
	}

	@Subscribe
	public void onNpcSpawned(NpcSpawned event)
	{
		NPC npc = event.getNpc();
		if (npc.getId() == SHAMAN_SPAWN_ID)
		{
			pendingSpawns.add(new PendingSpawn(npc, client.getTickCount()));
		}
	}

	@Subscribe
	public void onNpcDespawned(NpcDespawned event)
	{
		NPC npc = event.getNpc();
		spawnTimers.remove(npc);
		pendingSpawns.removeIf(spawn -> spawn.npc == npc);
	}

	@Subscribe
	public void onGameTick(GameTick event)
	{
		int currentTick = client.getTickCount();
		List<PendingSpawn> ready = new ArrayList<>();
		pendingSpawns.removeIf(spawn ->
		{
			if (spawn.spawnTick < currentTick)
			{
				ready.add(spawn);
				return true;
			}
			return false;
		});

		ready.sort(Comparator.comparingInt(spawn -> spawn.npc.getIndex()));
		for (int i = 0; i < ready.size(); i++)
		{
			PendingSpawn spawn = ready.get(i);
			int positionInSet = i % SPAWNS_PER_SET;
			int explosionTick = spawn.spawnTick
				+ config.firstExplosionTicks()
				+ positionInSet * config.explosionSpacingTicks();
			spawnTimers.put(spawn.npc, new SpawnTimer(spawn.npc, explosionTick, positionInSet));
		}
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged event)
	{
		if (event.getGameState() != GameState.LOGGED_IN)
		{
			spawnTimers.clear();
			pendingSpawns.clear();
		}
	}

	static final class SpawnTimer
	{
		@Getter
		private final NPC npc;
		@Getter
		private final int explosionTick;
		@Getter
		private final int positionInSet;

		private SpawnTimer(NPC npc, int explosionTick, int positionInSet)
		{
			this.npc = npc;
			this.explosionTick = explosionTick;
			this.positionInSet = positionInSet;
		}
	}

	private static final class PendingSpawn
	{
		private final NPC npc;
		private final int spawnTick;

		private PendingSpawn(NPC npc, int spawnTick)
		{
			this.npc = npc;
			this.spawnTick = spawnTick;
		}
	}
}
