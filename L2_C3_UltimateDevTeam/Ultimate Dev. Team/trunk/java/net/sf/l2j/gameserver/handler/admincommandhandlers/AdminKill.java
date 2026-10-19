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
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.util.Util;

import java.util.logging.Logger;

/**
 * This class handles following admin commands:
 * - kill = kills target L2Character
 * 
 * @version $Revision: 1.2.4.5 $ $Date: 2005/04/11 10:06:06 $
 */
public class AdminKill implements IAdminCommandHandler {
	private static Logger _log = Logger.getLogger(AdminKill.class.getName());
	private static String[] _adminCommands = {"admin_kill"};
	private static final int REQUIRED_LEVEL = Config.GM_NPC_EDIT;

	public boolean useAdminCommand(String command, L2PcInstance activeChar) {
        if (!Config.ALT_PRIVILEGES_ADMIN)
            if (!(checkLevel(activeChar.getAccessLevel()) && activeChar.isGM())) return false;
        
        if (command.startsWith("admin_kill ")) handleKill(activeChar, command.split(" ")[1]);
        if (command.equals("admin_kill")) handleKill(activeChar);
		return true;
	}
	
	public String[] getAdminCommandList() {
		return _adminCommands;
	}
	
	private boolean checkLevel(int level) {
		return (level >= REQUIRED_LEVEL);
	}
	
    private void handleKill(L2PcInstance activeChar)
    {
        handleKill(activeChar, null);
    }
	
	private void handleKill(L2PcInstance activeChar, String player) {
		L2Object obj = activeChar.getTarget();
        if (player != null)
        {
            L2PcInstance plyr = L2World.getInstance().getPlayer(player);
            if (plyr != null)
            {
                obj = plyr;
            }
            else
            {
                int radius  = Integer.parseInt(player);
                for (L2Object object : activeChar.getKnownList().getKnownObjects())
                {
                    if (object instanceof L2Character)
                    {
                        L2Character character = (L2Character) object;
                        character.reduceCurrentHp(character.getMaxHp() + character.getMaxCp() + 1, character);
                    }
                }
                activeChar.sendMessage("Killed within " + radius + " unit radius.");
		Util.handleLogGmActions(activeChar, "Killed within " + radius + " unit radius.");
                return;
            }
        }
        
		if ((obj != null) && (obj instanceof L2Character)) {
			L2Character target = (L2Character)obj;
			target.reduceCurrentHp(target.getMaxHp() + target.getMaxCp() + 1, activeChar);
			if (Config.DEBUG) 
					_log.fine("GM: "+activeChar.getName()+"("+activeChar.getObjectId()+")"+
							" killed character "+target.getObjectId());
			Util.handleLogGmActions(activeChar, " killed character "+target.getName()+" "+target.getObjectId());
		} else {
			activeChar.sendMessage("Incorrect target.");
		}
	}
}
