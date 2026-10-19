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
import net.sf.l2j.gameserver.ai.CtrlIntention;
import net.sf.l2j.gameserver.handler.IVoicedCommandHandler;
import net.sf.l2j.gameserver.model.L2CastleGuardInstance;
import net.sf.l2j.gameserver.model.L2CharPosition;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2PcInstance;
import net.sf.l2j.gameserver.model.L2World;

/**
 * 
 *
 */
public class movement implements IVoicedCommandHandler
{
    private static String[] _voicedCommands = { "go home", "go to", "stay", "come" }; 

    public boolean useVoicedCommand(String command, L2PcInstance activeChar, String target)
    {
    	if(command.startsWith("go home"))
    	{
	    	for(L2Object obj : activeChar.getKnownObjects(L2World.PLAYER_VISIBLE_TILES))
    		{
    			if (obj instanceof L2CastleGuardInstance)
    			{
    				L2CastleGuardInstance guard = (L2CastleGuardInstance) obj;
	    			if ((guard.getClanId()==activeChar.getClanId())&&(activeChar.getClanPrivileges()>=Config.CG_MOVEMENT||activeChar.isClanLeader()))
    				{
    				    if (guard.getName().equals(target)||guard.getGroup().equals(target))
    				    {
    				    	L2CharPosition pos = new L2CharPosition(guard.x,guard.y,guard.z,0);
    			    		guard.setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, pos, null);
	    			    }
    				}
    			}
    		
	    	}
    	}
    	else if(command.startsWith("go to")){
    		for(L2Object obj : activeChar.getKnownObjects(L2World.PLAYER_VISIBLE_TILES)){
        		if (obj instanceof L2CastleGuardInstance){
        			L2CastleGuardInstance guard = (L2CastleGuardInstance) obj;
        			if ((guard.getClanId()==activeChar.getClanId())&&(activeChar.getClanPrivileges()>=Config.CG_MOVEMENT||activeChar.isClanLeader())){
        			    if (guard.getName().equals(target)||guard.getGroup().equals(target)){
        			    	L2CharPosition pos = new L2CharPosition(activeChar.getTarget().getX(), activeChar.getTarget().getY(), activeChar.getTarget().getZ(), 0);
        			    	guard.setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, pos, null);
        			    }
        			}
        		}
        		
        	}
    		
    	}
    	else if(command.startsWith("stay")){
    		for(L2Object obj : activeChar.getKnownObjects(L2World.PLAYER_VISIBLE_TILES)){
        		if (obj instanceof L2CastleGuardInstance){
        			L2CastleGuardInstance guard = (L2CastleGuardInstance) obj;
        			if ((guard.getClanId()==activeChar.getClanId())&&(activeChar.getClanPrivileges()>=Config.CG_MOVEMENT||activeChar.isClanLeader())){
        			    if (guard.getName().equals(target)||guard.getGroup().equals(target)){
        			    	guard.setIntention(CtrlIntention.AI_INTENTION_IDLE, null, null);
        			    }
        			}
        		}
        		
        	}
    		
    	}
    	else if(command.startsWith("come")){
    		for(L2Object obj : activeChar.getKnownObjects(L2World.PLAYER_VISIBLE_TILES)){
        		if (obj instanceof L2CastleGuardInstance){
        			L2CastleGuardInstance guard = (L2CastleGuardInstance) obj;
        			if ((guard.getClanId()==activeChar.getClanId())&&(activeChar.getClanPrivileges()>=Config.CG_MOVEMENT||activeChar.isClanLeader())){
        			    if (guard.getName().equals(target)||guard.getGroup().equals(target)){
        			    	guard.setIntention(CtrlIntention.AI_INTENTION_FOLLOW, activeChar, null);
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
        			if ((guard.getClanId()==activeChar.getClanId())&&(activeChar.getClanPrivileges()>=Config.CG_MOVEMENT||activeChar.isClanLeader())){
        			    if (guard.getName().equals(target)||guard.getGroup().equals(target)){
        			    	guard.setGroup(group);      			    	
        			    }
        			}
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
