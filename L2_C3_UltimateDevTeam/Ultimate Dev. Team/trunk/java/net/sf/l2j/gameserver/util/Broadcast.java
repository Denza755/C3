/*
 * $Header: Broadcast.java, 18/11/2005 15:33:35 luisantonioa Exp $
 *
 * $Author: luisantonioa $
 * $Date: 18/11/2005 15:33:35 $
 * $Revision: 1 $
 * $Log: Broadcast.java,v $
 * Revision 1  18/11/2005 15:33:35  luisantonioa
 * Added copyright notice
 *
 * 
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
package net.sf.l2j.gameserver.util;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.packets.serverpackets.ServerBasePacket;

import java.util.logging.Logger;

/**
 * This class ...
 * 
 * @version $Revision: 1.2 $ $Date: 2004/06/27 08:12:59 $
 */

public class Broadcast
{
    private static Logger _log = Logger.getLogger(Broadcast.class.getName());
    
    /**
     * Send a packet to all L2PcInstance in the _KnownPlayers of the L2Character that have the Character targetted.<BR><BR>
     *
     * <B><U> Concept</U> :</B><BR><BR>
     * L2PcInstance in the detection area of the L2Character are identified in <B>_knownPlayers</B>.
     * In order to inform other players of state modification on the L2Character, server just need to go through _knownPlayers to send Server->Client Packet<BR><BR>
     *
     * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method DOESN'T SEND Server->Client packet to this L2Character (to do this use function broadcastPacket)</B></FONT><BR><BR>
     *
     */
    public static final void toPlayersTargettingMyself(L2Character character, ServerBasePacket mov)
    {
        if (Config.DEBUG) _log.fine("players to notify:" + character.getKnownList().getKnownPlayers().size() + " packet:"+mov.getType());

        for (L2PcInstance player : character.getKnownList().getKnownPlayers())
        {
            if (player.getTarget() != character) continue;
            player.sendPacket(mov);
        }
    }

    /**
     * Send a packet to all L2PcInstance in the _KnownPlayers of the L2Character.<BR><BR>
     *
     * <B><U> Concept</U> :</B><BR><BR>
     * L2PcInstance in the detection area of the L2Character are identified in <B>_knownPlayers</B>.
     * In order to inform other players of state modification on the L2Character, server just need to go through _knownPlayers to send Server->Client Packet<BR><BR>
     *
     * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method DOESN'T SEND Server->Client packet to this L2Character (to do this use function broadcastPacket)</B></FONT><BR><BR>
     *
     */
    public static final void toKnownPlayers(L2Character character, ServerBasePacket mov)
    {
        if (Config.DEBUG) _log.fine("players to notify:" + character.getKnownList().getKnownPlayers().size() + " packet:"+mov.getType());

        for (L2PcInstance player : character.getKnownList().getKnownPlayers())    
            player.sendPacket(mov);
    }
    
    public static final void toSelfAndKnownPlayers(L2Character character, ServerBasePacket mov)
    {
        character.sendPacket(mov);
        toKnownPlayers(character, mov);
    }
    
    public static final void toAllOnlinePlayers(ServerBasePacket mov)
    {
        if (Config.DEBUG) _log.fine("Players to notify: " + L2World.getInstance().getAllPlayers().size() + " (with packet " + mov.getType() + ")");
        
        for (L2PcInstance onlinePlayer : L2World.getInstance().getAllPlayers())
            onlinePlayer.sendPacket(mov);
    }
}
