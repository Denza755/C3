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
package net.sf.l2j.gameserver;

import java.nio.ByteBuffer;

/**
 * @author mkizub
 */
public abstract class BasePacket implements Cloneable {

	/** The connection this packet was received from or to be sent to. */
	Connection _connection;
	
	/** The client this packet was received from or to be sent to. */
	ClientThread _client;
	
	/** Messages are organized into Double-linked lists, maintained by
	 * network and thread scheduler threads
	 */
	BasePacket _prev, _next;
	BasePacketQueue _queue;
	
	/** A field to store ByteBuffer used to decode/encode packet. */
	protected ByteBuffer _buf;
	
	protected BasePacket()
	{
	}

	protected BasePacket(Connection con)
	{
		_connection = con;
		_client = con.getClient();
	}

	protected BasePacket(ClientThread client)
	{
		_connection = client.getConnection();
		_client = client;
	}
	
	public BasePacket setConnection(Connection con)
	{
		if (_connection == null) {
			_connection = con;
			_client = _connection.getClient();
			return this;
		}
		return duplicate(con);
	}
	
	public final Connection getConnection()
	{
		return _connection;
	}
	
	public final ClientThread getClient()
	{
		return _client;
	}
	
	public BasePacket duplicate(Connection con)
	{
		try {
			BasePacket bp = (BasePacket)super.clone();
			bp._connection = con;
			bp._client = con.getClient();
			bp._prev = null;
			bp._next = null;
			bp._queue = null;
			return bp;
		} catch (CloneNotSupportedException e) { return null; /*never happens*/}
	}

	/**
	 * just for information and debug purposes
	 * @return text for trace message
	 */
	public abstract String getType();
}
