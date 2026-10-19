package net.sf.l2j.gameserver.skills;

import net.sf.l2j.gameserver.model.L2PlayableInstance;

public class ConditionTargetPlayable extends Condition
{
	boolean _flag;

	ConditionTargetPlayable(boolean flag)
	{
		_flag = flag;
	}

	boolean testImpl(Env env)
	{
		return (env._target instanceof L2PlayableInstance) == _flag;
	}

}
