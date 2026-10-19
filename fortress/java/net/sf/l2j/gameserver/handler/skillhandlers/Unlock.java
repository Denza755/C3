package net.sf.l2j.gameserver.handler.skillhandlers;

import net.sf.l2j.gameserver.handler.ISkillHandler;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2DoorInstance;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2Skill.SkillType;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.skills.Formulas;

public class Unlock implements ISkillHandler
{
    //private static Logger _log = Logger.getLogger(Unlock.class.getName()); 
    protected SkillType[] _skillIds = {SkillType.UNLOCK};	

 public void useSkill(L2Character activeChar, L2Skill skill, @SuppressWarnings("unused") L2Object[] targets)
 {
	 	L2Object[] targetList = skill.getTargetList(activeChar);

		for (int index = 0; index < targetList.length; index++) 
		{
			L2DoorInstance target = (L2DoorInstance) targetList[index];
			
			if(target.isOpen())
			{
		         activeChar.sendPacket(new SystemMessage(SystemMessage.IT_IS_NOT_LOCKED));	
		         return;
			}
			
			if (!target.isUnlockable())
			{
		         activeChar.sendPacket(new SystemMessage(SystemMessage.YOU_ARE_UNABLE_TO_UNLOCK_THE_DOOR));	
		         return;
			}
			
			boolean success = Formulas.getInstance().calculateUnlockChance(skill);

			if (success && !target.isOpen())
			{
				target.openMe();
				target.onOpen();
		    }
			else
		         activeChar.sendPacket(new SystemMessage(SystemMessage.YOU_HAVE_FAILED_TO_UNLOCK_THE_DOOR));				
		}
 }

 public SkillType[] getSkillIds() 
 { 
     return _skillIds; 
 }
}
