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

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.ai.CtrlIntention;
import net.sf.l2j.gameserver.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.serverpackets.MyTargetSelected;
import net.sf.l2j.gameserver.serverpackets.ValidateLocation;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;

/**
 * This class represents all guards in the world. It inherits all methods from
 * L2Attackable and adds some more such as tracking PK's or custom interactions.
 * 
 * @version $Revision: 1.11.2.1.2.7 $ $Date: 2005/04/06 16:13:40 $
 */
public final class L2GuardInstance extends L2Attackable
{
	//private static Logger _log = Logger.getLogger(L2GuardInstance.class.getName());

	private int _homeX;
	private int _homeY;
	private int _homeZ;

	public L2GuardInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
	}

	public boolean isAutoAttackable(L2Character attacker) {
		return attacker instanceof L2MonsterInstance;
	}
	
	public boolean addKnownObject(L2Object object, L2Character dropper)
	{
		boolean ret = super.addKnownObject(object, dropper); 
		if (!ret)
			return false;

		if (_homeX == 0) getHomeLocation();
		if (object instanceof L2PcInstance) 
		{
			L2PcInstance player = (L2PcInstance) object;
			if ( (player.getKarma() > 0) )
			{
				if (Config.DEBUG) _log.fine(getObjectId()+": PK "+player.getObjectId()+" entered scan range");
				if (getAI().getIntention() == CtrlIntention.AI_INTENTION_IDLE)
					getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE, null);
			}
		}
		else if (object instanceof L2MonsterInstance) 
		{
			L2MonsterInstance mob = (L2MonsterInstance) object;
			if (mob.isAggressive() )
			{
				if (Config.DEBUG) _log.fine(getObjectId()+": Aggressive mob "+mob.getObjectId()+" entered scan range");
				if (getAI().getIntention() == CtrlIntention.AI_INTENTION_IDLE)
					getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE, null);
			}
		}
		return ret;
	}		

	public void removeKnownObject(L2Object object)
	{
		super.removeKnownObject(object);
		
		if (noTarget()) {
			clearAggroList();
			removeAllKnownObjects();
			getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE, null);
			returnHome();
		}
	}

	/**
	 * Sets home location of guard. Guard will always try to return to this location after
	 * it has killed all PK's in range.
	 *
	 */
	private void getHomeLocation()
	{
		_homeX = getX();
		_homeY = getY();
		_homeZ = getZ();

		if (Config.DEBUG)
			_log.finer(getObjectId()+": Home location set to"+
					" X:" + _homeX + " Y:" + _homeY + " Z:" + _homeZ);
	}

	/**
	 * This method forces guard to return to home location previously set
	 *
	 */
	public void returnHome()
	{
		if (getDistance(_homeX, _homeY) > 40) {
			if (Config.DEBUG) _log.fine(getObjectId()+": moving hometo" + " X:" + _homeX + " Y:" + _homeY + " Z:" + _homeZ);
			clearAggroList();
			getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO,
					new L2CharPosition(_homeX, _homeY, _homeZ, 0));
		}
	}
	
	/*
	 * Just to make sure HomeLocation is set
	 * @see net.sf.l2j.gameserver.model.L2Object#OnSpawn()
	 */
	public void OnSpawn()
	{
		_homeX = getX();
		_homeY = getY();
		_homeZ = getZ();
		
		if (Config.DEBUG)
			_log.finer(getObjectId()+": Home location set to"+
					" X:" + _homeX + " Y:" + _homeY + " Z:" + _homeZ);		
	}	
	
	/**
	 * Override L2NpcInstance method to get correct html path
	 * 
	 */
	public String getHtmlPath(int npcId, int val)
	{
		String pom = "";
		if (val == 0)
		{
			pom = "" + npcId;
		} 
		else 
		{
			pom = npcId + "-" + val;
		}
		return "data/html/guard/" + pom + ".htm";
	}

	/**
	 * Custom onAction behaviour. Note that super() is not called because guards need
	 * extra check to see if a player should interract ot attack them when clicked.
	 * 
	 */
	public void onAction(L2PcInstance player)
	{
		if (getObjectId() != player.getTargetId())
		{
			player.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE, null);
//			player.setCurrentState(L2Character.STATE_IDLE);
			if (Config.DEBUG) _log.fine(player.getObjectId()+": Targetted guard "+getObjectId());
			player.setTarget(this);
			MyTargetSelected my = new MyTargetSelected(getObjectId(), 0);
			player.sendPacket(my);

			// correct location
			player.sendPacket(new ValidateLocation(this));
		}
		else
		{
			if (containsTarget(player)) {
				if (Config.DEBUG) _log.fine(player.getObjectId()+": Attacked guard "+getObjectId());
				//player.startAttack(this);
				player.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, this);
			} else {
				double distance = getDistance(player.getX(), player.getY());
				if (distance > INTERACTION_DISTANCE) {
					player.getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, this);
//					player.setCurrentState(L2Character.STATE_INTERACT);
//					player.moveTo(this.getX(), this.getY(), this.getZ(), INTERACTION_DISTANCE);
				} else {
					showChatWindow(player, 0);
					player.sendPacket(new ActionFailed());					
					player.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE, null);
//					player.setCurrentState(L2Character.STATE_IDLE);
				}
			}
		}
	}
}
