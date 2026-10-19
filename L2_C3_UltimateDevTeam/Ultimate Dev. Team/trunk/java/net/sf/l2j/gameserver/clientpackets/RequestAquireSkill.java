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
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2ShortCut;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2SkillLearn;
import net.sf.l2j.gameserver.model.actor.instance.L2FolkInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2NpcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.packets.serverpackets.ShortCutRegister;
import net.sf.l2j.gameserver.packets.serverpackets.StatusUpdate;
import net.sf.l2j.gameserver.packets.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.tables.SkillSpellbookTable;
import net.sf.l2j.gameserver.tables.SkillTable;
import net.sf.l2j.gameserver.tables.SkillTreeTable;
import net.sf.l2j.gameserver.util.IllegalPlayerAction;
import net.sf.l2j.gameserver.util.Util;

import java.nio.ByteBuffer;
import java.util.logging.Logger;

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
    private final int _unk1;
    
	/**
	 * packet type id 0x6c
	 * format rev650:	cddd
	 * @param rawPacket
	 */
	public RequestAquireSkill(ByteBuffer buf, ClientThread client)
	{
		super(buf, client);
		_id = readD();
		_level = readD();
        //TODO anaylse unk1
        _unk1 = readD();
	}

	void runImpl()
	{
		L2PcInstance player = getClient().getActiveChar();
		L2FolkInstance trainer = player.getLastFolkNPC();
        int npcid = trainer.getNpcId();
		if ((trainer == null || player.getDistance(trainer.getX(), trainer.getY()) > L2NpcInstance.INTERACTION_DISTANCE) && !player.isGM())
			return;
		
		if (player.getSkillLevel(_id) >= _level) {
			// already knows the skill with this level
			return;
		}
		L2Skill skill = SkillTable.getInstance().getInfo(_id, _level);
        
        //Skill Learn bug Fix
        L2SkillLearn[] skills = SkillTreeTable.getInstance().getAvailableSkills(player, player.getClassId());
        int counts=0;
        //for (int i = 0; i < skills.length; i++)
        for (L2SkillLearn s : skills)
        {
            //L2SkillLearn s = skills[i];
            L2Skill sk = SkillTable.getInstance().getInfo(s.getId(), s.getLevel());            
            if (sk == null || sk!=skill ||!sk.getCanLearn(player.getClassId()) || !sk.canTeachBy(npcid))
                continue;            
            counts++;            
        }
        // If 0 valid skills are found the player is trying to learn a skill he must usually not.
        // If Config.ALT_GAME_SKILL_LEARN is True it will anyway allow him to learn it
        if ((counts==0 || !skill.getCanLearn(player.getClassId()) || !skill.canTeachBy(npcid)) && !Config.ALT_GAME_SKILL_LEARN)//Cheater - want to learn skill that he cant
        {
		    {
        		player.sendMessage("You are trying to learn skill that u can't..");
        	}
            Util.handleIllegalPlayerAction(player,"Player "+player.getName()+" tried to learn skill that he can't!!!", _log, IllegalPlayerAction.PUNISH_KICK);
            return;
        }

		int _requiredSp = SkillTreeTable.getInstance().getSkillCost(player, skill);
		
		if (player.getSp() >= _requiredSp) 
		{
			if (Config.SP_BOOK_NEEDED)
			{
				Integer spb_id = SkillSpellbookTable._skillSpellbooks.get( skill.getId() );
				if ( skill.getLevel() == 1 && spb_id != null )
				{
					//Need spellbook
					//L2EtcItem spb = (L2EtcItem)ItemTable.getInstance ().getTemplate( spb_id );
					L2ItemInstance spb = player.getInventory().findItemByItemId( spb_id.intValue() );
					if ( spb == null )
					{
						//Haven't spellbook
						player.sendPacket( new SystemMessage( SystemMessage.ITEM_MISSING_TO_LEARN_SKILL ) );
						return;
					}
                    //ok
                    player.removeItemFromInventory(spb,1);
				}
			}
			player.addSkill(skill);
			if (Config.DEBUG) _log.fine("Learned skill "+_id+" for "+_requiredSp+" SP.");
			player.setSp(player.getSp() - _requiredSp);
			player.updateStats();
			player.sendPacket(new StatusUpdate(player.getObjectId()).addAttribute(StatusUpdate.SP, player.getSp()));

			player.sendPacket(new SystemMessage(SystemMessage.LEARNED_SKILL_S1).addSkillName(_id));
			
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
			player.sendPacket(new SystemMessage(SystemMessage.NOT_ENOUGH_SP_TO_LEARN_SKILL));
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
