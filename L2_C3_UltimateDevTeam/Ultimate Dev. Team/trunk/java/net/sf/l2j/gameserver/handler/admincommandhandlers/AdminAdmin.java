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
import net.sf.l2j.gameserver.cache.HtmCache;
import net.sf.l2j.gameserver.handler.IAdminCommandHandler;
import net.sf.l2j.gameserver.instancemanager.ZoneManager;
import net.sf.l2j.gameserver.model.L2Multisell;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.packets.serverpackets.*;
import net.sf.l2j.gameserver.tables.GmListTable;
import net.sf.l2j.gameserver.tables.ItemTable;
import net.sf.l2j.gameserver.tables.NpcTable;
import net.sf.l2j.gameserver.tables.SkillTable;

import java.util.StringTokenizer;

/**
 * This class handles following admin commands:
 * - admin = shows menu
 * 
 * @version $Revision: 1.3.2.1.2.4 $ $Date: 2005/04/11 10:06:06 $
 */
public class AdminAdmin implements IAdminCommandHandler {

 private static String[] _adminCommands = {"admin_admin","admin_admin2","admin_play_sounds","admin_play_sound",
                                           "admin_gmliston","admin_gmlistoff","admin_silence",
                                           "admin_atmosphere","admin_diet","admin_tradeoff",
                                           "admin_reload","admin_set","admin_config_server","admin_config_server2"};
	private static final int REQUIRED_LEVEL = Config.GM_MENU;

	public boolean useAdminCommand(String command, L2PcInstance activeChar) {
        
        if (!Config.ALT_PRIVILEGES_ADMIN)
            if (!(checkLevel(activeChar.getAccessLevel()) && activeChar.isGM())) return false;
        
		if (command.equals("admin_admin")) showMainPage(activeChar);
		if (command.equals("admin_admin2")) showMainPage2(activeChar);
		if (command.equals("admin_config_server")) ShowConfigPage(activeChar); 
		if (command.equals("admin_config_server2")) ShowConfigPage2(activeChar);
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
		
		else if(command.startsWith("admin_gmliston"))
		{
			GmListTable.getInstance().addGm(activeChar);
            activeChar.sendMessage("Registerd into gm list");
		}
		
		else if(command.startsWith("admin_gmlistoff"))
		{
		    GmListTable.getInstance().deleteGm(activeChar);
            activeChar.sendMessage("Removed from gm list");
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
        else if(command.startsWith("admin_reload"))
        {
            StringTokenizer st = new StringTokenizer(command);
            st.nextToken();

            try
            {
                String type = st.nextToken();

                if(type.equals("multisell"))
                {
                    L2Multisell.getInstance().reload();
                    activeChar.sendMessage("multisell reloaded");
                }
                else if(type.startsWith("skill"))
                {
                    SkillTable.getInstance().reload();
                    activeChar.sendMessage("skills reloaded");
                }
                else if(type.equals("npc"))
                {
                    NpcTable.getInstance().reloadAllNpc();
                    activeChar.sendMessage("npcs reloaded");
                }
                else if(type.startsWith("htm"))
                {
                    HtmCache.getInstance().reload();
                    activeChar.sendMessage("Cache[HTML]: " + HtmCache.getInstance().getMemoryUsage()  + " megabytes on " + HtmCache.getInstance().getLoadedFiles() + " files loaded");

                }
                else if(type.startsWith("item"))
                {
                	ItemTable.getInstance().reload();
                	activeChar.sendMessage("Item templates reloaded");
                }
                else if(type.startsWith("zone"))
                {
            	    ZoneManager.getInstance().reloadAll();
            	    activeChar.sendMessage("Zones reloaded.");
                }
            }
            catch(Exception e)
            {
                activeChar.sendMessage("Usage:  //reload <multisell|skill|npc|htm|item|zone>");
            }
        }

        else if(command.startsWith("admin_set") && activeChar.getAccessLevel()>=Config.GM_CAN_CONFIG)
        {
            StringTokenizer st = new StringTokenizer(command);
            st.nextToken();

            try
            {
                String pName = st.nextToken();
                String pValue = st.nextToken();
                
                if (Config.setParameterValue(pName, pValue))
                    activeChar.sendMessage("Parameter "+ pName +" have been set to "+ pValue);
                else activeChar.sendMessage("Invalid parameter!");
            }
            catch(Exception e)
            {
                activeChar.sendMessage("Usage:  //set <parameter> <value>");
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
            activeChar.sendMessage("Only sky and signsky atmosphere type allowed.");
        }

        if(packet != null)
        {
            for (L2PcInstance player : L2World.getInstance().getAllPlayers())
            {
                player.sendPacket(packet);
            }
        }
    }
    
	public void playAdminSound(L2PcInstance activeChar, String sound)
	{
		activeChar.sendPacket(new PlaySound(1,sound,0,0,0,0,0));
		activeChar.broadcastPacket(new PlaySound(1,sound,0,0,0,0,0));
		showMainPage(activeChar);
		activeChar.sendMessage("Playing "+sound+".");
	}

	public void showMainPage(L2PcInstance activeChar)
	{
		NpcHtmlMessage adminReply = new NpcHtmlMessage(5);

		StringBuffer replyMSG = new StringBuffer("<html><body>");
		replyMSG.append("<center><table width=270><tr><td width=60><button value=\"Config\" action=\"bypass -h admin_config_server\" width=60 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td><td width=150><font color=\"LEVEL\">Admin Manage Panel</font></td><td width=60><button value=\"Panel2\" action=\"bypass -h admin_admin2\" width=60 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr></table></center><br>");
		replyMSG.append("<center><table width=200><tr><td>");
		replyMSG.append("<button value=\"Character List\" action=\"bypass -h admin_show_characters 0\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
		replyMSG.append("<button value=\"Manage Server\" action=\"bypass -h admin_server_shutdown\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">"); 
		replyMSG.append("<button value=\"Announcements\" action=\"bypass -h admin_list_announcements\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
		replyMSG.append("<br>");
		replyMSG.append("<button value=\"Help Menu\" action=\"bypass -h admin_help admhelp.htm\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
		replyMSG.append("<button value=\"Fly Wyvern\" action=\"bypass -h admin_ride_wyvern\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
		replyMSG.append("<button value=\"Land Wyvern\" action=\"bypass -h admin_unride\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
		replyMSG.append("<button value=\"Invisible\" action=\"bypass -h admin_invisible\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
		replyMSG.append("<button value=\"Visible\" action=\"bypass -h admin_visible\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
		replyMSG.append("<br>");
		replyMSG.append("<button value=\"Paralyze World\" action=\"bypass -h admin_para_world\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
		replyMSG.append("<button value=\"Unparalyze\" action=\"bypass -h admin_unpara_world\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
	        replyMSG.append("<button value=\"Day\" action=\"bypass -h admin_atmosphere sky day\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
	        replyMSG.append("<button value=\"Night\" action=\"bypass -h admin_atmosphere sky night\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
		replyMSG.append("</td><td>");
		replyMSG.append("<button value=\"GM Shop\" action=\"bypass -h admin_gmshop\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
		replyMSG.append("<button value=\"Manage Chars\" action=\"bypass -h admin_char_manage\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
		replyMSG.append("<button value=\"Teleport Menu\" action=\"bypass -h admin_show_moves\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
		replyMSG.append("<br>");
		replyMSG.append("<button value=\"Create Item\" action=\"bypass -h admin_itemcreate\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
		replyMSG.append("<button value=\"Enchant Menu\" action=\"bypass -h admin_enchant\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
		replyMSG.append("<button value=\"Siege Menu\" action=\"bypass -h admin_siege\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");	
		//replyMSG.append("<button value=\"Mob Ctrl Menu\" action=\"bypass -h admin_mobmenu\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
        	replyMSG.append("<button value=\"Help & Info\" action=\"bypass -h admin_help admhelp.htm\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
		replyMSG.append("<button value=\"Npc Spawn\" action=\"bypass -h admin_show_spawns\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
		replyMSG.append("<br>");
		replyMSG.append("<button value=\"GM Silence\" action=\"bypass -h admin_silence\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
		replyMSG.append("<button value=\"GM Set\" action=\"bypass -h admin_set $menu_command\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
		replyMSG.append("<button value=\"Dawn Sky\" action=\"bypass -h admin_atmosphere signsky dawn\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
	        replyMSG.append("<button value=\"Dusk Sky\" action=\"bypass -h admin_atmosphere signsky dusk\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
		replyMSG.append("<br>");
		replyMSG.append("</td></tr></table><br>");
	        replyMSG.append("<table><tr><td>");
       		replyMSG.append("<button value=\"Re.Skill\" action=\"bypass -h admin_reload skill\" width=60 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td><td>");
	        replyMSG.append("<button value=\"Re.NPC\" action=\"bypass -h admin_reload npc\" width=60 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td><td>");
		replyMSG.append("<button value=\"Re.Html\" action=\"bypass -h admin_cache_htm_rebuild\" width=60 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
		replyMSG.append("</table></center><br>");
        	replyMSG.append("<center>Name of target:</center>");
	        replyMSG.append("<center><edit var=\"menu_command\" width=100 height=15></center><br>");
	        replyMSG.append("<center><table><tr><td>");
       		replyMSG.append("<button value=\"Kill\" action=\"bypass -h admin_kill\" width=50 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td><td>");
	        replyMSG.append("<button value=\"Kick\" action=\"bypass -h admin_kick $menu_command\" width=50 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td><td>");
		replyMSG.append("<button value=\"Open\" action=\"bypass -h admin_open\" width=50 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
		replyMSG.append("<br><tr><td>");
		replyMSG.append("<button value=\"Delete\" action=\"bypass -h admin_delete\" width=50 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td><td>");
		replyMSG.append("<button value=\"Ban\" action=\"bypass -h admin_ban $menu_command\" width=50 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td><td>");
		replyMSG.append("<button value=\"Close\" action=\"bypass -h admin_close\" width=50 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
		replyMSG.append("</table></center>");
//		replyMSG.append("<br><table width=200><tr><td><button value=\"Chat Ban\" action=\"bypass -h admin_banchat $menu_command\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td><td>");
//		replyMSG.append("<button value=\"Chat UnBan\" action=\"bypass -h admin_unbanchat $menu_command\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
//		replyMSG.append("</td></tr></table>");
		replyMSG.append("</body></html>");

        adminReply.setHtml(replyMSG.toString());
        activeChar.sendPacket(adminReply); 
	}

	public void showMainPage2(L2PcInstance activeChar)
	{
		NpcHtmlMessage adminReply = new NpcHtmlMessage(5);

		StringBuffer replyMSG = new StringBuffer("<html><body>");
		
		replyMSG.append("<center><table width=270><tr><td width=60><button value=\"Config\" action=\"bypass -h admin_config_server\" width=60 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td><td width=150><font color=\"LEVEL\">Admin Support Panel</font></td><td width=60><button value=\"Panel1\" action=\"bypass -h admin_admin\" width=60 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr></table></center><br>");
		replyMSG.append("<center><table width=200><tr><td>");
		replyMSG.append("<button value=\"Character List\" action=\"bypass -h admin_show_characters 0\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
		replyMSG.append("<button value=\"Manage Server\" action=\"bypass -h admin_server_shutdown\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">"); 
		replyMSG.append("<button value=\"Announcements\" action=\"bypass -h admin_list_announcements\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
		replyMSG.append("<br>");
		replyMSG.append("<button value=\"Monster Race\" action=\"bypass -h admin_mons\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
		replyMSG.append("<button value=\"Big Head\" action=\"bypass -h admin_bighead\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
		replyMSG.append("<button value=\"Shrink Head\" action=\"bypass -h admin_shrinkhead\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
		replyMSG.append("<button value=\"Polyself ID\" action=\"bypass -h admin_polyself $menu_command\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
		replyMSG.append("<button value=\"Unpolyself\" action=\"bypass -h admin_unpolyself\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
		replyMSG.append("<br>");
		replyMSG.append("<button value=\"Summon GMPet\" action=\"bypass -h admin_summon123 $menu_command\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
		replyMSG.append("<button value=\"Weight on\" action=\"bypass -h admin_diet off\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
		replyMSG.append("<button value=\"Weight off\" action=\"bypass -h admin_diet on\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
		replyMSG.append("<button value=\"Paralyze\" action=\"bypass -h admin_para\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
		replyMSG.append("<button value=\"Unparalyze\" action=\"bypass -h admin_unpara\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
		replyMSG.append("</td><td>");
		replyMSG.append("<button value=\"GM Shop\" action=\"bypass -h admin_gmshop\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
		replyMSG.append("<button value=\"Manage Chars\" action=\"bypass -h admin_char_manage\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
		replyMSG.append("<button value=\"Teleport Menu\" action=\"bypass -h admin_show_moves\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
		replyMSG.append("<br>");
		replyMSG.append("<button value=\"Give Adena\" action=\"bypass -h admin_create_adena $menu_command\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
		replyMSG.append("<button value=\"Enchant Weapon\" action=\"bypass -h admin_setew $menu_command\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
		replyMSG.append("<button value=\"Resurrection\" action=\"bypass -h admin_res\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
		replyMSG.append("<button value=\"Hide GM list\" action=\"bypass -h admin_gmlistoff\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
		replyMSG.append("<button value=\"Unhide GM list\" action=\"bypass -h admin_gmliston\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
		replyMSG.append("<br>");
	        replyMSG.append("<button value=\"Set level\" action=\"bypass -h admin_setlevel $menu_command\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
		replyMSG.append("<button value=\"Trade on\" action=\"bypass -h admin_tradeoff off\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
		replyMSG.append("<button value=\"Trade off\" action=\"bypass -h admin_tradeoff on\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
		replyMSG.append("<button value=\"Send Home\" action=\"bypass -h admin_sendhome\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
		replyMSG.append("<button value=\"Rem. Clanwait\" action=\"bypass -h admin_remclanwait\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
		replyMSG.append("<br>");
		replyMSG.append("</td></tr></table></center><br>");
		replyMSG.append("<center>Name / Number / ID / Karma / Ench </center>");
		replyMSG.append("<center><edit var=\"menu_command\" width=100 height=15></center><br>");
		replyMSG.append("<center><table width=200><tr><td>");
		replyMSG.append("<button value=\"Set Karma\" action=\"bypass -h admin_setkarma $menu_command\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
		replyMSG.append("<button value=\"Clear Karma\" action=\"bypass -h admin_nokarma\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
		replyMSG.append("</td><td>");
		replyMSG.append("<button value=\"Change Name\" action=\"bypass -h admin_changename $menu_command\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
		replyMSG.append("<button value=\"Play Sounds\" action=\"bypass -h admin_play_sounds\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
		replyMSG.append("</td></tr></table><br>");
		replyMSG.append("</center></body></html>");
		adminReply.setHtml(replyMSG.toString());
		activeChar.sendPacket(adminReply); 
	}
	
	public void ShowConfigPage2(L2PcInstance activeChar)
    {
    	NpcHtmlMessage adminReply = new NpcHtmlMessage(5);

		StringBuffer replyMSG = new StringBuffer("<html><body>");
		replyMSG.append("<center><table width=270><tr><td width=60><button value=\"Admin\" action=\"bypass -h admin_admin\" width=60 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td><td width=150><font color=\"LEVEL\">Config Server Panel</font></td><td width=60><button value=\"Panel1\" action=\"bypass -h admin_config_server\" width=60 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr></table></center><br>");
		replyMSG.append("<center><table width=260>");
		replyMSG.append("<tr><td><font color=\"LEVEL\">Allow SevenSign Teleport</font> = " + Config.ALLOW_SS_TELEPORT + "</td><td></td><td><button value=\""+ !Config.ALLOW_SS_TELEPORT +"\" action=\"bypass -h admin_set AllowSSTeleport " + !Config.ALLOW_SS_TELEPORT + "\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
		replyMSG.append("<tr><td><font color=\"LEVEL\">Allow SevenSign Regist</font> = " + Config.ALLOW_SS_REGIST + "</td><td></td><td><button value=\""+ !Config.ALLOW_SS_REGIST +"\" action=\"bypass -h admin_set AllowSSRegist " + !Config.ALLOW_SS_REGIST + "\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
		replyMSG.append("<tr><td><font color=\"LEVEL\">Show GM Login</font> = " + Config.SHOW_GM_LOGIN + "</td><td></td><td><button value=\""+ !Config.SHOW_GM_LOGIN +"\" action=\"bypass -h admin_set ShowGMLogin " + !Config.SHOW_GM_LOGIN + "\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
		replyMSG.append("<tr><td><font color=\"LEVEL\">Hide GM Status</font> = " + Config.HIDE_GM_STATUS + "</td><td></td><td><button value=\""+ !Config.HIDE_GM_STATUS +"\" action=\"bypass -h admin_set HideGMStatus " + !Config.HIDE_GM_STATUS + "\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
		replyMSG.append("<tr><td><font color=\"LEVEL\">Spawn Siege Guard</font> = " + Config.SPAWN_SIEGE_GUARD + "</td><td></td><td><button value=\""+ !Config.SPAWN_SIEGE_GUARD +"\" action=\"bypass -h admin_set SpawnSiegeGuard " + !Config.SPAWN_SIEGE_GUARD + "\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
		replyMSG.append("<tr><td><font color=\"LEVEL\">Auto Loot</font> = " + Config.AUTO_LOOT + "</td><td></td><td><button value=\""+ !Config.AUTO_LOOT +"\" action=\"bypass -h admin_set AutoLoot " + !Config.AUTO_LOOT + "\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
		replyMSG.append("<tr><td><font color=\"LEVEL\">Magic Failures</font> = " + Config.ALT_GAME_MAGICFAILURES + "</td><td></td><td><button value=\""+ !Config.ALT_GAME_MAGICFAILURES +"\" action=\"bypass -h admin_set MagicFailures " + !Config.ALT_GAME_MAGICFAILURES + "\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
		replyMSG.append("<tr><td><font color=\"LEVEL\">Book Needed</font> = " + Config.SP_BOOK_NEEDED + "</td><td></td><td><button value=\""+ !Config.SP_BOOK_NEEDED +"\" action=\"bypass -h admin_set SpBookNeeded " + !Config.SP_BOOK_NEEDED + "\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
		replyMSG.append("<tr><td><font color=\"LEVEL\">Learn Other Skill</font> = " + Config.ALT_GAME_SKILL_LEARN + "</td><td></td><td><button value=\""+ !Config.ALT_GAME_SKILL_LEARN +"\" action=\"bypass -h admin_set AltGameSkillLearn " + !Config.ALT_GAME_SKILL_LEARN + "\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
		replyMSG.append("<tr><td><font color=\"LEVEL\">Shield Block From All</font> = " + Config.ALT_GAME_SHIELD_BLOCKS + "</td><td></td><td><button value=\""+ !Config.ALT_GAME_SHIELD_BLOCKS +"\" action=\"bypass -h admin_set AltShieldBlocks " + !Config.ALT_GAME_SHIELD_BLOCKS + "\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
		replyMSG.append("<tr><td><font color=\"LEVEL\">Free Teleport</font> = " + Config.ALT_GAME_FREE_TELEPORT + "</td><td></td><td><button value=\""+ !Config.ALT_GAME_FREE_TELEPORT +"\" action=\"bypass -h admin_set AltFreeTeleporting " + !Config.ALT_GAME_FREE_TELEPORT + "\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
		replyMSG.append("<tr><td><font color=\"LEVEL\">Can Discard Items</font> = " + Config.ALLOW_DISCARDITEM + "</td><td></td><td><button value=\""+ !Config.ALLOW_DISCARDITEM +"\" action=\"bypass -h admin_set AllowDiscardItem " + !Config.ALLOW_DISCARDITEM + "\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
		replyMSG.append("<tr><td><font color=\"LEVEL\">Main Menu Language</font> = " + Config.MAIN_LANG + "</td><td><edit var=\"menu_command\" width=40 height=15></td><td><button value=\"Set\" action=\"bypass -h admin_set MainLanguage $menu_command\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
		replyMSG.append("<tr><td><font color=\"LEVEL\">Global Chat</font> = " + Config.DEFAULT_GLOBAL_CHAT + "</td><td><edit var=\"menu_command1\" width=40 height=15></td><td><button value=\"Set\" action=\"bypass -h admin_set GlobalChat $menu_command1\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
		replyMSG.append("<tr><td><font color=\"LEVEL\">Coord Synchronize</font> = " + Config.COORD_SYNCHRONIZE + "</td><td><edit var=\"menu_command2\" width=40 height=15></td><td><button value=\"Set\" action=\"bypass -h admin_set CoordSynchronize $menu_command2\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
		replyMSG.append("</table></body></html>");

        adminReply.setHtml(replyMSG.toString());
        activeChar.sendPacket(adminReply); 
    }
	
	public void ShowConfigPage(L2PcInstance activeChar)
    {
    	NpcHtmlMessage adminReply = new NpcHtmlMessage(5);

		StringBuffer replyMSG = new StringBuffer("<html><body>");
		replyMSG.append("<center><table width=270><tr><td width=60><button value=\"Admin\" action=\"bypass -h admin_admin\" width=60 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td><td width=150><font color=\"LEVEL\">Config Server Panel</font></td><td width=60><button value=\"Panel2\" action=\"bypass -h admin_config_server2\" width=60 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr></table></center><br>");
		replyMSG.append("<center><table width=260>");
		replyMSG.append("<tr><td><font color=\"LEVEL\">Auto loot</font> = " + Config.AUTO_LOOT + "</td><td></td><td><button value=\""+ !Config.AUTO_LOOT +"\" action=\"bypass -h admin_set AutoLoot " + !Config.AUTO_LOOT + "\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
		replyMSG.append("<tr><td><font color=\"LEVEL\">Allow Discard Item</font> = " + Config.ALLOW_DISCARDITEM + "</td><td></td><td><button value=\""+ !Config.ALLOW_DISCARDITEM +"\" action=\"bypass -h admin_set AllowDiscardItem " + !Config.ALLOW_DISCARDITEM + "\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
		replyMSG.append("<tr><td><font color=\"LEVEL\">Deep Blue Mobs Drop Rule</font> = " + Config.DEEPBLUE_DROP_RULES + "</td><td></td><td><button value=\""+ !Config.DEEPBLUE_DROP_RULES +"\" action=\"bypass -h admin_set UseDeepBlueDropRules " + !Config.DEEPBLUE_DROP_RULES + "\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
		replyMSG.append("<tr><td><font color=\"LEVEL\">Auto Destroy Item aft. sec.</font> = " + Config.AUTODESTROY_ITEM_AFTER + "</td><td><edit var=\"menu_command\" width=40 height=15></td><td><button value=\"Set\" action=\"bypass -h admin_set AutoDestroyDroppedItemAfter $menu_command\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
		replyMSG.append("<tr><td><font color=\"LEVEL\">Rate EXP</font> = " + Config.RATE_XP + "</td><td><edit var=\"menu_command1\" width=40 height=15></td><td><button value=\"Set\" action=\"bypass -h admin_set RateXP $menu_command1\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
		replyMSG.append("<tr><td><font color=\"LEVEL\">Rate SP</font> = " + Config.RATE_SP + "</td><td><edit var=\"menu_command2\" width=40 height=15></td><td><button value=\"Set\" action=\"bypass -h admin_set RateSP $menu_command2\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
		replyMSG.append("<tr><td><font color=\"LEVEL\">Rate Drop Adena</font> = " + Config.RATE_DROP_ADENA + "</td><td><edit var=\"menu_command3\" width=40 height=15></td><td><button value=\"Set\" action=\"bypass -h admin_set RateDropAdena $menu_command3\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
		replyMSG.append("<tr><td><font color=\"LEVEL\">Rate Drop Items</font> = " + Config.RATE_DROP_ITEMS + "</td><td><edit var=\"menu_command4\" width=40 height=15></td><td><button value=\"Set\" action=\"bypass -h admin_set RateDropItems $menu_command4\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
		replyMSG.append("<tr><td><font color=\"LEVEL\">Rate Drop Spoil</font> = " + Config.RATE_DROP_SPOIL + "</td><td><edit var=\"menu_command5\" width=40 height=15></td><td><button value=\"Set\" action=\"bypass -h admin_set RateDropSpoil $menu_command5\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
		replyMSG.append("<tr><td><font color=\"LEVEL\">Rate Quest Reward</font> = " + Config.RATE_QUESTS_REWARD + "</td><td><edit var=\"menu_command6\" width=40 height=15></td><td><button value=\"Set\" action=\"bypass -h admin_set RateQuestsReward $menu_command6\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
		replyMSG.append("<tr><td><font color=\"LEVEL\">Rate Raid Regen HP</font> = " + Config.RAID_HP_REGEN_MULTIPLIER + "</td><td><edit var=\"menu_command7\" width=40 height=15></td><td><button value=\"Set\" action=\"bypass -h admin_set RaidHpRegenMultiplier $menu_command7\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
		replyMSG.append("<tr><td><font color=\"LEVEL\">Rate Raid Regen MP</font> = " + Config.RAID_MP_REGEN_MULTIPLIER + "</td><td><edit var=\"menu_command8\" width=40 height=15></td><td><button value=\"Set\" action=\"bypass -h admin_set RaidMpRegenMultiplier $menu_command8\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
		replyMSG.append("<tr><td><font color=\"LEVEL\">Rate Raid Defence</font> = " + Config.RAID_DEFENCE_MULTIPLIER + "</td><td><edit var=\"menu_command9\" width=40 height=15></td><td><button value=\"Set\" action=\"bypass -h admin_set RaidDefenceMultiplier $menu_command9\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
		replyMSG.append("<tr><td><font color=\"LEVEL\">Alt Buff Time</font> = " + Config.ALT_BUFF_TIME + "</td><td><edit var=\"menu_command10\" width=40 height=15></td><td><button value=\"Set\" action=\"bypass -h admin_set AltBuffTime $menu_command10\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
		replyMSG.append("<tr><td><font color=\"LEVEL\">Min Seven Sign Party</font> = " + Config.MIN_SS_PARTY + "</td><td><edit var=\"menu_command11\" width=40 height=15></td><td><button value=\"Set\" action=\"bypass -h admin_set MinSSParty $menu_command11\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
		replyMSG.append("<tr><td><font color=\"LEVEL\">Value Acient Adena Trade</font> = " + Config.VALUE_AA + "</td><td><edit var=\"menu_command12\" width=40 height=15></td><td><button value=\"Set\" action=\"bypass -h admin_set ValueAA $menu_command12\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
		replyMSG.append("</table></body></html>");

        adminReply.setHtml(replyMSG.toString());
        activeChar.sendPacket(adminReply); 
    }

}
