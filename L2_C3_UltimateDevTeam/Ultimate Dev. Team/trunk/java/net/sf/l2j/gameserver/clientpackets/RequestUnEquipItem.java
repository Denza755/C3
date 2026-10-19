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
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.packets.serverpackets.InventoryUpdate;
import net.sf.l2j.gameserver.packets.serverpackets.SystemMessage;

import java.nio.ByteBuffer;

/**
 * This class ...
 * 
 * @version $Revision: 1.8.2.3.2.7 $ $Date: 2005/03/27 15:29:30 $
 */
public class RequestUnEquipItem extends ClientBasePacket
{
	private static final String _C__11_REQUESTUNEQUIPITEM = "[C] 11 RequestUnequipItem";

	// cd
	private final int _slot;
	
	/**
	 * packet type id 0x11
	 * format:		cd 
	 * @param decrypt
	 */
	public RequestUnEquipItem(ByteBuffer buf, ClientThread client)
	{
		super(buf, client);
		_slot = readD();
	}

	void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
		    return;
		L2ItemInstance[] unequiped =
			activeChar.getInventory().unEquipItemInBodySlotAndRecord(_slot); 
        
		
		// show the update in the inventory
		InventoryUpdate iu = new InventoryUpdate();
		for (int i = 0; i < unequiped.length; i++)
		{
            if (unequiped[i].isWear())
            {
                return;
            }
			iu.addModifiedItem(unequiped[i]);
		}
		activeChar.sendPacket(iu);
		
		activeChar.abortAttack();
		
		activeChar.refreshExpertisePenalty();
		activeChar.broadcastUserInfo();
		
		// this can be 0 if the user pressed the right mousebutton twice very fast
		if (unequiped.length > 0)
		{
            if (unequiped[0].isWear())
            {
                return;
            }
            activeChar.sendPacket(new SystemMessage(417).addItemName(unequiped[0].getItemId()));
		}
	}

	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.clientpackets.ClientBasePacket#getType()
	 */
	public String getType()
	{
		return _C__11_REQUESTUNEQUIPITEM;
	}
}
