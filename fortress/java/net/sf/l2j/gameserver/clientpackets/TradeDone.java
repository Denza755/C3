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
import net.sf.l2j.gameserver.GmListTable;
import net.sf.l2j.gameserver.model.L2PcInstance;
import net.sf.l2j.gameserver.serverpackets.ActionFailed;
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
        synchronized(getClient())
        {
        L2PcInstance player = getClient().getActiveChar();
		if(player == null) return;
		L2PcInstance requestor = player.getTransactionRequester();
		
    		if (requestor != null && requestor.getTransactionRequester() != null)
    		{
    			if (_response == 1 )
    			{
    				player.getTradeList().setConfirmedTrade(true);
    				if (requestor.getTradeList().hasConfirmed())
    				{
    					boolean trade1Valid = player.getTradeList().validateTrade(player);
    					boolean trade2Valid = requestor.getTradeList().validateTrade(requestor);
    					if (trade1Valid && trade2Valid)
    					{
    						if(player.getTradeList().getItems() == null)
    						{
    							_log.warning("TradeDone: empty player tradelist?");
    							sendPacket(new ActionFailed());
    							return;
    						}
    
    						if(requestor.getTradeList().getItems() == null)
    						{
    							_log.warning("TradeDone: empty requestor tradelist?");
    							sendPacket(new ActionFailed());
    							return;
    						}
    
    						player.getTradeList().tradeItems(player,requestor);
    						requestor.getTradeList().tradeItems(requestor,player);
    						requestor.getTradeList().getItems().clear();
    						player.getTradeList().getItems().clear();
    					}
    					requestor.sendPacket(new SendTradeDone(1));
    					player.sendPacket(new SendTradeDone(1));
    					
    					if (trade1Valid && trade2Valid)
    					{
        					SystemMessage msg = new SystemMessage(SystemMessage.TRADE_HAS_BEEN_SUCCESSFUL);
    						requestor.sendPacket(msg);
    						player.sendPacket(msg);
    					}
    					else
    					{
    
    						if (!trade2Valid)
    						{
    							String msgToSend = " " + requestor.getName() + " tried a trade dupe2";
    							_log.warning(msgToSend);
    							GmListTable.broadcastMessageToGMs(msgToSend);
    							player.sendMessage("Trade failed");
    							requestor.sendMessage("Trade failed, dont try to dupe!");
    						}
    
    						if (!trade1Valid)
    						{
    							String msgToSend = " " + player.getName() + " tried a trade dupe1";
    							_log.warning(msgToSend);
    							GmListTable.broadcastMessageToGMs(msgToSend);
    							requestor.sendMessage("Trade failed");
    							player.sendMessage("Trade failed, dont try to dupe!");
    						}
    					}
    					
    					// clear transaction flag
    					requestor.setTransactionRequester(null);
    					player.setTransactionRequester(null);
    				}
    				else
    				{
    					// first party accepted the trade
    					requestor.sendPacket(new SystemMessage(SystemMessage.S1_CONFIRMED_TRADE).addString(player.getName()));
    				}
    			}
    			else
    			{
    				player.sendPacket(new SendTradeDone(0));
    				requestor.sendPacket(new SendTradeDone(0));
    
    				player.setTradeList(null);
    				requestor.setTradeList(null);
    				
    				requestor.sendPacket(new SystemMessage(SystemMessage.S1_CANCELED_THE_TRADE).addString(player.getName()));
    				
    				// clear transaction flag
    				requestor.setTransactionRequester(null);
    				player.setTransactionRequester(null);
    			}
    		}
    		else
    		{
    			// trade partner logged off. trade is canceld
    			player.sendPacket(new SendTradeDone(0));
    			player.sendPacket(new SystemMessage(SystemMessage.TARGET_IS_NOT_FOUND_IN_THE_GAME));
    			player.setTransactionRequester(null);
    			if (requestor != null)
    				requestor.setTradeList(null);
    			player.setTradeList(null);
    		}
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
