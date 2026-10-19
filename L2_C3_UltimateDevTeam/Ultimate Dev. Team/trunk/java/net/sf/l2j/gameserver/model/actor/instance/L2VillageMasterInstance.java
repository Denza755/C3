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
package net.sf.l2j.gameserver.model.actor.instance;

import net.sf.l2j.Config;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.instancemanager.SiegeManager;
import net.sf.l2j.gameserver.model.L2Clan;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.base.ClassType;
import net.sf.l2j.gameserver.model.base.PlayerClass;
import net.sf.l2j.gameserver.model.base.PlayerRace;
import net.sf.l2j.gameserver.model.base.SubClass;
import net.sf.l2j.gameserver.packets.serverpackets.*;
import net.sf.l2j.gameserver.tables.CharTemplateTable;
import net.sf.l2j.gameserver.tables.ClanTable;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;

import java.sql.PreparedStatement;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.logging.Logger;

/**
 * This class ...
 * 
 * @version $Revision: 1.4.2.3.2.8 $ $Date: 2005/03/29 23:15:15 $
 */
public final class L2VillageMasterInstance extends L2FolkInstance
{
	private static Logger _log = Logger.getLogger(L2VillageMasterInstance.class.getName());
    
    private static final int ELIXIR_ITEM_ID = 6319; // Mimir's Elixir (obtained through quest)
    private static final int DESTINY_ITEM_ID = 5011; // Star of Destiny
    
	/**
	 * @param template
	 */
	public L2VillageMasterInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
	}
	
	public void onBypassFeedback(L2PcInstance player, String command)
	{
        double distance = getDistance(player.getX(), player.getY());
        if (distance > INTERACTION_DISTANCE)
            return;

        player.sendPacket( new ActionFailed() );

        StringTokenizer st = new StringTokenizer(command, " ");
        String actualCommand = st.nextToken(); // Get actual command

        String val = "";
        if (st.countTokens() >= 1) {val = st.nextToken();}

        if (actualCommand.equalsIgnoreCase("create_clan"))
		{
            if (val == "") return;
			createClan(player, val);
		} 
        else if (actualCommand.equalsIgnoreCase("create_ally"))
		{
            if (val == "") return;
			createAlly(player, val);
		} 
        else if (actualCommand.equalsIgnoreCase("dissolve_ally"))
            dissolveAlly(player);
        else if (actualCommand.equalsIgnoreCase("dissolve_clan"))
            dissolveClan(player, player.getClanId());
        else if (actualCommand.equalsIgnoreCase("increase_clan_level"))
            levelUpClan(player, player.getClanId());
        else if (command.startsWith("Subclass"))
        {
            int intVal = Integer.parseInt(command.substring(9, 10).trim());
            
            // Subclasses may not be changed while a skill is in use.
            if (player.isCastingNow() || player.isAllSkillsDisabled()) {
                player.sendPacket(new SystemMessage(1295));
                return;
            }
            
            StringBuffer content = new StringBuffer("<html><body>");
            NpcHtmlMessage html = new NpcHtmlMessage(1);
            
            List<SubClass> playerSubs = player.getSubClasses();
            Set<PlayerClass> subsAvailable;
            
            int classId = 0;
            int newClassId = 0;
            
            try {
                int endIndex = command.length();
                
                if (command.length() > 13) {
                    endIndex = 13;
                    newClassId = Integer.parseInt(command.substring(13).trim());
                }
                
                classId = Integer.parseInt(command.substring(11, endIndex).trim());
            }
            catch (Exception NumberFormatException) {}
            
            switch (intVal)
            {
                case 1: // Add Subclass - Initial
                    subsAvailable = getAvailableSubClasses(player);
                    
                    if (subsAvailable != null && !subsAvailable.isEmpty()) {
                        content.append("Add Subclass:<br>Which subclass do you wish to add?<br>");
                        
                        for (PlayerClass subClass : subsAvailable)
                            content.append("<a action=\"bypass -h npc_" + getObjectId() + "_Subclass 4 " + 
                                           subClass.ordinal() + "\">" + formatClassForDisplay(subClass) + "</a><br>");
                    }
                    else {
                        player.sendMessage("There are no subclasses available at this time.");
                        return;
                    }
                    break;
                case 2: // Change Class - Initial
                    content.append("Change Subclass:<br>");
                    
                    final int baseClassId = player.getBaseClass();
                    
                    if (playerSubs.isEmpty()) {
                        content.append("You can't change subclasses when you don't have a subclass to begin with.<br>" +
                                       "<a action=\"bypass -h npc_" + getObjectId() + "_Subclass 1\">Add subclass.</a>");
                    }
                    else {
                        content.append("Which class would you like to switch to?<br>");
                        
                        if (baseClassId == player.getActiveClass())
                            content.append(CharTemplateTable.getClassNameById(baseClassId) + " <font color=\"LEVEL\">(Base Class)</font><br><br>");
                        else
                            content.append("<a action=\"bypass -h npc_" + getObjectId() + "_Subclass 5 0\">" + CharTemplateTable.getClassNameById(baseClassId) + "</a> " +
                            "<font color=\"LEVEL\">(Base Class)</font><br><br>");
                        
                        int indexId = 1;
                        
                        for (SubClass subClass : playerSubs) {
                            int subClassId = subClass.getClassId();
                            
                            if (subClassId == player.getActiveClass())
                                content.append(CharTemplateTable.getClassNameById(subClassId) + "<br>");
                            else
                                content.append("<a action=\"bypass -h npc_" + getObjectId() + "_Subclass 5 " + indexId + "\">" + 
                                               CharTemplateTable.getClassNameById(subClassId) + "</a><br>");
                            
                            indexId++;
                        }
                    }
                    break;
                case 3: // Change/Cancel Subclass - Initial
                    content.append("Change Subclass:<br>Which of the following sub-classes would you like to change?<br>");
                    
                    for (int i = 0; i < 3; i++) 
                    {
                        content.append("Sub-class " + (i + 1) + "<br1>");
                        
                        try {
                            content.append("<a action=\"bypass -h npc_" + getObjectId() + "_Subclass 6 " + i + "\">" + 
                                           CharTemplateTable.getClassNameById(playerSubs.get(i).getClassId()) + "</a><br>");
                        }
                        catch (Exception NullPointerException) {}
                    }
                    
                    content.append("<br>If you change a sub-class, you'll start at level 40 after the 2nd class transfer.");
                    break;
                case 4: // Add Subclass - Action (Subclass 4 x[x])
                    boolean allowAddition = true;
                    
                    /*
                     * If the character is less than level 75 on any of their previously chosen 
                     * classes then disallow them to change to their most recently added sub-class choice.
                     */ 
                    if (player.getLevel() < 75) {
                        player.sendMessage("You may not add a new sub class before you are level 75 on your previous class.");
                        allowAddition = false;
                    }
                    
                    if (!playerSubs.isEmpty())
                        for (SubClass subClass : playerSubs) {
                            if (subClass.getLevel() < 75) {
                                player.sendMessage("You may not add a new sub class before you are level 75 on your previous sub class.");
                                allowAddition = false;
                                break;
                            }
                        }
                    
                    /* 
                     * If quest checking is enabled, verify if the character has completed the Mimir's Elixir (Path to Subclass) 
                     * and Fate's Whisper (A Grade Weapon) quests by checking for instances of their unique reward items.
                     * 
                     * If they both exist, remove both unique items and continue with adding the sub-class.
                     */
                    if (!Config.ALT_GAME_SUBCLASS_WITHOUT_QUESTS)
                    {
                        L2ItemInstance elixirItem = player.getInventory().findItemByItemId(ELIXIR_ITEM_ID);
                        L2ItemInstance destinyItem = player.getInventory().findItemByItemId(DESTINY_ITEM_ID);
                    
                        if (elixirItem == null) {
                            player.sendMessage("You must have completed the Mimir's Elixir quest to continue adding your sub class.");
                            allowAddition = false;
                        }
                    
                        if (destinyItem == null) {
                            player.sendMessage("You must have completed the Fate's Whisper quest to continue adding your sub class.");
                            allowAddition = false;
                        }
                    
                        if (allowAddition) {
                            player.removeItemFromInventory(elixirItem, 1);
                            player.removeItemFromInventory(destinyItem, 1);
                        
                            InventoryUpdate iu = new InventoryUpdate();
                            iu.addRemovedItem(elixirItem);
                            iu.addRemovedItem(destinyItem);
                            player.sendPacket(iu);
                            
                            player.sendPacket(new SystemMessage(SystemMessage.DISSAPEARED_ITEM).addItemName(ELIXIR_ITEM_ID));

                            player.sendPacket(new SystemMessage(SystemMessage.DISSAPEARED_ITEM).addItemName(DESTINY_ITEM_ID));
                        }
                    }
                        
                    ////////////////// \\\\\\\\\\\\\\\\\\
                    if (allowAddition) {
                        String className = CharTemplateTable.getClassNameById(classId);
                        
                        //sm = new SystemMessage(1268);
                        //sm.addString(className);
                        //player.sendPacket(sm); // Addition confirmation.
                        
                        if (!player.addSubClass(classId, -1)) {
                            player.sendMessage("The sub class could not be added.");
                            return;
                        }
                        
                        player.setActiveClass(player.getTotalSubClasses());
                        
                        content.append("Add Subclass:<br>The subclass of <font color=\"LEVEL\">" + className + "</font> has been added.");
                        player.sendPacket(new SystemMessage(1308)); // Transfer to new class.
                    }
                    else {
                        html.setFile("data/html/villagemaster/SubClass_Fail.htm");
                    }
                    break;
                case 5: // Change Class - Action
                    /*
                     * If the character is less than level 75 on any of their previously chosen 
                     * classes then disallow them to change to their most recently added sub-class choice.
                     */ 
                    int classIndex = classId;
                    
                    if (classIndex > 0 && classIndex == player.getTotalSubClasses() + 1) {
                        int subIndex = classIndex - 1;
                        int prevClassLevel = playerSubs.get(subIndex).getLevel();
                        
                        if (prevClassLevel < 75) {
                            player.sendMessage("You may not change to your subclass before you are level 75.");
                            return;
                        }
                    }
                    
                    player.setActiveClass(classIndex);
                    
                    content.append("Change Subclass:<br>Your active subclass is now a <font color=\"LEVEL\">" + 
                                   CharTemplateTable.getClassNameById(player.getActiveClass()) + "</font>.");
                    
                    player.sendPacket(new SystemMessage(1270)); // Transfer completed.
                    break;
                case 6: // Change/Cancel Subclass - Choice
                    content.append("Please choose a subclass to change to. If the one you are looking for is not here, " +
                                   "please seek out the appropriate master for that class.<br>" +
                    "<font color=\"LEVEL\">Warning!</font> All classes and skills for this class will be removed.<br><br>");
                    
                    subsAvailable = getAvailableSubClasses(player);
                    
                    if (!subsAvailable.isEmpty()) {
                        for (PlayerClass subClass : subsAvailable)
                            content.append("<a action=\"bypass -h npc_" + getObjectId() + "_Subclass 7 " + classId + " " +
                                           subClass.ordinal() + "\">" + formatClassForDisplay(subClass) + "</a><br>");
                    }
                    else {
                        player.sendMessage("There are no subclasses available at this time.");
                        return;
                    }
                    break;
                case 7: // Change/Cancel Subclass - Action
                    //player.sendPacket(new SystemMessage(1445)); // Change confirmation.
                   
                    if (player.modifySubClass(classId, newClassId)) {
                        classId++;
                        
                        if (player.getLevel() < 75 && (player.getActiveClass() == player.getBaseClass())) {
                            player.sendMessage("You may not switch to your subclass before you are level 75.");
                            return;
                        }
                        
                        player.setActiveClass(classId);
                        
                        content.append("Change Subclass:<br>Your subclass has been changed to <font color=\"LEVEL\">" + CharTemplateTable.getClassNameById(newClassId) + "</font>.");
                        player.sendPacket(new SystemMessage(1269)); // Subclass added.
                    }
                    else {
                        player.sendMessage("The sub class could not be added.");
                        return;
                    }
                    break;
            }
            
            content.append("</body></html>");
            
            // If the content is greater than for a basic blank page,
            // then assume no external HTML file was assigned.
            if (content.length() > 26)
                html.setHtml(content.toString());
            
            player.sendPacket(html);
        }
        else 
        {
            // this class dont know any other commands, let forward
            // the command to the parent class
            super.onBypassFeedback(player, command);
        }
	}
	
	public String getHtmlPath(int npcId, int val)
	{
		String pom = "";
		if (val == 0)
		{
			pom = "" + npcId;
		} 
		else 
		{
			pom = npcId + "-" + val;
		}
		
		return "data/html/villagemaster/" + pom + ".htm";
	}

	
	public void onAction(L2PcInstance player)
	{
		if (Config.DEBUG) _log.fine("Village Master activated");
		super.onAction(player);
	}
	
	//Private stuff
	public void createClan(L2PcInstance player, String clanName)
	{
		if (Config.DEBUG)
			_log.fine(player.getObjectId()+"("+player.getName()+
					") requested clan creation from "+getObjectId()+"("+getName()+")");
		if (player.getLevel() < 10)
		{
			player.sendPacket(new SystemMessage(SystemMessage.FAILED_TO_CREATE_CLAN));
			return;
		}
		
		if (player.getClanId() != 0)
		{
			player.sendPacket(new SystemMessage(SystemMessage.FAILED_TO_CREATE_CLAN));
			return;
		}

		if (!player.canCreateClan())
		{	
			// you can't create clan 10 days
			player.sendPacket(new SystemMessage(230));
			return;
		}
		if (clanName.length() > 16)
		{
			player.sendPacket(new SystemMessage(SystemMessage.CLAN_NAME_TOO_LONG));
			return;
		}
		
		L2Clan clan = ClanTable.getInstance().createClan(player, clanName.trim());
		if (clan == null)
		{
			// clan name is already taken
			player.sendPacket(new SystemMessage(SystemMessage.CLAN_NAME_INCORRECT));
			return;
		}
		
		//should be update packet only
		player.sendPacket(new PledgeShowInfoUpdate(clan, player));

		player.sendPacket(new UserInfo(player));

		player.sendPacket(new SystemMessage(SystemMessage.CLAN_CREATED));
	}
    
    private void dissolveClan(L2PcInstance player, int clanId)
    {
		if (player.getClan() == null)
		    return;
		if (!player.isClanLeader())
		{
	    //only clan leader
            player.sendPacket(new SystemMessage(785));
			return;
        }
		
        L2PcInstance[] clanMembers = player.getClan().getOnlineMembers(player.getName());
        player.setClan(null);
        player.setTitle(null);
        
        long deleteclantime = System.currentTimeMillis();
        player.setDeleteClanTime(deleteclantime);
        SiegeManager.getInstance().removeSiegeSkills(player);

        // The clan leader should take the XP penalty of a full death.
        player.deathPenalty(false);
        
        SystemMessage sm = new SystemMessage(193);
	
        for (int i = 0; i < clanMembers.length; i++)
        {
            clanMembers[i].setClan(null);
            clanMembers[i].setTitle(null);
            clanMembers[i].sendPacket(sm);
            clanMembers[i].setDeleteClanTime(deleteclantime);

            clanMembers[i].broadcastUserInfo();
        }
        
        java.sql.Connection con = null;
        
        try
        {
            con = L2DatabaseFactory.getInstance().getConnection();
            PreparedStatement statement = con.prepareStatement("UPDATE characters SET clanid = 0 and deleteclan = ? WHERE clanid=?");
            statement.setLong(1, deleteclantime);
            statement.setInt(2, clanId);
            statement.execute();
            statement.close();
            
            statement = con.prepareStatement("DELETE FROM clan_data WHERE clan_id=?");
            statement.setInt(1, clanId);
            statement.execute();
            statement.close();
            player.sendPacket(sm);
            player.broadcastUserInfo();
            
            con.close();
        }
        catch (Exception e)
        {
            _log.warning("could not dissolve clan:"+e);
        } 
        finally 
        {
            try { con.close(); } catch (Exception e) {}
        }
    }
	
	public void levelUpClan(L2PcInstance player, int clanId)
	{
		L2Clan clan = player.getClan();
		if (clan == null)
		{
			return;
		}
		if (!player.isClanLeader())
		{
			player.sendMessage("Only clan leader can increase the clan level.");
			return;
        }
		
		boolean increaseClanLevel = false;
		
		switch (clan.getLevel())
		{
			case 0:
			{
				// upgrade to 1
				if (player.getSp() >= 35000 && player.getAdena() >= 650000)
				{
					player.setSp(player.getSp()-35000);
					player.reduceAdena(650000);
					increaseClanLevel = true;
				}
				break;
			}
			case 1:
			{
				// upgrade to 2
				if (player.getSp() >= 150000 && player.getAdena() >= 2500000)
				{
					player.setSp(player.getSp()-150000);
					player.reduceAdena(2500000);
					increaseClanLevel = true;
				}
				break;
			}
			case 2:
			{
				// upgrade to 3
				if (player.getSp() >= 500000 && player.getInventory().findItemByItemId(1419) != null)
				{
					// itemid 1419 == proof of blood
					player.setSp(player.getSp()-500000);
					player.getInventory().destroyItemByItemId(1419, 1);
					increaseClanLevel = true;
				}
				break;
			}
			case 3:
			{
				// upgrade to 4
				if (player.getSp() >= 1400000 && player.getInventory().findItemByItemId(3874) != null)
				{
					// itemid 3874 == proof of alliance
					player.setSp(player.getSp()-1400000);
                    player.getInventory().destroyItemByItemId(3874, 1);
					increaseClanLevel = true;
				}
				break;
			}
			case 4:
			{
				// upgrade to 5
				if (player.getSp() >= 3500000 && (player.getInventory().findItemByItemId(3870) != null || player.getAdena() >= 30000000))
				{
					// itemid 3870 == proof of aspiration
					player.setSp(player.getSp()-3500000);
					if (player.getInventory().findItemByItemId(3870) != null)
					{
						player.getInventory().destroyItemByItemId(3870, 1);
					}
					else
					{
						player.reduceAdena(30000000);
					}
					increaseClanLevel = true;
				}
				break;
			}
		}
		
		if (increaseClanLevel)
		{
			// the player should know that he has less sp now :p
			sendPacket(new StatusUpdate(player.getObjectId()).addAttribute(StatusUpdate.SP, player.getSp()));
			
			player.sendPacket(new ItemList(player, false));
			
			java.sql.Connection con = null;
	        try
	        {
	            con = L2DatabaseFactory.getInstance().getConnection();
	            PreparedStatement statement = con.prepareStatement("UPDATE clan_data SET clan_level = ? WHERE clan_id = ?");
				statement.setInt(1, clan.getLevel()+1);
				statement.setInt(2, clanId);
	            statement.execute();
	            statement.close();
	            
	            con.close();
	        }
	        catch (Exception e)
	        {
	            _log.warning("could not increase clan level:"+e);
	        } 
	        finally 
	        {
	            try { con.close(); } catch (Exception e) {}
	        }
			
			clan.setLevel(clan.getLevel()+1);
			if (clan.getLevel() > 3) SiegeManager.getInstance().addSiegeSkills(player);
                            
			// notify all the members about it
			SystemMessage sm = new SystemMessage(SystemMessage.CLAN_LEVEL_INCREASED);
			player.sendPacket(sm);
			L2PcInstance[] members = clan.getOnlineMembers(player.getName());
			for (int i = 0; i < members.length; i++)
			{
				members[i].sendPacket(sm);
			}
			clan.broadcastToOnlineMembers(new PledgeStatusChanged(clan));
            clan.broadcastClanStatus();
		}
		else
		{
			player.sendPacket(new SystemMessage(SystemMessage.FAILED_TO_INCREASE_CLAN_LEVEL));
		}
	}
	public void createAlly(L2PcInstance player, String allyName)
	{
		//D5 You may not ally with clan you are battle with.
		//D6 Only the clan leader may apply for withdraw from alliance.
		//DD No response. Invitation to join an 
		//D7 Alliance leaders cannot withdraw.
		//D9 Different Alliance 
		//EB alliance information
		//Ec alliance name $s1
		//ee alliance leader: $s2 of $s1
		//ef affilated clans: total $s1 clan(s)
		//f6 you have already joined an alliance
		//f9 you cannot new alliance 10 days
		//fd cannot accept. clan ally is register as enemy during siege batle.
		//fe you have invited someone to your alliance.
		//100 do you wish to withdraw from the alliance
		//102 enter the name of the clan you wish to expel.
		//202 do you realy wish to dissolve the alliance
		//502 you have accepted alliance
		//602 you have failed to invite a clan into the alliance
		//702 you have withdrwa
		
		if (player.getClanId() == 0)
		    return;
		
		
		if (Config.DEBUG)
			_log.fine(player.getObjectId()+"("+player.getName()+
					") requested ally creation from "+getObjectId()+"("+getName()+")");
		
		if (!player.isClanLeader())
		{
			//only clan leaders may create alliances.
			player.sendPacket(new SystemMessage(504));
			return;
		}
		if (player.getClan().getAllyId() != 0)
		{
			//is in ally
			player.sendPacket(new SystemMessage(502));
			return;
		}
		if (allyName.length() < 2)
		{
			// incorect alliance name
			player.sendPacket(new SystemMessage(506));
			return;
		}
		if (allyName.length() > 16)
		{
			// incorect length of alliance name
			player.sendPacket(new SystemMessage(507));
			return;
		}
		if (player.getClan().getLevel()<5)
		{
			//only clan level 5 or higher.
			player.sendPacket(new SystemMessage(549));
			return;
		}
		if(ClanTable.getInstance().isAllyExists(allyName)){
			//name exists.
			player.sendPacket(new SystemMessage(508));
			return;
		}

		player.getClan().setAllyId(player.getClanId());
		player.getClan().setAllyName(allyName.trim());
		player.getClan().updateClanInDB();
		
		player.sendPacket(new UserInfo(player));

		player.sendMessage("Alliance "+allyName+" has been created.");
	}
    private void dissolveAlly(L2PcInstance player)
    {
        L2Clan playerClan = player.getClan();
        
        if (player.getClan() == null)
            return;
        
        int allyId = playerClan.getAllyId();
        
        if (allyId == 0)
            return;
        
        if (allyId != player.getClanId() || !player.isClanLeader())
        {
            player.sendPacket(new SystemMessage(SystemMessage.FEATURE_ONLY_FOR_ALLIANCE_LEADER));
            return;
        }	
        
        playerClan.setAllyId(0);
        playerClan.setAllyName(null);
        
        // The clan leader should take the XP penalty of a full death.
        player.deathPenalty(false);
        
        SystemMessage sm = new SystemMessage(SystemMessage.ALLIANCE_DISOLVED);
        
//        for (int i = 0; i < clanMembers.length; i++)
//        {
//            clanMembers[i].setClan(null);
//            clanMembers[i].setTitle(null);
//            clanMembers[i].sendPacket(sm);
//
//            clanMembers[i].broadcastUserInfo();
//        }
        
        java.sql.Connection con = null;
        try
        {
            con = L2DatabaseFactory.getInstance().getConnection();
            PreparedStatement statement = con.prepareStatement("UPDATE clan_data SET ally_id = 0, ally_name = '' WHERE ally_id=?");
            statement.setInt(1, allyId);
            statement.execute();
            statement.close();
            
            player.sendPacket(sm);
            player.broadcastUserInfo();
            
            con.close();
        }
        catch (Exception e)
        {
            _log.warning("could not dissolve clan:"+e);
        } 
        finally 
        {
            try { con.close(); } catch (Exception e) {}
        }
    }
    
    private final Set<PlayerClass> getAvailableSubClasses(L2PcInstance player)
    {
        final int charClassId = player.getClassId().ordinal();
        final PlayerRace npcRace = getVillageMasterRace();
        final ClassType npcTeachType = getVillageMasterTeachType();
        
        PlayerClass currClass = PlayerClass.values()[charClassId];//.valueOf(charClassName);
        
        /**
         * If the race of your main class is Elf or Dark Elf, 
         * you may not select each class as a subclass to the other class, 
         * and you may not select Overlord and Warsmith class as a subclass.
         * 
         * You may not select a similar class as the subclass. 
         * The occupations classified as similar classes are as follows:
         *  
         * Treasure Hunter, Plainswalker and Abyss Walker 
         * Hawkeye, Silver Ranger and Phantom Ranger 
         * Paladin, Dark Avenger, Temple Knight and Shillien Knight 
         * Warlocks, Elemental Summoner and Phantom Summoner 
         * Elder and Shillien Elder 
         * Swordsinger and Bladedancer 
         * Sorcerer, Spellsinger and Spellhowler
         * 
         */
        Set<PlayerClass> availSubs = currClass.getAvaliableSubclasses();
        
        if (availSubs != null)
        {
            for (PlayerClass availSub : availSubs)
            {
                for (SubClass subClass : player.getSubClasses())
                    if (availSub.ordinal() == subClass.getClassId()) availSubs.remove(currClass);
                
                if (!availSub.isOfRace(PlayerRace.Human) && !availSub.isOfRace(PlayerRace.LightElf))
                {
                    if (!availSub.isOfRace(npcRace)) availSubs.remove(availSub);
                }
                else
                {
                    if (!availSub.isOfType(npcTeachType)) availSubs.remove(availSub);
                }
            }
        }
        
        return availSubs;
    }
    
    private final String formatClassForDisplay(PlayerClass className)
    {
        String classNameStr = className.toString();
        char[] charArray = classNameStr.toCharArray();
        
        for (int i = 1; i < charArray.length; i++)
            if (Character.isUpperCase(charArray[i])) 
                classNameStr = classNameStr.substring(0, i) + " " + classNameStr.substring(i);

        return classNameStr;
    }
    
    private final PlayerRace getVillageMasterRace()
    {
        String npcClass = getTemplate().getStatsSet().getString("jClass");
        
        if (npcClass.indexOf("Human") > -1)
            return PlayerRace.Human;
        
        if (npcClass.indexOf("DarkElf") > -1)
            return PlayerRace.DarkElf;
        
        if (npcClass.indexOf("Elf") > -1)
            return PlayerRace.LightElf;
        
        if (npcClass.indexOf("Orc") > -1)
            return PlayerRace.Orc;
        
        return PlayerRace.Dwarf;
    }

    private final ClassType getVillageMasterTeachType()
    {
        String npcClass = getTemplate().getStatsSet().getString("jClass");
        
        if (npcClass.indexOf("sanctuary") > -1 || npcClass.indexOf("clergyman") > -1)
            return ClassType.Priest;
        
        if (npcClass.indexOf("mageguild") > -1 || npcClass.indexOf("patriarch") > -1)
            return ClassType.Mystic;
        
        return ClassType.Fighter;
    }
}
