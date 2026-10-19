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

import net.sf.l2j.gameserver.ClientScheduler;
import net.sf.l2j.gameserver.ClientThread;
import net.sf.l2j.gameserver.instancemanager.CastleManager;
import net.sf.l2j.gameserver.MapRegionTable;
import net.sf.l2j.gameserver.model.entity.Castle;
import net.sf.l2j.gameserver.model.L2PcInstance;
import net.sf.l2j.gameserver.model.Location;
import net.sf.l2j.gameserver.serverpackets.Revive;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;


/**
 * This class ...
 * 
 * @version $Revision: 1.7.2.3.2.6 $ $Date: 2005/03/27 15:29:30 $
 */
public class RequestRestartPoint extends ClientBasePacket
{
    private static final String _C__6d_REQUESTRESTARTPOINT = "[C] 6d RequestRestartPoint";
    //private static Logger _log = Logger.getLogger(RequestRestartPoint.class.getName());	
    
    protected int     requestedPointType;
    protected boolean continuation;
    
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
    
    class DeathTask implements Runnable
    {
        L2PcInstance activeChar;
        DeathTask (L2PcInstance _activeChar)
        {
            activeChar = _activeChar;
        }
        
		public void run()
		{
		try
		{
			Location loc;

			if (activeChar.isFestivalParticipant())
				requestedPointType = 4;

			switch (requestedPointType) {
			case 2: loc = MapRegionTable.getInstance().getTeleToLocation(activeChar, MapRegionTable.TeleportWhereType.Castle); break;
			case 3: loc = MapRegionTable.getInstance().getTeleToLocation(activeChar, MapRegionTable.TeleportWhereType.SiegeFlag); break;
			case 4: loc = new Location(activeChar.getX(), activeChar.getY(), activeChar.getZ()); break;
			default: loc = MapRegionTable.getInstance().getTeleToLocation(activeChar, MapRegionTable.TeleportWhereType.ClosestTown); break;
			}

			MapRegionTable.getInstance().teleToLocation(activeChar, loc);
			activeChar.doRevive();
		} catch (Throwable e) {
			//_log.log(Level.SEVERE, "", e);
		}
        }
    }
    
    void runImpl()
    {
        L2PcInstance activeChar = getClient().getActiveChar();
        
        if (activeChar == null)
            return;
            //SystemMessage sm2 = new SystemMessage(614);
	    //sm2.addString("type:"+requestedPointType);
	    //activeChar.sendPacket(sm2);
        
        if (activeChar.isFakeDeath())
        {
            activeChar.stopFakeDeath(null);
            activeChar.broadcastPacket(new Revive(activeChar));
            return;
        }

        Castle castle = CastleManager.getInstance().getCastle(activeChar.getX(),activeChar.getY());
        if (castle != null && castle.getSiege().getIsInProgress())
        {
            //DeathFinalizer df = new DeathFinalizer(10000);
            SystemMessage sm = new SystemMessage(614);
            if (activeChar.getClan() != null
                    && castle.getSiege().checkIsAttacker(activeChar.getClan()))
            {
                // Schedule respawn delay for attacker
                ClientScheduler.getInstance().scheduleLow(new DeathTask(activeChar), castle.getSiege().getAttackerRespawnDelay());
                sm.addString("You will be re-spawned in " + castle.getSiege().getAttackerRespawnDelay()/1000 + " seconds");
                activeChar.sendPacket(sm);
            }
            else
            {
                // Schedule respawn delay for defender with penalty for CT lose
                ClientScheduler.getInstance().scheduleLow(new DeathTask(activeChar), castle.getSiege().getDefenderRespawnDelay());
                sm.addString("You will be re-spawned in " + castle.getSiege().getDefenderRespawnDelay()/1000 + " seconds");
                activeChar.sendPacket(sm);
            }
            return;
        }
        
        ClientScheduler.getInstance().scheduleLow(new DeathTask(activeChar), 1);
    }
    
    
    
    /* (non-Javadoc)
     * @see net.sf.l2j.gameserver.clientpackets.ClientBasePacket#getType()
     */
    public String getType()
    {
        return _C__6d_REQUESTRESTARTPOINT;
    }
}
