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
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2PcInstance;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.serverpackets.InventoryUpdate;
import net.sf.l2j.gameserver.serverpackets.StatusUpdate;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.templates.L2Item;


/**
 * This class ...
 * 
 * @version $Revision: 1.2.2.3.2.5 $ $Date: 2005/03/27 15:29:30 $
 */
public class RequestCrystallizeItem extends ClientBasePacket 
{
	private static final String _C__72_REQUESTDCRYSTALLIZEITEM = "[C] 72 RequestCrystallizeItem";
	private static Logger _log = Logger.getLogger(RequestCrystallizeItem.class.getName());
	
	//0, d, c, b, a, s ... 0 is for none
	private static final int[] _crystalId = {0, 1458, 1459, 1460, 1461, 1462};

	private final int _objectId;
	private       int _count;
	
	public RequestCrystallizeItem(ByteBuffer buf, ClientThread client)
	{
		super(buf, client);
		_objectId = readD();
		_count = readD();
	}

	void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
        int skillLevel = activeChar.getSkillLevel(L2Skill.SKILL_CRYSTALLIZE);
        if (skillLevel <= 0)
        {
        	SystemMessage sm = new SystemMessage(SystemMessage.CRYSTALLIZE_LEVEL_TOO_LOW); 
            activeChar.sendPacket(sm);
            ActionFailed af = new ActionFailed();
            activeChar.sendPacket(af);
            return;
        }
        
		if (_count > activeChar.getInventory().getItemByObjectId(_objectId).getCount()) {
			_count = activeChar.getInventory().getItemByObjectId(_objectId).getCount();
		}
		
		L2ItemInstance itemToRemove = activeChar.getInventory().getItemByObjectId(_objectId);
		
		if (!itemToRemove.getItem().isCrystallizable() || (itemToRemove.getItem().getCrystalCount() <= 0)
				|| (itemToRemove.getItem().getCrystalType() == L2Item.CRYSTAL_NONE)) {
			_log.warning(""+activeChar.getObjectId()+" tried to crystallize "+itemToRemove.getItem().getItemId());
			return;
		}
				
		//unequip if needed
		if (itemToRemove.isEquipped())
		{
			L2ItemInstance[] unequiped =
				activeChar.getInventory().unEquipItemInSlotAndRecord(itemToRemove.getEquipSlot()); 
			InventoryUpdate iu = new InventoryUpdate();
			for (int i = 0; i < unequiped.length; i++)
			{
				iu.addModifiedItem(unequiped[i]);
			}
			activeChar.sendPacket(iu);
			//activeChar.updatePDef();
			//activeChar.updatePAtk();
			//activeChar.updateMDef();
			//activeChar.updateMAtk();
			//activeChar.updateAccuracy();
            //activeChar.updateCriticalChance();
		}

		//remove from inventory
		L2ItemInstance removedItem = activeChar.getInventory().destroyItem(_objectId, _count);
		
		//add crystals
        int crystalAmount = itemToRemove.getItem().getCrystalCount();
        int crystalId = _crystalId[itemToRemove.getItem().getCrystalType()];
        
		L2ItemInstance createditem = ItemTable.getInstance().createItem(crystalId);
	    createditem.setCount(crystalAmount);
		L2ItemInstance addedItem = activeChar.getInventory().addItem(createditem);

		//send inventory update
		InventoryUpdate iu = new InventoryUpdate();
		if (removedItem.getCount() == 0) {
			iu.addRemovedItem(removedItem);
		} else {
			iu.addModifiedItem(removedItem);
		}
		
		if (addedItem.getCount() != crystalAmount) {
			iu.addModifiedItem(addedItem);
		} else {
			iu.addNewItem(addedItem);	
		}
		activeChar.sendPacket(iu);
		
		//status & user info
		StatusUpdate su = new StatusUpdate(activeChar.getObjectId());
		su.addAttribute(StatusUpdate.CUR_LOAD, activeChar.getCurrentLoad());
		activeChar.sendPacket(su);

		activeChar.broadcastUserInfo();

		L2World world = L2World.getInstance();
		world.removeObject(removedItem);
	}

	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.clientpackets.ClientBasePacket#getType()
	 */
	public String getType()
	{
		return _C__72_REQUESTDCRYSTALLIZEITEM;
	}
}
