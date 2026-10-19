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

import net.sf.l2j.gameserver.ClientThread;
import net.sf.l2j.gameserver.model.L2PcInstance;
import net.sf.l2j.gameserver.model.L2TradeList;
import net.sf.l2j.gameserver.serverpackets.SendTradeDone;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.serverpackets.TradeStart;

/**
 * This class ...
 * 
 * @version $Revision: 1.5.4.2 $ $Date: 2005/03/27 15:29:30 $
 */
public class AnswerTradeRequest extends ClientBasePacket{
	private static final String _C__40_ANSWERTRADEREQUEST = "[C] 40 AnswerTradeRequest";
	
	private final int _response;
	
	public AnswerTradeRequest(ByteBuffer buf, ClientThread client)
	{
		super(buf, client);
		_response = readD();
	}

	void runImpl()
	{
		L2PcInstance player = getClient().getActiveChar();
		if(player == null) return;
		L2PcInstance requestor = player.getTransactionRequester();

		// trade partner logged off. trade is canceld
		if(requestor.getTransactionRequester() == null)
		{
			if(_response != 0)
			{
				player.sendPacket(new SendTradeDone(0));
				player.sendPacket(new SystemMessage(SystemMessage.TARGET_IS_NOT_FOUND_IN_THE_GAME));
				player.setTransactionRequester(null);
			}
			return;
		}

		if(_response != 1)
		{
			requestor.sendPacket(new SystemMessage(SystemMessage.S1_DENIED_YOUR_REQUEST_FOR_TRADE).addString(player.getName()));
			requestor.setTransactionRequester(null);
			player.setTransactionRequester(null);
			return;
		}
		
		requestor.sendPacket(new SystemMessage(SystemMessage.BEGIN_TRADING_WITH_S1).addString(player.getName()));
		requestor.sendPacket(new TradeStart(requestor));
		if(requestor.getTradeList() == null)
			requestor.setTradeList(new L2TradeList(0));

		player.sendPacket(new SystemMessage(SystemMessage.BEGIN_TRADING_WITH_S1).addString(requestor.getName()));
		player.sendPacket(new TradeStart(player));
		if(player.getTradeList() == null)
			player.setTradeList(new L2TradeList(0));
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.l2j.gameserver.clientpackets.ClientBasePacket#getType()
	 */
	public String getType()
	{
		return _C__40_ANSWERTRADEREQUEST;
	}
}
