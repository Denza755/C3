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

import net.sf.l2j.gameserver.ClientThread;
import net.sf.l2j.gameserver.HennaTable;
import net.sf.l2j.gameserver.model.L2HennaInstance;
import net.sf.l2j.gameserver.model.L2PcInstance;
import net.sf.l2j.gameserver.serverpackets.HennaInfo;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.templates.L2Henna;

/**
 * This class ...
 * 
 * @version $Revision$ $Date$
 */
public class RequestHennaEquip extends ClientBasePacket
{
	private static final String _C__BC_RequestHennaEquip = "[C] bc RequestHennaEquip";
	private static Logger _log = Logger.getLogger(RequestHennaEquip.class.getName());
	private final int SymbolId;
	// format  cd
	
	/**
	 * packet type id 0xbb
	 * format:		cd
	 * @param decrypt
	 */
	public RequestHennaEquip(ByteBuffer buf, ClientThread client)
	{
		super(buf, client);
		SymbolId  = readD();
	}

	void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		L2Henna template = HennaTable.getInstance().getTemplate(SymbolId);
    	L2HennaInstance temp = new L2HennaInstance(template);
		temp.setSymbolId(SymbolId);
		temp.setItemIdDye(template.getDyeId());
		temp.setAmountDyeRequire(template.getAmountDyeRequire());
		temp.setPrice(template.getPrice());
		temp.setStatINT(template.getStatINT());
		temp.setStatSTR(template.getStatSTR());
		temp.setStatCON(template.getStatCON());
		temp.setStatMEM(template.getStatMEM());
		temp.setStatDEX(template.getStatDEX());
		temp.setStatWIT(template.getStatWIT());
		
		////TODO: checking the amount of dye and aden
		/*try{
		_count = activeChar.getInventory().getItemByObjectId(temp.getItemIdDye()).getCount();
		}catch(Exception e){};
		
		if ((_count >= temp.getAmountDyeRequire())&& (activeChar.getAdena()>= temp.getPrice()))
		{
		*/
		//TODO: To fix SystemMessage after equip henna
		SystemMessage sm = new SystemMessage(302);
		//sm.addNumber(3);
		sm.addNumber(temp.getItemIdDye());
		activeChar.sendPacket(sm);
		SystemMessage sm2 = new SystemMessage(877);
		activeChar.sendPacket(sm2);
		
		HennaInfo hi = new HennaInfo(temp,activeChar);
		activeChar.sendPacket(hi);
		
		/*
		activeChar.getInventory().reduceAdena(temp.getPrice());
		activeChar.getInventory().destroyItemByItemId(temp.getItemIdDye(),temp.getAmountDyeRequire());
		
		}
		else
		{   //systemMessage can't draw a symbol
			SystemMessage sm3 = new SystemMessage(899);
			activeChar.sendPacket(sm3);
		}
		*/
	}
	
	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.clientpackets.ClientBasePacket#getType()
	 */
	public String getType()
	{
		return _C__BC_RequestHennaEquip;
	}
}
