package net.sf.l2j.gameserver.clientpackets;

import net.sf.l2j.gameserver.ClientThread;
import net.sf.l2j.gameserver.packets.serverpackets.SystemMessage;

import java.nio.ByteBuffer;

public class RequestDeleteMacro extends ClientBasePacket
{  
	private int _id;
	
	private static final String _C__C2_REQUESTDELETEMACRO = "[C] C2 RequestDeleteMacro";
	
	/**
	 * packet type id 0xc2
	 * 
	 * sample
	 * 
	 * c2
	 * d // macro id
	 * 
	 * format:		cd
	 * @param decrypt
	 */
	public RequestDeleteMacro(ByteBuffer buf, ClientThread client)
	{
		super(buf, client);
		_id = readD();
		//System.out.println("Delete macro id="+_id);
	}
	
	void runImpl()
	{
		if (getClient().getActiveChar() == null)
		    return;
		getClient().getActiveChar().deleteMacro(_id);
		sendPacket(new SystemMessage(SystemMessage.S1_S2).addString("Delete macro id="+_id));
	}
	
	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.clientpackets.ClientBasePacket#getType()
	 */
	public String getType()
	{
		return _C__C2_REQUESTDELETEMACRO;
	}

}
