package net.sf.l2j.gameserver.skills;

import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2NpcInstance;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.serverpackets.StatusUpdate;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.templates.StatsSet;

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
		
		L2ItemInstance weaponInst = caster.getActiveWeaponInstance();

		if (weaponInst != null)
		{
			if (weaponInst.getChargedSpiritshot() == L2ItemInstance.CHARGED_BLESSED_SPIRITSHOT) 
			{
				bss = true;
				weaponInst.setChargedSpiritshot(L2ItemInstance.CHARGED_NONE);
			}
			else if (weaponInst.getChargedSpiritshot() == L2ItemInstance.CHARGED_SPIRITSHOT) 
			{
				ss = true;
				weaponInst.setChargedSpiritshot(L2ItemInstance.CHARGED_NONE);
			}
		}

		for(L2Character target : (L2Character[])targets)
        {
			if (target.isAlikeDead() && getTargetType() != SkillTargetType.TARGET_CORPSE_MOB)
				continue;
			
			double hp = 0.;
			
			if (getTargetType() != SkillTargetType.TARGET_CORPSE_MOB)
			{
				boolean mcrit = Formulas.getInstance().calcMCrit(caster.getMCriticalHit(target, this));
				double damage = Formulas.getInstance().calcMagicDam(caster, target, this, ss, bss, mcrit);
				hp = damage * absorbPart;

				target.reduceCurrentHp(damage, caster);
				if(mcrit)
					caster.sendPacket(new SystemMessage(SystemMessage.MAGIC_CRITICAL_HIT));
            	caster.sendPacket(new SystemMessage(SystemMessage.YOU_HIT_FOR_S1_DAMAGE).addNumber((int)damage)); 
			}

			hp += absorbAbs + caster.getCurrentHp();

			caster.setCurrentHp(hp); 
			caster.sendPacket(new StatusUpdate(caster.getObjectId()).addAttribute(StatusUpdate.CUR_HP, (int)hp)); 
			
            // Check to see if we should damage the target
            // Check to see if we should do the decay right after the cast
            if (target.isDead() && getTargetType() == SkillTargetType.TARGET_CORPSE_MOB && target instanceof L2NpcInstance)
                ((L2NpcInstance)target).endDecayTask();
		}
	}
	
}
