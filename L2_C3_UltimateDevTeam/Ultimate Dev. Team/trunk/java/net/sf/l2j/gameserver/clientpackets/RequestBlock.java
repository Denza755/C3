package net.sf.l2j.gameserver.clientpackets;

import net.sf.l2j.gameserver.ClientThread;
import net.sf.l2j.gameserver.model.BlockList;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;

import java.nio.ByteBuffer;
import java.util.logging.Logger;

public class RequestBlock extends ClientBasePacket
{
   private static final String _C__A0_REQUESTBLOCK = "[C] A0 RequestBlock";
   private static Logger _log = Logger.getLogger(L2PcInstance.class.getName());
   
   private final static int BLOCK = 0;
   private final static int UNBLOCK = 1;
   private final static int BLOCKLIST = 2;
   private final static int ALLBLOCK = 3;
   private final static int ALLUNBLOCK = 4;
   
   public RequestBlock(ByteBuffer buf, ClientThread client)
   {
       super(buf, client);
       _type = readD(); //0x00 - block, 0x01 - unblock, 0x03 - allblock, 0x04 - allunblock

       if( _type == BLOCK || _type == UNBLOCK )
       {
           _name = readS();
           _target = L2World.getInstance().getPlayer(_name);
       }
   }
   
   void runImpl()
   {
       L2PcInstance activeChar = getClient().getActiveChar();
       if (activeChar == null)
    	    return;
               
//     FIXME: need to send message like 'Character blah added to blocklist'?
           switch(_type)
           {
               case BLOCK:
                   BlockList.addToBlockList(activeChar, _target);
                   break;
               case UNBLOCK:
                   BlockList.removeFromBlockList(activeChar, _target);
                   break;
               case BLOCKLIST:
                   BlockList.sendListToOwner(activeChar);
                   break;
               case ALLBLOCK:
                   BlockList.setBlockAll(activeChar, true);
                   break;
               case ALLUNBLOCK:
                   BlockList.setBlockAll(activeChar, false);
                   break;
               default:
                   _log.info("Unknown 0x0a block type: " + _type);
           }
   }
   
   public String getType()
   {
       return _C__A0_REQUESTBLOCK;
   }
   private String _name;
   private Integer _type;
   private L2PcInstance _target;
}