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

import javolution.util.FastList;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.ClientThread;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.TradeItem;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.base.Race;
import net.sf.l2j.gameserver.packets.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.packets.serverpackets.ChangeWaitType;
import net.sf.l2j.gameserver.packets.serverpackets.PrivateStoreMsgSell;
import net.sf.l2j.gameserver.packets.serverpackets.SystemMessage;

import java.nio.ByteBuffer;
import java.util.List;

/**
 * This class ...
 * 
 * @version $Revision: 1.2.2.1.2.5 $ $Date: 2005/03/27 15:29:30 $
 */
public class SetPrivateStoreListSell extends ClientBasePacket
{
	private static final String _C__74_SETPRIVATESTORELISTSELL = "[C] 74 SetPrivateStoreListSell";

	private int _count;
	@SuppressWarnings("unused")
	private int _unknow;
	private int[] _items; // count * 3
	public SetPrivateStoreListSell(ByteBuffer buf, ClientThread client)
	{
		super( buf, client);
	    _unknow = readD();
		_count = readD();
		_items = new int[_count * 3];
		for (int x = 0; x < _count ; x++)
		{
			int objectId = readD(); _items[x * 3 + 0] = objectId; 
			long cnt      = readD(); 
			if (cnt > Integer.MAX_VALUE || cnt < 0)
			{
			    _count = 0; _items = null;
			    return;
			}
			_items[x * 3 + 1] = (int)cnt; 
			int price    = readD(); _items[x * 3 + 2] = price; 
		}
	}

	void runImpl()
	{
		L2PcInstance player = getClient().getActiveChar();
		if (player == null)
		    return;
        
        if (Config.GM_DISABLE_TRANSACTION && player.getAccessLevel() >= Config.GM_TRANSACTION_MIN && player.getAccessLevel() <= Config.GM_TRANSACTION_MAX)
        {
        	player.sendMessage("Transactions are disable for your Access Level");
        }
        
		//L2TradeList tradelist = player.getTradeList();
		TradeItem temp;
		player.setSellList(new FastList<TradeItem>());
        List<TradeItem> listsell = player.getSellList();
        
        int maxSlots = 0;
        if (player.getRace() == Race.dwarf)
            maxSlots = Config.MAX_PVTSTORE_SLOTS_DWARF;
        else
            maxSlots = Config.MAX_PVTSTORE_SLOTS_OTHER;
        
        // Check maximum number of allowed slots for pvt shops
        if (_count > maxSlots)
        {
            player.sendPacket(new SystemMessage(SystemMessage.YOU_HAVE_EXCEEDED_QUANTITY_THAT_CAN_BE_INPUTTED));
            player.sendPacket(new ActionFailed());
            
            player.setPrivateStoreType(L2PcInstance.STORE_PRIVATE_NONE);
            player.broadcastUserInfo();
            player.isInStoreMode(false);
            player.standUp();
            
            return;
        }
		
		for (int x = 0; x < _count ; x++)
		{
			temp = new TradeItem();
			
			int objectId = _items[x * 3 + 0]; 
			int cnt      = _items[x * 3 + 1]; 
			int price    = _items[x * 3 + 2]; 

			temp.setObjectId(objectId);

			L2ItemInstance itemToSell = player.getInventory().getItemByObjectId(objectId);

			if (itemToSell == null)
				continue;

			if (itemToSell.getItemId() == 57)
				continue;

			if (itemToSell.isWear())
				return;
			
            // If player sells the enchant scroll he is using, desactivate it
            if(player.getEnchantScroll() != null && itemToSell.getObjectId() == player.getEnchantScroll().getObjectId())
            {
                player.setEnchantScroll(null);
            }
            
			if (cnt > itemToSell.getCount())
				cnt = itemToSell.getCount();

			temp.setCount(cnt);
			temp.setOwnersPrice(price);
			temp.setItemId(itemToSell.getItemId());
			listsell.add(temp);
		}
		
		if (_count == 0)
		{
			listsell = null;
		}
	
		if (_count!= 0)
		{
			player.setPrivateStoreType(L2PcInstance.STORE_PRIVATE_SELL);
			player.broadcastPacket(new ChangeWaitType (player,ChangeWaitType.WT_SITTING));
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
		}
	}
	
	public String getType()
	{
		return _C__74_SETPRIVATESTORELISTSELL;
	}
}
