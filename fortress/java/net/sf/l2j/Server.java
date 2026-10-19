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

import java.io.File;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.logging.LogManager;

import net.sf.l2j.gameserver.GameServer;
import net.sf.l2j.loginserver.LoginServer;
import net.sf.l2j.status.Status;
import net.sf.l2j.webserver.WebServer;

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
	// Global class instantiations. Used to actually ready the classes
	public static GameServer gameServer;
	public static LoginServer loginServer;
    public static Status statusServer;
    public static String fortressHomeDir = System.getProperty("user.dir")+"/";

	public static WebServer webServer = null;
	
	// Main Method
	public static void main(String[] args) throws Exception
	{
		String fortressHome = System.getProperty("fortress.home");
        if (fortressHome != null) {
            File home = new File(fortressHome);
            if (!home.isAbsolute()) {
                try { fortressHome = home.getCanonicalPath(); }
                catch (IOException e) { fortressHome = home.getAbsolutePath(); }
            }
            fortressHomeDir=fortressHome+"/";
            System.setProperty("python.home",fortressHome);	// python "cachedir"
            System.setProperty("user.dir",fortressHome);
        }

		// Local Constants
		final String LOG_FOLDER = "log"; // Name of folder for log file
		final String LOG_NAME   = "/config/log.cfg"; // Name of log file
		final String CONF_LOC   = "net.sf.l2j.Config"; // Location of configuration data
		
		/*** Main ***/
		// Initialize config 
		Class.forName(CONF_LOC); 

		// Create log folder
		File logFolder = new File("./", LOG_FOLDER); 
		logFolder.mkdir();
		
		// Create input stream for log file -- or store file data into memory
		InputStream is = new FileInputStream(fortressHomeDir+LOG_NAME);  
		LogManager.getLogManager().readConfiguration(is);
		is.close();

		if(Config.WebServerPort != 0)
			(webServer = new WebServer()).start();
		
		L2DatabaseFactory.getInstance();
		
		// Run server threads
		gameServer = new GameServer();
        loginServer = LoginServer.getInstance();
		//loginServer = new LoginServer();
		//loginServer.start();

		// run telnet server if enabled
		if(Config.IS_TELNET_ENABLED )
		{
		    statusServer = new Status();
		    statusServer.start();
		}
		else
		{
		    System.out.println("Telnet server is currently disabled.");
		}
		
		if(Config.MAT_BANCHAT)
			System.out.println("MAT AutoBANChat filter enable.");
		else 
			System.out.println("MAT AutoBANChat filter disable.");
	}
}
