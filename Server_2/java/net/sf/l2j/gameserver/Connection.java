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

import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.clientpackets.ClientBasePacket;
import net.sf.l2j.gameserver.serverpackets.ServerBasePacket;
import net.sf.l2j.gameserver.serverpackets.WrappedMessage;

/**
 * This class ...
 * 
 * @version $Revision: 1.8.4.9 $ $Date: 2005/04/06 16:13:38 $
 */
public final class Connection
{
	private static Logger _log = Logger.getLogger(Connection.class.getName());

	private Crypt _inCrypt;
	private Crypt _outCrypt;

	private byte[] _cryptkey;

	private final Socket _csocket;
	private final SocketChannel _chanell;
	
	// Data for NIO, non-blocking IO
	/** Used by network to store the very first byte from incoming message */
	byte readBufferFirstByte;
	/** Used by network to store read buffer for uncomplete messages */
	ByteBuffer readBuffer;
	/** Used by network to store read buffer for uncomplete writes */
	ByteBuffer writeBuffer;
	/** Queue of received messages */
	final ConcurrentLinkedQueue<ClientBasePacket> _receivedMsgQueue;
	/** Queue of messages to be sent, if writeBuffer is not null */
	final BasePacketQueue _sendMsgQueue;
	/** This is a protection timestamp, for last time of writing into the
	 * network buffer, if it's full. The connection will be closed if
	 * we cannot write nothing for 30 seconds
	 */
	int writeTimeStamp;
	/** The client that owns this connection */
	final ClientThread _client;
	
	public Connection(ClientThread client, Socket socket, byte[] cryptKey)
	{
		_client = client;
		_csocket = socket;
		_chanell = _csocket.getChannel();
		_receivedMsgQueue = new ConcurrentLinkedQueue<ClientBasePacket>();
		_sendMsgQueue = new BasePacketQueue();
		_inCrypt = new Crypt();
		_outCrypt = new Crypt();
		_cryptkey = cryptKey;	// this is defined here but it is not used before key packet was send 
	}
	
	public ClientThread getClient()
	{
		return _client;
	}

	/** Put a message received by NIO's thread
	 * Notifies all threads, that wait() on this Connection
	 */
	public synchronized void addReceivedMsg(ByteBuffer buf) {
		if (Config.ASSERT) assert Thread.currentThread() == SelectorThread.instance;
		ClientBasePacket pkt = PacketHandler.handlePacket(buf, _client);
		if (pkt != null) {
			if (pkt.getPriority() == TaskPriority.PR_URGENT)
			{
				pkt.run();
			}
			else
			{
				ClientScheduler.addReceivedMsg(pkt);
			}
		}
	}
	
	/** Get next message, received received by NIO's thread, if any.
	 * @return next message, or null if queue is empty.
	 */
	synchronized ClientBasePacket getNextReceivedMsg()
	{
		if (Config.ASSERT) assert Thread.currentThread() != SelectorThread.instance;
		if (_receivedMsgQueue.isEmpty())
			return null;
		return _receivedMsgQueue.remove();
	}
	
	/**
	 * This method will be called indirectly by several threads, to notify
	 * one client about all parallel events in the world.
	 * it has to be either synchronized like this, or it might be changed to 
	 * stack packets in a outbound queue. 
	 * advantage would be that the calling thread is independent of the amount
	 * of events that the target gets.
	 * if one target receives hundreds of events in parallel, all event sources
	 * will have to wait until the packets are send... 
	 * for now, we use the direct communication
	 * @param data
	 * @throws IOException
	 * @deprecated
	 */
	public void sendPacket(byte[] data)
	{
		// this is time consuming.. only enable for debugging
		if (Config.DEBUG && _log.isLoggable(Level.FINEST)) {
			_log.finest("\n" + printData(data, data.length));
		}

		WrappedMessage msg = new WrappedMessage(data, this);
		SelectorThread.instance.sendMessage(msg);
	}

	public void sendPacket(ServerBasePacket bp)
	{
		bp = (ServerBasePacket)bp.setConnection(this);
		SelectorThread.instance.sendMessage(bp);
	}

	public void activateCryptKey()
	{
		_inCrypt.setKey(_cryptkey);
		_outCrypt.setKey(_cryptkey);
	}
	
	public void decript(ByteBuffer b) {
		_inCrypt.decrypt(b);
	}
	public void encript(ByteBuffer b) {
		_outCrypt.encrypt(b);
	}

	/**
	 * this only gives the correct result if the cryptkey is not yet activated
	 */
	public byte[] getCryptKey()
	{
		return _cryptkey;
	}

	/**
	 * 
	 */
	public void close()
	{
		try {
			if (_csocket != null && _csocket.isConnected())
				_csocket.close();
		} catch (IOException e) {}
		try {
			if (_client.getActiveChar() != null)
				_client.onDisconnect();
		} catch (Throwable t) {_log.log(Level.WARNING, "", t);}
	}
	
	/**
	 * 
	 */
	public SocketChannel getChannel()
	{
		return _chanell;
	}
	
	private String printData(byte[] data, int len)
	{ 
		StringBuffer result = new StringBuffer();
		
		int counter = 0;
		
		for (int i=0;i< len;i++)
		{
			if (counter % 16 == 0)
			{
				result.append(fillHex(i,4)+": ");
			}
			
			result.append(fillHex(data[i] & 0xff, 2) + " ");
			counter++;
			if (counter == 16)
			{
				result.append("   ");
				
				int charpoint = i-15;
				for (int a=0; a<16;a++)
				{
					int t1 = data[charpoint++];
					if (t1 > 0x1f && t1 < 0x80)
					{
						result.append((char)t1);
					}
					else
					{
						result.append('.');
					}
				}
				
				result.append("\n");
				counter = 0;
			}
		}

		int rest = data.length % 16;
		if (rest > 0 )
		{
			for (int i=0; i<17-rest;i++ )
			{
				result.append("   ");
			}

			int charpoint = data.length-rest;
			for (int a=0; a<rest;a++)
			{
				int t1 = data[charpoint++];
				if (t1 > 0x1f && t1 < 0x80)
				{
					result.append((char)t1);
				}
				else
				{
					result.append('.');
				}
			}

			result.append("\n");
		}

		
		return result.toString();
	}
	
	private String fillHex(int data, int digits)
	{
		String number = Integer.toHexString(data);
		
		for (int i=number.length(); i< digits; i++)
		{
			number = "0" + number;
		}
		
		return number;
	}
}
