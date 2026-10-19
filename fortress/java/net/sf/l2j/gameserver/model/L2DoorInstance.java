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

import java.util.concurrent.ScheduledFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.ClientScheduler;
import net.sf.l2j.gameserver.ClientThread;
import net.sf.l2j.gameserver.GameServer;
import net.sf.l2j.gameserver.ai.CtrlIntention;
import net.sf.l2j.gameserver.ai.L2CharacterAI;
import net.sf.l2j.gameserver.ai.L2DoorAI;
import net.sf.l2j.gameserver.instancemanager.CastleManager;
import net.sf.l2j.gameserver.model.entity.Castle;
import net.sf.l2j.gameserver.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.serverpackets.DoorStatusUpdate;
import net.sf.l2j.gameserver.serverpackets.MyTargetSelected;
import net.sf.l2j.gameserver.serverpackets.ValidateLocation;
import net.sf.l2j.gameserver.templates.L2CharTemplate;
import net.sf.l2j.gameserver.templates.L2Weapon;

/**
 * This class ...
 * 
 * @version $Revision: 1.3.2.2.2.5 $ $Date: 2005/03/27 15:29:32 $
 */
public class L2DoorInstance extends L2Character
{
    protected static Logger _log = Logger.getLogger(L2DoorInstance.class.getName());

    /** The castle index in the array of L2Castle this L2NpcInstance belongs to */
    private int _CastleIndex = -2;

    protected final int _doorId;
    protected final String _name;
	private boolean _open;
	private boolean _unlockable;
	
    protected int _autoActionDelay = -1;
    private ScheduledFuture _autoActionTask;
    
    
	/** This class may be created only by L2Character and only for AI */
	public class AIAccessor extends L2Character.AIAccessor
	{
		protected AIAccessor() {}
		public L2DoorInstance getActor() { return L2DoorInstance.this; }
        @SuppressWarnings("unused")
		public void moveTo(int x, int y, int z, int offset) {}
        @SuppressWarnings("unused")
		public void moveTo(int x, int y, int z) {}
        @SuppressWarnings("unused")
		public void stopMove(L2CharPosition pos) {}
        @SuppressWarnings("unused")
		public void doAttack(L2Character target) {}
        @SuppressWarnings("unused")
		public void doCast(L2Skill skill) {}
	}
	
    public L2CharacterAI getAI() {
        if (_ai == null)
        {
            synchronized(this)
            {
                if (_ai == null)
                    _ai = new L2DoorAI(new AIAccessor());
            }
        }
        return _ai;
    }
	
    class CloseTask implements Runnable
    {
        public void run()
        {
        	try
        	{
        		onClose();
			} 
			catch (Throwable e) 
			{
				_log.log(Level.SEVERE, "", e);
			}
        }
    }	

	/**
     * Manages the auto open and closing of a door. 
     */
    class AutoOpenClose implements Runnable
    {
        public void run()
        {
            try {
                String doorAction;
                
                if (!isOpen()) {
                    doorAction = "opened";
                    openMe();
                }
                else {
                    doorAction = "closed";
                    closeMe();
                }
                
                if (Config.DEBUG)
                    _log.info("Auto " + doorAction + " door ID " + _doorId + " (" + _name + ") for " + (_autoActionDelay / 1000 / 60) + " minute(s).");
            }
            catch (Exception e) {
                _log.warning("Could not auto open/close door ID " + _doorId + " (" + _name + ")");
            }
        }
    }

    /**
	 */
	public L2DoorInstance(int objectId, L2CharTemplate template, int doorId, String name, boolean unlockable)
	{
		super(objectId, template);
		_doorId = doorId;
		_name = name;
        _unlockable = unlockable;
	}
	
	public final boolean isUnlockable() 
	{
		return _unlockable;
	}
	
	public final int getLevel() 
	{
		return 1;
	}
	
	/**
     * @return Returns the doorId.
     */
    public int getDoorId()
    {
        return _doorId;
    }

	/**
	 * @return Returns the open.
	 */
	public boolean isOpen()
	{
		return _open;
	}
	/**
	 * @param open The open to set.
	 */
	public void setOpen(boolean open)
	{
		_open = open;
	}
	
    /**
     * Sets the delay in milliseconds for automatic opening/closing 
     * of this door instance.
     * <BR>
     * <B>Note:</B> A value of -1 cancels the auto open/close task.
     * 
     * @param int actionDelay
     */
    public void setAutoActionDelay(int actionDelay)
    {
        if (_autoActionDelay == actionDelay)
            return;
        
        if (actionDelay > -1) {
            AutoOpenClose ao = new AutoOpenClose();
            ClientScheduler.getInstance().scheduleLowAtFixedRate(ao, actionDelay, actionDelay);
        }
        else {
            if (_autoActionTask != null)
                _autoActionTask.cancel(false);
        }
        
        _autoActionDelay = actionDelay;
    }
    
	public int getDamage() 
	{
		int dmg = 6 - (int)Math.ceil(getCurrentHp() / getMaxHp() * 6);
		if (dmg > 6)
			return 6;
		if (dmg < 0)
			return 0;
		return dmg;
	}

    public final Castle getCastle()
    {
        if (_CastleIndex < 0) _CastleIndex = CastleManager.getInstance().getCastleIndex(this);
        if (_CastleIndex < 0) return null;
        return CastleManager.getInstance().getCastles().get(_CastleIndex);
    }

	public boolean isEnemyOf(@SuppressWarnings("unused") L2Character cha) 
	{
		return true;
	}
	
	public boolean isAutoAttackable(L2Character attacker)
    {
		if (isUnlockable())
			return true;

		// Attackable during siege by attacker only
		return (attacker != null 
		        && attacker instanceof L2PcInstance 
		        && getCastle() != null
		        && getCastle().getCastleId() > 0
		        && getCastle().getSiege().getIsInProgress()
		        && getCastle().getSiege().checkIsAttacker(((L2PcInstance)attacker).getClan()));
    }

    public boolean isAttackable(L2Character attacker)
    {
        return isAutoAttackable(attacker);
    }

    
	public void updateAbnormalEffect() {}
	
	public int getDistanceToWatchObject(L2Object object) 
	{
		if (!(object instanceof L2PcInstance))
			return 0;
		return 2000;
	}

	/**
     * Return the distance after which the object must be remove from _knownObject according to the type of the object.<BR><BR>
     *   
     * <B><U> Values </U> :</B><BR><BR>
     * <li> object is a L2PcInstance : 4000</li>
     * <li> object is not a L2PcInstance : 0 </li><BR><BR>
     * 
     */
    public int getDistanceToForgetObject(L2Object object) 
     {
        if (!(object instanceof L2PcInstance))
            return 0;
        
        return 4000;
    }

    /**
	 * Return null.<BR><BR>
	 */ 
	public L2ItemInstance getActiveWeaponInstance() 
	{
		return null;
	}
	
	public L2Weapon getActiveWeaponItem() 
	{
		return null;
	}

	public L2ItemInstance getSecondaryWeaponInstance() 
	{
		return null;
	}

	public L2Weapon getSecondaryWeaponItem() 
	{
		return null;
	}

	@SuppressWarnings("static-access")
	public void onAction(L2PcInstance player) 
	{
        if (player == null)
            return;

		if(GameServer.events.onAction(player, this))
			return;

		if (this != player.getTarget())
		{
			player.setTarget(this);
			player.sendPacket(new MyTargetSelected(getObjectId(), player.getLevel()));
			
			if (isAutoAttackable(player))
				player.sendPacket(new DoorStatusUpdate(this));
			
			// correct location
			player.sendPacket(new ValidateLocation(this));
		}
		else
		{
			player.sendPacket(new MyTargetSelected(getObjectId(), player.getLevel()));
			if(isAutoAttackable(player))
			{
				if (Math.abs(player.getZ() - getZ()) < 400) // this max heigth difference might need some tweaking
					player.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, this, null);
				else
					player.sendPacket(new ActionFailed());
			}
			if(!isAutoAttackable(player))
			{
				/*double distance = getDistance(player.getX(), player.getY());
				if (distance > L2NpcInstance.INTERACTION_DISTANCE) {
					player.getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, this);
				} else {*/
					player.sendPacket(new ActionFailed());					
					//player.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE, this);
				//}
			}
		}
	}

	@SuppressWarnings("static-access")
	public void onActionShift(ClientThread client) 
	{
		L2PcInstance player = client.getActiveChar();
		
		if(GameServer.events.onActionShift(player, this))
			return;

		player.sendPacket(new ActionFailed());
	}

	public void broadcastStatusUpdate()
	{
		if(!isKnownPlayers())
			return;

		DoorStatusUpdate su = new DoorStatusUpdate(this);
		for(L2PcInstance player : getAllPlayers(6144))
			player.sendPacket(su);
	}

	public void onOpen()
	{
		ClientScheduler.getInstance().scheduleLow(new CloseTask(), 60000);
	}

	public void onClose()
	{
		this.closeMe();
	}
	
	public final void closeMe() 
	{
        _open = false;
		broadcastStatusUpdate();
	}	

	public final void openMe()
	{
		_open = true;;
	    broadcastStatusUpdate();
	} 

	public String toString()
	{
		return "door "+_doorId;
	}

    public String getDoorName()
    {
        return _name;
    }
}
