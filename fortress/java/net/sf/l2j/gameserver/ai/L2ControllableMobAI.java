package net.sf.l2j.gameserver.ai;

import static net.sf.l2j.gameserver.ai.CtrlIntention.AI_INTENTION_ACTIVE;
import static net.sf.l2j.gameserver.ai.CtrlIntention.AI_INTENTION_ATTACK;

import java.util.Vector;
import java.util.logging.Logger;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.lib.Rnd;
import net.sf.l2j.gameserver.model.*;
import net.sf.l2j.gameserver.model.L2Character.AIAccessor;

/**
 * @author littlecrow
 * AI for controllable mobs
 *
 */
public class L2ControllableMobAI extends L2AttackableAI 
{
	private static Logger _log = Logger.getLogger(L2ControllableMobAI.class.getName());

	public static int AI_IDLE = 1;

	public static int AI_NORMAL = 2;

	public static int AI_FORCEATTACK = 3;

	public static int AI_FOLLOW = 4;

	public static int AI_CAST = 5;
	
	public static int AI_ATTACK_GROUP = 6;
	public int FOLLOW_RANGE = 600;

	private int _alternateAI;

	private boolean thinking; // to prevent thinking recursivly

	private L2Character forcedTarget;

	private boolean no_move;
	
	private MobGroup _groupTarget;

	protected void thinkFollow() 
	{
		L2Attackable me = (L2Attackable) _actor;

		if(Config.DEBUG)
			_log.info("Think follow for "+forcedTarget+ " range="+FOLLOW_RANGE+" dist="+me.getDistance(forcedTarget));

		if(me.getDistance(forcedTarget) > FOLLOW_RANGE) 
		{
			L2CharPosition pos = Rnd.coordsRandomize(forcedTarget, 50, FOLLOW_RANGE);

			moveTo(pos.x, pos.y, pos.z);
		}
	}

	@Override
	protected void onEvtThink() 
	{
//		if(Config.DEBUG)
//			_log.info("Think "+_alternateAI);

		checkIdle();

		if(thinking)
			return;
		
		thinking = true;
		
		try 
		{
			if(_alternateAI == AI_IDLE) 
			{
				if(getIntention() != CtrlIntention.AI_INTENTION_ACTIVE)
					setIntention(CtrlIntention.AI_INTENTION_ACTIVE, null, null);
			} else if(_alternateAI == AI_FOLLOW)
				thinkFollow();
			else if(_alternateAI == AI_CAST)
				thinkCast();
			else if(_alternateAI == AI_FORCEATTACK)
				thinkForceAttack();
			else if(_alternateAI == AI_ATTACK_GROUP) 
				thinkAttackGroup();
			else 
			{
				if(getIntention() == AI_INTENTION_ACTIVE)
					thinkActive();
				else if(getIntention() == AI_INTENTION_ATTACK)
					thinkAttack();

			}

		} 
		catch(Exception e) { e.printStackTrace(); }
		finally 
		{ 
			thinking = false; 
		}
	}

	protected void thinkCast() {
		L2Attackable npc = (L2Attackable) _actor;
		if(_attack_target==null || _attack_target.isAlikeDead()) {
			_attack_target = findNextRndTarget();
			clientStopMoving(null);
		}
			
		if(_attack_target==null) {
			return;
		}
		npc.setTarget(_attack_target);
		

		L2Skill[] skills = null;
		double dist = 0;

		try {
			skills = _actor.getAllSkills();
			dist = _actor.getDistance(_attack_target.getX(), _attack_target.getY());
		} catch (NullPointerException e) {
			_log.warning("Encountered Null Value.");
			e.printStackTrace();
		}

		if(!_actor.isMuted()) 
		{
			int max_range = 0;
			// check distant skills
			for(L2Skill sk : skills) 
			{
				int range = sk.getCastRange();
				if(range >= dist
						&& !_actor.isSkillDisabled(sk.getId())
						&& _actor.getCurrentMp() > sk.getMpConsume()) {
					_accessor.doCast(sk);
					return;
				}
				if(range > max_range && Rnd.get(5)==0)
					max_range = range;
			}
			if(!no_move)
				moveToPawn(_attack_target, max_range-20);
			return;
		}

	}
	
	protected void thinkAttackGroup() 
	{
		if(forcedTarget==null || forcedTarget.isAlikeDead()) 
		{
			// try to get next group target
			forcedTarget = findNextGroupTarget();
			clientStopMoving(null);
		}
		
		if(forcedTarget==null)
			return;
		
		L2Skill[] skills = null;
		double dist = 0;
		int range = 0;
		int max_range = 0;
		
		_actor.setTarget(forcedTarget);
		// as a response, we put the target in a forcedattack mode
		L2ControllableMobInstance theTarget = (L2ControllableMobInstance) forcedTarget;
		L2ControllableMobAI ctrlAi = (L2ControllableMobAI)theTarget.getAI();
		if(ctrlAi == null)
		{	
			_log.warning("Null AI in L2ControllableMobAI:170");
			return;
		}

		ctrlAi.forceAttack(_actor);
		
		try {
			skills = _actor.getAllSkills();
			dist = _actor.getDistance(forcedTarget);
			range = _actor.getPhysicalAttackRange();
			max_range = range;
		} catch (NullPointerException e) {
			_log.warning("Encountered Null Value.");
			e.printStackTrace();
		}

		if(!_actor.isMuted() && dist > range - 20) {
			// check distant skills
			for(L2Skill sk : skills) 
			{
				range = sk.getCastRange();
				if(range >= dist
						&& !_actor.isSkillDisabled(sk.getId())
						&& _actor.getCurrentMp() > sk.getMpConsume()) {
					_accessor.doCast(sk);
					return;
				}
				if(range > max_range && Rnd.get(5)==0)
					max_range = range;
			}
			if(!no_move)
				moveToPawn(forcedTarget, max_range-20);
			return;
		}

		_accessor.doAttack(forcedTarget);
	}

	protected void thinkForceAttack() 
	{
		if(Config.DEBUG)
			_log.info("thinkForceAttack "+forcedTarget);

		if(forcedTarget == null || forcedTarget.isAlikeDead()) 
		{
			clientStopMoving(null);
			setIntention(AI_INTENTION_ACTIVE, null, null);
			setAlternateAI(AI_IDLE);
		}

		L2Skill[] skills = null;
		double dist = 0;
		int range = 0;
		int max_range = 0;

		try 
		{
			_actor.setTarget(forcedTarget);
			skills = _actor.getAllSkills();
			dist = _actor.getDistance(forcedTarget);
			range = _actor.getPhysicalAttackRange();
			max_range = range;
		} 
		catch(Exception e) 
		{
			e.printStackTrace();
		}

		if(Config.DEBUG)
			_log.info("dist="+dist+"; skills="+skills+"; range="+range+"; muted="+_actor.isMuted());

		if(!_actor.isMuted() && (dist > range - 20))
		{
			// check distant skills
			for(L2Skill sk : skills) 
			{
				range = sk.getCastRange();
				if(range >= dist
						&& !_actor.isSkillDisabled(sk.getId())
						&& _actor.getCurrentMp() > sk.getMpConsume()) 
				{
					_accessor.doCast(sk);
					return;
				}
				if(range > max_range && Rnd.get(5)==0)
					max_range = range;
			}

			if(!no_move)
				moveToPawn(forcedTarget, max_range-20);

			return;
		}

		_accessor.doAttack(forcedTarget);
	}

	protected void thinkAttack() 
	{
		if(_attack_target == null || _attack_target.isAlikeDead()) 
		{
			if(_attack_target != null) 
			{
				// stop hating
				L2Attackable npc = (L2Attackable) _actor;
				int hate = npc.getHating(_attack_target);
				if(hate > 0)
					npc.addDamageHate(_attack_target, 0, -hate);
			}
			_attack_target = null;
			// clientStopAutoAttack();
			clientStopMoving(null);
			setIntention(AI_INTENTION_ACTIVE, null, null);
		} 
		else 
		{
			// notify aggression
			if(((L2NpcInstance) _actor).getFactionId() != null) 
			{
				String faction_id = ((L2NpcInstance) _actor).getFactionId();
				for(L2NpcInstance npc : _actor.getKnownNpcs(1200)) 
				{
					if(faction_id != npc.getFactionId())
						continue;
					if(_actor.getDistance(npc) < npc.getFactionRange()
							&& Math.abs(_attack_target.getZ() - npc.getZ()) < 200) 
						npc.notifyEvent(CtrlEvent.EVT_AGGRESSION, _attack_target, 1);
				}
			}

			L2Skill[] skills = null;
			double dist = 0;
			int range = 0;
			int max_range = 0;

			try 
			{
				_actor.setTarget(_attack_target);
				skills = _actor.getAllSkills();
				dist = _actor.getDistance(_attack_target);
				range = _actor.getPhysicalAttackRange();
				max_range = range;
			} catch (NullPointerException e) {
				_log.warning("Encountered Null Value.");
				e.printStackTrace();
			}

			if(!_actor.isMuted() && dist > range - 20) 
			{
				// check distant skills
				for(L2Skill sk : skills) 
				{
					range = sk.getCastRange();
					if(range >= dist
							&& !_actor.isSkillDisabled(sk.getId())
							&& _actor.getCurrentMp() > sk.getMpConsume()) 
					{
						_accessor.doCast(sk);
						return;
					}
					if(range > max_range && Rnd.get(5)==0)
						max_range = range;
				}
				moveToPawn(_attack_target, max_range-20);
				return;
			}
			// Force mobs to attak anybody if confused
			L2Character hated;
			if(_actor.isConfused())
				hated = _attack_target;
			else
				hated = findNextRndTarget();
			
			if(hated == null) 
			{
				setIntention(AI_INTENTION_ACTIVE, null, null);
				return;
			}

			if(hated != _attack_target)
				_attack_target = hated;
			if(!_actor.isMuted() && skills.length > 0 && Rnd.get(5) == 3) 
			{
				for(L2Skill sk : skills) 
				{
					if(sk.getCastRange() >= dist
							&& !_actor.isSkillDisabled(sk.getId())
							&& _actor.getCurrentMp() < sk.getMpConsume()) 
					{
						_accessor.doCast(sk);
						return;
					}
				}
			}
			_accessor.doAttack(_attack_target);
		}
	}

	private void thinkActive() 
	{
		_attack_target = findNextRndTarget();
		
		// Force mobs to attak anybody if confused
		L2Character hated;
		if(_actor.isConfused())
			hated = _attack_target;
		else
			hated = _attack_target;
		if(hated != null) 
		{
			_actor.setRunning();
			setIntention(CtrlIntention.AI_INTENTION_ATTACK, hated, null);
		}
		return;
	}

	private boolean autoAttackCondition(L2Character target) 
	{
		L2Attackable me = (L2Attackable) _actor;
		
		if(target instanceof L2FolkInstance	|| target instanceof L2DoorInstance)
			return false;

		if(target.isAlikeDead()
				|| me.getDistance(target) > me.getAggroRange()
				|| Math.abs(_actor.getZ() - target.getZ()) > 100)
			return false;

		if(target instanceof L2PcInstance &&
			(((L2PcInstance)target).isInvul() ||
			 ((L2PcInstance)target).isSilentMoving()))
			return false;

		if(me.getFactionId() != null && target instanceof L2NpcInstance && me.getFactionId() == ((L2NpcInstance)target).getFactionId())
			return false;

		if(target instanceof L2NpcInstance && me.getTemplate() == ((L2NpcInstance)target).getTemplate())
			return false;

//		if(target instanceof L2NpcInstance)
//			return false;
		
		return me.isAggressive();
	}

	private L2Character findNextRndTarget() 
	{
		int aggroRange  = ((L2Attackable)_actor).getAggroRange();
		
		L2Attackable npc = (L2Attackable)_actor;
		
		Vector<L2Character> potentialTarget = new Vector<L2Character>();

		for(L2Character target : npc.getKnownCharacters(aggroRange)) 
			if(autoAttackCondition(target)) // check aggression
				potentialTarget.add(target);

		if(potentialTarget.size() == 0) // nothing to do
			return null;

		L2Character target = potentialTarget.get(Rnd.get(potentialTarget.size()));

		return target;
	}

	private L2ControllableMobInstance findNextGroupTarget() {
		return _groupTarget.getAMob();
		
		
	}
	public L2ControllableMobAI(AIAccessor accessor) {
		super(accessor);
		_alternateAI = AI_IDLE;

	}

	public int getAlternateAI() {
		return _alternateAI;
	}

	public void setAlternateAI(int _alternateai) {
		_alternateAI = _alternateai;
	}

	public void forceAttack(L2Character target) 
	{
		setAlternateAI(AI_FORCEATTACK);
		forcedTarget = target;
	}
	
	public void forceAttackGroup(MobGroup group) {
		forcedTarget=null;
		_groupTarget = group;
		setAlternateAI(AI_ATTACK_GROUP);
	}

	public void stop() {
		setAlternateAI(AI_IDLE);
		clientStopMoving(null);
	}

	public void move(int x, int y, int z) {
		moveTo(x, y, z);
	}

	public void follow(L2Character target, int range) 
	{
		FOLLOW_RANGE = range;
		setAlternateAI(AI_FOLLOW);
		forcedTarget = target;
	}

	public boolean isNoMove() {
		return no_move;
	}

	public void setNoMove(boolean no_move) {
		this.no_move = no_move;
	}


}