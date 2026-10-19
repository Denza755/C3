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
import net.sf.l2j.gameserver.model.L2PcInstance;
import net.sf.l2j.gameserver.model.L2PetInstance;
import net.sf.l2j.gameserver.model.PcInventory;
import net.sf.l2j.gameserver.model.PetInventory;
import net.sf.l2j.gameserver.serverpackets.InventoryUpdate;
import net.sf.l2j.gameserver.serverpackets.PetItemList;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;

/**
 * This class ...
 * 
 * @version $Revision: 1.3.2.1.2.5 $ $Date: 2005/03/29 23:15:33 $
 */
public class RequestGiveItemToPet extends ClientBasePacket
{
	private static final String REQUESTCIVEITEMTOPET__C__8B = "[C] 8B RequestGiveItemToPet";
	private static Logger _log = Logger.getLogger(RequestGiveItemToPet.class.getName());

	private final int _objectId;
	private final int _amount;
	
	public RequestGiveItemToPet(ByteBuffer buf, ClientThread client)
	{
		super(buf, client);
		_objectId = readD();
		_amount   = readD();
	}

	void runImpl()
	{
		L2PcInstance player = getClient().getActiveChar(); 
		L2PetInstance pet = (L2PetInstance)player.getPet(); 
		if (pet == null || pet.isDead())
		{
			SystemMessage sm = new SystemMessage(SystemMessage.CANNOT_GIVE_ITEMS_TO_DEAD_PET);
			sendPacket(sm);
			return;
		}
		
		PetInventory petInventory = pet.getInventory();
		PcInventory playerInventory = player.getInventory();
			
		L2ItemInstance playerItem = playerInventory.getItemByObjectId(_objectId);
		
		if (playerItem.getObjectId() == pet.getControlItemId())
			return;

		if (_amount >= playerItem.getCount())
		{
			playerInventory.dropItem(_objectId, playerItem.getCount());
			petInventory.addItem(playerItem);

			InventoryUpdate playerUI = new InventoryUpdate();
			playerUI.addRemovedItem(playerItem);
			sendPacket(playerUI);
			PetItemList petiu = new PetItemList(pet);
			sendPacket(petiu);
		}
		else
		{
			L2ItemInstance newPetItem = playerInventory.dropItem(_objectId, _amount);
			// deal with pet
			petInventory.addItem(newPetItem);

			PetItemList petiu = new PetItemList(pet);

			InventoryUpdate playerUI = new InventoryUpdate();
			playerUI.addModifiedItem(playerItem);
			// send updates
			sendPacket(petiu);
			sendPacket(playerUI);
		}

	}

	public String getType()
	{
		return REQUESTCIVEITEMTOPET__C__8B;
	}
}
