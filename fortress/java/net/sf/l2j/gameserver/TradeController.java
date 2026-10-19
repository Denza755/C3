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
package net.sf.l2j.gameserver;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.logging.Logger;

import net.sf.l2j.Config;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2TradeList;

/**
 * This class ...
 * 
 * @version $Revision: 1.5.4.13 $ $Date: 2005/04/06 16:13:38 $
 */
public class TradeController
{
	private static Logger _log = Logger.getLogger(TradeController.class.getName());
	private static TradeController _instance;
	
	private int _nextListId;
	private HashMap<Integer, L2TradeList> _lists;
	
	public static TradeController getInstance()
	{
		if (_instance == null)
		{
			_instance = new TradeController();
		}
		return _instance;
	}
	
	private TradeController()
	{
		_lists = new HashMap<Integer, L2TradeList>();
		
		java.sql.Connection con = null;
		int dummyItemCount = 0;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement1 = con.prepareStatement("SELECT shop_id, npc_id FROM merchant_shopids");
			ResultSet rset1 = statement1.executeQuery();
			while(rset1.next())
			{
			PreparedStatement statement = con.prepareStatement("SELECT item_id, price, shop_id, `order` FROM merchant_buylists WHERE shop_id=? ORDER BY `order` ASC");
			statement.setString(1,String.valueOf(rset1.getInt("shop_id")));
			ResultSet rset = statement.executeQuery();
			if(rset.next())
			{
				dummyItemCount++;
				L2TradeList buy1 = new L2TradeList(rset1.getInt("shop_id"));
				int itemId = rset.getInt("item_id");
				int price = rset.getInt("price");
				L2ItemInstance item = ItemTable.getInstance().createDummyItem(itemId);
				item.setPriceToSell(price);
				buy1.addItem(item);
                buy1.setNpcId(rset1.getString("npc_id"));
				while (rset.next())
				{
					dummyItemCount++;
					itemId = rset.getInt("item_id");
					price = rset.getInt("price");
					L2ItemInstance item2 = ItemTable.getInstance().createDummyItem(itemId);
					if(item2 != null)
					{
						item2.setPriceToSell(price);
						buy1.addItem(item2);
					}
					else
					{
						_log.warning("TradeController[92]: null dummy item "+itemId);
					}
				}

				_lists.put(new Integer(buy1.getListId()), buy1);
				_nextListId = Math.max(_nextListId, buy1.getListId()+1);
			}
			
			rset.close();
			statement.close();
			}
			rset1.close();
			statement1.close();
			
			if (Config.DEBUG) _log.fine("created " + dummyItemCount + " Dummy-Items for buylists");
            _log.config("TradeController: Loaded " + _lists.size() + " Buylists.");
		}
		catch (Exception e)
		{
			// problem with initializing spawn, go to next one
			_log.warning("TradeController: Buylists could not be initialized.");
            e.printStackTrace();
		} 
		finally 
		{
			try { con.close(); } catch (Exception e) {}
		}
	}
	
	public L2TradeList getBuyList(int listId)
	{
		return _lists.get(new Integer(listId));
	}


	/**
	 * @return
	 */
	public synchronized int getNextId()
	{
		return _nextListId++;
	}
}
