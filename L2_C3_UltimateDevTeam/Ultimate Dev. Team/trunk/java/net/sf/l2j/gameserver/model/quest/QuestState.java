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
package net.sf.l2j.gameserver.model.quest;

import javolution.util.FastList;
import javolution.util.FastMap;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.GameTimeController;
import net.sf.l2j.gameserver.cache.HtmCache;
import net.sf.l2j.gameserver.lib.Log;
import net.sf.l2j.gameserver.lib.Rnd;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2DropData;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2NpcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.packets.serverpackets.*;
import net.sf.l2j.gameserver.skills.Stats;
import net.sf.l2j.gameserver.tables.ItemTable;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * @author Luis Arias
 */
public final class QuestState
{
	protected static Logger _log = Logger.getLogger(Quest.class.getName());

	/** Quest associated to the QuestState */
	private final Quest quest;
	
	/** Player who engaged the quest */
	private final L2PcInstance player;
	
	/** State of the quest */
	private State state;
	
	/** Boolean representing the completion of the quest */
	private boolean completed;
	
	/** List of couples (variable for quest,value of the variable for quest) */
	private Map<String, String> vars;
	
	/** List of drops needed for quest according to the mob */
	private Map<Integer, List<L2DropData>> drops;
	
    /** List of timer for quest */
    private List<QuestTimer> _QuestTimers;
    
    /** Boolean flag letting QuestStateManager know to exit quest when cleaning up */
    private boolean _IsExitQuestOnCleanUp = false;

    /**
	 * Constructor of the QuestState : save the quest in the list of quests of the player.<BR/><BR/>
	 * 
	 * <U><I>Actions :</U></I><BR/>
	 * <LI>Save informations in the object QuestState created (Quest, Player, Completion, State)</LI>
	 * <LI>Add the QuestState in the player's list of quests by using setQuestState()</LI>
	 * <LI>Add drops gotten by the quest</LI>
	 * <BR/>
	 * @param quest : quest associated with the QuestState
	 * @param player : L2PcInstance pointing out the player  
	 * @param state : state of the quest
	 * @param completed : boolean for completion of the quest
	 */
    QuestState(Quest quest, L2PcInstance player, State state, boolean completed)
    {
		this.quest = quest;
		this.player = player;
		
		// Save the state of the quest for the player in the player's list of quest onwed
		this.player.setQuestState(this);
		
		this.completed = completed;
		// set the state of the quest
		this.state = state;
		
		// add drops from state of the quest
		if (state != null && !completed) {
			Map<Integer, List<L2DropData>> new_drops = state.getDrops();
			if (new_drops != null) {
				drops = new FastMap<Integer, List<L2DropData>>();
				drops.putAll(new_drops);
			}
		}
    }
	
    /**
     * Return the quest
     * @return Quest
     */
	public Quest getQuest() {
		return quest;
	}
	
	/**
	 * Return the L2PcInstance
	 * @return L2PcInstance
	 */
	public L2PcInstance getPlayer() {
		return player;
	}
	
	/**
	 * Return the state of the quest
	 * @return State
	 */
	public State getState() {
		return state;
	}
	
	/**
	 * Return list of drops needed for the quest in concordance with mobs
	 * @return FastMap
	 */
	public Map<Integer, List<L2DropData>> getDrops()
	{
		return drops;
	}
	
	/**
	 * Return true if quest completed, false otherwise
	 * @return boolean
	 */
	public boolean isCompleted() {
		return completed;
	}

	/**
	 * Return true if quest started, false otherwise
	 * @return boolean
	 */
	public boolean isStarted() {
		if (getStateId().equals("Start") || getStateId().equals("Completed")) 
		{
		return false;
		}
		return true;
	}
	
	/**
	 * Return state of the quest after its initialization.<BR><BR>
	 * <U><I>Actions :</I></U>
	 * <LI>Remove drops from previous state</LI>
	 * <LI>Set new state of the quest</LI>
	 * <LI>Add drop for new state</LI>
	 * <LI>Update information in database</LI>
	 * <LI>Send packet QuestList to client</LI>
	 * @param state
	 * @return object
	 */
	public Object setState(State state) {
		// remove drops from previous state
		if (drops != null) {
			for (Iterator<List<L2DropData>> i = drops.values().iterator(); i.hasNext(); ) {
				List<L2DropData> lst = i.next();
				for (Iterator<L2DropData> ds = lst.iterator(); ds.hasNext(); ) {
					L2DropData d = ds.next();
					String[] states = d.getStateIDs();
					for (int k=0; k < states.length; k++) {
						if (this.state.getName().equals(states[k])) {
							ds.remove();
							break;
						}
					}
				}
				if (lst == null || lst.size() == 0)
					i.remove();
			}
		}
		// set new state
		this.state = state;
		// add drops from new state
		if (state != null && !completed) {
			Map<Integer, List<L2DropData>> new_drops = state.getDrops();
			if (new_drops != null) {
				if (drops == null)
					drops = new FastMap<Integer, List<L2DropData>>();
				drops.putAll(new_drops);
			}
		}
		Quest.updateQuestInDb(this);
		player.sendPacket(new QuestList());
		return state;
	}
	
	/**
	 * Return ID of the state of the quest
	 * @return String
	 */
	public String getStateId() {
		return state.getName();
	}
	
	/**
	 * Add parameter used in quests.
	 * @param var : String pointing out the name of the variable for quest
	 * @param val : String pointing out the value of the variable for quest 
	 * @return String (equal to parameter "val")
	 */
	String setInternal(String var, String val) {
		if (vars == null)
			vars = new FastMap<String, String>();
		if (val == null)
			val = "";
		vars.put(var, val);
		return val;
	}

	/**
	 * Return value of parameter "val" after adding the couple (var,val) in class variable "vars".<BR><BR>
	 * <U><I>Actions :</I></U><BR>
	 * <LI>Initialize class variable "vars" if is null</LI>
	 * <LI>Initialize parameter "val" if is null</LI>
	 * <LI>Add/Update couple (var,val) in class variable FastMap "vars"</LI>
	 * <LI>If the key represented by "var" exists in FastMap "vars", the couple (var,val) is updated in the database. The key is known as
	 * existing if the preceding value of the key (given as result of function put()) is not null.<BR>
	 * If the key doesn't exist, the couple is added/created in the database</LI>
	 * @param var : String indicating the name of the variable for quest
	 * @param val : String indicating the value of the variable for quest
	 * @return String (equal to parameter "val")
	 */
	public String set(String var, String val) {
		if (vars == null)
			vars = new FastMap<String, String>();
		if (val == null)
			val = "";
		// FastMap.put() returns previous value associated with specified key, or null if there was no mapping for key. 
		String old = vars.put(var, val);
		if (old != null)
			Quest.updateQuestVarInDb(this, var, val);
		else
			Quest.createQuestVarInDb(this, var, val);
		return val;
	}

	/**
	 * Remove the variable of quest from the list of variables for the quest.<BR><BR>
	 * <U><I>Concept : </I></U>
	 * Remove the variable of quest represented by "var" from the class variable FastMap "vars" and from the database.
	 * @param var : String designating the variable for the quest to be deleted
	 * @return String pointing out the previous value associated with the variable "var"
	 */
	public String unset(String var) {
		if (vars == null)
			return null;
		String old = vars.remove(var);
		if (old != null)
			Quest.deleteQuestVarInDb(this, var);
		return old;
	}

	/**
	 * Return the value of the variable of quest represented by "var"
	 * @param var : name of the variable of quest
	 * @return Object
	 */
	public Object get(String var) {
		if (vars == null)
			return null;
		return vars.get(var);
	}

	/**
	 * Return the value of the variable of quest represented by "var"
	 * @param var : String designating the variable for the quest
	 * @return int
	 */
    public int getInt(String var) {
        int varint = 0;
        try
        {
            varint = Integer.parseInt(vars.get(var));
        } catch (Exception e)
        {
            _log.finer(getPlayer().getName()+": variable "+var+" isn't an integer: " + varint + e);
//	    if (Config.AUTODELETE_INVALID_QUEST_DATA)
//		exitQuest(true);
        }
        return varint;
    }
    
    /**
     * Return true if ID of the L2Attackable attcked is needed for the quest, otherwise, return false.
     * @param npcId
     * @return boolean
     */
    public boolean waitsForAttack(L2NpcInstance npc) {
        for (int k : state.getAttackIds()) {
            if (k == npc.getNpcId()) {
                return true;
            }
        }
        return false;
    }

    
	public boolean waitsForEvent(String event) {
		for (String se : state.getEvents()) {
			if (se.equals(event)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Return true if ID of the L2Attackable killed is needed for the quest, otherwise, return false.
	 * @param npcId
	 * @return boolean
	 */
	public boolean waitsForKill(L2NpcInstance npc) {
		for (int k : state.getKillIds()) {
			if (k == npc.getNpcId()) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Return true if the L2NpcInstance has to be talked to 
	 * @param npcId
	 * @return boolean
	 */
	public boolean waitsForTalk(int npcId) {
		for (int k : state.getTalkIds()) {
			if (k == npcId) {
				return true;
			}
		}
		return false;
	}

    /**
     * Add player to get notification of characters death
     * @param character : L2Character of the character to get notification of death
     */
    public void addNotifyOfDeath(L2Character character)
    {
        if (character == null)
            return;
        character.addNotifyQuestOfDeath(this);
    }
	
	/**
	 * Add drop to the list of drops for the quest
	 * @param npcId : int pointing out the NPC given the drop
	 * @param itemId : int pointing out the ID of the item dropped
	 * @param chance : int pointing out the chance to get the drop
	 */
	public void addQuestDrop(int npcId, int itemId, int chance) {
		if (drops == null)
			drops = new FastMap<Integer, List<L2DropData>>();
		L2DropData d = new L2DropData();
		d.setItemId(itemId);
		d.setChance(chance);
		d.setQuestID(quest.name);
		d.addStates(new String[]{state.getName()});
		List<L2DropData> lst = drops.get(npcId);
		if (lst != null) {
			lst.add(d);
		} else {
			lst = new FastList<L2DropData>();
			lst.add(d);
			drops.put(npcId, lst);
		}
	}

	/**
	 * Clear all drops from the class variable "drops"
	 */
	public void clearQuestDrops() {
		drops = null;
	}

	/**
	 * Add quest drops to the parameter "drops"
	 * @param npc : L2Attackable killed
	 * @param drops : List of drops of the L2Attackable
	 */
	public void fillQuestDrops(L2NpcInstance npc, List<L2DropData> drops)
	{
		if (this.drops == null)
			return;
		// Get drops of the NPC recorded in class variable "drops"
		List<L2DropData> lst = this.drops.get(npc.getTemplate().npcId);
		// If drops of the NPC in class variable "drops" exist, add them to parameter "drops"
		if (lst != null)
			drops.addAll(lst);
		// Get drops for all NPC recorded in class variable "drops"
		lst = this.drops.get(0); // all mobs
		// If drops for all NPC in class variable "drops" exist, add them to parameter "drops"
		if (lst != null)
			drops.addAll(lst);
	}
	
	/**
	 * Return the quantity of one sort of item hold by the player
	 * @param itemId : ID of the item wanted to be count
	 * @return int
	 */
    public int getQuestItemsCount(int itemId) {
        int count = 0;
        for (L2ItemInstance item: player.getInventory().getItems())
        {
            if (item.getItemId() == itemId)
                count += item.getCount();
        }
        return count;
    }
    
    /**
     * Return the level of enchantment on the weapon of the player(Done specifically for weapon SA's)
     * @param itemId : ID of the item to check enchantment
     * @return int
     */
    public int getEnchantLevel(int itemId) {
        L2ItemInstance enchanteditem = player.getInventory().getItemByItemId(itemId);
        if (enchanteditem == null)
            return 0;
        return enchanteditem.getEnchantLevel();
    }
    
    /**
     * Set the level of enchantment on the weapon of the player(Done specifically for weapon SA's)
     * @param itemId : ID of the item to enchant
     * @param level : level of enchantment we want to add
     */
//    public void setEnchantLevel(int itemId, int level) {
        // Try to find the first one starting from the end first
        // If we don't then the current item that exist will get it's enchant
        // level changed while the newly added one doesn't get any enchant level 

//        L2ItemInstance item;
//        for (int i = player.getInventory().getItems().length - 1; i == 0; i--)
//        {
//            item = player.getInventory().getItems()[i];
//            if (item.getItemId() == itemId)
//            {
//                item.setEnchantLevel(level);
//                return;
//            }
//        }
//    }	

    /**
	 * Give item/reward to the player
	 * @param itemId
	 * @param count
	 */
	public void giveItems(int itemId, int count) {
	    giveItems(itemId, count, 0);
	}
	public void giveItems(int itemId, int count, int enchantlevel) {
		//if (count <= 0 && itemId != 57)
		//	count = 1;
		if (count <= 0)
		    return;
		// Create new instance for the item
		L2ItemInstance item = ItemTable.getInstance().createItem(itemId);
		if (item == null)
			return;
		if (enchantlevel > 0)
            	    item.setEnchantLevel(enchantlevel);
		
		// If item for reward is gold (ID=57), modify count with rate for quest reward
		if (item.getItemId() == 57 
		    && !(getQuest().getQuestIntId()>=217 && getQuest().getQuestIntId()<=233)
		    && !(getQuest().getQuestIntId()>=401 && getQuest().getQuestIntId()<=418)
		    ) 
			count=(int)(count*Config.RATE_QUESTS_REWARD);
		// Set quantity of item
		item.setCount(count);
		if (Config.LOG_ITEMS) 
		{
		    if (player.isGM())
			Log.add(player.getName()+"("+player.getObjectId()+") gived from quest "+getQuest().getQuestIntId()+"("+getQuest().getName()+")"+item.getCount()+" "+item.getItem().getName()+"("+item.getItemId()+")", "items_quests_gm");
		    else
			Log.add(player.getName()+"("+player.getObjectId()+") gived from quest "+getQuest().getQuestIntId()+"("+getQuest().getName()+")"+item.getCount()+" "+item.getItem().getName()+"("+item.getItemId()+")", "items_quests");
		}
		// Add items to player's inventory
		player.getInventory().addItem(item);

		
		// If item for reward is gold, send message of gold reward to client
		if (item.getItemId() == 57) {
			player.sendPacket(new SystemMessage(SystemMessage.EARNED_ADENA).addNumber(count));
			player.sendPacket(new ItemList(player, false));
		} 
		// Otherwise, send message of object reward to client
		else {
			SystemMessage smsg = new SystemMessage(SystemMessage.EARNED_ITEM);
			smsg.addItemName(item.getItemId());
			smsg.addNumber(count);
			player.sendPacket(smsg);
			player.sendPacket(new ItemList(player, false));
		}
	}
//TODO: More radar functions need to be added when the radar class is complete.
// BEGIN STUFF THAT WILL PROBABLY BE CHANGED
	public void addRadar(int x, int y, int z){
	  player.radar.addMarker(x,y,z);
	}
	
	public void removeRadar(int x, int y, int z){
	  player.radar.removeMarker(x,y,z);
	}
	
	public void clearRadar(){
		  player.radar.removeAllMarkers();
	}
// END STUFF THAT WILL PROBABLY BE CHANGED
	


	/**
	 * Remove items from player's inventory when talking to NPC in order to have rewards.<BR><BR>
	 * <U><I>Actions :</I></U>
	 * <LI>Destroy quantity of items wanted</LI>
	 * <LI>Send new inventory list to player</LI>
	 * @param itemId : Identifier of the item
	 * @param count : Quantity of items to destroy 
	 */
	public void takeItems(int itemId, int count) {
		// Get object item from player's inventory list
		L2ItemInstance item = player.getInventory().getItemByItemId(itemId);
		if (item == null)
			return;
		// Tests on count value in order not to have negative value
		if (count < 0 || count > item.getCount())
			count = item.getCount();
		
		// Destroy the quantity of items wanted
		if (Config.LOG_ITEMS) 
		{
		    if (player.isGM())
			Log.add(player.getName()+"("+player.getObjectId()+") taked from quest "+getQuest().getQuestIntId()+"("+getQuest().getName()+")"+item.getCount()+" "+item.getItem().getName()+"("+item.getItemId()+")", "items_quests_gm");
		    else
			Log.add(player.getName()+"("+player.getObjectId()+") taked from quest "+getQuest().getQuestIntId()+"("+getQuest().getName()+")"+item.getCount()+" "+item.getItem().getName()+"("+item.getItemId()+")", "items_quests");
		}
		player.getInventory().destroyItemByItemId(itemId, count);
		// Send message of destruction to client
		if (itemId == 57)
		    player.sendPacket(new SystemMessage(SystemMessage.DISSAPEARED_ADENA).addNumber(count));
		else
		    player.sendPacket(new SystemMessage(SystemMessage.DISSAPEARED_ITEM).addItemName(itemId));

		// Create and send new inventory list to client
		player.sendPacket(new ItemList(player, false));
	}
	
	/**
	 * Send a packet in order to play sound at client terminal
	 * @param sound
	 */
	public void playSound(String sound) {
		player.sendPacket(new PlaySound(sound));
	}

	/**
	 * Add XP and SP as quest reward
	 * @param exp
	 * @param sp
	 */
	public void addExpAndSp(int exp, int sp) {
	    player.addExpAndSp((int)player.calcStat(Stats.EXPSP_RATE, exp * Config.RATE_QUESTS_REWARD, null, null), 
                           (int)player.calcStat(Stats.EXPSP_RATE, sp * Config.RATE_QUESTS_REWARD, null, null));
	}
	
	/**
	 * Return random value
	 * @param max : max value for randomisation
	 * @return int
	 */
	public int getRandom(int max) {
		return Rnd.get(max);
	}
	
	/**
	 * Return number of ticks from GameTimeController
	 * @return int
	 */
	public int getItemEquipped(int loc) {
		int item=player.getInventory().getPaperdollItemId(loc);
		return item;
	}
	
	/**
	 * Return the number of ticks from the GameTimeController
	 * @return int
	 */
	public int getGameTicks() {
		return GameTimeController.getGameTicks();
	}
    
    /**
     * Return true if quest is to exited on clean up by QuestStateManager
     * @return boolean
     */
    public final boolean isExitQuestOnCleanUp() {
        return _IsExitQuestOnCleanUp;
    }
    
    /**
     * Return the QuestTimer object with the specified name
     * @return QuestTimer<BR> Return null if name does not exist
     */
    public void setIsExitQuestOnCleanUp(boolean isExitQuestOnCleanUp)
    {
        _IsExitQuestOnCleanUp = isExitQuestOnCleanUp;
    }
    
    /**
     * Start a timer for quest.<BR><BR>
     * @param name<BR> The name of the timer. Will also be the value for event of onEvent
     * @param time<BR> The milisecond value the timer will elapse
     */
    public void startQuestTimer(String name, long time)
    {
        // Add quest timer if timer doesn't already exist
        if (getQuestTimer(name) == null)
            _QuestTimers.add(new QuestTimer(this, name, time));
    }
    
    /**
     * Return the QuestTimer object with the specified name
     * @return QuestTimer<BR> Return null if name does not exist
     */
    public final QuestTimer getQuestTimer(String name)
    {
        for (int i = 0; i < getQuestTimers().size(); i++)
        {
            if (getQuestTimers().get(i).getName() == name)
                return  getQuestTimers().get(i);
        }
        return null;
    }
    
    /**
     * Return a list of QuestTimer
     * @return FastList<QuestTimer>
     */
    public final List<QuestTimer> getQuestTimers()
    {
        if (_QuestTimers == null)
            _QuestTimers = new FastList<QuestTimer>();
        return _QuestTimers;
    }
    
    /**
     * Return a QuestPcSpawn for curren player instance
     */
    public final QuestPcSpawn getPcSpawn()
    {
        return QuestPcSpawnManager.getInstance().getPcSpawn(this.getPlayer());
    }

	/**
	 * Show HTML file to client
	 * @param fileName
	 * @return String : message sent to client 
	 */
	public String showHtmlFile(String fileName) {
        String questId = getQuest().getName();

        //Create handler to file linked to the quest
        String directory    = getQuest().getDescr().toLowerCase();
        String content = HtmCache.getInstance().getHtm("data/jscript/" + directory + "/" + questId + "/"+fileName);
        
        if (content == null)
        {
            content = HtmCache.getInstance().getHtmForce("data/jscript/quests/"+questId+"/"+fileName);
        }

        if(player != null && player.getTarget() != null)
        {
            content = content.replaceAll("%objectId%", String.valueOf(player.getTarget().getObjectId()));
        }

        //Send message to client if message not empty     
         if (content != null) 
         {
             NpcHtmlMessage npcReply = new NpcHtmlMessage(5);
             npcReply.setHtml(content);
             player.sendPacket(npcReply);
         }
         
         return content;
	}

	/**
	 * Destroy element used by quest when quest is exited
	 * @param repeatable
	 * @return QuestState
	 */
	public QuestState exitQuest(boolean repeatable) {
		if (completed)
			return this;
		// Say quest is completed
		completed = true;
		// Clean drops
		if (drops != null) {
			// Go through values of class variable "drops" pointing out mobs that drop for quest
		    for (Iterator<List<L2DropData>> i = drops.values().iterator(); i.hasNext(); ) {
		    	List<L2DropData> lst = i.next();
		    	// Go through values of mobs that drop for quest pointing out drops of the mob
		    	for (Iterator<L2DropData> ds = lst.iterator(); ds.hasNext(); ) {
					L2DropData d = ds.next();
					int itemId = d.getItemId();
					// Get [item from] / [presence of the item in] the inventory of the player 
					L2ItemInstance item = player.getInventory().getItemByItemId(itemId);
				    	if (item == null || itemId == 57)
						continue;
					int count = item.getCount();
					// If player has the item in inventory, destroy it (if not gold)
					player.getInventory().destroyItemByItemId(itemId, count);
				}
		    }
		    // Send new inventory list to player
		    player.sendPacket(new ItemList(player, false));
		    drops = null;
		}
		
		// If quest is repeatable, delete quest from list of quest of the player and from database (quest CAN be created again => repeatable)
		if (repeatable) {
			player.delQuestState(quest.name);
			Quest.deleteQuestInDb(this);
			vars = null;

		} else { // Otherwise, delete variables for quest and update database (quest CANNOT be created again => not repeatable)
			if (vars != null) {
				for (String var : vars.keySet().toArray(new String[vars.size()]))
					unset(var);
			}
			Quest.updateQuestInDb(this);

		}
		return this;
	}
	
	
}
