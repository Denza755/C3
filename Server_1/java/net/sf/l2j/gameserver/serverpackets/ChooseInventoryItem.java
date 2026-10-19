package net.sf.l2j.gameserver.serverpackets;

public class ChooseInventoryItem extends L2GameServerPacket
{
	private static final String _S__6F_CHOOSEINVENTORYITEM = "[S] 6f ChooseInventoryItem";

	public ChooseInventoryItem()
	{
	}


	protected final void runImpl()
	{
		// no long-running tasks
	}
	
	protected final void writeImpl()
	{
		writeC(111);
	}

	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.serverpackets.ServerBasePacket#getType()
	 */
	public String getType()
	{
		return _S__6F_CHOOSEINVENTORYITEM;
	}
}
