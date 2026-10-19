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
import java.util.Iterator;

import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2MerchantInstance;
import net.sf.l2j.gameserver.model.L2PcInstance;
import net.sf.l2j.gameserver.model.TradeItem;

/**
 * This class ...
 * 
 * @version $Revision: 1.2.2.3.2.6 $ $Date: 2005/03/27 15:29:57 $
 */
public class PrivateBuyListSell extends ServerBasePacket
{
	
	private static final String _S__B4_PRIVATEBUYLISTSELL = "[S] 9b PrivateBuyListSell";
	private final L2Character _seller;
	private final int _buyerAdena;
	private ArrayList<TradeItem> _sellList;
	private final boolean exchangeWithLeaseHolder;
	
	// player's private shop
	public PrivateBuyListSell(L2PcInstance buyer, L2PcInstance seller)
	{
		_seller = seller;
		_buyerAdena = buyer.getAdena();
		exchangeWithLeaseHolder = false;
	}
	// lease shop
	public PrivateBuyListSell(L2PcInstance buyer, L2MerchantInstance seller)
	{
		_seller = seller;
		_buyerAdena = buyer.getAdena();
		exchangeWithLeaseHolder = buyer.getObjectId() == seller.getLeaseHolderId();
	}
	
	final void runImpl()
	{
		if (_seller instanceof L2PcInstance) {
			L2PcInstance seller = (L2PcInstance)_seller;
			_sellList = seller.getSellList();
			for (Iterator<TradeItem> i=_sellList.iterator(); i.hasNext();) {
				TradeItem ti = i.next();
				L2ItemInstance inst = seller.getInventory().getItemByObjectId(ti.getObjectId());
				if (inst == null) {
					i.remove();
					continue;
				}
				if (ti.getCount() < inst.getCount())
					ti.setCount(inst.getCount());
				if (ti.getCount() <= 0) {
					i.remove();
					continue;
				}
				ti.setEnchantLevel(inst.getEnchantLevel());
			}
		} else {
			L2MerchantInstance seller = (L2MerchantInstance)_seller;
			_sellList = new ArrayList<TradeItem>();
			for (L2ItemInstance inst : seller.listLeaseItems()) {
				if (inst.getCount() <= 0)
					continue;
				TradeItem ti = new TradeItem();
				ti.setObjectId(inst.getObjectId());
				ti.setItemId(inst.getItemId());
				ti.setOwnersPrice(exchangeWithLeaseHolder ? 0 : inst.getPriceToSell());
				ti.setCount(inst.getCount());
				ti.setEnchantLevel(inst.getEnchantLevel());
				_sellList.add(ti);
			}
			if (exchangeWithLeaseHolder) {
				L2ItemInstance inst = seller.getLeaseAdena();
				TradeItem ti = new TradeItem();
				ti.setObjectId(inst.getObjectId());
				ti.setItemId(inst.getItemId());
				ti.setOwnersPrice(0);
				ti.setCount(inst.getCount());
				ti.setEnchantLevel(inst.getEnchantLevel());
				_sellList.add(ti);
			}
		}
	}
	
	final void writeImpl()
	{
		writeC(0x9b);
		writeD(_seller.getObjectId());
		writeD(_buyerAdena);
		
		writeD(_sellList.size());
		for (TradeItem ti : _sellList)
		{
			writeD(0x00);
			writeD(ti.getObjectId());
			writeD(ti.getItemId());
            writeD(ti.getCount());
			writeH(0x00);
			writeH(ti.getEnchantLevel());
			writeH(0x00);
			writeD(ti.getOwnersPrice());
			writeD(ti.getOwnersPrice());//temp2.getStorePrice());
			writeD(0); //c2
		}
	}
	
	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.serverpackets.ServerBasePacket#getType()
	 */
	public String getType()
	{
		return _S__B4_PRIVATEBUYLISTSELL;
	}

}
