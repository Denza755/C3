/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package net.sf.l2j.gameserver.handler.voicedcommandhandlers;

import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import net.sf.l2j.gameserver.handler.IVoicedCommandHandler;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.Base64;
import net.sf.l2j.L2DatabaseFactory;

public class PasswordChange implements IVoicedCommandHandler
{
	private static final String UPDATE_PASS = "UPDATE accounts SET password=? WHERE login = ? and password = ?";
	private static final String[] _voicedCommands = { "password" };
	private static Logger _logAudit = Logger.getLogger("audit");

	/**
	 * @see net.sf.l2j.gameserver.handler.IVoicedCommandHandler#useVoicedCommand(java.lang.String, net.sf.l2j.gameserver.model.actor.instance.L2PcInstance, java.lang.String)
	 */
	public boolean useVoicedCommand(String command, L2PcInstance activeChar, String target)
	{
		if (command.equalsIgnoreCase("password"))
		{
			StringTokenizer st = new StringTokenizer(target);
			if (st == null || st.countTokens() < 3)
			{
				activeChar.sendMessage("Use format <oldpass> <newpass> <newpass>.");
				return true;
			}
			String oldPass = st.nextToken();
			String newPass1 = st.nextToken();
			String newPass2 = st.nextToken();
			if (!newPass1.equals(newPass2))
			{
				activeChar.sendMessage("New passwords do not match.");
				return true;
			}
            
            // Encode Password - must be always in sync with login server
			Connection con = null;
			try
			{
				MessageDigest md = MessageDigest.getInstance("SHA");
				byte[] newpass;
				newpass = newPass1.getBytes("UTF-8");
				newpass = md.digest(newpass);
				byte[] oldpass;
				oldpass = oldPass.getBytes("UTF-8");
				oldpass = md.digest(oldpass);
				// Add to Base
				con = L2DatabaseFactory.getInstance().getConnection();
				PreparedStatement statement = con.prepareStatement(UPDATE_PASS);
				statement.setString(1, Base64.encodeBytes(newpass));
				statement.setString(2, activeChar.getAccountName());
				statement.setString(3, Base64.encodeBytes(oldpass));
				int numUpdates = statement.executeUpdate();
				statement.close();
				if (numUpdates == 1)
				{
					LogRecord record = new LogRecord(Level.INFO, "AUDIT: Password change");
					record.setLoggerName("audit");
					record.setParameters(new Object[] { activeChar, activeChar.getClient().getConnection().getInetAddress().toString() });
					_logAudit.log(record);
					activeChar.sendMessage("Your password has been changed.");
				}
				else
					activeChar.sendMessage("Your old password does not match.");
			}
			catch (Exception e)
			{
				//_log.error(e.getMessage());
			}
			finally
			{
				L2DatabaseFactory.close(con);
			}
		}
		return true;
	}

	/**
	 * @see net.sf.l2j.gameserver.handler.IVoicedCommandHandler#getVoicedCommandList()
	 */
	public String[] getVoicedCommandList()
	{
		return _voicedCommands;
	}
}
