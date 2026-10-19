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

import java.util.ArrayList;

/**
 * This class ...
 * 
 * @version $Revision: 1.1.2.1.2.2 $ $Date: 2005/03/27 15:29:33 $
 */
public class L2ManufactureList
{
    private ArrayList<L2ManufactureItem> _list;
    private boolean _confirmed;
    private String _manufactureStoreName;
    
    public L2ManufactureList()
    {
        _list = new ArrayList<L2ManufactureItem>();
        _confirmed = false;
    }
    
    public int size()
    {
        return _list.size();
    }
    
    public void setConfirmedTrade(boolean x)
    {
        _confirmed = x;
    }
    
    public boolean hasConfirmed()
    {
        return _confirmed;
    }

    /**
     * @param _manufactureStoreName The _manufactureStoreName to set.
     */
    public void setStoreName(String _manufactureStoreName)
    {
        this._manufactureStoreName = _manufactureStoreName;
    }

    /**
     * @return Returns the _manufactureStoreName.
     */
    public String getStoreName() 
    {
        return _manufactureStoreName;
    }
    
    public void add(L2ManufactureItem item)
    {
        _list.add(item);
    }
    
    public ArrayList<L2ManufactureItem> getList()
    {
        return _list;
    }
 
    public void setList(ArrayList<L2ManufactureItem> list)
    {
        _list = list;
    }
    
}
