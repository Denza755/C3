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

import net.sf.l2j.gameserver.ai.CtrlIntention;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.model.L2PcInstance;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;


class EffectRelax extends L2Effect
{		
	public EffectRelax(Env env, EffectTemplate template)
	{
		super(env, template);
	}
	
	public EffectType getEffectType()
	{
		return EffectType.RELAXING;
	}

	/** Notify started */
	public void onStart() {
		getEffected().getAI().setIntention(CtrlIntention.AI_INTENTION_REST);
	}
	
	public boolean onActionTime()
	{	
		if(getEffected().isDead())
			return false;
		if(!((L2PcInstance)getEffected()).isSitting())
			return false;
		
		if (getEffected().getCurrentHp()+1 > getEffected().getMaxHp()) {
			if(getSkill().isToggle())
			{
				SystemMessage sm = new SystemMessage(614);
				sm.addString("Fully rested. Effect of " + getSkill().getName() + " has been removed.");
				getEffected().sendPacket(sm);
				if (getEffected() instanceof L2PcInstance)
					((L2PcInstance)getEffected()).standUp();
				return false;
			}
		}
		
		double manaDam = calc();
		
		if(manaDam > getEffected().getCurrentMp())
		{
			if(getSkill().isToggle())
			{
				SystemMessage sm = new SystemMessage(614);
				sm.addString("Not enough mana. Effect of " + getSkill().getName() + " has been removed.");
				getEffected().sendPacket(sm);
				if (getEffected() instanceof L2PcInstance)
					((L2PcInstance)getEffected()).standUp();
				return false;
			}
		}
		
		getEffected().reduceCurrentMp(manaDam);
		return true;
	}
}
