package net.sf.l2j.gameserver.clientpackets;

import java.nio.ByteBuffer;
import java.util.logging.Logger;

import net.sf.l2j.gameserver.ClanTable;
import net.sf.l2j.gameserver.ClientThread;
import net.sf.l2j.gameserver.model.L2Clan;
import net.sf.l2j.gameserver.model.L2PcInstance;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.serverpackets.StartPledgeWar;
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
        _pledgeName  = readS();
    }

    void runImpl()
    {
		player = getClient().getActiveChar();
		if(player == null) return;
		_clan = player.getClan();
        
        if(_clan == null)
            return;
        
        if (player.isTransactionInProgress())
		{
            player.sendPacket(new SystemMessage(SystemMessage.WAITING_FOR_ANOTHER_REPLY));
			return;
		}
        
        if(_clan.getLevel() < 3)
        {
            player.sendMessage("Only lvl3+ clans can declare war.");
            player.sendPacket(new ActionFailed());
            return;
        }
        else if(player.isAtWar() == 1 || player.isAtWar() == 3)
        {
            player.sendMessage("You can't declare war with multiply clans.");
            player.sendPacket(new ActionFailed());
            return;            
        }
        
        L2Clan clan = ClanTable.getInstance().getClanByName(_pledgeName);
        if(clan == null)
        {
            player.sendMessage("No such clan.");
            player.sendPacket(new ActionFailed());
            return;                        
        }
		
		if(_clan.getClanId()==clan.getClanId())
            return;
		
        if(_clan.getAllyId() != 0 && _clan.getAllyId()==clan.getAllyId())
        {
            player.sendPacket(new SystemMessage(SystemMessage.YOU_CANNOT_APPLY_FOR_CLAN_WAR_WITH_A_CLAN_THAT_BELONGS_TO_THE_SAME_ALLIANCE));
            player.sendPacket(new ActionFailed());
            return;
        }

        _log.warning("RequestStartPledgeWar, leader: "+clan.getLeaderName()+" clan: "+_clan.getName());
        
        L2PcInstance leader = L2World.getPlayer(clan.getLeaderName());
        
        if(leader == null)
            return;
        
        if(leader != null && leader.isOnline() == 0)
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
        leader.sendPacket(new StartPledgeWar(_clan.getName(),player.getName()));
    }
    
    public String getType()
    {
        return _C__4D_REQUESTSTARTPLEDGEWAR;
    }
}