package net.sf.l2j.gameserver.ai;

import static net.sf.l2j.gameserver.ai.CtrlIntention.AI_INTENTION_ACTIVE;
import static net.sf.l2j.gameserver.ai.CtrlIntention.AI_INTENTION_ATTACK;
import net.sf.l2j.gameserver.lib.Rnd;
import net.sf.l2j.gameserver.model.*;
import net.sf.l2j.gameserver.model.L2Character.AIAccessor;
import net.sf.l2j.gameserver.model.actor.instance.*;

import java.util.Vector;

/**
 * @author littlecrow
 * AI for controllable mobs
 *
 */
public class L2ControllableMobAI extends L2AttackableAI {

	public static int AI_IDLE = 1;

	public static int AI_NORMAL = 2;

	public static int AI_FORCEATTACK = 3;

	public static int AI_FOLLOW = 4;

	public static int AI_CAST = 5;
	
	public static int AI_ATTACK_GROUP = 6;
	public static int FOLLOW_RANGE = 300;

	private int _alternateAI;

	private boolean thinking; // to prevent thinking recursivly

	private L2Character forcedTarget;

	private boolean no_move;
	
	private MobGroup _groupTarget;

	protected void thinkFollow() {
		L2Attackable me = (L2Attackable) _actor;

		if (me.getDistance(forcedTarget.getX(), forcedTarget.getY()) > FOLLOW_RANGE) {
			int signX = (Rnd.nextInt(2) == 0 ? -1 : 1);
			int signY = (Rnd.nextInt(2) == 0 ? -1 : 1);
			int randX = Rnd.nextInt(FOLLOW_RANGE);
			int randY = Rnd.nextInt(FOLLOW_RANGE);

			moveTo(forcedTarget.getX() + signX * randX, forcedTarget.getY()
					+ signY * randY, forcedTarget.getZ());
		}
	}

	@Override
	protected void onEvtThink() {

		if (thinking || _actor.isAllSkillsDisabled())
			return;
		thinking = true;
		try {
			if (_alternateAI == AI_IDLE) {
				if (getIntention() != CtrlIntention.AI_INTENTION_ACTIVE)
					setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
			} else if (_alternateAI == AI_FOLLOW)
				thinkFollow();
			else if (_alternateAI == AI_CAST)
				thinkCast();
			else if (_alternateAI == AI_FORCEATTACK)
				thinkForceAttack();
			else if (_alternateAI == AI_ATTACK_GROUP) 
				thinkAttackGroup();
			
			else {
				if (getIntention() == AI_INTENTION_ACTIVE)
					thinkActive();
				else if (getIntention() == AI_INTENTION_ATTACK)
					thinkAttack();

			}

		} finally {
			thinking = false;
		}
	}

	protected void thinkCast() {
		L2Attackable npc = (L2Attackable) _actor;
		if (getAttackTarget() ==null || getAttackTarget().isAlikeDead()) {
            setAttackTarget(findNextRndTarget());
			clientStopMoving(null);
		}
			
		if (getAttackTarget()==null) {
			return;
		}
		npc.setTarget(getAttackTarget());
		

		L2Skill[] skills = null;
		double dist = 0;

		try {
			skills = _actor.getAllSkills();
			dist = _actor.getDistance(getAttackTarget().getX(), getAttackTarget().getY());
		} catch (NullPointerException e) {
			_log.warning("Encountered Null Value.");
			e.printStackTrace();
		}

		if (!_actor.isMuted()) {
			int max_range = 0;
			// check distant skills
			for (L2Skill sk : skills) {
				if (sk.getCastRange() >= dist
						&& !_actor.isSkillDisabled(sk.getId())
						&& _actor.getCurrentMp() > sk.getMpConsume()) {
					_accessor.doCast(sk);
					return;
				}
				max_range = Math.max(max_range, sk.getCastRange());
			}
			if (!no_move)
				moveToPawn(getAttackTarget(), max_range);
        }

	}
	
	protected void thinkAttackGroup() {
		if (forcedTarget==null || forcedTarget.isAlikeDead()) {
			
			// try to get next group target
			forcedTarget = findNextGroupTarget();
			clientStopMoving(null);
		}
		
		if (forcedTarget==null)
			return;
		
		L2Skill[] skills = null;
		double dist = 0;
		int range = 0;
		int max_range = 0;
		
		_actor.setTarget(forcedTarget);
		// as a response, we put the target in a forcedattack mode
		L2ControllableMobInstance theTarget = (L2ControllableMobInstance) forcedTarget;
		L2ControllableMobAI ctrlAi = (L2ControllableMobAI)theTarget.getAI();
		ctrlAi.forceAttack(_actor);
		
		
		try {
			
			skills = _actor.getAllSkills();
			dist = _actor.getDistance(forcedTarget.getX(), forcedTarget.getY());
			range = _actor.getPhysicalAttackRange();
			max_range = range;
		} catch (NullPointerException e) {
			_log.warning("Encountered Null Value.");
			e.printStackTrace();
		}

		if (!_actor.isMuted() && dist > range + 20) {
			// check distant skills
			for (L2Skill sk : skills) {
				if (sk.getCastRange() >= dist
						&& !_actor.isSkillDisabled(sk.getId())
						&& _actor.getCurrentMp() > sk.getMpConsume()) {
					_accessor.doCast(sk);
					return;
				}
				max_range = Math.max(max_range, sk.getCastRange());
			}
			if (!no_move)
				moveToPawn(forcedTarget, range);
			return;
		}

		_accessor.doAttack(forcedTarget);
	}

	protected void thinkForceAttack() {
		if (forcedTarget == null || forcedTarget.isAlikeDead()) {
			clientStopMoving(null);
			setIntention(AI_INTENTION_ACTIVE);
			setAlternateAI(AI_IDLE);
		}
		L2Skill[] skills = null;
		double dist = 0;
		int range = 0;
		int max_range = 0;

		try {
			_actor.setTarget(forcedTarget);
			skills = _actor.getAllSkills();
			dist = _actor.getDistance(forcedTarget.getX(), forcedTarget.getY());
			range = _actor.getPhysicalAttackRange();
			max_range = range;
		} catch (NullPointerException e) {
			_log.warning("Encountered Null Value.");
			e.printStackTrace();
		}

		if (!_actor.isMuted() && dist > range + 20) {
			// check distant skills
			for (L2Skill sk : skills) {
				if (sk.getCastRange() >= dist
						&& !_actor.isSkillDisabled(sk.getId())
						&& _actor.getCurrentMp() > sk.getMpConsume()) {
					_accessor.doCast(sk);
					return;
				}
				max_range = Math.max(max_range, sk.getCastRange());
			}
			if (!no_move)
				moveToPawn(forcedTarget, _actor.getPhysicalAttackRange()/*range*/);
			return;
		}

		_accessor.doAttack(forcedTarget);
	}

	protected void thinkAttack() {

		if (getAttackTarget() == null || getAttackTarget().isAlikeDead()) {
			if (getAttackTarget() != null) {
				// stop hating
				L2Attackable npc = (L2Attackable) _actor;
				int hate = npc.getHating(getAttackTarget());
				if (hate > 0)
					npc.addDamageHate(getAttackTarget(), 0, -hate);
			}
            setAttackTarget(null);
			// clientStopAutoAttack();
			clientStopMoving(null);
			setIntention(AI_INTENTION_ACTIVE);
		} else {
			// notify aggression
			if (((L2NpcInstance) _actor).getFactionId() != null) {
				String faction_id = ((L2NpcInstance) _actor).getFactionId();
				for (L2Object obj : _actor.getKnownList().getKnownObjects()) {
                    if (!(obj instanceof L2NpcInstance))
                        continue;

                    L2NpcInstance npc = (L2NpcInstance) obj;
                    if (faction_id != npc.getFactionId())
                        continue;
                    if (_actor.getDistance(npc.getX(), npc.getY()) < npc
                            .getFactionRange()
                            && Math.abs(getAttackTarget().getZ() - npc.getZ()) < 200) {
                        npc.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION,
                                                getAttackTarget(), 1);
                    }
				}
			}

			L2Skill[] skills = null;
			double dist = 0;
			int range = 0;
			int max_range = 0;

			try {
				_actor.setTarget(getAttackTarget());
				skills = _actor.getAllSkills();
				dist = _actor.getDistance(getAttackTarget().getX(), getAttackTarget()
						.getY());
				range = _actor.getPhysicalAttackRange();
				max_range = range;
			} catch (NullPointerException e) {
				_log.warning("Encountered Null Value.");
				e.printStackTrace();
			}

			if (!_actor.isMuted() && dist > range + 20) {
				// check distant skills
				for (L2Skill sk : skills) {
					if (sk.getCastRange() >= dist
							&& !_actor.isSkillDisabled(sk.getId())
							&& _actor.getCurrentMp() > sk.getMpConsume()) {
						_accessor.doCast(sk);
						return;
					}
					max_range = Math.max(max_range, sk.getCastRange());
				}
				moveToPawn(getAttackTarget(), range);
				return;
			}
			// Force mobs to attak anybody if confused
			L2Character hated;
			if (_actor.isConfused())
				hated = getAttackTarget();
			else
				hated = findNextRndTarget();
			
			if (hated == null) {
				setIntention(AI_INTENTION_ACTIVE);
				return;
			}

			if (hated != getAttackTarget())
                setAttackTarget(hated);
			if (!_actor.isMuted() && skills.length > 0 && Rnd.nextInt(5) == 3) {
				for (L2Skill sk : skills) {
					if (sk.getCastRange() >= dist
							&& !_actor.isSkillDisabled(sk.getId())
							&& _actor.getCurrentMp() < sk.getMpConsume()) {
						_accessor.doCast(sk);
						return;
					}
				}
			}
			_accessor.doAttack(getAttackTarget());
		}
	}

	private void thinkActive() {

        setAttackTarget(findNextRndTarget());
		
		// Force mobs to attak anybody if confused
		L2Character hated;
		if (_actor.isConfused())
			hated = getAttackTarget();
		else
			hated = getAttackTarget();
		if (hated != null) {
			_actor.setRunning();
			setIntention(CtrlIntention.AI_INTENTION_ATTACK, hated);
		}
    }

	private boolean autoAttackCondition(L2Character target) {
		L2Attackable me = (L2Attackable) _actor;
        return !(target instanceof L2FolkInstance
                || target instanceof L2DoorInstance) && !(target.isAlikeDead()
                || me.getDistance(target.getX(), target.getY()) > me
                .getAggroRange()
                || Math.abs(_actor.getZ() - target.getZ()) > 100) && !(target instanceof L2PcInstance
                && (((L2PcInstance) target).isInvul() || ((L2PcInstance) target)
                .isSilentMoving())) && !(target instanceof L2NpcInstance) && me.isAggressive();
    }

	private L2Character findNextRndTarget() {
              
        int aggroRange  = ((L2Attackable)_actor).getAggroRange();
        
        L2Attackable npc = (L2Attackable)_actor;
        
        int npcX, npcY, targetX, targetY, dy, dx;

		Vector<L2Character> potentialTarget = new Vector<L2Character>();

		for (L2Object obj : npc.getKnownList().getKnownObjects()) {
			if (!(obj instanceof L2Character))
				continue;
            
            npcX    = npc.getX();
            npcY    = npc.getY();
            targetX = obj.getX();
            targetY = obj.getY();
            
            dx      = Math.abs(npcX - targetX);
            dy      = Math.abs(npcY - targetY);
            
            if (dx*dx + dy*dy > aggroRange*aggroRange)
                continue;
            
			L2Character target = (L2Character) obj;
			if (autoAttackCondition(target)) // check aggression
			{
				potentialTarget.add(target);
			}
		}

		if (potentialTarget.size() == 0) // nothing to do
			return null;
		// we choose a random target
		int choice = Rnd.nextInt(potentialTarget.size());
		L2Character target = potentialTarget.get(choice);

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

	public void forceAttack(L2Character target) {
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

	public void follow(L2Character target) {
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