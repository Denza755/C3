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

/**
 * @author Luis Arias
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class Transition
{

    private ConditionList conditionList;
    private State nextState;
    
    public Transition(Condition condition, State nextState)
    {
        conditionList = new ConditionList();
        nextState = null;
    }
    
    public Transition(ConditionList conditions, State nextState)
    {
        this.conditionList = conditions;
        this.nextState = nextState;
    }

    public ConditionList getConditionList()
    {
        return conditionList;
    }

    public void setConditionList(ConditionList conditionList)
    {
        this.conditionList = conditionList;
    }

    public void removeCondition(Condition condition)
    {
        conditionList.removeCondition(condition);
    }

    public void addCondition(Condition condition)
    {
        conditionList.addCondition(condition);
    }

    public State getNextState()
    {
        return nextState;
    }

    public void setNextState(State nextState)
    {
        this.nextState = nextState;
    }
}
