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

import java.util.HashMap;

/**
 * @author Luis Arias
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class ConditionInventory extends Condition
{

    public static final String INV_ITEM_ID			= "ItemID";
    public static final String INV_COUNT			= "Count";
    public static final String INV_OPERATION		= "Operation";
    
    public static final String OP_EQ_EQ			= "==";
    public static final String OP_NOT_EQ		= "!=";
    public static final String OP_GT_EQ			= ">=";
    public static final String OP_GT			= ">";
    public static final String OP_LT_EQ			= "<=";
    public static final String OP_LT			= "<";
    
    /**
     * @param type
     * @param attributes
     */
    public ConditionInventory(HashMap<Object, Object> attributes)
    {
        super(attributes);
        
    }

    /* (non-Javadoc)
     * @see net.sf.l2j.gameserver.model.quest.Condition#checkCondition()
     */
    public void checkCondition()
    {
        if (validCondition())
        {
            
        }
    }
    
    public boolean validCondition()
    {
        return (attributes.get(INV_ITEM_ID) != null
                && attributes.get(INV_COUNT) != null
                && attributes.get(INV_OPERATION) != null);
    }
    
    public int getItemID()
    {
        return Integer.parseInt((String)attributes.get(INV_ITEM_ID));
    }
    
    public int getCount()
    {
        return Integer.parseInt((String)attributes.get(INV_COUNT));
    }
    
    public String getOP()
    {
        return (String)attributes.get(INV_OPERATION);
    }

}
