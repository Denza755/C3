package net.sf.l2j.gameserver.clientpackets;

import java.nio.ByteBuffer;
import java.util.logging.Logger;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.ClanTable;
import net.sf.l2j.gameserver.ClientThread;
import net.sf.l2j.gameserver.model.L2Clan;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;

public class RequestStartPledgeWar extends ClientBasePacket
{
    private static final String _C__4D_REQUESTSTARTPLEDGEWAR = "[C] 4D RequestStartPledgewar";
    private static Logger _log = Logger.getLogger(RequestStartPledgeWar.class.getName());

    String _pledgeName;
    L2Clan _clan;
    L2PcInstance player;

    public RequestStartPledgeWar(ByteBuffer buf, ClientThread client)
    {
        super(buf, client);
        _pledgeName = readS();
    }

    void runImpl()
    {
        player = getClient().getActiveChar();
        if (player == null) return;

        _clan = getClient().getActiveChar().getClan();
        if (_clan == null) return;

        if (_clan.getLevel() < 3 || _clan.getMembersCount() < Config.ALT_CLAN_MEMBERS_FOR_WAR)
        {
            SystemMessage sm = new SystemMessage(1564);
            player.sendPacket(sm);
            player.sendPacket(new ActionFailed());
            return;
        }
        
        if ((player.getClanPrivileges() & L2Clan.CP_CL_CLAN_WAR) != L2Clan.CP_CL_CLAN_WAR)
        {
            player.sendMessage("You are not authorized to manage clan wars.");
            player.sendPacket(new ActionFailed());
            return;
        }

        L2Clan clan = ClanTable.getInstance().getClanByName(_pledgeName);
        if (clan == null)
        {
            player.sendMessage("Invalid Clan.");
            player.sendPacket(new ActionFailed());
            return;
        }
        
        if (_clan.getAllyId() == clan.getAllyId() && _clan.getAllyId() != 0)
        {
            SystemMessage sm = new SystemMessage(1569);
            player.sendPacket(sm);
            player.sendPacket(new ActionFailed());
            sm = null;
            return;
        }
        
        if (clan.getLevel() < 3 || clan.getMembersCount() < Config.ALT_CLAN_MEMBERS_FOR_WAR)
        {
            SystemMessage sm = new SystemMessage(1564);
            player.sendPacket(sm);
            player.sendPacket(new ActionFailed());
            sm = null;
            return;
        }

        ClanTable.getInstance().storeclanswars(player.getClanId(), clan.getClanId());
        for (L2PcInstance cha : L2World.getInstance().getAllPlayers())
        {
                if (cha.getClan() == player.getClan() || cha.getClan() == clan)
                        cha.broadcastUserInfo();
        }
    }

    public String getType()
    {
        return _C__4D_REQUESTSTARTPLEDGEWAR;
    }
}