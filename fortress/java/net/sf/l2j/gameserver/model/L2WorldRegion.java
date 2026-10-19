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
import java.util.logging.Logger;
import java.util.concurrent.CopyOnWriteArraySet;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.SpawnTable;
import net.sf.l2j.gameserver.ai.CtrlIntention;
import net.sf.l2j.gameserver.lib.Rnd;

/**
 * This class ...
 * 
 * @version $Revision: 1.3.4.4 $ $Date: 2005/03/27 15:29:33 $
 */
public final class L2WorldRegion
{
	private static Logger _log = Logger.getLogger(L2WorldRegion.class.getName());
	
	public CopyOnWriteArraySet<Integer> _players;
	public CopyOnWriteArraySet<Integer> _aroundPlayers;
	public CopyOnWriteArraySet<Integer> _objects;

	public int tileX, tileY;
	
	public L2WorldRegion(int tileX, int tileY)
	{
		this.tileX  = tileX;
		this.tileY  = tileY;
	}

	public synchronized final CopyOnWriteArraySet<Integer> getObjects()
	{
		if(_objects == null)
			_objects = new CopyOnWriteArraySet<Integer>();
		return _objects;
	}

	public synchronized final CopyOnWriteArraySet<Integer> getPlayers()
	{
		if(_players == null)
			_players = new CopyOnWriteArraySet<Integer>();
		return _players;
	}

	public synchronized final CopyOnWriteArraySet<Integer> getAroundPlayers()
	{
		if(_aroundPlayers == null)
			_aroundPlayers = new CopyOnWriteArraySet<Integer>();
		return _aroundPlayers;
	}

	public synchronized final void removeObject(Integer object_id)
	{
		if(_objects == null)
			return;

		_objects.remove(object_id);

		if(Rnd.get(500)==0)
			for(Integer oid : _objects)
				if(L2World.findObject(oid) == null)
					_objects.remove(oid);

		if(_objects.isEmpty())
			_objects = null;
	}

	public synchronized final void removePlayer(Integer player_id)
	{
		if(_players == null)
			return;

		_players.remove(player_id);

		if(Rnd.get(100)==0)
			for(Integer pid : _players)
				if(L2World.findObject(pid) == null)
					_players.remove(pid);

		if(_players.isEmpty())
			_players = null;
	}

	public synchronized final void removeAroundPlayer(Integer player_id)
	{
		if(_aroundPlayers == null)
			return;

		_aroundPlayers.remove(player_id);

		if(Rnd.get(100)==0)
			for(Integer pid : _aroundPlayers)
				if(L2World.findObject(pid) == null)
					_aroundPlayers.remove(pid);

		if(_aroundPlayers.isEmpty())
			_aroundPlayers = null;

		if(_objects == null || _aroundPlayers != null)
			return;

		for(Integer oid : _objects)
		{
			L2Object obj = L2World.findObject(oid);
			if(obj != null && obj.getIntention() != CtrlIntention.AI_INTENTION_IDLE)
				obj.setIntention(CtrlIntention.AI_INTENTION_IDLE, null, null);
		}

//		L2World.freeRegion(this);
	}

	/**
	 * Add the L2Object in the HashSet _visibleObjects containing L2Object visible in this L2WorldRegion <BR>
	 * If L2Object is a L2PcInstance, Add the L2PcInstance in the HashSet _allPlayers
	 * containing L2PcInstance of all player in game in this L2WorldRegion <BR>
	 * Assert : object.getCurrentWorldRegion() == this
	 */

	public final void addVisibleObject(L2Object object, L2Character dropper)
	{
		Integer object_id = object.getObjectId();

		if(object instanceof L2PcInstance)
			getPlayers().add(object_id);

		if(!object.isVisible())
			return;

		getObjects().add(object_id);

		if(isKnownPlayers() && object.getIntention() == CtrlIntention.AI_INTENTION_IDLE)
			object.setIntention(CtrlIntention.AI_INTENTION_ACTIVE, null, null);

		for(int y = tileY-L2World.PLAYER_VISIBLE_TILES; y <= tileY+L2World.PLAYER_VISIBLE_TILES; y++)
			for(int x = tileX-L2World.PLAYER_VISIBLE_TILES; x <= tileX+L2World.PLAYER_VISIBLE_TILES; x++)
				if(L2World.validRegion(x,y))
					L2World.getRegion(x,y).addKnownObject(tileX, tileY, object, dropper);
	}

	private synchronized final void addAroundPlayer(Integer player_id)
	{
		boolean not_was_players = !isKnownPlayers();
		getAroundPlayers().add(player_id);

		if(not_was_players && _objects != null)
		{
			for(Integer oid : _objects)
			{
				L2Object obj = L2World.findObject(oid);
				if(obj != null && obj.getIntention() == CtrlIntention.AI_INTENTION_IDLE)
					obj.setIntention(CtrlIntention.AI_INTENTION_ACTIVE, null, null);
			}
		}
	}

	public final void addKnownObject(int tile_x, int tile_y, L2Object object, L2Character dropper)
	{
		Integer object_id = object.getObjectId();
		Integer distance = Math.max(Math.abs(tile_x-tileX), Math.abs(tile_y-tileY));

		if(object instanceof L2PcInstance)
			addAroundPlayer(object_id);

		if(_objects == null)
			return;

		for(Integer oid : _objects)
		{
			L2Object obj = L2World.findObject(oid);
			if(obj == null)
				continue;

			if(distance <= object.getDistanceToWatchObject(obj)/L2World.TILE_SIZE)
				object.addKnownObject(obj, dropper);

			if(distance <= obj.getDistanceToWatchObject(object)/L2World.TILE_SIZE)
				obj.addKnownObject(object, dropper);
		}
	}

	/**
	 * Remove the L2Object from the HashSet _visibleObjects in this L2WorldRegion <BR><BR>
	 * 
	 * If L2Object is a L2PcInstance, remove it from the HashSet _allPlayers of this L2WorldRegion <BR>
	 * Assert : object.getCurrentWorldRegion() == this || object.getCurrentWorldRegion() == null
	 */

	public final void removeVisibleObject(Integer object_id)
	{
		//L2Object object = L2World.findObject(object_id);

		removeObject(object_id);
		removePlayer(object_id);

		for(int y = tileY-L2World.PLAYER_VISIBLE_TILES; y <= tileY+L2World.PLAYER_VISIBLE_TILES; y++)
			for(int x = tileX-L2World.PLAYER_VISIBLE_TILES; x <= tileX+L2World.PLAYER_VISIBLE_TILES; x++)
				if(L2World.validRegion(x,y))
					L2World.getRegion(x,y).removeKnownObject(tileX, tileY, object_id);
	}

	public final void removeKnownObject(int tile_x, int tile_y, Integer object_id)
	{
		L2Object object = L2World.findObject(object_id);

		if(object == null)
		{
			removeObject(object_id);
			removePlayer(object_id);
			removeAroundPlayer(object_id);

			return;
		}

		Integer distance = Math.max(Math.abs(tile_x-tileX), Math.abs(tile_y-tileY));

		if(object instanceof L2PcInstance)
			removeAroundPlayer(object_id);

		if(_objects == null)
			return;

		for(Integer oid : _objects)
		{
			if(oid == object_id)
				continue;

			L2Object obj = L2World.findObject(oid);
			if(obj == null)
				continue;

			if(distance <= object.getDistanceToWatchObject(obj)/L2World.TILE_SIZE)
				object.removeKnownObject(obj);

			if(distance <= obj.getDistanceToWatchObject(object)/L2World.TILE_SIZE)
				obj.removeKnownObject(object);
		}
	}

	private final void errorMoveObject(L2Object obj, L2WorldRegion old)
	{
		_log.warning("Error move object "+obj+" from "+old+" to "+this);
		old.removeVisibleObject(obj.getObjectId());
		addVisibleObject(obj, null);
	}

	public final void moveObject(L2Object object, L2WorldRegion old)
	{
		if(old == this)
			return;

		int dx = tileX - old.tileX;
		int dy = tileY - old.tileY;

		int adx = Math.abs(dx);
		int ady = Math.abs(dy);

		if(adx == 1 && ady == 1) // Move diagonale offset in two steps.
		{
			L2WorldRegion tmp = L2World.getRegion(old.tileX, old.tileY+dy);
			tmp.moveObject(object, old);
			moveObject(object, tmp);
			return;
		}
		
		if(dx*dy != 0)
		{
			errorMoveObject(object, old);
			return;
		}

		if(adx+ady != 1)
		{
			errorMoveObject(object, old);
			return;
		}

		Integer object_id = object.getObjectId();
		old.removeObject(object_id);
		old.removePlayer(object_id);

		if(object instanceof L2PcInstance)
			getPlayers().add(object_id);

		if(!object.isVisible())
			return;

		getObjects().add(object_id);

		int xmin_disp = 0;
		int xmax_disp = 0;
		if(dx==1)
			xmin_disp = -1;
		else if(dx==-1)
			xmax_disp = 1;

		int ymin_disp = 0;
		int ymax_disp = 0;
		if(dy==1)
			ymin_disp = -1;
		else if(dy==-1)
			ymax_disp = 1;

		for(int y = tileY-L2World.PLAYER_VISIBLE_TILES+ymin_disp; y <= tileY+L2World.PLAYER_VISIBLE_TILES+ymax_disp; y++)
			for(int x = tileX-L2World.PLAYER_VISIBLE_TILES-xmin_disp; x <= tileX+L2World.PLAYER_VISIBLE_TILES+xmax_disp; x++)
				if(L2World.validRegion(x,y))
					L2World.getRegion(x,y).moveKnownObject(this, old, object);
	}
	

	public final void moveKnownObject(L2WorldRegion reg, L2WorldRegion old, L2Object object)
	{
		Integer object_id = object.getObjectId();
		Integer distance = Math.max(Math.abs(reg.tileX-tileX), Math.abs(reg.tileY-tileY));
		Integer old_dist = Math.max(Math.abs(old.tileX-tileX), Math.abs(old.tileY-tileY));

        if(old_dist == distance)
			return;

		if(object instanceof L2PcInstance)
		{
			if(distance <= L2World.PLAYER_VISIBLE_TILES && old_dist > L2World.PLAYER_VISIBLE_TILES)
				addAroundPlayer(object_id);

			if(distance > L2World.PLAYER_VISIBLE_TILES && old_dist <= L2World.PLAYER_VISIBLE_TILES)
				removeAroundPlayer(object_id);
		}

		if(_objects == null)
			return;

		for(Integer oid : _objects)
		{
			if(oid == object_id)
				continue;

			L2Object obj = L2World.findObject(oid);
			if(obj == null)
				continue;

			Integer dwto   = object.getDistanceToWatchObject(obj)/L2World.TILE_SIZE;
			Integer dwfrom = obj.getDistanceToWatchObject(object)/L2World.TILE_SIZE;

			if(old_dist >  dwto	&& distance <= dwto)
				object.addKnownObject(obj, null);

			if(old_dist <= dwto	&& distance >  dwto)
				object.removeKnownObject(obj);
			
			if(old_dist >  dwfrom && distance <= dwfrom)
				obj.addKnownObject(object, null);

			if(old_dist <= dwfrom && distance  > dwfrom)
				obj.removeKnownObject(object);
		}

		return;
	}
	
	public final CopyOnWriteArraySet<Integer> getRawVisibleObjects()
	{
		return _objects;
	}

	public final ArrayList<L2Object> getVisibleObjects(L2Object object, int range)
	{
		ArrayList<L2Object> ret = new ArrayList<L2Object>();
		//Integer object_id = object.getObjectId();

		int tile_range = range/L2World.TILE_SIZE;

		for(int y = tileY-tile_range; y <= tileY+tile_range; y++)
		{
			for(int x = tileX-tile_range; x <= tileX+tile_range; x++)
				if(L2World.validRegion(x,y) && L2World.getRegion(x,y)._objects != null)
					for(Integer oid : L2World.getRegion(x,y)._objects)
					{
						L2Object obj = L2World.findObject(oid);
						if(obj != null && obj != object && object.isInRange(obj, range))
							ret.add(obj);
					}
			
		}

		return ret;
	}

	public final ArrayList<L2Character> getVisibleCharacters(L2Object object, int range)
	{
		ArrayList<L2Character> ret = new ArrayList<L2Character>();
		//Integer object_id = object.getObjectId();

		int tile_range = range/L2World.TILE_SIZE;

		for(int y = tileY-tile_range; y <= tileY+tile_range; y++)
		{
			for(int x = tileX-tile_range; x <= tileX+tile_range; x++)
				if(L2World.validRegion(x,y) && L2World.getRegion(x,y)._objects != null)
					for(Integer oid : L2World.getRegion(x,y)._objects)
					{
						L2Object obj = L2World.findObject(oid);
						if(obj != null && obj != object && obj instanceof L2Character && object.isInRange(obj, range))
							ret.add((L2Character)obj);
					}
			
		}

		return ret;
	}

	public final ArrayList<L2Character> getVisibleCharacters(L2Object object, int radius, int height)
	{
		ArrayList<L2Character> ret = new ArrayList<L2Character>();
		//Integer object_id = object.getObjectId();

		int tile_range = radius/L2World.TILE_SIZE;
		int z = object.getZ();

		for(int y = tileY-tile_range; y <= tileY+tile_range; y++)
		{
			for(int x = tileX-tile_range; x <= tileX+tile_range; x++)
				if(L2World.validRegion(x,y) && L2World.getRegion(x,y)._objects != null)
					for(Integer oid : L2World.getRegion(x,y)._objects)
					{
						L2Object obj = L2World.findObject(oid);
						if(obj != null 
								&& obj != object 
								&& Math.abs(obj.getZ()-z) <= height
								&& obj instanceof L2Character 
								&& object.isInRange(obj, radius))
							ret.add((L2Character)obj);
					}
			
		}

		return ret;
	}

	public final ArrayList<L2NpcInstance> getVisibleNpcs(L2Object object, int range)
	{
		ArrayList<L2NpcInstance> ret = new ArrayList<L2NpcInstance>();
		//Integer object_id = object.getObjectId();

		int tile_range = range/L2World.TILE_SIZE;

		for(int y = tileY-tile_range; y <= tileY+tile_range; y++)
		{
			for(int x = tileX-tile_range; x <= tileX+tile_range; x++)
				if(L2World.validRegion(x,y) && L2World.getRegion(x,y)._objects != null)
					for(Integer oid : L2World.getRegion(x,y)._objects)
					{
						L2Object obj = L2World.findObject(oid);
						if(obj != null && obj != object && obj instanceof L2NpcInstance && object.isInRange(obj, range))
							ret.add((L2NpcInstance)obj);
					}
			
		}

		return ret;
	}

	public final ArrayList<L2PcInstance> getAllPlayers(L2Object object, int range)
	{
		ArrayList<L2PcInstance> ret = new ArrayList<L2PcInstance>();
		//Integer object_id = object.getObjectId();

		int tile_range = range/L2World.TILE_SIZE;

		for(int y = tileY-tile_range; y <= tileY+tile_range; y++)
		{
			for(int x = tileX-tile_range; x <= tileX+tile_range; x++)
			{
				if(!L2World.validRegion(x,y))
					continue;

				CopyOnWriteArraySet<Integer> players = L2World.getRegion(x,y)._players;

				if(players == null)
					continue;

				for(Integer oid : players)
				{
					L2Object obj = L2World.findObject(oid);
					if(obj != null && obj != object && object.isInRange(obj, range))
						ret.add((L2PcInstance)obj);
				}
			}
		}
		return ret;
	}

	public final ArrayList<L2PcInstance> getVisiblePlayers(L2Object object, int range)
	{
		ArrayList<L2PcInstance> ret = new ArrayList<L2PcInstance>();

		//Integer object_id = object.getObjectId();

		int tile_range = range/L2World.TILE_SIZE;

		for(int y = tileY-tile_range; y <= tileY+tile_range; y++)
		{
			for(int x = tileX-tile_range; x <= tileX+tile_range; x++)
				if(L2World.validRegion(x,y) && L2World.getRegion(x,y)._players != null)
					for(Integer pid : L2World.getRegion(x,y)._players)
					{
						L2Object player = L2World.findObject(pid);
						if(player != null 
								&& player != object
								&& player.isVisible()
								&& object.isInRange(player, range))
							ret.add((L2PcInstance)player);
					}
			
		}

		return ret;
	}

	public String getName()
	{
		return "("+tileX + ", "+tileY+")";
	}

	/**
	 * Deleted all spawns in the world.
	 */
	public final synchronized void deleteVisibleNpcSpawns()
	{
		if(Config.DEBUG)
			_log.fine("Deleting all visible NPC's in Region: "+getName());

		if(_objects == null)
			return;

		for(Integer oid : _objects)
		{
			L2Object obj = L2World.findObject(oid);
			if(obj == null)
				removeVisibleObject(oid);
			else if(obj instanceof L2NpcInstance)
			{
				L2NpcInstance target = (L2NpcInstance) obj;
				target.deleteMe();
				L2Spawn spawn = target.getSpawn();
				if (spawn != null)
				{
					spawn.stopRespawn();
					SpawnTable.getInstance().deleteSpawn(spawn, false);
				}
				if(Config.DEBUG)
					_log.finest("Removed NPC "+target.getObjectId());
			}
		}

		if(Config.DEBUG)
			_log.info("All visible NPC's deleted in Region: "+getName());
	}

	public final boolean nearBy(L2WorldRegion reg)
	{
		return reg != null && Math.abs(tileX - reg.tileX) <= 1 && Math.abs(tileY - reg.tileY) <= 1;
	}

	public String toString()
	{
		return "region("+tileX+", "+tileY+")";
	}

	public final boolean isThisRegionPlayers()
	{
		return _players != null && !_players.isEmpty();
	}

	public final boolean isKnownPlayers()
	{
		return _aroundPlayers != null && !_aroundPlayers.isEmpty();
	}

	public final void cleanCheck()
	{
		if(isKnownPlayers())
			return;

		if(_objects==null)
			return;
		
		for(Integer oid : _objects)
		{
			L2Object obj = L2World.findObject(oid);
			if(obj == null)
				continue;

			if(obj.hasAI())
			{
				if(obj.getIntention() != CtrlIntention.AI_INTENTION_IDLE)
					obj.setIntention(CtrlIntention.AI_INTENTION_IDLE, null, null);
				obj.setAI(null);
                _log.config("Cleaning AI, region "+tileX+":"+tileY+", oid "+oid);
			}
		}
	}
}
