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
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2Summon;
import net.sf.l2j.gameserver.model.PcInventory;
import net.sf.l2j.gameserver.model.PetInventory;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.packets.serverpackets.ItemList;
import net.sf.l2j.gameserver.packets.serverpackets.PetInventoryUpdate;
import net.sf.l2j.gameserver.packets.serverpackets.StatusUpdate;

import java.nio.ByteBuffer;
import java.util.logging.Logger;

/**
 * This class ...
 * 
 * @version $Revision: 1.3.4.4 $ $Date: 2005/03/29 23:15:33 $
 */
public class RequestGetItemFromPet extends ClientBasePacket
{
	private static final String REQUESTGETITEMFROMPET__C__8C = "[C] 8C RequestGetItemFromPet";
	private static Logger _log = Logger.getLogger(RequestGetItemFromPet.class
			.getName());

	private final int _objectId;
	private final int _amount;
	@SuppressWarnings("unused")
    private final int _unknown;
	
	public RequestGetItemFromPet(ByteBuffer buf, ClientThread client)
	{
		super(buf, client);
		_objectId = readD();
		_amount   = readD();
		_unknown  = readD();// = 0 for most trades
	}

	void runImpl()
	{
        L2PcInstance activeChar = getClient().getActiveChar();
        if (activeChar == null)
            return;
        L2Summon pet = activeChar.getPet();
        if (pet == null)
            return;
		PetInventory petInventory = pet.getInventory();
		PcInventory playerInventory = activeChar.getInventory();
			
		L2ItemInstance petItem = petInventory.getItemByObjectId(_objectId);
		
		if (petItem == null)
		{
			_log.warning("item requested from pet, but its not there.");
			return;
		}
			
		if (petItem.isEquipped())
		{
			return;
		}

		if (_amount >= petItem.getCount())
		{
            petInventory.dropItem(_objectId, petItem.getCount());
			playerInventory.addItem(petItem);
			//petInventory.destroyItem(_objectId, petItem.getCount());

			PetInventoryUpdate petiu = new PetInventoryUpdate();
			petiu.addRemovedItem(petItem);
			sendPacket(petiu);

			sendPacket(new ItemList(getClient().getActiveChar(), false));
		}
		else
		{
			int total = petItem.getCount();
			//deal with pet
			L2ItemInstance newPlayerItem = petInventory.dropItem(_objectId, total);
			PetInventoryUpdate petiu = new PetInventoryUpdate();
			petiu.addModifiedItem(petItem);
			//deal with player
			playerInventory.addItem(newPlayerItem);
			
			//send updates
			sendPacket(petiu);
			sendPacket(new ItemList(getClient().getActiveChar(), false));
		}
		activeChar.sendPacket(new StatusUpdate(activeChar.getObjectId()).addAttribute(StatusUpdate.CUR_LOAD, activeChar.getCurrentLoad()));
	}

	public String getType()
	{
		return REQUESTGETITEMFROMPET__C__8C;
	}
}
