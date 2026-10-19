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

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.handler.IAdminCommandHandler;
import net.sf.l2j.gameserver.model.L2NpcInstance;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2PcInstance;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;

/**
 * Admin Command Handler for Mammon NPCs
 * 
 * @author Tempy
 */
public class AdminMammon implements IAdminCommandHandler {

	private static String[] _adminCommands = {"admin_find_mammon", "admin_show_mammon", "admin_hide_mammon", "admin_list_spawns"};
	private static final int REQUIRED_LEVEL = Config.GM_MENU;

	ArrayList<Integer> npcIds = new ArrayList<Integer>();
	
	public boolean useAdminCommand(String command, L2PcInstance activeChar) {
		    npcIds.clear();
		
		if (!(checkLevel(activeChar.getAccessLevel()) && activeChar.isGM())) return false;
		
		else if (command.startsWith("admin_find_mammon"))
		{
			npcIds.add(8113);
			npcIds.add(8126); 
			npcIds.add(8092); // Add the Marketeer of Mammon also
			int teleportIndex = -1;
			
			try {
				if (command.length() > 16)
					teleportIndex = Integer.parseInt(command.substring(18));
			}
			catch (Exception NumberFormatException)	{
				//activeChar.sendPacket(SystemMessage.sendString("Command format is //find_mammon <teleportIndex>"));
			}

			findAdminNPCs(activeChar, npcIds, teleportIndex, -1);
		}
		
		else if (command.equals("admin_show_mammon")) 
		{
			npcIds.add(8113);
			npcIds.add(8126); 
			
			findAdminNPCs(activeChar, npcIds, -1, 1);
		}
		
		else if (command.equals("admin_hide_mammon"))
		{
			npcIds.add(8113);
			npcIds.add(8126); 
			
			findAdminNPCs(activeChar, npcIds, -1, 0);
		}
		
		else if (command.startsWith("admin_list_spawns"))
		{
			int npcId = 0;
			
			try {
				npcId = Integer.parseInt(command.substring(18).trim());
			}
			catch (Exception NumberFormatException)	{
				activeChar.sendPacket(SystemMessage.sendString("Command format is //list_spawns <NPC_ID>"));
			}

			npcIds.add(npcId);
			findAdminNPCs(activeChar, npcIds, -1, -1);
		}
		
		// Used for testing SystemMessage IDs	- Use //msg <ID>
		else if (command.startsWith("admin_msg"))
		{
			int msgId = Integer.parseInt(command.substring(10).trim());
		
			activeChar.sendPacket(new SystemMessage(msgId));
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

	public void findAdminNPCs(L2PcInstance activeChar, ArrayList<Integer> npcIdList, int teleportIndex, int makeVisible)
	{
		int index = 0;
		
		ConcurrentHashMap map = L2World.getAllVisibleObjects();

		for(Object object : map.values()) 
		{
			L2Object obj = (L2Object)object;

			if (obj instanceof L2NpcInstance) {
				L2NpcInstance npcInst = (L2NpcInstance)obj;
				int npcId = npcInst.getNpcId();
				
				if (npcIdList.contains(npcId)) {
					if (makeVisible == 1) 
						npcInst.spawnMe();
					else if (makeVisible == 0)
						npcInst.decayMe();
						
					if (npcInst.isVisible()) {
						index++;
						
						if (teleportIndex > -1) {
							if (teleportIndex == index)
								activeChar.teleToLocation(npcInst.getX(), npcInst.getY(), npcInst.getZ());
						}
						else {
							activeChar.sendPacket(SystemMessage.sendString(index + " - " + npcInst.getName() + " (" + npcInst.getObjectId() + "): " + npcInst.getX() + " " + npcInst.getY() + " " + npcInst.getZ()));
						}
					}
				}
			}
		}
	}
}
