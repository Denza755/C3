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

import java.util.logging.Logger;

import net.sf.l2j.Config;
import ru.balancer.JBForth;
import net.sf.l2j.gameserver.lib.Rnd;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2DoorInstance;
import net.sf.l2j.gameserver.model.L2NpcInstance;
import net.sf.l2j.gameserver.model.L2PcInstance;
import net.sf.l2j.gameserver.model.L2PlayableInstance;
import net.sf.l2j.gameserver.model.L2RaidBossInstance;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2Summon;
import net.sf.l2j.gameserver.skills.ConditionPlayerState.CheckPlayerState;
import net.sf.l2j.gameserver.templates.L2ArmorType;
import net.sf.l2j.gameserver.templates.L2PcTemplate;
import net.sf.l2j.gameserver.templates.L2Weapon;
import net.sf.l2j.gameserver.templates.L2WeaponType;

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
			super(stat, 0x10, null);
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
			double ib = INTbonus[env._player.getINT()];
			double lvlb = env._player.getLevelMod();
			env.value *= (lvlb * lvlb) * (ib * ib);
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
			env.value *= MENbonus[env._player.getMEN()];
		}
	}

	static class FuncAttackRange extends Func
	{
		static final FuncAttackRange _far_instance = new FuncAttackRange();

		static Func getInstance() {
			return _far_instance;
		}

		private FuncAttackRange()
		{
			super(Stats.POWER_ATTACK_RANGE, 0x10, null);
		}

		public void calc(Env env)
		{
			if(env._player.getActiveWeaponItem().getItemType() == L2WeaponType.BOW)
				env.value += 460;
			else if(env._player.getActiveWeaponItem().getItemType() == L2WeaponType.POLE)
				env.value += 40;
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
			//[Square(DEX)]*6 + lvl + weapon hitbonus;
			env.value += Math.sqrt(env._player.getDEX())*6 + env._player.getLevel();
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
			//[Square(DEX)]*6 + lvl;
			env.value += Math.sqrt(env._player.getDEX())*6 + env._player.getLevel();
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
			env.value *= DEXbonus[env._player.getDEX()];
			if (!(env._player instanceof L2Summon))
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
			env.value *= DEXbonus[env._player.getDEX()];
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
			env.value *= DEXbonus[env._player.getDEX()];
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
			env.value *= WITbonus[env._player.getWIT()];
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
			cha.addStatFunc(FuncAttackRange.getInstance());
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
		//double init = cha.getMaxHp()*cha.getTemplate().baseHpReg * (Config.HP_REGEN_MULTIPLIER / 100);
		double multiplier=1;
		if (!cha.isRunning()) multiplier=1.5;
		if (cha instanceof L2PcInstance){
		    if (((L2PcInstance)cha).isSitting()) multiplier=2;
		}
		else if (cha instanceof L2RaidBossInstance)
		    multiplier=Config.RATE_RAID_REGEN_HP;
        double init = ((0.1 * cha.getLevel() + cha.getTemplate().baseHpReg) * (1 + CONbonus[cha.getCON()])) * (Config.HP_REGEN_MULTIPLIER * multiplier / 100);
		return cha.calcStat(Stats.REGENERATE_HP_RATE, init, null, null);
	}

	/**
	 * Calculate the MP regen rate (base + modifiers).<BR><BR>
	 */
	public final double calcMpRegen(L2Character cha)
	{
		double multiplier=1;
		if (!cha.isRunning()) multiplier=1.5;
		if (cha instanceof L2PcInstance){
		    if (((L2PcInstance)cha).isSitting()) multiplier=2;
		}
		else if (cha instanceof L2RaidBossInstance)
		    multiplier=Config.RATE_RAID_REGEN_MP;
		//double init = cha.getMaxMp()*cha.getTemplate().baseMpReg * (Config.MP_REGEN_MULTIPLIER / 100);
        double init = ((0.03 * cha.getLevel() + cha.getTemplate().baseMpReg) * (1 + MENbonus[cha.getMEN()])) * (Config.MP_REGEN_MULTIPLIER * multiplier / 100);
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
	public final double calcPhysDam(L2Character attacker, L2Character target, L2Skill skill, boolean shld, boolean crit, boolean dual, boolean ss)
	{
		double damage = attacker.getPAtk(target);
		double defence = target.getPDef(attacker);

		if(ss) damage *= 2;

		if(skill != null)
			damage += skill.getPower();
		else {
			if(crit) damage = attacker.getCriticalDmg(target, damage);
			if(dual) damage /= 1.5;
		}

		if(shld) defence += target.getShldDef();

		damage *= 70. / defence;
		damage += Rnd.get()* attacker.getRandomDamage(target);

		L2Weapon weapon = attacker.getActiveWeaponItem();
		if(weapon != null)
			switch (weapon.getItemType()){
			case BOW:	damage *= 0.01*target.calcStat(Stats.BOW_WPN_RES,100,null,skill);break;
			case BLUNT:	damage *= 0.01*target.calcStat(Stats.BLUNT_WPN_RES,100,null,skill);break;
			case DAGGER:damage *= 0.01*target.calcStat(Stats.DAGGER_WPN_RES,100,null,skill);break;
			case DUAL:
			case BIGSWORD:
			case SWORD:	damage *= 0.01*target.calcStat(Stats.SWORD_WPN_RES,100,null,skill);break;
			case DUALFIST:damage*=0.01*target.calcStat(Stats.DUALFIST_WPN_RES,100,null,skill);break;
			case POLE:	damage *= 0.01*target.calcStat(Stats.POLE_WPN_RES,100,null,skill);break;
			case ETC:	damage *= 0.01*target.calcStat(Stats.ETC_WPN_RES,100,null,skill);break;
			case FIST:	damage *= 0.01*target.calcStat(Stats.FIST_WPN_RES,100,null,skill);
			}

        if(target instanceof L2NpcInstance)
        {
        	switch(((L2NpcInstance)target).getTemplate().race){
        	case 4290: damage *= 0.01*attacker.calcStat(Stats.PATK_UNDEAD,100,target,skill);break;
        	case 4292: damage *= 0.01*attacker.calcStat(Stats.PATK_MONSTERS,100,target,skill);break;
        	case 4293: damage *= 0.01*attacker.calcStat(Stats.PATK_ANIMALS,100,target,skill);break;
        	case 4294: damage *= 0.01*attacker.calcStat(Stats.PATK_PLANTS,100,target,skill);break;
        	case 4299: damage *= 0.01*attacker.calcStat(Stats.PATK_DRAGONS,100,target,skill);break;
        	case 4301: damage *= 0.01*attacker.calcStat(Stats.PATK_INSECTS,100,target,skill);
        	}

        	if(target instanceof L2RaidBossInstance)
        	{
        		int cLevel = attacker.getLevel();
        		int tLevel = target.getLevel();
        		if(cLevel > tLevel)
        		{
        			int rDamage = cLevel - tLevel;
        			if(rDamage > 9)
        				damage /= rDamage;
        		}
        	}
        }
        else if(target instanceof L2PlayableInstance && attacker instanceof L2RaidBossInstance)
		{
			int cLevel = attacker.getLevel();
			int tLevel = target.getLevel();
			if(cLevel < tLevel)
			{
				int rDamage = tLevel - cLevel;
				if(rDamage > 9)
					damage *= rDamage;
			}
		}

		if(damage < 1)
			return 1;
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

		double damage = 91*skill.getPower()*Math.sqrt(mAtk)/mDef;

		if(mcrit)
		{
			damage *= 2;
			if (Rnd.get(100)==0)
				damage *= 2;
		}

		switch(skill.getElement()){
		case L2Skill.ELEMENT_EARTH:	damage *= 0.01*target.calcStat(Stats.EARTH_RES,100,target,skill); break;
		case L2Skill.ELEMENT_FIRE:	damage *= 0.01*target.calcStat(Stats.FIRE_RES,100,target,skill);break;
		case L2Skill.ELEMENT_WATER:	damage *= 0.01*target.calcStat(Stats.WATER_RES,100,target,skill);break;
		case L2Skill.ELEMENT_WIND:	damage *= 0.01*target.calcStat(Stats.WIND_RES,100,target,skill);break;
		case L2Skill.ELEMENT_UNHOLY:damage *= 0.01*target.calcStat(Stats.UNHOLY_RES,100,target,skill);break;
		case L2Skill.ELEMENT_SACRED:damage *= 0.01*target.calcStat(Stats.SACRED_RES,100,target,skill);break;
		}

		if (target instanceof L2RaidBossInstance)
		{
			int cLevel = attacker.getLevel();
			int tLevel = target.getLevel();
			int rDamage = 1;
			if ( cLevel > tLevel )
			{
				rDamage = cLevel - tLevel;
				if (rDamage > 9)
					damage /= rDamage;
				if  (damage < 1)
					damage = 1;
			}
		}

		if (target instanceof L2PlayableInstance && attacker instanceof L2RaidBossInstance)
		{
			int cLevel = attacker.getLevel();
			int tLevel = target.getLevel();
			int rDamage = 1;
			if ( cLevel < tLevel )
			{
				rDamage = tLevel - cLevel;
				if (rDamage > 9)
					damage *= rDamage;
			}
		}
		return damage;
	}

	/** Returns true in case of fatal blow success */
	public final boolean calcBlow(L2Character activeChar, L2Character target)
	{
		int head = activeChar.getHeadingTo(target, true);
		double rate = activeChar.calcStat(Stats.FATALBLOW_RATE, activeChar.getDEX(), target, null);
		if (!target.isInCombat())
			rate *= 1.41;
		if (head <= 10430 || head >= 55105)
			rate *= 1.41;
		else if (head <= 21000 || head >= 44500)
			rate *= 1.19;
		if(target instanceof L2PcInstance)
		{
			L2PcInstance pcTarget = (L2PcInstance)target;
			if(pcTarget.isWearingArmor(L2ArmorType.HEAVY))
				rate *= 0.6;
			else if(pcTarget.isWearingArmor(L2ArmorType.MAGIC))
				rate *= 1.19;
		}
		return rate > Rnd.get(100);
	}

	/** Returns true in case of critical hit */
	public final boolean calcCrit(double rate)
	{
		return rate > Rnd.get(1000);
	}
	/** Returns true in case of magic critical hit */
	public final boolean calcMCrit(double mRate)
	{
		return mRate > Rnd.get(1000);
	}
	/** Returns true in case when ATTACK is canceled due to hit */
	public final boolean calcCastBreak(L2Character cha, double cancel)
	{
		if (cha.isCastingNow())
			return cancel > Rnd.get(100);
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
		return 85 + attacker.getAccuracy() - target.getEvasionRate(attacker) < Rnd.get(100);
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
		return shldRate > Rnd.get(100);
    }

	public double calcSavevsDependence(int save, L2Character cha)
	{
		switch (save){
		case L2Skill.SAVEVS_WIT: return WITbonus[cha.getWIT()+9]; 
		case L2Skill.SAVEVS_CON: return CONbonus[cha.getCON()-14];
		case L2Skill.SAVEVS_DEX: return DEXbonus[cha.getDEX()*3-56];
		case L2Skill.SAVEVS_STR: return STRbonus[cha.getSTR()];
		case L2Skill.SAVEVS_INT: return INTbonus[cha.getINT()];
		case L2Skill.SAVEVS_MEN: return MENbonus[cha.getMEN()]/1.32;
		}
		return 1.;
	}

	public boolean calcSkillSuccess(L2Character player, L2Character target, L2Skill skill)
	{
        if(Config.ALT_GAME_SKILL_FORMULAS.equalsIgnoreCase("forth"))
        {
			String word = "calc-"+skill.getSkillType()+"-success";

			if(player.jbf.word_exists(word))
			{
				JBForth jbf = new JBForth(player);
				jbf.uvset("skill", skill);
				jbf.uvset("target", target);
				Object res = jbf.execr(word);

				if(res != null)
	        		return (Integer)res != 0;
        	}
			else
				_log.fine("Not defined '" + word + "'. Using new default");
		}

		double actRate = skill.getActivateRate();
		if (actRate == -1) return true;

		actRate += (skill.getMagicLevel() - target.getLevel()) * skill.getLevelModifier();

		if (skill.isMagic())
			actRate *= 15 * Math.sqrt(player.getMAtkSps(target, skill)) / target.getMDef(null, skill);

		actRate /= calcSavevsDependence(skill.getSavevs(), target);

		if (skill.getStat()!=null)
		switch(skill.getStat())
		{
		case ROOT:	actRate*=0.01*target.calcStat(Stats.ROOT_RES, 100, null, null); break;
		case SLEEP:	actRate*=0.01*target.calcStat(Stats.SLEEP_RES, 100, null, null); break;
		case STUN:	actRate*=0.01*target.calcStat(Stats.STUN_RES, 100, null, null); break;
		case BLEED:	actRate*=0.01*target.calcStat(Stats.BLEED_RES, 100, null, null); break;
		case POISON:actRate*=0.01*target.calcStat(Stats.POISON_RES, 100, null, null); break;
		case MOVEMENT:actRate*=0.01*target.calcStat(Stats.MOVEMENT_RES, 100, null, null); break;
		}

		if (actRate > 98)
			actRate = 98;

		player.sendMessage(skill.getName()+": "+(int)actRate);

		return actRate >= 100. * Rnd.get();
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
	     if(Rnd.get(120) > chance)
			 return false;

		return true;
	}
}
