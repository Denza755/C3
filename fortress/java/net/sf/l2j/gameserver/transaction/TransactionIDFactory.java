/*
 * $Header: TransactionIDFactory.java, 20/09/2005 01:48:04 luisantonioa Exp $
 *
 * $Author: luisantonioa $
 * $Date: 20/09/2005 01:48:04 $
 * $Revision: 1 $
 * $Log: TransactionIDFactory.java,v $
 * Revision 1  20/09/2005 01:48:04  luisantonioa
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

public class TransactionIDFactory
{
    private static long transactionID = 1;
    
    public static synchronized long getNextID()
    {
        long id = transactionID;
        transactionID++;
        
        if (transactionID == Long.MAX_VALUE)
        {
            transactionID   = 1;
        }
        
        return id;
    }
}
