/* 
	coded by Balancer
	balancer@balancer.ru
	http://balancer.ru

	version 0.1, 2005-06-06
*/

package net.sf.l2j.gameserver.lib;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Logger;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.GmListTable;
import net.sf.l2j.gameserver.model.L2PcInstance;

public class Log
{
	private static final Logger _log = Logger.getLogger(Log.class.getName());

	public static final void add(String text, String cat)
	{
/*		Logger _log = logs.get(cat);
		if(_log == null)
		{
			_log = Logger.getLogger(cat);
			logs.put(cat, _log);
		}*/

		String date = (new SimpleDateFormat("yy.MM.dd hh:mm:ss")).format(new Date());

		new File("log/game").mkdirs();
		File file 		= new File("log/game/"+(cat!=null?cat:"_all")+".txt");
		FileWriter save 		= null; 
		
		try
		{
//			file.getAbsolutePath().mkdirs();
			save = new FileWriter(file, true);
			String out = "["+date+"] '---': "+text+"\n"; // "+char_name()+"
			save.write(out);
		}
		catch (IOException e)
		{
			_log.warning("saving chat log failed: " + e);
			e.printStackTrace();
		}
		finally
		{
			try { save.close(); } catch (Exception e1) { }
		}
	
		if(cat != null)
			add(text, null);
	}

	public static final void Assert(boolean exp)
	{
		Assert(exp,"");
	}

	public static final void Assert(boolean exp, String cmt)
	{
		if(exp || !Config.ASSERT)
			return;


		System.out.println("Assertion error ["+cmt+"]");
		Thread.dumpStack();
	}

	public static final void IllegalPlayerAction(L2PcInstance player, String msg, Integer jail_items)
	{
		msg = "Illegal "+player.toFullString()+" action "+(jail_items>0?"(autojailed for "+jail_items+" items)":"")+": "+msg;
		
		Log.add(msg,"illegal-actions");
		
		GmListTable.broadcastMessageToGMs(msg);

		if(jail_items > 0)
			player.jbf.exec(""+jail_items + " jail-me");
	}
}
