/*
 *  * This program is free software; you can redistribute it and/or modify
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

//import java.util.logging.Logger;

import java.util.ArrayList;

import net.sf.l2j.Config;
import net.sf.l2j.loginserver.IpManager;
import net.sf.l2j.loginserver.IpManager.BannedIp;
import net.sf.l2j.gameserver.handler.IAdminCommandHandler;
import net.sf.l2j.gameserver.model.L2PcInstance;
import net.sf.l2j.gameserver.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;

/**
 * This class handles following admin commands:
 * - banip
 * - unbanip
 *
 * @version $Revision: 1 $ $Date: 2006/01/27 10:06:06 $
 */
public class AdminIP implements IAdminCommandHandler
{

    //private static final Logger _log = Logger.getLogger(AdminIP.class.getName());

    private static final int REQUIRED_LEVEL = Config.GM_BAN;
    private static String[] _adminCommands = {
        "admin_baniplist","admin_banip","admin_unbanip"
    };

    /* (non-Javadoc)
     * @see net.sf.l2j.gameserver.handler.IAdminCommandHandler#useAdminCommand(java.lang.String, net.sf.l2j.gameserver.model.L2PcInstance)
     */
    public boolean useAdminCommand(String command, L2PcInstance activeChar)
    {
        if (!(checkLevel(activeChar.getAccessLevel()) && activeChar.isGM())) return false;

        if (command.equals("admin_baniplist"))
        {
            try
            {
                ArrayList<BannedIp> baniplist = new ArrayList<BannedIp>();
                baniplist = IpManager.getInstance().getBanList();
                
                NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
                StringBuffer replyMSG = new StringBuffer("<html><body>");
                
                replyMSG.append("<center>Ban IP List</center><br>");
                replyMSG.append("<center><table width=300><tr><td>");
                replyMSG.append("<center>Ip</center></td><td><center>Banned by</center></td></tr>");
                for(BannedIp temp:baniplist)
                    replyMSG.append("<tr><td><center>"+temp.ip+"</center></td><td><center>"+temp.admin+"</center></td><td><center><button value=\"Unban ip\" action=\"bypass -h admin_unbanip"+temp.ip+"\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></center></td></tr>");
                replyMSG.append("</table></center>");
                replyMSG.append("</body></html>");

                adminReply.setHtml(replyMSG.toString());
                activeChar.sendPacket(adminReply); 
            }
            catch (StringIndexOutOfBoundsException e)
            {
                // Send syntax to the user
                SystemMessage sm = new SystemMessage(SystemMessage.S1_S2);
                sm.addString("Usage mode: //baniplist");
                activeChar.sendPacket(sm);
            }
        }
        else if (command.startsWith("admin_banip "))
        {
            try
            {
                String ipAddress = command.substring(11);
                IpManager.getInstance().BanIp(ipAddress, activeChar.getName());
                activeChar.sendMessage("Ip "+ipAddress+" has been banned");
            }
            catch (StringIndexOutOfBoundsException e)
            {
                // Send syntax to the user
                SystemMessage sm = new SystemMessage(SystemMessage.S1_S2);
                sm.addString("Usage mode: //banip <ip>");
                activeChar.sendPacket(sm);
            }
        }
        else if (command.startsWith("admin_unbanip "))
        {
            try
            {
                String ipAddress = command.substring(13);
                IpManager.getInstance().UnbanIp(ipAddress);
                activeChar.sendMessage("Ip "+ipAddress+" has been unbanned");
            }
            catch (StringIndexOutOfBoundsException e)
            {
                // Send syntax to the user
                SystemMessage sm = new SystemMessage(SystemMessage.S1_S2);
                sm.addString("Usage mode: //unbanip <ip>");
                activeChar.sendPacket(sm);
            }
        }
        
        return true;
    }

    public String[] getAdminCommandList()
    {
        return _adminCommands;
    }

    private boolean checkLevel(int level)
    {
        return (level >= REQUIRED_LEVEL);
    }
}