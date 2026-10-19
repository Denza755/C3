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

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.ClientThread;
import net.sf.l2j.gameserver.ItemTable;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2MerchantInstance;
import net.sf.l2j.gameserver.model.L2PcInstance;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.TradeItem;
import net.sf.l2j.gameserver.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.util.Util;

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
			_items[i * 3 + 0] = readD(); //object id
			_items[i * 3 + 1] = readD(); //count
			_items[i * 3 + 2] = readD(); //price
		}
	}

	void runImpl()
	{
		L2PcInstance buyer = getClient().getActiveChar();
		if(buyer == null) return;
		ArrayList<TradeItem> buyerlist = new ArrayList<TradeItem>();

		int cost = 0;

		for (int i = 0; i < _count; i++)
		{
			TradeItem temp = new TradeItem();

			temp.setObjectId(_items[i * 3 + 0]);
			temp.setCount(_items[i * 3 + 1]);
			temp.setOwnersPrice(_items[i * 3 + 2]);
			cost = cost + (temp.getOwnersPrice()*temp.getCount());
			buyerlist.add(temp);
		}

        if (buyer.getAdena() < cost || _count <= 0)
		{
			buyer.sendPacket(new SystemMessage(SystemMessage.YOU_DO_NOT_HAVE_ENOUGH_ADENA));
			sendPacket(new ActionFailed());
			return;
		}

        if(cost < 0 || cost > Integer.MAX_VALUE)
        {
            String msgErr = "[SendPrivateStoreBuyList] player "+buyer.getName()+" tried an overflow exploit, ban this player!";
            Util.handleIllegalPlayerAction(buyer,msgErr,_log,Config.DEFAULT_PUNISH);
            return;
        }

		ArrayList<TradeItem> sellerlist;
		L2Character _seller = (L2Character) L2World.findObject(_sellerID);
		if (_seller != null && _seller instanceof L2PcInstance)
		{
			// private shop sell
			L2PcInstance seller = (L2PcInstance)_seller;
			if (seller.getPrivateStoreType() != L2PcInstance.STORE_PRIVATE_SELL)
			{
				sendPacket(new ActionFailed());
				return;
			}
			for (TradeItem ti : buyerlist)
			{
				if(seller.getInventory().getItemByObjectId(ti.getObjectId()) == null)
				{
					continue;
				}
				ti.setItemId(seller.getInventory().getItemByObjectId(ti.getObjectId()).getItemId());
			}
			sellerlist = ((L2PcInstance)_seller).getSellList();
            
            if(seller.getTradeList() == null)
            {
                sendPacket(new ActionFailed());
                _log.warning("TradeList == null for player "+seller.getName());
                return;
            }

			seller.getTradeList().buySellItems(buyer,buyerlist,seller,sellerlist);
			if (seller.getSellList().size() == 0)
			{
				seller.setPrivateStoreType(L2PcInstance.STORE_PRIVATE_NONE);
                seller.isInStoreMode(false);
				/*seller.sendPacket(new ChangeWaitType (seller,true));
				seller.broadcastPacket(new ChangeWaitType (seller,true));*/
				seller.broadcastUserInfo();
			}
		}
		else if (_seller != null)
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
