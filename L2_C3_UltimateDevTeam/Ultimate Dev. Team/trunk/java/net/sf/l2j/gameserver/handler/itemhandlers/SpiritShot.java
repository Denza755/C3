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
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PlayableInstance;
import net.sf.l2j.gameserver.packets.serverpackets.ExAutoSoulShot;
import net.sf.l2j.gameserver.packets.serverpackets.MagicSkillUser;
import net.sf.l2j.gameserver.packets.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.tables.ItemTable;
import net.sf.l2j.gameserver.templates.L2Item;
import net.sf.l2j.gameserver.templates.L2Weapon;
import net.sf.l2j.gameserver.util.Broadcast;

import java.util.Set;

/** 
 * This class ... 
 *  
 * @version $Revision: 1.1.2.1.2.5 $ $Date: 2005/03/27 15:30:07 $ 
 */ 

public class SpiritShot implements IItemHandler 
{ 
	// All the item IDs that this handler knows.
	private static int[] _itemIds = { 5790, 2509, 2510, 2511, 2512, 2513, 2514 }; 
	private static int[] _skillIds = { 2061, 2155, 2156, 2157, 2158, 2159 };

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
		Set<Integer> autoSoulShots = activeChar.getAutoSoulShot();
		int SoulshotId = item.getItemId();
		boolean isAutoSoulShot = false;
		L2Item itemTemplate = ItemTable.getInstance().getTemplate( item.getItemId() );
		
		if (autoSoulShots != null && autoSoulShots.contains(SoulshotId))
		{
			isAutoSoulShot = true;
		}
		
		if (weaponInst == null)
		{
			if (isAutoSoulShot)
			{
				activeChar.removeAutoSoulShot(SoulshotId);
				activeChar.sendPacket(new ExAutoSoulShot(SoulshotId, 0));
				activeChar.sendPacket(new SystemMessage(SystemMessage.AUTO_USE_OF_S1_CANCELLED).addString(itemTemplate.getName()));
				return;
			}
            
			activeChar.sendPacket(new SystemMessage(SystemMessage.CANNOT_USE_SPIRITSHOTS));
			return;
		}

		if (weaponInst.getChargedSpiritshot() != L2ItemInstance.CHARGED_NONE)
		{
			// spiritshot is already active
			return;
		} 

		int SpiritshotId = item.getItemId();  
		int grade = weaponItem.getCrystalType(); 
		int soulSpiritConsumption = weaponItem.getSpiritShotCount(); 
		int count = item.getCount(); 

		if (soulSpiritConsumption == 0) 
		{
			//  Can't use Spiritshots
			if (isAutoSoulShot)
			{
				activeChar.removeAutoSoulShot(SoulshotId);
				activeChar.sendPacket(new ExAutoSoulShot(SoulshotId, 0));
				activeChar.sendPacket(new SystemMessage(SystemMessage.AUTO_USE_OF_S1_CANCELLED).addString(itemTemplate.getName()));                
				return;
			}
            
			activeChar.sendPacket(new SystemMessage(SystemMessage.CANNOT_USE_SPIRITSHOTS));
			return; 
		} 

		if ((grade == L2Item.CRYSTAL_NONE && SpiritshotId != 5790 && SpiritshotId != 2509) ||  
		(grade == L2Item.CRYSTAL_D && SpiritshotId != 2510) ||  
		(grade == L2Item.CRYSTAL_C && SpiritshotId != 2511) ||  
		(grade == L2Item.CRYSTAL_B && SpiritshotId != 2512) ||  
		(grade == L2Item.CRYSTAL_A && SpiritshotId != 2513) ||  
		(grade == L2Item.CRYSTAL_S && SpiritshotId != 2514)) 
		{ 
			//  wrong grade for weapon 
			if (isAutoSoulShot)
			{
				activeChar.removeAutoSoulShot(SoulshotId);
				activeChar.sendPacket(new ExAutoSoulShot(SoulshotId, 0));
				activeChar.sendPacket(new SystemMessage(SystemMessage.AUTO_USE_OF_S1_CANCELLED).addString(itemTemplate.getName()));                
				return;
			}
            
			activeChar.sendPacket(new SystemMessage(SystemMessage.SPIRITSHOTS_GRADE_MISMATCH));
			return; 
		} 

		if (!(count >= soulSpiritConsumption) && (isAutoSoulShot)) 
		{ 
			activeChar.removeAutoSoulShot(SoulshotId);
			activeChar.sendPacket(new ExAutoSoulShot(SoulshotId, 0));
			//  Not Enough Spiritshots 
			activeChar.sendPacket(new SystemMessage(SystemMessage.NOT_ENOUGH_SPIRITSHOTS));
			return; 
		} 
		else if (!(count >= soulSpiritConsumption))
		{ 
			activeChar.sendPacket(new SystemMessage(SystemMessage.NOT_ENOUGH_SPIRITSHOTS));
			return; 
		} 
		weaponInst.setChargedSpiritshot(L2ItemInstance.CHARGED_SPIRITSHOT);
		activeChar.removeItemFromInventory(item, soulSpiritConsumption);
        L2Object oldTarget = activeChar.getTarget();
        activeChar.setTarget(activeChar);

		activeChar.sendPacket(new SystemMessage(SystemMessage.ENABLED_SPIRITSHOT));
        
		MagicSkillUser MSU = new MagicSkillUser(activeChar,_skillIds[grade],1,0,0); 
        Broadcast.toSelfAndKnownPlayers(activeChar, MSU);
        activeChar.setTarget(oldTarget);        
	} 

	public int[] getItemIds() 
	{ 
		return _itemIds; 
	} 
}
