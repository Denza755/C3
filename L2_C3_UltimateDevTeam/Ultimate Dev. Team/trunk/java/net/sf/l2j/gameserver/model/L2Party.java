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

import javolution.util.FastList;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.lib.Rnd;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PlayableInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2SummonInstance;
import net.sf.l2j.gameserver.model.entity.SevenSignsFestival;
import net.sf.l2j.gameserver.packets.serverpackets.*;
import net.sf.l2j.gameserver.skills.Stats;
import net.sf.l2j.gameserver.tables.ItemTable;

import java.util.List;
import java.util.logging.Logger;

/**
 * This class ...
 * 
 * @author nuocnam
 * @version $Revision: 1.6.2.2.2.6 $ $Date: 2005/04/11 19:12:16 $
 */
public class L2Party {
    static double[] _bonusExpSp = {1, 1.30, 1.39, 1.50, 1.54, 1.58, 1.63, 1.67, 1.71};

    private static Logger _log = Logger.getLogger(L2Party.class.getName());
    
	private List<L2PcInstance> _members = null;
    private List<L2PcInstance> _validMembers = null;
	private int _partyLvl = 0;
	private int _itemDistribution = 0;
	private int _itemOrder = 0;
	
	public static final int ITEM_LOOTER = 0;
	public static final int ITEM_RANDOM = 1;
	public static final int ITEM_RANDOM_SPOIL = 2;
	public static final int ITEM_ORDER = 3;
	public static final int ITEM_ORDER_SPOIL = 4;
	
	/**
	 * constructor ensures party has always one member - leader
	 * @param leader
	 * @param itemDistributionMode
	 */
	public L2Party(L2PcInstance leader, int itemDistribution) 
    {
        _validMembers = new FastList<L2PcInstance>();
		_itemDistribution = itemDistribution;
        getPartyMembers().add(leader);
        _validMembers.add(leader);
		_partyLvl = leader.getLevel();
	}
	
	/**
	 * returns number of party members
	 * @return
	 */
	public int getMemberCount() { return getPartyMembers().size(); }

    /**
     * returns number of party members
     * @return
     */
    public int getValidMemberCount() { return _validMembers.size(); }

	/**
	 * returns all party members
	 * @return
	 */
	public List<L2PcInstance> getPartyMembers()
	{
        if (_members == null) _members = new FastList<L2PcInstance>();
	    return _members;
	}
	
	/**
	 * get random member from party
	 * @return
	 */
	private L2PcInstance getRandomMember() { return getPartyMembers().get(Rnd.get(getPartyMembers().size())); }

	/**
	 * get next item looter
	 * @return
	 */
	private L2PcInstance getNextLooter()
	{
		int looter = _itemOrder;
		
		_itemOrder++;
		if (_itemOrder > getMemberCount()-1) _itemOrder = 0;
		
		return (looter < getPartyMembers().size()) ? getPartyMembers().get(looter) : null;
	}

	/**
	 * true if player is party leader
	 * @param player
	 * @return
	 */
	public boolean isLeader(L2PcInstance player) { return (getPartyMembers().get(0).equals(player)); }
    
    /**
     * Returns the Object ID for the party leader to be used as a unique identifier of this party
     * @return int 
     */
    public int getPartyLeaderOID() { return getPartyMembers().get(0).getObjectId(); }
		
	/**
	 * Broadcasts packet to every party member 
	 * @param msg
	 */
	public void broadcastToPartyMembers(ServerBasePacket msg) 
    {
		for(L2PcInstance member : getPartyMembers())
            member.sendPacket(msg);
	}

	
	/**
	 * Send a Server->Client packet to all other L2PcInstance of the Party.<BR><BR>
	 */
	public void broadcastToPartyMembers(L2PcInstance player, ServerBasePacket msg) 
    {
		for(L2PcInstance member : getPartyMembers())
		{
			if (!member.equals(player)) member.sendPacket(msg);
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
		window.setPartyList(getPartyMembers());
		player.sendPacket(window);
		
		player.sendPacket(new SystemMessage(SystemMessage.YOU_JOINED_S1_PARTY).addString(getPartyMembers().get(0).getName()));
		broadcastToPartyMembers(new SystemMessage(SystemMessage.S1_JOINED_PARTY).addString(player.getName()));
		broadcastToPartyMembers(new PartySmallWindowAdd(player));
		
		//add player to party, adjust party level
        getPartyMembers().add(player);
        if (checkMemberValidity(player)) _validMembers.add(player);
		if (player.getLevel() > _partyLvl)
        {
			_partyLvl = player.getLevel();
            recalculateValidMembers();
        }
		//update partySpelled 
		for(L2PcInstance member : getPartyMembers())
            member.updateEffectIcons();
	}
	
	/**
	 * removes player from party
	 * @param player
	 */
	public void removePartyMember(L2PcInstance player) 
    {
		if (getPartyMembers().contains(player)) 
        {
            getPartyMembers().remove(player);
            if (_validMembers.contains(player)) _validMembers.remove(player);
			recalculatePartyLevel();
            
            if (player.isFestivalParticipant())
                SevenSignsFestival.getInstance().updateParticipants(player, this);
			
			player.sendPacket(new SystemMessage(SystemMessage.YOU_LEFT_PARTY));
			player.sendPacket(new PartySmallWindowDeleteAll());
			player.setParty(null);
			
			broadcastToPartyMembers(new SystemMessage(SystemMessage.S1_LEFT_PARTY).addString(player.getName()));
			broadcastToPartyMembers(new PartySmallWindowDelete(player));
			
			if (getPartyMembers().size() == 1) 
            {
                getPartyMembers().get(0).setParty(null);
			}
		}
	}
	
	/**
	 * Change party leader (used for string arguments)
	 * @param name
	 */
	
	public void changePartyLeader(String name)
	{
		L2PcInstance player = getPlayerByName(name);
		
		if (player != null)
		{
			if (getPartyMembers().contains(player))
			{
				if (isLeader(player))
				{
                    player.sendPacket(new SystemMessage(SystemMessage.YOU_CANNOT_TRANSFER_RIGHTS_TO_YOURSELF));
				}
				else
				{
					//Swap party members
					L2PcInstance temp;
					int p1 = getPartyMembers().indexOf(player);
					temp = getPartyMembers().get(0);
                    getPartyMembers().set(0,getPartyMembers().get(p1));
                    getPartyMembers().set(p1,temp);
					
					broadcastToPartyMembers(new SystemMessage(SystemMessage.S1_HAS_BECOME_A_PARTY_LEADER).addString(_members.get(0).getName()));
					broadcastToPartyMembers(new PartySmallWindowUpdate(getPartyMembers().get(0)));
				}
			}
			else
			{
                player.sendPacket(new SystemMessage(SystemMessage.YOU_CAN_TRANSFER_RIGHTS_ONLY_TO_ANOTHER_PARTY_MEMBER));
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
		for(L2PcInstance member : getPartyMembers())
		{
			if (member.getName().equals(name)) return member;
		}
		return null;
	}

	/**
	 * Oust player from party
	 * @param player
	 */
	public void oustPartyMember(L2PcInstance player) 
    {
		if (getPartyMembers().contains(player)) 
        {
            if (isLeader(player)) 
            {
				removePartyMember(player);
                if(getPartyMembers().size() > 0)
                {
    				broadcastToPartyMembers(new SystemMessage(SystemMessage.S1_HAS_BECOME_A_PARTY_LEADER).addString(getPartyMembers().get(0).getName()));
    				broadcastToPartyMembers(new PartySmallWindowUpdate(getPartyMembers().get(0)));
                }
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
				removePartyMember(player);
				broadcastToPartyMembers(new SystemMessage(SystemMessage.S1_HAS_BECOME_A_PARTY_LEADER).addString(getPartyMembers().get(0).getName()));
				broadcastToPartyMembers(new PartySmallWindowUpdate(getPartyMembers().get(0)));
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
    /*  [DEPRECATED]
	private void dissolveParty() 
    {
		for(int i = 0; i < _members.size(); i++) 
        {
			L2PcInstance temp = _members.get(i);
			temp.sendPacket(new SystemMessage(SystemMessage.PARTY_DISPERSED));
			temp.sendPacket(new PartySmallWindowDeleteAll());
			temp.setParty(null);
		}
	}
    */

	
	/**
	 * distribute item(s) to party members
	 * @param player
	 * @param item
	 */
	public void distributeItem(L2PcInstance player, L2ItemInstance item) 
    {
		L2PcInstance target = null;
		
		switch (_itemDistribution)
		{
		case ITEM_RANDOM:
		case ITEM_RANDOM_SPOIL:
			target = getRandomMember();
			break;
		case ITEM_ORDER:
		case ITEM_ORDER_SPOIL:
			target = getNextLooter();
			break;
		case ITEM_LOOTER:
		default:
			target = player;
			break;
		}
        
        PcInventory inventory = player.getInventory();
        
        if (!inventory.validateCapacity(item))
        {
            if (_itemDistribution == ITEM_RANDOM || _itemDistribution == ITEM_RANDOM_SPOIL)
            {
                while (target == player)
                {
                    target = getRandomMember();
                }
            }
            else if (_itemDistribution == ITEM_ORDER || _itemDistribution == ITEM_ORDER_SPOIL)
            {
                target = getNextLooter();
            }
            else
            {
                if (Config.DEBUG) _log.warning("itemDistribution is ITEM_LOOTER on distributeItem() method");
            }
        }
		
		if (target != null)
		{
		    if (item.getCount() == 1) 
		    {
		        target.sendPacket(new SystemMessage(SystemMessage.YOU_PICKED_UP_S1).addItemName(item.getItemId()));
		        broadcastToPartyMembers(target, new SystemMessage(SystemMessage.S1_PICKED_UP_S2).addString(target.getName()).addItemName(item.getItemId()));
		    } 
		    else 
		    {
		        target.sendPacket(new SystemMessage(SystemMessage.YOU_PICKED_UP_S1_S2).addItemName(item.getItemId()).addNumber(item.getCount()));
		        
		        SystemMessage smsg = new SystemMessage(SystemMessage.S1_PICKED_UP_S2_S3);
		        smsg.addString(target.getName());
		        smsg.addItemName(item.getItemId());
                	smsg.addNumber(item.getCount());
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

		    target.sendPacket(new StatusUpdate(target.getObjectId()).addAttribute(StatusUpdate.CUR_LOAD, target.getCurrentLoad()));
		}
	}
	
	/**
	 * distribute adena to party members
	 * @param adena
	 */
	public void distributeAdena(L2ItemInstance adena) 
    {
        SystemMessage smsg = new SystemMessage(SystemMessage.YOU_PICKED_UP_S1_ADENA);
        
        int[] adenaDist = new int[getPartyMembers().size()];
        int totalAdena = adena.getCount();
        for(int i=0; i<adenaDist.length; i++)
        {
            int amount = totalAdena / (adenaDist.length - i);
            adenaDist[i] = amount;
            totalAdena -= amount;
            
            smsg.addNumber(adenaDist[i]);
            
            L2ItemInstance newAdena = ItemTable.getInstance().createItem(57);
            newAdena.setCount(adenaDist[i]);
            
            L2PcInstance member = getPartyMembers().get(i);
            
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
	 * Distribute Experience and SP rewards to L2PcInstance Party members in the known area of the last attacker.<BR><BR>
	 * 
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Get the L2PcInstance owner of the L2SummonInstance (if necessary) </li>
	 * <li>Calculate the Experience and SP reward distribution rate </li>
	 * <li>Add Experience and SP to the L2PcInstance </li><BR><BR>
	 * 
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method DOESN'T GIVE rewards to L2PetInstance</B></FONT><BR><BR>
	 * 
	 * @param xpReward The Experience reward to distribute
	 * @param spReward The SP reward to distribute
	 * @param rewardedMembers The list of L2PcInstance to reward and LSummonInstance whose owner must be reward
	 * @param lastAttacker The L2Character that has killed the L2Attackable
	 * 
	 */
	public void distributeXpAndSp(int xpReward, int spReward, List<L2Character> rewardedMembers, L2Character lastAttacker) 
    {
        L2SummonInstance summon = null;
        L2PcInstance owner      = null;
        
        float penalty;
        double sqLevel;
        double preCalculation;

        xpReward *= getExpBonus();
        spReward *= getSpBonus();

        double sqLevelSum = 0;
        for (L2PcInstance character : _validMembers)
            sqLevelSum += (character.getLevel() * character.getLevel());
        
        List<L2Character> ToRemove = new FastList<L2Character>();
        
		// Go through the members that must be rewarded
        synchronized(rewardedMembers)
        {
    		for (L2Character member : rewardedMembers)
    		{
                if(ToRemove != null && ToRemove.contains(member)) continue;

                // Check if members are near the last Attacker
    		    if ((member instanceof L2PlayableInstance && lastAttacker.getKnownList().knowsObject(member)) || lastAttacker.getKnownList().knowsThePlayer((L2PcInstance)member))
    			{
    				penalty = 0;
    				
    				// The reward can only be given to a L2PcInstance that owns the L2SummonInstance
    				// The Summon and the L2PcInstance are in the rewarded members table, but it's impossible to know which one is first
    				// That's why only one of them must be used (remove the other during the calculation of the first)
    				if (member instanceof L2SummonInstance)
    				{
                        summon  = (L2SummonInstance)member;
    					penalty = summon.getExpPenalty();
                        owner   = summon.getOwner();
                        
    					// Remove the owner from the rewarded members
    					if (rewardedMembers.contains(owner)) ToRemove.add(owner);
    				}
                    else if (member instanceof L2PcInstance)
                    { 
                        owner   = (L2PcInstance)member;
    					
    					// The L2SummonInstance penalty is only applied if it has hit the L2Attackable
                        if (owner.getPet() instanceof L2SummonInstance && rewardedMembers.contains(owner.getPet()))
                        {
                            summon     = (L2SummonInstance)owner.getPet();
                            penalty    = summon.getExpPenalty();
    						
    						// Remove the L2SummonInstance from the rewarded members
                            ToRemove.add(summon);
                        }
    				}
                    
    				// Calculate and add the EXP and SP reward to the member
                    if (_validMembers.contains(member))
                    {
                        sqLevel = member.getLevel() * member.getLevel();
                        preCalculation = (sqLevel / sqLevelSum) * (1 - penalty);

                        // Add the XP/SP points to the requested party member
                        member.addExpAndSp((int)member.calcStat(Stats.EXPSP_RATE, xpReward * preCalculation, null, null), 
                                           (int)member.calcStat(Stats.EXPSP_RATE, spReward * preCalculation, null, null));
                    }
    			}
    		}
        }
	}
	
    /**
     * Calculates and gives final XP and SP rewards to the party member.<BR>
     * This method takes in consideration number of members, members' levels, rewarder's level and bonus modifier for the actual party.<BR><BR>
     * 
     * @param member is the L2Character to be rewarded
     * @param xpReward is the total amount of XP to be "splited" and given to the member
     * @param spReward is the total amount of SP to be "splited" and given to the member
     * @param penalty is the penalty that must be applied to the XP rewards of the requested member
     */
    
	/**
	 * refresh party level
	 *
	 */
	public void recalculatePartyLevel() 
    {
		int newLevel = 0;
		for (L2PcInstance member : getPartyMembers())
		{
			if (member.getLevel() > newLevel)
				newLevel = member.getLevel();
		}
		_partyLvl = newLevel;
        
        recalculateValidMembers();
	}
	
    /**
     * refresh party valid members
     *
     */
    public void recalculateValidMembers()
    {
        _validMembers.clear();
        for (L2PcInstance member : getPartyMembers())
        {
            if (checkMemberValidity(member)) _validMembers.add(member);
        }
    }
    
    private boolean checkMemberValidity(L2PcInstance member)
    {
//      Fixed LevelDiff cutoff point
        if (Config.PARTY_XP_CUTOFF_METHOD.equalsIgnoreCase("level"))
        {
            return (getLevel() - member.getLevel() <= Config.PARTY_XP_CUTOFF_LEVEL);
        }
//      Fixed MinPercentage cutoff point
        else if (Config.PARTY_XP_CUTOFF_METHOD.equalsIgnoreCase("percentage")) 
        {
            int sqLevel = member.getLevel() * member.getLevel();
            int sqLevelSum = 0;
            for (L2PcInstance character : getPartyMembers())
            {
                sqLevelSum += (character.getLevel() * character.getLevel());
            }
            return (sqLevel * 100 >= sqLevelSum * Config.PARTY_XP_CUTOFF_PERCENT);
        }
//      Automatic cutoff method
        else if (Config.PARTY_XP_CUTOFF_METHOD.equalsIgnoreCase("auto")) 
        {
            int sqLevel = member.getLevel() * member.getLevel();
            int sqLevelSum = 0;
            for (L2PcInstance character : getPartyMembers())
            {
                sqLevelSum += (character.getLevel() * character.getLevel());
            }
            
            int i = getMemberCount() -1;
            if (i < 1 ) return true;
            if (i >= _bonusExpSp.length) i = _bonusExpSp.length -1;

            return (sqLevel >= sqLevelSum * (1-1/(1 +_bonusExpSp[i] -_bonusExpSp[i-1])));
        }
        return true;
    }

    private double getBaseExpSpBonus()
    {
        int i = getValidMemberCount() -1;
        if (i < 1 ) return 1;
        if (i >= _bonusExpSp.length) i = _bonusExpSp.length -1;

        return _bonusExpSp[i];
    }
    
	private double getExpBonus() { return getBaseExpSpBonus() * Config.RATE_PARTY_XP; }
    
    private double getSpBonus() { return getBaseExpSpBonus() * Config.RATE_PARTY_SP; }
	
	public int getLevel() { return _partyLvl; }
    
    public int getLootDistribution() { return _itemDistribution; }
	
	public boolean isDistributeSpoilLoot() { return (_itemDistribution == ITEM_RANDOM_SPOIL || _itemDistribution == ITEM_ORDER_SPOIL); }
}
