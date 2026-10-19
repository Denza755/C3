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

import java.util.logging.Logger;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.serverpackets.WareHouseDepositList;
import net.sf.l2j.gameserver.serverpackets.WareHouseWithdrawalList;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;

/**
 * This class ...
 * 
 * @version $Revision: 1.3.4.10 $ $Date: 2005/04/06 16:13:41 $
 */
public final class L2WarehouseInstance extends L2FolkInstance
{
	private static Logger _log = Logger.getLogger(L2WarehouseInstance.class.getName());
	
	/**
	 * @param template
	 */
	public L2WarehouseInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
	}
	
	public void onAction(L2PcInstance player)
	{
		if (Config.DEBUG) _log.fine("Warehouse activated");
		player.setLastFolkNPC(this);
		super.onAction(player);
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
		return "data/html/warehouse/" + pom + ".htm";
	}

	
	private void showRetrieveWindow(L2PcInstance player)
	{	
        player.setUsingClanWarehouse(false);
		if (Config.DEBUG) _log.fine("Showing stored items");
		//model
		Warehouse list = player.getWarehouse();
		
		if (list != null)
		{	
			//serverpacket
			WareHouseWithdrawalList wl  = new WareHouseWithdrawalList(player);
			player.sendPacket(wl);
		}
		else
		{
			_log.warning("no items stored");
		}
		
		player.sendPacket(new ActionFailed());
	}
	
	private void showDepositWindow(L2PcInstance player)
	{
        player.setUsingClanWarehouse(false);
		player.tempInvetoryDisable();
		if (Config.DEBUG) _log.fine("Showing items to deposit");
		
		WareHouseDepositList dl  = new WareHouseDepositList(player);
		player.sendPacket(dl);		
		
		player.sendPacket(new ActionFailed());
	}
	
	private void showDepositWindowClan(L2PcInstance player)
	{
        player.setUsingClanWarehouse(true);
        if (player.getClan() != null)
        {
            if (player.getClan().getLevel() == 0)
            {
                player.sendPacket(new SystemMessage(SystemMessage.ONLY_LEVEL_1_CLAN_OR_HIGHER_CAN_USE_WAREHOUSE));
            }
            else
            {
                player.sendPacket(new SystemMessage(SystemMessage.ONLY_CLAN_LEADER_CAN_RETRIEVE_ITEMS_FROM_CLAN_WAREHOUSE));
                player.tempInvetoryDisable();
                if (Config.DEBUG) _log.fine("Showing items to deposit - clan");
                
                WareHouseDepositList dl  = new WareHouseDepositList(player);
                player.sendPacket(dl);
            }
        }
		
		player.sendPacket(new ActionFailed());
	}

	private void showWithdrawWindowClan(L2PcInstance player)
	{
        player.setUsingClanWarehouse(true);
        if (player.getClan() != null) //FIXME this should be available only to authorized clan members
        {
            if (player.getClan().getLevel() == 0)
            {
                player.sendPacket(new SystemMessage(SystemMessage.ONLY_LEVEL_1_CLAN_OR_HIGHER_CAN_USE_WAREHOUSE));
            }
            else
            {
                Warehouse list = player.getClan().getWarehouse();
                if (list != null)
                {
                	if (Config.DEBUG) _log.fine("Showing items to deposit - clan");
                    WareHouseWithdrawalList wl  = new WareHouseWithdrawalList(player, player.getClan());
                    player.sendPacket(wl);
                }
            }
        }
        else
        {
            _log.warning("no items stored");
        }

		player.sendPacket(new ActionFailed());
	}

	public void onBypassFeedback(L2PcInstance player, String command)
	{
		if (command.startsWith("WithdrawP"))
		{
			showRetrieveWindow(player);
		} else if (command.equals("DepositP"))
		{
			showDepositWindow(player);
		}
		else if (command.equals("WithdrawC"))
		{
			showWithdrawWindowClan(player);
		}
		else if (command.equals("DepositC"))
		{
			showDepositWindowClan(player);
		}
		else 
		{
			// this class dont know any other commands, let forward
			// the command to the parent class
			
			super.onBypassFeedback(player, command);
		}
	}
}
