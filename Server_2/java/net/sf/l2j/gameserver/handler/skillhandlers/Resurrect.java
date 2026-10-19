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

import net.sf.l2j.gameserver.handler.ISkillHandler;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2Skill.SkillType;
import net.sf.l2j.gameserver.serverpackets.Revive;
import net.sf.l2j.gameserver.serverpackets.SocialAction;
/**
 * This class ...
 * 
 * @version $Revision: 1.1.2.5.2.4 $ $Date: 2005/04/03 15:55:03 $
 */

public class Resurrect implements ISkillHandler
{
	private static Logger _log = Logger.getLogger(Resurrect.class.getName());
	
	private static SkillType[] _skillIds = {SkillType.RESURRECT};
	
	public void useSkill(L2Character activeChar, L2Skill skill, L2Object[] targets)
	{
		L2Character target = null;
		
		for(int index = 0;index < targets.length;index++)
		{
			target = (L2Character) targets[index];

			target.setCurrentHpMp(0.6 * target.getMaxHp(), 0.6 * target.getMaxMp()); 
			Revive revive = new Revive(target); 
			SocialAction sa = new SocialAction(target.getObjectId(), 15); 
			target.broadcastPacket(sa); 
			target.sendPacket(sa); 
			target.sendPacket(revive); 
			target.broadcastPacket(revive); 
		}
	}
	
	
	public SkillType[] getSkillIds()
	{
		return _skillIds;
	}
}
