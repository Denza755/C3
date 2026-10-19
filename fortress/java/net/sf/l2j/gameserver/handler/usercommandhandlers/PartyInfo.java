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

import net.sf.l2j.gameserver.handler.IUserCommandHandler;
import net.sf.l2j.gameserver.model.L2PcInstance;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.model.L2Party;

/**
 * Support for /partyinfo command  
 * Added by Tempy - 28 Jul 05
 */
public class PartyInfo implements IUserCommandHandler
{
    private static final int[] COMMAND_IDS = { 81 }; 
	
    /* (non-Javadoc)
     * @see net.sf.l2j.gameserver.handler.IUserCommandHandler#useUserCommand(int, net.sf.l2j.gameserver.model.L2PcInstance)
     */
    public boolean useUserCommand(int id, L2PcInstance activeChar)
    {
        if (id != COMMAND_IDS[0]) return false;
        
    	if (!activeChar.isInParty())
    	{
    		activeChar.sendMessage("You are not in a party.");
    		return false;	
    	}
    	
        L2Party playerParty = activeChar.getParty();
    	int memberCount = playerParty.getMemberCount();
    	int lootDistribution = playerParty.getLootDistribution();
    	String partyLeader = playerParty.getPartyMembers().get(0).getName();
    	
        activeChar.sendPacket(new SystemMessage(SystemMessage._PARTY_INFORMATION_));
        
    		switch (lootDistribution) {
    		case L2Party.ITEM_LOOTER:
    			activeChar.sendPacket(new SystemMessage(SystemMessage.LOOTING_METHOD_FINDERS_KEEPERS));
    			break;
    		case L2Party.ITEM_ORDER:
                activeChar.sendPacket(new SystemMessage(SystemMessage.LOOTING_METHOD_BY_TURN));
    			break;
    		case L2Party.ITEM_ORDER_SPOIL:
                activeChar.sendPacket(new SystemMessage(SystemMessage.LOOTING_METHOD_BY_TURN_INCLUDING_SPOIL));
    			break;
    		case L2Party.ITEM_RANDOM:
                activeChar.sendPacket(new SystemMessage(SystemMessage.LOOTING_METHOD_RANDOM));
    			break;
    		case L2Party.ITEM_RANDOM_SPOIL:
                activeChar.sendPacket(new SystemMessage(SystemMessage.LOOTING_METHOD_RANDOM_INCLUDING_SPOIL));
    			break;
    		}
    	
       	activeChar.sendPacket(new SystemMessage(SystemMessage.PARTY_LEADER_S1).addString(partyLeader));
    	activeChar.sendMessage("Members: " + memberCount + "/9");
    	activeChar.sendPacket(new SystemMessage(SystemMessage.__DASHES__));
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