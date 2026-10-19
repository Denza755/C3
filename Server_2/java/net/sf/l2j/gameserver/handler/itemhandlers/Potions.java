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

import net.sf.l2j.gameserver.IdFactory;
import net.sf.l2j.gameserver.SkillTable;
import net.sf.l2j.gameserver.handler.IItemHandler;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2PcInstance;
import net.sf.l2j.gameserver.model.L2PetInstance;
import net.sf.l2j.gameserver.model.L2PlayableInstance;
import net.sf.l2j.gameserver.model.L2Potion;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.serverpackets.MagicSkillUser;


/**
 * This class ...
 * 
 * @version $Revision: 1.2.4.4 $ $Date: 2005/03/27 15:30:07 $
 */

public class Potions implements IItemHandler
{
	private static int[] _itemIds = { 65, 725, 726, 727, 728, 734, 735, 1060, 1061, 1062, 1374, 1375, 1539, 1540, 1831, 6035, 6036 };
	
	public void useItem(L2PlayableInstance playable, L2ItemInstance item)
	{
		L2PcInstance activeChar;
		if (playable instanceof L2PcInstance)
			activeChar = (L2PcInstance)playable;
		else if (playable instanceof L2PetInstance)
			activeChar = ((L2PetInstance)playable).getOwner();
		else
			return;
		
		if (activeChar.isAllSkillsDisabled() || activeChar.isSitting())
		{
            ActionFailed af = new ActionFailed();
            activeChar.sendPacket(af);
			return;
		}
			
		L2Potion potion = new L2Potion(IdFactory.getInstance().getNextId());
	    int itemId = item.getItemId();

	    /*
	     * Mana potions
	     */
	    if (itemId == 726) // mana drug, xml: 2003
	    {
			MagicSkillUser MSU = new MagicSkillUser(playable, activeChar, 2003, 1, 1, 0);
			activeChar.sendPacket(MSU);
			activeChar.broadcastPacket(MSU);
//			usePotion(activeChar, 2003, 1); // skill mana heal over time is not implemented.
			potion.setCurrentMpPotion1(playable, itemId);
			playable.removeItemFromInventory(item, 1);
		}
		else if (itemId == 728) // mana_potion, xml: 2005
		{
			MagicSkillUser MSU = new MagicSkillUser(playable, activeChar, 2005, 1, 1, 0);
			activeChar.sendPacket(MSU);
			activeChar.broadcastPacket(MSU);
			usePotion(activeChar, 2005, 1);
			playable.removeItemFromInventory(item, 1);
		}
		/*
		 * Healing and speed potions
		 */
		else if (itemId == 65) // red_potion, xml: 2001
		{
			MagicSkillUser MSU = new MagicSkillUser(playable, activeChar, 2001, 1, 1, 0);
			activeChar.sendPacket(MSU);
			activeChar.broadcastPacket(MSU);
			usePotion(activeChar, 2001, 1);
			playable.removeItemFromInventory(item, 1);
		}
		else if (itemId == 725) // healing_drug, xml: 2002
		{
			MagicSkillUser MSU = new MagicSkillUser(playable, activeChar, 2002, 1, 1, 0);
			activeChar.sendPacket(MSU);
			activeChar.broadcastPacket(MSU);
			usePotion(activeChar, 2002, 1);
			playable.removeItemFromInventory(item, 1);
		}	 
		else if (itemId == 727) // _healing_potion, xml: 2032
		{
			MagicSkillUser MSU = new MagicSkillUser(playable, activeChar, 2032, 1, 1, 0);
			activeChar.sendPacket(MSU);
			activeChar.broadcastPacket(MSU);
			usePotion(activeChar, 2032, 1);
			playable.removeItemFromInventory(item, 1);
		}	 
		else if (itemId == 734) // quick_step_potion, xml: 2011
		{
			MagicSkillUser MSU = new MagicSkillUser(playable, activeChar, 2011, 1, 1, 0);
			activeChar.sendPacket(MSU);
			activeChar.broadcastPacket(MSU);
			usePotion(activeChar, 2011, 1);
			playable.removeItemFromInventory(item, 1);
		}
		else if (itemId == 735) // swift_attack_potion, xml: 2012
		{
			MagicSkillUser MSU = new MagicSkillUser(playable, activeChar, 2012, 1, 1, 0);
			activeChar.sendPacket(MSU);
			activeChar.broadcastPacket(MSU);
			usePotion(activeChar, 2012, 1);
			playable.removeItemFromInventory(item, 1);
		}
		else if (itemId == 1060) // lesser_healing_potion, xml: 2031
		{
			MagicSkillUser MSU = new MagicSkillUser(playable, activeChar, 2031, 1, 1, 0);
			activeChar.sendPacket(MSU);
			activeChar.broadcastPacket(MSU);
			usePotion(activeChar, 2031, 1);
			playable.removeItemFromInventory(item, 1);
		}	 
		else if (itemId == 1061) // healing_potion, xml: 2032
		{
			MagicSkillUser MSU = new MagicSkillUser(playable, activeChar, 2032, 1, 1, 0);
			activeChar.sendPacket(MSU);
			activeChar.broadcastPacket(MSU);
			usePotion(activeChar, 2032, 1);
			playable.removeItemFromInventory(item, 1);
		}  
		else if (itemId == 1062) // haste_potion, xml: 2033
		{
			MagicSkillUser MSU = new MagicSkillUser(playable, activeChar, 2033, 1, 1, 0);
			activeChar.sendPacket(MSU);
			activeChar.broadcastPacket(MSU);
			usePotion(activeChar, 2033, 1);	
			playable.removeItemFromInventory(item, 1);
		}
		else if (itemId == 1374) // adv_quick_step_potion, xml: 2034
		{
			MagicSkillUser MSU = new MagicSkillUser(playable, activeChar, 2034, 1, 1, 0);
			activeChar.sendPacket(MSU);
			activeChar.broadcastPacket(MSU);
			usePotion(activeChar, 2034, 1);
			playable.removeItemFromInventory(item, 1);
		}
		else if (itemId == 1375) // adv_swift_attack_potion, xml: 2035
		{
			MagicSkillUser MSU = new MagicSkillUser(playable, activeChar, 2035, 1, 1, 0);
			activeChar.sendPacket(MSU);
			activeChar.broadcastPacket(MSU);
			usePotion(activeChar, 2035, 1);
			playable.removeItemFromInventory(item, 1);
		}
		else if (itemId == 1539) // greater_healing_potion, xml: 2037
		{
			MagicSkillUser MSU = new MagicSkillUser(playable, activeChar, 2037, 1, 1, 0);
			activeChar.sendPacket(MSU);
			activeChar.broadcastPacket(MSU);
			usePotion(activeChar, 2037, 1);
			playable.removeItemFromInventory(item, 1);
		}	 
		else if (itemId == 1540) // quick_healing_potion, xml: 2038
		{
			MagicSkillUser MSU = new MagicSkillUser(playable, activeChar, 2038, 1, 1, 0);
			activeChar.sendPacket(MSU);
			activeChar.broadcastPacket(MSU);
			usePotion(activeChar, 2038, 1);
			playable.removeItemFromInventory(item, 1);
		}	 
		else if (itemId == 1831) // antidote, xml: 2042
		{
			MagicSkillUser MSU = new MagicSkillUser(playable, activeChar, 2042, 1, 1, 0);
			activeChar.sendPacket(MSU);
			activeChar.broadcastPacket(MSU);
			usePotion(activeChar, 2042, 1);
			playable.removeItemFromInventory(item, 1);
		}
	    else if (itemId == 6035) // Magic Haste Potion, xml: 2169
	    {
	    	MagicSkillUser MSU = new MagicSkillUser(playable, activeChar, 2169, 1, 1, 0);
	    	activeChar.sendPacket(MSU);
	    	activeChar.broadcastPacket(MSU);
	    	usePotion(activeChar, 2169, 1);
	    	playable.removeItemFromInventory(item, 1);
	    }	    
	    else if (itemId == 6036) // Greater Magic Haste Potion, xml: 2169
	    {
	    	MagicSkillUser MSU = new MagicSkillUser(playable, activeChar, 2169, 1, 1, 0);
	    	activeChar.sendPacket(MSU);
	    	activeChar.broadcastPacket(MSU);
	    	usePotion(activeChar, 2169, 2);
	    	playable.removeItemFromInventory(item, 1);
	    }	    
	}
	public void usePotion(L2PcInstance activeChar, int magicId,int level) {
		L2Skill skill = SkillTable.getInstance().getInfo(magicId,level);
		if (skill != null) {
			activeChar.useMagic(skill, false, false);
		}
	}
	public int[] getItemIds()
	{
		return _itemIds;
	}
}
