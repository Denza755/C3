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
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2Skill.SkillType;
import net.sf.l2j.gameserver.serverpackets.StatusUpdate;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.skills.Formulas;
/**
 * This class ...
 * 
 * @version $Revision: 1.1.2.6.2.11 $ $Date: 2005/04/05 19:41:23 $
 */

public class Drain implements ISkillHandler
{
	//private static Logger _log = Logger.getLogger(Drain.class.getName());
	
	private static SkillType[] _skillIds = {/*SkillType.DRAIN*/};
	
	public void useSkill(L2Character activeChar, L2Skill skill, L2Object[] targets)
	{
		if (activeChar.isAlikeDead())
			return;
		
		boolean ss = false;
		boolean bss = false;
		
        for(int index = 0;index < targets.length;index++)
        {
			L2Character target = (L2Character)targets[index];
			if (target.isAlikeDead() && skill.getTargetType() != L2Skill.SkillTargetType.TARGET_CORPSE_MOB)
				continue;
			
			double hp = activeChar.getCurrentHp(); 
			int hpAdd = - skill.getHpConsume();

			L2ItemInstance weaponInst = activeChar.getActiveWeaponInstance();
			
			if (weaponInst == null)
			{
			}
			else if (weaponInst.getChargedSpiritshot() == L2ItemInstance.CHARGED_BLESSED_SPIRITSHOT) 
			{
				hpAdd *= 2;
				bss = true;
				weaponInst.setChargedSpiritshot(L2ItemInstance.CHARGED_NONE);
			}
            else if (weaponInst.getChargedSpiritshot() == L2ItemInstance.CHARGED_SPIRITSHOT) 
			{
				hpAdd *= 1.5;
				ss = true;
				weaponInst.setChargedSpiritshot(L2ItemInstance.CHARGED_NONE);
			}
            
			int damage = (int)Formulas.getInstance().calcMagicDam(
					activeChar, target, skill, ss, bss);

			if(skill.getId() == 70) // drain energy
				hp += hpAdd * damage;
			else
				hp += hpAdd;
			
			activeChar.setCurrentHp(hp); 
			StatusUpdate suhp = new StatusUpdate(activeChar.getObjectId()); 
			suhp.addAttribute(StatusUpdate.CUR_HP, (int)hp); 
			activeChar.sendPacket(suhp); 
			
			target.reduceCurrentHp(damage, activeChar);
			SystemMessage sm = new SystemMessage(SystemMessage.YOU_DID_S1_DMG);
			sm.addNumber(damage); 
			activeChar.sendPacket(sm); 
		}
	}
	
	public SkillType[] getSkillIds()
	{
		return _skillIds;
	}
}
