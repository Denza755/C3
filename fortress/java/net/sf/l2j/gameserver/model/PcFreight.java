package net.sf.l2j.gameserver.model;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.model.L2ItemInstance.ItemClass;
import net.sf.l2j.gameserver.model.L2ItemInstance.ItemLocation;

public class PcFreight extends Warehouse
{
    //private static final Logger _log = Logger.getLogger(PcFreight.class.getName());

    private L2PcInstance _owner;    // This is the L2PcInstance that owns this Freight;
    private String _locId1;         // This is the loc_data to use in the SELECT stmt;
    private int _locId2;            // This is the loc_data to use in INSERT/UPDATE/DELETE stmt;

    public PcFreight(L2PcInstance owner)
    {
        _owner = owner;
    }

    /**
     * Returns an int identifying the owner for this PcFreight instance
     */
    public int getOwnerId() { return _owner.getObjectId(); }

    /**
     * Returns an ItemLocation identifying the freight location type
     */
    public ItemLocation getLocationType() { return ItemLocation.FREIGHT; }

    /**
     * Returns an String identifying the Location of the freight (for using with SELECT)
     */
    public String getLocationId()
    {
        return _locId1;
    }

    /**
     * Returns an int identifying the Location of the freight (for using with INSERT/UPDATE/DELETE)
     */
    public int getLocationId(@SuppressWarnings("unused") boolean addItem)
    {
        return _locId2;
    }

    /**
     * Sets the Location of the freight based on the place of activeChar or _owner if activeChar is null
     */
    public void setLocationId(L2PcInstance activeChar)
    {
        if (activeChar == null)
            _locId2 = _owner.getCurrentZoneIndex(L2World.zoneTown);
        else
            _locId2 = activeChar.getCurrentZoneIndex(L2World.zoneTown);

                if (!Config.ALT_GAME_FREIGHTS)
            _locId1 = String.valueOf(_locId2);
                else
                {
            _locId1 = "0";
            for (int i = 1; i <= L2World.zoneTown.length; i++)
                _locId1 += "," + i;
        }
    }

    public ItemClass getItemClass()	{ return ItemClass.ALL;	}

}