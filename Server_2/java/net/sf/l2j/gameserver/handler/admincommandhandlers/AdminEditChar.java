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

import java.util.Collection;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.ClanTable;
import net.sf.l2j.gameserver.handler.IAdminCommandHandler;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2PcInstance;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.serverpackets.StatusUpdate;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;

/**
 * This class handles following admin commands:
 * - edit_character
 * - current_player
 * - character_list
 * - show_characters
 * - find_character
 * - save_modifications
 * 
 * @version $Revision: 1.3.2.1.2.10 $ $Date: 2005/04/11 10:06:06 $
 */
public class AdminEditChar implements IAdminCommandHandler {
	private static Logger _log = Logger.getLogger(AdminEditChar.class.getName());
	
	private static String[] _adminCommands = {
			"admin_edit_character",
			"admin_current_player",
			"admin_character_list",
			"admin_show_characters",
			"admin_find_character",
			"admin_save_modifications"
			};
	private static final int REQUIRED_LEVEL = Config.GM_CHAR_EDIT;

	public boolean useAdminCommand(String command, L2PcInstance activeChar) {
		if (!(checkLevel(activeChar.getAccessLevel()) && activeChar.isGM())) return false;
		
		if (command.equals("admin_edit_character"))
		{
			editCharacter(activeChar);
		}
		else if (command.equals("admin_current_player"))
		{
			showCharacterList(activeChar);
		}

		else if (command.startsWith("admin_character_list"))
		{
			try
			{
				showCharacterList(activeChar);
			}
			catch (StringIndexOutOfBoundsException e)
			{
				//Case of empty character name
			}
			
		}
		else if (command.startsWith("admin_show_characters"))
		{
			try
			{   
				String val = command.substring(22);
				int page = Integer.parseInt(val);
				listCharacters(activeChar, page);
			}
			catch (StringIndexOutOfBoundsException e)
			{
				//Case of empty page
			}
		}
		else if (command.startsWith("admin_find_character"))
		{
			try
			{
				String val = command.substring(21); 
				findCharacter(activeChar, val);
			}
			catch (StringIndexOutOfBoundsException e)
			{	//Case of empty character name
				SystemMessage sm = new SystemMessage(614);
				sm.addString("You didnt enter a character name to find.");
				activeChar.sendPacket(sm);
				
				listCharacters(activeChar, 0);
			}			
		}
		else if (command.startsWith("admin_save_modifications"))
		{
			try
			{
				String val = command.substring(24); 
				adminModifyCharacter(activeChar, val);
			}
			catch (StringIndexOutOfBoundsException e)
			{	//Case of empty character name
				SystemMessage sm = new SystemMessage(614);
				sm.addString("Error while modifying character.");
				activeChar.sendPacket(sm);
				listCharacters(activeChar, 0);
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
	
	private void listCharacters(L2PcInstance activeChar, int page)
	{	
		Collection<L2PcInstance> allPlayers = L2World.getInstance().getAllPlayers();
		L2PcInstance[] players = allPlayers.toArray(new L2PcInstance[allPlayers.size()]);

		int MaxCharactersPerPage = 20;
		int MaxPages = players.length / MaxCharactersPerPage;
		int modulus = players.length % MaxCharactersPerPage;
			
		if (modulus!=0)
		{
			MaxPages = MaxPages+1;
		}
		
		//Check if number of users changed
		if (page>MaxPages)
		{
			page=MaxPages;
		}

		int CharactersStart = MaxCharactersPerPage*page;
		int CharactersEnd = players.length-CharactersStart;		
		
		NpcHtmlMessage adminReply = new NpcHtmlMessage(5);		

		StringBuffer replyMSG = new StringBuffer("<html><body>");
		replyMSG.append("<table width=260><tr>");
		replyMSG.append("<td width=40><button value=\"Main\" action=\"bypass -h admin_admin\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
		replyMSG.append("<td width=180><center>Character Selection Menu</center></td>");
		replyMSG.append("<td width=40><button value=\"Back\" action=\"bypass -h admin_admin\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
		replyMSG.append("</tr></table>");
		replyMSG.append("<br><br>");
		replyMSG.append("<table width=270>");
		replyMSG.append("<tr><td width=270>You can find a character by writing his name and</td></tr>");
		replyMSG.append("<tr><td width=270>clicking Find bellow.<br></td></tr>");
		replyMSG.append("<tr><td width=270>Note: Names should be written case sensitive.</td></tr>");
		replyMSG.append("</table><br>");
		replyMSG.append("<center><table><tr><td>");
		replyMSG.append("<edit var=\"character_name\" width=80></td><td><button value=\"Find\" action=\"bypass -h admin_find_character $character_name\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
		replyMSG.append("</td></tr></table></center><br><br>");
		
		for (int x=0; x<MaxPages; x++)
		{
			replyMSG.append("<center><a action=\"bypass -h admin_show_characters " + x + "\">Page " + x+1 + "</a></center>");	
		}			
		replyMSG.append("<br>");

		//List Players in a Table
		replyMSG.append("<table width=270>");		
		replyMSG.append("<tr><td width=80>Name:</td><td width=110>Class:</td><td width=40>Level:</td></tr>");
		for (int i = CharactersStart; i < CharactersEnd; i++)
		{	//Add player info into new Table row
			replyMSG.append("<tr><td width=80>" + "<a action=\"bypass -h admin_character_list " + players[i].getName() + "\">" + players[i].getName() + "</a></td><td width=110>" + players[i].getTemplate().className + "</td><td width=40>" + players[i].getLevel() + "</td></tr>");
		}
		replyMSG.append("</table>");
		replyMSG.append("</body></html>");
		
		adminReply.setHtml(replyMSG.toString());
		activeChar.sendPacket(adminReply);			
	}
	
	private void showCharacterList(L2PcInstance activeChar)
	{
		L2Object target = activeChar.getTarget();
		L2PcInstance player = null;
		if (target instanceof L2PcInstance) {
			player = (L2PcInstance)target;
		} else {
			return;
		}

		NpcHtmlMessage adminReply = new NpcHtmlMessage(5); 
		
		StringBuffer replyMSG = new StringBuffer("<html><body>");
		replyMSG.append("<table width=260><tr>");
		replyMSG.append("<td width=40><button value=\"Main\" action=\"bypass -h admin_admin\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
		replyMSG.append("<td width=180><center>Character Selection Menu</center></td>");
		replyMSG.append("<td width=40><button value=\"Back\" action=\"bypass -h admin_show_characters 0\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
		replyMSG.append("</tr></table>");
		replyMSG.append("<br><br>");
		replyMSG.append("<center>Character Information:</center>");
		replyMSG.append("<br>");
		
		// Character Player Info
		replyMSG.append("<table width=270>");		
		replyMSG.append("<tr><td width=135>Name: " + player.getName() + "</td><td width=135>Lv: " + player.getLevel() + " " + player.getTemplate().className + "</td></tr>");	
		replyMSG.append("<tr><td width=135>Clan: " + ClanTable.getInstance().getClan(player.getClanId()) + "</td><td width=135>Exp: " + player.getExp() + "</td></tr>");			
		replyMSG.append("</table>");

		// Character ClassID & Coördinates
		replyMSG.append("<table width=270>");
		replyMSG.append("<tr><td width=270>Class Template Id: " + player.getClassId() + "</td></tr>");
		replyMSG.append("<tr><td width=270>Character Co-ordinates: " + player.getX() + " " + player.getY() + " " + player.getZ() + "</td></tr>");
		replyMSG.append("</table>");
		replyMSG.append("<br>");
		
		// Character Stats
		replyMSG.append("<table width=270>");
		replyMSG.append("<tr><td width=40></td><td width=70>Curent:</td><td width=70>Max:</td><td width=90></td></tr>");
		replyMSG.append("<tr><td width=40>HP:</td><td width=70>" + player.getCurrentHp() + "</td><td width=70>" + player.getMaxHp() + "</td><td width=70>Karma: " + player.getKarma() + "</td><td width=20></td></tr>");
		replyMSG.append("<tr><td width=40>MP:</td><td width=70>" + player.getCurrentMp() + "</td><td width=70>" + player.getMaxMp() + "</td><td width=70>Pvp Kills: " + player.getPvpKills() +  "</td><td width=20></td></tr>");
		replyMSG.append("<tr><td width=40>Load:</td><td width=70>" + player.getCurrentLoad() + "</td><td width=70>" + player.getMaxLoad() + "</td><td width=70>Pvp Flag: " + player.getPvpFlag() + "</td><td width=20></td></tr>");
		replyMSG.append("<tr><td width=40>SP:</td><td width=70>" + player.getSp() + "</td><td width=70></td><td width=90></td></tr>");		
		replyMSG.append("</table>");
		replyMSG.append("<table width=270>");		
		replyMSG.append("<tr><td width=60>P.ATK: " + player.getPAtk(null) + "</td><td width=100>M.ATK: " + player.getMAtk(null,null) + "</td><td width=40>Manage:</td></tr>");
		replyMSG.append("<tr><td width=60>P.DEF: " + player.getPDef(null) + "</td><td width=100>M.DEF: " + player.getMDef(null,null) + "</td><td width=40></td></tr>");
		replyMSG.append("<tr><td width=90>Accuracy: " + player.getAccuracy() + "</td><td width=70>Evasion: " + player.getEvasionRate(null) + "</td><td width=40><button value=\"Skills\" action=\"bypass -h admin_show_skills\" width=60 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
		replyMSG.append("<tr><td width=90>Critical: " + player.getCriticalHit(null,null) + "</td><td width=70>Speed: " + player.getRunSpeed() + "</td><td width=40><button value=\"Stats\" action=\"bypass -h admin_edit_character\" width=60 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
		replyMSG.append("<tr><td width=90>ATK Spd: " + player.getPAtkSpd() + "</td><td width=70>Casting Spd: " + player.getMAtkSpd() + "</td><td width=40><button value=\"Exp & Sp\" action=\"bypass -h admin_add_exp_sp_to_character\" width=60 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");		
		replyMSG.append("</table>");
		replyMSG.append("</body></html>");

		adminReply.setHtml(replyMSG.toString());
		activeChar.sendPacket(adminReply);
	}
		
	private void adminModifyCharacter(L2PcInstance activeChar, String modifications)
	{
		L2Object target = activeChar.getTarget();
		L2PcInstance player = null;
		if (target instanceof L2PcInstance) {
			player = (L2PcInstance)target;
		} else {
			return;
		}

		StringTokenizer st = new StringTokenizer(modifications);
		if (st.countTokens()!=9)
		{
			editCharacter(player);
		}
		else
		{
		String hp = st.nextToken();
		String hpmax = st.nextToken();
		String mp = st.nextToken();
		String mpmax = st.nextToken();
		String load = st.nextToken();
		String karma = st.nextToken();
		String pvpflag = st.nextToken();
		String pvpkills = st.nextToken();
		String classid = st.nextToken();
		int hpval = Integer.parseInt(hp);
		int hpmaxval = Integer.parseInt(hpmax);
		int mpval = Integer.parseInt(mp);
		int mpmaxval = Integer.parseInt(mpmax);
		int loadval = Integer.parseInt(load);
		int karmaval = Integer.parseInt(karma);
		int pvpflagval = Integer.parseInt(pvpflag);
		int pvpkillsval = Integer.parseInt(pvpkills);
		int classidval = Integer.parseInt(classid);
		
		//Common character information
		SystemMessage sm = new SystemMessage(614);
		sm.addString("Admin has changed your stats." +
				" Hp: "+hpval+" HpMax: "+hpmaxval+" Mp: "+mpval+" MpMax: "+mpmaxval+
				" MaxLoad: "+loadval+" Karma: "+karmaval+" Pvp: "+pvpflagval+" / "+pvpkillsval+" ClassId: "+classidval);
		
		player.sendPacket(sm);
		
		player.setCurrentHp(hpval);
		player.setCurrentMp(mpval);
//		player.setMaxHp(hpmaxval);
//		player.setBaseMaxMp(mpmaxval);
		player.setMaxLoad(loadval);
		player.setKarma(karmaval);
		player.setPvpFlag(pvpflagval);
		player.setPvpKills(pvpkillsval);
		player.setClassId(classidval);
				
		StatusUpdate su = new StatusUpdate(player.getObjectId());
		su.addAttribute(StatusUpdate.CUR_HP, hpval);
		su.addAttribute(StatusUpdate.MAX_HP, hpmaxval);
		su.addAttribute(StatusUpdate.CUR_MP, mpval);
		su.addAttribute(StatusUpdate.MAX_MP, mpmaxval);
		su.addAttribute(StatusUpdate.MAX_LOAD, loadval);
		su.addAttribute(StatusUpdate.KARMA, karmaval);
		su.addAttribute(StatusUpdate.PVP_FLAG, pvpflagval);
		player.sendPacket(su);
		
		//Admin information	
		SystemMessage smA = new SystemMessage(614);
		smA.addString("Changed stats of "+player.getName()+". " +
				" Hp: "+hpval+" HpMax: "+hpmaxval+" Mp: "+mpval+" MpMax: "+mpmaxval+
				" MaxLoad: "+loadval+" Karma: "+karmaval+" Pvp: "+pvpflagval+" / "+pvpkillsval+" ClassId: "+classidval);
		
		activeChar.sendPacket(smA);
		if (Config.DEBUG) 
			_log.fine("[GM]"+activeChar.getName()+" changed stats of "+player.getName()+". " +
					" Hp: "+hpval+" HpMax: "+hpmaxval+" Mp: "+mpval+" MpMax: "+mpmaxval+
					" MaxLoad: "+loadval+" Karma: "+karmaval+" Pvp: "+pvpflagval+" / "+pvpkillsval+
					" ClassId: "+classidval);
		
		showCharacterList(activeChar); //Back to start
		}
	}

	private void editCharacter(L2PcInstance activeChar)
	{
		///FIXME Made it so that you have to enter all values to 'prevent' abuses...
		L2Object target = activeChar.getTarget();
		L2PcInstance player = null;
		if (target instanceof L2PcInstance) {
			player = (L2PcInstance)target;
		} else {
			return;
		}
		
		NpcHtmlMessage adminReply = new NpcHtmlMessage(5); 
		
		StringBuffer replyMSG = new StringBuffer("<html><body>");
		replyMSG.append("<table width=260><tr>");
		replyMSG.append("<td width=40><button value=\"Main\" action=\"bypass -h admin_admin\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
		replyMSG.append("<td width=180><center>Character Selection Menu</center></td>");
		replyMSG.append("<td width=40><button value=\"Back\" action=\"bypass -h admin_current_player\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
		replyMSG.append("</tr></table>");
		replyMSG.append("<br><br>");
		replyMSG.append("<center>Editing character: " + player.getName() + "</center><br>");
		replyMSG.append("<table width=270>");
		replyMSG.append("<tr><td width=40></td><td width=70>Curent:</td><td width=70>Max:</td><td width=90></td></tr>");
		replyMSG.append("<tr><td width=40>HP:</td><td width=70>" + player.getCurrentHp() + "</td><td width=70>" + player.getMaxHp() + "</td><td width=70>Karma: " + player.getKarma() + "</td><td width=20></td></tr>");
		replyMSG.append("<tr><td width=40>MP:</td><td width=70>" + player.getCurrentMp() + "</td><td width=70>" + player.getMaxMp() + "</td><td width=70>Pvp Kills: " + player.getPvpKills() +  "</td><td width=20></td></tr>");
		replyMSG.append("<tr><td width=40>Load:</td><td width=70>" + player.getCurrentLoad() + "</td><td width=70>" + player.getMaxLoad() + "</td><td width=70>Pvp Flag: " + player.getPvpFlag() + "</td><td width=20></td></tr>");
		replyMSG.append("</table>");
		replyMSG.append("<table width=270><tr><td>Class Template Id: " + player.getClassId() + "</td></tr></table><br>");
		replyMSG.append("<table width=270>");
		replyMSG.append("<tr><td>Note: Fill all values before saving the modifications.</td></tr>");
		replyMSG.append("</table><br>");
		replyMSG.append("<table width=270>");
		replyMSG.append("<tr><td width=50>Hp:</td><td><edit var=\"hp\" width=50></td><td width=50>Karma:</td><td><edit var=\"karma\" width=50></td></tr>");
		replyMSG.append("<tr><td width=50>HpMax:</td><td><edit var=\"hpmax\" width=50></td><td width=50>Pvp Flag:</td><td><edit var=\"pvpflag\" width=50></td></tr>");
		replyMSG.append("<tr><td width=50>Mp:</td><td><edit var=\"mp\" width=50></td><td width=50>Pvp Kills:</td><td><edit var=\"pvpkills\" width=50></td></tr>");
		replyMSG.append("<tr><td width=50>MpMax:</td><td><edit var=\"mpmax\" width=50></td><td width=50>Class Id:</td><td><edit var=\"classid\" width=50></td></tr>");
		replyMSG.append("<tr><td width=50>MaxLoad:</td><td><edit var=\"load\" width=50></td><td width=50></td><td width=50></td></tr>");
		replyMSG.append("</table><br>");
		replyMSG.append("<center><button value=\"Save Changes\" action=\"bypass -h" + " admin_save_modifications $hp $hpmax $mp $mpmax $load $karma $pvpflag $pvpkills $classid\" width=80 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></center><br>");
		replyMSG.append("</body></html>");

		adminReply.setHtml(replyMSG.toString());
		activeChar.sendPacket(adminReply);
	}
	

	//FIXME: needs removal, whole thing needs to use getTarget()
	private void findCharacter(L2PcInstance activeChar, String CharacterToFind)
	{
		Collection<L2PcInstance> allPlayers = L2World.getInstance().getAllPlayers();
		L2PcInstance[] players = allPlayers.toArray(new L2PcInstance[allPlayers.size()]);
		NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
		int CharactersFound = 0;
		
		StringBuffer replyMSG = new StringBuffer("<html><body>");
		replyMSG.append("<table width=260><tr>");
		replyMSG.append("<td width=40><button value=\"Main\" action=\"bypass -h admin_admin\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
		replyMSG.append("<td width=180><center>Character Selection Menu</center></td>");
		replyMSG.append("<td width=40><button value=\"Back\" action=\"bypass -h admin_show_characters 0\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
		replyMSG.append("</tr></table>");
		replyMSG.append("<br><br>");
		
		for (int i = 0; i < players.length; i++)
		{	//Add player info into new Table row
			
			if (players[i].getName().startsWith((CharacterToFind)))
			{
				CharactersFound = CharactersFound+1;
				replyMSG.append("<table width=270>");		
				replyMSG.append("<tr><td width=80>Name</td><td width=110>Class</td><td width=40>Level</td></tr>");
				replyMSG.append("<tr><td width=80><a action=\"bypass -h admin_character_list " + players[i].getName() + "\">" + players[i].getName() + "</a></td><td width=110>" + players[i].getTemplate().className + "</td><td width=40>" + players[i].getLevel() + "</td></tr>");
				replyMSG.append("</table>");
			}	
		}
		
		if (CharactersFound==0)
    	{
			replyMSG.append("<table width=270>");
			replyMSG.append("<tr><td width=270>Your search did not find any characters.</td></tr>");
			replyMSG.append("<tr><td width=270>Please try again.<br></td></tr>");
			replyMSG.append("</table><br>");
			replyMSG.append("<center><table><tr><td>");
			replyMSG.append("<edit var=\"character_name\" width=80></td><td><button value=\"Find\" action=\"bypass -h admin_find_character $character_name\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
			replyMSG.append("</td></tr></table></center>");
		}
		else
		{
			replyMSG.append("<center><br>Found " + CharactersFound + " character");
			
			if (CharactersFound==1)
			{
				replyMSG.append(".");
			}
			else 
			{
				if (CharactersFound>1)
				{
					replyMSG.append("s.");
				}
			}

		}
		
		replyMSG.append("</center></body></html>");
		
		adminReply.setHtml(replyMSG.toString());
		activeChar.sendPacket(adminReply);			
	}
}
