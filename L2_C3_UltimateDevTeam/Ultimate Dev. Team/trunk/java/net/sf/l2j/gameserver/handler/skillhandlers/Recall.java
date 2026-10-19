package net.sf.l2j.gameserver.handler.skillhandlers;

import net.sf.l2j.gameserver.handler.ISkillHandler;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2Skill.SkillType;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.packets.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.tables.MapRegionTable;

public class Recall implements ISkillHandler
{
	protected SkillType[] _skillIds = {SkillType.RECALL};

 	public void useSkill(@SuppressWarnings("unused") L2Character activeChar, @SuppressWarnings("unused") L2Skill skill, L2Object[] targets)
	{
		try {
			for(int index = 0; index < targets.length; index++)
			{
				L2Character target = (L2Character)targets[index];
				L2PcInstance targetChar = (L2PcInstance)target;
				
                if (targetChar.isFestivalParticipant()) {
                    targetChar.sendPacket(SystemMessage.sendString("You may not use an escape skill in a festival."));
                    break;
                }
                  
                ((L2Character)target).teleToLocation(MapRegionTable.TeleportWhereType.Town);
			}
                } catch (Throwable e) {
 	 	 	e.printStackTrace();
 	 	}
 	}

	public SkillType[] getSkillIds()
	{
		return _skillIds;
	}
}