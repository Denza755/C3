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
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.Calendar;
import java.util.Date;
import java.text.SimpleDateFormat;

import net.sf.l2j.Base64;
import net.sf.l2j.Config;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.network.Disconnection;
import net.sf.l2j.gameserver.datatables.GmListTable;
import net.sf.l2j.gameserver.datatables.SkillTable;
import net.sf.l2j.gameserver.*;
import net.sf.l2j.gameserver.util.FloodProtector;
import net.sf.l2j.gameserver.util.Util;
import net.sf.l2j.gameserver.cache.HtmCache;
import net.sf.l2j.gameserver.handler.AdminCommandHandler;
import net.sf.l2j.gameserver.instancemanager.PetitionManager;
import net.sf.l2j.gameserver.instancemanager.ClanHallManager;
import net.sf.l2j.gameserver.model.L2Clan;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.entity.L2Event;
import net.sf.l2j.gameserver.model.entity.ClanHall;
import net.sf.l2j.gameserver.model.quest.Quest;
import net.sf.l2j.gameserver.serverpackets.*;

/**
 * Enter World Packet Handler<p>
 * <p>
 * 0000: 03 <p>
 * packet format rev656 cbdddd  
 * <p>
 * 
 * @version $Revision: 1.16.2.1.2.7 $ $Date: 2005/03/29 23:15:33 $
 */
public class EnterWorld extends L2GameClientPacket
{
	private static final String _C__03_ENTERWORLD = "[C] 03 EnterWorld";
	private static Logger _log = Logger.getLogger(EnterWorld.class.getName());

	private static final SimpleDateFormat fmt = new SimpleDateFormat("H:mm.");
	long _daysleft;
	SimpleDateFormat df = new SimpleDateFormat("dd MM yyyy");    


    public TaskPriority getPriority() { return TaskPriority.PR_URGENT; }
	
	/**
	 * @param decrypt
	 */
    public EnterWorld(ByteBuffer buf, L2GameClient client)
	{
		super(buf, client);
		// this is just a trigger packet. it has no content
	}

	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		
		if (activeChar == null)
		{
			_log.warning("EnterWorld failed! activeChar is null...");
		    return;
		}

        // Проверка на пакеты при входе в игру.
        FloodProtector.getInstance().registerNewPlayer(activeChar.getObjectId());        

		if (L2World.getInstance().findObject(activeChar.getObjectId()) != null)
		{
			if (Config.DEBUG)
				_log.warning("User already exists in Object ID map! User "+activeChar.getName()+" is a character clone.");
		}
        
        if (activeChar.isGM())
        {
        	if (Config.GM_STARTUP_INVULNERABLE
        			&& (!Config.ALT_PRIVILEGES_ADMIN && activeChar.getAccessLevel() >= Config.GM_GODMODE
        			  || Config.ALT_PRIVILEGES_ADMIN && AdminCommandHandler.getInstance().checkPrivileges(activeChar, "admin_invul")))
        		activeChar.setIsInvul(true);
        	
            if (Config.GM_STARTUP_INVISIBLE 
                    && (!Config.ALT_PRIVILEGES_ADMIN && activeChar.getAccessLevel() >= Config.GM_GODMODE
                      || Config.ALT_PRIVILEGES_ADMIN && AdminCommandHandler.getInstance().checkPrivileges(activeChar, "admin_invisible")))
                activeChar.setInvisible();

            if (Config.GM_STARTUP_SILENCE 
                    && (!Config.ALT_PRIVILEGES_ADMIN && activeChar.getAccessLevel() >= Config.GM_MENU
                      || Config.ALT_PRIVILEGES_ADMIN && AdminCommandHandler.getInstance().checkPrivileges(activeChar, "admin_silence")))
                activeChar.setMessageRefusal(true);
            
            if (Config.GM_STARTUP_AUTO_LIST 
                    && (!Config.ALT_PRIVILEGES_ADMIN && activeChar.getAccessLevel() >= Config.GM_MENU
                      || Config.ALT_PRIVILEGES_ADMIN && AdminCommandHandler.getInstance().checkPrivileges(activeChar, "admin_gmliston")))
            	GmListTable.getInstance().addGm(activeChar);
            
            if (Config.GM_NAME_COLOR_ENABLED)
            {
                if (activeChar.getAccessLevel() >= 100)
                    activeChar.setNameColor(Config.ADMIN_NAME_COLOR);
                else if (activeChar.getAccessLevel() >= 75)
                    activeChar.setNameColor(Config.GM_NAME_COLOR);
            }
        }

		// Set dead status if applies
		if (activeChar.getCurrentHp() < 0.5)
			activeChar.isDead();

        if (Config.PLAYER_SPAWN_PROTECTION > 0)
            activeChar.setProtection(true);
        
		activeChar.spawnMe(activeChar.getX(), activeChar.getY(), activeChar.getZ());
		
		if (L2Event.active && L2Event.connectionLossData.containsKey(activeChar.getName()) && L2Event.isOnEvent(activeChar))
            L2Event.restoreChar(activeChar);
        else if (L2Event.connectionLossData.containsKey(activeChar.getName()))
            L2Event.restoreAndTeleChar(activeChar);

		if (SevenSigns.getInstance().isSealValidationPeriod())
			sendPacket(new SignsSky());

        // Если истина, то все эффекты на персонаже будут восстановлены после релода в игре.
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

        // SECURE FIX - Anti Overenchant Cheat!!
        if(Config.MAX_ITEM_ENCHANT_KICK > 0)
        {
            for (L2ItemInstance i : activeChar.getInventory().getItems())
            {
                if (!activeChar.isGM())
                {
                    if (i.isEquipable())
                    {
                        if (i.getEnchantLevel() > Config.MAX_ITEM_ENCHANT_KICK)
                        {
                            //Delete Item Over enchanted
                            activeChar.getInventory().destroyItem(null, i, activeChar, null);
                            //Message to Player
                            activeChar.sendMessage("[Server]: You have over enchanted items you will be kicked from server!");
                            activeChar.sendMessage("[Server]: Respect our server rules.");
                            //Message with screen chat
                            activeChar.sendMessage("You have an over enchanted item, you will be kicked from server!");
                            //Punishment e log in audit
                            Util.handleIllegalPlayerAction(activeChar, "Player " + activeChar.getName() + " has Overenchanted  item! Kicked! ", Config.DEFAULT_PUNISH);
                            //Logger in console
                            _log.info("#### ATTENTION ####");
                            _log.info(i+" item has been removed from "+activeChar);
                        }

                    }
                }
            }
        }

        // Загружаем персонажу его макросы при входе в игру.
        activeChar.getMacroses().sendUpdate();
        // Загружаем персонажу все его итемы
        sendPacket(new ItemList(activeChar, false));
        // Загружаем при входе в игру всю информацю о персонаже.
        sendPacket(new UserInfo(activeChar));
        // Skills update
        activeChar.sendSkillList();
		sendPacket(new ShortCutInit(activeChar));
        // Загружаем краски игрока при входе в игру если имеются.
        sendPacket(new HennaInfo(activeChar));
        // Загружаем лист друзей игрока при входе в игру.
        sendPacket(new FriendList(activeChar));
        // Выводим сообщения
        SystemMessage sm = new SystemMessage(34);
        sendPacket(sm);

        if(activeChar.isAio())
		    onEnterAio(activeChar);

        // If caharacter have Premium account, show html message.
		if (activeChar.getPremiumService() == 1)
		{
			String premiumMessage = HtmCache.getInstance().getHtm("data/html/premium_message.htm");
			if (premiumMessage != null)
				sendPacket(new NpcHtmlMessage(1, premiumMessage));
		}
        if (activeChar.getPremiumService() == 1)
        {
            activeChar.sendMessage("You have Premium Account.");
			Calendar finishtime = Calendar.getInstance();
			finishtime.setTimeInMillis(System.currentTimeMillis());
			finishtime.set(Calendar.SECOND, 0);          
        }

        SevenSigns.getInstance().sendCurrentPeriodMsg(activeChar);
        Announcements.getInstance().showAnnouncements(activeChar);

		Quest.playerEnter(activeChar);

		// Check chits skills a players
		if(Config.CHECK_SKILLS_ON_ENTER && !Config.GAME_SKILL_LEARN)
	    if(!activeChar.isAio())
	    		activeChar.checkAllowedSkills();
        

        PetitionManager.getInstance().checkPetitionMessages(activeChar);
		
        // send user info again .. just like the real client
        //sendPacket(ui);

        if (activeChar.getClanId() != 0 && activeChar.getClan() != null)
        {
        	sendPacket(new PledgeShowMemberListAll(activeChar.getClan(), activeChar));
        	sendPacket(new PledgeStatusChanged(activeChar.getClan()));
        }
	
		if (activeChar.isAlikeDead())
		{
			// no broadcast needed since the player will already spawn dead to others
			sendPacket(new Die(activeChar));
		}      

        if (activeChar.getClan() != null)
        {

            // Add messege to characters, else Clan Hall not buy.
            ClanHall clanHall = ClanHallManager.getInstance().getClanHallByOwner(activeChar.getClan());
            if(clanHall != null){
                if(!clanHall.getPaid()){
                    activeChar.sendPacket(new SystemMessage(SystemMessage.PAYMENT_FOR_YOUR_CLAN_HALL_HAS_NOT_BEEN_MADE_PLEASE_MAKE_PAYMENT_TO_YOUR_CLAN_WAREHOUSE_BY_S1_TOMORROW));
                }
            }
        }

        if (Config.ALLOW_WATER) activeChar.checkWaterState();

		if (!nProtect.getInstance().checkRestriction(activeChar, nProtect.RestrictionType.RESTRICT_ENTER))
		{
			activeChar.setIsImobilised(true);
			activeChar.disableAllSkills();
			ThreadPoolManager.getInstance().scheduleGeneral(new Disconnection(activeChar), 20000);
		}
        
        //add char to online characters
		activeChar.setOnlineStatus(true);
		
        notifyFriends(activeChar);
		notifyClanMembers(activeChar);

		activeChar.setRunning(); // running is default
		activeChar.standUp(); // standing is default
        
        activeChar.onPlayerEnter();
        activeChar.setTarget(activeChar);
        
        if(Config.GAMEGUARD_ENFORCE)
            activeChar.sendPacket(new GameGuardQuery());

		if(Config.SERVER_TIME_ON_START) {
			activeChar.sendMessage("SVR time is " + fmt.format(new Date(System.currentTimeMillis())));
		}        
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
            statement = con.prepareStatement("SELECT friend_name FROM character_friends WHERE charId=?");
            statement.setInt(1, cha.getObjectId());
            ResultSet rset = statement.executeQuery();

            L2PcInstance friend;
            String friendName;

            SystemMessage sm = new SystemMessage(SystemMessage.FRIEND_S1_HAS_LOGGED_IN);
            sm.addString(cha.getName());

            while (rset.next())
            {
                friendName = rset.getString("friend_name");

                friend = L2World.getInstance().getPlayer(friendName);

                if (friend != null) //friend logged in.
                {
                    friend.sendPacket(new FriendList(friend));
                    friend.sendPacket(sm);
                }
            }
            sm = null;

            rset.close();
            statement.close();
        }
        catch (Exception e)
        {
            _log.log(Level.SEVERE, "Error restoring friend data.", e);
        }
        finally
        {
            try {con.close();} catch (Exception e){}
        }
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

            if(activeChar.isClanLeader()) 
                activeChar.setClanPrivileges(L2Clan.CP_ALL);

			L2PcInstance[] clanMembers = clan.getOnlineMembers(activeChar.getName());
            PledgeShowMemberListAdd ps = new PledgeShowMemberListAdd(activeChar);
			for (int i = 0; i < clanMembers.length; i++)
			{
				clanMembers[i].sendPacket(ps);
				clanMembers[i].sendPacket(msg);
			}
		}
	}

	private void onEnterAio(L2PcInstance activeChar)
	{
		long now = Calendar.getInstance().getTimeInMillis();
		long endDay = activeChar.getAioEndTime();
		if(now > endDay)
    	{
    		activeChar.setAio(false);
    		activeChar.setAioEndTime(0);
    		activeChar.lostAioSkills();
    		activeChar.sendMessage("Removed your Aio stats... period ends ");
    	}
    	else
    	{
    		Date dt = new Date(endDay);
    		_daysleft = (endDay - now)/86400000;
    		if(_daysleft > 30)
    			activeChar.sendMessage("Aio period ends in " + df.format(dt) + ". enjoy the Game");
    		else if(_daysleft > 0)
    			activeChar.sendMessage("left " + (int)_daysleft + " for Aio period ends");
    		else if(_daysleft < 1)
    		{
    			long hour = (endDay - now)/3600000;
    			activeChar.sendMessage("left " + (int)hour + " hours to Aio period ends");
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
