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
package net.sf.l2j.gameserver.serverpackets;

/**
 * chddd<BR>
 * c = 0xfe<BR>
 * h = 0x28<BR>
 * d = unknow<BR>
 * d = LargeCrestId<BR>
 * d = LargeCrestSize<BR>
 */
public class ExPledgeCrestLarge extends ServerBasePacket
{
	private static final String _S__FE_28_EXPLEDGECRESTLARGE = "[S] FE:28 ExPledgeCrestLarge";

	private int _LargeCrestId, _LargeCrestSize;
	private byte[] _Data;
	
	public ExPledgeCrestLarge(int largeCrestId, byte[] data)
	{
		_LargeCrestId = largeCrestId;
		_Data = data;
		_LargeCrestSize = _Data.length;
	}
	
	final void runImpl()
	{
		// no long-running tasks
	}
	
	final void writeImpl()
	{
		writeC(0xfe);
		writeH(0x28);
		writeD(0x00);
		writeD(_LargeCrestId);
		writeD(_LargeCrestSize);
		writeB(_Data);
	}
	
	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.serverpackets.ServerBasePacket#getType()
	 */
	public String getType()
	{
		return _S__FE_28_EXPLEDGECRESTLARGE;
	}

}
