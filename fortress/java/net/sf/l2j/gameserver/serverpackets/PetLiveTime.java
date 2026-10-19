package net.sf.l2j.gameserver.serverpackets;

import net.sf.l2j.gameserver.model.L2Summon;

public class PetLiveTime extends ServerBasePacket {

	private static final String _S__D1_PETLIVETIME = "[S] D1 PetLiveTime";

	private final int _maxFed;
	private final int _curFed;

	/**
	 * packet type id 0xd1
	 * format: cdd
	 * @param summon
	 */
	public PetLiveTime(L2Summon summon)
	{
		_curFed = summon.getCurrentFed();
		_maxFed = summon.getMaxFed();
	}

	void runImpl()
	{
		//no long-running tasks
	}

	void writeImpl()
	{
		writeC(0xD1);
		writeD(_maxFed);
		writeD(_curFed);
	}

	public String getType() {return _S__D1_PETLIVETIME;}
}
