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

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.instancemanager.SiegeManager;
import net.sf.l2j.gameserver.lib.Rnd;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2SiegeClan;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2Skill.SkillType;
import net.sf.l2j.gameserver.model.L2Summon;
import net.sf.l2j.gameserver.model.actor.instance.*;
import net.sf.l2j.gameserver.model.entity.SevenSigns;
import net.sf.l2j.gameserver.model.entity.SevenSignsFestival;
import net.sf.l2j.gameserver.model.entity.Siege;
import net.sf.l2j.gameserver.skills.conditions.ConditionPlayerState.CheckPlayerState;
import net.sf.l2j.gameserver.skills.conditions.ConditionPlayerState;
import net.sf.l2j.gameserver.skills.conditions.ConditionUsingItemType;
import net.sf.l2j.gameserver.skills.funcs.Func;
import net.sf.l2j.gameserver.tables.SkillTable;
import net.sf.l2j.gameserver.templates.L2PcTemplate;
import net.sf.l2j.gameserver.templates.L2Weapon;
import net.sf.l2j.gameserver.templates.L2WeaponType;
import net.sf.l2j.gameserver.util.Util;

import java.util.logging.Logger;

/**
 * Global calculations, can be modified by server admins
 */
public final class Formulas {

	/** Regen Task period */
    protected static final Logger _log = Logger.getLogger(L2Character.class.getName());
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
    
    public static int MAX_STAT_VALUE = 100;
    
    protected static final double[] WITbonus = new double[MAX_STAT_VALUE];
    protected static final double[] MENbonus = new double[MAX_STAT_VALUE];
    protected static final double[] INTbonus = new double[MAX_STAT_VALUE];
    protected static final double[] STRbonus = new double[MAX_STAT_VALUE];
    protected static final double[] DEXbonus = new double[MAX_STAT_VALUE];
    protected static final double[] CONbonus = new double[MAX_STAT_VALUE];

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
			env.value *= (lvlb * lvlb) * (ib * ib) * 1.17;
		}
	}

	static class FuncMultRegenResting extends Func
	{
		static final FuncMultRegenResting[] _instancies = new FuncMultRegenResting[Stats.NUM_STATS];


		/**
		 * Return the Func object corresponding to the state concerned.<BR><BR>
		 */
		static Func getInstance(Stats stat)
		{
			int pos = stat.ordinal();

			if (_instancies[pos] == null)
				_instancies[pos] = new FuncMultRegenResting(stat);

			return _instancies[pos];
		}

		/**
		 * Constructor of the FuncMultRegenResting.<BR><BR>
		 */
		private FuncMultRegenResting(Stats stat)
		{
			super(stat, 0x20, null);
			setCondition(new ConditionPlayerState(CheckPlayerState.RESTING, true));
		}

		/**
		 * Calculate the modifier of the state concerned.<BR><BR>
		 */
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
			env.value *= STRbonus[p.getSTR()]*STRbonus[p.getSTR()]/1.25;
			double lvlMod = env._player.getLevelMod();
			//0 added at lvl 1, ~75 added at lvl 75
			env.value += (STRbonus[p.getSTR()]*2)*40*(lvlMod-9.0/10.0);
		}
	}

	static class FuncMDefMod extends Func
	{
		static final FuncMDefMod _fmm_instance = new FuncMDefMod();
		static Func getInstance() {
			return _fmm_instance;
		}
		private FuncMDefMod()
		{
			super(Stats.MAGIC_DEFENCE, 0x20, null);
		}
		public void calc(Env env)
		{
			L2PcInstance p = (L2PcInstance)env._player;
			env.value *= MENbonus[p.getMEN()];
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


	static class FuncHennaSTR extends Func
	{
		static final FuncHennaSTR _fh_instance = new FuncHennaSTR();
		static Func getInstance() {
			return _fh_instance;
		}
		private FuncHennaSTR()
		{
			super(Stats.STAT_STR, 0x10, null);
		}
		public void calc(Env env)
		{
//			L2PcTemplate t = (L2PcTemplate)env._player.getTemplate();
			L2PcInstance pc = (L2PcInstance)env._player;
			if (pc!=null)
				env.value += pc.getHennaStatSTR();
		}
	}
	static class FuncHennaDEX extends Func
	{
		static final FuncHennaDEX _fh_instance = new FuncHennaDEX();
		static Func getInstance() {
			return _fh_instance;
		}
		private FuncHennaDEX()
		{
			super(Stats.STAT_DEX, 0x10, null);
		}
		public void calc(Env env)
		{
//			L2PcTemplate t = (L2PcTemplate)env._player.getTemplate();
			L2PcInstance pc = (L2PcInstance)env._player;
			if (pc!=null)
				env.value += pc.getHennaStatDEX();
		}
	}
	static class FuncHennaINT extends Func
	{
		static final FuncHennaINT _fh_instance = new FuncHennaINT();
		static Func getInstance() {
			return _fh_instance;
		}
		private FuncHennaINT()
		{
			super(Stats.STAT_INT, 0x10, null);
		}
		public void calc(Env env)
		{
//			L2PcTemplate t = (L2PcTemplate)env._player.getTemplate();
			L2PcInstance pc = (L2PcInstance)env._player;
			if (pc!=null)
				env.value += pc.getHennaStatINT();
		}
	}
	static class FuncHennaMEN extends Func
	{
		static final FuncHennaMEN _fh_instance = new FuncHennaMEN();
		static Func getInstance() {
			return _fh_instance;
		}
		private FuncHennaMEN()
		{
			super(Stats.STAT_MEN, 0x10, null);
		}
		public void calc(Env env)
		{
//			L2PcTemplate t = (L2PcTemplate)env._player.getTemplate();
			L2PcInstance pc = (L2PcInstance)env._player;
			if (pc!=null)
				env.value += pc.getHennaStatMEN();
		}
	}
	static class FuncHennaCON extends Func
	{
		static final FuncHennaCON _fh_instance = new FuncHennaCON();
		static Func getInstance() {
			return _fh_instance;
		}
		private FuncHennaCON()
		{
			super(Stats.STAT_CON, 0x10, null);
		}
		public void calc(Env env)
		{
//			L2PcTemplate t = (L2PcTemplate)env._player.getTemplate();
			L2PcInstance pc = (L2PcInstance)env._player;
			if (pc!=null)
				env.value += pc.getHennaStatCON();
		}
	}
	static class FuncHennaWIT extends Func
	{
		static final FuncHennaWIT _fh_instance = new FuncHennaWIT();
		static Func getInstance() {
			return _fh_instance;
		}
		private FuncHennaWIT()
		{
			super(Stats.STAT_WIT, 0x10, null);
		}
		public void calc(Env env)
		{
//			L2PcTemplate t = (L2PcTemplate)env._player.getTemplate();
			L2PcInstance pc = (L2PcInstance)env._player;
			if (pc!=null)
				env.value += pc.getHennaStatWIT();
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
    static class FuncMaxCpAdd extends Func
    {
        static final FuncMaxCpAdd _fmca_instance = new FuncMaxCpAdd();
        static Func getInstance() {
            return _fmca_instance;
        }
        private FuncMaxCpAdd()
        {
            super(Stats.MAX_CP, 0x10, null);
        }
        public void calc(Env env)
        {
            L2PcTemplate t = (L2PcTemplate)env._player.getTemplate();
            int lvl = env._player.getLevel() - t.classBaseLevel;
            double cpmod = t.lvlCpMod * lvl;
            double cpmax = (t.lvlCpAdd + cpmod) * lvl;
            double cpmin = (t.lvlCpAdd * lvl) + cpmod;
            env.value += (cpmax + cpmin) / 2;
        }
    }
    static class FuncMaxCpMul extends Func
    {
        static final FuncMaxCpMul _fmcm_instance = new FuncMaxCpMul();
        static Func getInstance() {
            return _fmcm_instance;
        }
        private FuncMaxCpMul()
        {
            super(Stats.MAX_CP, 0x20, null);
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

	public static Formulas getInstance()
	{
		return _instance;
	}

	private Formulas() {}


	/**
	 * Return the period between 2 regenerations task (3s for L2Character, 5 min for L2DoorInstance).<BR><BR>
	 */
	public int getRegeneratePeriod(L2Character cha)
	{
		if (cha instanceof L2DoorInstance)
			return HP_REGENERATE_PERIOD * 100; // 5 mins

		return HP_REGENERATE_PERIOD; // 3s
	}


	/**
	 * Return the standard NPC Calculator set containing ACCURACY_COMBAT and EVASION_RATE.<BR><BR>
	 *
	 * <B><U> Concept</U> :</B><BR><BR>
	 * A calculator is created to manage and dynamically calculate the effect of a character property (ex : MAX_HP, REGENERATE_HP_RATE...).
     * In fact, each calculator is a table of Func object in which each Func represents a mathematic function : <BR><BR>
	 *
	 * FuncAtkAccuracy -> Math.sqrt(_player.getDEX())*6+_player.getLevel()<BR><BR>
	 *
	 * To reduce cache memory use, L2NPCInstances who don't have skills share the same Calculator set called <B>NPC_STD_CALCULATOR</B>.<BR><BR>
	 *
	 */
	public Calculator[] getStdNPCCalculators()
	{
		Calculator[] std = new Calculator[Stats.NUM_STATS];

	    // Add the FuncAtkAccuracy to the Standard Calculator of ACCURACY_COMBAT
		std[Stats.ACCURACY_COMBAT.ordinal()] = new Calculator();
		std[Stats.ACCURACY_COMBAT.ordinal()].addFunc(FuncAtkAccuracy.getInstance());

	    // Add the FuncAtkEvasion to the Standard Calculator of EVASION_RATE
		std[Stats.EVASION_RATE.ordinal()] = new Calculator();
		std[Stats.EVASION_RATE.ordinal()].addFunc(FuncAtkEvasion.getInstance());

		return std;
	}


	/**
	 * Add basics Func objects to L2PcInstance and L2Summon.<BR><BR>
	 *
	 * <B><U> Concept</U> :</B><BR><BR>
	 * A calculator is created to manage and dynamically calculate the effect of a character property (ex : MAX_HP, REGENERATE_HP_RATE...).
     * In fact, each calculator is a table of Func object in which each Func represents a mathematic function : <BR><BR>
	 *
	 * FuncAtkAccuracy -> Math.sqrt(_player.getDEX())*6+_player.getLevel()<BR><BR>
	 *
	 * @param cha L2PcInstance or L2Summon that must obtain basic Func objects
	 */
	public void addFuncsToNewCharacter(L2Character cha)
	{
		if (cha instanceof L2PcInstance)
		{
			cha.addStatFunc(FuncMaxHpAdd.getInstance());
			cha.addStatFunc(FuncMaxHpMul.getInstance());
            cha.addStatFunc(FuncMaxCpAdd.getInstance());
            cha.addStatFunc(FuncMaxCpMul.getInstance());
			cha.addStatFunc(FuncMaxMpAdd.getInstance());
			cha.addStatFunc(FuncMaxMpMul.getInstance());
			cha.addStatFunc(FuncMultRegenResting.getInstance(Stats.REGENERATE_HP_RATE));
            cha.addStatFunc(FuncMultRegenResting.getInstance(Stats.REGENERATE_CP_RATE));
			cha.addStatFunc(FuncMultRegenResting.getInstance(Stats.REGENERATE_MP_RATE));
			cha.addStatFunc(FuncBowAtkRange.getInstance());
			cha.addStatFunc(FuncMultLevelMod.getInstance(Stats.POWER_ATTACK));
			cha.addStatFunc(FuncMultLevelMod.getInstance(Stats.POWER_DEFENCE));
			cha.addStatFunc(FuncMultLevelMod.getInstance(Stats.MAGIC_DEFENCE));
			cha.addStatFunc(FuncPAtkMod.getInstance());
			cha.addStatFunc(FuncMAtkMod.getInstance());
			cha.addStatFunc(FuncMDefMod.getInstance());
			cha.addStatFunc(FuncAtkCritical.getInstance());
			cha.addStatFunc(FuncAtkAccuracy.getInstance());
			cha.addStatFunc(FuncAtkEvasion.getInstance());
			cha.addStatFunc(FuncPAtkSpeed.getInstance());
			cha.addStatFunc(FuncMAtkSpeed.getInstance());
			cha.addStatFunc(FuncMoveSpeed.getInstance());

			cha.addStatFunc(FuncHennaSTR.getInstance());
			cha.addStatFunc(FuncHennaDEX.getInstance());
			cha.addStatFunc(FuncHennaINT.getInstance());
			cha.addStatFunc(FuncHennaMEN.getInstance());
			cha.addStatFunc(FuncHennaCON.getInstance());
			cha.addStatFunc(FuncHennaWIT.getInstance());
		}
		else if (cha instanceof L2Summon)
		{
			cha.addStatFunc(FuncMultRegenResting.getInstance(Stats.REGENERATE_HP_RATE));
			cha.addStatFunc(FuncMultRegenResting.getInstance(Stats.REGENERATE_MP_RATE));
			cha.addStatFunc(FuncAtkCritical.getInstance());
			cha.addStatFunc(FuncAtkAccuracy.getInstance());
			cha.addStatFunc(FuncAtkEvasion.getInstance());
		}

	}

	/**
	 * Calculate the HP regen rate (base + modifiers).<BR><BR>
	 */
	public final double calcHpRegen(L2Character cha)
	{
	    double hpRegenMultiplier = Config.HP_REGEN_MULTIPLIER;
        
        if (cha instanceof L2PcInstance)
        {
            L2PcInstance activeChar = (L2PcInstance)cha;
            
            if (SevenSignsFestival.getInstance().isFestivalInProgress() && activeChar.isFestivalParticipant()) 
                hpRegenMultiplier -= calcFestivalRegenModifier(activeChar);
            else
            {
               double siegeModifier = this.calcSiegeRegenModifer(activeChar);
               if (siegeModifier > 0) hpRegenMultiplier *= siegeModifier;
            }

            if (activeChar.isSitting()) 
                hpRegenMultiplier *= 2;
            else if (!activeChar.isRunning()) 
                hpRegenMultiplier *= 1.5;
            
        }
        else if (cha.isRaid())
            hpRegenMultiplier *= Config.RAID_HP_REGEN_MULTIPLIER;
        
		//double init = cha.getMaxHp()*cha.getTemplate().baseHpReg * (Config.HP_REGEN_MULTIPLIER / 100);
        double init = ((0.1 * cha.getLevel() + cha.getTemplate().baseHpReg) * (1 + CONbonus[cha.getCON()])) * (hpRegenMultiplier / 100);
		if (init < 1)
		    init = 1;

		return cha.calcStat(Stats.REGENERATE_HP_RATE, init, null, null);
	}

	/**
	 * Calculate the MP regen rate (base + modifiers).<BR><BR>
	 */
	public final double calcMpRegen(L2Character cha)
	{
        double mpRegenMultiplier = Config.MP_REGEN_MULTIPLIER;
        
        if (cha instanceof L2PcInstance)
        {
            L2PcInstance activeChar = (L2PcInstance)cha;
            
            if (SevenSignsFestival.getInstance().isFestivalInProgress() && activeChar.isFestivalParticipant()) 
                mpRegenMultiplier -= calcFestivalRegenModifier(activeChar);

            if (activeChar.isSitting()) 
                mpRegenMultiplier *= 2;
            
            else if (!activeChar.isRunning()) 
                mpRegenMultiplier *= 1.5;
        }
        
		if (cha.isRaid()) mpRegenMultiplier *= Config.RAID_MP_REGEN_MULTIPLIER;
		//double init = cha.getMaxMp()*cha.getTemplate().baseMpReg * (Config.MP_REGEN_MULTIPLIER / 100);
        double init = ((0.03 * cha.getLevel() + cha.getTemplate().baseMpReg) * (1 + MENbonus[cha.getMEN()])) * (mpRegenMultiplier / 100);
		return cha.calcStat(Stats.REGENERATE_MP_RATE, init, null, null);
	}

	/**
	 * Calculate the CP regen rate (base + modifiers).<BR><BR>
	 */
	public final double calcCpRegen(L2Character cha)
	{
        //double init = cha.getMaxCp()*cha.getTemplate().baseCpReg * Config.CP_REGEN_MULTIPLIER / 100;
        double init = ((0.08 * cha.getLevel() + cha.getTemplate().baseCpReg) * (0.6 + CONbonus[cha.getCON()])) * (Config.CP_REGEN_MULTIPLIER / 100);
        return cha.calcStat(Stats.REGENERATE_CP_RATE, init, null, null);
	}
    
    public final double calcFestivalRegenModifier(L2PcInstance activeChar)
    {
        final int[] festivalInfo = SevenSignsFestival.getInstance().getFestivalForPlayer(activeChar);
        final int oracle = festivalInfo[0];
        final int festivalId = festivalInfo[1];
        int[] festivalCenter;
        
        // If the player isn't found in the festival, leave the regen rate as it is.
        if (festivalId < 0) 
            return 0;
        
        // Retrieve the X and Y coords for the center of the festival arena the player is in.
        if (oracle == SevenSigns.CABAL_DAWN)
            festivalCenter = SevenSignsFestival.festivalDawnPlayerSpawns[festivalId];                
        else
            festivalCenter = SevenSignsFestival.festivalDuskPlayerSpawns[festivalId];
        
        // Check the distance between the player and the player spawn point, in the center of the arena.
        double distToCenter = activeChar.getDistance(festivalCenter[0], festivalCenter[1]);
        
        if (Config.DEBUG)
            _log.info("Distance: " + distToCenter + ", RegenMulti: " + (distToCenter * 2.5) / 50);
        
        return (distToCenter * 2.5) / 50; // Maximum Decreased Regen of ~ -65%;
    }

    public final double calcSiegeRegenModifer(L2PcInstance activeChar)
    {
        if (activeChar == null || activeChar.getClan() == null) return 0;

        Siege siege = SiegeManager.getInstance().getSiege(activeChar.getPosition().getX(), activeChar.getPosition().getY());
        if (siege == null || !siege.getIsInProgress()) return 0;

        L2SiegeClan siegeClan = siege.getAttackerClan(activeChar.getClan().getClanId());
        if (siegeClan == null || siegeClan.getFlag().size() == 0 || !Util.checkIfInRange(200, activeChar, siegeClan.getFlag().get(0), true)) return 0;
        
        return 1.5; // If all is true, then modifer will be 50% more
    }
    
	/** Calculated damage caused by ATTACK of attacker on target,
	 * called separatly for each weapon, if dual-weapon is used.
	 *
	 * @param attacker player or NPC that makes ATTACK
	 * @param target player or NPC, target of ATTACK
	 * @param miss one of ATTACK_XXX constants
	 * @param crit if the ATTACK have critical success
	 * @param dual if dual weapon is used
	 * @param ss if weapon item was changes by soulshot
	 * @return damage points
	 */
    public final double calcPhysDam(L2Character attacker, L2Character target,
			L2Skill skill, boolean shld, boolean crit, boolean dual, boolean ss)
    {
    	double damage = attacker.getPAtk(target);
    	double defence = target.getPDef(attacker);
	if (ss)
		damage *= 2;
    	if (skill != null)
	{
	    damage += skill.getPower();
	    //damage += skill.getPower() * 0.7 * attacker.getPAtk(target)/defence;
	}
//	damage = damage * attacker.getSTR()*(1 - attacker.getLevel()/100)/60*1.15;
	if (target instanceof L2NpcInstance)
	{
		    Integer resistPAtk = ((L2NpcInstance)target).getTemplate().getResist(Stats.POWER_DEFENCE);
		    damage*=resistPAtk.doubleValue()/100;
	}
		// defence modifier depending of the attacker weapon
		L2Weapon weapon = attacker.getActiveWeaponItem();
		if (weapon !=null)
		{
			switch (weapon.getItemType())
			{
			case BOW:
				defence=target.calcStat(Stats.BOW_WPN_RES,defence,target,null);
				if (target instanceof L2NpcInstance)
				{
				    Integer resistBow = ((L2NpcInstance)target).getTemplate().getResist(Stats.BOW_WPN_RES);
				    damage*=resistBow.doubleValue()/100;
				}
				break;
			case BLUNT:
				defence=target.calcStat(Stats.BLUNT_WPN_RES,defence,target,null);
				if (target instanceof L2NpcInstance)
				{
				    Integer resistBlunt = ((L2NpcInstance)target).getTemplate().getResist(Stats.BLUNT_WPN_RES);
				    damage*=resistBlunt.doubleValue()/100;
				}
				break;
			case DAGGER:
				defence=target.calcStat(Stats.DAGGER_WPN_RES,defence,target,null);
				if (target instanceof L2NpcInstance)
				{
				    Integer resistDagger = ((L2NpcInstance)target).getTemplate().getResist(Stats.DAGGER_WPN_RES);
				    damage*=resistDagger.doubleValue()/100;
				}
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
			case BIGSWORD: //TODO: have a proper resitance for Big swords
				defence=target.calcStat(Stats.SWORD_WPN_RES,defence,target,null);
				break;
			}
		}
			if (crit)
				damage += attacker.getCriticalDmg(target, damage);    		
		if (shld && !Config.ALT_GAME_SHIELD_BLOCKS) {
			defence += target.getShldDef();
		}
		//if (!(attacker instanceof L2RaidBossInstance) && 
		if ((attacker instanceof L2NpcInstance || attacker instanceof L2SiegeGuardInstance)){
		    if (attacker instanceof L2RaidBossInstance)
			damage *= 1;  // was 10 changed for temp fix
//		    else
//			damage *= 2;
//		if (attacker instanceof L2NpcInstance || attacker instanceof L2SiegeGuardInstance){
		    //damage = damage * attacker.getSTR() * attacker.getAccuracy() * 0.05 / defence;
//		    damage = damage * attacker.getSTR()*  (attacker.getSTR() + attacker.getLevel()) * 0.025 / defence;
//    		    damage += _rnd.nextDouble() * damage / 10 ;
		}
//		else {
    		//if (skill == null)
		    damage = 70 * damage / defence;
		    
    		damage += Rnd.nextDouble() * damage / 10 ;
//    		    damage += _rnd.nextDouble()* attacker.getRandomDamage(target);
//		}
		if (shld && Config.ALT_GAME_SHIELD_BLOCKS) {
			damage -= target.getShldDef();
			if (damage < 0)
				damage = 0;
		}
//        if (target instanceof L2PcInstance && weapon != null && weapon.getItemType() == L2WeaponType.DAGGER && skill != null)
//        {
//        		L2Armor armor = ((L2PcInstance)target).getActiveChestArmorItem();
//            if (armor != null)
//            {
//                if(((L2PcInstance)target).isWearingHeavyArmor())
//                    damage /=2.2;
//                if(((L2PcInstance)target).isWearingLightArmor())
//                    damage /=1.5;               
//            }            
//        }

        if (attacker instanceof L2NpcInstance)
        {
	    int raceId = ((L2NpcInstance)attacker).getTemplate().race;
            //Skill Race : Undead
            if (raceId == 4290)
        	damage /= attacker.getPDefUndead(target);
	}
        if (target instanceof L2NpcInstance)
        {
	    int raceId = ((L2NpcInstance)target).getTemplate().race;
            //Skill Race : Undead
            if (raceId == 4290)
        	damage *= attacker.getPAtkUndead(target);
            //Skill Race : Beast
            if (raceId == 4292)
        	damage *= attacker.getPAtkMonsters(target);
            //Skill Race : Animal
            if (raceId == 4293)
                damage *= attacker.getPAtkAnimals(target);
            //Skill Race : Plant
            if (raceId == 4294)
                damage *= attacker.getPAtkPlants(target);
            //Skill Race : Dragon
            if (raceId == 4299)
                damage *= attacker.getPAtkDragons(target);
            //Skill Race : Bug
            if (raceId == 4301)
                damage *= attacker.getPAtkInsects(target);
        }



        if  (damage > 0 && damage < 1)
        {
            damage  = 1;
        }
        else if (damage < 0)
        {
            damage  = 0;
        }

		return damage;
    }

	public final double calcMagicDam(L2Character attacker, L2Character target,
			L2Skill skill, boolean ss, boolean bss, boolean mcrit)
	{
		double mAtk = attacker.getMAtk(target, skill);
		double mDef = target.getMDef(attacker, skill);
		if (bss)
			mAtk *= 4;
		else if (ss)
			mAtk *= 2;

		//double damage = Math.sqrt(mAtk/mDef)*skill.getPower()*10.18;
        //double damage = 91*Math.sqrt(mAtk)/mDef*skill.getPower(attacker);
        //double damage = 5*Math.sqrt(mAtk/mDef)*skill.getPower(attacker);
        double damage = 40/Math.pow(mAtk*mDef,1./6)*Math.sqrt(mAtk/mDef)*skill.getPower(attacker);
        if(attacker instanceof L2PcInstance && target instanceof L2PcInstance) damage *= 0.9; // PvP modifier (-10%)
		if(mcrit)
		{
			damage *= 2;
			if (Rnd.nextInt(100)==0)
				damage *= 2; 
		}

        // Failure calculation
        if (Config.ALT_GAME_MAGICFAILURES && !calcMagicSuccess(attacker, target, skill))
        {
        	if (attacker instanceof L2PcInstance) attacker.sendMessage("Attack failed");
        	if (calcMagicSuccess(attacker, target, skill))
                damage /= 2;
            else damage = 1;
        }
         
		return damage;
	}

	/** Returns true in case of critical hit */
	public final boolean calcCrit(double rate)
	{
		int critHit = Rnd.get(1000);
		return rate > critHit;
	}
	public final boolean calcMCrit(double mRate)
	{
		int mcritHit = Rnd.get(1000);
		return mRate > mcritHit;
	}
	/** Returns true in case when ATTACK is canceled due to hit */
	public final boolean calcAtkBreak(L2Character cha, double cancel)
	{
		if (Config.ALT_GAME_CANCEL_CAST && cha.isCastingNow())
			return cancel > Rnd.get(100);
		if (Config.ALT_GAME_CANCEL_BOW && cha.isAttackingNow()) {
			L2Weapon wpn = cha.getActiveWeaponItem();
			if (wpn != null && wpn.getItemType() == L2WeaponType.BOW)
				return cancel > Rnd.get(100);
		}
		return false;
	}

	/** Calculate delay (in milliseconds) before next ATTACK */
	public final int calcPAtkSpd(@SuppressWarnings("unused") L2Character attacker, @SuppressWarnings("unused") L2Character target, double rate)
	{
		//will fix freeze at >1000 atkspd, optimum calcation between 0 to 1400
		return (int) (3*Math.pow(rate, 2)/2000 - 4*rate + 2700);
	}

	/** Calculate delay (in milliseconds) for skills cast */
	public final int calcMAtkSpd(L2Character attacker, @SuppressWarnings("unused") L2Character target, L2Skill skill, double skillTime)
	{
		if (skill.isMagic())
			return (int)(skillTime * 333 / attacker.getMAtkSpd());
        return (int)(skillTime * 333 / attacker.getPAtkSpd());
	}

	/** Returns true if hit missed (taget evaded) */
    public boolean calcHitMiss(L2Character attacker, L2Character target) {
		// accuracy+dexterity => probability to hit in percents
		int acc_attacker;
		int evas_target;
		acc_attacker = attacker.getAccuracy();
		evas_target = target.getEvasionRate(attacker);
		int d = 85 + acc_attacker - evas_target;
		return d < Rnd.get(100);
	}

	/** Returns true if shield defence successfull */
    public boolean calcShldUse(L2Character attacker, L2Character target) 
    {
		double shldRate = 0.;
		
		if (target.calcStat(Stats.SHIELD_RATE, 0, attacker, null) == 0)
			return false;

		if (!target.isInFront(attacker, (int)attacker.calcStat(Stats.SHIELD_ANGLE, 60, null, null)))
			return false;

		if (attacker != null && attacker.getActiveWeaponItem() != null)
		{
			if (attacker.getActiveWeaponItem().getItemType() == L2WeaponType.BOW)
				shldRate = 30.;
			else if (attacker.getActiveWeaponItem().getItemType() == L2WeaponType.DAGGER)
				shldRate = 12.;
		}

		shldRate = target.calcStat(Stats.SHIELD_RATE, shldRate, attacker, null);
		return Rnd.get(100) < shldRate;
    }

    public boolean calcMagicAffected(L2Character actor, L2Character target, L2Skill skill)
    {
    	double defence = 0;
    	if (skill.isActive() && skill.isOffensive())
    		defence = target.getMDef(actor, skill);
    	double attack = 2 * actor.getMAtk(target, skill);
    	double d = attack - defence;
    	d /= attack + defence;
		d += 0.5 * Rnd.nextGaussian();
		return d > 0;
    }

    public boolean calcSkillSuccessOld(L2Character player, L2Character target, L2Skill skill, boolean ss, boolean sps, boolean bss )
    {
        if(Config.ALT_GAME_SKILL_FORMULAS.equalsIgnoreCase("alt") ||
        	Config.ALT_GAME_SKILL_FORMULAS.equalsIgnoreCase("true"))
        	return calcAltSkillSuccess(player, target, skill);
	if (target instanceof L2RaidBossInstance)
	    return false;

    	SkillType type = skill.getSkillType();

    	boolean success = false;
    	int rate = 0;
    	int check = Rnd.get(100);
        double modifier = 1;
        double ssmodifier = 1;
        int value = 1;
    	int maxLevel = SkillTable.getInstance().getMaxLevel(skill.getId(),skill.getLevel());
    	double pDef, mAtk, mDef;//,pAtk ;
        if (bss)
            ssmodifier *= 2;
        else if (sps)
            ssmodifier *= 1.5;        
        else if (ss)
            ssmodifier *= 1.5;        
	
    	switch(type)
		{
    	case STUN:
    		// For normal Stun Attack, with skillType = STUN
		pDef = 1;
		pDef = target.calcStat(Stats.STUN_RES,pDef,target,null);
		value = 4800  + (int)(100 * (player.getLevel()-target.getLevel())*0.5 + 5100 * ((float)skill.getLevel()/maxLevel));
		if (pDef > 0)
		    value /= pDef;
        	modifier = 100 * target.getCON()/30;
		if (modifier>0)
		    value /= modifier;
		value *= ssmodifier;
		value /= 100;
	    if (!(target instanceof L2RaidBossInstance))
	    {
		//min success
		if (value < 40)
		    value = 40;
		//max success
		if (value > 99)
		    value = 99;
	    }
		rate = value;
    		break;
    	case MDOT:
    	case CONFUSION:
            mAtk = player.getMAtk(target, skill);
            mDef = target.getMDef(player, skill);
            value = 3000 + (int)(7000 * ((float)skill.getLevel()/maxLevel));
            mDef = target.calcStat(Stats.CONFUSION_RES,mDef,target,null);
            if (mDef > 0 && mAtk > 0)
                value *= 0.6 * mAtk/mDef;
                modifier    = 100 * target.getMEN()/15;
            //_log.fine(player.getName()+" matk:"+mAtk+",mdef="+mDef+",value="+value+",modifier="+modifier+",maxlevel="+maxLevel+",level="+skill.getLevel());
                break;
    	case MUTE:
            mAtk = player.getMAtk(target, skill);
            mDef = target.getMDef(player, skill);
            value = 3000 + (int)(7000 * ((float)skill.getLevel()/maxLevel));
            mDef = target.calcStat(Stats.MUTE_RES,mDef,target,null);
            if (mDef > 0 && mAtk > 0)
                value *= 0.6 * mAtk/mDef;
                modifier    = 100 * target.getMEN()/15;
            //_log.fine(player.getName()+" matk:"+mAtk+",mdef="+mDef+",value="+value+",modifier="+modifier+",maxlevel="+maxLevel+",level="+skill.getLevel());
                break;
        case MDAM:                
    	case PARALYZE:
		mAtk = player.getMAtk(target, skill);
		mDef = target.getMDef(player, skill);
		value = 5000 + (int)(5000 * ((float)skill.getLevel()/maxLevel));
		if (mDef > 0 && mAtk > 0)
		    value *= 0.6 * mAtk/mDef;
        	modifier    = 100 * target.getMEN()/15;
		if (modifier>0)
		    value /= modifier;
		value *= ssmodifier;
		value /= 100;
	    if (!(target instanceof L2RaidBossInstance))
	    {
		//min success
		if (value < 35)
		    value = 35;
		//max success
		if (value > 90)
		    value = 90;
		rate = value;
	    }
		//_log.fine(player.getName()+" matk:"+mAtk+",mdef="+mDef+",value="+value+",modifier="+modifier+",maxlevel="+maxLevel+",level="+skill.getLevel());
    		break;
    	case SLEEP:
		mAtk = player.getMAtk(target, skill);
		mDef = target.getMDef(player, skill);
		value = 5000 + (int)(5000 * ((float)skill.getLevel()/maxLevel));
		mDef = target.calcStat(Stats.SLEEP_RES,mDef,target,null);
		if (mDef > 0 && mAtk > 0)
		    value *= 0.6 * mAtk/mDef;
        	modifier    = 100 * target.getWIT()/15;
		if (modifier>0)
		    value /= modifier;
		value *= ssmodifier;
		value /= 100;
	    if (!(target instanceof L2RaidBossInstance))
	    {
		//min success
		if (value < 45)
		    value = 45;
		//max success
		if (value > 90)
		    value = 90;
	    }
		rate = value;
		//_log.fine(player.getName()+" matk:"+mAtk+",mdef="+mDef+",value="+value+",modifier="+modifier+",maxlevel="+maxLevel+",level="+skill.getLevel());
    		break;
    	case ROOT:
		mAtk = player.getMAtk(target, skill);
		mDef = target.getMDef(player, skill);
		value = 5000 + (int)(5000 * ((float)skill.getLevel()/maxLevel));
		mDef = target.calcStat(Stats.ROOT_RES,mDef,target,null);
		if (mDef > 0 && mAtk > 0)
		    value *= 0.6 * mAtk/mDef;
        	modifier    = 100 * target.getDEX()/30;
		if (modifier>0)
		    value /= modifier;
		value *= ssmodifier;
		value /= 100;
	    if (!(target instanceof L2RaidBossInstance))
	    {
		//min success
		if (value < 35)
		    value = 35;
		//max success
		if (value > 90)
		    value = 90;
	    }
		rate = value;
    		break;
    	default:
    		// For normal Stun attack with skillType = PDAM
        	//pAtk = (int)skill.getPower();
		pDef = 1;
		pDef = target.calcStat(Stats.STUN_RES,pDef,target,null);
		value = 5000  + (int)(100 * (player.getLevel()-target.getLevel())*0.5 + 5000 * ((float)skill.getLevel()/maxLevel));
		if (pDef > 0)
		    value /= pDef;
        	modifier = 100 * target.getCON()/30;
		if (modifier>0)
		    value /= modifier;
		value *= ssmodifier;
		value /= 100;
	    if (!(target instanceof L2RaidBossInstance))
	    {
		//min success
		if (value < 35)
		    value = 35;
		//max success
		if (value > 90)
		    value = 90;
	    }
		rate = value;
//		pDef = target.getPDef(player);
//		pDef = target.calcStat(Stats.STUN_RES,pDef,target,null);
//		value = 30 * 100 + (int)(70 * 100 * ((float)skill.getLevel()/maxLevel));
//		value = 100 * 100;
//		if (pDef > 0 && pAtk > 0)
//		    value *= 0.6 * pAtk/pDef;
//        	modifier    = 100 * target.getCON()/30;
    		break;
		}

        if (modifier == 0)
        {
            _log.warning("Name: "+target.getName()+" has bad base stat value. Fix datapack or notify dp ppl");
            modifier = 1;
        }

	if (rate == 0)
    	    rate = (int)(((player.getLevel() - target.getLevel()) + (int)(value / modifier)) * ssmodifier);        
	//_log.fine(player.getName()+" rate:"+rate);


    	if (rate > 100) rate = 100;
	else if (rate < 0)
		rate = 0;

		if (target instanceof L2RaidBossInstance)
		{
			int cLevel = player.getLevel();
			int tLevel = target.getLevel();
			int rRate = 1;
			if ( cLevel > tLevel )
			{
				rRate = cLevel - tLevel;
				if (rRate > 9)
					rate /= rRate;
				if  (rate < 1)
					rate = 0;
			}
		}


        success = check <= rate;

    	return success;
    }

    public int calcSkillResistance(SkillType type, L2Character target)
    {
        if (type == null) return 0;
        switch(type)
        {
            case BLEED: return (int) target.calcStat(Stats.BLEED_RES, 0, target, null);
            case POISON: return (int) target.calcStat(Stats.POISON_RES, 0, target, null);
            case STUN: return (int) target.calcStat(Stats.STUN_RES, 0, target, null);
            case PARALYZE: return (int) target.calcStat(Stats.PARALYZE_RES, 0, target, null); 
            case ROOT: return (int) target.calcStat(Stats.ROOT_RES, 0, target, null);
            case SLEEP: return (int) target.calcStat(Stats.SLEEP_RES, 0, target, null);
            case MUTE: return (int) target.calcStat(Stats.MUTE_RES, 0, target, null); 
            case FEAR:
            case CONFUSION: return (int) target.calcStat(Stats.CONFUSION_RES, 0, target, null);
            default: return 0;
        }
    }

    public int calcSkillStatModifier(SkillType type, L2Character target)
    {
        if (type == null) return 0;
        switch(type)
        {
            case STUN: return target.getCON();
            case ROOT: return target.getDEX();
            case SLEEP: return target.getWIT();
            case MUTE:
            case CONFUSION: 
            case PARALYZE: return target.getMEN();
            default: return 0;
        }
    }

    public boolean calcSkillSuccess(L2Character attacker, L2Character target, L2Skill skill, boolean ss, boolean sps, boolean bss)
    {
        if(Config.ALT_GAME_SKILL_FORMULAS.equalsIgnoreCase("alt") || Config.ALT_GAME_SKILL_FORMULAS.equalsIgnoreCase("true"))
        	return calcAltSkillSuccess(attacker, target, skill);

        // Uncomment this if you want to revert to old skill success calculation: 
        // return calcSkillSuccessOld(attacker, target, skill, ss, sps, bss);

    	SkillType type = skill.getSkillType();

        if (target.isRaid() && (type == SkillType.CONFUSION || type == SkillType.MUTE || type == SkillType.PARALYZE || type == SkillType.ROOT)) 
            return false; // these skills should not work on RaidBoss

        int value = (int)skill.getPower();
        int lvlDepend = skill.getLevelDepend();
        
        if (type == SkillType.PDAM || type == SkillType.MDAM) // For additional effects on PDAM skills (like STUN, SHOCK,...)
        {
            value = skill.getEffectPower();
            type = skill.getEffectType();
        }
        // TODO: Temporary fix for skills with EffectPower = 0 or EffectType not set
        if (value == 0 || type == null)
        {
            if (skill.getSkillType() == SkillType.PDAM)
            {
                value = 50;
                type = SkillType.STUN;
            }
            if (skill.getSkillType() == SkillType.MDAM)
            {
                value = 30;
                type = SkillType.PARALYZE;
            }
        }

        // TODO: Temporary fix for skills with Power = 0 or LevelDepend not set
        if(value == 0) value = (type == SkillType.PARALYZE)? 50 : 80;
        if(lvlDepend == 0) lvlDepend = (type == SkillType.PARALYZE)? 1 : 2;

        // TODO: Temporary fix for NPC skills with MagicLevel not set
        // int lvlmodifier = (skill.getMagicLevel() - target.getLevel()) * lvlDepend;
        int lvlmodifier = ((skill.getMagicLevel()>0?skill.getMagicLevel():attacker.getLevel()) - target.getLevel()) * lvlDepend;
        int statmodifier = calcSkillStatModifier(type, target);

        int ssmodifier = 100;
        if (bss) ssmodifier = 200;
        else if (sps) ssmodifier = 150;        
        else if (ss) ssmodifier = 150;

        int rate = value + lvlmodifier - calcSkillResistance(type, target);
        if (skill.isMagic()) rate *= Math.sqrt((double)attacker.getMAtk(target, skill)/target.getMDef(attacker, skill));
        if (statmodifier > 0) rate = rate * (100 - statmodifier) / 100;

        if (rate > 99) rate = 99;
        else if (rate < 1) rate = 1;

        if (ssmodifier != 100)
        {
            if (rate > 10000/(100+ssmodifier))
                rate = 100-(100-rate)*100/ssmodifier;
            else rate = rate*ssmodifier/100;
        }

        if(Config.DEVELOPER) System.out.println(skill.getName() + ": " + value + ", " + lvlmodifier + ", " + calcSkillResistance(type, target) + ", " + statmodifier + ", " + ssmodifier + ", " + Math.sqrt((double)attacker.getMAtk(target, skill)/target.getMDef(attacker, skill)) + " ==> " + rate);
        return (Rnd.get(100) < rate);
    }

    public boolean calcMagicSuccess(L2Character attacker, L2Character target, L2Skill skill)
    {
        /* Level difference: 
         *  less then 10 : full chance
         *  10 - 20      : reduced chance
         *  20 and more  : min chance
         */
        int value = target.isRaid()? 100 : 200; // chance is reduced for RaidBoss
        int lvlDepend = 10;

        // TODO: Temporary fix for NPC skills with MagicLevel not set
        // int lvlmodifier = (skill.getMagicLevel() - target.getLevel()) * lvlDepend;
        int lvlmodifier = ((skill.getMagicLevel()>0?skill.getMagicLevel():attacker.getLevel()) - target.getLevel()) * lvlDepend;

        int rate = value + lvlmodifier;

        if (rate > 99) rate = 99;
        else if (rate < 1) rate = 1;

        return (Rnd.get(100) < rate);
    }

    public boolean calcAltSkillSuccess(L2Character activeChar, L2Character target, L2Skill skill)
    {
        // Get our numbers and base success rate
        SkillType type = skill.getSkillType();
        boolean success = true;
        int skillPower = (int)skill.getPower();
        int skillLevel = skill.getLevel();
        int attackerLevel = activeChar.getLevel();
        int targetLevel = target.getLevel();
        int CONModifier = (100 - target.getCON());
        int DEXModifier = (90 - target.getDEX());
        int WITModifier = (80 - target.getWIT());
        int powerModifier = Math.round(skillPower/100);
        int levelModifier = Math.round(skillLevel/2);
        int baseRate = (attackerLevel - targetLevel) + powerModifier + levelModifier;
        int rate = baseRate;
        int check = Rnd.get(100);

        switch (type) {
            case STUN:
                rate += CONModifier;    // uses CON Modifier for STUN types
                break;
            case ROOT:
                rate += DEXModifier;    // uses DEX Modifier for ROOT types
                break;
            case PARALYZE:
            case SLEEP:
                rate += WITModifier;    // uses WIT Modifier for SLEEP and PARALYZE types
                break;
            default:
                rate += CONModifier;    // uses CON Modifier for any other types (like PDAM ones)
                break;
        }

        if (rate > 100) rate = 100;         // We shouldn't have more than 100% success rate
        if (rate < 1) rate = 1;             // We shouldn't have less than 1% success rate

        if (check > rate) success = false;
        return success;
    }

	public boolean calculateUnlockChance(L2Skill skill)
	{
		int level = skill.getLevel();
		int chance = 0;
		switch (level)
		{
	     case 1:
			 chance = 30;
	         break;

	     case 2:
			 chance = 50;
	         break;

	     case 3:
			 chance = 75;
	         break;

	     case 4:
	     case 5:
	     case 6:
	     case 7:
	     case 8:
	     case 9:
	     case 10:
	     case 11:
	     case 12:
	     case 13:
	     case 14:
			 chance = 100;
	         break;
		}
        return Rnd.get(120) <= chance;

    }
}
