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
import net.sf.l2j.gameserver.lib.Rnd;
import net.sf.l2j.gameserver.lib.mysql;
import net.sf.l2j.gameserver.model.Inventory;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2PcInstance;
import net.sf.l2j.gameserver.model.L2RecipeInstance;
import net.sf.l2j.gameserver.model.L2RecipeList;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.serverpackets.ItemList;
import net.sf.l2j.gameserver.serverpackets.RecipeBookItemList;
import net.sf.l2j.gameserver.serverpackets.RecipeItemMakeInfo;
import net.sf.l2j.gameserver.serverpackets.StatusUpdate;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.templates.L2EtcItemType;

public class RecipeController
{
	protected static Logger _log = Logger.getLogger(RecipeController.class.getName());
	private static RecipeController _instance;
	
	private HashMap<Integer, L2RecipeList> _lists;
	
	//protected static HashMap<L2PcInstance,ReceipeItemMaker> activeMakers = new HashMap<L2PcInstance,ReceipeItemMaker>();
	
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
			File recipesData = new File("data/recipes.csv");
			lnr = new LineNumberReader(new BufferedReader( new FileReader(recipesData)));
			
			while ((line=lnr.readLine()) != null)
			{
				if (line.trim().length() == 0 || line.startsWith("#"))
				{
					continue;
				}
				
				parseList(line);
				
			}
			_log.config("RecipeController: Loaded "+_lists.size()+" Recipes.");
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
    	RecipeBookItemList response = new RecipeBookItemList();
    	response.addRecipes(player.getRecipeBook());
    	player.sendPacket(response);
	}
	
	public synchronized void requestMakeItem(L2PcInstance player, int receipeListId)
	{
		L2RecipeList recipeList = getRecipeList(receipeListId-1);
		
		player.isInCraftMode(true);
		// reset regen bonus counter for active crafter
                player.resetWaitSitTime();

		if ((recipeList == null) || (recipeList.getRecipes().length == 0))
		{
			player.sendMessage("No recipe for: " + receipeListId);
            player.isInCraftMode(false);
			return;
		}
		new ReceipeItemMaker(player, recipeList);
	}
	
	public synchronized void requestMakeItemAbort(L2PcInstance player)
	{
		//activeMakers.remove(player);
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
                player.isInCraftMode(false);
                player.sendPacket(new RecipeItemMakeInfo(recipeList.getId(),player, 0));                
				return;
			}
			
			if ((recipeList == null) || (recipeList.getRecipes().length == 0))
			{
				player.sendMessage("No such recipe");
                player.isInCraftMode(false);
                player.sendPacket(new RecipeItemMakeInfo(recipeList.getId(),player, 0));
                return;
			}
			
			if (recipeList.getLevel()>skillLevel)
			{
				player.sendMessage("Need skill level "+recipeList.getLevel());
                player.sendPacket(new RecipeItemMakeInfo(recipeList.getId(),player, 0));
                player.isInCraftMode(false);
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
                    if(ItemTable.getInstance().getTemplate(recipe.getItemId()).getItemType() != L2EtcItemType.RECEIPE )
    					if ((invItem == null) || (recipe.getQuantity()>invItem.getCount()))
    					{
    						player.sendMessage("You dont have the right elements for making this item");
                            player.isInCraftMode(false);
                            player.sendPacket(new RecipeItemMakeInfo(recipeList.getId(),player, 0));
                            return;
    					}
				}
			}
			
			if (player.getCurrentMp() < recipeList.getMpCost())
			{
				if (Config.DEBUG) _log.fine("not enough mana");
				player.sendPacket(new SystemMessage(SystemMessage.NOT_ENOUGH_MP));
				timeReady = GameTimeController.getGameTicks();
                player.isInCraftMode(false);
                player.sendPacket(new RecipeItemMakeInfo(recipeList.getId(),player, 0));
                return;
			}
			
			for (L2RecipeInstance recipe : recipes)
			{
				if (recipe.getQuantity() != 0)
				{
					L2ItemInstance invItem = inv.getItemByItemId(recipe.getItemId());
                    if(ItemTable.getInstance().getTemplate(recipe.getItemId()).getItemType() == L2EtcItemType.RECEIPE )
                    {
                        player.unregisterRecipeList(RecipeController.getInstance().getRecipeByItemId(recipe.getItemId()).getId());
                    }
                    else
                    {
    					invItem = inv.dropItem(invItem, recipe.getQuantity());
                    }
                    player.sendPacket(new SystemMessage(SystemMessage.S1_HAS_DISAPPEARED).addItemName(recipe.getItemId()));
				}
			}
			
			isValid = true; 
			
			timeReady = GameTimeController.TICKS_PER_SECOND + GameTimeController.getGameTicks();
			ClientScheduler.getInstance().scheduleLow(this, Math.max(1100, Config.CRAFT_TIME));
		}
		
		public void run()
		{
            player.isInCraftMode(true);
            
				double currentPlayerMp = player.getCurrentMp(); 
				if (currentPlayerMp < recipeList.getMpCost()) {
					if (Config.DEBUG) _log.fine("not enough mana");
					SystemMessage sm = new SystemMessage(SystemMessage.NOT_ENOUGH_MP); 
					player.sendPacket(sm);
					timeReady = GameTimeController.getGameTicks();
                    player.isInCraftMode(false);
                    player.sendPacket(new RecipeItemMakeInfo(recipeList.getId(),player, 0));
                    return;
				}
				
				int gameTicks = GameTimeController.getGameTicks();	
				if (timeReady > gameTicks)
                {
                    player.isInCraftMode(false);
					return;
                }
				
				//FIXME why getMReuseRate?
				timeReady = gameTicks + (int)(15 * player.getMReuseRate(player.getKnownSkill(172)) * GameTimeController.TICKS_PER_SECOND);			
				
				player.reduceCurrentMp(recipeList.getMpCost());
				
				L2ItemInstance createdItem = ItemTable.getInstance().createItem(recipeList.getItemId());
				createdItem.setCount(recipeList.getCount());
				
                SystemMessage sm;
                int success=0;
				if(recipeList.getSuccessRate() <= Rnd.get(100)) 
				{
                    sm = new SystemMessage(SystemMessage.S1_HAS_DISAPPEARED).addItemName(createdItem.getItemId());
                    L2World.removeObject(createdItem);
				}
				else
                {		
					
					if (Config.CRAFT_COUNTER){
						//SystemMessage sm = new SystemMessage(614);
						//sm.addString("Killer "+killer.getObjectId()+"; killed: "+this.getNpcId());
						//killer.sendPacket(sm);

						mysql.set("UPDATE `server_variables` SET `value` = `value` + '"+recipeList.getCount()+"' WHERE `type` = 'craftcount' AND `index` = '"+createdItem.getItemId()+"'" );
						int count = mysql.simple_get_int("count", "craftcount", "`char_id` = '"+player.getObjectId()+"' AND `item_id` = '"+createdItem.getItemId()+"'" );
						if ( count != 0 )
							{
								mysql.set("UPDATE `craftcount` SET `count` = `count` + '"+recipeList.getCount()+"' WHERE `char_id` = '"+player.getObjectId()+"' AND `item_id` = '"+createdItem.getItemId()+"'" );
								SystemMessage sm1 = new SystemMessage(614);
								sm1.addString("You have crafted "+(count+recipeList.getCount())+" items of this type");
								player.sendPacket(sm1);
							}
						else
							{
								mysql.set("INSERT INTO `craftcount` SET `count` = '"+recipeList.getCount()+"', `char_id` = '"+player.getObjectId()+"', `item_id` = '"+createdItem.getItemId()+"'" );
							}
						}
					
					player.getInventory().addItem(createdItem);
                    sm = new SystemMessage(SystemMessage.EARNED_S2_S1_s);
                    sm.addItemName(createdItem.getItemId());
                    sm.addNumber(recipeList.getCount());
                    success=1;
				}				
//				activeMakers.remove(player);
                player.isInCraftMode(false);
                player.sendPacket(sm);
				StatusUpdate su = new StatusUpdate(player.getObjectId());									
                su.addAttribute(StatusUpdate.CUR_LOAD, player.getCurrentLoad());
				su.addAttribute(StatusUpdate.CUR_MP, (int) player.getCurrentMp());
				player.sendPacket(su);
				player.sendPacket(new ItemList(player, true));
                player.sendPacket(new RecipeItemMakeInfo(recipeList.getId(),player, success));
		}
	}
}
