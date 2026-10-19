/* This program is free software; you can redistribute it and/or modify
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

import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.packets.serverpackets.RadarControl;

import java.util.Vector;

/**
 * @author dalrond
 */
public final class L2Radar
{

    private L2PcInstance        player;
    private Vector<RadarMarker> markers;

    public L2Radar(L2PcInstance player)
    {
        this.player = player;
        markers = new Vector<RadarMarker>();
    }

    // Add a marker to player's radar
    public void addMarker(int x, int y, int z)
    {
        markers.add(new RadarMarker(x, y, z));
        player.sendPacket(new RadarControl(0, 1, x, y, z));
    }

    // Remove a marker from player's radar
    public void removeMarker(int x, int y, int z)
    {
        markers.remove(new RadarMarker(x, y, z));
        player.sendPacket(new RadarControl(1, 1, x, y, z));
    }

    public void removeAllMarkers()
    {
        // TODO: Need method to remove all markers from radar at once
        for (RadarMarker tempMarker : markers)
        {
            player.sendPacket(new RadarControl(1, tempMarker.type, tempMarker.x, tempMarker.y, tempMarker.z));
        }
        markers.removeAllElements();
    }

    public void loadMarkers()
    {
        // TODO: Need method to re-send radar markers after load/teleport/death
        // etc.
    }

    public class RadarMarker
    {
        // Simple class to model radar points.
        public int type, x, y, z;

        public RadarMarker(int type, int x, int y, int z)
        {
            this.type = type;
            this.x = x;
            this.y = y;
            this.z = z;
        }

        public RadarMarker(int x, int y, int z)
        {
            this.type = 1;
            this.x = x;
            this.y = y;
            this.z = z;
        }

        public boolean equals(Object obj)
        {
            try
            {
                RadarMarker temp = (RadarMarker) obj;
                if ((temp.x == this.x) && (temp.y == this.y) && (temp.z == this.z) && (temp.type == this.type))
                {
                    return true;
                }
                return false;
            }
            catch (Exception e)
            {
                return false;
            }
        }
    }
}