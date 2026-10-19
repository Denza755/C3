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
package net.sf.l2j.gameserver.clientpackets;

import java.nio.ByteBuffer;
import java.util.logging.Logger;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.ClientThread;
import net.sf.l2j.gameserver.ai.CtrlIntention;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2PcInstance;
import net.sf.l2j.gameserver.model.L2Summon;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.serverpackets.Ride;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;
/**
 * This class ...
 * 
 * @version $Revision: 1.11.2.7.2.9 $ $Date: 2005/04/06 16:13:48 $
 */
public class RequestActionUse extends ClientBasePacket
{
	private static final String _C__45_REQUESTACTIONUSE = "[C] 45 RequestActionUse";
	private static Logger _log = Logger.getLogger(RequestActionUse.class.getName());
	
	private final int _actionId;
	private final boolean _ctrlPressed;
	private final boolean _shiftPressed;
	/**
	 * packet type id 0x45
	 * format:		cddc
	 * @param rawPacket
	 */
	public RequestActionUse(ByteBuffer buf, ClientThread client)
	{
		super(buf, client);
		_actionId     = readD();
		_ctrlPressed  = readD()==1 ;
		_shiftPressed =  readC()==1;
	}

	void runImpl()
	{
		if (Config.DEBUG)
			_log.finest("request Action use: id "+_actionId + " 2:" + _ctrlPressed + " 3:"+_shiftPressed);
		L2PcInstance activeChar = getClient().getActiveChar();
		// dont do anything if player is dead
		if (activeChar.isDead())
		{
			activeChar.sendPacket(new ActionFailed());
			return;
		}
		
		// don't do anything if player is confused
		if (activeChar.isOutOfControl())
		{
			activeChar.sendPacket(new ActionFailed());
			return;
		}

		switch (_actionId)
		{
			case 0:
			{
				if (activeChar.isSitting())
					activeChar.standUp();
				else
					activeChar.sitDown();
				if (Config.DEBUG) _log.fine("new wait type: "+(activeChar.isSitting() ? "STANDING" : "SITTING"));
				
				break;
			}
			case 1:
			{
				if (activeChar.isRunning())
					activeChar.setWalking();
				else
					activeChar.setRunning();
				if (Config.DEBUG) _log.fine("new move type: "+(activeChar.isRunning() ? "RUNNING" : "WALKIN"));
				break;
			}
            case 21:
			case 15:
			{
				L2Summon pet = activeChar.getPet(); 
				if (pet != null)
					pet.setFollowStatus(!pet.getFollowStatus());
				break;
			}
            case 22:
			case 16:
			{
                L2Object target = activeChar.getTarget();
				if (target != null && activeChar.getPet()!=null && activeChar.getPet()!= target && activeChar != target)
				{
				    if (activeChar.getPet().withinZones(L2World.zones) || target.withinZones(L2World.zones))
                    {
                        activeChar.sendPacket(new SystemMessage(SystemMessage.TARGET_IN_PEACEZONE));
                        return;
                    }
                    
                    if (target.isAutoAttackable(activeChar) || _ctrlPressed)
                    {
                        //activeChar.getPet().startAttack((L2Character)activeChar.getTarget());
                    	activeChar.getPet().getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, activeChar.getTarget());
                    }
				}
				break;
			}
            case 23:
			case 17:
			{
				L2Summon pet = activeChar.getPet(); 
				if (pet != null)
					pet.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE, null);
				break;
			}
			case 18:
			{
				_log.warning("unhandled action type 18");
				break;
			}
			case 19:
			{
				if (activeChar.getPet()!=null)
				{
					//returns pet to control item
					if (activeChar.getPet().isDead())
					{
						SystemMessage sm = new SystemMessage(SystemMessage.DEAD_PET_CANNOT_BE_RETURNED);
						activeChar.sendPacket(sm);
					}
					else
					{
					activeChar.getPet().unSummon(activeChar); 
				}
				}
				break;
			}
			case 20:
			{
                _log.warning("unhandled action type 20");
				break;
			}
			case 38:
			{
                L2Summon pet = activeChar.getPet();
                
				//mount
				if (pet != null && pet.isMountable() && !activeChar.isMounted()) 
				{
                    Ride mount = new Ride(activeChar.getObjectId(), Ride.ACTION_MOUNT, pet.getTemplate().npcId);
                    activeChar.sendPacket(mount);
                    activeChar.broadcastPacket(mount);
                    activeChar.setMountType(mount.getMountType());
                    pet.PetdeleteMe(activeChar);
                }
                else if (activeChar.isMounted())
                {                        
					Ride dismount = new Ride(activeChar.getObjectId(), Ride.ACTION_DISMOUNT, 0);
					activeChar.sendPacket(dismount);	
                    activeChar.broadcastPacket(dismount);
                    activeChar.setMountType(0);
                }
                break;
			}
            default:
            {
                _log.warning("unhandled action type "+_actionId);
            }
		}
	}
	
	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.clientpackets.ClientBasePacket#getType()
	 */
	public String getType()
	{
		return _C__45_REQUESTACTIONUSE;
	}
}
