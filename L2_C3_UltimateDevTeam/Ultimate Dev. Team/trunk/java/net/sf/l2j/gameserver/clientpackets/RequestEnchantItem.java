package net.sf.l2j.gameserver.clientpackets;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.ClientThread;
import net.sf.l2j.gameserver.model.Inventory;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.packets.serverpackets.*;
import net.sf.l2j.gameserver.tables.ItemTable;
import net.sf.l2j.gameserver.templates.L2Item;
import net.sf.l2j.gameserver.util.IllegalPlayerAction;
import net.sf.l2j.gameserver.util.Util;

import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.logging.Logger;

public class RequestEnchantItem extends ClientBasePacket
{
	protected static final Logger _log = Logger.getLogger(Inventory.class.getName());
	private static final String _C__58_REQUESTENCHANTITEM = "[C] 58 RequestEnchantItem";

	private int _objectId;
	/**
	 * packet type id 0x58
	 * 
	 * sample
	 * 
	 * 58
	 * c0 d5 00 10 // objectId
	 * 
	 * format:		cd
	 * @param decrypt
	 */
	public RequestEnchantItem(ByteBuffer buf, ClientThread client)
	{
		super(buf, client);
		_objectId = 0;
	    try {
		_objectId = readD();
	    } catch (Exception e) {}
	}

	void runImpl()
	{
		if (_objectId == 0)
		    return;
		L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
		    return;
        
        L2ItemInstance item = activeChar.getInventory().getItemByObjectId(_objectId);
        L2ItemInstance scroll = activeChar.getEnchantScroll();
        if (activeChar.getPrivateStoreType() != 0)
        if (activeChar.getPrivateStoreType() != 0 || activeChar.isInCrystallize())
        {
            activeChar.setEnchantScroll(null);
            activeChar.sendPacket(new EnchantResult(0));
            activeChar.sendMessage("Cannot enchant items while trading");
        }
		if (item == null || scroll == null)
		{
                        activeChar.setEnchantScroll(null);
                        activeChar.sendPacket(new EnchantResult(0));
			Util.handleIllegalPlayerAction(activeChar,"Player "+activeChar.getName()+" tried to enchant with no enchant scroll equiped", _log, Config.DEFAULT_PUNISH);
			return;
		}
		
		if(item.isWear())
		{
			Util.handleIllegalPlayerAction(activeChar,"Player "+activeChar.getName()+" tried to enchant a weared Item", _log, IllegalPlayerAction.PUNISH_KICKBAN);
			return;
		}
		
    	L2ItemInstance scrollItem = activeChar.getInventory().getItemByObjectId(scroll.getObjectId());
        
        if(scrollItem == null)
        {
        	activeChar.setEnchantScroll(null);
        	activeChar.sendPacket(new EnchantResult(0));
        	activeChar.sendMessage("You dont have such an enchant scroll");
        	Util.handleIllegalPlayerAction(activeChar,"Player "+activeChar.getName()+" tried to enchant with a scroll he doesnt have", _log, Config.DEFAULT_PUNISH);
        	return;
        }

		boolean enchantItem = false;
		boolean blessedScroll = false;
		int crystalId = 0;
		
		// nasty code :P
		switch (item.getItem().getCrystalType())
		{
			case L2Item.CRYSTAL_A:
				crystalId = 1461;
				if (scroll.getItemId() == 729 && item.getItem().getType2() == L2Item.TYPE2_WEAPON){ enchantItem = true; }
				else if (scroll.getItemId() == 730 && (item.getItem().getType2() == L2Item.TYPE2_SHIELD_ARMOR || item.getItem().getType2() == L2Item.TYPE2_ACCESSORY)){ enchantItem = true; }
				else if (scroll.getItemId() == 731 && item.getItem().getType2() == L2Item.TYPE2_WEAPON){ blessedScroll = true; enchantItem = true; }
				else if (scroll.getItemId() == 732 && (item.getItem().getType2() == L2Item.TYPE2_SHIELD_ARMOR || item.getItem().getType2() == L2Item.TYPE2_ACCESSORY)){ blessedScroll = true; enchantItem = true; }
				break;
			case L2Item.CRYSTAL_B:
				crystalId = 1460;
				if (scroll.getItemId() == 947 && item.getItem().getType2() == L2Item.TYPE2_WEAPON){ enchantItem = true; }
				else if (scroll.getItemId() == 948 && (item.getItem().getType2() == L2Item.TYPE2_SHIELD_ARMOR || item.getItem().getType2() == L2Item.TYPE2_ACCESSORY)){ enchantItem = true; }
				else if (scroll.getItemId() == 949 && item.getItem().getType2() == L2Item.TYPE2_WEAPON){ blessedScroll = true; enchantItem = true; }
				else if (scroll.getItemId() == 950 && (item.getItem().getType2() == L2Item.TYPE2_SHIELD_ARMOR || item.getItem().getType2() == L2Item.TYPE2_ACCESSORY)){ blessedScroll = true; enchantItem = true; }
				break;
			case L2Item.CRYSTAL_C:
				crystalId = 1459;
				if (scroll.getItemId() == 951 && item.getItem().getType2() == L2Item.TYPE2_WEAPON){ enchantItem = true; }
				else if (scroll.getItemId() == 952 && (item.getItem().getType2() == L2Item.TYPE2_SHIELD_ARMOR || item.getItem().getType2() == L2Item.TYPE2_ACCESSORY)){ enchantItem = true; }
				else if (scroll.getItemId() == 953 && item.getItem().getType2() == L2Item.TYPE2_WEAPON){ blessedScroll = true; enchantItem = true; }
				else if (scroll.getItemId() == 954 && (item.getItem().getType2() == L2Item.TYPE2_SHIELD_ARMOR || item.getItem().getType2() == L2Item.TYPE2_ACCESSORY)){ blessedScroll = true; enchantItem = true; }
				break;
			case L2Item.CRYSTAL_D:
				crystalId = 1458;
				if (scroll.getItemId() == 955 && item.getItem().getType2() == L2Item.TYPE2_WEAPON){ enchantItem = true; }
				else if (scroll.getItemId() == 956 && (item.getItem().getType2() == L2Item.TYPE2_SHIELD_ARMOR || item.getItem().getType2() == L2Item.TYPE2_ACCESSORY)){ enchantItem = true; }
				else if (scroll.getItemId() == 957 && item.getItem().getType2() == L2Item.TYPE2_WEAPON){ blessedScroll = true; enchantItem = true; }
				else if (scroll.getItemId() == 958 && (item.getItem().getType2() == L2Item.TYPE2_SHIELD_ARMOR || item.getItem().getType2() == L2Item.TYPE2_ACCESSORY)){ blessedScroll = true; enchantItem = true; }
				break;
			case L2Item.CRYSTAL_S:
				crystalId = 1462;
				if (scroll.getItemId() == 959 && item.getItem().getType2() == L2Item.TYPE2_WEAPON){ enchantItem = true; }
				else if (scroll.getItemId() == 960 && (item.getItem().getType2() == L2Item.TYPE2_SHIELD_ARMOR || item.getItem().getType2() == L2Item.TYPE2_ACCESSORY)){ enchantItem = true; }
				else if (scroll.getItemId() == 961 && item.getItem().getType2() == L2Item.TYPE2_WEAPON){ blessedScroll = true; enchantItem = true; }
				else if (scroll.getItemId() == 962 && (item.getItem().getType2() == L2Item.TYPE2_SHIELD_ARMOR || item.getItem().getType2() == L2Item.TYPE2_ACCESSORY)){ blessedScroll = true; enchantItem = true; }
				break;
		}
		
		if (!enchantItem /*|| item.isEquipped()*/)
		{
			activeChar.sendPacket(new EnchantResult(item.getEnchantLevel())); //FIXME i'm really not sure about this...
			activeChar.sendPacket(new SystemMessage(SystemMessage.INAPPROPRIATE_ENCHANT_CONDITION));
			return;
		}
		
		int chance = 0;
		if (item.getEnchantLevel() < 3)
		{
			chance = 100;
		}
		else if (item.getEnchantLevel() < 4 && item.getItem().getBodyPart() == L2Item.SLOT_FULL_ARMOR)
		{
			chance = 100;
		}
		else if (item.getEnchantLevel() < Config.ENCHANT_MAX)
		{
			if (blessedScroll && Config.ENCHANT_CHANCE < 75)
				chance =  75;
			else
				chance =  Config.ENCHANT_CHANCE;
		}
		
		SecureRandom rnd = new SecureRandom();
		int x = rnd.nextInt(100);
		if (x < chance)
		{
            if (item.getEnchantLevel() >= Config.ENCHANT_MAX)
            {
            	SystemMessage sm = new SystemMessage(614);
            	sm.addString("Enchant failed as max enchant level is set to " + Config.ENCHANT_MAX);
            	activeChar.sendPacket(sm);
                
            	return;
           }
           if (item.getEnchantLevel() == 0)
           {
            	activeChar.sendPacket(new SystemMessage(SystemMessage.S1_SUCCESSFULLY_ENCHANTED).addItemName(item.getItemId()));
            }
            else
            {
            	SystemMessage sm = new SystemMessage(SystemMessage.S1_S2_SUCCESSFULLY_ENCHANTED);
            	sm.addNumber(item.getEnchantLevel());
            	sm.addItemName(item.getItemId());
            	activeChar.sendPacket(sm);
            }
            item.setEnchantLevel(item.getEnchantLevel()+1);
            item.updateDatabase();
		}
		else
		{
			if (item.getEnchantLevel() == 0)
			{
				activeChar.sendPacket(new SystemMessage(SystemMessage.ENCHANTMENT_FAILED_S1_EVAPORATED).addItemName(item.getItemId()));
			}
			else
			{
				SystemMessage sm = new SystemMessage(SystemMessage.ENCHANTMENT_FAILED_S1_S2_EVAPORATED);
				sm.addNumber(item.getEnchantLevel());
				sm.addItemName(item.getItemId());
				activeChar.sendPacket(sm);
			}

			SystemMessage sm = new SystemMessage(1064);
			sm.addNumber(item.getEnchantLevel());
			sm.addItemName(item.getItemId());
			activeChar.sendPacket(sm);
			
			L2ItemInstance[] unequiped = activeChar.getInventory().unEquipItemInSlotAndRecord(item.getEquipSlot());
			if (item.isEquipped())
			{
				InventoryUpdate iu = new InventoryUpdate();
			 	for (int i = 0; i < unequiped.length; i++)
			 	{
				 	iu.addModifiedItem(unequiped[i]);
			 	}
			 	activeChar.sendPacket(iu);
			
			 	activeChar.broadcastUserInfo();
			}
			
			L2ItemInstance destroyItem = activeChar.getInventory().destroyItem(_objectId, 1);
			
			InventoryUpdate iu = new InventoryUpdate();
			if (destroyItem.getCount() == 0)
			{
				iu.addRemovedItem(destroyItem);
			}
			else
			{
				iu.addModifiedItem(destroyItem);
			}
			activeChar.sendPacket(iu);
			
			L2ItemInstance crystals = ItemTable.getInstance().createItem(crystalId);			
			int count = (int)(item.getItem().getCrystalCount()*0.67);
			if (count < 1)
			count = 1;
			crystals.setCount(count);
			activeChar.getInventory().addItem(crystals);
            SystemMessage sm2 = new SystemMessage(SystemMessage.EARNED_S2_S1_s);
            sm2.addItemName(crystals.getItemId());
            sm2.addNumber(count);
            activeChar.sendPacket(sm2);
		
            activeChar.sendPacket(new ItemList(activeChar, true));
		
            activeChar.sendPacket(new StatusUpdate(activeChar.getObjectId()).addAttribute(StatusUpdate.CUR_LOAD, activeChar.getCurrentLoad()));
		
            activeChar.broadcastUserInfo();
		
            L2World world = L2World.getInstance();
            world.removeObject(destroyItem);
		}
		activeChar.sendPacket(new StatusUpdate(activeChar.getObjectId()).addAttribute(StatusUpdate.CUR_LOAD, activeChar.getCurrentLoad()));
		
		activeChar.sendPacket(new EnchantResult(item.getEnchantLevel())); //FIXME i'm really not sure about this...
		activeChar.setEnchantScroll(null);
		L2ItemInstance checkScroll = activeChar.getInventory().destroyItem(scroll.getObjectId(), 1);//FIXME why availble this bug...
		if(checkScroll==null)
		{
		  activeChar.getInventory().destroyItem(_objectId, 1);
		  Util.handleIllegalPlayerAction(activeChar,"Player "+activeChar.getName()+" tries enchant "+item.getItemId()+" without scrolls", _log, Config.DEFAULT_PUNISH);
		  activeChar.sendMessage("Congratulations! You found enchant bug!");
		} 
		activeChar.sendPacket(new ItemList(activeChar, false)); //TODO update only the enchanted item
		activeChar.broadcastUserInfo();
	}
	
	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.clientpackets.ClientBasePacket#getType()
	 */
	public String getType()
	{
		return _C__58_REQUESTENCHANTITEM;
	}
}
