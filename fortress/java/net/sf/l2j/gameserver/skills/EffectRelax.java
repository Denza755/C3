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
        setRelax(true);
		getEffected().setIntention(CtrlIntention.AI_INTENTION_REST, null, null);
		super.onStart();
	}
	
	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.model.L2Effect#onExit()
	 */
	@Override
	public void onExit() {
        setRelax(false);
		super.onExit();
	}

	public boolean onActionTime()
	{
        boolean retval = true;
		if(getEffected().isDead())
            retval = false;
        
		if(!((L2PcInstance)getEffected()).isSitting())
            retval = false;
		
		if (getEffected().getCurrentHp() >= getEffected().getMaxHp()) {
			if(getSkill().isToggle())
			{
				getEffected().sendPacket(new SystemMessage(SystemMessage.HP_WAS_FULLY_RECOVERED_AND_SKILL_WAS_REMOVED));
                retval = false;
			}
		}
		
		double manaDam = calc();
		
		if(manaDam > getEffected().getCurrentMp())
		{
			if(getSkill().isToggle())
			{
				getEffected().sendPacket(new SystemMessage(SystemMessage.NOT_ENOUGH_MP));
				getEffected().sendPacket(new SystemMessage(SystemMessage.THE_EFFECT_OF_S1_HAS_BEEN_REMOVED).addString(getSkill().getName()));
                retval = false;
			}
		}
		
        if (!retval)
            setRelax(retval);
        else
            getEffected().reduceCurrentMp(manaDam);
        
        return retval;
	}
    
    private void setRelax(boolean val)
    {
        ((L2PcInstance)getEffected()).setRelax(val);
    }
}
