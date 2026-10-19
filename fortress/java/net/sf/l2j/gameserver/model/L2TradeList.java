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

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import net.sf.l2j.gameserver.serverpackets.InventoryUpdate;
import net.sf.l2j.gameserver.serverpackets.StatusUpdate;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;

/**
 * This class ...
 * 
 * @version $Revision: 1.4.2.1.2.5 $ $Date: 2005/03/27 15:29:33 $
 */
public class L2TradeList
{
    private static Logger _log = Logger.getLogger(L2TradeList.class.getName());
    
	private ArrayList<L2ItemInstance> _items;
	private int _listId;
	private boolean _confirmed;
	private String _Buystorename,_Sellstorename;
    
    private String _npcId;
	
	public L2TradeList(int listId)
	{
		_items = new ArrayList<L2ItemInstance>();
		_listId = listId;
		_confirmed = false;
	}
	
    public void setNpcId(String id)
    {
        _npcId = id;
    }
    
    public String getNpcId()
    {
        return _npcId;
    }
    
	public void addItem(L2ItemInstance item)
	{
		_items.add(item);
	}
	
    public void replaceItem(int itemID, int price)
    {
        for (int i = 0; i < _items.size(); i++)
        {
            L2ItemInstance item = _items.get(i);
            if (item.getItemId() == itemID)
            {
                item.setPriceToSell(price);
            }
        }
    }
    
    public void removeItem(int itemID)
    {
        for (int i = 0; i < _items.size(); i++)
        {
            L2ItemInstance item = _items.get(i);
            if (item.getItemId() == itemID)
            {
                _items.remove(i);
            }
        }
    }
	
	/**
	 * @return Returns the listId.
	 */
	public int getListId()
	{
		return _listId;
	}
	public void setSellStoreName(String name)
	{
		_Sellstorename = name;
	}
	public String getSellStoreName()
	{
		return _Sellstorename;
	}
	public void setBuyStoreName(String name)
	{
		_Buystorename = name;
	}
	public String getBuyStoreName()
	{
		return _Buystorename;
	}
	
	/**
	 * @return Returns the items.
	 */
	public ArrayList<L2ItemInstance> getItems()
	{
		return _items;
	}
	
    public List<L2ItemInstance> getItems(int start, int end)
    {
        return _items.subList(start, end);
    }
	
	public int getPriceForItemId(int itemId)
	{
		for (int i = 0; i < _items.size(); i++)
		{
			L2ItemInstance item = _items.get(i);
			if (item.getItemId() == itemId)
			{
				return item.getPriceToSell();
			}
		}
		return -1;
	}
	public L2ItemInstance getItem(int ObjectId)
	{
		for (int i = 0; i < _items.size(); i++)
		{
			L2ItemInstance item = _items.get(i);
			if (item.getObjectId() == ObjectId)
			{
				return item;
			}
		}
		return null;
	}
	public void setConfirmedTrade(boolean x)
	{
		_confirmed = x;
	}
	public boolean hasConfirmed()
	{
		return _confirmed;
	}
	public void removeItem(int objId,int count)
	{
		L2ItemInstance temp;
		for(int y = 0 ; y < _items.size(); y++)
		{
			temp = _items.get(y);
			if (temp.getObjectId()  == objId)
			{
				if (count == temp.getCount())
				{
					_items.remove(temp);
				}
				
				break;
			}
		}
		
		
	}
	
	public boolean contains(int objId)
	{
		boolean bool = false;
		L2ItemInstance temp;
		for(int y = 0 ; y < _items.size(); y++)
		{
			temp = _items.get(y);
			if (temp.getObjectId()  == objId)
			{
				bool = true;
				break;
			}
		}
		
		return bool;
	}
	
	public boolean validateTrade(L2PcInstance player)
	{
		Inventory playersInv = player.getInventory();
		L2ItemInstance playerItem,temp;
		
		for(int y = 0 ; y < _items.size(); y++)
		{
			temp = _items.get(y);
			playerItem = playersInv.getItemByObjectId(temp.getObjectId());
			if (playerItem == null || playerItem.getCount() < temp.getCount())
				return false;
		}
		return true;
	}
	
	//Call validate before this
	public void tradeItems(L2PcInstance player,L2PcInstance reciever)
	{
		Inventory playersInv = player.getInventory();
		Inventory recieverInv = reciever.getInventory();
		L2ItemInstance playerItem,recieverItem,TransferItem,temp;
		InventoryUpdate update = new InventoryUpdate();
	
		for(int y = 0 ; y < _items.size(); y++)
		{
			temp = _items.get(y);
			  //If player trades the enchant scroll he was using remove its effect
            if(player.getEnchantScroll() != null && temp.getObjectId() == player.getEnchantScroll().getObjectId()) 
            {
                player.setEnchantScroll(null);
            }
           
			playerItem = playersInv.getItemByObjectId(temp.getObjectId());
			TransferItem = playersInv.dropItem(temp.getObjectId(),temp.getCount());
			
			
            if(playerItem == null)
            {
                _log.warning("L2TradeList: PlayersInv.destroyItem returned NULL! player: "+player.getName()+" reciver:"+reciever.getName());
                continue;
            }
            
			if (playerItem.getLastChange() == L2ItemInstance.MODIFIED)
			{
				update.addModifiedItem(playerItem);
			}
			else
			{
			//	L2World.removeObject(playerItem);
				update.addRemovedItem(playerItem);
			}
			
			recieverItem = recieverInv.addItem(TransferItem);
			
			player.sendPacket(update);
			
			update = new InventoryUpdate();
			if (recieverItem.getLastChange() == L2ItemInstance.MODIFIED)
				update.addModifiedItem(recieverItem);
			else
				update.addNewItem(recieverItem);
			
			reciever.sendPacket(update);
		}
		
		// weight status update both player and reciever
		StatusUpdate su = new StatusUpdate(player.getObjectId());
		su.addAttribute(StatusUpdate.CUR_LOAD, player.getCurrentLoad());
		player.sendPacket(su);
		
		su = new StatusUpdate(reciever.getObjectId());
		su.addAttribute(StatusUpdate.CUR_LOAD, reciever.getCurrentLoad());
		reciever.sendPacket(su);
	}
	public void updateBuyList(L2PcInstance player, ArrayList<TradeItem> list)
	{
		
		TradeItem temp;
		int count;
		Inventory playersInv = player.getInventory();
		L2ItemInstance temp2;
		count =0;
		
		while(count!= list.size())
		{
			temp = list.get(count);
			temp2 =playersInv.findItemByItemId(temp.getItemId());
			if (temp2 == null) 
			{
				list.remove(count);
				count = count-1;
			}
			else
			{
				if ( temp.getCount() ==0)
				{
					list.remove(count);
					count =count-1;
				}
			}
			count++;
		}
		
	}
	public void updateSellList(L2PcInstance player, ArrayList<TradeItem> list)
	{
		Inventory playersInv = player.getInventory();
		TradeItem temp;
		L2ItemInstance temp2;
		int count =0;
		while(count != list.size())
		{
			temp = list.get(count);
			temp2 =playersInv.getItemByObjectId(temp.getObjectId());
			if (temp2 == null) 
			{
				list.remove(count);
				count = count-1;
			}
			else
			{
				if (temp2.getCount() < temp.getCount())
				{
					temp.setCount(temp2.getCount());
				}
				
			}
			count++;
		} 
		
	}
	
	public synchronized void buySellItems(L2PcInstance buyer, ArrayList<TradeItem> buyerslist, L2PcInstance seller, ArrayList<TradeItem> sellerslist)
	{
		Inventory sellerInv         = seller.getInventory();
		Inventory buyerInv          = buyer.getInventory();
        
		//TradeItem buyerItem         = null;
        TradeItem temp2             = null;
        
		L2ItemInstance sellerItem   = null;
		L2ItemInstance TransferItem   = null;
        L2ItemInstance temp         = null;
        L2ItemInstance adena        = null;
                
		InventoryUpdate buyerupdate     = new InventoryUpdate();
        InventoryUpdate sellerupdate    = new InventoryUpdate();
        
		int cost, amount, x, y  = 0;
        
		ArrayList<SystemMessage> sysmsgs  = new ArrayList<SystemMessage>();
		SystemMessage msg                 = null;
		
		for(TradeItem buyerItem : buyerslist)
		{
		    for(x=0 ; x < sellerslist.size(); x++)//find in sellerslist
		    {
		        temp2 = sellerslist.get(x);
		        if (temp2.getItemId() == buyerItem.getItemId())
		        {
		            sellerItem = sellerInv.findItemByItemId(buyerItem.getItemId());
		            break;
		        }
		    }
		    
		    if (sellerItem !=null)
		    {
		        if (buyerItem.getCount()> temp2.getCount())
		        {
		            amount = temp2.getCount();
		        }
		        if (buyerItem.getCount()> sellerItem.getCount())
		        {
		            amount = sellerItem.getCount();
		        }
		        else
		        {
		            amount = buyerItem.getCount();
		        }
                if (buyerItem.getCount() > Integer.MAX_VALUE / buyerItem.getOwnersPrice())
                {
                    _log.warning("Integer Overflow on Cost. Possible Exploit attempt between "+buyer.getName()+" and "+seller.getName()+".");
                    return;
                }
                
                cost          =  amount * buyerItem.getOwnersPrice();
                // fix - trying to buy without enougth adena
                if (buyer.getAdena()<cost)
                {
                	_log.warning("buy item whithout full addena sum"+buyer.getName()+" and "+seller.getName()+".");
                	return;
                }
                TransferItem    = sellerInv.dropItem(sellerItem.getObjectId(),amount);
                buyer.reduceAdena(cost);
		        seller.addAdena(cost);
		        temp = buyerInv.addItem(TransferItem);
		        if (amount == 1)//system msg stuff
		        {
		            msg = new SystemMessage(SystemMessage.S1_PURCHASED_S2);
		            msg.addString(buyer.getName());
		            msg.addItemName(sellerItem.getItemId());
		            sysmsgs.add(msg);
		            msg = new SystemMessage(SystemMessage.S1_PURCHASED_S2);
		            msg.addString("You");
		            msg.addItemName(sellerItem.getItemId());
		            sysmsgs.add(msg);
		        }
		        else
		        {
		            msg = new SystemMessage(SystemMessage.S1_PURCHASED_S3_S2_S);
		            msg.addString(buyer.getName());
		            msg.addItemName(sellerItem.getItemId());
		            msg.addNumber(amount);
		            sysmsgs.add(msg);
		            msg = new SystemMessage(SystemMessage.S1_PURCHASED_S3_S2_S);
		            msg.addString("You");
		            msg.addItemName(sellerItem.getItemId());
		            msg.addNumber(amount);
		            sysmsgs.add(msg);
		        }
		        if(temp2.getCount() == buyerItem.getCount())
		        {
		            sellerslist.remove(x);
		            buyerItem.setCount(0);
		        }
		        else
		        {
		            if (buyerItem.getCount()< temp2.getCount())
		            {	
		                temp2.setCount(temp2.getCount()-buyerItem.getCount());
		            }
		            else
		            {
		                buyerItem.setCount(buyerItem.getCount()-temp2.getCount());
		            }
		        }
		        
		        
		        if (sellerItem .getLastChange() == L2ItemInstance.MODIFIED)
		        {
		            sellerupdate.addModifiedItem(sellerItem);
		        }
		        else
		        {
		            L2World.removeObject(sellerItem );
		            sellerupdate.addRemovedItem(sellerItem );
		        }
		        
		        if (temp.getLastChange() == L2ItemInstance.MODIFIED)
		            buyerupdate.addModifiedItem(temp);
		        else
		            buyerupdate.addNewItem(temp);
		        
		        //}
		        
		        sellerItem =  null;
		    }
		}
		if (TransferItem != null)
		{
			//updateSellList(seller,sellerslist);
			adena = seller.getInventory().getAdenaInstance();
			adena.setLastChange(L2ItemInstance.MODIFIED);
			sellerupdate.addModifiedItem(adena);
			adena = buyer.getInventory().getAdenaInstance();
			adena.setLastChange(L2ItemInstance.MODIFIED);
			buyerupdate.addModifiedItem(adena);
			
			seller.sendPacket(sellerupdate);
			buyer.sendPacket(buyerupdate );
			y=0;
			
			for (x=0;x < sysmsgs.size();x++)
			{
				
				if (y == 0)
				{
					seller.sendPacket(sysmsgs.get(x));
					y=1;
				}
				else
				{
					buyer.sendPacket(sysmsgs.get(x));
					y=0;
				}
			}
		}
	}
		
	
	
}

