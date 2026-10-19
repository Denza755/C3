package net.sf.l2j.gameserver.handler.skillhandlers;

import net.sf.l2j.gameserver.MapRegionTable;
import net.sf.l2j.gameserver.handler.ISkillHandler;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2PcInstance;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2Skill.SkillType;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;

public class Recall implements ISkillHandler
{
	//private static Logger _log = Logger.getLogger(Recall.class.getName());
	protected SkillType[] _skillIds = {SkillType.RECALL};

 	public void useSkill(@SuppressWarnings("unused") L2Character activeChar, @SuppressWarnings("unused") L2Skill skill, L2Object[] targets)
	{
		try {
			for(L2Character target : (L2Character[])targets)
			{
				// Check to see if the current player target is in a festival.
                if ((target instanceof L2PcInstance) && ((L2PcInstance)target).isFestivalParticipant()) {
                    target.sendPacket(SystemMessage.sendString("You may not use an escape skill in a festival."));
                    break;
                }
                  
                MapRegionTable.getInstance().teleToLocation(target, MapRegionTable.TeleportWhereType.ClosestTown);
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