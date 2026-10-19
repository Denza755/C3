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
package net.sf.l2j.gameserver.templates;

import java.util.ArrayList;

import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.skills.Env;
import net.sf.l2j.gameserver.skills.Func;
import net.sf.l2j.gameserver.skills.FuncTemplate;

/**
 * This class ...
 * 
 * @version $Revision: 1.4.2.3.2.5 $ $Date: 2005/04/02 15:57:51 $
 */
public final class L2Weapon  extends L2Item
{
	private final int _soulShotCount;
	private final int _spiritShotCount;
	private final int _pDam;
	private final int _rndDam;
	private final int _critical;
	private final double _hitModifier;
	private final int _avoidModifier;
	private final int _shieldDef;
	private final double _shieldDefRate;
	private final int _atkSpeed;
	private final int _atkReuse;
	private final int _mpConsume;
	private final int _mDam;
    
	public L2Weapon(L2WeaponType type, StatsSet set)
	{
		super(type, set);
		_soulShotCount   = set.getIntegr("soulshots");
		_spiritShotCount = set.getIntegr("spiritshots");
		_pDam            = set.getIntegr("p_dam");
		_rndDam          = set.getIntegr("rnd_dam");
		_critical        = set.getIntegr("critical");
		_hitModifier     = set.getDouble("hit_modify");
		_avoidModifier   = set.getIntegr("avoid_modify");
		_shieldDef       = set.getIntegr("shield_def");
		_shieldDefRate   = set.getDouble("shield_def_rate");
		_atkSpeed        = set.getIntegr("atk_speed");
		_atkReuse        = set.getIntegr("atk_reuse", type==L2WeaponType.BOW ? 1500 : 0);
		_mpConsume       = set.getIntegr("mp_consume");
		_mDam            = set.getIntegr("m_dam");
	}
	
	public L2WeaponType getItemType()
	{
		return (L2WeaponType)super.type;
	}

	public int getItemMask()
	{
		return getItemType().mask();
	}
	
	/**
	 * @return Returns the soulShotCount.
	 */
	public int getSoulShotCount()
	{
		return _soulShotCount;
	}
	/**
	 * @return Returns the spiritShotCount.
	 */
	public int getSpiritShotCount()
	{
		return _spiritShotCount;
	}
	/**
	 * @return Returns the pDam.
	 */
	public int getPDamage()
	{
		return _pDam;
	}
	/**
	 * @return Returns the rndDam.
	 */
	public int getRandomDamage()
	{
		return _rndDam;
	}
	/**
	 * @return Returns the atkSpeed.
	 */
	public int getAttackSpeed()
	{
		return _atkSpeed;
	}
	/**
	 * @return Returns the atkReuse.
	 */
	public int getAttackReuseDelay()
	{
		return _atkReuse;
	}
	/**
	 * @return Returns the avoidModifier.
	 */
	public int getAvoidModifier()
	{
		return _avoidModifier;
	}
	/**
	 * @return Returns the critical.
	 */
	public int getCritical()
	{
		return _critical;
	}
	/**
	 * @return Returns the hitModifier.
	 */
	public double getHitModifier()
	{
		return _hitModifier;
	}
	/**
	 * @return Returns the mDam.
	 */
	public int getMDamage()
	{
		return _mDam;
	}
	/**
	 * @return Returns the mpConsume.
	 */
	public int getMpConsume()
	{
		return _mpConsume;
	}
	/**
	 * @return Returns the shieldDef.
	 */
	public int getShieldDef()
	{
		return _shieldDef;
	}
	/**
	 * @return Returns the shieldDefRate.
	 */
	public double getShieldDefRate()
	{
		return _shieldDefRate;
	}

	public Func[] getStatFuncs(L2ItemInstance instance, L2Character player)
    {
    	ArrayList<Func> funcs = new ArrayList<Func>();
    	if (_funcTemplates != null)
    	{
    		for (FuncTemplate t : _funcTemplates) {
		    	Env env = new Env();
		    	env._player = player;
		    	env._item = instance;
		    	Func f = t.getFunc(env, instance);
		    	if (f != null)
		    		funcs.add(f);
    		}
    	}
    	return funcs.toArray(new Func[funcs.size()]);
    }
}
