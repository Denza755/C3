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
import net.sf.l2j.gameserver.skills.Stats;
import net.sf.l2j.Config;

/**
 * This class ...
 *
 * @version $Revision: 1.1.2.2.2.1 $ $Date: 2005/03/02 15:38:36 $
 */

public class ManaHeal implements ISkillHandler
{
	//private static Logger _log = Logger.getLogger(ManaHeal.class.getName());

	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.handler.IItemHandler#useItem(net.sf.l2j.gameserver.model.L2PcInstance, net.sf.l2j.gameserver.model.L2ItemInstance)
	 */
	private static SkillType[] _skillIds = {SkillType.MANAHEAL};

	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.handler.IItemHandler#useItem(net.sf.l2j.gameserver.model.L2PcInstance, net.sf.l2j.gameserver.model.L2ItemInstance)
	 */
	public void useSkill(L2Character activeChar, L2Skill skill, L2Object[] targets)
	{
        for(L2Character target : (L2Character[])targets)
        {
			double mp = skill.getPower()*target.calcStat(Stats.MANAHEAL_EFFECTIVNESS,100,null,null)/100;
			//int cLev = activeChar.getLevel();
			//hp += skill.getPower()/*+(Math.sqrt(cLev)*cLev)+cLev*/;

			L2ItemInstance weaponInst = activeChar.getActiveWeaponInstance();
			if (Config.MANAHEAL_SPS_BONUS && weaponInst != null)
				{
					if (weaponInst.getChargedSpiritshot() == L2ItemInstance.CHARGED_BLESSED_SPIRITSHOT)
						{
							mp*=1.5;
							weaponInst.setChargedSpiritshot(L2ItemInstance.CHARGED_NONE);
						}
					else if (weaponInst.getChargedSpiritshot() == L2ItemInstance.CHARGED_SPIRITSHOT)
						{
							mp*=1.3;
							weaponInst.setChargedSpiritshot(L2ItemInstance.CHARGED_NONE);
						}
				}


			target.setCurrentMp(mp+target.getCurrentMp());
			StatusUpdate sump = new StatusUpdate(target.getObjectId());
			sump.addAttribute(StatusUpdate.CUR_MP, (int)target.getCurrentMp());
			target.sendPacket(sump);
			SystemMessage sm = new SystemMessage(SystemMessage.S1_MPS_HAVE_BEEN_RESTORED).addNumber((int)mp);
			target.sendPacket(sm);
			if (activeChar != target)
					activeChar.sendPacket(sm);
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
