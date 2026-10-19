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
package net.sf.l2j.gameserver.model.entity;

import java.sql.PreparedStatement;
import java.util.List;
import java.util.logging.Logger;

import javolution.util.FastList;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.DoorTable;
import net.sf.l2j.gameserver.instancemanager.ZoneManager;
import net.sf.l2j.gameserver.model.entity.Zone;
import net.sf.l2j.gameserver.model.entity.ZoneType;
import net.sf.l2j.gameserver.model.L2Clan;
import net.sf.l2j.gameserver.model.L2ClanMember;
import net.sf.l2j.gameserver.model.L2DoorInstance;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2PcInstance;
import net.sf.l2j.gameserver.serverpackets.PledgeShowMemberListAll;

public class ClanHall
{
    protected static Logger _log = Logger.getLogger(ClanHall.class.getName());
    
	// =========================================================
    // Data Field
	private int _ClanHallId                    = 0;
	private List<L2DoorInstance> _Doors;
	private List<String> _DoorDefault          = new FastList<String>();
    private String _Name                       = "";
	private int _OwnerId                       = 0;
    private Zone _Zone;

	// =========================================================
	// Constructor
	public ClanHall(int clanHallId)
	{
		_ClanHallId = clanHallId;
	}

	// =========================================================
	// Method - Public
    /** Return true if object is inside the zone */
    public boolean checkIfInZone(L2Object obj) { return checkIfInZone(obj.getX(), obj.getY()); }

    /** Return true if object is inside the zone */
    public boolean checkIfInZone(int x, int y) { return getZone().checkIfInZone(x, y); }
	
	public void closeDoor(L2PcInstance activeChar, int doorId) { openCloseDoor(activeChar, doorId, false); }

	public void openDoor(L2PcInstance activeChar, int doorId) { openCloseDoor(activeChar, doorId, true); }

	public void openCloseDoor(L2PcInstance activeChar, int doorId, boolean open)
	{
	    if (activeChar.getClanId() != getOwnerId()) return;

	    L2DoorInstance door = getDoor(doorId);
        if (door != null)
        {
            if (open)
                door.openMe();
            else
                door.closeMe();
        }
	}
	
	public void setOwner(L2Clan clan)
	{
	    if (clan == null) return;
        updateOwnerInDB(clan);
	}

    /** Respawn all doors */
	public void spawnDoor() { spawnDoor(false); }
    
	/** Respawn all doors */
	public void spawnDoor(boolean isDoorWeak)
    {
	    for (int i = 0; i < getDoors().size(); i++)
        {
            L2DoorInstance door = getDoors().get(i);
            if (door.getCurrentHp() <= 0)
            {
                door.decayMe();	// Kill current if not killed already
                door = DoorTable.parseList(_DoorDefault.get(i));
                if (isDoorWeak) door.setCurrentHp(door.getMaxHp() / 2);
    			door.spawnMe(door.getX(), door.getY(),door.getZ());
    			getDoors().set(i, door);
            }
            else if (door.isOpen())
                door.closeMe();
        }
    }
	
	// =========================================================
	// Method - Private
	
	private void updateOwnerInDB(L2Clan clan)
	{
	    _OwnerId = clan.getClanId();	// Update owner id property

	    java.sql.Connection con = null;
        try
        {
            con = L2DatabaseFactory.getInstance().getConnection();
            PreparedStatement statement;

            statement = con.prepareStatement("UPDATE clan_data SET hasHideout=0 WHERE hasHideout=?");
            statement.setInt(1, getClanHallId());
            statement.execute();
            statement.close();   

            statement = con.prepareStatement("UPDATE clan_data SET hasHideout=? WHERE clan_id=?");
            statement.setInt(1, getClanHallId());
            statement.setInt(2, getOwnerId());
            statement.execute();
            statement.close();   
            
            for (L2ClanMember member : clan.getMembers())
    		{
    			if (member.isOnline() && member.getPlayerInstance() != null)
    			{
    				member.getPlayerInstance().sendPacket(new PledgeShowMemberListAll(clan, member.getPlayerInstance()));
    			}
    		}
        }
        catch (Exception e)
        {
            System.out.println("Exception: updateOwnerInDB(L2Clan clan): " + e.getMessage());
            e.printStackTrace();
        }
        finally
        {
            try { con.close(); } catch (Exception e) {}
        }
	}
	
	// =========================================================
	// Proeprty
	public final int getClanHallId() { return _ClanHallId; }

	public final L2DoorInstance getDoor(int doorId)
	{
	    if (doorId <= 0) return null;
	    
        for (int i = 0; i < getDoors().size(); i++)
        {
            L2DoorInstance door = getDoors().get(i);
            if (door.getDoorId() == doorId) return door;
        }
		return null;
	}

	public final List<L2DoorInstance> getDoors()
	{
        if (_Doors == null) _Doors = new FastList<L2DoorInstance>();
		return _Doors;
	}

    public final String getName() { return _Name; }

	public final int getOwnerId() { return _OwnerId; }

    public final Zone getZone()
    {
        if (_Zone == null) _Zone = ZoneManager.getInstance().getZone(ZoneType.getZoneTypeName(ZoneType.ZoneTypeEnum.ClanHall), getName());
        return _Zone;
    }
}