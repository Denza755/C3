package net.sf.l2j.gameserver.clientpackets;

import java.nio.ByteBuffer;
import java.util.logging.Logger;

import net.sf.l2j.gameserver.ClanTable;
import net.sf.l2j.gameserver.ClientThread;
import net.sf.l2j.gameserver.model.L2Alliance;
import net.sf.l2j.gameserver.model.L2PcInstance;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.serverpackets.StartAllianceWar;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;

public class RequestStartAllianceWar extends ClientBasePacket
{
    private static final String _C__98_REQUESTSTARTALLIANCEWAR = "[C] 98 RequestStartAllianceWar";
    private static Logger _log = Logger.getLogger(RequestStartAllianceWar.class.getName());

    String _allianceName;
    L2Alliance _alliance;
    L2PcInstance player;
    
    public RequestStartAllianceWar(ByteBuffer buf, ClientThread client)
    {
        super(buf, client);
        _allianceName  = readS();
    }

    void runImpl()
    {
        player = getClient().getActiveChar();
		if(player == null) return;
        _alliance = player.getClan().getAlliance();
        
        if(_alliance == null)
            return;
		
        if (player.isTransactionInProgress())
		{
            player.sendPacket(new SystemMessage(SystemMessage.WAITING_FOR_ANOTHER_REPLY));
			return;
		}
        
        else if(_alliance.isAtWar() == 2 || _alliance.isAtWar() == 3)
        {
            player.sendMessage("You can't declare war with multiply alliances.");
            player.sendPacket(new ActionFailed());
            return;            
        }
		
        L2Alliance alliance = ClanTable.getInstance().getAllianceByName(_allianceName);
        if(alliance == null)
        {
            player.sendMessage("No such alliance.");
            player.sendPacket(new ActionFailed());
            return;                        
        }
		
        if(_alliance.getAllyId()==alliance.getAllyId())
            return;                        

        _log.warning("RequestStartAllianceWar, leader: "+alliance.getAllyLeaderName()+" with alliance: "+_alliance.getAllyName());
        
        L2PcInstance leader = L2World.getPlayer(alliance.getAllyLeaderName());
        
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
			player.sendPacket(new SystemMessage(SystemMessage.S1_IS_BUSY_PLEASE_TRY_AGAIN_LATER).addString(leader.getName()));
			return;
		} 
        
		leader.setTransactionRequester(player);
		player.setTransactionRequester(leader);
        leader.sendPacket(new StartAllianceWar(_alliance.getAllyName(),player.getName()));
    }
    
    public String getType()
    {
        return _C__98_REQUESTSTARTALLIANCEWAR;
    }
}