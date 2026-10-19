package net.sf.l2j.gameserver.skills;

import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2PcInstance;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.templates.StatsSet;

public class L2SkillChargeDmg extends L2Skill {

	final int num_charges;
	final int charge_skill_id;
	
	public L2SkillChargeDmg(StatsSet set) {
		super(set);
		
		num_charges = set.getIntegr("num_charges", getLevel());
		charge_skill_id = set.getIntegr("charge_skill_id");
	}

	public boolean checkCondition(L2Character activeChar)
	{
		if (activeChar instanceof L2PcInstance)
		{
			L2PcInstance player = (L2PcInstance)activeChar;
			EffectCharge e = (EffectCharge)player.getEffect(charge_skill_id);
			if(e == null || e.num_charges < this.num_charges)
			{
				SystemMessage sm = new SystemMessage(614);
				sm.addString("Not enough charges.");
				activeChar.sendPacket(sm);
				return false;
			}
		}
		return super.checkCondition(activeChar);
	}
	
	public void useSkill(L2Character caster, L2Object[] targets) {
		if (caster.isAlikeDead())
			return;
		
		// get the effect
		EffectCharge effect = (EffectCharge) caster.getEffect(charge_skill_id);
		if (effect == null || effect.num_charges < this.num_charges)
		{
			SystemMessage sm = new SystemMessage(614);
			sm.addString("Not enough charges.");
			caster.sendPacket(sm);
			return;
		}
		effect.num_charges -= this.num_charges;
		if (effect.num_charges <= 0)
			effect.exit();
		else
			caster.updateEffectIcons();
		
        for(int index = 0;index < targets.length;index++)
        {
			L2Character target = (L2Character)targets[index];
			if (target.isAlikeDead())
				continue;
			
			boolean dual  = caster.isUsingDualWeapon();
			boolean shld = Formulas.getInstance().calcShldUse(caster, target);
			boolean crit = Formulas.getInstance().calcCrit(caster.getCriticalHit(target, this));
			
			int damage = (int)Formulas.getInstance().calcPhysDam(
					caster, target, this, shld, crit, dual, false);
			
			if (damage > 0) {
				target.reduceCurrentHp(damage, caster);
				SystemMessage sm = new SystemMessage(SystemMessage.YOU_DID_S1_DMG);
				sm.addNumber(damage);
				caster.sendPacket(sm);
			} else {
				SystemMessage sm = new SystemMessage(SystemMessage.MISSED_TARGET);
				caster.sendPacket(sm);
			}
		}	
	}
	
}
