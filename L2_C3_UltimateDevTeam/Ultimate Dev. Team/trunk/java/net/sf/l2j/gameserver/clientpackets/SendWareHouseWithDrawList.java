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
import net.sf.l2j.gameserver.model.*;
import net.sf.l2j.gameserver.model.actor.instance.L2FolkInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2NpcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.packets.serverpackets.ItemList;
import net.sf.l2j.gameserver.packets.serverpackets.StatusUpdate;
import net.sf.l2j.gameserver.packets.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.tables.ItemTable;
import net.sf.l2j.gameserver.util.IllegalPlayerAction;
import net.sf.l2j.gameserver.util.Util;

import java.nio.ByteBuffer;
import java.util.logging.Logger;

/**
 * This class ...
 *
 * 32  SendWareHouseWithDrawList  cd (dd)
 * WootenGil rox :P
 * @version $Revision: 1.2.2.1.2.4 $ $Date: 2005/03/29 23:15:16 $
 */
public class SendWareHouseWithDrawList extends ClientBasePacket
{
	private static final String _C__32_SENDWAREHOUSEWITHDRAWLIST = "[C] 32 SendWareHouseWithDrawList";
	private static Logger _log = Logger.getLogger(SendWareHouseWithDrawList.class.getName());

	private int _count;
	private int[] _items;
	
	public SendWareHouseWithDrawList(ByteBuffer buf, ClientThread client)
	{
		super(buf, client);
		_count = readD();
		_items = new int[_count * 2];
		for (int i=0; i < _count; i++)
		{
			int objectId = readD(); _items[i * 2 + 0] = objectId;
			long cnt    = readD(); 
			if (cnt > Integer.MAX_VALUE || cnt < 0)
			{
			    _count = 0; _items = null;
			    return;
			}
			_items[i * 2 + 1] = (int)cnt;
		}
	}

	void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
        if (activeChar == null)
            return;
        
		L2FolkInstance warehouse = activeChar.getLastFolkNPC();
		if ((warehouse == null || activeChar.getDistance(warehouse.getX(), warehouse.getY()) > L2NpcInstance.INTERACTION_DISTANCE) && !activeChar.isGM())
			return;
		
        if (activeChar.isUsingClanWarehouse() && !((activeChar.getClanPrivileges() & L2Clan.CP_CL_VIEW_WAREHOUSE) == L2Clan.CP_CL_VIEW_WAREHOUSE))
        {
            return;
        }
        
		int weight = 0;
		boolean needsSpace = true;
		int finalCount = activeChar.getInventory().getSize();
		L2ItemInstance[] items = new L2ItemInstance[_count];

		for (int i=0; i < _count; i++)
		{
			int objectId = _items[i * 2 + 0];
			int count  = _items[i * 2 + 1];
            
            L2ItemInstance old = L2ItemInstance.restoreFromDb(objectId);

            if(old.getCount() < count)
            {
                Util.handleIllegalPlayerAction(activeChar,
                                               "[SendWarehouseWithdrawList] player "+activeChar.getName()+" tried to retrive more items than he has, ban this player!",
                                               _log,
                                               IllegalPlayerAction.PUNISH_KICK);
                return;
            }

			L2ItemInstance inst = ItemTable.getInstance().createItem(old.getItemId());
			
			// FIXME: item get ban for who want to use it
            /*if((!inst.isStackable() && count > 1 && (inst.getItemType().name() != "recipe")) || (!old.isStackable() && count > 1 && (old.getItemType().name() != "recipe")))
            {
                Util.handleIllegalPlayerAction(activeChar,
                                               "[SendWarehouseWithdrawList] count > 1 but item is not stackable! oid: "+objectId+" owner: "+activeChar.getName(),
                                               _log,
                                               IllegalPlayerAction.PUNISH_BROADCAST);
                return;
            }*/
            
            inst.setCount(count);
			items[i] = inst;
			weight += items[i].getItem().getWeight() * count;
			if (inst.getItem().isStackable()
					&& activeChar.getInventory().getItemByObjectId(objectId) != null)
				needsSpace = false;
			if (needsSpace)
			{
				if (inst.getItem().isStackable())
					finalCount += 1;
				else
					finalCount += count;
			}
		}
		
		if (!activeChar.getInventory().validateCapacity(finalCount))
		{
			for (int i = 0; i < items.length; i++)
			{
				L2World.getInstance().removeObject(items[i]);
			}
			SystemMessage sm = new SystemMessage(SystemMessage.SLOTS_FULL);
			activeChar.sendPacket(sm);
			return;
		}

		if (activeChar.getMaxLoad() - activeChar.getCurrentLoad() >= weight)
		{
            
            Warehouse warehouse2 = null;
            if (!activeChar.isUsingClanWarehouse() && !activeChar.isUsingFreight())
                warehouse2 = activeChar.getWarehouse();
            else if (activeChar.isUsingClanWarehouse() && !activeChar.isUsingFreight())
            {
               	ClanWarehousePool.getInstance().AddWork(activeChar,items);
               	return;
            }
            else if (!activeChar.isUsingClanWarehouse() && activeChar.isUsingFreight())
                warehouse2 = activeChar.getFreight();
            else
            {
                // Something went wrong!
                _log.warning("Error retrieving a warehouse object for char " + activeChar.getName() + " - using freight: " + activeChar.isUsingFreight());
                return;     
            }
            
			for (int i = 0; i < items.length; i++)
			{
                L2ItemInstance old = warehouse2.destroyItem(items[i].getItemId(), items[i].getCount());
                if(old == null)
                {
                    continue;
                }

                
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
