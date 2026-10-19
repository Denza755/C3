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

import net.sf.l2j.gameserver.model.Inventory;
import net.sf.l2j.gameserver.model.L2PcInstance;
/**
 *
 *
 * dddddSdddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddffffddddSddd   rev420
 * dddddSdddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddffffddddSdddcccddhh  rev478
 * @version $Revision: 1.2.2.2.2.8 $ $Date: 2005/03/27 15:29:39 $
 */
public class GMViewCharacterInfo extends ServerBasePacket
{
	private static final String _S__04_USERINFO = "[S] 8F GMViewCharacterInfo";
	private L2PcInstance _cha;
	private static int _test = 1;

	/**
	 * @param _characters
	 */
	public GMViewCharacterInfo(L2PcInstance cha)
	{
		_cha = cha;
	}


	final void runImpl()
	{
		// no long-running tasks
	}
	
	final void writeImpl()
	{
		writeC(0x8f);
		
		writeD(_cha.getX());
		writeD(_cha.getY());
		writeD(_cha.getZ());
		writeD(_cha.getHeading());
		writeD(_cha.getObjectId());
		writeS(_cha.getName());
		writeD(_cha.getRace().ordinal());
		writeD(_cha.getSex());
		writeD(_cha.getClassId().getId());
		writeD(_cha.getLevel());
		writeD(_cha.getExp());
		writeD(_cha.getSTR());
		writeD(_cha.getDEX());
		writeD(_cha.getCON());
		writeD(_cha.getINT());
		writeD(_cha.getWIT());
		writeD(_cha.getMEN());
		writeD(_cha.getMaxHp());
		writeD((int) _cha.getCurrentHp());
		writeD(_cha.getMaxMp());
		writeD((int)_cha.getCurrentMp());
		writeD(_cha.getSp());
		writeD(_cha.getCurrentLoad());
		writeD(_cha.getMaxLoad());

		writeD(0x28);  // unknown
		
		writeD(_cha.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_UNDER)); 
		writeD(_cha.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_REAR)); 
		writeD(_cha.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_LEAR)); 
		writeD(_cha.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_NECK)); 
		writeD(_cha.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_RFINGER));
		writeD(_cha.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_LFINGER));

		writeD(_cha.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_HEAD));
		writeD(_cha.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_RHAND));
		writeD(_cha.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_LHAND));
		writeD(_cha.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_GLOVES));
		writeD(_cha.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_CHEST));
		writeD(_cha.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_LEGS));
		writeD(_cha.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_FEET));
		writeD(_cha.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_BACK));
		writeD(_cha.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_LRHAND));

        if (getClient().getRevision() >= 514)
		{
            //writeD(0);     //c3    PAPERDOLL_item_hair ObjectId ??  
			writeD(_cha.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_HAIR));
		}

		writeD(_cha.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_UNDER)); 
		writeD(_cha.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_REAR));  
		writeD(_cha.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_LEAR)); 
		writeD(_cha.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_NECK)); 
		writeD(_cha.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_RFINGER));
		writeD(_cha.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_LFINGER));

		writeD(_cha.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_HEAD));
		writeD(_cha.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_RHAND));
		writeD(_cha.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_LHAND));
		writeD(_cha.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_GLOVES));
		writeD(_cha.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_CHEST));
		writeD(_cha.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_LEGS));
		writeD(_cha.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_FEET));
		writeD(_cha.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_BACK));
		writeD(_cha.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_LRHAND));

        if (getClient().getRevision() >= 514)
		{
            //writeD(0);     //c3    PAPERDOLL_item_hair ObjectId ??  
			writeD(_cha.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_HAIR));
		}

		writeD(_cha.getPAtk(null));
		writeD(_cha.getPAtkSpd());
		writeD(_cha.getPDef(null));
		writeD(_cha.getEvasionRate(null));
		writeD(_cha.getAccuracy());
		writeD(_cha.getCriticalHit(null, null));
		writeD(_cha.getMAtk(null, null));

		writeD(_cha.getMAtkSpd());
		writeD(_cha.getPAtkSpd());
		
		writeD(_cha.getMDef(null, null));

		writeD(_cha.getPvpFlag()); // 0-non-pvp  1-pvp = violett name
		writeD(_cha.getKarma());

		writeD(_cha.getRunSpeed());
		writeD(_cha.getWalkSpeed());
		writeD(0x32);	// swimspeed
		writeD(0x32);	// swimspeed
		writeD(_cha.getRunSpeed()/*_cha.getFloatingRunSpeed()*/);
		writeD(_cha.getWalkSpeed()/*_cha.getFloatingWalkSpeed()*/);
		writeD(_cha.getRunSpeed()/*_cha.getFlyingRunSpeed()*/);
		writeD(_cha.getWalkSpeed()/*_cha.getFlyingWalkSpeed()*/);
		writeF(1/*_cha.getProperMultiplier()*/); // move speed 0.7);/
		writeF(1/*_cha.getAttackSpeedMultiplier()*/); //2.9);//
		writeF(_cha.getTemplate().collisionRadius);  // scale
		writeF(_cha.getTemplate().collisionHeight); // y offset ??!? fem dwarf 4033

		writeD(_cha.getHairStyle());
		writeD(_cha.getHairColor());
		writeD(_cha.getFace());		
		if (_cha.isGM()) 
		{
			writeD(0x01);
		} 
		else 
		{
			writeD(0x00);		// builder level			
		}
		
		writeS(_cha.getTitle());
		writeD(_cha.getClanId());		// pledge id
		writeD(_cha.getClanId());		// pledge crest id
		writeD(_cha.getAllyId());		// ally id 
		writeD(_cha.getAllyId());		// ally crest id    new in rev 417
		if (_cha.isClanLeader())
		{
			writeD(0x60);			// siege-flags        0x40 - leader rights  0x20 - ?? 			
		} 
		else 
		{
			writeD(0);			// siege-flags      new in rev 417 						
		}
		
		writeC(0);//c2
		writeC(0);//c2
		writeC(0);//c2
		writeD(0);//c2
		writeD(0);//c2
		writeH(0);//c2
		writeH(0);//c2
		
        if (getClient().getRevision() >= 514)
        {
            writeD(0x16); //c3   unknown??
            writeD(0x00); //c3   unknown??
            writeD(0x6a); //c3   max cp??
            writeD(0x6a); //c3   current cp??
            writeC(0x00); //_cha.getEnchantEffect()); //c3 for client 528 only
        }
	}

	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.serverpackets.ServerBasePacket#getType()
	 */
	public String getType()
	{
		return _S__04_USERINFO;
	}
}
