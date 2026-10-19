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
package net.sf.l2j.gameserver.handler.usercommandhandlers;

import net.sf.l2j.gameserver.ClientScheduler;
import net.sf.l2j.gameserver.MapRegionTable;
import net.sf.l2j.gameserver.handler.IUserCommandHandler;
import net.sf.l2j.gameserver.model.L2PcInstance;
import net.sf.l2j.gameserver.model.Location;
import net.sf.l2j.gameserver.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.serverpackets.MagicSkillUser;
import net.sf.l2j.gameserver.serverpackets.SetupGauge;
import net.sf.l2j.gameserver.serverpackets.StopMove;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;

/**
 * 
 *
 */
public class Escape implements IUserCommandHandler
{
    private static final int[] COMMAND_IDS = { 52 }; 
	private static final int REQUIRED_LEVEL = net.sf.l2j.Config.GM_ESCAPE;

    /* (non-Javadoc)
     * @see net.sf.l2j.gameserver.handler.IUserCommandHandler#useUserCommand(int, net.sf.l2j.gameserver.model.L2PcInstance)
     */
    public boolean useUserCommand(int id, L2PcInstance activeChar)
    {
		int _unstuckTimer = (activeChar.getAccessLevel() >=REQUIRED_LEVEL? 5000 : 300000 );
		SystemMessage sm = new SystemMessage(614);
		sm.addString("After " + _unstuckTimer/60000 + " min. you be returned to near village.");
		activeChar.sendPacket(sm);
        //SoE Animation section
        activeChar.setTarget(activeChar);
        MagicSkillUser msk = new MagicSkillUser(activeChar, 1050, 1, _unstuckTimer, 0);
        activeChar.sendPacket(msk);
        activeChar.broadcastPacket(msk);
        SetupGauge sg = new SetupGauge(0, _unstuckTimer );
        activeChar.sendPacket(sg);
        //End SoE Animation section
		activeChar.disableAllSkills();
        EscapeFinalizer ef = new EscapeFinalizer(activeChar);
		// continue execution later
		ClientScheduler.getInstance().scheduleLow(ef, _unstuckTimer  - 200);
		return true;
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
				_activeChar.enableAllSkills();
		        _activeChar.teleToLocation(townCords.getX(), townCords.getY(), townCords.getZ());
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}
	}
	
    /* (non-Javadoc)
     * @see net.sf.l2j.gameserver.handler.IUserCommandHandler#getUserCommandList()
     */
    public int[] getUserCommandList()
    {
        return COMMAND_IDS;
    }
}
