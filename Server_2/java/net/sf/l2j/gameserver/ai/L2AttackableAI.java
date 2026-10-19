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
package net.sf.l2j.gameserver.ai;

import static net.sf.l2j.gameserver.ai.CtrlIntention.AI_INTENTION_ACTIVE;
import static net.sf.l2j.gameserver.ai.CtrlIntention.AI_INTENTION_ATTACK;
import static net.sf.l2j.gameserver.ai.CtrlIntention.AI_INTENTION_IDLE;

import java.util.Random;
import java.util.concurrent.Future;
import java.util.logging.Logger;

import net.sf.l2j.gameserver.ClientScheduler;
import net.sf.l2j.gameserver.GameTimeController;
import net.sf.l2j.gameserver.model.L2Attackable;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2DoorInstance;
import net.sf.l2j.gameserver.model.L2FolkInstance;
import net.sf.l2j.gameserver.model.L2FriendlyMobInstance;
import net.sf.l2j.gameserver.model.L2GuardInstance;
import net.sf.l2j.gameserver.model.L2MonsterInstance;
import net.sf.l2j.gameserver.model.L2NpcInstance;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2PcInstance;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2World;

/**
 * @author mkizub
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class L2AttackableAI extends L2CharacterAI implements Runnable {

    protected static final Logger _log = Logger.getLogger(L2AttackableAI.class.getName());
    
    private static final int RANDOM_WALK_RATE = 100;
    private static final int MAX_DRIFT_RANGE = 300;
    private static final int MAX_ATTACK_TIMEOUT = 300; // int ticks, i.e. 30 seconds 
	
	private final static Random _rnd = new Random();

	private Future aiTask;
	private int _attack_timeout;
	private int _globalAggro;
	
	private boolean thinking; // to prevent recursive thinking
	
	public L2AttackableAI(L2Character.AIAccessor accessor)
	{
		super(accessor);
		_attack_timeout = Integer.MAX_VALUE;
		_globalAggro = -10; // 10 seconds timeout of attack after respawn
	}
	
	public void run() {
		onEvtThink();
	}
	
	private boolean autoAttackCondition(L2Character target)
	{
		L2Attackable me = (L2Attackable)_actor;
		if (target instanceof L2FolkInstance || target instanceof L2DoorInstance)
			return false;
		if (target.isAlikeDead() ||
				me.getDistance(target.getX(), target.getY()) > me.getAggroRange() ||
			 Math.abs(_actor.getZ()-target.getZ()) > 100
			 )
			return false;
		if (target instanceof L2PcInstance && ((L2PcInstance)target).isInvul())
			return false;
		if (_actor instanceof L2GuardInstance)
		{
			if (target instanceof L2PcInstance)
				return ((L2PcInstance)target).getKarma() > 0;
			//if (target instanceof L2Summon)
			//	return ((L2Summon)target).getKarma() > 0;
			if (target instanceof L2MonsterInstance)
				return ((L2MonsterInstance)target).isAggressive();
			return false;
		}
		else if (_actor instanceof L2FriendlyMobInstance)
		{
			if (target instanceof L2NpcInstance)
				return false;
			if (target instanceof L2PcInstance)
				return ((L2PcInstance)target).getKarma() > 0;
			return false;
		}
		else { //L2MonsterInstance
			if (target instanceof L2NpcInstance)
				return false;
			return me.isAggressive();
		}
	}
	
	/** used by AI classes */
	synchronized void changeIntention(CtrlIntention intention)
	{
		if (intention == AI_INTENTION_IDLE || intention == AI_INTENTION_ACTIVE)
		{
			if (!_actor.isAlikeDead())
			{
	            L2Attackable npc = (L2Attackable)_actor;
				if (npc.getKnownPlayers() != null && !npc.getKnownPlayers().isEmpty())
					intention = AI_INTENTION_ACTIVE;
			}
			if (intention == AI_INTENTION_IDLE)
			{
				super.changeIntention(AI_INTENTION_IDLE);
				// stop AI task and detach AI from NPC
				if (aiTask != null)
				{
					aiTask.cancel(true);
					aiTask = null;
				}
				_accessor.detachAI();
				return;
			}
		}
		super.changeIntention(intention);
		// if not idle - create AI task (schedule onEvtThink repeatedly)
		if (aiTask == null)
		{
			aiTask = ClientScheduler.getInstance().scheduleLowAtFixedRate(this, 1000, 1000);
		}
	}
	
	protected void onIntentionAttack(L2Character target)
	{
		_attack_timeout = MAX_ATTACK_TIMEOUT + GameTimeController.getGameTicks();
		super.onIntentionAttack(target);
	}

	private void thinkActive() {
        L2Attackable npc = (L2Attackable)_actor;
		if (_globalAggro != 0) {
			if (_globalAggro < 0)
				_globalAggro++;
			else
				_globalAggro--;
		}
		// skan for target
		if (_globalAggro >= 0) {
			L2Object[] objects = L2World.getInstance().getVisibleObjects(_actor, ((L2NpcInstance)_actor).getAggroRange());
			for (L2Object obj : objects)
			{
				if (!(obj instanceof L2Character))
					continue;
				L2Character target = (L2Character)obj;
				if (autoAttackCondition(target)) // check aggression
				{
					int hating = npc.getHating(target);
					if (hating == 0)
						npc.addDamageHate(target, 0, 1);
				}
			}
			// Force mobs to attak anybody if confused
			L2Character hated;
			if (_actor.isConfused())
				hated = _attack_target;
			else
				hated = npc.getMostHated();
			if (hated != null) {
				int aggro = npc.getHating(hated);
				if (aggro + _globalAggro > 0) {
					if (!_actor.isRunning())
						_actor.setRunning();
					setIntention(CtrlIntention.AI_INTENTION_ATTACK, hated);
				}
				return;
			}
		}
        if (_actor instanceof L2GuardInstance)
        {
			// return to home
			((L2GuardInstance)_actor).returnHome();
        } else {
			// random walk
	        Random r = L2Character.getRnd();
	        if (r.nextInt(RANDOM_WALK_RATE) == 0)
	        {
	            int x1 = npc.getSpawn().getLocx() + r.nextInt(MAX_DRIFT_RANGE * 2) - MAX_DRIFT_RANGE;
				int y1 = npc.getSpawn().getLocy() + r.nextInt(MAX_DRIFT_RANGE * 2) - MAX_DRIFT_RANGE;
	            //_log.config("Curent pos ("+getX()+", "+getY()+"), moving to ("+x1+", "+y1+").");
				moveTo(x1, y1, npc.getZ());
	        }
		}
		return;
	}
	
	private void thinkAttack() {
		if (_attack_timeout < GameTimeController.getGameTicks())
		{
			if (_actor.isRunning()) {
				_actor.setWalking();
				_attack_timeout = MAX_ATTACK_TIMEOUT + GameTimeController.getGameTicks();
			}
		}
		if (	_attack_target == null ||
				_attack_target.isAlikeDead() ||
				_attack_timeout < GameTimeController.getGameTicks()
			)
		{
			if (_attack_target != null) {
				// stop hating
				L2Attackable npc = (L2Attackable)_actor;
				int hate = npc.getHating(_attack_target);
				if (hate > 0)
					npc.addDamageHate(_attack_target, 0, -hate);
			}
			_attack_timeout = Integer.MAX_VALUE;
			_attack_target = null;
			//clientStopAutoAttack();
			clientStopMoving(null);
			setIntention(AI_INTENTION_ACTIVE);
		}
		else
		{
			// notify aggression
			if (((L2NpcInstance)_actor).getFactionId() != null)
			{
				String faction_id = ((L2NpcInstance)_actor).getFactionId();
				for (L2Object obj : _actor.getKnownObjects()) {
					if (obj instanceof L2NpcInstance) {
						L2NpcInstance npc = (L2NpcInstance)obj;
						if (faction_id != npc.getFactionId())
							continue;
						if (_actor.getDistance(npc.getX(), npc.getY()) < npc.getFactionRange()
								&& Math.abs(_attack_target.getZ()-npc.getZ()) < 200)
						{
							npc.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, _attack_target, 1);
						}
					}
				}
			}
            
            L2Skill[] skills = null;
            double dist      = 0;
            int range        = 0;
            int max_range    = 0;
            
            try
            {
                _actor.setTarget(_attack_target);
                skills      = _actor.getAllSkills();
                dist        = _actor.getDistance(_attack_target.getX(), _attack_target.getY());
                range       = _actor.getPhysicalAttackRange();
                max_range   = range;
            }
            catch (NullPointerException e)
            {
                _log.warning("Encountered Null Value.");
                e.printStackTrace();
            }
            
			if (!_actor.isMuted() && dist > range+20)
			{
				// check distant skills
				for (L2Skill sk : skills) {
					if (sk.getCastRange() >= dist && !_actor.isSkillDisabled(sk.getId()) &&
						_actor.getCurrentMp() > sk.getMpConsume())
					{
						clientStopMoving(null);
						_accessor.doCast(sk);
						return;
					}
					max_range = Math.max(max_range, sk.getCastRange());
				}
				moveToPawn(_attack_target, range);
				return;
			}
			// Force mobs to attak anybody if confused
			L2Character hated;
			if (_actor.isConfused())
				hated = _attack_target;
			else
				hated =((L2Attackable)_actor).getMostHated();
			if (hated == null)
			{
				setIntention(AI_INTENTION_ACTIVE);
				return;
			}
			if (hated != _attack_target)
				_attack_target = hated;
			_attack_timeout = MAX_ATTACK_TIMEOUT + GameTimeController.getGameTicks();
			if (!_actor.isMuted() && skills.length > 0 && _rnd.nextInt(5) == 3) {
				for (L2Skill sk : skills) {
					if (sk.getCastRange() >= dist && !_actor.isSkillDisabled(sk.getId()) &&
							_actor.getCurrentMp() < sk.getMpConsume())
					{
						_accessor.doCast(sk);
						return;
					}
				}
			}
			_accessor.doAttack(_attack_target);
		}
	}
	
	protected void onEvtThink()
	{
		if (thinking || _actor.isAllSkillsDisabled())
			return;
		thinking = true;
		try
		{
			if (getIntention() == AI_INTENTION_ACTIVE)
				thinkActive();
			else if (getIntention() == AI_INTENTION_ATTACK)
				thinkAttack();
		} finally {
			thinking = false;
		}
	}


	protected void onEvtAttacked(L2Character attacker)
	{
		_attack_timeout = MAX_ATTACK_TIMEOUT + GameTimeController.getGameTicks();
		if (_globalAggro < 0)
			_globalAggro = 0;
		((L2Attackable)_actor).addDamageHate(attacker, 0,1);
		if (!_actor.isRunning())
			_actor.setRunning();
		if (getIntention() != AI_INTENTION_ATTACK)
		{
			setIntention(CtrlIntention.AI_INTENTION_ATTACK, attacker);
		}
	}

	protected void onEvtAggression(L2Character target, int aggro)
	{
		if (target != null)
		{
			L2Attackable me = (L2Attackable)_actor; 
			me.addDamageHate(target, 0, aggro);
			aggro = me.getHating(target);
			if (aggro <= 0)
				return;
			if (getIntention() != CtrlIntention.AI_INTENTION_ATTACK)
			{
				if (!_actor.isRunning())
					_actor.setRunning();
				setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);
			}
		} else {
			_globalAggro += aggro;
		}
	}

}
