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
import net.sf.l2j.gameserver.skills.L2SkillElemental;
import net.sf.l2j.gameserver.skills.L2SkillHealPercent;
import net.sf.l2j.gameserver.skills.L2SkillNegate;
import net.sf.l2j.gameserver.skills.L2SkillSeed;
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
        TARGET_ALLY,
        TARGET_CLAN,
        TARGET_PET,
        TARGET_AREA,
        TARGET_AURA,
        TARGET_CORPSE,
        TARGET_UNDEAD,
        TARGET_MULTIFACE,
        TARGET_CORPSE_ALLY,
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
        FATALBLOW,
        DOT,
        BLEED,
        POISON,
        HEAL,
        HOT,
        COMBATPOINTHEAL,
        CPHOT,
        MANAHEAL,
        MPHOT,
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
        CHARGE					(L2SkillCharge.class),
        FEAR,
        MHOT,
        DRAIN					(L2SkillDrain.class),
        NEGATE					(L2SkillNegate.class),
        CANCEL					(L2SkillNegate.class),
        SLEEP,
        AGGREDUCE,
        AGGREMOVE,
        AGGREDUCE_CHAR,
        CHARGEDAM				(L2SkillChargeDmg.class),
        CONFUSE_MOB_ONLY,
        DEATHLINK,
        DEATHLINK_PDAM,
        DETECT_WEAKNESS,
        ENCHANT_ARMOR,
        ENCHANT_WEAPON,
        FEED_PET,
        HEAL_PERCENT            (L2SkillHealPercent.class),
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
        SUMMON					(L2SkillSummon.class),
        WEAKNESS,
        DEATHLINK_PET,
        MANA_BY_LEVEL,
        FAKE_DEATH,
        UNBLEED,
        UNPOISON,
        SIEGEFLAG,
        TAKECASTLE,
        UNDEAD_DEFENSE,
        SEED					(L2SkillSeed.class),
        MDAM_ELEMENTAL			(L2SkillElemental.class),
        PARALYZE,
        DRAIN_SOUL,
        
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
    public final static int ELEMENT_UNHOLY = 5;
    public final static int ELEMENT_SACRED = 6;
    
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
    public final static int COMBAT_MOD_BEAST    = 201;
    public final static int COMBAT_MOD_BUG  = 202;
    public final static int COMBAT_MOD_DRAGON = 203;
    public final static int COMBAT_MOD_MONSTER= 204;
    public final static int COMBAT_MOD_PLANT    = 205;
    public final static int COMBAT_MOD_HOLY = 206;
    public final static int COMBAT_MOD_UNHOLY = 207;
    public final static int COMBAT_MOD_BOW  = 208;
    public final static int COMBAT_MOD_BLUNT    = 209;
    public final static int COMBAT_MOD_DAGGER = 210;
    public final static int COMBAT_MOD_FIST     = 211;
    public final static int COMBAT_MOD_DUAL     = 212;
    public final static int COMBAT_MOD_SWORD    = 213;
    public final static int COMBAT_MOD_POISON = 214;
    public final static int COMBAT_MOD_BLEED    = 215;
    public final static int COMBAT_MOD_FIRE = 216;
    public final static int COMBAT_MOD_WATER    = 217;
    public final static int COMBAT_MOD_EARTH    = 218;
    public final static int COMBAT_MOD_WIND = 219;
    public final static int COMBAT_MOD_ROOT = 220;
    public final static int COMBAT_MOD_STUN = 221;
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

    /** Identifier for a skill that client can't display */
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
    private final int _reuseDelay;
    private final int _buffDuration;
    
    private final SkillTargetType _targetType;
    private final double _power;
    
    // Effecting area of the skill, in radius.
    // The radius center varies according to the _targetType:
    // "caster" if targetType = AURA/PARTY/CLAN or "target" if targetType = AREA
    private final int _skillRadius;
    
    private final SkillType _skillType;

    private final boolean _ispotion;
    private final int _element;
    private final int _savevs;
    private final int _activateRate;
    private final int _levelModifier;
    private final int _magicLevel;
    
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
        _id           = set.getInteger("skill_id");
        _level        = set.getInteger("level");

	if ((_id >= 2001 && _id <= 2003) || _id == 2005 ||
	    (_id >= 2010 && _id <= 2012) || 
	    (_id >= 2032 && _id <= 2035) ||
	    _id == 2037 || _id == 2038 ||
	    _id == 2169 || _id == 4082)
		_ispotion = true;
	else
		_ispotion = false;


        _displayId    = set.getInteger("displayId", _id);
        _name         = set.getString("name");
        _operateType  = set.getEnum("operateType", SkillOpType.class);
        _magic        = set.getBool  ("isMagic", false);
        _mpConsume    = set.getInteger("mpConsume", 0);
        _hpConsume    = set.getInteger("hpConsume", 0);
        _itemConsume  = set.getInteger("itemConsumeCount", 0);
        _itemConsumeId= set.getInteger("itemConsumeId", 0);
        
        _castRange    = set.getInteger("castRange", 0);
        _skillTime    = set.getInteger("skillTime", 0);
        _skillInterruptTime = set.getInteger("skillInterruptTime", _skillTime/2);
        _reuseDelay   = set.getInteger("reuseDelay", 0);
        _buffDuration = set.getInteger("buffDuration", 0);
        
        _skillRadius  = set.getInteger("skillRadius", 80);
        
        _targetType   = set.getEnum("target", SkillTargetType.class);
        _power        = set.getFloat("power", 0.f);
        _stat         = set.getEnum ("stat", Stats.class, null);
        
        _skillType    = set.getEnum("skillType", SkillType.class);
        
        _element      = set.getInteger("element", 0);
        _savevs       = set.getInteger("save", 0);
        _activateRate = set.getInteger("activateRate", -1);
        _levelModifier= set.getInteger("levelModifier", 1);
        _magicLevel   = set.getInteger("magicLevel", 0);
        
        
        _condition       = set.getInteger("condition", 0);
        _conditionValue  = set.getInteger("conditionValue", 0);
        _overhit         = set.getBool  ("overHit", false);
        _weaponsAllowed  = set.getInteger("weaponsAllowed", 0);
        _armorsAllowed   = set.getInteger("armorsAllowed", 0);

        _addCrossLearn       = set.getInteger("addCrossLearn", 1000);
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
    

    public final boolean isPotion()
    {
        return _ispotion;
    }

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
    
	public final int getActivateRate() {
		return _activateRate;
	}

	public final int getLevelModifier() {
		return _levelModifier;
	}

	public final int getMagicLevel() {
		return _magicLevel;
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
    
    
    /**
     * Return the power of the skill.<BR><BR>
     */
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
    
    /**
     * Return the skill type (ex : BLEED, SLEEP, WATER...).<BR><BR>
     */
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
    
    public final int getSkillRadius()
    {
        return _skillRadius;
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
    
    public final boolean isPvpSkill() {
        switch (_skillType)
        {
        case DOT:
        case BLEED:
        case CONFUSION:
        case POISON:
        case DEBUFF:
        case STUN:
        case ROOT:
        case FEAR:
        case SLEEP:
        case MDOT:
        case MUTE:
        case WEAKNESS:
        case PARALYZE:
        case CANCEL:
            return true;
        default:
            return false;
        }
    }

	public final boolean isOffensive() {
		switch (_skillType)
		{
		case AGGDAMAGE:
		case AGGREDUCE:
		case AGGREMOVE:
		case BLEED:
		case CANCEL:
		case CHARGEDAM:
		case CONFUSE_MOB_ONLY:
		case CONFUSION:
		case DEATHLINK:
		case DEBUFF:
		case DOT:
		case DRAIN:
		case DRAIN_SOUL:
		case FATALBLOW:
		case FEAR:
		case MANA_BY_LEVEL:
		case MANADAM:
		case MDAM:
		case MDAM_ELEMENTAL:
		case MDOT:
		case MUTE:
		case PARALYZE:
		case PDAM:
		case POISON:
		case ROOT:
		case SLEEP:
		case SOULSHOT:
		case SPIRITSHOT:
		case SPOIL:
		case STUN:
		case SWEEP:
		case UNLOCK:
		case WEAKNESS:
			return true;
		case SUMMON:
			if (_targetType == SkillTargetType.TARGET_CORPSE)
				return true;
			return false;
		default:
			return false;
		}
	}
    
//  int weapons[] = {L2Weapon.WEAPON_TYPE_ETC, L2Weapon.WEAPON_TYPE_BOW,
//           L2Weapon.WEAPON_TYPE_POLE, L2Weapon.WEAPON_TYPE_DUALFIST,
//           L2Weapon.WEAPON_TYPE_DUAL, L2Weapon.WEAPON_TYPE_BLUNT,
//           L2Weapon.WEAPON_TYPE_SWORD, L2Weapon.WEAPON_TYPE_DAGGER};
    
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
            if (activeChar.getSecondaryWeaponItem() != null)
            {
                playerWeapon = activeChar.getSecondaryWeaponItem().getItemType();
                mask = playerWeapon.mask();
                if((mask & weaponsAllowed) != 0)
                    return true;
            }
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
        SystemMessage message = new SystemMessage(SystemMessage.S1_S2);
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
                SystemMessage sm = new SystemMessage(SystemMessage.S1_S2);
                sm.addString(msg);
                activeChar.sendPacket(sm);
            }
            return false;
        }
        return true;
    }
    
    
    /**
     * Return all targets of the skill in a table in function a the skill type.<BR><BR>
     * 
     * <B><U> Values of skill type</U> :</B><BR><BR>
     * <li>ONE : The skill can only be used on the L2PcInstance targeted, or on the caster if it's a L2PcInstance and no L2PcInstance targeted</li>
     * <li>SELF</li>
     * <li>HOLY, UNDEAD</li>
     * <li>PET</li>
     * <li>AURA, AURA_CLOSE</li>
     * <li>AREA</li>
     * <li>MULTIFACE</li>
     * <li>PARTY, CLAN</li>
     * <li>CORPSE_PLAYER, CORPSE_MOB, CORPSE_CLAN</li>
     * <li>UNLOCKABLE</li>
     * <li>ITEM</li><BR><BR>
     * 
     * @param activeChar The L2Character who use the skill
     * 
     */
    public final L2Object[] getTargetList(L2Character activeChar)
    {
    	return getTargetList(activeChar,activeChar.getTarget());
    }
    public final L2Object[] getTargetList(L2Character activeChar,L2Object _target)
    {
        ArrayList<L2Character> targetList = new ArrayList<L2Character>();
        
        // Get the target type of the skill 
        // (ex : ONE, SELF, HOLY, PET, AURA, AURA_CLOSE, AREA, MULTIFACE, PARTY, CLAN, CORPSE_PLAYER, CORPSE_MOB, CORPSE_CLAN, UNLOCKABLE, ITEM, UNDEAD)
        SkillTargetType targetType = getTargetType();
        
        // Init to null the target of the skill
        L2Character target = null;
        
        // Get the L2Objcet targeted by the user of the skill at this moment
        L2Object objTarget = _target;
        
        // Get the type of the skill
        // (ex : PDAM, MDAM, DOT, BLEED, POISON, HEAL, HOT, MANAHEAL, AGGDAMAGE, BUFF, DEBUFF, STUN, ROOT, RESURRECT, PASSIVE...)
        SkillType skillType = getSkillType();

        // If the L2Object targeted is a L2Character, it becomes the L2Character target
        if(objTarget instanceof L2Character)
        {
            target = (L2Character) objTarget;
        }
        
        
        switch(targetType)
        {
        // The skill can only be used on the L2Character targeted, or on the caster itself
        case TARGET_ONE:
        {
            if(target == null || target.isDead() || 
            (target == activeChar && !(
                    skillType == SkillType.BUFF
                    || skillType == SkillType.HEAL
                    || skillType == SkillType.HEAL_PERCENT
                    || skillType == SkillType.HOT
                    || skillType == SkillType.MANAHEAL
                    || skillType == SkillType.NEGATE
                    || skillType == SkillType.CANCEL
                    || skillType == SkillType.REFLECT
                    || skillType == SkillType.UNBLEED
                    || skillType == SkillType.UNPOISON
                    )))
            {
                activeChar.sendPacket(new SystemMessage(SystemMessage.TARGET_IS_INCORRECT));
                return null;
            }
            
            // If a target is found, return it in a table else send a system message TARGET_IS_INCORRECT
            return new L2Character[]{target};
            //}
        }
        case TARGET_SELF:
        {     
			return new L2Character[]{activeChar};
        }
        case TARGET_HOLY:
        {
		    if(activeChar instanceof L2PcInstance && target instanceof L2ArtefactInstance) // &&  L2Siege.checkIfOkToCastSealOfRule(activeChar, false)
                    return new L2Character[]{target};
		    return null;
        }
        case TARGET_PET:
        {
        	target = activeChar.getPet();
			if(target != null && !target.isDead())
                    return new L2Character[]{target};

			return null;
        }
		case TARGET_AURA:
		{
			for(L2Character targ : activeChar.getKnownCharacters(getSkillRadius(), 100))
			{
				if(!targ.isAttackable())
					continue;

				L2PcInstance pcActive = activeChar.getPcInstance();

				if(pcActive != null)
				{
					if(!pcActive.checkPvpSkill(targ, this))
						continue;

					L2PcInstance pcTarget = targ.getPcInstance();

					if(pcTarget != null)
					{
						if(pcActive.getClanId() != 0 && pcActive.getClanId() == pcTarget.getClanId())
							continue;
						if(pcActive.getParty() != null && pcActive.getParty() == pcTarget.getParty())
							continue;
					}
				}

				targetList.add(targ);
            }
            return targetList.toArray(new L2Character[targetList.size()]);
        }
        case TARGET_AREA:
        {
        	if(target == null || target == activeChar || target.isAlikeDead() || !target.isAttackable())
        	{
                activeChar.sendPacket(new SystemMessage(SystemMessage.TARGET_IS_INCORRECT));
                return null;
            }

            targetList.add(target);
			L2PcInstance pcActive = activeChar.getPcInstance();

			for(L2Character targ : target.getKnownCharacters(getSkillRadius(), 100))
			{
				if(targ.isAlikeDead())
					continue;

				if(pcActive != null)
				{
					if(!pcActive.checkPvpSkill(targ, this))
						continue;

					L2PcInstance pcTarget = targ.getPcInstance();

					if(pcTarget != null)
					{
						if(pcActive.getClanId() != 0 && pcActive.getClanId() == pcTarget.getClanId())
							continue;
						if(pcActive.getParty() != null && pcActive.getParty() == pcTarget.getParty())
							continue;
					}
				}
				targetList.add(targ);
			}

			return targetList.toArray(new L2Character[targetList.size()]);
		}
		case TARGET_MULTIFACE:
		{
			if(!target.isAttackable())
			{
				activeChar.sendPacket(new SystemMessage(SystemMessage.TARGET_IS_INCORRECT));
				return null;
			}

			for(L2Character targ : activeChar.getKnownCharacters(getSkillRadius(), 100))
			{
				if(!targ.isAttackable())
					continue;
				if(!activeChar.isInFront(targ, 60))
					continue;

				L2PcInstance pcActive = activeChar.getPcInstance();
				if(pcActive != null)
				{
					if(!pcActive.checkPvpSkill(targ, this))
						continue;

					L2PcInstance pcTarget = targ.getPcInstance();

					if(pcTarget != null)
					{
						if(pcActive.getClanId() != 0 && pcActive.getClanId() == pcTarget.getClanId())
							continue;
						if(pcActive.getParty() != null && pcActive.getParty() == pcTarget.getParty())
							continue;
					}
				}

				targetList.add(targ);
            }
            return targetList.toArray(new L2Character[targetList.size()]);
        }
		case TARGET_PARTY:
		{
			L2PcInstance player = activeChar.getPcInstance();
			L2Party party = player.getParty();
			if(party == null)
			{
				targetList.add(player);
				if (player.getPet() != null)
					targetList.add(player.getPet());
			}
			else
			{
				int charX, charY, charZ, targetX, targetY, targetZ, dx, dy, dz;
				int radius = getSkillRadius();

				charX = activeChar.getX();
				charY = activeChar.getY();
				charZ = activeChar.getZ();

				for(L2PcInstance partyMember : party.getPartyMembers())
				{
					if(partyMember.isDead())
						continue;

					targetX = partyMember.getX();
					targetY = partyMember.getY();
					targetZ = partyMember.getZ();

					dx = Math.abs(charX - targetX);
					dy = Math.abs(charY - targetY);
					dz = Math.abs(charZ - targetZ);

					if (dx*dx + dy*dy + dz*dz > radius*radius)
						continue;

					if (!player.checkPvpSkill(partyMember, this))
						continue;

					targetList.add(partyMember);
					if (partyMember.getPet() != null)
						targetList.add(partyMember.getPet());
				}
			}
			return targetList.toArray(new L2Character[targetList.size()]);
		}
		case TARGET_CORPSE_ALLY:
		case TARGET_CORPSE_CLAN:
		case TARGET_ALLY:
		case TARGET_CLAN:
		{
			L2PcInstance player = activeChar.getPcInstance();
			L2Clan clan = player.getClan();
			
			if(targetType != SkillTargetType.TARGET_CORPSE_ALLY && targetType != SkillTargetType.TARGET_CORPSE_CLAN)
			{
				targetList.add(player);
				if (player.getPet() != null)
					targetList.add(player.getPet());
			}

			if(clan != null)
			{
				if(player.getAllyId() == 0)
				{
					if(targetType == SkillTargetType.TARGET_ALLY)
						targetType = SkillTargetType.TARGET_CLAN;
					else if(targetType == SkillTargetType.TARGET_CORPSE_ALLY)
						targetType = SkillTargetType.TARGET_CORPSE_CLAN;
				}

				for(L2Character targ : activeChar.getKnownCharacters(getSkillRadius(), 200))
				{
					if (!(targ instanceof L2PcInstance))
						continue;
					L2PcInstance pcTarget = (L2PcInstance)targ;
					if (pcTarget.getClan() == null)
						continue;
					if ((targetType == SkillTargetType.TARGET_CLAN || targetType == SkillTargetType.TARGET_CORPSE_CLAN) && pcTarget.getClan() != player.getClan())
						continue;
					if ((targetType == SkillTargetType.TARGET_ALLY || targetType == SkillTargetType.TARGET_CORPSE_ALLY) && pcTarget.getAllyId() != player.getAllyId())
						continue;
					if ((targetType == SkillTargetType.TARGET_CORPSE_CLAN || targetType == SkillTargetType.TARGET_CORPSE_ALLY) && !pcTarget.isDead())
						continue;
					if (!player.checkPvpSkill(pcTarget, this))
						continue;

					targetList.add(pcTarget);
					if ((targetType == SkillTargetType.TARGET_ALLY || targetType == SkillTargetType.TARGET_CLAN) && (pcTarget.getPet() != null))
						targetList.add(pcTarget.getPet());
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
            
            return new L2Character[]{target};
        }
		case TARGET_AREA_CORPSE_MOB:
		{
			if(!target.isDead())
			{
				activeChar.sendPacket(new SystemMessage(SystemMessage.TARGET_IS_INCORRECT));
				return null;
			}

			for(L2Character targ : target.getKnownCharacters(getSkillRadius(), getSkillRadius()))
			{
			    if(targ instanceof L2Attackable && !targ.isAlikeDead())
			        targetList.add(targ);
			}
			return targetList.toArray(new L2Object[targetList.size()]);
		}
		case TARGET_CORPSE_MOB:
        {
            if(!(target instanceof L2Attackable) || !target.isDead())
            {
                activeChar.sendPacket(new SystemMessage(SystemMessage.TARGET_IS_INCORRECT));
                return null;
            }
            return new L2Character[]{target};
        }
        case TARGET_UNLOCKABLE:
        {
            if(!(target instanceof L2DoorInstance))
            {
                activeChar.sendPacket(new SystemMessage(SystemMessage.TARGET_IS_INCORRECT));
                return null;
            }
            
            return new L2Character[]{target};
        }       
        case TARGET_UNDEAD:
        {
            if (target instanceof L2NpcInstance)
            {
                if(!(((L2NpcInstance)target).isUndead() && !(target.isDead()) ))
                {
                    activeChar.sendPacket(new SystemMessage(SystemMessage.TARGET_IS_INCORRECT));
                    return null;
                }
                return new L2Character[]{target};
            }
            activeChar.sendMessage("It's not undead target");
            return null;
        }
        case TARGET_ITEM:
        default:
        {
            activeChar.sendMessage("Target type of skill is not currently handled");
            return null;
        }
        }//end switch
    }

    public final Func[] getStatFuncs(@SuppressWarnings("unused") L2Effect effect, L2Character player)
    {
        //if(!(player instanceof L2PcInstance))
        //    return _emptyFunctionSet;
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
        
        if (effected instanceof L2PcInstance){
            L2PcInstance targetplayer = (L2PcInstance)effected;
            //No effect on invulnerable players unless they cast it themselves.
            if ((effector != effected) && targetplayer.isInvul()){
               return _emptyEffectSet;
            }
        }
        
        ArrayList<L2Effect> effects = new ArrayList<L2Effect>();
        
        for (EffectTemplate et : _effectTemplates) 
        {
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

    public String toString()
    {
        return ""+_name+"[id="+_id+",lvl="+_level+"]";
    }
}
