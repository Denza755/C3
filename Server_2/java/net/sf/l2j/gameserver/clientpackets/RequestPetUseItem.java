package net.sf.l2j.gameserver.clientpackets;

import java.nio.ByteBuffer;
import java.util.logging.Logger;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.ClientThread;
import net.sf.l2j.gameserver.handler.IItemHandler;
import net.sf.l2j.gameserver.handler.ItemHandler;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2PcInstance;
import net.sf.l2j.gameserver.model.L2PetInstance;
import net.sf.l2j.gameserver.serverpackets.PetInfo;
import net.sf.l2j.gameserver.serverpackets.PetItemList;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;

public class RequestPetUseItem extends ClientBasePacket
{
	private static Logger _log = Logger.getLogger(RequestPetUseItem.class.getName());
	private static final String _C__8A_REQUESTPETUSEITEM = "[C] 8a RequestPetUseItem";
	
	private final int _objectId;
	/**
	 * packet type id 0x8a
	 * format:		cd
	 * @param decrypt
	 */
	public RequestPetUseItem(ByteBuffer buf, ClientThread client)
	{
		super(buf, client);
		_objectId = readD();
	}

	void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		L2PetInstance pet = (L2PetInstance)activeChar.getPet();
		L2ItemInstance item = pet.getInventory().getItemByObjectId(_objectId);
		
		if (item == null) { return; }
		
		int itemId = item.getItemId();

		if(activeChar.isAlikeDead() || pet.isDead()) 
        {
			SystemMessage sm = new SystemMessage(SystemMessage.S1_CANNOT_BE_USED);
			sm.addItemName(item.getItemId());
			activeChar.sendPacket(sm);
			return;
		}
		if (Config.DEBUG) _log.finest(activeChar.getObjectId()+": pet use item " + _objectId);
		
		//check if the item matches the pet
		if (item.isEquipable())
		{
			if (pet.getNpcId() == 12077 && // wolf
					((itemId == 2505 || itemId == 3439 || (itemId >= 3902 && itemId <= 3911)) || // wolf weapons
					(itemId == 2506 || (itemId >= 3891 && itemId <= 3901)))) // wolf armors
			{
				useItem(pet, item, activeChar);
				return;
			}
			else if (((pet.getNpcId() >= 12311 && pet.getNpcId() <= 12313) || (pet.getNpcId() >= 12526 && pet.getNpcId() <= 12528)) && //hatchlings and striders
						((itemId >= 3919 && itemId <= 3925) || // hatchling weapons
						(itemId >= 3912 && itemId <= 3918))) // hatchling armors
			{
				useItem(pet, item, activeChar);
				return;
			}
			else
			{
				SystemMessage sm = new SystemMessage(SystemMessage.ITEM_NOT_FOR_PETS);
				activeChar.sendPacket(sm);
			}
		}
	    IItemHandler handler = ItemHandler.getInstance().getItemHandler(item.getItemId());
	    if (handler != null)
		{
			useItem(pet, item, activeChar);
		}
		else
		{
			SystemMessage sm = new SystemMessage(SystemMessage.ITEM_NOT_FOR_PETS);
			activeChar.sendPacket(sm);
		}
		return;
	}
	
	private void useItem(L2PetInstance pet, L2ItemInstance item, L2PcInstance activeChar)
	{
		if (item.isEquipable())
		{
			if (item.isEquipped())
				pet.getInventory().unEquipItemInSlot(item.getEquipSlot());
			else
				pet.getInventory().equipItem(item);
			
			PetItemList pil = new PetItemList(pet);
			activeChar.sendPacket(pil);
			
			PetInfo pi = new PetInfo(pet);
			activeChar.sendPacket(pi);
		}
		else
		{
			//_log.finest("item not equipable id:"+ item.getItemId());
		    IItemHandler handler = ItemHandler.getInstance().getItemHandler(item.getItemId());
		    if (handler == null)
		    {
		        _log.warning("no itemhandler registered for itemId:" + item.getItemId());
		    }
		    else
		    {
		        handler.useItem(pet, item);
		    }
		}
	}
	
	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.clientpackets.ClientBasePacket#getType()
	 */
	public String getType()
	{
		return _C__8A_REQUESTPETUSEITEM;
	}
}
