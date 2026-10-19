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

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.l2j.Config;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.model.ClassId;
import net.sf.l2j.gameserver.model.L2DropData;
import net.sf.l2j.gameserver.model.L2MinionData;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;
import net.sf.l2j.gameserver.templates.StatsSet;

/**
 * This class ...
 * 
 * @version $Revision: 1.8.2.6.2.9 $ $Date: 2005/04/06 16:13:25 $
 */
@SuppressWarnings({"nls","boxing"})
public class NpcTable
{
	private static Logger _log = Logger.getLogger(NpcTable.class.getName());

	private static NpcTable _instance;

	private HashMap<Integer, L2NpcTemplate> _npcs;
	private HashMap<String, L2NpcTemplate> _npcsNames;
	private boolean _initialized = false;

	public static NpcTable getInstance()
	{
		if (_instance == null)
			_instance = new NpcTable();
		return _instance;
	}

	private NpcTable()
	{
		this._npcs = new HashMap<Integer, L2NpcTemplate>();
		this._npcsNames = new HashMap<String, L2NpcTemplate>();
		RestoreNpcData();
	}

	/**
	 * 
	 */
	private void RestoreNpcData()
	{
		java.sql.Connection con = null;
		try
		{
			try
			{
				con = L2DatabaseFactory.getInstance().getConnection();
				PreparedStatement statement;
				statement = con.prepareStatement("SELECT id, name, class, collision_radius, collision_height, level, sex, type, attackrange, hp, mp, str, con, dex, `int`, wit, men, exp, sp, patk, pdef, matk, mdef, atkspd, aggro, matkspd, rhand, lhand, armor, walkspd, runspd, faction_id, faction_range, isUndead FROM npc");
				ResultSet npcdata = statement.executeQuery();

				fillNpcTable(npcdata);
				npcdata.close();
				statement.close();
			}
			catch (Exception e)
			{
				_log.log(Level.SEVERE, "error while creating npc table ", e);
			}

			try
			{
				con = L2DatabaseFactory.getInstance().getConnection();
				PreparedStatement statement = con.prepareStatement("SELECT npcid, skillid, level FROM npcskills");
				ResultSet npcskills = statement.executeQuery();
				L2NpcTemplate npcDat = null;
				L2Skill npcSkill = null;

				while(npcskills.next())
				{
					int mobId = npcskills.getInt("npcid");
					npcDat = this._npcs.get(mobId);
					if (npcDat == null)
						continue;
					int skillId = npcskills.getInt("skillid");
					int level = npcskills.getInt("level");
					if (npcDat.race == 0)
						if (skillId >= 4290 && skillId <= 4302)
						{
							npcDat.setRace(skillId);
							continue;
						}
					if (npcDat.rateHp == 1)
					{
						if (skillId >= 4303 && skillId <= 4310)
						{
							if (!Config.ENABLE_RATE_HP)
								continue;
							npcDat.setRateHp(skillId - 4301);
							continue;
						}
						else if (skillId == 4311)
						{
							if (!Config.ENABLE_RATE_HP)
								continue;
							npcDat.setRateHp(0.5);
							continue;
						}
					}

					npcSkill = SkillTable.getInstance().getInfo(skillId, level);
					if (npcSkill == null)
						continue;
					npcDat.addSkill(npcSkill);
				}
				npcskills.close();
				statement.close();
			}
			catch (Exception e)
			{
				_log.log(Level.SEVERE, "error while reading npcskills table ", e);
			}

			try
			{
				PreparedStatement statement2 = con.prepareStatement("SELECT mobId, itemId, min, max, sweep, chance FROM droplist ORDER BY mobId,chance DESC");
				ResultSet dropData = statement2.executeQuery();
				L2DropData dropDat = null;
				L2NpcTemplate npcDat = null;

				while(dropData.next())
				{
					int mobId = dropData.getInt("mobId");
					npcDat = this._npcs.get(mobId);
					dropDat = new L2DropData();

					dropDat.setItemId(dropData.getInt("itemId"));
					dropDat.setMinDrop(dropData.getInt("min"));
					dropDat.setMaxDrop(dropData.getInt("max"));
					dropDat.setSweep(dropData.getInt("sweep") == 1);
					dropDat.setChance(dropData.getInt("chance"));
					npcDat.addDropData(dropDat);
				}

				dropData.close();
				statement2.close();
			}
			catch (Exception e)
			{
				_log.log(Level.SEVERE, "error reading npc drops ", e);
			}

//			try
//			{
//				PreparedStatement statement3 = con.prepareStatement("SELECT npc_id, class_id FROM skill_learn");
//				ResultSet learndata = statement3.executeQuery();
//
//				while(learndata.next())
//				{
//					int npc_id = learndata.getInt("npc_id");
//					int class_id = learndata.getInt("class_id");
//					L2NpcTemplate npc = getTemplate(npc_id);
//					npc.addTeachInfo(ClassId.values()[class_id]);
//				}
//
//				learndata.close();
//				statement3.close();
//			}
//			catch (Exception e)
//			{
//				_log.log(Level.SEVERE, "error reading npc trainers ", e);
//			}
			try
			{
				PreparedStatement statement4 = con.prepareStatement("SELECT boss_id, minion_id, amount FROM minions");
				ResultSet minionData = statement4.executeQuery();
				L2MinionData minionDat = null;
				L2NpcTemplate npcDat = null;
				int cnt = 0;

				while(minionData.next())
				{
					int raidId = minionData.getInt("boss_id");
					npcDat = this._npcs.get(raidId);
					minionDat = new L2MinionData();
					minionDat.setMinionId(minionData.getInt("minion_id"));
					minionDat.setAmount(minionData.getInt("amount"));
					npcDat.addRaidData(minionDat);
					cnt++;
				}

				minionData.close();
				statement4.close();
				_log.config("NpcTable: Loaded " + cnt + " Minions.");
			}
			catch (Exception e)
			{
				_log.log(Level.SEVERE, "error loading minions", e);
			}
		}
		finally
		{
			try
			{
				con.close();
			}
			catch (Exception e)
			{}
		}

		this._initialized = true;
	}

	private void fillNpcTable(ResultSet NpcData) throws Exception
	{
		while(NpcData.next())
		{
			StatsSet npcDat = new StatsSet();
			int id = NpcData.getInt("id");
			if (Config.ASSERT)
				assert id < 1000000;

			npcDat.set("npcId", id);
			npcDat.set("level", NpcData.getInt("level"));
			npcDat.set("jClass", NpcData.getString("class"));

			npcDat.set("baseShldDef", 0);
			npcDat.set("baseShldRate", 0);
			npcDat.set("baseCritRate", 38);

			npcDat.set("name", NpcData.getString("name"));
			npcDat.set("collision_radius", NpcData.getDouble("collision_radius"));
			npcDat.set("collision_height", NpcData.getDouble("collision_height"));
			npcDat.set("sex", NpcData.getString("sex"));
			npcDat.set("type", NpcData.getString("type"));
			npcDat.set("baseAtkRange", NpcData.getInt("attackrange"));
			npcDat.set("revardExp", NpcData.getInt("exp"));
			npcDat.set("revardSp", NpcData.getInt("sp"));
			npcDat.set("basePAtkSpd", NpcData.getInt("atkspd"));
			npcDat.set("baseMAtkSpd", NpcData.getInt("matkspd"));
			npcDat.set("aggroRange", NpcData.getInt("aggro"));
			npcDat.set("rhand", NpcData.getInt("rhand"));
			npcDat.set("lhand", NpcData.getInt("lhand"));
			npcDat.set("armor", NpcData.getInt("armor"));
			npcDat.set("baseWalkSpd", NpcData.getInt("walkspd"));
			npcDat.set("baseRunSpd", NpcData.getInt("runspd"));

			npcDat.set("baseHpReg", 0.01);
			npcDat.set("baseCpReg", 0);
			npcDat.set("baseMpReg", 0.01);

			// constants, until we have stats in DB
			npcDat.set("baseSTR", NpcData.getInt("str"));
			npcDat.set("baseCON", NpcData.getInt("con"));
			npcDat.set("baseDEX", NpcData.getInt("dex"));
			npcDat.set("baseINT", NpcData.getInt("int"));
			npcDat.set("baseWIT", NpcData.getInt("wit"));
			npcDat.set("baseMEN", NpcData.getInt("men"));

			npcDat.set("baseHpMax", NpcData.getInt("hp"));
			npcDat.set("baseCpMax", 0);
			npcDat.set("baseMpMax", NpcData.getInt("mp"));
			npcDat.set("basePAtk", NpcData.getInt("patk"));
			npcDat.set("basePDef", NpcData.getInt("pdef"));
			npcDat.set("baseMAtk", NpcData.getInt("matk"));
			npcDat.set("baseMDef", NpcData.getInt("mdef"));

			npcDat.set("factionId", NpcData.getString("faction_id"));
			npcDat.set("factionRange", NpcData.getInt("faction_range"));

			npcDat.set("isUndead", NpcData.getInt("isUndead"));

			L2NpcTemplate template = new L2NpcTemplate(npcDat);
			this._npcs.put(id, template);
			this._npcsNames.put(NpcData.getString("name").toLowerCase(), template);
		}
		_log.config("NpcTable: Loaded " + this._npcs.size() + " Npc Templates.");
	}

	public void reloadNpc(int id)
	{
		java.sql.Connection con = null;
		try
		{
			// save a copy of the old data
			L2NpcTemplate old = getTemplate(id);
			HashMap<Integer, L2Skill> skills = new HashMap<Integer, L2Skill>();
			if (old.getSkills() != null)
				skills.putAll(old.getSkills());
			ArrayList<L2DropData> drops = new ArrayList<L2DropData>();
			if (old.getDropData() != null)
				drops.addAll(old.getDropData());
			ClassId[] classIds = null;
			if (old.getTeachInfo() != null)
				classIds = old.getTeachInfo().clone();
			ArrayList<L2MinionData> minions = new ArrayList<L2MinionData>();
			if (old.getMinionData() != null)
				minions.addAll(old.getMinionData());

			// reload the NPC base data
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement st = con.prepareStatement("SELECT id, name, class, collision_radius, collision_height, level, sex, type, attackrange, hp, mp, str, con, dex, `int`, wit, men, exp, sp, patk, pdef, matk, mdef, atkspd, aggro, matkspd, rhand, lhand, armor, walkspd, runspd, faction_id, faction_range, isUndead FROM npc WHERE id=?");
			st.setInt(1, id);
			ResultSet rs = st.executeQuery();
			fillNpcTable(rs);
			rs.close();
			st.close();

			// restore additional data from saved copy
			L2NpcTemplate created = getTemplate(id);
			for (L2Skill skill : skills.values())
				created.addSkill(skill);
			for (L2DropData drop : drops)
				created.addDropData(drop);
			if (classIds != null)
				for (ClassId classId : classIds)
					created.addTeachInfo(classId);
			for (L2MinionData minion : minions)
				created.addRaidData(minion);
		}
		catch (Exception e)
		{
			_log.warning("cannot reload npc " + id + ": " + e);
			e.printStackTrace();
		}
		finally
		{
			try
			{
				con.close();
			}
			catch (Exception e)
			{}
		}
	}

	// just wrapper
	public void reloadAllNpc()
	{
		RestoreNpcData();
	}

	public void saveNpc(StatsSet npc)
	{
		java.sql.Connection con = null;
		String query = "";
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			HashMap<String, Object> set = npc.getSet();
			String name = "";
			String values = "";
			for (Object obj : set.keySet())
			{
				name = (String) obj;
				if (!name.equalsIgnoreCase("npcId"))
				{
					if (values != "")
						values += ", ";
					values += name + " = '" + set.get(name) + "'";

				}
			}
			query = "UPDATE npc SET " + values + " WHERE id = ?";
			PreparedStatement statement = con.prepareStatement(query);
			statement.setInt(1, npc.getInteger("npcId"));
			statement.execute();
			statement.close();
		}
		catch (Exception e1)
		{
			// problem with storing spawn
			_log.warning("npc data couldnt be stored in db, query is :" + query + " : " + e1);
		}
		finally
		{
			try
			{
				con.close();
			}
			catch (Exception e)
			{}
		}
	}

	public boolean isInitialized()
	{
		return this._initialized;
	}

	public void replaceTemplate(L2NpcTemplate npc)
	{
		this._npcs.put(npc.npcId, npc);
		this._npcsNames.put(npc.name.toLowerCase(), npc);
	}

	public L2NpcTemplate getTemplate(int id)
	{
		return this._npcs.get(id);
	}

	public L2NpcTemplate getTemplateByName(String name)
	{
		return this._npcsNames.get(name.toLowerCase());
	}

	public L2NpcTemplate[] getAllOfLevel(int lvl)
	{
		ArrayList<L2NpcTemplate> list = new ArrayList<L2NpcTemplate>();
		for (L2NpcTemplate t : this._npcs.values())
		{
			if (t.level == lvl)
				list.add(t);
		}
		return list.toArray(new L2NpcTemplate[list.size()]);
	}
}
