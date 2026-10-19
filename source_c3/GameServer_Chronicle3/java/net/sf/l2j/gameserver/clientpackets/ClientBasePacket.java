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
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.BasePacket;
import net.sf.l2j.gameserver.ClientThread;
import net.sf.l2j.gameserver.TaskPriority;
import net.sf.l2j.gameserver.serverpackets.ServerBasePacket;

/**
 * This class ...
 * 
 * @version $Revision: 1.5.4.9 $ $Date: 2005/03/27 15:29:30 $
 */
public abstract class ClientBasePacket extends BasePacket implements Runnable
{
	private static Logger _log = Logger.getLogger(ClientBasePacket.class.getName());
	
	protected ClientBasePacket(ByteBuffer buf, ClientThread client)
	{
		super(client);
		if (Config.DEBUG) _log.fine(getType()+" <<< "+client.getLoginName());
		_buf = buf;
		if (Config.ASSERT) assert _buf.position() == 1;
	}

	/** urgent messages are executed immediatly */
	public TaskPriority getPriority() { return TaskPriority.PR_NORMAL; }
	
	public final void run()
	{
		//assert _isValid;
		try
		{
			runImpl();
		}
		catch (Throwable e)
		{
			_log.log(Level.SEVERE, "error handling client message "+getType(), e);
		}
		
	}

	abstract void runImpl();
	
	protected void sendPacket(ServerBasePacket msg)
	{
		getConnection().sendPacket(msg);
	}

	public final int readD()
	{
		return _buf.getInt();
	}

	public final int readC()
	{
		return _buf.get() & 0xFF;
	}

	public final int readH()
	{
		return _buf.getShort() & 0xFFFF;
	}

	public final double readF()
	{
		return _buf.getDouble();
	}

	public final String readS()
	{
		StringBuffer sb = new StringBuffer();
		char ch;
		while ((ch = _buf.getChar()) != 0)
			sb.append(ch);
		return sb.toString();
	}
	
	public final byte[] readB(int length)
	{
		byte[] result = new byte[length];
		_buf.get(result);
		return result;
	}
}
