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
package net.sf.l2j.gameserver.skills;

import java.io.File;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.templates.L2Armor;
import net.sf.l2j.gameserver.templates.L2ArmorType;
import net.sf.l2j.gameserver.templates.L2EtcItem;
import net.sf.l2j.gameserver.templates.L2EtcItemType;
import net.sf.l2j.gameserver.templates.L2Item;
import net.sf.l2j.gameserver.templates.L2Weapon;
import net.sf.l2j.gameserver.templates.L2WeaponType;
import net.sf.l2j.gameserver.templates.StatsSet;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * @author mkizub
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
final class DocumentItem extends DocumentBase {

	private int id;
	private Enum type;
	private String name;
	private StatsSet set;
	private L2Item item;
	private int currentLevel; // enchant level
	
	DocumentItem(int id, Enum type, String name, StatsSet set, File file)
	{
		super(file);
		this.id = id;
		this.type = type;
		this.name = name;
		this.set = set;
	}
	protected StatsSet getStatsSet()
	{
		return set;
	}
	protected L2Item getItem()
	{
		return item;
	}
	
	protected Number getTableValue(String name)
	{
		return tables.get(name)[currentLevel];
	}
	protected Number getTableValue(String name, int idx)
	{
		return tables.get(name)[idx-1];
	}

	protected void parseDocument(Document doc)
	{
		for (Node n=doc.getFirstChild(); n != null; n = n.getNextSibling())
		{
			if ("item".equalsIgnoreCase(n.getNodeName()))
				parseItem(n);
		}
	}
	protected void parseItem(Node n)
	{
		int itemId = Integer.parseInt(n.getAttributes().getNamedItem("id").getNodeValue());
		String itemName = n.getAttributes().getNamedItem("name").getNodeValue();
		
		if (id == 0)
			id = itemId;
		if (id != itemId)
			throw new RuntimeException("Item id="+itemId+" missmatch, id="+id+" expected");
		
		if (name == null)
			name = itemName;
		if (!name.equals(itemName))
			if (Config.DEBUG) _log.fine("Item id="+itemId+" name missmatch, name='"+name+"' expected");
			//throw new RuntimeException("Item id="+itemId+" name missmatch, name='"+name+"' expected");
		
		Node first = n.getFirstChild();
		for (n=first; n != null; n = n.getNextSibling())
		{
			if ("table".equalsIgnoreCase(n.getNodeName()))
				parseTable(n);
		}
		for (n=first; n != null; n = n.getNextSibling())
		{
			if ("set".equalsIgnoreCase(n.getNodeName()))
				parseBeanSet(n, set, 1);
		}
		for (n=first; n != null; n = n.getNextSibling())
		{
			if ("for".equalsIgnoreCase(n.getNodeName()))
			{
				makeItem();
				parseTemplate(n, item);
			}
		}
	}
	private void makeItem()
	{
		if (item != null)
			return;
		if (type instanceof L2ArmorType)
			item = new L2Armor((L2ArmorType)type, set);
		else if (type instanceof L2WeaponType)
			item = new L2Weapon((L2WeaponType)type, set);
		else if (type instanceof L2EtcItemType)
			item = new L2EtcItem((L2EtcItemType)type, set);
		else
			throw new Error("Unknown item type "+type);
	}
}
