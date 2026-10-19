// Java Forth Implementaton by Balancer

package net.sf.l2j.gameserver.lib.jbf;

import java.io.File;
import java.util.ArrayList;
import java.util.logging.Logger;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.lib.jbf.words.Dict;
import net.sf.l2j.gameserver.lib.jbf.words.Literal;
import net.sf.l2j.gameserver.lib.jbf.words.Word;

public final class JBCore
{
    static Logger _log = Logger.getLogger(JBCore.class.getName());
	private static final JBCore _instance = new JBCore();
	public static final Dict dict = new Dict();

	public ArrayList<Object> current_code;
	public Word current_word;
	public String current_name;
	public Word last_word;

	// State is common data because not worked parallel compile
	public Variable state = new Variable();

	private JBCore()
	{
//		System.out.println("new jbc="+this);
	}

    public JBData reload()
    {
        JBData data = new JBData();
        dict.reset();
		state = new Variable();
        exec("Main", data);
        return data;
    }

	public static JBCore getInstance()
	{	
		return _instance;
	}

	public Dict getDict()
	{	
		return dict;
	}

    public void do_word(Word w, JBData data)
	{
//		System.out.println("Do word "+w.getName());
        if((Integer)state.get() != 0 && !w.isImmediate())
		{
			current_code.add(w);
            current_word.Count++;
		}
	   	else
	   	{
	   		try
	   		{
                w.exec(data);
	   	  	} catch(Exception e){e.printStackTrace();}
	 	}
	}

    public void literal(Object n, JBData data)
	{
        if((Integer)state.get() != 0)
		{
			current_code.add(new Literal(n));
            current_word.Count++;
		}
	  	else
	  	{
//			System.out.println("Push "+n);
	  		data.st.push(n);
	  	}
	}

    public void exec(String code, JBData data)
	{
//      System.out.println("Execute '"+code+"'");

		data.fst.push(data.in.get());
		data.fst.push(data.commands);

		data.in.set(0);
		data.commands = code;
		boolean cont = true;
		String word_name;
        while((word_name = nextWord(data)) != null && cont)
		{
//			System.out.println("Parse word '"+word_name+"'");
			Word w = getDict().get(word_name);
			if(w != null)
			{
                do_word(w, data);
				continue;
		   	}
			
            if(word_name.matches("^[\\-\\+]?[\\d]+$"))
			{
                literal(Integer.parseInt(word_name), data);
				continue;
			}
		   	
            if(word_name.matches("^[\\-\\+]?0x[\\da-fA-F]+$"))
            {
                word_name = word_name.replaceAll("0x","");
                literal(Integer.parseInt(word_name,16), data);
                continue;
            }

            if(word_name.matches("^[\\-\\+]?[\\d]+\\.?[\\d]*[Ee]*[\\d]*$"))
            {
                literal(Double.parseDouble(word_name), data);
                continue;
            }

		   	if(word_name.charAt(0) == '\"')
		   	{
			   	String rest = word_name.length() > 1 ? word_name.substring(1,word_name.length()-1) : null;
		   		if(word_name.charAt(word_name.length()-1) == '\"')
					literal(rest, data);
			   	else
					literal(rest+nextWord('\"', data), data);
			  	continue;
		   	}

		   	if(word_name.charAt(0) == '\'')
		   	{
			   	String rest = word_name.length() > 1 ? word_name.substring(1,word_name.length()-1) : null;
		   		if(word_name.charAt(word_name.length()-1) == '\'')
					literal(rest, data);
			   	else
					literal(rest+nextWord('\'', data), data);
			  	continue;
		   	}

			String fname = "data/scripts/"+word_name;
			File file = new File(Config.DATAPACK_ROOT, fname);
			if(file.exists())
		   	{
//				System.out.println("1:"+fname);
                exec("\""+fname+"\" INCLUDED", data);
		   		continue;
		   	}

			fname = "data/scripts/"+word_name+".f";
			file = new File(Config.DATAPACK_ROOT, fname);
			if(file.exists())
		   	{
//				System.out.println("2:"+fname);
                exec("\""+fname+"\" INCLUDED", data);
		   		continue;
		   	}
				
			fname = "data/scripts/jbf/"+word_name+".f";
			file = new File(Config.DATAPACK_ROOT, fname);
			if(file.exists())
		   	{
//				System.out.println("2:"+fname);
                exec("\""+fname+"\" INCLUDED", data);
		   		continue;
		   	}
				
//			System.out.println("Not found '"+word_name+"'");
			data.stdout += "\nAbort: Unknown word '"+word_name+"'\n";
			cont = false;
		}

		data.commands = (String)data.fst.pop();
		data.in.set(data.fst.pop());
		
		return;
	}

    public String nextWord(JBData data)
	{
		boolean skip = true;
		int start = -1;

		for(int i = (Integer)data.in.get(); i < data.commands.length(); i++)
		{
			char ch = data.commands.charAt(i);

			if(Character.isWhitespace(ch))
			{

				if(skip) 
					continue;

				data.in.set(i);
				return data.commands.substring(start,i);
			}
			else
			{
				if(skip)
				{
					skip = false;
					start = i;
					continue;
				}
			}
		}
		data.in.set(data.commands.length());
		return start >= 0 ? data.commands.substring(start,(Integer)data.in.get()) : null;
	}

	public String nextWord(char delim, JBData data)
	{
		int start=(Integer)data.in.get();

		for(int i=start; i<data.commands.length(); i++)
		{
			char ch = data.commands.charAt(i);

			if(ch == delim)
			{
				data.in.set(i+1);
				return data.commands.substring(start,i);
			}
		}
		data.in.set(data.commands.length());
		return data.commands.substring(start,(Integer)data.in.get());
	}
}
