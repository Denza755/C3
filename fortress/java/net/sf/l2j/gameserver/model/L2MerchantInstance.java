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
package net.sf.l2j.gameserver.model;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.StringTokenizer;
import java.util.concurrent.ScheduledFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.l2j.Config;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.ClientScheduler;
import net.sf.l2j.gameserver.ClientThread;
import net.sf.l2j.gameserver.GameServer;
import net.sf.l2j.gameserver.ItemTable;
import net.sf.l2j.gameserver.TradeController;
import net.sf.l2j.gameserver.idfactory.IdFactory;
import net.sf.l2j.gameserver.lib.Rnd;
import net.sf.l2j.gameserver.model.L2ItemInstance.ItemLocation;
import net.sf.l2j.gameserver.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.serverpackets.BuyList;
import net.sf.l2j.gameserver.serverpackets.MultiSellList;
import net.sf.l2j.gameserver.serverpackets.MyTargetSelected;
import net.sf.l2j.gameserver.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.serverpackets.PrivateBuyListSell;
import net.sf.l2j.gameserver.serverpackets.SellList;
import net.sf.l2j.gameserver.serverpackets.StatusUpdate;
import net.sf.l2j.gameserver.serverpackets.WearList;
import net.sf.l2j.gameserver.templates.L2Item;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;

/**
 * This class ...
 *
 * @version $Revision: 1.10.4.9 $ $Date: 2005/04/11 10:06:08 $
 */
public final class L2MerchantInstance extends L2FolkInstance implements Runnable
{
	private static Logger _log = Logger.getLogger(L2MerchantInstance.class.getName());

	private enum BidderDbType {
		RETURN_ADENA,
		LEASEHOLDER,
		AUCTION
	}

	// bigs, tousands
	private static final int[] BidsTable = {
		0,
		10,  20, 30, 40, 50, 60, 70, 80, 90,
		100, 120, 140, 160, 180,
		200, 220, 240, 260, 280,
		300, 325, 350, 375,
		400, 425, 450, 475,
		500, 525, 550, 575,
		600, 650, 700, 750,
		800, 900,
		1000, 1100, 1200, 1300, 1400, 1500, 1600, 1700, 1800, 1900,
		2000, 2200, 2400, 2600, 2800,
		3000, 3250, 3500, 3750,
		4000, 4250, 4500, 4750,
		5000, 5250, 5500, 5750,
		6000, 6500, 7000, 7500,
		8000, 9000,
		10000, 11000, 12000, 13000, 14000, 15000, 16000, 17000, 18000, 19000,
		20000, 22000, 24000, 26000, 28000,
		30000, 32500, 35000, 37500,
		40000, 42500, 45000, 47500,
		50000, 52500, 55000, 57500,
		60000, 65000, 70000, 75000,
		80000, 90000,
		100000
	};

	private static class LeaseBidder {
		int player_id;
		int adena_to_return;
	}

	private static final ArrayList<LeaseBidder> biddersList;
	static {
		biddersList = new ArrayList<LeaseBidder>();
		loadBidders();
	}

	private String leaseHolderName;
	private int    leaseHolderId;
	private ArrayList<L2ItemInstance> leaseList;
	private L2ItemInstance leaseAdena;
	private final Date swapLeaseHolderTime;
	private ScheduledFuture swapLeaseHolderTask;

	private String  auctionCurrentPlayerName;
	private int     auctionCurrentPlayerId;
	private int     auctionCurrentBid;

	/**
	 * @param template
	 */
	public L2MerchantInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
		loadMerchant();
		if (leaseHolderId != 0) {
			loadItems(leaseHolderId);
		} else {
			leaseList = new ArrayList<L2ItemInstance>();
		}
		if (leaseAdena == null) {
			leaseAdena = ItemTable.getInstance().createItem(57);
			leaseAdena.setOwnerId(leaseHolderId);
			leaseAdena.setLocation(ItemLocation.LEASE, template.npcId);
			leaseAdena.setCount(0);
			leaseAdena.updateDatabase();
		}
		Calendar c = Calendar.getInstance();
		c.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);
		c.set(Calendar.HOUR_OF_DAY, 23);
		c.set(Calendar.MINUTE, 55);
		c.set(Calendar.SECOND, 0);
		while (c.compareTo(Calendar.getInstance()) <= 0)
			c.add(Calendar.DATE, 7);
		swapLeaseHolderTime = c.getTime();
		swapLeaseHolderTask = ClientScheduler.getInstance().scheduleLow(this, Rnd.get(100, 300000) + swapLeaseHolderTime.getTime() - System.currentTimeMillis());
	}

	public synchronized void run() {
		if (swapLeaseHolderTime.before(new Date()))
			swapLeaseHolder();
	}

	public L2ItemInstance[] listLeaseItems() {
		return leaseList.toArray(new L2ItemInstance[leaseList.size()]);
	}
	public L2ItemInstance getLeaseAdena() {
		return leaseAdena;
	}
	public int getLeaseHolderId() {
		return leaseHolderId;
	}
	public L2ItemInstance getLeaseItemByObjectId(int object_id) {
		for (L2ItemInstance li : leaseList) {
			if (li.getObjectId() == object_id)
				return li;
		}
		return null;
	}

	private static int findBidIndex(int bid) {
		bid /= 1000;
		for (int i=0; i < BidsTable.length; i++) {
			if (bid <= BidsTable[i])
				return i;
		}
		return BidsTable.length;
	}

	@SuppressWarnings("static-access")
	public void onAction(L2PcInstance player)
	{
		if(GameServer.events.onAction(player, this))
			return;

		if (Config.DEBUG) _log.fine("Merchant activated");
		player.sendPacket( new ActionFailed() );
		player.setTarget(this);
		player.setLastFolkNPC(this);
		super.onAction(player);
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

		return "data/html/merchant/" + pom + ".htm";
	}

	private void showWearWindow(L2PcInstance player, int val)
	{
        player.tempInvetoryDisable();
		if (Config.DEBUG) _log.fine("Showing wearlist");
		L2TradeList list = TradeController.getInstance().getBuyList(val);

		if (list != null)
		{
			WearList bl  = new WearList(list, player.getAdena(), player.expertiseIndex);
			player.sendPacket(bl);
		}
		else
		{
			_log.warning("no buylist with id:" +val);
            player.sendPacket( new ActionFailed() );
		}
	}
	private void showBuyWindow(L2PcInstance player, int val)
	{
		double taxRate = 0;
		
		if (getCastle() != null)
		    taxRate = getCastle().getTaxRate();

		player.tempInvetoryDisable();
		if (Config.DEBUG) _log.fine("Showing buylist");
		L2TradeList list = TradeController.getInstance().getBuyList(val);
		if ((list != null)&&(list.getNpcId().equals(String.valueOf(getNpcId()))) )
		{
			BuyList bl  = new BuyList(list, player.getAdena(), taxRate);
			player.sendPacket(bl);
		}
		else
		{
            _log.warning("possible client hacker: " + player.getName() + " attempting to buy from GM shop! < Ban him!");
			_log.warning("buylist id:" +val);
		}

		player.sendPacket( new ActionFailed() );
	}

	private void showSellWindow(L2PcInstance player)
	{
		if (Config.DEBUG) _log.fine("Showing selllist");
		SellList sl = new SellList(player);
		player.sendPacket(sl);

		if (Config.DEBUG) _log.fine("Showing sell window");
		player.sendPacket( new ActionFailed() );
	}

	private void showLeaseBuyWindow(L2PcInstance player) {
		if (Config.DEBUG)
			_log.fine("Showing lease buylist");
		player.setLastFolkNPC(this);
		player.sendPacket(new PrivateBuyListSell(player, this));
		player.sendPacket(new ActionFailed());
	}

	private void showLeaseSellWindow(L2PcInstance player) {
		if (Config.DEBUG)
			_log.fine("Showing lease selllist");
		player.setLastFolkNPC(this);
		player.sendPacket(new SellList(player, this));
		player.sendPacket(new ActionFailed());
	}

	private void showManageWindow(L2PcInstance player, String error, int item_id, int enchant)
	{
		if (player.getObjectId() != leaseHolderId) {
			showLeaseWindow(player, error);
			return;
		}
        StringBuffer html1 = new StringBuffer("<html><body>");
		if (error != null)
	        html1.append("Error: "+error+"</br>");
		String name = "???";
		int cur_sell = 0;
		int cur_buy = 0;
		L2Item item = ItemTable.getInstance().getTemplate(item_id);
		if (item != null)
			name = item.getName();
		for (L2ItemInstance li : leaseList) {
			if (li.getItemId() == item_id && li.getEnchantLevel() == enchant) {
				cur_sell = li.getPriceToSell();
				cur_buy  = li.getPriceToBuy();
				break;
			}
		}
		html1.append("<a action=\"bypass -h npc_%objectId%_Lease\">Lease</a></br>");
		html1.append("<a action=\"bypass -h npc_%objectId%_LSell\">Sell</a></br>");
		html1.append("<a action=\"bypass -h npc_%objectId%_LBuy\">Buy</a></br>");
        html1.append("<center>Manage items</center><br>");
		html1.append("<table>");
		html1.append("<tr><td>Item ID</td><td><edit var=\"id\" width=80></td><td width=100>"+name+"</td></tr>");
		html1.append("<tr><td>Enchant lelev</td><td><edit var=\"enchant\" width=80></td><td>"+enchant+"</td></tr>");
		html1.append("<tr><td>Price to sell</td><td><edit var=\"sell\" width=80></td><td>"+cur_sell+"</td></tr>");
		html1.append("<tr><td>Proice to buy</td><td><edit var=\"buy\" width=80></td><td>"+cur_buy+"</td></tr>");
		html1.append("</table><br>");
		html1.append("<button value=\"Set\" action=\"bypass -h npc_%objectId%_Items set $id $sell $buy $enchant\" width=80 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></br>");
		html1.append("<button value=\"View\" action=\"bypass -h npc_%objectId%_Items view $id $enchant\" width=80 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></br>");
		html1.append("<button value=\"Remove\" action=\"bypass -h npc_%objectId%_Items del $id $enchant\" width=80 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></br>");
        html1.append("</body></html>");

		insertObjectIdAndShowChatWindow(player, html1.toString());
	}

	private void showLeaseWindow(L2PcInstance player, String error)
	{
        StringBuffer html1 = new StringBuffer("<html><body>");
		if (error != null)
	        html1.append("Error: "+error+"</br>");
        html1.append("<center>Lease</center></br>");
		NumberFormat nf = NumberFormat.getInstance();
		int idx = 1 + findBidIndex(auctionCurrentBid);
		if (idx >= BidsTable.length) {
	        html1.append("Auction closed</br>");
		} else {
			String bid = nf.format(BidsTable[idx]*1000);
			html1.append("Your bid: <button value=\""+bid+"\" action=\"bypass -h npc_%objectId%_Lease "+BidsTable[idx]*1000+"\" width=80 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></br>");
		}

		String bid = nf.format(auctionCurrentBid);
        html1.append("Last bid: "+bid+" by "+auctionCurrentPlayerName+"</br></br>");
		html1.append("<a action=\"bypass -h npc_%objectId%_LSell\">Sell to leaseholder</a></br>");
		html1.append("<a action=\"bypass -h npc_%objectId%_LBuy\">Buy from leaseholder</a></br>");
        html1.append("</body></html>");

		insertObjectIdAndShowChatWindow(player, html1.toString());
	}

	public void onBypassFeedback(L2PcInstance player, String command)
	{
		player.sendPacket( new ActionFailed() );

		// first do the common stuff
		// and handle the commands that all NPC classes know
		//super.onBypassFeedback(player, command);

        StringTokenizer st = new StringTokenizer(command, " ");
        String actualCommand = st.nextToken(); // Get actual command

        if (actualCommand.equalsIgnoreCase("TerritoryStatus"))
		{
    		NpcHtmlMessage html = new NpcHtmlMessage(1);
    		html.setFile("data/html/merchant/territorystaus.htm");
    		html.replace("%objectId%", String.valueOf(getObjectId()));
    		html.replace("%npcname%", getName());
    		
    		if (getCastle() != null && getCastle().getCastleId() > 0)
    		{
        		html.replace("%castlename%", getCastle().getName());
        		html.replace("%taxpercent%", "" + getCastle().getTaxPercent());

        		if (getCastle().getOwnerId() > 0)
        		{
        		    L2Clan clan = new L2Clan(getCastle().getOwnerId());
            		html.replace("%clanname%", clan.getName());
            		html.replace("%clanleadername%", clan.getLeaderName());
        		}
        		else
        		{
            		html.replace("%clanname%", "NPC");
            		html.replace("%clanleadername%", "NPC");
        		}
    		}
    		else
    		{
        		html.replace("%castlename%", "Open");
        		html.replace("%taxpercent%", "0");

        		html.replace("%clanname%", "No");
        		html.replace("%clanleadername%", getName());
    		}

    		player.sendPacket(html);
		}
		else if (actualCommand.equalsIgnoreCase("Buy"))
		{
	        if (st.countTokens() < 1) {return;}
			int val = Integer.parseInt(st.nextToken());
			showBuyWindow(player, val);
		}
		else if (actualCommand.equalsIgnoreCase("Sell"))
		{
			showSellWindow(player);
		}
		else if (actualCommand.equalsIgnoreCase("Wear") && Config.WEAR_TEST_ENABLED)
		{
	        if (st.countTokens() < 1) {return;}
			int val = Integer.parseInt(st.nextToken());
			showWearWindow(player, val);
		}
        else if (actualCommand.equalsIgnoreCase("Multisell"))
        {
            if (st.countTokens() < 1) {return;}
            int val = Integer.parseInt(st.nextToken());
            player.sendPacket(new MultiSellList(val));
        }
		else if (actualCommand.equalsIgnoreCase("LBuy"))
		{
			showLeaseBuyWindow(player);
		}
		else if (actualCommand.equalsIgnoreCase("LSell"))
		{
			showLeaseSellWindow(player);
		}
		else if (actualCommand.equalsIgnoreCase("Lease"))
		{
	        if (st.countTokens() < 1) {return;}
			String err = null;
			command = st.nextToken();
			if (command.length() > 0) {
				if (command.equals("next")) {
					if (player.isGM())
						err = swapLeaseHolder();
					else
						err = "You are not GM";
				}
				else if (command.equals("manage")) {
					if (player.getObjectId() == leaseHolderId) {
						showManageWindow(player, null, 0, 0);
						return;
					}
					err = "You are not the leaseholder";
				}
				else {
					err = addBid(this, player, Integer.parseInt(command));
				}
			}
			showLeaseWindow(player, err);
		}
		else if (actualCommand.equalsIgnoreCase("Items"))
		{
	        if (st.countTokens() < 1) {return;}
			String action    = st.nextToken();
			int item_id = 0;
			int sell    = 0;
			int buy     = 0;
			int enchant = 0;
			if (action.equals("set")) {
				String s_item_id = st.nextToken();
				String s_sell    = st.nextToken();
				String s_buy     = st.nextToken();
				item_id = Integer.parseInt(s_item_id);
				sell    = Integer.parseInt(s_sell);
				buy     = Integer.parseInt(s_buy);
				if (st.hasMoreTokens())
					enchant = Integer.parseInt(st.nextToken());
				setLeaseItemInfo(item_id, sell, buy, enchant);
			}
			else if (action.equals("del")) {
				String s_item_id = st.nextToken();
				item_id = Integer.parseInt(s_item_id);
				if (st.hasMoreTokens())
					enchant = Integer.parseInt(st.nextToken());
			}
			else if (action.equals("view")) {
				String s_item_id = st.nextToken();
				item_id = Integer.parseInt(s_item_id);
				if (st.hasMoreTokens())
					enchant = Integer.parseInt(st.nextToken());
			}
			showManageWindow(player, null, item_id, enchant);
		}
		else
		{
			// this class dont know any other commands, let forward
			// the command to the parent class

			super.onBypassFeedback(player, command);
		}
	}

    @SuppressWarnings("static-access")
	public void onActionShift(ClientThread client)
    {
        L2PcInstance player = client.getActiveChar();
		
		if(GameServer.events.onActionShift(player, this))
			return;

        if (player.getAccessLevel() >= Config.GM_ACCESSLEVEL)
        {
            player.setTarget(this);
            MyTargetSelected my = new MyTargetSelected(getObjectId(), player.getLevel() - getLevel());
            player.sendPacket(my);

            if (isAutoAttackable(player))
            {
                StatusUpdate su = new StatusUpdate(getObjectId());
                su.addAttribute(StatusUpdate.CUR_HP, (int)getCurrentHp() );
                su.addAttribute(StatusUpdate.MAX_HP, getMaxHp() );
                player.sendPacket(su);
            }

            NpcHtmlMessage html = new NpcHtmlMessage(1);
            StringBuffer html1 = new StringBuffer("<html><body><table border=0>");
            html1.append("<tr><td>Current Target:</td></tr>");
            html1.append("<tr><td><br></td></tr>");

            html1.append("<tr><td>Object ID: "+getObjectId()+"</td></tr>");
            html1.append("<tr><td>Template ID: "+getTemplate().npcId+"</td></tr>");
            html1.append("<tr><td><br></td></tr>");

            html1.append("<tr><td>HP: "+getCurrentHp()+"</td></tr>");
            html1.append("<tr><td>MP: "+getCurrentMp()+"</td></tr>");
            html1.append("<tr><td>Level: "+getLevel()+"</td></tr>");
            html1.append("<tr><td><br></td></tr>");

            html1.append("<tr><td>Class: "+getClass().getName()+"</td></tr>");
            html1.append("<tr><td><br></td></tr>");

            //changed by terry 2005-02-22 21:45
            html1.append("</table><table><tr><td><button value=\"Edit NPC\" action=\"bypass -h admin_edit_npc " + getTemplate().npcId + "\" width=100 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
            html1.append("<td><button value=\"Kill\" action=\"bypass -h admin_kill\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
            html1.append("<tr><td><button value=\"Show DropList\" action=\"bypass -h admin_show_droplist " + getTemplate().npcId + "\" width=100 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
            html1.append("<td><button value=\"Delete\" action=\"bypass -h admin_delete\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
            html1.append("</table>");

            if (player.isGM())
            {
                html1.append("<button value=\"View Shop\" action=\"bypass -h admin_showShop " + getTemplate().npcId + "\" width=100 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></br>");
                html1.append("<button value=\"Lease next week\" action=\"bypass -h npc_"+getObjectId()+"_Lease\" width=100 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
                html1.append("<button value=\"Abort current leasing\" action=\"bypass -h npc_"+getObjectId()+"_Lease next\" width=100 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
                html1.append("<button value=\"Manage items\" action=\"bypass -h npc_"+getObjectId()+"_Lease manage\" width=100 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
            }
            html1.append("</body></html>");

            html.setHtml(html1.toString());
            player.sendPacket(html);
        }
        else
        {
            player.setTarget(this);
            MyTargetSelected my = new MyTargetSelected(getObjectId(), player.getLevel() - getLevel());
            player.sendPacket(my);

			if (player.getObjectId() != leaseHolderId)
				showLeaseWindow(player, null);
			else
				showManageWindow(player, null, 0, 0);
        }

        player.sendPacket(new ActionFailed());
    }

	public L2ItemInstance findLeaseItem(int item_id, int enchant)
	{
		if (item_id == 57)
			return leaseAdena;
		for (L2ItemInstance li : leaseList) {
			if (li.getItemId() == item_id && li.getEnchantLevel() == enchant)
				return li;
		}
		return null;
	}

	private synchronized void setLeaseItemInfo(int item_id, int sell, int buy, int enchant)
	{
		L2Item item = ItemTable.getInstance().getTemplate(item_id);
		if (item == null || ! item.isSellable())
			return;
		if (item.isStackable())
			enchant = 0;
		for (L2ItemInstance li : leaseList)
		{
			if (li.getItemId() == item_id && li.getEnchantLevel() == enchant) {
				li.setPriceToSell(sell);
				li.setPriceToBuy(buy);
				li.updateDatabase();
				return;
			}
		}
		L2ItemInstance inst = new L2ItemInstance(IdFactory.getInstance().getNextId(), item_id);
		inst.setOwnerId(leaseHolderId);
		inst.setLocation(ItemLocation.LEASE, getTemplate().npcId);
		inst.setCount(0);
		inst.setEnchantLevel(enchant);
		inst.setPriceToSell(sell);
		inst.setPriceToBuy(buy);
		leaseList.add(inst);
		inst.updateDatabase();
	}

    private synchronized String swapLeaseHolder()
    {
		swapLeaseHolderTask.cancel(false);
		Calendar c = Calendar.getInstance();
		c.setTime(swapLeaseHolderTime);
		c.add(Calendar.DATE, 7);
		swapLeaseHolderTime.setTime(c.getTime().getTime());
		swapLeaseHolderTask = ClientScheduler.getInstance().scheduleLow(this, Rnd.get(100,300000) + swapLeaseHolderTime.getTime() - System.currentTimeMillis());

    	final int    oldHolderId   = leaseHolderId;
    	leaseHolderName = auctionCurrentPlayerName;
    	leaseHolderId   = auctionCurrentPlayerId;
		auctionCurrentPlayerName = null;
		auctionCurrentPlayerId = 0;
		auctionCurrentBid = 0;

		// simple case, next holder is the same as previous one
		if (oldHolderId == leaseHolderId) {
			removeBidder(this.getTemplate().npcId, leaseHolderId, BidderDbType.AUCTION);
			return null;
		}

		// remove old holder and set new one
    	leaseList = new ArrayList<L2ItemInstance>();
		if (oldHolderId != 0) {
			removeBidder(this.getTemplate().npcId, oldHolderId, BidderDbType.LEASEHOLDER);
		}
		if (leaseHolderId != 0) {
			removeBidder(this.getTemplate().npcId, leaseHolderId, BidderDbType.AUCTION);
			addBidder(this.getTemplate().npcId, leaseHolderId, BidderDbType.LEASEHOLDER, 0, leaseHolderName);
		}
		if (oldHolderId != 0){
			returnItems(getTemplate().npcId, oldHolderId); // will be returned on player's entry
			return null;
		}
		return null;
    }


	public synchronized static void playerEnter(L2PcInstance player)
	{
		for (LeaseBidder b : biddersList) {
			if (b.player_id == player.getObjectId()) {
				removeBidder(0, b.player_id, BidderDbType.RETURN_ADENA);
				player.addAdena(b.adena_to_return);
				biddersList.remove(b);
				break;
			}
		}
	}

	private synchronized static String addBid(L2MerchantInstance merchant, L2PcInstance player, int adena)
	{
		int idx = findBidIndex(adena);
		adena = BidsTable[idx] * 1000;
		if (adena <= merchant.auctionCurrentBid)
			return merchant.auctionCurrentPlayerName+" already make a bid of "+merchant.auctionCurrentBid+" adena";
		if (merchant.auctionCurrentPlayerId == player.getObjectId())
		{
			adena -= merchant.auctionCurrentBid;
			if (player.getAdena() < adena)
				return "You need "+adena+" adena to make this bid";
			boolean ok = updateBidder(merchant.getTemplate().npcId, player.getObjectId(),
					BidderDbType.AUCTION, merchant.auctionCurrentBid + adena);
			if (!ok)
				return "Cannot make the bid, please retry";
			player.reduceAdena(adena);
			merchant.auctionCurrentBid += adena;
			return null;
		}
        if (player.getAdena() < adena)
        	return "You need "+adena+" adena to make this bid";
        player.reduceAdena(adena);
        if (merchant.auctionCurrentPlayerId != 0) {
        	// return money
        	boolean ok = removeBidder(merchant.getTemplate().npcId, merchant.auctionCurrentPlayerId, BidderDbType.AUCTION);
        	if (!ok)
        		return "Cannot make the bid, please retry";
        	L2PcInstance old = L2World.getPlayer(merchant.auctionCurrentPlayerName);
        	if (old != null && old.isOnline() != 0) {
        		old.addAdena(merchant.auctionCurrentBid);
        		merchant.auctionCurrentPlayerId = 0;
        	} else {
        		for (LeaseBidder b : biddersList) {
        			if (b.player_id == merchant.auctionCurrentPlayerId) {
        				ok = updateBidder(0, b.player_id, BidderDbType.RETURN_ADENA,
        						b.adena_to_return + merchant.auctionCurrentBid);
        				if (!ok)
        					return "Cannot make the bid, please retry";
        				b.adena_to_return += merchant.auctionCurrentBid;
        				merchant.auctionCurrentPlayerId = 0;
        			}
        		}
        		if (merchant.auctionCurrentPlayerId != 0) {
        			LeaseBidder b = new LeaseBidder();
        			b.player_id = merchant.auctionCurrentPlayerId;
        			b.adena_to_return = merchant.auctionCurrentBid;

        			ok = addBidder(0, merchant.auctionCurrentPlayerId, BidderDbType.RETURN_ADENA,
        					merchant.auctionCurrentBid, merchant.auctionCurrentPlayerName);
        			if (!ok)
        				return "Cannot make the bid, please retry";
        			biddersList.add(b);
        			merchant.auctionCurrentPlayerId = 0;
        		}
        	}
        }
        boolean ok = addBidder(merchant.getTemplate().npcId, player.getObjectId(),
        		BidderDbType.AUCTION, adena, player.getName());
        if (!ok)
        	return "Cannot make the bid, please retry";
        merchant.auctionCurrentBid = adena;
        merchant.auctionCurrentPlayerId = player.getObjectId();
        merchant.auctionCurrentPlayerName = player.getName();
		return null;
	}

	private static synchronized boolean addBidder(int merchant_id, int player_id, BidderDbType bt, int bid, String name)
	{
		boolean ok;
		java.sql.Connection con = null;
		try {
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement1 = con.prepareStatement(
					"INSERT INTO merchant_lease (merchant_id, player_id, bid, type, player_name)" +
					" VALUES (?,?,?,?,?)");
			statement1.setInt(1, merchant_id);
			statement1.setInt(2, player_id);
			statement1.setInt(3, bid);
			statement1.setInt(4, bt.ordinal());
			statement1.setString(5, name);
			ok = statement1.executeUpdate() > 0;
			statement1.close();
		} catch (SQLException e) {
			_log.log(Level.SEVERE, "", e);
			return false;
		} finally {
			try { con.close(); } catch (Exception e) {}
		}
		return ok;
	}

	private static synchronized boolean updateBidder(int merchant_id, int player_id, BidderDbType bt, int bid)
	{
		boolean ok;
		java.sql.Connection con = null;
		try {
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement1 = con.prepareStatement(
					"UPDATE merchant_lease SET bid=?" +
					" WHERE merchant_id = ? AND player_id = ? AND type = ?");
			statement1.setInt(1, bid);
			statement1.setInt(2, merchant_id);
			statement1.setInt(3, player_id);
			statement1.setInt(4, bt.ordinal());
			ok = statement1.executeUpdate() > 0;
			statement1.close();
		} catch (SQLException e) {
			_log.log(Level.SEVERE, "", e);
			return false;
		} finally {
			try { con.close(); } catch (Exception e) {}
		}
		return ok;
	}

	private static synchronized boolean removeBidder(int merchant_id, int player_id, BidderDbType bt)
	{
		java.sql.Connection con = null;
		try {
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement1 = con.prepareStatement(
					"DELETE FROM merchant_lease" +
					" WHERE merchant_id = ? AND player_id = ? AND type = ?");
			statement1.setInt(1, merchant_id);
			statement1.setInt(2, player_id);
			statement1.setInt(3, bt.ordinal());
			statement1.executeUpdate();
			statement1.close();
		} catch (SQLException e) {
			_log.log(Level.SEVERE, "", e);
			return false;
		} finally {
			try { con.close(); } catch (Exception e) {}
		}
		return true;
	}

	private static synchronized boolean returnItems(int merchant_id, int player_id)
	{
		boolean ok;
		java.sql.Connection con = null;
		try {
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement1 = con.prepareStatement(
					"UPDATE items" +
					" SET loc='WAREHOUSE', loc_data=0 " +
					" WHERE loc='LEASE' AND loc_data=? AND owner_id = ?");
			statement1.setInt(1, merchant_id);
			statement1.setInt(2, player_id);
			ok = statement1.executeUpdate() > 0;
			statement1.close();
		} catch (SQLException e) {
			_log.log(Level.SEVERE, "", e);
			return false;
		} finally {
			try { con.close(); } catch (Exception e) {}
		}
		return ok;
	}

	private static void loadBidders()
	{
		java.sql.Connection con = null;
		try {
			con = L2DatabaseFactory.getInstance().getConnection();

			PreparedStatement statement;
			ResultSet rset;

			// load list of players we have to return adena, because
			// they were not online when their bids were overtaken
			statement = con.prepareStatement("SELECT merchant_id, player_id, bid, type, player_name FROM merchant_lease WHERE merchant_id = 0");
			rset = statement.executeQuery();
			while(rset.next())
			{
				int objId   = rset.getInt("player_id");
				int adena   = rset.getInt("bid");
				int type    = rset.getInt("type");
				if (type == BidderDbType.RETURN_ADENA.ordinal())
				{
					LeaseBidder b = new LeaseBidder();
					b.player_id = objId;
					b.adena_to_return = adena;
					biddersList.add(b);
				}
			}
			rset.close();
			statement.close();

		} catch (SQLException e) {
			_log.log(Level.SEVERE, "", e);
		} finally {
			try { con.close(); } catch (Exception e) {}
		}

	}

	private void loadMerchant()
	{
		java.sql.Connection con = null;
		try {
			con = L2DatabaseFactory.getInstance().getConnection();

			PreparedStatement statement = con.prepareStatement("SELECT merchant_id, player_id, bid, type, player_name FROM merchant_lease WHERE merchant_id = ?");
			statement.setInt(1, getTemplate().npcId);
			ResultSet rset = statement.executeQuery();
			while(rset.next())
			{
				int objId   = rset.getInt("player_id");
				int bid     = rset.getInt("bid");
				int type    = rset.getInt("type");
				String name = rset.getString("player_name");
				if (type == BidderDbType.LEASEHOLDER.ordinal()) {
					// current leaseholder
					leaseHolderId   = objId;
					leaseHolderName = name;
				}
				else if (type == BidderDbType.AUCTION.ordinal()) {
					// latest bidder for the next week
					auctionCurrentPlayerId = objId;
					auctionCurrentBid = bid;
					auctionCurrentPlayerName = name;
				}
			}
			rset.close();
			statement.close();
		} catch (SQLException e) {
			_log.log(Level.SEVERE, "", e);
		} finally {
			try { con.close(); } catch (Exception e) {}
		}
	}

	private synchronized void loadItems(int player_id)
	{
		leaseList = new ArrayList<L2ItemInstance>();
		java.sql.Connection con = null;
		try {
			con = L2DatabaseFactory.getInstance().getConnection();

			PreparedStatement statement;
			ResultSet rset;

			// load leaseholder's items
			statement = con.prepareStatement(
					"SELECT object_id FROM items" +
					" WHERE loc='LEASE' and loc_data=? AND owner_id=?");
			statement.setInt(1, getTemplate().npcId);
			statement.setInt(2, player_id);
			rset = statement.executeQuery();
			while(rset.next())
			{
				int object_id = rset.getInt("object_id");
				L2ItemInstance item = L2ItemInstance.restoreFromDb(object_id);
				if (item.getItemId() == 57)
					leaseAdena = item;
				else
					leaseList.add(item);
			}
			rset.close();
			statement.close();
		} catch (SQLException e) {
			_log.log(Level.SEVERE, "", e);
		} finally {
			try { con.close(); } catch (Exception e) {}
		}
	}
}
