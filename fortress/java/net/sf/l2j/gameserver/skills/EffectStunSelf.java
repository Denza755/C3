package net.sf.l2j.gameserver.skills;

import net.sf.l2j.gameserver.model.L2Effect;

public class EffectStunSelf extends L2Effect
{
	public EffectStunSelf(Env env, EffectTemplate template)
	{
		super(env, template);
	}

	public EffectType getEffectType()
	{
		return EffectType.STUN_SELF;
	}

	public void onStart()
	{
		getEffector().startStunning();
	}

	public void onExit()
	{
		getEffector().stopStunning(this);
	}

	public boolean onActionTime()
	{
		// just stop this effect
		return false;
	}
}
