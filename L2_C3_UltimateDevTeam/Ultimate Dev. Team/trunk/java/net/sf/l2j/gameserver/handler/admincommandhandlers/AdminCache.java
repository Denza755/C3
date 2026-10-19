/**
 * 
 */
package net.sf.l2j.gameserver.handler.admincommandhandlers;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.cache.CrestCache;
import net.sf.l2j.gameserver.cache.HtmCache;
import net.sf.l2j.gameserver.handler.IAdminCommandHandler;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;

import java.io.File;

/**
 * @author Layanere
 *
 */
public class AdminCache implements IAdminCommandHandler
{

    private static final int   REQUIRED_LEVEL  = Config.GM_CACHE;
    private static String[]    _adminCommands  = 
    {
        "admin_cache_htm_rebuild",
        "admin_cache_htm_reload",
        "admin_cache_crest_rebuild",
        "admin_cache_crest_reload",
        "admin_cache_crest_fix"
    };
    
    public String[] getAdminCommandList()
    {
        return _adminCommands;
    }

    public boolean useAdminCommand(String command, L2PcInstance activeChar)
    {
        if (!Config.ALT_PRIVILEGES_ADMIN)
            if (!(checkLevel(activeChar.getAccessLevel()) && activeChar.isGM())) return false;
        
        if (command.startsWith("admin_cache_htm_rebuild") || command.equals("admin_cache_htm_reload"))
        {
            HtmCache.getInstance().reload();
            activeChar.sendMessage("Cache[HTML]: " + HtmCache.getInstance().getMemoryUsage()  + " megabytes on " + HtmCache.getInstance().getLoadedFiles() + " files loaded");
        }
        else if(command.startsWith("admin_cache_htm_reload "))
        {
            String path = command.split(" ")[1];
            if (HtmCache.getInstance().loadFile(new File(Config.DATAPACK_ROOT,path)) != null)
            {
                activeChar.sendMessage("Cache[HTML]: file was loaded");
            }
            else
            {
                activeChar.sendMessage("Cache[HTML]: file can't be loaded");
            }
        }
        else if (command.startsWith("admin_cache_crest_rebuild") || command.startsWith("admin_cache_crest_reload"))
        {
            CrestCache.getInstance().reload();
            activeChar.sendMessage("Cache[Crest]: " + String.format("%.3f",CrestCache.getInstance().getMemoryUsage())  + " megabytes on " + CrestCache.getInstance().getLoadedFiles() + " files loaded");
        }
        else if (command.startsWith("admin_cache_crest_fix"))
        {
            CrestCache.getInstance().convertOldPedgeFiles();
            activeChar.sendMessage("Cache[Crest]: crests fixed");
        }
        
        return true;
    }
    
    private boolean checkLevel(int level)
    {
        return (level >= REQUIRED_LEVEL);
    }
    
}
