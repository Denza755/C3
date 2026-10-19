package net.sf.l2j.gameserver.model;

import java.util.List;
import java.util.logging.Logger;

import javolution.util.FastList;
import net.sf.l2j.gameserver.util.Util;

public class L2Zone
{
    protected static Logger _log = Logger.getLogger(L2Zone.class.getName());

    // =========================================================
    // Data Field
    private List<int[]> _Coords;
    private int _Id;
    private String _Name;
    private int _TaxById;

    // =========================================================
    // Constructor
    public L2Zone(int id, String zoneName, int taxById)
    {
        _Name = zoneName.trim();
        _TaxById = taxById;
    }

    // =========================================================
    // Method - Public
    public int addCoord(int x1, int y1, int x2, int y2, int z)
    {
        getCoords().add(new int[]{x1, y1, x2, y2, z});
        return getCoords().size() - 1;
    }
    
    public boolean checkIfInZone(L2Object obj)
    {
        return checkIfInZone(obj.getX(), obj.getY());
    }
    
    public boolean checkIfInZone(int x, int y)
    {
        return (getCoord(x, y) != null);
    }

    public void setId(int id)
    {
        _Id = id;
    }

    public void setTaxById(int taxById)
    {
        _TaxById = taxById;
    }
    
    // =========================================================
    // Method - Private

    // =========================================================
    // Property - Public
    public int[] getCoord(int x, int y)
    {
        for (int[] coord: getCoords())
        {
            if (x >= Math.min(coord[0], coord[2]) &&
                    x <= Math.max(coord[0], coord[2]) &&
                    y >= Math.min(coord[1], coord[3]) &&
                    y <= Math.max(coord[1], coord[3]))
                return coord;
        }
        return null;
    }
    
    public List<int[]> getCoords()
    {
        if (_Coords == null) _Coords = new FastList<int[]>();
        return _Coords;
    }
    
    public int getId()
    {
        return _Id;
    }
    
    public String getName()
    {
        return _Name;
    }
    
    public int getTaxById()
    {
        return _TaxById;
    }
	
	public final double findDistanceToZone(L2Object obj) { return findDistanceToZone(obj.getX(), obj.getY(), obj.getZ()); }

    public final double findDistanceToZone(int x, int y, int z)
    {
        double closestDistance = 99999999;
        double distance;
		int x2,y2;
        for (int[] coord: getCoords())
        {
			x2 = (coord[0] + coord[2])/2;
			y2 = (coord[1] + coord[3])/2;
            distance = Util.calculateDistance(x, y, z, x2, y2);
            if (distance < closestDistance) closestDistance = distance;
        }
        return closestDistance;
    }

}