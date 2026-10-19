package net.sf.l2j.gameserver.skills;

import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.serverpackets.StatusUpdate;
import net.sf.l2j.gameserver.templates.StatsSet;

public class L2SkillHealPercent extends L2Skill
{
    private float healPercent;

    public L2SkillHealPercent(StatsSet set)
    {
        super(set);
        healPercent = set.getFloat("power", 0.f);
    }

    public void useSkill(L2Character caster, L2Object[] targets)
    {
        if(caster.isAlikeDead()) return;
        L2ItemInstance weaponInst = caster.getActiveWeaponInstance();

        if(weaponInst != null)
            if(weaponInst.getChargedSpiritshot() == L2ItemInstance.CHARGED_BLESSED_SPIRITSHOT
                    || weaponInst.getChargedSpiritshot() == L2ItemInstance.CHARGED_SPIRITSHOT)
                weaponInst.setChargedSpiritshot(L2ItemInstance.CHARGED_NONE);

        for(L2Character target : (L2Character[])targets)
        {
            if(target.isAlikeDead()) continue;

            double hp = target.getCurrentHp() + target.getMaxHp() * healPercent / 100;

            // add effects
            target.stopEffect(this.getId());
            if(target.getEffect(this.getId()) != null)
                target.removeEffect(target.getEffect(this.getId()));
            this.getEffects(caster, target);

            target.setCurrentHp(hp);

            StatusUpdate suhp = new StatusUpdate(target.getObjectId());
            suhp.addAttribute(StatusUpdate.CUR_HP, (int)hp);
            suhp.addAttribute(StatusUpdate.MAX_HP, (int)target.getMaxHp());
            target.sendPacket(suhp);
            target.broadcastUserInfo();
        }
    }

}
