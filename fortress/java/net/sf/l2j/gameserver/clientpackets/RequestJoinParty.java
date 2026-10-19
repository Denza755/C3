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
import net.sf.l2j.gameserver.model.L2Party;
import net.sf.l2j.gameserver.model.L2PcInstance;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.serverpackets.AskJoinParty;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;

/**
 *  sample
 *  29 
 *  42 00 00 10 
 *  01 00 00 00
 * 
 *  format  cdd
 * 
 * 
 * @version $Revision: 1.7.4.4 $ $Date: 2005/03/27 15:29:30 $
 */
public class RequestJoinParty extends ClientBasePacket
{
	private static final String _C__29_REQUESTJOINPARTY = "[C] 29 RequestJoinParty";
	private static Logger _log = Logger.getLogger(RequestJoinParty.class.getName());
	
	private final String _name;
	private final int _itemDistribution;
	
	public RequestJoinParty(ByteBuffer buf, ClientThread client)
	{
		super(buf, client);
		_name = readS();
		_itemDistribution = readD();
	}

	void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if(activeChar == null) return;
		L2PcInstance target	= L2World.getPlayer(_name);

		if(target == null || target == activeChar)
			return;

		if(activeChar.isTransactionInProgress())
		{
			activeChar.sendPacket(new SystemMessage(SystemMessage.WAITING_FOR_ANOTHER_REPLY));
			return;
		}

		if(target.isInParty()) 
		{
			activeChar.sendPacket(new SystemMessage(SystemMessage.S1_IS_A_MEMBER_OF_ANOTHER_PARTY_AND_CANNOT_BE_INVITED).addString(target.getName()));
			return;
		}

		
		if (!activeChar.isInParty())
			createNewParty(getClient(), _itemDistribution, target, activeChar);
		else
			addTargetToParty(getClient(), _itemDistribution, target, activeChar);
	}
	
	/**
	 * @param client
	 * @param itemDistribution
	 * @param target
	 * @param activeChar
	 */
	private void addTargetToParty(ClientThread client, int itemDistribution, L2PcInstance target, L2PcInstance activeChar)
	{
		if(activeChar.getParty().getMemberCount() >= 9)
		{
			activeChar.sendPacket(new SystemMessage(SystemMessage.PARTY_IS_FULL));
			return;
		}

		if(!activeChar.getParty().isLeader(activeChar))
		{
			activeChar.sendPacket(new SystemMessage(SystemMessage.ONLY_THE_LEADER_CAN_GIVE_OUT_INVITATIONS));
			return;
		}

		if(target.knowsThePlayer(activeChar))
		{
			if(!target.isTransactionInProgress())
			{
				target.setTransactionRequester(activeChar);
				activeChar.setTransactionRequester(target);

				target.sendPacket(new AskJoinParty(activeChar.getName(), itemDistribution));
				activeChar.sendPacket(new SystemMessage(SystemMessage.YOU_HAVE_INVITED_S1_TO_YOUR_PARTY).addString(target.getName()));
			}
			else
				activeChar.sendPacket(new SystemMessage(SystemMessage.S1_IS_BUSY_PLEASE_TRY_AGAIN_LATER).addString(target.getName()));
		}
		else
			_log.warning(client.getActiveChar().getName() + " invited someone who doesn't know him.");
	}

	/**
	 * @param client
	 * @param itemDistribution
	 * @param target
	 * @param requestor
	 */
	private void createNewParty(ClientThread client, int itemDistribution, L2PcInstance target, L2PcInstance requestor)
	{
		if(target.knowsThePlayer(requestor))
		{
			if(!target.isTransactionInProgress())
			{
				requestor.setParty(new L2Party(requestor, itemDistribution));

				target.setTransactionRequester(requestor);
				requestor.setTransactionRequester(target);
				target.sendPacket(new AskJoinParty(requestor.getName(), itemDistribution));
				requestor.sendPacket(new SystemMessage(SystemMessage.YOU_HAVE_INVITED_S1_TO_YOUR_PARTY).addString(target.getName()));
			}
			else
				requestor.sendPacket(new SystemMessage(SystemMessage.S1_IS_BUSY_PLEASE_TRY_AGAIN_LATER).addString(target.getName()));
		}
		else
			_log.warning(client.getActiveChar().getName() + " invited someone who doesn't know him.");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.l2j.gameserver.clientpackets.ClientBasePacket#getType()
	 */
	public String getType()
	{
		return _C__29_REQUESTJOINPARTY;
	}
}
