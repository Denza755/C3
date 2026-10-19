package net.sf.l2j.gameserver.handler.admincommandhandlers;

import java.util.StringTokenizer;
import java.util.logging.Logger;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.handler.IAdminCommandHandler;
import net.sf.l2j.gameserver.model.L2PcInstance;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;

public class AdminKick implements IAdminCommandHandler {
    private static Logger _log = Logger.getLogger(AdminKick.class.getName());
    private static String[] _adminCommands = {"admin_kick"};
    private static final int REQUIRED_LEVEL = Config.GM_KICK;
	
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
    {

		if (!(checkLevel(activeChar.getAccessLevel()) && activeChar.isGM()))
        {
            //System.out.println("Not required level");
            return false;
        }
        if (command.startsWith("admin_kick"))
        {
            //System.out.println("ADMIN KICK");
            StringTokenizer st = new StringTokenizer(command);
            //System.out.println("Tokens: "+st.countTokens());
            if (st.countTokens() > 1)
            {
                st.nextToken();
                String player = st.nextToken();
                //System.out.println("Player1 "+player);
                L2PcInstance plyr = L2World.getInstance().getPlayer(player);
                if (plyr != null)
                {
                    //System.out.println("Player2 "+plyr.getName());
                    plyr.logout();
                }
				SystemMessage sm = new SystemMessage(614);
				sm.addString("You kicked " + plyr.getName() + " from the game.");
				activeChar.sendPacket(sm);
            }
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
