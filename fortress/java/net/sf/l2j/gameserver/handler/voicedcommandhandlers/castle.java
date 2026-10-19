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
package net.sf.l2j.gameserver.handler.voicedcommandhandlers;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.handler.IVoicedCommandHandler;
import net.sf.l2j.gameserver.model.L2DoorInstance;
import net.sf.l2j.gameserver.model.L2PcInstance;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.serverpackets.Ride;

/**
 * 
 *
 */
public class castle implements IVoicedCommandHandler
{
    private static String[] _voicedCommands = { "open doors", "close doors", "ride wyvern" }; 

    public boolean useVoicedCommand(String command, L2PcInstance activeChar, String target)
    {
    	if(command.startsWith("open doors")&&target.equals("castle")&&(activeChar.getClanPrivileges()>=Config.CG_CASTLE||activeChar.isClanLeader())){
    		int theCastle = activeChar.getClan().getHasCastle();
	        int x1 = L2World.zoneCastle[theCastle][0];
	        int y1 = L2World.zoneCastle[theCastle][1];
	        int x2 = L2World.zoneCastle[theCastle][2];
	        int y2 = L2World.zoneCastle[theCastle][3];
	        L2DoorInstance door = (L2DoorInstance) activeChar.getTarget();
	        if (door.getX() >= Math.min(x1,x2)
	                && door.getX() <= Math.max(x1,x2)
	                && door.getY() >= Math.min(y1,y2)
	                && door.getY() <= Math.max(y1,y2))
	        {
	        	door.openMe();
	        }

    	}
    	else if(command.startsWith("close doors")&&target.equals("castle")&&(activeChar.getClanPrivileges()>=Config.CG_CASTLE||activeChar.isClanLeader())){
    		int theCastle = activeChar.getClan().getHasCastle();
	        int x1 = L2World.zoneCastle[theCastle][0];
	        int y1 = L2World.zoneCastle[theCastle][1];
	        int x2 = L2World.zoneCastle[theCastle][2];
	        int y2 = L2World.zoneCastle[theCastle][3];
	        L2DoorInstance door = (L2DoorInstance) activeChar.getTarget();
	        if (door.getX() >= Math.min(x1,x2)
	                && door.getX() <= Math.max(x1,x2)
	                && door.getY() >= Math.min(y1,y2)
	                && door.getY() <= Math.max(y1,y2))
	        {
	        	door.closeMe();
	        }

    	}
    	else if(command.startsWith("ride wyvern")&&target.equals("castle")){
    		if(activeChar.getClan().getHasCastle()>0&&activeChar.isClanLeader()){
    			 Ride mount = new Ride(activeChar.getObjectId(), Ride.ACTION_MOUNT, 12621);
                 activeChar.broadcastPacket(mount);
                 activeChar.setMountType(mount.getMountType());
    		}

    	}
    	return true;
    }

 
    public String[] getVoicedCommandList()
    {
        return _voicedCommands;
    }
}
