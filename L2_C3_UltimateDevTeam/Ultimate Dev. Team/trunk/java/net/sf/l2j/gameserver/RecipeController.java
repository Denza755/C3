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

import javolution.util.FastList;
import javolution.util.FastMap;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.model.*;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.packets.serverpackets.*;
import net.sf.l2j.gameserver.skills.Stats;
import net.sf.l2j.gameserver.tables.ItemTable;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.LineNumberReader;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RecipeController
{
	protected static Logger _log = Logger.getLogger(RecipeController.class.getName());
	private static RecipeController _instance;
	
	private Map<Integer, L2RecipeList> _lists;
	
	protected static Map<L2PcInstance, ReceipeItemMaker> activeMakers = Collections.synchronizedMap(new WeakHashMap<L2PcInstance,ReceipeItemMaker>());
	
    protected static final Random _rnd = new Random();
	
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
		_lists = new FastMap<Integer, L2RecipeList>();
		String line = null;
		LineNumberReader lnr = null;
		
		try
		{
			File recipesData = new File(Config.DATAPACK_ROOT, "data/csv/recipes.csv");
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
        finally
        {
            try { lnr.close(); } catch (Exception e) { }
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
			L2RecipeList find = _lists.get(i);
			if (find.getRecipeId() == itemId)
			{
				return find;
			}
		}
		return null;
	}
	
	public synchronized void requestBookOpen(L2PcInstance player, boolean isDwarvenCraft)
	{						
		if (!Config.ALT_GAME_CREATION){
			RecipeBookItemList response = new RecipeBookItemList(isDwarvenCraft,player.getMaxMp()); 
		 	if (isDwarvenCraft) 
		 		response.addRecipes(player.getDwarvenRecipeBook()); 
		 	else 
		 		response.addRecipes(player.getCommonRecipeBook()); 
			player.sendPacket(response);
			return;
		}
        ReceipeItemMaker maker = activeMakers.get(player);
        if (maker == null) {
        	RecipeBookItemList response = new RecipeBookItemList(isDwarvenCraft,player.getMaxMp()); 
		 	if (isDwarvenCraft) 
		 		response.addRecipes(player.getDwarvenRecipeBook()); 
		 	else 
		 		response.addRecipes(player.getCommonRecipeBook());
        	player.sendPacket(response);
        	return;
        }
        if (maker.timeReady <= GameTimeController.getGameTicks()) {
        	maker.run();
        	return;
        }
        player.sendPacket(new SystemMessage(SystemMessage.CANT_ALTER_RECIPEBOOK_WHILE_CRAFTING));
    }
	
	public synchronized void requestManufactureItem(L2PcInstance manufacturer, int recipeListId, L2PcInstance player)
	{
		L2RecipeList recipeList = getRecipeList(recipeListId-1);
		
		manufacturer.isInCraftMode(true);
		if ((recipeList == null) || (recipeList.getRecipes().length == 0) || (!manufacturer.hasRecipeList(recipeListId)))
		{
			player.sendMessage("No recipe for: " + recipeListId);
        		manufacturer.isInCraftMode(false);
			return;
		}
		if (!Config.ALT_GAME_CREATION) {
			new ReceipeItemMaker(manufacturer, recipeList, player);
		} else {
			ReceipeItemMaker maker = activeMakers.get(manufacturer);
			if (maker != null)
			{
				player.sendMessage("Manufacturer are busy creating "+recipeList.getItemId());
            			manufacturer.isInCraftMode(false);
				return;
			}
			maker = new ReceipeItemMaker(manufacturer, recipeList, player);
			if (maker.isValid)
				activeMakers.put(manufacturer, maker);
		}
	}
	public synchronized void requestMakeItem(L2PcInstance player, int recipeListId)
	{
		L2RecipeList recipeList = getRecipeList(recipeListId-1);
		
		SystemMessage sm = new SystemMessage(614);
        
    		player.isInCraftMode(true);
		if ((recipeList == null) || (recipeList.getRecipes().length == 0))
		{
			player.sendMessage("No recipe for: " + recipeListId);
            player.isInCraftMode(false);
			return;
		}
		if (!Config.ALT_GAME_CREATION) {
			new ReceipeItemMaker(player, recipeList, null);
		} else {
			ReceipeItemMaker maker = activeMakers.get(player);
			if (maker != null)
			{
				player.sendMessage("You are busy creating "+recipeList.getItemId());
				player.sendPacket(sm);
                player.isInCraftMode(false);
				return;
			}
			maker = new ReceipeItemMaker(player, recipeList, null);
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
		List<L2RecipeInstance> recipePartList = new FastList<L2RecipeInstance>();
		
        //we use common/dwarf for easy reading of the recipes.csv file 
	 	String recipeTypeString = st.nextToken(); 
	 	
	 	//now we parse the string into a boolean 
	 	boolean isDwarvenRecipe = false; 
	 	if (recipeTypeString.equalsIgnoreCase("dwarven")) 
	 		isDwarvenRecipe = true; 

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
		
		L2RecipeList recipeList = new L2RecipeList(id,level,recipeId,recipeName,successRate,mpCost,itemId,count,isDwarvenRecipe);
		for (L2RecipeInstance recipePart : recipePartList)
		{
			recipeList.addRecipe(recipePart);
		}
		_lists.put(_lists.size(), recipeList);
	}
	
	
	
	
	class ReceipeItemMaker implements Runnable
	{
		int   timeReady;
		final L2PcInstance player;
		final L2PcInstance target;
		final L2RecipeList recipeList;
		final List<L2ItemInstance> items;
		int   materialsRefPrice;
		int   totalItems;
		int   usedItems;
		int   price;
		boolean isValid;
		
		ReceipeItemMaker(L2PcInstance player, L2RecipeList recipeList, L2PcInstance target )
		{
			this.player = player;
			this.target = target;
			this.recipeList = recipeList;
			this.items = new FastList<L2ItemInstance>();
			
			int skillLevel; 
			if (recipeList.isDwarvenRecipe()) 
			{
				skillLevel = player.getSkillLevel(L2Skill.SKILL_CREATE_DWARVEN); 
				if (skillLevel <= 0) 
				{
					player.sendPacket(new ActionFailed()); 
					player.isInCraftMode(false); 
					return;
				} 
			} 
			else 
			{ 
				skillLevel = player.getSkillLevel(L2Skill.SKILL_CREATE_COMMON); 
				if (skillLevel <= 0) 
				{ 
					player.sendPacket(new ActionFailed()); 
					player.isInCraftMode(false); 
					return; 
				} 
			}
			
			if ((recipeList == null) || (recipeList.getRecipes().length == 0))
			{
				player.sendMessage("No such recipe");
				player.isInCraftMode(false);
				return;
			}
			
			if (target != null)
			{
			    for (L2ManufactureItem temp : player.getCreateList().getList())
			    {
				if (temp.getRecipeId() == recipeList.getId())
				{
				    price = temp.getCost();
				    if (target.getAdena() < price)
				    {
					target.sendPacket(new SystemMessage(SystemMessage.YOU_NOT_ENOUGH_ADENA));
					player.isInCraftMode(false);
					return;
				    }
				    break;
				}
			    }
			}
			if (recipeList.getLevel()>skillLevel)
			{
				player.sendMessage("Need skill level "+recipeList.getLevel());
		        player.isInCraftMode(false);
				return;
			}
			/*
			 if (recipeList.getAdenaCost()>activeChar.getAdena())
			 {
			 SystemMessage sm = new SystemMessage(614); 
			 if (Config.ALT_LANGUAGE) {sm.addString("C\u1EA7n th\u00EAm "+recipeList.getAdenaCost()+" Adena");}
			 else {sm.addString("Need "+recipeList.getAdenaCost()+" Adena");}
			 activeChar.sendPacket(sm);
			 return;
			 }*/
			
			L2RecipeInstance[] recipes = recipeList.getRecipes();
			
			Inventory inv = null;
			if (target == null)
			    inv = player.getInventory();
			else
			    inv = target.getInventory();
			
			for (L2RecipeInstance recipe : recipes)
			{
				if (recipe.getQuantity() != 0)
				{
					L2ItemInstance invItem = inv.getItemByItemId(recipe.getItemId());
					if ((invItem == null) || ((recipeList.isConsumable() ? (int)(recipe.getQuantity() * Config.RATE_CONSUMABLE_COST) : recipe.getQuantity()) > invItem.getCount()))
					{
						if (target == null)
						    player.sendMessage("You dont have the right elements for making this item" + ((recipeList.isConsumable() && Config.RATE_CONSUMABLE_COST != 1)?".\nDue to server rates you need "+Config.RATE_CONSUMABLE_COST+"x more material than listed in recipe":""));
						else
						    target.sendMessage("You dont have the right elements for making this item" + ((recipeList.isConsumable() && Config.RATE_CONSUMABLE_COST != 1)?".\nDue to server rates you need "+Config.RATE_CONSUMABLE_COST+"x more material than listed in recipe":""));
			                        player.isInCraftMode(false);
						return;
					}
				}
			}
			
			if (!Config.ALT_GAME_CREATION)
			{
				if (player.getCurrentMp() < recipeList.getMpCost())
				{
					if (Config.DEBUG) _log.fine("not enough mana");
					if (target == null)
					    player.sendPacket(new SystemMessage(SystemMessage.NOT_ENOUGH_MP));
					else
					    target.sendPacket(new SystemMessage(SystemMessage.NOT_ENOUGH_MP));
					timeReady = GameTimeController.getGameTicks();
					player.isInCraftMode(false);
					return;
				}
			}
			
			for (L2RecipeInstance recipe : recipes)
			{
				if (recipe.getQuantity() != 0)
				{
					L2ItemInstance invItem = inv.getItemByItemId(recipe.getItemId());
					invItem = inv.dropItem(invItem, recipeList.isConsumable() ? (int)(recipe.getQuantity() * Config.RATE_CONSUMABLE_COST) : recipe.getQuantity());
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
            player.isInCraftMode(true);
            
			if ( Config.IS_CRAFTING_ENABLED ) {
				double currentPlayerMp = player.getCurrentMp(); 
				if (currentPlayerMp < recipeList.getMpCost()) {
					if (Config.DEBUG) _log.fine("not enough mana");
					if (target == null)
					    player.sendPacket(new SystemMessage(SystemMessage.NOT_ENOUGH_MP));
					else
					    target.sendPacket(new SystemMessage(SystemMessage.NOT_ENOUGH_MP));
					timeReady = GameTimeController.getGameTicks();
					player.isInCraftMode(false);
					return;
				}
				
				int gameTicks = GameTimeController.getGameTicks();	
				if (timeReady > gameTicks)
                {
                    player.isInCraftMode(false);
					return;
                }
				
				timeReady = gameTicks + (int)(15 * player.getMReuseRate(player.getKnownSkill(172)) * GameTimeController.TICKS_PER_SECOND);			
				
				if (!Config.ALT_GAME_CREATION || items.isEmpty())
                {
					player.reduceCurrentMp(recipeList.getMpCost());
					
					StatusUpdate su = new StatusUpdate(player.getObjectId());
					
					L2ItemInstance createdItem = ItemTable.getInstance().createItem(recipeList.getItemId());
					createdItem.setCount(recipeList.getCount());
					
					int randCheck = _rnd.nextInt(100);
					if(recipeList.getSuccessRate() <= randCheck ) {
					player.sendMessage("Item(s) failed to create");
			        player.isInCraftMode(false);
					}
					else
                    {
						int createdItemCount = createdItem.getCount();
						SystemMessage sm = new SystemMessage(SystemMessage.EARNED_ITEM);
						sm.addItemName(createdItem.getItemId());
						sm.addNumber(createdItemCount);
						//sm.addString("Item(s) created");
						if (target == null)
						{
						    player.getInventory().addItem(createdItem);
						    player.sendPacket(sm);
						    su.addAttribute(StatusUpdate.CUR_LOAD, player.getCurrentLoad());
						} else 
						{
						    if (price > 0) {
							target.reduceAdena(price);
							player.addAdena(price);
							L2ItemInstance adenaupdate = player.getInventory().findItemByItemId(57);
							player.sendPacket(new InventoryUpdate().addModifiedItem(adenaupdate));
							target.sendPacket(new SystemMessage(SystemMessage.DISSAPEARED_ADENA).addNumber(price));
							player.sendPacket(new SystemMessage(SystemMessage.EARNED_ADENA).addNumber(price));
						    }
						    target.getInventory().addItem(createdItem);
						    target.sendPacket(sm);
						    target.sendPacket(new StatusUpdate(target.getObjectId()).addAttribute(StatusUpdate.CUR_LOAD, target.getCurrentLoad()));
						}
						
						
						if (Config.ALT_GAME_CREATION) {
							int skillLevel;
							if (recipeList.isDwarvenRecipe()) 
								skillLevel = player.getSkillLevel(L2Skill.SKILL_CREATE_DWARVEN); 
							else 
								skillLevel = player.getSkillLevel(L2Skill.SKILL_CREATE_COMMON); 
							int receipeLevel = recipeList.getLevel();
							int exp = createdItem.getReferencePrice()*createdItemCount;
							// one variation
							exp -= materialsRefPrice;
							if (exp < 0) exp = 0;
							// another variation
							// exp /= receipeLevel
							for (int i=skillLevel; i > receipeLevel; i--)
								exp /= 4;
							int sp = exp / 10;
                            player.addExpAndSp((int)player.calcStat(Stats.EXPSP_RATE, exp * Config.RATE_XP, null, null), 
                                               (int)player.calcStat(Stats.EXPSP_RATE, sp * Config.RATE_SP, null, null));
						}
					}				
					activeMakers.remove(player);
                    player.isInCraftMode(false);
					
					su.addAttribute(StatusUpdate.CUR_MP, (int) player.getCurrentMp());
					player.sendPacket(su);
					if (target == null){
					    player.sendPacket(new ItemList(player, false));
					    player.sendPacket(new RecipeItemMakeInfo(recipeList.getId(),player));
					} else
					{
					    target.sendPacket(new ItemList(target, false));
					    target.sendPacket(new RecipeShopItemInfo(player.getObjectId(),recipeList.getId()));
					}
				} 
				else {
					double currentMp = player.getCurrentMp(); 
					if (currentMp < recipeList.getMpCost()) {
						if (Config.DEBUG) _log.fine("not enough mana");
						if (target == null)
						    player.sendPacket(new SystemMessage(SystemMessage.NOT_ENOUGH_MP));
						else
						    target.sendPacket(new SystemMessage(SystemMessage.NOT_ENOUGH_MP));
						timeReady = GameTimeController.getGameTicks();
			                        player.isInCraftMode(false);
						return;
					}
					player.setCurrentMp(currentMp - recipeList.getMpCost());
					
					int numItems = player.getSkillLevel(172);
					while (numItems > 0 && !items.isEmpty()) {
						L2ItemInstance item = items.get(0);
						
						int count = item.getCount();
						if (count >= numItems) count = numItems;
						
						item.setCount(item.getCount() - count);
						if (item.getCount() <= 0) items.remove(0);
						
						numItems -= count;
						usedItems += count;
						
						SystemMessage sm = new SystemMessage(368);
						sm.addNumber(count);
						sm.addItemName(item.getItemId());
						if (target == null)
						    player.sendPacket(sm);
						else
						    target.sendPacket(sm);
						
			                        player.isInCraftMode(false);
					}
					
					int delay = (timeReady-gameTicks) * GameTimeController.MILLIS_IN_TICK;
					player.broadcastPacket(new MagicSkillUser(player, 1050, 1, delay, 0));
					player.sendPacket(new SetupGauge(0, delay));
					
					ClientScheduler.getInstance().scheduleLow(this, 100+delay);
				}
			}
			else {
				// crafting is disabled...
				if (target == null)
				    player.sendMessage("Crafting is currently disabled.");
				else
				    target.sendMessage("Crafting is currently disabled.");
		                player.isInCraftMode(false);
			}
		}
	}
}
