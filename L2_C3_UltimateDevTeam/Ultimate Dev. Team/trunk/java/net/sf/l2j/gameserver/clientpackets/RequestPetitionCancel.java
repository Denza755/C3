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
import net.sf.l2j.util.Util;

import java.nio.ByteBuffer;

public class RequestPetitionCancel extends ClientBasePacket
{
	private static final String _C__80_REQUEST_PETITIONCANCEL = "[C] 80 RequestPetitionCancel";
	@SuppressWarnings("unused")
	private String _text;
	/**
	 * @param buf
	 * @param client
	 */
	public RequestPetitionCancel(ByteBuffer buf, ClientThread client)
	{
		super(buf, client);
		//_text = readS();
		int size = buf.remaining(); 
     	System.out.println("petition cancel"); 
     	byte[] array = new byte[size]; 
     	buf.get(array); 
     	System.out.println(Util.printData(array, size)); 
	}

	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.clientpackets.ClientBasePacket#runImpl()
	 */
	@Override
	void runImpl()
	{
		//System.out.println("PetitionCancel:"+_text);
		
	}

	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.BasePacket#getType()
	 */
	@Override
	public String getType()
	{
		return _C__80_REQUEST_PETITIONCANCEL;
	}
	
}