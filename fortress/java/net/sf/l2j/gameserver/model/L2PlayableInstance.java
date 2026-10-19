package net.sf.l2j.gameserver.model;

import net.sf.l2j.gameserver.templates.L2CharTemplate;

/**
 * This class represents all Playable characters in the world.
 * 
 * L2PlayableInstance:
 *  * L2PcInstance
 *  * L2Summon
 * 
 */

public abstract class L2PlayableInstance extends L2Character 
{
	protected class AIAccessor extends L2Character.AIAccessor
	{
		@Override
		public void detachAI() 
		{
			// do nothing, AI of players or pets can't be detached automatically
		}
	}
		
	public L2PlayableInstance(int objectId, L2CharTemplate template)
	{
		super(objectId, template);
	}
    
    public boolean isAttackable()
    {
        return true;
    }
    
	public abstract void removeItemFromInventory(L2ItemInstance item, int count);
}
