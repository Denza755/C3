package net.sf.l2j.gameserver.model;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.logging.Level;
import java.util.List;

import net.sf.l2j.Config;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.ItemTable;
import net.sf.l2j.gameserver.idfactory.IdFactory;
import net.sf.l2j.gameserver.lib.Log;
import net.sf.l2j.gameserver.model.L2ItemInstance.ItemLocation;

public class PcInventory extends Inventory {

	private final L2PcInstance _owner;
	private L2ItemInstance _adena;

	public PcInventory(L2PcInstance owner) {
		this._owner = owner;
	}
	protected L2PcInstance getOwner() { return _owner; }
	protected ItemLocation getBaseLocation() { return ItemLocation.INVENTORY; } 
	protected ItemLocation getEquipLocation() { return ItemLocation.PAPERDOLL; }

    protected L2ItemInstance addItem(L2ItemInstance newItem, boolean dbUpdate)
    {
	if (newItem!=null)
			Log.add("Inventory|"+getOwner().getName()+"|Get item|"+newItem.getItemId()+"|"+newItem.getCount()+"|"+newItem.getObjectId(),"items");
    	if (newItem.getItemId() == 57)
		{
            if (_adena == null) {
                addAdena(newItem, dbUpdate);
            } else {
                addAdena(newItem.getCount(), dbUpdate);
				newItem.setCount(0);
				newItem.setOwnerId(0);
				newItem.setLocation(ItemLocation.VOID);
				IdFactory.getInstance().releaseId(newItem.getObjectId());
				L2World.removeObject(newItem);
				//if (dbUpdate)
					newItem.updateDatabase();
            }
			return _adena;
		}
		return super.addItem(newItem, dbUpdate);
    }

	public L2ItemInstance dropItem(L2ItemInstance oldItem, int count)
	{
		if (Config.ASSERT) assert oldItem.getOwnerId() == getOwnerId();
		if (oldItem!=null)
			Log.add("Inventory|"+getOwner().getName()+"|Remove item|"+oldItem.getItemId()+"|"+count+"|"+oldItem.getObjectId(),"items");

		if (oldItem != null && oldItem.getItemId() == 57)
		{
			reduceAdena(count);
			L2ItemInstance adena = ItemTable.getInstance().createItem(oldItem.getItemId());
			adena.setCount(count);
			return adena;
		}
		
		return super.dropItem(oldItem, count); 
	}
	
	public L2ItemInstance getItemByItemId(int itemId)
	{
		if (_adena != null && itemId == _adena.getItemId())
			return _adena;
		return super.getItemByItemId(itemId);
	}
	public L2ItemInstance getItemByObjectId(int objectId)
	{
		if (_adena != null && objectId == _adena.getObjectId())
			return _adena;
		return super.getItemByObjectId(objectId);
	}
	
	/**
	 * @return
	 */
	public L2ItemInstance getAdenaInstance()
	{
		return _adena;
	}
	
	
	public int getAdena()
	{
		if (_adena == null)
			return 0;
		return _adena.getCount();
	}

	
    private void setAdena(int adena, boolean dbUpdate)
    {
    	// to find out whether this happens again
    	if (adena < 0 )
    	{
    		_log.warning("setAdena("+adena+") for char "+_owner.getName());
    		adena = 0;
    	}
    	if (_adena != null && !_items.contains(_adena))
    	{
    		_log.warning("missing adena in _items for char "+_owner.getName());
    		_items.add(_adena);
    	}
    	//_log.info("setAdena("+adena+"), _adena="+((_adena==null)?"null":"not null"));
        if (adena == 0)
        {
            if ( _adena != null && _items.contains(_adena))
            {
                _items.remove(_adena);
		_adena.setOwnerId(0);
		_adena.setLocation(ItemLocation.VOID);
                IdFactory.getInstance().releaseId(_adena.getObjectId());
                L2World.removeObject(_adena);
                //if (dbUpdate)
		  _adena.updateDatabase();
                _adena = null;
            }
        }
        else 
        {
            if (_adena == null) {
                _adena = ItemTable.getInstance().createItem(57);
				_adena.setOwnerId(getOwnerId());
				_adena.setLocation(getBaseLocation());
	            _adena.setCount(adena);
                _items.add(_adena);
            } else {
	            _adena.setCount(adena);
            }
            if (dbUpdate)
				_adena.updateDatabase();
        }
    }

    private void addAdena(L2ItemInstance adena, boolean dbUpdate)
    {
        _adena = adena;
		_adena.setOwnerId(getOwnerId());
		_adena.setLocation(getBaseLocation());
        _items.add(_adena);
        if (dbUpdate)
			_adena.updateDatabase();
    }
    
    
    /**
     * @param adena
     */
    public void addAdena(int adena)
    {
        addAdena(adena, true);
    }

    private void addAdena(int adena, boolean dbUpdate)
    {
        setAdena(getAdena() + adena, dbUpdate);
    }

	public void reduceAdena(int adena)
	{
		setAdena(getAdena() - adena, true);
	}

    public static int[][] restoreVisibleInventory(int objectId)
    {
    	int[][] paperdoll = new int[0x10][3];
        java.sql.Connection con = null;
        try
        {
            con = L2DatabaseFactory.getInstance().getConnection();
            PreparedStatement statement2 = con.prepareStatement(
					"SELECT object_id,item_id,loc_data,enchant_level FROM items WHERE owner_id=? AND loc='PAPERDOLL'");
            statement2.setInt(1, objectId);
            ResultSet invdata = statement2.executeQuery();

            while (invdata.next())
            {
            	int slot = invdata.getInt("loc_data");
            	paperdoll[slot][0] = invdata.getInt("object_id");
            	paperdoll[slot][1] = invdata.getInt("item_id");
				paperdoll[slot][2] = invdata.getInt("enchant_level");
            }
        } catch (Exception e) {
			_log.log(Level.WARNING, "could not restore inventory:", e);
        } finally {
            try { con.close(); } catch (Exception e) {}
        }
        return paperdoll;
    }
    
    
    public boolean validateCapacity(L2ItemInstance item)
    {
        int slots = getSize();
        
        if (!(item.isStackable() && findItemByItemId(item.getItemId()) != null))
        {
            slots++;
        }
        
        return validateCapacity(slots);
    }
    
    public boolean validateCapacity(List<L2ItemInstance> items)
    {
        int slots = getSize();
        
        for (L2ItemInstance item : items)
        {
            if (!(item.isStackable() && findItemByItemId(item.getItemId()) != null))
            {
                slots++;
            }
        }
        
        return validateCapacity(slots);
    }
    
    public boolean validateCapacity(int slots)
    {
        if ( (_owner.isGM() && slots <= Config.INVENTORY_MAXIMUM_GM) || 
                (_owner.getRace() == Race.dwarf && slots <= Config.INVENTORY_MAXIMUM_DWARF) || 
                (_owner.getRace() != Race.dwarf && slots <= Config.INVENTORY_MAXIMUM_NO_DWARF) )
           {
               return true;
           }
           
           return false;
    }

    
}
