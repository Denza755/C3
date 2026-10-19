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
import java.util.logging.Logger;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.BasePacket;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.TradeList;
import net.sf.l2j.gameserver.model.TradeList.TradeItem;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.util.Util;

/**
 * This class ...
 *
 * @version $Revision: 1.2.2.1.2.5 $ $Date: 2005/03/27 15:29:30 $
 */
public class RequestPrivateStoreBuy extends L2GameClientPacket
{
	private static final String _C__79_REQUESTPRIVATESTOREBUY = "[C] 79 RequestPrivateStoreBuy";
	private static Logger _log = Logger.getLogger(RequestPrivateStoreBuy.class.getName());

	private final int _storePlayerId;
	private int _count;
	private BasePacket.ItemRequest[] _items;

	public RequestPrivateStoreBuy(ByteBuffer buf, L2GameClient client)
	{
		super(buf, client);
		_storePlayerId = readD();
		_count = readD();

        // count*12 is the size of a for iteration of each item
		if(_count < 0 || _count * 12 > _buf.remaining() || _count > Config.MAX_ITEM_IN_PACKET)
		{
			_count = 0;
		}

        _items = new BasePacket.ItemRequest[_count];

		for (int i = 0; i < _count ; i++)
		{
			int objectId = readD();
			long count   = readD();
			int price    = readD();

			_items[i] = new BasePacket.ItemRequest(objectId, (int)count, price);
		}

		if(Config.DEBUG){

			_log.info("Player "+getClient().getActiveChar().getName()+" requested to buy to storeId "+_storePlayerId+" Items Number: "+_count);

			for(int i = 0; i < _count; i++)
			{
				_log.info("Requested Item ObjectID: "+_items[i].getObjectId());
				_log.info("Requested Item Id: "+_items[i].getItemId());
				_log.info("Requested Item count: "+_items[i].getCount());
				_log.info("Requested Item price: "+_items[i].getPrice());

			}
		}

    }

	@Override
	protected void runImpl()
	{
		L2PcInstance player = getClient().getActiveChar();
		if(player == null)
			return;

        L2Object object = L2World.getInstance().findObject(_storePlayerId);
		if(object == null || !(object instanceof L2PcInstance))
			return;

		L2PcInstance storePlayer = (L2PcInstance) object;
		if(!(storePlayer.getPrivateStoreType() == L2PcInstance.STORE_PRIVATE_SELL || storePlayer.getPrivateStoreType() == L2PcInstance.STORE_PRIVATE_PACKAGE_SELL))
			return;

		if(player.getAccessLevel() > 200)
		{
			player.sendMessage("Unsufficient privileges.");
			player.sendPacket(new ActionFailed());
			return;
		}

		TradeList storeList = storePlayer.getSellList();

		if(storeList == null)
			return;

		// FIXME: this check should be (and most probabliy is) done in the TradeList mechanics
		long priceTotal = 0;
		for(ItemRequest ir : _items)
		{
			if(ir.getCount() > Integer.MAX_VALUE || ir.getCount() < 0)
			{
				String msgErr = "[RequestPrivateStoreBuy] player " + getClient().getActiveChar().getName() + " tried an overflow exploit, ban this player!";
				Util.handleIllegalPlayerAction(getClient().getActiveChar(), msgErr, Config.DEFAULT_PUNISH);
				return;
			}

			TradeItem sellersItem = storeList.getItem(ir.getObjectId());

			if(sellersItem == null)
			{
				String msgErr = "[RequestPrivateStoreBuy] player " + getClient().getActiveChar().getName() + " tried to buy an item not sold in a private store (buy), ban this player!";
				Util.handleIllegalPlayerAction(getClient().getActiveChar(), msgErr, Config.DEFAULT_PUNISH);
				return;
			}

			if(ir.getPrice() != sellersItem.getPrice())
			{
				String msgErr = "[RequestPrivateStoreBuy] player " + getClient().getActiveChar().getName() + " tried to change the seller's price in a private store (buy), ban this player!";
				Util.handleIllegalPlayerAction(getClient().getActiveChar(), msgErr, Config.DEFAULT_PUNISH);
				return;
			}

			priceTotal += ir.getPrice() * ir.getCount();
		}

		// FIXME: this check should be (and most probabliy is) done in the TradeList mechanics
		if(priceTotal < 0 || priceTotal > Integer.MAX_VALUE)
		{
			String msgErr = "[RequestPrivateStoreBuy] player " + getClient().getActiveChar().getName() + " tried an overflow exploit, ban this player!";
			Util.handleIllegalPlayerAction(getClient().getActiveChar(), msgErr, Config.DEFAULT_PUNISH);
			return;
		}

        if (player.getAdena() < priceTotal)
		{
			sendPacket(new SystemMessage(SystemMessage.YOU_NOT_ENOUGH_ADENA));
			sendPacket(new ActionFailed());
			return;
		}

		if(storePlayer.getPrivateStoreType() == L2PcInstance.STORE_PRIVATE_PACKAGE_SELL)
		{
			if(storeList.getItemCount() > _count)
			{
				String msgErr = "[RequestPrivateStoreBuy] player " + getClient().getActiveChar().getName() + " tried to buy less items then sold by package-sell, ban this player for bot-usage!";
				Util.handleIllegalPlayerAction(getClient().getActiveChar(), msgErr, Config.DEFAULT_PUNISH);
				return;
			}
		}

		if(!storeList.PrivateStoreBuy(player, _items, (int) priceTotal))
		{
			sendPacket(new ActionFailed());
			//Punishment e log in audit
			Util.handleIllegalPlayerAction(storePlayer, "PrivateStore buy has failed due to invalid list or request. Player: "+player.getName(), Config.DEFAULT_PUNISH);
			_log.warning("PrivateStore buy has failed due to invalid list or request. Player: " + player.getName() + ", Private store of: " + storePlayer.getName());
			return;
		}

		if(storeList.getItemCount() == 0)
		{
			storePlayer.setPrivateStoreType(L2PcInstance.STORE_PRIVATE_NONE);
			storePlayer.broadcastUserInfo();
		}

	}

	public String getType()
	{
		return _C__79_REQUESTPRIVATESTOREBUY;
	}
}
