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

import net.sf.l2j.gameserver.ClientThread;
import net.sf.l2j.gameserver.GameTimeController;
import net.sf.l2j.gameserver.model.L2Party;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.entity.SevenSignsFestival;
import net.sf.l2j.gameserver.packets.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.packets.serverpackets.LeaveWorld;
import net.sf.l2j.gameserver.packets.serverpackets.SystemMessage;

import java.nio.ByteBuffer;

/**
 * This class ...
 * 
 * @version $Revision: 1.9.4.3 $ $Date: 2005/03/27 15:29:30 $
 */
public class Logout extends ClientBasePacket
{
    private static final String _C__09_LOGOUT = "[C] 09 Logout";
    
    // c

    /**
     * @param decrypt
     */
    public Logout(ByteBuffer buf, ClientThread client)
    {
        super(buf, client);
        // this is just a trigger packet. it has no content
    }

    void runImpl()
    {
        // Dont allow leaving if player is fighting
        L2PcInstance player = getClient().getActiveChar();
        
        if (player == null)
            return;
	
	player.getInventory().updateDatabase();

        if(GameTimeController.getGameTicks() - player.getAttackEndTime() < 15 * GameTimeController.TICKS_PER_SECOND  || player.isAttacked())
        {
            player.sendPacket(new SystemMessage(SystemMessage.CANT_LOGOUT_WHILE_FIGHTING));
            player.sendPacket(new ActionFailed());
            return;
        }
        
        if(player.atEvent) {
            player.sendPacket(SystemMessage.sendString("A superior power doesn't allow you to leave the event"));
            return;
        }
   
     
        // Prevent player from logging out if they are a festival participant
        // and it is in progress, otherwise notify party members that the player
        // is not longer a participant.
        if (player.isFestivalParticipant()) {
            if (SevenSignsFestival.getInstance().isFestivalInitialized()) 
            {
            	player.sendMessage("You cannot log out while you are a participant in a festival.");
            }
            L2Party playerParty = player.getParty();
            
            if (playerParty != null)
                player.getParty().broadcastToPartyMembers(SystemMessage.sendString(player.getName() + " has been removed from the upcoming festival."));
        }
        
        player.deleteMe();
        //save character
        ClientThread.saveCharToDisk(player);
        
        // normally the server would send serveral "delete object" before "leaveWorld"
        // we skip that for now
        
        sendPacket(new LeaveWorld());
    }

    /* (non-Javadoc)
     * @see net.sf.l2j.gameserver.clientpackets.ClientBasePacket#getType()
     */
    public String getType()
    {
        return _C__09_LOGOUT;
    }
}
