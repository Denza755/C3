package net.sf.l2j.status;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.ArrayList;
import java.util.logging.Logger;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.Announcements;
import net.sf.l2j.gameserver.ClientScheduler;
import net.sf.l2j.gameserver.GameTimeController;
import net.sf.l2j.gameserver.GmListTable;
import net.sf.l2j.gameserver.Shutdown;
import net.sf.l2j.gameserver.clientpackets.Say2;
import net.sf.l2j.gameserver.handler.admincommandhandlers.AdminTeleport;
import ru.balancer.JBForth;
import ru.balancer.jbforth.Word;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2DoorInstance;
import net.sf.l2j.gameserver.model.L2PcInstance;
import net.sf.l2j.gameserver.model.L2CastleGuardInstance;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2MonsterInstance;
import net.sf.l2j.gameserver.model.L2NpcInstance;
import net.sf.l2j.gameserver.model.L2SummonInstance;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.NpcTable;
import net.sf.l2j.gameserver.SkillTable;
import net.sf.l2j.gameserver.model.quest.Quest;
import net.sf.l2j.gameserver.serverpackets.CreatureSay;
import net.sf.l2j.loginserver.LoginController;
import net.sf.l2j.loginserver.LoginServer;
import net.sf.l2j.loginserver.IpManager;
import net.sf.l2j.loginserver.IpManager.BannedIp;

import org.apache.bsf.BSFException;
import org.apache.bsf.BSFManager;

import bsh.*;
import bsh.util.*;

public class StatusThread extends Thread
{
	private static final Logger _log = Logger.getLogger(AdminTeleport.class.getName());
	
	public	int					log_chat=0;
	public	int					log_tell=0;
	public	StatusThread next;

	private Socket			 _csocket;
	
	private PrintWriter		_print;
	private BufferedReader _read;
	
	private int					 _uptime;

	public JBForth jbf;
	
	private void telnetOutput(int type, String text)
	{
		if ( type == 1 ) System.out.println("TELNET | "+text);
		else if ( type == 2 ) System.out.print("TELNET | "+text);
		else if ( type == 3 ) System.out.print(text);
		else if ( type == 4 ) System.out.println(text);
		else System.out.println("TELNET | "+text);
	}
	
	private boolean isValidIP(Socket client)
	{
		boolean result = false;		
		InetAddress ClientIP = client.getInetAddress();
				 
		// convert IP to String, and compare with list
		String clientStringIP = ClientIP.getHostAddress();
		
		telnetOutput(1, "Connection from: "+clientStringIP);
		
		// read and loop thru list of IPs, compare with newIP
		if ( Config.DEVELOPER ) telnetOutput(2, "");	

		try
		{
			Properties telnetSettings = new Properties();
			InputStream telnetIS = getClass().getResourceAsStream(Config.TELNET_FILE);
			telnetSettings.load(telnetIS);
			telnetIS.close();
			
			String HostList = telnetSettings.getProperty("ListOfHosts", "127.0.0.1,localhost");
			
			if ( Config.DEVELOPER ) telnetOutput(3, "Comparing ip to list...");
			
			// compare
			String ipToCompare = null;
			for (String ip:HostList.split(","))
			{
				if ( !result )
				{
					ipToCompare = InetAddress.getByName(ip).getHostAddress();
					if ( clientStringIP.equals(ipToCompare) ) result = true;
					if ( Config.DEVELOPER ) telnetOutput(3, clientStringIP + " = " + ipToCompare + "("+ip+") = " + result);
				}
			}	
		}
		catch ( IOException e)
		{
			if ( Config.DEVELOPER ) telnetOutput(4, "");
			telnetOutput(1, "Error: "+e);
		}

		if ( Config.DEVELOPER ) telnetOutput(4, "Allow IP: "+result);
		return result;
	}

	public StatusThread(Socket client, int uptime, String StatusPW) throws IOException
	{
		jbf = new JBForth(this);

		_csocket = client;
		this._uptime = uptime;

		_print = new PrintWriter(_csocket.getOutputStream());
		_read	= new BufferedReader(new InputStreamReader(_csocket.getInputStream()));

		if ( isValidIP(client) )
		{
			telnetOutput(1, client.getInetAddress().getHostAddress()+" accepted.");
			_print.println("Welcome To The L2J Telnet Session.");
			_print.println("Please Insert Your Password!");
			_print.print("Password: ");
			_print.flush();
			String tmpLine = _read.readLine();
			if ( tmpLine == null )
			{
				_print.println("Error.");
				_print.println("Disconnected...");
				_print.flush();
				_csocket.close();
			}
			else
			{
				if (tmpLine.compareTo(StatusPW) != 0)
				{
					_print.println("Incorrect Password!");
					_print.println("Disconnected...");
					_print.flush();
					_csocket.close();
				}
				else
				{
					_print.println("Password Correct!");
					_print.println("[L2J]");
					_print.print("");
					_print.flush();

					start();
				}
			}
		}
		else
		{
			telnetOutput(1, "Connection attempt from "+ client.getInetAddress().getHostAddress() +" rejected.");
			_csocket.close();
		}
	}
		
	@SuppressWarnings("static-access")
	public void run()
	{
		next = Status.telnetlist;
		Status.telnetlist = this;

		String _usrCommand = "";
	
		try
		{
			while (_usrCommand.compareTo("quit") != 0 && _usrCommand.compareTo("exit") != 0)
			{
				jbf.uvset("telnet?", "-1");
				_usrCommand = _read.readLine();
				if(_usrCommand == null)
				{
					_csocket.close();
					break;
				}
				String command = _usrCommand.trim();
				String word = (command.split("\\s+"))[0];
				String args = command.substring(word.length()).trim();
	
				// chect telnet_* jbf words
				Word wt = jbf.find_word("telnet_"+word);
				Word wa = jbf.find_word("admin_"+word);
				if(wt != null)
				{
					jbf.st.push(args);
					wt.execp(jbf);
					if(jbf.stdout != "")
					_print.println(jbf.stdout);
					jbf.stdout = "";
				}
				else if(wa != null)
				{
					jbf.st.push(args);
					wa.execp(jbf);
					if(jbf.stdout != "")
					_print.println(jbf.stdout);
					jbf.stdout = "";
				}
				else if (_usrCommand.equals("status") || _usrCommand.equals("s"))
				{
					int playerCount = 0, objectCount = 0;
					int max = LoginController.getInstance().getMaxAllowedOnlinePlayers();
				 
					playerCount = L2World.getInstance().getAllPlayers().size();		 
					objectCount = L2World.getInstance().getAllVisibleObjects().size();
				 
					int itemCount=0;
					int itemVoidCount=0;
					int monsterCount=0;
					int minionCount = 0;
					int minionsGroupCount = 0;
					int npcCount=0;
					int guardCount=0;
					int charCount=0;
					int pcCount=0;
					int doorCount=0;
					int summonCount=0;
					int AICount=0;

					ConcurrentHashMap map = L2World.getInstance().getAllVisibleObjects();
		
					for(Object object : map.values()) 
					{
						L2Object obj = (L2Object)object;
		
						if (obj instanceof L2Character)
							if (((L2Character)obj).hasAI())
								AICount++;
						if (obj instanceof L2ItemInstance)
							if (((L2ItemInstance)obj).getLocation() == L2ItemInstance.ItemLocation.VOID)
								itemVoidCount++;
						else
							itemCount++;
						else if (obj instanceof L2MonsterInstance)
						{
							monsterCount++;
							minionCount += ((L2MonsterInstance)obj).getTotalSpawnedMinionsInstances();
							minionsGroupCount += ((L2MonsterInstance)obj).getTotalSpawnedMinionsGroups();
						}
						else if (obj instanceof L2NpcInstance)
							npcCount++;
						else if (obj instanceof L2PcInstance)
							pcCount++;
						else if (obj instanceof L2SummonInstance)
							summonCount++;
						else if (obj instanceof L2CastleGuardInstance)
							guardCount++;
						else if (obj instanceof L2DoorInstance)
							doorCount++;
						else if (obj instanceof L2Character)
							charCount++;
					}
					_print.println("Server Status: ");
					_print.println(" ------>  Player Count: " + playerCount + "/" + max); 
					_print.println(" ------>      GM Count: " + GetOnlineGMS());
					_print.println(" +----->  Object Count: " + objectCount);
					_print.println(" +----->      AI Count: " + AICount);
					_print.println(" +-L2Item(Void)/L2item: " + itemVoidCount + "/" + itemCount);
					_print.println(" +.L2Monster / Minions: " + monsterCount + "/" + minionCount);
					_print.println(" +.. Minions Groups: " + minionsGroupCount);
					_print.println(" +.....L2Npc / L2Pc: " + npcCount + " / " + pcCount);
					_print.println(" +........ L2Summon: " + summonCount);
					_print.println(" +... L2CastleGuard: " + guardCount);
					_print.println(" +.......... L2Door: " + doorCount);
					_print.println(" +.......... L2Char: " + charCount);
					_print.println(" *InGame Time / Uptime: " + GameTime() + " / " + GetUptime(_uptime));
					_print.println(" *Shutdown_sec / mode : " + Shutdown.getInstance().get_seconds() + " / " + Shutdown.getInstance().get_mode());
					_print.println(" *Log_chat / log_tell : " + log_chat + " / " + log_tell );
					_print.flush();
				}
				else if (_usrCommand.equals("performance") || _usrCommand.equals("p"))
				{
					String[] out = ClientScheduler.getInstance().getStats();
					 
					for (String line : out)
					{
						_print.println(line);
					}
					_print.flush();
				}
				else if (_usrCommand.startsWith("announce") || _usrCommand.startsWith("!"))
				{
					try
					{
						_usrCommand = _usrCommand.substring(9);
						Announcements.getInstance().announceToAll(_usrCommand);
						_print.println("Announcement Sent!");
					}
					catch (StringIndexOutOfBoundsException e)
					{
						_print.println("Please Enter Some Text To Announce!");
					}
				}
				else if (_usrCommand.startsWith("msg"))
				{
					try
					{
						String val = _usrCommand.substring(4);
						StringTokenizer st = new StringTokenizer(val);
						String name = st.nextToken();
						String message = val.substring(name.length()+1);
						L2PcInstance reciever = L2World.getInstance().getPlayer(name);
						CreatureSay cs = new CreatureSay(0, Say2.TELL, "Telnet Priv", message);
						if(reciever != null)
						{
							reciever.sendPacket(cs);
							_print.println("Telnet Priv->" + name + ": " + message);
							_print.println("Message Sent!");
						}
						else
						{
							_print.println("Unable To Find Username: " + name);
						}
					}
					catch (StringIndexOutOfBoundsException e)
					{
						_print.println("Please Enter Some Text!");
					}
				}
				else if (_usrCommand.startsWith("gmchat"))
				{
					try
					{
						_usrCommand = _usrCommand.substring(7);
						CreatureSay cs = new CreatureSay(0, 9, "Telnet GM Broadcast from " + _csocket.getInetAddress().getHostAddress(), _usrCommand);
						GmListTable.broadcastToGMs(cs);
						_print.println("Your Message Has Been Sent To " + GetOnlineGMS() + " GM(s).");
					}
					catch (StringIndexOutOfBoundsException e)
					{
						_print.println("Please Enter Some Text To Announce!");
					}
				}
				else if (_usrCommand.equals("gmlist"))
				{
					int igm = 0;
					String gmList = "";
						
					for(L2PcInstance player : GmListTable.getAllGMs())
					{
						gmList = gmList + ", " + player.getName();
						igm++;
					}
					_print.println("There are currently " + igm +" GM(s) online...");
					if ( gmList != "" ) _print.println(gmList);
				}
				else if (_usrCommand.startsWith("unblock"))
				{
					try
					{
						_usrCommand = _usrCommand.substring(8);
						if (LoginServer.getInstance().unblockIp(_usrCommand))
						{
							_log.warning("IP removed via TELNET by host: " + _csocket.getInetAddress().getHostAddress());
							_print.println("The IP " + _usrCommand + " has been removed from the hack protection list!");
						}
						else
						{
							_print.println("IP " + _usrCommand + " not found in hack protection list...");
						}
					}
					catch (StringIndexOutOfBoundsException e)
					{
						_print.println("Please Enter the IP to Unblock!");
					}
				}
				else if (_usrCommand.startsWith("whois"))
				{
					try
					{
						_usrCommand = _usrCommand.substring(6);
						L2PcInstance player = L2World.getInstance().getPlayer(_usrCommand);
						if(player != null)
						{
							_print.println("Name:"+player.getName());
							_print.println("Account:"+player.getAccountName());
							_print.println("IP:"+IpManager.getInstance().getIpByPlayer(player));
						}
						else _print.println("No such player online!");
					}
					catch (StringIndexOutOfBoundsException e)
					{
						_print.println("Please enter player name to kick");
					}
				}
				else if (_usrCommand.equals("baniplist"))
				{
					try
					{
							ArrayList<BannedIp> baniplist = new ArrayList<BannedIp>();
							baniplist = IpManager.getInstance().getBanList();
							_print.println("Ban IP List:");
							for(BannedIp temp: baniplist)
									_print.println("Ip:" + temp.ip + " banned by "+temp.admin);
							_print.flush();
					}
					catch (StringIndexOutOfBoundsException e)
					{
						_print.println("Please enter ip to ban");
					}
				}
				else if (_usrCommand.startsWith("banip"))
				{
					try
					{
						_usrCommand = _usrCommand.substring(6);
						IpManager.getInstance().BanIp(_usrCommand,"Telnet: " + _csocket.getInetAddress().getHostAddress());
					}
					catch (StringIndexOutOfBoundsException e)
					{
						_print.println("Please enter ip to unban");
					}
				}
				else if (_usrCommand.startsWith("unbanip"))
				{
					try
					{
							_usrCommand = _usrCommand.substring(8);
							IpManager.getInstance().UnbanIp(_usrCommand);
					}
					catch (StringIndexOutOfBoundsException e)
					{
						_print.println("Please enter ip to unban");
					}
				}
				else if (_usrCommand.startsWith("kick"))
				{
					try
					{
						_usrCommand = _usrCommand.substring(5);
						L2PcInstance player = L2World.getInstance().getPlayer(_usrCommand);
						if(player != null)
						{
							player.sendMessage("You are kicked by gm");
							player.logout();
							_print.println("Player kicked");
						}
					}
					catch (StringIndexOutOfBoundsException e)
					{
						_print.println("Please enter player name to kick");
					}
				}
				else if (_usrCommand.startsWith("enemy"))
				{
					try
					{
						_usrCommand = _usrCommand.substring(6);
						L2PcInstance player = L2World.getInstance().getPlayer(_usrCommand);
						if(player != null)
							for (L2Character enemy : player.getEnemyList(player))
								_print.println("--> "+enemy.getName()+" <--");
						else
							_print.println("Player is offline.");
					}
					catch (StringIndexOutOfBoundsException e)
					{
						_print.println("Please enter player name.");
					}
				}
	else if (_usrCommand.startsWith("quest_reload"))
	{
		try {
		Object result;
		File file;
		String questName,questDir;

		_usrCommand = _usrCommand.substring(13);
		questName = _usrCommand; // directory is default command value
		questDir = "quests"; // set default quest directory

		file = new File(Config.DATAPACK_ROOT,"data/jscript/"+questDir+"/_"+questName+"/__init__.py");
		if (!file.exists()) {
			try {
			int questId = Integer.parseInt(_usrCommand);

			Quest q = Quest.findQuest(questId);
			if (q != null) {
				questName = q.getName();
				questDir = q.getDescr().toLowerCase();
				file = new File(Config.DATAPACK_ROOT,"data/jscript/"+questDir+"/_"+questName+"/__init__.py");
				if (!file.exists())
				{ questDir = "quests"; file = new File(Config.DATAPACK_ROOT,"data/jscript/"+questDir+"/_"+questName+"/__init__.py");}
			}
			else
			{
				_print.println("Quest with ID "+questId+" not found");
			}
			}
			catch (NumberFormatException e) { }
		}
		if (file.exists()) {
			try {
			BSFManager bsf = new BSFManager();
			bsf.exec("jython","admin", 0, 0, "from data.jscript."+questDir+" import _"+questName+";reload(_"+questName+")");
			result = questName+" reload complete.";
			} catch (BSFException e) { result = e; }
			_print.println("Jython: "+result);
		}
		else { _print.println("Wrong path for quest "+questName); }
		}
		catch (StringIndexOutOfBoundsException e)
		{
		_print.println("Please enter Quest name.");
		}
	}
				else if(_usrCommand.startsWith("skill_reload"))
				{
					SkillTable.getInstance().reload();
					_print.println("Skills reloaded.");
				}
				else if (_usrCommand.startsWith("npc_reload"))
				{
					NpcTable.getInstance().reloadAllNpc();
					_print.println("NPC's reloaded.");
				}
				else if (_usrCommand.startsWith("shutdown"))
				{
					try
					{
						if(_usrCommand.substring(9).equals("NOW"))
						{
							_log.warning("Shutting down via TELNET by host: " + _csocket.getInetAddress().getHostAddress());
							_print.println("Shutting down...");
							System.exit(-1);
						}
						int val = Integer.parseInt(_usrCommand.substring(9)); 
						Shutdown.getInstance().startTelnetShutdown(_csocket.getInetAddress().getHostAddress(), val, false);
						_print.println("Server Will Shutdown In " + val + " Seconds!");
						_print.println("Type \"abort\" To Abort Shutdown!");
					}
					catch (StringIndexOutOfBoundsException e)
					{
						_print.println("Please Enter * amount of seconds to shutdown!");
					}
					catch (Exception NumberFormatException)
					{
						_print.println("Numbers Only!");
					}
				}
				else if (_usrCommand.startsWith("restart"))
				{
					try
					{
						int val = Integer.parseInt(_usrCommand.substring(8)); 
						Shutdown.getInstance().startTelnetShutdown(_csocket.getInetAddress().getHostAddress(), val, true);
						_print.println("Server Will Restart In " + val + " Seconds!");
						_print.println("Type \"abort\" To Abort Restart!");
					}
					catch (StringIndexOutOfBoundsException e)
					{
						_print.println("Please Enter * amount of seconds to restart!");
					}	
					catch (Exception NumberFormatException)
					{
						_print.println("Numbers Only!");
					}
				}
				else if (_usrCommand.startsWith("jconsole"))
				{
					try
					{
						JConsole bshConsole = new JConsole();
						Interpreter bsh = new Interpreter(bshConsole);
						bsh.eval("bsh.system.shutdownOnExit=false");
						bsh.eval("desktop()");
						Thread t = new Thread(bsh);
						t.start();
					}
					catch ( EvalError e )
					{
						System.err.println("Couldn't start java console: "+e);
					}
					_print.println("OK! - try to start java console");
				}
				else if (_usrCommand.equals("abort") || _usrCommand.equals("a"))
				{
					Shutdown.getInstance().Telnetabort(_csocket.getInetAddress().getHostAddress());
					_print.println("OK! - Shutdown/Restart Aborted.");
				}
				else if (_usrCommand.startsWith("log_chat"))
				{
					if (log_chat==1)
					{
						log_chat=0;
						_print.println("Log_chat is turned off");
					}
					else
					{
						log_chat=1;
						_print.println("Log_chat is turned on.");
					}
				}
				else if (_usrCommand.equals("log_tell"))
				{
					if (log_tell==1)
					{
						log_tell=0;
						_print.println("Log_tell is turned off.");
					}
					else
					{
						log_tell=1;
						_print.println("Log_tell is turned on.");
					}

				}
				else if (_usrCommand.startsWith("tell"))
				{
					 StatusThread temp = Status.telnetlist;
					 int tcounter = 1;
					 if (temp==null) { this.write("telnetlist is null!"); }
					 while (temp!=null)
					 {
						 temp.write("Telnet" + tcounter + ":" + _usrCommand.substring(4));
						 temp=temp.next;
						 tcounter++;
					 }
				}
				else if (_usrCommand.startsWith("gc"))
				{
					try
					{
						System.gc();Thread.sleep(1000L);System.gc();Thread.sleep(1000L);System.gc();
					}
					catch(Exception e)
					{
					}
					_print.println("OK! - garbage collector called.");
				}
				else if (_usrCommand.equals("quit"))
				{ /* Do Nothing :p - Just here to save us from the "Command Not Understood" Text */ }
				else if (_usrCommand.length() == 0)
				{ /* Do Nothing Again - Same reason as the quit part */ }
				else
				{
					// Use forth for other commands
					jbf.execp(command);
					if(jbf.stdout != "")
						_print.println(jbf.stdout);
					jbf.stdout = "";
				}
				_print.print("");
				_print.flush();
			}
			if(!_csocket.isClosed())
			{
				_print.println("Bye Bye!");
				_print.flush();
				_csocket.close();
			}
			telnetOutput(1, "Connection from "+_csocket.getInetAddress().getHostAddress()+" was closed by client.");
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
// begin clean telnetlist
		if (this==Status.telnetlist)
		{
			Status.telnetlist = next;
		}
		else
		{
			StatusThread temp = Status.telnetlist;
			while (temp.next != this) { temp=temp.next; }
			temp.next = next;
		}
// finished clean telnetlist
	}

	public void write(String _text)
	{
		_print.println(_text);
		_print.flush();
	}

	private int GetOnlineGMS()
	{
		return GmListTable.getAllGMs().size();
	}
	
	private String GetUptime(int _uptime)
	{
		int uptime = (int)System.currentTimeMillis()-_uptime;
		uptime = uptime/1000;
		int h = uptime/3600;
		int m = (uptime-(h*3600))/60;
		int s = ((uptime-(h*3600))-(m*60));
		return h + "hrs " + m + "mins " + s + "secs";
	}
	
	private String GameTime()
	{
		int t = GameTimeController.getInstance().getGameTime();
		int h = t/60;
		int m = t%60;
		SimpleDateFormat format = new SimpleDateFormat("H:mm");
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.HOUR_OF_DAY, h);
		cal.set(Calendar.MINUTE, m);
		return format.format(cal.getTime());
	}
}
