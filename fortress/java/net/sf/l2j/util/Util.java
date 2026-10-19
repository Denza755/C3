/*
 * $Header: Util.java, 14-Jul-2005 03:27:51 luisantonioa Exp $
 *
 * $Author: luisantonioa $
 * $Date: 14-Jul-2005 03:27:51 $
 * $Revision: 1 $
 * $Log: Util.java,v $
 * Revision 1  14-Jul-2005 03:27:51  luisantonioa
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
package net.sf.l2j.util;

/**
 * This class ...
 * 
 * @version $Revision: 1.2 $ $Date: 2004/06/27 08:12:59 $
 */

public class Util
{
    public static boolean isInternalIP(String ipAddress)
    {
        return (ipAddress.startsWith("192.168.") || 
                ipAddress.startsWith("10.") ||
				//ipAddress.startsWith("172.16.") || 
                //Removed because there are some net IPs in this range.
                //TODO: Use regexp or something to only include 172.16.0.0 => 172.16.31.255
                ipAddress.startsWith("127.0.0.1"));
    }
}
