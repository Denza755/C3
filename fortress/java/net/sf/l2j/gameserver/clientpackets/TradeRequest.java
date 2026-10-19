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
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2PcInstance;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.serverpackets.SendTradeRequest;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;

/**
 * 
 * This class ...
 * 
 * @version $Revision: 1.2.2.1.2.3 $ $Date: 2005/03/27 15:29:30 $
 */
public class TradeRequest extends ClientBasePacket
{
	private static final String TRADEREQUEST__C__15 = "[C] 15 TradeRequest";
	private final int _objectId;
	
	public TradeRequest(ByteBuffer buf, ClientThread client)
	{
		super(buf, client);
		_objectId = readD();
	}

	void runImpl()
	{
		L2PcInstance player = getClient().getActiveChar();
		if(player == null) return;
		L2Object target = L2World.findObject(_objectId);
		
        if (player.getPrivateStoreType() != 0)
        {
            player.sendPacket(new SystemMessage(SystemMessage.WHILE_OPERATING_A_PRIVATE_STORE_OR_WORKSHOP_YOU_CANNOT_DISCARD_DESTROY_OR_TRADE_AN_ITEM));
            return;
        }

        if (target == null || !(target instanceof L2PcInstance) || (target.getObjectId() == player.getObjectId()))
		{
			player.sendPacket(new SystemMessage(SystemMessage.TARGET_IS_INCORRECT));
			return;
		}
		
		L2PcInstance pcTarget = (L2PcInstance) target;
		
        if(pcTarget.getTradeRefusal())
        {
            player.sendMessage("Target are in trade refusal mode");
            return;
        }
        
		if (player.isTransactionInProgress())
		{
			player.sendPacket(new SystemMessage(SystemMessage.ALREADY_TRADING));
			return;
		}

		if (player.knowsObject(target) && !pcTarget.isTransactionInProgress()) 
		{
			pcTarget.setTransactionRequester(player);
			player.setTransactionRequester(pcTarget);
			pcTarget.sendPacket(new SendTradeRequest(player.getObjectId()));
			player.sendPacket(new SystemMessage(SystemMessage.REQUEST_S1_FOR_TRADE).addString(pcTarget.getName()));
		}
		else
		{
			player.sendPacket(new SystemMessage(SystemMessage.S1_IS_BUSY_PLEASE_TRY_AGAIN_LATER).addString(pcTarget.getName()));
		}
	} 
	
	public String getType()
	{
		return TRADEREQUEST__C__15;
	}
}
