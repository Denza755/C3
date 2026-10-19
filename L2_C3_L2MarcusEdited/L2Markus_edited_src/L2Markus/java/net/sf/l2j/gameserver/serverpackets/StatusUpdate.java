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

import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;

import java.util.Vector;

/**
 * 
 * 01                // Packet Identifier <BR>
 * c6 37 50 40       // ObjectId <BR><BR>
 * 
 * 01 00             // Number of Attribute Trame of the Packet <BR><BR>
 * 
 * c6 37 50 40       // Attribute Identifier : 01-Level, 02-Experience, 03-STR, 04-DEX, 05-CON, 06-INT, 07-WIT, 08-MEN, 09-Current HP, 0a, Max HP...<BR>
 * cd 09 00 00       // Attribute Value <BR>
 * 
 * format   d d(dd) 
 * 
 * @version $Revision: 1.3.2.1.2.5 $ $Date: 2005/03/27 15:29:39 $
 */
public class StatusUpdate extends L2GameServerPacket
{
    private static final String _S__1A_STATUSUPDATE = "[S] 0e StatusUpdate";
    public static int LEVEL = 0x01;
    public static int EXP = 0x02;
    public static int STR = 0x03;
    public static int DEX = 0x04;
    public static int CON = 0x05;
    public static int INT = 0x06;
    public static int WIT = 0x07;
    public static int MEN = 0x08;

    public static int CUR_HP = 0x09;
    public static int MAX_HP = 0x0a;
    public static int CUR_MP = 0x0b;
    public static int MAX_MP = 0x0c;

    public static int SP = 0x0d;
    public static int CUR_LOAD = 0x0e;
    public static int MAX_LOAD = 0x0f;

    public static int P_ATK = 0x11;
    public static int ATK_SPD = 0x12;
    public static int P_DEF = 0x13;
    public static int EVASION = 0x14;
    public static int ACCURACY = 0x15;
    public static int CRITICAL = 0x16;
    public static int M_ATK = 0x17;
    public static int CAST_SPD = 0x18;
    public static int M_DEF = 0x19;
    public static int PVP_FLAG = 0x1a;
    public static int KARMA = 0x1b;

    public static int CUR_CP = 0x21;
    public static int MAX_CP = 0x22;

    private L2PcInstance _actor;

    private int _objectId;
    private Vector<Attribute> _attributes;

    class Attribute
    {
        /** id values
         * 09 - current health
         * 0a - max health
         * 0b - current mana
         * 0c - max mana
         * 
         */
        public int id;
        public int value;

        Attribute(int pId, int pValue)
        {
            this.id = pId;
            this.value = pValue;
        }
    }

    public StatusUpdate(int objectId)
    {
        _attributes = new Vector<Attribute>();
        _objectId = objectId;
    }

    public void addAttribute(int id, int level)
    {
        _attributes.add(new Attribute(id, level));
    }

	public StatusUpdate(L2PcInstance actor)
	{
		_actor = actor;
	}

    protected final void runImpl()
    {
        // no long-running tasks
    }

	@Override
	protected final void writeImpl()
	{
		writeC(14);

		if(_actor!=null){
			writeD(_actor.getObjectId());
			writeD(28); //all the attributes

			writeD(LEVEL);
			writeD(_actor.getLevel());
			writeD(EXP);
			writeD((int) _actor.getExp());
			writeD(STR);
			writeD(_actor.getSTR());
			writeD(DEX);
			writeD(_actor.getDEX());
			writeD(CON);
			writeD(_actor.getCON());
			writeD(INT);
			writeD(_actor.getINT());
			writeD(WIT);
			writeD(_actor.getWIT());
			writeD(MEN);
			writeD(_actor.getMEN());

			writeD(CUR_HP);
			writeD((int) _actor.getCurrentHp());
			writeD(MAX_HP);
			writeD(_actor.getMaxHp());
			writeD(CUR_MP);
			writeD((int) _actor.getCurrentMp());
			writeD(MAX_MP);
			writeD(_actor.getMaxMp());
			writeD(SP);
			writeD(_actor.getSp());
			writeD(CUR_LOAD);
			writeD(_actor.getCurrentLoad());
			writeD(MAX_LOAD);
			writeD(_actor.getMaxLoad());

			writeD(P_ATK);
			writeD(_actor.getPAtk(null));
			writeD(ATK_SPD);
			writeD(_actor.getPAtkSpd());
			writeD(P_DEF);
			writeD(_actor.getPDef(null));
			writeD(EVASION);
			writeD(_actor.getEvasionRate(null));
			writeD(ACCURACY);
			writeD(_actor.getAccuracy());
			writeD(CRITICAL);
			writeD(_actor.getCriticalHit(null, null));
			writeD(M_ATK);
			writeD(_actor.getMAtk(null, null));

			writeD(CAST_SPD);
			writeD(_actor.getMAtkSpd());
			writeD(M_DEF);
			writeD(_actor.getMDef(null, null));
			writeD(PVP_FLAG);
			writeD(_actor.getPvpFlag());
			writeD(KARMA);
			writeD(_actor.getKarma());
			writeD(CUR_CP);
			writeD((int) _actor.getCurrentCp());
			writeD(MAX_CP);
			writeD(_actor.getMaxCp());

		}else{

			writeD(_objectId);
			writeD(_attributes.size());

			for(int i = 0; i < _attributes.size(); i++)
			{
				Attribute temp = _attributes.get(i);

				writeD(temp.id);
				writeD(temp.value);
			}
		}

	}

    /* (non-Javadoc)
     * @see net.sf.l2j.gameserver.serverpackets.ServerBasePacket#getType()
     */
    public String getType()
    {
        return _S__1A_STATUSUPDATE;
    }
}
