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
import net.sf.l2j.gameserver.SevenSignsFestival;
import net.sf.l2j.gameserver.GameServer;
import net.sf.l2j.gameserver.datatables.SkillTable;
import net.sf.l2j.gameserver.communitybbs.Manager.RegionBBSManager;
import net.sf.l2j.gameserver.model.L2Party;
import net.sf.l2j.gameserver.model.Inventory;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.serverpackets.CharSelectInfo;
import net.sf.l2j.gameserver.serverpackets.RestartResponse;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.taskmanager.AttackStanceTaskManager;

/**
 * This class ...
 * 
 * @version $Revision: 1.11.2.1.2.4 $ $Date: 2005/03/27 15:29:30 $
 */
public class RequestRestart extends L2GameClientPacket
{
    private static final String _C__46_REQUESTRESTART = "[C] 46 RequestRestart";
    private static Logger _log = Logger.getLogger(RequestRestart.class.getName());

    /**
     * packet type id 0x46
     * format:      c
     * @param decrypt
     */
    public RequestRestart(ByteBuffer buf, L2GameClient client)
    {
        super(buf, client);
    }

	@Override
	protected void runImpl()
	{
		L2PcInstance player = getClient().getActiveChar();

		// Check if player is == null
		if(player == null)
		{
			_log.warning("[RequestRestart] activeChar null!?");
			return;
		}

		// Check if player is enchanting
		if(player.getActiveEnchantItem() != null)
		{
			sendPacket(RestartResponse.valueOf(false));
			return;
		}

		// Check if player are changing class
		if (player.isLocked())
		{
			_log.severe(" [ERROR] [WARNING]Player " + player.getName() + " tried to restart during class change.");
			sendPacket(RestartResponse.valueOf(false));
			return;
		}

		player.getInventory().updateDatabase();

		// Check if player is in private store
		if(player.getPrivateStoreType() != 0)
		{
			player.sendMessage("Cannot restart while trading.");
			sendPacket(RestartResponse.valueOf(false));
			return;
		}

		// Check if player is in combat
		if(AttackStanceTaskManager.getInstance().getAttackStanceTask(player) && !(player.isGM()))
		{
			if(Config.DEBUG)
				_log.fine("Player " + player.getName() + " tried to logout while fighting.");

			player.sendPacket(new SystemMessage(SystemMessage.CANT_RESTART_WHILE_FIGHTING));
			sendPacket(RestartResponse.valueOf(false));
			return;
		}

        // Check if player is in away mode
		if(player.isAway())
		{
			player.sendMessage("You can't restart in Away mode.");
			sendPacket(RestartResponse.valueOf(false));
			return;
		}

		// Prevent player from restarting if they are a festival participant
		// and it is in progress, otherwise notify party members that the player
		// is not longer a participant.
		if(player.isFestivalParticipant())
		{
			if(SevenSignsFestival.getInstance().isFestivalInitialized())
			{
				player.sendPacket(SystemMessage.sendString("You cannot restart while you are a participant in a festival."));
				player.sendPacket(new ActionFailed());
				sendPacket(RestartResponse.valueOf(false));
				return;
			}

			L2Party playerParty = player.getParty();
			if(playerParty != null)
			{
				player.getParty().broadcastToPartyMembers(SystemMessage.sendString(player.getName() + " has been removed from the upcoming festival."));
			}
		}

		// Fix against exploit anti-target
		if (player.isCastingNow())
		{
			player.abortCast();
			player.sendPacket(new ActionFailed());
		}

		// Check if player is teleporting
		if(player.isTeleporting())
		{
			player.abortCast();
			player.setIsTeleporting(false);
		}

		// Check if player is trading
		if(player.getActiveRequester() != null)
		{
			player.getActiveRequester().onTradeCancel(player);
			player.onTradeCancel(player.getActiveRequester());
		}

		// Check if player are flying
		if(player.isFlying())
		{
			player.removeSkill(SkillTable.getInstance().getInfo(4289, 1));
		}

		L2GameClient client = getClient();

		// detach the client from the char so that the connection isnt closed in the deleteMe
		player.setClient(null);

		RegionBBSManager.getInstance().changeCommunityBoard();

		// removing player from the world
		player.deleteMe();
		client.getActiveChar().store();

		// Restart true
		sendPacket(RestartResponse.valueOf(true));
        getClient().setActiveChar(null);
        // send char list
        CharSelectInfo cl = new CharSelectInfo(getClient().getLoginName(),
                                               getClient().getSessionId().playOkID1);
        sendPacket(cl);
        getClient().setCharSelection(cl.getCharInfo());
    }

    /* (non-Javadoc)
     * @see net.sf.l2j.gameserver.clientpackets.ClientBasePacket#getType()
     */
    public String getType()
    {
        return _C__46_REQUESTRESTART;
    }
}