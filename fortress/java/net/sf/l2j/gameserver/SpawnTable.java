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
import java.util.HashMap;
import java.util.logging.Logger;

import net.sf.l2j.Config;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.model.L2Spawn;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;

/**
 * This class ...
 * 
 * @author Nightmare
 * @version $Revision: 1.5.2.6.2.7 $ $Date: 2005/03/27 15:29:18 $
 */
public class SpawnTable
{
    private static Logger _log = Logger.getLogger(SpawnTable.class.getName());

    private static final SpawnTable _instance = new SpawnTable();

    private HashMap<Integer, L2Spawn> _spawntable;
    private int _npcSpawnCount;

    private int _highestId;

    public static SpawnTable getInstance()
    {
        return _instance;
    }

    private SpawnTable()
    {
        fillSpawnTable();
    }

    public HashMap<Integer, L2Spawn> getSpawnTable()
    {
        return _spawntable;
    }

    /**
     * TODO remove unneeded fields: RandomX, RandomY, Location
     * from dp and code
     */

    private void fillSpawnTable()
    {
//		_log.info("SpawnTable init start");

		L2World.deleteVisibleNpcSpawns();
        GmListTable.broadcastMessageToGMs("NPC Unspawn completed!");

        _spawntable = new HashMap<Integer, L2Spawn>();
        java.sql.Connection con = null;

        try
        {
            con = L2DatabaseFactory.getInstance().getConnection();
            PreparedStatement statement = con.prepareStatement("SELECT id, location, count, npc_templateid, locx, locy, locz, randomx, randomy, heading, respawn_delay, loc_id, respawn_time FROM spawnlist ORDER by id");
            ResultSet rset = statement.executeQuery();

            L2Spawn spawnDat;
            L2NpcTemplate template1;

            while (rset.next())
            {
                template1 = NpcTable.getInstance().getTemplate(rset.getInt("npc_templateid"));
                if (template1 != null)
                {
                    if (template1.type.equalsIgnoreCase("L2SiegeGuard"))
                    {
                        // Don't spawn
                    }
                    else if (Config.ALLOW_CLASS_MASTERS_LIST.isEmpty() && template1.type.equals("L2ClassMaster"))
                    {
                        // Dont' spawn class masters
                    }
                    else
                    {
                        spawnDat = new L2Spawn(template1);
                        spawnDat.setId(rset.getInt("id"));
                        spawnDat.setAmount(rset.getInt("count"));
                        spawnDat.setLocx(rset.getInt("locx"));
                        spawnDat.setLocy(rset.getInt("locy"));
                        spawnDat.setLocz(rset.getInt("locz"));
                        spawnDat.setHeading(rset.getInt("heading"));
                        spawnDat.setRespawnDelay(rset.getInt("respawn_delay"));
                        spawnDat.setRespawnTime(rset.getInt("respawn_time"));
                        spawnDat.setLocation(rset.getInt("loc_id"));

                        _npcSpawnCount += spawnDat.init();

                        _spawntable.put(spawnDat.getId(), spawnDat);
                        if (spawnDat.getId() > _highestId)
                            _highestId = spawnDat.getId();
                    }
                }
                else
                {
                    _log.warning("mob data for id:" + rset.getInt("npc_templateid") +
                                 " missing in npc table");
                }
            }

            rset.close();
            statement.close();
        }
        catch (Exception e1)
        {
            // problem with initializing spawn, go to next one
            _log.warning("spawn couldnt be initialized:" + e1);
        }
        finally
        {
            try { con.close(); } catch (Exception e) {_log.warning("ExcST:137");}
        }
        _log.config("SpawnTable: Loaded " + _spawntable.size() + " Npc Spawn Locations. Total NPCs: "+_npcSpawnCount);
        if (Config.DEBUG) _log.fine("Spawning completed, total number of NPCs in the world: " + _npcSpawnCount);
    }

    public L2Spawn getTemplate(int id)
    {
        return _spawntable.get(id);
    }

    public void addNewSpawn(L2Spawn spawn, boolean storeInDb)
    {
        _highestId++;
        spawn.setId(_highestId);
        _spawntable.put(new Integer(spawn.getId()), spawn);

        if (storeInDb)
        {
            java.sql.Connection con = null;
            try
            {
                /*
                 * TODO: Check that removing id from insert statement works
                 * as id should be set to autoincrement. 
                 */
                con = L2DatabaseFactory.getInstance().getConnection();
                PreparedStatement statement = con.prepareStatement("INSERT INTO spawnlist (location,count,npc_templateid,locx,locy,locz,heading,respawn_delay,loc_id, respawn_time) values(?,?,?,?,?,?,?,?,?,?)");
                statement.setString(1, "");// spawn.getLocation());
                statement.setInt(2, spawn.getAmount());
                statement.setInt(3, spawn.getNpcid());
                statement.setInt(4, spawn.getLocx());
                statement.setInt(5, spawn.getLocy());
                statement.setInt(6, spawn.getLocz());
                //                statement.setInt(7, 0);
                //                statement.setInt(8, 0);				
                statement.setInt(7, spawn.getHeading());
                statement.setInt(8, spawn.getRespawnDelay());
                statement.setInt(9, spawn.getLocation());
                statement.setInt(10, spawn.getRespawnTime());
                statement.execute();
                statement.close();
            }
            catch (Exception e1)
            {
                // problem with storing spawn
                _log.warning("spawn couldnt be stored in db:" + e1);
            }
            finally
            {
                try { con.close(); } catch (Exception e) {_log.warning("ExcST:186");}
            }
        }
    }

    public void deleteSpawn(L2Spawn spawn, boolean updateDb)
    {
        if (_spawntable.remove(new Integer(spawn.getId())) != null && updateDb)
        {
            java.sql.Connection con = null;
            try
            {
                con = L2DatabaseFactory.getInstance().getConnection();
                PreparedStatement statement = con.prepareStatement("DELETE FROM spawnlist WHERE id=?");
                statement.setInt(1, spawn.getId());
                statement.execute();
                statement.close();
            }
            catch (Exception e1)
            {
                // problem with deleting spawn
                _log.warning("spawn couldnt be deleted in db:" + e1);
            }
            finally
            {
                try { con.close(); } catch (Exception e) {_log.warning("ExcST:211");}
            }
        }
    }
    
    //just wrapper
    public void reloadAll()
    {
        fillSpawnTable();
    }
}
