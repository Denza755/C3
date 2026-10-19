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

import java.nio.ByteBuffer;
import java.util.logging.Logger;

import net.sf.l2j.gameserver.ClientThread;
import net.sf.l2j.gameserver.SkillSpellbookTable;
import net.sf.l2j.gameserver.SkillTable;
import net.sf.l2j.gameserver.SkillTreeTable;
import net.sf.l2j.gameserver.model.L2FolkInstance;
import net.sf.l2j.gameserver.model.L2NpcInstance;
import net.sf.l2j.gameserver.model.L2PcInstance;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.serverpackets.AquireSkillInfo;

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
	/**
	 * packet type id 0x6b
	 * format:		cdd
	 * @param rawPacket
	 */
	public RequestAquireSkillInfo(ByteBuffer buf, ClientThread client)
	{
		super(buf, client);
		_id = readD();
		_level = readD();
	}

	void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if(activeChar == null) return;
		L2FolkInstance trainer = activeChar.getLastFolkNPC();
		if ((trainer == null || activeChar.getDistance(trainer.getX(), trainer.getY()) > L2NpcInstance.INTERACTION_DISTANCE) && !activeChar.isGM())
			return;
		
		L2Skill skill = SkillTable.getInstance().getInfo(_id, _level);
		
		if (skill == null)
		{
		    _log.warning("skill id "+_id+" level "+_level+" is undefined. aquireSkillInfo failed.");
		    return;
		}
		
		int requiredSp = SkillTreeTable.getInstance().getSkillCost(activeChar, skill);
		
		AquireSkillInfo asi = new AquireSkillInfo(skill.getId(), skill.getLevel(), requiredSp);
//		asi.addRequirement(6,5,1,2);
//		asi.addRequirement(5,1097,2,0);
		Integer spb_id = SkillSpellbookTable._skillSpellbooks.get( skill.getId() );
		if ( skill.getLevel() == 1 && spb_id != null )
		{
			asi.addRequirement( 99 , spb_id.intValue() , 1 , 50 );
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
