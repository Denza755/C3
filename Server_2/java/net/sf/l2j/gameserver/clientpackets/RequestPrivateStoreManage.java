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
import net.sf.l2j.gameserver.model.L2TradeList;
import net.sf.l2j.gameserver.model.TradeItem;
import net.sf.l2j.gameserver.serverpackets.ChangeWaitType;
import net.sf.l2j.gameserver.serverpackets.PrivateSellListSell;

/**
 * This class ...
 * 
 * @version $Revision: 1.2.2.1.2.4 $ $Date: 2005/03/27 15:29:30 $
 */
public class RequestPrivateStoreManage extends ClientBasePacket
{
	private static final String _C__73_REQUESTPRIVATESTOREMANAGE = "[C] 73 RequestPrivateStoreManage";
	private static Logger _log = Logger.getLogger(RequestPrivateStoreManage.class.getName());
	
	
	public RequestPrivateStoreManage(ByteBuffer buf, ClientThread client)
	{
		super(buf, client);
	}

	void runImpl()
	{
		L2PcInstance player = getClient().getActiveChar();
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
    			if (player.getSellList() == null)
    			{
    				player.setSellList(new ArrayList<TradeItem>());
    			}
    			player.getTradeList().updateSellList(player,player.getSellList());
    			player.setPrivateStoreType(2);
    			player.sendPacket(new PrivateSellListSell(getClient().getActiveChar()));
				player.broadcastUserInfo();
                break;
    		}
            case (L2PcInstance.STORE_PRIVATE_SELL):
            {
    			player.setPrivateStoreType(2);
    			player.isInStoreMode(true);
    			player.sendPacket(new PrivateSellListSell(getClient().getActiveChar()));	
    			player.sendPacket(new ChangeWaitType (player,true));
    			player.broadcastPacket(new ChangeWaitType (player,true));
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
		return _C__73_REQUESTPRIVATESTOREMANAGE;
	}
}
