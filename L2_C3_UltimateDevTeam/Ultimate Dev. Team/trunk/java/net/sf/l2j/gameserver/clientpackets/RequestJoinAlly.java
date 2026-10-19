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

import net.sf.l2j.gameserver.ClientThread;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.packets.serverpackets.AskJoinAlly;
import net.sf.l2j.gameserver.packets.serverpackets.SystemMessage;

import java.nio.ByteBuffer;

/**
 * This class ...
 * 
 * @version $Revision: 1.3.4.2 $ $Date: 2005/03/27 15:29:30 $
 */
public class RequestJoinAlly extends ClientBasePacket{
	
	private static final String _C__82_REQUESTJOINALLY = "[C] 82 RequestJoinAlly";

	private final int _id;
	
	public RequestJoinAlly(ByteBuffer buf, ClientThread client)
	{
		super(buf, client);
		_id = readD();
	}

	void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if(activeChar == null)
    	    return;
		if(activeChar.getClan() == null)
    	    return;
		if(activeChar.getAllyId() == 0)
    	    return;
		L2PcInstance target = (L2PcInstance) L2World.getInstance().findObject(_id);
    	if(target == null){
		    //Target is not found in the game.
		    activeChar.sendPacket(new SystemMessage(SystemMessage.TARGET_IS_NOT_FOUND_IN_THE_GAME));
		    return;
		}
    		if(target.getClan() == null)
        	    return;
		if (!activeChar.isClanLeader() || activeChar.getClanId() != activeChar.getAllyId()){
		    //feature available only for alliance leader
		    activeChar.sendPacket(new SystemMessage(SystemMessage.FEATURE_ONLY_FOR_ALLIANCE_LEADER));
        	    return;
		}
		if (activeChar == target){
		    //you don't need invite yourself
		    activeChar.sendPacket(new SystemMessage(SystemMessage.CANNOT_INVITE_YOURSELF));
        	    return;
		}
		if(target.getAllyId() == activeChar.getAllyId()){
	    //same alliance - no need to invite
	    SystemMessage sm = new SystemMessage(SystemMessage.S1_CLAN_ALREADY_MEMBER_OF_S2_ALLIANCE);
	    sm.addString(activeChar.getClan().getName());
	    sm.addString(activeChar.getClan().getAllyName());
	    activeChar.sendPacket(sm);
    	    return;
		}
		if(!target.isClanLeader()){
		    //not clan leader
		    activeChar.sendPacket(new SystemMessage(SystemMessage.S1_IS_NOT_A_CLAN_LEADER).addString(target.getName()));
        	    return;
		}
		if(activeChar.getClan().isAtWarWith(target.getClanId())>0)
		{
			activeChar.sendPacket(new SystemMessage(SystemMessage.MAY_NOT_ALLY_CLAN_BATTLE));
			return;
		}
		if (activeChar.isTransactionInProgress())
		{
		    activeChar.sendPacket(new SystemMessage(SystemMessage.WAITING_FOR_REPLY));
		    return;
		}
        if (target.isTransactionInProgress())
        {
		    activeChar.sendPacket(new SystemMessage(SystemMessage.S1_IS_BUSY_TRY_LATER));
		    return;
        }
	    target.setTransactionRequester(activeChar);
	    activeChar.setTransactionRequester(target);
	    //leader of alliance request an alliance.
	    SystemMessage sm = new SystemMessage(SystemMessage.S2_ALLIANCE_LEADER_OF_S1_REQUESTED_ALLIANCE);
	    sm.addString(activeChar.getClan().getAllyName());
	    sm.addString(activeChar.getName());
	    target.sendPacket(sm);
	    target.sendPacket(new AskJoinAlly(activeChar.getObjectId(),activeChar.getName(), activeChar.getClan().getAllyName()));
	    return;
	}
	
	
	public String getType()
	{
		return _C__82_REQUESTJOINALLY;
	}
}

