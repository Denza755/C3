package net.sf.l2j.gameserver.handler.itemhandlers;

import net.sf.l2j.gameserver.handler.IItemHandler;
import net.sf.l2j.gameserver.instancemanager.CastleManager;
import net.sf.l2j.gameserver.model.entity.Castle;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2PlayableInstance;
import net.sf.l2j.gameserver.model.L2PcInstance;
import net.sf.l2j.gameserver.model.L2Spawn;
import net.sf.l2j.gameserver.model.quest.QuestPcSpawn;

public class MercTicket implements IItemHandler
{
    private static int[] _ItemIds = {
                                     3960, 3961, 3962, 3963, 3964, 3965, 3966, 3967, 3968, 3969, // Gludio
                                     3973, 3974, 3975, 3976, 3977, 3978, 3979, 3980, 3981, 3982, // Dion
                                     3986, 3987, 3988, 3989, 3990, 3991, 3992, 3993, 3994, 3995, // Giran
                                     3999, 4000, 4001, 4002, 4003, 4004, 4005, 4006, 4007, 4008, // Oren
                                     4012, 4013, 4014, 4015, 4016, 4017, 4018, 4019, 4020, 4021 // Aden
                                     };

    private static int[] _NpcIds = {
                                    12301, 12302, 12303, 12304, 12305, 12306, 12307, 12308, 12309, 12310, // Gludio
                                    12301, 12302, 12303, 12304, 12305, 12306, 12307, 12308, 12309, 12310, // Dion
                                    12301, 12302, 12303, 12304, 12305, 12306, 12307, 12308, 12309, 12310, // Giran
                                    12301, 12302, 12303, 12304, 12305, 12306, 12307, 12308, 12309, 12310, // Oren
                                    12301, 12302, 12303, 12304, 12305, 12306, 12307, 12308, 12309, 12310 // Aden
                                    };

    private static String[] _Messages = {
                                         "To arms!.",
                                         "I am ready to serve you my lord when the time comes.",
                                         "You summon me."
                                        };
    
    public void useItem(L2PlayableInstance playable, L2ItemInstance item)
    {
        L2PcInstance activeChar = (L2PcInstance)playable;
        if (activeChar.getClan() == null || // Not in a clan
                !activeChar.isClanLeader()) // Not leader of clan
            return;
        
        Castle castle = CastleManager.getInstance().getCastle(activeChar.getClan().getHasCastle());
        if (castle == null)
            return;

        if (castle.getSiege().getIsInProgress())
        {
            activeChar.sendMessage("You cannot hire mercenary while siege is in progress!");
            return;
        }
        // TODO
        /*if (HIRED_MERC_COUNT_FOR_CASTLE >= CONFIG_MAX_MERC)
        {
            activeChar.sendMessage("You cannot hire any more mercenary!");
            return;
        }*/

        int itemId = item.getItemId();
        for (int i = 0; i < _ItemIds.length; i++)
        {
            if (_ItemIds[i] == itemId) // Find the index of the item used
            {
                // Temporary tap into QuestPcSpawn to spawn, auto chat, and despawn merc
                QuestPcSpawn qps = new QuestPcSpawn(new L2PcInstance[] {activeChar});
                int spawnObjectId = qps.addSpawn(_NpcIds[i], activeChar.getX(), activeChar.getY(), activeChar.getZ(), 3000, _Messages, 0); // Spawn, auto chat, and despawn merc.
                L2Spawn spawn = qps.getSpawn(spawnObjectId);

                // Hire merc for this caslte.  NpcId is at the same index as the item used.
                castle.getSiege().getSiegeGuardManager().hireMerc(spawn.getLocx(), spawn.getLocy(), spawn.getLocz(), spawn.getHeading(), _NpcIds[i]);

                activeChar.removeItemFromInventory(item, 1); // Remove item from char's inventory
                activeChar.sendMessage("Hired mercenary ("+itemId+","+_NpcIds[i]+") at coords:" + spawn.getLocx() + "," + spawn.getLocy() + "," + spawn.getLocz() + " heading:" + spawn.getHeading());
                break;
            }
        }
    }

    public int[] getItemIds()
    {
        return _ItemIds;
    }
}
