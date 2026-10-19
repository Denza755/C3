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

import javolution.util.FastMap;
import net.sf.l2j.L2DatabaseFactory;

import java.net.Socket;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * This class ...
 * 
 * @version $Revision: 1.7.4.3 $ $Date: 2005/03/27 15:30:09 $
 */
public class LoginController
{
	protected static Logger _log = Logger.getLogger(LoginController.class.getName());
	
	private static LoginController _instance;
	
	/**
	 * this map contains the session ids that belong to one account
	 */
	private Map<String, Integer> _logins;
	
	/** this map contains the connections of the players that are in the loginserver*/
	private Map<String, Socket> _accountsInLoginServer;
	private int _maxAllowedOnlinePlayers;

	private LoginController()
	{
		_logins = new FastMap<String, Integer>();
		_accountsInLoginServer = new FastMap<String, Socket>();
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
	
	public void removeGameServerLogin(String account)
	{
		if (account != null)
		{
			_logins.remove(account);
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

	public boolean isAccountInAnyGameServer(String account)
	{
		List<GameServerThread> gslist = GameServerListener.getInstance().getGameServerThreads();
		synchronized (gslist)
		{
			for(GameServerThread gs : gslist)
			{
				if(gs.isPlayerInGameServer(account))
					return true;
			}
		}
		return false;
	}
	
	public int getGameServerIDforAccount(String account)
	{
		List<GameServerThread> gslist = GameServerListener.getInstance().getGameServerThreads();
		synchronized (gslist)
		{
			for(GameServerThread gs : gslist)
			{
				if(gs.isPlayerInGameServer(account))
					return gs.getServerID();
			}
		}
		return -1;
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
	
	public int getTotalOnlinePlayerCount()
	{
		int playerCount = 0;
		List<GameServerThread> gslist = GameServerListener.getInstance().getGameServerThreads();
		synchronized (gslist)
		{
			for(GameServerThread gs : gslist)
			{
				playerCount += gs.getCurrentPlayers();
			}
		}
		return playerCount;
	}
	
	public int getOnlinePlayerCount(int ServerID)
	{
		List<GameServerThread> gslist = GameServerListener.getInstance().getGameServerThreads();
		synchronized (gslist)
		{
			for(GameServerThread gs : gslist)
			{
				if(gs.getServerID() == ServerID)
					return gs.getCurrentPlayers();
			}
		}
		return 0;
	}
	
	public int getMaxAllowedOnlinePlayers(int ServerID)
	{
		List<GameServerThread> gslist = GameServerListener.getInstance().getGameServerThreads();
		synchronized (gslist)
		{
			for(GameServerThread gs : gslist)
			{
				if(gs.getServerID() == ServerID)
					return gs.getMaxPlayers();
			}
		}
		return 0;
	}

	public void setMaxAllowedOnlinePlayers(int maxAllowedOnlinePlayers)
	{
		_maxAllowedOnlinePlayers = maxAllowedOnlinePlayers;
	}

	/**
	 * @return
	 */
	public boolean loginPossible(int access, int ServerID)
	{
		return ((getOnlinePlayerCount(ServerID) < _maxAllowedOnlinePlayers) || (access >= 50));
	}
	
	public Socket getLoginServerConnection(String loginName)
	{
		return _accountsInLoginServer.get(loginName);
	}
	
	public void setAccountAccessLevel(String user, int banLevel)
    {
        java.sql.Connection con = null;
        PreparedStatement statement = null;
        try
        {           
            con = L2DatabaseFactory.getInstance().getConnection();
            
            String stmt = "UPDATE accounts, characters SET accounts.access_level = ? WHERE characters.account_name = accounts.login AND characters.char_name=?";
            statement = con.prepareStatement(stmt);
            statement.setInt(1, banLevel);
            statement.setString(2, user);
            statement.executeUpdate();
            statement.close();
        }
        catch (Exception e)
        {
            _log.warning("Could not set accessLevl:"+e);
        } 
        finally 
        {
            try { con.close(); } catch (Exception e) {}
            try { statement.close();} catch (Exception e) {}
        }
    }
	
	public boolean isGM(String user)
    {
    	boolean ok = false;
    	java.sql.Connection con = null;
    	PreparedStatement statement = null;
        try
        {           
            con = L2DatabaseFactory.getInstance().getConnection();
            statement = con.prepareStatement("SELECT access_level FROM accounts WHERE login=?");
            statement.setString(1, user);
            ResultSet rset = statement.executeQuery();
            if (rset.next()) 
            {
                int accessLevel = rset.getInt(1);
                if (accessLevel >= 100)
                {
                    ok = true;
                }
            }
            rset.close();
            statement.close();
        }
        catch (Exception e)
        {
            _log.warning("could not check gm state:"+e);
            ok = false;
        } 
        finally 
        {
            try { con.close(); } catch (Exception e) {}
            try { statement.close();} catch (Exception e) {}
        }
        return ok;
    }
}
