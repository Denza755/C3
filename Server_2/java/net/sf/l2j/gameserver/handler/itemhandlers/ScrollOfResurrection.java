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

import net.sf.l2j.gameserver.ClientScheduler;
import net.sf.l2j.gameserver.SkillTable;
import net.sf.l2j.gameserver.handler.IItemHandler;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2PcInstance;
import net.sf.l2j.gameserver.model.L2PetInstance;
import net.sf.l2j.gameserver.model.L2PlayableInstance;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.serverpackets.MagicSkillUser;
import net.sf.l2j.gameserver.serverpackets.Revive;
import net.sf.l2j.gameserver.serverpackets.SetupGauge;
import net.sf.l2j.gameserver.serverpackets.SocialAction;
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
    // all the items ids that this handler knowns 
    private final static int[] _itemIds = { 737 }; 
    
    /* (non-Javadoc) 
     * @see net.sf.l2j.gameserver.handler.IItemHandler#useItem(net.sf.l2j.gameserver.model.L2PcInstance, net.sf.l2j.gameserver.model.L2ItemInstance) 
     */ 
	public void useItem(L2PlayableInstance playable, L2ItemInstance item)
	{
		if (!(playable instanceof L2PcInstance))
			return;
		L2PcInstance activeChar = (L2PcInstance)playable;
        //SoR Animation section 
        L2Character target= (L2Character)activeChar.getTarget(); 

        if (target==activeChar || !((target instanceof L2PcInstance) || (target instanceof L2PetInstance))) 
        { 
            activeChar.sendPacket(new SystemMessage(SystemMessage.TARGET_IS_INCORRECT));
            return; 
        } 
        if (activeChar.isDead()) 
        { 
            return; 
        } 
        if (target instanceof L2PetInstance)
        {
            ((L2PetInstance)target).stopDecay();
        }
        
        L2Skill skill = SkillTable.getInstance().getInfo(2014, 1); 
        MagicSkillUser msk = new MagicSkillUser(activeChar, 2014, 1,15000,0); 
        activeChar.sendPacket(msk); 
        activeChar.broadcastPacket(msk); 
        SetupGauge sg = new SetupGauge(0, skill.getSkillTime()); 
        activeChar.sendPacket(sg); 
        
		activeChar.removeItemFromInventory(item, 1);
        ResurrectFinalizer rf = new ResurrectFinalizer(target);
		// continue execution later
		ClientScheduler.getInstance().scheduleLow(rf, skill.getSkillTime() - 200);
    } 
    
	static class ResurrectFinalizer implements Runnable
	{
		private L2Character _target;
		
		ResurrectFinalizer(L2Character target)
		{
			_target = target;
		}
		
		public void run()
		{
			try {
		        if ( _target.isDead() )
		        {
		            //End SoE Animation section 
		            Revive revive = new Revive(_target); 
		            _target.setCurrentHpMp(0.6 * _target.getMaxHp(), 0.6 * _target.getMaxMp()); 
		            SocialAction sa = new SocialAction(_target.getObjectId(), 15); 
		            _target.broadcastPacket(sa); 
		            _target.sendPacket(sa); 
		            _target.sendPacket(revive); 
		            _target.broadcastPacket(revive); 
		        } 
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}
	}
	
    public int[] getItemIds() 
    { 
        return _itemIds; 
    } 
} 
