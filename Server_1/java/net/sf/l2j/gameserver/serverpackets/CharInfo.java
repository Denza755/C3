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

import net.sf.l2j.gameserver.datatables.NpcTable;
import net.sf.l2j.gameserver.model.Inventory;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;

import java.util.Iterator;

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
public class CharInfo extends L2GameServerPacket
{

	private static final String _S__03_CHARINFO = "[S] 03 CharInfo";
    private L2PcInstance _cha;
    private Inventory _inv;
    private int _x;
    private int _y;
    private int _z;
    private int _heading;
    private int _mAtkSpd;
    private int _pAtkSpd;
    private int _runSpd;
    private int _walkSpd;
    private int _swimRunSpd;
    private int _swimWalkSpd;
    private int _flRunSpd;
    private int _flWalkSpd;
    private int _flyRunSpd;
    private int _flyWalkSpd;
    private float moveMultiplier;

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
     }

     protected final void runImpl()
     {
         moveMultiplier = _cha.getMovementSpeedMultiplier();
         _runSpd = (int)((float)_cha.getRunSpeed() / moveMultiplier);
         _walkSpd = (int)((float)_cha.getWalkSpeed() / moveMultiplier);
         _swimRunSpd = _flRunSpd = _flyRunSpd = _runSpd;
         _swimWalkSpd = _flWalkSpd = _flyWalkSpd = _walkSpd;
     }

     protected final void writeImpl()
     {
         if(_cha.getInvisible() == 1)
             return;
         if(_cha.getPoly().isMorphed())
         {
             writeC(22);
             writeD(_cha.getObjectId());
             writeD(_cha.getPoly().getPolyId() + 0xf4240);
             writeD(_cha.getKarma() <= 0 ? 0 : 1);
             writeD(_x);
             writeD(_y);
             writeD(_z);
             writeD(_heading);
             writeD(0);
             writeD(_mAtkSpd);
             writeD(_pAtkSpd);
             writeD(_runSpd);
             writeD(_walkSpd);
             writeD(_swimRunSpd);
             writeD(_swimWalkSpd);
             writeD(_flRunSpd);
             writeD(_flWalkSpd);
             writeD(_flyRunSpd);
             writeD(_flyWalkSpd);
             writeF(_cha.getMovementSpeedMultiplier());
             writeF(_cha.getAttackSpeedMultiplier());
             writeF(NpcTable.getInstance().getTemplate(_cha.getPoly().getPolyId()).collisionRadius);
             writeF(NpcTable.getInstance().getTemplate(_cha.getPoly().getPolyId()).collisionHeight);
             writeD(_inv.getPaperdollItemId(7));
             writeD(0);
             writeD(_inv.getPaperdollItemId(8));
             writeC(1);
             writeC(_cha.isRunning() ? 1 : 0);
             writeC(_cha.isInCombat() ? 1 : 0);
             writeC(_cha.isAlikeDead() ? 1 : 0);
             writeC(_cha.getInvisible());
             writeS(_cha.getName());
             writeS(_cha.getTitle());
             writeD(0);
             writeD(0);
             writeD(0);
             writeH(_cha.getAbnormalEffect());
             writeH(0);
             writeD(0);
             writeD(0);
             writeD(0);
             writeD(0);
             writeC(0);
         } else
         {
             writeC(3);
             writeD(_x);
             writeD(_y);
             writeD(_z);
             writeD(_heading);
             writeD(_cha.getObjectId());
             writeS(_cha.getName());
             writeD(_cha.getRace().ordinal());
             writeD(_cha.getSex());
             if(_cha.getClassIndex() == 0)
                 writeD(_cha.getClassId().getId());
             else
                 writeD(_cha.getBaseClass());
             writeD(0);
             writeD(_inv.getPaperdollItemId(6));
             writeD(_inv.getPaperdollItemId(7));
             writeD(_inv.getPaperdollItemId(8));
             writeD(_inv.getPaperdollItemId(9));
             writeD(_inv.getPaperdollItemId(10));
             writeD(_inv.getPaperdollItemId(11));
             writeD(_inv.getPaperdollItemId(12));
             writeD(_inv.getPaperdollItemId(13));
             writeD(_inv.getPaperdollItemId(14));
             writeD(_inv.getPaperdollItemId(15));
             writeD(_cha.getPvpFlag());
             writeD(_cha.getKarma());
             writeD(_mAtkSpd);
             writeD(_pAtkSpd);
             writeD(_cha.getPvpFlag());
             writeD(_cha.getKarma());
             writeD(_runSpd);
             writeD(_walkSpd);
             writeD(_swimRunSpd);
             writeD(_swimWalkSpd);
             writeD(_flRunSpd);
             writeD(_flWalkSpd);
             writeD(_flyRunSpd);
             writeD(_flyWalkSpd);
             writeF(_cha.getMovementSpeedMultiplier());
             writeF(_cha.getAttackSpeedMultiplier());
             writeF(_cha.getTemplate().collisionRadius);
             writeF(_cha.getTemplate().collisionHeight);
             writeD(_cha.getHairStyle());
             writeD(_cha.getHairColor());
             writeD(_cha.getFace());
             writeS(_cha.getTitle());
             writeD(_cha.getClanId());
             writeD(_cha.getClanCrestId());
             writeD(_cha.getAllyId());
             writeD(_cha.getAllyCrestId());
	        // In UserInfo leader rights and siege flags, but here found nothing??
	        // Therefore RelationChanged packet with that info is required
	         writeD(0);
             writeC(_cha.isSitting() ? 0 : 1);
             writeC(_cha.isRunning() ? 1 : 0);
             writeC(_cha.isInCombat() ? 1 : 0);
             writeC(_cha.isAlikeDead() ? 1 : 0);
             writeC(_cha.getInvisible());
             writeC(_cha.getMountType());
             writeC(_cha.getPrivateStoreType());
             writeH(_cha.getCubics().size());
             int id;
             for(Iterator i$ = _cha.getCubics().keySet().iterator(); i$.hasNext(); writeH(id))
                 id = ((Integer)i$.next()).intValue();

             writeC(0);
             if(getClient().getRevision() > 600)
             {
                 writeD(_cha.getAbnormalEffect());
             } else
             {
                 writeH(_cha.getAbnormalEffect());
                 writeH(0);
             }
             writeC(0);
             writeH(_cha.getRecomHave());
             writeD(0);
             writeD(0);
             writeD(0);
             writeC(_cha.isMounted() ? 0 : _cha.getEnchantEffect());
             writeC(0);
             if(getClient().getRevision() > 600)
             {
                 writeD(_cha.getClanCrestLargeId());
                 writeC(0);
                 writeD(0);
                 writeD(0);
                 writeD(1);
             }
             writeD(_cha.getNameColor());
         }
     }




	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.serverpackets.ServerBasePacket#getType()
	 */
	public String getType()
	{
		return _S__03_CHARINFO;
	}

}
