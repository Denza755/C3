package net.sf.l2j.gameserver.handler.admincommandhandlers;

import java.sql.PreparedStatement;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import net.sf.l2j.Config;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.ClanTable;
import net.sf.l2j.gameserver.handler.IAdminCommandHandler;
import net.sf.l2j.gameserver.instancemanager.SiegeManager;
import net.sf.l2j.gameserver.model.L2Clan;
import net.sf.l2j.gameserver.model.L2PcInstance;
import net.sf.l2j.gameserver.serverpackets.PledgeShowInfoUpdate;
import net.sf.l2j.gameserver.serverpackets.PledgeStatusChanged;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.serverpackets.UserInfo;

/**
 * Pledge Manipulation
 * //pledge <create|dismiss|setlevel>
 */
public class AdminPledge implements IAdminCommandHandler
{
    private static String[] _adminCommands = {"admin_pledge"};
    private static Logger _log = Logger.getLogger(AdminPledge.class.getName());

    public boolean useAdminCommand(String command, L2PcInstance activeChar)
    {
        if (!Config.ALT_PRIVILEGES_ADMIN)
        {
        if (!activeChar.isGM() || activeChar.getAccessLevel() < Config.GM_ACCESSLEVEL || activeChar.getTarget() == null || !(activeChar.getTarget() instanceof L2PcInstance))
            return false;
        }
        
        L2PcInstance target = (L2PcInstance)activeChar.getTarget();

        if(command.startsWith("admin_pledge"))
        {
            StringTokenizer st = new StringTokenizer(command);
            st.nextToken();
            
            String action = st.nextToken(); // create|dismiss|setlevel
            
            if(action.equals("create"))
            {
                try
                {
                    String pledgeName = st.nextToken();
                    L2Clan clan = ClanTable.getInstance().createClan(target, pledgeName);
                    if(clan != null)
                    {
                        activeChar.sendMessage("Clan "+pledgeName+" created! Leader: "+target.getName());

                        target.sendPacket(new PledgeShowInfoUpdate(clan, target));
                        target.sendPacket(new UserInfo(target));
                        target.sendPacket(new SystemMessage(SystemMessage.CLAN_HAS_BEEN_CREATED));
                        return true;
                    }
                }
                catch(Exception e)
                {
                    _log.warning("Error creating pledge by GM command: "+e);
                }
            }
            else if(action.equals("dismiss"))
            {
                if (target.getClan() == null || target.getObjectId() != target.getClan().getLeaderId())
                {
                    activeChar.sendMessage("Target are not clan leader, or clan doesnt exist");
                    return false;
                }
                
                SiegeManager.getInstance().removeSiegeSkills(target);
                SystemMessage sm = new SystemMessage(SystemMessage.CLAN_HAS_DISPERSED);
                for (L2PcInstance clanMember : target.getClan().getOnlineMembers(null))
                {
                    clanMember.setClan(null);
                    clanMember.setTitle(null);
                    clanMember.sendPacket(sm);
                    clanMember.broadcastUserInfo();
                }

                java.sql.Connection con = null;
                try
                {
                    con = L2DatabaseFactory.getInstance().getConnection();
                    PreparedStatement statement = con.prepareStatement("UPDATE characters SET clanid = 0 WHERE clanid=?");
                    statement.setInt(1, target.getClanId());
                    statement.execute();
                    statement.close();
                    
                    statement = con.prepareStatement("DELETE FROM clan_data WHERE clan_id=?");
                    statement.setInt(1, target.getClanId());
                    statement.execute();
                    statement.close();
                    target.sendPacket(sm);
                    target.broadcastUserInfo();
                    
                    con.close();
                }
                catch (Exception e)
                {
                    _log.warning("Error while dissolving clan by GM Command: "+e);
                } 
                finally 
                {
                    try { con.close(); } catch (Exception e) {}
                }
                
                return true;
            }
            else if(action.equals("setlevel"))
            {
                if (target.getClan() == null || target.getObjectId() != target.getClan().getLeaderId())
                {
                    activeChar.sendMessage("Target are not clan leader, or clan doesnt exist");
                    return false;
                }
                
                try
                {
                    int level = Integer.parseInt(st.nextToken());

                    java.sql.Connection con = null;
                    try
                    {
                        con = L2DatabaseFactory.getInstance().getConnection();
                        PreparedStatement statement = con.prepareStatement("UPDATE clan_data SET clan_level = ? WHERE clan_id = ?");
                        statement.setInt(1, level);
                        statement.setInt(2, target.getClanId());
                        statement.execute();
                        statement.close();
                        
                        con.close();
                    }
                    catch (Exception e)
                    {
                        _log.warning("could not store new clan level with GM comamnd:"+e);
                    } 
                    finally 
                    {
                        try { con.close(); } catch (Exception e) {}
                    }

                    activeChar.sendMessage("You set level "+level+" for clan "+target.getClan().getName());
                    target.getClan().setLevel(level);

                    if(level < 4)
                    {
                        SiegeManager.getInstance().removeSiegeSkills(target);
                    }
                    else if(level > 3)
                    {
                        SiegeManager.getInstance().addSiegeSkills(target);
                    }
                    
                    SystemMessage sm = new SystemMessage(SystemMessage.CLANS_SKILL_LEVEL_HAS_INCREASED);
                    for (L2PcInstance member : target.getClan().getOnlineMembers(null))
                        member.sendPacket(sm);

                    target.getClan().broadcastToOnlineMembers(new PledgeStatusChanged(target.getClan()));
					target.getClan().broadcastClanStatus();
                    
                    return true;
                }
                catch(Exception e)
                {
                    _log.warning("Error while changing clan level by GM Command: "+e);
                }
            }
        }
        
        return false;
    }
    
    public String[] getAdminCommandList()
    {
        return _adminCommands;
    }

}
