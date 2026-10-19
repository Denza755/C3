package net.sf.l2j.gameserver.clientpackets;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.logging.Logger;

import net.sf.l2j.gameserver.ClientThread;
import net.sf.l2j.gameserver.model.L2PcInstance;
import net.sf.l2j.gameserver.model.L2World;

public class RequestBlock extends ClientBasePacket
{
   private static final String _C__A0_REQUESTBLOCK = "[C] A0 RequestBlock";
   private static Logger _log = Logger.getLogger(L2PcInstance.class.getName());
   
   private final static int BLOCK = 0;
   private final static int UNBLOCK = 1;
   private final static int BLOCKLIST = 2;
   private final static int ALLBLOCK = 3;
   private final static int ALLUNBLOCK = 4;
   
   private final Integer _type;
   private final String _name;
   private final L2PcInstance _target;

   public RequestBlock(ByteBuffer buf, ClientThread client)
   {
       super(buf, client);
       _type = readD(); //0x00 - block, 0x01 - unblock, 0x03 - allblock, 0x04 - allunblock

       if( _type == BLOCK || _type == UNBLOCK )
       {
           _name = readS();
           _target = L2World.getPlayer(_name);
       }
       else
       {
    	   _name = null;
    	   _target = null;
       }
   }
   
	void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if(activeChar == null) return;

		switch(_type)
		{
		case BLOCK:
			if(_target == null)
				return;
			activeChar.addToBlockList(_target);
			activeChar.sendMessage("Added to blocklist: " + _target.getName());
			break;
		case UNBLOCK:
			if(_target == null)
				return;
			activeChar.removeFromBlockList(_target);
			activeChar.sendMessage("Removed from blocklist: " + _target.getName());
			break;
		case BLOCKLIST:
			ArrayList<String> blockList = activeChar.getBlockList();

			if(blockList != null)
			{
				activeChar.sendMessage("Blocked characters list:");

				for(String name : blockList)
					activeChar.sendMessage(name);
			}
			break;
		case ALLBLOCK:
			activeChar.setBlockAll(true);
			break;
		case ALLUNBLOCK:
			activeChar.setBlockAll(false);
			break;
		default:
			_log.info("Unknown 0x0a block type: " + _type);
		}
   }
   
   public String getType()
   {
       return _C__A0_REQUESTBLOCK;
   }
}