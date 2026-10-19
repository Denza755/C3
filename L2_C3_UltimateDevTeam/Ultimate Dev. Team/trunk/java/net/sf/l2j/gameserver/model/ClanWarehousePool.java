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
 * [URL]http://www.gnu.org/copyleft/gpl.html[/URL]
 */
package net.sf.l2j.gameserver.model;

import javolution.util.FastList;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.packets.serverpackets.ItemList;
import net.sf.l2j.gameserver.packets.serverpackets.StatusUpdate;

import java.util.List;
import java.util.logging.Logger;

public class ClanWarehousePool
{
	private class ClanWarehouseWork
	{
		private L2PcInstance activeChar;
		private L2ItemInstance[] items;
		public boolean complete;

		public ClanWarehouseWork(L2PcInstance _activeChar, L2ItemInstance[] _items)
		{
			activeChar = _activeChar;
			items = _items;
			complete = false;
		}

		public void RunWork()
		{
			Warehouse warehouse2 = null;
			warehouse2 = activeChar.getClan().getWarehouse();

			for (int i = 0; i < items.length; i++)
			{
				L2ItemInstance old = warehouse2.destroyItem(items[i].getItemId(), items[i].getCount());
				if (old == null)
				{
					continue;
				}
				items[i].setEnchantLevel(old.getEnchantLevel());
				items[i].setPriceToBuy(old.getPriceToBuy());
				items[i].setPriceToSell(old.getPriceToSell());
				activeChar.getInventory().addItem(items[i]);
			}

			activeChar.sendPacket(new StatusUpdate(activeChar.getObjectId()).addAttribute(StatusUpdate.CUR_LOAD, activeChar.getCurrentLoad()));

			activeChar.sendPacket(new ItemList(activeChar, false));

			complete = true;
		}
	}

	private static final Logger _log = Logger.getLogger(ClanWarehousePool.class.getName());

	private static ClanWarehousePool _instance;
	private List<ClanWarehouseWork> _works;
	private boolean inWork;

	public static ClanWarehousePool getInstance()
	{
		if (_instance == null)
		{
			_instance = new ClanWarehousePool();
		}
		return _instance;
	}

	public ClanWarehousePool()
	{
		_works = new FastList<ClanWarehouseWork>();
		inWork = false;
	}

	public void AddWork(L2PcInstance _activeChar, L2ItemInstance[] _items)
	{
		_works.add(new ClanWarehouseWork(_activeChar, _items));
		if (Config.DEBUG) _log.warning("ClanWarehousePool: add work, work count " + _works.size());
		RunWorks();
	}

	private void RunWorks()
	{
		if (inWork)
		{
			if (Config.DEBUG)
				_log.warning("ClanWarehousePool: work in progress, work count " + _works.size());
			return;
		}

		inWork = true;
		try
		{
			if (_works.size() > 0)
			{
				ClanWarehouseWork cww = _works.get(0);
				if (!cww.complete)
				{
					if (Config.DEBUG)
						_log.warning("ClanWarehousePool: run work, work count " + _works.size());
					cww.RunWork();
				}
				_works.remove(0);
			}
		}
		catch (Exception e)
		{
			_log.warning("Error ClanWarehousePool: " + e);
		}
		inWork = false;

		if (_works.size() > 0) RunWorks();
	}
}