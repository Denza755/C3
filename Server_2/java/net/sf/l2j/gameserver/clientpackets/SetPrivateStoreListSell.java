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
import java.util.ArrayList;
import java.util.logging.Logger;

import net.sf.l2j.gameserver.ClientThread;
import net.sf.l2j.gameserver.model.L2PcInstance;
import net.sf.l2j.gameserver.model.TradeItem;
import net.sf.l2j.gameserver.serverpackets.ChangeWaitType;
import net.sf.l2j.gameserver.serverpackets.PrivateStoreMsgSell;

/**
 * This class ...
 * 
 * @version $Revision: 1.2.2.1.2.5 $ $Date: 2005/03/27 15:29:30 $
 */
public class SetPrivateStoreListSell extends ClientBasePacket
{
	private static final String _C__74_SETPRIVATESTORELISTSELL = "[C] 74 SetPrivateStoreListSell";
	private static Logger _log = Logger.getLogger(SetPrivateStoreListSell.class.getName());

	private final int _count;
	private final int[] _items; // count * 3
	public SetPrivateStoreListSell(ByteBuffer buf, ClientThread client)
	{
		super( buf, client);
		_count = readD();
		_items = new int[_count * 3];
		for (int x = 0; x < _count ; x++)
		{
			int objectId = readD(); _items[x * 3 + 0] = objectId; 
			int cnt      = readD(); _items[x * 3 + 1] = cnt; 
			int price    = readD(); _items[x * 3 + 2] = price; 
		}
	}

	void runImpl()
	{
		L2PcInstance player = getClient().getActiveChar();
		//L2TradeList tradelist = player.getTradeList();
		TradeItem temp;
		player.setSellList(new ArrayList<TradeItem>());
		ArrayList<TradeItem> listsell = player.getSellList();
		
		
		for (int x = 0; x < _count ; x++)
		{
			temp = new TradeItem();
			
			int objectId = _items[x * 3 + 0]; 
			int cnt      = _items[x * 3 + 1]; 
			int price    = _items[x * 3 + 2]; 
            /*totalPrice   += price;
            if (totalPrice > player.getAdena())
            {
                listsell = null;
                player.sendPacket(SystemMessage.sendString("Not enough Adena."));
                return;
            }*/
			temp.setObjectId(objectId);
			temp.setCount(cnt);
			temp.setOwnersPrice(price);
			temp.setItemId(player.getInventory().getItemByObjectId(temp.getObjectId()).getItemId());
			listsell.add(temp);
		}
		
		if (_count == 0)
		{
			listsell = null;
		}
	
		if (_count!= 0)
		{
			player.setPrivateStoreType(L2PcInstance.STORE_PRIVATE_SELL);
			player.sendPacket(new ChangeWaitType (player,false));
			player.broadcastPacket(new ChangeWaitType (player,false));
			player.broadcastUserInfo();
			player.sendPacket(new PrivateStoreMsgSell(player));
			player.broadcastPacket(new PrivateStoreMsgSell(player));
			player.isInStoreMode(true);
			player.sitDown();
		}
		else 
		{
			player.setPrivateStoreType(L2PcInstance.STORE_PRIVATE_NONE);
			player.broadcastUserInfo();
			player.isInStoreMode(false);
			player.standUp();
			//send error no items sellected to sell
			//msg_begin	id=280	group=[popup]	msg=[You do not have any items to sell.]
		}
	}
	
	public String getType()
	{
		return _C__74_SETPRIVATESTORELISTSELL;
	}
}
