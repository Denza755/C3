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
package net.sf.l2j.gameserver.handler.skillhandlers; 

import java.util.logging.Logger;

import net.sf.l2j.gameserver.ai.CtrlEvent;
import net.sf.l2j.gameserver.handler.ISkillHandler;
import net.sf.l2j.gameserver.model.L2Attackable;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2PcInstance;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2Skill.SkillType;

/** 
 * @author _drunk_ 
 * 
 * TODO To change the template for this generated type comment go to 
 * Window - Preferences - Java - Code Style - Code Templates 
 */ 
public class Disablers implements ISkillHandler 
{ 
    private static Logger _log = Logger.getLogger(Disablers.class.getName()); 
    protected SkillType[] _skillIds = {
			L2Skill.SkillType.STUN,
			L2Skill.SkillType.ROOT,
			L2Skill.SkillType.SLEEP,
			L2Skill.SkillType.CONFUSION,
			L2Skill.SkillType.AGGDAMAGE,
			L2Skill.SkillType.AGGREDUCE,
			L2Skill.SkillType.AGGREDUCE_CHAR,
			L2Skill.SkillType.AGGREMOVE,
			L2Skill.SkillType.UNBLEED,
			L2Skill.SkillType.UNPOISON,
			L2Skill.SkillType.MUTE,
 			L2Skill.SkillType.FAKE_DEATH,
 			L2Skill.SkillType.CONFUSE_MOB_ONLY
			}; 
    
    public void useSkill(L2Character activeChar, L2Skill skill, L2Object[] targets) 
    { 
        SkillType type = skill.getSkillType(); 
        
        for(int index = 0;index < targets.length; index++) 
        {
        	if (!(targets[index] instanceof L2Character))
        		continue;
        	L2Character target = (L2Character)targets[index];
        	switch (type)
        	{
        	case STUN:
        	case ROOT:
        	case SLEEP:
        	case CONFUSION:
        	case FAKE_DEATH:
        	case MUTE: {
        			// stop same type effect if avaiable
	        		L2Effect[] effects = target.getAllEffects();
	        		for (L2Effect e : effects) {
	        			if (e.getSkill().getSkillType() == type)
	        				e.exit();
	        		}
	        		// then restart
	        		skill.getEffects(activeChar, target);
	                if (skill.isOffensive() && activeChar instanceof L2PcInstance)
	                	((L2PcInstance)activeChar).checkPvP(target);	        		
	        		break;
	        	}
        	case CONFUSE_MOB_ONLY:
				// do nothing if not on mob
				if (target instanceof L2Attackable)
					skill.getEffects(activeChar,target);
        		break;
        	case AGGDAMAGE:
				if (target instanceof L2Attackable)
					target.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, activeChar, (int)skill.getPower());
        		break;
        	case AGGREDUCE:
				if (target instanceof L2Attackable)
					target.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, null, -(int)skill.getPower());
        		break;
        	case AGGREDUCE_CHAR:
				if (target instanceof L2Attackable)
					target.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, activeChar, -(int)skill.getPower());
        		break;
        	case AGGREMOVE:
				if (target instanceof L2Attackable)
					target.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, null, -(int)skill.getPower());
        		break;
        	case UNBLEED: {
					L2Effect[] effects = target.getAllEffects();
					for (L2Effect e : effects) {
						if (e.getSkill().getSkillType() == SkillType.BLEED &&
								skill.getPower() >= e.getSkill().getPower()) {
							e.exit();
							break;
						}
					}
				}
        		break;
        	case UNPOISON: {
				L2Effect[] effects = target.getAllEffects();
				for (L2Effect e : effects) {
					if (e.getSkill().getSkillType() == SkillType.POISON &&
							skill.getPower() >= e.getSkill().getPower()) {
						e.exit();
						break;
					}
				}
			}
    		break;
        	}
        }
    } 
    
    public SkillType[] getSkillIds() 
    { 
        return _skillIds; 
    } 
}
