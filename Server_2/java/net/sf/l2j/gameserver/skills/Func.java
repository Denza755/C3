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



public abstract class Func {
	/**
	 * Statistics, that is affected by this function.
	 * See L2Character.CALCULATOR_XXX constants.
	 */
	public final Stats _stat; 
	
	/**
	 * Order of functions calculation.
	 * Functions with lower order are executed first.
	 * Functions with the same order are executed in unspecified order.
	 * Usually add/substruct functions has lowest order,
	 * then bonus/penalty functions (multiplay/divide) are
	 * applied, then functions that do more complex calculations
	 * (non-linear functions).
	 */
	public final int _order;
	
	/**
	 *  Owner can be an armor, weapon, skill, system event, quest, etc
	 *  Used to remove all functions added by this owner.
	 */
	public final Object _funcOwner;
	
	/**
	 *  Function may be disabled by attached condition.
	 */
	public Condition _cond;
	
	public Func(Stats stat, int order, Object owner) {
		_stat = stat;
		_order = order;
		_funcOwner = owner;
	}
	public void setCondition(Condition cond) {
		_cond = cond;
	}
	public abstract void calc(Env env);
}
