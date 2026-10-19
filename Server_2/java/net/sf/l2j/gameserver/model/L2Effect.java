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
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.l2j.gameserver.ClientScheduler;
import net.sf.l2j.gameserver.serverpackets.MagicEffectIcons;
import net.sf.l2j.gameserver.skills.EffectTemplate;
import net.sf.l2j.gameserver.skills.Env;
import net.sf.l2j.gameserver.skills.Func;
import net.sf.l2j.gameserver.skills.FuncTemplate;
import net.sf.l2j.gameserver.skills.Lambda;
import net.sf.l2j.gameserver.templates.L2Item;

/**
 * This class ...
 * 
 * @version $Revision: 1.1.2.1.2.12 $ $Date: 2005/04/11 10:06:07 $
 */
public abstract class L2Effect
{
	static final Logger _log = Logger.getLogger(L2Effect.class.getName());

	public static enum EffectState {
		CREATED,
		ACTING,
		FINISHING
	}
    
	public static enum EffectType  {
		BUFF,
		CHARGE,
		DMG_OVER_TIME,
		HEAL_OVER_TIME,
		MANA_DMG_OVER_TIME,
		MANA_HEAL_OVER_TIME,
		RELAXING,
		STUN,
		ROOT,
		SLEEP,
		HATE,
		FAKE_DEATH,
		CONFUSION,
		CONFUSE_MOB_ONLY,
		MUTE,
		FEAR
	}
	
	private static final Func[] _emptyFunctionSet = new Func[0];
	
	//member _effector is the instance of L2Character that cast/used the spell/skill that is
	//causing this effect.  Do not confuse with the instance of L2Character that
	//is being affected by this effect.
	private final L2Character _effector;

	//member _effected is the instance of L2Character that was affected
	//by this effect.  Do not confuse with the instance of L2Character that
	//catsed/used this effect.
	private final L2Character _effected;

	//the skill that was used.
	private final L2Skill _skill;
	
	//or the items that was used.
	private final L2Item _item;
	
	// the value of an update
	private final Lambda _lambda;
	
	// the current state
	private EffectState _state;
	
	// period, seconds
	private final int _period;

	// function templates
	private final FuncTemplate[] _funcTemplates;
	
	// counter
	private int _count;

	// abnormal effect mask
	private short _abnormalEffect;

	public final class EffectTask implements Runnable
	{	
		protected final int delay;
		protected final int rate;
		EffectTask(int delay, int rate)
		{
			this.delay = delay;
			this.rate = rate;
		}
		public void run()
		{
			try
			{
				L2Effect.this.scheduleEffect();
			}
			catch (Throwable e)
			{
				_log.log(Level.SEVERE, "", e);
			}
		}
	}
	
	private ScheduledFuture _currentFuture;
	private EffectTask _currentTask;
	
	protected L2Effect(Env env, EffectTemplate template)
	{
		_state = EffectState.CREATED;
		_skill = env._skill;
		_item = env._item == null ? null : env._item.getItem();
		_effected = env._target;
		_effector = env._player;
		_lambda = template._lambda;
		_funcTemplates = template._funcTemplates;
		_count = template._counter;
		_period = template._period;
		_abnormalEffect = template._abnormalEffect;
		scheduleEffect();
	}
	
	public final L2Skill getSkill()
	{
		return _skill;
	}
	
	public final L2Character getEffector()
	{
		return _effector;
	}
	
	public final L2Character getEffected()
	{
		return _effected;
	}
	
	public final double calc()
	{
		Env env = new Env();
		env._player = _effector;
		env._target = _effected;
		env._skill  = _skill;
		return _lambda.calc(env);
	}
	
	private synchronized void startEffectTask(int duration)
	{
		stopEffectTask();
		_currentTask = new EffectTask(duration, -1);
        _currentFuture = ClientScheduler.getInstance().scheduleLow(
        		_currentTask, duration);
		if (_state == EffectState.ACTING)
			_effected.addEffect(this);
		updateEffects(_effected);
	}
	
	private synchronized void startEffectTaskAtFixedRate(int delay, int rate)
	{
		stopEffectTask();
		_currentTask = new EffectTask(delay, rate);
        _currentFuture = ClientScheduler.getInstance().scheduleLowAtFixedRate(
        		_currentTask, delay, rate);
		if (_state == EffectState.ACTING)
			_effected.addEffect(this);
		updateEffects(_effected);
	}
	
	public final void exit()
	{
		_state = EffectState.FINISHING;
		scheduleEffect();
	}
	
	private synchronized void stopEffectTask()
	{
		if (_currentFuture != null)
		{
			_currentFuture.cancel(false);
			_currentFuture = null;
			_currentTask = null;
			_effected.removeEffect(this);
			_effected.updateStats();
		}
	}
	
	/** returns effect type */
	public abstract EffectType getEffectType();
	
	/** Notify started */
	public void onStart() {
		if (_abnormalEffect != 0)
			getEffected().startAbnormalEffect(_abnormalEffect);
	}
	
	/** Notify exited */
	public void onExit() {
		if (_abnormalEffect != 0)
			getEffected().stopAbnormalEffect(_abnormalEffect);
	}
	
	/** Return true for continueation of this effect */
	public abstract boolean onActionTime();
	
	public final void scheduleEffect()
	{
		if (_state == EffectState.CREATED)
		{
			_state = EffectState.ACTING;
			onStart();
			if (_count > 1) {
				startEffectTaskAtFixedRate(5, _period*1000);
				return;
			}
			if (_period > 0) {
				startEffectTask(_period*1000);
				return;
			}
		}
		if (_state == EffectState.ACTING)
		{
			if (_count-- > 0)
			{
				if (onActionTime())
					return;
			}
			_state = EffectState.FINISHING;
		}
		if (_state == EffectState.FINISHING)
		{
			onExit();
			stopEffectTask();
		}
	}
	
	public void updateEffects(L2Character effectedChar)
	{
		effectedChar.updateStats();
	}
	
    public Func[] getStatFuncs()
    {
    	if (_funcTemplates == null)
    		return _emptyFunctionSet;
    	ArrayList<Func> funcs = new ArrayList<Func>();
		for (FuncTemplate t : _funcTemplates) {
	    	Env env = new Env();
	    	env._player = getEffector();
	    	env._target = getEffected();
	    	env._skill = getSkill();
	    	Func f = t.getFunc(env, this); // effect is owner
	    	if (f != null)
	    		funcs.add(f);
		}
    	if (funcs.size() == 0)
    		return _emptyFunctionSet;
    	return funcs.toArray(new Func[funcs.size()]);
    }

	public final void addIcon(MagicEffectIcons mi)
	{
		EffectTask task = _currentTask;
		ScheduledFuture future = _currentFuture;
		if (task == null || future == null)
			return;
		if (_state == EffectState.FINISHING || _state == EffectState.CREATED) 
			return;
		L2Skill sk = getSkill();
		if (task.rate > 0)
			mi.addEffect(sk.getId(), getLevel(), -1);
		else
			mi.addEffect(sk.getId(), getLevel(), (int)future.getDelay(TimeUnit.MILLISECONDS));
	}
	
	protected int getLevel() { return getSkill().getLevel(); }
}
