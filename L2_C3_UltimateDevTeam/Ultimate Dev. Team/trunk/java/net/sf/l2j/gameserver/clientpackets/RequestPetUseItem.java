package net.sf.l2j.gameserver.clientpackets;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.ClientThread;
import net.sf.l2j.gameserver.handler.IItemHandler;
import net.sf.l2j.gameserver.handler.ItemHandler;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PetInstance;
import net.sf.l2j.gameserver.packets.serverpackets.PetInfo;
import net.sf.l2j.gameserver.packets.serverpackets.PetItemList;
import net.sf.l2j.gameserver.packets.serverpackets.SystemMessage;

import java.nio.ByteBuffer;
import java.util.logging.Logger;

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
		if (activeChar == null)
		    return;
		L2PetInstance pet = (L2PetInstance)activeChar.getPet();
		if (pet == null)
		{
			return;
		}
		L2ItemInstance item = pet.getInventory().getItemByObjectId(_objectId);
		
		if (item == null || item.isWear()) return;
		
		int itemId = item.getItemId();

		if(activeChar.isAlikeDead() || pet.isDead()) 
        {
			activeChar.sendPacket(new SystemMessage(SystemMessage.S1_CANNOT_BE_USED).addItemName(item.getItemId()));
			return;
		}
		if (Config.DEBUG) _log.finest(activeChar.getObjectId()+": pet use item " + _objectId);
		int petId = pet.getNpcId();
		//check if the item matches the pet
		if (item.isEquipable())
		{
			if ((petId == 12077 && item.getItem().isForWolf()) || (petId >= 12311 && petId <= 12313 && item.getItem().isForHatchling()) || (petId >= 12526 && petId <= 12528 && item.getItem().isForStrider()))
			{
				useItem(pet, item, activeChar);
				return;
			}
			else
			{
				activeChar.sendPacket(new SystemMessage(SystemMessage.ITEM_NOT_FOR_PETS));
                return;
			}
		}
		if ((itemId == 2515) || (itemId == 4038) || (itemId == 5168))
		{
			if((petId == 12077 && itemId == 2515) || (petId >= 12311 && petId <= 12313 && itemId == 4038) || (petId >= 12526 && petId <= 12528 && itemId == 5168))
 			{
				feed(activeChar,pet, item);
				return;
			}
		}
	    IItemHandler handler = ItemHandler.getInstance().getItemHandler(item.getItemId());
	    if (handler != null)
		{
			useItem(pet, item, activeChar);
		}
		else
		{
			activeChar.sendPacket(new SystemMessage(SystemMessage.ITEM_NOT_FOR_PETS));
		}
		return;
	}
	
	private synchronized void useItem(L2PetInstance pet, L2ItemInstance item, L2PcInstance activeChar)
	{
		if (item.isEquipable())
		{
			if (item.isEquipped())
				pet.getInventory().unEquipItemInSlot(item.getEquipSlot());
			else
				pet.getInventory().equipItem(item);
			
			activeChar.sendPacket(new PetItemList(pet));
			activeChar.sendPacket(new PetInfo(pet));
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
	private void feed(L2PcInstance player,L2PetInstance pet, L2ItemInstance item)
	{

        if (pet.getMaxFed() <=  pet.getCurrentFed() + item.getCount())
        {
            //Not exists message to send in "systemmsg-e.txt"
            return;
        }
        
		if (pet.getCurrentFed() <= item.getCount())
		{
			pet.setCurrentFed(pet.getCurrentFed() + item.getCount());
			pet.removeItemFromInventory(item,item.getCount());
		}
		else
		{
			pet.removeItemFromInventory(item,pet.getMaxFed() - pet.getCurrentFed());
			pet.setCurrentFed(pet.getMaxFed());
		}
		pet.broadcastStatusUpdate();
	}
	
	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.clientpackets.ClientBasePacket#getType()
	 */
	public String getType()
	{
		return _C__8A_REQUESTPETUSEITEM;
	}
}
