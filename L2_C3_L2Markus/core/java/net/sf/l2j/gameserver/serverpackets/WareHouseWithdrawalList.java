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
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;

/**
 * 0x54 WarehouseWithdrawalList  dh (h dddhh dhhh d)
 *
 * @version $Revision: 1.3.2.1.2.5 $ $Date: 2005/03/29 23:15:10 $
 */
public class WareHouseWithdrawalList extends L2GameServerPacket
{
	public static final int PRIVATE = 1;
	public static final int CLAN = 2;
	public static final int CASTLE = 3; //not sure
	public static final int FREIGHT = 4; //not sure
	private static Logger _log = Logger.getLogger(WareHouseWithdrawalList.class.getName());
	private static final String _S__54_WAREHOUSEWITHDRAWALLIST = "[S] 42 WareHouseWithdrawalList";
	private L2PcInstance _player;
	private int _playerAdena;
	private L2ItemInstance[] _items;
	private int _whtype;

	public WareHouseWithdrawalList(L2PcInstance player, int type)
	{
		_player = player;
		_whtype = type;
    }

	protected final void runImpl()
	{
		_playerAdena = _player.getAdena();
		if (_player.getActiveWarehouse() == null)
		{
            // Something went wrong!
            _log.warning("error while sending withdraw request to: " + _player.getName());
            return;
		}
		else _items = _player.getActiveWarehouse().getItems();

		if (Config.DEBUG)
			for (L2ItemInstance item : _items)
				_log.fine("item:" + item.getItem().getName() +
						" type1:" + item.getItem().getType1() + " type2:" + item.getItem().getType2());
	}

    protected final void writeImpl()
    {
        writeC(66);
        writeD(_playerAdena);
        writeH(_items.length);
        L2ItemInstance arr$[] = _items;
        int len$ = arr$.length;
        for(int i$ = 0; i$ < len$; i$++)
        {
            L2ItemInstance temp = arr$[i$];
            writeH(temp.getItem().getType1());
            writeD(temp.getObjectId());
            writeD(temp.getItemId());
            writeD(temp.getCount());
            writeH(temp.getItem().getType2());
            writeH(0);
            writeD(temp.getItem().getBodyPart());
            writeH(temp.getEnchantLevel());
            writeH(0);
            writeH(0);
            writeD(temp.getObjectId());
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
