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


public enum L2ArmorType {
	NONE      ( 1, "None"),
	LIGHT     ( 2, "Light"),
	HEAVY     ( 3, "Heavy"),
	MAGIC     ( 4, "Magic");
	
	final int _id;
	final String _name;
	L2ArmorType(int id, String name)
	{
		_id = id;
		_name = name;
	}
	public int mask() {
		return 1<<(_id+16);
	}
	public String toString()
	{
		return _name;
	}
}
