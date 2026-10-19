/* 
	coded by Balancer
	balancer@balancer.ru
	http://balancer.ru

	version 0.1, 2005-03-16
*/

package net.sf.l2j.gameserver.lib;

import java.util.Random;

public class Rnd
{
	private static final Random _rnd = new Random();

	public static int get(int n)
	{
		return _rnd.nextInt(n);
	}

	public static int get(int min, int max)
	{
		return min + _rnd.nextInt(max - min);
	}
}
