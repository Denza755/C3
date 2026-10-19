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

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.ClientThread;
import net.sf.l2j.gameserver.SkillSpellbookTable;
import net.sf.l2j.gameserver.SkillTable;
import net.sf.l2j.gameserver.SkillTreeTable;
import net.sf.l2j.gameserver.model.L2FolkInstance;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2NpcInstance;
import net.sf.l2j.gameserver.model.L2PcInstance;
import net.sf.l2j.gameserver.model.L2ShortCut;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.serverpackets.ShortCutRegister;
import net.sf.l2j.gameserver.serverpackets.StatusUpdate;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;

/**
 * This class ...
 * 
 * @version $Revision: 1.7.2.1.2.4 $ $Date: 2005/03/27 15:29:30 $
 */
public class RequestAquireSkill extends ClientBasePacket
{
	private static final String _C__6C_REQUESTAQUIRESKILL = "[C] 6C RequestAquireSkill";
	private static Logger _log = Logger.getLogger(RequestAquireSkill.class.getName());

	private final int _id;
	private final int _level;
	/**
	 * packet type id 0x6c
	 * format:		cdd
	 * @param rawPacket
	 */
	public RequestAquireSkill(ByteBuffer buf, ClientThread client)
	{
		super(buf, client);
		_id = readD();
		_level = readD();
	}

	void runImpl()
	{
		L2PcInstance player = getClient().getActiveChar();
		if(player == null) return;
		L2FolkInstance trainer = player.getLastFolkNPC();
		if ((trainer == null || player.getDistance(trainer.getX(), trainer.getY()) > L2NpcInstance.INTERACTION_DISTANCE) && !player.isGM())
			return;
		
		if (player.getSkillLevel(_id) >= _level) {
			// already knows the skill with this level
			return;
		}

		L2Skill skill = SkillTable.getInstance().getInfo(_id, _level);
		
		int _requiredSp = SkillTreeTable.getInstance().getSkillCost(player, skill);
		
		if (player.getSp() >= _requiredSp) 
		{
			Integer spb_id = SkillSpellbookTable._skillSpellbooks.get( skill.getId() );
			if ( skill.getLevel() == 1 && spb_id != null )
			{
				//Need spellbook
				//L2EtcItem spb = (L2EtcItem)ItemTable.getInstance ().getTemplate( spb_id );
				L2ItemInstance spb = player.getInventory().findItemByItemId( spb_id.intValue() );
				if(spb == null)
				{
					//Haven't spellbook
					player.sendPacket(new SystemMessage(SystemMessage.YOU_DO_NOT_HAVE_ENOUGH_ITEMS_TO_LEARN_SKILLS));
					return;
				}
				//ok
				player.getInventory().destroyItem(player.getInventory().getItemByItemId(spb_id).getObjectId(), 1);
			}
			player.addSkill(skill);
			if (Config.DEBUG) _log.fine("Learned skill "+_id+" for "+_requiredSp+" SP.");
			player.setSp(player.getSp() - _requiredSp);
			player.updateStats();
			StatusUpdate su = new StatusUpdate(player.getObjectId());
			su.addAttribute(StatusUpdate.SP, player.getSp());
			player.sendPacket(su);

			player.sendPacket(new SystemMessage(SystemMessage.YOU_HAVE_EARNED_S1).addSkillName(_id));
			
			//update all the shortcuts to this skill
			if (_level > 1)
			{
				L2ShortCut[] allShortCuts = player.getAllShortCuts();
				for (L2ShortCut sc : allShortCuts) {
					if (sc.id == _id && sc.type == L2ShortCut.TYPE_SKILL)
					{
						L2ShortCut newsc = new L2ShortCut(sc.slot, sc.page, sc.type, sc.id, _level, sc.unk);
						player.sendPacket(new ShortCutRegister(newsc));
						player.registerShortCut(newsc);
					}
				}
			}
		} 
		else
		{
			player.sendPacket(new SystemMessage(SystemMessage.YOU_DO_NOT_HAVE_ENOUGH_SP_TO_LEARN_SKILLS));
			if (Config.DEBUG) _log.fine("Not enough SP!");
		}

		trainer.showSkillList(player, player.getSkillLearningClassId());
	}

	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.clientpackets.ClientBasePacket#getType()
	 */
	public String getType()
	{
		return _C__6C_REQUESTAQUIRESKILL;
	}
}
