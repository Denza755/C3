package net.sf.l2j.gameserver.lib.jbf.words;

import net.sf.l2j.gameserver.lib.jbf.JBData;


public class Literal extends Word
{
	Object _x;

	public Literal(Object x) 
	{ 
		_x = x; 
//			System.out.println("Add literal "+_x);
		setName("LIT "+x);
	}

	public void exec(JBData data)
	{
//			System.out.println("Get literal "+_x);
			data.st.push(_x);
	}
}
