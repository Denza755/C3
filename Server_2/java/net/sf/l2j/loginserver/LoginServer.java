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

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.l2j.Config;
import net.sf.l2j.Server;

/**
 * This class ...
 * 
 * @version $Revision: 1.9.4.4 $ $Date: 2005/03/27 15:30:09 $
 */
public class LoginServer extends Thread
{
	private LoginController _loginController;
	private ServerSocket _serverSocket;
	private Logins _logins;
	private String _ip;
	private int _port;
	private int _gamePort;
	private String _externalHostname; 
	private String _internalHostname; 
	private boolean _forwardLocalClient;   
	
	static Logger _log = Logger.getLogger(LoginServer.class.getName());

	public static void main(String[] args) throws IOException
	{
		LoginServer server = new LoginServer();
		_log.config("LoginServer Listening on port 2106");
		server.start();
	}
	
	public void close()
	{
		this.interrupt();
		try
		{
			_serverSocket.close();
		} catch (IOException e) {
			_log.log(Level.INFO, "", e);
		}
	}
	/**
	 * 
	 */
	public void run()
	{
		while (true)
		{
			try
			{
				if (Config.DEBUG) _log.fine("waiting for client connection...");
				Socket connection = _serverSocket.accept();
				if (Config.DEBUG) _log.fine("connection from "+connection.getInetAddress());
				
				
				// use local ip addess if no external hostname is set or if
				// the the connection is requested from localhost (for router compatibility)
				String connectedIp = connection.getInetAddress().getHostAddress(); 
	            if (connectedIp.startsWith("192.168.") || 
						connectedIp.startsWith("10.") || 
                        connectedIp.startsWith("127.0.0.1"))
	            {
	            	if (Config.DEBUG) _log.fine("using internal ip as server ip " + _internalHostname); 
					new ClientThread(connection, _logins, _internalHostname, _gamePort); 
				} 
				else
				{
					if (Config.DEBUG) _log.fine("using external ip as server ip " + _externalHostname);
					new ClientThread(connection, _logins, _externalHostname, _gamePort);
				}
				if (this.isInterrupted())
				{
					// shutdown?
					try { _serverSocket.close(); } catch (IOException io) { _log.log(Level.INFO, "", io); }
					break;
				}
			} 
			catch (IOException e)
			{
				// not a real problem
				if (this.isInterrupted())
				{
					// shutdown?
					try { _serverSocket.close(); } catch (IOException io) { _log.log(Level.INFO, "", io); }
					break;
				}
			}
		}
	}

	public LoginServer() throws IOException
	{
		super("LoginServer");

		Server.loginServer = this;
		
		Properties serverSettings = new Properties();
		InputStream is = getClass().getResourceAsStream("/server.cfg");  
		serverSettings.load(is);
		is.close();
		
		String loginIp = serverSettings.getProperty("LoginserverHostname");
		_externalHostname = serverSettings.getProperty("ExternalHostname");
		if (_externalHostname == null)
		{
			_externalHostname = "localhost";
		}

		_internalHostname = serverSettings.getProperty("InternalHostname");
		if (_internalHostname == null)
		{
			_internalHostname = "localhost";
		}
		String gamePort = serverSettings.getProperty("GameserverPort");
		String createAccounts = serverSettings.getProperty("AutoCreateAccounts");
		
		
		if (!"*".equals(loginIp))
		{
			InetAddress adr = InetAddress.getByName(loginIp);
			_ip = adr.getHostAddress();
			_log.config("LoginServer listening on IP:"+_ip + " Port 2106");
			_serverSocket = new ServerSocket(2106, 50, adr);
		}
		else
		{
			_log.config("LoginServer listening on all available IPs on Port 2106");
			_serverSocket = new ServerSocket(2106);
		}

		_log.config("Hostname for external connections is: "+_externalHostname);
		_log.config("Hostname for internal connections is: "+_internalHostname);
		
		_logins = new Logins( Boolean.valueOf(createAccounts).booleanValue() );
		
		_gamePort = Integer.parseInt(gamePort);
		
		
		try
		{
			InputStream bannedFile = getClass().getResourceAsStream("/banned_ip.cfg");
			if (bannedFile != null)
			{
				int count = 0;
				InputStreamReader reader = new InputStreamReader( bannedFile );
				LineNumberReader lnr = new LineNumberReader(reader);
				String line = null;
				while ( (line = lnr.readLine()) != null)
				{
					line = line.trim();
					if (line.length() > 0)
					{
						count++;
						ClientThread.addBannedIP(line);
					}
				}
				
				_log.info(count + " banned IPs defined");
			}
			else
			{
				_log.info("banned_ip.cfg not found");
			}
			
		}
		catch (Exception e)
		{
			_log.warning("error while reading banned file:"+e);
		}
		
	}
}
