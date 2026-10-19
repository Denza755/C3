package net.sf.l2j.gameserver.clientpackets;

import java.nio.ByteBuffer;

import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.nProtect;

/**
 * @author zabbix
 * Lets drink to code!
 * 
 * Unknown Packet:ca
 * 0000: 45 00 01 00 1e 37 a2 f5 00 00 00 00 00 00 00 00    E....7..........
 */

public class GameGuardReply extends L2GameClientPacket
{
    private static final String _C__CA_GAMEGUARDREPLY = "[C] CA GameGuardReply";
    private int[] _reply = new int[4];

    public GameGuardReply(ByteBuffer buf, L2GameClient client)
    {
        super(buf,client);
		_reply[0] = readD();
		_reply[1] = readD();
		_reply[2] = readD();
		_reply[3] = readD();
    }

    protected void runImpl()
    {
        L2PcInstance activeChar = getClient().getActiveChar();
        
        if(activeChar == null)
            return;

		if (!nProtect.getInstance().checkGameGuardRepy(getClient(), _reply))
			return;        
        
        getClient().setGameGuardOk(true);
    }

    public String getType()
    {
        return _C__CA_GAMEGUARDREPLY;
    }

}
