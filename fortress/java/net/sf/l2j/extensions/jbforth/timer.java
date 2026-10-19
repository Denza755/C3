package net.sf.l2j.extensions.jbforth;

import net.sf.l2j.gameserver.ClientScheduler;
import ru.balancer.JBForth;
import ru.balancer.jbforth.Dict;
import ru.balancer.jbforth.Word;

public class timer
{
    public timer(Dict d)
    {
        d.reg("TIMER-START", new timer_start());
        d.reg("do-timer", new timer_start());
        d.reg("p-do-timer", new p_timer_start());
    }

	public class TimerTask implements Runnable
	{
		private final String code;
		private final Object self;
		private Object obj;

		TimerTask(String code, Object player)
		{
			this.code = code;
			this.self = player;
		}

		TimerTask(String code, Object player, Object obj)
		{
			this.code = code;
			this.self = player;
			this.obj  = obj;
		}

		public void run()
		{
			try
			{
				JBForth jbf_tmp = new JBForth(self);
//				jbf_tmp.setPlayer(player);
				if(obj != null)
					jbf_tmp.st.push(obj);
				jbf_tmp.execp(code);
			} 
			catch (Throwable t) 
			{
				System.out.println(""+t);
				Thread.dumpStack();
			}
		}
	}

    public class timer_start extends Word
    {
		// ( "code" n -- )
		// Start timer to use "code" after n msec.

        public void exec(JBForth jbf) 
        { 
			Integer time = jbf.st.pop_i();
			String code = jbf.st.pop_s();
//			System.out.println("Code to schedule="+code+"; time="+time);
			
			ClientScheduler.getInstance().scheduleMed(new TimerTask(code, jbf.getObject()), new Long(time));
        }
    }

    public class p_timer_start extends Word
    {
		// ( obj "code" n -- )
		// Start timer with new stack to use "code" after n msec.
		// obj placed from current to new stack.

        public void exec(JBForth jbf) 
        { 
			Integer time = jbf.st.pop_i();
			String code = jbf.st.pop_s();
			Object obj = jbf.st.pop();
//			System.out.println("Code to schedule="+code+"; time="+time);
			
			ClientScheduler.getInstance().scheduleMed(new TimerTask(code, jbf.getObject(), obj), new Long(time));
        }
    }
}
