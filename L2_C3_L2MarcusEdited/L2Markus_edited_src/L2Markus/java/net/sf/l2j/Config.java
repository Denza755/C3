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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.util.List;
import java.util.Properties;
import java.util.Map;
import java.util.logging.Logger;

import javolution.util.FastList;
import javolution.util.FastMap;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;

/**
 * This class containce global server configuration.<br>
 * It has static final fields initialized from configuration files.<br>
 * It's initialized at the very begin of startup, and later JIT will optimize
 * away debug/unused code.
 * 
 * @author mkizub
 */
public final class Config 
{
	protected static Logger _log = Logger.getLogger(Config.class.getName());

    /** ������ ������� ���������� ��� ������� OPTIONS ---------------------------------------------------------------------------------------------------------------------------- */

    /** Debug/release mode */
    public static boolean DEBUG;
    /** Enable/disable assertions */
    public static boolean ASSERT;
    /** Enable/disable code 'in progress' */
    public static boolean DEVELOPER;
    /** Set if this server is a test server used for development */
    public static boolean TEST_SERVER;

    public static boolean SHOW_SERVER_VERSION;

    /** For test servers - everybody has admin rights */
    public static boolean EVERYBODY_HAS_ADMIN_RIGHTS;

    // Setting for serverList
    /** Displays [] in front of server name ? */
    public static boolean SERVER_LIST_BRACKET;
    /** Displays a clock next to the server name ? */
    public static boolean SERVER_LIST_CLOCK;
    /** Display test server in the list of servers ? */
    public static boolean SERVER_LIST_TESTSERVER;
    /** Set the server as gm only at startup ? */
    public static boolean SERVER_GMONLY;

    /** Zone Setting */
    public static int ZONE_TOWN;

    /** Global chat state */
    public static String  DEFAULT_GLOBAL_CHAT;
    /** Trade chat state */
    public static String  DEFAULT_TRADE_CHAT;
    /** Global chat level state */
    public static int SHOUT_CHAT_LEVEL;
    /** Trade chat level state */
    public static int TRADE_CHAT_LEVEL;
    	
    /** Default punishment for illegal actions */
    public static int DEFAULT_PUNISH;
    /** Parameter for default punishment */
    public static int DEFAULT_PUNISH_PARAM;

    /** Bypass exploit protection ? */
    public static boolean BYPASS_VALIDATION;

    public static boolean PROTECTED_ENCHANT;

    /** Enforce gameguard query on character login ? */
    public static boolean GAMEGUARD_ENFORCE;
    /** Don't allow player to perform trade,talk with npc and move until gameguard reply received ? */
    public static boolean GAMEGUARD_PROHIBITACTION;

    // Day/Night Status
    /** Force the client to change their day and night status ? */
    public static boolean DAY_STATUS_FORCE_CLIENT_UPDATE;
    /** Time for sunrise */
    public static int     DAY_STATUS_SUN_RISE_AT;
    /** Time for sunset */
    public static int     DAY_STATUS_SUN_SET_AT;
    /** Period in days after which character is deleted */
    public static int     DELETE_DAYS;


    /** Time after which item will auto-destroy */
    public static int     AUTODESTROY_ITEM_AFTER;
    /** Auto-delete invalid quest data ? */
    public static boolean AUTODELETE_INVALID_QUEST_DATA;
    /** Accept precise drop calculation ? */
    public static boolean PRECISE_DROP_CALCULATION;
    /** Accept multi-items drop ? */
    public static boolean MULTIPLE_ITEM_DROP;

    /** Force full item inventory packet to be sent for any item change ?<br>
    * <u><i>Note:</i></u> This can increase network traffic*/
    public static boolean    FORCE_INVENTORY_UPDATE;

    public static boolean    FORCE_COMPLETE_STATUS_UPDATE;

    public static boolean LAZY_CACHE;

    /** Maximum range mobs can randomly go from spawn point */
    public static int MAX_DRIFT_RANGE;

    // random animation interval
    /** Minimal time between 2 animations of a NPC */
    public static int MIN_NPC_ANIMATION;
    /** Maximal time between 2 animations of a NPC */
    public static int MAX_NPC_ANIMATION;

    /** Show L2Monster level and aggro ? */
    public static boolean    SHOW_NPC_LVL;

    /** Activate position recorder ? */
    public static boolean ACTIVATE_POSITION_RECORDER;
    
    /** Allow Discard item ?*/
    public static boolean         ALLOW_DISCARDITEM;
    /** Allow freight ? */
    public static boolean         ALLOW_FREIGHT;
    /** Allow warehouse ? */
    public static boolean         ALLOW_WAREHOUSE;
    /** Allow wear ? (try on in shop) */
    public static boolean 	      ALLOW_WEAR;
    /** Duration of the try on after which items are taken back */
    public static int         	  WEAR_DELAY;
    /** Price of the try on of one item */
    public static int         	  WEAR_PRICE;
    /** Allow lottery ? */
    public static boolean 	      ALLOW_LOTTERY;
    /** Allow race ? */
    public static boolean 	      ALLOW_RACE;
    /** Allow water ? */
    public static boolean	      ALLOW_WATER;
    /** Allow Manor system */
    public static boolean         ALLOW_MANOR;       
    /** Allow rent pet ? */
    public static boolean         ALLOW_RENTPET;
    /** Allow boat ? */
    public static boolean 	      ALLOW_BOAT;

    /** Allow the use of L2Walker client ? */
    public static L2WalkerAllowed ALLOW_L2WALKER_CLIENT;
    /** Auto-ban client that use L2Walker ? */
    public static boolean         AUTOBAN_L2WALKER_ACC;
    /** Revision of L2Walker */
    public static int             L2WALKER_REVISION;

    /** Logging Chat Window */
    public static boolean LOG_CHAT;
    /** Logging Item Window */
    public static boolean LOG_ITEMS;
    /** GM Audit ?*/
    public static boolean GMAUDIT;    

    // Community Board
    public static String COMMUNITY_TYPE;
	public static String BBS_DEFAULT;
	public static boolean SHOW_LEVEL_COMMUNITYBOARD;
	public static boolean SHOW_STATUS_COMMUNITYBOARD;
	public static int NAME_PAGE_SIZE_COMMUNITYBOARD;
	public static int NAME_PER_ROW_COMMUNITYBOARD;

    public static boolean ALLOW_USE_CURSOR_FOR_WALK;

    /** Thread pool size effect */
    public static int THREAD_P_EFFECTS;
    /** Thread pool size general */
    public static int THREAD_P_GENERAL;
    /** Packet max thread */
	public static int GENERAL_PACKET_THREAD_CORE_SIZE;
    public static int URGENT_PACKET_THREAD_CORE_SIZE;
    /** General max thread */
	public static int GENERAL_THREAD_CORE_SIZE;
    /** AI max thread */
	public static int AI_MAX_THREAD;

    public static boolean SERVER_TIME_ON_START;

    /** Time after which a packet is considered as lost */
    public static int             PACKET_LIFETIME;    

   /** ����� ������� ���������� ��� ������� OPTIONS ---------------------------------------------------------------------------------------------------------------------------- */

   /** ������ ������� ���������� ��� ������� SKILLS ----------------------------------------------------------------------------------------------------------    */

    public static boolean CHECK_SKILLS_ON_ENTER;
    /** Alternative game skill learning */
    public static boolean GAME_SKILL_LEARN;
    /** Alternative auto skill learning */
    public static boolean AUTO_LEARN_SKILLS;
    /** Alternative success rate formulas for skills such root/sleep/stun */
    public static String ALT_GAME_SKILL_FORMULAS;
    /** Fast or slow multiply coefficient for skill hit time */
    public static float ALT_GAME_SKILL_HIT_RATE;
    /** Spell Book needed to learn skill */
    public static boolean SP_BOOK_NEEDED;
    /**Add skill Teleport 5 min. */
    public static boolean ADD_TELEPORT_SKILL_5_MIN;
    /** Allowed skills character */
    public static String ALLOWED_SKILLS;
    /** Allowed skills character in game */
    public static FastList<Integer> ALLOWED_SKILLS_LIST = new FastList<Integer>();
    /** Store skills cooltime on char exit/relogin */
    public static boolean STORE_SKILL_COOLTIME;


   /** ����� ������� ���������� ��� ������� SKILLS ----------------------------------------------------------------------------------------------------------    */ 

    /** ������ ������� ���������� ��� ������� server ---------------------------------------------------------------------------------------------------------------------------- */

    /** Hostname of the Game Server */
    public static String GAMESERVER_HOSTNAME;

    /** Game Server ports */
    public static int PORT_GAME;

    /** External Hostname */
    public static String     EXTERNAL_HOSTNAME;    

    /** Internal Hostname */
    public static String     INTERNAL_HOSTNAME;

    /** Game Server login Host */
    public static String     GAME_SERVER_LOGIN_HOST;

    /** Login Server port */
    public static int PORT_LOGIN;

    /** Number of trys of login before ban */
    public static int LOGIN_TRY_BEFORE_BAN;

     public static int MAX_ITEM_IN_PACKET;

    /** Game Server login port */
    public static int        GAME_SERVER_LOGIN_PORT;

    /** Accept new game server ? */
	public static boolean ACCEPT_NEW_GAMESERVER;

    /** Accept alternate ID for server ? */
	public static boolean ACCEPT_ALTERNATE_ID;

    /** Driver to access to database */
    public static String DATABASE_DRIVER;

    /** Path to access to database */
    public static String DATABASE_URL;

    /** Database login */
    public static String DATABASE_LOGIN;

    /** Database password */
    public static String DATABASE_PASSWORD;

    /** Maximum number of connections to the database */
    public static int DATABASE_MAX_CONNECTIONS;

    /** Character name template */
    public static String CNAME_TEMPLATE;
    
    /** Pet name template */
    public static String PET_NAME_TEMPLATE;

    /** Maximum number of characters per account */
    public static int MAX_CHARACTERS_NUMBER_PER_ACCOUNT;

    /** Maximum number of players allowed to play simultaneously on server */
    public static int   MAXIMUM_ONLINE_USERS;

    /** Minimal protocol revision */
    public static int MIN_PROTOCOL_REVISION;

    /** Maximal protocol revision */
    public static int MAX_PROTOCOL_REVISION;    

    /** ����� ������� ���������� ��� ������� server ---------------------------------------------------------------------- */

    /** Hexadecimal ID of the game server */
	public static byte[] HEX_ID;

    /** ID for request to the server */
	public static int REQUEST_ID;
	public static boolean RESERVE_HOST_ON_LOGIN = false;


    
    /** Accept auto-loot ? */
    public static boolean AUTO_LOOT;




    /** Alternative game crafting */
    public static boolean ALT_GAME_CREATION;
    /** Alternative game crafting speed mutiplier - default 0 (fastest but still not instant) */
    public static double ALT_GAME_CREATION_SPEED;
    /** Alternative game crafting XP rate multiplier - default 1*/
    public static double ALT_GAME_CREATION_XP_RATE;
    /** Alternative game crafting SP rate multiplier - default 1*/
    public static double ALT_GAME_CREATION_SP_RATE;

    public static boolean	ALT_BLACKSMITH_USE_RECIPES;

    public static int ALT_MINIMUM_FALL_HEIGHT;
    
    
   
    /** Cancel attack bow by hit */
    public static boolean ALT_GAME_CANCEL_BOW;
    /** Cancel cast by hit */
    public static boolean ALT_GAME_CANCEL_CAST;

    /** Alternative game - use tiredness, instead of CP */
    public static boolean ALT_GAME_TIREDNESS;
    public static int ALT_PARTY_RANGE;
    public static int ALT_PARTY_RANGE2;    

    /** Alternative shield defence */
    public static boolean ALT_GAME_SHIELD_BLOCKS;

    /** Alternative game mob ATTACK AI */
    public static boolean ALT_GAME_MOB_ATTACK_AI;
    public static boolean ALT_MOB_AGRO_IN_PEACEZONE;
    

    
    /** Alternative freight modes - Freights can be withdrawed from any village */
    public static boolean ALT_GAME_FREIGHTS;
    /** Alternative freight modes - Sets the price value for each freightened item */
    public static int ALT_GAME_FREIGHT_PRICE;



    /** Alternative gameing - loss of XP on death */
    public static boolean ALT_GAME_DELEVEL;

    /** Alternative gameing - magic dmg failures */
    public static boolean ALT_GAME_MAGICFAILURES;



    
    /* ������ ���������� ��� ���� ������� ---------------------------------------------------------------------------------------------------- **/

    /** Alternative gaming - player must be in a castle-owning clan or ally to sign up for Dawn. */
    public static boolean ALT_GAME_REQUIRE_CASTLE_DAWN;
    
    /** Alternative gaming - allow clan-based castle ownage check rather than ally-based. */
    public static boolean ALT_GAME_REQUIRE_CLAN_CASTLE;

    /** Festival Manager start time. */
    public static long ALT_FESTIVAL_MANAGER_START;

    /** Festival Length */
    public static long ALT_FESTIVAL_LENGTH;

    /** Festival Cycle Length */
    public static long ALT_FESTIVAL_CYCLE_LENGTH;

    /** Festival First Spawn */
    public static long ALT_FESTIVAL_FIRST_SPAWN;

    /** Festival First Swarm */
    public static long ALT_FESTIVAL_FIRST_SWARM;

    /** Festival Second Spawn */
    public static long ALT_FESTIVAL_SECOND_SPAWN;

    /** Festival Second Swarm */
    public static long ALT_FESTIVAL_SECOND_SWARM;

    /** Festival Chest Spawn */
    public static long ALT_FESTIVAL_CHEST_SPAWN;

    /** Minimum number of player to participate in SevenSigns Festival */
    public static int ALT_FESTIVAL_MIN_PLAYER;

    /** Maximum of player contrib during Festival */
    public static int ALT_MAXIMUM_PLAYER_CONTRIB;

    /* ����� ������� ���������� ���� ������� -------------------------------------------------------------------------------------------------- **/

    /* ������ ������� ���������� ���� ������� -------------------------------------------------------------------------------------------------- **/

    /** Alternative gaming - allow free teleporting around the world. */
    public static boolean ALT_GAME_FREE_TELEPORT;
    
    /** View npc stats/drop by shift-cliking it for nongm-players */
    public static boolean ALT_GAME_VIEWNPC;

    /** Number of members needed to request a clan war */
    public static int ALT_CLAN_MEMBERS_FOR_WAR;
    
    /** Number of days before joining a new clan */
    public static int ALT_CLAN_JOIN_DAYS;
    /** Number of days before creating a new clan */
    public static int ALT_CLAN_CREATE_DAYS;
    
    /** Alternative gaming - all new characters always are newbies. */
    public static boolean ALT_GAME_NEW_CHAR_ALWAYS_IS_NEWBIE;
    
    /** Enable Rate Hp  */
    public static boolean ENABLE_RATE_HP;

    /** Manor Refresh Starting time */
    public static int ALT_MANOR_REFRESH_TIME;

    /** Manor Refresh Min */
    public static int ALT_MANOR_REFRESH_MIN;

    /** Manor Next Period Approve Starting time */
    public static int ALT_MANOR_APPROVE_TIME;

    /** Manor Next Period Approve Min */
    public static int ALT_MANOR_APPROVE_MIN;

    /** Manor Maintenance Time */
    public static int ALT_MANOR_MAINTENANCE_PERIOD;

    /** Manor Save All Actions */
    public static boolean ALT_MANOR_SAVE_ALL_ACTIONS;

    /** Manor Save Period Rate */
    public static int ALT_MANOR_SAVE_PERIOD_RATE;

    /** Initial Lottery prize */
    public static int ALT_LOTTERY_PRIZE;
    
    /** Lottery Ticket Price */
    public static int ALT_LOTTERY_TICKET_PRICE;
    
    /** What part of jackpot amount should receive characters who pick 5 wining numbers */
    public static float ALT_LOTTERY_5_NUMBER_RATE;
    
    /** What part of jackpot amount should receive characters who pick 4 wining numbers */
    public static float ALT_LOTTERY_4_NUMBER_RATE;
    
    /** What part of jackpot amount should receive characters who pick 3 wining numbers */
    public static float ALT_LOTTERY_3_NUMBER_RATE;
    
    /** How much adena receive characters who pick two or less of the winning number */
    public static int ALT_LOTTERY_2_AND_1_NUMBER_PRIZE;

    /* Epic Boss Config*/
    public static boolean EPIC_FLY;    



    /* ������ ������ ��������� GMAccess Level ----------------------------------------------------------------------------------------------- */
    /* **************************************************************************
     * GM CONFIG General GM AccessLevel *
     ************************************************************************* */
    /** General GM access level */
    public static int     GM_ACCESSLEVEL;
    /** General GM Minimal AccessLevel */
    public static int     GM_MIN;
    /** General GM AccessLevel to change config of server*/
    public static int     GM_CAN_CONFIG;    
    /** General GM AccessLevel to change announcements */
    public static int     GM_ANNOUNCE;
    /** General GM AccessLevel can /ban /unban */
    public static int     GM_BAN;
    /** General GM AccessLevel can /ban /unban for chat */
    public static int     GM_BAN_CHAT;
    /** General GM AccessLevel can /create_item and /gmshop */
    public static int     GM_CREATE_ITEM;
    /** General GM AccessLevel can /delete */
    public static int     GM_DELETE;
    /** General GM AccessLevel can /kick /disconnect */
    public static int     GM_KICK;
    /** General GM AccessLevel for access to GMMenu */
    public static int     GM_MENU;
    /** General GM AccessLevel to use god mode command */
    public static int     GM_GODMODE;
    /** General GM AccessLevel with character edit rights */
    public static int     GM_CHAR_EDIT;
    /** General GM AccessLevel with edit rights for other characters */
    public static int     GM_CHAR_EDIT_OTHER;
    /** General GM AccessLevel with character view rights */
    public static int     GM_CHAR_VIEW;
    /** General GM AccessLevel with NPC edit rights */
    public static int     GM_NPC_EDIT;
    public static int     GM_NPC_VIEW;
    /** General GM AccessLevel to teleport to any location */
    public static int     GM_TELEPORT;
    /** General GM AccessLevel to teleport character to any location */
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
    public static int     GM_CACHE;
    /** General GM AccessLevel to use test&st commands */
    public static int     GM_TALK_BLOCK;
    public static int     GM_TEST;
    /** Disable transaction on AccessLevel **/
    public static boolean GM_DISABLE_TRANSACTION;
    /** GM transactions disabled from this range */
    public static int     GM_TRANSACTION_MIN;
    /** GM transactions disabled to this range */
    public static int     GM_TRANSACTION_MAX;
    /** Minimum level to don't take aggro */
    public static int     GM_DONT_TAKE_AGGRO;       
    /** Minimum level to don't give Exp/Sp in party */
    public static int     GM_DONT_TAKE_EXPSP;
    
    public static int GM_REPAIR = 75;
    /* ����� ������ ��������� GMAccess Level ----------------------------------------------------------------------------------------------- */

    /* ����� ������� ���������� ������ ---------------------------------------------------------------------------------------------------------------------------------------**/
    
    /* Rate control */
    /** Rate for eXperience Point rewards */
    public static float   RATE_XP;
    /** Rate for Skill Point rewards */
    public static float   RATE_SP;
    /** Rate for party eXperience Point rewards */
    public static float   RATE_PARTY_XP;
    /** Rate for party Skill Point rewards */
    public static float   RATE_PARTY_SP;
    /** Rate for Quest rewards (XP and SP) */
    public static float   RATE_QUESTS_REWARD;
    /** Rate for drop adena */
    public static float   RATE_DROP_ADENA;
    /** Rate for cost of consumable */
    public static float   RATE_CONSUMABLE_COST;
    /** Rate for dropped items */
    public static float   RATE_DROP_ITEMS;
    /** Rate for dropped items by Raid Bosses */
    public static float	  RATE_DROP_ITEMS_BY_RAID;
    /** Rate for spoiled items */
    public static float   RATE_DROP_SPOIL;
    /** Rate for manored items */
    public static int     RATE_DROP_MANOR;    
    /** Rate for quest items */
    public static float   RATE_DROP_QUEST;
    /** Rate for karma and experience lose */
    public static float   RATE_KARMA_EXP_LOST;
    /** Rate siege guards prices */
    public static float   RATE_SIEGE_GUARDS_PRICE;

	public static boolean	USE_PREMIUMSERVICE;
	public static float		PREMIUM_RATE_XP;
	public static float		PREMIUM_RATE_SP;
	public static float		PREMIUM_RATE_DROP_ADENA;
	public static float		PREMIUM_RATE_DROP_SPOIL;
	public static float		PREMIUM_RATE_DROP_ITEMS;
	public static float		PREMIUM_RATE_DROP_QUEST;


    /*Alternative Xp/Sp rewards, if not 0, then calculated as 2^((mob.level-player.level) / coef),
    * A few examples for "AltGameExponentXp = 5." and "AltGameExponentSp = 3."
    * diff = 0 (player and mob has the same level), XP bonus rate = 1, SP bonus rate = 1
    * diff = 3 (mob is 3 levels above), XP bonus rate = 1.52, SP bonus rate = 2
    * diff = 5 (mob is 5 levels above), XP bonus rate = 2, SP bonus rate = 3.17
    * diff = -8 (mob is 8 levels below), XP bonus rate = 0.4, SP bonus rate = 0.16 */
    /** Alternative eXperience Point rewards */
    public static float   ALT_GAME_EXPONENT_XP;
    /** Alternative Spirit Point rewards */
    public static float   ALT_GAME_EXPONENT_SP;

    // Player Drop Rate control
    /** Limit for player drop */
    public static int   PLAYER_DROP_LIMIT;
    /** Rate for drop */
    public static int   PLAYER_RATE_DROP;
    /** Rate for player's item drop */
    public static int   PLAYER_RATE_DROP_ITEM;
    /** Rate for player's equipment drop */
    public static int   PLAYER_RATE_DROP_EQUIP;
    /** Rate for player's equipment and weapon drop */
    public static int   PLAYER_RATE_DROP_EQUIP_WEAPON;

    // Karma Drop Rate control
    /** Karma drop limit */
    public static int   KARMA_DROP_LIMIT;
    /** Karma drop rate */
    public static int   KARMA_RATE_DROP;
    /** Karma drop rate for item */
    public static int   KARMA_RATE_DROP_ITEM;
    /** Karma drop rate for equipment */
    public static int   KARMA_RATE_DROP_EQUIP;
    /** Karma drop rate for equipment and weapon */
    public static int   KARMA_RATE_DROP_EQUIP_WEAPON;


    /** Все мобы не являющиеся рейдами спавнятся в двойном количестве */
    public static boolean ALT_DOUBLE_SPAWN;


    // Pet Rates (Multipliers)
    /** Rate for experience rewards of the pet */
	public static float         PET_XP_RATE;
    /** Rate for food consumption of the pet */
 	public static int           PET_FOOD_RATE;
    public static float         SINEATER_XP_RATE;

    // Spoil Rates
    /** Allow spoil on lower level mobs than the character */
    public static boolean CAN_SPOIL_LOWER_LEVEL_MOBS;
    /** Allow delevel and spoil mob ? */
    public static boolean CAN_DELEVEL_AND_SPOIL_MOBS;
    /** Maximum level difference between player and mob level */
    public static float   MAXIMUM_PLAYER_AND_MOB_LEVEL_DIFFERENCE;
    /** Base rate for spoil */
    public static float   BASE_SPOIL_RATE;
    /** Minimum spoil rate */
    public static float   MINIMUM_SPOIL_RATE;
    /** Maximum level difference between player and spoil level to allow before decreasing spoil chance */
    public static float   SPOIL_LEVEL_DIFFERENCE_LIMIT;
    /** Spoil level multiplier */
    public static float   SPOIL_LEVEL_DIFFERENCE_MULTIPLIER;
    /** Last level spoil learned */
    public static int     LAST_LEVEL_SPOIL_IS_LEARNED;

    /* ����� ������� ���������� ������ ----------------------------------------------------------------------------------------------------------------------------------------**/

    
    /** Datapack root directory */
    public static File    DATAPACK_ROOT;



    

    /** Enumeration describing values for Allowing the use of L2Walker client */
    public static enum L2WalkerAllowed
    {
        True,
        False,
        GM
    }


    // Configuration files
    /** Properties file that allows selection of new Classes for storage of World Objects. 
     * <br>This may help servers with large amounts of players recieving error messages related to 
     * the <i>L2ObjectHashMap</i> and <i>L2ObejctHashSet</i> classes.*/
    /** Properties file for game server (connection and ingame) configurations */
    public static final String  CONFIGURATION_FILE          = "./config/server.properties";
    /** Properties file for game server options */
    public static final String  OPTIONS_FILE                = "./config/options.properties";
    /** Properties file for login server configurations */
    public static final String  LOGIN_CONFIGURATION_FILE    = "./config/loginserver.properties";
    /** Properties file for the ID factory */
    public static final String  ID_CONFIG_FILE				= "./config/idfactory.properties";
    /** Properties file for other configurations */
    public static final String  OTHER_CONFIG_FILE			= "./config/other.properties";
    /** Properties file for rates configurations */
    public static final String  RATES_CONFIG_FILE			= "./config/rates.properties";
    /** Properties file for alternative configuration */
    public static final String  CHARACTER_SETTINGS_FILE		= "./config/character.properties";
    /** Properties file for PVP configurations */
    public static final String  PVP_CONFIG_FILE				= "./config/pvp.properties";
    /** Properties file for GM access configurations */
    public static final String  GM_ACCESS_FILE				= "./config/GMAccess.properties";
    /** Properties file for telnet configuration */
    public static final String  TELNET_FILE					= "./config/telnet.properties";
    /** Properties file for siege configuration */
    public static final String  SIEGE_CONFIGURATION_FILE	= "./config/siege.properties";
    /** XML file for banned IP */
    public static final String  BANNED_IP_XML				= "./config/banned.xml";
    /** Text file containing hexadecimal value of server ID */
    public static final String  HEXID_FILE					= "./config/hexid.txt";
    /** Properties file for alternative configure GM commands access level.<br>
     * Note that this file only read if "AltPrivilegesAdmin = True" */
    public static final String  COMMAND_PRIVILEGES_FILE     = "./config/command-privileges.properties";
    /** Properties file for AI configurations */
    public static final String  AI_FILE     				= "./config/ai.properties";
    /** Properties file for 7 Signs Festival */
    public static final String  SEVENSIGNS_FILE             = "./config/sevensigns.properties";
    /** Geodata config file */
    public static final String  GEODATA_CONFIG_FILE         = "./config/geodata.properties";
    /** Clan Hall config file */
    public static final String  CLANHALL_CONFIG_FILE        = "./config/clanhall.properties";
    /** Chat config file */
    public static final String  CHAT_CONFIG_FILE            = "./config/chat.properties";
    /** Enchant config file */
    public static final String  ENCHANT_CONFIG_FILE         = "./config/enchant.properties";
    /** Chat config file */
    public static final String  SKILLS_CONFIG_FILE          = "./config/skills.properties";
    /** Chat config file */
    public static final String  PTS_CONFIG_FILE             = "./config/pts.properties";
    /** Aio config file */
    public static final String  AIO_CONFIG_FILE             = "./config/aio.properties";



    public static boolean CHECK_KNOWN;
    


    public static int        NEW_NODE_ID;
    public static int        SELECTED_NODE_ID;
    public static int        LINKED_NODE_ID;
    public static String     NEW_NODE_TYPE;

    public static int INVENTORY_MAXIMUM_PET;
    


    /** Allow use Event Managers for change occupation ?*/
	public static boolean    ALLOW_CLASS_MASTERS;
    /** Time between 2 updates of IP */
	public static int        IP_UPDATE_TIME;

    /** Crafting Enabled? */
    public static boolean IS_CRAFTING_ENABLED;
    

    /** ������ ������� ��� ��������� -------------------------------------------------------------------------------------------------------------------------------------------- */
    
    // Karma System Variables
    /** Minimum karma gain/loss */
    public static int     KARMA_MIN_KARMA;
    
    /** Maximum karma gain/loss */
    public static int     KARMA_MAX_KARMA;

    /** Number to divide the xp recieved by, to calculate karma lost on xp gain/lost */
    public static int     KARMA_XP_DIVIDER;

    /** The Minimum Karma lost if 0 karma is to be removed */
    public static int     KARMA_LOST_BASE;

    /** Can a GM drop item ? */
    public static boolean KARMA_DROP_GM;

    /** Should award a pvp point for killing a player with karma ? */
    public static boolean KARMA_AWARD_PK_KILL;

    /** List of pet items that cannot be dropped (seperated by ",") when PVP */
    public static String        KARMA_NONDROPPABLE_PET_ITEMS;
    
    /** List of items that cannot be dropped (seperated by ",") when PVP*/
    public static String        KARMA_NONDROPPABLE_ITEMS;

    /** Minimum PK required to drop */
    public static int     KARMA_PK_LIMIT;

    /** List of pet items that cannot be dropped when PVP */
    public static List<Integer> KARMA_LIST_NONDROPPABLE_PET_ITEMS   = new FastList<Integer>();

    /** List of items that cannot be dropped when PVP */
    public static List<Integer> KARMA_LIST_NONDROPPABLE_ITEMS       = new FastList<Integer>();

    /** Duration (in ms) while a player stay in PVP mode after hitting an innocent */
    public static int PVP_NORMAL_TIME;

    /** Duration (in ms) while a player stay in PVP mode after hitting a purple player */
    public static int PVP_PVP_TIME;
    

    /** ����� ������� ��� ��������� --------------------------------------------------------------------------------------------------------------------------------------------- */

    

    /** List of items that cannot be dropped (seperated by ",") */
    public static String        NONDROPPABLE_ITEMS;
    /** List of items that cannot be dropped */
    public static List<Integer> LIST_NONDROPPABLE_ITEMS       = new FastList<Integer>();

    // Karma Punishment
    /** Allow player with karma to be killed in peace zone ? */
    public static boolean ALT_GAME_KARMA_PLAYER_CAN_BE_KILLED_IN_PEACEZONE;
    /** Allow player with karma to shop ? */
    public static boolean ALT_GAME_KARMA_PLAYER_CAN_SHOP;
    /** Allow player with karma to use teleport ? */
    public static boolean ALT_GAME_KARMA_PLAYER_CAN_TELEPORT;
    /** Allow player with karma to trade ? */
    public static boolean ALT_GAME_KARMA_PLAYER_CAN_TRADE;
    /** Allow player with karma to use warehouse ?*/
    public static boolean ALT_GAME_KARMA_PLAYER_CAN_USE_WAREHOUSE;


    
    // Packet information
    /** Count the amount of packets per minute ? */  
	public static boolean  COUNT_PACKETS           = false;
    /** Dump packet count ? */
	public static boolean  DUMP_PACKET_COUNTS      = false;
    /** Time interval between 2 dumps */
    public static int      DUMP_INTERVAL_SECONDS   = 60;
    
    /** Enumeration for type of ID Factory */
	public static enum IdFactoryType
    {
	    Compaction,
        BitSet,
        Stack
    }
    
    /** ID Factory type */
    public static IdFactoryType IDFACTORY_TYPE;
    /** Check for bad ID ? */
    public static boolean BAD_ID_CHECKING;
	
    /** Enumeration for type of maps object */
    public static enum ObjectMapType
    {
        L2ObjectHashMap,
        WorldObjectMap
    }

    /** Enumeration for type of set object */
    public static enum ObjectSetType
    {
        L2ObjectHashSet,
        WorldObjectSet
    }

    /** Type of map object */
    public static ObjectMapType   MAP_TYPE;
    /** Type of set object */
    public static ObjectSetType   SET_TYPE;
    /* ����� ������� AIO ���������  -------------------------------------------------------------------------------------------------------------------------------------------**/

    public static boolean ENABLE_AIO_SYSTEM;
 	public static boolean ALLOW_AIO_USE_GK;
	public static boolean ALLOW_AIO_USE_CM;
    public static Map<Integer, Integer> AIO_SKILLS;
    /* ����� ������� AIO ���������  -------------------------------------------------------------------------------------------------------------------------------------------**/


    /* ����� ������� OTHER ���������  -------------------------------------------------------------------------------------------------------------------------------------------**/

    /** Amount of adenas when starting a new character */
    public static int STARTING_ADENA;
    public static byte STARTING_LEVEL;
    public static int STARTING_SP;


    // Pets
    /** Speed of Weverns */
    public static int WYVERN_SPEED;
    /** Speed of Striders */
    public static int STRIDER_SPEED;
    /** Allow Wyvern Upgrader ? */
    public static boolean ALLOW_WYVERN_UPGRADER;

    /**
     * Allow lesser effects to be canceled if stronger effects are used when effects of the same stack group are used.<br>
     * New effects that are added will be canceled if they are of lesser priority to the old one.
     */
    public static boolean EFFECT_CANCELING;


    /** Disable the use of guards against agressive monsters ? */
    public static boolean    ALLOW_GUARDS;

    /** Deep Blue Mobs' Drop Rules Enabled */
    public static boolean DEEPBLUE_DROP_RULES;
    public static int     UNSTUCK_INTERVAL;


    // Inventory slots limit
    /** Maximum inventory slots limits for non dwarf characters */
    public static int INVENTORY_MAXIMUM_NO_DWARF;
    /** Maximum inventory slots limits for dwarf characters */
    public static int INVENTORY_MAXIMUM_DWARF;
    /** Maximum inventory slots limits for GM */
    public static int INVENTORY_MAXIMUM_GM;


    // Warehouse slots limits
    /** Maximum inventory slots limits for non dwarf warehouse */
    public static int WAREHOUSE_SLOTS_NO_DWARF;
    /** Maximum inventory slots limits for dwarf warehouse */
    public static int WAREHOUSE_SLOTS_DWARF;
    /** Maximum inventory slots limits for clan warehouse */
    public static int WAREHOUSE_SLOTS_CLAN;
    /** Maximum inventory slots limits for freight */
    public static int FREIGHT_SLOTS;


    /** Enchant Setting start ----------------------------------------------------------------------------------------- */

    //============================================================
	// Enchant map
	public static FastMap<Integer, Integer> NORMAL_WEAPON_ENCHANT_LEVEL = new FastMap<Integer, Integer>();
	public static FastMap<Integer, Integer> BLESS_WEAPON_ENCHANT_LEVEL = new FastMap<Integer, Integer>();
	public static FastMap<Integer, Integer> CRYTAL_WEAPON_ENCHANT_LEVEL = new FastMap<Integer, Integer>();

	public static FastMap<Integer, Integer> NORMAL_ARMOR_ENCHANT_LEVEL = new FastMap<Integer, Integer>();
	public static FastMap<Integer, Integer> BLESS_ARMOR_ENCHANT_LEVEL = new FastMap<Integer, Integer>();
	public static FastMap<Integer, Integer> CRYSTAL_ARMOR_ENCHANT_LEVEL = new FastMap<Integer, Integer>();

	public static FastMap<Integer, Integer> NORMAL_JEWELRY_ENCHANT_LEVEL = new FastMap<Integer, Integer>();
	public static FastMap<Integer, Integer> BLESS_JEWELRY_ENCHANT_LEVEL = new FastMap<Integer, Integer>();
	public static FastMap<Integer, Integer> CRYSTAL_JEWELRY_ENCHANT_LEVEL = new FastMap<Integer, Integer>();    

    /** Chance that an item will succesfully be enchanted */
    public static int ENCHANT_CHANCE;
    /** Maximum level of enchantment */
    public static int ENCHANT_MAX;
    /** maximum level of safe enchantment for normal items*/
    public static int ENCHANT_SAFE_MAX;
    /** maximum level of safe enchantment for full body armor*/
    public static int ENCHANT_SAFE_MAX_FULL;

    public static int MAX_ITEM_ENCHANT_KICK;

    /** Enchant Setting end ----------------------------------------------------------------------------------------- */

    // Character multipliers
    /** Multiplier for character HP regeneration */
    public static double  HP_REGEN_MULTIPLIER;
    /** Mutilplier for character MP regeneration */
    public static double  MP_REGEN_MULTIPLIER;
    /** Multiplier for character CP regeneration */
    public static double  CP_REGEN_MULTIPLIER;

    // Raid Boss multipliers
    /** Multiplier for Raid boss HP regeneration */
    public static double   RAID_HP_REGEN_MULTIPLIER;
    /** Mulitplier for Raid boss MP regeneration */
    public static double   RAID_MP_REGEN_MULTIPLIER;
    /** Multiplier for Raid boss defense multiplier */
    public static double   RAID_DEFENCE_MULTIPLIER;

    /** Player Protection control */
    public static int   PLAYER_SPAWN_PROTECTION;

     /** Define Party XP cutoff point method - Possible values: level and percentage */
    public static String  PARTY_XP_CUTOFF_METHOD;
    /** Define the cutoff point value for the "level" method */
    public static int PARTY_XP_CUTOFF_LEVEL;
    /** Define the cutoff point value for the "percentage" method */
    public static double  PARTY_XP_CUTOFF_PERCENT;

    /** Allow randomizing of the respawn point in towns. */
    public static boolean RESPAWN_RANDOM_ENABLED;    

    /** The maximum offset from the base respawn point to allow. */
    public static int RESPAWN_RANDOM_MAX_OFFSET;

    /** Maximum number of available slots for pvt stores (sell/buy) - Dwarves */
    public static int  MAX_PVTSTORE_SLOTS_DWARF;
    /** Maximum number of available slots for pvt stores (sell/buy) - Others */
    public static int  MAX_PVTSTORE_SLOTS_OTHER;
    

    
    /** List of NPCs that rent pets (seperated by ",") */
    public static String        PET_RENT_NPC;
    /** List of NPCs that rent pets */
    public static List<Integer> LIST_PET_RENT_NPC   = new FastList<Integer>();

    public static boolean ANNOUNCE_MAMMON_SPAWN;

    /** Alternative privileges for admin */
    public static boolean ALT_PRIVILEGES_ADMIN;
    /** Alternative secure check privileges */
    public static boolean ALT_PRIVILEGES_SECURE_CHECK;
    /** Alternative default level for privileges */
    public static int ALT_PRIVILEGES_DEFAULT_LEVEL;

    /* L2PTS Config */
    public static boolean AUTO_SOUL_SHOT;

    public static boolean SHOW_MINI_MAP;

    public static boolean EQUIPABLE_CHARACTER;

    public static boolean AUTO_SELECT_CHAR;


    /** Enable colored name for GM ? */
    public static boolean GM_NAME_COLOR_ENABLED;
    /** Color of GM name */
    public static int     GM_NAME_COLOR;
    /** Color of admin name */
    public static int     ADMIN_NAME_COLOR;
    /** Set the GM invulnerable at startup ? */
    public static boolean GM_STARTUP_INVULNERABLE;
    /** Set the GM invisible at startup ? */
    public static boolean GM_STARTUP_INVISIBLE;
    /** Set silence to GM at startup ? */
    public static boolean GM_STARTUP_SILENCE;
    /** Add GM in the GM list at startup ? */
    public static boolean GM_STARTUP_AUTO_LIST;

    /** Jail config **/
    public static boolean JAIL_IS_PVP;
    public static boolean JAIL_DISABLE_CHAT;
    
    /* ����� ������� OTHER ���������  ------------------------------------------------------------------------------------------------------------------------------------------- **/












    
    /** ������ ���������� ��� �������. */
    public static boolean IS_TELNET_ENABLED;




    /** Percent CP is restore on respawn */
    public static double  RESPAWN_RESTORE_CP;
    /** Percent HP is restore on respawn */
    public static double  RESPAWN_RESTORE_HP;
    /** Percent MP is restore on respawn */
    public static double  RESPAWN_RESTORE_MP;


    


    /** Show licence or not just after login (if false, will directly go to the Server List */
	public static boolean SHOW_LICENCE;
	/** Force GameGuard authorization in loginserver */
	public static boolean FORCE_GGAUTH;

    

    
    public static int MINIMUM_UPDATE_DISTANCE;
    public static int KNOWNLIST_FORGET_DELAY;
    public static int MINIMUN_UPDATE_TIME;
    


    

    
    /** Allow petition ? */
    public static boolean PETITIONING_ALLOWED;
    /** Maximum number of petitions per player */
    public static int     MAX_PETITIONS_PER_PLAYER;
    /** Maximum number of petitions pending */
    public static int     MAX_PETITIONS_PENDING;
    
    // Alternative AI setting
    /** Enable AI ? */
    public static boolean AI_ENABLED;
    /** AI default class */
    public static String  AI_DEFAULT_CLASS;
    


    
	/** Allow auto-create account ? */
    public static boolean AUTO_CREATE_ACCOUNTS;
	

	public static boolean FLOOD_PROTECTION;
	public static int     FAST_CONNECTION_LIMIT;
	public static int     NORMAL_CONNECTION_TIME;
	public static int     FAST_CONNECTION_TIME;
	public static int     MAX_CONNECTION_PER_IP;
    
    /** Recipebook limits */
    public static int DWARF_RECIPE_LIMIT;
    
    /** Geodata **/
    public static boolean     GRIDS_ALWAYS_ON;
    public static int         GRID_NEIGHBOR_TURNON_TIME;
    public static int         GRID_NEIGHBOR_TURNOFF_TIME;
    public static int		  GEODATA;
    public static boolean	  FORCE_GEODATA;
    public static int         COORD_SYNCHRONIZE;
    public static boolean     ACCEPT_GEOEDITOR_CONN;
	public static enum CorrectSpawnsZ
	{
		TOWN, MONSTER, ALL, NONE
	}
	public static CorrectSpawnsZ	GEO_CORRECT_Z;    


    /** Clan Hall function related configs*/
    public static long CH_TELE_FEE_RATIO;
    public static int CH_TELE1_FEE;
    public static int CH_TELE2_FEE;
    public static long CH_ITEM_FEE_RATIO;
    public static int CH_ITEM1_FEE;
    public static int CH_ITEM2_FEE;
    public static int CH_ITEM3_FEE;
    public static long CH_MPREG_FEE_RATIO;
    public static int CH_MPREG1_FEE;
    public static int CH_MPREG2_FEE;
    public static int CH_MPREG3_FEE;
    public static int CH_MPREG4_FEE;
    public static int CH_MPREG5_FEE;
    public static long CH_HPREG_FEE_RATIO;
    public static int CH_HPREG1_FEE;
    public static int CH_HPREG2_FEE;
    public static int CH_HPREG3_FEE;
    public static int CH_HPREG4_FEE;
    public static int CH_HPREG5_FEE;
    public static int CH_HPREG6_FEE;
    public static int CH_HPREG7_FEE;
    public static int CH_HPREG8_FEE;
    public static int CH_HPREG9_FEE;
    public static int CH_HPREG10_FEE;
    public static int CH_HPREG11_FEE;
    public static int CH_HPREG12_FEE;
    public static int CH_HPREG13_FEE;
    public static long CH_EXPREG_FEE_RATIO;
    public static int CH_EXPREG1_FEE;
    public static int CH_EXPREG2_FEE;
    public static int CH_EXPREG3_FEE;
    public static int CH_EXPREG4_FEE;
    public static int CH_EXPREG5_FEE;
    public static int CH_EXPREG6_FEE;
    public static int CH_EXPREG7_FEE;
    public static long CH_SUPPORT_FEE_RATIO;
    public static int CH_SUPPORT1_FEE;
    public static int CH_SUPPORT2_FEE;
    public static int CH_SUPPORT3_FEE;
    public static int CH_SUPPORT4_FEE;
    public static int CH_SUPPORT5_FEE;
    public static int CH_SUPPORT6_FEE;
    public static int CH_SUPPORT7_FEE;
    public static int CH_SUPPORT8_FEE;
    public static long CH_CURTAIN_FEE_RATIO;
    public static int CH_CURTAIN1_FEE;
    public static int CH_CURTAIN2_FEE;
    public static long CH_FRONT_FEE_RATIO;
    public static int CH_FRONT1_FEE;
    public static int CH_FRONT2_FEE;

	public static int DEVASTATED_DAY;
	public static int DEVASTATED_HOUR;
	public static int DEVASTATED_MINUTES;
	public static int PARTISAN_DAY;
	public static int PARTISAN_HOUR;
	public static int PARTISAN_MINUTES;    


    public static int		FLOODPROTECTOR_INITIALSIZE;

 /** -------Configuration config file start --------------------------------------------------------------------------------------------------------------------------------------- */
    /**
     * This class initializes all global variables for configuration.<br>
     * If key doesn't appear in properties file, a default value is setting on by this class.
     * @see CONFIGURATION_FILE (propertie file) for configuring your server. 
     */
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
				
                GAMESERVER_HOSTNAME     = serverSettings.getProperty("GameserverHostname");
                PORT_GAME               = Integer.parseInt(serverSettings.getProperty("GameserverPort", "7777"));

                EXTERNAL_HOSTNAME       = serverSettings.getProperty("ExternalHostname", "192.168.1.3");
                INTERNAL_HOSTNAME       = serverSettings.getProperty("InternalHostname", "192.168.1.3");
                
                PORT_LOGIN              = Integer.parseInt(serverSettings.getProperty("LoginserverPort", "2106"));
                LOGIN_TRY_BEFORE_BAN    = Integer.parseInt(serverSettings.getProperty("LoginTryBeforeBan", "10"));

                GAME_SERVER_LOGIN_PORT  = Integer.parseInt(serverSettings.getProperty("LoginPort","9014"));
                GAME_SERVER_LOGIN_HOST  = serverSettings.getProperty("LoginHost","127.0.0.1");

                REQUEST_ID              = Integer.parseInt(serverSettings.getProperty("RequestServerID","0"));
                ACCEPT_ALTERNATE_ID     = Boolean.parseBoolean(serverSettings.getProperty("AcceptAlternateID","False"));

                DATABASE_DRIVER             = serverSettings.getProperty("Driver", "com.mysql.jdbc.Driver");
                DATABASE_URL                = serverSettings.getProperty("URL", "jdbc:mysql://localhost/l2markus");
                DATABASE_LOGIN              = serverSettings.getProperty("Login", "root");
                DATABASE_PASSWORD           = serverSettings.getProperty("Password", "root");
                DATABASE_MAX_CONNECTIONS    = Integer.parseInt(serverSettings.getProperty("MaximumDbConnections", "10"));

                DATAPACK_ROOT           = new File(serverSettings.getProperty("DatapackRoot", ".")).getCanonicalFile();

                CNAME_TEMPLATE          = serverSettings.getProperty("CnameTemplate", ".*");
                PET_NAME_TEMPLATE       = serverSettings.getProperty("PetNameTemplate", ".*");

                MAX_CHARACTERS_NUMBER_PER_ACCOUNT = Integer.parseInt(serverSettings.getProperty("CharMaxNumber", "7"));
                MAXIMUM_ONLINE_USERS        = Integer.parseInt(serverSettings.getProperty("MaximumOnlineUsers", "1000"));
               
                MIN_PROTOCOL_REVISION   = Integer.parseInt(serverSettings.getProperty("MinProtocolRevision", "560"));
                MAX_PROTOCOL_REVISION   = Integer.parseInt(serverSettings.getProperty("MaxProtocolRevision", "560"));

                SHOW_SERVER_VERSION      = Boolean.valueOf(serverSettings.getProperty("ShowServerVersion", "False"));
                
                if (MIN_PROTOCOL_REVISION > MAX_PROTOCOL_REVISION)
                {
                    throw new Error("MinProtocolRevision is bigger than MaxProtocolRevision in server configuration file.");
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
                throw new Error("Failed to Load "+CONFIGURATION_FILE+" File.");
            }

 /** -------Configuration config file end --------------------------------------------------------------------------------------------------------------------------------------- */

 /** -------Options config file start --------------------------------------------------------------------------------------------------------------------------------------- */


            try 
            {
                Properties optionsSettings    = new Properties();
                InputStream is               = new FileInputStream(new File(OPTIONS_FILE));
                optionsSettings.load(is);
                is.close();


                             
                DEBUG                           = Boolean.parseBoolean(optionsSettings.getProperty("Debug", "false"));
                ASSERT                          = Boolean.parseBoolean(optionsSettings.getProperty("Assert", "false"));
                DEVELOPER                       = Boolean.parseBoolean(optionsSettings.getProperty("Developer", "false"));
                TEST_SERVER                     = Boolean.parseBoolean(optionsSettings.getProperty("TestServer", "false"));
                SERVER_LIST_TESTSERVER          = Boolean.parseBoolean(optionsSettings.getProperty("TestServer", "false"));

                EVERYBODY_HAS_ADMIN_RIGHTS      = Boolean.parseBoolean(optionsSettings.getProperty("EverybodyHasAdminRights", "false"));
                             
                SERVER_LIST_BRACKET             = Boolean.valueOf(optionsSettings.getProperty("ServerListBrackets", "false"));
                SERVER_LIST_CLOCK               = Boolean.valueOf(optionsSettings.getProperty("ServerListClock", "false"));
                SERVER_GMONLY                   = Boolean.valueOf(optionsSettings.getProperty("ServerGMOnly", "false"));
                FLOODPROTECTOR_INITIALSIZE		= Integer.parseInt(optionsSettings.getProperty("FloodProtectorInitialSize", "50"));
                AUTODESTROY_ITEM_AFTER          = Integer.parseInt(optionsSettings.getProperty("AutoDestroyDroppedItemAfter", "0"));
                AUTODELETE_INVALID_QUEST_DATA   = Boolean.valueOf(optionsSettings.getProperty("AutoDeleteInvalidQuestData", "False"));
                PRECISE_DROP_CALCULATION        = Boolean.valueOf(optionsSettings.getProperty("PreciseDropCalculation", "True"));
                MULTIPLE_ITEM_DROP              = Boolean.valueOf(optionsSettings.getProperty("MultipleItemDrop", "True"));            
             
                ALLOW_WAREHOUSE                 = Boolean.valueOf(optionsSettings.getProperty("AllowWarehouse", "True"));
                ALLOW_FREIGHT                   = Boolean.valueOf(optionsSettings.getProperty("AllowFreight", "True"));
                ALLOW_WEAR                      = Boolean.valueOf(optionsSettings.getProperty("AllowWear", "False"));
                WEAR_DELAY                      = Integer.parseInt(optionsSettings.getProperty("WearDelay", "5"));
                WEAR_PRICE                      = Integer.parseInt(optionsSettings.getProperty("WearPrice", "10"));
                ALLOW_LOTTERY                   = Boolean.valueOf(optionsSettings.getProperty("AllowLottery", "False"));
                ALLOW_RACE                      = Boolean.valueOf(optionsSettings.getProperty("AllowRace", "False"));
                ALLOW_WATER						= Boolean.parseBoolean(optionsSettings.getProperty("AllowWater", "False"));
                ALLOW_MANOR                     = Boolean.parseBoolean(optionsSettings.getProperty("AllowManor", "False"));
                FLOODPROTECTOR_INITIALSIZE		= Integer.parseInt(optionsSettings.getProperty("FloodProtectorInitialSize", "50"));
                ALLOW_RENTPET                   = Boolean.valueOf(optionsSettings.getProperty("AllowRentPet", "False"));
                ALLOW_DISCARDITEM               = Boolean.valueOf(optionsSettings.getProperty("AllowDiscardItem", "True"));
                ALLOW_BOAT                      = Boolean.valueOf(optionsSettings.getProperty("AllowBoat", "False"));
                
                ALLOW_L2WALKER_CLIENT           = L2WalkerAllowed.valueOf(optionsSettings.getProperty("AllowL2Walker", "False"));
                L2WALKER_REVISION               = Integer.parseInt(optionsSettings.getProperty("L2WalkerRevision", "537"));
                AUTOBAN_L2WALKER_ACC            = Boolean.valueOf(optionsSettings.getProperty("AutobanL2WalkerAcc", "False"));

                ALLOW_USE_CURSOR_FOR_WALK       = Boolean.valueOf(optionsSettings.getProperty("AllowUseCursorForWalk", "False"));
                
                ACTIVATE_POSITION_RECORDER      = Boolean.valueOf(optionsSettings.getProperty("ActivatePositionRecorder", "False"));

                LOG_ITEMS                       = Boolean.valueOf(optionsSettings.getProperty("LogItems", "false"));
                             
                GMAUDIT                         = Boolean.valueOf(optionsSettings.getProperty("GMAudit", "False"));

		    	COMMUNITY_TYPE                  = optionsSettings.getProperty("CommunityType", "old").toLowerCase();
			    BBS_DEFAULT                     = optionsSettings.getProperty("BBSDefault", "_bbshome");
			    SHOW_LEVEL_COMMUNITYBOARD       = Boolean.valueOf(optionsSettings.getProperty("ShowLevelOnCommunityBoard", "False"));
			    SHOW_STATUS_COMMUNITYBOARD      = Boolean.valueOf(optionsSettings.getProperty("ShowStatusOnCommunityBoard", "True"));
			    NAME_PAGE_SIZE_COMMUNITYBOARD   = Integer.parseInt(optionsSettings.getProperty("NamePageSizeOnCommunityBoard", "50"));
			    NAME_PER_ROW_COMMUNITYBOARD     = Integer.parseInt(optionsSettings.getProperty("NamePerRowOnCommunityBoard", "5"));
                             
                ZONE_TOWN                       = Integer.parseInt(optionsSettings.getProperty("ZoneTown", "0"));
                             
                MAX_DRIFT_RANGE                 = Integer.parseInt(optionsSettings.getProperty("MaxDriftRange", "300"));

                MIN_NPC_ANIMATION               = Integer.parseInt(optionsSettings.getProperty("MinNPCAnimation", "0"));
                MAX_NPC_ANIMATION               = Integer.parseInt(optionsSettings.getProperty("MaxNPCAnimation", "0"));
                SHOW_NPC_LVL					= Boolean.valueOf(optionsSettings.getProperty("ShowNpcLevel", "False"));

                FORCE_INVENTORY_UPDATE          = Boolean.valueOf(optionsSettings.getProperty("ForceInventoryUpdate", "False"));

                FORCE_COMPLETE_STATUS_UPDATE    = Boolean.valueOf(optionsSettings.getProperty("ForceCompletePlayerStatusUpdate", "true"));
                             
                DAY_STATUS_SUN_RISE_AT          = Integer.parseInt(optionsSettings.getProperty("DayStatusSunRiseAt", "6"));
                DAY_STATUS_SUN_SET_AT           = Integer.parseInt(optionsSettings.getProperty("DayStatusSunSetAt", "18"));
                DAY_STATUS_FORCE_CLIENT_UPDATE  = Boolean.valueOf(optionsSettings.getProperty("DayStatusForceClientUpdate", "True"));

                THREAD_P_EFFECTS                = Integer.parseInt(optionsSettings.getProperty("ThreadPoolSizeEffects", "6"));
                THREAD_P_GENERAL                = Integer.parseInt(optionsSettings.getProperty("ThreadPoolSizeGeneral", "15"));
                GENERAL_PACKET_THREAD_CORE_SIZE         = Integer.parseInt(optionsSettings.getProperty("GeneralPacketThreadCoreSize", "4"));
                URGENT_PACKET_THREAD_CORE_SIZE          =Integer.parseInt(optionsSettings.getProperty("UrgentPacketThreadCoreSize", "2"));
                AI_MAX_THREAD                   = Integer.parseInt(optionsSettings.getProperty("AiMaxThread", "10"));
                GENERAL_THREAD_CORE_SIZE        = Integer.parseInt(optionsSettings.getProperty("GeneralThreadCoreSize", "4"));
                             
                DELETE_DAYS                     = Integer.parseInt(optionsSettings.getProperty("DeleteCharAfterDays", "7"));
                             
                DEFAULT_PUNISH                  = Integer.parseInt(optionsSettings.getProperty("DefaultPunish", "2"));
                DEFAULT_PUNISH_PARAM            = Integer.parseInt(optionsSettings.getProperty("DefaultPunishParam", "0"));

                LAZY_CACHE                      = Boolean.valueOf(optionsSettings.getProperty("LazyCache", "False"));

                PACKET_LIFETIME                 = Integer.parseInt(optionsSettings.getProperty("PacketLifeTime", "0"));
                             
                BYPASS_VALIDATION               = Boolean.valueOf(optionsSettings.getProperty("BypassValidation", "False"));
                             
                GAMEGUARD_ENFORCE               = Boolean.valueOf(optionsSettings.getProperty("GameGuardEnforce", "False"));
                GAMEGUARD_PROHIBITACTION        = Boolean.valueOf(optionsSettings.getProperty("GameGuardProhibitAction", "False"));    
               

                // ---------------------------------------------------
                // Configuration values not found in config files
                // ---------------------------------------------------

                COUNT_PACKETS                   = Boolean.valueOf(optionsSettings.getProperty("CountPacket", "false"));  
                DUMP_PACKET_COUNTS              = Boolean.valueOf(optionsSettings.getProperty("DumpPacketCounts", "false"));
                DUMP_INTERVAL_SECONDS           = Integer.parseInt(optionsSettings.getProperty("PacketDumpInterval", "60"));
                
                MINIMUM_UPDATE_DISTANCE         = Integer.parseInt(optionsSettings.getProperty("MaximumUpdateDistance", "50"));
                MINIMUN_UPDATE_TIME             = Integer.parseInt(optionsSettings.getProperty("MinimumUpdateTime", "500"));
                CHECK_KNOWN                     = Boolean.valueOf(optionsSettings.getProperty("CheckKnownList", "false"));
                KNOWNLIST_FORGET_DELAY          = Integer.parseInt(optionsSettings.getProperty("KnownListForgetDelay", "10000"));
            }
            catch (Exception e)
            {
                e.printStackTrace();
                throw new Error("Failed to Load "+OPTIONS_FILE+" File.");
            }

  /** -------Options config file end --------------------------------------------------------------------------------------------------------------------------------------- */

 /** -------Rates config file start --------------------------------------------------------------------------------------------------------------------------------------- */
	        
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

 /** -------Telnet config file end --------------------------------------------------------------------------------------------------------------------------------------- */

 /** -------Id Factory config file start --------------------------------------------------------------------------------------------------------------------------------------- */

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

 /** -------Rates config file end --------------------------------------------------------------------------------------------------------------------------------------- */

 /** -------Rates config file start --------------------------------------------------------------------------------------------------------------------------------------- */   	        
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
                MAX_ITEM_IN_PACKET 	= Math.max(INVENTORY_MAXIMUM_NO_DWARF, Math.max(INVENTORY_MAXIMUM_DWARF, INVENTORY_MAXIMUM_GM));
                
                /* Inventory slots limits */
                WAREHOUSE_SLOTS_NO_DWARF = Integer.parseInt(otherSettings.getProperty("MaximumWarehouseSlotsForNoDwarf", "100"));
                WAREHOUSE_SLOTS_DWARF = Integer.parseInt(otherSettings.getProperty("MaximumWarehouseSlotsForDwarf", "120"));
                WAREHOUSE_SLOTS_CLAN = Integer.parseInt(otherSettings.getProperty("MaximumWarehouseSlotsForClan", "150"));
                FREIGHT_SLOTS       = Integer.parseInt(otherSettings.getProperty("MaximumFreightSlots", "20"));

	            

	            
	            /* if different from 100 (ie 100%) heal rate is modified acordingly */
	            HP_REGEN_MULTIPLIER = Double.parseDouble(otherSettings.getProperty("HpRegenMultiplier", "100"));
	            MP_REGEN_MULTIPLIER = Double.parseDouble(otherSettings.getProperty("MpRegenMultiplier", "100"));
	            CP_REGEN_MULTIPLIER = Double.parseDouble(otherSettings.getProperty("CpRegenMultiplier", "100"));

                RAID_HP_REGEN_MULTIPLIER  = Double.parseDouble(otherSettings.getProperty("RaidHpRegenMultiplier", "500")) /100;    
                RAID_MP_REGEN_MULTIPLIER  = Double.parseDouble(otherSettings.getProperty("RaidMpRegenMultiplier", "500")) /100;    
                RAID_DEFENCE_MULTIPLIER  = Double.parseDouble(otherSettings.getProperty("RaidDefenceMultiplier", "500")) /100;    
	            
	            STARTING_ADENA      = Integer.parseInt(otherSettings.getProperty("StartingAdena", "100"));
				STARTING_LEVEL      = Byte.parseByte(otherSettings.getProperty("StartingLevel", "1"));
				STARTING_SP         = Integer.parseInt(otherSettings.getProperty("StartingSP", "0"));                

                UNSTUCK_INTERVAL    = Integer.parseInt(otherSettings.getProperty("UnstuckInterval", "300"));

                ALT_MINIMUM_FALL_HEIGHT = Integer.parseInt(otherSettings.getProperty("MinimumFallHeight", "400"));

                /* Player protection after teleport or login */
                PLAYER_SPAWN_PROTECTION = Integer.parseInt(otherSettings.getProperty("PlayerSpawnProtection", "0"));
	            
	            /* Defines some Party XP related values */
	            PARTY_XP_CUTOFF_METHOD  = otherSettings.getProperty("PartyXpCutoffMethod", "percentage");
	            PARTY_XP_CUTOFF_PERCENT = Double.parseDouble(otherSettings.getProperty("PartyXpCutoffPercent", "3."));
	            PARTY_XP_CUTOFF_LEVEL   = Integer.parseInt(otherSettings.getProperty("PartyXpCutoffLevel", "30"));
	            
	            /* Amount of HP, MP, and CP is restored */
	            RESPAWN_RESTORE_CP = Double.parseDouble(otherSettings.getProperty("RespawnRestoreCP", "70")) / 100;
	            RESPAWN_RESTORE_HP = Double.parseDouble(otherSettings.getProperty("RespawnRestoreHP", "70")) / 100;
	            RESPAWN_RESTORE_MP = Double.parseDouble(otherSettings.getProperty("RespawnRestoreMP", "70")) / 100;
	            
	            RESPAWN_RANDOM_ENABLED = Boolean.parseBoolean(otherSettings.getProperty("RespawnRandomInTown", "False"));
	            RESPAWN_RANDOM_MAX_OFFSET = Integer.parseInt(otherSettings.getProperty("RespawnRandomMaxOffset", "50"));
	            
	            /* Maximum number of available slots for pvt stores */
	            MAX_PVTSTORE_SLOTS_DWARF = Integer.parseInt(otherSettings.getProperty("MaxPvtStoreSlotsDwarf", "5"));
	            MAX_PVTSTORE_SLOTS_OTHER = Integer.parseInt(otherSettings.getProperty("MaxPvtStoreSlotsOther", "4"));

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
                
                GM_NAME_COLOR_ENABLED = Boolean.parseBoolean(otherSettings.getProperty("GMNameColorEnabled", "False"));
                GM_NAME_COLOR = Integer.decode("0x" + otherSettings.getProperty("GMNameColor", "FFFF00"));
                ADMIN_NAME_COLOR = Integer.decode("0x" + otherSettings.getProperty("AdminNameColor", "00FF00"));
                GM_STARTUP_INVULNERABLE = Boolean.parseBoolean(otherSettings.getProperty("GMStartupInvulnerable", "True"));
                GM_STARTUP_INVISIBLE = Boolean.parseBoolean(otherSettings.getProperty("GMStartupInvisible", "True"));
                GM_STARTUP_SILENCE = Boolean.parseBoolean(otherSettings.getProperty("GMStartupSilence", "True"));
                GM_STARTUP_AUTO_LIST = Boolean.parseBoolean(otherSettings.getProperty("GMStartupAutoList", "True"));
                
                PETITIONING_ALLOWED = Boolean.parseBoolean(otherSettings.getProperty("PetitioningAllowed", "True"));
                MAX_PETITIONS_PER_PLAYER = Integer.parseInt(otherSettings.getProperty("MaxPetitionsPerPlayer", "5"));
                MAX_PETITIONS_PENDING = Integer.parseInt(otherSettings.getProperty("MaxPetitionsPending", "25"));
                
                JAIL_IS_PVP       = Boolean.valueOf(otherSettings.getProperty("JailIsPvp", "True"));
                JAIL_DISABLE_CHAT = Boolean.valueOf(otherSettings.getProperty("JailDisableChat", "True"));
	        }
	        catch (Exception e)
	        {
	            e.printStackTrace();
	            throw new Error("Failed to Load "+OTHER_CONFIG_FILE+" File.");
	        }

 /** -------Other config file end --------------------------------------------------------------------------------------------------------------------------------------- */

 /** -------Rates config file start --------------------------------------------------------------------------------------------------------------------------------------- */

            // rates
	        try
	        {
	            Properties ratesSettings    = new Properties();
	            InputStream is              = new FileInputStream(new File(RATES_CONFIG_FILE));
                ratesSettings.load(is);
	            is.close();
                
                RATE_XP                         = Float.parseFloat(ratesSettings.getProperty("RateXp", "1."));
                RATE_SP                         = Float.parseFloat(ratesSettings.getProperty("RateSp", "1."));
                RATE_PARTY_XP                   = Float.parseFloat(ratesSettings.getProperty("RatePartyXp", "1."));
                RATE_PARTY_SP                   = Float.parseFloat(ratesSettings.getProperty("RatePartySp", "1."));
                RATE_QUESTS_REWARD              = Float.parseFloat(ratesSettings.getProperty("RateQuestsReward", "1."));
                RATE_DROP_ADENA                 = Float.parseFloat(ratesSettings.getProperty("RateDropAdena", "1."));
                RATE_CONSUMABLE_COST            = Float.parseFloat(ratesSettings.getProperty("RateConsumableCost", "1."));
                RATE_DROP_ITEMS                 = Float.parseFloat(ratesSettings.getProperty("RateDropItems", "1."));
                RATE_DROP_ITEMS_BY_RAID         = Float.parseFloat(ratesSettings.getProperty("RateRaidDropItems", "1."));
                RATE_DROP_SPOIL                 = Float.parseFloat(ratesSettings.getProperty("RateDropSpoil", "1."));
                RATE_DROP_MANOR                 = Integer.parseInt(ratesSettings.getProperty("RateDropManor", "1"));
                RATE_DROP_QUEST                 = Float.parseFloat(ratesSettings.getProperty("RateDropQuest", "1."));
                RATE_KARMA_EXP_LOST             = Float.parseFloat(ratesSettings.getProperty("RateKarmaExpLost", "1."));    
                RATE_SIEGE_GUARDS_PRICE         = Float.parseFloat(ratesSettings.getProperty("RateSiegeGuardsPrice", "1."));

     			USE_PREMIUMSERVICE              = Boolean.parseBoolean(ratesSettings.getProperty("UsePremiumServices", "False"));
	     		PREMIUM_RATE_XP                 = Float.parseFloat(ratesSettings.getProperty("PremiumRateXp", "2"));
		    	PREMIUM_RATE_SP                 = Float.parseFloat(ratesSettings.getProperty("PremiumRateSp", "2"));
			    PREMIUM_RATE_DROP_ADENA         = Float.parseFloat(ratesSettings.getProperty("PremiumRateDropAdena", "2"));
			    PREMIUM_RATE_DROP_SPOIL         = Float.parseFloat(ratesSettings.getProperty("PremiumRateDropSpoil", "2"));
			    PREMIUM_RATE_DROP_ITEMS         = Float.parseFloat(ratesSettings.getProperty("PremiumRateDropItems", "2"));
			    PREMIUM_RATE_DROP_QUEST         = Float.parseFloat(ratesSettings.getProperty("PremiumRateDropQuest", "2"));                

                ALT_GAME_EXPONENT_XP            = Float.parseFloat(ratesSettings.getProperty("AltGameExponentXp", "0."));
	            ALT_GAME_EXPONENT_SP            = Float.parseFloat(ratesSettings.getProperty("AltGameExponentSp", "0."));
                
                PLAYER_DROP_LIMIT               = Integer.parseInt(ratesSettings.getProperty("PlayerDropLimit", "3"));
                PLAYER_RATE_DROP                = Integer.parseInt(ratesSettings.getProperty("PlayerRateDrop", "5"));
                PLAYER_RATE_DROP_ITEM           = Integer.parseInt(ratesSettings.getProperty("PlayerRateDropItem", "70"));
                PLAYER_RATE_DROP_EQUIP          = Integer.parseInt(ratesSettings.getProperty("PlayerRateDropEquip", "25"));
                PLAYER_RATE_DROP_EQUIP_WEAPON   = Integer.parseInt(ratesSettings.getProperty("PlayerRateDropEquipWeapon", "5"));

                PET_XP_RATE                     = Float.parseFloat(ratesSettings.getProperty("PetXpRate", "1."));
                PET_FOOD_RATE                   = Integer.parseInt(ratesSettings.getProperty("PetFoodRate", "1"));
                SINEATER_XP_RATE                = Float.parseFloat(ratesSettings.getProperty("SinEaterXpRate", "1.00"));

                KARMA_DROP_LIMIT                = Integer.parseInt(ratesSettings.getProperty("KarmaDropLimit", "10"));
                KARMA_RATE_DROP                 = Integer.parseInt(ratesSettings.getProperty("KarmaRateDrop", "70"));
                KARMA_RATE_DROP_ITEM            = Integer.parseInt(ratesSettings.getProperty("KarmaRateDropItem", "50"));
                KARMA_RATE_DROP_EQUIP           = Integer.parseInt(ratesSettings.getProperty("KarmaRateDropEquip", "40"));
                KARMA_RATE_DROP_EQUIP_WEAPON    = Integer.parseInt(ratesSettings.getProperty("KarmaRateDropEquipWeapon", "10"));
	            
	            CAN_SPOIL_LOWER_LEVEL_MOBS              = Boolean.parseBoolean(ratesSettings.getProperty("CanSpoilLowerLevelMobs", "false"));
	            CAN_DELEVEL_AND_SPOIL_MOBS              = Boolean.parseBoolean(ratesSettings.getProperty("CanDelevelToSpoil", "true"));                       
	            MAXIMUM_PLAYER_AND_MOB_LEVEL_DIFFERENCE = Float.parseFloat(ratesSettings.getProperty("MaximumPlayerAndMobLevelDifference", "9."));
	            BASE_SPOIL_RATE                         = Float.parseFloat(ratesSettings.getProperty("BasePercentChanceOfSpoilSuccess", "40."));
	            MINIMUM_SPOIL_RATE                      = Float.parseFloat(ratesSettings.getProperty("MinimumPercentChanceOfSpoilSuccess", "3."));
	            SPOIL_LEVEL_DIFFERENCE_LIMIT            = Float.parseFloat(ratesSettings.getProperty("SpoilLevelDifferenceLimit", "5."));
	            SPOIL_LEVEL_DIFFERENCE_MULTIPLIER       = Float.parseFloat(ratesSettings.getProperty("SpoilLevelMultiplier", "7."));
	            LAST_LEVEL_SPOIL_IS_LEARNED             = Integer.parseInt(ratesSettings.getProperty("LastLevelSpoilIsLearned", "72"));
            }
	        catch (Exception e) {
	            e.printStackTrace();
	            throw new Error("Failed to Load "+RATES_CONFIG_FILE+" File.");
	        }

 /** -------Rates config file end --------------------------------------------------------------------------------------------------------------------------------------- */            

 /** -------Chat config file start --------------------------------------------------------------------------------------------------------------------------------------- */

            // chat settings
	        try
	        {
	            Properties chatSettings    = new Properties();
	            InputStream is              = new FileInputStream(new File(CHAT_CONFIG_FILE));
                chatSettings.load(is);
	            is.close();

                DEFAULT_GLOBAL_CHAT             = chatSettings.getProperty("GlobalChat", "ON");
                DEFAULT_TRADE_CHAT              = chatSettings.getProperty("TradeChat", "ON");

                SHOUT_CHAT_LEVEL                = Integer.parseInt(chatSettings.getProperty("GlobalChatLevel", "10"));
                TRADE_CHAT_LEVEL                = Integer.parseInt(chatSettings.getProperty("TradeChatLevel", "10"));

                LOG_CHAT                        = Boolean.valueOf(chatSettings.getProperty("LogChat", "false"));

            }
	        catch (Exception e) {
	            e.printStackTrace();
	            throw new Error("Failed to Load "+CHAT_CONFIG_FILE+" File.");
	        }

 /** -------Chat config file end --------------------------------------------------------------------------------------------------------------------------------------- */

 /** -------L2PTS config file start --------------------------------------------------------------------------------------------------------------------------------------- */

            // L2PTS settings
	        try
	        {
	            Properties ptsSettings    = new Properties();
	            InputStream is              = new FileInputStream(new File(PTS_CONFIG_FILE));
                ptsSettings.load(is);
	            is.close();

                EQUIPABLE_CHARACTER	         = Boolean.parseBoolean(ptsSettings.getProperty("EquipableCharacter", "true"));

                AUTO_SOUL_SHOT	             = Boolean.parseBoolean(ptsSettings.getProperty("AutoSoulShot", "true"));

                SHOW_MINI_MAP                = Boolean.parseBoolean(ptsSettings.getProperty("ShowMiniMap", "true"));

                AUTO_SELECT_CHAR             = Boolean.parseBoolean(ptsSettings.getProperty("AutoSelectChar", "true"));

                SERVER_TIME_ON_START         = Boolean.parseBoolean(ptsSettings.getProperty("ShowServerTimeOnStart", "true"));
            }
	        catch (Exception e) {
	            e.printStackTrace();
	            throw new Error("Failed to Load "+PTS_CONFIG_FILE+" File.");
	        }

 /** -------L2PTS config file end --------------------------------------------------------------------------------------------------------------------------------------- */

 /** -------Enchantig config file start --------------------------------------------------------------------------------------------------------------------------------------- */

            // enchant settings
	        try
	        {
	            Properties enchantSettings    = new Properties();
	            InputStream is              = new FileInputStream(new File(ENCHANT_CONFIG_FILE));
                enchantSettings.load(is);
	            is.close();

	            /* chance to enchant an item over +3 */
	            ENCHANT_CHANCE  = Integer.parseInt(enchantSettings.getProperty("EnchantChance", "65"));
	            /* limit on enchant */
	            ENCHANT_MAX = Integer.parseInt(enchantSettings.getProperty("EnchantMax", "25"));
                /*limit of safe enchant normal */
                ENCHANT_SAFE_MAX = Integer.parseInt(enchantSettings.getProperty("EnchantSafeMax", "3"));
                /*limit of safe enchant full */
                ENCHANT_SAFE_MAX_FULL = Integer.parseInt(enchantSettings.getProperty("EnchantSafeMaxFull", "4"));
		    	/** enchant protected **/
			    PROTECTED_ENCHANT = Boolean.parseBoolean(enchantSettings.getProperty("ProtectorEnchant", "false"));

                MAX_ITEM_ENCHANT_KICK = Integer.parseInt(enchantSettings.getProperty("EnchantKick", "0"));
            }
	        catch (Exception e) {
	            e.printStackTrace();
	            throw new Error("Failed to Load "+ENCHANT_CONFIG_FILE+" File.");
	        }

 /** -------Enchantig config file end --------------------------------------------------------------------------------------------------------------------------------------- */

 /** -------Alternative config file start --------------------------------------------------------------------------------------------------------------------------------------- */

            // alternative settings
	        try
	        {
	            Properties characterSettings  = new Properties();
	            InputStream is          = new FileInputStream(new File(CHARACTER_SETTINGS_FILE));
	            characterSettings.load(is);
	            is.close();

                ALT_GAME_TIREDNESS      = Boolean.parseBoolean(characterSettings.getProperty("AltGameTiredness", "false"));
	            ALT_GAME_CREATION       = Boolean.parseBoolean(characterSettings.getProperty("AltGameCreation", "false"));
	            ALT_GAME_CREATION_SPEED = Double.parseDouble(characterSettings.getProperty("AltGameCreationSpeed", "1"));
	            ALT_GAME_CREATION_XP_RATE=Double.parseDouble(characterSettings.getProperty("AltGameCreationRateXp", "1"));
	            ALT_GAME_CREATION_SP_RATE=Double.parseDouble(characterSettings.getProperty("AltGameCreationRateSp", "1"));

                ALT_BLACKSMITH_USE_RECIPES	= Boolean.parseBoolean(characterSettings.getProperty("AltBlacksmithUseRecipes", "true"));

                ALT_GAME_CANCEL_BOW     = characterSettings.getProperty("AltGameCancelByHit", "Cast").equalsIgnoreCase("bow") || characterSettings.getProperty("AltGameCancelByHit", "Cast").equalsIgnoreCase("all");
	            ALT_GAME_CANCEL_CAST    = characterSettings.getProperty("AltGameCancelByHit", "Cast").equalsIgnoreCase("cast") || characterSettings.getProperty("AltGameCancelByHit", "Cast").equalsIgnoreCase("all");
	            ALT_GAME_SHIELD_BLOCKS  = Boolean.parseBoolean(characterSettings.getProperty("AltShieldBlocks", "false"));
	            ALT_GAME_DELEVEL        = Boolean.parseBoolean(characterSettings.getProperty("Delevel", "true"));
            	ALT_GAME_MAGICFAILURES  = Boolean.parseBoolean(characterSettings.getProperty("MagicFailures", "false"));
	            ALT_GAME_MOB_ATTACK_AI  = Boolean.parseBoolean(characterSettings.getProperty("AltGameMobAttackAI", "false"));
                ALT_MOB_AGRO_IN_PEACEZONE  = Boolean.parseBoolean(characterSettings.getProperty("AltMobAgroInPeaceZone", "false"));

                ALLOW_CLASS_MASTERS     = Boolean.valueOf(characterSettings.getProperty("AllowClassMasters", "False"));
	            ALT_GAME_FREIGHTS       = Boolean.parseBoolean(characterSettings.getProperty("AltGameFreights", "false"));
	            ALT_GAME_FREIGHT_PRICE  = Integer.parseInt(characterSettings.getProperty("AltGameFreightPrice", "1000"));

                ALT_PARTY_RANGE 		= Integer.parseInt(characterSettings.getProperty("AltPartyRange", "1600"));
                ALT_PARTY_RANGE2  		= Integer.parseInt(characterSettings.getProperty("AltPartyRange2", "1400"));                

                ENABLE_RATE_HP          = Boolean.parseBoolean(characterSettings.getProperty("EnableRateHp", "false"));
	            IS_CRAFTING_ENABLED     = Boolean.parseBoolean(characterSettings.getProperty("CraftingEnabled", "true"));
	            AUTO_LOOT               = characterSettings.getProperty("AutoLoot").equalsIgnoreCase("True");
                ALT_GAME_KARMA_PLAYER_CAN_BE_KILLED_IN_PEACEZONE    = Boolean.valueOf(characterSettings.getProperty("AltKarmaPlayerCanBeKilledInPeaceZone", "false"));
                ALT_GAME_KARMA_PLAYER_CAN_SHOP                      = Boolean.valueOf(characterSettings.getProperty("AltKarmaPlayerCanShop", "true"));
                ALT_GAME_KARMA_PLAYER_CAN_TELEPORT                  = Boolean.valueOf(characterSettings.getProperty("AltKarmaPlayerCanTeleport", "true"));
                ALT_GAME_KARMA_PLAYER_CAN_TRADE                     = Boolean.valueOf(characterSettings.getProperty("AltKarmaPlayerCanTrade", "true"));
                ALT_GAME_KARMA_PLAYER_CAN_USE_WAREHOUSE             = Boolean.valueOf(characterSettings.getProperty("AltKarmaPlayerCanUseWareHouse", "true"));
                ALT_GAME_FREE_TELEPORT                              = Boolean.parseBoolean(characterSettings.getProperty("AltFreeTeleporting", "False"));
                ALT_GAME_VIEWNPC                    				= Boolean.parseBoolean(characterSettings.getProperty("AltGameViewNpc", "False"));
                ALT_GAME_NEW_CHAR_ALWAYS_IS_NEWBIE                  = Boolean.parseBoolean(characterSettings.getProperty("AltNewCharAlwaysIsNewbie", "False"));
                DWARF_RECIPE_LIMIT                                  = Integer.parseInt(characterSettings.getProperty("DwarfRecipeLimit","50"));
                
                ALT_CLAN_MEMBERS_FOR_WAR                            = Integer.parseInt(characterSettings.getProperty("AltClanMembersForWar", "15"));
                ALT_CLAN_JOIN_DAYS                                  = Integer.parseInt(characterSettings.getProperty("DaysBeforeJoinAClan", "5"));
                ALT_CLAN_CREATE_DAYS                                = Integer.parseInt(characterSettings.getProperty("DaysBeforeCreateAClan", "10"));
                
                ALT_LOTTERY_PRIZE                                   = Integer.parseInt(characterSettings.getProperty("AltLotteryPrize","50000"));
                ALT_LOTTERY_TICKET_PRICE                            = Integer.parseInt(characterSettings.getProperty("AltLotteryTicketPrice","2000"));
                ALT_LOTTERY_5_NUMBER_RATE                           = Float.parseFloat(characterSettings.getProperty("AltLottery5NumberRate","0.6"));
                ALT_LOTTERY_4_NUMBER_RATE                           = Float.parseFloat(characterSettings.getProperty("AltLottery4NumberRate","0.2"));
                ALT_LOTTERY_3_NUMBER_RATE                           = Float.parseFloat(characterSettings.getProperty("AltLottery3NumberRate","0.2"));
                ALT_LOTTERY_2_AND_1_NUMBER_PRIZE                    = Integer.parseInt(characterSettings.getProperty("AltLottery2and1NumberPrize","200"));

                ALT_MANOR_REFRESH_TIME                              = Integer.parseInt(characterSettings.getProperty("AltManorRefreshTime","20"));
    	        ALT_MANOR_REFRESH_MIN                               = Integer.parseInt(characterSettings.getProperty("AltManorRefreshMin","00"));
    	        ALT_MANOR_APPROVE_TIME                              = Integer.parseInt(characterSettings.getProperty("AltManorApproveTime","6"));
    	        ALT_MANOR_APPROVE_MIN                               = Integer.parseInt(characterSettings.getProperty("AltManorApproveMin","00"));
    	        ALT_MANOR_MAINTENANCE_PERIOD                        = Integer.parseInt(characterSettings.getProperty("AltManorMaintenancePeriod","360000"));
    	        ALT_MANOR_SAVE_ALL_ACTIONS                          = Boolean.parseBoolean(characterSettings.getProperty("AltManorSaveAllActions","false"));
    	        ALT_MANOR_SAVE_PERIOD_RATE                          = Integer.parseInt(characterSettings.getProperty("AltManorSavePeriodRate","2"));

                EPIC_FLY                                            = Boolean.parseBoolean(characterSettings.getProperty("DontEpicFly", "false"));

                ALT_DOUBLE_SPAWN                                    = Boolean.parseBoolean(characterSettings.getProperty("DoubleSpawn", "false"));


                INVENTORY_MAXIMUM_PET                               = Integer.parseInt(characterSettings.getProperty("MaximumSlotsForPet", "12"));
            }
	        catch (Exception e)
	        {
	            e.printStackTrace();
	            throw new Error("Failed to Load "+CHARACTER_SETTINGS_FILE+" File.");
	        }

 /** -------Alternative config file end --------------------------------------------------------------------------------------------------------------------------------------- */

 /** -------Seven Signs config file start --------------------------------------------------------------------------------------------------------------------------------------- */

            // Seven Signs Config
            try
            {
                Properties SevenSettings  = new Properties();
                InputStream is            = new FileInputStream(new File(SEVENSIGNS_FILE));  
                SevenSettings.load(is);
                is.close();
                
                ALT_GAME_REQUIRE_CASTLE_DAWN    = Boolean.parseBoolean(SevenSettings.getProperty("AltRequireCastleForDawn", "False"));
                ALT_GAME_REQUIRE_CLAN_CASTLE    = Boolean.parseBoolean(SevenSettings.getProperty("AltRequireClanCastle", "False"));
                ALT_FESTIVAL_MIN_PLAYER         = Integer.parseInt(SevenSettings.getProperty("AltFestivalMinPlayer", "5"));
                ALT_MAXIMUM_PLAYER_CONTRIB      = Integer.parseInt(SevenSettings.getProperty("AltMaxPlayerContrib", "1000000"));
                ALT_FESTIVAL_MANAGER_START      = Long.parseLong(SevenSettings.getProperty("AltFestivalManagerStart", "120000"));
                ALT_FESTIVAL_LENGTH             = Long.parseLong(SevenSettings.getProperty("AltFestivalLength", "1080000"));
                ALT_FESTIVAL_CYCLE_LENGTH       = Long.parseLong(SevenSettings.getProperty("AltFestivalCycleLength", "2280000"));
                ALT_FESTIVAL_FIRST_SPAWN        = Long.parseLong(SevenSettings.getProperty("AltFestivalFirstSpawn", "120000"));
                ALT_FESTIVAL_FIRST_SWARM        = Long.parseLong(SevenSettings.getProperty("AltFestivalFirstSwarm", "300000"));
                ALT_FESTIVAL_SECOND_SPAWN       = Long.parseLong(SevenSettings.getProperty("AltFestivalSecondSpawn", "540000"));
                ALT_FESTIVAL_SECOND_SWARM       = Long.parseLong(SevenSettings.getProperty("AltFestivalSecondSwarm", "720000"));
                ALT_FESTIVAL_CHEST_SPAWN        = Long.parseLong(SevenSettings.getProperty("AltFestivalChestSpawn", "900000"));
            }
            catch (Exception e)
            {
                e.printStackTrace();
                throw new Error("Failed to Load "+SEVENSIGNS_FILE+" File.");
            }

 /** -------Seven Signs config file end --------------------------------------------------------------------------------------------------------------------------------------- */

 /** -------Geodata config file start --------------------------------------------------------------------------------------------------------------------------------------- */

            // Geodata config file
	        try
	        {
	            Properties geodataSettings      = new Properties();
	            InputStream is              = new FileInputStream(new File(GEODATA_CONFIG_FILE));
	            geodataSettings.load(is);
	            is.close();
                // GEODATA CONFIG
                GRIDS_ALWAYS_ON                 = Boolean.parseBoolean(geodataSettings.getProperty("GridsAlwaysOn", "False"));
                GRID_NEIGHBOR_TURNON_TIME       = Integer.parseInt(geodataSettings.getProperty("GridNeighborTurnOnTime", "30"));
                GRID_NEIGHBOR_TURNOFF_TIME      = Integer.parseInt(geodataSettings.getProperty("GridNeighborTurnOffTime", "300"));  
                GEODATA							= Integer.parseInt(geodataSettings.getProperty("GeoData", "0"));
                FORCE_GEODATA					= Boolean.parseBoolean(geodataSettings.getProperty("ForceGeoData", "True"));
                COORD_SYNCHRONIZE               = Integer.parseInt(geodataSettings.getProperty("CoordSynchronize", "-1"));
                ACCEPT_GEOEDITOR_CONN			= Boolean.parseBoolean(geodataSettings.getProperty("AcceptGeoeditorConn", "false"));
     			String correctZ		          	= geodataSettings.getProperty("GeoCorrectZ", "ALL");
	    		GEO_CORRECT_Z	        		= CorrectSpawnsZ.valueOf(correctZ.toUpperCase());
            }
	        catch (Exception e)
	        {
	            e.printStackTrace();
	            throw new Error("Failed to Load "+GEODATA_CONFIG_FILE+" File.");
	        }

 /** -------Geodata config file end --------------------------------------------------------------------------------------------------------------------------------------- */

 /** -------Skills config file start --------------------------------------------------------------------------------------------------------------------------------------- */

            // Skills config file
	        try
	        {
	            Properties skillsSettings      = new Properties();
	            InputStream is              = new FileInputStream(new File(SKILLS_CONFIG_FILE));
	            skillsSettings.load(is);
	            is.close();

                CHECK_SKILLS_ON_ENTER = Boolean.parseBoolean(skillsSettings.getProperty("CheckSkillsOnEnter", "True"));
	            GAME_SKILL_LEARN    = Boolean.parseBoolean(skillsSettings.getProperty("GameSkillLearn", "false"));
                AUTO_LEARN_SKILLS       = Boolean.parseBoolean(skillsSettings.getProperty("AutoLearnSkills", "false"));
                SP_BOOK_NEEDED          = Boolean.parseBoolean(skillsSettings.getProperty("SpBookNeeded", "true"));

                ALT_GAME_SKILL_FORMULAS = skillsSettings.getProperty("AltGameSkillFormulas", "none");
                ALT_GAME_SKILL_HIT_RATE = Float.parseFloat(skillsSettings.getProperty("AltGameSkillHitRate", "1."));
			    ALLOWED_SKILLS = skillsSettings.getProperty("AllowedSkills", "541,542,543,544,545,546,547,548,549,550,551,552,553,554,555,556,557,558,617,618,619,2099");
			    ALLOWED_SKILLS_LIST = new FastList<Integer>();
			    for(String id : ALLOWED_SKILLS.trim().split(","))
			    {
				    ALLOWED_SKILLS_LIST.add(Integer.parseInt(id.trim()));
			    }

                ADD_TELEPORT_SKILL_5_MIN            = Boolean.parseBoolean(skillsSettings.getProperty("AddSkillTeleport5MMin","false"));

                STORE_SKILL_COOLTIME = Boolean.parseBoolean(skillsSettings.getProperty("StoreSkillCooltime", "true"));

            	if(ENABLE_AIO_SYSTEM) //create map if system is enabled
        	    {
        		String[] AioSkillsSplit = skillsSettings.getProperty("AioSkills", "").split(";");
        		AIO_SKILLS = new FastMap<Integer, Integer>(AioSkillsSplit.length);
        		for (String skill : AioSkillsSplit)
        		{
        			String[] skillSplit = skill.split(",");
        			if (skillSplit.length != 2)
        			{
        				System.out.println("[Aio System]: invalid config property in extensions.properties -> AioSkills \"" + skill + "\"");
        			}
        			else
        			{
        				try
        				{
        					AIO_SKILLS.put(Integer.parseInt(skillSplit[0]), Integer.parseInt(skillSplit[1]));
        				}
        				catch (NumberFormatException nfe)
        				{
    						if (!skill.equals(""))
        					{
        						System.out.println("[Aio System]: invalid config property in extensions.properties -> AioSkills \"" + skillSplit[0] + "\"" + skillSplit[1]);
        					}
        				}
        			}
        		  }
        	   }
            }
	        catch (Exception e)
	        {
	            e.printStackTrace();
	            throw new Error("Failed to Load "+SKILLS_CONFIG_FILE+" File.");
	        }

 /** -------Skills config file end --------------------------------------------------------------------------------------------------------------------------------------- */

 /** -------Aio config file start --------------------------------------------------------------------------------------------------------------------------------------- */

            // Aio config file
	        try
	        {
	            Properties aioSettings      = new Properties();
	            InputStream is              = new FileInputStream(new File(AIO_CONFIG_FILE));
	            aioSettings.load(is);
	            is.close();


                ENABLE_AIO_SYSTEM = Boolean.parseBoolean(aioSettings.getProperty("EnableAioSystem", "True"));
             	ALLOW_AIO_USE_GK = Boolean.parseBoolean(aioSettings.getProperty("AllowAioUseGk", "False"));
        	    ALLOW_AIO_USE_CM = Boolean.parseBoolean(aioSettings.getProperty("AllowAioUseClassMaster", "False"));

            }
	        catch (Exception e)
	        {
	            e.printStackTrace();
	            throw new Error("Failed to Load "+AIO_CONFIG_FILE+" File.");
	        }

 /** -------Aio config file end --------------------------------------------------------------------------------------------------------------------------------------- */

 /** -------Clanhall config file start --------------------------------------------------------------------------------------------------------------------------------------- */

            // clanhall settings
            try
            {
                Properties clanhallSettings  = new Properties();
                InputStream is          = new FileInputStream(new File(CLANHALL_CONFIG_FILE));
                clanhallSettings.load(is);
                is.close();

                CH_TELE_FEE_RATIO                                   = Long.valueOf(clanhallSettings.getProperty("ClanHallTeleportFunctionFeeRation", "86400000"));
                CH_TELE1_FEE                                        = Integer.valueOf(clanhallSettings.getProperty("ClanHallTeleportFunctionFeeLvl1", "86400000"));
                CH_TELE2_FEE                                        = Integer.valueOf(clanhallSettings.getProperty("ClanHallTeleportFunctionFeeLvl2", "86400000"));
                CH_SUPPORT_FEE_RATIO                                = Long.valueOf(clanhallSettings.getProperty("ClanHallSupportFunctionFeeRation", "86400000"));
                CH_SUPPORT1_FEE                                     = Integer.valueOf(clanhallSettings.getProperty("ClanHallSupportFeeLvl1", "86400000"));
                CH_SUPPORT2_FEE                                     = Integer.valueOf(clanhallSettings.getProperty("ClanHallSupportFeeLvl2", "86400000"));
                CH_SUPPORT3_FEE                                     = Integer.valueOf(clanhallSettings.getProperty("ClanHallSupportFeeLvl3", "86400000"));
                CH_SUPPORT4_FEE                                     = Integer.valueOf(clanhallSettings.getProperty("ClanHallSupportFeeLvl4", "86400000"));
                CH_SUPPORT5_FEE                                     = Integer.valueOf(clanhallSettings.getProperty("ClanHallSupportFeeLvl5", "86400000"));
                CH_SUPPORT6_FEE                                     = Integer.valueOf(clanhallSettings.getProperty("ClanHallSupportFeeLvl6", "86400000"));
                CH_SUPPORT7_FEE                                     = Integer.valueOf(clanhallSettings.getProperty("ClanHallSupportFeeLvl7", "86400000"));
                CH_SUPPORT8_FEE                                     = Integer.valueOf(clanhallSettings.getProperty("ClanHallSupportFeeLvl8", "86400000"));
                CH_MPREG_FEE_RATIO                                  = Long.valueOf(clanhallSettings.getProperty("ClanHallMpRegenerationFunctionFeeRation", "86400000"));
                CH_MPREG1_FEE                                       = Integer.valueOf(clanhallSettings.getProperty("ClanHallMpRegenerationFeeLvl1", "86400000"));
                CH_MPREG2_FEE                                       = Integer.valueOf(clanhallSettings.getProperty("ClanHallMpRegenerationFeeLvl2", "86400000"));
                CH_MPREG3_FEE                                       = Integer.valueOf(clanhallSettings.getProperty("ClanHallMpRegenerationFeeLvl3", "86400000"));
                CH_MPREG4_FEE                                       = Integer.valueOf(clanhallSettings.getProperty("ClanHallMpRegenerationFeeLvl4", "86400000"));
                CH_MPREG5_FEE                                       = Integer.valueOf(clanhallSettings.getProperty("ClanHallMpRegenerationFeeLvl5", "86400000"));
                CH_HPREG_FEE_RATIO                                  = Long.valueOf(clanhallSettings.getProperty("ClanHallHpRegenerationFunctionFeeRation", "86400000"));
                CH_HPREG1_FEE                                       = Integer.valueOf(clanhallSettings.getProperty("ClanHallHpRegenerationFeeLvl1", "86400000"));
                CH_HPREG2_FEE                                       = Integer.valueOf(clanhallSettings.getProperty("ClanHallHpRegenerationFeeLvl2", "86400000"));
                CH_HPREG3_FEE                                       = Integer.valueOf(clanhallSettings.getProperty("ClanHallHpRegenerationFeeLvl3", "86400000"));
                CH_HPREG4_FEE                                       = Integer.valueOf(clanhallSettings.getProperty("ClanHallHpRegenerationFeeLvl4", "86400000"));
                CH_HPREG5_FEE                                       = Integer.valueOf(clanhallSettings.getProperty("ClanHallHpRegenerationFeeLvl5", "86400000"));
                CH_HPREG6_FEE                                       = Integer.valueOf(clanhallSettings.getProperty("ClanHallHpRegenerationFeeLvl6", "86400000"));
                CH_HPREG7_FEE                                       = Integer.valueOf(clanhallSettings.getProperty("ClanHallHpRegenerationFeeLvl7", "86400000"));
                CH_HPREG8_FEE                                       = Integer.valueOf(clanhallSettings.getProperty("ClanHallHpRegenerationFeeLvl8", "86400000"));
                CH_HPREG9_FEE                                       = Integer.valueOf(clanhallSettings.getProperty("ClanHallHpRegenerationFeeLvl9", "86400000"));
                CH_HPREG10_FEE                                       = Integer.valueOf(clanhallSettings.getProperty("ClanHallHpRegenerationFeeLvl10", "86400000"));
                CH_HPREG11_FEE                                       = Integer.valueOf(clanhallSettings.getProperty("ClanHallHpRegenerationFeeLvl11", "86400000"));
                CH_HPREG12_FEE                                       = Integer.valueOf(clanhallSettings.getProperty("ClanHallHpRegenerationFeeLvl12", "86400000"));
                CH_HPREG13_FEE                                       = Integer.valueOf(clanhallSettings.getProperty("ClanHallHpRegenerationFeeLvl13", "86400000"));
                CH_EXPREG_FEE_RATIO                                 = Long.valueOf(clanhallSettings.getProperty("ClanHallExpRegenerationFunctionFeeRation", "86400000"));
                CH_EXPREG1_FEE                                      = Integer.valueOf(clanhallSettings.getProperty("ClanHallExpRegenerationFeeLvl1", "86400000"));
                CH_EXPREG2_FEE                                      = Integer.valueOf(clanhallSettings.getProperty("ClanHallExpRegenerationFeeLvl2", "86400000"));
                CH_EXPREG3_FEE                                      = Integer.valueOf(clanhallSettings.getProperty("ClanHallExpRegenerationFeeLvl3", "86400000"));
                CH_EXPREG4_FEE                                      = Integer.valueOf(clanhallSettings.getProperty("ClanHallExpRegenerationFeeLvl4", "86400000"));
                CH_EXPREG5_FEE                                      = Integer.valueOf(clanhallSettings.getProperty("ClanHallExpRegenerationFeeLvl5", "86400000"));
                CH_EXPREG6_FEE                                      = Integer.valueOf(clanhallSettings.getProperty("ClanHallExpRegenerationFeeLvl6", "86400000"));
                CH_EXPREG7_FEE                                      = Integer.valueOf(clanhallSettings.getProperty("ClanHallExpRegenerationFeeLvl7", "86400000"));
                CH_ITEM_FEE_RATIO                                   = Long.valueOf(clanhallSettings.getProperty("ClanHallItemCreationFunctionFeeRation", "86400000"));
                CH_ITEM1_FEE                                        = Integer.valueOf(clanhallSettings.getProperty("ClanHallItemCreationFunctionFeeLvl1", "86400000"));
                CH_ITEM2_FEE                                        = Integer.valueOf(clanhallSettings.getProperty("ClanHallItemCreationFunctionFeeLvl2", "86400000"));
                CH_ITEM3_FEE                                        = Integer.valueOf(clanhallSettings.getProperty("ClanHallItemCreationFunctionFeeLvl3", "86400000"));
                CH_CURTAIN_FEE_RATIO                                = Long.valueOf(clanhallSettings.getProperty("ClanHallCurtainFunctionFeeRation", "86400000"));
                CH_CURTAIN1_FEE                                        = Integer.valueOf(clanhallSettings.getProperty("ClanHallCurtainFunctionFeeLvl1", "86400000"));
                CH_CURTAIN2_FEE                                        = Integer.valueOf(clanhallSettings.getProperty("ClanHallCurtainFunctionFeeLvl2", "86400000"));
                CH_FRONT_FEE_RATIO                                = Long.valueOf(clanhallSettings.getProperty("ClanHallFrontPlatformFunctionFeeRation", "86400000"));
                CH_FRONT1_FEE                                        = Integer.valueOf(clanhallSettings.getProperty("ClanHallFrontPlatformFunctionFeeLvl1", "86400000"));
                CH_FRONT2_FEE                                        = Integer.valueOf(clanhallSettings.getProperty("ClanHallFrontPlatformFunctionFeeLvl2", "86400000"));

		     	DEVASTATED_DAY                                      = Integer.valueOf(clanhallSettings.getProperty("DevastatedDay", "1"));
			    DEVASTATED_HOUR                                     = Integer.valueOf(clanhallSettings.getProperty("DevastatedHour", "18"));
			    DEVASTATED_MINUTES                                 = Integer.valueOf(clanhallSettings.getProperty("DevastatedMinutes", "0"));
			    PARTISAN_DAY                                       = Integer.valueOf(clanhallSettings.getProperty("PartisanDay", "5"));
			    PARTISAN_HOUR                                      = Integer.valueOf(clanhallSettings.getProperty("PartisanHour", "21"));
			    PARTISAN_MINUTES                                  = Integer.valueOf(clanhallSettings.getProperty("PartisanMinutes", "0"));
            }
            catch (Exception e)
            {
                e.printStackTrace();
                throw new Error("Failed to Load "+CLANHALL_CONFIG_FILE+" File.");
            }
 /** -------Clanhall config file end --------------------------------------------------------------------------------------------------------------------------------------- */

 /** -------Pvp config file start --------------------------------------------------------------------------------------------------------------------------------------- */
	        
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
	            
	            KARMA_NONDROPPABLE_PET_ITEMS    = pvpSettings.getProperty("ListOfPetItems", "2375,3500,3501,3502,4422,4423,4424,4425,6648,6649,6650");
	            KARMA_NONDROPPABLE_ITEMS        = pvpSettings.getProperty("ListOfNonDroppableItems", "57,1147,425,1146,461,10,2368,7,6,2370,2369,6842,6611,6612,6613,6614,6615,6616,6617,6618,6619,6620,6621");
	            
	            KARMA_LIST_NONDROPPABLE_PET_ITEMS = new FastList<Integer>();
	            for (String id : KARMA_NONDROPPABLE_PET_ITEMS.split(",")) {
	                KARMA_LIST_NONDROPPABLE_PET_ITEMS.add(Integer.parseInt(id));
	            }
	            
	            KARMA_LIST_NONDROPPABLE_ITEMS = new FastList<Integer>();
	            for (String id : KARMA_NONDROPPABLE_ITEMS.split(",")) {
	                KARMA_LIST_NONDROPPABLE_ITEMS.add(Integer.parseInt(id));
	            }

                PVP_NORMAL_TIME = Integer.parseInt(pvpSettings.getProperty("PvPVsNormalTime", "15000"));
                PVP_PVP_TIME = Integer.parseInt(pvpSettings.getProperty("PvPVsPvPTime", "30000"));
            }
	        catch (Exception e)
	        {
	            e.printStackTrace();
	            throw new Error("Failed to Load "+PVP_CONFIG_FILE+" File.");
	        }

 /** -------Pvp config file end --------------------------------------------------------------------------------------------------------------------------------------- */

 /** -------Access level config file start --------------------------------------------------------------------------------------------------------------------------------------- */

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
                GM_ENCHANT      = Integer.parseInt(gmSettings.getProperty("GMEnchant", "100"));
                GM_DOOR         = Integer.parseInt(gmSettings.getProperty("GMDoor", "100"));
	            GM_RES          = Integer.parseInt(gmSettings.getProperty("GMRes", "100"));
	            GM_PEACEATTACK  = Integer.parseInt(gmSettings.getProperty("GMPeaceAttack", "100"));
	            GM_HEAL         = Integer.parseInt(gmSettings.getProperty("GMHeal", "100"));
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
                GM_DONT_TAKE_AGGRO = Integer.parseInt(gmSettings.getProperty("GMDontTakeAggro", "90"));
                GM_DONT_TAKE_EXPSP = Integer.parseInt(gmSettings.getProperty("GMDontGiveExpSp", "90"));
                
            }
	        catch (Exception e)
	        {
	            e.printStackTrace();
	            throw new Error("Failed to Load "+GM_ACCESS_FILE+" File.");
	        }

 /** -------Access level config file end --------------------------------------------------------------------------------------------------------------------------------------- */

 /** -------Hexid config file start --------------------------------------------------------------------------------------------------------------------------------------- */

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
	        	_log.warning("Could not load HexID file ("+HEXID_FILE+"). Hopefully login will give us one.");
	        }

 /** -------Hexid config file end --------------------------------------------------------------------------------------------------------------------------------------- */

 /** -------AI config file start --------------------------------------------------------------------------------------------------------------------------------------- */

            /** AI Config */
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
	        	//e.printStackTrace();
	        	//throw new Error("Failed to Load " + AI_FILE + " File.");
	        }
		}

 /** -------AI config file end --------------------------------------------------------------------------------------------------------------------------------------- */           

 /** -------Login server config file start --------------------------------------------------------------------------------------------------------------------------------------- */

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
                GM_MIN          = Integer.parseInt(serverSettings.getProperty("GMMinLevel", "100"));
				
				DATAPACK_ROOT    = new File(serverSettings.getProperty("DatapackRoot", ".")).getCanonicalFile(); //FIXME: in login?
	
	            INTERNAL_HOSTNAME   = serverSettings.getProperty("InternalHostname", "localhost");
	            EXTERNAL_HOSTNAME   = serverSettings.getProperty("ExternalHostname", "localhost");
	            
	            DATABASE_DRIVER             = serverSettings.getProperty("Driver", "com.mysql.jdbc.Driver");
	            DATABASE_URL                = serverSettings.getProperty("URL", "jdbc:mysql://localhost/l2jdb");
	            DATABASE_LOGIN              = serverSettings.getProperty("Login", "root");
	            DATABASE_PASSWORD           = serverSettings.getProperty("Password", "");
	            DATABASE_MAX_CONNECTIONS    = Integer.parseInt(serverSettings.getProperty("MaximumDbConnections", "10"));
	            
	            SHOW_LICENCE = Boolean.parseBoolean(serverSettings.getProperty("ShowLicence", "true"));
	            IP_UPDATE_TIME				= Integer.parseInt(serverSettings.getProperty("IpUpdateTime","15"));
	            FORCE_GGAUTH = Boolean.parseBoolean(serverSettings.getProperty("ForceGGAuth", "false"));
	            
	            AUTO_CREATE_ACCOUNTS = Boolean.parseBoolean(serverSettings.getProperty("AutoCreateAccounts","True"));
	            
	            FLOOD_PROTECTION = Boolean.parseBoolean(serverSettings.getProperty("EnableFloodProtection","True"));
	            FAST_CONNECTION_LIMIT = Integer.parseInt(serverSettings.getProperty("FastConnectionLimit","15"));
	            NORMAL_CONNECTION_TIME = Integer.parseInt(serverSettings.getProperty("NormalConnectionTime","700"));
	            FAST_CONNECTION_TIME = Integer.parseInt(serverSettings.getProperty("FastConnectionTime","350"));
	            MAX_CONNECTION_PER_IP = Integer.parseInt(serverSettings.getProperty("MaxConnectionPerIP","50"));
	        }
	        catch (Exception e)
	        {
	            e.printStackTrace();
	            throw new Error("Failed to Load "+CONFIGURATION_FILE+" File.");
	        }
  /** -------Login server config file end --------------------------------------------------------------------------------------------------------------------------------------- */

  /** -------Telnet config file start --------------------------------------------------------------------------------------------------------------------------------------- */

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

 /** -------Telnet config file end --------------------------------------------------------------------------------------------------------------------------------------- */               

        }
		else
		{
			_log.severe("Could not Load Config: server mode was not set");
		}



    }
	
    /**
     * Set a new value to a game parameter from the admin console.
     * @param pName (String) : name of the parameter to change
     * @param pValue (String) : new value of the parameter
     * @return boolean : true if modification has been made
     * @link useAdminCommand
     */
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
        else if (pName.equalsIgnoreCase("RateRaidDropItems")) RATE_DROP_ITEMS_BY_RAID = Float.parseFloat(pValue);
        else if (pName.equalsIgnoreCase("RateDropSpoil")) RATE_DROP_SPOIL = Float.parseFloat(pValue);
        else if (pName.equalsIgnoreCase("RateDropManor")) RATE_DROP_MANOR = Integer.parseInt(pValue);
        else if (pName.equalsIgnoreCase("RateDropQuest")) RATE_DROP_QUEST = Float.parseFloat(pValue);
        else if (pName.equalsIgnoreCase("RateKarmaExpLost")) RATE_KARMA_EXP_LOST = Float.parseFloat(pValue);
        else if (pName.equalsIgnoreCase("RateSiegeGuardsPrice")) RATE_SIEGE_GUARDS_PRICE = Float.parseFloat(pValue);

		else if (pName.equalsIgnoreCase("PremiumRateXp"))
			PREMIUM_RATE_XP = Float.parseFloat(pValue);
		else if (pName.equalsIgnoreCase("PremiumRateSp"))
			PREMIUM_RATE_SP = Float.parseFloat(pValue);
		else if (pName.equalsIgnoreCase("PremiumRateDropAdena"))
			PREMIUM_RATE_DROP_ADENA = Float.parseFloat(pValue);
		else if (pName.equalsIgnoreCase("PremiumRateDropSpoil"))
			PREMIUM_RATE_DROP_SPOIL = Float.parseFloat(pValue);
		else if (pName.equalsIgnoreCase("PremiumRateDropItems"))
			PREMIUM_RATE_DROP_ITEMS = Float.parseFloat(pValue);
		else if (pName.equalsIgnoreCase("PremiumRateDropQuest"))
			PREMIUM_RATE_DROP_QUEST = Float.parseFloat(pValue);

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
        else if (pName.equalsIgnoreCase("PreciseDropCalculation")) PRECISE_DROP_CALCULATION = Boolean.valueOf(pValue);
        else if (pName.equalsIgnoreCase("MultipleItemDrop")) MULTIPLE_ITEM_DROP = Boolean.valueOf(pValue);
        else if (pName.equalsIgnoreCase("CoordSynchronize")) COORD_SYNCHRONIZE = Integer.parseInt(pValue);
        else if (pName.equalsIgnoreCase("DeleteCharAfterDays")) DELETE_DAYS = Integer.parseInt(pValue);

        else if (pName.equalsIgnoreCase("AllowDiscardItem")) ALLOW_DISCARDITEM = Boolean.valueOf(pValue);
        else if (pName.equalsIgnoreCase("AllowFreight")) ALLOW_FREIGHT = Boolean.valueOf(pValue);
        else if (pName.equalsIgnoreCase("AllowWarehouse")) ALLOW_WAREHOUSE = Boolean.valueOf(pValue);
        else if (pName.equalsIgnoreCase("AllowWear")) ALLOW_WEAR = Boolean.valueOf(pValue);
        else if (pName.equalsIgnoreCase("WearDelay")) WEAR_DELAY = Integer.parseInt(pValue);
        else if (pName.equalsIgnoreCase("WearPrice")) WEAR_PRICE = Integer.parseInt(pValue);
        else if (pName.equalsIgnoreCase("AllowWater")) ALLOW_WATER = Boolean.valueOf(pValue);
        else if (pName.equalsIgnoreCase("AllowManor")) ALLOW_MANOR = Boolean.valueOf(pValue); 
        else if (pName.equalsIgnoreCase("AllowRentPet")) ALLOW_RENTPET = Boolean.valueOf(pValue);
        else if (pName.equalsIgnoreCase("AllowBoat")) ALLOW_BOAT = Boolean.valueOf(pValue);

        else if (pName.equalsIgnoreCase("CommunityType")) COMMUNITY_TYPE = pValue;
        else if (pName.equalsIgnoreCase("BBSDefault")) BBS_DEFAULT = pValue;
        else if (pName.equalsIgnoreCase("ShowLevelOnCommunityBoard")) SHOW_LEVEL_COMMUNITYBOARD = Boolean.valueOf(pValue);
        else if (pName.equalsIgnoreCase("ShowStatusOnCommunityBoard")) SHOW_STATUS_COMMUNITYBOARD = Boolean.valueOf(pValue);
        else if (pName.equalsIgnoreCase("NamePageSizeOnCommunityBoard")) NAME_PAGE_SIZE_COMMUNITYBOARD = Integer.parseInt(pValue);
        else if (pName.equalsIgnoreCase("NamePerRowOnCommunityBoard")) NAME_PER_ROW_COMMUNITYBOARD = Integer.parseInt(pValue);

        else if (pName.equalsIgnoreCase("ShowNpcLevel")) SHOW_NPC_LVL = Boolean.valueOf(pValue);

        else if (pName.equalsIgnoreCase("ForceInventoryUpdate")) FORCE_INVENTORY_UPDATE = Boolean.valueOf(pValue);

        else if (pName.equalsIgnoreCase("AutoDeleteInvalidQuestData")) AUTODELETE_INVALID_QUEST_DATA = Boolean.valueOf(pValue);

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
        else if (pName.equalsIgnoreCase("EnchantSafeMax")) ENCHANT_SAFE_MAX = Integer.parseInt(pValue);
        else if (pName.equalsIgnoreCase("EnchantSafeMaxFull")) ENCHANT_SAFE_MAX_FULL = Integer.parseInt(pValue);

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
        else if (pName.equalsIgnoreCase("AltGameCreationSpeed")) ALT_GAME_CREATION_SPEED = Double.parseDouble(pValue);
        else if (pName.equalsIgnoreCase("AltGameCreationXpRate")) ALT_GAME_CREATION_XP_RATE = Double.parseDouble(pValue);
        else if (pName.equalsIgnoreCase("AltGameCreationSpRate")) ALT_GAME_CREATION_SP_RATE = Double.parseDouble(pValue);
        else if (pName.equalsIgnoreCase("AltGameSkillLearn")) GAME_SKILL_LEARN = Boolean.valueOf(pValue);

        else if (pName.equalsIgnoreCase("AltGameCancelByHit"))
        {
            ALT_GAME_CANCEL_BOW     = pValue.equalsIgnoreCase("bow") || pValue.equalsIgnoreCase("all");
            ALT_GAME_CANCEL_CAST    = pValue.equalsIgnoreCase("cast") || pValue.equalsIgnoreCase("all");
        }

        else if (pName.equalsIgnoreCase("AltShieldBlocks")) ALT_GAME_SHIELD_BLOCKS = Boolean.valueOf(pValue);
        else if (pName.equalsIgnoreCase("Delevel")) ALT_GAME_DELEVEL = Boolean.valueOf(pValue);
        else if (pName.equalsIgnoreCase("MagicFailures")) ALT_GAME_MAGICFAILURES = Boolean.valueOf(pValue);
        else if (pName.equalsIgnoreCase("AltGameMobAttackAI")) ALT_GAME_MOB_ATTACK_AI = Boolean.valueOf(pValue);
        else if (pName.equalsIgnoreCase("AltMobAgroInPeaceZone")) ALT_MOB_AGRO_IN_PEACEZONE = Boolean.valueOf(pValue);
        else if (pName.equalsIgnoreCase("AltGameSkillFormulas")) ALT_GAME_SKILL_FORMULAS = pValue;

        else if (pName.equalsIgnoreCase("AltGameExponentXp")) ALT_GAME_EXPONENT_XP = Float.parseFloat(pValue);
        else if (pName.equalsIgnoreCase("AltGameExponentSp")) ALT_GAME_EXPONENT_SP = Float.parseFloat(pValue);

        else if (pName.equalsIgnoreCase("AllowClassMasters")) ALLOW_CLASS_MASTERS = Boolean.valueOf(pValue);
        else if (pName.equalsIgnoreCase("AltGameFreights")) ALT_GAME_FREIGHTS = Boolean.valueOf(pValue);
        else if (pName.equalsIgnoreCase("AltGameFreightPrice")) ALT_GAME_FREIGHT_PRICE = Integer.parseInt(pValue);

        else if (pName.equalsIgnoreCase("AltPartyRange")) ALT_PARTY_RANGE = Integer.parseInt(pValue);
        else if (pName.equalsIgnoreCase("AltPartyRange2")) ALT_PARTY_RANGE2 = Integer.parseInt(pValue);

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
        else if (pName.equalsIgnoreCase("AltNewCharAlwaysIsNewbie")) ALT_GAME_NEW_CHAR_ALWAYS_IS_NEWBIE = Boolean.valueOf(pValue);
        else if (pName.equalsIgnoreCase("DwarfRecipeLimit")) DWARF_RECIPE_LIMIT = Integer.parseInt(pValue);

        // PvP settings
        else if (pName.equalsIgnoreCase("MinKarma")) KARMA_MIN_KARMA = Integer.parseInt(pValue);
        else if (pName.equalsIgnoreCase("MaxKarma")) KARMA_MAX_KARMA = Integer.parseInt(pValue);
        else if (pName.equalsIgnoreCase("XPDivider")) KARMA_XP_DIVIDER = Integer.parseInt(pValue);
        else if (pName.equalsIgnoreCase("BaseKarmaLost")) KARMA_LOST_BASE = Integer.parseInt(pValue);

        else if (pName.equalsIgnoreCase("CanGMDropEquipment")) KARMA_DROP_GM = Boolean.valueOf(pValue);
        else if (pName.equalsIgnoreCase("AwardPKKillPVPPoint")) KARMA_AWARD_PK_KILL = Boolean.valueOf(pValue);
        else if (pName.equalsIgnoreCase("MinimumPKRequiredToDrop")) KARMA_PK_LIMIT = Integer.parseInt(pValue);
        
        else if (pName.equalsIgnoreCase("PvPVsNormalTime")) PVP_NORMAL_TIME = Integer.parseInt(pValue);
        else if (pName.equalsIgnoreCase("PvPVsPvPTime")) PVP_PVP_TIME = Integer.parseInt(pValue);
        else if (pName.equalsIgnoreCase("GlobalChat")) DEFAULT_GLOBAL_CHAT = pValue;
        else if (pName.equalsIgnoreCase("TradeChat"))  DEFAULT_TRADE_CHAT = pValue;
        else return false;
        return true;
    }
    
    /**
     * Allow the player to use L2Walker ?
     * @param player (L2PcInstance) : Player trying to use L2Walker
     * @return boolean : true if (L2Walker allowed as a general rule) or (L2Walker client allowed for GM and 
     *                   player is a GM)
     */
    public static boolean allowL2Walker(L2PcInstance player)
    {
        return (ALLOW_L2WALKER_CLIENT == L2WalkerAllowed.True ||
                (ALLOW_L2WALKER_CLIENT == L2WalkerAllowed.GM && player != null && player.isGM()));
    }
	
	// it has no instancies
	private Config() {}

	/**
     * Save hexadecimal ID of the server in the properties file.
	 * @param string (String) : hexadecimal ID of the server to store
     * @see HEXID_FILE
     * @see saveHexid(String string, String fileName)
     * @link LoginServerThread
	 */
	public static void saveHexid(String string)
	{
		saveHexid(string,HEXID_FILE);
	}
	
	/**
     * Save hexadecimal ID of the server in the properties file.
     * @param string (String) : hexadecimal ID of the server to store
     * @param fileName (String) : name of the properties file
	 */
	public static void saveHexid(String string, String fileName)
	{
		try
        {
            Properties hexSetting    = new Properties();
            File file = new File(fileName);
            //Create a new empty file only if it doesn't exist
            file.createNewFile();
            OutputStream out = new FileOutputStream(file);
            hexSetting.setProperty("HexID",string);
			hexSetting.store(out,"the hexID to auth into login");
			out.close();
        }
        catch (Exception e)
        {
            _log.warning("Failed to save hex id to "+fileName+" File.");
            e.printStackTrace();
        }
	}
	
}
