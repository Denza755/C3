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
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2Skill.SkillType;
import net.sf.l2j.gameserver.templates.StatsSet;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * @author mkizub
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
final class DocumentSkill extends DocumentBase {

	private int id;
	private String name;
	private StatsSet[] sets;
	private L2Skill[] skills;
	private int currentLevel;
	
	DocumentSkill(int id, String name, StatsSet[] sets, File file)
	{
		super(file);
		this.id = id;
		this.name = name;
		this.sets = sets;
	}
	protected StatsSet getStatsSet()
	{
		return sets[currentLevel];
	}
	protected L2Skill[] getSkills()
	{
		return skills;
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
			if ("skill".equalsIgnoreCase(n.getNodeName()))
				parseSkill(n);
		}
	}
	protected void parseSkill(Node n)
	{
		NamedNodeMap attrs = n.getAttributes();
		int skillId = Integer.parseInt(attrs.getNamedItem("id").getNodeValue());
		String skillName = attrs.getNamedItem("name").getNodeValue();
		String levels = attrs.getNamedItem("levels").getNodeValue();
		int lastLvl = Integer.parseInt(levels);
		
		if (id == 0)
			id = skillId;
		if (id != 0 && id != skillId)
			throw new RuntimeException("Skill id="+skillId+" missmatch, id="+id+" expected");
		
		if (name == null)
			name = skillName;
		if (!name.equals(skillName))
			if (Config.DEBUG) _log.fine("Skill id="+skillId+" name missmatch, name='"+name+"' expected");
		
		if (sets == null)
		{
			sets = new StatsSet[lastLvl];
			for (int i=0; i < lastLvl; i++)
			{
				sets[i] = new StatsSet();
				sets[i].set("skill_id", id);
				sets[i].set("level",    i+1);
				sets[i].set("name",     name);
			}
		}
		if (sets.length != lastLvl)
			throw new RuntimeException("Skill id="+skillId+" number of levels missmatch, "+lastLvl+" levels expected");
		
		Node first = n.getFirstChild();
		for (n=first; n != null; n = n.getNextSibling())
		{
			if ("table".equalsIgnoreCase(n.getNodeName()))
				parseTable(n);
		}
		for (int i=1; i <= lastLvl; i++)
		{
			for (n=first; n != null; n = n.getNextSibling())
			{
				if ("set".equalsIgnoreCase(n.getNodeName()))
					parseBeanSet(n, sets[i-1], i);
			}
		}
		makeSkills();
		for (int i=0; i < lastLvl; i++)
		{
			currentLevel = i;
			for (n=first; n != null; n = n.getNextSibling())
			{
				if ("cond".equalsIgnoreCase(n.getNodeName()))
				{
					Condition condition = parseCondition(n.getFirstChild(), skills[i]);
					Node msg = n.getAttributes().getNamedItem("msg");
					if (condition != null && msg != null)
						condition.setMessage(msg.getNodeValue());
					skills[i].attach(condition);
				}
				if ("for".equalsIgnoreCase(n.getNodeName()))
				{
					parseTemplate(n, skills[i]);
				}
			}
		}
	}
	
	private void makeSkills()
	{
		if (skills != null)
			return;
		skills = new L2Skill[sets.length];
		for (int i=0; i < sets.length; i++)
			skills[i] = sets[i].getEnum("skillType", SkillType.class).makeSkill(sets[i]);
	}
}
