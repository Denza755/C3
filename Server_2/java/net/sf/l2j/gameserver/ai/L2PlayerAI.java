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

import static net.sf.l2j.gameserver.ai.CtrlIntention.AI_INTENTION_ATTACK;
import static net.sf.l2j.gameserver.ai.CtrlIntention.AI_INTENTION_CAST;
import static net.sf.l2j.gameserver.ai.CtrlIntention.AI_INTENTION_IDLE;
import static net.sf.l2j.gameserver.ai.CtrlIntention.AI_INTENTION_INTERACT;
import static net.sf.l2j.gameserver.ai.CtrlIntention.AI_INTENTION_PICK_UP;
import static net.sf.l2j.gameserver.ai.CtrlIntention.AI_INTENTION_REST;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2PcInstance;
import net.sf.l2j.gameserver.model.L2Character.AIAccessor;

public class L2PlayerAI extends L2CharacterAI {

	private boolean thinking; // to prevent recursive thinking
	
	public L2PlayerAI(AIAccessor accessor) {
		super(accessor);
	}

	protected void onIntentionRest()
	{
		if (getIntention() != AI_INTENTION_REST)
		{
			changeIntention(AI_INTENTION_REST);
			_target = null;
			_attack_target = null;
			_follow_target = null;
			clientStopAutoAttack();
			clientStopMoving(null);
			clientSitDown();
		}
	}
	
	protected void onIntentionActive()
	{
		setIntention(AI_INTENTION_IDLE);
	}
	
	private void thinkAttack() {
		if (checkTargetLostOrDead(_attack_target))
		{
			_attack_target = null;
			return;
		}
		if (maybeMoveToPawn(_attack_target, _actor.getPhysicalAttackRange()))
			return;
		clientStopMoving(null);
		clientStartAutoAttack();
		_accessor.doAttack(_attack_target);
		return;
	}
	
	private void thinkCast() {
		if (checkTargetLost(_cast_target))
		{
			_cast_target = null;
			return;
		}
		if (maybeMoveToPawn(_cast_target, _actor.getMagicalAttackRange(_skill)))
			return;
		
		L2Object oldTarget = _actor.getTarget();
		L2Character target = _cast_target;
		setIntention(AI_INTENTION_IDLE);
		
		if (oldTarget != null)
		{
			_actor.setTarget(target);
			_accessor.doCast(_skill);
			_actor.setTarget(oldTarget);
		}
		else
			_accessor.doCast(_skill);

		return;
	}
	
	private void thinkPickUp() {
		if (_actor.isAllSkillsDisabled())
			return;
		if (checkTargetLost(_target))
			return;
		if (maybeMoveToPawn(_target, 36))
			return;
		setIntention(AI_INTENTION_IDLE);
		((L2PcInstance.AIAccessor)_accessor).doPickupItem(_target);
		return;
	}
	
	private void thinkInteract() {
		if (_actor.isAllSkillsDisabled())
			return;
		if (checkTargetLost(_target))
			return;
		if (maybeMoveToPawn(_target, 36))
			return;
		((L2PcInstance.AIAccessor)_accessor).doInteract((L2Character)_target);
		setIntention(AI_INTENTION_IDLE);
		return;
	}
	
	protected void onEvtThink()
	{
		if (thinking || _actor.isAllSkillsDisabled())
			return;
		thinking = true;
		try
		{
			if (getIntention() == AI_INTENTION_ATTACK)
				thinkAttack();
			else if (getIntention() == AI_INTENTION_CAST)
				thinkCast();
			else if (getIntention() == AI_INTENTION_PICK_UP)
				thinkPickUp();
			else if (getIntention() == AI_INTENTION_INTERACT)
				thinkInteract();
		} finally {
			thinking = false;
		}
	}

}
