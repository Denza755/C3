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
package net.sf.l2j.gameserver.skills;

import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.templates.L2Item;



public class FuncEnchant extends Func {
	private final Lambda _lambda;
	public FuncEnchant(Stats stat, int order, Object owner, Lambda lambda) {
		super(stat, order, owner);
		_lambda = lambda;
	}
	public void calc(Env env)
	{
		if (_cond != null && !_cond.test(env))
			return;
		L2ItemInstance item = (L2ItemInstance)_funcOwner;
		int cristall = item.getItem().getCrystalType();
		if (cristall == L2Item.CRYSTAL_NONE)
			return;
		int enchant = item.getEnchantLevel();
		if (enchant < 4)
		{
			env.value += 4 * enchant;
			return;
		}
		switch (item.getItem().getCrystalType())
		{
		case L2Item.CRYSTAL_A:
			env.value += 4*3+8*item.getEnchantLevel();
			return;
		case L2Item.CRYSTAL_B:
			env.value += 3*3+6*item.getEnchantLevel();
			return;
		case L2Item.CRYSTAL_C:
			env.value += 3*3+6*item.getEnchantLevel();
			return;
		case L2Item.CRYSTAL_D:
			env.value += 2*3+4*item.getEnchantLevel();
			return;
		case L2Item.CRYSTAL_S:
			env.value += 5*3+10*item.getEnchantLevel();
			return;
		}
		return;
	}
}
