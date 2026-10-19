/*
 * $Header: EventData.java
 *
 * $Author: SarEvoK 
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

import java.util.LinkedList;

public class EventData
{
	public int eventX;
	public int eventY;
	public int eventZ;
	public int eventkarma;
	public int eventpvpkills;
	public int eventpkkills;
	public String eventTitle;
	public LinkedList<String> kills = new LinkedList<String>();
	public boolean eventSitForced = false;
	

	public EventData(int eventX, int eventY, int eventZ, int eventkarma, int eventpvpkills, int eventpkkills, String eventTitle, LinkedList<String> kills, boolean eventSitForced)
	{
		this.eventX = eventX;
		this.eventY = eventY;
		this.eventZ = eventZ;
		this.eventkarma = eventkarma;
		this.eventpvpkills = eventpvpkills;
		this.eventpkkills = eventpkkills;
		this.eventTitle = eventTitle;
		this.kills = kills;
		this.eventSitForced = eventSitForced;
		
	}
}
