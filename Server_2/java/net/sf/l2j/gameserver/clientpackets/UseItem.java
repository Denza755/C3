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
import java.util.logging.Logger;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.ClientThread;
import net.sf.l2j.gameserver.handler.IItemHandler;
import net.sf.l2j.gameserver.handler.ItemHandler;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2PcInstance;
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
	private static Logger _log = Logger.getLogger(UseItem.class.getName());
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
		if (activeChar == null)
			return;
		L2ItemInstance item = activeChar.getInventory().getItemByObjectId(_objectId);

		if(activeChar.isDead()) 
        {
			SystemMessage sm = new SystemMessage(SystemMessage.S1_CANNOT_BE_USED);
			sm.addItemName(item.getItemId());
			getClient().getActiveChar().sendPacket(sm);
			return;
		}
		if (Config.DEBUG) _log.finest(activeChar.getObjectId()+": use item " + _objectId);

		
		if (item != null && item.isEquipable())
		{
			L2ItemInstance[] items = activeChar.getInventory().equipItemAndRecord(item); 
			//if (item.getItem().getType2() == L2Item.TYPE2_WEAPON)
			//{
                //((L2Weapon)item.getItem()).setExpertisePenalty(activeChar.expertisePenalty(item.getItem().getCrystalType()));
				//activeChar.updatePAtk();
				//activeChar.updateMAtk();
				//activeChar.updateAccuracy();
                //activeChar.updateCriticalChance();
			//}
			//else if (item.getItem().getType2() == L2Item.TYPE2_SHIELD_ARMOR)
			//{
				//activeChar.updatePDef();
				//activeChar.updateMpBonus();
                //activeChar.updateEffectiveSpeed();
			//} 
			//else if (item.getItem().getType2() == L2Item.TYPE2_ACCESSORY)
			//{
				//activeChar.updateMDef();
                //activeChar.updateEffectiveSpeed();
			//}
			
			SystemMessage sm = new SystemMessage(49);
			sm.addItemName(item.getItemId());
			activeChar.sendPacket(sm);
			
			InventoryUpdate iu = new InventoryUpdate(
					new ArrayList<L2ItemInstance>(Arrays.asList(items)));
			activeChar.sendPacket(iu);
			activeChar.abortAttack();
			activeChar.broadcastUserInfo();
		}
		else
		{
			//_log.finest("item not equipable id:"+ item.getItemId());
			if (item != null && item.getItemId() == 4393)
			{
				activeChar.sendPacket(new ShowCalculator(4393));
			}
			else
			{
				if (item != null)
				{
					IItemHandler handler = ItemHandler.getInstance().getItemHandler(item.getItemId());
					if (handler == null)
					{
						_log.warning("no itemhandler registered for itemId:" + item.getItemId());
					}
					else
					{
						handler.useItem(activeChar, item);
					}
				}
			}
		}
	}
	
	public String getType()
	{
		return _C__14_USEITEM;
	}

}
