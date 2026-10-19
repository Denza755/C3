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
import net.sf.l2j.gameserver.model.Inventory;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.packets.serverpackets.CharInfo;
import net.sf.l2j.gameserver.packets.serverpackets.InventoryUpdate;
import net.sf.l2j.gameserver.packets.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.packets.serverpackets.UserInfo;
import net.sf.l2j.gameserver.util.Util;

import java.util.StringTokenizer;

/**
 * This class handles following admin commands:
 * - enchant_armor
 * 
 * @version $Revision: 1.3.2.1.2.10 $ $Date: 2005/08/24 21:06:06 $
 */
public class AdminEncArmor implements IAdminCommandHandler {
	private static String[] _adminCommands = {
		"admin_seteh",// Head
		"admin_setec",// Chest
		"admin_seteg",// Gloves
		"admin_setel",// Legs
		"admin_seteb",// Body
		"admin_setes",// Left Hand (Shield)
		"admin_setle",// Left Ear
		"admin_setre",// Right Ear
		"admin_setlf",// Left Finger
		"admin_setrf",// Right Finger
		"admin_seten",// Neck
		"admin_setun",// Under Wear
		"admin_setba",// Boot
		"admin_enchant",
	};
	private static final int REQUIRED_LEVEL = Config.GM_ENCHANT;

	public boolean useAdminCommand(String command, L2PcInstance activeChar) 
	{
		if (!(checkLevel(activeChar.getAccessLevel()) && activeChar.isGM())) 
			return false;
		
		if (command.equals("admin_enchant")) showMainPage(activeChar);
		else
		{
			StringTokenizer st = new StringTokenizer(command);
			st.nextToken();
		
			if (command.startsWith("admin_seteh"))
			{	
				try
				{   
					int ench = Integer.parseInt(st.nextToken());
					setTarget(activeChar, ench ,Inventory.PAPERDOLL_HEAD);
				}
				catch (StringIndexOutOfBoundsException e)
				{
					activeChar.sendMessage("Please specify new enchant value.");
				}
			}	
			else if (command.startsWith("admin_setec"))
			{
				try
				{   
					int ench = Integer.parseInt(st.nextToken());
					setTarget(activeChar, ench ,Inventory.PAPERDOLL_CHEST);
				}
				catch (StringIndexOutOfBoundsException e)
				{
					activeChar.sendMessage("Please specify new enchant value.");
				}
			}	
			else if (command.startsWith("admin_seteg"))
			{
				try
				{   
					int ench = Integer.parseInt(st.nextToken());
					setTarget(activeChar, ench ,Inventory.PAPERDOLL_GLOVES);
				}
				catch (StringIndexOutOfBoundsException e)
				{
					activeChar.sendMessage("Please specify new enchant value.");
				}
			}
			else if (command.startsWith("admin_seteb"))
			{
				try
				{   
					int ench = Integer.parseInt(st.nextToken());
					setTarget(activeChar, ench ,Inventory.PAPERDOLL_FEET);
				}
				catch (StringIndexOutOfBoundsException e)
				{
					activeChar.sendMessage("Please specify new enchant value.");
				}
			}
			else if (command.startsWith("admin_setel"))
			{
				try
				{   
					int ench = Integer.parseInt(st.nextToken());
					setTarget(activeChar, ench ,Inventory.PAPERDOLL_LEGS);
				}
				catch (StringIndexOutOfBoundsException e)
				{
					activeChar.sendMessage("Please specify new enchant value.");
				}
			}
			else if (command.startsWith("admin_setes"))
			{
				try
				{   
					int ench = Integer.parseInt(st.nextToken());
					setTarget(activeChar, ench ,Inventory.PAPERDOLL_LHAND);
				}
				catch (StringIndexOutOfBoundsException e)
				{
					activeChar.sendMessage("Please specify new enchant value.");
				}
			}
			else if (command.startsWith("admin_setle"))
			{
				try
				{   
					int ench = Integer.parseInt(st.nextToken());
					setTarget(activeChar, ench ,Inventory.PAPERDOLL_LEAR);
				}
				catch (StringIndexOutOfBoundsException e)
				{
					activeChar.sendMessage("Please specify new enchant value.");
				}
			}
			else if (command.startsWith("admin_setre"))
			{
				try
				{   
					int ench = Integer.parseInt(st.nextToken());
					setTarget(activeChar, ench ,Inventory.PAPERDOLL_REAR);
				}
				catch (StringIndexOutOfBoundsException e)
				{
					activeChar.sendMessage("Please specify new enchant value.");
				}
			}
			else if (command.startsWith("admin_setlf"))
			{
				try
				{   
					int ench = Integer.parseInt(st.nextToken());
					setTarget(activeChar, ench ,Inventory.PAPERDOLL_LFINGER);
				}
				catch (StringIndexOutOfBoundsException e)
				{
					activeChar.sendMessage("Please specify new enchant value.");
				}
			}
			else if (command.startsWith("admin_setrf"))
			{
				try
				{   
					int ench = Integer.parseInt(st.nextToken());
					setTarget(activeChar, ench ,Inventory.PAPERDOLL_RFINGER);
				}
				catch (StringIndexOutOfBoundsException e)
				{
					activeChar.sendMessage("Please specify new enchant value.");
				}
			}
			else if (command.startsWith("admin_seten"))
			{
				try
				{   
					int ench = Integer.parseInt(st.nextToken());
					setTarget(activeChar, ench ,Inventory.PAPERDOLL_NECK);
				}
				catch (StringIndexOutOfBoundsException e)
				{
					activeChar.sendMessage("Please specify new enchant value.");
				}
			}
			else if (command.startsWith("admin_setun"))
			{
				try
				{   
					int ench = Integer.parseInt(st.nextToken());
					setTarget(activeChar, ench ,Inventory.PAPERDOLL_UNDER);
				}
				catch (StringIndexOutOfBoundsException e)
				{
					activeChar.sendMessage("Please specify new enchant value.");
				}
			}
			else if (command.startsWith("admin_setba"))
			{
				try
				{   
					int ench = Integer.parseInt(st.nextToken());
					setTarget(activeChar, ench ,Inventory.PAPERDOLL_BACK);
				}
				catch (StringIndexOutOfBoundsException e)
				{
					activeChar.sendMessage("Please specify new enchant value.");
				}
			}
		}
			return true;
	}
	
	private void setTarget(L2PcInstance activeChar, int ench ,int armorType)
	{
		// set target
		L2Object target = activeChar.getTarget();
		if (target == null)
			target = activeChar;
		L2PcInstance player = null;
		if (target instanceof L2PcInstance) 
		{
			player = (L2PcInstance)target;
		} 
		else 
		{
			return;
		}
		
		// check value
		if ( ench >= 0 && ench <= 65535 ) 
		{
			// now we need to find the equipped weapon of the targeted character...
			int curEnchant = 0; // display purposes only
			int dropSlot = armorType;
			boolean canEnchant = false;
			L2ItemInstance parmorInstance = null;
			L2ItemInstance armorToEnchant = null;
			
			// only attempt to enchant if there is a weapon equipped
			if ( canEnchant == false )
			{
				// check equip
				parmorInstance = player.getInventory().getPaperdollItem(dropSlot);
				if (parmorInstance != null && parmorInstance.getEquipSlot() == dropSlot ) 
				{ 		
					armorToEnchant = parmorInstance;
					curEnchant = armorToEnchant.getEnchantLevel();
					canEnchant = true;
				}
			}
			
			if ( canEnchant == true && armorToEnchant != null ) {
				// set enchant value
				player.getInventory().unEquipItemInSlotAndRecord(dropSlot);
				parmorInstance.setEnchantLevel(ench);							    
				player.getInventory().equipItemAndRecord(armorToEnchant);
				
				InventoryUpdate iu = new InventoryUpdate();	
				iu.addModifiedItem(armorToEnchant);
				player.sendPacket(iu);
				
				player.broadcastPacket(new CharInfo(player));
				player.sendPacket(new UserInfo(player));
			    
				// information
				activeChar.sendMessage("Changed enchantment of equipped item ("+armorToEnchant.getItem().getName()+") from ("+curEnchant+") to ("+ench+").");
				player.sendMessage("Admin has changed the enchantment of your equipped item ("+armorToEnchant.getItem().getName()+") from "+curEnchant+" to "+ench+".");
				Util.handleLogGmActions(activeChar, "Changed enchantment of equipped item ("+armorToEnchant.getItem().getName()+") from ("+curEnchant+") to ("+ench+"). to "+player.getName());
			}
			else {
				// no item is equipped
				// notify admin
				activeChar.sendMessage("Cannot enchant item for player "+player.getName()+": No item equipped.");
				return;
			}

		}
		else 
		{
			// inform our gm of their mistake
			activeChar.sendMessage("You must set the enchant level to be between 0-65535.");			
		}
	}
	
	public void showMainPage(L2PcInstance activeChar)
	{
		NpcHtmlMessage adminReply = new NpcHtmlMessage(5);

		StringBuffer replyMSG = new StringBuffer("<html><body>");
		replyMSG.append("<center><table width=260><tr><td width=40>");
		replyMSG.append("<button value=\"Main\" action=\"bypass -h admin_admin\" width=45 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
		replyMSG.append("</td><td width=180>");
		replyMSG.append("<center>Enchant Equip</center>");
		replyMSG.append("</td><td width=40>");
		replyMSG.append("</td></tr></table></center><br>");
		replyMSG.append("<center><table width=270><tr><td>");
		replyMSG.append("<button value=\"Underwear\" action=\"bypass -h admin_setun $menu_command\" width=50 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td><td>");
		replyMSG.append("<button value=\"Helmet\" action=\"bypass -h admin_seteh $menu_command\" width=50 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td><td>");		
		replyMSG.append("<button value=\"Cloak\" action=\"bypass -h admin_setba $menu_command\" width=50 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td><td>");
		replyMSG.append("<button value=\"Mask\" width=50 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td><td>");
		replyMSG.append("<button value=\"Necklace\" action=\"bypass -h admin_seten $menu_command\" width=50 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr></table>");
		replyMSG.append("</center><center><table width=270><tr><td>");
		replyMSG.append("<button value=\"Weapon\" action=\"bypass -h admin_setew $menu_command\" width=50 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td><td>");
		replyMSG.append("<button value=\"Chest\" action=\"bypass -h admin_setec $menu_command\" width=50 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td><td>");		
		replyMSG.append("<button value=\"Shield\" action=\"bypass -h admin_setes $menu_command\" width=50 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td><td>");
		replyMSG.append("<button value=\"Earring\" action=\"bypass -h admin_setre $menu_command\" width=50 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td><td>");
		replyMSG.append("<button value=\"Earring\" action=\"bypass -h admin_setle $menu_command\" width=50 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr></table>");
		replyMSG.append("</center><center><table width=270><tr><td>");
		replyMSG.append("<button value=\"Gloves\" action=\"bypass -h admin_seteg $menu_command\" width=50 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td><td>");
		replyMSG.append("<button value=\"Leggings\" action=\"bypass -h admin_setel $menu_command\" width=50 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td><td>");		
		replyMSG.append("<button value=\"Boots\" action=\"bypass -h admin_seteb $menu_command\" width=50 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td><td>");
		replyMSG.append("<button value=\"Ring\" action=\"bypass -h admin_setrf $menu_command\" width=50 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td><td>");
		replyMSG.append("<button value=\"Ring\" action=\"bypass -h admin_setlf $menu_command\" width=50 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr></table>");
		replyMSG.append("</center><br>");
		replyMSG.append("<center>[Enchant 0-65535]</center>");
		replyMSG.append("<center><edit var=\"menu_command\" width=100 height=15></center><br>");
		replyMSG.append("</body></html>");

		adminReply.setHtml(replyMSG.toString());
		activeChar.sendPacket(adminReply); 
	}

	public String[] getAdminCommandList() {
		return _adminCommands;
	}

	private boolean checkLevel(int level) {	
		return (level >= REQUIRED_LEVEL);
	}
}
