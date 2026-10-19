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
package net.sf.l2j.gameserver.handler.skillhandlers;

import net.sf.l2j.gameserver.handler.ISkillHandler;
import net.sf.l2j.gameserver.model.*;
import net.sf.l2j.gameserver.model.L2Skill.SkillType;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.packets.serverpackets.InventoryUpdate;
import net.sf.l2j.gameserver.packets.serverpackets.SystemMessage;

/** 
 * @author _drunk_ 
 * 
 * TODO To change the template for this generated type comment go to 
 * Window - Preferences - Java - Code Style - Code Templates 
 */ 
public class Sweep implements ISkillHandler 
{ 
    protected SkillType[] _skillIds = {SkillType.SWEEP}; 
    
    public void useSkill(L2Character activeChar, @SuppressWarnings("unused") L2Skill skill, L2Object[] targets) 
    { 
        if (!(activeChar instanceof L2PcInstance))
        {
            return;
        }
        
        L2PcInstance player = (L2PcInstance)activeChar;
		InventoryUpdate iu = new InventoryUpdate();
		boolean send = false;
		
        for(int index = 0;index < targets.length;index++) 
        { 
            if (!(targets[index] instanceof L2Attackable))
                continue;
            
	        L2Attackable target = (L2Attackable)targets[index];
            
            if (target.isSweepActive())
            {
				L2ItemInstance[] items = target.takeSweep();
				if (items == null || items.length == 0)
					continue;
				for (L2ItemInstance item : items)
				{
					if (player.isInParty() && player.getParty().isDistributeSpoilLoot())
					{
						player.getParty().distributeItem(player, item);
					}
					else
					{
						int itemCount = item.getCount();
						item = player.getInventory().addItem(item);
						if (item.getLastChange() == L2ItemInstance.ADDED) {
							iu.addNewItem(item);
						} else {
							iu.addModifiedItem(item);
						}
						send = true;
						SystemMessage smsg = new SystemMessage(SystemMessage.YOU_PICKED_UP_S1_S2); // you picked up $s1$s2
						smsg.addNumber(itemCount);
						smsg.addItemName(item.getItemId());
						player.sendPacket(smsg);
						if (player.isInParty())
						{
							smsg = new SystemMessage(SystemMessage.S1_SWEEPED_UP_S2_S3); // $1 obtained $s2 $s3(s) by using Sweeper.
							smsg.addString(player.getName());
							smsg.addNumber(itemCount);
							smsg.addItemName(item.getItemId());
							player.getParty().broadcastToPartyMembers(player, smsg);
						}
					}
				}
            }
            target.endDecayTask();
        }
		
		if (send)
			player.sendPacket(iu);
    } 
    
    public SkillType[] getSkillIds() 
    { 
        return _skillIds; 
    } 
}
