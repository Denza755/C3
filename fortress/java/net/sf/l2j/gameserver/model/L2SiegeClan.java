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

import java.util.List;

import javolution.util.FastList;

public class L2SiegeClan
{
	// ==========================================================================================
	// Instance
    // ===============================================================
    // Data Field
	private int _ClanId                    = 0;
	private List<L2NpcInstance> _Flag  = new FastList<L2NpcInstance>();
    private int _TypeId                = 0; // -1 = owner, 0 = defender, 1 = attacker, 2 = defender pending

	// =========================================================
	// Constructor
	public L2SiegeClan(int clanId, int typeId)
	{
	    _ClanId = clanId;
        _TypeId = typeId;
	}

	// =========================================================
	// Method - Public
    public void addFlag(L2NpcInstance flag)
    {
        _Flag.add(flag);
    }

	public boolean removeFlag(L2NpcInstance flag)
	{
	    if (flag == null)
	        return false;

	    for (int i = 0; i < _Flag.size(); i++)
	    {
	        if (_Flag.get(i).getObjectId() == flag.getObjectId())
	        {
		        flag.deleteMe();
	            _Flag.remove(i);
	            return true;
	        }
	    }
	    
	    return false;
	}

	public void removeFlags()
	{
	    for (int i = 0; i < _Flag.size(); i++)
	    {
	        L2NpcInstance flag = _Flag.get(i);
	        if (flag != null) {flag.deleteMe();}
            _Flag.remove(i);
	    }
        _Flag.clear();
	}

   	// =========================================================
   	// Proeprty
	public final int getClanId()
	{
	    return _ClanId;
	}

	public final List<L2NpcInstance> getFlag()
	{
        if (_Flag == null) _Flag  = new FastList<L2NpcInstance>();
	    return _Flag;
	}

    public final int getTypeId() { return _TypeId; }
}
