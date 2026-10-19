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
package net.sf.l2j.gameserver.handler.admincommandhandlers;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.handler.IAdminCommandHandler;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.util.Util;
import org.apache.bsf.BSFException;
import org.apache.bsf.BSFManager;

/**
 * This class handles following admin commands:
 * - kill = kills target L2Character
 * 
 * @version $Revision: 1.2.4.3 $ $Date: 2005/02/28 00:35:37 $
 */
public class AdminForth implements IAdminCommandHandler {
	private static String[] _adminCommands = {"admin_bsh", "admin_jython", "admin_reload_quest"};
	private static final int REQUIRED_LEVEL = Config.GM_GODMODE;

	public boolean useAdminCommand(String command, L2PcInstance activeChar)
    {
        try
        {
    		if (!(checkLevel(activeChar.getAccessLevel()) && activeChar.isGM())) return false;
    		Util.handleLogGmActions(activeChar, command);
    		if (command.startsWith("admin_bsh "))
    			handleBsh(activeChar, command.substring(10));
    		else if (command.startsWith("admin_jython "))
    			handleJython(activeChar, command.substring(13));
    		else if (command.startsWith("admin_reload_quest "))
    			handleJython(activeChar, "from data.jscript.quests import "+command.substring(19)+"; reload("+command.substring(19)+")");
        }
        catch (StringIndexOutOfBoundsException e)
        { }
		return true;
	}
	
	public String[] getAdminCommandList() {
		return _adminCommands;
	}
	
	private boolean checkLevel(int level) {
		return (level >= REQUIRED_LEVEL);
	}
	
	private void handleBsh(L2PcInstance activeChar, String code) {
		Object result;
		try {
			BSFManager bsf = new BSFManager();
			result = bsf.eval("beanshell", "admin", 0, 0, code);
		} catch (BSFException e) {
			result = e;
		}
		activeChar.sendMessage("BeanShell: "+result); 
	}

	private void handleJython(L2PcInstance activeChar, String code) {
		Object result;
		try {
			BSFManager bsf = new BSFManager();
			if (code.equals("reset")) {
				bsf.exec("jython", "admin", 0, 0, "from data.jscript.quests import Tutorial; reload(Tutorial)");
				result = "reset";
			} else {
				bsf.exec("jython", "admin", 0, 0, code);
				//result = bsf.eval("jython", "admin", 0, 0, code);
				result = null;
			}
		} catch (BSFException e) {
			result = e;
		}
		activeChar.sendMessage("Jython: "+result);
		Util.handleLogGmActions(activeChar,"result = "+result);
	}
}
