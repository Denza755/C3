package net.sf.l2j.gameserver.instancemanager;

import java.util.List;
import java.util.logging.Logger;

import net.sf.l2j.gameserver.MapRegionTable;
import net.sf.l2j.gameserver.model.entity.Town;
import net.sf.l2j.gameserver.model.entity.Zone;
import net.sf.l2j.gameserver.model.entity.ZoneType;
import net.sf.l2j.gameserver.model.L2Object;

import javolution.util.FastList;

public class TownManager
{
    protected static Logger _log = Logger.getLogger(ClanHallManager.class.getName());

    // =========================================================
    // Data Field
    private static TownManager _Instance;
    private List<Town> _Towns;
    
    // =========================================================
    // Constructor
    public TownManager()
    {
        load();
    }

    // =========================================================
    // Method - Public
    /** Return true if object is inside zone */
    public final boolean checkIfInZone(L2Object obj) { return (getTown(obj) != null); }

    /** Return true if object is inside zone */
    public final boolean checkIfInZone(int x, int y) { return (getTown(x, y) != null); }

    // =========================================================
    // Method - Private
    private final void load()
    {
        // TEMP UNTIL TOWN'S TABLE IS ADDED
        for (Zone zone: ZoneManager.getInstance().getZones(ZoneType.getZoneTypeName(ZoneType.ZoneTypeEnum.Town)))
            getTowns().add(new Town(zone.getId()));
    }

    // =========================================================
    // Property - Public
    public static final TownManager getInstance()
    {
        if (_Instance == null) _Instance = new TownManager();
        return _Instance;
    }

    public final Town getClosestTown(L2Object activeObject)
    {
        switch (MapRegionTable.getInstance().getMapRegion(activeObject.getX(), activeObject.getY()))
        {
            case 0: return getTown(2);      // TI
            case 1: return getTown(3);      // Elven
            case 2: return getTown(1);      // DE
            case 3: return getTown(4);      // Orc
            case 4: return getTown(6);      // Dwarven
            case 5: return getTown(7);      // Gludio
            case 6: return getTown(5);      // Gludin
            case 7: return getTown(8);      // Dion
            case 8: return getTown(9);      // Giran
            case 9: return getTown(10);     // Oren
            case 10: return getTown(12);    // Aden
            case 11: return getTown(11);    // HV
            case 12: return getTown(16);    // Floran
            case 13: return getTown(15);    // Heine
        }

        return getTown(16); // Default to floran
    }

    public final Town getTown(int townId)
    {
        int index = getTownIndex(townId);
        if (index >= 0) return getTowns().get(index);
        return null;
    }

    public final Town getTown(L2Object activeObject) { return getTown(activeObject.getX(), activeObject.getY()); }

    public final Town getTown(int x, int y)
    {
        int index = getTownIndex(x, y);
        if (index >= 0) return getTowns().get(index);
        return null;
    }

    public final Town getTown(String name)
    {
        int index = getTownIndex(name);
        if (index >= 0) return getTowns().get(index);
        return null;
    }

    public final int getTownIndex(int townId)
    {
        Town town;
        for (int i = 0; i < getTowns().size(); i++)
        {
            town = getTowns().get(i);
            if (town != null && town.getTownId() == townId) return i;
        }
        return -1;
    }

    public final int getTownIndex(L2Object activeObject) { return getTownIndex(activeObject.getX(), activeObject.getY()); }

    public final int getTownIndex(int x, int y)
    {
        Town town;
        for (int i = 0; i < getTowns().size(); i++)
        {
            town = getTowns().get(i);
            if (town != null && town.checkIfInZone(x, y)) return i;
        }
        return -1;
    }

    public final int getTownIndex(String name)
    {
        Town town;
        for (int i = 0; i < getTowns().size(); i++)
        {
            town = getTowns().get(i);
            if (town != null && town.getName().equalsIgnoreCase(name.trim())) return i;
        }
        return -1;
    }

    public final List<Town> getTowns()
    {
        if (_Towns == null) _Towns = new FastList<Town>();
        return _Towns;
    }
}