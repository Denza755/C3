package net.sf.l2j.gameserver.clientpackets;

import java.nio.ByteBuffer;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.ClientThread;
import net.sf.l2j.gameserver.ItemTable;
import net.sf.l2j.gameserver.TradeController;
import net.sf.l2j.gameserver.instancemanager.CastleManager;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2TradeList;
import net.sf.l2j.gameserver.model.actor.instance.L2ManorManagerInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2MerchantInstance;
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
public class RequestBuySeed extends ClientBasePacket {
    private static final String _C__C4_REQUESTBUYSEED = "[C] C4 RequestBuySeed";
    private final static Logger _log = Logger.getLogger(RequestBuySeed.class.getName());
    private final int _listId;
    private int _count;
    private int[] _items;
    /**
     * packet type id 0xc4
     * 
     * sample
     * 
     * c4
     * 00 00 00 00      // list id?
     * 01 00 00 00      // seeds to buy
     * 
     * a4 13 00 00      // seed id
     * 01 00 00 00      // count
     * 
     * 83 06 00 00
     * 01 00 00 00
     * 
     * format:      cdd (dd) 
     * @param decrypt
     */
    public RequestBuySeed(ByteBuffer buf, ClientThread client)
    {
        super(buf, client);
        _listId = readD();
        _count = readD();
        if(_count * 2 < 0) _count = 0;

        _items = new int[_count * 2];
        for (int i = 0; i < _count; i++)
        {
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


/*        if ((target == null                               // No target (ie GM Shop)
                || !(target instanceof L2MerchantInstance || target instanceof L2MercManagerInstance || target instanceof L2ManorManagerInstance)   // Target not a merchant and not mercmanager
                || (player.getDistanceSq(target.getX(), target.getY(), target.getZ()) > L2NpcInstance.INTERACTION_DISTANCE)  // Distance is too far
                    )) return;*/


        L2MerchantInstance merchant = (target != null && target instanceof L2MerchantInstance) ? (L2MerchantInstance)target : null;


        if (_listId > 1000000) // lease
        {
            if (merchant != null && merchant.getTemplate().npcId != _listId-1000000)
            {
                sendPacket(new ActionFailed());
                return;
            }
        }

        if(_count < 1)
        {
            sendPacket(new ActionFailed());
            return;
        }


        double taxRate = 0;
        if (merchant != null && merchant.getIsInTown())
            taxRate = merchant.getCastle().getTaxRate();
        else
            taxRate = 0;


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
            if ( merchant != null )
            {
                if (_listId < 1000000)
                {
                    L2TradeList list = TradeController.getInstance().getBuyList(_listId);
                    price = CastleManager.getInstance().getCastle(player).getSeedPrice(itemId);
                    if (itemId >= 3960 && itemId <= 4026) price *= Config.RATE_SIEGE_GUARDS_PRICE;
                    if (price < 0)
                    {
                        _log.warning("ERROR, no price found .. wrong buylist ??");
                        sendPacket(new ActionFailed());
                        return;
                    }
                }
            }
            else
            if ( manor != null )
            {

                L2TradeList list = manor.getTradeList();

                price =  manor.getCastle().getSeedPrice(itemId);



                if (price <= 0)
                {
                    _log.warning("ERROR, no price found .. wrong buylist ??");
                    sendPacket(new ActionFailed());
                    return;
                }
            }

            subTotal += count * price;  // Before tax
            tax += (int)(subTotal * taxRate);


            if (subTotal + tax > Integer.MAX_VALUE)
            {
                Util.handleIllegalPlayerAction(player,"Warning!! Character "+player.getName()+" of account "+player.getAccountName()+" tried to purchase over "+Integer.MAX_VALUE+" adena worth of goods.",  Config.DEFAULT_PUNISH);
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


        // Charge buyer and add tax to castle treasury if not owned by npc clan
        if ((subTotal < 0) || !player.reduceAdena("Buy", (int)(subTotal + tax), player.getLastFolkNPC(), false))
        {
            sendPacket(new SystemMessage(SystemMessage.YOU_NOT_ENOUGH_ADENA));
            return;
        }


        if (merchant != null && merchant.getIsInTown() && merchant.getCastle().getOwnerId() > 0)
            merchant.getCastle().addToTreasury(tax);


        if (manor != null && manor.getIsInTown() && manor.getCastle().getOwnerId() > 0)
            manor.getCastle().addToTreasury(tax);


        // Proceed the purchase
        InventoryUpdate playerIU = new InventoryUpdate();
        for (int i=0; i < _count; i++)
        {
            int itemId = _items[i * 2 + 0];
            int count  = _items[i * 2 + 1];
            if (count < 0) count = 0;

            // Add item to Inventory and adjust update packet
            L2ItemInstance item;
            if ( merchant != null )
                item = player.getInventory().addItem("Buy", itemId, count, player, merchant);
            else
                item = player.getInventory().addItem("Buy", itemId, count, player, manor);
                
            if (item.getCount() > count) playerIU.addModifiedItem(item);
            else playerIU.addNewItem(item);
            int SeedCount =  manor.getCastle().getSeedAmount(itemId);
            SeedCount = SeedCount - count;
             manor.getCastle().setSeedAmount(itemId,SeedCount);
    manor.getCastle().saveSeedData();
        }
        // Send update packets
        player.sendPacket(playerIU);
        
        StatusUpdate su = new StatusUpdate(player.getObjectId());
        su.addAttribute(StatusUpdate.CUR_LOAD, player.getCurrentLoad());
        player.sendPacket(su);
    }
    public String getType()
    {
        return _C__C4_REQUESTBUYSEED;
    }
}
