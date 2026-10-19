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

import java.lang.reflect.Constructor;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.ClientScheduler;
import net.sf.l2j.gameserver.IdFactory;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;

/**
 * This class ...
 * 
 * @author Nightmare
 * @version $Revision: 1.9.2.3.2.8 $ $Date: 2005/03/27 15:29:32 $
 */
public class L2Spawn
{
    protected static Logger _log = Logger.getLogger(L2Spawn.class.getName());
    private static final String DEFAULT_LOCATION = "unset"; 
	private L2NpcTemplate _template;
	
	private int _id; // just to find this in the spawn table
	private String _location = DEFAULT_LOCATION;
	private int _maximumCount;
	private int _currentCount;
    protected int _scheduledCount;
	private int _locx;
	private int _locy;
	private int _locz;
	private int _randomx;
	private int _randomy;
	private int _heading;
	private int _respawnDelay;
	private Constructor _constructor;
    private boolean _doRespawn;
	
	class SpawnTask implements Runnable
	{
		L2NpcInstance _instance;
		int _objId;
		
		public SpawnTask(int objid)
		{
			_objId= objid;
		}
		
		public void run()
		{		
			try
			{
				doSpawn();
			}
			catch (Exception e)
			{
				_log.log(Level.WARNING, "", e);
			}
			
			_scheduledCount--;
		}
	}

	public L2Spawn(L2NpcTemplate mobTemplate) throws SecurityException, ClassNotFoundException
	{
		 _template = mobTemplate;
		String implementationName = _template.type; // implementing class name
        if (mobTemplate.npcId == 7995)
            implementationName = "L2RaceManager";
		//if (mobTemplate.npcId == 8050)
		if ((mobTemplate.npcId >= 8046)&&(mobTemplate.npcId <= 8053))
            implementationName = "L2SymbolMaker";
		_constructor = Class.forName("net.sf.l2j.gameserver.model." + implementationName + "Instance").getConstructors()[0];
	}

	/**
	 * @return Returns the amount.
	 */
	public int getAmount()
	{
		return _maximumCount;
	}
	/**
	 * @return Returns the id.
	 */
	public int getId()
	{
		return _id;
	}
	/**
	 * @return Returns the location.
	 */
	public String getLocation()
	{
		return _location;
	}
	/**
	 * @return Returns the locx.
	 */
	public int getLocx()
	{
		return _locx;
	}
	/**
	 * @return Returns the locy.
	 */
	public int getLocy()
	{
		return _locy;
	}
	/**
	 * @return Returns the locz.
	 */
	public int getLocz()
	{
		return _locz;
	}
	/**
	 * @return Returns the npcid.
	 */
	public int getNpcid()
	{
		return _template.npcId;
	}
	/**
	 * @return Returns the randomheading.
	 */
	public int getHeading()
	{
		return _heading;
	}
    /**
     * @return Returns the respawnDelay.
     */
    public int getRespawnDelay()
    {
        return _respawnDelay;
    }
	/**
	 * @return Returns the randomx.
	 */
	public int getRandomx()
	{
		return _randomx;
	}
	/**
	 * @return Returns the randomy.
	 */
	public int getRandomy()
	{
		return _randomy;
	}
	/**
	 * @param amount
	 *            The amount to set.
	 */
	public void setAmount(int amount)
	{
		_maximumCount = amount;
	}
	/**
	 * @param id
	 *            The id to set.
	 */
	public void setId(int id)
	{
		_id = id;
	}
	/**
	 * @param location
	 *            The location to set.
	 */
	public void setLocation(String location)
	{
		_location = location;
	}
	/**
	 * @param locx
	 *            The locx to set.
	 */
	public void setLocx(int locx)
	{
		_locx = locx;
	}
	/**
	 * @param locy
	 *            The locy to set.
	 */
	public void setLocy(int locy)
	{
		_locy = locy;
	}
	/**
	 * @param locz
	 *            The locz to set.
	 */
	public void setLocz(int locz)
	{
		_locz = locz;
	}
	/**
	 * @param heading
	 *            The randomheading to set.
	 */
	public void setHeading(int heading)
	{
		_heading = heading;
	}
	/**
	 * @param randomx
	 *            The randomx to set.
	 */
	public void setRandomx(int randomx)
	{
		_randomx = randomx;
	}
	/**
	 * @param randomy
	 *            The randomy to set.
	 */
	public void setRandomy(int randomy)
	{
		_randomy = randomy;
	}
	
	public void decreaseCount(int npcId)
	{
		_currentCount--;
		// this is there just to prevent multiple respawning caused by lag
		if (_doRespawn && _scheduledCount + _currentCount < _maximumCount )
		{
			_scheduledCount++;
			ClientScheduler.getInstance().scheduleLow(
					new SpawnTask(npcId), _respawnDelay);
		}
	}
	
	/**
	 * this method create the inital spawning  
	 *
	 * @return the number of creatures that were spawned
	 */
	public int init()
	{
		while (_currentCount < _maximumCount)
		{
			doSpawn();
		}
        _doRespawn = true;
		
		return _currentCount;
	}
	
    public void stopRespawn()
    {
        _doRespawn = false;
    }
    
	public void doSpawn()
	{
		L2NpcInstance mob = null;		
	
		try
		{
            if (_template.type.equalsIgnoreCase("L2Pet"))
            {
                _currentCount++;
                return;
            }
            
			Object[] parameters = {IdFactory.getInstance().getNextId(), _template};
			Object  tmp = _constructor.newInstance(parameters);
			if (!(tmp instanceof L2NpcInstance))
				return;
			mob = (L2NpcInstance)tmp; 
			
			int newlocx, newlocy;
			if (getRandomx() > 0)
			{
				int random1 = L2Character.getRnd().nextInt(getRandomx());
				newlocx = getLocx() + L2Character.getRnd().nextInt(getRandomx()) -random1;
			} 
			else 
			{
				newlocx = getLocx();
			}						
			
			if (getRandomy() > 0)
			{
				int random2 = L2Character.getRnd().nextInt(getRandomy());
				newlocy = getLocy() + L2Character.getRnd().nextInt(getRandomy()) -random2;
			} 
			else 
			{
				newlocy = getLocy();
			}
			
			
			mob.setCurrentHpMp(mob.getMaxHp(), mob.getMaxMp());
			
			if (getHeading() == -1)
			{
				mob.setHeading(L2Character.getRnd().nextInt(61794));	
			} 
			else 
			{
				mob.setHeading(getHeading());
			}
			
			mob.setSpawn(this);
			
			mob.spawnMe(newlocx, newlocy, getLocz());
			
			if (Config.DEBUG) 
				_log.finest("spawned Mob ID: "+_template.npcId+" ,at: "
						+mob.getX()+" x, "+mob.getY()+" y, "+mob.getZ()+" z");
			_currentCount++;
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "NPC class not found", e);
		}		
	}

	/**
	 * @param i delay in seconds
	 */
	public void setRespawnDelay(int i)
	{
        if (i<0)
        {
            _log.warning("respawn delay is negative for spawnId:"+_id);
            i=10;
        }
		_respawnDelay = i * 1000;
	}
    
    
}
