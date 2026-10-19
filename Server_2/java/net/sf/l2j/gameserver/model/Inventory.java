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
package net.sf.l2j.gameserver.model;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.l2j.Config;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.ItemTable;
import net.sf.l2j.gameserver.model.L2ItemInstance.ItemLocation;
import net.sf.l2j.gameserver.templates.L2EtcItem;
import net.sf.l2j.gameserver.templates.L2EtcItemType;
import net.sf.l2j.gameserver.templates.L2Item;
import net.sf.l2j.gameserver.templates.L2WeaponType;

/**
 * This class ...
 * 
 * @version $Revision: 1.13.2.9.2.12 $ $Date: 2005/03/29 23:15:15 $
 */
public abstract class Inventory
{
	protected static final Logger _log = Logger.getLogger(Inventory.class.getName());

    public interface PaperdollListener {
    	public void notifyEquiped(int slot, L2ItemInstance inst);
    	public void notifyUnequiped(int slot, L2ItemInstance inst);
    }
    
	public static final int PAPERDOLL_UNDER = 0;
	public static final int PAPERDOLL_LEAR = 1;
	public static final int PAPERDOLL_REAR = 2;
	public static final int PAPERDOLL_NECK = 3;
	public static final int PAPERDOLL_LFINGER = 4;
	public static final int PAPERDOLL_RFINGER = 5;
	public static final int PAPERDOLL_HEAD = 6;
	public static final int PAPERDOLL_RHAND = 7;
	public static final int PAPERDOLL_LHAND = 8;
	public static final int PAPERDOLL_GLOVES = 9;
	public static final int PAPERDOLL_CHEST = 10;
	public static final int PAPERDOLL_LEGS = 11;
	public static final int PAPERDOLL_FEET = 12;
	public static final int PAPERDOLL_BACK = 13;
	public static final int PAPERDOLL_LRHAND = 14;
	public static final int PAPERDOLL_HAIR = 15;
    
    //Speed percentage mods
    public static final double MAX_ARMOR_WEIGHT = 12000;

	private final L2ItemInstance[] _paperdoll;
	private final ArrayList<PaperdollListener> _paperdollListeners;

	// protected to be accessed from child classes only
	protected final ArrayList<L2ItemInstance> _items;
	private int _totalWeight;
	
	// used to quickly check for using of items of special type
	private int _wearedMask;
	
    public static final class ChangeRecorder implements PaperdollListener
    {
    	private final Inventory _inventory;
    	private final ArrayList<L2ItemInstance> _changed;
    	ChangeRecorder(Inventory inventory) {
    		_inventory = inventory;
			_changed = new ArrayList<L2ItemInstance>();
			_inventory.addPaperdollListener(this);
    	}
    	public void notifyEquiped(int slot, L2ItemInstance item) {
    		if (!_changed.contains(item))
    			_changed.add(item);
    	}
    	
    	public void notifyUnequiped(int slot, L2ItemInstance item) {
    		if (!_changed.contains(item))
    			_changed.add(item);
    	}
    	public L2ItemInstance[] getChangedItems() {
    		return _changed.toArray(new L2ItemInstance[_changed.size()]);
    	}
    }

    final class BowListener implements PaperdollListener
    {
    	public void notifyUnequiped(int slot, L2ItemInstance item)
    	{
    		if (slot != PAPERDOLL_LRHAND)
    			return;
    		if (Config.ASSERT) assert null == getPaperdollItem(PAPERDOLL_LRHAND); 
    		if (item.getItemType() == L2WeaponType.BOW)
    		{
    			L2ItemInstance arrow = getPaperdollItem(PAPERDOLL_LHAND);
    			if (arrow != null)
    				setPaperdollItem(PAPERDOLL_LHAND, null);
    		}
    	}
    	public void notifyEquiped(int slot, L2ItemInstance item)
    	{
    		if (slot != PAPERDOLL_LRHAND)
    			return;
    		if (Config.ASSERT) assert item == getPaperdollItem(PAPERDOLL_LRHAND); 
    		if (item.getItemType() == L2WeaponType.BOW)
    		{
    			L2ItemInstance arrow = findArrowForBow(item.getItem());
    			if (arrow != null)
    				setPaperdollItem(PAPERDOLL_LHAND, arrow);
    		}
    	}
    }
	
    final class StatsListener implements PaperdollListener
    {
    	public void notifyUnequiped(int slot, L2ItemInstance item)
    	{
    		if (slot == PAPERDOLL_LRHAND)
    			return;
    		getOwner().removeStatsOwner(item);
    	}
    	public void notifyEquiped(int slot, L2ItemInstance item)
    	{
    		if (slot == PAPERDOLL_LRHAND)
    			return;
    		getOwner().addStatFuncs(item.getStatFuncs(getOwner()));
    	}
    }
	
	protected Inventory()
	{
		_paperdoll = new L2ItemInstance[0x10];
		_items = new ArrayList<L2ItemInstance>();
		_paperdollListeners = new ArrayList<PaperdollListener>();
		addPaperdollListener(new BowListener());
		addPaperdollListener(new StatsListener());
	}
	
	protected abstract L2Character getOwner();
	protected abstract ItemLocation getBaseLocation();
	protected abstract ItemLocation getEquipLocation();
	
	public int getOwnerId()
	{
		return getOwner() == null ? 0 : getOwner().getObjectId();
	}
	
	public ChangeRecorder newRecorder()
	{
		return new ChangeRecorder(this);
	}
	
	public int getSize()
	{
		return _items.size();
	}
	
	public L2ItemInstance[] getItems()
	{
		return _items.toArray(new L2ItemInstance[_items.size()]);
	}

    public L2ItemInstance addItem(L2ItemInstance newItem)
    {
        return addItem(newItem, true);
    }

    protected L2ItemInstance addItem(L2ItemInstance newItem, boolean dbUpdate)
    {
		L2ItemInstance result = newItem;
		boolean stackableFound = false;
		
		if (newItem.isStackable())
		{
			int itemId = newItem.getItemId();
			L2ItemInstance old = findItemByItemId(itemId);
			if (old != null)
			{
				old.setCount(old.getCount()+newItem.getCount()); // add new item to existing stack
				newItem.setCount(0);
				newItem.setOwnerId(0);
				newItem.setLocation(ItemLocation.VOID);
				stackableFound = true;
				old.setLastChange(L2ItemInstance.MODIFIED);
				if (dbUpdate) {
					old.updateDatabase();
					newItem.updateDatabase();
				}
				result = old;
			}
		}
		
		if (!stackableFound)
		{	
			_items.add(newItem);
			if (newItem.getOwnerId() != getOwnerId() || dbUpdate) {
				newItem.setOwnerId(getOwnerId());
				newItem.setLocation(getBaseLocation());
				newItem.setLastChange((L2ItemInstance.ADDED));
			}
			if (dbUpdate) {
				newItem.updateDatabase();
			}
		}
		
		refreshWeight();
		return result;
	}
	

	public L2ItemInstance findItemByItemId(int itemId)
	{
		for (L2ItemInstance temp : _items)
		{
			if (temp.getItemId() == itemId)
				return temp;
		}

		return null;
	}
	
	/**
	 * @return
	 */
	public L2ItemInstance getPaperdollItem(int slot)
	{
		return _paperdoll[slot];
	}

	public int getPaperdollItemId(int slot)
	{
		L2ItemInstance item = _paperdoll[slot]; 
		if (item != null)
			return item.getItemId();
		return 0;
	}

	public int getPaperdollObjectId(int slot)
	{
		L2ItemInstance item = _paperdoll[slot]; 
		if (item != null)
			return item.getObjectId();
		return 0;
	}

	
	/** Adds new inventory's paperdoll listener */
	public synchronized void addPaperdollListener(PaperdollListener listener)
	{
		if (Config.ASSERT) assert !_paperdollListeners.contains(listener);
		_paperdollListeners.add(listener);
	}
	
	/** Removes a paperdoll listener */
	public synchronized void removePaperdollListener(PaperdollListener listener)
	{
		_paperdollListeners.remove(listener);
	}
	
	/**
	 * equips an item in the given slot. the item has to be already in the inventory
	 * @param underwear
	 */
	L2ItemInstance setPaperdollItem(int slot, L2ItemInstance item)
	{
		L2ItemInstance old = _paperdoll[slot]; 
		if (old != item)
		{
			if (old != null)
			{
				_paperdoll[slot] = null;
				old.setLocation(getBaseLocation());
				old.setLastChange(L2ItemInstance.MODIFIED);
				int mask = 0;
				for (int i=0; i < PAPERDOLL_LRHAND; i++)
				{
					L2ItemInstance pi = _paperdoll[i]; 
					if (pi != null)
						mask |= pi.getItem().getItemMask(); 
				}
				_wearedMask = mask;
				for (PaperdollListener listener : _paperdollListeners)
					listener.notifyUnequiped(slot, old);
				old.updateDatabase();
			}
			if (item != null)
			{
				_paperdoll[slot] = item;
				item.setLocation(getEquipLocation(), slot);
				item.setLastChange(L2ItemInstance.MODIFIED);
				_wearedMask |= item.getItem().getItemMask();
				for (PaperdollListener listener : _paperdollListeners)
					listener.notifyEquiped(slot, item);
				item.updateDatabase();
			}
		}
		return old;
	}
	
	public int getWearedMask()
	{
		return _wearedMask;
	}

    public L2ItemInstance[] unEquipItemInBodySlotAndRecord(int slot)
    {
		Inventory.ChangeRecorder recorder = newRecorder(); 
		try
		{
			unEquipItemInBodySlot(slot); 
		} finally {
			removePaperdollListener(recorder);
		}
		return recorder.getChangedItems();
    }
    
	public L2ItemInstance unEquipItemInSlot(int pdollSlot) {
		return setPaperdollItem(pdollSlot, null);
	}
	
    public L2ItemInstance[] unEquipItemInSlotAndRecord(int slot)
    {
		Inventory.ChangeRecorder recorder = newRecorder(); 
		try
		{
			unEquipItemInSlot(slot); 
		} finally {
			removePaperdollListener(recorder);
		}
		return recorder.getChangedItems();
    }
    
	/**
	 * @param slot
	 */
	public void unEquipItemInBodySlot(int slot)
	{
		if (Config.DEBUG) _log.fine("--- unequip body slot:" + slot);
		int pdollSlot = -1;
		
		switch (slot)
		{
		case L2Item.SLOT_L_EAR:		pdollSlot = PAPERDOLL_LEAR;	break;
		case L2Item.SLOT_R_EAR:		pdollSlot = PAPERDOLL_REAR;	break;
		case L2Item.SLOT_NECK:		pdollSlot = PAPERDOLL_NECK;	break;
		case L2Item.SLOT_R_FINGER:	pdollSlot = PAPERDOLL_RFINGER;	break;
		case L2Item.SLOT_L_FINGER:	pdollSlot = PAPERDOLL_LFINGER;	break;
		case L2Item.SLOT_HAIR:		pdollSlot = PAPERDOLL_HAIR;	break;
		case L2Item.SLOT_HEAD:		pdollSlot = PAPERDOLL_HEAD;	break;
		case L2Item.SLOT_R_HAND:	pdollSlot = PAPERDOLL_RHAND; break;
		case L2Item.SLOT_L_HAND:	pdollSlot = PAPERDOLL_LHAND; break;
		case L2Item.SLOT_GLOVES:	pdollSlot = PAPERDOLL_GLOVES; break;
		case L2Item.SLOT_CHEST:		// fall through
		case L2Item.SLOT_FULL_ARMOR:pdollSlot = PAPERDOLL_CHEST; break;
		case L2Item.SLOT_LEGS:		pdollSlot = PAPERDOLL_LEGS;	break;
		case L2Item.SLOT_BACK:		pdollSlot = PAPERDOLL_BACK;	break;
		case L2Item.SLOT_FEET:		pdollSlot = PAPERDOLL_FEET;	break;
		case L2Item.SLOT_UNDERWEAR:	pdollSlot = PAPERDOLL_UNDER;break;
		case L2Item.SLOT_LR_HAND:
			setPaperdollItem(PAPERDOLL_LHAND, null);
			setPaperdollItem(PAPERDOLL_RHAND, null);// this should be the same as in LRHAND
			pdollSlot = PAPERDOLL_LRHAND;
			break;
		}
		if (pdollSlot >= 0)
			setPaperdollItem(pdollSlot, null);
	}

	
    public L2ItemInstance[] equipItemAndRecord(L2ItemInstance item)
    {
		Inventory.ChangeRecorder recorder = newRecorder(); 
		try
		{
	       equipItem(item, true); 
		} finally {
			removePaperdollListener(recorder);
		}
		return recorder.getChangedItems();
    }
    
    public void equipItem(L2ItemInstance item)
    {
       equipItem(item, true); 
    }
    
	/**
	 * 
	 * @param item
	 * @return  arraylist of all items that were updated
	 */
	public void equipItem(L2ItemInstance item, boolean dbUpdate)
	{
		int targetSlot = item.getItem().getBodyPart();
		switch (targetSlot)
		{
			case L2Item.SLOT_LR_HAND:
			{
				if (setPaperdollItem(PAPERDOLL_LHAND, null) != null)
				{
					// exchange 2h for 2h
					setPaperdollItem(PAPERDOLL_RHAND, null);
					setPaperdollItem(PAPERDOLL_LHAND, null);
				}
				else
				{
					setPaperdollItem(PAPERDOLL_RHAND, null);
				}
				
				setPaperdollItem(PAPERDOLL_RHAND, item);
				setPaperdollItem(PAPERDOLL_LRHAND, item);
				break;
			}
			case L2Item.SLOT_L_HAND:
			{
				if (!(item.getItem() instanceof L2EtcItem) || item.getItem().getItemType() != L2EtcItemType.ARROW)
				{
					L2ItemInstance old1 = setPaperdollItem(PAPERDOLL_LRHAND, null);
					if (old1 != null)
					{
						setPaperdollItem(PAPERDOLL_RHAND, null);
					}
				}
				setPaperdollItem(PAPERDOLL_LHAND, null);
				setPaperdollItem(PAPERDOLL_LHAND, item);
					break;
			}
			case L2Item.SLOT_R_HAND:
			{
				if (_paperdoll[PAPERDOLL_LRHAND] != null)
				{
					setPaperdollItem(PAPERDOLL_LRHAND, null);
					setPaperdollItem(PAPERDOLL_LHAND, null);
					setPaperdollItem(PAPERDOLL_RHAND, null);
				}
				else
				{
					setPaperdollItem(PAPERDOLL_RHAND, null);
				}
				
				setPaperdollItem(PAPERDOLL_RHAND, item);
				break;
			}
			case L2Item.SLOT_L_EAR:
			case L2Item.SLOT_R_EAR:
			case L2Item.SLOT_L_EAR | L2Item.SLOT_R_EAR:
			{
				if (_paperdoll[PAPERDOLL_LEAR] == null)
				{
					setPaperdollItem(PAPERDOLL_LEAR, item);
				}
				else if (_paperdoll[PAPERDOLL_REAR] == null)
				{
					setPaperdollItem(PAPERDOLL_REAR, item);
				}
				else
				{
					setPaperdollItem(PAPERDOLL_LEAR, null);
					setPaperdollItem(PAPERDOLL_LEAR, item);
				}
					
				break;
			}
			case L2Item.SLOT_L_FINGER:
			case L2Item.SLOT_R_FINGER:
			case L2Item.SLOT_L_FINGER | L2Item.SLOT_R_FINGER:
			{
				if (_paperdoll[PAPERDOLL_LFINGER] == null)
				{
					setPaperdollItem(PAPERDOLL_LFINGER, item);
				}
				else if (_paperdoll[PAPERDOLL_RFINGER] == null)
				{
					setPaperdollItem(PAPERDOLL_RFINGER, item);
				}
				else
				{
					setPaperdollItem(PAPERDOLL_LFINGER, null);
					setPaperdollItem(PAPERDOLL_LFINGER, item);
				}
				break;
			}
			case L2Item.SLOT_NECK:
				setPaperdollItem(PAPERDOLL_NECK, item);
				break;
			case L2Item.SLOT_FULL_ARMOR:
				setPaperdollItem(PAPERDOLL_CHEST, null);
				setPaperdollItem(PAPERDOLL_LEGS, null);
				setPaperdollItem(PAPERDOLL_CHEST, item);
				break;
			case L2Item.SLOT_CHEST:
				setPaperdollItem(PAPERDOLL_CHEST, item);
				break;
			case L2Item.SLOT_LEGS:
			{
				// handle full armor
				L2ItemInstance chest = getPaperdollItem(PAPERDOLL_CHEST);
				if (chest != null && chest.getItem().getBodyPart() == L2Item.SLOT_FULL_ARMOR)
				{
					setPaperdollItem(PAPERDOLL_CHEST, null);
				}
				
				setPaperdollItem(PAPERDOLL_LEGS, null);
				setPaperdollItem(PAPERDOLL_LEGS, item);
				break;
			}
			case L2Item.SLOT_FEET:
				setPaperdollItem(PAPERDOLL_FEET, item);
				break;
			case L2Item.SLOT_GLOVES:
				setPaperdollItem(PAPERDOLL_GLOVES, item);
				break;
			case L2Item.SLOT_HEAD:
				setPaperdollItem(PAPERDOLL_HEAD, item);
				break;
			case L2Item.SLOT_HAIR:
				setPaperdollItem(PAPERDOLL_HAIR, item);
				break;
			case L2Item.SLOT_UNDERWEAR:
				setPaperdollItem(PAPERDOLL_UNDER, item);
				break;
			case L2Item.SLOT_BACK:
				setPaperdollItem(PAPERDOLL_BACK, item);
				break;
			default:
				_log.warning("unknown body slot:" + targetSlot);
		}
	}
    
	/**
	 * @param itemId
	 * @return
	 */
	public L2ItemInstance getItemByItemId(int itemId)
	{
		for (L2ItemInstance temp : _items)
		{
			if (temp.getItemId() == itemId)
				return temp;
		}
		return null;
	}

	/**
	 * @param objectId
	 * @return
	 */
	public L2ItemInstance getItemByObjectId(int objectId)
	{
		for (L2ItemInstance temp : _items)
		{
			if (temp.getObjectId() == objectId)
				return temp;
		}
		return null;
	}

	/**
	 * @param objectId
	 */
	public L2ItemInstance destroyItem(int objectId, int count)
	{
		L2ItemInstance item = getItemByObjectId(objectId);
		if (item == null) return null;
		if (item.getCount() <= count)
		{	
			_items.remove(item);
			item.setCount(0);
			item.setOwnerId(0);
			item.setLocation(ItemLocation.VOID);
			item.setLastChange(L2ItemInstance.REMOVED);
			item.updateDatabase();
		}
		else
		{
			item.setCount(item.getCount()-count);
			item.setLastChange(L2ItemInstance.MODIFIED);
			item.updateDatabase();
		}
		refreshWeight();
		return item;
	}

	// we need this one cuz warehouses send itemId only
	/**
	 * @param objectId
	 */
	public L2ItemInstance destroyItemByItemId(int itemId, int count)
	{
		L2ItemInstance item = findItemByItemId(itemId);
        if (item == null) return null;
        if (item.getCount() < count)
        	count = item.getCount();
		if (item.getCount() == count)
		{	
			_items.remove(item);
			item.setCount(0);
			item.setOwnerId(0);
			item.setLocation(ItemLocation.VOID);
			item.setLastChange(L2ItemInstance.REMOVED);
			item.updateDatabase();
		}
		else
		{
			item.setCount(item.getCount()-count);
			item.setLastChange(L2ItemInstance.MODIFIED);
			item.updateDatabase();
		}
		refreshWeight();
		return item;
	}

	/**
	 * @param objectId
	 */
	public L2ItemInstance dropItem(int objectId, int count)
	{
		L2ItemInstance oldItem = getItemByObjectId(objectId);
		return dropItem(oldItem, count);
	}

	
	
	/**
	 * @param oldItem
	 * @param count
	 * @return
	 */
	public L2ItemInstance dropItem(L2ItemInstance oldItem, int count)
	{
		if (oldItem == null)
		{
			_log.warning("DropItem: item id does not exist in inventory");
			return null;
		}
		
		if (Config.ASSERT) assert oldItem.getOwnerId() == getOwnerId();
		
		if (oldItem.isEquipped())
			setPaperdollItem(oldItem.getEquipSlot(), null);
			
		if (oldItem.getCount() <= count || oldItem.getCount() < 1)
		{	
			if (Config.DEBUG) _log.fine(" count = count  --> remove");
			_items.remove(oldItem);
			oldItem.setOwnerId(0);
			oldItem.setLocation(ItemLocation.VOID);
			oldItem.setLastChange(L2ItemInstance.REMOVED);
			oldItem.updateDatabase();
			refreshWeight();
			return oldItem;
		}
		else
		{
			if (Config.DEBUG) _log.fine(" count != count  --> reduce");
			oldItem.setCount(oldItem.getCount()-count);
			oldItem.setLastChange(L2ItemInstance.MODIFIED);
			L2ItemInstance newItem = ItemTable.getInstance().createItem(oldItem.getItemId());
			newItem.setCount(count);
			oldItem.updateDatabase();
			refreshWeight();
			return newItem;
		}
	}

	private void refreshWeight()
	{
		int weight = 0;
		
		for (L2ItemInstance element : _items)
		{
			weight += element.getItem().getWeight() * element.getCount();
		}
		
		_totalWeight = weight;
		// notify char for overload checking
		if (getOwner() instanceof L2PcInstance)
			((L2PcInstance) getOwner()).refreshOverloaded();
	}
	
	/**
	 * @return Returns the totalWeight.
	 */
	public int getTotalWeight()
	{
		return _totalWeight;
	}
	
	public L2ItemInstance findArrowForBow(L2Item bow)
	{
		int arrowsId = 0;
		
		switch (bow.getCrystalType())
		{
		default: // broken weapon.csv ??
		case L2Item.CRYSTAL_NONE:   arrowsId = 17;   break;
		case L2Item.CRYSTAL_D:      arrowsId = 1341; break;
		case L2Item.CRYSTAL_C:      arrowsId = 1342; break;
		case L2Item.CRYSTAL_B:      arrowsId = 1343; break;
		case L2Item.CRYSTAL_A:      arrowsId = 1344; break;
		case L2Item.CRYSTAL_S:      arrowsId = 1345; break;
		}

		 return findItemByItemId(arrowsId);
	}
	
	public void deleteMe()
	{
		ArrayList<L2ItemInstance> items = new ArrayList<L2ItemInstance>(_items);
		_items.clear();
		try { updateDatabase(items); } catch (Throwable t) {_log.log(Level.SEVERE, "deletedMe()", t); }
		L2World world = L2World.getInstance();
		for (L2ItemInstance inst : items)
			try { world.removeObject(inst); } catch (Throwable t) {_log.log(Level.SEVERE, "deletedMe()", t); }
	}
	
	public void updateDatabase()
	{
		updateDatabase(_items);
	}
	private void updateDatabase(ArrayList<L2ItemInstance> items)
	{
		if (getOwner() != null)
		{
			for (L2ItemInstance inst : items)
			{
				inst.updateDatabase();
			}
		}
	}

    public void restore()
    {
        java.sql.Connection con = null;
        try
        {
            con = L2DatabaseFactory.getInstance().getConnection();
            PreparedStatement statement = con.prepareStatement(
					"SELECT object_id FROM items WHERE owner_id=? AND (loc=? OR loc=?) " +
					"ORDER BY object_id DESC");
            statement.setInt(1, getOwner().getObjectId());
            statement.setString(2, getBaseLocation().name());
            statement.setString(3, getEquipLocation().name());
            ResultSet inv = statement.executeQuery();
            
            L2ItemInstance item;
            while (inv.next())
            {
				int objectId = inv.getInt(1);
				item = L2ItemInstance.restoreFromDb(objectId);
				if (item == null)
					continue;
                addItem(item, false);
                if (item.isEquipped())
                    equipItem(item, false);
                // add this item to the world
                L2World.getInstance().storeObject(item);
            }
            
            inv.close();
            statement.close();
            refreshWeight();
        }
        catch (Exception e)
        {
			_log.log(Level.WARNING, "could not restore inventory:", e);
        } 
        finally 
        {
            try { con.close(); } catch (Exception e) {}
        }
    }
    
//	private int calculateEnchantBonus(L2ItemInstance item)
//	{
//		int bonus = 0;
//		if (item.getItem().getType2() == L2Item.TYPE2_WEAPON)
//		{
//			switch (item.getItem().getCrystalType())
//			{
//				case L2Item.CRYSTAL_A:
//					bonus = item.getEnchantLevel() < 4 ? 4*item.getEnchantLevel() : 4*3+8*item.getEnchantLevel();
//					break;
//				case L2Item.CRYSTAL_B:
//					bonus = item.getEnchantLevel() < 4 ? 4*item.getEnchantLevel() : 3*3+6*item.getEnchantLevel();
//					break;
//				case L2Item.CRYSTAL_C:
//					bonus = item.getEnchantLevel() < 4 ? 4*item.getEnchantLevel() : 3*3+6*item.getEnchantLevel();
//					break;
//				case L2Item.CRYSTAL_D:
//					bonus = item.getEnchantLevel() < 4 ? 4*item.getEnchantLevel() : 2*3+4*item.getEnchantLevel();
//					break;
//				case L2Item.CRYSTAL_S:
//					bonus = item.getEnchantLevel() < 4 ? 4*item.getEnchantLevel() : 5*3+10*item.getEnchantLevel();
//					break;
//			}
//		}
//		else
//		{
//			bonus = item.getEnchantLevel() < 4 ? 1*item.getEnchantLevel() : 1*3+3*item.getEnchantLevel();
//		}
//		return bonus;
//	}
}
