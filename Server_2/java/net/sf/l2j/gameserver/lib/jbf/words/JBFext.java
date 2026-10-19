package net.sf.l2j.gameserver.lib.jbf.words;

import net.sf.l2j.gameserver.lib.Rnd;
import net.sf.l2j.gameserver.lib.jbf.JBData;

public class JBFext
{
	public static void init(Dict d)
	{
		d.reg("CHOOSE", new choose());
		d.reg("NUMBER?", new numberq());
	}

	public static class choose extends Word
	{
		public void exec(JBData data) 
		{ 
			data.st.set(Rnd.get((Integer)data.st.top()));
		}
	}

	public static class numberq extends Word
	{
		public void exec(JBData data) 
		{ 
			data.st.set(data.st.top() instanceof Integer ? -1 : 0);
		}
	}
}

