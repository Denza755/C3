package net.sf.l2j.gameserver.lib.jbf.words;

import net.sf.l2j.gameserver.lib.jbf.JBData;

public class Vars
{
	public static void init(Dict d)
	{
		d.reg(">IN", new var_in());
        d.reg("WARNING", new var_warning());
	}

	public static class var_in extends Word
	{
        public void exec(JBData data) 
		{ 
            data.st.push(data.in);
        }
    }

    public static class var_warning extends Word
    {
        public void exec(JBData data) 
        { 
            data.st.push(data.warning);
		}
	}
}
