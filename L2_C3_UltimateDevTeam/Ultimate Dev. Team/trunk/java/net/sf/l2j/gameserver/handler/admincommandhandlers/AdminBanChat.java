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
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.handler.IAdminCommandHandler;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;

import java.sql.PreparedStatement;
import java.util.StringTokenizer;
import java.util.logging.Logger;

/**
 * This class handles following admin commands:
 * - kill = kills target L2Character
 * 
 * @version $Revision: 1.1.6.3 $ $Date: 2005/04/11 10:06:06 $
 */
public class AdminBanChat implements IAdminCommandHandler {
    private static Logger _log = Logger.getLogger(AdminBan.class.getName());
    private static String[] _adminCommands = {"admin_banchat", "admin_unbanchat"};
    private static final int REQUIRED_LEVEL = Config.GM_BAN_CHAT;

    public boolean useAdminCommand(String command, L2PcInstance activeChar)
    {
        if (!Config.ALT_PRIVILEGES_ADMIN)
        {
            if (!(checkLevel(activeChar.getAccessLevel())))
            {
                System.out.println("Not required level");
                return false;
            }
        }
        
        if (command.startsWith("admin_banchat"))
        {
            StringTokenizer st = new StringTokenizer(command);
            if (st.countTokens() > 1)
            {
                st.nextToken();
                String player = st.nextToken();
                setCharacterAccessLevel(player, activeChar, -1);
            }
        }
        else if (command.startsWith("admin_unbanchat"))
            {
                StringTokenizer st = new StringTokenizer(command);
                if (st.countTokens() > 1)
                {
                    st.nextToken();
                    String player = st.nextToken();
                    setCharacterAccessLevel(player, activeChar, 0);
                }
            }
        return true;
    }
    
    public String[] getAdminCommandList() {
        return _adminCommands;
    }
    
    private boolean checkLevel(int level) {
        return (level >= REQUIRED_LEVEL);
    }
    
    private void setCharacterAccessLevel(String user, L2PcInstance player, int banLevel)
    {
        java.sql.Connection con = null;
        try
        {           
            con = L2DatabaseFactory.getInstance().getConnection();
            
            String stmt = "UPDATE characters SET characters.accesslevel = ? WHERE characters.char_name=?";
            PreparedStatement statement = con.prepareStatement(stmt);
            statement.setInt(1, banLevel);
            statement.setString(2, user);
            statement.executeUpdate();
            statement.close();
            
            L2PcInstance userInstance = L2World.getInstance().getPlayer(user);
            if (userInstance != null)
                userInstance.setAccessLevel(banLevel);

            player.sendMessage("Character Access Level for "+user+" set to "+banLevel+".");
        }
        catch (Exception e)
        {
            _log.warning("Could not set accessLevl:"+e);
        } 
        finally 
        {
            try { con.close(); } catch (Exception e) {}
        }
    }
}
