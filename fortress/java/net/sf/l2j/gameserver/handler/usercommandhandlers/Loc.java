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

import net.sf.l2j.gameserver.MapRegionTable;
import net.sf.l2j.gameserver.handler.IUserCommandHandler;
import net.sf.l2j.gameserver.model.L2PcInstance;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;

/**
 * 
 *
 */
public class Loc implements IUserCommandHandler
{
    private static final int[] COMMAND_IDS = { 0 }; 

    /* (non-Javadoc)
     * @see net.sf.l2j.gameserver.handler.IUserCommandHandler#useUserCommand(int, net.sf.l2j.gameserver.model.L2PcInstance)
     */
    public boolean useUserCommand(@SuppressWarnings("unused") int id, L2PcInstance activeChar)
    {
    	int _nearestTown=MapRegionTable.getInstance().getClosestTownNumber(activeChar);
    	int msg = 0;
    	switch (_nearestTown)
    	{
    	case 0: msg = SystemMessage.CURRENT_LOCATION__S1_S2_S3_NEAR_TALKING_ISLAND_VILLAGE; break;
    	case 1: msg = SystemMessage.CURRENT_LOCATION__S1_S2_S3_NEAR_ELVEN_VILLAGE; break;
    	case 2: msg = SystemMessage.CURRENT_LOCATION__S1_S2_S3_NEAR_DARK_ELVEN_VILLAGE; break;
    	case 3: msg = SystemMessage.CURRENT_LOCATION__S1_S2_S3_NEAR_ORC_VILLAGE; break;
    	case 4: msg = SystemMessage.CURRENT_LOCATION__S1_S2_S3_NEAR_DWARVEN_VILLAGE; break;
    	case 5: msg = SystemMessage.CURRENT_LOCATION__S1_S2_S3_NEAR_GLUDIO_CASTLE_TOWN; break;
    	case 6: msg = SystemMessage.CURRENT_LOCATION__S1_S2_S3_NEAR_GLUDIN_VILLAGE; break;
    	case 7: msg = SystemMessage.CURRENT_LOCATION__S1_S2_S3_NEAR_DION_CASTLE_TOWN; break;
    	case 8: msg = SystemMessage.CURRENT_LOCATION__S1_S2_S3_NEAR_GIRAN_CASTLE_TOWN; break;
    	case 9: msg = SystemMessage.CURRENT_LOCATION__S1_S2_S3_NEAR_THE_TOWN_OF_OREN; break;
    	case 10: msg = SystemMessage.CURRENT_LOCATION__S1_S2_S3_NEAR_ADEN_CASTLE_TOWN; break;
    	case 11: msg = SystemMessage.CURRENT_LOCATION__S1_S2_S3_NEAR_HUNTERS_VILLAGE; break;
    	case 12: msg = SystemMessage.CURRENT_LOCATION__S1_S2_S3_NEAR_GIRAN_HARBOR; break;
    	case 13: msg = SystemMessage.CURRENT_LOCATION__S1_S2_S3_NEAR_HEINE; break;
    	default: msg = SystemMessage.CURRENT_LOCATION__S1_S2_S3_NEAR_ADEN_CASTLE_TOWN;
    	}
        SystemMessage sm = new SystemMessage(msg);
        sm.addNumber(activeChar.getX());
        sm.addNumber(activeChar.getY());
        sm.addNumber(activeChar.getZ());
        activeChar.sendPacket(sm);
        return true;
    }

    /* (non-Javadoc)
     * @see net.sf.l2j.gameserver.handler.IUserCommandHandler#getUserCommandList()
     */
    public int[] getUserCommandList()
    {
        return COMMAND_IDS;
    }
}
