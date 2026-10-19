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

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.datatables.NpcTable;
import net.sf.l2j.gameserver.model.base.Race;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;

import java.util.Iterator;

/**
 * 0000: 04 03 15 00 00 77 ff 00 00 80 f1 ff ff 00 00 00    .....w..........
 * 0010: 00 2a 89 00 4c 43 00 61 00 6c 00 61 00 64 00 6f    .*..LC.a.l.a.d.o
 * 0020: 00 6e 00 00 00 01 00 00 00 00 00 00 00 19 00 00    .n..............
 * 0030: 00 0d 00 00 00 ee 81 02 00 15 00 00 00 18 00 00    ................
 * 0040: 00 19 00 00 00 25 00 00 00 17 00 00 00 28 00 00    .....%.......(..
 * 0050: 00 14 01 00 00 14 01 00 00 02 01 00 00 02 01 00    ................
 * 0060: 00 fa 09 00 00 81 06 00 00 26 34 00 00 2e 00 00    .........&4.....
 * 0070: 00 00 00 00 00 db 9f a1 41 93 26 64 41 de c8 31    ........A.&dA..1
 * 0080: 41 ca 73 c0 41 d5 22 d0 41 83 bd 41 41 81 56 10    A.s.A.".A..AA.V.
 * 0090: 41 00 00 00 00 27 7d 30 41 69 aa e0 40 b4 fb d3    A....'}0Ai..@...
 * 00a0: 41 91 f9 63 41 00 00 00 00 81 56 10 41 00 00 00    A..cA.....V.A...
 * 00b0: 00 71 00 00 00 71 00 00 00 76 00 00 00 74 00 00    .q...q...v...t..
 * 00c0: 00 74 00 00 00 2a 00 00 00 e8 02 00 00 00 00 00    .t...*..........
 * 00d0: 00 5f 04 00 00 ac 01 00 00 cf 01 00 00 62 04 00    ._...........b..
 * 00e0: 00 00 00 00 00 e8 02 00 00 0b 00 00 00 52 01 00    .............R..
 * 00f0: 00 4d 00 00 00 2a 00 00 00 2f 00 00 00 29 00 00    .M...*.../...)..
 * 0100: 00 12 00 00 00 82 01 00 00 52 01 00 00 53 00 00    .........R...S..
 * 0110: 00 00 00 00 00 00 00 00 00 7a 00 00 00 55 00 00    .........z...U..
 * 0120: 00 32 00 00 00 32 00 00 00 00 00 00 00 00 00 00    .2...2..........
 * 0130: 00 00 00 00 00 00 00 00 00 a4 70 3d 0a d7 a3 f0    ..........p=....
 * 0140: 3f 64 5d dc 46 03 78 f3 3f 00 00 00 00 00 00 1e    ?d].F.x.?.......
 * 0150: 40 00 00 00 00 00 00 38 40 02 00 00 00 01 00 00    @......8@.......
 * 0160: 00 00 00 00 00 00 00 00 00 00 00 c1 0c 00 00 01    ................
 * 0170: 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00    ................
 * 0180: 00 00 00 00                                        ....
 *
 *
 * dddddSdddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddffffddddSdddcccdd (h)
 * dddddSdddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddffffddddSdddddcccddh (h) c dc hhdh
 * @version $Revision: 1.14.2.4.2.12 $ $Date: 2005/04/11 10:05:55 $
 */
public class UserInfo extends L2GameServerPacket
{
	private static final String _S__04_USERINFO = "[S] 04 UserInfo";
	private L2PcInstance _cha;
    private int _runSpd, _walkSpd, _swimRunSpd, _swimWalkSpd, _flRunSpd, _flWalkSpd, _flyRunSpd, _flyWalkSpd;
    private float moveMultiplier;

    public UserInfo(L2PcInstance cha)
    {
        _cha = cha;
         moveMultiplier = _cha.getMovementSpeedMultiplier();
        _runSpd = (int)((float)_cha.getRunSpeed() / moveMultiplier);
        _walkSpd = (int)((float)_cha.getWalkSpeed() / moveMultiplier);
        _swimRunSpd = _flRunSpd = _flyRunSpd = _runSpd;
        _swimWalkSpd = _flWalkSpd = _flyWalkSpd = _walkSpd;
    }

    protected final void runImpl()
    {
        //null
    }


    protected final void writeImpl()
    {
        writeC(4);
        writeD(_cha.getX());
        writeD(_cha.getY());
        writeD(_cha.getZ());
        writeD(_cha.getHeading());
        writeD(_cha.getObjectId());
        writeS(_cha.getName());
        writeD(_cha.getRace().ordinal());
        writeD(_cha.getSex());
        if(_cha.getClassIndex() == 0)
            writeD(_cha.getClassId().getId());
        else
            writeD(_cha.getBaseClass());
        writeD(_cha.getLevel());
        writeD(_cha.getExp());
        writeD(_cha.getSTR());
        writeD(_cha.getDEX());
        writeD(_cha.getCON());
        writeD(_cha.getINT());
        writeD(_cha.getWIT());
        writeD(_cha.getMEN());
        writeD(_cha.getMaxHp());
        writeD((int)_cha.getCurrentHp());
        writeD(_cha.getMaxMp());
        writeD((int)_cha.getCurrentMp());
        writeD(_cha.getSp());
        writeD(_cha.getCurrentLoad());
        writeD(_cha.getMaxLoad());
        writeD(0);
        writeD(_cha.getInventory().getPaperdollObjectId(0));
        writeD(_cha.getInventory().getPaperdollObjectId(2));
        writeD(_cha.getInventory().getPaperdollObjectId(1));
        writeD(_cha.getInventory().getPaperdollObjectId(3));
        writeD(_cha.getInventory().getPaperdollObjectId(5));
        writeD(_cha.getInventory().getPaperdollObjectId(4));
        writeD(_cha.getInventory().getPaperdollObjectId(6));
        writeD(_cha.getInventory().getPaperdollObjectId(7));
        writeD(_cha.getInventory().getPaperdollObjectId(8));
        writeD(_cha.getInventory().getPaperdollObjectId(9));
        writeD(_cha.getInventory().getPaperdollObjectId(10));
        writeD(_cha.getInventory().getPaperdollObjectId(11));
        writeD(_cha.getInventory().getPaperdollObjectId(12));
        writeD(_cha.getInventory().getPaperdollObjectId(13));
        writeD(_cha.getInventory().getPaperdollObjectId(14));
        writeD(_cha.getInventory().getPaperdollObjectId(15));
        writeD(_cha.getInventory().getPaperdollItemId(0));
        writeD(_cha.getInventory().getPaperdollItemId(2));
        writeD(_cha.getInventory().getPaperdollItemId(1));
        writeD(_cha.getInventory().getPaperdollItemId(3));
        writeD(_cha.getInventory().getPaperdollItemId(5));
        writeD(_cha.getInventory().getPaperdollItemId(4));
        writeD(_cha.getInventory().getPaperdollItemId(6));
        writeD(_cha.getInventory().getPaperdollItemId(7));
        writeD(_cha.getInventory().getPaperdollItemId(8));
        writeD(_cha.getInventory().getPaperdollItemId(9));
        writeD(_cha.getInventory().getPaperdollItemId(10));
        writeD(_cha.getInventory().getPaperdollItemId(11));
        writeD(_cha.getInventory().getPaperdollItemId(12));
        writeD(_cha.getInventory().getPaperdollItemId(13));
        writeD(_cha.getInventory().getPaperdollItemId(14));
        writeD(_cha.getInventory().getPaperdollItemId(15));
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
        if(_cha.getMountType() != 0 && _cha.getPet() != null)
        {
            writeF(_cha.getPet().getTemplate().collisionRadius);
            writeF(_cha.getPet().getTemplate().collisionHeight);
        } else
        {
            writeF(_cha.getTemplate().collisionRadius);
            writeF(_cha.getTemplate().collisionHeight);
        }
        writeD(_cha.getHairStyle());
        writeD(_cha.getHairColor());
        writeD(_cha.getFace());
        writeD(_cha.getAccessLevel() <= 0 ? 0 : 1);
        String title = _cha.getTitle();
        if(_cha.getInvisible() == 1)
            title = "Invisible";
        if(_cha.getPoly().isMorphed())
            title = (new StringBuilder()).append(title).append(" - ").append(NpcTable.getInstance().getTemplate(_cha.getPoly().getPolyId()).name).toString();
        writeS(title);
        writeD(_cha.getClanId());
        writeD(_cha.getClanCrestId());
        writeD(_cha.getAllyId());
        writeD(_cha.getAllyCrestId());
        writeD(_cha.isClanLeader() ? 96 : 0);
        writeC(_cha.getMountType());
        writeC(_cha.getPrivateStoreType());
        writeC(_cha.hasDwarvenCraft() ? 1 : 0);
        writeD(_cha.getPkKills());
        writeD(_cha.getPvpKills());
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
        writeD(_cha.getClanPrivileges());
        writeD(0);
        writeD(0);
        writeD(0);
        writeD(0);
        writeD(0);
        writeD(0);
        writeD(0);
        writeH(_cha.getRecomLeft());
        writeH(_cha.getRecomHave());
        writeD(0);
        if(_cha.isGM())
            writeH(Config.INVENTORY_MAXIMUM_GM);
        else
        if(_cha.getRace() == Race.dwarf)
            writeH(Config.INVENTORY_MAXIMUM_DWARF);
        else
            writeH(Config.INVENTORY_MAXIMUM_NO_DWARF);
        writeD(_cha.getClassId().getId());
        writeD(0);
        writeD(_cha.getMaxCp());
        writeD((int)_cha.getCurrentCp());
        writeC(_cha.isMounted() ? 0 : _cha.getEnchantEffect());
        writeC(0);
        writeD(_cha.getNameColor());
    }


	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.serverpackets.ServerBasePacket#getType()
	 */
	public String getType()
	{
		return _S__04_USERINFO;
	}
}
