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
import net.sf.l2j.gameserver.GameServer;
import net.sf.l2j.gameserver.instancemanager.SiegeManager;
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
    
    if (command.startsWith("create_clan") && command.length() > 12)
    {
      String val = command.substring(12);
      createClan(player, val);
    } 
    if (command.startsWith("create_ally") && command.length() > 12)
    {
      String val = command.substring(12);
      createAlly(player, val);
    } 
        else if (command.startsWith("dissolve_ally"))
        {
      dissolveAlly(player);
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

  
  @SuppressWarnings("static-access")
public void onAction(L2PcInstance player)
  {
    if(GameServer.events.onAction(player, this))
      return;

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
      player.sendPacket(new SystemMessage(SystemMessage.YOU_ARE_NOT_QUALIFIED_TO_CREATE_A_CLAN));
      return;
    }
    
    if (player.getClanId() != 0)
    {
      player.sendPacket(new SystemMessage(SystemMessage.YOU_HAVE_FAILED_TO_CREATE_A_CLAN));
      return;
    }

    if (!player.canCreateClan())
    { 
      // you can't create clan 10 days
      player.sendPacket(new SystemMessage(SystemMessage.YOU_CANNOT_CREATE_A_NEW_CLAN_WITHIN_10_DAYS_OF_DISPERSION));
      return;
    }
    if (clanName.length() > 16)
    {
      player.sendPacket(new SystemMessage(SystemMessage.CLAN_NAMES_LENGTH_IS_INCORRECT));
      return;
    }
    
    L2Clan clan = ClanTable.getInstance().createClan(player, clanName);
    if (clan == null)
    {
      // clan name is already taken
      player.sendPacket(new SystemMessage(SystemMessage.CLAN_NAME_IS_INCORRECT));
      return;
    }
    
    //should be update packet only
    player.sendPacket(new PledgeShowInfoUpdate(clan, player));
    player.sendPacket(new UserInfo(player));
    player.sendPacket(new SystemMessage(SystemMessage.CLAN_HAS_BEEN_CREATED));
  }
    
    private void dissolveClan(L2PcInstance player, int clanId)
    {
        if (player.getClan() == null)
            return;
        if (!player.isClanLeader())
        {
            player.sendPacket(new SystemMessage(SystemMessage.ONLY_THE_CLAN_LEADER_IS_ENABLED));
            return;
        }
        if(player.isAtWar()>0)
        {
            player.sendMessage("You can't dissolve clan while at war.");
            return;
        }
        long deleteclantime = System.currentTimeMillis();
        SiegeManager.getInstance().removeSiegeSkills(player);
        SystemMessage sm = new SystemMessage(SystemMessage.CLAN_HAS_DISPERSED);
        
        for (L2PcInstance clanMember : player.getClan().getOnlineMembers(null))
        {
            clanMember.setClan(null);
            clanMember.setTitle(null);
            clanMember.sendPacket(sm);
            clanMember.setDeleteClanTime(deleteclantime);

            clanMember.broadcastUserInfo();
        }
        
        java.sql.Connection con = null;
        try
        {
            con = L2DatabaseFactory.getInstance().getConnection();
            PreparedStatement statement = con.prepareStatement("UPDATE characters SET clanid = 0, deleteclan = ? WHERE clanid=?");
            statement.setLong(1, deleteclantime/1000);
            statement.setInt(2, clanId);
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
    if (!player.isClanLeader())
    {
        player.sendPacket(new SystemMessage(SystemMessage.ONLY_THE_CLAN_LEADER_IS_ENABLED));
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
      sendPacket(new StatusUpdate(player.getObjectId()).addAttribute(StatusUpdate.SP, player.getSp()));
      
      player.sendPacket(new ItemList(player, false));
      
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
        if (clan.getLevel() > 3)
        	SiegeManager.getInstance().addSiegeSkills(player);
                            
      // notify all the members about it
      clan.broadcastToOnlineMembers(new SystemMessage(SystemMessage.CLANS_SKILL_LEVEL_HAS_INCREASED));
      clan.broadcastToOnlineMembers(new PledgeStatusChanged(clan));
            clan.broadcastClanStatus();
    }
    else
    {
      player.sendPacket(new SystemMessage(SystemMessage.CLAN_HAS_FAILED_TO_INCREASE_SKILL_LEVEL));
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
    
    if (player.getClanId() == 0)
        return;
    
    
    if (Config.DEBUG)
      _log.fine(player.getObjectId()+"("+player.getName()+
          ") requested ally creation from "+getObjectId()+"("+getName()+")");
    
    if (!player.isClanLeader())
    {
      player.sendPacket(new SystemMessage(SystemMessage.ONLY_CLAN_LEADERS_MAY_CREATE_ALLIANCES));
      return;
    }
    if (player.getClan().getAllyId() != 0)
    {
      player.sendPacket(new SystemMessage(SystemMessage.YOU_HAVE_ALREADY_JOINED_AN_ALLIANCE));
      return;
    }
    if (allyName.length() < 2)
    {
      player.sendPacket(new SystemMessage(SystemMessage.INCORRECT_ALLIANCE_NAME));
      return;
    }
    if (allyName.length() > 16)
    {
      player.sendPacket(new SystemMessage(SystemMessage.INCORRECT_LENGTH_FOR_AN_ALLIANCE_NAME));
      return;
    }
    if (player.getClan().getLevel()<5)
    {
      player.sendPacket(new SystemMessage(SystemMessage.TO_CREATE_AN_ALLIANCE_YOUR_CLAN_MUST_BE_LEVEL_5_OR_HIGHER));
      return;
    }
    if(ClanTable.getInstance().isAllyExists(allyName)){
      player.sendPacket(new SystemMessage(SystemMessage.THE_FOLLOWING_ALLIANCE_ALREADY_EXISTS));
      return;
    }

    L2Alliance alliance = ClanTable.getInstance().createAlliance(player, allyName);
    if (alliance == null)
    {
      return;
    }
    
    player.sendPacket(new UserInfo(player));

    SystemMessage sm = new SystemMessage(SystemMessage.S1_S2);
    sm.addString("Alliance "+allyName+" has been created.");
    player.sendPacket(sm);
  }
  
    private void dissolveAlly(L2PcInstance player)
    {
      if (player.getAlliance() == null)
          return;
      if (!player.isAllyLeader())
      {
          player.sendPacket(new SystemMessage(SystemMessage.FEATURE_AVAILABLE_TO_ALLIANCE_LEADERS_ONLY));
          return;
      }
      if(player.isAtWar()>1)
      {
          player.sendMessage("You can't dissolve alliance while at war.");
          return;
      }
      int clanid = player.getClanId();
      int allyid = player.getAllyId();
      player.setAlliance(null);
      player.getClan().setAllyId(0);
      player.getClan().setAlliance(null);
      SystemMessage sm = new SystemMessage(SystemMessage.THE_ALLIANCE_HAS_DISSOLVED);
        
      for (L2PcInstance allyMember : player.getAlliance().getOnlineMembers(player.getName()))
      {
          allyMember.setAlliance(null);
          allyMember.sendPacket(sm);

          allyMember.broadcastUserInfo();
      }
    
        java.sql.Connection con = null;
        try
        {
            con = L2DatabaseFactory.getInstance().getConnection();
            PreparedStatement statement = con.prepareStatement("UPDATE clan_data SET ally_id = 0 WHERE clan_id=?");
            statement.setInt(1, clanid);
            statement.execute();
            statement.close();
            statement = con.prepareStatement("UPDATE characters SET allyid = 0 WHERE clanid=?");
            statement.setInt(1, clanid);
            statement.execute();
            statement.close();
            statement = con.prepareStatement("DELETE FROM ally_data WHERE ally_id=?");
            statement.setInt(1, allyid);
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
  
}
