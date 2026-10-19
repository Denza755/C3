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

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.ClientScheduler;


/**
 * 
 * This class ...
 * 
 * @version $Revision: 1.2.2.1.2.5 $ $Date: 2005/03/27 15:29:30 $
 */
public class L2Potion extends L2Object
{
    protected static final Logger _log = Logger.getLogger(L2Character.class.getName());


	L2Character _target;
	
	private Future _potionhpRegTask;
	private boolean _potionhpRegenActive;
	private Future _potionmpRegTask;
	private boolean _potionmpRegenActive;
    protected int _milliseconds;
    protected double _effect;
    protected int _duration;
	private int _potion;
    protected Object _mpLock = new Object();
    protected Object _hpLock = new Object();
	
	
	class PotionHpHealing implements Runnable
	{
		L2Character _instance;
				
		public PotionHpHealing(L2Character instance)
		{
			_instance = instance;
		}
		
		public void run()
		{
			try
			{
				synchronized(_hpLock)
				{
					double nowHp = _instance.getCurrentHp();
					if(_duration == 0)
					{
						stopPotionHpRegeneration();
					}
					if (_duration != 0)
					{
						nowHp     += _effect;
						_instance.setCurrentHp(nowHp);
						_duration = _duration - (_milliseconds / 1000);
						setCurrentHpPotion2(_instance);
					}
				}
			}
			catch (Exception e)
			{
				_log.warning("Error in hp potion task:"+e);
			}
		}
	}
	
	
	public L2Potion(int objectId)
	{
		super(objectId);
	}
	
	private void startPotionHpRegeneration(L2Character activeChar)
	{
		_potionhpRegTask = ClientScheduler.getInstance().scheduleMedAtFixedRate(
				new PotionHpHealing(activeChar), 1000, _milliseconds);
		_potionhpRegenActive = true;
		if (Config.DEBUG) _log.fine("Potion HP regen Started");
	}

	public void stopPotionHpRegeneration()
	{
		if (_potionhpRegTask != null)
		{
			_potionhpRegTask.cancel(false);
		}
		_potionhpRegTask = null;
		_potionhpRegenActive = false;
		if (Config.DEBUG) _log.fine("Potion HP regen stop");
	}

	public void setCurrentHpPotion2(L2Character activeChar)
	{
		if (_duration == 0)
		{
			stopPotionHpRegeneration();
		}
	}

	class PotionMpHealing implements Runnable
	{
		L2Character _instance;
		
		public PotionMpHealing(L2Character instance)
		{
			_instance = instance;
		}
		
		public void run()
		{
			try
			{
				synchronized(_mpLock)
				{
					double nowMp = _instance.getCurrentMp();
					if(_duration == 0)
					{
						stopPotionMpRegeneration();
					}
					if (_duration != 0)
					{
						nowMp+=_effect;
						_instance.setCurrentMp(nowMp);
						_duration=(_duration-(_milliseconds/1000));
						setCurrentMpPotion2(_instance);
						
					}
				}
			}
			catch (Exception e)
			{
				_log.warning("error in mp potion task:"+e);
			}
		}
	}
	
	private void startPotionMpRegeneration(L2Character activeChar)
	{
		_potionmpRegTask = ClientScheduler.getInstance().scheduleMedAtFixedRate(
				new PotionMpHealing(activeChar), 1000, _milliseconds);
		_potionmpRegenActive = true;
		if (Config.DEBUG) _log.fine("Potion MP regen Started");
	}

	public void stopPotionMpRegeneration()
	{
		if (_potionmpRegTask != null)
		{
			_potionmpRegTask.cancel(false);
		}

		_potionmpRegTask = null;
		_potionmpRegenActive = false;
		if (Config.DEBUG) _log.fine("Potion MP regen stop");
	}

	public void setCurrentMpPotion2(L2Character activeChar)
	{
		if (_duration == 0)
		{
			stopPotionMpRegeneration();
		}
	}
	
	public void setCurrentMpPotion1(L2Character activeChar, int item)
	{
		_potion = item;
		_target = activeChar;

		switch (_potion)
		{
			case (726): // mana drug
				_milliseconds = 1000;
				_duration = 15;
				_effect = 1.5;
				startPotionMpRegeneration(activeChar);
				break;	
		}
	}

    /* (non-Javadoc)
     * @see net.sf.l2j.gameserver.model.L2Object#isAttackable()
     */
    public boolean isAutoAttackable(L2Character attacker)
    {
        return false;
    }
}