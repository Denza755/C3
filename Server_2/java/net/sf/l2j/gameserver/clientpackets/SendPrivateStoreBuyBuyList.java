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
package net.sf.l2j.gameserver.clientpackets;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.logging.Logger;

import net.sf.l2j.gameserver.ClientThread;
import net.sf.l2j.gameserver.model.L2PcInstance;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.TradeItem;
import net.sf.l2j.gameserver.serverpackets.ChangeWaitType;

/**
 * This class ...
 * 
 * @version $Revision: 1.2.2.1.2.4 $ $Date: 2005/03/27 15:29:30 $
 */
public class SendPrivateStoreBuyBuyList extends ClientBasePacket
{
	private static final String _C__96_SENDPRIVATESTOREBUYBUYLIST = "[C] 96 SendPrivateStoreBuyBuyList";
	private static Logger _log = Logger.getLogger(SendPrivateStoreBuyBuyList.class.getName());
	
	private final int _buyerID;
	private final int _count;
    private final int[] _items; // count * 4
    
	public SendPrivateStoreBuyBuyList (ByteBuffer buf, ClientThread client)
	{
		super(buf, client);
		_buyerID = readD();
		_count = readD();
        _items = new int[_count * 4];
		for (int i = 0; i < _count; i++)
		{
            int objectId = readD(); _items[i * 4 + 0] = objectId;
            int itemId   = readD(); _items[i * 4 + 1] = itemId;
                           readH();// don't know what this is
            int cnt      = readD(); _items[i * 4 + 2] = cnt;
            int price    = readD(); _items[i * 4 + 3] = price;
		}
	}

	void runImpl()
	{
		L2World world = L2World.getInstance();
		L2PcInstance seller = getClient().getActiveChar();
		L2PcInstance buyer = (L2PcInstance) world.findObject(_buyerID);
		ArrayList<TradeItem> buyerlist = buyer.getBuyList();
		ArrayList<TradeItem> sellerlist = new ArrayList<TradeItem>();
        
		int cost = 0;
		TradeItem temp;
		
		for (int i = 0; i < _count; i++)
		{
			temp = new TradeItem();
            int objectId = _items[i * 4 + 0];
            int itemId   = _items[i * 4 + 1];
            int cnt      = _items[i * 4 + 2];
            int price    = _items[i * 4 + 3];
			temp.setObjectId(objectId);
			temp.setItemId(itemId);
			temp.setCount(cnt);
			temp.setOwnersPrice(price);
			cost = cost + (temp.getOwnersPrice()*temp.getCount());
			sellerlist.add(temp);
		}
		
		if (buyer.getAdena() >= cost && 
            _count > 0 && 
            buyer.getPrivateStoreType() == L2PcInstance.STORE_PRIVATE_BUY)
		{
			buyer.getTradeList().BuySellItems(buyer, buyerlist, seller, sellerlist);
			buyer.getTradeList().updateBuyList(buyer, buyerlist);
			if (buyer.getBuyList().size() == 0)
			{
				buyer.setPrivateStoreType(L2PcInstance.STORE_PRIVATE_NONE);
				buyer.sendPacket(new ChangeWaitType(buyer,true));
				buyer.broadcastPacket(new ChangeWaitType(buyer,true));
				buyer.broadcastUserInfo();
			}
		}
		else
		{
			buyer.getTradeList().updateBuyList(buyer,buyerlist);
		}
	}
    
	public String getType()
	{
		return _C__96_SENDPRIVATESTOREBUYBUYLIST;
	}
}
