package net.sf.l2j.status;


import net.sf.l2j.Config;
import net.sf.l2j.gameserver.*;
import net.sf.l2j.gameserver.Shutdown;
import net.sf.l2j.gameserver.cache.HtmCache;
import net.sf.l2j.gameserver.clientpackets.Say2;
import net.sf.l2j.gameserver.instancemanager.ZoneManager;
import net.sf.l2j.gameserver.model.*;
import net.sf.l2j.gameserver.model.actor.instance.L2DoorInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2MonsterInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2NpcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.packets.serverpackets.CreatureSay;
import net.sf.l2j.gameserver.packets.serverpackets.InventoryUpdate;
import net.sf.l2j.gameserver.packets.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.tables.GmListTable;
import net.sf.l2j.gameserver.tables.ItemTable;
import net.sf.l2j.gameserver.tables.NpcTable;
import net.sf.l2j.gameserver.tables.SkillTable;
import net.sf.l2j.gameserver.taskmanager.DecayTaskManager;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Properties;
import java.util.StringTokenizer;


public class GameStatusThread extends Thread
{
    //private static final Logger _log = Logger.getLogger(AdminTeleport.class.getName());
    
    private Socket                  _csocket;
    
    private PrintWriter             _print;
    private BufferedReader          _read;
    
    private int                     _uptime;
      
    private void telnetOutput(int type, String text) {
        if ( type == 1 ) System.out.println("TELNET | "+text);
        else if ( type == 2 ) System.out.print("TELNET | "+text);
        else if ( type == 3 ) System.out.print(text);
        else if ( type == 4 ) System.out.println(text);
        else System.out.println("TELNET | "+text);
    }
    
    private boolean isValidIP(Socket client) {
        boolean result = false;        
        InetAddress ClientIP = client.getInetAddress();
               
        // convert IP to String, and compare with list
        String clientStringIP = ClientIP.getHostAddress();
        
        telnetOutput(1, "Connection from: "+clientStringIP);
        
        // read and loop thru list of IPs, compare with newIP
        if ( Config.DEVELOPER ) telnetOutput(2, "");    
        
        try {
            Properties telnetSettings = new Properties();
            InputStream telnetIS = new FileInputStream(new File(Config.TELNET_FILE));
            telnetSettings.load(telnetIS);
            telnetIS.close();
            
            String HostList = telnetSettings.getProperty("ListOfHosts", "127.0.0.1,localhost");
            
            if ( Config.DEVELOPER ) telnetOutput(3, "Comparing ip to list...");
            
            // compare
            String ipToCompare = null;
            for (String ip:HostList.split(",")) {
                if ( !result ) {
                    ipToCompare = InetAddress.getByName(ip).getHostAddress();
                    if ( clientStringIP.equals(ipToCompare) ) result = true;
                    if ( Config.DEVELOPER ) telnetOutput(3, clientStringIP + " = " + ipToCompare + "("+ip+") = " + result);
                }
            }    
        }
        catch ( IOException e) {
            if ( Config.DEVELOPER ) telnetOutput(4, "");
            telnetOutput(1, "Error: "+e);
        }
        
        if ( Config.DEVELOPER ) telnetOutput(4, "Allow IP: "+result);
        return result;
    }
    
    public GameStatusThread(Socket client, int uptime, String StatusPW) throws IOException
    {
        _csocket = client;
        this._uptime = uptime;
        
        _print = new PrintWriter(_csocket.getOutputStream());
        _read  = new BufferedReader(new InputStreamReader(_csocket.getInputStream()));
        
        if ( isValidIP(client) ) {    
            telnetOutput(1, client.getInetAddress().getHostAddress()+" accepted.");
            _print.println("Welcome To The L2J Telnet Session.");
            _print.println("Please Insert Your Password!");
            _print.print("Password: ");
            _print.flush();
            String tmpLine = _read.readLine();
            if ( tmpLine == null )  {
                _print.println("Error.");
                _print.println("Disconnected...");
                _print.flush();
                _csocket.close();
            }
            else {
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
        else {
            telnetOutput(1, "Connection attempt from "+ client.getInetAddress().getHostAddress() +" rejected.");
            _csocket.close();
        }
    }
    
    public void run()
    {
        String _usrCommand = "";
        try
        {
            while (_usrCommand.compareTo("quit") != 0 && _usrCommand.compareTo("exit") != 0)
            {
                _usrCommand = _read.readLine();
                if(_usrCommand == null)
                {
                	_csocket.close();
                	break;
                }
                if (_usrCommand.equals("help")) {
                    _print.println("The following is a list of all available commands: ");
                    _print.println("help                - shows this help.");
                    _print.println("status              - displays basic server statistics.");
                    _print.println("performance         - shows server performance statistics.");
                    _print.println("announce <text>     - announces <text> in game.");
                    _print.println("msg <nick> <text>   - Sends a whisper to char <nick> with <text>.");
                    _print.println("gmchat <text>       - Sends a message to all GMs with <text>.");
                    _print.println("gmlist              - lists all gms online.");
                    _print.println("kick                - kick player <name> from server.");
                    _print.println("shutdown <time>     - shuts down server in <time> seconds.");
                    _print.println("restart <time>      - restarts down server in <time> seconds.");
                    _print.println("abort               - aborts shutdown/restart.");
                    _print.println("give <player> <itemid> <amount>");
                    _print.println("quit                - closes telnet session.");
                }
                else if (_usrCommand.equals("status"))
                {
                    int playerCount = 0, objectCount = 0;
                    int max = LoginServerThread.getInstance().getMaxPlayer();

                    playerCount = L2World.getInstance().getAllPlayers().size();                    
                    objectCount = L2World.getInstance().getAllVisibleObjects().size();
                    
                    int itemCount=0;
                    int itemVoidCount=0;
                    int monsterCount=0;
                    int minionCount = 0;
                    int minionsGroupCount = 0;
                    int npcCount=0;
                    int charCount=0;
                    int pcCount=0;
                    int doorCount=0;
                    int summonCount=0;
                    int AICount=0;

		            for (L2Object obj : L2World.getInstance().getAllVisibleObjects())
		            {
		            	if(obj == null)
		            		continue;
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
                        else if (obj instanceof L2Summon)
                            summonCount++;
                        else if (obj instanceof L2DoorInstance)
                            doorCount++;
                        else if (obj instanceof L2Character)
                            charCount++;
                    }
                    _print.println("Server Status: ");
                    _print.println("  --->  Player Count: " + playerCount + "/" + max); 
                    _print.println("  +-->  Object Count: " + objectCount);
                    _print.println("  +-->      AI Count: " + AICount);
                    _print.println("  +.... L2Item(Void): " + itemVoidCount);
                    _print.println("  +.......... L2Item: " + itemCount);
                    _print.println("  +....... L2Monster: " + monsterCount);
                    _print.println("  +......... Minions: " + minionCount);
                    _print.println("  +.. Minions Groups: " + minionsGroupCount);
                    _print.println("  +........... L2Npc: " + npcCount);
                    _print.println("  +............ L2Pc: " + pcCount);
                    _print.println("  +........ L2Summon: " + summonCount);
                    _print.println("  +.......... L2Door: " + doorCount);
                    _print.println("  +.......... L2Char: " + charCount);
                    _print.println("  --->   Ingame Time: " + GameTime());
                    _print.println("  ---> Server Uptime: " + GetUptime(_uptime));
                    _print.println("  --->      GM Count: " + GetOnlineGMS());
                    _print.flush();
                }
                else if (_usrCommand.equals("performance"))
                {
                    for (String line : ClientScheduler.getInstance().getStats())
                    {
                        _print.println(line);
                    }
                    _print.flush();
                }
                else if (_usrCommand.startsWith("announce"))
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
                    
                    if (!Config.HIDE_GM_STATUS)
                    {
                    	for (L2PcInstance player : GmListTable.getInstance().getAllGms())
                    	{
                            	gmList = gmList + ", " + player.getName();
                            	igm++;
                    	}
                    }
                    _print.println("There are currently " + igm +" GM(s) online...");
                    if ( gmList != "" ) _print.println(gmList);
                }
                /*else if (_usrCommand.startsWith("unblock"))
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
                            _print.println("IP not found in hack protection list...");
                        }
                        //TODO: with packet
                    }
                    catch (StringIndexOutOfBoundsException e)
                    {
                        _print.println("Please Enter the IP to Unblock!");
                    }
                }*/
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
                else if (_usrCommand.startsWith("shutdown"))
                {
                    try
                    {
                        int val = Integer.parseInt(_usrCommand.substring(9)); 
                        Shutdown.getInstance().startTelnetShutdown(_csocket.getInetAddress().getHostAddress(), val, false);
                        _print.println("Server Will Shutdown In " + val + " Seconds!");
                        _print.println("Type \"abort\" To Abort Shutdown!");
                    }
                    catch (StringIndexOutOfBoundsException e)
                    {
                        _print.println("Please Enter * amount of seconds to shutdown!");
                    }
                    catch (Exception NumberFormatException) {
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
                    catch (Exception NumberFormatException) {
                        _print.println("Numbers Only!");
                    }
                }
                else if (_usrCommand.startsWith("abort"))
                {
                    Shutdown.getInstance().Telnetabort(_csocket.getInetAddress().getHostAddress());
                    _print.println("OK! - Shutdown/Restart Aborted.");
                }
                else if (_usrCommand.equals("quit")) { /* Do Nothing :p - Just here to save us from the "Command Not Understood" Text */ }
                else if (_usrCommand.startsWith("give"))
                {
                    StringTokenizer st = new StringTokenizer(_usrCommand.substring(5));
                    
                    try
                    {
                        L2PcInstance player = L2World.getInstance().getPlayer(st.nextToken());
                        int itemId = Integer.parseInt(st.nextToken());
                        int amount = Integer.parseInt(st.nextToken());
                        
                        if(player != null)
                        {
                            L2ItemInstance item = ItemTable.getInstance().createItem(itemId);
                            item.setCount(amount);
                            player.getInventory().addItem(item);
                            InventoryUpdate iu = new InventoryUpdate();
                            iu.addItem(item);
                            SystemMessage sm = new SystemMessage(SystemMessage.YOU_PICKED_UP_S1_S2);
                            sm.addItemName(itemId);
                            sm.addNumber(amount);
                            player.sendPacket(iu);
                            _print.println("ok");
                        }
                    }
                    catch(Exception e)
                    {
                        
                    }
                }
                else if (_usrCommand.startsWith("debug"))
                {
                	StringTokenizer st = new StringTokenizer(_usrCommand.substring(6));
                	try
                	{
                		String dbg = st.nextToken();
                		
                		if(dbg.equals("decay"))
                		{
                			_print.print(DecayTaskManager.getInstance().toString());
                		}
                	}
                	catch(Exception e){}
                }
                else if (_usrCommand.startsWith("reload"))
                {
                	StringTokenizer st = new StringTokenizer(_usrCommand.substring(7));
                	try
                	{
                		String type = st.nextToken();
                		
                		if(type.equals("multisell"))
                		{
                			_print.print("Reloading multisell... ");
                			L2Multisell.getInstance().reload();
                			_print.print("done\n");
                		}
                		else if(type.equals("skill"))
                		{
                			_print.print("Reloading skills... ");
                			SkillTable.getInstance().reload();
                			_print.print("done\n");
                		}
                		else if(type.equals("npc"))
                		{
                			_print.print("Reloading npc templates... ");
                			NpcTable.getInstance().reloadAllNpc();
                			_print.print("done\n");
                		}
                		else if(type.equals("html"))
                		{
                			_print.print("Reloading html cache... ");
                			HtmCache.getInstance().reload();
                			_print.print("done\n");
                		}
                		else if(type.equals("item"))
                		{
                			_print.print("Reloading item templates... ");
                			ItemTable.getInstance().reload();
                			_print.print("done\n");
                		}
                		else if(type.equals("zone"))
                		{
                			_print.print("Reloading zone tables... ");
                			ZoneManager.getInstance().reloadAll();
                			_print.print("done\n");
                		}

                	}
                	catch(Exception e){}
                }
                else if (_usrCommand.length() == 0) { /* Do Nothing Again - Same reason as the quit part */ }
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
    }
    
    private int GetOnlineGMS()
    {
        return GmListTable.getInstance().getAllGms().length;
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
