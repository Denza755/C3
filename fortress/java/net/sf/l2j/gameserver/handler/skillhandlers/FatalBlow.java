package net.sf.l2j.gameserver.handler.skillhandlers;

import java.util.logging.Logger;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.handler.ISkillHandler;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2PcInstance;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2Summon;
import net.sf.l2j.gameserver.model.L2Skill.SkillType;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.skills.Formulas;

public class FatalBlow implements ISkillHandler
{
	private static Logger _log = Logger.getLogger(Pdam.class.getName());

	private static SkillType[] _skillIds = {SkillType.FATALBLOW};

	public void useSkill(L2Character activeChar, L2Skill skill, L2Object[] targets)
	{
		if (activeChar.isAlikeDead())
			return;

		int damage = 0;

		if (Config.DEBUG)
			_log.fine("Begin Skill processing in FatalBlow.java " + skill.getSkillType());

		for(L2Character target : (L2Character[])targets)
        {
			if (target.isAlikeDead())
				continue;

			if ((skill.getCondition() & L2Skill.COND_CRIT) != 0)
				if (!Formulas.getInstance().calcBlow(activeChar, target))
					continue;

			boolean shld = Formulas.getInstance().calcShldUse(activeChar, target);
			L2ItemInstance weaponInst = activeChar.getActiveWeaponInstance();
			boolean ss = (weaponInst != null && weaponInst.getChargedSoulshot() == L2ItemInstance.CHARGED_SOULSHOT);

			damage = (int)Formulas.getInstance().calcPhysDam(activeChar, target, skill, shld, false, false, ss);

			damage = (int)activeChar.getFatalBlowDamage(target, damage);

			if (damage > 0)
			{
				target.reduceCurrentHp(damage, activeChar);
				if (ss) weaponInst.setChargedSoulshot(L2ItemInstance.CHARGED_NONE);

				if (activeChar instanceof L2PcInstance)
				{
					activeChar.sendPacket(new SystemMessage(SystemMessage.CRITICAL_HIT));
					activeChar.sendPacket(new SystemMessage(SystemMessage.YOU_HIT_FOR_S1_DAMAGE).addNumber(damage));
				}
				else if (activeChar instanceof L2Summon)
                {
					L2PcInstance owner = ((L2Summon)activeChar).getOwner();
					owner.sendPacket(new SystemMessage(SystemMessage.PETS_CRITICAL_HIT));
                    owner.sendPacket(new SystemMessage(SystemMessage.THE_SUMMONED_MONSTER_GAVE_DAMAGE_OF_S1).addNumber(damage));
                }
			}
        }
	}

	public SkillType[] getSkillIds()
	{
		return _skillIds;
	}
}
