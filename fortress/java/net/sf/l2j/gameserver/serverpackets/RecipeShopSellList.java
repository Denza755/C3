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
package net.sf.l2j.gameserver.serverpackets;

import net.sf.l2j.gameserver.model.L2ManufactureItem;
import net.sf.l2j.gameserver.model.L2PcInstance;

/**
 * This class ...
 * dddd d(ddd)
 * @version $Revision: 1.1.2.1.2.3 $ $Date: 2005/03/27 15:29:39 $
 */
public class RecipeShopSellList extends ServerBasePacket
{
    
    private static final String _S__D9_RecipeShopSellList = "[S] d9 RecipeShopSellList";
    @SuppressWarnings("unused")
    private L2PcInstance _buyer,_manufacturer;
    
    public RecipeShopSellList(L2PcInstance buyer,L2PcInstance manufacturer)
    {
        _buyer = buyer;
        _manufacturer = manufacturer;
    }
    
	final void runImpl()
	{
		// no long-running tasks
	}
	
	final void writeImpl()
    {
        //dddd d(ddd)
        writeC(0xd9);
        //writeD(_manufacturer.getObjectId());
        //writeD(_buyer.getAdena());
        writeD(_manufacturer.getObjectId());
        writeF(_manufacturer.getCurrentMp());//Creator's MP
        writeD(0x04);//Thing under Creator's MP

        int count = _manufacturer.getCreateList().size();
        writeD(count);
        L2ManufactureItem temp;
        
        for (int i = 0; i < count; i++)
        {
            temp = _manufacturer.getCreateList().getList().get(i);
            writeD(temp.getRecipeId());
            writeD(0x00); //unknown
            writeD(temp.getCost());
        }
            

        //writeD(0x02);
        //writeD(0x03);
        //int count = _manufacturer.getSellList().size();
        
        //TradeItem temp2; 
            
         
        //writeD(count);
        /*int type;
        for (int i = 0; i < count; i++)
        {
            temp2 = (TradeItem) _manufacturer.getSellList().get(i);
            
            writeD(0x00);
            writeD(temp2.getObjectId());
            writeD(temp2.getItemId());
            writeD(temp2.getCount());
            writeH(0x00);
            writeH(0x00);
            writeH(0x00);
            writeD(temp2.getOwnersPrice());
            writeD(temp2.getStorePrice());
            writeD(0); //c2
        }*/
    }
    
    /* (non-Javadoc)
     * @see net.sf.l2j.gameserver.serverpackets.ServerBasePacket#getType()
     */
    public String getType()
    {
        return _S__D9_RecipeShopSellList;
    }

}
