/* (C) Saboteur
 *	Nochannel mode for player with autooff in x minutes
 *	usage: nochannel <name> [<minutes>]
 *	default 30 minutes
 */
package net.sf.l2j.gameserver.handler.admincommandhandlers;

import java.util.StringTokenizer;
import java.util.logging.Logger;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.handler.IAdminCommandHandler;
import net.sf.l2j.gameserver.model.L2PcInstance;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;

public class AdminNochannel implements IAdminCommandHandler
{
	private static Logger _log = Logger.getLogger(AdminBan.class.getName());
	private static String[] _adminCommands = {"admin_nochannel"};
	private static final int REQUIRED_LEVEL = Config.GM_BAN_CHAT;
	
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		if (!(checkLevel(activeChar.getAccessLevel()) && activeChar.isGM())) // not enougth GM rights
			return false;

		if (command.startsWith("admin_nochannel"))
		{
			StringTokenizer st = new StringTokenizer(command);

			if (st.countTokens() > 1)
			{
				st.nextToken();
				String player = st.nextToken();
				L2PcInstance plr = L2World.getPlayer(player);
				SystemMessage sm = new SystemMessage(614);

				if (plr != null)
				{
					int timeval = 30; // if no args, then 30 min default.

					if ( st.countTokens() == 1)
					{
						String time = st.nextToken();
						timeval = Integer.parseInt(time);
					}

					if (timeval == 0)
					{
						sm.addString("You deactivate nochannel mode for player " + plr.getName());
						update_NoChannel(player, plr, 0);
					}
					else
					{
						sm.addString("You activated nochannel mode for player " + plr.getName()+" for "+ timeval +" minutes.");
						update_NoChannel(player, plr, timeval);
					}
				}
				else { sm.addString("Player not found (" + player + ")."); }
			 	activeChar.sendPacket(sm);
			}
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

	private void update_NoChannel(String user, L2PcInstance player, int time)
	{
		player.updateNoChannel(time*60000);
		if(time == 0) 
		{
			_log.warning("Nochannel removed from character " + user);
			player.sendMessage("Nochannel removed from character " + user);
		}
		else
		{
			_log.warning("Nochannel activated for character " + user + " for " + time + " min.");
			player.sendMessage("Nochannel activated for character " + user + " for " + time + " min.");
		}
	}
}
