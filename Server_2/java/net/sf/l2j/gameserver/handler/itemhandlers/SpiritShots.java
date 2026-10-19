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

import net.sf.l2j.gameserver.handler.IItemHandler;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2PcInstance;
import net.sf.l2j.gameserver.model.L2PlayableInstance;
import net.sf.l2j.gameserver.serverpackets.MagicSkillUser;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.templates.L2Item;
import net.sf.l2j.gameserver.templates.L2Weapon;

/**
 * This class ...
 * 
 * @version $Revision: 1.1.2.1.2.4 $ $Date: 2005/03/27 15:30:07 $
 */

public class SpiritShots implements IItemHandler
{
	// all the items ids that this handler knowns
	//FIXME: this is part is dirty, needs changing to something more intelligent
	private static int[] _itemIds = { 2510, 2511, 2512, 2513, 2514};

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
		if (weaponInst == null)
		{
			activeChar.sendPacket(new SystemMessage(SystemMessage.CANNOT_USE_SOULSHOTS));
			return;
		}

		if (weaponInst.getChargedSpiritshot() != L2ItemInstance.CHARGED_NONE)
		{
			// spiritshot is already active
			return;
		}
		
		int SpiritshotId = item.getItemId();

		int grade = weaponItem.getCrystalType();
		int spiritShotConsumption = weaponItem.getSpiritShotCount();
		int count = item.getCount();
		
		
		if (spiritShotConsumption == 0)
		{
			//	 Can't use soulshots
			activeChar.sendPacket(new SystemMessage(SystemMessage.CANNOT_USE_SOULSHOTS));
			return;
		}
		
		
		if ((grade == L2Item.CRYSTAL_NONE && SpiritshotId != 2509) || 
			(grade == L2Item.CRYSTAL_D && SpiritshotId != 2510) || 
			(grade == L2Item.CRYSTAL_C && SpiritshotId != 2511) || 
			(grade == L2Item.CRYSTAL_B && SpiritshotId != 2512) || 
			(grade == L2Item.CRYSTAL_A && SpiritshotId != 2513) || 
			(grade == L2Item.CRYSTAL_S && SpiritshotId != 2514))
		{
			//	 wrong grade for weapon
			activeChar.sendPacket(new SystemMessage(SystemMessage.SOULSHOTS_GRADE_MISMATCH));
			return;
		}
		
		
		if (!(count >= spiritShotConsumption))
		{
			//	 Not Enough Soulshots
			activeChar.sendPacket(new SystemMessage(SystemMessage.NOT_ENOUGH_SOULSHOTS));
			return;
		}
		
		weaponInst.setChargedSpiritshot(L2ItemInstance.CHARGED_SPIRITSHOT);
		activeChar.removeItemFromInventory(item, spiritShotConsumption);
		activeChar.sendPacket(new SystemMessage(342));
		L2Object OldTarget = activeChar.getTarget();
		activeChar.setTarget(activeChar);
		MagicSkillUser MSU = new MagicSkillUser(activeChar,2039,1,0,0);
		activeChar.sendPacket(MSU);
		activeChar.broadcastPacket(MSU);
		activeChar.setTarget(OldTarget);
	}
	
	public int[] getItemIds()
	{
		return _itemIds;
	}
}
