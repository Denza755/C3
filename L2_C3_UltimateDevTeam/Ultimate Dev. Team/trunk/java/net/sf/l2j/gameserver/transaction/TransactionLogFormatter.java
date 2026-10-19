/*
 * $Header: TransactionLogFormatter.java, 20/09/2005 01:25:53 luisantonioa Exp $
 *
 * $Author: luisantonioa $
 * $Date: 20/09/2005 01:25:53 $
 * $Revision: 1 $
 * $Log: TransactionLogFormatter.java,v $
 * Revision 1  20/09/2005 01:25:53  luisantonioa
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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
 * This class ...
 * 
 * @version $Revision: 1.2 $ $Date: 2004/06/27 08:12:59 $
 */

public class TransactionLogFormatter extends Formatter
{
    //private static final String _ = ": ";
    private static final String CRLF = "\r\n";
    
    private SimpleDateFormat dateFmt = new SimpleDateFormat("dd MMM H:mm:ss");
    
    /* (non-Javadoc)
     * @see java.util.logging.Formatter#format(java.util.logging.LogRecord)
     */
    @Override
    public String format(LogRecord record)
    {
        Object[] params = record.getParameters();
        StringBuffer output = new StringBuffer();
        output.append('[');
        output.append(dateFmt.format(new Date(record.getMillis())));
        output.append(']');
        output.append(' ');
        if (params != null) {
            for (Object p : params) {
                output.append(p);
                output.append(' ');
            }
        }
        output.append(record.getMessage());
        output.append(CRLF);

        return output.toString();
    }

}
