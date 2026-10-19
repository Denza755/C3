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
package net.sf.l2j.gameserver.model.actor.instance;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.cache.HtmCache;
import net.sf.l2j.gameserver.model.L2Clan;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.entity.SevenSigns;
import net.sf.l2j.gameserver.packets.serverpackets.InventoryUpdate;
import net.sf.l2j.gameserver.packets.serverpackets.ItemList;
import net.sf.l2j.gameserver.packets.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.packets.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.tables.ClanTable;
import net.sf.l2j.gameserver.tables.ItemTable;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;

import java.util.StringTokenizer;
import java.util.logging.Logger;

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
					player.sendPacket(new SystemMessage(SystemMessage.YOU_NOT_ENOUGH_ADENA));
					break;
				}
				
				if (!player.getInventory().validateCapacity(inventorySize)) {
					player.sendPacket(new SystemMessage(SystemMessage.SLOTS_FULL));
					break;
				}
				
				L2ItemInstance recordSevenSigns = ItemTable.getInstance().createItem(SevenSigns.RECORD_SEVEN_SIGNS_ID);
				player.reduceAdena(SevenSigns.RECORD_SEVEN_SIGNS_COST);
				player.getInventory().addItem(recordSevenSigns);
				
                InventoryUpdate iu = new InventoryUpdate();
                iu.addNewItem(recordSevenSigns);
                iu.addModifiedItem(player.getInventory().getAdenaInstance());
                player.sendPacket(iu);
				
				SystemMessage sm = new SystemMessage(SystemMessage.DISSAPEARED_ITEM);
				sm.addItemName(57);
				sm.addNumber(SevenSigns.RECORD_SEVEN_SIGNS_COST);
				player.sendPacket(sm);
					
				player.sendPacket(new SystemMessage(SystemMessage.EARNED_ITEM).addItemName(SevenSigns.RECORD_SEVEN_SIGNS_ID));
				break; 
			case 3: // Join Cabal Intro 1
			case 8: // Festival of Darkness Intro	(SevenSigns x [0]1)
			case 10: // Teleport Location List
				showChatWindow(player, val, SevenSigns.getCabalShortName(cabal), false);
				break;					
			case 4: // Join a Cabal - SevenSigns 4 0[1] x
				int newSeal = Integer.parseInt(command.substring(15));
				int oldCabal = SevenSigns.getInstance().getPlayerCabal(player);
				
				if (oldCabal != SevenSigns.CABAL_NULL) 
				{
					player.sendMessage("You are already a member of the " + SevenSigns.getCabalName(cabal) + ".");
				    return;
				}
                
				/* 
				 * TODO:
				 * - Make joining class-transfer based rather than by level.
				 * - Use the SystemMessages 1271 and 1272 and allow confirmation before joining.
				 */ 
				if (player.getLevel() < 20) 
				{
					player.sendMessage("You must have already completed your first class transfer.");
                    return;
				}
				else if (player.getLevel() >= 40) {
				    /*
				     * The character has completed their second class transfer. 
				     * They may no longer freely join Dawn or Dusk.
				     * 
				     * If set to use the official rules...
				     * If the character's clan is a member of a castle-owning clan/alliance they cannot join Dusk.
				     * 
				     * Otherwise, if the character is not in a castle-owning clan/alliance they must pay 50k adena or
				     * possess a special certificate to be able to join Dawn.
				     */
				    if (Config.ALT_GAME_REQUIRE_CASTLE_DAWN) 
				    {
				        if (getPlayerAllyHasCastle(player)) {
				            if (cabal == SevenSigns.CABAL_DUSK) 
				            {
				            	player.sendMessage("You must be not be a member of a castle-owning clan to join the Revolutionaries of Dusk.");
				                return;
				            }
				        }
                        else {
                            /*
                             * If the player is trying to join the Lords of Dawn, check if they are
                             * carrying a Lord's certificate.
                             * 
                             * If not then try to take the required amount of adena instead.
                             */ 
                            if (cabal == SevenSigns.CABAL_DAWN) 
                            {
                                L2ItemInstance certAppDawn = player.getInventory().getItemByItemId(SevenSigns.CERTIFICATE_OF_APPROVAL_ID);
                                boolean allowJoinDawn = false;
                                
                                if (certAppDawn != null) {
                                    player.removeItemFromInventory(certAppDawn, 1);
                                    
                                    player.sendPacket(new InventoryUpdate().addRemovedItem(certAppDawn));
                                    
                                    allowJoinDawn = true;
                                }
                                else {
                                    L2ItemInstance playerAdena = player.getInventory().getAdenaInstance();
                                    
                                    if (player.getAdena() >= SevenSigns.ADENA_JOIN_DAWN_COST) {
                                        player.reduceAdena(SevenSigns.ADENA_JOIN_DAWN_COST);
                                        
                                        player.sendPacket(new InventoryUpdate().addModifiedItem(playerAdena));
                                        
                                        allowJoinDawn = true;
                                    }
                                    else {
                                        player.sendPacket(new SystemMessage(SystemMessage.YOU_NOT_ENOUGH_ADENA));
                                        return;
                                    }
                                } 
                                
                                if (!allowJoinDawn) 
                                {
                                	player.sendMessage("You must be a member of a castle-owning clan to join the Lords of Dawn, or have a Certificate of Lord's Approval.");
                                    return;
                                }
                            }
                        }
				    }
				}
					
                SevenSigns.getInstance().setPlayerInfo(player, cabal, newSeal);
                
				if (cabal == SevenSigns.CABAL_DAWN) 
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
				        player.sendPacket(new SystemMessage(1277));  
				        break;
				}
				
				showChatWindow(player, 4, SevenSigns.getCabalShortName(cabal), false);
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
                    player.sendPacket(new SystemMessage(SystemMessage.CONTRIB_SCORE_EXCEEDED));
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
				
				if (!stonesFound) 
				{
					player.sendMessage("You do not have any seal stones of that type.");
					break;
				}
				
				player.sendPacket(new SystemMessage(SystemMessage.CONTRIB_SCORE_INCREASED).addNumber(contribScore));

				showChatWindow(player, 6, null, false);
				break;
			case 7: // Exchange Ancient Adena for Adena - SevenSigns 7 xxxxxxx
				int ancientAdenaConvert = 0;	
				
				try {
					ancientAdenaConvert = Integer.parseInt(command.substring(13).trim());
				}
				catch (Exception NumberFormatException) 
				{
					player.sendMessage("You must enter an integer amount.");
					break;
				}
				
				if (ancientAdenaAmount < ancientAdenaConvert || ancientAdenaConvert < 1) {
					player.sendPacket(new SystemMessage(SystemMessage.YOU_NOT_ENOUGH_ADENA));
					break;
				}
				
				if ((player.getAdena() + (ancientAdenaConvert * Config.VALUE_AA)) > 2000000000) //FIXME: dont really need value 2 bil but i dont know more about it 
				{
					player.sendMessage("Too much Adena in your Inventory");
					break;
				}
					
				L2ItemInstance destAncientAdena = player.getInventory().destroyItemByItemId(SevenSigns.ANCIENT_ADENA_ID, ancientAdenaConvert);
				player.addAdena(ancientAdenaConvert * Config.VALUE_AA);
					
                iu = new InventoryUpdate();
                iu.addModifiedItem(destAncientAdena);
                iu.addModifiedItem(player.getInventory().getAdenaInstance());
                player.sendPacket(iu);
				

				player.sendPacket(new SystemMessage(SystemMessage.EARNED_ADENA).addNumber(ancientAdenaConvert * Config.VALUE_AA));
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
					
					sm = new SystemMessage(SystemMessage.EARNED_ITEM);
					sm.addNumber(ancientAdenaReward);
					sm.addItemName(SevenSigns.ANCIENT_ADENA_ID);
					player.sendPacket(sm);
					
                    showChatWindow(player, 9, "a", false);
				}
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
	                		player.sendPacket(new SystemMessage(SystemMessage.YOU_NOT_ENOUGH_ADENA));
	                		break;
	                	}
		                	
	                	player.removeItemFromInventory(ancientAdena, ancientAdenaCost);
	                
	                	sm = new SystemMessage(SystemMessage.DISSAPEARED_ITEM);
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
				//FIXME: Deleted it till i can fix this null pointer
				/*StringBuffer listHtml = new StringBuffer("<html><body>");
				if (Config.ALT_LANGUAGE)
				{
					listHtml.append("<font color=\"LEVEL\">[Mu\u0061 \u0111\u1ED3 \u0111\u1EA1\u0063]</font><br>\u0110\u00E2y l\u00E0 \u0062\u1EA3ng \u0063h\u1ECDn \u0063\u00E1\u0063 \u0111\u01ED3 m\u00E0 \u0063h\u1EC9 \u0063\u00F3 nh\u1EEFng ng\u01B0\u1EDDi \u0063hi\u1EBFn th\u1EAFng m\u1EDBi \u0063\u00F3 th\u1EC3 mu\u0061!<br>");
				}else{
					listHtml.append("<font color=\"LEVEL\">[Purchase Consumable Items]</font><br>Here is a selection of items that only those who are victorious can purchase!<br>");
				}*/
				
				// Show 10 items per "page", as showing all of them will crash the client! :)
				int totalItems = SevenSigns.priestItems.length;
				int indexStart = (val - 12) * 10;
				int indexEnd = (((val - 11) * 10) > totalItems) ? totalItems : ((val - 11) * 10);
				int totalPages = (totalItems / 10) + 1;
				
                String path = SevenSigns.SEVEN_SIGNS_HTML_PATH + "signs_12.htm";
                String originalTemp = HtmCache.getInstance().getHtm(path);

                if (originalTemp != null)
                {
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
                else
                {
					_log.warning("Problem with HTML text " + SevenSigns.SEVEN_SIGNS_HTML_PATH + "signs_12.htm: " + path);
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
					player.sendPacket(new SystemMessage(SystemMessage.YOU_NOT_ENOUGH_ADENA));
					break;
				}
                
                if (adenaCost > player.getAdena()) {
                    player.sendPacket(new SystemMessage(SystemMessage.YOU_NOT_ENOUGH_ADENA));
                    break;
                }

                if (!player.getInventory().validateCapacity(inventorySize)) {
                    player.sendPacket(new SystemMessage(SystemMessage.SLOTS_FULL));
                    break;
                }
														
				L2ItemInstance boughtItem = ItemTable.getInstance().createItem(boughtItemId);
				player.getInventory().destroyItemByItemId(SevenSigns.ANCIENT_ADENA_ID, ancientAdenaCost);
                player.reduceAdena(adenaCost);
				player.getInventory().addItem(boughtItem);
						
                iu = new InventoryUpdate();
                iu.addNewItem(boughtItem);
                iu.addModifiedItem(player.getInventory().getAdenaInstance());
                player.sendPacket(iu);
						
				sm = new SystemMessage(SystemMessage.DISSAPEARED_ITEM);
				sm.addNumber(ancientAdenaCost);
				sm.addItemName(SevenSigns.ANCIENT_ADENA_ID);
				player.sendPacket(sm);
                
                sm = new SystemMessage(SystemMessage.DISSAPEARED_ITEM);
                sm.addNumber(adenaCost);
                sm.addItemName(57);
                player.sendPacket(sm);
						
				player.sendPacket(new SystemMessage(SystemMessage.EARNED_ITEM).addItemName(boughtItemId));
				break;
			case 17: // Exchange Seal Stones for Ancient Adena (Type Choice) - SevenSigns 17 x
				stoneType = Integer.parseInt(command.substring(14));
				int stoneId = 0;
				int stoneCount = 0;
				int stoneValue = 0;					
				String stoneColor = null;
                String content;
                
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

                path = SevenSigns.SEVEN_SIGNS_HTML_PATH + "signs_17.htm";
                content = HtmCache.getInstance().getHtm(path);

				if (content != null)
				{
					content = content.replaceAll("%stoneColor%", stoneColor);
					content = content.replaceAll("%stoneValue%", String.valueOf(stoneValue));
					content = content.replaceAll("%stoneCount%", String.valueOf(stoneCount));
					content = content.replaceAll("%stoneItemId%", String.valueOf(stoneId));						
					content = content.replaceAll("%objectId%", String.valueOf(getObjectId()));
					
					NpcHtmlMessage html = new NpcHtmlMessage(1);					
					html.setHtml(content);
					player.sendPacket(html);
				}
                else
                {
					_log.warning("Problem with HTML text " + SevenSigns.SEVEN_SIGNS_HTML_PATH + "signs_17.htm: " + path);
				}

				break;
			case 18: // Exchange Seal Stones for Ancient Adena - SevenSigns 18 xxxx xxxxxx
				int convertStoneId = Integer.parseInt(command.substring(14, 18));
				int convertCount = 0;
	
				try {
					convertCount = Integer.parseInt(command.substring(19).trim());
				}
				catch (Exception NumberFormatException) 
				{
					player.sendMessage("You must enter an integer amount.");
					break;
				}
									
				L2ItemInstance convertItem = player.getInventory().getItemByItemId(convertStoneId);
				
				if (convertItem == null) 
				{
					player.sendMessage("You do not have any seal stones of that type.");
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
					
                    iu = new InventoryUpdate();
                    iu.addModifiedItem(ancientAdena);
                    iu.addModifiedItem(convertItem);
                    player.sendPacket(iu);
					
					sm = new SystemMessage(SystemMessage.DISSAPEARED_ITEM);
					sm.addNumber(convertCount);
					sm.addItemName(convertStoneId);
					player.sendPacket(sm);
					
					sm = new SystemMessage(SystemMessage.EARNED_ITEM);
					sm.addNumber(ancientAdenaReward);
					sm.addItemName(SevenSigns.ANCIENT_ADENA_ID);
					player.sendPacket(sm);
				}
				else
                {
					player.sendMessage("You do not have that many seal stones.");
				}
				break;
            case 19: // Seal Information (for when joining a cabal)
                int chosenSeal = Integer.parseInt(command.substring(16));
                String fileSuffix = SevenSigns.getSealName(chosenSeal, true) + "_" + SevenSigns.getCabalShortName(cabal);
                
                showChatWindow(player, val, fileSuffix, false);
                break;
            case 20: // Seal Status (for when joining a cabal)
                StringBuffer contentBuffer = new StringBuffer("<html><body><font color=\"LEVEL\">[Seal Status]</font><br>");
                
                for (int i = 1; i < 4; i++) {
                    int sealOwner = SevenSigns.getInstance().getSealOwner(i);

                    if (sealOwner != SevenSigns.CABAL_NULL)
                        contentBuffer.append("[" + SevenSigns.getSealName(i, false) + ": " + SevenSigns.getCabalName(sealOwner) + "]<br>");
                    else
                        contentBuffer.append("[" + SevenSigns.getSealName(i, false) + ": Nothingness]<br>");
                }
                
                contentBuffer.append("<a action=\"bypass -h npc_" + getObjectId() + "_SevenSigns 3 " + cabal + "\">Go back.</a></body></html>");
                
                NpcHtmlMessage html = new NpcHtmlMessage(1);                    
                html.setHtml(contentBuffer.toString());
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
        else 
        {
            // this class dont know any other commands, let forward
            // the command to the parent class
            super.onBypassFeedback(player, command);
        }
	}
    
    private final boolean getPlayerAllyHasCastle(L2PcInstance player)
    {
        L2Clan playerClan = player.getClan();
        
        // The player is not in a clan, so return false.
        if (playerClan == null)
            return false;

        // If castle ownage check is clan-based rather than ally-based,
        // check if the player's clan has a castle and return the result.
        if (Config.ALT_GAME_REQUIRE_CLAN_CASTLE)
            return (playerClan.getHasCastle() > 0);
        
        int allyId = playerClan.getAllyId();

        // The player's clan is not in an alliance, so return false.
        if (allyId == 0)
            return false;
        
        // Check if another clan in the same alliance owns a castle,
        // by traversing the list of clans and act accordingly.
        L2Clan[] clanList = ClanTable.getInstance().getClans();
        
        for (L2Clan clan : clanList)
            if (clan.getAllyId() == allyId)
                if (clan.getHasCastle() > 0)
                    return true;
        
        return false;
    }
    
    private void showChatWindow(L2PcInstance player, int val, String suffix, boolean isDescription)
    {
        String filename = SevenSigns.SEVEN_SIGNS_HTML_PATH;
        
        filename += (isDescription) ? "desc_" + val : "signs_" + val;
        filename += (suffix != null) ? "_" + suffix + ".htm" : ".htm";
        
        showChatWindow(player, filename);
    }
}
