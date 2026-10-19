/*
 * $Header: Transactor.java, 20/09/2005 01:11:57 luisantonioa Exp $
 *
 * $Author: luisantonioa $
 * $Date: 20/09/2005 01:11:57 $
 * $Revision: 1 $
 * $Log: Transactor.java,v $
 * Revision 1  20/09/2005 01:11:57  luisantonioa
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

import javolution.util.FastList;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PetInstance;
import net.sf.l2j.gameserver.templates.L2CharTemplate;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * This class ...
 * 
 * @version $Revision: 1.2 $ $Date: 2004/06/27 08:12:59 $
 */

public class Transactor
{
    private AtomicBoolean           inTransaction;
    private L2Object                owner;
    private Long                    currentTransactionID;
    private String                  name;
    private Transactor              partner;
    @SuppressWarnings("unused")
    private Boolean                 initiatedTransaction;
    private List<TransactionItem>   itemList;
    private AtomicReference<Stage>  stage;
    
    public enum Stage
    {
        PENDING,
        INTIATED,
        ACCEPTED,
        CONFIRMED,
        COMPLETED,
        AVALIABLE
    }
    
    public Transactor(L2Object transactorOwner)
    {
        this.owner              = transactorOwner;
        inTransaction           = new AtomicBoolean(false);
        reset();
        setTransactorName();
    }
    
    public boolean initiate(long transactionID, Transactor transactionPartner, boolean initiatedTransaction)
    {
        if (inTransaction.compareAndSet(false, true))
        {
            currentTransactionID        = transactionID;
            this.partner                = transactionPartner;
            this.initiatedTransaction   = initiatedTransaction;
            stage                       = new AtomicReference<Stage>(Stage.PENDING);
            itemList                    = null;
            return true;
        }
        
        return false;
    }

    public boolean cancelTransaction()
    {
        if (inTransaction.compareAndSet(true, false))
        {
            if (itemList != null && itemList.size() > 0)
            {
                //TODO Need to return reserved items
            }
            reset();
            return true;
        }
        
        return false;
    }

    /**
     * 
     */
    private void reset()
    {
        currentTransactionID    = null;
        partner                 = null;
        initiatedTransaction    = null;
        stage.set(Stage.AVALIABLE);
        //itemList                = null;
    }
    
    public String getName()
    {
        return name;
    }
    
    private void setTransactorName()
    {
        name = "";
        
        if (owner == null)
        {
            name    += "[Ground]";
        }
        else if (owner instanceof L2Character)
        {
            L2Character character = (L2Character) owner;
            name    += "[" + character.getClass().getName() + " ";
            if (character instanceof L2PetInstance ||
                character instanceof L2PcInstance)
            {
                name    +=  character.getName();
            }
            else
            {
                L2CharTemplate template = character.getTemplate();
                if (template instanceof L2NpcTemplate)
                {
                    name    += ((L2NpcTemplate)template).name;
                }
                else
                {
                    name    += "Unknown";
                }
            }
            name    += "]";
        }
        else
        {
            name = "[Unknown]";
        }
    }
    
    public boolean isTradingPartner(Transactor partner)
    {
        return this.partner != null && getTransactorID() == partner.getTransactorID();
    }
    
    public int getTransactorID()
    {
        return owner.getObjectId();
    }

    public Transactor getPartner()
    {
        return partner;
    }

    public String getPartnerName()
    {
        if (partner != null)
        {
            return partner.getName();
        }
        return "No Partner";
    }

    public void initiated()
    {
        if (stage.compareAndSet(Stage.PENDING, Stage.INTIATED))
        {
            
        }
    }

    public void requestAcceptance()
    {
        // TODO request owner for trade acceptance
        if (stage.get() == Stage.INTIATED)
        {
            
        }
    }
    
    public Long getTransactionID()
    {
        return currentTransactionID;
    }

    public void accept()
    {
        if (stage.compareAndSet(Stage.INTIATED, Stage.ACCEPTED))
        {
            itemList    = new FastList<TransactionItem>();
        }
    }

    public void denied()
    {
        //TODO send denied packet to owner
        if (stage.get() == Stage.INTIATED)
        {
            
        }
    }

    public void confirm()
    {
        if (stage.compareAndSet(Stage.ACCEPTED, Stage.CONFIRMED))
        {
            if (getPartner().hasConfirmed())
            {
                TransactionManager.doSwap(this);
            }
        }
    }

    public boolean hasConfirmed()
    {
        return stage.get() == Stage.CONFIRMED;
    }

    public void uncomfirm()
    {
        if (stage.compareAndSet(Stage.CONFIRMED, Stage.ACCEPTED))
        {
            
        }
    }

    public List<TransactionItem> getItemList()
    {
        return itemList;
    }

    public void setItemList(List<TransactionItem> itemList)
    {
        if (stage.get() == Stage.CONFIRMED)
        {
            this.itemList = itemList;
        }
    }

    public void addToItemList(List<TransactionItem> itemList)
    {
        if (stage.get() == Stage.ACCEPTED)
        {
            this.itemList.addAll(itemList);
        }
    }

    public void addToItemList(TransactionItem item)
    {
        if (stage.get() == Stage.ACCEPTED)
        {
            this.itemList.add(item);
        }
    }

    public void updateInventory()
    {
        // TODO Update inventory with itemList
        
    }
    
    public Stage getStage()
    {
        return stage.get();
    }

    public void complete()
    {
        if (inTransaction.compareAndSet(true, false))
        {
            reset();
        }
    }
}
