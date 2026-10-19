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
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.ClientThread;
import net.sf.l2j.gameserver.model.L2PcInstance;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.serverpackets.CreatureSay;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;

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
	public final static int TELL = 2;
	public final static int PARTY = 3; //#
	public final static int CLAN = 4;  //@
	public final static int GM = 5;    
	public final static int PETITION_PLAYER = 6; // used for petition
	public final static int PETITION_GM = 7; //* used for petition
	public final static int TRADE = 8; //+
	public final static int ALLIANCE = 9; //$
	public final static int ANNOUNCEMENT = 10;
    
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
	
    private final String _text;
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
		if (Config.DEBUG) _log.fine("Say type:"+_type);
		
		L2PcInstance activeChar = getClient().getActiveChar();
        
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
                
		CreatureSay cs = new CreatureSay(activeChar.getObjectId(), _type, activeChar.getName(), _text);
	
		if (_type == TELL)
		{
			L2PcInstance receiver = L2World.getInstance().getPlayer(_target);
			if (receiver != null)
			{
				receiver.sendPacket(cs);
				activeChar.sendPacket(cs);
			}
			else
			{
				SystemMessage sm = new SystemMessage(SystemMessage.S1_IS_NOT_ONLINE);
				sm.addString(_target);
				activeChar.sendPacket(sm);
			}
		} 
		else if ((_type == SHOUT || _type == TRADE)
                && 
                (gobalChatState.equalsIgnoreCase("on") ||
                        (gobalChatState.equalsIgnoreCase("gm") && activeChar.isGM())))
		// For those who want Global chat to be GM only
		// else if (activeChar.isGM() && (type == SHOUT || type == TRADE))
		{
			for (L2PcInstance player : L2World.getInstance().getAllPlayers())
			{
				player.sendPacket(cs);
			}
		}
		else if (_type == ALL)
		{
			for (L2PcInstance player : activeChar.getKnownPlayers())
			{
				player.sendPacket(cs);
			}
			
			activeChar.sendPacket(cs);
		}
		else if (_type == CLAN && activeChar.getClan() != null)
		{
			activeChar.getClan().broadcastToOnlineMembers(cs);
		}
		else if (_type == PARTY && activeChar.isInParty())
		{
			activeChar.getParty().broadcastToPartyMembers(cs);
		}
		else if (_type == PETITION_PLAYER) 
		{
            for (L2PcInstance gm : L2World.getInstance().getAllGMs())
            {
                gm.sendPacket(cs);
            }
       	}
		/*
        else if (text.equals(".ride") && false)
        {
            //activeChar.sendPacket(new PetStatusShow(activeChar.getPet()));
            //activeChar.sendPacket(new PetStatusUpdate(activeChar.getPet()));
            //activeChar.sendPacket(new ActionFailed());
            SystemMessage sm = new SystemMessage(614); 
            sm.addString("aDoing .ride");
            activeChar.sendPacket(sm);
            Ride r = new Ride(activeChar);
            activeChar.sendPacket(r);
            sm = new SystemMessage(614); 
            sm.addString("bDoing .ride");
            activeChar.sendPacket(sm);
            //activeChar.setPrivateStoreType(4);
            //activeChar.sendPacket(new UserInfo(activeChar));
            //activeChar.broadcastPacket(new UserInfo(activeChar));
            //L2World.getInstance().
        }//*/
	}

	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.clientpackets.ClientBasePacket#getType()
	 */
	public String getType()
	{
		return _C__38_SAY2;
	}
}
