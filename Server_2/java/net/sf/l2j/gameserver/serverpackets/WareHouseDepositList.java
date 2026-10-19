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
import java.util.logging.Logger;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2PcInstance;
import net.sf.l2j.gameserver.templates.L2Item;

/**
 * 0x53 WareHouseDepositList  dh (h dddhh dhhh d)
 * 
 * @version $Revision: 1.4.2.1.2.4 $ $Date: 2005/03/27 15:29:39 $
 */
public class WareHouseDepositList extends ServerBasePacket
{
	private static Logger _log = Logger.getLogger(WareHouseDepositList.class.getName());
	private static final String _S__53_WAREHOUSEDEPOSITLIST = "[S] 41 WareHouseDepositList";
	private L2PcInstance _cha;
	private int _money;
	private L2ItemInstance[] _itemslist;

	public WareHouseDepositList(L2PcInstance cha)
	{
		_cha = cha;
		_money = cha.getAdena();
	}	
	
	final void runImpl()
	{
		ArrayList<L2ItemInstance> itemlist = new ArrayList<L2ItemInstance>();
		L2ItemInstance[] inventory = _cha.getInventory().getItems();
		int count = inventory.length;
		for (int i = 0; i < count; i++)
		{
			L2ItemInstance item = inventory[i];
			if ((!item.isEquipped()) && (item.getItem().getType2() != L2Item.TYPE2_QUEST))
			{
				itemlist.add(item);
				if (Config.DEBUG)
					_log.fine("item:" + item.getItem().getName() +
							" type1:" + item.getItem().getType1() + " type2:" + item.getItem().getType2());
			}
		}
		_itemslist = itemlist.toArray(new L2ItemInstance[itemlist.size()]);
		if (Config.DEBUG) _log.fine("count:"+itemlist.size());
		
	}
	
	final void writeImpl()
	{
		writeC(0x41);
		writeD(_money);
		int count = _itemslist.length;
		if (Config.DEBUG) _log.fine("count:"+count);
		writeH(count);
		
		for (int i = 0; i < count; i++)
		{
			L2ItemInstance temp = _itemslist[i];
			
			writeH(temp.getItem().getType1()); // item type1 //unconfirmed, works
			writeD(temp.getObjectId()); //unconfirmed, works
			writeD(temp.getItemId()); //unconfirmed, works
			writeD(temp.getCount()); //unconfirmed, works
			writeH(temp.getItem().getType2());	// item type2 //unconfirmed, works
			writeH(100);	// ?
			writeD(temp.getItem().getBodyPart());	// ?
			writeH(temp.getEnchantLevel());	// enchant level -confirmed
			writeH(300);	// ?
			writeH(200);	// ?
			writeD(temp.getItemId()); // item id - confimed			
		}
	}
	
	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.serverpackets.ServerBasePacket#getType()
	 */
	public String getType()
	{
		return _S__53_WAREHOUSEDEPOSITLIST;
	}
}
