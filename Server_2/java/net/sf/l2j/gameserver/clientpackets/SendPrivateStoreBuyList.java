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
import net.sf.l2j.gameserver.ItemTable;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2MerchantInstance;
import net.sf.l2j.gameserver.model.L2PcInstance;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.TradeItem;
import net.sf.l2j.gameserver.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.serverpackets.ChangeWaitType;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;

/**
 * This class ...
 * 
 * @version $Revision: 1.2.2.1.2.5 $ $Date: 2005/03/27 15:29:30 $
 */
public class SendPrivateStoreBuyList extends ClientBasePacket
{
	private static final String _C__79_SENDPRIVATESTOREBUYLIST = "[C] 79 SendPrivateStoreBuyList";
	private static Logger _log = Logger.getLogger(SendPrivateStoreBuyList.class.getName());
	
	private final int _sellerID;
	private final int _count;
	private final int[] _items; // count * 3
	
	public SendPrivateStoreBuyList(ByteBuffer buf, ClientThread client)
	{
		super(buf, client);
		_sellerID = readD();
		_count = readD();
		_items = new int[_count * 3];
		for (int i = 0; i < _count ; i++)
		{
			int objectId = readD(); _items[i * 3 + 0] = objectId; 
			int cnt      = readD(); _items[i * 3 + 1] = cnt; 
			int price    = readD(); _items[i * 3 + 2] = price; 
		}
	}

	void runImpl()
	{
		L2World world;
		world = L2World.getInstance();
		L2PcInstance buyer = getClient().getActiveChar();
		ArrayList<TradeItem> buyerlist = new ArrayList<TradeItem>();

		int cost = 0;
		
		for (int i = 0; i < _count; i++)
		{
			TradeItem temp = new TradeItem();
			int objectId = _items[i * 3 + 0]; 
			int cnt      = _items[i * 3 + 1]; 
			int price    = _items[i * 3 + 2]; 
			temp.setObjectId(objectId);
			temp.setCount(cnt);
			temp.setOwnersPrice(price);
			cost = cost + (temp.getOwnersPrice()*temp.getCount());
			buyerlist.add(temp);
		}

        if (buyer.getAdena() < cost || _count <= 0)
		{
			SystemMessage msg = new SystemMessage(SystemMessage.YOU_NOT_ENOUGH_ADENA);
			buyer.sendPacket(msg);
			sendPacket(new ActionFailed());
			return;
		}
		
		ArrayList<TradeItem> sellerlist;
		L2Character _seller = (L2Character) world.findObject(_sellerID);
		if (_seller instanceof L2PcInstance)
		{
			// private shop sell 
			L2PcInstance seller = (L2PcInstance)_seller;
			if (seller.getPrivateStoreType() != L2PcInstance.STORE_PRIVATE_SELL)
			{
				sendPacket(new ActionFailed());
				return;
			}
			for (TradeItem ti : buyerlist)
				ti.setItemId(seller.getInventory().getItemByObjectId(ti.getObjectId()).getItemId());
			sellerlist = ((L2PcInstance)_seller).getSellList();
			seller.getTradeList().BuySellItems(buyer,buyerlist,seller,sellerlist);
			if (seller.getSellList().size() == 0)
			{
				seller.setPrivateStoreType(L2PcInstance.STORE_PRIVATE_NONE);
				seller.sendPacket(new ChangeWaitType (seller,true));
				seller.broadcastPacket(new ChangeWaitType (seller,true));
				seller.broadcastUserInfo();
			}
		}
		else
		{
			// lease shop sell 
			L2MerchantInstance seller = (L2MerchantInstance)_seller;
			L2ItemInstance ladena = seller.getLeaseAdena();
			for (TradeItem ti : buyerlist) {
				L2ItemInstance li = seller.getLeaseItemByObjectId(ti.getObjectId());
				if (li == null) {
					if (ti.getObjectId() == ladena.getObjectId())
					{
						buyer.addAdena(ti.getCount());
						ladena.setCount(ladena.getCount()-ti.getCount());
						ladena.updateDatabase();
					}
					continue;
				}
				int cnt = li.getCount();
				if (cnt < ti.getCount())
					ti.setCount(cnt);
				if (ti.getCount() <= 0)
					continue;
				L2ItemInstance inst = ItemTable.getInstance().createItem(li.getItemId());
				inst.setCount(ti.getCount());
				inst.setEnchantLevel(li.getEnchantLevel());
				buyer.getInventory().addItem(inst);
				li.setCount(li.getCount()-ti.getCount());
				li.updateDatabase();
				ladena.setCount(ladena.getCount()+ti.getCount()*ti.getOwnersPrice());
				ladena.updateDatabase();
			}
		}

	}
    
	public String getType()
	{
		return _C__79_SENDPRIVATESTOREBUYLIST;
	}
}
