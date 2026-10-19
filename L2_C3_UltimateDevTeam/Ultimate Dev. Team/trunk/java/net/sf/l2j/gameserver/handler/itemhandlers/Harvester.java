package net.sf.l2j.gameserver.handler.itemhandlers;

import net.sf.l2j.gameserver.handler.IItemHandler;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2MonsterInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PlayableInstance;
import net.sf.l2j.gameserver.packets.serverpackets.InventoryUpdate;
import net.sf.l2j.gameserver.packets.serverpackets.SystemMessage;

import java.util.Random;

public class Harvester implements IItemHandler
{
    private static int[] _itemIds = { 5125 };
    L2PcInstance player;
    L2MonsterInstance target;
    
    public void useItem(L2PlayableInstance playable, L2ItemInstance _item)
    {
        InventoryUpdate iu = new InventoryUpdate();
        player = (L2PcInstance)playable;

        if(player.getTarget() == null || !(player.getTarget() instanceof L2MonsterInstance))
        {
            player.sendPacket(new SystemMessage(SystemMessage.TARGET_IS_INCORRECT));
            return;
        }

        target = (L2MonsterInstance)player.getTarget();
        boolean send = false;
        int total = 0;
        int cropId = 0;
        
        if (target.isSeeded() && target.isDead() && calcSuccess())
        {
            L2ItemInstance[] items = target.takeHarvest();
            if (items != null && items.length > 0)
            {
                for (L2ItemInstance item : items)
                {
                    cropId = item.getItemId(); // always got 1 type of crop as reward
                    if (player.isInParty() && player.getParty().isDistributeSpoilLoot())
                    {
                        player.getParty().distributeItem(player, item);
                    }
                    else
                    {
                        int itemCount = item.getCount();
                        total += itemCount;
                        item = player.getInventory().addItem(item);
                        if (item.getLastChange() == L2ItemInstance.ADDED)
                        {
                            iu.addNewItem(item);
                        }
                        else
                        {
                            iu.addModifiedItem(item);
                        }
                        send = true;
                    }
                }
                if (send)
                {
                    SystemMessage smsg = new SystemMessage(SystemMessage.YOU_PICKED_UP_S1_S2);
                    smsg.addNumber(total);
                    smsg.addItemName(cropId);
                    player.sendPacket(smsg);
                    player.sendPacket(iu);
                }
            }
        }
        else
        		player.sendMessage("Target not seeded");
    }
    
    public int[] getItemIds()
    {
        return _itemIds;
    }
    
    private boolean calcSuccess()
    {
        int basicSuccess = 90;
        int levelPlayer = player.getLevel();
        int levelTarget = target.getLevel();

        int diffPlayerTarget = (levelPlayer - levelTarget);
        if(diffPlayerTarget < 0)
            diffPlayerTarget = -diffPlayerTarget;
                
        // apply penalty, target <=> player levels
        // 15% penalty for each level
        if(diffPlayerTarget > 5)
        {
            basicSuccess -= (diffPlayerTarget - 5)*15;
        }
        
        
        // success rate cant be less than 1%
        if(basicSuccess < 1)
            basicSuccess = 1;
        
        Random rnd = new Random();
        int rate = rnd.nextInt(99);
        
        if(rate < basicSuccess)
            return true;
        return false;
    }

}
