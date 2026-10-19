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

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.ClientThread;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2PcInstance;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.serverpackets.InventoryUpdate;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.serverpackets.UserInfo;
import net.sf.l2j.gameserver.templates.L2Item;

/**
 * This class ...
 * 
 * @version $Revision: 1.11.2.1.2.7 $ $Date: 2005/04/02 21:25:21 $
 */
public class RequestDropItem extends ClientBasePacket
{
	private static final String _C__12_REQUESTDROPITEM = "[C] 12 RequestDropItem";
	private static Logger _log = Logger.getLogger(RequestDropItem.class.getName());

	private final int _objectId;
	private final int _count;
	private final int _x;
	private final int _y;
	private final int _z;
	/**
	 * packet type id 0x12
	 * 
	 * sample
	 * 
	 * 12 
	 * 09 00 00 40 		// object id
	 * 01 00 00 00 		// count ??
	 * fd e7 fe ff 		// x
	 * e5 eb 03 00 		// y
	 * bb f3 ff ff 		// z 
	 * 
	 * format:		cdd ddd 
	 * @param decrypt
	 */
	public RequestDropItem(ByteBuffer buf, ClientThread client)
	{
		super(buf, client);
		_objectId = readD();
		_count    = readD();
		_x        = readD();
		_y        = readD();
		_z        = readD();
	}

	void runImpl()
	{
		
		if (_count == 0) 
        {
			return;
		}
        
        L2PcInstance activeChar = getClient().getActiveChar();
		
        if (activeChar.getPrivateStoreType() != 0)
        {
            activeChar.sendPacket(new SystemMessage(SystemMessage.CANNOT_TRADE_DISCARD_DROP_ITEM_WHILE_IN_SHOPMODE));
            return;
        }
        
		if (!activeChar.isTransactionInProgress())
		{
			L2ItemInstance oldItem = activeChar.getInventory().getItemByObjectId(_objectId);
			if (oldItem == null)
			{
			    _log.warning("tried to drop an item that is not in the inventory ?!?:" + _objectId);
			    return;
			}
			
			int oldCount = oldItem.getCount();
			if (Config.DEBUG)
				_log.fine("requested drop item " + _objectId + "("+ oldCount+") at "+_x+"/"+_y+"/"+_z);
			L2ItemInstance dropedItem = null;

			
			// MAJORDOMO
			// - now u can't drop quest items
			if(L2Item.TYPE2_QUEST == oldItem.getItem().getType2())
			{
				if (Config.DEBUG) 
					_log.finest(activeChar.getObjectId()+":player tried to drop quest item");
			    return;				
			}
			
			if ((oldCount < _count))
			{
				if (Config.DEBUG) 
					_log.finest(activeChar.getObjectId()+":player tried to drop more items than he has");
			    return;
			}
			
			if ( activeChar.getDistance(_x, _y) > 150  || Math.abs(_z-activeChar.getZ())> 50 )
			{ 
				if (Config.DEBUG) 
					_log.finest(activeChar.getObjectId()+": trying to drop too far away");
			    SystemMessage sm = new SystemMessage(151);
			    activeChar.sendPacket(sm);
			    return;
			}
			
			if (oldItem.isEquipped())
			{
			    //_log.fine("item is equiped");
				int slot = oldItem.getEquipSlot();
				// unequip the item before we drop it
				L2ItemInstance[] unequipped =
					activeChar.getInventory().unEquipItemInSlotAndRecord(slot);
				if (unequipped != null)
				{
					InventoryUpdate iu = new InventoryUpdate();
					for (int i = 0; i < unequipped.length; i++)
					{
						iu.addModifiedItem(unequipped[i]);
						SystemMessage sm = new SystemMessage(0x1a1); // unequip
						sm.addItemName(unequipped[i].getItemId());
						activeChar.sendPacket(sm);
					}
					activeChar.sendPacket(iu);
					activeChar.broadcastUserInfo();
				}
				activeChar.sendPacket(SystemMessage.sendString("You have to unequip the item before you can drop it."));
				return;
			    
//			    EquipUpdate eu = new EquipUpdate(oldItem, L2ItemInstance.REMOVED);
//			    con.sendPacket(eu);
			}
			else
			{
			    dropedItem = activeChar.getInventory().dropItem(_objectId, _count);
			}
			
			if (Config.DEBUG) 
				_log.fine("dropping " + _objectId + " item("+_count+") at: " + _x + " " + _y + " " + _z);
			dropedItem.dropMe(activeChar, _x, _y, _z);
			
			InventoryUpdate iu = new InventoryUpdate();
			if (oldCount == dropedItem.getCount())	// we drop all the had, so remove the item
			{
				if (Config.DEBUG) _log.finest("remove item from inv");
			    iu.addRemovedItem(oldItem); 
			}
			else
			{
				if (Config.DEBUG) _log.finest("reducing item in inv");
			    iu.addModifiedItem(oldItem);
			}
			activeChar.sendPacket(iu);
			
			SystemMessage sm = new SystemMessage(298);
			sm.addItemName(dropedItem.getItemId());
			activeChar.sendPacket(sm);
			activeChar.sendPacket(new UserInfo(activeChar));
			
            if (dropedItem.getItemId() == 57 && dropedItem.getCount() >= 1000000)
            {
                String msg = "Over 1mil adena dropped by: "+activeChar.getName()+" at ("+_x+","+_y+","+_z+")";
                _log.warning(msg);
                L2PcInstance[] list = L2World.getInstance().getAllGMs();
                sm = new SystemMessage(614);
                sm.addString(msg);
                for (int i=0; i<list.length; i++)
                {
                    list[i].sendPacket(sm);
                }
            }
		}
		else
		{
		    SystemMessage msg = new SystemMessage(SystemMessage.NOTHING_HAPPENED);
		    sendPacket(msg);
		}
	
	}
	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.clientpackets.ClientBasePacket#getType()
	 */
	public String getType()
	{
		return _C__12_REQUESTDROPITEM;
	}
}
