/* 
	coded by Balancer
	balancer@balancer.ru
	http://balancer.ru

	version 0.1, 2005-10-31
*/

package net.sf.l2j.gameserver.lib;

import java.util.logging.Logger;
import java.util.regex.Pattern;

import net.sf.l2j.Config;

public class Strings
{
	private static Logger _log = Logger.getLogger(Strings.class.getName());

	private static final char hex[] ={'0','1','2','3','4','5','6','7','8','9','a','b','c','d','e','f'};
	public static final String bytesToString(byte[] b)
	{
		String ret = new String();
		for(int i=0; i<b.length; i++)
		{
			ret += String.valueOf(hex[(b[i]&0xF0)>>4]);
			ret += String.valueOf(hex[b[i]&0x0F]);
		}
		return ret;
	}

	public static final String addSlashes(String s)
	{
		if(s == null)
			return "";

		s = s.replace("\\", "\\\\");
		s = s.replace("\"", "\\\"");
		s = s.replace("@", "\\@");
		s = s.replace("'", "\\'");
		return s;
	}

	public static final String stripSlashes(String s)
	{
		s = s.replace("\\'", "'");
		s = s.replace("\\\\", "\\");
		return s;
	}

	public static final Integer parseInt(Object x)
	{
		if(x == null)
			return 0;

		if(x instanceof Integer)
			return (Integer)x;

		if(x instanceof Double)
			return ((Double)x).intValue();

		if(x instanceof Boolean)
			return (Boolean)x ? -1 : 0;

		Integer res = 0;
		try { res = Integer.parseInt(""+x); } catch(Exception e) { }
		return res;
	}

	public static final Double parseFloat(Object x)
	{
		if(x instanceof Double)
			return (Double)x;

		if(x instanceof Integer)
			return 0.0+(Integer)x;

		if(x == null)
			return 0.0;

		Double res = 0.0;
		try { res = Double.parseDouble(""+x); } catch(Exception e) { }
		return res;
	}

	public static final Boolean parseBoolean(Object x)
	{
		if(x instanceof Integer)
			return (Integer)x != 0;

		if(x == null)
			return false;

		if(x instanceof Boolean)
			return (Boolean)x;

		if(x instanceof Double)
			return Math.abs((Double)x) < 0.00001;

		return !((""+x).equals(""));
	}

	private static String[] tr;
	private static String[] trb;

	public static final void reload()
	{
		Files.cacheClean();
		String[] pairs = Files.read("data/translit.txt").split("\n");
		tr = new String[pairs.length*2];
		for(int i=0; i<pairs.length; i++)
		{
			String[] ss = pairs[i].split(" +");
			tr[i*2]     = ss[0];
			tr[i*2+1]   = ss[1];
		}

		pairs = Files.read("data/translit_back.txt").split("\n");
		trb = new String[pairs.length*2];
		for(int i=0; i<pairs.length; i++)
		{
			String[] ss = pairs[i].split(" +");
			trb[i*2]     = ss[0];
			trb[i*2+1]   = ss[1];
		}

	}

	public static final String translit(String s)
	{
		if(!Config.ENABLE_TRANSLIT_ENGINE)
			return s;

		for(int i=0; i<tr.length; i+=2)
			s = s.replace(tr[i], tr[i+1]);

		return s;
	}

	public static final String fromTranslit(String s)
	{
		if(!Config.ENABLE_TRANSLIT_ENGINE)
			return s;

		for(int i=0; i<trb.length; i+=2)
			s = s.replace(trb[i], trb[i+1]);

		return s;
	}

	public static final String replace(String str, String regex, int flags, String replace)
	{
		return Pattern.compile(regex, flags).matcher(str).replaceAll(replace);
    }

	public static final boolean matches(String str, String regex, int flags)
	{
		return Pattern.compile(regex, flags).matcher(str).matches();
    }

	public static final String bbParse(String s)
	{
		if(s==null)
			return null;

		s = s.replace("\r","");

		if(Config.DEBUG)
			_log.info("Parse string\n==========================\n"+s+"\n==========================\n");
			
		s = s.replaceAll("(\\s|\"|\'|\\(|^|\n)\\*(.*?)\\*(\\s|\"|\'|\\)|\\?|\\.|!|:|;|,|$|\n)","$1<font color=\"LEVEL\">$2</font>$3");
	
		s = replace(s, "^!(.*?)$", Pattern.MULTILINE, "<font color=\"LEVEL\">$1</font>\n\n");

		s = s.replaceAll("%%\\s*\n","<br1>\n");

		if(!s.contains("<br>"))
			s = s.replaceAll("\n\n+"," <br>\n");

		s = replace(s, "\\[([^\\]\\|]*?)\\|([^\\]]*?)\\]", Pattern.DOTALL, "<a action=\"bypass -h $1\">$2</a>");

		if(Config.DEBUG)
			_log.info("to \n==========================\n"+s+"\n==========================\n");

		return s;
	}
}
