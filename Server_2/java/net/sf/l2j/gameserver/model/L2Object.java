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
import net.sf.l2j.gameserver.ClientThread;
import net.sf.l2j.gameserver.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.serverpackets.GetItem;

/**
 * This class ...
 * 
 * @version $Revision: 1.15.2.8.2.6 $ $Date: 2005/03/31 16:07:50 $
 */
public abstract class L2Object
{
	private static final Logger _log = Logger.getLogger(L2Object.class.getName());
	
	private final int _objectId;
	// this is for items/chars that have are seen in the world
	private int _x;
	private int _y;
	private int _z;
	private L2WorldRegion _worldRegion;
	private boolean _hidden;

	public L2Object(int objectId)
	{
		_objectId = objectId;
	}
	
	public final int hashCode() {
		return _objectId;
	}
	
	/**
	 * @return
	 */
	public final int getObjectId()
	{
		return _objectId;
	}

	/**
	 * @return Returns the x.
	 */
	public final int getX()
	{
		if (Config.ASSERT) assert _worldRegion != null || _hidden;
		return _x;
	}


	/**
	 * @return Returns the y.
	 */
	public final int getY()
	{
		if (Config.ASSERT) assert _worldRegion != null || _hidden;
		return _y;
	}

	/**
	 * @return Returns the z.
	 */
	public final int getZ()
	{
		if (Config.ASSERT) assert _worldRegion != null || _hidden;
		return _z;
	}

	public final void setXYZ(int x, int y, int z)
	{
		if (Config.ASSERT) assert _worldRegion != null;
		_x = x;
		_y = y;
		_z = z;
		if (L2World.getInstance().getRegion(_x, _y) != _worldRegion)
			updateCurrentWorldRegion();
	}
	
	public final void setXYZInvisible(int x, int y, int z)
	{
		if (Config.ASSERT) assert _worldRegion == null;
		_x = x;
		_y = y;
		_z = z;
		_hidden = true;
	}
	
    public final boolean isVisible() {
		return _worldRegion != null;
    }

	public final void dropMe(L2Character dropper, int x, int y, int z) {
		if (Config.ASSERT) assert this instanceof L2ItemInstance;
		if (Config.ASSERT) assert _worldRegion == null;
		synchronized (this) {
			_hidden = false;
			_x = x;
			_y = y;
			_z = z;
			_worldRegion = L2World.getInstance().getRegion(_x, _y);
			_worldRegion.addVisibleObject(this);
		}
		// this can synchronize on others instancies, so it's out of
		// synchronized, to avoid deadlocks
		L2World.getInstance().addVisibleObject(this, _worldRegion, dropper);
	}
	public final void spawnMe(int x, int y, int z) {
		if (Config.ASSERT) assert _worldRegion == null;
		synchronized (this) {
			_hidden = false;
			_x = x;
			_y = y;
			_z = z;
			L2World.getInstance().storeObject(this);
			_worldRegion = L2World.getInstance().getRegion(_x, _y);
			_worldRegion.addVisibleObject(this);
		}
		// this can synchronize on others instancies, so it's out of
		// synchronized, to avoid deadlocks
		L2World.getInstance().addVisibleObject(this, _worldRegion, null);
	}
	public final void pickupMe(L2Character player) {
		if (Config.ASSERT) assert this instanceof L2ItemInstance;
		if (Config.ASSERT) assert _worldRegion != null;
		L2WorldRegion reg = _worldRegion;
		GetItem gi = new GetItem((L2ItemInstance)this, player.getObjectId());
		player.broadcastPacket(gi);
		synchronized (this) {
			_hidden = true;
			_worldRegion = null;
		}
		// this can synchronize on others instancies, so it's out of
		// synchronized, to avoid deadlocks
		L2World.getInstance().removeVisibleObject(this, reg);
	}
	public final void decayMe() {
		if (Config.ASSERT) assert _worldRegion != null;
		L2WorldRegion reg = _worldRegion;
		synchronized (this) {
			_hidden = true;
			_worldRegion = null;
		}
		// this can synchronize on others instancies, so it's out of
		// synchronized, to avoid deadlocks
		L2World.getInstance().removeVisibleObject(this, reg);
	}

	/**
	 * @param activeChar
	 */
	public void onAction(L2PcInstance player)
	{
		player.sendPacket(new ActionFailed());
	}

	/**
	 * @param player
	 */
	public void onActionShift(ClientThread client)
	{
		client.getActiveChar().sendPacket(new ActionFailed());
	}
	
	public void onForcedAttack(L2PcInstance player)
	{
		player.sendPacket(new ActionFailed());
	}

	/** @return true is the object was really added */ 
	public boolean addKnownObject(L2Object object, L2Character dropper)
	{
		return false;
	}

	public void removeKnownObject(L2Object object)
	{
	}

	public boolean knowsObject(L2Object object)
	{
		return false;
	}
	
	public void removeAllKnownObjects()
	{
	}

	/**
	 * returns reference to region this object is in
	 * @return
	 */
	public L2WorldRegion getCurrentWorldRegion() 
    {
		return _worldRegion;
	}

	/**
	 * checks if current object changed its region, if so, update referencies
	 *
	 */
	private void updateCurrentWorldRegion() 
    {
		if (!isVisible())
			return;
		L2WorldRegion newRegion = L2World.getInstance().getRegion(_x, _y);
		if (newRegion != _worldRegion) {
				//_log.config("removing me cause region change. old:" +_worldRegion+" new: "+newRegion);
				_worldRegion.removeVisibleObject(this);
			_worldRegion = newRegion;
			_worldRegion.addVisibleObject(this);
		}
	}

    public boolean withinZones(int[][] zoneList)
    {//L2World.zones
    	int x = getX();
        int y = getY();
    	for (int i=0; i<zoneList.length;i++)
    	{
    		int x1 = zoneList[i][0];
    		int y1 = zoneList[i][1];
    		int x2 = zoneList[i][2];
    		int y2 = zoneList[i][3];
    		
    		if (x >= Math.min(x1,x2)
    				&& x <= Math.max(x1,x2)
    				&& y >= Math.min(y1,y2)
    				&& y <= Math.max(y1,y2))
    		{
    			//_log.config("Within a zone");
    			return true;
    		}
    	}
    	
    	return false;
    }
    
    public abstract boolean isAutoAttackable(L2Character attacker);
}
