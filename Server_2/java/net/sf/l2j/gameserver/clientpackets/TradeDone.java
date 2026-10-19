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

import net.sf.l2j.gameserver.ClientThread;
import net.sf.l2j.gameserver.model.L2PcInstance;
import net.sf.l2j.gameserver.serverpackets.SendTradeDone;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;

/**
 * This class ...
 * 
 * @version $Revision: 1.6.2.2.2.2 $ $Date: 2005/03/27 15:29:30 $
 */
public class TradeDone extends ClientBasePacket
{
	private static final String _C__17_TRADEDONE = "[C] 17 TradeDone";
	private static Logger _log = Logger.getLogger(TradeDone.class.getName());

	private final int _response;
	
	public TradeDone(ByteBuffer buf, ClientThread client)
	{
		super(buf, client);
		_response = readD();
	}

	void runImpl()
	{
		L2PcInstance player = getClient().getActiveChar();
		L2PcInstance requestor = player.getTransactionRequester();
		
		//FIXME: added in requestor != null for NUllpointer exception
		if (requestor != null && requestor.getTransactionRequester() != null)
		{
			if (_response == 1 )
			{
				player.getTradeList().setConfirmedTrade(true);
				if (requestor.getTradeList().hasConfirmed())
				{
					player.getTradeList().tradeItems(player,requestor);
					requestor.getTradeList().tradeItems(requestor,player);
					requestor.sendPacket(new SendTradeDone(1));
					player.sendPacket(new SendTradeDone(1));
					requestor.getTradeList().getItems().clear();
					player.getTradeList().getItems().clear();
					SystemMessage msg = new SystemMessage(SystemMessage.TRADE_SUCCESSFUL);
					requestor.sendPacket(msg);
					player.sendPacket(msg);
					
					// clear transaction flag
					requestor.setTransactionRequester(null);
					player.setTransactionRequester(null);
				}
				else
				{
					// first party accepted the trade
					SystemMessage msg = new SystemMessage(SystemMessage.S1_CONFIRMED_TRADE);
					msg.addString(player.getName());
					requestor.sendPacket(msg);
				}
			}
			else
			{
				player.sendPacket(new SendTradeDone(0));
				requestor.sendPacket(new SendTradeDone(0));

				player.setTradeList(null);
				requestor.setTradeList(null);
				
				SystemMessage msg = new SystemMessage(SystemMessage.S1_CANCELED_TRADE);
				msg.addString(player.getName());
				requestor.sendPacket(msg);
				
				// clear transaction flag
				requestor.setTransactionRequester(null);
				player.setTransactionRequester(null);
			}
		}
		else
		{
			// trade partner logged off. trade is canceld
			player.sendPacket(new SendTradeDone(0));
			SystemMessage msg = new SystemMessage(SystemMessage.TARGET_IS_NOT_FOUND_IN_THE_GAME);
			player.sendPacket(msg);
			player.setTransactionRequester(null);
            if (requestor != null)
                requestor.setTradeList(null);
			player.setTradeList(null);
		}
	}
	
	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.clientpackets.ClientBasePacket#getType()
	 */
	public String getType()
	{
		return _C__17_TRADEDONE;
	}
}
