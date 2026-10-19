/*
 * $Header: net.sf.l2j.gameserver.model/Transaction.java,v 1.1 12/03/2005 22:45:43 luisantonioa Exp $
 *
 * $Author: luisantonioa $
 * $Date: 12/03/2005 22:45:43 $
 * $Revision: 1.1 $
 * $Log: Transaction.java $
 * Revision 1.1  12/03/2005 22:45:43  luisantonioa
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
import java.util.Date;
import java.util.List;

import net.sf.l2j.gameserver.TransactionManager;


/**
 * This class ...
 * 
 * @version $Revision: 1.2 $ $Date: 2004/06/27 08:12:59 $
 */

public class Transaction
{
    private int transactionID;
    private Date completed;
    private Transactor transactor;
    private TransactorState transactorState;
    private Transactor transactee;
    private TransactorState transacteeState;
    
    private ArrayList<Transactor> participants;

    public enum TransactorState
    {
        ACCEPTED,
        DECIDING,
        CONFIRMED,
        SWAPPING,
        RETREIVED,
    }
    
    public Transaction(Transactor transactor, Transactor transactee)
    {
        this.transactor         = transactor;
        this.transactorState    = TransactorState.ACCEPTED;
        this.transactee         = transactee;
        this.transacteeState    = TransactorState.DECIDING;
        
        transactee.setTradeRequestor(transactor);
        transactor.setTradeRequestor(transactee);
        
        participants            = new ArrayList<Transactor>();
        participants.add(transactor);
        participants.add(transactee);
        
        //System.out.println("Transactor "+transactor.getName()+" added to transaction");
        //System.out.println("Transacee "+transactee.getName()+" added to transaction");
        
        transactionID      = TransactionManager.getNewTransactionID();
        
        //System.out.println("Transaction given ID: "+transactionID);
        
        transactee.addNewTransaction(transactor, transactionID);
        transactor.addNewTransaction(transactee, transactionID);
    }
    
    public List<Transactor> getParticipants()
    {
        return participants;
    }
    
    public int getTransactionID()
    {
        return transactionID;
    }
    
    public TransactorState getState(Transactor transactor)
    {
        if (this.transactor == transactor)
            return transactorState;
        else if (this.transactee == transactor)
            return transacteeState;
        
        return null;
    }
    
    public Date getCompletedDate()
    {
        return completed;
    }
    
    public void setDateCompleted(Date date)
    {
        completed   = date;
    }
    
    public boolean equals(Object object)
    {
        return (object instanceof Transaction 
                && ((Transaction)object).transactionID == transactionID);
    }

    public void acceptedRequest(Transactor transactee)
    {
        transacteeState = TransactorState.ACCEPTED;
    }

    public void cancel()
    {
        transactee.cancelTransaction(transactor);
        transactor.cancelTransaction(transactee);
    }

    public void addItemListToTrade(Transactor transactor, List<L2ItemInstance> itemList)
    {
        if (this.transactor == transactor)
        {
            this.transactor.addItemList(itemList);
            if (transacteeState == TransactorState.CONFIRMED)
            {
                //System.out.println("Trades Changed need new confirmations.");
                transacteeState = TransactorState.ACCEPTED;
            }
        }
        else if (this.transactee == transactor)
        {
            this.transactee.addItemList(itemList);
            if (transactorState == TransactorState.CONFIRMED)
            {
                //System.out.println("Trades Changed need new confirmations.");
                transactorState = TransactorState.ACCEPTED;
            }
        }
    }

    public void confirmTrade(Transactor transactor)
    {
        if (this.transactor == transactor)
            transactorState = TransactorState.CONFIRMED;
        else if (this.transactee == transactor)
            transacteeState = TransactorState.CONFIRMED;
    }
    
    public boolean transactionConfirmed()
    {
        return (transactorState == TransactorState.CONFIRMED 
                || transactorState == TransactorState.SWAPPING
                || transactorState == TransactorState.RETREIVED)
                && (transacteeState == TransactorState.SWAPPING 
                        || transacteeState == TransactorState.CONFIRMED
                        || transacteeState == TransactorState.RETREIVED);
    }

    public List<L2ItemInstance> getTradeList(Transactor transactor)
    {
        if (this.transactor == transactor)
        {
            if (transactorState == TransactorState.CONFIRMED)
            {
                transactorState = TransactorState.SWAPPING;
                return transactor.getTradeList();
            }
            else if (transactorState == TransactorState.SWAPPING)
            {
                transactorState = TransactorState.RETREIVED;
                return transactor.getTradeList();
            }
        }
        else if (this.transactee == transactor)
        {
            if (transacteeState == TransactorState.CONFIRMED)
            {
                transacteeState = TransactorState.SWAPPING;
                return transactee.getTradeList();
            }
            else if (transacteeState == TransactorState.SWAPPING)
            {
                transacteeState = TransactorState.RETREIVED;
                return transactee.getTradeList();
            }
        }
        return null;
    }
    
    public boolean isTransactionFinished()
    {
        return transactorState == TransactorState.RETREIVED && transacteeState == TransactorState.RETREIVED;
    }

    public Transactor getTransactee()
    {
        return transactee;
    }
    

    public Transactor getTransactor()
    {
        return transactor;
    }

    public TransactorState getTransacteeState()
    {
        return transacteeState;
    }
    

    public TransactorState getTransactorState()
    {
        return transactorState;
    }
    
    
    
    
}
