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
import net.sf.l2j.gameserver.GameServer;
import net.sf.l2j.gameserver.ai.CtrlIntention;
import net.sf.l2j.gameserver.ai.L2CharacterAI;
import net.sf.l2j.gameserver.ai.L2SiegeGuardAI;
import net.sf.l2j.gameserver.lib.Rnd;
import net.sf.l2j.gameserver.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.serverpackets.MyTargetSelected;
import net.sf.l2j.gameserver.serverpackets.NpcInfo;
import net.sf.l2j.gameserver.serverpackets.SocialAction;
import net.sf.l2j.gameserver.serverpackets.StatusUpdate;
import net.sf.l2j.gameserver.serverpackets.ValidateLocation;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;

/**
 * This class represents all guards in the world. It inherits all methods from
 * L2Attackable and adds some more such as tracking PK's or custom interactions.
 * 
 * @version $Revision: 1.11.2.1.2.7 $ $Date: 2005/04/06 16:13:40 $
 */
public final class L2SiegeGuardInstance extends L2Attackable
{
    //private static Logger _log = Logger.getLogger(L2GuardInstance.class.getName());

    private int _homeX;
    private int _homeY;
    private int _homeZ;
    
    public L2SiegeGuardInstance(int objectId, L2NpcTemplate template)
    {
        super(objectId, template);
    }

	public L2CharacterAI getAI() 
	{
		return setAI(new L2SiegeGuardAI(new AIAccessor()));
	}    
    
	/**
	 * Return True if a siege is in progress and the L2Character attacker isn't a Defender.<BR><BR>
	 * 
	 * @param attacker The L2Character that the L2SiegeGuardInstance try to attack
	 * 
	 */
    public boolean isAutoAttackable(L2Character attacker) 
	{
		// Attackable during siege by attacker only
		return (attacker != null 
		        && attacker instanceof L2PcInstance 
		        && getCastle() != null
		        && getCastle().getCastleId() > 0
		        && getCastle().getSiege().getIsInProgress()
		        && getCastle().getSiege().checkIsAttacker(((L2PcInstance)attacker).getClan()));
    }
    
    public boolean addKnownObject(L2Object object, L2Character dropper)
    {
		if(!super.addKnownObject(object, dropper))
			return false;
        
        if(_homeX == 0) 
        	getHomeLocation();

        // Check if siege is in progress 
        if(getCastle() != null && getCastle().getSiege().getIsInProgress())
        {
            L2PcInstance player = null;
            if(object instanceof L2PcInstance)
                player = (L2PcInstance) object;
            else if (object instanceof L2Summon)
                player = ((L2Summon)object).getOwner();

            // Check if player is not the defender
            if (player != null && (player.getClan() == null || getCastle().getSiege().getAttackerClan(player.getClan()) != null))
            {
                //if (Config.DEBUG) _log.fine(getObjectId()+": PK "+player.getObjectId()+" entered scan range");
                if(getIntention() == CtrlIntention.AI_INTENTION_IDLE)
                    setIntention(CtrlIntention.AI_INTENTION_ACTIVE, null, null);//(L2Character)object);
            }

        }

        return true;
    }		
    
    public void removeKnownObject(L2Object object)
    {
        super.removeKnownObject(object);
        
        if (noTarget()) 
        {
            clearAggroList();
			if(!isKnownPlayers())
				removeAllKnownObjects();
			else
				clearAllKnownObjects();
            setIntention(CtrlIntention.AI_INTENTION_IDLE, null, null);
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
    
    public int getHomeX()
    {
    	return _homeX;
    }
    
    public int getHomeY()
    {
    	return _homeY;
    }
    
    /**
     * This method forces guard to return to home location previously set
     *
     */
    public void returnHome()
    {
        if (getDistance(_homeX, _homeY) > 40) {
            if (Config.DEBUG) _log.fine(getObjectId()+": moving home");
            clearAggroList();
            setIntention(CtrlIntention.AI_INTENTION_MOVE_TO,
                    new L2CharPosition(_homeX, _homeY, _homeZ, 0), null);
        }
    }
    
    /**
     * Custom onAction behaviour. Note that super() is not called because guards need
     * extra check to see if a player should interract ot ATTACK them when clicked.
     * 
     */
    @SuppressWarnings("static-access")
	public void onAction(L2PcInstance player)
    {
		if(GameServer.events.onAction(player, this))
			return;

		if (this != player.getTarget())
		{
			if (Config.DEBUG) _log.fine("new target selected:"+getObjectId());
			//player.setNewTarget(this);
			
			player.sendPacket(new NpcInfo(this, player));
			
			player.setTarget(this);
			player.sendPacket(new MyTargetSelected(getObjectId(), player.getLevel() - getLevel()));
			
			//if ((isAutoAttackable(player))||(this instanceof L2SiegeGuardInstance))
			//{	
				StatusUpdate su = new StatusUpdate(getObjectId());
				su.addAttribute(StatusUpdate.CUR_HP, (int)getCurrentHp() );
				su.addAttribute(StatusUpdate.MAX_HP, getMaxHp() );
				player.sendPacket(su);
			//}
			
			// correct location
			player.sendPacket(new ValidateLocation(this));
		}
		else
		{
			player.sendPacket(new MyTargetSelected(getObjectId(), player.getLevel() - getLevel()));
			
			if (isAutoAttackable(player) && !isAlikeDead())
			{
				if (Math.abs(player.getZ() - getZ()) < 600) // this max heigth difference might need some tweaking
				{
					player.setIntention(CtrlIntention.AI_INTENTION_ATTACK, this, null);
//					player.startAttack(this);
				}
				else
				{
					player.sendPacket(new ActionFailed());
				}
			}
			if(!isAutoAttackable(player))
			{
				player.setIntention(CtrlIntention.AI_INTENTION_INTERACT, this, null);
				double distance = getDistance(player.getX(), player.getY());
				if (distance > INTERACTION_DISTANCE) 
				{
//					player.setCurrentState(L2Character.STATE_INTERACT);
//					player.setInteractTarget(this);
//					player.moveTo(this.getX(), this.getY(), this.getZ(), INTERACTION_DISTANCE);
				} 
				else 
				{
					broadcastPacket(new SocialAction(getObjectId(), Rnd.get(8)));
					showChatWindow(player, 0);
					player.sendPacket(new ActionFailed());					
				}
			}
		}
		//super.onAction(player);
    }
    
    public void addDamageHate(L2Character attacker, int damage, int aggro)
    {
        if (attacker == null)
            return;
        
        if (!(attacker instanceof L2SiegeGuardInstance))
        {
            super.addDamageHate(attacker, damage, aggro);
        }
    }
}
