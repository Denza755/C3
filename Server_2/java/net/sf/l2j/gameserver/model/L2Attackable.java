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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.EventDroplist;
import net.sf.l2j.gameserver.GameServer;
import net.sf.l2j.gameserver.ItemTable;
import net.sf.l2j.gameserver.EventDroplist.DateDrop;
import net.sf.l2j.gameserver.ai.CtrlEvent;
import net.sf.l2j.gameserver.ai.CtrlIntention;
import net.sf.l2j.gameserver.ai.L2AttackableAI;
import net.sf.l2j.gameserver.ai.L2CharacterAI;
import net.sf.l2j.gameserver.model.quest.QuestState;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;
import net.sf.l2j.util.L2ObjectHashSet;

/**
 * This class ...
 * 
 * @version $Revision: 1.24.2.3.2.16 $ $Date: 2005/04/11 19:11:21 $
 */
public class L2Attackable extends L2NpcInstance
{
	static Logger _log = Logger.getLogger(L2Attackable.class.getName());
    
//    private static final int MAX_DRIFT_RANGE = 300;
	
	public final class AggroInfo
	{
		// the attacker we track
		L2Character attacker;
		// how much this mob hates the attacker
		int hate;
		// how much damage was done by attacker
		int damage;
		AggroInfo(L2Character attacker) {
			this.attacker = attacker;
		}
		public boolean equals(Object obj) {
			return this == obj || this.attacker == attacker;
		}
		public int hashCode() {
			return this.attacker.hashCode();
		}
	}
	private HashMap<L2Character, AggroInfo> _aggroList;
	
	private boolean _spoiled;
	private L2ItemInstance[] _sweepItems;
	
	/**
	 * @param template
	 */
	public L2Attackable(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
	}
	
	public L2CharacterAI getAI() {
		if (_ai == null)
		{
			synchronized(this)
			{
				if (_ai == null)
					_ai = new L2AttackableAI(new AIAccessor());
			}
		}
		return _ai;
	}
	
	protected int getDistanceToWatchObject(L2Object object)
	{
		if (object instanceof L2FolkInstance || !(object instanceof L2Character))
			return 0;
		if (object instanceof L2PlayableInstance) {
			return 1500;
		}
		if (getAggroRange() > getFactionRange())
			return getAggroRange();
		if (getFactionRange() > 200)
			return getFactionRange();
		return 200;
	}
	protected int getDistanceToForgetObject(L2Object object)
	{
		if (_aggroList != null && _aggroList.get(object) != null)
			return 3000;
		return 2*getDistanceToWatchObject(object);
	}

	// get condition to hate, actually isAggressive() is checked
	// by monster and karma by guards in motheds that overwrite this one.
	public boolean getCondition2(L2Character target)
	{
		if (target instanceof L2FolkInstance || target instanceof L2DoorInstance)
			return false;
		if (target.isAlikeDead() ||
			 getDistance(target.getX(), target.getY()) > getAggroRange() ||
			 Math.abs(getZ()-target.getZ()) > 100
			 )
			return false;
		if (target instanceof L2PcInstance)
			return !((L2PcInstance)target).isInvul();
		return true;
	}
    
	public void removeKnownObject(L2Object object)
	{
		super.removeKnownObject(object);
		
		// this monster forgets any agro when hostile leaves the area
		if (_aggroList != null && object instanceof L2Character)
		{
			_aggroList.remove(object);
			if (_aggroList.isEmpty())
				_aggroList = null;
		}
		
		L2ObjectHashSet<L2PcInstance> known = getKnownPlayers();
		if (getAI() != null && (known == null || known.isEmpty()))
        {
			getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
        }
	}
	
	public void reduceCurrentHp(double damage, L2Character attacker)
	{
		reduceCurrentHp(damage, attacker, true);
	}
	
	public void reduceCurrentHp(double damage, L2Character attacker, boolean awake)
	{
		// this is usually only called in combat
		if (attacker != null)
			addDamage(attacker, (int)damage);
		super.reduceCurrentHp(damage, attacker, awake);
	}
	
	protected void doDie(L2Character killer) {
		try { calculateRewards(killer); } catch (Exception e) { _log.log(Level.SEVERE, "", e); }
		try {
			if (killer instanceof L2PcInstance) {
				L2PcInstance player = (L2PcInstance)killer;
				QuestState[] quests = player.getQuestsForKills(getTemplate().npcId);
				if (quests != null) {
					for (QuestState qs : quests) {
						qs.getQuest().notifyKill(getTemplate().npcId, qs);
					}
				}
			}
		} catch (Exception e) { _log.log(Level.SEVERE, "", e); }
		super.doDie(killer);
	}
	
	
	/**
	 * @param attacker
	 */
	private void calculateRewards(L2Character lastAttacker)
	{
		if (_aggroList == null)
			return;
		int npcID = getTemplate().npcId;
		
		while (!_aggroList.isEmpty())
		{
			AggroInfo info;
			{
				Iterator<AggroInfo> it = _aggroList.values().iterator();
				info = it.next();
			}
			float penalty = 0;
			L2Character attacker = info.attacker;
			int damage = info.damage;
			L2Party attackerParty = null;
			if (attacker instanceof L2PcInstance)
			{
				L2PcInstance temp = (L2PcInstance)attacker;
				if (temp.isInParty()) attackerParty = temp.getParty();
			}
			else if (attacker instanceof L2SummonInstance)
			{
				L2Summon temp = (L2Summon)attacker;
                if (temp.isInParty()) attackerParty = temp.getParty();
			}
			
			if (npcID <= 0 || damage <= 1)
			{
				_aggroList.remove(attacker);
				continue;
			}
			
			if (attackerParty == null)
			{
				if (attacker instanceof L2SummonInstance)
                {
                    penalty = ((L2SummonInstance)attacker).getExpPenalty();
                    if (_aggroList.containsKey(((L2SummonInstance)attacker).getOwner()))
                    {
                        L2PcInstance owner = ((L2SummonInstance)attacker).getOwner();
                        damage += _aggroList.get(owner).damage;
                        _aggroList.remove(owner); // remove the owner so we won't calc it's xp/sp again
                    }
                }
				
				if (attacker instanceof L2PcInstance && ((L2PcInstance)attacker).getPet() instanceof L2SummonInstance && _aggroList.containsKey(((L2PcInstance)attacker).getPet()))
                {
                    L2SummonInstance summon = (L2SummonInstance)((L2PcInstance)attacker).getPet();
                    penalty = summon.getExpPenalty();
                    damage += _aggroList.get(summon).damage;
                    _aggroList.remove(summon); // remove the summon so we won't calc it's xp/sp again
                }
				
				if (damage > getMaxHp())
					damage = getMaxHp();
				
				//mob = 24, atk = 10, diff = -14 (full xp)
				//mob = 24, atk = 28, diff = 4 (some xp)
				//mob = 24, atk = 50, diff = 26 (no xp)
				int diff = attacker.getLevel() - getLevel();
                if (attacker instanceof L2SummonInstance)
                {
                    diff = ((L2SummonInstance)attacker).getOwner().getLevel() - getLevel();
                }
				
				int xp = 0;
				int sp = 0;
				if (attacker.knowsObject(this))
				{
					int[] tmp = calculateExpAndSp(diff, damage);
					xp = tmp[0];
					xp *= 1-penalty;
					sp = tmp[1];
				}
				
				attacker.addExpAndSp(xp, sp);
				_aggroList.remove(attacker);
			}
			else
			{
				//share with party members
				int partyDmg = 0;
				float partyMul = 1.f;
				
				ArrayList<L2PcInstance> members = attackerParty.getPartyMembers();
				ArrayList<L2Character> rewardedMembers = new ArrayList<L2Character>();
				for(L2PcInstance pl : members) {
					AggroInfo ai = _aggroList.get(pl);
					if (ai != null) {
						partyDmg += ai.damage;
						_aggroList.remove(pl);
					}

					if (pl.getPet() instanceof L2SummonInstance && _aggroList.containsKey(pl.getPet()))
                    {
                        partyDmg += _aggroList.get(pl.getPet()).damage;
                        rewardedMembers.add(pl.getPet());
                        _aggroList.remove(pl.getPet());
                    }
					if (!pl.isDead())
						rewardedMembers.add(pl);
				}
				if (partyDmg < getMaxHp())
					{
					float partyDmgF = partyDmg;
					float getMaxHpf = getMaxHp();
					partyMul = (partyDmgF / getMaxHpf);
					}
				
				if (partyDmg > getMaxHp())
					partyDmg = getMaxHp();
				
				int diff = attackerParty.getLevel() - getLevel();
				
				int[] tmp = calculateExpAndSp(diff, partyDmg);
				int xp = tmp[0];
				xp *= 1-penalty;
				int sp = tmp[1];
				
				xp *= partyMul;
				sp *= partyMul;
				
				attackerParty.distributeXpAndSp(xp, sp, rewardedMembers);
			}
		}
		_aggroList = null;
		
		doItemDrop(lastAttacker);
		
		if (hasSpoil())
			doSweepDrop(lastAttacker);
	}
	
	private void addDamage(L2Character attacker, int damage)
	{
		addDamageHate(attacker, damage, damage);
	}
	
	/**
	 * @param damage
	 * @param attacker
	 * @return most hated character 
	 */
	public void addDamageHate(L2Character attacker, int damage, int aggro)
	{
		if (attacker == null)
			return;
		if (_aggroList == null)
		{
			_aggroList = new HashMap<L2Character,AggroInfo>();
			AggroInfo ai = new AggroInfo(attacker);
			ai.damage = damage;
			ai.hate = aggro;
			_aggroList.put(attacker, ai);
		}
		else
		{
			AggroInfo ai = _aggroList.get(attacker);
			if (ai != null) {
				ai.damage += damage;
				ai.hate += aggro;
			} else {
				ai = new AggroInfo(attacker);
				ai.damage = damage;
				ai.hate = aggro;
				_aggroList.put(attacker, ai);
			}
		}
		if (aggro > 0 && getAI().getIntention() == CtrlIntention.AI_INTENTION_IDLE)
			getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
		if (damage > 0)
			getAI().notifyEvent(CtrlEvent.EVT_ATTACKED, attacker);
	}
	
	public L2Character getMostHated() {
		if (_aggroList == null || isAlikeDead())
			return null;
		L2Character mostHated = null;
		int maxHate = 0;
		for (AggroInfo ai : _aggroList.values()) {
			if (ai.attacker.isAlikeDead() || !knowsObject(ai.attacker) ||!ai.attacker.isVisible())
				ai.hate = 0;
			if (ai.hate > maxHate) {
				mostHated = ai.attacker;
				maxHate = ai.hate;
			}
		}
		return mostHated;
	}

	public int getHating(L2Character target) {
		if (_aggroList == null)
			return 0;
		AggroInfo ai = _aggroList.get(target);
		if (ai == null)
			return 0;
		if (!ai.attacker.isVisible()) {
			_aggroList.remove(target);
			return 0;
		}
		if (ai.attacker.isAlikeDead()) {
			ai.hate = 0;
			return 0;
		}
		return ai.hate;
	}

    public void doSweepDrop(L2Character lastAttacker)
    {
        if (lastAttacker.getLevel() - getLevel() > 9)
        {
            //System.out.println("Too high level no Spoil.");
            return;
        }
		
		List<L2DropData> drops = getTemplate().getDropData();
		ArrayList<L2ItemInstance> spoiled = new ArrayList<L2ItemInstance>();
        
        // See how many items we need to drop for a given mobId
        if (Config.DEBUG) _log.finer("this npc has "+drops.size()+" drops defined");
        
        for (Iterator<L2DropData> iter = drops.iterator(); iter.hasNext();)
        {
            L2DropData drop = iter.next();
            if (!drop.isSweep())
            {
                // this is a sweep drop, it will not appear on the ground
                // so just skip it
                continue;
            }
            
            // Roll the dice.. between 0 and MAX_CHANCE
            int roll = L2Character.getRnd().nextInt(L2DropData.MAX_CHANCE);
            
            // compare roll with chance... "are we lucky?"
            if (roll < drop.getChance()*Config.RATE_DROP_SPOIL)
            {
                L2ItemInstance dropit = ItemTable.getInstance().createItem(drop.getItemId());
                
                // Get min,max,sweep
                int min = drop.getMinDrop();
                int max = drop.getMaxDrop();
                
                int itemCount = 0;
                if (min < max)
                {
                    // getRnd().nextInt(n) requires n > 0
                    // Get RND between 0 and (max-min) then add min to get range between min and max...
                    itemCount = L2Character.getRnd().nextInt(max - min) + min;
                }
                else
                {
                    itemCount = 1;
                }
                
                if (dropit.getItemId() == 57) 
                {
                    itemCount *= Config.RATE_DROP_ADENA;
                }
                
                if (itemCount != 0) // only happens if min=0 and max=1... but still need to check!
                {
                    dropit.setCount(itemCount);
                    
                    if (Config.DEBUG)
                    	_log.fine("Item id to spoil: "+drop.getItemId()+" amount: "+dropit.getCount());
					
					spoiled.add(dropit);
                }
                else
                {
                    _log.warning("Roll produced 0 items to drop... Cancelling");
                }
            }
        }
		
		if (spoiled.size() > 0)
		{
			_sweepItems = spoiled.toArray(new L2ItemInstance[spoiled.size()]);
		}
    }
    
    public void doEventDrop(L2Character lastAttacker)
    {
        if (lastAttacker.getLevel() - getLevel() > 9)
        {
            //System.out.println("Too low level no drop.");
            return;
        }
        for (DateDrop drop : EventDroplist.getInstance().getAllDrops())
        {
            int roll = L2Character.getRnd().nextInt(L2DropData.MAX_CHANCE);
            if (roll < drop.chance)
            {
                Random generator = new Random();
			    int number = generator.nextInt(drop.items.length);
			    int itemID = drop.items[number];
			    
			    L2ItemInstance dropit = ItemTable.getInstance().createItem(itemID);
			    
			    int min = drop.min;
				int max = drop.max;
				
				int itemCount = 0;
				if (min < max)
				{
					// getRnd().nextInt(n) requires n > 0
					// Get RND between 0 and (max-min) then add min to get range between min and max...
					itemCount = L2Character.getRnd().nextInt(max - min) + min;
				}
				else
				{
					itemCount = 1;
				}
			    
			    dropit.setCount(itemCount);
			    
			    if (Config.DEBUG)
			    _log.fine("Item id to drop: "+number+" amount: "+dropit.getCount());
			    
                if (GameServer.autoLootEnabled() && lastAttacker instanceof L2PcInstance)
                {
                    ((L2PcInstance)lastAttacker).doAutoLoot(dropit);
                }
                else 
                {
                	// position where mob was last
                	dropit.dropMe(this, getX(), getY(), getZ()+100);
                }
            }
        }
    }
	
	/**
	 */
    public void doItemDrop(L2Character lastAttacker)
    {
        
        doEventDrop(lastAttacker);
        
        // FIXME: SWEEP is not implemented... waiting for skills...
        
        ArrayList<L2DropData> drops = new ArrayList<L2DropData>();
		drops.addAll(getTemplate().getDropData());
		if (lastAttacker instanceof L2PcInstance) {
			L2PcInstance player = (L2PcInstance)lastAttacker;
			player.fillQuestDrops(this, drops);
		}
        
		// See how many items we need to drop for a given mobId
		
		if (Config.DEBUG) _log.finer("this npc has "+drops.size()+" drops defined");
		
		for (L2DropData drop : drops)
		{
			if (drop.isSweep())
			{
				// this is a sweep drop, it will not appear on the ground
				// so just skip it
				continue;
			}
			
			// Roll the dice.. between 0 and MAX_CHANCE
			int roll = L2Character.getRnd().nextInt(L2DropData.MAX_CHANCE);
			//System.out.println("roll = "+roll+", chance = "+drop.getChance()*Config.RATE_DROP_ITEMS);
			// compare roll with chance... "are we lucky?"
			if (roll < drop.getChance()*Config.RATE_DROP_ITEMS)
			{
				L2ItemInstance dropit = ItemTable.getInstance().createItem(drop.getItemId());
				
				// Get min,max,sweep
				int min = drop.getMinDrop();
				int max = drop.getMaxDrop();
				
				int itemCount = 0;
				if (min < max)
				{
					// getRnd().nextInt(n) requires n > 0
					// Get RND between 0 and (max-min) then add min to get range between min and max...
					itemCount = L2Character.getRnd().nextInt(max - min) + min;
				}
				else
				{
					itemCount = 1;
				}
				
				if (dropit.getItemId() == 57) 
				{
					itemCount *= Config.RATE_DROP_ADENA;
				}
				
				if (itemCount != 0)	// only happens if min=0 and max=1... but still need to check!
				{
					dropit.setCount(itemCount);
					
					if (Config.DEBUG)
						_log.fine("Item id to drop: "+drop.getItemId()+" amount: "+dropit.getCount());
                    if (GameServer.autoLootEnabled() && lastAttacker instanceof L2PcInstance)
                    {
                        ((L2PcInstance)lastAttacker).doAutoLoot(dropit);
                    }
                    else if (drop.isQuestDrop())
					{
                        ((L2PcInstance)lastAttacker).doAutoLoot(dropit);
					}
                    else 
                    {
						// position where mob was last
						dropit.dropMe(this, getX(), getY(), getZ()+100);
					}
				}
				else
				{
					_log.warning("Roll produced 0 items to drop... Cancelling");
				}
			}
		}
	}
	
	public L2ItemInstance getActiveWeapon()
	{
		return null;
	}
	
	//some exports for guards
	public boolean noTarget()
	{
		return _aggroList==null || _aggroList.isEmpty();
	}
	
	public boolean containsTarget(L2Character player)
	{
		if (_aggroList == null)
			return false;
		return _aggroList.containsKey(player);
	}
	
	public void clearAggroList()
	{
		_aggroList = null;
	}
	
	public boolean isSweepActive()
	{
		return _sweepItems != null;
	}
	
    public synchronized L2ItemInstance[] takeSweep() {
		L2ItemInstance[] sweep = _sweepItems;
		_sweepItems = null;
		return sweep;
	}
    
    public void activateSpoil() {
		_spoiled = true;
	}

	public boolean hasSpoil() {
		return _spoiled;
	}
	
	private int[] calculateExpAndSp(int diff, int damage)
	{
		double xp = 0;
		double sp = 0;
		if (Config.ALT_GAME_EXPONENT_XP != 0 || Config.ALT_GAME_EXPONENT_SP != 0)
		{
			xp = (double)getExpReward() * damage / getMaxHp();
			if (Config.ALT_GAME_EXPONENT_XP != 0)
				xp *= Math.pow(2., -diff / Config.ALT_GAME_EXPONENT_XP);
			sp = (double)getSpReward() * damage / getMaxHp();
			if (Config.ALT_GAME_EXPONENT_SP != 0)
				sp *= Math.pow(2., -diff / Config.ALT_GAME_EXPONENT_SP);
		}
		else
		{
			// mob is 1 to 9 levels below attacker lvl
			if ((diff > 0) && (diff <= 9))
			{
				xp = (double)getExpReward() * damage / getMaxHp();
				sp = (double)getSpReward() * damage / getMaxHp();
				xp = xp - diff * xp / 10;
				sp = sp - diff * sp / 10;
			}
			//attacker's level below or equal to mob lvl
			else if (diff <= 0) 
			{
				xp = (double)getExpReward() * damage / getMaxHp();
				sp = (double)getSpReward() * damage / getMaxHp();
			}
			//mob is more than 9 levels below attacker lvl 
			else if (diff > 9)
			{
				xp = 0;
				sp = 0;
			}
			
			if (xp <= 0)
			{
				xp = 0;
				sp = 0;
			}
			else if (sp <= 0 && xp > 0)
			{
				sp = 0;
			}
		}
		
		int[] tmp = { (int)xp, (int)sp };
		return  tmp;
	}
}
