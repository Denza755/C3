/*
 * $Header: GameServerListener.java, 14-Jul-2005 03:26:20 luisantonioa Exp $
 *
 * $Author: luisantonioa $
 * $Date: 14-Jul-2005 03:26:20 $
 * $Revision: 1 $
 * $Log: GameServerListener.java,v $
 * Revision 1  14-Jul-2005 03:26:20  luisantonioa
 * Added copyright notice
 *
 * 
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

import javolution.util.FastList;
import javolution.util.FastMap;
import javolution.xml.pull.XmlPullParserException;
import javolution.xml.pull.XmlPullParserImpl;
import net.sf.l2j.Config;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.loginserver.gameserverpackets.ServerStatus;
import net.sf.l2j.loginserver.serverpackets.ServerList;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Logger;

public class GameServerTable
{
	protected static Logger _log = Logger.getLogger(GameServerTable.class.getName());
	private static GameServerTable _instance;
	private List<GameServer> _gameServerList;
	public Map<Integer, String> serverNames;
	
	public static GameServerTable getInstance()
	{
		if(_instance == null)
		{
			_instance = new GameServerTable();
		}
		return _instance;
	}
	
	public class GameServer
	{
		public String ip;
		public int server_id;
		public int port;
		public boolean pvp = true;
		public boolean testServer = false;
		public int maxPlayers;
		public byte[] hexID;
		public GameServerThread gst;
		public boolean brackets = false;
		public boolean clock = false;
		public int status = ServerStatus.STATUS_AUTO;
		public String internal_ip;
		
		GameServer(GameServerThread gamest)
		{
			gst = gamest;
			ip = gst.getGameExternalIP();
			port = gst.getPort();
			pvp = gst.getPvP();
			testServer = gst.isTestServer();
			maxPlayers = gst.getMaxPlayers();
			hexID = gst.getHexID();
			server_id = gst.getServerID();
			internal_ip = gst.getGameInternalIP();
		}
		
		public String toString()
		{
			return "GameServer: "+serverNames.get(server_id)+" id:"+server_id+" hex:"+hexToString(hexID)+" ip:"+ip+":"+port+" status: "+ServerStatus.statusString[status];
		}
		
		private String hexToString(byte[] hex)
		{
			if(hex == null)
				return "null";
			return new BigInteger(hex).toString(16);
		}
		
		public GameServer(byte[] hex, int id)
		{
			hexID = hex;
			server_id = id;
		}
		
		public GameServer(int id)
		{
			server_id = id;
		}
	}
	
	public void shutDown()
	{
		for(GameServer gs :_gameServerList)
		{
			if(gs.gst != null)
				gs.gst.interrupt();
		}
	}
	
	public void load()
	{
		_gameServerList = new FastList<GameServer>();
		serverNames =  new FastMap<Integer, String>();
		loadServerNames();
		java.sql.Connection con = null;
		PreparedStatement statement = null;
		int id = 0;
		int number = 0;
		int previousID = 0;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT * FROM gameservers");
			ResultSet rset = statement.executeQuery();
			while(rset.next())
			{
				id = rset.getInt("server_id");
				for(int i = 1;id-i > previousID; i++) //fill with dummy servers to keep
				{
					GameServer gs = new GameServer(previousID+i);
					_gameServerList.add(gs);
				}
				GameServer gs =  new GameServer(stringToHex(rset.getString("hexid")),id);
				_gameServerList.add(gs);
				previousID = id;
				number++;
			}
			_log.info("GameServerTable: Loaded "+number+" servers (max id:"+id+")");
		}
		catch (SQLException e)
		{
			_log.warning("Error while loading Server List:");
			e.printStackTrace();
		}
		finally
		{
			try { con.close();} catch (Exception e2) {}
        	try { statement.close();} catch (Exception e2) {}
		}
		if (Config.DEBUG)
		{
			for(GameServer gs : _gameServerList)
			{
				_log.info(gs.toString());
			}
		}
	}
	
	/**
	 * 
	 */
	private void loadServerNames()
	{
        InputStream in = null;
		try
		{
			in = new FileInputStream("servername.xml");
			XmlPullParserImpl xpp = new XmlPullParserImpl();
			xpp.setInput(in);
			for (int e = xpp.getEventType(); e != XmlPullParserImpl.END_DOCUMENT; e = xpp.next())
			{
				if (e == XmlPullParserImpl.START_TAG)
				{
					if(xpp.getName().toString().equals("server"))
					{
						Integer id = new Integer(xpp.getAttributeValue(null,"id").toString());
						String name = xpp.getAttributeValue(null,"name").toString();
						serverNames.put(id,name);
					}
				}
			}
			_log.info("Loaded "+serverNames.size()+" server names");
		}
		catch (FileNotFoundException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (IOException ioe)
		{
			ioe.printStackTrace();
		}
		catch (XmlPullParserException xppe)
		{
			xppe.printStackTrace();
		}
        finally
        {
            try { in.close(); } catch (Exception e) {}
        }
	}

	/**
	 * @param string
	 * @return
	 */
	private byte[] stringToHex(String string)
	{
		return new BigInteger(string, 16).toByteArray();
	}
	
	
	private String hexToString(byte[] hex)
	{
		if(hex == null)
			return "null";
		return new BigInteger(hex).toString(16);
	}

	public void setServerReallyDown(int id)
	{
		for(GameServer gs : _gameServerList)
		{
			if(gs.server_id == id)
			{
				gs.ip = null;
				gs.internal_ip = null;
				gs.port = 0;
				gs.gst = null;
			}
		}
	}
	
	public GameServerThread getGameServerThread(int ServerID)
	{
		for(GameServer gs : _gameServerList)
		{
			if(gs.server_id == ServerID)
			{
				return gs.gst;
			}
		}
		return null;
	}
	
	public int getGameServerStatus(int ServerID)
	{
		for(GameServer gs : _gameServerList)
		{
			if(gs.server_id == ServerID)
			{
				return gs.status;
			}
		}
		return -1;
	}
	
	public void addServer(GameServerThread gst)
	{
		GameServer gameServer = new GameServer(gst);
		GameServer toReplace = null;
		/*
		gst.setAuthed(true);
		_gameServerList.add(gameServer);*/
		
		for(GameServer gs : _gameServerList)
		{
			if(gs.server_id == gst.getServerID())
			{
				toReplace = gs;
			}
		}
		if(toReplace != null)
		{
			_gameServerList.remove(toReplace);
		}
		_gameServerList.add(gameServer);
		orderList();
		if (Config.DEBUG)
		{
			for(GameServer gs : _gameServerList)
			{
				_log.info(gs.toString());
			}
		}
		gst.setAuthed(true);
	}
	
	public int getServerIDforHex(byte[] hex)
	{
		for(GameServer gs : _gameServerList)
		{
			if(areEqualByteArrays(hex,gs.hexID))
				return gs.server_id;
		}
		return -1;
	}
	
	//FIXME: isn't there a simpler way in java??
	private boolean areEqualByteArrays(byte[] b1, byte[] b2)
	{
		if(b1 == null || b2 == null)
			return false;
		int b1len = b1.length;
		int b2len = b2.length;
		if(b1len != b2len)
			return false;
		for(int i = 0; i < b1len; i++)
		{
			if(b1[i] != b2[i])
				return false;
		}
		return true;
	}
	
	public boolean isIDfree(int id)
	{
		for(GameServer gs : _gameServerList)
		{
			if(gs.server_id == id && gs.hexID != null)
				return false;
		}
		return true;
	}
	
	public void createServer(GameServer gs)
	{
		java.sql.Connection con = null;
		PreparedStatement statement = null;
        try
        {
            con = L2DatabaseFactory.getInstance().getConnection();
            statement = con.prepareStatement(
                    "INSERT INTO gameservers " +
                    "(hexid,server_id,host)" +
                    "values (?,?,?)");
            statement.setString(1, hexToString(gs.hexID));
            statement.setInt(2, gs.server_id);
            if(gs.gst != null)
            {
            	statement.setString(3, gs.gst.getGameHost());
            }
            else
            {
            	statement.setString(3, "*");
            }
			statement.executeUpdate();
            statement.close();
        }
        catch(SQLException e)
        {
        	_log.warning("SQL error while saving gameserver :"+e);
        }
        finally
        {
        	try { con.close();} catch (Exception e) {}
        	try { statement.close();} catch (Exception e) {}
        }
	}
	
	public boolean isARegisteredServer(byte [] hex)
	{
		for(GameServer gs : _gameServerList)
		{
			if(areEqualByteArrays(hex,gs.hexID))
				return true;
		}
		return false;
	}
	
	public int findFreeID()
	{
		for(int i = 0; i < 127; i++)
		{
			if(isIDfree(i))
				return i;
		}
		return -1;
	}
	
	public void deleteServer(int id)
	{
		java.sql.Connection con = null;
		PreparedStatement statement = null;
        try
        {
            con = L2DatabaseFactory.getInstance().getConnection();
            statement = con.prepareStatement(
                    "DELETE FROM gameservers WHERE gameservers.server_id=?");
            statement.setInt(1, id);
			statement.executeUpdate();
            statement.close();
        }
        catch(SQLException e)
        {
        	_log.warning("SQL error while deleting gameserver :"+e);
        }
        finally
        {
        	try { con.close();} catch (Exception e) {}
        	try { statement.close();} catch (Exception e) {}
        }
	}

	public GameServerTable()
	{
		_gameServerList = new FastList<GameServer>();
		load();
	}
	
	public ServerList makeServerList(boolean isGM, boolean _internalip)
	{
		orderList();
		ServerList sl = new ServerList();
		for(GameServer gs : _gameServerList)
		{
			if(_internalip)
			{
				int status = gs.status;
				if(status == ServerStatus.STATUS_AUTO)
				{
					if(gs.internal_ip == null)
					{
						status = ServerStatus.STATUS_DOWN;
					}
				}
				else if(status == ServerStatus.STATUS_GM_ONLY)
				{
					if(!isGM)
					{
						status = ServerStatus.STATUS_DOWN;
					}
					else
					{
						if(gs.internal_ip == null)
						{
							status = ServerStatus.STATUS_DOWN;
						}
					}
				}
				sl.addServer(gs.internal_ip,gs.port,gs.pvp,gs.testServer,(gs.gst == null ? 0 : gs.gst.getCurrentPlayers()),gs.maxPlayers,gs.brackets,gs.clock,status,gs.server_id);
			}
			else
			{
				int status = gs.status;
				if(status == ServerStatus.STATUS_AUTO)
				{
					if(gs.ip == null)
					{
						status = ServerStatus.STATUS_DOWN;
					}
				}
				else if(status == ServerStatus.STATUS_GM_ONLY)
				{
					if(!isGM)
					{
						status = ServerStatus.STATUS_DOWN;
					}
					else
					{
						if(gs.ip == null)
						{
							status = ServerStatus.STATUS_DOWN;
						}
					}
				}
				sl.addServer(gs.ip,gs.port,gs.pvp,gs.testServer,(gs.gst == null ? 0 : gs.gst.getCurrentPlayers()),gs.maxPlayers,gs.brackets,gs.clock,status,gs.server_id);
			}
		}
		return sl;
		
	}
	
	/**
	 * 
	 */
	private void orderList()
	{
		Collections.sort(_gameServerList, gsComparator);
	}
	
	private static final Comparator<GameServer> gsComparator = new Comparator<GameServer>()
    {
        public int compare(GameServer gs1, GameServer gs2)
        {
            return (gs1.server_id < gs2.server_id ? -1 : gs1.server_id  == gs2.server_id ? 0 : 1);
        }
    };
	
	/**
	 * @param thread
	 */
	public void createServer(GameServerThread thread)
	{
		java.sql.Connection con = null;
		PreparedStatement statement = null;
        try
        {
            con = L2DatabaseFactory.getInstance().getConnection();
            statement = con.prepareStatement(
                    "INSERT INTO gameservers " +
                    "(hexid,server_id,host)" +
                    "values (?,?,?)");
            statement.setString(1, hexToString(thread.getHexID()));
            statement.setInt(2, thread.getServerID());
            statement.setString(3, thread.getGameHost());
			statement.executeUpdate();
            statement.close();
        }
        catch(SQLException e)
        {
        	_log.warning("SQL error while saving gameserver :"+e);
        }
        finally
        {
        	try { con.close();} catch (Exception e) {}
        	try { statement.close();} catch (Exception e) {}
        }
	}

	/**
	 * @param value
	 * @param serverID
	 */
	public void setMaxPlayers(int value, int serverID)
	{
		for(GameServer gs : _gameServerList)
		{
			if(gs.server_id == serverID)
			{
				gs.maxPlayers = value;
				gs.gst.setMaxPlayers(value);
			}
		}
	}

	/**
	 * @param b
	 * @param serverID
	 */
	public void setBracket(boolean b, int serverID)
	{
		for(GameServer gs : _gameServerList)
		{
			if(gs.server_id == serverID)
			{
				gs.brackets = b;
			}
		}
	}

	/**
	 * @param b
	 * @param serverID
	 */
	public void setClock(boolean b, int serverID)
	{
		for(GameServer gs : _gameServerList)
		{
			if(gs.server_id == serverID)
			{
				gs.clock  = b;
			}
		}
	}
	
	/**
	 * @param b
	 * @param serverID
	 */
	public void setTestServer(boolean b, int serverID)
	{
		for(GameServer gs : _gameServerList)
		{
			if(gs.server_id == serverID)
			{
				gs.testServer = b;
			}
		}
	}

	/**
	 * @param value
	 * @param serverID
	 */
	public void setStatus(int value, int serverID)
	{
		for(GameServer gs : _gameServerList)
		{
			if(gs.server_id == serverID)
			{
				gs.status   = value;
				if (Config.DEBUG)_log.info("Status Changed for server "+serverID);
			}
		}
	}
	
	public boolean isServerAuthed(int serverID)
	{
		for(GameServer gs : _gameServerList)
		{
			if(gs.server_id == serverID)
			{
				if(gs.ip != null && gs.gst != null && gs.gst.isAuthed())
				{
					return true;
				}
			}
		}
		return false;
	}
	
	public List<String> status()
	{
		List<String> str = new ArrayList<String>();
		str.add("There are "+_gameServerList.size()+" GameServers");
		for(GameServer gs : _gameServerList)
		{
			str.add(gs.toString());
		}
		return str;
	}
}