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
import net.sf.l2j.gameserver.model.L2Clan;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.packets.serverpackets.*;

import java.nio.ByteBuffer;

/**
 * This class ...
 * 
 * @version $Revision: 1.4.2.1.2.3 $ $Date: 2005/03/27 15:29:30 $
 */
public class RequestAnswerJoinPledge extends ClientBasePacket
{
	private static final String _C__25_REQUESTANSWERJOINPLEDGE = "[C] 25 RequestAnswerJoinPledge";
	
	private final int _answer;
			
	public RequestAnswerJoinPledge(ByteBuffer buf, ClientThread client)
	{
		super(buf, client);
		_answer  = readD();
	}

	void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
		    return;
		L2PcInstance requestor = activeChar.getTransactionRequester();
        if (requestor == null)
            return;
		
		if (_answer == 1)
		{
		    if (activeChar.canJoinClan())
		    {
			//not used ?_?
			activeChar.sendPacket(new JoinPledge(requestor.getClanId()));
			
			L2Clan clan = requestor.getClan();
            // this also updates the database
			clan.addClanMember(activeChar);
			activeChar.setClan(clan);
			activeChar.setClanPrivileges(0);
			
			//should be update packet only
			activeChar.sendPacket(new PledgeShowInfoUpdate(clan, activeChar));

			activeChar.broadcastUserInfo();

	    activeChar.sendPacket(new SystemMessage(SystemMessage.ENTERED_THE_CLAN));
		    
			//L2ClanMember[] members = clan.getMembers();
			clan.broadcastToOnlineMembers(new PledgeShowMemberListAdd(activeChar));
			clan.broadcastToOnlineMembers(new SystemMessage(SystemMessage.S1_HAS_JOINED_CLAN).addString(activeChar.getName()));
			
			// this activates the clan tab on the new member
			activeChar.sendPacket(new PledgeShowMemberListAll(clan, activeChar));
			activeChar.setDeleteClanTime(0);
		    } else {
			requestor.sendPacket(new SystemMessage(231));
			activeChar.sendPacket(new SystemMessage(232));
		    }
		} 
		else
		{
			requestor.sendPacket(new SystemMessage(SystemMessage.S1_REFUSED_TO_JOIN_CLAN).addString(activeChar.getName()));
		}
		
		requestor.setTransactionRequester(null);
		activeChar.setTransactionRequester(null);
	}

	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.clientpackets.ClientBasePacket#getType()
	 */
	public String getType()
	{
		return _C__25_REQUESTANSWERJOINPLEDGE;
	}
}
