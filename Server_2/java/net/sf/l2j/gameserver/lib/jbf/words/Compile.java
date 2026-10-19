package net.sf.l2j.gameserver.lib.jbf.words;

import java.util.ArrayList;

import net.sf.l2j.gameserver.lib.jbf.JBCore;
import net.sf.l2j.gameserver.lib.jbf.JBData;

public class Compile
{
	public static void init(Dict d)
	{
		d.reg(":", new colon());
		d.reg(";", new semicolon(), true);
		d.reg("STATE", new state());
		d.reg("HERE", new here());
		d.reg("?PAIR", new qpair());
		d.reg("IMMEDIATE", new immediate());
		d.reg("[']", new comp_apo(), true);
		d.reg("POSTPONE", new postpone(), true);
	}

	public static class colon extends Word
	{
        public void exec(JBData data) 
		{ 
            jbc.state.set(1);
			jbc.current_word = new Word();
			jbc.current_code = new ArrayList<Object>();
            jbc.current_name = jbc.nextWord(data);
			jbc.last_word = null;
            jbc.current_word.Count = 0;
		}
	}

	public static class semicolon extends Word
	{
        public void exec(JBData data) 
		{ 
			jbc.current_word.make(jbc.current_code);
            JBCore.dict.reg(jbc.current_name, jbc.current_word);
			jbc.last_word = jbc.current_word;
            jbc.state.set(0);
	  	}
 	}

	public static class state extends Word
	{
        public void exec(JBData data) 
		{ 
            data.st.push(jbc.state);
	  	}
	}

	public static class here extends Word
	{
        public void exec(JBData data) 
		{ 
            data.st.push(jbc.current_word.Count);
	  	}
	}

	public static class immediate extends Word
	{
        public void exec(JBData data) 
		{ 
			jbc.last_word.immediate = true;
	  	}
	}

	public static class qpair extends Word
	{
        public void exec(JBData data) throws Exception
		{ 
            Object x[] = data.st.pop2();
			if(x[0] != x[1])
				throw new Exception("Non pair operators! "+x[0]+"!="+x[1]);
	  	}
	}

	public static class comp_apo extends Word
	{
        public void exec(JBData data) throws Exception
		{
            String name = jbc.nextWord(data);
            Word w = JBCore.dict.get(name);
			if(w == null)
				throw new Exception("Word '"+name+"' not found for [']");
//			System.out.println("get address for "+name+" -> "+w);
            jbc.literal(w, data);
	  	}
	}

	public static class postpone extends Word
	{
        public void exec(JBData data) throws Exception
		{
            String name = jbc.nextWord(data);
            Word w = JBCore.dict.get(name);
			if(w == null)
				throw new Exception("Word '"+name+"' not found for POSTPONE");
			if(w.isImmediate())
			{
                jbc.literal(w, data);
			}
			else
			{
				jbc.current_code.add(new Literal(w));
                jbc.current_word.Count++;
                jbc.current_code.add(new Literal(JBCore.dict.get(",")));
                jbc.current_word.Count++;
			}
		}
	}
}
