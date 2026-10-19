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
import net.sf.l2j.gameserver.Territory;
import net.sf.l2j.gameserver.lib.Rnd;
import net.sf.l2j.gameserver.model.L2Attackable;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2CharPosition;
import net.sf.l2j.gameserver.model.L2DoorInstance;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.model.L2FestivalMonsterInstance;
import net.sf.l2j.gameserver.model.L2FolkInstance;
import net.sf.l2j.gameserver.model.L2FriendlyMobInstance;
import net.sf.l2j.gameserver.model.L2GuardInstance;
import net.sf.l2j.gameserver.model.L2MonsterInstance;
import net.sf.l2j.gameserver.model.L2NpcInstance;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2PcInstance;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2Summon;


/**
 * This class manages AI of L2Attackable.<BR><BR>
 * 
 */
public class L2AttackableAI extends L2CharacterAI implements Runnable 
{

	protected static final Logger _log = Logger.getLogger(L2AttackableAI.class.getName());
	
	private static final int RANDOM_WALK_RATE = 100;
	// private static final int MAX_DRIFT_RANGE = 300;
	private static final int MAX_ATTACK_TIMEOUT = 120000/GameTimeController.MILLIS_IN_TICK; // int millis->ticks
	
	/** The L2Attackable AI task executed every 1s (call onEvtThink method)*/
	private Future aiTask;

	/** The delay after wich the attacked is stopped */
	private int _attack_timeout;
	
	/** The L2Attackable aggro counter */
	private int _globalAggro;
	
	/** The flag used to indicate that a thinking action is in progress */
	private boolean thinking; // to prevent recursive thinking
	
	private int aggroRange;
	
	/**
	 * Constructor of L2AttackableAI.<BR><BR>
	 * 
	 * @param accessor The AI accessor of the L2Character
	 * 
	 */
	public L2AttackableAI(L2Character.AIAccessor accessor)
	{
		super(accessor);
		
		_attack_timeout = Integer.MAX_VALUE;
		_globalAggro = -10; // 10 seconds timeout of ATTACK after respawn
		
		aggroRange  = ((L2Attackable)_actor).getAggroRange();
	}
	
	
	public void run() 
	{
		// Launch actions corresponding to the Event Think
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

	
	/**
	 * Return True if the target is autoattackable (depends on the actor type).<BR><BR>
	 * 
	 * <B><U> Actor is a L2GuardInstance</U> :</B><BR><BR>
	 * <li>The target isn't a Folk or a Door</li>
	 * <li>The target isn't dead, isn't invulnerable, isn't in silent moving mode AND too far (>100)</li>
	 * <li>The target is in the actor Aggro range and is at the same height</li>
	 * <li>The L2PcInstance target has karma (=PK)</li>
	 * <li>The L2MonsterInstance target is aggressive</li><BR><BR>
	 * 
	 * <B><U> Actor is a L2SiegeGuardInstance</U> :</B><BR><BR>
	 * <li>The target isn't a Folk or a Door</li>
	 * <li>The target isn't dead, isn't invulnerable, isn't in silent moving mode AND too far (>100)</li>
	 * <li>The target is in the actor Aggro range and is at the same height</li>
	 * <li>A siege is in progress</li>
	 * <li>The L2PcInstance target isn't a Defender</li><BR><BR>
	 * 
	 * <B><U> Actor is a L2FriendlyMobInstance</U> :</B><BR><BR>
	 * <li>The target isn't a Folk, a Door or another L2NpcInstance</li>
	 * <li>The target isn't dead, isn't invulnerable, isn't in silent moving mode AND too far (>100)</li>
	 * <li>The target is in the actor Aggro range and is at the same height</li>
	 * <li>The L2PcInstance target has karma (=PK)</li><BR><BR>
	 * 
	 * <B><U> Actor is a L2MonsterInstance</U> :</B><BR><BR>
	 * <li>The target isn't a Folk, a Door or another L2NpcInstance</li>
	 * <li>The target isn't dead, isn't invulnerable, isn't in silent moving mode AND too far (>100)</li>
	 * <li>The target is in the actor Aggro range and is at the same height</li>
	 * <li>The actor is Aggressive</li><BR><BR>
	 * 
	 * @param target The targeted L2Object
	 * 
	 */
	private boolean autoAttackCondition(L2Character target)
	{
		L2Attackable me = (L2Attackable)_actor;
	
		// Check if the target isn't a Folk or a Door
		if (target instanceof L2FolkInstance || target instanceof L2DoorInstance)
			return false;
		
		// Check if the target isn't dead, is in the Aggro range and is at the same height
		if (target.isAlikeDead() 
				|| me.getDistance(target) > me.getAggroRange() 
				|| Math.abs(_actor.getZ()-target.getZ()) > 300)
			return false;
		
		// Check if the L2PcInstance target isn't invulnerable and isn't in silent moving mode
		if (target instanceof L2PcInstance &&
			(((L2PcInstance)target).isInvul() ||
			 ((L2PcInstance)target).isSilentMoving()))
			return false;
		
		// Check if the actor is a L2GuardInstance
		if (_actor instanceof L2GuardInstance)
		{
			// Check if the L2PcInstance target has karma (=PK)
			if (target instanceof L2PcInstance)
				return ((L2PcInstance)target).getKarma() > 0;
				
			if (target instanceof L2Summon)
				return ((L2Summon)target).getOwner().getKarma() > 0;
			
			// Check if the L2MonsterInstance target is aggressive
			if (target instanceof L2MonsterInstance)
				return ((L2MonsterInstance)target).isAggressive();
			
			return false;
		}
		else if (_actor instanceof L2FriendlyMobInstance)
		{
			// Check if the actor is a L2FriendlyMobInstance
			
			// Check if the target isn't another L2NpcInstance
			if (target instanceof L2NpcInstance)
				return false;
			
			// Check if the L2PcInstance target has karma (=PK)
			if (target instanceof L2PcInstance)
				return ((L2PcInstance)target).getKarma() > 0;
			
			return false;
		}
		else 
		{   //The actor is a L2MonsterInstance
			
			// Check if the target isn't another L2NpcInstance
			if (target instanceof L2NpcInstance)
				return false;

			// Check if the actor is Aggressive
			return me.isAggressive();
		}
		
	}
	
	/**
	 * Set the Intention of this L2CharacterAI and create an  AI Task executed every 1s (call onEvtThink method) for this L2Attackable.<BR><BR>
	 * 
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : If actor _knowPlayer isn't EMPTY, AI_INTENTION_IDLE will be change in AI_INTENTION_ACTIVE</B></FONT><BR><BR>
	 * 
	 * @param intention The new Intention to set to the AI
	 * @param arg0 The first parameter of the Intention
	 * @param arg1 The second parameter of the Intention
	 * 
	 */
	void changeIntention(CtrlIntention intention, Object arg0, Object arg1)
	{
		if (Config.DEBUG)
			_log.info("L2AttackableAI: "+_actor+".changeIntention("+intention+", "+arg0+", "+arg1+")");

		if (intention == AI_INTENTION_IDLE || intention == AI_INTENTION_ACTIVE)
		{
			// Check if actor is not dead
			if (!_actor.isAlikeDead())
			{
				L2Attackable npc = (L2Attackable)_actor;
				
				// If its _knownPlayer isn't empty set the Intention to AI_INTENTION_ACTIVE
				if(npc.isKnownPlayers())
					intention = AI_INTENTION_ACTIVE;
				else
					intention = AI_INTENTION_IDLE;
			}
			
			if (intention == AI_INTENTION_IDLE)
			{
				// Set the Intention of this L2AttackableAI to AI_INTENTION_IDLE
				super.changeIntention(AI_INTENTION_IDLE, null, null);
				
				// Stop AI task and detach AI from NPC
				if (aiTask != null)
				{
					aiTask.cancel(true);
					aiTask = null;
				}

				// Cancel the AI
				_accessor.detachAI();
				
				return;
			}
		}
		
		// Set the Intention of this L2AttackableAI to intention
		super.changeIntention(intention, arg0, arg1);
		
		// If not idle - create an AI task (schedule onEvtThink repeatedly)
		if (aiTask == null)
			aiTask = ClientScheduler.getInstance().scheduleLowAtFixedRate(this, 1000, 1000);
	}
	
	
	/**
	 * Manage the Attack Intention : Stop current Attack (if necessary), Calculate attack timeout, Start a new Attack and Launch Think Event.<BR><BR>
	 *
	 * @param target The L2Character to attack
	 *
	 */
	protected void onIntentionAttack(L2Character target)
	{
		// Calculate the attack timeout
		_attack_timeout = MAX_ATTACK_TIMEOUT + GameTimeController.getGameTicks();
		
		// Manage the Attack Intention : Stop current Attack (if necessary), Start a new Attack and Launch Think Event
		super.onIntentionAttack(target);
	}

	
	/**
	 * Manage AI standard thinks of a L2Attackable (called by onEvtThink).<BR><BR>
	 * 
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Update every 1s the _globalAggro counter to come close to 0</li>
	 * <li>If the actor is Aggressive and can attack, add all autoAttackable L2Character in its Aggro Range to its _aggroList, chose a target and order to attack it</li>
	 * <li>If the actor is a L2GuardInstance that can't attack, order to it to return to its home location</li>
	 * <li>If the actor is a L2MonsterInstance that can't attack, order to it to random walk (1/100)</li><BR><BR>
	 * 
	 */
	private void thinkActive() 
	{
//		if(Config.DEBUG)
//			_log.info(this+".thinkActive(); _globalAggro="+_globalAggro);

		L2Attackable npc = (L2Attackable)_actor;
		
		// Update every 1s the _globalAggro counter to come close to 0
		if (_globalAggro != 0) 
		{
			if (_globalAggro < 0)
				_globalAggro++;
			else
				_globalAggro--;
		}
		
		// Add all autoAttackable L2Character in L2Attackable Aggro Range to its _aggroList with 0 damage and 1 hate
		// A L2Attackable isn't aggressive during 10s after its spawn because _globalAggro is set to -10
		if (_globalAggro >= 0) 
		{
			// Go through visible objects
			for(L2Character target : npc.getKnownCharacters(aggroRange))
			{
				/*
				 * Check to see if this is a festival mob spawn.
				 * If it is, then check to see if the aggro trigger
				 * is a festival participant...if so, move to attack it.
				 */
				if((_actor instanceof L2FestivalMonsterInstance) && target instanceof L2PcInstance)
				{
					L2PcInstance targetPlayer = (L2PcInstance)target;
					
					if(!(targetPlayer.isFestivalParticipant()))
						continue;
				}
				
				// For each L2Character check if the target is autoattackable

				if(autoAttackCondition(target)) // check aggression
				{
					// Get the hate level of the L2Attackable against this L2Character target contained in _aggroList
					// Add the attacker to the L2Attackable _aggroList with 0 damage and 1 hate
					if(npc.getHating(target) == 0)
						npc.addDamageHate(target, 0, 1);
				}
			}
		
			// Chose a target from its aggroList
			// Force mobs to attak anybody if confused
			L2Character hated = _actor.isConfused() ? _attack_target : npc.getMostHated();
		
			// Order to the L2Attackable to attack the target
			if(hated != null) 
			{
				// Get the hate level of the L2Attackable against this L2Character target contained in _aggroList
				int aggro = npc.getHating(hated);
				
				if (aggro + _globalAggro > 0) 
				{
					// Set the L2Character movement type to run and send Server->Client packet ChangeMoveType to all others L2PcInstance
					if (!_actor.isRunning())
						_actor.setRunning();
					
					// Set the AI Intention to AI_INTENTION_ATTACK
					setIntention(CtrlIntention.AI_INTENTION_ATTACK, hated, null);
				}
				
				return;
			}
			
		}
		

		// Check if the actor is a L2GuardInstance
		if (_actor instanceof L2GuardInstance)
		{
			// Order to the L2GuardInstance to return to its home location because there's no target to attack
			((L2GuardInstance)_actor).returnHome();
		} 
		
		// If this is a festival monster, then it remains in the same location.
		if (_actor instanceof L2FestivalMonsterInstance)
			return;
		
		// The actor is a L2MonsterInstance
		
		if (npc.getSpawn()!=null && Rnd.get(RANDOM_WALK_RATE) == 0)
		{
			int x1, y1, z1;
			
			// If NPC with random coord in territory
			if (npc.getSpawn().getLocx()==0 && npc.getSpawn().getLocy()==0)
			{
				// If NPC with random fixed coord, don't move
				if (Territory.getInstance().getProcMax(npc.getSpawn().getLocation())>0)
					return;
		
				// Calculate a destination point in the spawn area
				int p[] = Territory.getInstance().getRandomPoint(npc.getSpawn().getLocation());
				x1 = p[0]; 
				y1 = p[1]; 
				z1 = p[2];
				
				// Calculate the distance between the current position of the L2Character and the target (x,y)
				double distance = _actor.getDistance(x1,y1);
		
				if (distance > Config.MAX_DRIFT_RANGE)
				{
					float delay = (float)distance/Config.MAX_DRIFT_RANGE;
					x1 = _actor.getX()+(int)((x1-_actor.getX())/delay);
					y1 = _actor.getY()+(int)((y1-_actor.getY())/delay);
				}
				
			} 
			else 
			{
				// If NPC with fixed coord
				L2CharPosition pos = Rnd.coordsRandomize(npc.getSpawn().getLocx(), npc.getSpawn().getLocy(), npc.getSpawn().getLocz(), 0, 0, Config.MAX_DRIFT_RANGE);

				x1 = pos.x;
				y1 = pos.y;
				z1 = npc.getZ();
			}			
			
			//_log.config("Curent pos ("+getX()+", "+getY()+"), moving to ("+x1+", "+y1+").");
			// Move the actor to Location (x,y,z) server side AND client side by sending Server->Client packet CharMoveToLocation (broadcast)
			moveTo(x1, y1, z1);
		}
		
		return;
	}
	
	/**
	 * Manage AI attack thinks of a L2Attackable (called by onEvtThink).<BR><BR>
	 * 
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Update the attack timeout if actor is running</li>
	 * <li>If target is dead or timeout is expired, stop this attack and set the Intention to AI_INTENTION_ACTIVE</li>
	 * <li>Call all L2Object of its Faction inside the Faction Range</li>
	 * <li>Chose a target and order to attack it with magic skill or physical attack</li><BR><BR>
	 * 
	 * TODO: Manage casting rules to healer mobs (like Ant Nurses)
	 * 
	 */
	private void thinkAttack() 
	{
		
		if (_attack_timeout < GameTimeController.getGameTicks())
		{
			// Check if the actor is running
			if (_actor.isRunning()) 
			{
				// Set the actor movement type to walk and send Server->Client packet ChangeMoveType to all others L2PcInstance
				_actor.setWalking();
				
				// Calculate a new attack timeout
				_attack_timeout = MAX_ATTACK_TIMEOUT + GameTimeController.getGameTicks();
			}
		}
		
		
		// Check if target is dead or if timeout is expired to stop this attack
		if (	_attack_target == null ||
				_attack_target.isAlikeDead() ||
				_attack_timeout < GameTimeController.getGameTicks()
			)
		{
			// Stop hating this target after the attack timeout or if target is dead
			if (_attack_target != null) 
			{
				L2Attackable npc = (L2Attackable)_actor;
				int hate = npc.getHating(_attack_target);
				if (hate > 0)
					npc.addDamageHate(_attack_target, 0, -hate);
			}
			
			// Cancel target and timeout
			_attack_timeout = Integer.MAX_VALUE;
			_attack_target = null;
			//clientStopAutoAttack();
			
			// Stop the actor movement server side AND client side by sending Server->Client packet StopMove/StopRotation (broadcast)
			clientStopMoving(null);
			
			// Set the AI Intention to AI_INTENTION_ACTIVE
			setIntention(AI_INTENTION_ACTIVE, null, null);
			
		}
		else
		{
			// Call all L2Object of its Faction inside the Faction Range
			if(((L2NpcInstance)_actor).getFactionId() != null)
			{
				String faction_id = ((L2NpcInstance)_actor).getFactionId();
				
				// Go through all L2Object that belong to its faction
				if(_attack_target != null)
				{
					for(L2NpcInstance npc : _actor.getKnownNpcs(1200))
					{
						if(faction_id != npc.getFactionId())
							continue;
				
						// Check if the L2Object is inside the Faction Range of the actor
						if(_actor.getDistance(npc) < npc.getFactionRange()
								&& Math.abs(_attack_target.getZ()-npc.getZ()) < 600 
								&& _actor.isAttacked())
							npc.notifyEvent(CtrlEvent.EVT_AGGRESSION, _attack_target, 1);
					}
				}
			}
			
			// Get all information needed to chose between physical or magical attack
			L2Skill[] skills = null;
			double dist	  = 0;
			int range		= 0;
			
			_actor.setTarget(_attack_target);
			skills	  = _actor.getAllSkills();
			dist		= _actor.getDistance(_attack_target);
			range	   = _actor.getPhysicalAttackRange();
			
			// Check if the actor isn't muted and if it is far from target
			if (!_actor.isMuted() && dist > range+20)
			{
				// check for long ranged skills and heal/buff skills
				for (L2Skill sk : skills) {
					if (( (sk.getSkillType() == L2Skill.SkillType.BUFF || sk.getSkillType() == L2Skill.SkillType.HEAL)
					|| (dist >= (sk.getCastRange()/3)) && (dist <= sk.getCastRange()) && (sk.getCastRange() > 70)) &&
					!_actor.isSkillDisabled(sk.getId()) && _actor.getCurrentMp() >= sk.getMpConsume() && !sk.isPassive() && Rnd.get(100) <= 5)
					{
						L2Object OldTarget = _actor.getTarget();
						if (sk.getSkillType() == L2Skill.SkillType.BUFF || sk.getSkillType() == L2Skill.SkillType.HEAL)
						{
							boolean useSkillSelf = true;
							if (sk.getSkillType() == L2Skill.SkillType.HEAL && _actor.getCurrentHp() > (int)(_actor.getMaxHp()/1.5)) {
								useSkillSelf = false;
								break;
							}
							if (sk.getSkillType() == L2Skill.SkillType.BUFF) {
								L2Effect[] effects = _actor.getAllEffects();
								for(int i = 0; effects != null && i < effects.length; i++)
								{
									L2Effect effect = effects[i];
									if (effect.getSkill() == sk) {
										useSkillSelf = false;
										break;
									}
								}
							}
							if (useSkillSelf)
								_actor.setTarget(_actor);
						}
						
						clientStopMoving(null);
						_accessor.doCast(sk);
						_actor.setTarget(OldTarget);
						return;
					}
				}
				

					// Move the actor to Pawn server side AND client side by sending Server->Client packet MoveToPawn (broadcast)
				moveToPawn(_attack_target, range);
				
				return;
				
			}
			// Else, if the actor is muted and far from target, just "move to pawn"
			else if (_actor.isMuted() && dist > range+20)
			{
				moveToPawn(_attack_target, range);
				return;
			}
			// Else, if this is close enough to attack
			else if (dist <= range+20)
			{
				// Force mobs to attak anybody if confused
				L2Character hated;
				if (_actor.isConfused())
					hated = _attack_target;
				else
					hated =((L2Attackable)_actor).getMostHated();
				
				if (hated == null)
				{
					setIntention(AI_INTENTION_ACTIVE, null, null);
					return;
				}
				if (hated != _attack_target)
					_attack_target = hated;
				
				_attack_timeout = MAX_ATTACK_TIMEOUT + GameTimeController.getGameTicks();
				
				// check for close combat skills && heal/buff skills
				if (!_actor.isMuted() /*&& Rnd.get(100) <= 5*/) {
					for (L2Skill sk : skills) {
						if (/*sk.getCastRange() >= dist && sk.getCastRange() <= 70 && */!sk.isPassive() &&
								_actor.getCurrentMp() >= sk.getMpConsume() && !_actor.isSkillDisabled(sk.getId()) && Rnd.get(100) <= 8)
						{
							L2Object OldTarget = _actor.getTarget();
							if (sk.getSkillType() == L2Skill.SkillType.BUFF || sk.getSkillType() == L2Skill.SkillType.HEAL)
							{
								boolean useSkillSelf = true;
								if (sk.getSkillType() == L2Skill.SkillType.HEAL && _actor.getCurrentHp() > (int)(_actor.getMaxHp()/1.5)) {
									useSkillSelf = false;
									break;
								}
								if (sk.getSkillType() == L2Skill.SkillType.BUFF) {
									L2Effect[] effects = _actor.getAllEffects();
									for(int i = 0; effects != null && i < effects.length; i++)
									{
										L2Effect effect = effects[i];
										if (effect.getSkill() == sk) {
											useSkillSelf = false;
											break;
										}
									}
								}
								if (useSkillSelf)
									_actor.setTarget(_actor);
							}
							
							clientStopMoving(null);
							_accessor.doCast(sk);
							_actor.setTarget(OldTarget);
							return;
						}
					}
				}
				
				// Finally, do the physical attack itself
				_accessor.doAttack(_attack_target);
			}
			
		}
	}
	
	
	/**
	 * Manage AI thinking actions of a L2Attackable.<BR><BR>
	 */
	protected void onEvtThink()
	{
//		if(getIntention() != AI_INTENTION_IDLE && (!_actor.isVisible() || !_actor.hasAI() || !_actor.isKnownPlayers()))
//			setIntention(AI_INTENTION_IDLE);

		// Check if the actor can't use skills and if a thinking action isn't already in progress
		if (thinking || _actor.isAllSkillsDisabled())
			return;
		
		// Start thinking action
		thinking = true;
		
		try
		{
			// Manage AI thinks of a L2Attackable
			if (getIntention() == AI_INTENTION_ACTIVE)
				thinkActive();
			else if (getIntention() == AI_INTENTION_ATTACK)
				thinkAttack();
		} 
		finally 
		{
			// Stop thinking action
			thinking = false;
		}
	}


	/**
	 * Launch actions corresponding to the Event Attacked.<BR><BR>
	 * 
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Init the attack : Calculate the attack timeout, Set the _globalAggro to 0, Add the attacker to the actor _aggroList</li>
	 * <li>Set the L2Character movement type to run and send Server->Client packet ChangeMoveType to all others L2PcInstance</li>
	 * <li>Set the Intention to AI_INTENTION_ATTACK</li><BR><BR>
	 * 
	 * @param attacker The L2Character that attacks the actor
	 * 
	 */
	protected void onEvtAttacked(L2Character attacker)
	{
		// Calculate the attack timeout
		_attack_timeout = MAX_ATTACK_TIMEOUT + GameTimeController.getGameTicks();
		
		// Set the _globalAggro to 0 to permit attack even just after spawn
		if (_globalAggro < 0)
			_globalAggro = 0;
		
		// Add the attacker to the _aggroList of the actor
		((L2Attackable)_actor).addDamageHate(attacker, 0,1);
		
		// Set the L2Character movement type to run and send Server->Client packet ChangeMoveType to all others L2PcInstance
		if (!_actor.isRunning())
			_actor.setRunning();
		
		// Set the Intention to AI_INTENTION_ATTACK
		if (getIntention() != AI_INTENTION_ATTACK)
		{
			setIntention(CtrlIntention.AI_INTENTION_ATTACK, attacker, null);
		}
		else if(((L2Attackable)_actor).getMostHated() != _attack_target)
		{
			setIntention(CtrlIntention.AI_INTENTION_ATTACK, attacker, null);
		}
	}

	/**
	 * Launch actions corresponding to the Event Aggression.<BR><BR>
	 * 
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Add the target to the actor _aggroList or update hate if already present </li>
	 * <li>Set the actor Intention to AI_INTENTION_ATTACK (if actor is L2GuardInstance check if it isn't too far from its home location)</li><BR><BR>
	 * 
	 * @param attacker The L2Character that attacks
	 * @param aggro The value of hate to add to the actor against the target
	 * 
	 */
	protected void onEvtAggression(L2Character target, int aggro)
	{
		if (target != null)
		{
			L2Attackable me = (L2Attackable)_actor; 
			
			// Add the target to the actor _aggroList or update hate if already present
			me.addDamageHate(target, 0, aggro);
			
			// Get the hate of the actor against the target
			aggro = me.getHating(target);
			
			if (aggro <= 0)
				return;
			
			
			// Set the actor AI Intention to AI_INTENTION_ATTACK
			if (getIntention() != CtrlIntention.AI_INTENTION_ATTACK)
			{
				// Set the L2Character movement type to run and send Server->Client packet ChangeMoveType to all others L2PcInstance
				if (!_actor.isRunning())
					_actor.setRunning();

				setIntention(CtrlIntention.AI_INTENTION_ATTACK, target, null);
			}
		} 
		else 
		{
			_globalAggro += aggro;
		}
	}

}
