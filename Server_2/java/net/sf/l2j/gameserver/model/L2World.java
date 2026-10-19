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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.GmListTable;
import net.sf.l2j.gameserver.IdFactory;
import net.sf.l2j.util.L2ObjectHashMap;

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
	private static final int SHIFT_BY = 13;
	
	//map dimensions
	private static final int MAP_MIN_X = -131072;
	private static final int MAP_MAX_X = 196608;
	private static final int MAP_MIN_Y = -393216;
	private static final int MAP_MAX_Y = 262144;
	
	//calculated offset used so top left region is 0,0
	private static final int OFFSET_X = Math.abs(MAP_MIN_X >> SHIFT_BY);
	private static final int OFFSET_Y = Math.abs(MAP_MIN_Y >> SHIFT_BY);
	//number of regions
	private static final int REGIONS_X = (MAP_MAX_X >> SHIFT_BY) + OFFSET_X;
	private static final int REGIONS_Y = (MAP_MAX_Y >> SHIFT_BY) + OFFSET_Y;
	
	
    //private ConcurrentHashMap<String, L2PcInstance> _allGms;
	private ConcurrentHashMap<String, L2PcInstance> _allPlayers;
	private L2ObjectHashMap<L2Object> _allObjects;
	private IdFactory _idFactory;
	
	private static final L2World _instance = new L2World();
	
	public static final int[][] zones = {
			{6063,19664,17248,14019},//DE Village
			{-87312,240096,-81129,246345},//TI
			{48294,52995,43193,45911},//Elven
			{-42078,-109785,-47648,-117366},//Orc Village
			{-84892,149075,-76820,156125},//Gludin
			{117395,-176766,114824,-184347},//Dwarven
			{-11853,126610,-16652,121003},//Gludio
			{15300,141609,21570,147635},//Dion
			{76995,141424,90565,153614},//Giran
			{76696,57199,84511,50120},//Oren
			{121308,73941,114667,80383},//Hunters
			{142312,32317,152163,19708},//Aden
			{147121,46232,148016,47213},//Collusieum 1
			{150945,47214,151895,46225},//Collusieum 2
			{103598,216010,118991,225905}//Heine Spawn {111381,220896,-3551}, KSpawn {102455,225614,-3403}
	};
	
	public static final int[][] arenas = {
		{73482,142269,72507,143247},//Giran Arena Spawn {73890,142656,-3778
		{-88412,142709,-87392,141696},//Gudin Arena Spaen {-86979,142402,-3643}
		{148089,48067,150938,45359},//Collusieum
	};
	
	public static final int[][] arenaspawn = {{73890,142656,-3778},{-86979,142402,-3643},{147451,46728,-3410}};
	
	private L2WorldRegion[][] _worldRegions;
	
	private L2World()
	{
        //_allGms     = new ConcurrentHashMap<String, L2PcInstance>();
		_allPlayers = new ConcurrentHashMap<String, L2PcInstance>();
		_allObjects = new L2ObjectHashMap<L2Object>();

		_idFactory = IdFactory.getInstance();
		initRegions();
	}
	
	public static L2World getInstance()
	{
		return _instance;
	}
	
	
	/*
	 * Old methods
	 */
	
	public void storeObject(L2Object temp)
	{
		_allObjects.put(temp);
	}
	
	public void removeObject(L2Object object)
	{
		_allObjects.remove(object);  // suggestion by whatev
	}
	
	
	/**
	 * find the object that belongs to an ID
	 * @param oID
	 * @return  null if no object was found. 
	 */
	public L2Object findObject(int oID)
	{
		return _allObjects.get(oID);
	}
	
    public L2PcInstance[] getAllGMs()
    {
        return GmListTable.getInstance().getAllGms();
    }
	
	/** read-only, please! */
	public Collection<L2PcInstance> getAllPlayers()
	{
		return _allPlayers.values();
	}
	
	public L2PcInstance getPlayer(String name)
	{
		return _allPlayers.get(name.toLowerCase());
	}

	/*
	 * New L2WorldRegion stuff
	 */
	
	public void addVisibleObject(L2Object object, L2WorldRegion newRegion, L2Character dropper)
	{
		if (object instanceof L2PcInstance)
		{
			L2PcInstance player = (L2PcInstance)object;
			//old stuff to remember all players
			_allPlayers.put(player.getName().toLowerCase(), player);
            if (player.isGM()) 
                GmListTable.getInstance().addGm(player);
		}
        //get all visible objects around
        L2Object[] visible = getVisibleObjects(object, 2000);
        if (Config.DEBUG) _log.finest("objects in range:"+visible.length);
        
        // tell the player about the surroundings
        for (int i = 0; i < visible.length; i++)
        {
            object.addKnownObject(visible[i], dropper);
	        visible[i].addKnownObject(object, dropper);
		}
	}
	
	public void removeVisibleObject(L2Object object, L2WorldRegion oldRegion)
	{	
        if (oldRegion != null)
        {
            //remove visible object from it's region
            oldRegion.removeVisibleObject(object);
            //update known objects
            ArrayList<L2WorldRegion> regions = oldRegion.getSurroundingRegions();
            for (L2WorldRegion reg : regions) {
                Iterator<L2Object> iter = reg.iterateVisibleObjects();
                while (iter.hasNext())
                {
                    L2Object obj = iter.next();
                    obj.removeKnownObject(object);
                    object.removeKnownObject(obj);
                }
            }
            object.removeAllKnownObjects();
            
            //old stuff to rember all players
            if (object instanceof L2PcInstance)
            {
                _allPlayers.remove(((L2PcInstance)object).getName().toLowerCase());
                if (((L2PcInstance)object).isGM()) 
                    GmListTable.getInstance().deleteGm((L2PcInstance)object);
            }
        }
	}
	
	/**
	 * returns all visible objects in nearest regions
	 * @param object
	 * @return
	 */
	public L2Object[] getVisibleObjects(L2Object object)
	{
		L2WorldRegion reg = object.getCurrentWorldRegion();
		if (reg == null)
			return new L2Object[0];
		ArrayList<L2Object> result = new ArrayList<L2Object>();
		
		//get all regions around
		ArrayList<L2WorldRegion> _regions = reg.getSurroundingRegions();
		
		for (int i = 0; i < _regions.size(); i++) {
			//get visible objects in region & do stuff if needed
			Iterator<L2Object> _objects = _regions.get(i).iterateVisibleObjects();
			while (_objects.hasNext())
			{
				L2Object _object = _objects.next();
				if (_object.equals(object))
					continue;	// skip our own character
				if (!_object.isVisible())
					continue;	// skip dying objects
				result.add(_object);
			}
		}
		
		return result.toArray(new L2Object[result.size()]);
	}
		
	/**
	 * returns all visible objects in range
	 * @param object
	 * @param radius
	 * @return
	 */
	public L2Object[] getVisibleObjects(L2Object object, int radius)
	{
		if (object == null || !object.isVisible())
			return new L2Object[0];
		int x = object.getX();
		int y = object.getY();
		int sqRadius = radius*radius;
		ArrayList<L2Object> result = new ArrayList<L2Object>();
		
		//get all regions around
		ArrayList<L2WorldRegion> _regions = object.getCurrentWorldRegion().getSurroundingRegions();
		
		for (int i = 0; i < _regions.size(); i++) {
			//get visible objects in region & do stuff if needed
			Iterator<L2Object> _objects = _regions.get(i).iterateVisibleObjects();
			while (_objects.hasNext())
			{
				L2Object _object = _objects.next();
				if (_object.equals(object)) continue;	// skip our own character
				
				int x1 = _object.getX();
				int y1 = _object.getY();
				
				int dx = x1 - x;
				//if (dx > radius || -dx > radius)
				//	continue;
				int dy = y1 - y;
				//if (dy > radius || -dy > radius)
				//	continue;
				if (dx*dx + dy*dy < sqRadius)
					result.add(_object);
			}
		}
		
		return result.toArray(new L2Object[result.size()]);
	}
		
	/**
	 * returns all visible objects in range
	 * @param object
	 * @param radius
	 * @return
	 */
	public L2Object[] getVisibleObjects3D(L2Object object, int radius)
	{
		if (object == null || !object.isVisible())
			return new L2Object[0];
		int x = object.getX();
		int y = object.getY();
		int z = object.getZ();
		int sqRadius = radius*radius;
		ArrayList<L2Object> result = new ArrayList<L2Object>();
		
		//get all regions around
		ArrayList<L2WorldRegion> _regions = object.getCurrentWorldRegion().getSurroundingRegions();
		
		for (int i = 0; i < _regions.size(); i++) {
			//get visible objects in region & do stuff if needed
			Iterator<L2Object> _objects = _regions.get(i).iterateVisibleObjects();
			while (_objects.hasNext())
			{
				L2Object _object = _objects.next();
				if (_object.equals(object)) continue;	// skip our own character
				
				int x1 = _object.getX();
				int y1 = _object.getY();
				int z1 = _object.getZ();
				
				int dx = x1 - x;
				//if (dx > radius || -dx > radius)
				//	continue;
				int dy = y1 - y;
				//if (dy > radius || -dy > radius)
				//	continue;
				int dz = z1 - z;
				if (dx*dx + dy*dy + dz*dz < sqRadius)
					result.add(_object);
			}
		}
		
		return result.toArray(new L2Object[result.size()]);
	}
		
	public L2WorldRegion getRegion(int x, int y) {		
		return _worldRegions[(x >> SHIFT_BY) + OFFSET_X][(y >> SHIFT_BY) + OFFSET_Y];
	}
	
	private boolean validRegion(int x, int y)
	{
	    return (x >= 0 && x < REGIONS_X && y >= 0 && y < REGIONS_Y);
	}
	
	private void initRegions() {
		_log.config("setting up world regions ...");
		_worldRegions = new L2WorldRegion[REGIONS_X+1][REGIONS_Y+1];
		for (int i = 0; i <= REGIONS_X; i++) {
			for (int j = 0; j <= REGIONS_Y; j++) {
				_worldRegions[i][j] = new L2WorldRegion();
			}
		}
		
		for (int x=0; x < REGIONS_X; x++)
		{
		    for (int y=0; y < REGIONS_Y; y++)
		    {
		        for (int a = -1; a <= 1; a++)
		        {
					for (int b = -1; b <= 1; b++) 
					{
					    if (validRegion(x + a, y + b))
					    {
					        _worldRegions[x + a][y + b].addSurroundingRegion(_worldRegions[x][y]);
					    }
					}
		        }
		    }
		}
		_log.config("... done ("+REGIONS_X+"x"+REGIONS_Y+" set up)");
		
	}
}
