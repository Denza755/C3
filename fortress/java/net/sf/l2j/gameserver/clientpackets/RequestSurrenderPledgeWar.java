package net.sf.l2j.gameserver.clientpackets;

import java.nio.ByteBuffer;
import java.util.logging.Logger;

import net.sf.l2j.gameserver.ClanTable;
import net.sf.l2j.gameserver.ClientThread;
import net.sf.l2j.gameserver.model.L2Clan;
import net.sf.l2j.gameserver.model.L2PcInstance;
import net.sf.l2j.gameserver.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;

public class RequestSurrenderPledgeWar extends ClientBasePacket
{
    private static final String _C__51_REQUESTSURRENDERPLEDGEWAR = "[C] 51 RequestSurrenderPledgeWar";
    private static Logger _log = Logger.getLogger(RequestSurrenderPledgeWar.class.getName());

    String _pledgeName;
    L2Clan _clan;
    L2PcInstance player;
    
    public RequestSurrenderPledgeWar(ByteBuffer buf, ClientThread client)
    {
        super(buf, client);
        _pledgeName  = readS();
    }

    void runImpl()
    {
		player = getClient().getActiveChar();
		if(player == null) return;
		_clan = player.getClan();
        L2Clan clan = ClanTable.getInstance().getClanByName(_pledgeName);
        
        if(_clan == null)
            return;
        
        if (player.isTransactionInProgress())
		{
            player.sendPacket(new SystemMessage(SystemMessage.WAITING_FOR_ANOTHER_REPLY));
			return;
		}
        
        if(clan == null)
        {
            player.sendMessage("No such clan.");
            player.sendPacket(new ActionFailed());
            return;                        
        }

        _log.info("RequestSurrenderPledgeWar by "+_clan.getName()+" with "+_pledgeName);
        
        if(player.isAtWarWith(clan.getClanId())!=1)
        {
            player.sendMessage("You aren't at war with this clan.");
            player.sendPacket(new ActionFailed());
            return;            
        }

        player.sendPacket(new SystemMessage(SystemMessage.YOU_HAVE_SURRENDERED_TO_THE_S1_CLAN).addString(_pledgeName));
        player.deathPenalty(false);
		ClanTable.getInstance().deletewars(_clan.getClanId(), clan.getClanId());
    }
    
    public String getType()
    {
        return _C__51_REQUESTSURRENDERPLEDGEWAR;
    }
}