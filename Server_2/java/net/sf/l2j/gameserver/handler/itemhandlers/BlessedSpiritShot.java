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
import net.sf.l2j.gameserver.model.L2Summon;
import net.sf.l2j.gameserver.serverpackets.MagicSkillUser;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.templates.L2Item;
import net.sf.l2j.gameserver.templates.L2Weapon;

/** 
 * This class ... 
 *  
 * @version $Revision: 1.1.2.1.2.5 $ $Date: 2005/03/27 15:30:07 $ 
 */ 

public class BlessedSpiritShot implements IItemHandler 
{ 
	// all the items ids that this handler knowns 
	private static int[] _itemIds = { 3947, 3948, 3949, 3950, 3951, 3952 };
	private static int[] _skillIds = { 2061, 2160, 2161, 2162, 2163, 2164 };

	/* (non-Javadoc) 
	 * @see net.sf.l2j.gameserver.handler.IItemHandler#useItem(net.sf.l2j.gameserver.model.L2PcInstance, net.sf.l2j.gameserver.model.L2ItemInstance) 
	 */ 
	public void useItem(L2PlayableInstance playable, L2ItemInstance item) 
	{ 
		if (!(playable instanceof L2PcInstance))
		{
			((L2Summon)playable).getOwner().sendPacket(new SystemMessage(SystemMessage.CANNOT_USE_SPIRITSHOTS));
			return;
		}

		L2PcInstance activeChar = (L2PcInstance)playable;
		L2ItemInstance weaponInst = activeChar.getActiveWeaponInstance();
		L2Weapon weaponItem = activeChar.getActiveWeaponItem();
		if (weaponInst == null)
		{
			activeChar.sendPacket(new SystemMessage(SystemMessage.CANNOT_USE_SPIRITSHOTS));
			return;
		}

		if (activeChar.isCastingNow()) 
		{ 
			// Chars are in cast state 
			return; 
		}
		
		if (weaponInst.getChargedSpiritshot() == L2ItemInstance.CHARGED_BLESSED_SPIRITSHOT)
		{
			// already charged by blessed spirit shot
			// btw we cant charge only when bsps is charged
			return;
		}		

		int spiritshotId = item.getItemId(); 
		int grade = weaponItem.getCrystalType(); 
		int blessedsoulSpiritConsumption = weaponItem.getSpiritShotCount(); 
		int count = item.getCount(); 


		if (blessedsoulSpiritConsumption == 0) 
		{ 
			//  Can't use Spiritshots 
			activeChar.sendPacket(new SystemMessage(SystemMessage.CANNOT_USE_SPIRITSHOTS));
			return; 
		} 


		if ((grade == L2Item.CRYSTAL_NONE && spiritshotId != 3947) ||  
		(grade == L2Item.CRYSTAL_D && spiritshotId != 3948) ||  
		(grade == L2Item.CRYSTAL_C && spiritshotId != 3949) ||  
		(grade == L2Item.CRYSTAL_B && spiritshotId != 3950) ||  
		(grade == L2Item.CRYSTAL_A && spiritshotId != 3951) ||  
		(grade == L2Item.CRYSTAL_S && spiritshotId != 3952)) 
		{ 
			SystemMessage sm = new SystemMessage(614); 
			sm.addString( "Wrong BlessedSpiritShot grade for your weapon" ); 
			activeChar.sendPacket(sm); 
			return; 
		} 


		if (!(count >= blessedsoulSpiritConsumption)) 
		{ 
			//  Not Enough Spiritshots 
			activeChar.sendPacket(new SystemMessage(SystemMessage.NOT_ENOUGH_SPIRITSHOTS));
			return; 
		} 

		weaponInst.setChargedSpiritshot(L2ItemInstance.CHARGED_BLESSED_SPIRITSHOT); 
		activeChar.removeItemFromInventory(item, blessedsoulSpiritConsumption);
		activeChar.sendPacket(new SystemMessage(533));//????? 
		L2Object OldTarget = activeChar.getTarget(); 
		activeChar.setTarget(activeChar); 
		MagicSkillUser MSU = new MagicSkillUser(activeChar,_skillIds[grade],1,0,0); 
		activeChar.sendPacket(MSU); 
		activeChar.broadcastPacket(MSU); 
		activeChar.setTarget(OldTarget); 
	} 

	public int[] getItemIds() 
	{ 
		return _itemIds; 
	} 
}
