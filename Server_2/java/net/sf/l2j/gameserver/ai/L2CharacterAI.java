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
import static net.sf.l2j.gameserver.ai.CtrlIntention.AI_INTENTION_CAST;
import static net.sf.l2j.gameserver.ai.CtrlIntention.AI_INTENTION_FOLLOW;
import static net.sf.l2j.gameserver.ai.CtrlIntention.AI_INTENTION_IDLE;
import static net.sf.l2j.gameserver.ai.CtrlIntention.AI_INTENTION_INTERACT;
import static net.sf.l2j.gameserver.ai.CtrlIntention.AI_INTENTION_MOVE_TO;
import static net.sf.l2j.gameserver.ai.CtrlIntention.AI_INTENTION_PICK_UP;
import static net.sf.l2j.gameserver.ai.CtrlIntention.AI_INTENTION_REST;
import net.sf.l2j.gameserver.model.L2CharPosition;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2Skill;

/**
 * @author mkizub
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class L2CharacterAI extends AbstractAI {

	public L2CharacterAI(L2Character.AIAccessor accessor)
	{
		super(accessor);
	}
	
	//
	// Default intention's implementation
	//
	
	protected void onIntentionIdle()
	{
		changeIntention(AI_INTENTION_IDLE);
		_cast_target   = null;
		_attack_target = null;
		_follow_target = null;
		clientStopAutoAttack();
		clientStopMoving(null);
		clientStandUp();
	}
	
	protected void onIntentionActive()
	{
		if (getIntention() != AI_INTENTION_ACTIVE)
		{
			changeIntention(AI_INTENTION_ACTIVE);
			_cast_target   = null;
			_attack_target = null;
			_follow_target = null;
			clientStopAutoAttack();
			clientStopMoving(null);
			onEvtThink();
		}
	}
	
	protected void onIntentionRest()
	{
		// mobs cannot rest, and L2CharacterAI should overwrite
		setIntention(AI_INTENTION_IDLE);
	}
	
	protected void onIntentionAttack(L2Character target)
	{
		if (getIntention() == AI_INTENTION_REST) {
			clientActionFailed();
			return;
		}
		if (_actor.isAllSkillsDisabled() || _actor.isAffraid()) {
			clientActionFailed();
			return;
		}
		if (getIntention() == AI_INTENTION_ATTACK)
		{
			// already attacking, switch target
			if (_attack_target != target)
			{
				clientStopAutoAttack();
				_attack_target = target;
				clientStartAutoAttack();
				notifyEvent(CtrlEvent.EVT_THINK, null);
			}
		} else {
			clientStopAutoAttack();
			changeIntention(AI_INTENTION_ATTACK);
			_attack_target = target;
			clientStartAutoAttack();
			notifyEvent(CtrlEvent.EVT_THINK, null);
		}
	}
	
	protected void onIntentionCast(L2Skill skill, L2Object target)
	{
		if (getIntention() == AI_INTENTION_REST) {
			clientActionFailed();
			return;
		}
		if (_actor.isAllSkillsDisabled()) {
			clientActionFailed();
			return;
		}
		// can't cast if muted
		if (_actor.isMuted() && skill.isMagic()){
			clientActionFailed();
			return;
		}

		_cast_target = (L2Character)target;
		clientStopAutoAttack();
		_actor.abortAttack();
		clientActionFailed();
		_skill = skill;
		changeIntention(AI_INTENTION_CAST);
		notifyEvent(CtrlEvent.EVT_THINK, null);
	}
	
	protected void onIntentionMoveTo(L2CharPosition pos)
	{
		if (getIntention() == AI_INTENTION_REST) {
			clientActionFailed();
			return;
		}
		if (_actor.isAllSkillsDisabled()) {
			clientActionFailed();
			return;
		}
		changeIntention(AI_INTENTION_MOVE_TO);
		clientStopAutoAttack();
		moveTo(pos.x, pos.y, pos.z);
	}
	
	protected void onIntentionFollow(L2Character target)
	{
		if (getIntention() == AI_INTENTION_REST) {
			clientActionFailed();
			return;
		}
		if (_actor.isAllSkillsDisabled()) {
			clientActionFailed();
			return;
		}
		_follow_target = target;
		changeIntention(AI_INTENTION_FOLLOW);
		clientStopAutoAttack();
		moveToPawn(target, 60);
	}
	
	protected void onIntentionPickUp(L2Object object)
	{
		if (getIntention() == AI_INTENTION_REST) {
			clientActionFailed();
			return;
		}
		if (_actor.isAllSkillsDisabled()) {
			clientActionFailed();
			return;
		}
		changeIntention(AI_INTENTION_PICK_UP);
		_target = object;
		moveToPawn(object, 20);
	}
	
	protected void onIntentionInteract(L2Object object)
	{
		if (getIntention() == AI_INTENTION_REST) {
			clientActionFailed();
			return;
		}
		if (_actor.isAllSkillsDisabled()) {
			clientActionFailed();
			return;
		}
		if (getIntention() != AI_INTENTION_INTERACT)
		{
			clientStopAutoAttack();
			changeIntention(AI_INTENTION_INTERACT);
			_target = object;
			moveToPawn(object, 60);
		}
	}

	//
	// Default event handling implementation
	//
	
	
	protected void onEvtThink()
	{
		// do nothing
	}

	protected void onEvtAttacked(L2Character attacker)
	{
		// do nothing
	}
	protected void onEvtAggression(L2Character target, int aggro)
	{
		// do nothing
	}

	protected void onEvtStunned(L2Character attacker)
	{
		clientStopAutoAttack();
		clientStopMoving(null);
		onEvtAttacked(attacker);
	}
	protected void onEvtSleeping(L2Character attacker)
	{
		clientStopAutoAttack();
		clientStopMoving(null);
		_target = null;
		_cast_target = null;
		_attack_target = null;
		setIntention(AI_INTENTION_IDLE);
	}
	protected void onEvtRooted(L2Character attacker)
	{
		clientStopMoving(null);
		onEvtAttacked(attacker);
	}

	protected void onEvtConfused(L2Character attacker) {
		// do nothing
		clientStopMoving(null);
		onEvtAttacked(attacker);
	}
	
	protected void onEvtReadyToAct()
	{
		onEvtThink();
	}

	protected void onEvtUserCmd(Object arg0, Object arg1)
	{
		// do nothing
	}

	protected void onEvtArrived()
	{
		if (getIntention() == AI_INTENTION_MOVE_TO)
			setIntention(AI_INTENTION_ACTIVE);
		onEvtThink();
	}

	protected void onEvtArrivedRevalidate()
	{
		onEvtThink();
	}
	
	protected void onEvtArrivedBlocked(L2CharPosition blocked_at_pos)
	{
		clientStopMoving(blocked_at_pos);
		if (getIntention() == AI_INTENTION_MOVE_TO)
			setIntention(AI_INTENTION_ACTIVE);
		onEvtThink();
	}
	
	protected void onEvtForgetObject(L2Object object)
	{
		if (_target == object)
		{
			_target = null;
			if (getIntention() == AI_INTENTION_INTERACT)
				setIntention(AI_INTENTION_ACTIVE);
			else if (getIntention() == AI_INTENTION_PICK_UP)
				setIntention(AI_INTENTION_ACTIVE);
		}
		if (_attack_target == object)
		{
			clientStopAutoAttack();
			_attack_target = null;
			setIntention(AI_INTENTION_ACTIVE);
		}
		if (_cast_target == object)
		{
			_cast_target = null;
			setIntention(AI_INTENTION_ACTIVE);
		}
		if (_follow_target == object)
		{
			clientStopMoving(null);
			_follow_target = null;
			_followin_target = false;
			setIntention(AI_INTENTION_ACTIVE);
		}
		if (_actor == object)
		{
			_target = null;
			_attack_target = null;
			_cast_target = null;
			_follow_target = null;
			_followin_target = false;
			clientStopMoving(null);
			changeIntention(AI_INTENTION_IDLE);
		}
	}

	protected void onEvtCancel()
	{
		onEvtThink();
	}

	protected void onEvtDead()
	{
		clientNotifyDead();
	}
	
	
	//
	// Helper methods
	//
	
	
	protected boolean maybeMoveToPawn(L2Object target, int offset)
	{
		double dist = _actor.getDistance(target.getX(), target.getY());
		if (dist > offset+20)
		{
			if (!_actor.isRunning())
				_actor.setRunning();
			if (_actor.isMovementDisabled())
				return true;
			moveToPawn(target, offset);
			return true;
		}
		else
		{
			clientStopMoving(null);
			return false;
		}
	}
	
	protected boolean checkTargetLostOrDead(L2Character target)
	{
		if (target == null || target.isAlikeDead())
		{
			clientStopAutoAttack();
			clientStopMoving(null);
			setIntention(AI_INTENTION_ACTIVE);
			return true;
		}
		return false;
	}

	protected boolean checkTargetLost(L2Object target)
	{
		if (target == null)
		{
			clientStopAutoAttack();
			clientStopMoving(null);
			setIntention(AI_INTENTION_ACTIVE);
			return true;
		}
		return false;
	}
}
