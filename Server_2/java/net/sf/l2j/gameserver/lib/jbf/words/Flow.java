package net.sf.l2j.gameserver.lib.jbf.words;

import net.sf.l2j.gameserver.lib.jbf.JBData;

public class Flow
{
	public static void init(Dict d)
	{
		d.reg("?COMP", new qcomp(), true);
		d.reg("BRANCH", new branch());
		d.reg("?BRANCH", new zbranch());
		d.reg("RECURSE", new recurse(), true);
        d.reg("EXIT", new exit());
	}

	public static class mark extends Word
	{
        public void exec(JBData data) 
		{ 
            data.st.push(jbc.current_word.Count);
			jbc.current_code.add(null);
		}
	}

	public static class qcomp extends Word
	{
        public void exec(JBData data) throws Exception
		{ 
            if((Integer)jbc.state.get() == 0)
				throw new Exception("Compile only word using while non-compile time!");
		}
	}

	public static class branch extends Word
	{
        public void exec(JBData data) throws Exception
		{ 
//			System.out.print("branch in word "+jbf.cws.w.getName()+" from "+jbf.cws.wp+" to ");
            data.cws.wp = (Integer)data.cws.w.code[data.cws.wp+1] - 1;
//			System.out.println(jbf.cws.wp);
		}
	}

	public static class zbranch extends Word
	{
        public void exec(JBData data) throws Exception
		{ 
            int pos = (Integer)data.cws.w.code[++data.cws.wp];
            Object x = data.st.pop();
            int cond = x instanceof Integer ? (Integer)x : -1;
//			System.out.println("zbranch. count= "+ws.wp+", cond="+cond+", go to "+(pos-1));
			if(cond == 0)
                data.cws.wp = pos-1;
		}
	}

	public static class recurse extends Word
	{
        public void exec(JBData data) throws Exception
		{ 
//			System.out.println("Recurse to "+jbf.current_name);
            jbc.do_word(jbc.current_word, data);
        }
    }

    public static class exit extends Word
    {
        public void exec(JBData data) throws Exception
        { 
            data.cws.wp = 9999999;
		}
	}
}
