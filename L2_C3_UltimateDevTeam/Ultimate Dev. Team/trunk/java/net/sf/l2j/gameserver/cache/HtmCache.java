/**
 * 
 */
package net.sf.l2j.gameserver.cache;

//import java.io.BufferedInputStream;

import javolution.util.FastMap;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.util.Util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.util.logging.Logger;

/**
 * @author Layane
 *
 */
public class HtmCache
{
    private static Logger _log = Logger.getLogger(HtmCache.class.getName());
    private static HtmCache _instance;
    
    private FastMap<Integer, String> _cache;
    
    private int _loadedFiles;
    private long _bytesBuffLen;
    
    public static HtmCache getInstance()
    {
        if (_instance == null)
        {
            _instance = new HtmCache();
        }
        
        return _instance;
    }
    
    public HtmCache()
    {
        _cache = new FastMap<Integer, String>();
        reload();
    }
    
    public void reload()
    {
        if (!Config.LAZY_CACHE)
        {
        	_log.info("Html cache start...");
            parseDir(Config.DATAPACK_ROOT);
            _log.info("Cache[HTML]: " + String.format("%.3f",getMemoryUsage())  + " megabytes on " + getLoadedFiles() + " files loaded");
        }
        else
        {
            _log.info("Cache[HTML]: Running lazy cache");
        }
    }
    
    public float getMemoryUsage()
    {
        return ((float)_bytesBuffLen/1048576);
    }
    
    public int getLoadedFiles()
    {
        return _loadedFiles;
    }
    
    class HtmFilter implements FileFilter
    {
        public boolean accept(File file)
        {
            return file.isDirectory() || (file.getName().endsWith(".htm") || file.getName().endsWith(".html"));
        }
    }
    
    private void parseDir(File dir)
    {
        File[] files = dir.listFiles(new HtmFilter());
        
        for (File file : files)
        {
            
            if (!file.isDirectory())
            {
                loadFile(file);
            }
            else
            {
                parseDir(file);
            }
        }
    }
    
    public String loadFile(File file)
    {
        HtmFilter filter = new HtmFilter();
        if (file.exists() && filter.accept(file) && !file.isDirectory())
        {
            String content = null;
            FileInputStream fis = null;
            
            try
            {
                fis = new FileInputStream(file);
                BufferedReader br = new BufferedReader(new UnicodeReader(fis,"UTF-8"));
                int bytes = fis.available();
		StringBuffer sb = new StringBuffer();
		String s = "";
       		while((s = br.readLine())!= null) { sb.append(s).append("\n"); }
		content = sb.toString();
		s=null; sb=null;
                
                String relpath = Util.getRelativePath(Config.DATAPACK_ROOT,file); 
                int hashcode = relpath.hashCode();
                
                String oldContent = _cache.get(hashcode);
                
                if (oldContent == null)
                {
                    _bytesBuffLen += bytes;
                    _loadedFiles++;
                }
                else
                {
                    _bytesBuffLen = _bytesBuffLen - oldContent.length() + bytes;
                }
                
                _cache.put(hashcode,content);
                
                return content;
            }
            catch (Exception e)
            {
                _log.warning("problem with htm file " + e);
            }
            finally
            {
                try { fis.close(); } catch (Exception e1) { }
            }   
        }
        return null;
    }
    
    public String getHtmForce(String path)
    {
        String content = getHtm(path);
        
        if (content == null)
        {
            content = "<html><body>My Text is missing:<br>" + path + "</body></html>";
            _log.warning("missing html page "+path);
        }
        
        return content;
    }
    
    public String getHtm(String path)
    {
        String content = _cache.get(path.hashCode());
        
        if (Config.LAZY_CACHE && content == null)
        {
            content = loadFile(new File(Config.DATAPACK_ROOT,path));
        }
        
        return content;
    }
    
    public boolean contains(String path)
    {
        return _cache.containsKey(path.hashCode());
    }
    /**  
 	* Check if an HTM exists and can be loaded  
 	* @param 
 	* path The path to the HTM 
 	* */ 
 	public boolean isLoadable(String path) 
 	{ 
 		File file = new File(path); 
 		HtmFilter filter = new HtmFilter();
        return file.exists() && filter.accept(file) && !file.isDirectory();
     }
    
}
