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

import net.sf.l2j.Base64;
import net.sf.l2j.Config;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.Announcements;
import net.sf.l2j.gameserver.ClientThread;
import net.sf.l2j.gameserver.LoginServerThread;
import net.sf.l2j.gameserver.model.L2Clan;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.entity.L2Event;
import net.sf.l2j.gameserver.model.entity.SevenSigns;
import net.sf.l2j.gameserver.model.quest.Quest;
import net.sf.l2j.gameserver.packets.serverpackets.*;
import net.sf.l2j.gameserver.tables.GmListTable;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.logging.Logger;

/**
 * Enter World Packet Handler<p>
 * <p>
 * 0000: 03 <p>
 * packet format rev656 cbdddd  
 * <p>
 * 
 * @version $Revision: 1.16.2.1.2.7 $ $Date: 2005/03/29 23:15:33 $
 */
public class EnterWorld extends ClientBasePacket
{
	private static final String _C__03_ENTERWORLD = "[C] 03 EnterWorld";
	private static Logger _log = Logger.getLogger(EnterWorld.class.getName());

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
		
		if (activeChar == null)
		{
			_log.warning("EnterWorld failed! activeChar is null...");
		    return;
		}
/*
		if(L2World.getInstance().findObject(activeChar.getObjectId()) != null)
		{
			if(Config.DEBUG)
				_log.warning("User already exist in OID map! User "+activeChar.getName()+" is character clone");
			//activeChar.closeNetConnection();
		}
*/        
	if (!Config.NORMAL_NAME_COLOR.equalsIgnoreCase("none") && (activeChar.getAccessLevel() == 0)) {activeChar.setNameColor(Integer.decode("0x"+Config.NORMAL_NAME_COLOR));}
	if (!Config.VIP_NAME_COLOR.equalsIgnoreCase("none") && (activeChar.getAccessLevel() == Config.VIP_ACCESSLEVEL)) {activeChar.setNameColor(Integer.decode("0x"+Config.VIP_NAME_COLOR));}
	if (!Config.GM_NAME_COLOR.equalsIgnoreCase("none") && activeChar.isGM()) {activeChar.setNameColor(Integer.decode("0x"+Config.GM_NAME_COLOR));}

        if (activeChar.isGM() && Config.SHOW_GM_LOGIN) 
        { 
            String gmname = activeChar.getName(); 
            String text = "GM "+gmname+" has logged on."; 
            Announcements.getInstance().announceToAll(text); 
            GmListTable.getInstance().addGm(activeChar);
        }
        else if (activeChar.isGM() && !Config.SHOW_GM_LOGIN)
        {
            activeChar.setInvisible();
        }
        
        if (Config.PLAYER_SPAWN_PROTECTION > 0)
            activeChar.setProtection(true);
        
		activeChar.spawnMe(activeChar.getX(), activeChar.getY(), activeChar.getZ());
		if(L2Event.active && L2Event.connectionLossData.containsKey(activeChar.getName()) &&L2Event.isOnEvent(activeChar)){
            L2Event.restoreChar(activeChar);
        }
        else if(L2Event.connectionLossData.containsKey(activeChar.getName())){
            L2Event.restoreAndTeleChar(activeChar);
        }
		if (SevenSigns.getInstance().isSealValidationPeriod())
		{
			SignsSky ss = new SignsSky();
			activeChar.sendPacket(ss);
		}
		
        if (Config.STORE_SKILL_COOLTIME)
            activeChar.restoreEffects();
        
        if (activeChar.getAllEffects() != null)
        {
            for (L2Effect e : activeChar.getAllEffects())
            {
                if (e.getEffectType() == L2Effect.EffectType.HEAL_OVER_TIME)
                {
                    activeChar.stopEffects(L2Effect.EffectType.HEAL_OVER_TIME);
                    activeChar.removeEffect(e);
                }
                if (e.getEffectType() == L2Effect.EffectType.COMBAT_POINT_HEAL_OVER_TIME)
                {
                    activeChar.stopEffects(L2Effect.EffectType.COMBAT_POINT_HEAL_OVER_TIME);
                    activeChar.removeEffect(e);
                }
            }
        }
	
        activeChar.sendPacket(new ItemList(activeChar, false));
        activeChar.sendPacket(new UserInfo(activeChar));
        activeChar.getMacroses().sendUpdate();
        activeChar.sendPacket(new ShortCutInit());
        activeChar.sendPacket(new SystemMessage(34));
        activeChar.sendPacket(new HennaInfo(activeChar));
        SystemMessage sm;
	
        if (Config.SHOW_L2J_LICENSE)
        {
            sm = new SystemMessage(614);
            sm.addString(getText("UGhpw6puIGLhuqNuIFZp4buHdCBow7NhIGPhu6dhIGThu7Egw6FuIEwySi1WTi4="));
            sm.addString(getText("Tmjhuq1uIGjhu5cgdHLhu6MgdOG6oWkga8OqbmggI0wySi1WTiwgaXJjLmZyZWVub2RlLm5ldA=="));
            sendPacket(sm);
	    if (Config.SERVER_VERSION != null) {
            sm = new SystemMessage(614);
            sm.addString(getText("UGhpw6puIGLhuqNuOiA=")+"   "+Config.SERVER_VERSION);
            sendPacket(sm); }
        }
	else if (Config.SHOW_L2J_LICENSE)
	{
        sm = new SystemMessage(614);
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
        sm = new SystemMessage(614);
        sm.addString(getText("V2VsY29tZSB0byA="));
        sm.addString(LoginServerThread.getInstance().getServerName());
        sendPacket(sm);
        
        if (Config.SERVER_VERSION != null)
        {
            sm = new SystemMessage(614);
            sm.addString(getText("TDJKIFNlcnZlciBWZXJzaW9uOg==")+"   "+Config.SERVER_VERSION);
            sendPacket(sm);
            sm = new SystemMessage(614);
            sm.addString(getText("TDJKIFNlcnZlciBCdWlsZCBEYXRlOg==")+" "+Config.SERVER_BUILD_DATE);
            sendPacket(sm);
        }
        }

        if (Config.SHOW_HTML_WELCOME) {
        String welcome_Path = "data/html/welcome.htm";
        File mainText = new File(Config.DATAPACK_ROOT, welcome_Path);        // Return the pathfile of the HTML file
        if (mainText.exists())
        {   
            NpcHtmlMessage html = new NpcHtmlMessage(1);
            html.setFile(welcome_Path);
            sendPacket(html);
        } }
        
        Announcements.getInstance().showAnnouncements(activeChar);
	if (Config.SHOW_SS_PERIOD)
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

		if (Config.ALLOW_WATER)
		    activeChar.checkWaterState();
        

		//add char to online characters
		activeChar.setOnlineStatus(true);
		
        notifyFriends(activeChar);
		notifyClanMembers(activeChar);
        
        activeChar.onPlayerEnter();
	}
    
	/**
	 * @param activeChar
	 */
	private void notifyFriends(L2PcInstance cha)
	{
		java.sql.Connection con = null;
		try {
		    con = L2DatabaseFactory.getInstance().getConnection();
		    PreparedStatement statement;
		    statement = con.prepareStatement("SELECT char_id,friend_name from character_friends where char_id=? or friend_name=?");
		    statement.setInt(1, cha.getObjectId());
		    statement.setString(2, cha.getName());
		    ResultSet rset = statement.executeQuery();

            L2PcInstance friend;
            int objectId;
            String name;

            while (rset.next()){
                objectId = rset.getInt("char_id");
                name = rset.getString("friend_name");

                if (cha.getObjectId() != objectId)
                    friend = (L2PcInstance)L2World.getInstance().findObject(objectId);
                else 
                    friend = L2World.getInstance().getPlayer(name);

                
                if (friend != null) //friend logged in.
                    friend.sendPacket(new SystemMessage(503).addString(cha.getName()));
		    }
        } catch (Exception e) {
            _log.warning("could not restore friend data:"+e);
        } finally {
            try {con.close();} catch (Exception e){}
        }
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

		SystemMessage msg = new SystemMessage(SystemMessage.CLAN_MEMBER_S1_LOGGED_IN).addString(activeChar.getName());
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
    
    /* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.clientpackets.ClientBasePacket#getType()
	 */
	public String getType()
	{
		return _C__03_ENTERWORLD;
	}
}
