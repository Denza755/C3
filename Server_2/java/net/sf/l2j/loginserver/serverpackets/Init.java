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
package net.sf.l2j.loginserver.serverpackets;

/**
 * This class ...
 * 
 * @version $Revision: 1.3.4.1 $ $Date: 2005/03/27 15:30:10 $
 */
public class Init
{
	// format	dd
	//
	private static byte[] _content = {
		(byte)0x00, (byte)0x9c, (byte)0x77, (byte)0xed, 
		(byte)0x03, (byte)0x5a, (byte)0x78, (byte)0x00,
		(byte)0x00
	}; 

	
	public byte[] getContent()
	{
		return _content;
	}
	
	public int getLength()
	{
		return _content.length+2;
	}
}
