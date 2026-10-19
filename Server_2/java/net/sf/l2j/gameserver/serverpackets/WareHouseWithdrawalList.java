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

import java.util.logging.Logger;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.model.L2Clan;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2PcInstance;

/**
 * 0x54 WarehouseWithdrawalList  dh (h dddhh dhhh d)
 * 
 * @version $Revision: 1.3.2.1.2.5 $ $Date: 2005/03/29 23:15:10 $
 */
public class WareHouseWithdrawalList extends ServerBasePacket
{
	private static Logger _log = Logger.getLogger(WareHouseWithdrawalList.class.getName());
	private static final String _S__54_WAREHOUSEWITHDRAWALLIST = "[S] 42 WareHouseWithdrawalList";
	private L2PcInstance _cha;
    private L2Clan _clan;
	private int _money;
	private L2ItemInstance[] _items;

	public WareHouseWithdrawalList(L2PcInstance cha)
	{
		_cha = cha;
        _clan = null;
		_money = cha.getAdena();
    }
    
    public WareHouseWithdrawalList(L2PcInstance cha, L2Clan clan)
    {
        _cha = cha;
        _clan = clan;
		_money = cha.getAdena();
	}	
	
	final void runImpl()
	{
        if (_clan == null) // player warehouse
        {
			_items = _cha.getWarehouse().listItems();
			
			for (int i = 0; i < _items.length; i++)
			{
				L2ItemInstance temp = _items[i];
				if (Config.DEBUG)
					_log.fine("item:" + temp.getItem().getName() +
							" type1:" + temp.getItem().getType1() + " type2:" + temp.getItem().getType2());
			}
        }
        else // clan warehouse
        {
            _items = _clan.getWarehouse().listItems();
            
            for (int i = 0; i < _items.length; i++)
            {
                L2ItemInstance temp = _items[i];
                if (Config.DEBUG)
                    _log.fine("item:" + temp.getItem().getName() +
                            " type1:" + temp.getItem().getType1() + " type2:" + temp.getItem().getType2());
            }
        }
	}
	
	final void writeImpl()
	{
		writeC(0x42);
		writeD(_money);
		writeH(_items.length);
		
		for (L2ItemInstance temp : _items)
		{
			writeH(temp.getItem().getType1()); // item type1 //unconfirmed, works
			writeD(temp.getObjectId()); //unconfirmed, works
			writeD(temp.getItemId()); //unconfirmed, works
			writeD(temp.getCount()); //unconfirmed, works
			writeH(temp.getItem().getType2());	// item type2 //unconfirmed, works
			writeH(0x00);	// ?
			writeD(temp.getItem().getBodyPart());	// ?
			writeH(temp.getEnchantLevel());	// enchant level -confirmed
			writeH(0x00);	// ?
			writeH(0x00);	// ?
			writeD(temp.getItemId()); // item id - confimed		
		}
	}
	
	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.serverpackets.ServerBasePacket#getType()
	 */
	public String getType()
	{
		return _S__54_WAREHOUSEWITHDRAWALLIST;
	}
}
