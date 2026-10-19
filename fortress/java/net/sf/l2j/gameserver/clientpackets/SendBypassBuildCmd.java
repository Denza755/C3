/*
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA.
 *
 * http://www.gnu.org/copyleft/gpl.html
 */
package net.sf.l2j.gameserver.clientpackets;

import java.nio.ByteBuffer;
import java.util.logging.Logger;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.ClientThread;
import net.sf.l2j.gameserver.handler.AdminCommandHandler;
import net.sf.l2j.gameserver.handler.IAdminCommandHandler;
import ru.balancer.JBForth;
import net.sf.l2j.gameserver.model.L2PcInstance;

/**
 * This class handles all GM commands triggered by //command
 * 
 * @version $Revision: 1.3.4.2 $ $Date: 2005/03/27 15:29:29 $
 */
public class SendBypassBuildCmd extends ClientBasePacket
{
	private static Logger _log = Logger.getLogger(SendBypassBuildCmd.class.getName());

	private static final String _C__5B_SENDBYPASSBUILDCMD = "[C] 5b SendBypassBuildCmd";
	public final static int GM_MESSAGE = 9;
	public final static int ANNOUNCEMENT = 10;

	private String _command;
	
	public SendBypassBuildCmd(ByteBuffer buf, ClientThread client)
	{
		super(buf, client);
		_command = readS();

		if(_command != null)
			_command = _command.trim();

	}

	void runImpl()
	{
		if(Config.DEBUG) 	_log.info("Got command '"+_command+"'");
		
		L2PcInstance activeChar = getClient().getActiveChar();

        if(activeChar == null)
            return;
		
		String word = (_command.split("\\s+"))[0];
		String args = _command.substring(word.length()).trim();

		if(activeChar.jbf.word_exists("admin_"+word))
		{
			if(Config.DEBUG) 	_log.info("Parsed by forth as '"+args+"' admin_"+word);

			JBForth tmp = new JBForth(activeChar);
			tmp.st.push(args);
		    tmp.execp("admin_"+word);
	   		return;
		}

		if(activeChar.jbf.word_exists("gm_"+word))
		{
			if(Config.DEBUG) 	_log.info("Parsed by forth as '"+args+"' gm_"+word);

			JBForth tmp = new JBForth(activeChar);
			tmp.st.push(args);
		    tmp.execp("gm_"+word);
	   		return;
		}

		IAdminCommandHandler ach = AdminCommandHandler.getInstance().getAdminCommandHandler("admin_"+_command);		
		if (ach != null) 
		{
			ach.useAdminCommand("admin_"+_command, activeChar);
			return;
		} 

    	if(activeChar.getAccessLevel() >= Config.GM_USE_FORTH)
    	{
			if(Config.DEBUG) 	_log.info("Eval by JBForth");
			activeChar.jbf.execp(_command);
   			return;
		}

		activeChar.sendMessage("No access to use forth or command '"+_command+"' not recognized");
	}
		
	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.clientpackets.ClientBasePacket#getType()
	 */
	public String getType()
	{
		return _C__5B_SENDBYPASSBUILDCMD;
	}
}
