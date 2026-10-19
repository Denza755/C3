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

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.logging.Logger;

import net.sf.l2j.Base64;
import net.sf.l2j.gameserver.Announcements;
import net.sf.l2j.gameserver.ClientThread;
import net.sf.l2j.gameserver.model.L2Clan;
import net.sf.l2j.gameserver.model.L2PcInstance;
import net.sf.l2j.gameserver.model.quest.Quest;
import net.sf.l2j.gameserver.serverpackets.Die;
import net.sf.l2j.gameserver.serverpackets.ItemList;
import net.sf.l2j.gameserver.serverpackets.PledgeShowMemberListAll;
import net.sf.l2j.gameserver.serverpackets.PledgeStatusChanged;
import net.sf.l2j.gameserver.serverpackets.ShortCutInit;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.serverpackets.UserInfo;

/**
 * Enter World Packet Handler<p>
 * <p>
 * 0000: 03 <p>
 * <p>
 * 
 * @version $Revision: 1.16.2.1.2.7 $ $Date: 2005/03/29 23:15:33 $
 */
public class EnterWorld extends ClientBasePacket
{
	private static final String _C__03_ENTERWORLD = "[C] 03 EnterWorld";
	private static Logger _log = Logger.getLogger(EnterWorld.class.getName());

	// c

	/**
	 * @param decrypt
	 */
	public EnterWorld(ByteBuffer buf, ClientThread client)
	{
		super(buf, client);
		// this is just a trigger packet. it has no content
	}

	void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		activeChar.spawnMe(activeChar.getX(), activeChar.getY(), activeChar.getZ());
		
        ItemList il = new ItemList(activeChar, false);
        activeChar.sendPacket(il);

        UserInfo ui = new UserInfo(activeChar);
        sendPacket(ui);

		activeChar.getMacroses().sendUpdate();

		ShortCutInit sci = new ShortCutInit();
		sendPacket(sci);

        SystemMessage sm = new SystemMessage(34);
        sendPacket(sm);
        
        sm = new SystemMessage(614);
        sm.addString(getText("VGhpcyBTZXJ2ZXIgaXMgcnVubmluZyBMMko="));
        sm.addString(getText("IHZlcnNpb24gMy4wLjA="));
        sendPacket(sm);
        sm = new SystemMessage(614);
        sm.addString(getText("Y3JlYXRlZCBieSBMMkNoZWYgYW5kIHRoZQ=="));
        sm.addString(getText("IEwySiB0ZWFtLg=="));
        sendPacket(sm);
        sm = new SystemMessage(614);
        sm.addString(getText("dmlzaXQgIGwyai5zb3VyY2Vmb3JnZS5uZXQ="));
        sm.addString(getText("ICBmb3Igc3VwcG9ydC4="));
        sendPacket(sm);

        Announcements.getInstance().showAnnouncements(activeChar);

		Quest.playerEnter(activeChar);

        // send user info again .. just like the real client
        sendPacket(ui);

        if (activeChar.getClanId() != 0 && activeChar.getClan() != null)
        {
        	sendPacket( new PledgeShowMemberListAll(activeChar.getClan(), activeChar) );
        	sendPacket( new PledgeStatusChanged(activeChar.getClan()));
        }
        
		if (activeChar.isAlikeDead())
		{
			// no broadcast needed since the player will already spawn dead to others
			activeChar.sendPacket(new Die(activeChar));
		}

		notifyClanMembers(activeChar);

		//add char to online characters
		activeChar.setOnlineStatus(true);
	}

	/**
	 * @param activeChar
	 */
	private void notifyClanMembers(L2PcInstance activeChar)
	{
		L2Clan clan = activeChar.getClan();
		if (clan != null)
		{
			clan.getClanMember(activeChar.getName()).setPlayerInstance(activeChar);
			SystemMessage msg = new SystemMessage(SystemMessage.CLAN_MEMBER_S1_LOGGED_IN);
			msg.addString(activeChar.getName());

			L2PcInstance[] clanMembers = clan.getOnlineMembers(activeChar.getName());
			for (int i = 0; i < clanMembers.length; i++)
			{
				clanMembers[i].sendPacket(msg);
			}
		}
	}

	/**
	 * @param string
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	private String getText(String string)
	{
		try {
			String result = new String(Base64.decode(string), "UTF-8"); 
			return result;
		} catch (UnsupportedEncodingException e) {
			// huh, UTF-8 is not supported? :)
			return null;
		}
	}

	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.clientpackets.ClientBasePacket#getType()
	 */
	public String getType()
	{
		return _C__03_ENTERWORLD;
	}
}
