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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.concurrent.ScheduledFuture;
import java.util.logging.Logger;

import net.sf.l2j.Base64;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.Announcements;
import net.sf.l2j.gameserver.ClientScheduler;
import net.sf.l2j.gameserver.ClientThread;
import net.sf.l2j.gameserver.SevenSigns;
import net.sf.l2j.gameserver.model.L2Clan;
import net.sf.l2j.gameserver.model.L2PcInstance;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.quest.Quest;
import net.sf.l2j.gameserver.serverpackets.Die;
import net.sf.l2j.gameserver.serverpackets.HennaInfo;
import net.sf.l2j.gameserver.serverpackets.ItemList;
import net.sf.l2j.gameserver.serverpackets.PledgeShowMemberListAdd;
import net.sf.l2j.gameserver.serverpackets.PledgeShowMemberListAll;
import net.sf.l2j.gameserver.serverpackets.PledgeStatusChanged;
import net.sf.l2j.gameserver.serverpackets.ShortCutInit;
import net.sf.l2j.gameserver.serverpackets.SignsSky;
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
    protected ScheduledFuture _currentFuture;
    
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
		if(activeChar == null) return;
		activeChar.spawnMe(); // activeChar.getX(), activeChar.getY(), activeChar.getZ()
		
		if(SevenSigns.getInstance().isSealValidationPeriod())
			activeChar.sendPacket(new SignsSky());
		
		activeChar.sendPacket(new ItemList(activeChar, false));
		sendPacket(new UserInfo(activeChar));
		activeChar.getMacroses().sendUpdate();
		sendPacket(new ShortCutInit());
		sendPacket(new SystemMessage(SystemMessage.WELCOME_TO_THE_WORLD_OF_LINEAGE_II));
		sendPacket(new HennaInfo(activeChar));

        SystemMessage sm = new SystemMessage(614);
        sm.addString(getText("VGhpcyBTZXJ2ZXIgaXMgcnVubmluZyBMMko="));
        sm.addString(getText("IHZlcnNpb24gMyBkZXYvdW5zdGFibGU="));
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
        SevenSigns.getInstance().sendCurrentPeriodMsg(activeChar);

		Quest.playerEnter(activeChar);

        // send user info again .. just like the real client
        sendPacket(new UserInfo(activeChar));

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

		//add char to online characters
		activeChar.setOnlineStatus(true);

		activeChar.jbf.execp("on-player-start");
		
		activeChar.restoreEffects();

		notifyFriends(activeChar);
		notifyClanMembers(activeChar);
		_currentFuture = ClientScheduler.getInstance().scheduleLowAtFixedRate(new WarnUserTakeBreak(activeChar), 7200000, 7200000);
	}

	/**
	 * @param activeChar
	 */
	private void notifyFriends(L2PcInstance cha)
	{
		java.sql.Connection con = null;
		try 
		{
		    con = L2DatabaseFactory.getInstance().getConnection();
		    PreparedStatement statement;
		    statement = con.prepareStatement("SELECT char_id,friend_name from character_friends where char_id=? or friend_name=?");
		    statement.setInt(1, cha.getObjectId());
		    statement.setString(2, cha.getName());
		    ResultSet rset = statement.executeQuery();
		    while (rset.next())
		    {
				int objectId = rset.getInt("char_id");
				String name = rset.getString("friend_name");
				L2PcInstance friend;
				if (cha.getObjectId() != objectId)
				    friend = (L2PcInstance)L2World.findObject(objectId);
				else 
				    friend = L2World.getPlayer(name);
				if (friend != null)
				{
			    	//friend logged in.
				    friend.sendPacket(new SystemMessage(SystemMessage.S1_FRIEND_HAS_LOGGED_IN).addString(cha.getName()));
				}
		    }
		} 
		catch (Exception e) {_log.warning("could not restore friend data:"+e);} 
		finally { try {con.close();} catch (Exception e){}; }
		cha.updateEffectIcons();
	}

	/**
	 * @param activeChar
	 */
	private void notifyClanMembers(L2PcInstance activeChar)
	{
		L2Clan clan = activeChar.getClan();
		if(clan == null)
			return;

		clan.getClanMember(activeChar.getName()).setPlayerInstance(activeChar);
		if(activeChar.isClanLeader())
			activeChar.setClanPrivileges(L2Clan.CP_ALL);

		SystemMessage msg = new SystemMessage(SystemMessage.CLAN_MEMBER_S1_HAS_LOGGED_INTO_GAME).addString(activeChar.getName());
		PledgeShowMemberListAdd memberAdd = new PledgeShowMemberListAdd(activeChar);
		for(L2PcInstance clanMember : clan.getOnlineMembers(activeChar.getName()))
		{
			clanMember.sendPacket(memberAdd);
			clanMember.sendPacket(msg);
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
    public class WarnUserTakeBreak implements Runnable
    {
        L2PcInstance Char = null;
        public WarnUserTakeBreak(L2PcInstance player)
        {
            Char = player;
        }
        public void run()
        {
            if (Char.isOnline() == 1)
                Char.sendPacket(new SystemMessage(SystemMessage.YOU_HAVE_BEEN_PLAYING_FOR_AN_EXTENDED_PERIOD_OF_TIME_PLEASE_CONSIDER_TAKING_A_BREAK));
            else
                _currentFuture.cancel(false);
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
