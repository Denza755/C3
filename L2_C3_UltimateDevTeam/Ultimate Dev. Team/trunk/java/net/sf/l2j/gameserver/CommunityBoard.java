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
package net.sf.l2j.gameserver;

import net.sf.l2j.Config;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.clientpackets.Say2;
import net.sf.l2j.gameserver.model.L2Clan;
import net.sf.l2j.gameserver.model.L2ClanMember;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.base.Experience;
import net.sf.l2j.gameserver.packets.serverpackets.CreatureSay;
import net.sf.l2j.gameserver.packets.serverpackets.ShowBoard;
import net.sf.l2j.gameserver.packets.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.tables.RecordTable;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.logging.Logger;

public class CommunityBoard
{
	private static Logger _log = Logger.getLogger(CommunityBoard.class.getName());
	private static CommunityBoard _instance;
	
	public static CommunityBoard getInstance()
	{
		if (_instance == null)
		{
			_instance = new CommunityBoard();
		}
		
		return _instance;
	}
	
	public void handleCommands(ClientThread client, String command)
	{
		L2PcInstance activeChar = client.getActiveChar();
        if(activeChar == null)
            return;
		
		if (command.startsWith("bbs_"))
		{
			StringBuffer htmlCode = new StringBuffer("<html><body><br>");

			if (command.startsWith("bbs_default") && (Config.SHOW_STATUS_COMMUNITYBOARD || activeChar.isGM()))
			{
				int startIndex;
				String arg = command.substring("bbs_default".length()).trim();
				if (arg.equals(""))
					startIndex = 0;
				else
				{
					try {
						startIndex = Integer.parseInt(arg);
						if (startIndex < 0)
							throw new Exception();
						if (startIndex > L2World.getInstance().getAllPlayers().size()-1)
							startIndex = 0;
					} catch (Exception e) {
						_log.warning("Bad argument was passed to bbs_default. "+arg);
						return;
					}
				}
				onBBSDefault(htmlCode, startIndex, activeChar);
			}

			
			else if (command.equals("bbs_default") || command.equals("bbs_top"))
			{
				htmlCode.append("<center><br><br1><br1>");
				htmlCode.append("<table border=0 cellspacing=0 cellpadding=0>");
				htmlCode.append("<tr>");
				htmlCode.append("<td width=50></td>");
				htmlCode.append("<td align=center width=560 height=30 align=left><img src=\"l2ui.bbs_lineage2\" width=128 height=16 ></td>");
				htmlCode.append("</tr>");
				htmlCode.append("</table>");
				htmlCode.append("<table border=0 cellspacing=0 cellpadding=2 bgcolor=5A5A5A width=610>");
				htmlCode.append("<tr>");
				htmlCode.append("<td WIDTH=60 align=center>&$412;</td>");
				htmlCode.append("<td WIDTH=410 align=center>&$413;</td>");
				htmlCode.append("<td WIDTH=80 align=center>&$414;</td>");
				htmlCode.append("<td WIDTH=110 align=center>&$415;</td>");
				htmlCode.append("</tr>");
				htmlCode.append("</table>");
				htmlCode.append("<br>");
				htmlCode.append("<table border=0 cellspacing=0 cellpadding=2>");
				htmlCode.append("<tr>");
				htmlCode.append("<td FIXWIDTH=60 align=right valign=top><img src=\"l2ui.bbs_folder\" width=32 height=32></td>");
//				 action
				htmlCode.append("<td FIXWIDTH=410 align=left valign=top><a action=\"bypass bbs_init_1_1\">Favorites</a><br1><font color=\"AAAAAA\">Bookmarks</font></td>");
				htmlCode.append("<td FIXWIDTH=80 align=center valign=top>0</td>");
				htmlCode.append("<td FIXWIDTH=110 align=center valign=top>admin</td>");
				htmlCode.append("</tr>");
				htmlCode.append("</table>");
				htmlCode.append("<table border=0 cellspacing=0 cellpadding=0>");
				htmlCode.append("<tr>");
				htmlCode.append("<td width=610><img src=\"l2ui.squaregray\" width=\"610\" height=\"1\"></td>");
				htmlCode.append("</tr>");
				htmlCode.append("</table>");
				htmlCode.append("<br>");
				htmlCode.append("<table border=0 cellspacing=0 cellpadding=2>");
				htmlCode.append("<tr>");
				htmlCode.append("<td FIXWIDTH=60 align=right valign=top><img src=\"l2ui.bbs_folder\" width=32 height=32></td>");
//				 action
				htmlCode.append("<td FIXWIDTH=410 align=left valign=top><a action=\"bypass bbs_init_1_2\">Lineage II News</a><br1><font color=\"AAAAAA\">New Information</font></td>");
				htmlCode.append("<td FIXWIDTH=80 align=center valign=top>0</td>");
				htmlCode.append("<td FIXWIDTH=110 align=center valign=top>admin</td>");
				htmlCode.append("</tr>");
				htmlCode.append("</table>");
				htmlCode.append("<table border=0 cellspacing=0 cellpadding=0>");
				htmlCode.append("<tr>");
				htmlCode.append("<td width=610><img src=\"l2ui.squaregray\" width=\"610\" height=\"1\"></td>");
				htmlCode.append("</tr>");
				htmlCode.append("</table>");
				htmlCode.append("<br>");
				htmlCode.append("<table border=0 cellspacing=0 cellpadding=2>");
				htmlCode.append("<tr>");
				htmlCode.append("<td FIXWIDTH=60 align=right valign=top><img src=\"l2ui.bbs_folder\" width=32 height=32></td>");
//				 action
				htmlCode.append("<td FIXWIDTH=410 align=left valign=top><a action=\"bypass bbs_init_1_3\">Clan Community</a><br1><font color=\"AAAAAA\">Clan Community</font></td>");
				htmlCode.append("<td FIXWIDTH=80 align=center valign=top>0</td>");
				htmlCode.append("<td FIXWIDTH=110 align=center valign=top>admin</td>");
				htmlCode.append("</tr>");
				htmlCode.append("</table>");
				htmlCode.append("<table border=0 cellspacing=0 cellpadding=0>");
				htmlCode.append("<tr>");
				htmlCode.append("<td width=610><img src=\"l2ui.squaregray\" width=\"610\" height=\"1\"></td>");
				htmlCode.append("</tr>");
				htmlCode.append("</table>");
				htmlCode.append("<br><br></center>");
			}
			
			else if (command.equals("bbs_init_1_1") || command.equals("bbs_favorite") || command.equals("bbs_region"))
			{
				htmlCode.append("<center><br><br1><br1>");
				htmlCode.append("<table border=0 cellspacing=0 cellpadding=0>");
				htmlCode.append("<tr>");
				htmlCode.append("<td FIXWIDTH=15>&nbsp;</td>");
//				 action
				htmlCode.append("<td width=610 height=30 align=left><a action=\"bypass bbs_top\"> HOME </a>&nbsp;&gt; <a action=\"bypass bbs_init_1_1\"> Get Favorites </a></td>");
				htmlCode.append("</tr>");
				htmlCode.append("</table>");
				htmlCode.append("<table border=0 cellspacing=0 cellpadding=2 bgcolor=5A5A5A width=610>");
				htmlCode.append("<tr>");
				htmlCode.append("<td FIXWIDTH=50>&nbsp;</td>");
				htmlCode.append("<td FIXWIDTH=350 align=center>&$413;</td>");
				htmlCode.append("<td FIXWIDTH=150 align=center>&$418;</td>");
				htmlCode.append("<td FIXWIDTH=60 align=center><img src=\"l2ui.mini_logo\" width=5 height=1></td>");
				htmlCode.append("</tr>");
				htmlCode.append("</table>");
				htmlCode.append("<table border=0 cellspacing=0 cellpadding=0><tr>");
				htmlCode.append("<td width=610><img src=\"sek.cbui355\" width=\"610\" height=\"1\"><br><img src=\"sek.cbui355\" width=\"610\" height=\"1\"></td>");
				htmlCode.append("</tr>");
				htmlCode.append("</table>");
				htmlCode.append("</center>");
			}
			
			else if (command.equals("bbs_init_1_2"))
			{
				htmlCode.append("<center><br><br1><br1>");
				htmlCode.append("<table border=0 cellspacing=0 cellpadding=0>");
				htmlCode.append("<tr>");
				htmlCode.append("<td FIXWIDTH=15>&nbsp;</td>");
//				 action
				htmlCode.append("<td width=610 height=30 align=left><a action=\"bypass bbs_top\"> HOME </a>&nbsp;&gt; <a action=\"bypass bbs_init_1_2\"> Lineage II News </a></td>");
				htmlCode.append("</tr>");
				htmlCode.append("</table>");
				htmlCode.append("<table border=0 cellspacing=0 cellpadding=2 bgcolor=5A5A5A width=610>");
				htmlCode.append("<tr>");
				htmlCode.append("<td WIDTH=60 align=center>&$412;</td>");
				htmlCode.append("<td WIDTH=410 align=center>&$413;</td>");
				htmlCode.append("<td WIDTH=80 align=center>&$414;</td>");
				htmlCode.append("<td WIDTH=110 align=center>&$415;</td>");
				htmlCode.append("</tr>");
				htmlCode.append("</table>");
				htmlCode.append("<br>");
				htmlCode.append("<table border=0 cellspacing=0 cellpadding=2><tr>");
				htmlCode.append("<td FIXWIDTH=60 align=right valign=top><img src=\"l2ui.bbs_board\" width=32 height=32></td>");
//				 action
				htmlCode.append("<td FIXWIDTH=410 align=left valign=top><a action=\"bypass bbs_init_1_2_1\">Events (0)</a><br1><font color=\"AAAAAA\">Scheduled Events</font></td>");
				htmlCode.append("<td FIXWIDTH=80 align=center valign=top>0</td>");
				htmlCode.append("<td FIXWIDTH=110 align=center valign=top>admin</td>");
				htmlCode.append("</tr>");
				htmlCode.append("</table>");
				htmlCode.append("<table border=0 cellspacing=0 cellpadding=0>");
				htmlCode.append("<tr>");
				htmlCode.append("<td width=610><img src=\"l2ui.squaregray\" width=\"610\" height=\"1\"></td>");
				htmlCode.append("</tr>");
				htmlCode.append("</table>");
				htmlCode.append("<br>");
				htmlCode.append("<table border=0 cellspacing=0 cellpadding=2>");
				htmlCode.append("<tr>");
				htmlCode.append("<td FIXWIDTH=60 align=right valign=top><img src=\"l2ui.bbs_board\" width=32 height=32></td>");
//				 action
				htmlCode.append("<td FIXWIDTH=410 align=left valign=top><a action=\"bypass bbs_init_1_2_2\">Official Announcements (0)</a><br1><font color=\"AAAAAA\">General Information</font></td>");
				htmlCode.append("<td FIXWIDTH=80 align=center valign=top>0</td>");
				htmlCode.append("<td FIXWIDTH=110 align=center valign=top>admin</td>");
				htmlCode.append("</tr>");
				htmlCode.append("</table>");
				htmlCode.append("<table border=0 cellspacing=0 cellpadding=0>");
				htmlCode.append("<tr>");
				htmlCode.append("<td width=610><img src=\"l2ui.squaregray\" width=\"610\" height=\"1\"></td>");
				htmlCode.append("</tr>");
				htmlCode.append("</table>");
				htmlCode.append("<br>");
				htmlCode.append("<table border=0 cellspacing=0 cellpadding=2>");
				htmlCode.append("<tr>");
				htmlCode.append("<td FIXWIDTH=60 align=right valign=top><img src=\"l2ui.bbs_board\" width=32 height=32></td>");
//				 action
				htmlCode.append("<td FIXWIDTH=410 align=left valign=top><a action=\"bypass bbs_init_1_2_3\">Update Information (0)</a><br1><font color=\"AAAAAA\">Patch Notes</font></td>");
				htmlCode.append("<td FIXWIDTH=80 align=center valign=top>0</td>");
				htmlCode.append("<td FIXWIDTH=110 align=center valign=top>admin</td>");
				htmlCode.append("</tr>");
				htmlCode.append("</table>");
				htmlCode.append("<table border=0 cellspacing=0 cellpadding=0>");
				htmlCode.append("<tr>");
				htmlCode.append("<td width=610><img src=\"l2ui..squaregray\" width=\"610\" height=\"1\"></td>");
				htmlCode.append("</tr>");
				htmlCode.append("</table>");
				htmlCode.append("<br><br></center>");
			}
			
			else if (command.equals("bbs_init_1_2_1"))
			{
				htmlCode.append("<center><br><br1><br1>");
				htmlCode.append("<table border=0 cellspacing=0 cellpadding=0>");
				htmlCode.append("<tr>");
				htmlCode.append("<td FIXWIDTH=15>&nbsp;</td>");
//				 action
				htmlCode.append("<td width=610 height=30 align=left><a action=\"bypass bbs_top\"> HOME </a>&nbsp;&gt; <a action=\"bypass bbs_init_1_2\"> Lineage II News </a>&nbsp;&gt; <a action=\"bypass bbs_init_1_2_1\"> Events </a></td>");
				htmlCode.append("</tr>");
				htmlCode.append("</table>");
				htmlCode.append("<table border=0 cellspacing=0 cellpadding=2 bgcolor=5A5A5A width=610>");
				htmlCode.append("<tr>");
				htmlCode.append("<td FIXWIDTH=60 align=center>&$416;</td>");
				htmlCode.append("<td FIXWIDTH=305 align=center>&$413;</td>");
				htmlCode.append("<td FIXWIDTH=120 align=center>&$417;</td>");
				htmlCode.append("<td FIXWIDTH=70 align=center>&$418;</td>");
				htmlCode.append("<td FIXWIDTH=55 align=center>&$419;</td>");
				htmlCode.append("</tr>");
				htmlCode.append("</table>");
				htmlCode.append("<br>");
				htmlCode.append("<table width=610 cellspace=0 cellpadding=0>");
				htmlCode.append("<tr>");
//				 action
				htmlCode.append("<td width=50><button value=\"&$422;\" action=\"bypass _bbslist_5_1_0\" back=\"l2ui_ch3.smallbutton2_down\" width=65 height=20 fore=\"l2ui_ch3.smallbutton2\"></td>");
				htmlCode.append("<td width=510 align=center>");
				htmlCode.append("<table cellspacing=2 cellpadding=0 border=0>");
				htmlCode.append("<tr>");
				htmlCode.append("<td><button action=\"\" back=\"l2ui_ch3.prev1_down\" fore=\"l2ui_ch3.prev1\" width=16 height=16 ></td>");
				htmlCode.append("<td>1</td>");
				htmlCode.append("<td><button action=\"\" back=\"l2ui_ch3.next1_down\" fore=\"l2ui_ch3.next1\" width=16 height=16 ></td>");
				htmlCode.append("</tr>");
				htmlCode.append("</table>");
				htmlCode.append("</td>");
//				 action
				htmlCode.append("<td align=right><button value = \"&$421;\" action=\"bypass _bbser2_5_1_0\" back=\"l2ui_ch3.smallbutton2_down\" width=65 height=20 fore=\"l2ui_ch3.smallbutton2\" ></td>");
				htmlCode.append("</tr>");
				htmlCode.append("<tr>");
				htmlCode.append("<td><img src=\"l2ui.mini_logo\" width=5 height=10></td>");
				htmlCode.append("</tr>");
				htmlCode.append("<tr>");
				htmlCode.append("<td></td>");
				htmlCode.append("<td align=center>");
				htmlCode.append("<table border=0>");
				htmlCode.append("<tr>");
				htmlCode.append("<td><combobox width=65 var=keyword list=\"Title;Writer\"></td>");
				htmlCode.append("<td><edit var=\"Search\" width=130 height=11 length=\"16\"></td>");
//				 action
				htmlCode.append("<td><button value=\"&$420;\" action=\"Write 5 5 0 Search keyword keyword\" back=\"l2ui_ch3.smallbutton2_down\" width=65 height=20 fore=\"l2ui_ch3.smallbutton2\"></td>");
				htmlCode.append("</tr>");
				htmlCode.append("</table>");
				htmlCode.append("</td>");
				htmlCode.append("</tr>");
				htmlCode.append("</table>");
				htmlCode.append("<br><br><br></center>");
			}
			
			else if (command.equals("bbs_init_1_2_2"))
			{
				htmlCode.append("<center><br><br1><br1>");
				htmlCode.append("<table border=0 cellspacing=0 cellpadding=0>");
				htmlCode.append("<tr>");
				htmlCode.append("<td FIXWIDTH=15>&nbsp;</td>");
//				 action
				htmlCode.append("<td width=610 height=30 align=left><a action=\"bypass bbs_top\"> HOME </a>&nbsp;&gt; <a action=\"bypass bbs_init_1_2\"> Lineage II News </a>&nbsp;&gt; <a action=\"bypass bbs_init_1_2_2\"> Official Announcements </a></td>");
				htmlCode.append("</tr>");
				htmlCode.append("</table>");
				htmlCode.append("<table border=0 cellspacing=0 cellpadding=0 width=610  bgcolor=333333>");
				htmlCode.append("<tr>");
				htmlCode.append("<td height=10></td>");
				htmlCode.append("</tr>");
				htmlCode.append("<tr>");
				htmlCode.append("<td fixWIDTH=5></td>");
				htmlCode.append("<td fixWIDTH=90 align=left valign=top>[&$429;]</td>");
//				 action
				htmlCode.append("<td fixWIDTH=380 align=left valign=top><a action=\"bypass bbs_init_1_2_2_1\">Welcome to the world of Lineage II</a></td>");
				htmlCode.append("<td fixWIDTH=75 align=right valign=top>&$418; :</td>");
				htmlCode.append("<td fixWIDTH=55 align=right valign=top>05-05-10</td>");
				htmlCode.append("<td fixWIDTH=5></td>");
				htmlCode.append("</tr>");
				htmlCode.append("<tr>");
				htmlCode.append("<td height=10></td>");
				htmlCode.append("</tr>");
				htmlCode.append("</table>");
				htmlCode.append("<table border=0 cellspacing=0 cellpadding=0>");
				htmlCode.append("<tr>");
				htmlCode.append("<td height=10></td>");
				htmlCode.append("</tr>");
				htmlCode.append("</table>");
				htmlCode.append("<table border=0 cellspacing=0 cellpadding=2 bgcolor=5A5A5A width=610>");
				htmlCode.append("<tr>");
				htmlCode.append("<td FIXWIDTH=60 align=center>&$416;</td>");
				htmlCode.append("<td FIXWIDTH=305 align=center>&$413;</td>");
				htmlCode.append("<td FIXWIDTH=120 align=center>&$417;</td>");
				htmlCode.append("<td FIXWIDTH=70 align=center>&$418;</td>");
				htmlCode.append("<td FIXWIDTH=55 align=center>&$419;</td>");
				htmlCode.append("</tr>");
				htmlCode.append("</table>");
				htmlCode.append("<br>");
				htmlCode.append("<table width=610 cellspace=0 cellpadding=0>");
				htmlCode.append("<tr>");
//				 action
				htmlCode.append("<td width=50><button value=\"&$422;\" action=\"bypass _bbslist_4_1_0\" back=\"l2ui_ch3.smallbutton2_down\" width=65 height=20 fore=\"l2ui_ch3.smallbutton2\"></td>");
				htmlCode.append("<td width=510 align=center>");
				htmlCode.append("<table cellspacing=2 cellpadding=0 border=0>");
				htmlCode.append("<tr>");
//				 action
				htmlCode.append("<td><button action=\"\" back=\"l2ui_ch3.prev1_down\" fore=\"l2ui_ch3.prev1\" width=16 height=16 ></td>");
				htmlCode.append("<td>1</td>");
//				 action
				htmlCode.append("<td><button action=\"\" back=\"l2ui_ch3.next1_down\" fore=\"l2ui_ch3.next1\" width=16 height=16 ></td>");
				htmlCode.append("</tr>");
				htmlCode.append("</table>");
				htmlCode.append("</td>");
//				 action
				htmlCode.append("<td align=right><button value = \"&$421;\" action=\"bypass _bbser2_4_1_0\" back=\"l2ui_ch3.smallbutton2_down\" width=65 height=20 fore=\"l2ui_ch3.smallbutton2\"></td>");
				htmlCode.append("</tr>");
				htmlCode.append("<tr>");
				htmlCode.append("<td><img src=\"l2ui.mini_logo\" width=5 height=10></td>");
				htmlCode.append("</tr>");
				htmlCode.append("<tr>");
				htmlCode.append("<td></td> ");
				htmlCode.append("<td align=center>");
				htmlCode.append("<table border=0>");
				htmlCode.append("<tr>");
				htmlCode.append("<td><combobox width=65 var=keyword list=\"Title;Writer\"></td>");
				htmlCode.append("<td><edit var=\"Search\" width=130 height=11 length=\"16\"></td>");
//				 action
				htmlCode.append("<td><button value=\"&$420;\" action=\"Write 5 4 0 Search keyword keyword\" back=\"l2ui_ch3.smallbutton2_down\" width=65 height=20 fore=\"l2ui_ch3.smallbutton2\"></td>");
				htmlCode.append("</tr>");
				htmlCode.append("</table>");
				htmlCode.append("</td>");
				htmlCode.append("</tr>");
				htmlCode.append("</table>");
				htmlCode.append("<br><br><br></center>");
			}
			
			else if (command.equals("bbs_init_1_2_2_1"))
			{
				htmlCode.append("<br><br1><br1>");
				htmlCode.append("<table border=0 cellspacing=0 cellpadding=0>");
				htmlCode.append("<tr>");
				htmlCode.append("<td FIXWIDTH=15>&nbsp;</td>");
//				 action
				htmlCode.append("<td width=610 height=30 align=left><a action=\"bypass bbs_top\"> HOME </a>&nbsp;&gt; <a action=\"bypass bbs_init_1_2\"> Lineage II News </a>&nbsp;&gt; <a action=\"bypass bbs_init_1_2_2\"> Official Announcements </a></td>");
				htmlCode.append("</tr>");
				htmlCode.append("</table>");
				htmlCode.append("<center>");
				htmlCode.append("<table width=610 border=0 cellspacing=0 cellpadding=0 bgcolor=333333>");
				htmlCode.append("<tr>");
				htmlCode.append("<td height=10></td>");
				htmlCode.append("</tr>");
				htmlCode.append("<tr>");
				htmlCode.append("<td fixWIDTH=55 align=center valign=top></td>");
				htmlCode.append("<td fixWIDTH=380 valign=top>Welcome to the world of Lineage II</td>");
				htmlCode.append("<td fixwidth=5></td><td><font color=\"AAAAAA\">&$418; :</font></td>");
				htmlCode.append("<td><font color=\"AAAAAA\">2005-05-10 11:31:50</font></td>");
				htmlCode.append("</tr>");
				htmlCode.append("<tr>");
				htmlCode.append("<td height=10></td>");
				htmlCode.append("</tr>");
				htmlCode.append("</table>");
				htmlCode.append("<br>");
				htmlCode.append("<table border=0 cellspacing=0 cellpadding=0>");
				htmlCode.append("<tr>");
				htmlCode.append("<td fixwidth=5></td>");
				htmlCode.append("<td FIXWIDTH=600 align=left></td>");
				htmlCode.append("<td fixwidth=5></td>");
				htmlCode.append("</tr>");
				htmlCode.append("</table>");
				htmlCode.append("<br>");
				htmlCode.append("<img src=\"L2UI.squaregray\" width=\"610\" height=\"1\">");
				htmlCode.append("<br><br><br></center>");
			}

			else if (command.equals("bbs_init_1_2_3"))
			{
				htmlCode.append("<center><br><br1><br1>");
				htmlCode.append("<table border=0 cellspacing=0 cellpadding=0>");
				htmlCode.append("<tr>");
				htmlCode.append("<td FIXWIDTH=15>&nbsp;</td>");
//				 action
				htmlCode.append("<td width=610 height=30 align=left><a action=\"bypass bbs_top\"> HOME </a>&nbsp;&gt; <a action=\"bypass bbs_init_1_2\"> Lineage II News </a>&nbsp;&gt; <a action=\"bypass bbs_init_1_2_3\"> Update Information </a></td>");
				htmlCode.append("</tr>");
				htmlCode.append("</table>");
				htmlCode.append("<table border=0 cellspacing=0 cellpadding=2 bgcolor=5A5A5A width=610>");
				htmlCode.append("<tr>");
				htmlCode.append("<td FIXWIDTH=60 align=center>&$416;</td>");
				htmlCode.append("<td FIXWIDTH=305 align=center>&$413;</td>");
				htmlCode.append("<td FIXWIDTH=120 align=center>&$417;</td>");
				htmlCode.append("<td FIXWIDTH=70 align=center>&$418;</td>");
				htmlCode.append("<td FIXWIDTH=55 align=center>&$419;</td>");
				htmlCode.append("</tr>");
				htmlCode.append("</table>");
				htmlCode.append("<br>");
				htmlCode.append("<table width=610 cellspace=0 cellpadding=0>");
				htmlCode.append("<tr>");
//				 action
				htmlCode.append("<td width=50><button value=\"&$422;\" action=\"bypass _bbslist_6_1_0\" back=\"l2ui_ch3.smallbutton2_down\" width=65 height=20 fore=\"l2ui_ch3.smallbutton2\"></td>");
				htmlCode.append("<td width=510 align=center>");
				htmlCode.append("<table cellspacing=2 cellpadding=0 border=0>");
				htmlCode.append("<tr>");
//				 action
				htmlCode.append("<td><button action=\"\" back=\"l2ui_ch3.prev1_down\" fore=\"l2ui_ch3.prev1\" width=16 height=16 ></td>");
				htmlCode.append("<td>1</td>");
//				 action
				htmlCode.append("<td><button action=\"\" back=\"l2ui_ch3.next1_down\" fore=\"l2ui_ch3.next1\" width=16 height=16 ></td>");
				htmlCode.append("</tr>");
				htmlCode.append("</table>");
				htmlCode.append("</td>");
//				 action
				htmlCode.append("<td align=right><button value=\"&$421;\" action=\"bypass _bbser2_6_1_0\" back=\"l2ui_ch3.smallbutton2_down\" width=65 height=20 fore=\"l2ui_ch3.smallbutton2\"></td>");
				htmlCode.append("</tr>");
				htmlCode.append("<tr>");
				htmlCode.append("<td><img src=\"l2ui.mini_logo\" width=5 height=10></td>");
				htmlCode.append("</tr>");
				htmlCode.append("<tr>");
				htmlCode.append("<td></td>");
				htmlCode.append("<td align=center><table border=0>");
				htmlCode.append("<tr>");
				htmlCode.append("<td><combobox width=65 var=keyword list=\"Title;Writer\"></td>");
				htmlCode.append("<td><edit var=\"Search\" width=130 height=11 length=\"16\"></td>");
//				 action
				htmlCode.append("<td><button value=\"&$420;\" action=\"Write 5 6 0 Search keyword keyword\" back=\"l2ui_ch3.smallbutton2_down\" width=65 height=20 fore=\"l2ui_ch3.smallbutton2\"></td>");
				htmlCode.append("</tr>");
				htmlCode.append("</table>");
				htmlCode.append("</td>");
				htmlCode.append("</tr>");
				htmlCode.append("</table>");
				htmlCode.append("<br><br><br></center>");
			}

			else if (command.equals("bbs_init_1_3"))
			{
				htmlCode.append("<center><br><br1><br1>");
				htmlCode.append("<table border=0 cellspacing=0 cellpadding=0>");
				htmlCode.append("<tr>");
				htmlCode.append("<td FIXWIDTH=15>&nbsp;</td>");
//				 action
				htmlCode.append("<td width=610 height=30 align=left><a action=\"bypass bbs_top\"> HOME </a>&nbsp;&gt; <a action=\"bypass bbs_init_1_3\"> CLAN COMMUNITY </a></td>");
				htmlCode.append("</tr>");
				htmlCode.append("</table>");
				htmlCode.append("<table border=0 cellspacing=0 cellpadding=0 width=610 bgcolor=434343>");
				htmlCode.append("<tr>");
				htmlCode.append("<td height=10></td>");
				htmlCode.append("</tr>");
				htmlCode.append("<tr>");
				htmlCode.append("<td fixWIDTH=5></td>");
//				 action
				htmlCode.append("<td fixWIDTH=600><a action=\"bypass _clbbsclan_0\">[GO TO MY CLAN]</a>&nbsp;&nbsp;</td>");
				htmlCode.append("<td fixWIDTH=5></td>");
				htmlCode.append("</tr>");
				htmlCode.append("<tr>");
				htmlCode.append("<td height=10></td>");
				htmlCode.append("</tr>");
				htmlCode.append("</table>");
				htmlCode.append("<br>");
				htmlCode.append("<table border=0 cellspacing=0 cellpadding=2 bgcolor=5A5A5A width=610>");
				htmlCode.append("<tr>");
				htmlCode.append("<td FIXWIDTH=5></td>");
				htmlCode.append("<td FIXWIDTH=200 align=center>CLAN NAME</td>");
				htmlCode.append("<td FIXWIDTH=200 align=center>CLAN LEADER</td>");
				htmlCode.append("<td FIXWIDTH=100 align=center>CLAN LEVEL</td>");
				htmlCode.append("<td FIXWIDTH=100 align=center>CLAN MEMBERS</td>");
				htmlCode.append("<td FIXWIDTH=5></td>");
				htmlCode.append("</tr>");
				htmlCode.append("</table>");
				htmlCode.append("<img src=\"L2UI.Squareblank\" width=\"1\" height=\"5\"><img src=\"L2UI.SquareBlank\" width=\"610\" height=\"3\">");
				htmlCode.append("<table border=0 cellspacing=0 cellpadding=0 width=610>");
				htmlCode.append("<tr>");
				htmlCode.append("<td FIXWIDTH=5></td>");
//				 action
				htmlCode.append("<td FIXWIDTH=200 align=center><a action=\"bypass _clbbsclan_1847\">L2J</a></td>");
				htmlCode.append("<td FIXWIDTH=200 align=center>TestUser</td>");
				htmlCode.append("<td FIXWIDTH=100 align=center>1</td>");
				htmlCode.append("<td FIXWIDTH=100 align=center>1</td>");
				htmlCode.append("<td FIXWIDTH=5></td>");
				htmlCode.append("</tr>");
				htmlCode.append("<tr>");
				htmlCode.append("<td height=5></td>");
				htmlCode.append("</tr>");
				htmlCode.append("</table>");
				htmlCode.append("<img src=\"L2UI.SquareBlank\" width=\"610\" height=\"3\"><img src=\"L2UI.SquareGray\" width=\"610\" height=\"1\"><img src=\"L2UI.SquareBlank\" width=\"610\" height=\"3\">");
				htmlCode.append("<table cellpadding=0 cellspacing=2 border=0>");
				htmlCode.append("<tr>");
//				 action
				htmlCode.append("<td><button action=\"\" back=\"l2ui_ch3.prev1_down\" fore=\"l2ui_ch3.prev1\" width=16 height=16></td>");
				htmlCode.append("<td>1</td>");
				htmlCode.append("<td><a action=\"bypass bbs_init_1_3\"> 2 </a></td>");
				htmlCode.append("<td><a action=\"bypass bbs_init_1_3\"> 3 </a></td>");
				htmlCode.append("<td><a action=\"bypass bbs_init_1_3\"> 4 </a></td>");
				htmlCode.append("<td><a action=\"bypass bbs_init_1_3\"> 5 </a></td>");
				htmlCode.append("<td><a action=\"bypass bbs_init_1_3\"> 6 </a></td>");
				htmlCode.append("<td><a action=\"bypass bbs_init_1_3\"> 7 </a></td>");
				htmlCode.append("<td><a action=\"bypass bbs_init_1_3\"> 8 </a></td>");
				htmlCode.append("<td><a action=\"bypass bbs_init_1_3\"> 9 </a></td>");
				htmlCode.append("<td><a action=\"bypass bbs_init_1_3\"> 10 </a></td>");
//				 action
				htmlCode.append("<td><button action=\"bypass _clbbslist_2_\" back=\"l2ui_ch3.next1_down\" fore=\"l2ui_ch3.next1\" width=16 height=16 ></td>");
				htmlCode.append("</tr>");
				htmlCode.append("</table>");
				htmlCode.append("<table border=0 cellspacing=0 cellpadding=0>");
				htmlCode.append("<tr>");
				htmlCode.append("<td width=610><img src=\"sek.cbui141\" width=\"610\" height=\"1\"></td>");
				htmlCode.append("</tr>");
				htmlCode.append("</table>");
				htmlCode.append("<br><br></center>");
			}
			
			else if (command.equals("bbs_clan"))
			{
				htmlCode.append("<table border=0><tr><td FIXWIDTH=15></td><td align=center>L2J Community Board<img src=\"sek.cbui355\" width=610 height=1></td></tr><tr><td FIXWIDTH=10></td><td>");

				L2Clan clan = activeChar.getClan();
				htmlCode.append("<table border=0><tr><td>"+clan.getName()+" (Level "+clan.getLevel()+"):</td></tr><tr><td><table border=0>");
				String title = "";
				if (!clan.getClanMember(clan.getLeaderName()).getTitle().equals(""))
				{
					title = "<td>["+clan.getClanMember(clan.getLeaderName()).getTitle()+"]</td><td FIXWIDTH=5></td>";
				}
				String name = clan.getLeaderName();
				if (clan.getClanMember(clan.getLeaderName()).isOnline())
				{
					name = "<a action=\"bypass bbs_player_info "+clan.getLeaderName()+"\">"+clan.getLeaderName()+"</a>";
				}
				htmlCode.append("<tr>"+title+"<td>"+name+"</td></tr>");
				htmlCode.append("<tr><td><br></td></tr>");
				
				L2ClanMember[] members = clan.getMembers();
				for (int i = 0; i < members.length; i++)
				{
					if (members[i].getName() != clan.getLeaderName())
					{
						title = "";
						if (!members[i].getTitle().equals(""))
						{
							title = "<td>["+members[i].getTitle()+"]</td><td FIXWIDTH=5></td>";
						}
						name = members[i].getName();
						if (members[i].isOnline())
						{
							name = "<a action=\"bypass bbs_player_info "+members[i].getName()+"\">"+members[i].getName()+"</a>";
						}
						htmlCode.append("<tr>"+title+"<td>"+name+"</td></tr>");
					}
				}
				htmlCode.append("</table></td></tr></table>");
				htmlCode.append("</td></tr></table>");
			}

			else if (command.equals("bbs_friends"))
			{
				htmlCode.append("<table border=0><tr><td FIXWIDTH=15></td><td align=center>L2J Community Board<img src=\"sek.cbui355\" width=610 height=1></td></tr><tr><td width=10></td><td width=600 align=left>");
				htmlCode.append("<a action=\"bypass bbs_top\">Home</a>&gt; <a action=\"bypass bbs_friends\">Friends</a></td></tr></table>");
				htmlCode.append("<center><img src=\"L2UI.SquareBlank\" width=\"610\" height=\"3\"><img src=\"L2UI.SquareGray\" width=\"610\" height=\"1\"><img src=\"L2UI.SquareBlank\" width=\"610\" height=\"3\"><br>");

				htmlCode.append("<table border=0 cellspacing=0 cellpadding=0 width=610 bgcolor=555555><tr><td width=5></td><td height=10 width=605></td></tr><tr><td></td><td height=20>");
				htmlCode.append("<a action = \"bypass bbs_friends\">[Friend List]</a>  <a action = \"bypass bbs_friendblock\">[Block List]</a></td></tr><tr><td></td><td height=10></td></tr></table><br>");
				htmlCode.append("Friend List");
				htmlCode.append("<img src=\"L2UI.SquareGray\" width=\"610\" height=\"1\"><br>");
				htmlCode.append("<table border=0 cellspacing=0 cellpadding=0 width=610 bgcolor=555555><tr><td height=10>");
        	    java.sql.Connection con = null;
        	    try
        	    {
            		con = L2DatabaseFactory.getInstance().getConnection();
            		PreparedStatement statement = con.prepareStatement("select char_id,friend_name from character_friends where char_id=? or friend_name=?");
            		statement.setInt(1, activeChar.getObjectId());
            		statement.setString(2, activeChar.getName());
            		ResultSet rset = statement.executeQuery();
            		while (rset.next())
                    {
            		    int objectId = rset.getInt("char_id");
            		    String friendName = rset.getString("friend_name");
            		    L2PcInstance friend;
            		    if (activeChar.getObjectId() != objectId)
                        {
                			friend = (L2PcInstance) L2World.getInstance().findObject(objectId);
                			if (friend != null)
                			    friendName = friend.getName();
                			else 
                            {
                			    PreparedStatement statement2 = con.prepareStatement("select char_name from characters where obj_id=?");
                			    statement2.setInt(1, objectId);
                			    ResultSet rset2 = statement2.executeQuery();
                			    if (rset2.next())
                				friendName=rset2.getString("char_name");
                			    statement2.close();
                			}
            		    }
                        else
                            friend = L2World.getInstance().getPlayer(friendName);
            		    if (friend == null)
                        {
                			htmlCode.append("<a action=\"bypass friendlist\">"+friendName+"</a>(Off)&nbsp;");
                			//(Currently: Offline)
            		    }
                        else
                        {
                			//(Currently: Online)
                			htmlCode.append("<a action=\"bypass friendlist\">"+friendName+"</a>(On)&nbsp;");
            		    }
            		}
            		rset.close();
            		statement.close();
        	    }
        	    catch (Exception e)
        	    {
        	        _log.warning("Error in friendlist: "+e);
        	    }
        	    finally
        	    {
        	        try {con.close();} catch (Exception e) {}
        	    }

				htmlCode.append("</td></tr></table><img src=\"L2UI.SquareGray\" width=\"610\" height=\"1\"><br><br>");
				htmlCode.append("<font color=\"999999\">Selection List</font>");
				htmlCode.append("<img src=\"L2UI.SquareGray\" width=\"610\" height=\"1\"><br>");
				htmlCode.append("<table border=0 cellspacing=0 cellpadding=0 width=610><tr><td></td></tr></table><br>");
				htmlCode.append("<img src=\"L2UI.SquareGray\" width=\"610\" height=\"1\"><br>");
				htmlCode.append("<table fixwidth=610 border=0 cellspacing=0 cellpadding=0><tr><td height=10></td></tr><tr><td width=5></td><td><td align=center width=390></td>");
				htmlCode.append("<td align=center width=70><button value=\"Delete\" action=\"bypass frienddelete\" back=\"l2ui_ch3.smallbutton2_down\" width=65 height=20 fore=\"l2ui_ch3.smallbutton2\"></td>");
				htmlCode.append("<td align=center width=70><button value=\"Delete All\" action=\"bypass frienddeleteall\" back=\"l2ui_ch3.smallbutton2_down\" width=65 height=20 fore=\"l2ui_ch3.smallbutton2\"></td>");
				htmlCode.append("<td align=center width=70><button value=\"Write Mail\" action=\"bypass friendmail\" back=\"l2ui_ch3.smallbutton2_down\" width=65 height=20 fore=\"l2ui_ch3.smallbutton2\"></td>");
				htmlCode.append("<td width=5></td></tr><tr><td height=10>");

				htmlCode.append("</td></tr></table>");
			}
			
			else if (command.equals("bbs_memo"))
			{
				htmlCode.append("<center><br><br1><br1>");
				htmlCode.append("<table border=0 cellspacing=0 cellpadding=0>");
				htmlCode.append("<tr>");
				htmlCode.append("<td FIXWIDTH=15>&nbsp;</td>");
//				 action
				htmlCode.append("<td width=610 height=30 align=left><a action=\"bypass bbs_top\">HOME</a>&nbsp;>&nbsp;<a action=\"bypass bbs_memo\">Memo Form</a></td>");
				htmlCode.append("</tr>");
				htmlCode.append("</table>");
				htmlCode.append("<table border=0 cellspacing=0 cellpadding=2 bgcolor=888888 width=610>");
				htmlCode.append("<tr>");
				htmlCode.append("<td FIXWIDTH=5></td>");
				htmlCode.append("<td FIXWIDTH=415 align=center>&$413;</td>");
				htmlCode.append("<td FIXWIDTH=120 align=center></td>");
				htmlCode.append("<td FIXWIDTH=70 align=center>&$418;</td>");
				htmlCode.append("</tr>");
				htmlCode.append("</table>");
				htmlCode.append("<br>");
				htmlCode.append("<table width=610 cellspace=0 cellpadding=0>");
				htmlCode.append("<tr>");
//				 action
				htmlCode.append("<td width=50><button value=\"&$422;\" action=\"bypass _mmlist_1_\" back=\"l2ui_ch3.smallbutton2_down\" width=65 height=20 fore=\"l2ui_ch3.smallbutton2\"></td>");
				htmlCode.append("<td width=510 align=center>");
				htmlCode.append("<table border=0>");
				htmlCode.append("<tr>");
//				 action
				htmlCode.append("<td><button action=\"\" back=\"l2ui_ch3.prev1_down\" fore=\"l2ui_ch3.prev1\" width=16 height=16></td>");
				htmlCode.append("<td>1</td>");
//				 action		
				htmlCode.append("<td><button action=\"bypass _mmlist_2_\" back=\"l2ui_ch3.next1_down\" fore=\"l2ui_ch3.next1\" width=16 height=16></td>");
				htmlCode.append("</tr>");
				htmlCode.append("</table>");
				htmlCode.append("</td>");
//				 action
				htmlCode.append("<td align=right><button value=\"&$421;\" action=\"bypass _mmcrea\" back=\"l2ui_ch3.smallbutton2_down\" width=65 height=20 fore=\"l2ui_ch3.smallbutton2\"></td>");
				htmlCode.append("</tr>");
				htmlCode.append("<tr>");
				htmlCode.append("<td><img src=\"l2ui.mini_logo\" width=5 height=10></td>");
				htmlCode.append("</tr>");
				htmlCode.append("<tr>");
				htmlCode.append("<td></td>");
				htmlCode.append("<td align=center><table border=0><tr><td></td>");
				htmlCode.append("<td><edit var=\"Search\" width=130 height=11></td>");
//				 action
				htmlCode.append("<td><button value=\"&$420;\" action=\"Write 5 -2 0 Search _ _\" back=\"l2ui_ch3.smallbutton2_down\" width=65 height=20 fore=\"l2ui_ch3.smallbutton2\"></td>");
				htmlCode.append("</tr>");
				htmlCode.append("</table>");
				htmlCode.append("</td>");
				htmlCode.append("</tr>");
				htmlCode.append("</table>");
				htmlCode.append("<br><br><br></center>");
			}

			else if (command.equals("bbs_mail")) // mail inbox
			{
				htmlCode.append("<center><br><br1><br1>");
				htmlCode.append("<table border=0 cellspacing=0 cellpadding=0>");
				htmlCode.append("<tr>");
				htmlCode.append("<td FIXWIDTH=15>&nbsp;</td>");
//				 action
				htmlCode.append("<td width=610 height=30 align=left><a action=\"bypass bbs_top\"> HOME </a>&nbsp;&gt;<a action=\"bypass bbs_mail\">&$917;</a></td>");
				htmlCode.append("</tr>");
				htmlCode.append("</table>");
				htmlCode.append("<table border=0 cellspacing=0 cellpadding=0 width=610 bgcolor=555555>");
				htmlCode.append("<tr>");
				htmlCode.append("<td WIDTH=5></td>");
				htmlCode.append("<td height=10 WIDTH=605></td>");
				htmlCode.append("</tr>");
				htmlCode.append("<tr>");
				htmlCode.append("<td></td>");
//				 action
				htmlCode.append("<td height=20><a action=\"bypass bbs_mail\">[&amp;$917;]</a>(0)&nbsp; <a action=\"bypass bbs_init_2_1\">[&amp;$918;]</a>(0)&nbsp; <a action=\"bypass bbs_init_2_2\">[&amp;$919;]</a>(0)&nbsp; <a action=\"bypass bbs_init_2_3\">[&amp;$920;]</a>(0)</td>");
				htmlCode.append("</tr>");
				htmlCode.append("<tr>");
				htmlCode.append("<td></td>");
				htmlCode.append("<td height=10></td>");
				htmlCode.append("</tr>");
				htmlCode.append("</table>");
				htmlCode.append("<br>");
				htmlCode.append("<table border=0 cellspacing=0 cellpadding=2 bgcolor=5A5A5A width=610>");
				htmlCode.append("<tr>");
				htmlCode.append("<td FIXWIDTH=5 align=center></td>");
				htmlCode.append("<td FIXWIDTH=120 align=center>&$911;</td>");
				htmlCode.append("<td FIXWIDTH=410>&amp;$413;</td>");
				htmlCode.append("<td FIXWIDTH=70 align=center>&$910;</td>");
				htmlCode.append("<td FIXWIDTH=5 align=center></td>");
				htmlCode.append("</tr>");
				htmlCode.append("</table>");
				htmlCode.append("<br>");
				htmlCode.append("<table width=610 cellspace=0 cellpadding=0>");
				htmlCode.append("<tr>");
				htmlCode.append("<td width=50></td>");
				htmlCode.append("<td width=510 align=center>");
//				 action
				htmlCode.append("<table cellspacing=2 cellpadding=0 border=0><tr><td><button action=\"\" back=\"l2ui_ch3.prev1_down\" fore=\"l2ui_ch3.prev1\" width=16 height=16 ></td>");
				htmlCode.append("<td>1</td>");
//				 action
				htmlCode.append("<td><button action=\"\" back=\"l2ui_ch3.next1_down\" fore=\"l2ui_ch3.next1\" width=16 height=16 ></td>");
				htmlCode.append("</tr>");
				htmlCode.append("</table>");
				htmlCode.append("</td>");
//				 action
				htmlCode.append("<td align=right><button value=\"&$421;\" action=\"bypass bbs_init_2_4_1\" back=\"l2ui_ch3.smallbutton2_down\" width=65 height=20 fore=\"l2ui_ch3.smallbutton2\"></td>");
				htmlCode.append("</tr>");
				htmlCode.append("<tr>");
				htmlCode.append("<td><img src=\"l2ui.mini_logo\" width=5 height=10></td>");
				htmlCode.append("</tr>");
				htmlCode.append("<tr>");
				htmlCode.append("<td></td>");
				htmlCode.append("<td align=center><table border=0>");
				htmlCode.append("<tr>");
				htmlCode.append("<td><combobox width=65 var=SearchTarget list=\"Writer;Title\"></td>");
				htmlCode.append("<td><edit var=\"Keyword\" width=130 height=11 length=\"16\"></td>");
//				 action
				htmlCode.append("<td><button value=\"&$420;\" action=\"Write _mailsearch_0__ 0 0 SearchTarget SearchTarget Keyword\" back=\"l2ui_ch3.smallbutton2_down\" width=65 height=20 fore=\"l2ui_ch3.smallbutton2\"></td>");
				htmlCode.append("</tr>");
				htmlCode.append("</table>");
				htmlCode.append("</td>");
				htmlCode.append("</tr>");
				htmlCode.append("</table>");
				htmlCode.append("<br><br><br></center>");
			}

			else if (command.equals("bbs_init_2_1")) // mail Sent box
			{
				htmlCode.append("<center><br><br1><br1>");
				htmlCode.append("<table border=0 cellspacing=0 cellpadding=0>");
				htmlCode.append("<tr>");
				htmlCode.append("<td FIXWIDTH=15>&nbsp;</td>");
//				 action
				htmlCode.append("<td width=610 height=30 align=left><a action=\"bypass bbs_top\"> HOME </a>&nbsp;&gt;<a action=\"bypass bbs_init_2_1\">&$918;</a></td>");
				htmlCode.append("</tr>");
				htmlCode.append("</table>");
				htmlCode.append("<table border=0 cellspacing=0 cellpadding=0 width=610 bgcolor=555555>");
				htmlCode.append("<tr>");
				htmlCode.append("<td WIDTH=5></td>");
				htmlCode.append("<td height=10 WIDTH=605></td>");
				htmlCode.append("</tr>");
				htmlCode.append("<tr>");
				htmlCode.append("<td></td>");
//				 action
				htmlCode.append("<td height=20><a action=\"bypass bbs_mail\">[&amp;$917;]</a>(0)&nbsp; <a action=\"bypass bbs_init_2_1\">[&amp;$918;]</a>(0)&nbsp; <a action=\"bypass bbs_init_2_2\">[&amp;$919;]</a>(0)&nbsp; <a action=\"bypass bbs_init_2_3\">[&amp;$920;]</a>(0)</td>");
				htmlCode.append("</tr>");
				htmlCode.append("<tr>");
				htmlCode.append("<td></td>");
				htmlCode.append("<td height=10></td>");
				htmlCode.append("</tr>");
				htmlCode.append("</table>");
				htmlCode.append("<br>");
				htmlCode.append("<table border=0 cellspacing=0 cellpadding=2 bgcolor=5A5A5A width=610>");
				htmlCode.append("<tr>");
				htmlCode.append("<td FIXWIDTH=5 align=center></td>");
				htmlCode.append("<td FIXWIDTH=120 align=center>&$909;</td>");
				htmlCode.append("<td FIXWIDTH=410>&amp;$413;</td>");
				htmlCode.append("<td FIXWIDTH=70 align=center>&$910;</td>");
				htmlCode.append("<td FIXWIDTH=5 align=center></td>");
				htmlCode.append("</tr>");
				htmlCode.append("</table>");
				htmlCode.append("<br>");
				htmlCode.append("<table width=610 cellspace=0 cellpadding=0>");
				htmlCode.append("<tr>");
				htmlCode.append("<td width=50></td>");
				htmlCode.append("<td width=510 align=center>");
				htmlCode.append("<table cellspacing=2 cellpadding=0 border=0>");
				htmlCode.append("<tr>");
//				 action
				htmlCode.append("<td><button action=\"\" back=\"l2ui_ch3.prev1_down\" fore=\"l2ui_ch3.prev1\" width=16 height=16></td>");
				htmlCode.append("<td>1</td>");
//				 action
				htmlCode.append("<td><button action=\"\" back=\"l2ui_ch3.next1_down\" fore=\"l2ui_ch3.next1\" width=16 height=16></td>");
				htmlCode.append("</tr>");
				htmlCode.append("</table>");
				htmlCode.append("</td>");
//				 action
				htmlCode.append("<td align=right><button value=\"&$421;\" action=\"bypass bbs_init_2_4_2\" back=\"l2ui_ch3.smallbutton2_down\" width=65 height=20 fore=\"l2ui_ch3.smallbutton2\"></td>");
				htmlCode.append("</tr>");
				htmlCode.append("<tr>");
				htmlCode.append("<td><img src=\"l2ui.mini_logo\" width=5 height=10></td>");
				htmlCode.append("</tr>");
				htmlCode.append("<tr>");
				htmlCode.append("<td></td>");
				htmlCode.append("<td align=center>");
				htmlCode.append("<table border=0>");
				htmlCode.append("<tr>");
				htmlCode.append("<td><combobox width=65 var=SearchTarget list=\"Writer;Title\"></td>");
				htmlCode.append("<td><edit var=\"Keyword\" width=130 height=11 length=\"16\"></td>");
//				 action
				htmlCode.append("<td><button value=\"&$420;\" action=\"Write _mailsearch_0__ 0 0 SearchTarget SearchTarget Keyword\" back=\"l2ui_ch3.smallbutton2_down\" width=65 height=20 fore=\"l2ui_ch3.smallbutton2\"></td>");
				htmlCode.append("</tr>");
				htmlCode.append("</table>");
				htmlCode.append("</td>");
				htmlCode.append("</tr>");
				htmlCode.append("</table>");
				htmlCode.append("<br><br><br></center>");
			}

			else if (command.equals("bbs_init_2_2")) // mail archive
			{
				htmlCode.append("<center><br><br1><br1>");
				htmlCode.append("<table border=0 cellspacing=0 cellpadding=0>");
				htmlCode.append("<tr>");
				htmlCode.append("<td FIXWIDTH=15>&nbsp;</td>");
//				 action
				htmlCode.append("<td width=610 height=30 align=left><a action=\"bypass bbs_top\"> HOME </a>&nbsp;&gt;<a action=\"bypass bbs_init_2_2\">&$919;</a></td>");
				htmlCode.append("</tr>");
				htmlCode.append("</table>");
				htmlCode.append("<table border=0 cellspacing=0 cellpadding=0 width=610 bgcolor=555555>");
				htmlCode.append("<tr>");
				htmlCode.append("<td WIDTH=5></td>");
				htmlCode.append("<td height=10 WIDTH=605></td>");
				htmlCode.append("</tr>");
				htmlCode.append("<tr>");
				htmlCode.append("<td></td>");
//				 action
				htmlCode.append("<td height=20><a action=\"bypass bbs_mail\">[&amp;$917;]</a>(0)&nbsp; <a action=\"bypass bbs_init_2_1\">[&amp;$918;]</a>(0)&nbsp; <a action=\"bypass bbs_init_2_2\">[&amp;$919;]</a>(0)&nbsp; <a action=\"bypass bbs_init_2_3\">[&amp;$920;]</a>(0)</td>");
				htmlCode.append("</tr>");
				htmlCode.append("<tr>");
				htmlCode.append("<td></td>");
				htmlCode.append("<td height=10></td>");
				htmlCode.append("</tr>");
				htmlCode.append("</table>");
				htmlCode.append("<br>");
				htmlCode.append("<table border=0 cellspacing=0 cellpadding=2 bgcolor=5A5A5A width=610>");
				htmlCode.append("<tr>");
				htmlCode.append("<td FIXWIDTH=5 align=center></td>");
				htmlCode.append("<td FIXWIDTH=120 align=center>&$911;</td>");
				htmlCode.append("<td FIXWIDTH=410>&amp;$413;</td>");
				htmlCode.append("<td FIXWIDTH=70 align=center>&$910;</td>");
				htmlCode.append("<td FIXWIDTH=5 align=center></td>");
				htmlCode.append("</tr>");
				htmlCode.append("</table>");
				htmlCode.append("<br>");
				htmlCode.append("<table width=610 cellspace=0 cellpadding=0>");
				htmlCode.append("<tr>");
				htmlCode.append("<td width=50></td>");
				htmlCode.append("<td width=510 align=center>");
//				 action
				htmlCode.append("<table cellspacing=2 cellpadding=0 border=0><tr><td><button action=\"\" back=\"l2ui_ch3.prev1_down\" fore=\"l2ui_ch3.prev1\" width=16 height=16 ></td>");
				htmlCode.append("<td>1</td>");
//				 action
				htmlCode.append("<td><button action=\"\" back=\"l2ui_ch3.next1_down\" fore=\"l2ui_ch3.next1\" width=16 height=16 ></td>");
				htmlCode.append("</tr>");
				htmlCode.append("</table>");
				htmlCode.append("</td>");
//				 action
				htmlCode.append("<td align=right><button value=\"&$421;\" action=\"bypass bbs_init_2_4_3\" back=\"l2ui_ch3.smallbutton2_down\" width=65 height=20 fore=\"l2ui_ch3.smallbutton2\"></td>");
				htmlCode.append("</tr>");
				htmlCode.append("<tr>");
				htmlCode.append("<td><img src=\"l2ui.mini_logo\" width=5 height=10></td>");
				htmlCode.append("</tr>");
				htmlCode.append("<tr>");
				htmlCode.append("<td></td>");
				htmlCode.append("<td align=center><table border=0>");
				htmlCode.append("<tr>");
				htmlCode.append("<td><combobox width=65 var=SearchTarget list=\"Writer;Title\"></td>");
				htmlCode.append("<td><edit var=\"Keyword\" width=130 height=11 length=\"16\"></td>");
//				 action
				htmlCode.append("<td><button value=\"&$420;\" action=\"Write _mailsearch_0__ 0 0 SearchTarget SearchTarget Keyword\" back=\"l2ui_ch3.smallbutton2_down\" width=65 height=20 fore=\"l2ui_ch3.smallbutton2\"></td>");
				htmlCode.append("</tr>");
				htmlCode.append("</table>");
				htmlCode.append("</td>");
				htmlCode.append("</tr>");
				htmlCode.append("</table>");
				htmlCode.append("<br><br><br></center>");
			}

			else if (command.equals("bbs_init_2_3")) // temporary mail archive
			{
				htmlCode.append("<center><br><br1><br1>");
				htmlCode.append("<table border=0 cellspacing=0 cellpadding=0>");
				htmlCode.append("<tr>");
				htmlCode.append("<td FIXWIDTH=15>&nbsp;</td>");
//				 action
				htmlCode.append("<td width=610 height=30 align=left><a action=\"bypass bbs_top\"> HOME </a>&nbsp;&gt;<a action=\"bypass bbs_init_2_3\">&$920;</a></td>");
				htmlCode.append("</tr>");
				htmlCode.append("</table>");
				htmlCode.append("<table border=0 cellspacing=0 cellpadding=0 width=610 bgcolor=555555>");
				htmlCode.append("<tr>");
				htmlCode.append("<td WIDTH=5></td>");
				htmlCode.append("<td height=10 WIDTH=605></td>");
				htmlCode.append("</tr>");
				htmlCode.append("<tr>");
				htmlCode.append("<td></td>");
//				 action
				htmlCode.append("<td height=20><a action=\"bypass bbs_mail\">[&amp;$917;]</a>(0)&nbsp; <a action=\"bypass bbs_init_2_1\">[&amp;$918;]</a>(0)&nbsp; <a action=\"bypass bbs_init_2_2\">[&amp;$919;]</a>(0)&nbsp; <a action=\"bypass bbs_init_2_3\">[&amp;$920;]</a>(0)</td>");
				htmlCode.append("</tr>");
				htmlCode.append("<tr>");
				htmlCode.append("<td></td>");
				htmlCode.append("<td height=10></td>");
				htmlCode.append("</tr>");
				htmlCode.append("</table>");
				htmlCode.append("<br>");
				htmlCode.append("<table border=0 cellspacing=0 cellpadding=2 bgcolor=5A5A5A width=610>");
				htmlCode.append("<tr>");
				htmlCode.append("<td FIXWIDTH=5 align=center></td>");
				htmlCode.append("<td FIXWIDTH=120 align=center>&$909;</td>");
				htmlCode.append("<td FIXWIDTH=410>&amp;$413;</td>");
				htmlCode.append("<td FIXWIDTH=70 align=center>&$910;</td>");
				htmlCode.append("<td FIXWIDTH=5 align=center></td>");
				htmlCode.append("</tr>");
				htmlCode.append("</table>");
				htmlCode.append("<br>");
				htmlCode.append("<table width=610 cellspace=0 cellpadding=0>");
				htmlCode.append("<tr>");
				htmlCode.append("<td width=50></td>");
				htmlCode.append("<td width=510 align=center>");
				htmlCode.append("<table cellspacing=2 cellpadding=0 border=0>");
				htmlCode.append("<tr>");
//				 action
				htmlCode.append("<td><button action=\"\" back=\"l2ui_ch3.prev1_down\" fore=\"l2ui_ch3.prev1\" width=16 height=16></td>");
				htmlCode.append("<td>1</td>");
//				 action
				htmlCode.append("<td><button action=\"\" back=\"l2ui_ch3.next1_down\" fore=\"l2ui_ch3.next1\" width=16 height=16></td>");
				htmlCode.append("</tr>");
				htmlCode.append("</table>");
				htmlCode.append("</td>");
//				 action
				htmlCode.append("<td align=right><button value=\"&$421;\" action=\"bypass bbs_init_2_4_4\" back=\"l2ui_ch3.smallbutton2_down\" width=65 height=20 fore=\"l2ui_ch3.smallbutton2\"></td>");
				htmlCode.append("</tr>");
				htmlCode.append("<tr>");
				htmlCode.append("<td><img src=\"l2ui.mini_logo\" width=5 height=10></td>");
				htmlCode.append("</tr>");
				htmlCode.append("<tr>");
				htmlCode.append("<td></td>");
				htmlCode.append("<td align=center>");
				htmlCode.append("<table border=0>");
				htmlCode.append("<tr>");
				htmlCode.append("<td><combobox width=65 var=SearchTarget list=\"Writer;Title\"></td>");
				htmlCode.append("<td><edit var=\"Keyword\" width=130 height=11 length=\"16\"></td>");
//				 action
				htmlCode.append("<td><button value=\"&$420;\" action=\"Write _mailsearch_0__ 0 0 SearchTarget SearchTarget Keyword\" back=\"l2ui_ch3.smallbutton2_down\" width=65 height=20 fore=\"l2ui_ch3.smallbutton2\"></td>");
				htmlCode.append("</tr>");
				htmlCode.append("</table>");
				htmlCode.append("</td>");
				htmlCode.append("</tr>");
				htmlCode.append("</table>");
				htmlCode.append("<br><br><br></center>");
			}

			else if (command.startsWith("bbs_init_2_4")) // write mail form
			{
				htmlCode.append("<br><br1><br1>");
				htmlCode.append("<table border=0 cellspacing=0 cellpadding=0>");
				htmlCode.append("<tr>");
				htmlCode.append("<td FIXWIDTH=15>&nbsp;</td>");
				
				if (command.equals("bbs_init_2_4_1"))
				{
					htmlCode.append("<td width=610 height=30 align=left><a action=\"bypass bbs_top\"> HOME </a>&nbsp;&gt;<a action=\"bypass bbs_mail\">&$917;</a></td>");
				}
				
				else if (command.equals("bbs_init_2_4_2"))
				{
					htmlCode.append("<td width=610 height=30 align=left><a action=\"bypass bbs_top\"> HOME </a>&nbsp;&gt;<a action=\"bypass bbs_init_2_1\">&$918;</a></td>");
				}
				
				else if (command.equals("bbs_init_2_4_3"))
				{
					htmlCode.append("<td width=610 height=30 align=left><a action=\"bypass bbs_top\"> HOME </a>&nbsp;&gt;<a action=\"bypass bbs_init_2_2\">&$919;</a></td>");
				}
				
				else if (command.equals("bbs_init_2_4_4"))
				{
					htmlCode.append("<td width=610 height=30 align=left><a action=\"bypass bbs_top\"> HOME </a>&nbsp;&gt;<a action=\"bypass bbs_init_2_3\">&$920;</a></td>");
				}
				
				htmlCode.append("</tr>");
				htmlCode.append("</table>");
				htmlCode.append("<table border=0 cellspacing=0 cellpadding=0>");
				htmlCode.append("<tr>");
				htmlCode.append("<td width=610><img src=\"L2UI.SquareGray\" width=\"610\" height=\"1\"></td>");
				htmlCode.append("</tr>");
				htmlCode.append("</table>");
				htmlCode.append("<table fixwidth=610 border=0 cellspacing=0 cellpadding=0>");
				htmlCode.append("<tr>");
				htmlCode.append("<td><img src=\"l2ui.mini_logo\" width=5 height=20></td>");
				htmlCode.append("</tr>");
				htmlCode.append("<tr>");
				htmlCode.append("<td width=5></td>");
				htmlCode.append("<td align=center FIXWIDTH=50 height=29>To:</td>");
				htmlCode.append("<td FIXWIDTH=550><edit var=\"ToList\" width=550 height=13 length=\"128\"></td>");
				htmlCode.append("<td width=5></td>");
				htmlCode.append("</tr>");
				htmlCode.append("<tr>");
				htmlCode.append("<td></td>");
				htmlCode.append("<td align=center height=29>&amp;$413;</td>");
				htmlCode.append("<td><edit var=\"Title\" width=550 height=13 length=\"128\"></td>");
				htmlCode.append("<td></td>");
				htmlCode.append("</tr>");
				htmlCode.append("</table>");
				htmlCode.append("<table fixwidth=610 border=0 cellspacing=0 cellpadding=0>");
				htmlCode.append("<tr>");
				htmlCode.append("<td width=5></td>");
				htmlCode.append("<td align=center FIXWIDTH=50 height=29 valign=top>&amp;$427;</td>");
				htmlCode.append("<td FIXWIDTH=550><MultiEdit var=\"Content\" width=550 height=313></td>");
				htmlCode.append("<td width=5></td>");
				htmlCode.append("</tr>");
				htmlCode.append("<tr>");
				htmlCode.append("<td width=5 height=10></td>");
				htmlCode.append("</tr>");
				htmlCode.append("</table>");
				htmlCode.append("<table fixwidth=610 border=0 cellspacing=0 cellpadding=0>");
				htmlCode.append("<tr>");
				htmlCode.append("<td height=10></td>");
				htmlCode.append("</tr>");
				htmlCode.append("<tr>");
				htmlCode.append("<td width=5></td>");
				htmlCode.append("<td align=center FIXWIDTH=50 height=29>&nbsp;</td>");
				htmlCode.append("<td align=center FIXWIDTH=70><button value=\"Forward\" action=\"Write _mailsend_0_0_3_1_0__ 0 0 ToList Title Content\" back=\"l2ui_ch3.smallbutton2_down\" width=65 height=20 fore=\"l2ui_ch3.smallbutton2\"></td>");
				htmlCode.append("<td align=center FIXWIDTH=70><button value=\"&$141;\" action=\"bypass _maillist_3_1_0_\" back=\"l2ui_ch3.smallbutton2_down\" width=65 height=20 fore=\"l2ui_ch3.smallbutton2\"></td>");
				htmlCode.append("<td align=center FIXWIDTH=70><button value=\"Save\" action=\"Write _mailsave_0_0_3_1_0__ 0 0 ToList Title Content\" back=\"l2ui_ch3.smallbutton2_down\" width=65 height=20 fore=\"l2ui_ch3.smallbutton2\"></td>");
				htmlCode.append("<td align=center FIXWIDTH=340>&nbsp;</td>");
				htmlCode.append("<td width=5></td>");
				htmlCode.append("</tr>");
				htmlCode.append("</table>");
			}

			//EXTRAS
			else if (command.startsWith("bbs_player_info"))
			{
				htmlCode.append("<table border=0><tr><td FIXWIDTH=15></td><td align=center>L2J Community Board<img src=\"sek.cbui355\" width=610 height=1></td></tr><tr><td FIXWIDTH=15></td><td>");

				String name = command.substring(16);
				L2PcInstance player = L2World.getInstance().getPlayer(name);
				
				if (player != null)
				{
				    String sex = "Male";
				    if (player.getSex() == 1)
				    {
				        sex = "Female";
				    }
				    String levelApprox = "low";
				    if (player.getLevel() >= 60)
				        levelApprox = "very high";
				    else if (player.getLevel() >= 40)
				        levelApprox = "high";
				    else if (player.getLevel() >= 20)
				        levelApprox = "medium";
				    htmlCode.append("<table border=0><tr><td>"+player.getName()+" ("+sex+" "+player.getTemplate().className+"):</td></tr>");
				    htmlCode.append("<tr><td>Level: "+levelApprox+"</td></tr>");
				    htmlCode.append("<tr><td><br></td></tr>");
				    
				    //Dont need it but for fun only
				    String plyrStatus = "Normal Player";
				    if (player.isGM())
				    	plyrStatus = "<font color=\"LEVEL\">Game Master</font>";
				    else if (player.getAccessLevel() == Config.VIP_ACCESSLEVEL)
				    	plyrStatus = "<font color=\"LEVEL\">V.I.P Player</font>";
				    else if (player.isHero())
				    	plyrStatus = "<font color=\"LEVEL\">Hero Player</font>";
				    htmlCode.append("<tr><td>Status: "+plyrStatus+"</td></tr>");
				    htmlCode.append("<tr><td><br></td></tr>");
				    
				    if (activeChar != null && (activeChar.isGM() || player.getObjectId() == activeChar.getObjectId()
				            || Config.SHOW_LEVEL_COMMUNITYBOARD))
				    {
				        int nextLevelExp = 0;
				        int nextLevelExpNeeded = 0;
				        if (player.getLevel() < Config.MAX_LEVEL)
				        {
				            nextLevelExp = Experience.LEVEL[player.getLevel() + 1];
				            nextLevelExpNeeded = nextLevelExp-player.getExp();
				        }
				        
				        htmlCode.append("<tr><td>Level: "+player.getLevel()+"</td></tr>");
				        htmlCode.append("<tr><td>Experience: "+player.getExp()+"/"+nextLevelExp+"</td></tr>");
				        htmlCode.append("<tr><td>Experience needed for level up: "+nextLevelExpNeeded+"</td></tr>");
				        htmlCode.append("<tr><td><br></td></tr>");
				    }
				    
				    int uptime = (int)player.getUptime()/1000;
				    int h = uptime/3600;
				    int m = (uptime-(h*3600))/60;
				    int s = ((uptime-(h*3600))-(m*60));
				    
				    htmlCode.append("<tr><td>Uptime: "+h+"h "+m+"m "+s+"s</td></tr>");
				    htmlCode.append("<tr><td><br></td></tr>");
				    
				    if (player.getClan() != null)
				    {
				        htmlCode.append("<tr><td>Clan: "+player.getClan().getName()+"</td></tr>");
				        htmlCode.append("<tr><td><br></td></tr>");
				    }
				    
				    htmlCode.append("<tr><td><multiedit var=\"pm\" width=240 height=40><button value=\"Send PM\" action=\"bypass bbs_player_pm "+player.getName()+" $pm\" width=110 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr><tr><td><br><button value=\"Back\" action=\"bypass bbs_default\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr></table>");
				    htmlCode.append("</td></tr></table>");
				}
			}
			
			else if (command.startsWith("bbs_player_pm"))
			{
				htmlCode.append("<table border=0><tr><td FIXWIDTH=15></td><td align=center>L2J Community Board<img src=\"sek.cbui355\" width=610 height=1></td></tr><tr><td FIXWIDTH=15></td><td>");

				try
				{
					String val = command.substring(14);
					StringTokenizer st = new StringTokenizer(val);
					String name = st.nextToken();
					String message = val.substring(name.length()+1);
					L2PcInstance reciever = L2World.getInstance().getPlayer(name);
					CreatureSay cs = new CreatureSay(activeChar.getObjectId(), Say2.TELL, activeChar.getName(), message);
					if(reciever!=null && !reciever.getMessageRefusal())
					{
						reciever.sendPacket(cs);
						activeChar.sendPacket(cs);
						htmlCode.append("Message Sent<br><button value=\"Back\" action=\"bypass bbs_player_info "+reciever.getName()+"\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
					}
                    else
                    {
                        activeChar.sendPacket(new SystemMessage(SystemMessage.THE_PERSON_IS_IN_MESSAGE_REFUSAL_MODE));
                    }
				}
				catch (StringIndexOutOfBoundsException e)
				{
					// ignore
				}
				htmlCode.append("</td></tr></table>");
			}

			else
			{
				htmlCode.append("<center>the command: "+command+" is not implemented yet</center>");
			}
			
			htmlCode.append("</body></html>");
			
			activeChar.sendPacket(new ShowBoard(activeChar,htmlCode.toString()));
		}
	}

	private void onBBSDefault(StringBuffer htmlCode, int startIndex, L2PcInstance activeChar) 
	{
		if (activeChar != null)
        {
            Collection<L2PcInstance> players = L2World.getInstance().getAllPlayers();
            String tdClose = "</td>";
            String tdOpen = "<td align=left valign=top>";
            String trClose = "</tr>";
            String trOpen = "<tr>";
            String colSpacer = "<td FIXWIDTH=15></td>";
            RecordTable recordTableInstance = RecordTable.getInstance();

            htmlCode.append("<table>");

            SimpleDateFormat format = new SimpleDateFormat("H:mm");
            Calendar cal = Calendar.getInstance();
            int t = GameTimeController.getInstance().getGameTime();

            htmlCode.append(trOpen);
            htmlCode.append(tdOpen + "Server Time: " + format.format(cal.getTime()) + tdClose);
            htmlCode.append(colSpacer);
            cal.set(Calendar.HOUR_OF_DAY, t / 60);
            cal.set(Calendar.MINUTE, t % 60);
            htmlCode.append(tdOpen + "Game Time: " + format.format(cal.getTime()) + tdClose);
            htmlCode.append(colSpacer);
            htmlCode.append(tdOpen + "Server Restarted: " + GameServer.DateTimeServerStarted.getTime() + tdClose);
            htmlCode.append(trClose);

            htmlCode.append(trOpen);
            htmlCode.append(tdOpen + "XP Rate: " + Config.RATE_XP + tdClose);
            htmlCode.append(colSpacer);
            htmlCode.append(tdOpen + "Party XP Rate: " + Config.RATE_PARTY_XP + tdClose);
            htmlCode.append(colSpacer);
            htmlCode.append(tdOpen + "XP Exponent: " + Config.ALT_GAME_EXPONENT_XP + tdClose);
            htmlCode.append(trClose);

            htmlCode.append(trOpen);
            htmlCode.append(tdOpen + "SP Rate: " + Config.RATE_SP + tdClose);
            htmlCode.append(colSpacer);
            htmlCode.append(tdOpen + "Party SP Rate: " + Config.RATE_PARTY_SP + tdClose);
            htmlCode.append(colSpacer);
            htmlCode.append(tdOpen + "SP Exponent: " + Config.ALT_GAME_EXPONENT_SP + tdClose);
            htmlCode.append(trClose);

            htmlCode.append(trOpen);
            htmlCode.append(tdOpen + "Drop Rate: " + Config.RATE_DROP_ITEMS + tdClose);
            htmlCode.append(colSpacer);
            htmlCode.append(tdOpen + "Spoil Rate: " + Config.RATE_DROP_SPOIL + tdClose);
            htmlCode.append(colSpacer);
            htmlCode.append(tdOpen + "Adena Rate: " + Config.RATE_DROP_ADENA + tdClose);
            htmlCode.append(trClose);

            htmlCode.append("</table>");

            htmlCode.append("<table>");
            htmlCode.append(trOpen);
            htmlCode.append("<td><img src=\"sek.cbui355\" width=625 height=1><br></td>");
            htmlCode.append(trClose);

            if (activeChar.isGM())
            {
                htmlCode.append(trOpen);
                htmlCode.append(tdOpen + L2World.getInstance().getAllVisibleObjects().size()
                    + " Object count</td>");
                htmlCode.append(trClose);
            }
            htmlCode.append(trOpen);
            htmlCode.append(tdOpen + " Record of Player(s) Online:" + + recordTableInstance.getMaxPlayer() + "</td>");
            htmlCode.append(trClose);

            htmlCode.append(trOpen);
            htmlCode.append(tdOpen + " on : " + recordTableInstance.getDateMaxPlayer() + "</td>");
            htmlCode.append(trClose);

            htmlCode.append(trOpen);
            htmlCode.append(tdOpen + players.size() + " Player(s) Online:</td>");
            htmlCode.append(trClose);
            htmlCode.append("</table>");

            int i;
            htmlCode.append("<table border=0>");
            htmlCode.append("<tr><td><table border=0>");
            Iterator<L2PcInstance> iterator = players.iterator();

            int cell = 0;
            for (i = 0; i < startIndex + Config.NAME_PAGE_SIZE_COMMUNITYBOARD; i++)
            {
                if (i > players.size() - 1 || !iterator.hasNext()) break;

                L2PcInstance player = iterator.next();  // Get the current record
                if (i < startIndex) continue;           // If not at start index
                if ((player == null) || (player.getInvisible() == 1 && player != activeChar && !activeChar.isGM()))
                {
                    i--;                                // Don't count the current loop
                    continue;                           // Go to next
                }

                cell++;

                if (cell == 1) htmlCode.append(trOpen);

                htmlCode.append("<td align=left valign=top FIXWIDTH=75><a action=\"bypass bbs_player_info "
                    + player.getName() + "\">");

                if (player.isGM()) htmlCode.append("<font color=\"LEVEL\">" + player.getName()
                    + "</font>");
                else htmlCode.append(player.getName());

                htmlCode.append("</a></td>");

                if (cell < Config.NAME_PER_ROW_COMMUNITYBOARD) htmlCode.append(colSpacer);

                if (cell == Config.NAME_PER_ROW_COMMUNITYBOARD)
                {
                    cell = 0;
                    htmlCode.append(trClose);
                }
            }
            if (cell > 0 && cell < Config.NAME_PER_ROW_COMMUNITYBOARD) htmlCode.append(trClose);
            htmlCode.append("</table></td></tr>");

            if (players.size() > Config.NAME_PAGE_SIZE_COMMUNITYBOARD)
            {
                htmlCode.append("<tr><td align=center valign=top>Displaying " + (startIndex + 1) + " - "
                    + i + " player(s)</td></tr>");
                htmlCode.append("<tr><td align=center valign=top><table border=0 width=610>");
                htmlCode.append("<tr>");
                if (startIndex == 0) htmlCode.append("<td><button value=\"Prev\" width=50 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
                else htmlCode.append("<td><button value=\"Prev\" action=\"bypass bbs_default "
                    + (startIndex - Config.NAME_PAGE_SIZE_COMMUNITYBOARD)
                    + "\" width=50 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
                htmlCode.append("<td FIXWIDTH=10></td>");
                if (players.size() <= startIndex + Config.NAME_PAGE_SIZE_COMMUNITYBOARD) htmlCode.append("<td><button value=\"Next\" width=50 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
                else htmlCode.append("<td><button value=\"Next\" action=\"bypass bbs_default "
                    + (startIndex + Config.NAME_PAGE_SIZE_COMMUNITYBOARD)
                    + "\" width=50 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
                htmlCode.append("</tr>");
                htmlCode.append("</table></td></tr>");
            }

            htmlCode.append("</table>");
        }
	}

	/**
	 * @param client
	 * @param url
	 * @param arg1
	 * @param arg2
	 * @param arg3
	 * @param arg4
	 * @param arg5
	 */
	public void handleWriteCommands(ClientThread client, String url, String arg1, String arg2, String arg3, String arg4, String arg5)
	{
		// TODO Auto-generated method stub
		
	}
}
