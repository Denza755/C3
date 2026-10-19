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
import net.sf.l2j.gameserver.model.L2Party;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.packets.serverpackets.AskJoinParty;
import net.sf.l2j.gameserver.packets.serverpackets.SystemMessage;

import java.nio.ByteBuffer;
import java.util.logging.Logger;

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
	
	private int _id;
	private String _name = null;
	private final int _itemDistribution;
    private final int revision;
	
	public RequestJoinParty(ByteBuffer buf, ClientThread client)
	{
		super(buf, client);
        revision    = client.getRevision();
		if (revision >= 551)
		{
			_name = readS();
		}
		else
		{
			_id = readD();
		}
		_itemDistribution = readD();
	}

	void runImpl()
	{
		if (getClient().getActiveChar() == null)
		    return;
		L2PcInstance target	= null;
		if (revision < 551)
		{
			target = (L2PcInstance) L2World.getInstance().findObject(_id);
		}
        else
        {
            target  = L2World.getInstance().getPlayer(_name);
        }
		
        if(target == null)
        {
            getClient().getActiveChar().sendPacket(new SystemMessage(SystemMessage.TARGET_IS_INCORRECT));
            return;
        }
        
		L2PcInstance requestor = getClient().getActiveChar();

		if (requestor.isTransactionInProgress())
		{
			requestor.sendPacket(new SystemMessage(SystemMessage.WAITING_FOR_REPLY));
			return;
		}

		if( target.isInParty()) {
			requestor.sendPacket(new SystemMessage(SystemMessage.S1_IS_ALREADY_IN_PARTY).addString(target.getName()));
			return;
		}

		if( target == requestor) {
			requestor.sendPacket(new SystemMessage(SystemMessage.CANNOT_INVITE_YOURSELF));
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
		if(requestor.getParty().getMemberCount() >= 9) {
			requestor.sendPacket(new SystemMessage(SystemMessage.PARTY_FULL));
			return;
		}
		
		if(!requestor.getParty().isLeader(requestor)) {
			requestor.sendPacket(new SystemMessage(SystemMessage.ONLY_LEADER_CAN_INVITE));
			return;
		}
		
		if (target.getKnownList().knowsThePlayer( requestor )) 
		{
			// only continue if the client actually knowns the id
			
			if (!target.isTransactionInProgress()) 
			{
				target.setTransactionRequester(requestor);
				requestor.setTransactionRequester(target);
				
				target.sendPacket(new AskJoinParty( requestor.getName(), itemDistribution));
				requestor.sendPacket(new SystemMessage(SystemMessage.YOU_INVITED_S1_TO_PARTY).addString(target.getName()));
			}
			else
			{
				requestor.sendPacket(new SystemMessage(SystemMessage.S1_IS_BUSY_TRY_LATER).addString(target.getName()));
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
		if(target.getKnownList().knowsThePlayer( requestor ))
		{
			if (!target.isTransactionInProgress())
			{
				requestor.setParty(new L2Party(requestor, itemDistribution));
				
				target.setTransactionRequester(requestor);
				requestor.setTransactionRequester(target);
				target.sendPacket(new AskJoinParty( requestor.getName(), itemDistribution));
				requestor.sendPacket(new SystemMessage(SystemMessage.YOU_INVITED_S1_TO_PARTY).addString(target.getName()));
			}
			else
			{
				requestor.sendPacket(new SystemMessage(SystemMessage.S1_IS_BUSY_TRY_LATER).addString(target.getName()));
			}
		}
		else
		{
			if(target.getKnownList().knowsThePlayer( requestor ))
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
