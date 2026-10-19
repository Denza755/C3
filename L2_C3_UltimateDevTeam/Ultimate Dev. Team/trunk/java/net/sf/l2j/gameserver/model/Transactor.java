/*
 * $Header: net.sf.l2j.gameserver.model/Transactor.java,v 1.1 13/03/2005 14:26:00 luisantonioa Exp $
 *
 * $Author: luisantonioa $
 * $Date: 13/03/2005 14:26:00 $
 * $Revision: 1.1 $
 * $Log: Transactor.java $
 * Revision 1.1  13/03/2005 14:26:00  luisantonioa
 * Created New Class 
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
package net.sf.l2j.gameserver.model;

import javolution.util.FastList;
import javolution.util.FastMap;
import net.sf.l2j.gameserver.tables.ItemTable;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * This class ...
 * 
 * @version $Revision: 1.2 $ $Date: 2004/06/27 08:12:59 $
 */
public class Transactor
{
    private int id;
    private String name;
    private Object object;
    
    private Transactor tradeRequestor;
    private FastMap<Transactor, Integer> currentTransactions;
    private FastMap<Integer,L2ItemInstance> tradeList;
    
    public Transactor(Object transactor, int transactorID, String newName)
    {
        this.id = transactorID;
        name = newName;
        object = transactor;

        tradeList               = new FastMap<Integer,L2ItemInstance>().setShared(true);
        currentTransactions     = new FastMap<Transactor, Integer>().setShared(true);
    }
    
    public Set<Transactor> getTransactingPartners()
    {
        return currentTransactions.keySet();
    }
    
    public Collection<Integer> getTransactionIds()
    {
        return currentTransactions.values();
    }
    
    public Object getObject()
    {
        return object;
    }
    
    public Transactor(Object transactor)
    {
        this(transactor, getTransactorID(transactor), getTransactorName(transactor));
    }
    
    public Transactor getTradeRequestor()
    {
        return tradeRequestor;
    }
    
    public synchronized void clearTradeRequestor()
    {
        tradeRequestor   = null;
    }
    
    public synchronized void setTradeRequestor(Transactor requestor)
    {
        tradeRequestor  = requestor;
    }
    
    public static int getTransactorID(Object object)
    {
        if (object instanceof L2Object)
        {
            return ((L2Object)object).getObjectId();
        }
        return -1;
    }
    
    public static String getTransactorName(Object object)
    {
        if (object instanceof L2Character)
        {
            return "" + ((L2Character)object).getName();
        }
        return "NoName";
    }
    
    public Collection<L2ItemInstance> getTradeList()
    {
        return tradeList.unmodifiable().values();
    }

    public synchronized void setTradeList(FastMap<Integer,L2ItemInstance> tradeList)
    {
        this.tradeList = tradeList.setShared(true);
    }

    public int getID()
    {
        return id;
    }

    public String getName()
    {
        return name;
    }

    public synchronized void addItem(L2ItemInstance newItem)
    {
        
        if (newItem.isStackable())
        {
            for (L2ItemInstance item : tradeList.values())
            {
                if (item.getItemId() == newItem.getItemId())
                {
                    int newAmount   = item.getCount() + newItem.getCount();
                    item.setCount(newAmount);
                    newItem.setCount(0);
                    return;
                }
            }
        }
        
        tradeList.put(newItem.getObjectId(),newItem);
    }

    public synchronized L2ItemInstance removeItem(int itemObjectID)
    {
        return tradeList.remove(itemObjectID);
    }

    public synchronized L2ItemInstance removeItem(int itemObjectID, int amountToRemove)
    {
        L2ItemInstance item = tradeList.get(itemObjectID);
        
        if (item == null)
            return null;
        
        int amountOfItem = item.getCount();
        
        if (amountToRemove > 0 && item.isStackable())
        {
            if (amountOfItem > amountToRemove)
            {
                //Item modified
                item.setCount(amountOfItem - amountToRemove);
                L2ItemInstance newItem = ItemTable.getInstance().createItem(item.getItemId());
                newItem.setCount(amountToRemove);
                
                return newItem;
            }
            else if (amountOfItem == amountToRemove)
            {
                return tradeList.remove(item.getObjectId());
            }
        }
        else
        {
            return tradeList.remove(item);
        }
        
        return null;
    }

    public synchronized void addNewTransaction(Transactor transactee, int transactionID)
    {
        currentTransactions.put(transactee, transactionID);
    }

    public int getTransactionID(Transactor transactee)
    {
        if (currentTransactions.get(transactee) == null)
            return -1;
        return currentTransactions.get(transactee);
    }

    public synchronized void cancelTransaction(Transactor transactor)
    {
        currentTransactions.remove(transactor);
    }

    public synchronized void addItemList(List<L2ItemInstance> itemList)
    {
        for (L2ItemInstance newItem : itemList)
            addItem(newItem);
    }

    public synchronized List<L2ItemInstance> removeItemList(FastMap<Integer, Integer> itemList)
    {
        List<L2ItemInstance> removedItems = new FastList<L2ItemInstance>();
        
        for (int itemObjectId : itemList.values())
        {
            L2ItemInstance removedItem  = removeItem(itemObjectId, itemList.get(itemObjectId));
            if (removedItem != null)
                removedItems.add(removedItem);
        }
        
        if (removedItems.size() <= 0)
            return null;
        
        return removedItems;
    }

    public int getTransactionsInProgress()
    {
        return currentTransactions.size();
    }

}
