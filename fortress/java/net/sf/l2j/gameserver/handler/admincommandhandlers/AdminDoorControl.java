package net.sf.l2j.gameserver.handler.admincommandhandlers;

import java.util.HashMap;
import java.util.Map;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.DoorTable;
import net.sf.l2j.gameserver.handler.IAdminCommandHandler;
import net.sf.l2j.gameserver.model.L2DoorInstance;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2PcInstance;

/**
 * This class handles following admin commands:
 * - open1 = open coloseum door 24190001
 * - open2 = open coloseum door 24190002
 * - open3 = open coloseum door 24190003
 * - open4 = open coloseum door 24190004
 * - openall = open all coloseum door
 * - close1 = close coloseum door 24190001
 * - close2 = close coloseum door 24190002
 * - close3 = close coloseum door 24190003
 * - close4 = close coloseum door 24190004
 * - closeall = close all coloseum door
 * 
 * - open = open selected door
 * - close = close selected door
 * @version $Revision: 1.2.4.5 $ $Date: 2005/04/11 10:06:06 $
 */
public class AdminDoorControl implements IAdminCommandHandler
{
    //private static Logger      _log            = Logger.getLogger(AdminDoorControl.class.getName());
    private static final int   REQUIRED_LEVEL  = Config.GM_DOOR;
    private static DoorTable   _doorTable;
    private static String[]    _adminCommands  = 
    {
        "admin_open",
        "admin_close",
        "admin_openall",
        "admin_closeall"
    };
    private static final Map<String, Integer>   doorMap = new HashMap<String, Integer>();
    
    static
    {
        doorMap.put("1", 24190001);
        doorMap.put("2", 24190002);
        doorMap.put("3", 24190003);
        doorMap.put("4", 24190004);
    }
    
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		if (!(checkLevel(activeChar.getAccessLevel()) && activeChar.isGM())) return false;
		_doorTable = DoorTable.getInstance();
        
		try {
            if (command.startsWith("admin_open "))
            {
                String doorName = command.split(" ")[1];
                _doorTable.getDoor(doorMap.get(doorName)).openMe();	
            }
            else if (command.equals("admin_close "))
            {
                String doorName = command.split(" ")[1];
                _doorTable.getDoor(doorMap.get(doorName)).closeMe(); 
            }
            if (command.equals("admin_closeall"))
            {
                for(L2DoorInstance door : _doorTable.getDoors())
                {
                    door.closeMe();
                }
            }
            if (command.equals("admin_openall"))
            {
                for(L2DoorInstance door : _doorTable.getDoors())
                {
                    door.openMe();
                }
            }
            if (command.equals("admin_open"))
            {
                L2Object target     = activeChar.getTarget();
                if (target instanceof L2DoorInstance)
                {
                    ((L2DoorInstance)target).openMe();
                }
                else
                {
                    activeChar.sendMessage("Incorrect target.");
                }
            }
            
            if (command.equals("admin_close"))
            {
                L2Object target = activeChar.getTarget();
                if (target instanceof L2DoorInstance)
                {
                    ((L2DoorInstance)target).closeMe();
                }
                else
                {
                    activeChar.sendMessage("Incorrect target.");
                }
            }
        } 
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return true;
	}

    public String[] getAdminCommandList()
    {
        return _adminCommands;
    }

    private boolean checkLevel(int level)
    {
        return (level >= REQUIRED_LEVEL);
    }
}
	
   