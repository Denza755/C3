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

import java.util.HashSet;

import net.sf.l2j.gameserver.ItemTable;
import net.sf.l2j.gameserver.handler.IItemHandler;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2PcInstance;
import net.sf.l2j.gameserver.model.L2PlayableInstance;
import net.sf.l2j.gameserver.serverpackets.ExAutoSoulShot;
import net.sf.l2j.gameserver.serverpackets.MagicSkillUser;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.templates.L2Item;
import net.sf.l2j.gameserver.templates.L2Weapon;

/**
 * This class ...
 * 
 * @version $Revision: 1.2.4.4 $ $Date: 2005/03/27 15:30:07 $
 */

public class SoulShots implements IItemHandler
{
	// all the items ids that this handler knowns
	//FIXME: this is part is dirty, needs changing to something more intelligent
	private static int[] _itemIds = {5789, 1835, 1463, 1464, 1465, 1466, 1467 };
	private static int[] _skillIds = {2039, 2150, 2151, 2152, 2153, 2154 };

	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.handler.IItemHandler#useItem(net.sf.l2j.gameserver.model.L2PcInstance, net.sf.l2j.gameserver.model.L2ItemInstance)
	 */
	public void useItem(L2PlayableInstance playable, L2ItemInstance item)
	{
		if (!(playable instanceof L2PcInstance))
			return;
		L2PcInstance activeChar = (L2PcInstance)playable;
		L2ItemInstance weaponInst = activeChar.getActiveWeaponInstance();
		L2Weapon weaponItem = activeChar.getActiveWeaponItem(); 
		HashSet<Integer> autoSoulShots = activeChar.getAutoSoulShot();
		int SoulshotId = item.getItemId();
		boolean isAutoSoulShot = false;
		L2Item itemTemplate = ItemTable.getInstance().getTemplate( item.getItemId() );
		
		if(autoSoulShots != null && autoSoulShots.contains(SoulshotId))
		{
			isAutoSoulShot = true;
		}
		if (weaponInst == null)
		{
			if(isAutoSoulShot)
			{
				activeChar.removeAutoSoulShot(SoulshotId);
				activeChar.sendPacket(new ExAutoSoulShot(SoulshotId, 0));
                activeChar.sendPacket(new SystemMessage(SystemMessage.THE_AUTOMATIC_USE_OF_S1_WILL_NOW_BE_CANCELLED).addString(itemTemplate.getName()));
				return;
			}
			activeChar.sendPacket(new SystemMessage(SystemMessage.CANNOT_USE_SOULSHOTS));
			return;
		}

		if (weaponInst.getChargedSoulshot() != L2ItemInstance.CHARGED_NONE)
		{
			// soulshot is already active
			return;
		}
		

		int grade = weaponItem.getCrystalType();
		int soulShotConsumption = weaponItem.getSoulShotCount();
		int count = item.getCount();
		
		
		if (soulShotConsumption == 0)
		{
			//	 Can't use soulshots
			if(isAutoSoulShot)
			{
				activeChar.removeAutoSoulShot(SoulshotId);
				activeChar.sendPacket(new ExAutoSoulShot(SoulshotId, 0));
                activeChar.sendPacket(new SystemMessage(SystemMessage.THE_AUTOMATIC_USE_OF_S1_WILL_NOW_BE_CANCELLED).addString(itemTemplate.getName()));
				return;
			}
			activeChar.sendPacket(new SystemMessage(SystemMessage.CANNOT_USE_SOULSHOTS));
			return;
		}
		
		
		if ((grade == L2Item.CRYSTAL_NONE && SoulshotId != 5789 && SoulshotId != 1835) || 
			(grade == L2Item.CRYSTAL_D && SoulshotId != 1463) || 
			(grade == L2Item.CRYSTAL_C && SoulshotId != 1464) || 
			(grade == L2Item.CRYSTAL_B && SoulshotId != 1465) || 
			(grade == L2Item.CRYSTAL_A && SoulshotId != 1466) || 
			(grade == L2Item.CRYSTAL_S && SoulshotId != 1467))
		{
			//	 wrong grade for weapon
			if(isAutoSoulShot)
			{
				activeChar.removeAutoSoulShot(SoulshotId);
				activeChar.sendPacket(new ExAutoSoulShot(SoulshotId, 0));
                activeChar.sendPacket(new SystemMessage(SystemMessage.THE_AUTOMATIC_USE_OF_S1_WILL_NOW_BE_CANCELLED).addString(itemTemplate.getName()));
				return;
			}
			activeChar.sendPacket(new SystemMessage(SystemMessage.SOULSHOT_DOES_NOT_MATCH_WEAPON_GRADE));
			return;
		}
		
		
		if (!(count >= soulShotConsumption))
		{
			activeChar.sendPacket(new SystemMessage(SystemMessage.NOT_ENOUGH_SOULSHOTS));
			return;
		}
		
		weaponInst.setChargedSoulshot(L2ItemInstance.CHARGED_SOULSHOT);
		activeChar.removeItemFromInventory(item, soulShotConsumption);
		activeChar.sendPacket(new SystemMessage(SystemMessage.POWER_OF_THE_SPIRITS_ENABLED));
		activeChar.broadcastPacket(new MagicSkillUser(activeChar, activeChar, _skillIds[grade],1,0,0));
	}
	
	public int[] getItemIds()
	{
		return _itemIds;
	}
}
