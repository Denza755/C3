/*
 * $Header: Util.java, 21/10/2005 23:17:40 luisantonioa Exp $
 *
 * $Author: luisantonioa $
 * $Date: 21/10/2005 23:17:40 $
 * $Revision: 1 $
 * $Log: Util.java,v $
 * Revision 1  21/10/2005 23:17:40  luisantonioa
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
package net.sf.l2j.gameserver.util;

import java.io.File;
import java.util.logging.Logger;

import net.sf.l2j.gameserver.ClientScheduler;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2PcInstance;


/**
 * General Utility functions related to Gameserver
 * 
 * @version $Revision: 1.2 $ $Date: 2004/06/27 08:12:59 $
 */

public final class Util
{
    public static void handleIllegalPlayerAction(L2PcInstance actor, String message, Logger log, int punishment)
    {
        ClientScheduler.scheduleEvent(new IllegalPlayerAction(actor,message, log, punishment), 500);
    }
    
    public static String getRelativePath(File base,File file)
    {
        return file.toURI().getPath().substring(base.toURI().getPath().length());
    }

    /** Return degree value of object 2 to the horizontal line with object 1 being the origin */
    public static double calculateAngleFrom(L2Object obj1, L2Object obj2) { return calculateAngleFrom(obj1.getX(), obj1.getY(), obj2.getX(), obj2.getY()); }

    /** Return degree value of object 2 to the horizontal line with object 1 being the origin */
    public static double calculateAngleFrom(int obj1X, int obj1Y, int obj2X, int obj2Y)
    {
        double angleTarget = Math.toDegrees(Math.atan2(obj1Y - obj2Y, obj1X - obj2X));
        if (angleTarget <= 0) angleTarget += 360;
        return angleTarget;
    }
    
    public static boolean checkIfInRange(int range, int x1, int y1, int x2, int y2) { return checkIfInRange(range, x1, y1, 0, x2, y2, 0, false); }

    public static boolean checkIfInRange(int range, int x1, int y1, int z1, int x2, int y2, int z2, boolean includeZAxis)
    {
        int dx = x1 - x2;
        int dy = y1 - y2;
        
        if (includeZAxis)
        {
            int dz = z1 - z2;
            return ((dx*dx) + (dy*dy) + (dz*dz) <= (range*range));
        }
        else
            return ((dx*dx) + (dy*dy) <= (range*range));
    }

    public static boolean checkIfInRange(int range, L2Object obj1, L2Object obj2, boolean includeZAxis)
    {
        if (obj1 == null || obj2 == null) return false;
        return checkIfInRange(range, obj1.getX(), obj1.getY(), obj1.getZ(), obj2.getX(), obj2.getY(), obj2.getZ(), includeZAxis);
    }

    public static double convertHeadingToDegree(int heading)
    {
        double angle = 360.0 * (heading / 64400.0);
        if (angle == 0) angle = 360;
        return angle;
    }
	
	public static double calculateDistance(int x1, int y1, int z1, int x2, int y2) { return calculateDistance(x1, y1, 0, x2, y2, 0, false); }

    public static double calculateDistance(int x1, int y1, int z1, int x2, int y2, int z2, boolean includeZAxis)
    {
        double dx = x1 - x2;
        double dy = y1 - y2;

        if (includeZAxis)
        {
            int dz = z1 - z2;
            return Math.sqrt((dx*dx) + (dy*dy) + (dz*dz));
        }
        else
            return Math.sqrt((dx*dx) + (dy*dy));
    }

    public static double calculateDistance(L2Object obj1, L2Object obj2, boolean includeZAxis)
    {
        if (obj1 == null || obj2 == null) return 1000000;
        return calculateDistance(obj1.getX(), obj1.getY(), obj1.getZ(), obj2.getX(), obj2.getY(), obj2.getZ(), includeZAxis);
    }
}
