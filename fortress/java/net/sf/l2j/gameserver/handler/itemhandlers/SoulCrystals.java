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
package net.sf.l2j.gameserver.handler.itemhandlers;

import net.sf.l2j.gameserver.ClientScheduler;
import net.sf.l2j.gameserver.SkillTable;
import net.sf.l2j.gameserver.handler.IItemHandler;
import net.sf.l2j.gameserver.model.L2Attackable;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2MonsterInstance;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2PcInstance;
import net.sf.l2j.gameserver.model.L2PlayableInstance;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;

/**
 * This class ...
 * 
 * @version $Revision: 1.2.4 $ $Date: 2005/08/14 21:31:07 $
 */

public class SoulCrystals implements IItemHandler
{
	// First line is for Red Soul Crystals, second is Green and third is Blue Soul Crystals,
	// ordered by ascending level, from 0 to 12... (There's also a 13th lvl, but it's still unused in retail) 
	private static int[] _itemIds = { 4629, 4630, 4631, 4632, 4633, 4634, 4635, 4636, 4637, 4638, 4639, 5577, 5580,
									  4640, 4641, 4642, 4643, 4644, 4645, 4646, 4647, 4648, 4649, 4650, 5578, 5581,
									  4651, 4652, 4653, 4654, 4655, 4656, 4657, 4658, 4659, 4660, 4661, 5579, 5582};
	
	// Our main method, where everything goes on
	public void useItem(L2PlayableInstance playable, L2ItemInstance item)
	{
		if (!(playable instanceof L2PcInstance))
			return;
		
		L2PcInstance activeChar = (L2PcInstance)playable;
		L2Object target = activeChar.getTarget();

		if (!(target instanceof L2MonsterInstance))
		{
			// Send a System Message to the caster
			activeChar.sendPacket(new SystemMessage(SystemMessage.INCORRECT_TARGET));
			
			// Send a Server->Client packet ActionFailed to the L2PcInstance 
            activeChar.sendPacket(new ActionFailed());
			
            return;
		}
        
        // u can use soul crystal only when target hp goest to <50%
        if(((L2MonsterInstance)target).getCurrentHp() > ((L2MonsterInstance)target).getMaxHp()/2)
        {
            activeChar.sendPacket(new SystemMessage(SystemMessage.THE_SOUL_CRYSTAL_WAS_NOT_ABLE_TO_ABSORB_A_SOUL));
            activeChar.sendPacket(new ActionFailed());
            return;
        }
        
		int crystalId = item.getItemId();

        // Soul Crystal Casting section
        L2Skill skill = SkillTable.getInstance().getInfo(2096, 1);
        activeChar.useMagic(skill, false, true);
        // End Soul Crystal Casting section
        
        activeChar.disableAllSkills(skill.getSkillTime());
        
        // Continue execution later
        ClientScheduler.getInstance().scheduleLow(new CrystalFinalizer(activeChar, target, crystalId), skill.getSkillTime());
		
	}

	static class CrystalFinalizer implements Runnable
	{
		private L2PcInstance _activeChar;
		private L2Attackable _target;
		private int _crystalId;
		
		CrystalFinalizer(L2PcInstance activeChar, L2Object target, int crystalId)
		{
		    _activeChar = activeChar;
		    _target = (L2Attackable)target;
		    _crystalId = crystalId;
		}
		
		public void run()
		{
        	if (_activeChar.isDead() || _target.isDead()) 
                return; 
            try {
            	_target.addAbsorber(_activeChar, _crystalId);
            	_activeChar.setTarget(_target);
            } catch (Throwable e) {
                e.printStackTrace();
            }
		}
	}

	public int[] getItemIds()
	{
		return _itemIds;
	}
}