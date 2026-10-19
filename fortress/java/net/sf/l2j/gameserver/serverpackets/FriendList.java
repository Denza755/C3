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
package net.sf.l2j.gameserver.serverpackets;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.model.L2PcInstance;
import net.sf.l2j.gameserver.model.L2World;


/**
 */
public class FriendList extends ServerBasePacket
{
	private static Logger _log = Logger.getLogger(FriendList.class.getName());
	private static final String _S__7A_FRIENDLIST = "[S] 7a FriendList";
	private static L2PcInstance _cha ;

	public FriendList(L2PcInstance cha)
	{
		_cha = cha;
	}	

	final void runImpl()
	{
		// no long-running tasks
	}
	
	final void writeImpl()
	{
	    java.sql.Connection con = null;
	    L2PcInstance activeChar = getClient().getActiveChar();
	    try
	    {
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("select char_id,friend_name from character_friends where char_id=? or friend_name=?");
			statement.setInt(1, activeChar.getObjectId());
			statement.setString(2, activeChar.getName());
			ResultSet rset = statement.executeQuery();

			_cha.sendPacket(new SystemMessage(SystemMessage._FRIENDS_LIST_));
			while (rset.next())
			{
			    int objectId = rset.getInt("char_id");
			    String friendName = rset.getString("friend_name");
			    L2PcInstance friend;
			    if (activeChar.getObjectId() != objectId)
			    {
			    	friend = (L2PcInstance) L2World.findObject(objectId);
			    	if (friend != null)
			    		friendName = friend.getName();
			    	else
			    	{
					    PreparedStatement statement2 = con.prepareStatement("select char_name from characters where obj_id=?");
					    statement2.setInt(1, objectId);
					    ResultSet rset2 = statement2.executeQuery();
					    if (rset2.next())
						friendName=rset2.getString("char_name");
					    statement2.close();
			    	}
			    }
			    else
					friend = L2World.getPlayer(friendName);
			    if (friend == null)
			    	_cha.sendPacket(new SystemMessage(SystemMessage.S1_CURRENTLY_OFFLINE).addString(friendName));
			    else
			    	_cha.sendPacket(new SystemMessage(SystemMessage.S1_CURRENTLY_ONLINE).addString(friendName));
			}
			_cha.sendPacket(new SystemMessage(SystemMessage.__EQUALS__));
			rset.close();
			statement.close();
	    }
	    catch (Exception e)
	    {
	    	_log.log(Level.WARNING, "Error in friendlist ",e);
	    }
	    finally
	    {
	    	try {con.close();} catch (Exception e) {}
	    }
	}
	
	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.serverpackets.ServerBasePacket#getType()
	 */
	public String getType()
	{
		return _S__7A_FRIENDLIST;
	}
}
