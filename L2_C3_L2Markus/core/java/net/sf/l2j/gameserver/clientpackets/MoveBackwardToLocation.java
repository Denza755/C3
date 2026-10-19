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
package net.sf.l2j.gameserver.clientpackets;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.TaskPriority;
import net.sf.l2j.gameserver.ai.CtrlIntention;
import net.sf.l2j.gameserver.serverpackets.EnchantResult;
import net.sf.l2j.gameserver.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.serverpackets.StopMove;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.L2CharPosition;
import net.sf.l2j.Config;

@SuppressWarnings("unused")
public class MoveBackwardToLocation extends L2GameClientPacket
{
    private static final String _C__01_MOVEBACKWARDTOLOC = "[C] 01 MoveBackwardToLoc";
    private int _targetX, _targetY, _targetZ, _originX, _originY, _originZ, _moveMovement;
	private int _curX, _curY, _curZ; // for geodata

	public TaskPriority getPriority()
	{
		return TaskPriority.PR_HIGH;
	}

	public MoveBackwardToLocation(ByteBuffer buf, L2GameClient client)
	{
		super(buf, client);
		_targetX = readD();
		_targetY = readD();
		_targetZ = readD();
		_originX = readD();
		_originY = readD();
		_originZ = readD();

		try
		{
			_moveMovement = readD(); // is 0 if cursor keys are used  1 if mouse is used
		}
		catch (BufferUnderflowException e)
		{
		}
	}

    @Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();

		if(activeChar == null)
			return;

		// Like L2OFF movements prohibited when char is sitting
		if(activeChar.isSitting()){
			sendPacket(new ActionFailed());
			return;
		}

		// Like L2OFF the enchant window will close
		if(activeChar.getActiveEnchantItem() != null){
            activeChar.sendPacket(new EnchantResult(0));
            activeChar.setActiveEnchantItem(null);
		}

		if (_targetX == _originX && _targetY == _originY && _targetZ == _originZ)
		{
			activeChar.sendPacket(new StopMove(activeChar));
			return;
		}

		// Correcting targetZ from floor level to head level
		_targetZ += activeChar.getTemplate().collisionHeight;

		_curX = activeChar.getX();
		_curY = activeChar.getY();
		_curZ = activeChar.getZ();

		if (activeChar.getTeleMode() > 0)
		{
			if (activeChar.getTeleMode() == 1)
				activeChar.setTeleMode(0);

			activeChar.sendPacket(new ActionFailed());
			activeChar.teleToLocation(_targetX, _targetY, _targetZ, false);
			return;
		}

		if (_moveMovement == 0 && !Config.ALLOW_USE_CURSOR_FOR_WALK)
			activeChar.sendPacket(new ActionFailed());
		else
		{
			double dx = _targetX - _curX;
			double dy = _targetY - _curY;

			// Can't move if character is confused, or trying to move a huge distance
			if (activeChar.isOutOfControl() || dx * dx + dy * dy > 98010000) // 9900*9900
			{
				activeChar.sendPacket(new ActionFailed());
				return;
			}
			activeChar.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new L2CharPosition(_targetX, _targetY, _targetZ, 0));
		}
	}

	@Override
	public String getType()
	{
		return _C__01_MOVEBACKWARDTOLOC;
	}
}