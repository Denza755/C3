// Java Forth Implementaton by Balancer

package net.sf.l2j.gameserver.lib.jbf;


public final class Stack
{
	private Object _st[];
	private int _sp; // Stack pointer

	public Stack()
	{	
	    _sp = -1; 
		_st = null;
	    setStack(1024);
	}

    public Stack(int size)
    {   
        _sp = -1; 
        _st = null;
        setStack(size);
    }

	public void setStack(int n)
	{
		Object[] new_stack = new Object[n];

		if(_st != null)
			for(int i=0; i<Math.min(_st.length,n) && i<=_sp; i++)
				new_stack[i] = _st[i];
		
		_st = new_stack;
	}

	public void push(Object n) 
	{
		_st[++_sp] = n;
//		System.out.println("Push "+_st[_sp]);
	}
	
	public Object pop()
	{
//		System.out.println("pop "+_st[_sp]);
        if(_sp<0)
            return null;

		return _st[_sp--];
	}

	public Object[] pop2()
	{
        if(_sp<1)
            return null;

		Object x[] = new Object[2];
		x[0] = _st[_sp--];
		x[1] = _st[_sp--];
		return x;
	}

	public void swap()
	{
        if(_sp<1)
            return;

		Object n = _st[_sp];
		_st[_sp] = _st[_sp-1];
		_st[_sp-1] = n;
	}

	public void drop()
	{
        if(_sp>=0)
			_sp--;
	}

	public void ddrop()
	{
        if(_sp>0)
			_sp-=2;
	}

	public void dup()
	{
		push(top());
	}

	public void rot()
	{
        if(_sp<2)
            return;

		Object tmp = _st[_sp];
		_st[_sp] = _st[_sp-2];
		_st[_sp-2] = _st[_sp-1];
		_st[_sp-1] = tmp;
	}

	public Object peek(int n)
	{
        if(_sp<n)
            return null;

		return _st[_sp-n];
	}

	public Object top()
	{
		return _st[_sp];
	}

	public void set(Object x)
	{
		_st[_sp] = x;
	}

	public int depth()
	{
        return _sp + 1;
    }

    public void setDepth(int size)
    {
        _sp = size - 1;
    }

    public void dup2()
    {
        if(_sp<1)
            return;

        _sp+=2;
        _st[_sp]   = _st[_sp-2];
        _st[_sp-1] = _st[_sp-3];
    }

    public void over()
    {
        if(_sp<1)
            return;

        _st[++_sp] = _st[_sp-2];
	}
}
