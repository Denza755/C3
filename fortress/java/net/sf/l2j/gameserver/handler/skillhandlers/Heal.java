/*
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2, or (at your option) any later version. This
 * program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place - Suite 330, Boston, MA 02111-1307, USA.
 * http://www.gnu.org/copyleft/gpl.html
 */
package net.sf.l2j.gameserver.handler.skillhandlers;

import net.sf.l2j.gameserver.handler.ISkillHandler;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2PcInstance;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2Skill.SkillType;
import net.sf.l2j.gameserver.serverpackets.StatusUpdate;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.skills.Stats;

/**
 * This class ...
 * 
 * @version $Revision: 1.1.2.2.2.4 $ $Date: 2005/04/06 16:13:48 $
 */

public class Heal implements ISkillHandler
{
    // all the items ids that this handler knowns
    // private static Logger _log = Logger.getLogger(Heal.class.getName());

    /*
     * (non-Javadoc)
     * 
     * @see net.sf.l2j.gameserver.handler.IItemHandler#useItem(net.sf.l2j.gameserver.model.L2PcInstance,
     *      net.sf.l2j.gameserver.model.L2ItemInstance)
     */
    private static SkillType[] _skillIds = { SkillType.HEAL };

    /*
     * (non-Javadoc)
     * 
     * @see net.sf.l2j.gameserver.handler.IItemHandler#useItem(net.sf.l2j.gameserver.model.L2PcInstance,
     *      net.sf.l2j.gameserver.model.L2ItemInstance)
     */
    public void useSkill(L2Character actChar, L2Skill skill, L2Object[] targets)
    {
        L2ItemInstance weaponInst = actChar.getActiveWeaponInstance();

        double hp = skill.getPower();

        if(weaponInst != null)
        {
            if(weaponInst.getChargedSpiritshot() == L2ItemInstance.CHARGED_BLESSED_SPIRITSHOT)
            {
                hp *= 1.5;
                weaponInst.setChargedSpiritshot(L2ItemInstance.CHARGED_NONE);
            }
            else if(weaponInst.getChargedSpiritshot() == L2ItemInstance.CHARGED_SPIRITSHOT)
            {
                hp *= 1.3;
                weaponInst.setChargedSpiritshot(L2ItemInstance.CHARGED_NONE);
            }
        }

        for(L2Character target : (L2Character[]) targets)
        {
            // We should not heal if char is dead
            if(target == null || target.isDead()) continue;

            target.setCurrentHp(hp
                    * target.calcStat(Stats.HEAL_EFFECTIVNESS, 100, null, null) / 100
                    + target.getCurrentHp());

            StatusUpdate suhp = new StatusUpdate(target.getObjectId());
            suhp.addAttribute(StatusUpdate.CUR_HP, (int)target.getCurrentHp());
            target.sendPacket(suhp);
            if(target instanceof L2PcInstance)
            {
                // AI attention for target's enemy
                ((L2PcInstance)actChar).onBuff((L2PcInstance)target, (int)hp);
                // $s1 HPs have been restored.
                SystemMessage sm2 = new SystemMessage(SystemMessage.S1_HPS_HAVE_BEEN_RESTORED).addNumber((int)hp);
                target.sendPacket(sm2);
                if(actChar != target) actChar.sendPacket(sm2);
            }
        }
    }

    public SkillType[] getSkillIds()
    {
        return _skillIds;
    }
}
