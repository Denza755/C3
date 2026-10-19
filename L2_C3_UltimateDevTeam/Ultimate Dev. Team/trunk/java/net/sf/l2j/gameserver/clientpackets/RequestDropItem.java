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
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.packets.serverpackets.*;
import net.sf.l2j.gameserver.tables.GmListTable;
import net.sf.l2j.gameserver.templates.L2Item;
import net.sf.l2j.gameserver.util.IllegalPlayerAction;
import net.sf.l2j.gameserver.util.Util;

import java.nio.ByteBuffer;
import java.util.logging.Logger;

/**
 * This class ...
 * 
 * @version $Revision: 1.11.2.1.2.7 $ $Date: 2005/04/02 21:25:21 $
 */
public class RequestDropItem extends ClientBasePacket
{
	private static final String _C__12_REQUESTDROPITEM = "[C] 12 RequestDropItem";
	private static Logger _log = Logger.getLogger(RequestDropItem.class.getName());
	private static Logger _logItems = Logger.getLogger("items");

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
        if (!Config.ALLOW_DISCARDITEM || _count == 0)
            return;
        
        L2PcInstance activeChar = getClient().getActiveChar();
	if (activeChar == null)
	    return;
        
        if (Config.GM_DISABLE_TRANSACTION && activeChar.getAccessLevel() >= Config.GM_TRANSACTION_MIN && activeChar.getAccessLevel() <= Config.GM_TRANSACTION_MAX)
        {
        		activeChar.sendMessage("Transactions are disable for your Access Level");
        }
		
        if (activeChar.getPrivateStoreType() != 0)
        {
            activeChar.sendPacket(new SystemMessage(SystemMessage.CANNOT_TRADE_DISCARD_DROP_ITEM_WHILE_IN_SHOPMODE));
            return;
        }
        
		if (!activeChar.isTransactionInProgress() && !activeChar.isProcessingTransaction())
		{
			L2ItemInstance oldItem = activeChar.getInventory().getItemByObjectId(_objectId);
			if (oldItem == null)
			{
				Util.handleIllegalPlayerAction(activeChar,"[runImpl] player "+activeChar.getName()+" ried to drop an item that is not in the inventory. objectId:" + _objectId,_log,Config.DEFAULT_PUNISH); 
			    return;
			}
			
			if (oldItem.isWear())
			{
                Util.handleIllegalPlayerAction(activeChar,"Warning!! Character "+activeChar.getName()+" tried to drop weared item: "+_objectId, _log, Config.DEFAULT_PUNISH);
			    return;
			}
			if (Config.LIST_NONDROPPABLE_ITEMS.contains(oldItem.getItemId()))
			{
			    return;
			}
			int oldCount = oldItem.getCount();
			if (Config.DEBUG)
				_log.fine("requested drop item " + _objectId + "("+ oldCount+") at "+_x+"/"+_y+"/"+_z);

			
			if (L2Item.TYPE2_QUEST == oldItem.getItem().getType2())
			{
				if (Config.DEBUG) 
					_log.finest(activeChar.getObjectId()+":player tried to drop quest item");

                activeChar.sendPacket(new SystemMessage(SystemMessage.CANNOT_DISCARD_EXCHANGE_ITEM));
                return;
			}
			
			// Pet is summoned and not the item that summoned the pet
            if (activeChar.getPet() != null && oldItem.getObjectId() == activeChar.getPet().getControlItemId())
            {
                if (Config.DEBUG) 
                    _log.finest(activeChar.getObjectId()+": player tried to drop item controling pet");
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
                
                activeChar.sendPacket(new SystemMessage(SystemMessage.CANNOT_DISCARD_DISTANCE_TOO_FAR));
			    return;
			}
            
            if(activeChar.getEnchantScroll() != null && oldItem.getObjectId() == activeChar.getEnchantScroll().getObjectId())
            {
                if (Config.DEBUG) 
                    _log.finest(activeChar.getObjectId()+":player tried to drop an enchant scroll he was using");
                return;
            }
			
			if (oldItem.isEquipped())
			{
				L2ItemInstance[] unequiped = activeChar.getInventory().unEquipItemInBodySlotAndRecord(oldItem.getItem().getBodyPart());
				InventoryUpdate iu = new InventoryUpdate();
				for (int i = 0; i < unequiped.length; i++)
				{
				 iu.addModifiedItem(unequiped[i]);
				}
				activeChar.sendPacket(iu);
				activeChar.broadcastUserInfo();
				
				activeChar.sendPacket(new ItemList(activeChar, true));
			}
			
			L2ItemInstance dropedItem = activeChar.getInventory().dropItem(_objectId, _count);
			try 
			{
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

				activeChar.sendPacket(new ItemList(activeChar, true));
				activeChar.sendPacket(new StatusUpdate(activeChar.getObjectId()).addAttribute(StatusUpdate.CUR_LOAD, activeChar.getCurrentLoad()));
				activeChar.sendPacket(new SystemMessage(SystemMessage.YOU_DROPPED_S1).addItemName(dropedItem.getItemId()));

				activeChar.broadcastUserInfo();

				activeChar.sendPacket(new UserInfo(activeChar));

				dropedItem.dropMe(activeChar, _x, _y, _z);
				dropedItem.setDropTime(0);
			} catch(Exception e) {
				dropedItem.decayMe();
				Util.handleIllegalPlayerAction(activeChar,"[runImpl] player "+activeChar.getName()+" tried to duplicate an item by disconnecting while dropping! BAN him!",_log,IllegalPlayerAction.PUNISH_KICKBAN);
			}
			
			if (Config.LOG_ITEMS || activeChar.isGM()) 
				Util.handleLogPlayerDrop(activeChar, dropedItem, "drop", _x, _y, _z); 

			if (dropedItem.getItemId() == 57 && dropedItem.getCount() >= 1000000)
			{
				String msg = "Character ("+activeChar.getName()+") has dropped ("+dropedItem.getCount()+")adena at ("+_x+","+_y+","+_z+")";
				_log.warning(msg);
				GmListTable.broadcastMessageToGMs(msg);
			}
			activeChar.refreshExpertisePenalty();
		}else{
		    activeChar.sendPacket(new SystemMessage(SystemMessage.NOTHING_HAPPENED));
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
