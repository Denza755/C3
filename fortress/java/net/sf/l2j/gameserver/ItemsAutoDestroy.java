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
package net.sf.l2j.gameserver;

import java.util.concurrent.CopyOnWriteArraySet;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2World;


public class ItemsAutoDestroy
{
	private static ItemsAutoDestroy _instance;
	CopyOnWriteArraySet<L2ItemInstance> _items = null;
	
	private ItemsAutoDestroy()
	{
		_items = new CopyOnWriteArraySet<L2ItemInstance>();
		ClientScheduler.getInstance().scheduleLow(new CheckItemsForDestroy(), Config.AUTODESTROY_ITEM_AFTER * 1000);
	}
	
	public static ItemsAutoDestroy getInstance()
	{
		if (_instance == null)
		{
			_instance = new ItemsAutoDestroy();
		}
		
		return _instance;
	}
	
	public void addItem(L2ItemInstance item)
	{
		item.setDropTime(System.currentTimeMillis());
		_items.add(item);
	}

	public CopyOnWriteArraySet<L2ItemInstance> getKnownItems()
	{
		return _items;
	}
	
	class CheckItemsForDestroy extends Thread
	{
		public void run()
		{
			long _sleep	= Config.AUTODESTROY_ITEM_AFTER * 1000;
			try 
			{
				long curtime = System.currentTimeMillis();
				for(L2ItemInstance item : _items)
				{
					if(item == null 	
							|| item.getDropTime()==0 
							|| item.getLocation() != L2ItemInstance.ItemLocation.VOID)
						_items.remove(item);
					else 
					{
	 					if(item.getDropTime() + _sleep < curtime)
	 					{
							L2World.removeVisibleObject(item, item.getCurrentWorldRegion());
							L2World.removeObject(item);
							_items.remove(item);
	  					}
					}
	  			}
	 		} catch (Exception e) {e.printStackTrace();}

			ClientScheduler.getInstance().scheduleLow(new CheckItemsForDestroy(), _sleep);
		}	
	}
}
