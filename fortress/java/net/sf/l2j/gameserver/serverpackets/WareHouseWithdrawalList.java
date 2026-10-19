/*
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2, or (at your option) any later version. This
 * program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place - Suite 330, Boston, MA 02111-1307, USA.
 * http://www.gnu.org/copyleft/gpl.html
 */
package net.sf.l2j.gameserver.serverpackets;

import java.util.logging.Logger;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.model.L2Clan;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2PcInstance;
import net.sf.l2j.gameserver.model.Warehouse;

/**
 * 0x54 WarehouseWithdrawalList dh (h dddhh dhhh d)
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
        if(_clan == null && !_cha.isUsingFreight()) // player warehouse
        {
            _items = _cha.getWarehouse().listItems();
        }
        else if(_clan != null && !_cha.isUsingFreight()) // clan warehouse
        {
            _items = _clan.getWarehouse().listItems();
        }
        else if(_clan == null && _cha.isUsingFreight()) // Freight withdraw
        {
            Warehouse wh = _cha.getFreight();
            wh.setLocationId(null);
            _items = wh.listItems();
        }
        else
        {
            // Something went wrong!
            _log.warning("error while sending withdraw request to: " + _cha.getName());
            return;
        }

        if(Config.DEBUG)
            for(int i = 0; i < _items.length; i++)
            {
                L2ItemInstance temp = _items[i];
                _log.fine("item:" + temp.getItem().getName() + " type1:"
                        + temp.getItem().getType1() + " type2:"
                        + temp.getItem().getType2());
            }
    }

    final void writeImpl()
    {
        writeC(0x42);
		if (getClient().getRevision() > 560)
		{
			/* 0x01-Private Warehouse
			 * 0x02-Clan Warehouse
			 * 0x03-Castle Warehouse
			 * 0x04-Warehouse */
			if(_clan == null)
				writeH(0x01);
			else if(_clan != null)
				writeH(0x02);
			else
				writeH(0x01);
		}
        writeD(_money);
        writeH(_items.length);
        if(Config.DEBUG) _log.fine("Send withdrawallist to: " + _cha.getName());
        for(L2ItemInstance temp : _items)
        {
            writeH(temp.getItem().getType1()); // unconfirmed, works
            writeD(temp.getObjectId()); // unconfirmed, works
            writeD(temp.getItemId()); // unconfirmed, works
            writeD(temp.getCount()); // unconfirmed, works
            writeH(temp.getItem().getType2()); // unconfirmed, works
            writeH(0); // ?
            writeD(temp.getItem().getBodyPart()); // ?
            writeH(temp.getEnchantLevel()); // enchant level -confirmed
            writeH(0); // ?
            writeH(0); // ?
            writeD(temp.getObjectId()); // return value for define item (object_id)
            if(Config.DEBUG)
                _log.fine("OID: " + temp.getObjectId() + ", ID: " + temp.getItemId()
                        + ", count: " + temp.getCount() + ", enchant: "
                        + temp.getEnchantLevel());
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.sf.l2j.gameserver.serverpackets.ServerBasePacket#getType()
     */
    public String getType()
    {
        return _S__54_WAREHOUSEWITHDRAWALLIST;
    }
}
