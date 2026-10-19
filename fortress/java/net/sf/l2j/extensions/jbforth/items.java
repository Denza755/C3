package net.sf.l2j.extensions.jbforth;

import java.util.logging.Logger;

import net.sf.l2j.gameserver.ItemTable;
import ru.balancer.JBForth;
import ru.balancer.jbforth.Dict;
import ru.balancer.jbforth.Word;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2PcInstance;
import net.sf.l2j.gameserver.model.Inventory;
import net.sf.l2j.gameserver.serverpackets.ItemList;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;

public class items
{
	static Logger _log = Logger.getLogger(items.class.getName());

	public items(Dict d)
	{
		d.reg("INVENTORY+!", new inventory_add());
		d.reg("INVENTORY-!", new inventory_del());
		d.reg("INVENTORY?", new inventory_exists());
		d.reg("ADENA@", new adena_at());
		d.reg("ADENA+!", new adena_add());
		d.reg("ADENA-!", new adena_sub());
		d.reg("item_spawn", new item_spawn());
		d.reg("item_unspawn", new item_unspawn());

		d.reg("enchant-level@", new item_get_enchant());
		d.reg("enchant-level!", new item_set_enchant());
		d.reg("item-name@", new item_name());
	}

	public class item_name extends Word
	{
//		( player item_id -- item_name)
		public void exec(JBForth jbf)
		{
			Integer itemid = jbf.st.pop_i();
			L2ItemInstance item = ((L2PcInstance)jbf.st.pop()).getInventory().getItemByItemId(itemid);
			jbf.st.push(item);
		}
	}

	public class item_get_enchant extends Word
	{
//	  ( player item_id -- enchanlevel ) 
		public void exec(JBForth jbf) 
		{ 
			Integer itemid = jbf.st.pop_i();
			L2ItemInstance item = ((L2PcInstance)jbf.st.pop()).getInventory().getItemByItemId(itemid);
			jbf.st.push(item.getEnchantLevel());
		}
	}

	public class item_set_enchant extends Word
	{
//	  ( player item_id enchanlevel -- ) 
		public void exec(JBForth jbf)
		{ 
			Integer el = jbf.st.pop_i();
			Integer itemid = jbf.st.pop_i();
			L2ItemInstance item = ((L2PcInstance)jbf.st.pop()).getInventory().getItemByItemId(itemid);
			item.setEnchantLevel(el);
		}
	}

	public class inventory_add extends Word
	{
//	  ( item_id count player -- ) 
		public void exec(JBForth jbf) 
		{ 
			L2PcInstance player = (L2PcInstance)jbf.st.pop();
			Integer count = jbf.st.pop_i();
			Integer item_id = jbf.st.pop_i();

			L2ItemInstance item = ItemTable.getInstance().createItem(item_id);
	
			if(!item.isStackable())
			{
				for (int i=0; i<count; i++)
					player.getInventory().addItem(ItemTable.getInstance().createItem(item_id));
			} 
			else 
			{
				item.setCount(item.getCount()+(count-1));
				player.getInventory().addItem(item);
			}

			player.sendPacket(new ItemList(player, false));
			player.sendPacket((new SystemMessage(SystemMessage.EARNED_S2_S1_s)).addItemName(item_id).addNumber(count));
		}
	}

	public class inventory_del extends Word
	{
//	  	inventory-!  ( item_id count player -- flag/count )
//		return: 	null - some error in input data
//					0 - operation success
//					else - count of not enough items, diff from items needed to remove count and real items count.

		public void exec(JBForth jbf) 
		{ 
			L2PcInstance player = (L2PcInstance)jbf.st.pop();
			Integer count = jbf.st.pop_i();
			Integer removed = count;
			Integer item_id = jbf.st.pop_i();

			if(player == null || item_id == null || item_id == 0)
			{
				jbf.st.push(null);
				return;
			}

			if(count <= 0)
			{
				jbf.st.push(null);
				return;
			}

			Inventory inv = player.getInventory();

			if(inv == null)
			{
				jbf.st.push(count);
				return;
			}

			String name = "item";

			L2ItemInstance[] items = inv.getItems();
			synchronized(items)
			{
				for(L2ItemInstance item : items)
				{
					if(item.getItemId() == item_id)
					{
						name = item.getItem().getName();
						if(count > 0)
						{
							int item_count = item.getCount();
							int rem = (count<=item_count) ? count : item_count;
							player.getInventory().destroyItemByItemId(item_id, rem);
							count -= rem;
						}
					}
				}
			}
			
			jbf.st.push(count);

			player.sendPacket(new ItemList(player, false));
				
			removed -= count;
			
			player.sendMessage((removed>1 ? ""+removed+" ": "")+name+(removed>1 ? "" : "s") + " removed from your inventory.");
		}
	}

	public class inventory_exists extends Word
	{
//	  	inventory?  ( item_id player -- flag/count ) 
		public void exec(JBForth jbf) 
		{ 
			L2PcInstance player = (L2PcInstance)jbf.st.pop();
			if(player == null)
			{
				jbf.st.set(null);
				return;
			}

			Inventory inv = player.getInventory();

			if(inv == null)
			{
				jbf.st.set(0);
				return;
			}

			int count = 0;
			int item_id = jbf.st.pop_i();
			L2ItemInstance[] items = inv.getItems();

			synchronized(items)
			{
				for(L2ItemInstance item : items)
					if(item.getItemId() == item_id)
						count += item.getCount();
			}

			jbf.st.push(count);
		}
 	}

	public class adena_at extends Word
	{
//			  ( player -- adena )
		public void exec(JBForth jbf) 
		{ 
			L2PcInstance player = (L2PcInstance)jbf.st.pop();
			jbf.st.push(player.getAdena());
		}
	}

	public class adena_add extends Word
	{
//			  ( adena player -- )
		public void exec(JBForth jbf) 
		{
			L2PcInstance player = (L2PcInstance)jbf.st.pop();
			player.addAdena(jbf.st.pop_i());
			player.sendPacket(new ItemList(player, false));
		}
	}

	public class adena_sub extends Word
	{
//		( adena player -- )
		public void exec(JBForth jbf) 
		{ 
			L2PcInstance player = (L2PcInstance)jbf.st.pop();
			player.addAdena(-jbf.st.pop_i());
			player.sendPacket(new ItemList(player, false));
		}
	}

	public class item_spawn extends Word
	{
//		( item_id count droper x y z  -- item_object )
		public void exec(JBForth jbf) 
		{ 
			int z = jbf.st.pop_i();
			int y = jbf.st.pop_i();
			int x = jbf.st.pop_i();
			L2Character droper = (L2Character)jbf.st.pop();
			int count = jbf.st.pop_i();
			int item_id = jbf.st.pop_i();

			L2ItemInstance dropit = ItemTable.getInstance().createItem(item_id);
			dropit.setCount(count);

			dropit.dropMe(droper, x, y, z);

			jbf.st.push(dropit);
		}
	}

	public class item_unspawn extends Word
	{
//		( item_object -- )
		public void exec(JBForth jbf) 
		{ 
			Object item = jbf.st.pop();

			if(item != null && item instanceof L2Object)
				((L2Object)item).decayMe();
		}
	}
}
