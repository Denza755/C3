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
package net.sf.l2j.loginserver;

import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import net.sf.l2j.gameserver.Connection;

/**
 * This class ...
 * 
 * @version $Revision: 1.7.4.3 $ $Date: 2005/03/27 15:30:09 $
 */
public class LoginController
{
	private static LoginController _instance;
	/**
	 * this map contains the session ids that belong to one account
	 */
	private Map<String, Integer> _logins;
	
	private HashMap<String, Socket> _accountsInLoginServer;
	private HashMap<String, Connection> _accountsInGameServer;
	private int _maxAllowedOnlinePlayers;
	
	private LoginController()
	{
		_logins = new HashMap<String, Integer>();
		_accountsInGameServer = new HashMap<String, Connection>();
		_accountsInLoginServer = new HashMap<String, Socket>();
	}
	
	public static LoginController getInstance()
	{
		if (_instance == null)
		{
			_instance = new LoginController();
		}

		return _instance;
	}

	public int assignSessionKeyToLogin(String account, Socket _csocket)
	{
		int key = -1;
		
		 key = (int) System.currentTimeMillis() & 0xffffff;
		 _logins.put(account, new Integer(key));
		 _accountsInLoginServer.put(account, _csocket);
		return key;
	}

	public void addGameServerLogin(String account, Connection connection)
	{
		_accountsInGameServer.put(account, connection);
	}
	
	public void removeGameServerLogin(String account)
	{
		if (account != null)
		{
			_logins.remove(account);
			_accountsInGameServer.remove(account);
		}
	}

	public void removeLoginServerLogin(String account)
	{
		if (account != null)
		{
			_accountsInLoginServer.remove(account);
		}
	}
	
	public boolean isAccountInLoginServer(String account)
	{
		return _accountsInLoginServer.containsKey(account);
	}

	public boolean isAccountInGameServer(String account)
	{
		return _accountsInGameServer.containsKey(account);
	}

	public int getKeyForAccount(String account)
	{
		int key = 0;
		
		Integer result = _logins.get(account);
		if (result != null)
		{
			key = result.intValue();
		}
		
		return key;
	}
	
	public int getOnlinePlayerCount()
	{
		return _accountsInGameServer.size();
	}
	
	public int getMaxAllowedOnlinePlayers()
	{
		return _maxAllowedOnlinePlayers;
	}

	public void setMaxAllowedOnlinePlayers(int maxAllowedOnlinePlayers)
	{
		_maxAllowedOnlinePlayers = maxAllowedOnlinePlayers;
	}

	/**
	 * @return
	 */
	public boolean loginPossible(int access)
	{
		return ((_accountsInGameServer.size() < _maxAllowedOnlinePlayers) || (access >= 50));
	}
	
	public Connection getClientConnection(String loginName) {
		return _accountsInGameServer.get(loginName);
	}
	
	public Socket getLoginServerConnection(String loginName) {
		return _accountsInLoginServer.get(loginName);
	}
}
