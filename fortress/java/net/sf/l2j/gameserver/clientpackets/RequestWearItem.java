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
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.l2j.gameserver.ClientScheduler;
import net.sf.l2j.gameserver.ClientThread;
import net.sf.l2j.gameserver.ItemTable;
import net.sf.l2j.gameserver.model.L2FolkInstance;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2MerchantInstance;
import net.sf.l2j.gameserver.model.L2NpcInstance;
import net.sf.l2j.gameserver.model.L2PcInstance;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.serverpackets.ItemList;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;

/**
 * This class ...
 * 
 * @version $Revision: 1.12.4.4 $ $Date: 2005/03/27 15:29:30 $
 */
public class RequestWearItem extends ClientBasePacket
{
	private static final String _C__C6_REQUESTWEARITEM = "[C] C6 RequestWearItem";
    protected static Logger _log = Logger.getLogger(RequestWearItem.class.getName());

	protected Future _removeWearItemsTask;
	
    @SuppressWarnings("unused")
	private final int _unknow;
	private final int _listId;
	private final int _count;
	private final int[] _items; // count*2
    protected final L2PcInstance _cha;

	class RemoveWearItemsTask implements Runnable
	{
	    public void run()
	    {
		try
		{
		 	L2ItemInstance[] items = _cha.getInventory().getItems();
		  	for (L2ItemInstance i : items)
		    	if (i.isWear())
		    	{
					if (i.isEquipped())
			    		_cha.getInventory().unEquipItemInSlotAndRecord(i.getEquipSlot());
					L2World.removeObject(_cha.getInventory().destroyItem(i.getObjectId(),1));
		    	}
			_cha.broadcastUserInfo();

			_cha.sendMessage("Trying-on mode has ended.");
			sendPacket(new ItemList(_cha, false));
		
		} catch (Throwable e){
		    _log.log(Level.SEVERE, "", e);
		}
	    }
	}

	public RequestWearItem(ByteBuffer buf, ClientThread client)
	{
		super(buf, client);
		_cha = getClient().getActiveChar();
		_unknow = readD();
		_listId = readD();
		int tempCount = readD();
		if(tempCount * 2 < 0)
		{
		    _count = 0;
		}
		else
		{
		    _count = tempCount;
		}
//		_items = new int[_count * 2];
		_items = new int[_count];
		for (int i = 0; i < _count; i++)
		{
			int itemId   = readD(); _items[i] = itemId;
//			int cnt      = readD(); _items[i * 2 + 1] = cnt;
		}
	}

	void runImpl()
	{
		L2PcInstance activeChar = _cha;
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
		if(_count < 1 || _listId >= 1000000)
		{
			sendPacket(new ActionFailed());
			return;
		}
        
		ArrayList<L2ItemInstance> items = new ArrayList<L2ItemInstance>(_count);
		for (int i=0; i < _count; i++)
		{
			int itemId = _items[i];
			int cnt    = 1;
			L2ItemInstance inst = ItemTable.getInstance().createItem(itemId);
			inst.setCount(cnt);
			items.add(inst);
		}

		//TODO check if valid buylist, stackable items ?
		
		long neededMoney = 0;
		int finalLoad = activeChar.getCurrentLoad();
		int finalCount = activeChar.getInventory().getSize();
		int needsSpace = 2;
		int weight = 0;
		long currentMoney = activeChar.getAdena();

		for (L2ItemInstance item : items)
		{
			int itemId = item.getItemId();
			int cnt = item.getCount();
			int price;
			if (item.getItem().isStackable())
			{
				needsSpace = 1;
				if (activeChar.getInventory().getItemByItemId(itemId) != null)
					needsSpace = 0;
			}
            //L2TradeList list = TradeController.getInstance().getBuyList(_listId);
			price = 10;
			weight = item.getItem().getWeight();
			neededMoney += cnt * price;
            if (neededMoney > Integer.MAX_VALUE)
            {
                _log.warning("Warning!! Character "+activeChar.getName()+" of account "+activeChar.getAccountName()+" tried to purchase over "+Integer.MAX_VALUE+" adena worth of goods.");
                return;
            }
			finalLoad += cnt * weight;
			if (needsSpace == 2)
				finalCount += cnt;
			else if (needsSpace == 1)
				finalCount += 1;
		}

		if ((neededMoney > currentMoney) || (neededMoney < 0) || (currentMoney <= 0))
		{
			sendPacket(new SystemMessage(SystemMessage.YOU_DO_NOT_HAVE_ENOUGH_ADENA));
			for (L2ItemInstance i : items)
				L2World.removeObject(i);
			return;
		}

		if (finalLoad > activeChar.getMaxLoad())
		{
			sendPacket(new SystemMessage(SystemMessage.YOU_HAVE_EXCEEDED_THE_WEIGHT_LIMIT));
			for (L2ItemInstance i : items)
				L2World.removeObject(i);
			return;
		}

		activeChar.reduceAdena((int)neededMoney);
		
		sendPacket(new SystemMessage(SystemMessage.S1_ADENA_DISAPPEARED).addNumber((int)neededMoney));
		
		for (L2ItemInstance item : items)
		{
			item.setWear(true);
			activeChar.getInventory().addItem(item);
			activeChar.getInventory().equipItemAndRecord(item);
		}
		activeChar.broadcastUserInfo();

		sendPacket(new ItemList(activeChar, false));
		
		if (_removeWearItemsTask == null)
		    _removeWearItemsTask=ClientScheduler.getInstance().scheduleLow(new RemoveWearItemsTask(), 10000);
	}

	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.clientpackets.ClientBasePacket#getType()
	 */
	public String getType()
	{
		return _C__C6_REQUESTWEARITEM;
	}
}
