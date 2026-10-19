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

import java.util.HashMap;

import ru.balancer.jbforth.Word;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.skills.SkillsEngine;
import net.sf.l2j.gameserver.templates.L2WeaponType;

public class SkillTable
{
	//private static Logger _log = Logger.getLogger(SkillTable.class.getName());
	private static SkillTable _instance;
	
	private HashMap<Integer, L2Skill> _skills;
	private boolean _initialized = true;

	public static HashMap<Integer,Word> skillStartHandlers = new HashMap<Integer,Word>();
	public static HashMap<Integer,Word> skillDoHandlers = new HashMap<Integer,Word>();

	public static SkillTable getInstance()
	{
		if (_instance == null)
		{
			_instance = new SkillTable();
		}
		return _instance;
	}

	private SkillTable()
	{
		_skills = new HashMap<Integer, L2Skill>();
		SkillsEngine.getInstance().loadAllSkills(_skills);
	}

    public void reload()
	{
		_instance = new SkillTable();
	}
    
	public boolean isInitialized()
	{
	    return _initialized;
	}
	
	public L2Skill getInfo(int magicId, int level)
	{
		return _skills.get(magicId*100 + level);
	}
	
	private static final L2WeaponType[] weaponDbMasks = {
		L2WeaponType.ETC,
		L2WeaponType.BOW,
		L2WeaponType.POLE,
		L2WeaponType.DUALFIST,
		L2WeaponType.DUAL,
		L2WeaponType.BLUNT,
		L2WeaponType.SWORD,
		L2WeaponType.DAGGER,
        L2WeaponType.BIGSWORD
		};
	
	public int calcWeaponsAllowed(int mask)
	{
		if (mask == 0)
			return 0;
		int weaponsAllowed = 0;
		for (int i=0; i < weaponDbMasks.length; i++)
		{
			if ((mask & (1<<i)) != 0)
				weaponsAllowed |= weaponDbMasks[i].mask();
		}
		return weaponsAllowed;
	}
}
