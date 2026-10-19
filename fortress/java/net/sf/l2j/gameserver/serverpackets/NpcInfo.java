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
import net.sf.l2j.gameserver.model.L2CastleGuardInstance;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2MonsterInstance;
import net.sf.l2j.gameserver.model.L2NpcInstance;
import net.sf.l2j.gameserver.model.L2PetInstance;
import net.sf.l2j.gameserver.model.L2Summon;
/**
 * This class ...
 * 
 * @version $Revision: 1.7.2.4.2.9 $ $Date: 2005/04/11 10:05:54 $
 */
public class NpcInfo extends ServerBasePacket
{
	//   ddddddddddddddddddffffdddcccccSSddd dddddc
	     
	     
	private static final String _S__22_NPCINFO = "[S] 16 NpcInfo";
	private L2Character _cha;
	private int _x, _y, _z, _heading;
	private int _npcId;
	private boolean _isAttackable, _isSummoned;
	private int _mAtkSpd, _pAtkSpd;
	private int _runSpd, _walkSpd, _swimRunSpd, _swimWalkSpd, _flRunSpd, _flWalkSpd, _flyRunSpd, _flyWalkSpd;
	private int _rhand, _lhand;
    private int collisionHeight, collisionRadius;
    private String _name = "";

	/**
	 * @param _characters
	 */
	public NpcInfo(L2NpcInstance cha, L2Character attacker)
	{
		_cha = cha;
		_npcId = cha.getTemplate().npcId;
		_isAttackable = cha.isAutoAttackable(attacker);
		_rhand = cha.getTemplate().rhand;
		_lhand = cha.getTemplate().lhand;
		_isSummoned = false;
        collisionHeight = _cha.getTemplate().collisionHeight;
        collisionRadius = _cha.getTemplate().collisionRadius;
        if (Config.SERVER_SIDE_NPC_NAME){
	    if (Config.SERVER_SIDE_NPC_NAME_WITH_LVL && _cha instanceof L2MonsterInstance)
	    {
    		String _aggro = "";
		if (cha.getAggroRange()>0)
		    _aggro="*";
		_name = "Lv"+cha.getLevel()+_aggro+" "+cha.getName();
	    }
	    else
        	_name = cha.getName();
	}
	}
	
	public NpcInfo(L2CastleGuardInstance cha, L2Character attacker)
	{
		_cha = cha;
		_npcId = cha.getTemplate().npcId;
		_isAttackable = cha.isAutoAttackable(attacker);
		_rhand = cha.getTemplate().rhand;
		_lhand = cha.getTemplate().lhand;
		_isSummoned = false;
        collisionHeight = _cha.getTemplate().collisionHeight;
        collisionRadius = _cha.getTemplate().collisionRadius;
        if (Config.SERVER_SIDE_NPC_NAME)
        	_name = _cha.getName();
	}
	
	public NpcInfo(L2Summon cha, L2Character attacker)
	{
		_cha = cha;
		_npcId = cha.getTemplate().npcId;
		_isAttackable = cha.isAutoAttackable(attacker); //(cha.getKarma() > 0);
		_rhand = 0;
		_lhand = 0;
		_isSummoned = cha.isShowSummonAnimation();
        collisionHeight = _cha.getTemplate().collisionHeight;
        collisionRadius = _cha.getTemplate().collisionRadius;
        if (Config.SERVER_SIDE_NPC_NAME || cha instanceof L2PetInstance)
        	_name = _cha.getName();
	}
	
	final void runImpl()
	{
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
	
	final void writeImpl()
	{
		writeC(0x16);
		writeD(_cha.getObjectId());
		writeD(_npcId+1000000);  // npctype id
		writeD(_isAttackable ? 1 : 0); 
		writeD(_x);
		writeD(_y);
		writeD(_z);
		writeD(_heading);
		writeD(0x00);
		writeD(_mAtkSpd);
		writeD(_pAtkSpd);
		writeD(_runSpd);
		writeD(_walkSpd);
		writeD(_swimRunSpd/*0x32*/);  // swimspeed
		writeD(_swimWalkSpd/*0x32*/);  // swimspeed
		writeD(_flRunSpd);
		writeD(_flWalkSpd);
		writeD(_flyRunSpd);
		writeD(_flyWalkSpd);
		writeF(1/*_cha.getProperMultiplier()*/);
		writeF(1/*_cha.getAttackSpeedMultiplier()*/);
		writeF(collisionRadius);
		writeF(collisionHeight);
		writeD(_rhand); // right hand weapon
		writeD(0);
		writeD(_lhand); // left hand weapon
		writeC(1);	// name above char 1=true ... ??
		writeC(_cha.isRunning() ? 1 : 0);
		writeC(_cha.isInCombat() ? 1 : 0);
		writeC(_cha.isAlikeDead() ? 1 : 0);
		writeC(_isSummoned ? 2 : 0); // invisible ?? 0=false  1=true   2=summoned (only works if model has a summon animation)
		writeS(_name);
		writeS(_cha.getTitle());
		writeD(0);
		writeD(0);
		writeD(0000);  // hmm karma ??

		if (getClient().getRevision() > 560)
		{
			writeD(_cha.getAbnormalEffect());  // C2
		}
		else
		{
			writeH(_cha.getAbnormalEffect());  // C2
			writeH(0x00);  // C2
		}
		writeD(0000);  // C2
		writeD(0000);  // C2
		writeD(0000);  // C2
		writeD(0000);  // C2
		writeC(0000);  // C2
		if (getClient().getRevision() > 560)
		{
    	    writeC(0x00);  // C3  team circle 1-blue, 2-red
	        writeF(0x00);  // C4
    	    writeF(0x00);  // C4
        	writeD(0x00);  // C4
		}	
	}
	
	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.serverpackets.ServerBasePacket#getType()
	 */
	public String getType()
	{
		return _S__22_NPCINFO + ((_cha!=null) ? " about " + _cha : "");
	}
}
