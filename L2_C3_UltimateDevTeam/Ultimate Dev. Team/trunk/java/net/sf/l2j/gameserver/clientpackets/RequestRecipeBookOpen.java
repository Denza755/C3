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
import net.sf.l2j.gameserver.RecipeController;

import java.nio.ByteBuffer;

public class RequestRecipeBookOpen extends ClientBasePacket 
{
    private static final String _C__AC_REQUESTRECIPEBOOKOPEN = "[C] AC RequestRecipeBookOpen";
    @SuppressWarnings("unused")
    private final int _unk1;

	/**
	 * packet type id 0xac
	 * packet format rev656  cd
	 * @param decrypt
	 */
	public RequestRecipeBookOpen(ByteBuffer buf, ClientThread client)
	{
		super(buf, client);
		_unk1 = readD();
	}

	void runImpl()
	{
	if (getClient().getActiveChar() == null)
	    return;
        if (getClient().getActiveChar().getPrivateStoreType() != 0)
        {
        		getClient().getActiveChar().sendMessage("Cannot use recipe book while trading");
        }
        RecipeController.getInstance().requestBookOpen(getClient().getActiveChar(),true);
	}
	
    /* (non-Javadoc)
     * @see net.sf.l2j.gameserver.clientpackets.ClientBasePacket#getType()
     */
    public String getType() 
    {
        return _C__AC_REQUESTRECIPEBOOKOPEN;
    }
}
