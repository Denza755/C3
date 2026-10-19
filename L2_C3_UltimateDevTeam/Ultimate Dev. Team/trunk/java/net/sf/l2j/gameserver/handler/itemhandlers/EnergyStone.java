
package net.sf.l2j.gameserver.handler.itemhandlers;

import net.sf.l2j.gameserver.handler.IItemHandler;
import net.sf.l2j.gameserver.idfactory.IdFactory;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2Potion;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PlayableInstance;
import net.sf.l2j.gameserver.packets.serverpackets.MagicSkillUser;
import net.sf.l2j.gameserver.skills.EffectCharge;


public class EnergyStone implements IItemHandler
{
    private static int[] _itemIds = { 5589};
    int num_charges;   
    
    public void useItem(L2PlayableInstance playable, L2ItemInstance item)
    {
        if (!(playable instanceof L2PcInstance))
            return;
        L2PcInstance activeChar = (L2PcInstance)playable;
        
        L2Potion potion = new L2Potion(IdFactory.getInstance().getNextId());
        int itemId = item.getItemId();
          
        if (itemId == 5589)
        {
            if ((activeChar.getClassId().getId() == 2) || (activeChar.getClassId().getId() == 48))
            {
                num_charges = (activeChar.getClassId().getId() == 2) ? activeChar.getSkillLevel(8) : activeChar.getSkillLevel(50);
                EffectCharge effect = (activeChar.getClassId().getId() == 2) ? (EffectCharge)activeChar.getEffect(8) : (EffectCharge)activeChar.getEffect(50);
                
                if (effect != null) {
                    if (effect.getLevel()  < num_charges)
                    {
                        MagicSkillUser MSU = new MagicSkillUser(playable, activeChar, 2165, 1, 1, 0);
                        activeChar.sendPacket(MSU);
                        activeChar.broadcastPacket(MSU);
                        effect.num_charges++;
                        activeChar.updateEffectIcons();
                        potion.setCurrentMpPotion1(playable, itemId);
                        playable.removeItemFromInventory(item, 1);
                        activeChar.sendMessage("Sonic charged.");
                    }else{
                        activeChar.sendMessage("Maximum amount of Sonics charged.");
                    }
                }
            }else{
                activeChar.sendMessage("Only Gladiators or Tyrants can use Energy Stone.");
            }
        }        
    }
    
    public int[] getItemIds()
    {
        return _itemIds;
    }
}