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
import java.util.logging.Logger;

import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.model.L2CastleGuardInstance;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;

/**
 * This class ...
 * 
 * @author Nightmare
 * @version $Revision: 1.5.2.6.2.7 $ $Date: 2005/03/27 15:29:18 $
 */
public class CastleGuardsSpawner
{
	private static Logger _log = Logger.getLogger(SpawnTable.class.getName());
    private static CastleGuardsSpawner _instance;
    public static ArrayList<Integer> _castleGuards;
    
	private CastleGuardsSpawner()
	{
	    java.sql.Connection con = null;
	    try
	    {
	        con = L2DatabaseFactory.getInstance().getConnection();
	        PreparedStatement statement = con.prepareStatement("SELECT object_id, template_id FROM castle_guards");
	        ResultSet rset = statement.executeQuery();
	        
	        L2NpcTemplate template1;
	        while (rset.next())
	        {
	            template1 = NpcTable.getInstance().getTemplate(rset.getInt("template_id"));
	            
	            L2CastleGuardInstance castleguard = new L2CastleGuardInstance(rset.getInt("object_id"), template1);
	            castleguard.setCurrentHpMp(castleguard.getMaxHp(), castleguard.getMaxMp());
	            castleguard.spawnMe(castleguard.x, castleguard.y, castleguard.z);
	            castleguard.OnSpawn();
                if (_castleGuards == null)
                {
                    _castleGuards   = new ArrayList<Integer>();
                }
	            _castleGuards.add(rset.getInt("object_id"));
	        }		
	        rset.close();
	        statement.close();
	    }
	    catch (Exception e1)
	    {
	        // problem with initializing spawn, go to next one
	        _log.warning("spawn couldnt be initialized:"+e1);
	    } 
	    finally 
	    {
	        try { con.close(); } catch (Exception e) {}
	    }
	    if(_castleGuards != null) _log.config("CastleGuards Loaded " + L2CastleGuardInstance._castleGuards.size() + " Instances Created");
	}
	public static CastleGuardsSpawner getInstance()
	{
		if (_instance == null)
		{
			_instance = new CastleGuardsSpawner();
		}
		return _instance;
	}
	
}
