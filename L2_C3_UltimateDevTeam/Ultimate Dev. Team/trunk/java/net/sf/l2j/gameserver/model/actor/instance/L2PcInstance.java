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
package net.sf.l2j.gameserver.model.actor.instance;

import javolution.util.FastList;
import javolution.util.FastMap;
import javolution.util.FastSet;
import net.sf.l2j.Config;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.*;
import net.sf.l2j.gameserver.ai.CtrlIntention;
import net.sf.l2j.gameserver.ai.L2CharacterAI;
import net.sf.l2j.gameserver.ai.L2PlayerAI;
import net.sf.l2j.gameserver.cache.HtmCache;
import net.sf.l2j.gameserver.handler.IItemHandler;
import net.sf.l2j.gameserver.handler.ItemHandler;
import net.sf.l2j.gameserver.handler.skillhandlers.SiegeFlag;
import net.sf.l2j.gameserver.handler.skillhandlers.TakeCastle;
import net.sf.l2j.gameserver.instancemanager.*;
import net.sf.l2j.gameserver.lib.Log;
import net.sf.l2j.gameserver.lib.Rnd;
import net.sf.l2j.gameserver.model.*;
import net.sf.l2j.gameserver.model.L2Skill.SkillTargetType;
import net.sf.l2j.gameserver.model.L2Skill.SkillType;
import net.sf.l2j.gameserver.model.actor.knownlist.PcKnownList;
import net.sf.l2j.gameserver.model.actor.stat.PcStat;
import net.sf.l2j.gameserver.model.actor.status.PcStatus;
import net.sf.l2j.gameserver.model.base.ClassId;
import net.sf.l2j.gameserver.model.base.Race;
import net.sf.l2j.gameserver.model.base.SubClass;
import net.sf.l2j.gameserver.model.entity.Castle;
import net.sf.l2j.gameserver.model.entity.L2Event;
import net.sf.l2j.gameserver.model.entity.SevenSignsFestival;
import net.sf.l2j.gameserver.model.entity.Siege;
import net.sf.l2j.gameserver.model.quest.Quest;
import net.sf.l2j.gameserver.model.quest.QuestState;
import net.sf.l2j.gameserver.model.waypoint.WayPointNode;
import net.sf.l2j.gameserver.packets.serverpackets.*;
import net.sf.l2j.gameserver.skills.Stats;
import net.sf.l2j.gameserver.tables.*;
import net.sf.l2j.gameserver.templates.*;
import net.sf.l2j.gameserver.util.Broadcast;
import net.sf.l2j.util.Point3D;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;
import java.util.concurrent.ScheduledFuture;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;


/**
 * This class represents all player characters in the world.
 * There is always a client-thread connected to this (except if a player-store is activated upon logout).<BR><BR>
 *
 * @version $Revision: 1.66.2.41.2.33 $ $Date: 2005/04/11 10:06:09 $
 */
public final class L2PcInstance extends L2PlayableInstance
{
    private static final String RESTORE_SKILLS_FOR_CHAR = "SELECT skill_id,skill_level FROM character_skills WHERE char_obj_id=? AND class_index=?";
    private static final String ADD_NEW_SKILL = "INSERT INTO character_skills (char_obj_id,skill_id,skill_level,skill_name,class_index) VALUES (?,?,?,?,?)";
    private static final String UPDATE_CHARACTER_SKILL_LEVEL = "UPDATE character_skills SET skill_level=? WHERE skill_id=? AND char_obj_id=? AND class_index=?";
    private static final String DELETE_SKILL_FROM_CHAR = "DELETE FROM character_skills WHERE skill_id=? AND char_obj_id=? AND class_index=?";
    private static final String DELETE_CHAR_SKILLS = "DELETE FROM character_skills WHERE char_obj_id=? AND class_index=?";

    private static final String ADD_SKILL_SAVE = "INSERT INTO character_skills_save (char_obj_id,skill_id,skill_level,effect_count,effect_cur_time,class_index) VALUES (?,?,?,?,?,?)";
    private static final String RESTORE_SKILL_SAVE = "SELECT skill_id,skill_level,effect_count,effect_cur_time FROM character_skills_save WHERE char_obj_id=? AND class_index=?";
    private static final String DELETE_SKILL_SAVE = "DELETE FROM character_skills_save WHERE char_obj_id=? AND class_index=?";

    
    private static final String RESTORE_CHARACTER = "SELECT account_name, obj_Id, char_name, level, maxHp, curHp, maxCp, curCp, maxMp, curMp, acc, crit, evasion, mAtk, mDef, mSpd, pAtk, pDef, pSpd, runSpd, walkSpd, str, con, dex, _int, men, wit, face, hairStyle, hairColor, sex, heading, x, y, z, movement_multiplier, attack_speed_multiplier, colRad, colHeight, exp, sp, karma, pvpkills, pkkills, clanid, maxload, race, classid, deletetime, cancraft, title, allyId, rec_have, rec_left, accesslevel, online, char_slot, lastAccess, clan_privs, wantspeace, deleteclan, base_class, onlinetime FROM characters WHERE obj_id=?";
    private static final String RESTORE_CHAR_SUBCLASSES = "SELECT class_id,exp,sp,level,classindex FROM character_subclasses WHERE char_obj_id=?";
    private static final String ADD_CHAR_SUBCLASS = "INSERT INTO character_subclasses (char_obj_id,class_id,exp,sp,level,classindex) VALUES (?,?,?,?,?,?)";
    private static final String UPDATE_CHAR_SUBCLASS = "UPDATE character_subclasses SET exp=?,sp=?,level=?,classindex=? WHERE char_obj_id=? AND class_id=?";
    private static final String DELETE_CHAR_SUBCLASS = "DELETE FROM character_subclasses WHERE char_obj_id=? AND class_id=?";
    
    private static final String RESTORE_CHAR_HENNAS = "SELECT slot,symbol_id FROM character_hennas WHERE char_obj_id=? AND class_index=?";
    private static final String ADD_CHAR_HENNA = "INSERT INTO character_hennas (char_obj_id,symbol_id,slot,class_index) VALUES (?,?,?,?)";
    private static final String DELETE_CHAR_HENNA = "DELETE FROM character_hennas WHERE char_obj_id=? AND slot=? AND class_index=?";
    private static final String DELETE_CHAR_HENNAS = "DELETE FROM character_hennas WHERE char_obj_id=? AND class_index=?";
    private static final String DELETE_CHAR_SHORTCUTS = "DELETE FROM character_shortcuts WHERE char_obj_id=? AND class_index=?";

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
	
	private static final int[] COMMON_CRAFT_LEVELS = 
	{
	 	5,20,28,36,43,49,55,62
	};


    private static Logger _log = Logger.getLogger(L2PcInstance.class.getName());

	public class AIAccessor extends L2Character.AIAccessor
	{
		protected AIAccessor() {}
		public L2PcInstance getPlayer() { return L2PcInstance.this; }
		public void doPickupItem(L2Object object) {
			L2PcInstance.this.doPickupItem(object);
		}
		public void doInteract(L2Character target) {
			L2PcInstance.this.doInteract(target);
		}
		public void doAttack(L2Character target) {
			super.doAttack(target);

			for (L2CubicInstance cubic : _cubics.values())
			{
				if (cubic.getId() != L2CubicInstance.LIFE_CUBIC)
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

	/** The Identifier of the L2PcInstance */
	private int _charId = 0x00030b7a;

	/** The Experience of the L2PcInstance before the last Death Penalty */
	private int _expBeforeDeath = 0;

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
    
    /** List with the recomendations that I've give */
    private List<Integer> _recomChars = new FastList<Integer>();

	/** The random number of the L2PcInstance */
	//private static final Random _rnd = new Random();

    private final int baseLoad;
	private int _curWeightPenalty = 0;

	private int _deleteTimer;
	private boolean _inStoreMode;
    private boolean _inCraftMode;
	private PcInventory _inventory = new PcInventory(this);
	private PcWarehouse _warehouse = new PcWarehouse(this);
    private PcFreight _freight = new PcFreight(this);


	/** True if the L2PcInstance is sitting */
	private boolean _waitTypeSitting;
    
    /** True if the L2PcInstance is using the relax skill */
    private boolean _relax;

    /** True if the L2PcInstance is in a boat */
    private boolean _InBoat;
    
    /** Last NPC Id talked on a quest */
    private int questNpcObject = 0; 
    
	/** The face type Identifier of the L2PcInstance */
	private int _face;

	/** The hair style Identifier of the L2PcInstance */
	private int _hairStyle;

	/** The hair color Identifier of the L2PcInstance */
	private int _hairColor;

	/** The table containing all Quests began by the L2PcInstance */
	private Map<String, QuestState> _quests = new FastMap<String, QuestState>();

	/** The list containing all shortCuts of this L2PcInstance */
	private ShortCuts _shortCuts = new ShortCuts(this);

	/** The list containing all macroses of this L2PcInstance */
	private MacroList _macroses = new MacroList(this);

	/** The Alliance Identifier of the L2PcInstance */
	//private int _allyId;

	private L2TradeList _tradeList;
    private L2ManufactureList _createList;
	private List<TradeItem> _sellList;
	private List<TradeItem> _buyList;
	
	private List<L2PcInstance> _SnoopListener = new FastList<L2PcInstance>();
	private List<L2PcInstance> _SnoopedPlayer = new FastList<L2PcInstance>();

	/** The Private Store type of the L2PcInstance (STORE_PRIVATE_NONE=0, STORE_PRIVATE_SELL=1, sellmanage=2, STORE_PRIVATE_BUY=3, buymanage=4, STORE_PRIVATE_MANUFACTURE=5) */
	private int _privatestore;
	private ClassId _skillLearningClassId;

	// hennas
	private final L2HennaInstance[] _henna = new L2HennaInstance[3];
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

	/** The Clan Leader Flag of the L2PcInstance (True : the L2PcInstance is the leader of the clan) */
	private boolean _clanLeader;
	
	private long _deleteClanTime;

	private long _onlineTime;
	private long _onlineBeginTime;
	//GM Stuff
	private boolean _isInvul;
	private boolean _isGm;
	private int _accessLevel;

	private boolean _messageRefusal = false;    // message refusal mode
	private boolean _dietMode = false;          // ignore weight penalty
    private boolean _tradeRefusal = false;       // Trade refusal
    private boolean _exchangeRefusal = false;   // Exchange refusal
	
	public boolean _exploring = false;

	// this is needed to find the inviting player for Party response
	// there can only be one active party request at once
	private L2PcInstance _currentTransactionRequester;
	private L2ItemInstance _arrowItem;

    // Used for protection after teleport
    private long _protectEndTime = 0;
    
	/** The fists L2Weapon of the L2PcInstance (used when no weapon is equiped) */
	private L2Weapon _fistsWeaponItem;

	private long _uptime;
    private String _accountName;
    
    private final Map<Integer, String> _chars = new FastMap<Integer, String>();

	public byte updateKnownCounter = 0;

	/** The table containing all L2RecipeList of the L2PcInstance */
	private Map<Integer, L2RecipeList> _DwarvenRecipeBook = new FastMap<Integer, L2RecipeList>(); 
 	private Map<Integer, L2RecipeList> _CommonRecipeBook = new FastMap<Integer, L2RecipeList>(); 

    private int _mountType;

	// stats watch
	int oldMaxHP;
	int oldMaxMP;

	/** The current higher Expertise of the L2PcInstance (None=0, D=1, C=2, B=3, A=4, S=5)*/
	private int expertiseIndex; // index in EXPERTISE_LEVELS
	int expertisePenalty = 0;

	private L2ItemInstance _enchantScroll = null;

    private int _freightId;
    private boolean _usingFreight;
    private boolean _usingClanWarehouse;
    private boolean _isOnline = false;

    protected boolean _inventoryDisable = false;

    protected Map<Integer, L2CubicInstance> _cubics = new FastMap<Integer, L2CubicInstance>();

	/** The L2FolkInstance corresponding to the last Folk wich one the player talked. */
	private L2FolkInstance _lastFolkNpc = null;

	private boolean _isSilentMoving = false;

    protected Set<Integer> _activeSoulShots = new FastSet<Integer>();
	private boolean isPathNodeMode;
	private boolean isPathNodesVisible;
	private Map<WayPointNode, List<WayPointNode>> pathNodeMap;
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

    /** Event parameters */
   
    public int eventX;
    public int eventY;
    public int eventZ;
    public int eventkarma;
    public int eventpvpkills;
    public int eventpkkills;
    public String eventTitle;
    public LinkedList<String> kills = new LinkedList<String>();
    public boolean eventSitForced = false;
    public boolean atEvent = false;
    
    public int _telemode = 0;

	/** new loto ticket **/
	public int _loto[] = new int[5];
	//public static int _loto_nums[] = {0,1,2,3,4,5,6,7,8,9,};
	/** new race ticket **/
	public int _race[] = new int[2];
    
    private final BlockList _blockList = new BlockList();
    private boolean _isConnected = true;
    
    private boolean _hero = false;
    private int _team = 0;
    private int _wantsPeace = 0;
    
    /** The list of sub-classes this character has. */
    protected List<SubClass> _subClasses = new FastList<SubClass>();
    protected int _baseClass;
    protected int _activeClass;
    protected int _classIndex = 0;

    private boolean _isProcessingTransaction = false;
    private long _lastAccess; 
    private int _boatId;
    

    /** Stored from last ValidatePosition **/
    public Point3D _lastClientPosition = new Point3D(0, 0, 0);
    public Point3D _lastServerPosition = new Point3D(0, 0, 0);
    
    /** Bypass validations */
    private List<String> _validBypass = new FastList<String>();
    private List<String> _validBypass2 = new FastList<String>();
    
    private boolean _inCrystallize;
    
    private BuySellList _BuySellList = null;

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
        
        // Set the base class ID to that of the actual class ID.
        player.setBaseClass(player.getClassId());
        
        // Add the player in the characters table of the database
        boolean ok = player.createDb();
        
        if (!ok)
            return null;
        
        return player;
    }
    
    public static L2PcInstance createDummyPlayer(int objectId, String name)
    {   
        // Create a new L2PcInstance with an account name
        L2PcInstance player = new L2PcInstance(objectId);
        player.setName(name);
        
        return player;
    } 

    public String getAccountName()
    {
        if (_connection == null) return "";
        return _connection.getClient().getLoginName();
    }

    public Map<Integer, String> getAccountChars()
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
        super.setKnownList(new PcKnownList(new L2PcInstance[] {this}));
        super.setStat(new PcStat(new L2PcInstance[] {this}));
        super.setStatus(new PcStatus(new L2PcInstance[] {this}));

        _accountName  = accountName;
		_nameColor    = 0xFFFFFF;
        baseLoad      = template.baseLoad;

		// Create an AI
		_ai = new L2PlayerAI(new L2PcInstance.AIAccessor());

		// Create a L2Radar object
		radar = new L2Radar(this);

		// Retrieve from the database all skills of this L2PcInstance and add them to _skills
		// Retrieve from the database all items of this L2PcInstance and add them to _inventory
        getInventory().restore();
    }
    
    private L2PcInstance(int objectId)
    {
        super(objectId, null);
        super.setKnownList(new PcKnownList(new L2PcInstance[] {this}));
        super.setStat(new PcStat(new L2PcInstance[] {this}));
        super.setStatus(new PcStatus(new L2PcInstance[] {this}));

        baseLoad = 0;
    }

    public final PcKnownList getKnownList() { return (PcKnownList)super.getKnownList(); }
    public final PcStat getStat() { return (PcStat)super.getStat(); }
    public final PcStatus getStatus() { return (PcStatus)super.getStatus(); }

    /**
     * Return the base L2PcTemplate link to the L2PcInstance.<BR><BR>
     */
    public final L2PcTemplate getBaseTemplate()
    {
        return CharTemplateTable.getInstance().getTemplate(_baseClass, getSex() == 1);
    }
    
    /** Return the L2PcTemplate link to the L2PcInstance. */
    public final L2PcTemplate getTemplate() { return (L2PcTemplate)super.getTemplate(); }
    
    public void setTemplate(ClassId newclass, boolean female) { super.setTemplate(CharTemplateTable.getInstance().getTemplate(newclass, female)); }
    
    public void changeSex() { super.setTemplate(CharTemplateTable.getInstance().getTemplate(getClassId(), (getSex() == 1))); }

	/**
	 * Return the AI of the L2PcInstance (create it if necessary).<BR><BR>
	 */
	public L2CharacterAI getAI()
	{
		if (_ai == null)
		{
			synchronized(this)
			{
				if (_ai == null)
					_ai = new L2PlayerAI(new L2PcInstance.AIAccessor());
			}
		}
		return _ai;
	}


	/**
	 * Calculate a destination to explore the area and set the AI Intension to AI_INTENTION_MOVE_TO.<BR><BR>
	 */
	public void explore()
	{
		if (!_exploring) return;

		if(getMountType() == 2)
		    return;

		// Calculate the destination point (random)
		
		int x = getX()+Rnd.nextInt(6000)-3000;
		int y = getY()+Rnd.nextInt(6000)-3000;
		if (x > Universe.MAX_X) x = Universe.MAX_X;
		if (x < Universe.MIN_X) x = Universe.MIN_X;
		if (y > Universe.MAX_Y) y = Universe.MAX_Y;
		if (y < Universe.MIN_Y) y = Universe.MIN_Y;

		int z = getZ();

		L2CharPosition pos = new L2CharPosition(x,y,z,0);

		// Set the AI Intention to AI_INTENTION_MOVE_TO
		getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO,pos);

	}

	/** Return the Level of the L2PcInstance. */
	public final int getLevel() { return getStat().getLevel(); }

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
    
    public void setBaseClass(int baseClass)
    {
        _baseClass = baseClass;
    }

    public void setBaseClass(ClassId classId)
    {
        _baseClass = classId.ordinal();
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
	 * Manage Logout Task.<BR><BR>
	 */
    public void logout()
    {
		// Delete all Path Nodes
    	clearPathNodes();

		// Close the connection with the client
        if (_connection != null) _connection.close();
    }


	/**
	 * Return a table containing all Common L2RecipeList of the L2PcInstance.<BR><BR> 
	 */ 
    public L2RecipeList[] getCommonRecipeBook() 
    { 
    	return _CommonRecipeBook.values().toArray(new L2RecipeList[_CommonRecipeBook.values().size()]);
    }
    
 	/** 
 	 * Return a table containing all Dwarf L2RecipeList of the L2PcInstance.<BR><BR> 
 	 */ 
    public L2RecipeList[] getDwarvenRecipeBook() 
    { 
    	return _DwarvenRecipeBook.values().toArray(new L2RecipeList[_DwarvenRecipeBook.values().size()]);
    } 
    
 	/** 
 	 * Add a new L2RecipList to the table _commonrecipebook containing all L2RecipeList of the L2PcInstance <BR><BR> 
 	 * 
 	 * @param recipe The L2RecipeList to add to the _recipebook 
 	 * 
     */ 
    public void registerCommonRecipeList(L2RecipeList recipe) 
    { 
    	_CommonRecipeBook.put(recipe.getId(), recipe); 
    }

	/**
	 * Add a new L2RecipList to the table _recipebook containing all L2RecipeList of the L2PcInstance <BR><BR>
	 *
	 * @param recipe The L2RecipeList to add to the _recipebook
	 *
	 */
    public void registerDwarvenRecipeList(L2RecipeList recipe) 
    {
    	_DwarvenRecipeBook.put(recipe.getId(), recipe); 
    } 
    
    /** 
 	 * @param RecipeID The Identifier of the L2RecipeList to check in the player's recipe books 
 	 * 
 	 * @return  
 	 * <b>TRUE</b> if player has the recipe on Common or Dwarven Recipe book else returns <b>FALSE</b> 
 	 */ 
    public boolean hasRecipeList(int recipeId) 
    { 
    	if (_DwarvenRecipeBook.containsKey(recipeId)) 
    		return true; 
    	else if(_CommonRecipeBook.containsKey(recipeId)) 
    		return true; 
    	else 
    		return false; 
    } 
    
    
 	/** 
 	 * Tries to remove a L2RecipList from the table _DwarvenRecipeBook or from table _CommonRecipeBook, those table contain all L2RecipeList of the L2PcInstance <BR><BR> 
	 *
	 * @param RecipeID The Identifier of the L2RecipeList to remove from the _recipebook
	 *
	 */
    public void unregisterRecipeList(int recipeId) 
    {
    	if (_DwarvenRecipeBook.containsKey(recipeId)) 
    		_DwarvenRecipeBook.remove(recipeId); 
    	else if(_CommonRecipeBook.containsKey(recipeId)) 
    		_CommonRecipeBook.remove(recipeId); 
    	else  
    		_log.warning("Attempted to remove unknown RecipeList: "+recipeId);
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
	public void fillQuestDrops(L2NpcInstance npc, List<L2DropData> drops)
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
		FastList<Quest> quests = new FastList<Quest>();

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

    
 // Custom PVP Color System - Start
	public void updatePvPColor(int pvpKillAmount) {
		if (Config.PVP_COLOR_SYSTEM_ENABLED) {
			// Check if the character has GM access and if so, let them be.
			if (isGM())
				return;
			{
				if ((pvpKillAmount >= (Config.PVP_AMOUNT1))
						&& (pvpKillAmount < (Config.PVP_AMOUNT2))) {
					getAppearance().setNameColor(
							Config.NAME_COLOR_FOR_PVP_AMOUNT1);
				} else if ((pvpKillAmount >= (Config.PVP_AMOUNT2))
						&& (pvpKillAmount < (Config.PVP_AMOUNT3))) {
					getAppearance().setNameColor(
							Config.NAME_COLOR_FOR_PVP_AMOUNT2);
				} else if ((pvpKillAmount >= (Config.PVP_AMOUNT3))
						&& (pvpKillAmount < (Config.PVP_AMOUNT4))) {
					getAppearance().setNameColor(
							Config.NAME_COLOR_FOR_PVP_AMOUNT3);
				} else if ((pvpKillAmount >= (Config.PVP_AMOUNT4))
						&& (pvpKillAmount < (Config.PVP_AMOUNT5))) {
					getAppearance().setNameColor(
							Config.NAME_COLOR_FOR_PVP_AMOUNT4);
				} else if (pvpKillAmount >= (Config.PVP_AMOUNT5)) {
					getAppearance().setNameColor(
							Config.NAME_COLOR_FOR_PVP_AMOUNT5);
				}
			}
		}
	}

	// Custom PVP Color System - End

	// Custom Pk Color System - Start
	public void updatePkColor(int pkKillAmount) {
		if (Config.PK_COLOR_SYSTEM_ENABLED) {
			// Check if the character has GM access and if so, let them be, like
			// above.
			if (isGM())
				return;
			{
				if ((pkKillAmount >= (Config.PK_AMOUNT1))
						&& (pkKillAmount < (Config.PVP_AMOUNT2))) {
					getAppearance().setTitleColor(
							Config.TITLE_COLOR_FOR_PK_AMOUNT1);
				} else if ((pkKillAmount >= (Config.PK_AMOUNT2))
						&& (pkKillAmount < (Config.PVP_AMOUNT3))) {
					getAppearance().setTitleColor(
							Config.TITLE_COLOR_FOR_PK_AMOUNT2);
				} else if ((pkKillAmount >= (Config.PK_AMOUNT3))
						&& (pkKillAmount < (Config.PVP_AMOUNT4))) {
					getAppearance().setTitleColor(
							Config.TITLE_COLOR_FOR_PK_AMOUNT3);
				} else if ((pkKillAmount >= (Config.PK_AMOUNT4))
						&& (pkKillAmount < (Config.PVP_AMOUNT5))) {
					getAppearance().setTitleColor(
							Config.TITLE_COLOR_FOR_PK_AMOUNT4);
				} else if (pkKillAmount >= (Config.PK_AMOUNT5)) {
					getAppearance().setTitleColor(
							Config.TITLE_COLOR_FOR_PK_AMOUNT5);
				}
			}
		}
	}

	// Custom Pk Color System - End

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
			Quest q = QuestManager.getInstance().getQuest(quest);
			if (q == null)
				return retval;
			qs = q.newQuestState(this);
		}
		if (qs != null) {
            if (getLastQuestNpcObject() > 0)
            {
                L2Object object = L2World.getInstance().findObject(getLastQuestNpcObject());
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

		return retval;
	}

	private void showQuestWindow(String questId, String stateId)
    {
        String path = "data/jscript/quests/"+questId+"/"+stateId+".htm";
        String content = HtmCache.getInstance().getHtm(path);  //TODO path for quests html
		
		if (content != null)
        {
			if (Config.DEBUG)
				_log.fine("Showing quest window for quest "+questId+" state "+stateId+" html path: " + path);
            
			NpcHtmlMessage npcReply = new NpcHtmlMessage(5);
			npcReply.setHtml(content);
			sendPacket(npcReply);
		}

		sendPacket( new ActionFailed() );
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
	 * Return True if the L2PcInstance can Craft Dwarven Recipes.<BR><BR> 
 	 */ 
	public boolean hasDwarvenCraft() 
	{ 
		return getSkillLevel(L2Skill.SKILL_CREATE_DWARVEN) >= 1; 
	} 

	public int getDwarvenCraft() 
	{ 
		return getSkillLevel(L2Skill.SKILL_CREATE_DWARVEN); 
	} 
	
 	/** 
 	 * Return True if the L2PcInstance can Craft Dwarven Recipes.<BR><BR> 
 	 */ 
	public boolean hasCommonCraft() 
	{ 
		return getSkillLevel(L2Skill.SKILL_CREATE_COMMON) >= 1;
	}

	public int getCommonCraft() 
	{ 
		return getSkillLevel(L2Skill.SKILL_CREATE_COMMON);
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
     * Increment the number of recommandation obtained by the L2PcInstance (Max : 255).<BR><BR>
     */
    protected void incRecomHave()
    {
        if (_recomHave < 255)
            _recomHave++;
    }
    
    /**
     * Set the number of recommandation obtained by the L2PcInstance (Max : 255).<BR><BR>
     */
    public void setRecomHave(int value)
    {
        if (value > 255)
            _recomHave = 255;
        else if (value < 0)
            _recomHave = 0;
        else
            _recomHave = value;
    }
    
    

    /**
     * Return the number of recommandation that the L2PcInstance can give.<BR><BR>
     */
    public int getRecomLeft()
    {
        return _recomLeft;
    }

    /**
     * Increment the number of recommandation that the L2PcInstance can give.<BR><BR>
     */
    protected void decRecomLeft()
    {
        if (_recomLeft > 0)
            _recomLeft--;
    }

    public void giveRecom(L2PcInstance target)
    {
        target.incRecomHave();
        decRecomLeft();
        _recomChars.add(target.getName().hashCode());
    }
    
    public boolean canRecom(L2PcInstance target)
    {
        return !_recomChars.contains(target.getName().hashCode());
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
		if (karma < 0) karma = 0;
		if (_karma == 0 && karma > 0)
		{
			for (L2Object object : getKnownList().getKnownObjects())
			{
				if (object == null || !(object instanceof L2GuardInstance)) continue;

				if (((L2GuardInstance)object).getAI().getIntention() == CtrlIntention.AI_INTENTION_IDLE)
					((L2GuardInstance)object).getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE,null);
			}
		}
        else if (_karma > 0 && karma == 0)
        {
            // Send a Server->Client StatusUpdate packet with Karma and PvP Flag to the L2PcInstance and all L2PcInstance to inform (broadcast)
            setKarmaFlag(0);
        }

		_karma = karma;
		updateKarma();
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
            setIsOverloaded(getCurrentLoad() > getMaxLoad());
            int weightproc = getCurrentLoad() * 1000 / getMaxLoad();
            int newWeightPenalty;
            if ( weightproc < 500) newWeightPenalty = 0;
            else if ( weightproc < 666) newWeightPenalty = 1;
            else if ( weightproc < 800) newWeightPenalty = 2;
            else if ( weightproc < 1000) newWeightPenalty = 3;
            else newWeightPenalty = 4;

            if (_curWeightPenalty != newWeightPenalty)
            {
                _curWeightPenalty = newWeightPenalty;
                if (newWeightPenalty > 0) 
                    super.addSkill(SkillTable.getInstance().getInfo(4270,newWeightPenalty));
                else super.removeSkill(getKnownSkill(4270));

                super.updateEffectIcons();
            }
		}
	}

    public void refreshExpertisePenalty()
    {
	if (Config.GRADE_PENALTY) {
        int newPenalty = 0;
        for (L2ItemInstance item : getInventory().getItems())
        {
            if (item.isEquipped())
		    {
			int crystaltype = item.getItem().getCrystalType();
			if (crystaltype > newPenalty)
			    newPenalty = crystaltype;
		    }
        }
        newPenalty = newPenalty - getExpertiseIndex();
        if (newPenalty <= 0) newPenalty = 0;

        if (expertisePenalty != newPenalty)
        {
            expertisePenalty = newPenalty;
            if (newPenalty > 0) 
                super.addSkill(SkillTable.getInstance().getInfo(4267, newPenalty));
            else super.removeSkill(getKnownSkill(4267));

            super.updateEffectIcons();
        }
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
        _activeClass = Id;
        L2PcTemplate t = CharTemplateTable.getInstance().getTemplate(Id, getSex()==1);

		if (t == null)
		{
			_log.severe("Missing template for classId: "+Id);
			throw new Error();
		}

		// Set the template of the L2PcInstance
		setTemplate(t);
	}

	/** Return the Experience of the L2PcInstance. */
	public int getExp() { return getStat().getExp(); }


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
	public void rewardSkills()
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
				setExpertiseIndex(i);
		}

		// Add the Expertise skill corresponding to its Expertise level
        if (getExpertiseIndex() > 0)
		{
            L2Skill skill = SkillTable.getInstance().getInfo(239, getExpertiseIndex());
            addSkill(skill);

            if (Config.DEBUG) _log.fine("awarded "+getName()+" with new expertise.");

        }
		else
		{
			if (Config.DEBUG) _log.fine("No skills awarded at lvl: "+lvl);
		}
        
	if (Config.AUTO_LEARN_SKILLS) {
        boolean countUnlearnable = true;
        int unLearnable = 0;
        int skillCounter = 0;
        L2SkillLearn[] skills = SkillTreeTable.getInstance().getAvailableSkills(this, this.getClassId());
        while(skills.length > unLearnable)
        {
            for (L2SkillLearn s : skills)
            {
                L2Skill sk = SkillTable.getInstance().getInfo(s.getId(), s.getLevel());
                if (sk == null || !sk.getCanLearn(this.getClassId()))
                {
                	if(countUnlearnable)
                		unLearnable++;
                    continue;
                }
                if(this.getSkillLevel(sk.getId()) == -1)
                {
                    skillCounter++;
                }
                this.addSkill(sk);
            }
            countUnlearnable = false;
            skills = SkillTreeTable.getInstance().getAvailableSkills(this, this.getClassId());
        }
            this.sendMessage("You have learn new " + skillCounter + " skills.");
        }

	if (_connection.getClient().getRevision() > 600)
	{
        //Active skill dwarven craft
        
        if (getSkillLevel(1321) < 1 && getRace() == Race.dwarf)
        {
        	L2Skill skill = SkillTable.getInstance().getInfo(1321,1);
        	addSkill(skill);
        }
        
        //Active skill common craft
        if (((getSkillLevel(1322) < 1) && (_connection.getClient().getRevision() >= 640)) || (getRace() == Race.dwarf)) // require C4 Client to insert skill common craft for normal class
        {
        	L2Skill skill = SkillTable.getInstance().getInfo(1322,1);
        	addSkill(skill);
        }
        
        for(int i = 0; i < COMMON_CRAFT_LEVELS.length; i++)
        {
        	if(lvl >= COMMON_CRAFT_LEVELS[i] && getSkillLevel(1320) < (i+1))
        	{
        		L2Skill skill = SkillTable.getInstance().getInfo(1320, (i+1));
        		addSkill(skill);
        	}
        }
	}

		// This function gets called on login, so not such a bad place to check weight
        refreshOverloaded();		// Update the overloaded status of the L2PcInstance
        refreshExpertisePenalty();  // Update the expertise status of the L2PcInstance
	}


	/** Set the Experience value of the L2PcInstance. */
	public void setExp(int exp) { getStat().setExp(exp); }

	/**
	 * Return the Race object of the L2PcInstance.<BR><BR>
	 */
	public Race getRace()
	{
        if (!isSubClassActive())
            return getTemplate().race;
        
        boolean isFemale = (getSex() == 1);
        L2PcTemplate charTemp = CharTemplateTable.getInstance().getTemplate(_baseClass, isFemale);

        return charTemp.race;
    }

    /** Return the SP amount of the L2PcInstance. */
    public int getSp() { return getStat().getSp(); }

    /** Set the SP amount of the L2PcInstance. */
    public void setSp(int sp) { super.getStat().setSp(sp); }

    /**
     * @return
     */
    public int getPvpFlag()
    {
        return _pvpFlag;
    }
    /** 
 	* Return true if this L2PcInstance is a clan leader in  
 	* ownership of the passed castle 
 	*/ 
 	public boolean isCastleLord(int castleId) 
 	{ 
 		L2Clan clan = getClan(); 
 		// player has clan and is the clan leader, check the castle info 
 		if ((clan != null) && (clan.getLeader().getPlayerInstance() == this)) 
 		{ 
 			// if the clan has a castle and it is actually the queried castle, return true 
 			Castle castle = CastleManager.getInstance().getCastleByOwner(clan); 
 			if ((castle != null) && (castle == CastleManager.getInstance().getCastle(castleId))) 
 				return true; 
 		} 
 		return false; 
 	}

    /**
     * Return the Clan Identifier of the L2PcInstance.<BR><BR>
     */
    public int getClanId()
    {
        return _clanId;
    }

    /**
     * Return the Clan Crest Identifier of the L2PcInstance or 0.<BR><BR>
     */
    public int getClanCrestId()
    {        
        if (_clan != null && _clan.hasCrest())
        {
            return _clan.getCrestId();
        }
        return 0;
    }
    
    /**
     * @return The Clan CrestLarge Identifier or 0
     */
    public int getClanCrestLargeId()
    {        
        if (_clan != null && _clan.hasCrestLarge())
        {
            return _clan.getCrestLargeId();
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
	public void setDeleteClanCurTime()
	{
		_deleteClanTime = System.currentTimeMillis();
	}
	public void setOnlineTime(long time)
	{
		_onlineTime = time;
		_onlineBeginTime = System.currentTimeMillis();
	}
	public boolean canJoinClan()
	{
		if (!Config.CLAN_PENALTY)
		return true;
		else
		return (_deleteClanTime == 0) || ((System.currentTimeMillis()-_deleteClanTime) >= 5*24*60*60*1000);
	}
	public boolean canCreateClan()
	{
		if (!Config.CLAN_PENALTY)
		return true;
		else
		return (_deleteClanTime == 0) || ((System.currentTimeMillis()-_deleteClanTime) >= 10*24*60*60*1000);
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
     * Set character is sitting in server without sending packet (Throne).<BR><BR>
     */
    public void sitDown2()
    {
        _waitTypeSitting = true;
    }

	/**
	 * Sit down the L2PcInstance, set the AI Intention to AI_INTENTION_REST and send a Server->Client ChangeWaitType packet (broadcast)<BR><BR>
	 */
	public void sitDown()
	{
		if ((!_waitTypeSitting)&&(!isAttackingDisabled())&&(!isAlikeDead()))//use attackDissabled for now
		{
            abortAttack();
            abortCast();

            _waitTypeSitting = true;
			getAI().setIntention(CtrlIntention.AI_INTENTION_REST);
			broadcastPacket(new ChangeWaitType (this, ChangeWaitType.WT_SITTING));
		}
	}

	/**
	 * Stand up the L2PcInstance, set the AI Intention to AI_INTENTION_IDLE and send a Server->Client ChangeWaitType packet (broadcast)<BR><BR>
	 */
	public void standUp()
	{
		if (L2Event.active && eventSitForced)
        {
            sendMessage("A dark force beyond your mortal understanding makes your knees to shake when you try to stand up ...");
        }
        else
        {
        	
        	if (_waitTypeSitting && !_inStoreMode && !isAlikeDead())
        
		{
            if (_relax)
            {
                setRelax(false);
                stopEffects(L2Effect.EffectType.RELAXING);
            }
			_waitTypeSitting = false;
			getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
			broadcastPacket(new ChangeWaitType (this, ChangeWaitType.WT_STANDING));
		}
        }
	}
    
    /**
     * Set the value of the _relax value. Must be True if using skill Relax and False if not. 
     */
    public void setRelax(boolean val)
    {
        _relax = val;
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
		reduceAdena(adena, false, false);
	}
	public void reduceAdena(int adena, boolean updateClient, boolean showMessage)
	{
		_inventory.reduceAdena(adena);
		_inventory.findItemByItemId(57).updateDatabase();
		if (updateClient)
		    sendUpdateAdena();
		if (showMessage)
		{
		    SystemMessage sm = new SystemMessage(SystemMessage.DISSAPEARED_ADENA);
		    sm.addNumber(adena);
		    sendPacket(sm);
		}
		    
	}

	/**
	 * Add Adena to the L2PcInstance.<BR><BR>
	 */
	public void addAdena(int adena)
	{
		addAdena(adena, false, false);
	}
	public void addAdena(int adena, boolean updateClient, boolean showMessage)
	{
		_inventory.addAdena(adena);
		_inventory.findItemByItemId(57).updateDatabase();
		if (updateClient)
		    sendUpdateAdena();
		if (showMessage)
		{
		    SystemMessage sm = new SystemMessage(SystemMessage.EARNED_ADENA);
		    sm.addNumber(adena);
		    sendPacket(sm);
		}
	}

	public void sendUpdateAdena ()
	{
	    InventoryUpdate iu = new InventoryUpdate();
	    L2ItemInstance itemadena = _inventory.findItemByItemId(57);
	    iu.addModifiedItem(itemadena);
	    sendPacket(iu);
	}

    /**
     * Set _protectEndTime according settings.
     */
    public void setProtection(boolean protect)
    {
        if (Config.DEVELOPER && (protect || _protectEndTime > 0)) System.out.println(this.getName() + ": Protection " + (protect?"ON " + (GameTimeController.getGameTicks() + Config.PLAYER_SPAWN_PROTECTION * GameTimeController.TICKS_PER_SECOND) :"OFF") + " (currently " + GameTimeController.getGameTicks() + ")");
        _protectEndTime = protect ? GameTimeController.getGameTicks() + Config.PLAYER_SPAWN_PROTECTION * GameTimeController.TICKS_PER_SECOND : 0;
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
		if (_connection != null) _connection.close();
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
	public void onAction(L2PcInstance player)
	{
		// Check if the L2PcInstance is confused
		if (player.isConfused())
		{
			// Send a Server->Client packet ActionFailed to the player
			ActionFailed af = new ActionFailed();
			player.sendPacket(af);
		}


		// Check if the other player already target this L2PcInstance
		if (player.getTarget() != this)
		{
			// Set the target of the player
			player.setTarget(this);

			// Send a Server->Client packet MyTargetSelected to the player
			// The color to display in the select window is White
			MyTargetSelected my = new MyTargetSelected(getObjectId(), 0);
			player.sendPacket(my);

		}
		else
		{
			// Check if this L2PcInstance has a Private Store
		    if (getPrivateStoreType() != 0 )
		        player.getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, this);
		    else
		    {
				// Check if this L2PcInstance is autoAttackable
		        if (isAutoAttackable(player))
		            player.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, this);
		        else
		            player.getAI().setIntention(CtrlIntention.AI_INTENTION_FOLLOW, this);
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
		//super.broadcastStatusUpdate();

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
			PartySmallWindowUpdate update = new PartySmallWindowUpdate(this);
			getParty().broadcastToPartyMembers(this, update);
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
	public final void broadcastUserInfo()
	{
		// Send a Server->Client packet UserInfo to this L2PcInstance
		UserInfo ui = new UserInfo(this);
		this.sendPacket(ui);

		// Send a Server->Client packet CharInfo to all L2PcInstance in _KnownPlayers of the L2PcInstance
		CharInfo info = new CharInfo(this);
		if (Config.DEBUG) _log.fine("players to notify:" + getKnownList().getKnownPlayers().size() + " packet:"+info.getType());
		Broadcast.toKnownPlayers(this, info);
	}

	/**
	 * Return the Alliance Identifier of the L2PcInstance.<BR><BR>
	 */
	public int getAllyId()
	{
//		return _allyId;
	    if (_clan == null)
	        return 0;
	    return _clan.getAllyId();
	}

    public int getAllyCrestId()
    {
        if (getAllyId() == 0)
            return 0;
        return _clan.getAllyCrestId();
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
                SystemMessage smsg = new SystemMessage(SystemMessage.YOU_PICKED_UP_S1_ADENA); // you picked up $s1 adena
                smsg.addNumber(target.getCount());
                sendPacket(smsg);
            }
			else
			{
                SystemMessage smsg = new SystemMessage(SystemMessage.YOU_PICKED_UP_S1_S2); // you picked up $s1$s2
                smsg.addItemName(target.getItemId());
                smsg.addNumber(target.getCount());
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
    	if (isAlikeDead() || isFakeDeath())
    		return;
    	
		// Set the AI Intention to AI_INTENTION_IDLE
		getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);

		// Check if the L2Object to pick up is a L2ItemInstance
		if (! (object instanceof L2ItemInstance))
		{
			// dont try to pickup anything that is not an item :)
			_log.warning("trying to pickup wrong target."+getTarget());
			return;
		}

		L2ItemInstance target = (L2ItemInstance) object;

		int ItemOwner = 0;                       
        
		if(isInParty())
		{
			for (L2PcInstance member : _party.getPartyMembers())
			{
			if (member.getObjectId() == target.getOwnerId() || target.getOwnerId() == 0  || (member.getPet() != null && member.getPet().getObjectId() == target.getOwnerId())) ItemOwner = 1;
			}
		} else {
			if(this.getObjectId() == target.getOwnerId() || target.getOwnerId() == 0 || (this.getPet() != null && this.getPet().getObjectId() == target.getOwnerId())) ItemOwner = 1;
		}        
		if(ItemOwner == 1)
		{ 
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
                sendPacket(new SystemMessage(SystemMessage.SLOTS_FULL));
                return;
            }

			// Remove the L2ItemInstance from the world and send server->client GetItem packets
			target.pickupMe(this);
		}


		// Check if a Party is in progress
		if  (!isInParty())
		{
			if (target.getItemId() == 57)
			{
				// Send a System message to this L2PcInstance
				SystemMessage smsg = new SystemMessage(SystemMessage.YOU_PICKED_UP_S1_ADENA); // you picked up $s1 adena
				smsg.addNumber(target.getCount());
				sendPacket(smsg);
			}
			else
			{
				// Send a System message to this L2PcInstance
				SystemMessage smsg = new SystemMessage(SystemMessage.YOU_PICKED_UP_S1_S2); // you picked up $s1$s2
				smsg.addItemName(target.getItemId());
				smsg.addNumber(target.getCount());
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
		} else {
			SystemMessage sm2 = new SystemMessage(614);
			sm2.addString("No priority on the item");
			sendPacket(sm2);
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
			TargetSelected my = new TargetSelected(getObjectId(), newTarget.getObjectId(), getX(), getY(), getZ());
			broadcastPacket(my);
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
    
    public L2ItemInstance getChestArmorInstance()
    {
        return getInventory().getPaperdollItem(Inventory.PAPERDOLL_CHEST);
    }
    
    public L2Armor getActiveChestArmorItem()
    {
        L2ItemInstance armor = getChestArmorInstance();
        
        if (armor == null)
            return null;
        
        return (L2Armor) armor.getItem();
    }
    
    public boolean isWearingHeavyArmor()
    {
        L2ItemInstance armor = getChestArmorInstance();
        if ((L2ArmorType)armor.getItemType() == L2ArmorType.HEAVY )
            return true;
        return false;
    }
    
    public boolean isWearingLightArmor()
    {
        L2ItemInstance armor = getChestArmorInstance();
        if ((L2ArmorType)armor.getItemType() == L2ArmorType.LIGHT )
            return true;
        return false;
    }
    
    public boolean isWearingMagicArmor()
    {
        L2ItemInstance armor = getChestArmorInstance();
        if ((L2ArmorType)armor.getItemType() == L2ArmorType.MAGIC )
            return true;
        return false;
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


	/**
	 * Kill the L2Character, Apply Death Penalty, Manage gain/loss Karma and Item Drop.<BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Reduce the Experience of the L2PcInstance in function of the calculated Death Penalty </li>
	 * <li>If necessary, unsummon the Pet of the killed L2PcInstance </li>
	 * <li>Manage Karma gain for attacker and Karam loss for the killed L2PcInstance </li>
	 * <li>If the killed L2PcInstance has Karma, manage Drop Item</li>
	 * <li>Kill the L2PcInstance </li><BR><BR>
	 *
	 *
	 * @param i The HP decrease value
	 * @param attacker The L2Character who attacks
	 *
	 */
	public void doDie(L2Character killer)
	{
        // Kill the L2PcInstance
        super.doDie(killer);

        if (killer != null)
        {
        	if (atEvent && killer instanceof L2PcInstance)
            {
                L2PcInstance pk = (L2PcInstance) killer;
                pk.kills.add(getName());
            }
            if (!ArenaManager.getInstance().checkIfInZone(this))
            {
                if (Config.ALT_GAME_DELEVEL)
                {
                    // Reduce the Experience of the L2PcInstance in function of the calculated Death Penalty
                    // NOTE: deathPenalty +- Exp will update karma
                    if (getSkillLevel(L2Skill.SKILL_LUCKY) < 0 || getStat().getLevel() > 4)
                        deathPenalty((killer instanceof L2PcInstance && ((L2PcInstance)killer).getClan() != null && this.getClan() != null && this.getClan().isAtWarWith(((L2PcInstance)killer).getClanId())>0));
                }
                else
                    onDieUpdateKarma(); // Update karma if delevel is not allowed
            }

            onDieDropItem(killer);  // Check if any item should be dropped
        }

        setPvpFlag(0);              // Clear the pvp flag

        // Unsummon the Pet
        if (getPet() != null) getPet().unSummon(this);
        
        // Unsummon Cubics
        if (_cubics.size() > 0) {
            for (L2CubicInstance cubic : _cubics.values())
            {
                cubic.stopAction();
                cubic.cancelDisappear();
            }
            _cubics.clear();
        }

        stopRentPet();
        stopWaterTask();
    }

    private void onDieDropItem(L2Character killer)
    {
        if (atEvent || killer == null) return;
        
        if (!ZoneManager.getInstance().checkIfInZonePvP(this) && (!isGM() || Config.KARMA_DROP_GM))
        {
            boolean isKarmaDrop = false;
            boolean isKillerNpc = (killer instanceof L2NpcInstance);
            int pkLimit = Config.KARMA_PK_LIMIT;;

            int dropEquip           = 0;
            int dropEquipWeapon     = 0;
            int dropItem            = 0;
            int dropLimit           = 0;
            int dropPercent         = 0;
            
            if (!isKillerNpc && getKarma() > 0 && this.getPkKills() >= pkLimit)
            {
                isKarmaDrop = true;
                dropPercent = Config.KARMA_RATE_DROP;
                dropEquip = Config.KARMA_RATE_DROP_EQUIP;
                dropEquipWeapon = Config.KARMA_RATE_DROP_EQUIP_WEAPON;
                dropItem = Config.KARMA_RATE_DROP_ITEM;
                dropLimit = Config.KARMA_DROP_LIMIT;
            }
            else if (isKillerNpc && getLevel() > 4 && !isFestivalParticipant())
            {
                dropPercent = Config.PLAYER_RATE_DROP;
                dropEquip = Config.PLAYER_RATE_DROP_EQUIP;
                dropEquipWeapon = Config.PLAYER_RATE_DROP_EQUIP_WEAPON;
                dropItem = Config.PLAYER_RATE_DROP_ITEM;
                dropLimit = Config.PLAYER_DROP_LIMIT;
            }

            if (dropPercent > 0 && Rnd.get(100) + dropPercent >= 100)
            {
                List<Integer> nonDroppableList = new FastList<Integer>();
                List<Integer> nonDroppableListPet = new FastList<Integer>();
                
                nonDroppableList = Config.KARMA_LIST_NONDROPPABLE_ITEMS;
                nonDroppableListPet = Config.KARMA_LIST_NONDROPPABLE_ITEMS;

                InventoryUpdate iu;
                SystemMessage sm;
                
                int dropCount = 0;
                for (L2ItemInstance itemDrop : getInventory().getItems())
                {
                    // Don't drop
                    if (
                            itemDrop.getItemId() == 57 ||                           // Adena
                            itemDrop.getItem().getType2() == L2Item.TYPE2_QUEST ||  // Quest Items
                            nonDroppableList.contains(itemDrop.getItemId()) ||      // Item listed in the non droppable item list
                            nonDroppableListPet.contains(itemDrop.getItemId())      // Item listed in the non droppable pet item list
                        ) continue;

                    if (!itemDrop.isEquipped()) // Item is not equiped
                    {
                        if (getPet() != null && getPet().getControlItemId() == itemDrop.getItemId()) continue;
                        if (Rnd.get(100) - (dropCount * 2) + dropItem < 100) continue;
                    }
                    else // Item equiped
                    {
                        if (itemDrop.getItem().getType2() == L2Item.TYPE2_WEAPON) // Is a weapn
                            if (Rnd.get(100) - (dropCount * 2) + dropEquip < 100) continue;
                        else // Is not a weapon
                            if (Rnd.get(100) - (dropCount * 2) + dropEquipWeapon < 100) continue;

                        getInventory().unEquipItemInSlotAndRecord(itemDrop.getEquipSlot());
                    }
                    // NOTE: Each time an item is dropped, the chance of another item being dropped gets lesser (dropCount * 2)

                    itemDrop = getInventory().dropItem(itemDrop, itemDrop.getCount());
                    itemDrop.dropMe(this, getPosition().getX() + Rnd.get(50), getPosition().getY() + Rnd.get(50), getPosition().getZ() + 20);
                    itemDrop.setDropTime(0); //avoids it from beeing removed by the auto item destroyer

                    sm = new SystemMessage(298);
                    sm.addItemName(itemDrop.getItemId());
                    sendPacket(sm);

                    iu = new InventoryUpdate(); // for inventory updating
                    iu.addRemovedItem(itemDrop);
                    sendPacket(iu);

                    if (isKarmaDrop)
                        _log.warning(getName() + " has karma and dropped id = " + itemDrop.getItemId() + ", count = " + itemDrop.getCount());
                    else
                        _log.warning(getName() + " dropped id = " + itemDrop.getItemId() + ", count = " + itemDrop.getCount());

                    dropCount++;
                    if (dropCount >= dropLimit) break;
                }
            }
        }
    }
    
    private void onDieUpdateKarma()
    {
        // Karma lose for server that does not allow delevel
        if ( getKarma() > 0 )
        {
            // this formula seems to work relatively well:
            // baseKarma * thisLVL * (thisLVL/100)
            // Calculate the new Karma of the attacker : newKarma = baseKarma*pkCountMulti*lvlDiffMulti
            double karmaLost = Config.KARMA_LOST_BASE;
            karmaLost *= getLevel(); // multiply by char lvl
            karmaLost *= (getLevel() / 100); // divide by 0.charLVL
            karmaLost = Math.round(karmaLost);
            if ( karmaLost < 0 ) karmaLost = 1;

            // Decrease Karma of the L2PcInstance and Send it a Server->Client StatusUpdate packet with Karma and PvP Flag if necessary
            setKarma(getKarma() - (int)karmaLost);
        }
    }
    
    public void onKillUpdatePvPKarma(L2Character target)
    {
        if (target == null) return;
        if (!(target instanceof L2PlayableInstance)) return;

        L2PcInstance targetPlayer = null;
        if (target instanceof L2PcInstance)
            targetPlayer = (L2PcInstance)target;
        else if (target instanceof L2Summon)
            targetPlayer = ((L2Summon)target).getOwner();

        if (targetPlayer == null) return;                                          // Target player is null
        if (targetPlayer == this) return;                                          // Target player is self

	if (ArenaManager.getInstance().getArenaIndex(this.getX(),this.getY())!=-1 ||
	    ArenaManager.getInstance().getArenaIndex(target.getX(),target.getY())!=-1)
	    return;

        if (
                (
                        checkIfPvP(target)&&                                        //   Can pvp and
                        targetPlayer.getPvpFlag() != 0                              //   Target player has pvp flag set
                ) ||                                                                // or
                (
                        ZoneManager.getInstance().checkIfInZonePvP(this) &&         //   Player is inside pvp zone and
                        ZoneManager.getInstance().checkIfInZonePvP(target)          //   Target player is inside pvp zone
                ) ||
		(
		    targetPlayer.getClan()!=null && this.getClan()!=null &&
		    targetPlayer.getClan().isAtWarWith(this.getClanId())>0 &&
		    this.getClan().isAtWarWith(targetPlayer.getClanId())>0
		)
            )
        {
            // Increase the PvP Kills counter (Number of player killed during a PvP)
            setPvpKills(getPvpKills() + 1);

            // Send a Server->Client UserInfo packet to attacker with its PvP Kills Counter
            sendPacket(new UserInfo(this));
        }
        else                                                                        // Target player doesn't have pvp flag set
        {
            if (targetPlayer.getKarma() > 0)                                        // Target player has karma
            {
                if ( Config.KARMA_AWARD_PK_KILL )
                {
                    setPvpKills(getPvpKills() + 1);

                    // Send a Server->Client UserInfo packet to attacker with its PvP Kills Counter
                    sendPacket(new UserInfo(this));
                }
            }
            else                                                                    // Target player doesn't have karma
            {
                int baseKarma           = Config.KARMA_MIN_KARMA;
                int newKarma            = baseKarma;
                int karmaLimit          = Config.KARMA_MAX_KARMA;

                int pkLVL               = getLevel();
                int pkPKCount           = getPkKills();
                int targLVL             = targetPlayer.getLevel();

                int lvlDiffMulti = 0;
                int pkCountMulti = 0;

                // Check if the attacker has a PK counter greater than 0
                if ( pkPKCount > 0 )
                    pkCountMulti = pkPKCount / 2;
                else
                    pkCountMulti = 1;
                if ( pkCountMulti < 1 ) pkCountMulti = 1;

                // Calculate the level difference Multiplier between attacker and killed L2PcInstance
                if ( pkLVL > targLVL )
                    lvlDiffMulti = pkLVL / targLVL;
                else
                    lvlDiffMulti = 1;
                if ( lvlDiffMulti < 1 ) lvlDiffMulti = 1;

                // Calculate the new Karma of the attacker : newKarma = baseKarma*pkCountMulti*lvlDiffMulti
                newKarma *= pkCountMulti;
                newKarma *= lvlDiffMulti;

                // Make sure newKarma is less than karmaLimit and higher than baseKarma
                if ( newKarma < baseKarma ) newKarma = baseKarma;
                if ( newKarma > karmaLimit ) newKarma = karmaLimit;

                // Add karma to attacker and increase its PK counter
                setPkKills(getPkKills() + 1);
		if  (!(this.getClan() != null && this.getClan().isAtWarWith(targetPlayer.getClanId())>0))
            	    setKarma(getKarma() + newKarma);

                // Send a Server->Client UserInfo packet to attacker with its Karma and PK Counter
                sendPacket(new UserInfo(this));
            }
        }
    }

    public int calculateKarmaLost(int exp)
    {
        // KARMA LOSS
        // When a PKer gets killed by another player or a L2MonsterInstance, it loses a certain amount of Karma based on their level.
        // this (with defaults) results in a level 1 losing about ~2 karma per death, and a lvl 70 loses about 11760 karma per death...
        // You lose karma as long as you were not in a pvp zone and you did not kill urself.
        // NOTE: exp for death (if delevel is allowed) is based on the players level

        int karmaLost = Math.abs(exp);
        karmaLost /= Config.KARMA_XP_DIVIDER;

        if (karmaLost < Config.KARMA_LOST_BASE) karmaLost = Config.KARMA_LOST_BASE;
        if (karmaLost > getKarma()) karmaLost = getKarma();
        
        return karmaLost;
    }
    
    public void updatePvPStatus()
    {
        if (getPvpFlag() == 0) startPvPFlag();
        if (getPvpFlag() != 0) setlastPvpAttack (System.currentTimeMillis()); //update last pvp ATTACK controller
    }

    public void updatePvPStatus(L2Character target)
    {
        if (
                checkIfPvP(target) &&
                (
                        !ZoneManager.getInstance().checkIfInZonePvP(this) ||       // Player is not inside pvp zone or
                        !ZoneManager.getInstance().checkIfInZonePvP(target)        // Target player is not inside pvp zone
                )
            ) updatePvPStatus();
    }

    /**
     * Restore the specified % of experience this L2PcInstance has
     * lost and sends a Server->Client StatusUpdate packet.<BR><BR>
     */
    public void restoreExp(double restorePercent)
    { 
        if (_expBeforeDeath > 0)
        {   
            // Restore the specified % of lost experience.
        	getStat().addExp((int)Math.round((_expBeforeDeath - getExp()) * restorePercent / 100));
            _expBeforeDeath = 0;
        }
   }


	/**
	 * Reduce the Experience (and level if necessary) of the L2PcInstance in function of the calculated Death Penalty.<BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Calculate the Experience loss </li>
	 * <li>Set the value of _expBeforeDeath </li>
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
        if (getKarma() > 0) percentLost *= Config.RATE_KARMA_EXP_LOST;
        if (isFestivalParticipant() || atwar || SiegeManager.getInstance().checkIfInZone(this)) percentLost /= 4.0;
        
		// Calculate the Experience loss
		int lostExp = (int) Math.round((getStat().getExpForLevel(lvl+1) - getStat().getExpForLevel(lvl)) * percentLost /100);
		if (atEvent) lostExp = 0; //yeah we want ppl to participate

		// Get the Experience before applying penalty
		_expBeforeDeath = getExp();

        _log.fine("is dead, so exp to remove:" + lostExp);

		// Set the new Experience value of the L2PcInstance
        getStat().addExp(-lostExp);
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
	public synchronized void setTransactionRequester(L2PcInstance requestor)
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
		// Set the current HP and MP of the L2Character, Launch/Stop a HP/MP/CP Regeneration Task and send StatusUpdate packet to all other L2PcInstance to inform (exclusive broadcast)
		setCurrentHpMp(getMaxHp(),getMaxMp());
        setCurrentCp(getMaxCp());
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
    		stopWarnUserTakeBreak();
    		stopWaterTask();
    		stopRentPet();
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
                requestor.setBuyList(null);
                requestor.setSellList(null);
            }
            
            setSellList(null);
            setBuyList(null);
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

		if (getClanId() != 0 && getClan() != null)
		{
			// set the status for pledge member list to OFFLINE
			try
            {
			    L2ClanMember clanMember = getClan().getClanMember(getName());
                if (clanMember != null) clanMember.setPlayerInstance(null);
            } catch (Throwable t) {_log.log(Level.SEVERE, "deletedMe()", t); }
		}

		if (getTransactionRequester() != null)
		{
			// deals with sudden exit in the middle of transaction
			setTransactionRequester(null);
		}

		// If the L2PcInstance is a GM, remove it from the GM List
		if (isGM())
        {
			try { GmListTable.getInstance().deleteGm(this); } catch (Throwable t) {_log.log(Level.SEVERE, "deletedMe()", t); }
        }

		// Update database with items in its inventory and remove them from the world
		try { getInventory().deleteMe(); } catch (Throwable t) {_log.log(Level.SEVERE, "deletedMe()", t); }

		// Remove all L2Object from _knownObjects and _knownPlayer of the L2Character then cancel Attak or Cast and notify AI
		try { getKnownList().removeAllKnownObjects(); } catch (Throwable t) {_log.log(Level.SEVERE, "deletedMe()", t); }

		// Close the connection with the client
		try { setNetConnection(null); } catch (Throwable t) {_log.log(Level.SEVERE, "deletedMe()", t); }

		clearPathNodes();

        if (getClanId() > 0)
        	ClanTable.getInstance().getClan(getClanId()).broadcastToOnlineMembers(new PledgeShowMemberListAdd(this));
        
        for(L2PcInstance player : _SnoopedPlayer)
        {
        	player.removeSnooper(this);
        }
        
        for(L2PcInstance player : _SnoopListener)
        {
        	player.removeSnooped(this);
        }
        
		// Remove L2Object object from _allObjects of L2World
		L2World.getInstance().removeObject(this);
	}

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
	public void setSellList(FastList<TradeItem> x)
	{
		_sellList = x;
	}

	/**
	 * Return the _sellList object of the L2PcInstance.<BR><BR>
	 */
	public List<TradeItem> getSellList()
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
	public void setBuyList(FastList<TradeItem> x)
	{
		_buyList = x;
	}

	/**
	 * Return the _buyList object of the L2PcInstance.<BR><BR>
	 */
	public List<TradeItem> getBuyList()
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
		_clanLeader = (this.getObjectId() == clan.getLeaderId());
        setTitle("");

	}

	/**
	 * Return the _clan object of the L2PcInstance.<BR><BR>
	 */
	public L2Clan getClan()
	{
		return _clan;
	}

	/**
	 * Return True if the L2PcInstance is the leader of its clan.<BR><BR>
	 */
	public boolean isClanLeader()
	{
		return _clanLeader;
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
		return _isInvul || isTeleporting() || _protectEndTime > GameTimeController.getGameTicks();
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
		getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);

		// Enable all skills (set _allSkillsDisabled to False)
		enableAllSkills();

		// Send a Server->Client Packet MagicSkillCanceld to the L2PcInstance and all L2PcInstance in the _KnownPlayers of the L2Character (broadcast)
		MagicSkillCanceld msc = new MagicSkillCanceld(getObjectId());

		// Already done in the broadcast
		// sendPacket(msc);
		broadcastPacket(msc);

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
		if(_connection != null)
		{
			LoginServerThread.getInstance().sendAccessLevel(getAccountName(),level);
		}
		else
		{
			_log.info("Couldnt set the player's account access level");
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
        refreshOverloaded();
        refreshExpertisePenalty();
		// Send a Server->Client packet UserInfo to this L2PcInstance and CharInfo to all L2PcInstance in its _KnownPlayers (broadcast)
		broadcastUserInfo();

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
		su.addAttribute(StatusUpdate.KARMA, getKarma());
		su.addAttribute(StatusUpdate.PVP_FLAG, flag);
		sendPacket(su);
		broadcastPacket(su);
	}


	/**
	 * Send a Server->Client StatusUpdate packet with Karma to the L2PcInstance and all L2PcInstance to inform (broadcast).<BR><BR>
	 */
	public void updateKarma()
	{
		// Send a Server->Client StatusUpdate packet with Karma to the L2PcInstance
		StatusUpdate su = new StatusUpdate(getObjectId());
		su.addAttribute(StatusUpdate.KARMA, getKarma());
		sendPacket(su);
		broadcastPacket(su);
	}


	/**
	 * Set the online Flag to True or False and update the characters table of the database with online status and lastAccess (called when login and logout).<BR><BR>
	 */
    public void setOnlineStatus(boolean isOnline)
    {
        if (_isOnline != isOnline)
        {
            _isOnline = isOnline;            
        }
        
        // Update the characters table of the database with online status and lastAccess (called when login and logout)
        updateOnlineStatus();
    }


	/**
	 * Update the characters table of the database with online status and lastAccess of this L2PcInstance (called when login and logout).<BR><BR>
	 */
    public void updateOnlineStatus()
    {
        java.sql.Connection con = null;

        try {
            con = L2DatabaseFactory.getInstance().getConnection();
            PreparedStatement statement = con.prepareStatement("UPDATE characters SET online=?, lastAccess=? WHERE obj_id=?");
            statement.setInt(1, isOnline());
            statement.setLong(2, System.currentTimeMillis());
            statement.setInt(3, getObjectId());
            statement.execute();
            statement.close();

        // Modification for Max Player Record
        if ( isOnline () == 1 ) 
        { 
            int nbPlayerIG = 0; 
            int maxPlayer = 0; 
        
            statement = con.prepareStatement("SELECT count(*) as nb_player from characters WHERE online = '1'"); 
            ResultSet rset = statement.executeQuery(); 
            while (rset.next()) 
            { 
                nbPlayerIG = rset.getInt("nb_player"); 
            } 
        
            statement = con.prepareStatement("SELECT maxplayer from record"); 
            rset = statement.executeQuery(); 
            while (rset.next()) 
            { 
                maxPlayer = rset.getInt("maxplayer"); 
            } 
        
            if ( nbPlayerIG > maxPlayer ) 
            { 
                statement = con.prepareStatement("UPDATE record SET maxplayer=?, date=NOW()"); 
                statement.setInt(1, nbPlayerIG); 
                statement.execute(); 
                statement.close(); 
            } 
        } 
        // End Modification for Max Player record

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
                    "cancraft,title,allyId,accesslevel,online,clan_privs,wantspeace,deleteclan," +
                    "base_class) " +
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
            statement.setInt(45, getDeleteTimer());
            statement.setInt(46, hasDwarvenCraft() ? 1 : 0);
            statement.setString(47, getTitle());
            statement.setInt(48, getAllyId());
            statement.setInt(49, getAccessLevel());
            statement.setInt(50, isOnline());
            statement.setInt(51, getClanPrivileges());
            statement.setInt(52, getWantsPeace());
            statement.setLong(53, getDeleteClanTime());
            statement.setInt(54, getBaseClass());
            statement.executeUpdate();
            statement.close();
        }
        catch (Exception e)
        {
            _log.warning("Could not insert char data: " + e);
            return false;
        }
        finally
        {
            try { con.close(); } catch (Exception e) {}
        }
		return true;
	}


	/**
	 * Retrieve a L2PcInstance from the characters table of the database and add it in _allObjects of the L2world.<BR><BR>
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
            
            PreparedStatement statement = con.prepareStatement(RESTORE_CHARACTER);
            statement.setInt(1, objectId);
            ResultSet rset = statement.executeQuery();

            while (rset.next())
            {
                final int activeClassId = rset.getInt("classid");
                final boolean female = rset.getInt("sex")!=0;
                final L2PcTemplate template = CharTemplateTable.getInstance().getTemplate(activeClassId, female);
                
                player = new L2PcInstance(objectId, template, rset.getString("account_name"));
                player.setName(rset.getString("char_name"));
                player._lastAccess = rset.getLong("lastAccess");

                player._activeClass = activeClassId;
                try { player.setBaseClass(rset.getInt("base_class")); }
                catch (Exception e) { player.setBaseClass(activeClassId); }

                player._classIndex = 0;
                player.getStat().setExp(rset.getInt("exp"));
                player.getStat().setLevel(rset.getInt("level"));
                player.getStat().setSp(rset.getInt("sp"));
                
                player.setFace(rset.getInt("face"));
                player.setHairStyle(rset.getInt("hairStyle"));
                player.setHairColor(rset.getInt("hairColor"));
                player.setClanPrivileges(rset.getInt("clan_privs"));
                player.setWantsPeace(rset.getInt("wantspeace"));

            	player.setHeading(rset.getInt("heading"));
                
                player.setKarma(rset.getInt("karma"));
                player.setPvpKills(rset.getInt("pvpkills"));
                player.setPkKills(rset.getInt("pkkills"));
                player.setDeleteClanTime(rset.getLong("deleteclan"));
                player.setOnlineTime(rset.getLong("onlinetime"));
                
        		if (player.getDeleteClanTime() > 0 && player.canCreateClan())
        		    player.setDeleteClanTime(0);
                
                int clanId	= rset.getInt("clanid");
                
                if (clanId > 0)
                	player.setClan(ClanTable.getInstance().getClan(clanId));
                
                player.setDeleteTimer(rset.getInt("deletetime"));

                player.setTitle(rset.getString("title"));
                player.setAccessLevel(rset.getInt("accesslevel"));
                player.setFistsWeaponItem(player.findFistsWeaponItem(activeClassId));
                player.setUptime(System.currentTimeMillis());

            	player.setCurrentHp(rset.getDouble("curHp"));
                player.setCurrentCp(rset.getDouble("curCp"));
                player.setCurrentMp(rset.getDouble("curMp"));
                
                //Check recs
                player.checkRecom(rset.getInt("rec_have"),rset.getInt("rec_left"));

                // Restore sub classes
                if (restoreSubClassData(player)) {
                	if (player.getActiveClass() != player.getBaseClass())
                		for (int subClassIndex = 0; subClassIndex < player.getSubClasses().size(); subClassIndex++)
                		{
                			if (player.getSubClasses().get(subClassIndex).getClassId() == player.getActiveClass()) {
                				player._classIndex = subClassIndex + 1;
                				break;
                			}
                		}
                }

                // Add the L2PcInstance object in _allObjects
                //L2World.getInstance().storeObject(player);

				// Set the x,y,z position of the L2PcInstance and make it invisible
            	player.setXYZInvisible(rset.getInt("x"), rset.getInt("y"), rset.getInt("z"));

                // Retrieve the name and ID of the other characters assigned to this account.
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

            // Retrieve from the database all secondary data of this L2PcInstance 
            // and reward expertise/lucky skills if necessary.
            player.restoreCharData();
            player.rewardSkills();
             
            // Update the overloaded status of the L2PcInstance
            player.refreshOverloaded();
        }
        catch (Exception e)
        {
        	if (Config.DEBUG)
        		_log.warning("Could not restore char data: " + e);
        }
        finally
        {
            try { con.close(); } catch (Exception e) {}
        }
        return player;
    }
    
    // Sort class
    private void sortSubClassData(L2PcInstance player)
    {
        List<SubClass> _dummysubClasses = new FastList<SubClass>();
        
        if (player._subClasses == null)
            return;
        
        for(int i=0;i < player._subClasses.size();i++) {
         _dummysubClasses.add(player._subClasses.get(i));
        }
        
        for(int i=0;i < player._subClasses.size();i++) {
            SubClass subClass = new SubClass();
            subClass.setExp(player._subClasses.get(i).getExp());
            subClass.setClassId(player._subClasses.get(i).getClassId());
            subClass.setLevel(player._subClasses.get(i).getLevel());
            subClass.setSp(player._subClasses.get(i).getSp());
            subClass.setClassIndex(_subClasses.get(i).getClassIndex());
           
            if(player._subClasses.get(i).getClassIndex() == 3){
                _dummysubClasses.set(2, subClass);
                
            }
            else if (player._subClasses.get(i).getClassIndex() == 2)  {
                _dummysubClasses.set(1, subClass);
                
            }
            else if(player._subClasses.get(i).getClassIndex() == 1) {
                _dummysubClasses.set(0, subClass); 
            }
        }
        player._subClasses = _dummysubClasses;
    }

    /**
     * Restores sub-class data for the L2PcInstance, used to check the current
     * class index for the character.
     */
    private static boolean restoreSubClassData(L2PcInstance player)
    {
        java.sql.Connection con = null;
        
        try {
            con = L2DatabaseFactory.getInstance().getConnection();
            PreparedStatement statement = con.prepareStatement(RESTORE_CHAR_SUBCLASSES);
            statement.setInt(1, player.getObjectId());
            
            ResultSet rset = statement.executeQuery();
            int i=1;
			
            while (rset.next())
            {
                SubClass subClass = new SubClass();
                subClass.setClassId(rset.getInt("class_id"));
                subClass.setLevel(rset.getInt("level"));
                subClass.setExp(rset.getInt("exp"));
                subClass.setSp(rset.getInt("sp"));
                if(rset.getInt("classindex") == 0)
					subClass.setClassIndex(i);
                else
					subClass.setClassIndex(rset.getInt("classindex"));
				
                player._subClasses.add(subClass);
				i++;
            }
            
            statement.close();
			player.sortSubClassData(player);
        }
        catch (Exception e) {
if (Config.DEBUG)
            _log.warning("Could not restore char sub-classes: " + e);
        }
        finally {
            try { con.close(); } catch (Exception e) {}
        }
        
        return true;
    }

    /**
     * Restores secondary data for the L2PcInstance, based on the current class index.
     */
    private void restoreCharData()
    {
        // Retrieve from the database all skills of this L2PcInstance and add them to _skills.
        restoreSkills();

        // Retrieve from the database all macroses of this L2PcInstance and add them to _macroses.
        _macroses.restore();

        // Retrieve from the database all shortCuts of this L2PcInstance and add them to _shortCuts.
        _shortCuts.restore();

        // Retrieve from the database all henna of this L2PcInstance and add them to _henna.
        restoreHenna();
        
        // Retrieve from the database the recipe book of this L2PcInstance.
        if (!isSubClassActive())
            restoreRecipeBook();
    }

    /**
     * Store recipe book data for this L2PcInstance, if not on an active sub-class.
     */
    private void storeRecipeBook()
    {
        // If the player is on a sub-class don't even attempt to store a recipe book.
        if (isSubClassActive())
            return;
        if (getCommonRecipeBook().length == 0 && getDwarvenRecipeBook().length == 0)
        	return;
        
        java.sql.Connection con = null;
        
        try {
            con = L2DatabaseFactory.getInstance().getConnection();
            PreparedStatement statement = con.prepareStatement("DELETE FROM character_recipebook WHERE char_id=?");
            statement.setInt(1, getObjectId());
            statement.execute();
            statement.close();

            L2RecipeList[] recipes = getCommonRecipeBook();
            
            for (int count = 0; count < recipes.length; count++)
            {
                statement = con.prepareStatement("INSERT INTO character_recipebook (char_id, id, type) values(?,?,0)");
                statement.setInt(1, getObjectId());
                statement.setInt(2, recipes[count].getId());
                statement.execute();
                statement.close();
            }
            
            recipes = getDwarvenRecipeBook(); 
            for (int count = 0; count < recipes.length; count++) 
            { 
            	statement = con.prepareStatement("INSERT INTO character_recipebook (char_id, id, type) values(?,?,1)");
            	statement.setInt(1, getObjectId()); 
            	statement.setInt(2, recipes[count].getId()); 
            	statement.execute(); 
            	statement.close(); 
            } 
        }
        catch (Exception e) {
            _log.warning("Could not store recipe book data: " + e);
        } 
        finally {
            try { con.close(); } catch (Exception e) {}
        }
    }
    
    /**
     * Restore recipe book data for this L2PcInstance.
     */
    private void restoreRecipeBook()
    {
        java.sql.Connection con = null;
        
        try {
            con = L2DatabaseFactory.getInstance().getConnection();
            PreparedStatement statement = con.prepareStatement("SELECT id, type FROM character_recipebook WHERE char_id=?");
            statement.setInt(1, getObjectId());
            ResultSet rset = statement.executeQuery();
            
            L2RecipeList recipe;
            while (rset.next()) {
            	recipe = RecipeController.getInstance().getRecipeList(rset.getInt("id") - 1);
            	
            	if (rset.getInt("type") == 1) 
            		registerDwarvenRecipeList(recipe); 
            	else 
            		registerCommonRecipeList(recipe); 
            }
            
            rset.close();
            statement.close();
        }
        catch (Exception e) {
            _log.warning("Could not restore recipe book data:" + e);
        } 
        finally {
            try { con.close(); } catch (Exception e) {}
        }
    }
    
    /**
     * Update L2PcInstance stats in the characters table of the database.<BR><BR>
     */
    public void store()
    {
        //update client coords, if these look like true
        if (getDistance(getClientX(), getClientY()) < 1000) setXYZ(getClientX(), getClientY(), getClientZ());

        storeCharBase();
        storeCharSub();
        storeEffect();
        storeRecipeBook();
    }

    private void storeCharBase()
    {
        java.sql.Connection con = null;
        
        try
        {
        	// Get the exp, level, and sp of base class to store in base table
        	int currentClassIndex = getClassIndex();
        	_classIndex = 0;
        	int exp     = getStat().getExp();
        	int level   = getStat().getLevel();
        	int sp      = getStat().getSp();
        	_classIndex = currentClassIndex;

            con = L2DatabaseFactory.getInstance().getConnection();
            PreparedStatement statement;

            // Update base class
            statement = con.prepareStatement(
                    "UPDATE characters SET " +
                    "level=?,maxHp=?,curHp=?,maxCp=?,curCp=?,maxMp=?,curMp=?," +
                    "str=?,con=?,dex=?,_int=?,men=?,wit=?,face=?,hairStyle=?,hairColor=?," +
                    "heading=?,x=?,y=?,z=?,exp=?,sp=?,karma=?,pvpkills=?,pkkills=?," +
                    "rec_have=?,rec_left=?,clanid=?,maxload=?,race=?,classid=?,deletetime=?," +
                    "title=?,allyId=?,accesslevel=?,online=?,clan_privs=?,wantspeace=?,deleteclan=?," +
                    "base_class=?,onlinetime=? " +
                    "WHERE obj_id=?");
            statement.setInt(1, level);
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
            statement.setInt(21, exp);
            statement.setInt(22, sp);
            statement.setInt(23, getKarma());
            statement.setInt(24, getPvpKills());
            statement.setInt(25, getPkKills());
            statement.setInt(26, getRecomHave());
            statement.setInt(27, getRecomLeft());
            statement.setInt(28, getClanId());
            statement.setInt(29, getMaxLoad());
            
            if (!isSubClassActive())
                statement.setInt(30, getRace().ordinal());
            else 
                statement.setInt(30, getBaseTemplate().race.ordinal());
                
            statement.setInt(31, getClassId().getId());
            statement.setInt(32, getDeleteTimer());
            statement.setString(33, getTitle());
            statement.setInt(34, getAllyId());
            statement.setInt(35, getAccessLevel());
            statement.setInt(36, isOnline());
            statement.setInt(37, getClanPrivileges());
            statement.setInt(38, getWantsPeace());
            statement.setLong(39, getDeleteClanTime());
            statement.setInt(40, getBaseClass());
	    long totalOnlineTime = _onlineTime;
	    if (_onlineBeginTime > 0)
		totalOnlineTime += (System.currentTimeMillis()-_onlineBeginTime)/1000;
            statement.setLong(41, totalOnlineTime);
            statement.setInt(42, getObjectId());
           
            statement.execute();
            statement.close();
        }
        catch (Exception e) { _log.warning("Could not store char base data: "+ e); }
        finally { try { con.close(); } catch (Exception e) {} }
    }

    private void storeCharSub()
    {
        java.sql.Connection con = null;
        
        try
        {
            con = L2DatabaseFactory.getInstance().getConnection();
            PreparedStatement statement;

            if (getTotalSubClasses() > 0) 
                for (SubClass subClass : getSubClasses()) 
                {
                    statement = con.prepareStatement(UPDATE_CHAR_SUBCLASS);
                    statement.setInt(1, subClass.getExp());
                    statement.setInt(2, subClass.getSp());
                    statement.setInt(3, subClass.getLevel());
                    statement.setInt(4, subClass.getClassIndex());
					statement.setInt(5, getObjectId());
					statement.setInt(6, subClass.getClassId());
                    statement.execute();
                    statement.close();
                }
        }
        catch (Exception e) { _log.warning("Could not store char sub data: "+ e); }
        finally { try { con.close(); } catch (Exception e) {} }
    }

    private void storeEffect()
    {
    	if (!Config.STORE_SKILL_COOLTIME) return;

    	java.sql.Connection con = null;
        
        try
        {
            con = L2DatabaseFactory.getInstance().getConnection();
            PreparedStatement statement;

            // Delete all current stored effects for char to avoid dupe
            statement = con.prepareStatement(DELETE_SKILL_SAVE);
            statement.setInt(1, getObjectId());
            statement.setInt(2, getClassIndex());
            statement.execute();
            statement.close();

            // Loop thru all effects for char and same them each to database
            for (L2Effect effect : getAllEffects())
            {
                if (effect != null && effect.getInUse() && !effect.getSkill().isToggle())
                {
                    statement = con.prepareStatement(ADD_SKILL_SAVE);
                    statement.setInt(1, getObjectId());
                    statement.setInt(2, effect.getSkill().getId());
                    statement.setInt(3, effect.getSkill().getLevel());
                    statement.setInt(4, effect.getCount());
                    statement.setInt(5, effect.getTime());
                    statement.setInt(6, getClassIndex());
                    statement.execute();
                    statement.close();
                }
            }
        }
        catch (Exception e) { _log.warning("Could not store char effect data: "+ e); }
        finally { try { con.close(); } catch (Exception e) {} }
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
        storeSkill(newSkill, oldSkill, -1);

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
                statement.setInt(3, getClassIndex());
                statement.execute();
                statement.close();
            }
        }
        catch (Exception e)
        {
			_log.warning("Error could not delete skill: " + e);
        }
        finally
        {
            try { con.close(); } catch (Exception e) {}
        }

        return oldSkill;
    }


    /**
     * Add or update a L2PcInstance skill in the character_skills table of the database.
     * <BR><BR>
     * If newClassIndex > -1, the skill will be stored with that class index, not the current one.
     */
    private void storeSkill(L2Skill newSkill, L2Skill oldSkill, int newClassIndex)
    {
        int classIndex = _classIndex;
        
        if (newClassIndex > -1)
            classIndex = newClassIndex;
        
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
                statement.setInt(4, classIndex);
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
                statement.setInt(5, classIndex);
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
            _log.warning("Error could not store char skills: " + e);
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
            con = L2DatabaseFactory.getInstance().getConnection();
            PreparedStatement statement = con.prepareStatement(RESTORE_SKILLS_FOR_CHAR);
            statement.setInt(1, getObjectId());
            statement.setInt(2, getClassIndex());
            ResultSet rset = statement.executeQuery();

			// Go though the recordset of this SQL query
            while (rset.next())
            {
                int id = rset.getInt("skill_id");
                int level = rset.getInt("skill_level");

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
            _log.warning("Could not restore character skills: " + e);
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
        
        try {
            con = L2DatabaseFactory.getInstance().getConnection();
            PreparedStatement statement;
            
            statement = con.prepareStatement(RESTORE_SKILL_SAVE);
            statement.setInt(1, getObjectId());
            statement.setInt(2, getClassIndex());
            
            ResultSet rset = statement.executeQuery();
            
            while (rset.next())
            {
                int skillId = rset.getInt("skill_id");
                int skillLvl = rset.getInt("skill_level");
                int effectCount = rset.getInt("effect_count");
                int effectCurTime = rset.getInt("effect_cur_time");
                
                L2Skill skill = SkillTable.getInstance().getInfo(skillId, skillLvl);
                callSkill(skill, targets);
                
                for (L2Effect effect : getAllEffects())
                    if (effect.getSkill().getId() == skillId)
                    {
                        effect.setCount(effectCount);
                        effect.setFirstTime(effectCurTime);
                    }
            }
            
            statement = con.prepareStatement(DELETE_SKILL_SAVE);
            statement.setInt(1, getObjectId());
            statement.setInt(2, getClassIndex());
            statement.executeUpdate();
        } 
        catch (Exception e) {
            _log.warning("Could not restore active skills data: " + e);
        } 
        finally {
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
            PreparedStatement statement = con.prepareStatement(RESTORE_CHAR_HENNAS);
            statement.setInt(1, getObjectId());
            statement.setInt(2, getClassIndex());
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
            PreparedStatement statement = con.prepareStatement(DELETE_CHAR_HENNA);
            statement.setInt(1, getObjectId());
            statement.setInt(2, slot+1);
            statement.setInt(3, getClassIndex());
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
		HennaInfo hi = new HennaInfo(this);
		this.sendPacket(hi);

		// Send Server->Client UserInfo packet to this L2PcInstance
		UserInfo ui = new UserInfo(this);
		this.sendPacket(ui);

        // Add the recovered dyes to the player's inventory and notify them.
        getInventory().addItem(hennaDyes);
        SystemMessage sm = new SystemMessage(SystemMessage.EARNED_ITEM);
        sm.addItemName(dyeID);
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
    	            PreparedStatement statement = con.prepareStatement(ADD_CHAR_HENNA);
    	            statement.setInt(1, getObjectId());
    	            statement.setInt(2, henna.getSymbolId());
    	            statement.setInt(3, i+1);
                    statement.setInt(4, getClassIndex());
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

		// Check if the attacker is not in the same party
		if (getParty() != null && getParty().getPartyMembers().contains(attacker))
		    return false;

		// Check if the L2PcInstance has Karma
		if (getKarma() > 0)
		    return true;

		// Check if the attacker is a L2PcInstance
		if (attacker instanceof L2PcInstance)
		{
            // Check if the L2PcInstance is in an arena or a siege area
            if (ZoneManager.getInstance().checkIfInZonePvP(this) && ZoneManager.getInstance().checkIfInZonePvP(attacker))
                return true;

            if (this.getClan() != null)
            {
                Siege siege = SiegeManager.getInstance().getSiege(getX(), getY());
                if (siege != null)
                {
                    // Check if a siege is in progress and if attacker and the L2PcInstance aren't in the Defender clan
                    if (siege.checkIsDefender(((L2PcInstance)attacker).getClan()) &&
                            siege.checkIsDefender(this.getClan()))
                        return false;

                    // Check if a siege is in progress and if attacker and the L2PcInstance aren't in the Attacker clan
                    if (siege.checkIsAttacker(((L2PcInstance)attacker).getClan()) &&
                            siege.checkIsAttacker(this.getClan()))
                        return false;
                }

                // Check if clan is at war
                if (this.getClan() != null && ((L2PcInstance)attacker).getClan() != null && (this.getClan().isAtWarWith(((L2PcInstance)attacker).getClanId())>0 && this.getWantsPeace() == 0 && ((L2PcInstance)attacker).getWantsPeace() == 0))
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

        L2Object target = null;
        switch(skill.getTargetType())
        {
            case TARGET_AURA:
            case TARGET_PARTY:      // AURA, PARTY, CLAN, SELF skills should work regardless of current target
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
        	sendPacket(new SystemMessage(SystemMessage.CANT_SEE_TARGET));
            sendPacket(new ActionFailed());
        	return;
        }
        
	if (skill.isNeedWeapon())
	{
		L2ItemInstance weaponInst = this.getActiveWeaponInstance();         
		if (this instanceof L2PcInstance && weaponInst == null)
		{ 
			SystemMessage sm2 = new SystemMessage(614);  
			sm2.addString("You must equip one weapon before cast spell.");
			sendPacket(sm2); 
	        sendPacket(new ActionFailed());
			return; 
		}

	}

        // Check if this is offensive magic
        if (skill.getTargetType() == SkillTargetType.TARGET_HOLY &&
                !TakeCastle.checkIfOkToCastSealOfRule(this, false))
        {
            sendPacket(new ActionFailed());
            abortCast();
            return;
        }
        else if (skill.getSkillType() == SkillType.SIEGEFLAG &&
                !SiegeFlag.checkIfOkToPlaceFlag(this, false))
        {
            sendPacket(new ActionFailed());
            abortCast();
            return;
        }
        else if (skill.isOffensive() && getAccessLevel() < Config.GM_PEACEATTACK)
        {
            if (isInsidePeaceZone(this, target))

            {
                // If L2Character or target is in a peace zone, send a system message TARGET_IN_PEACEZONE a Server->Client packet ActionFailed
                sendPacket(new SystemMessage(SystemMessage.TARGET_IN_PEACEZONE));
                sendPacket(new ActionFailed());
                return;
            }
            else if (target != null && !target.isAttackable())
            {
                // If target is not attackable, send a Server->Client packet ActionFailed
                sendPacket(new ActionFailed());
                return;
            }

            // Forced ATTACK is needed on non-attackable targets
            else if (target != null && !target.isAutoAttackable(this) && !forceUse &&
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
            else if (target != null && dontMove)
			{
				// Calculate the distance between the L2PcInstance and the target
				double distance = getDistance(target.getX(), target.getY());

			    if (skill.getCastRange() > 0 && distance > skill.getCastRange())
			    {
					// Send a System Message to the caster
	                sendPacket(new SystemMessage(SystemMessage.TARGET_TOO_FAR));

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
	            sendPacket(new SystemMessage(SystemMessage.ALREDAY_SPOILED));

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
                sendPacket(new SystemMessage(SystemMessage.NOT_ENOUGH_ITEMS));
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
            sendPacket(new SystemMessage(SystemMessage.CANT_MOVE_SITTING));

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
            SystemMessage sm = new SystemMessage(SystemMessage.SKILL_NOT_AVAILABLE);
            sm.addString(skill.getName());
            sendPacket(sm);
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
                target != null &&                                           // target not null and
                target != this &&                                           // target is not self and
                target instanceof L2PcInstance &&                           // target is L2PcInstance and
                skill.isPvpSkill() &&                                       // pvp skill and
                !ZoneManager.getInstance().checkIfInZonePvP(this) &&        // Pc is not in PvP zone
                !ZoneManager.getInstance().checkIfInZonePvP(target)         // target is not in PvP zone
            )
        {
            if (
                    ((L2PcInstance)target).getPvpFlag() == 0 &&             //   target's pvp flag is not set and
                    ((L2PcInstance)target).getKarma() == 0                  //   target has no karma
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
    public boolean checkLandingState() 
    { 
        // Check if char is in a no landing zone 
    	if (ZoneManager.getInstance().checkIfInZoneNoLanding(this))
    		return true;
    	else 
    		// if this is a castle that is currently being sieged, and the rider is NOT a castle owner
    		// he cannot land.
    		// castle owner is the leader of the clan that owns the castle where the pc is
    		if (SiegeManager.getInstance().checkIfInZone(this) &&  
    				!(getClan()!=null &&
    					CastleManager.getInstance().getCastle(this) == CastleManager.getInstance().getCastleByOwner(getClan()) &&
    					this == getClan().getLeader().getPlayerInstance()))
    			return true;
    	
    	return false;
    } 
    public void setMountType(int mountType)
    {
       if (checkLandingState() && mountType !=0) 
          return; 
    	switch(mountType)
    	{
    	case 0: setIsFlying(false);
    			setIsRiding(false);
    			break; //Dismounted
    	case 1: setIsRiding(true);
    			break;
    	case 2: setIsFlying(true);
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

    public Map<Integer, L2CubicInstance> getCubics()
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
    	return "player "+getName();
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

    public void addAutoSoulShot(int itemId)
	{
		_activeSoulShots.add(Integer.valueOf(itemId));
        rechargeAutoSoulShot();
	}

	public void removeAutoSoulShot(int itemId)
	{
		_activeSoulShots.remove(Integer.valueOf(itemId));
	}
	
	public Set<Integer> getAutoSoulShot()
	{
		return _activeSoulShots;
	}
    
    public void rechargeAutoSoulShot()
    {
        L2ItemInstance item;
        IItemHandler handler;
        
        if (_activeSoulShots.size() == 0 || _activeSoulShots == null)
            return;
        
        for (int itemId : _activeSoulShots)
        {
            item = getInventory().findItemByItemId(itemId);
            
            if (item != null)
            {
                handler = ItemHandler.getInstance().getItemHandler(itemId);
                handler.useItem(this, item);
            }
            else
            {
                removeAutoSoulShot(itemId);
            }
        }
    }
    
    private ScheduledFuture _taskWarnUserTakeBreak;
    
    class WarnUserTakeBreak implements Runnable
    {
        public void run()
        {
            if (L2PcInstance.this.isOnline() == 1)
            {
                SystemMessage msg = new SystemMessage(764);
                L2PcInstance.this.sendPacket(msg);
            }
            else
                stopWarnUserTakeBreak();
        }
    }
    private ScheduledFuture _taskRentPet;
    
    class RentPetTask implements Runnable
    {
        public void run()
        {
            stopRentPet();
        }
    }

    private ScheduledFuture _taskWater;
	private L2BoatInstance _Boat;
	private Point3D InBoatPosition;
    
    class WaterTask implements Runnable
    {
        public void run()
        {
	    double reduceHp = getMaxHp()/100;
	    if (reduceHp < 1)
		reduceHp = 1;
	    reduceCurrentHp(reduceHp,L2PcInstance.this,false);
	    //reduced hp, becouse not rest
	    SystemMessage sm = new SystemMessage(297);
	    sm.addNumber((int)reduceHp);
	    sendPacket(sm);

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

	public Map<WayPointNode, List<WayPointNode>> getPathNodeMap()
	{
		if (pathNodeMap == null)
			pathNodeMap	= new FastMap<WayPointNode, List<WayPointNode>>();
		return pathNodeMap;
	}

	public void addPathNodePoint()
	{
		//add Node to Players PathNode List
		//TODO: Addcheck for nearby point in radius
		setSelectedNode(WayPointNode.spawn(this));
		getPathNodeMap().put(getSeltectedNode(), new FastList<WayPointNode>());

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
            List<WayPointNode> linkedNodes	= getPathNodeMap().get(selectedNode);
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
            List<WayPointNode> linkedNodes   = getPathNodeMap().get(selectedNode);
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
            List<WayPointNode> list	= pathNodeMap.get(selectedNode);
			if (list == null)
				list = new FastList<WayPointNode>();
			list.add(target);
			pathNodeMap.put(selectedNode, list);


            List<WayPointNode> list2	= pathNodeMap.get(target);
			if (list2 == null)
				list2 = new FastList<WayPointNode>();
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
            List<WayPointNode> list	= pathNodeMap.get(selectedNode);
			if (list != null && list.contains(target))
			{
				list.remove(target);
				pathNodeMap.put(selectedNode, list);

                List<WayPointNode> list2	= pathNodeMap.get(target);
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
            List<WayPointNode> list  = pathNodeMap.get(node);
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

    public Map<Point3D, List<Point3D>> getSaveList()
    {
        Map<Point3D, List<Point3D>> saveList   = null;
        if (pathNodeMap != null)
        {
            saveList    = Collections.synchronizedMap(new WeakHashMap<Point3D, List<Point3D>>());
            for (WayPointNode node : pathNodeMap.keySet())
            {
                Point3D nodePoint   = Point3D.getPosition(node);

                List<WayPointNode> links = pathNodeMap.get(node);
                List<Point3D> linkPoints   = null;
                if (links != null)
                {
                    linkPoints  = new FastList<Point3D>();
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
     * @param string
     */
    @SuppressWarnings(value = { "unchecked" })
    public void loadPathNodes(String fileName)
    {
        Map<Point3D, List<Point3D>> newNodeMap = null;
        try
        {
            //Create necessary input streams
            //File file   = new File(Config.DATAPACK_ROOT, "pathnode/" + fileName + ".bin");
            FileInputStream fis = new FileInputStream("pathnode/" + fileName + ".bin"); // Read from file
            GZIPInputStream gzis = new GZIPInputStream(fis); // Uncompress
            ObjectInputStream in = new ObjectInputStream(gzis); // Read objects
            // Read in an object. It should be a vector of scribbles

            newNodeMap = (FastMap<Point3D, List<Point3D>>)in.readObject();
            in.close(); // Close the stream.
        }
        catch (Exception e)
        {
            sendPacket(SystemMessage.sendString("Could not Load Path Node Table from: L2J/pathnode/"+fileName+".bin"));
            e.printStackTrace();
            return;
        }
        clearPathNodes();
        loadNodeList(newNodeMap);
        sendPacket(SystemMessage.sendString("Path Node table loaded from: L2J/pathnode/"+fileName+".bin"));
    }

    /**
     * @param newNodeMap
     */
    private void loadNodeList(Map<Point3D, List<Point3D>> newNodeMap)
    {
        pathNodeMap = Collections.synchronizedMap(new WeakHashMap<WayPointNode, List<WayPointNode>>());
        Map<Point3D, WayPointNode> map    = new FastMap<Point3D, WayPointNode>();

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

            List<Point3D> links = newNodeMap.get(point);
            List<WayPointNode> nodeLinks = null;
            if (links != null)
            {
                nodeLinks  = new FastList<WayPointNode>();
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

    public void sendMessage(String message)
    {
        sendPacket(SystemMessage.sendString(message));
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
        setIsParalyzed(true);
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
		setIsParalyzed(false);
        setVisible();
        if (getAI() != null)
		{
            getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
        }
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
    
    public BlockList getBlockList()
    {
        return _blockList;
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
    
    /**
     * Add the specified class ID as a sub-class (up to the maximum number of <b>three</b>)
     * for this character.
     * <BR>
     * If classIndex > -1, then reset the sub-class at that index to the new class ID.
     * 
     * @param int classId
     * @param int classIndex
     * @return boolean subclassAdded
     */
    public boolean addSubClass(int classId, int subIndex)
    {
        int classIndex;
        ClassId newId = ClassId.values()[classId];
        
        SubClass newClass = new SubClass();
        newClass.setClassId(classId);
        
        if (subIndex > -1) {
            classIndex = subIndex + 1;
			newClass.setClassIndex(subIndex+1);
            _subClasses.set(subIndex, newClass);
        }
        else {
            if (getTotalSubClasses() == 3)
                return false;
            for(int i=0;i < getTotalSubClasses();i++) 
			{
				if (_subClasses.get(i).getClassId() == classId) {
					return false;
                }
			}
            classIndex = getTotalSubClasses() + 1;
            
            newClass.setClassIndex(classIndex);
            _subClasses.add(newClass);
        }
        
        java.sql.Connection con = null;

        try {
            // Store the basic info about this new sub-class.
            con = L2DatabaseFactory.getInstance().getConnection();
            PreparedStatement statement = con.prepareStatement(ADD_CHAR_SUBCLASS);
            statement.setInt(1, getObjectId());
            statement.setInt(2, newClass.getClassId());
            statement.setInt(3, newClass.getExp());
            statement.setInt(4, newClass.getSp());
            statement.setInt(5, newClass.getLevel());
			statement.setInt(6, newClass.getClassIndex());
            statement.execute();
            statement.close();
        }
        catch (Exception e) {
            _log.warning("Could not add character sub-class: " + e);
            return false;
        }
        finally {
            try { con.close(); } catch (Exception e) {}
        }
        
        // Add all the necessary skills up to level 40 for this new class.
        L2SkillLearn[] availSkills = SkillTreeTable.getInstance().getAvailableSkills(this, newId);
        int numSkillsAdded = 0;
        
        for (L2SkillLearn availSkill : availSkills)
            if (availSkill.getMinLevel() < 43) {
                L2Skill skill = SkillTable.getInstance().getInfo(availSkill.getId(), availSkill.getLevel());
                storeSkill(skill, null, classIndex);
                
                numSkillsAdded++;
            }
        
        if (Config.DEBUG)
            _log.info(numSkillsAdded + " skills added for " + getName() + "'s sub-class.");
        
        // Update database with new class information.
        store();
        
        return true; 
    }
    
    /**
     * Remove old info for the specified sub-class index from this character,
     * and update it with that for the specified new class ID. 
     * 
     * @param int subIndex
     * @param int newClassId
     * @return boolean subclassAdded
     */
    public boolean modifySubClass(int subIndex, int newClassId)
    {
        SubClass originalClass = _subClasses.get(subIndex);
        java.sql.Connection con = null;
        
        try {
            con = L2DatabaseFactory.getInstance().getConnection();
            PreparedStatement statement;
            
            // Remove all basic info stored about this sub-class.
            statement = con.prepareStatement(DELETE_CHAR_SUBCLASS);
            statement.setInt(1, getObjectId());
            statement.setInt(2, originalClass.getClassId());
            statement.execute();
            statement.close();
            
            int classIndex = subIndex + 1;
            
            // Remove all skill info stored for this sub-class.
            statement = con.prepareStatement(DELETE_CHAR_SKILLS);
            statement.setInt(1, getObjectId());
            statement.setInt(2, classIndex);
            statement.execute();
            statement.close();
            
            // Remove all henna info stored for this sub-class.
            statement = con.prepareStatement(DELETE_CHAR_HENNAS);
            statement.setInt(1, getObjectId());
            statement.setInt(2, classIndex);
            statement.execute();
            statement.close();
            
            // Remove all shortcuts info stored for this sub-class.
            statement = con.prepareStatement(DELETE_CHAR_SHORTCUTS);
            statement.setInt(1, getObjectId());
            statement.setInt(2, classIndex);
            statement.execute();
            statement.close();
        }
        catch (Exception e) {
            _log.warning("Could not delete char sub-class: " + e);
            return false;
        }
        finally {
            try { con.close(); } catch (Exception e) {}
        }    
        
        return addSubClass(newClassId, subIndex);
    }
    
    public boolean isSubClassActive()
    {
        return _classIndex > 0;
    }
    
    public List<SubClass> getSubClasses()
    {
        return _subClasses;
    }
    
    public int getTotalSubClasses()
    {
        return _subClasses.size();
    }
    
    public int getBaseClass()
    {
        return _baseClass;
    }

    public int getActiveClass()
    {
        return _activeClass;
    }
    
    public int getClassIndex()
    {
		if(getActiveClass() == getBaseClass())
			return 0;

		for (int subClassIndex = 0; subClassIndex < getSubClasses().size(); subClassIndex++)
        {
			if (getSubClasses().get(subClassIndex).getClassId() == getActiveClass()) 
			{
				_classIndex = getSubClasses().get(subClassIndex).getClassIndex();
                break;
			}
        }
		return _classIndex;
    }
    
    /**
     * Changes the character's class based on the given class index.
     * <BR><BR>
     * An index of zero specifies the character's original (base) class,
     * while indexes 1-3 specifies the character's sub-classes respectively.
     *  
     * @param index
     */
    public boolean setActiveClass(int classIndex)
    {
        _classIndex = classIndex;
       
        if (classIndex == 0) {
            setClassId(_baseClass);
        }
        else {
            try
            {
                setClassId(_subClasses.get(classIndex-1).getClassId());
            }
            catch (Exception e) { return false; }
        }
        
        /* Update the character's change in class status.
         * 
         * 1. Renovate the characters table in the database with the new class info, storing also buff/effect data.
         * 2. Remove all existing skills.
         * 3. Restore all the learned skills for the current class from the database.
         * 4. Restore effect/buff data for the new class.
         * 5. Restore henna data for the class, applying the new stat modifiers while removing existing ones.
         * 6. Reset HP/MP/CP stats and send Server->Client character status packet to reflect changes.
         * 7. Restore shortcut data related to this class.
         * 8. Resend the "level-up" effect to broadcast to all nearby players.
         * 9. Respawn the character.
         */
        store();
        
        for (L2Skill oldSkill : getAllSkills())
            super.removeSkill(oldSkill);
        
        for (L2Effect effect : getAllEffects())
            effect.exit();
        
        if (isSubClassActive())
        {
        	_DwarvenRecipeBook.clear();
        	_CommonRecipeBook.clear();
        }
        else
            restoreRecipeBook();
        
        restoreSkills();
        rewardSkills();
        restoreEffects();

        for (int i = 0; i < 3; i++)
            _henna[i] = null;
        
        restoreHenna();
        sendPacket(new HennaInfo(this));
        
        setCurrentHpMp(getMaxHp(), getMaxMp());
        setCurrentCp(getMaxCp());
        updateStats();

        //_macroses.restore();
        //_macroses.sendUpdate();
        _shortCuts.restore();
        sendPacket(new ShortCutInit());
        
        broadcastPacket(new SocialAction(getObjectId(), 15));
        
        //decayMe();
        //spawnMe(getX(), getY(), getZ());

        return true;
    }
    
    public void stopWarnUserTakeBreak()
    {
        if (_taskWarnUserTakeBreak != null)
        {
            _taskWarnUserTakeBreak.cancel(false);
            _taskWarnUserTakeBreak = null;
        }
    }
    
    public void startWarnUserTakeBreak()
    {
        if (_taskWarnUserTakeBreak == null)
            _taskWarnUserTakeBreak = ClientScheduler.getInstance().scheduleLowAtFixedRate(new WarnUserTakeBreak(), 7200000, 7200000);
    }
    public void stopRentPet()
    {
        if (_taskRentPet != null)
        {
            // if the rent of a wyvern expires while over a flying zone, tp to down before unmounting
            if (checkLandingState() && getMountType()==2)
            	teleToLocation(MapRegionTable.getInstance().getTeleToLocation(this, MapRegionTable.TeleportWhereType.Town));
            _taskRentPet.cancel(false);
	    Ride dismount = new Ride(getObjectId(), Ride.ACTION_DISMOUNT, 0);
	    sendPacket(dismount);
	    broadcastPacket(dismount);
            _taskRentPet = null;
	    setMountType(0);
        }
    }
    public void startRentPet(int seconds)
    {
        if (_taskRentPet == null)
            _taskRentPet = ClientScheduler.getInstance().scheduleLowAtFixedRate(new RentPetTask(), seconds * 1000, seconds * 1000);
    }
    public boolean isRentedPet()
    {
        if (_taskRentPet != null)
            return true;
        return false;
    }
    public void stopWaterTask()
    {
        if (_taskWater != null)
        {
            _taskWater.cancel(false);
            _taskWater = null;
	    sendPacket(new SetupGauge(2, 0));
        }
    }
    public void startWaterTask()
    {
        if (!isDead() && _taskWater == null)
	{
	    int timeinwater = 86;
	    sendPacket(new SetupGauge(2, timeinwater * 1000));
            _taskWater = ClientScheduler.getInstance().scheduleLowAtFixedRate(new WaterTask(), timeinwater * 1000, 1000);
	}    
    }
    public boolean isInWater()
    {
        if (_taskWater != null)
            return true;
        return false;
    }
    public void checkWaterState()
    {
        //checking if char is  over base level of  water (sea, rivers)
        if (getZ() > -3793)
        {
            stopWaterTask();
            return;
        }

        // Check if char is in water or is underground and in water
        int[] coord;
        Zone zone;

        zone = ZoneManager.getInstance().getZone(ZoneType.getZoneTypeName(ZoneType.ZoneTypeEnum.Water), getX(), getY()); //checking if char is in water zone
        if (zone == null) zone = ZoneManager.getInstance().getZoneType(ZoneType.getZoneTypeName(ZoneType.ZoneTypeEnum.Underground)).getZone(getX(), getY()); //checking if char is in underground and in water

        if (zone != null)
        {
            coord = zone.getCoord(getX(), getY());
            if (coord != null && getZ() > coord[4])
            {
                stopWaterTask();
                return;
            }    
        }

        startWaterTask();
    }

    public void onPlayerEnter()
    {
        startWarnUserTakeBreak();
    }
    
    public boolean isProcessingTransaction()
    {
        return _isProcessingTransaction;
    }
    
    public synchronized void setIsProcessingTransaction(boolean value)
    {
        _isProcessingTransaction = value;
    }
    
    public long getLastAccess()
    {
        return _lastAccess;
    }
    
    private void checkRecom(int recsHave, int recsLeft)
    {
        Calendar check = Calendar.getInstance();
        check.setTimeInMillis(_lastAccess);
        check.add(Calendar.DAY_OF_MONTH,1);
        
        Calendar min = Calendar.getInstance();
        min.set(Calendar.HOUR_OF_DAY, 13);
        min.set(Calendar.MINUTE, 0);
        
        Calendar max = Calendar.getInstance();
        max.add(Calendar.DAY_OF_MONTH,1);
        max.set(Calendar.HOUR_OF_DAY, 13);
        max.set(Calendar.MINUTE, 0);

        if (getStat().getLevel() < 10 || !(check.after(min) && check.before(max)) )
        {
            _recomHave = recsHave;
            _recomLeft = recsLeft;
            return;
        }
        
        restartRecom();
    }
    
    public void restartRecom()
    {
        _recomChars.clear();
        
        if (getStat().getLevel() < 20) {
            _recomLeft = 3;
            _recomHave--;
        }
        else if (getStat().getLevel() < 40) {
            _recomLeft = 3;
            _recomHave -= 2;
        }
        else
        {
            _recomLeft = 6;
            _recomHave -= 3;
        }
        
        if (_recomHave < 0) 
            _recomHave = 0;
    }
	public final void illegalAction(String msg, Integer jail_items)
	{
	 	Log.IllegalPlayerAction(this, msg, jail_items);
	}

	public final String toFullString()
	{
		return "player '"+getName()+"' [oid="+getObjectId()+", account='"+getAccountName()+", ip="+getNetConnection().getSocket().getInetAddress().getHostAddress()+"']";
	}
    public int getBoatId()
    {
        return _boatId;
    }
    
    public void setBoatId(int boatId)
    {
        _boatId = boatId;
    }

    public void doRevive()
    {
        super.doRevive();
        updateEffectIcons();
    }
    
    public void doRevive(L2Skill skill)
    {
        // Restore the player's lost experience, 
        // depending on the % return of the skill used (based on its power).
        restoreExp(skill.getPower());
        doRevive();
    }
    
    public void onActionRequest()
    {
        setProtection(false);
    }

	/**
	 * @param expertiseIndex The expertiseIndex to set.
	 */
	public void setExpertiseIndex(int expertiseIndex)
	{
		this.expertiseIndex = expertiseIndex;
	}

	/**
	 * @return Returns the expertiseIndex.
	 */
	public int getExpertiseIndex()
	{
		return expertiseIndex;
	}
    
	public final void onTeleported()
	{
		super.onTeleported();

		if (Config.PLAYER_SPAWN_PROTECTION > 0) setProtection(true);
		if (Config.ALLOW_WATER && this instanceof L2PcInstance) ((L2PcInstance) this).checkWaterState();
	}

    public final boolean updatePosition(int gameTicks)
    {
        // Disables custom movement for L2PCInstance when Old Synchronization is selected
        if (Config.COORD_SYNCHRONIZE == -1) 
            return super.updatePosition(gameTicks);
            
        // Get movement data
        MoveData m = _move;

        if (_move == null)
            return true;

        if (!isVisible())
        {
            _move = null;
            return true;
        }

        // Check if the position has alreday be calculated
        if (m._moveTimestamp == 0)
            m._moveTimestamp = m._moveStartTime;

            // Check if the position has alreday be calculated
        if (m._moveTimestamp == gameTicks)
            return false;

        int dx = m._xDestination - getX();
        int dy = m._yDestination - getY();
        int dz = m._zDestination - getZ();
        int distPassed = (int)getStat().getMoveSpeed() * (gameTicks - m._moveTimestamp) / GameTimeController.TICKS_PER_SECOND;
        double distFraction = (distPassed) / Math.sqrt(dx*dx + dy*dy + dz*dz); 
//        if (Config.DEVELOPER) System.out.println("Move Ticks:" + (gameTicks - m._moveTimestamp) + ", distPassed:" + distPassed + ", distFraction:" + distFraction);

        if (distFraction > 1)
        {
            // Set the position of the L2Character to the destination
            super.setXYZ(m._xDestination, m._yDestination, m._zDestination);

            // Cancel the move action
            _move = null;
        }
        else
        {
            // Set the position of the L2Character to estimated after parcial move
            super.setXYZ(getX() + (int)(dx * distFraction + 0.5), getY() + (int)(dy * distFraction + 0.5), getZ() + (int)(dz * distFraction));
        }

        // Set the timer of last position update to now
        m._moveTimestamp = gameTicks;
        return (_move == null);
    }

    public void setLastClientPosition(int x, int y, int z)
    {
        _lastClientPosition.x = x;
        _lastClientPosition.y = y;
        _lastClientPosition.z = z;
    }
    
    public boolean checkLastClientPosition(int x, int y, int z)
    {
        return _lastClientPosition.x == x && _lastClientPosition.y == y && _lastClientPosition.z == z;
    }
    
    public int getLastClientDistance(int x, int y, int z)
    {
        int dx = (x - _lastClientPosition.x); 
        int dy = (y - _lastClientPosition.y); 
        int dz = (z - _lastClientPosition.z); 
        return (int)Math.sqrt(dx*dx + dy*dy + dz*dz);
    }
    
    public void setLastServerPosition(int x, int y, int z)
    {
        _lastServerPosition.x = x;
        _lastServerPosition.y = y;
        _lastServerPosition.z = z;
    }
    
    public boolean checkLastServerPosition(int x, int y, int z)
    {
        return _lastServerPosition.x == x && _lastServerPosition.y == y && _lastServerPosition.z == z;
    }
    
    public int getLastServerDistance(int x, int y, int z)
    {
        int dx = (x - _lastServerPosition.x); 
        int dy = (y - _lastServerPosition.y); 
        int dz = (z - _lastServerPosition.z); 
        return (int)Math.sqrt(dx*dx + dy*dy + dz*dz);
    }

	public void addExpAndSp(int addToExp, int addToSp) { getStat().addExpAndSp(addToExp, addToSp); }
    public void reduceCurrentHp(double i, L2Character attacker) { getStatus().reduceHp(i, attacker); }
    public void reduceCurrentHp(double value, L2Character attacker, boolean awake) { getStatus().reduceHp(value, attacker, awake); }
    
    
	public void broadcastSnoop(int type, String name, String _text)
	{
		if(_SnoopListener.size() > 0)
		{
			Snoop sn = new Snoop(getObjectId(),getName(),type,name,_text);
			for(L2PcInstance pci : _SnoopListener)
			{
				if(pci != null)
				{
					pci.sendPacket(sn);
				}
			}
		}
	}
	
	public void addSnooper(L2PcInstance pci )
	{
		if(!_SnoopListener.contains(pci))
			_SnoopListener.add(pci);
	}
	
	public void removeSnooper(L2PcInstance pci )
	{
		_SnoopListener.remove(pci);
	}
	
	public void addSnooped(L2PcInstance pci )
	{
		if(!_SnoopedPlayer.contains(pci))
			_SnoopedPlayer.add(pci);
	}
	
	public void removeSnooped(L2PcInstance pci )
	{
		_SnoopedPlayer.remove(pci);
	}
	
	public void addBypass(String bypass)
    {
    	_validBypass.add(bypass);
    	//_log.warning("[BypassAdd]"+getName()+" '"+bypass+"'");
    }
	public void addBypass2(String bypass)
    {
    	_validBypass2.add(bypass);
    	//_log.warning("[BypassAdd]"+getName()+" '"+bypass+"'");
    }
    
    public boolean validateBypass(String cmd)
    {
    	if(!Config.BYPASS_VALIDATION)
    		return true;

    	for(String bp : _validBypass)
    	{
    		//_log.warning("[BypassValidation]"+getName()+" '"+bp+"'");
    		if(bp.equals(cmd))
    			return true;
    	}
    	for(String bp : _validBypass2)
    	{
    		//_log.warning("[BypassValidation]"+getName()+" '"+bp+"'");
    		if(cmd.startsWith(bp))
    			return true;
    	}
    	
    	_log.warning("[L2PcInstance] player ["+getName()+"] sent invalid bypass '"+cmd+"', ban this player!");
    	return false;
    }
    
    public void clearBypass()
    {
    	_validBypass.clear();
    	_validBypass2.clear();
    }

	/**
	 * @return Returns the inBoat.
	 */
	public boolean isInBoat()
	{
		return _InBoat;
	}

	/**
	 * @param inBoat The inBoat to set.
	 */
	public void setInBoat(boolean inBoat)
	{
		_InBoat = inBoat;
	}

	/**
	 * @return
	 */
	public L2BoatInstance getBoat()
	{		
		return _Boat;
	}

	/**
	 * @param boat
	 */
	public void setBoat(L2BoatInstance boat)
	{
		_Boat = boat;
	}

	public void setInCrystallize(boolean inCrystallize)
	{
		_inCrystallize = inCrystallize;
	}

	public boolean isInCrystallize()
	{
		return _inCrystallize;
	}

	/**
	 * @return
	 */
	public Point3D getInBoatPosition()
	{
		return InBoatPosition;
	}
	public void setInBoatPosition(Point3D pt)
	{
		InBoatPosition = pt;
	}

	public boolean setBuySellList(BuySellList buySellList)
	{
		if(_BuySellList != null)
		{
			_log.warning("[L2PcInstance] attempt to re-initialize buysell list! Player "+getName());
			return false;
		}

		_BuySellList = buySellList;
		return true;
	}

	public BuySellList getBuySellList()
	{
		return _BuySellList;
	}
}
