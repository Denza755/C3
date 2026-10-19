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

import java.nio.ByteBuffer;

/**
 * Format: (c) Sd
 * @author -Wooden-
 *
 */
public class RequestPetition extends ClientBasePacket
{
	private static final String _C__7F_REQUEST_PETITION = "[C] 7f RequestPetition";
	private String _text;
	private int _type;

	/**
	 * @param buf
	 * @param client
	 */
	public RequestPetition(ByteBuffer buf, ClientThread client)
	{
		super(buf, client);
		_text = readS();
		_type = readD();
	}

	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.clientpackets.ClientBasePacket#runImpl()
	 */
	@Override
	void runImpl()
	{
		System.out.println("Petition: txt:"+_text+" type:"+_type);
		
	}

	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.BasePacket#getType()
	 */
	@Override
	public String getType()
	{
		return _C__7F_REQUEST_PETITION;
	}
	
}