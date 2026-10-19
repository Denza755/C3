/* 
	coded by Balancer
	balancer@balancer.ru
	http://balancer.ru

	version 0.2, 2005-12-10
	version 0.1, 2005-03-16
*/

package net.sf.l2j.gameserver.lib;

import java.util.Random;

import net.sf.l2j.gameserver.model.L2CharPosition;
import net.sf.l2j.gameserver.model.L2Object;

public class Rnd
{
	private static final Random _rnd = new Random();

	public static final float get()	// get random number from 0 to 1
	{
		return _rnd.nextFloat();
	}

	public static final int get(int n) // get random number from 0 to n-1
	{
		return _rnd.nextInt(n);
	}

	public static final int get(int min, int max)  // get random number from min to max (not max-1 !)
	{
		return min + _rnd.nextInt(max - min + 1);
	}

	public static final L2CharPosition coordsRandomize(L2Object obj, int min, int max)
	{
		return coordsRandomize(obj.getX(), obj.getY(), obj.getZ(), 0, min, max);
	}

	public static final L2CharPosition coordsRandomize(L2Object obj, int radius)
	{
		return coordsRandomize(obj, 0, radius);
	}

	public static final L2CharPosition coordsRandomize(int x, int y, int z, int heading, int radius_min, int radius_max)
	{
		if(radius_max==0 || radius_max<=radius_min)
			return new L2CharPosition(x, y, z, heading);

		int cycles = 100;
		L2CharPosition pos;
		double dist;
		do
		{
			pos = new L2CharPosition(x + Rnd.get(-radius_max, radius_max), y + Rnd.get(-radius_max, radius_max), z, heading);
			dist = Math.hypot(pos.x-x, pos.y-y);
		} while(dist < radius_min || dist > radius_max && cycles-- > 0);

		return pos;
	}

	public static final L2CharPosition coordsRandomize(L2CharPosition pos, int radius)
	{
		return coordsRandomize(pos.x, pos.y, pos.z, pos.heading, 0, radius);
	}
}
