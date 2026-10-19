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
package net.sf.l2j.gameserver;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.model.Inventory;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2PcInstance;
import net.sf.l2j.gameserver.model.L2RecipeInstance;
import net.sf.l2j.gameserver.model.L2RecipeList;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.serverpackets.ItemList;
import net.sf.l2j.gameserver.serverpackets.MagicSkillUser;
import net.sf.l2j.gameserver.serverpackets.RecipeBookItemList;
import net.sf.l2j.gameserver.serverpackets.SetupGauge;
import net.sf.l2j.gameserver.serverpackets.StatusUpdate;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;

public class RecipeController
{
    protected static Logger _log = Logger.getLogger(RecipeController.class.getName());
    private static RecipeController _instance;
    
    private HashMap<Integer, L2RecipeList> _lists;

    protected static HashMap<L2PcInstance,ReceipeItemMaker> activeMakers = new HashMap<L2PcInstance,ReceipeItemMaker>();
	
	
    public static RecipeController getInstance()
    {
        if (_instance == null)
        {
            _instance = new RecipeController();
        }
        return _instance;
    }
    
    public RecipeController()
    {
        _lists = new HashMap<Integer, L2RecipeList>();
        String line = null;
        LineNumberReader lnr = null;
        
        try
        {
            File recipesData = new File(Config.DATAPACK_ROOT, "data/recipes.csv");
            lnr = new LineNumberReader(new BufferedReader( new FileReader(recipesData)));
            
            while ((line=lnr.readLine()) != null)
            {
                if (line.trim().length() == 0 || line.startsWith("#"))
                {
                    continue;
                }
                
                parseList(line);
                
            }
            _log.config("created "+_lists.size()+" recipes");
        }
        catch (Exception e)
        {
            if (lnr != null)
            {
				_log.log(Level.WARNING, "error while creating recipe controller in linenr: "+lnr.getLineNumber(), e);
            }
            else
            {
                _log.warning("No recipes were found in data folder");
            }
        }
    }
    
    public int getRecipesCount()
    {
        return _lists.size();
    }
    
    public L2RecipeList getRecipeList(int listId)
    {
        return _lists.get(listId);
    }
    
    public L2RecipeList getRecipeByItemId(int itemId)
    {
        for (int i = 0; i < _lists.size(); i++)
        {
          L2RecipeList find = _lists.get(new Integer(i));
          if (find.getRecipeId() == itemId)
          {
              return find;
          }
        }
        return null;
    }
	
	public synchronized void requestBookOpen(L2PcInstance player)
	{
		if (!Config.ALT_GAME_CREATION)
		{
			RecipeBookItemList response = new RecipeBookItemList();
			response.addRecipes(player.getRecipeBook());
			player.sendPacket(response);
			return;
		} else {
			ReceipeItemMaker maker = activeMakers.get(player);
			if (maker == null) {
				RecipeBookItemList response = new RecipeBookItemList();
				response.addRecipes(player.getRecipeBook());
				player.sendPacket(response);
				return;
			}
			if (maker.timeReady <= GameTimeController.getGameTicks())
			{
				maker.run();
				return;
			} else {
				SystemMessage sm = new SystemMessage(614); 
	            sm.addString("You are busy creating ");
				sm.addItemName(maker.recipeList.getItemId());
				player.sendPacket(sm);
	            return;
			}
		}
	}
	
	public synchronized void requestMakeItem(L2PcInstance player, int receipeListId)
	{
        L2RecipeList recipeList = getRecipeList(receipeListId-1);
        
        SystemMessage sm = new SystemMessage(614); 
        if ((recipeList == null) || (recipeList.getRecipes().length == 0))
        {
        	sm = new SystemMessage(614); 
            sm.addString("No recipe for: " + receipeListId);
			player.sendPacket(sm);
            return;
        }
		if (!Config.ALT_GAME_CREATION) {
			new ReceipeItemMaker(player, recipeList);
		} else {
			ReceipeItemMaker maker = activeMakers.get(player);
			if (maker != null)
			{
	        	sm = new SystemMessage(614); 
	            sm.addString("You are busy creating ");
				sm.addItemName(recipeList.getItemId());
				player.sendPacket(sm);
	            return;
			}
			maker = new ReceipeItemMaker(player, recipeList);
			if (maker.isValid)
				activeMakers.put(player, maker);
		}
	}
	
	public synchronized void requestMakeItemAbort(L2PcInstance player)
	{
		activeMakers.remove(player);
	}
        
    private void parseList(String line)
    {
        StringTokenizer st = new StringTokenizer(line,";");
        Vector<L2RecipeInstance> recipePartList = new Vector<L2RecipeInstance>();
        
        String recipeName = st.nextToken();
        int id = Integer.parseInt(st.nextToken());
        int recipeId = Integer.parseInt(st.nextToken());
        int level = Integer.parseInt(st.nextToken());
        
        //material
        StringTokenizer st2 = new StringTokenizer(st.nextToken(), "[],");
        while (st2.hasMoreTokens())
        {
        	StringTokenizer st3 = new StringTokenizer(st2.nextToken(), "()");
        	int rpItemId = Integer.parseInt(st3.nextToken());
            int quantity = Integer.parseInt(st3.nextToken());
            L2RecipeInstance rp = new L2RecipeInstance(rpItemId, quantity);
            recipePartList.add(rp);
            //recipe.addRecipe(rp);
        }

        int itemId = Integer.parseInt(st.nextToken());
        int count = Integer.parseInt(st.nextToken());
        
        //npc fee
        /*String notdoneyet = */ st.nextToken();
        
        int mpCost = Integer.parseInt(st.nextToken());
        int successRate = Integer.parseInt(st.nextToken());
        
        L2RecipeList recipeList = new L2RecipeList(id,level,recipeId,recipeName,successRate,mpCost,itemId,count);
        for (L2RecipeInstance recipePart : recipePartList)
        {
            recipeList.addRecipe(recipePart);
        }
        _lists.put(new Integer(_lists.size()), recipeList);
    }
    
	
	
	
	class ReceipeItemMaker implements Runnable
	{
		int   timeReady;
		final L2PcInstance player;
		final L2RecipeList recipeList;
		final ArrayList<L2ItemInstance> items;
		int   materialsRefPrice;
		int   totalItems;
		int   usedItems;
		boolean isValid;

		ReceipeItemMaker(L2PcInstance player, L2RecipeList recipeList)
		{
			this.player = player;
			this.recipeList = recipeList;
			this.items = new ArrayList<L2ItemInstance>();
			
			int skillLevel = player.getSkillLevel(L2Skill.SKILL_CRAFTING);
	        if (skillLevel <= 0)
	        {
				player.sendPacket(new ActionFailed());
	            return;
	        }
	        
	        if ((recipeList == null) || (recipeList.getRecipes().length == 0))
	        {
				SystemMessage sm = new SystemMessage(614); 
	            sm.addString("No such recipe");
				player.sendPacket(sm);
	            return;
	        }
	        
	        if (recipeList.getLevel()>skillLevel)
	        {
				SystemMessage sm = new SystemMessage(614); 
	            sm.addString("Need skill level "+recipeList.getLevel());
				player.sendPacket(sm);
	            return;
	        }
	        /*
	        if (recipeList.getAdenaCost()>activeChar.getAdena())
	        {
	        	SystemMessage sm = new SystemMessage(614); 
	            sm.addString("Need "+recipeList.getAdenaCost()+" Adena");
	            activeChar.sendPacket(sm);
	            return;
	        }*/
	        
			L2RecipeInstance[] recipes = recipeList.getRecipes();

	        Inventory inv = player.getInventory();
	        
	        for (L2RecipeInstance recipe : recipes)
	        {
	            if (recipe.getQuantity() != 0)
	            {
	                L2ItemInstance invItem = inv.getItemByItemId(recipe.getItemId());
	                if ((invItem == null) || (recipe.getQuantity()>invItem.getCount()))
	                {
						SystemMessage sm = new SystemMessage(614); 
	                    sm.addString("You dont have the right elements for making this item");
						player.sendPacket(sm);
	                    return;
	                }
	            }
	        }
			
			if (!Config.ALT_GAME_CREATION)
			{
		        if (player.getCurrentMp() < recipeList.getMpCost())
		        {
		        	if (Config.DEBUG) _log.fine("not enough mana");
					SystemMessage sm = new SystemMessage(614); 
		            sm.addString("Not enough mana");
		            player.sendPacket(sm);
					return;
		        }
			}
			
	        for (L2RecipeInstance recipe : recipes)
	        {
	            if (recipe.getQuantity() != 0)
	            {
	                L2ItemInstance invItem = inv.getItemByItemId(recipe.getItemId());
					invItem = inv.dropItem(invItem, recipe.getQuantity());
					items.add(invItem);
					materialsRefPrice += invItem.getReferencePrice()*invItem.getCount();
					totalItems += invItem.getCount();
	            }
	        }
			
			isValid = true; 
			
			timeReady = GameTimeController.TICKS_PER_SECOND + GameTimeController.getGameTicks();
			ClientScheduler.getInstance().scheduleLow(this, 1100);
		}
		
		public void run()
		{
			if (timeReady > GameTimeController.getGameTicks())
				return;
	        
			int gameTicks = GameTimeController.getGameTicks(); 
			timeReady = gameTicks + (int)(15 * player.getMReuseRate(player.getKnownSkill(172))
					                         * GameTimeController.TICKS_PER_SECOND);

			if (!Config.ALT_GAME_CREATION || items.isEmpty())
			{
				player.reduceCurrentMp(recipeList.getMpCost());
		        
		        L2ItemInstance createdItem = ItemTable.getInstance().createItem(recipeList.getItemId());
		        createdItem.setCount(recipeList.getCount());
				player.getInventory().addItem(createdItem);
		        
		        player.sendPacket(new ItemList(player, true));
		        
				SystemMessage sm = new SystemMessage(614);
		        sm.addString("Item(s) created");
				player.sendPacket(sm);
				
		        StatusUpdate su = new StatusUpdate(player.getObjectId());
		        su.addAttribute(StatusUpdate.CUR_MP, (int) player.getCurrentMp());
				su.addAttribute(StatusUpdate.CUR_LOAD, player.getCurrentLoad());
				player.sendPacket(su);

				if (Config.ALT_GAME_CREATION)
				{
					int skillLevel = player.getSkillLevel(L2Skill.SKILL_CRAFTING);
					int receipeLevel = recipeList.getLevel();
					int exp = createdItem.getReferencePrice()*createdItem.getCount();
					exp *= Config.RATE_XP;
					// one variation
					exp -= materialsRefPrice;
					if (exp < 0) exp = 0;
					// another variation
					// exp /= receipeLevel
					for (int i=skillLevel; i > receipeLevel; i--)
						exp /= 4;
					int sp = exp / 10;
					sp *= Config.RATE_SP;
					player.addExpAndSp(exp, sp);
				}

				activeMakers.remove(player);

				//new RecipeItemMakeInfo(_id);
			} else {
				double currentMp = player.getCurrentMp(); 
		        if (currentMp < recipeList.getMpCost())
		        {
		        	if (Config.DEBUG) _log.fine("not enough mana");
					SystemMessage sm = new SystemMessage(SystemMessage.NOT_ENOUGH_MP); 
		            player.sendPacket(sm);
					timeReady = GameTimeController.getGameTicks();
					return;
		        }
				player.setCurrentMp(currentMp - recipeList.getMpCost());

				int numItems = player.getSkillLevel(172);
				while (numItems > 0 && !items.isEmpty())
				{
					L2ItemInstance item = items.get(0);
					int count = item.getCount();
					if (count >= numItems)
						count = numItems;
					item.setCount(item.getCount() - count);
					if (item.getCount() <= 0)
						items.remove(0);
					numItems -= count;
					usedItems += count;
					
					SystemMessage sm = new SystemMessage(368);
					sm.addNumber(count);
					sm.addItemName(item.getItemId());
					//sm.addItemName(recipeList.getItemId());
		            player.sendPacket(sm);
				}

				int delay = (timeReady-gameTicks) * GameTimeController.MILLIS_IN_TICK;
				MagicSkillUser msk = new MagicSkillUser(player, 1050, 1, delay, 0);
				player.broadcastPacket(msk);
				SetupGauge sg = new SetupGauge(0, delay);
				player.sendPacket(sg);

				ClientScheduler.getInstance().scheduleLow(this, 100+delay);
			}
		}
	}
}
