package net.sf.l2j.gameserver.skills.skills;

import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.actor.instance.L2NpcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.packets.serverpackets.StatusUpdate;
import net.sf.l2j.gameserver.packets.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.templates.StatsSet;
import net.sf.l2j.gameserver.skills.Formulas;

public class L2SkillDrain extends L2Skill {

	private float absorbPart;
	private int   absorbAbs;
	
	public L2SkillDrain(StatsSet set) {
		super(set);
		
		absorbPart = set.getFloat ("absorbPart", 0.f);
		absorbAbs  = set.getInteger("absorbAbs", 0);
	}

	public void useSkill(L2Character caster, L2Object[] targets)
    {
		if (caster.isAlikeDead())
			return;
		
		boolean ss = false;
		boolean bss = false;
		
        for(int index = 0;index < targets.length;index++)
        {
			L2Character target = (L2Character)targets[index];
			if (target.isAlikeDead() && getTargetType() != SkillTargetType.TARGET_CORPSE_MOB)
				continue;

            if (target instanceof L2PcInstance && caster != target && ((L2PcInstance)target).isInvul())
                continue; // No effect on invulnerable players unless they cast it themselves.

			L2ItemInstance weaponInst = caster.getActiveWeaponInstance();
			
			if (weaponInst == null)
			{
			}
			else if (weaponInst.getChargedSpiritshot() == L2ItemInstance.CHARGED_BLESSED_SPIRITSHOT) 
			{
				bss = true;
				weaponInst.setChargedSpiritshot(L2ItemInstance.CHARGED_NONE);
			}
            else if (weaponInst.getChargedSpiritshot() == L2ItemInstance.CHARGED_SPIRITSHOT) 
			{
				ss = true;
				weaponInst.setChargedSpiritshot(L2ItemInstance.CHARGED_NONE);
			}

			boolean mcrit = Formulas.getInstance().calcMCrit(caster.getMCriticalHit(target, this));
			int damage = (int)Formulas.getInstance().calcMagicDam(
					caster, target, this, ss, bss, mcrit);

			double hpAdd = absorbAbs + absorbPart * damage;
			double hp = caster.getCurrentHp() + hpAdd;

			if (hp > caster.getMaxHp())
			    hp = caster.getMaxHp();

			caster.setCurrentHp(hp); 
			caster.sendPacket(new StatusUpdate(caster.getObjectId()).addAttribute(StatusUpdate.CUR_HP, (int)hp));
			
            // Check to see if we should damage the target
            if (!target.isDead() || getTargetType() != SkillTargetType.TARGET_CORPSE_MOB)
            {
    			target.reduceCurrentHp(damage, caster);
    			caster.sendPacket(new SystemMessage(SystemMessage.YOU_DID_S1_DMG).addNumber(damage));
            }
            
            // Check to see if we should do the decay right after the cast
            if (target.isDead() && getTargetType() == SkillTargetType.TARGET_CORPSE_MOB && target instanceof L2NpcInstance)
                ((L2NpcInstance)target).endDecayTask();
		}
	}
	
}
