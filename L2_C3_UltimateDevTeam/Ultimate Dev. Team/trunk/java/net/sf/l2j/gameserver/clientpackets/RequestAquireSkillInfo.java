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
package net.sf.l2j.gameserver.clientpackets;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.ClientThread;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2SkillLearn;
import net.sf.l2j.gameserver.model.actor.instance.L2FolkInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2NpcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.packets.serverpackets.AquireSkillInfo;
import net.sf.l2j.gameserver.tables.SkillSpellbookTable;
import net.sf.l2j.gameserver.tables.SkillTable;
import net.sf.l2j.gameserver.tables.SkillTreeTable;

import java.nio.ByteBuffer;
import java.util.logging.Logger;

/**
 * This class ...
 * 
 * @version $Revision: 1.5.2.1.2.5 $ $Date: 2005/04/06 16:13:48 $
 */
public class RequestAquireSkillInfo extends ClientBasePacket
{
	private static final String _C__6B_REQUESTAQUIRESKILLINFO = "[C] 6B RequestAquireSkillInfo";
	private static Logger _log = Logger.getLogger(RequestAquireSkillInfo.class.getName());

	private final int _id;
	private final int _level;
    @SuppressWarnings("unused")
    private final int _unk1;
    
	/**
	 * packet type id 0x6b
	 * packet format rev650  cddd
	 * @param rawPacket
	 */
	public RequestAquireSkillInfo(ByteBuffer buf, ClientThread client)
	{
		super(buf, client);
		_id = readD();
		_level = readD();
        //TODO analyse unk1
        _unk1 = readD();
	}

	void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
		    return;
		L2FolkInstance trainer = activeChar.getLastFolkNPC();
		if ((trainer == null || activeChar.getDistance(trainer.getX(), trainer.getY()) > L2NpcInstance.INTERACTION_DISTANCE) && !activeChar.isGM())
			return;
		
		if (!trainer.getTemplate().canTeach(activeChar.getClassId()))
		    return; //cheater

		L2Skill skill = SkillTable.getInstance().getInfo(_id, _level);
		if (skill == null)
		{
		    _log.warning("skill id "+_id+" level "+_level+" is undefined. aquireSkillInfo failed.");
		    return;
		}

		L2SkillLearn[] skills = SkillTreeTable.getInstance().getAvailableSkills(activeChar,activeChar.getClassId());
		boolean canteach = false;
		
		for (int i = 0; i < skills.length; i++)
		{
		    L2SkillLearn s = skills[i];
		    if (s.getId() == _id && s.getLevel() == _level)
		    {
			canteach = true;
			break;
		    }		    
		}
		if (!canteach)
		    return; //cheater or something wrong. etc c4 :)
		
		
		
		
		int requiredSp = SkillTreeTable.getInstance().getSkillCost(activeChar, skill);
		
		AquireSkillInfo asi = new AquireSkillInfo(skill.getId(), skill.getLevel(), requiredSp);
//		asi.addRequirement(6,5,1,2);
//		asi.addRequirement(5,1097,2,0);
		if (Config.SP_BOOK_NEEDED) {
			Integer spb_id = SkillSpellbookTable._skillSpellbooks.get( skill.getId() );
			if ( skill.getLevel() == 1 && spb_id != null )
			{
				asi.addRequirement( 99 , spb_id.intValue() , 1 , 50 );
			}
		}
		sendPacket(asi);
		
		
	}

	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.clientpackets.ClientBasePacket#getType()
	 */
	public String getType()
	{
		return _C__6B_REQUESTAQUIRESKILLINFO;
	}
}
