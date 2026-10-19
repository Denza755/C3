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
import java.util.HashMap;
import java.util.logging.Logger;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.RecipeController;
import net.sf.l2j.gameserver.ClientThread;
import net.sf.l2j.gameserver.ai.CtrlIntention;
import net.sf.l2j.gameserver.model.L2DoorInstance;
import net.sf.l2j.gameserver.model.L2MonsterInstance;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2PcInstance;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2StaticObjectInstance;
import net.sf.l2j.gameserver.model.L2Summon;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.serverpackets.ChairSit;
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
		_shiftPressed = readC()==1;
	}

	void runImpl()
	{
		if (Config.DEBUG)
			_log.finest("request Action use: id "+_actionId + " 2:" + _ctrlPressed + " 3:"+_shiftPressed);
		L2PcInstance activeChar = getClient().getActiveChar();
		if(activeChar == null) return;

		// dont do anything if player is dead or confused
		if (activeChar.isAlikeDead() || activeChar.isOutOfControl())
		{
			activeChar.sendPacket(new ActionFailed());
			return;
		}

		L2Object target = activeChar.getTarget();
		L2Summon pet = activeChar.getPet();
		switch (_actionId)
		{
			case 0:
			{
                if (target != null && !activeChar.isSitting()
                        && target instanceof L2StaticObjectInstance
                        && ((L2StaticObjectInstance)target).getType() == 1
                        && activeChar.getClan() != null
                        && activeChar.getClan().getLeaderId() == activeChar.getCharId())
                {
                    activeChar.broadcastPacket(new ChairSit(activeChar,((L2StaticObjectInstance)target).getStaticObjectId()));
                    break;
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
				if(target == null || pet == null || pet == target)
					return;

				if(!(target instanceof L2MonsterInstance) && (pet.withinZones(L2World.zonePeace) || target.withinZones(L2World.zonePeace)))
				{
					activeChar.sendPacket(new SystemMessage(SystemMessage.YOU_CANNOT_ATTACK_THE_TARGET_IN_THE_PEACE_ZONE));
					return;
				}

				if(target.isAutoAttackable(activeChar) || _ctrlPressed)
					if(pet.getNpcId() != 12251 || target instanceof L2DoorInstance)
						pet.setIntention(CtrlIntention.AI_INTENTION_ATTACK, target, null);
				break;
			}
            case 23:
			case 17:
			{
				if (pet != null)
					pet.setIntention(CtrlIntention.AI_INTENTION_IDLE, null, null);
				break;
			}
			case 18:
			{
				_log.warning("unhandled action type 18");
				break;
			}
			case 19:
			{
				if (pet == null)
					return;

				if (pet.isDead())
					activeChar.sendPacket(new SystemMessage(SystemMessage.A_DEAD_PET_CANNOT_BE_SENT_BACK));
				else
					pet.unSummon(activeChar);

				break;
			}
			case 20:
			{
                _log.warning("unhandled action type 20");
				break;
			}
			case 38: //mount
			{
                if(pet != null && pet.isMountable() && !activeChar.isMounted()) 
                {
                    if(activeChar.isDead())
                        activeChar.sendPacket(new SystemMessage(SystemMessage.A_STRIDER_CANNOT_BE_RIDDEN_WHEN_DEAD));
                    else if(pet.isDead())
                        activeChar.sendPacket(new SystemMessage(SystemMessage.A_DEAD_STRIDER_CANNOT_BE_RIDDEN));
                    else if(pet.isInCombat())
                        activeChar.sendPacket(new SystemMessage(SystemMessage.A_STRIDER_IN_BATTLE_CANNOT_BE_RIDDEN));
                    else if(activeChar.isInCombat())
                        activeChar.sendPacket(new SystemMessage(SystemMessage.A_STRIDER_CANNOT_BE_RIDDEN_WHILE_IN_BATTLE));                        
                    else if(activeChar.isSitting() || activeChar.isMoving())
                        activeChar.sendPacket(new SystemMessage(SystemMessage.A_STRIDER_CAN_BE_RIDDEN_ONLY_WHEN_STANDING));
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
                L2Summon pet = activeChar.getPet();
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
                UseSkill(4378, pet);
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
            case 1000: //Siege Hammer
            {
                UseSkill(4079);
                break;
            }
            case 37:
			{
				if (activeChar.getPrivateStoreType() != 0)
		        {
					activeChar.sendMessage("Cannot use recipe book while trading");
		            return;
		        }
				RecipeController.getInstance().requestBookOpen(activeChar);
				break;
			}
            case 51:
            {
                /*if (activeChar.getPrivateStoreType() != 0)
                {
                    activeChar.sendMessage("Cannot use recipe book while trading");
                    return;
                }
                RecipeController.getInstance().requestBookOpen(activeChar);*/
                activeChar.sendMessage("Not working yet. Dwarves must use \"Dwarven Manufacture\" button.");
                break;
            }
            case 52:
            	pet.unSummon(activeChar);
            	break;
            case 53: //summon move to target

            default:
            {
                _log.warning("unhandled action type "+_actionId);
            }
		}
	}

    private void UseSkill(int skillId, L2Object target)
    {
        L2PcInstance activeChar = getClient().getActiveChar();
        L2Summon pet = activeChar.getPet();

        if (activeChar.getPrivateStoreType() != 0)
        {
            activeChar.sendPacket(new SystemMessage(SystemMessage.A_PRIVATE_STORE_MAY_NOT_BE_OPENED_WHILE_USING_A_SKILL));
            return;
        }

        if(target == null || pet == null || pet == target)
			return;

        HashMap<Integer, L2Skill> _skills = pet.getTemplate().getSkills();
        if(_skills.size() == 0)
        {
        	activeChar.sendPacket(new SystemMessage(SystemMessage.S1_IS_NOT_AVAILABLE_AT_THIS_TIME_BEING_PREPARED_FOR_REUSE));
        	return;
        }

        L2Skill skill = _skills.get(skillId);
        if(skill == null)
        	return;

        if(pet.getCurrentMp() < skill.getMpConsume())
        {
        	activeChar.sendPacket(new SystemMessage(SystemMessage.NOT_ENOUGH_MP));
        	return;
        }

		if(skill.isOffensive() && !(target instanceof L2MonsterInstance) && (pet.withinZones(L2World.zonePeace) || target.withinZones(L2World.zonePeace)))
		{
			activeChar.sendPacket(new SystemMessage(SystemMessage.YOU_CANNOT_ATTACK_THE_TARGET_IN_THE_PEACE_ZONE));
			return;
		}
		pet.setTarget(target);
		pet.useMagic(skill);
    }

    private void UseSkill(int skillId)
    {
        L2PcInstance activeChar = getClient().getActiveChar();
        L2Object target = activeChar.getTarget();
		if(activeChar == null || target == null)
	    	return;

		if(target != null && (target.isAutoAttackable(activeChar) || _ctrlPressed))
			UseSkill(skillId, target);
    }

	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.clientpackets.ClientBasePacket#getType()
	 */
	public String getType()
	{
		return _C__45_REQUESTACTIONUSE;
	}
}
