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
package net.sf.l2j.gameserver.clientpackets;

import java.nio.ByteBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.ClientThread;
import net.sf.l2j.gameserver.CommunityBoard;
import net.sf.l2j.gameserver.ai.CtrlIntention;
import net.sf.l2j.gameserver.handler.AdminCommandHandler;
import net.sf.l2j.gameserver.handler.IAdminCommandHandler;
import ru.balancer.JBForth;
import net.sf.l2j.gameserver.model.BypassEngine;
import net.sf.l2j.gameserver.model.L2CharPosition;
import net.sf.l2j.gameserver.model.L2NpcInstance;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2PcInstance;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.serverpackets.NpcHtmlMessage;

/**
 * This class ...
 * 
 * @version $Revision: 1.12.4.5 $ $Date: 2005/04/11 10:06:11 $
 */
public class RequestBypassToServer extends ClientBasePacket
{
	private static final String _C__21_REQUESTBYPASSTOSERVER = "[C] 21 RequestBypassToServer";
	private static Logger _log = Logger.getLogger(RequestBypassToServer.class.getName());

	// S
	private String _command;

	/**
	 * @param decrypt
	 */
	public RequestBypassToServer(ByteBuffer buf, ClientThread client)
	{
		super(buf, client);
		_command = BypassEngine.decode(getClient().getActiveChar(), readS());
		if(_command != null)
			_command = _command.trim();
	}

	void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if(activeChar == null) return;
		
		try 
		{
			if(_command.startsWith("admin_") || _command.startsWith("gm_"))
			{
				String word = (_command.split("\\s+"))[0];
				String args = _command.substring(word.length()).trim();

				if(JBForth.word_exists(word))
				{
					JBForth tmp = new JBForth(activeChar);
  	   				tmp.st.push(args);
    			    tmp.execp(word);
   			       	return;
				}
			}

			if (_command.startsWith("admin_") && activeChar.getAccessLevel() >= Config.GM_ACCESSLEVEL)
			{
				IAdminCommandHandler ach = AdminCommandHandler.getInstance().getAdminCommandHandler(_command);
				
				if (ach != null) {
					ach.useAdminCommand(_command, activeChar);
				} else {
					_log.warning("No handler registered for bypass '"+_command+"'");
				}
			}
			else if (_command.equals("come_here") && activeChar.getAccessLevel() >= Config.GM_ACCESSLEVEL)
			{
				comeHere(getClient());
			}
			else if (_command.startsWith("player_help "))
			{
				playerHelp(activeChar, _command.substring(12));
			}
			else if (_command.startsWith("forth"))
			{
				JBForth tmp = new JBForth(activeChar);
    		    tmp.execp(_command.substring(5));
			}
			else if (_command.startsWith("jbf_"))
			{
				String command = _command.substring(4).trim();
				String word = "bypass_" + (command.split("\\s+"))[0];
				String args = command.substring(word.length()-7).trim();

				if(activeChar.jbf.word_exists(word))
				{
					JBForth tmp = new JBForth(activeChar);
  	   				tmp.st.push(args);
    			    tmp.execp(word);
				}
				else
					_log.warning("Unknow word '"+word+"'");
			}
			else if (_command.startsWith("user_"))
			{
				String command = _command.substring(5).trim();
				String word = "user_" + (command.split("\\s+"))[0];
				String args = command.substring(word.length()-5).trim();

				if(activeChar.jbf.word_exists(word))
				{
					JBForth tmp = new JBForth(activeChar);
  	   				tmp.st.push(args);
    			    tmp.execp(word);
				}
				else
					_log.warning("Unknow word '"+word+"'");
			}
			else if (_command.startsWith("npc_"))
			{
				int endOfId = _command.indexOf('_', 5);
				String id;
				if (endOfId > 0)
					id = _command.substring(4, endOfId);
				else
					id = _command.substring(4);
				L2Object object = L2World.findObject(Integer.parseInt(id));
				if (object != null && object instanceof L2NpcInstance && endOfId > 0 && activeChar.getDistance(object.getX(), object.getY()) <= L2NpcInstance.INTERACTION_DISTANCE)
				{
					((L2NpcInstance) object).onBypassFeedback(activeChar, _command.substring(endOfId+1));
				}
			}
			//	Draw a Symbol
			else if (_command.equals("menu_select?ask=-16&reply=1"))
			{
                L2Object object = activeChar.getTarget();
                if (object instanceof L2NpcInstance)
                {
                    ((L2NpcInstance) object).onBypassFeedback(activeChar, _command);
                }
			}
			else if (_command.equals("menu_select?ask=-16&reply=2"))
			{
                L2Object object = activeChar.getTarget();
                if (object instanceof L2NpcInstance)
                {
                    ((L2NpcInstance) object).onBypassFeedback(activeChar, _command);
                }
			}
			else if (_command.startsWith("bbs_"))
			{
				CommunityBoard.getInstance().handleCommands(getClient(), _command);
			}
			else if (_command.startsWith("Quest "))
			{
				L2PcInstance player = getClient().getActiveChar();
				String p = _command.substring(6).trim();
				int idx = p.indexOf(' ');
				if (idx < 0)
					player.processQuestEvent(p, "");
				else
					player.processQuestEvent(p.substring(0, idx), p.substring(idx).trim());
			}
		} catch (Exception e) {
			_log.log(Level.WARNING, "Bad RequestBypassToServer: ", e);
		}
	}

	/**
	 * @param client
	 */
	private void comeHere(ClientThread client) 
	{
		L2Object obj = client.getActiveChar().getTarget();
		if (obj instanceof L2NpcInstance)
		{
			L2NpcInstance temp = (L2NpcInstance) obj;
			L2PcInstance player = client.getActiveChar(); 
			temp.setTarget(player);
			temp.setIntention(CtrlIntention.AI_INTENTION_MOVE_TO,
					new L2CharPosition(player.getX(),player.getY(), player.getZ(), 0 ), null);
//			temp.moveTo(player.getX(),player.getY(), player.getZ(), 0 );
		}
		
	}

	private void playerHelp(L2PcInstance activeChar, String path)
	{
		String filename = "data/html/"+path;
		NpcHtmlMessage html = new NpcHtmlMessage(1);
		html.setFile(filename);
		activeChar.sendPacket(html);
	}

	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.clientpackets.ClientBasePacket#getType()
	 */
	public String getType()
	{
		return _C__21_REQUESTBYPASSTOSERVER;
	}
}
