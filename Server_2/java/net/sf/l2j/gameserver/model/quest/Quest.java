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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Collection;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.NpcTable;
import net.sf.l2j.gameserver.model.L2PcInstance;
import net.sf.l2j.gameserver.model.quest.jython.QuestJython;
import net.sf.l2j.gameserver.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;

/**
 * @author Luis Arias
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public abstract class Quest
{
	protected static Logger _log = Logger.getLogger(Quest.class.getName());

	private static HashMap<String, Quest> allQuestsS = new HashMap<String, Quest>();
	private static HashMap<Integer, Quest> allQuestsI = new HashMap<Integer, Quest>();
	private static HashMap<String, Quest> allEventsS = new HashMap<String, Quest>();

	public final int questId;
	public final String name;
	public final String descr;
    private State initialState;
    private HashMap<String, State> states;
    
	
	public static void init() {
		QuestJython.init();
	}
	
	public static Quest findQuest(String id) {
		Quest q = allQuestsS.get(id);
		if (q != null)
			return q;
		q = allEventsS.get(id);
		if (q != null)
			return q;
		return null;
	}
	
	public static Collection<Quest> findAllEvents() {
		return allEventsS.values();
	}
	
	public static Quest findQuest(int id) {
		return allQuestsI.get(id);
	}
	
    /**
     * 
     */
    public Quest(int questId, String name, String descr)
    {
		this.questId = questId;
		this.name = name;
		this.descr = descr;
        states = new HashMap<String, State>();
		if (questId != 0) {
			allQuestsS.put(name, this);
			allQuestsI.put(questId, this);
		} else {
			allEventsS.put(name, this);
		}
    }
	
	public int getQuestIntId() {
		return questId;
	}
	
	public void setInitialState(State state) {
		this.initialState = state;
	}
	
	public QuestState newQuestState(L2PcInstance player) {
		QuestState qs = new QuestState(this, player, getInitialState(), false);
		Quest.createQuestInDb(qs);
		return qs;
	}
	
	public State getInitialState() {
		return initialState;
	}
    
	public String getName() {
		return name;
	}
	
	public String getDescr() {
		return descr;
	}
	
    public State addState(State state)
    {
        states.put(state.getName(), state);
		return state;
    }
    
    public L2NpcTemplate addStartNpc(int npcId)
    {
		L2NpcTemplate t = NpcTable.getInstance().getTemplate(npcId);
		if (t != null) {
			t.addStartQuests(this);
		}
		return t;
    }
    
	// these are methods to call from java
	public final boolean notifyEvent(String event, QuestState qs) {
		String res = null;
		try { res = onEvent(event, qs); } catch (Exception e) { return showError(qs, e); }
		return showResult(qs, res);
	} 
	public final boolean notifyKill (int npcId, QuestState qs) {
		String res = null;
		try { res = onKill(npcId, qs); } catch (Exception e) { return showError(qs, e); }
		return showResult(qs, res);
	}
	public final boolean notifyTalk (int npcId, QuestState qs) {
		String res = null;
		try { res = onTalk(npcId, qs); } catch (Exception e) { return showError(qs, e); }
		return showResult(qs, res);
	}

	// these are methods that java calls to invoke scripts
	public String onEvent(String event, QuestState qs) { return null; } 
	public String onKill (int npcId, QuestState qs) { return onEvent("", qs); }
	public String onTalk (int npcId, QuestState qs) { return onEvent("", qs); }
	
	
	private boolean showError(QuestState qs, Throwable t) {
		_log.log(Level.WARNING, "", t);
		if (qs.getPlayer().getAccessLevel() > 0) {
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			t.printStackTrace(pw);
			pw.close();
			String res = "<html><body><title>Script error</title>"+sw.toString()+"</body></html>";
			return showResult(qs, res);
		}
		return false;
	}
	
	private boolean showResult(QuestState qs, String res) {
		if (res == null)
			return true;
		if (res.endsWith(".htm")) {
			qs.showHtmlFile(res);
		}
		else if (res.startsWith("<html>")) {
			NpcHtmlMessage npcReply = new NpcHtmlMessage(5);
			npcReply.setHtml(res);
			qs.getPlayer().sendPacket(npcReply);
		}
		else {
			SystemMessage sm = new SystemMessage(SystemMessage.S1_S2);
			sm.addString(res);
			qs.getPlayer().sendPacket(sm);
		}
		return false;
	}
	
	public static void playerEnter(L2PcInstance player) {
		// restore player's state
        java.sql.Connection con = null;
        try
        {
            con = L2DatabaseFactory.getInstance().getConnection();
            PreparedStatement statement;
            statement = con.prepareStatement("SELECT name,value FROM character_quests WHERE char_id=? AND var=?");
            statement.setInt   (1,player.getObjectId());
            statement.setString(2, "<state>");
			ResultSet rs = statement.executeQuery();
			while (rs.next()) {
				String questId = rs.getString("name");
				String stateId = rs.getString("value");
				Quest q = findQuest(questId);
				if (q == null) {
					_log.warning("Unknown quest "+questId+" for player "+player.getName());
					continue;
				}
				boolean completed = false;
				if (stateId.length() > 0 && stateId.charAt(0) == '*') {
					completed = true;
					stateId = stateId.substring(1);
				}
				State state = q.states.get(stateId);
				if (state == null) {
					_log.warning("Unknown state "+state+" in quest "+questId+" for player "+player.getName());
					continue;
				}
				new QuestState(q, player, state, completed);
			}
			rs.close();
            statement.close();

            statement = con.prepareStatement("SELECT name,var,value FROM character_quests WHERE char_id=?");
            statement.setInt   (1,player.getObjectId());
			rs = statement.executeQuery();
			while (rs.next()) {
				String questId = rs.getString("name");
				String var     = rs.getString("var");
				String value   = rs.getString("value");
				QuestState qs = player.getQuestState(questId);
				if (qs == null) {
					_log.warning("Lost variable "+var+" in quest "+questId+" for player "+player.getName());
					continue;
				}
				qs.setInternal(var, value);
			}
			rs.close();
            statement.close();
			
		} catch (Exception e) {
			_log.log(Level.WARNING, "could not insert char quest:", e);
        } finally {
            try { con.close(); } catch (Exception e) {}
        }
		
		// events
		for (String name : allEventsS.keySet()) {
			player.processQuestEvent(name, "enter");
		}
	}


	public static void createQuestVarInDb(QuestState qs, String var, String value) {
        java.sql.Connection con = null;
        try
        {
            con = L2DatabaseFactory.getInstance().getConnection();
            PreparedStatement statement;
            statement = con.prepareStatement("INSERT INTO character_quests (char_id,name,var,value) VALUES (?,?,?,?)");
            statement.setInt   (1, qs.getPlayer().getObjectId());
            statement.setString(2, qs.getQuest().getName());
            statement.setString(3, var);
            statement.setString(4, value);
			statement.executeUpdate();
            statement.close();
        } catch (Exception e) {
			_log.log(Level.WARNING, "could not insert char quest:", e);
        } finally {
            try { con.close(); } catch (Exception e) {}
        }
	}
	
	public static void updateQuestVarInDb(QuestState qs, String var, String value) {
        java.sql.Connection con = null;
        try
        {
            con = L2DatabaseFactory.getInstance().getConnection();
            PreparedStatement statement;
            statement = con.prepareStatement("UPDATE character_quests SET value=? WHERE char_id=? AND name=? AND var = ?");
            statement.setString(1, value);
            statement.setInt   (2, qs.getPlayer().getObjectId());
            statement.setString(3, qs.getQuest().getName());
            statement.setString(4, var);
			statement.executeUpdate();
            statement.close();
        } catch (Exception e) {
			_log.log(Level.WARNING, "could not delete char quest:", e);
        } finally {
            try { con.close(); } catch (Exception e) {}
        }
	}
	
	public static void deleteQuestVarInDb(QuestState qs, String var) {
        java.sql.Connection con = null;
        try
        {
            con = L2DatabaseFactory.getInstance().getConnection();
            PreparedStatement statement;
            statement = con.prepareStatement("DELETE FROM character_quests WHERE char_id=? AND name=? AND var=?");
            statement.setInt   (1, qs.getPlayer().getObjectId());
            statement.setString(2, qs.getQuest().getName());
            statement.setString(3, var);
			statement.executeUpdate();
            statement.close();
        } catch (Exception e) {
			_log.log(Level.WARNING, "could not delete char quest:", e);
        } finally {
            try { con.close(); } catch (Exception e) {}
        }
	}
	
	public static void deleteQuestInDb(QuestState qs) {
        java.sql.Connection con = null;
        try
        {
            con = L2DatabaseFactory.getInstance().getConnection();
            PreparedStatement statement;
            statement = con.prepareStatement("DELETE FROM character_quests WHERE char_id=? AND name=?");
            statement.setInt   (1, qs.getPlayer().getObjectId());
            statement.setString(2, qs.getQuest().getName());
			statement.executeUpdate();
            statement.close();
        } catch (Exception e) {
			_log.log(Level.WARNING, "could not delete char quest:", e);
        } finally {
            try { con.close(); } catch (Exception e) {}
        }
	}
	
	public static void createQuestInDb(QuestState qs) {
		createQuestVarInDb(qs, "<state>", qs.getStateId());
	}
	
	public static void updateQuestInDb(QuestState qs) {
		String val = qs.getStateId();
		if (qs.isCompleted())
			val = "*" + val;
		updateQuestVarInDb(qs, "<state>", val);
	}
	
}
