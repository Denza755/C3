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

import java.nio.ByteBuffer;

import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.serverpackets.BeginRotation;
import net.sf.l2j.Config;

public final class StartRotating extends L2GameClientPacket
{
    private static final String _C__4A_STARTROTATION = "[C] 4A StartRotating";
    private int _degree;
	private int _side;

    public StartRotating(ByteBuffer buf, L2GameClient client)
	{
		super(buf, client);
		_degree = readD();
		_side = readD();
	}

    @Override
	protected void runImpl()
	{
		if(getClient().getActiveChar() == null)
			return;

		if(!Config.ALLOW_USE_CURSOR_FOR_WALK){
			getClient().getActiveChar().sendPacket(new ActionFailed());
			return;
		}

		BeginRotation br = new BeginRotation(getClient().getActiveChar(), _degree, _side, 0);
		getClient().getActiveChar().broadcastPacket(br);
	}

	@Override
	public String getType()
	{
		return _C__4A_STARTROTATION;
	}
}
