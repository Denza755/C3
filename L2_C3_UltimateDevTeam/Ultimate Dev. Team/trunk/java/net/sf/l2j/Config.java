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

import javolution.util.FastList;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;

import java.io.*;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.logging.Logger;

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
	/** Debug/release mode */
    public static boolean DEBUG;
    /** Enable/disable assertions */
    public static boolean ASSERT;
    /** Enable/disable code 'in progress' */
    public static boolean DEVELOPER;
    
    public static boolean TEST_SERVER;

    /** Game/Login Server ports */
    public static int PORT_GAME;
    public static int PORT_LOGIN;
    public static int LOGIN_TRY_BEFORE_BAN;
    public static String GAMESERVER_HOSTNAME;
    
    public static String DATABASE_DRIVER;
    public static String DATABASE_URL;
    public static String DATABASE_LOGIN;
    public static String DATABASE_PASSWORD;
    public static int DATABASE_MAX_CONNECTIONS;
    
    public static int   MAXIMUM_ONLINE_USERS;
    
    public static boolean SERVER_LIST_BRACKET;
    public static boolean SERVER_LIST_CLOCK;
    public static boolean SERVER_LIST_TESTSERVER;
    public static boolean SERVER_GMONLY;
    
    /** Thread pools size */
    public static int THREAD_P_HIGH;
    public static int THREAD_P_MED;
    public static int THREAD_P_LOW;
    
    /** Auto-loot? */
    public static boolean AUTO_LOOT;

    /** Character name template */
    public static String CNAME_TEMPLATE;

    /** Global chat state */
    public static String  DEFAULT_GLOBAL_CHAT;
    /** For test servers - evrybody has admin rights */
    public static boolean EVERYBODY_HAS_ADMIN_RIGHTS;
    /** Alternative game crafting */
    public static boolean ALT_GAME_CREATION;
    /** Alternative game skill learning */
    public static boolean ALT_GAME_SKILL_LEARN;
    /** Cancel ATTACK/cast by hit */
    public static boolean ALT_GAME_CANCEL_BOW;
    public static boolean ALT_GAME_CANCEL_CAST;

    /** Alternative game - use tiredness, instead of CP */
    public static boolean ALT_GAME_TIREDNESS;

    /** Alternative shield defence */
    public static boolean ALT_GAME_SHIELD_BLOCKS;

    /** Alternative game mob ATTACK AI */
    public static boolean ALT_GAME_MOB_ATTACK_AI;
    
    /** Alternative success rate formulas for skills such root/sleep/stun */
    public static String ALT_GAME_SKILL_FORMULAS;
    
    /** Alternative freight modes - Freights can be withdrawed from any village */
    public static boolean ALT_GAME_FREIGHTS;
    /** Alternative freight modes - Sets the price value for each freightened item */
    public static int ALT_GAME_FREIGHT_PRICE;

    /** Fast or slow multiply coefficient for skill hit time */
    public static float ALT_GAME_SKILL_HIT_RATE;

    /** Alternative gameing - loss of XP on death */
    public static boolean ALT_GAME_DELEVEL;

    /** Alternative gameing - magic dmg failures */
    public static boolean ALT_GAME_MAGICFAILURES;

    /** Alternative gaming - player must be in a castle-owning clan or ally to sign up for Dawn. */
    public static boolean ALT_GAME_REQUIRE_CASTLE_DAWN;
    
    /** Alternative gaming - allow clan-based castle ownage check rather than ally-based. */
    public static boolean ALT_GAME_REQUIRE_CLAN_CASTLE;
    
    /** Alternative gaming - allow free teleporting around the world. */
    public static boolean ALT_GAME_FREE_TELEPORT;
    
    /** Alternative gaming - allow sub-class addition without quest completion. */
    public static boolean ALT_GAME_SUBCLASS_WITHOUT_QUESTS;
    
    /** View npc stats/drop by shift-cliking it for nongm-players */
    public static boolean ALT_GAME_VIEWNPC;
    
    /** Enable Rate Hp  */
    public static boolean ENABLE_RATE_HP;

    /** Spell Book needed to learn skill */
    public static boolean SP_BOOK_NEEDED;
    /** Logging Chat Window */
    public static boolean LOG_CHAT;
    public static boolean LOG_ITEMS;
    
    public static boolean ALT_PRIVILEGES_ADMIN;
    public static boolean ALT_PRIVILEGES_SECURE_CHECK;
    public static int ALT_PRIVILEGES_DEFAULT_LEVEL;
    
	public static boolean PVP_COLOR_SYSTEM_ENABLED; /** Colored pvp name system */
	public static int     PVP_AMOUNT1; /** Colored pvp name system */
	public static int     PVP_AMOUNT2; /** Colored pvp name system */
	public static int     PVP_AMOUNT3; /** Colored pvp name system */
	public static int     PVP_AMOUNT4; /** Colored pvp name system */
	public static int     PVP_AMOUNT5; /** Colored pvp name system */
	public static int     NAME_COLOR_FOR_PVP_AMOUNT1; /** Colored pvp name system */
	public static int     NAME_COLOR_FOR_PVP_AMOUNT2; /** Colored pvp name system */
	public static int     NAME_COLOR_FOR_PVP_AMOUNT3; /** Colored pvp name system */
	public static int     NAME_COLOR_FOR_PVP_AMOUNT4; /** Colored pvp name system */
	public static int     NAME_COLOR_FOR_PVP_AMOUNT5; /** Colored pvp name system */
	public static boolean PK_COLOR_SYSTEM_ENABLED; /** Colored pk name system */
	public static int     PK_AMOUNT1; /** Colored pk name system */
	public static int     PK_AMOUNT2; /** Colored pk name system */
	public static int     PK_AMOUNT3; /** Colored pk name system */
	public static int     PK_AMOUNT4; /** Colored pk name system */
	public static int     PK_AMOUNT5; /** Colored pk name system */
	public static int     TITLE_COLOR_FOR_PK_AMOUNT1; /** Colored pk name system */
	public static int     TITLE_COLOR_FOR_PK_AMOUNT2; /** Colored pk name system */
	public static int     TITLE_COLOR_FOR_PK_AMOUNT3; /** Colored pk name system */
	public static int     TITLE_COLOR_FOR_PK_AMOUNT4; /** Colored pk name system */
	public static int     TITLE_COLOR_FOR_PK_AMOUNT5; /** Colored pk name system */
    /***************************************************************************
     * GM CONFIG General GM AccessLevel *
     **************************************************************************/
    public static int     GM_ACCESSLEVEL;
    /** General GM Minimal AccessLevel */
    public static int     GM_MIN;
    /** General GM AccessLevel to change config of server*/
    public static int     GM_CAN_CONFIG;
    /** General GM AccessLevel to change announcements */
    public static int     GM_ANNOUNCE;
    /** General GM AccessLevel can /ban /unban */
    public static int     GM_BAN;
    public static int     GM_BAN_CHAT;
    /** General GM AccessLevel can /create_item and /gmshop */
    public static int     GM_CREATE_ITEM;
    /** General GM AccessLevel can /delete */
    public static int     GM_DELETE;
    /** General GM AccessLevel can /kick /disconnect */
    public static int     GM_KICK;
    /** General GM AccessLevel for access to GMMenu */
    public static int     GM_MENU;
    /** General GM AccessLevel tu use god mode command */
    public static int     GM_GODMODE;
    /** General GM AccessLevel with character edit rights */
    public static int     GM_CHAR_EDIT;
    public static int     GM_CHAR_EDIT_OTHER;
    public static int     GM_CHAR_VIEW;
    /** General GM AccessLevel with NPC edit rights */
    public static int     GM_NPC_EDIT;
    public static int     GM_NPC_VIEW;
    /** General GM AccessLevel to teleport to any location */
    public static int     GM_TELEPORT;
    public static int     GM_TELEPORT_OTHER;
    /** General GM AccessLevel to restart server */
    public static int     GM_RESTART;
    /** General GM AccessLevel for MonsterRace */
    public static int     GM_MONSTERRACE;
    /** General GM AccessLevel to ride Wyvern */
    public static int     GM_RIDER;
    /** General GM AccessLevel to unstuck without 5min delay */
    public static int     GM_ESCAPE;
    /** General GM AccessLevel to resurect fixed after death */
    public static int     GM_FIXED;
    /** General GM AccessLevel to create Path Nodes */
    public static int     GM_CREATE_NODES;
    /** General GM AccessLevel with Enchant rights */
    public static int     GM_ENCHANT;
    /** General GM AccessLevel to close/open Doors */
    public static int     GM_DOOR;
    /** General GM AccessLevel with Resurrection rights */
    public static int     GM_RES;
    /** General GM AccessLevel to attack in the peace zone */
    public static int     GM_PEACEATTACK;   
    /** General GM AccessLevel to heal */
    public static int     GM_HEAL;
    /** General GM AccessLevel to unblock IPs detected as hack IPs */
    public static int     GM_UNBLOCK;
    /** General GM AccessLevel to use Cache commands */
    public static int GM_CACHE;
    /** General GM AccessLevel to use test&st commands */
    public static int GM_TALK_BLOCK;
    public static int GM_TEST;
    /** Disable transaction on AccessLevel **/
    public static boolean GM_DISABLE_TRANSACTION;
    public static int GM_TRANSACTION_MIN;
    public static int GM_TRANSACTION_MAX;
    
    /** Show mob stats/droplist to players ? */
    public static boolean SHOW_DROPLIST;
    /** Config for announcements when player enter game */
    public static boolean SHOW_L2J_LICENSE;
    public static boolean SHOW_HTML_WELCOME;
    public static boolean SHOW_SS_PERIOD;
    public static boolean SHOW_GM_LOGIN;
    public static boolean HIDE_GM_STATUS;
    /** Alternative buff time */
    public static int ALT_BUFF_TIME;
    /** Alternative Charge usage **/
    public static boolean ALT_CHARGE;
    /** Config for Auto Learn Skills */
    public static boolean AUTO_LEARN_SKILLS;
    /** Config for limit attack speed */
    public static int MAX_PATK_SPEED;
    public static int MAX_MATK_SPEED;
    /** Config for clan penalty */
    public static boolean CLAN_PENALTY;
    /** Config for seven signs */
    public static boolean ALLOW_SS_TELEPORT;
    public static boolean ALLOW_SS_REGIST;
    public static int MIN_SS_PARTY;
    public static int VALUE_AA;
    /** Config for GM/VIP Name Color */
    public static String GM_NAME_COLOR;
    public static String VIP_NAME_COLOR;
    public static String NORMAL_NAME_COLOR;
    public static int VIP_ACCESSLEVEL;
    /** Alternative Pet Hunger **/
    public static boolean PET_HUNGER;
    /** Alternative DOT effect **/
    public static boolean DOT_KILL;
    /** Alternative Npc kill **/
    public static boolean ALLOW_KILL_ALL_NPC;
    /** Alternative Recommandation **/
    public static boolean RESTART_RECOM;
    /** Config for language display **/
    public static String MAIN_LANG;
    /** Config for Grade Penalty **/
    public static boolean GRADE_PENALTY;
    /** Config for Deep Blue mob exp/sp penalty **/
    public static boolean DEEPBLUE_EXP_RULES;
    /** Config for max level **/
    public static int MAX_LEVEL = 80;
    /** Config for maximum items buy**/
    public static int MAX_ITEM_BUY;
    /** Config for spawn siege guard**/
    public static boolean SPAWN_SIEGE_GUARD;
    /** Config for use chat filter **/
    public static boolean USE_SAY_FILTER;
    public static ArrayList<String> FILTER_LIST = new ArrayList<String>();

    /** Rate control */
    public static float   RATE_XP;
    public static float   RATE_SP;
    public static float   RATE_PARTY_XP;
    public static float   RATE_PARTY_SP;
    public static float   RATE_QUESTS_REWARD;
    public static float   RATE_DROP_ADENA;
    public static float   RATE_CONSUMABLE_COST;
    public static float   RATE_DROP_ITEMS;
    public static float   RATE_DROP_SPOIL;
    public static float   RATE_KARMA_EXP_LOST;	
    public static float   RATE_SIEGE_GUARDS_PRICE;	
    public static float   ALT_GAME_EXPONENT_XP;
    public static float   ALT_GAME_EXPONENT_SP;
    public static int     RATE_PET_EXP;
    public static int     RATE_PET_SP;

    /** Player Drop Rate control */
    public static int   PLAYER_DROP_LIMIT;
    public static int   PLAYER_RATE_DROP;
    public static int   PLAYER_RATE_DROP_ITEM;
    public static int   PLAYER_RATE_DROP_EQUIP;    
    public static int   PLAYER_RATE_DROP_EQUIP_WEAPON;    

    /** Karma Drop Rate control */
    public static int   KARMA_DROP_LIMIT;
    public static int   KARMA_RATE_DROP;
    public static int   KARMA_RATE_DROP_ITEM;
    public static int   KARMA_RATE_DROP_EQUIP;    
    public static int   KARMA_RATE_DROP_EQUIP_WEAPON;    
    
    public static int     AUTODESTROY_ITEM_AFTER;

    public static int     COORD_SYNCHRONIZE;
    
    public static int     DELETE_DAYS;
    

    /** Datapack root directory */
    public static File    DATAPACK_ROOT;

    /** Maximum range mobs can randomly go from spawn point */
    public static int MAX_DRIFT_RANGE;


    // Allow L2Walker client
    public static enum L2WalkerAllowed
    {
        True,
        False,
        GM
    }

    public static L2WalkerAllowed ALLOW_L2WALKER_CLIENT;
    public static boolean         AUTOBAN_L2WALKER_ACC;
    public static int             L2WALKER_REVISION;

    public static boolean         ALLOW_DISCARDITEM;
    public static boolean         ALLOW_FREIGHT;
    public static boolean         ALLOW_WAREHOUSE;
    public static boolean 	  ALLOW_WEAR;
    public static boolean 	  ALLOW_LOTTERY;
    public static boolean 	  ALLOW_RACE;
    public static boolean 	  ALLOW_WATER;
    public static boolean     ALLOW_RENTPET;
    public static boolean 	  ALLOW_BOAT;
    
    public static int PACKET_LIFETIME;

    // Pets
    public static int WYVERN_SPEED;
    public static int STRIDER_SPEED;
    public static boolean ALLOW_WYVERN_UPGRADER;

    // protocol revision
    public static int MIN_PROTOCOL_REVISION;
    public static int MAX_PROTOCOL_REVISION;

    // random animation interval
    public static int MIN_NPC_ANIMATION;
    public static int MAX_NPC_ANIMATION;

    public static boolean ACTIVATE_POSITION_RECORDER;
    public static boolean USE_3D_MAP;

    // Community Board
    public static boolean SHOW_LEVEL_COMMUNITYBOARD;
    public static boolean SHOW_STATUS_COMMUNITYBOARD;
    public static int     NAME_PAGE_SIZE_COMMUNITYBOARD;
    public static int     NAME_PER_ROW_COMMUNITYBOARD;

    /** Configuration files */
    public static final String  MOD		          			= "./config/mod.properties";
    public static final String  ID_CONFIG_FILE				= "./config/idfactory.properties";
    public static final String  OTHER_CONFIG_FILE			= "./config/other.properties";
    public static final String  SPOIL_CONFIG_FILE			= "./config/spoil.properties";
    public static final String  ALT_SETTINGS_FILE			= "./config/altsettings.properties";
    public static final String  PVP_CONFIG_FILE				= "./config/pvp.properties";
    public static final String  GM_ACCESS_FILE				= "./config/GMAccess.properties";
    public static final String  TELNET_FILE					= "./config/telnet.properties";
    public static final String  CONFIGURATION_FILE			= "./config/server.properties";
    public static final String	LOGIN_CONFIGURATION_FILE	= "./config/loginserver.properties";
    public static final String  VERSION_FILE				= "./config/l2j-version.properties";
    public static final String  SIEGE_CONFIGURATION_FILE	= "./config/siege.properties";
    public static final String  BANNED_IP_XML				= "./config/banned.xml";
    public static final String  HEXID_FILE					= "./config/hexid.txt";
    public static final String  COMMAND_PRIVILEGES_FILE     = "./config/command-privileges.properties";
    public static final String  AI_FILE     				= "./config/ai.properties";
    public static final String  CUSTOM_CONFIG_FILE     = "./config/custom.properties";
    public static final String	SAY_FILTER_FILE	= "/config/sayfilter.txt";    
    public static boolean CHECK_KNOWN;
    
    public static int     GAME_SERVER_LOGIN_PORT;
    public static String     GAME_SERVER_LOGIN_HOST;
    public static String  INTERNAL_HOSTNAME;
    public static String  EXTERNAL_HOSTNAME;
    public static int     PATH_NODE_RADIUS;
    public static int           NEW_NODE_ID;
    public static int           SELECTED_NODE_ID;
    public static int           LINKED_NODE_ID;
    public static String        NEW_NODE_TYPE;
    public static boolean SERVER_SIDE_NPC_NAME;
    public static boolean SERVER_SIDE_NPC_NAME_WITH_LVL;
    public static boolean ALLOW_GUARDS;
    public static String	ALLOW_CLASS_MASTERS;
    public static ArrayList<Integer> ALLOW_CLASS_MASTERS_LIST = new ArrayList<Integer>();
    
    public static String    SERVER_VERSION;
    public static String    SERVER_BUILD_DATE;
    
    /** Zone Setting */
    public static int ZONE_TOWN;
    
    /** Crafting Enabled? */
    public static boolean IS_CRAFTING_ENABLED;
    
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
    public static boolean CAN_SPOIL_LOWER_LEVEL_MOBS;
    public static boolean CAN_DELEVEL_AND_SPOIL_MOBS;
    public static float   MAXIMUM_PLAYER_AND_MOB_LEVEL_DIFFERENCE;
    public static float   BASE_SPOIL_RATE;
    public static float   MINIMUM_SPOIL_RATE;
    public static float   SPOIL_LEVEL_DIFFERENCE_LIMIT;
    public static float   SPOIL_LEVEL_DIFFERENCE_MULTIPLIER;
    public static int     LAST_LEVEL_SPOIL_IS_LEARNED;
    
    /** Karma System Variables */
    public static int     KARMA_MIN_KARMA;
    public static int     KARMA_MAX_KARMA;
    public static int     KARMA_XP_DIVIDER;
    public static int     KARMA_LOST_BASE;
    public static boolean KARMA_DROP_GM;
    public static boolean KARMA_AWARD_PK_KILL;
    public static int     KARMA_PK_LIMIT;
    
    public static String  KARMA_NONDROPPABLE_PET_ITEMS;
    public static String  KARMA_NONDROPPABLE_ITEMS;
    public static List<Integer> KARMA_LIST_NONDROPPABLE_PET_ITEMS   = new FastList<Integer>();
    public static List<Integer> KARMA_LIST_NONDROPPABLE_ITEMS       = new FastList<Integer>();

    public static String  NONDROPPABLE_ITEMS;
    public static List<Integer> LIST_NONDROPPABLE_ITEMS       = new FastList<Integer>();

    public static String  PET_RENT_NPC;
    public static List<Integer> LIST_PET_RENT_NPC   = new FastList<Integer>();
    
    public static int PVP_TIME;    

    /** Karma Punishment */
    public static boolean ALT_GAME_KARMA_PLAYER_CAN_BE_KILLED_IN_PEACEZONE;
    public static boolean ALT_GAME_KARMA_PLAYER_CAN_SHOP;
    public static boolean ALT_GAME_KARMA_PLAYER_CAN_TELEPORT;
    public static boolean ALT_GAME_KARMA_PLAYER_CAN_TRADE;
    public static boolean ALT_GAME_KARMA_PLAYER_CAN_USE_WAREHOUSE;
    public static boolean ALT_GAME_KARMA_PLAYER_CAN_USE_GATEKEEPER;

    /** Day/Night Status **/
    public static boolean DAY_STATUS_FORCE_CLIENT_UPDATE;
    public static int     DAY_STATUS_SUN_RISE_AT;
    public static int     DAY_STATUS_SUN_SET_AT;

    /**  
	 * Counting of amount of packets per minute  
	 */  
	public static boolean  COUNT_PACKETS           = false;
	public static boolean  DUMP_PACKET_COUNTS      = false;
    public static int      DUMP_INTERVAL_SECONDS   = 60;
    
    public static int      EXECUTOR_PRI_LOW;
    public static int      EXECUTOR_PRI_MED;
    public static int      EXECUTOR_PRI_HIGH;

    
	public static enum IdFactoryType
    {
	    Compaction,
        BitSet,
        Stack
    }
    
    public static IdFactoryType IDFACTORY_TYPE;
    public static boolean BAD_ID_CHECKING;
	
    public static enum ObjectMapType
    {
        L2ObjectHashMap,
        WorldObjectMap
    }

    public static enum ObjectSetType
    {
        L2ObjectHashSet,
        WorldObjectSet
    }

    public static ObjectMapType   MAP_TYPE;
    public static ObjectSetType   SET_TYPE;
    
    public static boolean EFFECT_CANCELING;

    public static boolean AUTODELETE_INVALID_QUEST_DATA;
    
    /** Chance that an item will succesfully be enchanted */
    public static int ENCHANT_CHANCE;
    public static int ENCHANT_MAX;
    
    public static double  HP_REGEN_MULTIPLIER;
    public static double  MP_REGEN_MULTIPLIER;
    public static double  CP_REGEN_MULTIPLIER;
    
    /** Raid Boss Multipliers */
    public static double   RAID_HP_REGEN_MULTIPLIER;   
    public static double   RAID_MP_REGEN_MULTIPLIER;   
    public static double   RAID_DEFENCE_MULTIPLIER;
    
    public static int STARTING_ADENA;
    
    /** Deep Blue Mobs' Drop Rules Enabled */
    public static boolean DEEPBLUE_DROP_RULES;
    public static int     UNSTUCK_INTERVAL;
    
    // telnet enabled
    public static boolean IS_TELNET_ENABLED;
    
    /** Player Protection control */
    public static int   PLAYER_SPAWN_PROTECTION;

    /** Define Party XP cutoff point method - Possible values: level and percentage */
    public static String  PARTY_XP_CUTOFF_METHOD;
    /** Define the cutoff point value for the "level" method */
    public static int PARTY_XP_CUTOFF_LEVEL;
    /** Define the cutoff point value for the "percentage" method */
    public static double  PARTY_XP_CUTOFF_PERCENT;

    /** Percent CP is restore on respawn */
    public static double  RESPAWN_RESTORE_CP;
    /** Percent HP is restore on respawn */
    public static double  RESPAWN_RESTORE_HP;
    /** Percent MP is restore on respawn */
    public static double  RESPAWN_RESTORE_MP;
    /** Allow randomizing of the respawn point in towns. */
    public static boolean RESPAWN_RANDOM_ENABLED;
    /** The maximum offset from the base respawn point to allow. */
    public static int RESPAWN_RANDOM_MAX_OFFSET;
    
    /** Maximum number of available slots for pvt stores (sell/buy) - Dwarves */
    public static int  MAX_PVTSTORE_SLOTS_DWARF;
    /** Maximum number of available slots for pvt stores (sell/buy) - Others */
    public static int  MAX_PVTSTORE_SLOTS_OTHER;
    
    /** Store skills cooltime on char exit/relogin */
    public static boolean STORE_SKILL_COOLTIME;
    /** Show licence or not just after login (if false, will directly go to the Server List */
	public static boolean SHOW_LICENCE;
	
    /** Deafult punishment for illegal actions */
    public static int DEFAULT_PUNISH;
    
	public static boolean ACCEPT_NEW_GAMESERVER;
	public static byte[] HEX_ID;
	public static boolean ACCEPT_ALTERNATE_ID;
	public static int REQUEST_ID;
	public static boolean RESERVE_HOST_ON_LOGIN = false;
    
    public static int MINIMUM_UPDATE_DISTANCE;
    public static int KNOWNLIST_FORGET_DELAY;
    public static int MINIMUN_UPDATE_TIME;
    
    public static boolean ANNOUNCE_MAMMON_SPAWN;
    public static boolean LAZY_CACHE;
    
    public static boolean GM_NAME_COLOUR_ENABLED;
    public static int GM_NAME_COLOUR;
    
    /** Alternative AI setting*/
    public static boolean	AI_ENABLED;
    public static String	AI_DEFAULT_CLASS;
    
    public static boolean	BYPASS_VALIDATION;

	public static void load()
	{
		if(Server.SERVER_MODE == Server.MODE_GAMESERVER)
		{
			_log.info("loading gameserver config");
		    try {
		        Properties serverSettings    = new Properties();
				InputStream is               = new FileInputStream(new File(CONFIGURATION_FILE));
				serverSettings.load(is);
				is.close();
				
				GAME_SERVER_LOGIN_HOST = serverSettings.getProperty("LoginHost","127.0.0.1");
				GAME_SERVER_LOGIN_PORT = Integer.parseInt(serverSettings.getProperty("LoginPort","9013"));
				DEBUG        = Boolean.parseBoolean(serverSettings.getProperty("Debug", "false"));
				DEVELOPER    = Boolean.parseBoolean(serverSettings.getProperty("Developer", "false"));
				ASSERT       = Boolean.parseBoolean(serverSettings.getProperty("Assert", "false"));
				TEST_SERVER  =  Boolean.parseBoolean(serverSettings.getProperty("TestServer", "false"));
				SERVER_LIST_TESTSERVER = Boolean.parseBoolean(serverSettings.getProperty("TestServer", "false"));
				
				ACCEPT_NEW_GAMESERVER = Boolean.parseBoolean(serverSettings.getProperty("AcceptNewGameServer","True"));
				REQUEST_ID = Integer.parseInt(serverSettings.getProperty("RequestServerID","0"));
				ACCEPT_ALTERNATE_ID = Boolean.parseBoolean(serverSettings.getProperty("AcceptAlternateID","True"));
				
	            PORT_GAME               = Integer.parseInt(serverSettings.getProperty("GameserverPort", "7777"));
	            PORT_LOGIN              = Integer.parseInt(serverSettings.getProperty("LoginserverPort", "2106"));
	            CNAME_TEMPLATE 		    = serverSettings.getProperty("CnameTemplate", ".*");
	            LOGIN_TRY_BEFORE_BAN    = Integer.parseInt(serverSettings.getProperty("LoginTryBeforeBan", "10"));
	            GAMESERVER_HOSTNAME     = serverSettings.getProperty("GameserverHostname");
	            
				DEFAULT_GLOBAL_CHAT          = serverSettings.getProperty("GlobalChat", "ON");						
				EVERYBODY_HAS_ADMIN_RIGHTS   = Boolean.parseBoolean(serverSettings.getProperty("EverybodyHasAdminRights", "false"));
				LOG_CHAT                     = Boolean.valueOf(serverSettings.getProperty("LogChat", "false"));
				LOG_ITEMS                    = Boolean.valueOf(serverSettings.getProperty("LogItems", "false"));
	            
				RATE_XP              = Float.parseFloat(serverSettings.getProperty("RateXp", "1."));
				RATE_SP              = Float.parseFloat(serverSettings.getProperty("RateSp", "1."));
                RATE_PARTY_XP        = Float.parseFloat(serverSettings.getProperty("RatePartyXp", "1."));
                RATE_PARTY_SP        = Float.parseFloat(serverSettings.getProperty("RatePartySp", "1."));
				RATE_QUESTS_REWARD   = Float.parseFloat(serverSettings.getProperty("RateQuestsReward", "1."));
				RATE_DROP_ADENA      = Float.parseFloat(serverSettings.getProperty("RateDropAdena", "1."));
                RATE_CONSUMABLE_COST = Float.parseFloat(serverSettings.getProperty("RateConsumableCost", "1."));
				RATE_DROP_ITEMS      = Float.parseFloat(serverSettings.getProperty("RateDropItems", "1."));
				RATE_DROP_SPOIL      = Float.parseFloat(serverSettings.getProperty("RateDropSpoil", "1."));
				RATE_KARMA_EXP_LOST  = Float.parseFloat(serverSettings.getProperty("RateKarmaExpLost", "1."));    
				RATE_PET_EXP         = Integer.parseInt(serverSettings.getProperty("RatePetExp", "1"));
				RATE_PET_SP          = Integer.parseInt(serverSettings.getProperty("RatePetSp", "1"));
				RATE_SIEGE_GUARDS_PRICE  = Float.parseFloat(serverSettings.getProperty("RateSiegeGuardsPrice", "1."));    

                PLAYER_DROP_LIMIT               = Integer.parseInt(serverSettings.getProperty("PlayerDropLimit", "3"));
	            PLAYER_RATE_DROP                = Integer.parseInt(serverSettings.getProperty("PlayerRateDrop", "5"));
	            PLAYER_RATE_DROP_ITEM           = Integer.parseInt(serverSettings.getProperty("PlayerRateDropItem", "70"));
	            PLAYER_RATE_DROP_EQUIP          = Integer.parseInt(serverSettings.getProperty("PlayerRateDropEquip", "25"));
	            PLAYER_RATE_DROP_EQUIP_WEAPON   = Integer.parseInt(serverSettings.getProperty("PlayerRateDropEquipWeapon", "5"));
                
                KARMA_DROP_LIMIT               = Integer.parseInt(serverSettings.getProperty("KarmaDropLimit", "10"));
                KARMA_RATE_DROP                = Integer.parseInt(serverSettings.getProperty("KarmaRateDrop", "70"));
                KARMA_RATE_DROP_ITEM           = Integer.parseInt(serverSettings.getProperty("KarmaRateDropItem", "50"));
                KARMA_RATE_DROP_EQUIP          = Integer.parseInt(serverSettings.getProperty("KarmaRateDropEquip", "40"));
                KARMA_RATE_DROP_EQUIP_WEAPON   = Integer.parseInt(serverSettings.getProperty("KarmaRateDropEquipWeapon", "10"));
	
	            AUTODESTROY_ITEM_AFTER = Integer.parseInt(serverSettings.getProperty("AutoDestroyDroppedItemAfter", "0"));
                COORD_SYNCHRONIZE = Integer.parseInt(serverSettings.getProperty("CoordSynchronize", "-1"));
				DELETE_DAYS = Integer.parseInt(serverSettings.getProperty("DeleteCharAfterDays", "7"));
				
				DATAPACK_ROOT    = new File(serverSettings.getProperty("DatapackRoot", ".")).getCanonicalFile();
	
	            MAX_DRIFT_RANGE = Integer.parseInt(serverSettings.getProperty("MaxDriftRange", "300"));
	            
	            ALLOW_L2WALKER_CLIENT   = L2WalkerAllowed.valueOf(serverSettings.getProperty("AllowL2Walker", "False"));
	            AUTOBAN_L2WALKER_ACC    = Boolean.valueOf(serverSettings.getProperty("AutobanL2WalkerAcc", "False"));
	            L2WALKER_REVISION       = Integer.parseInt(serverSettings.getProperty("L2WalkerRevision", "537"));
				
	            ALLOW_DISCARDITEM       = Boolean.valueOf(serverSettings.getProperty("AllowDiscardItem", "True"));
	            ALLOW_FREIGHT           = Boolean.valueOf(serverSettings.getProperty("AllowFreight", "True"));
	            ALLOW_WAREHOUSE         = Boolean.valueOf(serverSettings.getProperty("AllowWarehouse", "True"));
	            ALLOW_WEAR              = Boolean.valueOf(serverSettings.getProperty("AllowWear", "False"));
	            ALLOW_LOTTERY           = Boolean.valueOf(serverSettings.getProperty("AllowLottery", "False"));
        	    ALLOW_RACE              = Boolean.valueOf(serverSettings.getProperty("AllowRace", "False"));
	            ALLOW_WATER              = Boolean.valueOf(serverSettings.getProperty("AllowWater", "False"));
	            ALLOW_BOAT        = Boolean.valueOf(serverSettings.getProperty("AllowBoat", "False"));
            	    ALLOW_RENTPET              = Boolean.valueOf(serverSettings.getProperty("AllowRentPet", "False"));
            

            	    PACKET_LIFETIME            = Integer.parseInt(serverSettings.getProperty("PacketLifeTime", "0"));
	           
	            MIN_PROTOCOL_REVISION   = Integer.parseInt(serverSettings.getProperty("MinProtocolRevision", "557"));
	            MAX_PROTOCOL_REVISION   = Integer.parseInt(serverSettings.getProperty("MaxProtocolRevision", "999"));
	            
	            if (MIN_PROTOCOL_REVISION > MAX_PROTOCOL_REVISION)
	            {
	            	throw new Error("MinProtocolRevision is bigger than MaxProtocolRevision in server configuration file.");
	            }
	            
	            ACTIVATE_POSITION_RECORDER  = Boolean.valueOf(serverSettings.getProperty("ActivatePositionRecorder", "False"));
	            USE_3D_MAP                  = Boolean.valueOf(serverSettings.getProperty("Use3DMap", "False"));
	            MIN_NPC_ANIMATION           = Integer.parseInt(serverSettings.getProperty("MinNPCAnimation", "0"));
	            MAX_NPC_ANIMATION           = Integer.parseInt(serverSettings.getProperty("MaxNPCAnimation", "0"));
	
                SHOW_LEVEL_COMMUNITYBOARD       = Boolean.valueOf(serverSettings.getProperty("ShowLevelOnCommunityBoard", "False"));
                SHOW_STATUS_COMMUNITYBOARD      = Boolean.valueOf(serverSettings.getProperty("ShowStatusOnCommunityBoard", "True"));
                NAME_PAGE_SIZE_COMMUNITYBOARD   = Integer.parseInt(serverSettings.getProperty("NamePageSizeOnCommunityBoard", "50"));
                NAME_PER_ROW_COMMUNITYBOARD     = Integer.parseInt(serverSettings.getProperty("NamePerRowOnCommunityBoard", "5"));

                INTERNAL_HOSTNAME   = serverSettings.getProperty("InternalHostname", "*");
	            EXTERNAL_HOSTNAME   = serverSettings.getProperty("ExternalHostname", "*");
	            
	            PATH_NODE_RADIUS    = Integer.parseInt(serverSettings.getProperty("PathNodeRadius", "50"));
	            NEW_NODE_ID		    = Integer.parseInt(serverSettings.getProperty("NewNodeId", "7952"));
	            SELECTED_NODE_ID    = Integer.parseInt(serverSettings.getProperty("NewNodeId", "7952"));
	            LINKED_NODE_ID      = Integer.parseInt(serverSettings.getProperty("NewNodeId", "7952"));
	            NEW_NODE_TYPE       = serverSettings.getProperty("NewNodeType", "npc");
	            
	            SERVER_SIDE_NPC_NAME            = Boolean.valueOf(serverSettings.getProperty("ServerSideNpcName", "False"));
	            SERVER_SIDE_NPC_NAME_WITH_LVL   = Boolean.valueOf(serverSettings.getProperty("ServerSideNpcNameWithLvl", "False"));

	            AUTODELETE_INVALID_QUEST_DATA   = Boolean.valueOf(serverSettings.getProperty("AutoDeleteInvalidQuestData", "False"));
	            THREAD_P_HIGH                   = Integer.parseInt(serverSettings.getProperty("ThreadPoolSizeHigh", "4"));
	            THREAD_P_MED                    = Integer.parseInt(serverSettings.getProperty("ThreadPoolSizeMed", "10"));
	            THREAD_P_LOW                    = Integer.parseInt(serverSettings.getProperty("ThreadPoolSizeLow", "4"));
	            
	            MAXIMUM_ONLINE_USERS        = Integer.parseInt(serverSettings.getProperty("MaximumOnlineUsers", "100"));
	            
                ZONE_TOWN          = Integer.parseInt(serverSettings.getProperty("ZoneTown", "0"));
                
				DAY_STATUS_FORCE_CLIENT_UPDATE  = Boolean.valueOf(serverSettings.getProperty("DayStatusForceClientUpdate", "True"));
	            DAY_STATUS_SUN_RISE_AT          = Integer.parseInt(serverSettings.getProperty("DayStatusSunRiseAt", "6"));
	            DAY_STATUS_SUN_SET_AT           = Integer.parseInt(serverSettings.getProperty("DayStatusSunSetAt", "18"));
	
	            DATABASE_DRIVER             = serverSettings.getProperty("Driver", "com.mysql.jdbc.Driver");
	            DATABASE_URL                = serverSettings.getProperty("URL", "jdbc:mysql://localhost/l2jdb");
	            DATABASE_LOGIN              = serverSettings.getProperty("Login", "root");
	            DATABASE_PASSWORD           = serverSettings.getProperty("Password", "");
	            DATABASE_MAX_CONNECTIONS    = Integer.parseInt(serverSettings.getProperty("MaximumDbConnections", "10"));
	            
	            COUNT_PACKETS          = Boolean.valueOf(serverSettings.getProperty("CountPacket", "false"));  
	            DUMP_PACKET_COUNTS     = Boolean.valueOf(serverSettings.getProperty("DumpPacketCounts", "false"));
                DUMP_INTERVAL_SECONDS  = Integer.parseInt(serverSettings.getProperty("PacketDumpInterval", "60"));
                
                SERVER_LIST_BRACKET = Boolean.valueOf(serverSettings.getProperty("ServerListBrackets", "false"));
                SERVER_LIST_CLOCK   = Boolean.valueOf(serverSettings.getProperty("ServerListClock", "false"));
                SERVER_GMONLY       = Boolean.valueOf(serverSettings.getProperty("ServerGMOnly", "false"));
                
                MINIMUM_UPDATE_DISTANCE = Integer.parseInt(serverSettings.getProperty("MaximumUpdateDistance", "50"));
                MINIMUN_UPDATE_TIME = Integer.parseInt(serverSettings.getProperty("MinimumUpdateTime", "500"));
                CHECK_KNOWN             = Boolean.valueOf(serverSettings.getProperty("CheckKnownList", "false"));
                KNOWNLIST_FORGET_DELAY = Integer.parseInt(serverSettings.getProperty("KnownListForgetDelay", "10000"));
                
                LAZY_CACHE  = Boolean.valueOf(serverSettings.getProperty("LazyCache", "False"));
                
                EXECUTOR_PRI_LOW = Integer.parseInt(serverSettings.getProperty("ExecutorPriLow", "-1"));
                EXECUTOR_PRI_MED = Integer.parseInt(serverSettings.getProperty("ExecutorPriMed", "0"));
                EXECUTOR_PRI_HIGH = Integer.parseInt(serverSettings.getProperty("ExecutorPriHigh", "1"));
                
                DEFAULT_PUNISH = Integer.parseInt(serverSettings.getProperty("DefaultPunish", "2"));
                BYPASS_VALIDATION = Boolean.valueOf(serverSettings.getProperty("BypassValidation", "False"));
               // CUSTOM PVP PK COLOR SYSTEM!========================================================
				// HOLYDOGSHIT! i forgot that! >.< 
                //Sometimes i think im stupid. . . 
                PVP_COLOR_SYSTEM_ENABLED = Boolean.parseBoolean(serverSettings.getProperty("EnablePvPColorSystem", "False"));
				PVP_AMOUNT1 = Integer.parseInt(serverSettings.getProperty("PvpAmount1", "500"));
				PVP_AMOUNT2 = Integer.parseInt(serverSettings.getProperty("PvpAmount2", "1000"));
				PVP_AMOUNT3 = Integer.parseInt(serverSettings.getProperty("PvpAmount3", "1500"));
				PVP_AMOUNT4 = Integer.parseInt(serverSettings.getProperty("PvpAmount4", "2500"));
				PVP_AMOUNT5 = Integer.parseInt(serverSettings.getProperty("PvpAmount5", "5000"));
				NAME_COLOR_FOR_PVP_AMOUNT1 = Integer.decode("0x" + serverSettings.getProperty("ColorForAmount1", "00FF00"));
				NAME_COLOR_FOR_PVP_AMOUNT2 = Integer.decode("0x" + serverSettings.getProperty("ColorForAmount2", "00FF00"));
				NAME_COLOR_FOR_PVP_AMOUNT3 = Integer.decode("0x" + serverSettings.getProperty("ColorForAmount3", "00FF00"));
				NAME_COLOR_FOR_PVP_AMOUNT4 = Integer.decode("0x" + serverSettings.getProperty("ColorForAmount4", "00FF00"));
				NAME_COLOR_FOR_PVP_AMOUNT5 = Integer.decode("0x" + serverSettings.getProperty("ColorForAmount4", "00FF00"));
				PK_COLOR_SYSTEM_ENABLED = Boolean.parseBoolean(serverSettings.getProperty("EnablePkColorSystem", "False"));
				PK_AMOUNT1 = Integer.parseInt(serverSettings.getProperty("PkAmount1", "500"));
				PK_AMOUNT2 = Integer.parseInt(serverSettings.getProperty("PkAmount2", "1000"));
				PK_AMOUNT3 = Integer.parseInt(serverSettings.getProperty("PkAmount3", "1500"));
				PK_AMOUNT4 = Integer.parseInt(serverSettings.getProperty("PkAmount4", "2500"));
				PK_AMOUNT5 = Integer.parseInt(serverSettings.getProperty("PkAmount5", "5000"));
				TITLE_COLOR_FOR_PK_AMOUNT1 = Integer.decode("0x" + serverSettings.getProperty("TitleForAmount1", "00FF00"));
				TITLE_COLOR_FOR_PK_AMOUNT2 = Integer.decode("0x" + serverSettings.getProperty("TitleForAmount2", "00FF00"));
				TITLE_COLOR_FOR_PK_AMOUNT3 = Integer.decode("0x" + serverSettings.getProperty("TitleForAmount3", "00FF00"));
				TITLE_COLOR_FOR_PK_AMOUNT4 = Integer.decode("0x" + serverSettings.getProperty("TitleForAmount4", "00FF00"));
				TITLE_COLOR_FOR_PK_AMOUNT5 = Integer.decode("0x" + serverSettings.getProperty("TitleForAmount5", "00FF00"));
			
				
				
				
				
		    }
	        catch (Exception e)
	        {
	            e.printStackTrace();
	            throw new Error("Failed to Load "+CONFIGURATION_FILE+" File.");
	        }
	        
	        // Try to load filter say list before all
	        filterLoad();
	        
	        /*
	         * Load L2J Version Properties file (if exists)
	         */
	        try
	        {
	            Properties serverVersion    = new Properties();
	            InputStream is              = new FileInputStream(new File(VERSION_FILE));  
	            serverVersion.load(is);
	            is.close();
	            
	            SERVER_VERSION      = serverVersion.getProperty("version", "Unsupported Custom Version.");
	            SERVER_BUILD_DATE   = serverVersion.getProperty("builddate", "Undefined Date.");
	        }
	        catch (Exception e)
	        {
	            //Ignore Properties file if it doesnt exist
	            SERVER_VERSION      = "Unsupported Custom Version.";
	            SERVER_BUILD_DATE   = "Undefined Date.";
	        }
	        
	        // telnet
	        try
	        {
	            Properties telnetSettings   = new Properties();
	            InputStream is              = new FileInputStream(new File(TELNET_FILE));  
	            telnetSettings.load(is);
	            is.close();
	            
	            IS_TELNET_ENABLED   = Boolean.valueOf(telnetSettings.getProperty("EnableTelnet", "false"));
	        }
	        catch (Exception e)
	        {
	            e.printStackTrace();
	            throw new Error("Failed to Load "+TELNET_FILE+" File.");
	        }
	        
	        // id factory
	        try
	        {
	            Properties idSettings   = new Properties();
	            InputStream is          = new FileInputStream(new File(ID_CONFIG_FILE));
	            idSettings.load(is);
	            is.close();
	            
	            MAP_TYPE        = ObjectMapType.valueOf(idSettings.getProperty("L2Map", "WorldObjectMap"));
	            SET_TYPE        = ObjectSetType.valueOf(idSettings.getProperty("L2Set", "WorldObjectSet"));
	            IDFACTORY_TYPE  = IdFactoryType.valueOf(idSettings.getProperty("IDFactory", "Compaction"));
	            BAD_ID_CHECKING = Boolean.valueOf(idSettings.getProperty("BadIdChecking", "True"));
	        }
	        catch (Exception e)
	        {
	            e.printStackTrace();
	            throw new Error("Failed to Load "+ID_CONFIG_FILE+" File.");
	        }
	        
	        // other
	        try
	        {
	            Properties otherSettings    = new Properties();
	            InputStream is              = new FileInputStream(new File(OTHER_CONFIG_FILE));
	            otherSettings.load(is);
	            is.close();
	            
	            DEEPBLUE_DROP_RULES = Boolean.parseBoolean(otherSettings.getProperty("UseDeepBlueDropRules", "True"));
	            ALLOW_GUARDS        = Boolean.valueOf(otherSettings.getProperty("AllowGuards", "False"));
	            EFFECT_CANCELING    = Boolean.valueOf(otherSettings.getProperty("CancelLesserEffect", "True"));
	            WYVERN_SPEED        = Integer.parseInt(otherSettings.getProperty("WyvernSpeed", "100"));         
	            STRIDER_SPEED       = Integer.parseInt(otherSettings.getProperty("StriderSpeed", "80"));
	            ALLOW_WYVERN_UPGRADER     = Boolean.valueOf(otherSettings.getProperty("AllowWyvernUpgrader", "False"));
	            
	            /* Inventory slots limits */
                INVENTORY_MAXIMUM_NO_DWARF  = Integer.parseInt(otherSettings.getProperty("MaximumSlotsForNoDwarf", "80"));
                INVENTORY_MAXIMUM_DWARF  = Integer.parseInt(otherSettings.getProperty("MaximumSlotsForDwarf", "100"));
	            INVENTORY_MAXIMUM_GM    = Integer.parseInt(otherSettings.getProperty("MaximumSlotsForGMPlayer", "250"));
                
                /* Inventory slots limits */
                WAREHOUSE_SLOTS_NO_DWARF = Integer.parseInt(otherSettings.getProperty("MaximumWarehouseSlotsForNoDwarf", "80"));
                WAREHOUSE_SLOTS_DWARF = Integer.parseInt(otherSettings.getProperty("MaximumWarehouseSlotsForDwarf", "150"));
                WAREHOUSE_SLOTS_CLAN = Integer.parseInt(otherSettings.getProperty("MaximumWarehouseSlotsForClan", "150"));
                FREIGHT_SLOTS       = Integer.parseInt(otherSettings.getProperty("MaximumFreightSlots", "20"));

	            
	            /* chance to enchant an item over +3 */
	            ENCHANT_CHANCE  = Integer.parseInt(otherSettings.getProperty("EnchantChance", "65"));
	            /* limit on enchant */
	            ENCHANT_MAX = Integer.parseInt(otherSettings.getProperty("EnchantMax", "255"));
	            
	            /* if different from 100 (ie 100%) heal rate is modified acordingly */
	            HP_REGEN_MULTIPLIER = Double.parseDouble(otherSettings.getProperty("HpRegenMultiplier", "100"));
	            MP_REGEN_MULTIPLIER = Double.parseDouble(otherSettings.getProperty("MpRegenMultiplier", "100"));
	            CP_REGEN_MULTIPLIER = Double.parseDouble(otherSettings.getProperty("CpRegenMultiplier", "100"));

                RAID_HP_REGEN_MULTIPLIER  = Double.parseDouble(otherSettings.getProperty("RaidHpRegenMultiplier", "500")) /100;    
                RAID_MP_REGEN_MULTIPLIER  = Double.parseDouble(otherSettings.getProperty("RaidMpRegenMultiplier", "500")) /100;    
                RAID_DEFENCE_MULTIPLIER  = Double.parseDouble(otherSettings.getProperty("RaidDefenceMultiplier", "500")) /100;    
	            
	            STARTING_ADENA      = Integer.parseInt(otherSettings.getProperty("StartingAdena", "100"));
	            UNSTUCK_INTERVAL    = Integer.parseInt(otherSettings.getProperty("UnstuckInterval", "300"));

                /* Player protection after teleport or login */
                PLAYER_SPAWN_PROTECTION = Integer.parseInt(otherSettings.getProperty("PlayerSpawnProtection", "0"));
	            
	            /* Defines some Party XP related values */
	            PARTY_XP_CUTOFF_METHOD  = otherSettings.getProperty("PartyXpCutoffMethod", "percentage");
	            PARTY_XP_CUTOFF_PERCENT = Double.parseDouble(otherSettings.getProperty("PartyXpCutoffPercent", "3."));
	            PARTY_XP_CUTOFF_LEVEL   = Integer.parseInt(otherSettings.getProperty("PartyXpCutoffLevel", "30"));
	            
	            /* Amount of HP, MP, and CP is restored */
	            RESPAWN_RESTORE_CP = Double.parseDouble(otherSettings.getProperty("RespawnRestoreCP", "0")) / 100;
	            RESPAWN_RESTORE_HP = Double.parseDouble(otherSettings.getProperty("RespawnRestoreHP", "70")) / 100;
	            RESPAWN_RESTORE_MP = Double.parseDouble(otherSettings.getProperty("RespawnRestoreMP", "70")) / 100;
	            
	            RESPAWN_RANDOM_ENABLED = Boolean.parseBoolean(otherSettings.getProperty("RespawnRandomInTown", "False"));
	            RESPAWN_RANDOM_MAX_OFFSET = Integer.parseInt(otherSettings.getProperty("RespawnRandomMaxOffset", "50"));
	            /* Maximum number of available slots for pvt stores */
	            MAX_PVTSTORE_SLOTS_DWARF = Integer.parseInt(otherSettings.getProperty("MaxPvtStoreSlotsDwarf", "5"));
	            MAX_PVTSTORE_SLOTS_OTHER = Integer.parseInt(otherSettings.getProperty("MaxPvtStoreSlotsOther", "4"));
	            
	            STORE_SKILL_COOLTIME = Boolean.parseBoolean(otherSettings.getProperty("StoreSkillCooltime", "true"));
                
	            PET_RENT_NPC =  otherSettings.getProperty("ListPetRentNpc", "7827");

	            LIST_PET_RENT_NPC = new FastList<Integer>();
	            for (String id : PET_RENT_NPC.split(",")) {
	                LIST_PET_RENT_NPC.add(Integer.parseInt(id));
	            }
	            NONDROPPABLE_ITEMS        = otherSettings.getProperty("ListOfNonDroppableItems", "1147,425,1146,461,10,2368,7,6,2370,2369,5598");
	            
	            LIST_NONDROPPABLE_ITEMS = new FastList<Integer>();
	            for (String id : NONDROPPABLE_ITEMS.split(",")) {
	                LIST_NONDROPPABLE_ITEMS.add(Integer.parseInt(id));
	            }
                
	            ANNOUNCE_MAMMON_SPAWN = Boolean.parseBoolean(otherSettings.getProperty("AnnounceMammonSpawn", "True"));
                
                ALT_PRIVILEGES_ADMIN = Boolean.parseBoolean(otherSettings.getProperty("AltPrivilegesAdmin", "False"));
                ALT_PRIVILEGES_SECURE_CHECK = Boolean.parseBoolean(otherSettings.getProperty("AltPrivilegesSecureCheck", "True"));
                ALT_PRIVILEGES_DEFAULT_LEVEL = Integer.parseInt(otherSettings.getProperty("AltPrivilegesDefaultLevel", "100"));
                
                GM_NAME_COLOUR_ENABLED = Boolean.parseBoolean(otherSettings.getProperty("GMNameColourEnabled", "False"));
                GM_NAME_COLOUR = Integer.decode("0x" + otherSettings.getProperty("GMNameColour", "00FF00"));
                
	        }
	        catch (Exception e)
	        {
	            e.printStackTrace();
	            throw new Error("Failed to Load "+OTHER_CONFIG_FILE+" File.");
	        }
	        
	        // spoil
	        try
	        {
	            Properties spoilSettings    = new Properties();
	            InputStream is              = new FileInputStream(new File(SPOIL_CONFIG_FILE));
	            spoilSettings.load(is);
	            is.close();
	            
	            CAN_SPOIL_LOWER_LEVEL_MOBS              = Boolean.parseBoolean(spoilSettings.getProperty("CanSpoilLowerLevelMobs", "false"));
	            CAN_DELEVEL_AND_SPOIL_MOBS              = Boolean.parseBoolean(spoilSettings.getProperty("CanDelevelToSpoil", "true"));                       
	            MAXIMUM_PLAYER_AND_MOB_LEVEL_DIFFERENCE = Float.parseFloat(spoilSettings.getProperty("MaximumPlayerAndMobLevelDifference", "9."));
	            BASE_SPOIL_RATE                         = Float.parseFloat(spoilSettings.getProperty("BasePercentChanceOfSpoilSuccess", "40."));
	            MINIMUM_SPOIL_RATE                      = Float.parseFloat(spoilSettings.getProperty("MinimumPercentChanceOfSpoilSuccess", "3."));
	            SPOIL_LEVEL_DIFFERENCE_LIMIT            = Float.parseFloat(spoilSettings.getProperty("SpoilLevelDifferenceLimit", "5."));
	            SPOIL_LEVEL_DIFFERENCE_MULTIPLIER       = Float.parseFloat(spoilSettings.getProperty("SpoilLevelMultiplier", "7."));
	            LAST_LEVEL_SPOIL_IS_LEARNED             = Integer.parseInt(spoilSettings.getProperty("LastLevelSpoilIsLearned", "72"));
	        }
	        catch (Exception e) {
	            e.printStackTrace();
	            throw new Error("Failed to Load "+SPOIL_CONFIG_FILE+" File.");
	        }
	        
	        // alternative settings
	        try
	        {
	            Properties altSettings  = new Properties();
	            InputStream is          = new FileInputStream(new File(ALT_SETTINGS_FILE));  
	            altSettings.load(is);
	            is.close();
	            
	            ALT_GAME_TIREDNESS      = Boolean.parseBoolean(altSettings.getProperty("AltGameTiredness", "false"));
	            ALT_GAME_CREATION       = Boolean.parseBoolean(altSettings.getProperty("AltGameCreation", "false"));
	            ALT_GAME_SKILL_LEARN    = Boolean.parseBoolean(altSettings.getProperty("AltGameSkillLearn", "false"));
	            ALT_GAME_CANCEL_BOW     = altSettings.getProperty("AltGameCancelByHit", "Cast").equalsIgnoreCase("bow") || altSettings.getProperty("AltGameCancelByHit", "Cast").equalsIgnoreCase("all");
	            ALT_GAME_CANCEL_CAST    = altSettings.getProperty("AltGameCancelByHit", "Cast").equalsIgnoreCase("cast") || altSettings.getProperty("AltGameCancelByHit", "Cast").equalsIgnoreCase("all");
	            ALT_GAME_SHIELD_BLOCKS  = Boolean.parseBoolean(altSettings.getProperty("AltShieldBlocks", "false"));
	            ALT_GAME_DELEVEL        = Boolean.parseBoolean(altSettings.getProperty("Delevel", "true"));
            	    ALT_GAME_MAGICFAILURES  = Boolean.parseBoolean(altSettings.getProperty("MagicFailures", "false"));
	            ALT_GAME_MOB_ATTACK_AI  = Boolean.parseBoolean(altSettings.getProperty("AltGameMobAttackAI", "false"));
	            ALT_GAME_SKILL_FORMULAS = altSettings.getProperty("AltGameSkillFormulas", "none");
	            ALT_GAME_EXPONENT_XP    = Float.parseFloat(altSettings.getProperty("AltGameExponentXp", "0."));
	            ALT_GAME_EXPONENT_SP    = Float.parseFloat(altSettings.getProperty("AltGameExponentSp", "0."));
	            ALLOW_CLASS_MASTERS		 	= altSettings.getProperty("AllowClassMasters", "1,2,3");
	            if(ALLOW_CLASS_MASTERS.length()!=0)
	                for (String id : ALLOW_CLASS_MASTERS.split(","))
	                    ALLOW_CLASS_MASTERS_LIST.add(Integer.parseInt(id));

	            ALT_GAME_FREIGHTS       = Boolean.parseBoolean(altSettings.getProperty("AltGameFreights", "false"));
	            ALT_GAME_FREIGHT_PRICE  = Integer.parseInt(altSettings.getProperty("AltGameFreightPrice", "1000"));
	            ALT_GAME_SKILL_HIT_RATE = Float.parseFloat(altSettings.getProperty("AltGameSkillHitRate", "1."));
	            ENABLE_RATE_HP          = Boolean.parseBoolean(altSettings.getProperty("EnableRateHp", "false"));
	            IS_CRAFTING_ENABLED     = Boolean.parseBoolean(altSettings.getProperty("CraftingEnabled", "true"));
	            SP_BOOK_NEEDED          = Boolean.parseBoolean(altSettings.getProperty("SpBookNeeded", "true"));
	            AUTO_LOOT               = altSettings.getProperty("AutoLoot").equalsIgnoreCase("True");
                ALT_GAME_KARMA_PLAYER_CAN_BE_KILLED_IN_PEACEZONE    = Boolean.valueOf(altSettings.getProperty("AltKarmaPlayerCanBeKilledInPeaceZone", "false"));
                ALT_GAME_KARMA_PLAYER_CAN_SHOP                      = Boolean.valueOf(altSettings.getProperty("AltKarmaPlayerCanShop", "true"));
                ALT_GAME_KARMA_PLAYER_CAN_TELEPORT                  = Boolean.valueOf(altSettings.getProperty("AltKarmaPlayerCanTeleport", "true"));
                ALT_GAME_KARMA_PLAYER_CAN_TRADE                     = Boolean.valueOf(altSettings.getProperty("AltKarmaPlayerCanTrade", "true"));
                ALT_GAME_KARMA_PLAYER_CAN_USE_WAREHOUSE             = Boolean.valueOf(altSettings.getProperty("AltKarmaPlayerCanUseWareHouse", "true"));
                ALT_GAME_KARMA_PLAYER_CAN_USE_GATEKEEPER            = Boolean.valueOf(altSettings.getProperty("AltKarmaPlayerCanUseGateKeeper", "false"));
                ALT_GAME_REQUIRE_CASTLE_DAWN                        = Boolean.parseBoolean(altSettings.getProperty("AltRequireCastleForDawn", "False"));
                ALT_GAME_REQUIRE_CLAN_CASTLE                        = Boolean.parseBoolean(altSettings.getProperty("AltRequireClanCastle", "False"));
                ALT_GAME_FREE_TELEPORT                              = Boolean.parseBoolean(altSettings.getProperty("AltFreeTeleporting", "False"));
                ALT_GAME_SUBCLASS_WITHOUT_QUESTS                    = Boolean.parseBoolean(altSettings.getProperty("AltSubClassWithoutQuests", "False"));
                ALT_GAME_VIEWNPC                    				= Boolean.parseBoolean(altSettings.getProperty("AltGameViewNpc", "False"));
	        }
	        catch (Exception e)
	        {
	            e.printStackTrace();
	            throw new Error("Failed to Load "+ALT_SETTINGS_FILE+" File.");
	        }

	        // custom
	        try
	        {
	            Properties customSettings   = new Properties();
	            InputStream is          = new FileInputStream(new File(CUSTOM_CONFIG_FILE));
	            customSettings.load(is);
	            is.close();
	            
                    SHOW_DROPLIST	= Boolean.parseBoolean(customSettings.getProperty("ShowDroplist", "false"));
	            SHOW_L2J_LICENSE 	= Boolean.parseBoolean(customSettings.getProperty("ShowL2JLicense", "true"));
	            SHOW_HTML_WELCOME 	= Boolean.parseBoolean(customSettings.getProperty("ShowHTMLWelcome", "false"));
	            ALT_BUFF_TIME 	= Integer.parseInt(customSettings.getProperty("AltBuffTime", "1"));
	            ALT_CHARGE 		= Boolean.parseBoolean(customSettings.getProperty("AltCharge", "false"));
	            AUTO_LEARN_SKILLS 	= Boolean.parseBoolean(customSettings.getProperty("AutoLearnSkills", "false"));
	            MAX_PATK_SPEED 	= Integer.parseInt(customSettings.getProperty("MaxPAtkSpeed", "0"));
	            MAX_MATK_SPEED 	= Integer.parseInt(customSettings.getProperty("MaxMAtkSpeed", "0"));
	            CLAN_PENALTY 	= Boolean.parseBoolean(customSettings.getProperty("ClanPenalty", "true"));
	            ALLOW_SS_TELEPORT 	= Boolean.parseBoolean(customSettings.getProperty("AllowSSTeleport", "false"));
	            ALLOW_SS_REGIST 	= Boolean.parseBoolean(customSettings.getProperty("AllowSSRegist", "false"));
	            MIN_SS_PARTY 	= Integer.parseInt(customSettings.getProperty("MinSSParty", "5"));
	            VALUE_AA		= Integer.parseInt(customSettings.getProperty("ValueAA", "1"));
	            SHOW_SS_PERIOD 	= Boolean.parseBoolean(customSettings.getProperty("ShowSSPeriod", "true"));
	            SHOW_GM_LOGIN 	= Boolean.parseBoolean(customSettings.getProperty("ShowGMLogin", "false"));
	            HIDE_GM_STATUS	= Boolean.parseBoolean(customSettings.getProperty("HideGMStatus", "false")); 
	            GM_NAME_COLOR 	= customSettings.getProperty("GMNameColor","none");
	            VIP_NAME_COLOR 	= customSettings.getProperty("VIPNameColor","none");
	            NORMAL_NAME_COLOR 	= customSettings.getProperty("NormalNameColor","none");
	            VIP_ACCESSLEVEL 	= Integer.parseInt(customSettings.getProperty("VIPAccessLevel", "10"));
	            PET_HUNGER 		= Boolean.parseBoolean(customSettings.getProperty("HungryPetCanDie", "true"));
	            DOT_KILL 		= Boolean.parseBoolean(customSettings.getProperty("DoTCanKill", "true"));
	            ALLOW_KILL_ALL_NPC 	= Boolean.parseBoolean(customSettings.getProperty("AllowKillAllNpc", "false"));
	            RESTART_RECOM 	= Boolean.parseBoolean(customSettings.getProperty("RestartRecommandation", "true"));
	            MAIN_LANG		= customSettings.getProperty("MainLanguage", "en");
	            GRADE_PENALTY	= Boolean.parseBoolean(customSettings.getProperty("GradePenalty", "true"));
	            DEEPBLUE_EXP_RULES	= Boolean.parseBoolean(customSettings.getProperty("UseDeepBlueExpRules", "true"));
	            MAX_ITEM_BUY	= Integer.parseInt(customSettings.getProperty("MaxItemBuy", "0"));
	            SPAWN_SIEGE_GUARD 	= Boolean.parseBoolean(customSettings.getProperty("SpawnSiegeGuard", "true"));
	            USE_SAY_FILTER 	= Boolean.parseBoolean(customSettings.getProperty("UseSayFilter", "false"));
	        }
	        catch (Exception e)
	        {
	            e.printStackTrace();
	            throw new Error("Failed to Load "+CUSTOM_CONFIG_FILE+" File.");
	        }
	        
	        // pvp config
	        try
	        {
	            Properties pvpSettings      = new Properties();
	            InputStream is              = new FileInputStream(new File(PVP_CONFIG_FILE));  
	            pvpSettings.load(is);
	            is.close();
	            
	            /* KARMA SYSTEM */
	            KARMA_MIN_KARMA     = Integer.parseInt(pvpSettings.getProperty("MinKarma", "240"));
	            KARMA_MAX_KARMA     = Integer.parseInt(pvpSettings.getProperty("MaxKarma", "10000"));
	            KARMA_XP_DIVIDER    = Integer.parseInt(pvpSettings.getProperty("XPDivider", "260"));
	            KARMA_LOST_BASE     = Integer.parseInt(pvpSettings.getProperty("BaseKarmaLost", "0"));
	            
	            KARMA_DROP_GM               = Boolean.parseBoolean(pvpSettings.getProperty("CanGMDropEquipment", "false"));
	            KARMA_AWARD_PK_KILL         = Boolean.parseBoolean(pvpSettings.getProperty("AwardPKKillPVPPoint", "true"));
	            
	            KARMA_PK_LIMIT                      = Integer.parseInt(pvpSettings.getProperty("MinimumPKRequiredToDrop", "5"));
	            
	            KARMA_NONDROPPABLE_PET_ITEMS    = pvpSettings.getProperty("ListOfPetItems", "3500,3501,3502,4422,4423,4424,2375");          
	            KARMA_NONDROPPABLE_ITEMS        = pvpSettings.getProperty("ListOfNonDroppableItems", "57,1147,425,1146,461,10,2368,7,6,2370,2369");
	            
	            KARMA_LIST_NONDROPPABLE_PET_ITEMS = new FastList<Integer>();
	            for (String id : KARMA_NONDROPPABLE_PET_ITEMS.split(",")) {
	                KARMA_LIST_NONDROPPABLE_PET_ITEMS.add(Integer.parseInt(id));
	            }
	            
	            KARMA_LIST_NONDROPPABLE_ITEMS = new FastList<Integer>();
	            for (String id : KARMA_NONDROPPABLE_ITEMS.split(",")) {
	                KARMA_LIST_NONDROPPABLE_ITEMS.add(Integer.parseInt(id));
	            }
	            
	            PVP_TIME = Integer.parseInt(pvpSettings.getProperty("PvPTime", "15000"));
	        }
	        catch (Exception e)
	        {
	            e.printStackTrace();
	            throw new Error("Failed to Load "+PVP_CONFIG_FILE+" File.");
	        }
	        
	        // access levels
	        try
	        {
	            Properties gmSettings   = new Properties();
	            InputStream is          = new FileInputStream(new File(GM_ACCESS_FILE));  
	            gmSettings.load(is);
	            is.close();               
	            
	            GM_ACCESSLEVEL  = Integer.parseInt(gmSettings.getProperty("GMAccessLevel", "100"));
	            GM_MIN          = Integer.parseInt(gmSettings.getProperty("GMMinLevel", "100"));
	            GM_CAN_CONFIG   = Integer.parseInt(gmSettings.getProperty("GMCanConfig", "100"));
	            GM_ANNOUNCE     = Integer.parseInt(gmSettings.getProperty("GMCanAnnounce", "100"));
	            GM_BAN          = Integer.parseInt(gmSettings.getProperty("GMCanBan", "100"));
	            GM_BAN_CHAT     = Integer.parseInt(gmSettings.getProperty("GMCanBanChat", "100"));
	            GM_CREATE_ITEM  = Integer.parseInt(gmSettings.getProperty("GMCanShop", "100"));
	            GM_DELETE       = Integer.parseInt(gmSettings.getProperty("GMCanDelete", "100"));
	            GM_KICK         = Integer.parseInt(gmSettings.getProperty("GMCanKick", "100"));
	            GM_MENU         = Integer.parseInt(gmSettings.getProperty("GMMenu", "100"));
	            GM_GODMODE      = Integer.parseInt(gmSettings.getProperty("GMGodMode", "100"));
	            GM_CHAR_EDIT    = Integer.parseInt(gmSettings.getProperty("GMCanEditChar", "100"));
	            GM_CHAR_EDIT_OTHER    = Integer.parseInt(gmSettings.getProperty("GMCanEditCharOther", "100"));
	            GM_CHAR_VIEW    = Integer.parseInt(gmSettings.getProperty("GMCanViewChar", "100"));
	            GM_NPC_EDIT     = Integer.parseInt(gmSettings.getProperty("GMCanEditNPC", "100"));
	            GM_NPC_VIEW     = Integer.parseInt(gmSettings.getProperty("GMCanViewNPC", "100"));
	            GM_TELEPORT     = Integer.parseInt(gmSettings.getProperty("GMCanTeleport", "100"));
	            GM_TELEPORT_OTHER     = Integer.parseInt(gmSettings.getProperty("GMCanTeleportOther", "100"));
	            GM_RESTART      = Integer.parseInt(gmSettings.getProperty("GMCanRestart", "100"));
	            GM_MONSTERRACE  = Integer.parseInt(gmSettings.getProperty("GMMonsterRace", "100"));
	            GM_RIDER        = Integer.parseInt(gmSettings.getProperty("GMRider", "100"));
	            GM_ESCAPE       = Integer.parseInt(gmSettings.getProperty("GMFastUnstuck", "100"));
	            GM_FIXED        = Integer.parseInt(gmSettings.getProperty("GMResurectFixed", "100"));
	            GM_CREATE_NODES = Integer.parseInt(gmSettings.getProperty("GMCreateNodes", "100"));
	            GM_DOOR         = Integer.parseInt(gmSettings.getProperty("GMDoor", "100"));
	            GM_RES          = Integer.parseInt(gmSettings.getProperty("GMRes", "100"));
	            GM_PEACEATTACK  = Integer.parseInt(gmSettings.getProperty("GMPeaceAttack", "100"));
	            GM_HEAL         = Integer.parseInt(gmSettings.getProperty("GMHeal", "100"));
	            GM_ENCHANT      = Integer.parseInt(gmSettings.getProperty("GMEnchant", "100"));
	            GM_UNBLOCK      = Integer.parseInt(gmSettings.getProperty("GMUnblock", "100"));
                GM_CACHE        = Integer.parseInt(gmSettings.getProperty("GMCache", "100"));
                GM_TALK_BLOCK   = Integer.parseInt(gmSettings.getProperty("GMTalkBlock", "100"));
                GM_TEST         = Integer.parseInt(gmSettings.getProperty("GMTest", "100"));
                
                String gmTrans = gmSettings.getProperty("GMDisableTransaction", "False");
                
                if (!gmTrans.equalsIgnoreCase("false"))
                {
                    String[] params = gmTrans.split(",");
                    GM_DISABLE_TRANSACTION = true;
                    GM_TRANSACTION_MIN = Integer.parseInt(params[0]);
                    GM_TRANSACTION_MAX = Integer.parseInt(params[1]);
                }
                else
                {
                    GM_DISABLE_TRANSACTION = false; 
                }
                
	        }
	        catch (Exception e)
	        {
	            e.printStackTrace();
	            throw new Error("Failed to Load "+GM_ACCESS_FILE+" File.");
	        }
	        
	        try
	        {
	            Properties Settings   = new Properties();
	            InputStream is          = new FileInputStream(HEXID_FILE);  
	            Settings.load(is);
	            is.close();  
	            HEX_ID = new BigInteger(Settings.getProperty("HexID"), 16).toByteArray();
	        }
	        catch (Exception e)
	        {
	        	
	        }
		}
		else if(Server.SERVER_MODE == Server.MODE_LOGINSERVER)
		{
			_log.info("loading login config");
			try {
		        Properties serverSettings    = new Properties();
				InputStream is               = new FileInputStream(new File(LOGIN_CONFIGURATION_FILE));  
				serverSettings.load(is);
				is.close();
				
				GAME_SERVER_LOGIN_HOST = serverSettings.getProperty("LoginserverHostname","127.0.0.1");
				GAME_SERVER_LOGIN_PORT = Integer.parseInt(serverSettings.getProperty("LoginPort","9013"));
				PORT_LOGIN              = Integer.parseInt(serverSettings.getProperty("LoginserverPort", "2106"));
				
				DEBUG        = Boolean.parseBoolean(serverSettings.getProperty("Debug", "false"));
				DEVELOPER    = Boolean.parseBoolean(serverSettings.getProperty("Developer", "false"));
				ASSERT       = Boolean.parseBoolean(serverSettings.getProperty("Assert", "false"));
				
				ACCEPT_NEW_GAMESERVER = Boolean.parseBoolean(serverSettings.getProperty("AcceptNewGameServer","True"));
				REQUEST_ID = Integer.parseInt(serverSettings.getProperty("RequestServerID","0"));
				ACCEPT_ALTERNATE_ID = Boolean.parseBoolean(serverSettings.getProperty("AcceptAlternateID","True"));
				
	            LOGIN_TRY_BEFORE_BAN    = Integer.parseInt(serverSettings.getProperty("LoginTryBeforeBan", "10"));
				
				DATAPACK_ROOT    = new File(serverSettings.getProperty("DatapackRoot", ".")).getCanonicalFile(); //FIXME: in login?
	
	            INTERNAL_HOSTNAME   = serverSettings.getProperty("InternalHostname", "localhost"); //FIXME: in login?
	            EXTERNAL_HOSTNAME   = serverSettings.getProperty("ExternalHostname", "localhost"); //FIXME: in login?
	            
	            DATABASE_DRIVER             = serverSettings.getProperty("Driver", "com.mysql.jdbc.Driver");
	            DATABASE_URL                = serverSettings.getProperty("URL", "jdbc:mysql://localhost/l2jdb");
	            DATABASE_LOGIN              = serverSettings.getProperty("Login", "root");
	            DATABASE_PASSWORD           = serverSettings.getProperty("Password", "");
	            DATABASE_MAX_CONNECTIONS    = Integer.parseInt(serverSettings.getProperty("MaximumDbConnections", "10"));
	            
	            SHOW_LICENCE = Boolean.parseBoolean(serverSettings.getProperty("ShowLicence", "true"));
	        }
	        catch (Exception e)
	        {
	            e.printStackTrace();
	            throw new Error("Failed to Load "+CONFIGURATION_FILE+" File.");
	        }
	        
//	      telnet
	        try
	        {
	            Properties telnetSettings   = new Properties();
	            InputStream is              = new FileInputStream(new File(TELNET_FILE));  
	            telnetSettings.load(is);
	            is.close();
	            
	            IS_TELNET_ENABLED   = Boolean.valueOf(telnetSettings.getProperty("EnableTelnet", "false"));
	        }
	        catch (Exception e)
	        {
	            e.printStackTrace();
	            throw new Error("Failed to Load "+TELNET_FILE+" File.");
	        }

	        /** AI Config */
	        if (Server.SERVER_MODE == Server.MODE_GAMESERVER)
			{
				try
				{
					Properties aiSettings = new Properties();
					InputStream is = new FileInputStream(new File(AI_FILE));
					aiSettings.load(is);
					is.close();

					AI_ENABLED = Boolean.valueOf(aiSettings.getProperty("EnableAI", "false"));
					AI_DEFAULT_CLASS = aiSettings.getProperty("DefaultAI");
				}
				catch (Exception e)
				{
					e.printStackTrace();
					throw new Error("Failed to Load " + AI_FILE + " File.");
				}
			}

		}
		else
		{
			_log.severe("Could not Load Config: server mode was not set");
		}
       
	}
	
	public static boolean setParameterValue(String pName, String pValue) 
 		    { 
 		        // Server settings 
 		        if (pName.equalsIgnoreCase("RateXp")) RATE_XP = Float.parseFloat(pValue); 
 		        else if (pName.equalsIgnoreCase("RateSp")) RATE_SP = Float.parseFloat(pValue); 
 		        else if (pName.equalsIgnoreCase("RatePartyXp")) RATE_PARTY_XP = Float.parseFloat(pValue); 
 		        else if (pName.equalsIgnoreCase("RatePartySp")) RATE_PARTY_SP = Float.parseFloat(pValue); 
 		        else if (pName.equalsIgnoreCase("RateQuestsReward")) RATE_QUESTS_REWARD = Float.parseFloat(pValue); 
 		        else if (pName.equalsIgnoreCase("RateDropAdena")) RATE_DROP_ADENA = Float.parseFloat(pValue); 
 		        else if (pName.equalsIgnoreCase("RateConsumableCost")) RATE_CONSUMABLE_COST = Float.parseFloat(pValue); 
 		        else if (pName.equalsIgnoreCase("RateDropItems")) RATE_DROP_ITEMS = Float.parseFloat(pValue); 
 		        else if (pName.equalsIgnoreCase("RateDropSpoil")) RATE_DROP_SPOIL = Float.parseFloat(pValue); 
 		        else if (pName.equalsIgnoreCase("RateKarmaExpLost")) RATE_KARMA_EXP_LOST = Float.parseFloat(pValue); 
 		        else if (pName.equalsIgnoreCase("RateSiegeGuardsPrice")) RATE_SIEGE_GUARDS_PRICE = Float.parseFloat(pValue); 
 		 
 		        else if (pName.equalsIgnoreCase("PlayerDropLimit")) PLAYER_DROP_LIMIT = Integer.parseInt(pValue); 
 		        else if (pName.equalsIgnoreCase("PlayerRateDrop")) PLAYER_RATE_DROP = Integer.parseInt(pValue); 
 		        else if (pName.equalsIgnoreCase("PlayerRateDropItem")) PLAYER_RATE_DROP_ITEM = Integer.parseInt(pValue); 
 		        else if (pName.equalsIgnoreCase("PlayerRateDropEquip")) PLAYER_RATE_DROP_EQUIP = Integer.parseInt(pValue); 
 		        else if (pName.equalsIgnoreCase("PlayerRateDropEquipWeapon")) PLAYER_RATE_DROP_EQUIP_WEAPON = Integer.parseInt(pValue); 
 		 
 		        else if (pName.equalsIgnoreCase("KarmaDropLimit")) KARMA_DROP_LIMIT = Integer.parseInt(pValue); 
 		        else if (pName.equalsIgnoreCase("KarmaRateDrop")) KARMA_RATE_DROP = Integer.parseInt(pValue); 
 		        else if (pName.equalsIgnoreCase("KarmaRateDropItem")) KARMA_RATE_DROP_ITEM = Integer.parseInt(pValue); 
 		        else if (pName.equalsIgnoreCase("KarmaRateDropEquip")) KARMA_RATE_DROP_EQUIP = Integer.parseInt(pValue); 
 		        else if (pName.equalsIgnoreCase("KarmaRateDropEquipWeapon")) KARMA_RATE_DROP_EQUIP_WEAPON = Integer.parseInt(pValue); 
 		 
 		        else if (pName.equalsIgnoreCase("AutoDestroyDroppedItemAfter")) AUTODESTROY_ITEM_AFTER = Integer.parseInt(pValue); 
 		        else if (pName.equalsIgnoreCase("CoordSynchronize")) COORD_SYNCHRONIZE = Integer.parseInt(pValue); 
 		        else if (pName.equalsIgnoreCase("DeleteCharAfterDays")) DELETE_DAYS = Integer.parseInt(pValue); 
 		 
 		        else if (pName.equalsIgnoreCase("AllowDiscardItem")) ALLOW_DISCARDITEM = Boolean.valueOf(pValue); 
 		        else if (pName.equalsIgnoreCase("AllowFreight")) ALLOW_FREIGHT = Boolean.valueOf(pValue); 
 		        else if (pName.equalsIgnoreCase("AllowWarehouse")) ALLOW_WAREHOUSE = Boolean.valueOf(pValue); 
 		        else if (pName.equalsIgnoreCase("AllowWear")) ALLOW_WEAR = Boolean.valueOf(pValue); 
 		        else if (pName.equalsIgnoreCase("AllowWater")) ALLOW_WATER = Boolean.valueOf(pValue); 
 		        else if (pName.equalsIgnoreCase("AllowRentPet")) ALLOW_RENTPET = Boolean.valueOf(pValue); 
 		 
 		        else if (pName.equalsIgnoreCase("ShowLevelOnCommunityBoard")) SHOW_LEVEL_COMMUNITYBOARD = Boolean.valueOf(pValue); 
 		        else if (pName.equalsIgnoreCase("ShowStatusOnCommunityBoard")) SHOW_STATUS_COMMUNITYBOARD = Boolean.valueOf(pValue); 
 		        else if (pName.equalsIgnoreCase("NamePageSizeOnCommunityBoard")) NAME_PAGE_SIZE_COMMUNITYBOARD = Integer.parseInt(pValue); 
 		        else if (pName.equalsIgnoreCase("NamePerRowOnCommunityBoard")) NAME_PER_ROW_COMMUNITYBOARD = Integer.parseInt(pValue); 
 		 
 		        else if (pName.equalsIgnoreCase("ServerSideNpcName")) SERVER_SIDE_NPC_NAME = Boolean.valueOf(pValue); 
 		        else if (pName.equalsIgnoreCase("ServerSideNpcNameWithLvl")) SERVER_SIDE_NPC_NAME_WITH_LVL = Boolean.valueOf(pValue); 
 		 
 		        else if (pName.equalsIgnoreCase("AutoDeleteInvalidQuestData")) AUTODELETE_INVALID_QUEST_DATA = Boolean.valueOf(pValue); 
 		        else if (pName.equalsIgnoreCase("GlobalChat")) DEFAULT_GLOBAL_CHAT = pValue;
 		 
 		        else if (pName.equalsIgnoreCase("MaximumOnlineUsers")) MAXIMUM_ONLINE_USERS = Integer.parseInt(pValue); 
 		 
 		        else if (pName.equalsIgnoreCase("ZoneTown")) ZONE_TOWN = Integer.parseInt(pValue); 
 		 
 		        else if (pName.equalsIgnoreCase("DayStatusForceClientUpdate")) DAY_STATUS_FORCE_CLIENT_UPDATE = Boolean.valueOf(pValue); 
 		        else if (pName.equalsIgnoreCase("DayStatusSunRiseAt")) DAY_STATUS_SUN_RISE_AT = Integer.parseInt(pValue); 
 		        else if (pName.equalsIgnoreCase("DayStatusSunSetAt")) DAY_STATUS_SUN_SET_AT = Integer.parseInt(pValue); 
 		 
 		        else if (pName.equalsIgnoreCase("MaximumUpdateDistance")) MINIMUM_UPDATE_DISTANCE = Integer.parseInt(pValue); 
 		        else if (pName.equalsIgnoreCase("MinimumUpdateTime")) MINIMUN_UPDATE_TIME = Integer.parseInt(pValue); 
 		        else if (pName.equalsIgnoreCase("CheckKnownList")) CHECK_KNOWN = Boolean.valueOf(pValue); 
 		        else if (pName.equalsIgnoreCase("KnownListForgetDelay")) KNOWNLIST_FORGET_DELAY = Integer.parseInt(pValue); 
 		 
 		        // Other settings 
 		        else if (pName.equalsIgnoreCase("UseDeepBlueDropRules")) DEEPBLUE_DROP_RULES = Boolean.valueOf(pValue); 
 		        else if (pName.equalsIgnoreCase("AllowGuards")) ALLOW_GUARDS = Boolean.valueOf(pValue); 
 		        else if (pName.equalsIgnoreCase("CancelLesserEffect")) EFFECT_CANCELING = Boolean.valueOf(pValue); 
 		        else if (pName.equalsIgnoreCase("WyvernSpeed")) WYVERN_SPEED = Integer.parseInt(pValue); 
 		        else if (pName.equalsIgnoreCase("StriderSpeed")) STRIDER_SPEED = Integer.parseInt(pValue); 
 		 
 		        else if (pName.equalsIgnoreCase("MaximumSlotsForNoDwarf")) INVENTORY_MAXIMUM_NO_DWARF = Integer.parseInt(pValue); 
 		        else if (pName.equalsIgnoreCase("MaximumSlotsForDwarf")) INVENTORY_MAXIMUM_DWARF = Integer.parseInt(pValue); 
 		        else if (pName.equalsIgnoreCase("MaximumSlotsForGMPlayer")) INVENTORY_MAXIMUM_GM = Integer.parseInt(pValue); 
 		 
 		        else if (pName.equalsIgnoreCase("MaximumWarehouseSlotsForNoDwarf")) WAREHOUSE_SLOTS_NO_DWARF = Integer.parseInt(pValue); 
 		        else if (pName.equalsIgnoreCase("MaximumWarehouseSlotsForDwarf")) WAREHOUSE_SLOTS_DWARF = Integer.parseInt(pValue); 
 		        else if (pName.equalsIgnoreCase("MaximumWarehouseSlotsForClan")) WAREHOUSE_SLOTS_CLAN = Integer.parseInt(pValue); 
 		        else if (pName.equalsIgnoreCase("MaximumFreightSlots")) FREIGHT_SLOTS = Integer.parseInt(pValue); 
 		 
 		        else if (pName.equalsIgnoreCase("EnchantChance")) ENCHANT_CHANCE = Integer.parseInt(pValue); 
 		        else if (pName.equalsIgnoreCase("EnchantMax")) ENCHANT_MAX = Integer.parseInt(pValue); 
 		 
 		        else if (pName.equalsIgnoreCase("HpRegenMultiplier")) HP_REGEN_MULTIPLIER = Double.parseDouble(pValue); 
 		        else if (pName.equalsIgnoreCase("MpRegenMultiplier")) MP_REGEN_MULTIPLIER = Double.parseDouble(pValue); 
 		        else if (pName.equalsIgnoreCase("CpRegenMultiplier")) CP_REGEN_MULTIPLIER = Double.parseDouble(pValue); 
 		 
 		        else if (pName.equalsIgnoreCase("RaidHpRegenMultiplier")) RAID_HP_REGEN_MULTIPLIER = Double.parseDouble(pValue) /100; 
 		        else if (pName.equalsIgnoreCase("RaidMpRegenMultiplier")) RAID_MP_REGEN_MULTIPLIER = Double.parseDouble(pValue) /100; 
 		        else if (pName.equalsIgnoreCase("RaidDefenceMultiplier")) RAID_DEFENCE_MULTIPLIER = Double.parseDouble(pValue) /100; 
 		 
 		        else if (pName.equalsIgnoreCase("StartingAdena")) STARTING_ADENA = Integer.parseInt(pValue); 
 		        else if (pName.equalsIgnoreCase("UnstuckInterval")) UNSTUCK_INTERVAL = Integer.parseInt(pValue); 
 		 
 		        else if (pName.equalsIgnoreCase("PlayerSpawnProtection")) PLAYER_SPAWN_PROTECTION = Integer.parseInt(pValue); 
 		 
 		        else if (pName.equalsIgnoreCase("PartyXpCutoffMethod")) PARTY_XP_CUTOFF_METHOD = pValue; 
 		        else if (pName.equalsIgnoreCase("PartyXpCutoffPercent")) PARTY_XP_CUTOFF_PERCENT = Double.parseDouble(pValue); 
 		        else if (pName.equalsIgnoreCase("PartyXpCutoffLevel")) PARTY_XP_CUTOFF_LEVEL = Integer.parseInt(pValue); 
 		 
 		        else if (pName.equalsIgnoreCase("RespawnRestoreCP")) RESPAWN_RESTORE_CP = Double.parseDouble(pValue) / 100; 
 		        else if (pName.equalsIgnoreCase("RespawnRestoreHP")) RESPAWN_RESTORE_HP = Double.parseDouble(pValue) / 100; 
 		        else if (pName.equalsIgnoreCase("RespawnRestoreMP")) RESPAWN_RESTORE_MP = Double.parseDouble(pValue) / 100; 
 		 
 		        else if (pName.equalsIgnoreCase("MaxPvtStoreSlotsDwarf")) MAX_PVTSTORE_SLOTS_DWARF = Integer.parseInt(pValue); 
 		        else if (pName.equalsIgnoreCase("MaxPvtStoreSlotsOther")) MAX_PVTSTORE_SLOTS_OTHER = Integer.parseInt(pValue); 
 		 
 		        else if (pName.equalsIgnoreCase("StoreSkillCooltime")) STORE_SKILL_COOLTIME = Boolean.valueOf(pValue); 
 		        else if (pName.equalsIgnoreCase("AnnounceMammonSpawn")) ANNOUNCE_MAMMON_SPAWN = Boolean.valueOf(pValue); 
 		         
 		        // Spoil settings 
 		        else if (pName.equalsIgnoreCase("CanSpoilLowerLevelMobs")) CAN_SPOIL_LOWER_LEVEL_MOBS = Boolean.valueOf(pValue); 
 		        else if (pName.equalsIgnoreCase("CanDelevelToSpoil")) CAN_DELEVEL_AND_SPOIL_MOBS = Boolean.valueOf(pValue); 
 		 
 		        else if (pName.equalsIgnoreCase("MaximumPlayerAndMobLevelDifference")) MAXIMUM_PLAYER_AND_MOB_LEVEL_DIFFERENCE = Float.parseFloat(pValue); 
 		        else if (pName.equalsIgnoreCase("BasePercentChanceOfSpoilSuccess")) BASE_SPOIL_RATE = Float.parseFloat(pValue); 
 		        else if (pName.equalsIgnoreCase("MinimumPercentChanceOfSpoilSuccess")) MINIMUM_SPOIL_RATE = Float.parseFloat(pValue); 
 		        else if (pName.equalsIgnoreCase("SpoilLevelDifferenceLimit")) SPOIL_LEVEL_DIFFERENCE_LIMIT = Float.parseFloat(pValue); 
 		        else if (pName.equalsIgnoreCase("SpoilLevelMultiplier")) SPOIL_LEVEL_DIFFERENCE_MULTIPLIER = Float.parseFloat(pValue); 
 		 
 		        else if (pName.equalsIgnoreCase("LastLevelSpoilIsLearned")) LAST_LEVEL_SPOIL_IS_LEARNED = Integer.parseInt(pValue); 
 		 
 		        // Alternative settings 
 		        else if (pName.equalsIgnoreCase("AltGameTiredness")) ALT_GAME_TIREDNESS = Boolean.valueOf(pValue); 
 		        else if (pName.equalsIgnoreCase("AltGameCreation")) ALT_GAME_CREATION = Boolean.valueOf(pValue); 
 		        else if (pName.equalsIgnoreCase("AltGameSkillLearn")) ALT_GAME_SKILL_LEARN = Boolean.valueOf(pValue); 
 		 
 		        else if (pName.equalsIgnoreCase("AltGameCancelByHit")) 
 		        { 
 		            ALT_GAME_CANCEL_BOW     = pValue.equalsIgnoreCase("bow") || pValue.equalsIgnoreCase("all"); 
 		            ALT_GAME_CANCEL_CAST    = pValue.equalsIgnoreCase("cast") || pValue.equalsIgnoreCase("all"); 
 		        } 
 		 
 		        else if (pName.equalsIgnoreCase("AltShieldBlocks")) ALT_GAME_SHIELD_BLOCKS = Boolean.valueOf(pValue); 
 		        else if (pName.equalsIgnoreCase("Delevel")) ALT_GAME_DELEVEL = Boolean.valueOf(pValue); 
 		        else if (pName.equalsIgnoreCase("MagicFailures")) ALT_GAME_MAGICFAILURES = Boolean.valueOf(pValue); 
 		        else if (pName.equalsIgnoreCase("AltGameMobAttackAI")) ALT_GAME_MOB_ATTACK_AI = Boolean.valueOf(pValue); 
 		        else if (pName.equalsIgnoreCase("AltGameSkillFormulas")) ALT_GAME_SKILL_FORMULAS = pValue; 
 		 
 		        else if (pName.equalsIgnoreCase("AltGameExponentXp")) ALT_GAME_EXPONENT_XP = Float.parseFloat(pValue); 
 		        else if (pName.equalsIgnoreCase("AltGameExponentSp")) ALT_GAME_EXPONENT_SP = Float.parseFloat(pValue); 
 		 
 		        else if (pName.equalsIgnoreCase("AllowClassMasters")) ALLOW_CLASS_MASTERS = String.valueOf(pValue); 
 		        else if (pName.equalsIgnoreCase("AltGameFreights")) ALT_GAME_FREIGHTS = Boolean.valueOf(pValue); 
 		        else if (pName.equalsIgnoreCase("AltGameFreightPrice")) ALT_GAME_FREIGHT_PRICE = Integer.parseInt(pValue); 
 		 
 		        else if (pName.equalsIgnoreCase("AltGameSkillHitRate")) ALT_GAME_SKILL_HIT_RATE = Float.parseFloat(pValue); 
 		        else if (pName.equalsIgnoreCase("EnableRateHp")) ENABLE_RATE_HP = Boolean.valueOf(pValue); 
 		 
 		        else if (pName.equalsIgnoreCase("CraftingEnabled")) IS_CRAFTING_ENABLED = Boolean.valueOf(pValue); 
 		        else if (pName.equalsIgnoreCase("SpBookNeeded")) SP_BOOK_NEEDED = Boolean.valueOf(pValue); 
 		        else if (pName.equalsIgnoreCase("AutoLoot")) AUTO_LOOT = Boolean.valueOf(pValue); 
 		 
 		        else if (pName.equalsIgnoreCase("AltKarmaPlayerCanBeKilledInPeaceZone")) ALT_GAME_KARMA_PLAYER_CAN_BE_KILLED_IN_PEACEZONE = Boolean.valueOf(pValue); 
 		        else if (pName.equalsIgnoreCase("AltKarmaPlayerCanShop")) ALT_GAME_KARMA_PLAYER_CAN_SHOP = Boolean.valueOf(pValue); 
 		        else if (pName.equalsIgnoreCase("AltKarmaPlayerCanTeleport")) ALT_GAME_KARMA_PLAYER_CAN_TELEPORT = Boolean.valueOf(pValue); 
 		        else if (pName.equalsIgnoreCase("AltKarmaPlayerCanTrade")) ALT_GAME_KARMA_PLAYER_CAN_TRADE = Boolean.valueOf(pValue); 
 		        else if (pName.equalsIgnoreCase("AltKarmaPlayerCanUseWareHouse")) ALT_GAME_KARMA_PLAYER_CAN_USE_WAREHOUSE = Boolean.valueOf(pValue); 
 		        else if (pName.equalsIgnoreCase("AltRequireCastleForDawn")) ALT_GAME_REQUIRE_CASTLE_DAWN = Boolean.valueOf(pValue); 
 		        else if (pName.equalsIgnoreCase("AltRequireClanCastle")) ALT_GAME_REQUIRE_CLAN_CASTLE = Boolean.valueOf(pValue); 
 		        else if (pName.equalsIgnoreCase("AltFreeTeleporting")) ALT_GAME_FREE_TELEPORT = Boolean.valueOf(pValue); 
 		        else if (pName.equalsIgnoreCase("AltSubClassWithoutQuests")) ALT_GAME_SUBCLASS_WITHOUT_QUESTS = Boolean.valueOf(pValue); 
 		 
 		        // PvP settings 
 		        else if (pName.equalsIgnoreCase("MinKarma")) KARMA_MIN_KARMA = Integer.parseInt(pValue); 
 		        else if (pName.equalsIgnoreCase("MaxKarma")) KARMA_MAX_KARMA = Integer.parseInt(pValue); 
 		        else if (pName.equalsIgnoreCase("XPDivider")) KARMA_XP_DIVIDER = Integer.parseInt(pValue); 
 		        else if (pName.equalsIgnoreCase("BaseKarmaLost")) KARMA_LOST_BASE = Integer.parseInt(pValue); 
 		 
 		        else if (pName.equalsIgnoreCase("CanGMDropEquipment")) KARMA_DROP_GM = Boolean.valueOf(pValue); 
 		        else if (pName.equalsIgnoreCase("AwardPKKillPVPPoint")) KARMA_AWARD_PK_KILL = Boolean.valueOf(pValue); 
 		        else if (pName.equalsIgnoreCase("MinimumPKRequiredToDrop")) KARMA_PK_LIMIT = Integer.parseInt(pValue); 
 		         
 		        else if (pName.equalsIgnoreCase("PvPTime")) PVP_TIME = Integer.parseInt(pValue);
 		        
 		        //Custom settings
 		        else if (pName.equalsIgnoreCase("AutoLearnSkills")) AUTO_LEARN_SKILLS = Boolean.valueOf(pValue);
 		        else if (pName.equalsIgnoreCase("ClanPenalty")) CLAN_PENALTY = Boolean.valueOf(pValue);
 		        
 		        else if (pName.equalsIgnoreCase("AllowSSTeleport")) ALLOW_SS_TELEPORT = Boolean.valueOf(pValue);
 		        else if (pName.equalsIgnoreCase("AllowSSRegist")) ALLOW_SS_REGIST = Boolean.valueOf(pValue);
 		        
 		        else if (pName.equalsIgnoreCase("ShowGMLogin")) SHOW_GM_LOGIN = Boolean.valueOf(pValue);
 		        else if (pName.equalsIgnoreCase("HideGMStatus")) HIDE_GM_STATUS = Boolean.valueOf(pValue);
 		        
 		        else if (pName.equalsIgnoreCase("HungryPetCanDie")) PET_HUNGER = Boolean.valueOf(pValue);
 		        else if (pName.equalsIgnoreCase("DoTCanKill")) DOT_KILL = Boolean.valueOf(pValue);
 		        else if (pName.equalsIgnoreCase("AllowKillAllNpc")) ALLOW_KILL_ALL_NPC = Boolean.valueOf(pValue);
 		        else if (pName.equalsIgnoreCase("RestartRecommandation")) RESTART_RECOM = Boolean.valueOf(pValue);
 		        else if (pName.equalsIgnoreCase("GradePenalty")) GRADE_PENALTY = Boolean.valueOf(pValue);
 		        else if (pName.equalsIgnoreCase("UseDeepBlueExpRules")) DEEPBLUE_EXP_RULES = Boolean.valueOf(pValue);
 		        else if (pName.equalsIgnoreCase("SpawnSiegeGuard")) SPAWN_SIEGE_GUARD = Boolean.valueOf(pValue);
 		        
 		        else if (pName.equalsIgnoreCase("AltBuffTime")) ALT_BUFF_TIME = Integer.parseInt(pValue);
 		        else if (pName.equalsIgnoreCase("MaxPAtkSpeed")) MAX_PATK_SPEED = Integer.parseInt(pValue);
 		        else if (pName.equalsIgnoreCase("MaxMAtkSpeed")) MAX_MATK_SPEED = Integer.parseInt(pValue);
 		        else if (pName.equalsIgnoreCase("MinSSParty")) MIN_SS_PARTY = Integer.parseInt(pValue);
 		        else if (pName.equalsIgnoreCase("ValueAA")) VALUE_AA = Integer.parseInt(pValue);
 		        else if (pName.equalsIgnoreCase("MaxItemBuy")) MAX_ITEM_BUY = Integer.parseInt(pValue);
 		        
 		        else if (pName.equalsIgnoreCase("GMNameColor")) GM_NAME_COLOR = pValue;
 		        else if (pName.equalsIgnoreCase("VIPNameColor")) VIP_NAME_COLOR = pValue;
 		        else if (pName.equalsIgnoreCase("MainLanguage")) MAIN_LANG = pValue;
 		 
 		        else return false; 
 		        return true; 
 		    }	
    
	public static boolean allowL2Walker(L2PcInstance player)
    {
        return (ALLOW_L2WALKER_CLIENT == L2WalkerAllowed.True ||
                (ALLOW_L2WALKER_CLIENT == L2WalkerAllowed.GM && player != null && player.isGM()));
    }
	
	// The function of Say filter 
    public static void filterLoad()
	{
		FILTER_LIST = new ArrayList<String>();
		InputStream fileIn          = Config.class.getResourceAsStream(SAY_FILTER_FILE);
		LineNumberReader lnr = null;
		try
        {
            int i=0;
            String line = null;

			lnr = new LineNumberReader(new InputStreamReader(fileIn,"UTF-8"));

            while ( (line = lnr.readLine()) != null)
            {
				StringTokenizer st = new StringTokenizer(line,"\n\r");
                if (st.hasMoreTokens())
                {
                    String Filter = st.nextToken();
                    FILTER_LIST.add(Filter);

                    i++;
                }
            }

            _log.info("Say Filter: Loaded " + i + " filter words.");

        }
        catch (IOException e1)
        {
            _log.warning("Error reading filter: "+ e1);
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
        
	// it has no instancies
	private Config() {}

	/**
	 * @param string
	 */
	public static void saveHexid(String string)
	{
		saveHexid(string,HEXID_FILE);
	}
	
	/**
	 * @param string
	 */
	public static void saveHexid(String string, String fileName)
	{
		try
        {
            Properties hexSetting    = new Properties();
            File file = new File(fileName);
            file.createNewFile();
            OutputStream out = new FileOutputStream(file);
            hexSetting.setProperty("HexID",string);
			hexSetting.store(out,"the hexID to auth into login");
			out.close();
        }
        catch (Exception e)
        {
            System.out.println("Failed to save hex id to "+fileName+" File.");
            e.printStackTrace();
        }
	}

	private static Config _instance = new Config(); 

	public static Config getInstance()
	{
		return _instance;
	} 
}
