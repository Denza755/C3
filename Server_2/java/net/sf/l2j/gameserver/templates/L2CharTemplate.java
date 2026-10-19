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
package net.sf.l2j.gameserver.templates;

/**
 * This class ...
 * 
 * @version $Revision: 1.2.4.6 $ $Date: 2005/04/02 15:57:51 $
 */
public class L2CharTemplate
{
	// BaseStats
	public final int baseSTR;
	public final int baseCON;
	public final int baseDEX;
	public final int baseINT;
	public final int baseWIT;
	public final int baseMEN;
	public final float baseHpMax;
	public final float baseMpMax;
	public final float baseHpReg;
	public final float baseMpReg;
	public final int basePAtk;
	public final int baseMAtk;
	public final int basePDef;
	public final int baseMDef;
	public final int basePAtkSpd;
	public final int baseMAtkSpd;
	public final float baseMReuseRate;
	public final int baseShldDef;
	public final int baseAtkRange;
	public final int baseShldRate;
	public final int baseCritRate;
	public final int baseAttackCancel;
	public final int baseRunSpd;
	// SpecialStats
	public final int baseBreath;
	public final int baseAggression;
	public final int baseBleed;
	public final int basePoison;
	public final int baseStun;
	public final int baseRoot;
	public final int baseMovement;
	public final int baseConfusion;
	public final int baseSleep;
	public final int baseFire;
	public final int baseWind;
	public final int baseWater;
	public final int baseEarth;
	public final int baseAggressionRes;
	public final int baseBleedRes;
	public final int basePoisonRes;
	public final int baseStunRes;
	public final int baseRootRes;
	public final int baseMovementRes;
	public final int baseConfusionRes;
	public final int baseSleepRes;
	public final int baseFireRes;
	public final int baseWindRes;
	public final int baseWaterRes;
	public final int baseEarthRes;
	
	public final int collisionRadius;   
	public final int collisionHeight;
	
	public L2CharTemplate(StatsSet set)
	{
		// Base stats
		baseSTR            = set.getIntegr("baseSTR");
		baseCON            = set.getIntegr("baseCON");
		baseDEX            = set.getIntegr("baseDEX");
		baseINT            = set.getIntegr("baseINT");
		baseWIT            = set.getIntegr("baseWIT");
		baseMEN            = set.getIntegr("baseMEN");
		baseHpMax          = set.getFloat ("baseHpMax");
		baseMpMax          = set.getFloat ("baseMpMax");
		baseHpReg          = set.getFloat ("baseHpReg");
		baseMpReg          = set.getFloat ("baseMpReg");
		basePAtk           = set.getIntegr("basePAtk");
		baseMAtk           = set.getIntegr("baseMAtk");
		basePDef           = set.getIntegr("basePDef");
		baseMDef           = set.getIntegr("baseMDef");
		basePAtkSpd        = set.getIntegr("basePAtkSpd");
		baseMAtkSpd        = set.getIntegr("baseMAtkSpd");
		baseMReuseRate     = set.getFloat ("baseMReuseDelay", 1.f);
		baseShldDef        = set.getIntegr("baseShldDef");
		baseAtkRange       = set.getIntegr("baseAtkRange");
		baseShldRate       = set.getIntegr("baseShldRate");
		baseCritRate       = set.getIntegr("baseCritRate");
		baseAttackCancel   = set.getIntegr("baseAttackCancel", 50); // 50% to break attack
		baseRunSpd         = set.getIntegr("baseRunSpd");
		// SpecialStats
		baseBreath         = set.getIntegr("baseBreath",         100);
		baseAggression     = set.getIntegr("baseAggression",     0);
		baseBleed          = set.getIntegr("baseBleed",          0);
		basePoison         = set.getIntegr("basePoison",         0);
		baseStun           = set.getIntegr("baseStun",           0);
		baseRoot           = set.getIntegr("baseRoot",           0);
		baseMovement       = set.getIntegr("baseMovement",       0);
		baseConfusion      = set.getIntegr("baseConfusion",      0);
		baseSleep          = set.getIntegr("baseSleep",          0);
		baseFire           = set.getIntegr("baseFire",           0);
		baseWind           = set.getIntegr("baseWind",           0);
		baseWater          = set.getIntegr("baseWater",          0);
		baseEarth          = set.getIntegr("baseEarth",          0);
		baseAggressionRes  = set.getIntegr("baseAaggressionRes", 0);
		baseBleedRes       = set.getIntegr("baseBleedRes",       0);
		basePoisonRes      = set.getIntegr("basePoisonRes",      0);
		baseStunRes        = set.getIntegr("baseStunRes",        0);
		baseRootRes        = set.getIntegr("baseRootRes",        0);
		baseMovementRes    = set.getIntegr("baseMovementRes",    0);
		baseConfusionRes   = set.getIntegr("baseConfusionRes",   0);
		baseSleepRes       = set.getIntegr("baseSleepRes",       0);
		baseFireRes        = set.getIntegr("baseFireRes",        0);
		baseWindRes        = set.getIntegr("baseWindRes",        0);
		baseWaterRes       = set.getIntegr("baseWaterRes",       0);
		baseEarthRes       = set.getIntegr("baseEarthRes",       0);
		
		// Geometry
		collisionRadius    = set.getIntegr("collision_radius");
		collisionHeight    = set.getIntegr("collision_height");
	}
}
