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

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.l2j.L2DatabaseFactory;

/**
 * This class ...
 * 
 * @version $Revision: 1.1.2.1.2.3 $ $Date: 2005/03/27 15:29:33 $
 */
public class ShortCuts
{
    private static Logger _log = Logger.getLogger(ShortCuts.class.getName());

    private L2PcInstance _owner;
    private Map<Integer, L2ShortCut> _shortCuts = new TreeMap<Integer, L2ShortCut>();

    public ShortCuts(L2PcInstance owner)
    {
        _owner = owner;
    }
    
    public L2ShortCut[] getAllShortCuts()
    {
        return _shortCuts.values().toArray(new L2ShortCut[_shortCuts.values().size()]);
    }
    
    public L2ShortCut getShortCut(int slot, int page)
    {
		L2ShortCut sc = _shortCuts.get(slot + page*12);
		// verify shortcut
		if (sc != null && sc.type == L2ShortCut.TYPE_ITEM) {
			if (_owner.getInventory().getItemByObjectId(sc.id) == null) {
				deleteShortCut(sc.slot, sc.page);
				sc = null;
			}
		}
		return sc;
    }
    
    public void registerShortCut(L2ShortCut shortcut)
    {
        L2ShortCut oldShortCut = _shortCuts.put(shortcut.slot+12*shortcut.page, shortcut);
        registerShortCutInDb(shortcut, oldShortCut);
    }

    private void registerShortCutInDb(L2ShortCut shortcut, L2ShortCut oldShortCut)
    {
        if (oldShortCut != null)
        {
            deleteShortCutFromDb(shortcut);
        }
    
        java.sql.Connection con = null;
        try
        {
            con = L2DatabaseFactory.getInstance().getConnection();
        
            PreparedStatement statement = con.prepareStatement("INSERT INTO character_shortcuts (char_obj_id,slot,page,type,shortcut_id,level,unknown) values(?,?,?,?,?,?,?)");
            statement.setInt(1, _owner.getObjectId());
            statement.setInt(2, shortcut.slot);
            statement.setInt(3, shortcut.page);
            statement.setInt(4, shortcut.type);
            statement.setInt(5, shortcut.id);
            statement.setInt(6, shortcut.level);
            statement.setInt(7, shortcut.unk);
            statement.execute();
            statement.close();
        }
        catch (Exception e)
        {
			_log.log(Level.WARNING, "could not store shortcuts:", e);
        } 
        finally 
        {
            try { con.close(); } catch (Exception e) {}
        }
    }
    
    /**
     * @param shortcut
     */
    private void deleteShortCutFromDb(L2ShortCut shortcut)
    {
        java.sql.Connection con = null;
        try
        {
            con = L2DatabaseFactory.getInstance().getConnection();
        
            PreparedStatement statement = con.prepareStatement("DELETE FROM character_shortcuts WHERE char_obj_id=? AND slot=? AND page=?");
            statement.setInt(1, _owner.getObjectId());
            statement.setInt(2, shortcut.slot);
            statement.setInt(3, shortcut.page);
            statement.execute();
            statement.close();
        }
        catch (Exception e)
        {
			_log.log(Level.WARNING, "could not store shortcuts:", e);
        } 
        finally 
        {
            try { con.close(); } catch (Exception e) {}
        }
    }
    
    /**
     * @param slot
     */
    public void deleteShortCut(int slot, int page)
    {
        L2ShortCut old = _shortCuts.remove(slot+page*12);
		if (old != null)
        deleteShortCutFromDb(old);
    }
    
    public void deleteShortCutByObjectId(int objectId)
    {
        L2ShortCut toRemove = null;
        for (L2ShortCut shortcut : _shortCuts.values())
        {
            if (shortcut.type == L2ShortCut.TYPE_ITEM && shortcut.id == objectId)
            {
                toRemove    = shortcut;
                break;
            }
        }
        if (toRemove != null)
        {
            deleteShortCut(toRemove.slot, toRemove.page);
        }
    }
    
    public void restore()
    {
        _shortCuts.clear();
        java.sql.Connection con = null;
        try
        {
            con = L2DatabaseFactory.getInstance().getConnection();
            PreparedStatement statement = con.prepareStatement("SELECT char_obj_id, slot, page, type, shortcut_id, level, unknown FROM character_shortcuts WHERE char_obj_id=?");
            statement.setInt(1, _owner.getObjectId());
            ResultSet rset = statement.executeQuery();
            while (rset.next())
            {
                int slot = rset.getInt("slot");
                int page = rset.getInt("page");
                int type = rset.getInt("type");
                int id = rset.getInt("shortcut_id");
                int level = rset.getInt("level");
                int unk = rset.getInt("unknown");
                
                L2ShortCut sc = new L2ShortCut(slot, page, type, id, level, unk);
                _shortCuts.put(slot+page*12, sc);
            }
            rset.close();
            statement.close();
        }
        catch (Exception e)
        {
			_log.log(Level.WARNING, "could not store shortcuts:", e);
        } 
        finally 
        {
            try { con.close(); } catch (Exception e) {}
        }
		// verify shortcuts
		for (L2ShortCut sc : getAllShortCuts()) {
			if (sc.type == L2ShortCut.TYPE_ITEM) {
				if (_owner.getInventory().getItemByObjectId(sc.id) == null) {
					deleteShortCut(sc.slot, sc.page);
				}
			}
		}
    }
}
