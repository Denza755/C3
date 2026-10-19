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
 * [URL]http://www.gnu.org/copyleft/gpl.html[/URL]
 */
package net.sf.l2j.gameserver.handler.admincommandhandlers;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.handler.IAdminCommandHandler;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.tables.MapRegionTable;

import java.util.logging.Logger;
//import net.sf.l2j.gameserver.model.Location;
//import net.sf.l2j.gameserver.packets.serverpackets.ActionFailed;
//import net.sf.l2j.gameserver.packets.serverpackets.StopMove;
/**
 * This class handles following admin commands:
 * - res = resurrects target L2Character
 * 
 * @version $Revision: 1.2.4.5 $ $Date: 2005/04/11 10:06:06 $
 */
public class AdminSendHome implements IAdminCommandHandler {
    private static Logger _log = Logger.getLogger(AdminSendHome.class.getName());
    private static String[] _adminCommands = {"admin_sendhome"};
    private static final int REQUIRED_LEVEL = Config.GM_TELEPORT;

    public boolean useAdminCommand(String command, L2PcInstance activeChar) {
        if (!(checkLevel(activeChar.getAccessLevel()) && activeChar.isGM())) return false;
        if (command.startsWith("admin_sendhome "))
            if(command.split(" ").length > 1)
                handleSendhome(activeChar, command.split(" ")[1]);
        if (command.equals("admin_sendhome")) handleSendhome(activeChar);
        return true;
    }
    
    public String[] getAdminCommandList() {
        return _adminCommands;
    }
    
    private boolean checkLevel(int level) {
        return (level >= REQUIRED_LEVEL);
    }
    
    private void handleSendhome(L2PcInstance activeChar)
    {
        handleSendhome(activeChar, null);
    }
    
    private void handleSendhome(L2PcInstance activeChar, String player) {
        L2Object obj = activeChar.getTarget();
        
        if (player != null)
        {
            L2PcInstance plyr = L2World.getInstance().getPlayer(player);
            
            if (plyr != null)
            {
                obj = plyr;
            }
        }
        
        if (obj == null)
            obj = activeChar;
        
        if ((obj != null) && (obj instanceof L2Character)) 
	{
            doSendhome((L2Character)obj);
        } 
        else 
        {
            activeChar.sendMessage("Incorrect target.");
        }
    }
    
    private void doSendhome(L2Character targetChar)
    {
	targetChar.teleToLocation(MapRegionTable.TeleportWhereType.Town);
    }
    
}