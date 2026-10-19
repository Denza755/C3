/*
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
package net.sf.l2j.gameserver;

import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Logger;

import net.sf.l2j.gameserver.script.DateRange;

/**
 * @author Luis Arias
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class EventDroplist
{

    private static Logger _log = Logger.getLogger(EventDroplist.class.getName());

	private static EventDroplist _instance;

	private ArrayList<DateDrop> allNpcDateDrops;

	public static EventDroplist getInstance()
	{
		if (_instance == null)
		{
			_instance = new EventDroplist();
		}
		return _instance;
	}
	
	public class DateDrop
	{
        DateRange dateRange;
	    public int[] items;
	    public int min;
	    public int max;
	    public int chance;
	    
	    
	}

	private EventDroplist()
	{
	    allNpcDateDrops = new ArrayList<DateDrop>();
	}
	
	public void addGlobalDrop(int[] items, int[] count, int chance, DateRange range)
	{
	    DateDrop date = new DateDrop();
	    date.dateRange = range;
	    date.items = items;
	    date.min = count[0];
	    date.max = count[1];
	    date.chance = chance;
	    allNpcDateDrops.add(date);
	}
	
	public ArrayList<DateDrop> getAllDrops()
	{
	    ArrayList<DateDrop> list = new ArrayList<DateDrop>();
	    
	    for (DateDrop drop : allNpcDateDrops)
	    {
	        Date currentDate = new Date();
	        //System.out.println("From: "+drop.from+" To: "+drop.to+" Now: "+ currentDate);
	        if (drop.dateRange.isWithinRange(currentDate))
	        {
	            list.add(drop);
	        }
	    }
	    
	    return list;
	}

}
