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
package net.sf.l2j.gameserver.handler.itemhandlers;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.GameTimeController;
import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.datatables.MapRegionTable;
import net.sf.l2j.gameserver.datatables.SkillTable;
import net.sf.l2j.gameserver.ai.CtrlIntention;
import net.sf.l2j.gameserver.handler.IItemHandler;
import net.sf.l2j.gameserver.instancemanager.CastleManager;
import net.sf.l2j.gameserver.instancemanager.ClanHallManager;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PlayableInstance;
import net.sf.l2j.gameserver.serverpackets.MagicSkillUser;
import net.sf.l2j.gameserver.serverpackets.SetupGauge;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;

/**
 * This class ...
 *
 * @version $Revision: 1.2.2.3.2.5 $ $Date: 2005/03/27 15:30:07 $
 */

public class ScrollOfEscape implements IItemHandler
{
    // all the items ids that this handler knowns
    private static int[] _itemIds = { 736, 1830, 1829, 1538, 3958, 5858, 5859 };

    /* (non-Javadoc)
     * @see net.sf.l2j.gameserver.handler.IItemHandler#useItem(net.sf.l2j.gameserver.model.L2PcInstance, net.sf.l2j.gameserver.model.L2ItemInstance)
     */
    /* (non-Javadoc)
     * @see com.l2jfrozen.gameserver.handler.IItemHandler#useItem(com.l2jfrozen.gameserver.model.L2PcInstance, com.l2jfrozen.gameserver.model.L2ItemInstance)
     */
    @Override
    public void useItem(L2PlayableInstance playable, L2ItemInstance item)
    {
        if(!(playable instanceof L2PcInstance))
            return;

        L2PcInstance activeChar = (L2PcInstance) playable;

        if(checkConditions(activeChar))
            return;

        //Check to see if player is sitting
        if(activeChar.isSitting())
        {
            activeChar.sendPacket(new SystemMessage(SystemMessage.CANT_MOVE_SITTING));
            return;
        }

        //not usefull
        /*if(GrandBossManager.getInstance().getZone(activeChar) != null && !activeChar.isGM())
        {
            activeChar.sendMessage("You Can't Use SOE In Grand boss zone!");
            return;
        }*/


        if(activeChar.getPvpFlag() != 0)
        {
            activeChar.sendMessage("You Can't Use SOE In PvP!");
            return;
        }

        // Check to see if the player is in a festival.
        if(activeChar.isFestivalParticipant())
        {
            activeChar.sendPacket(SystemMessage.sendString("You may not use an escape skill in a festival."));
            return;
        }

        // Check to see if player is in jail
        if(activeChar.isInJail())
        {
            activeChar.sendPacket(SystemMessage.sendString("You can not escape from jail."));
            return;
        }

        if(activeChar.isParalyzed())
        {
            activeChar.sendPacket(SystemMessage.sendString("You may not use an escape skill in a paralyzed."));
            return;
        }

        //activeChar.abortCast();
        activeChar.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
        //SoE Animation section
        // Check if this is a blessed scroll, if it is then shorten the cast time.
        int itemId = item.getItemId();

        SystemMessage sm3 = new SystemMessage(SystemMessage.USE_S1);
        sm3.addItemName(itemId);
        activeChar.sendPacket(sm3);

        int escapeSkill = itemId == 1538 || itemId == 5858 || itemId == 5859 || itemId == 3958 ? 2036 : 2013;

        if(!activeChar.destroyItem("Consume", item.getObjectId(), 1, null, false))
           return;

        activeChar.disableAllSkills();

        //fix soe
        L2Object oldtarget = activeChar.getTarget();
        activeChar.setTarget(activeChar);

        L2Skill skill = SkillTable.getInstance().getInfo(escapeSkill, 1);
        MagicSkillUser msu = new MagicSkillUser(activeChar, escapeSkill, 1, skill.getHitTime(), 0);
        activeChar.broadcastPacket(msu);
        activeChar.setTarget(oldtarget);
        SetupGauge sg = new SetupGauge(0, skill.getHitTime());
        activeChar.sendPacket(sg);
        oldtarget = null;
        sg = null;
        //End SoE Animation section
        activeChar.setTarget(null);

        SystemMessage sm = new SystemMessage(SystemMessage.S1_DISAPPEARED);
        sm.addItemName(itemId);
        activeChar.sendPacket(sm);
        sm = null;

        EscapeFinalizer ef = new EscapeFinalizer(activeChar, itemId);
        // continue execution later
        activeChar.setSkillCast(ThreadPoolManager.getInstance().scheduleEffect(ef, skill.getHitTime()));
        activeChar.setSkillCastEndTime(10 + GameTimeController.getGameTicks() + skill.getHitTime() / GameTimeController.MILLIS_IN_TICK);

        ef = null;
        activeChar = null;
    }


    static class EscapeFinalizer implements Runnable
    {
        private L2PcInstance _activeChar;
        private int _itemId;

        EscapeFinalizer(L2PcInstance activeChar, int itemId)
        {
            _activeChar = activeChar;
            _itemId = itemId;
        }

        public void run()
        {
        	if (_activeChar.isDead()) return;
        	_activeChar.enableAllSkills();

            _activeChar.setIsIn7sDungeon(false);

            try {
                if ((_itemId == 1830 || _itemId == 5859) && CastleManager.getInstance().getCastleByOwner(_activeChar.getClan()) != null) // escape to castle if own's one
                { _activeChar.teleToLocation(MapRegionTable.TeleportWhereType.Castle); }
                else if ((_itemId == 1829 || _itemId == 5858) && _activeChar.getClan() != null && ClanHallManager.getInstance().getClanHallByOwner(_activeChar.getClan()) != null) // escape to clan hall if own's one
                {
                	_activeChar.teleToLocation(MapRegionTable.TeleportWhereType.ClanHall);
                }
                else if(_itemId == 5858) // do nothing
                {
                	_activeChar.sendPacket(new SystemMessage(SystemMessage.CLAN_HAS_NO_CLAN_HALL));
                    return;
                }
                else if(_itemId == 5859) // do nothing
                {
                	_activeChar.sendPacket(SystemMessage.sendString("Your clan does not own a castle."));
                    return;
                }
                else
                {
                	if(_itemId < 7117)
                		_activeChar.teleToLocation(MapRegionTable.TeleportWhereType.Town);
                	else
                	{
                		switch(_itemId)
                		{
                			case 7117 :
                				_activeChar.teleToLocation(-84318,244579,-3730); // Talking Island
                				break;
                			case 7554 :
                				_activeChar.teleToLocation(-84318,244579,-3730); // Talking Island quest scroll
                				break;
                			case 7118 :
                				_activeChar.teleToLocation(46934,51467,-2977);   // Elven Village
                				break;
                			case 7555 :
                				_activeChar.teleToLocation(46934,51467,-2977);   // Elven Village quest scroll
                				break;
                			case 7119 :
                				_activeChar.teleToLocation(9745,15606,-4574);    // Dark Elven Village
                				break;
                			case 7556 :
                				_activeChar.teleToLocation(9745,15606,-4574);    // Dark Elven Village quest scroll
                				break;
                			case 7120 :
                				_activeChar.teleToLocation(-44836,-112524,-235);  // Orc Village
                				break;
                			case 7557 :
                				_activeChar.teleToLocation(-44836,-112524,-235);  // Orc Village quest scroll
                				break;
                			case 7121 :
                				_activeChar.teleToLocation(115113,-178212,-901);  // Dwarven Village
                				break;
                			case 7558 :
                				_activeChar.teleToLocation(115113,-178212,-901);  // Dwarven Village quest scroll
                				break;
                			case 7122 :
                				_activeChar.teleToLocation(-80826,149775,-3043);  // Gludin Village
                				break;
                			case 7123 :
                				_activeChar.teleToLocation(-12678,122776,-3116);  // Gludio Castle Town
                				break;
                			case 7124 :
                				_activeChar.teleToLocation(15670,142983,-2705);  // Dion Castle Town
                				break;
                			case 7125 :
                				_activeChar.teleToLocation(17836, 170178, -3507);  // Floran
                				break;
                			case 7126 :
                				_activeChar.teleToLocation(83400,147943,-3404);  // Giran Castle Town
                				break;
                			case 7559 :
                				_activeChar.teleToLocation(83400,147943,-3404);  // Giran Castle Town quest scroll
                				break;
                			case 7127 :
                				_activeChar.teleToLocation(105918,109759,-3207);  // Hardin's Private Academy
                				break;
                			case 7128 :
                				_activeChar.teleToLocation(111409,219364,-3545);  // Heine
                				break;
                			case 7129 :
                				_activeChar.teleToLocation(82956,53162,-1495);  // Oren Castle Town
                				break;
                			case 7130 :
                				_activeChar.teleToLocation(85348,16142,-3699);  // Ivory Tower
                				break;
                			case 7131 :
                				_activeChar.teleToLocation(116819,76994,-2714);  // Hunters Village
                				break;
                			case 7132 :
                				_activeChar.teleToLocation(146331,25762,-2018);  // Aden Castle Town
                				break;
					default:
					     _activeChar.teleToLocation(MapRegionTable.TeleportWhereType.Town);
                				break;
                		}
                	}
                }
            } catch (Throwable e) {
                if (Config.DEBUG) e.printStackTrace();
            }
        }
    }

	private static boolean checkConditions(L2PcInstance actor)
	{
		return actor.isStunned() || actor.isSleeping() || actor.isParalyzed() || actor.isFakeDeath() || actor.isTeleporting() || actor.isMuted() || actor.isAlikeDead() || actor.isAllSkillsDisabled();
	}    

    public int[] getItemIds()
    {
        return _itemIds;
    }
}
