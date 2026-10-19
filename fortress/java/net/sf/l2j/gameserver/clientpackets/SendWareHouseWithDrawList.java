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
import net.sf.l2j.gameserver.model.ClanWarehousePool;
import net.sf.l2j.gameserver.model.L2Clan;
import net.sf.l2j.gameserver.model.L2FolkInstance;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2NpcInstance;
import net.sf.l2j.gameserver.model.L2PcInstance;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.Warehouse;
import net.sf.l2j.gameserver.serverpackets.ItemList;
import net.sf.l2j.gameserver.serverpackets.StatusUpdate;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;

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
	private final int[] counts;
	
	public SendWareHouseWithDrawList(ByteBuffer buf, ClientThread client)
	{
		super(buf, client);
		_count = readD();
		_items = new int[_count * 2];
		counts = new int[_count];
		for (int i = 0; i < _count; i++)
		{
			_items[i * 2 + 0] = readD(); //item object id
			_items[i * 2 + 1] = readD(); //count
		}
	}

	void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
        if (activeChar == null)
            return;
        
		L2FolkInstance whkeeper = activeChar.getLastFolkNPC();
		if (whkeeper == null)
            return;

        if(activeChar.getDistance(whkeeper.getX(), whkeeper.getY()) > L2NpcInstance.INTERACTION_DISTANCE && !activeChar.isGM())
            return;

        if (activeChar.isUsingClanWarehouse() && !((activeChar.getClanPrivileges() & L2Clan.CP_CL_VIEW_WAREHOUSE) == L2Clan.CP_CL_VIEW_WAREHOUSE))
            return;

        int weight = 0;
        int finalCount = activeChar.getInventory().getSize();
        //L2ItemInstance[] items = new L2ItemInstance[_count];
        L2ItemInstance[] olditems = new L2ItemInstance[_count];

        for (int i=0; i < _count; i++)
        {
            int itemObjId = _items[i * 2 + 0];
            int count  = _items[i * 2 + 1];
            L2ItemInstance oldinst = L2ItemInstance.restoreFromDb(itemObjId);
            if(oldinst == null || oldinst.getCount()<count)
            {
                activeChar.sendMessage("Somebody has changed Clan Warehouse. Try again.");
                for(int f=0; f<i;f++)
                {
 //                   L2World.removeObject(items[f]);
                    L2World.removeObject(olditems[i]); // FIXME don't sure...
                }
 //               items = null;
                olditems = null;
                return;
            }
  //          L2ItemInstance inst = ItemTable.getInstance().createItem(oldinst.getItem().getItemId());
  //          inst.setCount(count);
            counts[i]=count;
            olditems [i] = oldinst;
            weight += oldinst.getItem().getWeight() * count;
            finalCount ++;
            if (oldinst.getItem().isStackable() && activeChar.getInventory().getItemByItemId(oldinst.getItemId()) != null)
              finalCount --;
		}
		
        if (!activeChar.getInventory().validateCapacity(finalCount))
        {
            for (int i = 0; i < olditems.length; i++)
            {
                //L2World.removeObject(items[i]);
                L2World.removeObject(olditems[i]); // FIXME don't sure...
            }
            activeChar.sendPacket(new SystemMessage(SystemMessage.INVENTORY_SLOT_IS_FULL));
//            items = null;
            olditems = null;
            return;
        }

        if (activeChar.getMaxLoad() - activeChar.getCurrentLoad() < weight)
        {
            for (int i = 0; i < olditems.length; i++)
            {
 //               L2World.removeObject(items[i]);
                L2World.removeObject(olditems[i]); // FIXME don't sure...
            }
            activeChar.sendPacket(new SystemMessage(SystemMessage.YOU_HAVE_EXCEEDED_THE_WEIGHT_LIMIT));
//            items = null;
            olditems = null;
            return;
        }

        Warehouse warehouse2 = null;
        if (!activeChar.isUsingClanWarehouse() && !activeChar.isUsingFreight())
            warehouse2 = activeChar.getWarehouse();
        else if (activeChar.isUsingClanWarehouse() && !activeChar.isUsingFreight())
            {
//                warehouse2 = activeChar.getClan().getWarehouse();
				ClanWarehousePool.getInstance().AddWork(activeChar,olditems,counts);
				return;
            }
        else if (!activeChar.isUsingClanWarehouse() && activeChar.isUsingFreight())
            warehouse2 = activeChar.getFreight();
        else
        {
            // Something went wrong!
            _log.warning("Error retrieving a warehouse object for char " + activeChar.getName() + " - using freight: " + activeChar.isUsingFreight());
//            items = null;
            olditems = null;
            return;     
        }
              
        for (int i = 0; i < olditems.length; i++)
        {
            L2ItemInstance TransferItem = warehouse2.getItemByObj(olditems[i].getObjectId(), counts[i]);
            if(TransferItem == null)
            	_log.warning("Error getItem from warhouse player: " + activeChar.getName());
                
            
        	/*items[i].setEnchantLevel(old.getEnchantLevel());
        	items[i].setPriceToBuy(old.getPriceToBuy());
        	items[i].setPriceToSell(old.getPriceToSell());
        	*/
        	activeChar.getInventory().addItem(TransferItem);
            L2World.removeObject(olditems[i]);
        }
        StatusUpdate su = new StatusUpdate(activeChar.getObjectId());
        su.addAttribute(StatusUpdate.CUR_LOAD, activeChar.getCurrentLoad());			
        activeChar.sendPacket(su);
        activeChar.sendPacket(new ItemList(activeChar, false));			
//        items = null;
        olditems = null;
	}

	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.clientpackets.ClientBasePacket#getType()
	 */
	public String getType()
	{
		return _C__32_SENDWAREHOUSEWITHDRAWLIST;
	}
}
