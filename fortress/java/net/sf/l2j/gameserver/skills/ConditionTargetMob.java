package net.sf.l2j.gameserver.skills;

import net.sf.l2j.gameserver.model.L2MonsterInstance;

public class ConditionTargetMob extends Condition
{
	final boolean _isMob;
	
	ConditionTargetMob(boolean isMob)
	{
		_isMob = isMob;
	}
	
	boolean testImpl(Env env)
	{
		return (env._target instanceof L2MonsterInstance) == _isMob;
	}
}
