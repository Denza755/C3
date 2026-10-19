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
import net.sf.l2j.gameserver.ClientThread;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2PcInstance;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.serverpackets.ActionFailed;

/**
 * This class ...
 * 
 * @version $Revision: 1.7.4.4 $ $Date: 2005/03/27 18:46:19 $
 */
public class Action extends ClientBasePacket
{
	private static final String ACTION__C__04 = "[C] 04 Action";
	
	// cddddc
	private final int _objectId;
	@SuppressWarnings("unused")
	private final int _originX;
	@SuppressWarnings("unused")
	private final int _originY;
	@SuppressWarnings("unused")
	private final int _originZ;
	private final int _actionId;

	/**
	 * @param decrypt
	 */
	public Action(ByteBuffer buf, ClientThread client)
	{
		super(buf, client);
		_objectId  = readD();
		_originX   = readD();
		_originY   = readD();
		_originZ   = readD();
		_actionId  = readC();// 0 for simple click  1 for shift click
	}

	void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if(activeChar == null) return;

		L2Object obj = L2World.findObject(_objectId);
		if(obj == null || activeChar.getPrivateStoreType() != 0 || activeChar.getTransactionRequester() != null)
		{
			activeChar.sendPacket(new ActionFailed());
			return;
		}

		switch(_actionId)
		{
		case 0:
			if(obj instanceof L2PcInstance)
				if(Math.abs(activeChar.getZ() - obj.getZ()) > 300)
					return;
			obj.onAction(activeChar);
			break;
		case 1:
			if(obj instanceof L2Character && ((L2Character)obj).isAlikeDead())
				obj.onAction(activeChar);
			else
				obj.onActionShift(activeChar);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.l2j.gameserver.clientpackets.ClientBasePacket#getType()
	 */
	public String getType()
	{
		return ACTION__C__04;
	}
}
