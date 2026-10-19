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
import net.sf.l2j.gameserver.serverpackets.SystemMessage;

/**
 * This class ...
 * 
 * @version $Revision: 1.3.4.2 $ $Date: 2005/03/27 15:29:30 $
 */
public class RequestFriendDel extends ClientBasePacket{
	
	private static final String _C__61_REQUESTFRIENDDEL = "[C] 61 RequestFriendDel";
	private static Logger _log = Logger.getLogger(RequestFriendDel.class.getName());

	private final String _name;
	
	public RequestFriendDel(ByteBuffer buf, ClientThread client)
	{
		super(buf, client);
		_name = readS();
	}

	void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if(activeChar == null) return;
		java.sql.Connection con = null;
		try 
		{
		    L2PcInstance friend = L2World.getPlayer(_name);
		    con = L2DatabaseFactory.getInstance().getConnection();
		    PreparedStatement statement;
		    ResultSet rset;
		    if (friend != null){
			statement = con.prepareStatement("select char_id, friend_name from character_friends where (char_id=? and friend_name=?) or (char_id=? and friend_name=?)");
			statement.setInt(1, activeChar.getObjectId());
			statement.setString(2, friend.getName());
			statement.setInt(3, friend.getObjectId());
			statement.setString(4, activeChar.getName());
			rset = statement.executeQuery();
			if (!rset.next()){
			    statement.close();
			    //Player is not in your friendlist
			    activeChar.sendPacket(new SystemMessage(SystemMessage.S1_IS_NOT_ON_YOUR_FRIEND_LIST).addString(_name));
			    return;
			}
		    } else {
			statement = con.prepareStatement("select char_id, friend_name from character_friends where char_id=? and friend_name=?");
			statement.setInt(1, activeChar.getObjectId());
			statement.setString(2, _name);
			rset = statement.executeQuery();
			if (!rset.next()){
			    statement.close();
			    statement = con.prepareStatement("select obj_id from characters where char_name=?");
			    statement.setString(1, _name);
			    rset = statement.executeQuery();
			    //if not found in characters table
			    if (!rset.next()){
				statement.close();
				//Player is not in your friendlist
				activeChar.sendPacket(new SystemMessage(SystemMessage.S1_IS_NOT_ON_YOUR_FRIEND_LIST).addString(_name));
				return;
			    }
			    statement = con.prepareStatement("select char_id, friend_name from character_friends where (char_id=? and friend_name=?)");
			    statement.setInt(1, rset.getInt("obj_id"));
			    statement.setString(2, activeChar.getName());
			    rset = statement.executeQuery();
			    if (!rset.next()){
				statement.close();
				//Player is not in your friendlist
				activeChar.sendPacket(new SystemMessage(SystemMessage.S1_IS_NOT_ON_YOUR_FRIEND_LIST).addString(_name));
				return;
			    }
			}
		    }
			int objectId = rset.getInt("char_id");
			String objectName = rset.getString("friend_name");
			statement.close();
			statement = con.prepareStatement("delete from character_friends where char_id=? and friend_name=?");
			statement.setInt(1, objectId);
			statement.setString(2, objectName);
			statement.execute();
			//Player deleted from your friendlist
			activeChar.sendPacket(new SystemMessage(SystemMessage.S1_HAS_BEEN_REMOVED_FROM_YOUR_FRIEND_LIST).addString(_name));
			if (friend != null){
			    friend.sendPacket(new SystemMessage(SystemMessage.S1__HAS_BEEN_DELETED_FROM_YOUR_FRIENDS_LIST).addString(activeChar.getName()));
			}
			rset.close();
			statement.close();
		} 
		catch (Exception e)
		{
		    _log.log(Level.WARNING, "could not del friend objectid: ", e);
		}
		finally
		{
		    try { con.close(); } catch (Exception e) {}
		}
		
	}
	
	
	public String getType()
	{
		return _C__61_REQUESTFRIENDDEL;
	}
}

