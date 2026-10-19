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
import net.sf.l2j.gameserver.model.L2Clan;
import net.sf.l2j.gameserver.model.L2ClanMember;
import net.sf.l2j.gameserver.model.L2PcInstance;

/**
 * This class ...
 * 
 * @version $Revision: 1.11.2.5.2.5 $ $Date: 2005/03/27 15:29:18 $
 */
public class ClanTable
{
	private static Logger _log = Logger.getLogger(ClanTable.class.getName());
	
	private static ClanTable _instance;
	
	private HashMap<Integer, L2Clan> _clans;
	
	public static ClanTable getInstance()
	{
		if (_instance == null)
		{
			_instance = new ClanTable();
		}
		return _instance;
	}
	
	private ClanTable()
	{
		_clans = new HashMap<Integer, L2Clan>();
	}
	
	/**
	 * @param clanId
	 * @return
	 */
	public L2Clan getClan(int clanId)
	{
        L2Clan clan = _clans.get(new Integer(clanId));
        if (clan == null)
        {
            clan = new L2Clan(clanId);
            if (clan != null)
            {
                _clans.put(clan.getClanId(), clan);
            }
        }
        
		return clan; 
	}
	
	
	/**
	 * @param player
	 * @return NULL if clan with same name already exists
	 */
	public L2Clan createClan(L2PcInstance player, String clanName)
	{
        java.sql.Connection con = null;
        boolean clanExists = true;
        L2Clan clan = null;
        
        try//store the new clan in db
        {
            con = L2DatabaseFactory.getInstance().getConnection();
            PreparedStatement statement = con.prepareStatement("SELECT clan_id FROM clan_data WHERE clan_name=?");
            statement.setString(1, clanName);
            ResultSet result = statement.executeQuery();
            clanExists = result.next(); 
            result.close();
            statement.close();
        }
        catch (Exception e)
        {
            _log.warning("error while saving new clan to db "+e);
        }
        finally
        {
            try { con.close(); } catch (Exception e) {}
        }

        if (!clanExists)
        {
            // no clan with same name exists
            L2ClanMember leader = new L2ClanMember(player.getName(), player.getLevel(), player.getClassId().getId(), player.getObjectId());
            clan = new L2Clan(IdFactory.getInstance().getNextId(), clanName, leader);
            clan.store();
            player.setClan(clan);

            if (Config.DEBUG) _log.fine("New clan created: "+clan.getClanId() + " " +clan.getName());
            
            _clans.put(new Integer(clan.getClanId()), clan);
        }
		
		return clan;
	}
}
