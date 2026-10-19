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
package net.sf.l2j.gameserver.model;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import net.sf.l2j.L2DatabaseFactory;

/**
 * This class ...
 * 
 * @version $Revision: 1.21.2.5.2.7 $ $Date: 2005/03/27 15:29:32 $
 */
public final class L2World
{
	private static Logger _log = Logger.getLogger(L2World.class.getName());
	
	/*
	 * biteshift, defines number of regions
	 * note, shifting by 15 will result in regions corresponding to map tiles
	 * shifting by 12 divides one tile to 8x8 regions
	 */
	public static final int TILE_SIZE = 600;
	public static final int PLAYER_VISIBLE_TILES = 6000/TILE_SIZE;
	public static final int MAX_NPC_AGGRO_TILES = 1200/TILE_SIZE;
	
	/** Map dimensions */
	public static final int MAP_MIN_X = -131072;
	public static final int MAP_MAX_X = 196608;
	public static final int MAP_MIN_Y = -393216;
	public static final int MAP_MAX_Y = 262144;
	
	/** number of regions */
	private static final int REGIONS_X = (MAP_MAX_X - MAP_MIN_X)/TILE_SIZE+1;
	private static final int REGIONS_Y = (MAP_MAX_Y - MAP_MIN_Y)/TILE_SIZE+1;
	
	/** HashMap(String Player name, L2PcInstance) containing all the players in game */
	private static ConcurrentHashMap<String, L2PcInstance> _allPlayers;
	
	/** HashMap containing all visible objects */
	private static ConcurrentHashMap<Integer,L2Object> _allObjects;
	
	/** Arena Zone */
	public static int[][] zoneArena;	
	/** Castle Zone <I>(used to determine castle ground)</I> */
	public static int[][] zoneCastle;	
	/** Peace Zone */
	public static int[][] zonePeace;	
	/** Town Zone */
	public static int[][] zoneTown;	
	
	/** Area where to spawn for arena */
	public static int[][] spawnArena;

	/** Area where to spawn.move defender for castle */
	public static int[][] spawnCastleDefender;
	
	public static final int[][] castleownerspawns = {
		{-18325, 112682, -2659},  //Gludio Castle
		{22339, 157110, -2953},   //Dion Castle
		{113291, 144631, -2328},   //Giran Castle
		{79289,36896,-2464},   //Oren Castle
		{147485,8192,-470}   //Aden Castle
	};
	
	public static final int[][] castlewinareas = { //x1,y1,x2,y2,z
		{-18178,108056,-18064,108169,  -2455}, //Gludio Castle Podium
		{22019, 161585, 22129, 161700, -2655}, //Dion Castle Podium
		{117759, 145037, 117872, 145144, -2520},  //Giran Castle Podium
		{83839, 37102, 83944, 37288, -2245},   //Oren Castle Podium
		{147424, 1607, 147521, 1716, -345}, //Aden Castle Podium
	};
	
	public static final int[][] castlestatuespawns = {
		{-18120, 107976, -2482},  //Gludio Castle
		{22083,161762,-2677},   //Dion Castle
		{117950, 145087, -2515},   //Giran Castle
		{84028,37180,-2277},   //Oren Castle
		{147465,1523,-373}   //Aden Castle
	};
	

	private static final L2World _instance = new L2World();

	public static L2World getInstance()
	{
		return _instance;
	}

	public static void init()
	{
		loadZone();
	}
	
	private static void loadZone()
	{
		java.sql.Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("Select * from zone order by id");
			ResultSet rs = statement.executeQuery();

			String columnNameType		= "type";
			String columnNameX1			= "x1";
			String columnNameX2			= "x2";
			String columnNameY1			= "y1";
			String columnNameY2			= "y2";
			String columnNameZ			= "z";
			String typeNameArena		= "Arena";
			String typeNameCastleArea	= "Castle Area";
			String typeNamePeace		= "Peace";
			String typeNameTown			= "Town";
			String typeNameSpawnArena		   = "Arena Spawn";
			String typeNameSpawnCastleDefender  = "Castle Defender Spawn";

			ArrayList<int[]> tempArena = new ArrayList<int[]>();
			ArrayList<int[]> tempCastleArea = new ArrayList<int[]>();
			ArrayList<int[]> tempPeace = new ArrayList<int[]>();
			ArrayList<int[]> tempTown = new ArrayList<int[]>();

			ArrayList<int[]> tempSpawnArena = new ArrayList<int[]>();
			ArrayList<int[]> tempSpawnCastleDefender = new ArrayList<int[]>();
			while (rs.next())
			{
				if (rs.getString(columnNameType).equalsIgnoreCase(typeNameArena))
					tempArena.add(new int[]{rs.getInt(columnNameX1), rs.getInt(columnNameY1), rs.getInt(columnNameX2), rs.getInt(columnNameY2)});
				else if (rs.getString(columnNameType).equalsIgnoreCase(typeNameCastleArea))
					tempCastleArea.add(new int[]{rs.getInt(columnNameX1), rs.getInt(columnNameY1), rs.getInt(columnNameX2), rs.getInt(columnNameY2)});
				else if (rs.getString(columnNameType).equalsIgnoreCase(typeNamePeace))
					tempPeace.add(new int[]{rs.getInt(columnNameX1), rs.getInt(columnNameY1), rs.getInt(columnNameX2), rs.getInt(columnNameY2)});
				else if (rs.getString(columnNameType).equalsIgnoreCase(typeNameTown))
					tempTown.add(new int[]{rs.getInt(columnNameX1), rs.getInt(columnNameY1), rs.getInt(columnNameX2), rs.getInt(columnNameY2)});
				else if (rs.getString(columnNameType).equalsIgnoreCase(typeNameSpawnArena))
					tempSpawnArena.add(new int[]{rs.getInt(columnNameX1), rs.getInt(columnNameY1), rs.getInt(columnNameZ)});
				else if (rs.getString(columnNameType).equalsIgnoreCase(typeNameSpawnCastleDefender))
					tempSpawnCastleDefender.add(new int[]{rs.getInt(columnNameX1), rs.getInt(columnNameY1), rs.getInt(columnNameZ)});
			}
			statement.close();
			
			zonePeace = new int[tempPeace.size() + tempTown.size()][];

			zoneArena = new int[tempArena.size()][];
			for (int i = 0; i < tempArena.size(); i++)
				zoneArena[i] = tempArena.get(i);

			zoneCastle = new int[tempCastleArea.size()][];
			for (int i = 0; i < tempCastleArea.size(); i++)
				zoneCastle[i] = tempCastleArea.get(i);

			for (int i = 0; i < tempPeace.size(); i++)
				zonePeace[i] = tempPeace.get(i);

			zoneTown = new int[tempTown.size()][];
			for (int i = 0; i < tempTown.size(); i++)
			{
				zonePeace[i + tempPeace.size()] = tempTown.get(i);
				zoneTown[i] = tempTown.get(i);
			}

			spawnArena = new int[tempSpawnArena.size()][];
			for (int i = 0; i < tempSpawnArena.size(); i++)
				spawnArena[i] = tempSpawnArena.get(i);

			spawnCastleDefender = new int[tempSpawnCastleDefender.size()][];
			for (int i = 0; i < tempSpawnCastleDefender.size(); i++)
				spawnCastleDefender[i] = tempSpawnCastleDefender.get(i);

			_log.info("L2World: Loaded " + zoneArena.length + " arena zones");
			_log.info("L2World: Loaded " + spawnArena.length + " arena spawn area");
			_log.info("L2World: Loaded " + zoneCastle.length + " castle area zones");
			_log.info("L2World: Loaded " + spawnCastleDefender.length + " castle defender spawn area");
			_log.info("L2World: Loaded " + zonePeace.length + " peace zones");
			_log.info("L2World: Loaded " + zoneTown.length + " town zones");
		}
		catch (Exception e)
		{
			e.getMessage();
			e.printStackTrace();
		}
		finally {try { con.close(); } catch (Exception e) {}}
	}
	
		
	private static L2WorldRegion[][] _worldRegions;
	
	/**
	 * Constructor of L2World.<BR><BR>
	 */
	private L2World()
	{
		//_allGms	 = new ConcurrentHashMap<String, L2PcInstance>();
		_allPlayers = new ConcurrentHashMap<String, L2PcInstance>();
		_allObjects = new ConcurrentHashMap<Integer,L2Object>();

		_log.config("L2World: Setting up World Regions");
		_worldRegions = new L2WorldRegion[REGIONS_X][REGIONS_Y];

		for(int y=0; y<REGIONS_Y; y++)
			for(int x=0; x<REGIONS_X; x++)
				_worldRegions[x][y] = new L2WorldRegion(x,y);

		_log.config("L2World: ("+REGIONS_X+" by "+REGIONS_Y+") World Region Grid set up.");
	}
	
	/**
	 * Add L2Object object in _allObjects.<BR><BR>
	 *   
	 * <B><U> Example of use </U> :</B><BR><BR>
	 * <li> Withdraw an item from the warehouse, create an item</li>
	 * <li> Spawn a L2Character (PC, NPC, Pet)</li><BR>
	 */
	public static void storeObject(L2Object object)
	{
		_allObjects.put(object.getObjectId(), object);

		if(object instanceof L2PcInstance)
		{
			L2PcInstance player = (L2PcInstance)object;
			_allPlayers.put(player.getName().toLowerCase(), player);
		}
	}
	
	/**
	 * Remove L2Object object from _allObjects of L2World.<BR><BR>
	 *  
	 * <B><U> Example of use </U> :</B><BR><BR>
	 * <li> Delete item from inventory, tranfer Item from inventory to warehouse</li>
	 * <li> Crystallize item</li>
	 * <li> Remove NPC/PC/Pet from the world</li><BR>
	 * 
	 * @param object L2Object to remove from _allObjects of L2World
	 * 
	 */
	public static final void removeObject(L2Object object)
	{
		_allObjects.remove(object.getObjectId());  // suggestion by whatev
		//IdFactory.getInstance().releaseId(object.getObjectId());

		if(object instanceof L2PcInstance)
		{
			L2PcInstance player = (L2PcInstance)object;
			_allPlayers.remove(player.getName().toLowerCase());
		}
	}
	
	/**
	 * Return the L2Object object that belongs to an ID or null if no object found.<BR><BR>
	 * 
	 * <B><U> Example of use </U> :</B><BR><BR>
	 * <li> Client packets : Action, AttackRequest, RequestJoinParty, RequestJoinPledge...</li><BR>
	 * 
	 * @param oID Identifier of the L2Object
	 */
	public static final L2Object findObject(Integer oID)
	{
		return _allObjects.get(oID);
	}
	
    // Added by Tempy - 08 Aug 05
    // Allows easy retrevial of all visible objects in world.
    public static final ConcurrentHashMap<Integer,L2Object> getAllVisibleObjects()
    {
    	return _allObjects;
    }

	/**
	 * Return a collection containing all players in game.<BR><BR>
	 * 
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : Read-only, please! </B></FONT><BR><BR>
	 */
	public static Collection<L2PcInstance> getAllPlayers()
	{
		return _allPlayers.values();
	}

	/**
	 * Return the player instance corresponding to the given name.<BR><BR>
	 * 
	 * @param name Name of the player to get Instance
	 */
	public static L2PcInstance getPlayer(String name)
	{
		return _allPlayers.get(name.toLowerCase());
	}
	
	/**
	 * Remove the L2PcInstance from _allPlayers of L2World.<BR><BR>
	 *  
	 * <B><U> Example of use </U> :</B><BR><BR>
	 * <li> Remove a player fom the visible objects </li><BR>
	 * 
	 */
	public static void removeFromAllPlayers(L2PcInstance cha)
	{
		_allPlayers.remove(cha.getName().toLowerCase());
  	}
	
	/**
	 * Add a L2Object in the world.<BR><BR>
	 * 
	 * <B><U> Concept</U> :</B><BR><BR>
	 * L2Object (including L2PcInstance) are identified in <B>_visibleObjects</B> of his current L2WorldRegion and in <B>_knownObjects</B> of other surrounding L2Characters <BR>
	 * L2PcInstance are identified in <B>_allPlayers</B> of L2World, in <B>_allPlayers</B> of his current L2WorldRegion and in <B>_knownPlayer</B> of other surrounding L2Characters <BR><BR>
	 * 
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Add the L2Object object in _allPlayers* of L2World </li>
	 * <li>Add the L2Object object in _gmList** of GmListTable </li>
	 * <li>Add object in _knownObjects and _knownPlayer* of all surrounding L2WorldRegion L2Characters </li><BR>
	 * 
	 * <li>If object is a L2Character, add all surrounding L2Object in its _knownObjects and all surrounding L2PcInstance in its _knownPlayer </li><BR>
	 * 
	 * <I>*  only if object is a L2PcInstance</I><BR>
	 * <I>** only if object is a GM L2PcInstance</I><BR><BR>
	 * 
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method DOESN'T ADD the object in _visibleObjects and _allPlayers* of L2WorldRegion (need synchronisation)</B></FONT><BR>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method DOESN'T ADD the object to _allObjects and _allPlayers* of L2World (need synchronisation)</B></FONT><BR><BR>
	 * 
	 * <B><U> Example of use </U> :</B><BR><BR>
	 * <li> Drop an Item </li>
	 * <li> Spawn a L2Character</li>
	 * <li> Apply Death Penalty of a L2PcInstance </li><BR><BR>
	 * 
	 * @param object L2object to add in the world
	 * @param newregion L2WorldRegion in wich the object will be add (not used)
	 * @param dropper L2Character who has dropped the object (if necessary)
	 * 
	 */
	public static void addVisibleObject(L2Object object, @SuppressWarnings("unused") L2WorldRegion newRegion, L2Character dropper)
	{
		if(object instanceof L2PcInstance)
		{
			L2PcInstance player = (L2PcInstance)object;
			_allPlayers.put(player.getName().toLowerCase(), player);
		}
		else
			if(_allPlayers.size()==0)
				return;
	}

	/**
	 * Remove a L2Object from the world.<BR><BR>
	 * 
	 * <B><U> Concept</U> :</B><BR><BR>
	 * L2Object (including L2PcInstance) are identified in <B>_visibleObjects</B> of his current L2WorldRegion and in <B>_knownObjects</B> of other surrounding L2Characters <BR>
	 * L2PcInstance are identified in <B>_allPlayers</B> of L2World, in <B>_allPlayers</B> of his current L2WorldRegion and in <B>_knownPlayer</B> of other surrounding L2Characters <BR><BR>
	 * 
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Remove the L2Object object from _allPlayers* of L2World </li>
	 * <li>Remove the L2Object object from _visibleObjects and _allPlayers* of L2WorldRegion </li>
	 * <li>Remove the L2Object object from _gmList** of GmListTable </li>
	 * <li>Remove object from _knownObjects and _knownPlayer* of all surrounding L2WorldRegion L2Characters </li><BR>
	 * 
	 * <li>If object is a L2Character, remove all L2Object from its _knownObjects and all L2PcInstance from its _knownPlayer </li><BR><BR>
	 * 
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method DOESN'T REMOVE the object from _allObjects of L2World</B></FONT><BR><BR>
	 * 
	 * <I>*  only if object is a L2PcInstance</I><BR>
	 * <I>** only if object is a GM L2PcInstance</I><BR><BR>
	 * 
	 * <B><U> Example of use </U> :</B><BR><BR>
	 * <li> Pickup an Item </li>
	 * <li> Decay a L2Character</li><BR><BR>
	 * 
	 * @param object L2object to remove from the world
	 * @param oldregion L2WorldRegion in wich the object was before removing 
	 * 
	 */

	public static void removeVisibleObject(L2Object object, L2WorldRegion oldRegion)
	{	
		if(object == null)
			return;

		// If selected L2Object is a L2PcIntance, remove it from HashSet _allPlayers of L2World
		if(object instanceof L2PcInstance)
			removeFromAllPlayers((L2PcInstance)object);

		if(oldRegion!= null)
			oldRegion.removeVisibleObject(object.getObjectId());

		object.removeAllKnownObjects();
	}
	
	/**
	 * Calculate the current L2WorldRegions of the object according to its position (x,y).<BR><BR>
	 * 
	 * <B><U> Example of use </U> :</B><BR><BR>
	 * <li> Set position of a new L2Object (drop, spawn...) </li>
	 * <li> Update position of a L2Object after a mouvement </li><BR>
	 * 
	 * @param x X position of the object
	 * @param y Y position of the object
	 */
	public static L2WorldRegion getRegionByCoords(int x, int y)
	{		
		return getRegion((x-MAP_MIN_X)/TILE_SIZE, (y-MAP_MIN_Y)/TILE_SIZE);
	}

	public static L2WorldRegion getRegion(int tile_x, int tile_y)
	{	
		if(!validRegion(tile_x, tile_y))
		{
			_log.warning("Error region tiles: ("+tile_x+", "+tile_y+")");
			Thread.dumpStack();
			return null;
		}
		return _worldRegions[tile_x][tile_y];
	}

	/**
	 * Check if the current L2WorldRegions of the object is valid according to its position (x,y).<BR><BR>
	 * 
	 * <B><U> Example of use </U> :</B><BR><BR>
	 * <li> Init L2WorldRegions </li><BR>
	 * 
	 * @param x X position of the object
	 * @param y Y position of the object
	 * 
	 * @return True if the L2WorldRegion is valid
	 */
	public static boolean validRegion(int tile_x, int tile_y)
	{
		return (tile_x >= 0 && tile_x < REGIONS_X && tile_y >= 0 && tile_y < REGIONS_Y);
	}

	public static boolean validCoords(int x, int y)
	{
		return (x > MAP_MIN_X && x < MAP_MAX_X && y > MAP_MIN_Y && y < MAP_MAX_Y);
	}

	/**
	 * Deleted all spawns in the world.
	 */
	public static synchronized void deleteVisibleNpcSpawns()
	{
		_log.info("Deleting all visible NPC's.");
		for(int i = 0; i < REGIONS_X; i++) 
		{
			for(int j = 0; j < REGIONS_Y; j++) 
				if(_worldRegions[i][j] != null)
					_worldRegions[i][j].deleteVisibleNpcSpawns();
		}
		_log.info("All visible NPC's deleted.");
	}

	public static void RegionsCleanCheck()
	{
		for(int i = 0; i < REGIONS_X; i++) 
		{
			for(int j = 0; j < REGIONS_Y; j++) 
				if(_worldRegions[i][j] != null)
					_worldRegions[i][j].cleanCheck();
		}
	}
}
