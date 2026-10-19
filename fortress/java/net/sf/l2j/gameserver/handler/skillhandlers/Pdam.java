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

import net.sf.l2j.gameserver.handler.ISkillHandler;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2Summon;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2PcInstance;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2Skill.SkillType;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.skills.Formulas;
/**
 * This class ...
 * 
 * @version $Revision: 1.1.2.7.2.16 $ $Date: 2005/04/06 16:13:49 $
 */

public class Pdam implements ISkillHandler
{
	public static double DEATHLINK_PDAM_MULTIPLIER = 2;
	
	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.handler.IItemHandler#useItem(net.sf.l2j.gameserver.model.L2PcInstance, net.sf.l2j.gameserver.model.L2ItemInstance)
	 */
	private static SkillType[] _skillIds = {SkillType.PDAM, SkillType.DEATHLINK_PDAM};
	
	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.handler.IItemHandler#useItem(net.sf.l2j.gameserver.model.L2PcInstance, net.sf.l2j.gameserver.model.L2ItemInstance)
	 */
	public void useSkill(L2Character activeChar, L2Skill skill, L2Object[] targets)
	{
		if (activeChar.isAlikeDead())
			return;

		double damage = 0;
		double deathlinkMult = DEATHLINK_PDAM_MULTIPLIER * (1. - activeChar.getCurrentHp()/activeChar.getMaxHp());

		L2ItemInstance weaponInst = activeChar.getActiveWeaponInstance();
		boolean ss = (weaponInst != null && weaponInst.getChargedSoulshot() == L2ItemInstance.CHARGED_SOULSHOT);
		if(ss) weaponInst.setChargedSoulshot(L2ItemInstance.CHARGED_NONE);

		for(L2Character target : (L2Character[])targets)
        {
			if (target.isAlikeDead())
				continue;
			
			boolean shld = Formulas.getInstance().calcShldUse(activeChar, target);
			damage = Formulas.getInstance().calcPhysDam(activeChar, target, skill, shld, false, false, ss);
			
			if (skill.getSkillType() == SkillType.DEATHLINK_PDAM)
				damage *= deathlinkMult;

			target.reduceCurrentHp(damage, activeChar);

			if (activeChar instanceof L2PcInstance)
			{
				activeChar.sendPacket(new SystemMessage(SystemMessage.YOU_HIT_FOR_S1_DAMAGE).addNumber((int)damage));
			}
			else if (activeChar instanceof L2Summon)
            {
				((L2Summon)activeChar).getOwner().sendPacket(new SystemMessage(SystemMessage.THE_SUMMONED_MONSTER_GAVE_DAMAGE_OF_S1).addNumber((int)damage));
            }

			// activate attacked effects, if any
			target.stopEffect(skill.getId());
			if (target.getEffect(skill.getId()) != null)
				target.removeEffect(target.getEffect(skill.getId()));
			skill.getEffects(activeChar, target);
        }
	}
	
	public SkillType[] getSkillIds()
	{
		return _skillIds;
	}
}
