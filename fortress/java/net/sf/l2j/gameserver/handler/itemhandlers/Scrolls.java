/* This program is free software; you can redistribute it and/or modify
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

import net.sf.l2j.gameserver.SkillTable;
import net.sf.l2j.gameserver.handler.IItemHandler;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2PcInstance;
import net.sf.l2j.gameserver.model.L2PetInstance;
import net.sf.l2j.gameserver.model.L2PlayableInstance;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.serverpackets.MagicSkillUser;

/**
* This class ...
* 
* @version $Revision: 1.1.6.4 $ $Date: 2005/04/06 18:25:18 $
*/

public class Scrolls implements IItemHandler
{
   private static int[] _itemIds = { 3926, 3927, 3928, 3929, 3930, 3931, 3932, 
                                     3933, 3934, 3935, 5593, 5594, 5595, 6037 };

   public void useItem(L2PlayableInstance playable, L2ItemInstance item)
   {
		L2PcInstance activeChar;
		if (playable instanceof L2PcInstance)
			activeChar = (L2PcInstance)playable;
		else if (playable instanceof L2PetInstance)
			activeChar = ((L2PetInstance)playable).getOwner();
		else
			return;
		
		if (activeChar.isAllSkillsDisabled())
		{
            activeChar.sendPacket(new ActionFailed());
			return;
		}

		int itemId = item.getItemId();
	    
	    if (itemId == 3926) // Scroll of Guidance XML:2050
	    {
			activeChar.broadcastPacket(new MagicSkillUser(playable, activeChar, 2050, 1, 1, 0));
			useScroll(activeChar, 2050, 1);
			playable.removeItemFromInventory(item, 1);
		}
		else if (itemId == 3927) // Scroll of Death Whipser XML:2051
		{
			activeChar.broadcastPacket(new MagicSkillUser(playable, activeChar, 2051, 1, 1, 0));
			useScroll(activeChar, 2051, 1);
			playable.removeItemFromInventory(item, 1);
		}
		else if (itemId == 3928) // Scroll of Focus XML:2052
		{
			activeChar.broadcastPacket(new MagicSkillUser(playable, activeChar, 2052, 1, 1, 0));
			useScroll(activeChar, 2052, 1);
			playable.removeItemFromInventory(item, 1);
		}
		else if (itemId == 3929) // Scroll of Greater Acumen XML:2053
		{
			activeChar.broadcastPacket(new MagicSkillUser(playable, activeChar, 2053, 1, 1, 0));
			useScroll(activeChar, 2053, 1);
			playable.removeItemFromInventory(item, 1);
		}
		else if (itemId == 3930) // Scroll of Haste XML:2054
		{
			activeChar.broadcastPacket(new MagicSkillUser(playable, activeChar, 2054, 1, 1, 0));
			useScroll(activeChar, 2054, 1);
			playable.removeItemFromInventory(item, 1);
		}
		else if (itemId == 3931) // Scroll of Agility XML:2055
		{
			activeChar.broadcastPacket(new MagicSkillUser(playable, activeChar, 2055, 1, 1, 0));
			useScroll(activeChar, 2055, 1);
			playable.removeItemFromInventory(item, 1);
		}
		else if (itemId == 3932) // Scroll of Mystic Enpower XML:2056
		{
			activeChar.broadcastPacket(new MagicSkillUser(playable, activeChar, 2056, 1, 1, 0));
			useScroll(activeChar, 2056, 1);
			playable.removeItemFromInventory(item, 1);
		}		
		else if (itemId == 3933) // Scroll of Might XML:2057
		{
			activeChar.broadcastPacket(new MagicSkillUser(playable, activeChar, 2057, 1, 1, 0));
			useScroll(activeChar, 2057, 1);
			playable.removeItemFromInventory(item, 1);
		}		
		else if (itemId == 3934) // Scroll of Wind Walk XML:2058
		{
			activeChar.broadcastPacket(new MagicSkillUser(playable, activeChar, 2058, 1, 1, 0));
			useScroll(activeChar, 2058, 1);
			playable.removeItemFromInventory(item, 1);
		}		
		else if (itemId == 3935) // Scroll of Shield XML:2059
		{
			activeChar.broadcastPacket(new MagicSkillUser(playable, activeChar, 2059, 1, 1, 0));
			useScroll(activeChar, 2059, 1);
			playable.removeItemFromInventory(item, 1);
		}	
		else if (itemId == 6037) // Scroll of Waking XML:2170
		{
			activeChar.broadcastPacket(new MagicSkillUser(playable, playable, 2170, 1, 1, 0));
			useScroll(activeChar, 2170, 1);
			playable.removeItemFromInventory(item, 1);
		}		
    	else if (itemId == 5593 || itemId == 5594 || itemId == 5595) // SP Scrolls
	    {
    	    int amountSP = 0;
        
	        switch (itemId) {
        	case 5593: // Low Grade
                amountSP = 500;
    	        break;
	        case 5594: // Medium Grade
        	    amountSP = 5000;
                break;
    	    case 5595: // High Grade
	            amountSP = 100000;
            	break;
        	}
        
        	activeChar.broadcastPacket(new MagicSkillUser(playable, playable, 2167, 1, 1, 0));
    	    activeChar.addExpAndSp(0, amountSP);
	        activeChar.removeItemFromInventory(item, 1);
        }
	}

	public void useScroll(L2PcInstance activeChar, int magicId,int level) {
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

