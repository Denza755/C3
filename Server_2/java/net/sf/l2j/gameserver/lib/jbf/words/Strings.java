package net.sf.l2j.gameserver.lib.jbf.words;

import net.sf.l2j.gameserver.lib.jbf.JBData;

public class Strings
{
    public Strings(Dict d)
    {
        d.reg("REPLACE", new replace());
        d.reg(">SLASHES", new addslashes());
        d.reg("S+", new splus());
        d.reg(">S", new tos());
    }

    public class replace extends Word
    {
        public void exec(JBData data) 
        { 
            // ( str from to -- str2 )
            String to = (String)data.st.pop();
            String from = (String)data.st.pop();
            data.st.set(((String)data.st.top()).replace(from, to));
        }
    }

    public class addslashes extends Word
    {
        public void exec(JBData data) 
        { 
            // ( str -- str2 )
            String s = (String)data.st.top();
            s = s.replace("'", "\\'");
            data.st.set(s);
        }
    }

    public class splus extends Word
    {
        public void exec(JBData data) 
        { 
            // ( str1 str2 -- str1+str2 )
            String s = (String)data.st.pop();
            data.st.set("" + data.st.top() + s);
        }
    }

    public class tos extends Word
    {
        public void exec(JBData data) 
        { 
            // ( x -- str )
            data.st.set(""+data.st.top());
        }
    }
}
