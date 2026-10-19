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
import net.sf.l2j.gameserver.serverpackets.ValidateLocation;
/**
 * This class ...
 * 
 * @version $Revision: 1.13.4.7 $ $Date: 2005/03/27 15:29:30 $
 */
public class ValidatePosition extends ClientBasePacket
{
	private static Logger _log = Logger.getLogger(ValidatePosition.class.getName());
	private static final String _C__48_VALIDATEPOSITION = "[C] 48 ValidatePosition";

	private final int _x;
	private final int _y;
	private final int _z;
	private final int _heading;
	private final int _data;
	/**
	 * packet type id 0x48
	 * format:		cddddd
	 * @param decrypt
	 */
	public ValidatePosition(ByteBuffer buf, ClientThread client)
	{
		super(buf, client);
		_x  = readD();
		_y  = readD();
		_z  = readD();
		_heading  = readD();
		_data  = readD();
	}

	void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar != null)
		{
			int realX = activeChar.getX();		
			int realY = activeChar.getY();		
			int realZ = activeChar.getZ();		
			int realHeading = activeChar.getHeading();
			
			//TODO we have to trust the client on the z-coord right now,
			// so just copy the value we got back
			activeChar.setXYZ(realX, realY, _z);
			
			if (Config.DEBUG) {
				_log.fine("client pos: "+ _x + " "+ _y + " "+ _z +" head "+ _heading);
				_log.fine("server pos: "+ realX + " "+realY+ " "+realZ +" head "+realHeading);
			}
			
			if (Config.DEVELOPER)
			{
			int dx = _x - realX;
			int dy = _y - realY;
			int diff2 = (dx*dx + dy*dy);
			if (diff2 > 1000000) {
				if (Config.DEBUG) _log.fine("client/server dist diff "+ (int)Math.sqrt(diff2));
				sendPacket(new ValidateLocation(activeChar));
			}
			}
			
			activeChar.updateKnownObjects();
//			// check for objects that are now out of range
//			activeChar.updateKnownCounter += 1;
//			if (activeChar.updateKnownCounter >3)
//			{
//				int delete = 0;
//				Iterator<L2Object> known = activeChar.iterateKnownObjects();
//				ArrayList<L2Object> toBeDeleted = new ArrayList<L2Object>();
//				
//				while (known.hasNext())
//				{
//					L2Object obj = known.next();
//					if (distance(activeChar, obj) > 4000*4000)
//					{
//						toBeDeleted.add(obj);
//						delete++;
//					}
//				}
//				
//				if (delete >0)
//				{
//					for (int i = 0; i < toBeDeleted.size(); i++)
//					{
//						L2Object obj = toBeDeleted.get(i);
//						activeChar.removeKnownObject(obj);
//						obj.removeKnownObject(activeChar);
//						
//					}
//					if (Config.DEBUG) _log.fine("deleted " +delete+" objects");
//				}
//				
//				
//				// check for new objects that are now in range
//				int newObjects = 0;
//				L2Object[] visible = L2World.getInstance().getVisibleObjects(activeChar, 3000);
//				for (int i = 0; i < visible.length; i++)
//				{
//					if (! activeChar.knownsObject(visible[i]))
//					{
//						activeChar.addKnownObject(visible[i]);
//						visible[i].addKnownObject(activeChar);
//						newObjects++;
//					}
//				}
//				
//				if (newObjects >0)
//				{
//					if (Config.DEBUG) _log.fine("added " + newObjects + " new objects");
//				}
//				activeChar.updateKnownCounter = 0;	
//			}
		}
	}
	
	
	private long distance(L2Object a, L2Object b)
	{
		long dX = a.getX() - b.getX();
		long dY = a.getY() - b.getY();
		return ( dX*dX + dY*dY );
	}
	
	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.clientpackets.ClientBasePacket#getType()
	 */
	public String getType()
	{
		return _C__48_VALIDATEPOSITION;
	}
	
}
