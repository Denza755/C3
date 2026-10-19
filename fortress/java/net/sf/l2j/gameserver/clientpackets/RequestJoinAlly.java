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

import net.sf.l2j.gameserver.ClientThread;
import net.sf.l2j.gameserver.model.L2PcInstance;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.serverpackets.AskJoinAlly;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;

/**
 * This class ...
 * 
 * @version $Revision: 1.3.4.2 $ $Date: 2005/03/27 15:29:30 $
 */
public class RequestJoinAlly extends ClientBasePacket{
	
	private static final String _C__82_REQUESTJOINALLY = "[C] 82 RequestJoinAlly";
	//private static Logger _log = Logger.getLogger(RequestJoinAlly.class.getName());

	private final int _id;
	
	public RequestJoinAlly(ByteBuffer buf, ClientThread client)
	{
		super(buf, client);
		_id = readD();
	}

	void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
    	if(activeChar == null || activeChar.getClan() == null || activeChar.getAlliance() == null)
			return;

    	L2PcInstance target = (L2PcInstance)L2World.findObject(_id);
    	if(target == null)
		{
		    activeChar.sendPacket(new SystemMessage(SystemMessage.TARGET_IS_NOT_FOUND_IN_THE_GAME));
		    return;
		}
    	if(target.getClan() == null)
            return;
		if(!activeChar.isAllyLeader())
		{
		    activeChar.sendPacket(new SystemMessage(SystemMessage.FEATURE_AVAILABLE_TO_ALLIANCE_LEADERS_ONLY));
        	return;
		}
		if(activeChar == target)
		{
		    activeChar.sendPacket(new SystemMessage(SystemMessage.YOU_CANNOT_ASK_YOURSELF_TO_APPLY_TO_A_CLAN));
        	return;
		}
    	if(target.getAlliance() != null || activeChar.getAlliance().isMember(target.getClan().getName()))
		{
		    //same or another alliance - no need to invite
    		SystemMessage sm = new SystemMessage(SystemMessage.S1_CLAN_IS_ALREADY_A_MEMBER_OF_S2_ALLIANCE);
		    sm.addString(target.getClan().getName());
		    sm.addString(target.getAlliance().getAllyName());
		    activeChar.sendPacket(sm);
        	return;
		}
		if(!target.isClanLeader())
		{
		    activeChar.sendPacket(new SystemMessage(SystemMessage.S1_IS_NOT_A_CLAN_LEADER).addString(target.getName()));
        	return;
		}
		if(activeChar.isAtWarWith(target.getClanId()) > 0)
		{
			activeChar.sendPacket(new SystemMessage(SystemMessage.YOU_MAY_NOT_ALLY_WITH_A_CLAN_YOU_ARE_AT_BATTLE_WITH));
			return;
		}
		if(activeChar.isTransactionInProgress())
		{
			activeChar.sendPacket(new SystemMessage(SystemMessage.WAITING_FOR_ANOTHER_REPLY));
			return;
		}
        if(target.isTransactionInProgress())
        {
			activeChar.sendPacket(new SystemMessage(SystemMessage.S1_IS_BUSY_PLEASE_TRY_AGAIN_LATER).addString(target.getName()));
			return;
		}
	    target.setTransactionRequester(activeChar);
	    activeChar.setTransactionRequester(target);
	    //leader of alliance request an alliance.
	    SystemMessage sm = new SystemMessage(SystemMessage.S2_THE_LEADER_OF_S1_HAS_REQUESTED_AN_ALLIANCE);
	    sm.addString(activeChar.getAlliance().getAllyName());
	    sm.addString(activeChar.getName());
	    target.sendPacket(sm);
	    target.sendPacket(new AskJoinAlly(activeChar.getObjectId(),activeChar.getName(), activeChar.getAlliance().getAllyName()));
	    return;
	}
	
	
	public String getType()
	{
		return _C__82_REQUESTJOINALLY;
	}
}

