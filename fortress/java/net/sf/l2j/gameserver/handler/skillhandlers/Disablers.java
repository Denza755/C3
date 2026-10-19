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

import net.sf.l2j.gameserver.ai.CtrlEvent;
import net.sf.l2j.gameserver.handler.ISkillHandler;
import net.sf.l2j.gameserver.model.L2Attackable;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2PcInstance;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2Skill.SkillType;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.skills.Formulas;

/** 
 * This Handles Disabler skills
 * @author _drunk_ 
 */ 
public class Disablers implements ISkillHandler 
{ 
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
 			L2Skill.SkillType.CONFUSE_MOB_ONLY,
 			L2Skill.SkillType.NEGATE,
 			L2Skill.SkillType.CANCEL,
	        L2Skill.SkillType.PARALYZE		
	}; 

    public void useSkill(L2Character activeChar, L2Skill skill, L2Object[] targets) 
    { 
        SkillType type = skill.getSkillType(); 
        
		for(L2Character target : (L2Character[])targets)
        {
        	switch (type)
        	{
        	case FAKE_DEATH:
        	case STUN:
        	{
        		// stun/fakedeath is not mdef dependant, it depends on lvl difference, target CON and power of stun
        		skill.getEffects(activeChar, target);
        		break;
        	}
        	case ROOT:
        	case PARALYZE: //use same as root for now
        	{
        		if (Formulas.getInstance().calcSkillSuccess(activeChar, target, skill))
        		{
        			skill.getEffects(activeChar, target);
        		}
        		else
        		{
        			SystemMessage sm = new SystemMessage(614);
        			sm.addString(skill.getName() + " failed."); 
        			if (activeChar instanceof L2PcInstance)
        				activeChar.sendPacket(sm);
        		}
        		break;
        	}
        	case SLEEP:
        	{
        		if (Formulas.getInstance().calcSkillSuccess(activeChar, target, skill))
        		{
        			skill.getEffects(activeChar, target);
        		}
        		else
        		{
        			SystemMessage sm = new SystemMessage(614);
        			sm.addString(skill.getName() + " failed."); 
        			if (activeChar instanceof L2PcInstance)
        				activeChar.sendPacket(sm);
        		}
        		break;
        	}
        	case CONFUSION:
        	case MUTE:
        	{
    			// stop same type effect if avaiable
        		L2Effect[] effects = target.getAllEffects();
        		for (L2Effect e : effects)
        		{
        			if (e.getSkill().getSkillType() == type)
        				e.exit();
        		}
        		// then restart
        		// Make above skills mdef dependant	        		
        		if(Formulas.getInstance().calcSkillSuccess(activeChar, target, skill))
        		{
        			skill.getEffects(activeChar, target);
        		}
        		else
        		{
        			SystemMessage sm = new SystemMessage(614);
        			sm.addString(skill.getName() + " failed.");
        			if (activeChar instanceof L2PcInstance)
        				activeChar.sendPacket(sm);
        		}
        		break;
        	}
        	case CONFUSE_MOB_ONLY:
        	{
				// do nothing if not on mob
				if (target instanceof L2Attackable)
					skill.getEffects(activeChar,target);
        		break;
        	}
        	case AGGDAMAGE:
        	{
				if (target instanceof L2Attackable)
					target.notifyEvent(CtrlEvent.EVT_AGGRESSION, activeChar, (int)skill.getPower());
        		break;
        	}
        	case AGGREDUCE:
        	{
				if (target instanceof L2Attackable)
					target.notifyEvent(CtrlEvent.EVT_AGGRESSION, null, -(int)skill.getPower());
        		break;
        	}
        	case AGGREDUCE_CHAR:
        	{
				if (target instanceof L2Attackable)
					target.notifyEvent(CtrlEvent.EVT_AGGRESSION, activeChar, -(int)skill.getPower());
        		break;
        	}
        	case AGGREMOVE:
        	{
        		// 1034 = repose, 1049 = requiem
        		//if (skill.getId() == 1034 || skill.getId() == 1049)
                if ((skill.getTargetType() == L2Skill.SkillTargetType.TARGET_UNDEAD && target.isUndead()) ||
                        target.isAttackable())
        		{
        			target.notifyEvent(CtrlEvent.EVT_AGGRESSION, null, -(int)skill.getPower());
                }
                break;
        	}
        	case UNBLEED:
        	{
				L2Effect[] effects = target.getAllEffects();
				for (L2Effect e : effects)
				{
					if (e.getSkill().getSkillType() == SkillType.BLEED &&
							skill.getPower() >= e.getSkill().getPower())
					{
						e.exit();
						break;
					}
				}
        		break;
        	}
        	case UNPOISON:
        	{
				L2Effect[] effects = target.getAllEffects();
				for (L2Effect e : effects)
				{
					if (e.getSkill().getSkillType() == SkillType.POISON &&
							skill.getPower() >= e.getSkill().getPower()) 
					{
						e.exit();
						break;
					}
				}
				break;
        	}
        	case CANCEL:
        	case NEGATE:
        	{
    		    // cancel
				if (skill.getId() == 1056)
				{
					L2Effect[] effects = target.getAllEffects();
					for (L2Effect e : effects)
					{
						e.exit();
					}
					break;
				}
    			// purify
				else if (skill.getId() == 1018)
				{
					L2Effect[] effects = target.getAllEffects();
					for (L2Effect e : effects)
					{
						if (skill.getLevel() == 1)
						{
							if (e.getSkill().getSkillType() == SkillType.BLEED && e.getSkill().getLevel() == 1 ||
									e.getSkill().getSkillType() == SkillType.POISON && e.getSkill().getLevel() == 1 ||
									e.getSkill().getSkillType() == SkillType.PARALYZE && e.getSkill().getLevel() == 1)
							{
								e.exit();
							}
						}
						else
						{
							if (e.getSkill().getSkillType() == SkillType.BLEED ||
									e.getSkill().getSkillType() == SkillType.POISON ||
									e.getSkill().getSkillType() == SkillType.PARALYZE)
							{
								e.exit();
							}
						}
					}
				}
    			// vitalize
				else if (skill.getId() == 1020)
				{
					L2Effect[] effects = target.getAllEffects();
					for (L2Effect e : effects)
					{
						if (skill.getLevel() < 6)
						{
							if (e.getSkill().getSkillType() == SkillType.BLEED && e.getSkill().getLevel() == 1 ||
									e.getSkill().getSkillType() == SkillType.POISON && e.getSkill().getLevel() == 1)
							{
								e.exit();
							}
						}
						else
						{
							if (e.getSkill().getSkillType() == SkillType.BLEED ||
									e.getSkill().getSkillType() == SkillType.POISON)
							{
								e.exit();
							}
							break;
						}
					} 
				}
        	}
        	}
        }

		L2ItemInstance weaponInst = activeChar.getActiveWeaponInstance();
		if (weaponInst != null && weaponInst.getChargedSpiritshot() != L2ItemInstance.CHARGED_NONE)
			weaponInst.setChargedSpiritshot(L2ItemInstance.CHARGED_NONE);
    }
    
    public SkillType[] getSkillIds() 
    { 
        return _skillIds; 
    } 
}
