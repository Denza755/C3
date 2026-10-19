/*
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2, or (at your option) any later version. This
 * program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place - Suite 330, Boston, MA 02111-1307, USA.
 * http://www.gnu.org/copyleft/gpl.html
 */
package net.sf.l2j.gameserver.clientpackets;

import java.nio.ByteBuffer;
import java.util.logging.Logger;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.ClientThread;
import net.sf.l2j.gameserver.model.L2FolkInstance;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2NpcInstance;
import net.sf.l2j.gameserver.model.L2PcInstance;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.Warehouse;
import net.sf.l2j.gameserver.serverpackets.InventoryUpdate;
import net.sf.l2j.gameserver.serverpackets.ItemList;
import net.sf.l2j.gameserver.serverpackets.StatusUpdate;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;

// import net.sf.l2j.gameserver.serverpackets.InventoryUpdate;

/**
 * This class ... 31 SendWareHouseDepositList cd (dd)
 * 
 * @version $Revision: 1.3.4.5 $ $Date: 2005/04/11 10:06:09 $
 */
public class SendWareHouseDepositList extends ClientBasePacket
{
    private static final String _C__31_SENDWAREHOUSEDEPOSITLIST = "[C] 31 SendWareHouseDepositList";

    private static Logger _log = Logger.getLogger(SendWareHouseDepositList.class.getName());

    private final int _count;

    private final int[] _items;

    private static final int _MAX_WAREHOUSE_ITEMS = 250;

    private static final int _MAX_CLANWH_ITEMS = 350;

    private static final int _MAX_FREIGHT_ITEMS = 50;

    public SendWareHouseDepositList(ByteBuffer buf, ClientThread client)
    {
        super(buf, client);
        _count = readD();
        _items = new int[_count * 2];
        for(int i = 0; i < _count; i++)
        {
            _items[i * 2 + 0] = readD(); //item object id
            _items[i * 2 + 1] = readD(); //count
        }
    }

    void runImpl()
    {
        L2PcInstance activeChar = getClient().getActiveChar();
		if(activeChar == null) return;

        L2FolkInstance whkeeper = activeChar.getLastFolkNPC();
        if(whkeeper == null)
            return;
        if(activeChar.getDistance(whkeeper.getX(), whkeeper.getY()) > L2NpcInstance.INTERACTION_DISTANCE && !activeChar.isGM())
            return;

        int price;

        if(activeChar.isUsingFreight())
            price = Config.ALT_GAME_FREIGHT_PRICE; // Freight price per item slot
        else
            price = 30; // Normal price per item slot

        int neededMoney = 0;
        int currentMoney = activeChar.getAdena();
        int itemsLimit = 0;

        L2ItemInstance adenaitem = activeChar.getInventory().getAdenaInstance();
        if(adenaitem == null)
        {
            activeChar.sendPacket(new SystemMessage(SystemMessage.YOU_DO_NOT_HAVE_ENOUGH_ADENA));
            return;
        }
        int[] itemObjIds = new int[_count];
        int[] counts = new int[_count];
        for(int i = 0; i < _count; i++)
        {
            itemObjIds[i] = _items[i * 2 + 0];
            counts[i] = _items[i * 2 + 1];

            // Calculate needed money
//            if(itemObjIds[i] == adenaitem.getObjectId())
//                neededMoney += counts[i];
            neededMoney += price;
        }

        // Check for needed money
        if(neededMoney > currentMoney)
        {
            activeChar.sendPacket(new SystemMessage(SystemMessage.YOU_DO_NOT_HAVE_ENOUGH_ADENA));
            return;
        }

        L2PcInstance destChar = null;
        Warehouse warehouse2 = null;
        if(!activeChar.isUsingClanWarehouse() && !activeChar.isUsingFreight())
        {
            itemsLimit = _MAX_WAREHOUSE_ITEMS;
            warehouse2 = activeChar.getWarehouse();
        }
        else if(activeChar.isUsingClanWarehouse() && !activeChar.isUsingFreight())
        {
            itemsLimit = _MAX_CLANWH_ITEMS;
            warehouse2 = activeChar.getClan().getWarehouse();
        }
        else if(!activeChar.isUsingClanWarehouse() && activeChar.isUsingFreight())
        {
            destChar = L2PcInstance.load(activeChar.getFreightId());
            if(destChar == null)
            {
                // Something went wrong!
                if(Config.DEBUG)
                    _log.warning("Error retrieving a warehouse object for char "
                            + activeChar.getName() + " - using freight: "
                            + activeChar.isUsingFreight());
                return;
            }
            itemsLimit = _MAX_FREIGHT_ITEMS;
            warehouse2 = destChar.getFreight();
            warehouse2.setLocationId(activeChar);
        }
        else
        {
            // Something went wrong!
            if(Config.DEBUG)
                _log.warning("Error retrieving a warehouse object for char "
                        + activeChar.getName() + " - using freight: "
                        + activeChar.isUsingFreight());
            return;
        }

        // Item Max Limit Check
        if((itemObjIds.length + warehouse2.listItems().length) > itemsLimit)
        {
            activeChar.sendMessage("You've exceeded the maximum item limit for your transaction.");
            return;
        }

        // From now on, it's safe to conclude the requested transaction...

        // Transfer the items from activeChar's Inventory Instance to destChar's Freight Instance
        InventoryUpdate update = new InventoryUpdate();
        for(int i = 0; i < itemObjIds.length; i++)
        {
        	//found item
            L2ItemInstance found = activeChar.getInventory().getItemByObjectId(
                    itemObjIds[i]);
            if(found == null) continue;
            //L2ItemInstance item = ItemTable.getInstance().createItem(found.getItemId());
            L2ItemInstance item = activeChar.getInventory().dropItem(itemObjIds[i],counts[i]);
            //update inventory
			if (found.getLastChange() == L2ItemInstance.MODIFIED)
			{
				update.addModifiedItem(found);
			}
			else
			{
				L2World.removeObject(found);
				update.addRemovedItem(found);
			}
			activeChar.sendPacket(update);
			//add item to warehouse2
            L2ItemInstance olditem = warehouse2.addItem(item);
            L2World.removeObject(olditem);
        }

        activeChar.reduceAdena(neededMoney);

        activeChar.sendPacket(new ItemList(activeChar, false));

        StatusUpdate su = new StatusUpdate(activeChar.getObjectId());
        su.addAttribute(StatusUpdate.CUR_LOAD, activeChar.getCurrentLoad());
        activeChar.sendPacket(su);

        // Delete destination L2PcInstance used for freight
        if(destChar != null)
        {
            destChar.deleteMe();
            activeChar.sendMessage("Freight transaction successfully completed!");
        }
        itemObjIds = null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.sf.l2j.gameserver.clientpackets.ClientBasePacket#getType()
     */
    public String getType()
    {
        return _C__31_SENDWAREHOUSEDEPOSITLIST;
    }
}
