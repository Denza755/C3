/*
 * $Header$
 *
 * 
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

import net.sf.l2j.gameserver.model.L2HennaInstance;
import net.sf.l2j.gameserver.model.L2PcInstance;


public class HennaInfo extends ServerBasePacket
{

		private static final String _S__E4_HennaInfo = "[S] E4 HennaInfo";
		
		
		//private int _id;
		private L2PcInstance _player;
		private L2HennaInstance _henna;
		
		public HennaInfo(L2HennaInstance henna, L2PcInstance player)
		{
			_henna = henna;			
			_player = player;                   
				
		}
		final void runImpl()
		{
			// no long-running tasks
		}
		
    
		final void writeImpl()
		{
			
		writeC(0xe4);
		writeC(_henna.getStatINT());    //equip INT
		writeC(_henna.getStatSTR());   //equip STR
		writeC(_henna.getStatCON());    //equip CON
		writeC(_henna.getStatMEM());		//equip MEM
		writeC(_henna.getStatDEX());		//equip DEX
		writeC(_henna.getStatWIT());		//equip WIT
		writeD(3);		//maybe totol number of slot
		
		writeD(1);     //slot 1
		writeD(_henna.getSymbolId());		//symbolId
		writeD(1);		//??
		}


	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.serverpackets.ServerBasePacket#getType()
	 */
	public String getType()
	{
		return _S__E4_HennaInfo;
	}
}
