package net.sf.l2j.gameserver.lib.jbf.words;

import java.util.ArrayList;

import net.sf.l2j.gameserver.lib.jbf.JBCore;
import net.sf.l2j.gameserver.lib.jbf.JBData;

public class Word
{
	public boolean immediate;
	public JBCore jbc;

	Object[] code;
	public int Count;
	String name;

	public Word()
	{
		jbc = JBCore.getInstance();
//		System.out.println("init jbc="+jbc);
	}

	public void immediate(boolean i) 
	{
		immediate = i;
	}
	
	public boolean isImmediate() 
	{
		return immediate;
	}

	public void make(ArrayList<Object> code_list)
 	{
 		code = code_list.toArray(new Object[code_list.size()]);
 	}

 	private String rname(Object w)
 	{
 		if(w instanceof Word)
 			return ((Word)w).getName();

	   	return (String)w;
 	}

 	public void exec(JBData data) throws Exception
 	{
	 	data.rst.push(data.cws);
        data.cws = new WordState(this,Count);
 		for(data.cws.wp=0; data.cws.wp<code.length; data.cws.wp++)
 		{
//			System.out.print(""+name+"["+count+"]: ("+code[count]+") = ");
//			System.out.println(""+((Word)code[count]).getName());
			try
			{
 				((Word)code[data.cws.wp]).exec(data);
 		   	}
 		   	catch(Exception e)
 		   	{
	 		   	String cur_name = rname(code[data.cws.wp]);

 		   		data.stdout += "Exception while execute word '"+cur_name+"' in word '"+name+"'.\n";
 		   		data.stdout += ": "+name+" ";
 		   		for(int i=0; i<code.length; i++)
 		   		{
 		   			if(i==data.cws.wp)
		 		   		data.stdout += "~";
 		   			data.stdout += rname(code[i]);
 		   			if(i==data.cws.wp)
		 		   		data.stdout += "~";
	 		   		data.stdout += " ";
 		   		}
 		   		data.stdout += ";\n";

 		   		data.stdout += e;
 		   		
/*				for(int i=jbc.data.rst.depth()-1; i>=0; i--)
				{
					Object x = jbc.data.rst.peek(i);
					System.out.println("RST: "+x);
					String rname = "??? ";
					try
					{
						rname = ((Word)x).getName();
					} catch(Exception e2) {}
	
					jbc.data.stdout += ""+rname+" ";

			   	}*/
			  	data.stdout += "\n";
//				System.out.println("ERR: " + jbc.data.stdout);
 		   		e.printStackTrace();
 		   	}
 		}
 		data.cws = (WordState)data.rst.pop();
 	}

 	public void setName(String s)
 	{
 		name = s;
 	}

 	public String getName()
 	{
 		return name;
 	}
}
