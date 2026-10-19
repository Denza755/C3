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

import java.util.ArrayList;
import java.util.HashMap;

import net.sf.l2j.gameserver.model.L2DropData;

/**
 * @author Luis Arias
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class State
{
	private static final String[] emptyStrList = new String[0];
	private static final int[] emptyIntList = new int[0];
	
    public final String name;
	public final Quest quest;
	private HashMap<Integer, ArrayList<L2DropData>> drops;
	private String[] events = emptyStrList;
	private int[] killIds = emptyIntList;
	private int[] talkIds = emptyIntList;


    public State(String name, Quest quest)
    {
        this.name = name;
		this.quest = quest;
		quest.addState(this);
    }
	
    public String getName()
    {
        return name;
    }
	
	public String toString() {
		return name;
	}
	
	String[] getEvents() {
		return events;
	}
	
	int[] getKillIds() {
		return killIds;
	}
	
	public int addKillId(int killId) {
		int len = killIds.length;
		int[] tmp = new int[len+1];
		for (int i=0; i < len; i++)
			tmp[i] = killIds[i];
		tmp[len] =killId;
		killIds = tmp;
		return killId;
	}
	
	int[] getTalkIds() {
		return talkIds;
	}
	
	public int addTalkId(int talkId) {
		int len = talkIds.length;
		int[] tmp = new int[len+1];
		for (int i=0; i < len; i++)
			tmp[i] = talkIds[i];
		tmp[len] =talkId;
		talkIds = tmp;
		return talkId;
	}
	
	HashMap<Integer, ArrayList<L2DropData>> getDrops() {
		return drops;
	}
	
	public void addQuestDrop(int npcId, int itemId, int chance) {
		try {
			if (drops == null)
				drops = new HashMap<Integer, ArrayList<L2DropData>>();
			L2DropData d = new L2DropData();
			d.setItemId(itemId);
			d.setChance(chance);
			d.setQuestID(quest.name);
			d.addStates(new String[]{this.name});
			ArrayList<L2DropData> lst = drops.get(npcId);
			if (lst != null) {
				lst.add(d);
			} else {
				lst = new ArrayList<L2DropData>();
				lst.add(d);
				drops.put(npcId, lst);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
