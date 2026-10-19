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

import net.sf.l2j.gameserver.handler.ISkillHandler;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2Party;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.actor.instance.L2PetInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.L2Skill.SkillTargetType;
import net.sf.l2j.gameserver.model.L2Skill.SkillType;
import net.sf.l2j.gameserver.serverpackets.Revive;
import net.sf.l2j.gameserver.serverpackets.SocialAction;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;
/**
 * This class ...
 *
 * @version $Revision: 1.1.2.5.2.4 $ $Date: 2005/04/03 15:55:03 $
 */

public class Resurrect implements ISkillHandler
{
	//private static Logger _log = Logger.getLogger(Resurrect.class.getName());

	private static SkillType[] _skillIds = {SkillType.RESURRECT};

	public void useSkill(L2Character activeChar, L2Skill skill, L2Object[] targets)
	{
		L2Character target = null;

        for (int index = 0; index < targets.length; index++)
        {
            target = (L2Character) targets[index];

            if (target instanceof L2PetInstance)
            {
                L2PetInstance targetPet = (L2PetInstance)target;
                targetPet.stopDecay();
                targetPet.setCurrentHpMp(0.6 * target.getMaxHp(), 0.6 * target.getMaxMp());
                targetPet.setCurrentFed(targetPet.getMaxFed() / 10);
            }

            if (target instanceof L2PcInstance)
            {
                L2PcInstance player = (L2PcInstance)activeChar;
                L2PcInstance targetPlayer = (L2PcInstance)target;

                // Check for same party or for same clan, if target is for clan.
                if (skill.getTargetType() == SkillTargetType.TARGET_CORPSE_CLAN)
                {
                    if (player.getClanId() != targetPlayer.getClanId())
                        return;
                }
                else
                {
                    L2Party charParty = player.getParty();
                    L2Party targetParty = targetPlayer.getParty();

                    if (charParty == null || targetParty == null || charParty.getPartyLeaderOID() != targetParty.getPartyLeaderOID() )
                        return;
                }

                // Check to see if the current player target is in a festival.
                if (targetPlayer.isFestivalParticipant())
                {
                    targetPlayer.sendPacket(SystemMessage.sendString("You may not resurrect participants in a festival."));
                    break;
                }

                // Restore the player's lost experience,
                // depending on the % return of the skill used (based on its power).
                targetPlayer.restoreExp(skill.getPower());
            }

            target.setCurrentHpMp(0.6 * target.getMaxHp(), 0.6 * target.getMaxMp());
            target.setCurrentCp(0.6 * target.getMaxCp());

            Revive revive = new Revive(target);
            SocialAction sa = new SocialAction(target.getObjectId(), 15);
            target.broadcastPacket(sa);
            target.broadcastPacket(revive);
        }
	}


	public SkillType[] getSkillIds()
	{
		return _skillIds;
	}
}
