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
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;
import net.sf.l2j.gameserver.templates.StatsSet;

/**
 * This class ...
 * 
 * @version $Revision: 1.8.2.6.2.9 $ $Date: 2005/04/06 16:13:25 $
 */
public class NpcTable
{
	private static Logger _log = Logger.getLogger(NpcTable.class.getName());

	private static NpcTable _instance;

	private HashMap<Integer, L2NpcTemplate> _npcs;
	private HashMap<String, L2NpcTemplate> _npcsNames;
	private boolean _initialized = true;

	public static NpcTable getInstance()
	{
		if (_instance == null)
		{
			_instance = new NpcTable();
		}
		return _instance;
	}

	private NpcTable()
	{
		_npcs = new HashMap<Integer, L2NpcTemplate>();
		_npcsNames = new HashMap<String, L2NpcTemplate>();
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
			try {
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement;
			statement = con.prepareStatement("SELECT * FROM npc");
			ResultSet npcdata = statement.executeQuery();

			fillNpcTable(npcdata);
			npcdata.close();
			statement.close();
			} catch (Exception e) {
				_log.log(Level.SEVERE, "error while creating npc table ", e);
			}
			
			try {
				con = L2DatabaseFactory.getInstance().getConnection();
				PreparedStatement statement = con.prepareStatement("SELECT * FROM npcskills");
				ResultSet npcskills = statement.executeQuery();
				L2NpcTemplate npcDat = null;
				L2Skill npcSkill = null;
	
				while (npcskills.next())
				{
				    int mobId = npcskills.getInt("npcid");
				    npcDat = _npcs.get(mobId);
					if (npcDat == null)
						continue;
				    int skillId = npcskills.getInt("skillid");
				    int level = npcskills.getInt("level");
					npcSkill = SkillTable.getInstance().getInfo(skillId, level);
					if (npcSkill == null)
						continue;
					npcDat.addSkill(npcSkill);
				}
				npcskills.close();
				statement.close();
			} catch (Exception e) {
				_log.log(Level.SEVERE, "error while reading npcskills table ", e);
			}
			
			try {
			PreparedStatement statement2 = con.prepareStatement("SELECT * FROM droplist ORDER BY mobId");
			ResultSet dropData = statement2.executeQuery();
			L2DropData dropDat = null;
			L2NpcTemplate npcDat = null;
			
			while (dropData.next())
			{
			    int mobId = dropData.getInt("mobId");
			    npcDat = _npcs.get(mobId);
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
			} catch (Exception e) {
				_log.log(Level.SEVERE, "error reading npc drops ", e);
			}

			try {
			PreparedStatement statement3 = con.prepareStatement("SELECT npc_id, class_id FROM skill_learn");
			ResultSet learndata = statement3.executeQuery();
			
			while (learndata.next())
			{
				int npc_id = learndata.getInt("npc_id");
				int class_id = learndata.getInt("class_id");
				L2NpcTemplate npc = getTemplate(npc_id);
				npc.addTeachInfo(ClassId.values()[class_id]);
			}

			learndata.close();
			statement3.close();
			} catch (Exception e) {
				_log.log(Level.SEVERE, "error reading npc trainers ", e);
		} 
		} finally {
			try { con.close(); } catch (Exception e) {}
		}
	}

	private void fillNpcTable(ResultSet NpcData)
			throws Exception
	{
		while (NpcData.next())
		{
			StatsSet npcDat = new StatsSet(); 
			int id = NpcData.getInt("id");
			if (Config.ASSERT) assert id < 1000000; 

			npcDat.set("npcId", id);
			int level = NpcData.getInt("level");
			npcDat.set("level", level);
			npcDat.set("jClass", NpcData.getString("class"));

			npcDat.set("baseShldDef", 0);
			npcDat.set("baseShldRate", 0);
			npcDat.set("baseCritRate",  38);

			npcDat.set("name", NpcData.getString("name"));
			//npcDat.set("name", "");
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
			npcDat.set("baseMpReg", 0.01);

			// constants, until we have stats in DB
			npcDat.set("baseSTR", 30);
			npcDat.set("baseCON", 30);
			npcDat.set("baseDEX", 30);
			npcDat.set("baseINT", 30);
			npcDat.set("baseWIT", 30);
			npcDat.set("baseMEN", 30);

			npcDat.set("baseHpMax", NpcData.getInt("hp"));
			npcDat.set("baseMpMax", NpcData.getInt("mp"));
			npcDat.set("basePAtk", NpcData.getInt("patk"));
			npcDat.set("basePDef", NpcData.getInt("pdef"));
			npcDat.set("baseMAtk", NpcData.getInt("matk"));
			npcDat.set("baseMDef", NpcData.getInt("mdef"));

			npcDat.set("factionId", NpcData.getString("faction_id"));
			npcDat.set("factionRange", NpcData.getInt("faction_range"));

			L2NpcTemplate template = new L2NpcTemplate(npcDat);
			_npcs.put(id, template);
			_npcsNames.put(NpcData.getString("name"), template);
		}
		_log.config(_npcs.size() + " npc templates loaded");
	}
    
    public void saveNpc(L2NpcTemplate npc)
    {
//            java.sql.Connection con = null;
//            try
//            {
//                con = L2DatabaseFactory.getInstance().getConnection();
//                PreparedStatement statement = con.prepareStatement("UPDATE npc SET id = ?, name = ?" +
//                        ", class = ?, collision_radius = ?, collision_height = ?, level = ?" +
//                        ", sex = ?, type = ?, attackrange = ?, hp = ?, mp = ?, exp = ?" +
//                        ", sp = ?, patk = ?, pdef = ?, matk = ?, mdef = ?, atkspd = ?, aggro = ?" +
//                        ", matkspd = ?, rhand = ?, lhand = ?, armor = ?, walkspd = ?, runspd = ? WHERE id = ?;");
//                statement.setInt(1, npc.getNpcId());
//                statement.setString(2, npc.getName());
//                statement.setString(3, npc.getType());
//                statement.setDouble(4, npc.getRadius());
//                statement.setDouble(5, npc.getHeight());
//                statement.setInt(6, npc.getLevel());
//                statement.setString(7, npc.getSex());
//                statement.setString(8, npc.getType());
//                statement.setInt(9, npc.getAttackRange());
//                statement.setInt(10, npc.getHp());
//                statement.setInt(11, npc.getMp());
//                statement.setInt(12, npc.getExp());
//                statement.setInt(13, npc.getSp());
//                statement.setInt(14, npc.getPatk());
//                statement.setInt(15, npc.getPdef());
//                statement.setInt(16, npc.getMatk());
//                statement.setInt(17, npc.getMdef());
//                statement.setInt(18, npc.getAtkspd());
//                statement.setBoolean(19, npc.getAgro());
//                statement.setInt(20, npc.getMatkspd());
//                statement.setInt(21, npc.getRhand());
//                statement.setInt(22, npc.getLhand());
//                statement.setInt(23, npc.getArmor());
//                statement.setInt(24, npc.getWalkSpeed());
//                statement.setInt(25, npc.getRunSpeed());
//                statement.setInt(26, npc.getNpcId());
//                statement.execute();
//                statement.close();
//            }
//            catch (Exception e1)
//            {
//                // problem with storing spawn
//                _log.warning("npc data couldnt be stored in db:"+e1);
//            } 
//            finally 
//            {
//                try { con.close(); } catch (Exception e) {}
//            }
    }

	public boolean isInitialized()
	{
		return _initialized;
	}
    
    public void replaceTemplate(L2NpcTemplate npc)
    {
        _npcs.put(npc.npcId, npc);
		_npcsNames.put(npc.name, npc);
    }

	public L2NpcTemplate getTemplate(int id)
	{
		return _npcs.get(id);
	}
	
	public L2NpcTemplate getTemplateByName(String name)
	{
		return _npcsNames.get(name);
	}
	
	public L2NpcTemplate[] getAllOfLevel(int lvl)
	{
		ArrayList<L2NpcTemplate> list = new ArrayList<L2NpcTemplate>(); 
		for (L2NpcTemplate t : _npcs.values())
		{
			if (t.level == lvl)
				list.add(t);
		}
		return list.toArray(new L2NpcTemplate[list.size()]);
	}
}
