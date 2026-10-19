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
/**
 * This class ...
 * 
 * @version $Revision: 1.1.2.8.2.9 $ $Date: 2005/04/05 19:41:23 $
 */

public class Mdam implements ISkillHandler
{
	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.handler.IItemHandler#useItem(net.sf.l2j.gameserver.model.L2PcInstance, net.sf.l2j.gameserver.model.L2ItemInstance)
	 */
	private static SkillType[] _skillIds = {SkillType.MDAM, SkillType.DEATHLINK};
	
	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.handler.IItemHandler#useItem(net.sf.l2j.gameserver.model.L2PcInstance, net.sf.l2j.gameserver.model.L2ItemInstance)
	 */
	public void useSkill(L2Character activeChar, L2Skill skill, L2Object[] targets) {
        if (activeChar.isAlikeDead())
            return;

        boolean ss = false;
        boolean bss = false;
        double deathlinkMult = 2.5 * (1. - activeChar.getCurrentHp() / activeChar.getMaxHp());
        L2ItemInstance weaponInst = activeChar.getActiveWeaponInstance();

        if (activeChar instanceof L2PcInstance) {
            if (weaponInst == null) {
                activeChar.sendMessage("You must equip one weapon before cast spell.");
            }
        }
        if (weaponInst != null) {
            if (weaponInst.getChargedSpiritshot() == L2ItemInstance.CHARGED_BLESSED_SPIRITSHOT) {
                bss = true;
                weaponInst.setChargedSpiritshot(L2ItemInstance.CHARGED_NONE);
            } else if (weaponInst.getChargedSpiritshot() == L2ItemInstance.CHARGED_SPIRITSHOT) {
                ss = true;
                weaponInst.setChargedSpiritshot(L2ItemInstance.CHARGED_NONE);
            }
        }


        int damage = 0;
        L2Character target = null;
        for (int index = 0; index < targets.length; index++) {
            target = (L2Character) targets[index];
            if (activeChar instanceof L2PcInstance && target instanceof L2PcInstance &&
                    target.isAlikeDead() && target.isFakeDeath()) {
                target.stopFakeDeath(null);
            } else if (target.isAlikeDead())
                continue;
            boolean mcrit = Formulas.getInstance().calcMCrit(activeChar.getMCriticalHit(target, skill));

            damage = (int) Formulas.getInstance().calcMagicDam(
                    activeChar, target, skill, ss, bss, mcrit);

            if (skill.getSkillType() == SkillType.DEATHLINK)
                damage *= deathlinkMult;

            if (damage > 5000 && activeChar instanceof L2PcInstance) {
                String name = "";
                if (target instanceof L2RaidBossInstance)
                    name = "RaidBoss ";
                if (target instanceof L2NpcInstance)
                    name += target.getName() + "(" + ((L2NpcInstance) target).getTemplate().npcId + ")";
                if (target instanceof L2PcInstance)
                    name = target.getName() + "(" + target.getObjectId() + ") ";
                name += target.getLevel() + " lvl";
                Log.add(activeChar.getName() + "(" + activeChar.getObjectId() + ") " + activeChar.getLevel() + " lvl did damage " + damage + " with skill " + skill.getName() + "(" + skill.getId() + ") to " + name, "damage_mdam");
            }
            boolean abort = Formulas.getInstance().calcAtkBreak(target, target.getStat().getAtkCancel());
            if (abort) {
                target.breakAttack();
                target.breakCast();
            }
            // More damage to Boom Attack
            if (skill.getId() == 4139)
                damage *= 2.5;

            if (damage < 1)
                damage = 1;

            target.reduceCurrentHp(damage, activeChar);

            if (activeChar instanceof L2PcInstance) {
                if (mcrit) {
                    activeChar.sendPacket(SystemMessage.sendString("Magic Critical Hit!"));
                }
            }
            activeChar.sendPacket(new SystemMessage(SystemMessage.YOU_DID_S1_DMG).addNumber(damage));
        }

        if (activeChar instanceof L2SummonInstance)
            ((L2SummonInstance) activeChar).getOwner().sendPacket(new SystemMessage(SystemMessage.SUMMON_GAVE_DAMAGE_OF_S1).addNumber(damage));

        // activate attacked effects, if any
        target.stopEffect(skill.getId());
        if (target.getEffect(skill.getId()) != null)
            target.removeEffect(target.getEffect(skill.getId()));
        if (Formulas.getInstance().calcSkillSuccess(activeChar, target, skill, false, ss, bss))
            skill.getEffects(activeChar, target);
// When use this skill, boomber nust be die!            
        if ((skill.getId() == 4139) && (activeChar instanceof L2SummonInstance))
            activeChar.reduceCurrentHp(activeChar.getMaxHp() + 1, activeChar);
    }
	
	public SkillType[] getSkillIds()
	{
		return _skillIds;
	}
}
