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

public class RequestSurrenderAllianceWar extends ClientBasePacket
{
    private static final String _C__51_REQUESTSURRENDERALLIANCEWAR = "[C] 51 RequestSurrenderAllianceWar";
    private static Logger _log = Logger.getLogger(RequestSurrenderAllianceWar.class.getName());

    String _allianceName;
    L2Clan _clan;
    L2PcInstance player;
    
    public RequestSurrenderAllianceWar(ByteBuffer buf, ClientThread client)
    {
        super(buf, client);
		_allianceName  = readS();
    }

    void runImpl()
    {
        
        _clan = getClient().getActiveChar().getClan();
        player = getClient().getActiveChar();
        L2Clan clan = ClanTable.getInstance().getClanByName(_allianceName);
        
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

        _log.info("RequestSurrenderAllianceWar by "+getClient().getActiveChar().getClan().getAllyName()+" with "+_allianceName);
        
        if(_clan.isAtWarWith(clan.getClanId())!=2)
        {
        	player.sendMessage("You aren't at war with this alliance.");
            player.sendPacket(new ActionFailed());
            return;            
        }
        
        
        player.sendPacket(new SystemMessage(SystemMessage.YOU_HAVE_SURRENDERED_TO_THE_S1_CLAN).addString(_allianceName));
        player.deathPenalty(false);
		ClanTable.getInstance().deletewars(_clan.getClanId(), clan.getClanId());
    }
    
    public String getType()
    {
        return _C__51_REQUESTSURRENDERALLIANCEWAR;
    }
}