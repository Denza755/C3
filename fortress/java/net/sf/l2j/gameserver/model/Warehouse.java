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
package net.sf.l2j.gameserver.model;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.l2j.Config;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.ItemTable;
import net.sf.l2j.gameserver.idfactory.IdFactory;
import net.sf.l2j.gameserver.model.L2ItemInstance.ItemClass;
import net.sf.l2j.gameserver.model.L2ItemInstance.ItemLocation;

/**
 * This class ...
 *
 * @version $Revision: 1.3.2.1.2.12 $ $Date: 2005/04/06 16:13:42 $
 */
public abstract class Warehouse
{
	protected static int _selecttype;

    private static final Logger _log = Logger.getLogger(Warehouse.class.getName());

    public abstract int getOwnerId();

    public abstract ItemLocation getLocationType();

    public abstract ItemClass getItemClass();

    public abstract String getLocationId();

    public abstract int getLocationId(boolean addItem);

    public abstract void setLocationId(L2PcInstance activeChar);

    public L2ItemInstance[] listItems()
    {
        ArrayList<L2ItemInstance> items = new ArrayList<L2ItemInstance>();
        java.sql.Connection con = null;
        try
        {
        	String _select = "";
        	switch (getItemClass()){
        		case EQUIPMENT:
        			_select = " AND prices.item_type IN ( 'armor', 'weapon', 'arrow' ) ";
        			break;
        		case CONSUMABLE:
        			_select = " AND ( prices.item_type IN ( 'potion' ) OR prices.name IN ( 'Cursed Bone', 'Crystal: D Grade', 'Crystal: C Grade', 'Crystal: B Grade', 'Crystal: A Grade', 'Crystal: S Grade' ) OR prices.name LIKE '%Soulshot:%' OR prices.name LIKE '%Spiritshot:%' OR prices.name LIKE '%Scroll of Escape%' OR prices.name LIKE '%Scroll of Resurrection%' OR prices.name LIKE '%Waking Scroll%' OR prices.name LIKE '%SP Scroll%' OR prices.name LIKE '%L2Day%' ) AND prices.item_type NOT LIKE 'recipe' ";
        			break;
        		case MATHERIALS:
        			_select = " AND ( prices.item_type IN ( 'material' ) OR prices.name IN ( 'Crystal: D Grade', 'Crystal: C Grade', 'Crystal: B Grade', 'Crystal: A Grade', 'Crystal: S Grade' ) ) AND prices.weight < '3' ";
        			break;
        		case PIECES:
        			_select = " AND prices.item_type IN ( 'material' ) AND prices.weight > '3' ";
        			break;
        		case SPELLBOOKS:
        			_select = " AND prices.item_type IN ( 'spellbook' ) ";
        			break;
        		case RECIPIES:
        			_select = " AND prices.item_type IN ( 'recipe' ) ";
        			break;
        		case DYES:
        			_select = " AND prices.item_type IN ( 'dye' ) ";
        			break;
        		case OTHER:
        			_select = " AND prices.item_type NOT IN ( 'material', 'recipe', 'spellbook', 'dye', 'armor', 'weapon', 'arrow', 'potion' ) AND prices.name NOT LIKE '%Soulshot:%' AND prices.name NOT LIKE '%Spiritshot:%' AND prices.name NOT LIKE '%Scroll of Escape%' AND prices.name NOT LIKE '%Scroll of Resurrection%' AND prices.name NOT LIKE '%Waking Scroll%' AND prices.name NOT LIKE '%SP Scroll%' AND prices.name NOT LIKE '%L2Day%' AND prices.name NOT IN ( 'Crystal: D Grade', 'Crystal: C Grade', 'Crystal: B Grade', 'Crystal: A Grade', 'Crystal: S Grade' ) ";
        			break;
        		case ALL:
        			_select = "";
        			break;
        		default:
        			_select = "";
        	}

            con = L2DatabaseFactory.getInstance().getConnection();
            PreparedStatement statement = con.prepareStatement("SELECT items.object_id FROM items LEFT JOIN prices ON ( prices.item_id = items.item_id ) WHERE items.owner_id=? AND items.loc=? AND items.loc_data IN ("
                            + getLocationId() + ")" + _select + " ORDER BY prices.name ASC");
            statement.setInt(1, getOwnerId());
            statement.setString(2, getLocationType().name());
            ResultSet inv = statement.executeQuery();

            L2ItemInstance item;
            while(inv.next())
            {
                int objectId = inv.getInt(1);
                item = L2ItemInstance.restoreFromDb(objectId);
                if(item == null) continue;
                items.add(item);
            }

            inv.close();
            statement.close();
        }
        catch(Exception e)
        {
            _log.log(Level.SEVERE, "could not restore warehouse:", e);
        }
        finally
        {
            try{con.close();}catch(Exception e){}
        }
        return items.toArray(new L2ItemInstance[items.size()]);
    }

    public synchronized L2ItemInstance addItem(L2ItemInstance newItem)
    {
        L2ItemInstance oldItem = null;
        // non-stackable items are simply added to DB
        if(!newItem.isStackable() || (oldItem = findItemId(newItem.getItemId())) == null)
        {
            newItem.setOwnerId(getOwnerId());
            newItem.setLocation(getLocationType(), getLocationId(true));
            newItem.setLastChange(L2ItemInstance.ADDED);
            newItem.updateDatabase();
            return newItem;
        }
        oldItem.setCount(oldItem.getCount() + newItem.getCount());
        oldItem.setLastChange(L2ItemInstance.MODIFIED);
        newItem.setCount(0);
        newItem.setOwnerId(0);
        newItem.setLocation(ItemLocation.VOID, getLocationId(true));
        oldItem.updateDatabase();
        newItem.updateDatabase();
        L2World.removeObject(newItem);
        IdFactory.getInstance().releaseId(newItem.getObjectId());
        return oldItem;
    }

    public synchronized L2ItemInstance destroyItemByObj(int objectId, int count)
    {
    	L2ItemInstance item = (L2ItemInstance)L2World.findObject(objectId);
    	if (item==null)
    		item = L2ItemInstance.restoreFromDb(objectId);
        
        if(item != null)
        {
            if(item.getCount() <= count)
            {
                item.setCount(0);
                item.setLocation(ItemLocation.VOID, getLocationId(true));
                item.setLastChange(L2ItemInstance.REMOVED);
                item.updateDatabase();
            }
            else
            {
                item.setCount(item.getCount() - count);
                item.setLastChange(L2ItemInstance.MODIFIED);
                item.updateDatabase();
            }
        }
        else
        {
            if(Config.DEBUG)
                _log.fine("can't destroy " + count + " item(s) obj_id:" + objectId);
        }
        return item;
    }

    public synchronized L2ItemInstance getItemByObj(int objectId, int count)
    {
        L2ItemInstance item = (L2ItemInstance)L2World.findObject(objectId);
    	if (item==null)
    		item = L2ItemInstance.restoreFromDb(objectId);
       
        if(item != null)
        {
            if(item.getCount() <= count)
            {
                item.setLastChange(L2ItemInstance.MODIFIED);
                item.setLocation(ItemLocation.VOID, getLocationId(true));
                item.updateDatabase();
                return item;
            }
            else
            {
                item.setCount(item.getCount() - count);
                item.setLastChange(L2ItemInstance.MODIFIED);
                item.updateDatabase();
                L2ItemInstance Newitem = ItemTable.getInstance().createItem(item.getItem().getItemId());
                Newitem.setCount(count);
                return Newitem;
            }
        }
        else
        {
            if(Config.DEBUG)
                _log.fine("can't destroy " + count + " item(s) obj_id:" + objectId);
        }
        return null;
    }
    
    public synchronized L2ItemInstance destroyItem(int itemId, int count)
    {
        L2ItemInstance item = findItemId(itemId);
        if(item != null)
        {
            if (item.getCount() < count)
            	return null;
        	
            if (item.getCount() == count)
            {
                item.setCount(0);
                item.setLocation(ItemLocation.VOID, getLocationId(true));
                item.setLastChange(L2ItemInstance.REMOVED);
                item.updateDatabase();
            }
            else
            {
                item.setCount(item.getCount() - count);
                item.setLastChange(L2ItemInstance.MODIFIED);
                item.updateDatabase();
            }
        }
        else
        {
            if(Config.DEBUG) _log.fine("can't destroy " + count + " item(s) " + itemId);
        }
        return item;
    }

    public L2ItemInstance findItemId(int itemId)
    {
        L2ItemInstance foundItem = null;
        java.sql.Connection con = null;
        try
        {
            con = L2DatabaseFactory.getInstance().getConnection();
            PreparedStatement statement = con.prepareStatement("SELECT object_id FROM items "
                            + "WHERE owner_id=? AND loc=? AND loc_data IN ("
                            + getLocationId() + ") AND item_id=? ");
            statement.setInt(1, getOwnerId());
            statement.setString(2, getLocationType().name());
            statement.setInt(3, itemId);
            ResultSet inv = statement.executeQuery();

            if(inv.next())
            {
                int objectId = inv.getInt(1);
                foundItem = L2ItemInstance.restoreFromDb(objectId);
            }

            inv.close();
            statement.close();
        }
        catch(Exception e)
        {
            _log.log(Level.WARNING, "could not list warehouse:", e);
        }
        finally
        {
            try{con.close();}catch(Exception e){}
        }
        return foundItem;
    }

}
