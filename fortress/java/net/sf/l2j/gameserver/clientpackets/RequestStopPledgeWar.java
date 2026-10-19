package net.sf.l2j.gameserver.clientpackets;

import java.nio.ByteBuffer;
import java.util.logging.Logger;

import net.sf.l2j.gameserver.ClanTable;
import net.sf.l2j.gameserver.ClientThread;
import net.sf.l2j.gameserver.model.L2Clan;
import net.sf.l2j.gameserver.model.L2PcInstance;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.serverpackets.StopPledgeWar;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;

public class RequestStopPledgeWar extends ClientBasePacket
{
    private static final String _C__4F_REQUESTSTOPPLEDGEWAR = "[C] 4F RequestStopPledgeWar";
    private static Logger _log = Logger.getLogger(RequestStopPledgeWar.class.getName());

    String _pledgeName;
    L2Clan _clan;
    L2PcInstance player;
    
    public RequestStopPledgeWar(ByteBuffer buf, ClientThread client)
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
        _log.info("RequestStopPledgeWar by "+player.getClan().getName()+" with "+_pledgeName);
        
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

        if(player.isAtWarWith(clan.getClanId())!=1)
        {
            player.sendMessage("You aren't at war with this clan.");
            player.sendPacket(new ActionFailed());
            return;            
        }
        
        _log.warning("RequestStopPledgeWar, leader: "+_clan.getLeaderName()+" clan: "+_clan.getName());
        
        L2PcInstance leader = L2World.getPlayer(clan.getLeaderName());
        if(leader != null)
        {
            if(leader.isOnline() == 0)
            {
                player.sendMessage("Clan leader isn't online.");
                player.sendPacket(new ActionFailed());
                return;                        
            }
        
        if (leader.isTransactionInProgress())
		{
			player.sendPacket(new SystemMessage(SystemMessage.S1_IS_BUSY_PLEASE_TRY_AGAIN_LATER).addString(leader.getName()));
			return;
		} 
        
		    leader.setTransactionRequester(player);
		    player.setTransactionRequester(leader);
            leader.sendPacket(new StopPledgeWar(_clan.getName(),player.getName()));
        }
    }
    
    public String getType()
    {
        return _C__4F_REQUESTSTOPPLEDGEWAR;
    }
}
