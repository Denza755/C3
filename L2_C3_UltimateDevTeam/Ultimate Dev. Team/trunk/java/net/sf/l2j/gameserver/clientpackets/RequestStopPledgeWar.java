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
	if (player == null)
	    return;
        _log.info("RequestStopPledgeWar by "+getClient().getActiveChar().getClan().getName()+" with "+_pledgeName);
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

        if(_clan.isAtWarWith(clan.getClanId())!=1)
        {
        	player.sendMessage("You aren't at war with this clan.");
            player.sendPacket(new ActionFailed());
            return;            
        }
        
        _log.warning("RequestStopPledgeWar, leader: "+_clan.getLeaderName()+" clan: "+_clan.getName());
        
/*        L2PcInstance leader = L2World.getInstance().getPlayer(clan.getLeaderName());
        if(leader != null && leader.isOnline() == 0)
        {
        	if (Config.ALT_LANGUAGE)
        	{
        		player.sendMessage("Ch\u1EE7 m�n ph�i kh�ng online.");
        	}else{
        		player.sendMessage("Clan leader isn't online.");
        	}
            player.sendPacket(new ActionFailed());
            return;                        
        } */
        
//        if (leader.isTransactionInProgress())
//        {
//            SystemMessage sm = new SystemMessage(SystemMessage.S1_IS_BUSY_TRY_LATER);
//            sm.addString(leader.getName());
//            player.sendPacket(sm);
//            return;
//        } 
        
	ClanTable.getInstance().deletewars(player.getClanId(),clan.getClanId());
//        leader.setTransactionRequester(player);
//        player.setTransactionRequester(leader);
//        leader.sendPacket(new StopPledgeWar(_clan.getName(),player.getName()));
    }
    
    public String getType()
    {
        return _C__4F_REQUESTSTOPPLEDGEWAR;
    }
}
