package net.sf.l2j.gameserver.clientpackets;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.ClientThread;
import net.sf.l2j.gameserver.model.L2Clan;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.packets.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.packets.serverpackets.StopAllianceWar;
import net.sf.l2j.gameserver.packets.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.tables.ClanTable;

import java.nio.ByteBuffer;
import java.util.logging.Logger;

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
        _log.info("RequestStopAllianceWar by "+getClient().getActiveChar().getClan().getAllyName()+" with "+_allianceName);
        _clan = getClient().getActiveChar().getClan();
        player = getClient().getActiveChar();
        L2Clan clan = ClanTable.getInstance().getClanByName(_allianceName);
        
        if(_clan == null)
            return;
		
        if(_clan.getAllyId()!=_clan.getClanId())
            return;
		
        if (player.isTransactionInProgress())
		{
            player.sendPacket(new SystemMessage(SystemMessage.WAITING_FOR_REPLY));
			if (Config.DEBUG) _log.fine("player is already doing some other action");
			return;
		}
        
        if(clan == null)
        {
        	player.sendMessage("No such alliance.");
            player.sendPacket(new ActionFailed());
            return;                        
        }

        if(_clan.isAtWarWith(clan.getClanId())!=2)
        {
        	player.sendMessage("You aren't at war with this alliance.");
            player.sendPacket(new ActionFailed());
            return;            
        }
        
        _log.warning("RequestStopAllianceWar, leader: "+_clan.getLeaderName()+" clan: "+_clan.getAllyName());
        
        L2PcInstance leader = L2World.getInstance().getPlayer(clan.getLeaderName());
        if(leader != null && leader.isOnline() == 0)
        {
        	player.sendMessage("Alliance leader isn't online.");
            player.sendPacket(new ActionFailed());
            return;                        
        }
        
        if (leader.isTransactionInProgress())
		{
			player.sendPacket(new SystemMessage(SystemMessage.S1_IS_BUSY_TRY_LATER).addString(leader.getName()));
			return;
		} 
        
		leader.setTransactionRequester(player);
		player.setTransactionRequester(leader);
        leader.sendPacket(new StopAllianceWar(_clan.getAllyName(),player.getName()));
    }
    
    public String getType()
    {
        return _C__9A_REQUESTSTOPALLIANCEWAR;
    }
}