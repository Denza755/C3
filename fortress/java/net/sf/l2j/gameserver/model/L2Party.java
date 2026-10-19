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

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.ItemTable;
import net.sf.l2j.gameserver.SevenSignsFestival;
import net.sf.l2j.gameserver.lib.Rnd;
import net.sf.l2j.gameserver.serverpackets.InventoryUpdate;
import net.sf.l2j.gameserver.serverpackets.PartySmallWindowAdd;
import net.sf.l2j.gameserver.serverpackets.PartySmallWindowAll;
import net.sf.l2j.gameserver.serverpackets.PartySmallWindowDelete;
import net.sf.l2j.gameserver.serverpackets.PartySmallWindowDeleteAll;
import net.sf.l2j.gameserver.serverpackets.PartySmallWindowUpdate;
import net.sf.l2j.gameserver.serverpackets.ServerBasePacket;
import net.sf.l2j.gameserver.serverpackets.StatusUpdate;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;

/**
 * This class ...
 * 
 * @author nuocnam
 * @version $Revision: 1.6.2.2.2.6 $ $Date: 2005/04/11 19:12:16 $
 */
public class L2Party 
{
    //private static Logger _log = Logger.getLogger(L2Party.class.getName());
    
	private ArrayList<L2PcInstance> _members = null;
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
		_members = new ArrayList<L2PcInstance>();
		_itemDistribution = itemDistribution;
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
		return _members.get(Rnd.get(_members.size()));
	}

	/**
	 * get next item looter
	 * @return
	 */
	private L2PcInstance getNextLooter()
	{
		int looter = _itemOrder;
		
		_itemOrder++;
		if (_itemOrder > getMemberCount()-1)
			_itemOrder = 0;
		
		return (looter < _members.size()) ? _members.get(looter) : null;
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
     * Returns the Object ID for the party leader to be used as a unique identifier of this party
     * @return int 
     */
    public int getPartyLeaderOID()
    {
        return _members.get(0).getObjectId();
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
	 * Send a Server->Client packet to all other L2PcInstance of the Party.<BR><BR>
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
		
		player.sendPacket(new SystemMessage(SystemMessage.YOU_HAVE_JOINED_S1S_PARTY).addString(_members.get(0).getName()));
		broadcastToPartyMembers(new SystemMessage(SystemMessage.S1_HAS_JOINED_THE_PARTY).addString(player.getName()));
		broadcastToPartyMembers(new PartySmallWindowAdd(player));
		
		//add player to party, adjust party level
		_members.add(player);
		if (player.getLevel() > _partyLvl)
			_partyLvl = player.getLevel();
		//update partySpelled 
		player.updateEffectIcons();
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
            
            if (player.isFestivalParticipant())
                SevenSignsFestival.getInstance().updateParticipants(player, this);
			
			player.sendPacket(new SystemMessage(SystemMessage.YOU_HAVE_WITHDRAWN_FROM_THE_PARTY));
			player.sendPacket(new PartySmallWindowDeleteAll());
			player.setParty(null);
			
			broadcastToPartyMembers(new SystemMessage(SystemMessage.S1_HAS_LEFT_THE_PARTY).addString(player.getName()));
			broadcastToPartyMembers(new PartySmallWindowDelete(player));
			
			if (_members.size() == 1) 
            {
				_members.get(0).setParty(null);
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
			if (_members.contains(player))
			{
				if (isLeader(player))
				{
                    player.sendPacket(new SystemMessage(SystemMessage.YOU_CANNOT_TRANSFER_RIGHTS_TO_YOURSELF));
				}
				else
				{
					//Swap party members
					L2PcInstance temp;
					int p1 = _members.indexOf(player);
					temp = _members.get(0);
					_members.set(0,_members.get(p1));
					_members.set(p1,temp);
					
					SystemMessage msg = new SystemMessage(SystemMessage.S1_HAS_BECOME_A_PARTY_LEADER);
					msg.addString(_members.get(0).getName());
					broadcastToPartyMembers(msg);
					broadcastToPartyMembers(new PartySmallWindowUpdate(_members.get(0)));
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
				removePartyMember(player);
                if(_members.size() > 0)
                {
    				SystemMessage msg = new SystemMessage(SystemMessage.S1_HAS_BECOME_A_PARTY_LEADER);
    				msg.addString(_members.get(0).getName());
    				broadcastToPartyMembers(msg);
    				broadcastToPartyMembers(new PartySmallWindowUpdate(_members.get(0)));
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
				broadcastToPartyMembers(new SystemMessage(SystemMessage.S1_HAS_BECOME_A_PARTY_LEADER).addString(_members.get(0).getName()));
				broadcastToPartyMembers(new PartySmallWindowUpdate(_members.get(0)));
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
		SystemMessage msg = new SystemMessage(SystemMessage.PARTY_DISPERSED);
		for(int i = 0; i < _members.size(); i++) 
        {
			L2PcInstance temp = _members.get(i);
			temp.sendPacket(msg);
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
		
		if (target != null)
		{
		    if (item.getCount() == 1) 
		    {
		        target.sendPacket(new SystemMessage(SystemMessage.YOU_HAVE_OBTAINED_S1).addItemName(item.getItemId()));
		        
		        SystemMessage smsg = new SystemMessage(SystemMessage.S1_HAS_OBTAINED_S2);
		        smsg.addString(target.getName());
		        smsg.addItemName(item.getItemId());
		        broadcastToPartyMembers(target, smsg);
		    } 
		    else 
		    {
		        SystemMessage smsg = new SystemMessage(SystemMessage.YOU_HAVE_OBTAINED_S2_S1);
		        smsg.addNumber(item.getCount());
		        smsg.addItemName(item.getItemId());
		        target.sendPacket(smsg);
		        
		        smsg = new SystemMessage(SystemMessage.S1_HAS_OBTAINED_S3_S2);
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
	}
	
	/**
	 * distribute adena to party members
	 * @param adena
	 */
	public void distributeAdena(L2ItemInstance adena) 
    {
        SystemMessage smsg = new SystemMessage(SystemMessage.YOU_HAVE_OBTAINED_S1_ADENA);
        
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
	public void distributeXpAndSp(int xpReward, int spReward, ArrayList<L2Character> rewardedMembers, L2Character lastAttacker) 
    {
        L2SummonInstance summon = null;
        L2PcInstance owner      = null;
        
        float penalty           = 0;
        
        ArrayList<L2Character> ToRemove = new ArrayList<L2Character>();
        
		// Go through the members that must be rewarded
        synchronized(rewardedMembers)
        {
    		for (L2Character member : rewardedMembers)
    		{
          if(ToRemove != null && ToRemove.contains(member))
          {
              continue;
          }
    			// Check if members are near the last Attacker
          if ((member instanceof L2PlayableInstance && lastAttacker.knowsObject(member)) || lastAttacker.knowsThePlayer((L2PcInstance)member))
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
    					if (rewardedMembers.contains(owner))
    					  ToRemove.add(owner);
    				}
            else
              if (member instanceof L2PcInstance)
              { 
                owner = (L2PcInstance)member;
    					
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
    				giveRewardsToMember(member, xpReward, spReward, penalty);
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
    
    private void giveRewardsToMember(L2Character member, int xpReward, int spReward, float penalty)
    {
        double xpToReward = 0;
        double spToReward = 0;
        double sum = 0;
        double sqd = 0;
        for (L2PcInstance character : getPartyMembers())
        {
            sum += (character.getLevel() * character.getLevel());
        }
        sqd = member.getLevel() * member.getLevel();
        xpToReward = (xpReward * (sqd/sum) * getExpBonus()) * (1 - penalty);
        spToReward = (spReward * (sqd/sum) * getSpBonus()) * (1-penalty);

        // Get the XP Cutoff point
        if (!Config.PARTY_XP_CUTOFF_METHOD.equalsIgnoreCase("none"))
        {
            if (Config.PARTY_XP_CUTOFF_METHOD.equalsIgnoreCase("level"))   // Fixed LevelDiff cutoff point
            {
                int MemberDiffLevel = getLevel() - member.getLevel();
                int PartyMaxDiffLevel = Config.PARTY_XP_CUTOFF_LEVEL;
                if ( MemberDiffLevel > PartyMaxDiffLevel )
                {
                    xpToReward = 0;
                    spToReward = 0;
                }
            }
            else                                                        // Fixed MinPercentage cutoff point
            {
                double lowestPercentageToGive = Config.PARTY_XP_CUTOFF_PERCENT;
                double percentageToGive = ((sqd/sum)*100);
                if (percentageToGive < lowestPercentageToGive)
                {
                    xpToReward = 0;
                    spToReward = 0;
                }
            }
        }
        
        // Add the XP/SP points to the requested party member
        member.addExpAndSp((int)xpToReward, (int)spToReward);
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
        if (!Config.PARTY_XP_CUTOFF_METHOD.equalsIgnoreCase("none"))
        {
            if (Config.PARTY_XP_CUTOFF_METHOD.equalsIgnoreCase("level"))   // Fixed LevelDiff cutoff point
            {
                for(L2PcInstance member : getPartyMembers())
                {
                    int MemberDiffLevel = getLevel() - member.getLevel();
                    int PartyMaxDiffLevel = Config.PARTY_XP_CUTOFF_LEVEL;
                    if ( MemberDiffLevel > PartyMaxDiffLevel )
                    {
                        mcount--;
                    }
                }
            }
            else                                                        // Fixed MinPercentage cutoff point
            {
                for(L2PcInstance member : getPartyMembers())
                {
                    double sum = 0;
                    double sqd = 0;
                    for (L2PcInstance character : getPartyMembers())
                    {
                        sum += (character.getLevel() * character.getLevel());
                    }
                    sqd = member.getLevel() * member.getLevel();
                    double lowestPercentageToGive = Config.PARTY_XP_CUTOFF_PERCENT;
                    double percentageToGive = ((sqd/sum)*100);
                    if (percentageToGive > lowestPercentageToGive)
                    {
                        mcount--;
                    }
                }
            }
        }
        if (mcount < 2 )
            return 1;
        double mul = 1;
        switch (mcount)
        {
			default: // 9 and above
                mul += 0.71; //71% extra
                break;
            case 8:
                mul += 0.67; //67% extra
                break;
            case 7:
                mul += 0.63; //63% extra
                break;
            case 6:
                mul += 0.58; //58% extra
                break;
            case 5:
                mul += 0.54; //54% extra
                break;
            case 4:
                mul += 0.50; //50% extra
                break;
            case 3:
                mul += 0.39; //39% extra
                break;
            case 2:
                mul += 0.30; //30% extra
                break;
        }
		return mul;
	}
  private double getSpBonus()
  {
    int mcount = getMemberCount();
    if (!Config.PARTY_XP_CUTOFF_METHOD.equalsIgnoreCase("none"))
    {
        if (Config.PARTY_XP_CUTOFF_METHOD.equalsIgnoreCase("level"))   // Fixed LevelDiff cutoff point
        {
            for(L2PcInstance member : getPartyMembers())
            {
                int MemberDiffLevel = getLevel() - member.getLevel();
                int PartyMaxDiffLevel = Config.PARTY_XP_CUTOFF_LEVEL;
                if ( MemberDiffLevel > PartyMaxDiffLevel )
                {
                    mcount--;
                }
            }
        }
        else                                                        // Fixed MinPercentage cutoff point
        {
            for(L2PcInstance member : getPartyMembers())
            {
                double sum = 0;
                double sqd = 0;
                for (L2PcInstance character : getPartyMembers())
                {
                    sum += (character.getLevel() * character.getLevel());
                }
                sqd = member.getLevel() * member.getLevel();
                double lowestPercentageToGive = Config.PARTY_XP_CUTOFF_PERCENT;
                double percentageToGive = ((sqd/sum)*100);
                if (percentageToGive > lowestPercentageToGive)
                {
                    mcount--;
                }
            }
        }
    }
    if (mcount < 2 )
      return 1;
    double mul = 1;
        switch (mcount)
        {
      default: // 9 and above
                mul += 0.71; //71% extra
                break;
            case 8:
                mul += 0.67; //67% extra
                break;
            case 7:
                mul += 0.63; //63% extra
                break;
            case 6:
                mul += 0.58; //58% extra
                break;
            case 5:
                mul += 0.54; //54% extra
                break;
            case 4:
                mul += 0.50; //50% extra
                break;
            case 3:
                mul += 0.39; //39% extra
                break;
            case 2:
                mul += 0.30; //30% extra
                break;
        }
    return mul;
  }

  
	public int getLevel()
	{
		return _partyLvl;
	}
    
    public int getLootDistribution()
    {
        return _itemDistribution;
    }
	
	public boolean isDistributeSpoilLoot()
	{
		boolean rv = false;
		
		if (_itemDistribution == ITEM_RANDOM_SPOIL || _itemDistribution == ITEM_ORDER_SPOIL)
			rv = true;
		
		return rv;
	}
}
