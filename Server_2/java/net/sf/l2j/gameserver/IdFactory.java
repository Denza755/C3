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
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Logger;

import net.sf.l2j.L2DatabaseFactory;

/**
 * This class ...
 * 
 * @version $Revision: 1.3.2.1.2.7 $ $Date: 2005/04/11 10:06:12 $
 */
public class IdFactory
{
	private static Logger _log = Logger.getLogger(IdFactory.class.getName());

	private static final int STACK_SIZE_INCREMENT = 8196;
	
	private static String[] id_updates = 
	{
		"UPDATE items                SET owner_id = ?    WHERE owner_id = ?",
		"UPDATE items                SET object_id = ?   WHERE object_id = ?",
        "UPDATE character_quests     SET char_id = ?     WHERE char_id = ?",
		"UPDATE character_recipebook SET char_id = ?     WHERE char_id = ?",
		"UPDATE character_shortcuts  SET char_obj_id = ? WHERE char_obj_id = ?",
		"UPDATE character_shortcuts  SET shortcut_id = ? WHERE shortcut_id = ? AND type = 1", // items
		"UPDATE character_macroses   SET char_obj_id = ? WHERE char_obj_id = ?",
		"UPDATE character_skills     SET char_obj_id = ? WHERE char_obj_id = ?",
		"UPDATE characters           SET obj_Id = ?      WHERE obj_Id = ?",
		"UPDATE characters           SET clanid = ?      WHERE clanid = ?",
		"UPDATE clan_data            SET clan_id = ?     WHERE clan_id = ?",
		"UPDATE clan_data            SET ally_id = ?     WHERE ally_id = ?",
		"UPDATE clan_data            SET leader_id = ?   WHERE leader_id = ?",
		"UPDATE pets                 SET item_obj_id = ? WHERE item_obj_id = ?",
	};

	private static String[] id_checks = 
	{
		"SELECT object_id   FROM items                WHERE object_id >= ?   AND object_id < ?",
		"SELECT owner_id    FROM items                WHERE object_id >= ?   AND object_id < ?",
		"SELECT char_id     FROM character_quests     WHERE char_id >= ?     AND char_id < ?",
		"SELECT char_id     FROM character_recipebook WHERE char_id >= ?     AND char_id < ?",
		"SELECT char_obj_id FROM character_shortcuts  WHERE char_obj_id >= ? AND char_obj_id < ?",
		"SELECT char_obj_id FROM character_macroses   WHERE char_obj_id >= ? AND char_obj_id < ?",
		"SELECT char_obj_id FROM character_skills     WHERE char_obj_id >= ? AND char_obj_id < ?",
		"SELECT obj_Id      FROM characters           WHERE obj_Id >= ?      AND obj_Id < ?",
		"SELECT clanid      FROM characters           WHERE clanid >= ?      AND clanid < ?",
		"SELECT clan_id     FROM clan_data            WHERE clan_id >= ?     AND clan_id < ?",
		"SELECT ally_id     FROM clan_data            WHERE ally_id >= ?     AND ally_id < ?",
		"SELECT leader_id   FROM clan_data            WHERE leader_id >= ?   AND leader_id < ?",
		"SELECT item_obj_id FROM pets                 WHERE item_obj_id >= ? AND item_obj_id < ?",
	};

	private int _curOID;
	private int[] _freeOIDs;
	private int _freeSize;
	
	private boolean initialized;
	
	private static int FIRST_OID = 0x10000000;  
	private static final IdFactory _instance = new IdFactory();

	private IdFactory()
	{
    	_curOID = FIRST_OID;
		_freeOIDs = new int[STACK_SIZE_INCREMENT];
		_freeSize = 0;
        
        java.sql.Connection con = null;
        try
        {
            con = L2DatabaseFactory.getInstance().getConnection();
			// create a temporary table
			Statement s = con.createStatement();
			try {
				s.executeUpdate("drop table tmp_obj_id");
			} catch (SQLException e) {/* ignore */}
			s.executeUpdate("create table tmp_obj_id" +
					" (object_id int NOT NULL PRIMARY KEY)");
			
			s.executeUpdate("insert into tmp_obj_id (object_id)" +
					" select obj_id from characters");
			s.executeUpdate("insert into tmp_obj_id (object_id)" +
					" select object_id from items");
			s.executeUpdate("insert into tmp_obj_id (object_id)" +
					" select clan_id from clan_data");
			
	        ResultSet result = s.executeQuery(
					"select count(object_id) from tmp_obj_id ORDER BY object_id");
			
			result.next();
			int[] tmp_obj_ids = new int[result.getInt(1)];
			result.close();
			
	        result = s.executeQuery(
					"select object_id from tmp_obj_id ORDER BY object_id");
			int idx = 0;
	        while (result.next())
	        {
				tmp_obj_ids[idx++] = result.getInt(1);
	        }
	        result.close();
			
			s.executeUpdate("drop table tmp_obj_id");
			s.close();

			int N = tmp_obj_ids.length;
	        for (idx=0; idx < N; idx++)
	        {
				N = insertUntil(tmp_obj_ids, idx, N, con);
	        }
			
            _curOID++;
            _log.config("next usable object ID is:"+_curOID);
			initialized = true;
        }
        catch (Exception e1)
        {
            _log.severe("ID Factory could not be initialized correctly:"+e1);
        } 
        finally 
        {
            try { con.close(); } catch (Exception e) {}
        }
	}

	public boolean isInitialized() {
		return initialized;
	}
	
	private int insertUntil(int[] tmp_obj_ids, int idx, int N, java.sql.Connection con)
		throws SQLException 
        {
		int id = tmp_obj_ids[idx];
		if (id == _curOID) {
			_curOID++;
			return N;
		}
			// check these IDs not present in DB
			for (String check : id_checks) {
				PreparedStatement ps = con.prepareStatement(check);
				ps.setInt(1, _curOID);
				ps.setInt(2, id);
				ResultSet rs = ps.executeQuery();
				while (rs.next()) {
					int badId = rs.getInt(1);
		            _log.severe("Bad ID "+badId+" in DB found by: "+check);
					throw new RuntimeException();
				}
			rs.close();
			ps.close();
		}
		int hole = id - _curOID;
		if (hole > N - idx)
			hole = N - idx;
		for (int i=1; i <= hole; i++) {
			id = tmp_obj_ids[N-i];
			System.out.println("Compacting DB object ID="+id+" into "+(_curOID));
			for (String update : id_updates) {
				PreparedStatement ps = con.prepareStatement(update);
				ps.setInt(1, _curOID);
				ps.setInt(2, id);
				ps.execute();
				ps.close();
			}
			_curOID++;
        }
		if (hole < N - idx)
		_curOID++;
		return N - hole;
    }
	
	public static IdFactory getInstance()
	{
		return _instance;
	}


	public synchronized int getNextId()
	{
		if (_freeSize == 0)
			return _curOID++;
		else
			return _freeOIDs[--_freeSize];
	}
	
	/**
	 * return a used Object ID back to the pool
	 * @param object ID
	 */
	public synchronized void releaseId(int id)
	{
		if (_freeSize >= _freeOIDs.length) {
			int[] tmp = new int[_freeSize + STACK_SIZE_INCREMENT];
			System.arraycopy(_freeOIDs, 0, tmp, 0, _freeSize);
			_freeOIDs = tmp;
		}
		_freeOIDs[_freeSize++] = id;
	}
}
