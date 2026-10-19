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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.ClientThread;
import net.sf.l2j.gameserver.model.L2PcInstance;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.serverpackets.AskJoinFriend;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;

/**
 * This class ...
 * 
 * @version $Revision: 1.3.4.2 $ $Date: 2005/03/27 15:29:30 $
 */
public class RequestFriendInvite extends ClientBasePacket{
	
	private static final String _C__5E_REQUESTFRIENDINVITE = "[C] 5E RequestFriendInvite";
	private static Logger _log = Logger.getLogger(RequestFriendInvite.class.getName());

	private final String _name;
	
	public RequestFriendInvite(ByteBuffer buf, ClientThread client)
	{
		super(buf, client);
		_name = readS();
	}

	void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if(activeChar == null) return;
		java.sql.Connection con = null;
		if(activeChar.isTransactionInProgress())
		{
			activeChar.sendPacket(new SystemMessage(SystemMessage.WAITING_FOR_ANOTHER_REPLY));
			return;
		}
		L2PcInstance friend = L2World.getPlayer(_name);
		if(friend == null)
		{
			// Target is not found in the game.
			activeChar.sendPacket(new SystemMessage(SystemMessage.THE_USER_WHO_REQUESTED_TO_BECOME_FRIENDS_IS_NOT_FOUND_IN_THE_GAME));
			return;
		}
		if(friend == activeChar)
		{
			// You cannot add yourself to your own friend list.
			activeChar.sendPacket(new SystemMessage(SystemMessage.YOU_CANNOT_ADD_YOURSELF_TO_YOUR_OWN_FRIEND_LIST));
			return;
		}

		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("select char_id, friend_name from character_friends where char_id=? and friend_name=?");
			statement.setInt(1, activeChar.getObjectId());
			statement.setString(2, friend.getName());
			ResultSet rset = statement.executeQuery();
			if(rset.next())
			{
				// Player already is in your friendlist
				sendPacket(new SystemMessage(SystemMessage.S1_IS_ALREADY_ON_YOUR_FRIEND_LIST).addString(_name));
			}
			else
			{
				if(!friend.isTransactionInProgress())
				{
					// requets to become friend
					friend.setTransactionRequester(activeChar);
					activeChar.setTransactionRequester(friend);
					sendPacket(new SystemMessage(SystemMessage.S1_HAS_REQUESTED_TO_BECOME_FRIENDS).addString(_name));
					friend.sendPacket(new AskJoinFriend(activeChar.getName()));
				}
				else
				{
					sendPacket(new SystemMessage(SystemMessage.S1_IS_BUSY_PLEASE_TRY_AGAIN_LATER));
				}
			}
			rset.close();
			statement.close();
		}
		catch(Exception e)
		{
			_log.log(Level.WARNING, "could not add friend objectid: ", e);
		}
		finally
		{
			try
			{
				con.close();
			}
			catch(Exception e)
			{}
		}

		// if (activeChar.isInParty() &&
		// activeChar.getParty().isLeader(activeChar))
		// activeChar.getParty().changePartyLeader(_name);
	}
	
	
	public String getType()
	{
		return _C__5E_REQUESTFRIENDINVITE;
	}
}

