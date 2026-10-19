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


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.l2j.Config;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.skills.SkillsEngine;
import net.sf.l2j.gameserver.templates.L2Armor;
import net.sf.l2j.gameserver.templates.L2ArmorType;
import net.sf.l2j.gameserver.templates.L2EtcItem;
import net.sf.l2j.gameserver.templates.L2EtcItemType;
import net.sf.l2j.gameserver.templates.L2Item;
import net.sf.l2j.gameserver.templates.L2Weapon;
import net.sf.l2j.gameserver.templates.L2WeaponType;
import net.sf.l2j.gameserver.templates.StatsSet;

/**
 * This class ...
 * 
 * @version $Revision: 1.9.2.6.2.9 $ $Date: 2005/04/02 15:57:34 $
 */
public class ItemTable
{
	private static Logger _log = Logger.getLogger(ItemTable.class.getName());
	
	private static final HashMap<String, Integer> _materials = new HashMap<String, Integer>();
	private static final HashMap<String, Integer> _crystalTypes = new HashMap<String, Integer>();
	private static final HashMap<String, L2WeaponType> _weaponTypes = new HashMap<String, L2WeaponType>();
	private static final HashMap<String, L2ArmorType> _armorTypes = new HashMap<String, L2ArmorType>();
	private static final HashMap<String, Integer> _slots = new HashMap<String, Integer>();
	
	private L2Item[] _allTemplates;
	private HashMap<Integer, L2EtcItem> _etcItems;
	private HashMap<Integer, L2Armor>   _armors;
	private HashMap<Integer, L2Weapon>  _weapons;
	
	private HashMap _crystalList;
	
	private boolean _initialized = true;
	
	static
	{
		_materials.put("paper",            L2Item.MATERIAL_PAPER);
		_materials.put("wood",             L2Item.MATERIAL_WOOD);
		_materials.put("liquid",           L2Item.MATERIAL_LIQUID);
		_materials.put("cloth",            L2Item.MATERIAL_CLOTH);
		_materials.put("leather",          L2Item.MATERIAL_LEATHER);
		_materials.put("horn",             L2Item.MATERIAL_HORN);
		_materials.put("bone",             L2Item.MATERIAL_BONE);
		_materials.put("bronze",           L2Item.MATERIAL_BRONZE);
		_materials.put("fine_steel",       L2Item.MATERIAL_FINE_STEEL);
		_materials.put("cotton",           L2Item.MATERIAL_FINE_STEEL);
		_materials.put("mithril",          L2Item.MATERIAL_MITHRIL);
		_materials.put("silver",           L2Item.MATERIAL_SILVER);
		_materials.put("gold",             L2Item.MATERIAL_GOLD);
		_materials.put("adamantaite",      L2Item.MATERIAL_ADAMANTAITE);
		_materials.put("steel",            L2Item.MATERIAL_STEEL);
		_materials.put("oriharukon",       L2Item.MATERIAL_ORIHARUKON);
		_materials.put("blood_steel",      L2Item.MATERIAL_BLOOD_STEEL);
		_materials.put("crystal",          L2Item.MATERIAL_CRYSTAL);
		_materials.put("damascus",         L2Item.MATERIAL_DAMASCUS);
		_materials.put("chrysolite",       L2Item.MATERIAL_CHRYSOLITE);
		_materials.put("scale_of_dragon",  L2Item.MATERIAL_SCALE_OF_DRAGON);
		_materials.put("dyestuff",         L2Item.MATERIAL_DYESTUFF);
		_materials.put("cobweb",           L2Item.MATERIAL_COBWEB);
        _materials.put("seed",             L2Item.MATERIAL_SEED);
        
		_crystalTypes.put("s",             L2Item.CRYSTAL_S);
		_crystalTypes.put("a",             L2Item.CRYSTAL_A);
		_crystalTypes.put("b",             L2Item.CRYSTAL_B);
		_crystalTypes.put("c",             L2Item.CRYSTAL_C);
		_crystalTypes.put("d",             L2Item.CRYSTAL_D);
		_crystalTypes.put("none",          L2Item.CRYSTAL_NONE);

		_weaponTypes.put("blunt",          L2WeaponType.BLUNT);
		_weaponTypes.put("bow",            L2WeaponType.BOW);
		_weaponTypes.put("dagger",         L2WeaponType.DAGGER);
		_weaponTypes.put("dual",           L2WeaponType.DUAL);
		_weaponTypes.put("dualfist",       L2WeaponType.DUALFIST);
		_weaponTypes.put("etc",            L2WeaponType.ETC);
		_weaponTypes.put("fist",           L2WeaponType.FIST);
		_weaponTypes.put("none",           L2WeaponType.NONE); // these are shields !
		_weaponTypes.put("pole",           L2WeaponType.POLE);
		_weaponTypes.put("sword",          L2WeaponType.SWORD);
		
		_armorTypes.put("none",            L2ArmorType.NONE);
		_armorTypes.put("light",           L2ArmorType.LIGHT);
		_armorTypes.put("heavy",           L2ArmorType.HEAVY);
		_armorTypes.put("magic",           L2ArmorType.MAGIC);
		
		_slots.put("chest",                L2Item.SLOT_CHEST);
		_slots.put("CHEST",                L2Item.SLOT_FULL_ARMOR); 
		_slots.put("head",                 L2Item.SLOT_HEAD);
		_slots.put("hair",                 L2Item.SLOT_HAIR);
		_slots.put("underwear",            L2Item.SLOT_UNDERWEAR);
		_slots.put("back",                 L2Item.SLOT_BACK);
		_slots.put("neck",                 L2Item.SLOT_NECK);
		_slots.put("legs",                 L2Item.SLOT_LEGS);
		_slots.put("feet",                 L2Item.SLOT_FEET);
		_slots.put("gloves",               L2Item.SLOT_GLOVES);
		_slots.put("chest,legs",           L2Item.SLOT_CHEST | L2Item.SLOT_LEGS);
		_slots.put("rhand",                L2Item.SLOT_R_HAND);
		_slots.put("lhand",                L2Item.SLOT_L_HAND);
		_slots.put("lrhand",               L2Item.SLOT_LR_HAND);
		_slots.put("rear,lear",            L2Item.SLOT_R_EAR | L2Item.SLOT_L_EAR);
		_slots.put("rfinger,lfinger",      L2Item.SLOT_R_FINGER | L2Item.SLOT_L_FINGER);
		_slots.put("none",                 L2Item.SLOT_NONE);
	}

	
	private static ItemTable _instance;
	
	public static ItemTable getInstance()
	{
		if (_instance == null)
		{
			_instance = new ItemTable();
		}
		return _instance;
	}
	
	public ItemTable()
	{
		fillEtcItemsTable();
		fillArmorsTable();
		FillWeaponsTable();
		buildFastLookupTable();
		
		//free unused stuff
		_crystalList = null;
	}
	
	public boolean isInitialized()
	{
	    return _initialized;
	}
	
	private void fillEtcItemsTable()
	{
		_etcItems = new HashMap<Integer, L2EtcItem>();
		java.sql.Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("SELECT * FROM etcitem");
			ResultSet rset = statement.executeQuery();
			
			while(rset.next())
			{
				StatsSet set = new StatsSet();
				L2EtcItemType type;
				int itemId = rset.getInt("item_id");
				
				set.set("item_id", itemId);
				set.set("crystallizable", Boolean.valueOf(rset.getString("crystallizable")));
				set.set("type1", L2Item.TYPE1_ITEM_QUESTITEM_ADENA);
				set.set("type2", L2Item.TYPE2_OTHER);
				set.set("bodypart", 0);
				set.set("crystal_count", rset.getInt("crystal_count"));
				set.set("sellable", Boolean.valueOf(rset.getString("sellable")));
				String itemType = rset.getString("item_type");
				if (itemType.equals("none"))              type = L2EtcItemType.OTHER; // only for default
				else if (itemType.equals("castle_guard")) type = L2EtcItemType.SCROLL; // dummy
				else if (itemType.equals("material"))     type = L2EtcItemType.MATERIAL;
				else if (itemType.equals("pet_collar"))   type = L2EtcItemType.PET_COLLAR;
				else if (itemType.equals("potion"))       type = L2EtcItemType.POTION;
				else if (itemType.equals("recipe"))       type = L2EtcItemType.RECEIPE;
				else if (itemType.equals("scroll"))       type = L2EtcItemType.SCROLL;
                else if (itemType.equals("seed"))         type = L2EtcItemType.SEED;
				else if (itemType.equals("arrow"))
				{
					type = L2EtcItemType.ARROW;
					set.set("bodypart", L2Item.SLOT_L_HAND);
				}
				else
				{
					_log.info("unknown etcitem type:" + itemType);
					type = L2EtcItemType.OTHER;
				}

				String consume = rset.getString("consume_type");
				if (consume.equals("asset"))
				{
					type = L2EtcItemType.MONEY;
					set.set("stackable", true);
					set.set("type2", L2Item.TYPE2_MONEY);
				}
				else if (consume.equals("stackable"))
				{
					set.set("stackable", true);
				}
				else
				{
					set.set("stackable", false);
				}

				int material = _materials.get(rset.getString("material"));
				set.set("material", material);

				int crystal = _crystalTypes.get(rset.getString("crystal_type"));
				set.set("crystal_type", crystal);

				int weight = rset.getInt("weight");
				set.set("weight", weight);
				String name = rset.getString("name");
				set.set("name", name);

				set.set("durability", rset.getInt("durability"));
				set.set("price", rset.getInt("price"));

				if (weight == 0 && type != L2EtcItemType.MONEY && 
						!name.startsWith("world_map") &&
						!name.startsWith("crystal_"))
				{
					type = L2EtcItemType.QUEST;
					set.set("type2", L2Item.TYPE2_QUEST);
				}
				else if (name.startsWith("sb_"))
				{
					type = L2EtcItemType.SPELLBOOK;
					set.set("type2", L2Item.TYPE2_OTHER);
				}
				else if (name.startsWith("rp_"))
				{
					type = L2EtcItemType.RECEIPE;
					set.set("type2", L2Item.TYPE2_OTHER);
				}
				else if (name.startsWith("q_"))
				{
					type = L2EtcItemType.QUEST;
					set.set("type2", L2Item.TYPE2_QUEST);
				}
				
				try
				{
				L2EtcItem item = SkillsEngine.getInstance().loadEtcItem(itemId, type, name, set);
				if (item == null)
					item = new L2EtcItem(type, set);

				_etcItems.put(item.getItemId(), item);
				} catch (Exception e) {
					_log.log(Level.WARNING, "etcitem id="+itemId+" creation error: ", e);
					continue;
				}
			}
			
			rset.close();
			statement.close();
			
			_log.config("parsed " + _etcItems.size() + " etc items");
		} catch (Exception e) {
			_log.log(Level.WARNING, "data error on etc item: ", e);
		} finally {
			try { con.close(); } catch (Exception e) {}
		}
	}

	
	private void fillArmorsTable()
	{
		_armors = new HashMap<Integer, L2Armor>();
		java.sql.Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("SELECT * FROM armor");
			ResultSet rset = statement.executeQuery();
			
			while(rset.next())
			{
				StatsSet set = new StatsSet();
				L2ArmorType type = _armorTypes.get(rset.getString("armor_type"));
				int itemId = rset.getInt("item_id");
				String name = rset.getString("name");
				
				set.set("item_id", itemId);
				set.set("name", name);
				int bodypart = _slots.get(rset.getString("bodypart"));
				set.set("bodypart", bodypart);
				set.set("crystallizable", Boolean.valueOf(rset.getString("crystallizable")));
				set.set("crystal_count", rset.getInt("crystal_count"));
				set.set("sellable", Boolean.valueOf(rset.getString("sellable")));
				if (bodypart == L2Item.SLOT_NECK ||
					bodypart == L2Item.SLOT_HAIR ||
					(bodypart & L2Item.SLOT_L_EAR) != 0 || 
					(bodypart & L2Item.SLOT_L_FINGER) != 0)
				{
					set.set("type1", L2Item.TYPE1_WEAPON_RING_EARRING_NECKLACE);
					set.set("type2", L2Item.TYPE2_ACCESSORY);
				}
				else
				{
					set.set("type1", L2Item.TYPE1_SHIELD_ARMOR);
					set.set("type2", L2Item.TYPE2_SHIELD_ARMOR);
				}
				
				set.set("weight", rset.getInt("weight"));
				set.set("material", _materials.get(rset.getString("material")));
				set.set("crystal_type", _crystalTypes.get(rset.getString("crystal_type")));
				set.set("avoid_modify", rset.getInt("avoid_modify"));
				set.set("durability", rset.getInt("durability"));
				set.set("p_def", rset.getInt("p_def"));
				set.set("m_def", rset.getInt("m_def"));
				set.set("mp_bonus", rset.getInt("mp_bonus"));
				set.set("price", rset.getInt("price"));
				
				L2Armor armor;
				try {
					armor = SkillsEngine.getInstance().loadArmor(itemId, type, name, set);
				if (armor == null)
					armor = new L2Armor(type, set);
				
				_armors.put(armor.getItemId(), armor);
				} catch (Exception e) {
					_log.log(Level.WARNING, "armor id="+itemId+" creation error: ", e);
					continue;
				}
				
				// export
				if (false)
				{
					StringBuffer fname = new StringBuffer();
					fname.append(new DecimalFormat("0000").format(armor.getItemId()));
					fname.append('_').append(armor.getName());
					fname.setCharAt(5, Character.toUpperCase(fname.charAt(5)));
					for (int i=5; i < fname.length(); i++)
					{
						if (fname.charAt(i) == '_') {
							fname.deleteCharAt(i);
							fname.setCharAt(i, Character.toUpperCase(fname.charAt(i)));
						}
						else if (fname.charAt(i) == '\'') {
							fname.deleteCharAt(i);
							fname.deleteCharAt(i);
							i--;
						}
						else if (fname.charAt(i) == '*') {
							fname.setCharAt(i, '_');
							fname.setCharAt(i+1, Character.toUpperCase(fname.charAt(i+1)));
						}
					}
					File file = new File(Config.DATAPACK_ROOT, "data/export/armor/"+fname+".xml");
					BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8")); 
					out.write("<?xml version='1.0' encoding='utf-8'?>\n");
					out.write("<item id='"+armor.getItemId()+"' name=\""+armor.getName()+"\">\n\n");
					out.write("  <for>\n\n");
					if (armor.getPDef() != 0)
						out.write("    <add val='"+armor.getPDef()+"' order='0x10' stat='pDef' />\n");
					if (armor.getMDef() != 0)
						out.write("    <add val='"+armor.getMDef()+"' order='0x10' stat='mDef' />\n");
					if (armor.getAvoidModifier() != 0)
						out.write("    <add val='"+armor.getAvoidModifier()+"' order='0x10' stat='rEvas' />\n");
					if (armor.getMpBonus() != 0)
						out.write("    <add val='"+armor.getMpBonus()+"' order='0x10' stat='maxMp' />\n");
					out.write("\n");
					out.write("  </for>\n\n");
					out.write("</item>\n");
					out.close();
				}
			}
			
			rset.close();
			statement.close();
			_log.config("parsed " + _armors.size() + " armors");

		} catch (Exception e) {
			_log.log(Level.WARNING, "data error on armor: ", e);
		} finally {
			try { con.close(); } catch (Exception e) {}
		}
	}	

	private void FillWeaponsTable()
	{
		_weapons = new HashMap<Integer, L2Weapon>();
		java.sql.Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("SELECT * FROM weapon");
			ResultSet rset = statement.executeQuery();

			while(rset.next())
			{
				StatsSet set = new StatsSet();
				L2WeaponType type = _weaponTypes.get(rset.getString("weaponType"));
				int itemId = rset.getInt("item_id");
				String name = rset.getString("name");
				
				set.set("item_id", itemId);
				set.set("name", name);
				
				// lets see if this is a shield
				if (type == L2WeaponType.NONE)
				{
					set.set("type1", L2Item.TYPE1_SHIELD_ARMOR);
					set.set("type2", L2Item.TYPE2_SHIELD_ARMOR);
				}
				else
				{
					set.set("type1", L2Item.TYPE1_WEAPON_RING_EARRING_NECKLACE);
					set.set("type2", L2Item.TYPE2_WEAPON);
				}
				
				set.set("bodypart",       _slots.get(rset.getString("bodypart")));
				set.set("material",       _materials.get(rset.getString("material")));
				set.set("crystal_type",   _crystalTypes.get(rset.getString("crystal_type")));
				set.set("crystallizable", Boolean.valueOf(rset.getString("crystallizable")).booleanValue());
				set.set("weight",         rset.getInt("weight"));
				set.set("soulshots",      rset.getInt("soulshots"));
				set.set("spiritshots",    rset.getInt("spiritshots"));
				set.set("p_dam",          rset.getInt("p_dam"));
				set.set("rnd_dam",        rset.getInt("rnd_dam"));
				set.set("critical",       rset.getInt("critical"));
				set.set("hit_modify",     rset.getDouble("hit_modify"));
				set.set("avoid_modify",   rset.getInt("avoid_modify"));
				set.set("shield_def",     rset.getInt("shield_def"));
				set.set("shield_def_rate",rset.getInt("shield_def_rate"));
				set.set("atk_speed",      rset.getInt("atk_speed"));
				set.set("mp_consume",     rset.getInt("mp_consume"));
				set.set("m_dam",          rset.getInt("m_dam"));
				set.set("durability",     rset.getInt("durability"));
				set.set("price",          rset.getInt("price"));
				set.set("crystal_count",  rset.getInt("crystal_count"));
				set.set("sellable",       Boolean.valueOf(rset.getString("sellable")));
				
				L2Weapon weapon;
				try {
					weapon = SkillsEngine.getInstance().loadWeapon(itemId, type, name, set);
				if (weapon == null)
					weapon = new L2Weapon(type, set);
				
				_weapons.put(weapon.getItemId(), weapon);
				} catch (Exception e) {
					_log.log(Level.WARNING, "weapon id="+itemId+" creation error: ", e);
					continue;
				}
				
				// export
				if (false)
				{
					StringBuffer fname = new StringBuffer();
					fname.append(new DecimalFormat("0000").format(weapon.getItemId()));
					fname.append('_').append(weapon.getName());
					fname.setCharAt(5, Character.toUpperCase(fname.charAt(5)));
					for (int i=5; i < fname.length(); i++)
					{
						if (fname.charAt(i) == '_') {
							fname.deleteCharAt(i);
							fname.setCharAt(i, Character.toUpperCase(fname.charAt(i)));
						}
						else if (fname.charAt(i) == '\'') {
							fname.deleteCharAt(i);
							fname.deleteCharAt(i);
							i--;
						}
						else if (fname.charAt(i) == '*') {
							fname.setCharAt(i, '_');
							fname.setCharAt(i+1, Character.toUpperCase(fname.charAt(i+1)));
						}
					}
					File file = new File(Config.DATAPACK_ROOT, "data/export/weapon/"+fname+".xml");
					BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8")); 
					out.write("<?xml version='1.0' encoding='utf-8'?>\n");
					out.write("<item id='"+weapon.getItemId()+"' name=\""+weapon.getName()+"\">\n\n");
					out.write("  <for>\n\n");
					if (weapon.getPDamage() != 0)
						out.write("    <add val='"+weapon.getPDamage()+"' order='0x10' stat='pAtk' />\n");
					if (weapon.getMDamage() != 0)
						out.write("    <add val='"+weapon.getMDamage()+"' order='0x10' stat='mAtk' />\n");
					if (weapon.getCritical() != 0)
						out.write("    <add val='"+weapon.getCritical()+"' order='0x10' stat='rCrit' />\n");
					if (weapon.getHitModifier() != 0)
						out.write("    <add val='"+weapon.getHitModifier()+"' order='0x10' stat='accCombat' />\n");
					if (weapon.getAvoidModifier() != 0)
						out.write("    <add val='"+weapon.getAvoidModifier()+"' order='0x10' stat='rEvas' />\n");
					if (weapon.getShieldDef() != 0)
						out.write("    <add val='"+weapon.getShieldDef()+"' order='0x10' stat='sDef' />\n");
					if (weapon.getShieldDefRate() != 0)
						out.write("    <add val='"+weapon.getShieldDefRate()+"' order='0x10' stat='rShld' />\n");
					if (weapon.getAttackSpeed() != 0)
						out.write("    <add val='"+weapon.getAttackSpeed()+"' order='0x10' stat='pAtkSpd' />\n");
					out.write("\n");
					out.write("  </for>\n\n");
					out.write("</item>\n");
					out.close();
				}
			}
			
			rset.close();
			statement.close();
			_log.config("parsed " + _weapons.size() + " weapons");
		} catch (Exception e) {
			_log.log(Level.WARNING, "data error on weapon: ", e);
		} finally {
			try { con.close(); } catch (Exception e) {}
		}
	}	

	private void buildFastLookupTable()
	{
		int highestId = 0;		
		
		for (Iterator<Integer> iter = _armors.keySet().iterator(); iter.hasNext();)
		{
			Integer id = iter.next();
			L2Armor item = _armors.get(id);
			if (item.getItemId() > highestId)
			{
				highestId = item.getItemId();
			}
		}

		for (Iterator<Integer> iter = _weapons.keySet().iterator(); iter.hasNext();)
		{
			
			Integer id = iter.next();
			L2Weapon item = _weapons.get(id);
			if (item.getItemId() > highestId)
			{
				highestId = item.getItemId();
			}
		}
		
		for (Iterator<Integer> iter = _etcItems.keySet().iterator(); iter.hasNext();)
		{
			Integer id = iter.next();
			L2EtcItem item = _etcItems.get(id);
			if (item.getItemId() > highestId)
			{
				highestId = item.getItemId();
			}
		}
		
		if (Config.DEBUG) _log.fine("highest item id used:" + highestId);
		_allTemplates = new L2Item[highestId +1];
		
		for (Iterator<Integer> iter = _armors.keySet().iterator(); iter.hasNext();)
		{
			Integer id = iter.next();
			L2Armor item = _armors.get(id);
			assert _allTemplates[id.intValue()] == null;
			_allTemplates[id.intValue()] = item;
		}

		for (Iterator<Integer> iter = _weapons.keySet().iterator(); iter.hasNext();)
		{
			Integer id = iter.next();
			L2Weapon item = _weapons.get(id);
			assert _allTemplates[id.intValue()] == null;
			_allTemplates[id.intValue()] = item;
		}
		
		for (Iterator<Integer> iter = _etcItems.keySet().iterator(); iter.hasNext();)
		{
			Integer id = iter.next();
			L2EtcItem item = _etcItems.get(id);
			assert _allTemplates[id.intValue()] == null;
			_allTemplates[id.intValue()] = item;
		}
	}

	public L2Item getTemplate(int id)
	{
		return _allTemplates[id];
	}

	public L2ItemInstance createItem(int itemId)
	{
		L2ItemInstance temp = new L2ItemInstance(IdFactory.getInstance().getNextId(), itemId);
		
		if (Config.DEBUG) _log.fine("item created  oid:" + temp.getObjectId()+ " itemid:" + itemId);
		L2World.getInstance().storeObject(temp);
		return temp; 	
	}

	public L2ItemInstance createDummyItem(int itemId)
	{
		L2Item item = ItemTable.getInstance().getTemplate(itemId);
		if (item == null)
			return null;
		L2ItemInstance temp = new L2ItemInstance(0, item);
		try
		{
			temp = new L2ItemInstance(0, itemId);
		} 
		catch (ArrayIndexOutOfBoundsException e)
		{
			// this can happen if the item templates were not initialized
		}
		
		if (temp.getItem() == null)
		{
			_log.warning("item template missing. id:"+ itemId);
		}
		
		return temp; 	
	}

	
}
