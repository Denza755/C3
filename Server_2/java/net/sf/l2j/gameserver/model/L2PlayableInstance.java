package net.sf.l2j.gameserver.model;

import net.sf.l2j.gameserver.templates.L2CharTemplate;

public abstract class L2PlayableInstance extends L2Character {

	public L2PlayableInstance(int objectId, L2CharTemplate template)
	{
		super(objectId, template);
	}
	public abstract void removeItemFromInventory(L2ItemInstance item, int count);
}
