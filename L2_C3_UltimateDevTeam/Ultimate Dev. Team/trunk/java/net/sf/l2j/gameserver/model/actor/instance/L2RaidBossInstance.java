/*
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA.
 *
 * http://www.gnu.org/copyleft/gpl.html
 */
package net.sf.l2j.gameserver.model.actor.instance;

import net.sf.l2j.gameserver.ai.CtrlIntention;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.packets.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;

import java.util.logging.Logger;

/**
 * This class manages all RaidBoss. 
 * In a group mob, there are one master called RaidBoss and several slaves called Minions.
 * 
 * @version $Revision: 1.20.4.6 $ $Date: 2005/04/06 16:13:39 $
 */
public final class L2RaidBossInstance extends L2MonsterInstance
{
	protected static Logger _log = Logger.getLogger(L2RaidBossInstance.class.getName());
	private boolean _teleportedToNest;
    
    private static final int RAIDBOSS_MAINTENANCE_INTERVAL = 60000;

	/**
	 * Constructor of L2RaidBossInstance (use L2Character and L2NpcInstance constructor).<BR><BR>
	 *  
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Call the L2Character constructor to set the _template of the L2RaidBossInstance (copy skills from template to object and link _calculators to NPC_STD_CALCULATOR) </li>
	 * <li>Set the name of the L2RaidBossInstance</li>
	 * <li>Create a RandomAnimation Task that will be launched after the calculated delay if the server allow it </li><BR><BR>
	 * 
	 * @param objectId Identifier of the object to initialized
	 * @param L2NpcTemplate Template to apply to the NPC
	 */
	public L2RaidBossInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
	}
    
    public boolean isRaid()
    {
        return true; 
    }

    protected int getMaintenanceInterval() { return RAIDBOSS_MAINTENANCE_INTERVAL; }
	
    public void doDie(L2Character killer)
    {
        if(killer instanceof L2PcInstance)
        {
            L2PcInstance player = (L2PcInstance) killer;
            if (player.getParty() != null)
            {
                player.getParty().broadcastToPartyMembers(new SystemMessage(1209));
            }
            else
            {
                player.sendPacket(new SystemMessage(1209));
            }
        }
        super.doDie(killer);
    }
    /**
     * Used by Orfen to set 'teleported' flag, when hp goes to <50%
     * @param flag
     */
    public void setTeleported(boolean flag)
    {
        _teleportedToNest = flag;
    }
    
    public boolean getTeleported()
    {
        return _teleportedToNest;
    }
    /**
     * Reduce the current HP of the L2Attackable, update its _aggroList and launch the doDie Task if necessary.<BR><BR> 
     * 
     */
    public void reduceCurrentHp(double damage, L2Character attacker, boolean awake)
    {
        switch (getTemplate().npcId)
        {
            case 12169: // Orfen
                if ((getCurrentHp() - damage) < getMaxHp() / 2 && !getTeleported())
                {
                    clearAggroList();
                    getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
                    teleToLocation(43577,15985,-4396);
                    setTeleported(true);
                }
                break;
            default:
        }

        super.reduceCurrentHp(damage, attacker, awake);
    }

    
}
