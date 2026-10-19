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

import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.logging.Level;

import net.sf.l2j.gameserver.ClientScheduler;
import net.sf.l2j.gameserver.ai.CtrlEvent;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;
import net.sf.l2j.gameserver.util.MinionList;

/**
 * This class manages all Monsters.
 * 
 * L2MonsterInstance :<BR><BR>
 * <li>L2MinionInstance</li>
 * <li>L2RaidBossInstance </li>
 * 
 * @version $Revision: 1.20.4.6 $ $Date: 2005/04/06 16:13:39 $
 */
public class L2MonsterInstance extends L2Attackable
{
	//private static Logger _log = Logger.getLogger(L2MonsterInstance.class.getName());
    
    protected final MinionList minionList;
    
    @SuppressWarnings("unused")
    private ScheduledFuture minionMaintainTask;
    
    private static final int MONSTER_MAINTENANCE_INTERVAL = 1000;
    
	
	/**
	 * Constructor of L2MonsterInstance (use L2Character and L2NpcInstance constructor).<BR><BR>
	 *  
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Call the L2Character constructor to set the _template of the L2MonsterInstance (copy skills from template to object and link _calculators to NPC_STD_CALCULATOR) </li>
	 * <li>Set the name of the L2MonsterInstance</li>
	 * <li>Create a RandomAnimation Task that will be launched after the calculated delay if the server allow it </li><BR><BR>
	 * 
	 * @param objectId Identifier of the object to initialized
	 * @param L2NpcTemplate Template to apply to the NPC
	 */
	public L2MonsterInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
        minionList  = new MinionList(this);
	}	
		
	/**
	 * Return True if the attacker is not another L2MonsterInstance.<BR><BR>
	 */
	public boolean isAutoAttackable(L2Character attacker) 
	{
		if(attacker instanceof L2MonsterInstance)
			return false;
		
		return true;
	}
	
	/**
	 * Return True if the L2MonsterInstance is Agressive (aggroRange > 0).<BR><BR>
	 */
	public boolean isAggressive()
	{
		return getTemplate().aggroRange > 0;
	}

	/**
	 * Return False.<BR><BR>
	 */
	public boolean hasRandomAnimation()
	{
		return false;
	}

	/**
	 * Remove a L2Object from _knownObjects, _knownPlayer, _aggroList of the L2MonsterInstance then cancel Attak or Cast if it was targeted.<BR><BR>
	 * 
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Remove object from _knownObjects of the L2Attackable </li>
	 * <li>If object is a L2PcInstance, remove object from _knownPlayer of the L2Attackable </li>
	 * <li>If object is targeted by the L2Attackable, cancel Attack or Cast </li>
	 * <li>If object is a L2Character, remove it from the _aggrolist of the L2Attackable </li>
	 * <li>Set the L2Attackable Intention to AI_INTENTION_IDLE </li>
	 * <li>Notify the L2Attackable AI with EVT_FORGET_OBJECT </li>
	 * <li>If its _knownPlayer is empty, clean its _knownObjects, _knownPlayer, _aggroList </li><BR><BR>
	 * 
	 * @param object The L2Object to remove from _knownObjects, _knownPlayer and _aggroList
	 * 
	 */
	public void removeKnownObject(L2Object object)
	{
		// Remove a L2Object from _knownObjects, _knownPlayer, _aggroList of the L2Attackable then cancel Attak or Cast if it was targeted
		super.removeKnownObject(object);
		
		// Check if the object is a L2Character
		if(!(object instanceof L2Character))
			return;
		
		// Notify the L2MonsterInstance AI with EVT_FORGET_OBJECT
		if(hasAI()) 
			notifyEvent(CtrlEvent.EVT_FORGET_OBJECT, object, null);
	
		if(isVisible() && !isKnownPlayers())
		{
			// Clear the _aggroList of the L2MonsterInstance
			clearAggroList();
			
			// Remove all L2Object from _knownObjects and _knownPlayer of the L2MonsterInstance then cancel Attak or Cast and notify AI
			removeAllKnownObjects();
			
			return;
		}
	}
    
	public void OnSpawn()
	{
        super.OnSpawn();
		try
		{
			minionMaintainTask = ClientScheduler.scheduleEvent(new Runnable() 
			{
                public void run()
                {
                    try
                    {
                        minionList.maintainMinions();
                    }
                    catch (Throwable e) { _log.log(Level.SEVERE, "", e); e.printStackTrace(); }
                }
            }, getMaintenanceInterval());
		}
		catch(NullPointerException e) { }		
	}
    
    protected int getMaintenanceInterval() { return MONSTER_MAINTENANCE_INTERVAL; }
    
    public void callMinions()
    {
        if (minionList.hasMinions())
        {
            for (L2MinionInstance minion : minionList.getSpawnedMinions())
            {
                // Get actual coords of the minion and check to see if it's too far away from this L2MonsterInstance
                if (getDistance(minion.getX(), minion.getY()) > 100)
                {
                    // Calculate a new random coord for the minion based on themaster's coord
                    L2CharPosition pos = minionList.getMinionPosition();
                    // Move the minion to the new coords
                    if (!minion.isInCombat() && !minion.isDead() && !minion.isMovementDisabled())
                    {
                        minion.moveToLocation(pos.x, pos.y, pos.z, 0);
                    }
                }
            }
        }
    }
    
    public void callMinionsToAssist(L2Character attacker)
    {
        if (minionList.hasMinions())
        {
            for (L2MinionInstance minion : minionList.getSpawnedMinions())
            {
                // Trigger the aggro condition of the minion
                if (!minion.isInCombat() && !minion.isDead())
                {
                    minion.addDamage(attacker, 1);
                }
            }
        }
    }
    
    public List<L2MinionInstance> getSpawnedMinions()
    {
        return minionList.getSpawnedMinions();
    }
    
    public int getTotalSpawnedMinionsInstances()
    {
        return minionList.countSpawnedMinions();
    }
    
    public int getTotalSpawnedMinionsGroups()
    {
        return minionList.lazyCountSpawnedMinionsGroups();
    }
    
    public void notifyMinionDied(L2MinionInstance minion)
    {
        minionList.removeSpawnedMinion(minion);
    }
    
    public void notifyMinionSpawned(L2MinionInstance minion)
    {
        minionList.addSpawnedMinion(minion);
    }
    
    public boolean hasMinions()
    {
        return minionList.hasMinions();
    }
    
    public void addDamageHate(L2Character attacker, int damage, int aggro)
    {
        if (!(attacker instanceof L2MonsterInstance))
        {
            super.addDamageHate(attacker, damage, aggro);
        }
    }
}
