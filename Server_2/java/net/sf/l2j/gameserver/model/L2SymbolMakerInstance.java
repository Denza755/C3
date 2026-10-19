/*
 * $Header$
 *
 * 
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

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.logging.Logger;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.HennaTreeTable;
import net.sf.l2j.gameserver.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.serverpackets.HennaEquipList;
import net.sf.l2j.gameserver.serverpackets.MyTargetSelected;
import net.sf.l2j.gameserver.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.serverpackets.NpcInfo;
import net.sf.l2j.gameserver.templates.L2Henna;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;

/**
 * This class ...
 * 
 * @version $Revision$ $Date$
 */
public class L2SymbolMakerInstance extends L2FolkInstance
{
	private static Logger _log = Logger.getLogger(L2SymbolMakerInstance.class.getName());
	

	private int _heading;
	private double _collisionRadius;   
	private double _collisionHeight; // this is  positioning the model relative to the ground
	private ArrayList<L2Henna> _hennaEList;
	
	/**
	 * @return Returns the zOffset.
	 */
	public double getCollisionHeight()
	{
		return _collisionHeight;
	}
	
	/**
	 * @param offset The zOffset to set.
	 */
	public void setCollisionHeight(double offset)
	{
		_collisionHeight = offset;
	}
	
	
	
	/**
	 * @return Returns the collisionRadius.
	 */
	public double getCollisionRadius()
	{
		return _collisionRadius;
	}
	
	/**
	 * @param collisionRadius The collisionRadius to set.
	 */
	public void setCollisionRadius(double collisionRadius)
	{
		_collisionRadius = collisionRadius;
	}
	
	/**
	 * @return Returns the unknown1.
	 */
    
	public void onBypassFeedback(L2PcInstance player, String command)
	{
		super.onBypassFeedback(player, command);
		
	if (command.equals("menu_select?ask=-16&reply=1"))
	{
		L2HennaInstance[] henna = HennaTreeTable.getInstance().getAvailableHenna(player, player.getClassId());
		HennaEquipList He = new HennaEquipList(player, henna);
		player.sendPacket(He);
	}	
		else if (command.equals("menu_select?ask=-16&reply=2"))
	{
		_log.warning("Delete a symbol");
	}
	}
	
	public L2SymbolMakerInstance(int objectID, L2NpcTemplate template)
	{
		super(objectID, template);
	}
	
	/**
	 * this is called when a player interacts with this NPC
	 * @param player
	 */
	public void onAction(L2PcInstance player)
	{
		 
		player.sendPacket(new ActionFailed());
		
		if (player.getTarget()==this)
		{
			showMessageWindow(player,1);
		}
		else
		{
		// showMessageWindow(player,1);
		}
		
		
		
		
		player.setTarget(this);
		MyTargetSelected my = new MyTargetSelected(getObjectId(), -15);
		player.sendPacket(my);
		ActionFailed af = new ActionFailed();
		player.sendPacket(af);
		
				
	}
	public boolean addKnownObject(L2Object object, L2Character dropper)
	{
		boolean ret = super.addKnownObject(object, dropper);
		if (!ret)
			return false;

		if (object instanceof L2PcInstance) 
		{
			NpcInfo info = new NpcInfo(this, (L2PcInstance) object); 
			((L2PcInstance)object).sendPacket(info);
		}
		return true;
	}
	
	public void showMessageWindow(L2PcInstance player, int val)
	{
		
		String filename = "data/html/symbolmaker/symbol_maker001.htm";
		File file = new File(Config.DATAPACK_ROOT, filename);
		if (!file.exists())
		{
			NpcHtmlMessage html = new NpcHtmlMessage(1);
			html.setHtml("<html><head><body>My Text is missing:<br>"+filename+"</body></html>");
			player.sendPacket(html);
			
			player.sendPacket( new ActionFailed() );
		}
		else
		{
			FileInputStream fis = null;		
			try
			{
				fis = new FileInputStream(file);
				byte[] raw = new byte[fis.available()];
				fis.read(raw);
				
				//String content = new String(raw, "UTF-8");
				
				String content = new String(raw, "Unicode");
				
				//NpcHtmlMessage m = new NpcHtmlMessage(IdFactory.getInstance().getNextId());
				NpcHtmlMessage m = new NpcHtmlMessage(player.getTarget().getObjectId());
				
				m.setHtml(content);
				player.sendPacket(m);
				
				
				
				ActionFailed af2 = new ActionFailed();
				player.sendPacket(af2);
			}
			catch (Exception e)
			{
				_log.warning("problem with message text "+e);
			}
			finally
			{
				try	{ fis.close(); } catch (Exception e1) {/* ignore this */}
			}
		}
		
		player.sendPacket( new ActionFailed() );
	}



	
	
    /* (non-Javadoc)
     * @see net.sf.l2j.gameserver.model.L2Object#isAttackable()
     */
    public boolean isAutoAttackable(L2Character attacker)
    {
        return false;
    }
}
