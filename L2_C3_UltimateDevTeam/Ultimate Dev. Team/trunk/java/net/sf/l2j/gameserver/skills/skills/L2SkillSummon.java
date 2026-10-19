package net.sf.l2j.gameserver.skills.skills;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.idfactory.IdFactory;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2SummonInstance;
import net.sf.l2j.gameserver.model.base.Experience;
import net.sf.l2j.gameserver.packets.serverpackets.PetInfo;
import net.sf.l2j.gameserver.packets.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.tables.NpcTable;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;
import net.sf.l2j.gameserver.templates.StatsSet;

public class L2SkillSummon extends L2Skill {

	private int     npcId;
	private float   expPenalty;
	private boolean isCubic;
	
	public L2SkillSummon(StatsSet set) {
		super(set);
		
		npcId      = set.getInteger("npcId", 0); // default for undescribed skills
		expPenalty = set.getFloat ("expPenalty", 0.f);
		isCubic    = set.getBool  ("isCubic", false);
	}

	public boolean checkCondition(L2Character activeChar)
	{
		if (activeChar instanceof L2PcInstance)
		{
			L2PcInstance player = (L2PcInstance)activeChar;
			if (isCubic) {
				int mastery = player.getSkillLevel(L2Skill.SKILL_CUBIC_MASTERY);
				if (mastery < 0)
					mastery = 0;
				int count = player.getCubics().size(); 
				if (count > mastery) {
					activeChar.sendMessage("You already have "+count+" cubic(s).");
					return false;
				}
			} else {
				if(player.getPet() != null)
				{
					activeChar.sendMessage("You already have a pet.");
					return false;
				}
			}
		}
		return super.checkCondition(activeChar);
	}
	
	public void useSkill(L2Character caster, @SuppressWarnings("unused") L2Object[] targets) {
		if (caster.isAlikeDead() || !(caster instanceof L2PcInstance))
			return;

		L2PcInstance activeChar = (L2PcInstance) caster;

		if (npcId == 0) {
			activeChar.sendMessage("Summon skill "+getId()+" not described yet");
			return;
		}
		
		if (isCubic) {
			int mastery = activeChar.getSkillLevel(L2Skill.SKILL_CUBIC_MASTERY);
			if (mastery < 0)
				mastery = 0;
			if (activeChar.getCubics().size() > mastery) {
				if (Config.DEBUG)
					_log.fine("player can't summon any more cubics. ignore summon skill");
				activeChar.sendPacket(new SystemMessage(SystemMessage.CUBIC_SUMMONING_FAILED));
				return;
			}
			activeChar.addCubic(npcId, getLevel());
			activeChar.broadcastUserInfo();
			return;
		}

		if (activeChar.getPet() != null) {
			if (Config.DEBUG)
				_log.fine("player has a pet already. ignore summon skill");
			return;
		}
		
		L2NpcTemplate summonTemplate = NpcTable.getInstance().getTemplate(npcId);
        L2SummonInstance summon = new L2SummonInstance(IdFactory.getInstance().getNextId(), summonTemplate, activeChar);
		
        summon.setTitle(activeChar.getName());
        summon.setExpPenalty(expPenalty);
        if (summon.getLevel() >= Experience.LEVEL.length)
        {
            summon.getStat().setExp(Experience.LEVEL[Experience.LEVEL.length - 1]);
            _log.warning("Summon ("+summon.getName()+") NpcID: "+summon.getNpcId()+" has a level above 75. Please rectify.");
        }
        else
        {
            summon.getStat().setExp(Experience.LEVEL[(summon.getLevel() % Experience.LEVEL.length)]);
        }
		summon.setCurrentHp(summon.getMaxHp());
		summon.setCurrentMp(summon.getMaxMp());
		summon.setHeading(activeChar.getHeading());
    	summon.setRunning();
		activeChar.setPet(summon);
    		
    	L2World.getInstance().storeObject(summon);
        summon.spawnMe(activeChar.getX()+50, activeChar.getY()+100, activeChar.getZ());
    		
    	summon.setFollowStatus(true);
        summon.setShowSummonAnimation(false); // addVisibleObject created the info packets with summon animation
                                              // if someone comes into range now, the animation shouldnt show any more
        activeChar.sendPacket(new PetInfo(summon));

	}

}
