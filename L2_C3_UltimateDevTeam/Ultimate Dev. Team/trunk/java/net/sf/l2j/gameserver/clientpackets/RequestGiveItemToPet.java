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
import net.sf.l2j.gameserver.model.PcInventory;
import net.sf.l2j.gameserver.model.PetInventory;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PetInstance;
import net.sf.l2j.gameserver.packets.serverpackets.InventoryUpdate;
import net.sf.l2j.gameserver.packets.serverpackets.PetItemList;
import net.sf.l2j.gameserver.packets.serverpackets.StatusUpdate;
import net.sf.l2j.gameserver.packets.serverpackets.SystemMessage;

import java.nio.ByteBuffer;

/**
 * This class ...
 * 
 * @version $Revision: 1.3.2.1.2.5 $ $Date: 2005/03/29 23:15:33 $
 */
public class RequestGiveItemToPet extends ClientBasePacket
{
	private static final String REQUESTCIVEITEMTOPET__C__8B = "[C] 8B RequestGiveItemToPet";

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

        if (player == null)
            return;

        // Alt game - Karma punishment
        if (!Config.ALT_GAME_KARMA_PLAYER_CAN_TRADE && player.getKarma() > 0)
            return;

        L2PetInstance pet = (L2PetInstance)player.getPet(); 
		if (pet == null || pet.isDead())
		{
			sendPacket(new SystemMessage(SystemMessage.CANNOT_GIVE_ITEMS_TO_DEAD_PET));
			return;
		}
		
        if (player.getPrivateStoreType() != 0)
        {
        		player.sendMessage("Cannot exchange items while trading");
        }

        
		PetInventory petInventory = pet.getInventory();
		PcInventory playerInventory = player.getInventory();
			
		L2ItemInstance playerItem = playerInventory.getItemByObjectId(_objectId);
		
		if (playerItem == null)
			return;
        
        if (playerItem.getObjectId() == pet.getControlItemId() || playerItem.isWear())
            return;

		if (_amount >= playerItem.getCount())
		{
			playerInventory.dropItem(_objectId, playerItem.getCount());
			petInventory.addItem(playerItem);

			sendPacket(new InventoryUpdate().addRemovedItem(playerItem));
			sendPacket(new PetItemList(pet));
		}
		else
		{
			L2ItemInstance newPetItem = playerInventory.dropItem(_objectId, _amount);
			// deal with pet
			petInventory.addItem(newPetItem);

			sendPacket(new PetItemList(pet));
			sendPacket(new InventoryUpdate().addModifiedItem(playerItem));
		}

		player.sendPacket(new StatusUpdate(player.getObjectId()).addAttribute(StatusUpdate.CUR_LOAD, player.getCurrentLoad()));
	}

	public String getType()
	{
		return REQUESTCIVEITEMTOPET__C__8B;
	}
}
