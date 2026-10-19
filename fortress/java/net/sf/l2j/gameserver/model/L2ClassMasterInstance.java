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

import java.util.logging.Logger;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.GameServer;
import net.sf.l2j.gameserver.ai.CtrlIntention;
import net.sf.l2j.gameserver.model.quest.Quest;
import net.sf.l2j.gameserver.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.serverpackets.MyTargetSelected;
import net.sf.l2j.gameserver.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.serverpackets.ValidateLocation;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;

/**
 * This class ...
 * 
 * @version $Revision: 1.4.2.1.2.7 $ $Date: 2005/03/27 15:29:32 $
 */
public final class L2ClassMasterInstance extends L2FolkInstance
{
	private static Logger _log = Logger.getLogger(L2ClassMasterInstance.class.getName());
	
	/**
	 * @param template
	 */
	public L2ClassMasterInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
	}
	
	@SuppressWarnings("static-access")
	public void onAction(L2PcInstance player)
	{
		if(GameServer.events.onAction(player, this))
			return;
		
		if (getObjectId() != player.getTargetId())
		{
			player.setIntention(CtrlIntention.AI_INTENTION_IDLE, null, null);
			if (Config.DEBUG) _log.fine("ClassMaster selected:"+getObjectId());
			player.setTarget(this);
			player.sendPacket(new MyTargetSelected(getObjectId(), player.getLevel() - getLevel()));
			// correct location
			player.sendPacket(new ValidateLocation(this));
		}
		else
		{
			if(getDistance(player) > INTERACTION_DISTANCE) 
			{
				player.setIntention(CtrlIntention.AI_INTENTION_INTERACT, this, null);
				player.sendPacket(new ActionFailed());					
				return;
			}

			if (Config.DEBUG) _log.fine("ClassMaster activated");
			
			ClassId classId = player.getClassId();

			int jobLevel = 0;
			int level = player.getLevel();

			switch (classId)
			{
			case fighter:
			case mage:
			case elvenFighter:
			case elvenMage:
			case darkFighter:
			case darkMage:
			case orcFighter:
			case orcMage:
			case dwarvenFighter:
				jobLevel = 1;
				break;
			case elvenScout:
			case elvenKnight:
			case elvenWizard:
			case oracle:
			case knight:
			case warrior:
			case rogue:
			case wizard:
			case cleric:
			case palusKnight:
			case assassin:
			case darkWizard:
			case shillienOracle:
			case orcRaider:
			case orcMonk:
			case orcShaman:
			case scavenger: 
			case artisan:
				jobLevel = 2;
				break;
            case gladiator:
            case warlord:
            case paladin:
            case darkAvenger:
            case treasureHunter:
            case hawkeye:
            case sorceror:
            case necromancer:
            case warlock:
            case bishop:
            case prophet:
            case templeKnight:
            case swordSinger:
            case plainsWalker:
            case silverRanger:
            case spellsinger:
            case elementalSummoner:
            case elder:
            case shillienKnight:
            case bladedancer:
            case abyssWalker:
            case phantomRanger:
            case spellhowler:
            case phantomSummoner:
            case shillienElder:
            case destroyer:
            case tyrant:
            case overlord:
            case warcryer:
            case bountyHunter:
            case warsmith:
                jobLevel = 3;
                break;
			default:
				jobLevel = 4;
			}
            
			if (Config.ALLOW_CLASS_MASTERS_LIST.isEmpty() || !Config.ALLOW_CLASS_MASTERS_LIST.contains(jobLevel)) {
				jobLevel = 4;
			}
			if (((level >= 20 && jobLevel == 1) ||
				(level >= 40 && jobLevel == 2) ||
                (level >= 76 && jobLevel == 3)) && Config.ALLOW_CLASS_MASTERS_LIST.contains(jobLevel))
			{				
				showChatWindow(player, classId.getId());
			}
			else 
			{
				NpcHtmlMessage html = new NpcHtmlMessage(1);
				StringBuffer sb = new StringBuffer();
				sb.append("<html><head><body>");
				switch (jobLevel)
				{
					case 1:
						sb.append("Come back here when you reached level 20 to change your class.<br>");
						break;
					case 2:
						sb.append("Come back here when you reached level 40 to change your class.<br>");
						break;
                    case 3:
                        sb.append("Come back here when you reached level 76 to change your class.<br>");
                        break;
					case 4:
						sb.append("There is no class changes for you any more.<br>");
						break;
				}
				for (Quest q : Quest.findAllEvents()) {
					sb.append("Event: <a action=\"bypass -h Quest "+q.getName()+"\">"+q.getDescr()+"</a><br>");
				}
				sb.append("</body></html>");
				html.setHtml(sb.toString());
				player.sendPacket(html);
			}
				
			player.sendPacket(new ActionFailed());
		}
	}
	
	public String getHtmlPath(@SuppressWarnings("unused") int npcId, int val)
	{
		return "data/html/classmaster/" + val + ".htm";
	}

	public void onBypassFeedback(L2PcInstance player, String command)
	{
		if(command.startsWith("change_class"))
		{
			int val = Integer.parseInt(command.substring(13));
			changeClass(player, val);
		}
		else 
		{
			// this class dont know any other commands, let forward
			// the command to the parent class
			
			super.onBypassFeedback(player, command);
		}
	}
	
	private void changeClass(L2PcInstance player, int val)
	{
		if (Config.DEBUG) _log.fine("Changing class to ClassId:"+val);
		player.setClassId(val);
		if (Config.DEBUG) _log.fine("name:"+player.getName());
		if (Config.DEBUG) _log.fine("level:"+player.getLevel());
		if (Config.DEBUG) _log.fine("classId:"+player.getClassId());
		player.broadcastUserInfo();
	}
}
