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
 * [URL]http://www.gnu.org/copyleft/gpl.html[/URL]
 */
package net.sf.l2j.gameserver.model;

import java.util.ArrayList;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.l2j.gameserver.ClientScheduler;
import net.sf.l2j.gameserver.SkillTable;
import net.sf.l2j.gameserver.TaskPriority;
import net.sf.l2j.gameserver.handler.ISkillHandler;
import net.sf.l2j.gameserver.handler.SkillHandler;
import net.sf.l2j.gameserver.lib.Rnd;
import net.sf.l2j.gameserver.serverpackets.MagicSkillUser;

public class L2CubicInstance
{
    protected static Logger _log = Logger.getLogger(L2CubicInstance.class.getName());

    public static final int STORM_CUBIC = 1;
    public static final int VAMPIRIC_CUBIC = 2;
    public static final int LIFE_CUBIC = 3;
    public static final int VIPER_CUBIC = 4;
    public static final int POLTERGEIST_CUBIC = 5;
    public static final int BINDING_CUBIC = 6;
    public static final int AQUA_CUBIC = 7;
    public static final int SPARK_CUBIC = 8;
	public static final int NOVICE_LIFE_CUBIC = 9;

    protected L2PcInstance _owner;
    protected L2Character _target;

    protected int _id;
    protected int _level = 1;

    protected ArrayList<Integer> _skills = new ArrayList<Integer>();

    private Future _disappearTask;
    private Future _actionTask;

    public L2CubicInstance(L2PcInstance owner, int id, int level)
    {
        _owner = owner;
        _id = id;
        _level = level;

        switch(_id)
        {
            case STORM_CUBIC:
            	_disappearTask = ClientScheduler.getInstance().scheduleLow(new Disappear(), 1200000); // disappear in 20 mins
                _skills.add(4049);
                break;
            case VAMPIRIC_CUBIC:
            	_disappearTask = ClientScheduler.getInstance().scheduleLow(new Disappear(), 1200000); // disappear in 20 mins
                _skills.add(4050);
                break;
            case LIFE_CUBIC:
            	_disappearTask = ClientScheduler.getInstance().scheduleLow(new Disappear(), 1200000); // disappear in 20 mins
                _skills.add(4051);
                _actionTask = ClientScheduler.getInstance().scheduleAtFixedRate(new Heal(), TaskPriority.PR_LOW, 0, 20000);
                break;
            case VIPER_CUBIC:
            	_disappearTask = ClientScheduler.getInstance().scheduleLow(new Disappear(), 1200000); // disappear in 20 mins
                _skills.add(4052);
                break;
            case POLTERGEIST_CUBIC:
            	_disappearTask = ClientScheduler.getInstance().scheduleLow(new Disappear(), 1200000); // disappear in 20 mins
                _skills.add(4053);
                _skills.add(4054);
                _skills.add(4055);
                break;
            case BINDING_CUBIC:
            	_disappearTask = ClientScheduler.getInstance().scheduleLow(new Disappear(), 1200000); // disappear in 20 mins
                _skills.add(4164);
                break;
            case AQUA_CUBIC:
            	_disappearTask = ClientScheduler.getInstance().scheduleLow(new Disappear(), 1200000); // disappear in 20 mins
                _skills.add(4165);
                break;
            case SPARK_CUBIC:
            	_disappearTask = ClientScheduler.getInstance().scheduleLow(new Disappear(), 1200000); // disappear in 20 mins
                _skills.add(4166);
                break;
            case NOVICE_LIFE_CUBIC:
            	_disappearTask = ClientScheduler.getInstance().scheduleLow(new Disappear(), 3600000); // disappear in 60 mins
                _skills.add(4051);
                _actionTask = ClientScheduler.getInstance().scheduleAtFixedRate(new Heal(), TaskPriority.PR_LOW, 0, 20000);
                break;
        }
    }

    public void doAction(L2Character target)
    {
        if (_target == target || _owner == target)
            return;
        stopAction();
        _target = target;
        switch (_id)
        {
            case STORM_CUBIC:
            case VAMPIRIC_CUBIC:
            case VIPER_CUBIC:
            case POLTERGEIST_CUBIC:
            case BINDING_CUBIC:
            case AQUA_CUBIC:
            case SPARK_CUBIC:
                _actionTask = ClientScheduler.getInstance().scheduleAtFixedRate(new Action(), TaskPriority.PR_LOW, 0, 20000);
                break;
        }
    }

    public int getId()
    {
        return _id;
    }

    public void setLevel(int level)
    {
        _level = level;
    }

    public void stopAction()
    {
        _target = null;
        if (_actionTask != null)
        {
            _actionTask.cancel(true);
            _actionTask = null;
        }
    }

    protected void cancelDisappear()
    {
        if (_disappearTask != null)
        {
            _disappearTask.cancel(true);
            _disappearTask = null;
        }
    }

    private class Action implements Runnable
    {
        Action()
        {
			// run task
        }

        public void run()
        {
            L2Skill skill = SkillTable.getInstance().getInfo(_skills.get(Rnd.get(_skills.size())), _level);
			if (skill != null)
			{
				if (_owner.isDead() || _target == null || _target.isDead() || _owner.getDistance(_target) >= skill.getCastRange())
				{
					stopAction();
					if (_owner.isDead())
					{
						_owner.delCubic(_id);
						_owner.broadcastUserInfo();
						cancelDisappear();
					}
					return;
				}
				try
				{
					if (Rnd.get(9) < 4)
					{
						L2Character[] targets = { _target };
						ISkillHandler handler = SkillHandler.getInstance().getSkillHandler(skill.getSkillType());
						if (handler != null)
							handler.useSkill(_owner, skill, targets);
						else
							skill.useSkill(_owner, targets);

						_owner.broadcastPacket(new MagicSkillUser(_owner, _target, skill.getId(), _level, 0, 0));
					}
				}
				catch (Exception e)
				{
					_log.log(Level.SEVERE, "", e);
				}
			}
        }
    }

    private class Heal implements Runnable
    {
        Heal()
        {
            // run task
        }

        public void run()
        {
            if (_owner.isDead())
            {
                stopAction();
                _owner.delCubic(_id);
                _owner.broadcastUserInfo();
                cancelDisappear();
                return;
            }
            if (_owner.getCurrentHp() < _owner.getMaxHp())
            {
                try
                {
                    if(Rnd.get(2) == 0) // 1/2 rate
                    {
                        L2Skill skill = SkillTable.getInstance().getInfo(_skills.get(Rnd.get(_skills.size())), _level);
                        if (skill != null)
						{
	                        L2Character[] targets = { _owner };
                            ISkillHandler handler = SkillHandler.getInstance().getSkillHandler(skill.getSkillType());
                            if (handler != null)
                                handler.useSkill(_owner, skill, targets);
                            else
                                skill.useSkill(_owner, targets);

	                        _owner.broadcastPacket(new MagicSkillUser(_owner, _owner, skill.getId(), _level, 0, 0));
						}
                    }
                }
                catch (Exception e)
                {
                }
            }
        }
    }

    private class Disappear implements Runnable
    {
        Disappear()
        {
			// run task
        }

        public void run()
        {
            stopAction();
            _owner.delCubic(_id);
            _owner.broadcastUserInfo();
        }
    }
}
