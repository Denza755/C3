/*
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2, or (at your option) any later version. This
 * program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place - Suite 330, Boston, MA 02111-1307, USA.
 * http://www.gnu.org/copyleft/gpl.html
 */
package net.sf.l2j.gameserver.handler.itemhandlers;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.RecipeController;
import net.sf.l2j.gameserver.handler.IItemHandler;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2PcInstance;
import net.sf.l2j.gameserver.model.L2PlayableInstance;
import net.sf.l2j.gameserver.model.L2RecipeList;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;

/**
 * This class ...
 *
 * @version $Revision: 1.1.2.5.2.5 $ $Date: 2005/04/06 16:13:51 $
 */

public class Recipes implements IItemHandler
{
    private static int[] _itemIds = null;

    public Recipes()
    {
        RecipeController rc = RecipeController.getInstance();
        _itemIds = new int[rc.getRecipesCount()];
        for(int i = 0; i < rc.getRecipesCount(); i++)
        {
            _itemIds[i] = rc.getRecipeList(i).getRecipeId();
        }
    }

    public void useItem(L2PlayableInstance playable, L2ItemInstance item)
    {
        if(!(playable instanceof L2PcInstance)) return;
        L2PcInstance activeChar = (L2PcInstance)playable;
        if(activeChar.getCanCraft())
        {
            if(activeChar.getRecipeBook().length >= Config.ALT_MAX_RECIPES)
            {
            	activeChar.sendPacket(new SystemMessage(SystemMessage.NO_FURTHER_RECIPES_MAY_BE_REGISTERED));
                return;
            }

            L2RecipeList rp = RecipeController.getInstance().getRecipeByItemId(item.getItemId());
			if(rp.getLevel() > activeChar.getSkillLevel(L2Skill.SKILL_CRAFTING))
			{
                activeChar.sendPacket(new SystemMessage(SystemMessage.CREATE_ITEM_LEVEL_IS_TOO_LOW_TO_REGISTER_THIS_RECIPE));
                return;
			}
            if(activeChar.findRecipe(rp))
            {
                activeChar.sendPacket(new SystemMessage(SystemMessage.THAT_RECIPE_IS_ALREADY_REGISTERED));
                return;
            }
            // add recipe to recipebook
            activeChar.registerRecipeList(rp);
            activeChar.sendPacket(new SystemMessage(SystemMessage.S1_HAS_BEEN_ADDED).addString(item.getItem().getName()));
            activeChar.removeItemFromInventory(item, 1);
        }
        else
        {
        	activeChar.sendPacket(new SystemMessage(SystemMessage.YOU_ARE_NOT_AUTHORIZED_TO_REGISTER_A_RECIPE));
        }
    }

    public int[] getItemIds()
    {
        return _itemIds;
    }
}
