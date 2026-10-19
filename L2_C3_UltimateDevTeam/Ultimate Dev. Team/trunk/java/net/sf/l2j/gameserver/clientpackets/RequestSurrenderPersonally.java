package net.sf.l2j.gameserver.clientpackets;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.ClientThread;
import net.sf.l2j.gameserver.model.L2Clan;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.packets.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.packets.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.tables.ClanTable;

import java.nio.ByteBuffer;
import java.util.logging.Logger;

public class RequestSurrenderPersonally extends ClientBasePacket
{
    private static final String _C__69_REQUESTSURRENDERPERSONALLY = "[C] 69 RequestSurrenderPersonally";
    private static Logger _log = Logger.getLogger(RequestSurrenderPledgeWar.class.getName());

    String _pledgeName;
    L2Clan _clan;
    L2PcInstance player;
    
    public RequestSurrenderPersonally(ByteBuffer buf, ClientThread client)
    {
        super(buf, client);
        _pledgeName  = readS();
    }

    void runImpl()
    {
        player = getClient().getActiveChar();
	if (player == null)
	    return;
        _log.info("RequestSurrenderPersonally by "+getClient().getActiveChar().getName()+" with "+_pledgeName);
        _clan = getClient().getActiveChar().getClan();
        L2Clan clan = ClanTable.getInstance().getClanByName(_pledgeName);
        
        if(_clan == null)
            return;
        
        if (player.isTransactionInProgress())
        {
            player.sendPacket(new SystemMessage(SystemMessage.WAITING_FOR_REPLY));
            if (Config.DEBUG) _log.fine("player is already doing some other action");
            return;
        }
        
        if(clan == null)
        {
        	player.sendMessage("No such clan.");
            player.sendPacket(new ActionFailed());
            return;                        
        }

        if(_clan.isAtWarWith(clan.getClanId())!=1 || player.getWantsPeace() == 1)
        {
        	player.sendMessage("You aren't at war with this clan.");
            player.sendPacket(new ActionFailed());
            return;            
        }
        
        player.setWantsPeace(1);
        player.deathPenalty(false);
        player.sendPacket(new SystemMessage(SystemMessage.YOU_HAVE_PERSONALLY_SURRENDERED_TO_THE_S1_CLAN).addString(_pledgeName));
        ClanTable.getInstance().CheckSurrender(_clan, clan);
    }
    
    public String getType()
    {
        return _C__69_REQUESTSURRENDERPERSONALLY;
    }
}
