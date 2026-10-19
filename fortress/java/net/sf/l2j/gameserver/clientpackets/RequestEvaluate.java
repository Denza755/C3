package net.sf.l2j.gameserver.clientpackets;

import java.nio.ByteBuffer;

import net.sf.l2j.gameserver.ClientThread;
import net.sf.l2j.gameserver.model.L2PcInstance;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;

public class RequestEvaluate extends ClientBasePacket {
	private static final String _C__B9_REQUESTEVALUATE = "[C] B9 RequestEvaluate";

	//private static Logger _log = Logger.getLogger(RequestEvaluate.class.getName());

	@SuppressWarnings("unused")
	private final int _targetid;

	public RequestEvaluate(ByteBuffer buf, ClientThread client) {
		super(buf, client);
		_targetid = readD();
	}

	void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if(activeChar == null) return;

		if (!(activeChar.getTarget() instanceof L2PcInstance))
		{
			activeChar.sendPacket(new SystemMessage(SystemMessage.TARGET_IS_INCORRECT));
			return;
		}

		L2PcInstance target = (L2PcInstance)activeChar.getTarget(); 

		if(target == activeChar)
		{
			activeChar.sendPacket(new SystemMessage(SystemMessage.YOU_CANNOT_RECOMMEND_YOURSELF));
			return;
		}
		if(target.getRecomHave() >= 255)
		{
			activeChar.sendPacket(new SystemMessage(SystemMessage.THAT_CHARACTER_HAS_ALREADY_BEEN_RECOMMENDED));
			return;
		}
		if(activeChar.getRecomLeft() <= 0)
		{
			activeChar.sendPacket(new SystemMessage(SystemMessage.YOU_ARE_NOT_AUTHORIZED_TO_MAKE_FURTHER_RECOMMENDATIONS_AT_THIS_TIME_YOU_WILL_RECEIVE_MORE_RECOMMENDATION_CREDITS_EACH_DAY_AT_1_PM));
			return;
		}
		
		activeChar.setRecomLeft(activeChar.getRecomLeft()-1);
		target.setRecomHave(target.getRecomHave()+1);

		target.sendPacket(new SystemMessage(SystemMessage.YOU_HAVE_BEEN_RECOMMENDED_BY_S1).addString(activeChar.getName()));
		activeChar.sendPacket(new SystemMessage(SystemMessage.YOU_HAVE_RECOMMENDED_S1_YOU_ARE_AUTHORIZED_TO_MAKE_S2_MORE_RECOMMENDATIONS).addString(target.getName()).addNumber(activeChar.getRecomLeft()));
		target.broadcastUserInfo();
		activeChar.broadcastUserInfo();
	}
	
	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.clientpackets.ClientBasePacket#getType()
	 */
	public String getType() {
		return _C__B9_REQUESTEVALUATE;
	}
}
