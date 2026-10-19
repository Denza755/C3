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

import java.nio.ByteBuffer;
import java.util.logging.Logger;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.clientpackets.*;


/**
 * This class ...
 * 
 * @version $Revision: 1.18.2.6.2.8 $ $Date: 2005/04/06 16:13:25 $
 */
public class PacketHandler
{
	private static Logger _log = Logger.getLogger(PacketHandler.class.getName());

	private PacketHandler()
	{
	}
	
	
	public static ClientBasePacket handlePacket(ByteBuffer data, ClientThread _client)
	{
		ClientBasePacket msg = null;
		int id = data.get() & 0xff;
		switch(id)
		{
			case 0x00:
				msg = new ProtocolVersion(data, _client);
				break;
			case 0x01:
				msg = new MoveBackwardToLocation(data, _client); 
				break;
//			case 0x02:
//				// Say  ... not used any more ??
//				break;
			case 0x03:
				msg = new EnterWorld(data, _client);
				break;
			case 0x04:
				msg = new Action(data, _client);
				break;
				
			case 0x08:
				msg = new AuthLogin(data, _client);
				break;
			case 0x09:
				msg = new Logout(data, _client);
				break;
			case 0x0a:
				msg = new AttackRequest(data, _client);
				break;
			case 0x0b:
				msg = new CharacterCreate(data, _client);	
				break;
			case 0x0c:
				msg = new CharacterDelete(data, _client);	
				break;
			case 0x0d:
				msg = new CharacterSelected(data, _client);
				break;
			case 0x0e:
				msg = new NewCharacter(data, _client);
				break;
			case 0x0f:
				msg = new RequestItemList(data, _client);
				break;
//			case 0x10:
//				// RequestEquipItem ... not used any more, instead "useItem"  
//				break;
			case 0x11:
				msg = new RequestUnEquipItem(data, _client);
				break;
			case 0x12:
				msg = new RequestDropItem(data, _client);
				break;
				
			case 0x14:
				msg = new UseItem(data, _client);
				break;
			case 0x15:
				msg = new TradeRequest(data, _client);
				break;
			case 0x16:
				msg = new AddTradeItem(data, _client);
				break;
			case 0x17:
				msg = new TradeDone(data, _client);
				break;
			
//			case 0x1a:
//				// RequestTeleport
//				break;
			case 0x1b:
				msg = new RequestSocialAction(data, _client);
				break;
			case 0x1c:
				msg = new ChangeMoveType2(data, _client);
				break;
			case 0x1d:
				msg = new ChangeWaitType2(data, _client);
				break;
			case 0x1e:
				msg = new RequestSellItem(data, _client);
				break;
			case 0x1f:
				msg = new RequestBuyItem(data, _client);
				break;
//			case 0x20:
//				// RequestLinkHtml
//				break;
			case 0x21:
				msg = new RequestBypassToServer(data, _client);
				break;
//			case 0x22:
//				// RequestBBSwrite
//				break;
//			case 0x23:
//				// RequestCreatePledge   ... seems to be deprecated
//				break;
			case 0x24:
				msg = new RequestJoinPledge(data, _client);
				break;
			case 0x25:
				msg = new RequestAnswerJoinPledge(data, _client);
				break;
			case 0x26:
				msg = new RequestWithdrawalPledge(data, _client);  
				break;
			case 0x27:
				msg = new RequestOustPledgeMember(data, _client);  
				break;
//			case 0x28:
//				// RequestDismissPledge  
//				break;
			case 0x29:
				msg = new RequestJoinParty(data, _client);    
				break;
			case 0x2a:
				msg = new RequestAnswerJoinParty(data, _client);     
				break;
			case 0x2b:
				msg = new RequestWithDrawalParty(data, _client);     
				break;
			case 0x2c:
				msg = new RequestOustPartyMember(data, _client);
				break;
//			case 0x2d:
//				// RequestDismissParty  
//				break;
//			case 0x2e:
//				// RequestMagicSkillList  .. deprecated .. RequestSkillList is used instead   
//				break;
			case 0x2f:
				msg = new RequestMagicSkillUse(data, _client);
				break;
			case 0x30:
				msg = new Appearing(data, _client);  //  (after death)
				break;
			case 0x31:
				if (Config.ALLOW_WAREHOUSE) msg = new SendWareHouseDepositList(data, _client);  
				break;
			case 0x32:
                msg = new SendWareHouseWithDrawList(data, _client);  
				break;
			case 0x33:
				msg = new RequestShortCutReg(data, _client);    
				break;
//			case 0x34:
//				// RequestShortCutUse    
//				break;
			case 0x35:
				msg = new RequestShortCutDel(data, _client);    
				break;

			case 0x36:
				msg = new CannotMoveAnymore(data, _client);    
				break;
				
			case 0x37:
				msg = new RequestTargetCanceld(data, _client);
				break;
			case 0x38:
				msg = new Say2(data, _client);
				break;
				
			case 0x3c:
				msg = new RequestPledgeMemberList(data, _client);
				break;
				
//			case 0x3e:
//				// RequestMagicList  .. deprecated .. RequestSkillList is used instead
//				break;
			case 0x3f:
				msg = new RequestSkillList(data, _client);
				break;

//			case 0x41:
//				// MoveWithDelta    ... unused ?? or only on ship ??
//				break;
//			case 0x42:
//				// GetOnVehicle    
//				break;
//			case 0x43:
//				// GetOffVehicle   
//				break;
			case 0x44:
				msg = new AnswerTradeRequest(data, _client);
				break;
			case 0x45:
				msg = new RequestActionUse(data, _client);
				break;
			case 0x46:
				msg = new RequestRestart(data, _client);
				break;
//			case 0x47:
//				// RequestSiegeInfo   
//				break;
			case 0x48:
				msg = new ValidatePosition(data, _client);
				break;
//			case 0x49:
//				// RequestSEKCustom   
//				break;

//				THESE ARE NOW TEMPORARY DISABLED 
			case 0x4a:
//				new StartRotating(data, _client);  
				break;
			case 0x4b:
//				new FinishRotating(data, _client); 
				break;

				
				
//			case 0x4d:
//				// RequestStartPledgeWar    
//				break;
//			case 0x4e:
//				// RequestReplyStartPledgeWar      
//				break;
//			case 0x4f:
//				// RequestStopPledgeWar      
//				break;
//			case 0x50:
//				// RequestReplyStopPledgeWar         
//				break;
//			case 0x51:
//				// RequestSurrenderPledgeWar         
//				break;
//			case 0x52:
//				// RequestReplySurrenderPledgeWar            
//				break;
			case 0x53:
				msg = new RequestSetPledgeCrest(data, _client);           
				break;

			case 0x55:
				msg = new RequestGiveNickName(data, _client);             
				break;
//
			case 0x57:
				msg = new RequestShowBoard(data, _client);                
				break;
			case 0x58:
				msg = new RequestEnchantItem(data, _client);
				break;
			case 0x59:
				msg = new RequestDestroyItem(data, _client);
				break;

			case 0x5b:
				msg = new SendBypassBuildCmd(data, _client);   
				break;
			
//			case 0x5e:
//				// RequestFriendInvite	cS
//				break;
//			case 0x5f:
//				// RequestFriendAddReply	cd
//				break;
//			case 0x60:
//				// RequestFriendList	c
//				break;
//			case 0x61:
//				// RequestFriendDel	cS
//				break;
			case 0x62:
				msg = new CharacterRestore(data, _client);
				break;
			case 0x63:
				msg = new RequestQuestList(data, _client);
				break;
//			case 0x64:
//				// RequestDestroyQuest   
//				break;
            case 0x64:   
			  	msg = new RequestQuestAbort(data, _client);   
			  	break;				
			case 0x66:
				msg = new RequestPledgeInfo(data, _client);
				break;
//			case 0x67:
//				// RequestPledgeExtendedInfo   
//				break;
			case 0x68:
				msg = new RequestPledgeCrest(data, _client);
				break;
//			case 0x69:
//				// RequestSurrenderPersonally  
//				break;
//			case 0x6a:
//				// Ride  
//				break;
			case 0x6b: // send when talking to trainer npc, to show list of available skills
				msg = new RequestAquireSkillInfo(data, _client);//  --> [s] 0xa4;
				break;
			case 0x6c: // send when a skill to be learned is selected
				msg = new RequestAquireSkill(data, _client);    
				break;
			case 0x6d:
				msg = new RequestRestartPoint(data, _client); 
				break;
			case 0x6e: 
				msg = new RequestGMCommand(data, _client);  
				break;
			case 0x6f:
				msg = new RequestPartyMatchConfig(data, _client);   
				break;
			case 0x70:
				msg = new RequestPartyMatchList(data, _client);      
				break;
			case 0x71:
				msg = new RequestPartyMatchDetail(data, _client);     
				break;
			case 0x72:
				msg = new RequestCrystallizeItem(data, _client);
				break;
			case 0x73:
				msg = new RequestPrivateStoreManage(data, _client);        
				break;
			case 0x74:
				msg = new SetPrivateStoreListSell(data, _client);        
				break;
//			case 0x75:
//				msg = new RequestPrivateStoreManageCancel(data, _client);        
//				break;
			case 0x76:
				msg = new RequestPrivateStoreQuitSell(data, _client);          
				break;
			case 0x77:
				msg = new SetPrivateStoreMsgSell(data, _client);           
				break;
//			case 0x78:
//				// RequestPrivateStoreList         
//				break;
			case 0x79:
				msg = new SendPrivateStoreBuyList(data, _client);             
				break;
//			case 0x7a:
//				// ReviveReply            
//				break;
//			case 0x7b:
//				// RequestTutorialLinkHtml           
//				break;
//			case 0x7c:
//				// RequestTutorialPassCmdToServer              
//				break;
//			case 0x7d:
//				// RequestTutorialQuestionMark             
//				break;
//			case 0x7e:
//				// RequestTutorialClientEvent             
//				break;
//			case 0x7f:
//				// RequestPetition               
//				break;
//			case 0x80:
//				// RequestPetitionCancel               
//				break;
			case 0x81:
				msg = new RequestGmList(data, _client);
				break;
//			case 0x82:
//				// RequestJoinAlly 
//				break;
//			case 0x83:
//				// RequestAnswerJoinAlly
//				break;
//			case 0x84:
//				// RequestWithdrawAlly
//				break;
//			case 0x85:
//				// RequestOustAlly
//				break;
//			case 0x86:
//				// RequestDismissAlly
//				break;
//			case 0x87:
//				// RequestSetAllyCrest
//				break;
			case 0x88:
				msg = new RequestAllyCrest(data, _client);
				break;
			case 0x89:
				msg = new RequestChangePetName(data, _client);
				break;
			case 0x8a:
				msg = new RequestPetUseItem(data, _client);
				break;
			case 0x8b:
				msg = new RequestGiveItemToPet(data, _client);
				break;
			case 0x8c:
				msg = new RequestGetItemFromPet(data, _client);
				break;
				
//			case 0x8e:
//				// RequestAllyInfo
//				break;
			case 0x8f:
				msg = new RequestPetGetItem(data, _client);
				break;
			case 0x90:
				msg = new RequestPrivateStoreBuyManage(data, _client);
				break;
			case 0x91:
				msg = new SetPrivateStoreListBuy (data, _client);
				break;
//			case 0x92:
//				// RequestPrivateStoreBuyManageCancel
//				break;
			case 0x93:
				msg = new RequestPrivateStoreQuitBuy (data, _client);
				break;
			case 0x94:
				msg = new SetPrivateStoreMsgBuy(data, _client);
				break;
//			case 0x95:
//				// RequestPrivateStoreBuyList
//				break;
			case 0x96:
				msg = new SendPrivateStoreBuyBuyList(data, _client);
				break;
//			case 0x97:
//				// SendTimeCheckPacket
//				break;
//			case 0x98:
//				// RequestStartAllianceWar
//				break;
//			case 0x99:
//				// ReplyStartAllianceWar
//				break;
//			case 0x9a:
//				// RequestStopAllianceWar
//				break;
//			case 0x9b:
// 				// ReplyStopAllianceWar
//				break;
//			case 0x9c:
//				// RequestSurrenderAllianceWar
//				break;
			case 0x9d:
				// RequestSkillCoolTime
				if (Config.DEBUG) _log.info("Request Skill Cool Time .. ignored");
				msg = null;
				break;
//			case 0x9e:
//				// RequestPackageSendableItemList
//				break;
//			case 0x9f:
//				// RequestPackageSend
//				break;
//			case 0xa0:
//				// RequestBlock
//				break;
//			case 0xa1:
//				// RequestCastleSiegeInfo
//				break;
//			case 0xa2:
//				// RequestCastleSiegeAttackerList
//				break;
//			case 0xa3:
//				// RequestCastleSiegeInfo
//				break;
//			case 0xa4:
//				// RequestJoinCastleSiege
//				break;
//			case 0xa5:
//				// RequestConfirmCastleSiegeWaitingList
//				break;
//			case 0xa6:
//				// RequestSetCastleSiegeTime
//				break;
//			case 0xa7:
//				// RequestMultiSellChoose
//				break;
//			case 0xa8:
//				// NetPing
//				break;

            case 0xaa:
            	msg = new RequestUserCommand(data, _client);
                break;
            
            case 0xac:
            	msg = new RequestRecipeBookOpen(data, _client);
                break;
            case 0xad:
                msg = new RequestRecipeBookDestroy(data, _client);
                break;
			case 0xae:
				msg = new RequestRecipeItemMakeInfo(data, _client);
			    break;
			case 0xaf:
				msg = new RequestRecipeItemMakeSelf(data, _client);
			    break;
			case 0xb0:
				msg = new RequestRecipeShopManageList(data, _client);
			    break;
            case 0xb1:
            	msg = new RequestRecipeShopMessageSet(data, _client);
                break;
			case 0xb2:
				msg = new RequestRecipeShopListSet(data, _client);
			    break;
			case 0xb3:
				msg = new RequestRecipeShopManageQuit(data, _client);
			    break;
			case 0xb9:
				msg = new RequestEvaluate(data, _client);
			    break;
			case 0xbb:
				msg = new RequestHennaItemInfo(data, _client);
				break;
			case 0xbc:
				msg = new RequestHennaEquip(data, _client);
				break;
	
            case 0xc1:
                msg = new RequestMakeMacro(data, _client);
                break;
            case 0xc2:
				msg = new RequestDeleteMacro(data, _client);
                break;
            case 0xcd:
//				if (Config.DEBUG) _log.info("Request MAP .. ignored");
                msg = new RequestShowMiniMap(data, _client);
                break;
                
            case 0xce: // MSN dialogs so that you dont see them in the console.
				msg = null;
				break; 
				
			default:
			{
				msg = null;
				int sz = data.remaining(); 
				_log.warning("Unknown Packet:" + Integer.toHexString(id));
				byte[] arr = new byte[sz];
				data.get(arr);
				_log.warning(printData(arr, sz));
				break;
			}
		}
		if (msg != null) {
			//assert !msg._buf.hasRemaining(); // too many errors :(
			msg._buf = null;
		}
		return msg;
	}
	
	private static String printData(byte[] data, int len)
	{ 
		StringBuffer result = new StringBuffer();
		
		int counter = 0;
		
		for (int i=0;i< len;i++)
		{
			if (counter % 16 == 0)
			{
				result.append(fillHex(i,4)+": ");
			}
			
			result.append(fillHex(data[i] & 0xff, 2) + " ");
			counter++;
			if (counter == 16)
			{
				result.append("   ");
				
				int charpoint = i-15;
				for (int a=0; a<16;a++)
				{
					int t1 = data[charpoint++];
					if (t1 > 0x1f && t1 < 0x80)
					{
						result.append((char)t1);
					}
					else
					{
						result.append('.');
					}
				}
				
				result.append("\n");
				counter = 0;
			}
		}

		int rest = data.length % 16;
		if (rest > 0 )
		{
			for (int i=0; i<17-rest;i++ )
			{
				result.append("   ");
			}

			int charpoint = data.length-rest;
			for (int a=0; a<rest;a++)
			{
				int t1 = data[charpoint++];
				if (t1 > 0x1f && t1 < 0x80)
				{
					result.append((char)t1);
				}
				else
				{
					result.append('.');
				}
			}

			result.append("\n");
		}

		
		return result.toString();
	}
	
	private static String fillHex(int data, int digits)
	{
		String number = Integer.toHexString(data);
		
		for (int i=number.length(); i< digits; i++)
		{
			number = "0" + number;
		}
		
		return number;
	}
}
