package net.sf.l2j.webserver;

import net.sf.l2j.Config;
import net.sf.l2j.webserver.WebServerThread;

public class WebServer extends Thread
{
	WebServerThread wst = null;

	public void run()
	{
		System.out.println("Starting WebServer...");
//		while(true)
		{
			try
			{
				if(wst == null)
					wst = new WebServerThread(Config.WebServerPort, Config.WebServerRoot);
				else
					Thread.dumpStack();
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
	}
}
