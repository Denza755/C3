package net.sf.l2j.gameserver.lib.jbf.words;

import net.sf.l2j.gameserver.lib.jbf.JBData;

public class Parse
{
	public static void init(Dict d)
	{
		d.reg("\\", new back_slash(), true);
		d.reg("(", new open_bracket(), true);
	}

	public static class back_slash extends Word
	{
		public void exec(JBData data) 
		{ 
			for(int i=(Integer)data.in.get(); i<data.commands.length(); i++)
			{
				String ch = data.commands.substring(i,i+1);
				if(ch.equals("\n") || ch.equals("\r"))
				{
					data.in.set(i+1);
					return;
				}
		  	}
			data.in.set(data.commands.length());
		}
	}

	public static class open_bracket extends Word
	{
		public void exec(JBData data) 
		{ 
			for(int i=(Integer)data.in.get(); i<data.commands.length(); i++)
			{
				if(data.commands.substring(i,i+1).equals(")"))
				{
					data.in.set(i+1);
					return;
				}
		  	}
			data.in.set(data.commands.length());
		}
	}
}

