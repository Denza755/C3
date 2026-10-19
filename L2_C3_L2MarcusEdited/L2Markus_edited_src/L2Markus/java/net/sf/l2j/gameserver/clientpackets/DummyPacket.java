/**
 * 
 */
package net.sf.l2j.gameserver.clientpackets;

import java.nio.ByteBuffer;

import net.sf.l2j.gameserver.network.L2GameClient;

/**
 * @author zabbix
 * Lets drink to code!
 */
public class DummyPacket extends L2GameClientPacket
{
	public DummyPacket(ByteBuffer buf, L2GameClient client)
	{
		super(buf,client);
	}

    @Override
    public void runImpl() { }

	public String getType()
	{
		return "DummyPacket";
	}
}
