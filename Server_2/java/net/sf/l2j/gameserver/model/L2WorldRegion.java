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
import java.util.Iterator;

import net.sf.l2j.Config;
import net.sf.l2j.util.L2ObjectHashSet;

/**
 * This class ...
 * 
 * @version $Revision: 1.3.4.4 $ $Date: 2005/03/27 15:29:33 $
 */
public final class L2WorldRegion
{
	private L2ObjectHashSet<L2PcInstance> _allPlayers;
	private L2ObjectHashSet<L2Object> _visibleObjects;
	
	private ArrayList<L2WorldRegion> _surroundingRegions;
	
	public L2WorldRegion()
	{
		_allPlayers = new L2ObjectHashSet<L2PcInstance>();
		_visibleObjects = new L2ObjectHashSet<L2Object>();
		_surroundingRegions = new ArrayList<L2WorldRegion>();
		//_surroundingRegions.add(this); //done in L2World.initRegions()
	}

	public void addVisibleObject(L2Object object)
	{
		if (Config.ASSERT) assert object.getCurrentWorldRegion() == this;
		_visibleObjects.put(object);

		if (object instanceof L2PcInstance) {
			_allPlayers.put((L2PcInstance)object);
		}
	}
	
	public void removeVisibleObject(L2Object object)
	{
		if (Config.ASSERT) assert object.getCurrentWorldRegion() == this || object.getCurrentWorldRegion() == null;
		_visibleObjects.remove(object);
		
		if (object instanceof L2PcInstance) {
			_allPlayers.remove((L2PcInstance)object);
		}
	}
	
	public void addSurroundingRegion(L2WorldRegion region) {
		_surroundingRegions.add(region);
	}
	
	//change to return L2WorldRegion[] ?
	//this should not change after initialization, so maybe changes are not necessary 
	public ArrayList<L2WorldRegion> getSurroundingRegions() {
		return _surroundingRegions;
	}

	public Iterator<L2PcInstance> iterateAllPlayers()
	{
		return _allPlayers.iterator();
	}
	
	public Iterator<L2Object> iterateVisibleObjects() {
		return _visibleObjects.iterator();
	}
}
