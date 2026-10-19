/* 
	coded by Balancer
	balancer@balancer.ru
	http://balancer.ru

	version 0.1, 2005-10-31
*/

package net.sf.l2j.gameserver.lib;

import net.sf.l2j.Config;

import java.util.logging.Logger;
import java.util.regex.Pattern;

public class Strings
{
	private static Logger _log = Logger.getLogger(Strings.class.getName());

	private static final char hex[] ={'0','1','2','3','4','5','6','7','8','9','a','b','c','d','e','f'};
	public static String bytesToString(byte[] b)
	{
		String ret = "";
		for(int i=0; i<b.length; i++)
		{
			ret += String.valueOf(hex[(b[i]&0xF0)>>4]);
			ret += String.valueOf(hex[b[i]&0x0F]);
		}
		return ret;
	}

	public static String addSlashes(String s)
	{
		if(s == null)
			return "";

		s = s.replace("\\", "\\\\");
		s = s.replace("\"", "\\\"");
		s = s.replace("@", "\\@");
		s = s.replace("'", "\\'");
		return s;
	}

	public static String stripSlashes(String s)
	{
		s = s.replace("\\'", "'");
		s = s.replace("\\\\", "\\");
		return s;
	}

	public static Integer parseInt(Object x)
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

	public static Double parseFloat(Object x)
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

	public static Boolean parseBoolean(Object x)
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

    public static void reload()
	{
	}

	public static String translit(String s)
	{
		return s;
	}

	public static String fromTranslit(String s)
	{
		return s;
	}

	public static String replace(String str, String regex, int flags, String replace)
	{
		return Pattern.compile(regex, flags).matcher(str).replaceAll(replace);
    }

	public static boolean matches(String str, String regex, int flags)
	{
		return Pattern.compile(regex, flags).matcher(str).matches();
    }

	public static String bbParse(String s)
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
