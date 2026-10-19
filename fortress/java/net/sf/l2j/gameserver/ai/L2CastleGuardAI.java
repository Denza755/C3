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

import java.util.concurrent.Future;
import java.util.logging.Logger;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.ClientScheduler;
import net.sf.l2j.gameserver.GameTimeController;
import net.sf.l2j.gameserver.lib.Rnd;
import net.sf.l2j.gameserver.model.L2Attackable;
import net.sf.l2j.gameserver.model.L2CastleGuardInstance;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2PcInstance;
import net.sf.l2j.gameserver.model.L2Skill;


/**
 * @author mkizub
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class L2CastleGuardAI extends L2CharacterAI implements Runnable {

    protected static final Logger _log = Logger.getLogger(L2AttackableAI.class.getName());
    
    private static final int MAX_ATTACK_TIMEOUT = 300; // int ticks, i.e. 30 seconds 
	
	private Future aiTask;
	private int _attack_timeout;
	private int _globalAggro;
	
	private boolean thinking; // to prevent recursive thinking
	
	public L2CastleGuardAI(L2Character.AIAccessor accessor)
	{
		super(accessor);
		_attack_timeout = Integer.MAX_VALUE;
		_globalAggro = -10; // 10 seconds timeout of ATTACK after respawn
	}
	
	public void run() 
	{
		onEvtThink();
		checkIdle();
	}
	
	public void checkIdle()
	{
		super.checkIdle();
		if(_actor.hasAI())
			return;

		if(aiTask != null)
		{
			aiTask.cancel(true);
			aiTask = null;
		}
	}

	private boolean autoAttackCondition(L2Character target)
	{
		L2CastleGuardInstance me = (L2CastleGuardInstance)_actor;

		if (target instanceof L2PcInstance)
		{
			L2PcInstance player = (L2PcInstance) target;
			if(player.isAtWarWith(me.getClanId())>0) 
				return true;
		}
		return false;	
	}
	
	/** used by AI classes */
	void changeIntention(CtrlIntention intention, Object arg0, Object arg1)
	{
        if (Config.DEBUG)
            _log.info("L2CastleGuardAI: changeIntention -> " + intention + " " + arg0 + " " + arg1);

		if (intention == AI_INTENTION_IDLE || intention == AI_INTENTION_ACTIVE)
		{
			if (!_actor.isAlikeDead())
			{
	            L2Attackable npc = (L2Attackable)_actor;
				if (npc.isKnownPlayers())
					intention = AI_INTENTION_ACTIVE;
				else
					intention = AI_INTENTION_IDLE;
			}
			if (intention == AI_INTENTION_IDLE)
			{
				super.changeIntention(AI_INTENTION_IDLE, null, null);
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
		super.changeIntention(intention, arg0, arg1);
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

	private void thinkActive() 
	{
		for(L2Character target : _actor.getKnownCharacters(300))
		{
			if(autoAttackCondition(target)) // check aggression
			{
				if(!_actor.isRunning()) 
					_actor.setRunning();
				setIntention(CtrlIntention.AI_INTENTION_ATTACK, target, null);	
			}
		}
	}
	
	private void thinkAttack() 
	{
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
			
			_attack_timeout = Integer.MAX_VALUE;
			_attack_target = null;
			//clientStopAutoAttack();
			clientStopMoving(null);
			setIntention(AI_INTENTION_ACTIVE, null, null);
		}
		else
		{
			
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
						_accessor.doCast(sk);
						return;
					}
					max_range = Math.max(max_range, sk.getCastRange());
				}
				moveToPawn(_attack_target, range);
				return;
			}
			L2CastleGuardInstance guard = (L2CastleGuardInstance) _actor;
			if (!_actor.isMuted() && skills.length > 0) {
				for (L2Skill sk : skills) {
					if (sk.getCastRange() >= dist && !_actor.isSkillDisabled(sk.getId()) &&
							_actor.getCurrentMp() < sk.getMpConsume() && Rnd.get((skills.length*1000/guard.getMagery()-1)) == 0)
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
//		if(getIntention() != AI_INTENTION_IDLE && (!_actor.isVisible() || !_actor.hasAI() || !_actor.isKnownPlayers()))
//			setIntention(AI_INTENTION_IDLE);
		
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
			setIntention(CtrlIntention.AI_INTENTION_ATTACK, attacker, null);
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
				setIntention(CtrlIntention.AI_INTENTION_ATTACK, target, null);
			}
		} else {
			_globalAggro += aggro;
		}
	}
}
