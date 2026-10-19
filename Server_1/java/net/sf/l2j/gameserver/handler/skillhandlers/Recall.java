/*
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA.
 *
 * http://www.gnu.org/copyleft/gpl.html
 */
package net.sf.l2j.gameserver.handler.skillhandlers;

import javolution.util.FastList;

import net.sf.l2j.gameserver.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.datatables.MapRegionTable;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2RaidBossInstance;
import net.sf.l2j.gameserver.handler.ISkillHandler;
import net.sf.l2j.Config;

public class Recall implements ISkillHandler
{
	//private static Logger _log = Logger.getLogger(Recall.class.getName());
	private static final L2Skill.SkillType[] SKILL_IDS = { L2Skill.SkillType.RECALL };

	@Override
	public void useSkill(L2Character activeChar, L2Skill skill, L2Object[] targets)
	{
		try
		{
			if(activeChar instanceof L2PcInstance)
			{

				// Checks summoner not in siege zone
				if(activeChar.isInsideZone(L2Character.ZONE_SIEGE))
				{
					((L2PcInstance) activeChar).sendMessage("You cannot summon in siege zone.");
					return;
				}

				if(activeChar.isInsideZone(L2Character.ZONE_PVP))
				{
					activeChar.sendPacket(new SystemMessage(SystemMessage.YOU_CANNOT_SUMMON_IN_COMBAT));
					return;
				}

				L2Object[] objects = L2World.getInstance().getVisibleObjects(activeChar, 5000);
				if(objects != null)
				{
					for(L2Object object : objects)
					{
						if(object instanceof L2RaidBossInstance)
						{
							activeChar.sendPacket(new SystemMessage(SystemMessage.YOU_MAY_NOT_SUMMON_FROM_YOUR_CURRENT_LOCATION));
							return;
						}
					}
				}
			}

			for(int index = 0; index < targets.length; index++)
			{
				if(!(targets[index] instanceof L2Character))
					continue;

				L2Character target = (L2Character) targets[index];

				if(target instanceof L2PcInstance)
				{
					L2PcInstance targetChar = (L2PcInstance) target;

					if(targetChar.isFestivalParticipant())
					{
						targetChar.sendPacket(SystemMessage.sendString("You can't use escape skill in a festival."));
						continue;
					}


					if(targetChar.isInJail())
					{
						targetChar.sendPacket(SystemMessage.sendString("You can't escape from jail."));
						continue;
					}



					if(targetChar.isAlikeDead())
					{
						SystemMessage sm = new SystemMessage(SystemMessage.S1_IS_DEAD_AT_THE_MOMENT_AND_CANNOT_BE_SUMMONED);
						sm.addString(targetChar.getName());
						activeChar.sendPacket(sm);
						sm = null;
						continue;
					}

					if(targetChar.isInStoreMode())
					{
						SystemMessage sm = new SystemMessage(SystemMessage.S1_CURRENTLY_TRADING_OR_OPERATING_PRIVATE_STORE_AND_CANNOT_BE_SUMMONED);
						sm.addString(targetChar.getName());
						activeChar.sendPacket(sm);
						sm = null;
						continue;
					}

					if(targetChar.isRooted() || targetChar.isInCombat())
					{
						SystemMessage sm = new SystemMessage(SystemMessage.S1_IS_ENGAGED_IN_COMBAT_AND_CANNOT_BE_SUMMONED);
						sm.addString(targetChar.getName());
						activeChar.sendPacket(sm);
						sm = null;
						continue;
					}



					if(targetChar.isInsideZone(L2Character.ZONE_PVP))
					{
						activeChar.sendPacket(new SystemMessage(SystemMessage.YOUR_TARGET_IS_IN_AN_AREA_WHICH_BLOCKS_SUMMONING));
						continue;
					}
				}

				target.teleToLocation(MapRegionTable.TeleportWhereType.Town);
				target = null;
			}
		}
		catch(Throwable e)
		{
		}
	}

	@Override
	public L2Skill.SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}
}
