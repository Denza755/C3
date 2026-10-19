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

import java.nio.ByteBuffer;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import net.sf.l2j.Config;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.ClientThread;
import net.sf.l2j.gameserver.GmListTable;
import net.sf.l2j.gameserver.handler.IVoicedCommandHandler;
import net.sf.l2j.gameserver.handler.VoicedCommandHandler;
import net.sf.l2j.gameserver.lib.Log;
import net.sf.l2j.gameserver.lib.Strings;
import net.sf.l2j.gameserver.model.L2PcInstance;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.serverpackets.CreatureSay;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;
import net.sf.l2j.status.Status;
import net.sf.l2j.status.StatusThread;
import ru.balancer.JBForth;

/**
 * This class ...
 * 
 * @version $Revision: 1.16.2.12.2.7 $ $Date: 2005/04/11 10:06:11 $
 */
public class Say2 extends ClientBasePacket
{
	private static final String _C__38_SAY2 = "[C] 38 Say2";
	private static Logger _log = Logger.getLogger(Say2.class.getName());
	private static Logger _logChat = Logger.getLogger("chat");

	public final static int ALL = 0;
	public final static int SHOUT = 1; //!
	public final static int TELL = 2; //"
	public final static int PARTY = 3; //#
	public final static int CLAN = 4;  //@
	public final static int GM = 5;	
	public final static int PETITION_PLAYER = 6; // used for petition
	public final static int PETITION_GM = 7; //* used for petition
	public final static int TRADE = 8; //+
	public final static int ALLIANCE = 9; //$
	public final static int ANNOUNCEMENT = 10;
	public final static int PARTYROOM_ALL = 15; //(yellow)
	public final static int PARTYROOM_COMMANDER = 16; //(blue)
	public final static int HERO_VOICE = 17;
	
	public final static String[] chatNames = {
		"ALL  ",
		"SHOUT",
		"TELL ",
		"PARTY",
		"CLAN ",
		"GM   ",
		"PETITION_PLAYER",
		"PETITION_GM",
		"TRADE",
		"ALLIANCE",
		"ANNOUNCEMENT"   
	};
	
	private static String gobalChatState = Config.DEFAULT_GLOBAL_CHAT;
	protected static List<String> _banned = new ArrayList<String>();
	private String _text;
	private final int _type;
	private final String _target;
	/**
	 * packet type id 0x38
	 * format:		cSd (S)
	 * @param decrypt
	 */
	public Say2(ByteBuffer buf, ClientThread client)
	{
		super(buf, client);
		_text = readS();
		_type = readD();
		_target = (_type == TELL) ? readS() : null;  
	}

	void runImpl()
	{
		if (Config.DEBUG) 
			_log.info("Say type:"+_type);
		
		L2PcInstance activeChar = getClient().getActiveChar();
		if(activeChar == null) return;

		if (Config.LOG_CHAT) 
		{
			LogRecord record = new LogRecord(Level.INFO, _text);
			record.setLoggerName("chat");
			if (_type == TELL) {
				record.setParameters(new Object[]{chatNames[_type], "[" + activeChar.getName() + " to "+_target+"]"});
			} else {
				record.setParameters(new Object[]{chatNames[_type], "[" + activeChar.getName() + "]"});
			}
			_logChat.log(record);
		}

		if (Config.LOG_TELNET) 
		{
				  String line_output;

		  if (_type == TELL)
				  {
					line_output = chatNames[_type] +  "[" + activeChar.getName() + " to "+_target+"] " + _text;
				  }
				  else
				  {
					line_output = chatNames[_type] + "[" + activeChar.getName() + "] " + _text;
				  }
				  telnet_output(line_output, _type);
		}

		if(_text.startsWith(".")) 
		{
			String command = _text.substring(1).trim();
			String word = "user_" + command.split("\\s+")[0];
			String args = command.substring(word.length()-5).trim();

			_log.info("Find ("+word+"): "+JBForth.find_word(word));
			_log.info("Exists ("+word+"): "+JBForth.word_exists(word));

			if(activeChar.jbf.word_exists(word))
			{
				if (Config.DEBUG) 
					_log.info("Found user command: "+word);

				JBForth tmp = new JBForth(activeChar);
				tmp.st.push(args);
			    tmp.execp(word);

   				return;
			}
		}

/* character accesslevel not now ban chats - use nochannel

		if(activeChar.getAccessLevel()<0)
		{
			activeChar.sendMessage("You are banned in all chats");
			return;
		}
*/

		if(activeChar.getNoChannel() != 0)
		{
			if(activeChar.getNoChannel() > System.currentTimeMillis() || activeChar.getNoChannel() < 0)
			{
				activeChar.sendMessage("You are banned in all chats");
				return;
			}
			else
			{
				activeChar.updateNoChannel(0);
			}
		}
		
		if(Config.MAT_BANCHAT) 
		{
			for(String pattern : Config.MAT_LIST)
			{
//				_log.info("Try match "+pattern+" to "+_text);
				if(_text.matches(".*"+pattern+".*"))
				{
			  		activeChar.jbf.execp("'jbforth/users/messages/ban/abuse.htm' show");
	  				Log.add(""+activeChar+": "+_text, "abuse");
					activeChar.updateNoChannel(Config.UNCHATBANTIME);
	  				return;
	   			}
			}
		}
		
		String orig_text = _text;
		if(!activeChar.getVarB("translit@not"))
			_text = Strings.fromTranslit(_text);

		CreatureSay cs = new CreatureSay(activeChar.getObjectId(), _type, activeChar.getName(), _text);
	
		if (_type == TELL)
		{
			L2PcInstance receiver = L2World.getPlayer(_target);
			if (receiver != null && !receiver.isInBlockList(activeChar) && !receiver.isBlockAll())
			{	
				if(!receiver.getMessageRefusal())
				{
					receiver.sendPacket(cs);
					cs = new CreatureSay(activeChar.getObjectId(), _type,"->" + receiver.getName(),_text);
					activeChar.sendPacket(cs);
				}
				else
					activeChar.sendPacket(new SystemMessage(SystemMessage.THE_PERSON_IS_IN_A_MESSAGE_REFUSAL_MODE));
			}
			else
			{
				activeChar.sendPacket(new SystemMessage(SystemMessage.S1_IS_NOT_LOGGED_IN).addString(_target));
			}
		} 
		else if ((_type == SHOUT || _type == TRADE)) 
		{
			if (gobalChatState.equalsIgnoreCase("on") 
				|| (gobalChatState.equalsIgnoreCase("gm") && activeChar.isGM()))
		  // For those who want Global chat to be GM only
		  // else if (activeChar.isGM() && (type == SHOUT || type == TRADE))
			{
				for(L2PcInstance player : L2World.getAllPlayers())
				{
					if(!player.isInBlockList(activeChar) && !player.isBlockAll())
						player.sendPacket(cs);
				}
		  	} 
		  	else 
		  	{
				for(L2PcInstance player : L2World.getAllPlayers())
				{
					if(activeChar.getDistance(player)<10000 
						&& !player.isInBlockList(activeChar) && !player.isBlockAll())
						player.sendPacket(cs);
				}
		  	}
		}
		else if (_type == ALL)
		{
			for(L2PcInstance player : activeChar.getAllPlayers(1250))
			{
				if(!player.isInBlockList(activeChar) && !player.isBlockAll())
					player.sendPacket(cs);
			}
			activeChar.sendPacket(cs);

			if(orig_text.startsWith(".")) 
			{
				StringTokenizer st = new StringTokenizer(orig_text);
				String target = st.nextToken().substring(1);
				if (st.hasMoreTokens())
				{
					String command = st.nextToken();
					IVoicedCommandHandler vch = VoicedCommandHandler.getInstance().getVoicedCommandHandler(command);
					if (vch != null) 
						vch.useVoicedCommand(command, activeChar, target);
					else
						_log.warning("No handler registered for bypass '"+command+"'");
				}
			}
		}
		else if (_type == CLAN && activeChar.getClan() != null)
		{
			activeChar.getClan().broadcastToOnlineMembers(cs);
		}
		else if (_type == ALLIANCE && activeChar.getClan() != null && activeChar.getClan().getAlliance() != null)
		{
			activeChar.getClan().getAlliance().broadcastToOnlineMembers(cs);
		}
		else if (_type == PARTY && activeChar.isInParty())
		{
			activeChar.getParty().broadcastToPartyMembers(cs);
		}
		else if (_type == PETITION_PLAYER) 
		{
			for(L2PcInstance gm : GmListTable.getAllGMs())
			{
				gm.sendPacket(cs);
				if(!gm.getMessageRefusal())
					gm.sendPacket(cs);
			}
	   	}
	}

	private void telnet_output(String _text, int type)
		{
		  StatusThread tinstance = Status.telnetlist;
  
		  while (tinstance != null)
		  {
			 if (type==TELL && tinstance.log_tell==1) { tinstance.write( _text); }
			 else if (tinstance.log_chat==1) { tinstance.write( _text); }
			 tinstance=tinstance.next;
		  }
		}
	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.clientpackets.ClientBasePacket#getType()
	 */
	public String getType()
	{
		return _C__38_SAY2;
	}

	public class UnbanTask implements Runnable
	{
	   public String _name;
	   public UnbanTask(String Name)
	   {
		  _name = Name;
	   }

	   public void run()
	   {	
			L2PcInstance plyr = L2World.getPlayer(_name);
		  	if(plyr!=null)
		  	{
		  		plyr.setAccessLevel(0);
		  		plyr.jbf.execp("'jbforth/users/messages/ban/unabuse.htm' show");
  				Log.add(""+plyr+": unbanchat online", "abuse");
		  	}
		  	else 
		  	{
		  		setCharacterAccessLevel(_name, 0);
  				Log.add("Player "+_name+": unbanchat offline", "abuse");
		  	}

		  	_banned.remove(_name);
	   }
	}
	public void setCharacterAccessLevel(String user, int banLevel)
	{
	   java.sql.Connection con = null;
	   try
	   {		 
		  con = L2DatabaseFactory.getInstance().getConnection();
		  
		  String stmt = "UPDATE characters SET characters.accesslevel = ? WHERE characters.char_name=?";
		  PreparedStatement statement = con.prepareStatement(stmt);
		  statement.setInt(1, banLevel);
		  statement.setString(2, user);
		  statement.executeUpdate();
		  statement.close();
		  
		  //SystemMessage sm = new SystemMessage(614);
		  //sm.addString("Character Access Level for "+user+" set to "+banLevel+".");
		  //player.sendPacket(sm);
	   }
	   catch (Exception e)
	   {
		  //System.out.println("Error in sql");
		  _log.warning("Could not set accessLevl:"+e);
	   } 
	   finally 
	   {
		  try { con.close(); } catch (Exception e) {}
	   }
	}   
}
