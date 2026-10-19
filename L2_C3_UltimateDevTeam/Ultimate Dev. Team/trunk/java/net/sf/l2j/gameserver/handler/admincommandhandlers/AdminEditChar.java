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
import net.sf.l2j.gameserver.ai.CtrlIntention;
import net.sf.l2j.gameserver.handler.IAdminCommandHandler;
import net.sf.l2j.gameserver.model.Inventory;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.base.ClassId;
import net.sf.l2j.gameserver.packets.serverpackets.*;
import net.sf.l2j.gameserver.tables.ClanTable;
import net.sf.l2j.gameserver.util.Util;

import java.util.Collection;
import java.util.StringTokenizer;
import java.util.logging.Logger;

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
            "admin_setcolor", "admin_remclanwait"
			};
	private static final int REQUIRED_LEVEL = Config.GM_CHAR_EDIT;
	private static final int REQUIRED_LEVEL2 = Config.GM_CHAR_EDIT_OTHER;
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
				L2PcInstance target = L2World.getInstance().getPlayer(val);
				showCharacterList(activeChar, target);
			}
			catch (StringIndexOutOfBoundsException e)
			{
				//Case of empty character name
			}
			
		}
        else if (command.equals("admin_remclanwait"))
        {
            L2Object target = activeChar.getTarget();
            L2PcInstance player = null;
            if (activeChar != target && activeChar.getAccessLevel()<REQUIRED_LEVEL2)
                return false;
            if (target instanceof L2PcInstance) {
                player = (L2PcInstance)target;
            } else {
                return false;
            }

            if (player.getClan() == null)
            {
                player.setDeleteClanTime(0);
                player.sendMessage("A GM Has reset your clan wait time, You may now join another clan.");
                activeChar.sendMessage("You have reset " +player.getName()+ "'s wait time to join another clan.");
		Util.handleLogGmActions(activeChar, "reset " +player.getName()+ "'s wait time to join another clan.");
            }
            else
            {
                activeChar.sendMessage("Sorry, but " +player.getName()+ " must not be in a clan. Player must leave clan before the wait limit can be reset.");
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
				activeChar.sendMessage("You didnt enter a character name to find.");
				
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
				if (activeChar == activeChar.getTarget() || activeChar.getAccessLevel()>=REQUIRED_LEVEL2)
				setTargetKarma(activeChar, karma);
			}
			catch (StringIndexOutOfBoundsException e)
			{
				if ( Config.DEVELOPER ) System.out.println("Set karma error: "+e);
				activeChar.sendMessage("Please specify new karma value.");
			}
		}
		// Enchantment Commands
		else if (command.startsWith("admin_setew"))
		{
			try
			{   
				String val = command.substring(12);
				int ench = Integer.parseInt(val);
				if (activeChar == activeChar.getTarget() || activeChar.getAccessLevel()>=REQUIRED_LEVEL2)
				setTargetEW(activeChar, ench);
			}
			catch (StringIndexOutOfBoundsException e)
			{
				if ( Config.DEVELOPER ) System.out.println("Set weapon enchant error: "+e);
				activeChar.sendMessage("Please specify new enchant value.");
			}
		}
		else if (command.startsWith("admin_save_modifications"))
		{
			try
			{
				String val = command.substring(24); 
				if (activeChar == activeChar.getTarget() || activeChar.getAccessLevel()>=REQUIRED_LEVEL2)
				adminModifyCharacter(activeChar, val);
			}
			catch (StringIndexOutOfBoundsException e)
			{	//Case of empty character name
				activeChar.sendMessage("Error while modifying character.");
				listCharacters(activeChar, 0);
			}			
		}
		else if (command.equals("admin_rec"))
		{
			if (activeChar != activeChar.getTarget() && activeChar.getAccessLevel()<REQUIRED_LEVEL2)
			    return false;
			L2Object target = activeChar.getTarget();
			L2PcInstance player = null;
			if (target instanceof L2PcInstance) {
				player = (L2PcInstance)target;
			} else {
				return false;
			}
			player.setRecomHave(player.getRecomHave() + 1);
			player.sendMessage("You have been recommended by a GM.");
			player.broadcastUserInfo();
		}
		else if (command.startsWith("admin_rec"))
		{
			String val = command.substring(10);
			int recVal = Integer.parseInt(val);
			L2Object target = activeChar.getTarget();
			L2PcInstance player = null;
			if (activeChar != target && activeChar.getAccessLevel()<REQUIRED_LEVEL2)
			    return false;
			if (target instanceof L2PcInstance) {
				player = (L2PcInstance)target;
			} else {
				return false;
			}
            player.setRecomHave(player.getRecomHave() + recVal);
			player.sendMessage("You have been recommended by a GM.");
			player.broadcastUserInfo();
		}
        else if (command.startsWith("admin_settitle"))
        {
            try
            {
                String val = command.substring(15); 
                L2Object target = activeChar.getTarget();
                L2PcInstance player = null;
		if (activeChar != target && activeChar.getAccessLevel()<REQUIRED_LEVEL2)
		    return false;
                if (target instanceof L2PcInstance) {
                    player = (L2PcInstance)target;
                } else {
                    return false;
                }
                player.setTitle(val);
                player.sendMessage("Your title has been changed by a GM");
                player.broadcastUserInfo();
		Util.handleLogGmActions(activeChar, "set title="+val+" to "+player.getName());
            }
            catch (StringIndexOutOfBoundsException e)
            {   //Case of empty character title
                activeChar.sendMessage("You need to specify the new title.");
            }           
        }
        else if (command.startsWith("admin_setname"))
        {
            try
            {
                String val = command.substring(14); 
                L2Object target = activeChar.getTarget();
                L2PcInstance player = null;
		if (activeChar != target && activeChar.getAccessLevel()<REQUIRED_LEVEL2)
		    return false;
                if (target instanceof L2PcInstance) {
                    player = (L2PcInstance)target;
                } else {
                    return false;
                }
                player.setName(val);
                player.sendMessage("Your name has been changed by a GM");
                player.broadcastUserInfo();
		Util.handleLogGmActions(activeChar, "set name="+val+" to "+player.getName());
            }
            catch (StringIndexOutOfBoundsException e)
            {   //Case of empty character name
                activeChar.sendMessage("You need to specify the new name.");
            }           
        }	
        else if (command.startsWith("admin_setsex"))
        {
                L2Object target = activeChar.getTarget();
                L2PcInstance player = null;
		if (activeChar != target && activeChar.getAccessLevel()<REQUIRED_LEVEL2)
		    return false;
                if (target instanceof L2PcInstance) {
                    player = (L2PcInstance)target;
                } else {
                    return false;
                }
                player.changeSex();
                player.sendMessage("Your gender has been changed by a GM");
                player.broadcastUserInfo();
		Util.handleLogGmActions(activeChar, "shange sex to"+player.getName());
        }   
        else if (command.startsWith("admin_setcolor"))
        {
            try
            {
                String val          = command.substring(15); 
                L2Object target     = activeChar.getTarget();
                L2PcInstance player = null;
		if (activeChar != target && activeChar.getAccessLevel()<REQUIRED_LEVEL2)
		    return false;
                if (target instanceof L2PcInstance) {
                    player = (L2PcInstance)target;
                } else {
                    return false;
                }
                player.setNameColor(Integer.decode("0x"+val));
                player.sendMessage("Your name color has been changed by a GM");
                player.broadcastUserInfo();
		Util.handleLogGmActions(activeChar, "set color to"+player.getName());
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
		Collection<L2PcInstance> allPlayers = L2World.getInstance().getAllPlayers();
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
		replyMSG.append("<center><font color=\"LEVEL\">Character Information</font></center>");
		replyMSG.append("<br>");
		
		// Character Player Info
		replyMSG.append("<table width=270>");		
		replyMSG.append("<tr><td width=135>Name: " + player.getName() + "</td><td width=135>Level: " + player.getLevel() + "</td></tr>");	
		replyMSG.append("<tr><td width=135>Clan: " + ClanTable.getInstance().getClan(player.getClanId()) + "</td><td width=135>Exp: " + player.getExp() + "</td></tr>");
        replyMSG.append("<tr><td width=135>Class: " + player.getTemplate().className + "</td><td width=135>SP: " + player.getSp() + "</td></tr>");      
		replyMSG.append("</table>");
        replyMSG.append("<br>");

		// Character ClassID & Coordinates
		replyMSG.append("<table width=270>");
		replyMSG.append("<tr><td width=270>Class Template ID: " + player.getClassId().ordinal() + " (" + player.getClassId() + ")</td></tr>");
		replyMSG.append("<tr><td width=270>Character Co-ordinates: " + player.getX() + " " + player.getY() + " " + player.getZ() + "</td></tr>");
		replyMSG.append("</table>");
		replyMSG.append("<br>");
		
		// Character Stats
		replyMSG.append("<table width=270>");
		replyMSG.append("<tr><td width=40></td><td width=70>Current:</td><td width=70>Maximum:</td><td width=90></td></tr>");
		replyMSG.append("<tr><td width=40>HP:</td><td width=70>" + (int)player.getCurrentHp() + "</td><td width=70>" + (int)player.getMaxHp() + "</td><td width=70>Karma: " + player.getKarma() + "</td><td width=20></td></tr>");
		replyMSG.append("<tr><td width=40>MP:</td><td width=70>" + (int)player.getCurrentMp() + "</td><td width=70>" + (int)player.getMaxMp() + "</td><td width=70>PvP Flag: " + player.getPvpFlag() + "</td><td width=20></td></tr>");
        replyMSG.append("<tr><td width=40>CP:</td><td width=70>" + (int)player.getCurrentCp() + "</td><td width=70>" + (int)player.getMaxCp() + "</td><td width=70>PvP Kills: " + player.getPvpKills() +  "</td><td width=20></td></tr>");
		replyMSG.append("<tr><td width=40>Load:</td><td width=70>" + player.getCurrentLoad() + "</td><td width=70>" + player.getMaxLoad() + "</td><td width=70></td><td width=20></td></tr>");
		replyMSG.append("</table>");
		replyMSG.append("<table width=270>");		
		replyMSG.append("<tr><td width=60>P.ATK: " + player.getPAtk(null) + "</td><td width=100>M.ATK: " + player.getMAtk(null,null) + "</td><td width=40></td></tr>");
		replyMSG.append("<tr><td width=60>P.DEF: " + player.getPDef(null) + "</td><td width=100>M.DEF: " + player.getMDef(null,null) + "</td><td width=40><font color=\"LEVEL\">Manage:</font></td></tr>");
		replyMSG.append("<tr><td width=90>Accuracy: " + player.getAccuracy() + "</td><td width=70>Evasion: " + player.getEvasionRate(null) + "</td><td width=40><button value=\"Skills\" action=\"bypass -h admin_show_skills\" width=60 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
		replyMSG.append("<tr><td width=90>Critical: " + player.getCriticalHit(null,null) + "</td><td width=70>Speed: " + player.getRunSpeed() + "</td><td width=40><button value=\"Stats\" action=\"bypass -h admin_edit_character\" width=60 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
		replyMSG.append("<tr><td width=90>Atk. Spd: " + player.getPAtkSpd() + "</td><td width=70>Casting Spd: " + player.getMAtkSpd() + "</td><td width=40><button value=\"Exp & Sp\" action=\"bypass -h admin_add_exp_sp_to_character\" width=60 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");		
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
				Util.handleLogGmActions(activeChar, "changed the enchantment of "+player.getName()+weapType+" weapon ("+weaponToEnchant.getItem().getName()+") from ("+curEnchant+") to ("+ench+").");
				
				// notify player
				player.sendMessage("Admin has changed the enchantment of your equipped weapon ("+weaponToEnchant.getItem().getName()+") from "+curEnchant+" to "+ench+".");
			}
			else {
				// no weapon is equipped
				// notify admin
				activeChar.sendMessage("Cannot enchant weapon for player "+player.getName()+": No weapon equipped.");
				
				return;
			}
		}
		else {
			// inform our gm of their mistake
			activeChar.sendMessage("You must set the enchant level to be between 0-65535.");			
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
			
			player.sendPacket(new StatusUpdate(player.getObjectId()).addAttribute(StatusUpdate.KARMA, newKarma));
			
		    player.broadcastPacket(new CharInfo(player));
		    player.sendPacket(new UserInfo(player));
			
			//Common character information
			player.sendMessage("Admin has changed your karma from " + oldKarma + " to " + newKarma + ".");
			Util.handleLogGmActions(activeChar, "Changed karma for "+player.getName()+" from (" + oldKarma + ") to (" + newKarma + ").");
			
			//Admin information	
			activeChar.sendMessage("Successfully Changed karma for "+player.getName()+" from (" + oldKarma + ") to (" + newKarma + ").");			
			
			if (Config.DEBUG) 
				_log.fine("[SET KARMA] [GM]"+activeChar.getName()+" Changed karma for "+player.getName()+" from (" + oldKarma + ") to (" + newKarma + ").");
		}
		else {
			// tell admin of mistake 
			activeChar.sendMessage("You must enter a value for karma greater than or equal to 0.");
			
			if (Config.DEBUG) 
				_log.fine("[SET KARMA] ERROR: [GM]"+activeChar.getName()+" entered an incorrect value for new karma: " + newKarma + " for "+player.getName()+".");			
		}
	}
		
	private void adminModifyCharacter(L2PcInstance activeChar, String modifications)
	{
		L2Object target = activeChar.getTarget();
        
		if (!(target instanceof L2PcInstance))
            return;

        L2PcInstance player = (L2PcInstance)target;
		StringTokenizer st = new StringTokenizer(modifications);
        
		if (st.countTokens() != 7) {
			editCharacter(player);
            return;
        }

        String hp = st.nextToken();
        String mp = st.nextToken();
        String cp = st.nextToken();
        String karma = st.nextToken();
        String pvpflag = st.nextToken();
        String pvpkills = st.nextToken();
        String classid = st.nextToken();
            
        int hpval = Integer.parseInt(hp);
        int mpval = Integer.parseInt(mp);
        int cpval = Integer.parseInt(cp);
        int karmaval = Integer.parseInt(karma);
        int pvpflagval = Integer.parseInt(pvpflag);
        int pvpkillsval = Integer.parseInt(pvpkills);
        int classidval = Integer.parseInt(classid);
		    
        //Common character information
        player.sendMessage("Admin has changed your stats." +
                           "  HP: " + hpval + "  MP: " + mpval + "  CP: " + cpval + 
                           "  Karma: " + karmaval + "  PvP: " + pvpflagval + " / " + pvpkillsval + 
                           "  ClassID: " + ClassId.values()[classidval] + " (" + classidval + ")");
		    
        player.setCurrentHp(hpval);
        player.setCurrentMp(mpval);
        player.setCurrentCp(cpval);
        player.setKarma(karmaval);
        player.setPvpFlag(pvpflagval);
        player.setPvpKills(pvpkillsval);
        player.setClassId(classidval);
            
        // Update the base class also if this character is not on a sub-class.
        if (!player.isSubClassActive())
            player.setBaseClass(classidval);
            
        // Save the changed parameters to the database.
        player.store();
		    
        StatusUpdate su = new StatusUpdate(player.getObjectId());
        su.addAttribute(StatusUpdate.CUR_HP, hpval);
        su.addAttribute(StatusUpdate.MAX_HP, player.getMaxHp());
        su.addAttribute(StatusUpdate.CUR_MP, mpval);
        su.addAttribute(StatusUpdate.MAX_MP, player.getMaxMp());
        su.addAttribute(StatusUpdate.CUR_CP, cpval);
        su.addAttribute(StatusUpdate.MAX_CP, player.getMaxCp());
        su.addAttribute(StatusUpdate.KARMA, karmaval);
        su.addAttribute(StatusUpdate.PVP_FLAG, pvpflagval);
        player.sendPacket(su);
		    
        //Admin information	
        player.sendMessage("Changed stats of " + player.getName() + "." +
                           "  HP: " + hpval + "  MP: " + mpval + "  CP: " + cpval + 
                           "  Karma: " + karmaval + "  PvP: " + pvpflagval + " / " + pvpkillsval + 
                           "  ClassID: " + ClassId.values()[classidval] + " (" + classidval + ")");
            
        if (Config.DEBUG) 
            _log.fine("[GM]"+activeChar.getName()+" changed stats of "+player.getName()+". " +
                      " HP: "+hpval+" MP: "+mpval+" CP: " + cpval + " Karma: "+karmaval+
                      " PvP: "+pvpflagval+" / "+pvpkillsval+ " ClassID: "+classidval);
	    Util.handleLogGmActions(activeChar,"changed stats of "+player.getName()+". " +
                      " HP: "+hpval+" MP: "+mpval+" CP: " + cpval + " Karma: "+karmaval+
                      " PvP: "+pvpflagval+" / "+pvpkillsval+ " ClassID: "+classidval);
		    
        showCharacterList(activeChar, null); //Back to start
            
        player.broadcastPacket(new CharInfo(player));
        player.sendPacket(new UserInfo(player));
            
        player.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
        
        player.decayMe();
        player.spawnMe(activeChar.getX(),activeChar.getY(),activeChar.getZ());
	}

	private void editCharacter(L2PcInstance activeChar)
	{
		///FIXME Made it so that you have to enter all values to 'prevent' abuses...
		L2Object target = activeChar.getTarget();
        
		if (!(target instanceof L2PcInstance)) 
			return;

        L2PcInstance player = (L2PcInstance)target;
		NpcHtmlMessage adminReply = new NpcHtmlMessage(5); 
        final int loadPercent = (int)Math.round((long)(player.getCurrentLoad() / player.getMaxLoad()) * 100);
        
		StringBuffer replyMSG = new StringBuffer("<html><body>");
		replyMSG.append("<table width=260><tr>");
		replyMSG.append("<td width=40><button value=\"Main\" action=\"bypass -h admin_admin\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
		replyMSG.append("<td width=180><center>Character Selection Menu</center></td>");
		replyMSG.append("<td width=40><button value=\"Back\" action=\"bypass -h admin_current_player\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
		replyMSG.append("</tr></table>");
		replyMSG.append("<br><br>");
		replyMSG.append("<center>Editing <font color=\"LEVEL\">" + player.getName() + "</font></center><br>");
		replyMSG.append("<table width=270>");
		replyMSG.append("<tr><td width=40></td><td width=70>Current:</td><td width=70>Maximum:</td><td width=90></td></tr>");
		replyMSG.append("<tr><td width=40>HP:</td><td width=70>" + (int)player.getCurrentHp() + "</td><td width=70>" + player.getMaxHp() + "</td><td width=70>Karma: " + player.getKarma() + "</td><td width=20></td></tr>");
		replyMSG.append("<tr><td width=40>MP:</td><td width=70>" + (int)player.getCurrentMp() + "</td><td width=70>" + player.getMaxMp() + "</td><td width=70>PvP Kills: " + player.getPvpKills() +  "</td><td width=20></td></tr>");
        replyMSG.append("<tr><td width=40>CP:</td><td width=70>" + (int)player.getCurrentCp() + "</td><td width=70>" + player.getMaxCp() + "</td><td width=70>PvP Flag: " + player.getPvpFlag() + "</td><td width=20></td></tr>");
		replyMSG.append("<tr><td width=40>Load:</td><td width=70>" + player.getCurrentLoad() + "</td><td width=70>" + player.getMaxLoad() + "</td><td width=90>(" + loadPercent + "%)</td></tr>");
		replyMSG.append("</table>");
		replyMSG.append("<table width=270><tr><td>Class Template ID: " + player.getClassId().ordinal() + " (" + player.getClassId() + ")</td></tr></table><br>");
		replyMSG.append("<table width=270>");
		replyMSG.append("<tr><td>Note: Fill all values before saving the modifications.</td></tr>");
		replyMSG.append("</table><br>");
		replyMSG.append("<table width=270>");
		replyMSG.append("<tr><td width=50>Current HP:</td><td><edit var=\"hp\" width=50></td><td width=50>Karma:</td><td><edit var=\"karma\" width=50></td></tr>");
		replyMSG.append("<tr><td width=50>Current MP:</td><td><edit var=\"mp\" width=50></td><td width=50>PvP Flag:</td><td><edit var=\"pvpflag\" width=50></td></tr>");
        replyMSG.append("<tr><td width=50>Current CP:</td><td><edit var=\"cp\" width=50></td><td width=50>PvP Kills:</td><td><edit var=\"pvpkills\" width=50></td></tr>");
		replyMSG.append("<tr><td width=50>Class ID:</td><td><edit var=\"classid\" width=50></td><td width=50></td><td width=50></td></tr>");
        replyMSG.append("</table><br>");
		replyMSG.append("<center><button value=\"Save Changes\" action=\"bypass -h" + " admin_save_modifications $hp $mp $cp $karma $pvpflag $pvpkills $classid\" width=80 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></center><br>");
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
