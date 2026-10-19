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
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.ClientThread;
import net.sf.l2j.gameserver.model.L2Clan;
import net.sf.l2j.gameserver.model.L2PcInstance;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;

/**
 * This class ...
 * 
 * @version $Revision: 1.2.2.1.2.4 $ $Date: 2005/03/27 15:29:30 $
 */
public class RequestSetPledgeCrest extends ClientBasePacket
{
	private static final String _C__53_REQUESTSETPLEDGECREST = "[C] 53 RequestSetPledgeCrest";
	static Logger _log = Logger.getLogger(RequestSetPledgeCrest.class.getName());
			
	private final int _length;
	private final byte[] _data;
	
	public RequestSetPledgeCrest(ByteBuffer buf, ClientThread client)
	{
		super(buf, client);
		_length  = readD();
		_data = readB(_length);
	}

	void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		
		L2Clan clan = activeChar.getClan();
		if (activeChar.isClanLeader())
		{	
			if (clan.getLevel() < 3)
			{	
					SystemMessage sm = new SystemMessage(SystemMessage.CLAN_LVL_3_NEEDED_TO_SET_CREST);
					activeChar.sendPacket(sm);
				return;
			}
			
			File crestFile = new File(Config.DATAPACK_ROOT, "data/crests/Pledge_"+clan.getClanId()+".bmp");
			try {
				FileOutputStream out = new FileOutputStream(crestFile);
				out.write(_data);
				out.close();
			
				clan.setHasCrest(true);
				activeChar.broadcastUserInfo();
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
		return _C__53_REQUESTSETPLEDGECREST;
	}
}
