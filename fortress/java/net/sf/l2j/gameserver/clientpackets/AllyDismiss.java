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
import net.sf.l2j.gameserver.model.L2Alliance;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.ClanTable;

public class AllyDismiss extends ClientBasePacket
{
    private static final String _C__85_ALLYDISMISS = "[C] 85 AllyDismiss";
//    private static Logger _log = Logger.getLogger(AllyDismiss.class.getName());
    
    String _clanName;
    
    public AllyDismiss(ByteBuffer buf, ClientThread client)
    {
        super(buf, client);
        _clanName = readS();
    }
    
    void runImpl()
	{
		L2PcInstance player = getClient().getActiveChar();
		if(player == null) return;

		L2Clan leaderClan = player.getClan();
		if(leaderClan == null)
			return;
		L2Alliance alliance = player.getAlliance();
		L2Clan clan;

		if(alliance == null)
		{
			//no current alliance
			player.sendPacket(new SystemMessage(SystemMessage.NO_CURRENT_ALLIANCES));
			return;
		}

		if(!player.isAllyLeader())
		{
			player.sendPacket(new SystemMessage(SystemMessage.FEATURE_AVAILABLE_TO_ALLIANCE_LEADERS_ONLY));
			return;
		}

		if(_clanName == null)
			return;

		clan = ClanTable.getInstance().getClanByName(_clanName);

		if(clan != null)
		{
			if(!alliance.isMember(clan.getName()))
				return;

			clan.setAllyId(0);
			clan.updateClanInDB();
			alliance.removeAllyMember(_clanName);
			for(L2PcInstance temp : clan.getOnlineMembers(""))
			{
				temp.setAlliance(null);
				temp.broadcastUserInfo();
			}
			clan.broadcastClanStatus();
			player.sendMessage(clan.getName() + " has been dismissed from " + alliance.getAllyName());
		}
	}
    
    public String getType()
    {
        return _C__85_ALLYDISMISS;
    }
}