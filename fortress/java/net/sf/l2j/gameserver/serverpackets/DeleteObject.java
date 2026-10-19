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
package net.sf.l2j.gameserver.serverpackets;

import java.util.logging.Logger;

import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2World;

/**
 * sample
 * 0000: 1e  9b da 12 40                                     ....@
 *  
 * format  d 
 * 
 * @version $Revision: 1.3.2.1.2.3 $ $Date: 2005/03/27 15:29:39 $
 */
public class DeleteObject extends ServerBasePacket
{
	private static Logger _log = Logger.getLogger(DeleteObject.class.getName());

	private static final String _S__1E_DELETEOBJECT = "[S] 12 DeleteObject";
	private int _objectId;

	protected boolean logHandled() { return true;}

	public DeleteObject(L2Object obj)
	{
		_objectId = 0;
		if(getClient()!=null && getClient().getActiveChar() == obj)
		{
			_log.warning("Try self.DeleteObject for "+getClient().getActiveChar());
			Thread.dumpStack();
			return;
		}
		_objectId = obj.getObjectId();
	}
	
	public DeleteObject(Integer oid)
	{
		_objectId = 0;
		if(getClient()!=null && getClient().getActiveChar().getObjectId() == oid)
		{
			_log.warning("Try self.DeleteObject for "+getClient().getActiveChar());
			Thread.dumpStack();
			return;
		}
		_objectId = oid;
	}

	final void runImpl()
	{
		// no long-running tasks
	}
	
	final void writeImpl()
	{
		writeC(0x12);
		writeD(_objectId);
		writeD(0x00); //c2
	}
	
	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.serverpackets.ServerBasePacket#getType()
	 */
	public String getType()
	{
		return _S__1E_DELETEOBJECT+" "+L2World.findObject(_objectId)+" ("+_objectId+")";
	}

}
