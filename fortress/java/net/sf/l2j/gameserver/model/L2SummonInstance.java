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

import net.sf.l2j.gameserver.ClientScheduler;
import net.sf.l2j.gameserver.serverpackets.PetLiveTime;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;

public class L2SummonInstance extends L2Summon
{
	public final int CYCLE = 1000; // in millis
	public final int MAX_TIME = 4800000;
	private float _expPenalty = 0;
    private int _itemConsumeIdInTime;
    private int _itemConsumeCountInTime;
    private int _itemConsumeDelay;
	
	private Future _disappearTask;
    private Future _consumeTask;

	private long _summoned;
	private int _aliveTime;

	public L2SummonInstance(int objectId, L2NpcTemplate template, L2PcInstance owner)
	{
		super(objectId, template, owner);
		setShowSummonAnimation(true);
		_summoned = System.currentTimeMillis();
		_aliveTime = MAX_TIME;
		_disappearTask = ClientScheduler.getInstance().scheduleLow(new TimeC3(this), CYCLE);
	}

	public final int getLevel()
	{
		return getTemplate().level;
	}

	public int getSummonType()
	{
		return 1;
	}

	public int getCurrentFed()
	{
		return _aliveTime;
	}

	public int getMaxFed()
	{
		return MAX_TIME;
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
		_owner.addExpAndSp(addToExp, addToSp);
	}

	public void reduceCurrentHp(int damage, L2Character attacker)
	{
		super.reduceCurrentHp(damage, attacker);
		SystemMessage sm = new SystemMessage(SystemMessage.THE_SUMMONED_MONSTER_RECEIVED_DAMAGE_OF_S2_CAUSED_BY_S1);

		if(attacker instanceof L2NpcInstance)
			sm.addNpcName(((L2NpcInstance)attacker).getTemplate().npcId);
		else
			sm.addString(attacker.getName());

		_owner.sendPacket(sm.addNumber(damage));
	}

	protected void displayHitMessage(int damage, boolean crit, boolean miss)
	{
		if(crit)
			_owner.sendPacket(new SystemMessage(SystemMessage.SUMMONED_MONSTERS_CRITICAL_HIT));
		if(miss)
			_owner.sendPacket(new SystemMessage(SystemMessage.MISSED_TARGET));
		else
			_owner.sendPacket(new SystemMessage(SystemMessage.THE_SUMMONED_MONSTER_GAVE_DAMAGE_OF_S1).addNumber(damage));
	}

	class TimeC3 implements Runnable
	{
		private L2SummonInstance _summon;

		TimeC3(L2SummonInstance summon)
		{
			_summon = summon;
		}

		public void run()
		{
			_aliveTime -= isInCombat() ? 4 * CYCLE : CYCLE;

			if(_summoned + _aliveTime < System.currentTimeMillis())
			{
				_disappearTask = null;
				unSummon(_owner);
				return;
			}
			_owner.sendPacket(new PetLiveTime(_summon));

			_disappearTask = ClientScheduler.getInstance().scheduleLow(new TimeC3(_summon), CYCLE);
		}
	}

	public void doDie(L2Character killer)
	{
		_disappearTask.cancel(false);
		_disappearTask = null;
        stopConsume();
		super.doDie(killer);
	}

	public void removeItemFromInventory(L2ItemInstance item, int count)
	{
		_owner.removeItemFromInventory(item, count);
	}
	
    static class Consume implements Runnable
    {
        private L2PcInstance _activeChar;
        private L2SummonInstance _summon;
        
        Consume(L2PcInstance activeChar, L2SummonInstance newpet)
        {
            _activeChar = activeChar;
            _summon = newpet;
        }
        
        public void run()
        {
            L2ItemInstance item =_activeChar.getInventory().findItemByItemId(_summon.getItemConsumeIdInTime());
            if(item!=null && item.getCount()>=_summon.getItemConsumeCountInTime())
                _activeChar.getInventory().destroyItemByItemId(_summon.getItemConsumeIdInTime(),_summon.getItemConsumeCountInTime());
            else
            {
                _summon.unSummon(_activeChar);
            }
        }
    }
    
    public void setItemConsumeIdInTime(int id)
    {
        _itemConsumeIdInTime = id;
    }
    
    public int getItemConsumeIdInTime()
    {
        return _itemConsumeIdInTime;
    }
    
    public void setItemConsumeCountInTime(int count)
    {
        _itemConsumeCountInTime = count;
    }
    
    public int getItemConsumeCountInTime()
    {
        return _itemConsumeCountInTime;
    }
    
    public void setItemConsumeDelay(int time)
    {
        _itemConsumeDelay = time;
    }
    
    public int getItemConsumeDelay()
    {
        return _itemConsumeDelay;
    }
    
    protected synchronized void stopConsume()
    {
        if (_consumeTask != null)
        {
            _consumeTask.cancel(false);
            _consumeTask = null;
        }
    }
    
    public synchronized void startConsume()
    {
        if (_consumeTask == null && !isDead())
            if(_itemConsumeIdInTime!=0 && _itemConsumeCountInTime!=0 && _itemConsumeDelay!=0)
                _consumeTask = ClientScheduler.getInstance().scheduleMedAtFixedRate(new Consume(getOwner(),this), _itemConsumeDelay*1000, _itemConsumeDelay*1000);
    }
    public synchronized void unSummon (L2PcInstance owner)
    {
        stopConsume();
        super.unSummon(owner);
    }
}
