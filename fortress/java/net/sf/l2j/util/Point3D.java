/*
 * $Header: Point3D.java, 19/07/2005 21:33:07 luisantonioa Exp $
 *
 * $Author: luisantonioa $
 * $Date: 19/07/2005 21:33:07 $
 * $Revision: 1 $
 * $Log: Point3D.java,v $
 * Revision 1  19/07/2005 21:33:07  luisantonioa
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

import java.io.Serializable;

import net.sf.l2j.gameserver.model.waypoint.WayPointNode;

/**
 * This class ...
 * 
 * @version $Revision: 1.2 $ $Date: 2004/06/27 08:12:59 $
 */
public class Point3D implements Serializable
{
    /**
     * Comment for <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = 4638345252031872576L;
    public int x, y, z;
    public Point3D(int x, int y, int z)
    {
        this.x  = x;
        this.y  = y;
        this.z  = z;
    }
    
    public static Point3D getPosition(WayPointNode node)
    {
        return new Point3D(node.getX(), node.getY(), node.getZ());
    }
    
    public String toString()
    {
        return "("+x+", "+y+", "+z+")";
    }
    
    public int hashCode()
    {
        return (x+y+z)%Integer.MAX_VALUE;
    }
    
    public boolean equals(Object o)
    {
        if (o instanceof Point3D)
        {
            Point3D point3D = (Point3D) o;
            return
            point3D.x == x &&
            point3D.y == y &&
            point3D.z == z;
        }
        return false;
    }
}
