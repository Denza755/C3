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

import net.sf.l2j.Config;
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
	
	private String _name;
	private final int _itemDistribution;
	
	public RequestJoinParty(ByteBuffer buf, ClientThread client)
	{
		super(buf, client);
		_name             = readS();
		_itemDistribution = readD();
	}

	void runImpl()
	{
		SystemMessage msg;
		L2PcInstance target	= L2World.getInstance().getPlayer(_name);
		
		L2PcInstance requestor = getClient().getActiveChar();

		if (requestor.isTransactionInProgress())
		{
			msg = new SystemMessage(SystemMessage.WAITING_FOR_REPLY);
			requestor.sendPacket(msg);
			if (Config.DEBUG) _log.fine("player already invited someone.");
			return;
		}

		if( target.isInParty()) {
			msg = new SystemMessage(SystemMessage.S1_IS_ALREADY_IN_PARTY);
			msg.addString(target.getName());
			requestor.sendPacket(msg);
			return;
		}

		if( target == requestor) {
			msg = new SystemMessage(SystemMessage.CANNOT_INVITE_YOURSELF);
			msg.addString(target.getName());
			requestor.sendPacket(msg);
			return;
		}
		
		if (!getClient().getActiveChar().isInParty())//asker has no party
		{
			createNewParty(getClient(), _itemDistribution, target, requestor);
		}
		else//asker has a party
		{
			addTargetToParty(getClient(), _itemDistribution, target, requestor);
		}
	}
	
	/**
	 * @param client
	 * @param itemDistribution
	 * @param target
	 * @param requestor
	 */
	private void addTargetToParty(ClientThread client, int itemDistribution, L2PcInstance target, L2PcInstance requestor)
	{
		SystemMessage msg;

		if(requestor.getParty().getMemberCount() >= 9) {
			requestor.sendPacket(new SystemMessage(SystemMessage.PARTY_FULL));
			return;
		}
		
		if(!requestor.getParty().isLeader(requestor)) {
			requestor.sendPacket(new SystemMessage(SystemMessage.ONLY_LEADER_CAN_INVITE));
			return;
		}
		
		if (target.knowsThePlayer( requestor )) 
		{
			// only continue if the client actually knowns the id
			
			if (!target.isTransactionInProgress()) 
			{
				target.setTransactionRequester(requestor);
				requestor.setTransactionRequester(target);
				
				AskJoinParty ask = new AskJoinParty( requestor.getName(), itemDistribution);
                target.sendPacket(ask);
				if (Config.DEBUG) _log.fine("sent out a party invitation to:"+target.getName());
				msg = new SystemMessage(SystemMessage.YOU_INVITED_S1_TO_PARTY);
				msg.addString(target.getName());
				requestor.sendPacket(msg);
			}
			else
			{
				msg = new SystemMessage(SystemMessage.S1_IS_BUSY_TRY_LATER);
				requestor.sendPacket(msg);
				_log.warning( requestor.getName() + " already received a party invitation");
			}
		}
		else
		{
			_log.warning(client.getActiveChar().getName()+ " invited someone who doesn't know him.");
		}
	}

	/**
	 * @param client
	 * @param itemDistribution
	 * @param target
	 * @param requestor
	 */
	private void createNewParty(ClientThread client, int itemDistribution, L2PcInstance target, L2PcInstance requestor)
	{
		SystemMessage msg;
		if(target.knowsThePlayer( requestor ))
		{
			if (!target.isTransactionInProgress())
			{
				requestor.setParty(new L2Party(requestor, (itemDistribution == 1)));
				
				target.setTransactionRequester(requestor);
				requestor.setTransactionRequester(target);
				AskJoinParty ask = new AskJoinParty( requestor.getName(), itemDistribution);
				target.sendPacket(ask);
				if (Config.DEBUG) _log.fine("sent out a party invitation to:"+target.getName());

				msg = new SystemMessage(SystemMessage.YOU_INVITED_S1_TO_PARTY);
				msg.addString(target.getName());
				requestor.sendPacket(msg);
			}
			else
			{
				msg = new SystemMessage(SystemMessage.S1_IS_BUSY_TRY_LATER);
				msg.addString(target.getName());
				requestor.sendPacket(msg);
				_log.warning( requestor.getName() + " already received a party invitation");
			}
		}
		else
		{
			if(target.knowsThePlayer( requestor ))
			{
				_log.warning(client.getActiveChar().getName()+ " invited someone who doesn't know him.");
			}
		}
	}

	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.clientpackets.ClientBasePacket#getType()
	 */
	public String getType()
	{
		return _C__29_REQUESTJOINPARTY;
	}
}
