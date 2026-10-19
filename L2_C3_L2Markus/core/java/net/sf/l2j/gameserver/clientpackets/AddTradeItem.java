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

import java.nio.ByteBuffer;
import java.util.logging.Logger;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.TradeList;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.serverpackets.TradeOtherAdd;
import net.sf.l2j.gameserver.serverpackets.TradeOwnAdd;
import net.sf.l2j.gameserver.serverpackets.TradeUpdate;

/**
 * This class ...
 * 
 * @version $Revision: 1.5.2.2.2.5 $ $Date: 2005/03/27 15:29:29 $
 */
public class AddTradeItem extends L2GameClientPacket
{
    private static final String _C__16_ADDTRADEITEM = "[C] 16 AddTradeItem";
    private static Logger _log = Logger.getLogger(AddTradeItem.class.getName());

    @SuppressWarnings("unused")
    private int _tradeId;
    private int _objectId;
    private int _count;

    public AddTradeItem(ByteBuffer buf, L2GameClient client)
    {
        super(buf, client);
        _tradeId = readD();
        _objectId = readD();
        _count = readD();
    }

	@Override
	protected void runImpl()
	{
		final L2PcInstance player = getClient().getActiveChar();
		if (player == null) // Player null
			return;

		final TradeList trade = player.getActiveTradeList();
		if (trade == null) // Trade null
		{
			_log.warning("Character: " + player.getName() + " requested item:" + _objectId + " add without active tradelist:" + _tradeId);
			return;
		}

		// Check Partner and ocbjectId
		if (trade.getPartner() == null || L2World.getInstance().findObject(trade.getPartner().getObjectId()) == null)
		{
			// Trade partner not found, cancel trade
			if(trade.getPartner() != null)
				_log.warning("Character:" + player.getName() + " requested invalid trade object: " + _objectId);

			player.sendPacket(new SystemMessage(SystemMessage.TARGET_IS_NOT_FOUND_IN_THE_GAME));
			player.cancelActiveTrade();
			return;
		}

		// Check if player has Access level for Transaction
		if (player.getAccessLevel() > 200)
		{
			player.sendMessage("Transactions are disable for your Access Level.");
			player.cancelActiveTrade();
			return;
		}

		// Check validateItemManipulation
		if (!player.validateItemManipulation(_objectId, "trade"))
		{
			player.sendPacket(new SystemMessage(SystemMessage.NOTHING_HAPPENED));
			return;
		}

		// Java Emulator Security
		if (player.getInventory().getItemByObjectId(_objectId) == null || _count <= 0)
		{
			_log.info("Character:" + player.getName() + " requested invalid trade object");
			return;
		}

		final TradeList.TradeItem item = trade.addItem(_objectId, _count);
		if (item == null)
			return;

		player.sendPacket(new TradeOwnAdd(item));
		player.sendPacket(new TradeUpdate(trade, player));
		trade.getPartner().sendPacket(new TradeOtherAdd(item));
	}

    /* (non-Javadoc)
     * @see net.sf.l2j.gameserver.clientpackets.ClientBasePacket#getType()
     */
    public String getType()
    {
        return _C__16_ADDTRADEITEM;
    }
}
