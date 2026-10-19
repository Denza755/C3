package net.sf.l2j.gameserver.lib.jbf;


public final class Variable
{
    Object var = 0;

	public void set(Object x)
	{
		var = x;
	}
	
	public Object get()
	{
		return var;
	}
}
