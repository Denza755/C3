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

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.util.logging.Logger;

import net.sf.l2j.gameserver.ClientThread;
import net.sf.l2j.gameserver.ai.CtrlIntention;
import net.sf.l2j.gameserver.model.L2CharPosition;
import net.sf.l2j.gameserver.model.L2PcInstance;
import net.sf.l2j.gameserver.serverpackets.ActionFailed;
//import net.sf.l2j.gameserver.serverpackets.AttackCanceld;
/**
 * This class ...
 * 
 * @version $Revision: 1.11.2.4.2.4 $ $Date: 2005/03/27 15:29:30 $
 */
public class MoveBackwardToLocation extends ClientBasePacket
{
	private static Logger _log = Logger.getLogger(MoveBackwardToLocation.class.getName());
	// cdddddd
	private final int _targetX;
	private final int _targetY;
	private final int _targetZ;
	private final int _originX;
	private final int _originY;
	private final int _originZ;
	private       int _moveMovement; 

	private static final String _C__01_MOVEBACKWARDTOLOC = "[C] 01 MoveBackwardToLoc";
	/**
	 * packet type id 0x01
	 * @param decrypt
	 */
	public MoveBackwardToLocation(ByteBuffer buf, ClientThread client)
	{
		super(buf, client);
		_targetX  = readD();
		_targetY  = readD();
		_targetZ  = readD();
		_originX  = readD();
		_originY  = readD();
		_originZ  = readD();
		if (client.getRevision() == 484) {
            _moveMovement = 1;
		} else {
			try {
				_moveMovement = readD(); // is 0 if cursor keys are used  1 if mouse is used
			} catch (BufferUnderflowException e) {
				_log.warning("Incompatible client found: L2Walker? "+client.getActiveChar());
			}
		}
	}

	void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
			return;
		if (activeChar.isCastingNow() || activeChar.isAttackingNow() || _moveMovement == 0) //  cursor movement is temporary disabled 
		{
			activeChar.sendPacket(new ActionFailed());
		} 
		else 
		{
			// Can't move if character is confused
			if (activeChar.isOutOfControl()) {
				activeChar.sendPacket(new ActionFailed());
				return;
			}
			activeChar.setXYZ(_originX, _originY, _originZ);
			activeChar.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO,
					new L2CharPosition(_targetX, _targetY, _targetZ, 0));
		}		
	}

	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.clientpackets.ClientBasePacket#getType()
	 */
	public String getType()
	{
		return _C__01_MOVEBACKWARDTOLOC;
	}
}
