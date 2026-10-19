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
package net.sf.l2j.gameserver.model;

import java.sql.PreparedStatement;
import java.util.logging.Logger;

import net.sf.l2j.Config;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.ClanTable;
import net.sf.l2j.gameserver.serverpackets.ItemList;
import net.sf.l2j.gameserver.serverpackets.PledgeShowInfoUpdate;
import net.sf.l2j.gameserver.serverpackets.PledgeStatusChanged;
import net.sf.l2j.gameserver.serverpackets.StatusUpdate;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.serverpackets.UserInfo;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;

/**
 * This class ...
 * 
 * @version $Revision: 1.4.2.3.2.8 $ $Date: 2005/03/29 23:15:15 $
 */
public final class L2VillageMasterInstance extends L2FolkInstance
{
	private static Logger _log = Logger.getLogger(L2VillageMasterInstance.class.getName());
	
	/**
	 * @param template
	 */
	public L2VillageMasterInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
	}
	
	public void onBypassFeedback(L2PcInstance player, String command)
	{
		// first do the common stuff
		// and handle the commands that all NPC classes know
		super.onBypassFeedback(player, command);
		
		if (command.startsWith("create_clan"))
		{
			String val = command.substring(12);
			createClan(player, val);
		} 
        else if (command.startsWith("dissolve_clan"))
        {
			dissolveClan(player, player.getClanId());
        }
        else if (command.startsWith("increase_clan_level"))
		{
			levelUpClan(player, player.getClanId());
		}
		else 
		{
			// this class dont know any other commands, let forward
			// the command to the parent class
			
			super.onBypassFeedback(player, command);
		}
	}
	
	public String getHtmlPath(int npcId, int val)
	{
		String pom = "";
		if (val == 0)
		{
			pom = "" + npcId;
		} 
		else 
		{
			pom = npcId + "-" + val;
		}
		
		return "data/html/villagemaster/" + pom + ".htm";
	}

	
	public void onAction(L2PcInstance player)
	{
		if (Config.DEBUG) _log.fine("Village Master activated");
		super.onAction(player);
	}
	
	//Private stuff
	public void createClan(L2PcInstance player, String clanName)
	{
		if (Config.DEBUG)
			_log.fine(player.getObjectId()+"("+player.getName()+
					") requested clan creation from "+getObjectId()+"("+getName()+")");
		if (player.getLevel() < 10)
		{
			SystemMessage sm = new SystemMessage(SystemMessage.FAILED_TO_CREATE_CLAN);
			player.sendPacket(sm);
			return;
		}
		
		if (player.getClanId() != 0)
		{
			SystemMessage sm = new SystemMessage(SystemMessage.FAILED_TO_CREATE_CLAN);
			player.sendPacket(sm);
			return;
		}

		if (clanName.length() > 16)
		{
			SystemMessage sm = new SystemMessage(SystemMessage.CLAN_NAME_TOO_LONG);
			player.sendPacket(sm);
			return;
		}
		
		L2Clan clan = ClanTable.getInstance().createClan(player, clanName);
		if (clan == null)
		{
			// clan name is already taken
			SystemMessage sm = new SystemMessage(SystemMessage.CLAN_NAME_INCORRECT);
			player.sendPacket(sm);
			return;
		}
		
		//should be update packet only
		PledgeShowInfoUpdate pu = new PledgeShowInfoUpdate(clan, player);
		player.sendPacket(pu);

		UserInfo ui = new UserInfo(player);
		player.sendPacket(ui);

		SystemMessage sm = new SystemMessage(SystemMessage.CLAN_CREATED);
		player.sendPacket(sm);
	}
    
    private void dissolveClan(L2PcInstance player, int clanId)
    {
		if (player.getClan() != null && player.getObjectId() != player.getClan().getLeaderId())
		{
            SystemMessage sm = new SystemMessage(614);
            sm.addString("Only clan leader can dissolve a clan."); //i was too lazy to check if such a sysmsg already exists
            player.sendPacket(sm);
			return;
        }
		
        L2PcInstance[] clanMembers = player.getClan().getOnlineMembers(player.getName());
        player.setClan(null);
        player.setTitle(null);
        SystemMessage sm = new SystemMessage(614);
        sm.addString("The clan has been dissolved."); //i was too lazy to check if such a sysmsg already exists
        
        
        for (int i = 0; i < clanMembers.length; i++)
        {
            clanMembers[i].setClan(null);
            clanMembers[i].setTitle(null);
            clanMembers[i].sendPacket(sm);

            clanMembers[i].broadcastUserInfo();
        }
        
        java.sql.Connection con = null;
        try
        {
            con = L2DatabaseFactory.getInstance().getConnection();
            PreparedStatement statement = con.prepareStatement("UPDATE characters SET clanid = 0 WHERE clanid=?");
            statement.setInt(1, clanId);
            statement.execute();
            statement.close();
            
            statement = con.prepareStatement("DELETE FROM clan_data WHERE clan_id=?");
            statement.setInt(1, clanId);
            statement.execute();
            statement.close();
            player.sendPacket(sm);
            player.broadcastUserInfo();
            
            con.close();
        }
        catch (Exception e)
        {
            _log.warning("could not dissolve clan:"+e);
        } 
        finally 
        {
            try { con.close(); } catch (Exception e) {}
        }
    }
	
	public void levelUpClan(L2PcInstance player, int clanId)
	{
		L2Clan clan = player.getClan();
		if (clan == null)
		{
			return;
		}
		if (player.getObjectId() != player.getClan().getLeaderId())
		{
            SystemMessage sm = new SystemMessage(614);
            sm.addString("Only clan leader can increase the clan level."); //i was too lazy to check if such a sysmsg already exists
            player.sendPacket(sm);
			return;
        }
		
		boolean increaseClanLevel = false;
		
		switch (clan.getLevel())
		{
			case 0:
			{
				// upgrade to 1
				if (player.getSp() >= 35000 && player.getAdena() >= 650000)
				{
					player.setSp(player.getSp()-35000);
					player.reduceAdena(650000);
					increaseClanLevel = true;
				}
				break;
			}
			case 1:
			{
				// upgrade to 2
				if (player.getSp() >= 150000 && player.getAdena() >= 2500000)
				{
					player.setSp(player.getSp()-150000);
					player.reduceAdena(2500000);
					increaseClanLevel = true;
				}
				break;
			}
			case 2:
			{
				// upgrade to 3
				if (player.getSp() >= 500000 && player.getInventory().findItemByItemId(1419) != null)
				{
					// itemid 1419 == proof of blood
					player.setSp(player.getSp()-500000);
					player.getInventory().destroyItemByItemId(1419, 1);
					increaseClanLevel = true;
				}
				break;
			}
			case 3:
			{
				// upgrade to 4
				if (player.getSp() >= 1400000 && (player.getInventory().findItemByItemId(3874) != null || player.getAdena() >= 16000000))
				{
					// itemid 3874 == proof of alliance
					player.setSp(player.getSp()-1400000);
					if (player.getInventory().findItemByItemId(3874) != null)
					{
						player.getInventory().destroyItemByItemId(3874, 1);
					}
					else
					{
						player.reduceAdena(16000000);
					}
					increaseClanLevel = true;
				}
				break;
			}
			case 4:
			{
				// upgrade to 5
				if (player.getSp() >= 3500000 && (player.getInventory().findItemByItemId(3870) != null || player.getAdena() >= 30000000))
				{
					// itemid 3870 == proof of aspiration
					player.setSp(player.getSp()-3500000);
					if (player.getInventory().findItemByItemId(3870) != null)
					{
						player.getInventory().destroyItemByItemId(3870, 1);
					}
					else
					{
						player.reduceAdena(30000000);
					}
					increaseClanLevel = true;
				}
				break;
			}
		}
		
		if (increaseClanLevel)
		{
			// the player should know that he has less sp now :p
			StatusUpdate su = new StatusUpdate(player.getObjectId());
			su.addAttribute(StatusUpdate.SP, player.getSp());
			sendPacket(su);
			
			ItemList il = new ItemList(player, false);
			player.sendPacket(il);
			
			java.sql.Connection con = null;
	        try
	        {
	            con = L2DatabaseFactory.getInstance().getConnection();
	            PreparedStatement statement = con.prepareStatement("UPDATE clan_data SET clan_level = ? WHERE clan_id = ?");
				statement.setInt(1, clan.getLevel()+1);
				statement.setInt(2, clanId);
	            statement.execute();
	            statement.close();
	            
	            con.close();
	        }
	        catch (Exception e)
	        {
	            _log.warning("could not increase clan level:"+e);
	        } 
	        finally 
	        {
	            try { con.close(); } catch (Exception e) {}
	        }
			
			clan.setLevel(clan.getLevel()+1);
			// notify all the members about it
			SystemMessage sm = new SystemMessage(SystemMessage.CLAN_LEVEL_INCREASED);
			player.sendPacket(sm);
			L2PcInstance[] members = clan.getOnlineMembers(player.getName());
			for (int i = 0; i < members.length; i++)
			{
				members[i].sendPacket(sm);
			}
			clan.broadcastToOnlineMembers(new PledgeStatusChanged(clan));
		}
		else
		{
			SystemMessage sm = new SystemMessage(SystemMessage.FAILED_TO_INCREASE_CLAN_LEVEL);
			player.sendPacket(sm);
		}
	}
}
