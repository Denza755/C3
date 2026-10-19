package net.sf.l2j.gameserver.model.actor.instance;

import javolution.lang.TextBuilder;
import javolution.util.FastList;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.ItemTable;
import net.sf.l2j.gameserver.ClanTable;
import net.sf.l2j.gameserver.TradeController;
import net.sf.l2j.gameserver.model.CropProcure;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2Clan;
import net.sf.l2j.gameserver.model.L2Manor;
import net.sf.l2j.gameserver.model.L2TradeList;
import net.sf.l2j.gameserver.model.SeedProduction;
import net.sf.l2j.gameserver.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.serverpackets.BuyList;
import net.sf.l2j.gameserver.serverpackets.BuyListSeed;
import net.sf.l2j.gameserver.serverpackets.InventoryUpdate;
import net.sf.l2j.gameserver.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.serverpackets.SellListProcure;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;

/**
 * Done By L2Emuproject Team
 * User: Scar69
 * Date: Jan 05, 2006
 * Time: 20:13 PM
 */
public class L2ManorManagerInstance extends L2NpcInstance
{
    private FastList<SeedProduction> _SeedIds = new FastList<SeedProduction>();

    private L2TradeList _tradeList;

    public L2TradeList getTradeList()
    {
        return _tradeList;
    }

    public L2ManorManagerInstance(int objectId, L2NpcTemplate template)
    {
        //Erased some lines
        super(objectId, template);
    }

    public void onBypassFeedback(L2PcInstance player, String command)
    {
        if (Config.ALLOW_MANOR)
        {
            if (command.startsWith("buy_seeds"))
            {
                int castleID = Integer.parseInt(command.substring(10));
                _tradeList = new L2TradeList(0);
                // L2Object target = player.getTarget();
                //  L2ManorManagerInstance manor = (target != null && target instanceof L2ManorManagerInstance) ? (L2ManorManagerInstance)target : null;

                _SeedIds = getCastle().getSeedProduction();

                for (SeedProduction s : _SeedIds)
                {
                    L2ItemInstance item = ItemTable.getInstance().createDummyItem(s.getSeedId());
                    item.setPriceToSell(s.getPrice());
                    item.setCount(s.getCanProduce());
                    if ((item.getCount() > 0) && (item.getPriceToSell() > 0)) _tradeList.addItem(item);
                }

                /*            for( int itemId : _SeedIds )
                 {
                 L2ItemInstance item = ItemTable.getInstance().createDummyItem(itemId);
                 if(itemId < 5650 )
                 item.setPriceToSell(1000);
                 else
                 item.setPriceToSell(3000);

                 item.setCount(100);
                 _tradeList.addItem(item);
                 }*/

                BuyListSeed bl = new BuyListSeed(_tradeList, castleID, player.getAdena());
                player.sendPacket(bl);
            }
            else if (command.startsWith("show_procure"))
            {
                showManorProcure(player);
            }
            else if (command.startsWith("show_production"))
            {
                showManorProduction(player);
            }
            else if (command.startsWith("buy_harvester"))
            {
                showBuyWindow(player, "1" + getNpcId());
            }
            else if (command.startsWith("sell_crop"))
            {
                int castleID = Integer.parseInt(command.substring(10));
                player.sendPacket(new SellListProcure(player, castleID));
            }
        }
        else
        {
            TextBuilder msg = new TextBuilder("<html><body>");
            msg.append("Enable the manor system for use this function");
            msg.append("</body></html>");
            this.sendHtmlMessage(player, msg.toString());
        }
        super.onBypassFeedback(player, command);
    }

    public String getHtmlPath(int npcId, int val)
    {
        String pom = "";
        if (val == 0)
        {
            pom = "" + npcId;
        }
        else
        {
            pom = npcId + "-" + val;
        }
        return "data/html/manor/" + pom + ".htm";
    }
    
    private void showBuyWindow(L2PcInstance player, String val)
    {
        double taxRate = 0;
        player.tempInvetoryDisable();

        L2TradeList list = TradeController.getInstance().getBuyList(Integer.parseInt(val));

        if (list != null)
        {
            list.getItems().get(0).setCount(1);
            BuyList bl = new BuyList(list, player.getAdena(), taxRate);
            player.sendPacket(bl);
        }
        else
        {
            _log.info("possible client hacker: " + player.getName() + " attempting to buy from GM shop! < Ban him!");
            _log.info("buylist id:" + val);
        }
        player.sendPacket(new ActionFailed());
    }

    private void sendHtmlMessage(L2PcInstance player, String htmlMessage)
    {
        NpcHtmlMessage html = new NpcHtmlMessage(1);
        html.setHtml(htmlMessage);
        html.replace("%objectId%", String.valueOf(getObjectId()));
        html.replace("%npcname%", getName());
        player.sendPacket(html);
    }
    
    private void showManorProduction(L2PcInstance player)
    {
        L2Clan clan = ClanTable.getInstance().getClan(getCastle().getOwnerId());
        if (clan == null)
            return;
    
        if (getCastle() == null)
            return;

        FastList<SeedProduction> seedes = getCastle().getSeedProduction();
        NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
        String r="";

        for(SeedProduction seede : seedes)
        {
          r += "<tr><td>" + ItemTable.getInstance().getTemplate(seede.getSeedId()).getName() + "</td>";
          r += "<td>" + seede.getCanProduce() + "</td>";
          r += "<td>" + seede.getPrice() + "</td></tr>";
        }
        
        html.setFile("data/html/manor/manor-production.htm");
        html.replace("%objectId%", String.valueOf(getObjectId()));
        html.replace("%npcname%", getName());
        html.replace("%clanleadername%", clan.getLeaderName());
        html.replace("%clanname%", clan.getName());
        html.replace("%castlename%", getCastle().getName());
        html.replace("%table%", r);
        player.sendPacket(html);
    }   

    private void showManorProcure(L2PcInstance player)
    {
         L2Clan clan = ClanTable.getInstance().getClan(getCastle().getOwnerId());
        if (clan == null)
           return;
    
        if (getCastle() == null) 
           return;

        FastList<CropProcure> crops = getCastle().getManorRewards();
        NpcHtmlMessage html = new NpcHtmlMessage(1);
        String r = "";

        for (CropProcure crop : crops)
        {
            int cou = L2Manor.getInstance().getRewardAmount(crop.getId(),
                                                            getCastle().getCropReward(crop.getId()));
            r += "<tr><td>" + ItemTable.getInstance().getTemplate(crop.getId()).getName() + "</td>";
            r += "<td>" + crop.getAmount() + "</td>";
            r += "<td>" + crop.getReward() + "</td><td>" + cou + "</td></tr>";
        }

        html.setFile("data/html/manor/manor-procure.htm");
        html.replace("%objectId%", String.valueOf(getObjectId()));
        html.replace("%npcname%", getName());
        html.replace("%clanleadername%", clan.getLeaderName());
        html.replace("%clanname%", clan.getName());
        html.replace("%castlename%", getCastle().getName());
        html.replace("%table%", r);
        player.sendPacket(html);
    }

}