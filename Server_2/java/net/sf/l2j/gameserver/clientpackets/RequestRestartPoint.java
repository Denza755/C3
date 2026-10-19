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
import java.util.logging.Logger;

import net.sf.l2j.gameserver.ClientScheduler;
import net.sf.l2j.gameserver.ClientThread;
import net.sf.l2j.gameserver.MapRegionTable;
import net.sf.l2j.gameserver.model.L2PcInstance;
import net.sf.l2j.gameserver.model.Location;
import net.sf.l2j.gameserver.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.serverpackets.Revive;
import net.sf.l2j.gameserver.serverpackets.SocialAction;
import net.sf.l2j.gameserver.serverpackets.StopMove;


/**
 * This class ...
 * 
 * @version $Revision: 1.7.2.3.2.6 $ $Date: 2005/03/27 15:29:30 $
 */
public class RequestRestartPoint extends ClientBasePacket
{
	private static final String _C__6d_REQUESTRESTARTPOINT = "[C] 6d RequestRestartPoint";
	private static Logger _log = Logger.getLogger(RequestRestartPoint.class.getName());	
	
	private int     requestedPointType;
	private boolean continuation;
			
	/**
	 * packet type id 0x6d
	 * format:		c
	 * @param decrypt
	 */
	public RequestRestartPoint(ByteBuffer buf, ClientThread client)
	{
		super(buf, client);
		requestedPointType = readD();
	}

	void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		
		if (!continuation)
		{
			Location loc;
			if (requestedPointType == 4)
				loc = new Location(activeChar.getX(), activeChar.getY(), activeChar.getZ());
			else
				loc = MapRegionTable.getInstance().getClosestTownCords(activeChar);
			//String[] temp = null;
		
			//Packets and teleport
			sendPacket(new ActionFailed());
			activeChar.broadcastPacket(new StopMove(activeChar));
			activeChar.teleToLocation(loc.getX(), loc.getY(), loc.getZ());
	
			activeChar.setCurrentHpMp(0.6 * activeChar.getMaxHp(), 0.6 * activeChar.getMaxMp());
			
			// continue execution in 2 seconds
			continuation = true;
			ClientScheduler.getInstance().scheduleLow(this, 2000);
		}
		else
		{
			Revive revive = new Revive(activeChar);
			///L2World.getInstance().addVisibleObject(activeChar);
			
			SocialAction sa = new SocialAction(activeChar.getObjectId(), 15);
			activeChar.broadcastPacket(sa);
			activeChar.sendPacket(sa);
			activeChar.sendPacket(revive);
			activeChar.broadcastPacket(revive);
			activeChar.setTarget(activeChar);
			//MagicSkillUser msk = new MagicSkillUser(activeChar, 1016, 1,20000,0);
			//con.sendPacket(msk);
			//activeChar.broadcastPacket(msk);
		}
	}



	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.clientpackets.ClientBasePacket#getType()
	 */
	public String getType()
	{
		return _C__6d_REQUESTRESTARTPOINT;
	}
}
