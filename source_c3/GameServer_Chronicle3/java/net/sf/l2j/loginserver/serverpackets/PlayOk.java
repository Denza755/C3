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
 * Format: dd
 * d: the first half of the session key
 * d: the second half of the session key
 * 
 * Could be written 'f' instead of 'dd' but l2j
 * only uses the 1st half of the session key, the second half
 * is just fixed
 */
public class PlayOk extends ServerBasePacket
{
	// format 	dd
	//
	// 07 
	// 34 0b 00 00 
	// 14 16 0d 00
	// align+chksum  
	// 00 00 00 00 
	// 00 00 67 07 
	// 25 1d 6a 6b 66 6b 64                          
  
	 
	
	
/*		
		(byte) 0x07,
		(byte) 0x34,(byte) 0x0b,(byte) 0x00,(byte) 0x00,	
		(byte) 0x14,(byte) 0x16,(byte) 0x0d,(byte) 0x00,
		(byte) 0x00,(byte) 0x00,(byte) 0x00,(byte) 0x00,
		(byte) 0x00,(byte) 0x00,(byte) 0x00,(byte) 0x00,
		(byte) 0x00,(byte) 0x00,(byte) 0x00,(byte) 0x00,
		(byte) 0x00,(byte) 0x00,(byte) 0x00	
	};
*/
	/**
	 * "only the lower 3 byte of the session key are used by the client" ??
	 * 
	 */
	public PlayOk(int sessionKey) 
	{
		writeC(0x07);
		writeD(sessionKey);
		writeD(0x55667788); //FIXME: shoudln't we also write the second part of the session key? (would be a bit more secure..) 
	}
	
	public byte[] getContent()
	{
		return getBytes();
	}
}
