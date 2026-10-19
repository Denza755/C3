/*
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA.
 *
 * http://www.gnu.org/copyleft/gpl.html
 */
package net.sf.l2j;

import java.io.*;
import java.util.ArrayList;
import java.util.Properties;
import java.util.logging.Logger;

import net.sf.l2j.gameserver.model.L2PcInstance;

import java.io.LineNumberReader;
import java.util.StringTokenizer;
import java.io.IOException;
/**
 * This class containce global server configuration.
 * It has static final fields initialized from configuration files.
 * It's initialized at the very begin of startup, and later JIT will optimize
 * away debug/unused code.
 *
 * @author mkizub
 */
public final class Config {

	protected static Logger _log = Logger.getLogger(Config.class.getName());

    public static boolean TEST_SERVER;

	/** Game/Login Server ports */
	public static final int PORT_GAME	= Integer.parseInt(serverSettings("GameserverPort", "7777"));
	public static final int PORT_LOGIN	= Integer.parseInt(serverSettings("LoginserverPort", "2106"));
	public static int	LOGIN_TRY_BEFORE_BAN = Integer.parseInt(serverSettings("LoginTryBeforeBan", "20"));
	public static final String GAMESERVER_HOSTNAME = serverSettings("GameserverHostname", "");

	public static final int WebServerPort = Integer.parseInt(serverSettings("WebServerPort", "0"));
	public static final String WebServerRoot = serverSettings("WebServerRoot", "./data/webserver");

	public static File DATAPACK_ROOT;

	public static final String DATABASE_DRIVER;
	public static final String DATABASE_URL;
	public static final String DATABASE_LOGIN;
	public static final String DATABASE_PASSWORD;
	public static Integer DATABASE_MAX_CONNECTIONS;

	public static Integer   MAXIMUM_ONLINE_USERS;

	/** Thread pools size */
    public static int THREAD_P_HIGH;
    public static int THREAD_P_MED;
    public static int THREAD_P_LOW;

	/** Auto-loot? */
    public static boolean AUTO_LOOT;

	/** Character name template */
	public static String	CNAME_TEMPLATE = serverSettings("CnameTemplate", ".*");;

	/** Global chat state */
	public static String	DEFAULT_GLOBAL_CHAT;

	/** ChatBan */
	public static Boolean	MAT_BANCHAT;
	public static final int     UNCHATBANTIME;
	public static ArrayList<String> MAT_LIST = new ArrayList<String>();

	/** Saving SpS? */
	public static Boolean	SAVING_SPS;
	public static Boolean MANAHEAL_SPS_BONUS;

	/** Count kills? */
	public static Boolean	KILL_COUNTER;

	/** Count dropped? */
	public static Boolean DROP_COUNTER;
	
	/** Count crafted? */
	public static Boolean CRAFT_COUNTER;

	/** For test servers - evrybody has admin rights */
    public static boolean EVERYBODY_HAS_ADMIN_RIGHTS;

    public static int CRAFT_TIME = Integer.parseInt(serverSettings("CraftTime","300"));

    public static int ALT_MAX_RECIPES;

    public static boolean ALT_GAME_SKILL_LEARN;

    /** For small servers */
    public static boolean ALT_GAME_MATHERIALSDROP;

    /** Show mob stats/droplist to players? */
    public static boolean ALT_GAME_SHOW_DROPLIST;

	/** Alternative success rate formulas for skills such root/sleep/stun */
	public static String	ALT_GAME_SKILL_FORMULAS;

	/** Alternative freight modes - Freights can be withdrawed from any village */
    public static boolean ALT_GAME_FREIGHTS;
	/** Alternative freight modes - Sets the price value for each freightened item */
    public static int ALT_GAME_FREIGHT_PRICE;

	/** Alternative gameing - loss of XP on death */
    public static boolean ALT_GAME_DELEVEL;

    public static int CORPSE_MOB_TIME;

    public static boolean REBUILD_CAST_TARGETS_LIST;

	/** Enable Rate Hp  */
	public static boolean	ENABLE_RATE_HP;

    public static boolean ALT_PRIVILEGES_ADMIN = Boolean.parseBoolean(otherSettings("AltPrivilegesAdmin", "False"));

	/** Logging Chat Window */
	public static Boolean	LOG_CHAT;
	public static Boolean	LOG_TELNET;
    public static boolean LOG_ITEMS;
	/***************************************************************************
	 * GM CONFIG General GM AccessLevel *
	 **************************************************************************/
	public static Integer	GM_ACCESSLEVEL;
	/** General GM Minimal AccessLevel */
	public static Integer	GM_MIN;
	/** General GM AccessLevel to change announcements */
	public static Integer	GM_ANNOUNCE;
	/** General GM AccessLevel can /ban /unban */
	public static Integer	GM_BAN;
	public static Integer	GM_BAN_CHAT;
	/** General GM AccessLevel can /create_item and /gmshop */
	public static Integer	GM_CREATE_ITEM;
	/** General GM AccessLevel can /delete */
	public static Integer	GM_DELETE;
	/** General GM AccessLevel can /kick /disconnect */
	public static Integer	GM_KICK;
	/** General GM AccessLevel for access to GMMenu */
	public static Integer	GM_MENU;
	/** General GM AccessLevel tu use god mode command */
	public static Integer	GM_GODMODE;
	/** General GM AccessLevel with character edit rights */
	public static Integer	GM_CHAR_EDIT;
	public static Integer	GM_CHAR_VIEW;
	/** General GM AccessLevel with NPC edit rights */
	public static Integer	GM_NPC_EDIT;
	public static Integer	GM_NPC_VIEW;
	/** General GM AccessLevel to teleport to any location */
	public static Integer	GM_TELEPORT;
	/** General GM AccessLevel to restart server */
	public static Integer	GM_RESTART;
	/** General GM AccessLevel for MonsterRace */
	public static Integer	GM_MONSTERRACE;
	/** General GM AccessLevel to ride Wyvern */
	public static Integer	GM_RIDER;
	/** General GM AccessLevel to unstuck without 5min delay */
	public static Integer	GM_ESCAPE;
	/** General GM AccessLevel to resurect fixed after death */
	public static Integer	GM_FIXED;
	/** General GM AccessLevel to run Forth scripts by default command */
	public static Integer	GM_USE_FORTH;
	/** General GM AccessLevel to create Path Nodes */
	public static Integer	GM_CREATE_NODES;
	/** General GM AccessLevel to close/open Doors */
	public static Integer	GM_DOOR;
	/** General GM AccessLevel with Resurrection rights */
	public static Integer	GM_RES;
	/** General GM AccessLevel to attack in the peace zone */
	public static Integer	GM_PEACEATTACK;
	/** General GM AccessLevel to heal */
	public static Integer	GM_HEAL;
	/** General GM AccessLevel to unblock IPs detected as hack IPs */
	public static Integer	GM_UNBLOCK;


	/** Castle Guards stuff */
	public static Integer	CG_HIREPRICE;
	public static Integer	CG_HIRE;
	public static Integer	CG_KILL;
	public static Integer	CG_MAXHIREDGUARDS;
	public static Integer	CG_VIEWSTATS;
	/**
	 * this is not a clasical probability number but the higher the value is,
	 * the more the probablity of stats up is
	 */
	public static Integer	CG_STATUPPROB;
	public static Integer	CG_STATUPGAIN;
	public static Integer	CG_MOVEMENT;
	public static Integer	CG_SET;
	public static Integer	CG_CASTLE;
	public static Boolean CG_SPAWNSIEGEGUARDS;

	/** Rate control */
    public static float RATE_XP;
    public static float RATE_SP;
    public static float RATE_QUESTS_REWARD;
    public static float RATE_DROP_ADENA;
    public static float RATE_DROP_ITEMS;
    public static float RATE_DROP_SPOIL;
    public static int   RATE_DROP_EQUIPMENT;
    public static float RATE_SIEGE_GUARDS_PRICE;
    public static float RATE_RAID_REGEN_HP;
    public static float RATE_RAID_REGEN_MP;

	/** Player Drop Rate control */
    public static int PLAYER_RATE_DROP 			= Integer.parseInt(serverSettings("PlayerRateDrop", "5"));
    public static int PLAYER_RATE_DROP_EQUIP	= Integer.parseInt(serverSettings("PlayerRateDropEquip", "25"));
    public static int PLAYER_RATE_DROP_EQUIP_WEAPON	= Integer.parseInt(serverSettings("PlayerRateDropEquipWeapon", "5"));
    public static int PLAYER_MAX_DROP_ITEMS		= Integer.parseInt(serverSettings("PlayerMaxDropItems", "3"));

	public static Integer	 AUTODESTROY_ITEM_AFTER;

    public static int     DELETE_DAYS;

	/** Maximum range mobs can randomly go from spawn point */
	public static Integer	 MAX_DRIFT_RANGE;


	public static Boolean WEAR_TEST_ENABLED;

	// Allow L2Walker client
	public static enum L2WalkerAllowed
	{
		True,
		False,
		GM
	}

	public static final L2WalkerAllowed ALLOW_L2WALKER_CLIENT;
	public static Boolean	AUTOBAN_L2WALKER_ACC;
	public static Integer		L2WALKER_REVISION;

	public static Boolean	ALLOW_DISCARDITEM;
	public static Boolean	ALLOW_FREIGHT;
	public static Boolean	ALLOW_WAREHOUSE;

	// Pets
	public static Integer		WYVERN_SPEED;
	public static Integer		STRIDER_SPEED;

	// protocol revision
	public static Integer		MIN_PROTOCOL_REVISION;
	public static Integer		MAX_PROTOCOL_REVISION;

	// random animation interval
	public static Integer		MIN_NPC_ANIMATION;
	public static Integer		MAX_NPC_ANIMATION;

	public static Boolean	ACTIVATE_POSITION_RECORDER;
	public static Boolean	USE_3D_MAP;
	public static Boolean	SHOW_LEVEL_COMMUNITYBOARD;
	public static Boolean	SHOW_STATUS_COMMUNITYBOARD;

	public static Boolean	ENABLE_TRANSLIT_ENGINE;
	public static Boolean	ENABLE_TEXT_FILTER;

	public static Integer	RESTART_TIME;

	/** Configuration files */
	public static final String	ID_CONFIG_FILE		= "/config/idfactory.properties";
	public static final String	OTHER_CONFIG_FILE		= "/config/other.properties";
	public static final String	SPOIL_CONFIG_FILE		= "/config/spoil.properties";
	public static final String	ALT_SETTINGS_FILE		= "/config/altsettings.properties";
	public static final String	PVP_CONFIG_FILE		= "/config/pvp.properties";
	public static final String	GM_ACCESS_FILE		= "/config/GMAccess.properties";
	public static final String	TELNET_FILE			= "/config/telnet.properties";
	public static final String	CONFIGURATION_FILE	= "/config/server.properties";
	public static final String	VERSION_FILE			= "/config/l2j-version.properties";
	public static final String	SIEGE_CONFIGURATION_FILE = "/config/siege.properties";
	public static final String	BANNED_IP_XML			= "/config/banned.xml";
	public static final String  MAT_CONFIG_FILE          = "/config/mats.cfg";

	public static Integer		GAME_SERVER_LOGIN_PORT   = 9013;
	public static final String	INTERNAL_HOSTNAME;
	public static final String	EXTERNAL_HOSTNAME;
	public static Integer		PATH_NODE_RADIUS;
	public static int			NEW_NODE_ID;
	public static int			SELECTED_NODE_ID;
	public static int			LINKED_NODE_ID;
	public static String		NEW_NODE_TYPE;
	public static Boolean	SERVER_SIDE_NPC_NAME;
	public static Boolean	SERVER_SIDE_NPC_NAME_WITH_LVL;
	public static String	ALLOW_CLASS_MASTERS;
    public static ArrayList<Integer> ALLOW_CLASS_MASTERS_LIST = new ArrayList<Integer>();
    
	public static String		SERVER_VERSION;
	public static String		SERVER_BUILD_DATE;

    /** Inventory slots limits */
    public static int INVENTORY_MAXIMUM_NO_DWARF;
    public static int INVENTORY_MAXIMUM_DWARF;
    public static int INVENTORY_MAXIMUM_GM;

    /** Warehouse slots limits */
    public static int WAREHOUSE_SLOTS_NO_DWARF;
    public static int WAREHOUSE_SLOTS_DWARF;
    public static int WAREHOUSE_SLOTS_CLAN;
    public static int FREIGHT_SLOTS;

	/** Spoil Rates */
	public static Boolean CAN_SPOIL_LOWER_LEVEL_MOBS;
	public static Float 	MAXIMUM_PLAYER_AND_MOB_LEVEL_DIFFERENCE;
	public static Float 	BASE_SPOIL_RATE;
	public static Float 	MINIMUM_SPOIL_RATE;
	public static Float 	SPOIL_LEVEL_DIFFERENCE_LIMIT;
	public static Float 	SPOIL_LEVEL_DIFFERENCE_MULTIPLIER;
	public static Integer 	LAST_LEVEL_SPOIL_IS_LEARNED;

	/** Karma System Variables */
	public static Integer 	KARMA_MIN_KARMA;
	public static Integer 	KARMA_MAX_KARMA;

	public static Integer 	KARMA_XP_DIVIDER = Integer.parseInt(pvpSettings("XPDivider", "72"));

	public static Integer 	KARMA_LOST_BASE;
	public static Boolean KARMA_DROP_EQUIPPED_ITEMS;
	public static Boolean KARMA_DROP_EQUIPPED_WEAPONS;
	public static Boolean KARMA_DROP_GM			= Boolean.parseBoolean(pvpSettings("CanGMDropEquipment", "false"));

	public static Integer 	KARMA_DROP_CONSTANT	= Integer.parseInt(pvpSettings("DropConstant", "10000"));
	public static Integer 	KARMA_PK_LIMIT		= Integer.parseInt(pvpSettings("MinimumPKRequiredToDrop", "3"));
	public static Integer 	KARMA_DROP_ITEM_LIMIT=Integer.parseInt(pvpSettings("MaxItemsDroppable", "10"));

	public static Integer 	KARMA_RANDOM_DROP_LOCATION_LIMIT = 	Integer.parseInt(pvpSettings("MaxDropThrowDistance", "70"));

	public static Integer 	KARMA_DROPCHANCE_MINIMUM		= Integer.parseInt(pvpSettings("ChanceOfDropMinimum", "30"));
	public static Integer	KARMA_DROPCHANCE_INITIAL_CHECK	= Integer.parseInt(pvpSettings("ChanceOfDropInitial", "50"));
	public static Integer 	KARMA_DROPCHANCE_EQUIPMENT		= Integer.parseInt(pvpSettings("ChanceOfDropEquipped", "20"));
	public static Integer 	KARMA_DROPCHANCE_EQUIPPED_WEAPON= Integer.parseInt(pvpSettings("ChanceOfDropEquippedWeapon", "25"));


    public static float 	RATE_KARMA_EXP_LOST  			= Float.parseFloat(serverSettings("RateKarmaExpLost", "6."));

	public static final String	KARMA_NONDROPPABLE_ITEMS	= pvpSettings("ListOfNonDroppableItems", "57,1147,425,1146,461,10,2368,7,6,2370,2369,3500,3501,3502,4422,4423,4424,2375");

	public static ArrayList<Integer> KARMA_LIST_NONDROPPABLE_ITEMS = new ArrayList<Integer>();

	public static Integer PVP_TIME;

	/** Karma Punishment */
	public static Boolean ALT_GAME_KARMAPLAYERCANBEKILLEDINPEACEZONE;
    public static boolean ALT_GAME_KARMA_PLAYER_CAN_SHOP;

    public static int ZONE_TOWN = Integer.parseInt(serverSettings("ZoneTown", "0"));

	/** Day/Night Status **/
	public static Boolean DAY_STATUS_FORCE_CLIENT_UPDATE;
	public static Integer	 	DAY_STATUS_SUN_RISE_AT;
	public static Integer	 	DAY_STATUS_SUN_SET_AT;


	public static enum IdFactoryType
	{
		Compaction,
		BitSet,
		Stack
	}

	public static final IdFactoryType IDFACTORY_TYPE;
	public static Boolean BAD_ID_CHECKING;

	public static Integer		ADMIN_TEST;

	public static Boolean	AUTODELETE_INVALID_QUEST_DATA;

	/** Chance that an item will succesfully be enchanted */
	public static Integer		ENCHANT_CHANCE;
	public static Integer		ENCHANT_MAX;

	public static Double	HP_REGEN_MULTIPLIER;
	public static Double	MP_REGEN_MULTIPLIER;
	public static Double	CP_REGEN_MULTIPLIER;
	public static boolean	REGEN_SIT_WAIT;

	public static Integer	   	STARTING_ADENA;

	/** Deep Blue Mobs' Drop Rules Enabled */
	public static Boolean	DEEPBLUE_DROP_RULES;
	public static Integer 	UNSTUCK_INTERVAL;

	// telnet enabled
	public static Boolean	IS_TELNET_ENABLED;

	/** Define Party XP cutoff point method - Possible values: level and percentage */
	public static final String	PARTY_XP_CUTOFF_METHOD;
	/** Define the cutoff point value for the "level" method */
	public static Integer 	PARTY_XP_CUTOFF_LEVEL;
	/** Define the cutoff point value for the "percentage" method */
	public static Double 	PARTY_XP_CUTOFF_PERCENT;

	/** Percent CP is restore on respawn */
	public static Double 	RESPAWN_RESTORE_CP;
	/** Percent HP is restore on respawn */
	public static Double 	RESPAWN_RESTORE_HP;
	/** Percent MP is restore on respawn */
	public static Double 	RESPAWN_RESTORE_MP;

	/** Maximum number of available slots for pvt stores (sell/buy) - Dwarves */
	public static Integer  	MAX_PVTSTORE_SLOTS_DWARF;
	/** Maximum number of available slots for pvt stores (sell/buy) - Others */
	public static Integer  	MAX_PVTSTORE_SLOTS_OTHER;

	public static boolean SHOW_LICENCE;

    /** Deafult punishment for illegal actions */
    public static int DEFAULT_PUNISH;

	/** Debug/release mode */
	public static boolean DEBUG 	= Boolean.parseBoolean(serverSettings("Debug", "false"));
	/** Enable/disable code 'in progress' */
	public static boolean DEVELOPER	= Boolean.parseBoolean(serverSettings("Developer", "false"));
	/** Enable/disable assertions */
	public static boolean ASSERT	= Boolean.parseBoolean(serverSettings("Assert", "false"));

	public static boolean checkLangFilesModify = Boolean.valueOf(serverSettings("checkLangFilesModify", "false"));
	public static boolean useFileCache = Boolean.valueOf(serverSettings("useFileCache", "true"));

    public static boolean ANNOUNCE_MAMMON_SPAWN = Boolean.parseBoolean(otherSettings("AnnounceMammonSpawn", "True"));

	public static Properties serverSettingsProp = null;
	public static final String serverSettings(String prop, String def)
	{
		if(serverSettingsProp == null)
		{
			try
			{
				serverSettingsProp = new Properties();
				InputStream is = new FileInputStream(Server.fortressHomeDir+CONFIGURATION_FILE);
				serverSettingsProp.load(is);
				is.close();
			}
			catch (Exception e)
			{
				e.printStackTrace();
				throw new Error("Failed to Load "+Server.fortressHomeDir+CONFIGURATION_FILE+" File.");
			}
		}

		return serverSettingsProp.getProperty(prop, def);
	}

	public static Properties pvpSettingsProp = null;
	public static final String pvpSettings(String prop, String def)
	{
		if(pvpSettingsProp == null)
		{
			try
			{
				pvpSettingsProp = new Properties();
				InputStream is = new FileInputStream(Server.fortressHomeDir+PVP_CONFIG_FILE);
				pvpSettingsProp.load(is);
				is.close();
			}
			catch (Exception e)
			{
				e.printStackTrace();
				throw new Error("Failed to Load "+Server.fortressHomeDir+PVP_CONFIG_FILE+" File.");
			}
		}

		return pvpSettingsProp.getProperty(prop, def);
	}

	public static Properties otherSettingsProp = null;
	public static final String otherSettings(String prop, String def)
	{
		if(otherSettingsProp == null)
		{
			try
			{
				otherSettingsProp = new Properties();
				InputStream is = new FileInputStream(Server.fortressHomeDir+OTHER_CONFIG_FILE);
				otherSettingsProp.load(is);
				is.close();
			}
			catch (Exception e)
			{
				e.printStackTrace();
				throw new Error("Failed to Load "+Server.fortressHomeDir+OTHER_CONFIG_FILE+" File.");
			}
		}

		return otherSettingsProp.getProperty(prop, def);
	}

	static {
		try {
			Properties serverSettings = new Properties();
			InputStream is = new FileInputStream(Server.fortressHomeDir+CONFIGURATION_FILE);
			serverSettings.load(is);
			is.close();

			DATAPACK_ROOT 		= new File(serverSettings.getProperty("DatapackRoot", ".")).getCanonicalFile();

			DEFAULT_GLOBAL_CHAT	= serverSettings.getProperty("GlobalChat", "ON");
			EVERYBODY_HAS_ADMIN_RIGHTS	= Boolean.parseBoolean(serverSettings.getProperty("EverybodyHasAdminRights", "false"));
			LOG_CHAT			= Boolean.valueOf(serverSettings.getProperty("LogChat", "false"));
			LOG_TELNET			= Boolean.valueOf(serverSettings.getProperty("LogTelnet", "false"));
			LOG_ITEMS                    = Boolean.valueOf(serverSettings.getProperty("LogItems", "false"));

			CG_HIREPRICE		= Integer.parseInt(serverSettings.getProperty("AncientAdena", "100"));
			CG_HIRE				= Integer.parseInt(serverSettings.getProperty("CGHire", "110"));
			CG_MAXHIREDGUARDS	= Integer.parseInt(serverSettings.getProperty("CGMaxHiredGuards", "50"));
			CG_KILL			 	= Integer.parseInt(serverSettings.getProperty("CGKill", "70"));
			CG_VIEWSTATS		= Integer.parseInt(serverSettings.getProperty("CGViewStats", "80"));
			CG_STATUPGAIN	   	= Integer.parseInt(serverSettings.getProperty("CGStatUpGain", "1"));
			CG_STATUPPROB	   	= Integer.parseInt(serverSettings.getProperty("CGStatUpProb", "1"));
			CG_MOVEMENT		 	= Integer.parseInt(serverSettings.getProperty("CGMovement", "40"));
			CG_CASTLE		   	= Integer.parseInt(serverSettings.getProperty("CGCastle", "120"));
			CG_SET			  	= Integer.parseInt(serverSettings.getProperty("CGSet", "100"));
			CG_SPAWNSIEGEGUARDS = Boolean.parseBoolean(serverSettings.getProperty("CGSpawnSiegeGuards", "false"));

			ADMIN_TEST		  	= Integer.parseInt(serverSettings.getProperty("ADMINTest", "100"));

			RATE_XP			  	= Float.parseFloat(serverSettings.getProperty("RateXp", "1."));
			RATE_SP			  	= Float.parseFloat(serverSettings.getProperty("RateSp", "1."));
			RATE_QUESTS_REWARD  = Float.parseFloat(serverSettings.getProperty("RateQuestsReward", "1."));
			RATE_DROP_ADENA	  	= Float.parseFloat(serverSettings.getProperty("RateDropAdena", "1."));
			RATE_DROP_ITEMS	  	= Float.parseFloat(serverSettings.getProperty("RateDropItems", "1."));
			RATE_DROP_SPOIL	  	= Float.parseFloat(serverSettings.getProperty("RateDropSpoil", "1."));
			RATE_DROP_EQUIPMENT  = Integer.parseInt(serverSettings.getProperty("RateKarmaDropEquipment", "1"));
			RATE_SIEGE_GUARDS_PRICE  = Float.parseFloat(serverSettings.getProperty("RateSiegeGuardsPrice", "1."));
			RATE_RAID_REGEN_HP  = Float.parseFloat(serverSettings.getProperty("RateRaidRegenHp", "500."));
			RATE_RAID_REGEN_MP  = Float.parseFloat(serverSettings.getProperty("RateRaidRegenMp", "500."));

			AUTODESTROY_ITEM_AFTER = Integer.parseInt(serverSettings.getProperty("AutoDestroyDroppedItemAfter", "0"));
			DELETE_DAYS = Integer.parseInt(serverSettings.getProperty("DeleteCharAfterDays", "7"));

			MAX_DRIFT_RANGE		= Integer.parseInt(serverSettings.getProperty("MaxDriftRange", "300"));

			ALLOW_L2WALKER_CLIENT	= L2WalkerAllowed.valueOf(serverSettings.getProperty("AllowL2Walker", "False"));
			AUTOBAN_L2WALKER_ACC	= Boolean.valueOf(serverSettings.getProperty("AutobanL2WalkerAcc", "False"));
			L2WALKER_REVISION	= Integer.parseInt(serverSettings.getProperty("L2WalkerRevision", "537"));

			ALLOW_DISCARDITEM	= Boolean.valueOf(serverSettings.getProperty("AllowDiscardItem", "True"));
			ALLOW_FREIGHT		= Boolean.valueOf(serverSettings.getProperty("AllowFreight", "True"));
			ALLOW_WAREHOUSE		= Boolean.valueOf(serverSettings.getProperty("AllowWarehouse", "True"));

			MIN_PROTOCOL_REVISION	= Integer.parseInt(serverSettings.getProperty("MinProtocolRevision", "551"));
			MAX_PROTOCOL_REVISION	= Integer.parseInt(serverSettings.getProperty("MaxProtocolRevision", "553"));

			if (MIN_PROTOCOL_REVISION > MAX_PROTOCOL_REVISION)
			{
				throw new Error("MinProtocolRevision is bigger than MaxProtocolRevision in server configuration file.");
			}

			ACTIVATE_POSITION_RECORDER	= Boolean.valueOf(serverSettings.getProperty("ActivatePositionRecorder", "False"));
			USE_3D_MAP				= Boolean.valueOf(serverSettings.getProperty("Use3DMap", "False"));
			SHOW_LEVEL_COMMUNITYBOARD	= Boolean.valueOf(serverSettings.getProperty("ShowLevelOnCommunityBoard", "False"));
			SHOW_STATUS_COMMUNITYBOARD	= Boolean.valueOf(serverSettings.getProperty("ShowStatusOnCommunityBoard", "True"));
			MIN_NPC_ANIMATION		= Integer.parseInt(serverSettings.getProperty("MinNPCAnimation", "0"));
			MAX_NPC_ANIMATION		= Integer.parseInt(serverSettings.getProperty("MaxNPCAnimation", "0"));

			INTERNAL_HOSTNAME   	= serverSettings.getProperty("InternalHostname", "localhost");
			EXTERNAL_HOSTNAME		= serverSettings.getProperty("ExternalHostname", "localhost");

			PATH_NODE_RADIUS		= Integer.parseInt(serverSettings.getProperty("PathNodeRadius", "50"));
			NEW_NODE_ID				= Integer.parseInt(serverSettings.getProperty("NewNodeId", "7952"));
			SELECTED_NODE_ID		= Integer.parseInt(serverSettings.getProperty("NewNodeId", "7952"));
			LINKED_NODE_ID			= Integer.parseInt(serverSettings.getProperty("NewNodeId", "7952"));
			NEW_NODE_TYPE			= serverSettings.getProperty("NewNodeType", "npc");

			SERVER_SIDE_NPC_NAME	= Boolean.valueOf(serverSettings.getProperty("ServerSideNpcName", "False"));
			SERVER_SIDE_NPC_NAME_WITH_LVL   = Boolean.valueOf(serverSettings.getProperty("ServerSideNpcNameWithLvl", "False"));

			AUTODELETE_INVALID_QUEST_DATA   = Boolean.valueOf(serverSettings.getProperty("AutoDeleteInvalidQuestData", "False"));
			THREAD_P_HIGH			= Integer.parseInt(serverSettings.getProperty("ThreadPoolSizeHigh", "4"));
			THREAD_P_MED			= Integer.parseInt(serverSettings.getProperty("ThreadPoolSizeMed", "10"));
			THREAD_P_LOW			= Integer.parseInt(serverSettings.getProperty("ThreadPoolSizeLow", "4"));

			MAXIMUM_ONLINE_USERS	= Integer.parseInt(serverSettings.getProperty("MaximumOnlineUsers", "100"));

			DAY_STATUS_FORCE_CLIENT_UPDATE  = Boolean.valueOf(serverSettings.getProperty("DayStatusForceClientUpdate", "True"));
			DAY_STATUS_SUN_RISE_AT	= Integer.parseInt(serverSettings.getProperty("DayStatusSunRiseAt", "6"));
			DAY_STATUS_SUN_SET_AT	= Integer.parseInt(serverSettings.getProperty("DayStatusSunSetAt", "18"));

			DATABASE_DRIVER			= serverSettings.getProperty("Driver", "com.mysql.jdbc.Driver");
			DATABASE_URL			= serverSettings.getProperty("URL", "jdbc:mysql://localhost/l2jdb");
			DATABASE_LOGIN			= serverSettings.getProperty("Login", "root");
			DATABASE_PASSWORD		= serverSettings.getProperty("Password", "");
			DATABASE_MAX_CONNECTIONS	= Integer.parseInt(serverSettings.getProperty("MaximumDbConnections", "10"));
			MAT_BANCHAT				= Boolean.valueOf(serverSettings.getProperty("MAT_BANCHAT", "False"));
			UNCHATBANTIME			=Integer.parseInt(serverSettings.getProperty("Timer_to_UnBan", "30"));
			ENABLE_TRANSLIT_ENGINE	= Boolean.valueOf(serverSettings.getProperty("EnableTranslitEngine", "False"));
			ENABLE_TEXT_FILTER	= Boolean.valueOf(serverSettings.getProperty("EnableTextFilter", "False"));
			RESTART_TIME = Integer.parseInt(serverSettings.getProperty("AutoRestart", "4"));
		}
		catch (Exception e)
		{
			e.printStackTrace();
			throw new Error("Failed to Load "+Server.fortressHomeDir+CONFIGURATION_FILE+" File.");
		}

		abuseLoad();

		/*
		 * Load L2J Version Properties file (if exists)
		 */
		try
		{
			Properties serverVersion	= new Properties();
			InputStream is = new FileInputStream(Server.fortressHomeDir+VERSION_FILE);
			serverVersion.load(is);
			is.close();

			SERVER_VERSION	  = serverVersion.getProperty("version", "Unsupported Custom Version.");
			SERVER_BUILD_DATE   = serverVersion.getProperty("builddate", "Undefined Date.");
		}
		catch (Exception e)
		{
			//Ignore Properties file if it doesnt exist
			SERVER_VERSION	  = "Unsupported Custom Version.";
			SERVER_BUILD_DATE   = "Undefined Date.";
		}

		// telnet
		try
		{
			Properties telnetSettings   = new Properties();
			InputStream is = new FileInputStream(Server.fortressHomeDir+TELNET_FILE);
			telnetSettings.load(is);
			is.close();

			IS_TELNET_ENABLED   = Boolean.valueOf(telnetSettings.getProperty("EnableTelnet", "false"));
		}
		catch (Exception e)
		{
			e.printStackTrace();
			throw new Error("Failed to Load "+Server.fortressHomeDir+TELNET_FILE+" File.");
		}

		// id factory
		try
		{
			Properties idSettings   = new Properties();
			InputStream is = new FileInputStream(Server.fortressHomeDir+ID_CONFIG_FILE);
			idSettings.load(is);
			is.close();

			IDFACTORY_TYPE  = IdFactoryType.valueOf(idSettings.getProperty("IDFactory", "Compaction"));
			BAD_ID_CHECKING = Boolean.valueOf(idSettings.getProperty("BadIdChecking", "True"));
		}
		catch (Exception e)
		{
			e.printStackTrace();
			throw new Error("Failed to Load "+Server.fortressHomeDir+ID_CONFIG_FILE+" File.");
		}

		// other
		try
		{
			Properties otherSettings	= new Properties();
			InputStream is = new FileInputStream(Server.fortressHomeDir+OTHER_CONFIG_FILE);
			otherSettings.load(is);
			is.close();

			DEEPBLUE_DROP_RULES = Boolean.parseBoolean(otherSettings.getProperty("UseDeepBlueDropRules", "True"));
			WYVERN_SPEED		= Integer.parseInt(otherSettings.getProperty("WyvernSpeed", "100"));
			STRIDER_SPEED		= Integer.parseInt(otherSettings.getProperty("StriderSpeed", "80"));

            /* Inventory slots limits */
            INVENTORY_MAXIMUM_NO_DWARF  = Integer.parseInt(otherSettings.getProperty("MaximumSlotsForNoDwarf", "80"));
            INVENTORY_MAXIMUM_DWARF  = Integer.parseInt(otherSettings.getProperty("MaximumSlotsForDwarf", "100"));
			INVENTORY_MAXIMUM_GM	= Integer.parseInt(otherSettings.getProperty("MaximumSlotsForGMPlayer", "250"));

			/* chance to enchant an item over +3 */
			ENCHANT_CHANCE  = Integer.parseInt(otherSettings.getProperty("EnchantChance", "65"));
			/* limit on enchant */
			ENCHANT_MAX = Integer.parseInt(otherSettings.getProperty("EnchantMax", "255"));

			/* if different from 100 (ie 100%) heal rate is modified acordingly */
			HP_REGEN_MULTIPLIER = Double.parseDouble(otherSettings.getProperty("HpRegenMultiplier", "100"));
			MP_REGEN_MULTIPLIER = Double.parseDouble(otherSettings.getProperty("MpRegenMultiplier", "100"));
			CP_REGEN_MULTIPLIER = Double.parseDouble(otherSettings.getProperty("CpRegenMultiplier", "100"));
			REGEN_SIT_WAIT = Boolean.valueOf(otherSettings.getProperty("RegenSitWait", "False"));

			STARTING_ADENA	  = Integer.parseInt(otherSettings.getProperty("StartingAdena", "100"));
			UNSTUCK_INTERVAL	= Integer.parseInt(otherSettings.getProperty("UnstuckInterval", "300"));

			/* Defines some Party XP related values */
			PARTY_XP_CUTOFF_METHOD  = otherSettings.getProperty("PartyXpCutoffMethod", "percentage");
			PARTY_XP_CUTOFF_PERCENT = Double.parseDouble(otherSettings.getProperty("PartyXpCutoffPercent", "3."));
			PARTY_XP_CUTOFF_LEVEL   = Integer.parseInt(otherSettings.getProperty("PartyXpCutoffLevel", "30"));

			/* Amount of HP, MP, and CP is restored */
			RESPAWN_RESTORE_CP = Double.parseDouble(otherSettings.getProperty("RespawnRestoreCP", "0")) / 100;
			RESPAWN_RESTORE_HP = Double.parseDouble(otherSettings.getProperty("RespawnRestoreHP", "70")) / 100;
			RESPAWN_RESTORE_MP = Double.parseDouble(otherSettings.getProperty("RespawnRestoreMP", "70")) / 100;

			/* Maximum number of available slots for pvt stores */
			MAX_PVTSTORE_SLOTS_DWARF = Integer.parseInt(otherSettings.getProperty("MaxPvtStoreSlotsDwarf", "5"));
			MAX_PVTSTORE_SLOTS_OTHER = Integer.parseInt(otherSettings.getProperty("MaxPvtStoreSlotsOther", "4"));

            SHOW_LICENCE = Boolean.parseBoolean(otherSettings.getProperty("ShowLicence", "true"));

		}
		catch (Exception e)
		{
			e.printStackTrace();
			throw new Error("Failed to Load "+Server.fortressHomeDir+OTHER_CONFIG_FILE+" File.");
		}

		// spoil
		try
		{
			Properties spoilSettings	= new Properties();
			InputStream is = new FileInputStream(Server.fortressHomeDir+SPOIL_CONFIG_FILE);
			spoilSettings.load(is);
			is.close();

			CAN_SPOIL_LOWER_LEVEL_MOBS			  = Boolean.parseBoolean(spoilSettings.getProperty("CanSpoilLowerLevelMobs", "false"));
			MAXIMUM_PLAYER_AND_MOB_LEVEL_DIFFERENCE = Float.parseFloat(spoilSettings.getProperty("MaximumPlayerAndMobLevelDifference", "9."));
			BASE_SPOIL_RATE = Float.parseFloat(spoilSettings.getProperty("BasePercentChanceOfSpoilSuccess", "40."));
			MINIMUM_SPOIL_RATE = Float.parseFloat(spoilSettings.getProperty("MinimumPercentChanceOfSpoilSuccess", "3."));
			SPOIL_LEVEL_DIFFERENCE_LIMIT = Float.parseFloat(spoilSettings.getProperty("SpoilLevelDifferenceLimit", "5."));
			SPOIL_LEVEL_DIFFERENCE_MULTIPLIER = Float.parseFloat(spoilSettings.getProperty("SpoilLevelMultiplier", "7."));
			LAST_LEVEL_SPOIL_IS_LEARNED = Integer.parseInt(spoilSettings.getProperty("LastLevelSpoilIsLearned", "72"));
		}
		catch (Exception e) {
			e.printStackTrace();
		throw new Error("Failed to Load "+Server.fortressHomeDir+SPOIL_CONFIG_FILE+" File.");
		}

		// alternative settings
		try
		{
            Properties altSettings   = new Properties();
            InputStream is = new FileInputStream(Server.fortressHomeDir+ALT_SETTINGS_FILE);
            altSettings.load(is);
            is.close();

            ALT_GAME_SKILL_LEARN		= Boolean.parseBoolean(altSettings.getProperty("AltGameSkillLearn", "false"));
            ALT_GAME_DELEVEL			= Boolean.parseBoolean(altSettings.getProperty("Delevel", "true"));
            ALT_GAME_SKILL_FORMULAS	  	= altSettings.getProperty("AltGameSkillFormulas", "none");
            ALLOW_CLASS_MASTERS		 	= altSettings.getProperty("AllowClassMasters", "1,2,3");
            if(ALLOW_CLASS_MASTERS.length()!=0)
                for (String id : ALLOW_CLASS_MASTERS.split(","))
                    ALLOW_CLASS_MASTERS_LIST.add(Integer.parseInt(id));
            ALT_GAME_FREIGHTS	   		= Boolean.parseBoolean(altSettings.getProperty("AltGameFreights", "false"));
            ALT_GAME_FREIGHT_PRICE  	= Integer.parseInt(altSettings.getProperty("AltGameFreightPrice", "1000"));
            ENABLE_RATE_HP		  	= Boolean.parseBoolean(altSettings.getProperty("EnableRateHp", "false"));
            WEAR_TEST_ENABLED	   = Boolean.valueOf(altSettings.getProperty("WearTestEnabled", "False"));
            AUTO_LOOT			   = altSettings.getProperty("AutoLoot").equalsIgnoreCase("True");
            ALT_GAME_KARMAPLAYERCANBEKILLEDINPEACEZONE = Boolean.valueOf(altSettings.getProperty("AltKarmaPlayerCanBeKilledInPeaceZone", "false"));
            ALT_GAME_KARMA_PLAYER_CAN_SHOP                      = Boolean.valueOf(altSettings.getProperty("AltKarmaPlayerCanShop", "true"));
            SAVING_SPS					=	Boolean.parseBoolean(altSettings.getProperty("SavingSpS", "false"));
            MANAHEAL_SPS_BONUS	=	Boolean.parseBoolean(altSettings.getProperty("ManahealSpSBonus", "false"));
            KILL_COUNTER	=	Boolean.parseBoolean(altSettings.getProperty("KillCounter", "false"));
            DROP_COUNTER	= Boolean.parseBoolean(altSettings.getProperty("DropCounter", "False"));
            CRAFT_COUNTER = Boolean.parseBoolean(altSettings.getProperty("CraftCounter", "true"));
            CORPSE_MOB_TIME = Integer.parseInt(altSettings.getProperty("CorpseMobTime","8500"));
            REBUILD_CAST_TARGETS_LIST = Boolean.parseBoolean(altSettings.getProperty("RebuildCastTargetsList", "false"));
            ALT_GAME_MATHERIALSDROP = Boolean.parseBoolean(altSettings.getProperty("AltMatherialsDrop", "false"));
            ALT_GAME_SHOW_DROPLIST = Boolean.parseBoolean(altSettings.getProperty("AltShowDroplist", "true"));
            ALT_MAX_RECIPES = Integer.parseInt(altSettings.getProperty("AltMaxRecipes","50"));
		}
		catch (Exception e)
		{
				e.printStackTrace();
			throw new Error("Failed to Load "+Server.fortressHomeDir+ALT_SETTINGS_FILE+" File.");
		}

		// pvp config
		try
		{
			Properties pvpSettings	= new Properties();
			InputStream is = new FileInputStream(Server.fortressHomeDir+PVP_CONFIG_FILE);
			pvpSettings.load(is);
			is.close();

			/* KARMA SYSTEM */
			KARMA_MIN_KARMA				= Integer.parseInt(pvpSettings.getProperty("MinKarma", "240"));
			KARMA_MAX_KARMA				= Integer.parseInt(pvpSettings.getProperty("MaxKarma", "10000"));
			KARMA_LOST_BASE				= Integer.parseInt(pvpSettings.getProperty("BaseKarmaLost", "240"));

			KARMA_DROP_EQUIPPED_ITEMS	= Boolean.parseBoolean(pvpSettings.getProperty("DroppableEquipment", "true"));
			KARMA_DROP_EQUIPPED_WEAPONS	= Boolean.parseBoolean(pvpSettings.getProperty("DroppableWeapon", "true"));

			KARMA_LIST_NONDROPPABLE_ITEMS = new ArrayList<Integer>();

			for (String id : KARMA_NONDROPPABLE_ITEMS.split(","))
				KARMA_LIST_NONDROPPABLE_ITEMS.add(Integer.parseInt(id));

			PVP_TIME = Integer.parseInt(pvpSettings.getProperty("PvPTime", "15000"));
		}
		catch (Exception e)
		{
			e.printStackTrace();
			throw new Error("Failed to Load "+Server.fortressHomeDir+PVP_CONFIG_FILE+" File.");
		}

		// access levels
		try
		{
			Properties gmSettings   = new Properties();
			InputStream is = new FileInputStream(Server.fortressHomeDir+GM_ACCESS_FILE);
			gmSettings.load(is);
			is.close();

			GM_ACCESSLEVEL	= Integer.parseInt(gmSettings.getProperty("GMAccessLevel", "100"));
			GM_MIN		  	= Integer.parseInt(gmSettings.getProperty("GMMinLevel", "100"));
			GM_ANNOUNCE	 	= Integer.parseInt(gmSettings.getProperty("GMCanAnnounce", "100"));
			GM_BAN		  	= Integer.parseInt(gmSettings.getProperty("GMCanBan", "100"));
			GM_BAN_CHAT	 	= Integer.parseInt(gmSettings.getProperty("GMCanBanChat", "100"));
			GM_CREATE_ITEM  = Integer.parseInt(gmSettings.getProperty("GMCanShop", "100"));
			GM_DELETE	   	= Integer.parseInt(gmSettings.getProperty("GMCanDelete", "100"));
			GM_KICK		 	= Integer.parseInt(gmSettings.getProperty("GMCanKick", "100"));
			GM_MENU		 	= Integer.parseInt(gmSettings.getProperty("GMMenu", "100"));
			GM_GODMODE	  	= Integer.parseInt(gmSettings.getProperty("GMGodMode", "100"));
			GM_CHAR_EDIT	= Integer.parseInt(gmSettings.getProperty("GMCanEditChar", "100"));
			GM_CHAR_VIEW	= Integer.parseInt(gmSettings.getProperty("GMCanViewChar", "100"));
			GM_NPC_EDIT	 	= Integer.parseInt(gmSettings.getProperty("GMCanEditNPC", "100"));
			GM_NPC_VIEW	 	= Integer.parseInt(gmSettings.getProperty("GMCanViewNPC", "100"));
			GM_TELEPORT	 	= Integer.parseInt(gmSettings.getProperty("GMCanTeleport", "100"));
			GM_RESTART	 	= Integer.parseInt(gmSettings.getProperty("GMCanRestart", "100"));
			GM_MONSTERRACE  = Integer.parseInt(gmSettings.getProperty("GMMonsterRace", "100"));
			GM_RIDER		= Integer.parseInt(gmSettings.getProperty("GMRider", "100"));
			GM_ESCAPE	   	= Integer.parseInt(gmSettings.getProperty("GMFastUnstuck", "100"));
			GM_FIXED		= Integer.parseInt(gmSettings.getProperty("GMResurectFixed", "100"));
			GM_USE_FORTH	= Integer.parseInt(gmSettings.getProperty("GMUseForth", "100"));
			GM_CREATE_NODES = Integer.parseInt(gmSettings.getProperty("GMCreateNodes", "100"));
			GM_DOOR		 	= Integer.parseInt(gmSettings.getProperty("GMDoor", "100"));
			GM_RES		  	= Integer.parseInt(gmSettings.getProperty("GMRes", "100"));
			GM_PEACEATTACK  = Integer.parseInt(gmSettings.getProperty("GMPeaceAttack", "100"));
			GM_HEAL		 	= Integer.parseInt(gmSettings.getProperty("GMHeal", "100"));
			GM_UNBLOCK	  	= Integer.parseInt(gmSettings.getProperty("GMUnblock", "100"));
		}
		catch (Exception e)
		{
			e.printStackTrace();
			throw new Error("Failed to Load "+Server.fortressHomeDir+GM_ACCESS_FILE+" File.");
		}
	}

	public static boolean allowL2Walker(L2PcInstance player)
	{
		return (ALLOW_L2WALKER_CLIENT == L2WalkerAllowed.True ||
				(ALLOW_L2WALKER_CLIENT == L2WalkerAllowed.GM && player != null && player.isGM()));
	}

// 	it has no instancies
	private Config() {}

	private static Config _instance = new Config();

	public static final Config getInstance()
	{
		return _instance;
	}

	public final static void abuseLoad()
	{
		MAT_LIST = new ArrayList<String>();

		LineNumberReader lnr = null;
		try
        {
            int i=0;
            String line = null;

			lnr = new LineNumberReader(new InputStreamReader(new FileInputStream(Server.fortressHomeDir+MAT_CONFIG_FILE),"UTF-8"));

            while ( (line = lnr.readLine()) != null)
            {
				StringTokenizer st = new StringTokenizer(line,"\n\r");
                if (st.hasMoreTokens())
                {
                    String Mat = st.nextToken();
                    MAT_LIST.add(Mat);

                    i++;
                }
            }

            _log.info("Abuse: Loaded " + i + " abuse words.");

        }
        catch (IOException e1)
        {
            _log.warning("Error reading abuse: "+ e1);
        }
        finally
        {
            try
            {
                lnr.close();
            }
            catch (Exception e2)
            {
                // nothing
            }
        }
	}
}
