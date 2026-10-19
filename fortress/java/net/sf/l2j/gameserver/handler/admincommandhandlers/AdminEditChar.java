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
import net.sf.l2j.gameserver.model.Inventory;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2PcInstance;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.serverpackets.CharInfo;
import net.sf.l2j.gameserver.serverpackets.InventoryUpdate;
import net.sf.l2j.gameserver.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.serverpackets.StatusUpdate;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.serverpackets.UserInfo;

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
			"admin_nokarma", // this is to remove karma from selected char...
			"admin_setkarma", // sets karma of target char to any amount. //setkarma <karma>
			"admin_setew", // enchants the currently equipped weapon of the selected char. //setEW <0-65535>			
			"admin_character_list",
			"admin_show_characters",
			"admin_find_character",
			"admin_save_modifications",
			"admin_rec",
            "admin_settitle",
            "admin_setname",
            "admin_setsex",
            "admin_setcolor"
			};
	private static final int REQUIRED_LEVEL = Config.GM_CHAR_EDIT;
	private static final int REQUIRED_LEVEL_VIEW = Config.GM_CHAR_VIEW;

	public boolean useAdminCommand(String command, L2PcInstance activeChar) {
		if (!((checkLevel(activeChar.getAccessLevel()) || checkLevel2(activeChar.getAccessLevel())) && activeChar.isGM())) return false;
		else if (command.equals("admin_current_player"))
		{
			showCharacterList(activeChar, null);
		}
		else if (command.startsWith("admin_character_list"))
		{
			try
			{
				String val = command.substring(21); 
				L2PcInstance target = L2World.getPlayer(val);
				showCharacterList(activeChar, target);
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
				SystemMessage sm = new SystemMessage(SystemMessage.S1_S2);
				sm.addString("You didnt enter a character name to find.");
				activeChar.sendPacket(sm);
				
				listCharacters(activeChar, 0);
			}			
		}
		else if (!(checkLevel(activeChar.getAccessLevel()) && activeChar.isGM())) return false;
		else if (command.equals("admin_edit_character"))
		{
			editCharacter(activeChar);
		}
		// Karma control commands
		else if (command.equals("admin_nokarma"))
		{
			setTargetKarma(activeChar, 0);
		}
		else if (command.startsWith("admin_setkarma"))
		{
			try
			{   
				String val = command.substring(15);
				int karma = Integer.parseInt(val);
				setTargetKarma(activeChar, karma);
			}
			catch (StringIndexOutOfBoundsException e)
			{
				if ( Config.DEVELOPER ) System.out.println("Set karma error: "+e);
				SystemMessage sm = new SystemMessage(SystemMessage.S1_S2);
				sm.addString("Please specify new karma value.");
				activeChar.sendPacket(sm);
			}
		}
		// Enchantment Commands
		else if (command.startsWith("admin_setew"))
		{
			try
			{   
				String val = command.substring(12);
				int ench = Integer.parseInt(val);
				setTargetEW(activeChar, ench);
			}
			catch (StringIndexOutOfBoundsException e)
			{
				if ( Config.DEVELOPER ) System.out.println("Set weapon enchant error: "+e);
				SystemMessage sm = new SystemMessage(SystemMessage.S1_S2);
				sm.addString("Please specify new enchant value.");
				activeChar.sendPacket(sm);
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
				SystemMessage sm = new SystemMessage(SystemMessage.S1_S2);
				sm.addString("Error while modifying character.");
				activeChar.sendPacket(sm);
				listCharacters(activeChar, 0);
			}			
		}
		else if (command.equals("admin_rec"))
		{
			L2Object target = activeChar.getTarget();
			L2PcInstance player = null;
			if (target instanceof L2PcInstance) {
				player = (L2PcInstance)target;
			} else {
				return false;
			}
			player.setRecomHave(player.getRecomHave()+1);
			SystemMessage sm = new SystemMessage(SystemMessage.S1_S2);
			sm.addString("You have been recommended by a GM");
			player.sendPacket(sm);
			player.broadcastUserInfo();
		}
		else if (command.startsWith("admin_rec"))
		{
			String val = command.substring(10);
			int recVal = Integer.parseInt(val);
			L2Object target = activeChar.getTarget();
			L2PcInstance player = null;
			if (target instanceof L2PcInstance) {
				player = (L2PcInstance)target;
			} else {
				return false;
			}
			player.setRecomHave(player.getRecomHave()+recVal);
			SystemMessage sm = new SystemMessage(SystemMessage.S1_S2);
			sm.addString("You have been recommended by a GM");
			player.sendPacket(sm);
			player.broadcastUserInfo();
		}
        else if (command.startsWith("admin_settitle"))
        {
            try
            {
                String val = command.substring(15); 
                L2Object target = activeChar.getTarget();
                L2PcInstance player = null;
                if (target instanceof L2PcInstance) {
                    player = (L2PcInstance)target;
                } else {
                    return false;
                }
                player.setTitle(val);
                SystemMessage sm = new SystemMessage(SystemMessage.S1_S2);
                sm.addString("Your title has been changed by a GM");
                player.sendPacket(sm);
                player.broadcastUserInfo();
            }
            catch (StringIndexOutOfBoundsException e)
            {   //Case of empty character title
                SystemMessage sm = new SystemMessage(SystemMessage.S1_S2);
                sm.addString("You need to specify the new title.");
                activeChar.sendPacket(sm);
            }           
        }
        else if (command.startsWith("admin_setname"))
        {
            try
            {
                String val = command.substring(14); 
                L2Object target = activeChar.getTarget();
                L2PcInstance player = null;
                if (target instanceof L2PcInstance) {
                    player = (L2PcInstance)target;
                } else {
                    return false;
                }
                player.setName(val);
                SystemMessage sm = new SystemMessage(SystemMessage.S1_S2);
                sm.addString("Your name has been changed by a GM");
                player.sendPacket(sm);
                player.broadcastUserInfo();
            }
            catch (StringIndexOutOfBoundsException e)
            {   //Case of empty character name
                SystemMessage sm = new SystemMessage(SystemMessage.S1_S2);
                sm.addString("You need to specify the new name.");
                activeChar.sendPacket(sm);
            }           
        }	
        else if (command.startsWith("admin_setsex"))
        {
                L2Object target = activeChar.getTarget();
                L2PcInstance player = null;
                if (target instanceof L2PcInstance) {
                    player = (L2PcInstance)target;
                } else {
                    return false;
                }
                player.changeSex();
                SystemMessage sm = new SystemMessage(SystemMessage.S1_S2);
                sm.addString("Your gender has been changed by a GM");
                player.sendPacket(sm);
                player.broadcastUserInfo();  
        }   
        else if (command.startsWith("admin_setcolor"))
        {
            try
            {
                String val          = command.substring(15); 
                L2Object target     = activeChar.getTarget();
                L2PcInstance player = null;
                if (target instanceof L2PcInstance) {
                    player = (L2PcInstance)target;
                } else {
                    return false;
                }
                player.setNameColor(Integer.decode("0x"+val));
                player.sendMessage("Your name color has been changed by a GM");
                player.broadcastUserInfo();
            }
            catch (StringIndexOutOfBoundsException e)
            {   //Case of empty color
                activeChar.sendMessage("You need to specify the new color.");
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
	private boolean checkLevel2(int level) {
		return (level >= REQUIRED_LEVEL_VIEW);
	}
	
	private void listCharacters(L2PcInstance activeChar, int page)
	{	
		Collection<L2PcInstance> allPlayers = L2World.getAllPlayers();
		L2PcInstance[] players = allPlayers.toArray(new L2PcInstance[allPlayers.size()]);

		int MaxCharactersPerPage = 20;
		int MaxPages = players.length / MaxCharactersPerPage;
			
		if (players.length > MaxCharactersPerPage * MaxPages)
			MaxPages++;
		
		//Check if number of users changed
		if (page>MaxPages)
		{
			page=MaxPages;
		}

		int CharactersStart = MaxCharactersPerPage*page;
		int CharactersEnd = players.length;		
		if (CharactersEnd - CharactersStart > MaxCharactersPerPage)
		    CharactersEnd = CharactersStart + MaxCharactersPerPage;
		
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
			int pagenr = x + 1;
			replyMSG.append("<center><a action=\"bypass -h admin_show_characters " + x + "\">Page " + pagenr + "</a></center>");	
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
	
	private void showCharacterList(L2PcInstance activeChar, L2PcInstance player)
	{
	    if (player == null){
		L2Object target = activeChar.getTarget();
		//L2PcInstance player = null;
		if (target instanceof L2PcInstance) {
			player = (L2PcInstance)target;
		} else {
			return;
		}
	    } else {activeChar.setTarget(player);}

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

		// Character ClassID & Coordinates
		replyMSG.append("<table width=270>");
		replyMSG.append("<tr><td width=270>Class Template Id: " + player.getClassId() + "</td></tr>");
		replyMSG.append("<tr><td width=270>Character Coordinates: " + player.getX() + " " + player.getY() + " " + player.getZ() + "</td></tr>");
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
	
	private void setTargetEW(L2PcInstance activeChar, int ench) {
		// function to change enchantment of selected chars equipped weapon
		L2Object target = activeChar.getTarget();
		if (target == null)
			target = activeChar;
		L2PcInstance player = null;
		if (target instanceof L2PcInstance) {
			player = (L2PcInstance)target;
		} else {
			return;
		}
		
		// first make sure enchant lvl is within acceptable range
		if ( ench >= 0 && ench <= 65535 ) {
			// now we need to find the equipped weapon of the targeted character...
			int curEnchant = 0; // display purposes only
			String weapType = "NONE"; // display purposes
			int dropSlot = Inventory.PAPERDOLL_RHAND; // single handed weapon
			int dropSlot2 = Inventory.PAPERDOLL_LRHAND; // two handed weapon
			boolean canEnchant = false;
			L2ItemInstance pWeapInstance = null;
			L2ItemInstance weaponToEnchant = null;
			
			// only attempt to enchant if there is a weapon equipped
            // FIXME: Isn't canEnchant allways == false at this point???
			if ( canEnchant == false )
            {
				// need to check if is 1 hand weapon, 2 hand weapon, or no weapon
				pWeapInstance = player.getInventory().getPaperdollItem(dropSlot);
				if (pWeapInstance != null && pWeapInstance.getEquipSlot() == dropSlot )
                { // 1h weapon					
					weaponToEnchant = pWeapInstance;
					curEnchant = weaponToEnchant.getEnchantLevel();
					canEnchant = true;
					weapType = "R-Hand";
				}
				else
                {
					pWeapInstance = player.getInventory().getPaperdollItem(dropSlot2);
					if (pWeapInstance != null && pWeapInstance.getEquipSlot() == dropSlot2 )
                    { // 2h weapon
						weaponToEnchant = pWeapInstance;
						curEnchant = weaponToEnchant.getEnchantLevel();
						canEnchant = true;
						weapType = "LR-Hand";
					}
				}
			}
			else return;
			
			if ( canEnchant == true && weaponToEnchant != null ) {
				// unequip the item, set new enchant lvl, reequip
				player.getInventory().unEquipItemInSlotAndRecord(dropSlot);
				pWeapInstance.setEnchantLevel(ench);							    
				player.getInventory().equipItemAndRecord(weaponToEnchant);
				
				// update inventory
				InventoryUpdate iu = new InventoryUpdate();	
				iu.addModifiedItem(weaponToEnchant);
			    player.sendPacket(iu);
				
			    player.broadcastPacket(new CharInfo(player));
			    player.sendPacket(new UserInfo(player));
			    
				// notify admin
				activeChar.sendMessage("Changed enchantment of equipped "+weapType+" weapon ("+weaponToEnchant.getItem().getName()+") from ("+curEnchant+") to ("+ench+").");
				
				// notify player
				player.sendMessage("Admin has changed the enchantment of your equipped weapon ("+weaponToEnchant.getItem().getName()+") from "+curEnchant+" to "+ench+".");
			}
			else {
				// no weapon is equipped
				// notify admin
				SystemMessage smA = new SystemMessage(SystemMessage.S1_S2);
				smA.addString("Cannot enchant weapon for player "+player.getName()+": No weapon equipped.");		
				activeChar.sendPacket(smA);
				
				return;
			}
		}
		else {
			// inform our gm of their mistake
			SystemMessage smA = new SystemMessage(SystemMessage.S1_S2);
			smA.addString("You must set the enchant level to be between 0-65535.");		
			activeChar.sendPacket(smA);			
		}
	}
	
	private void setTargetKarma(L2PcInstance activeChar, int newKarma) {
		// function to change karma of selected char
		L2Object target = activeChar.getTarget();
		L2PcInstance player = null;
		if (target instanceof L2PcInstance) {
			player = (L2PcInstance)target;
		} else {
			return;
		}
		
		if ( newKarma >= 0 ) {
			// for display
			int oldKarma = player.getKarma();			
			
			// update karma
			player.setKarma(newKarma);
			
			StatusUpdate su = new StatusUpdate(player.getObjectId());
			su.addAttribute(StatusUpdate.KARMA, newKarma);
			player.sendPacket(su);
			
			/*
			StatusUpdate statu = new StatusUpdate(player.getObjectId());
			statu.addAttribute(StatusUpdate.KARMA, newKarma);
			activeChar.sendPacket(statu);
			*/
			
		    player.broadcastPacket(new CharInfo(player));
		    player.sendPacket(new UserInfo(player));
			
			//Common character information
			SystemMessage sm = new SystemMessage(SystemMessage.S1_S2);
			sm.addString("Admin has changed your karma from " + oldKarma + " to " + newKarma + ".");
			player.sendPacket(sm);
			
			//Admin information	
			SystemMessage smA = new SystemMessage(SystemMessage.S1_S2);
			smA.addString("Successfully Changed karma for "+player.getName()+" from (" + oldKarma + ") to (" + newKarma + ").");		
			activeChar.sendPacket(smA);			
			
			if (Config.DEBUG) 
				_log.fine("[SET KARMA] [GM]"+activeChar.getName()+" Changed karma for "+player.getName()+" from (" + oldKarma + ") to (" + newKarma + ").");
		}
		else {
			// tell admin of mistake 
			SystemMessage smA = new SystemMessage(SystemMessage.S1_S2);
			smA.addString("You must enter a value for karma greater than or equal to 0.");		
			activeChar.sendPacket(smA);
			
			if (Config.DEBUG) 
				_log.fine("[SET KARMA] ERROR: [GM]"+activeChar.getName()+" entered an incorrect value for new karma: " + newKarma + " for "+player.getName()+".");			
		}
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
		if (st.countTokens()!=8)
		{
			editCharacter(player);
		}
		else
		{
			String hp = st.nextToken();
			String hpmax = st.nextToken();
			String mp = st.nextToken();
			String mpmax = st.nextToken();
		    //String load = st.nextToken();
			String karma = st.nextToken();
			String pvpflag = st.nextToken();
			String pvpkills = st.nextToken();
			String classid = st.nextToken();
			int hpval = Integer.parseInt(hp);
			int hpmaxval = Integer.parseInt(hpmax);
			int mpval = Integer.parseInt(mp);
			int mpmaxval = Integer.parseInt(mpmax);
		    //int loadval = Integer.parseInt(load);
			int karmaval = Integer.parseInt(karma);
			int pvpflagval = Integer.parseInt(pvpflag);
			int pvpkillsval = Integer.parseInt(pvpkills);
			int classidval = Integer.parseInt(classid);
		
			//Common character information
		    SystemMessage sm = new SystemMessage(SystemMessage.S1_S2);
			sm.addString("Admin has changed your stats." +
					" Hp: "+hpval+" HpMax: "+hpmaxval+" Mp: "+mpval+" MpMax: "+mpmaxval+
		                 " Karma: "+karmaval+" Pvp: "+pvpflagval+" / "+pvpkillsval+" ClassId: "+classidval);
		
			player.sendPacket(sm);
		
			player.setCurrentHp(hpval);
			player.setCurrentMp(mpval);
//			player.setMaxHp(hpmaxval);
//			player.setBaseMaxMp(mpmaxval);
//		    player.setMaxLoad(loadval);
			player.setKarma(karmaval);
			player.setPvpFlag(pvpflagval);
			player.setPvpKills(pvpkillsval);
			player.setClassId(classidval);
				
			StatusUpdate su = new StatusUpdate(player.getObjectId());
			su.addAttribute(StatusUpdate.CUR_HP, hpval);
			su.addAttribute(StatusUpdate.MAX_HP, hpmaxval);
			su.addAttribute(StatusUpdate.CUR_MP, mpval);
			su.addAttribute(StatusUpdate.MAX_MP, mpmaxval);
			su.addAttribute(StatusUpdate.KARMA, karmaval);
			su.addAttribute(StatusUpdate.PVP_FLAG, pvpflagval);
			player.sendPacket(su);
		
			//Admin information	
		    SystemMessage smA = new SystemMessage(SystemMessage.S1_S2);
			smA.addString("Changed stats of "+player.getName()+". " +
				" Hp: "+hpval+" HpMax: "+hpmaxval+" Mp: "+mpval+" MpMax: "+mpmaxval+
		                  " Karma: "+karmaval+" Pvp: "+pvpflagval+" / "+pvpkillsval+" ClassId: "+classidval);
		
			activeChar.sendPacket(smA);
			if (Config.DEBUG) 
				_log.fine("[GM]"+activeChar.getName()+" changed stats of "+player.getName()+". " +
					" Hp: "+hpval+" HpMax: "+hpmaxval+" Mp: "+mpval+" MpMax: "+mpmaxval+
		                  " Karma: "+karmaval+" Pvp: "+pvpflagval+" / "+pvpkillsval+
					" ClassId: "+classidval);
		
			showCharacterList(activeChar,null); //Back to start
    		player.broadcastPacket(new CharInfo(player));
		    player.sendPacket(new UserInfo(player));
			player.decayMe();
			player.spawnMe(activeChar.getX(),activeChar.getY(),activeChar.getZ());
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
		replyMSG.append("</table><br>");
		replyMSG.append("<center><button value=\"Save Changes\" action=\"bypass -h" + " admin_save_modifications $hp $hpmax $mp $mpmax $load $karma $pvpflag $pvpkills $classid\" width=80 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></center><br>");
		replyMSG.append("</body></html>");

		adminReply.setHtml(replyMSG.toString());
		activeChar.sendPacket(adminReply);
	}
	

	//FIXME: needs removal, whole thing needs to use getTarget()
	private void findCharacter(L2PcInstance activeChar, String CharacterToFind)
	{
		Collection<L2PcInstance> allPlayers = L2World.getAllPlayers();
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
