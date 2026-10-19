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
package net.sf.l2j.loginserver;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.ClientScheduler;
import net.sf.l2j.loginserver.IpManager;
import net.sf.l2j.loginserver.clientpackets.RequestAuthLogin;
import net.sf.l2j.loginserver.clientpackets.RequestServerLogin;
import net.sf.l2j.loginserver.serverpackets.GGAuth;
import net.sf.l2j.loginserver.serverpackets.Init;
import net.sf.l2j.loginserver.serverpackets.LoginFail;
import net.sf.l2j.loginserver.serverpackets.LoginOk;
import net.sf.l2j.loginserver.serverpackets.PlayOk;
import net.sf.l2j.loginserver.serverpackets.ServerBasePacket;
import net.sf.l2j.loginserver.serverpackets.ServerList;

/**
 * This class ...
 * 
 * @version $Revision: 1.15.2.5.2.5 $ $Date: 2005/04/06 16:13:46 $
 */
public class ClientThread extends Thread
{
	static Logger _log = Logger.getLogger(ClientThread.class.getName());
	
	private InputStream _in;
	private OutputStream _out;
	private NewCrypt _crypt;
	private Logins _logins;
	
	private Socket _csocket;
	private String _gameServerHost;
	private int _gameServerPort;
    protected static List<String> _bannedIPs = new ArrayList<String>();
    
	
    public ClientThread(Socket client, Logins logins, String host, int port) throws IOException
	{
    	super("Login Client "+client.getInetAddress());
    	String ip = client.getInetAddress().getHostAddress();
    	if (IpManager.getInstance().CheckIp(ip) || _bannedIPs.contains(ip)) {
    		client.close();
    		_log.warning("Drop connection from banned IP: " + ip);
    		ip=null;
    		return;
    	}

    	setDaemon(true);
    	_csocket = client;
    	_in = client.getInputStream();
    	_out = new BufferedOutputStream(client.getOutputStream());
    	_crypt = new NewCrypt("_;5.]94-31==-%xT!^[$\000");
    	_logins = logins;
    	_gameServerHost = host;
    	_gameServerPort = port;
        
		start();
	}
	
	public void run()
	{
		if (Config.DEBUG) _log.fine("loginserver thread[C] started");
		
		int lengthHi =0;
		int lengthLo =0;
		int length = 0;
		boolean checksumOk = false;
		int sessionKey = -1;
		String account = null;
		
		String gameServerIp = null;
		
		try
		{
			// external ip address gets resolved again for each new connection
			// this should help users with dynamic IPs
			InetAddress adr = InetAddress.getByName(_gameServerHost);
			gameServerIp = adr.getHostAddress();

			Init startPacket = new Init(_logins.getScrambledModulus());
			_out.write(startPacket.getLength() & 0xff);
			_out.write(startPacket.getLength() >> 8 &0xff);
			_out.write(startPacket.getContent());
			_out.flush();

			while (true)
			{
                lengthLo = _in.read();
				lengthHi = _in.read();
				length= lengthHi*256 + lengthLo;  
				
				if (lengthHi < 0 )
				{
					_log.finer("LoginServer: Client terminated the connection.");
					break;
				}
				
				byte[] incoming = new byte[length];
				incoming[0] = (byte) lengthLo;
				incoming[1] = (byte) lengthHi;
				
				int receivedBytes = 2;
				int newBytes = 0;
				while (newBytes != -1 && receivedBytes<length)
				{
					newBytes =  _in.read(incoming, receivedBytes, length-receivedBytes);
					receivedBytes = receivedBytes + newBytes;
				}
				
				if (receivedBytes != length)
				{
					_log.warning("Incomplete Packet is sent to the server, closing connection.");
					break;
				}
				
				byte[] decrypt = new byte[length - 2];
				System.arraycopy(incoming, 2, decrypt, 0, decrypt.length);
				// decrypt if we have a key
				decrypt = _crypt.decrypt(decrypt);
				checksumOk = _crypt.checksum(decrypt);
				
				if (!checksumOk)
				{
					//_log.warning("Incorrect packet checksum, closing connection..");
                    _log.warning("Client is not using latest Authentication method. (Min is " + Config.MIN_PROTOCOL_REVISION + ")");
					break;
				}
				
				if (Config.DEBUG) 
                    _log.warning("[C]\n"+printData(decrypt, decrypt.length));
				
				int packetType = decrypt[0]&0xff;
				switch (packetType)
				{
					case 00:
					{
						RequestAuthLogin ral = new RequestAuthLogin(decrypt, _logins.getPrivateKey());
						account = ral.getUser().toLowerCase();
						if (Config.DEBUG) _log.fine("RequestAuthLogin from user:" + account);
						
						LoginController lc = LoginController.getInstance();
						if (_logins.loginValid(account, ral.getPassword(), _csocket.getInetAddress()))
						{	
                            if (_logins.loginBanned(account))
                            {
                                //Login BANNED
                                LoginFail lok = new LoginFail(LoginFail.REASON_ACCOUNT_BANNED);
                                sendPacket(lok);
                            }
                            else
                            {
                                //FIXME: this has to be done per server and not on login
                                if(LoginController.getInstance().getOnlinePlayerCount() >= LoginController.getInstance().getMaxAllowedOnlinePlayers())
                                {
                                    if (Config.DEBUG) _log.fine("Player tried to login but server is full");
                                    LoginFail lfail = new LoginFail(LoginFail.REASON_ACCESS_FAILED );
                                    sendPacket(lfail);
                                }
                                else if (!lc.isAccountInGameServer(account) && !lc.isAccountInLoginServer(account) )
                                {
                                    sessionKey = lc.assignSessionKeyToLogin(account, _csocket);
                                    if (Config.DEBUG) _log.fine("assigned SessionKey:" + Integer.toHexString(sessionKey));
                                    if(Config.SHOW_LICENCE)
                                    {
                                    LoginOk lok = new LoginOk();
                                    sendPacket(lok);
                                    }
                                    else
                                    {
                                    	ServerList sl = new ServerList();
                						int current = LoginController.getInstance().getOnlinePlayerCount();
                						int max = LoginController.getInstance().getMaxAllowedOnlinePlayers();
                						sl.addServer(gameServerIp, _gameServerPort, true, true, current, max);
                						sl.addServer(gameServerIp, _gameServerPort, true, false, current, max);
                						sendPacket(sl);
                                    }
                                }
                                else
                                {
                                    if (Config.DEBUG) _log.fine("KICKING!");
                                    if (lc.isAccountInLoginServer(account)) {
                                        _log.warning("Account is in use on Login server (kicking off): " + account);
                                        try
                                        {
                                            lc.getLoginServerConnection(account).close();
                                            lc.removeLoginServerLogin(account);
                                        }
                                        catch(NullPointerException e)
                                        {
                                            _log.warning("Error while kicking loginserver account, lost connection?");
                                        }
                                    }
                                    if (lc.isAccountInGameServer(account)) {
                                        _log.warning("Account is in use on Game server (kicking off): " + account);
                                        lc.getClientConnection(account).close();
                                        lc.removeGameServerLogin(account);
                                    }
                                    LoginFail lok = new LoginFail(LoginFail.REASON_ACCOUNT_IN_USE);
                                    sendPacket(lok);
                                }
                            }
						}
						else
						{
							LoginFail lok = new LoginFail(LoginFail.REASON_USER_OR_PASS_WRONG);
							sendPacket(lok);
							if (Config.DEBUG) _log.fine("login failed sent");
						}

						break;
					}
					
					case 02:
					{
						if (Config.DEBUG) _log.fine("RequestServerLogin");
						RequestServerLogin rsl = new RequestServerLogin(decrypt);
						if (Config.DEBUG) _log.fine("login to server:" + rsl.getData3());
						
						//PlayFail po = new PlayFail(PlayFail.REASON_TOO_MANY_PLAYERS);
						PlayOk po = new PlayOk(sessionKey);
						sendPacket(po);
						break;
					}
					
					case 05:
					{
						if (Config.DEBUG) _log.fine("RequestServerList");
						//RequestServerList rsl = new RequestServerList(decrypt);
						
						ServerList sl = new ServerList();
						int current = LoginController.getInstance().getOnlinePlayerCount();
						int max = LoginController.getInstance().getMaxAllowedOnlinePlayers();
						sl.addServer(gameServerIp, _gameServerPort, true, true, current, max);
						sl.addServer(gameServerIp, _gameServerPort, true, false, current, max);
						sendPacket(sl);
						break;
					}
					
					case 07:
					{
						_log.warning("Client requested GG Auth");
						GGAuth GGAuthPacket = new GGAuth(GGAuth.SKIP_GG_AUTH_REQUEST);
						sendPacket(GGAuthPacket);
						break;
					}

					default:
					{
						_log.warning("Unknown Packet:" + packetType);
						_log.warning(printData(decrypt, decrypt.length));
						break;
					}
				}
			}
		}
		catch(SocketTimeoutException e)
		{
			_log.info("Loginserver.ClientThread: client socket timeout, disconnected.");
		}
		catch(SocketException e)
		{
// 			Changed it to fine to stop spamming the console.
			_log.fine("Connection closed unexpectedly.");
		}
		catch(HackingException e)
		{
			addBannedIP(e.getIP(), e.getConnects());
		}
		catch(Exception e)
		{
			_log.log(Level.INFO, "", e);
			e.printStackTrace();
		}
		finally
		{
			try
			{
				_csocket.close();
			}
			catch (Exception e1) {}	// ignore problems
			
			LoginController.getInstance().removeLoginServerLogin(account);
			if (Config.DEBUG) _log.fine("loginserver thread[C] stopped");
		}
	}
	
	/**
	 * @param sl
	 * @throws IOException
	 */
	private void sendPacket(ServerBasePacket sl) throws IOException
	{
		byte[] data = sl.getContent();
		_crypt.checksum(data);
		if (Config.DEBUG) _log.finest("[S]\n"+printData(data, data.length));
		data = _crypt.crypt(data);

		int len = data.length+2;
		_out.write(len & 0xff);
		_out.write(len >> 8 &0xff);
		_out.write(data);
		_out.flush();
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

	public void addBannedIP(String ip, int incorrectCount)
	{
        
		_bannedIPs.add(ip);
        //IpManager.getInstance().BanIp(ip,"Hack Protection");
        int time = incorrectCount * incorrectCount * 1000;
        ClientScheduler.getInstance().scheduleMed(new UnbanTask(ip), time);
        _log.warning("Banning ip: "+ip+" for "+time/1000.0+" seconds.");
	}

    /*public static void addBannedIP(String ip)
    {
        //_bannedIPs.add(ip);
        IpManager.getInstance().BanIp(ip,"");
    }*/

    public class UnbanTask implements Runnable
    {
        public String _ip;
        public UnbanTask(String IP)
        {
            _ip = IP;
        }
        public void run()
        {
            _bannedIPs.remove(_ip);
            //IpManager.getInstance().UnbanIp(_ip);
    	    _log.fine("Remove ban for ip: " +_ip);
        }
    }
}
