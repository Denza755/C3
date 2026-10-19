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
import net.sf.l2j.gameserver.packets.serverpackets.CharSelected;

import java.nio.ByteBuffer;

/**
 * This class ...
 * 
 * @version $Revision: 1.5.2.1.2.5 $ $Date: 2005/03/27 15:29:30 $
 */
public class CharacterSelected extends ClientBasePacket
{
	private static final String _C__0D_CHARACTERSELECTED = "[C] 0D CharacterSelected";

	// cd
	private final int _charSlot;
	
	@SuppressWarnings("unused")
	private final int _unk1; 	// new in C4
	@SuppressWarnings("unused")
	private final int _unk2;	// new in C4
	@SuppressWarnings("unused")
	private final int _unk3;	// new in C4
	@SuppressWarnings("unused")
	private final int _unk4;	// new in C4

	/**
	 * @param decrypt
	 */
	public CharacterSelected(ByteBuffer buf, ClientThread client)
	{
		super(buf, client);
		_charSlot = readD();
		_unk1 = readH();
		_unk2 = readD();
		_unk3 = readD();
		_unk4 = readD();
	}

	void runImpl()
	{

		//loadup character from disk
		L2PcInstance cha = getClient().loadCharFromDisk(_charSlot);

		getClient().setActiveChar(cha);
		
		if(cha.getAccessLevel() < 0)
		{
			cha.closeNetConnection();
			return;
		}
		//weird but usefull, will send i..
		//cha.setAccessLevel(cha.getAccessLevel());
		CharSelected cs = new CharSelected(cha, getClient().getSessionId());
		sendPacket(cs);
	}
	
	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.clientpackets.ClientBasePacket#getType()
	 */
	public String getType()
	{
		return _C__0D_CHARACTERSELECTED;
	}	
}
