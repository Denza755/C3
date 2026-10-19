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
package net.sf.l2j.gameserver.serverpackets;

import java.util.ArrayList;

import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2PcInstance;

/**
 * This class ...
 * 
 * @version $Revision: 1.4.2.1.2.3 $ $Date: 2005/03/27 15:29:39 $
 */
public class TradeStart extends ServerBasePacket{
	private static final String _S__2E_TRADESTART = "[S] 1E TradeStart";
	private L2PcInstance _me;
	private ArrayList<L2ItemInstance> _tradelist = new ArrayList<L2ItemInstance>();
	public TradeStart (L2PcInstance me)
	{
		_me = me;
	}
	
	final void runImpl()
	{
		L2ItemInstance[] inventory = _me.getInventory().getItems();
		int count = _me.getInventory().getSize();
		for (int i = 0; i < count; i++)
		{
			L2ItemInstance item = inventory[i];
			if ((!item.isEquipped()) && (item.getItem().getType2() != 3))
			{
				_tradelist.add(item);
			}
		}
	}
	
	final void writeImpl()
	{//0x2e TradeStart   d h (h dddhh dhhh)

		writeC(0x1E);
		writeD(_me.getTransactionRequester().getObjectId());
		
		int count = _tradelist.size();
		
		writeH(count);//count??
		L2ItemInstance temp;
		int type;
		for (int i = 0; i < count; i++)
		{
			temp = _tradelist.get(i);
			type = temp.getItem().getType1();
			
			writeH(type); // item type1
			writeD(temp.getObjectId());
			writeD(temp.getItemId());
			writeD(temp.getCount());
			writeH(temp.getItem().getType2());	// item type2
			writeH(0x00);	// ?

			writeD(temp.getItem().getBodyPart());	// rev 415  slot    0006-lr.ear  0008-neck  0030-lr.finger  0040-head  0080-??  0100-l.hand  0200-gloves  0400-chest  0800-pants  1000-feet  2000-??  4000-r.hand  8000-r.hand
			writeH(temp.getEnchantLevel());	// enchant level
			writeH(0x00);	// ?
			writeH(0x00);
		}
	}

	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.serverpackets.ServerBasePacket#getType()
	 */
	public String getType()
	{
		return _S__2E_TRADESTART;
	}
}
