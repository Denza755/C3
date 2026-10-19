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
import net.sf.l2j.gameserver.model.L2CastleGuardInstance;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2PcInstance;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;

/**
 * 
 *
 */
public class set implements IVoicedCommandHandler
{
    private static String[] _voicedCommands = { "set name", "set home", "set group" }; 

    public boolean useVoicedCommand(String command, L2PcInstance activeChar, String target)
    {
    	if(command.startsWith("set name")){
    	String name = command.substring(9);
    	for(L2Object obj : activeChar.getKnownObjects(L2World.PLAYER_VISIBLE_TILES)){
    		if (obj instanceof L2CastleGuardInstance){
    			L2CastleGuardInstance guard = (L2CastleGuardInstance) obj;
    			if ((guard.getClanId()==activeChar.getClanId())&&(activeChar.getClanPrivileges()>=Config.CG_SET||activeChar.isClanLeader())){
    			    if (guard.getName().equals(target)||guard.getGroup().equals(target)){
    			    	guard.setName(name);
    			    }
    			}
    		}
    		
    	}
    	}
    	else if(command.startsWith("set home")){
    		for(L2Object obj : activeChar.getKnownObjects(L2World.PLAYER_VISIBLE_TILES)){
        		if (obj instanceof L2CastleGuardInstance){
        			L2CastleGuardInstance guard = (L2CastleGuardInstance) obj;
        			if ((guard.getClanId()==activeChar.getClanId())&&(activeChar.getClanPrivileges()>=Config.CG_SET||activeChar.isClanLeader())){
        			    if (guard.getName().equals(target)||guard.getGroup().equals(target)){
        			    	guard.x = guard.getX();
        			    	guard.y  =guard.getY();
        			    	guard.z = guard.getZ();        			    	
        			    }
        			}
        		}
        		
        	}
    		
    	}
    	else if(command.startsWith("set group")){
    		String group = command.substring(10);
    		for(L2Object obj : activeChar.getKnownObjects(L2World.PLAYER_VISIBLE_TILES)){
        		if (obj instanceof L2CastleGuardInstance){
        			L2CastleGuardInstance guard = (L2CastleGuardInstance) obj;
        			if ((guard.getClanId()==activeChar.getClanId())&&(activeChar.getClanPrivileges()>=Config.CG_SET||activeChar.isClanLeader())){
        			    if (guard.getName().equals(target)||guard.getGroup().equals(target)){
        			    	guard.setGroup(group);      			    	
        			    }
        			}
        		}
        		
        	}
    		
    	}
    	else if(command.startsWith("set privileges")){
    		int n = Integer.parseInt(command.substring(15));
    		L2PcInstance pc = (L2PcInstance) activeChar.getTarget();
    		if(pc!=null){
    			if(activeChar.getClan().getClanId()==pc.getClan().getClanId()&&(activeChar.getClanPrivileges()>n)||activeChar.isClanLeader()){
    				pc.setClanPrivileges(n);
    				SystemMessage sm = new SystemMessage(614);
    	            sm.addString("Your clan privileges have been set to " + n + " by " + activeChar.getName());
    	            activeChar.sendPacket(sm);
    			}
    			 
    		}
    		
     	}
    	
    	return true;
    }

 
    public String[] getVoicedCommandList()
    {
        return _voicedCommands;
    }
}
