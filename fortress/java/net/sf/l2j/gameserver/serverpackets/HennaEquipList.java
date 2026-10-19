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

public class HennaEquipList extends ServerBasePacket
{
    //private static Logger _log = Logger.getLogger(RequestHennaItemInfo.class.getName());

		private static final String _S__E2_HennaEquipList = "[S] E2 HennaEquipList";
		
		private L2PcInstance _player;
		private L2HennaInstance[] _hennaEquipList;
		
		public HennaEquipList(L2PcInstance player,L2HennaInstance[] hennaEquipList)
		{
			_player = player;  
			_hennaEquipList = hennaEquipList;
		}
		
		
		final void runImpl()
		{
			// no long-running tasks
		}
		
    
		final void writeImpl()
		{
			
		writeC(0xe2);
 		writeD(_player.getAdena());          //activeChar current amount of aden
		writeD(3);     //avaliable equip slot
		//writeD(10);    // total amount of symbol available which depends on difference classes
		writeD(_hennaEquipList.length);
		
		for (int i = 0; i < _hennaEquipList.length; i++)
		{
			writeD(_hennaEquipList[i].getSymbolId()); //symbolid
			writeD(_hennaEquipList[i].getItemIdDye());       //itemid of dye
			writeD(_hennaEquipList[i].getAmountDyeRequire());    //amount of dye require
			writeD(_hennaEquipList[i].getPrice());    //amount of aden require
			writeD(i);            //meet the requirement or not
		}
		
/*	
		writeD(13);    //symbolid
		writeD(4457);  //itemid of dye 
		writeD(5);     //amount of dye require
		writeD(87000); //amount of aden require
		writeD(0);     //meet the requirement or not
		
		writeD(23);
		writeD(4467);
		writeD(5);
		writeD(87000);
		writeD(0);
		
		writeD(18);
		writeD(4462);
		writeD(5);
		writeD(87000);
		writeD(0);
		
		writeD(42);
		writeD(4486);
		writeD(10);
		writeD(87000);
		writeD(1);
		
		writeD(39);
		writeD(4483);
		writeD(10);
		writeD(87000);
		writeD(1);
		
		writeD(15);
		writeD(4459);
		writeD(20);
		writeD(87000);
		writeD(0);
		
		writeD(17);
		writeD(4461);
		writeD(15);
		writeD(87000);
		writeD(0);
		
		writeD(24);
		writeD(4468);
		writeD(10);
		writeD(87000);
		writeD(0);
		
		writeD(21);
		writeD(4465);
		writeD(10);
		writeD(87000);
		writeD(0);

		writeD(20);
		writeD(4464);
		writeD(10);
		writeD(87000);
		writeD(0);
*/	
		}


	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.serverpackets.ServerBasePacket#getType()
	 */
	public String getType()
	{
		return _S__E2_HennaEquipList;
	}
}
