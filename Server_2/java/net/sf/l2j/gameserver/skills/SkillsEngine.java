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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.templates.L2Armor;
import net.sf.l2j.gameserver.templates.L2ArmorType;
import net.sf.l2j.gameserver.templates.L2EtcItem;
import net.sf.l2j.gameserver.templates.L2EtcItemType;
import net.sf.l2j.gameserver.templates.L2Weapon;
import net.sf.l2j.gameserver.templates.L2WeaponType;
import net.sf.l2j.gameserver.templates.StatsSet;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * @author mkizub
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class SkillsEngine {

	static Logger _log = Logger.getLogger(SkillsEngine.class.getName());
	
	private static final SkillsEngine _instance = new SkillsEngine();
	
	private HashMap<Integer, File> _armorFiles = new HashMap<Integer, File>(); 
	private HashMap<Integer, File> _weaponFiles = new HashMap<Integer, File>(); 
	private HashMap<Integer, File> _etcitemFiles = new HashMap<Integer, File>(); 
	private HashMap<Integer, File> _skillFiles = new HashMap<Integer, File>(); 
	
	public static SkillsEngine getInstance()
	{
		return _instance;
	}
	
	private SkillsEngine()
	{
		hashFiles("data/stats/etcitem", _etcitemFiles);
		hashFiles("data/stats/armor", _armorFiles);
		hashFiles("data/stats/weapon", _weaponFiles);
		hashFiles("data/stats/skills", _skillFiles);
//		hashFiles("c:/tmp/xmldata", _skillFiles);
	}
	
	private void hashFiles(String dirname, HashMap<Integer, File> hash)
	{
		File dir = new File(Config.DATAPACK_ROOT, dirname);
		if (!dir.exists())
		{
			_log.config("Dir "+dir.getAbsolutePath()+" not exists");
			return;
		}
		File[] files = dir.listFiles();
		for (File f : files)
		{
			if (f.getName().endsWith(".xml"))
			{
				Integer id = Integer.valueOf(f.getName().substring(0, 4));
				hash.put(id, f);
			}
		}
	}
	
	public L2Armor loadArmor(int id, L2ArmorType type, String name, StatsSet set)
	{
		File f = _armorFiles.get(id);
		if (f == null)
		{
			_log.config("File not found for armor item id "+id+", name '"+name+"'");
			return null;
		}
		DocumentItem doc = new DocumentItem(id, type, name, set, f);
		doc.parse();
		return (L2Armor)doc.getItem();
	}
	
	public L2Weapon loadWeapon(int id, L2WeaponType type, String name, StatsSet set)
	{
		File f = _weaponFiles.get(id);
		if (f == null)
		{
			_log.config("File not found for weapon item id "+id+", name '"+name+"'");
			return null;
		}
		DocumentItem doc = new DocumentItem(id, type, name, set, f);
		doc.parse();
		return (L2Weapon)doc.getItem();
	}
	
	public L2EtcItem loadEtcItem(int id, L2EtcItemType type, String name, StatsSet set)
	{
		File f = _etcitemFiles.get(id);
		if (f == null)
		{
			_log.config("File not found for item id "+id+", name '"+name+"'");
			return null;
		}
		DocumentItem doc = new DocumentItem(id, type, name, set, f);
		doc.parse();
		return (L2EtcItem)doc.getItem();
	}
	
	public L2Skill[] loadSkills(int id, String name, StatsSet[] sets)
	{
		File f = _skillFiles.get(id);
		if (f == null)
		{
			_log.config("File not found for skill id "+id+", name '"+name+"'");
			return null;
		}
		DocumentSkill doc = new DocumentSkill(id, name, sets, f);
		doc.parse();
//		Document d = doc.parse();
//		export(d, f, sets);
		return doc.getSkills();
	}
	
	private static void export(Document doc, File f, StatsSet[] sets)
	{
		ArrayList<Element> tables = new ArrayList<Element>();
		ArrayList<Element> consts = new ArrayList<Element>();
		for (String key : sets[0].getSet().keySet())
		{
			if (key.equals("level") || key.equals("skill_id") || key.equals("name"))
				continue;
			// check if the key is to be exported as a table,
			// or as a single value (that is - all values are the same)
			boolean as_table = false;
			Object value = sets[0].getSet().get(key);
			for (StatsSet s : sets)
			{
				Object v = s.getSet().get(key);
				if (!value.equals(v))
				{
					as_table = true;
					break;
				}
			}
			if (as_table)
			{
				Element n = doc.createElement("table");
				n.setAttribute("name", "#"+key);
				n.appendChild(doc.createTextNode(" "));
				for (StatsSet s : sets)
				{
					Object v = s.getSet().get(key);
					n.appendChild(doc.createTextNode(v + " "));
				}
				tables.add(n);
				n = doc.createElement("set");
				n.setAttribute("name", key);
				n.setAttribute("val", "#"+key);
				consts.add(n);
			}
			else
			{
				if (value.equals(0) || value.equals(false))
					continue;
				Element n = doc.createElement("set");
				n.setAttribute("name", key);
				n.setAttribute("val", value.toString());
				consts.add(n);
			}
		}
		Element root = (Element)doc.getElementsByTagName("skill").item(0);
		Element before = (Element)root.getElementsByTagName("for").item(0);
		for (Element e : tables) {
			root.insertBefore(e, before);
			root.insertBefore(doc.createTextNode("\n  "), before);
		}
		for (Element e : consts) {
			root.insertBefore(e, before);
			root.insertBefore(doc.createTextNode("\n  "), before);
		}
		try
		{
			Transformer trans = TransformerFactory.newInstance().newTransformer();
			trans.setOutputProperty(OutputKeys.INDENT, "yes");
			f = new File("c:/tmp/xmldata/"+f.getName());
			trans.transform(new DOMSource(doc), new StreamResult(f));
		} catch (Exception e) {
			_log.log(Level.SEVERE, "Error exporting file "+f, e);
		}
		
	}
	
//	private void loadAllItems(String dirname)
//	{
//		File dir = new File(Config.DATAPACK_ROOT, dirname);
//		if (!dir.exists())
//		{
//			System.err.println("Dir "+dir.getAbsolutePath()+" not exists");
//			return;
//		}
//		
//		int count = 0;
//		File[] files = dir.listFiles();
//		for (File f : files)
//		{
//			if (f.getName().endsWith(".xml"))
//			{
//				try
//				{
//					new DocumentItem(f).parse();
//					count++;
//				} catch (Exception e) {
//					System.err.println("Error loading item from "+f);
//					e.printStackTrace();
//				}
//			}
//		}
//		System.out.println("Processed XML description of "+count+" items");
//	}
	public void loadAllSkills(HashMap<Integer, L2Skill> allSkills)
	{
		int count = 0;
		for (int id : _skillFiles.keySet())
		{
			L2Skill[] s = loadSkills(id, null, null);
			for (L2Skill skill : s)
				allSkills.put(skill.getId()*100+ skill.getLevel(), skill);
			count++;
		}
		System.out.println("Processed XML description of "+count+" skill");
	}
}
