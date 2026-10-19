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

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.ClientThread;
import net.sf.l2j.gameserver.ai.CtrlIntention;
import net.sf.l2j.gameserver.model.*;
import net.sf.l2j.gameserver.model.actor.instance.L2DoorInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2StaticObjectInstance;
import net.sf.l2j.gameserver.packets.serverpackets.*;

import java.nio.ByteBuffer;
import java.util.Map;
import java.util.logging.Logger;
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
	private static final int[] CASTLE_THRONE = {0, 19210010, 20220010, 23220010, 22190010, 24180017, 23250010};
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
		L2PcInstance activeChar = getClient().getActiveChar();
		L2Summon pet = activeChar.getPet();
        if(activeChar == null)
            return;
		if (Config.DEBUG)
			_log.finest(activeChar.getName()+" request Action use: id "+_actionId + " 2:" + _ctrlPressed + " 3:"+_shiftPressed);
        
		// dont do anything if player is dead
		if (activeChar.isAlikeDead())
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
                if (activeChar.getTarget() != null && !activeChar.isSitting()
                        && activeChar.getTarget() instanceof L2StaticObjectInstance
                        && ((L2StaticObjectInstance)activeChar.getTarget()).getType() == 1
                        && activeChar.getClan() != null
                        && activeChar.isClanLeader())
                {
                    //ChairSit cs = new ChairSit(activeChar,((L2StaticObjectInstance)activeChar.getTarget()).getStaticObjectId());
                    //activeChar.sendPacket(cs);
                    //activeChar.broadcastPacket(cs);
                    //break;
                    if(((L2StaticObjectInstance)activeChar.getTarget()).getStaticObjectId() == CASTLE_THRONE[activeChar.getClan().getHasCastle()]) {
                        double distance = activeChar.getDistance(activeChar.getTarget().getX(), activeChar.getTarget().getY());
                        if(distance <= 30) {
                            ChairSit cs = new ChairSit(activeChar,((L2StaticObjectInstance)activeChar.getTarget()).getStaticObjectId());
                            activeChar.sendPacket(cs);
                            activeChar.broadcastPacket(cs);
                            activeChar.sitDown2();
                            break;
                        }
                    }
                }
                
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
                    if (
                            activeChar.getAccessLevel() < Config.GM_PEACEATTACK &&
                            activeChar.isInsidePeaceZone(activeChar.getPet(), target)
                       )
                    {
                        activeChar.sendPacket(new SystemMessage(SystemMessage.TARGET_IN_PEACEZONE));
                        return;
                    }

                    if (target.isAutoAttackable(activeChar) || _ctrlPressed)
                    {
                        //activeChar.getPet().startAttack((L2Character)activeChar.getTarget());
                        if ((activeChar.getPet().getNpcId() != 12251) || (activeChar.getTarget() instanceof L2DoorInstance))
                            activeChar.getPet().getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, activeChar.getTarget());
                    }
				}
				break;
			}
            case 23:
			case 17:
			{
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
				if (pet == null) return;

				if (pet.isDead())
					activeChar.sendPacket(new SystemMessage(SystemMessage.DEAD_PET_CANNOT_BE_RETURNED));
				else
					pet.unSummon(activeChar);

				break;
			}
			case 20:
			{
                _log.warning("unhandled action type 20");
				break;
			}
			case 38:
			{
                if(pet != null && pet.isMountable() && !activeChar.isMounted()) 
                {
                    if(activeChar.isDead())
                        activeChar.sendPacket(new SystemMessage(SystemMessage.STRIDER_CANT_BE_RIDDEN_WHILE_DEAD));
                    else if(pet.isDead())
                        activeChar.sendPacket(new SystemMessage(SystemMessage.DEAD_STRIDER_CANT_BE_RIDDEN));
                    else if(pet.isInCombat())
                        activeChar.sendPacket(new SystemMessage(SystemMessage.STRIDER_IN_BATLLE_CANT_BE_RIDDEN));
                    else if(activeChar.isInCombat())
                        activeChar.sendPacket(new SystemMessage(SystemMessage.STRIDER_CANT_BE_RIDDEN_WHILE_IN_BATTLE));                        
                    else if(activeChar.isSitting() || activeChar.isMoving())
                        activeChar.sendPacket(new SystemMessage(SystemMessage.STRIDER_CAN_BE_RIDDEN_ONLY_WHILE_STANDING));
                    else if(!pet.isDead() && !activeChar.isMounted())
                    {
                        Ride mount = new Ride(activeChar.getObjectId(), Ride.ACTION_MOUNT, pet.getTemplate().npcId);
                        activeChar.broadcastPacket(mount);
                        activeChar.setMountType(mount.getMountType());
                        pet.unSummon(activeChar);
                    }
                }
                else if(activeChar.isMounted())
                {
                    activeChar.broadcastPacket(new Ride(activeChar.getObjectId(), Ride.ACTION_DISMOUNT, 0));
                    activeChar.setMountType(0);
                }
                break;
			}
            case 32: //Wild Hog Cannon - Mode Change
            {
                /*L2Object target = activeChar.getTarget();
                if (target != null && pet != null && pet != target && activeChar != target)
                {
                    L2Skill skill = SkillTable.getInstance().getInfo(1259,1);
                    UseSkill(activeChar, pet, target, skill);
                }*/
                break;
            }
            case 36: //Toxic Smoke
            {
                UseSkill(4259);
                break;
            }
            case 37:
            {
            	if (activeChar.isAlikeDead())
                {
                    sendPacket(new ActionFailed());
                    return;
                }
            	
            	if (activeChar.isSitting())
            		activeChar.standUp();
                
                if (activeChar.getCreateList() == null)
                {
                    activeChar.setCreateList(new L2ManufactureList());
                }
        		
        		activeChar.sendPacket(new RecipeShopManageList(activeChar, true));
        		break;
            }
            case 39: //Parasite Burst
            {
                UseSkill(4138);
                break;
            }
            case 41: //Wild Hog Cannon - Attack
            {
                UseSkill(4230);
                break;
            }
            case 42: //Self Damage Shield
            {
                UseSkill(4378);
                break;
            }
            case 43: //Whirpool
            {
                UseSkill(4137);
                break;
            }
            case 44: //Boom Attack
            {
                UseSkill(4139);
                break;
            }
            case 45: //Master Recharge
            {
                UseSkill(4025, activeChar);
                break;
            }
            case 46: //Mega Storm Strike
            {
                UseSkill(4261);
                break;
            }
            case 47: //Steal Blood
            {
                UseSkill(4260);
                break;
            }
            case 48: //Mech. Cannon
            {
                UseSkill(4068);
                break;
            }
            case 49: //Wind Hatchling
            {
                UseSkill(4032);
                break;
            }
            case 1002: //Star Hatchling
            {
                UseSkill(4123);
                break;
            }
            case 51:
            {
                // Player shouldn't be able to set stores if he/she is alike dead (dead or fake death)
                if (activeChar.isAlikeDead())
                {
                	activeChar.sendMessage("Cannot use recipe book while trading");
                }
                
                if (activeChar.isSitting())
            		activeChar.standUp();
                
                if (activeChar.getCreateList() == null)
                {
                    activeChar.setCreateList(new L2ManufactureList());
                }
        		
        		activeChar.sendPacket(new RecipeShopManageList(activeChar, false));
        		break;
            }    
            case 52: // unsummon
            {
            	activeChar.getPet().unSummon(activeChar);
            	break;
            }
            case 53: // move to target
            {
            	if(activeChar.getTarget() != null)
            	{
            		L2Object target = activeChar.getTarget();
            		activeChar.getPet().getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO,
            		                                         new L2CharPosition(target.getX(),target.getY(), target.getZ(), 0 ));
            	}
            	break;
            }
            case 1000: //Siege Hammer
            {
                UseSkill(4079);
                break;
            }
            default:
            {
                _log.warning(activeChar.getName()+": unhandled action type "+_actionId);
            }
		}
	}

	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.clientpackets.ClientBasePacket#getType()
	 */
    
    private void UseSkill(int skillId, L2Object target)
    {
        L2PcInstance activeChar = getClient().getActiveChar();
        L2Summon pet = activeChar.getPet();
        
        if (activeChar.getPrivateStoreType() != 0)
        {
        	activeChar.sendMessage("Cannot use skill while trading");
        }
        
        if (target != null && pet != null && pet != target)
        {
            L2Skill skill;

            Map<Integer, L2Skill> _skills = pet.getTemplate().getSkills();
            if(_skills.size() == 0)
            {
                //activeChar.sendPacket(new SystemMessage(SystemMessage.SKILL_NOT_AVAILABLE));
                return;
            }
            skill = _skills.get(skillId);
            if(skill == null)
            {
                return;
            }
            if(pet.getCurrentMp() < skill.getMpConsume())
            {
                activeChar.sendPacket(new SystemMessage(SystemMessage.NOT_ENOUGH_MP));
                return;
            }
            if (
                    activeChar.getAccessLevel() < Config.GM_PEACEATTACK &&
                    activeChar.isInsidePeaceZone(activeChar.getPet(), target)
               )
            {
                activeChar.sendPacket(new SystemMessage(SystemMessage.TARGET_IN_PEACEZONE));
                return;
            }
            pet.setTarget(target);
            pet.useMagic(skill);
        }
    }
        
    
    private void UseSkill(int skillId)
    {
        L2PcInstance activeChar = getClient().getActiveChar();
        L2Object target = activeChar.getTarget();
	if (activeChar == null || target == null)
	    return;

        if (activeChar.getPrivateStoreType() != 0)
        {
        	activeChar.sendMessage("Cannot use skill while trading");
        }

        if (target != null && activeChar != target)
        {
            if (target.isAutoAttackable(activeChar) || _ctrlPressed)
            {
                UseSkill(skillId, target);
            }
        }
    }
    
	public String getType()
	{
		return _C__45_REQUESTACTIONUSE;
	}
}
