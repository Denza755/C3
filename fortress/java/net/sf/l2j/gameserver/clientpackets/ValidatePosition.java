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
import java.util.logging.Logger;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.ClientThread;
import net.sf.l2j.gameserver.model.L2PcInstance;
import net.sf.l2j.gameserver.serverpackets.ValidateLocation;

/**
 * This class ...
 * 
 * @version $Revision: 1.13.4.7 $ $Date: 2005/03/27 15:29:30 $
 */
public class ValidatePosition extends ClientBasePacket
{
	private static Logger _log = Logger.getLogger(ValidatePosition.class.getName());
	private static final String _C__48_VALIDATEPOSITION = "[C] 48 ValidatePosition";

	private final int _x;
	private final int _y;
	private final int _z;
	private final int _heading;
	@SuppressWarnings("unused")
	private final int _data;
	/**
	 * packet type id 0x48
	 * format:		cddddd
	 * @param decrypt
	 */
	public ValidatePosition(ByteBuffer buf, ClientThread client)
	{
		super(buf, client);
		_x  = readD();
		_y  = readD();
		_z  = readD();
		_heading  = readD();
		_data  = readD();
	}

	void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
        if (activeChar == null || activeChar.isTeleporting()) return;
		activeChar.setClientX(_x);
		activeChar.setClientY(_y);
		activeChar.setClientZ(_z);
		activeChar.setClientHeading(_heading);
		int realX = activeChar.getX();
		int realY = activeChar.getY();
		// int realZ = activeChar.getZ();
		activeChar.setXYZ(activeChar.getX(), activeChar.getY(), _z);
		// int realHeading = activeChar.getHeading();

		if(Config.DEVELOPER)
		{
			int dx = _x - realX;
			int dy = _y - realY;
			int diff2 = (dx * dx + dy * dy);
			if(diff2 > 1000000)
			{
				if(Config.DEBUG)
					_log.fine("client/server dist diff " + (int)Math.sqrt(diff2));
				sendPacket(new ValidateLocation(activeChar));
			}
		}
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.l2j.gameserver.clientpackets.ClientBasePacket#getType()
	 */
	public String getType()
	{
		return _C__48_VALIDATEPOSITION;
	}
	
}
