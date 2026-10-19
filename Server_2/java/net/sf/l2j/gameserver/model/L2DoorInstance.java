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
package net.sf.l2j.gameserver.model;

import java.util.logging.Logger;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.ClientThread;
import net.sf.l2j.gameserver.ai.CtrlIntention;
import net.sf.l2j.gameserver.ai.L2CharacterAI;
import net.sf.l2j.gameserver.ai.L2DoorAI;
import net.sf.l2j.gameserver.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.serverpackets.DoorStatusUpdate;
import net.sf.l2j.gameserver.serverpackets.MyTargetSelected;
import net.sf.l2j.gameserver.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.serverpackets.ValidateLocation;
import net.sf.l2j.gameserver.templates.L2CharTemplate;
import net.sf.l2j.gameserver.templates.L2Weapon;
import net.sf.l2j.util.L2ObjectHashSet;

/**
 * This class ...
 * 
 * @version $Revision: 1.3.2.2.2.5 $ $Date: 2005/03/27 15:29:32 $
 */
public class L2DoorInstance extends L2Character
{
    protected static Logger _log = Logger.getLogger(L2DoorInstance.class.getName());
	
    private final int _doorId;
	private final String _name;
	private int _open;
	
	/** This class may be created only by L2Character and only for AI */
	public class AIAccessor extends L2Character.AIAccessor
	{
		protected AIAccessor() {}
		public L2DoorInstance getActor() { return L2DoorInstance.this; }
		public void moveTo(int x, int y, int z, int offset) {}
		public void moveTo(int x, int y, int z) {}
		public void stopMove(L2CharPosition pos) {}
		public void doAttack(L2Character target) {}
		public void doCast(L2Skill skill) {}
	}
	
	public L2CharacterAI getAI() {
		return new L2DoorAI(new AIAccessor());
	}
	
	public boolean hasAI() {
		return false;
	}

	/**
	 */
	public L2DoorInstance(int objectId, L2CharTemplate template, int doorId, String name)
	{
		super(objectId, template);
		_doorId = doorId;
		_name = name;
	}
	
	public final int getLevel() {
		return 1;
	}
	
	/**
     * @return Returns the doorId.
     */
    public int getDoorId()
    {
        return _doorId;
    }

	/**
	 * @return Returns the open.
	 */
	public int getOpen()
	{
		return _open;
	}
	/**
	 * @param open The open to set.
	 */
	public void setOpen(int open)
	{
		_open = open;
	}
	
	public int getDamage() {
		int dmg = 6 - (int)Math.ceil(getCurrentHp() / getMaxHp() * 6);
		if (dmg > 6)
			return 6;
		if (dmg < 0)
			return 0;
		return dmg;
	}
	
	public boolean isEnemyOf(L2Character cha) {
		return true;
	}
	
	public boolean isAutoAttackable(L2Character attacker)
    {
        return true;
    }

	public void updateAbnormalEffect() {}
	
	protected int getDistanceToWatchObject(L2Object object) {
		if (!(object instanceof L2PcInstance))
			return 0;
		return 2000;
	}

	protected int getDistanceToForgetObject(L2Object object) {
		if (!(object instanceof L2PcInstance))
			return 0;
		return 4000;
	}

	public L2ItemInstance getActiveWeaponInstance() {
		return null;
	}
	public L2Weapon getActiveWeaponItem() {
		return null;
	}
	public L2ItemInstance getSecondaryWeaponInstance() {
		return null;
	}
	public L2Weapon getSecondaryWeaponItem() {
		return null;
	}

	public void onAction(L2PcInstance player) {
		if (this != player.getTarget())
		{
			if (Config.DEBUG) _log.fine("new target selected:"+getObjectId());
			player.setTarget(this);
			MyTargetSelected my = new MyTargetSelected(getObjectId(), player.getLevel());
			player.sendPacket(my);
			
			if (isAutoAttackable(player))
			{	
				DoorStatusUpdate su = new DoorStatusUpdate(this);
				player.sendPacket(su);
			}
			
			// correct location
			player.sendPacket(new ValidateLocation(this));
		}
		else
		{
			if (isAutoAttackable(player) )
			{
				if (Math.abs(player.getZ() - getZ()) < 400) // this max heigth difference might need some tweaking
				{
					player.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, this);
				}
				else
				{
					player.sendPacket(new ActionFailed());
				}
			}
			if(!isAutoAttackable(player))
			{
				double distance = getDistance(player.getX(), player.getY());
				if (distance > L2NpcInstance.INTERACTION_DISTANCE) {
					player.getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, this);
				} else {
					player.sendPacket(new ActionFailed());					
					player.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE, this);
				}
			}
		}
	}

	public void onActionShift(ClientThread client) {
		L2PcInstance player = client.getActiveChar();
		if (player.getAccessLevel() >= 100) {
			player.setTarget(this);
			MyTargetSelected my = new MyTargetSelected(getObjectId(), player
					.getLevel());
			player.sendPacket(my);

			if (isAutoAttackable(player)) {
				DoorStatusUpdate su = new DoorStatusUpdate(this);
				player.sendPacket(su);
			}

			NpcHtmlMessage html = new NpcHtmlMessage(1);
			StringBuffer html1 = new StringBuffer(
					"<html><body><table border=0>");
			html1.append("<tr><td>S.Y.L. Says:</td></tr>");
			html1.append("<tr><td><br>Hello baby~</td></tr>");

			html1.append("<tr><td>Object ID: " + getObjectId() + "</td></tr>");
			html1.append("<tr><td><br></td></tr>");
			html1.append("<tr><td><br></td></tr>");

			html1.append("<tr><td>Class: " + getClass().getName() + "</td></tr>");
			html1.append("<tr><td><br></td></tr>");

			html1.append("<tr><td><button value=\"Kill\" action=\"bypass -h admin_kill\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
			html1.append("<tr><td><button value=\"Delete\" action=\"bypass -h admin_delete\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
			html1.append("</table></body></html>");

			html.setHtml(html1.toString());
			player.sendPacket(html);
		} else {
			// attack the mob without moving?
		}

		player.sendPacket(new ActionFailed());
	}

	public void broadcastStatusUpdate()
	{
		L2ObjectHashSet<L2PcInstance> knownPlayers = getKnownPlayers(); 
		if (knownPlayers == null || knownPlayers.isEmpty())
			return;
		DoorStatusUpdate su = new DoorStatusUpdate(this);
		for (L2PcInstance player : knownPlayers)
			player.sendPacket(su);
	}
	
	
}
