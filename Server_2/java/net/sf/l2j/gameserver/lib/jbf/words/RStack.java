package net.sf.l2j.gameserver.lib.jbf.words;

import net.sf.l2j.gameserver.lib.jbf.JBData;

public class RStack
{
	public static void init(Dict d)
	{
		d.reg(">R", new tor());
		d.reg("R>", new rfrom());
        d.reg("R@", new rat());
        d.reg("RDROP", new rdrop());
	}

	public static class tor extends Word
	{
        public void exec(JBData data) 
		{ 
            data.rst.push(data.st.pop());
		}
	}

	public static class rfrom extends Word
	{
        public void exec(JBData data) 
        { 
            data.st.push(data.rst.pop());
        }
    }

    public static class rat extends Word
    {
        public void exec(JBData data) 
		{ 
            data.st.push(data.rst.top());
        }
    }

    public static class rdrop extends Word
    {
        public void exec(JBData data) 
        { 
            data.rst.drop();
		}
	}
}
