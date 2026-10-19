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
package net.sf.l2j.gameserver.skills;

import java.util.Random;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2DoorInstance;
import net.sf.l2j.gameserver.model.L2PcInstance;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2Summon;
import net.sf.l2j.gameserver.skills.ConditionPlayerState.CheckPlayerState;
import net.sf.l2j.gameserver.templates.L2PcTemplate;
import net.sf.l2j.gameserver.templates.L2Weapon;
import net.sf.l2j.gameserver.templates.L2WeaponType;

/**
 * Global calculations, can be modified by server admins
 */
public final class Formulas {

	private static final int HP_REGENERATE_PERIOD = 3000; // 3 secs
	
	private static final double[] WITpolynomRuben = new double[]{
		+9.9254810689570600E-11,
		-1.7432156601963700E-08,
		+1.6283114716481700E-06,
		-5.6970835467547200E-05,
		+1.7629733457427100E-03,
		+7.0204771689824400E-03,
		-5.9581455760650400E-01 + 1
	};
	private static final double[] MENpolynomRuben = new double[]{
		+6.5556050620594200E-13,
		-1.8638224683763800E-10,
		+2.0018163622572800E-08,
		-7.2108375368351700E-07,
		+6.3289304637999400E-05,
		+1.0029266341299700E-02,
		-1.0175136814796200E-03 + 1
	};
	private static final double[] INTpolynomRuben = new double[]{
		+1.1470901531361600E-12,
		-2.9553917011707800E-10,
		+3.7089973347674800E-08,
		-1.0695713178072000E-06,
		+1.5208868021921400E-04,
		+1.0050328784329600E-02,
		-4.6076900720778700E-01 + 1
	};
	private static final double[] STRpolynomRuben = new double[]{
		+1.1613512208115100E-13,
		-4.6270526432167100E-11,
		+1.1658934786851800E-08,
		-4.6101001831828200E-07,
		+1.1686709259968900E-04,
		+9.4339883191995000E-03,
		-2.9003757344207000E-01 + 1
	};
	private static final double[] DEXpolynomRuben = new double[]{
		-2.1880234745612400E-13,
		+7.3632610232465600E-11,
		-9.1763881163096500E-09,
		+6.4297949462682400E-07,
		+2.1977057725307600E-05,
		+7.5308782907148200E-03,
		-1.5829464582577600E-01 + 1
	};
	private static final double[] CONpolynomRuben = new double[]{
		-1.7186080812322300E-14,
		+2.4793116095651400E-11,
		-8.6283668624209900E-10,
		+5.6169661837635300E-07,
		+9.1066481005104500E-05,
		+1.2367571569199400E-02,
		-1.6963187489087200E-01 + 1
	};
    protected static final double[] WITbonus = new double[100];
    protected static final double[] MENbonus = new double[100];
    protected static final double[] INTbonus = new double[100];
    protected static final double[] STRbonus = new double[100];
    protected static final double[] DEXbonus = new double[100];
    protected static final double[] CONbonus = new double[100];
	
	private static double polynom(double[] p, int idx) {
		double val = 0;
		for (int i=0; i < p.length; i++) {
			val *= idx;
			val += p[i];
		}
		return val;
	}
	
	static {
		for (int i=0; i < WITbonus.length; i++)
			WITbonus[i] = polynom(WITpolynomRuben, i);
		for (int i=0; i < MENbonus.length; i++)
			MENbonus[i] = polynom(MENpolynomRuben, i);
		for (int i=0; i < INTbonus.length; i++)
			INTbonus[i] = polynom(INTpolynomRuben, i);
		for (int i=0; i < STRbonus.length; i++)
			STRbonus[i] = polynom(STRpolynomRuben, i);
		for (int i=0; i < DEXbonus.length; i++)
			DEXbonus[i] = polynom(DEXpolynomRuben, i);
		for (int i=0; i < CONbonus.length; i++)
			CONbonus[i] = polynom(CONpolynomRuben, i);
	}
	
	static class FuncAddLevel3 extends Func
	{
		static final FuncAddLevel3[] _instancies = new FuncAddLevel3[Stats.NUM_STATS];
		static Func getInstance(Stats stat) {
			int pos = stat.ordinal();
			if (_instancies[pos] == null)
				_instancies[pos] = new FuncAddLevel3(stat);
			return _instancies[pos];
		}
		private FuncAddLevel3(Stats stat)
		{
			super(stat, 0x10, null);
		}
		public void calc(Env env)
		{
			env.value += env._player.getLevel()/3;
		}
	}

	static class FuncMultLevelMod extends Func
	{
		static final FuncMultLevelMod[] _instancies = new FuncMultLevelMod[Stats.NUM_STATS];
		static Func getInstance(Stats stat) {
			int pos = stat.ordinal();
			if (_instancies[pos] == null)
				_instancies[pos] = new FuncMultLevelMod(stat);
			return _instancies[pos];
		}
		private FuncMultLevelMod(Stats stat)
		{
			super(stat, 0x30, null);
		}
		public void calc(Env env)
		{
				env.value *= env._player.getLevelMod();
			}
		}

	static class FuncMAtkMod extends Func
	{
		static final FuncMAtkMod _fma_instance = new FuncMAtkMod();
		static Func getInstance() {
			return _fma_instance;
		}
		private FuncMAtkMod()
		{
			super(Stats.MAGIC_ATTACK, 0x30, null);
		}
		public void calc(Env env)
		{
			//{Wpn*(lvlbn^2)*[(1+INTbn)^2]+Msty}
			L2PcInstance p = (L2PcInstance)env._player;
			double ib = INTbonus[p.getINT()];
			double lvlb = env._player.getLevelMod();
			env.value *= (lvlb * lvlb) * (ib * ib);
		}
	}

	static class FuncMultRegenResting extends Func
	{
		static final FuncMultRegenResting[] _instancies = new FuncMultRegenResting[Stats.NUM_STATS];
		static Func getInstance(Stats stat) {
			int pos = stat.ordinal();
			if (_instancies[pos] == null)
				_instancies[pos] = new FuncMultRegenResting(stat);
			return _instancies[pos];
		}
		private FuncMultRegenResting(Stats stat)
		{
			super(stat, 0x20, null);
			setCondition(new ConditionPlayerState(CheckPlayerState.RESTING, true));
		}
		public void calc(Env env)
		{
			if (!_cond.test(env))
				return;
			env.value *= 1.45;
		}
	}

	static class FuncPAtkMod extends Func
	{
		static final FuncPAtkMod _fpa_instance = new FuncPAtkMod();
		static Func getInstance() {
			return _fpa_instance;
		}
		private FuncPAtkMod()
		{
			super(Stats.POWER_ATTACK, 0x30, null);
		}
		public void calc(Env env)
		{
			L2PcInstance p = (L2PcInstance)env._player;
			env.value *= STRbonus[p.getSTR()];
		}
	}

	static class FuncBowAtkRange extends Func
	{
		private static final FuncBowAtkRange _fbar_instance = new FuncBowAtkRange();
		static Func getInstance() {
			return _fbar_instance;
		}
		private FuncBowAtkRange()
		{
			super(Stats.POWER_ATTACK_RANGE, 0x10, null);
			setCondition(new ConditionUsingItemType(L2WeaponType.BOW.mask()));
		}
		public void calc(Env env)
		{
			if (!_cond.test(env))
				return;
			env.value += 450;
		}
	}

	static class FuncAtkAccuracy extends Func
	{
		static final FuncAtkAccuracy _faa_instance = new FuncAtkAccuracy();
		static Func getInstance() {
			return _faa_instance;
		}
		private FuncAtkAccuracy()
		{
			super(Stats.ACCURACY_COMBAT, 0x10, null);
		}
		public void calc(Env env)
		{
			L2Character p = env._player;
			//[Square(DEX)]*6 + lvl + weapon hitbonus;
			env.value += Math.sqrt(p.getDEX())*6;
			env.value += p.getLevel();
		}
	}

	static class FuncAtkEvasion extends Func
	{
		static final FuncAtkEvasion _fae_instance = new FuncAtkEvasion();
		static Func getInstance() {
			return _fae_instance;
		}
		private FuncAtkEvasion()
		{
			super(Stats.EVASION_RATE, 0x10, null);
		}
		public void calc(Env env)
		{
			L2Character p = env._player;
			//[Square(DEX)]*6 + lvl;
			env.value += Math.sqrt(p.getDEX())*6;
			env.value += p.getLevel();
		}
	}

	static class FuncAtkCritical extends Func
	{
		static final FuncAtkCritical _fac_instance = new FuncAtkCritical();
		static Func getInstance() {
			return _fac_instance;
		}
		private FuncAtkCritical()
		{
			super(Stats.CRITICAL_RATE, 0x30, null);
		}
		public void calc(Env env)
		{
			L2Character p = env._player;
			env.value *= DEXbonus[p.getDEX()];
			env.value *= 10;
		}
	}

	static class FuncMoveSpeed extends Func
	{
		static final FuncMoveSpeed _fms_instance = new FuncMoveSpeed();
		static Func getInstance() {
			return _fms_instance;
		}
		private FuncMoveSpeed()
		{
			super(Stats.RUN_SPEED, 0x30, null);
		}
		public void calc(Env env)
		{
			L2PcInstance p = (L2PcInstance)env._player;
			env.value *= DEXbonus[p.getDEX()];
		}
	}

	static class FuncPAtkSpeed extends Func
	{
		static final FuncPAtkSpeed _fas_instance = new FuncPAtkSpeed();
		static Func getInstance() {
			return _fas_instance;
		}
		private FuncPAtkSpeed()
		{
			super(Stats.POWER_ATTACK_SPEED, 0x20, null);
		}
		public void calc(Env env)
		{
			L2PcInstance p = (L2PcInstance)env._player;
			env.value *= DEXbonus[p.getDEX()];
		}
	}

	static class FuncMAtkSpeed extends Func
	{
		static final FuncMAtkSpeed _fas_instance = new FuncMAtkSpeed();
		static Func getInstance() {
			return _fas_instance;
		}
		private FuncMAtkSpeed()
		{
			super(Stats.MAGIC_ATTACK_SPEED, 0x20, null);
		}
		public void calc(Env env)
		{
			L2PcInstance p = (L2PcInstance)env._player;
			env.value *= WITbonus[p.getWIT()];
		}
	}

	static class FuncMaxHpAdd extends Func
	{
		static final FuncMaxHpAdd _fmha_instance = new FuncMaxHpAdd();
		static Func getInstance() {
			return _fmha_instance;
		}
		private FuncMaxHpAdd()
		{
			super(Stats.MAX_HP, 0x10, null);
		}
		public void calc(Env env)
		{
			L2PcTemplate t = (L2PcTemplate)env._player.getTemplate();
			int lvl = env._player.getLevel() - t.classBaseLevel;
			double hpmod = t.lvlHpMod * lvl;
			double hpmax = (t.lvlHpAdd + hpmod) * lvl;
			double hpmin = (t.lvlHpAdd * lvl) + hpmod;
			env.value += (hpmax + hpmin) / 2;
		}
	}
	static class FuncMaxHpMul extends Func
	{
		static final FuncMaxHpMul _fmhm_instance = new FuncMaxHpMul();
		static Func getInstance() {
			return _fmhm_instance;
		}
		private FuncMaxHpMul()
		{
			super(Stats.MAX_HP, 0x20, null);
		}
		public void calc(Env env)
		{
			L2PcInstance p = (L2PcInstance)env._player;
			env.value *= CONbonus[p.getCON()];
		}
	}
	static class FuncMaxMpAdd extends Func
	{
		static final FuncMaxMpAdd _fmma_instance = new FuncMaxMpAdd();
		static Func getInstance() {
			return _fmma_instance;
		}
		private FuncMaxMpAdd()
		{
			super(Stats.MAX_MP, 0x10, null);
		}
		public void calc(Env env)
		{
			L2PcTemplate t = (L2PcTemplate)env._player.getTemplate();
			int lvl = env._player.getLevel() - t.classBaseLevel;
			double mpmod = t.lvlMpMod * lvl;
			double mpmax = (t.lvlMpAdd + mpmod) * lvl;
			double mpmin = (t.lvlMpAdd * lvl) + mpmod;
			env.value += (mpmax + mpmin) / 2;
		}
	}
	static class FuncMaxMpMul extends Func
	{
		static final FuncMaxMpMul _fmmm_instance = new FuncMaxMpMul();
		static Func getInstance() {
			return _fmmm_instance;
		}
		private FuncMaxMpMul()
		{
			super(Stats.MAX_MP, 0x20, null);
		}
		public void calc(Env env)
		{
			L2PcInstance p = (L2PcInstance)env._player;
			env.value *= MENbonus[p.getMEN()];
		}
	}

	private static final Formulas _instance = new Formulas();

	private static final Random _rnd = new Random();
	
	public static Formulas getInstance()
	{
		return _instance;
	}
	
	private Formulas() {}
	
	public int getRegeneratePeriod(L2Character cha) {
		if (cha instanceof L2DoorInstance)
			return HP_REGENERATE_PERIOD * 100; // 5 mins
		return HP_REGENERATE_PERIOD;
	}
	
	public Calculator[] getStdNPCCalculators()
	{
		Calculator[] std = new Calculator[Stats.NUM_STATS];
		std[Stats.ACCURACY_COMBAT.ordinal()] = new Calculator();
		std[Stats.ACCURACY_COMBAT.ordinal()].addFunc(FuncAtkAccuracy.getInstance());
		std[Stats.EVASION_RATE.ordinal()] = new Calculator();
		std[Stats.EVASION_RATE.ordinal()].addFunc(FuncAtkEvasion.getInstance());
		return std;
	}
	public void addFuncsToNewCharacter(L2Character cha)
	{
		if (cha instanceof L2PcInstance)
		{
			cha.addStatFunc(FuncMaxHpAdd.getInstance());
			cha.addStatFunc(FuncMaxHpMul.getInstance());
			cha.addStatFunc(FuncMaxMpAdd.getInstance());
			cha.addStatFunc(FuncMaxMpMul.getInstance());
			cha.addStatFunc(FuncMultRegenResting.getInstance(Stats.REGENERATE_HP_RATE));
			cha.addStatFunc(FuncMultRegenResting.getInstance(Stats.REGENERATE_MP_RATE));
			cha.addStatFunc(FuncBowAtkRange.getInstance());
			cha.addStatFunc(FuncMultLevelMod.getInstance(Stats.POWER_ATTACK));
			cha.addStatFunc(FuncMultLevelMod.getInstance(Stats.POWER_DEFENCE));
			cha.addStatFunc(FuncMultLevelMod.getInstance(Stats.MAGIC_DEFENCE));
			cha.addStatFunc(FuncPAtkMod.getInstance());
			cha.addStatFunc(FuncMAtkMod.getInstance());
			cha.addStatFunc(FuncAtkCritical.getInstance());
			cha.addStatFunc(FuncAtkAccuracy.getInstance());
			cha.addStatFunc(FuncAtkEvasion.getInstance());
			cha.addStatFunc(FuncPAtkSpeed.getInstance());
			cha.addStatFunc(FuncMAtkSpeed.getInstance());
			cha.addStatFunc(FuncMoveSpeed.getInstance());
		}
		else if (cha instanceof L2Summon) {
			cha.addStatFunc(FuncMultRegenResting.getInstance(Stats.REGENERATE_HP_RATE));
			cha.addStatFunc(FuncMultRegenResting.getInstance(Stats.REGENERATE_MP_RATE));
			cha.addStatFunc(FuncAtkCritical.getInstance());
			cha.addStatFunc(FuncAtkAccuracy.getInstance());
			cha.addStatFunc(FuncAtkEvasion.getInstance());
		}
	}
	
	public final double calcHpRegen(L2Character cha)
	{
		double init = cha.getMaxHp()*cha.getTemplate().baseHpReg;
		return cha.calcStat(Stats.REGENERATE_HP_RATE, init, null, null);
	}
	
	public final double calcMpRegen(L2Character cha)
	{
		double init = cha.getMaxMp()*cha.getTemplate().baseMpReg;
		return cha.calcStat(Stats.REGENERATE_MP_RATE, init, null, null);
	}
	
	public final double calcCpRegen(L2Character cha)
	{
		double init = cha.getMaxHp()*cha.getTemplate().baseHpReg;
		return cha.calcStat(Stats.REGENERATE_HP_RATE, init, null, null);
	}
	
	/** Calculated damage caused by attack of attacker on target,
	 * called separatly for each weapon, if dual-weapon is used.
	 * 
	 * @param attacker player or NPC that makes attack
	 * @param target player or NPC, target of attack
	 * @param miss one of ATTACK_XXX constants
	 * @param crit if the attack have critical success
	 * @param dual if dual weapon is used
	 * @param ss if weapon item was changes by soulshot
	 * @return damage points
	 */
    public final double calcPhysDam(L2Character attacker, L2Character target,
			L2Skill skill, boolean shld, boolean crit, boolean dual, boolean ss)
    {
    	double damage = attacker.getPAtk(target);
    	double defence = target.getPDef(attacker);
		// defence modifier depending of the attacker weapon
		L2Weapon weapon = attacker.getActiveWeaponItem();
		if (weapon !=null)
		{
			switch (weapon.getItemType())
			{
			case BOW:
				defence=target.calcStat(Stats.BOW_WPN_RES,defence,target,null);
				break;
			case BLUNT:
				defence=target.calcStat(Stats.BLUNT_WPN_RES,defence,target,null);
				break;
			case DAGGER:
				defence=target.calcStat(Stats.DAGGER_WPN_RES,defence,target,null);
				break;
			case DUAL:
				defence=target.calcStat(Stats.DUAL_WPN_RES,defence,target,null);
				break;
			case DUALFIST:
				defence=target.calcStat(Stats.DUALFIST_WPN_RES,defence,target,null);
				break;
			case ETC:
				defence=target.calcStat(Stats.ETC_WPN_RES,defence,target,null);
				break;
			case FIST:
				defence=target.calcStat(Stats.FIST_WPN_RES,defence,target,null);
				break;
			case POLE:
				defence=target.calcStat(Stats.POLE_WPN_RES,defence,target,null);
				break;
			case SWORD:
				defence=target.calcStat(Stats.SWORD_WPN_RES,defence,target,null);
				break;
				
			}
		}
    	if (skill != null)
    		damage += skill.getPower();
		if (ss)
		{
	    	if (skill != null)
	    		damage *= 1.44;
	    	else
				damage *= 2;
		}
		if (crit)
		damage += attacker.getCriticalDmg(target, damage);
		if(dual)
			damage /= 1.5;
		if (shld && !Config.ALT_GAME_SHIELD_BLOCKS) {
			defence += target.getShldDef();
		} 
		damage = damage * 70 / defence;
    	damage += _rnd.nextDouble()* attacker.getRandomDamage(target);
		if (shld && Config.ALT_GAME_SHIELD_BLOCKS) {
			damage -= target.getShldDef();
			if (damage < 0)
				damage = 0;
		}

		return damage;
    }

	public final double calcMagicDam(L2Character attacker, L2Character target,
			L2Skill skill, boolean ss, boolean bss)
	{
		//[Square(MAtk/MDef)]*SkillPower*10.18
		double mAtk = attacker.getMAtk(target, skill);
		double mDef = target.getMDef(attacker, skill);
		if (bss)
			mAtk *= 4;
		else if (ss)
			mAtk *= 2;
		return 91*skill.getPower()*Math.sqrt(mAtk)/mDef;
	}
	
	/** Returns true in case of critical hit */
	public final boolean calcCrit(double rate)
	{
		int critHit = _rnd.nextInt(1000);
		return rate > critHit;
	}
	
	/** Returns true in case when attack is canceled due to hit */
	public final boolean calcAtkBreak(L2Character cha, double cancel)
	{
		if (Config.ALT_GAME_CANCEL_ALL && cha.isAttackingNow()) {
			return cancel > _rnd.nextInt(100);
		}
		if (Config.ALT_GAME_CANCEL_CAST && cha.isCastingNow())
			return cancel > _rnd.nextInt(100);
		if (Config.ALT_GAME_CANCEL_BOW && cha.isAttackingNow()) {
			L2Weapon wpn = cha.getActiveWeaponItem();
			if (wpn != null && wpn.getItemType() == L2WeaponType.BOW)
				return cancel > _rnd.nextInt(100);
		}
		return false;
	}
	
	/** Calculate delay (in milliseconds) before next attack */
	public final int calcPAtkSpd(L2Character attacker, L2Character target, double rate)
	{
		//will fix freeze at >1000 atkspd, optimum calcation between 0 to 1400
		return (int) (3*Math.pow(rate, 2)/2000 - 4*rate + 2700);
	}
	
	/** Calculate delay (in milliseconds) for skills cast */
	public final int calcMAtkSpd(L2Character attacker, L2Character target, L2Skill skill, double skillTime)
	{
		if (skill.isMagic())
			return (int)(skillTime * 333 / attacker.getMAtkSpd());
		else
			return (int)(skillTime * 333 / attacker.getPAtkSpd());
	}
	
	/** Returns true if hit missed (taget evaded) */
    public boolean calcHitMiss(L2Character attacker, L2Character target) {
		// accuracy+dexterity => probability to hit in percents
		int acc_attacker;
		int evas_target;
		acc_attacker = attacker.getAccuracy();
		evas_target = target.getEvasionRate(attacker);
		int d = 75 + acc_attacker - evas_target;
		return d < _rnd.nextInt(100);
	}
    
	/** Returns true if shield defence successfull */
    public boolean calcShldUse(L2Character attacker, L2Character target) {
		double shldRate = target.calcStat(Stats.SHIELD_RATE, 0, attacker, null);
		return shldRate > _rnd.nextInt(80);
    }
    
    public boolean calcMagicAffected(L2Character actor, L2Character target, L2Skill skill)
    {
    	double defence = 0;
    	if (skill.isActive() && skill.isOffensive())
    		defence = target.getMDef(actor, skill);
    	double attack = 2 * actor.getMAtk(target, skill);
    	double d = attack - defence;
    	d /= attack + defence;
		d += 0.5 * _rnd.nextGaussian();
		return d > 0;
    }
}
