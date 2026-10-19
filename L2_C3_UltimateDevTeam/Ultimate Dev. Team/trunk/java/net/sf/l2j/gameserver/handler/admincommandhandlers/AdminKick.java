package net.sf.l2j.gameserver.handler.admincommandhandlers;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.handler.IAdminCommandHandler;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.packets.serverpackets.LeaveWorld;

import java.util.StringTokenizer;

public class AdminKick implements IAdminCommandHandler {
    private static String[] _adminCommands = {"admin_kick" ,"admin_kick_non_gm"};
    private static final int REQUIRED_LEVEL = Config.GM_KICK;
	
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
    {

        if (!Config.ALT_PRIVILEGES_ADMIN)
        {
	     if (!(checkLevel(activeChar.getAccessLevel()) && activeChar.isGM()))
                return false;
        }
        
        if (command.startsWith("admin_kick"))
        {
            StringTokenizer st = new StringTokenizer(command);
            if (st.countTokens() > 1)
            {
                st.nextToken();
                String player = st.nextToken();
                //System.out.println("Player1 "+player);
                L2PcInstance plyr = L2World.getInstance().getPlayer(player);
                if (plyr != null)
                {
                    plyr.logout();
                }
				activeChar.sendMessage("You kicked " + plyr.getName() + " from the game.");
            }
        }
        if (command.startsWith("admin_kick_non_gm"))
        {
        	int counter = 0;
        	for (L2PcInstance player : L2World.getInstance().getAllPlayers())
            {
        		if(!player.isGM())
        		{
        			counter++;
        			player.sendPacket(new LeaveWorld());
        			player.logout();
        		}
            }
        	activeChar.sendMessage("Kicked "+counter+" players");
        }
        return true;
    }

    public String[] getAdminCommandList() {
        return _adminCommands;
    }
    
    private boolean checkLevel(int level) {
        return (level >= REQUIRED_LEVEL);
    }
}
