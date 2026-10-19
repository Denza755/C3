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
package net.sf.l2j.gameserver.model;

import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPOutputStream;

import net.sf.l2j.Config;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.CharTemplateTable;
import net.sf.l2j.gameserver.ClanTable;
import net.sf.l2j.gameserver.ClientScheduler;
import net.sf.l2j.gameserver.Connection;
import net.sf.l2j.gameserver.GameServer;
import net.sf.l2j.gameserver.HennaTable;
import net.sf.l2j.gameserver.ItemTable;
import net.sf.l2j.gameserver.RecipeController;
import net.sf.l2j.gameserver.SevenSignsFestival;
import net.sf.l2j.gameserver.SkillTable;
import net.sf.l2j.gameserver.SkillTreeTable;
import net.sf.l2j.gameserver.ai.CtrlIntention;
import net.sf.l2j.gameserver.ai.L2CharacterAI;
import net.sf.l2j.gameserver.ai.L2PlayerAI;
import net.sf.l2j.gameserver.handler.IItemHandler;
import net.sf.l2j.gameserver.handler.ItemHandler;
import net.sf.l2j.gameserver.instancemanager.ArenaManager;
import net.sf.l2j.gameserver.instancemanager.SiegeManager;
import net.sf.l2j.gameserver.instancemanager.ZoneManager;
import net.sf.l2j.gameserver.lib.Files;
import net.sf.l2j.gameserver.lib.Log;
import net.sf.l2j.gameserver.lib.Rnd;
import net.sf.l2j.gameserver.lib.Strings;
import net.sf.l2j.gameserver.model.L2Skill.SkillTargetType;
import net.sf.l2j.gameserver.model.L2Skill.SkillType;
import net.sf.l2j.gameserver.model.entity.Siege;
import net.sf.l2j.gameserver.model.quest.Quest;
import net.sf.l2j.gameserver.model.quest.QuestState;
import net.sf.l2j.gameserver.model.waypoint.WayPointNode;
import net.sf.l2j.gameserver.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.serverpackets.ChangeWaitType;
import net.sf.l2j.gameserver.serverpackets.CharInfo;
import net.sf.l2j.gameserver.serverpackets.DeleteObject;
import net.sf.l2j.gameserver.serverpackets.DoorInfo;
import net.sf.l2j.gameserver.serverpackets.DoorStatusUpdate;
import net.sf.l2j.gameserver.serverpackets.DropItem;
import net.sf.l2j.gameserver.serverpackets.HennaInfo;
import net.sf.l2j.gameserver.serverpackets.InventoryUpdate;
import net.sf.l2j.gameserver.serverpackets.ItemList;
import net.sf.l2j.gameserver.serverpackets.MagicSkillCanceld;
import net.sf.l2j.gameserver.serverpackets.MyTargetSelected;
import net.sf.l2j.gameserver.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.serverpackets.NpcInfo;
import net.sf.l2j.gameserver.serverpackets.NpcInfoPoly;
import net.sf.l2j.gameserver.serverpackets.ObservationMode;
import net.sf.l2j.gameserver.serverpackets.ObservationReturn;
import net.sf.l2j.gameserver.serverpackets.PartySmallWindowUpdate;
import net.sf.l2j.gameserver.serverpackets.PetInfo;
import net.sf.l2j.gameserver.serverpackets.PetItemList;
import net.sf.l2j.gameserver.serverpackets.PledgeShowMemberListAdd;
import net.sf.l2j.gameserver.serverpackets.PrivateBuyListBuy;
import net.sf.l2j.gameserver.serverpackets.PrivateBuyListSell;
import net.sf.l2j.gameserver.serverpackets.PrivateStoreMsgSell;
import net.sf.l2j.gameserver.serverpackets.RecipeShopSellList;
import net.sf.l2j.gameserver.serverpackets.ServerBasePacket;
import net.sf.l2j.gameserver.serverpackets.SocialAction;
import net.sf.l2j.gameserver.serverpackets.SpawnItem;
import net.sf.l2j.gameserver.serverpackets.SpawnItemPoly;
import net.sf.l2j.gameserver.serverpackets.StaticObject;
import net.sf.l2j.gameserver.serverpackets.StatusUpdate;
import net.sf.l2j.gameserver.serverpackets.StopMove;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.serverpackets.TargetSelected;
import net.sf.l2j.gameserver.serverpackets.UserInfo;
import net.sf.l2j.gameserver.skills.Stats;
import net.sf.l2j.gameserver.templates.L2ArmorType;
import net.sf.l2j.gameserver.templates.L2CharTemplate;
import net.sf.l2j.gameserver.templates.L2Henna;
import net.sf.l2j.gameserver.templates.L2Item;
import net.sf.l2j.gameserver.templates.L2PcTemplate;
import net.sf.l2j.gameserver.templates.L2Weapon;
import net.sf.l2j.gameserver.templates.L2WeaponType;
import net.sf.l2j.util.Point3D;
import ru.balancer.JBForth;


/**
 * This class represents all player characters in the world.
 * There is always a client-thread connected to this (except if a player-store is activated upon logout).<BR><BR>
 *
 * @version $Revision: 1.66.2.41.2.33 $ $Date: 2005/04/11 10:06:09 $
 */
public final class L2PcInstance extends L2PlayableInstance
{
	private static final String ADD_NEW_SKILL = "INSERT INTO character_skills (char_obj_id,skill_id,skill_level,skill_name) values(?,?,?,?)";
	private static final String DELETE_SKILL_FROM_CHAR = "DELETE FROM character_skills WHERE skill_id=? AND char_obj_id=?";
	private static final String UPDATE_CHARACTER_SKILL_LEVEL = "UPDATE character_skills SET skill_level=? WHERE skill_id=? AND char_obj_id=?";

	private static final String ADD_SKILL_SAVE = "INSERT INTO character_skills_save (char_obj_id,skill_id,skill_level,effect_count,effect_cur_time) values(?,?,?,?,?)";
	private static final String RESTORE_SKILL_SAVE = "SELECT skill_id,skill_level,effect_count,effect_cur_time FROM character_skills_save WHERE char_obj_id=?";
	private static final String DELETE_SKILL_SAVE = "DELETE FROM character_skills_save WHERE char_obj_id = ?";

	private static final String GET_ALL_SKILLS_FOR_CHAR = "SELECT skill_id,skill_level FROM character_skills WHERE char_obj_id=?";
	private static final String COLUMN_SKILL_LEVEL = "skill_level";
	private static final String COLUMN_SKILL_ID = "skill_id";

	public static final int STORE_PRIVATE_NONE = 0;
	public static final int STORE_PRIVATE_SELL = 1;
	public static final int STORE_PRIVATE_BUY = 3;
	public static final int STORE_PRIVATE_MANUFACTURE = 5;


	/** The table containing all minimum level needed for each Expertise (None, D, C, B, A, S)*/
	private static final int[] EXPERTISE_LEVELS =
	{
		SkillTreeTable.getExpertiseLevel(0), //NONE
		SkillTreeTable.getExpertiseLevel(1), //D
		SkillTreeTable.getExpertiseLevel(2), //C
		SkillTreeTable.getExpertiseLevel(3), //B
		SkillTreeTable.getExpertiseLevel(4), //A
		SkillTreeTable.getExpertiseLevel(5), //S
	};


	private static Logger _log = Logger.getLogger(L2PcInstance.class.getName());

	public class AIAccessor extends L2PlayableInstance.AIAccessor
	{
		protected AIAccessor() {}

		public L2PcInstance getPlayer() { return L2PcInstance.this; }

		public void doPickupItem(L2Object object)
		{
			L2PcInstance.this.doPickupItem(object);
		}

		public void doInteract(L2Character target)
		{
			L2PcInstance.this.doInteract(target);
		}

		public void doAttack(L2Character target)
		{
			super.doAttack(target);

			for(L2CubicInstance cubic : _cubics.values())
			{
				if(cubic.getId() != L2CubicInstance.LIFE_CUBIC)
					cubic.doAction(target);
			}
		}

		public void doCast(L2Skill skill)
		{
			super.doCast(skill);

			L2Object target = getTarget();

			if (skill.isOffensive() && target != null && target instanceof L2Character)
				for(L2CubicInstance cubic : _cubics.values())
				{
					if(cubic.getId() != L2CubicInstance.LIFE_CUBIC)
						cubic.doAction((L2Character)target);
				}
		}
	}

	private Connection _connection;

	//private L2Object _newTarget = null;

	/** The level of the L2PcInstance */
	private int _level;

	/** The Identifier of the L2PcInstance */
	private int _charId = 0x00030b7a;

	/** The Experience of the L2PcInstance */
	private int _exp;

	/** The Experience of the L2PcInstance before the last Death Penalty */
//	private int _expBeforeDeath = 0;

	/** The losted experience of the L2PcInstance last Death Penalty */
	private int _lostExp = 0;

	/** The number of SP of the L2PcInstance */
	private int _sp;

	/** The Karma of the L2PcInstance (if higher than 0, the name of the L2PcInstance appears in red) */
	private int _karma;

	/** The number of player killed during a PvP (the player killed was PvP Flagged) */
	private int _pvpKills;

	/** The hexadecimal Color of players name (white is 0xFFFFFF) */
	private int _nameColor;

	/** The PK counter of the L2PcInstance (= Number of non PvP Flagged player killed) */
	private int _pkKills;

	/** The PvP Flag state of the L2PcInstance (0=White, 1=Purple) */
	private int _pvpFlag;

	/** The number of recommandation obtained by the L2PcInstance */
	private int _recomHave; // how much I was recommended by others

	/** The number of recommandation that the L2PcInstance can give */
	private int _recomLeft; // how many recomendations I can give to others

	private final int baseLoad;
	private int _curWeightPenalty = 0;

	private int _deleteTimer;
	private boolean _inStoreMode;
	private boolean _inCraftMode;
	private PcInventory _inventory = new PcInventory(this);
	private PcWarehouse _warehouse = new PcWarehouse(this, 0);
	private PcFreight _freight = new PcFreight(this);

	/** True if the L2PcInstance is sitting */
	private boolean _waitTypeSitting;
	/** Time counter when L2PcInstance is sitting */
	private int _waitTimeWhenSit;

	/** True if the L2PcInstance is using the relax skill */
	private boolean _relax;

	/** The clan crest Identifier of the L2PcInstance */
	private int _crestId;

	/** Last NPC Id talked on a quest */
	private int questNpcObject = 0;

	/** The face type Identifier of the L2PcInstance */
	private int _face;

	/** The hair style Identifier of the L2PcInstance */
	private int _hairStyle;

	/** The hair color Identifier of the L2PcInstance */
	private int _hairColor;

	/** The table containing all Quests began by the L2PcInstance */
	private HashMap<String, QuestState> _quests = new HashMap<String, QuestState>();

	/** The list containing all shortCuts of this L2PcInstance */
	private ShortCuts _shortCuts = new ShortCuts(this);

	/** The list containing all macroses of this L2PcInstance */
	private MacroList _macroses = new MacroList(this);

	/** The Alliance Identifier of the L2PcInstance */
	//private int _allyId;

	private L2TradeList _tradeList;
	private L2ManufactureList _createList;
	private ArrayList<TradeItem> _sellList;
	private ArrayList<TradeItem> _buyList;

	/** The Private Store type of the L2PcInstance (STORE_PRIVATE_NONE=0, STORE_PRIVATE_SELL=1, sellmanage=2, STORE_PRIVATE_BUY=3, buymanage=4, STORE_PRIVATE_MANUFACTURE=5) */
	private int _privatestore;
	private ClassId _skillLearningClassId;

	// hennas
	private L2HennaInstance[] _henna = new L2HennaInstance[3];
	private int _hennaSTR;
	private int _hennaINT;
	private int _hennaDEX;
	private int _hennaMEN;
	private int _hennaWIT;
	private int _hennaCON;

	/** The L2Summon of the L2PcInstance */
	private L2Summon _summon = null;

	// client radar
	//TODO: This needs to be better intergrated and saved/loaded
	public L2Radar radar;

	// these values are only stored temporarily
	private boolean _partyMatchingAutomaticRegistration;
	private boolean _partyMatchingShowLevel;
	private boolean _partyMatchingShowClass;
	private String _partyMatchingMemo;

	private L2Party _party;
	// clan related attributes

	/** The Clan Identifier of the L2PcInstance */
	private int _clanId;

	/** The Clan object of the L2PcInstance */
	private L2Clan _clan;
	
	private int _allyId;
	private L2Alliance _alliance;
	
	/** The Clan Leader Flag of the L2PcInstance (True : the L2PcInstance is the leader of the clan) */
	private boolean _clanLeader;
	
	private boolean _allyLeader;

	private long _deleteClanTime;

	private long _NoChannel; // Nochannel mode

	//GM Stuff
	private boolean _isInvul;
	private boolean _isGm;
	private int _accessLevel;

	private boolean _messageRefusal = false;	// message refusal mode
	private boolean _dietMode = false;		  // ignore weight penalty
	private boolean _tradeRefusal = false;	   // Trade refusal
	private boolean _exchangeRefusal = false;   // Exchange refusal

	public boolean _exploring = false;

	// this is needed to find the inviting player for Party response
	// there can only be one active party request at once
	private L2PcInstance _currentTransactionRequester;
	private L2ItemInstance _arrowItem;

	/** The fists L2Weapon of the L2PcInstance (used when no weapon is equiped) */
	private L2Weapon _fistsWeaponItem;

	private long _uptime;
	private String _accountName;

	private final HashMap<Integer, String> _chars = new HashMap<Integer, String>();

	public byte updateKnownCounter = 0;

	/** The table containing all L2RecipeList of the L2PcInstance */
	private Map<Integer, L2RecipeList> _recipebook = new TreeMap<Integer, L2RecipeList>();

	private int _mountType;

	// stats watch
	int oldMaxHP;
	int oldMaxMP;

	/** The current higher Expertise of the L2PcInstance (None=0, D=1, C=2, B=3, A=4, S=5)*/
	int expertiseIndex; // index in EXPERTISE_LEVELS
	int expertisePenalty = 0;

	private L2ItemInstance _enchantScroll = null;

	private int _freightId;
	private boolean _usingFreight;
	private boolean _usingClanWarehouse;
	private boolean _isOnline = false;

	protected boolean _inventoryDisable = false;

	protected HashMap<Integer, L2CubicInstance> _cubics = new HashMap<Integer, L2CubicInstance>();

	/** The L2FolkInstance corresponding to the last Folk wich one the player talked. */
	private L2FolkInstance _lastFolkNpc = null;

	private boolean _isSilentMoving = false;

	private Future _taskAutoSoulShot = null;
	protected HashSet<Integer> _activeSoulShots = new HashSet<Integer>();
	private boolean isPathNodeMode;
	private boolean isPathNodesVisible;
	private Map<WayPointNode, CopyOnWriteArrayList<WayPointNode>> pathNodeMap;
	private WayPointNode selectedNode;
	private int clanPrivileges = 0;
	private boolean linkToggle = true;

	/** 1 if  the player is invisible */
	private int _invisible = 0;

	/** Location before entering Observer Mode */
	private int _obsX;
	private int _obsY;
	private int _obsZ;
	private boolean _observerMode = false;

	public int _telemode = 0;

	/** new loto ticket **/
	public int _loto[] = new int[5];
	//public static int _loto_nums[] = {0,1,2,3,4,5,6,7,8,9,};
	/** new race ticket **/
	public int _race[] = new int[2];

	private ArrayList<L2PcInstance>  _blockList; // characters blocked with '/block <charname>' cmd

	private boolean _blockAll = false;		  // /blockall cmd handling

	private boolean _isConnected = true;

	private boolean _hero = false;
	private int _team = 0;
	private int _wantsPeace = 0;

	public L2Skill _nextSkill;
	public boolean _nextForceUse;
	public boolean _nextDontMove;

	/**
	 * Create a new L2PcInstance and add it in the characters table of the database.<BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Create a new L2PcInstance with an account name </li>
	 * <li>Set the name, the Hair Style, the Hair Color and  the Face type of the L2PcInstance</li>
	 * <li>Add the player in the characters table of the database</li><BR><BR>
	 *
	 * @param objectId Identifier of the object to initialized
	 * @param template The L2PcTemplate to apply to the L2PcInstance
	 * @param accountName The name of the L2PcInstance
	 * @param name The name of the L2PcInstance
	 * @param hairStyle The hair style Identifier of the L2PcInstance
	 * @param hairColor The hair color Identifier of the L2PcInstance
	 * @param face The face type Identifier of the L2PcInstance
	 *
	 * @return The L2PcInstance added to the database or null
	 *
	 */
	public static L2PcInstance create(int objectId, L2PcTemplate template, String accountName,
			String name, int hairStyle, int hairColor, int face)
	{

		// Create a new L2PcInstance with an account name
		L2PcInstance player = new L2PcInstance(objectId, template, accountName);

		// Set the name of the L2PcInstance
		player.setName(name);

		// Set the Hair Style of the L2PcInstance
		player.setHairStyle(hairStyle);

		// Set the Hair Color of the L2PcInstance
		player.setHairColor(hairColor);

		// Set the Face type of the L2PcInstance
		player.setFace(face);

		// Add the player in the characters table of the database
		boolean ok = player.createDb();

		if (!ok)
			return null;

		return player;
	}

	public String getAccountName()
	{
		return _connection.getClient().getLoginName();
	}

	public HashMap<Integer, String> getAccountChars()
	{
		return _chars;
	}

	/**
	 * Retrieve a L2PcInstance from the characters table of the database and add it in _allObjects of the L2world (call restore method).<BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Retrieve the L2PcInstance from the characters table of the database </li>
	 * <li>Add the L2PcInstance object in _allObjects </li>
	 * <li>Set the x,y,z position of the L2PcInstance and make it invisible</li>
	 * <li>Update the overloaded status of the L2PcInstance</li><BR><BR>
	 *
	 * @param objectId Identifier of the object to initialized
	 *
	 * @return The L2PcInstance loaded from the database
	 *
	 */
	public static L2PcInstance load(int objectId)
	{
		return restore(objectId);
	}


	/**
	 * Constructor of L2PcInstance (use L2Character constructor).<BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Call the L2Character constructor to create an empty _skills slot and copy basic Calculator set to this L2PcInstance </li>
	 * <li>Set the name of the L2PcInstance</li><BR><BR>
	 *
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method SET the level of the L2PcInstance to 1</B></FONT><BR><BR>
	 *
	 * @param objectId Identifier of the object to initialized
	 * @param template The L2PcTemplate to apply to the L2PcInstance
	 * @param accountName The name of the account including this L2PcInstance
	 *
	 */
	private L2PcInstance(int objectId, L2PcTemplate template, String accountName)
	{
		super(objectId, template);
		_accountName = accountName;
		_level = 1;
		_blockList = new ArrayList<L2PcInstance>();
		_nameColor	= 0xFFFFFF;
		baseLoad	  = template.baseLoad;
	}

	/**
	 * Constructor of L2PcInstance (use L2Character constructor).<BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Call the L2Character constructor to create an empty _skills slot and copy basic Calculator set to this L2PcInstance </li>
	 * <li>Create a L2Radar object</li>
	 * <li>Retrieve from the database all skills of this L2PcInstance and add them to _skills </li>
	 * <li>Retrieve from the database all items of this L2PcInstance and add them to _inventory </li>
	 * <li>Retrieve from the database all macroses of this L2PcInstance and add them to _macroses</li>
	 * <li>Retrieve from the database all shortCuts of this L2PcInstance and add them to _shortCuts</li><BR><BR>
	 *
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method DOESN'T SET the account name of the L2PcInstance</B></FONT><BR><BR>
	 *
	 * @param objectId Identifier of the object to initialized
	 * @param template The L2PcTemplate to apply to the L2PcInstance
	 *
	 */
	private L2PcInstance(int objectId, L2PcTemplate template)
	{
		this(objectId, template, null);

		jbf = new JBForth(this);

		// Create an AI
		_ai = new L2PlayerAI(new L2PcInstance.AIAccessor());

		// Create a L2Radar object
		radar = new L2Radar(this);

		// Retrieve from the database all skills of this L2PcInstance and add them to _skills
		restoreSkills();

		// Retrieve from the database all items of this L2PcInstance and add them to _inventory
		getInventory().restore();

		// Retrieve from the database all macroses of this L2PcInstance and add them to _macroses
		_macroses.restore();

		// Retrieve from the database all shortCuts of this L2PcInstance and add them to _shortCuts
		_shortCuts.restore();

		restoreHenna();
	}


	/**
	 * Return the L2PcTemplate link to the L2PcInstance.<BR><BR>
	 */
	public final L2PcTemplate getTemplate()
	{
		return (L2PcTemplate)_template;
	}

	public void setTemplate(ClassId newclass, boolean female)
	{
	   _template = CharTemplateTable.getInstance().getTemplate(newclass, female);
	}

	public void changeSex()
	{  boolean male = true;
		if (getSex() == 1){
			male = false;
		}
	   _template = CharTemplateTable.getInstance().getTemplate(getClassId(), !male);
	}

	/**
	 * Return the AI of the L2PcInstance (create it if necessary).<BR><BR>
	 */
	public L2CharacterAI getAI()
	{
		return setAI(new L2PlayerAI(new L2PcInstance.AIAccessor()));
	}


	// added buffer to aggrolist of target's enemys
	public void onBuff(L2PcInstance target,int aggro)
	{
		for(L2Character monster : getEnemyList(target)) {
		((L2Attackable)monster).addDamageHate(this,0,aggro);}
	}

	// make enemy's list for player
	public ArrayList<L2Character> getEnemyList(L2PcInstance target)
	{
		ArrayList<L2Character> targetList = new ArrayList<L2Character>();

		for (L2Object obj : target.getKnownObjects(L2World.PLAYER_VISIBLE_TILES * L2World.TILE_SIZE))
			if (obj instanceof L2Attackable)
				if(((L2Attackable)obj).getHating((L2Character)target)>0)
			   		targetList.add((L2Character)obj);

		return targetList;
	}

	/**
	 * Return the Level of the L2PcInstance.<BR><BR>
	 */
	public final int getLevel() { return _level; }

	/**
	 * Return the Sex of the L2PcInstance (Male=0, Female=1).<BR><BR>
	 */
	public int getSex() { return getTemplate().isMale ? 0 : 1; }


	/**
	 * Return the Face type Identifier of the L2PcInstance.<BR><BR>
	 */
	public int  getFace()
	{
		return _face;
	}

	/**
	 * Set the Face type of the L2PcInstance.<BR><BR>
	 *
	 * @param face The Identifier of the Face type<BR><BR>
	 *
	 */
	public void setFace(int face)
	{
		_face = face;
	}


	/**
	 * Return the Hair Color Identifier of the L2PcInstance.<BR><BR>
	 */
	public int  getHairColor()
	{
		return _hairColor;
	}

	/**
	 * Set the Hair Color of the L2PcInstance.<BR><BR>
	 *
	 * @param hairColor The Identifier of the Hair Color<BR><BR>
	 *
	 */
	public void setHairColor(int hairColor)
	{
		_hairColor = hairColor;
	}


	/**
	 * Return the Hair Style Identifier of the L2PcInstance.<BR><BR>
	 */
	public int getHairStyle()
	{
		return _hairStyle;
	}

	/**
	 * Set the Hair Style of the L2PcInstance.<BR><BR>
	 *
	 * @param hairStyle The Identifier of the Hair Style<BR><BR>
	 *
	 */
	public void setHairStyle(int hairStyle)
	{
		_hairStyle = hairStyle;
	}


	/**
	 * Set the In Store Mode of the L2PcInstance to True or False.<BR><BR>
	 *
	 * @param b The value to set to the In Store Mode<BR><BR>
	 *
	 */
	public void isInStoreMode(boolean b)
	{
		_inStoreMode = b;
	}

	public boolean isInCraftMode()
	{
		return _inCraftMode;
	}

	public void isInCraftMode(boolean b)
	{
		_inCraftMode = b;
	}

	/**
	 * Return the Max HP (base+modifier) of the L2PcInstance and Launch a Regen Task (if necessary).<BR><BR>
	 */
	public int getMaxHp()
	{
		// Get the Max HP (base+modifier) of the L2PcInstance
		int val = super.getMaxHp();

		if (val != oldMaxHP)
		{
			oldMaxHP = val;

			// Launch a regen task if the new Max HP is higher than the old one
			if (getCurrentHp() != val)
				setCurrentHp(getCurrentHp()); // trigger start of regeneration
		}

		return val;

	}


	/**
	 * Return the Max MP (base+modifier) of the L2PcInstance and Launch a Regen Task (if necessary).<BR><BR>
	 */
	public int getMaxMp()
	{
		// Get the Max MP (base+modifier) of the L2PcInstance
		int val = super.getMaxMp();

		if (val != oldMaxMP)
		{
			oldMaxMP = val;

			// Launch a regen task if the new Max MP is higher than the old one
			if (getCurrentMp() != val)
				setCurrentMp(getCurrentMp()); // trigger start of regeneration
		}

		return val;

	}

	/**
	 * Manage Logout Task.<BR><BR>
	 */
	public void logout()
	{
		// Delete all Path Nodes
		clearPathNodes();

		// Close the connection with the client
		_connection.close();
	}


	/**
	 * Return a table containing all L2RecipeList of the L2PcInstance.<BR><BR>
	 */
	public L2RecipeList[] getRecipeBook()
	{
		return _recipebook.values().toArray(new L2RecipeList[_recipebook.values().size()]);
	}

	public boolean findRecipe(L2RecipeList id)
	{
		return _recipebook.containsValue(id);
	}

	/**
	 * Add a new L2RecipList to the table _recipebook containing all L2RecipeList of the L2PcInstance <BR><BR>
	 *
	 * @param recipe The L2RecipeList to add to the _recipebook
	 *
	 */
	public void registerRecipeList(L2RecipeList recipe)
	{
		_recipebook.put(recipe.getId(), recipe);
	}


	/**
	 * Remove a L2RecipList from the table _recipebook containing all L2RecipeList of the L2PcInstance <BR><BR>
	 *
	 * @param RecipeID The Identifier of the L2RecipeList to remove from the _recipebook
	 *
	 */
	public void unregisterRecipeList(int RecipeID)
	{
		if(_recipebook.containsKey(RecipeID))
			_recipebook.remove(RecipeID);
		else
			_log.warning("Attempted to remove unknown RecipeList"+RecipeID);
	}

	/**
	 * Returns the Id for the last talked quest NPC.<BR><BR>
	 */
	public int getLastQuestNpcObject()
	{
		return questNpcObject;
	}

	public void setLastQuestNpcObject(int npcId)
	{
		questNpcObject = npcId;
	}

	/**
	 * Add Quest drops to the table containing all possible drops of a L2NpcInstance.<BR><BR>
	 *
	 * @param npc The L2NpcInstance killed by the L2PcInstance
	 * @param drops The table containing all possible drops of the L2NpcInstance
	 *
	 */
	public void fillQuestDrops(L2NpcInstance npc, ArrayList<L2DropData> drops)
	{
		for (QuestState qs : _quests.values())
		{
			qs.fillQuestDrops(npc, drops);
		}
	}

	/**
	 * Return the QuestState object corresponding to the quest name.<BR><BR>
	 *
	 * @param quest The name of the quest
	 *
	 */
	public QuestState getQuestState(String quest)
	{
		return _quests.get(quest);
	}


	/**
	 * Add a QuestState to the table _quest containing all quests began by the L2PcInstance.<BR><BR>
	 *
	 * @param qs The QuestState to add to _quest
	 *
	 */
	public void setQuestState(QuestState qs)
	{
		_quests.put(qs.getQuest().name, qs);
	}


	/**
	 * Remove a QuestState from the table _quest containing all quests began by the L2PcInstance.<BR><BR>
	 *
	 * @param quest The name of the quest
	 *
	 */
	public void delQuestState(String quest)
	{
		_quests.remove(quest);
	}

	private QuestState[] addToQuestStateArray(QuestState[] questStateArray, QuestState state) {
		int len = questStateArray.length;
		QuestState[] tmp = new QuestState[len+1];
		for (int i=0; i < len; i++)
			tmp[i] = questStateArray[i];
		tmp[len] = state;
		return tmp;
	}

	/**
	 * Return a table containing all Quest in progress from the table _quests.<BR><BR>
	 */
	public Quest[] getAllActiveQuests()
	{
		ArrayList<Quest> quests = new ArrayList<Quest>();

		for (QuestState qs : _quests.values())
		{
			if (qs.getQuest().getQuestIntId()>=1999)
				continue;

			if (qs.isCompleted() && !Config.DEVELOPER)
				continue;

			if (!qs.isStarted() && !Config.DEVELOPER)
				continue;

			quests.add(qs.getQuest());
		}

		return quests.toArray(new Quest[quests.size()]);
	}

    public QuestState[] getAllQuestsStates()
    {
        return _quests.values().toArray(new QuestState[_quests.size()]);
    }
	/**
	 * Return a table containing all QuestState to modify after a L2Attackable killing.<BR><BR>
	 *
	 * @param npcId The Identifier of the L2Attackable attacked
	 *
	 */
	public QuestState[] getQuestsForAttacks(L2NpcInstance npc)
	{
		// Create a QuestState table that will contain all QuestState to modify
		QuestState[] states = null;

		// Go through the QuestState of the L2PcInstance quests
		for (QuestState qs : _quests.values())
		{
			// Check if the Identifier of the L2Attackable attck is needed for the current quest
			if (qs.waitsForAttack(npc))
			{
				// Copy the current L2PcInstance QuestState in the QuestState table
				if (states == null)
					states = new QuestState[]{qs};
				else
					states = addToQuestStateArray(states, qs);
			}
		}

		// Return a table containing all QuestState to modify
		return states;
	}

	/**
	 * Return a table containing all QuestState to modify after a L2Attackable killing.<BR><BR>
	 *
	 * @param npcId The Identifier of the L2Attackable killed
	 *
	 */
	public QuestState[] getQuestsForKills(L2NpcInstance npc)
	{
		// Create a QuestState table that will contain all QuestState to modify
		QuestState[] states = null;

		// Go through the QuestState of the L2PcInstance quests
		for (QuestState qs : _quests.values())
		{
			// Check if the Identifier of the L2Attackable killed is needed for the current quest
			if (qs.waitsForKill(npc))
			{
				// Copy the current L2PcInstance QuestState in the QuestState table
				if (states == null)
					states = new QuestState[]{qs};
				else
					states = addToQuestStateArray(states, qs);
			}
				}

		// Return a table containing all QuestState to modify
		return states;
	}

	/**
	 * Return a table containing all QuestState from the table _quests in which the L2PcInstance must talk to the NPC.<BR><BR>
	 *
	 * @param npcId The Identifier of the NPC
	 *
	 */
	public QuestState[] getQuestsForTalk(int npcId)
	{
		// Create a QuestState table that will contain all QuestState to modify
		QuestState[] states = null;

		// Go through the QuestState of the L2PcInstance quests
		for (QuestState qs : _quests.values())
		{
			// Check if the Identifier of the L2Attackable talk is needed for the current quest
			if (qs.waitsForTalk(npcId))
			{
				// Copy the current L2PcInstance QuestState in the QuestState table
				if (states == null)
					states = new QuestState[]{qs};
				else
					states = addToQuestStateArray(states, qs);
			}
		}

		// Return a table containing all QuestState to modify
		return states;
	}

	public QuestState processQuestEvent(String quest, String event)
	{
		QuestState retval = null;
		if (event == null)
			event = "";
		if (!_quests.containsKey(quest))
			return retval;
		QuestState qs = getQuestState(quest);
		if (qs == null && event.length() == 0)
			return retval;
		if (qs == null) {
			Quest q = Quest.findQuest(quest);
			if (q == null)
				return retval;
			qs = q.newQuestState(this);
		}
		if (qs != null) {
			if (getAccessLevel() >= Config.GM_ACCESSLEVEL)
			{
				if (qs.getQuest().notifyEvent(event, qs))
					showQuestWindow(quest, qs.getStateId());

				retval = qs;
			}
			else
			{
				if (getLastQuestNpcObject() > 0)
				{
					L2Object object = L2World.findObject(getLastQuestNpcObject());
					if (object instanceof L2NpcInstance && getDistance(object.getX(), object.getY()) <= L2NpcInstance.INTERACTION_DISTANCE)
					{
						L2NpcInstance npc = (L2NpcInstance)object;
						QuestState[] states = getQuestsForTalk(npc.getNpcId());

						if (states != null)
						{
							for (QuestState state : states)
							{
								if ((state.getQuest().getQuestIntId() == qs.getQuest().getQuestIntId()) && !qs.isCompleted())
								{
									if (qs.getQuest().notifyEvent(event, qs))
										showQuestWindow(quest, qs.getStateId());

									retval = qs;
								}
							}
						}
					}
				}
			}
		}

		return retval;
	}

	private void showQuestWindow(String questId, String stateId)
	{
		String fileName = "data/jscript/quests/"+questId+"/"+stateId+".htm";
	   	String content = Files.read(fileName, this);

		if(content == null)
			content = "Can't find file'"+fileName+"'";

		NpcHtmlMessage npcReply = new NpcHtmlMessage(5);
		npcReply.setHtml(content);
		sendPacket(npcReply);
		sendPacket(new ActionFailed());
	}


	/**
	 * Return a table containing all L2ShortCut of the L2PcInstance.<BR><BR>
	 */
	public L2ShortCut[] getAllShortCuts()
	{
		return _shortCuts.getAllShortCuts();
	}

	/**
	 * Return the L2ShortCut of the L2PcInstance corresponding to the position (page-slot).<BR><BR>
	 *
	 * @param slot The slot in wich the shortCuts is equiped
	 * @param page The page of shortCuts containing the slot
	 *
	 */
	public L2ShortCut getShortCut(int slot, int page)
	{
		return _shortCuts.getShortCut(slot, page);
	}

	/**
	 * Add a L2shortCut to the L2PcInstance _shortCuts<BR><BR>
	 */
	public void registerShortCut(L2ShortCut shortcut)
	{
		_shortCuts.registerShortCut(shortcut);
	}

	/**
	 * Delete the L2ShortCut corresponding to the position (page-slot) from the L2PcInstance _shortCuts.<BR><BR>
	 */
	public void deleteShortCut(int slot, int page)
	{
		_shortCuts.deleteShortCut(slot, page);
	}

	/**
	 * Add a L2Macro to the L2PcInstance _macroses<BR><BR>
	 */
	public void registerMacro(L2Macro macro)
	{
		_macroses.registerMacro(macro);
	}

	/**
	 * Delete the L2Macro corresponding to the Identifier from the L2PcInstance _macroses.<BR><BR>
	 */
	public void deleteMacro(int id)
	{
		_macroses.deleteMacro(id);
	}

	/**
	 * Return all L2Macro of the L2PcInstance.<BR><BR>
	 */
	public MacroList getMacroses()
	{
		return _macroses;
	}

	/**
	 * Set the PvP Flag of the L2PcInstance.<BR><BR>
	 */
	public void setPvpFlag(int pvpFlag)
	{
		_pvpFlag = pvpFlag;
	}


	/**
	 * Return True if the L2PcInstance can Craft.<BR><BR>
	 */
	public boolean getCanCraft()
	{
		return getSkillLevel(L2Skill.SKILL_CRAFTING) >= 1;
	}

	/**
	 * Return the PK counter of the L2PcInstance.<BR><BR>
	 */
	public int getPkKills()
	{
		return _pkKills;
	}

	/**
	 * Set the PK counter of the L2PcInstance.<BR><BR>
	 */
	public void setPkKills(int pkKills)
	{
		_pkKills = pkKills;
	}

	/**
	 * Return the _deleteTimer of the L2PcInstance.<BR><BR>
	 */
	public int getDeleteTimer()
	{
		return _deleteTimer;
	}

	/**
	 * Set the _deleteTimer of the L2PcInstance.<BR><BR>
	 */
	public void setDeleteTimer(int deleteTimer)
	{
		_deleteTimer = deleteTimer;
	}

	/**
	 * Return the current weight of the L2PcInstance.<BR><BR>
	 */
	public int getCurrentLoad()
	{
		return _inventory.getTotalWeight();
	}

	/**
	 * Return the number of recommandation obtained by the L2PcInstance.<BR><BR>
	 */
	public int getRecomHave()
	{
		return _recomHave;
	}

	/**
	 * Set the number of recommandation obtained by the L2PcInstance (Max : 255).<BR><BR>
	 */
	public int setRecomHave(int recom)
	{
		if (recom > 255)
			recom = 255;

		return _recomHave = recom;
	}

	/**
	 * Return the number of recommandation that the L2PcInstance can give.<BR><BR>
	 */
	public int getRecomLeft()
	{
		return _recomLeft;
	}

	/**
	 * Set the number of recommandation that the L2PcInstance can give.<BR><BR>
	 */
	public int setRecomLeft(int recom)
	{
		return _recomLeft = recom;
	}

	/**
	 * Return the Karma of the L2PcInstance.<BR><BR>
	 */
	public int getKarma()
	{
		return _karma;
	}

	/**
	 * Set the Karma of the L2PcInstance and send a Server->Client packet StatusUpdate (broadcast).<BR><BR>
	 */
	public void setKarma(int karma)
	{
		_karma = karma;

		if (karma > 0)
		{
			StatusUpdate su = new StatusUpdate(getObjectId());
			su.addAttribute(StatusUpdate.KARMA, karma);
			for(L2Object object : getKnownObjects(L2World.PLAYER_VISIBLE_TILES * L2World.TILE_SIZE))
			{
				if(object instanceof L2GuardInstance
					&& ((L2GuardInstance)object).getIntention() == CtrlIntention.AI_INTENTION_IDLE)
					((L2GuardInstance)object).setIntention(CtrlIntention.AI_INTENTION_ACTIVE,null, null);
			}

			// Send a Server->Client packet StatusUpdate to the L2PcInstance AND to all L2PcInstance in its _KnownPlayers
			broadcastPacket(su);
		}
	}

	/**
	 * Return the max weight that the L2PcInstance can load.<BR><BR>
	 */
	public int getMaxLoad()
	{
		return (int)calcStat(Stats.MAX_LOAD, baseLoad, this, null);
	}

	public int getexpertisePenalty()
	{
		return expertisePenalty;
	}
	public int getWeightPenalty()
	{
		if(_dietMode)
			return 0;
		return _curWeightPenalty;
	}

	/**
	 * Update the overloaded status of the L2PcInstance.<BR><BR>
	 */
	public void refreshOverloaded()
	{
		if (getMaxLoad() > 0 && !_dietMode)
		{
		  setOverloaded(getCurrentLoad() > getMaxLoad());
		  float weightproc = getCurrentLoad() * 100 / getMaxLoad();
		  int newWeightPenalty;
		  if ( weightproc < 50) newWeightPenalty=0;
		  else if ( weightproc < 66.6) newWeightPenalty=1;
		  else if ( weightproc < 80) newWeightPenalty=2;
		  else if ( weightproc < 100) newWeightPenalty=3;
		  else newWeightPenalty=4;
		  if (_curWeightPenalty != newWeightPenalty){
			if (_curWeightPenalty>0){
			L2Skill oldskill = SkillTable.getInstance().getInfo(4270,_curWeightPenalty);
			super.removeSkill(oldskill);
			}
			if (newWeightPenalty>0){
			L2Skill newskill = SkillTable.getInstance().getInfo(4270,newWeightPenalty);
			super.addSkill(newskill);
			}
			_curWeightPenalty=newWeightPenalty;
			super.updateEffectIcons();
			updateStats();
		  }
		}
	}
	public void calcExpertisePenalty()
{
		  int newPenalty = 0;
		  for (L2ItemInstance item : getInventory().getItems()){
			if (item.isEquipped())
			{
			int crystaltype = item.getItem().getCrystalType();
			if (crystaltype > newPenalty)
				newPenalty = crystaltype;
			}
		  }
		  newPenalty = newPenalty - expertiseIndex;
		  if ( newPenalty <= 0) newPenalty=0;
		  else newPenalty = 1;
		  if (expertisePenalty != newPenalty){
			if (expertisePenalty>0){
			L2Skill oldskill = SkillTable.getInstance().getInfo(4267,expertisePenalty);
			super.removeSkill(oldskill);
			}
			if (newPenalty>0){
			L2Skill newskill = SkillTable.getInstance().getInfo(4267,newPenalty);
			super.addSkill(newskill);
			}
			expertisePenalty = newPenalty;
			super.updateEffectIcons();
		  }
}

	/**
	 * Return the the PvP Kills of the L2PcInstance (Number of player killed during a PvP).<BR><BR>
	 */
	public int getPvpKills()
	{
		return _pvpKills;
	}

	/**
	 * Set the the PvP Kills of the L2PcInstance (Number of player killed during a PvP).<BR><BR>
	 */
	public void setPvpKills(int pvpKills)
	{
		_pvpKills = pvpKills;
	}

	/**
	 * Return the ClassId object of the L2PcInstance contained in L2PcTemplate.<BR><BR>
	 */
	public ClassId getClassId()
	{
		return getTemplate().classId;
	}

	/**
	 * Set the template of the L2PcInstance.<BR><BR>
	 *
	 * @param Id The Identifier of the L2PcTemplate to set to the L2PcInstance
	 *
	 */
	public void setClassId(int Id)
	{
		L2PcTemplate t = CharTemplateTable.getInstance().getTemplate(Id, getSex()==1);

		if (t == null)
		{
			_log.severe("Missing template for classId: "+Id);
			throw new Error();
		}

		// Set the template of the L2PcInstance
		setTemplate(t);
	}

	/**
	 * Return the Experience of the L2PcInstance.<BR><BR>
	 */
	public int getExp()
	{
		return _exp;
	}


	public void setEnchantScroll(L2ItemInstance scroll)
	{
		_enchantScroll = scroll;
	}

	public L2ItemInstance getEnchantScroll()
	{
		return _enchantScroll;
	}


	/**
	 * Set the fists weapon of the L2PcInstance (used when no weapon is equiped).<BR><BR>
	 *
	 * @param weaponItem The fists L2Weapon to set to the L2PcInstance
	 *
	 */
	public void setFistsWeaponItem(L2Weapon weaponItem)
	{
		_fistsWeaponItem = weaponItem;
	}

	/**
	 * Return the fists weapon of the L2PcInstance (used when no weapon is equiped).<BR><BR>
	 */
	public L2Weapon getFistsWeaponItem()
	{
		return _fistsWeaponItem;
	}

	/**
	 * Return the fists weapon of the L2PcInstance Class (used when no weapon is equiped).<BR><BR>
	 */
	public L2Weapon findFistsWeaponItem(int classId)
	{
		L2Weapon weaponItem = null;
		if ((classId >= 0x00) && (classId <= 0x09))
		{
			//human fighter fists
			L2Item temp = ItemTable.getInstance().getTemplate(246);
			weaponItem = (L2Weapon)temp;
		}
		else if ((classId >= 0x0a) && (classId <= 0x11))
		{
			//human mage fists
			L2Item temp = ItemTable.getInstance().getTemplate(251);
			weaponItem = (L2Weapon)temp;
		}
		else if ((classId >= 0x12) && (classId <= 0x18))
		{
			//elven fighter fists
			L2Item temp = ItemTable.getInstance().getTemplate(244);
			weaponItem = (L2Weapon)temp;
		}
		else if ((classId >= 0x19) && (classId <= 0x1e))
		{
			//elven mage fists
			L2Item temp = ItemTable.getInstance().getTemplate(249);
			weaponItem = (L2Weapon)temp;
		}
		else if ((classId >= 0x1f) && (classId <= 0x25))
		{
			//dark elven fighter fists
			L2Item temp = ItemTable.getInstance().getTemplate(245);
			weaponItem = (L2Weapon)temp;
		}
		else if ((classId >= 0x26) && (classId <= 0x2b))
		{
			//dark elven mage fists
			L2Item temp = ItemTable.getInstance().getTemplate(250);
			weaponItem = (L2Weapon)temp;
		}
		else if ((classId >= 0x2c) && (classId <= 0x30))
		{
			//orc fighter fists
			L2Item temp = ItemTable.getInstance().getTemplate(248);
			weaponItem = (L2Weapon)temp;
		}
		else if ((classId >= 0x31) && (classId <= 0x34))
		{
			//orc mage fists
			L2Item temp = ItemTable.getInstance().getTemplate(252);
			weaponItem = (L2Weapon)temp;
		}
		else if ((classId >= 0x35) && (classId <= 0x39))
		{
			//dwarven fists
			L2Item temp = ItemTable.getInstance().getTemplate(247);
			weaponItem = (L2Weapon)temp;
		}

		return weaponItem;
	}


	/**
	 * Add Experience and SP rewards to the L2PcInstance, remove its Karma (if necessary) and Launch increase level task.<BR><BR>
	 *
	 * <B><U> Actions </U> :</B><BR><BR>
	 * <li>Remove Karma when the player kills L2MonsterInstance</li>
	 * <li>Send a Server->Client packet StatusUpdate to the L2PcInstance</li>
	 * <li>Send a Server->Client System Message to the L2PcInstance </li>
	 * <li>If the L2PcInstance increases it's level, send a Server->Client packet SocialAction (broadcast) </li>
	 * <li>If the L2PcInstance increases it's level, manage the increase level task (Max MP, Max MP, Recommandation, Expertise and beginner skills...) </li>
	 * <li>If the L2PcInstance increases it's level, send a Server->Client packet UserInfo to the L2PcInstance </li><BR><BR>
	 *
	 * @param addToExp The Experience value to add
	 * @param addToSp The SP value to add
	 *
	 */
	public void addExpAndSp(int addToExp, int addToSp)
	{
		if (Config.DEBUG) _log.fine("adding " + addToExp + " exp and " +addToSp+" sp to " + getName());

		if (_exp > Experience.LEVEL[79]) _exp = Experience.LEVEL[79];

		int max_xp = Experience.LEVEL[79] - _exp;

		_exp += (addToExp > max_xp) ? max_xp : addToExp;

		_sp += addToSp;

		// Remove Karma when the player kills L2MonsterInstance
		if(_exp != 0 || isGM())
			_karma -= Math.max(1, addToExp / (getLevel() * Config.KARMA_XP_DIVIDER));

		if(_karma < 0)
			_karma = 0;

		// Send a Server->Client packet StatusUpdate to the L2PcInstance
		StatusUpdate su = new StatusUpdate(getObjectId());
		su.addAttribute(StatusUpdate.EXP, _exp);
		su.addAttribute(StatusUpdate.SP, _sp);
		su.addAttribute(StatusUpdate.KARMA, _karma);
		sendPacket(su);

		// Send a Server->Client System Message to the L2PcInstance
		SystemMessage sm = new SystemMessage(SystemMessage.YOU_EARNED_S1_EXP_AND_S2_SP);
		sm.addNumber(addToExp);
		sm.addNumber(addToSp);
		sendPacket(sm);

		// If the L2PcInstance increases it's level send a Server->Client packet SocialAction (broadcast)
		if((_exp >= Experience.LEVEL[getLevel() + 1]) && (_exp < Experience.LEVEL[79]))
			broadcastPacket(new SocialAction(getObjectId(), 15));

		while ((_exp >= Experience.LEVEL[getLevel() + 1]) && (_exp < Experience.LEVEL[79]))
		{
			// Manage the increase level task of a L2PcInstance (Max MP, Max MP, Recommandation, Expertise and beginner skills...)
			increaseLevel();

			// Send a Server->Client packet UserInfo to the L2PcInstance
			sendPacket(new UserInfo(this));
		}

	}

	/**
	 * Give Expertise skill of this level and remove beginner Lucky skill.<BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Get the Level of the L2PcInstance </li>
	 * <li>If L2PcInstance Level is 5, remove beginner Lucky skill </li>
	 * <li>Add the Expertise skill corresponding to its Expertise level</li>
	 * <li>Update the overloaded status of the L2PcInstance</li><BR><BR>
	 *
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method DOESN'T give other free skills (SP needed = 0)</B></FONT><BR><BR>
	 *
	 */
	private void rewardSkills()
	{
		// Get the Level of the L2PcInstance
		int lvl = getLevel();

		// Remove beginner Lucky skill
		if (lvl == 5)
		{
			L2Skill skill = SkillTable.getInstance().getInfo(194, 1);
			skill = removeSkill(skill);

			if (Config.DEBUG && skill != null) _log.fine("removed skill 'Lucky' from "+getName());
		}

		// Calculate the current higher Expertise of the L2PcInstance
		for (int i=0; i < EXPERTISE_LEVELS.length; i++)
		{
			if (lvl >= EXPERTISE_LEVELS[i])
				expertiseIndex = i;
		}

		// Add the Expertise skill corresponding to its Expertise level
		if (expertiseIndex > 0)
		{
			L2Skill skill = SkillTable.getInstance().getInfo(239, expertiseIndex);
			addSkill(skill);

			if (Config.DEBUG) _log.fine("awarded "+getName()+" with new expertise.");

		}
		else
		{
			if (Config.DEBUG) _log.fine("No skills awarded at lvl: "+lvl);
		}

		// This function gets called on login, so not such a bad place to check weight
		// Update the overloaded status of the L2PcInstance
		refreshOverloaded();
	}


	/**
	 * Set the Experience value of the L2PcInstance.<BR><BR>
	 */
	public void setExp(int exp)
	{
		if (exp < 0)
			exp = 0;
		_exp = exp;
	}

	/**
	 * Return the Race object of the L2PcInstance.<BR><BR>
	 */
	public Race getRace()
	{
		return getTemplate().race;
	}

	/**
	 * Return the SP amount of the L2PcInstance.<BR><BR>
	 */
	public int getSp()
	{
		return _sp;
	}

	/**
	 * Set the SP amount of the L2PcInstance.<BR><BR>
	 */
	public void setSp(int sp)
	{
		if (sp < 0)
			sp = 0;
		_sp = sp;
	}

	/**
	 * @return
	 */
	public int getPvpFlag()
	{
		return _pvpFlag;
	}

	/**
	 * Return the Clan Identifier of the L2PcInstance.<BR><BR>
	 */
	public int getClanId()
	{
		return _clanId;
	}

	/**
	 * Return the Clan Crest Identifier (= Clan Identifier) of the L2PcInstance or 0.<BR><BR>
	 */
	public int getClanCrestId()
	{
		if (_clan != null && _clan.hasCrest())
		{
			return _clan.getCrestId();
		}
		return 0;
	}
	public long getDeleteClanTime()
	{
		return _deleteClanTime;
	}
	public void setDeleteClanTime(long time)
	{
		_deleteClanTime = time;
	}

	// Nochannel
	public long getNoChannel()
	{
		return _NoChannel;
	}
	
	public void setDeleteClanCurTime()
	{
		_deleteClanTime = System.currentTimeMillis();
	}
	
	public boolean canJoinClan()
	{
		if(_deleteClanTime == 0)
			return true;
		if((System.currentTimeMillis()-_deleteClanTime) >= 5*24*60*60*1000)
			return true;
		return false;
	}
	
	public boolean canCreateClan()
	{
		if(_deleteClanTime == 0)
			return true;
		if((System.currentTimeMillis()-_deleteClanTime) >= 10*24*60*60*1000)
			return true;
		return false;
	}


	/**
	 * Return the PcInventory Inventory of the L2PcInstance contained in _inventory.<BR><BR>
	 */
	public PcInventory getInventory()
	{
		return _inventory;
	}


	/**
	 * Remove an item from the Inventory of the L2PcInstance and send a Server->Client InventoryUpdate packet to the L2PcInstance.<BR><BR>
	 */
	public void removeItemFromInventory(L2ItemInstance item, int count)
	{
		L2ItemInstance item2 = getInventory().destroyItem(item.getObjectId(), count);

		if (item2 == null) return;

		InventoryUpdate iu = new InventoryUpdate();
		if (item2.getCount() == 0)
		{
			iu.addRemovedItem(item2);
		}
		else
		{
			iu.addModifiedItem(item2);
		}

		sendPacket(iu);
	}


	/**
	 * Delete a ShortCut of the L2PcInstance _shortCuts.<BR><BR>
	 */
	public void removeItemFromShortCut(int objectId)
	{
		_shortCuts.deleteShortCutByObjectId(objectId);
	}


	/**
	 * Return True if the L2PcInstance is sitting.<BR><BR>
	 */
	public boolean isSitting()
	{
		return _waitTypeSitting;
	}


	/**
	 * Sit down the L2PcInstance, set the AI Intention to AI_INTENTION_REST and send a Server->Client ChangeWaitType packet (broadcast)<BR><BR>
	 */
	public void sitDown()
	{
		if ((!_waitTypeSitting)&&(!isAttackingDisabled())&&(!isAlikeDead()))//use attackDissabled for now
		{
			_waitTypeSitting = true;
			resetWaitSitTime();
			setIntention(CtrlIntention.AI_INTENTION_REST, null, null);
			broadcastPacket(new ChangeWaitType (this, ChangeWaitType.WT_SITTING));
		}
	}

	/**
	 * Stand up the L2PcInstance, set the AI Intention to AI_INTENTION_IDLE and send a Server->Client ChangeWaitType packet (broadcast)<BR><BR>
	 */
	public void standUp()
	{
		if (_waitTypeSitting && !_inStoreMode && !isAlikeDead())
		{
			if (_relax)
			{
				setRelax(false);
				stopEffects(L2Effect.EffectType.RELAXING);
			}
			_waitTypeSitting = false;
			setIntention(CtrlIntention.AI_INTENTION_IDLE, null, null);
			broadcastPacket(new ChangeWaitType (this, ChangeWaitType.WT_STANDING));
		}
	}

	/**
	 * Set the value of the _relax value. Must be True if using skill Relax and False if not.
	 */
	public void setRelax(boolean val)
	{
		_relax = val;
	}

	/** update time counter when L2PcInstance is sitting. */
	public void updateWaitSitTime()
	{
	  if(_waitTimeWhenSit<200)
		_waitTimeWhenSit+=2;
	}

	/** getting how long L2PcInstance is sitting. */
	public int getWaitSitTime()
	{
	  return _waitTimeWhenSit;
	}

	/** reset time counter */
	public void resetWaitSitTime()
	{
	  _waitTimeWhenSit=0;
	}

	/**
	 * Return the PcWarehouse object of the L2PcInstance.<BR><BR>
	 */
	public Warehouse getWarehouse()
	{
		return _warehouse;
	}

	/**
	 * Return the PcFreight object of the L2PcInstance.<BR><BR>
	 */
	public Warehouse getFreight()
	{
		return _freight;
	}

	/**
	 * Return the Identifier of the L2PcInstance.<BR><BR>
	 */
	public int getCharId()
	{
		return _charId;
	}

	/**
	 * Set the Identifier of the L2PcInstance.<BR><BR>
	 */
	public void setCharId(int charId)
	{
		_charId = charId;
	}

	/**
	 * Return the Adena amount of the L2PcInstance.<BR><BR>
	 */
	public int getAdena()
	{
		return _inventory.getAdena();
	}

	/**
	 * Reduce the Adena amount of the L2PcInstance.<BR><BR>
	 */
	public void reduceAdena(int adena)
	{
		_inventory.reduceAdena(adena);
	}

	/**
	 * Add Adena to the L2PcInstance.<BR><BR>
	 */
	public void addAdena(int adena)
	{
		_inventory.addAdena(adena);
	}


	/**
	 * Return the active connection with the client.<BR><BR>
	 */
	public Connection getNetConnection()
	{
		return _connection;
	}


	/**
	 * Set the active connection with the client.<BR><BR>
	 */
	public void setNetConnection(Connection connection)
	{
		_connection = connection;
	}


	/**
	 * Close the active connection with the client.<BR><BR>
	 */
	public void closeNetConnection()
	{
		_connection.close();
	}


	/**
	 * Return the Clan Crest Identifier of the L2PcInstance.<BR><BR>
	 */
	public int getCrestId()
	{
		return _crestId;
	}

	/**
	 * Set the Clan Crest Identifier of the L2PcInstance.<BR><BR>
	 */
	public void setCrestId(int crestId)
	{
		_crestId = crestId;
	}

	/**
	 * Manage actions when a player click on this L2PcInstance.<BR><BR>
	 *
	 * <B><U> Actions on first click on the L2PcInstance (Select it)</U> :</B><BR><BR>
	 * <li>Set the target of the player</li>
	 * <li>Send a Server->Client packet MyTargetSelected to the player (display the select window)</li><BR><BR>
	 *
	 * <B><U> Actions on second click on the L2PcInstance (Follow it/Attack it/Intercat with it)</U> :</B><BR><BR>
	 * <li>Send a Server->Client packet MyTargetSelected to the player (display the select window)</li>
	 * <li>If this L2PcInstance has a Private Store, notify the player AI with AI_INTENTION_INTERACT</li>
	 * <li>If this L2PcInstance is autoAttackable, notify the player AI with AI_INTENTION_ATTACK</li><BR><BR>
	 * <li>If this L2PcInstance is NOT autoAttackable, notify the player AI with AI_INTENTION_FOLLOW</li><BR><BR>
	 *
	 * <B><U> Example of use </U> :</B><BR><BR>
	 * <li> Client packet : Action, AttackRequest</li><BR><BR>
	 *
	 * @param player The player that start an action on this L2PcInstance
	 *
	 */
	@SuppressWarnings("static-access")
	public void onAction(L2PcInstance player)
	{
		if(GameServer.events.onAction(player, this))
			return;

		// Check if the L2PcInstance is confused
		if (player.isConfused())
			player.sendPacket(new ActionFailed());

		if(Config.DEBUG)
			_log.info("player.getTarget()="+player.getTarget()+"; this="+this);

		// Check if the other player already target this L2PcInstance
		if (player.getTarget() != this)
		{
			// Set the target of the player
			player.setTarget(this);

			// The color to display in the select window is White
			player.sendPacket(new MyTargetSelected(getObjectId(), 0));

		}
		else
		{
			// Check if this L2PcInstance has a Private Store
			if (getPrivateStoreType() != 0 )
			{
				player.setIntention(CtrlIntention.AI_INTENTION_INTERACT, this, null);
			}
			else
			{
				// Check if this L2PcInstance is autoAttackable
				if (isAutoAttackable(player))
					player.setIntention(CtrlIntention.AI_INTENTION_ATTACK, this, null);
				else if(player!=this)
					player.setIntention(CtrlIntention.AI_INTENTION_FOLLOW, this, null);
				else
					sendPacket(new ActionFailed());
			}
		}
	}


	/**
	 * Send packet StatusUpdate with current HP,MP and CP to the L2PcInstance and only current HP, MP and Level to all other L2PcInstance of the Party.<BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Send the Server->Client packet StatusUpdate with current HP, MP and CP to this L2PcInstance </li><BR>
	 * <li>Send the Server->Client packet PartySmallWindowUpdate with current HP, MP and Level to all other L2PcInstance of the Party </li><BR><BR>
	 *
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method DOESN'T SEND current HP and MP to all L2PcInstance of the _statusListener</B></FONT><BR><BR>
	 *
	 */
	public void broadcastStatusUpdate()
	{
		//TODO We mustn't send these informations to other players
		// Send the Server->Client packet StatusUpdate with current HP and MP to all L2PcInstance that must be informed of HP/MP updates of this L2PcInstance
		super.broadcastStatusUpdate();

		// Send the Server->Client packet StatusUpdate with current HP, MP and CP to this L2PcInstance
		StatusUpdate su = new StatusUpdate(getObjectId());
		su.addAttribute(StatusUpdate.CUR_HP, (int) getCurrentHp());
		su.addAttribute(StatusUpdate.CUR_MP, (int) getCurrentMp());
		su.addAttribute(StatusUpdate.CUR_CP, (int) getCurrentCp());
		su.addAttribute(StatusUpdate.MAX_CP, 	   getMaxCp());
		sendPacket(su);


		// Check if a party is in progress
		if (isInParty())
		{
			// Send the Server->Client packet PartySmallWindowUpdate with current HP, MP and Level to all other L2PcInstance of the Party
			getParty().broadcastToPartyMembers(this, new PartySmallWindowUpdate(this));
		}
	}


	/**
	 * Send a Server->Client packet UserInfo to this L2PcInstance and CharInfo to all L2PcInstance in its _KnownPlayers.<BR><BR>
	 *
	 * <B><U> Concept</U> :</B><BR><BR>
	 * Others L2PcInstance in the detection area of the L2PcInstance are identified in <B>_knownPlayers</B>.
	 * In order to inform other players of this L2PcInstance state modifications, server just need to go through _knownPlayers to send Server->Client Packet<BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Send a Server->Client packet UserInfo to this L2PcInstance (Public and Private Data)</li>
	 * <li>Send a Server->Client packet CharInfo to all L2PcInstance in _KnownPlayers of the L2PcInstance (Public data only)</li><BR><BR>
	 *
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : DON'T SEND UserInfo packet to other players instead of CharInfo packet.
	 * Indeed, UserInfo packet contains PRIVATE DATA as MaxHP, STR, DEX...</B></FONT><BR><BR>
	 *
	 */
	public void broadcastUserInfo()
	{
		this.broadcastPacketToOthers(new CharInfo(this));
		this.sendPacket(new UserInfo(this));
	}

	/**
	 * Return the Alliance Identifier of the L2PcInstance.<BR><BR>
	 */
	public int getAllyId()
	{
		return _allyId;
	}

	public int getAllyCrestId()
	{
		if (getAlliance() != null && _alliance.hasAllyCrest())
			return _alliance.getAllyCrestId();
		else
			return 0;
	}

	/**
	 * Manage hit process (called by Hit Task of L2Character).<BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>If the attacker/target is dead or use fake death, notify the AI with EVT_CANCEL and send a Server->Client packet ActionFailed (if attacker is a L2PcInstance)</li>
	 * <li>If attack isn't aborted, send a message system (critical hit, missed...) to attacker/target if they are L2PcInstance </li>
	 * <li>If attack isn't aborted and hit isn't missed, reduce HP of the target and calculate reflection damage to reduce HP of attacker if necessary </li>
	 * <li>if attack isn't aborted and hit isn't missed, manage attack or cast break of the target (calculating rate, sending message...) </li><BR><BR>
	 *
	 * @param target The L2Character targeted
	 * @param damage Nb of HP to reduce
	 * @param crit True if hit is critical
	 * @param miss True if hit is missed
	 * @param soulshot True if SoulShot are charged
	 * @param shld True if shield is efficient
	 *
	 */
	protected void onHitTimer(L2Character target, int damage, boolean crit, boolean miss, boolean soulshot, boolean shld)
	{
		super.onHitTimer(target, damage, crit, miss, soulshot, shld);
		this.sendPacket(new UserInfo(this));
	}


	/**
	 * Send a Server->Client packet StatusUpdate to the L2PcInstance.<BR><BR>
	 */
	public void sendPacket(ServerBasePacket packet)
	{
		if(_isConnected)
		{
			try
			{
				if (_connection != null)
					_connection.sendPacket(packet);
			}
			catch (Exception e)
			{
				_log.log(Level.INFO, "", e);
				e.printStackTrace();
			}
		}
	}

	/**
	 * Manage Interact Task with another L2PcInstance.<BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>If the private store is a STORE_PRIVATE_SELL, send a Server->Client PrivateBuyListSell packet to the L2PcInstance</li>
	 * <li>If the private store is a STORE_PRIVATE_BUY, send a Server->Client PrivateBuyListBuy packet to the L2PcInstance</li>
	 * <li>If the private store is a STORE_PRIVATE_MANUFACTURE, send a Server->Client RecipeShopSellList packet to the L2PcInstance</li><BR><BR>
	 *
	 * @param target The L2Character targeted
	 *
	 */
	public void doInteract(L2Character target)
	{
		if (target instanceof L2PcInstance)
		{
			L2PcInstance temp = (L2PcInstance) target;
			sendPacket(new ActionFailed());

			if (temp.getPrivateStoreType() == STORE_PRIVATE_SELL)
				sendPacket(new PrivateBuyListSell(this, temp));
			else if (temp.getPrivateStoreType() == STORE_PRIVATE_BUY)
				sendPacket(new PrivateBuyListBuy(temp, this));
			else if (temp.getPrivateStoreType() == STORE_PRIVATE_MANUFACTURE)
				sendPacket(new RecipeShopSellList(this, temp));

		}
		else
		{
			// _interactTarget=null should never happen but one never knows ^^;
			if (target != null)
				target.onAction(this);
		}
	}


	/**
	 * Manage AutoLoot Task.<BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Send a System Message to the L2PcInstance : YOU_PICKED_UP_S1_ADENA or YOU_PICKED_UP_S1_S2</li>
	 * <li>Add the Item to the L2PcInstance inventory</li>
	 * <li>Send a Server->Client packet InventoryUpdate to this L2PcInstance with NewItem (use a new slot) or ModifiedItem (increase amount)</li>
	 * <li>Send a Server->Client packet StatusUpdate to this L2PcInstance with current weight</li><BR><BR>
	 *
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : If a Party is in progress, distribute Items between party members</B></FONT><BR><BR>
	 *
	 * @param target The L2ItemInstance dropped
	 *
	 */
	public void doAutoLoot(L2ItemInstance target)
	{

		// Check if the L2PcInstance is in a Party
		if  (!isInParty())
		{

			// Send a System Message to the L2PcInstance
			if (target.getItemId() == 57)
			{
				sendPacket(new SystemMessage(SystemMessage.YOU_HAVE_OBTAINED_S1_ADENA).addNumber(target.getCount()));
			}
			else
			{
				SystemMessage smsg = new SystemMessage(SystemMessage.YOU_HAVE_OBTAINED_S2_S1); // you picked up $s1$s2
				smsg.addNumber(target.getCount());
				smsg.addItemName(target.getItemId());
				sendPacket(smsg);
			}


			// Add the Item to the L2PcInstance inventory
			L2ItemInstance target2 = getInventory().addItem(target);


			// Send a Server->Client packet InventoryUpdate to this L2PcInstance with NewItem (use a new slot) or ModifiedItem (increase amount)
			InventoryUpdate iu = new InventoryUpdate();
			if (target2.getLastChange() == L2ItemInstance.ADDED)
			{
				iu.addNewItem(target);
			}
			else
			{
				iu.addModifiedItem(target2);
			}
			sendPacket(iu);

			// Send a Server->Client packet StatusUpdate to this L2PcInstance with current weight
			StatusUpdate su = new StatusUpdate(getObjectId());
			su.addAttribute(StatusUpdate.CUR_LOAD, getCurrentLoad());
			sendPacket(su);

		}
		else
		{
			if (target.getItemId() == 57)
			{
				// Distribute Adena between Party members
				getParty().distributeAdena(target);
			}
			else
			{
				// Distribute Item between Party members
				getParty().distributeItem(this, target);
			}
		}

	}


	/**
	 * Manage Pickup Task.<BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Send a Server->Client packet StopMove to this L2PcInstance </li>
	 * <li>Remove the L2ItemInstance from the world and send server->client GetItem packets </li>
	 * <li>Send a System Message to the L2PcInstance : YOU_PICKED_UP_S1_ADENA or YOU_PICKED_UP_S1_S2</li>
	 * <li>Add the Item to the L2PcInstance inventory</li>
	 * <li>Send a Server->Client packet InventoryUpdate to this L2PcInstance with NewItem (use a new slot) or ModifiedItem (increase amount)</li>
	 * <li>Send a Server->Client packet StatusUpdate to this L2PcInstance with current weight</li><BR><BR>
	 *
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : If a Party is in progress, distribute Items between party members</B></FONT><BR><BR>
	 *
	 * @param object The L2ItemInstance to pick up
	 *
	 */
	protected void doPickupItem(L2Object object)
	{
		// Set the AI Intention to AI_INTENTION_IDLE
		setIntention(CtrlIntention.AI_INTENTION_IDLE, null, null);

		// Check if the L2Object to pick up is a L2ItemInstance
		if (! (object instanceof L2ItemInstance))
		{
			// dont try to pickup anything that is not an item :)
			_log.warning("trying to pickup wrong target."+getTarget());
			return;
		}

		L2ItemInstance target = (L2ItemInstance) object;

		// Send a Server->Client packet ActionFailed to this L2PcInstance
		sendPacket(new ActionFailed());

		// Send a Server->Client packet StopMove to this L2PcInstance
		StopMove sm = new StopMove(getObjectId(), getX(), getY(), getZ(), getHeading());
		if (Config.DEBUG) _log.fine("pickup pos: "+ target.getX() + " "+target.getY()+ " "+target.getZ() );
		sendPacket(sm);


		synchronized (target)
		{
			// Check if the target to pick up is visible
			if (!target.isVisible())
			{
				// Send a Server->Client packet ActionFailed to this L2PcInstance
				sendPacket(new ActionFailed());
				return;

			}

			if ( ((isInParty() && getParty().getLootDistribution() == L2Party.ITEM_LOOTER) || !isInParty()) && !_inventory.validateCapacity(target))
			{
				sendPacket(new ActionFailed());
				sendPacket(new SystemMessage(SystemMessage.INVENTORY_SLOT_IS_FULL));
				return;
			}

			// Remove the L2ItemInstance from the world and send server->client GetItem packets
			target.pickupMe(this);


			// Check if a Party is in progress
			if  (!isInParty())
			{
				if (target.getItemId() == 57)
				{
					// Send a System message to this L2PcInstance
					SystemMessage smsg = new SystemMessage(SystemMessage.YOU_HAVE_OBTAINED_S1_ADENA); // you picked up $s1 adena
					smsg.addNumber(target.getCount());
					sendPacket(smsg);
				}
				else
				{
					// Send a System message to this L2PcInstance
					SystemMessage smsg = new SystemMessage(SystemMessage.YOU_HAVE_OBTAINED_S2_S1); // you picked up $s1$s2
					smsg.addNumber(target.getCount());
					smsg.addItemName(target.getItemId());
					sendPacket(smsg);
				}
	
				// Add the L2ItemInstance to the L2PcInstance _inventory
				L2ItemInstance target2 = getInventory().addItem(target);
	
				// Send a Server->Client packet InventoryUpdate to this L2PcInstance in order to add this item to its inventory client side
				InventoryUpdate iu = new InventoryUpdate();
				if (target2.getLastChange() == L2ItemInstance.ADDED)
				{
					iu.addNewItem(target);
				}
				else
				{
					iu.addModifiedItem(target2);
				}
				sendPacket(iu);
	
	
				// Send a Server->Client packet StatusUpdate to this L2PcInstance with its current weight
				StatusUpdate su = new StatusUpdate(getObjectId());
				su.addAttribute(StatusUpdate.CUR_LOAD, getCurrentLoad());
				sendPacket(su);
	
			}
			else
			{
				if (target.getItemId() == 57)
				{
					// Distribute Adena to Party members
					getParty().distributeAdena(target);
				}
				else
				{
					// Distribute Item to Party members
					getParty().distributeItem(this, target);
				}
			}
		}

	}


	/**
	 * Set a target.<BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Remove the L2PcInstance from the _statusListener of the old target if it was a L2Character </li>
	 * <li>Add the L2PcInstance to the _statusListener of the new target if it's a L2Character </li>
	 * <li>Target the new L2Object (add the target to the L2PcInstance _target, _knownObject and L2PcInstance to _KnownObject of the L2Object)</li><BR><BR>
	 *
	 * @param newTarget The L2Object to target
	 *
	 */
	public void setTarget(L2Object newTarget)
	{
		// Check if the new target is visible
		if (newTarget != null && !newTarget.isVisible())
			newTarget = null;


		// Get the current target
		L2Object oldTarget = getTarget();
		if (oldTarget != null)
		{
			if (oldTarget.equals(newTarget))
			{
				return; // no target change
			}

			// Remove the L2PcInstance from the _statusListener of the old target if it was a L2Character
			if (oldTarget instanceof L2Character)
			{
				((L2Character) oldTarget).removeStatusListener(this);
			}
		}

		// Add the L2PcInstance to the _statusListener of the new target if it's a L2Character
		if (newTarget != null && newTarget instanceof L2Character)
		{
			((L2Character) newTarget).addStatusListener(this);
			broadcastPacket(new TargetSelected(getObjectId(), newTarget.getObjectId(), getX(), getY(), getZ()));
		}


		// Target the new L2Object (add the target to the L2PcInstance _target, _knownObject and L2PcInstance to _KnownObject of the L2Object)
		super.setTarget(newTarget);

	}


	/**
	 * Return the active weapon instance (always equiped in the right hand).<BR><BR>
	 */
	public L2ItemInstance getActiveWeaponInstance()
	{
		return getInventory().getPaperdollItem(Inventory.PAPERDOLL_RHAND);
	}

	/**
	 * Return the active weapon item (always equiped in the right hand).<BR><BR>
	 */
	public L2Weapon getActiveWeaponItem()
	{
		L2ItemInstance weapon = getActiveWeaponInstance();

		if (weapon == null)
			return getFistsWeaponItem();

		return (L2Weapon) weapon.getItem();
	}

	/**
	 * Return the secondary weapon instance (always equiped in the left hand).<BR><BR>
	 */
	public L2ItemInstance getSecondaryWeaponInstance()
	{
		return getInventory().getPaperdollItem(Inventory.PAPERDOLL_LHAND);
	}

	/**
	 * Return the secondary weapon item (always equiped in the left hand) or the fists weapon.<BR><BR>
	 */
	public L2Weapon getSecondaryWeaponItem()
	{
		L2ItemInstance weapon = getSecondaryWeaponInstance();

		if (weapon == null)
			return getFistsWeaponItem();

		L2Item item = weapon.getItem();

		if (item instanceof L2Weapon)
			return (L2Weapon) item;

		return null;
	}

	public boolean isWearingArmor(L2ArmorType armorType)
	{
		L2ItemInstance chest = getInventory().getPaperdollItem(Inventory.PAPERDOLL_CHEST);
		if(chest != null && (L2ArmorType)chest.getItemType() == armorType)
		{
			if(chest.getItem().getBodyPart() == L2Item.SLOT_FULL_ARMOR)
				return true;
			L2ItemInstance legs = getInventory().getPaperdollItem(Inventory.PAPERDOLL_LEGS);
			if(legs != null && (L2ArmorType)legs.getItemType() == armorType)
				return true;
		}
		return false;
	}

	/**
	 * Reduce the current HP of the L2PcInstance.<BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Reduce the current HP of the L2PcInstance </li>
	 * <li>If necessary, stand up the L2PcInstance and PvP Flag the attacker </li>
	 * <li>Send a System Message to the L2PcInstance : S1_GAVE_YOU_S2_DMG </li><BR><BR>
	 *
	 * @param i The HP decrease value
	 * @param attacker The L2Character who attacks
	 *
	 */
	public void reduceCurrentHp(double i, L2Character attacker)
	{

		// Check if the L2PcInstance isn't Invulnerable
		if (this.isInvul())
			return;

		if (getPet()!=null)
		{
		 	double trans = calcStat(Stats.TRANSFER_DAMAGE_PERCENT, 0, null, null);
			if (trans>1)
			{
				trans *= i/100;
				i -= trans;
				getPet().reduceCurrentHp(trans, attacker, false);
			}
		}

		// Reduce the current HP of the L2PcInstance
		super.reduceCurrentHp(i, attacker);

		if (attacker != null)
		{
			//Check if the L2PcInstance isn't dead
			//Flag the attacker if it's a L2PcInstance outside a PvP area
			if (!isDead())
			{
				if(attacker instanceof L2PcInstance)
					((L2PcInstance)attacker).checkPvP(attacker);
				else if(attacker instanceof L2Summon)
					((L2Summon)attacker).getOwner().checkPvP(attacker);
			}

			// Send a System Message to the L2PcInstance
			SystemMessage smsg = new SystemMessage(SystemMessage.S1_HIT_YOU_FOR_S2_DAMAGE);
			if (Config.DEBUG) _log.fine("Attacker:"+attacker.getName());

			if (attacker instanceof L2MonsterInstance || attacker instanceof L2NpcInstance)
			{
				int mobId = ((L2NpcInstance) attacker).getTemplate().npcId;
				if (Config.DEBUG) _log.fine("mob id:"+mobId);
				smsg.addNpcName(mobId);
			}
			else
			{
				smsg.addString(attacker.getName());
			}
			smsg.addNumber((int)i);
			sendPacket(smsg);
		}

	}

	private void altDeathPenalty(L2Character killer)
	{
		// Reduce the Experience of the L2PcInstance in function of the calculated Death Penalty
		if(!Config.ALT_GAME_DELEVEL)
			return;
		if(ArenaManager.getInstance().checkIfInZone(this))
			return;
		if(getSkillLevel(L2Skill.SKILL_LUCKY) >= 0)
			return;

		if (killer != null && killer instanceof L2PcInstance)
			deathPenalty(atWarWith((L2PcInstance)killer));
		else
			deathPenalty(false);
	}

	public final boolean atWarWith(L2PcInstance player)
	{
		if(getClan()==null)
			return false;

  		if(player.getClan()==null)
  			return false;

		if(isAtWarWith(player.getClanId())==0)
			return false;

		if(getWantsPeace()!=0)
			return false;

		if(player.getWantsPeace()!=0)
			return false;

//		if(ZoneManager.getInstance().checkIfInZonePvP(this))
//			return false;

//		if(isFestivalParticipant())
//			return false;

		return true;
	}

	public final void doPurePk(L2PcInstance killer)
	{
		// Check if the attacker has a PK counter greater than 0
		int pkCountMulti = Math.max(killer.getPkKills()/2, 1);

		// Calculate the level difference Multiplier between attacker and killed L2PcInstance
		int lvlDiffMulti = Math.max(killer.getLevel()/this.getLevel(), 1);

		// Calculate the new Karma of the attacker : newKarma = baseKarma*pkCountMulti*lvlDiffMulti
		// Add karma to attacker and increase its PK counter
		killer.increaseKarma(Config.KARMA_MIN_KARMA * pkCountMulti * lvlDiffMulti);
		killer.setPkKills(getPkKills()+1);
	}

	public final void doKillInPeace(L2PcInstance killer)
	{			// Check if the L2PcInstance killed haven't Karma
		if(this.getKarma() <= 0)
			doPurePk(killer);
		else
			// Dead player have Karma
			// Increase the PvP Kills counter (Number of player killed during a PvP)
			killer.setPvpKills(killer.getPvpKills()+1);
	}

	public static final ArrayList<L2ItemInstance> checkAddItemToDrop(ArrayList<L2ItemInstance> array, L2ItemInstance item, double rate)
	{
//		_log.info("checkAddItemToDrop("+array+", "+item+", "+rate+");");

		if(Rnd.get()>rate)
			return array;

		if(Config.KARMA_LIST_NONDROPPABLE_ITEMS.contains(item.getItemId()))
			return array;

		array.add(item);

		return array;
	}

  /**
   * Pk/PvP situation cheching and managing after death<BR><BR>
   *
   * <B><U> Actions</U> :</B>
   * <li>Manage Karma gain for attacker and Karma loss for the killed L2PcInstance </li>
   * <li>If the killed L2PcInstance has Karma, manage Drop Item</li><BR>
   *
   * @param killer The L2Character who attacks
   *
   * @author Styx (parts of code by ootz0rz)
   *
   */
	protected void doPKPVPManage(L2Character killer)
	{
		if (killer == null)
	  		return;

		if(killer.getObjectId() == this.getObjectId())
	  		return;

		if(ZoneManager.getInstance().checkIfInZonePvP(this))
			return;

		if(killer instanceof L2Summon)
	  		killer = ((L2Summon)killer).getOwner();

		// Processing Karma/PKCount/PvPCount for killer
		if(killer instanceof L2PcInstance)
		{
	  		L2PcInstance pk = (L2PcInstance)killer;

	  		if(this.getPvpFlag() == 0 && !atWarWith(pk))
				doKillInPeace(pk);
	  		else
				// Dead player was PvP Flagged
				// Increase the PvP Kills counter (Number of player killed during a PvP)
				pk.setPvpKills(pk.getPvpKills()+1);

			// Send a Server->Client UserInfo packet to attacker with its PvP Kills Counter
	  		pk.sendPacket(new UserInfo(pk));
		}

		// KARMA LOSS FOR DEAD PLAYER
		// when a PKer gets killed by another player or a L2MonsterInstance, it loseq a certain amount of Karma based on their level.
		// this (with defaults) results in a level 1 losing about ~2 karma per death, and a lvl 70 loses about 11760 karma per death...
		// You lose karma as long as you were not in a pvp zone and you did not kill urself.
		// No item drop in PvP without Karma
		if(this.getKarma()==0 && killer instanceof L2PcInstance)
			return;

		// this formula seems to work relatively well:
		// baseKarma * thisLVL * (thisLVL/100)
		int karmaLost = (int)Math.round(Config.KARMA_LOST_BASE * getLevel()*getLevel()/100);
		// Decrease Karma of the L2PcInstance and Send it a Server->Client StatusUpdate packet with Karma and PvP Flag if necessary
		if(karmaLost<0)
			karmaLost=0;

//		if(!atWarWith(pk))

//		_log.info("1: "+Config.KARMA_DROP_GM);

		// No drop from GM's
		if(!Config.KARMA_DROP_GM && this.isGM())
	  		return;

		boolean dropFromPK = getKarma()>0
			&& getPkKills() >= Config.KARMA_PK_LIMIT
			&& !(killer instanceof L2NpcInstance);

		this.redreaseKarma(karmaLost);

		int max_drop_count = dropFromPK ? Config.KARMA_DROP_ITEM_LIMIT : Config.PLAYER_MAX_DROP_ITEMS;

//		_log.info("2: "+max_drop_count);

		double dropRate = Config.PLAYER_RATE_DROP/100.0;

		if(dropFromPK)
		{
			dropRate = Config.KARMA_DROPCHANCE_INITIAL_CHECK;

			dropRate *= ((double)getKarma())/Config.KARMA_DROP_CONSTANT;

			if(dropRate < Config.KARMA_DROPCHANCE_MINIMUM)
				dropRate = Config.KARMA_DROPCHANCE_MINIMUM;

			dropRate /= 100.0;
		}

		double dropEquipRate  = (dropFromPK ? Config.KARMA_DROPCHANCE_EQUIPMENT : Config.PLAYER_RATE_DROP_EQUIP)/100.0;
		double dropWeaponRate = (dropFromPK ? Config.KARMA_DROPCHANCE_EQUIPPED_WEAPON : Config.PLAYER_RATE_DROP_EQUIP_WEAPON)/100.0;
		double dropItemRate = Math.max(0, 1.0-(dropWeaponRate+dropWeaponRate));

//		_log.info("dropEquipRate="+dropEquipRate+"; dropWeaponRate="+dropWeaponRate+"; dropItemRate="+dropItemRate);

		ArrayList<L2ItemInstance> dropped_items = new ArrayList<L2ItemInstance>();
		// Items to check = max_drop_count * 3; (Inventory + Weapon + Equipment)
		for(int i=0, cycles=100; i<max_drop_count*3 && cycles>0 && dropped_items.size()<max_drop_count; cycles--)
		{
			L2ItemInstance random_item = _inventory.getItems()[Rnd.get(_inventory.getSize())];
			
			if(random_item.getItem().getType1() == L2Item.TYPE1_ITEM_QUESTITEM_ADENA)
				continue;
			
			if(random_item.getItem().getType2() == L2Item.TYPE2_QUEST)
				continue;

			if(dropped_items.contains(random_item))
				continue;
			
			i++;

			if(random_item.isEquipped())
			{
				if(random_item.getItem().getType2()==L2Item.TYPE2_WEAPON)
					dropped_items = checkAddItemToDrop(dropped_items, random_item, dropWeaponRate*dropRate);
				else
					dropped_items = checkAddItemToDrop(dropped_items, random_item, dropEquipRate*dropRate);
		 	}
		 	else
				dropped_items = checkAddItemToDrop(dropped_items, random_item, dropItemRate*dropRate);
		}

		// Dropping items, if present
		if(dropped_items.isEmpty())
			return;

		for(L2ItemInstance item : dropped_items)
  		{
			int count = item.getCount();
			if(item.isEquipped())
			{
				int dropslot = item.getEquipSlot();
				getInventory().unEquipItemInSlotAndRecord(dropslot);
				getInventory().setPaperdollItem(dropslot, null);
			}

			item = _inventory.dropItem(item, count);
			L2CharPosition pos = Rnd.coordsRandomize(this, Config.KARMA_RANDOM_DROP_LOCATION_LIMIT);

			item.dropMe(this, pos.x, pos.y, getZ()+100);
			sendPacket(new SystemMessage(298).addItemName(item.getItemId()));
			sendPacket(new InventoryUpdate().addRemovedItem(item));
			if(Config.DEBUG)
				_log.fine("["+getName()+"] died and dropped: "+item.getItem().getName()+" ("+count+")");
  		}
	}

	/**
	 * Kill the L2Character, Apply Death Penalty, Manage gain/loss Karma and Item Drop.<BR><BR>
	 *
	 * <B><U> Actions</U> :</B>
	 * <li>Reduce the Experience of the L2PcInstance in function of the calculated Death Penalty </li>
	 * <li>If necessary, unsummon the Pet of the killed L2PcInstance </li>
	 * <li>Kill the L2PcInstance </li><BR><BR>
	 *
	 * @param attacker The L2Character who attacks
	 *
	 */
	public void doDie(L2Character killer)
	{
		// Kill the L2PcInstance
		super.doDie(killer);

		// Dont unsummon a summon, it can kill few enemies. But pet must returned back into its item
		if (_summon != null && _summon instanceof L2PetInstance)
			_summon.unSummon(this);

		// Unsummon Cubics
		if(_cubics.size() > 0)
		{
			for (L2CubicInstance cubic : _cubics.values())
			{
				cubic.stopAction();
				cubic.cancelDisappear();
			}
			_cubics.clear();
		}

		_log.info(""+this+" killed by "+killer);

		doPKPVPManage(killer);

		// Set the PvP Flag of the L2PcInstance
		setPvpFlag(0);

		altDeathPenalty(killer);
	}

	/**
	 * Restore the Experience of the L2PcInstance and send it a Server->Client StatusUpdate packet.<BR><BR>
	 */
	public void restoreExp()
	{
		restoreExp(100.);
	}


	/**
	 * Restore the percentes of losted Experience of the L2PcInstance
	 * and send it a Server->Client StatusUpdate packet.<BR><BR>
	 */
	public void restoreExp(double percent)
	{
		if (_lostExp != 0)
		{   // Restore the Experience value
			_exp += _lostExp*(percent/100.);
			_lostExp = 0;
			while ((_exp < Experience.LEVEL[getLevel()]) && (_exp > Experience.LEVEL[1]))
			{
				// Manage the increase level task of a L2PcInstance (Max MP, Max MP, Recommandation, Expertise and beginner skills...)
				decreaseLevel();

				// Send a Server->Client packet UserInfo to the L2PcInstance
				sendPacket(new UserInfo(this));
			}
			while ((_exp >= Experience.LEVEL[getLevel() + 1]) && (_exp < Experience.LEVEL[76]))
			{
				// Manage the increase level task of a L2PcInstance (Max MP, Max MP, Recommandation, Expertise and beginner skills...)
				increaseLevel();

				// Send a Server->Client packet UserInfo to the L2PcInstance
				sendPacket(new UserInfo(this));
			}
			// Send a Server->Client StatusUpdate packet with its restored Experience
			StatusUpdate su = new StatusUpdate(getObjectId());
			su.addAttribute(StatusUpdate.EXP, _exp);
			sendPacket(su);
		}

   }


	/**
	 * Reduce the Experience (and level if necessary) of the L2PcInstance in function of the calculated Death Penalty.<BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Calculate the Experience loss </li>
	 * <li>Set the value of _lostExp </li>
	 * <li>Set the new Experience value of the L2PcInstance and Decrease its level if necessary </li>
	 * <li>Send a Server->Client StatusUpdate packet with its new Experience </li><BR><BR>
	 *
	 */
	public void deathPenalty(boolean atwar)
	{
		// TODO Need Correct Penalty

		// Get the level of the L2PcInstance
		final int lvl = getLevel();
		//The death steal you some Exp
		double percentLost = -0.07 * lvl + 6.5;

		if(percentLost < 2)
			percentLost = 2;

		if(getKarma()>0)
			percentLost *= Config.RATE_KARMA_EXP_LOST;

		if(ZoneManager.getInstance().checkIfInZonePvP(this) || isFestivalParticipant() || atwar)
			percentLost = percentLost/4.0;

		// Calculate the Experience loss
		_lostExp = (int) Math.round((Experience.LEVEL[lvl+1]-Experience.LEVEL[lvl]) * percentLost /100);

		// Get the Experience before applying penalty
//		_expBeforeDeath = getExp();

		_log.fine("is dead, so exp to remove:"+_lostExp);

		// Set the new Experience value of the L2PcInstance
		setExp(getExp()- _lostExp);

		// Decrease the level of the L2PcInstance if necessary
		if (getExp() < Experience.LEVEL[lvl])
			decreaseLevel();

		// Send a Server->Client StatusUpdate packet with its new Experience
		StatusUpdate su = new StatusUpdate(getObjectId());
		su.addAttribute(StatusUpdate.EXP, getExp());
		sendPacket(su);
	}

	/**
	 * @param b
	 */
	public void setPartyMatchingAutomaticRegistration(boolean b)
	{
		_partyMatchingAutomaticRegistration = b;
	}


	/**
	 * @param b
	 */
	public void setPartyMatchingShowLevel(boolean b)
	{
		_partyMatchingShowLevel = b;
	}


	/**
	 * @param b
	 */
	public void setPartyMatchingShowClass(boolean b)
	{
		_partyMatchingShowClass = b;
	}


	/**
	 * @param memo
	 */
	public void setPartyMatchingMemo(String memo)
	{
		_partyMatchingMemo = memo;
	}

	public boolean isPartyMatchingAutomaticRegistration()
	{
		return _partyMatchingAutomaticRegistration;
	}

	public String getPartyMatchingMemo()
	{
		return _partyMatchingMemo;
	}

	public boolean isPartyMatchingShowClass()
	{
		return _partyMatchingShowClass;
	}

	public boolean isPartyMatchingShowLevel()
	{
		return _partyMatchingShowLevel;
	}


	/**
	 * Set the L2PcInstance requester of the transaction.<BR><BR>
	 */
	public void setTransactionRequester(L2PcInstance requestor)
	{
		_currentTransactionRequester = requestor;
	}

	/**
	 * Return the L2PcInstance requester of the transaction.<BR><BR>
	 */
	public L2PcInstance getTransactionRequester()
	{
		return _currentTransactionRequester;
	}

	/**
	 * Return True if a transaction is in progress.<BR><BR>
	 */
	public boolean isTransactionInProgress()
	{
		return _currentTransactionRequester != null;
	}


	public int getDistanceToWatchObject(@SuppressWarnings("unused") L2Object object)
	{
		return 6144;
	}

	/**
	 * Add a visible L2Object to L2PcInstance _knownObjects and _knownPlayer (if necessary) and send Server-Client Packets needed to inform the L2PcInstance of its state and actions in progress.<BR><BR>
	 *
	 * <B><U> object is a L2ItemInstance </U> :</B><BR><BR>
	 * <li> Send Server-Client Packet DropItem/SpawnItem to the L2PcInstance </li><BR><BR>
	 *
	 * <B><U> object is a L2DoorInstance </U> :</B><BR><BR>
	 * <li> Send Server-Client Packets DoorInfo and DoorStatusUpdate to the L2PcInstance </li>
	 * <li> Send Server->Client packet MoveToPawn/CharMoveToLocation and AutoAttackStart to the L2PcInstance </li><BR><BR>
	 *
	 * <B><U> object is a L2NpcInstance </U> :</B><BR><BR>
	 * <li> Send Server-Client Packet NpcInfo to the L2PcInstance </li>
	 * <li> Send Server->Client packet MoveToPawn/CharMoveToLocation and AutoAttackStart to the L2PcInstance </li><BR><BR>
	 *
	 * <B><U> object is a L2Summon </U> :</B><BR><BR>
	 * <li> Send Server-Client Packet NpcInfo/PetItemList (if the L2PcInstance is the owner) to the L2PcInstance </li>
	 * <li> Send Server->Client packet MoveToPawn/CharMoveToLocation and AutoAttackStart to the L2PcInstance </li><BR><BR>
	 *
	 * <B><U> object is a L2PcInstance </U> :</B><BR><BR>
	 * <li> Send Server-Client Packet CharInfo to the L2PcInstance </li>
	 * <li> If the object has a private store, Send Server-Client Packet PrivateStoreMsgSell to the L2PcInstance </li>
	 * <li> Send Server->Client packet MoveToPawn/CharMoveToLocation and AutoAttackStart to the L2PcInstance </li><BR><BR>
	 *
	 * @param object The L2Object to add to _knownObjects and _knownPlayer
	 * @param dropper The L2Character who dropped the L2Object
	 *
	 */
	public boolean addKnownObject(L2Object object, L2Character dropper)
	{
		if(!super.addKnownObject(object, dropper))
			return false;

		if(object instanceof L2Character)
		{
			// Update the state of the L2Character object client side by sending Server->Client packet MoveToPawn/CharMoveToLocation and AutoAttackStart to the L2PcInstance
			L2Character obj = (L2Character) object;
			if(obj.getAI() != null)
				obj.getAI().describeStateToPlayer(this);
		}

		if(object.isPolymorphed() && object.getPolytype().equals("item"))
		{
			sendPacket(new SpawnItemPoly(object));
			return true;
		}

		if(object.isPolymorphed() && object.getPolytype().equals("npc"))
		{
			sendPacket(new NpcInfoPoly(object, this));
			return true;
		}

		if(object instanceof L2ItemInstance)
		{
			if (dropper != null)
				sendPacket(new DropItem((L2ItemInstance) object, dropper.getObjectId()));
			else
				sendPacket(new SpawnItem((L2ItemInstance) object));

			return true;
		}

		if(object instanceof L2DoorInstance)
		{
			sendPacket(new DoorInfo((L2DoorInstance) object));
			sendPacket(new DoorStatusUpdate((L2DoorInstance) object));
			return true;
		}

		if(object instanceof L2StaticObjectInstance)
		{
			sendPacket(new StaticObject((L2StaticObjectInstance) object));
			return true;
		}

		if (object instanceof L2NpcInstance)
		{
			sendPacket(new NpcInfo((L2NpcInstance) object, this));
			return true;
		}

		if(object instanceof L2Summon)
		{
			L2Summon summon = (L2Summon) object;

			// Check if the L2PcInstance is the owner of the Pet
			if (this.equals(summon.getOwner()))
			{
				sendPacket(new PetInfo(summon));
				if (summon instanceof L2PetInstance)
					sendPacket(new PetItemList((L2PetInstance) summon));
			}
			else
				sendPacket(new NpcInfo(summon, this));

			return true;
		}

		if(object instanceof L2PcInstance)
		{
			L2PcInstance otherPlayer = (L2PcInstance) object;
			sendPacket(new CharInfo(otherPlayer));

			if (  otherPlayer.getPrivateStoreType()	== STORE_PRIVATE_BUY
				  || otherPlayer.getPrivateStoreType() == STORE_PRIVATE_SELL
				  || otherPlayer.getPrivateStoreType() == STORE_PRIVATE_MANUFACTURE)
			{
				if (otherPlayer.getTradeList() != null)
					sendPacket(new PrivateStoreMsgSell(otherPlayer));
			}
		}

		return true;
	}

	/**
	 * Remove a L2Object from L2PcInstance _knownObjects and _knownPlayer (if necessary) and send Server-Client Packet DeleteObject to the L2PcInstance.<BR><BR>
	 *
	 * @param object The L2Object to remove from _knownObjects and _knownPlayer
	 *
	 */
	public void removeKnownObject(L2Object object)
	{
		if(object == null)
			return;

		if(object != this)
			sendPacket(new DeleteObject(object));

		super.removeKnownObject(object);
	}


	/**
	 * Manage the increase level task of a L2PcInstance (Max MP, Max MP, Recommandation, Expertise and beginner skills...).<BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Send a Server->Client System Message to the L2PcInstance : YOU_INCREASED_YOUR_LEVEL </li>
	 * <li>Send a Server->Client packet StatusUpdate to the L2PcInstance with new LEVEL, MAX_HP and MAX_MP </li>
	 * <li>Set the current HP and MP of the L2PcInstance, Launch/Stop a HP/MP/CP Regeneration Task and send StatusUpdate packet to all other L2PcInstance to inform (exclusive broadcast)</li>
	 * <li>Recalculate the party level</li>
	 * <li>Recalculate the number of Recommandation that the L2PcInstance can give</li>
	 * <li>Give Expertise skill of this level and remove beginner Lucky skill</li><BR><BR>
	 *
	 */
	public void increaseLevel()
	{
		if (Config.DEBUG) _log.finest("increasing level of " + getName());

		// Increase the level of the L2PcIstance
		if (_level < 79)
			_level++;

		// Send a Server->Client System Message to the L2PcInstance
		sendPacket(new SystemMessage(SystemMessage.YOU_HAVE_INCREASED_YOUR_LEVEL));

		// Send a Server->Client packet StatusUpdate to the L2PcInstance
		StatusUpdate su = new StatusUpdate(getObjectId());
		su.addAttribute(StatusUpdate.LEVEL, _level);
		su.addAttribute(StatusUpdate.MAX_HP, getMaxHp());
		su.addAttribute(StatusUpdate.MAX_MP, getMaxMp());
		su.addAttribute(StatusUpdate.MAX_CP, getMaxCp());
		sendPacket(su);

		// Set the current HP and MP of the L2Character, Launch/Stop a HP/MP/CP Regeneration Task and send StatusUpdate packet to all other L2PcInstance to inform (exclusive broadcast)
		setCurrentHpMp(getMaxHp(),getMaxMp());
		setCurrentCp(getMaxCp());

		// Recalculate the party level
		if (isInParty()) getParty().recalculatePartyLevel();

		// Recalculate the number of Recommandation that the L2PcInstance can give
		switch (getLevel())
		{
			case 10: setRecomLeft(getRecomLeft()+3); break;
			case 20: setRecomLeft(getRecomLeft()+3); break;
			case 30: setRecomLeft(getRecomLeft()+2); break;
			case 40: setRecomLeft(getRecomLeft()+2); break;
			case 50: setRecomLeft(getRecomLeft()+3); break;
			case 60: setRecomLeft(getRecomLeft()+5); break;
			case 70: setRecomLeft(getRecomLeft()+7); break;
			case 75: setRecomLeft(getRecomLeft()+9); break;
		}

		if (_clan != null)
			_clan.broadcastToOtherOnlineMembers(new PledgeShowMemberListAdd(this), this);
		// Give Expertise skill of this level and remove beginner Lucky skill
		rewardSkills();

	}


	/**
	 * Manage the decrease level task of a L2PcInstance (Max MP, Max MP, Recommandation, Expertise and beginner skills...).<BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Send a Server->Client packet StatusUpdate to the L2PcInstance with new LEVEL, MAX_HP and MAX_MP </li>
	 * <li>Recalculate the party level</li>
	 * <li>Recalculate the number of Recommandation that the L2PcInstance can give</li>
	 * <li>Give Expertise skill of this level</li><BR><BR>
	 *
	 */
	public void decreaseLevel()
	{

		// Recalculate the number of Recommandation that the L2PcInstance can give
		switch (getLevel())
		{
			case 10: setRecomLeft(getRecomLeft()-3); break;
			case 20: setRecomLeft(getRecomLeft()-3); break;
			case 30: setRecomLeft(getRecomLeft()-2); break;
			case 40: setRecomLeft(getRecomLeft()-2); break;
			case 50: setRecomLeft(getRecomLeft()-3); break;
			case 60: setRecomLeft(getRecomLeft()-5); break;
			case 70: setRecomLeft(getRecomLeft()-7); break;
			case 75: setRecomLeft(getRecomLeft()-9); break;
		}

		if (_level == 1)
			return;

		// Decrease the level of the L2PcIstance
		if (Config.DEBUG) _log.finest("decreasing level of " + getName());
		_level--;

		// Send a Server->Client packet StatusUpdate to the L2PcInstance
		StatusUpdate su = new StatusUpdate(getObjectId());
		su.addAttribute(StatusUpdate.LEVEL, _level);
		su.addAttribute(StatusUpdate.MAX_HP, getMaxHp());
		su.addAttribute(StatusUpdate.MAX_MP, getMaxMp());
		sendPacket(su);

		// Recalculate the party level
		if (isInParty()) getParty().recalculatePartyLevel();

		if (_clan != null)
			_clan.broadcastToOtherOnlineMembers(new PledgeShowMemberListAdd(this), this);
		// Give Expertise skill of this level
		rewardSkills();

	}


	/**
	 * Stop the HP/MP/CP Regeneration task.<BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Set the RegenActive flag to False </li>
	 * <li>Stop the HP/MP/CP Regeneration task </li><BR><BR>
	 *
	 */
	public void stopAllTimers()
	{
		stopHpMpRegeneration();
	}


	/**
	 * Return the L2Summon of the L2PcInstance or null.<BR><BR>
	 */
	public L2Summon getPet()
	{
		return _summon;
	}

	/**
	 * Set the L2Summon of the L2PcInstance.<BR><BR>
	 */
	public void setPet(L2Summon summon)
	{
		_summon = summon;
	}


	/**
	 * Manage the delete task of a L2PcInstance (Leave Party, Unsummon pet, Save its inventory in the database, Remove it from the world...).<BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>If the L2PcInstance is in observer mode, set its position to its position before entering in observer mode </li>
	 * <li>Set the online Flag to True or False and update the characters table of the database with online status and lastAccess </li>
	 * <li>Stop the HP/MP/CP Regeneration task </li>
	 * <li>Cancel Crafting, Attak or Cast </li>
	 * <li>Remove the L2PcInstance from the world </li>
	 * <li>Stop Party and Unsummon Pet </li>
	 * <li>Update database with items in its inventory and remove them from the world </li>
	 * <li>Remove all L2Object from _knownObjects and _knownPlayer of the L2Character then cancel Attak or Cast and notify AI </li>
	 * <li>Close the connection with the client </li><BR><BR>
	 *
	 */
	public void deleteMe()
	{
		// if 'this' is in transaction with someone, he should give up the transaction
		if(isTransactionInProgress())
		{
			L2PcInstance requestor = getTransactionRequester();
			if(requestor == this)
			{
				requestor.setTransactionRequester(null);
			}
		}

		// Check if the L2PcInstance is in observer mode to set its position to its position before entering in observer mode
		if (inObserverMode())
			setXYZ(_obsX, _obsY, _obsZ);

		// Set the online Flag to True or False and update the characters table of the database with online status and lastAccess (called when login and logout)
		try { setOnlineStatus(false); } catch (Throwable t) {_log.log(Level.SEVERE, "deletedMe()", t); }

		// Stop the HP/MP/CP Regeneration task (scheduled tasks)
		try { stopAllTimers(); } catch (Throwable t) {_log.log(Level.SEVERE, "deletedMe()", t); }

		// Stop crafting, if in progress
		try { RecipeController.getInstance().requestMakeItemAbort(this); } catch (Throwable t) {_log.log(Level.SEVERE, "deletedMe()", t); }

		// Cancel Attak or Cast
		try { setTarget(null); } catch (Throwable t) {_log.log(Level.SEVERE, "deletedMe()", t); }

		// Remove the L2PcInstance from the world
		if (isVisible())
			try { decayMe(); } catch (Throwable t) {_log.log(Level.SEVERE, "deletedMe()", t); }

		// If a Party is in progress, leave it
		if (isInParty()) try { leaveParty(); } catch (Throwable t) {_log.log(Level.SEVERE, "deletedMe()", t); }

		// If the L2PcInstance has Pet, unsummon it
		if (getPet() != null)
		{
			try { getPet().unSummon(this); } catch (Throwable t) {_log.log(Level.SEVERE, "deletedMe()", t); }// returns pet to control item
		}

		if(getClanId() != 0 && getClan() != null && getClan().getClanMember(getName())!=null)
		{
			// set the status for pledge member list to OFFLINE
			try
			{
				getClan().getClanMember(getName()).setPlayerInstance(null);
			} catch (Throwable t) {_log.log(Level.SEVERE, "deletedMe()", t); }
		}

		if (getTransactionRequester() != null)
		{
			// deals with sudden exit in the middle of transaction
			setTransactionRequester(null);
		}

		// Update database with items in its inventory and remove them from the world
		try { getInventory().deleteMe(); } catch (Throwable t) {_log.log(Level.SEVERE, "deletedMe()", t); }

		// Remove all L2Object from _knownObjects and _knownPlayer of the L2Character then cancel Attak or Cast and notify AI
		try { removeAllKnownObjects(); } catch (Throwable t) {_log.log(Level.SEVERE, "deletedMe()", t); }

		// Close the connection with the client
		try { setNetConnection(null); } catch (Throwable t) {_log.log(Level.SEVERE, "deletedMe()", t); }

		clearPathNodes();

		if (getClanId() > 0)
			ClanTable.getInstance().getClan(getClanId()).broadcastToOnlineMembers(new PledgeShowMemberListAdd(this));

		// Remove L2Object object from _allObjects of L2World
		L2World.removeObject(this);
	}

	/**
	 * Return the Expertise Penalty (when using a too high grade weapon or armor).<BR><BR>
	 */
//	public float getExpertisePenalty(int grade)
//	{
//		if (expertiseIndex >= grade)
//		return 1;
//		int d = EXPERTISE_LEVELS[grade] - getLevel();
//		int dL = EXPERTISE_LEVELS[grade] - EXPERTISE_LEVELS[grade-1];
//		return 1.2f + 0.8f * d / dL;
//	}

	/**
	 * Return the Weapon Expertise Penalty (when using a too high grade weapon).<BR><BR>
	 */
//	public float getWeaponExpertisePenalty()
//	{
//		L2Weapon wpn = getActiveWeaponItem();
//
//		if (wpn == null)
//			return 1.f;
//
//		int grade = wpn.getItemGrade();
//
//		if (grade <= 0)
//			return 1.f;
//
//		float penalty = getExpertisePenalty(grade);
//			return penalty;
//	}

	/**
	 * Calculate the Expertise Penalty of a paper doll Item.<BR><BR>
	 */
//	private float addPenalty(float scale, int ppd)
//	{
//		L2ItemInstance item = getInventory().getPaperdollItem(ppd);

//		if (item == null)
//			return 0.f;

//		int grade = item.getItem().getItemGrade();

//		if (grade <= 0)
//			return 0.f;
//
//		return scale*(getExpertisePenalty(grade) - 1.f);
//	}

	/**
	 * Return the Armour Expertise Penalty (when using a too high grade armour).<BR><BR>
	 */
//	public float getArmourExpertisePenalty()
//	{
//		float penalty = 1.f;
//		penalty += addPenalty(0.2f, Inventory.PAPERDOLL_HEAD);
//		penalty += addPenalty(0.2f, Inventory.PAPERDOLL_GLOVES);
//		penalty += addPenalty(0.2f, Inventory.PAPERDOLL_FEET);
//		penalty += addPenalty(0.5f, Inventory.PAPERDOLL_LEGS);
//		penalty += addPenalty(0.8f, Inventory.PAPERDOLL_CHEST);

//		return penalty;

//	}

	/**
	 * Set the _tradeList object of the L2PcInstance.<BR><BR>
	 */
	public void setTradeList(L2TradeList x)
	{
		_tradeList = x;
	}

	/**
	 * Return the _tradeList object of the L2PcInstance.<BR><BR>
	 */
	public L2TradeList getTradeList()
	{
		return _tradeList;
	}

	/**
	 * Set the _sellList object of the L2PcInstance.<BR><BR>
	 */
	public void setSellList(ArrayList<TradeItem> x)
	{
		_sellList = x;
	}

	/**
	 * Return the _sellList object of the L2PcInstance.<BR><BR>
	 */
	public ArrayList<TradeItem> getSellList()
	{
		return _sellList;
	}


	/**
	 * Return the _createList object of the L2PcInstance.<BR><BR>
	 */
	public L2ManufactureList getCreateList()
	{
		return _createList;
	}

	/**
	 * Set the _createList object of the L2PcInstance.<BR><BR>
	 */
	public void setCreateList(L2ManufactureList x)
	{
		_createList = x;
	}

	/**
	 * Set the _buyList object of the L2PcInstance.<BR><BR>
	 */
	public void setBuyList(ArrayList<TradeItem> x)
	{
		_buyList = x;
	}

	/**
	 * Return the _buyList object of the L2PcInstance.<BR><BR>
	 */
	public ArrayList<TradeItem> getBuyList()
	{
		return _buyList;
	}

	/**
	 * Set the Private Store type of the L2PcInstance.<BR><BR>
	 *
	 * <B><U> Values </U> :</B><BR><BR>
	 * <li>0 : STORE_PRIVATE_NONE</li>
	 * <li>1 : STORE_PRIVATE_SELL</li>
	 * <li>2 : sellmanage</li><BR>
	 * <li>3 : STORE_PRIVATE_BUY</li><BR>
	 * <li>4 : buymanage</li><BR>
	 * <li>5 : STORE_PRIVATE_MANUFACTURE</li><BR>
	 *
	 */
	public void setPrivateStoreType(int type)
	{
		_privatestore = type;
	}

	/**
	 * Return the Private Store type of the L2PcInstance.<BR><BR>
	 *
	 * <B><U> Values </U> :</B><BR><BR>
	 * <li>0 : STORE_PRIVATE_NONE</li>
	 * <li>1 : STORE_PRIVATE_SELL</li>
	 * <li>2 : sellmanage</li><BR>
	 * <li>3 : STORE_PRIVATE_BUY</li><BR>
	 * <li>4 : buymanage</li><BR>
	 * <li>5 : STORE_PRIVATE_MANUFACTURE</li><BR>
	 *
	 */
	public int getPrivateStoreType()
	{
		return _privatestore;
	}

	/**
	 * Set the _skillLearningClassId object of the L2PcInstance.<BR><BR>
	 */
	public void setSkillLearningClassId(ClassId classId)
	{
		_skillLearningClassId = classId;
	}

	/**
	 * Return the _skillLearningClassId object of the L2PcInstance.<BR><BR>
	 */
	public ClassId getSkillLearningClassId()
	{
		return _skillLearningClassId;
	}

	/**
	 * Set the _clan object, _clanId, _clanLeader Flag and title of the L2PcInstance.<BR><BR>
	 */
	public void setClan(L2Clan clan)
	{
		_clan = clan;

		if (clan == null)
		{
			_clanId = 0;
			_clanLeader = false;
			clanPrivileges = 0;
			return;
		}

		if (!clan.isMember(getName()))
		{
			// char has been kicked from clan
			setClan(null);
			setTitle("");
			return;
		}

		_clanId = clan.getClanId();
		_clanLeader = getObjectId() == clan.getLeaderId();
		setTitle("");

	}

	/**
	 * Return the _clan object of the L2PcInstance.<BR><BR>
	 */
	public L2Clan getClan()
	{
		return _clan;
	}
	
	public void setAlliance(L2Alliance alliance)
	{
		_alliance = alliance;
		getClan().setAlliance(alliance);
		if (alliance == null)
		{
			_allyId = 0;
			_allyLeader = false;
			return;
		}
		
		_allyLeader = getObjectId() == alliance.getLeader().getLeaderId();
		_allyId = alliance.getAllyId();
	}
	
	public L2Alliance getAlliance()
	{
		return _alliance;
	}
	/**
	 * Return True if the L2PcInstance is the leader of its clan.<BR><BR>
	 */
	public boolean isClanLeader()
	{
		return _clanLeader;
	}

	public boolean isAllyLeader()
	{
		return _allyLeader;
	}
	
	/**
	 * Reduce the number of arrows owned by the L2PcInstance and send it Server->Client Packet InventoryUpdate or ItemList (to unequip if the last arrow was consummed).<BR><BR>
	 */
	protected void reduceArrowCount()
	{
		L2ItemInstance arrows = getInventory().destroyItem(getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_LHAND), 1);

		if (Config.DEBUG) _log.fine("arrow count:" + (arrows==null? 0 : arrows.getCount()));

		if (arrows == null || arrows.getCount() == 0)
		{
			getInventory().unEquipItemInSlot(Inventory.PAPERDOLL_LHAND);
			_arrowItem = null;

			if (Config.DEBUG) _log.fine("removed arrows count");
			sendPacket(new ItemList(this,false));
		}
		else
		{
			InventoryUpdate iu = new InventoryUpdate();
			iu.addModifiedItem(arrows);
			sendPacket(iu);
		}
	}


	/**
	 * Equip arrows needed in left hand and send a Server->Client packet ItemList to the L2PcINstance then return True.<BR><BR>
	 */
	protected boolean checkAndEquipArrows()
	{
		// Check if nothing is equiped in left hand
		if (getInventory().getPaperdollItem(Inventory.PAPERDOLL_LHAND) == null)
		{
			// Get the L2ItemInstance of the arrows needed for this bow
			_arrowItem = getInventory().findArrowForBow(getActiveWeaponItem());

			if ( _arrowItem != null)
			{
				// Equip arrows needed in left hand
				getInventory().setPaperdollItem(Inventory.PAPERDOLL_LHAND, _arrowItem);

				// Send a Server->Client packet ItemList to this L2PcINstance to update left hand equipement
				ItemList il = new ItemList(this, false);
				sendPacket(il);
			}
		}
		else
		{
			// Get the L2ItemInstance of arrows equiped in left hand
			_arrowItem = getInventory().getPaperdollItem(Inventory.PAPERDOLL_LHAND);
		}

		return _arrowItem != null;
	}


	/**
	 * Return True if the L2PcInstance use a dual weapon.<BR><BR>
	 */
	public boolean isUsingDualWeapon()
	{
		L2Weapon weaponItem = getActiveWeaponItem();
		if (weaponItem.getItemType() == L2WeaponType.DUAL)
		{
			return true;
		}
		else if (weaponItem.getItemType() == L2WeaponType.DUALFIST)
		{
			return true;
		}
		else if (weaponItem.getItemId() == 248) // orc fighter fists
		{
			return true;
		}
		else if (weaponItem.getItemId() == 252) // orc mage fists
		{
			return true;
		}
		else
		{
			return false;
		}
	}

	public void setUptime(long time)
	{
		_uptime = time;
	}

	public long getUptime()
	{
		return System.currentTimeMillis()-_uptime;
	}

	/**
	 * Set the invulnerability Flag of the L2PcInstance.<BR><BR>
	 */
	public void setIsInvul(boolean b)
	{
	 	_isInvul = b;
	}

	/**
	 * Return True if the L2PcInstance is invulnerable.<BR><BR>
	 */
	public boolean isInvul()
	{
		return _isInvul;
	}


	/**
	 * Return True if the L2PcInstance has a Party in progress.<BR><BR>
	 */
	public boolean isInParty()
	{
		return _party != null;
	}


	/**
	 * Set the _party object of the L2PcInstance (without joining it).<BR><BR>
	 */
	public void setParty(L2Party party)
	{
		_party = party;
	}

	/**
	 * Set the _party object of the L2PcInstance AND join it.<BR><BR>
	 */
	public void joinParty(L2Party party)
	{
		if (party != null)
		{
			party.addPartyMember(this);
			_party = party;
		}
	}


	/**
	 * Manage the Leave Party task of the L2PcInstance.<BR><BR>
	 */
	public void leaveParty()
	{
		if (isInParty())
		{
			_party.removePartyMember(this);
			_party = null;
		}
	}

	/**
	 * Return the _party object of the L2PcInstance.<BR><BR>
	 */
	public L2Party getParty()
	{
		return _party;
	}

	/*
	public L2Object getNewTarget()
	{
		return _newTarget;
	}
	public void setNewTarget(L2Object object)
	{
		_newTarget = object;
	}
	*/

	/**
	 * Set the _isGm Flag of the L2PcInstance.<BR><BR>
	 */
	public void setIsGM(boolean status)
	{
		_isGm = status;
	}

	/**
	 * Return True if the L2PcInstance is a GM.<BR><BR>
	 */
	public boolean isGM()
	{
		return _isGm;
	}

	/**
	 * Manage a cancel cast task for the L2PcInstance.<BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Set the Intention of the AI to AI_INTENTION_IDLE </li>
	 * <li>Enable all skills (set _allSkillsDisabled to False) </li>
	 * <li>Send a Server->Client Packet MagicSkillCanceld to the L2PcInstance and all L2PcInstance in the _KnownPlayers of the L2Character (broadcast) </li><BR><BR>
	 *
	 */
	public void cancelCastMagic()
	{
		// Set the Intention of the AI to AI_INTENTION_IDLE
		setIntention(CtrlIntention.AI_INTENTION_IDLE, null, null);

		// Enable all skills
		enableAllSkills();

		// Send a Server->Client Packet MagicSkillCanceld to the L2PcInstance and all L2PcInstance in the _KnownPlayers of the L2Character (broadcast)
		broadcastPacket(new MagicSkillCanceld(getObjectId()));

	}

	/**
	 * Set the _accessLevel of the L2PcInstance.<BR><BR>
	 */
	public void setAccessLevel(int level)
	{
		_accessLevel = level;
		if (_accessLevel > 0 || Config.EVERYBODY_HAS_ADMIN_RIGHTS)
		{
			setIsGM((true));
		}
	}

	public void setAccountAccesslevel(int level)
	{
		java.sql.Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("UPDATE accounts SET access_level=? WHERE login=?");
			statement.setInt(1, level);
			statement.setString(2, getAccountName());
			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			_log.warning("could not set account access level:"+e);
		}
		finally
		{
			try { con.close(); } catch (Exception e) {}
		}
	}

	/**
	 * Return the _accessLevel of the L2PcInstance.<BR><BR>
	 */
	public int getAccessLevel()
	{
		if (Config.EVERYBODY_HAS_ADMIN_RIGHTS && _accessLevel <= 200)
			return 200;
		return _accessLevel;
	}


	public double getLevelMod()
	{
		return (100.0 - 11 + getLevel()) / 100.0;
	}

	/**
	 * Update Stats of the L2PcInstance client side by sending Server->Client packet UserInfo/StatusUpdate to this L2PcInstance and CharInfo/StatusUpdate to all L2PcInstance in its _KnownPlayers (broadcast).<BR><BR>
	 */
	public void updateStats()
	{
		super.updateStats();
		calcExpertisePenalty();
		// Send a Server->Client packet UserInfo to this L2PcInstance and CharInfo to all L2PcInstance in its _KnownPlayers (broadcast)
		this.broadcastUserInfo();

		// Send a Server->Client StatusUpdate packet with Karma to the L2PcInstance and to all L2PcInstance in its _KnownPlayers (broadcast)
		updateKarma();
	}


	/**
	 * Send a Server->Client StatusUpdate packet with Karma and PvP Flag to the L2PcInstance and all L2PcInstance to inform (broadcast).<BR><BR>
	 */
	public void setKarmaFlag(int flag)
	{
		// Send a Server->Client StatusUpdate packet with Karma and PvP Flag to the L2PcInstance and all L2PcInstance to inform (broadcast)
		StatusUpdate su = new StatusUpdate(getObjectId());
		su.addAttribute(StatusUpdate.KARMA, _karma);
		su.addAttribute(StatusUpdate.PVP_FLAG, flag);
		broadcastPacket(su);
	}


	/**
	 * Send a Server->Client StatusUpdate packet with Karma to the L2PcInstance and all L2PcInstance to inform (broadcast).<BR><BR>
	 */
	public void updateKarma()
	{
		// Send a Server->Client StatusUpdate packet with Karma to the L2PcInstance
		StatusUpdate su = new StatusUpdate(getObjectId());
		su.addAttribute(StatusUpdate.KARMA, _karma);
		broadcastPacket(su);
	}


	/**
	 * Set the online Flag to True or False and update the characters table of the database with online status and lastAccess (called when login and logout).<BR><BR>
	 */
	public void setOnlineStatus(boolean isOnline)
	{
		if(_isOnline != isOnline)
			_isOnline   = isOnline;

		// Update the characters table of the database with online status and lastAccess (called when login and logout)
		updateOnlineStatus();
	}


	/**
	 * Update the characters table of the database with online status and lastAccess of this L2PcInstance (called when login and logout).<BR><BR>
	 */
	public void updateOnlineStatus()
	{
		java.sql.Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("UPDATE characters SET online=?, lastAccess=? WHERE obj_id=?");
			statement.setInt(1, isOnline());
			statement.setLong(2, System.currentTimeMillis()/1000);
			statement.setInt(3, getObjectId());
			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			_log.warning("could not set char online status:"+e);
		}
		finally
		{
			try { con.close(); } catch (Exception e) {}
		}
	}


	/**
	 * Decrease Karma of the L2PcInstance and Send it StatusUpdate packet with Karma and PvP Flag (broadcast).<BR><BR>
	 */
	public void increaseKarma(long add_karma)
	{
		long new_karma = _karma + add_karma;

		if(new_karma > Integer.MAX_VALUE)
			new_karma = Integer.MAX_VALUE;

		if(_karma == 0 && new_karma > 0)
		{
			_karma = (int)new_karma;
			for(L2Character cha : getKnownCharacters(L2World.PLAYER_VISIBLE_TILES * L2World.TILE_SIZE))
				if(cha instanceof L2GuardInstance
						&& ((L2GuardInstance)cha).getIntention() == CtrlIntention.AI_INTENTION_IDLE)
					((L2GuardInstance)cha).setIntention(CtrlIntention.AI_INTENTION_ACTIVE,null, null);
		}
		else
			_karma = (int)new_karma;

		// Send a Server->Client StatusUpdate packet with Karma to the L2PcInstance and all L2PcInstance to inform (broadcast)
		updateKarma();
	}


	/**
	 * Decrease Karma of the L2PcInstance and Send it StatusUpdate packet with Karma and PvP Flag (broadcast).<BR><BR>
	 *
	 * @param i : The loss Karma value
	 *
	 */
	public void redreaseKarma(int i)
	{
		_karma -= i;

		if (_karma <= 0)
		{
			_karma = 0;

			// Send a Server->Client StatusUpdate packet with Karma and PvP Flag to the L2PcInstance and all L2PcInstance to inform (broadcast)
			setKarmaFlag(0);

			return;
		}

		// Send a Server->Client StatusUpdate packet with Karma to the L2PcInstance and all L2PcInstance to inform (broadcast)
		updateKarma();

	}

	/**
	 * Create a new player in the characters table of the database.<BR><BR>
	 */
	private boolean createDb()
	{
		java.sql.Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement;
			statement = con.prepareStatement(
					"INSERT INTO characters " +
					"(account_name,obj_Id,char_name,level,maxHp,curHp,maxCp,curCp,maxMp,curMp," +
					"acc,crit,evasion,mAtk,mDef,mSpd,pAtk,pDef,pSpd,runSpd,walkSpd," +
					"str,con,dex,_int,men,wit,face,hairStyle,hairColor,sex," +
					"movement_multiplier,attack_speed_multiplier,colRad,colHeight," +
					"exp,sp,karma,pvpkills,pkkills,clanid,maxload,race,classid,deletetime," +
					"cancraft,title,allyId,accesslevel,online,clan_privs,wantspeace,deleteclan, nochannel) " +
					"values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
			statement.setString(1, _accountName);
			statement.setInt(2, getObjectId());
			statement.setString(3, getName());
			statement.setInt(4, getLevel());
			statement.setInt(5, getMaxHp());
			statement.setDouble(6, getCurrentHp());
			statement.setInt(7, getMaxCp());
			statement.setDouble(8, getCurrentCp());
			statement.setInt(9, getMaxMp());
			statement.setDouble(10, getCurrentMp());
			statement.setInt(11, getAccuracy());
			statement.setInt(12, getCriticalHit(null, null));
			statement.setInt(13, getEvasionRate(null));
			statement.setInt(14, getMAtk(null, null));
			statement.setInt(15, getMDef(null, null));
			statement.setInt(16, getMAtkSpd());
			statement.setInt(17, getPAtk(null));
			statement.setInt(18, getPDef(null));
			statement.setInt(19, getPAtkSpd());
			statement.setInt(20, getRunSpeed());
			statement.setInt(21, getWalkSpeed());
			statement.setInt(22, getSTR());
			statement.setInt(23, getCON());
			statement.setInt(24, getDEX());
			statement.setInt(25, getINT());
			statement.setInt(26, getMEN());
			statement.setInt(27, getWIT());
			statement.setInt(28, getFace());
			statement.setInt(29, getHairStyle());
			statement.setInt(30, getHairColor());
			statement.setInt(31, getSex());
			statement.setDouble(32, 1/*getMovementMultiplier()*/);
			statement.setDouble(33, 1/*getAttackSpeedMultiplier()*/);
			statement.setDouble(34, getTemplate().collisionRadius/*getCollisionRadius()*/);
			statement.setDouble(35, getTemplate().collisionHeight/*getCollisionHeight()*/);
			statement.setInt(36, getExp());
			statement.setInt(37, getSp());
			statement.setInt(38, getKarma());
			statement.setInt(39, getPvpKills());
			statement.setInt(40, getPkKills());
			statement.setInt(41, getClanId());
			statement.setInt(42, getMaxLoad());
			statement.setInt(43, getRace().ordinal());
			statement.setInt(44, getClassId().getId());
			statement.setInt(45, getDeleteTimer()/1000);
			statement.setInt(46, getCanCraft() ? 1 : 0);
			statement.setString(47, getTitle());
			statement.setInt(48, getAllyId());
			statement.setInt(49, getAccessLevel());
			statement.setInt(50, isOnline());
			statement.setInt(51, getClanPrivileges());
			statement.setInt(52, getWantsPeace());
			statement.setLong(53, getDeleteClanTime()/1000);
			statement.setLong(54, getNoChannel()/1000); // Nochannel
			statement.executeUpdate();
			statement.close();
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "could not insert char data:", e);
			return false;
		}
		finally
		{
			try { con.close(); } catch (Exception e) {}
		}
		return true;
	}


	/**
	 * Retrieve a L2PcInstance from the characters table of the database and add it in _allObjects of the L2World.<BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Retrieve the L2PcInstance from the characters table of the database </li>
	 * <li>Add the L2PcInstance object in _allObjects </li>
	 * <li>Set the x,y,z position of the L2PcInstance and make it invisible</li>
	 * <li>Update the overloaded status of the L2PcInstance</li><BR><BR>
	 *
	 * @param objectId Identifier of the object to initialized
	 *
	 * @return The L2PcInstance loaded from the database
	 *
	 */
	private static L2PcInstance restore(int objectId)
	{
		L2PcInstance player = null;
		java.sql.Connection con = null;
		try
		{
			// Retrieve the L2PcInstance from the characters table of the database
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("SELECT account_name, obj_Id, char_name, level, maxHp, curHp, maxCp, curCp, maxMp, curMp, acc, crit, evasion, mAtk, mDef, mSpd, pAtk, pDef, pSpd, runSpd, walkSpd, str, con, dex, _int, men, wit, face, hairStyle, hairColor, sex, heading, x, y, z, movement_multiplier, attack_speed_multiplier, colRad, colHeight, exp, sp, karma, pvpkills, pkkills, clanid, maxload, race, classid, deletetime, cancraft, title, allyId, rec_have, rec_left, accesslevel, online, char_slot, lastAccess, clan_privs, wantspeace, deleteclan, nochannel FROM characters WHERE obj_id=?");
			statement.setInt(1, objectId);
			ResultSet rset = statement.executeQuery();

			while (rset.next())
			{
				final int classId = rset.getInt("classid");
				final boolean female = rset.getInt("sex")!=0;
				final L2PcTemplate template = CharTemplateTable.getInstance().getTemplate(classId, female);
				player = new L2PcInstance(objectId, template);
				player._accountName = rset.getString("account_name");
				player.setName(rset.getString("char_name"));
				player._level = rset.getInt("level");
				//player.setMaxHp(rset.getInt("maxHp"));
				//player.setBaseMaxMp(rset.getInt("maxMp"));
				//player.setAccuracy(rset.getInt("acc"));
				//player.setBaseCriticalHit(rset.getInt("crit"));
				//player.setEvasionRate(rset.getInt("evasion"));
				//player.setMagicalAttack(rset.getInt("mAtk"));
				//player.setMagicalDefense(rset.getInt("mDef"));
				//player.setMagicalSpeed(rset.getInt("mSpd"));
				//player.setPhysicalAttack(rset.getInt("pAtk"));
				//player.setPhysicalDefense(rset.getInt("pDef"));
				//player.setPhysicalSpeed(rset.getInt("pSpd"));
				//player.setRunSpeed(rset.getInt("runSpd"));
				//player.setWalkSpeed(rset.getInt("walkSpd"));
				//player.setStr(rset.getInt("str"));
				//player.setCon(rset.getInt("con"));
				//player.setDex(rset.getInt("dex"));
				//player.setInt(rset.getInt("_int"));
				//player.setMen(rset.getInt("men"));
				//player.setWit(rset.getInt("wit"));

				player.setFace(rset.getInt("face"));
				player.setHairStyle(rset.getInt("hairStyle"));
				player.setHairColor(rset.getInt("hairColor"));
				player.setClanPrivileges(rset.getInt("clan_privs"));
				player.setWantsPeace(rset.getInt("wantspeace"));

				player.setHeading(rset.getInt("heading"));

				//player.setMovementMultiplier(rset.getDouble("movement_multiplier"));
				//player.setAttackSpeedMultiplier(rset.getDouble("attack_speed_multiplier"));
				//player.setCollisionRadius(rset.getDouble("colRad"));
				//player.setCollisionHeight(rset.getDouble("colHeight"));

				int exp = rset.getInt("exp");

				if (player._level > 78)
					player._level = 78;

				// correct exp if the level was manipulated in the database
				if (exp < Experience.LEVEL[player.getLevel()] ||
					exp > Experience.LEVEL[player.getLevel()+1])
				{
					exp = Experience.LEVEL[player.getLevel()];
				}
				player.setExp(exp);
				player.setSp(rset.getInt("sp"));
				player.setKarma(rset.getInt("karma"));
				player.setPvpKills(rset.getInt("pvpkills"));
				player.setPkKills(rset.getInt("pkkills"));

				player.setDeleteClanTime(rset.getLong("deleteclan")*1000);
				if (player.getDeleteClanTime() > 0 && player.canCreateClan())
					player.setDeleteClanTime(0);

				// Nochannel
				player._NoChannel = rset.getLong("nochannel")*1000;
				if(player._NoChannel > 0 && player._NoChannel < System.currentTimeMillis())
					player._NoChannel = 0;

				int clanId	= rset.getInt("clanid");
				if (clanId > 0)
				{
					player.setClan(ClanTable.getInstance().getClan(clanId));
				}
				//player.setMaxLoad(rset.getInt("maxload"));
				//player.setRace(Race.values()[rset.getInt("race")]);
				player.setDeleteTimer(rset.getInt("deletetime")*1000);
				//player.setCanCraft(rset.getInt("cancraft"));

				player.setTitle(rset.getString("title"));
				//setCrestId(rset.getInt());
				int allyId	= rset.getInt("allyid");
				if (allyId > 0)
				{
					player.setAlliance(ClanTable.getInstance().getAlliance(allyId));
				}
				player.setAccessLevel(rset.getInt("accesslevel"));
				player.setFistsWeaponItem(player.findFistsWeaponItem(classId));
				player.setUptime(System.currentTimeMillis());

				player.setCurrentHp(rset.getDouble("curHp"));
				player.setCurrentCp(rset.getDouble("curCp"));
				player.setCurrentMp(rset.getDouble("curMp"));
				player.setRecomHave(rset.getInt("rec_have"));
				player.setRecomLeft(rset.getInt("rec_left"));
				player.rewardSkills();

				// Add the L2PcInstance object in _allObjects
				L2World.storeObject(player);

				// Set the x,y,z position of the L2PcInstance and make it invisible
				player.setXYZInvisible(rset.getInt("x"), rset.getInt("y"), rset.getInt("z"));

				PreparedStatement stmt = con.prepareStatement("SELECT obj_Id, char_name FROM characters WHERE account_name=? AND obj_Id<>?");
				stmt.setString(1, player._accountName);
				stmt.setInt(2, objectId);
				ResultSet chars = stmt.executeQuery();
				while (chars.next())
				{
					Integer charId = chars.getInt("obj_Id");
					String charName = chars.getString("char_name");
					player._chars.put(charId, charName);
				}

				break;
			}

			rset.close();
			statement.close();

			// Update the overloaded status of the L2PcInstance
			player.refreshOverloaded();

			//updateEvasion();
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "could not restore char data:", e);
		}
		finally
		{
			try { con.close(); } catch (Exception e) {}
		}
		return player;
	}


	/**
	 * Update L2PcInstance stats in the characters table of the database.<BR><BR>
	 */
	public void store()
	{
		java.sql.Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement;
		//update client coords, if these look like true
		if (getDistance(getClientX(),getClientY())<100){
		setXYZ(getClientX(),getClientY(),getZ());
		//setHeading(getClientHeading());
		}

			statement = con.prepareStatement(
					"UPDATE characters SET " +
					"level=?,maxHp=?,curHp=?,maxCp=?,curCp=?,maxMp=?,curMp=?," +
					"str=?,con=?,dex=?,_int=?,men=?,wit=?,face=?,hairStyle=?,hairColor=?," +
					"heading=?,x=?,y=?,z=?,exp=?,sp=?,karma=?,pvpkills=?,pkkills=?," +
					"rec_have=?,rec_left=?,clanid=?,maxload=?,race=?,classid=?,deletetime=?," +
					"title=?,allyId=?,accesslevel=?,online=?,clan_privs=?,wantspeace=?,deleteclan=?,nochannel=? " +
					"WHERE obj_id=?");
			statement.setInt(1, getLevel());
			statement.setInt(2, getMaxHp());
			statement.setDouble(3, getCurrentHp());
			statement.setInt(4, getMaxCp());
			statement.setDouble(5, getCurrentCp());
			statement.setInt(6, getMaxMp());
			statement.setDouble(7, getCurrentMp());
			statement.setInt(8, getSTR());
			statement.setInt(9, getCON());
			statement.setInt(10, getDEX());
			statement.setInt(11, getINT());
			statement.setInt(12, getMEN());
			statement.setInt(13, getWIT());
			statement.setInt(14, getFace());
			statement.setInt(15, getHairStyle());
			statement.setInt(16, getHairColor());
			statement.setInt(17, getHeading());
			statement.setInt(18, getX());
			statement.setInt(19, getY());
			statement.setInt(20, getZ());
			statement.setInt(21, getExp());
			statement.setInt(22, getSp());
			statement.setInt(23, getKarma());
			statement.setInt(24, getPvpKills());
			statement.setInt(25, getPkKills());
			statement.setInt(26, getRecomHave());
			statement.setInt(27, getRecomLeft());
			statement.setInt(28, getClanId());
			statement.setInt(29, getMaxLoad());
			statement.setInt(30, getRace().ordinal());
			statement.setInt(31, getClassId().getId());
			statement.setInt(32, getDeleteTimer()/1000);
			statement.setString(33, getTitle());
			statement.setInt(34, getAllyId());
			statement.setInt(35, getAccessLevel());
			statement.setInt(36, isOnline());
			statement.setInt(37, getClanPrivileges());
			statement.setInt(38, getWantsPeace());
			statement.setLong(39, getDeleteClanTime()/1000);
			statement.setLong(40, getNoChannel()/1000); // Nochannel
			statement.setInt(41, getObjectId());

			statement.execute();
			statement.close();

			// Delete all current stored effects for char to avoid dupe
			statement = con.prepareStatement(DELETE_SKILL_SAVE);
			statement.setInt(1, getObjectId());
			statement.execute();
			statement.close();

			// Loop thru all effects for char and same them each to database
			for (L2Effect effect : getAllEffects())
			{
				if (effect != null && effect.getInUse() && !effect.getSkill().isToggle() &&
					effect.getEffectType() != L2Effect.EffectType.HEAL_OVER_TIME &&
					effect.getEffectType() != L2Effect.EffectType.COMBAT_POINT_HEAL_OVER_TIME)
				{
					statement = con.prepareStatement(ADD_SKILL_SAVE);
					statement.setInt(1, getObjectId());
					statement.setInt(2, effect.getSkill().getId());
					statement.setInt(3, effect.getSkill().getLevel());
					statement.setInt(4, effect.getCount());
					statement.setInt(5, effect.getTime());
					statement.execute();
					statement.close();
				}
			}
		}
		catch (Exception e)
		{
			_log.warning("could not store char data:"+e);
		}
		finally
		{
			try { con.close(); } catch (Exception e) {}
		}
	}

	/**
	 * Return True if the L2PcInstance is on line.<BR><BR>
	 */
	public int isOnline()
	{
		return (_isOnline ? 1 : 0);
	}

	/**
	 * Add a skill to the L2PcInstance _skills and its Func objects to the calculator set of the L2PcInstance and save update in the character_skills table of the database.<BR><BR>
	 *
	 * <B><U> Concept</U> :</B><BR><BR>
	 * All skills own by a L2PcInstance are identified in <B>_skills</B><BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Replace oldSkill by newSkill or Add the newSkill </li>
	 * <li>If an old skill has been replaced, remove all its Func objects of L2Character calculator set</li>
	 * <li>Add Func objects of newSkill to the calculator set of the L2Character </li><BR><BR>
	 *
	 * @param newSkill The L2Skill to add to the L2Character
	 *
	 * @return The L2Skill replaced or null if just added a new L2Skill
	 *
	 */
	public L2Skill addSkill(L2Skill newSkill)
	{
		// Add a skill to the L2PcInstance _skills and its Func objects to the calculator set of the L2PcInstance
		L2Skill oldSkill = super.addSkill(newSkill);

		// Add or update a L2PcInstance skill in the character_skills table of the database
		storeSkill(newSkill, oldSkill);

		return oldSkill;
	}

	/**
	 * Remove a skill from the L2Character and its Func objects from calculator set of the L2Character and save update in the character_skills table of the database.<BR><BR>
	 *
	 * <B><U> Concept</U> :</B><BR><BR>
	 * All skills own by a L2Character are identified in <B>_skills</B><BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Remove the skill from the L2Character _skills </li>
	 * <li>Remove all its Func objects from the L2Character calculator set</li><BR><BR>
	 *
	 * <B><U> Overriden in </U> :</B><BR><BR>
	 * <li> L2PcInstance : Save update in the character_skills table of the database</li><BR><BR>
	 *
	 * @param skill The L2Skill to remove from the L2Character
	 *
	 * @return The L2Skill removed
	 *
	 */
	public L2Skill removeSkill(L2Skill skill)
	{
		// Remove a skill from the L2Character and its Func objects from calculator set of the L2Character
		L2Skill oldSkill = super.removeSkill(skill);

		java.sql.Connection con = null;
		try
		{
			// Remove or update a L2PcInstance skill from the character_skills table of the database
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement;

			if (oldSkill != null)
			{
				statement = con.prepareStatement(DELETE_SKILL_FROM_CHAR);
				statement.setInt(1, oldSkill.getId());
				statement.setInt(2, getObjectId());
				statement.execute();
				statement.close();
			}
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "Error could not delete Skill:", e);
		}
		finally
		{
			try { con.close(); } catch (Exception e) {}
		}

		return oldSkill;
	}


	/**
	 * Add or update a L2PcInstance skill in the character_skills table of the database.<BR><BR>
	 */
	private void storeSkill(L2Skill newSkill, L2Skill oldSkill)
	{
		java.sql.Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement;

			if (oldSkill != null && newSkill != null)
			{
				statement = con.prepareStatement(UPDATE_CHARACTER_SKILL_LEVEL);
				statement.setInt(1, newSkill.getLevel());
				statement.setInt(2, oldSkill.getId());
				statement.setInt(3, getObjectId());
				statement.execute();
				statement.close();
			}
			else if (newSkill != null)
			{
				statement = con.prepareStatement(ADD_NEW_SKILL);
				statement.setInt(1, getObjectId());
				statement.setInt(2, newSkill.getId());
				statement.setInt(3, newSkill.getLevel());
				statement.setString(4, newSkill.getName());
				statement.execute();
				statement.close();
			}
			else
			{
				_log.warning("could not store new skill. its NULL");
			}
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "Error could not store Skills:", e);
		}
		finally
		{
			try { con.close(); } catch (Exception e) {}
		}
	}


	/**
	 * Retrieve from the database all skills of this L2PcInstance and add them to _skills.<BR><BR>
	 */
	private void restoreSkills()
	{
		java.sql.Connection con = null;
		try
		{
			// Retrieve all skills of this L2PcInstance from the database
			// Send the SQL query : SELECT skill_id,skill_level FROM character_skills WHERE char_obj_id=? to the database
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(GET_ALL_SKILLS_FOR_CHAR);
			statement.setInt(1, getObjectId());
			ResultSet rset = statement.executeQuery();

			// Go though the recordset of this SQL query
			while (rset.next())
			{
				int id = rset.getInt(COLUMN_SKILL_ID);
				int level = rset.getInt(COLUMN_SKILL_LEVEL);

				if (id > 9000)
					continue; // fake skills for base stats

				// Create a L2Skill object for each record
				L2Skill skill = SkillTable.getInstance().getInfo(id, level);

				// Add the L2Skill object to the L2Character _skills and its Func objects to the calculator set of the L2Character
				super.addSkill(skill);

			}

			rset.close();
			statement.close();
		}
		catch (Exception e)
		{
			_log.warning("count not restore skills:"+e);
		}
		finally
		{
			try { con.close(); } catch (Exception e) {}
		}
	}

	public void restoreEffects()
	{
		L2Object[] targets = new L2Character[]{this};
		java.sql.Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement;

			statement = con.prepareStatement(RESTORE_SKILL_SAVE);
			statement.setInt(1, getObjectId());

			ResultSet rset = statement.executeQuery();

			while (rset.next())
			{
				int skillId = rset.getInt("skill_id");
				int skillLvl = rset.getInt("skill_level");
				int effectCount = rset.getInt("effect_count");
				int effectCurTime = rset.getInt("effect_cur_time");

				L2Skill skill = SkillTable.getInstance().getInfo(skillId, skillLvl);
				callSkill(skill, targets);

				for (L2Effect effect : this.getAllEffects())
					if (effect.getSkill().getId() == skillId)
					{
						effect.setCount(effectCount);
						effect.setFirstTime(effectCurTime);
					}
			}

			statement = con.prepareStatement(DELETE_SKILL_SAVE);
			statement.setInt(1, getObjectId());
			statement.executeUpdate();
		}
		catch (Exception e)
		{
			_log.warning("Could not restore active skills data: " + e);
		}
		finally
		{
			try {con.close();} catch (Exception e) {}
		}

		updateEffectIcons();
	}

	/**
	 * Retrieve from the database all Henna of this L2PcInstance, add them to _henna and calculate stats of the L2PcInstance.<BR><BR>
	 */
	private void restoreHenna()
	{
		java.sql.Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("select slot, symbol_id from character_hennas where char_obj_id=?");
			statement.setInt(1, getObjectId());
			ResultSet rset = statement.executeQuery();

			for (int i=0;i<3;i++)
				_henna[i]=null;

			while (rset.next())
			{
				int slot = rset.getInt("slot");
				if (slot<1 || slot>3)continue;

				int symbol_id = rset.getInt("symbol_id");

				L2HennaInstance sym = null;

				if (symbol_id!=0)
				{
					L2Henna tpl = HennaTable.getInstance().getTemplate(symbol_id);
					if (tpl!=null)
					{
						sym = new L2HennaInstance(tpl);
						_henna[slot-1] = sym;
					}
				}
			}

			rset.close();
			statement.close();
		}
		catch (Exception e)
		{
			_log.warning("could not restore henna: "+e);
		}
		finally
		{
			try { con.close(); } catch (Exception e) {}
		}

		// Calculate Henna modifiers of this L2PcInstance
		recalcHennaStats();

	}

	/**
	 * Return the number of Henna empty slot of the L2PcInstance.<BR><BR>
	 */
	public int getHennaEmptySlots()
	{
		int totalSlots = 1 + this.getClassId().level();
		for (int i=0;i<3;i++)
			if (_henna[i]!=null)totalSlots--;

		if (totalSlots<=0)return 0;

		return totalSlots;

	}

	/**
	 * Remove a Henna of the L2PcInstance, save update in the character_hennas table of the database and send Server->Client HennaInfo/UserInfo packet to this L2PcInstance.<BR><BR>
	 */
	public boolean removeHenna(int slot)
	{
		if (slot<1 || slot>3)
			return false;

		slot--;

		if (_henna[slot]==null)
			return false;

		L2HennaInstance henna = _henna[slot];
		int dyeID = henna.getItemIdDye();

		// Added by Tempy - 10 Aug 05
		// Gives amount equal to half of the dyes needed for the henna back.
		L2ItemInstance hennaDyes = ItemTable.getInstance().createItem(dyeID);
		hennaDyes.setCount(henna.getAmountDyeRequire() / 2);

		_henna[slot] = null;

		java.sql.Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("DELETE FROM character_hennas where char_obj_id=? and slot=?");
			statement.setInt(1, getObjectId());
			statement.setInt(2, slot+1);
			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			_log.warning("could not remove char henna: "+e);
		}
		finally
		{
			try { con.close(); } catch (Exception e) {}
		}

		// Calculate Henna modifiers of this L2PcInstance
		recalcHennaStats();

		// Send Server->Client HennaInfo packet to this L2PcInstance
		this.sendPacket(new HennaInfo(this));

		// Send Server->Client UserInfo packet to this L2PcInstance
		this.sendPacket(new UserInfo(this));

		// Add the recovered dyes to the player's inventory and notify them.
		getInventory().addItem(hennaDyes);
		SystemMessage sm = new SystemMessage(SystemMessage.EARNED_S2_S1_s);
		sm.addNumber(henna.getAmountDyeRequire() / 2);
		sendPacket(sm);

		return true;
	}


	/**
	 * Add a Henna to the L2PcInstance, save update in the character_hennas table of the database and send Server->Client HennaInfo/UserInfo packet to this L2PcInstance.<BR><BR>
	 */
	public boolean addHenna(L2HennaInstance henna)
	{
		if (getHennaEmptySlots()==0)
		{
			this.sendMessage("You may not have more than three equipped symbols at a time.");
			return false;
		}

		// int slot = 0;
		for (int i=0;i<3;i++)
		{
			if (_henna[i]==null)
			{
				_henna[i]=henna;

				// Calculate Henna modifiers of this L2PcInstance
				recalcHennaStats();

				java.sql.Connection con = null;
				try
				{
					con = L2DatabaseFactory.getInstance().getConnection();
					PreparedStatement statement = con.prepareStatement("INSERT INTO character_hennas (char_obj_id, symbol_id, slot) VALUES (?, ?, ?)");
					statement.setInt(1, getObjectId());
					statement.setInt(2, henna.getSymbolId());
					statement.setInt(3, i+1);
					statement.execute();
					statement.close();
				}
				catch (Exception e)
				{
					_log.warning("could not save char henna: "+e);
				}
				finally
				{
					try { con.close(); } catch (Exception e) {}
				}

				// Send Server->Client HennaInfo packet to this L2PcInstance
				HennaInfo hi = new HennaInfo(this);
				this.sendPacket(hi);

				// Send Server->Client UserInfo packet to this L2PcInstance
				UserInfo ui = new UserInfo(this);
				this.sendPacket(ui);

				return true;
			}
		}

		return false;
	}

	/**
	 * Calculate Henna modifiers of this L2PcInstance.<BR><BR>
	 */
	private void recalcHennaStats()
	{
		_hennaINT = 0;
		_hennaSTR = 0;
		_hennaCON = 0;
		_hennaMEN = 0;
		_hennaWIT = 0;
		_hennaDEX = 0;

		for (int i=0;i<3;i++)
		{
			if (_henna[i]==null)continue;
			_hennaINT += _henna[i].getStatINT();
			_hennaSTR += _henna[i].getStatSTR();
			_hennaMEN += _henna[i].getStatMEM();
			_hennaCON += _henna[i].getStatCON();
			_hennaWIT += _henna[i].getStatWIT();
			_hennaDEX += _henna[i].getStatDEX();
		}

		if (_hennaINT>5)_hennaINT=5;
		if (_hennaSTR>5)_hennaSTR=5;
		if (_hennaMEN>5)_hennaMEN=5;
		if (_hennaCON>5)_hennaCON=5;
		if (_hennaWIT>5)_hennaWIT=5;
		if (_hennaDEX>5)_hennaDEX=5;

	}

	/**
	 * Return the Henna of this L2PcInstance corresponding to the selected slot.<BR><BR>
	 */
	public L2HennaInstance getHenna(int slot)
	{
		if (slot<1 || slot>3)return null;
		return _henna[slot-1];
	}

	/**
	 * Return the INT Henna modifier of this L2PcInstance.<BR><BR>
	 */
	public int getHennaStatINT()
	{
		return _hennaINT;
	}

	/**
	 * Return the STR Henna modifier of this L2PcInstance.<BR><BR>
	 */
	public int getHennaStatSTR()
	{
		return _hennaSTR;
	}

	/**
	 * Return the CON Henna modifier of this L2PcInstance.<BR><BR>
	 */
	public int getHennaStatCON()
	{
		return _hennaCON;
	}

	/**
	 * Return the MEN Henna modifier of this L2PcInstance.<BR><BR>
	 */
	public int getHennaStatMEN()
	{
		return _hennaMEN;
	}

	/**
	 * Return the WIT Henna modifier of this L2PcInstance.<BR><BR>
	 */
	public int getHennaStatWIT()
	{
		return _hennaWIT;
	}

	/**
	 * Return the DEX Henna modifier of this L2PcInstance.<BR><BR>
	 */
	public int getHennaStatDEX()
	{
		return _hennaDEX;
	}


	/**
	 * Return True if the L2PcInstance is autoAttackable.<BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Check if the attacker isn't the L2PcInstance Pet </li>
	 * <li>Check if the attacker is L2MonsterInstance</li>
	 * <li>If the attacker is a L2PcInstance, check if it is not in the same party </li>
	 * <li>Check if the L2PcInstance has Karma </li>
	 * <li>If the attacker is a L2PcInstance, check if it is not in the same siege clan (Attacker, Defender) </li><BR><BR>
	 *
	 */
	public boolean isAutoAttackable(L2Character attacker)
	{

		// Check if the attacker isn't the L2PcInstance Pet
		if (attacker == this || attacker == this.getPet())
			return false;

		// TODO: check for friendly mobs
		// Check if the attacker is a L2MonsterInstance
		if (attacker instanceof L2MonsterInstance)
			return true;

		// Check if the L2PcInstance has Karma
		if (getKarma() > 0)
			return true;

		L2PcInstance pcAttacker = attacker.getPcInstance();
		// Check if the attacker is a L2PcInstance
		if (pcAttacker != null)
		{
			// Check if the L2PcInstance is in an arena or a siege area
			if (ZoneManager.getInstance().checkIfInZonePvP(this) && ZoneManager.getInstance().checkIfInZonePvP(pcAttacker))
				return true;

			// Check if the attacker is not in the same party
			if (getParty() != null && getParty().getPartyMembers().contains(pcAttacker))
				return false;

			if (getClan() != null)
			{
				Siege siege = SiegeManager.getInstance().getSiege(getX(), getY());
				if (siege != null)
				{
					// if attacker and this in the Defender clans
					if (siege.checkIsDefender(pcAttacker.getClan()) && siege.checkIsDefender(getClan()))
						return false;

					// if attacker and this in the Attacker clans
					if (siege.checkIsAttacker(pcAttacker.getClan()) && siege.checkIsAttacker(getClan()))
						return false;
					
					return true;
				}

				if (atWarWith(pcAttacker))
					return true;
			}
		}
		else if (attacker instanceof L2SiegeGuardInstance)
		{
			if (this.getClan() != null)
			{
				Siege siege = SiegeManager.getInstance().getSiege(this);
				return (siege != null && siege.checkIsAttacker(this.getClan()));
			}
		}

		return false;
	}


	/**
	 * Check if the active L2Skill can be casted.<BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Check if the skill isn't toggle and is offensive </li>
	 * <li>Check if the target is in the skill cast range </li>
	 * <li>Check if the skill is Spoil type and if the target isn't already spoiled </li>
	 * <li>Check if the caster owns enought consummed Item, enough HP and MP to cast the skill </li>
	 * <li>Check if the caster isn't sitting </li>
	 * <li>Check if all skills are enabled and this skill is enabled </li><BR><BR>
	 * <li>Check if the caster own the weapon needed </li><BR><BR>
	 * <li>Check if the skill is active </li><BR><BR>
	 * <li>Check if all casting conditions are completed</li><BR><BR>
	 * <li>Notify the AI with AI_INTENTION_CAST and target</li><BR><BR>
	 *
	 * @param skill The L2Skill to use
	 * @param forceUse used to force ATTACK on players
	 * @param dontMove used to prevent movement, if not in range
	 *
	 */
	public void useMagic(L2Skill skill, boolean forceUse, boolean dontMove)
	{

		// Check if the skill isn't toggle
		if (skill.isToggle())
		{
			L2Effect effect = getEffect(skill);
			if (effect != null)
			{
				effect.exit();

				// Send a Server->Client packet ActionFailed to the L2PcInstance
				sendPacket(new ActionFailed());
				return;
			}
		}
		if(getIntention()==CtrlIntention.AI_INTENTION_CAST && !skill.isToggle() && _nextSkill == null)
		{
			_nextSkill = skill;
			_nextForceUse = forceUse;
			_nextDontMove = dontMove;
//			sendMessage("Storing skill...");
		}
		L2Object target = null;
		switch(skill.getTargetType())
		{
			case TARGET_AURA:
			case TARGET_PARTY:	  // AURA, PARTY, CLAN, SELF skills should work regardless of current target
			case TARGET_ALLY:
			case TARGET_CLAN:
			case TARGET_SELF:
				target = this;
				break;
			default:
				target = getTarget();
				break;
		}

		//dont allow casting on players on different dungeon lvls etc
		if ((target != null)&&(Math.abs(target.getZ() - getZ()) > 1000))
		{
			sendPacket(new SystemMessage(SystemMessage.CANNOT_SEE_TARGET));
			sendPacket(new ActionFailed());
			return;
		}

		if (skill.isOffensive())
		{
			if (
					target != null
					&& !(target instanceof L2MonsterInstance)   // Target is not a monster
					&& (getAccessLevel() < Config.GM_PEACEATTACK)	// Player cannot attack in peace
					&& (withinZones(L2World.zonePeace) || target.withinZones(L2World.zonePeace)) // Target or player is in peace zone
					&& (
							!(target instanceof L2PcInstance)   // Target is not a player
							|| (!Config.ALT_GAME_KARMAPLAYERCANBEKILLEDINPEACEZONE) // Or Target is a player and killable in peace zone is false
							|| (((L2PcInstance)target).getKarma() < Config.KARMA_MAX_KARMA) // Or Target is a player, killable in peace zone is true, and has karma
						)
			   )
				{
					sendPacket(new SystemMessage(SystemMessage.YOU_CANNOT_ATTACK_THE_TARGET_IN_THE_PEACE_ZONE));
					sendPacket(new ActionFailed());
					return;
				}

			// Forced ATTACK is needed on non-attackable targets
			if (target != null && !target.isAutoAttackable(this) && !forceUse &&
					skill.getTargetType() != SkillTargetType.TARGET_AURA &&
					skill.getTargetType() != SkillTargetType.TARGET_CLAN &&
					skill.getTargetType() != SkillTargetType.TARGET_ALLY &&
					skill.getTargetType() != SkillTargetType.TARGET_PARTY &&
					skill.getTargetType() != SkillTargetType.TARGET_SELF)
			{
				// Send a Server->Client packet ActionFailed to the L2PcInstance
				sendPacket(new ActionFailed());
				return;
			}

			// Check if the target is in the skill cast range
			if (target != null && dontMove)
			{
				// Calculate the distance between the L2PcInstance and the target
				double distance = getDistance(target.getX(), target.getY());

				if (skill.getCastRange() > 0 && distance > skill.getCastRange())
				{
					// Send a System Message to the caster
					sendPacket(new SystemMessage(SystemMessage.TARGET_IS_TOO_FAR));

					// Send a Server->Client packet ActionFailed to the L2PcInstance
					sendPacket(new ActionFailed());
					return;
				}
			}
		}


		// Check if the skill is Spoil type and if the target isn't already spoiled
		if (skill.getSkillType() == SkillType.SPOIL)
		{
			if (target instanceof L2Attackable && ((L2Attackable)target).isSpoil())
			{
				// Send a System Message to the caster
				sendPacket(new SystemMessage(SystemMessage.ALREADY_SPOILED));

				// Send a Server->Client packet ActionFailed to the L2PcInstance
				sendPacket(new ActionFailed());
				return;
			}
		}

		// Check if the skill is Sweep type and if conditions not apply
		if (skill.getSkillType() == SkillType.SWEEP)
		{
			if (target instanceof L2Attackable && ((L2Attackable)target).isDead() && !((L2Attackable)target).isSpoil())
			{
				// Send a System Message to the L2PcInstance
				sendPacket(new SystemMessage(SystemMessage.SWEEPER_FAILED_TARGET_NOT_SPOILED));

				// Send a Server->Client packet ActionFailed to the L2PcInstance
				sendPacket(new ActionFailed());
				return;
			}
		}

		// Check if the skill is Drain Soul (Soul Crystals) and if the target is a MOB
		if (skill.getSkillType() == SkillType.DRAIN_SOUL)
		{
			if (!(target instanceof L2MonsterInstance))
			{
				// Send a System Message to the L2PcInstance
				sendPacket(new SystemMessage(SystemMessage.TARGET_IS_INCORRECT));

				// Send a Server->Client packet ActionFailed to the L2PcInstance
				sendPacket(new ActionFailed());
				return;
			}
		}

		// Check if the spell consummes an Item
		if (skill.getItemConsume() > 0)
		{
			// Get the L2ItemInstance consummed by the spell
			L2ItemInstance requiredItems = getInventory().findItemByItemId(skill.getItemConsumeId());

			// Check if the caster owns enought consummed Item to cast
			if (requiredItems == null || requiredItems.getCount() < skill.getItemConsume())
			{
				// Send a System Message to the caster
				sendPacket(new SystemMessage(SystemMessage.INCORRECT_ITEM_COUNT));
				return;
			}
		}

		if (isAlikeDead())
		{
			// Send a Server->Client packet ActionFailed to the L2PcInstance
			sendPacket(new ActionFailed());
			return;
		}

		// Check if the caster isn't sitting
		if (isSitting() && !skill.isPotion())
		{
			// Send a System Message to the caster
			sendPacket(new SystemMessage(SystemMessage.YOU_CANNOT_MOVE_WHILE_SITTING));

			// Send a Server->Client packet ActionFailed to the L2PcInstance
			sendPacket(new ActionFailed());
			return;
		}

		// Check if this is a Pvp skill and target isn't a non-flagged/non-karma player
		switch(skill.getTargetType()) {
			case TARGET_PARTY:
			case TARGET_ALLY:   // For such skills, checkPvpSkill() is called from L2Skill.getTargetList()
			case TARGET_CLAN:   // For such skills, checkPvpSkill() is called from L2Skill.getTargetList()
			case TARGET_AURA:
			case TARGET_SELF:
				break;
			default:
				if (!checkPvpSkill(target, skill) && (getAccessLevel() < Config.GM_PEACEATTACK)) {
					// Send a System Message to the L2PcInstance
					sendPacket(new SystemMessage(SystemMessage.TARGET_IS_INCORRECT));

					// Send a Server->Client packet ActionFailed to the L2PcInstance
					sendPacket(new ActionFailed());
					return;
				}
		}

		// Check if the caster has enough MP
		if (getCurrentMp() < skill.getMpConsume())
		{
			// Send a System Message to the caster
			sendPacket(new SystemMessage(SystemMessage.NOT_ENOUGH_MP));

			// Send a Server->Client packet ActionFailed to the L2PcInstance
			sendPacket(new ActionFailed());
			return;
		}

		// Check if the caster has enough HP
		if (getCurrentHp() <= skill.getHpConsume())
		{
			// Send a System Message to the caster
			sendPacket(new SystemMessage(SystemMessage.NOT_ENOUGH_HP));

			// Send a Server->Client packet ActionFailed to the L2PcInstance
			sendPacket(new ActionFailed());
			return;
		}

		// Check if all skills are enabled
		if (isAllSkillsDisabled() && (getAccessLevel() < Config.GM_PEACEATTACK))
		{
			// Send a Server->Client packet ActionFailed to the L2PcInstance
			sendPacket(new ActionFailed());
			return;
		}

		// Check if this skill is enabled
		if (isSkillDisabled(skill.getId()) && (getAccessLevel() < Config.GM_PEACEATTACK))
		{
			sendPacket(new SystemMessage(SystemMessage.S1_IS_NOT_AVAILABLE_AT_THIS_TIME_BEING_PREPARED_FOR_REUSE).addString(skill.getName()));
			// Send a Server->Client packet ActionFailed to the L2PcInstance
			sendPacket(new ActionFailed());
			return;
		}

		// Check if the caster own the weapon needed
		if(!skill.getWeaponDependancy(this))
		{
			// Send a Server->Client packet ActionFailed to the L2PcInstance
			sendPacket(new ActionFailed());
			return;
		}

		// Check if the skill is active
		if (skill.isPassive())
		{
			// just ignore the passive skill request. why does the client send it anyway ??
			// Send a Server->Client packet ActionFailed to the L2PcInstance
			sendPacket(new ActionFailed());
			return;
		}

		// Check if all casting conditions are completed
		if(!skill.checkCondition(this))
		{
			// Send a Server->Client packet ActionFailed to the L2PcInstance
			sendPacket(new ActionFailed());
			return;
		}

		// Set Intention to AI_INTENTION_CAST
		super.useMagic(skill);
	}

	/**
	 * Check if the requested casting is a Pc->Pc skill cast and if it's a valid pvp condition
	 * @param target L2Object instance containing the target
	 * @param skill L2Skill instance with the skill being casted
	 * @return False if the skill is a pvpSkill and target is not a valid pvp target
	 */
	public boolean checkPvpSkill(L2Object target, L2Skill skill)
	{
		// check for PC->PC Pvp status
		if (
				target != null &&										   // target not null and
				target != this &&										   // target is not self and
				target instanceof L2PcInstance &&						   // target is L2PcInstance and
				skill.isPvpSkill() &&									   // pvp skill and
				!ZoneManager.getInstance().checkIfInZonePvP(this) &&		// Pc is not in PvP zone
				!ZoneManager.getInstance().checkIfInZonePvP(target)		 // target is not in PvP zone
			)
		{
			if (
					((L2PcInstance)target).getPvpFlag() == 0 &&			 //   target's pvp flag is not set and
					((L2PcInstance)target).getKarma() == 0				  //   target has no karma
			)
				return false;
		}
		return true;
	}

	/**
	 * Reduce Item quantity of the L2PcInstance Inventory and send it a Server->Client packet InventoryUpdate.<BR><BR>
	 */
	public void consumeItem(int itemConsumeId, int itemCount)
	{
		if (itemConsumeId != 0 && itemCount != 0)
		{
			// Reduce the number of Item consummed
			L2ItemInstance item = getInventory().destroyItemByItemId(itemConsumeId, itemCount);

			// Send a Server->Client packet InventoryUpdate to the L2PcInstance
			InventoryUpdate update = new InventoryUpdate();
			update.addItem(item);
			sendPacket(update);
		}
	}

	/**
	 * Return True if the L2PcInstance is a Mage.<BR><BR>
	 */
	public boolean isMageClass()
	{
		L2CharTemplate template = CharTemplateTable.getInstance().getTemplate(getClassId(), getSex()!=0);

		if(template.baseMAtk > 3)
		{
			return true;
		}

		return false;
	}


	public boolean isMounted()
	{
		return _mountType > 0;
	}

	/**
	 * Set the type of Pet mounted (0 : none, 1 : Stridder, 2 : Wyvern) and send a Server->Client packet InventoryUpdate to the L2PcInstance.<BR><BR>
	 */
	public void setMountType(int mountType)
	{
		switch(mountType)
		{
		case 0: setFlying(false);
				setRidding(false);
				break; //Dismounted
		case 1: setRidding(true);
				break;
		case 2: setFlying(true);
				break; //Flying Wyvern
		}

		_mountType = mountType;

		// Send a Server->Client packet InventoryUpdate to the L2PcInstance in order to update speed
		UserInfo ui = new UserInfo(this);
		sendPacket(ui);

	}

	/**
	 * Return the type of Pet mounted (0 : none, 1 : Stridder, 2 : Wyvern).<BR><BR>
	 */
	public int getMountType()
	{
		return _mountType;
	}


	/**
	 * Send a Server->Client packet UserInfo to this L2PcInstance and CharInfo to all L2PcInstance in its _KnownPlayers.<BR><BR>
	 *
	 * <B><U> Concept</U> :</B><BR><BR>
	 * Others L2PcInstance in the detection area of the L2PcInstance are identified in <B>_knownPlayers</B>.
	 * In order to inform other players of this L2PcInstance state modifications, server just need to go through _knownPlayers to send Server->Client Packet<BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Send a Server->Client packet UserInfo to this L2PcInstance (Public and Private Data)</li>
	 * <li>Send a Server->Client packet CharInfo to all L2PcInstance in _KnownPlayers of the L2PcInstance (Public data only)</li><BR><BR>
	 *
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : DON'T SEND UserInfo packet to other players instead of CharInfo packet.
	 * Indeed, UserInfo packet contains PRIVATE DATA as MaxHP, STR, DEX...</B></FONT><BR><BR>
	 *
	 */
	public void updateAbnormalEffect()
	{
		broadcastUserInfo();
	}

	/**
	 * Disable the Inventory and create a new task to enable it after 1.5s.<BR><BR>
	 */
	public void tempInvetoryDisable()
	{
		_inventoryDisable  = true;

		ClientScheduler.getInstance().scheduleMed(new InventoryEnable(), 1500);
	}

	/**
	 * Return True if the Inventory is disabled.<BR><BR>
	 */
	public boolean isInvetoryDisabled()
	{
		return _inventoryDisable;
	}

	class InventoryEnable implements Runnable
	{
		public void run()
		{
			_inventoryDisable = false;
		}
	}

	public void setFreightId(int obj_Id)
	{
		_freightId = obj_Id;
	}

	public int getFreightId()
	{
		return _freightId;
	}

	public void setUsingFreight(boolean b)
	{
		_usingFreight = b;
	}

	public boolean isUsingFreight()
	{
		return _usingFreight;
	}

	public void setUsingClanWarehouse(boolean b)
	{
		_usingClanWarehouse = b;
	}

	public boolean isUsingClanWarehouse()
	{
		return _usingClanWarehouse;
	}

	public HashMap<Integer, L2CubicInstance> getCubics()
	{
		return _cubics;
	}

	/**
	 * Add a L2CubicInstance to the L2PcInstance _cubics.<BR><BR>
	 */
	public void addCubic(int id, int level)
	{
		L2CubicInstance cubic = new L2CubicInstance(this, id, level);
		_cubics.put(id, cubic);
	}

	/**
	 * Remove a L2CubicInstance from the L2PcInstance _cubics.<BR><BR>
	 */
	public void delCubic(int id)
	{
		_cubics.remove(id);
	}

	/**
	 * Return the L2CubicInstance corresponding to the Identifier of the L2PcInstance _cubics.<BR><BR>
	 */
	public L2CubicInstance getCubic(int id)
	{
		return _cubics.get(id);
	}

	public String toString()
	{
		return "player '"+getName()+"'";
	}

	/**
	 * Return the modifier corresponding to the Enchant Effect of the Active Weapon (Min : 127).<BR><BR>
	 */
	public int getEnchantEffect()
	{
		L2ItemInstance wpn = getActiveWeaponInstance();

		if (wpn == null)
			return 0;

		return Math.min(127, wpn.getEnchantLevel());
	}

	/**
	 * Set the _lastFolkNpc of the L2PcInstance corresponding to the last Folk wich one the player talked.<BR><BR>
	 */
	public void setLastFolkNPC(L2FolkInstance folkNpc)
	{
		_lastFolkNpc = folkNpc;
	}

	/**
	 * Return the _lastFolkNpc of the L2PcInstance corresponding to the last Folk wich one the player talked.<BR><BR>
	 */
	public L2FolkInstance getLastFolkNPC()
	{
		return _lastFolkNpc;
	}

	/**
	 * Set the Silent Moving mode Flag.<BR><BR>
	 */
	public void setSilentMoving(boolean flag)
	{
		_isSilentMoving = flag;
	}

	/**
	 * Return True if the Silent Moving mode is active.<BR><BR>
	 */
	public boolean isSilentMoving()
	{
		return _isSilentMoving;
	}

	/**
	 * Return True if L2PcInstance is a participant in the Festival of Darkness.<BR><BR>
	 */
	public boolean isFestivalParticipant()
	{
		return SevenSignsFestival.getInstance().isParticipant(this);
	}

	/**
	 * Set the PvP Flag of the L2PcInstance.<BR><BR>
	 */
	public void checkPvP(L2Character target)
	{
		if(target == null || target == this || getKarma() != 0)
			return;

		L2PcInstance pcTarget = target.getPcInstance();
		
		if(pcTarget == null || ZoneManager.getInstance().checkIfInZonePvP(this) || ZoneManager.getInstance().checkIfInZonePvP(target))
			return;

		if(atWarWith(pcTarget))
			return;

		if (getPvpFlag() == 0)
			startPvPFlag();
		setlastPvpAttack(System.currentTimeMillis());
	}

	public void addAutoSoulShot(int itemId)
	{
		_activeSoulShots.add(Integer.valueOf(itemId));
		if (_taskAutoSoulShot == null)
			_taskAutoSoulShot = ClientScheduler.getInstance().scheduleMedAtFixedRate(
					new AutoSoulShot(), 0, 300);
	}

	public void removeAutoSoulShot(int itemId)
	{
		_activeSoulShots.remove(Integer.valueOf(itemId));
		if (_activeSoulShots.isEmpty() && _taskAutoSoulShot != null)
		{
			_taskAutoSoulShot.cancel(false);
			_taskAutoSoulShot = null;
		}
	}

	public HashSet<Integer> getAutoSoulShot()
	{
		return _activeSoulShots;
	}

	class AutoSoulShot implements Runnable
	{
		public void run()
		{
			Integer[] itemIds = _activeSoulShots.toArray(new Integer[0]);
			for (Integer itemId : itemIds)
			{
				L2ItemInstance item = getInventory().findItemByItemId(itemId.intValue());
				if (item == null)
				{
					removeAutoSoulShot(itemId.intValue());
				}
				else
				{
					IItemHandler handler = ItemHandler.getInstance().getItemHandler(item.getItemId());
					handler.useItem(L2PcInstance.this, item);
				}
			}
		}
	}

	public void setInvisible()
	{
		_invisible = 1;
	}

	public void setVisible()
	{
		_invisible = 0;
	}

	public int getInvisible()
	{
		return _invisible;
	}


	public void togglePathNodeMode()
	{
		isPathNodeMode	= !isPathNodeMode;
	}

	public void toggleViewPathNodes()
	{
		isPathNodesVisible	= !isPathNodesVisible;
	}

	public boolean isPathNodeModeActive()
	{
		return isPathNodeMode;
	}

	public boolean isPathNodeVisible()
	{
		return isPathNodesVisible;
	}

	public Map<WayPointNode, CopyOnWriteArrayList<WayPointNode>> getPathNodeMap()
	{
		if (pathNodeMap == null)
			pathNodeMap	= new ConcurrentHashMap<WayPointNode, CopyOnWriteArrayList<WayPointNode>>();
		return pathNodeMap;
	}

	public void addPathNodePoint()
	{
		//add Node to Players PathNode List
		//TODO: Addcheck for nearby point in radius
		setSelectedNode(WayPointNode.spawn(this));
		getPathNodeMap().put(getSeltectedNode(), new CopyOnWriteArrayList<WayPointNode>());

	}

	/**
	 * Delete all Path Nodes.<BR><BR>
	 */
	public void clearPathNodes()
	{
		if (pathNodeMap != null)
		{
			for (WayPointNode node : pathNodeMap.keySet())
			{
				removePathNodePoint(node);
			}
			pathNodeMap.clear();
		}
		setSelectedNode(null);
	}

	public void setSelectedNode(WayPointNode decoInstance)
	{
		if (selectedNode != null)
		{
			CopyOnWriteArrayList<WayPointNode> linkedNodes	= getPathNodeMap().get(selectedNode);
			if (linkedNodes != null)
			{
				for (WayPointNode node : linkedNodes)
				{
					node.setNormal();
				}
			}
			selectedNode.setNormal();
		}
		selectedNode	= decoInstance;
		if (selectedNode != null)
		{
			selectedNode.setSelected();
			CopyOnWriteArrayList<WayPointNode> linkedNodes   = getPathNodeMap().get(selectedNode);
			if (linkedNodes != null)
			{
				for (WayPointNode linked : linkedNodes)
				{
					linked.setLinked();
				}
			}
		}
	}

	public WayPointNode getSeltectedNode()
	{
		return selectedNode;
	}

	public void addLink(WayPointNode target)
	{
		if (selectedNode != null)
		{
			CopyOnWriteArrayList<WayPointNode> list	= pathNodeMap.get(selectedNode);
			if (list == null)
				list = new CopyOnWriteArrayList<WayPointNode>();
			list.add(target);
			pathNodeMap.put(selectedNode, list);


			CopyOnWriteArrayList<WayPointNode> list2	= pathNodeMap.get(target);
			if (list2 == null)
				list2 = new CopyOnWriteArrayList<WayPointNode>();
			list2.add(selectedNode);
			pathNodeMap.put(target, list2);
			target.setLinked();
			WayPointNode.drawLine(target, selectedNode);
		}
	}

	public void removeLink(WayPointNode target)
	{
		if (selectedNode != null)
		{
			CopyOnWriteArrayList<WayPointNode> list	= pathNodeMap.get(selectedNode);
			if (list != null && list.contains(target))
			{
				list.remove(target);
				pathNodeMap.put(selectedNode, list);

				CopyOnWriteArrayList<WayPointNode> list2	= pathNodeMap.get(target);
				if (list2 != null && list2.contains(selectedNode))
				{
					list2.remove(selectedNode);
					pathNodeMap.put(target, list2);
				}
				target.setNormal();
				WayPointNode.eraseLine(target, selectedNode);
			}
			else
			{
				sendPacket(SystemMessage.sendString("Target not a Linked Node."));
			}
		}
	}

	public void removePathNodePoint()
	{
		removePathNodePoint(selectedNode);
	}

	public void removePathNodePoint(WayPointNode node)
	{
		if (node != null)
		{
			CopyOnWriteArrayList<WayPointNode> list  = pathNodeMap.get(node);
			if (list != null)
			{
				synchronized (list)
				{
					try
					{
						for (WayPointNode link : list)
						{
							//WayPointNode.eraseLine(node, link);
							removeLink(link);
						}
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
				}
			}
			pathNodeMap.remove(node);
			node.decayMe();
			node = null;
		}
	}

	/**
	 * @param string
	 */
	public void savePathNodes(String fileName)
	{
		try
		{
			//File file   = new File(Config.DATAPACK_ROOT, "pathnode/" + fileName + ".bin");
			FileOutputStream fos = new FileOutputStream("pathnode/" + fileName + ".bin"); // Save to file
			GZIPOutputStream gzos = new GZIPOutputStream(fos); // Compressed
			ObjectOutputStream out = new ObjectOutputStream(gzos); // Save objects
			out.writeObject(getSaveList()); // Write the entire pathNodeMap of Positions
			out.flush(); // Always flush the output.
			out.close(); // And close the stream.
			sendPacket(SystemMessage.sendString("Path Node table saved to: L2J/pathnode/"+fileName+".bin"));
		}
		catch (Exception e)
		{
			e.printStackTrace();
			sendPacket(SystemMessage.sendString("Could not Save Path Node Table."));
		}
	}

	public HashMap<Point3D, ArrayList<Point3D>> getSaveList()
	{
		HashMap<Point3D, ArrayList<Point3D>> saveList   = null;
		if (pathNodeMap != null)
		{
			saveList	= new HashMap<Point3D, ArrayList<Point3D>>();
			for (WayPointNode node : pathNodeMap.keySet())
			{
				Point3D nodePoint   = Point3D.getPosition(node);

				CopyOnWriteArrayList<WayPointNode> links = pathNodeMap.get(node);
				ArrayList<Point3D> linkPoints   = null;
				if (links != null)
				{
					linkPoints  = new ArrayList<Point3D>();
					for (WayPointNode link : links)
					{
						linkPoints.add(Point3D.getPosition(link));
					}
				}
				saveList.put(nodePoint, linkPoints);
			}
		}
		return saveList;
	}

	/**
	 * @param newNodeMap
	 */
	@SuppressWarnings("unused")
	private void loadNodeList(HashMap<Point3D, ArrayList<Point3D>> newNodeMap)
	{
		pathNodeMap = new ConcurrentHashMap<WayPointNode, CopyOnWriteArrayList<WayPointNode>>();
		HashMap<Point3D, WayPointNode> map	= new HashMap<Point3D, WayPointNode>();

		WayPointNode newNode = null;
		for (Point3D point : newNodeMap.keySet())
		{
			newNode = WayPointNode.spawn(point);
			newNode.setNormal();
			map.put(point, newNode);
		}

		for (Point3D point : newNodeMap.keySet())
		{
			WayPointNode node   = map.get(point);

			ArrayList<Point3D> links = newNodeMap.get(point);
			CopyOnWriteArrayList<WayPointNode> nodeLinks = null;
			if (links != null)
			{
				nodeLinks  = new CopyOnWriteArrayList<WayPointNode>();
				for (Point3D link : links)
				{
					WayPointNode linkNode = map.get(link);
					nodeLinks.add(linkNode);
					if (pathNodeMap.get(linkNode) != null &&
							!pathNodeMap.get(linkNode).contains(node))
						WayPointNode.drawLine(linkNode, node);
				}
			}
			pathNodeMap.put(node, nodeLinks);
		}
	}

	public int getClanPrivileges(){
		return clanPrivileges;
	}
	public void setClanPrivileges(int n){
		clanPrivileges = n;
	}

	/**
	 *
	 */
	public void refreshLinks()
	{
		if (linkToggle)
		{
			for (WayPointNode node : pathNodeMap.keySet())
			{
				for (WayPointNode lineNode : node.getLineNodes())
				{

					lineNode.decayMe();
					lineNode.refreshID();
					//lineNode.spawnMe();
				}
			}
		}
		else
		{
			for (WayPointNode node : pathNodeMap.keySet())
			{
				for (WayPointNode lineNode : node.getLineNodes())
				{

					//lineNode.decayMe();
					//lineNode.refreshID();
					lineNode.spawnMe();
				}
			}
		}
		linkToggle   = !linkToggle;
	}

	public void enterObserverMode(int x, int y, int z)
	{
		_obsX = getX();
		_obsY = getY();
		_obsZ = getZ();
		//if (getTarget() != null && getTarget() instanceof L2Character);
		//	sendPacket(new TargetUnselected((L2Character)getTarget()));
		setTarget(null);
		stopMove(null);
		setParalyzed(true);
		setInvisible();
		sendPacket(new ObservationMode(x, y, z));
		setXYZ(x, y, z);
		_observerMode = true;
		broadcastPacket(new CharInfo(this));
	}

	public void leaveObserverMode()
	{
		//if (getTarget() != null && getTarget() instanceof L2Character);
		//	sendPacket(new TargetUnselected((L2Character)getTarget()));
		setTarget(null);
		setXYZ(_obsX, _obsY, _obsZ);
		setParalyzed(false);
		setVisible();
		setIntention(CtrlIntention.AI_INTENTION_IDLE, null, null);
		sendPacket(new ObservationReturn(this));
		_observerMode = false;
		broadcastPacket(new CharInfo(this));
	}

	public int getObsX()
	{
		return _obsX;
	}

	public int getObsY()
	{
		return _obsY;
	}

	public int getObsZ()
	{
		return _obsZ;
	}

	public boolean inObserverMode()
	{
		return _observerMode;
	}

	public int getTeleMode()
	{
		return _telemode;
	}

	public void setTeleMode(int mode)
	{
		_telemode = mode;
	}

	public void setLoto(int i, int val)
	{
		_loto[i] = val;
	}
	public int getLoto(int i)
	{
		return _loto[i];
	}
	public void setRace(int i, int val)
	{
		_race[i] = val;
	}
	public int getRace(int i)
	{
		return _race[i];
	}

	public boolean getMessageRefusal()
	{
		return _messageRefusal;
	}

	public void setMessageRefusal(boolean mode)
	{
		_messageRefusal = mode;
	}

	public void setDietMode(boolean mode)
	{
		_dietMode = mode;
	}

	public boolean getDietMode()
	{
		return _dietMode;
	}

	public void setTradeRefusal(boolean mode)
	{
		_tradeRefusal = mode;
	}

	public boolean getTradeRefusal()
	{
		return _tradeRefusal;
	}

	public void setExchangeRefusal(boolean mode)
	{
		_exchangeRefusal = mode;
	}

	public boolean getExchangeRefusal()
	{
		return _exchangeRefusal;
	}

	public void addToBlockList(L2PcInstance charid)
	{
		_blockList.add(charid);
	}

	public void removeFromBlockList(L2PcInstance charid)
	{
		_blockList.remove(charid);
	}

	public boolean isInBlockList(L2PcInstance charid)
	{
		for(L2PcInstance candidate : _blockList)
		{
			if(candidate == charid)
			{
				return true;
			}
		}

		return false;
	}

	public boolean isBlockAll()
	{
		return _blockAll;
	}

	public void setBlockAll(boolean state)
	{
		_blockAll = state;
	}

	public ArrayList<String> getBlockList()
	{
		ArrayList<String> blocked = new ArrayList<String>();
		for(L2PcInstance current : _blockList)
		{
			blocked.add(current.getName());
		}

		return blocked;
	}

	public void setConnected(boolean connected)
	{
		_isConnected = connected;
	}

	public void setHero(boolean hero)
	{
		_hero = hero;
	}

	public boolean isHero()
	{
		return _hero;
	}

	public void setTeam(int team)
	{
		_team = team;
	}

	public int getTeam()
	{
		return _team;
	}

	public int getNameColor()
	{
		return _nameColor;
	}

	public void setNameColor(int nameColor)
	{
		_nameColor = nameColor;
	}

	public void setNameColor(int red, int green, int blue)
	{
		_nameColor = (red & 0xFF) + ((green & 0xFF) << 8) + ((blue & 0xFF) << 16);
	}

	public void setWantsPeace(int wantsPeace)
	{
		_wantsPeace = wantsPeace;
	}

	public int getWantsPeace()
	{
		return _wantsPeace;
	}

	public final void illegalAction(String msg, Integer jail_items)
	{
	 	Log.IllegalPlayerAction(this, msg, jail_items);
	}

	public final String toFullString()
	{
		return "player '"+getName()+"' [oid="+getObjectId()+", account='"+getAccountName()+", ip="+getNetConnection().getSocket().getInetAddress().getHostAddress()+"']";
	}

	public final void setVar(String name, Object value)
	{
		if(value instanceof Boolean)
			value = ((Boolean)value) ? -1 : 0;
		jbf.uvset(name, value);
	}

	public final Object getVar(String name)
	{
		return jbf.uvget(name);
	}

	public final Integer getVarI(String name)
	{
		return Strings.parseInt(jbf.uvget(name));
	}

	public final String getVarS(String name)
	{
		return ""+jbf.uvget(name);
	}

	public final Boolean getVarB(String name)
	{
		if(jbf == null)
		{
			_log.warning("JBForth is null for "+this); // See ticket #120 for trace
			return false;
		}

		Object var = jbf.uvget(name);
		if(var == null)
			return false;

		if(Strings.parseInt(var) == 0)
			return false;

		if(Math.abs(Strings.parseFloat(var)) <= 0.000001)
			return false;

		return true;
	}
	
	public int isAtWarWith(Integer id)
	{
		L2Clan clan = ClanTable.getInstance().getClan(id);
		if(getClan() == null)
			return 0;
		if ((getClan().getWarList() != null)&&(getClan().getWarList().size() > 0))
			if (getClan().getWarList().contains(id)) return 1;
		if(getAlliance() == null)
			return 0;
		if ((getAlliance().getWarList() != null)&&(getAlliance().getWarList().size() > 0))
			if (getAlliance().getWarList().contains(clan.getAllyId())) return 2;
		return 0;		
	}

	public int isAtWar()
	{
		int temp=0;
		if(getClan() == null)
			return temp;
		if ((getClan().getWarList() != null)&&(getClan().getWarList().size() > 0))
		   temp=+1;
		if(getAlliance() == null)
			return temp;
		if ((getAlliance().getWarList() != null)&&(getAlliance().getWarList().size() > 0))
		   temp=+2;
	   return temp;	   
   }

	public void doRevive()
	{
		super.doRevive();
		updateEffectIcons();
	}
	
	public void updateNoChannel(int time)
	{
		java.sql.Connection con = null;
		
		_NoChannel = time > 0 ? System.currentTimeMillis() + time : time;
		
		try
		{		   
			con = L2DatabaseFactory.getInstance().getConnection();
			
			String stmt = "UPDATE characters SET characters.nochannel = ? WHERE characters.char_name=?";
			PreparedStatement statement = con.prepareStatement(stmt);
			statement.setLong(1, _NoChannel);
			statement.setString(2, this.getName());
			statement.executeUpdate();
			statement.close();
		}
		catch (Exception e)
		{
			//System.out.println("Error in sql");
			_log.warning("Could not activate nochannel:"+e);
		} 
		finally 
		{
			try { con.close(); } catch (Exception e) {}
		}
	}
}
