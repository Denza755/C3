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
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2PcInstance;
import net.sf.l2j.gameserver.model.L2TradeList;
import net.sf.l2j.gameserver.serverpackets.SendTradeDone;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.serverpackets.TradeOtherAdd;
import net.sf.l2j.gameserver.serverpackets.TradeOwnAdd;

/**
 * This class ...
 * 
 * @version $Revision: 1.5.2.2.2.5 $ $Date: 2005/03/27 15:29:29 $
 */
public class AddTradeItem  extends ClientBasePacket
{
	private static final String _C__16_ADDTRADEITEM = "[C] 16 AddTradeItem";
	//private static Logger _log = Logger.getLogger(AddTradeItem.class.getName());
	
	private final int _tradeId;
	private final int _objectId;
	private final int _amount;

	public AddTradeItem(ByteBuffer buf, ClientThread client)
	{
		super(buf, client);
		_tradeId  = readD();
		_objectId = readD();
		_amount   = readD();
	}

	void runImpl()
	{
		L2PcInstance player = getClient().getActiveChar();
		L2PcInstance requestor = player.getTransactionRequester();
		
        //FIXME: requestor != null temp fix for null pointer
		if (requestor != null && requestor.getTransactionRequester() != null)
		{
			L2TradeList playerItemList = player.getTradeList();
			
			//TODO add an option to configure server trade behavior. org.mode or improved mode... now only improved mode is active
			// both players have a accept again after a change
			player.getTradeList().setConfirmedTrade(false);
			requestor.getTradeList().setConfirmedTrade(false);
			
			if (playerItemList.getItems().size() > 0)
			{
				if (!playerItemList.contains(_objectId))//is not item already in list
				{
					L2ItemInstance inst = player.getInventory().getItemByObjectId(_objectId); 
					int objectId = inst.getObjectId();
					L2ItemInstance temp = new L2ItemInstance(objectId, inst.getItem());
					temp.setCount(_amount);
					temp.setEnchantLevel(inst.getEnchantLevel());
					playerItemList.addItem(temp);
					
					player.sendPacket(new TradeOwnAdd(player.getInventory().getItemByObjectId(_objectId),_amount));
					requestor.sendPacket(new TradeOtherAdd(player.getInventory().getItemByObjectId(_objectId),_amount));
				}
				else
				{
					int InvItemCount = player.getInventory().getItemByObjectId(_objectId).getCount();
					L2ItemInstance tempTradeItem = playerItemList.getItem(_objectId);
					if  (!(InvItemCount == tempTradeItem.getCount()))
					{
						if ((_amount + tempTradeItem.getCount()) >=  InvItemCount)
						{
							tempTradeItem.setCount(InvItemCount);
							player.sendPacket(new TradeOwnAdd(player.getInventory().getItemByObjectId(_objectId),_amount));
							requestor.sendPacket(new TradeOtherAdd(player.getInventory().getItemByObjectId(_objectId),_amount));
						}
						else
						{
							tempTradeItem.setCount(_amount + tempTradeItem.getCount());
							player.sendPacket(new TradeOwnAdd(player.getInventory().getItemByObjectId(_objectId),_amount));
							requestor.sendPacket(new TradeOtherAdd(player.getInventory().getItemByObjectId(_objectId),_amount));
						}
					}
				}
			}
			else
			{
                L2ItemInstance temp2 = player.getInventory().getItemByObjectId(_objectId);
				L2ItemInstance temp = new L2ItemInstance(_objectId, temp2.getItem());
				temp.setCount(_amount);
				temp.setEnchantLevel(temp2.getEnchantLevel());
				playerItemList.addItem(temp);
                
				player.sendPacket(new TradeOwnAdd(temp2,_amount));
				requestor.sendPacket(new TradeOtherAdd(temp2,_amount));
			}
		}
		else
		{
			// trade partner logged off. trade is canceld
			player.sendPacket(new SendTradeDone(0));
			SystemMessage msg = new SystemMessage(SystemMessage.TARGET_IS_NOT_FOUND_IN_THE_GAME);
			player.sendPacket(msg);
			player.setTransactionRequester(null);
			requestor.getTradeList().getItems().clear();
			player.getTradeList().getItems().clear();
		}
	}
	
	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.clientpackets.ClientBasePacket#getType()
	 */
	public String getType()
	{
		return _C__16_ADDTRADEITEM;
	}
}
