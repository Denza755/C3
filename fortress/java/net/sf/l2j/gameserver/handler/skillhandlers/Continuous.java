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

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.handler.ISkillHandler;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2Skill.SkillType;
import net.sf.l2j.gameserver.skills.Formulas;

/**
 * This class ...
 * 
 * @version $Revision: 1.1.2.2.2.9 $ $Date: 2005/04/03 15:55:04 $
 */

public class Continuous implements ISkillHandler
{
	//private static Logger _log = Logger.getLogger(Continuous.class.getName());
	
	private static SkillType[] _skillIds = {
		L2Skill.SkillType.BUFF,
		L2Skill.SkillType.DEBUFF,
		L2Skill.SkillType.DOT,
		L2Skill.SkillType.MDOT,
		L2Skill.SkillType.POISON,
		L2Skill.SkillType.BLEED,
		L2Skill.SkillType.HOT,
    L2Skill.SkillType.CPHOT,
    L2Skill.SkillType.MPHOT,
		//L2Skill.SkillType.MANAHEAL,
		//L2Skill.SkillType.MANA_BY_LEVEL,
		L2Skill.SkillType.FEAR,
		L2Skill.SkillType.CONT,
		L2Skill.SkillType.WEAKNESS,
		L2Skill.SkillType.REFLECT,
        L2Skill.SkillType.UNDEAD_DEFENSE
		};
	
	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.handler.IItemHandler#useItem(net.sf.l2j.gameserver.model.L2PcInstance, net.sf.l2j.gameserver.model.L2ItemInstance)
	 */
	public void useSkill(L2Character activeChar, L2Skill skill, L2Object[] targets)
	{
		L2Character target = null;
		
		for(int index = 0;index < targets.length;index++)
        {
            target = (L2Character)targets[index];
			if (skill.isOffensive())
			{
				if (!Formulas.getInstance().calcSkillSuccess(activeChar, target, skill)) {
					activeChar.sendMessage(skill.getName()+" failed.");
					continue;
				}
			}
			boolean stopped = false;
			L2Effect[] effects = target.getAllEffects();
			if (effects != null)
			{
				for (L2Effect e : effects) {
					if (e.getSkill().getId() == skill.getId()) {
						e.exit();
						stopped = true;
					}
				}
			}
			if (skill.isToggle() && stopped)
				return;
            skill.getEffects(activeChar, target);
		}

		if (!skill.isMagic())
			return;
		if (skill.isToggle())
			return;
		if (skill.getId()>2000)
			return;
		if (Config.SAVING_SPS && skill.getSkillType() == SkillType.BUFF)
			return;

		L2ItemInstance weaponInst = activeChar.getActiveWeaponInstance();
		if (weaponInst != null && weaponInst.getChargedSpiritshot() != L2ItemInstance.CHARGED_NONE)
			weaponInst.setChargedSpiritshot(L2ItemInstance.CHARGED_NONE);
	}
	
	public SkillType[] getSkillIds()
	{
		return _skillIds;
	}
}
