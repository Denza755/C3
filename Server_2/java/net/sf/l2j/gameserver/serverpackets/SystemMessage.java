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

import java.util.Vector;

/**
 * This class ...
 * 
 * @version $Revision: 1.18.2.5.2.8 $ $Date: 2005/04/05 19:41:08 $
 */
public class SystemMessage extends ServerBasePacket
{
	// d d (d S/d d/d dd)
	//      |--------------> 0 - String  1-number 2-textref npcname (1000000-1002655)  3-textref itemname 4-textref skills 5-??	
	private static final int TYPE_SKILL_NAME = 4;
	private static final int TYPE_ITEM_NAME = 3;
	private static final int TYPE_NPC_NAME = 2;
	private static final int TYPE_NUMBER = 1;
	private static final int TYPE_TEXT = 0;
	private static final String _S__7A_SYSTEMMESSAGE = "[S] 64 SystemMessage";
	private int _messageId;
	private Vector<Integer> _types = new Vector<Integer>();
	private Vector<Object> _values = new Vector<Object>();
	
    public static final int S1_S2 = 614;
	//PeaceZones
	public static final int TARGET_IN_PEACEZONE = 85;
	public static final int CANT_ATK_PEACEZONE = 84;
	// magic related
	public static final int NOT_ENOUGH_HP = 23;
    public static final int NOT_ENOUGH_MP = 24;
    public static final int NOT_ENOUGH_ITEMS = 351;
    
	public static final int USE_S1 = 46;
	public static final int YOU_FEEL_S1_EFFECT = 110;
	public static final int REJUVENATING_HP = 25;
	public static final int CANNOT_USE_ITEM_WHILE_USING_MAGIC = 104;
	public static final int CASTING_INTERRUPTED = 27;
    public static final int S2_HP_RESTORED_BY_S1 = 1067;
		
	public static final int EARNED_ITEM = 53;  
	public static final int EARNED_ADENA = 52;  
	public static final int DISSAPEARED_ITEM = 301;	
	
	public static final int YOU_DID_S1_DMG = 0x23;
	public static final int S1_GAVE_YOU_S2_DMG = 0x24;
	public static final int EFFECT_S1_DISAPPEARED = 0x5c;
	public static final int YOU_EARNED_S1_EXP_AND_S2_SP = 0x5f;
	public static final int YOU_INCREASED_YOUR_LEVEL = 0x60;
	
	public static final int NOTHING_HAPPENED = 61;
	public static final int ITEM_MISSING_TO_LEARN_SKILL = 0x114;
	public static final int LEARNED_SKILL_S1 = 0x115;
	public static final int NOT_ENOUGH_SP_TO_LEARN_SKILL = 0x116;
	
	public static final int FALL_DAMAGE_S1 = 0x128;
	public static final int DROWN_DAMAGE_S1 = 0x129;

	public static final int YOU_DROPPED_S1 = 0x12a;
	public static final int TARGET_IS_NOT_FOUND_IN_THE_GAME = 0x91;
	public static final int TARGET_CANT_FOUND = 50;
	public static final int TARGET_MUST_BE_IN_CLAN = 234;

	public static final int SOULSHOTS_GRADE_MISMATCH = 0x151;
	public static final int NOT_ENOUGH_SOULSHOTS = 0x152;
	public static final int CANNOT_USE_SOULSHOTS = 0x153;
	
	public static final int SPIRITSHOTS_GRADE_MISMATCH = 530;
	public static final int NOT_ENOUGH_SPIRITSHOTS = 531;
	public static final int CANNOT_USE_SPIRITSHOTS = 532;		
	
	public static final int S1_IS_NOT_ONLINE = 3;
	
	public static final int GETTING_READY_TO_SHOOT_AN_ARROW =   41;
	public static final int AVOIDED_S1s_ATTACK              =   42;
	public static final int MISSED_TARGET                   =   43;
	public static final int CRITICAL_HIT                    =   44;
	public static final int CRITICAL_HIT_BY_PET             = 1017;
	public static final int CRITICAL_HIT_BY_SUMMONED_MOB    = 1028;
	public static final int SHIELD_DEFENCE_SUCCESSFULL      =  111;
	public static final int OVER_HIT                        =  361;
	public static final int SPOIL_SUCCESS					=  612;
	public static final int SPOIL_CANNOT_USE				=  661;
	
	// party related 
	public static final int WAITING_FOR_REPLY = 164;
	
	public static final int S1_INVITED_YOU_TO_PARTY_FINDER_KEEPER = 572;
	public static final int S1_INVITED_YOU_TO_PARTY_RANDOM = 573;
	
	public static final int S1_INVITED_YOU_TO_PARTY = 66;
	public static final int YOU_INVITED_S1_TO_PARTY = 105;
	
	public static final int OTHER_PARTY_IS_DROZEN = 692;
	
	public static final int YOU_JOINED_S1_PARTY = 106;
	public static final int S1_JOINED_PARTY = 107;
	public static final int S1_LEFT_PARTY = 108;
	public static final int YOU_LEFT_PARTY = 200;
	public static final int ONLY_LEADER_CAN_INVITE = 154;

	public static final int S1_DID_NOT_REPLY = 135;
	public static final int YOU_DID_NOT_REPLY = 136;
	
	public static final int PLAYER_DECLINED = 305;
	
	public static final int PARTY_FULL = 155;
	public static final int S1_IS_ALREADY_IN_PARTY = 160;
	public static final int INVITED_USER_NOT_ONLINE = 161;

	public static final int PARTY_DISPERSED = 203;

	//weight & inventory
	public static final int WEIGHT_LIMIT_EXCEEDED = 422;
	public static final int NOT_ENOUGH_ARROWS = 112;

	// trade 
	public static final int S1_IS_BUSY_TRY_LATER = 153;
	public static final int TARGET_IS_INCORRECT = 144;
	public static final int ALREADY_TRADING = 142;
	public static final int REQUEST_S1_FOR_TRADE = 118;
	public static final int S1_DENIED_TRADE_REQUEST = 119;
	public static final int BEGIN_TRADE_WITH_S1 = 120;
	public static final int S1_CONFIRMED_TRADE = 121;
	public static final int TRADE_SUCCESSFUL = 123;
	public static final int S1_CANCELED_TRADE = 124;
    public static final int CANNOT_USE_ITEM_WHILE_TRADING = 149;
    
	//private store & store
	public static final int S1_PURCHASED_S2 = 378;
	public static final int S1_PURCHASED_S2_S3 = 379;
	public static final int S1_PURCHASED_S3_S2_s = 380;
	public static final int PURCHASED_S2_FROM_S1 = 559;
	public static final int PURCHASED_S2_S3_FROM_S1 =560;
	public static final int	PURCHASED_S3_S2_s_FROM_S1 =561;
	public static final int	THE_PURCHASE_IS_COMPLETE =700;
	public static final int	THE_PURCHASE_PRICE_IS_HIGHER_THAN_MONEY = 720;
	public static final int YOU_NOT_ENOUGH_ADENA = 279;
    public static final int CANNOT_TRADE_DISCARD_DROP_ITEM_WHILE_IN_SHOPMODE = 1065;

    
	//Clan stuff
	public static final int FAILED_TO_CREATE_CLAN = 190;
	public static final int CLAN_NAME_INCORRECT = 261;
	public static final int CLAN_NAME_TOO_LONG = 262;
	
	public static final int CLAN_CREATED = 189;
	public static final int CLAN_LVL_3_NEEDED_TO_ENDOWE_TITLE = 271;
	public static final int CLAN_LVL_3_NEEDED_TO_SET_CREST = 272;
	public static final int CANNOT_INVITE_YOURSELF = 4;
	public static final int S1_WORKING_WITH_ANOTHER_CLAN = 10;
	public static final int ENTERED_THE_CLAN = 195;
	public static final int S1_REFUSED_TO_JOIN_CLAN = 196;
	public static final int S1_HAS_JOINED_CLAN = 222;
	public static final int CLAN_MEMBER_S1_LOGGED_IN = 304;
	
	public static final int CLAN_LEVEL_INCREASED = 274;
	public static final int FAILED_TO_INCREASE_CLAN_LEVEL = 275;
	
	public static final int CLAN_MEMBER_S1_EXPELLED = 191;
	public static final int CLAN_MEMBERSHIP_TERMINATED = 199;
    
    public static final int YOU_DO_NOT_HAVE_THE_RIGHT_TO_USE_CLAN_WAREHOUSE = 709;
    public static final int ONLY_LEVEL_1_CLAN_OR_HIGHER_CAN_USE_WAREHOUSE = 710;
    public static final int ONLY_CLAN_LEADER_CAN_RETRIEVE_ITEMS_FROM_CLAN_WAREHOUSE = 1039;
	
	//pickup items
	public static final int YOU_PICKED_UP_S1_ADENA = 28;
	public static final int YOU_PICKED_UP_S1_S2 = 29;
	public static final int YOU_PICKED_UP_S1 = 30;	
	public static final int S1_PICKED_UP_S2_S3 = 299;
	public static final int S1_PICKED_UP_S2 = 300;
	
	//GM LIST
	public static final int GM_LIST = 703;
	public static final int GM_S1 = 704;
    
    // loc messages
    public static final int LOC_TI_S1_S2_S3 = 910;
    public static final int LOC_GLUDIN_S1_S2_S3 = 911;
    public static final int LOC_GLUDIO_S1_S2_S3 = 912;
    public static final int LOC_NETRAL_ZONE_S1_S2_S3 = 913;
    public static final int LOC_ELVEN_S1_S2_S3 = 914;
    public static final int LOC_DARK_ELVEN_S1_S2_S3 = 915;
    public static final int LOC_DION_S1_S2_S3 = 916;
    public static final int LOC_FLORAN_S1_S2_S3 = 917;
    public static final int LOC_GIRAN_S1_S2_S3 = 918;
    public static final int LOC_GIRAN_HARBOR_S1_S2_S3 = 919;
    public static final int LOC_ORC_S1_S2_S3 = 920;
    public static final int LOC_DWARVEN_S1_S2_S3 = 921;
    public static final int LOC_OREN_S1_S2_S3 = 922;
    public static final int LOC_HUNTER_S1_S2_S3 = 923;
    public static final int LOC_ADEN_S1_S2_S3 = 924;
    public static final int LOC_COLISEUM_S1_S2_S3 = 925;
    public static final int LOC_HEINE_S1_S2_S3 = 926;
	
	//Crystallize
	public static final int CRYSTALLIZE_LEVEL_TOO_LOW = 562;
    
    public static final int EARNED_S2_S1_s = 53;

	public static final int S1_CANNOT_BE_USED = 113;
    
    //Monster Derby
    public static final int MONSRACE_TICKETS_AVALIABLE_FOR_S1_RACE = 816;
    public static final int MONSRACE_TICKETS_NOW_AVALIABLE_FOR_S1_RACE = 817;
    public static final int MONSRACE_TICKETS_STOP_IN_S1_MINUTES = 818;
    public static final int MONSRACE_TICKET_SALES_CLOSED = 819;
    public static final int MONSRACE_BEGINS_IN_S1_MINUTES = 820;
    public static final int MONSRACE_BEGINS_IN_30_SECONDS = 821;
    public static final int MONSRACE_COUNTDOWN_IN_FIVE_SECONDS = 822;
    public static final int MONSRACE_BEGINS_IN_S1_SECONDS = 823;
    public static final int MONSRACE_RACE_START = 824;
    public static final int MONSRACE_RACE_END = 825;
    public static final int MONSRACE_FIRST_PLACE_S1_SECOND_S2 = 826;
    
	//Pets
	public static final int ITEM_NOT_FOR_PETS = 544;
    public static final int DEAD_PET_CANNOT_BE_RETURNED = 589;
	public static final int CANNOT_GIVE_ITEMS_TO_DEAD_PET = 590;
	public static final int CANNOT_EQUIP_PET_ITEM = 600;
    public static final int PET_EARNED_S1_EXP = 1014;
	public static final int PET_GAVE_DAMAGE_OF_S1 = 1015;
    public static final int PET_RECEIVED_DAMAGE_OF_S2_BY_S1 = 1016;
    public static final int PET_CRITICAL_HIT = 1017;
	public static final int PET_DID_S1_DMG = 1015;
    public static final int S1_GAME_PET_S2_DMG = 1016;
	
    //Summons
    public static final int SUMMON_GAVE_DAMAGE_OF_S1 = 1026;
    public static final int SUMMON_RECEIVED_DAMAGE_OF_S2_BY_S1 = 1027;
    public static final int SUMMON_CRITICAL_HIT = 1028;
    
    public static final int CUBIC_SUMMONING_FAILED = 568;
    
	// enchants
	public static final int S1_SUCCESSFULLY_ENCHANTED = 62;
	public static final int S1_S2_SUCCESSFULLY_ENCHANTED = 63;
	public static final int ENCHANTMENT_FAILED_S1_EVAPORATED = 64;
	public static final int ENCHANTMENT_FAILED_S1_S2_EVAPORATED = 65;
	public static final int SELECT_ITEM_TO_ENCHANT = 303;
	public static final int INAPPROPRIATE_ENCHANT_CONDITION = 355;
	
	/**
	 * @param _characters
	 */
	public SystemMessage(int messageId)
	{
		_messageId = messageId;
	}
    
    public static SystemMessage sendString(String msg)
    {
        SystemMessage sm = new SystemMessage(614);
        sm.addString(msg);
        return sm;
    }
	
	public void addString(String text)
	{
		_types.add(new Integer(TYPE_TEXT));
		_values.add(text);
	}

	public void addNumber(int number)
	{
		_types.add(new Integer(TYPE_NUMBER));
		_values.add(new Integer(number));
	}
	
	public void addNpcName(int id)
	{
		_types.add(new Integer(TYPE_NPC_NAME));
		_values.add(new Integer(1000000+id));
	}

	public void addItemName(int id)
	{
		_types.add(new Integer(TYPE_ITEM_NAME));
		_values.add(new Integer(id));
	}

	public void addSkillName(int id)
	{
		_types.add(new Integer(TYPE_SKILL_NAME));
		_values.add(new Integer(id));
	}

	final void runImpl()
	{
		// no long-running tasks
	}
	
	final void writeImpl()
	{
		writeC(0x64);

		writeD(_messageId);
		writeD(_types.size());

		for (int i=0; i<_types.size();i++)
		{
			int t = _types.get(i).intValue();


			writeD(t);

			switch (t)
			{
				case TYPE_TEXT:
				{
					writeS( (String)_values.get(i));
					break;
				}
				case TYPE_NUMBER:
				case TYPE_NPC_NAME:
				case TYPE_ITEM_NAME:
				{
					int t1 = ((Integer)_values.get(i)).intValue();
					writeD(t1);	
					
					break;
				}
				case TYPE_SKILL_NAME:
				{
					int t1 = ((Integer)_values.get(i)).intValue();
					writeD(t1);	
					writeD(1);  //??
				}
			}
		}
	}

	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.serverpackets.ServerBasePacket#getType()
	 */
	public String getType()
	{
		return _S__7A_SYSTEMMESSAGE;
	}
}
	
