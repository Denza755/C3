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
package net.sf.l2j.gameserver;

import java.net.Socket;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.concurrent.ScheduledFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.l2j.Config;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.model.CharSelectInfoPackage;
import net.sf.l2j.gameserver.model.L2MerchantInstance;
import net.sf.l2j.gameserver.model.L2PcInstance;
import net.sf.l2j.gameserver.model.L2RecipeList;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.loginserver.LoginController;

/**
 * This class ...
 * 
 * @version $Revision: 1.21.2.19.2.12 $ $Date: 2005/04/04 19:47:01 $
 */
public final class ClientThread
{
	protected static Logger _log = Logger.getLogger(ClientThread.class.getName());
	
	public static int devCharId;
	
	class AutoSaveTask implements Runnable
	{
		public void run() {
			try
			{
				L2PcInstance player = ClientThread.this.getActiveChar();
				if (player != null) {
					saveCharToDisk(player);
				}
				else if (getConnection() == null
						|| !getConnection().getChannel().isOpen())
				{
					_autoSaveInDB.cancel(false);
				}
			} catch (Throwable e) {
				_log.severe(e.toString());
			}
		}
	}
	
	private String _loginName;
	private L2PcInstance _activeChar;
	private int _sessionId;
	private final Connection _connection;
	final ScheduledFuture _autoSaveInDB;
	
	//private byte[] _filter;
	
	private final byte[] _cryptkey = {
			(byte)0x94, (byte)0x35, (byte)0x00, (byte)0x00, 
			(byte)0xa1, (byte)0x6c,	(byte)0x54, (byte)0x87   // these 4 bytes are fixed
	};
	
    private int revision = 0;
    
    private ArrayList<Integer> _charSlotMapping = new ArrayList<Integer>();
    
    public static ClientThread create(Socket socket)
    {
    	final ClientThread client = new ClientThread(socket);
    	return client;
    }
	
	public ClientThread(Socket socket)
	{
		_sessionId = 0x12345678;
		_connection = new Connection(this, socket, _cryptkey);
   		_autoSaveInDB = ClientScheduler.getInstance().scheduleLowAtFixedRate(
   				new AutoSaveTask(), 5*60*1000L, 15*60*1000L
   				);
	}
    
	void onDisconnect()
	{
		try
		{
			_autoSaveInDB.cancel(false);
			if (_activeChar != null)  // this should only happen on connection loss
			{
				L2PcInstance player = _activeChar;
				_activeChar = null;
				
				// notify the world about our disconnect
				player.deleteMe();
				
				try {
					saveCharToDisk(player);
				} catch (Exception e2) { /* ignore any problems here */ }
			}
			
			_connection.close();
		}
		catch (Exception e1)
		{
			_log.log(Level.WARNING, "error while disconnecting client", e1);
		}
		finally
		{
			// remove the account
			LoginController.getInstance().removeGameServerLogin(getLoginName());
		}
	}
	
	/**
	 * 
	 */
	public static void saveCharToDisk(L2PcInstance cha)
	{  
		cha.store();
		storeRecipeBook(cha);
	}
	
	public void deleteChar(int charslot) throws Exception
	{	
		//have to make sure active character must be nulled
		if (getActiveChar() != null)
		{
			saveCharToDisk (getActiveChar());
			if (Config.DEBUG) _log.fine("active Char saved");
			_activeChar = null;
		}
        
		int objid = getObjectIdForSlot(charslot);

		java.sql.Connection con = null;
		try 
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("DELETE FROM characters WHERE obj_Id=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();
	
			statement = con.prepareStatement("DELETE FROM character_shortcuts WHERE char_obj_id=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();
	
			statement = con.prepareStatement("DELETE FROM character_macroses WHERE char_obj_id=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();
	
			statement = con.prepareStatement("DELETE FROM items WHERE owner_id=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();
	
			statement = con.prepareStatement("DELETE FROM character_skills WHERE char_obj_Id=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();

			statement = con.prepareStatement("DELETE FROM character_quests WHERE char_id=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();

			statement = con.prepareStatement("DELETE FROM character_recipebook WHERE char_id=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();

			statement = con.prepareStatement("DELETE FROM merchant_lease WHERE player_id=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "data error on delete char:", e);
		} 
		finally 
		{
			try { con.close(); } catch (Exception e) {}
		}
	}
	
	public L2PcInstance loadCharFromDisk(int charslot)
	{
		L2PcInstance character = L2PcInstance.load(getObjectIdForSlot(charslot));
		
		if (Config.DEVELOPER)
		{
			if ("test".equals(getLoginName()))
				devCharId = character.getObjectId();
		}
        if (character != null)
		{
			//restoreInventory(character);
			//restoreSkills(character);
            //character.restoreSkills();
			//restoreShortCuts(character);
			//restoreWarehouse(character);
			restoreRecipeBook(character);
			L2MerchantInstance.playerEnter(character);

			// preinit some values for each login
			character.setRunning();	// running is default
			character.standUp();		// standing is default
			
            character.updateStats();
			character.updateKarma(1);
            character.setOnlineStatus(true);
		}
		else
		{
			_log.warning("could not restore in slot:"+ charslot);
		}
		
		//setCharacter(character);
		return character;
	}
	
	
	/**
	 * @param cha
	 * @param saveFile
	 */
	private static void storeRecipeBook(L2PcInstance cha)
	{
		java.sql.Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("DELETE FROM character_recipebook WHERE char_id=?");
			statement.setInt(1, cha.getObjectId());
			statement.execute();
			statement.close();

			L2RecipeList[] recipes = cha.getRecipeBook();
			for (int count=0; count < recipes.length; count++)
			{
				statement = con.prepareStatement("INSERT INTO character_recipebook (char_id, id) values(?,?)");
				statement.setInt(1, cha.getObjectId());
				statement.setInt(2, recipes[count].getId());
				statement.execute();
				statement.close();
			}
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "could not store skills:", e);
		} 
		finally 
		{
			try { con.close(); } catch (Exception e) {}
		}
	}
	
	/**
	 * @param cha
	 * @param saveFile
	 */
	private static void restoreRecipeBook(L2PcInstance cha)
	{
		java.sql.Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("SELECT id FROM character_recipebook WHERE char_id=?");
			statement.setInt(1, cha.getObjectId());
			ResultSet rset = statement.executeQuery();
			
			while (rset.next())
 			{
				int id = rset.getInt(1);
 				L2RecipeList recipe = RecipeController.getInstance().getRecipeList(id-1);
 				cha.registerRecipeList(recipe);
 			}
			
			rset.close();
			statement.close();
		}
		catch (Exception e)
		{
			_log.warning("count not restore skills:"+e);
		} 
		finally 
		{
			try { con.close(); } catch (Exception e) {}
		}
	}

    /**
     * @param charslot
     * @return
     */
    private int getObjectIdForSlot(int charslot)
    {
        Integer objectId = _charSlotMapping.get(charslot);
        return objectId.intValue();
    }
	
	/**
	 * @return
	 */
	public Connection getConnection()
	{
		return _connection;
	}
	
	/**
	 * @return
	 */
	public L2PcInstance getActiveChar()
	{
		return _activeChar;
	}
	/**
	 * @return Returns the sessionId.
	 */
	public int getSessionId()
	{
		return _sessionId;
	}
	
	public String getLoginName()
	{
		return _loginName;
	}
	
	public void setLoginName(String loginName)
	{
		_loginName = loginName;
	}
	
	
	
	/**
	 * @param cha
	 */
	public void setActiveChar(L2PcInstance cha)
	{
		_activeChar = cha;
		if (cha != null)
		{
			// we store the connection in the player object so that external
			// events can directly send events to the players client
			// might be changed later to use a central event management and distribution system
			_activeChar.setNetConnection(_connection);
			
			// update world data
			L2World.getInstance().storeObject(_activeChar);
		}
	}

	/**
	 * @param sessionKey
	 */
	public void setSessionId(int sessionKey)
	{
		_sessionId = sessionKey;
	}
    
    /**
     * @param chars
     */
    public void setCharSelection(CharSelectInfoPackage[] chars)
    {
        _charSlotMapping.clear();
        
        for (int i = 0; i < chars.length; i++)
        {
            int objectId = chars[i].getObjectId();
            _charSlotMapping.add(new Integer(objectId));
        }
    }
    /**
     * @return Returns the revision.
     */
    public int getRevision()
    {
        return revision;
    }
    /**
     * @param revision The revision to set.
     */
    public void setRevision(int revision)
    {
        this.revision = revision;
    }
}
