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

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.model.L2PcInstance;
import net.sf.l2j.gameserver.serverpackets.ServerBasePacket;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;

/**
 * This class stores references to all online game masters. (access level > 100)
 * 
 * @version $Revision: 1.2.2.1.2.7 $ $Date: 2005/04/05 19:41:24 $
 */
public class GmListTable
{
	private static Logger _log = Logger.getLogger(GmListTable.class.getName());
	private static GmListTable _instance;
	
	private Set<L2PcInstance> _gmList;
	
	public static GmListTable getInstance()
	{
		if (_instance == null)
		{
			_instance = new GmListTable();
		}
		return _instance;
	}
	
    public L2PcInstance[] getAllGms()
    {
        return _gmList.toArray(new L2PcInstance[_gmList.size()]);
    }
	
	private GmListTable()
	{
		_gmList = new HashSet<L2PcInstance>();
	}
	
	public void addGm(L2PcInstance player)
	{
		if (Config.DEBUG) _log.fine("added gm: "+player.getName());
		_gmList.add(player);
	}
	
	public void deleteGm(L2PcInstance player)
	{
		if (Config.DEBUG) _log.fine("deleted gm: "+player.getName());
		_gmList.remove(player);
	}
	
	public void sendListToPlayer (L2PcInstance player){
		if (_gmList.isEmpty()) {
			SystemMessage sm = new SystemMessage(702); //There are not any GMs that are providing customer service currently.
			player.sendPacket(sm);
		} else {
			SystemMessage sm = new SystemMessage(SystemMessage.GM_LIST);
			player.sendPacket(sm);
            for (L2PcInstance gm : _gmList) {
				sm = new SystemMessage(SystemMessage.GM_S1);
				sm.addString(gm.getName());
				player.sendPacket(sm);
			}
		}
	}
	
	public void broadcastToGMs(ServerBasePacket packet) {
		for (L2PcInstance gm : _gmList) {
            gm.sendPacket(packet);
		}
	}
}
