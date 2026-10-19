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
package net.sf.l2j.gameserver;



import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2Skill.SkillType;
import net.sf.l2j.gameserver.skills.SkillsEngine;
import net.sf.l2j.gameserver.templates.L2WeaponType;
import net.sf.l2j.gameserver.templates.StatsSet;




/**
 * This class ...
 * 
 * @version $Revision: 1.8.2.6.2.18 $ $Date: 2005/04/06 16:13:25 $
 */
public class SkillTable
{
	private static Logger _log = Logger.getLogger(SkillTable.class.getName());
	private static SkillTable _instance;
	
	private HashMap<Integer, L2Skill> _skills;
	private boolean _initialized = true;
	public static SkillTable getInstance()
	{
		if (_instance == null)
		{
			_instance = new SkillTable();
		}
		return _instance;
	}
	
	private void makeSkills(int skillId, String name, ArrayList<StatsSet> sets)
	{
		try
		{
			StatsSet[] setarr = sets.toArray(new StatsSet[sets.size()]);
			sets.clear();
			L2Skill[] skills = SkillsEngine.getInstance().loadSkills(skillId, name, setarr);
			if (skills == null)
			{
				skills = new L2Skill[setarr.length];
				for (int i=0; i < setarr.length; i++)
					skills[i] = setarr[i].getEnum("skillType", SkillType.class).makeSkill(setarr[i]);
			}
			for (L2Skill skill : skills)
				_skills.put(skill.getId()*100+ skill.getLevel(), skill);
		} catch (Exception e) {
			_log.log(Level.SEVERE, "error while creating skill table ", e);
		}
	}
	private SkillTable()
	{
		_skills = new HashMap<Integer, L2Skill>();
//		java.sql.Connection con = null;
//		try
//		{
//			con = L2DatabaseFactory.getInstance().getConnection();
//			PreparedStatement statement = con.prepareStatement(
//					"SELECT skill_id, level, name, operateType, isMagic," +
//					" mpConsume, hpConsume, itemConsumeId, itemConsumeCount," +
//					" castRange, skillTime, reuseDelay, buffDuration, hitTime," +
//					" target, power, skillType, element, save, overHit," +
//					" weapon_type, condition, conditionValue " +
//					"FROM skills ORDER BY skill_id, level");
//			ResultSet rset = statement.executeQuery();
//				
//			ArrayList<StatsSet> sets = new ArrayList<StatsSet>();
//			String name = "";
//			String str;
//			String opType;
//			int last_skill_id = -1;
//			int last_skill_level = 0;
//					
//			while (rset.next())
//			{
//				int skill_id = rset.getInt("skill_id");
//				int _level = rset.getInt("level");
//				String _name = rset.getString("name");
//				if (skill_id != last_skill_id)
//				{
//					if (!sets.isEmpty())
//						makeSkills(last_skill_id, name, sets);
//					last_skill_id = skill_id;
//					name = _name;
//					last_skill_level = 1;
//				}
//				else
//				{
//					if (Config.ASSERT) assert name.equals(_name);
//					if (Config.ASSERT) assert last_skill_level+1 == _level;
//					last_skill_level++;
//				}
//				StatsSet set = new StatsSet();
//				sets.add(set);
//				
//				set.set("skill_id", skill_id);
//				set.set("level",    _level);
//				set.set("name",     _name );
//				
//				opType = rset.getString("operateType");
//				if      (opType.equalsIgnoreCase("once"))    set.set("operateType", SkillOpType.OP_ACTIVE);
//				else if (opType.equalsIgnoreCase("always"))  set.set("operateType", SkillOpType.OP_PASSIVE);
//				else if (opType.equalsIgnoreCase("duration"))set.set("operateType", SkillOpType.OP_ACTIVE);
//				else if (opType.equalsIgnoreCase("toggle"))  set.set("operateType", SkillOpType.OP_TOGGLE);
//				else
//					throw new IllegalStateException("Bad operateType "+opType+" for skill id="+skill_id);
//				
//				set.set("isMagic", Boolean.valueOf(rset.getString("isMagic")).booleanValue());
//				set.set("overHit", rset.getString("overHit").equalsIgnoreCase("yes"));
//				
//				set.set("mpConsume",        rset.getInt("mpConsume"));
//				set.set("hpConsume",        rset.getInt("hpConsume"));
//				set.set("itemConsumeId",    rset.getInt("itemConsumeId"));
//				set.set("itemConsumeCount", rset.getInt("itemConsumeCount"));
//				
//				set.set("castRange",        rset.getInt("castRange"));
//				set.set("skillTime",        rset.getInt("skillTime"));
//				set.set("reuseDelay",       rset.getInt("reuseDelay"));
//				set.set("buffDuration",     rset.getInt("buffDuration"));
//				set.set("hitTime",          rset.getInt("hitTime"));
//				set.set("power",            rset.getDouble("power"));
//				set.set("weaponsAllowed",   calcWeaponsAllowed(rset.getInt("weapon_type")));
//				//skill.setWeaponsAllowed(calcWeaponsAllowed(skill));
//
//					str = rset.getString("target");
//				SkillTargetType st_val;
//				if      (str.equalsIgnoreCase("no target"))       st_val = SkillTargetType.TARGET_NONE;
//				else if (str.equalsIgnoreCase("none"))            st_val = SkillTargetType.TARGET_NONE;
//				else if (str.equalsIgnoreCase("NOTDONE"))         st_val = SkillTargetType.TARGET_NONE;
//				else if (str.equalsIgnoreCase("self"))            st_val = SkillTargetType.TARGET_SELF;
//				else if (str.equalsIgnoreCase("one"))             st_val = SkillTargetType.TARGET_ONE;
//				else if (str.equalsIgnoreCase("party"))           st_val = SkillTargetType.TARGET_PARTY;
//				else if (str.equalsIgnoreCase("clan"))            st_val = SkillTargetType.TARGET_CLAN;
//				else if (str.equalsIgnoreCase("pet"))             st_val = SkillTargetType.TARGET_PET;
//				else if (str.equalsIgnoreCase("area"))            st_val = SkillTargetType.TARGET_AREA;
//				else if (str.equalsIgnoreCase("aura"))            st_val = SkillTargetType.TARGET_AURA;
//				else if (str.equalsIgnoreCase("corpse"))          st_val = SkillTargetType.TARGET_CORPSE;
//				else if (str.equalsIgnoreCase("undead"))          st_val = SkillTargetType.TARGET_UNDEAD;
//				else if (str.equalsIgnoreCase("aura_close"))      st_val = SkillTargetType.TARGET_AURA_CLOSE;
//				else if (str.equalsIgnoreCase("multiface"))       st_val = SkillTargetType.TARGET_MULTIFACE;
//				else if (str.equalsIgnoreCase("no target"))       st_val = SkillTargetType.TARGET_SELF;
//				else if (str.equalsIgnoreCase("mob_corpse"))      st_val = SkillTargetType.TARGET_CORPSE_MOB;
//				else if (str.equalsIgnoreCase("corpse_clan"))     st_val = SkillTargetType.TARGET_CORPSE_CLAN;
//				else if (str.equalsIgnoreCase("player_corpse"))   st_val = SkillTargetType.TARGET_CORPSE_PLAYER;
//				else if (str.equalsIgnoreCase("mob_corpse_area")) st_val = SkillTargetType.TARGET_CORPSE_MOB;
//				else if (str.equalsIgnoreCase("item"))            st_val = SkillTargetType.TARGET_ITEM;
//				else if (str.equalsIgnoreCase("unlockable"))      st_val = SkillTargetType.TARGET_UNLOCKABLE;
//				else if (str.equalsIgnoreCase("holy"))            st_val = SkillTargetType.TARGET_HOLY;
//				else
//					throw new IllegalStateException("Bad target type "+str+" for skill id="+skill_id);
//				if (st_val == SkillTargetType.TARGET_NONE)
//				{
//					if (set.getEnum("operateType", SkillOpType.class) != SkillOpType.OP_ACTIVE)
//						st_val = SkillTargetType.TARGET_SELF;
//				}
//				set.set("target", st_val);
//
//				SkillType val_st;
//				str = rset.getString("skillType");
//				if     (str.equalsIgnoreCase("pdam"))             val_st = SkillType.PDAM; 
//				else if(str.equalsIgnoreCase("chargedam"))        val_st = SkillType.CHARGEDAM;
//				else if(str.equalsIgnoreCase("mdam"))             val_st = SkillType.MDAM;
//				else if(str.equalsIgnoreCase("aggdam"))           val_st = SkillType.AGGDAMAGE;
//				else if(str.equalsIgnoreCase("aggreduce"))        val_st = SkillType.AGGREDUCE;
//				else if(str.equalsIgnoreCase("aggreducechar"))    val_st = SkillType.AGGREDUCE_CHAR;
//				else if(str.equalsIgnoreCase("aggrem"))           val_st = SkillType.AGGREMOVE;
//				else if(str.equalsIgnoreCase("dot"))              val_st = SkillType.DOT;
//				else if(str.equalsIgnoreCase("bleed"))            val_st = SkillType.BLEED;
//				else if(str.equalsIgnoreCase("unbleed"))          val_st = SkillType.UNBLEED;
//				else if(str.equalsIgnoreCase("poison"))           val_st = SkillType.POISON;
//				else if(str.equalsIgnoreCase("unpoison"))         val_st = SkillType.UNPOISON;
//				else if(str.equalsIgnoreCase("hot"))              val_st = SkillType.HOT;
//				else if(str.equalsIgnoreCase("heal"))             val_st = SkillType.HEAL;
//				else if(str.equalsIgnoreCase("manaheal"))         val_st = SkillType.MANAHEAL;
//				else if(str.equalsIgnoreCase("MHoT"))             val_st = SkillType.MHOT;
//				else if(str.equalsIgnoreCase("confusion"))        val_st = SkillType.CONFUSION;
//				else if(str.equalsIgnoreCase("unlock"))           val_st = SkillType.UNLOCK;
//				else if(str.equalsIgnoreCase("charge"))           val_st = SkillType.CHARGE;
//				else if(str.equalsIgnoreCase("fear"))             val_st = SkillType.FEAR;
//				else if(str.equalsIgnoreCase("resurrect"))        val_st = SkillType.RESURRECT;
//				else if(str.equalsIgnoreCase("buff"))             val_st = SkillType.BUFF;
//				else if(str.equalsIgnoreCase("debuff"))           val_st = SkillType.DEBUFF;
//				else if(str.equalsIgnoreCase("root"))             val_st = SkillType.ROOT;
//				else if(str.equalsIgnoreCase("stun"))             val_st = SkillType.STUN;
//				else if(str.equalsIgnoreCase("cont"))             val_st = SkillType.CONT;
//				else if(str.equalsIgnoreCase("passive"))          val_st = SkillType.PASSIVE;
//				else if(str.equalsIgnoreCase("drain"))            val_st = SkillType.DRAIN;
//				else if(str.equalsIgnoreCase("negate"))           val_st = SkillType.NEGATE;
//				else if(str.equalsIgnoreCase("sleep"))            val_st = SkillType.SLEEP;
//                else if(str.equalsIgnoreCase("summon"))           val_st = SkillType.SUMMON;
//                else if(str.equalsIgnoreCase("spoil"))            val_st = SkillType.SPOIL;
//                else if(str.equalsIgnoreCase("sweep"))            val_st = SkillType.SWEEP;
//                else if(str.equalsIgnoreCase("fakedeath"))        val_st = SkillType.FAKE_DEATH;
//                else if(str.equalsIgnoreCase("detectweakness"))   val_st = SkillType.DETECT_WEAKNESS;
//                else if(str.equalsIgnoreCase("weakness"))         val_st = SkillType.WEAKNESS;
//                else if(str.equalsIgnoreCase("reflect"))          val_st = SkillType.REFLECT;
//                else if(str.equalsIgnoreCase("healper"))          val_st = SkillType.HEAL_PERCENT;
//                else if(str.equalsIgnoreCase("luck"))             val_st = SkillType.LUCK;
//                else if(str.equalsIgnoreCase("recall"))           val_st = SkillType.RECALL;
//                else if(str.equalsIgnoreCase("mute"))             val_st = SkillType.MUTE;
//                else if(str.equalsIgnoreCase("MDoT"))             val_st = SkillType.MDOT;
//                else if(str.equalsIgnoreCase("deathlink"))        val_st = SkillType.DEATHLINK;
//                else if(str.equalsIgnoreCase("confusemobonly"))   val_st = SkillType.CONFUSE_MOB_ONLY;
//                else if(str.equalsIgnoreCase("enchantweapon"))    val_st = SkillType.ENCHANT_WEAPON;
//                else if(str.equalsIgnoreCase("enchantarmor"))     val_st = SkillType.ENCHANT_ARMOR;
//                else if(str.equalsIgnoreCase("soulshot"))         val_st = SkillType.SOULSHOT;
//                else if(str.equalsIgnoreCase("spiritshot"))       val_st = SkillType.SPIRITSHOT;
//                else if(str.equalsIgnoreCase("feedpet"))          val_st = SkillType.FEED_PET;
//                else if(str.equalsIgnoreCase("manadam"))          val_st = SkillType.MANADAM;
//                else if(str.equalsIgnoreCase("NOTDONE"))          val_st = SkillType.NOTDONE;
//				else
//					throw new IllegalStateException("Bad skill type "+str+" for skill id="+skill_id);
//				set.set("skillType", val_st);
//				
//				int val;
//				str = rset.getString("element");
//				if     (str.equalsIgnoreCase("wind"))             val = L2Skill.ELEMENT_WIND;
//				else if(str.equalsIgnoreCase("water"))            val = L2Skill.ELEMENT_WATER;
//				else if(str.equalsIgnoreCase("fire"))             val = L2Skill.ELEMENT_FIRE;
//				else if(str.equalsIgnoreCase("earth"))            val = L2Skill.ELEMENT_EARTH;
//				else if(str.equalsIgnoreCase("holy"))             val = L2Skill.ELEMENT_HOLY;
//				else                                              val = 0;
//				set.set("element", val);
//				
//				str = rset.getString("save");
//				if     (str.equalsIgnoreCase("WIT"))              val = L2Skill.SAVEVS_WIT;
//				else if(str.equalsIgnoreCase("MEN"))              val = L2Skill.SAVEVS_MEN;
//				else if(str.equalsIgnoreCase("INT"))              val = L2Skill.SAVEVS_INT;
//				else if(str.equalsIgnoreCase("CON"))              val = L2Skill.SAVEVS_CON;
//				else if(str.equalsIgnoreCase("DEX"))              val = L2Skill.SAVEVS_DEX;
//				else if(str.equalsIgnoreCase("STR"))              val = L2Skill.SAVEVS_STR;
//				else                                              val = 0;
//				set.set("save", val);
//				
//				val = 0;
//				str = rset.getString("condition");
//				StringTokenizer st = new StringTokenizer(str, " ,;");
//				while (st.hasMoreTokens())
//				{
//					str = st.nextToken();
//					if     (str.equalsIgnoreCase("running")) val |= L2Skill.COND_RUNNING;
//					else if(str.equalsIgnoreCase("walking")) val |= L2Skill.COND_WALKING;
//					else if(str.equalsIgnoreCase("sitting")) val |= L2Skill.COND_SIT;
//					else if(str.equalsIgnoreCase("behind"))  val |= L2Skill.COND_BEHIND;
//					else if(str.equalsIgnoreCase("crit"))    val |= L2Skill.COND_CRIT;
//					else if(str.equalsIgnoreCase("lowhp"))   val |= L2Skill.COND_LOWHP;
//					else if(str.equalsIgnoreCase("charges")) val |= L2Skill.COND_CHARGES;
//				}
//				set.set("condition", val);
//				
//				set.set("conditionValue", rset.getInt("conditionValue"));
//				
////				Integer[] armorTypes1 = calcArmorsAllowed(rset.getInt("armorType1"));
////				Integer[] armorTypes2 = calcArmorsAllowed(rset.getInt("armorType2"));
////				Integer[] armorTypes3 = calcArmorsAllowed(rset.getInt("armorType3"));
////				Integer[] armorTypes4 = calcArmorsAllowed(rset.getInt("armorType4"));
////				Integer[] armorTypes5 = calcArmorsAllowed(rset.getInt("armorType5"));
////				
////				Integer[] armorsAllowed[] = {armorTypes1, armorTypes2, armorTypes3, armorTypes4, armorTypes5};
////					
////				skill.setArmorsAllowed(armorsAllowed);
//			
//			}
//			//_log.config("created " + _skills.size() + " skills");
//			rset.close();
//			statement.close();
//		}
//		catch (Exception e)
//		{
//			_log.severe("error while creating skill table "+e);
//			e.printStackTrace();
//		} 
//		finally 
//		{
//			try { con.close(); } catch (Exception e) {}
//		}
		SkillsEngine.getInstance().loadAllSkills(_skills);
	}
	public boolean isInitialized()
	{
	    return _initialized;
	}
	
//	private void exportSkill(ArrayList<L2Skill> skills)
//	{
//		if (skills.isEmpty())
//			return;
//		final String name = skills.get(0).getName();
//		final int size = skills.size();
//		StringBuffer fname = new StringBuffer();
//		fname.append(new DecimalFormat("0000").format(skills.get(0).getId()));
//		fname.append('_').append(name);
//		fname.setCharAt(5, Character.toUpperCase(fname.charAt(5)));
//		for (int i=5; i < fname.length(); i++)
//		{
//			if (fname.charAt(i) == ' ') {
//				fname.deleteCharAt(i);
//				fname.setCharAt(i, Character.toUpperCase(fname.charAt(i)));
//				i--;
//			}
//			else if (fname.charAt(i) == '_') {
//				fname.deleteCharAt(i);
//				fname.setCharAt(i, Character.toUpperCase(fname.charAt(i)));
//			}
//			else if (fname.charAt(i) == '?') {
//				fname.deleteCharAt(i);
//				i--;
//			}
//			else if (fname.charAt(i) == '\'') {
//				fname.deleteCharAt(i);
//				fname.deleteCharAt(i);
//				i--;
//			}
//			else if (fname.charAt(i) == '*') {
//				fname.setCharAt(i, '_');
//				fname.setCharAt(i+1, Character.toUpperCase(fname.charAt(i+1)));
//			}
//		}
//		try {
//			File file = new File(Config.DATAPACK_ROOT, "data/export/skill/"+fname+".xml");
//			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8")); 
//			out.write("<?xml version='1.0' encoding='utf-8'?>\n");
//			out.write("<skill id='"+skills.get(0).getId()+"' levels='"+size+"' name=\""+name+"\">\n\n");
//
//			out.write("  <for>\n\n");
//			out.write("  </for>\n\n");
//			out.write("</skill>\n");
//			out.close();
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}
	
	public L2Skill getInfo(int magicId, int level)
	{
		return _skills.get(magicId*100 + level);
	}
	
//	public int parseEffectedStat(String stat1)
//	{
//		if(stat1.equalsIgnoreCase("patk"))
//		{
//			return(L2Skill.STAT_PATK);
//		}
//		else if(stat1.equalsIgnoreCase("pdef"))
//		{
//			return(L2Skill.STAT_PDEF);
//		}
//		else if(stat1.equalsIgnoreCase("matk"))
//		{
//			return(L2Skill.STAT_MATK);
//		}
//		else if(stat1.equalsIgnoreCase("mdef"))
//		{
//			return(L2Skill.STAT_MDEF);
//		}
//		else if(stat1.equalsIgnoreCase("maxhp"))
//		{
//			return(L2Skill.STAT_MAXHP);
//		}
//		else if(stat1.equalsIgnoreCase("maxmp"))
//		{
//			return(L2Skill.STAT_PDEF);
//		}
//		else if(stat1.equalsIgnoreCase("hp"))
//		{
//			return(L2Skill.STAT_CURHP);
//		}
//		else if(stat1.equalsIgnoreCase("mp"))
//		{
//			return(L2Skill.STAT_CURMP);
//		}
//		else if(stat1.equalsIgnoreCase("hpregen"))
//		{
//			return(L2Skill.STAT_HPREGEN);
//		}
//		else if(stat1.equalsIgnoreCase("mpregen"))
//		{
//			return(L2Skill.STAT_MPREGEN);
//		}
//		else if(stat1.equalsIgnoreCase("castingspd"))
//		{
//			return(L2Skill.STAT_CASTINGSPEED);
//		}
//		else if(stat1.equalsIgnoreCase("atkspd"))
//		{
//			return(L2Skill.STAT_ATKSPD);
//		}
//		else if(stat1.equalsIgnoreCase("critdam"))
//		{
//			return(L2Skill.STAT_CRITDAM);
//		}
//		else if(stat1.equalsIgnoreCase("critrate"))
//		{
//			return(L2Skill.STAT_CRITRATE);
//		}
//		else if(stat1.equalsIgnoreCase("fireres"))
//		{
//			return(L2Skill.STAT_FIRERES);
//		}
//		else if(stat1.equalsIgnoreCase("windres"))
//		{
//			return(L2Skill.STAT_WINDRES);
//		}
//		else if(stat1.equalsIgnoreCase("waterres"))
//		{
//			return(L2Skill.STAT_WATERRES);
//		}
//		else if(stat1.equalsIgnoreCase("earthres"))
//		{
//			return(L2Skill.STAT_EARTHRES);
//		}
//		else if(stat1.equalsIgnoreCase("rootres"))
//		{
//			return(L2Skill.STAT_ROOTRES);
//		}
//		else if(stat1.equalsIgnoreCase("sleepres"))
//		{
//			return(L2Skill.STAT_SLEEPRES);
//		}
//		else if(stat1.equalsIgnoreCase("confusionres"))
//		{
//			return(L2Skill.STAT_CONFUSIONRES);
//		}
//		else if(stat1.equalsIgnoreCase("breath"))
//		{
//			return(L2Skill.STAT_BREATH);
//		}
//		else if(stat1.equalsIgnoreCase("aggression"))
//		{
//			return(L2Skill.STAT_AGGRESSION);
//		}
//		else if(stat1.equalsIgnoreCase("bleed"))
//		{
//			return(L2Skill.TYPE_BLEED);
//		}
//		else if(stat1.equalsIgnoreCase("poison"))
//		{
//			return(L2Skill.TYPE_POISON);
//		}
//		else if(stat1.equalsIgnoreCase("stun"))
//		{
//			return(L2Skill.TYPE_STUN);
//		}
//		else if(stat1.equalsIgnoreCase("root"))
//		{
//			return(L2Skill.TYPE_ROOT);
//		}
//		else if(stat1.equalsIgnoreCase("DOT"))
//		{
//			return(L2Skill.TYPE_DOT);
//		}
//		else if(stat1.equalsIgnoreCase("MDOT"))
//		{
//			return(L2Skill.TYPE_MDOT);
//		}
//		else if(stat1.equalsIgnoreCase("MDOTLEVEL"))
//		{
//			return(L2Skill.TYPE_MANA_BY_LEVEL);
//		}
//		else if(stat1.equalsIgnoreCase("accuracy"))
//		{
//			return(L2Skill.STAT_ACCURACY);
//		}
//		else if(stat1.equalsIgnoreCase("evasion"))
//		{
//			return(L2Skill.STAT_EVASION);
//		}
//		else
//		{
//			return 0;
//		}
//	}
	
//	public int parseEffect(String effect1)
//	{
//		if(effect1.equalsIgnoreCase("buff"))
//		{
//			return(L2Skill.EFFECT_BUFF);
//		}
//		else if(effect1.equalsIgnoreCase("debuff"))
//		{
//			return(L2Skill.EFFECT_DEBUFF);
//		}
//		else if(effect1.equalsIgnoreCase("reduce"))
//		{
//			return(L2Skill.EFFECT_REDUCE);
//		}
//		else if(effect1.equalsIgnoreCase("restore"))
//		{
//			return(L2Skill.EFFECT_RESTORE);
//		}
//		
//		return 0;
//	}
	
	private static final L2WeaponType[] weaponDbMasks = {
		L2WeaponType.ETC,
		L2WeaponType.BOW,
		L2WeaponType.POLE,
		L2WeaponType.DUALFIST,
		L2WeaponType.DUAL,
		L2WeaponType.BLUNT,
		L2WeaponType.SWORD,
		L2WeaponType.DAGGER
		};
	
	public int calcWeaponsAllowed(int mask)
	{
		if (mask == 0)
			return 0;
		int weaponsAllowed = 0;
		for (int i=0; i < weaponDbMasks.length; i++)
		{
			if ((mask & (1<<i)) != 0)
				weaponsAllowed |= weaponDbMasks[i].mask();
		}
		return weaponsAllowed;
	}
}
