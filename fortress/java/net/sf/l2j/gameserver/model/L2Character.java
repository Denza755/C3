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
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.ClientScheduler;
import net.sf.l2j.gameserver.GameServer;
import net.sf.l2j.gameserver.GameTimeController;
import net.sf.l2j.gameserver.SkillTable;
import net.sf.l2j.gameserver.ai.CtrlEvent;
import net.sf.l2j.gameserver.ai.CtrlIntention;
import net.sf.l2j.gameserver.ai.L2CharacterAI;
import net.sf.l2j.gameserver.handler.ISkillHandler;
import net.sf.l2j.gameserver.handler.SkillHandler;
import net.sf.l2j.gameserver.instancemanager.SiegeManager;
import net.sf.l2j.gameserver.lib.Rnd;
import net.sf.l2j.gameserver.model.L2Skill.SkillTargetType;
import net.sf.l2j.gameserver.model.L2Skill.SkillType;
import net.sf.l2j.gameserver.model.quest.QuestState;
import net.sf.l2j.gameserver.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.serverpackets.Attack;
import net.sf.l2j.gameserver.serverpackets.ChangeMoveType;
import net.sf.l2j.gameserver.serverpackets.ChangeWaitType;
import net.sf.l2j.gameserver.serverpackets.MagicEffectIcons;
import net.sf.l2j.gameserver.serverpackets.MagicSkillCanceld;
import net.sf.l2j.gameserver.serverpackets.MagicSkillLaunched;
import net.sf.l2j.gameserver.serverpackets.MagicSkillUser;
import net.sf.l2j.gameserver.serverpackets.MyTargetSelected;
import net.sf.l2j.gameserver.serverpackets.PartySpelled;
import net.sf.l2j.gameserver.serverpackets.PetInfo;
import net.sf.l2j.gameserver.serverpackets.Revive;
import net.sf.l2j.gameserver.serverpackets.ServerBasePacket;
import net.sf.l2j.gameserver.serverpackets.SetupGauge;
import net.sf.l2j.gameserver.serverpackets.SocialAction;
import net.sf.l2j.gameserver.serverpackets.StatusUpdate;
import net.sf.l2j.gameserver.serverpackets.StopMove;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.serverpackets.TargetUnselected;
import net.sf.l2j.gameserver.serverpackets.TeleportToLocation;
import net.sf.l2j.gameserver.serverpackets.UserInfo;
import net.sf.l2j.gameserver.skills.Calculator;
import net.sf.l2j.gameserver.skills.Env;
import net.sf.l2j.gameserver.skills.Formulas;
import net.sf.l2j.gameserver.skills.Func;
import net.sf.l2j.gameserver.skills.Stats;
import net.sf.l2j.gameserver.templates.L2CharTemplate;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;
import net.sf.l2j.gameserver.templates.L2Weapon;
import net.sf.l2j.gameserver.templates.L2WeaponType;
import ru.balancer.JBForth;
import ru.balancer.jbforth.Word;

/**
 * Mother class of all character objects of the world (PC, NPC...)<BR><BR>
 *
 * L2Character :<BR><BR>
 * <li>L2CastleGuardInstance</li>
 * <li>L2DoorInstance</li>
 * <li>L2NpcInstance</li>
 * <li>L2PlayableInstance </li><BR><BR>
 *
 *
 * <B><U> Concept of L2CharTemplate</U> :</B><BR><BR>
 * Each L2Character owns generic and static properties (ex : all Keltir have the same number of HP...).
 * All of those properties are stored in a different template for each type of L2Character.
 * Each template is loaded once in the server cache memory (reduce memory use).
 * When a new instance of L2Character is spawned, server just create a link between the instance and the template.
 * This link is stored in <B>_template</B><BR><BR>
 *
 *
 * @version $Revision: 1.53.2.45.2.34 $ $Date: 2005/04/11 10:06:08 $
 */
public abstract class L2Character extends L2Object
{
	protected static final Logger _log = Logger.getLogger(L2Character.class.getName());

	public static final int ABNORMAL_EFFECT_BLEEDING   = 0x0001; // not sure
	public static final int ABNORMAL_EFFECT_POISON	 = 0x0002;
	public static final int ABNORMAL_EFFECT_UNKNOWN_3  = 0x0004;
	public static final int ABNORMAL_EFFECT_UNKNOWN_4  = 0x0008;
	public static final int ABNORMAL_EFFECT_UNKNOWN_5  = 0x0010;
	public static final int ABNORMAL_EFFECT_UNKNOWN_6  = 0x0020;
	public static final int ABNORMAL_EFFECT_STUN	   = 0x0040;
	public static final int ABNORMAL_EFFECT_SLEEP  	   = 0x0080;
	public static final int ABNORMAL_EFFECT_MUTED	  = 0x0100; // not sure
	public static final int ABNORMAL_EFFECT_ROOT	   = 0x0200;
	public static final int ABNORMAL_EFFECT_HOLD_1	 = 0x0400;
	public static final int ABNORMAL_EFFECT_HOLD_2	 = 0x0800;
	public static final int ABNORMAL_EFFECT_UNKNOWN_13 = 0x1000;
	public static final int ABNORMAL_EFFECT_BIG_HEAD   = 0x2000;
	public static final int ABNORMAL_EFFECT_FLAME	  = 0x4000;
	public static final int ABNORMAL_EFFECT_UNKNOWN_16 = 0x8000;
	// FIXME : correct the value of the abnormal effect confused
	public static final int ABNORMAL_EFFECT_CONFUSED   = 0x0020;
	// FIXME : correct the value of the abnormal effect affraid
	public static final int ABNORMAL_EFFECT_AFFRAID	= 0x0010;

	public JBForth jbf;
    private boolean _IsTeleporting = false;

	/** This class permit to the L2Character AI to obtain informations and uses L2Character method */
	public class AIAccessor
	{
		protected AIAccessor() {}

		/**
		 * Return the L2Character managed by this Accessor AI.<BR><BR>
		 */
		public L2Character getActor()
		{
			return L2Character.this;
		}

		/**
		 * Accessor to L2Character moveToLocation() method with an interaction area.<BR><BR>
		 */
		public void moveTo(int x, int y, int z, int offset)
		{
			L2Character.this.moveToLocation(x, y, z, offset);
		}

		/**
		 * Accessor to L2Character moveToLocation() method without interaction area.<BR><BR>
		 */
		public void moveTo(int x, int y, int z)
		{
			L2Character.this.moveToLocation(x, y, z, 0);
		}

		/**
		 * Accessor to L2Character stopMove() method.<BR><BR>
		 */
		public void stopMove(L2CharPosition pos)
		{
			L2Character.this.stopMove(pos);
		}

		/**
		 * Accessor to L2Character doAttack() method.<BR><BR>
		 */
		public void doAttack(L2Character target)
		{
			L2Character.this.doAttack(target);
		}

		/**
		 * Accessor to L2Character doCast() method.<BR><BR>
		 */
		public void doCast(L2Skill skill)
		{
			L2Character.this.doCast(skill);
		}

		/**
		 * Create a NotifyAITask.<BR><BR>
		 */
		public NotifyAITask newNotifyTask(CtrlEvent evt)
		{
			return new NotifyAITask(evt);
		}

		/**
		 * Cancel the AI.<BR><BR>
		 */
		public void detachAI()
		{
			L2Character.this._ai = null;
			jbf = null;
		}
	}

	/** Task of AI notification */
	public class NotifyAITask implements Runnable
	{
		private final CtrlEvent _evt;

		NotifyAITask(CtrlEvent evt)
		{
			this._evt = evt;
		}

		public void run()
		{
			try
			{
				L2Character.this.notifyEvent(_evt, null, null);
			}
			catch (Throwable t)
			{
				_log.warning("Exception "+t);
				t.printStackTrace();
			}
		}
	}

	/**
	 * Task lauching the function onHitTimer().<BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>If the attacker/target is dead or use fake death, notify the AI with EVT_CANCEL and send a Server->Client packet ActionFailed (if attacker is a L2PcInstance)</li>
	 * <li>If attack isn't aborted, send a message system (critical hit, missed...) to attacker/target if they are L2PcInstance </li>
	 * <li>If attack isn't aborted and hit isn't missed, reduce HP of the target and calculate reflection damage to reduce HP of attacker if necessary </li>
	 * <li>if attack isn't aborted and hit isn't missed, manage attack or cast break of the target (calculating rate, sending message...) </li><BR><BR>
	 *
	 */
	class HitTask implements Runnable
	{
		L2Character _hitTarget;
		int _damage;
		boolean _crit;
		boolean _miss;
		boolean _shld;
		boolean _soulshot;

		public HitTask(L2Character target, int damage, boolean crit, boolean miss, boolean soulshot, boolean shld)
		{
			_hitTarget = target;
			_damage = damage;
			_crit = crit;
			_shld = shld;
			_miss = miss;
			_soulshot = soulshot;
		}

		public void run()
		{
			try
			{
				onHitTimer(_hitTarget, _damage, _crit, _miss, _soulshot, _shld);
			}
			catch (Throwable e)
			{
				_log.log(Level.SEVERE, "", e);
			}
		}
	}

	/** Task of HP/MP/CP regeneration */
	class RegenTask implements Runnable
	{

		public void run()
		{
			try
			{
				double addHp = 0;
				double addMp = 0;

				// Added regen bonus when character is sitting
				if ((L2Character.this instanceof L2PcInstance) && Config.REGEN_SIT_WAIT)
				{
				  if ( ((L2PcInstance)L2Character.this).isSitting() )
				  {
					((L2PcInstance)L2Character.this).updateWaitSitTime();
					if(((L2PcInstance)L2Character.this).getWaitSitTime()>5)
					{
					  addHp = ((L2PcInstance)L2Character.this).getWaitSitTime();
					  addMp = ((L2PcInstance)L2Character.this).getWaitSitTime();
					}
				  }
				}

				// Caculate the HP regen rate for this L2Character
				if(getCurrentHp() < getMaxHp())
					addHp += Formulas.getInstance().calcHpRegen(L2Character.this);

				// Caculate the HP regen rate for this L2Character
				if(getCurrentMp() < getMaxMp())
					addMp += Formulas.getInstance().calcMpRegen(L2Character.this);

				// Modify the current HP and MP of the L2Character and broadcast Server->Client packet StatusUpdate
				setCurrentHpMp(getCurrentHp() + addHp, getCurrentMp() + addMp);

				if(getCurrentCp() < getMaxCp())
				{
					// Caculate the CP regen rate for this L2Character
					double addCp = Formulas.getInstance().calcCpRegen(L2Character.this);

					// Modify the current CP of the L2Character and broadcast Server->Client packet StatusUpdate
					setCurrentCp(getCurrentCp() + addCp);
				}
			}
			catch (Throwable e)
			{
				_log.log(Level.SEVERE, "", e);
			}
		}
	}

	/** Task lauching the function onMagicUseTimer() */
	class MagicUseTask implements Runnable
	{
		L2Object[] _targets;
		L2Object _target;
		L2Object[] _targetsNew;
		L2Skill _skill;
		L2Character _activeChar;

		public MagicUseTask(L2Object[] targets, L2Skill skill, L2Character activeChar)
		{
			_targets = targets;
			_skill = skill;
			_activeChar=activeChar;
			_target = targets[0];
		}

		public void run()
		{
			try
			{
				if (Config.REBUILD_CAST_TARGETS_LIST == true)
				{//rebuild targes list
					_targetsNew = _skill.getTargetList((L2Character)(_activeChar),_target);
				if (_targetsNew != null)
					onMagicUseTimer(_targetsNew, _skill);
				}
				else
				onMagicUseTimer(_targets, _skill);
			}
			catch (Throwable e)
			{
				_log.log(Level.SEVERE, "", e);
			}
		}
	}

	/** Task lauching the function enableSkill() */
	class EnableSkill implements Runnable
	{
		int _skillId;

		public EnableSkill(int skillId)
		{
			_skillId = skillId;
		}

		public void run()
		{
			try
			{
				enableSkill(_skillId);
			} catch (Throwable e) {
				_log.log(Level.SEVERE, "", e);
			}
		}
	}

	/** Task lauching the function stopPvPFlag() */
	class PvPFlag implements Runnable
	{
		public void run()
		{
			try
			{
				// _log.fine("Checking pvp time: " + getlastPvpAttack());
				// "lastattack: " _lastAttackTime "currenttime: "
				// System.currentTimeMillis());
				if(Math.abs(System.currentTimeMillis()-getlastPvpAttack()) > Config.PVP_TIME)
				{
					//  _log.fine("Stopping PvP");
					stopPvPFlag();
				}
				else if(Math.abs(System.currentTimeMillis()-getlastPvpAttack()) > Config.PVP_TIME - 5000)
				{
					updatePvPFlag(2);
				}
				else
				{
					updatePvPFlag(1);
					// Start a new PvP timer check
					//checkPvPFlag();
				}
			}
			catch (Exception e)
			{
				_log.log(Level.WARNING, "error in pvp flag task:", e);
			}
		}
	}

	/** Task lauching the function onDecay() */
	class DecayTask implements Runnable
	{
		public void run()
		{
			try
			{
				onDecay();
			} catch (Throwable e) {
				_log.log(Level.SEVERE, "", e);
			}
		}
	}

	/**
	 * This class group all mouvement data.<BR><BR>
	 *
	 * <B><U> Data</U> :</B><BR><BR>
	 * <li>_moveTimestamp : Last time position update</li>
	 * <li>_xDestination, _yDestination, _zDestination : Position of the destination</li>
	 * <li>_xMoveFrom, _yMoveFrom, _zMoveFrom  : Position of the origin</li>
	 * <li>_moveStartTime : Start time of the movement</li>
	 * <li>_ticksToMove : Nb of ticks between the start and the destination</li>
	 * <li>_xSpeedTicks, _ySpeedTicks : Speed in unit/ticks</li><BR><BR>
	 *
	 * */
	static class MoveData
	{
		// when we retrieve x/y/z we use GameTimeControl.getGameTicks()
		// if we are moving, but move timestamp==gameticks, we don't need
		// to recalculate position
		int _moveTimestamp;
		int _xDestination;
		int _yDestination;
		int _zDestination;
		int _xMoveFrom;
		int _yMoveFrom;
		int _zMoveFrom;
		int _moveStartTime;
		int _ticksToMove;
		float _xSpeedTicks;
		float _ySpeedTicks;
	}


	/** The link on the L2CharTemplate object containing generic and static properties of this L2Character type (ex : Max HP, Speed...) */
	protected L2CharTemplate _template;

	/** Table containing all skillId that are disabled */
	protected List<Integer> _disabledSkills;
	private long _allSkillsDisabled = 0;

	/** HashMap(Integer, L2Skill) containing all skills of the L2Character */
	protected final HashMap<Integer, L2Skill> _skills;

	/** Array containing all clients that need to be notified about hp/mp updates of the L2Character */
	private CopyOnWriteArraySet<L2Character> _statusListener;

	private Future _regTask;
	private int	_flagsRegenActive;
	private static final int HP_REGEN_FLAG = 1;
	private static final int MP_REGEN_FLAG = 2;
	private static final int CP_REGEN_FLAG = 4;

	private String   _name;
	protected String _title;

	/** Current HP of the L2Character */
	private double   _currentHp;

	/** Current MP of the L2Character */
	private double   _currentMp;

	/** Current CP of the L2Character */
	private double   _currentCp;

	private boolean  _running;
	private boolean  _flying = false; //Is flying Wyvern?
	private boolean  _ridding = false; //Is Ridding strider?

//	private int _flyingRunSpeed;
//	private int _floatingWalkSpeed;
//	private int _flyingWalkSpeed;
//	private int _floatingRunSpeed;

	/** Movement data of this L2Character */
	private MoveData _move;

	/** Attacking L2Character (the L2Character that attacks this one) */
	private L2Character _attackingChar;

	/** Attacking L2Character's Skill */
	private L2Skill _attackingCharSkill;

	/** List of all L2Character instance that attacked (would be used for quest) */
	private ArrayList<L2Character> _AttackByList = new ArrayList<L2Character>();

    /** List of all QuestState instance that needs to be notified of this character's death */
    private ArrayList<QuestState> _NotifyQuestOfDeathList = new ArrayList<QuestState>();

	/** Orientation of the L2Character */
	private int _heading;

	/** Table EMPTY_EFFECTS shared by all L2Character without effects in progress */
	private static final L2Effect[] EMPTY_EFFECTS = new L2Effect[0];

	/**
	 * HashMap containing all active skills effects in progress of a L2Character.
	 * The Integer key of this HashMap is the L2Skill Identifier that has created the effect.
	 */
	private ConcurrentHashMap<Integer,L2Effect> _effects;

	/** The table containing the List of all stacked effect in progress for each Stack group Identifier */
	protected HashMap<String, LinkedList<Integer>> _stackedEffects;

	/** L2Charcater targeted by the L2Character */
	private L2Object _target;

	// setted by the start of casting, in game ticks
	private int	 _castEndTime;
	private int	 _castInterruptTime;

	private boolean _killedAlready;

	// setted by the start of attack, in game ticks
	private int	 _attackEndTime;
	private int	 _attacking;
	private int	 _disableBowAttackEndTime;

	private boolean _paralyzed;
	/** Cannot move/attack until stun timed out */
	private boolean _stuned;
	/** Cannot move until root timed out */
	private boolean _rooted;
	/** Cannot move/attack until sleep timed out or monster is attacked */
	private boolean _sleeping;
	/** Fake death */
	private boolean _fake_death;
	/** Cannot use magic */
	private boolean _muted;
	/** Attack anyone randomly */
	private boolean _confused;
	/** Flee in a random direction */
	private boolean _affraid;
	/** the char is carrying too much */
	private boolean _overloaded;
	/** Map 16 bits (0x0000) containing all abnormal effect in progress */
	private short   _abnormalEffects;

	private boolean   _imobilised;

	/** Table of calculators containing all standard NPC calculator (ex : ACCURACY_COMBAT, EVASION_RATE */
	private static final Calculator[] NPC_STD_CALCULATOR;
	static {NPC_STD_CALCULATOR = Formulas.getInstance().getStdNPCCalculators();}

	/** Table of Calculators containing all used calculator */
	private Calculator[] _calculators;

	protected L2CharacterAI _ai;
	
	private boolean _isAttacked;
	protected int _lastAttackChange;
	
	/** Future Skill Cast */
	private Future _skillCast;

	/** Char Coords from Client */
	private int _clientX;
	private int _clientY;
	private int _clientZ;
	private int _clientHeading;
    
	/**
	 * Constructor of L2Character.<BR><BR>
	 *
	 * <B><U> Concept</U> :</B><BR><BR>
	 * Each L2Character owns generic and static properties (ex : all Keltir have the same number of HP...).
	 * All of those properties are stored in a different template for each type of L2Character.
	 * Each template is loaded once in the server cache memory (reduce memory use).
	 * When a new instance of L2Character is spawned, server just create a link between the instance and the template
	 * This link is stored in <B>_template</B><BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Set the _template of the L2Character </li>
	 * <li>Set _overloaded to false (the charcater can take more items)</li><BR><BR>
	 *
	 * <li>If L2Character is a L2NPCInstance, copy skills from template to object</li>
	 * <li>If L2Character is a L2NPCInstance, link _calculators to NPC_STD_CALCULATOR</li><BR><BR>
	 *
	 * <li>If L2Character is NOT a L2NPCInstance, create an empty _skills slot</li>
	 * <li>If L2Character is a L2PcInstance or L2Summon, copy basic Calculator set to object</li><BR><BR>
	 *
	 * @param objectId Identifier of the object to initialized
	 * @param template The L2CharTemplate to apply to the object
	 */
	public L2Character(int objectId, L2CharTemplate template)
	{
		super(objectId);

		// Set its template to the new L2Character
		_template = template;

		// Set state of overloaded (the charcater can take more items)
		_overloaded = false;
		
		_isAttacked = false;
		
		_lastAttackChange = 0;

		if(template != null && this instanceof L2NpcInstance)
		{
			// Copy the Standard Calcultors of the L2NPCInstance in _calculators
			_calculators = NPC_STD_CALCULATOR;

			// Copy the skills of the L2NPCInstance from its template to the L2Character Instance
			// The skills list can be affected by spell effects so it's necessary to make a copy
			// to avoid that a spell affecting a L2NPCInstance, affects others L2NPCInstance of the same type too.
			_skills = ((L2NpcTemplate)template).getSkills();

			if(_skills != null)
				for(L2Skill skill : _skills.values())
					addStatFuncs(skill.getStatFuncs(null, this));
			//Formulas.getInstance().addFuncsToNewCharacter(this);
		}
		else
		{
			// Initialize the HashMap _skills to null
			_skills = new HashMap<Integer,L2Skill>();

			// If L2Character is a L2PcInstance or a L2Summon, create the basic calculator set
			_calculators = new Calculator[Stats.NUM_STATS];
			Formulas.getInstance().addFuncsToNewCharacter(this);
		}

	}


	/**
	 * Return the template of the L2Character.<BR><BR>
	 *
	 * <B><U> Concept</U> :</B><BR><BR>
	 * Each L2Character owns generic and static properties (ex : all Keltir have the same number of HP...).
	 * All of those properties are stored in a different template for each type of L2Character.
	 * Each template is loaded once in the server cache memory (reduce memory use).
	 * When a new instance of L2Character is spawned, server just create a link between the instance and the template
	 * This link is stored in <B>_template</B><BR><BR>
	 *
	 */
	public L2CharTemplate getTemplate()
	{
		return _template;
	}

	/**
	 * Set the template of the L2Character.<BR><BR>
	 *
	 * <B><U> Concept</U> :</B><BR><BR>
	 * Each L2Character owns generic and static properties (ex : all Keltir have the same number of HP...).
	 * All of those properties are stored in a different template for each type of L2Character.
	 * Each template is loaded once in the server cache memory (reduce memory use).
	 * When a new instance of L2Character is spawned, server just create a link between the instance and the template
	 * This link is stored in <B>_template</B><BR><BR>
	 *
	 * <B><U> Assert </U> :</B><BR><BR>
	 * <li> this instanceof L2Character</li><BR><BR
	 *
	 */
	protected final void setTemplate(L2CharTemplate template)
	{
		_template = template;
	}


	/**
	 * Set _attackingChar to the L2Character that attacks this one.<BR><BR>
	 *
	 * @param player The L2Character that attcks this one
	 *
	 */
	public void setAttackingChar (L2Character player)
	{
		if(player == null || player == this)
			return;

		_attackingChar = player;
	}

	/**
	 * Set _attackingCharSkill to the L2Skill used against this L2Character.<BR><BR>
	 *
	 * @param skill The L2Skill used against this L2Character
	 *
	 */
	public void setAttackingCharSkill (L2Skill skill)
	{
		_attackingCharSkill = skill;
	}

	/**
	 * Add L2Character instance that is attacking to the attacker list.<BR><BR>
	 *
	 * @param player The L2Character that attcks this one
	 *
	 */
	public void addAttackerToAttackByList (L2Character player)
	{
		if(player == null || player == this || _AttackByList.contains(player))
			return;

		_AttackByList.add(player);
	}

	/**
	 * Return a list of L2Character that attacked.<BR><BR>
	 */
    public final ArrayList<L2Character> getAttackByList()
	{
		if(_AttackByList == null)
			_AttackByList = new ArrayList<L2Character>();

		return _AttackByList;
	}

	/**
     * Add QuestState instance that is to be notified of character's death.<BR><BR>
     *
     * @param qs The QuestState that subscribe to this event
     *
     */
    public void addNotifyQuestOfDeath (QuestState qs)
    {
        if(qs == null || _NotifyQuestOfDeathList.contains(qs))
            return;

        _NotifyQuestOfDeathList.add(qs);
    }

    /**
     * Return a list of L2Character that attacked.<BR><BR>
     */
    public final ArrayList<QuestState> getNotifyQuestOfDeath ()
    {
        if(_NotifyQuestOfDeathList == null)
            _NotifyQuestOfDeathList = new ArrayList<QuestState>();

        return _NotifyQuestOfDeathList;
    }

	/**
	 * Return the L2CharacterAI of the L2Character and if its null create a new one.<BR><BR>
	 */
	public L2CharacterAI getAI()
	{
		return setAI(new L2CharacterAI(new AIAccessor()));
	}

	public L2CharacterAI setAI(L2CharacterAI new_ai)
	{
		if(new_ai == null)
			jbf = null;
		else if(jbf == null)
			jbf = new JBForth(this);

		if(_ai != null)
			return _ai;

//		if(Config.DEBUG)
//			_log.info("Set AI for " + this + "; vis="+isVisible()+"; players="+getCurrentWorldRegion().isKnownPlayers());

		return _ai = new_ai;
	}

	/**
	 * Return True if the L2Character has a L2CharacterAI.<BR><BR>
	 */
	public boolean hasAI()
	{
		return _ai != null;
	}

	public final boolean checkClean()
	{
		if(isKnownPlayers() && L2World.findObject(_objectId)!=null)
			return false;

		setAI(null);
		return true;
	}

	public final void notifyEvent(CtrlEvent evt, Object arg0, Object arg1)
	{
		if(Config.DEBUG)
			_log.info(this+".notifyEvent("+evt+","+arg0+","+arg1+"); ai="+_ai);

		if(checkClean())
			return;

		L2CharacterAI ai = getAI();
		if(ai == null)
		{
			_log.warning("Null AI char '"+this+"'for notify "+evt+"; arg0="+arg0+"; arg1="+arg1);
			Thread.dumpStack();
		}
		else
			ai.notifyEvent(evt, arg0, arg1);
	}

	public void setIntention(CtrlIntention intention, Object arg0, Object arg1)
	{
		if(Config.DEBUG)
			_log.info(this+".setIntention("+intention+","+arg0+","+arg1+"); current intention="+getIntention()+"; ai="+_ai);

		if(!hasAI() && intention == CtrlIntention.AI_INTENTION_IDLE)
			return;

		if(Config.DEBUG && intention == CtrlIntention.AI_INTENTION_IDLE && getIntention()==CtrlIntention.AI_INTENTION_ATTACK)
			Thread.dumpStack();

		L2CharacterAI ai = getAI();
		if(ai == null)
		{
			_log.warning("Null AI char '"+this+"' for set intention "+intention+"; arg0="+arg0+"; arg1="+arg1);
			Thread.dumpStack();
		}
		else
			ai.setIntention(intention, arg0, arg1);
	}

	public CtrlIntention getIntention()
	{
		if(!hasAI())
			return CtrlIntention.AI_INTENTION_IDLE;

		L2CharacterAI ai = this.getAI();
		if(ai == null)
		{
			_log.warning("Null AI char '"+this+"' for get intention");
			Thread.dumpStack();
			return null;
		}
		else
			return ai.getIntention();
	}

	public final boolean isAway()
	{
		return getIntention() ==  CtrlIntention.AI_INTENTION_IDLE
			|| getIntention() ==  CtrlIntention.AI_INTENTION_ACTIVE;
	}		

	public final L2CharacterAI checkDropAI()
	{
		if(_ai == null)
			return null;

		if(_ai.getIntention() == CtrlIntention.AI_INTENTION_ACTIVE)
			_ai.setIntention(CtrlIntention.AI_INTENTION_IDLE, null, null);
			
        if(Config.DEBUG)
        {
   	        _log.info("Drop AI for "+this+"; now="+_ai);
//   	        Thread.dumpStack();
   		}

   		return _ai;
	}

	/**
	 * Remove the L2Character from the world when the decay task is launched.<BR><BR>
	 *
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method DOESN'T REMOVE the object from _allObjects of L2World </B></FONT><BR>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method DOESN'T SEND Server->Client packets to players</B></FONT><BR><BR>
	 *
	 */
	public void onDecay()
	{
		this.decayMe();
	}


	/**
	 * Add the object to the list of L2Character that must be informed of HP/MP updates of this L2Character.<BR><BR>
	 *
	 * <B><U> Concept</U> :</B><BR><BR>
	 * Each L2Character owns a list called <B>_statusListener</B> that contains all L2PcInstance to inform of HP/MP updates.
	 * Players who must be informed are players that target this L2Character.
	 * When a RegenTask is in progress sever just need to go through this list to send Server->Client packet StatusUpdate.<BR><BR>
	 *
	 * <B><U> Example of use </U> :</B><BR><BR>
	 * <li> Target a PC or NPC</li><BR><BR>
	 *
	 * @param object L2Character to add to the listener
	 *
	 */
	public void addStatusListener(L2Character object)
	{
		if(object == this)
			return;

		CopyOnWriteArraySet<L2Character> listeners;

		synchronized(this)
		{
			if(_statusListener == null)
				_statusListener = new CopyOnWriteArraySet<L2Character>();

			listeners = _statusListener;
		}

		listeners.add(object);

	}


	/**
	 * Return the list of L2Character that must be informed of HP/MP updates of this L2Character.<BR><BR>
	 *
	 * <B><U> Concept</U> :</B><BR><BR>
	 * Each L2Character owns a list called <B>_statusListener</B> that contains all L2PcInstance to inform of HP/MP updates.
	 * Players who must be informed are players that target this L2Character.
	 * When a RegenTask is in progress sever just need to go through this list to send Server->Client packet StatusUpdate.<BR><BR>
	 *
	 * @return The list of L2Character to inform or null if empty
	 *
	 */
	@SuppressWarnings("unused")
	private CopyOnWriteArraySet<L2Character> getStatusListener()
	{
		return _statusListener;
	}


	/**
	 * Remove the object from the list of L2Character that must be informed of HP/MP updates of this L2Character.<BR><BR>
	 *
	 * <B><U> Concept</U> :</B><BR><BR>
	 * Each L2Character owns a list called <B>_statusListener</B> that contains all L2PcInstance to inform of HP/MP updates.
	 * Players who must be informed are players that target this L2Character.
	 * When a RegenTask is in progress sever just need to go through this list to send Server->Client packet StatusUpdate.<BR><BR>
	 *
	 * <B><U> Example of use </U> :</B><BR><BR>
	 * <li> Untarget a PC or NPC</li><BR><BR>
	 *
	 * @param object L2Character to add to the listener
	 *
	 */
	public void removeStatusListener(L2Character object)
	{
		CopyOnWriteArraySet<L2Character> listeners;

		synchronized(this)
		{
			if(_statusListener == null)
				return;
			listeners = _statusListener;
		}

		listeners.remove(object);

		if(listeners.isEmpty())
		{
			synchronized(_statusListener)
			{
				if(_statusListener != null && _statusListener.isEmpty())
					_statusListener = null;
			}
		}

	}


	/**
	 * Add a Func to the Calculator set of the L2Character.<BR><BR>
	 *
	 * <B><U> Concept</U> :</B><BR><BR>
	 * A L2Character owns a table of Calculators called <B>_calculators</B>.
	 * Each Calculator (a calculator per state) own a table of Func object.
	 * A Func object is a mathematic function that permit to calculate the modifier of a state (ex : REGENERATE_HP_RATE...).
	 * To reduce cache memory use, L2NPCInstances who don't have skills share the same Calculator set called <B>NPC_STD_CALCULATOR</B>.<BR><BR>
	 *
	 * That's why, if a L2NPCInstance is under a skill/spell effect that modify one of its state, a copy of the NPC_STD_CALCULATOR
	 * must be create in its _calculators before addind new Func object.<BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>If _calculators is linked to NPC_STD_CALCULATOR, create a copy of NPC_STD_CALCULATOR in _calculators</li>
	 * <li>Add the Func object to _calculators</li><BR><BR>
	 *
	 * @param f The Func object to add to the Calculator corresponding to the state affected
	 */
	public final synchronized void addStatFunc(Func f)
	{
		if(f == null)
			return;


		// Check if Calculator set is linked to the standard Calculator set of NPC
		if(_calculators == NPC_STD_CALCULATOR)
		{
			// Create a copy of the standard NPC Calculator set
			_calculators = new Calculator[Stats.NUM_STATS];

			for (int i=0; i < Stats.NUM_STATS; i++)
			{
				if(NPC_STD_CALCULATOR[i] != null)
					_calculators[i] = new Calculator(NPC_STD_CALCULATOR[i]);
			}
		}

		// Select the Calculator of the affected state in the Calculator set
		int stat = f._stat.ordinal();

		if(_calculators[stat] == null)
			_calculators[stat] = new Calculator();

		// Add the Func to the calculator corresponding to the state
		_calculators[stat].addFunc(f);

	}


	/**
	 * Add a list of Funcs to the Calculator set of the L2Character.<BR><BR>
	 *
	 * <B><U> Concept</U> :</B><BR><BR>
	 * A L2Character owns a table of Calculators called <B>_calculators</B>.
	 * Each Calculator (a calculator per state) own a table of Func object.
	 * A Func object is a mathematic function that permit to calculate the modifier of a state (ex : REGENERATE_HP_RATE...). <BR><BR>
	 *
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method is ONLY for L2PcInstance</B></FONT><BR><BR>
	 *
	 * <B><U> Example of use </U> :</B><BR><BR>
	 * <li> Equip an item from inventory</li>
	 * <li> Learn a new passive skill</li>
	 * <li> Use an active skill</li><BR><BR>
	 *
	 * @param funcs The list of Func objects to add to the Calculator corresponding to the state affected
	 */
	public final synchronized void addStatFuncs(Func[] funcs)
	{
		for (Func f : funcs)
			addStatFunc(f);
	}


	/**
	 * Remove a Func from the Calculator set of the L2Character.<BR><BR>
	 *
	 * <B><U> Concept</U> :</B><BR><BR>
	 * A L2Character owns a table of Calculators called <B>_calculators</B>.
	 * Each Calculator (a calculator per state) own a table of Func object.
	 * A Func object is a mathematic function that permit to calculate the modifier of a state (ex : REGENERATE_HP_RATE...).
	 * To reduce cache memory use, L2NPCInstances who don't have skills share the same Calculator set called <B>NPC_STD_CALCULATOR</B>.<BR><BR>
	 *
	 * That's why, if a L2NPCInstance is under a skill/spell effect that modify one of its state, a copy of the NPC_STD_CALCULATOR
	 * must be create in its _calculators before addind new Func object.<BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Remove the Func object from _calculators</li><BR><BR>
	 * <li>If L2Character is a L2NPCInstance and _calculators is equal to NPC_STD_CALCULATOR,
	 * free cache memory and just create a link on NPC_STD_CALCULATOR in _calculators</li><BR><BR>
	 *
	 * @param f The Func object to remove from the Calculator corresponding to the state affected
	 */
	public final synchronized void removeStatFunc(Func f)
	{
		if(f == null)
			return;

		// Select the Calculator of the affected state in the Calculator set
		int stat = f._stat.ordinal();

		if(_calculators[stat] == null)
			return;

		// Remove the Func object from the Calculator
		_calculators[stat].removeFunc(f);

		if(_calculators[stat].size() == 0)
			_calculators[stat] = null;


		// If possible, free the memory and just create a link on NPC_STD_CALCULATOR
		if(this instanceof L2NpcInstance)
		{
			int i = 0;
			for (; i < Stats.NUM_STATS; i++)
			{
				if(!Calculator.equalsCals(_calculators[i], NPC_STD_CALCULATOR[i]))
					break;
			}

			if(i >= Stats.NUM_STATS)
				_calculators = NPC_STD_CALCULATOR;
		}
	}


	/**
	 * Remove a list of Funcs from the Calculator set of the L2PcInstance.<BR><BR>
	 *
	 * <B><U> Concept</U> :</B><BR><BR>
	 * A L2Character owns a table of Calculators called <B>_calculators</B>.
	 * Each Calculator (a calculator per state) own a table of Func object.
	 * A Func object is a mathematic function that permit to calculate the modifier of a state (ex : REGENERATE_HP_RATE...). <BR><BR>
	 *
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method is ONLY for L2PcInstance</B></FONT><BR><BR>
	 *
	 * <B><U> Example of use </U> :</B><BR><BR>
	 * <li> Unequip an item from inventory</li>
	 * <li> Stop an active skill</li><BR><BR>
	 *
	 * @param funcs The list of Func objects to add to the Calculator corresponding to the state affected
	 */
	public final synchronized void removeStatFuncs(Func[] funcs)
	{
		for (Func f : funcs)
			removeStatFunc(f);
	}


	/**
	 * Remove all Func objects with the selected owner from the Calculator set of the L2Character.<BR><BR>
	 *
	 * <B><U> Concept</U> :</B><BR><BR>
	 * A L2Character owns a table of Calculators called <B>_calculators</B>.
	 * Each Calculator (a calculator per state) own a table of Func object.
	 * A Func object is a mathematic function that permit to calculate the modifier of a state (ex : REGENERATE_HP_RATE...).
	 * To reduce cache memory use, L2NPCInstances who don't have skills share the same Calculator set called <B>NPC_STD_CALCULATOR</B>.<BR><BR>
	 *
	 * That's why, if a L2NPCInstance is under a skill/spell effect that modify one of its state, a copy of the NPC_STD_CALCULATOR
	 * must be create in its _calculators before addind new Func object.<BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Remove all Func objects of the selected owner from _calculators</li><BR><BR>
	 * <li>If L2Character is a L2NPCInstance and _calculators is equal to NPC_STD_CALCULATOR,
	 * free cache memory and just create a link on NPC_STD_CALCULATOR in _calculators</li><BR><BR>
	 *
	 * <B><U> Example of use </U> :</B><BR><BR>
	 * <li> Unequip an item from inventory</li>
	 * <li> Stop an active skill</li><BR><BR>
	 *
	 * @param owner The Object(Skill, Item...) that has created the effect
	 */
	public final synchronized void removeStatsOwner(Object owner)
	{

		// Go through the Calculator set
		for (int i=0; i < _calculators.length; i++)
		{
			if(_calculators[i] != null)
			{
				// Delete all Func objects of the selected owner
				_calculators[i].removeOwner(owner);

				if(_calculators[i].size() == 0)
					_calculators[i] = null;
			}
		}

		// If possible, free the memory and just create a link on NPC_STD_CALCULATOR
		if(this instanceof L2NpcInstance)
		{
			int i = 0;
			for (; i < Stats.NUM_STATS; i++)
			{
				if(!Calculator.equalsCals(_calculators[i], NPC_STD_CALCULATOR[i]))
					break;
			}

			if(i >= Stats.NUM_STATS)
				_calculators = NPC_STD_CALCULATOR;
		}
	}


	/**
	 * Calculate the new value of the state with modifiers that will be applied on the targeted L2Character.<BR><BR>
	 *
	 * <B><U> Concept</U> :</B><BR><BR>
	 * A L2Character owns a table of Calculators called <B>_calculators</B>.
	 * Each Calculator (a calculator per state) own a table of Func object.
	 * A Func object is a mathematic function that permit to calculate the modifier of a state (ex : REGENERATE_HP_RATE...) : <BR><BR>
	 *
	 * FuncAtkAccuracy -> Math.sqrt(_player.getDEX())*6+_player.getLevel()<BR><BR>
	 *
	 * When the calc method of a calculator is launched, each mathematic function is called according to its priority <B>_order</B>.
	 * Indeed, Func with lowest priority order is executed firsta and Funcs with the same order are executed in unspecified order.
	 * The result of the calculation is stored in the value property of an Env class instance.<BR><BR>
	 *
	 * @param stat The stat to calculate the new value with modifiers
	 * @param init The initial value of the stat before applying modifiers
	 * @param target The L2Charcater whose properties will be used in the calculation (ex : CON, INT...)
	 * @param skill The L2Skill whose properties will be used in the calculation (ex : Level...)
	 *
	 */
	public final double calcStat(Stats stat, double init, L2Character target, L2Skill skill)
	{
		int id = stat.ordinal();

		Calculator c = _calculators[id];

		// If no Func object found, no modifier is applied
		if(c == null || c.size() == 0)
			return init;

		// Create and init an Env object to pass parameters to the Calculator
		Env env = new Env();
		env._player = this;
		env._target = target;
		env._skill = skill;
		env.value = init;

		// Launch the calculation
		c.calc(env);

		return env.value;
	}


	/**
	 * Return a map of 16 bits (0x0000) containing all abnormal effect in progress for this L2Character.<BR><BR>
	 *
	 * <B><U> Concept</U> :</B><BR><BR>
	 * In Server->Client packet, each effect is represented by 1 bit of the map (ex : BLEEDING = 0x0001 (bit 1), SLEEP = 0x0080 (bit 8)...).
	 * The map is calculated by applying a BINARY OR operation on each effect.<BR><BR>
	 *
	 * <B><U> Example of use </U> :</B><BR><BR>
	 * <li> Server Packet : CharInfo, NpcInfo, NpcInfoPoly, UserInfo...</li><BR><BR>
	 */
	public short getAbnormalEffect()
	{
		short ae = _abnormalEffects;
		if(_stuned)   ae |= ABNORMAL_EFFECT_STUN;
		if(_rooted)   ae |= ABNORMAL_EFFECT_ROOT;
		if(_sleeping) ae |= ABNORMAL_EFFECT_SLEEP;
		if(_confused) ae |= ABNORMAL_EFFECT_CONFUSED;
		if(_muted)	   ae |= ABNORMAL_EFFECT_MUTED;
		if(_affraid)  ae |= ABNORMAL_EFFECT_AFFRAID;
		return ae;
	}


	/**
	 * Not Implemented.<BR><BR>
	 *
	 * <B><U> Overridden in</U> :</B><BR><BR>
	 * <li>L2NPCInstance</li>
	 * <li>L2PcInstance</li>
	 * <li>L2Summon</li>
	 * <li>L2DoorInstance</li><BR><BR>
	 *
	 */
	public abstract void updateAbnormalEffect();


	/**
	 * Return True if the L2Character can't use its skills (ex : stun, sleep...).<BR><BR>
	 */
	public final boolean isAllSkillsDisabled()
	{
		return _allSkillsDisabled > System.currentTimeMillis() || _stuned || _sleeping || _paralyzed;
	}


	/**
	 * Return True if the L2Character can't move (stun, root, sleep, overload, paralyzed).<BR><BR>
	 */
	public final boolean isMovementDisabled()
	{
		return _stuned || _rooted || _sleeping || _overloaded || _paralyzed || _imobilised || _fake_death;
	}


	/**
	 * Return True if the L2Character can't attack (stun, sleep, attackEndTime).<BR><BR>
	 */
	public final boolean isAttackingDisabled()
	{
		return _stuned || _sleeping || _attackEndTime > GameTimeController.getGameTicks();
	}


	/**
	 * Return True if the L2Character is confused (not in the abnormal effect map).<BR><BR>
	 */
	public final boolean isConfused()
	{
		return _confused;
	}


	/**
	 * Return True if the L2Character is affraid (not in the abnormal effect map).<BR><BR>
	 */
	public final boolean isAffraid()
	{
		return _affraid;
	}

	/**
	 * Return True if the L2Character can be controlled by the player (confused, affraid).<BR><BR>
	 */
	public boolean isOutOfControl() {
		return _confused || _affraid;
	}


	/**
	 * Return True if the L2Character is muted (not in the abnormal effect map).<BR><BR>
	 */
	public final boolean isMuted()
	{
		return _muted;
	}

	/**
	 * Return True if the L2Character is imobilised.<BR><BR>
	 */
	public final boolean getImobilised()
	{
		return _imobilised;
	}

	/**
	 * Set the Imobilised status of the L2Character.<BR><BR>
	 */
	public final void setImobilised(boolean imobilised)
	{
		_imobilised = imobilised;
	}

	/**
	 * Set the overloaded status of the L2Character is overloaded (if True, the L2PcInstance can't take more item).<BR><BR>
	 */
	public void setOverloaded(boolean overloaded)
	{
		_overloaded = overloaded;
	}

	/**
	 * Set the Paralyzed status of the L2Character.<BR><BR>
	 */
	public final void setParalyzed(Boolean paralyzed)
	{
		_paralyzed = paralyzed;
	}

	/**
	 * Launch a Stun Abnormal Effect on the L2Character.<BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Calculate the success rate of the Stun Abnormal Effect on this L2Character</li>
	 * <li>If Stun succeed, active the abnormal effect Stun flag, notify the L2Character AI and send Server->Client UserInfo/CharInfo packet</li>
	 * <li>If Stun NOT succeed, send a system message Failed to the L2PcInstance attacker</li><BR><BR>
	 *
	 */
	public final void startStunning()
	{
		if(_attackingChar == null)
			return;

		// Calculate the success rate of the Stun Abnormal Effect on this L2Character
		if(Formulas.getInstance().calcSkillSuccess(_attackingChar, this, _attackingCharSkill))
		{
			_stuned   = true;
			abortCast();
			abortAttack();
			notifyEvent(CtrlEvent.EVT_STUNNED, null, null);
			updateAbnormalEffect();

			//if(_attackingChar instanceof L2PcInstance)
				//((L2PcInstance)_attackingChar).checkPvP(this);
		}
		else if(_attackingChar instanceof L2PcInstance)
		{
			_attackingChar.sendPacket(new SystemMessage(SystemMessage.S1_HAS_FAILED).addString(_attackingCharSkill.getName()));
		}

	}

	/**
	 * Active the abnormal effect Sleep flag, notify the L2Character AI and send Server->Client UserInfo/CharInfo packet.<BR><BR>
	 */
	public final void startSleeping()
	{
		_sleeping = true;
		abortCast();
		abortAttack();
		notifyEvent(CtrlEvent.EVT_SLEEPING, null, null);
		updateAbnormalEffect();
	}

	/**
	 * Active the abnormal effect Root flag, notify the L2Character AI and send Server->Client UserInfo/CharInfo packet.<BR><BR>
	 */
	public final void startRooted()
	{
		_rooted   = true;
		notifyEvent(CtrlEvent.EVT_ROOTED, null, null);
		updateAbnormalEffect();
	}

	/**
	 * Active the abnormal effect Fake Death flag, notify the L2Character AI and send Server->Client UserInfo/CharInfo packet.<BR><BR>
	 */
	public final void startFakeDeath()
	{
		_fake_death = true;
		abortCast();
		abortAttack();
		notifyEvent(CtrlEvent.EVT_FAKE_DEATH, null, null);
		this.broadcastPacket(new ChangeWaitType(this,ChangeWaitType.WT_START_FAKEDEATH));
	}

	/**
	 * Active the abnormal effect Confused flag, notify the L2Character AI and send Server->Client UserInfo/CharInfo packet.<BR><BR>
	 */
	public final void startConfused()
	{
		_confused = true;
		notifyEvent(CtrlEvent.EVT_CONFUSED, null, null);
		updateAbnormalEffect();
	}

	/**
	 * Active the abnormal effect Muted flag, notify the L2Character AI and send Server->Client UserInfo/CharInfo packet.<BR><BR>
	 */
	public final void startMuted()
	{
		_muted = true;
		abortCast();
		notifyEvent(CtrlEvent.EVT_MUTED, null, null);
		updateAbnormalEffect();
	}

	/**
	 * Active the abnormal effect Fear flag, notify the L2Character AI and send Server->Client UserInfo/CharInfo packet.<BR><BR>
	 */
	public final void startFear()
	{
		_affraid = true;
		notifyEvent(CtrlEvent.EVT_AFFRAID, null, null);
		updateAbnormalEffect();
	}

	/**
	 * Active abnormal effects flags in the binary mask and send Server->Client UserInfo/CharInfo packet.<BR><BR>
	 */
	public final void startAbnormalEffect(short mask)
	{
		_abnormalEffects |= mask;
		updateAbnormalEffect();
	}

	/**
	 * Stop a specified/all Stun abnormal L2Effect.<BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Delete a specified/all (if effect=null) Stun abnormal L2Effect from L2Character and update client magic icone </li>
	 * <li>Set the abnormal effect flag _stuned to False </li>
	 * <li>Notify the L2Character AI</li>
	 * <li>Send Server->Client UserInfo/CharInfo packet</li><BR><BR>
	 *
	 */
	public final void stopStunning(L2Effect effect)
	{
		if(effect == null)
			stopEffects(L2Effect.EffectType.STUN);
		else
			removeEffect(effect);

		_stuned   = false;

		notifyEvent(CtrlEvent.EVT_THINK, null, null);
		updateAbnormalEffect();
	}


	/**
	 * Stop a specified/all Sleep abnormal L2Effect.<BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Delete a specified/all (if effect=null) Sleep abnormal L2Effect from L2Character and update client magic icone </li>
	 * <li>Set the abnormal effect flag _sleeping to False </li>
	 * <li>Notify the L2Character AI</li>
	 * <li>Send Server->Client UserInfo/CharInfo packet</li><BR><BR>
	 *
	 */
	public final void stopSleeping(L2Effect effect)
	{
		if(effect == null)
			stopEffects(L2Effect.EffectType.SLEEP);
		else
			removeEffect(effect);

		_sleeping = false;

		notifyEvent(CtrlEvent.EVT_THINK, null, null);
		updateAbnormalEffect();
	}


	/**
	 * Stop a specified/all Root abnormal L2Effect.<BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Delete a specified/all (if effect=null) Root abnormal L2Effect from L2Character and update client magic icone </li>
	 * <li>Set the abnormal effect flag _rooted to False </li>
	 * <li>Notify the L2Character AI</li>
	 * <li>Send Server->Client UserInfo/CharInfo packet</li><BR><BR>
	 *
	 */
	public final void stopRooting(L2Effect effect)
	{
		if(effect == null)
			stopEffects(L2Effect.EffectType.ROOT);
		else
			removeEffect(effect);

		_rooted   = false;

		notifyEvent(CtrlEvent.EVT_THINK, null, null);
		updateAbnormalEffect();
	}


	/**
	 * Stop a specified/all Fake Death abnormal L2Effect.<BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Delete a specified/all (if effect=null) Fake Death abnormal L2Effect from L2Character and update client magic icone </li>
	 * <li>Set the abnormal effect flag _fake_death to False </li>
	 * <li>Notify the L2Character AI</li><BR><BR>
	 *
	 */
	public final void stopFakeDeath(L2Effect effect)
	{
		_fake_death   = false;

		if(effect == null)
			stopEffects(L2Effect.EffectType.FAKE_DEATH);
		else
			removeEffect(effect);

		//revive
		broadcastPacket(new ChangeWaitType(this,ChangeWaitType.WT_STOP_FAKEDEATH)); 
		notifyEvent(CtrlEvent.EVT_THINK, null, null);

	}


	/**
	 * Stop a specified/all Confused abnormal L2Effect.<BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Delete a specified/all (if effect=null) Confused abnormal L2Effect from L2Character and update client magic icone </li>
	 * <li>Set the abnormal effect flag _confused to False </li>
	 * <li>Notify the L2Character AI</li>
	 * <li>Send Server->Client UserInfo/CharInfo packet</li><BR><BR>
	 *
	 */
	public final void stopConfused(L2Effect effect)
	{
		if(effect == null)
			stopEffects(L2Effect.EffectType.CONFUSION);
		else
			removeEffect(effect);

		_confused = false;

		notifyEvent(CtrlEvent.EVT_THINK, null, null);
		updateAbnormalEffect();

	}


	/**
	 * Stop a specified/all Muted abnormal L2Effect.<BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Delete a specified/all (if effect=null) Muted abnormal L2Effect from L2Character and update client magic icone </li>
	 * <li>Set the abnormal effect flag _muted to False </li>
	 * <li>Notify the L2Character AI</li>
	 * <li>Send Server->Client UserInfo/CharInfo packet</li><BR><BR>
	 *
	 */
	public final void stopMuted(L2Effect effect)
	{

		if(effect == null)
			stopEffects(L2Effect.EffectType.MUTE);
		else
			removeEffect(effect);

		_muted = false;

		updateAbnormalEffect();
	}


	/**
	 * Stop a specified/all Fear abnormal L2Effect.<BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Delete a specified/all (if effect=null) Fear abnormal L2Effect from L2Character and update client magic icone </li>
	 * <li>Set the abnormal effect flag _affraid to False </li>
	 * <li>Notify the L2Character AI</li>
	 * <li>Send Server->Client UserInfo/CharInfo packet</li><BR><BR>
	 *
	 */
	public final void stopFear(L2Effect effect)
	{
		if(effect == null)
			stopEffects(L2Effect.EffectType.FEAR);
		else
			removeEffect(effect);

		_affraid = false;

		updateAbnormalEffect();
	}

	/**
	 * Modify the abnormal effect map according to the mask.<BR><BR>
	 */
	public final void stopAbnormalEffect(short mask)
	{
		_abnormalEffects &= ~mask;

		// Not implemented
		updateAbnormalEffect();
	}

	/**
	 * Return the orientation of the L2Character.<BR><BR>
	 */
	public final int getHeading()
	{
		return _heading;
	}

	/**
	 * Set the orientation of the L2Character.<BR><BR>
	 */
	public final void setHeading(int heading)
	{
		_heading = heading;
	}

	/**
	 * Set the orientation of the L2Character to the target.<BR><BR>
	 */
	public final void setHeadingTo(L2Character target)
	{
		if(target == null || target == this)
			return;
		
		_heading = (int) (Math.atan2(getY() - target.getY(),getX() - target.getX()) * 32768./Math.PI + 32768);
	}
	
	/**
	 * Return the X destination of the L2Character or the X position if not in movement.<BR><BR>
	 */
	public final int getClientX()
	{
		return _clientX;
	}
	public final int getClientY()
	{
		return _clientY;
	}
	public final int getClientZ()
	{
		return _clientZ;
	}
	public final int getClientHeading()
	{
		return _clientHeading;
	}
	public final void setClientX(int val)
	{
		_clientX=val;
	}
	public final void setClientY(int val)
	{
		_clientY=val;
	}
	public final void setClientZ(int val)
	{
		_clientZ=val;
	}
	public final void setClientHeading(int val)
	{
		_clientHeading=val;
	}
	public final int getXdestination()
	{
		MoveData m = _move;

		if(m != null)
			return m._xDestination;

		return getX();
	}

	/**
	 * Return the Y destination of the L2Character or the Y position if not in movement.<BR><BR>
	 */
	public final int getYdestination()
	{
		MoveData m = _move;

		if(m != null)
			return m._yDestination;

		return getY();
	}

	/**
	 * Return the Z destination of the L2Character or the Z position if not in movement.<BR><BR>
	 */
	public final int getZdestination()
	{
		MoveData m = _move;

		if(m != null)
			return m._zDestination;

		return getZ();
	}

	/**
	 * Return True if the L2Character is in combat.<BR><BR>
	 */
	public final boolean isInCombat()
	{
		return (GameTimeController.getGameTicks() - _lastAttackChange < 15 * GameTimeController.TICKS_PER_SECOND);
	}

	/**
	 * Return True if the L2Character is moving.<BR><BR>
	 */
	public final boolean isMoving()
	{
		return _move != null;
	}

	/**
	 * Return True if the L2Character is casting.<BR><BR>
	 */
	public final boolean isCastingNow()
	{
		return _castEndTime > GameTimeController.getGameTicks();
	}

	/**
	 * Return True if the cast of the L2Character can be aborted.<BR><BR>
	 */
	public final boolean canAbortCast()
	{
		return _castInterruptTime > GameTimeController.getGameTicks();
	}

	/**
	 * Return True if the L2Character is attacking.<BR><BR>
	 */
	public final boolean isAttackingNow()
	{
		return _attackEndTime > GameTimeController.getGameTicks();
	}

	/**
	 * Return True if the L2Character has aborted its attack.<BR><BR>
	 */
	public final boolean isAttackAborted()
	{
		return _attacking <= 0;
	}


	/**
	 * Abort the attack of the L2Character and send Server->Client ActionFailed packet.<BR><BR>
	 */
	public final void abortAttack()
	{
		if(isAttackingNow())
		{
			_attacking = 0;
			sendPacket(new ActionFailed());
		}
	}

	/**
	 * Returns body part (paperdoll slot) we are targeting right now
	 */
	public final int getAttackingBodyPart()
	{
		return _attacking;
	}

	/**
	 * Abort the cast of the L2Character and send Server->Client MagicSkillCanceld/ActionFailed packet.<BR><BR>
	 */
	public final void abortCast()
	{
		if(isCastingNow())
		{
			_castEndTime = 0;
			_castInterruptTime = 0;
			if(_skillCast != null)
				_skillCast.cancel(true);							// cancels the skill hit scheduled task
			enableAllSkills();									  // re-enables the skills
			broadcastPacket(new MagicSkillCanceld(getObjectId()));  // broadcast packet to stop animations client-side
			sendPacket(new ActionFailed());						 // send an "action failed" packet to the caster
		}
	}

	/**
	 * Update the position of the L2Character during a movement and return True if the movement is finished.<BR><BR>
	 *
	 * <B><U> Concept</U> :</B><BR><BR>
	 * At the beginning of the move action, all properties of the movement are stored in the MoveData object called <B>_move</B> of the L2Character.
	 * The position of the start point and of the destination permit to estimated in function of the movement speed the time to achieve the destination.<BR><BR>
	 *
	 * When the movement is started (ex : by MovetoLocation), this method will be called each 0.1 sec to estimate and update the L2Character position on the server.
	 * Note, that the current server position can differe from the current client position even if each movement is straight foward.
	 * That's why, client send regularly a Client->Server ValidatePosition packet to eventually correct the gap on the server.
	 * But, it's always the server position that is used in range calculation.<BR><BR>
	 *
	 * At the end of the estimated movement time, the L2Character position is automatically set to the destination position even if the movement is not finished.<BR><BR>
	 *
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : The current Z position is obtained FROM THE CLIENT by the Client->Server ValidatePosition Packet.
	 * But x and y positions must be calculated to avoid that players try to modify their movement speed.</B></FONT><BR><BR>
	 *
	 * @param gameTicks Nb of ticks since the server start
	 * @return True if the movement is finished
	 */
	public final boolean updatePosition(int gameTicks)
	{
		// Get movement data
		MoveData m = _move;

		if(m == null)
			return true;

		if(!isVisible())
		{
			_move = null;
			return true;
		}

		// Check if the position has alreday be calculated
		if(m._moveTimestamp == gameTicks)
			return false;

		// Calculate the time between the beginning of the deplacement and now
		int elapsed = gameTicks - m._moveStartTime;

		// If estimated time needed to achieve the destination is passed,
		// the L2Character is positionned to the destination position
		if(elapsed >= m._ticksToMove)
		{
			// Set the timer of last position update to now
			m._moveTimestamp = gameTicks;

			// Set the position of the L2Character to the destination
			super.setXYZ(m._xDestination, m._yDestination, m._zDestination);

			// Cancel the move action
			_move = null;

			return true;
		}

		// Estimate the position of the L2Character dureing the movement according to its _xSpeedTicks and _ySpeedTicks
		// The Z position is obtained from the client
		super.setXYZ(m._xMoveFrom + (int)(elapsed * m._xSpeedTicks),m._yMoveFrom + (int)(elapsed * m._ySpeedTicks),super.getZ());

		// Set the timer of last position update to now
		m._moveTimestamp = gameTicks;

		return false;

	}


	/**
	 * Stop movement of the L2Character (Called by AI Accessor only).<BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Delete movement data of the L2Character </li>
	 * <li>Set the current position (x,y,z), its current L2WorldRegion if necessary and its heading </li>
	 * <li>Remove the L2Object object from _gmList** of GmListTable </li>
	 *
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method DOESN'T send Server->Client packet StopMove/StopRotation </B></FONT><BR><BR>
	 *
	 */
	protected void stopMove(L2CharPosition pos)
	{
		// Delete movement data of the L2Character
		_move = null;
		
		//if(getAI() != null)
		//	getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);

		// Set the current position (x,y,z), its current L2WorldRegion if necessary and its heading
		// All data are contained in a L2CharPosition object
		if(pos != null)
		{
			setXYZ(pos.x, pos.y, pos.z);
			setHeading(pos.heading);
		}

		sendPacket(new StopMove(this));
	}

	/**
	 * Return the current HP of the L2Character.<BR><BR>
	 */
	public final double getCurrentHp()
	{
		return _currentHp;
	}


	/**
	 * Set the current HP of the L2Character (Called from AI Accessor only).<BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Get the Max HP of the L2Character </li>
	 * <li>Set the RegenActive flag to True/False </li>
	 * <li>Launch/Stop the HP/MP/CP Regeneration task with Medium priority</li>
	 * <li>Send the Server->Client packet StatusUpdate with current HP and MP to all other L2PcInstance to inform</li><BR><BR>
	 *
	 */
	public final void setCurrentHp(double newHp)
	{
		// Get the Max HP of the L2Character
		int maxHp = getMaxHp();

		synchronized(this)
		{
			if(newHp >= maxHp)
			{
				// Set the RegenActive flag to false
				_currentHp = maxHp;
				_flagsRegenActive &= ~HP_REGEN_FLAG;

				// Stop the HP/MP/CP Regeneration task
				if(_flagsRegenActive == 0)
					stopHpMpRegeneration();

			}
			else
			{
				// Set the RegenActive flag to true
				_currentHp = newHp;
				_flagsRegenActive |= HP_REGEN_FLAG;

				// Start the HP/MP/CP Regeneration task with Medium priority
				startHpMpRegeneration();
			}
		}

		// Send the Server->Client packet StatusUpdate with current HP and MP to all other L2PcInstance to inform
		broadcastStatusUpdate();

	}

	/**
	 * Stop the HP/MP/CP Regeneration task.<BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Set the RegenActive flag to False </li>
	 * <li>Stop the HP/MP/CP Regeneration task </li><BR><BR>
	 *
	 */
	protected synchronized void stopHpMpRegeneration()
	{
		if(_regTask != null)
		{
			// Stop the HP/MP/CP Regeneration task
			_regTask.cancel(false);
			_regTask = null;

			// Set the RegenActive flag to false
			_flagsRegenActive = 0;

			if(Config.DEBUG) _log.fine("HP/MP/CP regen stop");
		}
	}

	/**
	 * Start the HP/MP/CP Regeneration task.<BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Calculate the regen task period </li>
	 * <li>Launch the HP/MP/CP Regeneration task with Medium priority </li><BR><BR>
	 *
	 */
	private synchronized void startHpMpRegeneration()
	{
		if(_regTask == null && !isDead())
		{
			if(Config.DEBUG) _log.fine("HP/MP/CP regen started");

			// Get the Regeneration periode
			int period = Formulas.getInstance().getRegeneratePeriod(this);

			// Create the HP/MP/CP Regeneration task
			_regTask = ClientScheduler.getInstance().scheduleMedAtFixedRate(new RegenTask(), period, period);
			if(this instanceof L2PcInstance)
				((L2PcInstance)this).sendPacket(new UserInfo((L2PcInstance)this));
		}
	}

	/**
	 * Return the current MP of the L2Character.<BR><BR>
	 */
	public final double getCurrentMp()
	{
		return _currentMp;
	}


	/**
	 * Set the current MP of the L2Character.<BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Get the Max HP of the L2Character </li>
	 * <li>Set the RegenActive flag to True/False </li>
	 * <li>Launch/Stop the HP/MP/CP Regeneration task with Medium priority</li>
	 * <li>Send the Server->Client packet StatusUpdate with current HP and MP to all other L2PcInstance to inform</li><BR><BR>
	 *
	 */
	public final void setCurrentMp(Double newMp)
	{
		setCurrentMp((double)newMp);
	}

	public final void setCurrentMp(double newMp)
	{
		// Get the Max MP of the L2Character
		int maxMp = getMaxMp();

		synchronized (this)
		{
			if(newMp >= maxMp)
			{
				// Set the RegenActive flag to false
				_currentMp = maxMp;
				_flagsRegenActive &= ~MP_REGEN_FLAG;

				// Stop the HP/MP/CP Regeneration task
				if(_flagsRegenActive == 0)
					stopHpMpRegeneration();
			}
			else
			{
				// Set the RegenActive flag to true
				_currentMp = newMp;
				_flagsRegenActive |= MP_REGEN_FLAG;

				// Start the HP/MP/CP Regeneration task with Medium priority
				startHpMpRegeneration();
			}
		}

		// Send the Server->Client packet StatusUpdate with current HP and MP to all other L2PcInstance to inform
		broadcastStatusUpdate();
	}

	/**
	 * Return the current CP of the L2Character.<BR><BR>
	 */
	public final double getCurrentCp()
	{
		return _currentCp;
	}

	/**
	 * Set the current CP of the L2Character.<BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Get the Max CP of the L2Character </li>
	 * <li>Set the RegenActive flag to True/False </li>
	 * <li>Launch/Stop the HP/MP/CP Regeneration task with Medium priority</li>
	 * <li>Send the Server->Client packet StatusUpdate with current HP and MP to all other L2PcInstance to inform</li><BR><BR>
	 *
	 */

	public final void setCurrentCp(Double newCp)
	{
		setCurrentCp((double) newCp);
	}

	public final void setCurrentCp(double newCp)
	{
		// Get the Max CP of the L2Character
		int maxCp = getMaxCp();

		if(newCp < 0)
			newCp = 0;

		synchronized (this)
		{
			if(newCp >= maxCp)
			{
				// Set the RegenActive flag to false
				_currentCp = maxCp;
				_flagsRegenActive &= ~CP_REGEN_FLAG;

				// Stop the HP/MP/CP Regeneration task
				if(_flagsRegenActive == 0)
					stopHpMpRegeneration();
			}
			else
			{
				// Set the RegenActive flag to true
				_currentCp = newCp;
				_flagsRegenActive |= CP_REGEN_FLAG;

				// Start the HP/MP/CP Regeneration task with Medium priority
				startHpMpRegeneration();
			}
		}

		// Send the Server->Client packet StatusUpdate with current HP and MP to all other L2PcInstance to inform
		broadcastStatusUpdate();
	}

	/**
	 * Set the current HP and MP of the L2Character, Launch/Stop a HP/MP/CP Regeneration Task and send StatusUpdate packet to all other L2PcInstance to inform (exclusive broadcast).<BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Get the Max HP and MP of the L2Character </li>
	 * <li>Set the RegenActive flag to True/False </li>
	 * <li>Launch/Stop the HP/MP/CP Regeneration task with Medium priority</li>
	 * <li>Send the Server->Client packet StatusUpdate with current HP and MP to all other L2PcInstance to inform</li><BR><BR>
	 *
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method DOESN'T SEND Server->Client StatusUpdate packet to the L2Character</B></FONT><BR><BR>
	 *
	 * @param newHp The new HP value of the L2PcInstance
	 * @param newMp The new MP value of the L2PcInstance
	 *
	 */
	public final void setCurrentHpMp(double newHp, double newMp)
	{
		// Get the Max HP and MP of the L2Character
		int maxHp = getMaxHp();
		int maxMp = getMaxMp();

		synchronized (this)
		{
			if(newHp >= maxHp)
			{
				// Set the RegenActive flag to false
				_killedAlready = false;
				_currentHp = maxHp;
				_flagsRegenActive &= ~HP_REGEN_FLAG;

			}
			else
			{
				_currentHp = newHp;

				if(!isDead())
				{
					// Set the RegenActive flag to true
					_killedAlready = false;
					_flagsRegenActive |= HP_REGEN_FLAG;

					// Start the HP/MP/CP Regeneration task with Medium priority
					startHpMpRegeneration();
				}
			}

			if(newMp >= maxMp)
			{
				// Set the RegenActive flag to false
				_currentMp = maxMp;
				_flagsRegenActive &= ~MP_REGEN_FLAG;
			}
			else
			{
				_currentMp = newMp;

				if(!isDead())
				{
					// Set the RegenActive flag to true
					_flagsRegenActive |= MP_REGEN_FLAG;

					// Start the HP/MP/CP Regeneration task with Medium priority
					startHpMpRegeneration();
				}
			}

			// Stop the HP/MP/CP Regeneration task
			if(_flagsRegenActive == 0)
				stopHpMpRegeneration();
		}

		// Send the Server->Client packet StatusUpdate with current HP and MP to all other L2PcInstance to inform
		broadcastStatusUpdate();
	}


	/**
	 * Send the Server->Client packet StatusUpdate with current HP and MP to all other L2PcInstance to inform.<BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Create the Server->Client packet StatusUpdate with current HP and MP </li>
	 * <li>Send the Server->Client packet StatusUpdate with current HP and MP to all
	 * L2Character called _statusListener that must be informed of HP/MP updates of this L2Character </li><BR><BR>
	 *
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method DOESN'T SEND CP information</B></FONT><BR><BR>
	 *
	 * <B><U> Overriden in </U> :</B><BR><BR>
	 * <li> L2PcInstance : Send current HP,MP and CP to the L2PcInstance and only current HP, MP and Level to all other L2PcInstance of the Party</li><BR><BR>
	 *
	 */
	public void broadcastStatusUpdate()
	{
		// Get the list of L2Character called _statusListener that must be informed of HP/MP updates of this L2Character
		CopyOnWriteArraySet<L2Character> list = _statusListener;

		if(list == null || list.isEmpty())
		{
			return;
		}

		// Create the Server->Client packet StatusUpdate with current HP and MP

		StatusUpdate su = new StatusUpdate(_objectId);
		su.addAttribute(StatusUpdate.CUR_HP,(int)_currentHp);
		su.addAttribute(StatusUpdate.CUR_MP,(int)_currentMp);

		if(Config.DEBUG)
			_log.info("Create StatusUpdate for " + this + "; getCurrentHp()="+ _currentHp);

		// Go through the _statusListener
		for (L2Character temp : list)
		{
			// Send the Server->Client packet StatusUpdate with current HP and MP
			temp.sendPacket(su);
		}
	}


	/**
	 * Return the STR of the L2Character (base+modifier).<BR><BR>
	 */
	public int getSTR() { return (int)calcStat(Stats.STAT_STR, _template.baseSTR, null, null); }

	/**
	 * Return the COM of the L2Character (base+modifier).<BR><BR>
	 */
	public int getCON() { return (int)calcStat(Stats.STAT_CON, _template.baseCON, null, null); }

	/**
	 * Return the DEX of the L2Character (base+modifier).<BR><BR>
	 */
	public int getDEX() { return (int)calcStat(Stats.STAT_DEX, _template.baseDEX, null, null); }

	/**
	 * Return the INT of the L2Character (base+modifier).<BR><BR>
	 */
	public int getINT() { return (int)calcStat(Stats.STAT_INT, _template.baseINT, null, null); }

	/**
	 * Return the WIT of the L2Character (base+modifier).<BR><BR>
	 */
	public int getWIT() { return (int)calcStat(Stats.STAT_WIT, _template.baseWIT, null, null); }

	/**
	 * Return the MEN of the L2Character (base+modifier).<BR><BR>
	 */
	public int getMEN() { return (int)calcStat(Stats.STAT_MEN, _template.baseMEN, null, null); }


	/**
	 * Return the Max HP (base+modifier) of the L2Character.<BR><BR>
	 */
	public int getMaxHp()
	{
		if(this instanceof L2NpcInstance)
			return (int)calcStat(Stats.MAX_HP, _template.baseHpMax * ((L2NpcTemplate)_template).rateHp, null, null);

		return (int)calcStat(Stats.MAX_HP, _template.baseHpMax, null, null);
	}


	/**
	 * Return the Max MP (base+modifier) of the L2Character.<BR><BR>
	 */
	public int getMaxMp()
	{
		return (int)calcStat(Stats.MAX_MP, _template.baseMpMax, null, null);
	}


	/**
	 * Return the Max CP (base+modifier) of the L2Character.<BR><BR>
	 */
	public final int getMaxCp()
	{
		return (int)calcStat(Stats.MAX_CP, _template.baseCpMax, null, null);
	}

	/**
	 * Return the Accuracy (base+modifier) of the L2Character in function of the Weapon Expertise Penalty.<BR><BR>
	 */
	public final int getAccuracy()
	{
		double val = calcStat(Stats.ACCURACY_COMBAT, 0, null, null);

		val /= getWeaponExpertisePenalty();
		return (int)(calcStat(Stats.ACCURACY_COMBAT, 0, null, null) / getWeaponExpertisePenalty());
	}

	/**
	 * Return the MAtk (base+modifier) of the L2Character for a skill used in function of abnormal effects in progress.<BR><BR>
	 *
	 * <B><U> Example of use </U> :</B><BR><BR>
	 * <li> Calculate Magic damage </li><BR><BR>
	 *
	 * @param target The L2Character targeted by the skill
	 * @param skill The L2Skill used against the target
	 *
	 */
	public int getMAtk(L2Character target, L2Skill skill)
	{
		return (int)calcStat(Stats.MAGIC_ATTACK, _template.baseMAtk, target, skill);
	}

	public double getMAtkSps(L2Character target, L2Skill skill)
	{
		double matk = calcStat(Stats.MAGIC_ATTACK, _template.baseMAtk, target, skill);
		L2ItemInstance weaponInst = getActiveWeaponInstance();
		if(weaponInst != null)
		{
			if(weaponInst.getChargedSpiritshot() == L2ItemInstance.CHARGED_BLESSED_SPIRITSHOT) 
				matk *= 2;
			else if(weaponInst.getChargedSpiritshot() == L2ItemInstance.CHARGED_SPIRITSHOT) 
				matk *= 1.41;
		}
		return matk;
	}

	/**
	 * Return the MDef (base+modifier) of the L2Character against a skill in function of abnormal effects in progress.<BR><BR>
	 *
	 * <B><U> Example of use </U> :</B><BR><BR>
	 * <li> Calculate Magic damage </li><BR><BR>
	 *
	 * @param target The L2Character targeted by the skill
	 * @param skill The L2Skill used against the target
	 *
	 */
	public int getMDef(L2Character target, L2Skill skill)
	{
		return (int)calcStat(Stats.MAGIC_DEFENCE, _template.baseMDef, null, skill);
	}

	/**
	 * Return the Magic Critical Hit rate (base+modifier) of the L2Character.<BR><BR>
	 */
	public final int getMCriticalHit(L2Character target, L2Skill skill)
	{
		return (int)calcStat(Stats.MCRITICAL_RATE, 40, target, skill);
	}

	/**
	 * Return the Critical Hit rate (base+modifier) of the L2Character.<BR><BR>
	 */
	public final int getCriticalHit(L2Character target, L2Skill skill)
	{
		return (int)calcStat(Stats.CRITICAL_RATE, _template.baseCritRate, target, skill);
	}

	/**
	 * Return the Critical Damage rate (base+modifier) of the L2Character.<BR><BR>
	 */
	public final double getCriticalDmg(L2Character target, double init)
	{
		return calcStat(Stats.CRITICAL_DAMAGE, 2*init, target, null);
	}

	/**
	 * Return the Fatal Blow Damage rate (base+modifier) of the L2Character.<BR><BR>
	 */
	public final double getFatalBlowDamage(L2Character target, double init)
	{
		return calcStat(Stats.FATALBLOW_DAMAGE, init*1.5, target, null);
	}

	/**
	 * Return the Attack Cancel rate (base+modifier) of the L2Character.<BR><BR>
	 */
	public final int getAtkCancel()
	{
		return (int)calcStat(Stats.ATTACK_CANCEL, _template.baseAttackCancel, null, null);
	}

	/**
	 * Return the Attack Evasion rate (base+modifier) of the L2Character.<BR><BR>
	 */
	public final int getEvasionRate(L2Character target)
	{
		return (int)(calcStat(Stats.EVASION_RATE, 0, target, null) / getArmourExpertisePenalty());
	}

	/**
	 * Return the Skill/Spell reuse modifier.<BR><BR>
	 */
	public double getReuseModifier(L2Character target)
	{
		return calcStat(Stats.ATK_REUSE, 1, target, null);
	}
	
	/**
	 * Return the PAtk (base+modifier) of the L2Character.<BR><BR>
	 */
	public int getPAtk(L2Character target)
	{
		return (int)calcStat(Stats.POWER_ATTACK, _template.basePAtk, target, null);
	}

	/**
	 * Return the PDef (base+modifier) of the L2Character.<BR><BR>
	 */
	public int getPDef(L2Character target)
	{
		return (int)calcStat(Stats.POWER_DEFENCE, _template.basePDef, target, null);
	}

	/**
	 * Return the MAtk Speed (base+modifier) of the L2Character in function of the Armour Expertise Penalty.<BR><BR>
	 */
	public final int getMAtkSpd()
	{
		return (int)(calcStat(Stats.MAGIC_ATTACK_SPEED, _template.baseMAtkSpd, null, null) / getArmourExpertisePenalty());
	}

	/**
	 * Return the PAtk Speed (base+modifier) of the L2Character in function of the Armour Expertise Penalty.<BR><BR>
	 */
	public final int getPAtkSpd()
	{
		return (int)(calcStat(Stats.POWER_ATTACK_SPEED, _template.basePAtkSpd, null, null) / getArmourExpertisePenalty());
	}

	/**
	 * Return the MReuse rate (base+modifier) of the L2Character.<BR><BR>
	 */
	public final double getMReuseRate(L2Skill skill)
	{
		return calcStat(Stats.MAGIC_REUSE_RATE, _template.baseMReuseRate, null, skill);
	}

	/**
	 * Return the ShieldDef rate (base+modifier) of the L2Character.<BR><BR>
	 */
	public final int getShldDef()
	{
		return (int)calcStat(Stats.SHIELD_DEFENCE, 0, null, null);
	}

	/**
	 * Return the RunSpeed (base+modifier) of the L2Character in function of the Armour Expertise Penalty.<BR><BR>
	 */
	public final int getRunSpeed()
	{
		// err we should be adding TO the persons run speed
		// not making it a constant
		int val = (int)calcStat(Stats.RUN_SPEED, _template.baseRunSpd, null, null);

		if(isFlying())
			return val + Config.WYVERN_SPEED;

		if(isRidding())
			return val + Config.STRIDER_SPEED;

		val /= getArmourExpertisePenalty();
		return val;
	}

	public float getMovementSpeedMultiplier()
	{
		return getRunSpeed() * 1f / _template.baseRunSpd;
	}

	/**
	 * Return the WalkSpeed (base+modifier) of the L2Character.<BR><BR>
	 */
	public final int getWalkSpeed()
	{
		return (getRunSpeed() * 70) / 100;
	}

	/**
	 * Return the RunSpeed (base+modifier) or WalkSpeed (base+modifier) of the L2Character in function of the movement type.<BR><BR>
	 */
	public final float getMoveSpeed()
	{
		if(isRunning())
			return getRunSpeed();
		return getWalkSpeed();
	}

	/**
	 * Return the Physical Attack range (base+modifier) of the L2Character.<BR><BR>
	 */
	public final int getPhysicalAttackRange()
	{
		return (int)calcStat(Stats.POWER_ATTACK_RANGE, getTemplate().baseAtkRange, null, null);
	}

	/**
	 * Return the Magical Attack range (base+modifier) of the L2Character.<BR><BR>
	 */
	public final int getMagicalAttackRange(L2Skill skill)
	{
		if(skill != null)
			return (int)calcStat(Stats.MAGIC_ATTACK_RANGE, skill.getCastRange(), null, skill);
		return getTemplate().baseAtkRange;
	}

	/**
	 * Return the Attack Speed multiplier (base+modifier) of the L2Character to get proper animations.<BR><BR>
	 */
	public final float getAttackSpeedMultiplier()
	{
		return (float) ((1.1)*getPAtkSpd()/getTemplate().basePAtkSpd);
	}


	/**
	 * Not Implemented.<BR><BR>
	 */
	public abstract int getLevel();


	/**
	 * Return the name of the L2Character.<BR><BR>
	 */
	public final String getName()
	{
		return _name;
	}

	/**
	 * Set the name of the L2Character.<BR><BR>
	 *
	 * @param name The name of the L2Character<BR><BR>
	 *
	 */
	public final void setName(String name)
	{
		_name = name;
	}


	/**
	 * Target a L2Object (add the target to the L2Character _target, _knownObject and L2Character to _KnownObject of the L2Object).<BR><BR>
	 *
	 * <B><U> Concept</U> :</B><BR><BR>
	 * The L2Object (including L2Character) targeted is identified in <B>_target</B> of the L2Character<BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Set the _target of L2Character to L2Object </li>
	 * <li>If necessary, add L2Object to _knownObject of the L2Character </li>
	 * <li>If necessary, add L2Character to _KnownObject of the L2Object </li>
	 * <li>If object==null, cancel Attak or Cast </li><BR><BR>
	 *
	 * <B><U> Overriden in </U> :</B><BR><BR>
	 * <li> L2PcInstance : Remove the L2PcInstance from the old target _statusListener and add it to the new target if it was a L2Character</li><BR><BR>
	 *
	 * @param object L2object to target
	 *
	 */
	public void setTarget(L2Object object)
	{
		if(object != null && !object.isVisible())
			object = null;

		if(object != null && object != _target)
		{
			this.addKnownObject(object, null);
			object.addKnownObject(this, null);
		}

		// If object==null, Cancel Attak or Cast
		if(object == null)
		{
			if(_target != null)
				broadcastPacket(new TargetUnselected(this));

			if(isAttackingNow() && hasAI() && getAI().getAttackTarget() == _target)
			{
				abortAttack();

				setIntention(CtrlIntention.AI_INTENTION_ACTIVE, null, null);

				if(this instanceof L2PcInstance) {
					sendPacket(new ActionFailed());
					SystemMessage sm = new SystemMessage(SystemMessage.S1_S2);
					sm.addString("Attack is aborted");
					sendPacket(sm);
				}
			}

			if(isCastingNow() && canAbortCast() && hasAI() && getAI().getCastTarget() == _target)
			{
				abortCast();
				setIntention(CtrlIntention.AI_INTENTION_ACTIVE, null, null);
				if(this instanceof L2PcInstance) {
					sendPacket(new ActionFailed());
					SystemMessage sm = new SystemMessage(SystemMessage.S1_S2);
					sm.addString("Casting is aborted");
					sendPacket(sm);
				}
			}
		}

		_target = object;
	}

	/**
	 * Return the identifier of the L2Object targeted or -1.<BR><BR>
	 */
	public final int getTargetId()
	{
		if(_target != null)
			return _target.getObjectId();

		return -1;
	}

	/**
	 * Return the L2Object targeted or null.<BR><BR>
	 */
	public final L2Object getTarget()
	{
		return _target;
	}

	/**
	 * Return the Title of the L2Character.<BR><BR>
	 */
	public final String getTitle()
	{
		return _title;
	}

	/**
	 * Set the Title of the L2Character.<BR><BR>
	 *
	 * @param title The texte to set as title
	 *
	 */
	public final void setTitle(String title)
	{
		this._title = title;
	}

	/**
	 * Return True if the L2Character is running.<BR><BR>
	 */
	public final boolean isRunning()
	{
		return _running;
	}

	/**
	 * Return True if the L2Character is flying.<BR><BR>
	 */
	public final boolean isFlying()
	{
		return _flying;
	}

	/**
	 * Return True if the L2Character is ridding.<BR><BR>
	 */
	public final boolean isRidding()
	{
		return _ridding;
	}

	/**
	 * Set the L2Character movement type to run and send Server->Client packet ChangeMoveType to all others L2PcInstance.<BR><BR>
	 */
	public final void setRunning()
	{
		if(!_running)
		{
			_running = true;

			broadcastPacket(new ChangeMoveType(this));
		}
	}

	/**
	 * Set the L2Character flying mode to True.<BR><BR>
	 */
	public final void setFlying(boolean mode)
	{
		_flying = mode;
	}

	/**
	 * Set the L2Character ridding mode to True.<BR><BR>
	 */
	public final void setRidding(boolean mode)
	{
		_ridding = mode;
	}

	/**
	 * Set the L2Character movement type to walk and send Server->Client packet ChangeMoveType to all others L2PcInstance.<BR><BR>
	 */
	public final void setWalking()
	{
		if(_running)
		{
			_running = false;
			broadcastPacket(new ChangeMoveType(this));
		}
	}

	/**
	 * Reduce the current MP of the L2Character.<BR><BR>
	 *
	 * @param i The MP decrease value
	 *
	 */
	public void reduceCurrentMp(double i)
	{
		i = getCurrentMp() - i;

		if(i < 0)
			i = 0;

		setCurrentMp(i);
	}

	/**
	 * Reduce the current HP of the L2Character.<BR><BR>
	 *
	 * <B><U> Overriden in </U> :</B><BR><BR>
	 * <li> L2Attackable : No change</li>
	 * <li> L2PcInstance : Send a System Message S1_GAVE_YOU_S2_DMG to the L2PcInstance and PvP Flag the attacker (if necessary)</li><BR><BR>
	 *
	 * @param i The HP decrease value
	 * @param attacker The L2Character who attacks
	 *
	 */
	public void reduceCurrentHp(double i, L2Character attacker)
	{
		reduceCurrentHp(i, attacker, true);
	}

	public static Word unStunCheck;
	public static ConcurrentHashMap<Integer,ArrayList<Word>> reduceHpTargetHandlers = new ConcurrentHashMap<Integer,ArrayList<Word>>();
	public static ConcurrentHashMap<Integer,ArrayList<Word>> reduceHpAttackerHandlers = new ConcurrentHashMap<Integer,ArrayList<Word>>();

	/**
	 * Reduce the current HP of the L2Character and launch the doDie Task if necessary.<BR><BR>
	 *
	 * <B><U> Overriden in </U> :</B><BR><BR>
	 * <li> L2Attackable : Update the attacker AggroInfo of the L2Attackable _aggroList</li><BR><BR>
	 *
	 * @param i The HP decrease value
	 * @param attacker The L2Character who attacks
	 * @param awake The awake state (If True : stop sleeping)
	 *
	 */
	public void reduceCurrentHp(double i, L2Character attacker, boolean awake)
	{
		if(isDead())
			return;

		if(awake)
			if(_sleeping)
				stopSleeping(null);

		if(_stuned && unStunCheck !=null &&  unStunCheck.execrb(jbf))
			stopStunning(null);

		ArrayList<Word> alwa = reduceHpAttackerHandlers.get(getObjectId());
		ArrayList<Word> alwt = reduceHpTargetHandlers.get(getObjectId());
		if(alwt != null || alwa != null)
		{
			JBForth j = new JBForth(this);
			j.uvset("attacker", attacker);
			j.st.push(i);
			if(alwa != null)
				for(Word w : alwa)
					if(w!=null)
						w.execp(j);
			if(alwt != null)
				for(Word w : alwt)
					if(w!=null)
						w.execp(j);
			i = (Double)j.st.pop();
		}

		if(attacker instanceof L2PlayableInstance)
		{
			i = getCurrentCp() - i;
			double ii = i;
			if(ii < 0)
				ii *= -1;
			
			if(i < 0)
				i = 0;
			
			setCurrentCp(i);
			
			if(getCurrentCp() == 0)
			{	
				ii = getCurrentHp() - ii;
				
				if(ii < 0)
					ii = 0;

				setCurrentHp(ii);
			}
		}
		
		else
		{
			i = getCurrentHp() - i;
			
			if(i < 0)
				i = 0;
			
			setCurrentHp(i);
		}

		if(isDead())
		{
			// killing is only possible one time
			synchronized(this)
			{
				if(_killedAlready)
					return;

				_killedAlready = true;
			}

			// first die (and calculate revards), if currentHp < 0,
			// then overhit may be calculated
			if(Config.DEBUG) _log.fine("char is dead.");

			stopMove(null);

			// Start the doDie process
			doDie(attacker);

			// now reset currentHp to zero
			_currentHp = 0;
		}
		else if(this instanceof L2PcInstance)
		{
			if(((L2PcInstance)this).isSitting())
			((L2PcInstance)this).standUp();
		}
	}

	/**
	 * Kill the L2Character.<BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Set target to null and cancel Attack or Cast </li>
	 * <li>Stop movement </li>
	 * <li>Stop HP/MP/CP Regeneration task </li>
	 * <li>Stop all active skills effects in progress on the L2Character </li>
	 * <li>Send the Server->Client packet StatusUpdate with current HP and MP to all other L2PcInstance to inform </li>
	 * <li>Notify L2Character AI </li><BR><BR>
	 *
	 * <B><U> Overriden in </U> :</B><BR><BR>
	 * <li> L2NpcInstance : Create a DecayTask to remove the corpse of the L2NpcInstance after 7 seconds </li>
	 * <li> L2Attackable : Distribute rewards (EXP, SP, Drops...) and notify Quest Engine </li>
	 * <li> L2PcInstance : Apply Death Penalty, Manage gain/loss Karma and Item Drop </li><BR><BR>
	 *
	 * @param killer The L2Character who killed it
	 *
	 */
	@SuppressWarnings("static-access")
	public void doDie(L2Character killer)
	{
		// Set target to null and cancel Attack or Cast
		setTarget(null);

		// Stop movement
		stopMove(null);

		// Stop HP/MP/CP Regeneration task
		stopHpMpRegeneration();

		_currentHp = 0;

		// Stop all active skills effects in progress on the L2Character
		stopAllEffects();

		// Send the Server->Client packet StatusUpdate with current HP and MP to all other L2PcInstance to inform
		broadcastStatusUpdate();

		// Notify L2Character AI
		notifyEvent(CtrlEvent.EVT_DEAD, null, null);

        if(jbf != null)
        {
            jbf.uvset("killer", killer);
		    jbfHandlersList(GameServer.getNpcOnDieMap(), getTemplate().getNpcId());
            jbfHandlersList(GameServer.getNpcOnDieMap(), getObjectId());
        }

        // Notify Quest of character's death
        L2NpcInstance npc = null;
        if(killer instanceof L2NpcInstance)
            npc = (L2NpcInstance)killer;
        for (QuestState qs: getNotifyQuestOfDeath())
        {
            qs.getQuest().notifyDeath(npc, this, qs);
        }
        _NotifyQuestOfDeathList.clear();
	}

	// called from AIAccessor only
	/**
	 * Calculate movement data for a move to location action and add the L2Character to movingObjects of GameTimeController (only called by AI Accessor).<BR><BR>
	 *
	 * <B><U> Concept</U> :</B><BR><BR>
	 * At the beginning of the move action, all properties of the movement are stored in the MoveData object called <B>_move</B> of the L2Character.
	 * The position of the start point and of the destination permit to estimated in function of the movement speed the time to achieve the destination.<BR><BR>
	 * All L2Character in movement are identified in <B>movingObjects</B> of GameTimeController that will call the updatePosition method of those L2Character each 0.1s.<BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Get current position of the L2Character </li>
	 * <li>Calculate distance (dx,dy) between current position and destination including offset </li>
	 * <li>Create and Init a MoveData object </li>
	 * <li>Set the L2Character _move object to MoveData object </li>
	 * <li>Add the L2Character to movingObjects of the GameTimeController </li>
	 * <li>Create a task to notify the AI that L2Character arrives at a check point of the movement </li><BR><BR>
	 *
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method DOESN'T send Server->Client packet MoveToPawn/CharMoveToLocation </B></FONT><BR><BR>
	 *
	 * <B><U> Example of use </U> :</B><BR><BR>
	 * <li> AI : onIntentionMoveTo(L2CharPosition), onIntentionPickUp(L2Object), onIntentionInteract(L2Object) </li>
	 * <li> FollowTask </li><BR><BR>
	 *
	 * @param x The X position of the destination
	 * @param y The Y position of the destination
	 * @param z The Y position of the destination
	 * @param offset The size of the interaction area of the L2Character targeted
	 *
	 */
	protected void moveToLocation(int x, int y, int z, int offset)
	{
		// Get current position of the L2Character
		final int curX = super.getX();
		final int curY = super.getY();
		final int curZ = super.getZ();

		// Calculate distance (dx,dy) between current position and destination
		final int dx = (x - curX);
		final int dy = (y - curY);
		double distance = Math.sqrt(dx*dx + dy*dy);

		if(Config.DEBUG) _log.fine("distance to target:" + distance);

		// Define movement angles needed
		// ^
		// |	 X (x,y)
		// |   /
		// |  /distance
		// | /
		// |/ angle
		// X ---------->
		// (curx,cury)

		double cos;
		double sin;


		// Check if a movement offset is defined or no distance to go through
		if(offset > 0 || distance < 1)
		{
			// If no distance to go through, the movement is canceled
			if(distance < 1 || distance - offset  <= 0)
			{
				sin = 0;
				cos = 1;
				distance = 0;
				x = curX;
				y = curY;

				if(Config.DEBUG) _log.fine("already in range, no movement needed.");

				// Notify the AI that the L2Character is arrived at destination
				notifyEvent(CtrlEvent.EVT_ARRIVED, null, null);

				return;
			}
			// Calculate movement angles needed
			sin = dy/distance;
			cos = dx/distance;

			distance -= (offset-5); // due to rounding error, we have to move a bit closer to be in range

			// Calculate the new destination with offset included
			x = curX + (int)(distance * cos);
			y = curY + (int)(distance * sin);

		}
		else
		{
			// Calculate movement angles needed
			sin = dy/distance;
			cos = dx/distance;
		}

		// Get the Move Speed of the L2Charcater
		float speed = getMoveSpeed();

		// Create and Init a MoveData object
		MoveData m = new MoveData();

		// Caclulate the Nb of ticks between the current position and the destination
		m._ticksToMove = (int)(GameTimeController.TICKS_PER_SECOND * distance / speed);

		// Calculate the xspeed and yspeed in unit/ticks in function of the movement speed
		m._xSpeedTicks = (float)(cos * speed / GameTimeController.TICKS_PER_SECOND);
		m._ySpeedTicks = (float)(sin * speed / GameTimeController.TICKS_PER_SECOND);

		// Calculate and set the heading of the L2Character
		int heading = (int) (Math.atan2(-sin, -cos) * 10430.378350470452724949566316381);
		heading += 32768;
		setHeading(heading);

		if(Config.DEVELOPER)
			_log.fine("dist:"+ distance +"speed:" + speed + " ttt:" +m._ticksToMove +
					" dx:"+(int)m._xSpeedTicks + " dy:"+(int)m._ySpeedTicks + " heading:" + heading);

		m._xDestination = x;
		m._yDestination = y;
		m._zDestination = z; // this is what was requested from client

		m._moveStartTime = GameTimeController.getGameTicks();
		m._xMoveFrom = curX;
		m._yMoveFrom = curY;
		m._zMoveFrom = curZ;

		// If necessary set Nb ticks needed to a min value to ensure small distancies movements
		if(m._ticksToMove < 1 )
			m._ticksToMove = 1;

		if(Config.DEBUG) _log.fine("time to target:" + m._ticksToMove);

		// Set the L2Character _move object to MoveData object
		_move = m;

		// Add the L2Character to movingObjects of the GameTimeController
		// The GameTimeController manage objects movement
		GameTimeController.getInstance().registerMovingObject(this);

		int tm = m._ticksToMove*GameTimeController.MILLIS_IN_TICK;

		// Create a task to notify the AI that L2Character arrives at a check point of the movement
		if(tm > 3000)
			ClientScheduler.getInstance().scheduleMed(new NotifyAITask(CtrlEvent.EVT_ARRIVED_REVALIDATE), 2000);

		// the CtrlEvent.EVT_ARRIVED will be sent when the character will actually arrive
		// to destination by GameTimeController
	}

	public void removeKnownObject(L2Object object)
	{
		if(object == getTarget())
			setTarget(null);
	}

	public ArrayList<L2Object> getKnownObjects(int range)
	{
		return isVisible() ? getCurrentWorldRegion().getVisibleObjects(this, range) : new ArrayList<L2Object>();
	}

	public ArrayList<L2Character> getKnownCharacters(int range)
	{
		return isVisible() ? getCurrentWorldRegion().getVisibleCharacters(this, range) : new ArrayList<L2Character>();
	}

	public ArrayList<L2PcInstance> getKnownPlayers(int range)
	{
		return isVisible() ? getCurrentWorldRegion().getVisiblePlayers(this, range) : new ArrayList<L2PcInstance>();
	}

	public ArrayList<L2Character> getKnownCharacters(int radius, int height)
	{
		return isVisible() ? getCurrentWorldRegion().getVisibleCharacters(this, radius, height) : new ArrayList<L2Character>();
	}

	public ArrayList<L2NpcInstance> getKnownNpcs(int range)
	{
		return getCurrentWorldRegion().getVisibleNpcs(this, range);
	}
/*	public final boolean knowsObject(L2Object obj)
	{
		if(obj == null)
			return false;

		if(obj == this)
			return true;

		return getCurrentWorldRegion().knowsObject(obj, getDistanceToWatchObject());
	}
*/
	public void removeAllKnownObjects()
	{	
		// Cancel AI Task
		if(this.hasAI())
			_ai = null;
	}

	public final ArrayList<L2PcInstance> getVisiblePlayers(Integer range)
	{
		if(isVisible())
			return getCurrentWorldRegion().getVisiblePlayers(this, range);
		else
			return new ArrayList<L2PcInstance>();
	}

	public final ArrayList<L2PcInstance> getAllPlayers(Integer range)
	{
		if(isVisible())
			return getCurrentWorldRegion().getAllPlayers(this, range);
		else
			return new ArrayList<L2PcInstance>();
	}

	public boolean isKnownPlayers()
	{
		return isVisible() && getCurrentWorldRegion().isKnownPlayers();
	}

	public boolean knowsObject(L2Object obj)
	{
		return getDistance(obj) <= getDistanceToWatchObject(obj);
	}

	public boolean knowsThePlayer(L2PcInstance player)
	{
		return getDistance(player) <= getDistanceToWatchObject(player);
	}

	/**
	 * Send a packet to all L2PcInstance in the _KnownPlayers of the L2Character.<BR><BR>
	 *
	 * <B><U> Concept</U> :</B><BR><BR>
	 * L2PcInstance in the detection area of the L2Character are identified in <B>_knownPlayers</B>.
	 * In order to inform other players of state modification on the L2Character, server just need to go through _knownPlayers to send Server->Client Packet<BR><BR>
	 *
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method DOESN'T SEND Server->Client packet to this L2Character (to do this use function broadcastPacket)</B></FONT><BR><BR>
	 *
	 */
	public final void broadcastPacketToOthers(ServerBasePacket mov)
	{
		if(!isVisible() || !getCurrentWorldRegion().isKnownPlayers())
			return;

		if(Config.DEVELOPER) 
			_log.fine("players to notify:" + getAllPlayers(6144).size() + " packet:"+mov.getType());

		for(L2PcInstance player : getCurrentWorldRegion().getAllPlayers(this, 6144))
			if(player != this && _objectId != player.getObjectId())
				player.sendPacket(mov);
	}

	/**
	 * Send a packet to the L2Character AND to all L2PcInstance in the _KnownPlayers of the L2Character.<BR><BR>
	 *
	 * <B><U> Concept</U> :</B><BR><BR>
	 * L2PcInstance in the detection area of the L2Character are identified in <B>_knownPlayers</B>.
	 * In order to inform other players of state modification on the L2Character, server just need to go through _knownPlayers to send Server->Client Packet<BR><BR>
	 *
	 */
	public final void broadcastPacket(ServerBasePacket mov)
	{
		this.sendPacket(mov);

		broadcastPacketToOthers(mov);
	}

	/**
	 * Not Implemented.<BR><BR>
	 *
	 * <B><U> Overriden in </U> :</B><BR><BR>
	 * <li> L2PcInstance</li><BR><BR>
	 */
	public void sendPacket(@SuppressWarnings("unused") ServerBasePacket mov)
	{
		// default implementation
	}

	public void sendMessage(String message)
	{
		sendPacket(SystemMessage.sendString(message));
	}

	/**
	 * Return True if the L2Character is dead.<BR><BR>
	 */
	public final boolean isDead()
	{
		return _currentHp < 0.5;
	}

	/**
	 * Return True if the L2Character is dead or use fake death.<BR><BR>
	 */
	public final boolean isAlikeDead()
	{
		return _fake_death || _currentHp < 0.5;
	}
	
	/**
	 * Return True if the L2Character is using Fake Death.<BR><BR>
	 */
	public final boolean isFakeDeath()
	{
		return _fake_death;
	}

	/**
	 * Return the Weapon Expertise Penalty of the L2Character.<BR><BR>
	 */
	public float getWeaponExpertisePenalty()
	{
		return 1.f;
	}

	/**
	 * Return the Armour Expertise Penalty of the L2Character.<BR><BR>
	 */
	public float getArmourExpertisePenalty()
	{
		return 1.f;
	}


	/**
	 * Set _attacking corresponding to Attacking Body part to CHEST.<BR><BR>
	 */
	public void setAttackingBodypart()
	{
		_attacking = Inventory.PAPERDOLL_CHEST;
	}

	/**
	 * Launch a physical attack against a target (Simple, Bow, Pole or Dual).<BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Get the active weapon (always equiped in the right hand) </li><BR><BR>
	 * <li>If weapon is a bow, check for arrows, MP and bow re-use delay (if necessary, equip the L2PcInstance with arrows in left hand)</li>
	 * <li>If weapon is a bow, consumme MP and set the new period of bow non re-use </li><BR><BR>
	 * <li>Get the Attack Speed of the L2Character (delay (in milliseconds) before next attack) </li>
	 * <li>Select the type of attack to start (Simple, Bow, Pole or Dual) and verify if SoulShot are charged then start calculation</li>
	 * <li>If the Server->Client packet Attack contains at least 1 hit, send the Server->Client packet Attack to the L2Character AND to all L2PcInstance in the _KnownPlayers of the L2Character</li>
	 * <li>Notify AI with EVT_READY_TO_ACT</li><BR><BR>
	 *
	 * @param target The L2Character targeted
	 *
	 */
	protected void doAttack(L2Character target)
	{
		if(Config.DEBUG)
			_log.fine("doAttack: target="+target);

		if(target == null)
			return;
		
		if(target instanceof L2SiegeNpcInstance || target instanceof L2CastleChamberlainInstance)
			return;

		if(target instanceof L2DoorInstance)
			if(((L2DoorInstance)target).getCastle()!=null)
				if(!((L2DoorInstance)target).getCastle().getSiege().getIsInProgress())
					return;

		if(isAttackingDisabled())
			return;

		if((withinZones(L2World.zonePeace) || target.withinZones(L2World.zonePeace)) &&
			this instanceof L2PcInstance &&	target instanceof L2PcInstance &&
			(((L2PcInstance)this).getAccessLevel() < Config.GM_PEACEATTACK))
		{
			sendPacket(new SystemMessage(SystemMessage.YOU_CANNOT_ATTACK_THE_TARGET_IN_THE_PEACE_ZONE));
			sendPacket(new ActionFailed());
			return;
		}

		// Get the active weapon instance (always equiped in the right hand)
		L2ItemInstance weaponInst = getActiveWeaponInstance();

		// Get the active weapon item corresponding to the active weapon instance (always equiped in the right hand)
		L2Weapon weaponItem = getActiveWeaponItem();

		if((weaponItem!=null && weaponItem.getItemType() == L2WeaponType.BOW))
		{
			//Check for arrows and MP
			if(this instanceof L2PcInstance)
			{
				if(_disableBowAttackEndTime <= GameTimeController.getGameTicks())
				{
					if(getCurrentMp() < weaponItem.getMpConsume())
					{
						ClientScheduler.getInstance().scheduleMed(new NotifyAITask(CtrlEvent.EVT_READY_TO_ACT), 1000);
						sendPacket(new SystemMessage(SystemMessage.NOT_ENOUGH_MP));
						sendPacket(new ActionFailed());
						return;
					}
					reduceCurrentMp(weaponItem.getMpConsume());

					// Set the period of bow non re-use
					_disableBowAttackEndTime = 5*GameTimeController.TICKS_PER_SECOND + GameTimeController.getGameTicks();

				}
				else
				{
					// Cancel the action because the bow can't be re-use at this moment
					ClientScheduler.getInstance().scheduleMed(new NotifyAITask(CtrlEvent.EVT_READY_TO_ACT), 1000);
					sendPacket(new ActionFailed());
					return;
				}

				// Equip arrows needed in left hand and send a Server->Client packet ItemList to the L2PcINstance then return True
				if(!checkAndEquipArrows())
				{
					// Cancel the action because the L2PcInstance have no arrow
					setIntention(CtrlIntention.AI_INTENTION_IDLE, null, null);
					sendPacket(new ActionFailed());
					sendPacket(new SystemMessage(SystemMessage.NOT_ENOUGH_ARROWS));
					return;
				}
			}
			else if(this instanceof L2NpcInstance && _disableBowAttackEndTime > GameTimeController.getGameTicks())
				return;
		}

		//TODO Change 'getDistance(target) > 2000' for real attack range
		if(isAlikeDead() || target == null || target.isAlikeDead() || getDistance(target) > 2000)
		{
			setIntention(CtrlIntention.AI_INTENTION_ACTIVE, null, null);
			sendPacket(new ActionFailed());
			return;
		}

		addKnownObject(target, null);
		target.addKnownObject(this, null);

		// Verify if SoulShot are charged
		boolean wasSSCharged = (weaponInst != null && weaponInst.getChargedSoulshot() == L2ItemInstance.CHARGED_SOULSHOT);

		// Get the Attack Speed of the L2Character (delay (in milliseconds) before next attack)
		int sAtk = calculateAttackSpeed(target, weaponInst);
		_attackEndTime = GameTimeController.getGameTicks();
		_attackEndTime += (sAtk / GameTimeController.MILLIS_IN_TICK);
		_attackEndTime -= 1;

		// Create a Server->Client packet Attack
		Attack attack = new Attack(this, wasSSCharged);

		boolean hitted;

		// Set the Attacking Body part to CHEST
		setAttackingBodypart();

		setHeadingTo(target);

		// Select the type of attack to start
		if(weaponItem == null)
			hitted = doAttackHitSimple(attack, target);
		else if(weaponItem.getItemType() == L2WeaponType.BOW)
			hitted = doAttackHitByBow(attack, target, sAtk);
		else if(weaponItem.getItemType() == L2WeaponType.POLE)
		{
			if(calcStat(Stats.POLE_ATTACK_ANGLE,0,null,null) == -1)
				hitted = doAttackHitSimple(attack, target);
			else
				hitted = doAttackHitByPole(attack);
		}
		else if(isUsingDualWeapon())
			hitted = doAttackHitByDual(attack, target);
		else
			hitted = doAttackHitSimple(attack, target);


		if(hitted)
		{
			if(wasSSCharged)
				weaponInst.setChargedSoulshot(L2ItemInstance.CHARGED_NONE);
		}
		else
			abortAttack();

		// If the Server->Client packet Attack contains at least 1 hit, send the Server->Client packet Attack
		// to the L2Character AND to all L2PcInstance in the _KnownPlayers of the L2Character
		if(attack.hasHits())
			broadcastPacket(attack);

		// Get the Attack Reuse Delay of the L2Weapon
		int reuse = weaponItem==null ? 0 : weaponItem.getAttackReuseDelay();

		// Notify AI with EVT_READY_TO_ACT
		ClientScheduler.getInstance().scheduleMed(new NotifyAITask(CtrlEvent.EVT_READY_TO_ACT), sAtk+reuse);
	}

	/**
	 * Launch a simple attack.<BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Calculate if hit is missed or not </li>
	 * <li>If hit isn't missed, calculate if shield defense is efficient </li>
	 * <li>If hit isn't missed, calculate if hit is critical </li>
	 * <li>If hit isn't missed, calculate physical damages </li>
	 * <li>Create a new hit task with Medium priority</li>
	 * <li>Add this hit to the Server-Client packet Attack </li><BR><BR>
	 *
	 * @param attack Server->Client packet Attack in which the hit will be added
	 * @param target The L2Character targeted
	 *
	 * @return True if the hit isn't missed
	 *
	 */
	private boolean doAttackHitSimple(Attack attack, L2Character target)
	{
		int damage1 = 0;
		boolean shld1 = false;
		boolean crit1 = false;

		// Calculate if hit is missed or not
		boolean miss1 = Formulas.getInstance().calcHitMiss(this, target);

		// Check if hit isn't missed
		if(!miss1)
		{
			// Calculate if shield defense is efficient
			shld1 = Formulas.getInstance().calcShldUse(this, target);

			// Calculate if hit is critical
			crit1 = Formulas.getInstance().calcCrit(getCriticalHit(target, null));

			// Calculate physical damages
			damage1 = (int)Formulas.getInstance().calcPhysDam(this, target, null, shld1, crit1, false, attack._soulshot);

			if(this.calcStat(Stats.ABSORB_DAMAGE_PERCENT, 0, null, null) > 0)
				setCurrentHp(getCurrentHp() + (damage1 * this.calcStat(Stats.ABSORB_DAMAGE_PERCENT, 0, null, null))/100);
		}
		else
		{
			if(target instanceof L2PcInstance)
			{
				((L2PcInstance)target).sendPacket(new SystemMessage(SystemMessage.AVOIDED_S1S_ATTACK).addString(getName()));
			}
		}

		// Create a new hit task with Medium priority
		ClientScheduler.getInstance().scheduleMed(new HitTask(target, damage1, crit1, miss1, attack._soulshot, shld1), 980);

		// Add this hit to the Server-Client packet Attack
		attack.addHit(target, damage1, miss1, crit1, shld1);

		// Return true if hit isn't missed
		return !miss1;

	}

	/**
	 * Launch a Bow attack.<BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Calculate if hit is missed or not </li>
	 * <li>Consumme arrows </li>
	 * <li>If hit isn't missed, calculate if shield defense is efficient </li>
	 * <li>If hit isn't missed, calculate if hit is critical </li>
	 * <li>If hit isn't missed, calculate physical damages </li>
	 * <li>If the L2Character is a L2PcInstance, Send a Server->Client packet SetupGauge </li>
	 * <li>Create a new hit task with Medium priority</li>
	 * <li>Calculate and set the disable delay of the bow in function of the Attack Speed</li>
	 * <li>Add this hit to the Server-Client packet Attack </li><BR><BR>
	 *
	 * @param attack Server->Client packet Attack in which the hit will be added
	 * @param target The L2Character targeted
	 * @param sAtk The Attack Speed of the attacker
	 *
	 * @return True if the hit isn't missed
	 *
	 */
	private boolean doAttackHitByBow(Attack attack, L2Character target, int sAtk)
	{

		int damage1 = 0;
		boolean shld1 = false;
		boolean crit1 = false;

		// Calculate if hit is missed or not
		boolean miss1 = Formulas.getInstance().calcHitMiss(this, target);

		reduceArrowCount();
		
		_move = null;

		if(!miss1)
		{
			shld1 = Formulas.getInstance().calcShldUse(this, target);
			crit1 = Formulas.getInstance().calcCrit(getCriticalHit(target, null));
			damage1 = (int)Formulas.getInstance().calcPhysDam(this, target, null, shld1, crit1, false, attack._soulshot);
		}

		int reuse;
		L2Weapon bow = getActiveWeaponItem();

		if(bow == null)
			reuse = 1500;
		else
			reuse = bow.getAttackReuseDelay();
		reuse *= getReuseModifier(target);

		if(this instanceof L2PcInstance)
		{
			sendPacket(new SystemMessage(SystemMessage.GETTING_READY_TO_SHOOT_ARROWS));
			sendPacket(new SetupGauge(SetupGauge.RED, sAtk+reuse));
		}

		// Create a new hit task with Medium priority
		ClientScheduler.getInstance().scheduleMed(new HitTask(target, damage1, crit1, miss1, attack._soulshot, shld1), sAtk);

		// Calculate and set the disable delay of the bow in function of the Attack Speed
		_disableBowAttackEndTime = (sAtk+reuse)/GameTimeController.MILLIS_IN_TICK + GameTimeController.getGameTicks();

		attack.addHit(target, damage1, miss1, crit1, shld1);
		return !miss1;
	}

	/**
	 * Launch a Dual attack.<BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Calculate if hits are missed or not </li>
	 * <li>If hits aren't missed, calculate if shield defense is efficient </li>
	 * <li>If hits aren't missed, calculate if hit is critical </li>
	 * <li>If hits aren't missed, calculate physical damages </li>
	 * <li>Create 2 new hit tasks with Medium priority</li>
	 * <li>Add those hits to the Server-Client packet Attack </li><BR><BR>
	 *
	 * @param attack Server->Client packet Attack in which the hit will be added
	 * @param target The L2Character targeted
	 *
	 * @return True if hit 1 or hit 2 isn't missed
	 *
	 */
	private boolean doAttackHitByDual(Attack attack, L2Character target)
	{
		int damage1 = 0;
		int damage2 = 0;
		boolean shld1 = false;
		boolean shld2 = false;
		boolean crit1 = false;
		boolean crit2 = false;

		boolean miss1 = Formulas.getInstance().calcHitMiss(this, target);
		boolean miss2 = Formulas.getInstance().calcHitMiss(this, target);

		if(!miss1)
		{
			shld1 = Formulas.getInstance().calcShldUse(this, target);
			crit1 = Formulas.getInstance().calcCrit(getCriticalHit(target, null));
			damage1 = (int)Formulas.getInstance().calcPhysDam(this, target, null, shld1, crit1, true, attack._soulshot);
			if(this.calcStat(Stats.ABSORB_DAMAGE_PERCENT, 0, null, null) > 0)
				setCurrentHp(getCurrentHp() + (damage1 * this.calcStat(Stats.ABSORB_DAMAGE_PERCENT, 0, null, null))/100);
		}

		if(!miss2)
		{
			shld2 = Formulas.getInstance().calcShldUse(this, target);
			crit2 = Formulas.getInstance().calcCrit(getCriticalHit(target, null));
			damage2 = (int)Formulas.getInstance().calcPhysDam(this, target, null, shld2, crit2, true, attack._soulshot);
			if(this.calcStat(Stats.ABSORB_DAMAGE_PERCENT, 0, null, null) > 0)
				setCurrentHp(getCurrentHp() + (damage2 * this.calcStat(Stats.ABSORB_DAMAGE_PERCENT, 0, null, null))/100);
		}

		// Create a new hit task with Medium priority for hit 1 and for hit 2 with a higher delay 
		ClientScheduler.getInstance().scheduleMed(new HitTask(target, damage1, crit1, miss1, attack._soulshot, shld1), 650);
		ClientScheduler.getInstance().scheduleMed(new HitTask(target, damage2, crit2, miss2, attack._soulshot, shld2), 1250);

		attack.addHit(target, damage1, miss1, crit1, shld1);
		attack.addHit(target, damage2, miss2, crit2, shld2);
		return !miss1 || !miss2;
	}

	/**
	 * Launch a Pole attack.<BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Get all visible objects in a spheric area near the L2Character to obtain possible targets </li>
	 * <li>If possible target is the L2Character targeted, launch a simple attack against it </li>
	 * <li>If possible target isn't the L2Character targeted but is attakable, launch a simple attack against it </li><BR><BR>
	 *
	 * @param attack Server->Client packet Attack in which the hit will be added
	 *
	 * @return True if one hit isn't missed
	 *
	 */
	private boolean doAttackHitByPole(Attack attack)
	{
		boolean hitted = false;
		int angle = (int)this.calcStat(Stats.POLE_ATTACK_ANGLE, 60, null, null);
		int range = (int)this.calcStat(Stats.POWER_ATTACK_RANGE, getTemplate().baseAtkRange, null, null);

		for(L2Character target : this.getKnownCharacters(range, 80))
		{
			// Launch a simple attack against the L2Character targeted
			if(target.isAlikeDead())
				continue;

			//Temp fix non-collision
			int dx = target.getX()-getX();
			int dy = target.getY()-getY();
			if(dx*dx+dy*dy>340)
				if(!isInFront(target, angle))
					continue;

			if(this.getAI()!= null && target == this.getAI().getAttackTarget())
				hitted |= doAttackHitSimple(attack, target);
		}
		
		for(L2Character target : this.getKnownCharacters(range, 80))
		{
			// If the L2Character is attakable, launch a simple attack against it
			if(!target.isAlikeDead() && target.isAutoAttackable(this))
			{
				if(this.getAI()!=null && target == this.getAI().getAttackTarget())
					continue;

				if(!isInFront(target, angle))
				continue;

				hitted |= doAttackHitSimple(attack, target);
				target.notifyEvent(CtrlEvent.EVT_ATTACKED, this, null);
			}
		}
		// Return true if one hit isn't missed
		return hitted;
	}

	/**
	 * Retun True if arrows are available.<BR><BR>
	 *
	 * <B><U> Overriden in </U> :</B><BR><BR>
	 * <li> L2PcInstance</li><BR><BR>
	 *
	 */
	protected boolean checkAndEquipArrows()
	{
		return true;
	}

	/**
	 * Add Exp and Sp to the L2Character.<BR><BR>
	 *
	 * <B><U> Overriden in </U> :</B><BR><BR>
	 * <li> L2PcInstance</li>
	 * <li> L2PetInstance</li><BR><BR>
	 *
	 */
	public void addExpAndSp(@SuppressWarnings("unused") int addToExp, @SuppressWarnings("unused") int addToSp)
	{
		// Dummy method (overridden by players and pets)
	}

	/**
	 * Return the active weapon instance (always equiped in the right hand).<BR><BR>
	 *
	 * <B><U> Overriden in </U> :</B><BR><BR>
	 * <li> L2PcInstance</li><BR><BR>
	 *
	 */
	public abstract L2ItemInstance getActiveWeaponInstance();

	/**
	 * Return the active weapon item (always equiped in the right hand).<BR><BR>
	 *
	 * <B><U> Overriden in </U> :</B><BR><BR>
	 * <li> L2PcInstance</li><BR><BR>
	 *
	 */
	public abstract L2Weapon getActiveWeaponItem();

	/**
	 * Return the secondary weapon instance (always equiped in the left hand).<BR><BR>
	 *
	 * <B><U> Overriden in </U> :</B><BR><BR>
	 * <li> L2PcInstance</li><BR><BR>
	 *
	 */
	public abstract L2ItemInstance getSecondaryWeaponInstance();

	/**
	 * Return the secondary weapon item (always equiped in the left hand).<BR><BR>
	 *
	 * <B><U> Overriden in </U> :</B><BR><BR>
	 * <li> L2PcInstance</li><BR><BR>
	 *
	 */
	public abstract L2Weapon getSecondaryWeaponItem();


	/**
	 * Manage hit process (called by Hit Task).<BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>If the attacker/target is dead or use fake death, notify the AI with EVT_CANCEL and send a Server->Client packet ActionFailed (if attacker is a L2PcInstance)</li>
	 * <li>If attack isn't aborted, send a message system (critical hit, missed...) to attacker/target if they are L2PcInstance </li>
	 * <li>If attack isn't aborted and hit isn't missed, reduce HP of the target and calculate reflection damage to reduce HP of attacker if necessary </li>
	 * <li>if attack isn't aborted and hit isn't missed, manage attack or cast break of the target (calculating rate, sending message...) </li><BR><BR>
	 *
	 * @param target The L2Character targeted
	 * @param damage Nb of HP to reduce
	 * @param crit True if hit is critical
	 * @param miss True if hit is missed
	 * @param soulshot True if SoulShot are charged
	 * @param shld True if shield is efficient
	 *
	 */
	protected void onHitTimer(L2Character target, int damage, boolean crit, boolean miss, @SuppressWarnings("unused") boolean soulshot, boolean shld)
	{
		// If the attacker/target is dead or use fake death, notify the AI with EVT_CANCEL
		// and send a Server->Client packet ActionFailed (if attacker is a L2PcInstance)
		if(isAlikeDead())
		{
			notifyEvent(CtrlEvent.EVT_CANCEL, null, null);
			return;
		}

		//TODO really distance instead 2000!
		if(target.isAlikeDead() || getDistance(target)>2000 && !(this instanceof L2DoorInstance))
		{
			//getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE, null);
			notifyEvent(CtrlEvent.EVT_CANCEL, null, null);

			sendPacket(new ActionFailed());

			return;
		}


		// If attack isn't aborted, send a message system (critical hit, missed...) to attacker/target if they are L2PcInstance
		if(!isAttackAborted())
		{
			// Notify AI with EVT_ATTACKED
			target.notifyEvent(CtrlEvent.EVT_ATTACKED, this, null);

			if(this instanceof L2PcInstance)
			{
				if(crit)
					sendPacket(new SystemMessage(SystemMessage.CRITICAL_HIT));

				if(miss)
					sendPacket(new SystemMessage(SystemMessage.MISSED_TARGET));
//				else if(damage < 1)
//					sendMessage("Enemy's armour hit");
				else
					sendPacket(new SystemMessage(SystemMessage.YOU_HIT_FOR_S1_DAMAGE).addNumber(damage));
			}
			
			if(this instanceof L2SummonInstance)
			{
				((L2SummonInstance)this).displayHitMessage(damage,crit,miss);
			}
			
			else if(this instanceof L2PetInstance)
			{
				((L2PetInstance)this).displayHitMessage(damage,crit,miss);
			}

			if(target instanceof L2PcInstance)
			{
				L2PcInstance enemy = (L2PcInstance)target;

				if(shld)
					enemy.sendPacket(new SystemMessage(SystemMessage.SHIELD_DEFENSE_HAS_SUCCEEDED));
				else if(!miss && damage < 1)
					enemy.sendMessage("Enemy's armour hit");
			}


			// Reduce HP of the target and calculate reflection damage to reduce HP of attacker if necessary
			if(!miss && damage > 0)
			{
				double reflectPercent = target.calcStat(Stats.REFLECT_DAMAGE_PERCENT,0,null,null);

				if(reflectPercent>0)
				{
					double reflectedDamage = damage * reflectPercent / 100.;
					this.reduceCurrentHp(reflectedDamage,target,true);
					if(target instanceof L2PcInstance)
						((L2PcInstance)target).sendMessage("You Reflected "+(int)reflectedDamage+" damage");
				}
				target.reduceCurrentHp(damage, this);
			}

			// Manage attack or cast break of the target (calculating rate, sending message...)
			if(!miss && damage > 0 && Formulas.getInstance().calcCastBreak(target, target.getAtkCancel()))
				target.breakCast();
		}
	}

	/**
	 * Break an attack and send Server->Client ActionFailed packet and a System Message to the L2Character.<BR><BR>
	 */
	public void breakAttack()
	{
		if(isAttackingNow())
		{
			abortAttack();

			if(this instanceof L2PcInstance)
				sendPacket(new SystemMessage(SystemMessage.ATTACK_FAILED));
		}
	}


	/**
	 * Break a cast and send Server->Client ActionFailed packet and a System Message to the L2Character.<BR><BR>
	 */
	public void breakCast()
	{
		if(isCastingNow() && canAbortCast())
		{
			abortCast();

			if(this instanceof L2PcInstance)
				sendPacket(new SystemMessage(SystemMessage.CASTING_HAS_BEEN_INTERRUPTED));
		}
	}

	/**
	 * Reduce the arrow number of the L2Character.<BR><BR>
	 *
	 * <B><U> Overriden in </U> :</B><BR><BR>
	 * <li> L2PcInstance</li><BR><BR>
	 *
	 */
	protected void reduceArrowCount()
	{
		// default is to do nothin
	}

	/**
	 * Manage Forced attack (ctrl + select target).<BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>If L2Character or target is in a town area, send a system message TARGET_IN_PEACEZONE a Server->Client packet ActionFailed </li>
	 * <li>If target is confused, send a Server->Client packet ActionFailed </li>
	 * <li>If L2Character is a L2ArtefactInstance, send a Server->Client packet ActionFailed </li>
	 * <li>Send a Server->Client packet MyTargetSelected to start attack and Notify AI with AI_INTENTION_ATTACK </li><BR><BR>
	 *
	 * @param player The L2PcInstance to attack
	 *
	 */
	public void onForcedAttack(L2PcInstance player)
	{
		L2Object target = player.getTarget();

		if(!(target instanceof L2MonsterInstance) &&
			(player.getAccessLevel() < Config.GM_PEACEATTACK) &&
			(withinZones(L2World.zonePeace) || target.withinZones(L2World.zonePeace)) &&
			(!(target instanceof L2PcInstance) || !Config.ALT_GAME_KARMAPLAYERCANBEKILLEDINPEACEZONE))
		{
			player.sendPacket(new SystemMessage(SystemMessage.YOU_CANNOT_ATTACK_THE_TARGET_IN_THE_PEACE_ZONE));
			player.sendPacket(new ActionFailed());
			return;
		}

		if(player.isConfused() || this instanceof L2ArtefactInstance)
		{
			player.sendPacket(new ActionFailed());
			return;
		}

		player.sendPacket(new MyTargetSelected(getObjectId(), player.getLevel() - getLevel()));
		player.setIntention(CtrlIntention.AI_INTENTION_ATTACK, this, null);
	}


	/**
	 * Return the Attack Speed of the L2Character (delay (in milliseconds) before next attack).<BR><BR>
	 */
	public int calculateAttackSpeed(L2Character target, @SuppressWarnings("unused") L2ItemInstance weaponInst)
	{
		return Formulas.getInstance().calcPAtkSpd(this, target, getPAtkSpd());
	}

	/**
	 * Return True if the L2Character use a dual weapon.<BR><BR>
	 */
	public boolean isUsingDualWeapon()
	{
		return false;
	}

	/**
	 * Add a skill to the L2Character _skills and its Func objects to the calculator set of the L2Character.<BR><BR>
	 *
	 * <B><U> Concept</U> :</B><BR><BR>
	 * All skills own by a L2Character are identified in <B>_skills</B><BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Replace oldSkill by newSkill or Add the newSkill </li>
	 * <li>If an old skill has been replaced, remove all its Func objects of L2Character calculator set</li>
	 * <li>Add Func objects of newSkill to the calculator set of the L2Character </li><BR><BR>
	 *
	 * <B><U> Overriden in </U> :</B><BR><BR>
	 * <li> L2PcInstance : Save update in the character_skills table of the database</li><BR><BR>
	 *
	 * @param newSkill The L2Skill to add to the L2Character
	 *
	 * @return The L2Skill replaced or null if just added a new L2Skill
	 *
	 */
	public L2Skill addSkill(L2Skill newSkill)
	{
		L2Skill oldSkill = null;

		if(newSkill != null)
		{
			// Replace oldSkill by newSkill or Add the newSkill
			oldSkill = _skills.put(newSkill.getId(), newSkill);

			// If an old skill has been replaced, remove all its Func objects
			if(oldSkill != null)
				removeStatsOwner(oldSkill);

			// Add Func objects of newSkill to the calculator set of the L2Character
			addStatFuncs(newSkill.getStatFuncs(null, this));
		}

		return oldSkill;
	}


	/**
	 * Remove a skill from the L2Character and its Func objects from calculator set of the L2Character.<BR><BR>
	 *
	 * <B><U> Concept</U> :</B><BR><BR>
	 * All skills own by a L2Character are identified in <B>_skills</B><BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Remove the skill from the L2Character _skills </li>
	 * <li>Remove all its Func objects from the L2Character calculator set</li><BR><BR>
	 *
	 * <B><U> Overriden in </U> :</B><BR><BR>
	 * <li> L2PcInstance : Save update in the character_skills table of the database</li><BR><BR>
	 *
	 * @param skill The L2Skill to remove from the L2Character
	 *
	 * @return The L2Skill removed
	 *
	 */
	public L2Skill removeSkill(L2Skill skill)
	{

		// Remove the skill from the L2Character _skills
		L2Skill oldSkill = _skills.remove(skill.getId());

		// Remove all its Func objects from the L2Character calculator set
		if(oldSkill != null)
			removeStatsOwner(oldSkill);

		return oldSkill;
	}

	/**
	 * Return all skills own by the L2Character in a table of L2Skill.<BR><BR>
	 *
	 * <B><U> Concept</U> :</B><BR><BR>
	 * All skills own by a L2Character are identified in <B>_skills</B> the L2Character <BR><BR>
	 *
	 */
	public final L2Skill[] getAllSkills()
	{
		if(_skills == null)
			return new L2Skill[0];

		return _skills.values().toArray(new L2Skill[_skills.values().size()]);
	}

	/**
	 * Return the level of a skill owned by the L2Character.<BR><BR>
	 *
	 * @param skillId The identifier of the L2Skill whose level must be returned
	 *
	 * @return The level of the L2Skill identified by skillId
	 *
	 */
	public final int getSkillLevel(int skillId)
	{
		if(_skills == null)
			return -1;

		L2Skill skill = _skills.get(skillId);

		if(skill == null)
			return -1;
		return skill.getLevel();
	}

	/**
	 * Return True if the skill is known by the L2Character.<BR><BR>
	 *
	 * @param skillId The identifier of the L2Skill to check the knowledge
	 *
	 */
	public final L2Skill getKnownSkill(int skillId)
	{
		if(_skills == null)
			return null;

		return _skills.get(skillId);
	}

    /** Sets HP, MP and CP and revives the L2Character. */
	public void doRevive()
	{
		setCurrentCp(getMaxCp() * Config.RESPAWN_RESTORE_CP);
		setCurrentHp(getMaxHp() * Config.RESPAWN_RESTORE_HP);
		setCurrentMp(getMaxMp() * Config.RESPAWN_RESTORE_MP);

		broadcastPacket(new SocialAction(getObjectId(), 15)); 
		broadcastPacket(new Revive(this));
	}

	/**
	 * Check if the active L2Skill can be casted.<BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Check if the L2Character can cast (ex : not sleeping...) </li>
	 * <li>Notify the AI with AI_INTENTION_CAST and target</li><BR><BR>
	 *
	 * @param skill The L2Skill to use
	 *
	 */
	protected void useMagic(L2Skill skill)
	{
		if(skill == null || isDead())
			return;

		// Check if the L2Character can cast
		if(isAllSkillsDisabled())
		{
			// must be checked by caller
			return;
		}


		// Ignore the passive skill request. why does the client send it anyway ??
		if(skill.isPassive())
			return;

		//CHECKME:
		// If Build Headquarters, check its ok to place the flag
		//if(skill.getSkillType() == SkillType.SIEGEFLAG && !L2Siege.checkIfOkToPlaceFlag(this, false))
		//	return;

		// If summon siege golem, hog cannon, check its ok to place the flag
		if((skill.getId() == 13 || skill.getId() == 299) 
				&& !SiegeManager.getInstance().checkIfOkToSummon(this, false))
			return;

		// Get the target for the skill
		L2Object target = null;
		switch (skill.getTargetType())
		{
			case TARGET_AURA:	// AURA, SELF should be cast even if no target has been found
			case TARGET_SELF:
				target = this;
				break;
			default:
				L2Object[] targets = skill.getTargetList(this);
				if(targets == null || targets.length == 0)
					return;

				target = targets[0];
				break;
		}

		// Notify the AI with AI_INTENTION_CAST and target
		setIntention(CtrlIntention.AI_INTENTION_CAST, skill, target);
	}


	/**
	 * Manage the casting task (casting and interrupt time, re-use delay...) and display the casting bar and animation on client.<BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Verify the possibilty of the the cast : skill is a spell, caster isn't muted... </li>
	 * <li>Get the list of all targets (ex : area effects) and define the L2Charcater targeted (its stats will be used in calculation)</li>
	 * <li>Calculate the casting time (base + modifier of MAtkSpd), interrupt time and re-use delay</li>
	 * <li>Send a Server->Client packet MagicSkillUser (to diplay casting animation), a packet SetupGauge (to display casting bar) and a system message </li>
	 * <li>Disable all skills during the casting time (create a task EnableAllSkills)</li>
	 * <li>Disable the skill during the re-use delay (create a task EnableSkill)</li>
	 * <li>Create a task MagicUseTask (that will call method onMagicUseTimer) to launch the Magic Skill at the end of the casting time</li><BR><BR>
	 *
	 * @param skill The L2Skill to use
	 *
	 */
	protected void doCast(L2Skill skill)
	{
		L2Object fixtarget=getTarget();
		/*
		if(Config.DEBUG)
			_log.warning("L2Character: doCast -> " + skill);
		*/
		
		if(skill == null)
		{
			notifyEvent(CtrlEvent.EVT_CANCEL, null, null);
			return;
		}

		if(isSkillDisabled(skill.getId()))
		{
			if(this instanceof L2PcInstance)
				sendPacket(new SystemMessage(SystemMessage.S1_IS_NOT_AVAILABLE_AT_THIS_TIME_BEING_PREPARED_FOR_REUSE).addSkillName(skill.getId()));
			return;
		}


		// Check if the skill is a magic spell and if the L2Character is not muted
		if(skill.isMagic() && isMuted())
		{
			notifyEvent(CtrlEvent.EVT_CANCEL, null, null);
			return;
		}

		// Get all possible targets of the skill in a table in function of the skill target type
		L2Object[] targets = skill.getTargetList(this,fixtarget);
		if(targets == null)
		{
			notifyEvent(CtrlEvent.EVT_CANCEL, null, null);
			return;
		}

		// Set the target of the skill in function of Skill Type and Target Type
		L2Character target = null;
		if(		skill.getSkillType() == SkillType.BUFF ||
				skill.getSkillType() == SkillType.HEAL ||
				skill.getSkillType() == SkillType.COMBATPOINTHEAL ||
				skill.getSkillType() == SkillType.MANAHEAL ||
				skill.getSkillType() == SkillType.REFLECT ||
				skill.getTargetType() == L2Skill.SkillTargetType.TARGET_SELF ||
				skill.getTargetType() == L2Skill.SkillTargetType.TARGET_PET ||
				skill.getTargetType() == L2Skill.SkillTargetType.TARGET_PARTY ||
                skill.getTargetType() == L2Skill.SkillTargetType.TARGET_CLAN || 
				skill.getTargetType() == L2Skill.SkillTargetType.TARGET_ALLY)
			target = (L2Character) targets[0];
		else
			//target = (L2Character) getTarget();
			target = (L2Character) fixtarget;

		// AURA skills should always be using caster as target
		if(skill.getTargetType() == SkillTargetType.TARGET_AURA)
			target = this;

		if(target == null)
		{
			notifyEvent(CtrlEvent.EVT_CANCEL, null, null);
			return;
		}

		/*
		if(Config.DEBUG)
			_log.warning("L2Character: doCast -> " + skill + " " + target + " " + targets);
		*/
		
		// Get the Identifier of the skill
		int magicId = skill.getId();

		// Get the Display Identifier for a skill that client can't display
		int displayId = skill.getDisplayId();

		// Get the level of the skill
		int level = getSkillLevel(magicId);
		if(level < 1) level = 1;

		// Get the casting time of the skill (base)
		int skillTime = skill.getSkillTime();

		// Get the delay under wich the cast can be aborted (base)
		int skillInterruptTime = skill.getSkillInterruptTime();

		// Calculate the casting time of the skill (base + modifier of MAtkSpd)
		skillTime = Formulas.getInstance().calcMAtkSpd(this, target, skill, skillTime);

		// Calculate the Interrupt Time of the skill (base + modifier) if the skill is a spell else 0
		if(skill.isMagic())
		{
			skillInterruptTime = Formulas.getInstance().calcMAtkSpd(this, target, skill, skillInterruptTime);
		}
		else
		{
			skillInterruptTime = 0;
		}

		// Calculate altered Cast Speed due to BSpS/SpS
		L2ItemInstance weaponInst = this.getActiveWeaponInstance();
		if(weaponInst != null && skill.isMagic() && skill.getTargetType() != SkillTargetType.TARGET_SELF)
		{
			if(weaponInst.getChargedSpiritshot() == L2ItemInstance.CHARGED_BLESSED_SPIRITSHOT ||
				weaponInst.getChargedSpiritshot() == L2ItemInstance.CHARGED_SPIRITSHOT)
			{
				//Only takes 70% of the time to cast a BSpS/SpS cast
				skillTime = (int)(0.70 * skillTime);
				skillInterruptTime = (int)(0.70 * skillInterruptTime);
			}
		}
		
		// Set the _castEndTime and _castInterruptTim
		_castEndTime = 10 + GameTimeController.getGameTicks() + skillTime / GameTimeController.MILLIS_IN_TICK;
		_castInterruptTime = GameTimeController.getGameTicks() + skillInterruptTime / GameTimeController.MILLIS_IN_TICK;

		// Init the reuse time of the skill
		int reuseDelay = (int)(skill.getReuseDelay() * getMReuseRate(skill) * 333. / getMAtkSpd());

		// Send a Server->Client packet MagicSkillUser with target, displayId, level, skillTime, reuseDelay
		// to the L2Character AND to all L2PcInstance in the _KnownPlayers of the L2Character
		MagicSkillUser msu = new MagicSkillUser(this, target, displayId, level, skillTime, reuseDelay);
		broadcastPacket(msu);

		// Send a system message USE_S1 to the L2Character
		if(this instanceof L2PcInstance) {
			SystemMessage sm = new SystemMessage(SystemMessage.USE_S1);
			sm.addSkillName(magicId);
			sendPacket(sm);
		}

		// Disable the skill during the re-use delay and create a task EnableSkill with Medium priority to enable it at the end of the re-use delay
		if(reuseDelay > 10)
		{
			disableSkill(skill.getId());
			ClientScheduler.getInstance().scheduleMed(new EnableSkill(skill.getId()), reuseDelay);
		}

		// launch the magic in skillTime milliseconds
		if(skillTime > 50)
		{
			// Send a Server->Client packet SetupGauge with the color of the gauge and the casting time
			if(this instanceof L2PcInstance)
			{
				SetupGauge sg = new SetupGauge(SetupGauge.BLUE, skillTime);
				sendPacket(sg);
			}

			// Disable all skills during the casting time and create a task EnableAllSkills with Medium priority to enable all skills at the end of the cating time
			disableAllSkills(skillTime);

			// Create a task MagicUseTask with Medium priority to launch the MagicSkill at the end of the casting time
			_skillCast = ClientScheduler.getInstance().scheduleMed(new MagicUseTask(targets, skill,this), skillTime);
		}
		else
		{
			onMagicUseTimer(targets, skill);
		}
	}

	/**
	 * Manage the magic skill launching task (MP, HP, Item consummation...) and display the magic skill animation on client.<BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Send a Server->Client packet MagicSkillLaunched (to display magic skill animation) to all L2PcInstance of L2Charcater _knownPlayers</li>
	 * <li>Consumme MP, HP and Item if necessary</li>
	 * <li>Send a Server->Client packet StatusUpdate with MP modification to the L2PcInstance</li>
	 * <li>Launch the magic skill in order to calculate its effects</li>
	 * <li>If the skill type is PDAM, notify the AI of the target with AI_INTENTION_ATTACK</li>
	 * <li>Notify the AI of the L2Character with EVT_FINISH_CASTING</li><BR><BR>
	 *
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : A magic skill casting MUST BE in progress</B></FONT><BR><BR>
	 *
	 * @param skill The L2Skill to use
	 *
	 */
	public void onMagicUseTimer(L2Object[] targets, L2Skill skill)
	{
		if(isAlikeDead())
		{
			_castEndTime = 0;
			_castInterruptTime = 0;
			return;
		}

		// Check if a cast is in progress
		if(isCastingNow())
		{
			_castEndTime = 0;
			_castInterruptTime = 0;

			// Get the display identifier of the skill
			int magicId = skill.getDisplayId();

			// Get the level of the skill
			int level = getSkillLevel(skill.getId());
			if(level < 1) level = 1;

			for(int i=0; i<targets.length; i++)
			{
				if(targets[i] instanceof L2Character)
				{
					L2Character target = (L2Character) targets[i];
					
					if(skill.getSkillType() == L2Skill.SkillType.BUFF )
						target.sendPacket(new SystemMessage(SystemMessage.YOU_CAN_FEEL_S1S_EFFECT).addString(skill.getName()));

					// Send a Server->Client packet MagicSkillLaunched to the L2Character AND to all L2PcInstance in the _KnownPlayers of the L2Character
					broadcastPacket(new MagicSkillLaunched(this, magicId, level, target));
					if(this instanceof L2PcInstance && target instanceof L2Summon){
                        ((L2Summon)target).getOwner().sendPacket(new PetInfo((L2Summon)target));
					}
				}
				else
				{
					if(Config.DEBUG) _log.warning("Class cast bad: "+targets.getClass().toString());
				}
			}

			// Consumme MP of the L2Character and Send the Server->Client packet StatusUpdate with current HP and MP to all other L2PcInstance to inform
			if(skill.getMpConsume() > 0)
			{
				reduceCurrentMp(skill.getMpConsume());
			}

			// Consumme HP if necessary and Send the Server->Client packet StatusUpdate with current HP and MP to all other L2PcInstance to inform
			if(skill.getHpConsume() > 0)
			{
				reduceCurrentHp(skill.getHpConsume(), null);
			}

			// Consumme Items if necessary and Send the Server->Client packet InventoryUpdate with Item modification to all the L2Character
			if(skill.getItemConsume() > 0)
			{
				consumeItem(skill.getItemConsumeId(), skill.getItemConsume());
			}

			// Send a Server->Client packet StatusUpdate with MP modification to the L2PcInstance
			sendPacket(new StatusUpdate(getObjectId()).addAttribute(StatusUpdate.CUR_MP, (int) getCurrentMp()));

			// Launch the magic skill in order to calculate its effects
			callSkill(skill, targets);

			// If the skill type is PDAM or DRAIN_SOUL, notify the AI of the target with AI_INTENTION_ATTACK
			if(skill.getSkillType() == SkillType.PDAM || skill.getSkillType() == SkillType.DRAIN_SOUL)
				if((getTarget() != null) && (getTarget() instanceof L2Character))
					setIntention(CtrlIntention.AI_INTENTION_ATTACK, getTarget(), null);

			// Notify the AI of the L2Character with EVT_FINISH_CASTING
			notifyEvent(CtrlEvent.EVT_FINISH_CASTING, null, null);
            
            if(this instanceof L2PcInstance)
            {
                if(((L2PcInstance)this)._nextSkill != null)
                {
                    L2Skill t = ((L2PcInstance)this)._nextSkill; 
                    ((L2PcInstance)this)._nextSkill = null;
                    ((L2PcInstance)this).useMagic(t,((L2PcInstance)this)._nextForceUse,((L2PcInstance)this)._nextDontMove);
                }
            }

		}
	}


	public void broadcastUserInfo()
	{
	}


	/**
	 * Reduce the item number of the L2Character.<BR><BR>
	 *
	 * <B><U> Overriden in </U> :</B><BR><BR>
	 * <li> L2PcInstance</li><BR><BR>
	 *
	 */
	public void consumeItem(@SuppressWarnings("unused") int itemConsumeId, @SuppressWarnings("unused") int itemCount)
	{
	}

	/**
	 * Enable a skill (remove it from _disabledSkills of the L2Character).<BR><BR>
	 *
	 * <B><U> Concept</U> :</B><BR><BR>
	 * All skills disabled are identified by their skillId in <B>_disabledSkills</B> of the L2Character <BR><BR>
	 *
	 * @param skillId The identifier of the L2Skill to enable
	 *
	 */
	public synchronized void enableSkill(int skillId)
	{
		if(_disabledSkills == null)
			return;

		_disabledSkills.remove(new Integer(skillId));

	}

	/**
	 * Disable a skill (add it to _disabledSkills of the L2Character).<BR><BR>
	 *
	 * <B><U> Concept</U> :</B><BR><BR>
	 * All skills disabled are identified by their skillId in <B>_disabledSkills</B> of the L2Character <BR><BR>
	 *
	 * @param skillId The identifier of the L2Skill to disable
	 *
	 */
	public synchronized void disableSkill(int skillId)
	{
		if(_disabledSkills == null)
		{
			_disabledSkills = new ArrayList<Integer>();

		}
		_disabledSkills.add(skillId);
	}

	/**
	 * Check if a skill is disabled.<BR><BR>
	 *
	 * <B><U> Concept</U> :</B><BR><BR>
	 * All skills disabled are identified by their skillId in <B>_disabledSkills</B> of the L2Character <BR><BR>
	 *
	 * @param skillId The identifier of the L2Skill to disable
	 *
	 */
	public boolean isSkillDisabled(int skillId)
	{
		if(isAllSkillsDisabled())
			return true;

		if(_disabledSkills == null)
			return false;

		return _disabledSkills.contains(skillId);
	}

	/**
	 * Disable all skills (set _allSkillsDisabled to True).<BR><BR>
	 */
	public void disableAllSkills(long time)
	{
		if(Config.DEBUG) _log.fine("all skills disabled for "+time+" ms");
		_allSkillsDisabled = System.currentTimeMillis() + time;
	}

	public void enableAllSkills()
	{
		_allSkillsDisabled = 0;
	}

	/**
	 * Launch the magic skill and calculate its effects on each target contained in the targets table.<BR><BR>
	 *
	 * @param skill The L2Skill to use
	 * @param targets The table of L2Object targets
	 *
	 */
	public void callSkill(L2Skill skill, L2Object[] targets)
	{
		try
		{
			// Do initial checkings for skills and set pvp flag/draw aggro when needed
			for (L2Object target : targets)
			{
				if(target instanceof L2Character)
				{
					// Set some values inside target's instance for later use
					L2Character player = (L2Character) target;
					player.setAttackingChar(this);
					player.setAttackingCharSkill(skill);
					
					if(player instanceof L2NpcInstance)		// Add attackers to npc's attacker list
						player.addAttackerToAttackByList(this);

					if(this instanceof L2PcInstance)
					{
						L2PcInstance activeChar = (L2PcInstance) this;
						
						// Check for PvP Flagging / Drawing Aggro
						if(skill.isOffensive())
						{
							if(player instanceof L2PcInstance)
							{
								if(((L2PcInstance)player).getKarma() == 0)
								{
									activeChar.checkPvP(player);
								}
							}
							// mobs will hate on debuff
							if(player instanceof L2Attackable)
							{
								((L2Attackable)player).addDamageHate(this, 1, 1);
							}
						}
						else
						{
							// mobs will hate if this. will buff mob's attaker
							if (target instanceof L2PcInstance) activeChar.onBuff(((L2PcInstance)target),1);

							if(player instanceof L2PcInstance)
							{
								if(((L2PcInstance)player).getPvpFlag() > 0 || ((L2PcInstance)player).getKarma() > 0)
								{
									activeChar.checkPvP(player);
								}
							}
							if(player instanceof L2Attackable)
							{
								activeChar.checkPvP(this);
							}
						}
					}
				}
			}

			ISkillHandler handler = null;

			// TODO Remove this useless section
			if(skill.isToggle())
			{
				// Check if the skill effects are already in progress on the L2Character
				if(getEffect(skill.getId()) != null)
				{
					Word w = SkillTable.skillStartHandlers.get(skill.getId());
					if(w!=null)
					{
						jbf.uvset("target", targets);
						jbf.uvset("skill", skill);
						jbf.uvset("skill-id", skill.getId());
						w.execp(jbf);
						return;
					}

					handler = SkillHandler.getInstance().getSkillHandler(skill.getSkillType());

					if(handler != null)
						handler.useSkill(this, skill, targets);
					else
						skill.useSkill(this, targets);

					return;
				}
			}

			Word w = SkillTable.skillDoHandlers.get(skill.getId());
			if(w!=null)
			{
				jbf.uvset("target", targets);
				jbf.uvset("skill", skill);
				jbf.uvset("skill-id", skill.getId());
				w.execp(jbf);
			}
			else
			{
				// Get the skill handler corresponding to the skill type (PDAM, MDAM, SWEEP...) started in gameserver
				handler = SkillHandler.getInstance().getSkillHandler(skill.getSkillType());

				// Launch the magic skill and calculate its effects
				if(handler != null)
					handler.useSkill(this, skill, targets);
				else
					skill.useSkill(this, targets);
			}

			if(this instanceof L2PcInstance)
			{
				for (L2Object obj : targets)
				{
					if(obj instanceof L2CastleGuardInstance)
					{
						L2CastleGuardInstance guard = (L2CastleGuardInstance) obj;
						guard.maybeLearnSkill((L2PcInstance) this,skill);
					}
				}
			}

		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "", e);
		}
	}

	/**
	 * Return True if the L2Character is behind the target and can't be seen.<BR><BR>
	 */
	public boolean isBehindTarget()
	{
		if(getTarget() instanceof L2Character)
		{
			int head = getHeadingTo((L2Character)getTarget(), true);
			if(head == -1)
				return false;
			if(head <= 10430 || head >= 55105)
				return true;
			return false;
		}

		_log.fine("isBehindTarget's target not an L2 Character.");
		return false;
	}

	/**
	 * Return True if the target is front L2Character and can be seen.<BR><BR>
	 * degrees = 0..180, front->sides->back
	 */
	public boolean isInFront(L2Character target, int degrees)
	{
		int head = getHeadingTo(target, false);
		if(head <= 32768*degrees/180 || head >= (65536 - 32768*degrees/180))
			return true;
		return false;
	}
	
	/**
	 * Return heading to L2Character<BR>
	 * If <b>boolean toChar</b> is true heading calcs this->target,
	 * else target->this.<BR><BR>
	 */

	public int getHeadingTo(L2Character target, boolean toChar)
	{
		if(target == null || target == this)
			return -1;
		
		int dx = target.getX() - getX();
		int dy = target.getY() - getY();
		int heading = (int) (Math.atan2(-dy,-dx) * 32768./Math.PI + 32768);
		if(toChar)
			heading = target.getHeading() - heading;
		else
			heading = getHeading() - heading;

		if(heading < 0)
			heading += 65536;
		return heading;
	}

	/**
	 * Update active skills in progress (In Use and Not In Use because stacked) icones on client.<BR><BR>
	 *
	 * <B><U> Concept</U> :</B><BR><BR>
	 * All active skills effects in progress (In Use and Not In Use because stacked) are represented by an icone on the client.<BR><BR>
	 *
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method ONLY UPDATE the client of the player and not clients of all players in the party.</B></FONT><BR><BR>
	 *
	 */
	public final void updateEffectIcons()
	{
		MagicEffectIcons mi = new MagicEffectIcons();
		PartySpelled ps = new PartySpelled();

		L2Effect[] effects = getAllEffects();

		if(this instanceof L2PcInstance)
		{
			if(((L2PcInstance)this).getWeightPenalty() > 0)
				mi.addEffect(4270, ((L2PcInstance)this).getWeightPenalty(), -1);

			if(((L2PcInstance)this).getexpertisePenalty() > 0)
				mi.addEffect(4267, ((L2PcInstance)this).getexpertisePenalty(), -1);
		}

		if(effects != null)
		{
			for(int i = 0; i < effects.length; i++)
			{
				L2Effect effect = effects[i];
				if(effect == null)
					continue;

				if(effect.getInUse())
				{
					effect.addIcon(mi);
					if(this instanceof L2PcInstance && ((L2PcInstance)this).isInParty())
					{
						effect.addPartySpelledIcon((L2PcInstance)this, ps);
					}
				}
			}
		}
		
		sendPacket(mi);
		if((this instanceof L2PcInstance) && (((L2PcInstance)this).isInParty()))
		{
			((L2PcInstance)this).getParty().broadcastToPartyMembers(ps);
		}

	}


	/**
	 * Launch and add L2Effect (including Stack Group management) to L2Character and update client magic icone.<BR><BR>
	 *
	 * <B><U> Concept</U> :</B><BR><BR>
	 * All active skills effects in progress on the L2Character are identified in ConcurrentHashMap(Integer,L2Effect) <B>_effects</B>.
	 * The Integer key of _effects is the L2Skill Identifier that has created the L2Effect.<BR><BR>
	 *
	 * Several same effect can't be used on a L2Character at the same time.
	 * Indeed, effects are not stackable and the last cast will replace the previous in progress.
	 * More, some effects belong to the same Stack Group (ex WindWald and Haste Potion).
	 * If 2 effects of a same group are used at the same time on a L2Character, only the more efficient (identified by its priority order) will be preserve.<BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Add the L2Effect to the L2Character _effects</li>
	 * <li>If this effect doesn't belong to a Stack Group, add its Funcs to the Calculator set of the L2Character (remove the old one if necessary)</li>
	 * <li>If this effect has higher priority in its Stack Group, add its Funcs to the Calculator set of the L2Character (remove previous stacked effect Funcs if necessary)</li>
	 * <li>If this effect has NOT higher priority in its Stack Group, set the effect to Not In Use</li>
	 * <li>Update active skills in progress icones on player client</li><BR>
	 *
	 */
	public final void addEffect(L2Effect newEffect)
	{
		synchronized (this)
		{
			if(_effects == null)
				_effects = new ConcurrentHashMap<Integer,L2Effect>();

			if(_stackedEffects == null)
				_stackedEffects = new HashMap<String, LinkedList<Integer>>();
		}

		// Add the L2Effect to all effect in progress on the L2Character
		L2Effect tempEffect = _effects.put(newEffect.getSkill().getId(), newEffect);

		// Check if a stack group is defined for this effect
		if(newEffect.getStackType().equals("none"))
		{
			// Remove all Func objects created by this L2Effect from the Calculator set of the L2Character
			if(tempEffect != null)
				removeStatsOwner(tempEffect);

			// Get the effect added to the L2Character _effects
			tempEffect = _effects.get(newEffect.getSkill().getId());

			// Set this L2Effect to In Use
			tempEffect.setInUse(true);

			// Add Funcs of this effect to the Calculator set of the L2Character
			addStatFuncs(newEffect.getStatFuncs());

			// Update active skills in progress icones on player client
			updateEffectIcons();

			return;
		}

		// Get the list of all stacked effects corresponding to the stack type of the L2Effect to add
		LinkedList<Integer> stackQueue = _stackedEffects.get(newEffect.getStackType());

		if(stackQueue == null)
			stackQueue = new LinkedList<Integer>();


		if(stackQueue.size() > 0)
		{
			// Get the first stacked effect of the Stack group selected
			tempEffect = _effects.get(stackQueue.peek());

			if(tempEffect != null)
			{
				// Remove all Func objects corresponding to this stacked effect from the Calculator set of the L2Character
				removeStatsOwner(tempEffect);

				// Set the L2Effect to Not In Use
				tempEffect.setInUse(false);
			}
		}

		// Add the new effect to the stack group selected at its position
		stackQueue = effectQueueInsert(newEffect.getSkill().getId(), newEffect.getStackOrder(), stackQueue);
		if(stackQueue == null)
			return;

		//TODO Verify if it's possible to remove this put function beacuse it will done after updateEffectIcons
		// Update the Stack Group table _stackedEffects of the L2Character
		_stackedEffects.put(newEffect.getStackType(), stackQueue);

		// Get the first stacked effect of the Stack group selected
		tempEffect = _effects.get(stackQueue.peek());

		// Set this L2Effect to In Use
		tempEffect.setInUse(true);

		// Add all Func objects corresponding to this stacked effect to the Calculator set of the L2Character
		addStatFuncs(tempEffect.getStatFuncs());

		// Update active skills in progress (In Use and Not In Use because stacked) icones on client
		updateEffectIcons();

		// Update the Stack Group table _stackedEffects of the L2Character
		_stackedEffects.put(newEffect.getStackType(), stackQueue);
	}


	/**
	 * Insert an effect at the specified position in a Stack Group.<BR><BR>
	 *
	 * <B><U> Concept</U> :</B><BR><BR>
	 * Several same effect can't be used on a L2Character at the same time.
	 * Indeed, effects are not stackable and the last cast will replace the previous in progress.
	 * More, some effects belong to the same Stack Group (ex WindWald and Haste Potion).
	 * If 2 effects of a same group are used at the same time on a L2Character, only the more efficient (identified by its priority order) will be preserve.<BR><BR>
	 *
	 * @param id The identifier of the stacked effect to add to the Stack Group
	 * @param stackOrder The position of the effect in the Stack Group
	 * @param stackQueue The Stack Group in wich the effect must be added
	 *
	 */
	private LinkedList<Integer> effectQueueInsert(int id, @SuppressWarnings("unused") int stackOrder, LinkedList<Integer> stackQueue)
	{
		// Get the L2Effect corresponding to the Effect Identifier from the L2Character _effects
		if(_effects == null)
			return null;

		L2Effect tempEffect = _effects.get(id);

		// Create an Iterator to go through the list of stacked effects in progress on the L2Character
		Iterator<Integer> queueIterator = stackQueue.iterator();

		int i = 0;
		while (queueIterator.hasNext() && tempEffect.getStackOrder() < _effects.get(queueIterator.next()).getStackOrder())
		{i++;
		}

		// Add the new effect to the Stack list in function of its position in the Stack group
		stackQueue.add(i, id);

//		if(Config.EFFECT_CANCELING && stackQueue.size() > 1) //TODO: make a better solution
//			stackQueue.remove(1); //only keep the current effect, cancel other effects

		return stackQueue;
	}

	/**
	 * Stop and remove L2Effect (including Stack Group management) from L2Character and update client magic icone.<BR><BR>
	 *
	 * <B><U> Concept</U> :</B><BR><BR>
	 * All active skills effects in progress on the L2Character are identified in ConcurrentHashMap(Integer,L2Effect) <B>_effects</B>.
	 * The Integer key of _effects is the L2Skill Identifier that has created the L2Effect.<BR><BR>
	 *
	 * Several same effect can't be used on a L2Character at the same time.
	 * Indeed, effects are not stackable and the last cast will replace the previous in progress.
	 * More, some effects belong to the same Stack Group (ex WindWald and Haste Potion).
	 * If 2 effects of a same group are used at the same time on a L2Character, only the more efficient (identified by its priority order) will be preserve.<BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Remove Func added by this effect from the L2Character Calculator (Stop L2Effect)</li>
	 * <li>If the L2Effect belongs to a not empty Stack Group, replace theses Funcs by next stacked effect Funcs</li>
	 * <li>Remove the L2Effect from _effects of the L2Character</li>
	 * <li>Update active skills in progress icones on player client</li><BR>
	 *
	 */
	public final void removeEffect(L2Effect effect)
	{
		if(effect.getStackType() == "none")
		{
			// Remove Func added by this effect from the L2Character Calculator
			removeStatsOwner(effect);
		}
		else
		{
			// Get the list of all stacked effects corresponding to the stack type of the L2Effect to add
			LinkedList<Integer> stackQueue = _stackedEffects.get(effect.getStackType());

			if(stackQueue.size() < 1)
				stackQueue = null;

			if(stackQueue == null)
				return;

			// Get the Identifier of the first stacked effect of the Stack group selected
			int frontEffect = stackQueue.peek();

			// Remove the effect from the Stack Group
			boolean removed = stackQueue.remove((Integer)effect.getSkill().getId());

			if(removed)
			{
				// Check if the first stacked effect was the effect to remove
				if(frontEffect == effect.getSkill().getId())
				{
					// Remove all its Func objects from the L2Character calculator set
					removeStatsOwner(effect);

					// Check if there's another effect in the Stack Group
					if(stackQueue.size() > 0)
					{
						// Add its list of Funcs to the Calculator set of the L2Character
						addStatFuncs(_effects.get(stackQueue.peek()).getStatFuncs());

						// Set the effect to In Use
						_effects.get(stackQueue.peek()).setInUse(true);
					}
				}
			}

			// Update the Stack Group table _stackedEffects of the L2Character
			_stackedEffects.put(effect.getStackType(), stackQueue);
		}


		synchronized (this)
		{
			if(_effects == null)
				return;

			// Remove the active skill L2effect from _effects of the L2Character
			// The Integer key of _effects is the L2Skill Identifier that has created the effect
			_effects.remove(effect.getSkill().getId());

			if(_effects.isEmpty())
				_effects = null;
		}

		// Update active skills in progress (In Use and Not In Use because stacked) icones on client
		updateEffectIcons();

	}

	/**
	 * Return L2Effect in progress on the L2Character corresponding to the L2Skill Identifier.<BR><BR>
	 *
	 * <B><U> Concept</U> :</B><BR><BR>
	 * All active skills effects in progress on the L2Character are identified in ConcurrentHashMap(Integer,L2Effect) <B>_effects</B>.
	 * The Integer key of _effects is the L2Skill Identifier that has created the L2Effect.<BR><BR>
	 *
	 * @param index The L2Skill Identifier of the L2Effect to return from the _effects
	 *
	 * @return The L2Effect corresponding to the L2Skill Identifier
	 *
	 */
	public final L2Effect getEffect(int index)
	{
		ConcurrentHashMap<Integer,L2Effect> effects = _effects;

		if(effects == null)
			return null;

		return effects.get(index);
	}

	/**
	 * Return the first L2Effect in progress on the L2Character created by the L2Skill.<BR><BR>
	 *
	 * <B><U> Concept</U> :</B><BR><BR>
	 * All active skills effects in progress on the L2Character are identified in ConcurrentHashMap(Integer,L2Effect) <B>_effects</B>.
	 * The Integer key of _effects is the L2Skill Identifier that has created the L2Effect.<BR><BR>
	 *
	 * @param skill The L2Skill whose effect must be returned
	 *
	 * @return The first L2Effect created by the L2Skill
	 *
	 */
	public final L2Effect getEffect(L2Skill skill)
	{
		ConcurrentHashMap<Integer,L2Effect> effects = _effects;

		if(effects == null)
			return null;

		for (L2Effect e : effects.values())
		{
			if(e.getSkill() == skill)
				return e;
		}

		return null;
	}


	/**
	 * Return the first L2Effect in progress on the L2Character corresponding to the Effect Type (ex : BUFF, STUN, ROOT...).<BR><BR>
	 *
	 * <B><U> Concept</U> :</B><BR><BR>
	 * All active skills effects in progress on the L2Character are identified in ConcurrentHashMap(Integer,L2Effect) <B>_effects</B>.
	 * The Integer key of _effects is the L2Skill Identifier that has created the L2Effect.<BR><BR>
	 *
	 * @param tp The Effect Type of skills whose effect must be returned
	 *
	 * @return The first L2Effect corresponding to the Effect Type
	 *
	 */
	public final L2Effect getEffect(L2Effect.EffectType tp)
	{
		ConcurrentHashMap<Integer,L2Effect> effects = _effects;
		if(effects == null)
			return null;
		for (L2Effect e : effects.values())
		{
			if(e.getEffectType() == tp)
				return e;
		}
		return null;
	}


	/**
	 * Return all active skills effects in progress on the L2Character.<BR><BR>
	 *
	 * <B><U> Concept</U> :</B><BR><BR>
	 * All active skills effects in progress on the L2Character are identified in <B>_effects</B>.
	 * The Integer key of _effects is the L2Skill Identifier that has created the effect.<BR><BR>
	 *
	 * @return A table containing all active skills effect in progress on the L2Character
	 *
	 */
	public final L2Effect[] getAllEffects()
	{
		// Create a copy of the effects set
		ConcurrentHashMap<Integer,L2Effect> effects = _effects;

		// If no effect found, return EMPTY_EFFECTS
		if(effects == null || effects.isEmpty())
			return EMPTY_EFFECTS;

		// Return all effects in progress in a table
		return effects.values().toArray(new L2Effect[effects.size()]);
	}

	/**
	 * Stop and remove the L2Effect corresponding to the L2Skill Identifier and update client magic icone.<BR><BR>
	 *
	 * <B><U> Concept</U> :</B><BR><BR>
	 * All active skills effects in progress on the L2Character are identified in ConcurrentHashMap(Integer,L2Effect) <B>_effects</B>.
	 * The Integer key of _effects is the L2Skill Identifier that has created the L2Effect.<BR><BR>
	 *
	 * @param effectId The L2Skill Identifier of the L2Effect to remove from _effects
	 *
	 */
	public final void stopEffect(int effectId)
	{
		L2Effect effect = getEffect(effectId);

		if(effect != null)
			effect.exit();
	}


	/**
	 * Stop and remove all L2Effect of the selected type (ex : BUFF, DMG_OVER_TIME...) from the L2Character and update client magic icone.<BR><BR>
	 *
	 * <B><U> Concept</U> :</B><BR><BR>
	 * All active skills effects in progress on the L2Character are identified in ConcurrentHashMap(Integer,L2Effect) <B>_effects</B>.
	 * The Integer key of _effects is the L2Skill Identifier that has created the L2Effect.<BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Remove Func added by this effect from the L2Character Calculator (Stop L2Effect)</li>
	 * <li>Remove the L2Effect from _effects of the L2Character</li>
	 * <li>Update active skills in progress icones on player client</li><BR><BR>
	 *
	 * @param type The type of effect to stop ((ex : BUFF, DMG_OVER_TIME...)
	 *
	 */
	public final void stopEffects(L2Effect.EffectType type)
	{
		// Get all active skills effects in progress on the L2Character
		L2Effect[] effects = getAllEffects();

		if(effects != null)
		{
			// Go through all active skills effects
			for(L2Effect e : effects)
			{
				// Stop active skills effects of the selected type
				if(e.getEffectType() == type)
					e.exit();
			}
		}
	}

	/**
	 * Stop all active skills effects in progress on the L2Character.<BR><BR>
	 */
	public final void stopAllEffects()
	{
		// Get all active skills effects in progress on the L2Character
		L2Effect[] effects = getAllEffects();

		if(effects != null)
		{
			// Go through all active skills effects
			for(L2Effect e : effects)
				stopEffect(e.getSkill().getId());
		}

	}

	/**
	 * Return 1.<BR><BR>
	 */
	public double getLevelMod()
	{
		return 1;
	}

	/**
	 * Not Implemented.<BR><BR>
	 */
	public void updateStats()
	{
	}

	private Future _PvPRegTask;

	private long _lastPvpAttack;

	public void setlastPvpAttack(long time)
	{
		_lastPvpAttack = time;
	}

	public long getlastPvpAttack()
	{
		return _lastPvpAttack;
	}

	public void startPvPFlag()
	{
		updatePvPFlag(1);

		_PvPRegTask = ClientScheduler.getInstance().scheduleLowAtFixedRate(
				new PvPFlag(), 1000, 1000);
	}

	public void stopPvPFlag() {
		if(_PvPRegTask != null) {
			_PvPRegTask.cancel(false);
		}

		updatePvPFlag(0);

		_PvPRegTask = null;
	}

	public void updatePvPFlag(int value) 
	{
		if(!(this instanceof L2PcInstance))
			return;
		L2PcInstance player = (L2PcInstance)this;
		if(player.getPvpFlag() == value)
			return;
		player.setPvpFlag(value);

		if(player.getKarma() < 1)
			broadcastPacket(new StatusUpdate(getObjectId()).addAttribute(StatusUpdate.PVP_FLAG, value));
	}

	/**
	 * Teleport a L2Character and its pet if necessary.<BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Stop the movement of the L2Character</li>
	 * <li>Set the x,y,z position of the L2Object and if necessary modify its _worldRegion</li>
	 * <li>Send a Server->Client packet TeleportToLocationt to the L2Character AND to all L2PcInstance in its _KnownPlayers</li>
	 * <li>Modify the position of the pet if necessary</li><BR><BR>
	 *
	 */

	public final void teleToLocation(Location loc)
	{
		teleToLocation(loc.getX(), loc.getY(), loc.getZ());
	}

	public final void teleToLocation(int x, int y, int z)
	{
		// Stop movement
		setAttacked(false);
        setIsTeleporting(true);
		setTarget(null);
		stopMove(null);

		if(Config.DEBUG) _log.fine("Teleporting to: "+x+", "+y+", "+z);

		// Send a Server->Client packet TeleportToLocationt to the L2Character AND to all L2PcInstance in the _KnownPlayers of the L2Character
		broadcastPacket(new TeleportToLocation(this,x,y,z));

		// Modify the position of the pet if necessary

		decayMe();

		// Set the x,y,z position of the L2Object and if necessary modify its _worldRegion
		setXYZInvisible(x, y, z);

        if(getPet() != null)
            getPet().teleToLocation(getX()+Rnd.get(-100,100), getY()+Rnd.get(-100,100), getZ());
//		stopMove(null);
//		setAttacked(false);
//		setTarget(null);
        if (!(this instanceof L2PcInstance)) onTeleported();
	}

    public void onTeleported()
    {
        setIsTeleporting(false);
        spawnMe(getX(), getY(), getZ());

        // Modify the position of the pet if necessary
        if(getPet() != null) getPet().teleToLocation(getX() + Rnd.get(-100,100), getY() + Rnd.get(-100,100), getZ());
    }

    public final boolean isTeleporting() { return _IsTeleporting; }
    public final void setIsTeleporting(boolean value) { _IsTeleporting = value; }
	/**
	 * Return the L2Summon of the L2Character.<BR><BR>
	 *
	 * <B><U> Overriden in </U> :</B><BR><BR>
	 * <li> L2PcInstance</li><BR><BR>
	 *
	 */
	public L2Summon getPet()
	{
		return null;
	}

	public L2PcInstance getPcInstance()
	{
		if(this instanceof L2PcInstance)
			return (L2PcInstance)this;
		if(this instanceof L2Summon)
			return ((L2Summon)this).getOwner();
		return null;
	}

	/**
	 * Return a Random Damage in function of the weapon.<BR><BR>
	 */
	public final int getRandomDamage(@SuppressWarnings("unused") L2Character target)
	{
		L2Weapon weaponItem = getActiveWeaponItem();

		if(weaponItem == null)
			return 5+(int)Math.sqrt(getLevel());

		return weaponItem.getRandomDamage();
	}

	public String toString()
	{
		return "mob "+this.getObjectId();
	}

	public boolean isUndead()
	{
		return false;
	}
	
	public void setAttacked(boolean isAttacked)
	{
		if(Config.DEBUG && isAttacked != _isAttacked)
		{
			_log.fine("Character " + getName() + " is attaked?" + Boolean.toString(isAttacked));
		}
		if(_isAttacked || isAttacked)
		{
			_lastAttackChange = GameTimeController.getGameTicks();
		}
		if(isAttacked == true && !_isAttacked)
		{
			// launch the task for attacking stance
			ClientScheduler.getInstance().scheduleLow(new AttackingStanceTask(), 15000);
			// set the attacking stance
		}
		_isAttacked = isAttacked;
	}
	
	public boolean isAttacked()
	{
		if(_isAttacked)
		{
			if(GameTimeController.getGameTicks() - _lastAttackChange > 15 * GameTimeController.TICKS_PER_SECOND)
			{
				_isAttacked = false;
				if(Config.DEBUG)
				{
					_log.fine("Unlocked isAttacked state");
				}
			}
		}
		else
		{
			if(GameTimeController.getGameTicks() - _lastAttackChange < 15 * GameTimeController.TICKS_PER_SECOND)
			{
				return true;
			}
		}
		return _isAttacked;
	}
	
	public int getAttackEndTime()
	{
		return _attackEndTime;
	}
	
	class AttackingStanceTask implements Runnable
	{

		public void run()
		{
			// if last attacked time is less than 15s
			if(GameTimeController.getGameTicks() - _lastAttackChange < 15 * GameTimeController.TICKS_PER_SECOND)
			{
				int time = 15 * GameTimeController.TICKS_PER_SECOND - (GameTimeController.getGameTicks() - _lastAttackChange);
				ClientScheduler.getInstance().scheduleLow(new AttackingStanceTask(), time);
			}
			// last attack time is less than 15s
			else if(GameTimeController.getGameTicks() - getAttackEndTime() < 15 * GameTimeController.TICKS_PER_SECOND)
			{
				int time = 15 * GameTimeController.TICKS_PER_SECOND - (GameTimeController.getGameTicks() - getAttackEndTime());
				ClientScheduler.getInstance().scheduleLow(new AttackingStanceTask(), time);
			}
			else
			{
				//stop attaking stance (back to normal)
			}
		}
	}

	public void jbfHandlersList(ConcurrentHashMap<Integer,ArrayList<Word>> handlersMap, Integer id)
	{
//		_log.info("do "+this+".jbfHandlersList("+handlersMap+", "+id+")");

		if(handlersMap==null || id==null)
			return;

		ArrayList<Word> handlersList = handlersMap.get(id);

		if(handlersList == null)
			return;

		JBForth jbf = new JBForth(this);
		
		for(Word w : handlersList)
		{
			if(w == null)
				continue;
			try 
			{
//				_log.info("Execute "+jbf+"."+w);
				w.exec(jbf); 
			} 
			catch(Exception e) {w.printStackTrace(jbf,e);}
		}
	}
}
