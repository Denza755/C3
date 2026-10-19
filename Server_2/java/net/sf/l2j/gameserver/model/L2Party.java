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
package net.sf.l2j.gameserver.model;

import java.util.ArrayList;
import java.util.Random;
import java.util.logging.Logger;

import net.sf.l2j.gameserver.ItemTable;
import net.sf.l2j.gameserver.serverpackets.InventoryUpdate;
import net.sf.l2j.gameserver.serverpackets.PartySmallWindowAdd;
import net.sf.l2j.gameserver.serverpackets.PartySmallWindowAll;
import net.sf.l2j.gameserver.serverpackets.PartySmallWindowDelete;
import net.sf.l2j.gameserver.serverpackets.PartySmallWindowDeleteAll;
import net.sf.l2j.gameserver.serverpackets.ServerBasePacket;
import net.sf.l2j.gameserver.serverpackets.StatusUpdate;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;

/**
 * This class ...
 * 
 * @author nuocnam
 * @version $Revision: 1.6.2.2.2.6 $ $Date: 2005/04/11 19:12:16 $
 */
public class L2Party {
	private static Random _rnd = new Random();

    private static Logger _log = Logger.getLogger(L2Party.class.getName());
    
	private ArrayList<L2PcInstance> _members = null;
	private boolean _randomLoot = false;
	private int _partyLvl = 0;
	
	/**
	 * constructor ensures party has always one member - leader
	 * @param leader
	 * @param randomLoot
	 */
	public L2Party(L2PcInstance leader, boolean randomLoot) 
    {
		_members = new ArrayList<L2PcInstance>();
		_randomLoot = randomLoot;
		_members.add(leader);
		_partyLvl = leader.getLevel();
	}
	
	/**
	 * returns number of party members
	 * @return
	 */
	public int getMemberCount() 
    {
		return _members.size();
	}

	/**
	 * returns all party members
	 * @return
	 */
	public ArrayList<L2PcInstance> getPartyMembers() 
    {
		return _members;
	}
	
	/**
	 * get random member from party
	 * @return
	 */
	private L2PcInstance getRandomMember() 
    {
		return _members.get(_rnd.nextInt(_members.size()));
	}

	/**
	 * true if player is party leader
	 * @param player
	 * @return
	 */
	public boolean isLeader(L2PcInstance player) 
    {
		return (_members.get(0).equals(player));
	}
		
	/**
	 * Broadcasts packet to every party member 
	 * @param msg
	 */
	public void broadcastToPartyMembers(ServerBasePacket msg) 
    {
		for(int i = 0; i < _members.size(); i++) 
        {
			L2PcInstance member = _members.get(i);
			member.sendPacket(msg);
		}
	}

	/**
	 * overloaded method broadcasts packet to every party member but player
	 * @param player
	 * @param msg
	 */
	public void broadcastToPartyMembers(L2PcInstance player, ServerBasePacket msg) 
    {
		for(int i = 0; i < _members.size(); i++) 
        {
			L2PcInstance member = _members.get(i);
			if (!member.equals(player)) 
            {
				member.sendPacket(msg);
			}
		}
	}
	
	/**
	 * adds new member to party
	 * @param player
	 */
	public void addPartyMember(L2PcInstance player) 
    {		
		//sends new member party window for all members
		//we do all actions before adding member to a list, this speeds things up a little
		PartySmallWindowAll window = new PartySmallWindowAll();
		window.setPartyList(_members);
		player.sendPacket(window);
		
		SystemMessage msg = new SystemMessage(SystemMessage.YOU_JOINED_S1_PARTY);
		msg.addString(_members.get(0).getName());
		player.sendPacket(msg);
		
		msg = new SystemMessage(SystemMessage.S1_JOINED_PARTY);
		msg.addString(player.getName());
		broadcastToPartyMembers(msg);
		broadcastToPartyMembers(new PartySmallWindowAdd(player));
		
		//add player to party, adjust party level
		_members.add(player);
		if (player.getLevel() > _partyLvl)
			_partyLvl = player.getLevel();
	}
	
	/**
	 * removes player from party
	 * @param player
	 */
	public void removePartyMember(L2PcInstance player) 
    {
		if (_members.contains(player)) 
        {
			_members.remove(player);
			recalculatePartyLevel();
			
			SystemMessage msg = new SystemMessage(SystemMessage.YOU_LEFT_PARTY);
			player.sendPacket(msg);
			player.sendPacket(new PartySmallWindowDeleteAll());
			player.setParty(null);
			
			msg = new SystemMessage(SystemMessage.S1_LEFT_PARTY);
			msg.addString(player.getName());
			broadcastToPartyMembers(msg);
			broadcastToPartyMembers(new PartySmallWindowDelete(player));
			
			if (_members.size() == 1) 
            {
				_members.get(0).setParty(null);
			}
		}
	}

	/**
	 * finds a player in the party by name
	 * @param name
	 * @return
	 */
	private L2PcInstance getPlayerByName(String name) 
    {
		for(int i = 0; i < _members.size(); i++) 
        {
			L2PcInstance temp = _members.get(i);
			if (temp.getName().equals(name)) return temp;
		}
		return null;
	}

	/**
	 * Oust player from party
	 * @param player
	 */
	public void oustPartyMember(L2PcInstance player) 
    {
		if (_members.contains(player)) 
        {
			if (isLeader(player)) 
            {
				dissolveParty();
			} 
            else 
            {
				removePartyMember(player);
			}
		}
	}

	/**
	 * Oust player from party
	 * Overloaded method that takes player's name as parameter
	 * @param name
	 */
	public void oustPartyMember(String name) 
    {
		L2PcInstance player = getPlayerByName(name);
		
		if (player != null) 
        {
			if (isLeader(player)) 
            {
				dissolveParty();
			} 
            else 
            {
				removePartyMember(player);
			}
		}
	}
	
	/**
	 * dissolves entire party
	 *
	 */
	private void dissolveParty() 
    {
		SystemMessage msg = new SystemMessage(SystemMessage.PARTY_DISPERSED);
		for(int i = 0; i < _members.size(); i++) 
        {
			L2PcInstance temp = _members.get(i);
			temp.sendPacket(msg);
			temp.sendPacket(new PartySmallWindowDeleteAll());
			temp.setParty(null);
		}
	}

	
	/**
	 * distribute item(s) to party members
	 * @param player
	 * @param item
	 */
	public void distributeItem(L2PcInstance player, L2ItemInstance item) 
    {
		L2PcInstance target = null;
		if (_randomLoot) 
        {
			target = getRandomMember();
		} 
        else 
        {
			target = player;
		}
		
		if (item.getCount() == 1) 
        {
			SystemMessage smsg = new SystemMessage(SystemMessage.YOU_PICKED_UP_S1);
			smsg.addItemName(item.getItemId());
			target.sendPacket(smsg);

			smsg = new SystemMessage(SystemMessage.S1_PICKED_UP_S2);
			smsg.addString(target.getName());
			smsg.addItemName(item.getItemId());
			broadcastToPartyMembers(target, smsg);
		} 
        else 
        {
			SystemMessage smsg = new SystemMessage(SystemMessage.YOU_PICKED_UP_S1_S2);
			smsg.addNumber(item.getCount());
			smsg.addItemName(item.getItemId());
			target.sendPacket(smsg);

			smsg = new SystemMessage(SystemMessage.S1_PICKED_UP_S2_S3);
			smsg.addString(target.getName());
			smsg.addNumber(item.getCount());
			smsg.addItemName(item.getItemId());
			broadcastToPartyMembers(target, smsg);
		}
	
		L2ItemInstance item2 = target.getInventory().addItem(item);
		InventoryUpdate iu = new InventoryUpdate();
		if (item2.getLastChange() == L2ItemInstance.ADDED) 
        {
			iu.addNewItem(item);
		} 
        else 
        {
			iu.addModifiedItem(item2);
		}
		
		target.sendPacket(iu);
		
		StatusUpdate su = new StatusUpdate(target.getObjectId());
		su.addAttribute(StatusUpdate.CUR_LOAD, target.getCurrentLoad());
		target.sendPacket(su);
	}
	
	/**
	 * distribute adena to party members
	 * @param adena
	 */
	public void distributeAdena(L2ItemInstance adena) 
    {
        SystemMessage smsg = new SystemMessage(SystemMessage.YOU_PICKED_UP_S1_ADENA);
        
        int[] adenaDist = new int[_members.size()];
        int totalAdena = adena.getCount();
        for(int i=0; i<adenaDist.length; i++)
        {
            int amount = totalAdena / (adenaDist.length - i);
            adenaDist[i] = amount;
            totalAdena -= amount;
            
            smsg.addNumber(adenaDist[i]);
            
            L2ItemInstance newAdena = ItemTable.getInstance().createItem(57);
            newAdena.setCount(adenaDist[i]);
            
            L2PcInstance member = _members.get(i);
            
            //_log.config(member.getName()+" Old adena: "+member.getInventory().getAdena());
            L2ItemInstance item2 = member.getInventory().addItem(newAdena);

            //_log.config(member.getName()+" New adena: "+member.getInventory().getAdena());

            InventoryUpdate iu = new InventoryUpdate();
            if (item2.getLastChange() == L2ItemInstance.ADDED) 
            {
                //_log.config(member.getName()+" update 1 adena: "+adena.getCount());
                iu.addNewItem(adena);
            } 
            else 
            {
                //_log.config(member.getName()+" update 2 adena: "+item2.getCount());
                iu.addModifiedItem(item2);
            }
            member.sendPacket(smsg);
            member.sendPacket(iu);
            
            //_log.config(member.getName()+" "+member.getInventory().getAdena());
        }
	}
		
	/**
	 * distributes XP and SP to party members
	 * takes total party dmg and monsters maxHp, xp rewars and sp reward as parameter
	 * @param partyDmg
	 * @param maxHp
	 * @param xpReward
	 * @param spReward
	 */
	public void distributeXpAndSp(int xpReward, int spReward, ArrayList<L2Character> rewardedMembers) 
    {
        double xpToReward       = 0;
        double spToReward       = 0;
        
        L2SummonInstance summon = null;
        L2PcInstance owner      = null;
        
        float penalty           = 0;
        
		for (L2Character member : rewardedMembers)
		{
		    penalty = 0;
		    
		    if (member instanceof L2SummonInstance)
		    {
		        summon  = (L2SummonInstance)member;
		        penalty = summon.getExpPenalty();
		        owner   = summon.getOwner();
		        
		        if (rewardedMembers.contains(owner))
		        {
		            rewardedMembers.remove(owner);
		        }
		    }
		    else if (member instanceof L2PcInstance)
		    { 
		        owner   = (L2PcInstance)member;
		        if (owner.getPet() instanceof L2SummonInstance && rewardedMembers.contains(owner.getPet()))
		        {
		            summon     = (L2SummonInstance)owner.getPet();
		            penalty    = summon.getExpPenalty();
		            rewardedMembers.remove(summon);
		        }
		    }
		    
		    xpToReward  = xpReward*((double)member.getLevel()/getLevel())*(1-((double)getMemberCount()/10));
		    xpToReward  *= 1-penalty;
		    spToReward  = spReward*((double)member.getLevel()/getLevel())*(1-((double)getMemberCount()/10));
		    
		    member.addExpAndSp((int)xpToReward, (int)spToReward);
		}
	}
	
	/**
	 * refresh party level
	 *
	 */
	public void recalculatePartyLevel() 
    {
		int newLevel = 0;
		for (L2PcInstance member : _members)
		{
			if (member.getLevel() > newLevel)
				newLevel = member.getLevel();
		}
		_partyLvl = newLevel;
	}
	
	private double getExpBonus()
	{
		int mcount = getMemberCount();
		if (mcount < 2 )
			return 1;
		double mul = 1;
        switch (mcount)
        {
			default: // 9 and above
                mul += 0.04; //71% extra
            case 8:
                mul += 0.04; //67% extra
            case 7:
                mul += 0.05; //63% extra
            case 6:
                mul += 0.04; //58% extra
            case 5:
                mul += 0.04; //54% extra
            case 4:
                mul += 0.11; //50% extra
            case 3:
                mul += 0.09; //39% extra
            case 2:
                mul += 0.30; //30% extra
        }
		return mul;
	}
	
	public int getLevel()
	{
		return _partyLvl;
	}
}
