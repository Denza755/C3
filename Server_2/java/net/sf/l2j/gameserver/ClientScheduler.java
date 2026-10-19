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
 * @author mkizub
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
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

	public static final ClientScheduler getInstance()
	{
		return _instance;
	}
	
	private ClientScheduler()
	{
		if (Config.ASSERT) assert _instance == null;
		_executorHighPriority = new ScheduledThreadPoolExecutor(4, new ThrFactory(1, "PoolHIGH"));
		_executorMedPriority = new ScheduledThreadPoolExecutor(10, new ThrFactory(0, "PoolMED"));
		_executorLowPriority = new ScheduledThreadPoolExecutor(4, new ThrFactory(-1, "PoolLOW"));
	}
	
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
	
	public static void addReceivedMsg(ClientBasePacket pkt)
	{
		TaskPriority pr = pkt.getPriority();
		if (pr == null)
			pr = PR_LOW;
		switch (pr)
		{
		case PR_URGENT:
			pkt.run();
			return;
		case PR_HIGH:
			_instance._executorHighPriority.execute(pkt);
			return;
		case PR_MEDIUM:
			_instance._executorMedPriority.execute(pkt);
			return;
		case PR_LOW:
		default:
			_instance._executorLowPriority.execute(pkt);
			return;
		}
	}
	
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

	public ScheduledFuture scheduleHighAtFixedRate(Runnable r, long initial, long delay)
	{
		try
		{
		if (delay < 0) delay = 0;
		return _executorHighPriority.scheduleAtFixedRate(r, initial, delay, TimeUnit.MILLISECONDS);
		} catch (RejectedExecutionException e) { return null; /* shutdown, ignore */ }
	}

	public ScheduledFuture scheduleMedAtFixedRate(Runnable r, long initial, long delay)
	{
		try
		{
		if (delay < 0) delay = 0;
		return _executorMedPriority.scheduleAtFixedRate(r, initial, delay, TimeUnit.MILLISECONDS);
		} catch (RejectedExecutionException e) { return null; /* shutdown, ignore */ }
	}

	public ScheduledFuture scheduleLowAtFixedRate(Runnable r, long initial, long delay)
	{
		try
		{
		if (delay < 0) delay = 0;
		return _executorLowPriority.scheduleAtFixedRate(r, initial, delay, TimeUnit.MILLISECONDS);
		} catch (RejectedExecutionException e) { return null; /* shutdown, ignore */ }
	}

	public ScheduledFuture schedule(Runnable r, TaskPriority pr, long delay)
	{
		switch (pr)
		{
		case PR_HIGH:   return scheduleHigh(r, delay);
		case PR_MEDIUM: return scheduleMed(r, delay);
		default:
		case PR_LOW:    return scheduleLow(r, delay);
		}
	}

	public ScheduledFuture scheduleHigh(Runnable r, long delay)
	{
		try
		{
		if (delay < 0) delay = 0;
		return _executorHighPriority.schedule(r, delay, TimeUnit.MILLISECONDS);
		} catch (RejectedExecutionException e) { return null; /* shutdown, ignore */ }
	}

	public ScheduledFuture scheduleMed(Runnable r, long delay)
	{
		try
		{
		if (delay < 0) delay = 0;
		return _executorMedPriority.schedule(r, delay, TimeUnit.MILLISECONDS);
		} catch (RejectedExecutionException e) { return null; /* shutdown, ignore */ }
	}

	public ScheduledFuture scheduleLow(Runnable r, long delay)
	{
		try
		{
		if (delay < 0) delay = 0;
		return _executorLowPriority.schedule(r, delay, TimeUnit.MILLISECONDS);
		} catch (RejectedExecutionException e) { return null; /* shutdown, ignore */ }
	}

	class ThrFactory implements ThreadFactory {
		private final String _prefix;
		private final int _priority;
		private int _count;
		
		ThrFactory(int priority, String prefix) {
			_prefix = prefix;
			_priority = priority;
		}
		
		public Thread newThread(Runnable r) {
			Thread thr = new Thread(r, _prefix+"-"+_count);
			thr.setDaemon(true);
			thr.setPriority(Thread.NORM_PRIORITY + _priority);
			_count++;
			return thr;
		}
		
	}
}
