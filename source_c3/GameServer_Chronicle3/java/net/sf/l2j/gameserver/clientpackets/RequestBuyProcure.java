package net.sf.l2j.gameserver.clientpackets;

import java.nio.ByteBuffer;

import javolution.util.FastList;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.ClientThread;
import net.sf.l2j.gameserver.ItemTable;
import net.sf.l2j.gameserver.model.CropProcure;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2Manor;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.actor.instance.L2ManorManagerInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.serverpackets.InventoryUpdate;
import net.sf.l2j.gameserver.serverpackets.StatusUpdate;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.templates.L2Item;
import net.sf.l2j.gameserver.util.Util;

import java.util.logging.Level;
import java.util.logging.Logger;
/*
 * Done by L2Emuproject team
 * User: Scar69
 * Date: Jan 12, 2006
 * Time: 22:13 PM GMT+1
 */
public class RequestBuyProcure extends ClientBasePacket 
{
    private static final String _C__C3_REQUESTBUYPROCURE = "[C] C3 RequestBuyProcure";
    private final static Logger _log = Logger.getLogger(RequestBuyProcure.class.getName());
    private final int _listId;
    private int _count;
    private int[] _items;
    private FastList<CropProcure> _procureList = new FastList<CropProcure>();
    /**
     * packet type id 0xc3
     * 
     * sample
     * 
     * c3
     * 00 00 00 00  //list id
     * 03 00 00 00  //count of sell mature
     *          //1-st mature
     * a5 93 00 10  //??? posible servise ie...
     * d7 16 00 00  //mature id    
     * 01 00 00 00  //count of mature
     *          //2-nd mature
     * a6 93 00 10 
     * de 16 00 00
     * 01 00 00 00    
     *          //3-rd mature
     * 95 94 00 10
     * e1 16 00 00
     * 01 00 00 00                
     * 
     * 
     * 
     * 
     * 
     * 
     * 
     * format:      cdd (dd) 
     * @param decrypt
     */
    public RequestBuyProcure(ByteBuffer buf, ClientThread client)
    {
        super(buf, client);
        _listId = readD();
        _count = readD();
        if(_count * 2 < 0) _count = 0;

        _items = new int[_count * 2];
        for (int i = 0; i < _count; i++)
        {
            long servise = readD();
            int itemId   = readD(); _items[i * 2 + 0] = itemId;
            long cnt      = readD(); 
            if (cnt > Integer.MAX_VALUE || cnt < 1)
            {
                _count=0; _items = null;
                return;
            }
            _items[i * 2 + 1] = (int)cnt;
        }
    }
    
    void runImpl()
    {
        L2PcInstance player = getClient().getActiveChar();
        if (player == null) return;


        // Alt game - Karma punishment
        if (!Config.ALT_GAME_KARMA_PLAYER_CAN_SHOP && player.getKarma() > 0) return;

    
        L2Object target = player.getTarget();
        //MetalHeart - handle manor manager trade list first. Later will be removed


    /*    if ((target == null                               // No target (ie GM Shop)
                || !(target instanceof L2MerchantInstance || target instanceof L2MercManagerInstance || target instanceof L2ManorManagerInstance)   // Target not a merchant and not mercmanager
                || (player.getDistanceSq(target.getX(), target.getY(), target.getZ()) > L2NpcInstance.INTERACTION_DISTANCE)  // Distance is too far
                    )) return;*/





        if(_count < 1)
        {
            sendPacket(new ActionFailed());
            return;
        }




        long subTotal = 0;
        int tax = 0;
        
        // Check for buylist validity and calculates summary values
        int slots = 0;
        int weight = 0;
        L2ManorManagerInstance manor = (target != null && target instanceof L2ManorManagerInstance) ? (L2ManorManagerInstance)target : null;


        for (int i = 0; i < _count; i++)
        {
            int itemId = _items[i * 2 + 0];
            int count  = _items[i * 2 + 1];
            int price = 0;
            if (count > Integer.MAX_VALUE)
            {
                Util.handleIllegalPlayerAction(player,"Warning!! Character "+player.getName()+" of account "+player.getAccountName()+" tried to purchase over "+Integer.MAX_VALUE+" items at the same time.",  Config.DEFAULT_PUNISH);
                SystemMessage sm = new SystemMessage(SystemMessage.YOU_HAVE_EXCEEDED_QUANTITY_THAT_CAN_BE_INPUTTED);
                sendPacket(sm);
                return;
            }
           


            L2Item template = ItemTable.getInstance().getTemplate(itemId);
            weight += count * template.getWeight();
            if (!template.isStackable()) slots += count;
            else if (player.getInventory().getItemByItemId(itemId) == null) slots++;
        }

        if (!player.getInventory().validateWeight(weight))
        {
            sendPacket(new SystemMessage(SystemMessage.WEIGHT_LIMIT_EXCEEDED));
            return;
        }

    
        if (!player.getInventory().validateCapacity(slots))
        {
            sendPacket(new SystemMessage(SystemMessage.SLOTS_FULL));
            return;
        }
        
        // Proceed the purchase
        InventoryUpdate playerIU = new InventoryUpdate();
        _procureList =  manor.getCastle().getCropProcure();

        for (int i=0; i < _count; i++)
        {
            int itemId = _items[i * 2 + 0];
            int count  = _items[i * 2 + 1];
            if (count < 0) count = 0;
            int nagrada=L2Manor.getInstance().getRewardItem(itemId,manor.getCastle().getCropReward(itemId));

            int cou= L2Manor.getInstance().getRewardAmount(itemId,manor.getCastle().getCropReward(itemId));
            cou= count / cou;
            // Add item to Inventory and adjust update packet
            L2ItemInstance item;
            item = player.getInventory().addItem("Manor",nagrada , cou, player, manor);
            L2ItemInstance iteme;
            iteme = player.getInventory().destroyItemByItemId("Manor",itemId,count,player,manor);

            playerIU.addRemovedItem(iteme);
            if (item.getCount() > cou) playerIU.addModifiedItem(item);
            else playerIU.addNewItem(item);
            int ProcureCount =  manor.getCastle().getCropAmount(itemId);
            ProcureCount = ProcureCount - count;
             manor.getCastle().setCropAmount(itemId,ProcureCount);
             manor.getCastle().saveCropData();
        }
        // Send update packets
        player.sendPacket(playerIU);
        
        StatusUpdate su = new StatusUpdate(player.getObjectId());
        su.addAttribute(StatusUpdate.CUR_LOAD, player.getCurrentLoad());
        player.sendPacket(su);
    }
    public String getType()
    {
        return _C__C3_REQUESTBUYPROCURE;
    }
}