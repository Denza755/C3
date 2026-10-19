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
package net.sf.l2j.gameserver.model.actor.instance;

import java.sql.PreparedStatement;
import java.util.Iterator;
import java.util.Set;

import javolution.lang.TextBuilder;

import net.sf.l2j.Config;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.datatables.CharTemplateTable;
import net.sf.l2j.gameserver.datatables.ClanTable;
import net.sf.l2j.gameserver.instancemanager.SiegeManager;
import net.sf.l2j.gameserver.instancemanager.CastleManager;
import net.sf.l2j.gameserver.model.L2Clan;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.entity.Castle;
import net.sf.l2j.gameserver.model.base.ClassType;
import net.sf.l2j.gameserver.model.base.PlayerClass;
import net.sf.l2j.gameserver.model.base.PlayerRace;

import net.sf.l2j.gameserver.serverpackets.*;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;


/**
 * This class ...
 * 
 * @version $Revision: 1.4.2.3.2.8 $ $Date: 2005/03/29 23:15:15 $
 */
public final class L2VillageMasterInstance extends L2FolkInstance
{

    /**
     * @param template
     */
    public L2VillageMasterInstance(int objectId, L2NpcTemplate template)
    {
        super(objectId, template);
    }

    public void onBypassFeedback(L2PcInstance player, String command)
    {
        if (!isInsideRadius(player, INTERACTION_DISTANCE, false, false)) return;

        player.sendPacket(new ActionFailed());

        String[] commandStr = command.split(" ");
        String actualCommand = commandStr[0]; // Get actual command

        String cmdParams = "";

        if (commandStr.length >= 2) cmdParams = commandStr[1];

	   	if(player.isAio() && !Config.ALLOW_AIO_USE_GK)
    	{
    		player.sendMessage("Aio Buffers Can't Speak To Village Masters.");
    		return;
    	}
        if (actualCommand.equalsIgnoreCase("create_clan"))
        {
            if (cmdParams.equals("")) return;

            createClan(player, cmdParams);
        }
        else if (actualCommand.equalsIgnoreCase("create_ally"))
        {
            if (cmdParams.equals("")) return;

            createAlly(player, cmdParams);
        }
        else if (actualCommand.equalsIgnoreCase("dissolve_ally"))
        {
            dissolveAlly(player);
        }
        else if (actualCommand.equalsIgnoreCase("dissolve_clan"))
        {
            dissolveClan(player, player.getClanId());
        }
        else if (actualCommand.equalsIgnoreCase("increase_clan_level"))
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

        if (val == 0) pom = "" + npcId;
        else pom = npcId + "-" + val;

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
            _log.fine(player.getObjectId() + "(" + player.getName() + ") requested clan creation from "
                + getObjectId() + "(" + getName() + ")");
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

        if (!player.canCreateClan())
        {
            // you can't create clan 10 days
            SystemMessage sm = new SystemMessage(230);
            player.sendPacket(sm);
            return;
        }
        if (clanName.length() > 16)
        {
            SystemMessage sm = new SystemMessage(SystemMessage.CLAN_NAME_TOO_LONG);
            player.sendPacket(sm);
            return;
        }

        L2Clan clan = ClanTable.getInstance().createClan(player, clanName.trim());
        if (clan == null)
        {
            // clan name is already taken
            SystemMessage sm = new SystemMessage(SystemMessage.CLAN_NAME_INCORRECT);
            player.sendPacket(sm);
            return;
        }

        //should be update packet only
        PledgeShowInfoUpdate pu = new PledgeShowInfoUpdate(clan);
        player.sendPacket(pu);

        UserInfo ui = new UserInfo(player);
        player.sendPacket(ui);
        clan.updateClanInDB();
        SystemMessage sm = new SystemMessage(SystemMessage.CLAN_CREATED);
        player.sendPacket(sm);
        player.setOnlineStatus(true);
        player.sendPacket(new PledgeShowMemberListAll(clan, player));
        player.sendPacket(new PledgeShowMemberListAll(clan, player));
        player.sendPacket(new PledgeShowInfoUpdate(clan));
    }

    private void dissolveClan(L2PcInstance player, int clanId)
    {
        L2Clan clan = player.getClan();

        if (player.getClan() == null) return;

		if(!player.isClanLeader())
		{
            player.sendMessage("You are not authorized to do that.");
			return;
		}

		if(player.isInsideZone(L2PcInstance.ZONE_SIEGE))
		{
			player.sendMessage("Cannot dissolve while in siege.");
			return;
		}

		if(clan.isAtWar() != 0)
		{
            player.sendMessage("Cannot dissolve while in war.");
            return;
		}

		if(clan.getAllyId() != 0)
		{
            player.sendMessage("Cannot disperse the clans in ally.");
			return;
		}

		if(clan.getHasCastle() != 0 || clan.getHasHideout() != 0)
		{
            player.sendMessage("Cannot dissolve while owning clan hall or castle.");
			return;
		}

		for(Castle castle : CastleManager.getInstance().getCastles())
		{
			if(SiegeManager.getInstance().checkIsRegistered(clan, castle.getCastleId()))
			{
                player.sendMessage("Cannot dissolve cause clan will participate in castle siege.");
				return;
			}
		}

        L2PcInstance[] clanMembers = player.getClan().getOnlineMembers(player.getName());
        player.setClan(null);
        player.setTitle(null);
        // Апдейт в базу, после расформировки клана клан лидером.
        clan.updateClanInDB();
        clan.updateClanMember(player);

        long deleteclantime = System.currentTimeMillis();
        player.setDeleteClanTime(deleteclantime);
        SiegeManager.getInstance().removeSiegeSkills(player);


        // The clan leader should take the XP penalty of a full death.
        player.deathPenalty(false);
        clan = null;
        
        SystemMessage sm = new SystemMessage(193);

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
            PreparedStatement statement = con.prepareStatement("UPDATE characters SET clanid = 0, clan_privs = 0 WHERE clanid=?");
            statement.setInt(1, clanId);
            statement.execute();
            statement.close();

            // save the leader DeleteClanTime
            statement = con.prepareStatement("UPDATE characters SET deleteclan = ? WHERE obj_Id=?");
            statement.setLong(1, deleteclantime);
            statement.setInt(2, player.getObjectId());
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
            _log.warning("could not dissolve clan:" + e);
        }
        finally
        {
            try
            {
                con.close();
            }
            catch (Exception e)
            {
            }
        }


    }

    public void levelUpClan(L2PcInstance player, int clanId)
    {
        L2Clan clan = player.getClan();
        if (clan == null)
        {
            return;
        }
        if (!player.isClanLeader())
        {
            SystemMessage sm = new SystemMessage(SystemMessage.S1_S2);
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
                     if (player.reduceAdena("ClanLvl", 650000, this, true))
                     {
                             player.setSp(player.getSp() - 35000);
                             increaseClanLevel = true;
                     }
                }
                break;
            }
            case 1:
            {
                // upgrade to 2
                if (player.getSp() >= 150000 && player.getAdena() >= 2500000)
                {
                     if (player.reduceAdena("ClanLvl", 2500000, this, true))
                     {
                             player.setSp(player.getSp() - 150000);
                             increaseClanLevel = true;
                     }
                }
                break;
            }
            case 2:
            {
                // upgrade to 3
                if (player.getSp() >= 500000 && player.getInventory().getItemByItemId(1419) != null)
                {
                    // itemid 1419 == proof of blood
                     if (player.destroyItemByItemId("ClanLvl", 1419, 1, player.getTarget(), false))
                     {
                             player.setSp(player.getSp() - 500000);
                             increaseClanLevel = true;
                     }
                }
                break;
            }
            case 3:
            {
                // upgrade to 4
                if (player.getSp() >= 1400000 && player.getInventory().getItemByItemId(3874) != null)
                {
                    // itemid 3874 == proof of alliance
                         if (player.destroyItemByItemId("ClanLvl", 3874, 1, player.getTarget(), false))
                         {
                             player.setSp(player.getSp() - 1400000);
                             increaseClanLevel = true;
                         }
                }
                break;
            }
            case 4:
            {
                // upgrade to 5
                if (player.getSp() >= 3500000 && player.getInventory().getItemByItemId(3870) != null)
                {
                    // itemid 3870 == proof of aspiration
                         if (player.destroyItemByItemId("ClanLvl", 3870, 1, player.getTarget(), false))
                         {
                                 player.setSp(player.getSp() - 3500000);
                         increaseClanLevel = true;
                         }
                         /*
                    if (player.getInventory().getItemByItemId(3870) != null)
                    {
                        player.getInventory().destroyItemByItemId("ClanLvl", 3870, 1, player,
                                                                  player.getTarget());
                    }
                    else
                    {
                        player.reduceAdena("ClanLvl", 30000000, this, true);
                    }
                    increaseClanLevel = true;
                    */
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
                statement.setInt(1, clan.getLevel() + 1);
                statement.setInt(2, clanId);
                statement.execute();
                statement.close();

                con.close();
            }
            catch (Exception e)
            {
                _log.warning("could not increase clan level:" + e);
            }
            finally
            {
                try
                {
                    con.close();
                }
                catch (Exception e)
                {
                }
            }

            clan.setLevel(clan.getLevel() + 1);
            if (clan.getLevel() > 3) SiegeManager.getInstance().addSiegeSkills(player);

            // notify all the members about it
            SystemMessage sm = new SystemMessage(SystemMessage.CLAN_LEVEL_INCREASED);
             clan.broadcastToOnlineMembers(sm);
             clan.broadcastToOnlineMembers(new PledgeShowInfoUpdate(clan)); 
             /*
              * Micht :
              *  - use PledgeShowInfoUpdate instead of PledgeStatusChanged
              *          to update clan level ingame
              *  - remove broadcastClanStatus() to avoid members duplication
              */
             //clan.broadcastToOnlineMembers(new PledgeStatusChanged(clan));
             //clan.broadcastClanStatus();
        }
        else
        {
            SystemMessage sm = new SystemMessage(SystemMessage.FAILED_TO_INCREASE_CLAN_LEVEL);
            player.sendPacket(sm);
        }
    }

    public void createAlly(L2PcInstance player, String allyName)
    {
        //D5 You may not ally with clan you are battle with.
        //D6 Only the clan leader may apply for withdraw from alliance.
        //DD No response. Invitation to join an 
        //D7 Alliance leaders cannot withdraw.
        //D9 Different Alliance 
        //EB alliance information
        //Ec alliance name $s1
        //ee alliance leader: $s2 of $s1
        //ef affilated clans: total $s1 clan(s)
        //f6 you have already joined an alliance
        //f9 you cannot new alliance 10 days
        //fd cannot accept. clan ally is register as enemy during siege batle.
        //fe you have invited someone to your alliance.
        //100 do you wish to withdraw from the alliance
        //102 enter the name of the clan you wish to expel.
        //202 do you realy wish to dissolve the alliance
        //502 you have accepted alliance
        //602 you have failed to invite a clan into the alliance
        //702 you have withdrwa

        if (player.getClanId() == 0) return;

        if (Config.DEBUG)
            _log.fine(player.getObjectId() + "(" + player.getName() + ") requested ally creation from "
                + getObjectId() + "(" + getName() + ")");

        if (!player.isClanLeader())
        {
            //only clan leaders may create alliances.
            SystemMessage sm = new SystemMessage(504);
            player.sendPacket(sm);
            return;
        }
        if (player.getClan().getAllyId() != 0)
        {
            //is in ally
            SystemMessage sm = new SystemMessage(502);
            player.sendPacket(sm);
            return;
        }
        if (allyName.length() < 2)
        {
            // incorect alliance name
            SystemMessage sm = new SystemMessage(506);
            player.sendPacket(sm);
            return;
        }
        if (allyName.length() > 16)
        {
            // incorect length of alliance name
            SystemMessage sm = new SystemMessage(507);
            player.sendPacket(sm);
            return;
        }
        if (player.getClan().getLevel() < 5)
        {
            //only clan level 5 or higher.
            SystemMessage sm = new SystemMessage(549);
            player.sendPacket(sm);
            return;
        }
        if (ClanTable.getInstance().isAllyExists(allyName))
        {
            //name exists.
            SystemMessage sm = new SystemMessage(508);
            player.sendPacket(sm);
            return;
        }

        player.getClan().setAllyId(player.getClanId());
        player.getClan().setAllyName(allyName.trim());
        player.getClan().updateClanInDB();

        UserInfo ui = new UserInfo(player);
        player.sendPacket(ui);

        SystemMessage sm = new SystemMessage(SystemMessage.S1_S2);
        sm.addString("Alliance " + allyName + " has been created.");
        player.sendPacket(sm);
    }

    private void dissolveAlly(L2PcInstance player)
    {
        L2Clan playerClan = player.getClan();

        if (player.getClan() == null) return;

        int allyId = playerClan.getAllyId();

        if (allyId == 0) return;

        if (allyId != player.getClanId() || !player.isClanLeader())
        {
            player.sendPacket(new SystemMessage(SystemMessage.FEATURE_ONLY_FOR_ALLIANCE_LEADER));
            return;
        }

        playerClan.setAllyId(0);
        playerClan.setAllyName(null);

        // The clan leader should take the XP penalty of a full death.
        player.deathPenalty(false);

        SystemMessage sm = new SystemMessage(SystemMessage.ALLIANCE_DISOLVED);
        java.sql.Connection con = null;

        try
        {
            con = L2DatabaseFactory.getInstance().getConnection();
            PreparedStatement statement = con.prepareStatement("UPDATE clan_data SET ally_id = 0, ally_name = '' WHERE ally_id=?");
            statement.setInt(1, allyId);
            statement.execute();
            statement.close();

            player.sendPacket(sm);
            player.broadcastUserInfo();

            con.close();
        }
        catch (Exception e)
        {
            _log.warning("could not dissolve clan:" + e);
        }
        finally
        {
            try
            {
                con.close();
            }
            catch (Exception e)
            {
            }
        }
    }
}
