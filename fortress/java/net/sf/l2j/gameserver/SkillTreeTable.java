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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.model.ClassId;
import net.sf.l2j.gameserver.model.L2PcInstance;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2SkillLearn;

/**
 * This class ...
 * 
 * @version $Revision: 1.13.2.2.2.8 $ $Date: 2005/04/06 16:13:25 $
 */
public class SkillTreeTable
{
	private static Logger _log = Logger.getLogger(SkillTreeTable.class.getName());
	private static final SkillTreeTable _instance = new SkillTreeTable();
	
	private HashMap<ClassId, ArrayList<L2SkillLearn>> _skillTrees;
	
	public static SkillTreeTable getInstance()
	{
		return _instance;
	}
	
	/**
	 * Return the minimum level needed to have this Expertise.<BR><BR>
	 * 
	 * @param grade The grade level searched
	 * 
	 */
	public static int getExpertiseLevel(int grade) 
	{
		if (grade <= 0)
			return 0;
		
		ArrayList<L2SkillLearn> lst = _instance._skillTrees.get(ClassId.paladin);
		
		for (L2SkillLearn sl : lst) 
		{
			if (sl.getId() == 239 && sl.getLevel() == grade)
				return sl.getMinLevel();
		}
		
		throw new Error("Expertise not found for grade "+grade);
	}
        
    public static int getMinSkillLevel(int skillID, ClassId classID, int skillLVL) {
    	if ( skillID > 0 && classID instanceof ClassId && skillLVL > 0 ) {

    		ArrayList<L2SkillLearn> lst = _instance._skillTrees.get(classID); 
			for (L2SkillLearn sl : lst) {
				if ( sl.getLevel() == skillLVL && sl.getId() == skillID ) return sl.getMinLevel();
			}
    	}
    	return 0;
    }
	
	private SkillTreeTable()
	{
		_skillTrees = new HashMap<ClassId, ArrayList<L2SkillLearn>>();
		int classId = 0;
        int count   = 0;
		java.sql.Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("SELECT * FROM class_list ORDER BY id");
			ResultSet classlist = statement.executeQuery();
			ArrayList<L2SkillLearn> list = new ArrayList<L2SkillLearn>();
			int parentClassId;
			L2SkillLearn skill;
			while (classlist.next())
			{
				list = new ArrayList<L2SkillLearn>();
				parentClassId = classlist.getInt("parent_id");
				classId = classlist.getInt("id");
				PreparedStatement statement2 = con.prepareStatement("SELECT class_id, skill_id, level, name, sp, min_level FROM skill_trees where class_id=? ORDER BY skill_id, level");
				statement2.setInt(1, classId);
				ResultSet skilltree = statement2.executeQuery();

				if (parentClassId != -1)
				{
					ArrayList<L2SkillLearn> parentList = _skillTrees.get(ClassId.values()[parentClassId]);
					list.addAll(parentList);
				}
				
				int prev_skill_id = -1;
				while (skilltree.next())
				{
					int id = skilltree.getInt("skill_id");
					int lvl = skilltree.getInt("level");
					String name = skilltree.getString("name");
					int minLvl = skilltree.getInt("min_level");
					int cost = skilltree.getInt("sp");
					if (prev_skill_id != id)
					{
						prev_skill_id = id;
					}
					skill = new L2SkillLearn(id, lvl, minLvl, name, cost);
					list.add(skill);
				}
				_skillTrees.put(ClassId.values()[classId], list);
				skilltree.close();
				statement2.close();
                count   += list.size();
				_log.fine("SkillTreeTable: skill tree for class " + classId + " has " + list.size() + " skills");		
			}
			
			classlist.close();
			statement.close();
		}
		catch (Exception e)
		{
			_log.log(Level.SEVERE, "error while creating skill tree for classId "+classId, e);
		} 
		finally 
		{
			try { con.close(); } catch (Exception e) {}
		}
        
        _log.config("SkillTreeTable: Loaded " + count + " skills.");
	}
	
	public L2SkillLearn[] getAvailableSkills(L2PcInstance cha, ClassId classId)
	{
		ArrayList<L2SkillLearn> result = new ArrayList<L2SkillLearn>();
		ArrayList<L2SkillLearn> skills = _skillTrees.get(classId);
		if (skills == null)
		{
			// the skilltree for this class is undefined, so we give an empty list
			_log.warning("Skilltree for class " + classId + " is not defined !");
			return new L2SkillLearn[0];
		}
		
		L2Skill[] oldSkills = cha.getAllSkills();
		
		for (int i = 0; i < skills.size(); i++)
		{
			L2SkillLearn temp = skills.get(i);
			if (temp.getMinLevel() <= cha.getLevel())
			{
				boolean knownSkill = false;
				
				for (int j = 0; j < oldSkills.length && !knownSkill; j++)
				{
					if (oldSkills[j].getId() == temp.getId() )
					{
						knownSkill = true;
						
						if ( oldSkills[j].getLevel() == temp.getLevel()-1)
						{
							// this is the next level of a skill that we know
							result.add(temp);
						}
					}
				}
				
				if (!knownSkill && temp.getLevel() == 1)
				{
					// this is a new skill
					result.add(temp);
				}
			}
		}
		
		return result.toArray(new L2SkillLearn[result.size()]);
	}
	public int getMinLevelForNewSkill(L2PcInstance cha, ClassId classId)
	{
		int minlevel=0;
		//ArrayList<L2SkillLearn> result = new ArrayList<L2SkillLearn>();
		ArrayList<L2SkillLearn> skills = _skillTrees.get(classId);
		if (skills == null)
		{
			// the skilltree for this class is undefined, so we give an empty list
			_log.warning("Skilltree for class " + classId + " is not defined !");
			return minlevel;
		}
		
		//L2Skill[] oldSkills = cha.getAllSkills();
		
		for (int i = 0; i < skills.size(); i++)
		{
			L2SkillLearn temp = skills.get(i);
			if (temp.getMinLevel() > cha.getLevel())
			    if (minlevel==0 || temp.getMinLevel()<minlevel)
				minlevel = temp.getMinLevel();
		}
		return minlevel;
	}
	
	public int getSkillCost(L2PcInstance player, L2Skill skill)
	{
		int min = 100000000;
		int lvl = player.getSkillLevel(skill.getId());
		if (lvl > 0)
			lvl += 1;
		else
			lvl = 1;
		for (ClassId classId : _skillTrees.keySet())
		{
			if (classId.level() != player.getClassId().level())
				continue;
			ArrayList<L2SkillLearn> lst = _skillTrees.get(classId);
			for (L2SkillLearn sl : lst)
			{
				if (sl.getId() != skill.getId())
					continue;
				if (sl.getLevel() != lvl)
					continue;
				if (sl.getMinLevel() > player.getLevel())
					continue;
				float cost = sl.getSpCost();
				if (!player.getClassId().equalsOrChildOf(classId))
				{
					if (skill.getCrossLearnAdd() < 0)
						continue;
					cost += skill.getCrossLearnAdd();
					cost *= skill.getCrossLearnMul();
				}
				if (classId.getRace() != player.getRace())
					cost *= skill.getCrossLearnRace();
				if (classId.isMage() != player.getClassId().isMage())
					cost *= skill.getCrossLearnProf();
				min = Math.min(min, Math.round(cost));
			}
		}
		return min;
	}
}
