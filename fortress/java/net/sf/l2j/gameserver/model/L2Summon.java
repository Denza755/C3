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

import java.util.concurrent.Future;
import java.util.logging.Logger;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.ClientScheduler;
import net.sf.l2j.gameserver.GameServer;
import net.sf.l2j.gameserver.ai.CtrlIntention;
import net.sf.l2j.gameserver.ai.L2CharacterAI;
import net.sf.l2j.gameserver.ai.L2SummonAI;
import net.sf.l2j.gameserver.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.serverpackets.MyTargetSelected;
import net.sf.l2j.gameserver.serverpackets.NpcInfo;
import net.sf.l2j.gameserver.serverpackets.PetDelete;
import net.sf.l2j.gameserver.serverpackets.PetStatusShow;
import net.sf.l2j.gameserver.serverpackets.PetStatusUpdate;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;
import net.sf.l2j.gameserver.templates.L2Weapon;

public abstract class L2Summon extends L2PlayableInstance
{
    private static Logger _log = Logger.getLogger(L2Summon.class.getName());
    
    protected int _exp = 0;
	protected int _sp = 0;
	protected int _pkKills;
	//private int _pvpKills;
    private byte _pvpFlag;
    protected final L2PcInstance _owner;
    private int _karma = 0;
    private int _attackRange = 36; //Melee range
    private boolean _follow = true;
	protected Future _decayTask;
    //private L2Character _lastTarget;
    private int _maxLoad;
    
    private boolean _showSummonAnimation;
    
	public class AIAccessor extends L2Character.AIAccessor
	{
		protected AIAccessor() {}
		public L2Summon getSummon() { return L2Summon.this; }
		public boolean isAutoFollow() {
			return L2Summon.this.getFollowStatus();
		}
		public void doPickupItem(L2Object object) {
			L2Summon.this.doPickupItem(object);
		}
	}

	public L2Summon(int objectId, L2NpcTemplate template, L2PcInstance owner)
	{
		super(objectId, template);
        _showSummonAnimation = true;
		_owner = owner;
		_ai = new L2SummonAI(new L2Summon.AIAccessor());
		setXYZInvisible(owner.getX()+50, owner.getY()+100, owner.getZ()+100);
	}
	
	public L2CharacterAI getAI() 
	{
		return setAI(new L2SummonAI(new L2Summon.AIAccessor()));
	}
	
	public L2NpcTemplate getTemplate()
	{
		return (L2NpcTemplate)super.getTemplate();
	}

	// this defines the action buttons, 1 for Summon, 2 for Pets
    public abstract int getSummonType();

	public void updateAbnormalEffect()
    {
		if(!isKnownPlayers())
			return;

		for(L2PcInstance player : getAllPlayers(6144))
			player.sendPacket(new NpcInfo(this, player));
    }
    
    /**
     * @return Returns the mountable.
     */
    public boolean isMountable()
    {
        return false;
    }

	@SuppressWarnings("static-access")
	public void onAction(L2PcInstance player)
    {
		if(GameServer.events.onAction(player, this))
			return;

        if (player == _owner && player.getTarget() == this)
        {
            player.sendPacket(new PetStatusShow(this));
            player.sendPacket(new ActionFailed());
        }
        else
        {
            if (Config.DEBUG) _log.fine("new target selected:"+getObjectId());
            player.setTarget(this);
            MyTargetSelected my = new MyTargetSelected(getObjectId(), player.getLevel() - getLevel());
            player.sendPacket(my);
        }
    }
    
	public int getExpForThisLevel()
    {
        if(getLevel() >= Experience.LEVEL.length)
        {
            return 0;
        }
		return Experience.LEVEL[getLevel()];
    }
	
    public int getExpForNextLevel()
    {
        if(getLevel()+1 >= Experience.LEVEL.length)
        {
            return 0;
        }
        return Experience.LEVEL[getLevel()+1];
    }
    
	public final int getKarma()
    {
        return _karma;
    }
    public void setKarma(int karma)
    {
        _karma = karma;
    }
    public final L2PcInstance getOwner()
    {
        return _owner;
    }
    public final int getNpcId()
    {
        return getTemplate().npcId;
    }
    public void setPvpFlag(byte pvpFlag)
    {
        _pvpFlag = pvpFlag;
    }
    public byte getPvpFlag()
    {
        return _pvpFlag;
    }
    public void setPkKills(int pkKills)
    {
        _pkKills = pkKills;
    }
    public final int getPkKills()
    {
        return _pkKills;
    }
    public final int getExp()
    {
        return _exp;
    }
    public final void setExp(int exp)
    {
        _exp = exp;
    }
    public final int getSp()
    {
        return _sp;
    }
    
    public void setSp(int sp)
    {
        _sp = sp;
    }
    
    public final int getMaxLoad()
    {
        return _maxLoad;
    }
    
    public void setMaxLoad(int maxLoad)
    {
        _maxLoad = maxLoad;
    }
    
	public abstract int getCurrentFed();

	public abstract int getMaxFed();

    public void followOwner()
    {
		setFollowStatus(true);
    }
	
	public synchronized void doDie(L2Character killer) 
	{
		if (_decayTask == null)
			_decayTask = ClientScheduler.getInstance().scheduleLow(new DecayTask(), 3000);

		super.doDie(killer);
	}
	
    public void stopDecay()
    {
        if (_decayTask != null) { _decayTask.cancel(true); }
    }
    
    public void onDecay()
    {
        deleteMe(_owner);
    }    
    
    public void broadcastStatusUpdate()
    {
        super.broadcastStatusUpdate();
        if (_owner != null && isVisible())
        {
        	_owner.sendPacket(new PetStatusUpdate(this));
        }
    }
    public void deleteMe(L2PcInstance owner)
    {   
        if(getAI()!=null)
	        getAI().stopFollow();
        owner.sendPacket(new PetDelete(getObjectId(), 2));
        //FIXME: I think it should really drop items to ground and only owner can take for a while
        giveAllToOwner(); 
        decayMe();
        removeAllKnownObjects();
        owner.setPet(null); 
    }

    public synchronized void unSummon (L2PcInstance owner)
    {
		if (isVisible() && !isDead())
	    {
		    if(getAI()!=null)
				getAI().stopFollow();
	        owner.sendPacket(new PetDelete(getObjectId(), 2));
	        store();
	        giveAllToOwner();
		    decayMe();
	        removeAllKnownObjects();
	        owner.setPet(null);
	        setTarget(null);
	    }
    }
    
	public int getDistanceToWatchObject(@SuppressWarnings("unused") L2Object object)
    {
        return 1500;
    }

    public int getAttackRange()
    {
        return _attackRange; 
    }
    
    public void setAttackRange(int range)
    {
        if (range < 36)
            range = 36;
        _attackRange = range;
    }
    
    public void setFollowStatus(boolean state)
    {
        _follow = state;
		if (_follow)
			setIntention(CtrlIntention.AI_INTENTION_FOLLOW, _owner, null);
		else
			setIntention(CtrlIntention.AI_INTENTION_IDLE, null, null);
    }
    
    public boolean getFollowStatus()
    {
        return _follow;
    }
    
    /* (non-Javadoc)
     * @see net.sf.l2j.gameserver.model.L2Character#isAttackable()
     */
    public boolean isAutoAttackable(L2Character attacker)
    {
        return _owner.isAutoAttackable(attacker);
    }
    /**
     * @return Returns the showSummonAnimation.
     */
    public boolean isShowSummonAnimation()
    {
        return _showSummonAnimation;
    }
    /**
     * @param showSummonAnimation The showSummonAnimation to set.
     */
    public void setShowSummonAnimation(boolean showSummonAnimation)
    {
        _showSummonAnimation = showSummonAnimation;
    }
    
    public int getControlItemId()
    {
        return 0;
    }
    public L2Weapon getActiveWeapon()
    {
        return null;
    }
    public PetInventory getInventory()
    {
        return null;
    }
	protected void doPickupItem(@SuppressWarnings("unused") L2Object object)
    {
        return;
    }
    public void giveAllToOwner()
    {
        return;
    }
    public void store()
    {
        return;
    }
    /*
    protected void displayHitMessage(int damage, boolean crit, boolean miss)
    {
        return;
    }*/
	
	/**
	 * Return null.<BR><BR>
	 */
	public L2ItemInstance getActiveWeaponInstance() 
	{
		return null;
	}
	
	public L2Weapon getActiveWeaponItem() {
		return null;
	}
	public L2ItemInstance getSecondaryWeaponInstance() {
		return null;
	}
	public L2Weapon getSecondaryWeaponItem() {
		return null;
	}
	
	
	/**
	 * Return the L2Party object of its L2PcInstance owner or null.<BR><BR>
	 */
	public L2Party getParty()
	{
		return _owner.getParty();
	}
	public boolean isInParty()
	{
		return _owner.getParty() != null;
	}
	
	public void useMagic(L2Skill skill)
	{
		super.useMagic(skill);
	}
}
