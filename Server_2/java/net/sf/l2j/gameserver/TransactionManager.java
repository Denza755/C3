/*
 * $Header: net.sf.l2j.gameserver/TransactionManager.java,v 1.1 12/03/2005 15:49:00 luisantonioa Exp $
 *
 * $Author: luisantonioa $
 * $Date: 12/03/2005 15:49:00 $
 * $Revision: 1.1 $
 * $Log: TransactionManager.java $
 * Revision 1.1  12/03/2005 15:49:00  luisantonioa
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
package net.sf.l2j.gameserver;

import java.util.Collections;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.Transaction;
import net.sf.l2j.gameserver.model.Transactor;
import net.sf.l2j.gameserver.model.Transaction.TransactorState;

/**
 * TransactionManager
 * 
 * Handles all trasaction in the game world related to L2ItemInstances.
 * Whether they be in the 
 * 
 * @version $Revision: 1.2 $ $Date: 2004/06/27 08:12:59 $
 */

public class TransactionManager
{
    private static TransactionManager singleton;
    
    private static Connection connection;
    private static int transactionCounter;

    private Hashtable<Integer, Transaction> transactionTable;
    private Set<Transactor> negotiatingTradeRequest;

    private Hashtable<Object, Transactor> transactorTable;
    
    private TransactionManager()
    {
        transactionTable            = new Hashtable<Integer, Transaction>();
        transactorTable             = new Hashtable<Object, Transactor>();
        negotiatingTradeRequest     = new HashSet<Transactor>();
    }
    
    public static TransactionManager getInstance()
    {
        if (singleton == null)
        {
            singleton   = new TransactionManager();
        }
        return singleton;
    }
    
    
    
    public Transaction getTransaction(int transactionID)
    {
        return transactionTable.get(transactionID);
    }
    
    private boolean validTransactor(Object transactor, int transactionID)
    {
        Transaction transaction = getTransaction(transactionID);
        return transaction.getState(retreiveTransactor(transactor)) != null;
    }
    
    private boolean isInTradeRequest(Object transactor)
    {
        return negotiatingTradeRequest.contains(retreiveTransactor(transactor));
    }
    
    public Object getTradeRequestor(Object requestee)
    {
        Transactor transactee   = retreiveTransactor(requestee);
        return transactee.getTradeRequestor().getObject();
    }
    
    public void requestTrade(Object objectTransactor, Object objectTransactee)
    {
        Transactor transactor   = retreiveTransactor(objectTransactor);
        Transactor transactee   = retreiveTransactor(objectTransactee);
        
        if (isInTradeRequest(transactor) || isInTradeRequest(transactee))
        {
            //Currently In A Trade Request
            return;
        }
        
        Transaction newTransaction  = new Transaction(transactor, transactee);
        int transactionID           = newTransaction.getTransactionID();
        
        //System.out.println("Putting new Transaction (ID: "+transactionID+") into table");

        negotiatingTradeRequest.addAll(newTransaction.getParticipants());
        transactionTable.put(transactionID, newTransaction);
    }
    
    public void acceptTradeRequest(Object objectTransactee, int transactionID)
    {
        Transactor transactee   = retreiveTransactor(objectTransactee);
        
        Transaction transaction    = getTransaction(transactionID);
        
        //System.out.println("Transacee "+transactee.getName()+" attempting to accept trade Request.");
        
        if (transaction.getState(transactee) == TransactorState.DECIDING)
        {
            transaction.acceptedRequest(transactee);
            
            //System.out.println("Trade Rquest Attempt successfull.");
        }

        negotiatingTradeRequest.removeAll(transaction.getParticipants());
    }
    
    public void cancelTrade(Object objectTransactor, int transactionID)
    {
        Transactor transactor   = retreiveTransactor(objectTransactor);
        
        if (validTransactor(transactor, transactionID))
        {
            Transaction transaction = transactionTable.remove(transactionID);
            transaction.cancel();

            negotiatingTradeRequest.removeAll(transaction.getParticipants());
            
            //System.out.println("Transaction ID: "+transactionID+" cancelled.");
        }
    }

    public void rejectTradeRequest(Object transactor1, Object transactor2)
    {
        Transactor transactor   = retreiveTransactor(transactor1);
        Transactor transactee   = retreiveTransactor(transactor2);
        
        int transactionID       = transactor.getTransactionID(transactee);
        if (transactionID >= 0)
        {
            Transaction transaction = transactionTable.remove(transactionID);
            transaction.cancel();

            negotiatingTradeRequest.removeAll(transaction.getParticipants());
        }
        
        transactor.clearTradeRequestor();
        transactee.clearTradeRequestor();
        
    }
    
    public void removeTransactor(Object objectTransactor)
    {
        Transactor transactor   = retreiveTransactor(objectTransactor);
        
        //for (int transactionID)
    }
    
    public void addItemToTrade(Object objectTransactor, L2ItemInstance item, int transactionID)
    {
        Transactor transactor   = retreiveTransactor(objectTransactor);
        getTransaction(transactionID).addItemListToTrade(transactor, Collections.nCopies(1, item));
    }
    
    public void addItemListToTrade(Object objectTransactor, List<L2ItemInstance> itemList, int transactionID)
    {
        Transactor transactor   = retreiveTransactor(objectTransactor);
        getTransaction(transactionID).addItemListToTrade(transactor, itemList);
    }

    public void confirmTrade(Object objectTransactor, int transactionID)
    {
        Transactor transactor   = retreiveTransactor(objectTransactor);
        Transaction transaction = getTransaction(transactionID);
        transaction.confirmTrade(transactor);
        //System.out.println("Trasactor "+transactor.getName()+" confirmed trade.");
        if (transaction.transactionConfirmed())
        {
            //System.out.println("Both sides confirmed to Transfers.");
            doTransfer(transaction);
        }
    }
    
    public List<L2ItemInstance> retreiveTradeList(Object objectTransactor, int transactionID)
    {
        Transactor transactor   = retreiveTransactor(objectTransactor);
        Transaction transaction = getTransaction(transactionID);
        if (transaction.transactionConfirmed())
        {
            if (transaction.isTransactionFinished())
            {
                transactionTable.remove(transactionID);
            }
            return transaction.getTradeList(transactor);
        }
        return null;
    }
    
    private void doTransfer(Transaction transaction)
    {
        Transactor transactor   = transaction.getTransactor();
        Transactor transactee   = transaction.getTransactee();
        
        List<L2ItemInstance> temporaryList  = transaction.getTradeList(transactor);
        
        transactor.setTradeList(transaction.getTradeList(transactee));
        transactee.setTradeList(temporaryList);
    }
    
    public static int getNewTransactionID()
    {
        int transactionID   = transactionCounter;
        if (transactionCounter == Integer.MAX_VALUE)
        {
            transactionCounter  = 1;
        }
        else
        {
            transactionCounter++;
        }
        return transactionID;
    }

    public int getTransactionsInProgress(Object objectTransactor)
    {
        Transactor transactor   = retreiveTransactor(objectTransactor);
        return (transactor == null) ? 0 : transactor.getTransactionsInProgress();
    }
    
    public Transactor retreiveTransactor(Object object)
    {
        Transactor transactor   = transactorTable.get(object);
        
        if (transactor == null)
        {
            transactor  = new Transactor(object);
            transactorTable.put(object, transactor);
        }
        
        return transactor;
    }
}
