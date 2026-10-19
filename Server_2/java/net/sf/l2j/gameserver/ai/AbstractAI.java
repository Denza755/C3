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

import static net.sf.l2j.gameserver.ai.CtrlIntention.AI_INTENTION_IDLE;
import net.sf.l2j.gameserver.GameTimeController;
import net.sf.l2j.gameserver.model.L2CharPosition;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2PcInstance;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.serverpackets.AutoAttackStart;
import net.sf.l2j.gameserver.serverpackets.AutoAttackStop;
import net.sf.l2j.gameserver.serverpackets.CharMoveToLocation;
import net.sf.l2j.gameserver.serverpackets.Die;
import net.sf.l2j.gameserver.serverpackets.MoveToPawn;
import net.sf.l2j.gameserver.serverpackets.ServerBasePacket;
import net.sf.l2j.gameserver.serverpackets.StopMove;
import net.sf.l2j.gameserver.serverpackets.StopRotation;

/**
 * @author mkizub
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
abstract class AbstractAI implements Ctrl {

	/** The character this AI manages */
	final L2Character _actor;
	/** An accessor for private methods of the actor */
	final L2Character.AIAccessor _accessor;
	/** Current long-term intention */
	private CtrlIntention _intention = AI_INTENTION_IDLE;
	
	/** Flags about client's state, so we will know which messages to send */
	private boolean _client_moving;
	private boolean _client_auto_attacking;
	private int _client_moving_to_pawn_offset;
	
	/** Different targets this AI maintains */
	L2Object    _target;
	L2Character _cast_target;
	L2Character _attack_target;
	L2Character _follow_target;
	
	/** The skill we are curently casting by INTENTION_CAST */
	L2Skill _skill;
	
	/** Diferent internal state flags */
	boolean _followin_target;
	private int _move_to_pawn_timeout;
	
	
	protected AbstractAI(L2Character.AIAccessor accessor)
	{
		_accessor = accessor;
		_actor = accessor.getActor();
	}
	
	/** the character this AI serves */
	public L2Character getActor() {
		return _actor;
	}
	
	/** get current intention */
	public CtrlIntention getIntention() {
		return _intention;
	}
	
	/** get current attack target */
	public L2Character getAttackTarget() {
		return _attack_target;
	}
	
	/** used by AI classes */
	void changeIntention(CtrlIntention intention)
	{
		_intention = intention;
	}
	
	/** Request to set general state/intention for AI, with optional target */
	public final void setIntention(CtrlIntention intention)
	{
		setIntention(intention, null, null);
	}
	
	/** Request to set general state/intention for AI, with optional target */
	public final void setIntention(CtrlIntention intention, Object arg0)
	{
		setIntention(intention, arg0, null);
	}
	
	/** Request to set general state/intention for AI, with optional target */
	public final void setIntention(CtrlIntention intention, Object arg0, Object arg1)
	{
		if (!_actor.isVisible() || !_actor.hasAI())
			return;
		switch (intention)
		{
		case AI_INTENTION_IDLE:
			onIntentionIdle();
			break;
		case AI_INTENTION_ACTIVE:
			onIntentionActive();
			break;
		case AI_INTENTION_REST:
			onIntentionRest();
			break;
		case AI_INTENTION_ATTACK: 
			onIntentionAttack((L2Character)arg0);
			break;
		case AI_INTENTION_CAST:
			onIntentionCast((L2Skill)arg0, (L2Object)arg1);
			break;
		case AI_INTENTION_MOVE_TO:
			onIntentionMoveTo((L2CharPosition)arg0);
			break;
		case AI_INTENTION_FOLLOW:
			onIntentionFollow((L2Character)arg0);
			break;
		case AI_INTENTION_PICK_UP:
			onIntentionPickUp((L2Object)arg0);
			break;
		case AI_INTENTION_INTERACT:
			onIntentionInteract((L2Object)arg0);
			break;
		}
	}
	
	public final void notifyEvent(CtrlEvent evt)
	{
		notifyEvent(evt, null, null);
	}
	
	public final void notifyEvent(CtrlEvent evt, Object arg0)
	{
		notifyEvent(evt, arg0, null);
	}
	
	/** Event, that notifies about previous step result, or user command,
	 * that does not change current general intention */
	public final void notifyEvent(CtrlEvent evt, Object arg0, Object arg1)
	{
		if (!_actor.isVisible() || !_actor.hasAI())
			return;
		switch (evt)
		{
		case EVT_THINK:
			onEvtThink();
			break;
		case EVT_ATTACKED: 
			onEvtAttacked((L2Character)arg0);
			break;
		case EVT_AGGRESSION:
			onEvtAggression((L2Character)arg0, ((Number)arg1).intValue());
			break;
		case EVT_STUNNED:
			onEvtStunned((L2Character)arg0);
			break;
		case EVT_SLEEPING:
			onEvtSleeping((L2Character)arg0);
			break;
		case EVT_ROOTED:
			onEvtRooted((L2Character)arg0);
			break;
		case EVT_CONFUSED:
			onEvtConfused((L2Character)arg0);
			break;
		case EVT_READY_TO_ACT: 
			onEvtReadyToAct();
			break;
		case EVT_USER_CMD:
			onEvtUserCmd(arg0, arg1);
			break;
		case EVT_ARRIVED:
			onEvtArrived();
			break;
		case EVT_ARRIVED_REVALIDATE:
			onEvtArrivedRevalidate();
			break;
		case EVT_ARRIVED_BLOCKED:
			onEvtArrivedBlocked((L2CharPosition)arg0);
			break;
		case EVT_FORGET_OBJECT:
			onEvtForgetObject((L2Object)arg0);
			break;
		case EVT_CANCEL:
			onEvtCancel();
			break;
		case EVT_DEAD:
			onEvtDead();
			break;
		}
	}

	protected abstract void onIntentionIdle();
	protected abstract void onIntentionActive();
	protected abstract void onIntentionRest();
	protected abstract void onIntentionAttack(L2Character target);
	protected abstract void onIntentionCast(L2Skill skill, L2Object target);
	protected abstract void onIntentionMoveTo(L2CharPosition destination);
	protected abstract void onIntentionFollow(L2Character target);
	protected abstract void onIntentionPickUp(L2Object item);
	protected abstract void onIntentionInteract(L2Object object);
	

	protected abstract void onEvtThink();
	protected abstract void onEvtAttacked(L2Character attacker);
	protected abstract void onEvtAggression(L2Character target, int aggro);
	protected abstract void onEvtStunned(L2Character attacker);
	protected abstract void onEvtSleeping(L2Character attacker);
	protected abstract void onEvtRooted(L2Character attacker);
	protected abstract void onEvtConfused(L2Character attacker);
	protected abstract void onEvtReadyToAct();
	protected abstract void onEvtUserCmd(Object arg0, Object arg1);
	protected abstract void onEvtArrived();
	protected abstract void onEvtArrivedRevalidate();
	protected abstract void onEvtArrivedBlocked(L2CharPosition blocked_at_pos);
	protected abstract void onEvtForgetObject(L2Object object);
	protected abstract void onEvtCancel();
	protected abstract void onEvtDead();
	
	/** low-level function, action failed */
	protected void clientActionFailed()
	{
		if (_actor instanceof L2PcInstance)
			_actor.sendPacket(new ActionFailed());
	}
	
	/** low-level function, to sit down */
	protected void clientSitDown()
	{
		if (_actor instanceof L2PcInstance)
		{
			L2PcInstance player = (L2PcInstance)_actor;
			player.sitDown();
		}
	}
	
	/** low-level function, to stand up */
	protected void clientStandUp()
	{
		if (_actor instanceof L2PcInstance)
		{
			L2PcInstance player = (L2PcInstance)_actor;
			player.standUp();
		}
	}
	
	/** low-level function, call it only when you really need to move */
	protected void moveToPawn(L2Object pawn, int offset)
	{
		if (!_actor.isMovementDisabled())
		{
			if (offset < 10)
				offset = 10;
			// don't send packets too often, check we already moving to this pawn
			if (_client_moving && _target == pawn && _client_moving_to_pawn_offset == offset) {
				if (GameTimeController.getGameTicks() < _move_to_pawn_timeout)
					return;
			}
			_client_moving = true;
			_client_moving_to_pawn_offset = offset;
			_target = pawn;
			_followin_target = false;
			_move_to_pawn_timeout = GameTimeController.getGameTicks();
			_move_to_pawn_timeout += 1900 / GameTimeController.MILLIS_IN_TICK;
			_accessor.moveTo(pawn.getX(), pawn.getY(), pawn.getZ(), offset);
			ServerBasePacket msg;
			if (pawn instanceof L2Character)
				msg = new MoveToPawn(_actor, (L2Character)pawn, offset);
			else
				msg = new CharMoveToLocation(_actor);
			_actor.broadcastPacket(msg);
		} else {
			_actor.sendPacket(new ActionFailed());
		}
	}
	
	/** low-level function, call it only when you really need to move */
	protected void moveTo(int x, int y, int z)
	{
		if (!_actor.isMovementDisabled())
		{
			_client_moving = true;
			_client_moving_to_pawn_offset = 0;
			_accessor.moveTo(x, y, z);
			CharMoveToLocation msg = new CharMoveToLocation(_actor);
			_actor.broadcastPacket(msg);
		} else {
			_actor.sendPacket(new ActionFailed());
		}
	}
	
	/** low-level function, call it only when you really have to stop character */
	protected void clientStopMoving(L2CharPosition pos)
	{
		_accessor.stopMove(pos);
		_client_moving_to_pawn_offset = 0;
		if (_client_moving || pos != null)
		{
			_client_moving = false;
			StopMove msg = new StopMove(_actor);
			_actor.broadcastPacket(msg);
			if (pos != null)
			{
				StopRotation sr = new StopRotation(_actor, pos.heading);
				_actor.sendPacket(sr);
				_actor.broadcastPacket(sr);
			}
		}
	}
	
	/** low-level function, starts auto-attacking mode of the client */
	protected void clientStartAutoAttack()
	{
		if (!_client_auto_attacking)
		{
			_client_auto_attacking = true;
			AutoAttackStart msg = new AutoAttackStart(_actor.getObjectId()); 
			_actor.broadcastPacket(msg);
		}
	}
	
	/** low-level function, stops auto-attacking mode of the client */
	protected void clientStopAutoAttack()
	{
		if (_client_auto_attacking)
		{
			_client_auto_attacking = false;
			AutoAttackStop msg = new AutoAttackStop(_actor.getObjectId());
			_actor.broadcastPacket(msg);
		}
	}
	
	/** low-level function, character is dead */
	protected void clientNotifyDead()
	{
		clientStopAutoAttack();
		clientStopMoving(null);
		Die msg = new Die(_actor);
		_actor.broadcastPacket(msg);
		_intention = AI_INTENTION_IDLE;
		_target        = null;
		_cast_target   = null;
		_attack_target = null;
		_follow_target = null;
	}
	
	public void describeStateToPlayer(L2PcInstance player)
	{
		if (_client_moving) 
		{
			if (_client_moving_to_pawn_offset != 0 && _follow_target != null)
			{
				MoveToPawn msg = new MoveToPawn(_actor, _follow_target, _client_moving_to_pawn_offset);
				player.sendPacket(msg);
			}
			else
			{
				CharMoveToLocation msg = new CharMoveToLocation(_actor);
				player.sendPacket(msg);
			}
		}
		if (_client_auto_attacking)
		{
			AutoAttackStart msg = new AutoAttackStart(_actor.getObjectId()); 
			player.sendPacket(msg);
		}
	}
}
