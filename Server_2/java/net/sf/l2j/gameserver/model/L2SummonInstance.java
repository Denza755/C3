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
package net.sf.l2j.gameserver.model;

import java.util.concurrent.Future;
import java.util.logging.Logger;

import net.sf.l2j.gameserver.ClientScheduler;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;

public class L2SummonInstance extends L2Summon
{
    private static Logger _log = Logger.getLogger(L2SummonInstance.class.getName());
    
    private float _expPenalty = 0; // exp decrease multiplier (i.e. 0.3 (= 30%) for shadow)
    
    private Future _disappearTask;
    
    public L2SummonInstance(int objectId, L2NpcTemplate template, L2PcInstance owner)
    {
		super(objectId, template, owner);
        setShowSummonAnimation(true);
        _disappearTask = ClientScheduler.getInstance().scheduleLow(new Disappear(getOwner(), this), 1200000); // disappear in 20 mins
    }
    
	public final int getLevel() {
		return getTemplate().level;
	}
	
    public int getSummonType()
	{
		return 1;
	}
	
	public void setExpPenalty(float expPenalty)
	{
		_expPenalty = expPenalty;
	}
    public float getExpPenalty()
    {
        return _expPenalty;
    }
    
    public void addExpAndSp(int addToExp, int addToSp)
    {
        getOwner().addExpAndSp(addToExp, addToSp);
    }
    
    public void reduceCurrentHp(int damage, L2Character attacker)
    {
        super.reduceCurrentHp(damage, attacker);
        SystemMessage sm = new SystemMessage(SystemMessage.SUMMON_RECEIVED_DAMAGE_OF_S2_BY_S1);
        if (attacker instanceof L2NpcInstance)
        {
            sm.addNpcName(((L2NpcInstance)attacker).getTemplate().npcId);
        }
        else
        {
            sm.addString(attacker.getName());
        }
        if (isDead())
        {
            _disappearTask.cancel(false);
            _disappearTask = null;
        }
        sm.addNumber(damage);
        getOwner().sendPacket(sm);
    }
    
    protected void displayHitMessage(int damage, boolean crit, boolean miss)
    {
        if (crit)
        {
            getOwner().sendPacket(new SystemMessage(SystemMessage.SUMMON_CRITICAL_HIT));
        }
        
        if (miss)
        {
            getOwner().sendPacket(new SystemMessage(SystemMessage.MISSED_TARGET));
        }
        else
        {
            SystemMessage sm = new SystemMessage(SystemMessage.SUMMON_GAVE_DAMAGE_OF_S1);
            sm.addNumber(damage);
            getOwner().sendPacket(sm);
        }
    }
    
    static class Disappear implements Runnable
    {
        private L2PcInstance _activeChar;
        private L2SummonInstance _summon;
        
        Disappear(L2PcInstance activeChar, L2SummonInstance newpet)
        {
            _activeChar = activeChar;
            _summon = newpet;
        }
        
        public void run()
        {
            _summon.PetdeleteMe(_activeChar);
        }
    }

	public void removeItemFromInventory(L2ItemInstance item, int count)
	{
		getOwner().removeItemFromInventory(item, count);
	}
	
}
