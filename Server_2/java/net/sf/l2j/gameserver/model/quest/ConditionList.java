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
package net.sf.l2j.gameserver.model.quest;

import java.util.ArrayList;

/**
 * @author Luis Arias
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class ConditionList
{

    private ArrayList<Condition> list;
    
    public ConditionList()
    {
        list = new ArrayList<Condition>();
    }

    /**
     * @return Returns the list.
     */
    public ArrayList getConditionList()
    {
        return list;
    }
    /**
     * @param list The list to set.
     */
    public void setConditionList(ArrayList<Condition> list)
    {
        this.list = list;
    }
    /**
     * @param list The list to set.
     */
    public void addCondition(Condition condition)
    {
        list.add(condition);
    }
    /**
     * @param list The list to set.
     */
    public void removeCondition(Condition condition)
    {
        list.remove(condition);
    }
}
