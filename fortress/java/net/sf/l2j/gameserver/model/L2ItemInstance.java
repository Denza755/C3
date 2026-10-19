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
import net.sf.l2j.gameserver.GameServer;
import net.sf.l2j.gameserver.ItemTable;
import net.sf.l2j.gameserver.ai.CtrlIntention;
import net.sf.l2j.gameserver.model.quest.QuestState;
import net.sf.l2j.gameserver.skills.Func;
import net.sf.l2j.gameserver.templates.L2Armor;
import net.sf.l2j.gameserver.templates.L2EtcItem;
import net.sf.l2j.gameserver.templates.L2Item;

/**
 * This class manages items.
 *
 * @version $Revision: 1.4.2.1.2.11 $ $Date: 2005/03/31 16:07:50 $
 */
public final class L2ItemInstance extends L2Object
{
	private static final Logger _log = Logger.getLogger(L2ItemInstance.class.getName());

	/** Enumeration of locations for item */
	public static enum ItemLocation {
		VOID,
		INVENTORY,
		PAPERDOLL,
		WAREHOUSE,
		CLANWH,
		PET,
		PET_EQUIP,
		LEASE,
        FREIGHT
	}

	/** Item types to select */
	public static enum ItemClass {
		/** List all deposited items */
    	ALL,
		/** Weapons, Armor, Jevels, Arrows*/
    	EQUIPMENT,
    	/** Soul/Spiritshot, Potions, Scrolls */
    	CONSUMABLE,
    	/** Common craft matherials */
    	MATHERIALS,
    	/** Special (item specific) craft matherials */
    	PIECES,
    	/** Crafting recipies */
    	RECIPIES,
    	/** Skill learn books */
    	SPELLBOOKS,
    	/** Dyes to make tattoo */
    	DYES,
    	/** All other */
    	OTHER
	}

	/** ID of the owner */
	private int _owner_id;

	/** Quantity of the item */
	private int _count;

	/** ID of the item */
	private final int _itemId;

	/** Object L2Item associated to the item */
	private final L2Item _item;

	/** Location of the item */
	private ItemLocation _loc;

	/** Type of the item */
	public ItemClass _class;

	/** Slot where item is stored */
	private int _loc_data;

	/** Level of enchantment of the item */
	private int _enchantLevel;

	/** Price of the item for selling */
	private int _price_sell;

	/** Price of the item for buying */
	private int _price_buy;

	/** Wear Item */
	private boolean _wear;

	/** Custom item types (used loto, race tickets) */
	private int _type1;
	private int _type2;

	private long _dropTime;

	public static final int CHARGED_NONE				=	0;
	public static final int CHARGED_SOULSHOT				=	1;
	public static final int CHARGED_SPIRITSHOT			=	1;
	public static final int CHARGED_BLESSED_SOULSHOT		=	2; // It's a realy exists? ;-)
	public static final int CHARGED_BLESSED_SPIRITSHOT		=	2;

	/** Item charged with SoulShot (type of SoulShot) */
	private int				_chargedSoulshot			=	CHARGED_NONE;
	/** Item charged with SpiritShot (type of SpiritShot) */
	private int				_chargedSpiritshot		=	CHARGED_NONE;

	public static final int UNCHANGED = 0;
	public static final int ADDED = 1;
	public static final int REMOVED = 3;
	public static final int MODIFIED = 2;
	private int _lastChange = 2;	//1 ??, 2 modified, 3 removed
	private boolean _existsInDb; // if a record exists in DB.
	private boolean _storedInDb; // if DB data is up-to-date.
    private ArrayList<QuestState> _NotifyQuestOfUseList = new ArrayList<QuestState>();

	/**
	 * Constructor of the L2ItemInstance from the objectId and the itemId.
	 * @param objectId : int designating the ID of the object in the world
	 * @param itemId : int designating the ID of the item
	 */
	public L2ItemInstance(int objectId, int itemId)
	{
		super(objectId);
		_itemId = itemId;
		_item = ItemTable.getInstance().getTemplate(itemId);
		if (_itemId == 0 || _item == null)
			throw new IllegalArgumentException();
		_count = 1;
		_loc = ItemLocation.VOID;
		_class = ItemClass.ALL;
		_type1 = 0;
		_type2 = 0;
		_dropTime = 0;
	}

	/**
	 * Constructor of the L2ItemInstance from the objetId and the description of the item given by the L2Item.
	 * @param objectId : int designating the ID of the object in the world
	 * @param item : L2Item containing informations of the item
	 */
	public L2ItemInstance(int objectId, L2Item item)
	{
		super(objectId);
		_itemId = item.getItemId();
		_item = item;
		if (_itemId == 0 || _item == null)
			throw new IllegalArgumentException();
		_count = 1;
		_loc = ItemLocation.VOID;
		_class = ItemClass.ALL;
	}

	/**
	 * Sets the ownerID of the item
	 * @param owner_id : int designating the ID of the owner
	 */
	public void setOwnerId(int owner_id)
	{
		if (owner_id == _owner_id)
			return;
		_owner_id = owner_id;
		_storedInDb = false;
	}

	/**
	 * Returns the ownerID of the item
	 * @return int : ownerID of the item
	 */
	public int getOwnerId()
	{
		return _owner_id;
	}

	/**
	 * Sets the location of the item
	 * @param loc : ItemLocation (enumeration)
	 */
	public void setLocation(ItemLocation loc)
	{
		setLocation(loc, 0);
	}

	/**
	 * Sets the location of the item.<BR><BR>
	 * <U><I>Remark :</I></U> If loc and loc_data different from database, say datas not up-to-date
	 * @param loc : ItemLocation (enumeration)
	 * @param loc_data : int designating the slot where the item is stored or the village for freights
	 */
	public void setLocation(ItemLocation loc, int loc_data)
	{
		if (loc == _loc && loc_data == _loc_data)
			return;
		_loc = loc;
		_loc_data = loc_data;
		_storedInDb = false;
	}

	public ItemLocation getLocation()
	{
		return _loc;
	}

	/**
	 * Returns the quantity of item
	 * @return int
	 */
	public int getCount()
	{
		return _count;
	}

	/**
	 * Sets the quantity of the item.<BR><BR>
	 * <U><I>Remark :</I></U> If loc and loc_data different from database, say datas not up-to-date
	 * @param count : int
	 */
	public void setCount(int count)
	{
		if (_count == count)
			return;
		_count = count;
		_storedInDb = false;
	}

	/**
	 * Returns if item is equipable
	 * @return boolean
	 */
	public boolean isEquipable()
	{
		return !(_item.getBodyPart() == 0 || _item instanceof L2EtcItem );
	}

	/**
	 * Returns if item is equipped
	 * @return boolean
	 */
	public boolean isEquipped()
	{
		return _loc == ItemLocation.PAPERDOLL || _loc == ItemLocation.PET_EQUIP;
	}

	/**
	 * Returns the slot where the item is stored
	 * @return int
	 */
	public int getEquipSlot()
	{
		if (Config.ASSERT) assert _loc == ItemLocation.PAPERDOLL || _loc == ItemLocation.PET_EQUIP;
		return _loc_data;
	}

	/**
	 * Returns the characteristics of the item
	 * @return L2Item
	 */
	public L2Item getItem()
	{
		return _item;
	}

	public int getCustomType1()
	{
		return _type1;
	}
	public int getCustomType2()
	{
		return _type2;
	}
	public void setCustomType1(int newtype)
	{
		_type1=newtype;
	}
	public void setCustomType2(int newtype)
	{
		_type2=newtype;
	}
	public void setDropTime(long time)
	{
		_dropTime=time;
	}
	public long getDropTime()
	{
		return _dropTime;
	}

	public boolean isWear()
	{
		return _wear;
	}

	public void setWear(boolean newwear)
	{
		_wear=newwear;
	}
	/**
	 * Returns the type of item
	 * @return Enum
	 */
	public Enum getItemType()
	{
		return _item.getItemType();
	}

	/**
	 * Returns the ID of the item
	 * @return int
	 */
	public int getItemId()
	{
		return _itemId;
	}

	/**
	 * Returns the reference price of the item
	 * @return int
	 */
	public int getReferencePrice()
	{
		return _item.getReferencePrice();
	}

	/**
	 * Returns the price of the item for selling
	 * @return int
	 */
	public int getPriceToSell()
	{
		return _price_sell;
	}

	/**
	 * Sets the price of the item for selling
	 * <U><I>Remark :</I></U> If loc and loc_data different from database, say datas not up-to-date
	 * @param price : int designating the price
	 */
	public void setPriceToSell(int price)
	{
		_price_sell = price;
		_storedInDb = false;
	}

	/**
	 * Returns the price of the item for buying
	 * @return int
	 */
	public int getPriceToBuy()
	{
		return _price_buy;
	}

	/**
	 * Sets the price of the item for buying
	 * <U><I>Remark :</I></U> If loc and loc_data different from database, say datas not up-to-date
	 * @param price : int
	 */
	public void setPriceToBuy(int price)
	{
		_price_buy = price;
		_storedInDb = false;
	}

	/**
	 * Returns the last change of the item
	 * @return int
	 */
	public int getLastChange()
	{
		return _lastChange;
	}

	/**
	 * Sets the last change of the item
	 * @param lastChange : int
	 */
	public void setLastChange(int lastChange)
	{
		_lastChange = lastChange;
	}

	/**
	 * Returns if item is stackable
	 * @return boolean
	 */
	public boolean isStackable()
	{
		return _item.isStackable();
	}

	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.model.L2Object#onAction(net.sf.l2j.gameserver.model.L2PcInstance)
	 */
	@SuppressWarnings("static-access")
	public void onAction(L2PcInstance player)
	{
		if(GameServer.events.onAction(player, this))
			return;

		// this causes the validate position handler to do the pickup if the location is reached.
		player.setIntention(CtrlIntention.AI_INTENTION_PICK_UP, this, null);
	}
	/**
	 * Returns the level of enchantment of the item
	 * @return int
	 */
	public int getEnchantLevel()
	{
		return _enchantLevel;
	}

	/**
	 * Sets the level of enchantment of the item
	 * @param int
	 */
	public void setEnchantLevel(int enchantLevel)
	{
		if (_enchantLevel == enchantLevel)
			return;
		_enchantLevel = enchantLevel;
		_storedInDb = false;
	}

	/**
	 * Returns the physical defense of the item
	 * @return int
	 */
	public int getPDef()
	{
		if (_item instanceof L2Armor)
			return ((L2Armor)_item).getPDef();
		return 0;
	}

	/**
	 * Returns false cause item can't be attacked
	 * @return boolean false
	 */
    public boolean isAutoAttackable(@SuppressWarnings("unused") L2Character attacker)
    {
        return false;
    }

	/**
	 * Returns the type of charge with SoulShot of the item.
	 * @return int (CHARGED_NONE, CHARGED_SOULSHOT)
	 */
	public int getChargedSoulshot()
	{
		return 	_chargedSoulshot;
	}

	/**
	 * Returns the type of charge with SpiritShot of the item
	 * @return int (CHARGED_NONE, CHARGED_SPIRITSHOT, CHARGED_BLESSED_SPIRITSHOT)
	 */
	public int getChargedSpiritshot()
	{
		return _chargedSpiritshot;
	}

	/**
	 * Sets the type of charge with SoulShot of the item
	 * @param type : int (CHARGED_NONE, CHARGED_SOULSHOT)
	 */
	public void setChargedSoulshot(int type)
	{
		_chargedSoulshot = type;
	}

	/**
	 * Sets the type of charge with SpiritShot of the item
	 * @param type : int (CHARGED_NONE, CHARGED_SPIRITSHOT, CHARGED_BLESSED_SPIRITSHOT)
	 */
	public void setChargedSpiritshot(int type)
	{
		_chargedSpiritshot = type;
	}

    /**
     * This function basically returns a set of functions from
     * L2Item/L2Armor/L2Weapon, but may add additional
     * functions, if this particular item instance is enhanched
     * for a particular player.
     * @param player : L2Character designating the player
     * @return Func[]
     */
    public Func[] getStatFuncs(L2Character player)
    {
    	return getItem().getStatFuncs(this, player);
    }

    /**
     * Updates database.<BR><BR>
     * <U><I>Concept : </I></U><BR>
     *
     * <B>IF</B> the item exists in database :
     * <UL>
     * 		<LI><B>IF</B> the item has no owner, or has no location, or has a null quantity : remove item from database</LI>
     * 		<LI><B>ELSE</B> : update item in database</LI>
     * </UL>
     *
     * <B> Otherwise</B> :
     * <UL>
     * 		<LI><B>IF</B> the item hasn't a null quantity, and has a correct location, and has a correct owner : insert item in database</LI>
     * </UL>
     */
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

	/**
	 * Returns a L2ItemInstance stored in database from its objectID
	 * @param objectId : int designating the objectID of the item
	 * @return L2ItemInstance
	 */
	public static L2ItemInstance restoreFromDb(int objectId) {
		L2ItemInstance inst = null;
		java.sql.Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("SELECT owner_id, object_id, item_id, count, enchant_level, loc, loc_data, price_sell, price_buy, custom_type1, custom_type2 FROM items WHERE object_id = ?");
			statement.setInt(1, objectId);
			ResultSet rs = statement.executeQuery();
			if (rs.next()) {
				int owner_id = rs.getInt("owner_id");
				int item_id = rs.getInt("item_id");
				int count = rs.getInt("count");
				ItemLocation loc = ItemLocation.valueOf(rs.getString("loc"));
				int loc_data = rs.getInt("loc_data");
				int enchant_level = rs.getInt("enchant_level");
				int custom_type1 =  rs.getInt("custom_type1");
				int custom_type2 =  rs.getInt("custom_type2");
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
				inst._type1 = custom_type1;
				inst._type2 = custom_type2;
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

	/**
	 * Update the database with values of the item
	 */
	private void updateInDb()
    {
		if (Config.ASSERT) assert this._existsInDb;
		if (this._wear)
			return;
		if (this._storedInDb)
			return;

		java.sql.Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(
					"UPDATE items SET owner_id=?,count=?,loc=?,loc_data=?,enchant_level=?,price_sell=?,price_buy=?,custom_type1=?,custom_type2=? " +
					"WHERE object_id = ?");
			statement.setInt(1, _owner_id);
			statement.setInt(2, getCount());
			statement.setString(3, _loc.name());
			statement.setInt(4, _loc_data);
			statement.setInt(5, getEnchantLevel());
			statement.setInt(6, _price_sell);
			statement.setInt(7, _price_buy);
			statement.setInt(8, getCustomType1());
			statement.setInt(9, getCustomType2());
			statement.setInt(10, getObjectId());
			statement.executeUpdate();
			_existsInDb = true;
			_storedInDb = true;
            statement.close();
        } catch (Exception e) {
			_log.log(Level.SEVERE, "Could not update item "+getObjectId()+" itemID "+_itemId+" count "+getCount()+" owner "+_owner_id+" in DB:", e);
		} finally {
			try { con.close(); } catch (Exception e) {}
		}
	}

	/**
	 * Insert the item in database
	 */
	private void insertIntoDb() {
		if (this._wear)
			return;
		if (Config.ASSERT) assert !this._existsInDb && this.getObjectId() != 0;
		java.sql.Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(
					"INSERT INTO items (owner_id,item_id,count,loc,loc_data,enchant_level,price_sell,price_buy,object_id,custom_type1,custom_type2) " +
					"VALUES (?,?,?,?,?,?,?,?,?,?,?)");
			statement.setInt(1, _owner_id);
			statement.setInt(2, _itemId);
			statement.setInt(3, getCount());
			statement.setString(4, _loc.name());
			statement.setInt(5, _loc_data);
			statement.setInt(6, getEnchantLevel());
			statement.setInt(7, _price_sell);
			statement.setInt(8, _price_buy);
			statement.setInt(9, getObjectId());
			statement.setInt(10, _type1);
			statement.setInt(11, _type2);

			statement.executeUpdate();
			_existsInDb = true;
			_storedInDb = true;
            statement.close();
        } catch (Exception e) {
        	_log.log(Level.SEVERE, "Could not insert item "+getObjectId()+" itemID "+_itemId+" count "+getCount()+" owner "+_owner_id+" into DB:", e);
		} finally {
			try { con.close(); } catch (Exception e) {}
		}
	}

	/**
	 * Delete item from database
	 */
	private void removeFromDb() {
		if (this._wear)
			return;
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

    public QuestState[] getQuestsForItemUse(L2ItemInstance item)
    {
        // Create a QuestState table that will contain all QuestState to modify
        QuestState[] states = null;

        L2PcInstance itemOwner = (L2PcInstance)L2World.findObject(this.getOwnerId());
        // Go through the QuestState of the L2PcInstance quests
        if(itemOwner==null)
            return states;
        for (QuestState qs : itemOwner.getAllQuestsStates())
        {
            // Check if the Identifier of the L2Attackable attck is needed for the current quest
            if (qs.waitsForItemUse(item.getItemId()))
            {
                // Copy the current L2PcInstance QuestState in the QuestState table
                if (states == null)
                    states = new QuestState[]{qs};
                else
                    states = addToQuestStateArray(states, qs);
            }
        }

        // Return a table containing all QuestState to modify
        return states;
    }
    
    private QuestState[] addToQuestStateArray(QuestState[] questStateArray, QuestState state) {
        int len = questStateArray.length;
        QuestState[] tmp = new QuestState[len+1];
        for (int i=0; i < len; i++)
            tmp[i] = questStateArray[i];
        tmp[len] = state;
        return tmp;
    }
    
	/**
	 * Returns the item in String format
	 * @return String
	 */
	public String toString()
	{
		return ""+_item;
	}
}
