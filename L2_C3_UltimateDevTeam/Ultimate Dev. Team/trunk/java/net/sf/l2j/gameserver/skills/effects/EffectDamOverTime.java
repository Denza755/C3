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
package net.sf.l2j.gameserver.skills.effects;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.model.L2Attackable;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.model.L2Skill.SkillTargetType;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.skills.Env;

class EffectDamOverTime extends L2Effect
{		
	public EffectDamOverTime(Env env, EffectTemplate template)
	{
		super(env, template);
	}
	
	public EffectType getEffectType()
	{
		return EffectType.DMG_OVER_TIME;
	}

	public boolean onActionTime()
	{	
		if (getEffected().isDead())
			return false;
		
		double damage = calc();
        
		if (damage >= getEffected().getCurrentHp())
		{
			if (getSkill().isToggle())
			{
				getEffected().sendMessage("Not enough HP. Effect of " + getSkill().getName() + " has been removed.");
			}
            
            // ** This is just hotfix, needs better solution **
            // 1947: "DOT skills shouldn't kill"
            // Well, some of them should ;-)
            if (getSkill().getId() != 4082 && !Config.DOT_KILL) damage = getEffected().getCurrentHp() - 1;
		}
		if (getSkill().getTargetType() != SkillTargetType.TARGET_SELF)
		{
			if (getEffector() instanceof L2PcInstance)
			{
				getEffector().sendMessage("Your "+getSkill().getName()+" did " + damage + " damage to "+getEffected().getName());
			}
			if (getEffected() instanceof L2PcInstance)
			{
				getEffected().sendMessage(getEffector().getName()+"'s "+getSkill().getName()+" did " + damage + " damage to you.");
			}
		}
		if (getEffected() instanceof L2Attackable)		
			getEffected().reduceCurrentHp(damage, getEffector(), false);
		else
			getEffected().reduceCurrentHp(damage, getEffector(), true);
		
		return true;
	}
}
