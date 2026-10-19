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
import net.sf.l2j.gameserver.ItemTable;
import net.sf.l2j.gameserver.TradeController;
import net.sf.l2j.gameserver.model.L2FolkInstance;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2MerchantInstance;
import net.sf.l2j.gameserver.model.L2NpcInstance;
import net.sf.l2j.gameserver.model.L2PcInstance;
import net.sf.l2j.gameserver.model.L2TradeList;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.PcInventory;
import net.sf.l2j.gameserver.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.serverpackets.ItemList;
import net.sf.l2j.gameserver.serverpackets.StatusUpdate;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;

/**
 * This class ...
 * 
 * @version $Revision: 1.12.4.4 $ $Date: 2005/03/27 15:29:30 $
 */
public class RequestBuyItem extends ClientBasePacket
{
	private static final String _C__1F_REQUESTBUYITEM = "[C] 1F RequestBuyItem";
	private static Logger _log = Logger.getLogger(RequestBuyItem.class.getName());

	private final int _listId;
	private final int _count;
	private final int[] _items; // count*2
	/**
	 * packet type id 0x1f
	 * 
	 * sample
	 * 
	 * 1f
	 * 44 22 02 01		// list id
	 * 02 00 00 00		// items to buy
	 * 
	 * 27 07 00 00		// item id
	 * 06 00 00 00		// count
	 * 
	 * 83 06 00 00
	 * 01 00 00 00
	 * 
	 * format:		cdd (dd) 
	 * @param decrypt
	 */
	public RequestBuyItem(ByteBuffer buf, ClientThread client)
	{
		super(buf, client);
		_listId = readD();
		_count = readD();
		_items = new int[_count * 2];
		for (int i = 0; i < _count; i++)
		{
			int itemId   = readD(); _items[i * 2 + 0] = itemId;
			int cnt      = readD(); _items[i * 2 + 1] = cnt;
		}
	}

	void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		L2FolkInstance folk = activeChar.getLastFolkNPC();
		if ((!(folk instanceof L2MerchantInstance) || activeChar.getDistance(folk.getX(), folk.getY()) > L2NpcInstance.INTERACTION_DISTANCE) && !activeChar.isGM())
			return;
               L2MerchantInstance merchant = null;
               // prevent cast exception and allow GMs use gmshop
               if (folk instanceof L2MerchantInstance)
                       merchant = (L2MerchantInstance)folk;
		if (_listId > 1000000) // lease
		{
			if (merchant.getTemplate().npcId != _listId-1000000)
			{
				sendPacket(new ActionFailed());
				return;
			}
		}
		
		ArrayList<L2ItemInstance> items = new ArrayList<L2ItemInstance>(_count);
		for (int i=0; i < _count; i++)
		{
			int itemId = _items[i * 2 + 0];
			int cnt    = _items[i * 2 + 1];
			L2ItemInstance inst = ItemTable.getInstance().createItem(itemId);
			inst.setCount(cnt);
			items.add(inst);
		}

		//TODO check if valid buylist, stackable items ?
		
		double neededMoney = 0;
		int finalLoad = activeChar.getCurrentLoad();
		int finalCount = activeChar.getInventory().getSize();
		int needsSpace = 2;
		int weight = 0;
		long currentMoney = activeChar.getAdena();

		for (int i = 0; i < items.size(); i++)
		{
			int itemId = items.get(i).getItemId();
			int cnt = items.get(i).getCount();
			int price;
			if (items.get(i).getItem().isStackable())
			{
				needsSpace = 1;
				if (activeChar.getInventory().getItemByItemId(itemId) != null)
					needsSpace = 0;
			}
			if (_listId < 1000000) {
				L2TradeList list = TradeController.getInstance().getBuyList(_listId);
				price = list.getPriceForItemId(itemId);
				weight = items.get(i).getItem().getWeight();
				if (price < 0)
				{
					_log.warning("ERROR, no price found .. wrong buylist ??");
					items.remove(i);
					continue;
				}
			} else {
				L2ItemInstance li = merchant.findLeaseItem(itemId, 0);
				if (li == null || li.getCount() < cnt) {
					cnt = li.getCount();
					if (cnt <= 0) {
						items.remove(i);
						continue;
					}
					items.get(i).setCount(cnt);
				}
				price = li.getPriceToSell(); // lease holder sells the item
				weight = li.getItem().getWeight();
			}
			neededMoney += cnt * price;
			finalLoad += cnt * weight;
			if (needsSpace == 2)
				finalCount += cnt;
			else if (needsSpace == 1)
				finalCount += 1;
		}

		if ((neededMoney > currentMoney) || (neededMoney < 0) || (currentMoney <= 0))
		{
			SystemMessage sm = new SystemMessage(SystemMessage.YOU_NOT_ENOUGH_ADENA);
			sendPacket(sm);
			for (L2ItemInstance i : items)
				L2World.getInstance().removeObject(i);
			return;
		}

		if (finalLoad > activeChar.getMaxLoad())
		{
			SystemMessage sm = new SystemMessage(SystemMessage.WEIGHT_LIMIT_EXCEEDED);
			sendPacket(sm);
			for (L2ItemInstance i : items)
				L2World.getInstance().removeObject(i);
			return;
		}

		if (finalCount > PcInventory.MAX_ITEMS)
		{
			SystemMessage sm = SystemMessage.sendString("Maximum item count reached in inventory.");
			sendPacket(sm);
			for (L2ItemInstance i : items)
				L2World.getInstance().removeObject(i);
			return;
		}

		activeChar.reduceAdena((int)neededMoney);
		for (int i = 0; i < items.size(); i++)
		{
			L2ItemInstance item = items.get(i);
			if (_listId < 1000000) {
				activeChar.getInventory().addItem(item);
			} else {
				L2ItemInstance li = merchant.findLeaseItem(item.getItemId(), 0);
				if (li == null)
					continue;
				if (li.getCount() < item.getCount())
					item.setCount(li.getCount());
				li.setCount(li.getCount() - item.getCount());
				li.updateDatabase();
				int price = item.getCount() + li.getPriceToSell();
				L2ItemInstance la = merchant.getLeaseAdena();
				la.setCount(la.getCount() + price);
				la.updateDatabase();
				activeChar.getInventory().addItem(item);
				item.updateDatabase();
			}
		}

		//TODO only send Inventory update instead of full list !
		ItemList il = new ItemList(activeChar, false);
		sendPacket(il);
		
		StatusUpdate su = new StatusUpdate(activeChar.getObjectId());
		su.addAttribute(StatusUpdate.CUR_LOAD, activeChar.getCurrentLoad());
		activeChar.sendPacket(su);
	}

	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.clientpackets.ClientBasePacket#getType()
	 */
	public String getType()
	{
		return _C__1F_REQUESTBUYITEM;
	}
}
