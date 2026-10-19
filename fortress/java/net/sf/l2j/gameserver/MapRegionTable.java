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
package net.sf.l2j.gameserver;


import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.logging.Logger;

import net.sf.l2j.Config;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.instancemanager.ArenaManager;
import net.sf.l2j.gameserver.instancemanager.CastleManager;
import net.sf.l2j.gameserver.instancemanager.TownManager;
import net.sf.l2j.gameserver.instancemanager.ZoneManager;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2NpcInstance;
import net.sf.l2j.gameserver.model.L2PcInstance;
import net.sf.l2j.gameserver.model.L2SiegeClan;
import net.sf.l2j.gameserver.model.Location;
import net.sf.l2j.gameserver.model.entity.Arena;
import net.sf.l2j.gameserver.model.entity.Castle;
import net.sf.l2j.gameserver.model.entity.Zone;
import net.sf.l2j.gameserver.model.entity.ZoneType;
import net.sf.l2j.gameserver.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.serverpackets.StopMove;

/**
 * This class ...
 */
public class MapRegionTable
{
	private static Logger _log = Logger.getLogger(MapRegionTable.class.getName());
	
	private static MapRegionTable _instance;
	
	private final int[][] _regions = new int[19][21];

    private final int[][] _pointsWithKarmas;

    private final int[][] _closestTown;

	public static enum TeleportWhereType
    {
        Castle,
        ClosestTown,
        SecondClosestTown,
        SiegeFlag
    }
	
	public static MapRegionTable getInstance()
	{
		if (_instance == null)
		{
			_instance = new MapRegionTable();
		}
		return _instance;
	}
	
	private MapRegionTable()
	{
		int count2 = 0;
		
		//LineNumberReader lnr = null;
		java.sql.Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("SELECT region, sec0, sec1, sec2, sec3, sec4, sec5, sec6, sec7, sec8, sec9 FROM mapregion");
			ResultSet rset = statement.executeQuery();
			int region;
			while (rset.next())
			{
				region = rset.getInt(1);
				
				for (int j=0; j<10; j++)
				{
					_regions[j][region] = rset.getInt(j+2);
					count2++;
					//_log.fine(j+","+region+" -> "+rset.getInt(j+2));
				}
			}
			
			rset.close();
			statement.close();
			if (Config.DEBUG) _log.fine(count2 +" mapregion loaded");
		}
		catch (Exception e)
		{
			_log.warning("error while creating map region data: "+e);
		} 
		finally 
		{
			try { con.close(); } catch (Exception e) {}
		}
        
        _pointsWithKarmas = new int[14][3];
        //Talking Island
        _pointsWithKarmas[0][0] = -79077;
        _pointsWithKarmas[0][1] = 240355;
        _pointsWithKarmas[0][2] = -3440;
        //Elven
        _pointsWithKarmas[1][0] = 43503;
        _pointsWithKarmas[1][1] = 40398;
        _pointsWithKarmas[1][2] = -3450;
        //DarkElven
        _pointsWithKarmas[2][0] = 1675;
        _pointsWithKarmas[2][1] = 19581;
        _pointsWithKarmas[2][2] = -3110;
        //Orc
        _pointsWithKarmas[3][0] = -44413;
        _pointsWithKarmas[3][1] = -121762;
        _pointsWithKarmas[3][2] = -235;
        //Dwalf
        _pointsWithKarmas[4][0] = 12009;
        _pointsWithKarmas[4][1] = -187319;
        _pointsWithKarmas[4][2] = -3309;
        //Gludio
        _pointsWithKarmas[5][0] = -18872;
        _pointsWithKarmas[5][1] = 126216;
        _pointsWithKarmas[5][2] = -3280;
        //Gludin
        _pointsWithKarmas[6][0] = -85915;
        _pointsWithKarmas[6][1] = 150402;
        _pointsWithKarmas[6][2] = -3060;
        //Dion
        _pointsWithKarmas[7][0] = 23652;
        _pointsWithKarmas[7][1] = 144823;
        _pointsWithKarmas[7][2] = -3330;
        //Giran
        _pointsWithKarmas[8][0] = 79125;
        _pointsWithKarmas[8][1] = 154197;
        _pointsWithKarmas[8][2] = -3490;
        //Oren
        _pointsWithKarmas[9][0] = 73840;
        _pointsWithKarmas[9][1] = 58193;
        _pointsWithKarmas[9][2] = -2730;
        //Aden
        _pointsWithKarmas[10][0] = 44413;
        _pointsWithKarmas[10][1] = 22610;
        _pointsWithKarmas[10][2] = 235;
        //Hunters
        _pointsWithKarmas[11][0] = 114137;
        _pointsWithKarmas[11][1] = 72993;
        _pointsWithKarmas[11][2] = -2445;
        //Giran
        _pointsWithKarmas[12][0] = 79125;
        _pointsWithKarmas[12][1] = 154197;
        _pointsWithKarmas[12][2] = -3490;
        // heine
        _pointsWithKarmas[13][0] = 119536;
        _pointsWithKarmas[13][1] = 218558;
        _pointsWithKarmas[13][2] = -3495;

        _closestTown = new int[14][3];
        // human
        _closestTown[0][0] = -84176; 
        _closestTown[0][1] = 243382;
        _closestTown[0][2] = -3126;
        //elf
        _closestTown[1][0] = 45525;
        _closestTown[1][1] = 48376;
        _closestTown[1][2] = -3059;
        // dark elf
        _closestTown[2][0] = 12181;
        _closestTown[2][1] = 16675;
        _closestTown[2][2] = -4580;
        // orc
        _closestTown[3][0] = -45232;
        _closestTown[3][1] = -113603;
        _closestTown[3][2] = -224;
        // dwarf
        _closestTown[4][0] = 115074;
        _closestTown[4][1] = -178115;
        _closestTown[4][2] = -880;
        // gludio
        _closestTown[5][0] = -14138;
        _closestTown[5][1] = 122042;
        _closestTown[5][2] = -2988;
        // gludin
        _closestTown[6][0] = -82856;
        _closestTown[6][1] = 150901;
        _closestTown[6][2] = -3128;
        // dion
        _closestTown[7][0] = 18823;
        _closestTown[7][1] = 145048;
        _closestTown[7][2] = -3126;
        // giran
        _closestTown[8][0] = 83235;
        _closestTown[8][1] = 148497;
        _closestTown[8][2] = -3404;
        // oren
        _closestTown[9][0] = 80853;
        _closestTown[9][1] = 54653;
        _closestTown[9][2] = -1524;
        // aden
        _closestTown[10][0] = 147391;
        _closestTown[10][1] = 25967;
        _closestTown[10][2] = -2012;
        // hunters 
        _closestTown[11][0] = 117163;
        _closestTown[11][1] = 76511;
        _closestTown[11][2]=    -2712;
        // harbor -> giran
        _closestTown[12][0] = 83235;
        _closestTown[12][1] = 148497;
        _closestTown[12][2] = -3404;
        // heine
        _closestTown[13][0] = 111381;
        _closestTown[13][1] = 219064;
        _closestTown[13][2] = -3543;
	}
	
	public final int getMapRegion(int posX, int posY)
	{
		int tileX = ( posX >> 15 ) + 4;// + centerTileX;
		int tileY = ( posY >> 15 ) + 10;// + centerTileY;
		//_log.fine(tileX+" : "+tileY);
		return _regions[tileX][tileY];
	}

	public int getClosestTownNumber(L2Character activeChar)
    {
        return getMapRegion(activeChar.getX(), activeChar.getY());
    }
    
    public String getClosestTownName(L2Character activeChar)
    {
        int nearestTownId = getMapRegion(activeChar.getX(), activeChar.getY());
        String nearestTown;
        
        switch (nearestTownId)
        {
            case 0: nearestTown = "Talking Island Village"; break;
            case 1: nearestTown = "Elven Village"; break;
            case 2: nearestTown = "Dark Elven Village"; break;
            case 3: nearestTown = "Orc Village"; break;
            case 4: nearestTown = "Dwarven Village"; break;
            case 5: nearestTown = "Gludio Castle Town"; break;
            case 6: nearestTown = "Gludin Village"; break;
            case 7: nearestTown = "Dion Castle Town"; break;
            case 8: nearestTown = "Giran Castle Town"; break;
            case 9: nearestTown = "Oren Castle Town"; break;
            case 10: nearestTown = "Aden Castle Town"; break;
            case 11: nearestTown = "Hunters Village"; break;
            case 12: nearestTown = "Giran Harbor"; break;
            case 13: nearestTown = "Heine"; break;
            default: nearestTown = "Aden Castle Town"; break;
        }
        
        return nearestTown;
    }

    @SuppressWarnings("static-access")
	public Location getTeleToLocation(L2Character activeChar, TeleportWhereType teleportWhere)
    {
        int[] coord;

        if(activeChar instanceof L2PcInstance)
        {
            L2PcInstance player = ((L2PcInstance)activeChar);

			Location loc = GameServer.events.onEscape(player);
			if(loc != null)
				return loc;

            if(player.getPkKills() > 5 && player.getKarma() > 1) return new Location(17817,170079,-3530); // to Floran Village

            // Checking if in arena
            Arena arena = ArenaManager.getInstance().getArena(player);
            if (arena != null)
            {
                coord = arena.getSpawn().get(0);
                if (coord != null) return new Location(coord[0], coord[1], coord[2]); 
            }

            if (player.getClan() != null)
            {
                // Checking if in siege
                Castle castle = CastleManager.getInstance().getCastle(player);
                if (castle != null && castle.getCastleId() > 0)
                {
                    if (castle.getSiege().getIsInProgress() || teleportWhere == TeleportWhereType.SiegeFlag)
                    {
                        // Check if player's clan is attacker
                        List<L2NpcInstance> flags = castle.getSiege().getFlag(player.getClan());
                        if (flags != null && flags.size() > 0)
                        {
                            // Spawn to flag - Need more work to get player to the nearest flag
                            L2NpcInstance flag = flags.get(0);
                            return new Location(flag.getX(), flag.getY(), flag.getZ());
                        }
                    }

                    if (castle.getSiege().getIsInProgress() || teleportWhere == TeleportWhereType.Castle)
                    {
                        // Check if player's clan is defender
                        L2SiegeClan sc = castle.getSiege().getDefenderClan(player.getClan());
                        if (sc != null)
                        {
                            Zone zone = ZoneManager.getInstance().getZone(ZoneType.getZoneTypeName(ZoneType.ZoneTypeEnum.CastleDefenderSpawn), castle.getName());
                            if (zone != null)
                            {
                                coord = zone.getCoords().get(0);
                                return new Location(coord[0], coord[1], coord[4]); 
                            }
                        }
                    }
                }
            }
        }

        // Get the nearest town
        coord = TownManager.getInstance().getClosestTown(activeChar).getSpawn().get(0);
        return new Location(coord[0], coord[1], coord[4]);

        /*
        // Return nearest location for death anywhere
        int closest =  getMapRegion(activeChar.getX(), activeChar.getY());
        if (redirect && teleportWhere != TeleportWhereType.ClosestTown) closest = _redirectTown[closest];
        return new Location(_closestTown[closest][0],_closestTown[closest][1],_closestTown[closest][2]);
        */
    }

    public void teleToLocation(L2Character activeChar, TeleportWhereType teleportWhere)
    {
        teleToLocation(activeChar, MapRegionTable.getInstance().getTeleToLocation(activeChar, teleportWhere));
    }

    public void teleToLocation(L2Character activeChar, Location loc)
    {
        if (activeChar == null || !(activeChar instanceof L2PcInstance)) return;

        L2PcInstance player = ((L2PcInstance)activeChar);

        player.abortAttack();
        player.abortCast();
        
        // Packets and other updates, teleport also
        player.broadcastPacket(new StopMove(player));
        player.sendPacket(new ActionFailed());

        // Get location where player should teleport to
        player.teleToLocation(loc.getX(), loc.getY(), loc.getZ());
    }
}
