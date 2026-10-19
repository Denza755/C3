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
package net.sf.l2j.gameserver.model;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.l2j.gameserver.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.skills.Condition;
import net.sf.l2j.gameserver.skills.EffectTemplate;
import net.sf.l2j.gameserver.skills.Env;
import net.sf.l2j.gameserver.skills.Func;
import net.sf.l2j.gameserver.skills.FuncTemplate;
import net.sf.l2j.gameserver.skills.L2SkillCharge;
import net.sf.l2j.gameserver.skills.L2SkillChargeDmg;
import net.sf.l2j.gameserver.skills.L2SkillDefault;
import net.sf.l2j.gameserver.skills.L2SkillDrain;
import net.sf.l2j.gameserver.skills.L2SkillSummon;
import net.sf.l2j.gameserver.skills.Stats;
import net.sf.l2j.gameserver.templates.L2WeaponType;
import net.sf.l2j.gameserver.templates.StatsSet;


/**
 * This class ...
 * 
 * @version $Revision: 1.3.2.8.2.22 $ $Date: 2005/04/06 16:13:42 $
 */
public abstract class L2Skill
{
	protected static Logger _log = Logger.getLogger(L2Skill.class.getName());
	
	public static final int SKILL_CUBIC_MASTERY = 143;
	public static final int SKILL_LUCKY = 194;
	public static final int SKILL_CRAFTING = 172;
	public static final int SKILL_CRYSTALLIZE = 248;
	
	public static final int SKILL_FAKE_INT = 9001;
	public static final int SKILL_FAKE_WIT = 9002;
	public static final int SKILL_FAKE_MEN = 9003;
	public static final int SKILL_FAKE_CON = 9004;
	public static final int SKILL_FAKE_DEX = 9005;
	public static final int SKILL_FAKE_STR = 9006;
	
	
	
	public static enum SkillOpType { OP_PASSIVE, OP_ACTIVE, OP_TOGGLE }
	
	public static enum SkillTargetType {
		TARGET_NONE,
		TARGET_SELF,
		TARGET_ONE,
		TARGET_PARTY,
		TARGET_CLAN,
		TARGET_PET,
		TARGET_AREA,
		TARGET_AURA,
		TARGET_CORPSE,
		TARGET_UNDEAD,
		TARGET_MULTIFACE,
		TARGET_AURA_CLOSE,
		TARGET_CORPSE_CLAN,
		TARGET_CORPSE_PLAYER,
		TARGET_ITEM,
		TARGET_AREA_CORPSE_MOB,
		TARGET_CORPSE_MOB,
		TARGET_UNLOCKABLE,
		TARGET_HOLY
		}
	
	
	public static enum SkillType {
		PDAM,
		MDAM,
		DOT,
		BLEED,
		POISON,
		HEAL,
		HOT,
		MANAHEAL,
		AGGDAMAGE,
		BUFF,
		DEBUFF,
		STUN,
		ROOT,
		RESURRECT,
		PASSIVE,
		CONT,
		CONFUSION,
		UNLOCK,
		CHARGE                (L2SkillCharge.class),
		FEAR,
		MHOT,
		DRAIN                 (L2SkillDrain.class),
		NEGATE,
		SLEEP,
		AGGREDUCE,
		AGGREMOVE,
		AGGREDUCE_CHAR,
		CHARGEDAM             (L2SkillChargeDmg.class),
		CONFUSE_MOB_ONLY,
		DEATHLINK,
		DETECT_WEAKNESS,
		ENCHANT_ARMOR,
		ENCHANT_WEAPON,
		FEED_PET,
		HEAL_PERCENT,
		LUCK,
		MANADAM,
		MDOT,
		MUTE,
		RECALL,
		REFLECT,
		SOULSHOT,
		SPIRITSHOT,
		SPOIL,
	    SWEEP,
		SUMMON                (L2SkillSummon.class),
		WEAKNESS,
		DEATHLINK_PET,
		MANA_BY_LEVEL,
		FAKE_DEATH,
		UNBLEED,
		UNPOISON,
		// unimplemented
		NOTDONE;
		
		private final Class<? extends L2Skill> clazz;
		
		public L2Skill makeSkill(StatsSet set)
		{
			try {
				Constructor<? extends L2Skill> c =
					clazz.getConstructor(StatsSet.class);
				return c.newInstance(set);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		
		private SkillType() {
			this.clazz = L2SkillDefault.class;
		}
		private SkillType(Class<? extends L2Skill> clazz) {
			this.clazz = clazz;
		}
	}
	
	//elements
	public final static int ELEMENT_WIND = 1;
	public final static int ELEMENT_FIRE = 2;
	public final static int ELEMENT_WATER = 3;
	public final static int ELEMENT_EARTH = 4;
	public final static int ELEMENT_HOLY = 5;
	
	//save vs 
	public final static int SAVEVS_INT = 1;
	public final static int SAVEVS_WIT = 2;
	public final static int SAVEVS_MEN = 3;
	public final static int SAVEVS_CON = 4;
	public final static int SAVEVS_DEX = 5;
	public final static int SAVEVS_STR = 6;
	
	//stat effected
	public final static int STAT_PATK = 301;           // pAtk 
	public final static int STAT_PDEF = 302;           // pDef
	public final static int STAT_MATK = 303;           // mAtk
	public final static int STAT_MDEF = 304;           // mDef
	public final static int STAT_MAXHP = 305;          // maxHp
	public final static int STAT_MAXMP = 306;          // maxMp
	public final static int STAT_CURHP = 307;
	public final static int STAT_CURMP = 308;
	public final static int STAT_HPREGEN = 309;        // regHp
	public final static int STAT_MPREGEN = 310;        // regMp
	public final static int STAT_CASTINGSPEED = 311;   // sCast
	public final static int STAT_ATKSPD = 312;         // sAtk
	public final static int STAT_CRITDAM = 313;        // critDmg
	public final static int STAT_CRITRATE = 314;       // critRate
	public final static int STAT_FIRERES = 315;        // fireRes
	public final static int STAT_WINDRES = 316;        // windRes
	public final static int STAT_WATERRES = 317;       // waterRes
	public final static int STAT_EARTHRES = 318;       // earthRes
	public final static int STAT_ROOTRES = 319;        // rootRes
	public final static int STAT_SLEEPRES = 320;       // sleepRes
	public final static int STAT_CONFUSIONRES = 321;   // confusRes
	public final static int STAT_BREATH = 322;         // breath
	public final static int STAT_AGGRESSION = 323;     // aggr
	public final static int STAT_BLEED = 324;          // bleed
	public final static int STAT_POISON = 325;         // poison
	public final static int STAT_STUN = 326;           // stun
	public final static int STAT_ROOT = 327;           // root
	public final static int STAT_MOVEMENT = 328;       // move
	public final static int STAT_EVASION = 329;        // evas
	public final static int STAT_ACCURACY = 330;       // accu
	public final static int STAT_COMBAT_STRENGTH = 331;
	public final static int STAT_COMBAT_WEAKNESS = 332;
	public final static int STAT_ATTACK_RANGE = 333;   // rAtk
	public final static int STAT_NOAGG = 334;          // noagg
	public final static int STAT_SHIELDDEF = 335;      // sDef
	
	//COMBAT DAMAGE MODIFIER SKILLS...DETECT WEAKNESS AND WEAKNESS/STRENGTH
	public final static int COMBAT_MOD_ANIMAL = 200;
	public final static int COMBAT_MOD_BEAST	= 201;
	public final static int COMBAT_MOD_BUG	= 202;
	public final static int COMBAT_MOD_DRAGON = 203;
	public final static int COMBAT_MOD_MONSTER= 204;
	public final static int COMBAT_MOD_PLANT	= 205;
	public final static int COMBAT_MOD_HOLY	= 206;
	public final static int COMBAT_MOD_UNHOLY = 207;
	public final static int COMBAT_MOD_BOW	= 208;
	public final static int COMBAT_MOD_BLUNT	= 209;
	public final static int COMBAT_MOD_DAGGER = 210;
	public final static int COMBAT_MOD_FIST 	= 211;
	public final static int COMBAT_MOD_DUAL 	= 212;
	public final static int COMBAT_MOD_SWORD	= 213;
	public final static int COMBAT_MOD_POISON = 214;
	public final static int COMBAT_MOD_BLEED	= 215;
	public final static int COMBAT_MOD_FIRE	= 216;
	public final static int COMBAT_MOD_WATER	= 217;
	public final static int COMBAT_MOD_EARTH 	= 218;
	public final static int COMBAT_MOD_WIND	= 219;
	public final static int COMBAT_MOD_ROOT	= 220;
	public final static int COMBAT_MOD_STUN	= 221;
	public final static int COMBAT_MOD_CONFUSION = 222;
	
	//conditional values
	public final static int COND_RUNNING =   0x0001;
	public final static int COND_WALKING =   0x0002;
	public final static int COND_SIT     =   0x0004;
	public final static int COND_BEHIND  =   0x0008;
	public final static int COND_CRIT    =   0x0010;
	public final static int COND_LOWHP   =   0x0020;
	public final static int COND_ROBES   =   0x0040;
	public final static int COND_CHARGES =   0x0080;
	public final static int COND_SHIELD  =   0x0100;
	public final static int COND_GRADEA  = 0x010000;
	public final static int COND_GRADEB  = 0x020000;
	public final static int COND_GRADEC  = 0x040000;
	public final static int COND_GRADED  = 0x080000;
	public final static int COND_GRADES  = 0x100000;
	
	
	private static final Func[] _emptyFunctionSet = new Func[0];
	private static final L2Effect[] _emptyEffectSet = new L2Effect[0];
	
	// these two build the primary key
	private final int _id;
	private final int _level;

	// for the skills that a client cannot display
	private int _displayId;

	// not needed, just for easier debug
	private final String _name;
	private final SkillOpType _operateType;
	private final boolean _magic;
	private final int _mpConsume;
	private final int _hpConsume;
	private final int _itemConsume;
	private final int _itemConsumeId;
	private final int _castRange;
	
	// all times in milliseconds 
	private final int _skillTime;
	private final int _skillInterruptTime;
	private final int _hitTime;
	private final int _reuseDelay;
	private final int _buffDuration;
	
	private final SkillTargetType _targetType;
	private final double _power;
	
	private final SkillType _skillType;

	private final int _element;
	private final int _savevs;
	
	private final Stats _stat;
	
	private final int _condition;
	private final int _conditionValue;
	private final boolean _overhit;
	private final int _weaponsAllowed;
	private final int _armorsAllowed;

	private final int _addCrossLearn; // -1 disable, otherwice SP price for others classes, default 1000
	private final float _mulCrossLearn; // multiplay for others classes, default 2
	private final float _mulCrossLearnRace; // multiplay for others races, default 2
	private final float _mulCrossLearnProf; // multiplay for fighter/mage missmatch, default 3
	private final ArrayList<ClassId> _canLearn; // which classes can learn
	private final ArrayList<Integer> _teachers; // which NPC teaches
	

	protected Condition _preCondition;
	protected FuncTemplate[] _funcTemplates;
	protected EffectTemplate[] _effectTemplates;
	
	
	protected L2Skill(StatsSet set)
	{
		_id           = set.getIntegr("skill_id");
		_level        = set.getIntegr("level");

		_displayId    = set.getIntegr("displayId", _id);
		_name         = set.getString("name");
		_operateType  = set.getEnum("operateType", SkillOpType.class);
		_magic        = set.getBool  ("isMagic", false);
		_mpConsume    = set.getIntegr("mpConsume", 0);
		_hpConsume    = set.getIntegr("hpConsume", 0);
		_itemConsume  = set.getIntegr("itemConsumeCount", 0);
		_itemConsumeId= set.getIntegr("itemConsumeId", 0);
		
		_castRange    = set.getIntegr("castRange", 0);
		_skillTime    = set.getIntegr("skillTime", 0);
		_skillInterruptTime = set.getIntegr("skillTime", _skillTime/2);
		_hitTime      = set.getIntegr("hitTime", 0);
		_reuseDelay   = set.getIntegr("reuseDelay", 0);
		_buffDuration = set.getIntegr("buffDuration", 0);
		
		_targetType   = set.getEnum("target", SkillTargetType.class);
		_power        = set.getFloat("power", 0.f);
		_stat         = set.getEnum  ("stat", Stats.class, null);
		
		_skillType    = set.getEnum("skillType", SkillType.class);
		
		_element      = set.getIntegr("element", 0);
		_savevs       = set.getIntegr("save", 0);
		
		
		_condition       = set.getIntegr("condition", 0);
		_conditionValue  = set.getIntegr("conditionValue", 0);
		_overhit         = set.getBool  ("overHit", false);
		_weaponsAllowed  = set.getIntegr("weaponsAllowed", 0);
		_armorsAllowed   = set.getIntegr("armorsAllowed", 0);

		_addCrossLearn       = set.getIntegr("addCrossLearn", 1000);
		_mulCrossLearn       = set.getFloat ("mulCrossLearn", 2.f);
		_mulCrossLearnRace   = set.getFloat ("mulCrossLearnRace", 2.f);
		_mulCrossLearnProf   = set.getFloat ("mulCrossLearnProf", 3.f);
	
		String canLearn = set.getString("canLearn", null);
		if (canLearn == null) {
			_canLearn = null;
		} else {
			_canLearn = new ArrayList<ClassId>();
			StringTokenizer st = new StringTokenizer(canLearn, " \r\n\t,;");
			while (st.hasMoreTokens()) {
				String cls = st.nextToken();
				try {
					_canLearn.add(ClassId.valueOf(cls));
				} catch (Throwable t) {
					_log.log(Level.SEVERE, "Bad class "+cls+" to learn skill", t);
				}
			}
		}

		String teachers = set.getString("teachers", null);
		if (teachers == null) {
			_teachers = null;
		} else {
			_teachers = new ArrayList<Integer>();
			StringTokenizer st = new StringTokenizer(teachers, " \r\n\t,;");
			while (st.hasMoreTokens()) {
				String npcid = st.nextToken();
				try {
					_teachers.add(Integer.parseInt(npcid));
				} catch (Throwable t) {
					_log.log(Level.SEVERE, "Bad teacher id "+npcid+" to teach skill", t);
				}
			}
		}
	}
	
	public abstract void useSkill(L2Character caster, L2Object[] targets);
	
	public final int getArmorsAllowed()
	{
		return _armorsAllowed;
	}
	
	public final int getConditionValue()
	{
		return _conditionValue;
	}
	
	public final SkillType getSkillType() 
	{
		return _skillType;
	}
	
	public final int getSavevs() {
		return _savevs;
	}
	
	public final int getElement() 
	{
		return _element;
	}
	
	public final SkillTargetType getTargetType() 
	{
		return _targetType;
	}
	
	public final int getCondition() {
		return _condition;
	}
	
	public final boolean isOverhit() 
	{
		return _overhit;
	}
	
	
	public final double getPower()
	{
		return _power;
	}
	
	/**
	 * @return Returns the buffDuration.
	 */
	public final int getBuffDuration()
	{
		return _buffDuration;
	}

	/**
	 * @return Returns the castRange.
	 */
	public final int getCastRange()
	{
		return _castRange;
	}

	/**
	 * @return Returns the hitTime.
	 */
	public final int getHitTime()
	{
		return _hitTime;
	}

	/**
	 * @return Returns the hpConsume.
	 */
	public final int getHpConsume()
	{
		return _hpConsume;
	}

	/**
	 * @return Returns the id.
	 */
	public final int getId()
	{
		return _id;
	}

	public int getDisplayId()
	{
		return _displayId;
	}

	public void setDisplayId(int id)
	{
		_displayId = id;
	}
	
	public final Stats getStat()
	{
		return _stat;
	}
	
	/**
	 * @return Returns the itemConsume.
	 */
	public final int getItemConsume()
	{
		return _itemConsume;
	}

	/**
	 * @return Returns the itemConsumeId.
	 */
	public final int getItemConsumeId()
	{
		return _itemConsumeId;
	}

	/**
	 * @return Returns the level.
	 */
	public final int getLevel()
	{
		return _level;
	}

	/**
	 * @return Returns the magic.
	 */
	public final boolean isMagic()
	{
		return _magic;
	}

	/**
	 * @return Returns the mpConsume.
	 */
	public final int getMpConsume()
	{
		return _mpConsume;
	}

	/**
	 * @return Returns the name.
	 */
	public final String getName()
	{
		return _name;
	}

	/**
	 * @return Returns the reuseDelay.
	 */
	public final int getReuseDelay()
	{
		return _reuseDelay;
	}

	public final int getSkillTime()
	{
		return _skillTime;
	}

	public final int getSkillInterruptTime()
	{
		return _skillInterruptTime;
	}

	public final boolean isActive()
	{
		return _operateType == SkillOpType.OP_ACTIVE;
	}
	
	public final boolean isPassive()
	{
		return _operateType == SkillOpType.OP_PASSIVE;
	}
	
	public final boolean isToggle()
	{
		return _operateType == SkillOpType.OP_TOGGLE;
	}
	

	public final int getWeaponsAllowed()
	{
		return _weaponsAllowed;
	}
	
	public final int getCrossLearnAdd() {
		return _addCrossLearn;
	}
	public final float getCrossLearnMul() {
		return _mulCrossLearn;
	}
	public final float getCrossLearnRace() {
		return _mulCrossLearnRace;
	}
	public final float getCrossLearnProf() {
		return _mulCrossLearnProf;
	}
	public final boolean getCanLearn(ClassId cls) {
		return _canLearn == null || _canLearn.contains(cls);
	}
	public final boolean canTeachBy(int npcId) {
		return _teachers == null || _teachers.contains(npcId);
	}
	
	public final boolean isOffensive() {
		switch (_skillType)
		{
		case PDAM:
		case MDAM:
		case DOT:
		case BLEED:
		case POISON:
		case AGGDAMAGE:
		case DEBUFF:
		case STUN:
		case ROOT:
		case CONFUSION:
		case FEAR:
		case DRAIN:
		case SLEEP:
		case CHARGEDAM:
		case CONFUSE_MOB_ONLY:
		case DEATHLINK:
		case MANADAM:
		case MDOT:
		case MUTE:
		case SPOIL:
		case WEAKNESS:
	    case SWEEP:
			return true;
		default:
			return false;
		}
	}
	
//	int weapons[] = {L2Weapon.WEAPON_TYPE_ETC, L2Weapon.WEAPON_TYPE_BOW,
//			 L2Weapon.WEAPON_TYPE_POLE, L2Weapon.WEAPON_TYPE_DUALFIST,
//			 L2Weapon.WEAPON_TYPE_DUAL, L2Weapon.WEAPON_TYPE_BLUNT,
//			 L2Weapon.WEAPON_TYPE_SWORD, L2Weapon.WEAPON_TYPE_DAGGER};
	
	public final boolean getWeaponDependancy(L2Character activeChar)
	{
		int weaponsAllowed = getWeaponsAllowed();
		//check to see if skill has a weapon dependency.
		if(weaponsAllowed == 0)
			return true;
		if(activeChar.getActiveWeaponItem() != null)
		{
			L2WeaponType playerWeapon;
			playerWeapon = activeChar.getActiveWeaponItem().getItemType();
			int mask = playerWeapon.mask(); 
			if((mask & weaponsAllowed) != 0)
				return true;
			// can be on the secondary weapon
			playerWeapon = activeChar.getSecondaryWeaponItem().getItemType();
			mask = playerWeapon.mask();
			if((mask & weaponsAllowed) != 0)
				return true;
		}
		StringBuffer skillmsg = new StringBuffer();
		skillmsg.append(getName());
		skillmsg.append(" can only be used with weapons of type ");
		for(L2WeaponType wt : L2WeaponType.values())
		{
			if((wt.mask() & weaponsAllowed) != 0)
				skillmsg.append(wt).append('/');
		}
		skillmsg.setCharAt(skillmsg.length()-1, '.');
		SystemMessage message = new SystemMessage(614);
		message.addString(skillmsg.toString()); 
		activeChar.sendPacket(message); 
		
		return false;
	}
	
	public boolean checkCondition(L2Character activeChar)
	{
		if((getCondition() & L2Skill.COND_BEHIND) != 0)
		{
			if (!activeChar.isBehindTarget())
			return false;
		}
		
		if((getCondition() & L2Skill.COND_SHIELD) != 0)
		{
			/*
			L2Armor armorPiece;
			L2ItemInstance dummy;
			dummy = activeChar.getInventory().getPaperdollItem(Inventory.PAPERDOLL_RHAND);
			armorPiece = (L2Armor) dummy.getItem();
			*/
			//TODO add checks for shield here.
			
		}

		if (_preCondition == null)
			return true;
		Env env = new Env();
		env._player = activeChar;
		env._skill = this;
		if (!_preCondition.test(env))
		{
			String msg = _preCondition.getMessage();
			if (msg != null)
			{
				SystemMessage sm = new SystemMessage(614);
				sm.addString(msg);
				activeChar.sendPacket(sm);
			}
			return false;
		}
		return true;
	}
	
	public final L2Object[] getTargetList(L2Character activeChar)
	{
		ArrayList<L2Character> targetList = new ArrayList<L2Character>();
		SkillTargetType targetType = getTargetType();
		L2Character target = null;
		L2Object objTarget = activeChar.getTarget();
		SkillType skillType = getSkillType();

		if(objTarget instanceof L2Character)
		{
			target = (L2Character) objTarget;
		}
		
		
		switch(targetType)
		{
		case TARGET_ONE:
		{
			if(skillType == SkillType.BUFF || skillType == SkillType.HEAL || skillType == SkillType.MANAHEAL || skillType == SkillType.HOT || skillType == SkillType.REFLECT)
			{
				//if target is npc or null then just focus on self for heal and buff type skills
				L2PcInstance tgt = null;
				if(target instanceof L2PcInstance)
					tgt = (L2PcInstance)target;
				else if (activeChar instanceof L2PcInstance)
					tgt = (L2PcInstance)activeChar;
				
				if (tgt != null && !tgt.isAlikeDead())
					return new L2Character[]{tgt};
				
				activeChar.sendPacket(new SystemMessage(SystemMessage.TARGET_IS_INCORRECT));
				return null;
			}
			else
			{
				/*at this point we know the skill is a single target damage skill */
					//check for null target or friendly target
				if(target == null || (target instanceof L2FolkInstance) || target.isDead())
					{
						activeChar.sendPacket(new SystemMessage(SystemMessage.TARGET_IS_INCORRECT));
						return null;
					}
				return new L2Character[]{target};
			}
		}
		case TARGET_SELF:
		{
			return new L2Character[]{activeChar};
		}
		case TARGET_PET:
		{
			if (activeChar instanceof L2PcInstance) {
				L2Summon summon = ((L2PcInstance)activeChar).getPet();
				if (summon != null)
					return new L2Character[]{summon};
			}
			return null;
		}
		case TARGET_AURA:
		{
			L2Object[] possibleTargets = L2World.getInstance().getVisibleObjects3D(activeChar, 205);
			for(int i = 0;i < possibleTargets.length;i++)
			{
				if(possibleTargets[i] instanceof L2Attackable)
				{
					targetList.add((L2Character)possibleTargets[i]);
				}
			}
			return targetList.toArray(new L2Character[targetList.size()]);
		}
		case TARGET_AURA_CLOSE:
		{
			L2Object[] possibleTargets = L2World.getInstance().getVisibleObjects3D(activeChar, 80);
			for(int i = 0;i < possibleTargets.length;i++)
			{
				if(possibleTargets[i] instanceof L2Attackable)
				{
					targetList.add((L2Character)possibleTargets[i]);
				}
			}
			return targetList.toArray(new L2Character[targetList.size()]);
		}
		case TARGET_AREA:
		{
			if((!(target instanceof L2Attackable)
					&& !(target instanceof L2PcInstance)) || target.isAlikeDead())
			{
				activeChar.sendPacket(new SystemMessage(SystemMessage.TARGET_IS_INCORRECT));
				return null;
			}
			
			L2Object[] possibleTargets = L2World.getInstance().getVisibleObjects3D(target, 205);
			for(int i = 0;i < possibleTargets.length;i++)
			{
				if(possibleTargets[i] instanceof L2Attackable)
				{
					target = (L2Character) possibleTargets[i];
					if(!target.isAlikeDead())
					{
						targetList.add((L2Character)possibleTargets[i]);
					}
				}
			}
			targetList.add((L2Character)activeChar.getTarget());
			
			return targetList.toArray(new L2Character[targetList.size()]);
		}
		case TARGET_MULTIFACE:
		{
			if((!(target instanceof L2Attackable)
					&& !(target instanceof L2PcInstance)))
			{
				activeChar.sendPacket(new SystemMessage(SystemMessage.TARGET_IS_INCORRECT));
				return null;
			}
			
			targetList.add(target);
			L2Object[] possibleTargets = L2World.getInstance().getVisibleObjects3D(activeChar, 60);
			for(int i = 0;i < possibleTargets.length;i++)
			{
				if(possibleTargets[i] instanceof L2Attackable && possibleTargets[i] != target)
				{
					targetList.add((L2Character)possibleTargets[i]);
				}
				if(targetList.size() == 0)
				{
					activeChar.sendPacket(new SystemMessage(SystemMessage.TARGET_CANT_FOUND));
					return null;
				}
			}
			return targetList.toArray(new L2Character[targetList.size()]);
			//TODO multiface targets all around right now.  need it to just get targets
			//the character is facing.
		}
		case TARGET_PARTY:
		{
			if(activeChar instanceof L2PcInstance)
			{
				L2PcInstance player = (L2PcInstance) activeChar;
				L2Party party = player.getParty();
				if(party != null)
				{
					ArrayList<L2PcInstance> partyList = party.getPartyMembers();
					L2Character partyMember = null;
					for(int i = 0;i < partyList.size();i++)
					{
						partyMember = partyList.get(i);
						if(!partyMember.isDead())
						{
							targetList.add(partyMember);
						}
					}
				}
				else
				{
                    targetList.add(player);
                    //SystemMessage sm = new SystemMessage(614);
					//sm.addString("Skill Cancelled. You have no party.");
					//activeChar.sendPacket(sm);
				}
			}
			return targetList.toArray(new L2Character[targetList.size()]);
		}
		case TARGET_CLAN:
		{
			if(activeChar instanceof L2PcInstance)
			{
				L2PcInstance player = (L2PcInstance) activeChar;
				L2Clan clan = player.getClan();
				if(clan != null)
				{
					L2ClanMember[] clanMembers = clan.getMembers();
					for(int i = 0;i < clanMembers.length;i++)
					{
						L2PcInstance member = clanMembers[i].getPlayerInstance(); 
						if(member != null && member.isOnline()!=0 && !member.isDead())
						{
							targetList.add(clanMembers[i].getPlayerInstance());
						}
					}
				}
				else
				{
					activeChar.sendPacket(new SystemMessage(SystemMessage.TARGET_MUST_BE_IN_CLAN));
					return null;
				}
			}
			return targetList.toArray(new L2Character[targetList.size()]);
		}
		case TARGET_CORPSE_PLAYER:
		{
			if(!((target instanceof L2PcInstance) || (target instanceof L2PetInstance)) || !target.isDead())
			{
				activeChar.sendPacket(new SystemMessage(SystemMessage.TARGET_IS_INCORRECT));
				return null;
			}
			
			targetList.add(target);
			return targetList.toArray(new L2Object[targetList.size()]);
		}
		case TARGET_CORPSE_MOB:
		{
			if(!(target instanceof L2Attackable) || !target.isDead())
			{
				activeChar.sendPacket(new SystemMessage(SystemMessage.TARGET_IS_INCORRECT));
				return null;
			}
			targetList.add(target);
			return targetList.toArray(new L2Object[targetList.size()]);
		}
		case TARGET_CORPSE_CLAN:
		{	
			L2PcInstance player = (L2PcInstance) activeChar;
			L2Clan clan = player.getClan();
			if(clan != null)
			{
				L2ClanMember[] clanMembers = clan.getMembers();
				for(int i = 0;i < clanMembers.length;i++)
				{
					if(clanMembers[i].getPlayerInstance().isDead())
					{
						targetList.add(clanMembers[i].getPlayerInstance());
					}
				}
				return targetList.toArray(new L2Object[targetList.size()]);
			}
			else
			{
				activeChar.sendPacket(new SystemMessage(SystemMessage.TARGET_MUST_BE_IN_CLAN));
				return null;
			}
		}
		case TARGET_ITEM:
		{
			SystemMessage sm = new SystemMessage(614);
			sm.addString("Target type of skill is not currently handled");
			activeChar.sendPacket(sm);
			return null;
		}
		default:
		{
			SystemMessage sm = new SystemMessage(614);
			sm.addString("Target type of skill is not currently handled");
			activeChar.sendPacket(sm);
			return null;
		}
		}//end switch
	}

    public final Func[] getStatFuncs(L2Effect effect, L2Character player)
    {
		if(!(player instanceof L2PcInstance))
    		return _emptyFunctionSet;
    	if (_funcTemplates == null)
    		return _emptyFunctionSet;
    	ArrayList<Func> funcs = new ArrayList<Func>();
		for (FuncTemplate t : _funcTemplates) {
	    	Env env = new Env();
	    	env._player = player;
	    	env._skill = this;
	    	Func f = t.getFunc(env, this); // skill is owner
	    	if (f != null)
	    		funcs.add(f);
		}
    	if (funcs.size() == 0)
    		return _emptyFunctionSet;
    	return funcs.toArray(new Func[funcs.size()]);
    }

    
    public final L2Effect[] getEffects(L2Character effector, L2Character effected)
    {
    	if (isPassive())
    		return _emptyEffectSet;
    	if (_effectTemplates == null)
    		return _emptyEffectSet;
    	ArrayList<L2Effect> effects = new ArrayList<L2Effect>();
		for (EffectTemplate et : _effectTemplates) {
	    	Env env = new Env();
	    	env._player = effector;
	    	env._target = effected;
	    	env._skill = this;
	    	L2Effect e = et.getEffect(env);
	    	if (e != null)
	    		effects.add(e);
		}
    	if (effects.size() == 0)
    		return _emptyEffectSet;
    	return effects.toArray(new L2Effect[effects.size()]);
    }
    
    public final void attach(FuncTemplate f)
    {
    	if (_funcTemplates == null)
    	{
    		_funcTemplates = new FuncTemplate[]{f};
    	}
    	else
    	{
    		int len = _funcTemplates.length;
    		FuncTemplate[] tmp = new FuncTemplate[len+1];
    		System.arraycopy(_funcTemplates, 0, tmp, 0, len);
    		tmp[len] = f;
    		_funcTemplates = tmp;
    	}
    }

    public final void attach(EffectTemplate effect)
    {
    	if (_effectTemplates == null)
    	{
    		_effectTemplates = new EffectTemplate[]{effect};
    	}
    	else
    	{
    		int len = _effectTemplates.length;
    		EffectTemplate[] tmp = new EffectTemplate[len+1];
    		System.arraycopy(_effectTemplates, 0, tmp, 0, len);
    		tmp[len] = effect;
    		_effectTemplates = tmp;
    	}
    }

    public final void attach(Condition c)
    {
		_preCondition = c;
    }
}
