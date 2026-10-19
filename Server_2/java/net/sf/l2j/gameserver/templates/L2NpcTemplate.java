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
package net.sf.l2j.gameserver.templates;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.sf.l2j.gameserver.model.ClassId;
import net.sf.l2j.gameserver.model.L2DropData;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.quest.Quest;

/**
 * This class ...
 * 
 * @version $Revision: 1.1.2.4 $ $Date: 2005/04/02 15:57:51 $
 */
public final class L2NpcTemplate extends L2CharTemplate
{
	public final int     npcId;
	public final String  type;
	public final String  name;
	public final String  sex;
	public final int     level;
	public final int     revardExp;
	public final int     revardSp;
	public final int     aggroRange;
	public final int     rhand;
	public final int     lhand;
	public final int     armor;
	public final String  factionId;
	public final int     factionRange;
	private final List<L2DropData>    _drops      = new ArrayList<L2DropData>(0);
	private List<ClassId>             _teachInfo;
	private HashMap<Integer, L2Skill> _skills;
	private Quest[]                   _questsStart;

	public L2NpcTemplate(StatsSet set)
	{
		super(set);
		npcId     = set.getIntegr("npcId");
		type      = set.getString("type");
		name      = set.getString("name");
		sex       = set.getString("sex");
		level     = set.getIntegr("level");
		revardExp = set.getIntegr("revardExp");
		revardSp  = set.getIntegr("revardSp");
		aggroRange= set.getIntegr("aggroRange");
		rhand     = set.getIntegr("rhand");
		lhand     = set.getIntegr("lhand");
		armor     = set.getIntegr("armor");
		String f = set.getString("factionId", null);
		if (f == null)
			factionId = null;
		else
			factionId = f.intern();
		factionRange  = set.getIntegr("factionRange");
	}
    
    public void addTeachInfo(ClassId classId)
	{
		if (_teachInfo == null)
			_teachInfo = new ArrayList<ClassId>();
		_teachInfo.add(classId);
	}
	
	public ClassId[] getTeachInfo()
	{
		if (_teachInfo == null)
			return null;
		return _teachInfo.toArray(new ClassId[_teachInfo.size()]);
	}
	
    public void addDropData(L2DropData drop)
	{
	    if (drop.isQuestDrop()) {
//			if (_questDrops == null)
//				_questDrops = new ArrayList<L2DropData>(0);
//	        _questDrops.add(drop);
	    } else {
	        _drops.add(drop);
	    }
	}
	
    public void addSkill(L2Skill skill)
	{
		if (_skills == null)
			_skills = new HashMap<Integer, L2Skill>();
		_skills.put(skill.getId(), skill);
	}
	
	/**
	 * 
	 * @return list of L2DropData objects
	 */
	public List<L2DropData> getDropData()
	{
		return _drops;
	}

    public HashMap<Integer, L2Skill> getSkills()
	{
		return _skills;
	}
	    
	public void addStartQuests(Quest q) {
		if (_questsStart == null) {
			_questsStart = new Quest[]{q};
		} else {
			int len = _questsStart.length;
			Quest[] tmp = new Quest[len+1];
			for (int i=0; i < len; i++) {
				if (_questsStart[i].getName().equals(q.getName())) {
					_questsStart[i] = q;
					return;
	            }
				tmp[i] = _questsStart[i];
	        }
			tmp[len] = q;
			_questsStart = tmp;
	    }
	}
	
	public Quest[] getStartQuests() {
		return _questsStart;
	}
}
