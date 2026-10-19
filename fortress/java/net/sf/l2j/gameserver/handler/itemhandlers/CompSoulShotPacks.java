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
package net.sf.l2j.gameserver.handler.itemhandlers;

import net.sf.l2j.gameserver.ItemTable;
import net.sf.l2j.gameserver.handler.IItemHandler;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2PcInstance;
import net.sf.l2j.gameserver.model.L2PlayableInstance;
import net.sf.l2j.gameserver.serverpackets.ItemList;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;

/**
 * This class ...
 * 
 * @version $Revision: 1.1.2.1.2.3 $ $Date: 2005/03/27 15:30:07 $
 */

public class CompSoulShotPacks implements IItemHandler
{
	private static int[] _itemIds = { 5134, 5135, 5136, 5137, 5138, 5139, 5250, 5251, 5252, 5253, 5254, 5255 };
	
	public void useItem(L2PlayableInstance playable, L2ItemInstance item)
	{
		if (!(playable instanceof L2PcInstance))
			return;
		L2PcInstance activeChar = (L2PcInstance)playable;

	    int itemId = item.getItemId();
	    int itemToCreateId = 1835; //default No Grade SS
	    int amount = 300; // default regular pack
	    L2ItemInstance createdItem = null;
	    
	    // The NG shots are itemId 1835, the other grades start at 1463.
	    // That's why the explicit 1835 itemcreate for both packs.
	    
	    if (itemId < 5200){ // Normal Compressed Package of SoulShots
	    	if (itemId == 5134) // No Grade
	    		createdItem = ItemTable.getInstance().createItem(itemToCreateId);
	    	else{
	    		itemToCreateId = itemId - 3672; // Gives id of matching item for this pack
	    		createdItem = ItemTable.getInstance().createItem(itemToCreateId);
	    	}
	    }else{  // Greater Compressed Package of SoulShots
	    	if (itemId == 5250) // No Grade
	    		createdItem = ItemTable.getInstance().createItem(itemToCreateId);
	    	else{
	       		itemToCreateId = itemId - 3788; // Gives id of matching item for this pack
	    		createdItem = ItemTable.getInstance().createItem(itemToCreateId);
	    	}
	    	amount = 1000;
	    }

	    if (createdItem == null)
	    	return;
	    
	    createdItem.setCount(amount);
	    activeChar.getInventory().addItem(createdItem);
		activeChar.removeItemFromInventory(item, 1);

	    SystemMessage sm = new SystemMessage(SystemMessage.EARNED_S2_S1_s); 
	    sm.addItemName(itemToCreateId);
	    sm.addNumber(amount);
        activeChar.sendPacket(sm);

		activeChar.sendPacket(new ItemList(activeChar, false));
	}
	public int[] getItemIds()
	{
		return _itemIds;
	}
}
