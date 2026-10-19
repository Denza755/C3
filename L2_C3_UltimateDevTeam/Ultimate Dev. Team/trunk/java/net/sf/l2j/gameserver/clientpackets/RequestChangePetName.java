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
import net.sf.l2j.gameserver.model.L2Summon;
import net.sf.l2j.gameserver.packets.serverpackets.NpcInfo;
import net.sf.l2j.gameserver.packets.serverpackets.PetInfo;

import java.nio.ByteBuffer;

/**
 * This class ...
 * 
 * @version $Revision: 1.3.4.4 $ $Date: 2005/04/06 16:13:48 $
 */
public class RequestChangePetName extends ClientBasePacket{
	private static final String REQUESTCHANGEPETNAME__C__89 = "[C] 89 RequestChangePetName";
	
	private final String _name;
	
	public RequestChangePetName(ByteBuffer buf, ClientThread client)
	{
		super(buf, client);
		_name = readS();
	}

	void runImpl()
	{
		final L2Summon pet = getClient().getActiveChar().getPet(); 
		if (pet != null && pet.getName()==null)
		{
			pet.setName(_name);
			pet.broadcastPacket(new NpcInfo(pet, getClient().getActiveChar()));
			sendPacket(new PetInfo(pet));
		}
	} 
	
	public String getType()
	{
		return REQUESTCHANGEPETNAME__C__89;
	}
}
