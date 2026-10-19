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
import net.sf.l2j.gameserver.ai.L2AttackableAI;
import net.sf.l2j.gameserver.ai.CtrlIntention;
import net.sf.l2j.gameserver.handler.ISkillHandler;
import net.sf.l2j.gameserver.model.L2Attackable;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2Summon;
import net.sf.l2j.gameserver.model.L2Skill.SkillType;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.skills.Formulas;
import net.sf.l2j.gameserver.skills.Stats;

/** 
 * This Handles Disabler skills
 * @author _drunk_ 
 */
public class Disablers implements ISkillHandler
{
    protected SkillType[] _skillIds = {L2Skill.SkillType.STUN, L2Skill.SkillType.ROOT,
                                       L2Skill.SkillType.SLEEP, L2Skill.SkillType.CONFUSION,
                                       L2Skill.SkillType.AGGDAMAGE, L2Skill.SkillType.AGGREDUCE,
                                       L2Skill.SkillType.AGGREDUCE_CHAR, L2Skill.SkillType.AGGREMOVE,
                                       L2Skill.SkillType.UNBLEED, L2Skill.SkillType.UNPOISON,
                                       L2Skill.SkillType.MUTE, L2Skill.SkillType.FAKE_DEATH,
                                       L2Skill.SkillType.CONFUSE_MOB_ONLY, L2Skill.SkillType.NEGATE,
                                       L2Skill.SkillType.CANCEL, L2Skill.SkillType.PARALYZE};

    public void useSkill(L2Character activeChar, L2Skill skill, L2Object[] targets)
    {
        SkillType type = skill.getSkillType();

        boolean ss = false;
        boolean sps = false;
        boolean bss = false;

        L2ItemInstance weaponInst = activeChar.getActiveWeaponInstance();

        if (activeChar instanceof L2PcInstance)
        {
            if (weaponInst == null)
            {
                SystemMessage sm2 = new SystemMessage(614);
                sm2.addString("You must equip a weapon before casting a spell.");
                activeChar.sendPacket(sm2);
                return;
            }
        }

        if (weaponInst != null)
        {
            if (weaponInst.getChargedSpiritshot() == L2ItemInstance.CHARGED_BLESSED_SPIRITSHOT)
            {
                bss = true;
                weaponInst.setChargedSpiritshot(L2ItemInstance.CHARGED_NONE);
            }
            else if (weaponInst.getChargedSpiritshot() == L2ItemInstance.CHARGED_SPIRITSHOT)
            {
                sps = true;
                weaponInst.setChargedSpiritshot(L2ItemInstance.CHARGED_NONE);
            }
            else if (weaponInst.getChargedSoulshot() == L2ItemInstance.CHARGED_SOULSHOT)
            {
                ss = true;
                weaponInst.setChargedSoulshot(L2ItemInstance.CHARGED_NONE);
            }
        }

        for (int index = 0; index < targets.length; index++)
        {
            // Get a target
            if (!(targets[index] instanceof L2Character)) continue;

            L2Character target = (L2Character) targets[index];

            switch (type)
            {
                case FAKE_DEATH:
                {
                    // stun/fakedeath is not mdef dependant, it depends on lvl difference, target CON and power of stun
                    skill.getEffects(activeChar, target);
                    break;
                }
                case STUN:
                {
                    if (Formulas.getInstance().calcSkillSuccess(activeChar, target, skill, ss, false,
                                                                false))
                    {
                        skill.getEffects(activeChar, target);
                    }
                    else
                    {
                        if (activeChar instanceof L2PcInstance)
                        {
                            SystemMessage sm = new SystemMessage(139);
                            sm.addString(target.getName());
                            sm.addSkillName(skill.getId());
                            activeChar.sendPacket(sm);
                        }
                    }
                    break;
                }
                case SLEEP:
                case ROOT:
                case PARALYZE: //use same as root for now
                {
                    if (Formulas.getInstance().calcSkillSuccess(activeChar, target, skill, false, sps,
                                                                bss))
                    {
                        skill.getEffects(activeChar, target);
                    }
                    else
                    {
                        if (activeChar instanceof L2PcInstance)
                        {
                            SystemMessage sm = new SystemMessage(139);
                            sm.addString(target.getName());
                            sm.addSkillName(skill.getId());
                            activeChar.sendPacket(sm);
                        }
                    }
                    break;
                }
                case CONFUSION:
                case MUTE:
                {
                    if (Formulas.getInstance().calcSkillSuccess(activeChar, target, skill, false, ss,
                                                                bss))
                    {
                        // stop same type effect if avaiable
                        L2Effect[] effects = target.getAllEffects();
                        for (L2Effect e : effects)
                        {
                            if (e.getSkill().getSkillType() == type) e.exit();
                        }
                        // then restart
                        // Make above skills mdef dependant	        		
                        if (Formulas.getInstance().calcSkillSuccess(activeChar, target, skill, false,
                                                                    sps, bss))
                        //if(Formulas.getInstance().calcMagicAffected(activeChar, target, skill))
                        {
                            skill.getEffects(activeChar, target);
                        }
                        else
                        {
                            if (activeChar instanceof L2PcInstance)
                            {
                                SystemMessage sm = new SystemMessage(139);
                                sm.addString(target.getName());
                                sm.addSkillName(skill.getId());
                                activeChar.sendPacket(sm);
                            }
                        }
                    }
                    else
                    {
                        if (activeChar instanceof L2PcInstance)
                        {
                            SystemMessage sm = new SystemMessage(139);
                            sm.addString(target.getName());
                            sm.addSkillName(skill.getId());
                            activeChar.sendPacket(sm);
                        }
                    }
                    break;
                }
                case CONFUSE_MOB_ONLY:
                {
                    // do nothing if not on mob
                    if (target instanceof L2Attackable) skill.getEffects(activeChar, target);
                    else activeChar.sendPacket(new SystemMessage(SystemMessage.TARGET_IS_INCORRECT));
                    break;
                }
				case AGGDAMAGE:
				{
					if(target instanceof L2Attackable)
						target.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, activeChar, (int) ((150 * skill.getPower()) / (target.getLevel() + 7)));
					//TODO [Nemesiss] should this have 100% chance?
					skill.getEffects(activeChar, target);
					break;
				}
                case AGGREDUCE:
                {
                	// these skills needs to be rechecked
                	if (target instanceof L2Attackable)
                    {
                    	skill.getEffects(activeChar, target);

                    	double aggdiff = ((L2Attackable)target).getHating(activeChar)
                    					   - target.calcStat(Stats.AGGRESSION, ((L2Attackable)target).getHating(activeChar), target, skill);

                    	if (skill.getPower() > 0)
                    		((L2Attackable)target).reduceHate(null, (int) skill.getPower());
                    	else if (aggdiff > 0)
                    		((L2Attackable)target).reduceHate(null, (int) aggdiff);
                    }
                    break;
                }
                case AGGREDUCE_CHAR:
                {
                	// these skills needs to be rechecked
                	if (Formulas.getInstance().calcSkillSuccess(activeChar, target, skill, ss, sps, bss))
                	{
                		if (target instanceof L2Attackable)
                		{
                    		L2Attackable targ = (L2Attackable)target;
                			targ.stopHating(activeChar);
                    		if (targ.getMostHated() == null)
                            {
                           		((L2AttackableAI)targ.getAI()).setGlobalAggro(-25);
                        		targ.clearAggroList();
                        		targ.getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
                        		targ.setWalking();
                            }
                        }
                    	skill.getEffects(activeChar, target);
                    }
                	else
                	{
                		if (activeChar instanceof L2PcInstance)
                		{
                			SystemMessage sm = new SystemMessage(SystemMessage.S1_WAS_UNAFFECTED_BY_S2);
                			sm.addString(target.getName());
                			sm.addSkillName(skill.getId());
                			activeChar.sendPacket(sm);
                		}
                	}
                    break;
                }
                case AGGREMOVE:
                {
                	// these skills needs to be rechecked
                	if (target instanceof L2Attackable && !target.isRaid())
                	{
                		if (Formulas.getInstance().calcSkillSuccess(activeChar, target, skill, ss, sps, bss))
                		{
                			if (skill.getTargetType() == L2Skill.SkillTargetType.TARGET_UNDEAD)
                			{
                				if(target.isUndead())
                					((L2Attackable)target).reduceHate(null, ((L2Attackable)target).getHating(((L2Attackable)target).getMostHated()));
                			}
                			else
                				((L2Attackable)target).reduceHate(null, ((L2Attackable)target).getHating(((L2Attackable)target).getMostHated()));
                		}
                		else
                		{
                			if (activeChar instanceof L2PcInstance)
                			{
                				SystemMessage sm = new SystemMessage(SystemMessage.S1_WAS_UNAFFECTED_BY_S2);
                				sm.addString(target.getName());
                				sm.addSkillName(skill.getId());
                				activeChar.sendPacket(sm);
                			}
                		}
                	}
                    break;
                }
                case UNBLEED:
                {
                    L2Effect[] effects = target.getAllEffects();
                    for (L2Effect e : effects)
                    {
                        if (e.getSkill().getSkillType() == SkillType.BLEED
                            && skill.getPower() >= e.getSkill().getPower())
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
                        if (e.getSkill().getSkillType() == SkillType.POISON
                            && skill.getPower() >= e.getSkill().getPower())
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
                            if (e.getSkill().getId() != 4082 && e.getSkill().getId() != 4215
                                && e.getSkill().getId() != 4515) // Cannot cancel skills 4082, 4215, 4515
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
                                if (e.getSkill().getSkillType() == SkillType.BLEED
                                    && e.getSkill().getLevel() == 1
                                    || e.getSkill().getSkillType() == SkillType.POISON
                                    && e.getSkill().getLevel() == 1
                                    || e.getSkill().getSkillType() == SkillType.PARALYZE
                                    && e.getSkill().getLevel() == 1)
                                {
                                    e.exit();
                                }
                            }
                            else
                            {
                                if (e.getSkill().getSkillType() == SkillType.BLEED
                                    || e.getSkill().getSkillType() == SkillType.POISON
                                    || e.getSkill().getSkillType() == SkillType.PARALYZE)
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
                                if (e.getSkill().getSkillType() == SkillType.BLEED
                                    && e.getSkill().getLevel() == 1
                                    || e.getSkill().getSkillType() == SkillType.POISON
                                    && e.getSkill().getLevel() == 1)
                                {
                                    e.exit();
                                }
                            }
                            else
                            {
                                if (e.getSkill().getSkillType() == SkillType.BLEED
                                    || e.getSkill().getSkillType() == SkillType.POISON)
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
    }

    public SkillType[] getSkillIds()
    {
        return _skillIds;
    }
}
