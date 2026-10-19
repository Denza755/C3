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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.sql.PreparedStatement;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.ClientThread;
import net.sf.l2j.gameserver.model.L2Clan;
import net.sf.l2j.gameserver.model.L2PcInstance;

/**
 * This class ...
 * 
 * @version $Revision: 1.2.2.1.2.4 $ $Date: 2005/03/27 15:29:30 $
 */
public class RequestSetAllyCrest extends ClientBasePacket
{
	private static final String _C__87_REQUESTSETALLYCREST = "[C] 87 RequestSetAllyCrest";
	static Logger _log = Logger.getLogger(RequestSetPledgeCrest.class.getName());
			
	private final int _length;
	private final byte[] _data;
	
	public RequestSetAllyCrest(ByteBuffer buf, ClientThread client)
	{
		super(buf, client);
		_length  = readD();
		_data = readB(_length);
	}

	void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if(activeChar == null) return;

		L2Clan clan = activeChar.getClan();
		if (activeChar.isAllyLeader())
		{	
			File crestFile = new File("./", "data/crests/allycrest_"+clan.getAllyId()+".bmp");
			try {
				FileOutputStream out = new FileOutputStream(crestFile);
				out.write(_data);
				out.close();
				clan.getAlliance().setHasAllyCrest(true);
				for (L2Clan temp : clan.getAlliance().getMembers())
				{
					clan.getAlliance().setAllyCrestId(clan.getAllyId());
					for (L2PcInstance member : temp.getOnlineMembers("")) 
						member.broadcastUserInfo();
					clan.broadcastClanStatus();
				}
				java.sql.Connection con = null;
				try
				{
					con = L2DatabaseFactory.getInstance().getConnection();
					PreparedStatement statement = con.prepareStatement("UPDATE ally_data SET ally_crestid=? WHERE ally_id=?");
					statement.setInt(1, clan.getAlliance().getAllyCrestId());
					statement.setInt(2, clan.getAllyId());
					statement.execute();
					statement.close();
				}
				catch (Exception e)
				{
					_log.warning("error while saving new ally crest to db "+e);
				}
				finally
				{
					try { con.close(); } catch (Exception e) {}
				}
			} catch (IOException e) {
					_log.log(Level.INFO, "Error loading "+crestFile+":", e);
			}
		}
	}

	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.clientpackets.ClientBasePacket#getType()
	 */
	public String getType()
	{
		return _C__87_REQUESTSETALLYCREST;
	}
}