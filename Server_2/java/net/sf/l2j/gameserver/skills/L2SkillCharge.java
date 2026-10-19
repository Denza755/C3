package net.sf.l2j.gameserver.skills;

import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.templates.StatsSet;

public class L2SkillCharge extends L2Skill {

	final int num_charges;
	
	public L2SkillCharge(StatsSet set) {
		super(set);
		
		num_charges = set.getIntegr("num_charges", getLevel());
	}

	public void useSkill(L2Character caster, L2Object[] targets) {
		if (caster.isAlikeDead())
			return;
		
		// get the effect
		EffectCharge effect = (EffectCharge) caster.getEffect(this);
		if (effect != null) {
			if (effect.num_charges < num_charges)
			{
				effect.num_charges++;
				caster.updateEffectIcons();
			}
			return;
		}
		this.getEffects(caster, caster);
	}
	
}
