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

import java.util.logging.Logger;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.GameServer;
import net.sf.l2j.gameserver.ai.CtrlIntention;
import net.sf.l2j.gameserver.ai.L2CharacterAI;
import net.sf.l2j.gameserver.idfactory.IdFactory;
import net.sf.l2j.gameserver.lib.Rnd;
import net.sf.l2j.gameserver.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.serverpackets.GetItem;

/**
 * Mother class of all objects in the world wich ones is it possible 
 * to interact (PC, NPC, Item...)<BR><BR>
 * 
 * L2Object :<BR><BR>
 * <li>L2Character</li>
 * <li>L2ItemInstance</li>
 * <li>L2Potion</li> 
 * 
 * @version $Revision: 1.15.2.8.2.6 $ $Date: 2005/03/31 16:07:50 $
 */

public abstract class L2Object
{
	private static final Logger _log = Logger.getLogger(L2Object.class.getName());
	
	/** Object identifier */
	protected Integer _objectId;
	
	/** Object localisation : Used for items/chars that are seen in the world */
	private int _x;
	private int _y;
	private int _z;
	private L2WorldRegion _worldRegion;
	
	private int _polyid;
	private String _polytype;
	
	/** Object visibility */
	private boolean _hidden;

	
	/**
	 * Constructor of L2Object.<BR><BR>
	 */
	public L2Object(Integer objectId)
	{
		_objectId = objectId;
	}
	
	/**
	 * Return the identifier of the L2Object.<BR><BR>
	 * 
	 * @ - deprecated?
	 */
	public final int hashCode() {
		return _objectId;
	}
	
	/**
	 * Return the identifier of the L2Object.<BR><BR>
	 */
	public final Integer getObjectId()
	{
		return _objectId;
	}

	/**
	 * Return the x position of the L2Object.<BR><BR>
	 * 
	 * <B><U> Assert </U> :</B><BR><BR>
	 * <li> _worldRegion != null || _hidden</li><BR>
	 * 
	 */
	public final int getX()
	{
//		if (Config.ASSERT) assert _worldRegion != null || _hidden;
		return _x;
	}


	/**
	 * Return the y position of the L2Object.<BR><BR>
	 *  
	 * <B><U> Assert </U> :</B><BR><BR>
	 * <li> _worldRegion != null || _hidden</li><BR>
	 */
	public final int getY()
	{
//		if (Config.ASSERT) assert _worldRegion != null || _hidden;
		return _y;
	}

	/**
	 * Return the z position of the L2Object.<BR><BR>
	 *  
	 * <B><U> Assert </U> :</B><BR><BR>
	 * <li> _worldRegion != null || _hidden</li><BR>
	 */
	public final int getZ()
	{
//		if (Config.ASSERT) assert _worldRegion != null || _hidden;
		return _z;
	}

	public void setPolyInfo(String polytype, String polyid)
	{
		_polytype = polytype;
		_polyid = Integer.parseInt(polyid);		
	}
		
	/**
	 * Set the x,y,z position of the L2Object and if necessary modify its _worldRegion.<BR><BR>
	 *
	 * <B><U> Assert </U> :</B><BR><BR>
	 * <li> _worldRegion != null</li><BR><BR>
	 * 
	 * <B><U> Example of use </U> :</B><BR><BR>
	 * <li> Update position during and after movement, or after teleport </li><BR>
	 * 
	 */

	public final void setXYZ(int x, int y, int z)
	{
//		if (Config.ASSERT) assert _worldRegion != null;
		
		_x = x;
		_y = y;
		_z = z;

        if(!L2World.validCoords(x, y))
        {
            _log.warning("Object "+this+" ("+_objectId+") at bad coords: ("+_x+", "+_y+").");
            Thread.dumpStack();
		    if(this instanceof L2PcInstance)
				((L2PcInstance)this).deleteMe();
		    else if (this instanceof L2Character)
				this.decayMe();
			return;
        }

		L2WorldRegion newRegion = L2World.getRegionByCoords(_x, _y);

		if(newRegion != _worldRegion)
		{
//	        if(this instanceof L2PcInstance)
//      		_log.info("setXYZ new region "+newRegion+"; old region="+_worldRegion+"; nearby="+newRegion.nearBy(_worldRegion));
			
			if(newRegion.nearBy(_worldRegion))
				newRegion.moveObject(this, _worldRegion);
			else
			{
				if(_worldRegion != null)
					_worldRegion.removeVisibleObject(_objectId);
				newRegion.addVisibleObject(this, null);
			}
			_worldRegion = newRegion;
		}
	}
	
	/**
	 * Set the x,y,z position of the L2Object and make it invisible.<BR><BR>
	 * 
	 * <B><U> Concept</U> :</B><BR><BR>
	 * A L2Object is invisble if <B>_hidden</B>=true or <B>_worldregion</B>==null <BR><BR>
	 * 
	 * <B><U> Assert </U> :</B><BR><BR>
	 * <li> _worldregion==null <I>(L2Object is invisible)</I></li><BR><BR>
	 *  
	 * <B><U> Example of use </U> :</B><BR><BR>
	 * <li> Create a Door</li>
	 * <li> Restore L2PcInstance</li><BR>
	 * 
	 */
	public final void setXYZInvisible(int x, int y, int z)
	{
		if (Config.ASSERT) assert _worldRegion == null;
		if (x > L2World.MAP_MAX_X)
			x = L2World.MAP_MAX_X - 5000;
		if (x < L2World.MAP_MIN_X)
			x = L2World.MAP_MIN_X + 5000;
		if (y > L2World.MAP_MAX_Y)
			y = L2World.MAP_MAX_Y - 5000;
		if (y < L2World.MAP_MIN_Y)
			y = L2World.MAP_MIN_Y + 5000;
		_x = x;
		_y = y;
		_z = z;
		_hidden = true;
		
	}
	
	public boolean isPolymorphed()
	{
		return _polytype!=null;
	}
	
	public String getPolytype()
	{
		return _polytype;
	}
	
	public int getPolyid()
	{
		return _polyid;
	}
	
	/**
	 * Return the visibilty state of the L2Object. <BR><BR>
	 *  
	 * <B><U> Concept</U> :</B><BR><BR>
	 * A L2Object is invisble if <B>_hidden</B>=true or <B>_worldregion</B>==null <BR><BR>
	 * 
	 */
    public final boolean isVisible() 
	{
		return !_hidden && _worldRegion != null;
    }


	/**
	 * Init a dropped L2ItemInstance and add it in the world as a visible object.<BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Set the x,y,z position of the L2ItemInstance dropped and update its _worldregion </li>
	 * <li>Add the L2ItemInstance dropped to _visibleObjects of its L2WorldRegion</li>
	 * <li>Add the L2ItemInstance dropped in the world as a <B>visible</B> object</li><BR><BR>
	 * 
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method DOESN'T ADD the object to _allObjects of L2World </B></FONT><BR><BR>
	 * 
	 * <B><U> Assert </U> :</B><BR><BR>
	 * <li> this instanceof L2ItemInstance</li>
	 * <li> _worldRegion == null <I>(L2Object is invisible at the beginning)</I></li><BR><BR>
	 *  
	 * <B><U> Example of use </U> :</B><BR><BR>
	 * <li> Drop item</li>
	 * <li> Call Pet</li><BR>
	 * 
	 */
	public final void dropMe(L2Character dropper, int x, int y, int z) 
	{
		if (Config.ASSERT) assert this instanceof L2ItemInstance;
		if (Config.ASSERT) assert _worldRegion == null;
		
		synchronized(this)
		{
			// Set the x,y,z position of the L2ItemInstance dropped and update its _worldregion
			_hidden = false;
			_x = x;
			_y = y;
			_z = z;
			_worldRegion = L2World.getRegionByCoords(_x, _y);
			
			// Add the L2ItemInstance dropped to _visibleObjects of its L2WorldRegion
			_worldRegion.addVisibleObject(this, dropper);
		}

		// this can synchronize on others instancies, so it's out of
		// synchronized, to avoid deadlocks
		// Add the L2ItemInstance dropped in the world as a visible object
		L2World.addVisibleObject(this, _worldRegion, dropper);
	}
	
	/**
	 * Init the position of a L2Oject spawn and add it in the world as a visible object.<BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Set the x,y,z position of the L2Object spawn and update its _worldregion </li>
	 * <li>Add the L2Oject spawn in the _allobjects of L2World </li>
	 * <li>Add the L2Oject spawn to _visibleObjects of its L2WorldRegion</li>
	 * <li>Add the L2Oject spawn in the world as a <B>visible</B> object</li><BR><BR>
	 * 
	 * <B><U> Assert </U> :</B><BR><BR>
	 * <li> _worldRegion == null <I>(L2Object is invisible at the beginning)</I></li><BR><BR>
	 *  
	 * <B><U> Example of use </U> :</B><BR><BR>
	 * <li> Create Door</li>
	 * <li> Spawn : Monster, Minion, CTs, Summon...</li><BR>
	 * 
	 */
	public final void spawnMe(int x, int y, int z) 
	{
		if(Config.ASSERT) assert _worldRegion == null;

		synchronized (this) 
		{
			// Set the x,y,z position of the L2Object spawn and update its _worldregion
			_hidden = false;

			if (x > L2World.MAP_MAX_X)
				x = L2World.MAP_MAX_X - 5000;
			if (x < L2World.MAP_MIN_X)
				x = L2World.MAP_MIN_X + 5000;
			if (y > L2World.MAP_MAX_Y)
				y = L2World.MAP_MAX_Y - 5000;
			if (y < L2World.MAP_MIN_Y)
				y = L2World.MAP_MIN_Y + 5000;
			
			_x = x;
			_y = y;
			_z = z;

			_worldRegion = L2World.getRegionByCoords(_x, _y);
			
			// Add the L2Oject spawn in the _allobjects of L2World
			L2World.storeObject(this);
			
			// Add the L2Oject spawn to _visibleObjects and if necessary to _allplayers of its L2WorldRegion
			_worldRegion.addVisibleObject(this, null);
		}
		
		// this can synchronize on others instancies, so it's out of
		// synchronized, to avoid deadlocks
		// Add the L2Object spawn in the world as a visible object
		L2World.addVisibleObject(this, _worldRegion, null);
	}

    public final void spawnMe() 
    {
        if(Config.ASSERT) assert _worldRegion == null;

        synchronized(this) 
        {
            // Set the x,y,z position of the L2Object spawn and update its _worldregion
            _hidden = false;
            _worldRegion = L2World.getRegionByCoords(_x, _y);
            
            // Add the L2Oject spawn in the _allobjects of L2World
            L2World.storeObject(this);
            
            // Add the L2Oject spawn to _visibleObjects and if necessary to _allplayers of its L2WorldRegion
            _worldRegion.addVisibleObject(this, null);
        }
        
        // this can synchronize on others instancies, so it's out of
        // synchronized, to avoid deadlocks
        // Add the L2Oject spawn in the world as a visible object
        L2World.addVisibleObject(this, _worldRegion, null);
    }

    public void toggleVisible()
    {
        if (isVisible())
            decayMe();
        else
            spawnMe();
    }
	
	
	/**
	 * Do Nothing.<BR><BR>
	 * 
	 * <B><U> Overriden in </U> :</B><BR><BR>
	 * <li> L2GuardInstance :  Set the home location of its L2GuardInstance </li>
	 * <li> L2Attackable    :  Reset the Spoiled falg </li><BR><BR>
	 * 
	 */
	public void OnSpawn()
	{
		
	}

	
	/**
	 * Remove a L2ItemInstance from the world and send server->client GetItem packets.<BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Send a Server->Client Packet GetItem to player that pick up and its _knowPlayers member </li>
	 * <li>Remove the L2Object from the world</li><BR><BR>
	 * 
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method DOESN'T REMOVE the object from _allObjects of L2World </B></FONT><BR><BR>
	 * 
	 * <B><U> Assert </U> :</B><BR><BR>
	 * <li> this instanceof L2ItemInstance</li>
	 * <li> _worldRegion != null <I>(L2Object is visible at the beginning)</I></li><BR><BR>
	 *  
	 * <B><U> Example of use </U> :</B><BR><BR>
	 * <li> Do Pickup Item : PCInstance and Pet</li><BR><BR>
	 * 
	 * @param player Player that pick up the item
	 * 
	 */
	public final void pickupMe(L2Character player) 
	{
		if (Config.ASSERT) assert this instanceof L2ItemInstance;
		if (Config.ASSERT) assert _worldRegion != null;
		
		L2WorldRegion oldregion = _worldRegion;
		
		// Create a server->client GetItem packet to pick up the L2ItemInstance
		player.broadcastPacket(new GetItem((L2ItemInstance)this, player.getObjectId()));
		
		synchronized(this) 
		{
			_hidden = true;
			_worldRegion = null;
		}
		
		// this can synchronize on others instancies, so it's out of
		// synchronized, to avoid deadlocks
		// Remove the L2ItemInstance from the world
		L2World.removeVisibleObject(this, oldregion);
	}

	/**
	 * Remove a L2Object from the world.<BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Remove the L2Object from the world</li><BR><BR>
	 * 
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method DOESN'T REMOVE the object from _allObjects of L2World </B></FONT><BR>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method DOESN'T SEND Server->Client packets to players</B></FONT><BR><BR>
	 * 
	 * <B><U> Assert </U> :</B><BR><BR>
	 * <li> _worldRegion != null <I>(L2Object is visible at the beginning)</I></li><BR><BR>
	 *  
	 * <B><U> Example of use </U> :</B><BR><BR>
	 * <li> Delete NPC/PC or Unsummon</li><BR><BR>
	 * 
	 */
	public final void decayMe() 
	{
		if (Config.ASSERT) assert _worldRegion != null;
		
		L2WorldRegion reg = _worldRegion;
		
		synchronized(this) 
		{
			_hidden = true;
			_worldRegion = null;
		}
		
		// this can synchronize on others instancies, so it's out of
		// synchronized, to avoid deadlocks
		// Remove the L2Object from the world
		L2World.removeVisibleObject(this, reg);
	}

	@SuppressWarnings("static-access")
	public void onAction(L2PcInstance player)
	{
		if(GameServer.events.onAction(player, this))
			return;

		player.sendPacket(new ActionFailed());
	}

	@SuppressWarnings("static-access")
	public void onActionShift(L2PcInstance player)
	{
		if(GameServer.events.onActionShift(player, this))
			return;
		
		player.sendPacket(new ActionFailed());
	}
	
	public void onForcedAttack(L2PcInstance player)
	{
		player.sendPacket(new ActionFailed());
	}

	/**  
	 * Method overridden in L2Character class
	 */ 
    @SuppressWarnings("unused")
	public boolean addKnownObject(L2Object object, L2Character dropper)
	{ 
		return object != null && object != this;
	}

	/**  
	 * Methods overridden in L2Character class
	 */ 
    @SuppressWarnings("unused")
	public void removeKnownObject(L2Object object) {}

    @SuppressWarnings("unused")
	public void addKnownPlayer(L2PcInstance player) {}

	public boolean knowsThePlayer(L2PcInstance player)
	{
		return false;
	}

	/**
	 * Return the distance under which the object must be add to _knownObject in function of the object type.<BR><BR>
	 *
	 * <B><U> Overriden in </U> :</B><BR><BR>
	 * <li> L2NPCInstance</li>
	 * <li> L2PcInstance</li>
	 * <li> L2Attackable</li>
	 * <li> L2DoorInstance</li>
	 * <li> L2Summon</li><BR><BR>
	 *
	 */

	public int getDistanceToWatchObject(L2Object object) { return 0; }

	/**  
	 * Method overridden in L2Character class
	 */ 
	public void removeAllKnownObjects() { }
	public void clearAllKnownObjects() { }

	/**
	 * returns reference to region this object is in
	 * @return
	 */
	public L2WorldRegion getCurrentWorldRegion() 
    {
		return _worldRegion;
	}

	public boolean knowsObjectCheck(Integer oid)
	{
		return false;
	}

    public int getCurrentZoneIndex(int[][] zoneList)
    {
        for (int i=0; i<zoneList.length; i++)
        {
            if (withinZone(zoneList[i]))
                return i;
        }
        
        return -1;
    }
    
    public boolean withinZone(int[] coordList)
    {
		return (getX() >= Math.min(coordList[0],coordList[2])
                && getX() <= Math.max(coordList[0],coordList[2])
                && getY() >= Math.min(coordList[1],coordList[3])
                && getY() <= Math.max(coordList[1],coordList[3]));
    }
    	
    public boolean withinZones(int[][] zoneList)
    {
    	return (getCurrentZoneIndex(zoneList) >= 0);
    }
    
    public boolean isAttackable()
    {
        return false;
    }
    
    public abstract boolean isAutoAttackable(L2Character attacker);

	public void setIntention(CtrlIntention intention, Object arg0, Object arg1) { };
	public CtrlIntention getIntention()	{ return CtrlIntention.AI_INTENTION_IDLE; }

	public boolean isMarker() 
	{
		return false;
	}
    
    public void refreshID()
    {
        int newObjectId = IdFactory.getInstance().getNextId();
        int oldObjectId = getObjectId();
		L2World.removeObject(this);
        _objectId       = newObjectId;
        IdFactory.getInstance().releaseId(oldObjectId);
    }

	public String getL2ClassShortName()
	{
		return getClass().getName().replaceAll("^.*\\.(.*?)$", "$1");
	}

	public final double getDistance(int x, int y)
	{
		return Math.hypot(x - getX(), y - getY());
	}

	public final boolean isInRange(L2Object obj, int range)
	{
		if(obj == null)
			return false;

		long dx = obj.getX() - getX();

		if(dx > range || dx < -range)
			return false;

		long dy = obj.getY() - getY();

		if(dy > range || dy < -range)
			return false;

		long dz = obj.getZ() - getZ();

		if(dz > 1500 || dz < -1500)
			return false;

		return dx*dx + dy*dy <= range*range;
	}

	public final double getDistance(L2Object obj)
	{
		if(obj == null)
			return 0;
		return Math.hypot(obj.getX() - getX(), obj.getY() - getY());
	}

	public final long getSqDistance(L2Object obj)
	{
		if(obj == null)
			return 0;

		long dx = obj.getX() - getX();
		long dy = obj.getY() - getY();

		return dx*dx + dy*dy;
	}

	public boolean inWorld()
	{
		if(Rnd.get(100)==0)
		{
			L2Object obj = L2World.findObject(_objectId);
			if(obj == null)
			{
				_log.warning("object "+this+" not found in world!");
				return false;
			}
		}
		return true;
	}

	public boolean hasAI() {return false;}

    @SuppressWarnings("unused")	public L2CharacterAI setAI(L2CharacterAI new_ai) {return null;}
}
