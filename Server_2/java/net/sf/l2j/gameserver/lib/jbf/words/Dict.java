package net.sf.l2j.gameserver.lib.jbf.words;

import java.util.HashMap;

public class Dict
{
	private HashMap<String,Word> _words;

	public Dict()
	{
		reset();
	}
	
	public void reset() {
	    _words = new HashMap<String,Word>();
	
		Parse.init(this);
		Compile.init(this);
		Memory.init(this);
		RStack.init(this);
		Stack.init(this);
		Core.init(this);
		Flow.init(this);
		FileSystem.init(this);
		Vars.init(this);
		L2j.init(this);
		JBFext.init(this);
        new SQL(this);
        new Strings(this);
	}

	public HashMap showWords()
	{
		return _words;
	}

	public void reg(String name, Word word) 
	{
        _words.put(name.toLowerCase(), word);
		word.setName(name);
//		System.out.println("registred "+name+" -> "+word);
	}

	public void reg(String name, Word word, boolean immed)
	{
		reg(name, word);
		word.immediate = immed;
	}

	public Word get(String name)
	{
//		System.out.println("get "+name+" -> "+_words.get(name));
        return _words.get(name.toLowerCase());
	}
}
