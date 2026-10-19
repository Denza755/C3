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

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.ClientThread;
import net.sf.l2j.gameserver.model.L2TradeList;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.packets.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.packets.serverpackets.SendTradeDone;
import net.sf.l2j.gameserver.packets.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.util.Util;

import java.nio.ByteBuffer;
import java.util.logging.Logger;
//import net.sf.l2j.gameserver.lib.Log;

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

		if (player == null || player.isProcessingTransaction())
		    return;
		
		L2PcInstance requestor = player.getTransactionRequester();
		
                if (requestor == null)
                   return;
		//FIXME: added in requestor != null for NUllpointer exception
		if (requestor != null && requestor.getTransactionRequester() != null && !requestor.isProcessingTransaction())
		{
            requestor.setIsProcessingTransaction(true);
            player.setIsProcessingTransaction(true);

			if (_response == 1 )
			{
				L2TradeList tradeList = player.getTradeList();
		if (tradeList == null)
			return;
                tradeList.setConfirmedTrade(true);
				L2TradeList tradeList2 = requestor.getTradeList();
		if (tradeList2 == null)
			return;
                if (tradeList2.hasConfirmed())
				{
					boolean trade1Valid = tradeList.validateTrade(player);
					boolean trade2Valid = tradeList2.validateTrade(requestor);
					if (trade1Valid && trade2Valid)
					{
                        if(tradeList.getItems() == null)
                        {
                            _log.warning("TradeDone: empty player tradelist?");   
                            sendPacket(new ActionFailed());   
                            return;   
                        }   
     
                        if(requestor.getTradeList().getItems() == null)   
                        {   
                            _log.warning("TradeDone: empty requestor tradelist?"); 
                            sendPacket(new ActionFailed());
                            requestor.setIsProcessingTransaction(false);
                            player.setIsProcessingTransaction(false);
                            return;
                        }
                        boolean requestorFull = requestor.getInventory().validateCapacity(tradeList.getItems()); 
                        boolean playerFull = player.getInventory().validateCapacity(tradeList2.getItems());
                        
                        if (!requestorFull || !playerFull)
                        {
                            player.sendPacket(new SendTradeDone(0));
                            requestor.sendPacket(new SendTradeDone(0));
   
                            player.setTradeList(null);
                            requestor.setTradeList(null);
                            
                            if (!requestorFull)
                            {
                                SystemMessage msg = new SystemMessage(SystemMessage.S1_CANCELED_TRADE);
                                msg.addString(requestor.getName());
                                player.sendPacket(msg);
                                
                                requestor.sendPacket(new SystemMessage(SystemMessage.SLOTS_FULL));
                            }
                            
                            if (!playerFull)
                            {
                                SystemMessage msg = new SystemMessage(SystemMessage.S1_CANCELED_TRADE);
                                msg.addString(player.getName());
                                requestor.sendPacket(msg);
                                
                                player.sendPacket(new SystemMessage(SystemMessage.SLOTS_FULL));
                            }
                            
                            // clear transaction flag
                            requestor.setTransactionRequester(null);
                            player.setTransactionRequester(null);
                            
                            requestor.setIsProcessingTransaction(false);
                            player.setIsProcessingTransaction(false);
                            
                            return;
                        }
                        
                        if (tradeList != null && tradeList2 != null)
                        {
                            tradeList.tradeItems(player,requestor);
    					    tradeList2.tradeItems(requestor,player);
    					    tradeList2.getItems().clear();
    					    tradeList.getItems().clear();
                        }
                        else
                        {
                            return;
                        }
					}
					requestor.sendPacket(new SendTradeDone(1));
					player.sendPacket(new SendTradeDone(1));
					
					if (trade1Valid && trade2Valid)
					{
					    requestor.sendPacket(new SystemMessage(SystemMessage.TRADE_SUCCESSFUL));
					    player.sendPacket(new SystemMessage(SystemMessage.TRADE_SUCCESSFUL));
					}
					else
					{
					    if (!trade2Valid)
					    {
					        String msgToSend = " " + requestor.getName() + " tried a trade dupe";
                            Util.handleIllegalPlayerAction(requestor,msgToSend,_log,Config.DEFAULT_PUNISH);
					        player.sendMessage("Trade failed");
					    }
					    if (!trade1Valid)
					    {
					        String msgToSend = " " + player.getName() + " tried a trade dupe";
					        requestor.sendMessage("Trade failed");
                            Util.handleIllegalPlayerAction(player,msgToSend,_log,Config.DEFAULT_PUNISH);
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
				
				SystemMessage msg = new SystemMessage(SystemMessage.S1_CANCELED_TRADE);
				msg.addString(player.getName());
				requestor.sendPacket(msg);
				
				// clear transaction flag
				requestor.setTransactionRequester(null);
				player.setTransactionRequester(null);
			}
            requestor.setIsProcessingTransaction(false);
            player.setIsProcessingTransaction(false);
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
			player.setIsProcessingTransaction(false);
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
