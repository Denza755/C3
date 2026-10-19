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

import net.sf.l2j.gameserver.ClientThread;
import net.sf.l2j.gameserver.model.L2Clan;
import net.sf.l2j.gameserver.model.L2PcInstance;
import net.sf.l2j.gameserver.serverpackets.JoinPledge;
import net.sf.l2j.gameserver.serverpackets.PledgeShowInfoUpdate;
import net.sf.l2j.gameserver.serverpackets.PledgeShowMemberListAdd;
import net.sf.l2j.gameserver.serverpackets.PledgeShowMemberListAll;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;

/**
 * This class ...
 * 
 * @version $Revision: 1.4.2.1.2.3 $ $Date: 2005/03/27 15:29:30 $
 */
public class RequestAnswerJoinPledge extends ClientBasePacket
{
	private static final String _C__25_REQUESTANSWERJOINPLEDGE = "[C] 25 RequestAnswerJoinPledge";
	private static Logger _log = Logger.getLogger(RequestAnswerJoinPledge.class.getName());
	
	private final int _answer;
			
	public RequestAnswerJoinPledge(ByteBuffer buf, ClientThread client)
	{
		super(buf, client);
		_answer  = readD();
	}

	void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		L2PcInstance requestor = activeChar.getTransactionRequester();
		
		if (_answer == 1)
		{
			//not used ?_?
			JoinPledge jp = new JoinPledge(requestor.getClanId());
			activeChar.sendPacket(jp);
			
			L2Clan clan = requestor.getClan();
            // this also updates the database
			clan.addClanMember(activeChar);
			activeChar.setClan(clan);
			
			//should be update packet only
			PledgeShowInfoUpdate pu = new PledgeShowInfoUpdate(clan, activeChar);
			activeChar.sendPacket(pu);

			activeChar.broadcastUserInfo();

			SystemMessage sm = new SystemMessage(SystemMessage.ENTERED_THE_CLAN);
		    activeChar.sendPacket(sm);
		    
			//L2ClanMember[] members = clan.getMembers();
			PledgeShowMemberListAdd la = new PledgeShowMemberListAdd(activeChar);
			sm = new SystemMessage(SystemMessage.S1_HAS_JOINED_CLAN);
			sm.addString(activeChar.getName());
			
			clan.broadcastToOnlineMembers(la);
			clan.broadcastToOnlineMembers(sm);
			
			// this activates the clan tab on the new member
			activeChar.sendPacket(new PledgeShowMemberListAll(clan, activeChar));
		} 
		else
		{
			SystemMessage sm = new SystemMessage(SystemMessage.S1_REFUSED_TO_JOIN_CLAN);
			sm.addString(activeChar.getName());
			requestor.sendPacket(sm);
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
