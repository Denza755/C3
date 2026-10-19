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

import java.nio.ByteBuffer;
//import java.util.logging.Logger;

import net.sf.l2j.gameserver.ClientThread;
import net.sf.l2j.gameserver.model.L2PcInstance;
import net.sf.l2j.gameserver.model.L2Clan;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;

public class AllyLeave extends ClientBasePacket
{
    private static final String _C__84_ALLYLEAVE = "[C] 84 AllyLeave";
//    private static Logger _log = Logger.getLogger(AllyLeave.class.getName());
    
    public AllyLeave(ByteBuffer buf, ClientThread client)
    {
        super(buf, client);
    }
    
    void runImpl()
    {
        L2PcInstance player = getClient().getActiveChar();
		if(player == null) return;
        L2Clan clan = player.getClan();
        if(clan == null)
            return;
        
        if(!player.isClanLeader())
        {
            player.sendPacket(new SystemMessage(SystemMessage.ONLY_THE_CLAN_LEADER_MAY_APPLY_FOR_WITHDRAWAL_FROM_THE_ALLIANCE).addString(player.getName()));
            return;
        }
        
        if(clan.getAlliance() == null)
        {
            player.sendPacket(new SystemMessage(SystemMessage.NO_CURRENT_ALLIANCES));
            return;
        }
        
        if(clan == clan.getAlliance().getLeader())
        {
            player.sendPacket(new SystemMessage(SystemMessage.ALLIANCE_LEADERS_CANNOT_WITHDRAW));
            return;
        }
        
        clan.setAllyId(0);
		clan.getAlliance().removeAllyMember(clan.getName());
        clan.updateClanInDB();
		for(L2PcInstance temp : clan.getOnlineMembers(null))
		{
			temp.setAlliance(null);
			temp.broadcastUserInfo();
		}
		clan.broadcastClanStatus();
        player.sendPacket(new SystemMessage(SystemMessage.YOU_HAVE_WITHDRAWN_FROM_THE_ALLIANCE));
        
    }
    
    public String getType()
    {
        return _C__84_ALLYLEAVE;
    }
}