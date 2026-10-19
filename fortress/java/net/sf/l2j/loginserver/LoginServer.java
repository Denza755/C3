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
import java.io.FileInputStream;
//import java.io.InputStreamReader;
//import java.io.LineNumberReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.l2j.Config;
import net.sf.l2j.loginserver.IpManager;

/**
 * This class ...
 * 
 * @version $Revision: 1.9.4.4 $ $Date: 2005/03/27 15:30:09 $
 */
public class LoginServer extends Thread
{
    private static LoginServer _instance;
    
    private ServerSocket    clientServerSocket;

    private Logins          loginManager;

    private InetAddress     clientListeningIP;
    private int             clientGameServerPort;

    private String          _externalHostname;

    private String          _internalHostname;

    static Logger           _log = Logger.getLogger(LoginServer.class.getName());

    public static void main(String[] args)
    {
        LoginServer server = new LoginServer("Stand Alone LoginServer.");
        _log.config("Stand Alone LoginServer Listening on port " + Config.PORT_LOGIN);
        server.start();
    }

    public void close()
    {
        this.interrupt();
        try
        {
            clientServerSocket.close();
        }
        catch (IOException e)
        {
            _log.log(Level.INFO, "", e);
            e.printStackTrace();
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
                if (Config.DEBUG)
                    _log.fine("waiting for client connection...");
                Socket connection = clientServerSocket.accept();
		connection.setSoTimeout(10*60*1000); // 10min timeout for login socket
                if (Config.DEBUG)
                    _log.fine("connection from " + connection.getInetAddress());

                // use local ip addess if no external hostname is set or if
                // the the connection is requested from localhost (for router
                // compatibility)
                String connectedIp = connection.getInetAddress().getHostAddress();
                if (connectedIp.startsWith("192.168.") || connectedIp.startsWith("10.") || connectedIp.startsWith("172.16.") || connectedIp.startsWith("127.0.0.1"))
                {
					if("*".equals(_internalHostname))
					{
						new ClientThread(connection, loginManager, connection.getLocalAddress().getHostAddress(), clientGameServerPort);
					}
					else
					{
						if (Config.DEBUG)
							_log.fine("using internal ip as server ip " + _internalHostname);
						new ClientThread(connection, loginManager, _internalHostname, clientGameServerPort);
					}
                }
                else
                {
					if("*".equals(_externalHostname))
					{
						new ClientThread(connection, loginManager, connection.getLocalAddress().getHostAddress(), clientGameServerPort);
					}
					else
					{
						if (Config.DEBUG)
							_log.fine("using external ip as server ip " + _externalHostname);
						new ClientThread(connection, loginManager, _externalHostname, clientGameServerPort);
					}
                }
                if (this.isInterrupted())
                {
                    // shutdown?
                    try
                    {
                        clientServerSocket.close();
                    }
                    catch (IOException io)
                    {
                        _log.log(Level.INFO, "", io);
                        io.printStackTrace();
                    }
                    break;
                }
            }
            catch (IOException e)
            {
                // not a real problem
                if (this.isInterrupted())
                {
                    // shutdown?
                    try
                    {
                        clientServerSocket.close();
                    }
                    catch (IOException io)
                    {
                        _log.log(Level.INFO, "", io);
                        io.printStackTrace();
                    }
                    break;
                }
            }
        }
    }

    public LoginServer(String threadName)
    {
        super(threadName);

        new GameServerListener().start();
    }

    public LoginServer() throws IOException
    {
        super("LoginServer");

        Properties serverSettings = new Properties();
        InputStream is = new FileInputStream(net.sf.l2j.Server.fortressHomeDir+Config.CONFIGURATION_FILE);
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
        String createAccounts = serverSettings.getProperty("AutoCreateAccounts");

        if (!"*".equals(loginIp))
        {
            clientListeningIP = InetAddress.getByName(loginIp);
            _log.config("LoginServer listening on IP:" + clientListeningIP.getHostAddress() + " Port " + Config.PORT_LOGIN);
            clientServerSocket = new ServerSocket(Config.PORT_LOGIN, 50, clientListeningIP);
        }
        else
        {
            _log.config("LoginServer listening on all available IPs on Port " + Config.PORT_LOGIN);
            clientServerSocket = new ServerSocket(Config.PORT_LOGIN);
        }

        _log.config("Hostname for external connections is: " + _externalHostname);
        _log.config("Hostname for internal connections is: " + _internalHostname);

        loginManager = new Logins(Boolean.valueOf(createAccounts).booleanValue());

        clientGameServerPort = Config.PORT_GAME;
        /*
         * ArrayList<String> bannedIpList = new ArrayList<String>();
         * Properties bannedIpFile = new Properties(); try {
         * bannedIpFile.loadFromXML(getClass().getResourceAsStream(Config.BANNED_IP_XML)); }
         * catch (InvalidPropertiesFormatException e) { e.printStackTrace(); }
         * catch (IOException e) { e.printStackTrace(); }
         * bannedIpFile.setProperty(loginIp, createAccounts);
         * bannedIpList.toString()
         */
        /*try
        {
            InputStream bannedFile = new FileInputStream(net.sf.l2j.Server.fortressHomeDir+"config/banned_ip.cfg");
            if (bannedFile != null)
            {
                int count = 0;
                InputStreamReader reader = new InputStreamReader(bannedFile);
                LineNumberReader lnr = new LineNumberReader(reader);
                String line = null;
                while ((line = lnr.readLine()) != null)
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
                _log.info("config/banned_ip.cfg not found");
            }

        }
        catch (Exception e)
        {
            _log.warning("error while reading banned file:" + e);
        }
*/
        _log.info(IpManager.getInstance().getBannedCount() + " banned IPs defined");
    }
    
    /**
     * 
     */
    public static LoginServer getInstance()
    {
        // If no instances started before, try to start a new one
        if (_instance == null)
        {
            try
            {
                _instance = new LoginServer();
                _instance.start();
            }
            catch (IOException e)
            {
                // Throws the exception, if any
                System.out.println(e.getMessage());
            }
        }
        // Return the actual instance
        return _instance;
    }
    
    /**
     * 
     */
    public boolean unblockIp(String ipAddress)
    {
        if (loginManager.ipBlocked(ipAddress))
        {
            return true;
        }
        return false;
    }
}
