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

import java.util.logging.Logger;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.ClientScheduler;
import net.sf.l2j.gameserver.NpcTable;
import net.sf.l2j.gameserver.handler.IItemHandler;
import net.sf.l2j.gameserver.model.Experience;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2PcInstance;
import net.sf.l2j.gameserver.model.L2PetInstance;
import net.sf.l2j.gameserver.model.L2PlayableInstance;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.serverpackets.MagicSkillLaunched;
import net.sf.l2j.gameserver.serverpackets.MagicSkillUser;
import net.sf.l2j.gameserver.serverpackets.PetInfo;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;
/**
 * This class ...
 * 
 * @version $Revision: 1.9.2.9.2.10 $ $Date: 2005/03/31 09:19:39 $
 */

public class PetSummon implements IItemHandler
{
    protected static Logger _log = Logger.getLogger(PetSummon.class.getName());
	
	// all the items ids that this handler knowns
	private static final int[] _itemIds = { 2375, 3500, 3501, 3502, 4422, 4423, 4424 };

	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.handler.IItemHandler#useItem(net.sf.l2j.gameserver.model.L2PcInstance, net.sf.l2j.gameserver.model.L2ItemInstance)
	 */
	public void useItem(L2PlayableInstance playable, L2ItemInstance item)
	{
		if (!(playable instanceof L2PcInstance))
			return;
		L2PcInstance activeChar = (L2PcInstance)playable;
		int npcId;
		
		if (activeChar.getPet() != null)
		{
			if (Config.DEBUG) _log.fine("player has a pet already. ignore use item");
			return;
		}
        
        if (activeChar.isMounted())
            return;
		
		switch (item.getItemId())
		{
			// wolf pet a
			case 2375:
				npcId = 12077;
				break;
			
			// hatchling of wind
			case 3500:
				npcId = 12311;
				break;
				
			// hatchling of star
			case 3501:
				npcId = 12312;
				break;
				
			// hatchling of twilight
			case 3502:
				npcId = 12313;
				break;
			//  wind strider
			case 4422:
				npcId = 12526;
				break;
			//	Star strider
			case 4423:
				npcId = 12527;
				break;		
			// Twilight strider
			case 4424:
				npcId = 12528;
				break;	
				
			// unknown item id.. should never happen
			default:
				return;
		}
		
		
		L2NpcTemplate petTemplate = NpcTable.getInstance().getTemplate(npcId);
		

		L2PetInstance newpet = L2PetInstance.spawnPet(petTemplate, activeChar, item);
		if (newpet == null)
			return;
		newpet.setTitle(activeChar.getName());
		newpet.setExp(Experience.LEVEL[newpet.getLevel()]);
		if (!newpet.isRespawned()) {
			newpet.setCurrentHp(newpet.getMaxHp());
			newpet.setCurrentMp(newpet.getMaxMp());
		}
		newpet.setRunning();
		if (!newpet.isRespawned()) {
			newpet.store();
		}
		

        activeChar.setPet(newpet);

		MagicSkillUser msk = new MagicSkillUser(activeChar, 2046, 1, 1000, 600000);
		activeChar.sendPacket(msk);
		SystemMessage sm2 = new SystemMessage(547);
		activeChar.sendPacket (sm2);
        L2World.getInstance().storeObject(newpet);
		newpet.spawnMe(activeChar.getX()+50, activeChar.getY()+100, activeChar.getZ());
        PetInfo pInfo = new PetInfo(newpet);
        activeChar.sendPacket(pInfo);

		// continue execution in 1 seconds
		ClientScheduler.getInstance().scheduleLow(new SummonFinalizer(activeChar, newpet), 900);
	}
	
	static class SummonFinalizer implements Runnable
	{
		private L2PcInstance _activeChar;
		private L2PetInstance _newpet;
		
		SummonFinalizer(L2PcInstance activeChar, L2PetInstance newpet)
		{
			_activeChar = activeChar;
			_newpet = newpet;
		}
		
		public void run()
		{
			try
			{
				_activeChar.sendPacket(new MagicSkillLaunched(_activeChar,2046,1));
				
				_newpet.setFollowStatus(true);
				// addVisibleObject created the info packets with summon animation
				// if someone comes into range now, the animation shouldnt show any more
		        _newpet.setShowSummonAnimation(false);
			}
			catch (Throwable e)
			{
				_log.severe(e.toString());
			}
		}
	}
	
	public int[] getItemIds()
	{
		return _itemIds;
	}
}
