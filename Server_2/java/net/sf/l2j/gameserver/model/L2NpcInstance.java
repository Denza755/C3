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
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.ClientScheduler;
import net.sf.l2j.gameserver.ClientThread;
import net.sf.l2j.gameserver.ItemTable;
import net.sf.l2j.gameserver.ai.CtrlIntention;
import net.sf.l2j.gameserver.model.quest.Quest;
import net.sf.l2j.gameserver.model.quest.QuestState;
import net.sf.l2j.gameserver.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.serverpackets.MyTargetSelected;
import net.sf.l2j.gameserver.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.serverpackets.NpcInfo;
import net.sf.l2j.gameserver.serverpackets.StatusUpdate;
import net.sf.l2j.gameserver.serverpackets.ValidateLocation;
import net.sf.l2j.gameserver.templates.L2Item;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;
import net.sf.l2j.gameserver.templates.L2Weapon;

/**
 * This class represents a Non-Player-Character in the world. it can be 
 * a monster or a friendly character.
 * it also uses a template to fetch some static values.
 * the templates are hardcoded in the client, so we can rely on them.
 * 
 * @version $Revision: 1.32.2.7.2.24 $ $Date: 2005/04/11 10:06:09 $
 */
public class L2NpcInstance extends L2Character
{
	private static Logger _log = Logger.getLogger(L2NpcInstance.class.getName());
	public static final int INTERACTION_DISTANCE = 150;
    private static final int MINIMUM_AGGRO_RANGE    = 200;

	private L2Spawn _spawn;

	
	public L2NpcInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
        setName(template.name);
	}
	
	public final L2NpcTemplate getTemplate()
	{
		return (L2NpcTemplate)_template;
	}
	
	public int getNpcId()
	{
		return getTemplate().npcId;
	}

	public final String getFactionId()
	{
		return getTemplate().factionId;
	}

	public final int getLevel() {
		return getTemplate().level;
	}
	
	public boolean isAggressive()
	{
		return false;
	}
	
	public int getAggroRange()
	{
		return getTemplate().aggroRange;
	}
	
	public int getFactionRange()
	{
		return getTemplate().factionRange;
	}
	
	public void updateAbnormalEffect()
    {
        //NpcInfo info = new NpcInfo(this);
        //broadcastPacket(info);
		for (L2PcInstance player : getKnownPlayers())
			player.sendPacket(new NpcInfo(this, player));
    }
    
	protected int getDistanceToWatchObject(L2Object object)
	{
		if (object instanceof L2FolkInstance || !(object instanceof L2Character))
			return 0;
		if (object instanceof L2PlayableInstance) {
			return 1500;
		}
		return 500;
	}
	protected int getDistanceToForgetObject(L2Object object)
	{
		return 2*getDistanceToWatchObject(object);
	}

	public boolean isAutoAttackable(L2Character attacker) {
		return false;
	}

	/**
	 * @return
	 */
	public int getLeftHandItem()
	{
		return getTemplate().lhand;
	}

	/**
	 * @return
	 */
	public int getRightHandItem()
	{
		return getTemplate().rhand;
	}

	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.model.L2Object#onAction(net.sf.l2j.gameserver.model.L2PcInstance)
	 */
	public void onAction(L2PcInstance player)
	{
		//if (this != player.getNewTarget())
		if (this != player.getTarget())
		{
			if (Config.DEBUG) _log.fine("new target selected:"+getObjectId());
			//player.setNewTarget(this);
			player.setTarget(this);
			MyTargetSelected my = new MyTargetSelected(getObjectId(), player.getLevel() - getLevel());
			player.sendPacket(my);
			
			if (isAutoAttackable(player))
			{	
				StatusUpdate su = new StatusUpdate(getObjectId());
				su.addAttribute(StatusUpdate.CUR_HP, (int)getCurrentHp() );
				su.addAttribute(StatusUpdate.MAX_HP, getMaxHp() );
				player.sendPacket(su);
			}
			
			// correct location
			player.sendPacket(new ValidateLocation(this));
		}
		else
		{
			if (isAutoAttackable(player) && !isAlikeDead())
			{
				if (Math.abs(player.getZ() - getZ()) < 400) // this max heigth difference might need some tweaking
				{
					player.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, this);
//					player.startAttack(this);
				}
				else
				{
					player.sendPacket(new ActionFailed());
				}
			}
			if(!isAutoAttackable(player))
			{
				player.getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, this);
				double distance = getDistance(player.getX(), player.getY());
				if (distance > INTERACTION_DISTANCE) {
//					player.setCurrentState(L2Character.STATE_INTERACT);
//					player.setInteractTarget(this);
//					player.moveTo(this.getX(), this.getY(), this.getZ(), INTERACTION_DISTANCE);
				} else {
					showChatWindow(player, 0);
					player.sendPacket(new ActionFailed());					
//					player.setCurrentState(L2Character.STATE_IDLE);
				}
			}
		}
	}
	
	public void onActionShift(ClientThread client)
	{
		L2PcInstance player = client.getActiveChar();
		if (player.getAccessLevel() >= Config.GM_ACCESSLEVEL)
		{
			player.setTarget(this);
			MyTargetSelected my = new MyTargetSelected(getObjectId(), player.getLevel() - getLevel());
			player.sendPacket(my);
			
			if (isAutoAttackable(player))
			{	
				StatusUpdate su = new StatusUpdate(getObjectId());
				su.addAttribute(StatusUpdate.CUR_HP, (int)getCurrentHp() );
				su.addAttribute(StatusUpdate.MAX_HP, getMaxHp() );
				player.sendPacket(su);
			}
			
			NpcHtmlMessage html = new NpcHtmlMessage(1);
			StringBuffer html1 = new StringBuffer("<html><body>");
			String className = getClass().getName().substring(28);
			html1.append("<center>class "+className+", faction "+getFactionId()+"</center>");
			html1.append("<table border=0 width=\"100%\">");
			html1.append("<tr><td>Target</td><td>"+getObjectId()+"</td><td>NPC ID</td><td>"+getTemplate().npcId+"</td></tr>");
			html1.append("<tr><td>Level</td><td>"+getLevel()+"</td><td>Aggro</td><td>"+((this instanceof L2Attackable)? ((L2Attackable)this).getAggroRange() : 0)+"</td></tr>");
			html1.append("</table>");
			
			html1.append("<p>Combat");
			html1.append("<table border=0 width=\"100%\">");
			html1.append("<tr><td>Max.HP</td><td>"+getMaxHp()+"</td><td>Max.MP</td><td>"+getMaxMp()+"</td></tr>");
			html1.append("<tr><td>P.Atk.</td><td>"+getPAtk(null)+"</td><td>M.Atk.</td><td>"+getMAtk(null,null)+"</td></tr>");
			html1.append("<tr><td>P.Def.</td><td>"+getPDef(null)+"</td><td>M.Def.</td><td>"+getMDef(null,null)+"</td></tr>");
			html1.append("<tr><td>Accuracy</td><td>"+getAccuracy()+"</td><td>Evasion</td><td>"+getEvasionRate(null)+"</td></tr>");
			html1.append("<tr><td>Critical</td><td>"+getCriticalHit(null,null)+"</td><td>Speed</td><td>"+getRunSpeed()+"</td></tr>");
			html1.append("<tr><td>Atk.Speed</td><td>"+getPAtkSpd()+"</td><td>Cast.Speed</td><td>"+getMAtkSpd()+"</td></tr>");
			html1.append("</table>");
			
			html1.append("<p>Basic");
			html1.append("<table border=0 width=\"100%\">");
			html1.append("<tr><td>STR</td><td>"+getSTR()+"</td><td>DEX</td><td>"+getDEX()+"</td><td>CON</td><td>"+getCON()+"</td></tr>");
			html1.append("<tr><td>INT</td><td>"+getINT()+"</td><td>WIT</td><td>"+getWIT()+"</td><td>MEN</td><td>"+getMEN()+"</td></tr>");
//			if (this instanceof L2Attackable)
//				html1.append("<tr><td>Aggro<td>"+((L2Attackable)this).getAggroRange()+"</td><td>x</td><td>x</td></tr>");
//			html1.append("<tr><td><br></td><td>x</td><td>x</td><td>x</td></tr>");
//			
//			html1.append("<tr><td>Class<td>x</td>"+getClass().getName()+"</td><td>x</td><td>x</td></tr>");
//			html1.append("<tr><td>x</td><td>x</td><td>x</td></tr>");
			html1.append("</table>");
			
			//changed by terry 2005-02-22 21:45
			html1.append("<table><tr><td><button value=\"Edit NPC\" action=\"bypass -h admin_edit_npc " + getTemplate().npcId + "\" width=100 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
			html1.append("<td><button value=\"Kill\" action=\"bypass -h admin_kill\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
			html1.append("<tr><td><button value=\"Show DropList\" action=\"bypass -h admin_show_droplist " + getTemplate().npcId + "\" width=100 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");			
			html1.append("<td><button value=\"Delete\" action=\"bypass -h admin_delete\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
            html1.append("</table>");
            html1.append("</body></html>");
			
			html.setHtml(html1.toString());
			player.sendPacket(html);
		}
		else
		{
			// attack the mob without moving?
		}
		
		player.sendPacket(new ActionFailed());
	}

	/**
	 * this gets called by the RequestBypassToServer packet handler
	 * override it in subclasses to use a different behavior
	 * @param command
	 */
	public void onBypassFeedback(L2PcInstance player, String command)
	{
		double distance = getDistance(player.getX(), player.getY());
		if (distance > INTERACTION_DISTANCE) {
			player.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, this);
//			player.moveTo(this.getX(), this.getY(), this.getZ(), INTERACTION_DISTANCE);
		} else {
			if (command.startsWith("Quest"))
			{
				String quest = command.substring(5).trim();
				if (quest.length() == 0)
					showQuestWindow(player);
				else
					showQuestWindow(player, quest);
			}
			else if (command.startsWith("Chat"))
			{
				int val = Integer.parseInt(command.substring(5));
				showChatWindow(player, val);
			}
		}
	}
	
	public L2ItemInstance getActiveWeaponInstance()
	{
		// regular NPCs dont have weapons instancies
		return null;
	}
	public L2Weapon getActiveWeaponItem()
	{
		// regular NPCs dont have weapons items
		int weaponId = getTemplate().rhand;
		if (weaponId < 1)
			return null;
		L2Item item = ItemTable.getInstance().getTemplate(getTemplate().rhand);
		if (!(item instanceof L2Weapon))
			return null;
		return (L2Weapon)item;
	}

	public L2ItemInstance getSecondaryWeaponInstance()
	{
		// regular NPCs dont have weapons instancies
		return null;
	}
	public L2Weapon getSecondaryWeaponItem()
	{
		// regular NPCs dont have weapons items
		int weaponId = getTemplate().lhand;
		if (weaponId < 1)
			return null;
		L2Item item = ItemTable.getInstance().getTemplate(getTemplate().lhand);
		if (!(item instanceof L2Weapon))
			return null;
		return (L2Weapon)item;
	}
	

	/**
	 * @param player
	 * @param content
	 */
	public void insertObjectIdAndShowChatWindow(L2PcInstance player, String content)
	{
		
		content = content.replaceAll("%objectId%", String.valueOf(getObjectId()));
		NpcHtmlMessage npcReply = new NpcHtmlMessage(5);
		
		npcReply.setHtml(content);
		player.sendPacket(npcReply);
	}

	public String getHtmlPath(int npcId, int val)
	{
		String pom = "";
		if (val == 0)
		{
			pom = "" + npcId;
		} 
		else 
		{
			pom = npcId + "-" + val;
		}
		
		String temp = "data/html/default/" + pom + ".htm"; 
		File mainText = new File(Config.DATAPACK_ROOT, temp);
		if (mainText.exists())
			return temp;
		
		return "data/html/npcdefault.htm";
	}

	public void showQuestChooseWindow(L2PcInstance player, Quest[] quests) {
		StringBuffer sb = new StringBuffer();
		sb.append("<html><body><title>Talk about:</title><br>");
		for (Quest q : quests) {
			sb.append("<a action=\"bypass -h npc_").append(getObjectId())
					.append("_Quest ").append(q.getName()).append("\">")
					.append(q.getDescr()).append("</a><br>");
		}
		sb.append("</body></html>");
		insertObjectIdAndShowChatWindow(player, sb.toString());
	}
	
	public void showQuestWindow(L2PcInstance player, String questId) {
		FileInputStream fis = null;
		try
		{
			QuestState qs = player.getQuestState(questId);
			if (qs != null) {
				if (!qs.getQuest().notifyTalk(getTemplate().npcId, qs))
					return; // no need to show a window
			}
			else if (Quest.findQuest(questId) != null) {
				Quest q = Quest.findQuest(questId);
				// check for start point
				Quest[] qlst = getTemplate().getStartQuests();
				if (qlst != null && qlst.length > 0) {
					for (int i=0; i < qlst.length; i++) {
						if (qlst[i] == q) {
							qs = q.newQuestState(player);
							if(qs.getState().getName().equalsIgnoreCase("completed"))
							{
								if (!qs.getQuest().notifyTalk(getTemplate().npcId, qs))
									return; // no need to show a window
							}
							break;
						}
					}
				}
			}
	        String content;
			if (qs == null) {
				// no quests found
				content = "<html><body>No Quest</body></html>";
			} else {
				questId = qs.getQuest().getName();
				String stateId = qs.getStateId();
				File f = new File(Config.DATAPACK_ROOT, "data/jscript/quests/"+questId+"/"+stateId+".htm");
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
			}
			if (content != null)
				insertObjectIdAndShowChatWindow(player, content);
		} catch (Exception e) {
			_log.warning("problem with npc text "+e);
		} finally {
			try	{ if (fis != null) fis.close(); } catch (Exception e) {/* ignore this */}
		}
		
		player.sendPacket( new ActionFailed() );
	}

	public void showQuestWindow(L2PcInstance player) {
		// collect awaiting quests and start points
		ArrayList<Quest> options = new ArrayList<Quest>();
		QuestState[] awaits = player.getQuestsForTalk(getTemplate().npcId);
		Quest[] starts = getTemplate().getStartQuests();
		if (awaits != null) {
			for (QuestState x : awaits) {
				if (!options.contains(x))
					options.add(x.getQuest());
			}
		}
		if (starts != null) {
			for (Quest x : starts) {
				if (!options.contains(x))
					options.add(x);
			}
		}
		if (options.size() > 1) {
			showQuestChooseWindow(player, options.toArray(new Quest[options.size()]));
		}
		else if (options.size() == 1) {
			showQuestWindow(player, options.get(0).getName());
		}
		else {
			showQuestWindow(player, "");
		}
	}

	public void showChatWindow(L2PcInstance player, int val)
	{
		int npcId = getTemplate().npcId;
		
		String filename = (getHtmlPath(npcId, val));
		
		File file = new File(Config.DATAPACK_ROOT, filename);
		if (!file.exists())
		{
			NpcHtmlMessage html = new NpcHtmlMessage(1);
			html.setHtml("<html><head><body>My Text is missing:<br>"+filename+"</body></html>");
			player.sendPacket(html);
			
			player.sendPacket( new ActionFailed() );
		}
		else
		{
			FileInputStream fis = null;		
			try
			{
				fis = new FileInputStream(file);
				byte[] raw = new byte[fis.available()];
				fis.read(raw);
				
				String content = new String(raw, "UTF-8");
				
				insertObjectIdAndShowChatWindow(player, content);
				
			}
			catch (Exception e)
			{
				_log.warning("problem with npc text "+e);
			}
			finally
			{
				try	{ fis.close(); } catch (Exception e1) {/* ignore this */}
			}
		}
		
		player.sendPacket( new ActionFailed() );
	}

	public int getExpReward()
	{
		return (int)(getTemplate().revardExp * Config.RATE_XP);
	}
	
	public int getSpReward()
	{
		return (int)(getTemplate().revardSp * Config.RATE_SP);
	}

	protected void doDie(L2Character killer) {
		// in 7 seconds this corpse will disappear
		ClientScheduler.getInstance().scheduleLow(new DecayTask(), 7000);
		super.doDie(killer);
	}
	
	/**
	 * @param spawn
	 */
	public void setSpawn(L2Spawn spawn)
	{
		_spawn = spawn;
	}
	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.model.L2Character#onDecay()
	 */
	
	public void onDecay()
	{
		super.onDecay();

		// tell my creator that I'm gone
		_spawn.decreaseCount(getTemplate().npcId);
	}
	
	public void deleteMe()
	{
		//FIXME this is just a temp hack, we should find a better solution
		try { decayMe(); } catch (Throwable t) {_log.log(Level.SEVERE, "deletedMe()", t); }
		try { removeAllKnownObjects(); } catch (Throwable t) {_log.log(Level.SEVERE, "deletedMe()", t); }
		L2World.getInstance().removeObject(this);
	}
    /**
     * @return Returns the spawn.
     */
    public L2Spawn getSpawn()
    {
        return _spawn;
    }
    
    public String toString()
    {
    	return this.getTemplate().name;
    }
}
