/**
 * 
 */
package net.sf.l2j.gameserver.model;

import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.tables.ItemTable;

import java.util.List;
import java.util.logging.Logger;

/**
 * @author zabbix
 * Lets drink to code!
 */

/**
 * Buy/Sell list handler implementation 
 */
public class BuySellList
{
	protected static Logger _log = Logger.getLogger(BuySellList.class.getName());
	
	private List<BuySellItem> _buyItems;	// for buyer
	private List<BuySellItem> _sellItems;	// for seller
	
	private L2PcInstance _requestor;
	private L2PcInstance _acceptor;
	private boolean _locked;

	public BuySellList(L2PcInstance req, L2PcInstance acp)
	{
		if(_acceptor.getTransactionRequester() != null || _requestor.getTransactionRequester() != null
			|| _acceptor.isProcessingTransaction() || _requestor.isProcessingTransaction())
		{
			_log.warning("[BuySellList] transaction already in progress");
			return;
		}

		_requestor = req;
		_acceptor = acp;
	}
	
	/**
	 * Add item to buy/sell list
	 * @param objectId
	 * @param amount
	 * @return
	 */
	public synchronized boolean addItem(Integer objectId, Integer amount, boolean buy)
	{
		if(_acceptor == null || _requestor == null)
			return false;
		
		if(isLocked())
		{
			_log.warning("[BuySellList] attempt to modify locked list!");
			return false;
		}

		L2Object item = L2World.getInstance().findObject(objectId);
		
		if(!(item instanceof L2ItemInstance))
		{
			_log.warning("[BuySellList] attempt to add non-item instance to buy list!");
			return false;
		}
		
		if(!((L2ItemInstance)item).isStackable() && amount > 1)
		{
			_log.warning("[BuySellList] attempt to add non-stackable item with count > 1!");
			return false;
		}

		if(buy)
		{
			_buyItems.add(new BuySellItem(objectId,amount));
		}
		else
		{
			_sellItems.add(new BuySellItem(objectId,amount));
		}
		
		return true;
	}
		
	public void tradeItems()
	{
		if(isLocked())
		{
			_log.warning("[BuySellList] attempt to trade with locked list!");
			return;
		}
		
		if(!validateTrade())
		{
			_log.warning("[BuySellList] trade validation failed");
			return;
		}
		
		/**
		 * Lock objects
		 */
		
		_acceptor.setIsProcessingTransaction(true);
		_requestor.setIsProcessingTransaction(true);
		
		/**
		 * TradeLists are valid, so lets exchange items...
		 * Lets start from seller move items from him to buyer...
		 */
		
		for(BuySellItem inst : _sellItems)
		{
			L2ItemInstance sellItem = _requestor.getInventory().getItemByObjectId(inst.getItemObjectId());
			
			if(sellItem.isStackable() && sellItem.getCount() > inst.getItemAmount())
			{
				L2ItemInstance newStackable = ItemTable.getInstance().createItem(sellItem.getItemId());
				newStackable.setCount(inst.getItemAmount());
				_acceptor.getInventory().addItem(newStackable);
				sellItem.setCount((sellItem.getCount()-inst.getItemAmount()));
			}
			else
			{
				sellItem.setOwnerId(_acceptor.getObjectId());
				sellItem.updateDatabase();
			}
		}
		_sellItems.clear();
		
		/**
		 * kk, lets continue with buyer now...
		 */
		
		for(BuySellItem inst : _buyItems)
		{
			L2ItemInstance buyItem = _acceptor.getInventory().getItemByObjectId(inst.getItemObjectId());
			
			if(buyItem.isStackable() && buyItem.getCount() > inst.getItemAmount())
			{
				L2ItemInstance newStackable = ItemTable.getInstance().createItem(buyItem.getItemId());
				newStackable.setCount(inst.getItemAmount());
				_requestor.getInventory().addItem(newStackable);
				buyItem.setCount((buyItem.getCount()-inst.getItemAmount()));
			}
			else
			{
				buyItem.setOwnerId(_requestor.getObjectId());
				buyItem.updateDatabase();
			}
		}
		
		_buyItems.clear();

		_acceptor.setIsProcessingTransaction(false);
		_requestor.setIsProcessingTransaction(false);
		
		_acceptor = null;
		_requestor = null;
	}
	
	private boolean validateTrade()
	{
		setLocked(true);
		
		L2World world = L2World.getInstance();

		/**
		 * to prevent errors in onDisconnect code...
		 */
		if(_acceptor == null || world.findObject(_acceptor.getObjectId()) == null
				|| _acceptor.isProcessingTransaction())
		{
			_log.warning("[BuySellList] buyer instance are invalid");
			return false;
		}
		if(_requestor == null || world.findObject(_requestor.getObjectId()) == null
				|| _requestor.isProcessingTransaction())
		{
			_log.warning("[BuySellList] seller instance are invalid");
			return false;
		}

		/**
		 * now check items in buysell list...
		 */		
		for(BuySellItem instance : _buyItems)
		{
			if(world.findObject(instance.getItemObjectId()) == null
				|| _acceptor.getInventory().getItemByObjectId(instance.getItemObjectId()) == null
				|| instance.getItemAmount() <= 0 )
			{
				return false;
			}
		}

		for(BuySellItem instance : _sellItems)
		{
			if(world.findObject(instance.getItemObjectId()) == null
				|| _requestor.getInventory().getItemByObjectId(instance.getItemObjectId()) == null
				|| instance.getItemAmount() <= 0)
			{
				return false;
			}
		}
		
		/**
		 * Now check players for overload...
		 * TODO:
		 */
		
		return true;
	}
	
	public void setLocked(boolean locked)
	{
		_locked = locked;
	}

	public boolean isLocked()
	{
		return _locked;
	}

	private class BuySellItem
	{
		private Integer _objId;
		private Integer _amount;
		
		public BuySellItem(Integer oid, Integer count)
		{
			_objId = oid;
			_amount = count;
		}
		
		public int getItemObjectId(){return _objId;}
		public int getItemAmount(){return _amount;}
	}
}
