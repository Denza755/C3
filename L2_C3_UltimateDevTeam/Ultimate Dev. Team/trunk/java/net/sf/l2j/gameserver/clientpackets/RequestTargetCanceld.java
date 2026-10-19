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

import net.sf.l2j.gameserver.ClientThread;
import net.sf.l2j.gameserver.ai.CtrlIntention;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.packets.serverpackets.TargetUnselected;

import java.nio.ByteBuffer;
//import net.sf.l2j.gameserver.packets.serverpackets.ActionFailed;

/**
 * This class ...
 * 
 * @version $Revision: 1.3.4.2 $ $Date: 2005/03/27 15:29:30 $
 */
public class RequestTargetCanceld extends ClientBasePacket
{
	private static final String _C__37_REQUESTTARGETCANCELD = "[C] 37 RequestTargetCanceld";
    
    private final int _unk1; 

	/**
	 * packet type id 0x37
	 * packet format rev656  ch
	 * @param rawPacket
	 */
	public RequestTargetCanceld(ByteBuffer buf, ClientThread client)
	{
		super(buf, client);
        //TODO analyse value unk1
        _unk1 = readH();
	}

	void runImpl()
	{
		L2Character activeChar = getClient().getActiveChar();
        if (activeChar != null)
        {
            if (activeChar.getTarget() != null)
            {
                sendPacket(new TargetUnselected(activeChar));
        
                activeChar.setTarget(null);
            }
	    activeChar.abortAttack();
	    activeChar.abortCast();
	    activeChar.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
	    //activeChar.sendPacket(new ActionFailed());
//            else
//                _log.finer("Cannot cancel, no target");
        }
	}

	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.clientpackets.ClientBasePacket#getType()
	 */
	public String getType()
	{
		return _C__37_REQUESTTARGETCANCELD;
	}
}
