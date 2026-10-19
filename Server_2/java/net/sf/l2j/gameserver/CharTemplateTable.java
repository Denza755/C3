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

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.logging.Logger;

import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.model.ClassId;
import net.sf.l2j.gameserver.templates.L2PcTemplate;
import net.sf.l2j.gameserver.templates.StatsSet;

/**
 * This class ...
 * 
 * @version $Revision: 1.6.2.1.2.10 $ $Date: 2005/03/29 14:00:54 $
 */
public class CharTemplateTable
{
	private static Logger _log = Logger.getLogger(CharTemplateTable.class.getName());
			
	private static CharTemplateTable _instance;
	
	private HashMap<Integer, L2PcTemplate> _templates;
	
	public static CharTemplateTable getInstance()
	{
		if (_instance == null)
		{
			_instance = new CharTemplateTable();
		}
		return _instance;
	}
	
	private CharTemplateTable()
	{
		_templates = new HashMap<Integer, L2PcTemplate>();
		java.sql.Connection con = null;
		
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(
					"SELECT * FROM class_list, char_templates, lvlupgain" +
					" WHERE class_list.id = char_templates.classId" +
					" AND class_list.id = lvlupgain.classId" +
					" ORDER BY class_list.id");
			ResultSet rset = statement.executeQuery();

			while (rset.next())
			{
				StatsSet set = new StatsSet();
				ClassId classId = ClassId.values()[rset.getInt("class_list.id")];
				set.set("classId", rset.getInt("class_list.id"));
				set.set("className", rset.getString("char_templates.className"));
				set.set("raceId", rset.getInt("char_templates.raceId"));
				set.set("baseSTR", rset.getInt("char_templates.STR"));
				set.set("baseCON", rset.getInt("char_templates.CON"));
				set.set("baseDEX", rset.getInt("char_templates.DEX"));
				set.set("baseINT", rset.getInt("char_templates._INT"));
				set.set("baseWIT", rset.getInt("char_templates.WIT"));
				set.set("baseMEN", rset.getInt("char_templates.MEN"));
				set.set("baseHpMax", rset.getFloat("lvlupgain.defaultHpBase"));
				set.set("lvlHpAdd", rset.getFloat("lvlupgain.defaultHpAdd"));
				set.set("lvlHpMod", rset.getFloat("lvlupgain.defaultHpMod"));
				set.set("baseMpMax", rset.getFloat("lvlupgain.defaultMpBase"));
				set.set("lvlMpAdd", rset.getFloat("lvlupgain.defaultMpAdd"));
				set.set("lvlMpMod", rset.getFloat("lvlupgain.defaultMpMod"));
				set.set("baseHpReg", 0.01);
				set.set("baseMpReg", 0.01);
				set.set("basePAtk", rset.getInt("char_templates.p_atk"));
				set.set("basePDef", /*classId.isMage()? 77 : 129*/ rset.getInt("char_templates.p_def"));
				set.set("baseMAtk", rset.getInt("char_templates.m_atk"));
				set.set("baseMDef", 41 /*rset.getInt("char_templates.m_def")*/);
				set.set("classBaseLevel", rset.getInt("lvlupgain.class_lvl"));
				set.set("basePAtkSpd", rset.getInt("char_templates.p_spd"));
				set.set("baseMAtkSpd", classId.isMage()? 166 : 333 /*rset.getInt("char_templates.m_spd")*/);
				set.set("baseCritRate", 0 /*rset.getInt("char_templates.critical")*/);
				set.set("baseRunSpd", rset.getInt("char_templates.move_spd"));
				set.set("baseLoad", rset.getInt("char_templates._load"));
				set.set("baseShldDef", 0);
				set.set("baseShldRate", 0);
				set.set("baseAtkRange", 40);

				set.set("spawnX", rset.getInt("char_templates.x"));
				set.set("spawnY", rset.getInt("char_templates.y"));
				set.set("spawnZ", rset.getInt("char_templates.z"));

				L2PcTemplate ct;

				//
				// Male class
				//
				set.set("isMale", true);
				//set.setMUnk1(rset.getDouble(27));
				//set.setMUnk2(rset.getDouble(28));
				set.set("collision_radius", rset.getDouble("char_templates.m_col_r"));
				set.set("collision_height", rset.getDouble("char_templates.m_col_h"));
				ct = new L2PcTemplate(set);
				//5items must go here
				for (int x=1; x < 6 ;x++)
				{
					if (rset.getInt(34+x) != 0)
					{
						ct.addItem(rset.getInt("char_templates.items"+x));
					}
				}
				_templates.put(ct.classId.getId(), ct);

				//
				// Female class
				//
				set.set("isMale", false);
				//set.setFUnk1(rset.getDouble(31));
				//set.setFUnk2(rset.getDouble(32));
				set.set("collision_radius", rset.getDouble("char_templates.f_col_r"));
				set.set("collision_height", rset.getDouble("char_templates.f_col_h"));
				ct = new L2PcTemplate(set);
				//5items must go here
				for (int x=1; x < 6 ;x++)
				{
					if (rset.getInt(34+x) != 0)
					{
						ct.addItem(rset.getInt("char_templates.items"+x));
					}
				}
				_templates.put(ct.classId.getId() | 0x100, ct);
			}
			
			rset.close();
			statement.close();
		}
		catch (SQLException e)
		{
			_log.warning("error while loading char templates "+e.getMessage());
		}
		finally
		{
			try { con.close(); } catch (Exception e) {}
		}

		_log.config("loaded " + _templates.size() + " char templates");
	}
	
	public L2PcTemplate getTemplate(ClassId classId, boolean female)
	{
		return getTemplate(classId.getId(), female);
	}
	
	public L2PcTemplate getTemplate(int classId, boolean female)
	{
		int key = classId;
		if (female)
			key |= 0x100;
		return _templates.get(key);
	}
	
//	public L2CharTemplate[] getAllTemplates()
//	{
//		return _templates.values().toArray(new L2CharTemplate[_templates.size()]);
//	}
}
