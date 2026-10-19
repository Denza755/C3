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

import javolution.util.FastList;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.model.actor.instance.L2NpcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.tables.SpawnTable;
import net.sf.l2j.util.L2ObjectSet;

import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

/**
 * This class ...
 * 
 * @version $Revision: 1.3.4.4 $ $Date: 2005/03/27 15:29:33 $
 */
public final class L2WorldRegion
{
    private static Logger _log = Logger.getLogger(L2WorldRegion.class.getName());
    
	/** L2ObjectHashSet(L2PcInstance) containing L2PcInstance of all player in game in this L2WorldRegion */
	private L2ObjectSet<L2PcInstance> _allPlayers;
	
	/** L2ObjectHashSet(L2Object) containing L2Object visible in this L2WorldRegion */
	private L2ObjectSet<L2Object> _visibleObjects;
	
	private List<L2WorldRegion> _surroundingRegions;
    private int tileX, tileY;
	
	public L2WorldRegion(int tileX, int tileY)
	{
		_allPlayers           = L2ObjectSet.createL2PlayerSet(); //new L2ObjectHashSet<L2PcInstance>();
		_visibleObjects       = L2ObjectSet.createL2ObjectSet(); // new L2ObjectHashSet<L2Object>();
		_surroundingRegions   = new FastList<L2WorldRegion>();
		//_surroundingRegions.add(this); //done in L2World.initRegions()
        
        this.tileX  = tileX;
        this.tileY  = tileY;
	}

	/**
	 * Add the L2Object in the L2ObjectHashSet(L2Object) _visibleObjects containing L2Object visible in this L2WorldRegion <BR>
	 * If L2Object is a L2PcInstance, Add the L2PcInstance in the L2ObjectHashSet(L2PcInstance) _allPlayers
	 * containing L2PcInstance of all player in game in this L2WorldRegion <BR>
	 * Assert : object.getCurrentWorldRegion() == this
	 */
	public void addVisibleObject(L2Object object)
	{
		if (Config.ASSERT) assert object.getWorldRegion() == this;
		
		_visibleObjects.put(object);

		if (object instanceof L2PcInstance) 
		{
			_allPlayers.put((L2PcInstance)object);
		}
	}
	
	/**
	 * Remove the L2Object from the L2ObjectHashSet(L2Object) _visibleObjects in this L2WorldRegion <BR><BR>
	 * 
	 * If L2Object is a L2PcInstance, remove it from the L2ObjectHashSet(L2PcInstance) _allPlayers of this L2WorldRegion <BR>
	 * Assert : object.getCurrentWorldRegion() == this || object.getCurrentWorldRegion() == null
	 */
	public void removeVisibleObject(L2Object object)
	{
		if (Config.ASSERT) assert object.getWorldRegion() == this || object.getWorldRegion() == null;
		
		if (object == null)
		    return;
		_visibleObjects.remove(object);
		
		if (object instanceof L2PcInstance) 
		{
			_allPlayers.remove((L2PcInstance)object);
		}
	}
	
	public void addSurroundingRegion(L2WorldRegion region) {
		_surroundingRegions.add(region);
	}
	
	
	/**
	 * Return the FastList _surroundingRegions containing all L2WorldRegion around the current L2WorldRegion 
	 */
	public List<L2WorldRegion> getSurroundingRegions()
	{
		//change to return L2WorldRegion[] ?
		//this should not change after initialization, so maybe changes are not necessary 
		
		return _surroundingRegions;
	}

	public Iterator<L2PcInstance> iterateAllPlayers()
	{
		return _allPlayers.iterator();
	}
	
	public Iterator<L2Object> iterateVisibleObjects() {
		return _visibleObjects.iterator();
	}
    
    public String getName()
    {
        return "("+tileX + ", "+tileY+")";
    }

    /**
     * Deleted all spawns in the world.
     */
    public synchronized void deleteVisibleNpcSpawns()
    {
        _log.fine("Deleting all visible NPC's in Region: "+getName());
        for(L2Object obj : _visibleObjects)
        {
            if(obj instanceof L2NpcInstance)
            {
                L2NpcInstance target = (L2NpcInstance) obj;
                target.deleteMe();
                L2Spawn spawn = target.getSpawn();
                if (spawn != null)
                {
                    spawn.stopRespawn();
                    SpawnTable.getInstance().deleteSpawn(spawn, false);
                }
                _log.finest("Removed NPC "+target.getObjectId());
            }
        }
        _log.info("All visible NPC's deleted in Region: "+getName());
    }
}
