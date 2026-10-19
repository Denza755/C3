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

import javolution.util.FastList;
import net.sf.l2j.gameserver.ClientThread;
import net.sf.l2j.gameserver.model.L2TradeList;
import net.sf.l2j.gameserver.model.TradeItem;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.packets.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.packets.serverpackets.ChangeWaitType;
import net.sf.l2j.gameserver.packets.serverpackets.PrivateSellListBuy;

import java.nio.ByteBuffer;

/**
 * This class ...
 * 
 * @version $Revision: 1.2.2.1.2.4 $ $Date: 2005/03/27 15:29:30 $
 */
public class RequestPrivateStoreBuyManage extends ClientBasePacket{
	private static final String _C__90_REQUESTPRIVATESTOREBUYMANAGE = "[C] 90 RequestPrivateStoreBuyManage";
	
	
	public RequestPrivateStoreBuyManage(ByteBuffer buf, ClientThread client)
	{
		super(buf, client);
	}

	void runImpl()
	{
		L2PcInstance player = getClient().getActiveChar();
		if (player == null)
		    return;
        
        // Player shouldn't be able to set stores if he/she is alike dead (dead or fake death)
        if (player.isAlikeDead())
        {
            sendPacket(new ActionFailed());
            return;
        }
        
		switch(player.getPrivateStoreType())
        {
            case (L2PcInstance.STORE_PRIVATE_NONE):
    		{
    			if (player.isSitting())
    			{
    				player.isInStoreMode(false);
    				player.standUp();
    			}
    			
    			if (player.getTradeList() == null)
    			{
    				player.setTradeList(new L2TradeList(0));
    			}
    
                if (player.getBuyList() == null)
    			{
    				player.setBuyList(new FastList<TradeItem>());
    			}
    			player.getTradeList().updateBuyList(player,player.getBuyList());
    			player.setPrivateStoreType(4);
    			player.sendPacket(new PrivateSellListBuy(getClient().getActiveChar()));
                break;
    		}
            case (L2PcInstance.STORE_PRIVATE_BUY):
    		{
    			player.setPrivateStoreType(4);
    			player.isInStoreMode(true);
    			player.sendPacket(new PrivateSellListBuy(getClient().getActiveChar()));	
    			player.broadcastPacket(new ChangeWaitType (player,ChangeWaitType.WT_STANDING));
    			player.sitDown();
                break;
    		}
        }
	}

	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.clientpackets.ClientBasePacket#getType()
	 */
	public String getType()
	{
		return _C__90_REQUESTPRIVATESTOREBUYMANAGE;
	}
}
