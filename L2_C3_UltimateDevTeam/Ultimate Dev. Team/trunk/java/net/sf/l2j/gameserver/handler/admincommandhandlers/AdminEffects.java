/* This program is free software; you can redistribute it and/or modify
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
package net.sf.l2j.gameserver.handler.admincommandhandlers;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.clientpackets.CharacterCreate;
import net.sf.l2j.gameserver.handler.IAdminCommandHandler;
import net.sf.l2j.gameserver.idfactory.IdFactory;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.instance.L2NpcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PlayableInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2SummonInstance;
import net.sf.l2j.gameserver.model.base.Experience;
import net.sf.l2j.gameserver.packets.serverpackets.*;
import net.sf.l2j.gameserver.tables.CharNameTable;
import net.sf.l2j.gameserver.tables.ItemTable;
import net.sf.l2j.gameserver.tables.NpcTable;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;
import net.sf.l2j.gameserver.util.Util;

import java.util.StringTokenizer;

/**
* This class handles following admin commands: 	- invis / invisible = make yourself invisible
* 												- vis / visible	= make yourself visible
* 
*/
public class AdminEffects implements IAdminCommandHandler
{

   private static String[] _adminCommands = { "admin_invis", "admin_invisible", "admin_vis",
	   "admin_visible", "admin_earthquake", "admin_bighead", "admin_shrinkhead", 
	   "admin_unpara_all", "admin_para_all", "admin_para_world", "admin_unpara_world", "admin_unpara", "admin_para", "admin_polyself",
	   "admin_unpolyself", "admin_changename", "admin_summon123", "admin_create_adena", "admin_setlevel", "admin_sethero", "admin_nobuff"};

   private static final int REQUIRED_LEVEL = Config.GM_GODMODE;

   public boolean useAdminCommand(String command, L2PcInstance activeChar)
   {
       if (!Config.ALT_PRIVILEGES_ADMIN)
       {
           if (!(checkLevel(activeChar.getAccessLevel()) && activeChar.isGM()))
               return false;
       }
       
       if (command.equals("admin_invis")||command.equals("admin_invisible"))
       {
    	   activeChar.setInvisible();
    	   activeChar.broadcastUserInfo();
    	   activeChar.teleToLocation(activeChar.getX(), activeChar.getY(), activeChar.getZ());
       }
       if (command.equals("admin_vis")||command.equals("admin_visible"))
       {
    	   activeChar.setVisible();
    	   activeChar.broadcastUserInfo();
       }
       if (command.startsWith("admin_earthquake"))
       {
           try
           {
        	   String val = command.substring(17);
               StringTokenizer st = new StringTokenizer(val);
               String val1 = st.nextToken();
               int intensity = Integer.parseInt(val1);
               String val2 = st.nextToken();
               int duration = Integer.parseInt(val2);
        	   Earthquake eq = new Earthquake(activeChar.getX(), activeChar.getY(), activeChar.getZ(), 
        			   						  intensity, duration);
               activeChar.broadcastPacket(eq);
           }
           catch (Exception e)
           {  }
       }
       if (command.equals("admin_para"))
       {
    	   String type = "1";
           StringTokenizer st = new StringTokenizer(command);
           try
           {
               st.nextToken();
               type = st.nextToken();
           }
           catch(Exception e){}
           try
           {
       		L2Object target = activeChar.getTarget();
       		L2Character player = null;
	    		if (target instanceof L2Character) {
	    			player = (L2Character)target;
	    			
	    			if (type.equals("1"))
	    			   player.startAbnormalEffect((short)0x0400);
	    			else
	    				player.startAbnormalEffect((short)0x0800);
	            	player.setIsParalyzed(true);
	            	
	            	StopMove sm = new StopMove(player);
	            	player.sendPacket(sm);
	            	player.broadcastPacket(sm);
			Util.handleLogGmActions(activeChar, "admin_para");
	    		}
           }
           catch (Exception e)
           {
           }
       }
       if (command.equals("admin_unpara"))
       {
           try
           {
          		L2Object target = activeChar.getTarget();
           		L2Character player = null;
    	    		if (target instanceof L2Character) {
    	    			player = (L2Character)target;
    	    			   player.stopAbnormalEffect((short)0x0400);
    	    			   player.setIsParalyzed(false);
				Util.handleLogGmActions(activeChar, "admin_unpara");
    	    		}
           }
           catch (Exception e)
           {
           }
       }
       if (command.startsWith("admin_para_all"))
       {
           try
           {
               for (L2PcInstance player : activeChar.getKnownList().getKnownPlayers())
               {
            	   if (!player.isGM())
            	   {
                	   player.startAbnormalEffect((short)0x0400);
                	   player.setIsParalyzed(true);
                	   
                	   StopMove sm = new StopMove(player);
   	            	   player.sendPacket(sm);
   	            	   player.broadcastPacket(sm);
            	   }
               }
               Util.handleLogGmActions(activeChar, "admin_para_all");
           }
           catch (Exception e)
           {
           }
       }
       if (command.startsWith("admin_unpara_all"))
       {
           try
           {
               for (L2PcInstance player : activeChar.getKnownList().getKnownPlayers())
               {
            	   player.stopAbnormalEffect((short)0x0400);
            	   player.setIsParalyzed(false);
               }
               Util.handleLogGmActions(activeChar, "admin_unpara_all");
           }
           catch (Exception e)
           {
           }
       }
       if (command.startsWith("admin_para_world"))
       {
           try
           {
               for (L2PcInstance player : L2World.getInstance().getAllPlayers())
               {
                   if (!player.isGM())
                   {
                       player.setIsInvul(true);
                       player.sendMessage("You are now paralyzed");

                       player.startAbnormalEffect((short)0x0400);
                       player.setIsParalyzed(true);
                       
                       StopMove sm = new StopMove(player);
                       player.sendPacket(sm);
                       player.broadcastPacket(sm);
                   }else{
                       player.sendMessage("GM paralyzed the world");               
                   }
               }
               Util.handleLogGmActions(activeChar, "GM paralyzed the world");
           }
           catch (Exception e)
           {
           }
       }
       if (command.startsWith("admin_unpara_world"))
       {
           try
           {
               for (L2PcInstance player : L2World.getInstance().getAllPlayers())
               {
                   player.setIsInvul(false);
                   player.sendMessage("Paralyzed is over now");
                   
                   player.stopAbnormalEffect((short)0x0400);
                   player.setIsParalyzed(false);
               }
               Util.handleLogGmActions(activeChar, "GM unparalyzed the world");
           }
           catch (Exception e)
           {
           }
       }
       if (command.startsWith("admin_bighead"))
       {
           try
           {   
               L2Object target = activeChar.getTarget();
               L2Character player = null;
                if (target instanceof L2Character) {
                  player = (L2Character)target;
                  player.startAbnormalEffect((short)0x2000);
		  Util.handleLogGmActions(activeChar, "admin_bighead of "+ player.getName());		  
                }
           }
           catch (Exception e)
           {
           }
       }
       if (command.startsWith("admin_shrinkhead"))
       {
           try
           {
                 L2Object target = activeChar.getTarget();
                   L2Character player = null;
                    if (target instanceof L2Character) {
                      player = (L2Character)target;
                      player.stopAbnormalEffect((short)0x2000);
		      Util.handleLogGmActions(activeChar, "admin_shrinkhead of "+ player.getName());
                    }
           }
           catch (Exception e)
           {
           }
       }
       if (command.startsWith("admin_polyself"))
       {
       	StringTokenizer st = new StringTokenizer(command);
        try
        {
            st.nextToken();
            String id = st.nextToken();
        	activeChar.getPoly().setPolyInfo("npc", id);
        	activeChar.teleToLocation(activeChar.getX(), activeChar.getY(), activeChar.getZ());
        	activeChar.broadcastPacket(new CharInfo(activeChar));
        	activeChar.sendPacket(new UserInfo(activeChar));
		Util.handleLogGmActions(activeChar, "admin_polyself to "+id);
        }
        catch(Exception e){}
       }
       if (command.startsWith("admin_unpolyself"))
       {
        	activeChar.getPoly().setPolyInfo(null, "1");
        	activeChar.decayMe();
        	activeChar.spawnMe(activeChar.getX(),activeChar.getY(),activeChar.getZ());
        	activeChar.broadcastPacket(new CharInfo(activeChar));
        	activeChar.sendPacket(new UserInfo(activeChar));
		Util.handleLogGmActions(activeChar, "admin_unpolyself");
       }
       if (command.startsWith("admin_setlevel"))
       {
	//String type = "1";
	StringTokenizer st = new StringTokenizer(command);
        try
        {
            st.nextToken();
	    int newlvl = Integer.parseInt(st.nextToken());
	    if (newlvl >= 1 && newlvl <= Config.MAX_LEVEL)
            {

                L2Object target = (L2PlayableInstance)activeChar.getTarget();
                L2PlayableInstance player = null;

                if (target instanceof L2PlayableInstance)
                    player = (L2PlayableInstance)activeChar.getTarget();
                else
                    player = (L2PlayableInstance)activeChar;

                int oldlvl = player.getLevel();
                int difflvl = newlvl - oldlvl;

                if (difflvl != 0)
		{
		player.getStat().addLevel(difflvl);
		player.broadcastPacket(new SocialAction(player.getObjectId(), 15));

                if (difflvl > 0)
		{
			player.sendMessage(activeChar.getName() + " has increased your level to " + player.getLevel());
			activeChar.sendMessage("You have increased " + player.getName() + " to level " + player.getLevel());
			Util.handleLogGmActions(activeChar, " has increased "+player.getName()+" level to " + player.getLevel());
		}
		else if (difflvl < 0)
		{
			player.sendMessage(activeChar.getName() + " has decreased your level to " + player.getLevel());
			activeChar.sendMessage("You have decreased " + player.getName() + " to level " + player.getLevel());
			Util.handleLogGmActions(activeChar, " has decreased "+player.getName()+" level to " + player.getLevel());
		}
		}

            } else {
                activeChar.sendMessage("Wrong paramater [1.."+Config.MAX_LEVEL+"].");
            }
        }
        catch (Exception e)
        {
	activeChar.sendMessage("Error when set level");
        }
       }
       if (command.startsWith("admin_create_adena"))
	{
	//String type = "1";
	StringTokenizer st = new StringTokenizer(command);
	try
	{
	st.nextToken();
	int numval = Integer.parseInt(st.nextToken());

	L2Object target = activeChar.getTarget();
	L2PcInstance player = null;

	if (target instanceof L2PcInstance)
		player = (L2PcInstance)target;
	else
     	      	player = activeChar;

	L2ItemInstance createditem = ItemTable.getInstance().createItem(57);

	createditem.setCount(createditem.getCount()+(numval-1));
	player.getInventory().addItem(createditem);

	player.sendMessage(activeChar.getName() + " has given " + numval  +" adena in your inventory.");
	activeChar.sendMessage("You have given " + numval + " adena to " + player.getName());				
	Util.handleLogGmActions(activeChar, "give" + numval + " adena to " + player.getName());
	}
	catch (StringIndexOutOfBoundsException e)
	{
	activeChar.sendMessage("Error while creating adena.");
	}
        }

       if (command.startsWith("admin_summon123"))
       {
	//String type = "1";
	StringTokenizer st = new StringTokenizer(command);
	try
	{
	st.nextToken();
        int npcId = Integer.parseInt(st.nextToken());

	if (activeChar.getPet() != null)
           return false;

        float expPenalty = 0.f;

	L2NpcTemplate summonTemplate = NpcTable.getInstance().getTemplate(npcId);
        L2SummonInstance summon = new L2SummonInstance(IdFactory.getInstance().getNextId(), summonTemplate, activeChar);
		
        summon.setTitle(activeChar.getName());
        summon.setExpPenalty(expPenalty);
        if (summon.getLevel() >= Experience.LEVEL.length)
        {
            summon.getStat().setExp(Experience.LEVEL[Experience.LEVEL.length - 1]);
        }
        else
        {
            summon.getStat().setExp(Experience.LEVEL[(summon.getLevel() % Experience.LEVEL.length)]);
        }
		summon.setCurrentHp(summon.getMaxHp());
		summon.setCurrentMp(summon.getMaxMp());
		summon.setHeading(activeChar.getHeading());
		summon.setRunning();
		activeChar.setPet(summon);
    		
    	L2World.getInstance().storeObject(summon);
        summon.spawnMe(activeChar.getX()+50, activeChar.getY()+100, activeChar.getZ());
    		
    	summon.setFollowStatus(true);
        summon.setShowSummonAnimation(false);
        activeChar.sendPacket(new PetInfo(summon));
	}
        catch(Exception e){}
       }
	if (command.startsWith("admin_sethero"))
	{
	L2Object target = activeChar.getTarget();
	L2PcInstance player = null;

	if (target instanceof L2PcInstance)
		player = (L2PcInstance)target;
	else
     	      	player = activeChar;

	if (player.isHero())
		player.setHero(false);
	else
		player.setHero(true);
	player.broadcastUserInfo();
	Util.handleLogGmActions(activeChar, "admin_sethero "+player.getName());
	}
	//GM_cansellation ...
	if (command.startsWith("admin_nobuff"))
	{
		L2Object target = activeChar.getTarget();
	    L2PcInstance player = null;
	    if (target instanceof L2PcInstance)
	    	player = (L2PcInstance)target;
	    else
	    	player = activeChar;
	    	player.stopAllEffects(); 
	    	Util.handleLogGmActions(activeChar, "admin_nobuff "+player.getName());
	}
       if (command.startsWith("admin_changename"))
       {
           try
           {
        	   String name = command.substring(17);
        	   if (CharNameTable.getInstance().doesCharNameExist(name) )
        	   {
        	   activeChar.sendMessage("Charname "+ name +" already exist.");
        	   return false; 
        	   }
        	   if ((name.length() < 3) || (name.length() > 16) || !CharacterCreate.isAlphaNumeric(name) || !CharacterCreate.isValidName(name))
        	   {
        	   activeChar.sendMessage("Charname: " + name + " is invalid.");
        	   return false;
        	   }
        	   String oldName = "null";
        	   try
               {
              		L2Object target = activeChar.getTarget();
               		L2Character player = null;
        	    		if (target instanceof L2Character) {
        	    			player = (L2Character)target;
        	    			oldName = player.getName();
        	    		}
        	    		else if (target == null)
        	    		{
        	    			player = activeChar;
        	    			oldName = activeChar.getName();
        	    		}
        	    		if (player instanceof L2PcInstance)
        	    			L2World.getInstance().removeFromAllPlayers((L2PcInstance)player);
        	    		player.setName(name);
        	    		if (player instanceof L2PcInstance)
        	    			L2World.getInstance().addVisibleObject(player, null, null);
        	    		
        	    		if (player instanceof L2PcInstance)
        	    		{
        	        	CharInfo info1 = new CharInfo((L2PcInstance)player);
        	        	player.broadcastPacket(info1);
        			    UserInfo info2 = new UserInfo((L2PcInstance)player);
        			    player.sendPacket(info2);
        	    		}
        	    		else if(player instanceof L2NpcInstance)
        	    		{
        	    			NpcInfo info1 = new NpcInfo((L2NpcInstance)player, null);
            	        	player.broadcastPacket(info1);
        	    		}
        				activeChar.sendMessage("Changed name from "+ oldName +" to "+ name +".");
               }
               catch (Exception e)
               {
               }
           }
           catch (StringIndexOutOfBoundsException e)
           { }
       }
       else if (command.equals("admin_clear_teams"))
       {
           try
           {
               for (L2PcInstance player : activeChar.getKnownList().getKnownPlayers())
               {
                   player.setTeam(0);
                   player.broadcastUserInfo();
               }
           }
           catch (Exception e)
           {
           }
       }
       else if (command.startsWith("admin_setteam_close"))
       {
           String val = command.substring(20);
           int teamVal = Integer.parseInt(val);
           try
           {
               for (L2PcInstance player : activeChar.getKnownList().getKnownPlayers())
               {
                   double dist = dist = activeChar.getDistance(player.getX(), player.getY());
                   if (dist < 400)
                   {
                   player.setTeam(0);
                   if (teamVal != 0)
                   {
                   player.sendMessage("You have joined team " + teamVal);
                   }
                   player.broadcastUserInfo();
                   }
               }
           }
           catch (Exception e)
           {
           }
       }
       else if (command.startsWith("admin_setteam"))
       {
           String val = command.substring(14);
           int teamVal = Integer.parseInt(val);
           L2Object target = activeChar.getTarget();
           L2PcInstance player = null;
           if (target instanceof L2PcInstance) {
               player = (L2PcInstance)target;
           } else {
               return false;
           }
           player.setTeam(teamVal);
           if (teamVal != 0)
           {
           player.sendMessage("You have joined team " + teamVal);
           }
           player.broadcastUserInfo();
       }
       
       return true;
   }

   public String[] getAdminCommandList()
   {
       return _adminCommands;
   }

   private boolean checkLevel(int level)
   {
       return (level >= REQUIRED_LEVEL);
   }

}
