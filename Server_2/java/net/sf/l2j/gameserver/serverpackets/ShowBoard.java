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

import net.sf.l2j.gameserver.model.L2PcInstance;

public class ShowBoard extends ServerBasePacket
{
	private static final String _S__6E_SHOWBOARD = "[S] 6e ShowBoard";
	private L2PcInstance _player;
	private String _htmlCode;
	
	public ShowBoard(L2PcInstance player, String htmlCode)
	{
		_player = player;
		_htmlCode = htmlCode;
	}
	
	final void runImpl()
	{
		// no long-running tasks
	}
	
	final void writeImpl()
	{
		writeC(0x6e);
		writeS("bypass bbs_top"); // top
		writeS("bypass bbs_favorite"); // favorite
		writeS("bypass bbs_region"); // region
		writeS(_player.getClan() != null ? "bypass bbs_clan" : "bypass bbs_default"); // clan
		writeS("bypass bbs_memo"); // memo
		writeS("bypass bbs_mail"); // mail
		writeS("bypass bbs_friends"); // friends
		writeS("bypass bbs_add_fav"); // add fav.
		writeS(_htmlCode); // current page
	}

	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.serverpackets.ServerBasePacket#getType()
	 */
	public String getType()
	{
		return _S__6E_SHOWBOARD;
	}
}
