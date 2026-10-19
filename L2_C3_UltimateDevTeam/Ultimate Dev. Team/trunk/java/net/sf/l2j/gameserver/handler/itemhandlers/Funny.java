package net.sf.l2j.gameserver.handler.itemhandlers;

import net.sf.l2j.gameserver.handler.IItemHandler;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PlayableInstance;
import net.sf.l2j.gameserver.packets.serverpackets.MagicSkillUser;

public class Funny implements IItemHandler
{
	private static int[] _itemIds = { 5283 };
	public void useItem(L2PlayableInstance playable, L2ItemInstance item)
	{
		if (!(playable instanceof L2PcInstance))
			return;
		L2PcInstance activeChar = (L2PcInstance)playable;
	    int itemId = item.getItemId();

        if (itemId == 5283) // firework
        {
            MagicSkillUser MSU = new MagicSkillUser(playable, activeChar, 2025, 1, 1, 0);
            activeChar.sendPacket(MSU);
            activeChar.broadcastPacket(MSU);
        }

		activeChar.removeItemFromInventory(item, 1);
	}
	
	public int[] getItemIds()
	{
		return _itemIds;
	}
}
