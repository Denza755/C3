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

import net.sf.l2j.gameserver.model.L2ShortCut;

/**
 * 
 *
 * sample
 *  
 * 57 
 * 0d 00 00 00 
 * 03 00 00 00  00 00 00 00  02 00 00 00  01 00 00 00 
 * 01 00 00 00  01 00 00 00  46 28 91 40  01 00 00 00 
 * 02 00 00 00  02 00 00 00  03 00 00 00  06 00 00 00  01 00 00 00  
 * 02 00 00 00  03 00 00 00  38 00 00 00  06 00 00 00  01 00 00 00  
 * 01 00 00 00  04 00 00 00  5f 37 32 43  01 00 00 00  
 * 03 00 00 00  05 00 00 00  05 00 00 00  01 00 00 00  
 * 01 00 00 00  06 00 00 00  3a df c3 41  01 00 00 00  
 * 01 00 00 00  07 00 00 00  5d 69 d1 41  01 00 00 00 
 * 01 00 00 00  08 00 00 00  7b 86 73 42  01 00 00 00 
 * 03 00 00 00  09 00 00 00  00 00 00 00  01 00 00 00 
 * 02 00 00 00  0a 00 00 00  4d 00 00 00  01 00 00 00  01 00 00 00 
 * 02 00 00 00  0b 00 00 00  5b 00 00 00  01 00 00 00  01 00 00 00 
 * 01 00 00 00  0c 00 00 00  5f 37 32 43  01 00 00 00
 
 * format   d *(1dddd)/(2ddddd)/(3dddd)
 * 
 * 
 * @version $Revision: 1.3.2.1.2.4 $ $Date: 2005/03/27 15:29:39 $
 */
public class ShortCutInit extends ServerBasePacket
{
	private static final String _S__57_SHORTCUTINIT = "[S] 45 ShortCutInit";
	private L2ShortCut[] _shortCuts;

	public ShortCutInit()
	{
	}	
	
	final void runImpl()
	{
		if (getClient().getRevision() >= 514) {
			_shortCuts = getClient().getActiveChar().getAllShortCuts();
		} else {
			L2ShortCut[] tmp = getClient().getActiveChar().getAllShortCuts();
			int n = 0;
			for (L2ShortCut sc : tmp) {
				if (sc.slot < 10)
					n++;
			}
			if (n == tmp.length) {
				_shortCuts = tmp;
			} else {
				_shortCuts = new L2ShortCut[n];
				n = 0;
				for (L2ShortCut sc : tmp) {
					if (sc.slot < 10)
						_shortCuts[n++] = sc;
				}
			}
	
		}
	}
	
	final void writeImpl()
	{
		writeC(0x45);
		writeD(_shortCuts.length);

		for (int i = 0; i < _shortCuts.length; i++)
			{
			L2ShortCut sc = _shortCuts[i];
			writeD(sc.type);			
			if (getClient().getRevision() >= 514)
				writeD(sc.slot + sc.page*12);
			else
				writeD(sc.slot + sc.page*10);
			writeD(sc.id);
			if (sc.level > -1)
				writeD(sc.level);
			writeD(sc.unk);
		}
	}

	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.serverpackets.ServerBasePacket#getType()
	 */
	public String getType()
	{
		return _S__57_SHORTCUTINIT;
	}
}
