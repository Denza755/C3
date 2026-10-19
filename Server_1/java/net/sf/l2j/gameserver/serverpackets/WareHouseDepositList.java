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
 * 0x53 WareHouseDepositList  dh (h dddhh dhhh d)
 *
 * @version $Revision: 1.4.2.1.2.4 $ $Date: 2005/03/27 15:29:39 $
 */
public class WareHouseDepositList extends L2GameServerPacket
{
	public static final int PRIVATE = 1;
	public static final int CLAN = 2;
	public static final int CASTLE = 3; //not sure
	public static final int FREIGHT = 4; //not sure
	private static Logger _log = Logger.getLogger(WareHouseDepositList.class.getName());
	private static final String _S__53_WAREHOUSEDEPOSITLIST = "[S] 41 WareHouseDepositList";
	private L2PcInstance _player;
	private int _playerAdena;
	private L2ItemInstance[] _items;
	private int _whtype;

	public WareHouseDepositList(L2PcInstance player, int type)
	{
        _player = player;
		_whtype = type;
	}

	protected final void runImpl()
	{
		_playerAdena = _player.getAdena();
		_items = _player.getInventory().getAvailableItems(true);
	}

    protected final void writeImpl()
    {
        writeC(65);

        writeD(_playerAdena);
        int count = _items.length;
        if(Config.DEBUG)
            _log.fine((new StringBuilder()).append("count:").append(count).toString());
        writeH(count);
        for(int i = 0; i < count; i++)
        {
            L2ItemInstance temp = _items[i];
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
		return _S__53_WAREHOUSEDEPOSITLIST;
	}
}
