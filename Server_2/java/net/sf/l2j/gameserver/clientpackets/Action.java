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
	private static Logger _log = Logger.getLogger(Action.class.getName());
	
	// cddddc
	private final int _objectId;
	private final int _originX;
	private final int _originY;
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
		if (Config.DEBUG) _log.fine("Action:" + _actionId);
		if (Config.DEBUG) _log.fine("oid:" + _objectId);
		L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
			return;
		
		L2Object obj = L2World.getInstance().findObject(_objectId);
		if (	obj != null &&
				!activeChar.isAlikeDead() &&
				activeChar.getPrivateStoreType()==0 &&
				activeChar.getTransactionRequester()==null
				)
		{
			switch (_actionId)
			{
				case 0:
					obj.onAction(activeChar);
					break;
				case 1:
					obj.onActionShift(getClient());
					break;
			}
		}
		else
		{
			_log.warning("object not found, oid "+_objectId+ " or player is dead");
			activeChar.sendPacket(new ActionFailed());
		}
	}

	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.clientpackets.ClientBasePacket#getType()
	 */
	public String getType()
	{
		return ACTION__C__04;
	}
}
