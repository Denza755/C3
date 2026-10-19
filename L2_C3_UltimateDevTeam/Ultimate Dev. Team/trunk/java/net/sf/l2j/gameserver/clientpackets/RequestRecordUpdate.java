/*
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA.
 *
 * http://www.gnu.org/copyleft/gpl.html
 */
package net.sf.l2j.gameserver.clientpackets;

import net.sf.l2j.gameserver.ClientThread;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.packets.serverpackets.RecordUpdate;

import java.nio.ByteBuffer;

/**
 * Seven Signs Record Update Request
 * 
 * packet type id 0xc7
 * format: cc
 * 
 * @author Tempy
 */
public class RequestRecordUpdate extends ClientBasePacket
{
	private static final String _C__C7_RequestRecordUpdate = "[C] C7 RequestRecordUpdate";
	
	private int _page;
	
	public RequestRecordUpdate(ByteBuffer buf, ClientThread client)
	{
		super(buf, client);
		_page = readC();
	}

	void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar(); 
		if (activeChar == null)
		    return;
		
		activeChar.sendPacket(new RecordUpdate(activeChar, _page));
	}

	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.clientpackets.ClientBasePacket#getType()
	 */
	public String getType()
	{
		return _C__C7_RequestRecordUpdate;
	}
}
