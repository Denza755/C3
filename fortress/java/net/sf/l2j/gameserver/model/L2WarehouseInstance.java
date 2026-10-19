/*
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2, or (at your option) any later version. This
 * program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place - Suite 330, Boston, MA 02111-1307, USA.
 * http://www.gnu.org/copyleft/gpl.html
 */
package net.sf.l2j.gameserver.model;

import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Logger;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.GameServer;
import net.sf.l2j.gameserver.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.serverpackets.NpcHtmlMessage;
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

    @SuppressWarnings("static-access")
	public void onAction(L2PcInstance player)
    {
        if(GameServer.events.onAction(player, this)) return;

        if(Config.DEBUG) _log.fine("Warehouse activated");
        player.setLastFolkNPC(this);
        super.onAction(player);
    }

    public String getHtmlPath(int npcId, int val)
    {
        String pom = "";
        if(val == 0)
        {
            pom = "" + npcId;
        }
        else
        {
            pom = npcId + "-" + val;
        }
        return "data/html/warehouse/" + pom + ".htm";
    }

    private void showRetrieveWindow(L2PcInstance player, int val)
    {
        player.setUsingClanWarehouse(false);
        player.setUsingFreight(false);
        if(Config.DEBUG) _log.fine("Showing stored items");
        // model

        Warehouse list = new PcWarehouse(player, val);
        //Warehouse list = player.getWarehouse();

        if(list != null)
        {
            player.sendPacket(new WareHouseWithdrawalList(player));
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
        player.setUsingFreight(false);
        player.tempInvetoryDisable();
        if(Config.DEBUG) _log.fine("Showing items to deposit");

        player.sendPacket(new WareHouseDepositList(player));
        player.sendPacket(new ActionFailed());
    }

    private void showDepositWindowClan(L2PcInstance player)
    {
        if(player.getClan() == null)
        {
            player.sendPacket(new ActionFailed());
            return;
        }

        if(player.getClan().getLevel() == 0)
        {
            player.sendPacket(new SystemMessage(SystemMessage.ONLY_CLANS_OF_CLAN_LEVEL_1_OR_HIGHER_CAN_USE_A_CLAN_WAREHOUSE));
            player.sendPacket(new ActionFailed());
            return;
        }

        player.setUsingClanWarehouse(true);
        player.setUsingFreight(false);
        player.tempInvetoryDisable();

        if(Config.DEBUG) _log.fine("Showing items to deposit - clan");

        player.sendPacket(new WareHouseDepositList(player, player.getClan()));
    }

    private void showWithdrawWindowClan(L2PcInstance player, int val)
    {
        if(player.getClan() == null)
        {
            player.sendPacket(new ActionFailed());
            return;
        }

        if(player.getClan().getLevel() == 0)
        {
            player.sendPacket(new SystemMessage(SystemMessage.ONLY_CLANS_OF_CLAN_LEVEL_1_OR_HIGHER_CAN_USE_A_CLAN_WAREHOUSE));
            player.sendPacket(new ActionFailed());
            return;
        }

        //Warehouse clanwh = player.getClan().getWarehouse();
        L2Clan _clan = player.getClan();
        Warehouse clanwh = new ClanWarehouse(_clan, val);
        
        if(clanwh == null)
        {
            player.sendPacket(new ActionFailed());
            return;
        }

        player.setUsingClanWarehouse(true);
        player.setUsingFreight(false);
        player.tempInvetoryDisable();

        if((player.getClanPrivileges() & L2Clan.CP_CL_VIEW_WAREHOUSE) != L2Clan.CP_CL_VIEW_WAREHOUSE)
            player.sendPacket(new SystemMessage(SystemMessage.ITEMS_LEFT_AT_THE_CLAN_HALL_WAREHOUSE_CAN_ONLY_BE_RETRIEVED_BY_THE_CLAN_LEADER_DO_YOU_WANT_TO_CONTINUE));

        if(Config.DEBUG) _log.fine("Showing items to deposit - clan");

        player.sendPacket(new WareHouseWithdrawalList(player, player.getClan()));
    }

    private void showWithdrawWindowFreight(L2PcInstance player)
    {
        player.setUsingClanWarehouse(false);
        player.setUsingFreight(true);
        if(Config.DEBUG) _log.fine("Showing freightened items");

        Warehouse list = player.getFreight();
        list.setLocationId(null);

        if(list != null)
        {
            player.sendPacket(new WareHouseWithdrawalList(player));
        }
        else
        {
            if(Config.DEBUG) _log.fine("no items freightened");
        }

        player.sendPacket(new ActionFailed());
    }

    private void showDepositWindowFreight(L2PcInstance player)
    {
        player.setUsingClanWarehouse(false);
        player.setUsingFreight(true);

        // No other chars in the account of this player
        if(player.getAccountChars().size() == 0)
        {
            NpcHtmlMessage npcReply = new NpcHtmlMessage(5);

            StringBuffer replyMSG = new StringBuffer("<html><body>");
            replyMSG.append("You have no other characters to make a freight for.");
            replyMSG.append("</body></html>");

            npcReply.setHtml(replyMSG.toString());
            player.sendPacket(npcReply);
        }
        // One or more chars other than this player for this account
        else
        {

            HashMap<Integer, String> chars = player.getAccountChars();

            if(chars.size() < 1)
            {
                player.sendPacket(new ActionFailed());
                return;
            }

            NpcHtmlMessage npcReply = new NpcHtmlMessage(5);
            StringBuffer replyMSG = new StringBuffer("<html><body>");
            replyMSG.append("Select the character for the freight:<br><br>");
            // replyMSG.append("<select>");
            for(Iterator<Integer> iter = chars.keySet().iterator(); iter.hasNext();)
            {
                Integer objId = iter.next();
                String charName = chars.get(objId);

                /*
                 * replyMSG.append("<option action=\"bypass -h npc_" +
                 * String.valueOf(getObjectId()) + "_FreightChar_" +
                 * String.valueOf(objId) + "\">"); replyMSG.append(charName);
                 * replyMSG.append("</option>");
                 */
                replyMSG.append("<a action=\"bypass -h npc_"
                        + String.valueOf(getObjectId()) + "_FreightChar_"
                        + String.valueOf(objId) + "\">");
                replyMSG.append(charName);
                replyMSG.append("</a><br><br>");
            }
            // replyMSG.append("</select>");
            replyMSG.append("</body></html>");
            npcReply.setHtml(replyMSG.toString());
            player.sendPacket(npcReply);

            if(Config.DEBUG)
                _log.fine("Showing destination chars to freight - char src: "
                        + player.getName());
        }
        player.sendPacket(new ActionFailed());
    }

    private void showDepositWindowFreight(L2PcInstance player, int obj_Id)
    {
        player.setUsingClanWarehouse(false);
        player.setUsingFreight(true);
        player.setFreightId(obj_Id);

        player.tempInvetoryDisable();

        if(Config.DEBUG) _log.fine("Showing items to freight");

        player.sendPacket(new WareHouseDepositList(player));
        player.sendPacket(new ActionFailed());
    }

    public void onBypassFeedback(L2PcInstance player, String command)
    {
        // lil check to prevent enchant exploit
        if(player.getEnchantScroll() != null)
        {
            _log.info("Player " + player.getName() + " trying to use enchant exploit, ban this player!");
            player.setEnchantScroll(null);
            return;
        }

        if(command.startsWith("WithdrawP"))
        {
        	int val = Integer.parseInt(command.substring(10));
        	if (val == 9){
				NpcHtmlMessage html = new NpcHtmlMessage(1);
				html.setFile("data/html/warehouse/personal.htm");
				html.replace("%objectId%", String.valueOf(getObjectId()));
				html.replace("%npcname%", getName());
				player.sendPacket(html);
        	}else{
            showRetrieveWindow(player, val);
        	}
        }
        else if(command.equals("DepositP"))
        {
            showDepositWindow(player);
        }
        else if(command.startsWith("WithdrawC"))
        {
        	int val = Integer.parseInt(command.substring(10));
        	if (val == 9){
				NpcHtmlMessage html = new NpcHtmlMessage(1);
				html.setFile("data/html/warehouse/clan.htm");
				html.replace("%objectId%", String.valueOf(getObjectId()));
				html.replace("%npcname%", getName());
				player.sendPacket(html);
        	}else{
        		showWithdrawWindowClan(player, val);
        	}
        }
        else if(command.equals("DepositC"))
        {
            showDepositWindowClan(player);
        }
        else if(command.startsWith("WithdrawF"))
        {
            if(Config.ALLOW_FREIGHT)
            {
                showWithdrawWindowFreight(player);
            }
        }
        else if(command.startsWith("DepositF"))
        {
            if(Config.ALLOW_FREIGHT)
            {
                showDepositWindowFreight(player);
            }
        }
        else if(command.startsWith("FreightChar"))
        {
            if(Config.ALLOW_FREIGHT)
            {
                int startOfId = command.lastIndexOf("_") + 1;
                String id = command.substring(startOfId);
                showDepositWindowFreight(player, Integer.parseInt(id));
            }
        }
        else
        {
            super.onBypassFeedback(player, command);
        }
    }
}
