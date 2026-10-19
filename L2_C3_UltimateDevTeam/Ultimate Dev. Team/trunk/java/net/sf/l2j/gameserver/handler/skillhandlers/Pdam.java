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
import net.sf.l2j.gameserver.lib.Log;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2Skill.SkillType;
import net.sf.l2j.gameserver.model.actor.instance.L2NpcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2RaidBossInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2SummonInstance;
import net.sf.l2j.gameserver.packets.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.skills.Formulas;
import net.sf.l2j.gameserver.templates.L2WeaponType;

import java.util.logging.Logger;
/**
 * This class ...
 * 
 * @version $Revision: 1.1.2.7.2.16 $ $Date: 2005/04/06 16:13:49 $
 */

public class Pdam implements ISkillHandler
{
	// all the items ids that this handler knowns
	private static Logger _log = Logger.getLogger(Pdam.class.getName());
	
	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.handler.IItemHandler#useItem(net.sf.l2j.gameserver.model.L2PcInstance, net.sf.l2j.gameserver.model.L2ItemInstance)
	 */
	private static SkillType[] _skillIds = {
		SkillType.PDAM,
		/* SkillType.CHARGEDAM */
	};
	
	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.handler.IItemHandler#useItem(net.sf.l2j.gameserver.model.L2PcInstance, net.sf.l2j.gameserver.model.L2ItemInstance)
	 */
	public void useSkill(L2Character activeChar, L2Skill skill, L2Object[] targets)
	{
		if (activeChar.isAlikeDead())
			return;
		
		int damage = 0;
		

			if (Config.DEBUG) _log.fine("Begin Skill processing in Pdam.java " + skill.getSkillType());
		
        for(int index = 0;index < targets.length;index++)
        {
        	L2Character target = (L2Character)targets[index];
            L2ItemInstance weapon = activeChar.getActiveWeaponInstance();
		if (activeChar instanceof L2PcInstance && target instanceof L2PcInstance &&
		    target.isAlikeDead() && target.isFakeDeath())
		{
		    target.stopFakeDeath(null);
		}else if (target.isAlikeDead())
				continue;
			
			boolean dual  = activeChar.isUsingDualWeapon();
			boolean shld = Formulas.getInstance().calcShldUse(activeChar, target);
			boolean crit = Formulas.getInstance().calcCrit(activeChar.getCriticalHit(target, skill));
            boolean soul = (weapon!= null && weapon.getChargedSoulshot() == L2ItemInstance.CHARGED_SOULSHOT && weapon.getItemType() != L2WeaponType.DAGGER );
			
			if(!crit && (skill.getCondition() & L2Skill.COND_CRIT) != 0)
				damage = 0;
			else
				damage = (int)Formulas.getInstance().calcPhysDam(
					activeChar, target, skill, shld, crit, dual, soul);
			
			if (damage > 0)
			{
 		if (damage > 5000 && activeChar instanceof L2PcInstance)
 		{
 		    String name = "";
 		    if (target instanceof L2RaidBossInstance)
 			name = "RaidBoss ";
 		    if (target instanceof L2NpcInstance)
 			name += target.getName()+"("+((L2NpcInstance)target).getTemplate().npcId+")";
 		    if (target instanceof L2PcInstance)
 			name = target.getName()+"("+target.getObjectId()+") ";
 		    name += target.getLevel()+" lvl";
 		    Log.add(activeChar.getName()+"("+activeChar.getObjectId()+") "+activeChar.getLevel()+" lvl did damage "+damage+" with skill "+skill.getName()+"("+skill.getId()+") to "+name,"damage_pdam");
 		}
				target.reduceCurrentHp(damage, activeChar);
                if (soul && weapon!= null)
                    weapon.setChargedSoulshot(L2ItemInstance.CHARGED_NONE);                

                if (activeChar instanceof L2PcInstance)
                    activeChar.sendPacket(new SystemMessage(SystemMessage.YOU_DID_S1_DMG).addNumber(damage));


                if (activeChar instanceof L2SummonInstance)
                    ((L2SummonInstance)activeChar).getOwner().sendPacket(new SystemMessage(SystemMessage.SUMMON_GAVE_DAMAGE_OF_S1).addNumber(damage));

				// activate attacked effects, if any
				target.stopEffect(skill.getId());
	            if (target.getEffect(skill.getId()) != null)
	                target.removeEffect(target.getEffect(skill.getId()));
                if (Formulas.getInstance().calcSkillSuccess(activeChar, target, skill, false, false, false))
                    skill.getEffects(activeChar, target);
                else
                		activeChar.sendPacket(SystemMessage.sendString(skill.getName() + " effect failed."));
			}
			else
					activeChar.sendPacket(SystemMessage.sendString(skill.getName() + " failed."));
		}	
	}
	
	public SkillType[] getSkillIds()
	{
		return _skillIds;
	}
}
