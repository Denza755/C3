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
import net.sf.l2j.gameserver.MapRegionTable;
import net.sf.l2j.gameserver.SkillTable;
import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.ai.CtrlIntention;
import net.sf.l2j.gameserver.handler.IItemHandler;
import net.sf.l2j.gameserver.instancemanager.CastleManager;
import net.sf.l2j.gameserver.instancemanager.ClanHallManager;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2Skill;
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
    private static int[] _itemIds = { 736, 1830, 1829, 1538, 3958, 5858, 5859};
    
    /* (non-Javadoc)
     * @see net.sf.l2j.gameserver.handler.IItemHandler#useItem(net.sf.l2j.gameserver.model.L2PcInstance, net.sf.l2j.gameserver.model.L2ItemInstance)
     */
    public void useItem(L2PlayableInstance playable, L2ItemInstance item)
    {
        if (!(playable instanceof L2PcInstance)) return;
        L2PcInstance activeChar = (L2PcInstance)playable;
        
        if (activeChar.isMovementDisabled() || activeChar.isMuted() || activeChar.isAlikeDead() || activeChar.isAllSkillsDisabled()) 
            return;
        
        if (activeChar.isSitting())
        {
            activeChar.sendPacket(new SystemMessage(SystemMessage.CANT_MOVE_SITTING));
            return;
        }
        
        // Check to see if the player is in a festival.
        if (activeChar.isFestivalParticipant()) 
        {
            activeChar.sendPacket(SystemMessage.sendString("You may not use an escape skill in a festival."));
            return;
        }
        
        // Check to see if player is in jail
        if (activeChar.isInJail())
        {
            activeChar.sendPacket(SystemMessage.sendString("You can not escape from jail."));
            return;
        }
        
	//activeChar.abortCast();
        activeChar.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
        //SoE Animation section
        activeChar.setTarget(activeChar);
        
        // Modified by Tempy - 28 Jul 05 \\
        // Check if this is a blessed scroll, if it is then shorten the cast time.
        int itemId = item.getItemId();
        int escapeSkill = (itemId == 1538 || itemId == 5858 || itemId == 5859 || itemId == 3958) ? 2036 : 2013;
        
        if (!activeChar.destroyItem("Consume", item.getObjectId(), 1, null, false))
            return;

        activeChar.disableAllSkills();

        L2Skill skill = SkillTable.getInstance().getInfo(escapeSkill, 1);
        MagicSkillUser msu = new MagicSkillUser(activeChar, escapeSkill, 1, skill.getSkillTime(), 0);
        activeChar.broadcastPacket(msu);
        SetupGauge sg = new SetupGauge(0, skill.getSkillTime());
        activeChar.sendPacket(sg);
        //End SoE Animation section
        
        SystemMessage sm = new SystemMessage(SystemMessage.S1_DISAPPEARED);
        sm.addItemName(itemId);
        activeChar.sendPacket(sm);

        EscapeFinalizer ef = new EscapeFinalizer(activeChar, itemId);
        // continue execution later
        activeChar.setSkillCast(ThreadPoolManager.getInstance().scheduleEffect(ef, skill.getSkillTime()));
        activeChar.setSkillCastEndTime(10+GameTimeController.getGameTicks()+skill.getSkillTime()/GameTimeController.MILLIS_IN_TICK);
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
            
            try 
            {
                if ((_itemId == 1830 || _itemId == 5859) && CastleManager.getInstance().getCastleByOwner(_activeChar.getClan()) != null) // escape to castle if own's one
                { 
                  _activeChar.teleToLocation(MapRegionTable.TeleportWhereType.Castle); 
                }
                else if ((_itemId == 1829 || _itemId == 5858) && _activeChar.getClan() != null && ClanHallManager.getInstance().getClanHallByOwner(_activeChar.getClan()) != null) // escape to clan hall if own's one
                { 
                  _activeChar.teleToLocation(MapRegionTable.TeleportWhereType.ClanHall); 
                }
            } 
            catch (Throwable e) 
            {
                if (Config.DEBUG) e.printStackTrace();
            }
        }
    }
    
    public int[] getItemIds()
    {
        return _itemIds;
    }
}
