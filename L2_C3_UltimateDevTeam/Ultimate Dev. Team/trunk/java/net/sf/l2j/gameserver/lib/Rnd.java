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
	private static Random _rnd = new Random();

/*	private Rnd()
    {
        _rnd = new Random();
    }
    public static Rnd getInstance()
    {
        if (_instance == null)
        _instance = new Rnd();
        return _instance;
    } */

	public static float get()	// get random number from 0 to 1
	{
		return _rnd.nextFloat();
	}

	public static int get(int n) // get random number from 0 to n-1
	{
		return (int)Math.floor(_rnd.nextDouble()*n);
	}

	public static int get(int min, int max)  // get random number from min to max (not max-1 !)
	{
		return min + _rnd.nextInt(max - min + 1);
	}
	public static int nextInt(int n)
	{
		return (int)Math.floor(_rnd.nextDouble()*n);
	}
	public static double nextDouble()
	{
		return _rnd.nextDouble();
	}
	public static double nextGaussian()
	{
		return _rnd.nextGaussian();
	}
}
