package net.sf.l2j.gameserver.clientpackets;

import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.logging.Logger;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.ClientThread;
import net.sf.l2j.gameserver.ItemTable;
import net.sf.l2j.gameserver.lib.Log;
import net.sf.l2j.gameserver.model.Inventory;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2PcInstance;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.serverpackets.EnchantResult;
import net.sf.l2j.gameserver.serverpackets.InventoryUpdate;
import net.sf.l2j.gameserver.serverpackets.ItemList;
import net.sf.l2j.gameserver.serverpackets.StatusUpdate;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.templates.L2Item;
import net.sf.l2j.gameserver.util.Util;

public class RequestEnchantItem extends ClientBasePacket
{
  protected static final Logger _log = Logger.getLogger(Inventory.class.getName());
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
 * format:cd
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
	if(activeChar == null) return;
    L2ItemInstance item = activeChar.getInventory().getItemByObjectId(_objectId);
    if(item == null) return;
    Log.add("Enchant|"+activeChar.getName()+"|Trying to enchant|"+item.getItemId()+"|+"+item.getEnchantLevel()+"|"+item.getObjectId(),"items");
    if (activeChar.getPrivateStoreType() != 0)
    {
      activeChar.setEnchantScroll(null);
      activeChar.sendPacket(new EnchantResult(0));
      activeChar.sendPacket(new SystemMessage(SystemMessage.INAPPROPRIATE_ENCHANT_CONDITIONS));
      activeChar.sendMessage("Cannot enchant in trade mode");
      Util.handleIllegalPlayerAction(activeChar,"Player "+activeChar.getName()+" tried to enchant during trade", _log, Config.DEFAULT_PUNISH);
      return;
    }
    if (activeChar.getTarget() != null)
    {
      activeChar.setEnchantScroll(null);
      activeChar.sendPacket(new EnchantResult(0));
      activeChar.sendPacket(new SystemMessage(SystemMessage.INAPPROPRIATE_ENCHANT_CONDITIONS));
      activeChar.sendMessage("Cannot enchant while something selected");
      Util.handleIllegalPlayerAction(activeChar,"Player "+activeChar.getName()+" tried to enchant with target selected: "+activeChar.getTarget().toString(), _log, Config.DEFAULT_PUNISH);
      return;
    }

	if(item.getCount()>1)
	{
		activeChar.setEnchantScroll(null);
		activeChar.sendPacket(new EnchantResult(0));
		activeChar.sendPacket(new SystemMessage(SystemMessage.INAPPROPRIATE_ENCHANT_CONDITIONS));
		if(!item.isStackable())
			Log.add("Enchant|"+activeChar.getName()+"|Trying to enchant non-stackable item with count >1!|"+item.getItemId(),"items");
		return;
	}

    L2ItemInstance scroll = activeChar.getEnchantScroll();
    if (item == null || scroll == null)
    {
      activeChar.setEnchantScroll(null);
      activeChar.sendPacket(new EnchantResult(0));
      activeChar.sendPacket(new SystemMessage(SystemMessage.INAPPROPRIATE_ENCHANT_CONDITIONS));
      return;
    }

	int scrollId = scroll.getItemId();
    if(activeChar.getInventory().getItemByItemId(scrollId) == null)
    {
      activeChar.setEnchantScroll(null);
      activeChar.sendPacket(new EnchantResult(0));
      activeChar.sendPacket(new SystemMessage(SystemMessage.INAPPROPRIATE_ENCHANT_CONDITIONS));
      activeChar.sendMessage("You dont have such an enchant scroll");
      Util.handleIllegalPlayerAction(activeChar,"Player "+activeChar.getName()+" tried to enchant with a scroll he doesnt have", _log, Config.DEFAULT_PUNISH);
      return;
    }

	boolean enchantItem = false;
	boolean blessedScroll = false;
	int crystalId = 0;
	int stScrollId = 0;
	int type2 = item.getItem().getType2();

	switch(item.getItem().getCrystalType())
	{
	case L2Item.CRYSTAL_A:
		crystalId = 1461;
		stScrollId = 729;
		break;
	case L2Item.CRYSTAL_B:
		crystalId = 1460;
		stScrollId = 947;
		break;
	case L2Item.CRYSTAL_C:
		crystalId = 1459;
		stScrollId = 951;
		break;
	case L2Item.CRYSTAL_D:
		crystalId = 1458;
		stScrollId = 955;
		break;
	case L2Item.CRYSTAL_S:
		crystalId = 1462;
		stScrollId = 959;
	}

	if(type2 == L2Item.TYPE2_WEAPON)
	{
		if(stScrollId == scrollId)
			enchantItem = true;
		else if(stScrollId + 2 == scrollId)
		{
			enchantItem = true;
			blessedScroll = true;
		}
	}
	else if(type2 == L2Item.TYPE2_SHIELD_ARMOR || type2 == L2Item.TYPE2_ACCESSORY)
	{
		if(++stScrollId == scrollId)
			enchantItem = true;
		else if(stScrollId + 2 == scrollId)
		{
			enchantItem = true;
			blessedScroll = true;
		}
	}

	if(!enchantItem)
	{
		activeChar.sendPacket(new EnchantResult(item.getEnchantLevel())); //FIXME i'm really not sure about this...
		activeChar.sendPacket(new SystemMessage(SystemMessage.INAPPROPRIATE_ENCHANT_CONDITIONS));
		return;
	}

	int chance = 0;
	if (item.getEnchantLevel() < 3)
		chance = 100;
	else if (item.getEnchantLevel() < 4 && item.getItem().getBodyPart() == L2Item.SLOT_FULL_ARMOR)
		chance = 100;
	else
		chance = Config.ENCHANT_CHANCE;

	SecureRandom Rand = new SecureRandom();
	if (Rand.nextInt(100) < chance)
	{
		if (item.getEnchantLevel() >= Config.ENCHANT_MAX)
		{
			activeChar.sendMessage("Enchant failed as max enchant level is set to " + Config.ENCHANT_MAX);
			return;
		}
		if (item.getEnchantLevel() == 0)
		{
			activeChar.sendPacket(new SystemMessage(SystemMessage.S1_HAS_BEEN_SUCCESSFULLY_ENCHANTED).addItemName(item.getItemId()));
		}
		else
		{
			SystemMessage sm = new SystemMessage(SystemMessage._S1_S2_HAS_BEEN_SUCCESSFULLY_ENCHANTED);
			sm.addNumber(item.getEnchantLevel());
			sm.addItemName(item.getItemId());
			activeChar.sendPacket(sm);
		}
		item.setEnchantLevel(item.getEnchantLevel()+1);
		item.updateDatabase();
		Log.add("Enchant|"+activeChar.getName()+"|Successfully enchanted|"+item.getItemId()+"|to+"+item.getEnchantLevel(),"items");
    }
    else
    {
      Log.add("Enchant|"+activeChar.getName()+"|Failed to enchant|"+item.getItemId()+"|+"+item.getEnchantLevel(),"items");
      if(blessedScroll)
      {
    	  item.setEnchantLevel(0);
    	  activeChar.sendPacket(new EnchantResult(item.getEnchantLevel()));
    	  activeChar.setEnchantScroll(null);
    	  return;
      }
      if (item.getEnchantLevel() == 0)
      {
        activeChar.sendPacket(new SystemMessage(SystemMessage.ENCHANTMENT_HAS_FAILED_S1_HAS_BEEN_CRYSTALLIZED).addItemName(item.getItemId()));
      }
      else
      {
        SystemMessage sm = new SystemMessage(SystemMessage.ENCHANTMENT_HAS_FAILED_S1_S2_HAS_BEEN_EVAPORATED);
        sm.addNumber(item.getEnchantLevel());
        sm.addItemName(item.getItemId());
        activeChar.sendPacket(sm);
      }

      SystemMessage sm = new SystemMessage(SystemMessage.EQUIPMENT_OF__S1_S2_HAS_BEEN_REMOVED);
      sm.addNumber(item.getEnchantLevel());
      sm.addItemName(item.getItemId());
      activeChar.sendPacket(sm);

      L2ItemInstance[] unequiped = activeChar.getInventory().unEquipItemInSlotAndRecord(item.getEquipSlot());
      if(item.isEquipped())
      {
        InventoryUpdate iu = new InventoryUpdate();
        for(L2ItemInstance uneq : unequiped)
          iu.addModifiedItem(uneq);

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

      sendPacket(new ItemList(activeChar, true));

      StatusUpdate su = new StatusUpdate(activeChar.getObjectId());
      su.addAttribute(StatusUpdate.CUR_LOAD, activeChar.getCurrentLoad());
      activeChar.sendPacket(su);

      activeChar.broadcastUserInfo();

      L2World.removeObject(destroyItem);
    }
    StatusUpdate su = new StatusUpdate(activeChar.getObjectId());
    su.addAttribute(StatusUpdate.CUR_LOAD, activeChar.getCurrentLoad());
    activeChar.sendPacket(su);

    activeChar.sendPacket(new EnchantResult(item.getEnchantLevel())); //FIXME i'm really not sure about this...
    activeChar.setEnchantScroll(null);
    L2ItemInstance checkScroll = activeChar.getInventory().destroyItem(scroll.getObjectId(), 1);
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
