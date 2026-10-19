package net.sf.l2j.gameserver.handler.skillhandlers;

import net.sf.l2j.gameserver.handler.ISkillHandler;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2Skill.SkillType;
import net.sf.l2j.gameserver.model.actor.instance.L2DoorInstance;
import net.sf.l2j.gameserver.packets.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.skills.Formulas;

public class Unlock implements ISkillHandler
{
    protected SkillType[] _skillIds = {SkillType.UNLOCK};	

 public void useSkill(L2Character activeChar, L2Skill skill, @SuppressWarnings("unused") L2Object[] targets)
 {
	 	L2Object[] targetList = skill.getTargetList(activeChar);

		for (int index = 0; index < targetList.length; index++) 
		{
			L2Object target = targetList[index];
			if(target instanceof L2DoorInstance)
			{
			L2DoorInstance door = (L2DoorInstance) target;
			
			boolean success = Formulas.getInstance().calculateUnlockChance(skill);
			
			if (!door.isUnlockable())
			{
		         activeChar.sendPacket(new SystemMessage(319));
		         return;
			}
			
			if (success && (door.getOpen() == 1))
			{
				door.openMe();
				door.onOpen();
		    }
			else
		         activeChar.sendPacket(new SystemMessage(320));
			}
		}
 }

 public SkillType[] getSkillIds() 
 { 
     return _skillIds; 
 }
}
