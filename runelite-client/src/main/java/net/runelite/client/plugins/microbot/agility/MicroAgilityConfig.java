package net.runelite.client.plugins.microbot.agility;

import net.runelite.client.config.*;
import net.runelite.client.plugins.microbot.agility.enums.AgilityCourse;

@ConfigGroup("MicroLocalAgility")
@ConfigInformation("Enable the plugin near the start of your selected agility course. <br />" +
	"<b>Course requirements:</b>" +
	"<ul>" +
	"<li> Ape Atoll - Kruk or Ninja greegree equipped. Stamina pots recommended. </li>" +
	"<li>Shayzien Advanced - Crossbow and Mith Grapple equipped.</li>" +
	"</ul>")
public interface MicroAgilityConfig extends Config
{

	String selectedCourse = "course";
	String hitpointsThreshold = "hitpointsThreshold";
	String shouldAlch = "shouldAlch";
	String itemsToAlch = "itemsToAlch";

	@ConfigSection(
		name = "General",
		description = "General",
		position = 0,
		closedByDefault = false
	)
	String generalSection = "general";

	@ConfigItem(
		keyName = selectedCourse,
		name = "Course",
		description = "Choose your agility course",
		position = 1,
		section = generalSection
	)
	default AgilityCourse agilityCourse()
	{
		return AgilityCourse.CANIFIS_ROOFTOP_COURSE;
	}

	@ConfigItem(
		keyName = hitpointsThreshold,
		name = "Eat at",
		description = "Use food below certain hitpoint percent. If there's no food in the inventory, the script stops. Set to 0 in order to disable.",
		position = 2,
		section = generalSection
	)
	default int hitpoints()
	{
		return 20;
	}

	@ConfigItem(
		keyName = shouldAlch,
		name = "Alch",
		description = "Use Low/High Alchemy while doing agility",
		position = 4,
		section = generalSection
	)
	default boolean alchemy()
	{
		return false;
	}

	@ConfigItem(
		keyName = itemsToAlch,
		name = "Items to Alch",
		description = "Enter items to alch, separated by commas (e.g., Rune sword, Dragon dagger, Mithril platebody)",
		position = 5,
		section = generalSection
	)
	default String itemsToAlch()
	{
		return "";
	}

	@ConfigItem(
		keyName = "efficientAlching",
		name = "Efficient Alching",
		description = "Click obstacle first, then alch, then click again (for obstacles 5+ tiles away)",
		position = 6,
		section = generalSection
	)
	default boolean efficientAlching()
	{
		return false;
	}

	@ConfigItem(
		keyName = "skipInefficient",
		name = "Skip Inefficient",
		description = "Only alch when obstacle is 5+ tiles away (skip inefficient alchs)",
		position = 7,
		section = generalSection
	)
	default boolean skipInefficient()
	{
		return false;
	}

	@ConfigItem(
		keyName = "alchSkipChance",
		name = "Alch Skip Chance",
		description = "Percentage chance to skip alching on any obstacle (0-100)",
		position = 8,
		section = generalSection
	)
	@Range(min = 0, max = 100)
	default int alchSkipChance()
	{
		return 5;
	}

	@ConfigItem(
			keyName = "sendNotification",
			name = "Send notification",
			description = "Should a notification be send when the cooldown has expired",
			position = 0
	)
	default boolean sendNotification()
	{
		return true;
	}

	@ConfigItem(
			keyName = "swapLeftClickOnWait",
			name = "Swap left click on wait",
			description = "Swaps left click of last obstacle while wait is on to prevent accidental lap completion",
			position = 1
	)
	default boolean swapLeftClickOnWait()
	{
		return false;
	}

	@ConfigItem(
			keyName = "swapLeftClickTimeLeft",
			name = "Swap left click on time left",
			description = "Only swap left click when the cooldown time remaining is below this number.",
			position = 2
	)
	@Units(Units.SECONDS)
	default int swapLeftClickTimeLeft()
	{
		return 180;
	}

	@ConfigItem(
			keyName = "leewaySeconds",
			name = "Seconds of leeway",
			description = "Grace period for when timer is triggered, increase if timings are off.",
			position = 3
	)
	@Units(Units.SECONDS)
	default int leewaySeconds()
	{
		return 1;
	}

	@ConfigItem(
			keyName = "useShortArdougneTimer",
			name = "Use short Ardougne timer",
			description = "When having the elite Ardougne diary, there is a 50% chance to reduce the Ardougne cooldown to 2 min. Would you want to be notified after the reduced time or normal time?",
			position = 4
	)
	default boolean useShortArdougneTimer()
	{
		return true;
	}

	@ConfigItem(
			keyName = "showDebugValues",
			name = "Show debug values",
			description = "Displays plugin debug values like ntp offset and state",
			position = 5
	)
	default boolean showDebugValues()
	{
		return false;
	}
}