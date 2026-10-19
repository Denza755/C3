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
package net.sf.l2j.gameserver.serverpackets;


import net.sf.l2j.gameserver.model.L2Clan;
import net.sf.l2j.gameserver.model.L2PcInstance;


/**
 */
public class AllyInfo extends ServerBasePacket
{
	//private static Logger _log = Logger.getLogger(AllyInfo.class.getName());
	private static final String _S__7A_FRIENDLIST = "[S] 7a AllyInfo";
	private static L2PcInstance _cha ;

	public AllyInfo(L2PcInstance cha)
	{
		_cha=cha;
	}	

	final void runImpl()
	{
		// no long-running tasks
	}
	
	final void writeImpl()
	{
	    SystemMessage sm;
	    L2PcInstance activeChar = getClient().getActiveChar();
	    if (activeChar.getAlliance() == null){
		return;
	    }

	    _cha.sendPacket(new SystemMessage(SystemMessage._ALLIANCE_INFORMATION_));
		_cha.sendPacket(new SystemMessage(SystemMessage.ALLIANCE_NAME_S1).addString(_cha.getClan().getAlliance().getAllyName()));
		int clancount = 0;
		L2Clan leaderclan = _cha.getAlliance().getLeader();
		clancount = leaderclan.getAlliance().getMembers().length;
		int[] online = new int[clancount + 1];
		int[] count = new int[clancount + 1];
		L2Clan[] clans = _cha.getAlliance().getMembers();
		for(int i = 0; i < clancount; i++)
		{
			online[i+1] = clans[i].getOnlineMembers(null).length;
			count[i+1] = clans[i].getMembers().length;
			online[0] += online[i+1];
			count[0] += count[i+1];
		}
		//Connection 
		sm = new SystemMessage(SystemMessage.CONNECTION_S1_TOTAL_S2);
		sm.addNumber(online[0]);
		sm.addNumber(count[0]);
		_cha.sendPacket(sm);
		sm = new SystemMessage(SystemMessage.ALLIANCE_LEADER_S2_OF_S1);
        sm.addString(leaderclan.getName());
		sm.addString(leaderclan.getAlliance().getAllyLeaderName());
		_cha.sendPacket(sm);
		//clan count
		_cha.sendPacket(new SystemMessage(SystemMessage.AFFILIATED_CLANS_TOTAL_S1_CLAN_S).addNumber(clancount));
		_cha.sendPacket(new SystemMessage(SystemMessage._CLAN_INFORMATION_));
		for (int i = 0; i < clancount; i++){
			_cha.sendPacket(new SystemMessage(SystemMessage.CLAN_NAME_S1).addString(clans[i].getName()));
			_cha.sendPacket(new SystemMessage(SystemMessage.CLAN_LEADER_S1).addString(clans[i].getLeaderName()));
			_cha.sendPacket(new SystemMessage(SystemMessage.CLAN_LEVEL_S1).addNumber(clans[i].getLevel()));
			sm = new SystemMessage(SystemMessage.CONNECTION_S1_TOTAL_S2);
			sm.addNumber(online[i+1]);
			sm.addNumber(count[i+1]);
			_cha.sendPacket(sm);
			_cha.sendPacket(new SystemMessage(SystemMessage.__DASHES__));
		}
		_cha.sendPacket(new SystemMessage(SystemMessage.__EQUALS__));
	}
	
	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.serverpackets.ServerBasePacket#getType()
	 */
	public String getType()
	{
		return _S__7A_FRIENDLIST;
	}
}
