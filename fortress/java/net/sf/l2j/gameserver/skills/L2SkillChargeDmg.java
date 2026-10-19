package net.sf.l2j.gameserver.skills;

import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2PcInstance;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.templates.StatsSet;

public class L2SkillChargeDmg extends L2Skill 
{

	final int num_charges;
	final int charge_skill_id;
	
	public L2SkillChargeDmg(StatsSet set)
    {
		super(set);
		
		num_charges = set.getInteger("num_charges", getLevel());
		charge_skill_id = set.getInteger("charge_skill_id");
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
	
	public void useSkill(L2Character caster, L2Object[] targets)
    {
		if(caster.isAlikeDead()) return;

		EffectCharge effect = (EffectCharge) caster.getEffect(charge_skill_id);
		if (effect == null || effect.num_charges < this.num_charges)
		{
			SystemMessage sm = new SystemMessage(614);
			sm.addString("Not enough charges.");
			caster.sendPacket(sm);
			return;
		}

		L2ItemInstance weaponInst = caster.getActiveWeaponInstance();
		boolean ss = (weaponInst != null && weaponInst.getChargedSoulshot() == L2ItemInstance.CHARGED_SOULSHOT);
		if(ss) weaponInst.setChargedSoulshot(L2ItemInstance.CHARGED_NONE);

		double modifier = 0.3 * effect.num_charges + 0.7;
		effect.num_charges = 0;
		caster.updateEffectIcons();
		effect.exit();
		for(L2Object targ : targets)
		{
			L2Character target = (L2Character)targ;
			if (target.isAlikeDead())
				continue;

			boolean shld = Formulas.getInstance().calcShldUse(caster, target);
			double damage = modifier * Formulas.getInstance().calcPhysDam(caster, target, this, shld, false, false, ss);

			target.reduceCurrentHp(damage, caster);
			caster.sendPacket(new SystemMessage(SystemMessage.YOU_HIT_FOR_S1_DAMAGE).addNumber((int)damage));
		}
	}
}
