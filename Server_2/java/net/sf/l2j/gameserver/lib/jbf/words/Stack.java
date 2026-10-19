package net.sf.l2j.gameserver.lib.jbf.words;

import net.sf.l2j.gameserver.lib.jbf.JBData;

public class Stack
{
	public static void init(Dict d)
	{
		d.reg("SWAP", new swap());
		d.reg("DUP", new dup());
        d.reg("?DUP", new qdup());
		d.reg("DROP", new drop());
        d.reg("NIP", new nip());
		d.reg("2DROP", new ddrop());
		d.reg("ROT", new rot());
        d.reg("OVER", new over());
	}

	public static class swap extends Word
	{
        public void exec(JBData data) 
		{ 
            data.st.swap();
		}
	}

	public static class dup extends Word
	{
        public void exec(JBData data) 
        { 
            data.st.dup();
        }
    }

    public static class qdup extends Word
    {
        public void exec(JBData data) 
		{ 
            Object x = data.st.top();
            if(x instanceof Integer && (Integer)x == 0)
                return;
            if(x instanceof String && (String)x == "")
                return;
            if(x == null)
                return;
            data.st.dup();
		}
	}

	public static class drop extends Word
	{
        public void exec(JBData data) 
        { 
            data.st.drop();
        }
    }

    public static class nip extends Word
    {
        public void exec(JBData data) 
        { 
            data.st.set(data.st.pop());
        }
    }

	public static class ddrop extends Word
	{
        public void exec(JBData data) 
		{ 
            data.st.ddrop();
		}
	}

	public static class rot extends Word
	{
        public void exec(JBData data) 
        { 
            data.st.rot();
        }
    }

    public static class over extends Word
    {
        public void exec(JBData data) 
		{ 
            data.st.over();
		}
	}
}
