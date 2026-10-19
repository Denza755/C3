/*
 * $Header: TransactionItem.java, 20/09/2005 01:09:26 luisantonioa Exp $
 *
 * $Author: luisantonioa $
 * $Date: 20/09/2005 01:09:26 $
 * $Revision: 1 $
 * $Log: TransactionItem.java,v $
 * Revision 1  20/09/2005 01:09:26  luisantonioa
 * Added copyright notice
 *
 * 
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
package net.sf.l2j.gameserver.transaction;


/**
 * This class ...
 * 
 * @version $Revision: 1.2 $ $Date: 2004/06/27 08:12:59 $
 */

public class TransactionItem
{
    private int objectID;
    private int stackCount;
    
    public TransactionItem(int objectID)
    {
        this.objectID   = objectID;
        stackCount      = 1;
    }
    
    public TransactionItem(int objectID, int amount)
    {
        this.objectID   = objectID;
        stackCount      = amount;
    }
    
    public int getObjectID()
    {
        return objectID;
    }
    public void setObjectID(int objectID)
    {
        this.objectID = objectID;
    }
    public int getStackCount()
    {
        return stackCount;
    }
    public void setStackCount(int stackCount)
    {
        this.stackCount = stackCount;
    }
    
    public String toString()
    {
        return "("+objectID+", "+stackCount+")";
    }
}
