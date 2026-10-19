package net.sf.l2j.gameserver.clientpackets;

import java.nio.ByteBuffer;
import java.util.logging.Logger;

import net.sf.l2j.gameserver.ClientThread;
import net.sf.l2j.gameserver.model.L2PcInstance;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;

public class RequestEvaluate extends ClientBasePacket {
	private static final String _C__B9_REQUESTEVALUATE = "[C] B9 RequestEvaluate";

	private static Logger _log = Logger.getLogger(RequestEvaluate.class.getName());

	private final int _targetid;

	public RequestEvaluate(ByteBuffer buf, ClientThread client) {
		super(buf, client);
		_targetid = readD();
	}

	void runImpl()
	{
		SystemMessage sm;
		L2PcInstance activeChar = getClient().getActiveChar();
		
		if (!(activeChar.getTarget() instanceof L2PcInstance) /* || activeChar.getTarget() == activeChar*/)
		{
			sm = new SystemMessage(SystemMessage.TARGET_IS_INCORRECT);
			activeChar.sendPacket(sm);
			return;
		}
		
		L2PcInstance tgt = (L2PcInstance)activeChar.getTarget(); 

		if (activeChar.getRecomLeft() <= 0 || tgt.getRecomHave() >= 255) {
			sm = new SystemMessage(SystemMessage.S1_S2);
			sm.addString("You cannot recommend");
			activeChar.sendPacket(sm);
			return;
		}
		
		activeChar.setRecomLeft(activeChar.getRecomLeft()-1);
		tgt.setRecomHave(tgt.getRecomHave()+1);

		sm = new SystemMessage(SystemMessage.S1_S2);
		sm.addString("You was recommeded by "+activeChar.getName());
		tgt.sendPacket(sm);
		sm = new SystemMessage(SystemMessage.S1_S2);
		sm.addString("You have recommeded "+tgt.getName());
		activeChar.sendPacket(sm);
		tgt.broadcastUserInfo();
		activeChar.broadcastUserInfo();
	}
	
	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.clientpackets.ClientBasePacket#getType()
	 */
	public String getType() {
		return _C__B9_REQUESTEVALUATE;
	}
}
