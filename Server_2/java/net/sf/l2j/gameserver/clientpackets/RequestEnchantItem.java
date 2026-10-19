package net.sf.l2j.gameserver.clientpackets;

import java.nio.ByteBuffer;
import java.util.Random;

import net.sf.l2j.gameserver.ClientThread;
import net.sf.l2j.gameserver.ItemTable;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2PcInstance;
import net.sf.l2j.gameserver.serverpackets.EnchantResult;
import net.sf.l2j.gameserver.serverpackets.ItemList;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.templates.L2Item;

public class RequestEnchantItem extends ClientBasePacket
{
	private static final String _C__58_REQUESTENCHANTITEM = "[C] 58 RequestEnchantItem";

	private final int _objectId;
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
		_objectId = readD();
	}

	void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		L2ItemInstance item = activeChar.getInventory().getItemByObjectId(_objectId);
		L2ItemInstance scroll = activeChar.getEnchantScroll();
		if (item == null || activeChar.getEnchantScroll() == null)
		{
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
		
		if (!enchantItem || item.isEquipped())
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
		else
		{
			chance = blessedScroll ? 75 : 50;
		}
		
		Random rnd = new Random();
		int x = rnd.nextInt(100);
		if (x < chance)
		{
			if (item.getEnchantLevel() == 0)
			{
				SystemMessage sm = new SystemMessage(SystemMessage.S1_SUCCESSFULLY_ENCHANTED);
				sm.addItemName(item.getItemId());
				activeChar.sendPacket(sm);
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
				SystemMessage sm = new SystemMessage(SystemMessage.ENCHANTMENT_FAILED_S1_EVAPORATED);
				sm.addItemName(item.getItemId());
				activeChar.sendPacket(sm);
			}
			else
			{
				SystemMessage sm = new SystemMessage(SystemMessage.ENCHANTMENT_FAILED_S1_S2_EVAPORATED);
				sm.addNumber(item.getEnchantLevel());
				sm.addItemName(item.getItemId());
				activeChar.sendPacket(sm);
			}
			L2ItemInstance crystals = ItemTable.getInstance().createItem(crystalId);
			int count = (int)(item.getItem().getCrystalCount()*0.67);
			if (count < 1)
				count = 1;
			crystals.setCount(count);
			activeChar.getInventory().addItem(crystals);
			activeChar.getInventory().destroyItem(item.getObjectId(), 1);
            SystemMessage sm = new SystemMessage(SystemMessage.EARNED_S2_S1_s);
            sm.addItemName(crystals.getItemId());
            sm.addNumber(crystals.getCount());
            activeChar.sendPacket(sm);
		}
		
		activeChar.sendPacket(new EnchantResult(item.getEnchantLevel())); //FIXME i'm really not sure about this...
		activeChar.setEnchantScroll(null);
		activeChar.getInventory().destroyItem(scroll.getObjectId(), 1);
		activeChar.sendPacket(new ItemList(activeChar, false)); //TODO update only the enchanted item
	}
	
	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.clientpackets.ClientBasePacket#getType()
	 */
	public String getType()
	{
		return _C__58_REQUESTENCHANTITEM;
	}
}