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
import net.sf.l2j.gameserver.ItemTable;
import net.sf.l2j.gameserver.model.L2FolkInstance;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2NpcInstance;
import net.sf.l2j.gameserver.model.L2PcInstance;
import net.sf.l2j.gameserver.model.Warehouse;
import net.sf.l2j.gameserver.serverpackets.ItemList;
import net.sf.l2j.gameserver.serverpackets.StatusUpdate;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;
//import net.sf.l2j.gameserver.serverpackets.InventoryUpdate;

/**
 * This class ...
 *
 * 31  SendWareHouseDepositList  cd (dd)
 *  
 * @version $Revision: 1.3.4.5 $ $Date: 2005/04/11 10:06:09 $
 */
public class SendWareHouseDepositList extends ClientBasePacket
{
	private static final String _C__31_SENDWAREHOUSEDEPOSITLIST = "[C] 31 SendWareHouseDepositList";
	private static Logger _log = Logger.getLogger(SendWareHouseDepositList.class.getName());

	private final int _count;
	private final int[] _items;
	
	public SendWareHouseDepositList(ByteBuffer buf, ClientThread client)
	{
		super(buf, client);
		_count = readD();
		_items = new int[_count * 2];
		for (int i=0; i < _count; i++)
		{
			int itemId = readD(); _items[i * 2 + 0] = itemId;
			int cnt    = readD(); _items[i * 2 + 1] = cnt;
		}
	}

	void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		L2FolkInstance warehouse = activeChar.getLastFolkNPC();
		if ((warehouse == null || activeChar.getDistance(warehouse.getX(), warehouse.getY()) > L2NpcInstance.INTERACTION_DISTANCE) && !activeChar.isGM())
			return;
		
		int price = 30;
		int neededMoney = _count * price;
		int currentMoney = activeChar.getAdena(); 

		if (neededMoney > currentMoney)
		{
			SystemMessage sm = new SystemMessage(279);
			sendPacket(sm);
			return;
		}
		
		L2ItemInstance[] items = new L2ItemInstance[_count];
		
		for (int i=0; i < _count; i++)
		{
			int itemId = _items[i * 2 + 0];
			int cnt    = _items[i * 2 + 1];
			L2ItemInstance inst = ItemTable.getInstance().createItem(itemId);
			inst.setCount(cnt);
			items[i] = inst;
		}
		
		//InventoryUpdate updateForInventory = new InventoryUpdate();
		neededMoney = 0; //this is moved in here because of that adena -_-; we don't want ppl to
						// pay 30a and store nothing if they coose to store adenas
        Warehouse warehouse2 = activeChar.isUsingClanWarehouse() ? activeChar.getClan().getWarehouse() : activeChar.getWarehouse();
		for (int i = 0; i < items.length; i++)
		{
			// recheck the count before we transfer the items, it may have changed meanwhile
			// FIXME: this could be combined with destroyItemByItemId() ?
			L2ItemInstance found = activeChar.getInventory().findItemByItemId(items[i].getItemId());
			if (found == null) continue;
			int realCount = items[i].getCount();
			if (realCount > found.getCount())
				realCount = found.getCount();
			// if it's adena, we should not delete the item. This will confuse _adena in PcInventory
			if (items[i].getItemId() == 57) {
				activeChar.reduceAdena(realCount);
			}
			else
			{
				L2ItemInstance old = activeChar.getInventory().destroyItemByItemId(items[i].getItemId(), realCount);
				items[i].setEnchantLevel(old.getEnchantLevel());
				items[i].setPriceToBuy(old.getPriceToBuy());
				items[i].setPriceToSell(old.getPriceToSell());
			}
			items[i].setCount(realCount);
			//_log.info("put item #"+items[i].getItemId()+", count "+realCount);
			warehouse2.addItem(items[i]);
			if (items[i].getItemId() != 57)
				neededMoney += price;
			//updateForInventory.addNewItem(items[i]);				
		}
		
		activeChar.reduceAdena(neededMoney);

		
		ItemList il = new ItemList(activeChar, false);
		sendPacket(il);
		
		// update current load status
		StatusUpdate su = new StatusUpdate(activeChar.getObjectId());
		su.addAttribute(StatusUpdate.CUR_LOAD, activeChar.getCurrentLoad());
		activeChar.sendPacket(su);
		
		//causes doubled items problem
		//con.sendPacket(updateForInventory);
	}

	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.clientpackets.ClientBasePacket#getType()
	 */
	public String getType()
	{
		return _C__31_SENDWAREHOUSEDEPOSITLIST;
	}
}
