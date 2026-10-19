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
package net.sf.l2j;

import net.sf.l2j.gameserver.GameServer;
import net.sf.l2j.loginserver.LoginServer;
import net.sf.l2j.status.Status;

import java.io.File;
import java.io.InputStream;
import java.util.logging.LogManager;

/**
 * This class ...
 * 
 * @version $Revision: 1.5.4.9 $ $Date: 2005/03/29 23:15:33 $
 */
/***********************************************
 * This class appears to be the starter class. The class the calls upon the
 * login and game server classes. It's purpose is to initiate the log file
 * and start the servers
 * *********************************************
 */
public class Server
{
	public static final int MODE_GAMESERVER = 1;
	public static final int MODE_LOGINSERVER = 2;
	public static int SERVER_MODE = 0;
	// Global class instantiations. Used to actually ready the classes
	public static GameServer gameServer;
	public static LoginServer loginServer;
    public static Status statusServer;
	
	// Main Method
	public static void main(String[] args) throws Exception
	{
		// Local Constants
		final String LOG_FOLDER = "log"; // Name of folder for log file
		final String LOG_NAME   = "log.cfg"; // Name of log file
		final String CONF_LOC   = "net.sf.l2j.Config"; // Location of configuration data
		
		/*** Main ***/
		// Create log folder
		File logFolder = new File(Config.DATAPACK_ROOT, LOG_FOLDER); 
		logFolder.mkdir();
		
		// Create input stream for log file -- or store file data into memory
		InputStream is = new Server().getClass().getResourceAsStream(LOG_NAME);  
		LogManager.getLogManager().readConfiguration(is);
		is.close();
		
		// Initialize config 
		Class.forName(CONF_LOC); 
		L2DatabaseFactory.getInstance();
		
		// Run server threads
		gameServer = new GameServer();
        loginServer = LoginServer.getInstance();
		//loginServer = new LoginServer();
		//loginServer.start();

		// run telnet server if enabled
		if ( Config.IS_TELNET_ENABLED ) {
		    statusServer = new Status(0);
		    statusServer.start();
		}
		else {
		    System.out.println("Telnet server is currently disabled.");
		}
	}
}
