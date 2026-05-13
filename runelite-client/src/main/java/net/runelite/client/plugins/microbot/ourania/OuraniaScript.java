package net.runelite.client.plugins.microbot.ourania;

import net.runelite.api.Constants;
import net.runelite.api.GameState;
import net.runelite.api.Skill;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.gameval.ItemID;
import net.runelite.api.gameval.NpcID;
import net.runelite.api.gameval.ObjectID;
import net.runelite.client.plugins.gpu.GpuPlugin;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.api.npc.models.Rs2NpcModel;
import net.runelite.client.plugins.microbot.breakhandler.BreakHandlerScript;
import net.runelite.client.plugins.microbot.ourania.enums.OuraniaState;
import net.runelite.client.plugins.microbot.ourania.enums.Path;
import net.runelite.client.plugins.microbot.pouch.Pouch;
import net.runelite.client.plugins.microbot.util.antiban.Rs2Antiban;
import net.runelite.client.plugins.microbot.util.antiban.enums.Activity;
import net.runelite.client.plugins.microbot.util.bank.Rs2Bank;
import net.runelite.client.plugins.microbot.util.camera.Rs2Camera;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.inventory.Rs2ItemModel;
import net.runelite.client.plugins.microbot.util.inventory.RunePouchType;
import net.runelite.client.plugins.microbot.util.keyboard.Rs2Keyboard;
import net.runelite.client.plugins.microbot.util.magic.Rs2Magic;
import net.runelite.client.plugins.microbot.util.magic.Rs2Spellbook;
import net.runelite.client.plugins.microbot.util.magic.Rs2Spells;
import net.runelite.client.plugins.microbot.util.math.Rs2Random;
import net.runelite.client.plugins.microbot.util.misc.Rs2Potion;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;
import net.runelite.client.plugins.microbot.util.walker.Rs2Walker;
import net.runelite.client.plugins.microbot.util.widget.Rs2Widget;
import net.runelite.client.plugins.skillcalculator.skills.MagicAction;

import javax.inject.Inject;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class OuraniaScript extends Script
{

	public static OuraniaState state;
	private final OuraniaConfig config;
	private final List<Integer> massWorlds = List.of(327, 480);
	private final OuraniaLocalPlugin plugin;
	private int selectedWorld = 0;
    public Instant startTime;
	String moltenGlass = "molten glass";
	String glassblowingPipe = "glassblowing pipe";
    /**
     * Get the total runtime of the script
     *
     * @return the total runtime of the script
     */
    public Duration getRunTime() {
        if (startTime == null) return Duration.ofSeconds(0);
        return Duration.between(startTime, Instant.now());
    }

	@Inject
	public OuraniaScript(OuraniaLocalPlugin plugin, OuraniaConfig config)
	{
		this.plugin = plugin;
		this.config = config;
	}

	@Override
	public void shutdown()
	{
		Rs2Antiban.resetAntibanSettings();
		super.shutdown();
	}

	public boolean run()
	{
        startTime = Instant.now();
		Microbot.enableAutoRunOn = false;
		Rs2Antiban.resetAntibanSettings();
		Rs2Antiban.antibanSetupTemplates.applyRunecraftingSetup();
		Rs2Antiban.setActivity(Activity.CRAFTING_RUNES_AT_OURANIA_ALTAR);
		mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> {
			try
			{
				if (!Microbot.isLoggedIn())
				{
					return;
				}
				if (!super.run())
				{
					return;
				}
				long startTime = System.currentTimeMillis();

				if (!Rs2Magic.isSpellbook(Rs2Spellbook.LUNAR))
				{
					Microbot.showMessage("Not currently on Lunar Spellbook");
					Microbot.stopPlugin(plugin);
					return;
				}

				if (Rs2Inventory.anyPouchUnknown())
				{
					Rs2Inventory.checkPouches();
					return;
				}

				if (config.useMassWorld() && !isOnMassWorld())
				{
					if (selectedWorld == 0)
					{
						selectedWorld = massWorlds.get(Rs2Random.between(0, massWorlds.size()));
					}
					Microbot.hopToWorld(selectedWorld);
					sleepUntil(() -> Microbot.getClient().getGameState() == GameState.LOGGED_IN);
					return;
				}

				if (selectedWorld != 0)
				{
					selectedWorld = 0;
				}

				if (hasStateChanged())
				{
					state = updateState();
				}

				if (state == null)
				{
					Microbot.showMessage("Unable to evaluate state");
					Microbot.stopPlugin(plugin);
					return;
				}

				switch (state)
				{
					case CRAFTING:
//						if (!Rs2Inventory.hasItem(config.essence().getItemId()) && Rs2Inventory.hasAnyPouch() && !Rs2Inventory.allPouchesEmpty())
//						{
//							Rs2Inventory.emptyPouches();
//							return;
//						}
						//if
						while (!Rs2Inventory.allPouchesEmpty()) {
							if (!Rs2Inventory.isFull()&&Rs2Inventory.hasAnyPouch()&&!Rs2Inventory.allPouchesEmpty()) {
								Rs2Inventory.emptyPouches();
								Rs2Inventory.waitForInventoryChanges(1200);
							}
							Microbot.getRs2TileObjectCache().query().withId(ObjectID.RC_ZMI_DUNGEON_CRACKED_CENTER_ALTAR).interact("craft-rune");
							sleep(200,600);
						}
//						Rs2Inventory.waitForInventoryChanges(5000);
						break;
					case RESETTING:
						if (Rs2Player.getWorldLocation().distanceTo(new WorldPoint(2468, 3246, 0)) > 24)
						{
							Rs2Magic.cast(MagicAction.OURANIA_TELEPORT);
						}
						sleepUntil(() -> Rs2Player.getWorldLocation().distanceTo(new WorldPoint(2468, 3246, 0)) < 24);

						if (plugin.isBreakHandlerEnabled())
						{
							BreakHandlerScript.setLockState(false);
						}

						if (Rs2Inventory.hasDegradedPouch() && Rs2Magic.hasRequiredRunes(Rs2Spells.NPC_CONTACT))
						{
//							Microbot.getRs2TileObjectCache().query().withId(ObjectID.RC_ZMI_DUNGEON_ENTRANCE).interact("Climb");
//							sleep(200,600);
							Rs2Magic.repairPouchesWithLunar();
							return;
						}

						if (config.directInteract() && Microbot.isPluginEnabled(GpuPlugin.class))
						{
							Microbot.getRs2TileObjectCache().query().withId(ObjectID.RC_ZMI_DUNGEON_ENTRANCE).interact("Climb");
							sleep(200,600);
							if (Rs2Inventory.contains("molten glass")&&Rs2Player.distanceTo(new WorldPoint(2452, 3231, 0)) >3){
								Rs2Inventory.combine(glassblowingPipe, moltenGlass);

								Rs2Widget.sleepUntilHasWidgetText("How many do you wish to make?", 270, 5, false, 5000);

								Rs2Keyboard.keyPress('8');

								Rs2Widget.sleepUntilHasNotWidgetText("How many do you wish to make?", 270, 5, false, 5000);


							boolean done = false;
							long startTimeloop = System.currentTimeMillis();
							do {
								done = Rs2Player.distanceTo(new WorldPoint(2452, 3231, 0)) <3;


								sleep(100);
							} while (!done && System.currentTimeMillis() - startTime < 30000);
							Microbot.getRs2TileObjectCache().query().withId(ObjectID.RC_ZMI_DUNGEON_ENTRANCE).interact("Climb");
							sleepUntil(this::isNearEniola, 20000);
						}
							else{
								sleepUntil(this::isNearEniola, 20000);
							}
						}
						else
						{
							Rs2Walker.walkTo(new WorldPoint(3014, 5625, 0));
						}
						break;
					case BANKING:
						if (plugin.isRanOutOfAutoPay())
						{
							Microbot.showMessage("You have ran out of auto-pay runes, check runepouch!");
							Microbot.stopPlugin(plugin);
							return;
						}

						if (!Rs2Bank.isOpen())
						{
							Rs2NpcModel eniola = Microbot.getRs2NpcCache().query().withId(NpcID.RC_ZMI_BANKER).nearest();
							if (eniola == null)
							{
								return;
							}
							eniola.click("bank");
							sleepUntil(Rs2Bank::isOpen, 3000);
							return;
						}

						if (!config.toggleProfitCalculator())
						{
							plugin.calcuateProfit();
						}

						boolean hasRunes = Rs2Inventory.items().anyMatch(item -> item.getName().toLowerCase().contains("rune") && !item.getName().toLowerCase().contains("rune pouch"));

						if (hasRunes)
						{
							if (config.useDepositAll())
							{
								Rs2Bank.depositAll();
							}
							else
							{
								// Get all RunePouchType IDs
								Integer[] runePouchIds = Arrays.stream(RunePouchType.values())
									.map(RunePouchType::getItemId)
									.toArray(Integer[]::new);

								// Get all eligible pouch IDs based on Runecrafting level
								Integer[] eligiblePouchIds = Arrays.stream(Pouch.values())
									.filter(Pouch::hasRequiredRunecraftingLevel)
									.flatMap(pouch -> Arrays.stream(pouch.getItemIds()).boxed())
									.toArray(Integer[]::new);

								// Combine RunePouchType IDs and eligible pouch IDs into a single array
								Integer[] excludedIds = Stream.concat(Arrays.stream(runePouchIds), Arrays.stream(eligiblePouchIds))
									.toArray(Integer[]::new);

								Rs2Bank.depositAllExcept(excludedIds);
								Rs2Inventory.waitForInventoryChanges(1800);
							}
						}

						if (config.useEnergyRestorePotions() && Rs2Player.getRunEnergy() <= config.drinkAtPercent())
						{
							boolean hasStaminaPotion = Rs2Bank.hasItem(Rs2Potion.getStaminaPotion());
							boolean hasEnergyRestorePotion = Rs2Bank.hasItem(Rs2Potion.getRestoreEnergyPotionsVariants());

							if ((Rs2Player.hasStaminaBuffActive() && hasEnergyRestorePotion) || (!hasStaminaPotion && hasEnergyRestorePotion))
							{
								Rs2ItemModel energyRestoreItem = Rs2Bank.bankItems().stream()
									.filter(rs2Item -> Rs2Potion.getRestoreEnergyPotionsVariants().stream()
										.anyMatch(variant -> rs2Item.getName().toLowerCase().contains(variant.toLowerCase())))
									.min(Comparator.comparingInt(rs2Item -> getDoseFromName(rs2Item.getName())))
									.orElse(null);

								if (energyRestoreItem == null)
								{
									Microbot.showMessage("Unable to find Restore Energy Potion but hasItem?");
									Microbot.stopPlugin(plugin);
									return;
								}

								withdrawAndDrink(energyRestoreItem.getName());
							}
							else if (hasStaminaPotion)
							{
								Rs2ItemModel staminaPotionItem = Rs2Bank.bankItems().stream()
									.filter(rs2Item -> rs2Item.getName().toLowerCase().contains(Rs2Potion.getStaminaPotion().toLowerCase()))
									.min(Comparator.comparingInt(rs2Item -> getDoseFromName(rs2Item.getName())))
									.orElse(null);

								if (staminaPotionItem == null)
								{
									Microbot.showMessage("Unable to find Stamina Potion but hasItem?");
									Microbot.stopPlugin(plugin);
									return;
								}

								withdrawAndDrink(staminaPotionItem.getName());
							}
							else
							{
								Microbot.showMessage("Unable to find Stamina Potion OR Energy Restore Potions");
								Microbot.stopPlugin(plugin);
								return;
							}
						}

						if (Rs2Player.getHealthPercentage() <= config.eatAtPercent())
						{
							while (Rs2Player.getHealthPercentage() < 100 && isRunning())
							{
								if (!Rs2Bank.hasItem(config.food().getId()))
								{
									Microbot.showMessage("Missing Food in Bank!");
									Microbot.stopPlugin(plugin);
									break;
								}

								Rs2Bank.withdrawOne(config.food().getId());
								Rs2Inventory.waitForInventoryChanges(1800);
								Rs2Player.useFood();
								sleepUntil(() -> !Rs2Inventory.hasItem(config.food().getId()));
							}

							if (Rs2Inventory.hasItem(ItemID.JUG_EMPTY))
							{
								Rs2Bank.depositAll(ItemID.JUG_EMPTY);
								Rs2Inventory.waitForInventoryChanges(1800);
							}
						}
						Rs2Player.useFood();
						int requiredEssence = Rs2Inventory.emptySlotCount() + Rs2Inventory.getRemainingCapacityInPouches();
						int requiredMoltenGlass = 11-Rs2Inventory.count("molten glass");
						int fixedspaces = 3;

						if (!Rs2Bank.hasBankItem(config.essence().getItemId(), requiredEssence))
						{
							Microbot.showMessage("Not enough essence to full run");
							Microbot.stopPlugin(plugin);
							return;
						}
//						if (!Rs2Inventory.contains("molten glass")&&Rs2Inventory.allPouchesEmpty()){
//							int runecraftLevel = Rs2Player.getBoostedSkillLevel(Skill.RUNECRAFT);
//							int pouchSize=27;
//							if (runecraftLevel >= 85) {
//								pouchSize=40;
//							} else if (runecraftLevel >= 75) {
//								pouchSize=27;
//							} else if (runecraftLevel >= 50) {
//								pouchSize=16;
//							} else {
//								pouchSize=8;
//							}
//							int moltenglassreq=11;
//							int numberOfWithdraws=(28-moltenglassreq-fixedspaces+pouchSize)/(28-moltenglassreq-fixedspaces);
//							Rs2Bank.withdrawX("molten glass", 11);
//							Rs2Bank.withdrawAll(config.essence().getItemId());
//							for (int count = 0; count <numberOfWithdraws; count++) {
//								Rs2Inventory.fillPouches();
//								Rs2Bank.withdrawAll(config.essence().getItemId());
//							}
//						}
//						else {
//							if (!Rs2Inventory.contains("molten glass")) {
//								Rs2Bank.withdrawX("molten glass", 11);
//							}
							if (Rs2Inventory.hasAnyPouch()) {
								while (!Rs2Inventory.allPouchesFull() && isRunning()) {
									Rs2Bank.withdrawAll(config.essence().getItemId());
									Rs2Inventory.fillPouches();
									Rs2Inventory.waitForInventoryChanges(1800);
								}
							}

							Rs2Bank.withdrawAll(config.essence().getItemId());
							Rs2Inventory.waitForInventoryChanges(1800);
//						}
						Rs2Bank.closeBank();
						sleepUntil(() -> !Rs2Bank.isOpen());
						break;
					case RUNNING_TO_ALTAR:
						if (plugin.isBreakHandlerEnabled())
						{
							BreakHandlerScript.setLockState(true);
						}

						if (config.path().equals(Path.SHORT))
						{
							if (config.directInteract() && Microbot.isPluginEnabled(GpuPlugin.class))
							{
								var altarModel = Microbot.getRs2TileObjectCache().query().withId(ObjectID.RC_ZMI_DUNGEON_CRACKED_CENTER_ALTAR).within(Constants.SCENE_SIZE).nearest();
								if (Rs2Camera.getPitch() < 210 || Rs2Camera.getPitch() > 280)
								{
									int randomPitch = Rs2Random.nextInt(220, 260, 1, false);
									Rs2Camera.setPitch(randomPitch);
									sleepUntil(() -> Rs2Camera.getPitch() == randomPitch);
								}
								if (Rs2Camera.getZoom() != 128)
								{
									Rs2Camera.setZoom(128);
									sleepUntil(() -> Rs2Camera.getZoom() == 128);
								}

								if (altarModel != null) altarModel.click("craft-rune");
								Rs2Inventory.waitForInventoryChanges(1800);
//								boolean done = false;
////								boolean hasCraftingAnimation = false;
//								long detectedTime=-1;
//								long startTimeloop = System.currentTimeMillis();
//								do {
//									done = isNearAltar();
//									if (Rs2Player.getAnimation()==884){
////										hasCraftingAnimation = true;
//										detectedTime=System.currentTimeMillis();
//									}
//									if ((System.currentTimeMillis() - detectedTime >1800)&&Rs2Inventory.contains("molten glass")){
//										Rs2Inventory.combine(glassblowingPipe, moltenGlass);
//
//										Rs2Widget.sleepUntilHasWidgetText("How many do you wish to make?", 270, 5, false, 5000);
//
//										Rs2Keyboard.keyPress('8');
//
//										Rs2Widget.sleepUntilHasNotWidgetText("How many do you wish to make?", 270, 5, false, 5000);
//									}
//
//									sleep(100);
//								} while (!done && System.currentTimeMillis() - startTime < 30000);


								sleepUntil(this::isNearAltar, 30000);
//								Rs2Inventory.dropAll(false,"light orb");
							}
							else
							{
								Rs2Walker.walkTo(config.path().getWorldPoint());
							}
						}
						else
						{
							Microbot.getRs2TileObjectCache().query().withId(ObjectID.RC_ZMI_DUNGEON_WALL_CRACK_ENTRANCE).interact("squeeze-through");
							sleepUntil(this::isNearAltar, 10000);
						}
						break;
				}

				long endTime = System.currentTimeMillis();
				long totalTime = endTime - startTime;
				System.out.println("Total time for loop " + totalTime);

			}
			catch (Exception ex)
			{
				Microbot.logStackTrace(this.getClass().getSimpleName(), ex);
				Microbot.log("Error in Ourania Altar Script: " + ex.getMessage());
			}
		}, 0, 600, TimeUnit.MILLISECONDS);
		return true;
	}

	private boolean hasStateChanged()
	{
		if (Microbot.isDebug())
		{
			Microbot.log("State: " + state);
		}
		if (state == null)
		{
			return true;
		}
		if (hasRequiredItems() && !isNearAltar())
		{
			return true;
		}
		if (hasRequiredItems() && isNearAltar())
		{
			return true;
		}
		if ((!hasRequiredItems() && isNearAltar()) || (!hasRequiredItems() && !isNearEniola()))
		{
			return true;
		}
		if (!hasRequiredItems() && isNearEniola())
		{
			return true;
		}
		return false;
	}

	private OuraniaState updateState()
	{
		if (hasRequiredItems() && !isNearAltar())
		{
			return OuraniaState.RUNNING_TO_ALTAR;
		}
		if (hasRequiredItems() && isNearAltar())
		{
			return OuraniaState.CRAFTING;
		}
		if ((!hasRequiredItems() && isNearAltar()) || (!hasRequiredItems() && !isNearEniola()))
		{
			return OuraniaState.RESETTING;
		}
		if (!hasRequiredItems() && isNearEniola())
		{
			return OuraniaState.BANKING;
		}
		return null;
	}

	private boolean hasRequiredItems()
	{
		if (Rs2Inventory.hasAnyPouch())
		{
			boolean pouchesContainEssence = !Rs2Inventory.allPouchesEmpty();
			boolean inventoryContainsEssence = Rs2Inventory.hasItem(config.essence().getItemId());
			return pouchesContainEssence || inventoryContainsEssence;
		}
		else
		{
			return Rs2Inventory.hasItem(config.essence().getItemId());
		}
	}

	private boolean isNearAltar()
	{
		return plugin.getOuraniaAltarArea().contains(Rs2Player.getWorldLocation());
	}

	private boolean isNearEniola()
	{
		Rs2NpcModel eniola = Microbot.getRs2NpcCache().query().withId(NpcID.RC_ZMI_BANKER).nearest();
		if (eniola == null)
		{
			return false;
		}
		return Rs2Player.getWorldLocation().distanceTo2D(eniola.getWorldLocation()) < 12;
	}

	private void withdrawAndDrink(String potionItemName)
	{
		String simplifiedPotionName = potionItemName.replaceAll("\\s*\\(\\d+\\)", "").trim();
		Rs2Bank.withdrawOne(potionItemName);
		Rs2Inventory.waitForInventoryChanges(1800);
		Rs2Inventory.interact(potionItemName, "drink");
		Rs2Inventory.waitForInventoryChanges(1800);
		if (Rs2Inventory.hasItem(simplifiedPotionName))
		{
			Rs2Bank.depositOne(simplifiedPotionName);
			Rs2Inventory.waitForInventoryChanges(1800);
		}
		if (Rs2Inventory.hasItem(ItemID.VIAL_EMPTY))
		{
			Rs2Bank.depositOne(ItemID.VIAL_EMPTY);
			Rs2Inventory.waitForInventoryChanges(1800);
		}
	}

	private boolean isOnMassWorld()
	{
		return massWorlds.contains(Rs2Player.getWorld());
	}

	private int getDoseFromName(String potionItemName)
	{
		Pattern pattern = Pattern.compile("\\((\\d+)\\)$");
		Matcher matcher = pattern.matcher(potionItemName);
		if (matcher.find())
		{
			return Integer.parseInt(matcher.group(1));
		}
		return 0;
	}
}
