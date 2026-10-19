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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.ClientScheduler;
import net.sf.l2j.gameserver.GameTimeController;
import net.sf.l2j.gameserver.ai.CtrlEvent;
import net.sf.l2j.gameserver.ai.CtrlIntention;
import net.sf.l2j.gameserver.ai.L2CharacterAI;
import net.sf.l2j.gameserver.handler.ISkillHandler;
import net.sf.l2j.gameserver.handler.SkillHandler;
import net.sf.l2j.gameserver.model.L2Skill.SkillTargetType;
import net.sf.l2j.gameserver.model.L2Skill.SkillType;
import net.sf.l2j.gameserver.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.serverpackets.Attack;
import net.sf.l2j.gameserver.serverpackets.ChangeMoveType;
import net.sf.l2j.gameserver.serverpackets.MagicEffectIcons;
import net.sf.l2j.gameserver.serverpackets.MagicSkillCanceld;
import net.sf.l2j.gameserver.serverpackets.MagicSkillLaunched;
import net.sf.l2j.gameserver.serverpackets.MagicSkillUser;
import net.sf.l2j.gameserver.serverpackets.ServerBasePacket;
import net.sf.l2j.gameserver.serverpackets.SetupGauge;
import net.sf.l2j.gameserver.serverpackets.StatusUpdate;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.serverpackets.TeleportToLocation;
import net.sf.l2j.gameserver.skills.Calculator;
import net.sf.l2j.gameserver.skills.Env;
import net.sf.l2j.gameserver.skills.Formulas;
import net.sf.l2j.gameserver.skills.Func;
import net.sf.l2j.gameserver.skills.Stats;
import net.sf.l2j.gameserver.templates.L2CharTemplate;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;
import net.sf.l2j.gameserver.templates.L2PcTemplate;
import net.sf.l2j.gameserver.templates.L2Weapon;
import net.sf.l2j.gameserver.templates.L2WeaponType;
import net.sf.l2j.util.L2ObjectHashSet;

/**
 * This class ...
 * 
 * @version $Revision: 1.53.2.45.2.34 $ $Date: 2005/04/11 10:06:08 $
 */
public abstract class L2Character extends L2Object
{	
    protected static final Logger _log = Logger.getLogger(L2Character.class.getName());

	public static final int ABNORMAL_EFFECT_BLEEDING   = 0x0001; // not sure
	public static final int ABNORMAL_EFFECT_POISON     = 0x0002;
	public static final int ABNORMAL_EFFECT_UNKNOWN_3  = 0x0004;
	public static final int ABNORMAL_EFFECT_UNKNOWN_4  = 0x0008;
	public static final int ABNORMAL_EFFECT_UNKNOWN_5  = 0x0010;
	public static final int ABNORMAL_EFFECT_UNKNOWN_6  = 0x0020;
	public static final int ABNORMAL_EFFECT_STUN       = 0x0040;
	public static final int ABNORMAL_EFFECT_SLEEP  	   = 0x0080;
	public static final int ABNORMAL_EFFECT_MUTED      = 0x0100; // not sure
	public static final int ABNORMAL_EFFECT_ROOT       = 0x0200;
	public static final int ABNORMAL_EFFECT_HOLD_1     = 0x0400;
	public static final int ABNORMAL_EFFECT_HOLD_2     = 0x0800;
	public static final int ABNORMAL_EFFECT_UNKNOWN_13 = 0x1000;
	public static final int ABNORMAL_EFFECT_BIG_HEAD   = 0x2000;
	public static final int ABNORMAL_EFFECT_FLAME      = 0x4000; 
	// FIXME : correct the value of the abnormal effect confused
	public static final int ABNORMAL_EFFECT_CONFUSED   = 0x0020;
	// FIXME : correct the value of the abnormal effect affraid
	public static final int ABNORMAL_EFFECT_AFFRAID    = 0x0010;

	/** This class may be created only by L2Character and only for AI */
	public class AIAccessor
	{
		protected AIAccessor() {}
		public L2Character getActor() { return L2Character.this; }
		public void moveTo(int x, int y, int z, int offset) {
			L2Character.this.moveToLocation(x, y, z, offset);
		}
		public void moveTo(int x, int y, int z) {
			L2Character.this.moveToLocation(x, y, z, 0);
		}
		public void stopMove(L2CharPosition pos) {
			L2Character.this.stopMove(pos);
		}
		public void doAttack(L2Character target) {
			L2Character.this.doAttack(target);
		}
		public void doCast(L2Skill skill) {
			L2Character.this.doCast(skill);
		}
		public NotifyAITask newNotifyTask(CtrlEvent evt) {
			return new NotifyAITask(evt);
		}
		public void detachAI() {
			L2Character.this._ai = null;
		}
	}
	
	
	public class NotifyAITask implements Runnable
	{
		private final CtrlEvent _evt;
		NotifyAITask(CtrlEvent evt) {
			this._evt = evt;
		}
		public void run()
		{
			try
			{
				getAI().notifyEvent(_evt, null);
			} catch (Throwable t) {
				_log.log(Level.WARNING, "", t);
			}
		}
	}
	
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
	
	class RegenTask implements Runnable
	{
		
		public void run()
		{
			try
			{
				double addHp = 0;
				double addMp = 0;
				if (getCurrentHp() < getMaxHp())
					addHp = Formulas.getInstance().calcHpRegen(L2Character.this); 
				if (getCurrentMp() < getMaxMp())
					addMp = Formulas.getInstance().calcMpRegen(L2Character.this); 
				setCurrentHpMp(getCurrentHp() + addHp, getCurrentMp() + addMp);
				if (getCurrentCp() < getMaxCp()) {
					double addCp = Formulas.getInstance().calcCpRegen(L2Character.this); 
					setCurrentCp(getCurrentCp() + addCp);
				}
			}
			catch (Throwable e)
			{
				_log.log(Level.SEVERE, "", e);
			}
		}
	}
	
	class MagicUseTask implements Runnable
	{
		L2Object[] _targets;
		L2Skill _skill;
		
		public MagicUseTask(L2Object[] targets, L2Skill skill)
		{
			_targets = targets;
			_skill = skill;
		}
		
		public void run()
		{
			try
			{
				onMagicUseTimer(_targets, _skill);
			}
			catch (Throwable e)
			{
				_log.log(Level.SEVERE, "", e);
			}
		}
	}
	
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
	
	class EnableAllSkills implements Runnable
	{
		L2Skill _skill;
		
		public EnableAllSkills(L2Skill skill)
		{
			_skill = skill;
		}
		
		public void run()
		{
			try
			{
				enableAllSkills();
			} catch (Throwable e) {
				_log.log(Level.SEVERE, "", e);
			}
		}
	}
	
    class PvPFlag implements Runnable
    {
        public void run() 
        {
            try 
            {
                 if (Config.DEBUG) _log.fine("Checking pvp time: " + getlastPvpAttack());
                // "lastattack: " _lastAttackTime "currenttime: "
                // System.currentTimeMillis());
                if (Math.abs(System.currentTimeMillis()-getlastPvpAttack()) > 10000) 
                {
                    //  _log.fine("Stopping PvP");
                    stopPvPFlag();
                } 
                else 
                {
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
    
	static class MoveData {
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
	
    // Template for stats, whould like to make it
    // final, but players can change it's class, thus template
    protected L2CharTemplate _template;
    
	protected int[] _disabledSkills;
	private boolean _allSkillsDisabled;
	protected final HashMap<Integer, L2Skill> _skills;
	
	// this array contains all clients that need to be notified about hp/mp updates
	private CopyOnWriteArraySet<L2Character> _statusListener;
	
	private Future _regTask;
	private int    _flagsRegenActive;
	private static final int HP_REGEN_FLAG = 1;
	private static final int MP_REGEN_FLAG = 2;
	private static final int CP_REGEN_FLAG = 4;
	
	private final static Random _rnd = new Random();
	
	private String   _name;
    protected String _title;
	private double   _currentHp;
	private double   _currentMp;
	private double   _currentCp;
	private boolean  _running;
	private boolean  _flying = false; //Is flying Wyvern?
	private boolean  _ridding = false; //Is Ridding strider?
	
//	private int _flyingRunSpeed;
//	private int _floatingWalkSpeed;
//	private int _flyingWalkSpeed;
//	private int _floatingRunSpeed;
	
	private MoveData _move;
	
	private int _heading;
	
	private static final L2Effect[] EMPTY_EFFECTS = new L2Effect[0];
	private ConcurrentHashMap<Integer,L2Effect> _effects;
	
	private L2Object _target;
	
	// setted by the start of casting, in game ticks
	private int     _castEndTime;
	private int     _castInterruptTime;
	
	private boolean _killedAlready;
	private boolean _inCombat;
	
	// setted by the start of attack, in game ticks
	private int     _attackEndTime;
	private int     _attacking;
    private int     _disableBowAttackEndTime;

	/** Cannot move/attack until stun timed out */
	private boolean _stuned;
	/** Cannot move until root timed out */
	private boolean _rooted;
	/** Cannot move/attack until sleep timed out or monster is attacked */
	private boolean _sleeping;
	/** Fake death */
	private boolean _fake_death;	
	/** Cannot use magic **/
	private boolean _muted;
	/** Attack anyone randomly **/
	private boolean _confused;
	/** Flee in a random direction **/
	private boolean _affraid;
	/** the char is carrying too much **/
	private boolean _overloaded;
	/** Others abnormal effect */
	private short   _abnormalEffects;
	
	
	private static final Calculator[] NPC_STD_CALCULATOR;
	static {
		NPC_STD_CALCULATOR = Formulas.getInstance().getStdNPCCalculators();
	}
	private Calculator[] _calculators;
	
	private L2ObjectHashSet<L2Object> _knownObjects = new L2ObjectHashSet<L2Object>();
	private L2ObjectHashSet<L2PcInstance> _knownPlayer = new L2ObjectHashSet<L2PcInstance>();

	protected L2CharacterAI _ai;

	public L2Character(int objectId, L2CharTemplate template)
	{
		super(objectId);
		_template = template;
		_overloaded = false;
		
		if (this instanceof L2NpcInstance) {
			_skills = ((L2NpcTemplate)template).getSkills();
			_calculators = NPC_STD_CALCULATOR;
		} else {
			_skills = new HashMap<Integer,L2Skill>();
			_calculators = new Calculator[Stats.NUM_STATS];
			Formulas.getInstance().addFuncsToNewCharacter(this);
		}
	}
	
	public L2CharTemplate getTemplate()
	{
		return _template;
	}
	protected final void setTemplate(L2PcTemplate template)
	{
		if (Config.ASSERT) assert this instanceof L2PcInstance;
		_template = template;
	}
	
	public L2CharacterAI getAI() {
		if (_ai == null)
		{
			synchronized(this)
			{
				if (_ai == null)
					_ai = new L2CharacterAI(new AIAccessor());
			}
		}
		return _ai;
	}
	
	public boolean hasAI() {
		return _ai != null;
	}
	
	/**
	 * 
	 */
	public void onDecay()
	{
		this.decayMe();
	}
	
	
	public void addStatusListener(L2Character object)
	{
		if (object == this)
			return;
		CopyOnWriteArraySet<L2Character> listeners;
		synchronized (this)
		{
			if (_statusListener == null)
				_statusListener = new CopyOnWriteArraySet<L2Character>();
			listeners = _statusListener;
		}
		listeners.add(object);
	}
	
	/** @return may return null */
	private CopyOnWriteArraySet<L2Character> getStatusListener()
	{
		return _statusListener;
	}
	
	public void removeStatusListener(L2Character object)
	{
		CopyOnWriteArraySet<L2Character> listeners;
		synchronized (this)
		{
			if (_statusListener == null)
				return;
			else
				listeners = _statusListener;
		}
		listeners.remove(object);
		if (listeners.isEmpty())
		{
			synchronized (this)
			{
				if (_statusListener != null && _statusListener.isEmpty())
					_statusListener = null;
			}
		}
	}
	
	public final synchronized void addStatFunc(Func f)
	{
		if (f == null)
			return;
		if (_calculators == NPC_STD_CALCULATOR) {
			_calculators = new Calculator[Stats.NUM_STATS];
			for (int i=0; i < Stats.NUM_STATS; i++) {
				if (NPC_STD_CALCULATOR[i] != null)
					_calculators[i] = new Calculator(NPC_STD_CALCULATOR[i]);
			}
		}
		int stat = f._stat.ordinal();
		if (_calculators[stat] == null)
			_calculators[stat] = new Calculator();
		_calculators[stat].addFunc(f);
	}
	
	public final synchronized void addStatFuncs(Func[] funcs)
	{
		for (Func f : funcs)
			addStatFunc(f);
	}
	
	public final synchronized void removeStatFunc(Func f)
	{
		if (f == null)
			return;
		int stat = f._stat.ordinal();
		if (_calculators[stat] == null)
			return;
		_calculators[stat].removeFunc(f);
		if (_calculators[stat].size() == 0)
			_calculators[stat] = null;
		if (this instanceof L2NpcInstance) {
			int i = 0;
			for (; i < Stats.NUM_STATS; i++) {
				if (!Calculator.equalsCals(_calculators[i], NPC_STD_CALCULATOR[i]))
					break;
			}
			if (i >= Stats.NUM_STATS)
				_calculators = NPC_STD_CALCULATOR;
		}
	}
	
	public final synchronized void removeStatFuncs(Func[] funcs)
	{
		for (Func f : funcs)
			removeStatFunc(f);
	}
	
	public final synchronized void removeStatsOwner(Object owner)
	{
		for (int i=0; i < _calculators.length; i++) {
			if (_calculators[i] != null) {
				_calculators[i].removeOwner(owner);
				if (_calculators[i].size() == 0)
					_calculators[i] = null;
			}
		}
		if (this instanceof L2NpcInstance) {
			int i = 0;
			for (; i < Stats.NUM_STATS; i++) {
				if (!Calculator.equalsCals(_calculators[i], NPC_STD_CALCULATOR[i]))
					break;
			}
			if (i >= Stats.NUM_STATS)
				_calculators = NPC_STD_CALCULATOR;
		}
	}
	
	public final double calcStat(Stats stat, double init, L2Character target, L2Skill skill) {
		int id = stat.ordinal();
		Calculator c = _calculators[id];
		if (c == null || c.size() == 0)
			return init;
		Env env = new Env();
		env._player = this;
		env._target = target;
		env._skill = skill;
		env.value = init;
		c.calc(env);
		return env.value;
	}

    public short getAbnormalEffect()
    {
		short ae = _abnormalEffects;
		if (_stuned)   ae |= ABNORMAL_EFFECT_STUN;
		if (_rooted)   ae |= ABNORMAL_EFFECT_ROOT;
		if (_sleeping) ae |= ABNORMAL_EFFECT_SLEEP;
		if (_confused) ae |= ABNORMAL_EFFECT_CONFUSED;
		if (_muted)	   ae |= ABNORMAL_EFFECT_MUTED;
		if (_affraid)  ae |= ABNORMAL_EFFECT_AFFRAID;
        return ae;
    }

	public abstract void updateAbnormalEffect();
	
	public final boolean isAllSkillsDisabled()
	{
		return _allSkillsDisabled || _stuned || _sleeping;
	}

	
	// disabled by stun, root, sleep, overload
	public final boolean isMovementDisabled()
	{
		return _stuned || _rooted || _sleeping || _overloaded;
	}

	// disabled by stun, sleep
	public final boolean isAttackingDisabled()
	{
		return _stuned || _sleeping || _attackEndTime > GameTimeController.getGameTicks();
	}

	public final boolean isConfused()
	{
		return _confused;
	}

	public final boolean isAffraid()
	{
		return _affraid;
	}
	/** Can the character be controlled by the player **/
	public boolean isOutOfControl() {
		return _confused || _affraid;
	}
	
	public final boolean isMuted()
	{
		return _muted;
	}

	public void setOverloaded(boolean overloaded)
	{
		_overloaded = overloaded;
	}

	public final void startStunning() {
		_stuned   = true;
		getAI().notifyEvent(CtrlEvent.EVT_STUNNED, null);
		updateAbnormalEffect();
	}
	public final void startSleeping() {
		_sleeping = true;
		getAI().notifyEvent(CtrlEvent.EVT_SLEEPING, null);
		updateAbnormalEffect();
	}
	public final void startRooted()   {
		_rooted   = true;
		getAI().notifyEvent(CtrlEvent.EVT_ROOTED, null);
		updateAbnormalEffect();
	}
	public final void startFakeDeath()   {
		_fake_death = true;
		getAI().notifyEvent(CtrlEvent.EVT_DEAD, null);
	}
	public final void startConfused() {
		_confused = true;
		getAI().notifyEvent(CtrlEvent.EVT_CONFUSED);
		updateAbnormalEffect();
	}
	public final void startMuted() {
		_muted = true;
		getAI().notifyEvent(CtrlEvent.EVT_MUTED);
		updateAbnormalEffect();
	}
	public final void startFear() {
		_affraid = true;
		getAI().notifyEvent(CtrlEvent.EVT_AFFRAID);
		updateAbnormalEffect();
	}
	public final void startAbnormalEffect(short mask) {
		_abnormalEffects |= mask;
		updateAbnormalEffect();
	}

	public final void stopStunning(L2Effect effect) {
		if (effect == null)
			stopEffects(L2Effect.EffectType.STUN);
		else
			removeEffect(effect);
		_stuned   = false;
		getAI().notifyEvent(CtrlEvent.EVT_THINK, null);
		updateAbnormalEffect();
	}
	public final void stopSleeping(L2Effect effect) {
		if (effect == null)
			stopEffects(L2Effect.EffectType.SLEEP);
		else
			removeEffect(effect);
		_sleeping = false;
		getAI().notifyEvent(CtrlEvent.EVT_THINK, null);
		updateAbnormalEffect();
	}
	public final void stopRooting(L2Effect effect)  {
		if (effect == null)
			stopEffects(L2Effect.EffectType.ROOT);
		else
			removeEffect(effect);
		_rooted   = false;
		getAI().notifyEvent(CtrlEvent.EVT_THINK, null);
		updateAbnormalEffect();
	}
	public final void stopFakeDeath(L2Effect effect)  {
		_fake_death   = false;
		if (effect == null)
			stopEffects(L2Effect.EffectType.FAKE_DEATH);
		else
			removeEffect(effect);
		getAI().notifyEvent(CtrlEvent.EVT_THINK, null);
	}
	public final void stopConfused(L2Effect effect) {
		if (effect == null)
			stopEffects(L2Effect.EffectType.CONFUSION);
		else
			removeEffect(effect);
		_confused = false;
		getAI().notifyEvent(CtrlEvent.EVT_THINK, null);
		updateAbnormalEffect();
			
	}
	public final void stopMuted(L2Effect effect) {
		
		if (effect == null)
			stopEffects(L2Effect.EffectType.MUTE);
		else
			removeEffect(effect);
		_muted = false;
		updateAbnormalEffect();
	}
	public final void stopFear(L2Effect effect) {
		if (effect == null)
			stopEffects(L2Effect.EffectType.FEAR);
		else
			removeEffect(effect);
		_affraid = false;
		updateAbnormalEffect();	
	}
	public final void stopAbnormalEffect(short mask)   {
		_abnormalEffects &= ~mask;
		updateAbnormalEffect();
	}
	
	/**
	 * @return
	 */
	public final int getHeading()
	{
		return _heading;
	}
	
	/**
	 * @param heading
	 */
	public final void setHeading(int heading)
	{
		_heading = heading;
	}
	
	/**
	 * @return
	 */
	public final int getXdestination()
	{
		MoveData m = _move;
		if (m != null)
			return m._xDestination;
		return getX();
	}
	
	/**
	 * @return
	 */
	public final int getYdestination()
	{
		MoveData m = _move;
		if (m != null)
			return m._yDestination;
		return getY();
	}
	
	/**
	 * @return
	 */
	public final int getZdestination()
	{
		MoveData m = _move;
		if (m != null)
			return m._zDestination;
		return getZ();
	}
	
	public final boolean isInCombat()
	{
		return _inCombat;
	}
	
	public final boolean isMoving()
	{
		return _move != null; 
	}
	
	public final boolean isCastingNow()
	{
		return _castEndTime > GameTimeController.getGameTicks();
	}
	
	public final boolean canAbortCast()
	{
		return _castInterruptTime > GameTimeController.getGameTicks();
	}
	
	public final boolean isAttackingNow()
	{
		return _attackEndTime > GameTimeController.getGameTicks();
	}
	
	public final boolean isAttackAborted()
	{
		return _attacking <= 0;
	}
	
	public final void abortAttack()
	{
		if (isAttackingNow()) {
			_attacking = 0;
			sendPacket(new ActionFailed());
		}
	}
	
	/** Returns body part (paperdoll slot) we are targeting right now */
	public final int getAttackingBodyPart()
	{
		return _attacking;
	}
	
	public final void abortCast()
	{
		if (isCastingNow()) {
			_castEndTime = 0;
			_castInterruptTime = 0;
			broadcastPacket(new MagicSkillCanceld(getObjectId()));
			sendPacket(new ActionFailed());
		}
	}
	
	/** A method called from GameTimeController each 0.1 sec to update object's position.
	 * @return true to exit movement
	 */
	public final boolean updatePosition(int gameTicks)
	{
		MoveData m = _move;
		if (m == null)
			return true;
		if (!isVisible()) {
			_move = null;
			return true;
		}
		if (m._moveTimestamp == gameTicks)
			return false;
		int elapsed = gameTicks - m._moveStartTime;
		if (elapsed >= m._ticksToMove)
		{
			m._moveTimestamp = gameTicks;
			super.setXYZ(m._xDestination, m._yDestination, m._zDestination);
			_move = null;
			return true;
		}
		super.setXYZ(
				m._xMoveFrom + (int)(elapsed * m._xSpeedTicks),
				m._yMoveFrom + (int)(elapsed * m._ySpeedTicks),
				super.getZ()
				);
		m._moveTimestamp = gameTicks;
		return false;
	}
	
	// called from AIAccessor only
    protected void stopMove(L2CharPosition pos)
	{
		_move = null;
		if (pos != null)
		{
			setXYZ(pos.x, pos.y, pos.z);
			setHeading(pos.heading);
		}
		updateKnownObjects();
	}
	
	/**
	 * @return
	 */
	public final double getCurrentHp()
	{
		return _currentHp;
	}
	
	/**
	 * @param currentHp
	 */
	public final void setCurrentHp(double newHp)
	{
		int maxHp = getMaxHp();
		synchronized (this)
		{
			if (newHp >= maxHp) {
				_currentHp = maxHp;
				_flagsRegenActive &= ~HP_REGEN_FLAG;
				if (_flagsRegenActive == 0)
					stopHpMpRegeneration();
			} else {
				_currentHp = newHp;
				_flagsRegenActive |= HP_REGEN_FLAG;
				startHpMpRegeneration();
			}
		}
		
		broadcastStatusUpdate();
	}
	
	/**
	 * 
	 */
	protected synchronized void stopHpMpRegeneration()
	{
		if (_regTask != null)
		{
			_regTask.cancel(false);
			_regTask = null;
			_flagsRegenActive = 0;
			if (Config.DEBUG) _log.fine("HP/MP/CP regen stop");
		}
	}
	
	/**
	 * 
	 */
	private synchronized void startHpMpRegeneration()
	{
		if (_regTask == null && !isDead()) {
			if (Config.DEBUG) _log.fine("HP/MP/CP regen started");
		
		int period = Formulas.getInstance().getRegeneratePeriod(this);
			_regTask = ClientScheduler.getInstance().scheduleMedAtFixedRate(
					new RegenTask(), period, period);
		}
	}
	
	/**
	 * @return
	 */
	public final double getCurrentMp()
	{
		return _currentMp;
	}
	
	/**
	 * @param currentMp
	 */
	public final void setCurrentMp(double newMp)
	{
		int maxMp = getMaxMp();
		synchronized (this)
		{
			if (newMp >= maxMp)
			{
				_currentMp = maxMp;
				_flagsRegenActive &= ~MP_REGEN_FLAG;
				if (_flagsRegenActive == 0)
					stopHpMpRegeneration();
			} else {
				_currentMp = newMp;
				_flagsRegenActive |= MP_REGEN_FLAG;
				startHpMpRegeneration();
			}
		}
		
		broadcastStatusUpdate();
	}
	
	/**
	 * @return
	 */
	public final double getCurrentCp()
	{
		return _currentCp;
	}
	
	/**
	 * @param currentMp
	 */
	public final void setCurrentCp(double newCp)
	{
		int maxCp = getMaxCp();
		if (newCp < 0)
			newCp = 0;
		synchronized (this)
		{
			if (newCp >= maxCp)
			{
				_currentCp = maxCp;
				_flagsRegenActive &= ~CP_REGEN_FLAG;
				if (_flagsRegenActive == 0)
					stopHpMpRegeneration();
			} else {
				_currentCp = newCp;
				_flagsRegenActive |= CP_REGEN_FLAG;
				startHpMpRegeneration();
			}
		}
		
		broadcastStatusUpdate();
	}
	
	/**
	 * @param currentMp
	 */
	public final void setCurrentHpMp(double newHp, double newMp)
	{
		int maxHp = getMaxHp();
		int maxMp = getMaxMp();
		synchronized (this)
		{
			if (newHp >= maxHp) {
				_killedAlready = false;
				_currentHp = maxHp;
				_flagsRegenActive &= ~HP_REGEN_FLAG;
			} else {
				_currentHp = newHp;
				if (!isDead()) {
					_killedAlready = false;
					_flagsRegenActive |= HP_REGEN_FLAG;
					startHpMpRegeneration();
				}
			}
	
			if (newMp >= maxMp) {
				_currentMp = maxMp;
				_flagsRegenActive &= ~MP_REGEN_FLAG;
			} else {
				_currentMp = newMp;
				if (!isDead()) {
					_flagsRegenActive |= MP_REGEN_FLAG;
					startHpMpRegeneration();
				}
			}
			if (_flagsRegenActive == 0)
				stopHpMpRegeneration();
		}
		
		broadcastStatusUpdate();
	}
	
	public void broadcastStatusUpdate()
	{
		CopyOnWriteArraySet<L2Character> list = getStatusListener();
		if (list == null || list.isEmpty())
		{
			return;
		}
		
		StatusUpdate su = new StatusUpdate(getObjectId());
		su.addAttribute(StatusUpdate.CUR_HP,(int)getCurrentHp());
		su.addAttribute(StatusUpdate.CUR_MP,(int)getCurrentMp());
		
		for (L2Character temp : list)
		{
			temp.sendPacket(su);
		}
	}
	
	
    public int getSTR() { return (int)calcStat(Stats.STAT_STR, _template.baseSTR, null, null); }
    public int getCON() { return (int)calcStat(Stats.STAT_CON, _template.baseCON, null, null); }
    public int getDEX() { return (int)calcStat(Stats.STAT_DEX, _template.baseDEX, null, null); }
    public int getINT() { return (int)calcStat(Stats.STAT_INT, _template.baseINT, null, null); }
    public int getWIT() { return (int)calcStat(Stats.STAT_WIT, _template.baseWIT, null, null); }
    public int getMEN() { return (int)calcStat(Stats.STAT_MEN, _template.baseMEN, null, null); }
	
	// may be overwritten, to watch changes of MAX_HP and for pets
	public int getMaxHp()
	{
        double val = calcStat(Stats.MAX_HP, _template.baseHpMax, null, null);
        return (int)val;
	}
	
	// may be overwritten, to watch changes of MAX_MP and for pets
	public int getMaxMp()
	{
        double val = calcStat(Stats.MAX_MP, _template.baseMpMax, null, null);
        return (int)val;
	}
	
	public final int getMaxCp()
	{
		return getMaxHp();
	}
	
	public final int getAccuracy()
	{
        double val = calcStat(Stats.ACCURACY_COMBAT, 0, null, null);
		val /= getWeaponExpertisePenalty();
        return (int)val;
	}
	
	public int getMAtk(L2Character target, L2Skill skill)
	{
		double attack = _template.baseMAtk;
		Stats stat = skill == null? null : skill.getStat();
		if (stat != null)
		{
			switch (stat)
			{
			case AGGRESSION: attack += _template.baseAggression; break;
			case BLEED:      attack += _template.baseBleed;      break;
			case POISON:     attack += _template.basePoison;     break;
			case STUN:       attack += _template.baseStun;       break;
			case ROOT:       attack += _template.baseRoot;       break;
			case MOVEMENT:   attack += _template.baseMovement;   break;
			case CONFUSION:  attack += _template.baseConfusion;  break;
			case SLEEP:      attack += _template.baseSleep;      break;
			case FIRE:       attack += _template.baseFire;       break;
			case WIND:       attack += _template.baseWind;       break;
			case WATER:      attack += _template.baseWater;      break;
			case EARTH:      attack += _template.baseEarth;      break;
			}
		}
		if (skill != null)
			attack += skill.getPower();
		return (int)calcStat(Stats.MAGIC_ATTACK, attack, target, skill);
	}
	
	public int getMDef(L2Character target, L2Skill skill)
	{
		double defence = _template.baseMDef;
		Stats stat = skill == null? null : skill.getStat();
		if (stat != null)
		{
			switch (stat)
			{
			case AGGRESSION: defence += _template.baseAggressionRes; break;
			case BLEED:      defence += _template.baseBleedRes;      break;
			case POISON:     defence += _template.basePoisonRes;     break;
			case STUN:       defence += _template.baseStunRes;       break;
			case ROOT:       defence += _template.baseRootRes;       break;
			case MOVEMENT:   defence += _template.baseMovementRes;   break;
			case CONFUSION:  defence += _template.baseConfusionRes;  break;
			case SLEEP:      defence += _template.baseSleepRes;      break;
			case FIRE:       defence += _template.baseFireRes;       break;
			case WIND:       defence += _template.baseWindRes;       break;
			case WATER:      defence += _template.baseWaterRes;      break;
			case EARTH:      defence += _template.baseEarthRes;      break;
			}
		}
		// Calculating elemental Defence
		if (skill!=null)
		{
			switch (skill.getElement())
			{
			case L2Skill.ELEMENT_EARTH:
				defence = calcStat(Stats.EARTH_RES,defence,target,skill); break;
			case L2Skill.ELEMENT_FIRE:
				defence = calcStat(Stats.FIRE_RES,defence,target,skill);break;
			case L2Skill.ELEMENT_WATER:
				defence = calcStat(Stats.WATER_RES,defence,target,skill);break;
			case L2Skill.ELEMENT_WIND:
				defence = calcStat(Stats.WIND_RES,defence,target,skill);break;
			}
		}

		return (int)calcStat(Stats.MAGIC_DEFENCE, defence, target, skill);
	}
	
    public final int getCriticalHit(L2Character target, L2Skill skill)
    {
        double rate = calcStat(Stats.CRITICAL_RATE, _template.baseCritRate, target, skill);
        return (int)rate;
    }
    
    public final double getCriticalDmg(L2Character target, double init)
    {
		double val = calcStat(Stats.CRITICAL_DAMAGE, init, target, null);
        return val;
    }
	
    public final int getAtkCancel()
    {
        double rate = calcStat(Stats.ATTACK_CANCEL, _template.baseAttackCancel, null, null);
        return (int)rate;
    }
	
	public final int getEvasionRate(L2Character target)
	{
        double val = calcStat(Stats.EVASION_RATE, 0, target, null);
		val /= getArmourExpertisePenalty();
        return (int)val;
	}

	public int getPAtk(L2Character target)
	{
		return (int)calcStat(Stats.POWER_ATTACK, _template.basePAtk, target, null);
	}
	
	public int getPDef(L2Character target)
	{
		return (int)calcStat(Stats.POWER_DEFENCE, _template.basePDef, target, null);
	}
	
	public final int getMAtkSpd()
	{
		double val = calcStat(Stats.MAGIC_ATTACK_SPEED, _template.baseMAtkSpd, null, null);
		val /= getArmourExpertisePenalty();
		return (int)val;
	}

	public final int getPAtkSpd()
	{
		double val = calcStat(Stats.POWER_ATTACK_SPEED, _template.basePAtkSpd, null, null);
		val /= getArmourExpertisePenalty();
		return (int)val;
	}
	
	public final double getMReuseRate(L2Skill skill)
	{
		return calcStat(Stats.MAGIC_REUSE_RATE, _template.baseMReuseRate, null, skill);
	}

	public final int getShldDef()
	{
		return (int)calcStat(Stats.SHIELD_DEFENCE, 0, null, null);
	}
	
	/**
	 * @return Returns the runSpeed.
	 */
	public final int getRunSpeed()
	{
		// err we should be adding TO the persons run speed
		// not making it a constant
		int val = (int)calcStat(Stats.RUN_SPEED, _template.baseRunSpd, null, null);
		if (isFlying())
		{	
			val += Config.WYVERN_SPEED;
			return val;
		}
		else
		{
			if (isRidding())
			{
				val += Config.STRIDER_SPEED;
				return val;
			}
			else
			{
				val /= getArmourExpertisePenalty();
				return val;
			}
		}	
	}
	
	public final int getWalkSpeed()
	{
		return (getRunSpeed() * 70) / 100;
	}
	
	public final float getMoveSpeed()
	{
		if (isRunning())
			return getRunSpeed();
		else
			return getWalkSpeed();
    }
    
	public final int getPhysicalAttackRange()
	{
		return (int)calcStat(Stats.POWER_ATTACK_RANGE, getTemplate().baseAtkRange, null, null);
	}
    
	public final int getMagicalAttackRange(L2Skill skill)
	{
		if (skill != null)
			return (int)calcStat(Stats.MAGIC_ATTACK_RANGE, skill.getCastRange(), null, skill);
		else
			return getTemplate().baseAtkRange;
	}
	
	//New AttackSpeedMultiplier Function to get proper animations.
	public final float getAttackSpeedMultiplier()
	{
		return (float) ((1.1)*getPAtkSpd()/getTemplate().basePAtkSpd);
    }
    
	
	public abstract int getLevel();
	
	/**
	 * @return
	 */
	public final String getName()
	{
		return _name;
	}
	
	/**
	 * @param name
	 */
	public final void setName(String name)
	{
		_name = name;
	}
	
	/**
	 * if the target is canceld, also the attack is canceld
	 * @param i
	 */
	public void setTarget(L2Object object)
	{
		if (object != null && !object.isVisible())
			object = null;
		if (object != null && object != _target) {
			this.addKnownObject(object, null);
			object.addKnownObject(this, null);
		}
		if (object == null) {
			if (isAttackingNow()) {
				abortAttack();
				getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
				if (this instanceof L2PcInstance) {
					sendPacket(new ActionFailed());
					SystemMessage sm = new SystemMessage(614);
					sm.addString("Attack is aborted");
					sendPacket(sm);
				}
			}
			if (isCastingNow() && canAbortCast()) {
				abortCast();
				getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
				if (this instanceof L2PcInstance) {
					sendPacket(new ActionFailed());
					SystemMessage sm = new SystemMessage(614);
					sm.addString("Casting is aborted");
					sendPacket(sm);
				}
			}
		}
		_target = object;
	}
	
	/**
	 * @return
	 */
	public final int getTargetId()
	{
		if (_target != null)
		{
			return _target.getObjectId();
		}
		return -1;
	}
	
	public final L2Object getTarget()
	{
		return _target;
	}
	
	/**
	 * @return
	 */
	public final String getTitle()
	{
		return _title;
	}
	
	/**
	 * @param clanName
	 */
	public final void setTitle(String title)
	{
		this._title = title;
	}
	
	public final boolean isRunning()
	{
		return _running;
	}

	public final boolean isFlying()
	{
		return _flying;
	}
	 	
	public final boolean isRidding()
	{
		return _ridding;		
	}
	
	public final void setRunning()
	{
		if (!_running) {
			_running = true;
			broadcastPacket(new ChangeMoveType(this));
		}
	}

	public final void setFlying(boolean mode)
	{
		_flying = mode;		
	}
	 	
	public final void setRidding(boolean mode)
	{
		_ridding = mode;		
	}
	
	public final void setWalking()
	{
		if (_running) {
			_running = false;
			broadcastPacket(new ChangeMoveType(this));
		}
	}
	
	/**
	 * @param i
	 */
	public void reduceCurrentMp(double i)
	{
		i = getCurrentMp() - i;
		if (i < 0)
			i = 0;
		setCurrentMp(i);
	}
	
	/**
	 * @param i
	 */
	public void reduceCurrentHp(double i, L2Character attacker)
	{
		reduceCurrentHp(i, attacker, true);
	}
	
	/**
	 * @param i
	 */
	public void reduceCurrentHp(double i, L2Character attacker, boolean awake)
	{
		if (isDead())
			return;
		if (awake)
			if (_sleeping)
				stopSleeping(null);
		if (_stuned && _rnd.nextInt(10) == 0)
			stopStunning(null);
		i = getCurrentHp() - i;
		if (i < 0)
			i = 0;
		setCurrentHp(i);
		if (isDead())
		{
			// killing is only possible one time
			synchronized (this)
			{
				if (_killedAlready)
					return;
				_killedAlready = true;
			}
			// first die (and calculate revards), if currentHp < 0,
			// then overhit may be calculated
			if (Config.DEBUG) _log.fine("char is dead.");
			stopMove(null);
			doDie(attacker);
			// now reset currentHp to zero
			_currentHp = 0;
		}
	}
		
	protected void doDie(L2Character killer) {
		setTarget(null);
		stopMove(null);
		stopHpMpRegeneration();
		stopAllEffects();
		
		broadcastStatusUpdate();
		getAI().notifyEvent(CtrlEvent.EVT_DEAD, null);
	}
	
	// called from AIAccessor only
	protected void moveToLocation(int x, int y, int z, int offset)
	{
		final int curX = super.getX();
		final int curY = super.getY();
		final int curZ = super.getZ();
		final int dx = (x - curX); 
		final int dy = (y - curY); 
		double distance = Math.sqrt(dx*dx + dy*dy);
		if (Config.DEBUG) _log.fine("distance to target:" + distance);
		
		double cos;
		double sin;
		
		if (offset > 0 || distance < 1)
		{
			if (distance < 1 || distance - offset  <= 0)
			{
				sin = 0;
				cos = 1;
				distance = 0;
				x = curX;
				y = curY;
				if (Config.DEBUG) _log.fine("already in range, no movement needed.");
				getAI().notifyEvent(CtrlEvent.EVT_ARRIVED, null);
				return;
			}
			else
			{
				sin = dy/distance;
				cos = dx/distance;
				distance -= (offset-5); // due to rounding error, we have to move a bit closer to be in range 
				x = curX + (int)(distance * cos);
				y = curY + (int)(distance * sin);
			}
		} else {
			sin = dy/distance;
			cos = dx/distance;
		}
		
		float speed = getMoveSpeed();
		
		MoveData m = new MoveData();
		m._ticksToMove = (int)(GameTimeController.TICKS_PER_SECOND * distance / speed);
		m._xSpeedTicks = (float)(cos * speed / GameTimeController.TICKS_PER_SECOND);
		m._ySpeedTicks = (float)(sin * speed / GameTimeController.TICKS_PER_SECOND);
		int heading = (int) (Math.atan2(-sin, -cos) * 10430.378350470452724949566316381);
		heading += 32768;
		setHeading(heading);
		if (Config.DEBUG)
			_log.fine("dist:"+ distance +"speed:" + speed + " ttt:" +m._ticksToMove +
					" dx:"+(int)m._xSpeedTicks + " dy:"+(int)m._ySpeedTicks + " heading:" + heading);
		m._xDestination = x;
		m._yDestination = y;
		m._zDestination = z; // this is what was requested from client
		
		m._moveStartTime = GameTimeController.getGameTicks();
		m._xMoveFrom = curX;
		m._yMoveFrom = curY;
		m._zMoveFrom = curZ;
		if (m._ticksToMove < 1 )
			m._ticksToMove = 1; // ensure we can "move" to small distancies
		
		if (Config.DEBUG) _log.fine("time to target:" + m._ticksToMove);
		_move = m;
		GameTimeController.getInstance().registerMovingObject(this);
		int tm = m._ticksToMove*GameTimeController.MILLIS_IN_TICK;
		if (tm > 3000)
			ClientScheduler.getInstance().scheduleMed( new NotifyAITask(CtrlEvent.EVT_ARRIVED_REVALIDATE), 2000);
		// the CtrlEvent.EVT_ARRIVED will be sent when the character will actually arrive
		// to destination by GameTimeController
	}
	
	public boolean addKnownObject(L2Object object, L2Character dropper)
	{
		if (object == null || !this.isVisible())
			return false;
		if (knowsObject(object)) {
			if (!object.isVisible())
				removeKnownObject(object);
			return false;
		}
		double farAway =  getDistanceToWatchObject(object);
		if (getDistance(object.getX(), object.getY()) >= farAway)
			return false;
		_knownObjects.put(object);
		if (object instanceof L2PcInstance)
			_knownPlayer.put((L2PcInstance)object);
		return true;
	}

	public void removeKnownObject(L2Object object)
	{
		_knownObjects.remove(object);
		if (object instanceof L2PcInstance)
			_knownPlayer.remove((L2PcInstance)object);
		if (object == getTarget())
			setTarget(null);
	}

	public final boolean knowsObject(L2Object object)
	{
		return this == object || _knownObjects.contains(object);
	}
	
	public final void removeAllKnownObjects()
	{
		_knownObjects.clear();
		_knownPlayer.clear();
		setTarget(null);
		if (this.hasAI())
			_ai = null;
		}

	/** please, read-only! */
	public final L2ObjectHashSet<L2PcInstance> getKnownPlayers()
	{
		return _knownPlayer;
	}
	
	/** please, read-only! */
	public final L2ObjectHashSet<L2Object> getKnownObjects()
	{
		return _knownObjects;
	}
	
	public final boolean knowsThePlayer(L2PcInstance player)
	{
		return this == player || _knownPlayer.contains(player);
	}
	
	protected abstract int getDistanceToWatchObject(L2Object object);
	protected abstract int getDistanceToForgetObject(L2Object object);
	
	public final synchronized void updateKnownObjects()
	{
		forgetDistantObjects();
		findCloseObjects();
	}
	
	private void forgetDistantObjects()
	{
		Iterator<L2Object> iter = _knownObjects.iterator();
		while (iter.hasNext())
		{
			L2Object object = iter.next();
			if (!object.isVisible()) {
				removeKnownObject(object);
				continue;
			}
			double dx = getX() - object.getX();
			double dy = getY() - object.getY();
			double farAway = getDistanceToForgetObject(object);
			if ( dx*dx+dy*dy > farAway*farAway ) {
				removeKnownObject(object);
				continue;
			}
		}
	}

	private void findCloseObjects()
	{
		if (isDead() || !isVisible())
			return;
		L2Object[] objects = L2World.getInstance().getVisibleObjects(this);
		for (L2Object object : objects)
		{
			if (_knownObjects.contains(object)) {
				if (!(object instanceof L2Character))
				continue;
				if (!object.knowsObject(this)) {
					L2Character tgt = (L2Character)object;
						tgt.addKnownObject(this, null);
				}
			} else {
				addKnownObject(object, null);
			}
		}
	}

	/**
	 * @param x   target x
	 * @param y   target y
	 * @return
	 */
	public final double getDistance(int x, int y) 
	{
		long dx = x-getX();
		long dy = y-getY();
		double distance = Math.sqrt(dx*dx + dy*dy);
		return distance;
	}
	
	public final void broadcastPacketToOthers(ServerBasePacket mov)
	{
		if (Config.DEBUG) _log.fine("players to notify:" + getKnownPlayers().size() + " packet:"+mov.getType());
		for (L2PcInstance player : getKnownPlayers())
		{
			if (player != this)
				player.sendPacket(mov);
		}
	}
	
	public final void broadcastPacket(ServerBasePacket mov)
	{
		if (Config.DEBUG) _log.fine("players to notify:" + getKnownPlayers().size() + " packet:"+mov.getType());
		this.sendPacket(mov);
		for (L2PcInstance player : getKnownPlayers())
		{
			if (player != this)
				player.sendPacket(mov);
		}
	}
	
	public void sendPacket(ServerBasePacket mov)
	{
		// default implementation
	}
	
//	/**
//	 * event that is called when the destination coordinates are reached
//	 */
//	public void onTargetReached()
//	{
//		L2Character pawn = getPawnTarget(); 
//		
//		if (pawn != null)
//		{
//			int x = pawn.getX(), y=pawn.getY(),z = pawn.getZ();
//			
//			double distance = getDistance(x,y);
//			if (getCurrentState() == STATE_FOLLOW)
//			{
//				calculateMovement(x,y,z,distance);
//				return;
//			}
//			
//			//			takes care of moving away but distance is 0 so i won't follow problem
//			
//			
//			if (((distance > getAttackRange()) && (getCurrentState() == STATE_ATTACKING)) || (pawn.isMoving() && getCurrentState() != STATE_ATTACKING))
//			{
//				calculateMovement(x,y,z,distance);
//				return;
//			}
//			
//		}
//		//		 update x,y,z with the current calculated position 
//		stopMove();
//		
//		if (Config.DEBUG)
//			_log.fine(this.getName() +":: target reached at: x "+getX()+" y "+getY()+ " z:" + getZ());
//		
//		if (getPawnTarget() != null)
//		{
//			
//			setPawnTarget(null); 
//			setMovingToPawn(false);
//		}
//	}
//	
//	public void setTo(int x, int y, int z, int heading)
//	{
//		setX(x);
//		setY(y);
//		setZ(z);
//		setHeading(heading);
//		updateCurrentWorldRegion(); //TODO: maybe not needed here
//		if (isMoving())
//		{	
//			setCurrentState(STATE_IDLE);
//			StopMove setto = new StopMove(this);
//			broadcastPacket(setto);
//		}
//		else
//		{
//			ValidateLocation setto = new ValidateLocation(this);
//			broadcastPacket(setto);
//		}	
//		
//		FinishRotation fr = new FinishRotation(this);
//		broadcastPacket(fr);
//	}
	
	/**
	 * @return if the character really dead
	 */
	public final boolean isDead()
	{
		return !(_fake_death) && !(_currentHp > 0.5); // ! for NaN
	}

	/**
	 * @return if the character really dead or uses fake death
	 */
	public final boolean isAlikeDead()
	{
		return _fake_death || !(_currentHp > 0.5); // ! for NaN
	}

	/**
	 * @return Returns the rnd.
	 */
	public final static Random getRnd()
	{
		return _rnd;
	}
	
	
//	protected void startCombat()
//	{
//		if (_currentAttackTask == null )//&& !isInCombat())
//		{
//			_currentAttackTask = ClientScheduler.getInstance().scheduleMed(new AttackTask(), 0);
//		}
//		else
//		{
//			_log.info("multiple attacks want to start in parallel. prevented.");
//		}
//	}
//    
    public float getWeaponExpertisePenalty()
    {
        return 1.f;
    }
    
    public float getArmourExpertisePenalty()
    {
        return 1.f;
    }
	
	// should be private, but public for fight_calculator
	public void setAttackingBodypart() {
		_attacking = Inventory.PAPERDOLL_CHEST;
	}
	
    protected void doAttack(L2Character target)
	{
		if (Config.DEBUG)
			_log.fine("doAttack: target="+target);
    	if (target == null)
    		return;

		if (isAttackingDisabled())
			return;
		
		L2ItemInstance weaponInst = getActiveWeaponInstance();
		L2Weapon weaponItem = getActiveWeaponItem(); 
		if ((weaponItem!=null && weaponItem.getItemType() == L2WeaponType.BOW))
		{
			//check for arrows and mana
			if(this instanceof L2PcInstance)
			{
				if (_disableBowAttackEndTime <= GameTimeController.getGameTicks())
				{
					if(!(this instanceof L2NpcInstance))
					//mobs don't check for mp with bows.
					{
						if (getCurrentMp() < weaponItem.getMpConsume())
						{
							ClientScheduler.getInstance().scheduleMed(
									new NotifyAITask(CtrlEvent.EVT_READY_TO_ACT), 1000);
							// player doesn't have enough mp, stop the attack
							sendPacket(new SystemMessage(SystemMessage.NOT_ENOUGH_MP));
							ActionFailed af = new ActionFailed();
							sendPacket(af);
							return;
						}
						else
						{
							reduceCurrentMp(weaponItem.getMpConsume());
						}
					}
					_disableBowAttackEndTime = 5*GameTimeController.TICKS_PER_SECOND + GameTimeController.getGameTicks();
				}
				else
				{
					ClientScheduler.getInstance().scheduleMed(
							new NotifyAITask(CtrlEvent.EVT_READY_TO_ACT), 1000);
					// trying to shoot before time, cancel the attack - temp fix!
					ActionFailed af = new ActionFailed();
					sendPacket(af);
					return;
				}
				if (!checkAndEquipArrows())
				{
					// char has no arrows, stop the attack
					getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
					ActionFailed af = new ActionFailed();
					sendPacket(af);
					sendPacket(new SystemMessage(SystemMessage.NOT_ENOUGH_ARROWS));
					return;
				}
			}
			else if (this instanceof L2NpcInstance)
			{
				if (_disableBowAttackEndTime > GameTimeController.getGameTicks())
					return;
			}
		}

		if (isAlikeDead() || target == null || target.isAlikeDead() || !knowsObject(target))
		{
			// if we are dead or the target is dead, we stop the attack 
			getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
			ActionFailed af = new ActionFailed();
			sendPacket(af);
			return;
		}
		
		target.addKnownObject(this, null);

		if (Config.ALT_GAME_TIREDNESS)
			setCurrentCp(getCurrentCp()-10);

		boolean wasSSCharged = (weaponInst != null &&
				weaponInst.getChargedSoulshot() == L2ItemInstance.CHARGED_SOULSHOT);


		int sAtk = calculateAttackSpeed(target, weaponInst);
		_attackEndTime = GameTimeController.getGameTicks();
		_attackEndTime += (sAtk / GameTimeController.MILLIS_IN_TICK);
		_attackEndTime -= 1;
			
		Attack attack = new Attack(this, wasSSCharged);
		boolean hitted;
		
		setAttackingBodypart();
		
		if (weaponItem == null)
			hitted = doAttakHitSimple(attack, target);
		else if (weaponItem.getItemType() == L2WeaponType.BOW)
			hitted = doAttakHitByBow(attack, target, sAtk);
		else if (weaponItem.getItemType() == L2WeaponType.POLE)
			hitted = doAttakHitByPole(attack);
		else if (isUsingDualWeapon())
			hitted = doAttakHitByDual(attack, target);
		else
			hitted = doAttakHitSimple(attack, target);
		
		if (hitted) {
			if (wasSSCharged) 
				weaponInst.setChargedSoulshot(L2ItemInstance.CHARGED_NONE);
		} else {
			abortAttack();
		}
		
		if (attack.hasHits())
			broadcastPacket(attack);
			
		int reuse = weaponItem==null ? 0 : weaponItem.getAttackReuseDelay();
		ClientScheduler.getInstance().scheduleMed(
				new NotifyAITask(CtrlEvent.EVT_READY_TO_ACT), sAtk+reuse);
	}
	
	
	private boolean doAttakHitSimple(Attack attack, L2Character target) {
		int damage1 = 0;
		boolean miss1 = Formulas.getInstance().calcHitMiss(this, target);
		boolean shld1 = false;
		boolean crit1 = false;

		if (!miss1) {
			shld1 = Formulas.getInstance().calcShldUse(this, target);
			crit1 = Formulas.getInstance().calcCrit(getCriticalHit(target, null));
			damage1 = (int)Formulas.getInstance().calcPhysDam(
					this, target, null, shld1, crit1, false, attack._soulshot);
		}

		ClientScheduler.getInstance().scheduleMed(
				new HitTask(target, damage1, crit1, miss1, attack._soulshot, shld1), 980);
		
		attack.addHit(target, damage1, miss1, crit1, shld1);
		return !miss1;
	}
    
	private boolean doAttakHitByBow(Attack attack, L2Character target, int sAtk) {
		int damage1 = 0;
		boolean miss1 = Formulas.getInstance().calcHitMiss(this, target);
		boolean shld1 = false;
		boolean crit1 = false;

		reduceArrowCount();
		
		if (!miss1) {
			shld1 = Formulas.getInstance().calcShldUse(this, target);
			crit1 = Formulas.getInstance().calcCrit(getCriticalHit(target, null));
			damage1 = (int)Formulas.getInstance().calcPhysDam(
					this, target, null, shld1, crit1, false, attack._soulshot);
		}

		int reuse;
		L2Weapon bow = getActiveWeaponItem();
		if (bow == null)
			reuse = 1500;
		else
			reuse = bow.getAttackReuseDelay();

		if (this instanceof L2PcInstance) {
			sendPacket(new SystemMessage(SystemMessage.GETTING_READY_TO_SHOOT_AN_ARROW));
			SetupGauge sg = new SetupGauge(SetupGauge.RED, sAtk+reuse);
			sendPacket(sg);
		}
		
		ClientScheduler.getInstance().scheduleMed(
				new HitTask(target, damage1, crit1, miss1, attack._soulshot, shld1), sAtk);

		_disableBowAttackEndTime = (sAtk+reuse)/GameTimeController.MILLIS_IN_TICK + GameTimeController.getGameTicks();
		
		attack.addHit(target, damage1, miss1, crit1, shld1);
	
		return !miss1;
	}
    
	private boolean doAttakHitByDual(Attack attack, L2Character target) {
		int damage1 = 0;
		int damage2 = 0;
		boolean miss1 = Formulas.getInstance().calcHitMiss(this, target);
		boolean miss2 = Formulas.getInstance().calcHitMiss(this, target);
		boolean shld1 = false;
		boolean shld2 = false;
		boolean crit1 = false;
		boolean crit2 = false;


		if (!miss1) {
			shld1 = Formulas.getInstance().calcShldUse(this, target);
			crit1 = Formulas.getInstance().calcCrit(getCriticalHit(target, null));
			damage1 = (int)Formulas.getInstance().calcPhysDam(
					this, target, null, shld1, crit1, true, attack._soulshot);
		}
		if (!miss2)
		{
			shld2 = Formulas.getInstance().calcShldUse(this, target);
			crit2 = Formulas.getInstance().calcCrit(getCriticalHit(target, null));
			damage2 = (int)Formulas.getInstance().calcPhysDam(
					this, target, null, shld2, crit2, true, attack._soulshot);
		}

		ClientScheduler.getInstance().scheduleMed(
				new HitTask(target, damage1, crit1, miss1, attack._soulshot, shld1), 650);
		ClientScheduler.getInstance().scheduleMed(
				new HitTask(target, damage2, crit2, miss2, attack._soulshot, shld2), 1250);

		attack.addHit(target, damage1, miss1, crit1, shld1);
		attack.addHit(target, damage2, miss2, crit2, shld2);

		return !miss1 || !miss2;
	}
    
	private boolean doAttakHitByPole(Attack attack) {
		boolean hitted = false;
		L2Object[] possibleTargets = L2World.getInstance().getVisibleObjects3D(this, 80);
		for(int i = 0;i < possibleTargets.length;i++) {
			if(possibleTargets[i] instanceof L2Character) {
				L2Character target = (L2Character) possibleTargets[i];
				if(!target.isAlikeDead())
				{
					if (target == getAI().getAttackTarget())
						hitted |= doAttakHitSimple(attack, target);
				}
			}
		}
		for(int i = 0;i < possibleTargets.length;i++) {
			if(possibleTargets[i] instanceof L2Character) {
				L2Character target = (L2Character) possibleTargets[i];
				if(!target.isAlikeDead() && target.isAutoAttackable(this))
				{
					if (target == getAI().getAttackTarget())
						continue;
					hitted |= doAttakHitSimple(attack, target);
					target.getAI().notifyEvent(CtrlEvent.EVT_ATTACKED, this);
				}
			}
		}
		return hitted;
	}
    
	/**
	 * @return true if arrows are available
	 */
	protected boolean checkAndEquipArrows()
	{
		return true;
	}
	
	public void addExpAndSp(int addToExp, int addToSp)
	{
		// Dummy method (overridden by players and pets)
	}
	
	/**
	 * @return
	 */
	public abstract L2ItemInstance getActiveWeaponInstance();
	public abstract L2Weapon getActiveWeaponItem();
	public abstract L2ItemInstance getSecondaryWeaponInstance();
	public abstract L2Weapon getSecondaryWeaponItem();
	
	protected void onHitTimer(L2Character target, int damage, boolean crit, boolean miss, boolean soulshot, boolean shld)
	{
		if (isAlikeDead()) {
			getAI().notifyEvent(CtrlEvent.EVT_CANCEL);
			return;
		}
		
		if (target.isAlikeDead() || !knowsObject(target))
		{
			//getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE, null);
			getAI().notifyEvent(CtrlEvent.EVT_CANCEL);
			ActionFailed af = new ActionFailed();
			sendPacket(af);
			return;
		}

		if (!isAttackAborted())
		{
			target.getAI().notifyEvent(CtrlEvent.EVT_ATTACKED, this);
			if (this instanceof L2PcInstance) {
				if (crit)
					sendPacket(new SystemMessage(SystemMessage.CRITICAL_HIT));
				if (miss) {
					sendPacket(new SystemMessage(SystemMessage.MISSED_TARGET));
				}
				else if (damage < 1) {
					SystemMessage sm = new SystemMessage(614);
					sm.addString("Enemy's armour hit");
					sendPacket(sm);
				} else {
					SystemMessage sm = new SystemMessage(SystemMessage.YOU_DID_S1_DMG);
					sm.addNumber(damage);
					sendPacket(sm);
				}
			}
			if (target instanceof L2PcInstance) {
				L2PcInstance enemy = (L2PcInstance)target; 
				if (shld) {
					enemy.sendPacket(new SystemMessage(SystemMessage.SHIELD_DEFENCE_SUCCESSFULL));
				}
				else if (!miss && damage < 1) {
					SystemMessage smsg = new SystemMessage(SystemMessage.S1_S2);
					smsg.addString("Armour hit");
					enemy.sendPacket(smsg);
				}
			}

			if (!miss && damage > 0) {
				double reflectPercent = target.calcStat(Stats.REFLECT_DAMAGE_PERCENT,0,null,null);
				if (reflectPercent>0)
				{
					int reflectedDamage = (int)(reflectPercent/100.*damage);
					damage -= reflectedDamage;
					this.reduceCurrentHp(reflectedDamage,target);
				}
				target.reduceCurrentHp(damage, this);
			}
			
			if (!miss && damage > 0) {
				double cancel = target.getAtkCancel();
				boolean abort = Formulas.getInstance().calcAtkBreak(target, cancel);
				if (abort) {
					if (target.isAttackingNow()) {
					target.abortAttack();
						if (target instanceof L2PcInstance) {
							target.sendPacket(new ActionFailed());
							SystemMessage sm = new SystemMessage(614);
							sm.addString("Attack is breaked");
							target.sendPacket(sm);
						}
					}
					if (target.isCastingNow() && target.canAbortCast()) {
					target.abortCast();
						if (target instanceof L2PcInstance) {
							target.sendPacket(new ActionFailed());
							SystemMessage sm = new SystemMessage(614);
							sm.addString("Casting is breaked");
							target.sendPacket(sm);
						}
					}
				}
			}
		}
	}
	
	
	/**
	 * 
	 */
	protected void reduceArrowCount()
	{
		// default is to do nothin
	}
	
	public void onForcedAttack(L2PcInstance player)
	{
		//_log.config("Checking zones on forced attack");
		if (withinZones(L2World.zones) ||
            player.withinZones(L2World.zones))
		{
			player.sendPacket(new SystemMessage(SystemMessage.TARGET_IN_PEACEZONE));
			ActionFailed af = new ActionFailed();
			player.sendPacket(af);
		}
		else if (player.isConfused())
		{
			ActionFailed af = new ActionFailed();
			player.sendPacket(af);
		}
        else
        {
            //_log.config("Not within a zone");
            //player.startAttack(this);
        	player.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, this);
        }
    }
	
	
	public int calculateAttackSpeed(L2Character target, L2ItemInstance weaponInst)
	{
		return Formulas.getInstance().calcPAtkSpd(this, target, getPAtkSpd());
	}
	
	public boolean isUsingDualWeapon()
	{
		return false;
	}
	
	/**
	 * 
	 * @param newSkill
	 * @return L2Skill old skill that was upgraded or NULL if there was no lower skill of same type
	 */
	public L2Skill addSkill(L2Skill newSkill)
	{
		L2Skill oldSkill = _skills.put(newSkill.getId(), newSkill);
		if (oldSkill != null)
			removeStatsOwner(oldSkill);
		addStatFuncs(newSkill.getStatFuncs(null, this));
        return oldSkill;
	}
	

	/**
	 * 
	 * @param skill
	 * @return L2Skill old skill that was removed or NULL if the skill did not exist
	 */
	public L2Skill removeSkill(L2Skill skill)
	{
		L2Skill oldSkill = _skills.remove(skill.getId());
		if (oldSkill != null)
			removeStatsOwner(oldSkill);
		return oldSkill;
	}
	
	/**
	 * @return L2Skill[]  all skills that this char knows
	 */
	public final L2Skill[] getAllSkills()
	{
		if (_skills == null)
			return new L2Skill[0];
		return _skills.values().toArray(new L2Skill[_skills.values().size()]);
	}
	
	/**
	 * Determine the level of a certain skill.
	 * this is used for shortcut reg
	 * @param skillId
	 * @return
	 */
	public final int getSkillLevel(int skillId)
	{
		if (_skills == null)
			return -1;
		L2Skill skill = _skills.get(skillId);
		if (skill == null)
			return -1;
		else
			return skill.getLevel();
	}
	
	public final L2Skill getKnownSkill(int skillId)
	{
		if (_skills == null)
			return null;
		return _skills.get(skillId);
	}
	
    /**
     * Force attack and movement are checked by L2PcCharacter.useMagic(),
     * and this method may be called if we allowed to attack or for monsters.
     * @param skill skill to use
     */
	protected void useMagic(L2Skill skill)
	{
		if (skill == null || isDead())
		    return;

		if (isAllSkillsDisabled())
		{
			// must be checked by caller
			_log.warning("currently casting or sleeping, skill disabled");
			return;
		}
		
		if (skill.isPassive())
		{
			// just ignore the passive skill request. why does the client send it anyway ??
			return;
		}
		
		L2Object target = null;
		switch (skill.getTargetType())
		{
		case TARGET_AURA:
		case TARGET_AURA_CLOSE:
		case TARGET_PARTY:
		case TARGET_CLAN:
			target = this;
			break;
		default:
			L2Object[] targets = skill.getTargetList(this);
			if(targets == null || targets.length == 0)
				return;
			
			target = targets[0];
			break;
		}
		
//		ISkillHandler handler = SkillHandler.getInstance().getSkillHandler(skill.getSkillType());
//		if(handler == null)
//		{
//			SystemMessage sm = new SystemMessage(614);
//			sm.addString("Skill not implemented.  Skill ID: " + skill.getId() + " " + skill.getSkillType());
//			sendPacket(sm);
//			return;
//		}
//		
		getAI().setIntention(CtrlIntention.AI_INTENTION_CAST, skill, target);
	}
	
    protected void doCast(L2Skill skill)
	{
		if (skill == null)
		{
			getAI().notifyEvent(CtrlEvent.EVT_CANCEL);
			return;
		}

		if (skill.isMagic() && isMuted()){
			getAI().notifyEvent(CtrlEvent.EVT_CANCEL);
			return;
		}
		
		L2Object[] targets = skill.getTargetList(this);
		if(targets == null)
		{
			getAI().notifyEvent(CtrlEvent.EVT_CANCEL);
			return;
		}
		
		L2Character target = null;
		if(		skill.getSkillType() == SkillType.BUFF ||
				skill.getSkillType() == SkillType.HEAL ||
				skill.getSkillType() == SkillType.MANAHEAL ||
				skill.getSkillType() == SkillType.REFLECT ||
				skill.getTargetType() == L2Skill.SkillTargetType.TARGET_SELF)
			target = (L2Character) targets[0];
		else
			target = (L2Character) getTarget();

		if (skill.getTargetType() == SkillTargetType.TARGET_AURA ||
				skill.getTargetType() == SkillTargetType.TARGET_AURA_CLOSE ||
				skill.getTargetType() == SkillTargetType.TARGET_PARTY ||
				skill.getTargetType() == SkillTargetType.TARGET_CLAN)
			target = this;
		
		if(target == null)
		{
			getAI().notifyEvent(CtrlEvent.EVT_CANCEL);
			return;
		}
		
		int magicId = skill.getId();
		int displayId = skill.getDisplayId();
		int level = getSkillLevel(magicId);
        
		// initial skill time
		int skillTime = skill.getSkillTime();
		int skillInterruptTime = skill.getSkillInterruptTime();
		skillTime = Formulas.getInstance().calcMAtkSpd(this, target, skill, skillTime);
		if (skill.isMagic()) {
			skillInterruptTime = Formulas.getInstance().calcMAtkSpd(this, target, skill, skillInterruptTime);
		} else {
			skillInterruptTime = 0;
		}

		_castEndTime = 10 + GameTimeController.getGameTicks() + skillTime / GameTimeController.MILLIS_IN_TICK;
		_castInterruptTime = GameTimeController.getGameTicks() + skillInterruptTime / GameTimeController.MILLIS_IN_TICK;
		
		// initial reuse time
		int reuseDelay = (int)(skill.getReuseDelay() * getMReuseRate(skill));

		//start casting
		MagicSkillUser msu = new MagicSkillUser(this, target, displayId, level, skillTime, reuseDelay);
		broadcastPacket(msu);
		
		//system message
		if (this instanceof L2PcInstance) {
			SystemMessage sm = new SystemMessage(SystemMessage.USE_S1);
			sm.addSkillName(magicId);
			sendPacket(sm);
		}
		
		if (reuseDelay > 10)
		{
			disableSkill(skill.getId());
			ClientScheduler.getInstance().scheduleMed(new EnableSkill(skill.getId()), reuseDelay);
		}

		// launch the magic in skillTime milliseconds
		if (skillTime > 50) {
			//gauge bar
			if (this instanceof L2PcInstance) {
				SetupGauge sg = new SetupGauge(SetupGauge.BLUE, skillTime);
				sendPacket(sg);
			}
			
			disableAllSkills();
			ClientScheduler.getInstance().scheduleMed(new EnableAllSkills(skill), skillTime);
			ClientScheduler.getInstance().scheduleMed(new MagicUseTask(targets, skill), skillTime);
		}
		else
		{
			onMagicUseTimer(targets, skill);
		}
	}
	
	public void onMagicUseTimer(L2Object[] targets, L2Skill skill)
	{
		if (isAlikeDead()) {
			_castEndTime = 0;
			_castInterruptTime = 0;
			return;
		}
			
		if (isCastingNow())
		{
			_castEndTime = 0;
			_castInterruptTime = 0;
			int magicId = skill.getDisplayId();
			int level = getSkillLevel(skill.getId());
			
			if(		skill.getTargetType() == L2Skill.SkillTargetType.TARGET_AREA ||
					skill.getTargetType() == L2Skill.SkillTargetType.TARGET_AREA_CORPSE_MOB)
			{
				L2Character target = (L2Character)targets[0];
				MagicSkillLaunched msl = new MagicSkillLaunched(this, magicId, level, target);
				if (Config.DEBUG) _log.fine("msl: "+getName()+" "+magicId+" "+level+" "+target.getName());
				//sendPacket(msl);
				broadcastPacket(msl);
			}
			else
			{
				for(int i = 0;i < targets.length;i++)
				{
                    if (targets[i] instanceof L2Character)
                    {
                        L2Character target = (L2Character) targets[i];
                        
                        if (Config.DEBUG) _log.fine("msl: "+getName()+" "+magicId+" "+level+" "+target.getTitle());
    					MagicSkillLaunched msl = new MagicSkillLaunched(this, magicId, level, target);
    					//sendPacket(msl);
    					broadcastPacket(msl);
    				}
    				else
    				{
    					if (Config.DEBUG) _log.config("Class cast bad: "+targets.getClass().toString());
    				}
                }
			}
            
			if (skill.getMpConsume() > 0)
            {
			    reduceCurrentMp(skill.getMpConsume());
            }
            
            if (skill.getHpConsume() > 0)
            {
                reduceCurrentHp(skill.getHpConsume(), null);
            }
            
            if (skill.getItemConsume() > 0)
            {
                consumeItem(skill.getItemConsumeId(), skill.getItemConsume());
            }
            
			StatusUpdate su = new StatusUpdate(getObjectId());
			su.addAttribute(StatusUpdate.CUR_MP, (int) getCurrentMp());
			sendPacket(su);
			
			callSkill(skill, targets);
			
			//if the skill has changed the character's state to something other than STATE_CASTING
			//then just leave it that way, otherwise switch back to STATE_IDLE.
			//if(isCastingNow())
			//	getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE, null);
			
			if(skill.getSkillType() == SkillType.PDAM)
			{
			    if((getTarget() != null) && (getTarget() instanceof L2Character))
			    {
					getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, getTarget());
//			        startAttack((L2Character)getTarget());
			    }
			}
		}
	}
	
	/**
     * @param itemConsumeId
     * @param itemCount
     */
    public void consumeItem(int itemConsumeId, int itemCount)
    {
    }

    public synchronized void enableSkill(int skillId)
	{
    	if (_disabledSkills == null)
    		return;
    	boolean hasDisabled = false;
    	for (int i=0; i < _disabledSkills.length; i++)
    	{
    		if (_disabledSkills[i] == skillId)
    			_disabledSkills[i] = 0;
    		if (_disabledSkills[i] != 0)
    			hasDisabled = true;
    	}
    	if (!hasDisabled)
    		_disabledSkills = null;
	}
    public synchronized void disableSkill(int skillId)
	{
    	if (_disabledSkills == null)
    	{
    		_disabledSkills = new int[]{skillId,0,0,0};
    	}
    	else
    	{
        	for (int i=0; i < _disabledSkills.length; i++)
        	{
        		if (_disabledSkills[i] == 0)
        		{
        			_disabledSkills[i] = skillId;
        			return;
        		}
        	}
        	int[] tmp = new int[2*_disabledSkills.length];
        	System.arraycopy(_disabledSkills, 0, tmp, 0, _disabledSkills.length);
        	_disabledSkills = tmp;
    	}
	}
	
	public boolean isSkillDisabled(int skillId)
	{
		if (isAllSkillsDisabled())
			return true;
		if (_disabledSkills == null)
			return false;
    	for (int i=0; i < _disabledSkills.length; i++)
    	{
    		if (_disabledSkills[i] == skillId)
    			return true;
    	}
		return false;
	}
	
	public void disableAllSkills()
	{
		if (Config.DEBUG) _log.fine("all skills disabled");
		_allSkillsDisabled = true;
	}
	
	public void enableAllSkills()
	{
		if (Config.DEBUG) _log.fine("all skills enabled");
		_allSkillsDisabled = false;
	}
	
	private void callSkill(L2Skill skill, L2Object[] targets)
	{
		try{
			ISkillHandler handler = null;
			if(skill.isToggle())
			{
				if(getEffect(skill.getId()) != null)
				{
					handler = SkillHandler.getInstance().getSkillHandler(skill.getSkillType());
					if (handler != null)
						handler.useSkill(this, skill, targets);
					else
						skill.useSkill(this, targets);
					return;
				}
			}
			
			handler = SkillHandler.getInstance().getSkillHandler(skill.getSkillType());
			if (handler != null)
				handler.useSkill(this, skill, targets);
			else
				skill.useSkill(this, targets);
		
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "", e);
		} 
	}
	
	public boolean isBehindTarget()
	{
		if (getTarget() instanceof L2Character)
		{
			L2Character target = (L2Character) getTarget();
			int tarheading = target.getHeading();
			int heading = getHeading();
			int count = tarheading;
			for(int i = 0;i < 3500;i++)
			{
				if(count > 65535)
				{
					count = 1;
				}
				if(heading == count)
				{
					return true;
				}
				count++;
			}
			count = tarheading;
			for(int i = 0;i < 3500;i++)
			{
				if(count < 1)
				{
					count = 65535;
				}
				if(heading == count)
				{
					return true;
				}
				count--;
			}
		}
		else
		{
			_log.warning("isBehindTarget's target not an L2 Character.");
		}
		return false;
	}
	
	public final void updateEffectIcons()
	{
		MagicEffectIcons mi = new MagicEffectIcons();
		L2Effect[] effects = getAllEffects();
		for(int i = 0; effects != null && i < effects.length; i++)
		{
			L2Effect effect = effects[i];
			effect.addIcon(mi);
		}
		sendPacket(mi);
	}
	
	public final void addEffect(L2Effect newEffect)
	{
		synchronized (this) {
			if (_effects == null)
				_effects = new ConcurrentHashMap<Integer,L2Effect>();
		}
		L2Effect old = _effects.put(newEffect.getSkill().getId(), newEffect);
		if (old != null)
			removeStatsOwner(old);
		addStatFuncs(newEffect.getStatFuncs());
		updateEffectIcons();
	}

	public final void removeEffect(L2Effect effect)
	{
		removeStatsOwner(effect);
		synchronized (this) {
			if (_effects == null)
				return;
			_effects.remove(effect.getSkill().getId());
			if (_effects.isEmpty())
				_effects = null;
		}
		updateEffectIcons();
	}

	public final L2Effect getEffect(int index)
	{
		ConcurrentHashMap<Integer,L2Effect> effects = _effects;
		if (effects == null)
			return null;
		return effects.get(index);
	}
	
	public final L2Effect getEffect(L2Skill skill)
	{
		ConcurrentHashMap<Integer,L2Effect> effects = _effects;
		if (effects == null)
			return null;
		for (L2Effect e : effects.values())
		{
			if (e.getSkill() == skill)
				return e;
		}
		return null;
	}
	
	public final L2Effect getEffect(L2Effect.EffectType tp)
	{
		ConcurrentHashMap<Integer,L2Effect> effects = _effects;
		if (effects == null)
			return null;
		for (L2Effect e : effects.values())
		{
			if (e.getEffectType() == tp)
				return e;
		}
		return null;
	}
	
	public final L2Effect[] getAllEffects()
	{
		ConcurrentHashMap<Integer,L2Effect> effects = _effects;
		if (effects == null || effects.isEmpty())
			return EMPTY_EFFECTS;
		return effects.values().toArray(new L2Effect[effects.size()]);
	}
	
	public final void stopEffect(int effectId)
	{
		L2Effect effect = getEffect(effectId);
		if(effect != null)
			effect.exit();
	}

	public final void stopEffects(L2Effect.EffectType type)
	{
		L2Effect[] effects = getAllEffects();
		if (effects != null)
		{
			for(L2Effect e : effects)
			{
				if (e.getEffectType() == type)
					e.exit();
			}
		}
	}

	public final void stopAllEffects()
	{
		L2Effect[] effects = getAllEffects();
		if (effects != null)
		{
			for(L2Effect e : effects)
				stopEffect(e.getSkill().getId());
		}
	}
	
	public double getLevelMod()
	{
		return 1;
	}
	
	public void updateStats()
	{
	}
	
    private Future _PvPRegTask;

	private Object _PvPLock = new Object();

	private boolean _PvPRegActive;

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
		((L2PcInstance) this).setPvpFlag(1);

		StatusUpdate su = new StatusUpdate(getObjectId());
		su.addAttribute(StatusUpdate.PVP_FLAG, 1);
		sendPacket(su);
		broadcastPacket(su);

		_PvPRegTask = ClientScheduler.getInstance().scheduleLowAtFixedRate(
				new PvPFlag(), 1000, 1000);
		_PvPRegActive = true;
		//	_log.fine("PvP turned on");
	}

	public void stopPvPFlag() {
		if (_PvPRegTask != null) {
			_PvPRegTask.cancel(false);
		}

		((L2PcInstance) this).setPvpFlag(0);

		StatusUpdate su = new StatusUpdate(getObjectId());
		su.addAttribute(StatusUpdate.PVP_FLAG, 0);
		sendPacket(su);
		broadcastPacket(su);

		_PvPRegTask = null;
		_PvPRegActive = false;
		//	_log.fine("Pvp turned off");
	}

//	public void checkPvPFlag() 
//    {
//		if (Config.DEBUG) _log.fine("Checking PvpFlag");
//		_PvPRegTask = ClientScheduler.getInstance().scheduleLowAtFixedRate(
//				new PvPFlag(), 1000, 5000);
//		_PvPRegActive = true;
//		//	_log.fine("PvP recheck");
//	}
//	

	public void teleToLocation(int x, int y, int z)
    {
		stopMove(null);
        if (Config.DEBUG) _log.fine("Teleporting to: "+x+", "+y+", "+z);
        setXYZ(x, y, z);
        TeleportToLocation teleport = new TeleportToLocation(this,x,y,z);
        sendPacket(teleport);
        
        if(getPet() != null)
            getPet().teleToLocation(getX(), getY(), getZ());
        }
    
    public L2Summon getPet()
    {
        return null;
    }
    
    public final int getRandomDamage(L2Character target)
    {
    	L2Weapon weaponItem = getActiveWeaponItem();
    	if (weaponItem == null)
    		return 5+(int)Math.sqrt(getLevel());
    	return weaponItem.getRandomDamage();
    }

    public String toString()
    {
    	return "mob "+this.getObjectId();
    }
}	
