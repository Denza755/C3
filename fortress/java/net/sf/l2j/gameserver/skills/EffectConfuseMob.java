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

import java.util.ArrayList;

import net.sf.l2j.gameserver.ai.CtrlIntention;
import net.sf.l2j.gameserver.lib.Rnd;
import net.sf.l2j.gameserver.model.L2Attackable;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.model.L2Object;



/**
 * @author littlecrow
 *
 * Implementation of the Confusion Effect
 */
final class EffectConfuseMob extends L2Effect {

	public EffectConfuseMob(Env env, EffectTemplate template)
	{
		super(env, template);
	}

	public EffectType getEffectType()
	{
		return EffectType.CONFUSE_MOB_ONLY;
	}
	
	/** Notify started */
	public void onStart() {
		getEffected().startConfused();
		onActionTime();
	}
	
	/** Notify exited */
	public void onExit() {
		getEffected().stopConfused(this);
	}
	
    public boolean onActionTime()
    {
		ArrayList<L2Character> targetList = new ArrayList<L2Character>();
        
		// Getting the possible targets

        for(L2Object obj : getEffected().getKnownObjects(900))
        {
            if ((obj instanceof L2Attackable) && (obj != getEffected()))
                targetList.add((L2Character)obj);
        }
		// if there is no target, exit function
		if (targetList.size()==0)
		{
			return true;
		}
			
		// Choosing randomly a new target
		L2Object target = targetList.get(Rnd.get(targetList.size()));
		
		// Attacking the target
		//getEffected().setTarget(target);
		getEffected().setTarget(target);
		getEffected().setIntention(CtrlIntention.AI_INTENTION_ATTACK, target, null);
		
		
    	return true;
    }
}

