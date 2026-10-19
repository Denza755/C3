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

import java.io.File;
import java.io.FileInputStream;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import net.sf.l2j.gameserver.ItemTable;
import net.sf.l2j.gameserver.SevenSigns;
import net.sf.l2j.gameserver.serverpackets.ItemList;
import net.sf.l2j.gameserver.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;

/**
 * Dawn/Dusk Seven Signs Priest Instance
 * 
 * @author Tempy
 */
public final class L2SignsPriestInstance extends L2FolkInstance
{
	private static Logger _log = Logger.getLogger(L2SignsPriestInstance.class.getName());
	
	/**
	 * @param template
	 */
	public L2SignsPriestInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
	}
	
	public void onBypassFeedback(L2PcInstance player, String command)
	{
		// first do the common stuff
		// and handle the commands that all NPC classes know
		super.onBypassFeedback(player, command);
		
		if (command.startsWith("SevenSignsDesc")) 	
		{
			int val = Integer.parseInt(command.substring(15));
			
			showChatWindow(player, val, null, true);
		}	
		
		else if (command.startsWith("SevenSigns")) 
		{
			int cabal = SevenSigns.CABAL_NULL;
			int stoneType = 0;
			int ancientAdenaAmount = 0;
			int inventorySize = player.getInventory().getSize() + 1;
			int val = Integer.parseInt(command.substring(11, 12));

			if (command.length() > 12)  // SevenSigns x[x] x [x..x] 
				val = Integer.parseInt(command.substring(11, 13).trim());
				
			if (command.length() > 13)
				try {
					cabal = Integer.parseInt(command.substring(14, 15).trim());
				}
				catch (Exception e) { 
					try {
						cabal = Integer.parseInt(command.substring(13, 14).trim());
					}
					catch (Exception e2) { }
				}
		
			L2ItemInstance ancientAdena = player.getInventory().getItemByItemId(SevenSigns.ANCIENT_ADENA_ID);
				
			if (ancientAdena != null) 
				ancientAdenaAmount = ancientAdena.getCount();
								
			switch (val) {
			case 2: // Purchase Record of Seven Signs
				if (player.getAdena() < SevenSigns.RECORD_SEVEN_SIGNS_COST) {
					player.sendPacket(new SystemMessage(SystemMessage.YOU_DO_NOT_HAVE_ENOUGH_ADENA));
					break;
				}
				
				if (!player.getInventory().validateCapacity(inventorySize)) {
					player.sendPacket(new SystemMessage(SystemMessage.INVENTORY_SLOT_IS_FULL));
					break;
				}
				
				L2ItemInstance recordSevenSigns = ItemTable.getInstance().createItem(SevenSigns.RECORD_SEVEN_SIGNS_ID);
				player.reduceAdena(SevenSigns.RECORD_SEVEN_SIGNS_COST);
				player.getInventory().addItem(recordSevenSigns);
				recordSevenSigns = ItemTable.getInstance().createItem(SevenSigns.RECORD_SEVEN_SIGNS_ID); 
				
				player.sendPacket(new ItemList(player, false));
				
				SystemMessage sm = new SystemMessage(SystemMessage.S2_S1_HAS_DISAPPEARED);
				sm.addItemName(57);
				sm.addNumber(SevenSigns.RECORD_SEVEN_SIGNS_COST);
				player.sendPacket(sm);
					
				sm = new SystemMessage(SystemMessage.EARNED_S1);
				sm.addItemName(SevenSigns.RECORD_SEVEN_SIGNS_ID);
				player.sendPacket(sm);
				break; 
			case 3: // Join Cabal Intro 1
			case 8: // Festival of Darkness Intro	(SevenSigns x [0]1)
			case 10: // Teleport Location List
				showChatWindow(player, val, SevenSigns.getCabalShortName(cabal), false);
				break;					
			case 4: // Join a Cabal - SevenSigns 4 0[1] x
				int newSeal = Integer.parseInt(command.substring(15));
				int newCabal = SevenSigns.getInstance().setPlayerInfo(player, cabal, newSeal);
				
				/* 
				 * TODO:
				 * - Castle-owning clan/ally members join Dawn only. Others will be allowed
				 * 	 to join Dawn if they pay 50k adena or have the certificate. 
				 * - Make joining class-transfer based rather than by level.
				 * - Use the SystemMessages 1271 and 1272 and allow confirmation before joining.
				 */ 
				if (player.getLevel() < 20) {
					player.sendPacket(SystemMessage.sendString("You must have already completed your first class transfer."));
					break;
				}
				else if (player.getLevel() < 40) {
					//player.sendPacket(SystemMessage.sendString("You must have already completed your second class transfer."));
					//break;
				}
				else {
					/*
					  if (playerClanOwnsCastle(player)) {
					  	if (newCabal != SevenSigns.CABAL_DAWN) {
					  		player.sendPacket(SystemMessage.sendString("You must be not be a member of a castle-owning clan to join the Revolutionaries of Dusk.");
					  		break;
					  	}
					  }
					  else { 
					  	if (newCabal != SevenSigns.CABAL_DUSK) {
					  		player.sendPacket(SystemMessage.sendString("You must be a member of a castle-owning clan to join the Lords of Dawn.");
					  		break;
					  	} 
					  }
					*/
				}
					
				if (newCabal == cabal) {
					if (newCabal == SevenSigns.CABAL_DAWN) 
						player.sendPacket(new SystemMessage(1273)); // Joined Dawn
					else
						player.sendPacket(new SystemMessage(1274)); // Joined Dusk
					
                    // Show a confirmation message to the user, indicating which seal they chose.
                    switch (newSeal) {
                    case SevenSigns.SEAL_AVARICE:
                        player.sendPacket(new SystemMessage(1275));
                        break;
                    case SevenSigns.SEAL_GNOSIS:
                        player.sendPacket(new SystemMessage(1276));
                        break;
                    case SevenSigns.SEAL_STRIFE:
                        player.sendPacket(new SystemMessage(1277)); // Actually is the same as 1275! 
                        break;
                    }
                        
					showChatWindow(player, 4, SevenSigns.getCabalShortName(cabal), false);
				}
				else { 
					player.sendPacket(SystemMessage.sendString("You are already a member of the " + SevenSigns.getCabalName(newCabal) + "."));
				}
				break;
			case 6: // Contribute Seal Stones - SevenSigns 6 x
				stoneType = Integer.parseInt(command.substring(13)); 
				int redStoneCount = 0;
				int greenStoneCount = 0;
				int blueStoneCount = 0;
				int contribScore = 0;
				boolean stonesFound = false;
			
				L2ItemInstance blueStones = player.getInventory().getItemByItemId(SevenSigns.SEAL_STONE_BLUE_ID);
				L2ItemInstance greenStones = player.getInventory().getItemByItemId(SevenSigns.SEAL_STONE_GREEN_ID);
				L2ItemInstance redStones = player.getInventory().getItemByItemId(SevenSigns.SEAL_STONE_RED_ID);
				
				if (blueStones != null) 
					blueStoneCount = blueStones.getCount();
				if (greenStones != null) 
					greenStoneCount = greenStones.getCount();
				if (redStones != null) 
					redStoneCount = redStones.getCount();
				
                contribScore = SevenSigns.getInstance().addPlayerStoneContrib(player, blueStoneCount, greenStoneCount, redStoneCount);
                if (contribScore == -1)
                {
                    // Exceeded maximum contribution
                    player.sendPacket(new SystemMessage(SystemMessage.CONTRIBUTION_LEVEL_HAS_EXCEEDED_THE_LIMIT_YOU_MAY_NOT_CONTINUE));
                    break;
                }
                else
                {
                    // Remove contributed stones from inventory
                    if (blueStones != null) 
                        player.removeItemFromInventory(blueStones, blueStoneCount);
                    if (greenStones != null) 
                        player.removeItemFromInventory(greenStones, greenStoneCount);
                    if (redStones != null) 
                        player.removeItemFromInventory(redStones, redStoneCount);

                    stonesFound = true;
                }
				
				if (!stonesFound) {
					player.sendPacket(SystemMessage.sendString("You do not have any seal stones of that type."));
					break;
				}
				
				player.sendPacket(new SystemMessage(SystemMessage.YOUR_CONTRIBUTION_SCORE_IS_INCREASED_BY_S1).addNumber(contribScore));

				showChatWindow(player, 6, null, false);
				break;
			case 7: // Exchange Ancient Adena for Adena - SevenSigns 7 xxxxxxx
				int ancientAdenaConvert = 0;	
				
				try {
					ancientAdenaConvert = Integer.parseInt(command.substring(13).trim());
				}
				catch (Exception NumberFormatException) {
					player.sendPacket(SystemMessage.sendString("You must enter an integer amount."));
					break;
				}
				
				if (ancientAdenaAmount < ancientAdenaConvert || ancientAdenaConvert < 1) {
					player.sendPacket(new SystemMessage(SystemMessage.YOU_DO_NOT_HAVE_ENOUGH_ADENA));
					break;
				}
					
				player.getInventory().destroyItemByItemId(SevenSigns.ANCIENT_ADENA_ID, ancientAdenaConvert);
				player.addAdena(ancientAdenaConvert);
					
				player.sendPacket(new ItemList(player, false));	
				
				player.sendPacket(new SystemMessage(SystemMessage.EARNED_S1_ADENA).addNumber(ancientAdenaConvert));
				break;
			case 9: // Recieve Contribution Rewards
				int playerCabal = SevenSigns.getInstance().getPlayerCabal(player);
				int winningCabal = SevenSigns.getInstance().getCabalHighestScore();
				
				if (SevenSigns.getInstance().isSealValidationPeriod() && playerCabal == winningCabal)
				{
					int ancientAdenaReward = SevenSigns.getInstance().getAncientAdenaReward(player, true);
					
					if (ancientAdenaReward < 3) { 
						showChatWindow(player, 9, "b", false);
						break;
					}
						
					ancientAdena = ItemTable.getInstance().createItem(SevenSigns.ANCIENT_ADENA_ID);
					ancientAdena.setCount(ancientAdena.getCount() + (ancientAdenaReward - 1));
					player.getInventory().addItem(ancientAdena);
					
					player.sendPacket(new ItemList(player, true));
					
					sm = new SystemMessage(SystemMessage.EARNED_S2_S1_s);
					sm.addNumber(ancientAdenaReward);
					sm.addItemName(SevenSigns.ANCIENT_ADENA_ID);
					player.sendPacket(sm);
					
                    showChatWindow(player, 9, "a", false);
				}
				break;
			case 11: // Teleport to Hunting Grounds
				try {
	                String portInfo = command.substring(14).trim();
		               
	                StringTokenizer st = new StringTokenizer(portInfo);
	                int x = Integer.parseInt(st.nextToken());
	                int y = Integer.parseInt(st.nextToken());
	                int z = Integer.parseInt(st.nextToken());
	                int ancientAdenaCost = Integer.parseInt(st.nextToken());
		            
	                if (ancientAdenaCost > 0)
	                {
	                	if (ancientAdenaCost > ancientAdenaAmount) {
	                		player.sendPacket(new SystemMessage(SystemMessage.YOU_DO_NOT_HAVE_ENOUGH_ADENA));
	                		break;
	                	}
		                	
	                	player.removeItemFromInventory(ancientAdena, ancientAdenaCost);
	                
	                	sm = new SystemMessage(SystemMessage.S2_S1_HAS_DISAPPEARED);
	                	sm.addItemName(SevenSigns.ANCIENT_ADENA_ID);
	                	sm.addNumber(ancientAdenaCost);
	                	player.sendPacket(sm);
					}
				
	                player.teleToLocation(x, y, z);			                
				}
				catch (Exception e) {
					_log.warning("SevenSigns: Error occurred while teleporting player: " + e);
				}	
				break;
			case 12: // Purchase Consumable Items
			case 13: // (Uses code adapted from Merchant of Mammon implementation.)
			case 14:
				StringBuffer listHtml = new StringBuffer("<html><body>" +
					"<font color=\"LEVEL\">[Purchase Consumable Items]</font><br>" +
					"Here is a selection of items that only those who are victorious can purchase!<br>");
				
				// Show 10 items per "page", as showing all of them will crash the client! :)
				int totalItems = SevenSigns.priestItems.length;
				int indexStart = (val - 12) * 10;
				int indexEnd = (((val - 11) * 10) > totalItems) ? totalItems : ((val - 11) * 10);
				int totalPages = (totalItems / 10) + 1;
				
				File listTemplate = new File(SevenSigns.SEVEN_SIGNS_HTML_PATH + "signs_12.htm");
				FileInputStream fis = null;

				try
				{
					fis = new FileInputStream(listTemplate);
					byte[] raw = new byte[fis.available()];
					fis.read(raw);
					
					String originalTemp = new String(raw, "UTF-8");
										
					for (int i = indexStart; i < indexEnd; i++) 
					{
						L2ItemInstance tempItem = ItemTable.getInstance().createDummyItem(Integer.parseInt(SevenSigns.priestItems[i][0]));
						
						String itemName = tempItem.getItem().getName();
						String itemDispName = itemName.replaceAll("_"," ");
						itemDispName = SevenSigns.capitalizeWords(itemDispName);
									
						String content = originalTemp;
						content = content.replaceAll("%itemIconName%", SevenSigns.priestItems[i][3]);
						content = content.replaceAll("%itemName%", itemDispName);
						content = content.replaceAll("%itemId%", SevenSigns.priestItems[i][0]);
						content = content.replaceAll("%ancientAdenaCost%", SevenSigns.priestItems[i][1]);
                        content = content.replaceAll("%adenaCost%", SevenSigns.priestItems[i][2]);
						content = content.replaceAll("%objectId%", String.valueOf(getObjectId()));
						listHtml.append(content);								
					}
				}
				catch (Exception e) {
					_log.warning("Problem with HTML text " + SevenSigns.SEVEN_SIGNS_HTML_PATH + "signs_12.htm: " + e);
				}
				finally	{
					try	{ fis.close(); } catch (Exception e1) {/* ignore this */}
				}
				
				listHtml.append("<br><table border=\"0\" width=\"230\"><tr><td width=\"19\"> " + 
					"<img src=\"icon.etc_ancient_adena_i00\" width=\"16\" height=\"16\" align=\"left\"></td>" +
					"<td width=\"211\">" + ancientAdenaAmount + "</td></tr></table>" + 
					"<center>");
					
				if ((val - 11) < totalPages) 
					listHtml.append("<button value=\"Next Page\" action=\"bypass -h npc_" + String.valueOf(getObjectId()) + "_SevenSigns " + (val + 1) + "\" width=\"60\" height=\"15\" back=\"sek.cbui94\" fore=\"sek.cbui92\"><br>");
					
				if (val > 12)
					listHtml.append("<button value=\"Prev Page\" action=\"bypass -h npc_" + String.valueOf(getObjectId()) + "_SevenSigns " + (val - 1) + "\" width=\"60\" height=\"15\" back=\"sek.cbui94\" fore=\"sek.cbui92\"><br>");
					
				listHtml.append("<br><button value=\"Return\" action=\"bypass -h npc_" + getObjectId() + "_Chat 0\" width=\"60\" height=\"15\" back=\"sek.cbui94\" fore=\"sek.cbui92\">" +
					"</center><br><br></body></html>");
				
				NpcHtmlMessage itemList = new NpcHtmlMessage(1);					
				itemList.setHtml(listHtml.toString());
				player.sendPacket(itemList);
				break;
			case 15: // SevenSigns 15 xxxx [x..x] [x..x]
                String[] itemData = command.substring(14).split(" ");
                int boughtItemId = Integer.parseInt(itemData[0]);
				int ancientAdenaCost = Integer.parseInt(itemData[1]);
                int adenaCost = Integer.parseInt(itemData[2]);
								
				if (ancientAdenaCost > ancientAdenaAmount) {
					player.sendPacket(new SystemMessage(SystemMessage.YOU_DO_NOT_HAVE_ENOUGH_ADENA));
					break;
				}
                
                if (adenaCost > player.getAdena()) {
                    player.sendPacket(new SystemMessage(SystemMessage.YOU_DO_NOT_HAVE_ENOUGH_ADENA));
                    break;
                }

                if (!player.getInventory().validateCapacity(inventorySize)) {
                    player.sendPacket(new SystemMessage(SystemMessage.INVENTORY_SLOT_IS_FULL));
					break;
				}
														
				L2ItemInstance boughtItem = ItemTable.getInstance().createItem(boughtItemId);
				player.getInventory().destroyItemByItemId(SevenSigns.ANCIENT_ADENA_ID, ancientAdenaCost);
                player.reduceAdena(adenaCost);
				player.getInventory().addItem(boughtItem);
				boughtItem = ItemTable.getInstance().createItem(boughtItemId); 
						
				player.sendPacket(new ItemList(player, true));
						
				sm = new SystemMessage(SystemMessage.S2_S1_HAS_DISAPPEARED);
				sm.addNumber(ancientAdenaCost);
				sm.addItemName(SevenSigns.ANCIENT_ADENA_ID);
				player.sendPacket(sm);
                
                sm = new SystemMessage(SystemMessage.S2_S1_HAS_DISAPPEARED);
                sm.addNumber(adenaCost);
                sm.addItemName(57);
                player.sendPacket(sm);
						
				sm = new SystemMessage(SystemMessage.EARNED_S1);
				sm.addItemName(boughtItemId);
				player.sendPacket(sm);
				break;
			case 17: // Exchange Seal Stones for Ancient Adena (Type Choice) - SevenSigns 15 x
				stoneType = Integer.parseInt(command.substring(14));
				int stoneId = 0;
				int stoneCount = 0;
				int stoneValue = 0;					
				String stoneColor = null;
				
					switch (stoneType) {
					case 1:
						stoneColor = "blue";
						stoneId = SevenSigns.SEAL_STONE_BLUE_ID;
						stoneValue = SevenSigns.SEAL_STONE_BLUE_VALUE;							
						break;
					case 2:
						stoneColor = "green";
						stoneId = SevenSigns.SEAL_STONE_GREEN_ID;
						stoneValue = SevenSigns.SEAL_STONE_GREEN_VALUE;
						break;
					case 3:
						stoneColor = "red";
						stoneId = SevenSigns.SEAL_STONE_RED_ID;
						stoneValue = SevenSigns.SEAL_STONE_RED_VALUE;
						break;
					}
				
				L2ItemInstance stoneInstance = player.getInventory().getItemByItemId(stoneId);
				
				if (stoneInstance != null)
					stoneCount = stoneInstance.getCount();
					
				File templateFile = new File(SevenSigns.SEVEN_SIGNS_HTML_PATH + "signs_17.htm");
				FileInputStream fis2 = null;

				try
				{
					fis2 = new FileInputStream(templateFile);
					byte[] raw = new byte[fis2.available()];
					fis2.read(raw);
					
					String content = new String(raw, "UTF-8");
										
					content = content.replaceAll("%stoneColor%", stoneColor);
					content = content.replaceAll("%stoneValue%", String.valueOf(stoneValue));
					content = content.replaceAll("%stoneCount%", String.valueOf(stoneCount));
					content = content.replaceAll("%stoneItemId%", String.valueOf(stoneId));						
					content = content.replaceAll("%objectId%", String.valueOf(getObjectId()));
					
					NpcHtmlMessage html = new NpcHtmlMessage(1);					
					html.setHtml(content);
					player.sendPacket(html);
				}
				catch (Exception e) {
					_log.warning("Problem with HTML text " + SevenSigns.SEVEN_SIGNS_HTML_PATH + "signs_17.htm: " + e);
				}
				finally	{
					try	{ fis2.close(); } catch (Exception e1) {/* ignore this */}
				}
				break;
			case 18: // Exchange Seal Stones for Ancient Adena - SevenSigns 18 xxxx xxxxxx
				int convertStoneId = Integer.parseInt(command.substring(14, 18));
				int convertCount = 0;
	
				try {
					convertCount = Integer.parseInt(command.substring(19).trim());
				}
				catch (Exception NumberFormatException) {
					player.sendPacket(SystemMessage.sendString("You must enter an integer amount."));
					break;
				}
									
				L2ItemInstance convertItem = player.getInventory().getItemByItemId(convertStoneId);
				
				if (convertItem == null) {
					player.sendPacket(SystemMessage.sendString("You do not have any seal stones of that type."));
					break;
				}	
					
				int totalCount = convertItem.getCount();
				int ancientAdenaReward = 0;
				
				if (convertCount <= totalCount && convertCount > 0) {
					switch (convertStoneId) {
					case SevenSigns.SEAL_STONE_BLUE_ID:
						ancientAdenaReward = SevenSigns.calcAncientAdenaReward(convertCount, 0, 0);							
						break;
					case SevenSigns.SEAL_STONE_GREEN_ID:
						ancientAdenaReward = SevenSigns.calcAncientAdenaReward(0, convertCount, 0);							
						break;
					case SevenSigns.SEAL_STONE_RED_ID:
						ancientAdenaReward = SevenSigns.calcAncientAdenaReward(0, 0, convertCount);							
						break;
					}
					
					player.removeItemFromInventory(convertItem, convertCount);
					
					ancientAdena = ItemTable.getInstance().createItem(SevenSigns.ANCIENT_ADENA_ID);
					ancientAdena.setCount(ancientAdena.getCount() + (ancientAdenaReward - 1));
					player.getInventory().addItem(ancientAdena);
					
					player.sendPacket(new ItemList(player, true));	
					
					sm = new SystemMessage(SystemMessage.EARNED_S2_S1_s);
					sm.addNumber(convertCount);
					sm.addItemName(convertStoneId);
					player.sendPacket(sm);
					
					sm = new SystemMessage(SystemMessage.EARNED_S2_S1_s);
					sm.addNumber(ancientAdenaReward);
					sm.addItemName(SevenSigns.ANCIENT_ADENA_ID);
					player.sendPacket(sm);
				}
				else {
					player.sendPacket(SystemMessage.sendString("You do not have that many seal stones."));
				}
				break;
            case 19: // Seal Information (for when joining a cabal)
                int chosenSeal = Integer.parseInt(command.substring(16));
                String fileSuffix = SevenSigns.getSealName(chosenSeal, true) + "_" + SevenSigns.getCabalShortName(cabal);
                
                showChatWindow(player, val, fileSuffix, false);
                break;
            case 20: // Seal Status (for when joining a cabal)
                StringBuffer content = new StringBuffer("<html><body><font color=\"LEVEL\">[Seal Status]</font><br>");
                
                for (int i = 1; i < 4; i++) {
                    int sealOwner = SevenSigns.getInstance().getSealOwner(i);

                    if (sealOwner != SevenSigns.CABAL_NULL)
                        content.append("[" + SevenSigns.getSealName(i, false) + ": " + SevenSigns.getCabalName(sealOwner) + "]<br>");
                    else
                        content.append("[" + SevenSigns.getSealName(i, false) + ": Nothingness]<br>");
                }
                
                content.append("<a action=\"bypass -h npc_" + getObjectId() + "_SevenSigns 3 " + cabal + "\">Go back.</a></body></html>");
                
                NpcHtmlMessage html = new NpcHtmlMessage(1);                    
                html.setHtml(content.toString());
                player.sendPacket(html);
                break;
			default:
				// 1 = Purchase Record Intro
				// 5 = Contrib Seal Stones Intro
				// 16 = Choose Type of Seal Stones to Convert
				
				showChatWindow(player, val, null, false);
				break;
			}
		}
	}
    
    private void showChatWindow(L2PcInstance player, int val, String suffix, boolean isDescription)
    {
        String filename = SevenSigns.SEVEN_SIGNS_HTML_PATH;
        
        filename += (isDescription) ? "desc_" + val : "signs_" + val;
        filename += (suffix != null) ? "_" + suffix + ".htm" : ".htm";
        
        showChatWindow(player, filename);
    }
}