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

import java.io.File;
import java.io.FileInputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.l2j.Config;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.CharTemplateTable;
import net.sf.l2j.gameserver.ClanTable;
import net.sf.l2j.gameserver.ClientScheduler;
import net.sf.l2j.gameserver.Connection;
import net.sf.l2j.gameserver.GmListTable;
import net.sf.l2j.gameserver.ItemTable;
import net.sf.l2j.gameserver.RecipeController;
import net.sf.l2j.gameserver.SkillTable;
import net.sf.l2j.gameserver.SkillTreeTable;
import net.sf.l2j.gameserver.ai.CtrlIntention;
import net.sf.l2j.gameserver.ai.L2CharacterAI;
import net.sf.l2j.gameserver.ai.L2PlayerAI;
import net.sf.l2j.gameserver.model.L2Skill.SkillTargetType;
import net.sf.l2j.gameserver.model.L2Skill.SkillType;
import net.sf.l2j.gameserver.model.quest.Quest;
import net.sf.l2j.gameserver.model.quest.QuestState;
import net.sf.l2j.gameserver.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.serverpackets.ChangeWaitType;
import net.sf.l2j.gameserver.serverpackets.CharInfo;
import net.sf.l2j.gameserver.serverpackets.DeleteObject;
import net.sf.l2j.gameserver.serverpackets.DoorInfo;
import net.sf.l2j.gameserver.serverpackets.DoorStatusUpdate;
import net.sf.l2j.gameserver.serverpackets.DropItem;
import net.sf.l2j.gameserver.serverpackets.InventoryUpdate;
import net.sf.l2j.gameserver.serverpackets.ItemList;
import net.sf.l2j.gameserver.serverpackets.MagicSkillCanceld;
import net.sf.l2j.gameserver.serverpackets.MyTargetSelected;
import net.sf.l2j.gameserver.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.serverpackets.NpcInfo;
import net.sf.l2j.gameserver.serverpackets.PartySmallWindowUpdate;
import net.sf.l2j.gameserver.serverpackets.PetInfo;
import net.sf.l2j.gameserver.serverpackets.PetItemList;
import net.sf.l2j.gameserver.serverpackets.PrivateBuyListBuy;
import net.sf.l2j.gameserver.serverpackets.PrivateBuyListSell;
import net.sf.l2j.gameserver.serverpackets.PrivateStoreMsgSell;
import net.sf.l2j.gameserver.serverpackets.RecipeShopSellList;
import net.sf.l2j.gameserver.serverpackets.ServerBasePacket;
import net.sf.l2j.gameserver.serverpackets.SocialAction;
import net.sf.l2j.gameserver.serverpackets.SpawnItem;
import net.sf.l2j.gameserver.serverpackets.StatusUpdate;
import net.sf.l2j.gameserver.serverpackets.StopMove;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.serverpackets.UserInfo;
import net.sf.l2j.gameserver.templates.L2CharTemplate;
import net.sf.l2j.gameserver.templates.L2Item;
import net.sf.l2j.gameserver.templates.L2PcTemplate;
import net.sf.l2j.gameserver.templates.L2Weapon;
import net.sf.l2j.gameserver.templates.L2WeaponType;


/**
 * This class represents a player character in the world.
 * there is always a client-thread connected to this 
 * (except if a player-store is activated upon logout)
 * 
 * @version $Revision: 1.66.2.41.2.33 $ $Date: 2005/04/11 10:06:09 $
 */
public final class L2PcInstance extends L2PlayableInstance
{
    private static final String DELETE_SKILL_FROM_CHAR = "DELETE FROM character_skills WHERE skill_id=? AND char_obj_id=?";
    private static final String ADD_NEW_SKILL = "INSERT INTO character_skills (char_obj_id,skill_id,skill_level,skill_name) values(?,?,?,?)";
    private static final String UPDATE_CHARACTER_SKILL_LEVEL = "UPDATE character_skills SET skill_level=? WHERE skill_id=? AND char_obj_id=?";

    private static final String GET_ALL_SKILLS_FOR_CHAR = "SELECT skill_id,skill_level FROM character_skills WHERE char_obj_id=?";
    private static final String COLUMN_SKILL_LEVEL = "skill_level";
    private static final String COLUMN_SKILL_ID = "skill_id";
    
    public static final int STORE_PRIVATE_NONE = 0;
    public static final int STORE_PRIVATE_SELL = 1;
    public static final int STORE_PRIVATE_BUY = 3;
    public static final int STORE_PRIVATE_MANUFACTURE = 5;

	private static final int[] EXPERTISE_LEVELS = {
		SkillTreeTable.getExpertiseLevel(0), //NONE
		SkillTreeTable.getExpertiseLevel(1), //D
		SkillTreeTable.getExpertiseLevel(2), //C
		SkillTreeTable.getExpertiseLevel(3), //B
		SkillTreeTable.getExpertiseLevel(4), //A
		SkillTreeTable.getExpertiseLevel(5), //S
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
	}

	private Connection _connection;
	
	//private L2Object _newTarget = null;
	private int _level;
	private int _charId = 0x00030b7a;
	//private int _canCraft = 0;
	private int _exp;
	private int _expBeforeDeath = 0;
	private int _sp;
	private int _karma;
	private int _pvpKills;
	private int _pkKills;
	private int _pvpFlag;
	private int _recomHave; // how much I was recommended by others
	private int _recomLeft; // how many recomendations I can give to others
	private static final Random _rnd = new Random();
	
	private int _maxLoad;
	private int _deleteTimer;
	private boolean _inStoreMode;
	private PcInventory _inventory = new PcInventory(this);
	private PcWarehouse _warehouse = new PcWarehouse(this);
	private boolean _waitTypeSitting;
	private int _crestId;
	
	private int _face;
	private int _hairStyle;
	private int _hairColor;
	
	private HashMap<String, QuestState> _quests = new HashMap<String, QuestState>();
    private ShortCuts _shortCuts = new ShortCuts(this);
	private MacroList _macroses = new MacroList(this); 
	private int _allyId;
	private L2TradeList _tradeList;
    private L2ManufactureList _createList;
	private ArrayList<TradeItem> _sellList;
	private ArrayList<TradeItem> _buyList;
	private int _privatestore;
	private ClassId _skillLearningClassId;
	
	//	pet
	private L2Summon _summon = null;
	
	// these values are only stored temporarily
	private boolean _partyMatchingAutomaticRegistration;
	private boolean _partyMatchingShowLevel;
	private boolean _partyMatchingShowClass;
	private String _partyMatchingMemo;
	
	private L2Party _party;
	// clan related attributes
	private int _clanId;
	private L2Clan _clan;
	private boolean _clanLeader;
	
	//GM Stuff
	private boolean _isInvul;
	private boolean _isGm;
	private int _accessLevel;
	
	
	// this is needed to find the inviting player for Party response
	// there can only be one active party request at once
	private L2PcInstance _currentTransactionRequester;
	private L2ItemInstance _arrowItem;
	
	private L2Weapon _fistsWeaponItem;
	
	private long _uptime;
    private String _accountName;
	
	public byte updateKnownCounter = 0;
	
	private int _adminLevel = 0;
	
	private Map<Integer, L2RecipeList> _recipebook = new TreeMap<Integer, L2RecipeList>();
	
    private int _mountType;
    
	// stats watch
	int oldMaxHP;
	int oldMaxMP;
	int expertiseIndex; // index in EXPERTISE_LEVELS
	
	private L2ItemInstance _enchantScroll = null;
    
    private boolean _usingClanWarehouse;
    private boolean _isOnline = false;
    
    protected boolean _inventoryDisable = false;
    
    protected HashMap<Integer, L2CubicInstance> _cubics = new HashMap<Integer, L2CubicInstance>();
    
	private L2FolkInstance _lastFolkNpc = null;
	
    public static L2PcInstance create(int objectId, L2PcTemplate template, String accountName,
			String name, int hairStyle, int hairColor, int face)
    {
		L2PcInstance player = new L2PcInstance(objectId, template, accountName);
		player.setName(name);
		player.setHairStyle(hairStyle);
		player.setHairColor(hairColor);
		player.setFace(face);
		boolean ok = player.createDb();
		if (!ok)
			return null;
		return player;
    }
    
    public static L2PcInstance load(int objectId)
    {
    	return restore(objectId);
    }
    
    private L2PcInstance(int objectId, L2PcTemplate template, String accountName)
    {
    	super(objectId, template);
        _accountName = accountName;
		_level = 1;
    }
    
    private L2PcInstance(int objectId, L2PcTemplate template)
    {
        this(objectId, template, null);
		_ai = new L2PlayerAI(new L2PcInstance.AIAccessor());
        restoreSkills();
        getInventory().restore();
        _macroses.restore();
        _shortCuts.restore();
	}
    
	public final L2PcTemplate getTemplate()
	{
		return (L2PcTemplate)_template;
	}
	
	public L2CharacterAI getAI() {
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
	
	public final int getLevel() { return _level; }
    public int getSex() { return getTemplate().isMale ? 0 : 1; }
    
	public int  getFace()         { return _face; }
	public void setFace(int face) { _face = face; }
	
	public int  getHairColor()              { return _hairColor; }
	public void setHairColor(int hairColor) { _hairColor = hairColor; }
	
	public int getHairStyle()               { return _hairStyle; }
	public void setHairStyle(int hairStyle) { _hairStyle = hairStyle; }
	
	public void isInStoreMode(boolean b) {_inStoreMode = b; }
	
    
	// overwritten, to watch changes of MAX_HP
	public int getMaxHp()
	{
        int val = super.getMaxHp();
		if (val != oldMaxHP)
		{
			oldMaxHP = val;
			if (getCurrentHp() != val)
				setCurrentHp(getCurrentHp()); // trigger start of regeneration
		}
        return val;
	}
	
	// may be overwritten, to watch changes of MAX_MP
	public int getMaxMp()
	{
        int val = super.getMaxMp();
		if (val != oldMaxMP)
		{
			oldMaxMP = val;
			if (getCurrentMp() != val)
				setCurrentMp(getCurrentMp()); // trigger start of regeneration
		}
        return val;
	}
	
    public void logout()
    {
        _connection.close();
    }
    
	public L2RecipeList[] getRecipeBook()
	{
		return _recipebook.values().toArray(new L2RecipeList[_recipebook.values().size()]);
	}
	
	public void registerRecipeList(L2RecipeList recipe)
	{
		_recipebook.put(recipe.getId(), recipe);
	}
	
    public void unregisterRecipeList(int RecipeID)
    {
        if(_recipebook.containsKey(RecipeID))
            _recipebook.remove(RecipeID);
        else
            _log.warning("Attempted to remove unknown RecipeList"+RecipeID);
    }

	public void fillQuestDrops(L2NpcInstance npc, ArrayList<L2DropData> drops)
	{
		for (QuestState qs : _quests.values()) {
			qs.fillQuestDrops(npc, drops);
		}
	}
	
	public QuestState getQuestState(String quest)
	{
		return _quests.get(quest);
	}
	
	public void setQuestState(QuestState qs)
	{
		_quests.put(qs.getQuest().name, qs);
	}
	
	public void delQuestState(String quest)
	{
		_quests.remove(quest);
	}
	
	public Quest[] getAllActiveQuests()
	{
		ArrayList<Quest> quests = new ArrayList<Quest>();
		for (QuestState qs : _quests.values()) {
			if (qs.isCompleted() && !Config.DEVELOPER)
				continue;
			if (!qs.isStarted() && !Config.DEVELOPER)
				continue;
			quests.add(qs.getQuest());
		}
		return quests.toArray(new Quest[quests.size()]);
	}
	
	public QuestState[] getQuestsForTalk(int npcId)
	{
		QuestState[] states = null;
		for (QuestState qs : _quests.values()) {
			if (qs.waitsForTalk(npcId)) {
				if (states == null) {
					states = new QuestState[]{qs}; 
				} else {
					int len = states.length;
					QuestState[] tmp = new QuestState[len+1];
					for (int i=0; i < len; i++)
						tmp[i] = states[i];
					tmp[len] = qs;
					states = tmp;
				}
			}
		}
		return states;
	}
	
	public QuestState[] getQuestsForKills(int npcId)
	{
		QuestState[] states = null;
		for (QuestState qs : _quests.values()) {
			if (qs.waitsForKill(npcId)) {
				if (states == null) {
					states = new QuestState[]{qs}; 
				} else {
					int len = states.length;
					QuestState[] tmp = new QuestState[len+1];
					for (int i=0; i < len; i++)
						tmp[i] = states[i];
					tmp[len] = qs;
					states = tmp;
				}
			}
		}
		return states;
	}
	
	public QuestState processQuestEvent(String quest, String event)
	{
		if (event == null)
			event = "";
		QuestState qs = getQuestState(quest);
		if (qs == null && event.length() == 0)
			return null;
		if (qs == null) {
			Quest q = Quest.findQuest(quest);
			if (q == null)
				return null;
			qs = q.newQuestState(this);
		}
		if (qs != null) {
			if (qs.getQuest().notifyEvent(event, qs))
				showQuestWindow(quest, qs.getStateId());
		}
		
		return qs;
	}
	
	private void showQuestWindow(String questId, String stateId) {
		FileInputStream fis = null;
        String content;
		try
		{
			File f = new File(Config.DATAPACK_ROOT, "data/jscript/quests/"+questId+"/"+stateId+".htm");
			if (f.exists()) {
				if (Config.DEBUG)
					_log.fine("Showing quest window for quest "+questId+" state "+stateId+" html path: "+f);
				fis = new FileInputStream(f);
				byte[] raw = new byte[fis.available()];
				fis.read(raw);
				content = new String(raw, "UTF-8");
				NpcHtmlMessage npcReply = new NpcHtmlMessage(5);
				npcReply.setHtml(content);
				sendPacket(npcReply);
			}
		} catch (Exception e) {
			_log.warning("problem with npc text "+e);
		} finally {
			try	{ if (fis != null) fis.close(); } catch (Exception e) {/* ignore this */}
		}
		
		sendPacket( new ActionFailed() );
	}


	public L2ShortCut[] getAllShortCuts()
	{
		return _shortCuts.getAllShortCuts();
	}
	
	public L2ShortCut getShortCut(int slot, int page)
	{
		return _shortCuts.getShortCut(slot, page);
	}
	
	public void registerShortCut(L2ShortCut shortcut)
	{
		_shortCuts.registerShortCut(shortcut);
	}

	/**
	 * @param slot
	 */
	public void deleteShortCut(int slot, int page)
	{
		_shortCuts.deleteShortCut(slot, page);
	}
	
	public void registerMacro(L2Macro macro)
	{
		_macroses.registerMacro(macro);
	}
	
	public void deleteMacro(int id)
	{
		_macroses.deleteMacro(id);
	}
	
	public MacroList getMacroses() {
		return _macroses;
	}
	
	/**
	 * @param pvpFlag
	 */
	public void setPvpFlag(int pvpFlag)
	{
		_pvpFlag = pvpFlag;
	}

	/**
	 * @return
	 */
	public boolean getCanCraft()
	{
		return getSkillLevel(L2Skill.SKILL_CRAFTING) >= 1;
	}

	/**
	 * @return
	 */
	public int getPkKills()
	{
		return _pkKills;
	}

	/**
	 * @param pkKills
	 */
	public void setPkKills(int pkKills)
	{
		_pkKills = pkKills;
	}

	/**
	 * @return
	 */
	public int getDeleteTimer()
	{
		return _deleteTimer;
	}

	/**
	 * @param deleteTimer
	 */
	public void setDeleteTimer(int deleteTimer)
	{
		_deleteTimer = deleteTimer;
	}

	/**
	 * @return
	 */
	public int getCurrentLoad()
	{
		return _inventory.getTotalWeight();
	}

	public int getRecomHave() {
		return _recomHave;
	}

	public int setRecomHave(int recom) {
		if (recom > 255)
			recom = 255;
		return _recomHave = recom;
	}

	public int getRecomLeft() {
		return _recomLeft;
	}

	public int setRecomLeft(int recom) {
		return _recomLeft = recom;
	}

	/**
	 * @return
	 */
	public int getKarma()
	{
		return _karma;
	}

	/**
	 * @param karma
	 */
	public void setKarma(int karma)
	{
		_karma = karma;
		if (karma > 0)
		{
			StatusUpdate su = new StatusUpdate(getObjectId());
			su.addAttribute(StatusUpdate.KARMA, karma);
			sendPacket(su);
			broadcastPacket(su);
		}
	}

	/**
	 * @return
	 */
	public int getMaxLoad()
	{
		return _maxLoad;
	}

	/**
	 * @param maxLoad
	 */
	public void setMaxLoad(int maxLoad)
	{
		_maxLoad = maxLoad;
	}

	public void refreshOverloaded()
	{
		if (getMaxLoad() > 0)
			setOverloaded(getCurrentLoad() > getMaxLoad());
	}
	/**
	 * @return
	 */
	public int getPvpKills()
	{
		return _pvpKills;
	}

	/**
	 * @param pvp
	 */
	public void setPvpKills(int pvpKills)
	{
		_pvpKills = pvpKills;
	}

	/**
	 * @return
	 */
	public ClassId getClassId()
	{
		return getTemplate().classId;
	}
	
	/**
	 * @param className
	 */
	public void setClassId(int classId)
	{
		L2PcTemplate t = CharTemplateTable.getInstance().getTemplate(classId, getSex()==1);
		if (t == null)
		{
			_log.severe("Missing template for classId: "+classId);
			throw new Error();
		}
		setTemplate(t);
	}

	/**
	 * @return
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
	
	public void setFistsWeaponItem(L2Weapon weaponItem)
	{
		_fistsWeaponItem = weaponItem;
	}
	
	public L2Weapon getFistsWeaponItem()
	{
		return _fistsWeaponItem;
	}
	
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
	
	public void addExpAndSp(int addToExp, int addToSp)
	{
		if (Config.DEBUG) _log.fine("adding " + addToExp + " exp and " +addToSp+" sp to " + getName());
	    _exp += addToExp;
	    _sp += addToSp;
	    
	    //only forgive karma if mob at xp lvl 
		if (_exp != 0 || isGM()) { _karma -= 1; } 
		if (_karma < 0) { _karma = 0; } 

		StatusUpdate su = new StatusUpdate(getObjectId());
		su.addAttribute(StatusUpdate.EXP, _exp);
		su.addAttribute(StatusUpdate.SP, _sp);
		su.addAttribute(StatusUpdate.KARMA, _karma);
		sendPacket(su);
		
		SystemMessage sm = new SystemMessage(SystemMessage.YOU_EARNED_S1_EXP_AND_S2_SP);
		sm.addNumber(addToExp);
		sm.addNumber(addToSp);
		sendPacket(sm);
	    
		if (_exp >= Experience.LEVEL[getLevel() + 1])
		{
			SocialAction sa = new SocialAction(getObjectId(), 15);
			broadcastPacket(sa);
			sendPacket(sa);
		}
		
		while (_exp >= Experience.LEVEL[getLevel() + 1])
	    {
	        // Looks like the level should be increased.
	        increaseLevel();
	        sendPacket(new UserInfo(this)); 
	    }
	}
	
	/**
	 *  reward player with free skill at certain lvl's.
	 */
	private void rewardSkills()
	{
		//int charclass = getClassId();
		int lvl = getLevel();
        
		if (lvl == 5) // remove the noobie skills
		{		
			L2Skill skill = SkillTable.getInstance().getInfo(194, 1);
			skill = removeSkill(skill);
			if (Config.DEBUG && skill != null) _log.fine("removed skill 'Lucky' from "+getName());
		}
		for (int i=0; i < EXPERTISE_LEVELS.length; i++) {
			if (lvl >= EXPERTISE_LEVELS[i]) {
				expertiseIndex = i;
        }
		}
//        int expertiseLvl = -1;
//        switch (lvl)
//        {
//        case 20:
//            expertiseLvl = 1;
//            break;
//        case 40:
//            expertiseLvl = 2;
//            break;
//        case 52:
//            expertiseLvl = 3;
//            break;
//        case 61:
//            expertiseLvl = 4;
//            break;
//        case 67:
//            expertiseLvl = 5;
//            break;
//        }
        
        if (expertiseIndex > 0) {
            L2Skill skill = SkillTable.getInstance().getInfo(239, expertiseIndex);
            addSkill(skill);
            if (Config.DEBUG) _log.fine("awarded "+getName()+" with new expertise.");
        } else {
			if (Config.DEBUG) _log.fine("No skills awarded at lvl: "+lvl);
		}
        // this function gets called on login, so not such a bad place to check weight
        refreshOverloaded();
	}
	

	/**
	 * @param exp
	 */
	public void setExp(int exp)
	{
		if (exp < 0)
			exp = 0;
		_exp = exp;
	}

	/**
	 * @return
	 */
	public Race getRace()
	{
		return getTemplate().race;
	}

	/**
	 * @return
	 */
	public int getSp()
	{
		return _sp;
	}
	
	/**
	 * @param sp
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
	 * @return
	 */
	public int getClanId()
	{
		return _clanId;
	}

    public int getClanCrestId()
    {
        if (_clan != null && _clan.hasCrest())
        {
            return _clanId;
        }
        else
        {
            return 0;
        }
    }
    
	/**
	 * @return
	 */
	public PcInventory getInventory()
	{
		return _inventory;
	}

	/**
	 * @param activeChar
	 * @param item
	 */
	public void removeItemFromInventory(L2ItemInstance item, int count)
	{
		L2ItemInstance item2 = getInventory().destroyItem(item.getObjectId(), count);
		
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
	
	public boolean isSitting()
	{
		return _waitTypeSitting;
	}

	/**
	 * @param waitType The waitType to set.
	 */
	public void sitDown()
	{
		if (!_waitTypeSitting)
		{
			_waitTypeSitting = true;
			getAI().setIntention(CtrlIntention.AI_INTENTION_REST);
			broadcastPacket(new ChangeWaitType (this, false));
		}
	}
	
	public void standUp()
	{
		if (_waitTypeSitting && !_inStoreMode)
		{
			_waitTypeSitting = false;
			getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
			broadcastPacket(new ChangeWaitType (this, true));
		}
	}


	public Warehouse getWarehouse()
	{
		return _warehouse;
	}
	
	/**
	 * @return Returns the charId.
	 */
	public int getCharId()
	{
		return _charId;
	}

	/**
	 * @param charId The charId to set.
	 */
	public void setCharId(int charId)
	{
		_charId = charId;
	}

	/**
	 * @return
	 */
	public int getAdena()
	{
		return _inventory.getAdena();
	}

	/**
	 * @param adena The adena to set.
	 */
	public void reduceAdena(int adena)
	{
		_inventory.reduceAdena(adena);
	}

	/**
	 * @param adena The adena to set.
	 */
	public void addAdena(int adena)
	{
		_inventory.addAdena(adena);
	}

	/**
	 * @return
	 */
//	public Connection getNetConnection()
//	{
//		return _netConnection;
//	}

	/**
	 * @param connection
	 */
	public void setNetConnection(Connection connection)
	{
		_connection = connection;
	}
	
	/**
	 * @param connection
	 */
	public void closeNetConnection()
	{
		_connection.close();
	}
	
	/**
	 * @return
	 */
	public int getCrestId()
	{
		return _crestId;
	}

	/**
	 * @param crestId The crestId to set.
	 */
	public void setCrestId(int crestId)
	{
		_crestId = crestId;
	}
	
	public void onAction(L2PcInstance player)
	{
		if (player.isConfused())
		{
			ActionFailed af = new ActionFailed();
			player.sendPacket(af);
		}
		if (player.getTarget() != this)
		{
			player.setTarget(this);
			MyTargetSelected my = new MyTargetSelected(getObjectId(), 0);
			player.sendPacket(my);
		}
		else
		{
//			player.setCurrentState(STATE_FOLLOW);
			if (getPrivateStoreType() != 0 )
//				player.setCurrentState(STATE_INTERACT);
				player.getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, this);
			else
				player.getAI().setIntention(CtrlIntention.AI_INTENTION_FOLLOW, this);
//			
//			player.moveTo(getX(), getY(), getZ(), 36);
		}
	}
	
	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.model.L2Character#broadCastUpdate()
	 */
	public void broadcastStatusUpdate()
	{
		super.broadcastStatusUpdate();
		
		StatusUpdate su = new StatusUpdate(getObjectId());
		su.addAttribute(StatusUpdate.CUR_HP, (int) getCurrentHp());
		su.addAttribute(StatusUpdate.CUR_MP, (int) getCurrentMp());
		su.addAttribute(StatusUpdate.CUR_CP, (int) getCurrentCp());
		su.addAttribute(StatusUpdate.MAX_CP,       getMaxCp());

		sendPacket(su);
		
		if (isInParty()) {
			PartySmallWindowUpdate update = new PartySmallWindowUpdate(this);
			getParty().broadcastToPartyMembers(this, update);
		}
	}

	
	public final void broadcastUserInfo()
	{
		UserInfo ui = new UserInfo(this);
		this.sendPacket(ui);
		CharInfo info = new CharInfo(this);
		if (Config.DEBUG) _log.fine("players to notify:" + getKnownPlayers().size() + " packet:"+info.getType());
		this.broadcastPacketToOthers(info);
	}
	
	/**
	 * @return
	 */
	public int getAllyId()
	{
		return _allyId;
	}
	/**
	 * @param allyId The allyId to set.
	 */
	public void setAllyId(int allyId)
	{
		_allyId = allyId;
	}
	
	protected void onHitTimer(L2Character target, int damage, boolean crit, boolean miss, boolean soulshot, boolean shld)
	{
		super.onHitTimer(target, damage, crit, miss, soulshot, shld);
		// PvP added by ZDC
		// PvP Modified by Delicioso : Killer don't have 300 Karma anymore when killing someone.
		if ((target instanceof L2PcInstance) && target.isAlikeDead() && _karma == 0){
		//if (target.isDead() && _karma == 0){
				stopPvPFlag(); // needed?
		}
	}
	
	public void sendPacket(ServerBasePacket packet)
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
		} else {
			// _interactTarget=null should never happen but one never knows ^^;
			if (target != null)
				target.onAction(this);
		}
	}

	public void doAutoLoot(L2ItemInstance target)
    {
        if  (!isInParty()) {
            if (target.getItemId() == 57) {
                SystemMessage smsg = new SystemMessage(SystemMessage.YOU_PICKED_UP_S1_ADENA); // you picked up $s1 adena
                smsg.addNumber(target.getCount());
                sendPacket(smsg);               
            } else {
                SystemMessage smsg = new SystemMessage(SystemMessage.YOU_PICKED_UP_S1_S2); // you picked up $s1$s2
                smsg.addNumber(target.getCount());
                smsg.addItemName(target.getItemId());
                sendPacket(smsg);
            }
            
            L2ItemInstance target2 = getInventory().addItem(target);
            InventoryUpdate iu = new InventoryUpdate();
            if (target2.getLastChange() == L2ItemInstance.ADDED) {
                iu.addNewItem(target);
            } else {
                iu.addModifiedItem(target2);
            }
            sendPacket(iu);

            StatusUpdate su = new StatusUpdate(getObjectId());
            su.addAttribute(StatusUpdate.CUR_LOAD, getCurrentLoad());
            sendPacket(su);
            
        } else {
            if (target.getItemId() == 57) {
                getParty().distributeAdena(target);
            } else {
                getParty().distributeItem(this, target);
            }
        }
    }
	
    protected void doPickupItem(L2Object object)
	{
		getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
//		setCurrentState(STATE_IDLE);

		if (! (object instanceof L2ItemInstance))
		{
			// dont try to pickup anything that is not an item :)
			_log.warning("trying to pickup wrong target."+getTarget());
			return;
		}
		
		L2ItemInstance target = (L2ItemInstance) object;
		
		sendPacket(new ActionFailed());
		
		StopMove sm = new StopMove(getObjectId(), getX(), getY(), getZ(), getHeading()); 
		if (Config.DEBUG) _log.fine("pickup pos: "+ target.getX() + " "+target.getY()+ " "+target.getZ() );
		sendPacket(sm);
		
		synchronized (target)
		{
			if (!target.isVisible())
			{
				sendPacket(new ActionFailed());
			return;
		}
			target.pickupMe(this);
		}
		
		if  (!isInParty()) {
			if (target.getItemId() == 57) {
				SystemMessage smsg = new SystemMessage(SystemMessage.YOU_PICKED_UP_S1_ADENA); // you picked up $s1 adena
				smsg.addNumber(target.getCount());
				sendPacket(smsg);				
			} else {
				SystemMessage smsg = new SystemMessage(SystemMessage.YOU_PICKED_UP_S1_S2); // you picked up $s1$s2
				smsg.addNumber(target.getCount());
				smsg.addItemName(target.getItemId());
				sendPacket(smsg);
			}
			
			L2ItemInstance target2 = getInventory().addItem(target);
			InventoryUpdate iu = new InventoryUpdate();
			if (target2.getLastChange() == L2ItemInstance.ADDED) {
				iu.addNewItem(target);
			} else {
				iu.addModifiedItem(target2);
			}
			sendPacket(iu);
			
			StatusUpdate su = new StatusUpdate(getObjectId());
			su.addAttribute(StatusUpdate.CUR_LOAD, getCurrentLoad());
			sendPacket(su);
			
		} else {
			if (target.getItemId() == 57) {
				getParty().distributeAdena(target);
			} else {
				getParty().distributeItem(this, target);
			}
		}
	}

	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.model.L2Character#setTarget(net.sf.l2j.gameserver.model.L2Object)
	 */
	public void setTarget(L2Object newTarget)
	{
		if (newTarget != null && !newTarget.isVisible())
			newTarget = null;
		// cancel old target
		L2Object oldTarget = getTarget();
		
		if (oldTarget != null)
		{
			if (oldTarget.equals(newTarget))
			{
				return; // no target change
			}
		
			// remove listener if the target was a char
			if (oldTarget instanceof L2Character)
			{
				((L2Character) oldTarget).removeStatusListener(this);
			}
		}

		if (newTarget != null && newTarget instanceof L2Character)
		{
			((L2Character) newTarget).addStatusListener(this);
		}
			
		super.setTarget(newTarget);
	}


	public L2ItemInstance getActiveWeaponInstance()
	{
		return getInventory().getPaperdollItem(Inventory.PAPERDOLL_RHAND);
	}
	public L2Weapon getActiveWeaponItem()
	{
		L2ItemInstance weapon = getActiveWeaponInstance();
		if (weapon == null)
			return getFistsWeaponItem();
		return (L2Weapon) weapon.getItem();
	}

	public L2ItemInstance getSecondaryWeaponInstance()
	{
		return getInventory().getPaperdollItem(Inventory.PAPERDOLL_LHAND);
	}
	public L2Weapon getSecondaryWeaponItem()
	{
		L2ItemInstance weapon = getSecondaryWeaponInstance();
		if (weapon == null)
			return getFistsWeaponItem();
		return (L2Weapon) weapon.getItem();
	}

	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.model.L2Character#reduceCurrentHp(int, net.sf.l2j.gameserver.model.L2Character)
	 */
	public void reduceCurrentHp(double i, L2Character attacker) 
	{
		boolean notInArena = !withinZones(L2World.arenas);
		
		//give reciever of damage updated pvp timer if in pvp 
		if (attacker instanceof L2PcInstance 
				&& getPvpFlag() != 0
				&& !isInsidePvPZone())
		{ 
			setlastPvpAttack(System.currentTimeMillis()); 
		} 
		if (this.isInvul()) return;
		super.reduceCurrentHp(i, attacker);
		
		if (!isDead())
		{
			if (attacker != null && attacker instanceof L2PcInstance) 
				((L2PcInstance)attacker).checkPvP(this); 
		}
		
		if (attacker != null)
		{
			SystemMessage smsg = new SystemMessage(SystemMessage.S1_GAVE_YOU_S2_DMG);
			if (Config.DEBUG) _log.fine("Attacker:"+attacker.getName());
			if (attacker instanceof L2MonsterInstance || attacker instanceof L2NpcInstance)
			{
				int mobId = ((L2NpcInstance) attacker).getTemplate().npcId;
				if (Config.DEBUG) _log.fine("mob id:"+mobId);
				smsg.addNpcName(mobId);
			} else {
				smsg.addString(attacker.getName());
			}
			smsg.addNumber((int)i);
			sendPacket(smsg);
		}
	}


	protected void doDie(L2Character killer) {
		if (Config.ALT_GAME_DELEVEL) {
			if (!isInsidePvPZone() && getSkillLevel(L2Skill.SKILL_LUCKY) < 0)
				deathPenalty();
		}
		if (getPet() != null) 
			getPet().unSummon(this); 
		
		// PvP Modified by Delicioso
		//step 0: reset pvp flag on death 
		//setPvpFlag(0); 
		// step 1: check if attacker need to change Karma
			if (killer != null && !isInsidePvPZone())
			{	// Killed by a player
				if (killer instanceof L2PcInstance)
				{
					L2PcInstance pk = (L2PcInstance) killer;
					if (this.getPvpFlag() == 0 ) // The dead one didnt hit
					{
						if (this.getKarma() < 200 ) // The dead one is innocent
						{ 
							pk.increaseKarma(75); // Killer's karma increases by 75
							pk.setPkKills(pk.getPkKills()+1); // Pk count +1
						}
						if (this.getKarma() >= 200) // The dead one is a PK.
						{
							pk.setPvpKills(pk.getPvpKills()+1); //Pvp count +1
							// The following lines to allow players to gain adenas
							// and exp/sp when killing a PK
							//pk.addAdena(this.getLevel()*(_rnd.nextInt(500)+500));
							//pk.addExpAndSp(this.getLevel()*(_rnd.nextInt(500)+500),this.getLevel()*(_rnd.nextInt(150)+100));
						}					
					}
					if (this.getPvpFlag() != 0 ) // The dead one hitted back : No Karma
					{
						if (this.getKarma() >= 200) //The dead one is a PK 
						{
							//pk.addAdena(this.getLevel()*(_rnd.nextInt(500)+500));
							//pk.addExpAndSp(this.getLevel()*(_rnd.nextInt(500)+500),this.getLevel()*(_rnd.nextInt(150)+100));
						}
						pk.setPvpKills(pk.getPvpKills()+1);
					}
				}
			//Step 2: lower dead player's karma, if anything
			if (_karma<=100)
				setKarmaFlag(getCharId(),0);
			
			_karma = (_karma>0)?_karma-100:0;
			_karma = (_karma<0)?0:_karma;
		}
		setPvpFlag(0); 
		super.doDie(killer);
	}

	public void restoreExp()
    {
		if (_expBeforeDeath != 0) {
			_exp = _expBeforeDeath;
			_expBeforeDeath = 0;
			StatusUpdate su = new StatusUpdate(getObjectId());
			su.addAttribute(StatusUpdate.EXP, _exp);
			sendPacket(su);
		}
	}

    // TODO Need Correct Penalty
	public void deathPenalty()
    {
		final int lvl = getLevel();
        //the death steal you some Exp
        final double percentLost = -0.07 * lvl + 6.5;
        
		int lostExp = (int) Math.round((Experience.LEVEL[lvl+1]-Experience.LEVEL[lvl]) * percentLost /100);
        _expBeforeDeath = getExp();
		
        _log.fine("is dead, so exp to remove:"+lostExp);
        setExp(getExp()- lostExp);
		if (getExp() < Experience.LEVEL[lvl])
			decreaseLevel();
        StatusUpdate su = new StatusUpdate(getObjectId());
        su.addAttribute(StatusUpdate.EXP, getExp());
        sendPacket(su);
        
        //randomly you lost item(s)
  /*      Random rand = new Random();
        if (0 == rand.nextInt(10)) { //you have 10% chance to drop  
            
            final L2ItemInstance[] inv = _inventory.getItems();
            
            final ArrayList<L2ItemInstance> equipItems = new ArrayList<L2ItemInstance>();
            final ArrayList<L2ItemInstance> unEquipItems = new ArrayList<L2ItemInstance>();
            
            for (int index = 0; index < inv.length; index++) {
                final L2ItemInstance item = inv[index];
                if (item.getEquipSlot() > 0) { //equipped item
                    if (item.getEquipSlot() == Inventory.PAPERDOLL_RHAND
                            || item.getEquipSlot() == Inventory.PAPERDOLL_LRHAND) { //take weapon only if karma > 0
                        if (_karma > 0) {
                            equipItems.add(item); 
                        }
                    } else {
                        equipItems.add(item);
                    }
                } else { //unequipped item
                    if (item.getItemId() != 57) //don't take adena
                        unEquipItems.add(item);
                }
            }
            
            L2ItemInstance oldItem = null;
            boolean noItem = false;
            if (0 == rand.nextInt(4)) { //25% chance the droped item is equipped, 75% unequipped
                if (equipItems.size() > 0)
                    oldItem = equipItems.get(rand.nextInt(equipItems.size()));
                else
                    noItem = true;
            } else {
                if (unEquipItems.size() > 0)
                    oldItem = unEquipItems.get(rand.nextInt(unEquipItems.size()));
                else
                    noItem = true;
            }
            
            if (!noItem) {
                int countToDrop = 1;
                final int oId = oldItem.getObjectId();
                L2ItemInstance dropedItem = null;
                _log.fine(".drop item oid:"+oId+" slot:"+oldItem.getEquipSlot());
                
                if (oldItem.isStackable()) {
                    countToDrop = oldItem.getCount(); //drop all the quantity
                }
                int oldCount = oldItem.getCount();
                
                if (oldItem.isEquipped())
                {
                    _log.fine("item is equiped");
                    dropedItem = _inventory.dropItem(oId, countToDrop);
                    // show the unequip update in the inventory. without this, the client still shows
                    // the equiped item ! stupid system
                    InventoryUpdate iu = new InventoryUpdate();
                    iu.addModifiedItem(oldItem);
                    sendPacket(iu);
                    
                    UserInfo ui = new UserInfo(this);
                    sendPacket(ui);
                    
                } else {
                    dropedItem = _inventory.dropItem(oId, countToDrop);
                }
                
                dropedItem.setX(getX());
                dropedItem.setY(getY());
                dropedItem.setZ(getZ());
                
                // mark it as 
                dropedItem.setOnTheGround(true);
                
                DropItem di = new DropItem(dropedItem, getObjectId()); 
                sendPacket(di);
                addKnownObjectWithoutCreate(dropedItem);
                
                // also notify all other players about our movement
                broadcastPacket(di);
                for (L2PcInstance player : getKnownPlayers())
                {
                    player.addKnownObjectWithoutCreate(dropedItem);
                }
                
                InventoryUpdate iu = new InventoryUpdate();
                if (oldCount <= dropedItem.getCount())  // we drop all the had, so remove the item
                {
                    if (Config.DEBUG) _log.finest("remove item from inv");
                    iu.addRemovedItem(oldItem);
                }
                else
                {
                    if (Config.DEBUG) _log.finest("reducing item in inv");
                    iu.addModifiedItem(oldItem);
                }
                sendPacket(iu);
                
                SystemMessage sm = new SystemMessage(298);
                sm.addItemName(dropedItem.getItemId());
                sendPacket(sm);
                
                sendPacket(new UserInfo(this));
                
                L2World.getInstance().addVisibleObject(dropedItem);
                
            }
        }
	*/	
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
	 * @param client
	 * @return
	 */
	public void setTransactionRequester(L2PcInstance requestor)
	{
		_currentTransactionRequester = requestor;
	}

	/**
	 * @return
	 */
	public L2PcInstance getTransactionRequester()
	{
		return _currentTransactionRequester;
	}
	
	public boolean isTransactionInProgress()
	{
		return _currentTransactionRequester != null;
	}

	protected int getDistanceToWatchObject(L2Object object)
	{
		return 4000;
	}
	protected int getDistanceToForgetObject(L2Object object)
	{
		return 6000;
	}

	public boolean addKnownObject(L2Object object, L2Character dropper)
	{
		boolean ret = super.addKnownObject(object, dropper);
		if (!ret)
			return false;

		if (object instanceof L2ItemInstance)
		{
			if (dropper != null)
				sendPacket(new DropItem((L2ItemInstance) object, dropper.getObjectId()));
			else
			sendPacket(new SpawnItem((L2ItemInstance) object));
		}
		else if (object instanceof L2DoorInstance) {
			sendPacket(new DoorInfo((L2DoorInstance) object));
			sendPacket(new DoorStatusUpdate((L2DoorInstance) object));
		}
		else if (object instanceof L2NpcInstance)
		{
			sendPacket(new NpcInfo((L2NpcInstance) object, this));
		}
		else if (object instanceof L2Summon)
		{
            L2Summon summon = (L2Summon) object;
            if (this.equals(summon.getOwner()))
            {
                sendPacket(new PetInfo(summon));
                if (summon instanceof L2PetInstance)
	            {
                    sendPacket(new PetItemList((L2PetInstance)summon));
                }
            }
            else 
            {
                sendPacket(new NpcInfo(summon, this));
            }
		}
		else if (object instanceof L2PcInstance)
		{
			// send player info to our client
			L2PcInstance otherPlayer = (L2PcInstance) object;
			sendPacket(new CharInfo(otherPlayer));
			if (otherPlayer.getPrivateStoreType() == STORE_PRIVATE_BUY || 
                otherPlayer.getPrivateStoreType() == STORE_PRIVATE_SELL ||
                otherPlayer.getPrivateStoreType() == STORE_PRIVATE_MANUFACTURE)
			{
//				sendPacket(new ChangeWaitType (otherPlayer,0));
				if (otherPlayer.getTradeList() != null)
					sendPacket(new PrivateStoreMsgSell(otherPlayer));
			}
		}
		
		if (object instanceof L2Character)
		{
			L2Character obj = (L2Character) object;
			obj.getAI().describeStateToPlayer(this);
		}
		
		return ret;
	}
	
	public void removeKnownObject(L2Object object)
	{
		if (knowsObject(object)) {
		super.removeKnownObject(object);
		sendPacket(new DeleteObject(object));
	}
	}
	
	
	public void increaseLevel()
	{
		if (Config.DEBUG) _log.finest("increasing level of " + getName());
		if (_level < 75)
			_level++;
		
		sendPacket(new SystemMessage(SystemMessage.YOU_INCREASED_YOUR_LEVEL));
		
		StatusUpdate su = new StatusUpdate(getObjectId());
		su.addAttribute(StatusUpdate.LEVEL, _level);
		su.addAttribute(StatusUpdate.MAX_HP, getMaxHp());
		su.addAttribute(StatusUpdate.MAX_MP, getMaxMp());
		sendPacket(su);
		// increase hp
		setCurrentHpMp(getMaxHp(),getMaxMp());
		// recalculate party lvl
		if (isInParty()) getParty().recalculatePartyLevel();
		
		switch (getLevel()) {
		case 10: setRecomLeft(getRecomLeft()+3); break;
		case 20: setRecomLeft(getRecomLeft()+3); break;
		case 30: setRecomLeft(getRecomLeft()+2); break;
		case 40: setRecomLeft(getRecomLeft()+2); break;
		case 50: setRecomLeft(getRecomLeft()+3); break;
		case 60: setRecomLeft(getRecomLeft()+5); break;
		case 70: setRecomLeft(getRecomLeft()+7); break;
		case 75: setRecomLeft(getRecomLeft()+9); break;
		}

        rewardSkills();
	}

	public void decreaseLevel()
	{
		switch (getLevel()) {
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
		if (Config.DEBUG) _log.finest("decreasing level of " + getName());
		_level--;
		
		StatusUpdate su = new StatusUpdate(getObjectId());
		su.addAttribute(StatusUpdate.LEVEL, _level);
		su.addAttribute(StatusUpdate.MAX_HP, getMaxHp());
		su.addAttribute(StatusUpdate.MAX_MP, getMaxMp());
		sendPacket(su);
		// recalculate party lvl
		if (isInParty()) getParty().recalculatePartyLevel();
		
        rewardSkills();
	}

	private static double getMod(int lvl, int classLvl, int classAdd, int classBase, double classModifier)
	{
		if (lvl == classLvl)
			return classBase;
		
		double hp = (lvl - classLvl) * classModifier + classAdd + getMod(lvl - 1, classLvl, classAdd, classBase, classModifier);
		return hp;
	}
	


	public void stopAllTimers()
	{
		stopHpMpRegeneration();
	}
	
	public L2Summon getPet()
	{
		return _summon;
	}

	public void setPet(L2Summon summon)
	{
		_summon = summon;
	}
	
	public void deleteMe()
	{
        try { setOnlineStatus(false); } catch (Throwable t) {_log.log(Level.SEVERE, "deletedMe()", t); }

		// stop all scheduled events
		try { stopAllTimers(); } catch (Throwable t) {_log.log(Level.SEVERE, "deletedMe()", t); }

		// stop crafting, if in progress
		try { RecipeController.getInstance().requestMakeItemAbort(this); } catch (Throwable t) {_log.log(Level.SEVERE, "deletedMe()", t); }

		try { setTarget(null); } catch (Throwable t) {_log.log(Level.SEVERE, "deletedMe()", t); }
		if (isVisible())
			try { decayMe(); } catch (Throwable t) {_log.log(Level.SEVERE, "deletedMe()", t); }
		
		if (isInParty()) try { leaveParty(); } catch (Throwable t) {_log.log(Level.SEVERE, "deletedMe()", t); }
		
		if (getPet() != null)
		{
			try { getPet().unSummon(this); } catch (Throwable t) {_log.log(Level.SEVERE, "deletedMe()", t); }// returns pet to control item
		}
		
		if (getClanId() != 0 && getClan() != null)
		{
			// set the status for pledge member list to OFFLINE
			try { getClan().getClanMember(getName()).setPlayerInstance(null); } catch (Throwable t) {_log.log(Level.SEVERE, "deletedMe()", t); }
		}
		if (getTransactionRequester() != null)
		{
			// deals with sudden exit in the middle of transaction
			setTransactionRequester(null);
		}
        
		if (isGM()) 
        {
			try { GmListTable.getInstance().deleteGm(this); } catch (Throwable t) {_log.log(Level.SEVERE, "deletedMe()", t); }
        }
		try { getInventory().deleteMe(); } catch (Throwable t) {_log.log(Level.SEVERE, "deletedMe()", t); }
        
		try { removeAllKnownObjects(); } catch (Throwable t) {_log.log(Level.SEVERE, "deletedMe()", t); }
		try { setNetConnection(null); } catch (Throwable t) {_log.log(Level.SEVERE, "deletedMe()", t); }
		L2World.getInstance().removeObject(this);
	}

    public float getExpertisePenalty(int grade)
    {
		if (expertiseIndex >= grade)
        return 1;
		int d = EXPERTISE_LEVELS[grade] - getLevel();
		int dL = EXPERTISE_LEVELS[grade] - EXPERTISE_LEVELS[grade-1];
		return 1.2f + 0.8f * d / dL;
    }
    
    public float getWeaponExpertisePenalty()
    {
		L2Weapon wpn = getActiveWeaponItem();
		if (wpn == null)
			return 1.f;
		int grade = wpn.getItemGrade();
		if (grade <= 0)
			return 1.f;
		float penalty = getExpertisePenalty(grade);
            return penalty;
        }
        
	private float addPenalty(float scale, int ppd) {
		L2ItemInstance item = getInventory().getPaperdollItem(ppd);
		if (item == null)
			return 0.f;
		int grade = item.getItem().getItemGrade();
		if (grade <= 0)
			return 0.f;
		return scale*(getExpertisePenalty(grade) - 1.f);
    }
	
    public float getArmourExpertisePenalty()
    {
		float penalty = 1.f;
		penalty += addPenalty(0.2f, Inventory.PAPERDOLL_HEAD);
		penalty += addPenalty(0.2f, Inventory.PAPERDOLL_GLOVES);
		penalty += addPenalty(0.2f, Inventory.PAPERDOLL_FEET);
		penalty += addPenalty(0.5f, Inventory.PAPERDOLL_LEGS);
		penalty += addPenalty(0.8f, Inventory.PAPERDOLL_CHEST);
		return penalty;
    }
	
	public void setTradeList(L2TradeList x)
	{
		_tradeList = x;
	}

	public L2TradeList getTradeList()
	{
		return _tradeList;
	}
	public void setSellList(ArrayList<TradeItem> x)
	{
		_sellList = x;
	}

	public ArrayList<TradeItem> getSellList()
	{
		return _sellList;
	}


	public L2ManufactureList getCreateList()
	{
		return _createList;
	}
    
    public void setCreateList(L2ManufactureList x)
    {
        _createList = x;
    }
	public void setBuyList(ArrayList<TradeItem> x)
	{
		_buyList = x;
	}

	public ArrayList<TradeItem> getBuyList()
	{
		return _buyList;
	}
	public void setPrivateStoreType(int type)
	{
		_privatestore = type;
	}
	public int getPrivateStoreType()//sellmanage=2,sellset=1,buymanage=4,buyset=3
	{
		return _privatestore;
	}

	public void setSkillLearningClassId(ClassId classId)
	{
		_skillLearningClassId = classId;
	}
	public ClassId getSkillLearningClassId()
	{
		return _skillLearningClassId;
	}
	
	/**
	 * @param clan
	 */
	public void setClan(L2Clan clan)
	{
		_clan = clan;
        if (clan == null) {
            _clanId = 0;
			_clanLeader = false;
			return;
        }
        if (!clan.isMember(getName())) {
            // char has been kicked from clan
            setClan(null);
            setTitle("");
			return;
        }
        _clanId = clan.getClanId();
		_clanLeader = getObjectId() == clan.getLeaderId();
        setTitle("");
	}

	public L2Clan getClan()
	{
		return _clan;
	}

	public boolean isClanLeader()
	{
		return _clanLeader;
	}

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
	
	protected boolean checkAndEquipArrows()
	{
		if (getInventory().getPaperdollItem(Inventory.PAPERDOLL_LHAND) == null)
		{
			_arrowItem = getInventory().findArrowForBow(getActiveWeaponItem());
			
			if ( _arrowItem != null)
			{
				getInventory().setPaperdollItem(Inventory.PAPERDOLL_LHAND, _arrowItem);
				ItemList il = new ItemList(this, false);
				sendPacket(il);
			}
		}
		else
		{
			_arrowItem = getInventory().getPaperdollItem(Inventory.PAPERDOLL_LHAND);
		}
		
		return _arrowItem != null;
	}
	
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
	
	//invulnerability
	public void setIsInvul(boolean b)
	{
	 	_isInvul = b;
	}
	
	public boolean isInvul()
	{
		return _isInvul;
	}
	
	/**
	 * true if player has a party
	 * @return
	 */
	public boolean isInParty() 
	{
		return _party != null;
	}
	
	/**
	 * set players party without joining it (good for creating)
	 * @param party
	 */
	public void setParty(L2Party party) 
	{
		_party = party;
	}
	
	/**
	 * sets & joins party
	 * @param party
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
	 * leave party
	 *
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
	 * returns players party
	 * @return
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
	public void setIsGM(boolean status) 
	{
		_isGm = status;
	}
	public boolean isGM()
	{
		return _isGm;
	}
	
	public void cancelCastMagic() 
	{
//		setCurrentState(L2Character.STATE_IDLE);
		getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
		enableAllSkills();
		MagicSkillCanceld msc = new MagicSkillCanceld(getObjectId());
		sendPacket(msc);
		broadcastPacket(msc);
	}
	
	public void setAccessLevel(int level) 
	{
		_accessLevel = level;
		if (_accessLevel > 0 || Config.EVERYBODY_HAS_ADMIN_RIGHTS)
		{
		    setIsGM((true));
		}
	}
	
	public int getAccessLevel() 
	{
		if (Config.EVERYBODY_HAS_ADMIN_RIGHTS)
			return 200;
		return _accessLevel;
	}
	
	public double getLevelMod()
	{
		return (100.0 - 11 + getLevel()) / 100.0;
	}
	
	public void updateStats()
	{
		super.updateStats();
		
		this.broadcastUserInfo();
		updateKarma(1);
	}
	
	public void setKarmaFlag(int id, int flag) {
		//send packet updates 
		StatusUpdate su = new StatusUpdate(getObjectId()); 
		su.addAttribute(StatusUpdate.KARMA, _karma); 
		su.addAttribute(StatusUpdate.PVP_FLAG, flag); 
		sendPacket(su); 
		broadcastPacket(su);
	}
	
	public void updateKarma(int id)
	{
		StatusUpdate su = new StatusUpdate(getObjectId()); 
		su.addAttribute(StatusUpdate.KARMA, _karma);
		sendPacket(su); 
		broadcastPacket(su);
	}
    
    public void setOnlineStatus(boolean isOnline)
    {
        _isOnline   = isOnline;
        
        if (isOnline)
        {
            GmListTable.getInstance().addGm(this);
        }
        else
        {
            GmListTable.getInstance().deleteGm(this);
        }
        
        updateOnlineStatus();
    }
    
    public void updateOnlineStatus()
    {
        GmListTable.getInstance().deleteGm(this);
        java.sql.Connection con = null;
        try
        {
            con = L2DatabaseFactory.getInstance().getConnection();
            PreparedStatement statement = con.prepareStatement("UPDATE characters SET online=?, lastAccess=? WHERE obj_id=?");
            statement.setInt(1, isOnline());
            statement.setLong(2, System.currentTimeMillis());
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
	 * 
	 * Increases Karma.
	 * 
	 * @param i
	 */
	public void increaseKarma(int i) {
		_karma += i;
		updateKarma(getCharId());
	}
	
	/**
	 * 
	 * Decreases Karma.
	 * 
	 * @param i
	 */
	public void redreaseKarma(int i) {
		_karma -= i;
		updateKarma(getCharId());
	}
	
	
	private boolean createDb() {
        java.sql.Connection con = null;
        try
        {
            con = L2DatabaseFactory.getInstance().getConnection();
            PreparedStatement statement;
            statement = con.prepareStatement(
                    "INSERT INTO characters " +
                    "(account_name,obj_Id,char_name,level,maxHp,curHp,maxMp,curMp," +
                    "acc,crit,evasion,mAtk,mDef,mSpd,pAtk,pDef,pSpd,runSpd,walkSpd," +
                    "str,con,dex,_int,men,wit,face,hairStyle,hairColor,sex," +
                    "movement_multiplier,attack_speed_multiplier,colRad,colHeight," +
                    "exp,sp,karma,pvpkills,pkkills,clanid,maxload,race,classid,deletetime," +
                    "cancraft,title,allyId,accesslevel,online) " +
                    "values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
            statement.setString(1, _accountName);
            statement.setInt(2, getObjectId());
            statement.setString(3, getName());
            statement.setInt(4, getLevel());
            statement.setInt(5, getMaxHp());
            statement.setDouble(6, getCurrentHp());
            statement.setInt(7, getMaxMp());
            statement.setDouble(8, getCurrentMp());
            statement.setInt(9, getAccuracy());
            statement.setInt(10, getCriticalHit(null, null));
            statement.setInt(11, getEvasionRate(null));
            statement.setInt(12, getMAtk(null, null));
            statement.setInt(13, getMDef(null, null));
            statement.setInt(14, getMAtkSpd());
            statement.setInt(15, getPAtk(null));
            statement.setInt(16, getPDef(null));
            statement.setInt(17, getPAtkSpd());
            statement.setInt(18, getRunSpeed());
            statement.setInt(19, getWalkSpeed());
            statement.setInt(20, getSTR());
            statement.setInt(21, getCON());
            statement.setInt(22, getDEX());
            statement.setInt(23, getINT());
            statement.setInt(24, getMEN());
            statement.setInt(25, getWIT());
            statement.setInt(26, getFace());
            statement.setInt(27, getHairStyle());
            statement.setInt(28, getHairColor());
            statement.setInt(29, getSex());
            statement.setDouble(30, 1/*getMovementMultiplier()*/);
            statement.setDouble(31, 1/*getAttackSpeedMultiplier()*/);
            statement.setDouble(32, getTemplate().collisionRadius/*getCollisionRadius()*/);
            statement.setDouble(33, getTemplate().collisionHeight/*getCollisionHeight()*/);
            statement.setInt(34, getExp());
            statement.setInt(35, getSp());
            statement.setInt(36, getKarma());
            statement.setInt(37, getPvpKills());
            statement.setInt(38, getPkKills());
            statement.setInt(39, getClanId());
            statement.setInt(40, getMaxLoad());
            statement.setInt(41, getRace().ordinal());
            statement.setInt(42, getClassId().getId());
            statement.setInt(43, getDeleteTimer());
            statement.setInt(44, getCanCraft() ? 1 : 0);
            statement.setString(45, getTitle());
            statement.setInt(46, getAllyId());
            statement.setInt(47, getAccessLevel());
            statement.setInt(48, isOnline());

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
     * @param line
     * @return
     */
    private static L2PcInstance restore(int objectId)
    {
    	L2PcInstance player = null;
        java.sql.Connection con = null;
        try
        {
            con = L2DatabaseFactory.getInstance().getConnection();
            PreparedStatement statement = con.prepareStatement("SELECT * FROM characters WHERE obj_id=?");
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
                
            	player.setHeading(rset.getInt("heading"));
                
            	//player.setMovementMultiplier(rset.getDouble("movement_multiplier"));
            	//player.setAttackSpeedMultiplier(rset.getDouble("attack_speed_multiplier"));
            	//player.setCollisionRadius(rset.getDouble("colRad"));
            	//player.setCollisionHeight(rset.getDouble("colHeight"));
                
                int exp = rset.getInt("exp");
                
                if (player._level > 75)
                	player._level = 75;
                
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
				player.setClan(ClanTable.getInstance().getClan(rset.getInt("clanid")));
                player.setMaxLoad(rset.getInt("maxload"));
                //player.setRace(Race.values()[rset.getInt("race")]);
                player.setDeleteTimer(rset.getInt("deletetime"));
                //player.setCanCraft(rset.getInt("cancraft"));
                
                player.setTitle(rset.getString("title"));  
                //setCrestId(rset.getInt());
                player.setAllyId(rset.getInt("allyId"));
                player.setAccessLevel(rset.getInt("accesslevel"));
                player.setFistsWeaponItem(player.findFistsWeaponItem(classId));
                player.setUptime(System.currentTimeMillis());
                
            	player.setCurrentHp(rset.getDouble("curHp"));
            	player.setCurrentMp(rset.getDouble("curMp"));
                player.setRecomHave(rset.getInt("rec_have"));
                player.setRecomLeft(rset.getInt("rec_left"));
	            player.rewardSkills();
				
                L2World.getInstance().storeObject(player);
            	player.setXYZInvisible(rset.getInt("x"), rset.getInt("y"), rset.getInt("z"));
				
                break;
            }
            
            rset.close();
            statement.close();
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
    
    public void store()
    {
        java.sql.Connection con = null;
        try
        {
            con = L2DatabaseFactory.getInstance().getConnection();
            PreparedStatement statement;

            statement = con.prepareStatement(
                    "UPDATE characters SET " +
                    "level=?,maxHp=?,curHp=?,maxMp=?,curMp=?," +
                    "str=?,con=?,dex=?,_int=?,men=?,wit=?,face=?,hairStyle=?,hairColor=?," +
                    "heading=?,x=?,y=?,z=?,exp=?,sp=?,karma=?,pvpkills=?,pkkills=?," +
                    "rec_have=?,rec_left=?,clanid=?,maxload=?,race=?,classid=?,deletetime=?," +
                    "title=?,allyId=?,accesslevel=?,online=? " +
                    "WHERE obj_id=?");
            statement.setInt(1, getLevel());
            statement.setInt(2, getMaxHp());
            statement.setDouble(3, getCurrentHp());
            statement.setInt(4, getMaxMp());
            statement.setDouble(5, getCurrentMp());
            statement.setInt(6, getSTR());
            statement.setInt(7, getCON());
            statement.setInt(8, getDEX());
            statement.setInt(9, getINT());
            statement.setInt(10, getMEN());
            statement.setInt(11, getWIT());
            statement.setInt(12, getFace());
            statement.setInt(13, getHairStyle());
            statement.setInt(14, getHairColor());
            statement.setInt(15, getHeading());
            statement.setInt(16, getX());
            statement.setInt(17, getY());
            statement.setInt(18, getZ());
            statement.setInt(19, getExp());
            statement.setInt(20, getSp());
            statement.setInt(21, getKarma());
            statement.setInt(22, getPvpKills());
            statement.setInt(23, getPkKills());
            statement.setInt(24, getRecomHave());
            statement.setInt(25, getRecomLeft());
            statement.setInt(26, getClanId());
            statement.setInt(27, getMaxLoad());
            statement.setInt(28, getRace().ordinal());
            statement.setInt(29, getClassId().getId());
            statement.setInt(30, getDeleteTimer());
            statement.setString(31, getTitle());
            statement.setInt(32, getAllyId());
            statement.setInt(33, getAccessLevel());
            statement.setInt(34, isOnline());

            statement.setInt(35, getObjectId());

            statement.execute();
            statement.close();
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
    
    public int isOnline()
    {
        return (_isOnline ? 1 : 0);
    }
    
    /* (non-Javadoc)
     * @see net.sf.l2j.gameserver.model.L2Character#addSkill(net.sf.l2j.gameserver.model.L2Skill)
     */
    public L2Skill addSkill(L2Skill newSkill)
    {
        L2Skill oldSkill = super.addSkill(newSkill);
        storeSkill(newSkill, oldSkill);
        return oldSkill;
    }
    
    /* (non-Javadoc)
     * @see net.sf.l2j.gameserver.model.L2Character#removeSkill(net.sf.l2j.gameserver.model.L2Skill)
     */
    public L2Skill removeSkill(L2Skill skill)
    {
        L2Skill oldSkill = super.removeSkill(skill);
        java.sql.Connection con = null;
        try
        {
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
    
    
    private void restoreSkills()
    {
        java.sql.Connection con = null;
        try
        {
            con = L2DatabaseFactory.getInstance().getConnection();
            PreparedStatement statement = con.prepareStatement(GET_ALL_SKILLS_FOR_CHAR);
            statement.setInt(1, getObjectId());
            ResultSet rset = statement.executeQuery();
            
            while (rset.next())
            {
                int id = rset.getInt(COLUMN_SKILL_ID);
                int level = rset.getInt(COLUMN_SKILL_LEVEL);
				if (id > 9000)
					continue; // fake skills for base stats
                L2Skill skill = SkillTable.getInstance().getInfo(id, level);
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

    /* (non-Javadoc)
     * @see net.sf.l2j.gameserver.model.L2Character#isAttackable()
     */
    public boolean isAutoAttackable(L2Character attacker)
    {
		if (attacker == this || attacker == this.getPet())
			return false;
		// TODO: check for friendly mobs
		if (attacker instanceof L2MonsterInstance)
			return true;
		if (getParty() != null && getParty().getPartyMembers().contains(attacker))
			return false;
		if (getKarma() > 0)
			return true;
        return false;
    }
    
    
    /**
     * @param skill skill to use
     * @param forceUse used to force attack on players
     * @param dontMove used to prevent movement, if not in range
     */
    public void useMagic(L2Skill skill, boolean forceUse, boolean dontMove)
    {
		if (skill.isToggle())
		{
			L2Effect effect = getEffect(skill);
			if (effect != null)
			{
				effect.exit();
                ActionFailed af = new ActionFailed();
                sendPacket(af);
				return;
			}
		}
		
        // check if this is offensive magic
        if (skill.isOffensive())
        {
            L2Object target = getTarget();
            
            if ( withinZones(L2World.zones) ||
                 (target != null && target.withinZones(L2World.zones) ) 
            )
            {
                sendPacket(new SystemMessage(SystemMessage.TARGET_IN_PEACEZONE));
                ActionFailed af = new ActionFailed();
                sendPacket(af);
                return;
            }

            // forced attack is needed on non-attackable targets
            if (target != null && !target.isAutoAttackable(this) && !forceUse &&
            		skill.getTargetType() != SkillTargetType.TARGET_AURA &&
            		skill.getTargetType() != SkillTargetType.TARGET_AURA_CLOSE)
            {
                ActionFailed af = new ActionFailed();
                sendPacket(af);
                return;
            }

			if (target != null && dontMove)
			{
			    double distance = getDistance(target.getX(), target.getY());
			    if (skill.getCastRange() > 0 && distance > skill.getCastRange())
			    {
	                SystemMessage sm = new SystemMessage(614);
	                sm.addString("Target too far.");
	                sendPacket(sm);
	                ActionFailed af = new ActionFailed();
	                sendPacket(af);
			        return;
			    }
			}
		}
		
		if (skill.getSkillType() == SkillType.SPOIL)
		{
            L2Object target = getTarget();
			if (target instanceof L2Attackable && ((L2Attackable)target).hasSpoil())
			{
	            SystemMessage sm = new SystemMessage(614);
	            sm.addString("Already spoiled.");
	            sendPacket(sm);
	            ActionFailed af = new ActionFailed();
	            sendPacket(af);
	            return;
			}
		}
		
        if (skill.getItemConsume() > 0)
        {
            L2ItemInstance requiredItems = getInventory().findItemByItemId(skill.getItemConsumeId());
            if (requiredItems == null || requiredItems.getCount() < skill.getItemConsume())
            {
                sendPacket(new SystemMessage(SystemMessage.NOT_ENOUGH_ITEMS));
                return;
            }
        }
		
		if (isSitting())
		{
            SystemMessage sm = new SystemMessage(614);
            sm.addString("You cannot use magic while sitting.");
            sendPacket(sm);
            ActionFailed af = new ActionFailed();
            sendPacket(af);
            return;
		}
        
		if (getCurrentMp() < skill.getMpConsume())
		{
			sendPacket(new SystemMessage(SystemMessage.NOT_ENOUGH_MP));
            ActionFailed af = new ActionFailed();
            sendPacket(af);
			return;
		}
		
		if (getCurrentHp() < skill.getHpConsume())
		{
			sendPacket(new SystemMessage(SystemMessage.NOT_ENOUGH_HP));
            ActionFailed af = new ActionFailed();
            sendPacket(af);
			return;
		}

		if (isAllSkillsDisabled())
		{
			if (Config.DEBUG) _log.fine("currently casting or sleeping, skill disabled");
            ActionFailed af = new ActionFailed();
            sendPacket(af);
			return;
		}
		
		if (isSkillDisabled(skill.getId()))
		{
			if (Config.DEBUG) _log.fine("skill disabled");
            ActionFailed af = new ActionFailed();
            sendPacket(af);
			return;
		}
		
		if(!skill.getWeaponDependancy(this))
		{
            ActionFailed af = new ActionFailed();
            sendPacket(af);
			return;
		}
		
		if (skill.isPassive())
		{
			// just ignore the passive skill request. why does the client send it anyway ??
            ActionFailed af = new ActionFailed();
            sendPacket(af);
			return;
		}
		
		if(!skill.checkCondition(this))
		{
            ActionFailed af = new ActionFailed();
            sendPacket(af);
			return;
		}
		
        super.useMagic(skill);
    }
	
    /* (non-Javadoc)
     * @see net.sf.l2j.gameserver.model.L2Character#consumeItem(int, int)
     */
    public void consumeItem(int itemConsumeId, int itemCount)
    {
        if (itemConsumeId != 0 && itemCount != 0)
        {
            L2ItemInstance item = getInventory().destroyItemByItemId(itemConsumeId, itemCount);
            InventoryUpdate update = new InventoryUpdate();
            update.addItem(item);
            sendPacket(update);
        }
    }
    
    public boolean isMageClass()
    {
    	L2CharTemplate template = CharTemplateTable.getInstance().getTemplate(getClassId(), getSex()!=0); 
    	if(template.baseMAtk > 3)
    	{
    		return true;
    	}
    	
    	return false;
    }
    
    /**
     * @return
     */
    public boolean isMounted()
    {
        return _mountType > 0;
    }
    
    /**
     * @param mountType The mountType to set.
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
        
        // Update speed on client side
        UserInfo ui = new UserInfo(this);
        sendPacket(ui); 
    }

    /**
     */
    public int getMountType()
    {
        return _mountType;
    }
    
	public void updateAbnormalEffect()
        {
		broadcastUserInfo();
    }
    
    public void tempInvetoryDisable()
    {
        _inventoryDisable  = true;
        
        ClientScheduler.getInstance().scheduleMed(new InventoryEnable(), 1500);
    }

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
    public void addCubic(int id, int level)
    {
        L2CubicInstance cubic = new L2CubicInstance(this, id, level);
        _cubics.put(id, cubic);
    }
    public void delCubic(int id)
    {
        _cubics.remove(id);
    }
    public L2CubicInstance getCubic(int id)
    {
        return _cubics.get(id);
    }
    
    public String toString()
    {
    	return "player "+getName();
    }
	
	public int getEnchantEffect()
	{
		L2ItemInstance wpn = getActiveWeaponInstance(); 
		if (wpn == null)
			return 0;
		return Math.min(127, wpn.getEnchantLevel());
	}
	
	public void setLastFolkNPC(L2FolkInstance folkNpc)
	{
		_lastFolkNpc = folkNpc;
	}
	public L2FolkInstance getLastFolkNPC()
	{
		return _lastFolkNpc;
	}

	public boolean isInsidePvPZone()
	{
		boolean inArena = withinZones(L2World.arenas);
		
		return inArena;
	}
	
	public void checkPvP(L2Character target)
	{
		if (target instanceof L2PcInstance && !isInsidePvPZone()) 
		{
			_log.fine("Attacker is a player checking for PvPFlag()");
			if (getPvpFlag() == 0)
			{
				startPvPFlag();
				_log.fine("setting attackers PvPFlag on");
			}
			if (getPvpFlag() != 0) 
				//update last pvp attack controller 
				setlastPvpAttack (System.currentTimeMillis()); 
		} 
	}
}
