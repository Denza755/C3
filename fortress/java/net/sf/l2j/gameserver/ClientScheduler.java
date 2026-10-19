/*
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA.
 *
 * http://www.gnu.org/copyleft/gpl.html
 */
package net.sf.l2j.gameserver;

import static net.sf.l2j.gameserver.TaskPriority.PR_LOW;

import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.clientpackets.ClientBasePacket;

/**
 * This class permit to create 3 Threads pools and to add to it tasks to execute in function of their priority (HIGH, MEDIUM,LOW).<BR><BR>
 * 
 * <B><U> Threads pools </U> :</B><BR><BR>
 * <li> A pool containing 4 Hight Threads </li>
 * <li> A pool containing 10 Medium Threads </li>
 * <li> A pool containing 4 Low priority Threads </li><BR><BR>
 * 
 */

public final class ClientScheduler
{
	static Logger _log = Logger.getLogger(ClientScheduler.class.getName());

	public static final int PRIORITY_URGENT = 3;
	public static final int PRIORITY_HIGH   = 2;
	public static final int PRIORITY_MEDIUM = 1;
	public static final int PRIORITY_LOW    = 0;
	
	private static final ClientScheduler _instance = new ClientScheduler();
	
	private final ScheduledThreadPoolExecutor _executorHighPriority;
	private final ScheduledThreadPoolExecutor _executorMedPriority;
	private final ScheduledThreadPoolExecutor _executorLowPriority;


	/**
	 * Constructor of ClientScheduler.<BR><BR>
	 */
	public static final ClientScheduler getInstance()
	{
		return _instance;
	}
	
	
	/**
	 * Create a pool of 4 Hight Threads, a pool of 10 Medium Threads, and a pool of 4 Low priority Threads to execute Task.<BR><BR>
	 */
	private ClientScheduler()
	{
		if (Config.ASSERT) assert _instance == null;
		
		_executorHighPriority = new ScheduledThreadPoolExecutor(Config.THREAD_P_HIGH, new ThrFactory(1, "PoolHIGH"));
		_executorMedPriority = new ScheduledThreadPoolExecutor(Config.THREAD_P_MED, new ThrFactory(0, "PoolMED"));
		_executorLowPriority = new ScheduledThreadPoolExecutor(Config.THREAD_P_LOW, new ThrFactory(-1, "PoolLOW"));
	}
	
	/**
	 * Close all pool of Threads.<BR><BR>
	 */
	public void closeAll()
	{
		try { _executorHighPriority.shutdown(); } catch (Throwable t) { _log.log(Level.WARNING, "", t); }
		try { _executorMedPriority.shutdown(); } catch (Throwable t) { _log.log(Level.WARNING, "", t); }
		try { _executorLowPriority.shutdown(); } catch (Throwable t) { _log.log(Level.WARNING, "", t); }
		
		try { _executorHighPriority.awaitTermination(1000, TimeUnit.MILLISECONDS); } catch (Throwable t) { _log.log(Level.WARNING, "", t); }
		try { _executorMedPriority.awaitTermination(1000, TimeUnit.MILLISECONDS); } catch (Throwable t) { _log.log(Level.WARNING, "", t); }
		try { _executorLowPriority.awaitTermination(1000, TimeUnit.MILLISECONDS); } catch (Throwable t) { _log.log(Level.WARNING, "", t); }
		
		_executorHighPriority.shutdownNow();
		_executorHighPriority.shutdownNow();
		_executorHighPriority.shutdownNow();
	}
	
	
	/**
	 * Send a ClientBasePacket to a pool in function of its priority (HIGH, MEDIUM,LOW) or execute it immediatly if URGENT.<BR><BR>
	 */
	public static void addReceivedMsg(ClientBasePacket pkt)
	{
		TaskPriority pr = pkt.getPriority();
		
		if (pr == null)
			pr = PR_LOW;
		
		// Add a task to one of the pool of thread in function of its priority (HIGH, MEDIUM,LOW)
		// This has effect equivalent to schedule(command, 0, anyUnit)

		if(Config.DEVELOPER)
			_log.info("Run execute packet "+pkt+"; pr="+pr);

		switch (pr)
		{
			case PR_URGENT:
				pkt.run();
				break;
			case PR_HIGH:
				_instance._executorHighPriority.execute(pkt);
				break;
			case PR_MEDIUM:
				_instance._executorMedPriority.execute(pkt);
				break;
			case PR_LOW:
			default:
				_instance._executorLowPriority.execute(pkt);
				break;
		}

		if(Config.DEVELOPER)
			_log.info("End execute packet "+pkt);
	}
	
	
	/**
	 * Create a task that is executed after an initial time (in ms) and each delay ms and define its priority.<BR><BR>
	 *   
	 * <B><U> Example of use </U> :</B><BR><BR>
	 * <li> Cubic  Task</li><BR><BR>
	 *
	 * @param r The task to launch
	 * @param TaskPriority The priority of the task
	 * @param initial The initial delay (in ms) after what the task is executed for the first time
	 * @param delay The delay (in ms) between 2 consecutive executions of the task
	 * 
	 */
	public ScheduledFuture scheduleAtFixedRate(Runnable r, TaskPriority pr, long initial, long delay)
	{
		switch (pr)
		{
			case PR_HIGH:   return scheduleHighAtFixedRate(r, initial, delay);
			case PR_MEDIUM: return scheduleMedAtFixedRate(r, initial, delay);
			default:
			case PR_LOW:    return scheduleLowAtFixedRate(r, initial, delay);
		}
	}


	/**
	 * Create a High priority task that is executed after an initial time (in ms) and each delay ms.<BR><BR>
	 *   
	 * @param r The task to launch
	 * @param initial The initial delay (in ms) after what the task is executed for the first time
	 * @param delay The delay (in ms) between 2 consecutive executions of the task
	 * 
	 */
	public ScheduledFuture scheduleHighAtFixedRate(Runnable r, long initial, long delay)
	{
		try
		{
			if (delay < 0) delay = 0;
			return _executorHighPriority.scheduleAtFixedRate(r, initial, delay, TimeUnit.MILLISECONDS);
		} catch (RejectedExecutionException e) { return null; /* shutdown, ignore */ }
	}

	
	/**
	 * Create a Medium priority task that is executed after an initial time (in ms) and each delay ms.<BR><BR>
	 *    
	 * <B><U> Example of use </U> :</B><BR><BR>
	 * <li> MP/HP Regeneration Task</li>
	 * <li> Auto SoulShot</li><BR><BR>
	 *  
	 * @param r The task to launch
	 * @param initial The initial delay (in ms) after what the task is executed for the first time
	 * @param delay The delay (in ms) between 2 consecutive executions of the task
	 * 
	 */
	public ScheduledFuture scheduleMedAtFixedRate(Runnable r, long initial, long delay)
	{
		try
		{
		if (delay < 0) delay = 0;
		return _executorMedPriority.scheduleAtFixedRate(r, initial, delay, TimeUnit.MILLISECONDS);
		} catch (RejectedExecutionException e) { return null; /* shutdown, ignore */ }
	}

	
	/**
	 * Create a Low priority task that is executed after an initial time (in ms) and each delay ms.<BR><BR>
	 *    
	 * <B><U> Example of use </U> :</B><BR><BR>
	 * <li> AI change intention</li>
	 * <li> Game time controller</li>
	 * <li> MP/HP Regeneration Task</li>
	 * <li> Effect Task</li>
	 * <li> Monster race</li><BR><BR>
	 * 
	 * @param r The task to launch
	 * @param initial The initial delay (in ms) after what the task is executed for the first time
	 * @param delay The delay (in ms) between 2 consecutive executions of the task
	 * 
	 */
	public ScheduledFuture scheduleLowAtFixedRate(Runnable r, long initial, long delay)
	{
		try
		{
			if (delay < 0) delay = 0;
				return _executorLowPriority.scheduleAtFixedRate(r, initial, delay, TimeUnit.MILLISECONDS);
		} catch (RejectedExecutionException e) { return null; /* shutdown, ignore */ }
	}

	
	/**
	 * Create a task that is executed after an initial time (in ms) and define its priority.<BR><BR>
	 *   
	 * @param r The task to launch
	 * @param TaskPriority The priority of the task
	 * @param initial The initial delay (in ms) after what the task is executed for the first time
	 * 
	 */
	public ScheduledFuture schedule(Runnable r, TaskPriority pr, long delay)
	{
		switch(pr)
		{
			case PR_HIGH:   return scheduleHigh(r, delay);
			case PR_MEDIUM: return scheduleMed(r, delay);
			default:
			case PR_LOW:    return scheduleLow(r, delay);
		}
	}

	
	/**
	 * Create a High priority task that is executed after an initial time (in ms).<BR><BR>
	 *   
	 * @param r The task to launch
	 * @param initial The initial delay (in ms) after what the task is executed for the first time
	 * @param delay The delay (in ms) between 2 consecutive executions of the task
	 * 
	 */
	public ScheduledFuture scheduleHigh(Runnable r, long delay)
	{
		try
		{
		if (delay < 0) delay = 0;
		return _executorHighPriority.schedule(r, delay, TimeUnit.MILLISECONDS);
		} catch (RejectedExecutionException e) { return null; /* shutdown, ignore */ }
	}

	
	/**
	 * Create a Medium priority task that is executed after an initial time (in ms).<BR><BR>
	 *   
	 * @param r The task to launch
	 * @param initial The initial delay (in ms) after what the task is executed for the first time
	 * @param delay The delay (in ms) between 2 consecutive executions of the task
	 * 
	 */
	public ScheduledFuture scheduleMed(Runnable r, long delay)
	{
		try
		{
		if (delay < 0) delay = 0;
		return _executorMedPriority.schedule(r, delay, TimeUnit.MILLISECONDS);
		} catch (RejectedExecutionException e) { return null; /* shutdown, ignore */ }
	}

	/**
	 * Create a Low priority task that is executed after an initial time (in ms).<BR><BR>
	 *   
	 * @param r The task to launch
	 * @param initial The initial delay (in ms) after what the task is executed for the first time
	 * @param delay The delay (in ms) between 2 consecutive executions of the task
	 * 
	 */
	public ScheduledFuture scheduleLow(Runnable r, long delay)
	{
		try
		{
			if (delay < 0) delay = 0;
				return _executorLowPriority.schedule(r, delay, TimeUnit.MILLISECONDS);
		} catch (RejectedExecutionException e) { return null; /* shutdown, ignore */ }
	}

	
	class ThrFactory implements ThreadFactory 
	{
		private final String _prefix;
		private final int _priority;
		private int _count;
		
		ThrFactory(int priority, String prefix) 
		{
			_prefix = prefix;
			_priority = priority;
		}
		
		public Thread newThread(Runnable r) 
		{
			Thread thr = new Thread(r, _prefix+"-"+_count);
			thr.setDaemon(true);
			thr.setPriority(Thread.NORM_PRIORITY + _priority);
			_count++;
			return thr;
		}
	}
    
    public String[] getStats()
    {
        return new String[] {
            "HIGH",
            "ActiveThreads:   "+_executorHighPriority.getActiveCount(),
            "CompletedTasks:  "+_executorHighPriority.getCompletedTaskCount(),
            "getCorePoolSize: "+_executorHighPriority.getCorePoolSize(),
            "MaximumPoolSize: "+_executorHighPriority.getMaximumPoolSize(),
            "LargestPoolSize: "+_executorHighPriority.getLargestPoolSize(),
            "PoolSize:        "+_executorHighPriority.getPoolSize(),
            "ScheduledTasks:  "+_executorHighPriority.getTaskCount(),
            "MEDIUM",
            "ActiveThreads:   "+_executorMedPriority.getActiveCount(),
            "CompletedTasks:  "+_executorMedPriority.getCompletedTaskCount(),
            "getCorePoolSize: "+_executorMedPriority.getCorePoolSize(),
            "MaximumPoolSize: "+_executorMedPriority.getMaximumPoolSize(),
            "LargestPoolSize: "+_executorMedPriority.getLargestPoolSize(),
            "PoolSize:        "+_executorMedPriority.getPoolSize(),
            "ScheduledTasks:  "+_executorMedPriority.getTaskCount(),
            "LOW",
            "ActiveThreads:   "+_executorLowPriority.getActiveCount(),
            "CompletedTasks:  "+_executorLowPriority.getCompletedTaskCount(),
            "getCorePoolSize: "+_executorLowPriority.getCorePoolSize(),
            "MaximumPoolSize: "+_executorLowPriority.getMaximumPoolSize(),
            "LargestPoolSize: "+_executorLowPriority.getLargestPoolSize(),
            "PoolSize:        "+_executorLowPriority.getPoolSize(),
            "ScheduledTasks:  "+_executorLowPriority.getTaskCount()
        };
    }


    public static ScheduledFuture scheduleEvent(Runnable event, long delay)
    {
        return getInstance().scheduleLow(event, delay);
    }
}
