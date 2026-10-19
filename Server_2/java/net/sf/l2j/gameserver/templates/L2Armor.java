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
 * @version $Revision: 1.2.2.1.2.6 $ $Date: 2005/03/27 15:30:10 $
 */
public final class L2Armor extends L2Item
{
	private final int _avoidModifier;
	private final int _pDef;
	private final int _mDef;
	private final int _mpBonus;
	private final int _hpBonus;
	
    
	public L2Armor(L2ArmorType type, StatsSet set)
	{
		super(type, set);
		_avoidModifier = set.getIntegr("avoid_modify");
		_pDef          = set.getIntegr("p_def");
		_mDef          = set.getIntegr("m_def");
		_mpBonus       = set.getIntegr("mp_bonus", 0);
		_hpBonus       = set.getIntegr("hp_bonus", 0);
	}
	
	public L2ArmorType getItemType()
	{
		return (L2ArmorType)super.type;
	}
	public final int getItemMask()
	{
		return getItemType().mask();
	}
	
	public final int getMDef()
	{
		return _mDef;
	}
	public final int getPDef()
	{
		return _pDef;
	}
	public final int getAvoidModifier()
	{
		return _avoidModifier;
	}
	public final int getMpBonus()
	{
		return _mpBonus;
	}
	public final int getHpBonus()
	{
		return _hpBonus;
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
