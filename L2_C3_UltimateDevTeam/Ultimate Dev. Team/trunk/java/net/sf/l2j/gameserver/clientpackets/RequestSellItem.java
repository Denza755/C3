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

import net.sf.l2j.Config;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.ClientThread;
import net.sf.l2j.gameserver.lib.Log;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2FolkInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2MerchantInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2NpcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.packets.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.packets.serverpackets.StatusUpdate;
import net.sf.l2j.gameserver.packets.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.util.Util;

import java.nio.ByteBuffer;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class ...
 * 
 * @version $Revision: 1.3.2.1.2.4 $ $Date: 2005/03/27 15:29:30 $
 */
public class RequestSellItem extends ClientBasePacket
{
	private static final String _C__1E_REQUESTSELLITEM = "[C] 1E RequestSellItem";
	private static Logger _log = Logger.getLogger(RequestSellItem.class.getName());

	private final int _listId;
	private int _count;
	private int[] _items; // count*3
	/**
	 * packet type id 0x1e
	 * 
	 * sample
	 * 
	 * 1e
	 * 00 00 00 00		// list id
	 * 02 00 00 00		// number of items
	 * 
	 * 71 72 00 10		// object id
	 * ea 05 00 00		// item id
	 * 01 00 00 00		// item count
	 * 
	 * 76 4b 00 10		// object id
	 * 2e 0a 00 00		// item id
	 * 01 00 00 00		// item count
	 * 
	 * format:		cdd (ddd)
	 * @param decrypt
	 */
	public RequestSellItem(ByteBuffer buf, ClientThread client)
	{
		super(buf, client);
		_listId = readD();
		_count = readD();
		_items = new int[_count * 3];
		for (int i = 0; i < _count; i++)
		{
			int objectId = readD(); _items[i * 3 + 0] = objectId;
			int itemId   = readD(); _items[i * 3 + 1] = itemId;
			long cnt      = readD(); 
			if (cnt > Integer.MAX_VALUE || cnt < 0)
			{
			    _count = 0; _items = null;
			    return;
			}
			_items[i * 3 + 2] = (int)cnt;
		}
	}

	void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();

        if (activeChar == null)
            return;

        // Alt game - Karma punishment
        if (!Config.ALT_GAME_KARMA_PLAYER_CAN_SHOP && activeChar.getKarma() > 0)
            return;

        L2FolkInstance folk = activeChar.getLastFolkNPC();
		if ((!(folk instanceof L2MerchantInstance) || activeChar.getDistance(folk.getX(), folk.getY()) > L2NpcInstance.INTERACTION_DISTANCE) && !activeChar.isGM())
		{
			sendPacket(new ActionFailed());
			return;
		}
		L2MerchantInstance merchant = (L2MerchantInstance)folk;
		if (_listId > 1000000) // lease
		{
			if (merchant.getTemplate().npcId != _listId-1000000)
			{
				sendPacket(new ActionFailed());
				return;
			}
		}
		
		for (int i = 0; i < _count; i++)
		{
			int objectId = _items[i * 3 + 0];
			int itemId   = _items[i * 3 + 1];
			long cnt      = _items[i * 3 + 2];
			if (cnt > Integer.MAX_VALUE)
			{
				Util.handleIllegalPlayerAction(activeChar,"Warning!! Character "+activeChar.getName()+" of account "+activeChar.getAccountName()+" tried to purchase over "+Integer.MAX_VALUE+" items at the same time.", _log, Config.DEFAULT_PUNISH);
				SystemMessage sm = new SystemMessage(SystemMessage.YOU_HAVE_EXCEEDED_QUANTITY_THAT_CAN_BE_INPUTTED);
				sendPacket(sm);
				return;
			}

			L2ItemInstance item = activeChar.getInventory().getItemByObjectId(objectId);
        		if (item == null || cnt>item.getCount())
            		    continue;
			if (item.isWear())
			{
			    sendPacket(new ActionFailed());
			    return;
			}
			int price = item.getReferencePrice()*(int)cnt/2;
			L2ItemInstance li = null;
			L2ItemInstance la = null;
			if (_listId > 1000000) {
				li = merchant.findLeaseItem(item.getItemId(),item.getEnchantLevel());
				la = merchant.getLeaseAdena();
				if (li == null || la == null)
					continue;
				price = li.getPriceToBuy()*(int)cnt; // player sells, thus merchant buys.
				if (price > la.getCount())
					continue;
			}
			if ((activeChar.getInventory().getItemByObjectId(objectId).getItemId() == itemId)
					&& (activeChar.getInventory().getItemByObjectId(objectId).getCount() >= cnt))
			{
				if (!item.getItem().isSellable())
				{
					if (!(_listId > 1000000 && item.getItemId() == 57 && merchant.getLeaseHolderId() == activeChar.getObjectId()))
						continue;
				}
				
				//activeChar.addAdena(price);
                
                // If player sells the enchant scroll he is using, desactivate it
                if(activeChar.getEnchantScroll() != null && item.getObjectId() == activeChar.getEnchantScroll().getObjectId())
                {
                    activeChar.setEnchantScroll(null);
                }
				
				if (itemId == 2375 // wolf
						|| itemId == 3500 || itemId == 3501 || itemId == 3502 // hatchlings
						|| itemId == 4422 || itemId == 4423 || itemId == 4424) // striders
				{
					int petObjectId = 0;
					java.sql.Connection con = null;
					try
					{
						con = L2DatabaseFactory.getInstance().getConnection();
						PreparedStatement statement = con.prepareStatement("SELECT objId FROM pets WHERE item_obj_id=?");
						statement.setInt(1, objectId);
						ResultSet rset = statement.executeQuery();
						while (rset.next())
						{
							petObjectId = rset.getInt("objId");
						}
						rset.close();
						statement.close();
						
						if (activeChar.getPet() != null && activeChar.getPet().getObjectId() == petObjectId)
						{
							activeChar.getPet().unSummon(activeChar);
						}
						
						// if it's a pet control item, delete the pet
						statement = con.prepareStatement("DELETE FROM pets WHERE item_obj_id=?");
						statement.setInt(1, objectId);
						statement.execute();
						statement.close();
					}
					catch (Exception e)
					{
						_log.log(Level.WARNING, "could not restore pet objectid:", e);
					}
					finally
					{
						try { con.close(); } catch (Exception e) {}
					}
				}
				
				if (Config.LOG_ITEMS) 
				{
				    if (activeChar.isGM())
					Log.add(activeChar.getName()+"("+activeChar.getObjectId()+") selling "+cnt+" "+item.getItem().getName()+"("+itemId+") by price "+price+" adenas", "items_gm");
				    else
					Log.add(activeChar.getName()+"("+activeChar.getObjectId()+") selling "+cnt+" "+item.getItem().getName()+"("+itemId+") by price "+price+" adenas", "items");
				}
				item = activeChar.getInventory().destroyItem(objectId, (int)cnt);
				if (item != null && _listId > 1000000) {
					li.setCount(li.getCount()+(int)cnt);
					li.updateDatabase();
					la.setCount(la.getCount()-price);
					la.updateDatabase();
				}
				activeChar.addAdena(price);
				if (Config.LOG_ITEMS) 
				{
				    if (activeChar.isGM())
					Log.add(activeChar.getName()+"("+activeChar.getObjectId()+") from selling got "+(int)price+" to total adenas of "+activeChar.getAdena()+" adenas", "items_gm");
				    else
					Log.add(activeChar.getName()+"("+activeChar.getObjectId()+") from selling got "+(int)price+" to total adenas of "+activeChar.getAdena()+" adenas", "items");
				}
			}
		}
		StatusUpdate su = new StatusUpdate(activeChar.getObjectId());
		su.addAttribute(StatusUpdate.CUR_LOAD, activeChar.getCurrentLoad());
		activeChar.sendPacket(su);
	}

	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.clientpackets.ClientBasePacket#getType()
	 */
	public String getType()
	{
		return _C__1E_REQUESTSELLITEM;
	}
}
