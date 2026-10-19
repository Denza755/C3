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

import java.util.Random;

import net.sf.l2j.gameserver.handler.IItemHandler;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2PcInstance;
import net.sf.l2j.gameserver.model.L2PlayableInstance;
import net.sf.l2j.gameserver.serverpackets.Dice;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;


/**
 * This class ...
 * 
 * @version $Revision: 1.1.4.2 $ $Date: 2005/03/27 15:30:07 $
 */

public class RollingDice implements IItemHandler
{
	private static int[] _itemIds = { 4625, 4626, 4627, 4628  };
	
	public void useItem(L2PlayableInstance playable, L2ItemInstance item)
	{
		if (!(playable instanceof L2PcInstance))
			return;
		L2PcInstance activeChar = (L2PcInstance)playable;
	    int itemId = item.getItemId();
		if (itemId == 4625 || itemId == 4626 || itemId == 4627 || itemId == 4628)
		{
			
			Random generator = new Random();
			
			int number = generator.nextInt(6) + 1;
			
			Dice d = new Dice (activeChar.getObjectId(),item.getItemId(),number,activeChar.getX()-30,activeChar.getY()-30,activeChar.getZ() );
			activeChar.sendPacket(d);
			activeChar.broadcastPacket(d);
			SystemMessage sm = new SystemMessage(0x0342);
//			sm.addNumber(2);
			sm.addString(activeChar.getName());
//			sm.addNumber(1);
			sm.addNumber(number);

			activeChar.sendPacket(sm);
			activeChar.broadcastPacket(sm);
			
//			[S]0x64	system message
//			64 
//			42 03 00 00   	xxx rolled a yyy
//			02 00 00 00   	2 parameters
//			00 00 00 00   	string-type
//			41 00 41 00 41 00 41 00  00 00   aaaa
//			01 00 00 00   	number-type
//			05 00 00 00   	value 
			
			
			
		}
	}
	public int[] getItemIds()
	{
		return _itemIds;
	}
}
