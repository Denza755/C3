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

import net.sf.l2j.gameserver.ai.CtrlEvent;
import net.sf.l2j.gameserver.ai.CtrlIntention;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;

/**
 * This class represents Friendly Mobs lying over the world.
 * These friendly mobs should ony attack players with karma > 0
 * and it is always aggro, since it just attacks players with karma
 * 
 * @version $Revision: 1.20.4.6 $ $Date: 2005/07/23 16:13:39 $
 */
public class L2FriendlyMobInstance extends L2Attackable {

	public L2FriendlyMobInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
	}	

	public boolean isAutoAttackable(L2Character attacker) {
		if (attacker instanceof L2PcInstance)
			return ((L2PcInstance)attacker).getKarma() > 0;
		return false;
	}
	
	public boolean isAggressive()
	{
		return true;
	}

	public boolean hasRandomAnimation()
	{
		return false;
	}

	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.model.L2Character#addKnownObject(net.sf.l2j.gameserver.model.L2Object)
	 */
	public boolean addKnownObject(L2Object object, L2Character dropper)
	{
		if(!super.addKnownObject(object, dropper))
			return false;

		if (object instanceof L2PcInstance && getIntention() == CtrlIntention.AI_INTENTION_IDLE) 
			setIntention(CtrlIntention.AI_INTENTION_ACTIVE, null, null);

		return true;
	}
	
	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.model.L2Character#removeKnownObject(net.sf.l2j.gameserver.model.L2Object)
	 */
	public void removeKnownObject(L2Object object)
	{
		super.removeKnownObject(object);
		if (!(object instanceof L2Character))
			return;
		if (hasAI()) {
			L2Character temp = (L2Character)object;
			notifyEvent(CtrlEvent.EVT_FORGET_OBJECT, object, null);
			if (getTarget() == temp)
				setTarget(null);
		}	
	
		if (isVisible() && !isKnownPlayers())
		{
			clearAggroList();
			removeAllKnownObjects();
			setIntention(CtrlIntention.AI_INTENTION_IDLE, null, null);
			return;
		}
	}

}
