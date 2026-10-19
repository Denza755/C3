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
package net.sf.l2j.gameserver;

import java.util.ArrayList;
//import java.util.logging.Logger;

import net.sf.l2j.gameserver.model.L2PcInstance;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.serverpackets.ServerBasePacket;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;

/**
 * This class stores references to all online game masters. (access level > 100)
 * 
 */
public class GmListTable
{
//	private static Logger _log = Logger.getLogger(GmListTable.class.getName());
	
	public static ArrayList<L2PcInstance> getAllGMs()
	{
		ArrayList<L2PcInstance> gmList = new ArrayList<L2PcInstance>();
		for(L2PcInstance player : L2World.getAllPlayers())
			if(player.isGM())
				gmList.add(player);

		return gmList;
	}

	public static ArrayList<L2PcInstance> getAllVisibleGMs()
	{
		ArrayList<L2PcInstance> gmList = new ArrayList<L2PcInstance>();
		for(L2PcInstance player : L2World.getAllPlayers())
			if(player.isGM() && player.getInvisible()==0)
				gmList.add(player);

		return gmList;
	}

	public static void sendListToPlayer (L2PcInstance player)
	{
		ArrayList<L2PcInstance> gmList = getAllVisibleGMs();
		if(gmList.isEmpty()) 
		{
			player.sendPacket(new SystemMessage(SystemMessage.THERE_ARE_NOT_ANY_GMS_THAT_ARE_PROVIDING_CUSTOMER_SERVICE_CURRENTLY));
		} 
		else 
		{
			player.sendPacket(new SystemMessage(SystemMessage._GM_LIST_));
            for(L2PcInstance gm : gmList) 
				player.sendPacket((new SystemMessage(SystemMessage.GM_S1)).addString(gm.getName()));
		}
	}
	
	public static void broadcastToGMs(ServerBasePacket packet) 
	{
		for(L2PcInstance gm : getAllGMs()) 
            gm.sendPacket(packet);
	}
    
    public static void broadcastMessageToGMs(String message) 
    {
        for(L2PcInstance gm : getAllGMs()) 
            gm.sendMessage(message);
    }
}
