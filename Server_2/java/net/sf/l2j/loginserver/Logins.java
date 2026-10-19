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

import java.net.InetAddress;
import java.security.MessageDigest;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.logging.Logger;

import net.sf.l2j.Base64;
import net.sf.l2j.Config;
import net.sf.l2j.L2DatabaseFactory;

/**
 * This class ...
 * 
 * @version $Revision: 1.11.2.4.2.5 $ $Date: 2005/03/27 15:30:09 $
 */
public class Logins 
{
	private static final String SHA = "SHA";
	private static final String UTF_8 = "UTF-8";
	private static Logger _log = Logger.getLogger(Logins.class.getName());
			
	private HashMap<String, byte[]> _logPass;
	private HashMap<String, Integer> _hackProtection;
	private boolean _autoCreate;

	public Logins(boolean autoCreate)
	{
		_logPass = new HashMap<String, byte[]>();
		_hackProtection = new HashMap<String, Integer>();
		_autoCreate = autoCreate;
		_log.config("Automatically creating new accounts: " + _autoCreate);
	}

	/**
	 * user name is not case sensitive any more
	 * @param user
	 * @param password
	 * @return
	 */
	public boolean loginValid(String user, String password, InetAddress address) throws HackingException
	{
		boolean ok = false;
				
		
		Integer failedConnects  = _hackProtection.get(address.getHostAddress());
		
		if (failedConnects != null && failedConnects > 10)
		{
			_log.warning("hacking detected from ip:"+address.getHostAddress()+" .. adding IP to banlist");
            failedConnects++;
			throw new HackingException(address.getHostAddress(), failedConnects);
		}
		
		java.sql.Connection con = null;
		try
		{			
			MessageDigest md = MessageDigest.getInstance(SHA);
			byte[] raw = password.getBytes("UTF-8");
			byte[] hash = md.digest(raw);
			
			byte[] expected = null;

			// this is here for temp debugging 
			int busy = L2DatabaseFactory.getInstance().getBusyConnectionCount();
			int idle = L2DatabaseFactory.getInstance().getIdleConnectionCount();
			_log.info("DB connections busy:"+busy+" idle:"+idle);
			
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("SELECT password FROM accounts WHERE login=?");
			statement.setString(1, user);
			ResultSet rset = statement.executeQuery();
			if (rset.next()) 
			{
				expected = Base64.decode(rset.getString(1));
				if (Config.DEBUG) _log.fine("account exists");
			}
			rset.close();
			statement.close();
			
			if (expected == null)
			{
				if (_autoCreate)
				{
					statement = con.prepareStatement("INSERT INTO accounts (login,password,lastactive,access_level) values(?,?,?,?)");
					statement.setString(1, user);
					statement.setString(2, Base64.encodeBytes(hash));
					statement.setLong(3, System.currentTimeMillis());
					statement.setInt(4, 0);
					statement.execute();
					statement.close();
					
					_logPass.put(user, hash);
					_log.info("created new account for "+ user);
					
					return true;
				}
				else
				{
					_log.warning("account missing for user "+user);
					return false;
				}
			}
			
			ok = true;
			for (int i=0;i<expected.length;i++)
			{
				if (hash[i] != expected[i])
				{
					ok = false;
					break;
				}
			}
			if (ok)
				{
					statement=con.prepareStatement("UPDATE accounts SET lastactive=? WHERE login=?");
					statement.setLong(1,System.currentTimeMillis());
					statement.setString(2,user);
					statement.execute();
					statement.close();
				}
		}
		catch (Exception e)
		{
			// digest algo not found ??
			// out of bounds should not be possible
			_log.warning("could not check password:"+e);
			ok = false;
		} 
		finally 
		{
			try { con.close(); } catch (Exception e) {}
		}
		
		if (!ok)
		{
			// add 1 to the failed counter for this IP 
			int failedCount = 1;
			if (failedConnects != null)
			{
				failedCount = failedConnects.intValue() + 1;
			}
			
			_hackProtection.put(address.getHostAddress(), new Integer(failedCount));
		}
		else
		{
			// for long running servers, this should prevent blocking 
			// of users that mistype their passwords once every day :)
			_hackProtection.remove(address.getHostAddress());
		}
		
		return ok;
	}

    public boolean loginBanned(String user)
    {
        boolean ok = false;
                
        java.sql.Connection con = null;
        try
        {           
            con = L2DatabaseFactory.getInstance().getConnection();
            PreparedStatement statement = con.prepareStatement("SELECT access_level FROM accounts WHERE login=?");
            statement.setString(1, user);
            ResultSet rset = statement.executeQuery();
            if (rset.next()) 
            {
                int accessLevel = rset.getInt(1);
                if (accessLevel < 0)
                    ok = true;
            }
            rset.close();
            statement.close();
        }
        catch (Exception e)
        {
            // digest algo not found ??
            // out of bounds should not be possible
            _log.warning("could not check ban state:"+e);
            ok = false;
        } 
        finally 
        {
            try { con.close(); } catch (Exception e) {}
        }
        
        return ok;
    }
}
