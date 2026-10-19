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
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.RSAKeyGenParameterSpec;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.logging.Logger;

import net.sf.l2j.Base64;
import net.sf.l2j.Config;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.lib.Log;

/**
 * This class ...
 * 
 * @version $Revision: 1.11.2.4.2.5 $ $Date: 2005/03/27 15:30:09 $
 */
public class Logins 
{
	private static final String SHA = "SHA";
	private static Logger _log = Logger.getLogger(Logins.class.getName());
			
	private HashMap<String, byte[]> _logPass;
	private HashMap<String, Integer> _hackProtection;
	private HashMap<String, String> _lastPassword;
	private boolean _autoCreate;
    
    private RSAPublicKey _publicKey;
    private RSAPrivateKey _privateKey;
    private byte[] _scrambledModulus;

	public Logins(boolean autoCreate)
	{
		_logPass = new HashMap<String, byte[]>();
		_hackProtection = new HashMap<String, Integer>();
		_lastPassword = new HashMap<String, String>();
		_autoCreate = autoCreate;
		_log.config("Automatically creating new accounts: " + _autoCreate);

        try
        {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            RSAKeyGenParameterSpec spec = new RSAKeyGenParameterSpec(1024,RSAKeyGenParameterSpec.F4);
            keyGen.initialize(spec);
            KeyPair pair = keyGen.generateKeyPair();
            _log.config("new RSA keypair created");
            _privateKey = (RSAPrivateKey) pair.getPrivate();
            _publicKey = (RSAPublicKey) pair.getPublic();
            
            _scrambledModulus = _publicKey.getModulus().toByteArray();
            if (_scrambledModulus.length == 0x81 && _scrambledModulus[0] == 0x00)
            {
                byte[] tmp = new byte[0x80];
                for (int i=1; i<_scrambledModulus.length; i++)
                {
                    tmp[i-1]    = _scrambledModulus[i];
                }
                _scrambledModulus = tmp;
            }
            //System.out.println("UnScrambled modulo.");
            //System.out.println(printData(_scrambledModulus, _scrambledModulus.length));
            //System.out.println(_publicKey.getModulus());
            // step 1 0x4d-0x50  <-> 0x00-0x04
            for (int i=0;i<4;i++)
            {
                byte temp = _scrambledModulus[0x00+i];
                _scrambledModulus[0x00+i] = _scrambledModulus[0x4d+i];
                _scrambledModulus[0x4d+i] = temp;
            }
            
            // step 2   xor  first 0x40 bytes with  last 0x40 bytes
            for (int i=0;i<0x40;i++)
            {
                _scrambledModulus[i] = (byte)(_scrambledModulus[i] ^ _scrambledModulus[0x40+i]);
            }
            
            // step 3  xor  bytes 0x0d-0x10 with bytes 0x34-0x38
            for (int i=0;i<4;i++)
            {
                _scrambledModulus[0x0d+i] = (byte)(_scrambledModulus[0x0d+i] ^ _scrambledModulus[0x34+i]);
            }
            
            // step 4   xor  last 0x40 bytes with  first 0x40 bytes            for (int i=0;i<0x40;i++)
            for (int i=0;i<0x40;i++)
            {
                _scrambledModulus[0x40+i] = (byte)(_scrambledModulus[0x40+i] ^ _scrambledModulus[i]);
            }
            _log.config("Scrambled modulo value stored.");
        }
        catch (GeneralSecurityException e)
        {
            _log.severe("Error in RSA setup:" + e);
        }       
	}

	/**
	 * user name is not case sensitive any more
	 * @param user
	 * @param password
     * @param address
	 * @return
	 */
	public boolean loginValid(String user, String password, InetAddress address) throws HackingException
	{
		boolean ok = false;
				
		
		Integer failedConnects  = _hackProtection.get(address.getHostAddress());
		String lastPassword = _lastPassword.get(address.getHostAddress());
		
		Log.add("'"+user+"' "+address.getHostAddress(), "logins_ip");
		
		if (failedConnects != null && failedConnects > Config.LOGIN_TRY_BEFORE_BAN)
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
			// int busy = L2DatabaseFactory.getInstance().getBusyConnectionCount();
			// int idle = L2DatabaseFactory.getInstance().getIdleConnectionCount();
			//_log.info("DB connections busy:"+busy+" idle:"+idle);
			
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
			        if ((user.length() >= 2) && (user.length() <= 14))
			        {
			            statement = con.prepareStatement("INSERT INTO accounts (login,password,lastactive,access_level,lastIP,comments) values(?,?,?,?,?,'')");
			            statement.setString(1, user);
			            statement.setString(2, Base64.encodeBytes(hash));
			            statement.setLong(3, System.currentTimeMillis());
			            statement.setInt(4, 0);
			            statement.setString(5, address.getHostAddress());
			            statement.execute();
			            statement.close();
			            
			            _logPass.put(user, hash);
			            _log.info("created new account for "+ user);
			            
			            return true;
			            
			        }
			        _log.warning("Invalid username creation/use attempt: "+user);
			        return false;
			    }
			    _log.warning("account missing for user "+user);
			    return false;
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
			    statement=con.prepareStatement("UPDATE accounts SET lastactive=?, lastIP=? WHERE login=?");
			    statement.setLong(1,System.currentTimeMillis());
			    statement.setString(2,address.getHostAddress());
			    statement.setString(3,user);
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
			Log.add("'"+user+"' "+address.getHostAddress(), "logins_ip_fails");

			// add 1 to the failed counter for this IP 
			int failedCount = 1;
			if (failedConnects != null)
			{
				failedCount = failedConnects.intValue() + 1;
			}
			
			if(password != lastPassword)
			{
				_hackProtection.put(address.getHostAddress(), new Integer(failedCount));
				_lastPassword.put(address.getHostAddress(), password);
		 	}
		}
		else
		{
			// for long running servers, this should prevent blocking 
			// of users that mistype their passwords once every day :)
			_hackProtection.remove(address.getHostAddress());
			_lastPassword.remove(address.getHostAddress());
			Log.add("'"+user+"' "+address.getHostAddress(), "logins_ip");
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
    
    public boolean ipBlocked(String ipAddress)
    {
        int tries = 0;
        
        if (_hackProtection.containsKey(ipAddress))
            tries = _hackProtection.get(ipAddress);
        
        if (tries > Config.LOGIN_TRY_BEFORE_BAN)
        {
            _hackProtection.remove(ipAddress);
            _log.warning("Removed host from hacklist! IP number: " + ipAddress);
            return true;
        }
        
        return false;
    }
    

    /**
     * @return
     */
    public byte[] getScrambledModulus()
    {
        return _scrambledModulus;
    }

    /**
     * @return
     */
    public RSAPrivateKey getPrivateKey()
    {
        return _privateKey;
    }
    
}
