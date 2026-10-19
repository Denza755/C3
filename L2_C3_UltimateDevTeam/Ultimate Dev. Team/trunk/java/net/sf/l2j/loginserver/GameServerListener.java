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

import net.sf.l2j.Config;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

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
    
    private List<GameServerThread> _gameServerThreads;
    private static GameServerListener _instance;
    
	public static GameServerListener getInstance()
	{
		if (_instance == null)
		{
			_instance = new GameServerListener();
		}
		return _instance;
	}
    
    public GameServerListener()
    {
        try
        {
        	if(Config.GAME_SERVER_LOGIN_HOST.equals("*"))
        	{
	            gameServerSocket = new ServerSocket(Config.GAME_SERVER_LOGIN_PORT);
	            _log.info("Ok, Listening for gameServer on port "+Config.GAME_SERVER_LOGIN_PORT);
        	}
        	else
        	{
        		 gameServerSocket = new ServerSocket(Config.GAME_SERVER_LOGIN_PORT, 50 , InetAddress.getByName(Config.GAME_SERVER_LOGIN_HOST));
 	            _log.info("Ok, Listening for gameServer on "+ Config.GAME_SERVER_LOGIN_HOST +" on port "+Config.GAME_SERVER_LOGIN_PORT);
        	}
        }
        catch (IOException e)
        {
            try { gameServerSocket.close(); } catch (Exception e2) {}
            _log.warning("Error creating GameServerListener server socket: "+e);
            e.printStackTrace();
        }
        _gameServerThreads = new ArrayList<GameServerThread>();
    }

    public void run()
    {
        while (true)
        {
            Socket connection = null;
            GameServerThread gst = null;
            try
            {
                connection = gameServerSocket.accept();
                //FIXME: need to avoid forks here
                 gst = new GameServerThread(connection);
                _gameServerThreads.add(gst);
            }
            catch (Exception e)
            {
                try { connection.close(); } catch (Exception e2) {};
                
                //its a real problem
                if (!(e instanceof IOException))
                    e.printStackTrace();
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

	/**
	 * @return Returns the gameServerThreads.
	 */
	public List<GameServerThread> getGameServerThreads()
	{
		return _gameServerThreads;
	}
	
	/**
	 * Removes a GameServerThread from the list
	 */
	public void removeGameServer(GameServerThread gst)
	{
		_gameServerThreads.remove(gst);
	}
}
