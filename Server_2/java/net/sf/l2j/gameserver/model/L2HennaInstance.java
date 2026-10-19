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

import net.sf.l2j.gameserver.templates.L2Henna;

/**
 * This class represents a Non-Player-Character in the world. it can be 
 * a monster or a friendly character.
 * it also uses a template to fetch some static values.
 * the templates are hardcoded in the client, so we can rely on them.
 * 
 * @version $Revision$ $Date$
 */

public class L2HennaInstance
{
	private static Logger _log = Logger.getLogger(L2HennaInstance.class.getName());
	
	private L2Henna _template;
	private int _symbolId;
	private int _itemIdDye;
	private int _price;
	private int _statINT;
	private int _statSTR;
	private int _statCON;
	private int _statMEM;
	private int _statDEX;
	private int _statWIT;
	private int _amountDyeRequire;
	
	public L2HennaInstance(L2Henna template)
	{
		_template = template;
	}
	
	public L2Henna getTemplate()
	{
		return _template;
	}
	
	
    public int getSymbolId()
    {
        return _symbolId;
    }
	
	public void setSymbolId(int SymbolId)
	{
		_symbolId = SymbolId;
	}
	
	public int getItemIdDye()
    {
        return _itemIdDye;
    }
	
	public void setItemIdDye(int ItemIdDye)
	{
		_itemIdDye = ItemIdDye;
	}
	
	
	public int getAmountDyeRequire()
	{
		return _amountDyeRequire;
	}
	
	public void setAmountDyeRequire(int AmountDyeRequire)
	{
		_amountDyeRequire = AmountDyeRequire;
	}
	
	public int getPrice()
	{
		return _price;
	}
	
	public void setPrice(int Price)
	{
		_price = Price;
	}
	
	
	public int getStatINT()
	{
		return _statINT;
	}
	
	public void setStatINT(int StatINT)
	{
		_statINT = StatINT;
	}

	public int getStatSTR()
	{
		return _statSTR;
	}
	
	public void setStatSTR(int StatSTR)
	{
		_statSTR = StatSTR;
	}
	
	public int getStatCON()
	{
		return _statCON;
	}
	
	public void setStatCON(int StatCON)
	{
		_statCON = StatCON;
	}
	
	public int getStatMEM()
	{
		return _statMEM;
	}
	
	public void setStatMEM(int StatMEM)
	{
		_statMEM = StatMEM;
	}
	
	public int getStatDEX()
	{
		return _statDEX;
	}
	
	public void setStatDEX(int StatDEX)
	{
		_statDEX = StatDEX;
	}
	
	public int getStatWIT()
	{
		return _statWIT;
	}
	
	public void setStatWIT(int StatWIT)
	{
		_statWIT = StatWIT;
	}
	
		
}
