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
import net.sf.l2j.gameserver.model.L2ManufactureList;
import net.sf.l2j.gameserver.model.L2RecipeList;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;

import java.util.Iterator;

/**
 * dd d(dd) d(ddd)
 * @version $Revision: 1.1.2.2.2.3 $ $Date: 2005/03/27 15:29:57 $
 */
public class RecipeShopManageList  extends L2GameServerPacket
{

	private static final String _S__D8_RecipeShopManageList = "[S] d8 RecipeShopManageList";

    private L2PcInstance _seller;
    private boolean _isDwarven;
    private L2RecipeList _recipes[];

    public RecipeShopManageList(L2PcInstance seller, boolean isDwarven)
    {
        _seller = seller;
        _isDwarven = isDwarven;
        if(_isDwarven && _seller.hasDwarvenCraft())
            _recipes = _seller.getDwarvenRecipeBook();
    }

    protected final void runImpl()
	{
		// no long-running tasks
	}

    final void writeImpl()
    {
        writeC(216);
        writeD(_seller.getObjectId());
        writeD(_seller.getAdena());
        if(getClient().getRevision() > 600)
            writeD(_isDwarven ? 0 : 1);
        if(_recipes == null)
        {
            writeD(0);
        } else
        {
            writeD(_recipes.length);
            for(int i = 0; i < _recipes.length; i++)
            {
                L2RecipeList temp = _recipes[i];
                writeD(temp.getId());
                writeD(i + 1);
            }

        }
        if(_seller.getCreateList() == null)
        {
            writeD(0);
        } else
        {
            L2ManufactureList list = _seller.getCreateList();
            writeD(list.size());
            L2ManufactureItem item;
            for(Iterator i$ = list.getList().iterator(); i$.hasNext(); writeD(item.getCost()))
            {
                item = (L2ManufactureItem)i$.next();
                writeD(item.getRecipeId());
                writeD(0);
            }

        }
    }
	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.serverpackets.ServerBasePacket#getType()
	 */
	public String getType()
	{
		return _S__D8_RecipeShopManageList;
	}
}
