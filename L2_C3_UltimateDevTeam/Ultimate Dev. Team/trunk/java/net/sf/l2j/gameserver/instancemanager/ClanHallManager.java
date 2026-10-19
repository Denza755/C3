package net.sf.l2j.gameserver.instancemanager;

import javolution.util.FastList;
import net.sf.l2j.gameserver.model.L2Clan;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.entity.ClanHall;

import java.util.List;
import java.util.logging.Logger;

public class ClanHallManager
{
    protected static Logger _log = Logger.getLogger(ClanHallManager.class.getName());

    // =========================================================
    // Data Field
    private static ClanHallManager _Instance;
    private List<ClanHall> _ClanHalls;
    
    // =========================================================
    // Constructor
    public ClanHallManager()
    {
        load();
    }

    // =========================================================
    // Method - Public
    /** Return true if object is inside zone */
    public final boolean checkIfInZone(L2Object obj) { return (getClanHall(obj) != null); }

    /** Return true if object is inside zone */
    public final boolean checkIfInZone(int x, int y) { return (getClanHall(x, y) != null); }

    // =========================================================
    // Method - Private
    public final void reloadAll()
    {
	_ClanHalls = null;
	load();
    }
    private void load()
    {
    }

    // =========================================================
    // Property - Public
    public static ClanHallManager getInstance()
    {
        if (_Instance == null) _Instance = new ClanHallManager();
        return _Instance;
    }

    public final ClanHall getClanHall(int clanHallId)
    {
        int index = getClanHallIndex(clanHallId);
        if (index >= 0) return getClanHalls().get(index);
        return null;
    }

    public final ClanHall getClanHall(L2Object activeObject) { return getClanHall(activeObject.getPosition().getX(), activeObject.getPosition().getY()); }

    public final ClanHall getClanHall(int x, int y)
    {
        int index = getClanHallIndex(x, y);
        if (index >= 0) return getClanHalls().get(index);
        return null;
    }

    public final ClanHall getClanHall(String name)
    {
        int index = getClanHallIndex(name);
        if (index >= 0) return getClanHalls().get(index);
        return null;
    }

    public final ClanHall getClanHallByOwner(L2Clan clan)
    {
        int index = getClanHallIndexByOwner(clan);
        if (index >= 0) return getClanHalls().get(index);
        return null;
    }

    public final int getClanHallIndex(int clanHallId)
    {
        ClanHall clanHall;
        for (int i = 0; i < getClanHalls().size(); i++)
        {
            clanHall = getClanHalls().get(i);
            if (clanHall != null && clanHall.getClanHallId() == clanHallId) return i;
        }
        return -1;
    }

    public final int getClanHallIndex(L2Object activeObject) { return getClanHallIndex(activeObject.getPosition().getX(), activeObject.getPosition().getY()); }

    public final int getClanHallIndex(int x, int y)
    {
        ClanHall clanHall;
        for (int i = 0; i < getClanHalls().size(); i++)
        {
            clanHall = getClanHalls().get(i);
            if (clanHall != null && clanHall.checkIfInZone(x, y)) return i;
        }
        return -1;
    }

    public final int getClanHallIndex(String name)
    {
        ClanHall clanHall;
        for (int i = 0; i < getClanHalls().size(); i++)
        {
            clanHall = getClanHalls().get(i);
            if (clanHall != null && clanHall.getName().equalsIgnoreCase(name.trim())) return i;
        }
        return -1;
    }

    public final int getClanHallIndexByOwner(L2Clan clan)
    {
        if (clan == null) return -1;
        
        ClanHall clanHall;
        for (int i = 0; i < getClanHalls().size(); i++)
        {
            clanHall = getClanHalls().get(i);
            if (clanHall != null && clanHall.getOwnerId() == clan.getClanId()) return i;
        }
        return -1;
    }

    public final List<ClanHall> getClanHalls()
    {
        if (_ClanHalls == null) _ClanHalls = new FastList<ClanHall>();
        return _ClanHalls;
    }
}
