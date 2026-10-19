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
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.PcInventory;
import net.sf.l2j.gameserver.model.Warehouse;
import net.sf.l2j.gameserver.serverpackets.ItemList;
import net.sf.l2j.gameserver.serverpackets.StatusUpdate;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;
//import net.sf.l2j.gameserver.serverpackets.InventoryUpdate;

/**
 * This class ...
 *
 * 32  SendWareHouseWithDrawList  cd (dd)
 *  
 * @version $Revision: 1.2.2.1.2.4 $ $Date: 2005/03/29 23:15:16 $
 */
public class SendWareHouseWithDrawList extends ClientBasePacket
{
	private static final String _C__32_SENDWAREHOUSEWITHDRAWLIST = "[C] 32 SendWareHouseWithDrawList";
	private static Logger _log = Logger.getLogger(SendWareHouseWithDrawList.class.getName());

	private final int _count;
	private final int[] _items;
	
	public SendWareHouseWithDrawList(ByteBuffer buf, ClientThread client)
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
		
        if (activeChar.isUsingClanWarehouse() && !activeChar.isClanLeader())
        {
            return;
        }

		int weight = 0;
		boolean needsSpace = true;
		int finalCount = activeChar.getInventory().getSize();
		L2ItemInstance[] items = new L2ItemInstance[_count];

		for (int i=0; i < _count; i++)
		{
			int itemId = _items[i * 2 + 0];
			int count  = _items[i * 2 + 1];
			L2ItemInstance inst = ItemTable.getInstance().createItem(itemId);
			inst.setCount(count);
			items[i] = inst;
			weight += items[i].getItem().getWeight() * count;
			if (inst.getItem().isStackable()
					&& activeChar.getInventory().getItemByItemId(itemId) != null)
				needsSpace = false;
			if (needsSpace)
			{
				if (inst.getItem().isStackable())
					finalCount += 1;
				else
					finalCount += count;
			}
		}
		
		if (finalCount > PcInventory.MAX_ITEMS)
		{
			for (int i = 0; i < items.length; i++)
			{
				L2World.getInstance().removeObject(items[i]);
			}
			SystemMessage sm = SystemMessage.sendString("Maximum item count reached in inventory.");
			activeChar.sendPacket(sm);
			return;
		}

		if (activeChar.getMaxLoad() - activeChar.getCurrentLoad() >= weight)
		{
			//InventoryUpdate updateForInventory = new InventoryUpdate();
            Warehouse warehouse2 = activeChar.isUsingClanWarehouse() ? activeChar.getClan().getWarehouse() : activeChar.getWarehouse();
			for (int i = 0; i < items.length; i++)
			{
                L2ItemInstance old = warehouse2.destroyItem(items[i].getItemId(), items[i].getCount());
				items[i].setEnchantLevel(old.getEnchantLevel());
				items[i].setPriceToBuy(old.getPriceToBuy());
				items[i].setPriceToSell(old.getPriceToSell());
				activeChar.getInventory().addItem(items[i]);
			}
						
			StatusUpdate su = new StatusUpdate(activeChar.getObjectId());
			su.addAttribute(StatusUpdate.CUR_LOAD, activeChar.getCurrentLoad());
			activeChar.sendPacket(su);

			ItemList il = new ItemList(activeChar, false);
			activeChar.sendPacket(il);			
		} 
		else
		{
			for (int i = 0; i < items.length; i++)
			{
				L2World.getInstance().removeObject(items[i]);
			}
			
			SystemMessage sm = new SystemMessage(SystemMessage.WEIGHT_LIMIT_EXCEEDED);
			activeChar.sendPacket(sm);
		}
	}

	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.clientpackets.ClientBasePacket#getType()
	 */
	public String getType()
	{
		return _C__32_SENDWAREHOUSEWITHDRAWLIST;
	}
}
