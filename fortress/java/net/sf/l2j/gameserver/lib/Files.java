/* 
	coded by Balancer
	balancer@balancer.ru
	http://balancer.ru

	version 0.1, 2006-01-04
*/

package net.sf.l2j.gameserver.lib;

import java.io.File;
import java.io.FileInputStream;
import java.io.BufferedReader;
import java.util.HashMap;
import java.util.logging.Logger;
import net.sf.l2j.gameserver.model.L2PcInstance;

import net.sf.l2j.Config;

public class Files
{
	private static Logger _log = Logger.getLogger(Strings.class.getName());

	private static HashMap<String,String> cache = new HashMap<String,String>();

	public static final String read(String name)
	{
		if(name == null)
			return null;

		if(Config.useFileCache && cache.containsKey(name))
			return cache.get(name);

		File file = new File("./"+name);

//		_log.info("Get file "+file.getPath());

		if(!file.exists())
			return null;

		String content = null;

		BufferedReader br = null;
		try
		{
			br = new BufferedReader(new UnicodeReader(new FileInputStream(file),"UTF-8"));
			StringBuffer sb = new StringBuffer();
			String s = new String();
            		while((s = br.readLine())!= null) { sb.append(s).append("\n"); }
			content = sb.toString();
			s=null; sb=null;
		}
		catch (Exception e) { /* problem are ignored */ }	   
		finally { try { br.close(); } catch (Exception e1) { /* problems ignored */ } }

		if(Config.useFileCache)			
			cache.put(name, content);

		return content;
	}

	public static final void cacheClean()
	{
		cache = new HashMap<String,String>();
	}

	public static final Long lastModified(String name)
	{
		if(name == null)
			return null;

		File file = new File(name);

//		_log.info("Get modify time for "+file.getPath());

		if(!file.exists())
			return null;

		return file.lastModified();
	}

	public static final String read(String name, L2PcInstance player)
	{
		return read(name, player.getVarS("lang@"));
	}

	public static final String langFileName(String name, String lang)
	{
		if(lang.equalsIgnoreCase("en"))
			lang = "";

		String tmp;

		tmp = name.replaceAll("(.+)(\\.htm)", "$1-"+lang+"$2");
		if(Config.DEBUG) _log.info("Try load file "+tmp);
		if(!tmp.equals(name) && (lastModified(tmp)) != null)
			return tmp;

		tmp = name.replaceAll("(.+)(/[^/].+\\.htm)", "$1/"+lang+"$2");
		if(Config.DEBUG) _log.info("Try load file "+tmp);
		if(!tmp.equals(name) && (lastModified(tmp)) != null)
			return tmp;

		tmp = name.replaceAll("(.+?/html)/", "$1-"+lang+"/");
		if(Config.DEBUG) _log.info("Try load file "+tmp);
		if(!tmp.equals(name) && (lastModified(tmp)) != null)
			return tmp;

		if(lastModified(name) != null)
			return name;

		return null;
	}

	public static final String read(String name, String lang)
	{
		if(lang.equalsIgnoreCase("en"))
			lang = "";

		String tmp = langFileName(name, lang);

		Long last_modif = lastModified(tmp);
		if(last_modif != null)
		{
			if(last_modif >= lastModified(name) || !Config.checkLangFilesModify)
				return Strings.bbParse(read(tmp));

			_log.warning("Last modify of "+name+" more then "+tmp);
		}

		return Strings.bbParse(read(name));
	}
}
