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

import net.sf.l2j.gameserver.SkillTable;
import net.sf.l2j.gameserver.handler.IItemHandler;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2Party;
import net.sf.l2j.gameserver.model.L2PcInstance;
import net.sf.l2j.gameserver.model.L2PetInstance;
import net.sf.l2j.gameserver.model.L2PlayableInstance;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.serverpackets.MagicSkillUser;
import net.sf.l2j.gameserver.serverpackets.SetupGauge;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;
//import net.sf.l2j.gameserver.model.L2ItemInstance; 
//import net.sf.l2j.gameserver.serverpackets.StopMove; 
//import net.sf.l2j.gameserver.serverpackets.TeleportToLocation; 

/** 
 * This class ... 
 * 
 * @version $Revision: 1.1.2.2.2.7 $ $Date: 2005/04/05 19:41:13 $ 
 */ 

public class ScrollOfResurrection implements IItemHandler 
{ 
    // all the items ids that this handler knows 
	// Modified by Tempy: Added blessed scroll support
    private final static int[] _itemIds = { 737, 3936, 3959, 6387 }; 
    
    /* (non-Javadoc) 
     * @see net.sf.l2j.gameserver.handler.IItemHandler#useItem(net.sf.l2j.gameserver.model.L2PcInstance, net.sf.l2j.gameserver.model.L2ItemInstance) 
     */ 
	public void useItem(L2PlayableInstance playable, L2ItemInstance item)
	{
        int itemId = item.getItemId();
        boolean blessedScroll = (itemId != 737);
        boolean petScroll = (itemId == 6387 || itemId == 737);
        boolean humanScroll = (itemId == 3936 || itemId == 737);

        if (!(playable instanceof L2PcInstance))
            return;
        L2PcInstance activeChar = (L2PcInstance)playable;
        if (activeChar.isMovementDisabled())
            return;

        // SoR Animation section 
        L2Character target= (L2Character)activeChar.getTarget(); 

        if (target==activeChar || !((target instanceof L2PcInstance) || (target instanceof L2PetInstance))) 
        { 
            activeChar.sendPacket(new SystemMessage(SystemMessage.TARGET_IS_INCORRECT));
            return; 
        } 
        if (activeChar.isDead() || !target.isDead()) 
        { 
            return; 
        } 

        if (petScroll && target instanceof L2PetInstance)
        {
            L2PetInstance targetPet = (L2PetInstance)target;
            targetPet.stopDecay();
			targetPet.setCurrentFed(targetPet.getMaxFed()/10);
        }
        else if ((humanScroll || blessedScroll) && target instanceof L2PcInstance) {
            // Check to see if this player is in party.
            L2PcInstance targetChar = (L2PcInstance)target;
            L2Party charParty = activeChar.getParty();
            L2Party targetParty = targetChar.getParty();
            if( charParty == null || targetParty == null || charParty.getPartyLeaderOID() != targetParty.getPartyLeaderOID() )
            {
                activeChar.sendPacket(new SystemMessage(SystemMessage.TARGET_IS_INCORRECT));
                return;
            }
            
            // Check to see if the player is in a festival.
            if (targetChar.isFestivalParticipant()) 
            {
                activeChar.sendPacket(SystemMessage.sendString("You may not resurrect participants in a festival."));
                return;
            }
        }
        else
        {
            activeChar.sendPacket(new SystemMessage(SystemMessage.TARGET_IS_INCORRECT));
            return;
        }
        
        activeChar.useMagic(SkillTable.getInstance().getInfo(2014, 1), true, true);
        L2Skill skill = SkillTable.getInstance().getInfo(2014, 1); 
        activeChar.broadcastPacket(new MagicSkillUser(activeChar, 2014, 1,15000,0)); 
        activeChar.sendPacket(new SetupGauge(0, skill.getSkillTime())); 
        
        /*
         * TODO: Allow blessed pet res scrolls to restore XP.
         * Meanwhile, a blessed scroll should restore full XP lost on use.
         */ 
        if (blessedScroll && (target instanceof L2PcInstance))
        	((L2PcInstance)target).restoreExp();
         
		activeChar.removeItemFromInventory(item, 1);
        // ResurrectFinalizer rf = new ResurrectFinalizer(target);
	// continue execution later
	// ClientScheduler.getInstance().scheduleLow(rf, skill.getSkillTime() - 200);
        // activeChar.sendPacket(SystemMessage.sendString("Resurrecting complete."));

    } 
    
	
    public int[] getItemIds() 
    { 
        return _itemIds; 
    } 
} 
