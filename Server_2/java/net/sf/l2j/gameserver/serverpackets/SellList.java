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

import java.util.ArrayList;
import java.util.logging.Logger;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2MerchantInstance;
import net.sf.l2j.gameserver.model.L2PcInstance;

/**
 * This class ...
 * 
 * @version $Revision: 1.4.2.3.2.4 $ $Date: 2005/03/27 15:29:39 $
 */
public class SellList extends ServerBasePacket
{
	private static final String _S__10_SELLLIST = "[S] 10 SellList";
	private static Logger _log = Logger.getLogger(SellList.class.getName());
	private final L2PcInstance _char;
	private final L2MerchantInstance _lease;
	private int _money;
	private ArrayList<L2ItemInstance> _selllist = new ArrayList<L2ItemInstance>();
	
	public SellList(L2PcInstance player)
	{
		_char = player;
		_lease = null;
		_money = _char.getAdena();
	}
	
	public SellList(L2PcInstance player, L2MerchantInstance lease)
	{
		_char = player;
		_lease = lease;
		_money = _char.getAdena();
	}
	
	final void runImpl()
	{
		if (_lease == null)
		{
			for (L2ItemInstance item : _char.getInventory().getItems())
			{
				if (!item.isEquipped() && item.getItem().isSellable())
				{
					_selllist.add(item);
					if (Config.DEBUG) _log.fine("item added to selllist: " + item.getItem().getName());
				}
			}
		}
		else
		{
			L2ItemInstance[] leaseList = _lease.listLeaseItems();
			L2ItemInstance[] playerList = _char.getInventory().getItems();
			for (L2ItemInstance li : leaseList)
			{
				for (L2ItemInstance item : playerList)
				{
					if (item.getItemId() != li.getItemId() || item.getEnchantLevel() != li.getEnchantLevel())
						continue;
					if (item.isEquipped() || !item.getItem().isSellable())
						continue;
					if (li.getPriceToBuy() == 0 && _char.getObjectId() != _lease.getLeaseHolderId())
						continue; // lease holder don't want to buy it
					_selllist.add(item);
					if (Config.DEBUG) _log.fine("item added to selllist: " + item.getItem().getName());
				}
			}
			if (_lease.getLeaseHolderId() == _char.getObjectId())
				_selllist.add(_char.getInventory().getAdenaInstance());
		}
	}
	
	final void writeImpl()
	{
		writeC(0x10);
		writeD(_money);
		writeD(_lease == null ? 0x00 : 1000000 + _lease.getTemplate().npcId);
		
		writeH(_selllist.size());
		
		for (L2ItemInstance item : _selllist)
		{
			writeH(item.getItem().getType1());
			writeD(item.getObjectId());
			writeD(item.getItemId());
			writeD(item.getCount());
			writeH(item.getItem().getType2());
			writeH(0x00);
			writeD(item.getItem().getBodyPart());
			writeH(item.getEnchantLevel());
			writeH(0x00);
			writeH(0x00);
			
			if (_lease == null)
				writeD(item.getItem().getReferencePrice()/2);
			else if (_lease.getLeaseHolderId() == _char.getObjectId())
				writeD(0);
			else if (_lease.getLeaseHolderId() != _char.getObjectId())
				writeD(item.getPriceToBuy()); // player sells, thus merchant buys.
		}
	}

	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.serverpackets.ServerBasePacket#getType()
	 */
	public String getType()
	{
		return _S__10_SELLLIST;
	}
}
