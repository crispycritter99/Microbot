package net.runelite.client.plugins.microbot.agility;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.*;
import net.runelite.client.Notifier;
import net.runelite.client.config.ConfigDescriptor;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.PluginConstants;
import net.runelite.client.plugins.microbot.agility.courses.AgilityCourseHandler;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.inventory.Rs2ItemModel;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;
import net.runelite.client.ui.overlay.OverlayManager;

import javax.inject.Inject;
import java.awt.*;
import java.util.List;
import java.util.stream.Collectors;

@PluginDescriptor(

	name = PluginConstants.MOCROSOFT + "Local Agility",
	description = "Microbot agility plugin",
    authors = { "Mocrosoft" },
    version = MicroAgilityLocalPlugin.version,
    minClientVersion = "2.0.0",
	tags = {"agility", "microbot"},
    iconUrl = "https://chsami.github.io/Microbot-Hub/MicroAgilityPlugin/assets/icon.png",
    cardUrl = "https://chsami.github.io/Microbot-Hub/MicroAgilityPlugin/assets/card.png",
    enabledByDefault = PluginConstants.DEFAULT_ENABLED,
    isExternal = PluginConstants.IS_EXTERNAL
)
@Slf4j
public class MicroAgilityLocalPlugin extends Plugin
{
	public static final long MILLIS_PER_MINUTE = 60_000;
	private static final int MARK_COOLDOWN_MINUTES = 3;

	public static final String version = "1.2.3";
	@Inject
	private MicroAgilityConfig config;
	@Inject
	private Client client;
	@Inject
	private OverlayManager overlayManager;
	@Inject
	private Notifier notifier;
	@Inject
	private MicroAgilityOverlay agilityOverlay;
	@Inject
	private AgilityScript agilityScript;


	public long lastCompleteMarkTimeMillis;
	public long lastCompleteTimeMillis;

	public boolean isOnCooldown = false;
	public Courses currentCourse;
	public boolean hasReducedCooldown = false;



	@Provides
	MicroAgilityConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(MicroAgilityConfig.class);
	}
	public MicroAgilityConfig getConfig()
	{
		return config;
	}

	@Override
	protected void startUp() throws AWTException
	{
		if (overlayManager != null)
		{
			overlayManager.add(agilityOverlay);
		}


	}

	protected void shutDown()
	{

		agilityScript.shutdown();
		overlayManager.remove(agilityOverlay);

	}


	public AgilityCourseHandler getCourseHandler()
	{
		return config.agilityCourse().getHandler();
	}

	public List<Rs2ItemModel> getInventoryFood()
	{
		return Rs2Inventory.getInventoryFood().stream().filter(i -> !(i.getName().toLowerCase().contains("summer pie"))).collect(Collectors.toList());
	}

	public List<Rs2ItemModel> getSummerPies()
	{
		return Rs2Inventory.getInventoryFood().stream().filter(i -> i.getName().toLowerCase().contains("summer pie")).collect(Collectors.toList());
	}

	public boolean hasRequiredLevel()
	{
		if (getSummerPies().isEmpty() || !getCourseHandler().canBeBoosted())
		{
			return Rs2Player.getRealSkillLevel(Skill.AGILITY) >= getCourseHandler().getRequiredLevel();
		}

		return Rs2Player.getBoostedSkillLevel(Skill.AGILITY) >= getCourseHandler().getRequiredLevel();
	}

	public AgilityScript getAgilityScript() {
		return agilityScript;
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged event) {
		if (event.getGameState() == GameState.LOGGED_IN) {
			log.debug("GameState changed to LOGGED_IN - initializing Agility tasks");


		} else if (event.getGameState() == GameState.LOGIN_SCREEN) {
			// Reset pre/post schedule tasks on logout for fresh initialization
			log.debug("GameState changed to LOGIN_SCREEN - resetting Agility tasks");
		}
	}
	@Subscribe
	public void onConfigChanged(ConfigChanged event)
	{

		final ConfigDescriptor desc = getConfigDescriptor();
		if (desc != null && desc.getGroup() != null && event.getGroup().equals(desc.getGroup().value())) {
			log.info(
					"Config change detected for {}: {}={}, config group {}",
					getName(),
					event.getGroup(),
					event.getKey(),
					desc.getGroup().value()
			);

		}
	}


    public ConfigDescriptor getConfigDescriptor() {
		if (Microbot.getConfigManager() == null) {
			return null;
		}
		MicroAgilityConfig conf = Microbot.getConfigManager().getConfig(MicroAgilityConfig.class);
		return Microbot.getConfigManager().getConfigDescriptor(conf);
	}

}
