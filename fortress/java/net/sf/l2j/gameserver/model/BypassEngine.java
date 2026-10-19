/* 
	coded by Balancer
	balancer@balancer.ru
	http://balancer.ru

	version 0.2, 2005-11-02
	version 0.1, 2005-10-31
*/

package net.sf.l2j.gameserver.model;

import java.security.MessageDigest;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sf.l2j.gameserver.lib.Rnd;
import net.sf.l2j.gameserver.lib.Strings;

public class BypassEngine
{
	private static final Logger _log = Logger.getLogger(BypassEngine.class.getName());

//	private static final int MAX_RECORDS = Config.MAXIMUM_ONLINE_USERS*10;
	private static final int HASH_LENGTH = 16;

	private static final ConcurrentHashMap<String,BypassStorage> bypassMap = new ConcurrentHashMap<String,BypassStorage>();

	public static final String encode(L2PcInstance player, String html)
	{
		Pattern p = Pattern.compile("\"(bypass +-h +)(.+?)\"");
		Matcher m = p.matcher(html);
		StringBuffer sb = new StringBuffer();
		while(m.find())
		{
//			System.out.print("Found bypass '");
			String bypass = m.group(2);
//			System.out.println(bypass+"'");

			try
			{
				String code   = bypass;
				String params = "";
				boolean use_params = false;

				int pos = bypass.indexOf(" $");
				if(pos>=0)
				{
					code       = bypass.substring(0,pos);
					params     = bypass.substring(pos).replace("$", "\\$");
//					_log.info("Found params '"+params+"' with code '"+code+"' in bypass '"+bypass+"'");
					use_params = true;
				}

				String hash = Strings.bytesToString(MessageDigest.getInstance("MD5").digest((""+Rnd.get()).getBytes("UTF-8"))).substring(0,HASH_LENGTH);
//				_log.info("Created hash '"+hash+"'");
				bypassMap.put(hash, new BypassStorage(player, code, use_params));
				m.appendReplacement(sb, "\"bypass -h "+hash+params+"\"");
//				_log.info("Hash appened.");
			}
			catch(Exception e)
			{
				_log.warning("Exception "+e);
				e.printStackTrace();
			}
		}
		m.appendTail(sb);

//		_log.info("Encript '"+html+"' to '"+sb.toString()+"'");

		return sb.toString();
	}

	public static final String decode(L2PcInstance player, String bypass)
	{
		if(bypass == "")
			return "";

		if(Strings.matches(bypass, "^(bbs_|_mrsl|_clbbs|_friend|_mm|_mail|_bbs|friendlist|friendmail).*", Pattern.DOTALL))
			return bypass;

		if(bypass.length() < HASH_LENGTH)
			return errRet(player, "Possible hack!!! Too short bypass: '"+bypass+"'", 0);

		String hash = bypass.substring(0,HASH_LENGTH);
		BypassStorage bs = bypassMap.get(hash);
		if(bs == null)
		{
			if(hash.matches("[0-9a-f]{"+HASH_LENGTH+"}"))
				return "";
			else
				return errRet(player, "Possible hack!!! Not found hash '"+hash+"' in bypass '"+bypass+"'", 0);
		}

		if(!bs.use_params && !hash.equals(bypass))
			return errRet(player, "Possible hack!!! Try use bypass with wrong parameters '"+bypass+"' with command '"+bs.code+"'", 0);

		if(bs.player != player)
			return errRet(player, "Possible hack!!! Wrong "+(player!=null?player.toFullString():"null player")+" try execute '"+bs.code+"' over stored bypass '"+bypass+"'", 0);

		String code = bs.code;

		if(bs.use_params)
			code += bypass.substring(HASH_LENGTH);

		bypassMap.remove(hash);

		clean(player);

//		_log.info("Decript bypass '"+bypass+"' with hash '"+hash+"' to '"+code+"'");

		return code;
	}

	private static final String errRet(L2PcInstance player, String errMsg, Integer jail_items)
	{
		if(player != null)
			player.illegalAction(errMsg, jail_items);
		else
			_log.warning("Hm... Illegal action '"+errMsg+"' for null player?");
		return "may_be_hack_detected";
	}

	private static final void clean(L2PcInstance caller)
	{
		long now = System.currentTimeMillis();
        for(Iterator it = bypassMap.keySet().iterator(); it.hasNext(); )
        {
            String hash = (String)it.next();
        	BypassStorage bs = bypassMap.get(hash);
        	if(bs.time <= now || bs.player == caller)
				bypassMap.remove(hash);
        }
	}
}
