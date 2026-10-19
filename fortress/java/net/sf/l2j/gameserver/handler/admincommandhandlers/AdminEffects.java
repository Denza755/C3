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

import java.util.StringTokenizer;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.handler.IAdminCommandHandler;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2NpcInstance;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2PcInstance;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.serverpackets.CharInfo;
import net.sf.l2j.gameserver.serverpackets.Earthquake;
import net.sf.l2j.gameserver.serverpackets.NpcInfo;
import net.sf.l2j.gameserver.serverpackets.StopMove;
import net.sf.l2j.gameserver.serverpackets.UserInfo;

/**
* This class handles following admin commands: 	- invis / invisible = make yourself invisible
* 												- vis / visible	= make yourself visible
* 
*/
public class AdminEffects implements IAdminCommandHandler
{
   //private static Logger _log = Logger.getLogger(AdminDelete.class.getName());

   private static String[] _adminCommands = { "admin_invis", "admin_invisible", "admin_vis",
	   "admin_visible", "admin_earthquake", "admin_bighead", "admin_shrinkhead", 
	   "admin_unpara_all", "admin_para_all", "admin_unpara", "admin_para", "admin_polyself",
	   "admin_unpolyself", "admin_changename" };

   private static final int REQUIRED_LEVEL = Config.GM_GODMODE;

   public boolean useAdminCommand(String command, L2PcInstance activeChar)
   {
       if (!(checkLevel(activeChar.getAccessLevel()) && activeChar.isGM()))
           return false;
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
               activeChar.broadcastPacket(new Earthquake(activeChar.getX(), activeChar.getY(), activeChar.getZ(), intensity, duration));
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
	            	player.setParalyzed(true);
	            	
	            	player.broadcastPacket(new StopMove(player));
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
    	    			   player.setParalyzed(false);
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
               for(L2PcInstance player : activeChar.getVisiblePlayers(1250))
               {
            	   if(!player.isGM())
            	   {
                	   player.startAbnormalEffect((short)0x0400);
                	   player.setParalyzed(true);
                	   
   	            	   player.broadcastPacket(new StopMove(player));
            	   }
               }
           }
           catch (Exception e) { }
       }
       if (command.startsWith("admin_unpara_all"))
       {
           try
           {
               for (L2PcInstance player : activeChar.getVisiblePlayers(2500))
               {
            	   player.stopAbnormalEffect((short)0x0400);
            	   player.setParalyzed(false);
               }
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
        	activeChar.setPolyInfo("npc", id);
        	activeChar.teleToLocation(activeChar.getX(), activeChar.getY(), activeChar.getZ());
        	activeChar.broadcastPacket(new CharInfo(activeChar));
		    activeChar.sendPacket(new UserInfo(activeChar));
        }
        catch(Exception e){}
       }
       if (command.startsWith("admin_unpolyself"))
       {
        	activeChar.setPolyInfo(null, "1");
        	activeChar.decayMe();
        	activeChar.spawnMe(activeChar.getX(),activeChar.getY(),activeChar.getZ());
        	activeChar.broadcastPacket(new CharInfo(activeChar));
		    activeChar.sendPacket(new UserInfo(activeChar));
       }
       if (command.startsWith("admin_changename"))
       {
           try
           {
        	   String name = command.substring(17);
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
        	    			L2World.removeFromAllPlayers((L2PcInstance)player);
        	    		player.setName(name);
        	    		if (player instanceof L2PcInstance)
        	    			L2World.addVisibleObject(player, null, null);
        	    		
        	    		if (player instanceof L2PcInstance)
        	    		{
	        	        	player.broadcastPacket(new CharInfo((L2PcInstance)player));
        				    player.sendPacket(new UserInfo((L2PcInstance)player));
        	    		}
        	    		else if(player instanceof L2NpcInstance)
        	    		{
            	        	player.broadcastPacket(new NpcInfo((L2NpcInstance)player, null));
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
