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

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.ClientScheduler;
import net.sf.l2j.gameserver.ai.CtrlIntention;
import net.sf.l2j.gameserver.handler.IUserCommandHandler;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.packets.serverpackets.MagicSkillUser;
import net.sf.l2j.gameserver.packets.serverpackets.SetupGauge;
import net.sf.l2j.gameserver.tables.MapRegionTable;
import net.sf.l2j.gameserver.util.Broadcast;

/**
 * 
 *
 */
public class Escape implements IUserCommandHandler
{
    private static final int[] COMMAND_IDS = { 52 }; 
 private static final int REQUIRED_LEVEL = Config.GM_ESCAPE;

    /* (non-Javadoc)
     * @see net.sf.l2j.gameserver.handler.IUserCommandHandler#useUserCommand(int, net.sf.l2j.gameserver.model.L2PcInstance)
     */
    public boolean useUserCommand(@SuppressWarnings("unused") int id, L2PcInstance activeChar)
    {   
        if (activeChar.isMovementDisabled() || activeChar.isMuted() || activeChar.isAlikeDead())
            return false;
        
        int unstuckTimer = (activeChar.getAccessLevel() >=REQUIRED_LEVEL? 5000 : Config.UNSTUCK_INTERVAL*1000 );
        
        //if (Config.ALT_LANGUAGE) {activeChar.sendMessage("Sau " + unstuckTimer/60000 + " ph\u00FAt. \u0042\u1EA1n s\u1EBD \u0111\u01B0\u1EE3\u0063 qu\u0061y v\u1EC1 l\u00E0ng g\u1EA7n nh\u1EA5t.");}
        //else {activeChar.sendMessage("After " + unstuckTimer/60000 + " min. you be returned to near village.");}
        
        activeChar.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
        //SoE Animation section
        activeChar.setTarget(activeChar);
        activeChar.disableAllSkills();

        Broadcast.toSelfAndKnownPlayers(activeChar, new MagicSkillUser(activeChar, 1050, 1, unstuckTimer, 0));
        activeChar.sendPacket(new SetupGauge(0, unstuckTimer));
        //End SoE Animation section
        // continue execution later
        ClientScheduler.getInstance().scheduleLow(new EscapeFinalizer(activeChar), unstuckTimer);
        
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
            if (_activeChar.isDead()) 
                return; 
            _activeChar.enableAllSkills();
            
            try 
            {
                _activeChar.teleToLocation(MapRegionTable.TeleportWhereType.Town);
            } catch (Throwable e) { e.printStackTrace(); }
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
