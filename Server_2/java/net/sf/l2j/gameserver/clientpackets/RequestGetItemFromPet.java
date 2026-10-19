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
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.PcInventory;
import net.sf.l2j.gameserver.model.PetInventory;
import net.sf.l2j.gameserver.serverpackets.ItemList;
import net.sf.l2j.gameserver.serverpackets.PetInventoryUpdate;

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
		PetInventory petInventory = getClient().getActiveChar().getPet().getInventory();
		PcInventory playerInventory = getClient().getActiveChar().getInventory();
			
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
			playerInventory.addItem(petItem);
			petInventory.destroyItem(_objectId, petItem.getCount());

			PetInventoryUpdate petiu = new PetInventoryUpdate();
			petiu.addRemovedItem(petItem);
			sendPacket(petiu);

			ItemList playerUI = new ItemList(getClient().getActiveChar(), false);
			sendPacket(playerUI);
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
			ItemList playerUI = new ItemList(getClient().getActiveChar(), false);
			sendPacket(petiu);
			sendPacket(playerUI);
		}
	}

	public String getType()
	{
		return REQUESTGETITEMFROMPET__C__8C;
	}
}
