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

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.ClientThread;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.packets.serverpackets.SendTradeRequest;
import net.sf.l2j.gameserver.packets.serverpackets.SystemMessage;

import java.nio.ByteBuffer;

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

        if (player == null)
            return;

        // Alt game - Karma punishment
        if (!Config.ALT_GAME_KARMA_PLAYER_CAN_TRADE && player.getKarma() > 0)
            return;

        L2World world = L2World.getInstance();
		L2Object target = world.findObject(_objectId);
		
        if (player.getPrivateStoreType() != 0)
        {
            player.sendPacket(new SystemMessage(SystemMessage.CANNOT_TRADE_DISCARD_DROP_ITEM_WHILE_IN_SHOPMODE));
            return;
        }
	player.setEnchantScroll(null);

        if (target == null || !(target instanceof L2PcInstance) || (target.getObjectId() == player.getObjectId()))
		{
			player.sendPacket(new SystemMessage(SystemMessage.TARGET_IS_INCORRECT));
			return;
		}
		
        if(((L2PcInstance)target).getTradeRefusal())
        {
        	player.sendMessage("Target are in trade refusal mode");
        }
        
		if (player.isTransactionInProgress())
		{
			player.sendPacket(new SystemMessage(SystemMessage.ALREADY_TRADING));
			return;
		}

		L2PcInstance pcTarget = (L2PcInstance) target;
		
		if (player.getKnownList().knowsObject(target) 
				&& !pcTarget.isTransactionInProgress()) 
		{
			pcTarget.setTransactionRequester(player);
			player.setTransactionRequester(pcTarget);
			pcTarget.sendPacket(new SendTradeRequest(player.getObjectId()));
			player.sendPacket(new SystemMessage(SystemMessage.REQUEST_S1_FOR_TRADE).addString(pcTarget.getName()));
		}
		else
		{
			player.sendPacket(new SystemMessage(SystemMessage.S1_IS_BUSY_TRY_LATER).addString(pcTarget.getName()));
		}
	} 
	
	public String getType()
	{
		return TRADEREQUEST__C__15;
	}
}
