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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

import net.sf.l2j.gameserver.ItemTable;

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
    //private TransactorType type;
    
    private List<L2ItemInstance> tradeList;
    
    private Transactor tradeRequestor;
    private Hashtable<Transactor, Integer> currentTransactions; 
    
    public Transactor(Object transactor, int transactorID, String newName)
    {
        this.id      = transactorID;
        name                    = newName;
        object                  = transactor;
        //type   = getTransactorType(transactor);

        tradeList               = new ArrayList<L2ItemInstance>();
        currentTransactions     = new Hashtable<Transactor, Integer>();
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
    
    public void clearTradeRequestor()
    {
        tradeRequestor   = null;
    }
    
    public void setTradeRequestor(Transactor requestor)
    {
        tradeRequestor  = requestor;
    }
    
    public static int getTransactorID(Object object)
    {
        if (object instanceof L2Object)
        {
            return ((L2Object)object).getObjectId();
        }
        else
        {
            return -1;
        }
    }
    
    public static String getTransactorName(Object object)
    {
        if (object instanceof L2Character)
        {
            return "" + ((L2Character)object).getName();
        }
        else
        {
            return "NoName";
        }
    }
    
    public List<L2ItemInstance> getTradeList()
    {
        return tradeList;
    }

    public void setTradeList(List<L2ItemInstance> tradeList)
    {
        this.tradeList = tradeList;
    }

    public int getID()
    {
        return id;
    }

    public String getName()
    {
        return name;
    }

    public void addItem(L2ItemInstance newItem)
    {
        synchronized (tradeList)
        {
            if (newItem.isStackable())
            {
                for (L2ItemInstance item : tradeList)
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
            
            tradeList.add(newItem);
        }
    }

    public L2ItemInstance removeItem(int itemObjectID)
    {
        return removeItem(itemObjectID, -1);
    }

    public L2ItemInstance removeItem(int itemObjectID, int amountToRemove)
    {
        synchronized (tradeList)
        {
            for (L2ItemInstance item : tradeList)
            {
                if (item.getObjectId() == itemObjectID)
                {
                    int amountOfItem    = item.getCount();
                    
                    if (amountToRemove > 0 &&
                            item.isStackable())
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
                            tradeList.remove(item);
                            return item;
                        }
                    }
                    else
                    {
                        tradeList.remove(item);
                        return item;
                    }
                }
            }
        }

        return null;
    }

    public void addNewTransaction(Transactor transactee, int transactionID)
    {
        currentTransactions.put(transactee, transactionID);
    }

    public int getTransactionID(Transactor transactee)
    {
        if (currentTransactions.get(transactee) == null)
            return -1;
        else
            return currentTransactions.get(transactee);
    }

    public void cancelTransaction(Transactor transactor)
    {
        currentTransactions.remove(transactor);
    }

    public void addItemList(List<L2ItemInstance> itemList)
    {
        //System.out.println("Adding Items To List."+itemList);
        synchronized (tradeList)
        {
            List<L2ItemInstance> tempList   = new ArrayList<L2ItemInstance>();
            for (L2ItemInstance newItem : itemList)
            {
                //System.out.println("Adding "+newItem+".");
                if (newItem.isStackable())
                {
                    //System.out.println("Item is Stackable at most one in list.");
                    for (L2ItemInstance item : tradeList)
                    {
                        //System.out.println(item.getItemId() + " " + newItem.getItemId());
                        if (item.getItemId() == newItem.getItemId())
                        {
                            int newAmount   = item.getCount() + newItem.getCount();
                            item.setCount(newAmount);
                            tempList.add(newItem);
                            //System.out.println("Same stackable found in List.");
                            //System.out.println("Old Count: "+item.getCount()+" + New Count: "+newItem.getCount()+".");
                            continue;
                        }
                    }
                }
                //System.out.println("New Item, adding to List.");
                tradeList.add(newItem);
            }
            tradeList.removeAll(tempList);
        }
        //System.out.println("Finished Adding Items.");
    }

    public List<L2ItemInstance> removeItemList(Hashtable<Integer, Integer> itemList)
    {
        List<L2ItemInstance> removedItems   = new ArrayList<L2ItemInstance>();
        Set<Integer> itemObjectIdList       = itemList.keySet();
        
        synchronized (itemObjectIdList)
        {
            for (int itemObjectId : itemObjectIdList)
            {
                L2ItemInstance removedItem  = removeItem(itemObjectId, itemList.get(itemObjectId));
                if (removedItem == null)
                {
                    return null;
                }
                removedItems.add(removedItem);
            }
        }
        
        if (removedItems.size() <= 0)
        {
            return null;
        }
        
        return removedItems;
    }

    public int getTransactionsInProgress()
    {
        return currentTransactions.size();
    }

}
