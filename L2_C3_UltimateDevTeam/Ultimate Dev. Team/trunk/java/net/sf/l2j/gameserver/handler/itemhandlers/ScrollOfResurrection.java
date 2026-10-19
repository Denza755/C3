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

import net.sf.l2j.gameserver.handler.IItemHandler;
import net.sf.l2j.gameserver.instancemanager.CastleManager;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PetInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PlayableInstance;
import net.sf.l2j.gameserver.model.entity.Castle;
import net.sf.l2j.gameserver.packets.serverpackets.MagicSkillUser;
import net.sf.l2j.gameserver.packets.serverpackets.SetupGauge;
import net.sf.l2j.gameserver.packets.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.tables.SkillTable;

/** 
 * This class ... 
 * 
 * @version $Revision: 1.1.2.2.2.7 $ $Date: 2005/04/05 19:41:13 $ 
 */ 

public class ScrollOfResurrection implements IItemHandler 
{ 
    // all the items ids that this handler knows 
    private final static int[] _itemIds = { 737, 3936, 3959, 6387 }; 
    
    /* (non-Javadoc) 
     * @see net.sf.l2j.gameserver.handler.IItemHandler#useItem(net.sf.l2j.gameserver.model.L2PcInstance, net.sf.l2j.gameserver.model.L2ItemInstance) 
     */ 
	public void useItem(L2PlayableInstance playable, L2ItemInstance item)
	{
        if (!(playable instanceof L2PcInstance)) return;
        
        L2PcInstance activeChar = (L2PcInstance)playable;
        if (activeChar.isMovementDisabled()) return;

        int itemId = item.getItemId();
        boolean blessedScroll = (itemId != 737);
        boolean humanScroll = (itemId == 3936 || itemId == 737);
        boolean petScroll = (itemId == 6387 || itemId == 737);

        // SoR Animation section 
        L2Character target = (L2Character)activeChar.getTarget(); 

        if(target != null && target.isDead())
        {
            L2PcInstance targetPlayer = null;
            if (target instanceof L2PcInstance) targetPlayer = (L2PcInstance)target;
            L2PetInstance targetPet = null;
            if (target instanceof L2PetInstance) targetPet = (L2PetInstance)target;
            if(targetPlayer != null || targetPet != null)
            {
                boolean condGood = true;
                
                //check target is not in a active siege zone
                Castle castle = null;

                if(targetPlayer != null)
                {
                	castle = CastleManager.getInstance().getCastle(targetPlayer.getX(),targetPlayer.getY());
                }
                else
                {
                	castle = CastleManager.getInstance().getCastle(targetPet.getX(),targetPet.getY());
                }
            	
            	if (castle != null)
            		if (castle.getSiege().getIsInProgress())
            		{	
                        condGood = false;
                        activeChar.sendMessage("You Cannot Resurect in a Sieged Zone");
            		}
                
                if (targetPet != null)
                {
                    if (targetPet.getOwner() != activeChar)
                    {
                        condGood = false;
                        activeChar.sendMessage("You are not the owner of this pet");
                    }
                    else if (!petScroll)
                    {
                        condGood = false;
                        activeChar.sendMessage("You are not the correct scroll");
                    }
                }
                else
                {
                    if (targetPlayer.isFestivalParticipant()) // Check to see if the current player target is in a festival.
                    {
                        condGood = false;
                        activeChar.sendPacket(SystemMessage.sendString("You may not resurrect participants in a festival."));
                    }
                    // Can only res party memeber or own pet
                    else if (activeChar.getParty() == null || targetPlayer.getParty() == null || activeChar.getParty().getPartyLeaderOID() != targetPlayer.getParty().getPartyLeaderOID())
                        condGood = false;
                    else if (!humanScroll)
                    {
                        condGood = false;
                        activeChar.sendMessage("You are not the correct scroll");
                    }
                }
                
                if (condGood)
                {
                    /*
                     * TODO: Allow blessed pet resurrection scrolls to restore XP.
                     * Meanwhile, a blessed scroll should restore all the XP lost.
                     */
                    if (blessedScroll && targetPlayer != null)
                    	targetPlayer.restoreExp(100.0);

                    activeChar.useMagic(SkillTable.getInstance().getInfo(2014, 1), true, true);
                    L2Skill skill = SkillTable.getInstance().getInfo(2014, 1); 
                    activeChar.broadcastPacket(new MagicSkillUser(activeChar, 2014, 1,15000,0)); 
                    activeChar.sendPacket(new SetupGauge(0, skill.getSkillTime())); 
                    
                    activeChar.removeItemFromInventory(item, 1);
                }
            }
        }
        activeChar.sendPacket(new SystemMessage(SystemMessage.TARGET_IS_INCORRECT));
    } 
    	
    public int[] getItemIds() 
    { 
        return _itemIds; 
    } 
} 
