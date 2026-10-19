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

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Logger;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.ItemTable;
import net.sf.l2j.gameserver.lib.Rnd;
import net.sf.l2j.gameserver.model.L2DropData;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2NpcInstance;
import net.sf.l2j.gameserver.model.L2PcInstance;
import net.sf.l2j.gameserver.serverpackets.ItemList;
import net.sf.l2j.gameserver.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.serverpackets.PlaySound;
import net.sf.l2j.gameserver.serverpackets.QuestList;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;

/**
 * @author Luis Arias
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public final class QuestState
{
	protected static Logger _log = Logger.getLogger(Quest.class.getName());

	private final Quest quest;
	private final L2PcInstance player;
	private State state;
	private boolean completed;
	private HashMap<String, String> vars;
	private HashMap<Integer, ArrayList<L2DropData>> drops;
	
    QuestState(Quest quest, L2PcInstance player, State state, boolean completed)
    {
		this.quest = quest;
		this.player = player;
		this.player.setQuestState(this);
		this.completed = completed;
		// set new state
		this.state = state;
		// add drops from new state
		if (state != null && !completed) {
			HashMap<Integer, ArrayList<L2DropData>> new_drops = state.getDrops();
			if (new_drops != null) {
				drops = new HashMap<Integer, ArrayList<L2DropData>>();
				drops.putAll(new_drops);
			}
		}
    }
	
	public Quest getQuest() {
		return quest;
	}
	
	public L2PcInstance getPlayer() {
		return player;
	}
	
	public State getState() {
		return state;
	}
	
	public HashMap<Integer, ArrayList<L2DropData>> getDrops()
	{
		return drops;
	}
	
	public boolean isCompleted() {
		return completed;
	}
	
     public boolean isStarted() {  
        return getStateId().equals("Started");  
     } 	
	
	public Object setState(State state) {
		// remove drops from previous state
		if (drops != null) {
			for (Iterator<ArrayList<L2DropData>> i = drops.values().iterator(); i.hasNext(); ) {
				ArrayList<L2DropData> lst = i.next();
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
			HashMap<Integer, ArrayList<L2DropData>> new_drops = state.getDrops();
			if (new_drops != null) {
				if (drops == null)
					drops = new HashMap<Integer, ArrayList<L2DropData>>();
				drops.putAll(new_drops);
			}
		}
		Quest.updateQuestInDb(this);
		QuestList ql = new QuestList();
		player.sendPacket(ql);
		return state;
	}
	
	public String getStateId() {
		return state.getName();
	}
	
	String setInternal(String var, String val) {
		if (vars == null)
			vars = new HashMap<String, String>();
		if (val == null)
			val = "";
		vars.put(var, val);
		return val;
	}

	public String set(String var, String val) {
		if (vars == null)
			vars = new HashMap<String, String>();
		if (val == null)
			val = "";
		String old = vars.put(var, val);
		if (old != null)
			Quest.updateQuestVarInDb(this, var, val);
		else
			Quest.createQuestVarInDb(this, var, val);
		return val;
	}

	public String unset(String var) {
		if (vars == null)
			return null;
		String old = vars.remove(var);
		if (old != null)
			Quest.deleteQuestVarInDb(this, var);
		return old;
	}

	public Object get(String var) {
		if (vars == null)
			return null;
		return vars.get(var);
	}

	public boolean waitsForEvent(String event) {
		for (String se : state.getEvents()) {
			if (se.equals(event)) {
				return true;
			}
		}
		return false;
	}
	
	public boolean waitsForKill(int npcId) {
		for (int k : state.getKillIds()) {
			if (k == npcId) {
				return true;
			}
		}
		return false;
	}
	
	public boolean waitsForTalk(int npcId) {
		for (int k : state.getTalkIds()) {
			if (k == npcId) {
				return true;
			}
		}
		return false;
	}
	
	public void addQuestDrop(int npcId, int itemId, int chance) {
		//if (drops == null)
			drops = new HashMap<Integer, ArrayList<L2DropData>>();
		L2DropData d = new L2DropData();
		d.setItemId(itemId);
		d.setChance(chance);
		d.setQuestID(quest.name);
		d.addStates(new String[]{state.name});
		ArrayList<L2DropData> lst = drops.get(npcId);
		if (lst != null) {
			lst.add(d);
		} else {
			lst = new ArrayList<L2DropData>();
			lst.add(d);
			drops.put(npcId, lst);
		}
	}

	public void clearQuestDrops() {
		drops = null;
	}

	public void fillQuestDrops(L2NpcInstance npc, ArrayList<L2DropData> drops)
	{
		if (this.drops == null)
			return;
		ArrayList<L2DropData> lst = this.drops.get(npc.getTemplate().npcId);
		if (lst != null)
			drops.addAll(lst);
		lst = this.drops.get(0); // all mobs
		if (lst != null)
			drops.addAll(lst);
	}
	
	public int getQuestItemsCount(int itemId) {
		L2ItemInstance inst = player.getInventory().getItemByItemId(itemId);
		if (inst == null)
			return 0;
		return inst.getCount();
	}
	
	public void giveItems(int itemId, int count) {
		if (count <= 0)
			count = 1;
		L2ItemInstance item = ItemTable.getInstance().createItem(itemId);
		if (item == null)
			return;
		item.setCount(count);
		player.getInventory().addItem(item);
		if (item.getItemId() == 57) {
			SystemMessage smsg = new SystemMessage(SystemMessage.EARNED_ADENA);
			//smsg.addNumber(item.getCount());
			smsg.addNumber(count);
			player.sendPacket(smsg);
		} else {
			SystemMessage smsg = new SystemMessage(SystemMessage.EARNED_ITEM);
			smsg.addNumber(count);
			smsg.addItemName(item.getItemId());
			player.sendPacket(smsg);
			ItemList il = new ItemList(player, false);
			player.sendPacket(il);
		}
	}

	public void takeItems(int itemId, int count) {
		L2ItemInstance item = player.getInventory().getItemByItemId(itemId);
		if (item == null)
			return;
		if (count < 0 || count > item.getCount())
			count = item.getCount();
		player.getInventory().destroyItemByItemId(itemId, count);
		SystemMessage smsg = new SystemMessage(SystemMessage.DISSAPEARED_ITEM);
		smsg.addItemName(itemId);
		player.sendPacket(smsg);
		ItemList il = new ItemList(player, false);
		player.sendPacket(il);
	}
	
    public void playSound(String sound) {  
		player.sendPacket(new PlaySound(sound));  
	}
	
	public int getRandom(int max) {  
		return Rnd.get(max);  
	}	

	public String showHtmlFile(String file) {
		FileInputStream fis = null;
		String content = null;
		try
		{
			String questId = getQuest().getName();
			File f = new File(Config.DATAPACK_ROOT, "data/jscript/quests/"+questId+"/"+file);
			if (f.exists()) {
				if (Config.DEBUG)
					_log.fine("Showing quest window for quest "+questId+" html path: "+f);
				fis = new FileInputStream(f);
				byte[] raw = new byte[fis.available()];
				fis.read(raw);
				content = new String(raw, "UTF-8");
			} else {
				if (Config.DEBUG)
					_log.fine("File not exists for quest "+questId+" html path: "+f);
				content = null;
			}
			if (content != null) {
				NpcHtmlMessage npcReply = new NpcHtmlMessage(5);
				npcReply.setHtml(content);
				player.sendPacket(npcReply);
			}
		} catch (Exception e) {
			_log.warning("problem with npc text " + e);
		} finally {
			try { if (fis != null) fis.close(); } catch (Exception e1) {/* ignore this */ }
		}
		return content;
	}

	public QuestState exitQuest(boolean repeatable) {
		if (completed)
			return this;
		completed = true;
		drops = null;
         if (drops != null) {  
			 for (Iterator<ArrayList<L2DropData>> i = drops.values().iterator(); i.hasNext(); ) {  
				 ArrayList<L2DropData> lst = i.next();  
				 for (Iterator<L2DropData> ds = lst.iterator(); ds.hasNext(); ) {  
					 L2DropData d = ds.next();  
					 int itemId = d.getItemId();  
					 L2ItemInstance item = player.getInventory().getItemByItemId(itemId);  
					 if (item == null || itemId == 57)  
						 continue;  
					 int count = item.getCount();  
					 player.getInventory().destroyItemByItemId(itemId, count);  
				 }  
			 }  
			 ItemList il = new ItemList(player, false);  
			 player.sendPacket(il);  
			 drops = null;  
		 }  		
		if (repeatable) {
			player.delQuestState(quest.name);
			Quest.deleteQuestInDb(this);
			vars = null;
		} else {
			if (vars != null) {
				for (String var : vars.keySet().toArray(new String[vars.size()]))
					unset(var);
			}
			Quest.updateQuestInDb(this);
		}
		return this;
	}
	
	
}
