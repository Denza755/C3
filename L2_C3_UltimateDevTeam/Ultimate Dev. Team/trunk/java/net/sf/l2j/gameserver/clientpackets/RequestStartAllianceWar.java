package net.sf.l2j.gameserver.clientpackets;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.ClientThread;
import net.sf.l2j.gameserver.model.L2Clan;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.packets.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.packets.serverpackets.StartAllianceWar;
import net.sf.l2j.gameserver.packets.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.tables.ClanTable;

import java.nio.ByteBuffer;
import java.util.logging.Logger;

public class RequestStartAllianceWar extends ClientBasePacket
{
    private static final String _C__98_REQUESTSTARTALLIANCEWAR = "[C] 98 RequestStartAllianceWar";
    private static Logger _log = Logger.getLogger(RequestStartAllianceWar.class.getName());

    String _allianceName;
    L2Clan _clan;
    L2PcInstance player;
    
    public RequestStartAllianceWar(ByteBuffer buf, ClientThread client)
    {
        super(buf, client);
        _allianceName  = readS();
    }

    void runImpl()
    {
        _clan = getClient().getActiveChar().getClan();
        player = getClient().getActiveChar();
        
        if(_clan == null)
            return;
		
        if (player.isTransactionInProgress())
		{
            player.sendPacket(new SystemMessage(SystemMessage.WAITING_FOR_REPLY));
			if (Config.DEBUG) _log.fine("player is already doing some other action");
			return;
		}
        
        else if(_clan.isAtWar() == 2 || _clan.isAtWar() == 3)
        {
        	player.sendMessage("You can't declare war with multiply alliances.");
            player.sendPacket(new ActionFailed());
            return;            
        }
		
        L2Clan clan = ClanTable.getInstance().getAllianceByName(_allianceName);
        if(clan == null)
        {
        	player.sendMessage("No such alliance.");
            player.sendPacket(new ActionFailed());
            return;                        
        }
		
        if(_clan.getAllyId()==clan.getAllyId())
            return;                        

        _log.warning("RequestStartAllianceWar, leader: "+clan.getLeaderName()+" with alliance: "+_clan.getAllyName());
        
        L2PcInstance leader = L2World.getInstance().getPlayer(clan.getLeaderName());
        
        if(leader == null)
            return;
        
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
        leader.sendPacket(new StartAllianceWar(_clan.getAllyName(),player.getName()));
    }
    
    public String getType()
    {
        return _C__98_REQUESTSTARTALLIANCEWAR;
    }
}