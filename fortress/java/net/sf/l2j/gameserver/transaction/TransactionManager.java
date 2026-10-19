/*
 * $Header: TransactionManager.java, 20/09/2005 01:00:26 luisantonioa Exp $
 *
 * $Author: luisantonioa $
 * $Date: 20/09/2005 01:00:26 $
 * $Revision: 1 $
 * $Log: TransactionManager.java,v $
 * Revision 1  20/09/2005 01:00:26  luisantonioa
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

import java.util.List;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * This class ...
 * 
 * @version $Revision: 1.2 $ $Date: 2004/06/27 08:12:59 $
 */

public class TransactionManager
{
    public static final     String  LOGGER_NAME     = "Transaction";
    private static final    boolean INITIATOR       = true;
    protected static        Logger  _logTransaction = Logger.getLogger(LOGGER_NAME);
    
    public static void commence(Transactor requestor, Transactor requestee)
    {
        long transactionID  = TransactionIDFactory.getNextID();
        if (requestor.initiate(transactionID, requestee, INITIATOR))
        {
            if (requestee.initiate(transactionID, requestor, !INITIATOR))
            {
                //set transactor and transactee in transaction mode and send request for trade acceptance to requestee
                requestor.initiated();
                requestee.initiated();
                
                requestee.requestAcceptance();
            }
            else
            {
                logRecord(
                          Level.INFO, 
                          "Requestee is already in a transaction, cannot start new Transaction.",
                          transactionID,
                          requestor.getName(),
                          requestee.getName()
                          );
                
                cancel(requestor);
            }
        }
        else
        {
            logRecord(
                      Level.INFO, 
                      "Requestor is already in a transaction, cannot start new Transaction.",
                      transactionID,
                      requestor.getName(),
                      requestee.getName()
                      );
        }
    }
    
    public static void accept(Transactor requestee)
    {
        requestee.accept();
        requestee.getPartner().accept();
    }
    
    public static void decline(Transactor requestee)
    {
        requestee.getPartner().denied();
        cancelEntireTransaction(requestee);
    }
    
    public static void cancelEntireTransaction(Transactor transactor)
    {
        Transactor transactee       = transactor.getPartner();
        boolean transacteeCancelled = transactee.cancelTransaction();
        boolean transactorCancelled = transactor.cancelTransaction();
        
        if (transactorCancelled && transacteeCancelled)
        {
            logRecord(
                      Level.INFO, 
                      "Transaction cancelled.",
                      transactor.getName(),
                      transactee.getName()
                      );
        }
        else
        {
            logRecord(
                      Level.INFO, 
                      "Transaction cancelled incorrectly.",
                      transactor.getName(),
                      transactee.getName()
                      );
        }
    }
    
    public static void cancel(Transactor transactor)
    {
        if (transactor.cancelTransaction())
        {
            logRecord(
                      Level.INFO, 
                      "Transaction cancelled.",
                      transactor.getName()
                      );
        }
        else
        {
            logRecord(
                      Level.INFO, 
                      "No transaction to cancel.",
                      transactor.getName()
                      );
        }
    }
    
    public static void confirm(Transactor transactor)
    {
        transactor.confirm();
    }
    
    public static void complete(Transactor transactor, Transactor transactee)
    {
        transactor.complete();
        transactee.complete();
        logRecord(
                  Level.INFO, 
                  "Transaction completed.",
                  transactor.getName(),
                  transactee.getName()
                  );
    }
    
    public static void doSwap(Transactor transactor)
    {
        Transactor transactee   = transactor.getPartner();
        
        if (!transactor.hasConfirmed() || !transactee.hasConfirmed())
        {
            logRecord(
                      Level.INFO, 
                      "Transaction swap incomplete both sides have not confirmed trade.",
                      transactor.getName(),
                      transactee.getName()
                      );
            return;
        }
        
        List<TransactionItem> transactorList    = transactor.getItemList();
        List<TransactionItem> transacteeList    = transactee.getItemList();
        
        transactor.setItemList(transacteeList);
        transactee.setItemList(transactorList);
        
        transactor.updateInventory();
        transactee.updateInventory();
        
        complete(transactor, transactee);
    }
    
    public static void addItems(Transactor transactor, List<TransactionItem> itemsToAdd)
    {
        transactor.uncomfirm();
        transactor.getPartner().uncomfirm();
        
        transactor.addToItemList(itemsToAdd);
    }
    
    @SuppressWarnings("unused")
    public static void doImmediateSend(Transactor sender, Transactor receiver, List<TransactionItem> itemList, boolean forceSend)
    {
        
    }
    
    public static void logRecord(Level level, String message)
    {
        _logTransaction.log(new LogRecord(level, message));
    }
    
    public static void logRecord(Level level, String message, Object... parameters)
    {
        LogRecord record    = new LogRecord(level, message);
        record.setParameters(parameters);
        _logTransaction.log(record);
    }
    
    @SuppressWarnings("unused")
    private static String getTransactorsString(Transactor transactor)
    {
        return transactor.getName() + "," + transactor.getPartnerName();
    }
    
    @SuppressWarnings("unused")
    private static String getTransactorsString(Transactor transactor, Transactor transactee)
    {
        return transactor.getName() + "," + transactee.getName();
    }
}
