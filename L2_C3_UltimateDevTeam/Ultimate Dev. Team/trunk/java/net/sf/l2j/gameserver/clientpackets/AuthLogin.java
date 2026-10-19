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
import net.sf.l2j.gameserver.LoginServerThread;
import net.sf.l2j.gameserver.taskmanager.TaskPriority;

import java.nio.ByteBuffer;
import java.util.logging.Logger;

/**
 * This class ...
 * 
 * @version $Revision: 1.9.2.3.2.4 $ $Date: 2005/03/27 15:29:30 $
 */
public class AuthLogin extends ClientBasePacket
{
	private static final String _C__08_AUTHLOGIN = "[C] 08 AuthLogin";
	private static Logger _log = Logger.getLogger(AuthLogin.class.getName());
	
	// loginName + keys must match what the loginserver used.  
	private final String _loginName;
	private final long _key1;
	private final long _key2;

	/**
	 * @param decrypt
	 */
	public AuthLogin(ByteBuffer buf, ClientThread client)
	{
		super(buf, client);
		
		_loginName = readS().toLowerCase();
		_key1  = readD();	// these
		_key2  = readD();	// are given by login server after server selection
		// ignore the rest
		while (buf.hasRemaining()) buf.get();
	}

	/** urgent messages, execute immediatly */
    public TaskPriority getPriority() { return TaskPriority.PR_MEDIUM; }
	
	void runImpl()
	{
		if (Config.DEBUG) {
			_log.fine("user:" + _loginName);
			_log.fine("key:" + Long.toHexString(_key1) + " " + Long.toHexString(_key2));
		}
		
		ClientThread client = getClient();
		client.setLoginName(_loginName);
		LoginServerThread.getInstance().addGameServerLogin(_loginName,getConnection());
		
		LoginServerThread.getInstance().addWaitingClientAndSendRequest(_loginName,client,(int)_key2);
	}

    
	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.clientpackets.ClientBasePacket#getType()
	 */
	public String getType()
	{
		return _C__08_AUTHLOGIN;
	}
}
