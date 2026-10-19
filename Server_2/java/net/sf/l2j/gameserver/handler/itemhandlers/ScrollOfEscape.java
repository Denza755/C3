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
import net.sf.l2j.gameserver.MapRegionTable;
import net.sf.l2j.gameserver.SkillTable;
import net.sf.l2j.gameserver.ai.CtrlIntention;
import net.sf.l2j.gameserver.handler.IItemHandler;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2PcInstance;
import net.sf.l2j.gameserver.model.L2PlayableInstance;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.Location;
import net.sf.l2j.gameserver.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.serverpackets.MagicSkillUser;
import net.sf.l2j.gameserver.serverpackets.SetupGauge;
import net.sf.l2j.gameserver.serverpackets.StopMove;

/**
 * This class ...
 * 
 * @version $Revision: 1.2.2.3.2.5 $ $Date: 2005/03/27 15:30:07 $
 */

public class ScrollOfEscape implements IItemHandler
{
	// all the items ids that this handler knowns
	private static int[] _itemIds = { 736 };

	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.handler.IItemHandler#useItem(net.sf.l2j.gameserver.model.L2PcInstance, net.sf.l2j.gameserver.model.L2ItemInstance)
	 */
	public void useItem(L2PlayableInstance playable, L2ItemInstance item)
	{
		if (!(playable instanceof L2PcInstance))
			return;
		L2PcInstance activeChar = (L2PcInstance)playable;
		activeChar.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
        //SoE Animation section
		activeChar.setTarget(activeChar);
		L2Skill skill = SkillTable.getInstance().getInfo(1050, 1);
		MagicSkillUser msk = new MagicSkillUser(activeChar, 1050, 1,20000,0);
		activeChar.sendPacket(msk);
		activeChar.broadcastPacket(msk);
		SetupGauge sg = new SetupGauge(0, skill.getSkillTime());
		activeChar.sendPacket(sg);
		//End SoE Animation section
		
		activeChar.removeItemFromInventory(item, 1);
		EscapeFinalizer ef = new EscapeFinalizer(activeChar);
		// continue execution later
		ClientScheduler.getInstance().scheduleLow(ef, skill.getSkillTime() - 200);
	}
	
	static class EscapeFinalizer implements Runnable
	{
		private L2PcInstance _activeChar;
		
		EscapeFinalizer(L2PcInstance activeChar)
		{
			_activeChar = activeChar;
		}
		
		public void run()
		{
			try {
				Location townCords = MapRegionTable.getInstance().getClosestTownCords(_activeChar);
				//Packets and other updates, teleport also
				StopMove sm = new StopMove(_activeChar);
				_activeChar.sendPacket(sm);
				_activeChar.broadcastPacket(sm);
	
				ActionFailed af = new ActionFailed();
				_activeChar.sendPacket(af);
				
				_activeChar.teleToLocation(townCords.getX(), townCords.getY(), townCords.getZ());
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
