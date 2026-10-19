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

/**
 * @author NightMarez
 * @version $Revision: 1.3.2.2.2.5 $ $Date: 2005/03/27 15:29:32 $
 *
 */

import java.util.ArrayList;
import java.util.logging.Logger;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.TeleportLocationTable;
import net.sf.l2j.gameserver.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;

public final class L2TeleporterInstance extends L2FolkInstance
{
	private static Logger _log = Logger.getLogger(L2TeleporterInstance.class.getName());
	private ArrayList _tpLocs;
	private int _tpId;
	
	/**
	 * @param template
	 */
	public L2TeleporterInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
	}
	
	public void onBypassFeedback(L2PcInstance player, String command)
	{
		// first do the common stuff
		// and handle the commands that all NPC classes know
		super.onBypassFeedback(player, command);
		
		if (command.startsWith("goto"))
		{
			int val = Integer.parseInt(command.substring(5));
			doTeleport(player, val);
		}
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
		
		return "data/html/teleporter/" + pom + ".htm";
	}

	
	public void onAction(L2PcInstance player)
	{
		if (Config.DEBUG) _log.fine("Teleporter activated");
		super.onAction(player);
	}
			
	private void doTeleport(L2PcInstance player, int val)
	{	
		
		L2TeleportLocation list = TeleportLocationTable.getInstance().getTemplate(val);
		
		if (list != null)
		{
			if(player.getAdena() >= list.getPrice())
			{
				if (Config.DEBUG)
					_log.fine("Teleporting to new location: "+
							list.getLocX()+":"+list.getLocY()+":"+list.getLocZ());	
				// take away money it costs to teleport from player
				player.reduceAdena(list.getPrice());
				if (Config.DEBUG)
					_log.fine("Took: "+list.getPrice()+" Adena from player for teleport");
				// teleport
				player.teleToLocation(list.getLocX(), list.getLocY(), list.getLocZ());
			}
			else
			{
				if (Config.DEBUG) _log.fine("Not enough adena to teleport");
				// send "not enough adena" message
				SystemMessage sm = new SystemMessage(279);
				player.sendPacket(sm);				
			}
		}
		else
		{
			_log.warning("No teleport destination with id:" +val);
		}
		player.sendPacket( new ActionFailed() );
	}
}
