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

import javolution.util.FastList;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.ClientThread;
import net.sf.l2j.gameserver.handler.IItemHandler;
import net.sf.l2j.gameserver.handler.ItemHandler;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.packets.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.packets.serverpackets.InventoryUpdate;
import net.sf.l2j.gameserver.packets.serverpackets.ShowCalculator;
import net.sf.l2j.gameserver.packets.serverpackets.SystemMessage;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.logging.Logger;

/**
 * This class ...
 * 
 * @version $Revision: 1.18.2.7.2.9 $ $Date: 2005/03/27 15:29:30 $
 */
public class UseItem extends ClientBasePacket
{
	private static Logger _log = Logger.getLogger(UseItem.class.getName());
	private static final String _C__14_USEITEM = "[C] 14 UseItem";
	
	private final int _objectId;
	/**
	 * packet type id 0x14
	 * format:		cd
	 * @param decrypt
	 */
	public UseItem(ByteBuffer buf, ClientThread client)
	{
		super(buf, client);
		_objectId = readD();
	}

	void runImpl()
	{
        
		L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
			return;
        
        if(activeChar.getPrivateStoreType() != 0)
        {
            activeChar.sendPacket(new SystemMessage(SystemMessage.CANNOT_TRADE_DISCARD_DROP_ITEM_WHILE_IN_SHOPMODE));
            activeChar.sendPacket(new ActionFailed());
            return;
        }
        
		synchronized (activeChar.getInventory())
		{
		    L2ItemInstance item = activeChar.getInventory().getItemByObjectId(_objectId);

            if (item == null)
                return;
            int itemId = item.getItemId();
            // Alt game - Karma punishment // SOE
            if (!Config.ALT_GAME_KARMA_PLAYER_CAN_TELEPORT && 
                    (item.getItemId() == 736 || item.getItemId() == 1538 || item.getItemId() == 1829 || 
                    item.getItemId() == 1830 || item.getItemId() == 3958 || item.getItemId() == 5858 || 
                    item.getItemId() == 5859) &&
                    activeChar.getKarma() > 0)
                return;

            // Items that cannot be used
            if (item.getItemId() == 57)
                return;

            // Char cannot use item when dead
            if(activeChar.isDead()) 
		    {
		        activeChar.sendPacket(new SystemMessage(SystemMessage.S1_CANNOT_BE_USED).addItemName(itemId));
		        return;
		    }
            // Char cannot use pet items
            if (item.getItem().isForWolf() || item.getItem().isForHatchling() || item.getItem().isForStrider())
            {
                activeChar.sendPacket(new SystemMessage(SystemMessage.CANNOT_EQUIP_PET_ITEM).addItemName(itemId));
                return;
            }
		    if (Config.DEBUG) _log.finest(activeChar.getObjectId()+": use item " + _objectId);
		    
		    if (item.isEquipable())
		    {
		    if(item.isEquipped()) {

		    L2ItemInstance[] unequiped =
		    activeChar.getInventory().unEquipItemInBodySlotAndRecord(item.getItem().getBodyPart());

		    activeChar.sendPacket(new SystemMessage(417).addItemName(item.getItemId()));

		    InventoryUpdate iu = new InventoryUpdate();
		    for (int i = 0; i < unequiped.length; i++)
		    {
		    iu.addModifiedItem(unequiped[i]);
		    }

		    activeChar.sendPacket(iu);
		    activeChar.abortAttack();
		    activeChar.broadcastUserInfo();

		    } else {
		        L2ItemInstance[] items = activeChar.getInventory().equipItemAndRecord(item); 
		        activeChar.refreshExpertisePenalty();
		        
		        activeChar.sendPacket(new SystemMessage(49).addItemName(item.getItemId()));
		        
		        activeChar.sendPacket(new InventoryUpdate(new FastList<L2ItemInstance>(Arrays.asList(items))));
		        activeChar.abortAttack();
		        activeChar.broadcastUserInfo();
		    }}
		    else
		    {
		        if (item.getItemId() == 4393)
                    activeChar.sendPacket(new ShowCalculator(4393));
		        else
		        {
                    IItemHandler handler = ItemHandler.getInstance().getItemHandler(item.getItemId());
                    if (handler == null)
                        _log.warning("no itemhandler registered for itemId:" + item.getItemId());
                    else
                        handler.useItem(activeChar, item);
		        }
		    }
		}
	}
	
	public String getType()
	{
		return _C__14_USEITEM;
	}

}
