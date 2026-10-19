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
import java.util.ArrayList;
import java.util.Arrays;

import net.sf.l2j.gameserver.ClientThread;
import net.sf.l2j.gameserver.ItemTable;
import net.sf.l2j.gameserver.handler.IItemHandler;
import net.sf.l2j.gameserver.handler.ItemHandler;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2PcInstance;
import net.sf.l2j.gameserver.model.quest.QuestState;
import net.sf.l2j.gameserver.serverpackets.InventoryUpdate;
import net.sf.l2j.gameserver.serverpackets.ShowCalculator;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;

/**
 * This class ...
 * 
 * @version $Revision: 1.18.2.7.2.9 $ $Date: 2005/03/27 15:29:30 $
 */
public class UseItem extends ClientBasePacket
{
	private static final String _C__14_USEITEM = "[C] 14 UseItem";
	
	private final int _objectId;
	/**
	 * packet type id 0x14
	 * format:		cd
	 * @param decrypt
	 */
	public UseItem(ByteBuffer buf, ClientThread client)
	{
		super(buf, client);
		_objectId = readD();
	}

	void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if(activeChar == null) return;

		synchronized(activeChar.getInventory())
		{
			L2ItemInstance item = activeChar.getInventory().getItemByObjectId(_objectId);

			if(item == null)
				return;

			int itemId = item.getItemId();
			if(itemId == 57)
				return;

			if(activeChar.isDead())
			{
				activeChar.sendPacket(new SystemMessage(SystemMessage.S1_CANNOT_BE_USED_DUE_TO_UNSUITABLE_TERMS).addItemName(itemId));
				return;
			}

			QuestState[] quests = item.getQuestsForItemUse(item);
			if(quests != null)
			{
				// Go through table containing all QuestState to modify
				for(QuestState qs : quests)
					// Notify the Quest Engine of the L2Attackable death
					qs.getQuest().notifyItemUse(item, qs);
			}

			if(item.getItem().isForPet())
			{
				activeChar.sendPacket(new SystemMessage(SystemMessage.YOU_CANNOT_EQUIP_A_PET_ITEM).addItemName(itemId));
				return;
			}

			if(item.isEquipable())
			{
				L2ItemInstance[] items = activeChar.getInventory().equipItemAndRecord(item);
				activeChar.calcExpertisePenalty();

				activeChar.sendPacket(new SystemMessage(SystemMessage.YOU_ARE_EQUIPPED_WITH_S1).addItemName(itemId));

				activeChar.sendPacket(new InventoryUpdate(new ArrayList<L2ItemInstance>(Arrays.asList(items))));
				activeChar.abortAttack();
				activeChar.broadcastUserInfo();
				return;
			}

			if(itemId == 4393)
			{
				activeChar.sendPacket(new ShowCalculator(4393));
				return;
			}

			if(ItemTable.useHandler(activeChar, item))
				return;

			IItemHandler handler = ItemHandler.getInstance().getItemHandler(item.getItemId());
			if(handler != null)
				handler.useItem(activeChar, item);
		}
	}
	
	public String getType()
	{
		return _C__14_USEITEM;
	}

}
