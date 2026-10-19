/*
 * $Header: MultiSellList.java, 2/08/2005 14:21:01 luisantonioa Exp $
 *
 * $Author: luisantonioa $
 * $Date: 2/08/2005 14:21:01 $
 * $Revision: 1 $
 * $Log: MultiSellList.java,v $
 * Revision 1  2/08/2005 14:21:01  luisantonioa
 * Added copyright notice
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
package net.sf.l2j.gameserver.serverpackets;

import java.util.ArrayList;

import net.sf.l2j.gameserver.model.L2Multisell;
import net.sf.l2j.gameserver.model.L2Multisell.MultiSellEntry;


/**
 * This class ...
 * 
 * @version $Revision: 1.2 $ $Date: 2004/06/27 08:12:59 $
 */
public class MultiSellList extends ServerBasePacket
{
    private static final String _S__D0_MULTISELLLIST = "[S] D0 MultiSellList";

    //protected L2PcInstance _player;
    protected int _listId;
    protected ArrayList<MultiSellEntry> entries;

    public MultiSellList(int listId)
    {
        _listId = listId;
    }   

    void runImpl()
    {
        entries = L2Multisell.getInstance().getList(_listId);
    }

    void writeImpl()
    {       
        writeC(0xd0);
        writeD(_listId);        //?? list id
        writeD(entries.size()); //list lenght
            
        int i=1;
        for(MultiSellEntry ent : entries)
        {
            //dhdhdhh
            writeD(i++);      // makeID
            writeH(ent.getItemId());     // item id
            writeD(71);     //?
            writeH(20);     //?

            writeD(ent.getItemAmount());      // Final count 
            writeH(ent.getCompAmount());      // Ingredient Count
            writeH(3);                               // ?

            for(int z=0;z<ent.getCompAmount();z++)
            {
                writeH(ent.getComp(z)[0]);       //ID
                writeH(0);      //??
                writeD(ent.getComp(z)[1]);       //Count
                writeH(ent.getComp(z)[2]);       //Enchant Level
            }
        }    

        writeD(12); //?? 
                
    }

    @Override
    public String getType()
    {
        return _S__D0_MULTISELLLIST;
    }

}

