package net.sf.l2j.gameserver.lib.jbf.words;

import net.sf.l2j.gameserver.lib.jbf.JBData;

public class Core
{
	public static void init(Dict d)
	{
		d.reg("+", new add());
		d.reg("-", new sub());
		d.reg("*", new Mul());
		d.reg("NEGATE", new negate());
		d.reg("NOT", new not());
		d.reg("<", new less());
		d.reg(">", new great());
		d.reg("=", new eq());
		d.reg("0=", new zeq());
		d.reg("1-", new oneminus());
		d.reg(".", new Dot());
		d.reg(".S", new dots());
		d.reg("D.S", new ddots());
	}

	public static class add extends Word
	{
		public void exec(JBData data) 
		{ 
		    int n1 = (Integer) data.st.pop();
		    int n2 = (Integer) data.st.top();
			data.st.set(n1 + n2);
		}
	}

	public static class sub extends Word
	{
		public void exec(JBData data) 
		{ 
		    int n = (Integer)data.st.pop();
			data.st.set((Integer)data.st.top() + n);
		}
	}

	public static class Mul extends Word
	{
		public void exec(JBData data) 
		{ 
		    int n = (Integer)data.st.pop();
			data.st.set((Integer)data.st.top() * n);
		}
	}

	public static class Dot extends Word
	{
		public void exec(JBData data) 
		{
//			System.out.println("dot jbc="+jbc);
			data.stdout += ""+data.st.pop()+" ";
		}
	}

	public static class dots extends Word
	{
		public void exec(JBData data) 
		{
			for(int i=data.st.depth()-1; i>=0; i--)
				data.stdout += ""+ data.st.peek(i) + " ";
		}
	}

	public static class ddots extends Word
	{
		public void exec(JBData data) 
		{
			System.out.print("Stack:");
			for(int i=data.st.depth()-1; i>=0; i--)
				System.out.print(data.st.peek(i) + " ");
			System.out.println(" ");
		}
	}

	public static class oneminus extends Word
	{
		public void exec(JBData data) 
		{
			data.st.set((Integer)data.st.top()-1);
		}
	}

	public static class less extends Word
	{
		public void exec(JBData data) 
		{
			data.st.set((Integer)data.st.pop() > (Integer)data.st.top() ? -1 : 0);
		}
	}

	public static class great extends Word
	{
		public void exec(JBData data) 
		{
			data.st.set((Integer)data.st.pop() < (Integer)data.st.top() ? -1 : 0);
		}
	}

	public static class eq extends Word
	{
		public void exec(JBData data) 
		{
		    int n = (Integer)data.st.pop();
			data.st.set((Integer)data.st.top() == n ? -1 : 0);
		}
	}

	public static class zeq extends Word
	{
		public void exec(JBData data) 
		{
			data.st.set((Integer)data.st.top() == 0 ? -1 : 0);
		}
	}

	public static class negate extends Word
	{
		public void exec(JBData data) 
		{
			data.st.set(-((Integer)data.st.top()));
		}
	}

	public static class not extends Word
	{
		public void exec(JBData data) 
		{
			data.st.set(~((Integer)data.st.top()));
		}
	}
}

