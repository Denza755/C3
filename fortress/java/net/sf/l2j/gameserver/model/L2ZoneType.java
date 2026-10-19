package net.sf.l2j.gameserver.model;

import java.util.List;
import java.util.logging.Logger;

import javolution.util.FastList;

public class L2ZoneType
{
    protected static Logger _log = Logger.getLogger(L2ZoneType.class.getName());

    // =========================================================
    // Data Field
    private String _TypeName;
    private List<L2Zone> _Zones;

    public static enum ZoneType
    {
        Arena,
        ArenaSpawn,
        CastleArea,
        CastleDefenderSpawn,
        ClanHall,
        Peace,
        SiegeBattleField,
        Town,
        Underground,
        Water
    }
    
    public static String[] ZoneTypeName =
        {
             "Arena", "Arena Spawn", "Castle Area",
             "Castle Defender Spawn", "Clan Hall", "Peace",
             "Siege Battlefield", "Town", "Underground",
             "Water"
        };
    
    public static String getZoneTypeName(ZoneType zt)
    {
        return ZoneTypeName[zt.ordinal()];
    }

    // =========================================================
    // Constructor
    public L2ZoneType(String typeName)
    {
        _TypeName = typeName.trim();
    }

    // =========================================================
    // Method - Public
    public int addZone(int id, String zoneName, int taxById)
    {
        getZones().add(new L2Zone(id, zoneName, taxById));
        return getZones().size() - 1;
    }

    public int addZoneCoord(String zoneName, int x1, int y1, int x2, int y2, int z)
    {
        return getZone(zoneName).addCoord(x1, y1, x2, y2, z);
    }
    
    public boolean checkIfInZone(L2Object obj)
    {
        return checkIfInZone(obj.getX(), obj.getY());
    }
    
    public boolean checkIfInZone(int x, int y)
    {
        for (L2Zone zone: getZones())
        {
            if (zone.checkIfInZone(x, y)) return true;
        }
        return false;
    }

    // =========================================================
    // Method - Private

    // =========================================================
    // Property - Public
    public final String getTypeName()
    {
        return _TypeName;
    }
    
    public final L2Zone getZone(String zoneName)
    {
        for (L2Zone z: getZones())
        {
            if (z.getName().equalsIgnoreCase(zoneName.trim()))
                return z;
        }

        getZones().add(new L2Zone(getZones().size() + 1, zoneName, 0));
        return getZones().get(getZones().size() - 1);
    }
    
    public final L2Zone getZone(int x, int y)
    {
        for (L2Zone z: getZones())
        {
            if (z.checkIfInZone(x, y))
                return z;
        }
        return null;
    }
    
    public final List<L2Zone> getZones()
    {
        if (_Zones == null) _Zones = new FastList<L2Zone>();
        return _Zones;
    }
}