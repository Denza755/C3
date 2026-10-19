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
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javolution.util.FastList;
import javolution.util.FastMap;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.EventDroplist;
import net.sf.l2j.gameserver.ItemTable;
import net.sf.l2j.gameserver.ItemsAutoDestroy;
import net.sf.l2j.gameserver.EventDroplist.DateDrop;
import net.sf.l2j.gameserver.ai.CtrlEvent;
import net.sf.l2j.gameserver.ai.CtrlIntention;
import net.sf.l2j.gameserver.ai.L2AttackableAI;
import net.sf.l2j.gameserver.ai.L2CharacterAI;
import net.sf.l2j.gameserver.lib.Rnd;
import net.sf.l2j.gameserver.lib.mysql;
import net.sf.l2j.gameserver.model.quest.QuestState;
import net.sf.l2j.gameserver.serverpackets.ItemList;
import net.sf.l2j.gameserver.serverpackets.PlaySound;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;

/**
 * This class manages all NPC that can be attacked.<BR><BR>
 *
 * L2Attackable :<BR><BR>
 * <li>L2ArtefactInstance</li>
 * <li>L2FriendlyMobInstance</li>
 * <li>L2MonsterInstance</li>
 * <li>L2SiegeGuardInstance </li>
 *
 * @version $Revision: 1.24.2.3.2.16 $ $Date: 2005/04/11 19:11:21 $
 */
public class L2Attackable extends L2NpcInstance
{
	static Logger _log = Logger.getLogger(L2Attackable.class.getName());

	/**
	 * This class contains all AggroInfo of the L2Attackable against the attacker L2Character.<BR><BR>
	 *
	 * <B><U> Data</U> :</B><BR><BR>
	 * <li>attacker : The attaker L2Character concerned by this AggroInfo of this L2Attackable </li>
	 * <li>hate : Hate level of this L2Attackable against the attaker L2Character (hate = damage) </li>
	 * <li>damage : Number of damages that the attaker L2Character gave to this L2Attackable </li><BR><BR>
	 *
	 */
	public final class AggroInfo
	{
		/** The attaker L2Character concerned by this AggroInfo of this L2Attackable */
		L2Character attacker;

		/** Hate level of this L2Attackable against the attaker L2Character (hate = damage) */
		int hate;

		/** Number of damages that the attaker L2Character gave to this L2Attackable */
		int damage;


		/**
		 * Constructor of AggroInfo.<BR><BR>
		 */
		AggroInfo(L2Character attacker)
		{
			this.attacker = attacker;
		}

		/**
		 * Verify is object is equal to this AggroInfo.<BR><BR>
		 */
		public boolean equals(Object obj)
		{
			return this == obj || this.attacker == attacker;
		}

		/**
		 * Return the Identifier of the attaker L2Character.<BR><BR>
		 */
		public int hashCode()
		{
			return this.attacker.getObjectId();
		}

	}


	/**
	 * This class contains all RewardInfo of the L2Attackable against the any attacker L2Character, based on amount of damage done.<BR><BR>
	 *
	 * <B><U> Data</U> :</B><BR><BR>
	 * <li>attacker : The attaker L2Character concerned by this RewardInfo of this L2Attackable </li>
	 * <li>dmg : Total amount of damage done by the attacker to this L2Attackable (summon + own) </li>
	 *
	 */
	protected final class RewardInfo
	{
		protected L2Character attacker;
		protected int dmg = 0;

		public RewardInfo(L2Character attacker, int dmg)
		{
			this.attacker = attacker;
			this.dmg = dmg;
		}

		public void addDamage(int dmg)
		{
			if(dmg<0)
				dmg = 0;

			this.dmg += dmg;
		}

		public boolean equals(Object obj)
		{
			return this == obj || this.attacker == attacker;
		}

		public int hashCode()
		{
			return this.attacker.getObjectId();
		}
	}

	/**
	 * This class contains all AbsorberInfo of the L2Attackable against the absorber L2Character.<BR><BR>
	 *
	 * <B><U> Data</U> :</B><BR><BR>
	 * <li>absorber : The attaker L2Character concerned by this AbsorberInfo of this L2Attackable </li>
	 *
	 */
	public final class AbsorberInfo
	{
		/** The attaker L2Character concerned by this AbsorberInfo of this L2Attackable */
		L2PcInstance absorber;
		int crystalId;
		double absorbedHP;

		/**
		 * Constructor of AbsorberInfo.<BR><BR>
		 */
		AbsorberInfo(L2PcInstance attacker, int crystalId, double absorbedHP)
		{
			this.absorber = attacker;
			this.crystalId = crystalId;
			this.absorbedHP = absorbedHP;
		}

		/**
		 * Verify is object is equal to this AbsorberInfo.<BR><BR>
		 */
		public boolean equals(Object obj)
		{
			return this == obj || this.absorber == absorber;
		}

		/**
		 * Return the Identifier of the absorber L2Character.<BR><BR>
		 */
		public int hashCode()
		{
			return this.absorber.getObjectId();
		}
	}

	/** The table containing all autoAttackable L2Character in its Aggro Range and L2Character that attacked the L2Attackable */
	private ConcurrentHashMap<L2Character, AggroInfo> _aggroList;

	/** Table containing all Items that a Dwarf can Sweep on this L2Attackable */
	private L2ItemInstance[] _sweepItems;

	/** crops */
	private L2ItemInstance[] _harvestItems;
	private boolean _seeded;
	private int _seedType;
	/** True if a Soul Crystal was successfuly used on the L2Attackable */
	private boolean _absorbed;

	/** The table containing all L2PcInstance that successfuly absorbed the soul of this L2Attackable */
	private ConcurrentHashMap<L2PcInstance, AbsorberInfo> _absorbersList;

	/** Static tables containing all mobIDs that can have their souls absorbed, divided by max soul crystal level */
	private int[] _absorbingMOBS_level4 = {
			583, 584, 585, 586, 587, 588, 636, 637, 638, 639, 640, 641, 642, 643, 644, 645};
	private int[] _absorbingMOBS_level8 = {
			646, 647, 648, 649, 650, 1006, 1007, 1008};
	private int[] _absorbingMOBS_level10 = {
			627, 628, 629, 674, 761, 762, 821, 823, 826, 827, 828, 829, 830, 831,
			858, 859, 860, 1009, 1010, 1062, 1063, 1068, 1070};
	private int[] _absorbingMOBS_level12 = {
			10283, 10286, 12221, 12372, 12374};

	/** Soul Crystal Basic Informations */
	// First ID of each soul crystal
	private static final int _REDCRYSTAL_OFFSET = 4629;
	private static final int _REDCRYSTAL_LVL11 = 5577;
	private static final int _REDCRYSTAL_LVL12 = 5580;
	private static final int _GREENCRYSTAL_OFFSET = 4640;
	private static final int _GREENCRYSTAL_LVL11 = 5578;
	private static final int _GREENCRYSTAL_LVL12 = 5581;
	private static final int _BLUECRYSTAL_OFFSET = 4651;
	private static final int _BLUECRYSTAL_LVL11 = 5579;
	private static final int _BLUECRYSTAL_LVL12 = 5582;
	// Max number of levels a soul crystal may reach
	private static final int _MAX_CRYSTALS_LEVEL = 12;
	// Chances for each event of a soul crystal
	private static final double _BREAK_CHANCE = 42.5;
	private static final double _LEVEL_CHANCE = 32.5;
	/** End of Soul Crystal Basic Informations */

	/**
	 * Constructor of L2Attackable (use L2Character and L2NpcInstance constructor).<BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Call the L2Character constructor to set the _template of the L2Attackable (copy skills from template to object and link _calculators to NPC_STD_CALCULATOR) </li>
	 * <li>Set the name of the L2Attackable</li>
	 * <li>Create a RandomAnimation Task that will be launched after the calculated delay if the server allow it </li><BR><BR>
	 *
	 * @param objectId Identifier of the object to initialized
	 * @param L2NpcTemplate Template to apply to the NPC
	 */
	public L2Attackable(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
	}


	/**
	 * Return the L2Character AI of the L2Attackable and if its null create a new one.<BR><BR>
	 */
	public L2CharacterAI getAI()
	{
		return setAI(new L2AttackableAI(new AIAccessor()));
	}

	/**
	 * Return the distance under which the object must be add to _knownObject in function of the object type.<BR><BR>
	 *
	 * <B><U> Values </U> :</B><BR><BR>
	 * <li> object is a L2FolkInstance : 0 (don't remember it) </li>
	 * <li> object is a L2Character : 0 (don't remember it) </li>
	 * <li> object is a L2PlayableInstance : 1500 </li>
	 * <li> Aggro Range > Faction Range : Aggro Range </li>
	 * <li> Faction Range > 200 : Faction Range </li>
	 * <li> others : 200 </li><BR><BR>
	 *
	 * @param object The Object to add to _knownObject
	 *
	 */
	public int getDistanceToWatchObject(L2Object object)
	{
		if (object instanceof L2PlayableInstance)
			return 4000;

		if (object instanceof L2FolkInstance || !(object instanceof L2Character))
			return 0;

		if (getAggroRange() > getFactionRange())
			return getAggroRange();

		if (getFactionRange() > 200)
			return getFactionRange();

		return 200;
	}


	/**
	 * Remove a L2Object from _knownObjects, _knownPlayer, _aggroList of the L2Attackable then cancel Attak or Cast if it was targeted.<BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Remove object from _knownObjects of the L2Attackable </li>
	 * <li>If object is a L2PcInstance, remove object from _knownPlayer of the L2Attackable </li>
	 * <li>If object is targeted by the L2Attackable, cancel Attack or Cast </li>
	 * <li>If object is a L2Character, remove it from the _aggrolist of the L2Attackable </li>
	 * <li>Set the L2Attackable Intention to AI_INTENTION_IDLE </li><BR><BR>
	 *
	 * <B><U> Overriden in </U> :</B><BR><BR>
	 * <li> L2MonsterInstance : Notify the L2MonsterInstance AI with EVT_FORGET_OBJECT </li>
	 * <li> L2GuardInstance :  Notify the L2GuardInstance to return to its home location</li><BR><BR>
	 *
	 * @param object The L2Object to remove from _knownObjects, _knownPlayer and _aggroList
	 *
	 */
	public void removeKnownObject(L2Object object)
	{
		// Remove a L2Object from _knownObjects and if necessary from _knownPlayer of the L2Attackable then cancel Attak or Cast if it was targeted
		super.removeKnownObject(object);

		// Remove the L2Object from the _aggrolist of the L2Attackable
		if (_aggroList != null && object instanceof L2Character)
		{
			_aggroList.remove(object);
			if (_aggroList.isEmpty())
				_aggroList = null;
		}

		//FIXME: This is a temporary solution
		if(hasAI() && !isKnownPlayers())
		{
//			_log.info("Set L2Attackable "+this+" idle: ai="+hasAI()+"; players="+isKnownPlayers()+"; super.players="+super.isKnownPlayers());
			setIntention(CtrlIntention.AI_INTENTION_IDLE, null, null);
		}
	}

	/**
	 * Reduce the current HP of the L2Attackable.<BR><BR>
	 *
	 * @param damage The HP decrease value
	 * @param attacker The L2Character who attacks
	 *
	 */
	public void reduceCurrentHp(double damage, L2Character attacker)
	{
		reduceCurrentHp(damage, attacker, true);
	}

	/**
	 * Reduce the current HP of the L2Attackable, update its _aggroList and launch the doDie Task if necessary.<BR><BR>
	 *
	 * @param i The HP decrease value
	 * @param attacker The L2Character who attacks
	 * @param awake The awake state (If True : stop sleeping)
	 *
	 */
	public void reduceCurrentHp(double damage, L2Character attacker, boolean awake)
	{
		if ((this instanceof L2SiegeGuardInstance) && (attacker instanceof L2SiegeGuardInstance))
			//if((this.getEffect(L2Effect.EffectType.CONFUSION)!=null) && (attacker.getEffect(L2Effect.EffectType.CONFUSION)!=null))
				return;

//		if ((this instanceof L2MonsterInstance)&&(attacker instanceof L2MonsterInstance))
			//if((this.getEffect(L2Effect.EffectType.CONFUSION)!=null) && (attacker.getEffect(L2Effect.EffectType.CONFUSION)!=null))
//				return;

		if (this instanceof L2MonsterInstance && attacker instanceof L2CastleGuardInstance)
		{
			L2CastleGuardInstance guard = (L2CastleGuardInstance) attacker;
			guard.statsUp((int)damage, getLevel());
		}

		// Add damage and hate to the attacker AggroInfo of the L2Attackable _aggroList
		if (attacker != null)
			addDamage(attacker, (int)damage);

		// If this L2Attackable is a L2MonsterInstance and it has spawned minions, call its minions to battle
		if (this instanceof L2MonsterInstance)
		{
			L2MonsterInstance master = (L2MonsterInstance) this;
			if (this instanceof L2MinionInstance)
			{
				master = ((L2MinionInstance)this).getLeader();
			//	if (!master.isInCombat())
                                if (!master.isInCombat() && !master.isDead())
		  			master.addDamage(attacker, 1);
			}
			if (master.hasMinions())
				master.callMinionsToAssist(attacker);
		}

		// Reduce the current HP of the L2Attackable and launch the doDie Task if necessary
		super.reduceCurrentHp(damage, attacker, awake);

	}

	/**
	 * Kill the L2Attackable (the corpse disappeared after 7 seconds), distribute rewards (EXP, SP, Drops...) and notify Quest Engine.<BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Distribute Exp and SP rewards to L2PcInstance (including Summon owner) that hit the L2Attackable and to their Party members </li>
	 * <li>Notify the Quest Engine of the L2Attackable death if necessary</li>
	 * <li>Kill the L2NpcInstance (the corpse disappeared after 7 seconds) </li><BR><BR>
	 *
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method DOESN'T GIVE rewards to L2PetInstance</B></FONT><BR><BR>
	 *
	 * @param killer The L2Character that has killed the L2Attackable
	 *
	 */
	public void doDie(L2Character killer)
	{
		// Enhance soul crystals of the attacker if this L2Attackable had its soul absorbed
		try {
			if (killer instanceof L2PcInstance)
			{
				levelSoulCrystals(killer);
			}
		}
		catch (Exception e) { _log.log(Level.SEVERE, "", e); }

		// Distribute Exp and SP rewards to L2PcInstance (including Summon owner) that hit the L2Attackable and to their Party members
		try { calculateRewards(killer); } catch (Exception e) { _log.log(Level.SEVERE, "", e); }

		// Notify the Quest Engine of the L2Attackable death if necessary
		try {
			if (killer instanceof L2PcInstance || killer instanceof L2SummonInstance)
			{
				// Get the L2PcInstance that killed the L2Attackable
				L2PcInstance player = killer instanceof L2PcInstance?(L2PcInstance)killer:((L2SummonInstance)killer).getOwner();

				// Get a table containing all QuestState to modify after the L2Attackable killing
				QuestState[] quests = player.getQuestsForKills(this);

				if (quests != null)
				{
					// Go through table containing all QuestState to modify
					for (QuestState qs : quests)
					{
						// Notify the Quest Engine of the L2Attackable death
						qs.getQuest().notifyKill(this, qs);
					}
				}

			}
		}
		catch (Exception e) { _log.log(Level.SEVERE, "", e); }

		// Kill the L2NpcInstance (the corpse disappeared after 7 seconds)
		super.doDie(killer);

		if (Config.KILL_COUNTER && killer instanceof L2PcInstance){
		//SystemMessage sm = new SystemMessage(614);
		//sm.addString("Killer "+killer.getObjectId()+"; killed: "+this.getNpcId());
		//killer.sendPacket(sm);

		mysql.set("UPDATE `server_variables` SET `value` = `value` + '1' WHERE `type` = 'killcount' AND `index` = '"+this.getNpcId()+"'" );
		int count = mysql.simple_get_int("count", "killcount", "`char_id` = '"+killer.getObjectId()+"' AND `npc_id` = '"+this.getNpcId()+"'" );
		if ( count != 0 )
			{
				mysql.set("UPDATE `killcount` SET `count` = `count` + '1' WHERE `char_id` = '"+killer.getObjectId()+"' AND `npc_id` = '"+this.getNpcId()+"'" );
				SystemMessage sm = new SystemMessage(614);
				sm.addString("You have killed "+(count+1)+" monsters of this type");
				killer.sendPacket(sm);
			}
		else
			{
				mysql.set("INSERT INTO `killcount` SET `count` = '1', `char_id` = '"+killer.getObjectId()+"', `npc_id` = '"+this.getNpcId()+"'" );
			}
		}

	}

	/**
	 * Distribute Exp and SP rewards to L2PcInstance (including Summon owner) that hit the L2Attackable and to their Party members.<BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Get the L2PcInstance owner of the L2SummonInstance (if necessary) and L2Party in progress </li>
	 * <li>Calculate the Experience and SP rewards in function of the level difference</li>
	 * <li>Add Exp and SP rewards to L2PcInstance (including Summon penalty) and to Party members in the known area of the last attacker </li><BR><BR>
	 *
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method DOESN'T GIVE rewards to L2PetInstance</B></FONT><BR><BR>
	 *
	 * @param lastAttacker The L2Character that has killed the L2Attackable
	 *
	 */
	private void calculateRewards(L2Character lastAttacker)
	{
		if (_aggroList == null)
			return;

		// Get the Identifier of the L2Attackable killed
		int npcID = getTemplate().npcId;

		// Creates an empty list of rewards
		FastMap<L2Character, RewardInfo> rewards = new FastMap<L2Character, RewardInfo>().setShared(true);
		int rewardCount = 0;

		synchronized (_aggroList)
		{
			// Go through the _aggroList of the L2Attackable
			if (!_aggroList.isEmpty())
			{
				List<L2Character> toRemove  = new FastList<L2Character>();
				for (AggroInfo info : _aggroList.values())
				{
					// Get the L2Character corresponding to this attacker
					if (info == null)
						continue;
					L2Character attacker = info.attacker;
					L2Character ddealer = info.attacker;

					// Get damages done by this attacker
					int damage = info.damage;

					// Prevent unwanted behavior
					if (npcID <= 0 || damage <= 1)
					{
						toRemove.add(attacker);
						//_aggroList.remove(attacker);
						continue;
					}

					if (attacker instanceof L2SummonInstance)
						ddealer = ((L2SummonInstance)attacker).getOwner();

					// Calculate real damages (Summoners should get own damage plus summon's damage)
					RewardInfo reward = null;
					
					reward = rewards.get(ddealer);
					
					if (reward == null)
					{
						reward = new RewardInfo(ddealer, damage);
						rewardCount++;
					}
					else
					{
						reward.addDamage(damage);
					}
					
					rewards.put(ddealer, reward);
					/*
					if (rewards.isEmpty() || !rewards.containsKey(ddealer))
					{
						reward = new RewardInfo(ddealer, damage);
						rewards.put(ddealer, reward);
					}
					else
					{
						reward = rewards.get(ddealer);
						reward.addDamage(damage);
					}*/
					//_aggroList.remove(attacker);
					toRemove.add(attacker);
					}
				
				for (L2Character attacker : toRemove)
				{
					if (attacker != null)
					_aggroList.remove(attacker);
				}
			}
		}

		if (!rewards.isEmpty())
		{
			for (FastMap.Entry<L2Character, RewardInfo> entry = rewards.head(), end = rewards.tail(); (entry = entry.getNext()) != end;)
			{
				if (entry == null)
					continue;

				RewardInfo reward = entry.getValue();
				
				if(reward == null)
					continue;
				
				// Penalty applied to the attacker's XP
				float penalty = 0;

				// Attacker to be rewarded
				L2Character attacker = reward.attacker;

				// Total amount of damage done
				int damage = reward.dmg;

				// Create a new L2Party object
				L2Party attackerParty = null;

				// If the attacker is a Pet, get the party of the owner
				if (attacker instanceof L2PetInstance)
					attackerParty = ((L2PetInstance)attacker).getParty();
				else
					if (!(attacker instanceof L2PcInstance))
						return;
					else
						attackerParty = ((L2PcInstance)attacker).getParty();

				// If this attacker is a L2PcInstance with a summoned L2SummonInstance, get Exp Penalty applied for the current summoned L2SummonInstance
				if (attacker instanceof L2PcInstance && ((L2PcInstance)attacker).getPet() instanceof L2SummonInstance)
				{
					L2SummonInstance summon = (L2SummonInstance)((L2PcInstance)attacker).getPet();
					penalty = summon.getExpPenalty();
				}

				// We must avoid "over damage", if any
				if (damage > getMaxHp()) damage = getMaxHp();

				// If there's NO party in progress
				if (attackerParty == null)
				{
					// Calculate the difference of level between this attacker (L2PcInstance or L2SummonInstance owner) and the L2Attackable
					// mob = 24, atk = 10, diff = -14 (full xp)
					// mob = 24, atk = 28, diff = 4 (some xp)
					// mob = 24, atk = 50, diff = 26 (no xp)
					int diff = attacker.getLevel() - getLevel();

					int xp = 0;
					int sp = 0;

					// Calculate Exp and SP rewards
					if (attacker.knowsObject(this))
					{
						int[] tmp = calculateExpAndSp(diff, damage);
						xp = tmp[0];
						xp *= 1 - penalty;
						sp = tmp[1];
					}

					// Distribute the Exp and SP between the L2PcInstance and its L2Summon
					attacker.addExpAndSp(xp, sp);
				}
				else
				{
					//share with party members
					int partyDmg = 0;
					float partyMul = 1.f;

					// Get all L2Character (including L2Summon) that can be rewarded in the party
					ArrayList<L2Character> rewardedMembers = new ArrayList<L2Character>();

					// Go through all L2PcInstance in the party
					for (L2PcInstance pl : attackerParty.getPartyMembers())
					{
						if (pl == null)
							continue;

						// Get the RewardInfo of this L2PcInstance from L2Attackable rewards
						RewardInfo ai = rewards.get(pl);

						// If the L2PcInstance is in the L2Attackable rewards add its damages to party damages
						if (ai != null)
						{
							// Add L2PcInstance damages to party damages
							partyDmg += ai.dmg;

							// Remove the L2PcInstance from the L2Attackable rewards
							rewards.remove(pl);
						}

						// Add L2PcInstance of the party (that have attacked or not) to members that can be rewarded if it's not dead
						if (!pl.isDead()) rewardedMembers.add(pl);
					}

					// If the party didn't killed this L2Attackable alone
					if (partyDmg < getMaxHp())
					{
						float partyDmgF = partyDmg;
						float getMaxHpf = getMaxHp();

						partyMul = (partyDmgF / getMaxHpf);
					}

					// Avoid "over damage"
					if (partyDmg > getMaxHp()) partyDmg = getMaxHp();

					// Calculate the level difference between Party and L2Attackable
					int diff = attackerParty.getLevel() - getLevel();

					// Calculate Exp and SP rewards
					int[] tmp = calculateExpAndSp(diff, partyDmg);
					int xp = tmp[0];
					xp *= 1 - penalty;
					int sp = tmp[1];

					xp *= partyMul;
					sp *= partyMul;

					// Distribute Experience and SP rewards to L2PcInstance Party members in the known area of the last attacker
					if (partyDmg > 0)
						attackerParty.distributeXpAndSp(xp, sp, rewardedMembers, lastAttacker);
				}
			}
		}

		_aggroList = null;
		rewards = null;

		// Manage Base, Quests and Special Events drops of the L2Attackable
		doItemDrop(lastAttacker);

		// Manage Sweep drops of the L2Attackable
		if (isSpoil())
			doSweepDrop(lastAttacker);

	}


	/**
	 * Add damage and hate to the attacker AggroInfo of the L2Attackable _aggroList.<BR><BR>
	 *
	 * @param attacker The L2Character that gave damages to this L2Attackable
	 * @param damage The number of damages given by the attacker L2Character
	 *
	 */
	public void addDamage(L2Character attacker, int damage)
	{
		addDamageHate(attacker, damage, damage);
	}

	/**
	 * Add damage and hate to the attacker AggroInfo of the L2Attackable _aggroList.<BR><BR>
	 *
	 * @param attacker The L2Character that gave damages to this L2Attackable
	 * @param damage The number of damages given by the attacker L2Character
	 * @param aggro The hate (=damage) given by the attacker L2Character
	 *
	 */
	public void addDamageHate(L2Character attacker, int damage, int aggro)
	{
		if(damage < 0)
			damage = 0;

		if (attacker == null)
			return;

		if (_aggroList == null)
		{
			// If necessary, create the _aggroList
			_aggroList = new ConcurrentHashMap<L2Character,AggroInfo>();

			// Create a AggroInfo object and Init it
			AggroInfo ai = new AggroInfo(attacker);
			ai.damage = damage;
			ai.hate = aggro;

			// Add the attaker L2Character and the AggroInfo object in the _aggroList of the L2Attackable
			_aggroList.put(attacker, ai);

		}
		else
		{
			// Get the AggroInfo of the attacker L2Character from the _aggroList of the L2Attackable
			AggroInfo ai = _aggroList.get(attacker);

			if (ai != null)
			{
				// Add new damage and aggro (=damage) to the AggroInfo object
				ai.damage += damage;
				ai.hate += aggro;
			}
			else
			{
				// Create a AggroInfo object and Init it
				ai = new AggroInfo(attacker);
				ai.damage = damage;
				ai.hate = aggro;

				// Add the attaker L2Character and the AggroInfo object in the _aggroList of the L2Attackable
				if (_aggroList != null)
				_aggroList.put(attacker, ai);
			}
		}

		// Set the intention to the L2Attackable to AI_INTENTION_ACTIVE

		if(aggro > 0 && getIntention() == CtrlIntention.AI_INTENTION_IDLE)
			setIntention(CtrlIntention.AI_INTENTION_ACTIVE, null, null);

		// Notify the L2Attackable AI with EVT_ATTACKED
		if(damage > 0)
			notifyEvent(CtrlEvent.EVT_ATTACKED, attacker, null);

		try
		{
			if (attacker instanceof L2PcInstance || attacker instanceof L2SummonInstance)
			{
				L2PcInstance player = attacker instanceof L2PcInstance?(L2PcInstance)attacker:((L2SummonInstance)attacker).getOwner();

				QuestState[] quests = player.getQuestsForAttacks(this);

				if (quests != null)
				{
					for (QuestState qs : quests)
						qs.getQuest().notifyAttack(this, qs);
				}

			}
		}
		catch (Exception e) { _log.log(Level.SEVERE, "", e); e.printStackTrace(); }

	}

	/**
	 * Return the most hated L2Character of the L2Attackable _aggroList.<BR><BR>
	 */
	public L2Character getMostHated()
	{
		if (_aggroList == null || isAlikeDead())
			return null;

		L2Character mostHated = null;
		int maxHate = 0;

		// Go through the aggroList of the L2Attackable
		for (AggroInfo ai : _aggroList.values())
		{
			if (ai.attacker.isAlikeDead() || !knowsObject(ai.attacker) ||!ai.attacker.isVisible())
				ai.hate = 0;

			if (ai.hate > maxHate)
			{
				mostHated = ai.attacker;
				maxHate = ai.hate;
			}
		}

		return mostHated;
	}


	/**
	 * Return the hate level of the L2Attackable against this L2Character contained in _aggroList.<BR><BR>
	 *
	 * @param target The L2Character whose hate level must be returned
	 *
	 */
	public int getHating(L2Character target)
	{
		if (_aggroList == null)
			return 0;

		AggroInfo ai = _aggroList.get(target);

		if (ai == null)
			return 0;

		if (!ai.attacker.isVisible())
		{
			_aggroList.remove(target);
			return 0;
		}

		if (ai.attacker.isAlikeDead())
		{
			ai.hate = 0;
			return 0;
		}

		return ai.hate;
	}

	/**
	 * Calculates quantity of items for specific drop acording to current situation <br>
	 * 
	 * @param drop The L2DropData count is being calculated for
	 * @param lastAttacker The L2PcInstance that has killed the L2Attackable
	 * @param deepBlueDrop Factor to divide the drop chance
	 * @param levelModifier level modifier in %'s (will be subtracted from drop chance)
	 */
	 private int calculateDropCount(L2PcInstance lastAttacker, L2DropData drop, int levelModifier)
	 {
		// Roll the dice.. between 0 and MAX_CHANCE (=1000000)
		int roll = Rnd.get(1, L2DropData.MAX_CHANCE);

		float rate = (drop.isSweep()? Config.RATE_DROP_SPOIL:Config.RATE_DROP_ITEMS);
		
		// Get default drop chance
		float dropChance = drop.getChance() * rate;

		int deepBlueDrop = 1;
		if (Config.DEEPBLUE_DROP_RULES)
		{
			 if (levelModifier > 0)
			 {
				 // We should multiply by the server's drop rate, so we always get a low chance of drop for deep blue mobs.
				 // NOTE: This is valid only for adena drops! Others drops will still obey server's rate
				 deepBlueDrop = 3;
				 if (drop.getItemId() == 57) deepBlueDrop *= (int)Config.RATE_DROP_ITEMS;
			 }
		}

		// Check if we should apply our maths so deep blue mobs will not drop that easy
		if (Config.DEEPBLUE_DROP_RULES) dropChance = ((drop.getChance() - ((drop.getChance() * levelModifier)/100)) * rate / deepBlueDrop);

		// Round drop chance
		dropChance = Math.round(dropChance);

		// Set our limits for chance of drop
		if (dropChance < 1) dropChance = 1;
		if (drop.getItemId() == 57 && dropChance > L2DropData.MAX_CHANCE) dropChance = L2DropData.MAX_CHANCE; // If item is adena, dont drop multiple time

		int itemCount = 0;

		// Check if the Item must be dropped
		while (roll <= dropChance)
		{
			 // Get min and max Item quantity that can be dropped in one time
			 int min = drop.getMinDrop();
			 int max = drop.getMaxDrop();

			 // Get the item quantity dropped
			 if (min < max)
				 itemCount += Rnd.get(min, max);
			 else if (min == max)
				 itemCount += min;
			 else
				 itemCount++;

			 // Calculate the Adena reward with the RATE_DROP_ADENA
			 if (drop.getItemId() == 57) itemCount *= Config.RATE_DROP_ADENA;
			 
			 // Prepare for next iteration if dropChance > L2DropData.MAX_CHANCE
			 dropChance -= L2DropData.MAX_CHANCE;
		}

		if (itemCount == 0 && Config.DEBUG) _log.fine("Roll produced 0 items to drop...");

		return itemCount;
	 }

	 /**
	  * Calculates the level modifier for drop<br>
	  * 
	  * @param lastAttacker The L2PcInstance that has killed the L2Attackable
	  */
	 private int calculateLevelModifierForDrop(L2PcInstance lastAttacker)
	 {
		 if (Config.DEEPBLUE_DROP_RULES)
		 {
			 int highestLevel = lastAttacker.getLevel();

			 // Check to prevent very high level player to nearly kill mob and let low level player do the last hit.
			 if (getAttackByList() != null && getAttackByList().size() > 0)
			 {
				 for (L2Character atkChar: getAttackByList())
					 if (atkChar != null && atkChar.getLevel() > highestLevel) highestLevel = atkChar.getLevel();
			 }

			 // According to official data (Prima), deep blue mobs are 9 or more levels below players
			 if (highestLevel - 9 >= getLevel()) return ((highestLevel - (getLevel() + 8)) * 9);
		 }

		 return 0;
	 }

	 private void dropItem(L2PcInstance lastAttacker, int itemId, int itemCount)
	 {
		if(itemCount == 0)return;
		if(Config.DROP_COUNTER)
		{
			mysql.set("UPDATE `server_variables` SET `value` = `value` + '"+itemCount+"' WHERE `type` = 'dropcount' AND `index` = '"+itemId+"'" );
			int counter = mysql.simple_get_int("counter", "dropcount", "`char_id` = '"+lastAttacker.getObjectId()+"' AND `item_id` = '"+itemId+"'" );
			if ( counter != 0 )
				mysql.set("UPDATE `dropcount` SET `counter` = `counter` + '"+itemCount+"' WHERE `char_id` = '"+lastAttacker.getObjectId()+"' AND `item_id` = '"+itemId+"'" );
			else
				mysql.set("INSERT INTO `dropcount` SET `counter` = '"+itemCount+"', `char_id` = '"+lastAttacker.getObjectId()+"', `item_id` = '"+itemId+"'" );
		}

		L2ItemInstance dropit;

		 for (int i = 1; i <= itemCount; i++)
		 {
			 dropit = ItemTable.getInstance().createItem(itemId);

			 // Set the Item quantity dropped if L2ItemInstance is stackable
			 if (dropit.isStackable())
			 {
				 i = itemCount;				  // Set so loop won't happent again
				 dropit.setCount(itemCount);	 // Set item count
			 }
			 if (Config.DEBUG) _log.fine("Item id to drop: " + itemId + " amount: " + dropit.getCount());

					if(this instanceof L2RaidBossInstance)
					{
						SystemMessage sm = new SystemMessage(SystemMessage.S1_DIED_AND_DROPPED_S3_S2);
						sm.addString(getName());
						sm.addNumber(dropit.getCount());
						sm.addItemName(itemId);
						broadcastPacket(sm);
					}

			 // Check if the autoLoot mode is active
			 if (Config.AUTO_LOOT)
				 lastAttacker.doAutoLoot(dropit); // Give this or these Item(s) to the L2PcInstance that has killed the L2Attackable
			 else 
			 {
				L2CharPosition dropPos = Rnd.coordsRandomize(this, 70);				 

				 // Init the dropped L2ItemInstance and add it in the world as a visible object at the position where mob was last
				 dropit.dropMe(this, dropPos.x, dropPos.y, this.getZ()+200);

				 // Add drop to auto destroy item task
				 if (Config.AUTODESTROY_ITEM_AFTER > 0) ItemsAutoDestroy.getInstance().addItem(dropit);
				}
			}
	 }

	/**
	 * Manage Special Events drops created by GM for a defined period.<BR><BR>
	 *
	 * <B><U> Concept</U> :</B><BR><BR>
	 * During a Special Event all L2Attackable can drop extra Items.
	 * Those extra Items are defined in the table <B>allNpcDateDrops</B> of the EventDroplist.
	 * Each Special Event has a start and end date to stop to drop extra Items automaticaly. <BR><BR>
	 *
	 * <B><U> Actions</U> : <I>If an extra drop must be generated</I></B><BR><BR>
	 * <li>Get an Item Identifier (random) from the DateDrop Item table of this Event </li>
	 * <li>Get the Item quantity dropped (random) </li>
	 * <li>Create this or these L2ItemInstance corresponding to this Item Identifier</li>
	 * <li>If the autoLoot mode is actif and if the L2Character that has killed the L2Attackable is a L2PcInstance, give this or these Item(s) to the L2PcInstance that has killed the L2Attackable</li>
	 * <li>If the autoLoot mode isn't actif or if the L2Character that has killed the L2Attackable is not a L2PcInstance, add this or these Item(s) in the world as a visible object at the position where mob was last</li><BR><BR>
	 *
	 * @param lastAttacker The L2Character that has killed the L2Attackable
	 */
	public void doEventDrop(L2Character lastAttacker)
	{
		 L2PcInstance player = null;
		 if (lastAttacker instanceof L2PcInstance)
			 player = (L2PcInstance)lastAttacker;
		 else if (lastAttacker instanceof L2Summon)
			 player = ((L2Summon)lastAttacker).getOwner();

		 if (player == null) return; // Don't drop anything if the last attacker or ownere isn't L2PcInstance

		 if (player.getLevel() - getLevel() > 9)
			return;

		// Go through DateDrop of EventDroplist allNpcDateDrops within the date range
		 int itemCount;
		 int max;
		 int min;
		for (DateDrop drop : EventDroplist.getInstance().getAllDrops())
		{
			 if (Rnd.get(1, L2DropData.MAX_CHANCE) < drop.chance)
			{
				 // Get min and max Item quantity that can be dropped in one time
				 max = drop.max;
				 min = drop.min;

				// Get the item quantity dropped
				 itemCount = 0;
				 if (min < max)
					 itemCount += Rnd.get(min, max);
				 else if (min == max)
					 itemCount += min;
				else
					 itemCount++;

				 dropItem(player, drop.items[Rnd.get(0, drop.items.length - 1)], itemCount);
			}
		}
	}


	/**
	 * Manage Base, Quests and Special Events drops of L2Attackable (called by calculateRewards).<BR><BR>
	 *
	 * <B><U> Concept</U> :</B><BR><BR>
	 * During a Special Event all L2Attackable can drop extra Items.
	 * Those extra Items are defined in the table <B>allNpcDateDrops</B> of the EventDroplist.
	 * Each Special Event has a start and end date to stop to drop extra Items automaticaly. <BR><BR>
	 *
	 * <B><U> Actions</U> : </B><BR><BR>
	 * <li>Manage drop of Special Events created by GM for a defined period </li>
	 * <li>Get all possible drops of this L2Attackable from L2NpcTemplate and add it Quest drops</li>
	 * <li>For each possible drops (base + quests), calculate which one must be dropped (random) </li>
	 * <li>Get each Item quantity dropped (random) </li>
	 * <li>Create this or these L2ItemInstance corresponding to each Item Identifier dropped</li>
	 * <li>If the autoLoot mode is actif and if the L2Character that has killed the L2Attackable is a L2PcInstance, give this or these Item(s) to the L2PcInstance that has killed the L2Attackable</li>
	 * <li>If the autoLoot mode isn't actif or if the L2Character that has killed the L2Attackable is not a L2PcInstance, add this or these Item(s) in the world as a visible object at the position where mob was last</li><BR><BR>
	 *
	 * @param lastAttacker The L2Character that has killed the L2Attackable
	 */
	public void doItemDrop(L2Character lastAttacker)
	{
		 L2PcInstance player = null;
		 if (lastAttacker instanceof L2PcInstance)
			 player = (L2PcInstance)lastAttacker;
		 else if (lastAttacker instanceof L2Summon)
			 player = ((L2Summon)lastAttacker).getOwner();

		 if (player == null) return; // Don't drop anything if the last attacker or ownere isn't L2PcInstance

		 int levelModifier = calculateLevelModifierForDrop(player);;		  // level modifier in %'s (will be subtracted from drop chance)

		// Manage drop of Special Events created by GM for a defined period
		doEventDrop(lastAttacker);


		// Create a table containing all possible drops of this L2Attackable from L2NpcTemplate
		ArrayList<L2DropData> drops = new ArrayList<L2DropData>();
		drops.addAll(getTemplate().getDropData());
		if (Config.DEBUG) _log.finer("this npc has "+drops.size()+" drops defined");


		// Add Quest drops to the table containing all possible drops of this L2Attackable
		player.fillQuestDrops(this, drops);
		
		// Go Throw all possible drops (base + quests) of this L2Attackable
		for (L2DropData drop : drops)
		{
			 if (drop == null) continue;
			 
			// If it's a sweep drop, it will not appear on the ground
			 if (drop.isSweep()) continue;

             dropItem(player, drop.getItemId(), calculateDropCount(player, drop, levelModifier));
		}
	}

	/**
	 * Manage Sweep drops of L2Attackable.<BR><BR>
	 *
	 * <B><U> Actions</U> : </B><BR><BR>
	 * <li>Get all possible drops of this L2Attackable from L2NpcTemplate</li>
	 * <li>For each possible drops (base including sweep), calculate which one can is spoiled (random) </li>
	 * <li>Get each Item spoiled quantity (random) </li>
	 * <li>Create this or these L2ItemInstance corresponding to each Item Identifier spoiled</li>
	 * <li>Add each of those L2ItemInstance to the table _sweepItems of this L2Attackable containing all Items that can be spoiled </li><BR><BR>
	 *
	 * @param lastAttacker The L2Character that has killed the L2Attackable
	 */
	public void doSweepDrop(L2Character lastAttacker)
	{

		// Create a table containing all possible drops of this L2Attackable from L2NpcTemplate
		List<L2DropData> drops = getTemplate().getDropData();
		if (Config.DEBUG) _log.finer("this npc has "+drops.size()+" drops defined");

		// Create a table that will contain all Items that can be spoiled
		ArrayList<L2ItemInstance> spoiled = new ArrayList<L2ItemInstance>();

		// If DeepBlue Drop Rule is enabled, calculate the drop modifiers
		int deepBlueDropAdena = 1;   // Factor to divide the drop chance for Adenas drops
		int deepBlueDropOther = 1;   // Factor to divide the drop chance for Others drops
		int levelModifier = 0;	 // level modifier in %'s (will be subtracted from drop chance)
		if (Config.DEEPBLUE_DROP_RULES)
		{
			if (lastAttacker instanceof L2PcInstance) {
				L2PcInstance player = (L2PcInstance)lastAttacker;
				// According to official data (Prima),
				// deep blue mobs are 9 or more levels below players
				if ((player.getLevel() - 9) >= getLevel()) {
					// We should multiply by the server's drop rate,
					// so we always get a low chance of drop for deep blue mobs.
					// NOTE: This is valid only for adena drops! Others drops will still obey server's rate
					deepBlueDropAdena = Math.round(3 * Config.RATE_DROP_SPOIL);
					deepBlueDropOther = 3;
					levelModifier = ((player.getLevel() - (getLevel()+8)) * 9);
				}
			}
		}

		// Go Throw all possible drops (base including sweep) of this L2Attackable
		for (Iterator<L2DropData> iter = drops.iterator(); iter.hasNext();)
		{
			L2DropData drop = iter.next();

			// If it's not a sweep drop, it will not appear on the ground
			if (!drop.isSweep())
				continue;

			// Roll the dice.. between 0 and MAX_CHANCE (=1000000)
			int roll = Rnd.get(L2DropData.MAX_CHANCE);

			// Get default drop chance
			float dropChance = drop.getChance()*Config.RATE_DROP_SPOIL;

			// Check our rates... Is it an Adena drop or is it any other drop?...
			int deepBlueDrop = 1;
			if (drop.getItemId() == 57)
			{
				deepBlueDrop = deepBlueDropAdena;
			}
			else
			{
				deepBlueDrop = deepBlueDropOther;
			}

			// Check if we should apply our maths so deep blue mobs will not drop that easy
			if (Config.DEEPBLUE_DROP_RULES)
			{
				dropChance = ((drop.getChance() - ((drop.getChance() * levelModifier)/100)) * (Config.RATE_DROP_SPOIL) / deepBlueDrop);
			}

			// Round drop chance
			dropChance = Math.round(dropChance);

			// Set our limits for chance of drop
			if (dropChance < 1) dropChance = 1;
			if (dropChance > L2DropData.MAX_CHANCE) dropChance = L2DropData.MAX_CHANCE;

			// Check if the Item must be dropped
			if (roll < dropChance)
			{
				// Create the L2ItemInstance corresponding to the Item Identifier and add it to _allObjects of L2world
				L2ItemInstance dropit = ItemTable.getInstance().createItem(drop.getItemId());

				// Get min and max Item quantity that can be dropped in one time
				int min = drop.getMinDrop();
				int max = drop.getMaxDrop();

				int itemCount = 0;

				// Get the item quantity dropped
				if (min < max)
				{
					// Get RND between [min and max] to get range between min and max...
					itemCount = Rnd.get(min, max);
				}
				else
				{
					itemCount = 1;
				}

				// Calculate the Adena reward with the RATE_DROP_ADENA
				if (dropit.getItemId() == 57)
				{
					itemCount *= Config.RATE_DROP_ADENA;
				}

				if (itemCount != 0)
				{
					// Set the Item quantity dropped
					dropit.setCount(itemCount);

					if (Config.DEBUG)
						_log.fine("Item id to spoil: "+drop.getItemId()+" amount: "+dropit.getCount());

					// Add the L2ItemInstance to the table containing all Items spoiled
					spoiled.add(dropit);
				}
				else
				{
					_log.fine("Roll produced 0 items to drop... Cancelling");
				}
			}
		}

		// Set the table _sweepItems of this L2Attackable
		if (spoiled.size() > 0)
			_sweepItems = spoiled.toArray(new L2ItemInstance[spoiled.size()]);

	}



	/**
	 * Return the active weapon of this L2Attackable (= null).<BR><BR>
	 */
	public L2ItemInstance getActiveWeapon()
	{
		return null;
	}

	/**
	 * Return True if the _aggroList of this L2Attackable is Empty.<BR><BR>
	 */
	public boolean noTarget()
	{
		return _aggroList==null || _aggroList.isEmpty();
	}

	/**
	 * Return True if the _aggroList of this L2Attackable contains the L2Character.<BR><BR>
	 *
	 * @param player The L2Character searched in the _aggroList of the L2Attackable
	 *
	 */
	public boolean containsTarget(L2Character player)
	{
		if (_aggroList == null)
			return false;
		return _aggroList.containsKey(player);
	}

	/**
	 * Clear the _aggroList of the L2Attackable.<BR><BR>
	 */
	public void clearAggroList()
	{
		_aggroList = null;
	}

	/**
	 * Return True if a Dwarf use Sweep on the L2Attackable and if item can be spoiled.<BR><BR>
	 */
	public boolean isSweepActive()
	{
		return _sweepItems != null;
	}

	/**
	 * Return table containing all L2ItemInstance that can be spoiled.<BR><BR>
	 */
	public synchronized L2ItemInstance[] takeSweep() 
	{
		L2ItemInstance[] sweep = _sweepItems;

		_sweepItems = null;

		return sweep;
	}

	/**
	 * Activate the absorbed soul condition on the L2Attackable.<BR><BR>
	 */
	public void absorbSoul()
	{
		_absorbed = true;

	}

	/**
	 * Return True if the L2Attackable had his soul absorbed.<BR><BR>
	 */
	public boolean isAbsorbed()
	{
		return _absorbed;
	}

	/**
	 * Adds an attacker that successfully absorbed the soul of this L2Attackable into the _absorbersList.<BR><BR>
	 *
	 * params:	attacker	- a valid L2PcInstance
	 * 			condition	- an integer indicating the event when mob dies. This should be:
	 * 							= 0		- "the crystal scatters";
	 * 							= 1		- "the crystal failed to absorb. nothing happens";
	 * 							= 2		- "the crystal resonates because you got more than 1 crystal on you";
	 * 							= 3		- "the crystal cannot absorb the soul because the mob level is too low";
	 * 							= 4		- "the crystal successfuly absorbed the soul";
	 */
	public void addAbsorber(L2PcInstance attacker, int crystalId)
	{
		// This just works for targets like L2MonsterInstance
		if (!(this instanceof L2MonsterInstance))
			return;

		// The attacker must not be null
		if (attacker == null)
			return;

		// This L2Attackable must be of one type in the _absorbingMOBS_levelXX tables.
		// OBS: This is done so to avoid triggering the absorbed conditions for mobs that can't be absorbed.
		boolean validTarget = false;
		for (int mobId : _absorbingMOBS_level4)
		{
			if (getNpcId() == mobId)
			{
				validTarget = true;
				break;
			}
		}
		if (!validTarget)
		{
			for (int mobId : _absorbingMOBS_level8)
			{
				if (getNpcId() == mobId)
				{
					validTarget = true;
					break;
				}
			}
		}
		if (!validTarget)
		{
			for (int mobId : _absorbingMOBS_level10)
			{
				if (getNpcId() == mobId)
				{
					validTarget = true;
					break;
				}
			}
		}
		if (!validTarget)
		{
			for (int mobId : _absorbingMOBS_level12)
			{
				if (getNpcId() == mobId)
				{
					validTarget = true;
					break;
				}
			}
		}
		if (!validTarget)
			return;

		// If we have no _absorbersList initiated, do it
		AbsorberInfo ai = null;
		if (_absorbersList == null)
		{
			_absorbersList = new ConcurrentHashMap<L2PcInstance, AbsorberInfo>();
		}
		else
		{
			ai = _absorbersList.get(attacker);
		}

		// If the L2Character attacker isn't already in the _absorbersList of this L2Attackable, add it
		if (ai == null)
		{
			ai = new AbsorberInfo(attacker, crystalId, getCurrentHp());
			_absorbersList.put(attacker, ai);
		}
		else
		{
			ai.absorber = attacker;
			ai.crystalId = crystalId;
			ai.absorbedHP = getCurrentHp();
		}

		// Set this L2Attackable as absorbed
		absorbSoul();
	}

	/**
	 * Calculate the leveling chance of Soul Crystals based on the attacker that killed this L2Attackable
	 *
	 * @param attacker The player that last hitted (killed) this L2Attackable
	 *
	 */
	private void levelSoulCrystals(L2Character attacker)
	{
		// Only L2PcInstance can absorb a soul
		if (!(attacker instanceof L2PcInstance))
		{
			_absorbed = false;
			_absorbersList = null;
			return;
		}

		// Init some useful vars
		boolean success = false;
		boolean levelPartyCrystals = false;
		boolean canIncreaseCrystal = true;
		int thisMobId = getNpcId();
		L2PcInstance killer = (L2PcInstance)attacker;

		// Check if this L2Attackable isn't within any of the groups of mobs that can be absorbed
		int minCrystalLevel = 0;
		int maxCrystalLevel = 0;
		for (int mobId : _absorbingMOBS_level4) {
			if (thisMobId == mobId) {
				maxCrystalLevel = 4;
				success = true;
				break;
			}
		}
		if (!success) {
			for (int mobId : _absorbingMOBS_level8) {
				if (thisMobId == mobId) {
					maxCrystalLevel = 8;
					success = true;
					break;
				}
			}
		}
		if (!success) {
			for (int mobId : _absorbingMOBS_level10) {
				if (thisMobId == mobId) {
					maxCrystalLevel = 10;
					success = true;
					break;
				}
			}
		}
		if (!success) {
			for (int mobId : _absorbingMOBS_level12) {
				if (thisMobId == mobId) {
					minCrystalLevel = 10;
					maxCrystalLevel = 12;
					success = true;
					levelPartyCrystals = true;
					break;
				}
			}
		}

		// If this is not a valid L2Attackable, clears the _absorbersList and just return
		if (!success) {
			_absorbed = false;
			_absorbersList = null;
			return;
		}

		// If this mob is a boss, then skip some checkings
		success = true;
		if (!levelPartyCrystals)
		{
			// Fail if this L2Attackable isn't absorbed or there's no one in its _absorbersList
			if (!isAbsorbed() || _absorbersList == null)
			{
				_absorbed = false;
				_absorbersList = null;
				return;
			}

			// Fail if the killer isn't in the _absorbersList of this L2Attackable and mob is not boss
			AbsorberInfo ai = _absorbersList.get(killer);
			if (ai == null || ai.absorber.getObjectId() != killer.getObjectId())
				success = false;

			// Check if the soul crystal was used when HP of this L2Attackable wasn't higher than half of it
			if (ai != null && ai.absorbedHP > (getMaxHp()/2))
				success = false;

			if (!success) {
				_absorbed = false;
				_absorbersList = null;
				return;
			}
		}

		// Init some more info now
		int dice = Rnd.get(100);
		int crystalId = 0;
		int oldCrystalId = 0;
		int newCrystalId = 0;
		int crystalOffset = 0;
		int crystalsCount = 0;
		SystemMessage sm = new SystemMessage(614);
		success = false;

		// Now we got four choices:
		// 1- The Monster level is too low for the crystal. Nothing happens.
		// 2- Everything is correct, but it failed. Nothing happens. (57.5%)
		// 3- Everything is correct, but it failed. The crystal scatters. A sound event is played. (10%)
		// 4- Everything is correct, the crystal level up. A sound event is played. (32.5%)

		ArrayList<L2PcInstance> players = new ArrayList<L2PcInstance>();
		if (levelPartyCrystals && killer.isInParty())
		{
			players = killer.getParty().getPartyMembers();
		}
		else
		{
			players.add(killer);
		}
		for (L2PcInstance player : players)
		{
			// Check how many soul crystals the player has in his inventory and which soul crystal he has
			canIncreaseCrystal = true;
			success = true;
			final L2ItemInstance[] inv = player.getInventory().getItems();
			for (int index = 0; index < inv.length; index++)
			{
				final L2ItemInstance item = inv[index];
				if ( (((item.getItemId() >= _REDCRYSTAL_OFFSET) || (item.getItemId() >= _GREENCRYSTAL_OFFSET) || (item.getItemId() >= _BLUECRYSTAL_OFFSET)) &&
					 ((item.getItemId() <= _REDCRYSTAL_OFFSET+10) || (item.getItemId() <= _GREENCRYSTAL_OFFSET+10) || (item.getItemId() <= _BLUECRYSTAL_OFFSET+10))) ||
					 (item.getItemId() == _REDCRYSTAL_LVL11) || (item.getItemId() == _BLUECRYSTAL_LVL11) || (item.getItemId() == _GREENCRYSTAL_LVL11) ||
					 (item.getItemId() == _REDCRYSTAL_LVL12) || (item.getItemId() == _BLUECRYSTAL_LVL12) || (item.getItemId() == _GREENCRYSTAL_LVL12) )
				{
					crystalsCount++;
					if (crystalsCount > 1)
					{
						success = false;
						break;	// break the "findCrystals" loop, as we already found more than one crystal.
					}
					crystalId = item.getItemId();
					crystalOffset = 0;
					for (int crystalLevel = 0; crystalLevel <= _MAX_CRYSTALS_LEVEL; crystalLevel++)
					{
						// Get a crystal offset to calculate the new crystal ID
						if (crystalId == (_REDCRYSTAL_OFFSET + crystalLevel)) crystalOffset = _REDCRYSTAL_OFFSET;
						if (crystalId == (_GREENCRYSTAL_OFFSET + crystalLevel)) crystalOffset = _GREENCRYSTAL_OFFSET;
						if (crystalId == (_BLUECRYSTAL_OFFSET + crystalLevel)) crystalOffset = _BLUECRYSTAL_OFFSET;
						if (crystalLevel > 9)
						{
							if (crystalId == (_REDCRYSTAL_OFFSET+10)) crystalOffset = _REDCRYSTAL_LVL11 - (crystalLevel + 1);
							if (crystalId == (_GREENCRYSTAL_OFFSET+10)) crystalOffset = _GREENCRYSTAL_LVL11 - (crystalLevel + 1);
							if (crystalId == (_BLUECRYSTAL_OFFSET+10)) crystalOffset = _BLUECRYSTAL_LVL11 - (crystalLevel + 1);
							if (crystalId == (_REDCRYSTAL_LVL11)) crystalOffset = _REDCRYSTAL_LVL12 - (crystalLevel + 1);
							if (crystalId == (_GREENCRYSTAL_LVL11)) crystalOffset = _GREENCRYSTAL_LVL12 - (crystalLevel + 1);
							if (crystalId == (_BLUECRYSTAL_LVL11)) crystalOffset = _BLUECRYSTAL_LVL12 - (crystalLevel + 1);
						}
						// If the crystal level is way too high for this mob, say that we can't increase it
						if ((crystalLevel >= minCrystalLevel) && (crystalLevel < maxCrystalLevel))
						{
							canIncreaseCrystal = true;
						}
						else
						{
							canIncreaseCrystal = false;
						}
						// Define crystals' IDs based on the crystalOffset found
						if (crystalOffset > 0)
						{
							oldCrystalId = crystalId;
							newCrystalId = (crystalOffset + crystalLevel) + 1;
							break;   // Break the "findLevel" loop, as we already got the crystal Level.
						}
					}
				}
			}
			// The player has no crystals at all, reset our control vars and skip this player
			if (crystalsCount < 1)
			{
				oldCrystalId = 0;
				newCrystalId = 0;
				crystalOffset = 0;
				crystalsCount = 0;
				continue;   // The player doesn't have any crystals with him. Get to the next player.
			}
			// The player has more than one soul crystal with him, the crystal resonates and we skip this player
			if (!success)
			{
				sm.addString("The soul crystal resonated.");
				player.sendPacket(new PlaySound("ItemSound2.broken_key"));
				player.sendPacket(sm);

				oldCrystalId = 0;
				newCrystalId = 0;
				crystalOffset = 0;
				crystalsCount = 0;
				continue;   // The player failed to increase his crystal as he has more than one crystal.
			}
			// The soul crystal stage of the player is way too high, refuse to increase it
			if (!canIncreaseCrystal)
			{
				sm.addString("The soul crystal has refused to absorb the soul.");
				player.sendPacket(sm);

				oldCrystalId = 0;
				newCrystalId = 0;
				crystalOffset = 0;
				crystalsCount = 0;
				continue;   // The player failed to increase his crystal cuz mob is wrong for his crystal level.
			}
				// If the killer succeeds or it is a boss mob, level up the crystal
			if ((dice < _LEVEL_CHANCE) || levelPartyCrystals)
			{
				final L2ItemInstance oldCrystal = player.getInventory().getItemByItemId(oldCrystalId);
				final L2ItemInstance newCrystal = ItemTable.getInstance().createItem(newCrystalId);
				final SystemMessage sms = new SystemMessage(SystemMessage.EARNED_S1);
				// Add new crystal to the killer's inventory
				player.getInventory().addItem(newCrystal);
				// Remove old crystal from the killer's inventory
				player.removeItemFromInventory(oldCrystal, 1);
				// Send a sound event and text message to the player
				player.sendPacket(new PlaySound("ItemSound.quest_itemget"));
				sms.addItemName(newCrystalId);
				player.sendPacket(sms);
				// Send Server->Client packets to notify the killer and update his Inventory
				player.sendPacket(new ItemList(player, false));
			}
			else if ((dice < _BREAK_CHANCE) && !levelPartyCrystals)
			{
				// Remove old crystal from the killer's inventory
				final L2ItemInstance oldCrystal = player.getInventory().getItemByItemId(oldCrystalId);
				killer.removeItemFromInventory(oldCrystal, 1);
				killer.sendPacket(new PlaySound("ItemSound3.sys_enchant_failed"));
				sm.addString("The soul crystal scattered itself.");
				killer.sendPacket(sm);
				_absorbed = false;
				_absorbersList = null;
				return;
			}
			else
			{
				sm.addString("The soul crystal has failed to absorb the soul.");
				player.sendPacket(sm);
			}
		}
	}

	/**
	 * Calculate the Experience and SP to distribute to attacker (L2PcInstance, L2SummonInstance or L2Party) of the L2Attackable.<BR><BR>
	 *
	 * @param diff The difference of level between attacker (L2PcInstance, L2SummonInstance or L2Party) and the L2Attackable
	 * @param damage The damages given by the attacker (L2PcInstance, L2SummonInstance or L2Party)
	 *
	 */
	private int[] calculateExpAndSp(int diff, int damage)
	{
		double xp = ((double)getExpReward() / getMaxHp()) * damage;
		double sp = ((double)getSpReward() / getMaxHp()) * damage;

		// on each diff level penalty is 9%
		if(diff > 4)
		{
			double divider = Math.pow(0.91, diff);
			xp *= divider;
			sp *= divider;
		}

		if(xp < 0) xp = 0;
		if(sp < 0) sp = 0;

		return new int[] {(int)xp, (int)sp};
	}

	/**
	 * Return True.<BR><BR>
	 */
	public boolean isAttackable()
	{
		return true;
	}

	@Override
	public void OnSpawn()
	{
		// Clear mob spoil,seed
		setSpoil(false);
		_sweepItems = null;
		_absorbed = false;
		_absorbersList = null;
	}
	
	public synchronized L2ItemInstance[] takeHarvest()
	{
		 L2ItemInstance[] harvest = _harvestItems;
		 _harvestItems = null;
		 return harvest;
	}
	
	public void setSeeded(int id, int seederLvl)
	{
		_seeded = true;
		_seedType = id;
		int count = 1;

		int diff = (getLevel() - seederLvl - 1);

		// hi-lvl mobs bonus
		if (diff > 0 && diff < 5)//Config.MANOR_HARVEST_DIFF_BONUS)
			count += Rnd.get(diff);

		List<L2ItemInstance> harvested = new FastList<L2ItemInstance>();

		for (int i = 0; i < count; i++)
			harvested.add(ItemTable.getInstance().createItem(L2Manor.getInstance().getCropType(_seedType)));
		

		_harvestItems = harvested.toArray(new L2ItemInstance[harvested.size()]);
	}

	public boolean isSeeded()
	{
		return _seeded;
	}

	@SuppressWarnings("unused")
	private boolean isSeededHigh()
	{
		return (_seedType < 5650); // low-grade seeds has id's below 5650
	}
}
