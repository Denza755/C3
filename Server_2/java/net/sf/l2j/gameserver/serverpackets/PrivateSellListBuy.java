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
import net.sf.l2j.gameserver.model.L2TradeList;
import net.sf.l2j.gameserver.model.TradeItem;

/**
 * This class ...
 * 
 * @version $Revision: 1.3.2.1.2.4 $ $Date: 2005/03/27 15:29:40 $
 */
public class PrivateSellListBuy extends ServerBasePacket
{
	
	private static final String _S__D0_PRIVATESELLLISTBUY = "[S] b7 PrivateSellListBuy";
	private L2PcInstance _buyer;
	private int _buyerAdena;
	private L2TradeList _list;
	private TradeItem[] _buyList;
	
	public PrivateSellListBuy(L2PcInstance buyer)
	{
		_buyer = buyer;
		_buyerAdena = _buyer.getAdena();
	}
	
	final void runImpl()
	{
		L2ItemInstance[] inventory = _buyer.getInventory().getItems();
		ArrayList<TradeItem> buyList =_buyer.getBuyList();
		int count = _buyer.getInventory().getSize();
		
		_list = new L2TradeList(0);
		_buyList = buyList.toArray(new TradeItem[buyList.size()]);
		for (int i = 0; i < count; i++)
		{
			L2ItemInstance item = inventory[i];
			if ((!item.isEquipped()) && (item.getItem().getType2() != 3)&& !((item.getItem().getType2() == 4) && (item.getItem().getType1() == 4))&& !((item.getItem().getType2() == 1) && (item.getItem().getType1() == 1))&& !item.isEquipped())
			{
				_list.addItem(item);
			}
		}
		
	}
	
	final void writeImpl()
	{
		writeC(0xb7);
		//section 1 
		writeD(_buyer.getObjectId());
		writeD(_buyerAdena);
		TradeItem temp2; 
		
				
		int count = _list.getItems().size();
		//section2 
		writeD(count);//for potential sells
		
		L2ItemInstance temp;
		for (int i = 0; i < count; i++)
		{
			temp = _list.getItems().get(i);

			writeD(temp.getItemId()); 
			writeH(0);
			writeD(temp.getCount());
			writeD(temp.getPriceToSell());
			writeH(0x00);
			writeD(0);
			writeH(0x00);
		}	
		//section 3
		count = _buyList.length;
		writeD(count);//count for any items already added for sell
		if (count !=0)
		{	
			for (int i = 0; i < count; i++)
			{
				temp2 = _buyList[i];
								
				writeD(temp2.getItemId()); 
				writeH(0);
				writeD(temp2.getCount());
				writeD(temp2.getStorePrice());
				writeH(0x00);
				writeD(0);
				writeH(0x00);
				writeD(temp2.getOwnersPrice());//your price
				writeD(temp2.getStorePrice());//fixed store price
			}	
		}
	}
	
	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.serverpackets.ServerBasePacket#getType()
	 */
	public String getType()
	{
		return _S__D0_PRIVATESELLLISTBUY;
	}
}
