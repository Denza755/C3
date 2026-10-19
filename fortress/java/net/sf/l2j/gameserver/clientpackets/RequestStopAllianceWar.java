package net.sf.l2j.gameserver.clientpackets;

import java.nio.ByteBuffer;
import java.util.logging.Logger;

import net.sf.l2j.gameserver.ClanTable;
import net.sf.l2j.gameserver.ClientThread;
import net.sf.l2j.gameserver.model.L2Clan;
import net.sf.l2j.gameserver.model.L2PcInstance;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.serverpackets.StopAllianceWar;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;

public class RequestStopAllianceWar extends ClientBasePacket
{
    private static final String _C__9A_REQUESTSTOPALLIANCEWAR = "[C] 9a RequestStopAllianceWar";
    private static Logger _log = Logger.getLogger(RequestStopAllianceWar.class.getName());

    String _allianceName;
    L2Clan _clan;
    L2PcInstance player;
    
    public RequestStopAllianceWar(ByteBuffer buf, ClientThread client)
    {
        super(buf, client);
        _allianceName  = readS();
    }

    void runImpl()
    {
        player = getClient().getActiveChar();
		if(player == null) return;
        _clan = player.getClan();
        L2Clan clan = ClanTable.getInstance().getClanByName(_allianceName);
        _log.info("RequestStopAllianceWar by "+player.getClan().getAlliance().getAllyName()+" with "+_allianceName);
        
        if(_clan == null)
            return;
		
        if(_clan.getAllyId()!=_clan.getClanId())
            return;
		
        if (player.isTransactionInProgress())
		{
            player.sendPacket(new SystemMessage(SystemMessage.WAITING_FOR_ANOTHER_REPLY));
			return;
		}
        
        if(clan == null)
        {
            player.sendMessage("No such alliance.");
            player.sendPacket(new ActionFailed());
            return;                        
        }

        if(player.isAtWarWith(clan.getClanId())!=2)
        {
            player.sendMessage("You aren't at war with this alliance.");
            player.sendPacket(new ActionFailed());
            return;            
        }
        
        _log.warning("RequestStopAllianceWar, leader: "+_clan.getLeaderName()+" clan: "+_clan.getAlliance().getAllyName());
        
        L2PcInstance leader = L2World.getPlayer(clan.getLeaderName());
        if(leader != null && leader.isOnline() == 0)
        {
            player.sendMessage("Alliance leader isn't online.");
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
        leader.sendPacket(new StopAllianceWar(_clan.getAlliance().getAllyName(),player.getName()));
    }
    
    public String getType()
    {
        return _C__9A_REQUESTSTOPALLIANCEWAR;
    }
}