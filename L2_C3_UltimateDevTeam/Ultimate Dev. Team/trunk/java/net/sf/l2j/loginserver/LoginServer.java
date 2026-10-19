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

import net.sf.l2j.Config;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.Server;
import net.sf.l2j.status.Status;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

/**
 * This class ...
 * 
 * @version $Revision: 1.9.4.4 $ $Date: 2005/03/27 15:30:09 $
 */
public class LoginServer extends Thread
{
    private static LoginServer _instance;
    
    private ServerSocket			clientServerSocket;
    private Logins					loginManager;
    private InetAddress				clientListeningIP;
    private String					_externalHostname;
    private String					_internalHostname;
    public static Status			statusServer;
    public static GameServerTable	gameservertable;
    public LoginController			loginController;

	private GameServerListener _gslistener;

    public static int PROTOCOL_REV = 0x0101;

    static Logger           _log = Logger.getLogger(LoginServer.class.getName());

    public static void main(String[] args) throws Exception
    {
	    System.out.println(".:===========================================:.");
	    System.out.println(".:       Thank you for using our software!   :.");
	    System.out.println(".:              ReMade bY Cr3a70r            :.");
	    System.out.println(".:            Ultimate Login Server          :.");
	    System.out.println(".:===========================================:.");
    	Server.SERVER_MODE = Server.MODE_LOGINSERVER;
//      Local Constants
		final String LOG_FOLDER = "log"; // Name of folder for log file
		final String LOG_NAME   = "./log.cfg"; // Name of log file
		
		/*** Main ***/
		// Create log folder
		File logFolder = new File(Config.DATAPACK_ROOT, LOG_FOLDER); 
		logFolder.mkdir();
		
		// Create input stream for log file -- or store file data into memory
		InputStream is =  new FileInputStream(new File(LOG_NAME));  
		LogManager.getLogManager().readConfiguration(is);
		is.close();
		
		// Initialize config and l2 db factory
		Config.load();
		L2DatabaseFactory.getInstance();
		
		gameservertable = GameServerTable.getInstance();
        LoginServer server = new LoginServer();
        _log.config("Stand Alone LoginServer Listening on port " + Config.PORT_LOGIN);
        server.start();
        
        if ( Config.IS_TELNET_ENABLED ) {
		    statusServer = new Status(Server.SERVER_MODE);
		    statusServer.start();
		}
		else {
		    System.out.println("Telnet server is currently disabled.");
		}
    }

    public void close(boolean restart)
    {
        this.interrupt();
        try
        {
        	_gslistener.interrupt();
        	gameservertable.shutDown();
        	clientServerSocket.close();
        }
        catch (IOException e)
        {
            _log.log(Level.INFO, "", e);
        }
        if(restart)
        {
        	Runtime.getRuntime().exit(2);
        }
        else
        {
        	Runtime.getRuntime().exit(0);
        }
    }

    /**
     * 
     */
    public void run()
    {
    	Server.loginServer = this;
        Socket connection = null;
        while (true)
        {
            try
            {
                if (Config.DEBUG)
                    _log.fine("waiting for client connection...");
                connection = clientServerSocket.accept();
                if (Config.DEBUG)
                    _log.fine("connection from " + connection.getInetAddress());

                // use local ip addess if no external hostname is set or if
                // the the connection is requested from localhost (for router
                // compatibility)
                // FIXME:Can we do that anymore with splited GL/LS?
                /*String connectedIp = connection.getInetAddress().getHostAddress();
                if (connectedIp.startsWith("192.168.") || connectedIp.startsWith("10.") || connectedIp.startsWith("172.16.") || connectedIp.startsWith("127.0.0.1"))
                {
                    if (Config.DEBUG)
                        _log.fine("using internal ip as server ip " + _internalHostname);
                    new ClientThread(connection, loginManager);
                }
                else
                {
                    if (Config.DEBUG)
                        _log.fine("using external ip as server ip " + _externalHostname);
                    new ClientThread(connection, loginManager);
                }*/
                new ClientThread(connection, loginManager);
                if (this.isInterrupted())
                {
                    // shutdown?
                    try {  clientServerSocket.close(); }
                    catch (IOException io)
                    {
                        _log.log(Level.INFO, "", io);
                    }
                    break;
                }
            }
            catch (Exception e)
            {
                try { connection.close(); } catch (Exception e2) {}
                if (this.isInterrupted())
                {
                    // shutdown?
                    try { clientServerSocket.close();}
                    catch (IOException io)
                    {
                        _log.log(Level.INFO, "", io);
                    }
                    break;
                }
            }
        }
    }

    public LoginServer(String threadName)
    {
        super(threadName);
        
    }

    public LoginServer() throws IOException
    {
        super("LoginServer");

        Properties serverSettings = new Properties();
        InputStream is = new FileInputStream( new File(Config.LOGIN_CONFIGURATION_FILE));
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
        
        _gslistener = GameServerListener.getInstance();
        _gslistener.start();
        
        loginController = LoginController.getInstance();

        /*
         * ArrayList<String> bannedIpList = new ArrayList<String>();
         * Properties bannedIpFile = new Properties(); try {
         * bannedIpFile.loadFromXML(getClass().getResourceAsStream(Config.BANNED_IP_XML)); }
         * catch (InvalidPropertiesFormatException e) { e.printStackTrace(); }
         * catch (IOException e) { e.printStackTrace(); }
         * bannedIpFile.setProperty(loginIp, createAccounts);
         * bannedIpList.toString()
         */
        InputStream bannedFile = null;
        
        try
        {
            bannedFile =  new FileInputStream(new File("./banned_ip.cfg"));
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
                _log.info("banned_ip.cfg not found");
            }

        }
        catch (Exception e)
        {
            _log.warning("error while reading banned file:" + e);
        }
        finally
        {
            try { bannedFile.close(); } catch (Exception e) {}
        }
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
