package net.sf.l2j.gameserver.skills;

import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2ItemInstance;
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
		absorbAbs  = set.getIntegr("absorbAbs", 0);
	}

	public void useSkill(L2Character caster, L2Object[] targets) {
		if (caster.isAlikeDead())
			return;
		
		boolean ss = false;
		boolean bss = false;
		
        for(int index = 0;index < targets.length;index++)
        {
			L2Character target = (L2Character)targets[index];
			if (target.isAlikeDead() && getTargetType() != SkillTargetType.TARGET_CORPSE_MOB)
				continue;
			
			double scale = 1.0;

			L2ItemInstance weaponInst = caster.getActiveWeaponInstance();
			
			if (weaponInst == null)
			{
			}
			else if (weaponInst.getChargedSpiritshot() == L2ItemInstance.CHARGED_BLESSED_SPIRITSHOT) 
			{
				scale *= 2;
				bss = true;
				weaponInst.setChargedSpiritshot(L2ItemInstance.CHARGED_NONE);
			}
            else if (weaponInst.getChargedSpiritshot() == L2ItemInstance.CHARGED_SPIRITSHOT) 
			{
				scale *= 1.5;
				ss = true;
				weaponInst.setChargedSpiritshot(L2ItemInstance.CHARGED_NONE);
			}
            
			int damage = (int)Formulas.getInstance().calcMagicDam(
					caster, target, this, ss, bss);

			double hpAdd = absorbAbs + absorbPart * damage;
			hpAdd *= scale;
			double hp = caster.getCurrentHp() + hpAdd;

			caster.setCurrentHp(hp); 
			StatusUpdate suhp = new StatusUpdate(caster.getObjectId()); 
			suhp.addAttribute(StatusUpdate.CUR_HP, (int)hp); 
			caster.sendPacket(suhp); 
			
			target.reduceCurrentHp(damage, caster);
			SystemMessage sm = new SystemMessage(SystemMessage.YOU_DID_S1_DMG);
			sm.addNumber(damage); 
			caster.sendPacket(sm); 
		}
	}
	
}
