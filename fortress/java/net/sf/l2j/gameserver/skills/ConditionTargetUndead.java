package net.sf.l2j.gameserver.skills;

import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2MonsterInstance;

public class ConditionTargetUndead extends Condition
{
    final boolean _isUndead;
    
    ConditionTargetUndead(boolean isUndead)
    {
        _isUndead = isUndead;
    }

    boolean testImpl(Env env)
    {
        L2Character target = (L2Character)env._player.getTarget();
        if(target == null)
            return false;
        if (target instanceof L2MonsterInstance)
            return ((L2MonsterInstance)target).isUndead() == _isUndead;
        return false;
    }
}
