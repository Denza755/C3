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
 * 0000: 03 32 15 00 00 44 fe 00 00 80 f1 ff ff 00 00 00    .2...D..........<p>
 * 0010: 00 6b b4 c0 4a 45 00 6c 00 6c 00 61 00 6d 00 69    .k..JE.l.l.a.m.i<p>
 * 0020: 00 00 00 01 00 00 00 01 00 00 00 12 00 00 00 00    ................<p>
 * 0030: 00 00 00 2a 00 00 00 42 00 00 00 71 02 00 00 31    ...*...B...q...1<p>
 * 0040: 00 00 00 18 00 00 00 1f 00 00 00 25 00 00 00 00    ...........%....<p>
 * 0050: 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 f9    ................<p>
 * 0060: 00 00 00 b3 01 00 00 00 00 00 00 00 00 00 00 7d    ...............}<p>
 * 0070: 00 00 00 5a 00 00 00 32 00 00 00 32 00 00 00 00    ...Z...2...2....<p>
 * 0080: 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 67    ...............g<p>
 * 0090: 66 66 66 66 66 f2 3f 5f 63 97 a8 de 1a f9 3f 00    fffff.?_c.....?.<p>
 * 00a0: 00 00 00 00 00 1e 40 00 00 00 00 00 00 37 40 01    .............7..<p>
 * 00b0: 00 00 00 01 00 00 00 01 00 00 00 00 00 c1 0c 00    ................<p>
 * 00c0: 00 00 00 00 00 00 00 00 00 01 01 00 00 00 00 00    ................<p>
 * 00d0: 00 00<p>
 * <p>
 *  dddddSdddddddddddddddddddddddddddffffdddSdddccccccc (h)<p>
 *  dddddSdddddddddddddddddddddddddddffffdddSdddddccccccch
 * 
 * @version $Revision: 1.7.2.6.2.11 $ $Date: 2005/04/11 10:05:54 $
 */
public class CharInfo extends ServerBasePacket
{

	private static final String _S__03_CHARINFO = "[S] 03 CharInfo";
	private L2PcInstance _cha;
	private Inventory _inv;
	private int _x, _y, _z, _heading;
	private int _mAtkSpd, _pAtkSpd;
	private int _runSpd, _walkSpd, _swimRunSpd, _swimWalkSpd, _flRunSpd, _flWalkSpd, _flyRunSpd, _flyWalkSpd;

	/**
	 * @param _characters
	 */
	public CharInfo(L2PcInstance cha)
	{
		_cha = cha;
		_inv = cha.getInventory();
		_x = _cha.getX();
		_y = _cha.getY();
		_z = _cha.getZ();
		_heading = _cha.getHeading();
		_mAtkSpd = _cha.getMAtkSpd();
		_pAtkSpd = _cha.getPAtkSpd();
		_runSpd = _cha.getRunSpeed();
		_walkSpd = _cha.getWalkSpeed();
		_swimRunSpd = _flRunSpd = _flyRunSpd = _runSpd;
		_swimWalkSpd = _flWalkSpd = _flyWalkSpd = _walkSpd;
	}


	final void runImpl()
	{
		// no long-running tasks
	}
	
	final void writeImpl()
	{
		writeC(0x03);
		writeD(_x);
		writeD(_y);
		writeD(_z);
		writeD(_heading);
		writeD(_cha.getObjectId());
		writeS(_cha.getName());
		writeD(_cha.getRace().ordinal());
		writeD(_cha.getSex());
		writeD(_cha.getClassId().getId());

		writeD(0x00); // unknown, maybe underwear?
		writeD(_inv.getPaperdollItemId(Inventory.PAPERDOLL_HEAD));
		writeD(_inv.getPaperdollItemId(Inventory.PAPERDOLL_RHAND));
		writeD(_inv.getPaperdollItemId(Inventory.PAPERDOLL_LHAND));
		writeD(_inv.getPaperdollItemId(Inventory.PAPERDOLL_GLOVES));
		writeD(_inv.getPaperdollItemId(Inventory.PAPERDOLL_CHEST));
		writeD(_inv.getPaperdollItemId(Inventory.PAPERDOLL_LEGS));
		writeD(_inv.getPaperdollItemId(Inventory.PAPERDOLL_FEET));
		writeD(_inv.getPaperdollItemId(Inventory.PAPERDOLL_BACK));
		writeD(_inv.getPaperdollItemId(Inventory.PAPERDOLL_LRHAND));
		writeD(_inv.getPaperdollItemId(Inventory.PAPERDOLL_HAIR));
		
		writeD(_cha.getPvpFlag());
		writeD(_cha.getKarma());

		writeD(_mAtkSpd);
		writeD(_pAtkSpd);
		
		writeD(_cha.getPvpFlag());
		writeD(_cha.getKarma());

		writeD(_runSpd);
		writeD(_walkSpd);
		writeD(_swimRunSpd/*0x32*/);  // swimspeed
		writeD(_swimWalkSpd/*0x32*/);  // swimspeed
		writeD(_flRunSpd);
		writeD(_flWalkSpd);
		writeD(_flyRunSpd);
		writeD(_flyWalkSpd);
		writeF(1); // _cha.getProperMultiplier()
		writeF(_cha.getAttackSpeedMultiplier()); // _cha.getAttackSpeedMultiplier()
		writeF(_cha.getTemplate().collisionRadius);
		writeF(_cha.getTemplate().collisionHeight);

		writeD(_cha.getHairStyle());
		writeD(_cha.getHairColor());
		writeD(_cha.getFace());

		writeS(_cha.getTitle());
		writeD(_cha.getClanId());
		writeD(_cha.getClanCrestId());
		writeD(0x10);
		
		writeD(_cha.getAllyId());
		writeD(0);	// new in rev 417   siege-flags
		
		writeC(_cha.isSitting() ? 0 : 1);	// standing = 1  sitting = 0
		writeC(_cha.isRunning() ? 1 : 0);	// running = 1   walking = 0
		writeC(_cha.isInCombat() ? 1 : 0);
		writeC(_cha.isAlikeDead() ? 1 : 0);
		
		writeC(0x00);	// invisible = 1  visible =0
		writeC(_cha.getMountType());	// 1 on strider   2 on wyfern   0 no mount
		writeC(_cha.getPrivateStoreType());   //  1 - sellshop
		
		writeH(_cha.getCubics().size());
		for (int id : _cha.getCubics().keySet())
			writeH(id);
		
		writeC(0x00);	// find party members
		
        writeH(_cha.getAbnormalEffect());

//		Code that works for getEnchantEffect()
        writeH(0x00);
		writeC(0x00);                       //Changed by Thorgrim
		writeH(_cha.getRecomHave()); //Blue value for name (0 = white, 255 = pure blue)
		writeD(0x00);
		
		writeD(0x00); // unknown
		writeD(0x00); // unknown
		writeC(_cha.getEnchantEffect());
//        Alternative code...
/*        if (getClient().getRevision() >= 528)
        {
            writeC(0x00); // 00 c3 ?? maybe cp
            writeH(0x00); // 0000 c3 ??
			writeH(0x00); // 0000 c3 ??
			writeC(0x00); // ??32 C3
			writeF(0x00); // 0000 0000 0000 0000
			
			writeC(0xce); //
			writeD(_cha.getObjectId()); // C3 CharID
			writeF(0x00);
			writeH(0x00);
			writeC(0x00);
			writeD(0x00);
			
       }
         else
		{
			writeH(0x00); // c2
			writeC(0x00); // c2
			writeH(_cha.getRecomHave()); // c2 - recommendations (blue name if more than xx )
			writeD(0x00); // c2
		}*/
	}
	
	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.serverpackets.ServerBasePacket#getType()
	 */
	public String getType()
	{
		return _S__03_CHARINFO;
	}
	
}
