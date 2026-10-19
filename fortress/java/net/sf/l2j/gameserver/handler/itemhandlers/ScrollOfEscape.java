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
import net.sf.l2j.gameserver.MapRegionTable;
import net.sf.l2j.gameserver.SkillTable;
import net.sf.l2j.gameserver.ai.CtrlIntention;
import net.sf.l2j.gameserver.handler.IItemHandler;
import net.sf.l2j.gameserver.model.L2Clan;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2PcInstance;
import net.sf.l2j.gameserver.model.L2PlayableInstance;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.serverpackets.MagicSkillUser;
import net.sf.l2j.gameserver.serverpackets.SetupGauge;
import net.sf.l2j.gameserver.serverpackets.StopMove;
import net.sf.l2j.gameserver.instancemanager.TownManager;
import net.sf.l2j.gameserver.model.Location;

/**
 * This class ...
 * 
 */

public class ScrollOfEscape implements IItemHandler
{
	// all the items ids that this handler knowns
	private static int[] _itemIds = { 736, 1830, 1829, 1538, 3958, 5858, 5859,
	                                  7117,7118,7119,7120,7121,7122,7123,7124,
	                                  7125,7126,7127,7128,7129,7130,7132,7133,7134,7135,
                                      7554,7555,7556,7557,7558,7559};
    
    private static int[][] _scrollstotown ={
        {7117,2},   //Talking Island
        {7118,3},   //Elven Village
        {7119,1},   //Dark Elven Village
        {7120,4},   //Orc Village
        {7121,6},   //Dwarven Village
        {7122,5},   //Gludin Village
        {7123,7},   //Gludio Castle Town
        {7124,8},   //Dion Castle Town
        {7125,16},  //Floran
        {7126,9},   //Giran Castle Town
        {7127,0},   //Hardin's Private Academy
        {7128,15},  //Heine
        {7129,10},  //Oren Castle Town
        {7130,17},  //Ivory Tower
        {7131,11},  //Hunters Village
        {7132,12},  //Aden Castle Town
        {7133,13},  //Goddard Castle Town
        {7134,14},  //Rune Castle Town
        {7135,0},   //Schuttgart Castle Town (???)
        {7554,2},   //Talking Island (Quest)
        {7555,3},   //Elven Village (Quest)
        {7556,1},   //Dark Elven Village (Quest)
        {7557,4},   //Orc Village (Quest)
        {7558,6},   //Dwarven Village (Quest)
        {7559,9}    //Giran Castle Town (Quest)
        };	
    
	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.handler.IItemHandler#useItem(net.sf.l2j.gameserver.model.L2PcInstance, net.sf.l2j.gameserver.model.L2ItemInstance)
	 */
    
	public void useItem(L2PlayableInstance playable, L2ItemInstance item)
	{
		if (!(playable instanceof L2PcInstance))
			return;

		L2PcInstance activeChar = (L2PcInstance)playable;
		
		if (activeChar.isMovementDisabled() || activeChar.isMuted() || activeChar.isAlikeDead())
			return;
		
		// Check to see if the player is in a festival.
		if (activeChar.isFestivalParticipant()) 
		{
			activeChar.sendMessage("You may not use an escape skill in a festival.");
			return;
		}
		
		if(activeChar.hasAI())
			activeChar.setIntention(CtrlIntention.AI_INTENTION_IDLE, null, null);
		//SoE Animation section
		activeChar.setTarget(activeChar);
		

		// Modified by Tempy - 28 Jul 05 \\
		// Check if this is a blessed scroll, if it is then shorten the cast time.
		int itemId = item.getItemId();
		int escapeSkill = (itemId == 1538 || itemId == 5858 || itemId == 5859 || itemId == 3958) ? 2036 : 2013;
		
		L2Skill skill = SkillTable.getInstance().getInfo(escapeSkill, 1);

		activeChar.disableAllSkills(skill.getSkillTime());

		activeChar.broadcastPacket(new MagicSkillUser(activeChar, escapeSkill, 1, skill.getSkillTime(), 0));
		activeChar.sendPacket(new SetupGauge(0, skill.getSkillTime()));
		//End SoE Animation section
		
		activeChar.removeItemFromInventory(item, 1);
		// continue execution later
		ClientScheduler.getInstance().scheduleLow(new EscapeFinalizer(activeChar, item.getItemId()), skill.getSkillTime());
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
			if (_activeChar.isDead()) 
				return; 
			_activeChar.enableAllSkills();
			try {
				L2Clan playersClan = _activeChar.getClan();
                for(int i=0;i<_scrollstotown.length;i++)
                    if(_itemId==_scrollstotown[i][0] && _scrollstotown[i][1]!=0)
                    {
                        int[] temp = TownManager.getInstance().getTown(_scrollstotown[i][1]).getSpawn().get(0);
                        Location loc = new Location(temp[0],temp[1],temp[4]);
                        MapRegionTable.getInstance().teleToLocation(_activeChar, loc);
                        return;
                    }
				if ((playersClan == null)||(_itemId != 1830))
				{
					escapeToTown();
				}
				else if (playersClan.getHasCastle() > 0) // escape to castle if has a castle
				{
					escapeToCastle(playersClan.getHasCastle()-1);
				}
				else // clan but no castle
				{
					escapeToTown();
				}
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}
		
		private void escapeToCastle(int castleNum)
		{
			_activeChar.broadcastPacket(new StopMove(_activeChar));
			
            _activeChar.sendPacket(new ActionFailed());
			_activeChar.teleToLocation(L2World.castleownerspawns[castleNum][0], 
			L2World.castleownerspawns[castleNum][1],
			L2World.castleownerspawns[castleNum][2]);
		}
		
		private void escapeToTown()
		{
            MapRegionTable.getInstance().teleToLocation(_activeChar, MapRegionTable.TeleportWhereType.ClosestTown);
		}
		
	}
	
	public int[] getItemIds()
	{
		return _itemIds;
	}
}
