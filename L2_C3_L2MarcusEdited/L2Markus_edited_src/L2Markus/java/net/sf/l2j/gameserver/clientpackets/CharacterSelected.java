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
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.serverpackets.CharSelected;
import net.sf.l2j.gameserver.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.nProtect;

/**
 * This class ...
 *
 * @version $Revision: 1.5.2.1.2.5 $ $Date: 2005/03/27 15:29:30 $
 */
public class CharacterSelected extends L2GameClientPacket
{
	private static final String _C__0D_CHARACTERSELECTED = "[C] 0D CharacterSelected";
	private static Logger _log = Logger.getLogger(CharacterSelected.class.getName());

	// cd
	private int _charSlot;

	/**
	 * @param decrypt
	 */
	public CharacterSelected(ByteBuffer buf, L2GameClient client)
	{
		super(buf, client);
		_charSlot = readD();
	}

	protected void runImpl()
	{
		// if there is a playback.dat file in the current directory, it will
		// be sent to the client instead of any regular packets
		// to make this work, the first packet in the playback.dat has to
		// be a  [S]0x21 packet
		// after playback is done, the client will not work correct and need to exit
		//playLogFile(getConnection()); // try to play log file

		// The L2PcInstance must be created here, so that it can be attached to the L2GameClient
		if (Config.DEBUG)
			_log.fine("DEBUG "+getType()+": selected slot:" + _charSlot);

		// Load up character from disk
		L2PcInstance cha = getClient().loadCharFromDisk(_charSlot);

		if (cha == null)
		{
			_log.severe("DEBUG "+getType()+": Character could not be loaded (slot:" + _charSlot + ")");
			sendPacket(new ActionFailed());
			return;
		}

		if (cha.getAccessLevel() < 0)
		{
			cha.deleteMe();
			return;
		}

        //weird but usefull, will send i..
        getClient().setActiveChar(cha);
        nProtect.getInstance().sendRequest(getClient());
        CharSelected cs = new CharSelected(cha, getClient().getSessionId().playOkID1);
		sendPacket(cs);
	}

	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.clientpackets.ClientBasePacket#getType()
	 */
	public String getType()
	{
		return _C__0D_CHARACTERSELECTED;
	}
}
