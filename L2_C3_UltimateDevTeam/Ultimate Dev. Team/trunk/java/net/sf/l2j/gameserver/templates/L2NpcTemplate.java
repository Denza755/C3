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

import javolution.util.FastList;
import javolution.util.FastMap;
import net.sf.l2j.gameserver.model.L2DropData;
import net.sf.l2j.gameserver.model.L2MinionData;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.base.ClassId;
import net.sf.l2j.gameserver.model.quest.Quest;
import net.sf.l2j.gameserver.skills.Stats;

import java.util.List;
import java.util.Map;

/**
 * This cl contains all generic data of a L2Spawn object.<BR><BR>
 * 
 * <B><U> Data</U> :</B><BR><BR>
 * <li>npcId, type, name, sex</li>
 * <li>revardExp, revardSp</li>
 * <li>aggroRange, factionId, factionRange</li>
 * <li>rhand, lhand, armor</li>
 * <li>isUndead</li>
 * <li>_drops</li>
 * <li>_minions</li>
 * <li>_teachInfo</li>
 * <li>_skills</li>
 * <li>_questsStart</li><BR><BR>
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
	public final boolean isUndead;
	
	private final StatsSet npcStatsSet;

	/** fixed skills*/
	public int     race;
	public double  rateHp;
	
	
	/** The table containing all Item that can be dropped by L2NpcInstance using this L2NpcTemplate*/
	private final List<L2DropData>    _drops       = new FastList<L2DropData>(0);
	
	/** The table containing all Minions that must be spawn with the L2NpcInstance using this L2NpcTemplate*/
	private final List<L2MinionData>  _minions     = new FastList<L2MinionData>(0);
	
	private List<ClassId>             _teachInfo;
	private Map<Integer, L2Skill> _skills;
	private Map<Stats, Integer> _resists;
	private Quest[]                   _questsStart;

	/**
	 * Constructor of L2Character.<BR><BR>
	 * 
	 * @param set The StatsSet object to transfert data to the method
	 * 
	 */
	public L2NpcTemplate(StatsSet set)
	{
		super(set);
		npcId     = set.getInteger("npcId");
		type      = set.getString("type");
		name      = set.getString("name");
		sex       = set.getString("sex");
		level     = set.getInteger("level");
		revardExp = set.getInteger("revardExp");
		revardSp  = set.getInteger("revardSp");
		aggroRange= set.getInteger("aggroRange");
		rhand     = set.getInteger("rhand");
		lhand     = set.getInteger("lhand");
		armor     = set.getInteger("armor");
		String f  = set.getString("factionId", null);
		if (f == null)
			factionId = null;
		else
			factionId = f.intern();
		factionRange  = set.getInteger("factionRange");
		isUndead      = (set.getInteger("isUndead", 0) == 1);
		//String r = set.getString("race", null);
		//if (r == null)
		//	race = null;
		//else
		//	race = r.intern();
		race = 0;
		rateHp = 1;
		npcStatsSet = set;
		_teachInfo = null;
	}
    
    public void addTeachInfo(ClassId classId)
	{
		if (_teachInfo == null)
			_teachInfo = new FastList<ClassId>();
		_teachInfo.add(classId);
	}
	
	public ClassId[] getTeachInfo()
	{
		if (_teachInfo == null)
			return null;
		return _teachInfo.toArray(new ClassId[_teachInfo.size()]);
	}
	public boolean  canTeach(ClassId classId)
	{
        return _teachInfo != null && _teachInfo.contains(classId);
    }
	
    public void addDropData(L2DropData drop)
	{
	    if (drop.isQuestDrop()) {
//			if (_questDrops == null)
//				_questDrops = new FastList<L2DropData>(0);
//	        _questDrops.add(drop);
	    } else {
	        _drops.add(drop);
	    }
	}
    
    public void addRaidData(L2MinionData minion)
    {
    	_minions.add(minion);
    }
	
    public void addSkill(L2Skill skill)
	{
		if (_skills == null)
			_skills = new FastMap<Integer, L2Skill>();
		_skills.put(skill.getId(), skill);
	}
    public void addResist(Stats id, int resist)
	{
		if (_resists == null)
			_resists = new FastMap<Stats, Integer>();
		_resists.put(id, resist);
	}
    public int getResist(Stats id)
	{
		return _resists.get(id);
	}
    public int removeResist(Stats id)
	{
		return _resists.remove(id);
	}
	
	/**
	 * Return the list of all possible drops of this L2NpcTemplate.<BR><BR>
	 */
	public List<L2DropData> getDropData()
	{
		return _drops;
	}
	
	/**
	 * Return the list of all Minions that must be spawn with the L2NpcInstance using this L2NpcTemplate.<BR><BR>
	 */
	public List<L2MinionData> getMinionData()
	{
		return _minions;
	}

    public Map<Integer, L2Skill> getSkills()
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
	
	public StatsSet getStatsSet()
	{
		return npcStatsSet;
	}
	public void setRace(int newrace)
	{
	    race = newrace;
	}
	public void setRateHp(double newrate)
	{
	    rateHp = newrate;
	}
	public String toString()
	{
		return "Npc template "+name+"["+npcId+"]";
	}
	public Integer getNpcId()
	{
		return npcId;
	}
}
