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

import java.util.StringTokenizer;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.handler.IAdminCommandHandler;
import net.sf.l2j.gameserver.model.L2Multisell;
import net.sf.l2j.gameserver.model.L2PcInstance;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.serverpackets.PlaySound;
import net.sf.l2j.gameserver.serverpackets.ServerBasePacket;
import net.sf.l2j.gameserver.serverpackets.SignsSky;
import net.sf.l2j.gameserver.serverpackets.SunRise;
import net.sf.l2j.gameserver.serverpackets.SunSet;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;

/**
 * This class handles following admin commands:
 * - admin = shows menu
 * 
 * @version $Revision: 1.3.2.1.2.4 $ $Date: 2005/04/11 10:06:06 $
 */
public class AdminAdmin implements IAdminCommandHandler {

 private static String[] _adminCommands = {"admin_admin","admin_play_sounds","admin_play_sound",
                                           "admin_silence",
                                           "admin_atmosphere","admin_diet","admin_tradeoff",
                                           "admin_reload_multisell"};
	private static final int REQUIRED_LEVEL = Config.GM_MENU;

	public boolean useAdminCommand(String command, L2PcInstance activeChar) {
		if (!(checkLevel(activeChar.getAccessLevel()) && activeChar.isGM())) return false;
		if (command.equals("admin_admin")) showMainPage(activeChar);

		if (command.equals("admin_play_sounds"))
		{
			AdminHelpPage.showHelpPage(activeChar, "songs/songs.htm");
		}

		else if (command.startsWith("admin_play_sounds"))
		{
            try
            {
                AdminHelpPage.showHelpPage(activeChar, "songs/songs"+command.substring(17)+".htm");
            }
            catch (StringIndexOutOfBoundsException e)
            { }
		}

		else if (command.startsWith("admin_play_sound"))
		{
            try
            {
                playAdminSound(activeChar,command.substring(17));
            }
            catch (StringIndexOutOfBoundsException e)
            { }
		}
       
        else if(command.startsWith("admin_silence"))
        {     	
			if(activeChar.getMessageRefusal()) // already in message refusal mode
			{
				activeChar.setMessageRefusal(false);
				activeChar.sendPacket(new SystemMessage(SystemMessage.MESSAGE_ACCEPTANCE_MODE));
			}
		    else
	        {
		    	activeChar.setMessageRefusal(true);
				activeChar.sendPacket(new SystemMessage(SystemMessage.MESSAGE_REFUSAL_MODE));
	        }	    
		    
		}
        
        else if(command.startsWith("admin_atmosphere"))
        {
            try
            {
                StringTokenizer st = new StringTokenizer(command);
                st.nextToken();
                String type = st.nextToken();
                String state = st.nextToken();
                adminAtmosphere(type,state,activeChar);
            }
            catch(Exception ex)
            {
            }
        }
        else if(command.startsWith("admin_diet"))
        {
            try
            {
                StringTokenizer st = new StringTokenizer(command);
                st.nextToken();
                if(st.nextToken().equalsIgnoreCase("on"))
                {
                    activeChar.setDietMode(true);
                    activeChar.refreshOverloaded();
                    activeChar.sendMessage("Diet mode on");
                }
                else if(st.nextToken().equalsIgnoreCase("off"))
                {
                    activeChar.setDietMode(false);
                    activeChar.sendMessage("Diet mode off");
                }
            }
            catch(Exception ex)
            {
                if(activeChar.getDietMode())
                    activeChar.sendMessage("Diet mode currently on");
                else
                    activeChar.sendMessage("Diet mode currently off");
            }            
        }
        else if(command.startsWith("admin_tradeoff"))
        {
            try
            {
                StringTokenizer st = new StringTokenizer(command);
                st.nextToken();
                if(st.nextToken().equalsIgnoreCase("on"))
                {
                    activeChar.setTradeRefusal(true);
                    activeChar.sendMessage("tradeoff enabled");
                }
                else if(st.nextToken().equalsIgnoreCase("off"))
                {
                    activeChar.setTradeRefusal(false);
                    activeChar.sendMessage("tradeoff disabled");
                }
            }
            catch(Exception ex)
            {
                if(activeChar.getTradeRefusal())
                    activeChar.sendMessage("tradeoff currently enabled");
                else
                    activeChar.sendMessage("tradeoff currently disabled");
            }            
        }
        else if(command.startsWith("admin_reload_multisell"))
        {
            try
            {
                L2Multisell.getInstance().reload();
            }            
            catch(Exception e){return false;}    // ConcModif possible?
            
            activeChar.sendMessage("Multisell list reloaded!");
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
    
    /**
     * 
     * @param type - atmosphere type (signssky,sky)
     * @param state - atmosphere state(night,day)
     */
    public void adminAtmosphere(String type, String state, L2PcInstance activeChar)
    {
        ServerBasePacket packet = null;
        
        if(type.equals("signsky"))
        {
            if(state.equals("dawn"))
            {
                packet = new SignsSky(2);
            }
            else if(state.equals("dusk"))
            {
                packet = new SignsSky(1);
            }
        }
        else if(type.equals("sky"))
        {
                if(state.equals("night"))
                {
                    packet = new SunSet();
                }
                else if(state.equals("day"))
                {
                    packet = new SunRise();
                }
        }
        else
        {
            SystemMessage sm = new SystemMessage(614);
            sm.addString("Only sky and signsky atmosphere type allowed, damn u!");
            activeChar.sendPacket(sm);
        }

        if(packet != null)
        {
            for (L2PcInstance player : L2World.getAllPlayers())
            {
                player.sendPacket(packet);
            }
        }
    }
    
	public void playAdminSound(L2PcInstance activeChar, String sound)
	{
		activeChar.broadcastPacket(new PlaySound(1,sound,0,0,0,0,0));
		showMainPage(activeChar);
		activeChar.sendMessage("Playing "+sound+".");
	}

	public void showMainPage(L2PcInstance activeChar)
	{
		NpcHtmlMessage adminReply = new NpcHtmlMessage(5);

		StringBuffer replyMSG = new StringBuffer("<html><body>");
		
		replyMSG.append("<center>L2J Admin Panel</center><br>");
		replyMSG.append("<center><table width=200><tr><td>");
		replyMSG.append("<button value=\"Character List\" action=\"bypass -h admin_show_characters 0\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
		replyMSG.append("<button value=\"Manage Chars\" action=\"bypass -h admin_char_manage\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
		replyMSG.append("<button value=\"Manage Server\" action=\"bypass -h admin_server_shutdown\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">"); 
		replyMSG.append("<button value=\"Announcements\" action=\"bypass -h admin_list_announcements\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
		replyMSG.append("<br>");
		replyMSG.append("<button value=\"Fly Wyvern\" action=\"bypass -h gm_ride_wyvern\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
		replyMSG.append("<button value=\"Land Wyvern\" action=\"bypass -h gm_unride\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
		replyMSG.append("<button value=\"Ride Strider\" action=\"bypass -h gm_ride_strider\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
		replyMSG.append("<br>");
		replyMSG.append("<button value=\"Find Mammon\" action=\"bypass -h admin_find_mammon\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
		replyMSG.append("<button value=\"List Spawns\" action=\"bypass -h admin_list_spawns $menu_command\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">"); 
		replyMSG.append("<button value=\"Mob Ctrl Menu\" action=\"bypass -h admin_mobmenu\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
		replyMSG.append("</td><td>");
		replyMSG.append("<button value=\"GM Shop\" action=\"bypass -h admin_gmshop\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
		replyMSG.append("<button value=\"Npc Spawn\" action=\"bypass -h admin_show_spawns\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
		replyMSG.append("<button value=\"Create Item\" action=\"bypass -h admin_itemcreate\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
		replyMSG.append("<button value=\"Teleport Menu\" action=\"bypass -h admin_show_moves\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
		replyMSG.append("<br>");
		replyMSG.append("<button value=\"Siege Menu\" action=\"bypass -h admin_siege\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
		replyMSG.append("<button value=\"Set Karma\" action=\"bypass -h admin_setkarma $menu_command\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
		replyMSG.append("<button value=\"Clear Karma\" action=\"bypass -h admin_nokarma\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
		replyMSG.append("<br>");
		replyMSG.append("<button value=\"Enchant Weapon\" action=\"bypass -h admin_setew $menu_command\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
//		replyMSG.append("<button value=\"Play Sounds\" action=\"bypass -h admin_play_sounds\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
		replyMSG.append("<button value=\"JBF menu\" action=\"bypass -h jbf_admin_menu\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
		replyMSG.append("<button value=\"Help & Info\" action=\"bypass -h admin_help admhelp.htm\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
		replyMSG.append("<br>");
		replyMSG.append("</td></tr></table></center><br>");
        replyMSG.append("<center>Name / Karma / Ench 0-65535:</center>");
        replyMSG.append("<center><edit var=\"menu_command\" width=100 height=15></center><br>");
        replyMSG.append("<center><table><tr><td>");
        replyMSG.append("<button value=\"Invis\" action=\"bypass -h admin_invis\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td><td>");
        replyMSG.append("<button value=\"Kill\" action=\"bypass -h admin_kill\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td><td>");
        replyMSG.append("<button value=\"Kick\" action=\"bypass -h admin_kick $menu_command\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td><td>");
        replyMSG.append("<button value=\"Open\" action=\"bypass -h admin_open\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
        replyMSG.append("<br><tr><td>");
        replyMSG.append("<button value=\"Revis\" action=\"bypass -h admin_vis\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td><td>");
        replyMSG.append("<button value=\"Delete\" action=\"bypass -h admin_delete\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td><td>");
        replyMSG.append("<button value=\"Ban\" action=\"bypass -h admin_ban $menu_command\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td><td>");
        replyMSG.append("<button value=\"Close\" action=\"bypass -h admin_close\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
        replyMSG.append("</table><br><table width=200><tr><td>");
        replyMSG.append("<button value=\"Chat Ban\" action=\"bypass -h admin_banchat $menu_command\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td><td>");
        replyMSG.append("<button value=\"Chat UnBan\" action=\"bypass -h admin_unbanchat $menu_command\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
        replyMSG.append("</td></tr></table></center>");
        replyMSG.append("<center><table><br>");
        replyMSG.append("<tr><td><button value=\"Day\" action=\"bypass -h admin_atmosphere sky day\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
        replyMSG.append("<td><button value=\"Night\" action=\"bypass -h admin_atmosphere sky night\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
        replyMSG.append("<tr><td><button value=\"Dawn Sky\" action=\"bypass -h admin_atmosphere signsky dawn\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
        replyMSG.append("<td><button value=\"Dusk Sky\" action=\"bypass -h admin_atmosphere signsky dusk\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
        replyMSG.append("</table></center><br><br><br>");

        replyMSG.append("<tr><td>Server Software: Server software running L2J version 3.0.1 nightly created by L2Chef and the L2J team.</td></tr>");
        replyMSG.append("</body></html>");

        adminReply.setHtml(replyMSG.toString());
        activeChar.sendPacket(adminReply); 
	}
}
