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

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.ClientThread;
import net.sf.l2j.gameserver.model.L2PcInstance;
import net.sf.l2j.gameserver.model.Race;
import net.sf.l2j.gameserver.model.TradeItem;
import net.sf.l2j.gameserver.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.serverpackets.ChangeWaitType;
import net.sf.l2j.gameserver.serverpackets.PrivateStoreMsgBuy;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;

/**
 * This class ...
 * 
 * @version $Revision: 1.2.2.1.2.5 $ $Date: 2005/03/27 15:29:30 $
 */
public class SetPrivateStoreListBuy extends ClientBasePacket
{
    private static final String _C__91_SETPRIVATESTORELISTBUY = "[C] 91 SetPrivateStoreListBuy";

    //private static Logger _log = Logger.getLogger(SetPrivateStoreListBuy.class.getName());

    private final int _count;
    private final int[] _items; // count * 3
    
    public SetPrivateStoreListBuy(ByteBuffer buf, ClientThread client)
    {
        super(buf, client);
        _count = readD();
        _items = new int[_count * 3];
        for (int x = 0; x < _count; x++)
        {
            _items[x * 3 + 0] = readD(); //item id
            readH();// don't know what this is

            if (getClient().getRevision() > 560)                         
            	readH();

            _items[x * 3 + 1] = readD(); //count
            _items[x * 3 + 2] = readD(); //price 
        }
	}

	void runImpl()
	{
        int maxSlots = 0;
        L2PcInstance player = getClient().getActiveChar();
		if(player == null) return;
        if (player.getRace() == Race.dwarf)
            maxSlots = Config.MAX_PVTSTORE_SLOTS_DWARF;
        else
            maxSlots = Config.MAX_PVTSTORE_SLOTS_OTHER;
        
        if (_count <= maxSlots)
        {
            //L2TradeList tradelist = player.getTradeList();
            TradeItem temp;
            player.setBuyList(new ArrayList<TradeItem>());
            ArrayList<TradeItem> listbuy = player.getBuyList();
            int cost = 0;

            for (int x = 0; x < _count; x++)
            {
                temp = new TradeItem();
                temp.setItemId(_items[x * 3 + 0]);
                temp.setCount(_items[x * 3 + 1]);
                temp.setOwnersPrice(_items[x * 3 + 2]);
                listbuy.add(temp);
                cost = cost + (temp.getOwnersPrice() * temp.getCount());
            }

            if (_count == 0)
            {
                listbuy = null;
            }

            if (cost > player.getAdena() || cost <= 0)
            {
                //_count = 0;
                player.sendPacket(new SystemMessage(SystemMessage.THE_PURCHASE_PRICE_IS_HIGHER_THAN_THE_AMOUNT_OF_MONEY_THAT_YOU_HAVE_AND_SO_YOU_CANNOT_OPEN_A_PERSONAL_STORE));
                player.sendPacket(new ActionFailed());
                player.setPrivateStoreType(L2PcInstance.STORE_PRIVATE_NONE);
                player.isInStoreMode(false);
                player.broadcastUserInfo();
                return;
            }

            if (_count != 0)
            {
                player.setPrivateStoreType(L2PcInstance.STORE_PRIVATE_BUY);
                player.broadcastPacket(new ChangeWaitType(player, ChangeWaitType.WT_SITTING));
				player.broadcastUserInfo();
                player.sendPacket(new PrivateStoreMsgBuy(player));
                player.broadcastPacket(new PrivateStoreMsgBuy(player));
                player.isInStoreMode(true);
                player.sitDown();
            }
            else
            {
                player.setPrivateStoreType(L2PcInstance.STORE_PRIVATE_NONE);
				player.broadcastUserInfo();
				player.isInStoreMode(false);
				player.standUp();
            }
        }
        else
        {
            player.sendPacket(new SystemMessage(SystemMessage.YOU_HAVE_EXCEEDED_THE_QUANTITY_THAT_CAN_BE_INPUTTED));
            player.sendPacket(new ActionFailed());
            player.setPrivateStoreType(L2PcInstance.STORE_PRIVATE_NONE);
            player.isInStoreMode(false);
            player.broadcastUserInfo();
        }
    }

    public String getType()
    {
        return _C__91_SETPRIVATESTORELISTBUY;
    }

}
