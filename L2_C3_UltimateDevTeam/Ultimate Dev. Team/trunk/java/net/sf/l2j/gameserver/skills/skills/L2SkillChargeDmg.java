package net.sf.l2j.gameserver.skills.skills;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.packets.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.templates.StatsSet;
import net.sf.l2j.gameserver.skills.effects.EffectCharge;
import net.sf.l2j.gameserver.skills.Formulas;

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
				activeChar.sendMessage("Not enough charges.");
				return false;
			}
		}
		return super.checkCondition(activeChar);
	}
	
	public void useSkill(L2Character caster, L2Object[] targets)
    {
		if (caster.isAlikeDead())
        {
			return;
        }
		
		// get the effect
		EffectCharge effect = (EffectCharge) caster.getEffect(charge_skill_id);
		if (effect == null || effect.num_charges < this.num_charges)
		{
			caster.sendMessage("Not enough charges.");
			return;
		}
		effect.num_charges -= this.num_charges;
        double modifier = 0;
        modifier = effect.num_charges*0.33;
	if (Config.ALT_CHARGE)
	{ 
		if (effect.num_charges <= 0) 
		{
	        	effect.num_charges = 0;
			caster.updateEffectIcons();
			effect.exit(); 
		}
		else 
			caster.updateEffectIcons(); 
	} 
	else
	{ 
        	effect.num_charges = 0;
		caster.updateEffectIcons();
	        effect.exit();
	}
        boolean ss = false;  
        L2ItemInstance weaponInst = caster.getActiveWeaponInstance();  
          
        if (caster instanceof L2PcInstance && weaponInst != null)   
        {  
            if (weaponInst.getChargedSoulshot() == L2ItemInstance.CHARGED_SOULSHOT)   
            {  
                ss = true;  
                weaponInst.setChargedSoulshot(L2ItemInstance.CHARGED_NONE);  
            }  
        }  

        for(int index = 0;index < targets.length;index++)
        {
			L2Character target = (L2Character)targets[index];
			if (target.isAlikeDead())
            {
				continue;
            }
			// TODO: should we use dual or not?
			// because if so, damage are lowered but we dont do anything special with dual then
			// like in doAttackHitByDual which in fact does the calcPhysDam call twice
			
			//boolean dual  = caster.isUsingDualWeapon();
			boolean shld = Formulas.getInstance().calcShldUse(caster, target);
			boolean crit = Formulas.getInstance().calcCrit(caster.getCriticalHit(target, this));
			
			int damage = (int)Formulas.getInstance().calcPhysDam(
					caster, target, this, shld, crit, false, ss);
			
			if (damage > 0)
            {
                double finalDamage = damage;
                finalDamage = finalDamage+(modifier*finalDamage);
				target.reduceCurrentHp(finalDamage, caster);
				caster.sendPacket(new SystemMessage(SystemMessage.YOU_DID_S1_DMG).addNumber((int)finalDamage));
			}
            else
            {
				caster.sendPacket(new SystemMessage(SystemMessage.MISSED_TARGET));
			}
		}	
	}
	
}
