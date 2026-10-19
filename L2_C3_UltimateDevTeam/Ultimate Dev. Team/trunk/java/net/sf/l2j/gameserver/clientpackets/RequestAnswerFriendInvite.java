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

import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.ClientThread;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.packets.serverpackets.SystemMessage;

import java.nio.ByteBuffer;
import java.sql.PreparedStatement;
import java.util.logging.Logger;

/**
 *  sample
 *  5F 
 *  01 00 00 00
 * 
 *  format  cdd
 * 
 * 
 * @version $Revision: 1.7.4.2 $ $Date: 2005/03/27 15:29:30 $
 */
public class RequestAnswerFriendInvite extends ClientBasePacket
{
	private static final String _C__5F_REQUESTANSWERFRIENDINVITE = "[C] 5F RequestAnswerFriendInvite";
	private static Logger _log = Logger.getLogger(RequestAnswerFriendInvite.class.getName());
	
	private final int _response;
	
	public RequestAnswerFriendInvite(ByteBuffer buf, ClientThread client)
	{
		super(buf, client);
		_response = readD();
	}

	void runImpl()
	{
		L2PcInstance player = getClient().getActiveChar();
    	if(player != null)
        {
    		L2PcInstance requestor = player.getTransactionRequester();
		if (requestor == null)
		    return;

    		if (_response == 1) {
		java.sql.Connection con = null;
		try 
		{
		    con = L2DatabaseFactory.getInstance().getConnection();
		    PreparedStatement statement = con.prepareStatement("insert into character_friends (char_id,friend_name) values(?,?)");
		    statement.setInt(1, requestor.getObjectId());
		    statement.setString(2, player.getName());
		    statement.execute();
		    statement.close();
   			requestor.sendPacket(new SystemMessage(SystemMessage.INVITED_A_FRIEND));
   			//Player added to your friendlist
   			requestor.sendPacket(new SystemMessage(SystemMessage.S1_ADDED_TO_FRIENDS).addString(player.getName()));
   			//has joined as friend.
   			player.sendPacket(new SystemMessage(SystemMessage.S1_JOINED_AS_FRIEND).addString(requestor.getName()));
		} 
		catch (Exception e)
		{
		    _log.warning("could not add friend objectid: "+ e);
		}
		finally
		{
		    try { con.close(); } catch (Exception e) {}
		}
			
    		} else {
    			requestor.sendPacket(new SystemMessage(SystemMessage.FAILED_TO_INVITE_A_FRIEND));
    		}
    		
    		player.setTransactionRequester(null);
    		requestor.setTransactionRequester(null);
        }
	}

	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.clientpackets.ClientBasePacket#getType()
	 */
	public String getType()
	{
		return _C__5F_REQUESTANSWERFRIENDINVITE;
	}
}
