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

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.l2j.Config;
import net.sf.l2j.loginserver.serverpackets.Init;
import net.sf.l2j.util.Util;

/**
 * This class ...
 * 
 * @version $Revision: 1.2 $ $Date: 2004/06/27 08:12:59 $
 */

public class GameServerListener extends Thread
{
    protected static Logger _log = Logger.getLogger(LoginServer.class.getName());

    protected ServerSocket  gameServerSocket;
    protected LoginManager  loginManager        = LoginManager.getInstance();

    public class GameServerRegistration implements Runnable
    {
        private Socket      connection;
        private String      connectionIpAddress;
        @SuppressWarnings("unused")
        private String      gameserverIpAddress;
        private String      hostname;
        private ByteBuffer  readBuffer              = ByteBuffer.allocate(64*1024);
        
        private InputStream in;
        private BufferedOutputStream out;

        public GameServerRegistration(Socket connection)
        {
            this.connection = connection;
            readBuffer.order(ByteOrder.LITTLE_ENDIAN);
            start();
        }

        public void run()
        {
            connectionIpAddress   = connection.getInetAddress().getHostAddress();
            
            setHostName();

            try
            {
                in  = connection.getInputStream();
                out = new BufferedOutputStream(connection.getOutputStream());
                
                gameserverIpAddress = InetAddress.getByName(hostname).getHostAddress();

                // _crypt = new NewCrypt("[;'.]94-31==-%&@!^+]\000");

                if (isBannedGameserverIP(connectionIpAddress))
                {
                    //LoginFail lok = new LoginFail(LoginFail.REASON_ACCOUNT_BANNED);
                    //sendPacket(lok);
                    // throw new IOException("banned IP");
                    _log.info("GameServerRegistration: IP Address " + connectionIpAddress + " is on Banned IP list.");
                }
                
                _log.fine("GameServerRegistration: Connected to GamerServer at " + connectionIpAddress);
                
                handleRegistrationProcess();
            }
            catch (IOException e)
            {
                _log.log(Level.FINE, "", e);
                e.printStackTrace();
            }
        }

        /**
         * @throws IOException 
         * 
         */
        private void handleRegistrationProcess() throws IOException
        {
            Init startPacket = new Init(new byte[0]);
            out.write(startPacket.getLength() & 0xff);
            out.write(startPacket.getLength() >> 8 &0xff);
            out.write(startPacket.getContent());
            out.flush();
            
            while (true)
            {
                readBuffer.put((byte)in.read());
                readBuffer.put((byte)in.read());
                
                int packetLength    = readBuffer.getShort();
                
                if (packetLength < 1)
                {
                    _log.warning("LoginServer: Gameserver terminated the connection.");
                }
                
                readBuffer.limit(packetLength);
                
                while (true)
                {
                    readBuffer.put((byte)in.read());
                }
                
                
            }
        }

        /**
         * 
         */
        private void setHostName()
        {
            /*
             * Use local ip addess if no external hostname is set or if the the
             * connection is requested from localhost (for router compatibility)
             */
            if (Util.isInternalIP(connectionIpAddress))
            {
                if (Config.DEBUG)
                {
                    _log.fine("Using Internal IP as Server IP. Internal Hostname is " + Config.INTERNAL_HOSTNAME);
                }
                hostname    = Config.INTERNAL_HOSTNAME;
            }
            else
            {
                if (Config.DEBUG)
                {
                    _log.fine("Using External IP as Server IP. External Hostname is " + Config.EXTERNAL_HOSTNAME);
                }
                hostname    = Config.EXTERNAL_HOSTNAME;
            }
        }
    }

    public GameServerListener()
    {
        try
        {
            gameServerSocket = new ServerSocket(Config.GAME_SERVER_LOGIN_PORT);
        }
        catch (IOException e)
        {
            _log.warning("Error creating GameServerListener server socket: "+e);
            e.printStackTrace();
        }
    }

    /**
     * @param ipAddress
     * @return
     */
    public boolean isBannedGameserverIP(@SuppressWarnings("unused") String ipAddress)
    {
        // TODO Auto-generated method stub
        return false;
    }

    public void run()
    {
        while (true)
        {
            try
            {
                Socket connection = gameServerSocket.accept();

                new GameServerRegistration(connection);
            }
            catch (IOException e)
            {
                // not a real problem
            }

            if (this.isInterrupted())
            {
                try
                {
                    gameServerSocket.close();
                }
                catch (IOException io)
                {
                    _log.finer("Error closing GameServerListener socket: "+io);
                }
                break;
            }
        }
    }
}
