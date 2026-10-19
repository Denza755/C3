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
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.l2j.Config;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.ItemTable;
import net.sf.l2j.gameserver.ai.CtrlIntention;
import net.sf.l2j.gameserver.skills.Func;
import net.sf.l2j.gameserver.templates.L2Armor;
import net.sf.l2j.gameserver.templates.L2EtcItem;
import net.sf.l2j.gameserver.templates.L2Item;

/**
 * This class ...
 * 
 * @version $Revision: 1.4.2.1.2.11 $ $Date: 2005/03/31 16:07:50 $
 */
public final class L2ItemInstance extends L2Object
{
	private static final Logger _log = Logger.getLogger(L2ItemInstance.class.getName());
	
	public static enum ItemLocation {
		VOID,
		INVENTORY,
		PAPERDOLL,
		WAREHOUSE,
		CLANWH,
		PET,
		PET_EQUIP,
		LEASE
	}
	
	private int             _owner_id;
	private int             _count;
	private final int       _itemId;
	private final L2Item	_item;
	private ItemLocation    _loc;
	private int             _loc_data;
	private int             _enchantLevel;
	private int             _price_sell;
	private int             _price_buy;
	
	
	public static final int CHARGED_NONE				=	0;
	public static final int CHARGED_SOULSHOT				=	1;
	public static final int CHARGED_SPIRITSHOT			=	1;
	public static final int CHARGED_BLESSED_SOULSHOT		=	2; // It's a realy exists? ;-)
	public static final int CHARGED_BLESSED_SPIRITSHOT		=	2; 
	
	
	private int				_chargedSoulshot			=	CHARGED_NONE; 
	private int				_chargedSpiritshot		=	CHARGED_NONE;  
	
	public static final int UNCHANGED = 0;
	public static final int ADDED = 1;
	public static final int REMOVED = 3;
	public static final int MODIFIED = 2;
	private int _lastChange = 2;	//1 ??, 2 modified, 3 removed
	private boolean _existsInDb; // if a record exists in DB.
	private boolean _storedInDb; // if DB data is up-to-date.
	
	
	public L2ItemInstance(int objectId, int itemId)
	{
		super(objectId);
		_itemId = itemId;
		_item = ItemTable.getInstance().getTemplate(itemId);
		if (_itemId == 0 || _item == null)
			throw new IllegalArgumentException();
		_count = 1;
		_loc = ItemLocation.VOID;
	}
	
	public L2ItemInstance(int objectId, L2Item item)
	{
		super(objectId);
		_itemId = item.getItemId();
		_item = item;
		if (_itemId == 0 || _item == null)
			throw new IllegalArgumentException();
		_count = 1;
		_loc = ItemLocation.VOID;
	}
	
	public void setOwnerId(int owner_id)
	{
		if (owner_id == _owner_id)
			return;
		_owner_id = owner_id;
		_storedInDb = false;
	}

	public int getOwnerId()
	{
		return _owner_id;
	}

	public void setLocation(ItemLocation loc)
	{
		setLocation(loc, 0);
	}

	public void setLocation(ItemLocation loc, int loc_data)
	{
		if (loc == _loc && loc_data == _loc_data)
			return;
		_loc = loc;
		_loc_data = loc_data;
		_storedInDb = false;
	}

	public int getCount()
	{
		return _count;
	}

	public void setCount(int count)
	{
		if (_count == count)
			return;
		_count = count;
		_storedInDb = false;
	}

	public boolean isEquipable()
	{
		
		return !(_item.getBodyPart() == 0 || _item instanceof L2EtcItem );
	}

	/**
	 * 
	 * @return
	 */
	public boolean isEquipped()
	{
		return _loc == ItemLocation.PAPERDOLL || _loc == ItemLocation.PET_EQUIP;
	}

	public int getEquipSlot()
	{
		if (Config.ASSERT) assert _loc == ItemLocation.PAPERDOLL || _loc == ItemLocation.PET_EQUIP;
		return _loc_data;
	}
	
	/**
	 * @return
	 */
	public L2Item getItem()
	{
		return _item;
	}

	/**
	 * @return
	 */
	public Enum getItemType()
	{
		return _item.getItemType();
	}

	/**
	 * @return
	 */
	public int getItemId()
	{
		return _itemId;
	}

	/**
	 * @return Returns the price.
	 */
	public int getReferencePrice()
	{
		return _item.getReferencePrice();
	}

	public int getPriceToSell()
	{
		return _price_sell;
	}
	public void setPriceToSell(int price)
	{
		_price_sell = price;
		_storedInDb = false;
	}
	public int getPriceToBuy()
	{
		return _price_buy;
	}
	public void setPriceToBuy(int price)
	{
		_price_buy = price;
		_storedInDb = false;
	}

	/**
	 * @return Returns the lastChange.
	 */
	public int getLastChange()
	{
		return _lastChange;
	}

	/**
	 * @param lastChange The lastChange to set.
	 */
	public void setLastChange(int lastChange)
	{
		_lastChange = lastChange;
	}

	public boolean isStackable()
	{
		return _item.isStackable();
	}
	
	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.model.L2Object#onAction(net.sf.l2j.gameserver.model.L2PcInstance)
	 */
	public void onAction(L2PcInstance player)
	{
		// this causes the validate position handler to do the pickup if the location is reached.
		player.getAI().setIntention(CtrlIntention.AI_INTENTION_PICK_UP, this);
	}
	/**
	 * @return Returns the enchantLevel.
	 */
	public int getEnchantLevel()
	{
		return _enchantLevel;
	}
	/**
	 * @param enchantLevel The enchantLevel to set.
	 */
	public void setEnchantLevel(int enchantLevel)
	{
		if (_enchantLevel == enchantLevel)
			return;
		_enchantLevel = enchantLevel;
		_storedInDb = false;
	}
	
	public int getPDef()
	{
		if (_item instanceof L2Armor)
			return ((L2Armor)_item).getPDef();
		return 0;
	}

    public boolean isAutoAttackable(L2Character attacker)
    {
        return false;
    }
    
	/**
	 * True is feapon charged with Sou
	 * @return
	 */
	
	public int getChargedSoulshot()
	{
		return 	_chargedSoulshot;	
	}
	
	public int getChargedSpiritshot()
	{
		return _chargedSpiritshot;		
	}
	
	
	
	/**
	 * sets if weapon is charged with ss or not
	 * @param isCharged
	 */
	public void setChargedSoulshot(int type) 
	{
		_chargedSoulshot = type;
	}
	
	public void setChargedSpiritshot(int type) 
	{
		_chargedSpiritshot = type;
	}

    /** This function basically returns a set of functions from
     * L2Item/L2Armor/L2Weapon, but may add additional
     * functions, if this particular item instance is enhanched
     * for a particular player.
     */
    public Func[] getStatFuncs(L2Character player)
    {
    	return getItem().getStatFuncs(this, player);
    }
	
	public void updateDatabase() {
		if (_existsInDb) {
			if (_owner_id == 0 || _loc==ItemLocation.VOID || (_count == 0 && _loc!=ItemLocation.LEASE))
				removeFromDb();
			else
				updateInDb();
		} else {
			if (_count == 0 && _loc!=ItemLocation.LEASE)
				return;
			if(_loc==ItemLocation.VOID || _owner_id == 0)
				return;
			insertIntoDb();
		}
	}
	
	
	public static L2ItemInstance restoreFromDb(int objectId) {
		L2ItemInstance inst = null;
		java.sql.Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(
					"SELECT * FROM items WHERE object_id = ?");
			statement.setInt(1, objectId);
			ResultSet rs = statement.executeQuery();
			if (rs.next()) {
				int owner_id = rs.getInt("owner_id");
				int item_id = rs.getInt("item_id");
				int count = rs.getInt("count");
				ItemLocation loc = ItemLocation.valueOf(rs.getString("loc"));
				int loc_data = rs.getInt("loc_data");
				int enchant_level = rs.getInt("enchant_level");
				int price_sell = rs.getInt("price_sell");
				int price_buy = rs.getInt("price_buy");
				L2Item item = ItemTable.getInstance().getTemplate(item_id);
				if (item == null) {
					_log.severe("Item item_id="+item_id+" not known, object_id="+objectId);
					return null;
				}
				inst = new L2ItemInstance(objectId, item);
				inst._existsInDb = true;
				inst._storedInDb = true;
				inst._owner_id = owner_id;
				inst._count = count;
				inst._enchantLevel = enchant_level;
				inst._loc = loc;
				inst._loc_data = loc_data;
				inst._price_sell = price_sell;
				inst._price_buy  = price_buy;
			} else {
				_log.severe("Item object_id="+objectId+" not found");
				return null;
			}
			rs.close();
            statement.close();
        } catch (Exception e) {
			_log.log(Level.SEVERE, "Could not restore item "+objectId+" from DB:", e);
		} finally {
			try { con.close(); } catch (Exception e) {}
		}
		return inst;
	}

	private void updateInDb() {
		if (Config.ASSERT) assert this._existsInDb;
		if (this._storedInDb)
			return;
		java.sql.Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(
					"UPDATE items SET owner_id=?,count=?,loc=?,loc_data=?,enchant_level=?,price_sell=?,price_buy=? " +
					"WHERE object_id = ?");
			statement.setInt(1, _owner_id);
			statement.setInt(2, getCount());
			statement.setString(3, _loc.name());
			statement.setInt(4, _loc_data);
			statement.setInt(5, getEnchantLevel());
			statement.setInt(6, _price_sell);
			statement.setInt(7, _price_buy);
			statement.setInt(8, getObjectId());
			statement.executeUpdate();
			_existsInDb = true;
			_storedInDb = true;
            statement.close();
        } catch (Exception e) {
			_log.log(Level.SEVERE, "Could not update item "+getObjectId()+" in DB:", e);
		} finally {
			try { con.close(); } catch (Exception e) {}
		}
	}

	private void insertIntoDb() {
		if (Config.ASSERT) assert !this._existsInDb && this.getObjectId() != 0;
		java.sql.Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(
					"INSERT INTO items (owner_id,item_id,count,loc,loc_data,enchant_level,price_sell,price_buy,object_id) " +
					"VALUES (?,?,?,?,?,?,?,?,?)");
			statement.setInt(1, _owner_id);
			statement.setInt(2, _itemId);
			statement.setInt(3, getCount());
			statement.setString(4, _loc.name());
			statement.setInt(5, _loc_data);
			statement.setInt(6, getEnchantLevel());
			statement.setInt(7, _price_sell);
			statement.setInt(8, _price_buy);
			statement.setInt(9, getObjectId());
			statement.executeUpdate();
			_existsInDb = true;
			_storedInDb = true;
            statement.close();
        } catch (Exception e) {
			_log.log(Level.SEVERE, "Could not insert item "+getObjectId()+" into DB:", e);
		} finally {
			try { con.close(); } catch (Exception e) {}
		}
	}


	private void removeFromDb() {
		if (Config.ASSERT) assert this._existsInDb;
		java.sql.Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(
					"DELETE FROM items WHERE object_id=?");
			statement.setInt(1, getObjectId());
			statement.executeUpdate();
			_existsInDb = false;
			_storedInDb = false;
            statement.close();
        } catch (Exception e) {
			_log.log(Level.SEVERE, "Could not delete item "+getObjectId()+" in DB:", e);
		} finally {
			try { con.close(); } catch (Exception e) {}
		}
	}
}
