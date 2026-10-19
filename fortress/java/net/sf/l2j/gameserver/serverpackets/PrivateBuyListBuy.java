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

import net.sf.l2j.gameserver.ItemTable;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2PcInstance;
import net.sf.l2j.gameserver.model.TradeItem;
import net.sf.l2j.gameserver.templates.L2Item;

/**
 * This class ...
 * 
 * @version $Revision: 1.7.2.2.2.3 $ $Date: 2005/03/27 15:29:39 $
 */
public class PrivateBuyListBuy extends ServerBasePacket
{
	private static final String _S__D1_PRIVATEBUYLISTBUY = "[S] b8 PrivateBuyListBuy";
	private L2PcInstance _buyer;
	private L2PcInstance _seller;
	private int _sellerAdena;
	private ArrayList<TradeItem> _buyerslist;
	private ArrayList<TradeItem> _sellerslist;
	
	public PrivateBuyListBuy(L2PcInstance buyer,L2PcInstance seller)
	{
		_buyer = buyer;
		_seller = seller;
	}
	
	final void runImpl()
	{
		_sellerAdena = _seller.getAdena();
		L2ItemInstance[] sellerItems =_seller.getInventory().getItems();
		_buyerslist =_buyer.getBuyList();
		_sellerslist = new ArrayList<TradeItem>(_buyerslist.size());
		
		int count = _buyerslist.size();
		boolean add;
		
		TradeItem temp2, temp3; 
		for (int i = 0; i < count; i++)
		{
			temp2 = _buyerslist.get(i);
			add = false;
			for (L2ItemInstance item : sellerItems)
			{
				if (temp2.getItemId() == item.getItemId())
				{
					temp3 = new TradeItem();
					temp3.setCount(item.getCount());
					temp3.setItemId(item.getItemId());
					temp3.setObjectId(item.getObjectId());
					temp2.setObjectId(item.getObjectId());
					temp3.setOwnersPrice(temp2.getOwnersPrice());
					temp3.setstorePrice(temp2.getOwnersPrice());//item.getPrice());
					if (!_sellerslist.contains(temp3))
					{
						_sellerslist.add(temp3);
						add = true;
					}
					
					break;
				}
			}
			
			if (!add)
			{
				temp3 = new TradeItem();
				temp3.setCount(0);
				temp3.setItemId(temp2.getItemId());
				temp3.setOwnersPrice(temp2.getOwnersPrice());
				_sellerslist.add(temp3);
			}
		}
	}
	
	final void writeImpl()
	{
		writeC(0xb8);
		writeD(_buyer.getObjectId());
		writeD(_sellerAdena);
		int count = _sellerslist.size();
		writeD(count);

		TradeItem temp2, temp3; 
		for (int i = 0; i < count; i++)
		{
			temp2 = _buyerslist.get(i);
			temp3 =	_sellerslist.get(i);
//			L2Item tempItem2 = ItemTable.getInstance().getTemplate(temp2.getItemId());
			L2Item tempItem3 = ItemTable.getInstance().getTemplate(temp3.getItemId());
			int buyCount = temp2.getCount();
			int sellCount = temp3.getCount();
			
			writeD(temp3.getObjectId());
			writeD(temp3.getItemId()); 
			writeH(temp3.getEnchantLevel());
			if (sellCount > buyCount)//give max possible sell amount
			{
				writeD(buyCount);
			}
			else
			{
				writeD(sellCount);
			}
			
			writeD(temp3.getStorePrice());
			writeH(0);

			writeD(tempItem3.getBodyPart());
			writeH(tempItem3.getType2());
			writeD(temp3.getOwnersPrice());//buyers price
			
			if (buyCount > sellCount)  // maximum possible tradecount
			{
				writeD(sellCount);
			}
			else
			{
				writeD(buyCount);
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.serverpackets.ServerBasePacket#getType()
	 */
	public String getType()
	{
		return _S__D1_PRIVATEBUYLISTBUY;
	}
}
