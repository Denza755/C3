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
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.Warehouse;
import net.sf.l2j.gameserver.model.actor.instance.L2FolkInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2NpcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.base.Race;
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
 * 31  SendWareHouseDepositList  cd (dd)
 *  
 * @version $Revision: 1.3.4.5 $ $Date: 2005/04/11 10:06:09 $
 */
public class SendWareHouseDepositList extends ClientBasePacket
{
	private static final String _C__31_SENDWAREHOUSEDEPOSITLIST = "[C] 31 SendWareHouseDepositList";
	private static Logger _log = Logger.getLogger(SendWareHouseDepositList.class.getName());

	private int _count;
	private int[] _items;
	
	public SendWareHouseDepositList(ByteBuffer buf, ClientThread client)
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
        
        if ((activeChar.isUsingFreight() || activeChar.isUsingClanWarehouse()) && Config.GM_DISABLE_TRANSACTION && activeChar.getAccessLevel() >= Config.GM_TRANSACTION_MIN && activeChar.getAccessLevel() <= Config.GM_TRANSACTION_MAX)
        {
        	activeChar.sendMessage("Transactions are disable for your Access Level");
        }

        // Alt game - Karma punishment
        if (!Config.ALT_GAME_KARMA_PLAYER_CAN_USE_WAREHOUSE && activeChar.getKarma() > 0)
            return;
        
		L2FolkInstance warehouse = activeChar.getLastFolkNPC();
		if ((warehouse == null || activeChar.getDistance(warehouse.getX(), warehouse.getY()) > L2NpcInstance.INTERACTION_DISTANCE) && !activeChar.isGM())
			return;
		
        int price;
        
        if (activeChar.isUsingFreight())
            price = Config.ALT_GAME_FREIGHT_PRICE;  // Freight price per item slot
        else
            price = 30;                             // Normal price per item slot
        
		int neededMoney = 0;
		int currentMoney = activeChar.getAdena(); 
        L2ItemInstance adenaitem = activeChar.getInventory().getAdenaInstance();
        int itemsLimit = 0;

        int[] objectIds = new int[_count];
        int[] counts = new int[_count];
		for (int i=0; i < _count; i++)
		{
			objectIds[i] = _items[i * 2 + 0];
			counts[i]  = _items[i * 2 + 1];
            
            // Calculate needed money
            if (objectIds[i] == adenaitem.getObjectId())
                neededMoney += counts[i];
            neededMoney += price;
		}
		
        // Check for needed money
        if (neededMoney > currentMoney)
        {
            SystemMessage sm = new SystemMessage(279);
            sendPacket(sm);
            return;
        }
        
        L2PcInstance destChar = null;
        Warehouse warehouse2 = null;
        if (!activeChar.isUsingClanWarehouse() && !activeChar.isUsingFreight())
        {
            if (activeChar.getRace() == Race.dwarf)
                itemsLimit = Config.WAREHOUSE_SLOTS_DWARF;
            else
                itemsLimit = Config.WAREHOUSE_SLOTS_NO_DWARF;
            
            warehouse2 = activeChar.getWarehouse();
        }
        else if (activeChar.isUsingClanWarehouse() && !activeChar.isUsingFreight())
        {
            itemsLimit = Config.WAREHOUSE_SLOTS_CLAN;
            warehouse2 = activeChar.getClan().getWarehouse();
        }
        else if (!activeChar.isUsingClanWarehouse() && activeChar.isUsingFreight())
        {
            destChar = L2PcInstance.load(activeChar.getFreightId());
            if (destChar == null)
            {
                // Something went wrong!
                if (Config.DEBUG)
                    _log.warning("Error retrieving a warehouse object for char " + activeChar.getName() + " - using freight: " + activeChar.isUsingFreight());
                return;     
            }
            itemsLimit = Config.FREIGHT_SLOTS;
            warehouse2 = destChar.getFreight();
            warehouse2.setLocationId(activeChar);
        }
        else
        {
            // Something went wrong!
            if (Config.DEBUG)
                _log.warning("Error retrieving a warehouse object for char " + activeChar.getName() + " - using freight: " + activeChar.isUsingFreight());
            return;     
        }
        int stackable = 0;
        for(int i = 0; i < objectIds.length; i++)
        {
        	L2ItemInstance temp = warehouse2.findItemId(activeChar.getInventory().getItemByObjectId(objectIds[i]).getItemId());
        	if(temp != null && temp.isStackable())
        		stackable++;
        }
        
        // Item Max Limit Check 
        if ((objectIds.length + warehouse2.listItems().length - stackable) > itemsLimit)
        {
            SystemMessage sm = new SystemMessage(SystemMessage.YOU_HAVE_EXCEEDED_QUANTITY_THAT_CAN_BE_INPUTTED);
            sendPacket(sm);
            return;
        }
        
        // From now on, it's safe to conclude the requested transaction...
        
        // Charges the activeChar for the transaction
		for (int i = 0; i < objectIds.length; i++)
		{
			L2ItemInstance found = activeChar.getInventory().getItemByObjectId(objectIds[i]);
			if (found == null) continue;
        		L2ItemInstance item = ItemTable.getInstance().createItem(found.getItemId());
            
        		item.setCount(counts[i]);
			int realCount = item.getCount();
			if (realCount > found.getCount())
				realCount = found.getCount();
			// if it's adena
			if (item.getItemId() == 57)
        		{
			    item.setCount(realCount);
			    warehouse2.addItem(item);
			}
		}
		activeChar.reduceAdena(neededMoney);



        // Transfer the items from activeChar's Inventory Instance to destChar's Freight Instance
		for (int i = 0; i < objectIds.length; i++)
		{
			L2ItemInstance found = activeChar.getInventory().getItemByObjectId(objectIds[i]);
			if (found == null) continue;
        		if((!found.isStackable() && counts[i] > 1))
        		{
            			Util.handleIllegalPlayerAction(activeChar,
                                               "[SendWarehouseDepositList] count > 1 but item "+found.getItem().getName()+"("+found.getItemId()+") is not stackable! oid: "+objectIds[i]+" owner: "+activeChar.getName(),
                                               _log,
                                               IllegalPlayerAction.PUNISH_BROADCAST);
            			return;
        		}

        		if(found.isWear())
        		{
            		    activeChar.sendMessage("You cannot deposit Weared item to warehouse.");
            		    continue;
        		}
            	
        		L2ItemInstance item = ItemTable.getInstance().createItem(found.getItemId());
            
        		item.setCount(counts[i]);
			int realCount = item.getCount();
			if (realCount > found.getCount())
				realCount = found.getCount();
			// if it's adena, we should not delete the item. This will confuse _adena in PcInventory
			if (item.getItemId() != 57)
        		{
				L2ItemInstance old = activeChar.getInventory().destroyItemByItemId(item.getItemId(), realCount);
				item.setEnchantLevel(old.getEnchantLevel());
				item.setPriceToBuy(old.getPriceToBuy());
				item.setPriceToSell(old.getPriceToSell());
				item.setCount(realCount);
				warehouse2.addItem(item);
			}
		}
		
		
        // Send updated item list to the activeChar
		ItemList il = new ItemList(activeChar, false);
		sendPacket(il);
		
		// Update current load status on activeChar
		StatusUpdate su = new StatusUpdate(activeChar.getObjectId());
		su.addAttribute(StatusUpdate.CUR_LOAD, activeChar.getCurrentLoad());
		activeChar.sendPacket(su);

        // Delete destination L2PcInstance used for freight
		if (destChar != null)
        {
		    destChar.deleteMe();
            SystemMessage sms = new SystemMessage(614);
            sms.addString("Freight transaction successfully completed!");
            activeChar.sendPacket(sms);
        }
	}

	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.clientpackets.ClientBasePacket#getType()
	 */
	public String getType()
	{
		return _C__31_SENDWAREHOUSEDEPOSITLIST;
	}
}
