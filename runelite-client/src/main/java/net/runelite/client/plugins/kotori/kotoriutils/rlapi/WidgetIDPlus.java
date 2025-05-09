/*
 * Copyright (c) 2017, Adam <Adam@sigterm.info>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package net.runelite.client.plugins.kotori.kotoriutils.rlapi;

/**
 * Extension of Runelite's WidgetID class mapping out constants they didn't or previously removed.
 * <p>
 * The constants defined directly under the WidgetID class are
 * Widget group IDs. All child IDs are defined in sub-classes relating
 * to their group.
 * <p>
 * For a more direct group-child widget mapping, use the
 * WidgetInfo enum class.
 */
public final class WidgetIDPlus
{
	public static final int FAIRY_RING_GROUP_ID = 398;
	public static final int GRAND_EXCHANGE_GROUP_ID = 465;
	public static final int PLAYER_TRADE_SCREEN_GROUP_ID = 335;
	public static final int EQUIPMENT_GROUP_ID = 387;
	public static final int MINIMAP_GROUP_ID = 160;
	public static final int RESIZABLE_VIEWPORT_BOTTOM_LINE_GROUP_ID = 164;
	public static final int PRAYER_GROUP_ID = 541;
	public static final int QUICK_PRAYERS_GROUP_ID = 77;
	public static final int SHOP_GROUP_ID = 300;
	public static final int SMITHING_GROUP_ID = 312;
	public static final int COMBAT_GROUP_ID = 593;
	public static final int LEVEL_UP_GROUP_ID = 233;
	public static final int LIGHT_BOX_GROUP_ID = 322;
	public static final int MTA_ENCHANTMENT_GROUP_ID = 195;
	public static final int MTA_GRAVEYARD_GROUP_ID = 196;
	public static final int THEATRE_OF_BLOOD_GROUP_ID = 23;
	public static final int WORLD_SWITCHER_GROUP_ID = 69;
	public static final int DIALOG_OPTION_GROUP_ID = 219;
	public static final int SPELLBOOK_GROUP_ID = 218;
	public static final int PVP_GROUP_ID = 90;
	public static final int ENTERING_HOUSE_GROUP_ID = 71;
	public static final int BANK_PIN_GROUP_ID = 213;
	public static final int DIALOG_MINIGAME_GROUP_ID = 229;
	public static final int PEST_CONTROL_EXCHANGE_WINDOW_GROUP_ID = 243;
	public static final int GAUNTLET_MAP_GROUP_ID = 638;
	public static final int PLAYER_TRADE_CONFIRM_GROUP_ID = 334;
	public static final int OPTIONS_GROUP_ID = 261;
	public static final int JEWELLERY_BOX_GROUP_ID = 590;
	public static final int EQUIPMENT_PAGE_GROUP_ID = 84;
	public static final int QUESTTAB_GROUP_ID = 629;
	public static final int MUSICTAB_GROUP_ID = 239;
	public static final int FOSSIL_ISLAND_MUSHROOM_TELE_GROUP_ID = 608;
	public static final int THEATRE_OF_BLOOD_PARTY_GROUP_ID = 28;
	public static final int DIALOG_NOTIFICATION_GROUP_ID = 229;
	public static final int DIALOG_SPRITE2_ID = 11;
	public static final int MULTISKILL_MENU_GROUP_ID = 270;

	static class DialogOption
	{
		static final int OPTIONS = 1;
	}
	
	static class GrandExchange
	{
		static final int HISTORY_BUTTON = 3;
		static final int BACK_BUTTON = 4;
		static final int OFFER1 = 7;
		static final int OFFER2 = 8;
		static final int OFFER3 = 9;
		static final int OFFER4 = 10;
		static final int OFFER5 = 11;
		static final int OFFER6 = 12;
		static final int OFFER7 = 13;
		static final int OFFER8 = 14;
		static final int OFFER_CONFIRM_BUTTON = 29;
	}

	static class Shop
	{
		static final int ITEMS_CONTAINER = 2;
	}

	static class Smithing
	{
		static final int DAGGER = 9;
		static final int SWORD = 10;
		static final int SCIMITAR = 11;
		static final int LONG_SWORD = 12;
		static final int TWO_H_SWORD = 13;
		static final int AXE = 14;
		static final int MACE = 15;
		static final int WARHAMMER = 16;
		static final int BATTLE_AXE = 17;
		static final int CLAWS = 18;
		static final int CHAIN_BODY = 19;
		static final int PLATE_LEGS = 20;
		static final int PLATE_SKIRT = 21;
		static final int PLATE_BODY = 22;
		static final int NAILS = 23;
		static final int MED_HELM = 24;
		static final int FULL_HELM = 25;
		static final int SQ_SHIELD = 26;
		static final int KITE_SHIELD = 27;
		static final int EXCLUSIVE1 = 28;
		static final int DART_TIPS = 29;
		static final int ARROW_HEADS = 30;
		static final int KNIVES = 31;
		static final int EXCLUSIVE2 = 32;
		static final int JAVELIN_HEADS = 33;
		static final int BOLTS = 34;
		static final int LIMBS = 35;
	}

	static class Equipment
	{
		static final int HELMET = 15;
		static final int CAPE = 16;
		static final int AMULET = 17;
		static final int WEAPON = 18;
		static final int BODY = 19;
		static final int SHIELD = 20;
		static final int LEGS = 21;
		static final int GLOVES = 22;
		static final int BOOTS = 23;
		static final int RING = 24;
		static final int AMMO = 25;
	}

	static class Minimap
	{
		static final int SPEC_CLICKBOX = 35;
		static final int WORLDMAP_ORB = 48;
	}

	static class ResizableViewport
	{
		static final int MULTICOMBAT_INDICATOR = 19;
	}

	static class ResizableViewportBottomLine
	{
		static final int MAGIC_TAB = 57;
	}

	static class Prayer
	{
		static final int THICK_SKIN = 9;
		static final int BURST_OF_STRENGTH = 10;
		static final int CLARITY_OF_THOUGHT = 11;
		static final int ROCK_SKIN = 12;
		static final int SUPERHUMAN_STRENGTH = 13;
		static final int IMPROVED_REFLEXES = 14;
		static final int RAPID_RESTORE = 15;
		static final int RAPID_HEAL = 16;
		static final int PROTECT_ITEM = 17;
		static final int STEEL_SKIN = 18;
		static final int ULTIMATE_STRENGTH = 19;
		static final int INCREDIBLE_REFLEXES = 20;
		static final int PROTECT_FROM_MAGIC = 21;
		static final int PROTECT_FROM_MISSILES = 22;
		static final int PROTECT_FROM_MELEE = 23;
		static final int RETRIBUTION = 24;
		static final int REDEMPTION = 25;
		static final int SMITE = 26;
		static final int SHARP_EYE = 27;
		static final int HAWK_EYE = 28;
		static final int EAGLE_EYE = 29;
		static final int MYSTIC_WILL = 30;
		static final int MYSTIC_LORE = 31;
		static final int MYSTIC_MIGHT = 32;
		static final int RIGOUR = 33;
		static final int CHIVALRY = 34;
		static final int PIETY = 35;
		static final int AUGURY = 36;
		static final int PRESERVE = 37;

		static final int DEADEYE = 29;
		static final int MYSTIC_VIGOUR = 32;
	}

	public static class QuickPrayer
	{
		static final int PRAYERS = 4;

		public static final int THICK_SKIN_CHILD_ID = 0;
		public static final int BURST_OF_STRENGTH_CHILD_ID = 1;
		public static final int CLARITY_OF_THOUGHT_CHILD_ID = 2;
		public static final int ROCK_SKIN_CHILD_ID = 3;
		public static final int SUPERHUMAN_STRENGTH_CHILD_ID = 4;
		public static final int IMPROVED_REFLEXES_CHILD_ID = 5;
		public static final int RAPID_RESTORE_CHILD_ID = 6;
		public static final int RAPID_HEAL_CHILD_ID = 7;
		public static final int PROTECT_ITEM_CHILD_ID = 8;
		public static final int STEEL_SKIN_CHILD_ID = 9;
		public static final int ULTIMATE_STRENGTH_CHILD_ID = 10;
		public static final int INCREDIBLE_REFLEXES_CHILD_ID = 11;
		public static final int PROTECT_FROM_MAGIC_CHILD_ID = 12;
		public static final int PROTECT_FROM_MISSILES_CHILD_ID = 13;
		public static final int PROTECT_FROM_MELEE_CHILD_ID = 14;
		public static final int RETRIBUTION_CHILD_ID = 15;
		public static final int REDEMPTION_CHILD_ID = 16;
		public static final int SMITE_CHILD_ID = 17;
		public static final int SHARP_EYE_CHILD_ID = 18;
		public static final int MYSTIC_WILL_CHILD_ID = 19;
		public static final int HAWK_EYE_CHILD_ID = 20;
		public static final int MYSTIC_LORE_CHILD_ID = 21;
		public static final int EAGLE_EYE_CHILD_ID = 22;
		public static final int MYSTIC_MIGHT_CHILD_ID = 23;
		public static final int RIGOUR_CHILD_ID = 24;
		public static final int CHIVALRY_CHILD_ID = 25;
		public static final int PIETY_CHILD_ID = 26;
		public static final int AUGURY_CHILD_ID = 27;
		public static final int PRESERVE_CHILD_ID = 28;

		public static final int DEADEYE_CHILD_ID = 22;
		public static final int MYSTIC_VIGOUR_CHILD_ID = 23;
	}
	
	static class RuinousPrayers
	{
		static final int ANCIENT_STRENGTH = 9;
		static final int ANCIENT_SIGHT = 10;
		static final int ANCIENT_WILL = 11;
		static final int TRINITAS = 12;
		static final int DECIMATE = 13;
		static final int ANNIHILATE = 14;
		static final int VAPORISE = 15;
		static final int DEFLECT_MELEE = 16;
		static final int DEFLECT_RANGED = 17;
		static final int DEFLECT_MAGIC = 18;
		static final int PURGE = 19;
		static final int REJUVENATION = 20;
		static final int RUINOUS_GRACE = 21;
		static final int WRATH = 22;
		static final int METABOLISE = 23;
		static final int BERSERKER = 24;
		static final int FUMUS_VOW = 25;
		static final int CRUORS_VOW = 26;
		static final int UMBRAS_VOW = 27;
		static final int GLACIES_VOW = 28;
		static final int GAMBIT = 29;
		static final int REBUKE = 30;
		static final int VINDICATION = 31;
	}
	
	public static class RuinousQuickPrayer
	{
		static final int PRAYERS = 4;
		
		public static final int ANCIENT_STRENGTH_CHILD_ID = 0;
		public static final int ANCIENT_SIGHT_CHILD_ID = 1;
		public static final int ANCIENT_WILL_CHILD_ID = 2;
		public static final int TRINITAS_CHILD_ID = 3;
		public static final int DECIMATE_CHILD_ID = 4;
		public static final int ANNIHILATE_CHILD_ID = 5;
		public static final int VAPORISE_CHILD_ID = 6;
		public static final int DEFLECT_MELEE_CHILD_ID = 7;
		public static final int DEFLECT_RANGED_CHILD_ID = 8;
		public static final int DEFLECT_MAGIC_CHILD_ID = 9;
		public static final int PURGE_CHILD_ID = 10;
		public static final int REJUVENATION_CHILD_ID = 11;
		public static final int RUINOUS_GRACE_CHILD_ID = 12;
		public static final int WRATH_CHILD_ID = 13;
		public static final int METABOLISE_CHILD_ID = 14;
		public static final int BERSERKER_CHILD_ID = 15;
		public static final int FUMUS_VOW_CHILD_ID = 16;
		public static final int CRUORS_VOW_CHILD_ID = 17;
		public static final int UMBRAS_CHILD_ID = 18;
		public static final int GLACIES_VOW_CHILD_ID = 19;
		public static final int GAMBIT_CHILD_ID = 20;
		public static final int REBUKE_CHILD_ID = 21;
		public static final int VINDICATION_CHILD_ID = 22;
	}

	static class Combat
	{
		static final int WEAPON_NAME = 1;
		static final int SPECIAL_ATTACK_BAR = 34;
		static final int SPECIAL_ATTACK_CLICKBOX = 36;
		static final int TOOLTIP = 41;
	}

	static class LevelUp
	{
		static final int CONTINUE = 3;
	}

	static class LightBox
	{
		static final int LIGHT_BOX_BUTTON_CONTAINER = 6;
	}

	static class FairyRing
	{
		static final int LEFT_ORB_CLOCKWISE = 19;
		static final int LEFT_ORB_COUNTER_CLOCKWISE = 20;
		static final int MIDDLE_ORB_CLOCKWISE = 21;
		static final int MIDDLE_ORB_COUNTER_CLOCKWISE = 22;
		static final int RIGHT_ORB_CLOCKWISE = 23;
		static final int RIGHT_ORB_COUNTER_CLOCKWISE = 24;
	}

	static class FairyRingCode
	{
		static final int FAIRY_QUEEN_HIDEOUT = 139;
	}

	static class WorldSwitcher
	{
		static final int CONTAINER = 1;
		static final int LOGOUT_BUTTON = 23;
	}
	
	static class Pvp
	{
		static final int PVP_WORLD_COMBAT_RANGE = 49;
	}

	// Also used for many other interfaces!
	static class BankPin
	{
		static final int TOP_LEFT_TEXT = 2;
		static final int FIRST_ENTERED = 3;
		static final int SECOND_ENTERED = 4;
		static final int THIRD_ENTERED = 5;
		static final int FOURTH_ENTERED = 6;
		static final int INSTRUCTION_TEXT = 10;
		static final int EXIT_BUTTON = 13;
		static final int FORGOT_BUTTON = 15;
		static final int BUTTON_1 = 16;
		static final int BUTTON_2 = 18;
		static final int BUTTON_3 = 20;
		static final int BUTTON_4 = 22;
		static final int BUTTON_5 = 24;
		static final int BUTTON_6 = 26;
		static final int BUTTON_7 = 28;
		static final int BUTTON_8 = 30;
		static final int BUTTON_9 = 32;
		static final int BUTTON_10 = 34;
	}

	static class DialogNotification
	{
		static final int TEXT = 0;
		static final int CONTINUE = 1;
	}

	static class PestControlExchangeWindow
	{
		static final int ITEM_LIST = 2;
		static final int BOTTOM = 5;
		static final int POINTS = 8;
		static final int CONFIRM_BUTTON = 6;
	}

	static class MinigameDialog
	{
		static final int TEXT = 1;
		static final int CONTINUE = 2;
	}

	static class TheatreOfBlood
	{
		static final int RAIDING_PARTY = 9;
		static final int ORB_BOX = 10;
		static final int BOSS_HEALTH_BAR = 35;
	}

	static class TheatreOfBloodParty
	{
		static final int CONTAINER = 10;
	}

	static class EquipmentWidgetIdentifiers
	{
		static final int EQUIP_YOUR_CHARACTER = 3;
		static final int STAB_ATTACK_BONUS = 24;
		static final int SLASH_ATTACK_BONUS = 25;
		static final int CRUSH_ATTACK_BONUS = 26;
		static final int MAGIC_ATTACK_BONUS = 27;
		static final int RANGED_ATTACK_BONUS = 28;
		static final int STAB_DEFENCE_BONUS = 30;
		static final int SLASH_DEFENCE_BONUS = 31;
		static final int CRUSH_DEFENCE_BONUS = 32;
		static final int MAGIC_DEFENCE_BONUS = 33;
		static final int RANGED_DEFENCE_BONUS = 34;
		static final int MELEE_STRENGTH = 36;
		static final int RANGED_STRENGTH = 37;
		static final int MAGIC_DAMAGE = 38;
		static final int PRAYER_BONUS = 39;
		static final int UNDEAD_DAMAGE_BONUS = 41;
		static final int SLAYER_DAMAGE_BONUS = 42;
		static final int WEIGHT = 49;
	}

	static class FossilMushroomTeleport
	{
		static final int ROOT = 2;
		static final int HOUSE_ON_HILL = 4;
		static final int VERDANT_VALLEY = 8;
		static final int SWAMP = 12;
		static final int MUSHROOM_MEADOW = 16;
	}

	static class SpellBook
	{
		static final int FILTERED_SPELLS_BOUNDS = 3;
		static final int TOOLTIP = 190;

		// LEAGUES HOME TELEPORTS
		static final int KOUREND_HOME_TELEPORT = 4;
		static final int CATHERBY_HOME_TELEPORT = 5;
		// NORMAL SPELLS
		static final int LUMBRIDGE_HOME_TELEPORT = 7;
		static final int WIND_STRIKE = 8;
		static final int CONFUSE = 9;
		static final int ENCHANT_CROSSBOW_BOLT = 10;
		static final int WATER_STRIKE = 11;
		static final int JEWELLERY_ENCHANTMENTS = 12;
		static final int EARTH_STRIKE = 14;
		static final int WEAKEN = 15;
		static final int FIRE_STRIKE = 16;
		static final int BONES_TO_BANANAS = 17;
		static final int WIND_BOLT = 18;
		static final int CURSE = 19;
		static final int BIND = 20;
		static final int LOW_LEVEL_ALCHEMY = 21;
		static final int WATER_BOLT = 22;
		static final int VARROCK_TELEPORT = 23;
		static final int EARTH_BOLT = 25;
		static final int LUMBRIDGE_TELEPORT = 26;
		static final int TELEKINETIC_GRAB = 27;
		static final int FIRE_BOLT = 28;
		static final int FALADOR_TELEPORT = 29;
		static final int CRUMBLE_UNDEAD = 30;
		static final int TELEPORT_TO_HOUSE = 31;
		static final int WIND_BLAST = 32;
		static final int SUPERHEAT_ITEM = 33;
		static final int CAMELOT_TELEPORT = 34;
		static final int WATER_BLAST = 35;
		static final int KOUREND_CASTLE_TELEPORT = 36;
		static final int IBAN_BLAST = 38;
		static final int SNARE = 39;
		static final int MAGIC_DART = 40;
		static final int ARDOUGNE_TELEPORT = 41;
		static final int EARTH_BLAST = 42;
		static final int CIVITAS_ILLA_FORTIS_TELEPORT = 43;
		static final int HIGH_LEVEL_ALCHEMY = 44;
		static final int CHARGE_WATER_ORB = 45;
		static final int WATCHTOWER_TELEPORT = 47;
		static final int FIRE_BLAST = 48;
		static final int CHARGE_EARTH_ORB = 49;
		static final int BONES_TO_PEACHES = 50;
		static final int SARADOMIN_STRIKE = 51;
		static final int CLAWS_OF_GUTHIX = 52;
		static final int FLAMES_OF_ZAMORAK = 53;
		static final int TROLLHEIM_TELEPORT = 54;
		static final int WIND_WAVE = 55;
		static final int CHARGE_FIRE_ORB = 56;
		static final int TELEPORT_TO_APE_ATOLL = 57;
		static final int WATER_WAVE = 58;
		static final int CHARGE_AIR_ORB = 59;
		static final int VULNERABILITY = 60;
		static final int EARTH_WAVE = 62;
		static final int ENFEEBLE = 63;
		static final int TELEOTHER_LUMBRIDGE = 64;
		static final int FIRE_WAVE = 65;
		static final int ENTANGLE = 66;
		static final int STUN = 67;
		static final int CHARGE = 68;
		static final int WIND_SURGE = 69;
		static final int TELEOTHER_FALADOR = 70;
		static final int WATER_SURGE = 71;
		static final int TELE_BLOCK = 72;
		static final int BOUNTY_TARGET_TELEPORT = 73;
		static final int TELEOTHER_CAMELOT = 75;
		static final int EARTH_SURGE = 76;
		static final int FIRE_SURGE = 78;

		// NORMAL SPELLS JEWELRY ENCHANTMENTS SUB MENU
		static final int LVL_1_ENCHANT = 13;
		static final int LVL_2_ENCHANT = 24;
		static final int LVL_3_ENCHANT = 37;
		static final int LVL_4_ENCHANT = 46;
		static final int LVL_5_ENCHANT = 61;
		static final int LVL_6_ENCHANT = 74;
		static final int LVL_7_ENCHANT = 77;

		// ANCIENT SPELLS
		static final int ICE_RUSH = 79;
		static final int ICE_BLITZ = 80;
		static final int ICE_BURST = 81;
		static final int ICE_BARRAGE = 82;
		static final int BLOOD_RUSH = 83;
		static final int BLOOD_BLITZ = 84;
		static final int BLOOD_BURST = 85;
		static final int BLOOD_BARRAGE = 86;
		static final int SMOKE_RUSH = 87;
		static final int SMOKE_BLITZ = 88;
		static final int SMOKE_BURST = 89;
		static final int SMOKE_BARRAGE = 90;
		static final int SHADOW_RUSH = 91;
		static final int SHADOW_BLITZ = 92;
		static final int SHADOW_BURST = 93;
		static final int SHADOW_BARRAGE = 94;
		static final int PADDEWWA_TELEPORT = 95;
		static final int SENNTISTEN_TELEPORT = 96;
		static final int KHARYRLL_TELEPORT = 97;
		static final int LASSAR_TELEPORT = 98;
		static final int DAREEYAK_TELEPORT = 99;
		static final int CARRALLANGER_TELEPORT = 100;
		static final int ANNAKARL_TELEPORT = 101;
		static final int GHORROCK_TELEPORT = 102;
		static final int EDGEVILLE_HOME_TELEPORT = 103;

		// LUNAR SPELLS
		static final int LUNAR_HOME_TELEPORT = 104;
		static final int BAKE_PIE = 105;
		static final int CURE_PLANT = 106;
		static final int MONSTER_EXAMINE = 107;
		static final int NPC_CONTACT = 108;
		static final int CURE_OTHER = 109;
		static final int HUMIDIFY = 110;
		static final int MOONCLAN_TELEPORT = 111;
		static final int TELE_GROUP_MOONCLAN = 112;
		static final int CURE_ME = 113;
		static final int HUNTER_KIT = 114;
		static final int WATERBIRTH_TELEPORT = 115;
		static final int TELE_GROUP_WATERBIRTH = 116;
		static final int CURE_GROUP = 117;
		static final int STAT_SPY = 118;
		static final int BARBARIAN_TELEPORT = 119;
		static final int TELE_GROUP_BARBARIAN = 120;
		static final int SUPERGLASS_MAKE = 121;
		static final int TAN_LEATHER = 122;
		static final int KHAZARD_TELEPORT = 123;
		static final int TELE_GROUP_KHAZARD = 124;
		static final int DREAM = 125;
		static final int STRING_JEWELLERY = 126;
		static final int STAT_RESTORE_POT_SHARE = 127;
		static final int MAGIC_IMBUE = 128;
		static final int FERTILE_SOIL = 129;
		static final int BOOST_POTION_SHARE = 130;
		static final int FISHING_GUILD_TELEPORT = 131;
		static final int TELE_GROUP_FISHING_GUILD = 132;
		static final int PLANK_MAKE = 133;
		static final int CATHERBY_TELEPORT = 134;
		static final int TELE_GROUP_CATHERBY = 135;
		static final int RECHARGE_DRAGONSTONE = 136;
		static final int ICE_PLATEAU_TELEPORT = 137;
		static final int TELE_GROUP_ICE_PLATEAU = 138;
		static final int ENERGY_TRANSFER = 139;
		static final int HEAL_OTHER = 140;
		static final int VENGEANCE_OTHER = 141;
		static final int VENGEANCE = 142;
		static final int HEAL_GROUP = 143;
		static final int SPELLBOOK_SWAP = 144;
		static final int GEOMANCY = 145;
		static final int SPIN_FLAX = 146;
		static final int OURANIA_TELEPORT = 147;

		// ARCEUUS SPELLS
		static final int ARCEUUS_HOME_TELEPORT = 148;
		static final int BASIC_REANIMATION = 149;
		static final int ARCEUUS_LIBRARY_TELEPORT = 150;
		static final int ADEPT_REANIMATION = 151;
		static final int EXPERT_REANIMATION = 152;
		static final int MASTER_REANIMATION = 153;
		static final int DRAYNOR_MANOR_TELEPORT = 154;
		static final int MIND_ALTAR_TELEPORT = 156;
		static final int RESPAWN_TELEPORT = 157;
		static final int SALVE_GRAVEYARD_TELEPORT = 158;
		static final int FENKENSTRAINS_CASTLE_TELEPORT = 159;
		static final int WEST_ARDOUGNE_TELEPORT = 160;
		static final int HARMONY_ISLAND_TELEPORT = 161;
		static final int CEMETERY_TELEPORT = 162;
		static final int RESURRECT_CROPS = 163;
		static final int BARROWS_TELEPORT = 164;
		static final int APE_ATOLL_TELEPORT = 165;
		static final int BATTLEFRONT_TELEPORT = 166;
		static final int INFERIOR_DEMONBANE = 167;
		static final int SUPERIOR_DEMONBANE = 168;
		static final int DARK_DEMONBANE = 169;
		static final int MARK_OF_DARKNESS = 170;
		static final int GHOSTLY_GRASP = 171;
		static final int SKELETAL_GRASP = 172;
		static final int UNDEAD_GRASP = 173;
		static final int WARD_OF_ARCEUUS = 174;
		static final int LESSER_CORRUPTION = 175;
		static final int GREATER_CORRUPTION = 176;
		static final int DEMONIC_OFFERING = 177;
		static final int SINISTER_OFFERING = 178;
		static final int DEGRIME = 179;
		static final int SHADOW_VEIL = 180;
		static final int VILE_VIGOUR = 181;
		static final int DARK_LURE = 182;
		static final int DEATH_CHARGE = 183;
		static final int RESURRECT_LESSER_GHOST = 184;
		static final int RESURRECT_LESSER_SKELETON = 185;
		static final int RESURRECT_LESSER_ZOMBIE = 186;
		static final int RESURRECT_SUPERIOR_GHOST = 187;
		static final int RESURRECT_SUPERIOR_SKELETON = 188;
		static final int RESURRECT_SUPERIOR_ZOMBIE = 189;
		static final int RESURRECT_GREATER_GHOST = 190;
		static final int RESURRECT_GREATER_SKELETON = 191;
		static final int RESURRECT_GREATER_ZOMBIE = 192;
	}

	static class DialogSprite2
	{
		static final int SPRITE1 = 1;
		static final int TEXT = 2;
		static final int SPRITE2 = 3;
		static final int CONTINUE = 4;
	}

	static class QuestTab
	{
		static final int QUEST_TAB = 3;
	}

	public static class TradeScreen
	{
		public static final int FIRST_TRADING_WITH = 31;
		public static final int SECOND_ACCEPT_FUNC = 13;
		public static final int SECOND_DECLINE_FUNC = 14;
		public static final int SECOND_MY_OFFER = 23;
		public static final int SECOND_THEIR_OFFER = 24;
		public static final int SECOND_ACCEPT_TEXT = 25;
		public static final int SECOND_DECLINE_TEXT = 26;
		public static final int SECOND_MY_ITEMS = 28;
		public static final int SECOND_THEIR_ITEMS = 29;
		public static final int SECOND_TRADING_WITH = 30;
	}

	static class JewelBox
	{
		static final int DUEL_RING = 2;
		static final int GAME_NECK = 3;
		static final int COMB_BRAC = 4;
		static final int SKIL_NECK = 5;
		static final int RING_OFGP = 6;
		static final int AMUL_GLOR = 7; // yes
	}

	static class Options
	{
		static final int CAMERA_ZOOM_SLIDER_HANDLE = 15;
		static final int MUSIC_SLIDER = 37;
		static final int SOUND_EFFECT_SLIDER = 43;
		static final int AREA_SOUND_SLIDER = 49;
	}

	static class GauntletMap
	{
		static final int CONTAINER = 4;
	}
}
