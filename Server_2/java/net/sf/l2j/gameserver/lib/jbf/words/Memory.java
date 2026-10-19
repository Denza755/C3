package net.sf.l2j.gameserver.lib.jbf.words;

import java.util.HashMap;

import net.sf.l2j.gameserver.lib.jbf.JBCore;
import net.sf.l2j.gameserver.lib.jbf.JBData;
import net.sf.l2j.gameserver.lib.jbf.Variable;

public class Memory
{
	public static void init(Dict d)
	{
		d.reg("@", new at());
		d.reg("!", new store());
        d.reg("ON", new won());
        d.reg("OFF", new woff());
		d.reg("H!", new hstore());
		d.reg(",", new comma());
		d.reg("CONSTANT", new constant());
        d.reg("GET{}", new pget());
        d.reg("SET{}", new pset());
	}

	public static class at extends Word
	{
        public void exec(JBData data) 
		{ 
            data.st.set(((Variable)data.st.top()).get());
		}
	}

	public static class store extends Word
	{
        public void exec(JBData data) 
		{ 
            Object x[] = data.st.pop2();
			((Variable)x[0]).set(x[1]);
		}
	}

    public static class won extends Word
    {
        public void exec(JBData data) 
        { 
            // ( var -- ) set var to true
            ((Variable)data.st.pop()).set(-1);
        }
    }

    public static class woff extends Word
    {
        public void exec(JBData data) 
        { 
            // ( var -- ) set var to false
            ((Variable)data.st.pop()).set(0);
        }
    }

	public static class hstore extends Word
	{
        public void exec(JBData data) 
		{ 
            Object x[] = data.st.pop2();
//			System.out.println("H! "+x[1]+"->"+x[0]);
			jbc.current_code.set((Integer)x[0], x[1]);
		}
	}

	public static class comma extends Word
	{
        public void exec(JBData data) 
		{ 
            jbc.current_code.add(data.st.pop());
            jbc.current_word.Count++;
		}
	}

	public static class constant extends Word
	{
        public void exec(JBData data) 
        { 
            JBCore.dict.reg(jbc.nextWord(data), new Literal(data.st.pop()));
        }
    }

    public static class pget extends Word
    {
        // ( HashMap<String,Object> "key-name" -- value )
        public void exec(JBData data) 
        { 
            String name = (String)data.st.pop();
            data.st.set(((HashMap<String,Object>)data.st.top()).get(name));
        }
    }

    public static class pset extends Word
    {
        // ( value HashMap<String,Object> "key-name" -- )
        public void exec(JBData data) 
		{ 
            String name = (String)data.st.pop();
            HashMap<String,Object> hash = (HashMap<String,Object>)data.st.top();
            hash.put(name,data.st.pop());
		}
	}
}
