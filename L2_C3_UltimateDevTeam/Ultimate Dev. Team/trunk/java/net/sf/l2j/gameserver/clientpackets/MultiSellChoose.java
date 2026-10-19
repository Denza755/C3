package net.sf.l2j.gameserver.clientpackets;

import net.sf.l2j.gameserver.ClientThread;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2Multisell;
import net.sf.l2j.gameserver.model.L2Multisell.MultiSellEntry;
import net.sf.l2j.gameserver.model.L2Multisell.MultiSellIngredient;
import net.sf.l2j.gameserver.model.L2Multisell.MultiSellListContainer;
import net.sf.l2j.gameserver.model.PcInventory;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.tables.ItemTable;

import java.nio.ByteBuffer;


public class MultiSellChoose extends ClientBasePacket
{
    private static final String _C__A7_MULTISELLCHOOSE = "[C] A7 MultiSellChoose";
    private int _listId;
    private int _entryId;
    private int _amount;
    
    public MultiSellChoose(ByteBuffer buf, ClientThread client)
    {
        super(buf,client);
        _listId = readD();
        _entryId = readD();
        _amount = readD();
    }
    
    public void runImpl()
    {
    	if(_amount < 1)
    		return;

        MultiSellListContainer list = L2Multisell.getInstance().getList(_listId);
        L2PcInstance player = getClient().getActiveChar();

        for(MultiSellEntry entry : list.getEntries())
        {
            if(entry.getEntryId() == _entryId)
            {
            	doExchange(player,entry);
            	return;
            }
        }
    }
    
    private void doExchange(L2PcInstance player, MultiSellEntry entry)
    {
    	PcInventory inv = player.getInventory();
    	
    	for(MultiSellIngredient e : entry.getIngredients())
    	{
    		L2ItemInstance item = inv.getItemByItemId(e.getItemId());

    		if(item == null || item.getCount() < e.getItemCount() ||
    				item.getEnchantLevel() < e.getItemEnchant())
    		{
    			//TODO: send message "dont have enought items"
    			return;
    		}
    	}
    	
    	/** All ok, remove items and add final product */
    	
    	for(MultiSellIngredient e : entry.getIngredients())
    	{
    		inv.destroyItemByItemId(e.getItemId(),e.getItemCount());
    	}
    	
    	L2ItemInstance product = ItemTable.getInstance().createItem(entry.getProductId());
    	product.setCount(entry.getProductCount());
    	product.setEnchantLevel(entry.getProductEnchant());
    	
    	inv.addItem(product);
    }
    
    public String getType()
    {
        return _C__A7_MULTISELLCHOOSE;
    }
}
