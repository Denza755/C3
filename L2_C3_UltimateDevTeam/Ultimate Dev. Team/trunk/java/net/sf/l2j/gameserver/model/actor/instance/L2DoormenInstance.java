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

import net.sf.l2j.gameserver.packets.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.packets.serverpackets.MyTargetSelected;
import net.sf.l2j.gameserver.packets.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;

import java.util.StringTokenizer;

/**
 * This class ...
 * 
 * @version $Revision$ $Date$
 */
public class L2DoormenInstance extends L2FolkInstance
{
	//private L2ClanHallInstance _ClanHall;
	private static int Cond_All_False = 0;
	private static int Cond_Busy_Because_Of_Siege = 1;
	private static int Cond_Castle_Owner = 2;
	private static int Cond_Hall_Owner = 3;

	/**
	 * @param template
	 */
	public L2DoormenInstance(int objectID, L2NpcTemplate template)
	{
		super(objectID, template);
	}

    //public final L2Castle getClanHall()
    //{
   	//    if (_ClanHall == null)
   	//     _ClanHall = L2ClanHallInstance.getHall(getX(), getY());
    //
	//    return _ClanHall;
    //}

	public void onBypassFeedback(L2PcInstance player, String command)
	{
		player.sendPacket( new ActionFailed() );

		int condition = validateCondition(player);
		if (condition <= Cond_All_False)
            return;

		if (condition == Cond_Busy_Because_Of_Siege)
            return;
		else if (condition == Cond_Castle_Owner || condition == Cond_Hall_Owner)
		{
			if (command.startsWith("open_doors"))
			{
				//DoorTable doorTable = DoorTable.getInstance();
				StringTokenizer st  = new StringTokenizer(command.substring(10), ", ");
				st.nextToken(); // Bypass first value since its castleid/hallid
				
				if (condition == 2)
				{
		           	while (st.hasMoreTokens())
		           	{
		           	    getCastle().openDoor(player, Integer.parseInt(st.nextToken()));
		           	}
                    return;
				}
				//else if (condition == 3)
				//{
		        //   	while (st.hasMoreTokens())
		        //   	{
		        //   	    getClanHall().openDoor(Integer.parseInt(st.nextToken()), player);
		        //   	}
                //      return;
				//}
			}
			else if (command.startsWith("close_doors"))
			{
				//DoorTable doorTable = DoorTable.getInstance();
				StringTokenizer st  = new StringTokenizer(command.substring(11), ", ");
				st.nextToken(); // Bypass first value since its castleid/hallid
				
				//L2Clan playersClan = player.getClan();
				
				if (condition == 2)
				{
		           	while (st.hasMoreTokens())
		           	{
		           	    getCastle().closeDoor(player, Integer.parseInt(st.nextToken()));
		           	}
                    return;
				}
				//else if (condition == 3)
				//{
		        //   	while (st.hasMoreTokens())
		        //   	{
		        //   	    getClanHall().closeDoor(Integer.parseInt(st.nextToken()), player);
		        //   	}
                //      return;
				//}
			}
		}

        super.onBypassFeedback(player, command);
	}
	
	/**
	 * this is called when a player interacts with this NPC
	 * @param player
	 */
	public void onAction(L2PcInstance player)
	{
        player.sendPacket(new ActionFailed());
		player.setTarget(this);
		player.sendPacket(new MyTargetSelected(getObjectId(), -15));

        if (getDistance(player.getX(), player.getY()) <= INTERACTION_DISTANCE)
            showMessageWindow(player);
	}
	
	public void showMessageWindow(L2PcInstance player)
	{
		player.sendPacket( new ActionFailed() );
		String filename = "data/html/doormen/" + getTemplate().npcId + "-no.htm";

		int condition = validateCondition(player);
        if (condition == Cond_Busy_Because_Of_Siege)
            filename = "data/html/doormen/" + getTemplate().npcId + "-busy.htm";	// Busy because of siege
        else if (condition == Cond_Castle_Owner)									// Clan owns castle
            filename = "data/html/doormen/" + getTemplate().npcId + ".htm";			// Owner message window
	    // Prepare doormen for clan hall
	    //else if (condition == Cond_Hall_Owner)
        //	filename = "data/html/doormen/" + getTemplate().npcId + ".htm";			// Owner message window
		
		NpcHtmlMessage html = new NpcHtmlMessage(1);
		html.setFile(filename);
		html.replace("%objectId%",String.valueOf(getObjectId()));
		player.sendPacket(html);
	}
	
	private int validateCondition(L2PcInstance player)
	{
	    if (player.getClan() != null)
	    {
			if (getCastle() != null && getCastle().getCastleId() >= 0)
			{
//		        if (getCastle().getSiege().getIsInProgress())
//		            return Cond_Busy_Because_Of_Siege;									// Busy because of siege
//		        else 
			if (getCastle().getOwnerId() == player.getClanId())				// Clan owns castle
		            return Cond_Castle_Owner;											// Owner
			}
		    // Prepare doormen for clan hall
			//else if (getClanHall() != null && getClanHall().getHallId() > 0)
			//{
		    //    if (getClanHall().getOwnerId() == player.getClanId())
		    //        return Cond_Hall_Owner;
			//}
	    }
		
		return Cond_All_False;
	}
}
